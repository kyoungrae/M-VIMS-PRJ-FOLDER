package com.fmsClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(name = "fms-service", url = "${fms.service.url:http://localhost:8082}", configuration = FmsClientConfiguration.class)
public interface FmsExcelClient {

    @PostMapping(value = "/fms/excel/excelUpload/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ExcelDataResponse uploadExcel(
            @RequestPart("file") MultipartFile file,
            @RequestHeader("X-Internal-API-Key") String apiKey);
}
