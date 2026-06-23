-- =====================================================================
-- '사용자 등록 신청' 메뉴 등록
-- 위치: 설정/관리 > 사용자 관리 > 사용자 등록 신청
-- 페이지 URL: /user/sysUserReqList.html  (templates/page/user/sysUserReqList.html)
-- ---------------------------------------------------------------------
-- [권장] 가능하면 '메뉴 관리' 화면에서 등록하세요.
--        - 메뉴명: 사용자 등록 신청 / 상위메뉴: 사용자 관리
--        - URL: /user/sysUserReqList.html / 사용여부: Y
--        UI로 등록하면 메뉴 순서/레벨과 '접근권한(SYS_ACCS_GROUP_MENU)' 매핑까지
--        함께 처리되어 메뉴가 사이드바에 정상 노출됩니다.
--
-- [SQL 직접 등록 시] 아래 스크립트는 상위 '사용자 관리' 메뉴를 이름으로 찾아
--        그 하위에 leaf 메뉴를 추가합니다. 운영 DB의 menu_no/menu_lvl 표기 규칙에
--        맞게 값(menu_cd, menu_no, menu_unq_no)을 확인/조정하세요.
-- =====================================================================

-- 1) 메뉴 본체 등록 (상위 '사용자 관리' 기준)
INSERT INTO SYS_MENU (
    menu_cd, menu_nm_kr, menu_nm_en, menu_nm_mn,
    top_menu_cd, menu_lvl, menu_no, url, use_yn,
    sys_gen_dt, sys_crt_usr_id
)
SELECT
    'USER_REQ',                                   -- 신규 메뉴 코드 (중복 없는 값으로)
    '사용자 등록 신청', 'User Registration Request', 'Хэрэглэгч бүртгэлийн хүсэлт',
    p.menu_cd,                                    -- 상위(사용자 관리) 코드
    CAST(CAST(p.menu_lvl AS UNSIGNED) + 1 AS CHAR),
    '99',                                         -- 정렬 순서 (필요시 조정)
    '/user/sysUserReqList.html',
    '1',
    NOW(), 'system'
FROM SYS_MENU p
WHERE p.menu_nm_kr = '사용자 관리'
  AND NOT EXISTS (SELECT 1 FROM SYS_MENU m WHERE m.menu_cd = 'USER_REQ')
LIMIT 1;

-- 2) 접근권한 매핑 (선택)
--    운영 DB의 SYS_ACCS_GROUP_MENU 컬럼 규칙에 맞춰, 관리자 접근그룹에
--    'USER_REQ' 메뉴를 매핑해야 사이드바에 노출됩니다.
--    (정확한 컬럼/그룹코드는 '접근권한 관리' 화면 또는 기존 데이터를 참고)
-- 예시:
-- INSERT INTO SYS_ACCS_GROUP_MENU (accs_grp_cd, menu_cd, use_yn, sys_gen_dt, sys_crt_usr_id)
-- SELECT DISTINCT accs_grp_cd, 'USER_REQ', '1', NOW(), 'system'
--   FROM SYS_ACCS_GROUP_MENU
--  WHERE menu_cd = (SELECT menu_cd FROM SYS_MENU WHERE menu_nm_kr='사용자 관리' LIMIT 1);
