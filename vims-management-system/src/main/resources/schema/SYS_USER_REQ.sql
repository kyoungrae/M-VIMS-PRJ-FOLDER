-- =====================================================================
-- SYS_USER_REQ : 사용자 등록 신청 (User Registration Application)
-- ---------------------------------------------------------------------
-- 흐름
--   1) 직원이 '사용자 등록 신청' 메뉴에서 새 직원 계정 생성을 신청  → stat_cd = 'REQ'
--   2) 승인 권한자가 승인/반려/취소 처리
--        - 승인(APPR) : 본 신청정보로 SYS_USER 계정을 생성하고 crt_user_seq 에 연결
--        - 반려(REJ)  : prc_rsn(사유) 기록
--        - 취소(CANCEL): 신청자 본인 또는 관리자가 REQ 상태에서 철회
-- 비밀번호는 본 신청에 저장하지 않으며, 승인 시 임시 비밀번호로 계정 생성 후
-- 신규 직원이 '비밀번호 찾기' 흐름으로 직접 설정한다.
-- 감사 컬럼/PK는 SYS_USER 규칙(sys_gen_dt 등 + SYS_TOKEN_SEQ 시퀀스)을 따른다.
-- =====================================================================
CREATE TABLE IF NOT EXISTS SYS_USER_REQ (
    req_id          BIGINT        NOT NULL                COMMENT '신청 ID (SYS_TOKEN_SEQ 시퀀스로 발급)',

    -- 신청(생성 예정) 사용자 정보
    user_id         VARCHAR(50)   NOT NULL                COMMENT '신청 아이디(새 직원 로그인 ID)',
    eml             VARCHAR(100)  NOT NULL                COMMENT '이메일',
    fnm             VARCHAR(100)  NULL                    COMMENT '사용자 이름',
    telno           VARCHAR(20)   NULL                    COMMENT '전화번호',
    role            VARCHAR(50)   NULL                    COMMENT '권한/역할 코드',
    inst_cd         VARCHAR(100)  NULL                    COMMENT '소속코드',
    addr            VARCHAR(255)  NULL                    COMMENT '주소',
    addr_dtl        VARCHAR(255)  NULL                    COMMENT '상세주소',
    post_cd         VARCHAR(20)   NULL                    COMMENT '우편번호',
    req_rsn         VARCHAR(500)  NULL                    COMMENT '신청 사유',

    -- 승인 워크플로우
    stat_cd         VARCHAR(20)   NOT NULL DEFAULT '0'  COMMENT '신청상태(공통코드 SYS_USER_REG_STATUS: 0=대기/1=신청완료/2=승인/3=반려)',
    prc_usr_id      VARCHAR(50)   NULL                    COMMENT '처리자(승인/반려/취소) ID',
    prc_dt          DATETIME      NULL                    COMMENT '처리 일시',
    prc_rsn         VARCHAR(500)  NULL                    COMMENT '처리 사유(반려/취소 사유)',
    crt_user_seq    BIGINT        NULL                    COMMENT '승인 시 생성된 SYS_USER.ID',

    -- 감사 컬럼 (SYS_USER 규칙)
    sys_crt_usr_id  VARCHAR(50)   NULL                    COMMENT '신청자 ID(작성자)',
    sys_gen_dt      DATETIME      NULL DEFAULT NOW()      COMMENT '신청 일시',
    sys_mdfr_id     VARCHAR(50)   NULL                    COMMENT '수정자 ID',
    sys_mdfcn_dt    DATETIME      NULL                    COMMENT '수정 일시',

    PRIMARY KEY (req_id),
    INDEX idx_user_req_stat (stat_cd),         -- 승인 화면 상태별 목록 필터
    INDEX idx_user_req_crtr (sys_crt_usr_id)   -- '내 신청 내역' 조회
    -- 중복 신청 방지 정책 확정 시 user_id/eml 에 UNIQUE 또는 인덱스 추가 검토
    -- (반려·취소 건의 재신청 허용 여부에 따라 UNIQUE 범위 결정 필요)
    -- 참고) 승인 후 생성 사용자 연결은 운영상 무결성으로 관리(프로젝트 관례상 물리 FK는 생략).
    --       엄격한 무결성이 필요하면 아래 FK를 활성화:
    -- , CONSTRAINT FK_SYS_USER_REQ_USER FOREIGN KEY (crt_user_seq) REFERENCES SYS_USER (ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='사용자 등록 신청';


-- =====================================================================
-- 신청 상태 공통코드 (SYS_CD_GRP / SYS_CD)
-- DRAFT: 대기(0) → REQ: 신청 완료(1) → APPR/REJ: 승인(2)/반려(3)
-- =====================================================================
INSERT INTO SYS_CD_GRP (grp_id, grp_nm, use_yn, sys_gen_dt, sys_crt_usr_id)
VALUES ('SYS_USER_REG_STATUS', '사용자 등록 신청 상태', '1', NOW(), 'system')
ON DUPLICATE KEY UPDATE grp_nm = VALUES(grp_nm), use_yn = VALUES(use_yn);

INSERT INTO SYS_CD (cd_id, grp_id, cd_nm, cd_no, use_yn, sys_gen_dt, sys_crt_usr_id) VALUES
    ('0', 'SYS_USER_REG_STATUS', '대기', 0, '1', NOW(), 'system'),
    ('1', 'SYS_USER_REG_STATUS', '신청 완료', 1, '1', NOW(), 'system'),
    ('2', 'SYS_USER_REG_STATUS', '승인', 2, '1', NOW(), 'system'),
    ('3', 'SYS_USER_REG_STATUS', '반려', 3, '1', NOW(), 'system')
ON DUPLICATE KEY UPDATE cd_nm = VALUES(cd_nm), cd_no = VALUES(cd_no), use_yn = VALUES(use_yn);
