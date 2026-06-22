package com.system.common.db;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "db.partition.maintenance.enabled", havingValue = "true", matchIfMissing = false)
public class DbPartitionService {

    private final DbPartitionMapper dbPartitionMapper;

    /**
     * 서버 시작 시 즉시 파티션 점검 및 생성
     */
    @PostConstruct
    public void init() {
        log.info("DbPartitionService initialized. Running startup partition check...");
        maintainPartitions();
    }

    /**
     * 매일 새벽 3시에 파티션 유지보수 실행
     * 앞으로 2개월치 파티션이 미리 생성되어 있도록 관리
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void maintainPartitions() {
        log.info("Starting database partition maintenance...");

        // after 스키마에서는 로그 테이블 파티션을 관리하지 않는다.
        String[] tables = {};

        for (String table : tables) {
            try {
                ensureFuturePartitions(table);
            } catch (Exception e) {
                log.error("Failed to maintain partitions for table: {}", table, e);
            }
        }
    }

    private void ensureFuturePartitions(String tableName) {
        List<String> existingPartitions = dbPartitionMapper.selectPartitions(tableName);

        // 현재부터 향후 2개월까지 체크
        for (int i = 0; i <= 2; i++) {
            LocalDate targetMonth = LocalDate.now().plusMonths(i);
            String partitionName = "p" + targetMonth.format(DateTimeFormatter.ofPattern("yyyyMM"));

            // 다음달 1일 기준으로 LESS THAN 처리 (즉, 이번달 데이터는 이 파티션에 들어감)
            LocalDate nextMonthFirstDay = targetMonth.plusMonths(1).withDayOfMonth(1);
            String lessThanValue = nextMonthFirstDay.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            if (!existingPartitions.contains(partitionName)) {
                log.info("Creating partition {} for table {}", partitionName, tableName);
                try {
                    dbPartitionMapper.addPartition(tableName, partitionName, lessThanValue);
                } catch (Exception e) {
                    // 이미 존재하는 경우 등 예외 처리 (수동 생성시 충돌 방지)
                    log.warn(
                            "Could not create partition {}. It might already exist or the table is not partitioned yet.",
                            partitionName);
                }
            }
        }
    }
}
