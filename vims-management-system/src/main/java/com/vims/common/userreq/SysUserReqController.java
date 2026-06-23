package com.vims.common.userreq;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 사용자 등록 신청 컨트롤러
 * 신청 등록/조회 + 승인/반려/취소
 */
@RestController
@RequestMapping("/cms/common/sysUserReq")
@RequiredArgsConstructor
public class SysUserReqController {

    private final SysUserReqService sysUserReqService;

    @PostMapping("/findPage")
    public Map<String, List<?>> findPage(@RequestBody SysUserReq request) throws Exception {
        return sysUserReqService.findPage(request);
    }

    @PostMapping("/find")
    public List<SysUserReq> find(@RequestBody SysUserReq request) throws Exception {
        return sysUserReqService.find(request);
    }

    @PostMapping("/register")
    public int register(@RequestBody SysUserReq request) throws Exception {
        return sysUserReqService.register(request);
    }

    @PostMapping("/update")
    public int update(@RequestBody SysUserReq request) throws Exception {
        return sysUserReqService.update(request);
    }

    @PostMapping("/remove")
    public int remove(@RequestBody SysUserReq request) throws Exception {
        return sysUserReqService.remove(request);
    }

    /** 승인: 신청정보로 SYS_USER 생성 후 APPR 처리 */
    @PostMapping("/approve")
    public int approve(@RequestBody SysUserReq request) throws Exception {
        return sysUserReqService.approve(request.getReq_id());
    }

    /** 반려 */
    @PostMapping("/reject")
    public int reject(@RequestBody SysUserReq request) throws Exception {
        return sysUserReqService.reject(request.getReq_id(), request.getPrc_rsn());
    }

    /** 취소 */
    @PostMapping("/cancel")
    public int cancel(@RequestBody SysUserReq request) throws Exception {
        return sysUserReqService.cancel(request.getReq_id(), request.getPrc_rsn());
    }
}
