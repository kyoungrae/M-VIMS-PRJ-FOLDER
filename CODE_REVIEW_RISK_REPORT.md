# M-VIMS 코드 리뷰 — 위험 분석 및 대응 방안

> 작성일: 2026-06-01
> 대상: vims-gateway · vims-login · vims-management-system · FMS · Core-lib
> 아키텍처: Spring Cloud Gateway 기반 분기, 전 모듈이 Core-lib 상속

---

## 요약

| # | 항목 | 심각도 | 분류 | 영향 모듈 |
|---|------|:------:|------|-----------|
| 1 | 시크릿 하드코딩 + Git 커밋 | 🔴 Critical | 보안 | Gateway, FMS, Core-lib |
| 2 | 백엔드 직접 접근 시 인증 우회 | 🔴 Critical | 보안 | Gateway, FMS, Login |
| 3 | 게이트웨이 화이트리스트 우회 로직 | 🔴 Critical | 보안 | Gateway |
| 4 | IDOR — 파일 다운로드 권한 미검증 | 🔴 Critical | 보안 | FMS |
| 5 | CORS 전체 허용 + 인증정보 허용 | 🔴 Critical | 보안 | Gateway |
| 6 | RestTemplate 타임아웃 없음 | 🟠 High | 과부하 | Core-lib(전 모듈) |
| 7 | 감사 로그 인터셉터 DB 부하 증폭 | 🟠 High | 과부하 | Core-lib(전 모듈) |
| 8 | 파일/엑셀 처리 메모리 위험 | 🟠 High | 과부하 | FMS |
| 9 | 경로 조작(Path Traversal) | 🟠 High | 보안 | FMS |
| 10 | 포트·라우팅 불일치 | 🟡 Medium | 구조 | Gateway, FMS, Mgmt |
| 11 | 인증 필터 클래스 중복 | 🟡 Medium | 구조 | Gateway |
| 12 | 예외/로깅 위생 | 🟡 Medium | 유지보수 | 전 모듈 |
| 13 | 토큰 재사용 정책 | 🟡 Medium | 보안 | Core-lib |

**가장 시급한 조치:** #1, #2 (운영 배포 전 필수 — 시크릿 로테이션 + 백엔드 포트 차단)

---

## 🔴 Critical — 보안 (즉시 조치)

### 1. 시크릿이 소스에 하드코딩 + Git 커밋됨

**위치**
- `vims-gateway/src/main/resources/application.yml` — `token.secret`, `gateway.api-key: vims-internal-secret-key-1234`
- `FMS/src/main/resources/application.yml` — DB 패스워드 `1234`, `secret-key`(JWT 키)
- 모든 서비스가 **동일한 JWT 키**를 공유

**위험**
- 저장소 접근만으로 JWT 위조 및 시스템 침투 가능
- `vims-internal-secret-key-1234`를 아는 사람은 누구나 `X-API-KEY` 헤더로 **SYSTEM_ADMIN** 권한 획득
- DB 패스워드가 평문으로 노출

**대응 방안**
- 노출된 키·패스워드 **즉시 로테이션**
- 시크릿을 환경변수 / Vault / `application-{profile}.yml`(`.gitignore` 처리)로 분리
- git 히스토리에서 시크릿 제거 (`git filter-repo` 또는 BFG)
- API-Key는 SYSTEM_ADMIN 자동 부여 대신, 화이트리스트된 내부 IP + mTLS로 제한

---

### 2. 백엔드 직접 접근 시 인증 완전 우회 가능

**위치**
- `vims-login/.../filter/DirectAccessFilter.java`
- `FMS/.../config/SecurityConfig.java`
- `FMS/.../file/FileManagerController.java`

**문제 흐름**
1. `DirectAccessFilter`는 `X-Forwarded-Port` / `X-Forwarded-Host` 헤더가 있으면 **무조건 통과**시킴 → 이 헤더는 클라이언트가 임의로 위조 가능
2. `FMS/SecurityConfig`는 `anyRequest().permitAll()`
3. `FileManagerController`는 "Controller에서 API-Key로 검증"이라는 주석과 달리 **실제 검증 미구현**

**위험**
- 백엔드 포트(예: 8082)에 도달 가능한 사람이 위조 헤더만 붙이면 게이트웨이를 우회해 **인증 없이 모든 파일 업로드/다운로드** 가능

**대응 방안**
- 백엔드 포트를 방화벽/네트워크 정책으로 **게이트웨이에서만 접근 가능**하게 차단
- `X-Forwarded-*` 헤더는 신뢰 프록시에서만 인정 (Spring `ForwardedHeaderFilter` + 신뢰 IP 설정)
- FMS가 게이트웨이 주입 서명/내부키를 실제로 검증하도록 구현

---

### 3. 게이트웨이 화이트리스트 우회를 부르는 인증 로직

**위치** `vims-gateway/.../filter/AuthorizationHeaderFilter.java`

**문제**
- public 판정이 `path.endsWith(".html" / ".js" / ".css" ...)` 기반
  → `/cms/secret/data.json?x=.html` 같은 확장자 트릭, `..` 조합으로 보호 자원 노출 여지
- `startsWith("Bearer")` 후 `replace("Bearer", "")` 사용
  → 토큰 내부 어디든 "Bearer" 문자열을 전부 제거함. prefix만 제거하려면 `substring(7)` 사용해야 함

**대응 방안**
- 화이트리스트를 **라우트 단위**(정적 라우트에만 인증 제외)로 처리, 확장자 기반 판정 제거
- Bearer 파싱은 prefix 검사 후 `substring(7)`

---

### 4. IDOR — 파일 다운로드 권한 검증 없음

**위치** `FMS/.../file/FileManagerService.java` — `fileDownload`, `fileDownloadByUuid`

**문제**
- `/fms/fileManager/download?fileId=...` 와 `downloadByUuid`가 요청자의 파일 소유권/접근권을 검증하지 않음
- UUID만 알면(또는 추측하면) 타인 파일 다운로드 가능

**대응 방안**
- 다운로드 시 `X-User-Id` / role 기반 소유권·접근권 검증 추가

---

### 5. CORS 전체 허용 + 인증정보 허용

**위치** `vims-gateway/.../config/CorsConfig.java`

**문제**
- `allowedOriginPattern("*")` + `allowCredentials(true)` 조합 (주석에 "운영 시 제한 필요"라 적혀 있으나 미반영)

**대응 방안**
- 허용 Origin을 실제 서비스 도메인으로 화이트리스트

---

## 🟠 High — 과부하·성능

### 6. RestTemplate 타임아웃 없음 (전 모듈 영향)

**위치** `Core-lib/.../common/config/ApplicationConfig.java`

**문제**
- `RestTemplate`에 connect/read 타임아웃 미설정 = 무한 대기
- 한 다운스트림(FMS 등)이 느려지면 호출 스레드가 묶여 **연쇄 장애(cascading failure)**로 확산

**대응 방안**
- connect/read 타임아웃 설정 (예: 3s / 10s)
- 가능하면 `RestClient` / `WebClient` + 서킷브레이커(Resilience4j) 도입

---

### 7. 감사 로그 인터셉터의 DB 부하 증폭

**위치** `Core-lib/.../common/interceptor/EventLogInterceptor.java`

**문제**
- 모든 UPDATE/DELETE 전에 **동기적으로 추가 SELECT**(`captureBeforeData`) 실행 → 쓰기 트래픽마다 DB 쿼리 2배
- 비동기 로그 writer(최대 5스레드)가 매 로그마다 `sqlSessionFactory.openSession()`으로 **Hikari 풀(최대 10)에서 커넥션을 별도 점유** → 쓰기 폭주 시 요청 스레드와 커넥션 경합(starvation)
- (큐·드롭 정책 자체는 잘 설계되어 있으나 커넥션 경합 문제는 남음)

**대응 방안**
- before-image 캡처를 꼭 필요한 테이블로 한정하거나 DB 트리거/CDC로 이전
- 로그 writer용 커넥션을 **별도 데이터소스**로 분리하거나 배치 insert로 묶기

---

### 8. 파일/엑셀 처리 메모리 위험

**위치** `FMS/.../file/FileProcessManager.java`, `FMS/.../excel/ExcelUploadService.java`

**문제**
- 업로드 한도 200MB인데 `uploadFile`은 `MAX_FILE_SZ` 검증을 하지 않음 (검증 코드가 주석 처리됨)
- `downloadFile`의 `setContentLength((int) file.length())` → 2GB 초과 시 int 오버플로 (현재 한도상 안전하나 잠재 버그)
- `ExcelUploadService`는 `WorkbookFactory.create`로 전체 워크북을 메모리 적재 후 `List<Map>` 전부 보관 → 큰 xlsx에서 OOM, 동시 업로드 시 위험 가중

**대응 방안**
- 실제 파일 크기·확장자·MIME 화이트리스트 검증 복원
- 대용량 xlsx는 SAX 기반 스트리밍 파싱(POI `XSSFReader` / EventModel)
- 다운로드는 `Content-Length`를 long 헤더로 설정

---

### 9. 경로 조작(Path Traversal) 가능성

**위치** `FMS/.../file/FileProcessManager.java` — `uploadFile`

**문제**
- 업로드 `folder_name` 파라미터가 검증 없이 디렉터리 경로에 그대로 연결됨
- `../../` 주입 시 베이스 디렉터리 밖에 파일 쓰기 가능

**대응 방안**
- `folder_name` 정규화 후 베이스 경로 prefix 검증, `..` / 절대경로 거부

---

## 🟡 Medium — 구조·유지보수

### 10. 포트·라우팅 불일치

**위치** `vims-gateway/application.yml`, `FMS/application.yml`, `FMS/application-podman.yml`

**문제**
- FMS `application.yml`은 **8083**, `application-podman.yml`은 **8082**인데, 게이트웨이는 FMS를 **8082**, management를 **8083**으로 라우팅
  → podman 프로필이 아니면 FMS(8083)와 management(8083) 포트 충돌 + 게이트웨이→FMS 라우팅 깨짐
- 게이트웨이 `/api/v1/auth/**` predicate가 **login 라우트와 fms 라우트에 중복** → 라우팅 모호
- management 라우트 주석에 "포트 누락" 메모가 그대로 남음

**대응 방안**
- 포트 정책을 프로필별로 일관되게 정리
- 중복 predicate 제거

---

### 11. 인증 필터 클래스 중복

**위치** `AuthorizationHeaderFilter.java`, `AuthorizationHeaderGatewayFilterFactory.java`

**문제**
- 거의 동일한 두 필터가 모두 빈 등록됨. 한쪽만 `/web` 우회를 포함 → 혼란·유지보수 위험

**대응 방안**
- 하나로 통합하고 미사용 클래스 제거

---

### 12. 예외/로깅 위생

**위치** 전 모듈

**문제**
- `printStackTrace` · `System.out/err` 다수 (Core-lib 43건, FMS 35건, management 30건) → 운영 로그에 안 남고 표준출력으로 샘
- `ExcelUploadService`는 `getMessage("")`로 빈 코드 조회 → 원본 예외를 가리는 2차 예외 발생
- `JwtService` 생성자의 `expiration-milliseconds`가 `int` → 큰 값 설정 시 오버플로 (현재 10시간이라 안전하나 `long` 권장)
- `SelectResultSqlAop`는 `com..*Service.*` 전체를 감싸면서 `proceed()`만 하는 빈 Around → 모든 서비스 호출에 불필요한 프록시 오버헤드

**대응 방안**
- SLF4J 로깅으로 통일, 표준출력 제거
- 빈 AOP 제거, `expiration-milliseconds`를 `long`으로

---

### 13. 토큰 재사용 정책

**위치** `Core-lib/.../auth/service/AuthenticationService.java` — `authenticate`

**문제**
- 로그인 시 기존 유효 토큰을 그대로 재발급 → 만료 전까지 동일 JWT 재사용, 탈취 시 무효화 어려움

**대응 방안**
- 짧은 수명의 access 토큰 + refresh 토큰 구조로 전환

---

## 권장 조치 순서

1. **즉시 (배포 차단 사유)** — #1 시크릿 로테이션·분리, #2 백엔드 포트 차단
2. **1차 스프린트** — #3 화이트리스트/Bearer 파싱, #4 IDOR, #5 CORS, #9 Path Traversal
3. **2차 스프린트** — #6 타임아웃, #7 감사로그 커넥션 분리, #8 파일/엑셀 스트리밍
4. **정리 작업** — #10 포트 정리, #11 필터 통합, #12 로깅 위생, #13 토큰 구조
