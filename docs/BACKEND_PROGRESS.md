# Backend Progress

## 완료

- Google OIDC `sub` 기반 계정 생성/재로그인
- 필수 약관 검증, 반복 온보딩 409, Account-Guardian 1:1 연결
- AAC 사용자 생성 시 PRIMARY Guardian 자동 연결
- 사용자 상세/설정/보호자/즐겨찾기/사용기록 소유권 검사
- 권장 `/api/v1/me/aac-users/**` API와 기존 API 호환
- JSON 401/403/404/409, 쿠키 CSRF, 제한된 CORS
- Swagger/OpenAPI, PostgreSQL Flyway V1-V4
- Docker Compose PostgreSQL 개발 환경과 PowerShell 실행 스크립트
- H2 기본 테스트와 별도 Testcontainers PostgreSQL 통합 테스트

## 실제 검증

- `postgres:16.14-alpine` Compose 컨테이너 healthcheck 성공
- 빈 실제 PostgreSQL 16.14에서 Flyway V1-V4 순차 적용 성공
- 실제 PostgreSQL에서 Hibernate `ddl-auto=validate`와 Spring Context 로딩 성공
- PostgreSQL IT 1개에서 Account/OAuth/Guardian/AAC 사용자/연결/상징/즐겨찾기/사용기록 CRUD 성공
- OAuth subject, Account-Guardian, User-Guardian, 즐겨찾기 unique 제약 및 FK 실패 검증 성공
- 사용기록 `SELECT`, `CANCEL`, `SPEAK` 문자열 저장 검증 성공
- Health, OpenAPI, Swagger UI 각각 HTTP 200
- DB와 서버 재시작 뒤 Flyway v4 및 스모크 마커 데이터 유지 확인
- 기본 H2 테스트는 Docker와 분리해 유지

## 미검증

- 실제 Google 계정 브라우저 로그인
- 실제 프론트엔드 CORS/쿠키/CSRF 통합
- 운영 HTTPS Secure 쿠키와 운영 배포 환경
- 장시간 부하, 장애 복구, 백업/복원

## 알려진 제한

- 전역 Guardian/상징 쓰기 API는 별도 관리자 권한이 아직 없습니다.
- 한 AAC 사용자에 두 번째 PRIMARY Guardian을 막는 제약은 아직 없습니다.
- FK 삭제 정책은 모두 `NO ACTION`이며 명시적 삭제 흐름은 별도 검증이 필요합니다.
- OneDrive 파일 잠금으로 Maven `clean`이 간헐적으로 `target` 삭제에 실패할 수 있습니다.
- AI, IoT, WebSocket, FCM, 배포는 범위 밖입니다.

## 다음 작업

1. 실제 Google 자격 증명으로 수동 로그인/로그아웃 검증
2. 프론트엔드의 권장 API, 세션, CSRF 통합 검증
3. 관리자 역할과 전역 쓰기 API 분리
4. Guardian 초대/수락 모델과 단일 PRIMARY 정책
5. 운영용 비밀 관리, 백업/복원, 배포 프로필 설계
