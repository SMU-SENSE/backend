# 진행 기록 (yji) — 카드 추천 · 문장 변환

작성일: 2026-11-26 · 브랜치 `feature/yunji-gardian-alarm`

기준 문서: `docs/AI_RECOMMENDATION_SPEC.md` (모델 담당 전달 명세). 아래 "명세와 다른 점"은 모델/앱 담당에게 공유해야 한다.

## 전체 흐름

```
[카드 선택] ──(앱, 기기 안)── model.json 으로 다음 카드 5장 추천
     │
     ├─ "문장으로 변환하기" → POST /sentences → LLM(OpenAI) → 자연어 문장 (저장 안 함)
     └─ 변환 없이 단어 그대로 사용
     │
     ▼
말하기/완성 시 → POST /utterance-logs (카드 순서 + converted + spoken) → 개인 n-gram 복원용
```

## 결정 대기

### 1. 카드별 기록(`usage-logs`)과 문장 기록(`utterance-logs`)의 중복

- 현재 앱이 카드를 누를 때마다 `POST /usage-logs`(SELECT/CANCEL/SPEAK)를 보내고, 문장을 마칠 때 `POST /utterance-logs`도 보내면 같은 사용이 두 번 기록된다.
- `usage-logs`가 맡고 있는 기능
  - 긴급 상징 SPEAK → 보호자 알림 (`NotificationService`)
  - 최근 사용 상징 (`recent-symbols`)
- 선택지
  - A. 둘 다 유지: 카드별 기록은 알림·최근 사용용, 문장 기록은 n-gram용으로 역할을 나눈다. 가장 단순하지만 저장량이 늘어난다.
  - B. `usage-logs`는 긴급 카드 SPEAK에만 쓴다. 최근 사용은 `utterance-logs`에서 계산하도록 바꾼다.
  - C. `utterance-logs`만 쓴다. spoken=true인 기록에 긴급 카드가 있으면 서버가 알림을 보낸다. 알림이 문장을 마칠 때까지 늦어질 수 있어 긴급어에는 맞지 않을 수 있다.
- 현재 상태: 두 API가 모두 동작한다(A). 어느 쪽을 쓸지는 앱 구현과 같이 정한다.

### 2. 그 외 미정 (명세 8장 이어받음)

- 사용 기록 서버 저장에 대한 사용자 동의 절차와 보관 기간
- 카드 목록과 `card_id` 최종 확정. 확정되면 Postgres용 카드 시드 Flyway 마이그레이션을 추가한다.
- 문법(GRAMMAR) 카드를 넣을지 여부
- OpenAI 모델 최종 선택(현재 기본 `gpt-4.1-mini`, `OPENAI_MODEL`로 변경)과 타임아웃(현재 5초)
- 앱(기기)에서 보내는 요청의 인증 방식. 현재 모든 API가 보호자 Google 세션 기준이고, 페어링된 기기 전용 토큰은 없다.

## 구현 완료 (이번 작업)

| 기능 | API / 위치 | 비고 |
|---|---|---|
| 추천 모델 배포 | `GET /api/v1/recommend-model/latest` → `{version, url}`<br>`GET /api/v1/recommend-model/files/{version}` → model.json 원본 | `recommend/`. 기동 시 한 번 읽음. 버전별 URL이라 immutable 캐시 |
| 카드 ID | `symbols.card_code` (V7) | 응답 `SymbolResponse`에 `cardCode`, `custom`, `predict` 추가 |
| H2 카드 시드 | `src/main/resources/seed/cards.csv` → `DataInitializer` | 기존 샘플 19개를 101장으로 교체. Postgres는 시드 없음 |
| 커스텀 카드 | `POST/GET /api/v1/me/aac-users/{id}/custom-symbols`<br>`DELETE .../custom-symbols/{symbolId}` | `card_code = ic_custom_<uuid32>`. 삭제는 비활성화만 함 |
| 커스텀 카드 격리 | `GET /api/v1/symbols`, `/symbols/{id}`, 즐겨찾기, 사용 기록 | 남의 커스텀 카드는 404 |
| 문장 변환 (LLM) | `POST /api/v1/me/aac-users/{id}/sentences` `{cardCodes, style}` → `{sentence, generated}` | `sentence/`. OpenAI Chat Completions, `RestClient` 사용(SDK 추가 없음). 결과 저장 안 함 |
| 문장 단위 사용 기록 | `POST/GET /api/v1/me/aac-users/{id}/utterance-logs` | `{cardCodes, converted, spoken, occurredAt}`. GET은 `since`(선택)부터 시간순 전체 |
| 마이그레이션 | `V7__add_card_codes_custom_symbols_and_utterance_logs.sql` | H2 Flyway 검증 + Postgres Testcontainers 검증 통과 |
| 테스트 | `RecommendationAndSentenceIntegrationTests`, `OpenAiClientTests` | `mvnw test` 21개, `-Ppostgres-it verify` 통과 |

## 명세(`AI_RECOMMENDATION_SPEC.md`)와 다른 점

| 명세 | 실제 구현 | 이유 |
|---|---|---|
| `cards` 테이블, PK = `card_id`(문자열) | 기존 `symbols` 테이블에 `card_code` 컬럼 추가. PK는 숫자 `id` 그대로 | 즐겨찾기·사용 기록·알림이 이미 숫자 FK로 연결돼 있음 |
| `label` | `name`(화면 표시). `ttsText`는 따로 있음(시드는 label과 같음) | 기존 컬럼 재사용 |
| `predict` 컬럼 | 저장 안 함. `predict = !emergency`로 계산해 응답에 포함 | cards.csv에서 predict=0 ⇔ 긴급어 6장으로 정확히 일치 |
| `deleted_at` | 기존 `active=false` | 기존 소프트 삭제 방식 재사용 |
| `is_custom`, `owner_id` | `owner_user_id`(AAC 사용자 FK). `custom = owner != null` | 소유자는 보호자가 아니라 AAC 사용자 |
| 카테고리 코드 소문자(`food`) | 대문자(`FOOD`). **앱은 model.json의 category 키와 비교할 때 소문자로 바꿔야 함** | 기존 카테고리 생성 API가 대문자만 허용 |
| `categories.color` | 없음 | 아직 쓰는 곳이 없음. 앱이 서버 색상이 필요하면 추가 |
| GRAMMAR 카테고리 | 시드에서 뺌 | 카드가 0장 |
| `GET /api/model/latest` | `GET /api/v1/recommend-model/latest` | 기존 `/api/v1` 규칙 |
| `POST /api/sentence` `{card_ids, style: polite}` | `POST /api/v1/me/aac-users/{id}/sentences` `{cardCodes, style: POLITE}` | 사용자별 권한 확인과 커스텀 카드 조회에 사용자 ID가 필요. 필드 이름은 camelCase |
| LLM 실패 시 앱이 라벨 이어붙임 | 서버가 라벨 이어붙인 값을 `generated=false`로 돌려줌 | 앱의 대체 처리가 단순해짐. 네트워크 오류는 여전히 앱이 처리해야 함 |
| 사용 기록 `{user_id, card_ids, spoken, created_at}` | `{cardCodes, converted, spoken, occurredAt}` | 변환 여부를 남기고 변환 문장 텍스트는 저장하지 않기로 함 |
| 커스텀 카드 긴급 여부 | 긴급어 카테고리(`EMERGENCY`)로 만들면 `emergency=true` | 긴급 SPEAK 알림이 커스텀 긴급 카드에도 동작 |

## 기타 변경

- `PostgresIntegrationIT`가 Flyway 마이그레이션 개수를 5로 하드코딩하고 있었다. V6를 추가할 때부터 이미 실패하던 상태였고, 7로 고쳤다.
- `SymbolRepository` 조회 메서드 이름에 `AndOwnerIsNull`을 붙였다. 기본 카드만 조회하도록 하기 위해서이고, 테스트도 함께 수정했다.
- 설정 추가: `OPENAI_API_KEY`, `OPENAI_MODEL`, `OPENAI_TIMEOUT`, `RECOMMEND_MODEL_LOCATION` (`.env.example`, `application.yml`). 키가 없으면 LLM 없이 라벨 이어붙이기로 동작한다(dev·테스트 기본).
- `ai/` 폴더는 삭제했다. 파일 위치: 모델 `src/main/resources/models/ngram/model.json`, 카드 `src/main/resources/seed/cards.csv`, 명세 `docs/AI_RECOMMENDATION_SPEC.md`.

## 구현할 항목 (다음)

- [ ] 결정 대기 1번(기록 중복)에 맞춰 앱 연동과 서버 정리
- [ ] card_id 확정 → 모델 재빌드 → Postgres 카드 시드 마이그레이션(Vn)
- [ ] 모델 파일 여러 버전 보관과 롤백. 지금은 배포된 1개 버전만 서빙(`RecommendModelService`의 ponytail 주석)
- [ ] 사용 기록 동의 절차, 보관 기간과 만료 삭제
- [ ] `utterance-logs` 조회 페이징 (기록이 많아지면)
- [ ] 문장 생성 API 호출 제한(rate limit). 인증된 사용자는 지금 제한 없이 OpenAI 비용을 쓸 수 있음
- [ ] 기기(페어링) 전용 인증. 앱이 보호자 세션 없이 호출하려면 필요
- [ ] 커스텀 카드 이미지 업로드 (지금은 URL 문자열만)
- [ ] 실제 OpenAI 키로 문장 품질 수동 확인 (자동 테스트는 대체 경로와 응답 파싱만 검증)
