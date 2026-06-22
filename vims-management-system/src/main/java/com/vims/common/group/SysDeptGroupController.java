package com.vims.common.group;

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
@RequestMapping("/cms/common/sysDeptGroup")
@RequiredArgsConstructor
public class SysDeptGroupController extends AbstractCommonController<SysDeptGroup> {

    private final SysDeptGroupService sysDeptGroupService;

    @PostMapping("/findPage")
    public Map<String, List<?>> findPage(@RequestBody SysDeptGroup reqeust) throws Exception {
        return sysDeptGroupService.findPage(reqeust);
    }

    @PostMapping("/findAll")
    protected List<SysDeptGroup> findAll(@RequestBody SysDeptGroup request) throws Exception {
        return sysDeptGroupService.findImpl(request);
    }

    @PostMapping("/findNotExistsSysAccsGroupMenu")
    protected List<SysDeptGroup> findNotExistsSysAccsGroupMenu(@RequestBody SysDeptGroup request) throws Exception {
        return sysDeptGroupService.findNotExistsSysAccsGroupMenu(request);
    }

    @PostMapping("/find")
    @Override
    protected List<SysDeptGroup> findImpl(@RequestBody SysDeptGroup request) throws Exception {
        return sysDeptGroupService.findImpl(request);
    }

    @PostMapping("/remove")
    @Override
    protected int removeImpl(@RequestBody SysDeptGroup request) throws Exception {
        return sysDeptGroupService.removeImpl(request);
    }

    @PostMapping("/update")
    @Override
    protected int updateImpl(@RequestBody SysDeptGroup request) {
        return sysDeptGroupService.updateImpl(request);
    }

    @PostMapping("/register")
    @Override
    protected int registerImpl(@RequestBody SysDeptGroup request) {
        return sysDeptGroupService.registerImpl(request);
    }

    @PostMapping("/excelUpload")
    @Override
    protected int excelUploadImpl(@RequestParam("file") MultipartFile file) throws Exception {
        return sysDeptGroupService.excelUploadImpl(file);
    }
}
