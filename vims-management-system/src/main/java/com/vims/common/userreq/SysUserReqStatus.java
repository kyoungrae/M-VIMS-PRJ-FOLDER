package com.vims.common.userreq;

/**
 * 사용자 등록 신청 상태 (공통코드 SYS_USER_REG_STATUS)
 */
public final class SysUserReqStatus {

    private SysUserReqStatus() {
    }

    /** 0 - 대기 (임시저장) */
    public static final String WAIT = "0";

    /** 1 - 신청 완료 (승인 대기) */
    public static final String SUBMITTED = "1";

    /** 2 - 승인 */
    public static final String APPROVED = "2";

    /** 3 - 반려 */
    public static final String REJECTED = "3";
}
