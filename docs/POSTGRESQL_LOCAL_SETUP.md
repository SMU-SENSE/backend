# PostgreSQL Local Setup

이 문서는 Docker Compose로 실행하는 말모아 로컬 개발 DB 절차입니다. 운영 배포 설정으로 사용하지 않습니다.

## 사전 요구사항

- Docker Desktop과 Docker Compose
- Java 21 이상
- Windows PowerShell
- 로컬에서 사용 가능한 TCP 포트 5432

## 환경 파일

```powershell
Copy-Item .env.example .env
```

`.env`의 `POSTGRES_PASSWORD`와 `DB_PASSWORD`를 동일한 강한 로컬 전용 값으로 바꿉니다. 실제 비밀번호나 Google OAuth secret은 커밋하지 않습니다. 일반 서버 실행에는 `GOOGLE_CLIENT_ID`와 `GOOGLE_CLIENT_SECRET`도 필요합니다.

기본 연결값은 다음과 같습니다.

```text
POSTGRES_DB=malmoa
POSTGRES_USER=malmoa
POSTGRES_PORT=5432
DB_URL=jdbc:postgresql://localhost:5432/malmoa
DB_USERNAME=malmoa
```

## DB 및 서버 실행

```powershell
.\scripts\postgres-up.ps1
docker compose ps
.\scripts\run-postgres.ps1
```

`postgres-up.ps1`은 Docker 연결과 `.env`를 확인하고 PostgreSQL healthcheck를 기다립니다. `run-postgres.ps1`은 `.env`를 현재 프로세스에만 읽고 `postgres` 프로필을 실행합니다. Flyway V1-V4가 먼저 적용되고 Hibernate가 `ddl-auto=validate`로 스키마를 검증합니다.

Google 설정 없이 DB와 공개 API만 확인할 때는 다음 명령을 사용합니다. 이 모드는 Google 로그인을 검증하거나 가짜로 성공시키지 않습니다.

```powershell
.\scripts\run-postgres.ps1 -Smoke
.\scripts\smoke-test.ps1
```

## psql 확인

```powershell
docker compose exec postgres psql -U malmoa -d malmoa
```

psql 안에서 다음을 확인할 수 있습니다.

```sql
\dt
SELECT * FROM flyway_schema_history ORDER BY installed_rank;
SELECT COUNT(*) FROM accounts;
SELECT COUNT(*) FROM guardians;
SELECT COUNT(*) FROM aac_users;
SELECT COUNT(*) FROM user_guardians;
SELECT COUNT(*) FROM symbol_categories;
SELECT COUNT(*) FROM symbols;
SELECT COUNT(*) FROM user_favorite_symbols;
SELECT COUNT(*) FROM symbol_usage_logs;
```

## 테스트

```powershell
.\mvnw.cmd clean test
.\mvnw.cmd -Ppostgres-it verify
```

첫 명령은 Docker 없이 H2로 실행됩니다. `postgres-it` Maven 프로필은 Testcontainers PostgreSQL에서 빈 스키마 마이그레이션, Hibernate 검증, CRUD와 제약조건을 검사합니다.

## 종료 및 초기화

```powershell
.\scripts\postgres-down.ps1
```

기본 종료는 named volume `malmoa_postgres_data`를 보존하므로 컨테이너나 서버 재시작 뒤에도 데이터와 `flyway_schema_history`가 유지됩니다. 전체 초기화는 모든 로컬 DB 데이터를 영구 삭제합니다.

```powershell
.\scripts\postgres-down.ps1 -ResetData
```

## 문제 해결

- Docker 연결 실패: Docker Desktop이 완전히 시작된 뒤 다시 실행합니다.
- 5432 bind 실패: `Get-NetTCPConnection -LocalPort 5432`와 `netsh interface ipv4 show excludedportrange protocol=tcp`로 점유/예약 범위를 확인합니다. 충돌 시 `.env`의 `POSTGRES_PORT`와 `DB_URL` 포트를 같은 값(예: 55432)으로 변경합니다.
- 인증 실패: `POSTGRES_*`와 `DB_*` 사용자/비밀번호가 일치하는지 확인합니다. 기존 volume은 최초 생성 시 자격 증명을 유지하므로 필요한 경우 데이터 삭제를 이해한 뒤 `-ResetData`로 초기화합니다.
- Flyway checksum 오류: 이미 적용한 migration을 수정하지 말고 새 버전 migration을 추가합니다.
- Swagger 404: PostgreSQL 프로필의 기본값은 비활성화입니다. 로컬 `.env`에 `SPRINGDOC_ENABLED=true`를 설정합니다.
- Google 변수 오류: 실제 로그인 실행에는 실제 OAuth 값이 필요합니다. DB 스모크만 필요하면 `-Smoke`를 사용합니다.
- OneDrive 경로: 동기화나 백신이 `target`을 잠그면 서버를 종료한 뒤 `clean`을 재실행합니다. 스크립트는 저장소 루트를 기준으로 동작하므로 공백과 한글 경로에서도 직접 실행할 수 있습니다.

Compose 포트는 `127.0.0.1`에만 바인딩됩니다. 비밀번호, OAuth secret, 개인정보를 예제 SQL이나 로그에 남기지 마십시오.
