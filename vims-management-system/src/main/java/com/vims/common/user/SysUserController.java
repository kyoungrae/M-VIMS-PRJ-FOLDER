package com.vims.common.user;

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
@RequestMapping("/cms/common/sysUser")
@RequiredArgsConstructor
public class SysUserController extends AbstractCommonController<SysUser> {

    private final SysUserService sysUserService;

    @PostMapping("/findPage")
    public Map<String, List<?>> findPage(@RequestBody SysUser reqeust) throws Exception {
        return sysUserService.findPage(reqeust);
    }

    @PostMapping("/findAll")
    protected List<SysUser> findAll(@RequestBody SysUser request) throws Exception {
        return sysUserService.findImpl(request);
    }

    @PostMapping("/find")
    @Override
    protected List<SysUser> findImpl(@RequestBody SysUser request) throws Exception {
        return sysUserService.findImpl(request);
    }

    @PostMapping("/remove")
    @Override
    public int removeImpl(@RequestBody SysUser request) {
        return sysUserService.removeImpl(request);
    }

    @PostMapping("/update")
    @Override
    public int updateImpl(@RequestBody SysUser request) throws Exception {
        return sysUserService.updateImpl(request);
    }

    @PostMapping("/register")
    @Override
    public int registerImpl(@RequestBody SysUser request) throws Exception {
        return sysUserService.registerImpl(request);
    }

    @PostMapping("/changePassword")
    public int changePassword(@RequestBody SysUser request) throws Exception {
        return sysUserService.changePassword(request);
    }

    @PostMapping("/updatePassword")
    public int updatePassword(@RequestBody SysUser request) throws Exception {
        return sysUserService.updatePasswordImpl(request);
    }

    @PostMapping("/excelUpload")
    @Override
    protected int excelUploadImpl(@RequestParam("file") MultipartFile arg0) throws Exception {
        return sysUserService.excelUploadImpl(arg0);
    }
}
