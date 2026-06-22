package com.vims.common.bbs;

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
@RequestMapping("/cms/common/sysBbsMst")
@RequiredArgsConstructor
public class SysBbsMstController extends AbstractCommonController<SysBbsMst> {

    private final SysBbsMstService sysBbsMstService;

    @PostMapping("/findPage")
    public Map<String, List<?>> findPage(@RequestBody SysBbsMst reqeust) throws Exception {
        return sysBbsMstService.findPage(reqeust);
    }

    @PostMapping("/findAll")
    protected List<SysBbsMst> findAll(@RequestBody SysBbsMst request) throws Exception {
        return sysBbsMstService.findImpl(request);
    }

    @PostMapping("/find")
    @Override
    protected List<SysBbsMst> findImpl(@RequestBody SysBbsMst request) throws Exception {
        return sysBbsMstService.findImpl(request);
    }

    @PostMapping("/remove")
    @Override
    protected int removeImpl(@RequestBody SysBbsMst request) {
        return sysBbsMstService.removeImpl(request);
    }

    @PostMapping("/update")
    @Override
    protected int updateImpl(@RequestBody SysBbsMst request) {
        return sysBbsMstService.updateImpl(request);
    }

    @PostMapping("/register")
    @Override
    protected int registerImpl(@RequestBody SysBbsMst request) {
        return sysBbsMstService.registerImpl(request);
    }

    @PostMapping("/excelUpload")
    @Override
    protected int excelUploadImpl(@RequestParam("file") MultipartFile file) throws Exception {
        return sysBbsMstService.excelUploadImpl(file);
    }
}
