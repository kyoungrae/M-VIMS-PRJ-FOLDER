package com.system.common.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.system.common.util.userinfo.UserInfo;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * CRUD 감사 로그 인터셉터
 * INSERT, UPDATE, DELETE 수행 시 SYS_ADT_LOG 테이블에 로그를 남깁니다.
 */
@Component
@Intercepts({
        @Signature(type = Executor.class, method = "update", args = { MappedStatement.class, Object.class })
})
public class EventLogInterceptor implements Interceptor {
    private static final Logger logger = LoggerFactory.getLogger(EventLogInterceptor.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final int DEFAULT_QUEUE_CAPACITY = 1000;
    private static final int DEFAULT_MAX_JSON_LENGTH = 8000;
    private static final long REJECTION_WARN_INTERVAL_MS = 60_000L;
    private static final AtomicLong lastRejectWarnAt = new AtomicLong(0L);

    @Value("${event-log.executor.core-pool-size:2}")
    private int corePoolSize;

    @Value("${event-log.executor.max-pool-size:5}")
    private int maxPoolSize;

    @Value("${event-log.executor.queue-capacity:" + DEFAULT_QUEUE_CAPACITY + "}")
    private int queueCapacity;

    @Value("${event-log.max-json-length:" + DEFAULT_MAX_JSON_LENGTH + "}")
    private int maxJsonLength;

    private volatile ThreadPoolExecutor logExecutor;

    @Autowired
    @Lazy
    private SqlSessionFactory sqlSessionFactory;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        MappedStatement ms = (MappedStatement) invocation.getArgs()[0];
        Object parameter = invocation.getArgs()[1];

        // 1. 감사 로그 자체가 다시 기록되는 무한 루프 방지
        if (ms.getId().contains("SysEventLog")) {
            return invocation.proceed();
        }

        String targetTable = extractTableName(ms);
        if (isExcludedTable(targetTable)) {
            return invocation.proceed();
        }

        String actionType = ms.getSqlCommandType().name();
        String beforeData = null;

        // 2. UPDATE/DELETE의 경우 실행 전 데이터를 조회하여 저장
        if ("UPDATE".equals(actionType) || "DELETE".equals(actionType)) {
            beforeData = captureBeforeData(invocation, ms, parameter);
        }

        // 3. 원래 쿼리 실행
        Object result = invocation.proceed();

        // 4. 로그 기록
        try {
            saveEventLog(invocation, ms, parameter, beforeData);
        } catch (Exception e) {
            logger.error("EventLogInterceptor Error: ", e);
        }

        return result;
    }

    private String captureBeforeData(Invocation invocation, MappedStatement ms, Object parameter) {
        if (parameter == null) {
            logger.debug("EventLog: Parameter is null, skipping captureBeforeData.");
            return null;
        }

        // ID와 해당 필드명을 함께 추출
        Map.Entry<String, String> idEntry = extractIdEntryFromParameter(parameter);
        if (idEntry == null || idEntry.getValue() == null || idEntry.getValue().isEmpty()) {
            logger.debug("EventLog: Could not extract ID from parameter, skipping captureBeforeData. Parameter: {}",
                    parameter);
            return null;
        }

        String idFieldName = idEntry.getKey();
        String targetId = idEntry.getValue();

        try {
            String mapperNamespace = ms.getId().substring(0, ms.getId().lastIndexOf(".") + 1);
            String selectMapperId = mapperNamespace + "SELECT";

            if (!ms.getConfiguration().hasStatement(selectMapperId)) {
                logger.debug(
                        "EventLog: No 'SELECT' statement found for {} (looked for {}), skipping captureBeforeData.",
                        ms.getId(), selectMapperId);
                return null;
            }

            MappedStatement selectMs = ms.getConfiguration().getMappedStatement(selectMapperId);
            Executor executor = (Executor) invocation.getTarget();

            // 조회를 위한 파라미터 설정 - 추출된 특정 필드명과 공용 'id' 키만 사용함
            Map<String, Object> queryParam = new HashMap<>();
            queryParam.put("id", targetId);
            queryParam.put("ID", targetId);
            if (idFieldName != null && !idFieldName.equals("id") && !idFieldName.equals("ID")) {
                queryParam.put(idFieldName, targetId);
            }

            logger.debug("EventLog: Attempting select for bfr_data. Mapper: {}, QueryParam: {}", selectMapperId,
                    queryParam);

            java.util.List<Object> list = executor.query(selectMs, queryParam,
                    org.apache.ibatis.session.RowBounds.DEFAULT, Executor.NO_RESULT_HANDLER);

            if (list != null && !list.isEmpty()) {
                String captured = toLimitedJson(list.get(0));
                logger.debug("EventLog: Successfully captured bfr_data from {}. Length: {}", selectMapperId,
                        captured.length());
                return captured;
            } else {
                logger.debug("EventLog: No record found in {} for ID {}", selectMapperId, targetId);
            }
        } catch (Exception e) {
            logger.warn("EventLog: Error during captureBeforeData for {}: {}", ms.getId(), e.getMessage());
        }
        return null;
    }

    private Map.Entry<String, String> extractIdEntryFromParameter(Object parameter) {
        if (parameter == null)
            return null;

        // 1. Map 형태인 경우 (MyBatis의 ParamMap 포함)
        if (parameter instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) parameter;

            if (map.containsKey("id")) {
                Object idObj = map.get("id");
                if (idObj instanceof String || idObj instanceof Number) {
                    return new AbstractMap.SimpleEntry<>("id", String.valueOf(idObj));
                }
                return extractIdEntryFromParameter(idObj);
            }

            // MyBatis 기본 파라미터명 확인 (param1, arg0 등)
            if (map.containsKey("param1")) {
                return extractIdEntryFromParameter(map.get("param1"));
            }
            if (map.containsKey("arg0")) {
                return extractIdEntryFromParameter(map.get("arg0"));
            }

            for (Object value : map.values()) {
                if (value == parameter)
                    continue;
                Map.Entry<String, String> entry = extractIdEntryFromParameter(value);
                if (entry != null && entry.getValue() != null && !entry.getValue().isEmpty())
                    return entry;
            }
        }
        // 2. 기본 타입(String, Number)인 경우 - 자체가 ID일 확률이 높음
        else if (parameter instanceof String || parameter instanceof Number) {
            return new AbstractMap.SimpleEntry<>("id", String.valueOf(parameter));
        }
        // 3. 일반 객체(VO 등)인 경우
        else {
            try {
                // 1) "id" 라는 필드를 우선적으로 찾음
                Field idField = findField(parameter.getClass(), "id");
                if (idField != null) {
                    idField.setAccessible(true);
                    Object idObj = idField.get(parameter);
                    return new AbstractMap.SimpleEntry<>("id", idObj != null ? idObj.toString() : "");
                }

                // 2) @Id 어노테이션이 붙은 필드를 찾음 (JPA 어노테이션 활용)
                for (Field field : parameter.getClass().getDeclaredFields()) {
                    if (field.isAnnotationPresent(jakarta.persistence.Id.class)) {
                        field.setAccessible(true);
                        Object idObj = field.get(parameter);
                        return new AbstractMap.SimpleEntry<>(field.getName(), idObj != null ? idObj.toString() : "");
                    }
                }

                // 3) "keys" 필드 분석 (시스템 관례)
                Field keysField = findField(parameter.getClass(), "keys");
                if (keysField != null) {
                    keysField.setAccessible(true);
                    Object keysObj = keysField.get(parameter);
                    if (keysObj instanceof String) {
                        String keysVal = (String) keysObj;
                        if (!keysVal.isEmpty()) {
                            String cleanKeys = keysVal.replace("[", "").replace("]", "");
                            String firstKey = cleanKeys.split(",")[0].trim();
                            Field pkField = findField(parameter.getClass(), firstKey);
                            if (pkField != null) {
                                pkField.setAccessible(true);
                                Object idObj = pkField.get(parameter);
                                return new AbstractMap.SimpleEntry<>(firstKey, idObj != null ? idObj.toString() : "");
                            }
                        }
                    }
                }
            } catch (Exception e) {
                // ignore
            }
        }
        return new AbstractMap.SimpleEntry<>("id", "");
    }

    // 로그 기록에서 제외할 테이블 목록 (Mapper 명칭 기준)
    private static final List<String> EXCLUDED_TABLES = Arrays.asList("SysAccsLog", "DbPartition");

    private void saveEventLog(Invocation invocation, MappedStatement ms, Object parameter, String beforeData)
            throws Exception {
        final String actionType = ms.getSqlCommandType().name();
        final String targetTable = extractTableName(ms);

        // 1. 제외 대상 테이블인지 확인
        if (isExcludedTable(targetTable)) {
            return;
        }

        Map.Entry<String, String> idEntry = extractIdEntryFromParameter(parameter);
        final String targetId = idEntry != null ? idEntry.getValue() : "";

        // 2. 유저 정보 추출 및 Null 처리
        String userEmailTemp = UserInfo.getUserEmail();
        if (userEmailTemp == null || userEmailTemp.trim().isEmpty()) {
            userEmailTemp = "ANONYMOUS";
        }
        final String userEmail = userEmailTemp;

        String userRolesTemp = UserInfo.getUserRoles();
        if (userRolesTemp == null || userRolesTemp.trim().isEmpty()) {
            userRolesTemp = "GUEST";
        }
        final String userRoles = userRolesTemp;

        getLogExecutor().execute(() -> {
            try (SqlSession sqlSession = sqlSessionFactory.openSession(true)) {
                Map<String, Object> logData = new HashMap<>();
                logData.put("id", UUID.randomUUID().toString().replace("-", ""));
                logData.put("user_id", userEmail);
                logData.put("email", userEmail);
                logData.put("role", userRoles);
                logData.put("act_type", actionType);
                logData.put("tgt_tbl", targetTable);
                logData.put("tgt_id", targetId);
                logData.put("bfr_data", beforeData);

                try {
                    if (parameter != null) {
                        logData.put("aft_data", toLimitedJson(parameter));
                    }
                } catch (Exception e) {
                    logData.put("aft_data", truncate(parameter != null ? parameter.toString() : ""));
                }

                sqlSession.insert("com.system.common.eventlog.SysEventLogMapper.INSERT", logData);

            } catch (Exception e) {
                logger.error("Async EventLog INSERT failed: ", e);
            }
        });
    }

    private ThreadPoolExecutor getLogExecutor() {
        ThreadPoolExecutor current = logExecutor;
        if (current != null) {
            return current;
        }

        synchronized (this) {
            if (logExecutor == null) {
                int safeCorePoolSize = Math.max(1, corePoolSize);
                int safeMaxPoolSize = Math.max(safeCorePoolSize, maxPoolSize);
                int safeQueueCapacity = Math.max(1, queueCapacity);

                logExecutor = new ThreadPoolExecutor(
                        safeCorePoolSize,
                        safeMaxPoolSize,
                        60L,
                        TimeUnit.SECONDS,
                        new ArrayBlockingQueue<>(safeQueueCapacity),
                        runnable -> {
                            Thread thread = new Thread(runnable, "event-log-writer");
                            thread.setDaemon(true);
                            return thread;
                        },
                        (runnable, executor) -> {
                            long now = System.currentTimeMillis();
                            long previous = lastRejectWarnAt.get();
                            if (now - previous >= REJECTION_WARN_INTERVAL_MS
                                    && lastRejectWarnAt.compareAndSet(previous, now)) {
                                logger.warn(
                                        "EventLog queue is full. Dropping audit log tasks to protect application CPU. active={}, queued={}, completed={}",
                                        executor.getActiveCount(), executor.getQueue().size(),
                                        executor.getCompletedTaskCount());
                            }
                        });
            }
            return logExecutor;
        }
    }

    private String toLimitedJson(Object value) throws Exception {
        return truncate(objectMapper.writeValueAsString(value));
    }

    private String truncate(String value) {
        if (value == null) {
            return null;
        }
        int safeMaxJsonLength = Math.max(0, maxJsonLength);
        if (safeMaxJsonLength == 0 || value.length() <= safeMaxJsonLength) {
            return value;
        }
        return value.substring(0, safeMaxJsonLength) + "...[truncated]";
    }

    @PreDestroy
    public void shutdown() {
        ThreadPoolExecutor current = logExecutor;
        if (current != null) {
            current.shutdown();
        }
    }

    private boolean isExcludedTable(String tableName) {
        if (tableName == null || tableName.isEmpty()) {
            return false;
        }
        return EXCLUDED_TABLES.stream()
                .anyMatch(excluded -> excluded.equalsIgnoreCase(tableName));
    }

    private String extractTableName(MappedStatement ms) {
        String id = ms.getId();
        String[] parts = id.split("\\.");
        if (parts.length > 1) {
            // Mapper 클래스 이름에서 테이블명을 추측 (예: SysUserMapper -> SysUser)
            String mapperName = parts[parts.length - 2];
            return mapperName.replace("Mapper", "");
        }
        return "UNKNOWN";
    }

    private Field findField(Class<?> clazz, String fieldName) {
        Class<?> currentClass = clazz;
        while (currentClass != null) {
            try {
                return currentClass.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                currentClass = currentClass.getSuperclass();
            }
        }
        return null;
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
    }
}
