# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

Spring Boot backend for 말모아 (Malmoa), an adaptive AAC (augmentative and communication) system for
people with developmental disabilities. It separates Guardian accounts (Google OIDC login) from AAC
user profiles, and gates access on the Guardian↔AAC-user link. Java 21, Spring Boot 3.5.16, Maven Wrapper.

## Commands

```powershell
# Run locally (H2 in-memory, seeded with sample categories/symbols) — needs Google OAuth env vars
$env:GOOGLE_CLIENT_ID="..."
$env:GOOGLE_CLIENT_SECRET="..."
$env:FRONTEND_BASE_URL="http://localhost:3000"
.\mvnw.cmd spring-boot:run

# Unit/integration tests on H2, no Docker required
.\mvnw.cmd clean test

# Run a single test class
.\mvnw.cmd test -Dtest=OnboardingAndPairingIntegrationTests

# Real PostgreSQL integration test (Testcontainers, requires Docker Desktop running)
.\mvnw.cmd -Ppostgres-it verify
```

- `postgres-it` runs only `PostgresIntegrationIT` (bound via the Surefire `postgres-it` profile in `pom.xml`) and
  verifies Flyway migrations, Hibernate `validate` mode, repository CRUD, and unique/FK constraints against a real
  Postgres. Test OAuth values never complete a real login — verify actual Google browser login manually with real
  Client ID/Secret.
- Health: `http://localhost:8080/api/v1/health` · Swagger UI: `/swagger-ui/index.html` · H2 console: `/h2-console`
  (default profile uses `jdbc:h2:mem:malmoa`).
- Note: on OneDrive-synced checkouts, `mvnw clean` can intermittently fail to delete `target/classes` due to file
  locking — rerun if `clean` fails before tests start.
- Requires JDK 21+ (`java.version` in `pom.xml`). If `java -version` on PATH resolves to something older, point
  `JAVA_HOME` at a 21+ install for the `mvnw` invocation rather than changing the PATH default.

### Local PostgreSQL

```powershell
Copy-Item .env.example .env      # then set a real local-only password
.\scripts\postgres-up.ps1        # docker-compose up
.\scripts\run-postgres.ps1       # run app on postgres profile (needs Google OAuth vars too)
.\scripts\run-postgres.ps1 -Smoke   # DB/schema smoke only, skips Google login
.\scripts\smoke-test.ps1
.\scripts\postgres-down.ps1              # keeps volume
.\scripts\postgres-down.ps1 -ResetData   # wipes local DB data
```

Postgres profile applies Flyway V1–V5 then runs Hibernate in `ddl-auto=validate` (schema must already match
entities — model changes need a new Flyway migration, not just entity edits). Compose binds Postgres to
`127.0.0.1` only, for local dev — not a deployment config. Full walkthrough: `docs/POSTGRESQL_LOCAL_SETUP.md`.

## Architecture

**Package-by-feature under `com.aac.ieojwo`**, each module (`account`, `auth`, `guardian`, `user`, `aac`,
`device`, `symbol`, `common`, `health`) follows `controller/ domain/ dto/ repository/ service/`. Cross-cutting
concerns (exceptions, `ApiResponse`/`ApiError` envelope, `BaseTimeEntity`) live in `common`.

**Identity model** — three distinct concepts, don't conflate them:
- `Account` — a person, auth-identified by Google OIDC `sub` (never by email; email/name/picture are synced
  from Google on every login but aren't the identity key). `OAuthAccount` links Account ↔ provider `sub`.
- `Guardian` — the caregiver role tied 1:1 to an `Account`, created during onboarding.
- `AacUser` — the actual AAC end-user profile (grid size, TTS voice, favorites, usage logs), linked to
  Guardian(s) via `UserGuardian`. An Account has no direct permissions on AAC data — every access check
  resolves Account → Guardian → UserGuardian → AacUser.

**Access control pattern**: business services don't inspect `OidcUser`/`SecurityContext` directly. They go
through `CurrentAccountService.requireGoogleAccount(principal)` → `Account`, then a per-feature
`*AccessService` (e.g. `GuardianAccessService.requireCurrentGuardian`, `AacUserAccessService`) that resolves
and authorizes the owning Guardian/AacUser, throwing `ForbiddenException`/`UnauthorizedException` on failure.
Follow this chain when adding new authenticated endpoints rather than querying repositories directly from
controllers.

**Two AAC-user API surfaces on purpose**: `/api/v1/me/aac-users/**` is the current, preferred API;
`/api/v1/users/**` is kept for compatibility with the same ownership checks. Don't delete the legacy one;
new work goes under `/me/aac-users`.

**Auth flow** (`auth/handler/OAuth2SuccessHandler`, `auth/service/CustomOidcUserService`): Google OIDC login →
lookup by `sub` → first login creates `Account` + `OAuthAccount` with `onboardingCompleted=false`; existing
`sub` reuses the Account and refreshes profile fields. Different `sub` claiming an email already in use is
rejected with 409, never auto-merged. No password or OAuth token is ever persisted. Session cookies are
HttpOnly/SameSite=Lax (Secure via `SESSION_COOKIE_SECURE`, true in the postgres/prod profile). CSRF uses
`CookieCsrfTokenRepository`; state-changing requests need the `XSRF-TOKEN` cookie value echoed in the
`X-XSRF-TOKEN` header (see `SecurityConfig`, exempted: `/h2-console/**`, device pairing claim endpoints).

**Device pairing** (`device/`): Guardian issues a `DevicePairingSession` (QR payload + 6-digit code, 10-minute
expiry, single-use); the public claim endpoint (`/api/v1/device-pairings/claim/**`, no auth) redeems it into
an `AacDevice`. Distinguishes 404 (unknown)/409 (already used)/410 (expired) — preserve that distinction if you
touch this flow.

**DB migrations**: Flyway files in `src/main/resources/db/migration`, `V<n>__description.sql`, applied only on
the `postgres` profile (default H2 profile uses `ddl-auto=create-drop` and reseeds via `DataInitializer`
instead). Adding/changing a JPA entity that affects schema requires a new `Vn` migration — Postgres profile
runs Hibernate in `validate` mode and will fail startup on drift.

**Guardian notifications** (`notification/`): `AacUsageService.createUsageLog` fires
`NotificationService.notifyEmergencySymbolUsed` when a logged action is `SPEAK` on a `Symbol` with
`emergency=true`; it fans out a `GuardianNotification` row to every guardian linked via `UserGuardian`
and best-effort pushes through `FcmPushSender` if the guardian has a `pushToken` and
`app.fcm.credentials-path`/`FIREBASE_CREDENTIALS_PATH` is configured (unset in dev — push is skipped, the
DB row is always the source of truth). `GET/PATCH /api/v1/me/notifications*`, `POST /api/v1/me/push-token`.

**API scope reference**: `docs/API_CONTRACT.md`. Currently implemented: auth, Guardian linking, AAC user
profile/grid/TTS onboarding, device pairing, ownership checks, symbols, favorites, usage logs, emergency-symbol
guardian notifications (in-app + best-effort FCM push). Explicitly out of scope: AI sentence generation, IoT,
WebSocket, deployment.
