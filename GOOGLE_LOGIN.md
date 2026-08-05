# Google 로그인 및 프론트 연동

## Google Cloud 설정

1. Google Cloud Console에서 OAuth 동의 화면을 구성합니다.
2. OAuth Client 유형을 웹 애플리케이션으로 생성합니다.
3. 승인된 리디렉션 URI에 아래 값을 등록합니다.

```text
http://localhost:8080/login/oauth2/code/google
```

실제 Client ID와 Secret은 환경변수로만 전달합니다.

```powershell
$env:GOOGLE_CLIENT_ID="..."
$env:GOOGLE_CLIENT_SECRET="..."
$env:FRONTEND_BASE_URL="http://localhost:3000"
```

## 로그인 흐름

1. 브라우저를 `GET /oauth2/authorization/google`로 이동합니다.
2. Google 콜백에서 OIDC `sub`로 기존 OAuthAccount를 조회합니다.
3. 최초 로그인은 Account와 OAuthAccount를 만들고 `onboardingCompleted=false`로 둡니다.
4. 기존 로그인은 같은 Account를 반환하며 Google의 이메일, 이름, 프로필 이미지를 최신값으로 동기화합니다.
5. 성공 시 `/oauth/callback?login=success&onboardingCompleted=...`로 이동합니다.
6. 프론트는 `GET /api/v1/auth/me`로 최종 계정 상태를 확인합니다.

같은 `sub`는 계정을 중복 생성하지 않습니다. 다른 `sub`가 이미 사용 중인 이메일을 제시하면 자동 병합하지 않고 409로 거부합니다. 비밀번호와 OAuth access/refresh token은 저장하지 않습니다.

## 세션과 CSRF

모든 API 요청은 쿠키를 포함해야 합니다.

```javascript
fetch(url, {
  credentials: "include"
});
```

변경 요청 전 `GET /api/v1/auth/csrf`를 호출합니다. 응답과 `XSRF-TOKEN` 쿠키의 토큰을 이후 요청의 `X-XSRF-TOKEN` 헤더에 넣습니다.

```javascript
fetch(url, {
  method: "POST",
  credentials: "include",
  headers: {
    "Content-Type": "application/json",
    "X-XSRF-TOKEN": csrfToken
  },
  body: JSON.stringify(payload)
});
```

로컬 세션 쿠키는 HttpOnly, SameSite=Lax, Secure=false입니다. HTTPS 운영 환경에서는 `SESSION_COOKIE_SECURE=true`를 사용합니다.

## 온보딩과 로그아웃

`POST /api/v1/auth/onboarding`은 서비스 이용약관과 개인정보 처리방침 동의가 필수입니다. GUARDIAN/SUPPORTER는 Account와 1:1 Guardian을 생성 또는 재사용합니다. 완료된 계정의 반복 요청은 409입니다.

`POST /api/v1/auth/logout`은 CSRF 헤더가 필요하며 SecurityContext와 HTTP 세션을 지우고 JSESSIONID를 삭제합니다.

## 수동 통합 테스트

- Google 로그인 시작과 콜백 URI 확인
- 최초 로그인 후 `onboardingCompleted=false` 확인
- 온보딩 후 Guardian 생성 및 `onboardingCompleted=true` 확인
- 같은 Google 계정 재로그인 시 Account 중복 없음 확인
- 브라우저 요청의 세션 쿠키와 CORS 확인
- CSRF 없는 POST가 403, 토큰 포함 POST가 성공하는지 확인
- 로그아웃 후 보호 API가 401인지 확인

실제 자격 증명, 세션 ID, 토큰, Google `sub`와 이메일을 로그나 Git에 남기지 않습니다.
