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
@RequestMapping("/cms/common/sysBbs")
@RequiredArgsConstructor
public class SysBbsController extends AbstractCommonController<SysBbs> {

    private final SysBbsService sysBbsService;

    @PostMapping("/findPage")
    public Map<String, List<?>> findPage(@RequestBody SysBbs reqeust) throws Exception {
        return sysBbsService.findPage(reqeust);
    }

    @PostMapping("/findAll")
    protected List<SysBbs> findAll(@RequestBody SysBbs request) throws Exception {
        return sysBbsService.findImpl(request);
    }

    @PostMapping("/find")
    @Override
    protected List<SysBbs> findImpl(@RequestBody SysBbs request) throws Exception {
        return sysBbsService.findImpl(request);
    }

    @PostMapping("/remove")
    @Override
    protected int removeImpl(@RequestBody SysBbs request) {
        return sysBbsService.removeImpl(request);
    }

    @PostMapping("/update")
    @Override
    protected int updateImpl(@RequestBody SysBbs request) {
        return sysBbsService.updateImpl(request);
    }

    @PostMapping("/register")
    @Override
    protected int registerImpl(@RequestBody SysBbs request) {
        return sysBbsService.registerImpl(request);
    }

    @PostMapping("/excelUpload")
    @Override
    protected int excelUploadImpl(@RequestParam("file") MultipartFile file) throws Exception {
        return sysBbsService.excelUploadImpl(file);
    }
}
