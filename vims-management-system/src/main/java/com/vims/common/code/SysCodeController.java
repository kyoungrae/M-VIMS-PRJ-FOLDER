package com.vims.common.code;

import com.system.common.base.AbstractCommonController;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cms/common/sysCode")
@RequiredArgsConstructor
public class SysCodeController extends AbstractCommonController<SysCode> {
    private final SysCodeService sysCodeService;

    @RequestMapping("/findSysCode")
    protected List<SysCode> findSysCode(@RequestBody SysCode request) throws Exception {
        return sysCodeService.findSysCode(request);
    }

    @PostMapping("/findPage")
    public Map<String, List<?>> findPage(@RequestBody SysCode reqeust) throws Exception {
        return sysCodeService.findPage(reqeust);
    }

    @PostMapping("/findAll")
    protected List<SysCode> findAll(@RequestBody SysCode request) throws Exception {
        return sysCodeService.findImpl(request);
    }

    @Override
    @PostMapping("/find")
    protected List<SysCode> findImpl(@RequestBody SysCode request) throws Exception {
        return sysCodeService.findImpl(request);
    }

    @Override
    @PostMapping("/remove")
    protected int removeImpl(@RequestBody SysCode request) {
        return sysCodeService.removeImpl(request);
    }

    @Override
    @PostMapping("/update")
    protected int updateImpl(@RequestBody SysCode request) {
        return sysCodeService.updateImpl(request);
    }

    @Override
    @PostMapping("/register")
    protected int registerImpl(@RequestBody SysCode request) {
        return sysCodeService.registerImpl(request);
    }

    @PostMapping("/excelUpload")
    @Override
    protected int excelUploadImpl(@RequestParam("file") MultipartFile file) throws Exception {
        return sysCodeService.excelUploadImpl(file);
    }
}
