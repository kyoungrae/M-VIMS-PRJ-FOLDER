-- ============================================================================
-- Resume script for a partial before.sql -> after.sql migration
-- Target: MariaDB 10.3 / schema NEW_MVIMS
-- Generated: 2026-06-18
--
-- Use this only when the first migration already renamed these tables:
--   SYS_BBS_BRD  -> SYS_BBS_PST
--   SYS_BBS_RPLY -> SYS_BBS_CMNT
--   SYS_EVT_LOG  -> SYS_ADT_LOG
--   SYS_OFFC     -> SYS_INST
--
-- This script continues from the SYS_TOKEN_SEQ failure point. It does not
-- reference the old table names, so it is safe for the current partial state.
-- ============================================================================

USE `NEW_MVIMS`;

SELECT GET_LOCK('NEW_MVIMS:resume_before_to_after:20260618', 10)
  INTO @MVIMS_MIGRATION_LOCK;

DELIMITER $$

DROP PROCEDURE IF EXISTS `_RESUME_BEFORE_TO_AFTER_20260618`$$

CREATE PROCEDURE `_RESUME_BEFORE_TO_AFTER_20260618`()
BEGIN
    DECLARE v_count BIGINT DEFAULT 0;
    DECLARE v_error_code INT DEFAULT NULL;
    DECLARE v_error_state VARCHAR(10) DEFAULT NULL;
    DECLARE v_error_message TEXT DEFAULT NULL;
    DECLARE v_old_sql_safe_updates INT DEFAULT 0;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        GET DIAGNOSTICS CONDITION 1
            v_error_state = RETURNED_SQLSTATE,
            v_error_code = MYSQL_ERRNO,
            v_error_message = MESSAGE_TEXT;

        UPDATE `MVIMS_SCHEMA_MIGRATION_HISTORY`
           SET `STATUS` = 'FAILED',
               `FINISHED_AT` = NOW(),
               `ERROR_CODE` = v_error_code,
               `ERROR_STATE` = v_error_state,
               `ERROR_MESSAGE` = v_error_message
         WHERE `VERSION` = '20260618_BEFORE_TO_AFTER';
        COMMIT;
        SET SESSION SQL_SAFE_UPDATES = v_old_sql_safe_updates;
        DO RELEASE_LOCK('NEW_MVIMS:resume_before_to_after:20260618');
        RESIGNAL;
    END;

    SET v_old_sql_safe_updates = @@SESSION.SQL_SAFE_UPDATES;
    SET SESSION SQL_SAFE_UPDATES = 0;

    IF COALESCE(@MVIMS_MIGRATION_LOCK, 0) <> 1 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Resume lock unavailable. Another migration may be running.';
    END IF;

    SELECT COUNT(*) INTO v_count
      FROM `information_schema`.`TABLES`
     WHERE `TABLE_SCHEMA` = DATABASE()
       AND `TABLE_NAME` IN ('SYS_BBS_PST', 'SYS_BBS_CMNT', 'SYS_ADT_LOG', 'SYS_INST');
    IF v_count <> 4 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Resume state mismatch: renamed tables are not all present.';
    END IF;

    SELECT COUNT(*) INTO v_count
      FROM `information_schema`.`TABLES`
     WHERE `TABLE_SCHEMA` = DATABASE()
       AND `TABLE_NAME` IN ('SYS_BBS_BRD', 'SYS_BBS_RPLY', 'SYS_EVT_LOG', 'SYS_OFFC');
    IF v_count <> 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Resume state mismatch: legacy table names still exist.';
    END IF;

    UPDATE `MVIMS_SCHEMA_MIGRATION_HISTORY`
       SET `STATUS` = 'RUNNING',
           `LAST_STEP` = '5_RESUME_AFTER_SYS_TOKEN_SEQ_FAILURE',
           `FINISHED_AT` = NULL,
           `ERROR_CODE` = NULL,
           `ERROR_STATE` = NULL,
           `ERROR_MESSAGE` = NULL
     WHERE `VERSION` = '20260618_BEFORE_TO_AFTER';
    COMMIT;

    -- The failed run did not yet have a SYS_TOKEN_SEQ backup. Preserve a safe
    -- replacement value using the next unused SYS_TOKEN.ID.
    CREATE TABLE IF NOT EXISTS `BAK_SYS_TOKEN_SEQ_20260618` (
        `next_not_cached_value` BIGINT NOT NULL,
        `minimum_value` BIGINT NOT NULL,
        `maximum_value` BIGINT NOT NULL,
        `start_value` BIGINT NOT NULL,
        `increment` BIGINT NOT NULL,
        `cache_size` BIGINT NOT NULL,
        `cycle_option` TINYINT NOT NULL,
        `cycle_count` BIGINT NOT NULL
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Fallback backup for rebuilt SYS_TOKEN_SEQ';

    SELECT COUNT(*) INTO v_count FROM `BAK_SYS_TOKEN_SEQ_20260618`;
    IF v_count = 0 THEN
        INSERT INTO `BAK_SYS_TOKEN_SEQ_20260618` (
            `next_not_cached_value`, `minimum_value`, `maximum_value`,
            `start_value`, `increment`, `cache_size`, `cycle_option`, `cycle_count`
        )
        SELECT COALESCE(MAX(`ID`), 0) + 1,
               1,
               9223372036854775807,
               1,
               1,
               1000,
               0,
               0
          FROM `SYS_TOKEN`;
    END IF;

    -- The broken object must be dropped as a sequence/table and then recreated
    -- as the normal target table from after.sql.
    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION BEGIN END;
        DROP SEQUENCE IF EXISTS `SYS_TOKEN_SEQ`;
    END;

    BEGIN
        DECLARE CONTINUE HANDLER FOR SQLEXCEPTION BEGIN END;
        DROP TABLE IF EXISTS `SYS_TOKEN_SEQ`;
    END;

    CREATE TABLE `SYS_TOKEN_SEQ` (
        `NXT_NOT_CHCD_VAL` BIGINT NOT NULL COMMENT '캐시되지않은다음값',
        `MIN_VL` BIGINT NOT NULL COMMENT '최소값',
        `MAX_VL` BIGINT NOT NULL COMMENT '최대값',
        `BGNG_VL` BIGINT NOT NULL COMMENT '시작값',
        `INCRS_VL` BIGINT NOT NULL COMMENT '증가값',
        `CACHE_SZ` BIGINT NOT NULL COMMENT '캐시크기',
        `CYCLE_OPT` TINYINT NOT NULL COMMENT '사이클옵션',
        `CYCLE_CNT` BIGINT NOT NULL COMMENT '사이클횟수'
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='토큰 시퀀스';

    INSERT INTO `SYS_TOKEN_SEQ` (
        `NXT_NOT_CHCD_VAL`, `MIN_VL`, `MAX_VL`, `BGNG_VL`,
        `INCRS_VL`, `CACHE_SZ`, `CYCLE_OPT`, `CYCLE_CNT`
    )
    SELECT `next_not_cached_value`, `minimum_value`, `maximum_value`,
           `start_value`, `increment`, `cache_size`,
           `cycle_option`, `cycle_count`
      FROM `BAK_SYS_TOKEN_SEQ_20260618`;

    -- Continue the original migration from the statement after SYS_TOKEN_SEQ.
    SELECT COUNT(*) INTO v_count
      FROM `information_schema`.`COLUMNS`
     WHERE `TABLE_SCHEMA` = DATABASE()
       AND `TABLE_NAME` = 'SYS_CD'
       AND `COLUMN_NAME` = 'SYS_UPD_USR_ID';
    IF v_count = 1 THEN
        ALTER TABLE `SYS_CD`
            DROP PRIMARY KEY,
            ADD COLUMN `UP_CD_ID` VARCHAR(10) NULL COMMENT '상위코드ID' AFTER `USE_YN`,
            CHANGE COLUMN `SYS_CRT_DT` `SYS_GEN_DT` DATETIME NULL COMMENT '시스템생성일시',
            CHANGE COLUMN `SYS_UPD_DT` `SYS_MDFCN_DT` DATETIME NULL COMMENT '시스템수정일시',
            CHANGE COLUMN `SYS_UPD_USR_ID` `TUN_APRV_YMD` VARCHAR(20) NULL COMMENT '시스템수정자ID',
            MODIFY COLUMN `USE_YN` CHAR(1) NULL DEFAULT 'Y' COMMENT '사용여부',
            ADD PRIMARY KEY (`GRP_ID`, `CD_ID`),
            COMMENT = '코드정보';
    END IF;

    UPDATE `MVIMS_SCHEMA_MIGRATION_HISTORY`
       SET `LAST_STEP` = '5_EXISTING_TABLES_ALTERED'
     WHERE `VERSION` = '20260618_BEFORE_TO_AFTER';
    COMMIT;

    SELECT COUNT(*) INTO v_count
      FROM `information_schema`.`COLUMNS`
     WHERE `TABLE_SCHEMA` = DATABASE()
       AND `TABLE_NAME` = 'SYS_BBS_PST'
       AND `COLUMN_NAME` = 'BOARD_ID';
    IF v_count = 1 THEN
        ALTER TABLE `SYS_BBS_PST`
            CHANGE COLUMN `BOARD_ID` `PST_ID` VARCHAR(50) NOT NULL COMMENT '게시물ID',
            CHANGE COLUMN `TITLE` `TTL` VARCHAR(200) NOT NULL COMMENT '글제목',
            CHANGE COLUMN `CONTENT` `CN` LONGTEXT NULL COMMENT '글내용',
            CHANGE COLUMN `HIT_CNT` `INQ_CNT` INT NULL DEFAULT 0 COMMENT '조회수',
            CHANGE COLUMN `THMBNL` `THMB_PATH` VARCHAR(255) NULL COMMENT '썸네일경로',
            CHANGE COLUMN `SYS_CRT_DT` `SYS_GEN_DT` DATETIME NULL COMMENT '시스템생성일시',
            CHANGE COLUMN `SYS_UPD_USR_ID` `SYS_MDFR_ID` VARCHAR(20) NULL COMMENT '시스템수정자ID',
            CHANGE COLUMN `SYS_UPD_DT` `SYS_MDFCN_DT` DATETIME NULL COMMENT '시스템수정일시',
            COMMENT = '게시판 게시물';
    END IF;

    SELECT COUNT(*) INTO v_count
      FROM `information_schema`.`COLUMNS`
     WHERE `TABLE_SCHEMA` = DATABASE()
       AND `TABLE_NAME` = 'SYS_BBS_CMNT'
       AND `COLUMN_NAME` = 'REPLY_ID';
    IF v_count = 1 THEN
        ALTER TABLE `SYS_BBS_CMNT`
            CHANGE COLUMN `REPLY_ID` `CMNT_ID` VARCHAR(50) NOT NULL COMMENT '댓글ID',
            CHANGE COLUMN `BOARD_ID` `PST_ID` VARCHAR(50) NOT NULL COMMENT '게시물ID',
            CHANGE COLUMN `PARENT_REPLY_ID` `PRNT_CMNT_ID` VARCHAR(50) NULL COMMENT '부모댓글ID',
            CHANGE COLUMN `CONTENT` `CN` TEXT NOT NULL COMMENT '내용',
            CHANGE COLUMN `SYS_CRT_DT` `SYS_GEN_DT` DATETIME NULL COMMENT '시스템생성일시',
            CHANGE COLUMN `SYS_UPD_USR_ID` `SYS_MDFR_ID` VARCHAR(20) NULL COMMENT '시스템수정자ID',
            CHANGE COLUMN `SYS_UPD_DT` `SYS_MDFCN_DT` DATETIME NULL COMMENT '시스템수정일시',
            COMMENT = '게시판 댓글';
    END IF;

    SELECT COUNT(*) INTO v_count
      FROM `information_schema`.`COLUMNS`
     WHERE `TABLE_SCHEMA` = DATABASE()
       AND `TABLE_NAME` = 'SYS_ADT_LOG'
       AND `COLUMN_NAME` = 'SYS_CRT_DT';
    IF v_count = 1 THEN
        ALTER TABLE `SYS_ADT_LOG`
            DROP PRIMARY KEY,
            DROP COLUMN `EMAIL`,
            DROP COLUMN `IP_ADDR`,
            DROP COLUMN `SYS_CRT_DT`,
            CHANGE COLUMN `TGT_TBL` `TRGT_TBL_NM` VARCHAR(50) NOT NULL COMMENT '대상테이블명',
            CHANGE COLUMN `TGT_ID` `TRGT_ID` VARCHAR(100) NULL COMMENT '대상ID',
            CHANGE COLUMN `BFR_DATA` `CHG_PREV_DATA` LONGTEXT NULL COMMENT '변경전데이터',
            CHANGE COLUMN `AFT_DATA` `CHG_AFTR_DATA` LONGTEXT NULL COMMENT '변경이후데이터',
            ADD COLUMN `CHCK` VARCHAR(1) NULL COMMENT '점검' AFTER `CHG_PREV_DATA`,
            MODIFY COLUMN `UP_GRP_ID` VARCHAR(50) NOT NULL COMMENT '상위그룹ID',
            ADD PRIMARY KEY (`ID`),
            COMMENT = '감사로그';
    END IF;

    SELECT COUNT(*) INTO v_count
      FROM `information_schema`.`COLUMNS`
     WHERE `TABLE_SCHEMA` = DATABASE()
       AND `TABLE_NAME` = 'SYS_INST'
       AND `COLUMN_NAME` = 'OFFC_CD';
    IF v_count = 1 THEN
        ALTER TABLE `SYS_INST`
            CHANGE COLUMN `OFFC_CD` `INST_CD` VARCHAR(7) NOT NULL COMMENT '기관코드',
            CHANGE COLUMN `TOP_OFFC_CD` `UP_INST_CD` VARCHAR(7) NOT NULL COMMENT '최상위코드',
            CHANGE COLUMN `OFFC_NM` `INST_NM` VARCHAR(100) NOT NULL COMMENT '기관명',
            CHANGE COLUMN `OFFC_TYPE` `INST_SE_CD` VARCHAR(100) NULL COMMENT '기관구분코드',
            CHANGE COLUMN `OFFC_TYPE_CD` `INST_TYPE_CD` VARCHAR(4) NULL COMMENT '기관유형코드',
            ADD COLUMN `INST_RBPRSN` VARCHAR(100) NULL COMMENT '기관책임자' AFTER `INST_TYPE_CD`,
            ADD COLUMN `TELNO` VARCHAR(100) NULL COMMENT '전화번호' AFTER `INST_RBPRSN`,
            ADD COLUMN `EML` VARCHAR(100) NULL COMMENT '이메일' AFTER `TELNO`,
            ADD COLUMN `OPER_HR` VARCHAR(100) NULL COMMENT '운영시간' AFTER `EML`,
            ADD COLUMN `AIMG_CD` VARCHAR(2) NULL COMMENT '아이막코드' AFTER `OPER_HR`,
            ADD COLUMN `ADDR` VARCHAR(100) NULL COMMENT '주소' AFTER `AIMG_CD`,
            ADD COLUMN `ADDR_DTL` VARCHAR(100) NULL COMMENT '주소상세' AFTER `ADDR`,
            ADD COLUMN `LAT` FLOAT NULL COMMENT '위도' AFTER `ADDR_DTL`,
            ADD COLUMN `LOT` FLOAT NULL COMMENT '경도' AFTER `LAT`,
            ADD COLUMN `WGHT_LMT_YN` CHAR(1) NULL COMMENT '중량제한여부' AFTER `LOT`,
            ADD COLUMN `PRIVT_YN` CHAR(1) NULL COMMENT '민간여부' AFTER `WGHT_LMT_YN`,
            ADD COLUMN `PRMSN_BGNG_YMD` VARCHAR(8) NULL COMMENT '허가시작일자' AFTER `PRIVT_YN`,
            ADD COLUMN `PRMSN_END_YMD` VARCHAR(8) NULL COMMENT '허가종료일자' AFTER `PRMSN_BGNG_YMD`,
            ADD COLUMN `USE_YN` CHAR(1) NULL DEFAULT 'Y' COMMENT '사용여부' AFTER `PRMSN_END_YMD`,
            CHANGE COLUMN `SYS_CRT_DT` `SYS_GEN_DT` DATETIME NULL COMMENT '시스템생성일시',
            CHANGE COLUMN `SYS_UPD_USR_ID` `SYS_MDFR_ID` VARCHAR(20) NULL COMMENT '시스템수정자ID',
            CHANGE COLUMN `SYS_UPD_DT` `SYS_MDFCN_DT` DATETIME NULL COMMENT '시스템수정일시',
            COMMENT = '기관';
    END IF;

    UPDATE `MVIMS_SCHEMA_MIGRATION_HISTORY`
       SET `LAST_STEP` = '6_RENAMED_TABLES_ALTERED'
     WHERE `VERSION` = '20260618_BEFORE_TO_AFTER';
    COMMIT;

    CREATE TABLE IF NOT EXISTS `SYS_ACHV_STNG` (
        `ID` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
        `AIMAG_CD` VARCHAR(2) NULL COMMENT '아이막코드',
        `INST_CD` VARCHAR(7) NULL COMMENT '기관코드',
        `ARCHV_NM` VARCHAR(30) NULL COMMENT '아카이브명',
        `ABBR` VARCHAR(100) NULL COMMENT '약어',
        `USE_YN` CHAR(1) NULL DEFAULT 'Y' COMMENT '사용여부',
        `RGTR_ID` VARCHAR(20) NULL COMMENT '등록자ID',
        `REG_DT` DATETIME NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
        `MDFR_ID` VARCHAR(20) NULL COMMENT '수정자ID',
        `MDFCN_DT` DATETIME NULL COMMENT '수정일시',
        PRIMARY KEY (`ID`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='아카이브기본정보';

    UPDATE `MVIMS_SCHEMA_MIGRATION_HISTORY`
       SET `LAST_STEP` = '7_NEW_TABLE_CREATED'
     WHERE `VERSION` = '20260618_BEFORE_TO_AFTER';
    COMMIT;

    ALTER TABLE `SYS_BBS_PST`
        MODIFY COLUMN `PST_ID` VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '게시물ID';
    ALTER TABLE `SYS_BBS_CMNT`
        MODIFY COLUMN `PST_ID` VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '게시물ID';
    ALTER TABLE `SYS_FILE`
        MODIFY COLUMN `FILE_UUID` VARCHAR(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '파일UUID';
    ALTER TABLE `SYS_FILE_DTL`
        MODIFY COLUMN `FILE_UUID` VARCHAR(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '파일UUID';
    ALTER TABLE `SYS_DEPT_GRP`
        MODIFY COLUMN `GRP_ID` VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '그룹아이디';
    ALTER TABLE `SYS_USER_GRP`
        MODIFY COLUMN `GRP_ID` VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '그룹아이디';
    ALTER TABLE `SYS_ACS_GRP_MENU`
        MODIFY COLUMN `GRP_ID` VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '그룹아이디';
    ALTER TABLE `SYS_INST`
        MODIFY COLUMN `INST_CD` VARCHAR(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '기관코드',
        MODIFY COLUMN `UP_INST_CD` VARCHAR(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '최상위코드';
    ALTER TABLE `SYS_CD_GRP`
        MODIFY COLUMN `GRP_ID` VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '그룹아이디';
    ALTER TABLE `SYS_CD`
        MODIFY COLUMN `GRP_ID` VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '그룹아이디';

    SELECT COUNT(*) INTO v_count
      FROM `information_schema`.`TABLE_CONSTRAINTS`
     WHERE `TABLE_SCHEMA` = DATABASE()
       AND `TABLE_NAME` = 'SYS_BBS_CMNT'
       AND `CONSTRAINT_NAME` = 'FK_SYS_BBS_CMNT_SYS_BBS_PST';
    IF v_count = 0 THEN
        SELECT COUNT(*) INTO v_count
          FROM `SYS_BBS_CMNT` c
          LEFT JOIN `SYS_BBS_PST` p
            ON CONVERT(p.`PST_ID` USING utf8mb4) COLLATE utf8mb4_unicode_ci
             = CONVERT(c.`PST_ID` USING utf8mb4) COLLATE utf8mb4_unicode_ci
         WHERE p.`PST_ID` IS NULL;
        IF v_count = 0 THEN
            ALTER TABLE `SYS_BBS_CMNT`
                ADD CONSTRAINT `FK_SYS_BBS_CMNT_SYS_BBS_PST`
                FOREIGN KEY (`PST_ID`) REFERENCES `SYS_BBS_PST` (`PST_ID`)
                ON DELETE CASCADE;
        END IF;
    END IF;

    SELECT COUNT(*) INTO v_count
      FROM `information_schema`.`TABLE_CONSTRAINTS`
     WHERE `TABLE_SCHEMA` = DATABASE()
       AND `TABLE_NAME` = 'SYS_FILE_DTL'
       AND `CONSTRAINT_NAME` = 'FK_SYS_FILE_DTL_SYS_FILE';
    IF v_count = 0 THEN
        SELECT COUNT(*) INTO v_count
          FROM `SYS_FILE_DTL` d
          LEFT JOIN `SYS_FILE` f
            ON CONVERT(f.`FILE_UUID` USING utf8mb4) COLLATE utf8mb4_unicode_ci
             = CONVERT(d.`FILE_UUID` USING utf8mb4) COLLATE utf8mb4_unicode_ci
         WHERE f.`FILE_UUID` IS NULL;
        IF v_count = 0 THEN
            ALTER TABLE `SYS_FILE_DTL`
                ADD CONSTRAINT `FK_SYS_FILE_DTL_SYS_FILE`
                FOREIGN KEY (`FILE_UUID`) REFERENCES `SYS_FILE` (`FILE_UUID`)
                ON DELETE CASCADE;
        END IF;
    END IF;

    SELECT COUNT(*) INTO v_count
      FROM `information_schema`.`TABLE_CONSTRAINTS`
     WHERE `TABLE_SCHEMA` = DATABASE()
       AND `TABLE_NAME` = 'SYS_USER_GRP'
       AND `CONSTRAINT_NAME` = 'FK_SYS_USER_GRP_SYS_DEPT_GRP';
    IF v_count = 0 THEN
        SELECT COUNT(*) INTO v_count
          FROM `SYS_USER_GRP` ug
          LEFT JOIN `SYS_DEPT_GRP` dg
            ON CONVERT(dg.`GRP_ID` USING utf8mb4) COLLATE utf8mb4_unicode_ci
             = CONVERT(ug.`GRP_ID` USING utf8mb4) COLLATE utf8mb4_unicode_ci
         WHERE dg.`GRP_ID` IS NULL;
        IF v_count = 0 THEN
            ALTER TABLE `SYS_USER_GRP`
                ADD CONSTRAINT `FK_SYS_USER_GRP_SYS_DEPT_GRP`
                FOREIGN KEY (`GRP_ID`) REFERENCES `SYS_DEPT_GRP` (`GRP_ID`)
                ON DELETE CASCADE;
        END IF;
    END IF;

    SELECT COUNT(*) INTO v_count
      FROM `information_schema`.`TABLE_CONSTRAINTS`
     WHERE `TABLE_SCHEMA` = DATABASE()
       AND `TABLE_NAME` = 'SYS_USER_GRP'
       AND `CONSTRAINT_NAME` = 'FK_SYS_USER_GRP_SYS_USER';
    IF v_count = 0 THEN
        SELECT COUNT(*) INTO v_count
          FROM `SYS_USER_GRP` ug
          LEFT JOIN `SYS_USER` u ON u.`ID` = ug.`ID`
         WHERE u.`ID` IS NULL;
        IF v_count = 0 THEN
            ALTER TABLE `SYS_USER_GRP`
                ADD CONSTRAINT `FK_SYS_USER_GRP_SYS_USER`
                FOREIGN KEY (`ID`) REFERENCES `SYS_USER` (`ID`)
                ON DELETE CASCADE;
        END IF;
    END IF;

    SELECT COUNT(*) INTO v_count
      FROM `information_schema`.`TABLE_CONSTRAINTS`
     WHERE `TABLE_SCHEMA` = DATABASE()
       AND `TABLE_NAME` = 'SYS_INST'
       AND `CONSTRAINT_NAME` = 'FK_SYS_INST_SYS_INST';
    IF v_count = 0 THEN
        SELECT COUNT(*) INTO v_count
          FROM `SYS_INST` c
          LEFT JOIN `SYS_INST` p
            ON CONVERT(p.`INST_CD` USING utf8mb4) COLLATE utf8mb4_unicode_ci
             = CONVERT(c.`UP_INST_CD` USING utf8mb4) COLLATE utf8mb4_unicode_ci
         WHERE p.`INST_CD` IS NULL;
        IF v_count = 0 THEN
            ALTER TABLE `SYS_INST`
                ADD CONSTRAINT `FK_SYS_INST_SYS_INST`
                FOREIGN KEY (`UP_INST_CD`) REFERENCES `SYS_INST` (`INST_CD`)
                ON DELETE CASCADE;
        END IF;
    END IF;

    SELECT COUNT(*) INTO v_count
      FROM `information_schema`.`TABLE_CONSTRAINTS`
     WHERE `TABLE_SCHEMA` = DATABASE()
       AND `TABLE_NAME` = 'SYS_ACS_GRP_MENU'
       AND `CONSTRAINT_NAME` = 'FK_SYS_ACS_GRP_MENU_SYS_DEPT_GRP';
    IF v_count = 0 THEN
        SELECT COUNT(*) INTO v_count
          FROM `SYS_ACS_GRP_MENU` m
          LEFT JOIN `SYS_DEPT_GRP` dg
            ON CONVERT(dg.`GRP_ID` USING utf8mb4) COLLATE utf8mb4_unicode_ci
             = CONVERT(m.`GRP_ID` USING utf8mb4) COLLATE utf8mb4_unicode_ci
         WHERE dg.`GRP_ID` IS NULL;
        IF v_count = 0 THEN
            ALTER TABLE `SYS_ACS_GRP_MENU`
                ADD CONSTRAINT `FK_SYS_ACS_GRP_MENU_SYS_DEPT_GRP`
                FOREIGN KEY (`GRP_ID`) REFERENCES `SYS_DEPT_GRP` (`GRP_ID`)
                ON DELETE CASCADE;
        END IF;
    END IF;

    SELECT COUNT(*) INTO v_count
      FROM `information_schema`.`TABLE_CONSTRAINTS`
     WHERE `TABLE_SCHEMA` = DATABASE()
       AND `TABLE_NAME` = 'SYS_CD'
       AND `CONSTRAINT_NAME` = 'FK_SYS_CD_SYS_CD_GRP';
    IF v_count = 0 THEN
        SELECT COUNT(*) INTO v_count
          FROM `SYS_CD` c
          LEFT JOIN `SYS_CD_GRP` g
            ON CONVERT(g.`GRP_ID` USING utf8mb4) COLLATE utf8mb4_unicode_ci
             = CONVERT(c.`GRP_ID` USING utf8mb4) COLLATE utf8mb4_unicode_ci
         WHERE g.`GRP_ID` IS NULL;
        IF v_count = 0 THEN
            ALTER TABLE `SYS_CD`
                ADD CONSTRAINT `FK_SYS_CD_SYS_CD_GRP`
                FOREIGN KEY (`GRP_ID`) REFERENCES `SYS_CD_GRP` (`GRP_ID`)
                ON DELETE CASCADE;
        END IF;
    END IF;

    UPDATE `MVIMS_SCHEMA_MIGRATION_HISTORY`
       SET `LAST_STEP` = '8_FOREIGN_KEYS_REBUILT'
     WHERE `VERSION` = '20260618_BEFORE_TO_AFTER';
    COMMIT;

    SELECT COUNT(*) INTO v_count
      FROM `information_schema`.`TABLES`
     WHERE `TABLE_SCHEMA` = DATABASE()
       AND `TABLE_NAME` IN (
           'SYS_ACS_GRP_MENU', 'SYS_ACS_LOG', 'SYS_ADT_LOG', 'SYS_ACHV_STNG',
           'SYS_BBS', 'SYS_BBS_CMNT', 'SYS_BBS_MST', 'SYS_BBS_PST',
           'SYS_CD', 'SYS_CD_GRP', 'SYS_DEPT_GRP', 'SYS_FILE', 'SYS_FILE_DTL',
           'SYS_ICON', 'SYS_INST', 'SYS_MENU', 'SYS_SITE_CFG', 'SYS_SITE_CFG_GRP',
           'SYS_TOKEN', 'SYS_TOKEN_SEQ', 'SYS_USER', 'SYS_USER_GRP'
       );
    IF v_count <> 22 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Post-check failed: the 22 target SYS_* tables were not all created.';
    END IF;

    SELECT COUNT(*) INTO v_count
      FROM `information_schema`.`TABLES`
     WHERE `TABLE_SCHEMA` = DATABASE()
       AND `TABLE_NAME` IN ('SYS_BBS_BRD', 'SYS_BBS_RPLY', 'SYS_EVT_LOG', 'SYS_OFFC');
    IF v_count <> 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Post-check failed: one or more legacy table names still exist.';
    END IF;

    UPDATE `MVIMS_SCHEMA_MIGRATION_HISTORY`
       SET `STATUS` = 'COMPLETED',
           `LAST_STEP` = '9_VALIDATION_COMPLETED',
           `FINISHED_AT` = NOW(),
           `ERROR_CODE` = NULL,
           `ERROR_STATE` = NULL,
           `ERROR_MESSAGE` = NULL
     WHERE `VERSION` = '20260618_BEFORE_TO_AFTER';
    COMMIT;

    SET SESSION SQL_SAFE_UPDATES = v_old_sql_safe_updates;
    DO RELEASE_LOCK('NEW_MVIMS:resume_before_to_after:20260618');
END$$

CALL `_RESUME_BEFORE_TO_AFTER_20260618`()$$

DROP PROCEDURE IF EXISTS `_RESUME_BEFORE_TO_AFTER_20260618`$$

DELIMITER ;

SELECT `VERSION`, `STATUS`, `LAST_STEP`, `ERROR_CODE`, `ERROR_STATE`, `ERROR_MESSAGE`
  FROM `MVIMS_SCHEMA_MIGRATION_HISTORY`
 WHERE `VERSION` = '20260618_BEFORE_TO_AFTER';

SELECT `TABLE_NAME`
  FROM `information_schema`.`TABLES`
 WHERE `TABLE_SCHEMA` = DATABASE()
   AND `TABLE_NAME` LIKE 'SYS_%'
 ORDER BY `TABLE_NAME`;

SELECT `CONSTRAINT_NAME`, `TABLE_NAME`, `COLUMN_NAME`,
       `REFERENCED_TABLE_NAME`, `REFERENCED_COLUMN_NAME`
  FROM `information_schema`.`KEY_COLUMN_USAGE`
 WHERE `TABLE_SCHEMA` = DATABASE()
   AND `REFERENCED_TABLE_NAME` IS NOT NULL
 ORDER BY `TABLE_NAME`, `CONSTRAINT_NAME`, `ORDINAL_POSITION`;
