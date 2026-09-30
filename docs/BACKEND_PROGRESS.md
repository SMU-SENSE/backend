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
- 보호자용 라이브 AAC Phase: 사용자별 판/카드/카테고리 편집, SSE 동기화
- 보호자 튜토리얼, 문장 이해 수준 1~4, 성인 음성 옵션
- 기기 전용 토큰, heartbeat, 카드 사용·위치·센서·긴급 이벤트 수집
- 장소/안심존, 루틴 스케줄러, 알림 이력, Push 구독 저장
- 보호자 리포트 집계/PDF, 인증 이미지 업로드, 회원 탈퇴
- 의사소통 세부 프로필, 표시 문구/TTS 문구 분리, 이미지 출처 메타데이터
- 중요·즐겨찾기·빈도·최근 사용 기반 개인화 AAC 컨텍스트와 단일 문장 AI provider 계약
- device token 기반 TTS/STT/표정 결과 기록과 설정형 이벤트 보존 정책
- Flyway V7 및 개인화 AAC 통합 테스트

## 검증 결과

- `mvnw test`: 17 tests 성공
- `mvnw clean test`: OneDrive의 `target/classes` 잠금으로 clean 단계 실패(테스트 진입 전)
- H2 Flyway V1~V7 및 Hibernate validate 성공
- `mvnw -Ppostgres-it verify`: Docker 엔진 미가동으로 컨테이너 시작 전 실패(코드 컴파일 성공, PostgreSQL 실행 검증 미완료)

## 외부 연동 / 다음 Phase

- 이메일 회원가입·인증·로그인, 비밀번호 재설정
- 카카오 OAuth, 실제 Google OAuth 브라우저 검증
- 운영 S3/Cloud Storage와 실제 TTS 공급자 연결
- VAPID/FCM/APNs Web Push 발송 어댑터 연결
- Map API 주소 검색 및 지도 UI 연결
- 운영용 pairing rate limit, 부하/경합 검증
- `AacSentenceRecommendationProvider` 실제 LLM 구현, STT/TTS/표정 인식 엔진, IoT 장치, 배포

## 알려진 제한

- 전역 Guardian/상징 쓰기 API의 관리자 역할 분리가 필요하다.
- 한 AAC 사용자에 여러 ACTIVE 기기를 허용하며 제품 정책 확정이 필요하다.
- OneDrive 파일 잠금으로 Maven `clean`이 간헐적으로 target 삭제에 실패할 수 있다.
