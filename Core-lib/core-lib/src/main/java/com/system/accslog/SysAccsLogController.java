package com.system.accslog;

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
@RequestMapping("/cms/common/sysAccsLog")
@RequiredArgsConstructor
public class SysAccsLogController extends AbstractCommonController<SysAccsLog> {

    private final SysAccsLogService sysAccsLogService;

    @PostMapping("/findPage")
    public Map<String, List<?>> findPage(@RequestBody SysAccsLog reqeust) throws Exception {
        return sysAccsLogService.findPage(reqeust);
    }

    @PostMapping("/findAll")
    protected List<SysAccsLog> findAll(@RequestBody SysAccsLog request) throws Exception {
        return sysAccsLogService.findImpl(request);
    }

    @PostMapping("/find")
    @Override
    protected List<SysAccsLog> findImpl(@RequestBody SysAccsLog request) throws Exception {
        return sysAccsLogService.findImpl(request);
    }

    @PostMapping("/remove")
    @Override
    protected int removeImpl(@RequestBody SysAccsLog request) {
        return sysAccsLogService.removeImpl(request);
    }

    @PostMapping("/update")
    @Override
    protected int updateImpl(@RequestBody SysAccsLog request) {
        return sysAccsLogService.updateImpl(request);
    }

    @PostMapping("/register")
    @Override
    protected int registerImpl(@RequestBody SysAccsLog request) {
        return sysAccsLogService.registerImpl(request);
    }

    @PostMapping("/excelUpload")
    @Override
    protected int excelUploadImpl(@RequestParam("file") MultipartFile file) throws Exception {
        return sysAccsLogService.excelUploadImpl(file);
    }
}
