# 보호자용 라이브 AAC 백엔드

## 구현 범위

- 보호자 최초 튜토리얼 완료 상태와 버전 관리
- 사용자 문장 이해 수준 1~4(기본값 2)
- 아동 남/여, 성인 남/여 TTS 설정과 클라이언트 미리 듣기 계약
- 사용자별 AAC 판, 카테고리, 카드, 즐겨찾기, 순서, 소프트 삭제
- 판 변경 버전과 SSE 실시간 이벤트
- 페어링된 사용자 기기용 90일 opaque access token, heartbeat, 폐기
- 사용자 기기의 판 조회, 카드 사용 기록, 위치, 심박/표정, 긴급 이벤트 전송
- 장소·안심존 CRUD, 현재 위치·이동 이력, 반경 이탈 판정
- 요일·시간대·타임존 기반 루틴 CRUD 및 서버 스케줄 실행
- Web Push 구독정보와 보호자 알림 이력·확인 처리
- 카드 사용 TOP 5, 카테고리 비중, 긴급 횟수, 센서 타임라인 리포트
- PDF 리포트 다운로드
- AAC 사용자 소유권 기반 비공개 이미지 업로드·조회
- 회원 탈퇴(소프트 탈퇴) 및 세션 무효화

## 보호자 API

```text
GET  /api/v1/me/tutorial
POST /api/v1/me/tutorial/complete

GET    /api/v1/me/aac-users/{id}/board
GET    /api/v1/me/aac-users/{id}/events                 # SSE
POST   /api/v1/me/aac-users/{id}/board/categories
PATCH  /api/v1/me/aac-users/{id}/board/categories/{categoryId}
DELETE /api/v1/me/aac-users/{id}/board/categories/{categoryId}
POST   /api/v1/me/aac-users/{id}/board/cards
PATCH  /api/v1/me/aac-users/{id}/board/cards/{cardId}
PATCH  /api/v1/me/aac-users/{id}/board/cards/{cardId}/favorite
DELETE /api/v1/me/aac-users/{id}/board/cards/{cardId}

PATCH /api/v1/me/aac-users/{id}/sentence-level
PATCH /api/v1/me/aac-users/{id}/status
GET   /api/v1/me/aac-users/{id}/voice-preview

GET    /api/v1/me/aac-users/{id}/places
POST   /api/v1/me/aac-users/{id}/places
PUT    /api/v1/me/aac-users/{id}/places/{placeId}
DELETE /api/v1/me/aac-users/{id}/places/{placeId}
GET    /api/v1/me/aac-users/{id}/locations/latest
GET    /api/v1/me/aac-users/{id}/locations?from=&to=

GET    /api/v1/me/aac-users/{id}/routines
POST   /api/v1/me/aac-users/{id}/routines
PUT    /api/v1/me/aac-users/{id}/routines/{routineId}
DELETE /api/v1/me/aac-users/{id}/routines/{routineId}

POST /api/v1/me/push-subscriptions
GET  /api/v1/me/aac-users/{id}/alerts?from=&to=
POST /api/v1/me/aac-users/{id}/alerts/{alertId}/acknowledge

GET /api/v1/me/aac-users/{id}/report?from=&to=
GET /api/v1/me/aac-users/{id}/report/pdf?from=&to=

POST /api/v1/me/aac-users/{id}/media/images
GET  /api/v1/me/aac-users/{id}/media/images/{name}
DELETE /api/v1/auth/me
```

보호자 변경 요청에는 기존과 동일하게 세션 쿠키, `credentials: include`, CSRF 토큰과 `X-XSRF-TOKEN` 헤더가 필요합니다.

## 사용자 기기 API

페어링 claim 응답의 `accessToken`을 안전한 기기 저장소에 저장하고 다음처럼 전송합니다.

```http
Authorization: Bearer {accessToken}
```

```text
POST /api/v1/device/heartbeat
GET  /api/v1/device/board
GET  /api/v1/device/events                  # SSE
POST /api/v1/device/card-usage
POST /api/v1/device/locations
POST /api/v1/device/sensor-events
POST /api/v1/device/emergency
```

보호자는 `DELETE /api/v1/me/aac-users/{id}/devices/{deviceDbId}`로 기기와 토큰을 폐기합니다.

## 실시간 이벤트

SSE event 이름은 `CONNECTED`, `BOARD_UPDATED`, `SETTINGS_UPDATED`, `STATUS_UPDATED`, `CARD_USED`, `LOCATION_UPDATED`, `SENSOR_EVENT`, `ROUTINE_TRIGGERED`, `ALERT`입니다. 재연결 후에는 항상 `/board`를 다시 읽어 `version` 기준으로 화면을 맞춥니다.

## 운영 연동 경계

- 지도 렌더링과 주소 검색은 프론트의 Map API가 담당하며 백엔드는 좌표·주소·안심존을 저장합니다.
- 현재 이미지 저장소는 인증된 로컬 파일 저장소입니다. 운영에서는 `MediaStorageService`를 S3/Cloud Storage 구현으로 교체합니다.
- 음성 미리 듣기는 현재 `CLIENT_TTS` 계약입니다. 서버 생성 음성이 필요하면 TTS 공급자 어댑터를 추가합니다.
- Web Push 구독과 알림 이력은 저장되지만 실제 VAPID/FCM/APNs 발송 어댑터와 키는 배포 환경에서 연결해야 합니다.
- PWA 위치 전송은 앱이 실행 중일 때를 기준으로 합니다. 앱 종료 후 지속 추적이 필수라면 네이티브/Capacitor 또는 IoT 기기 구현이 필요합니다.
- 위치·웹캠 표정·심박은 민감정보이므로 운영 전 명시적 동의, 최소 보관기간, 삭제·열람 정책을 확정해야 합니다.

## 검증

- `mvnw test`: 15 tests 성공
- H2에서 Flyway V1~V6 적용 및 Hibernate validate 성공
- 통합 테스트가 튜토리얼 → 사용자 설정 → 판 초기화/즐겨찾기 → 페어링/기기 토큰 → 카드 사용 → 위치/심박/긴급 → 리포트 조회 흐름을 검증합니다.
