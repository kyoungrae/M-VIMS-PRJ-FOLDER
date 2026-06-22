package com.vims.common.siteconfiggroup;

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
@RequestMapping("/cms/common/sysSiteConfigGroup")
@RequiredArgsConstructor
public class SysSiteConfigGroupController extends AbstractCommonController<SysSiteConfigGroup> {

    private final SysSiteConfigGroupService sysSiteConfigGroupService;

    @PostMapping("/findPage")
    public Map<String, List<?>> findPage(@RequestBody SysSiteConfigGroup reqeust) throws Exception {
        return sysSiteConfigGroupService.findPage(reqeust);
    }

    @PostMapping("/findAll")
    protected List<SysSiteConfigGroup> findAll(@RequestBody SysSiteConfigGroup request) throws Exception {
        return sysSiteConfigGroupService.findImpl(request);
    }

    @PostMapping("/find")
    @Override
    protected List<SysSiteConfigGroup> findImpl(@RequestBody SysSiteConfigGroup request) throws Exception {
        return sysSiteConfigGroupService.findImpl(request);
    }

    @PostMapping("/remove")
    @Override
    protected int removeImpl(@RequestBody SysSiteConfigGroup request) {
        return sysSiteConfigGroupService.removeImpl(request);
    }

    @PostMapping("/update")
    @Override
    protected int updateImpl(@RequestBody SysSiteConfigGroup request) {
        return sysSiteConfigGroupService.updateImpl(request);
    }

    @PostMapping("/register")
    @Override
    protected int registerImpl(@RequestBody SysSiteConfigGroup request) {
        return sysSiteConfigGroupService.registerImpl(request);
    }

    @PostMapping("/excelUpload")
    @Override
    protected int excelUploadImpl(@RequestParam("file") MultipartFile file) throws Exception {
        return sysSiteConfigGroupService.excelUploadImpl(file);
    }
}
