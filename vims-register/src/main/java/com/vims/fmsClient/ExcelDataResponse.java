package com.vims.fmsClient;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * FMS 엑셀 업로드 응답 DTO
 * FMS의 ExcelData 구조와 동일하게 매핑
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExcelDataResponse {

    /**
     * 엑셀 파일의 헤더 정보
     * 예: ["이름", "이메일", "전화번호"]
     */
    private List<String> headers;

    /**
     * 엑셀 파일의 데이터
     * 각 행은 Map<헤더명, 값> 형태
     * 예: [{"이름": "홍길동", "이메일": "hong@example.com", "전화번호": "010-1234-5678"}]
     */
    private List<Map<String, Object>> data;
    private List<Map<String, Object>> dataRows;
    /**
     * 전체 데이터 행 수
     */
    private int totalRows;

    /**
     * 파일 이름
     */
    private String fileName;
}
