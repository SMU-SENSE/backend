# 2026-08-16 진행 기록 — 보호자 긴급 알림 기능

## 배경

"보호자에게 알람 전송을 띄울 수 있는지 확인해줘" 요청으로 시작. 먼저 코드베이스 전체를 훑어 실현 가능성을 확인하고, 사용자가 "인앱 알림 + 실제 FCM 푸시 둘 다" 원한다고 답해서 두 채널 모두 구현했다.

## 1. 조사 단계 (구현 전 확인한 것)

- `grep -i "notif|alarm|push|fcm"` → 코드베이스에 알림 관련 코드 0건. `docs/BACKEND_PROGRESS.md`에도 "긴급 알림"이 다음 phase로 명시돼 있었음.
- 이미 있던 재료:
  - `Symbol.emergency` 플래그 + "긴급어" 카테고리(도와주세요/아파요/시끄러워요/쉬고싶어요) — `DataInitializer`가 시드
  - `SymbolUsageLog` — AAC 사용자가 상징을 `SELECT/CANCEL/SPEAK`할 때마다 기록 (`AacUsageService.createUsageLog`)
  - `UserGuardian` — AacUser 1명에 보호자 여러 명 연결 가능
  - `Guardian.email/phone` 필드는 있었지만 푸시 토큰 저장 컬럼은 없었음
- 없던 것: 발송 채널 자체. `pom.xml`에 firebase-admin, spring-boot-starter-mail 등 발송 관련 의존성 전무.

결론: 트리거(긴급 상징 + SPEAK)와 수신자 조회(Guardian↔AacUser 링크)는 이미 있으니, 알림 저장/발송 계층만 새로 얹으면 됨 → 확장 가능 판단.

## 2. 구현

### 트리거 지점
`AacUsageService.createUsageLog` — 사용 기록 저장 직후, `action == SPEAK && symbol.isEmergency()`일 때만
`NotificationService.notifyEmergencySymbolUsed(user, symbol)` 호출.

### 신규 모듈: `com.aac.ieojwo.notification`
기존 패키지 컨벤션(`domain/repository/dto/service/controller`)을 그대로 따름.

| 파일 | 역할 |
|---|---|
| `domain/GuardianNotification.java` | 알림 엔티티. guardian/aacUser/symbol(nullable) FK + message + read 플래그. `emergencySymbolUsed(...)` 팩토리로 메시지 문구까지 생성 |
| `repository/GuardianNotificationRepository.java` | `findAllByGuardianIdOrderByCreatedAtDesc` |
| `dto/NotificationResponse.java` | id/aacUserId/aacUserName/message/read/createdAt |
| `dto/RegisterPushTokenRequest.java` | `token` (`@NotBlank`) |
| `service/NotificationService.java` | ① `notifyEmergencySymbolUsed` — `UserGuardian` 전원 순회, 알림 row 저장 + `FcmPushSender` 호출 ② `findMyNotifications` ③ `markRead` (본인 알림 아니면 403) ④ `registerPushToken` |
| `service/FcmPushSender.java` | FCM 발송 래퍼. `app.fcm.credentials-path`(`FIREBASE_CREDENTIALS_PATH`) 미설정 시 `@PostConstruct`에서 조용히 비활성화. 발송 실패는 로그만 남기고 예외를 던지지 않음(알림 row 저장은 이미 끝난 뒤라 항상 보존됨) |
| `controller/NotificationController.java` | `GET /api/v1/me/notifications`, `PATCH /api/v1/me/notifications/{id}/read`, `POST /api/v1/me/push-token`. 인증/소유권은 기존 `GuardianAccessService.requireCurrentGuardian` 패턴 재사용 |

### 기존 파일 수정
- `Guardian.java` — `pushToken`(길이 255) 컬럼 + `updatePushToken()` 추가. 기기 1대분만 저장(최신 값으로 덮어씀).
- `AacUsageService.java` — `NotificationService` 주입, `createUsageLog`에 트리거 3줄 추가.
- `pom.xml` — `com.google.firebase:firebase-admin:9.4.3` 추가.
- `application.yml` — `app.fcm.credentials-path: ${FIREBASE_CREDENTIALS_PATH:}` 추가.
- `.env.example` — `FIREBASE_CREDENTIALS_PATH=` 안내 주석과 함께 추가.

### DB 마이그레이션
`src/main/resources/db/migration/V6__add_guardian_notifications.sql`
- `guardians.push_token` 컬럼 추가
- `guardian_notifications` 테이블 신설 (guardian_id/aac_user_id FK, symbol_id nullable FK, message, read, created_at/updated_at) + `(guardian_id, created_at)` 인덱스

### 테스트
`OnboardingAndPairingIntegrationTests`에 `emergencySpeakNotifiesGuardianButOtherActionsDoNot` 추가:
- 비-긴급 상징 SPEAK → 알림 0건
- 긴급 상징 SELECT(발화 아님) → 알림 0건
- 긴급 상징 SPEAK → 알림 1건 생성 확인
- `GET /notifications` 응답의 `read=false`, `aacUserId` 확인
- `PATCH /notifications/{id}/read` 후 `isRead()==true` 확인
- `POST /push-token` 200 확인

### 문서 갱신
- `docs/API_CONTRACT.md` — "보호자 알림 (긴급 상징 알람)" 섹션 추가 (엔드포인트 표 + 발동 조건 + FCM 미설정 시 동작 설명)
- `CLAUDE.md` — 신규 모듈 아키텍처 설명 추가, "out of scope" 목록에서 FCM 제거(이제 일부 구현됨), JDK 21+ 요구사항/이 머신에서의 우회 방법 기록

## 3. 빌드/검증

- 이 머신 PATH의 `java`는 17인데 `pom.xml`이 Java 21을 요구 → `C:\jdk-22.0.2`를 `JAVA_HOME`으로 지정해 컴파일/테스트 수행 (`JAVA_HOME="C:\jdk-22.0.2" ./mvnw.cmd test`).
- `mvnw compile` 성공 (firebase-admin 및 전이 의존성 다운로드 포함).
- `mvnw test` 결과: **15개 테스트 전부 통과** (`ApiIntegrationTests` 8, `FlywayMigrationTests` 1, `MalmoaBackendApplicationTests` 1, `OnboardingAndPairingIntegrationTests` 5 — 신규 테스트 포함).
- `FlywayMigrationTests`에서 V1~V6 마이그레이션 전부 정상 적용 확인.
- Postgres 프로필(`-Ppostgres-it`)은 Docker 필요 — 이번엔 미실행. 다음에 실기동 검증 필요.

## 4. 알려진 제한 / 다음에 할 일

- **실제 FCM 미검증**: firebase-admin 연동 코드는 작성했지만, 진짜 Firebase 프로젝트 서비스 계정 JSON이 없어 실기기 푸시 발송은 테스트하지 못함. `FIREBASE_CREDENTIALS_PATH`를 실제 키로 채우고 브라우저/앱에서 토큰 등록 후 검증 필요.
- **보호자 1인 1토큰**: 여러 기기(폰+태블릿 등)로 로그인하는 보호자는 마지막에 등록한 기기만 알림을 받음. 다중 디바이스 지원이 필요해지면 `guardian_push_tokens` 별도 테이블로 확장.
- **알림 타입 단일종**: 현재는 "긴급 상징 SPEAK" 한 종류뿐. 알림 종류가 늘어나면 `GuardianNotification`에 `type` 컬럼 추가 검토.
- **postgres-it 미실행**: Docker 환경에서 V6 마이그레이션과 FK 제약을 별도로 재검증할 것.
