# Google 로그인 실행 안내

## 1. Google Cloud Console 설정

웹 애플리케이션 OAuth Client를 생성하고 승인된 리디렉션 URI에 아래 주소를 등록한다.

```text
http://localhost:8080/login/oauth2/code/google
```

## 2. 환경변수

PowerShell:

```powershell
$env:GOOGLE_CLIENT_ID="발급받은 Client ID"
$env:GOOGLE_CLIENT_SECRET="발급받은 Client Secret"
$env:FRONTEND_BASE_URL="http://localhost:3000"
```

## 3. 로그인 시작

브라우저 또는 프론트엔드에서 아래 주소로 이동한다.

```text
http://localhost:8080/oauth2/authorization/google
```

성공하면 다음 주소로 이동한다.

```text
http://localhost:3000/oauth/callback?login=success
```

프론트엔드는 콜백 화면에서 아래 API를 호출한다.

```http
GET http://localhost:8080/api/v1/auth/me
Credentials: include
```

## 4. 최초 온보딩

```http
POST http://localhost:8080/api/v1/auth/onboarding
Content-Type: application/json
Credentials: include
```

```json
{
  "accountType": "GUARDIAN",
  "termsOfServiceAgreed": true,
  "privacyPolicyAgreed": true,
  "marketingAgreed": false,
  "phoneNumber": "010-0000-0000"
}
```

## 5. 로그아웃

```http
POST http://localhost:8080/api/v1/auth/logout
Credentials: include
```

## 주요 구현 파일

```text
account/domain/Account.java
auth/service/CustomOidcUserService.java
auth/security/SecurityConfig.java
auth/controller/AuthController.java
auth/handler/OAuth2SuccessHandler.java
```

## 주의

- Google Client Secret은 GitHub에 올리지 않는다.
- 로그인 계정과 AAC 사용자 프로필은 분리되어 있다.
- 보호자 또는 지원자가 Google로 로그인한 뒤 AAC 사용자 프로필을 관리하는 구조다.
- 현재 MVP는 세션 쿠키 인증을 사용한다.
