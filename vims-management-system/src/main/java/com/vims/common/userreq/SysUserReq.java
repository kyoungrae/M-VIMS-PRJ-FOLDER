package com.vims.common.userreq;

import com.system.common.base.Common;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.Arrays;
import java.util.Date;

/**
 * 사용자 등록 신청 (SYS_USER_REQ)
 * 직원이 새 직원 계정 생성을 신청하고, 승인 권한자가 승인/반려/취소한다.
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class SysUserReq extends Common {

    @Transient
    private String keys = Arrays.toString(new String[] { "req_id" });

    @Id
    private Long req_id;            // 신청 ID (시퀀스)

    // 신청(생성 예정) 사용자 정보
    private String user_id;        // 신청 아이디
    private String eml;            // 이메일
    private String fnm;            // 이름
    private String telno;          // 전화번호
    private String role;           // 권한/역할
    private String inst_cd;        // 소속코드
    private String addr;           // 주소
    private String addr_dtl;       // 상세주소
    private String post_cd;        // 우편번호
    private String req_rsn;        // 신청 사유

    // 승인 워크플로우
    private String stat_cd;        // 신청상태 (REQ/APPR/REJ/CANCEL)
    private String prc_usr_id;     // 처리자 ID
    private Date prc_dt;           // 처리 일시
    private String prc_rsn;        // 처리 사유(반려/취소)
    private Long crt_user_seq;     // 승인 시 생성된 SYS_USER.ID

    // 감사 컬럼 (SQL 에서 sys_gen_dt/sys_mdfcn_dt 로 매핑)
    private String sys_crt_usr_id; // 신청자 ID
    private Date sys_crt_dt;       // 신청 일시
    private String sys_upd_usr_id; // 수정자 ID
    private Date sys_upd_dt;       // 수정 일시

    // 표시용 (조인)
    @Transient
    private String stat_nm;        // 상태명(공통코드)
    @Transient
    private String stat_raw;       // 원본 상태코드(그리드 조건부 버튼 노출용)
    @Transient
    private String inst_nm;        // 소속명

    // 검색용 (LIKE)
    @Transient
    private String _user_id;
    @Transient
    private String _eml;
    @Transient
    private String _fnm;
    @Transient
    private String _stat_cd;
    @Transient
    private String _sys_crt_usr_id;
    // 관리자 목록에서 작성중(DRAFT) 제외 조회용 플래그
    @Transient
    private String not_draft;
}
