package com.system.common.interceptor;

import com.system.common.annotation.RemoveHypen;
import com.system.common.annotation.StringToIntegerRemoveComma;
import com.system.common.enumlist.InterCeptorRemoveDataValueTransformFieldNameList;
import com.system.common.util.DateUtil;
import com.system.common.util.userinfo.UserInfo;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;

import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;

/*
 * writer : 이경태
 * text : 파라미터 가공 처리 interceptor
 * */
@Component
@Intercepts({
        @Signature(type = org.apache.ibatis.executor.Executor.class, method = "update", args = {
                org.apache.ibatis.mapping.MappedStatement.class, Object.class }),
        @Signature(type = org.apache.ibatis.executor.Executor.class, method = "query", args = {
                org.apache.ibatis.mapping.MappedStatement.class, Object.class,
                org.apache.ibatis.session.RowBounds.class, org.apache.ibatis.session.ResultHandler.class })
})
public class QueryTypeInterceptor implements Interceptor {
    private static final Map<String, InterCeptorRemoveDataValueTransformFieldNameList> REMOVE_TRANSFORM_FIELDS =
            Arrays.stream(InterCeptorRemoveDataValueTransformFieldNameList.values())
                    .collect(Collectors.toUnmodifiableMap(Enum::name, Function.identity()));

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object[] args = invocation.getArgs();
        org.apache.ibatis.mapping.MappedStatement ms = (org.apache.ibatis.mapping.MappedStatement) args[0];
        Object parameterObject = args[1];

        if (parameterObject == null) {
            return invocation.proceed();
        }

        String userEmail = UserInfo.getUserEmail();
        org.apache.ibatis.mapping.SqlCommandType commandType = ms.getSqlCommandType();

        // 1. 공통 데이터 변환 (Hyphen 제거 등) - 모든 명령에 대해 수행
        applyTransformations(parameterObject);

        // 2. 자동 감사 필드 주입 (Insert/Update/Merge)
        if (commandType == org.apache.ibatis.mapping.SqlCommandType.INSERT) {
            Date systemDateTime = DateUtil.getServerTimeTypeDate();
            modifyFieldDate(parameterObject, "sys_crt_dt", systemDateTime);
            modifyFieldDate(parameterObject, "sys_login_dt", systemDateTime);
            modifyFieldDate(parameterObject, "sys_upd_dt", systemDateTime);

            if (userEmail != null) {
                modifyField(parameterObject, "sys_crt_usr_id", userEmail);
                modifyField(parameterObject, "sys_upd_usr_id", userEmail);
            }
        } else if (commandType == org.apache.ibatis.mapping.SqlCommandType.UPDATE) {
            Date systemDateTime = DateUtil.getServerTimeTypeDate();

            // 최상위 객체 수정
            updateAuditFields(parameterObject, userEmail, systemDateTime);

            // ParamMap인 경우 내부 객체들도 탐색
            if (parameterObject instanceof MapperMethod.ParamMap<?> paramMap) {
                for (Object value : paramMap.values()) {
                    if (value == null || value == parameterObject)
                        continue;
                    if (value instanceof List<?>) {
                        for (Object item : (List<?>) value) {
                            updateAuditFields(item, userEmail, systemDateTime);
                        }
                    } else {
                        updateAuditFields(value, userEmail, systemDateTime);
                    }
                }
            }
        }

        return invocation.proceed();
    }

    private void updateAuditFields(Object obj, String userEmail, Date systemDateTime) {
        modifyField(obj, "sys_upd_usr_id", userEmail);
        modifyFieldDate(obj, "sys_upd_dt", systemDateTime);
    }

    private void applyTransformations(Object obj) throws Exception {
        Class<?> clazz = obj.getClass();

        // 상위 클래스 필드도 포함하여 반복문을 통해 필드를 처리합니다.
        while (clazz != null) {
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                InterCeptorRemoveDataValueTransformFieldNameList enumConstant = findRemoveEnumConstant(field.getName());
                if (enumConstant != null) {
                    // enum 상수에서 어노테이션 검사
                    checkAndApplyAnnotation(obj, field, enumConstant);
                }
            }
            // 상위 클래스로 이동합니다.
            clazz = clazz.getSuperclass();
        }
    }

    // Remove Annotation
    private static InterCeptorRemoveDataValueTransformFieldNameList findRemoveEnumConstant(String fieldName) {
        return REMOVE_TRANSFORM_FIELDS.get(fieldName);
    }

    // 데이터 가공 [삭제]
    private static void checkAndApplyAnnotation(Object obj, Field field,
            InterCeptorRemoveDataValueTransformFieldNameList enumConstant) throws Exception {
        Class<?> enumClass = enumConstant.getClass();

        for (Annotation annotation : enumClass.getField(enumConstant.name()).getAnnotations()) {
            if (annotation instanceof RemoveHypen) {
                field.setAccessible(true);
                Object value = field.get(obj);
                if (value != null) {
                    String newValue = value.toString().replaceAll("-", "");
                    field.set(obj, newValue);
                }
            } else if (annotation instanceof StringToIntegerRemoveComma) {
                field.setAccessible(true);
                Object value = field.get(obj);
                if (value != null) {
                    String newValue = value.toString().replaceAll(",", "");
                    int intValue = Integer.parseInt(newValue);
                    field.set(obj, intValue);
                }
            }
            // Add other annotation checks as needed
        }
    }

    private void modifyField(Object obj, String fieldName, String value) {
        if (obj == null)
            return;

        // 1. Map 형태인 경우 처리
        if (obj instanceof java.util.Map) {
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> map = (java.util.Map<String, Object>) obj;
            map.put(fieldName, value);
            return;
        }

        // 2. 일반 객체(VO)인 경우 리플렉션 처리
        try {
            Field field = findField(obj.getClass(), fieldName);
            if (field != null) {
                field.setAccessible(true);
                field.set(obj, value);
            }
        } catch (NoSuchFieldException e) {
            // 필드가 없으면 무시
        } catch (IllegalAccessException e) {
            // 접근 불가면 무시
        }
    }

    private void modifyFieldDate(Object obj, String fieldName, Date value) {
        if (obj == null)
            return;

        // 1. Map 형태인 경우 처리
        if (obj instanceof java.util.Map) {
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> map = (java.util.Map<String, Object>) obj;
            map.put(fieldName, value);
            return;
        }

        // 2. 일반 객체(VO)인 경우 리플렉션 처리
        try {
            Field field = findField(obj.getClass(), fieldName);
            if (field != null) {
                field.setAccessible(true);
                field.set(obj, value);
            }
        } catch (NoSuchFieldException e) {
            // 필드가 없으면 무시
        } catch (IllegalAccessException e) {
            // 접근 불가면 무시
        }
    }

    // 특정 클래스에서 필드 찾기
    private Field findField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        Class<?> currentClass = clazz;
        while (currentClass != null) {
            try {
                return currentClass.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                currentClass = currentClass.getSuperclass();
            }
            clazz = clazz.getSuperclass();
        }
        throw new NoSuchFieldException("Field '" + fieldName + "' not found in class hierarchy");
    }

    @Override
    public Object plugin(Object target) {
        return Interceptor.super.plugin(target);
    }

    @Override
    public void setProperties(Properties properties) {
        Interceptor.super.setProperties(properties);
    }
}
