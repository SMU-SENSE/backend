# 말모아 Backend 1

AI-IoT 기반 적응형 AAC 프로젝트의 **Backend 1 공통 서버**입니다.

## 담당 범위

- Spring Boot 공통 구조 및 공통 응답/예외 처리
- AAC 사용자 프로필과 화면 모드
- 보호자 계정 및 사용자-보호자 연결
- 상징 카테고리와 상징 카드
- 즐겨찾기, 최근 사용, 상징 사용 기록
- AI/IoT 담당이 이후 연결할 API 기반 제공

## 기술 스택

- Java 21
- Spring Boot 3.5.16
- Spring Web
- Spring Data JPA
- Bean Validation
- H2: 로컬 즉시 실행
- PostgreSQL: 팀 통합용 프로필
- Maven

## 실행 방법

### 1. IntelliJ에서 실행

1. 압축을 해제합니다.
2. IntelliJ에서 `pom.xml`을 프로젝트로 엽니다.
3. 프로젝트 SDK를 Java 21로 지정합니다.
4. `MalmoaBackendApplication`을 실행합니다.

### 2. Maven 명령으로 실행

```bash
mvn spring-boot:run
```

Windows PowerShell에서도 동일합니다.

```powershell
mvn spring-boot:run
```

## 실행 확인

브라우저 또는 Postman:

```text
GET http://localhost:8080/api/v1/health
```

예상 응답:

```json
{
  "success": true,
  "data": {
    "status": "UP",
    "service": "malmoa-backend"
  },
  "message": null
}
```

## H2 콘솔

```text
http://localhost:8080/h2-console
```

- JDBC URL: `jdbc:h2:mem:malmoa`
- User Name: `sa`
- Password: 비워 둠

## 기본 데이터

서버 시작 시 다음 데이터가 자동 생성됩니다.

- 사용자: 김민우, SIMPLE, 2×2
- 보호자: 최성희
- 카테고리: 음식, 감정, 사람, 장소, 긴급어, 인사·사회어, 시간, 어미, 신체
- 기본 상징: 물, 밥, 좋아요, 싫어요, 도와주세요, 아파요 등

## 핵심 API

### 상태 확인

```text
GET /api/v1/health
```

### 사용자

```text
POST  /api/v1/users
GET   /api/v1/users
GET   /api/v1/users/{userId}
PATCH /api/v1/users/{userId}/settings
```

사용자 생성 예시:

```json
{
  "name": "박지현",
  "mode": "GENERAL",
  "gridSize": "GRID_3X3"
}
```

### 보호자

```text
POST /api/v1/guardians
GET  /api/v1/guardians
POST /api/v1/users/{userId}/guardians
GET  /api/v1/users/{userId}/guardians
```

### 카테고리·상징

```text
POST /api/v1/categories
GET  /api/v1/categories
POST /api/v1/symbols
GET  /api/v1/symbols
GET  /api/v1/symbols/{symbolId}
```

필터:

```text
GET /api/v1/symbols?categoryId=1
GET /api/v1/symbols?emergency=true
```

### 즐겨찾기

```text
POST   /api/v1/users/{userId}/favorites
GET    /api/v1/users/{userId}/favorites
DELETE /api/v1/users/{userId}/favorites/{symbolId}
```

등록 요청:

```json
{
  "symbolId": 1
}
```

### 최근 사용·사용 로그

```text
POST /api/v1/users/{userId}/usage-logs
GET  /api/v1/users/{userId}/recent-symbols?limit=10
```

사용 로그 요청:

```json
{
  "symbolId": 1,
  "action": "SELECT"
}
```

동작 값:

- `SELECT`: 상징 선택
- `CANCEL`: 선택 취소
- `SPEAK`: 실제 TTS 발화

## PostgreSQL 실행

PostgreSQL에 `malmoa` 데이터베이스를 만든 뒤 환경변수를 지정합니다.

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/malmoa"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="비밀번호"
mvn spring-boot:run -Dspring-boot.run.profiles=postgres
```

## Backend 2·AI·IoT 연결 경계

### AI 담당에 제공할 데이터

Backend 1이 사용자 및 상징 유효성을 확인한 뒤 다음 형태로 AI 연동 모듈에 전달할 예정입니다.

```json
{
  "userId": 1,
  "symbolIds": [1, 2],
  "listenerType": "TEACHER",
  "style": "POLITE"
}
```

### IoT 담당이 호출할 예정인 API

다음 단계에서 별도 모듈로 추가합니다.

```text
POST /api/v1/sensors/heart-rate
POST /api/v1/emergency-events
GET  /api/v1/users/{userId}/current-state
```

## 첫 검증 순서

1. `GET /api/v1/health`가 200인지 확인
2. `GET /api/v1/categories`에 9개 카테고리가 나오는지 확인
3. `GET /api/v1/symbols?emergency=true`에 긴급 상징 4개가 나오는지 확인
4. 사용자 1번에 상징 1번을 즐겨찾기로 등록
5. 사용자 1번의 상징 사용 로그 생성
6. 최근 사용 상징 조회

## 다음 구현 순서

1. 구글 로그인 프론트엔드 연결 및 접근 권한 테스트
2. 사용자별 활성 상징 및 화면 배치
3. 문장 생성 요청·후보·최종 선택 테이블
4. 이미지 업로드 저장소
5. PostgreSQL + Docker Compose
6. AI 문장 생성 모듈 연동
7. IoT 심박수 및 긴급 이벤트 연동

## Google 간편 로그인

Google OAuth2/OIDC 로그인 기능이 추가되어 있습니다.

- 로그인 시작: `GET /oauth2/authorization/google`
- 현재 계정: `GET /api/v1/auth/me`
- 최초 설정: `POST /api/v1/auth/onboarding`
- 로그아웃: `POST /api/v1/auth/logout`

실행 전 `GOOGLE_LOGIN.md`와 `.env.example`을 확인하세요.
