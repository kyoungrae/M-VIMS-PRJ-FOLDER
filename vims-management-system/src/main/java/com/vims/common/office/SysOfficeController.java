package com.vims.common.office;

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
@RequestMapping("/cms/common/sysOffice")
@RequiredArgsConstructor
public class SysOfficeController extends AbstractCommonController<SysOffice> {

    private final SysOfficeService sysOfficeService;

    @PostMapping("/findPage")
    public Map<String, List<?>> findPage(@RequestBody SysOffice reqeust) throws Exception {
        return sysOfficeService.findPage(reqeust);
    }

    @PostMapping("/findAll")
    protected List<SysOffice> findAll(@RequestBody SysOffice request) throws Exception {
        return sysOfficeService.findImpl(request);
    }

    @PostMapping("/find")
    @Override
    protected List<SysOffice> findImpl(@RequestBody SysOffice request) throws Exception {
        return sysOfficeService.findImpl(request);
    }

    @PostMapping("/remove")
    @Override
    protected int removeImpl(@RequestBody SysOffice request) {
        return sysOfficeService.removeImpl(request);
    }

    @PostMapping("/update")
    @Override
    protected int updateImpl(@RequestBody SysOffice request) {
        return sysOfficeService.updateImpl(request);
    }

    @PostMapping("/register")
    @Override
    protected int registerImpl(@RequestBody SysOffice request) {
        return sysOfficeService.registerImpl(request);
    }

    @PostMapping("/excelUpload")
    @Override
    protected int excelUploadImpl(@RequestParam("file") MultipartFile file) throws Exception {
        return sysOfficeService.excelUploadImpl(file);
    }
}
