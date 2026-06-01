package com.fmsClient;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExcelDataResponse {

    private List<String> headers;
    private List<Map<String, Object>> data;
    private List<Map<String, Object>> dataRows;
    private int totalRows;
    private String fileName;
}
