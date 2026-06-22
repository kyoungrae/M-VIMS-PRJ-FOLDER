package com.system.common.db;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DbPartitionMapper {
    /**
     * 특정 테이블의 파티션 목록 조회
     */
    List<String> selectPartitions(@Param("tableName") String tableName);

    /**
     * 파티션 추가 (Range Partition)
     */
    void addPartition(@Param("tableName") String tableName,
            @Param("partitionName") String partitionName,
            @Param("lessThanValue") String lessThanValue);
}
