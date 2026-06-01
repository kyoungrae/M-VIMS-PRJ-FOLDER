package com.vims.fmsClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "fms-client", url = "${fms.service.url}", configuration = FmsClientConfiguration.class)
public interface FmsClient {

    @PostMapping("/fms/common/file/sysFileDetail/removeByFileUuid")
    int removeByFileUuid(@RequestBody Map<String, Object> request);
}
