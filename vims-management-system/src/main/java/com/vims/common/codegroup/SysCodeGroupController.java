package com.vims.common.codegroup;

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
@RequestMapping("/cms/common/sysCodeGroup")
@RequiredArgsConstructor
public class SysCodeGroupController extends AbstractCommonController<SysCodeGroup> {
    private final SysCodeGroupService sysCodeGroupService;

    @RequestMapping("/findByGroupId")
    protected List<SysCodeGroup> findByGroupId(@RequestBody SysCodeGroup request) throws Exception {
        return sysCodeGroupService.findByGroupId(request);
    }

    @PostMapping("/findPage")
    public Map<String, List<?>> findPage(@RequestBody SysCodeGroup reqeust) throws Exception {
        return sysCodeGroupService.findPage(reqeust);
    }

    @PostMapping("/findAll")
    protected List<SysCodeGroup> findAll(@RequestBody SysCodeGroup request) throws Exception {
        return sysCodeGroupService.findImpl(request);
    }

    @Override
    @PostMapping("/find")
    protected List<SysCodeGroup> findImpl(@RequestBody SysCodeGroup request) throws Exception {
        return sysCodeGroupService.findImpl(request);
    }

    @Override
    @PostMapping("/remove")
    protected int removeImpl(@RequestBody SysCodeGroup request) {
        return sysCodeGroupService.removeImpl(request);
    }

    @Override
    @PostMapping("/update")
    protected int updateImpl(@RequestBody SysCodeGroup request) {
        return sysCodeGroupService.updateImpl(request);
    }

    @Override
    @PostMapping("/register")
    protected int registerImpl(@RequestBody SysCodeGroup request) {
        return sysCodeGroupService.registerImpl(request);
    }

    @PostMapping("/excelUpload")
    @Override
    protected int excelUploadImpl(@RequestParam("file") MultipartFile file) throws Exception {
        return sysCodeGroupService.excelUploadImpl(file);
    }
}
