package com.system.common.eventlog;

import com.system.common.base.AbstractCommonController;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cms/common/sysEventLog")
@RequiredArgsConstructor
public class SysEventLogController extends AbstractCommonController<SysEventLog> {

    private final SysEventLogService sysEventLogService;

    @PostMapping("/findPage")
    public Map<String, List<?>> findPage(@RequestBody SysEventLog reqeust) throws Exception {
        return sysEventLogService.findPage(reqeust);
    }

    @PostMapping("/findAll")
    protected List<SysEventLog> findAll(@RequestBody SysEventLog request) throws Exception {
        return sysEventLogService.findImpl(request);
    }

    @PostMapping("/find")
    @Override
    protected List<SysEventLog> findImpl(@RequestBody SysEventLog request) throws Exception {
        return sysEventLogService.findImpl(request);
    }

    @PostMapping("/remove")
    @Override
    protected int removeImpl(@RequestBody SysEventLog request) {
        return sysEventLogService.removeImpl(request);
    }

    @PostMapping("/update")
    @Override
    protected int updateImpl(@RequestBody SysEventLog request) {
        return sysEventLogService.updateImpl(request);
    }

    @PostMapping("/register")
    @Override
    protected int registerImpl(@RequestBody SysEventLog request) {
        return sysEventLogService.registerImpl(request);
    }

    @PostMapping("/excelUpload")
    @Override
    protected int excelUploadImpl(@RequestParam("file") MultipartFile file) throws Exception {
        return sysEventLogService.excelUploadImpl(file);
    }
}
