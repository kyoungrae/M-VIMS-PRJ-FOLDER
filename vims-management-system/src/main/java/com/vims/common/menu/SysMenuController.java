package com.vims.common.menu;

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
@RequestMapping("/cms/common/sysMenu")
@RequiredArgsConstructor
public class SysMenuController extends AbstractCommonController<SysMenu> {

    private final SysMenuService sysMenuService;

    @PostMapping("/findPage")
    public Map<String, List<?>> findPage(@RequestBody SysMenu reqeust) throws Exception {
        return sysMenuService.findPage(reqeust);
    }

    @PostMapping("/findHierarchy")
    public List<SysMenu> findHierarchy(@RequestBody SysMenu request) throws Exception {
        return sysMenuService.findHierarchy(request);
    }

    @PostMapping("/findAccessRightGroupForMenu")
    public List<SysMenu> findAccessRightGroupForMenu(@RequestBody SysMenu request) throws Exception {
        return sysMenuService.findAccessRightGroupForMenu(request);
    }

    @PostMapping("/findAll")
    protected List<SysMenu> findAll(@RequestBody SysMenu request) throws Exception {
        return sysMenuService.findImpl(request);
    }

    @PostMapping("/find")
    @Override
    protected List<SysMenu> findImpl(@RequestBody SysMenu request) throws Exception {
        return sysMenuService.findImpl(request);
    }

    @PostMapping("/removeMenuCode")
    public int removeMenuCode(@RequestBody SysMenu request) throws Exception {
        return sysMenuService.removeMenuCode(request);
    }

    @PostMapping("/remove")
    @Override
    protected int removeImpl(@RequestBody SysMenu request) {
        return sysMenuService.removeImpl(request);
    }

    @PostMapping("/update")
    @Override
    protected int updateImpl(@RequestBody SysMenu request) {
        return sysMenuService.updateImpl(request);
    }

    @PostMapping("/register")
    @Override
    protected int registerImpl(@RequestBody SysMenu request) throws Exception {
        return sysMenuService.registerImpl(request);
    }

    @PostMapping("/excelUpload")
    @Override
    protected int excelUploadImpl(@RequestParam("file") MultipartFile file) throws Exception {
        return sysMenuService.excelUploadImpl(file);
    }
}
