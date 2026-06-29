-- =====================================================================
-- 사용자 등록 신청 상태 공통코드 (SYS_USER_REG_STATUS)
-- stat_cd: 0=대기, 1=신청 완료, 2=승인, 3=반려
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

-- 기존 영문 코드 데이터가 있으면 숫자 코드로 변환
UPDATE SYS_USER_REQ SET stat_cd = '0' WHERE stat_cd IN ('DRAFT', 'WAIT');
UPDATE SYS_USER_REQ SET stat_cd = '1' WHERE stat_cd IN ('REQ', 'SUBMITTED');
UPDATE SYS_USER_REQ SET stat_cd = '2' WHERE stat_cd IN ('APPR', 'APPROVED');
UPDATE SYS_USER_REQ SET stat_cd = '3' WHERE stat_cd IN ('REJ', 'REJECTED', 'CANCEL');
