package com.vims.common.userreq;

import com.system.auth.service.SequenceService;
import com.system.common.base.AbstractCommonService;
import com.system.common.exception.CustomException;
import com.system.common.util.userinfo.UserInfo;
import com.vims.common.user.SysUser;
import com.vims.common.user.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;

import static com.vims.common.userreq.SysUserReqStatus.*;

/**
 * 사용자 등록 신청 서비스
 * - 신청(register) : stat_cd=0 (대기)
 * - 최종신청(submit): stat_cd=1 (신청 완료)
 * - 승인(approve)  : stat_cd=2
 * - 반려(reject)   : stat_cd=3
 */
@Service
@RequiredArgsConstructor
public class SysUserReqService extends AbstractCommonService<SysUserReq> {

    private final SysUserReqMapper sysUserReqMapper;
    private final SequenceService sequenceService;
    private final PasswordEncoder passwordEncoder;
    private final SysUserMapper sysUserMapper;

    // 승인 시 발급할 임시 비밀번호 (신규 직원은 '비밀번호 찾기'로 직접 재설정)
    @Value("${app.user-req.temp-password:1234}")
    private String tempPassword;

    /* ===================== 목록/조회 ===================== */
    @Override
    protected List<SysUserReq> selectPage(SysUserReq request) throws Exception {
        return sysUserReqMapper.SELECT_PAGE(request);
    }

    @Override
    protected int selectPagingTotalNumber(SysUserReq request) throws Exception {
        return sysUserReqMapper.SELECT_PAGING_TOTAL_NUMBER(request);
    }

    @Override
    protected List<SysUserReq> findImpl(SysUserReq request) throws Exception {
        return sysUserReqMapper.SELECT(request);
    }

    /* ===================== 신청서 작성(임시저장) ===================== */
    @Transactional(rollbackFor = Exception.class)
    @Override
    protected int registerImpl(SysUserReq request) throws Exception {
        if (isEmpty(request.getUser_id()) || isEmpty(request.getEml())) {
            throw new CustomException("아이디와 이메일은 필수입니다.");
        }
        request.setReq_id(sequenceService.selectTokenSequence());
        request.setStat_cd(WAIT);
        request.setSys_crt_usr_id(currentUser());
        return sysUserReqMapper.INSERT(request);
    }

    /* ===================== 신청서 수정 (작성중만 가능) ===================== */
    @Transactional(rollbackFor = Exception.class)
    @Override
    protected int updateImpl(SysUserReq request) throws Exception {
        loadDraft(request.getReq_id());
        request.setStat_cd(null); // 수정으로 상태가 바뀌지 않도록
        request.setSys_upd_usr_id(currentUser());
        return sysUserReqMapper.UPDATE(request);
    }

    /* ===================== 최종 신청 (0 -> 1) ===================== */
    @Transactional(rollbackFor = Exception.class)
    public int submit(Long reqId) throws Exception {
        SysUserReq req = loadDraft(reqId);
        if (existsSysUser(req.getEml(), req.getUser_id())) {
            throw new CustomException("이미 가입된 아이디 또는 이메일입니다.");
        }
        if (existsPendingRequest(req.getUser_id(), req.getEml())) {
            throw new CustomException("이미 신청된 동일 아이디 또는 이메일이 있습니다.");
        }
        SysUserReq upd = SysUserReq.builder()
                .req_id(reqId)
                .stat_cd(SUBMITTED)
                .sys_upd_usr_id(currentUser())
                .build();
        return sysUserReqMapper.UPDATE(upd);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    protected int removeImpl(SysUserReq request) throws Exception {
        return sysUserReqMapper.DELETE(request);
    }

    /* ===================== 승인 ===================== */
    @Transactional(rollbackFor = Exception.class)
    public int approve(Long reqId) throws Exception {
        SysUserReq req = loadPending(reqId);

        // 이미 가입된 이메일/아이디 중복 확인
        if (existsSysUser(req.getEml(), req.getUser_id())) {
            throw new CustomException("이미 가입된 아이디 또는 이메일입니다.");
        }

        // 1) SYS_USER 계정 생성 (임시 비밀번호)
        Long userSeq = sequenceService.selectTokenSequence();
        SysUser newUser = SysUser.builder()
                .id(userSeq)
                .eml(req.getEml())
                .user_id(req.getUser_id())
                .fnm(req.getFnm())
                .telno(req.getTelno())
                .role(isEmpty(req.getRole()) ? "USER" : req.getRole())
                .inst_cd(req.getInst_cd())
                .addr(req.getAddr())
                .addr_dtl(req.getAddr_dtl())
                .post_cd(req.getPost_cd())
                .pswd(passwordEncoder.encode(tempPassword))
                .sys_crt_usr_id(currentUser())
                .sys_crt_dt(new Date())
                .build();
        sysUserMapper.INSERT(newUser);

        // 2) 신청 APPR 처리
        SysUserReq upd = SysUserReq.builder()
                .req_id(reqId)
                .stat_cd(APPROVED)
                .prc_usr_id(currentUser())
                .prc_dt(new Date())
                .crt_user_seq(userSeq)
                .sys_upd_usr_id(currentUser())
                .build();
        return sysUserReqMapper.UPDATE(upd);
    }

    /* ===================== 반려 ===================== */
    @Transactional(rollbackFor = Exception.class)
    public int reject(Long reqId, String prcRsn) throws Exception {
        loadPending(reqId);
        SysUserReq upd = SysUserReq.builder()
                .req_id(reqId)
                .stat_cd(REJECTED)
                .prc_usr_id(currentUser())
                .prc_dt(new Date())
                .prc_rsn(prcRsn)
                .sys_upd_usr_id(currentUser())
                .build();
        return sysUserReqMapper.UPDATE(upd);
    }

    /* ===================== 취소 ===================== */
    @Transactional(rollbackFor = Exception.class)
    public int cancel(Long reqId, String prcRsn) throws Exception {
        loadPending(reqId);
        SysUserReq upd = SysUserReq.builder()
                .req_id(reqId)
                .stat_cd(WAIT)
                .prc_usr_id(currentUser())
                .prc_dt(new Date())
                .prc_rsn(prcRsn)
                .sys_upd_usr_id(currentUser())
                .build();
        return sysUserReqMapper.UPDATE(upd);
    }

    /* ===================== 내부 유틸 ===================== */
    private SysUserReq loadDraft(Long reqId) {
        if (reqId == null) {
            throw new CustomException("신청 정보를 찾을 수 없습니다.");
        }
        List<SysUserReq> list = sysUserReqMapper.SELECT(SysUserReq.builder().req_id(reqId).build());
        if (list == null || list.isEmpty()) {
            throw new CustomException("신청 정보를 찾을 수 없습니다.");
        }
        SysUserReq req = list.get(0);
        if (!WAIT.equals(req.getStat_cd())) {
            throw new CustomException("이미 신청되어 수정할 수 없습니다.");
        }
        return req;
    }

    private SysUserReq loadPending(Long reqId) {
        if (reqId == null) {
            throw new CustomException("신청 정보를 찾을 수 없습니다.");
        }
        List<SysUserReq> list = sysUserReqMapper.SELECT(SysUserReq.builder().req_id(reqId).build());
        if (list == null || list.isEmpty()) {
            throw new CustomException("신청 정보를 찾을 수 없습니다.");
        }
        SysUserReq req = list.get(0);
        if (!SUBMITTED.equals(req.getStat_cd())) {
            throw new CustomException("이미 처리된 신청입니다.");
        }
        return req;
    }

    private boolean existsPendingRequest(String userId, String eml) {
        List<SysUserReq> byId = sysUserReqMapper.SELECT(
                SysUserReq.builder().user_id(userId).stat_cd(SUBMITTED).build());
        if (byId != null && !byId.isEmpty()) return true;
        List<SysUserReq> byEml = sysUserReqMapper.SELECT(
                SysUserReq.builder().eml(eml).stat_cd(SUBMITTED).build());
        return byEml != null && !byEml.isEmpty();
    }

    private boolean existsSysUser(String eml, String userId) {
        List<SysUser> byEml = sysUserMapper.SELECT(SysUser.builder().eml(eml).build());
        if (byEml != null && !byEml.isEmpty()) return true;
        List<SysUser> byId = sysUserMapper.SELECT(SysUser.builder().user_id(userId).build());
        return byId != null && !byId.isEmpty();
    }

    private String currentUser() {
        try {
            return UserInfo.getUserEmail();
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    @Override
    protected int excelUploadImpl(MultipartFile file) throws Exception {
        throw new CustomException("지원하지 않는 기능입니다.");
    }
}
