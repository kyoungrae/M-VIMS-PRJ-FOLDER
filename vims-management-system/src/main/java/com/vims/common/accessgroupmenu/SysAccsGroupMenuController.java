package com.vims.common.accessgroupmenu;

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
@RequestMapping("/cms/common/sysAccsGroupMenu")
@RequiredArgsConstructor
public class SysAccsGroupMenuController extends AbstractCommonController<SysAccsGroupMenu> {

    private final SysAccsGroupMenuService sysAccsGroupMenuService;

    @PostMapping("/findPage")
    public Map<String, List<?>> findPage(@RequestBody SysAccsGroupMenu reqeust) throws Exception {
        return sysAccsGroupMenuService.findPage(reqeust);
    }

    @PostMapping("/findAll")
    protected List<SysAccsGroupMenu> findAll(@RequestBody SysAccsGroupMenu request) throws Exception {
        return sysAccsGroupMenuService.findImpl(request);
    }

    @PostMapping("/find")
    @Override
    protected List<SysAccsGroupMenu> findImpl(@RequestBody SysAccsGroupMenu request) throws Exception {
        return sysAccsGroupMenuService.findImpl(request);
    }

    @PostMapping("/remove")
    @Override
    protected int removeImpl(@RequestBody SysAccsGroupMenu request) {
        return sysAccsGroupMenuService.removeImpl(request);
    }

    @PostMapping("/update")
    @Override
    protected int updateImpl(@RequestBody SysAccsGroupMenu request) {
        return sysAccsGroupMenuService.updateImpl(request);
    }

    @PostMapping("/register")
    @Override
    protected int registerImpl(@RequestBody SysAccsGroupMenu request) {
        return sysAccsGroupMenuService.registerImpl(request);
    }

    @PostMapping("/excelUpload")
    @Override
    protected int excelUploadImpl(@RequestParam("file") MultipartFile file) throws Exception {
        return sysAccsGroupMenuService.excelUploadImpl(file);
    }

}
