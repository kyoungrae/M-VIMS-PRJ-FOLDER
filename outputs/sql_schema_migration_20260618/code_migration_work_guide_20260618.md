# before -> after 코드 전환 작업 가이드

작성일: 2026-06-18  
대상 DB: `NEW_MVIMS`  
목표: 마이그레이션 완료된 after 스키마 기준으로 애플리케이션 코드를 정상 구동 가능하게 수정

## 1. 현재 상태 요약

DB 스키마 마이그레이션은 완료된 상태로 본다.

확인된 DB 상태:

| 항목 | 상태 |
|---|---|
| `SYS_*` 대상 테이블 수 | 22개 |
| before legacy 테이블 | 없음 |
| after 대상 테이블 | 존재 |
| `SYS_TOKEN_SEQ` | after 구조로 재생성 완료 |
| 데이터 보존형 길이 예외 | 반영됨 |

의도적으로 after 원본과 다르게 둔 컬럼:

| 테이블 | 컬럼 | after 원본 | 현재 DB | 사유 |
|---|---|---:|---:|---|
| `SYS_USER` | `TELNO` | `varchar(8)` | `varchar(11)` | 기존 전화번호 데이터 보존 |
| `SYS_INST` | `INST_SE_CD` | `varchar(1)` | `varchar(100)` | 기존 기관구분 데이터 보존 |
| `SYS_USER` | `SYS_MDFR_ID` | `varchar(20)` | `varchar(50)` | 기존 수정자 ID 데이터 보존 |
| `SYS_USER_GRP` | `ID` | `int` | `bigint` | `SYS_USER.ID`와 타입 정합성 확보 |
| `SYS_ACS_LOG` | `ISTR_TYPE` | 깨진 `ENUM` | `enum('PC','MOBILE','TABLET','OTHER')` | 실행 가능한 DDL로 보정 |

주의:

- 이제 DB는 before 이름을 받지 않는다.
- 전체 마이그레이션 SQL을 다시 실행하지 않는다.
- 코드 수정은 after 컬럼명 기준으로 진행한다.
- `BAK_*` 테이블은 운영 검증이 끝날 때까지 삭제하지 않는다.

## 2. 작업 목표

1. 앱 실행 시 old table/column SQL 오류 제거
2. MyBatis Mapper와 Java 필드명을 after 스키마에 맞춤
3. 화면 form/grid/ajax 파라미터를 Java/SQL과 맞춤
4. 로그인, 게시판, 기관, 파일, 코드, 권한, 토큰, 감사로그 주요 기능 정상화
5. 운영 전까지 데이터 손실 없이 검증 가능하게 유지

## 3. 예상 작업 시간

Codex가 직접 작업하는 기준:

| 범위 | 예상 |
|---|---:|
| SQL 오류만 막는 최소 수정 | 2~4시간 |
| 앱 구동 가능한 수준 | 4~7시간 |
| 주요 기능 테스트 포함 안정형 | 6~10시간 |
| 메시지 키, 샘플, 주석까지 전체 정리 | 10~16시간 |

권장 목표:

| 단계 | 목표 | 예상 |
|---|---|---:|
| 1차 | 앱 구동과 주요 SQL 오류 제거 | 4~6시간 |
| 2차 | 주요 기능 수동 테스트와 누락 보정 | 2~4시간 |
| 3차 | 메시지 키, 샘플, 주석 정리 | 선택 |

## 4. 최우선 수정 파일

먼저 수정해야 하는 파일:

| 우선순위 | 파일 | 이유 |
|---|---|---|
| P0 | `Core-lib/core-lib/src/main/resources/mybatis-core/common/SysEventLogMapper.xml` | `SYS_EVT_LOG`가 `SYS_ADT_LOG`로 변경됨 |
| P0 | `Core-lib/core-lib/src/main/java/com/system/common/interceptor/EventLogInterceptor.java` | 감사 로그 생성 컬럼 변경 영향 |
| P0 | `Core-lib/core-lib/src/main/java/com/system/common/db/DbPartitionService.java` | 로그 테이블 파티션 제거 영향 |
| P0 | `Core-lib/core-lib/src/main/java/com/system/auth/service/AuthenticationService.java` | 토큰 컬럼 `EXP`, `REVOK`, `TOKEN` 변경 영향 |
| P0 | `vims-management-system/src/main/resources/mybatis/common/SysBbsBoardMapper.xml` | `SYS_BBS_BRD`가 `SYS_BBS_PST`로 변경됨 |
| P0 | `vims-management-system/src/main/resources/mybatis/common/SysBbsReplyMapper.xml` | `SYS_BBS_RPLY`가 `SYS_BBS_CMNT`로 변경됨 |
| P0 | `vims-management-system/src/main/resources/mybatis/common/SysOfficeMapper.xml` | `SYS_OFFC`가 `SYS_INST`로 변경됨 |
| P0 | `vims-management-system/src/main/resources/mybatis/common/SysUserMapper.xml` | 사용자/기관 컬럼 변경 영향 |
| P0 | `vims-management-system/src/main/resources/mybatis/common/SysUserGroupMapper.xml` | 사용자그룹/기관 컬럼 변경 영향 |
| P1 | `vims-management-system/src/main/java/com/vims/common/bbs/*.java` | 게시판 DTO/Entity/Service 필드 변경 |
| P1 | `vims-management-system/src/main/resources/templates/page/bbs/*.html` | 게시판 화면 JSON key 변경 |
| P1 | `vims-management-system/src/main/resources/templates/page/office/*.html` | 기관 화면 이름 변경 |
| P1 | `vims-management-system/src/main/resources/templates/page/user/*.html` | 사용자 화면 컬럼 변경 |
| P2 | `vims-login/src/main/resources/static/common/js/message/**` | 다국어 라벨 키 정리 |

## 5. 핵심 테이블 매핑

| before | after | 작업 |
|---|---|---|
| `SYS_BBS_BRD` | `SYS_BBS_PST` | 게시글 SQL, DTO, 화면 key 변경 |
| `SYS_BBS_RPLY` | `SYS_BBS_CMNT` | 댓글 SQL, DTO, 화면 key 변경 |
| `SYS_EVT_LOG` | `SYS_ADT_LOG` | 감사로그 SQL, 인터셉터, 조회 화면 변경 |
| `SYS_OFFC` | `SYS_INST` | 기관 SQL, 사용자 소속 조회, 화면 변경 |

## 6. 핵심 컬럼 매핑

### 공통 시스템 컬럼

| before | after |
|---|---|
| `SYS_CRT_DT` | `SYS_GEN_DT` |
| `SYS_UPD_DT` | `SYS_MDFCN_DT` |
| `SYS_UPD_USR_ID` | `SYS_MDFR_ID` |

예외:

| 테이블 | before | after |
|---|---|---|
| `SYS_DEPT_GRP` | `SYS_CRT_USR_ID` | `SYS_GEN_ID` |
| `SYS_CD` | `SYS_UPD_USR_ID` | `TUN_APRV_YMD` |

`SYS_CD.TUN_APRV_YMD`는 컬럼명이 업무 의미상 애매하다. 코드 수정 시 단순히 `sys_mdf_r_id`처럼 해석하지 말고, 실제 화면/업무에서 수정자 ID로 계속 쓰는지 확인해야 한다.

### 게시판

| before 테이블 | before 컬럼 | after 테이블 | after 컬럼 |
|---|---|---|---|
| `SYS_BBS` | `BBS_MANAGER` | `SYS_BBS` | `BBS_MNGR` |
| `SYS_BBS` | `FILE_YN` | `SYS_BBS` | `FILE_ATCH_YN` |
| `SYS_BBS` | `REPLY_YN` | `SYS_BBS` | `CMNT_YN` |
| `SYS_BBS` | `P_MENU_CD` | `SYS_BBS` | `UP_MENU_CD` |
| `SYS_BBS_BRD` | `BOARD_ID` | `SYS_BBS_PST` | `PST_ID` |
| `SYS_BBS_BRD` | `TITLE` | `SYS_BBS_PST` | `TTL` |
| `SYS_BBS_BRD` | `CONTENT` | `SYS_BBS_PST` | `CN` |
| `SYS_BBS_BRD` | `HIT_CNT` | `SYS_BBS_PST` | `INQ_CNT` |
| `SYS_BBS_BRD` | `THMBNL` | `SYS_BBS_PST` | `THMB_PATH` |
| `SYS_BBS_RPLY` | `REPLY_ID` | `SYS_BBS_CMNT` | `CMNT_ID` |
| `SYS_BBS_RPLY` | `BOARD_ID` | `SYS_BBS_CMNT` | `PST_ID` |
| `SYS_BBS_RPLY` | `PARENT_REPLY_ID` | `SYS_BBS_CMNT` | `PRNT_CMNT_ID` |
| `SYS_BBS_RPLY` | `CONTENT` | `SYS_BBS_CMNT` | `CN` |

### 로그

| before 테이블 | before 컬럼 | after 테이블 | after 컬럼 |
|---|---|---|---|
| `SYS_ACS_LOG` | `SYS_LOGIN_DT` | `SYS_ACS_LOG` | `SYS_LGN_DT` |
| `SYS_ACS_LOG` | `SYS_LOGOUT_DT` | `SYS_ACS_LOG` | `SYS_LGT_DT` |
| `SYS_ACS_LOG` | `DEV_TYPE` | `SYS_ACS_LOG` | `ISTR_TYPE` |
| `SYS_ACS_LOG` | `EMAIL` | `SYS_ACS_LOG` | 삭제, `UP_GRP_ID` 추가 |
| `SYS_EVT_LOG` | `TGT_TBL` | `SYS_ADT_LOG` | `TRGT_TBL_NM` |
| `SYS_EVT_LOG` | `TGT_ID` | `SYS_ADT_LOG` | `TRGT_ID` |
| `SYS_EVT_LOG` | `BFR_DATA` | `SYS_ADT_LOG` | `CHG_PREV_DATA` |
| `SYS_EVT_LOG` | `AFT_DATA` | `SYS_ADT_LOG` | `CHG_AFTR_DATA` |
| `SYS_EVT_LOG` | `EMAIL` | `SYS_ADT_LOG` | 삭제, `UP_GRP_ID` 추가 |
| `SYS_EVT_LOG` | `IP_ADDR` | `SYS_ADT_LOG` | 삭제 |
| `SYS_EVT_LOG` | `SYS_CRT_DT` | `SYS_ADT_LOG` | 삭제 |

`SYS_ADT_LOG`는 PK가 `ID` 단일 컬럼이다. 기존 `ID + SYS_CRT_DT` 기준 코드가 있으면 반드시 수정한다.

### 기관

| before 테이블 | before 컬럼 | after 테이블 | after 컬럼 |
|---|---|---|---|
| `SYS_OFFC` | `OFFC_CD` | `SYS_INST` | `INST_CD` |
| `SYS_OFFC` | `TOP_OFFC_CD` | `SYS_INST` | `UP_INST_CD` |
| `SYS_OFFC` | `OFFC_NM` | `SYS_INST` | `INST_NM` |
| `SYS_OFFC` | `OFFC_TYPE` | `SYS_INST` | `INST_SE_CD` |
| `SYS_OFFC` | `OFFC_TYPE_CD` | `SYS_INST` | `INST_TYPE_CD` |

### 사용자

| before | after |
|---|---|
| `EMAIL` | `EML` |
| `PWD` | `PSWD` |
| `OFFC_CD` | `INST_CD` |
| `USER_NM` | `FNM` |
| `TEL` | `TELNO` |

after에서 추가된 사용자 컬럼:

| 컬럼 | 의미 |
|---|---|
| `FAM_NM` | 가족명 |
| `LNM` | 성 |
| `TRSF_LGN_DT` | 이전로그인일시 |
| `RCNT_LGN_DT` | 최근로그인일시 |
| `PSWD_EXPRY_DT` | 비밀번호만료일시 |

### 사용자 그룹

| before | after |
|---|---|
| `OFFC_CD` | `INST_CD` |
| `USER_EMAIL` | 삭제 |
| 신규 | `USER_UP_GRP_ID` |

`SYS_USER_GRP.ID`는 `bigint`다. Java 타입은 `Long`으로 맞추는 것을 권장한다.

### 파일

| before | after |
|---|---|
| `FILE_EXT` | `FEXT` |
| `TEMP_YN` | `TMPR_FILE_YN` |

### 토큰

| before | after |
|---|---|
| `EXP` | `EXPRY_YN` |
| `REVOK` | `DSCD_YN` |
| `TOKEN` | `TOKEN_VL` |

## 7. 작업 순서

### 1단계: 전체 검색 결과를 작업 대상과 보류 대상으로 분류

실행:

```bash
rg -n -g '!outputs/**' -g '!.codex-sheet-build/**' -g '!**/target/**' -g '!**/assets/**' 'SYS_BBS_BRD|SYS_BBS_RPLY|SYS_EVT_LOG|SYS_OFFC|BOARD_ID|REPLY_ID|PARENT_REPLY_ID|BBS_MANAGER|FILE_YN|REPLY_YN|SYS_LOGIN_DT|SYS_LOGOUT_DT|DEV_TYPE|OFFC_CD|USER_NM|SYS_UPD_USR_ID|SYS_UPD_DT|SYS_CRT_DT|TOP_OFFC_CD|OFFC_TYPE|OFFC_TYPE_CD|TEMP_YN|FILE_EXT|EXP\b|REVOK\b' vims-management-system vims-login Core-lib FMS vims-web-app
```

분류 기준:

| 분류 | 처리 |
|---|---|
| MyBatis XML | 즉시 수정 |
| Java Entity/DTO/Repository/Service | 즉시 수정 |
| HTML/JS에서 ajax key나 grid column | 즉시 수정 |
| 다국어 메시지 key | 2차 정리 가능 |
| 주석 | 2차 정리 가능 |
| 샘플 페이지 | 앱 영향 없으면 2차 정리 가능 |

### 2단계: Core-lib 로그/토큰 수정

수정 대상:

- `Core-lib/core-lib/src/main/resources/mybatis-core/common/SysEventLogMapper.xml`
- `Core-lib/core-lib/src/main/java/com/system/common/interceptor/EventLogInterceptor.java`
- `Core-lib/core-lib/src/main/java/com/system/common/db/DbPartitionService.java`
- `Core-lib/core-lib/src/main/java/com/system/auth/service/AuthenticationService.java`

작업:

| 항목 | 변경 |
|---|---|
| `SYS_EVT_LOG` | `SYS_ADT_LOG` |
| `TGT_TBL` | `TRGT_TBL_NM` |
| `TGT_ID` | `TRGT_ID` |
| `BFR_DATA` | `CHG_PREV_DATA` |
| `AFT_DATA` | `CHG_AFTR_DATA` |
| `SYS_CRT_DT` 검색 조건 | 새 컬럼 기준으로 제거 또는 대체 |
| `EXP` | `EXPRY_YN` |
| `REVOK` | `DSCD_YN` |
| `TOKEN` | `TOKEN_VL` |

주의:

- `SYS_ADT_LOG`에는 `IP_ADDR`, `EMAIL`, `SYS_CRT_DT`가 없다.
- `DbPartitionService`에서 `SYS_EVT_LOG` 파티션 관리는 더 이상 유효하지 않을 수 있다.
- 파티션 제거 후에도 서비스가 partition DDL을 실행하면 오류가 날 수 있다.

### 3단계: 게시판 수정

수정 대상:

- `vims-management-system/src/main/resources/mybatis/common/SysBbsBoardMapper.xml`
- `vims-management-system/src/main/resources/mybatis/common/SysBbsReplyMapper.xml`
- `vims-management-system/src/main/java/com/vims/common/bbs/SysBbsBoard.java`
- `vims-management-system/src/main/java/com/vims/common/bbs/SysBbsReply.java`
- `vims-management-system/src/main/resources/templates/page/bbs/*.html`

작업:

| before | after |
|---|---|
| `SYS_BBS_BRD` | `SYS_BBS_PST` |
| `SYS_BBS_RPLY` | `SYS_BBS_CMNT` |
| `board_id` | `pst_id` |
| `reply_id` | `cmnt_id` |
| `parent_reply_id` | `prnt_cmnt_id` |
| `title` | `ttl` |
| `content` | `cn` |
| `hit_cnt` | `inq_cnt` |
| `thmbnl` | `thmb_path` |

주의:

- Java 메서드명은 기존 `SELECT_BY_BOARD_ID`처럼 남겨도 컴파일은 되지만, 유지보수상 `SELECT_BY_PST_ID`로 정리하는 것을 권장한다.
- 화면 JS가 `reply.reply_id`, `reply.board_id`를 직접 쓰면 after 응답과 불일치한다.
- API 응답 JSON key를 기존 이름으로 유지하려면 SQL alias를 사용한다.

선택지:

| 방식 | 장점 | 단점 |
|---|---|---|
| 코드 전체를 after 명칭으로 변경 | DB와 코드 일치, 장기적으로 좋음 | 화면/JS 변경량 큼 |
| SQL alias로 before JSON 유지 | 프론트 수정량 감소 | DB와 DTO 명칭 불일치 지속 |

권장:

- 서버 내부 Entity/Mapper는 after 명칭으로 변경
- 외부 화면 영향이 큰 경우 응답 alias는 임시로 유지
- 새 기능부터 after 명칭 사용

### 4단계: 기관/사용자 수정

수정 대상:

- `vims-management-system/src/main/resources/mybatis/common/SysOfficeMapper.xml`
- `vims-management-system/src/main/resources/mybatis/common/SysUserMapper.xml`
- `vims-management-system/src/main/resources/mybatis/common/SysUserGroupMapper.xml`
- `vims-management-system/src/main/resources/templates/page/office/*.html`
- `vims-management-system/src/main/resources/templates/page/user/*.html`

작업:

| before | after |
|---|---|
| `SYS_OFFC` | `SYS_INST` |
| `offc_cd` | `inst_cd` |
| `top_offc_cd` | `up_inst_cd` |
| `offc_nm` | `inst_nm` |
| `offc_type` | `inst_se_cd` |
| `offc_type_cd` | `inst_type_cd` |
| `user_nm` | `fnm` |
| `email` | `eml` |
| `pwd` | `pswd` |
| `tel` | `telno` |

주의:

- 화면에 “office”라는 경로/도메인명을 그대로 둘지 “institution”으로 바꿀지 결정 필요
- DB 컬럼은 after 명칭으로 반드시 바꿔야 한다.
- Java 클래스명 `SysOffice`는 당장 유지해도 되지만, SQL 내부는 `SYS_INST` 기준이어야 한다.

### 5단계: 파일/코드/권한/사이트 설정 수정

수정 대상:

- 파일 Mapper
- 코드 Mapper
- 권한그룹 Mapper
- 사이트 설정 Mapper

주요 변경:

| before | after |
|---|---|
| `FILE_EXT` | `FEXT` |
| `TEMP_YN` | `TMPR_FILE_YN` |
| `ACS_RTS_GRP_ID` | `GRP_ID` |
| `COMMENT` | `EXPLN` |
| `SYS_CRT_DT` | `SYS_GEN_DT` |
| `SYS_UPD_DT` | `SYS_MDFCN_DT` |
| `SYS_UPD_USR_ID` | `SYS_MDFR_ID` |

주의:

- `COMMENT`는 SQL 예약어와 충돌 가능성이 있어서 `EXPLN`으로 바뀐 것이 더 안전하다.
- `SYS_CD.SYS_UPD_USR_ID -> TUN_APRV_YMD`는 업무 의미 확인이 필요하다.

### 6단계: 빌드와 컴파일 오류 수정

권장 실행:

```bash
mvn -pl Core-lib/core-lib -am test
mvn -pl vims-management-system -am test
mvn -pl vims-login -am test
```

프로젝트 구조상 위 명령이 맞지 않으면 각 모듈 폴더에서 실행:

```bash
mvn test
```

확인할 오류:

| 오류 유형 | 의미 |
|---|---|
| cannot find symbol | Java 필드명/메서드명 변경 누락 |
| SQLSyntaxErrorException | MyBatis SQL에 before 컬럼/테이블 잔존 |
| BindingException | Mapper XML과 Java interface 불일치 |
| ResultMap 오류 | result alias와 DTO 필드 불일치 |

### 7단계: 런타임 기능 테스트

최소 수동 테스트:

| 기능 | 확인 |
|---|---|
| 로그인 | 토큰 생성/갱신/폐기 정상 |
| 내 정보 | 사용자 조회, 이름, 이메일, 전화번호 표시 |
| 사용자 관리 | 목록, 상세, 등록, 수정 |
| 사용자 그룹 | 목록, 상세, 등록, 수정 |
| 기관 관리 | 목록, 상세, 등록, 수정 |
| 게시판 마스터 | 목록, 등록, 수정 |
| 게시글 | 목록, 상세, 등록, 수정, 삭제, 조회수 |
| 댓글 | 등록, 목록, 대댓글, 삭제 |
| 파일 | 업로드, 다운로드, 상세 조회 |
| 코드 관리 | 코드그룹, 코드 목록, 저장 |
| 권한 메뉴 | 그룹별 메뉴 권한 조회/저장 |
| 접속 로그 | 목록 조회 |
| 감사 로그 | 목록 조회, 등록 인터셉터 동작 |

## 8. 검증 SQL

DB 상태 확인:

```sql
SELECT *
FROM MVIMS_SCHEMA_MIGRATION_HISTORY
WHERE VERSION = '20260618_BEFORE_TO_AFTER';
```

legacy 테이블 확인:

```sql
SELECT TABLE_NAME
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME IN ('SYS_BBS_BRD', 'SYS_BBS_RPLY', 'SYS_EVT_LOG', 'SYS_OFFC');
```

orphan 리포트 확인:

```sql
SELECT CHECK_NAME, COUNT(*) AS ORPHAN_COUNT
FROM MVIMS_MIGRATION_ORPHAN_REPORT_20260618
GROUP BY CHECK_NAME
ORDER BY CHECK_NAME;
```

자동 매칭 실패 로그 확인:

```sql
SELECT 'SYS_ACS_LOG' AS TABLE_NAME, COUNT(*) AS MIGRATED_UNKNOWN_COUNT
FROM SYS_ACS_LOG
WHERE UP_GRP_ID = 'MIGRATED_UNKNOWN'
UNION ALL
SELECT 'SYS_ADT_LOG', COUNT(*)
FROM SYS_ADT_LOG
WHERE UP_GRP_ID = 'MIGRATED_UNKNOWN';
```

## 9. 완료 기준

1차 완료 기준:

- 앱이 정상 기동된다.
- 로그인 가능하다.
- 주요 MyBatis SQL 오류가 없다.
- `SYS_BBS_BRD`, `SYS_BBS_RPLY`, `SYS_EVT_LOG`, `SYS_OFFC` 참조가 실행 SQL에서 사라졌다.
- 게시판 목록/상세/등록이 동작한다.
- 기관, 사용자, 사용자그룹 목록 조회가 동작한다.

2차 완료 기준:

- 수정/삭제/등록 계열 기능이 동작한다.
- 토큰 폐기/재발급이 동작한다.
- 감사로그 INSERT가 동작한다.
- 파일 업로드/다운로드가 동작한다.
- 코드/권한/사이트 설정 기능이 동작한다.

최종 완료 기준:

- before 컬럼명이 실제 SQL과 DTO에서 사라졌다.
- 다국어 메시지 키와 화면 label까지 after 명칭으로 정리됐다.
- 테스트 또는 수동 검증 기록이 남았다.
- 운영 검증 완료 후 `BAK_*` 테이블 삭제 여부를 결정했다.

## 10. 작업 중 판단 기준

SQL alias를 써도 되는 경우:

- 프론트 화면 수정량이 커서 당장 API 응답 key를 유지해야 할 때
- Java DTO를 한 번에 바꾸기 어렵고, 우선 런타임 오류 제거가 목표일 때

after 명칭으로 코드까지 바꿔야 하는 경우:

- 신규 개발이 계속될 핵심 도메인일 때
- DB 컬럼 의미가 바뀐 경우
- PK/FK와 직접 연결된 필드인 경우
- 저장/수정 요청 파라미터인 경우

보류해도 되는 경우:

- 다국어 메시지 key
- 주석
- 샘플 페이지
- 사용하지 않는 정적 HTML

보류하면 안 되는 경우:

- MyBatis XML
- Repository query
- Entity/DTO 필드
- Controller request/response binding
- 로그인/토큰/권한 관련 SQL

## 11. 권장 커밋 단위

커밋 1:

- Core-lib 로그/토큰 수정
- 감사로그/접속로그 조회 정상화

커밋 2:

- 게시판/댓글 테이블 및 컬럼 수정
- 게시글/댓글 기능 테스트

커밋 3:

- 기관/사용자/사용자그룹 수정
- 로그인 후 사용자 정보 조회 테스트

커밋 4:

- 파일/코드/권한/사이트 설정 수정
- 주요 관리 화면 테스트

커밋 5:

- 다국어 메시지, 샘플, 주석 정리
- 최종 검색 결과 정리

## 12. 최종 검색 체크

작업 후 아래 검색에서 실제 실행 SQL 관련 결과가 없어야 한다.

```bash
rg -n -g '!outputs/**' -g '!.codex-sheet-build/**' -g '!**/target/**' -g '!**/assets/**' 'SYS_BBS_BRD|SYS_BBS_RPLY|SYS_EVT_LOG|SYS_OFFC' vims-management-system vims-login Core-lib FMS vims-web-app
```

컬럼명 잔존 검색:

```bash
rg -n -g '!outputs/**' -g '!.codex-sheet-build/**' -g '!**/target/**' -g '!**/assets/**' 'BOARD_ID|REPLY_ID|PARENT_REPLY_ID|BBS_MANAGER|FILE_YN|REPLY_YN|SYS_LOGIN_DT|SYS_LOGOUT_DT|DEV_TYPE|OFFC_CD|USER_NM|SYS_UPD_USR_ID|SYS_UPD_DT|SYS_CRT_DT|TOP_OFFC_CD|OFFC_TYPE|OFFC_TYPE_CD|TEMP_YN|FILE_EXT|EXP\b|REVOK\b' vims-management-system vims-login Core-lib FMS vims-web-app
```

검색 결과가 남아도 되는 것:

- 마이그레이션 SQL
- 문서
- 다국어 메시지 key
- 주석
- 샘플 화면

검색 결과가 남으면 안 되는 것:

- MyBatis XML의 실제 SQL
- Java Entity/DTO 필드
- Controller request key
- Service business logic
- Repository query

