package com.vims.common.usergroup;

import com.system.common.base.AbstractCommonController;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cms/common/sysUserGroup")
@RequiredArgsConstructor
public class SysUserGroupController extends AbstractCommonController<SysUserGroup> {

    private final SysUserGroupService sysUserGroupService;

    @PostMapping("/findPage")
    public Map<String, List<?>> findPage(@RequestBody SysUserGroup reqeust) throws Exception {
        // System.out.println(reqeust);
        return sysUserGroupService.findPage(reqeust);
    }

    @PostMapping("/findAll")
    protected List<SysUserGroup> findAll(@RequestBody SysUserGroup request) throws Exception {
        return sysUserGroupService.findImpl(request);
    }

    @PostMapping("/find")
    @Override
    protected List<SysUserGroup> findImpl(@RequestBody SysUserGroup request) throws Exception {
        return sysUserGroupService.findImpl(request);
    }

    @PostMapping("/remove")
    @Override
    protected int removeImpl(@RequestBody SysUserGroup request) {
        return sysUserGroupService.removeImpl(request);
    }

    @PostMapping("/update")
    @Override
    protected int updateImpl(@RequestBody SysUserGroup request) {
        return sysUserGroupService.updateImpl(request);
    }

    @PostMapping("/register")
    @Override
    protected int registerImpl(@RequestBody SysUserGroup request) throws Exception {
        return sysUserGroupService.registerImpl(request);
    }

    @PostMapping("/findJoinSysUserGroupPage")
    public Map<String, List<?>> findJoinSysUserGroupPage(@RequestBody SysUserGroup reqeust) throws Exception {
        return sysUserGroupService.findJoinSysUserGroupPage(reqeust);
    }

    @PostMapping("/excelUpload")
    @Override
    protected int excelUploadImpl(MultipartFile arg0) throws Exception {
        return sysUserGroupService.excelUploadImpl(arg0);
    }
}
