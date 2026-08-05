# Backend API Contract

## 인증

Google OIDC 로그인 후 서버 세션의 `JSESSIONID` 쿠키를 사용합니다. 프론트 요청은 항상 `credentials: "include"`를 지정합니다. 미인증 보호 API는 JSON 401, 연결되지 않은 AAC 사용자 접근은 403입니다.

변경 요청은 먼저 `GET /api/v1/auth/csrf`를 호출하고 `XSRF-TOKEN` 값을 `X-XSRF-TOKEN` 헤더로 전달합니다.

## 공통 응답

성공:

```json
{"success":true,"data":{},"message":null}
```

실패:

```json
{"success":false,"code":"FORBIDDEN","message":"해당 AAC 사용자를 관리할 권한이 없습니다.","fieldErrors":{},"timestamp":"..."}
```

상태 코드는 400 입력/약관, 401 미인증, 403 권한 없음/CSRF, 404 리소스 없음, 409 중복/완료된 온보딩, 500 예상하지 못한 오류로 구분합니다.

## 주요 API

```text
GET  /api/v1/auth/csrf
GET  /api/v1/auth/me
POST /api/v1/auth/onboarding
POST /api/v1/auth/logout

GET   /api/v1/me/aac-users
POST  /api/v1/me/aac-users
GET   /api/v1/me/aac-users/{aacUserId}
PATCH /api/v1/me/aac-users/{aacUserId}/settings

GET    /api/v1/me/aac-users/{aacUserId}/favorites
POST   /api/v1/me/aac-users/{aacUserId}/favorites
DELETE /api/v1/me/aac-users/{aacUserId}/favorites/{symbolId}

POST /api/v1/me/aac-users/{aacUserId}/usage-logs
GET  /api/v1/me/aac-users/{aacUserId}/recent-symbols
GET  /api/v1/me/aac-users/{aacUserId}/guardians
POST /api/v1/me/aac-users/{aacUserId}/guardians
```

AAC 사용자 생성 요청:

```json
{"name":"박지현","mode":"GENERAL","gridSize":"GRID_3X3"}
```

생성한 Account의 Guardian이 PRIMARY 주 보호자로 자동 연결됩니다. 목록은 현재 Guardian과 연결된 사용자만 반환합니다.

## 기존 API 정책

기존 `/api/v1/users/**` 경로는 하위 호환을 위해 유지합니다. 삭제하거나 관리자 API로 바꾸지 않았고 새 API와 같은 소유권 검사를 적용합니다. 신규 프론트 구현은 `/api/v1/me/aac-users/**`를 사용합니다.

전역 `/api/v1/guardians` 생성/목록과 상징 쓰기 API는 현재 인증 사용자에게 열려 있는 기존 MVP 계약입니다. 운영 전 관리자 역할 분리가 필요합니다.

## 프론트 작업

- 모든 요청에 `credentials: "include"` 적용
- 앱 시작 또는 변경 요청 전에 CSRF 토큰 확보
- POST/PATCH/DELETE에 `X-XSRF-TOKEN` 헤더 적용
- 로그인 콜백 후 `/api/v1/auth/me` 재조회
- 401은 로그인 이동, 403은 권한/CSRF 오류로 구분
- AAC 사용자 화면을 권장 `/api/v1/me/aac-users/**` 경로로 전환
