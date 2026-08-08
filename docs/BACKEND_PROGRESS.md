# Backend Progress

## 완료

- 기존 Google OIDC/세션/CSRF 및 Guardian 기반 AAC 사용자 소유권 검사
- Figma 09 프로필 필드와 보호자별 관계 저장
- Figma 10 격자, 11 TTS 설정, 12 가입정보 summary/확정
- AAC 사용자 진행 단계 복구(`PROFILE_COMPLETED`~`CONFIRMED`)
- QR payload와 6자리 코드를 함께 발급하는 10분 pairing session
- refresh 무효화, USED 재사용 방지, 404/409/410 구분
- 여러 AAC Device 연결 및 목록 조회
- Flyway V5, Swagger 태그, H2 통합 테스트

## 검증 결과

- `mvnw test`: 14 tests 성공
- `mvnw clean test`: OneDrive의 `target/classes` 잠금으로 clean 단계 실패(테스트 진입 전)
- H2 Flyway V1~V5 및 Hibernate validate 성공
- `mvnw -Ppostgres-it verify`: Docker 엔진 미가동으로 컨테이너 시작 전 실패(코드 컴파일 성공, PostgreSQL 실행 검증 미완료)

## 미구현 / 다음 Phase

- 이메일 회원가입·인증·로그인, 비밀번호 재설정
- 카카오 OAuth, 실제 Google OAuth 브라우저 검증
- 이미지 업로드/S3와 실제 TTS 생성
- 운영용 pairing rate limit, 부하/경합 검증
- AI/IoT/긴급 알림/배포

## 알려진 제한

- 전역 Guardian/상징 쓰기 API의 관리자 역할 분리가 필요하다.
- 한 AAC 사용자에 여러 ACTIVE 기기를 허용하며 제품 정책 확정이 필요하다.
- OneDrive 파일 잠금으로 Maven `clean`이 간헐적으로 target 삭제에 실패할 수 있다.
