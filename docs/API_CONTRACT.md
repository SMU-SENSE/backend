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

## Figma 온보딩 매핑

| 화면 | API | 핵심 계약 |
|---|---|---|
| 09 사용자 프로필 | `POST /api/v1/me/aac-users` | `name`, `birthDate`, `relationshipType`, `emergencyContact` 필수. `OTHER`이면 `relationshipDetail` 필수 |
| 10 화면 격자 | `PATCH /api/v1/me/aac-users/{id}/onboarding/grid` | `GRID_2X2`, `GRID_3X3`, `GRID_4X4` |
| 11 TTS 음성 | `PATCH /api/v1/me/aac-users/{id}/voice-settings` | `CHILD_MALE`/`CHILD_FEMALE`, 속도 0.7~1.3 |
| 12 가입정보 확인 | `GET /api/v1/me/aac-users/{id}/onboarding-summary` | 프로필·현재 보호자 관계·격자·음성을 한 번에 반환 |
| 12 확정 | `POST /api/v1/me/aac-users/{id}/onboarding/confirm` | 격자와 음성 완료 후 `CONFIRMED` 전환 |

프로필 생성 예시:

```json
{"name":"민수","birthDate":"2012-01-15","relationshipType":"PARENT","relationshipDetail":null,"emergencyContact":"01012345678","notes":"큰 소리에 민감함","profileImageUrl":null}
```

초기값은 `mode=SIMPLE`, `gridSize=GRID_3X3`, `voiceType=CHILD_MALE`, `speechRate=1.0`입니다. 진행 상태는 `PROFILE_COMPLETED → GRID_COMPLETED → VOICE_COMPLETED → CONFIRMED`입니다. 관계는 보호자별로 달라질 수 있어 `user_guardians`에 저장합니다.

## Device Pairing

| 화면 | API | 설명 |
|---|---|---|
| 06 QR 연결 / 07 코드 | `POST /api/v1/me/aac-users/{id}/device-pairings` | QR payload와 6자리 코드를 함께 발급, 10분 유효 |
| 남은 시간 | `GET /api/v1/me/aac-users/{id}/device-pairings/current` | 자격 증명은 재노출하지 않고 만료시각/초만 반환 |
| 새로고침 | `POST /api/v1/me/aac-users/{id}/device-pairings/refresh` | 이전 ACTIVE 세션 즉시 REVOKED |
| 사용자 기기 QR | `POST /api/v1/device-pairings/claim/qr` | 공개 endpoint, opaque token이 인증 수단 |
| 사용자 기기 코드 | `POST /api/v1/device-pairings/claim/code` | 공개 endpoint, 정확히 6자리 숫자 |
| 연결 기기 | `GET /api/v1/me/aac-users/{id}/devices` | 현재 보호자가 접근 가능한 사용자의 기기 목록 |
| 06-B / 07-B | claim 응답 `410 Gone` | 만료 세션을 없는 자격 증명(404)과 구분 |

claim은 `deviceId`, 선택 `deviceName`, `deviceType`(`TABLET`, `MOBILE`, `WEB`, `UNKNOWN`)을 받습니다. 성공 응답은 `aacUserId`, `deviceId`, `pairedAt`만 반환합니다. 사용됨/취소됨은 409, 만료는 410, 미존재는 404, 형식 오류는 400입니다. claim 두 경로만 인증과 CSRF 예외이며, 발급·조회·기기 목록에는 기존 세션 인증과 AAC 사용자 소유권 검사가 적용됩니다.

## 보호자 알림 (긴급 상징 알람)

| API | 설명 |
|---|---|
| `GET /api/v1/me/notifications` | 로그인한 보호자에게 온 알림 목록, 최신순 |
| `PATCH /api/v1/me/notifications/{id}/read` | 알림 확인 처리. 본인 알림이 아니면 403 |
| `POST /api/v1/me/push-token` | 보호자 계정에 FCM 디바이스 토큰 등록(1개, 최신 값으로 교체) |

`POST .../usage-logs`에 `action=SPEAK`이고 상징이 `emergency=true`이면, 해당 AAC 사용자와 연결된 모든 보호자에게 `guardian_notifications` 행이 생성됩니다. 등록된 `pushToken`이 있으면 FCM 푸시도 best-effort로 함께 발송됩니다(발송 실패는 무시하고 DB 알림은 항상 남습니다). FCM은 `FIREBASE_CREDENTIALS_PATH` 환경변수(서비스 계정 JSON 경로)가 설정된 경우에만 동작하며, 미설정 시 인앱 알림만 저장됩니다.