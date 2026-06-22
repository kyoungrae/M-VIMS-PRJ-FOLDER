package com.vims.common.siteconfig;

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
@RequestMapping("/cms/common/sysSiteConfig")
@RequiredArgsConstructor
public class SysSiteConfigController extends AbstractCommonController<SysSiteConfig> {

    private final SysSiteConfigService sysSiteConfigService;

    @PostMapping("/findPage")
    public Map<String, List<?>> findPage(@RequestBody SysSiteConfig reqeust) throws Exception {
        return sysSiteConfigService.findPage(reqeust);
    }

    @PostMapping("/findAll")
    protected List<SysSiteConfig> findAll(@RequestBody SysSiteConfig request) throws Exception {
        return sysSiteConfigService.findImpl(request);
    }

    @PostMapping("/find")
    @Override
    protected List<SysSiteConfig> findImpl(@RequestBody SysSiteConfig request) throws Exception {
        return sysSiteConfigService.findImpl(request);
    }

    @PostMapping("/remove")
    @Override
    protected int removeImpl(@RequestBody SysSiteConfig request) {
        return sysSiteConfigService.removeImpl(request);
    }

    @PostMapping("/update")
    @Override
    protected int updateImpl(@RequestBody SysSiteConfig request) {
        return sysSiteConfigService.updateImpl(request);
    }

    @PostMapping("/register")
    @Override
    protected int registerImpl(@RequestBody SysSiteConfig request) throws Exception {
        return sysSiteConfigService.registerImpl(request);
    }

    @PostMapping("/excelUpload")
    @Override
    protected int excelUploadImpl(@RequestParam("file") MultipartFile file) throws Exception {
        return sysSiteConfigService.excelUploadImpl(file);
    }
}
