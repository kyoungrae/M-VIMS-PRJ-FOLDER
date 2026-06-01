package com.vims.fmsClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

/**
 * FMS(File Management System) 서비스와 통신하기 위한 Feign Client
 * 
 * - url: FMS 서비스의 주소 (application.yml에서 설정 가능)
 * - configuration: Feign 설정 클래스 지정
 */
@FeignClient(name = "fms-service", url = "${fms.service.url:http://localhost:8082}", configuration = FmsClientConfiguration.class)
public interface FmsExcelClient {

    /**
     * FMS 서비스의 엑셀 업로드 API 호출
     * 
     * @param file   업로드할 엑셀 파일
     * @param apiKey 내부 서비스 간 인증을 위한 API Key
     * @return 엑셀 파싱 결과 (ExcelData)
     */
    @PostMapping(value = "/fms/excel/excelUpload/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ExcelDataResponse uploadExcel(
            @RequestPart("file") MultipartFile file,
            @RequestHeader("X-Internal-API-Key") String apiKey);
}
