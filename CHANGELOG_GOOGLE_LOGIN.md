# Google 로그인 작업 내역

## 추가된 기능

- Spring Security 세션 인증
- Google OAuth2 / OpenID Connect 로그인
- Google 계정 최초 로그인 시 내부 계정 자동 생성
- 재로그인 시 기존 계정 갱신
- 현재 로그인 계정 조회 API
- 사용자 유형 및 약관 동의 온보딩 API
- 보호자·지원자 선택 시 Guardian 프로필 자동 생성 및 연결
- OAuth 성공·실패 후 Next.js 화면 리디렉션
- 세션 로그아웃 및 JSESSIONID 삭제
- 인증되지 않은 API 요청의 JSON 401 응답
- Google Client ID/Secret 환경변수 분리

## 추가된 API

```text
GET  /oauth2/authorization/google
GET  /login/oauth2/code/google
GET  /api/v1/auth/me
POST /api/v1/auth/onboarding
POST /api/v1/auth/logout
```

## 추가된 테이블

```text
accounts
oauth_accounts
terms_agreements
```

기존 `guardians` 테이블에는 선택적 `account_id` 연결이 추가되었다.

## 검증 상태

- `pom.xml` XML 구조 검사 완료
- `application.yml`, `application-postgres.yml` YAML 구조 검사 완료
- 프로젝트 내부 Java import 참조 검사 완료
- 전체 Java 파일 중괄호 균형 검사 완료
- 현재 실행 환경에는 Maven이 없어 `mvn test` 실제 실행은 수행하지 못함
