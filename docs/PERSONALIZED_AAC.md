# Personalized AAC backend contract

## Communication profile

`GET/PATCH /api/v1/me/aac-users/{userId}/communication-profile` uses the existing Guardian session and ownership check. The profile retains `sentenceLevel` 1–4 and adds maximum recommended words plus easy-word, abstract-expression, complex-grammar, and concise-expression preferences.

## Cards and speech text

Board card responses expose both `displayText` and `ttsText`. The legacy `text` field remains as an alias for `displayText`. A missing stored `ttsText` falls back to `displayText`; callers therefore always receive usable speech text.

Image provenance is represented by `imageSourceType`:

- `SYSTEM_DEFAULT`: application-provided symbol.
- `USER_UPLOAD`: URL must be under the authenticated media path `/api/v1/me/aac-users/{userId}/media/images/`.
- `EXTERNAL_ALLOWED`: requires an HTTPS image URL, source name, license, and HTTPS attribution URL.

No third-party image catalog is bundled or copied.

## Personalization context and recommendation

`GET /api/v1/me/aac-users/{userId}/ai/context` builds context from the user profile, active cards, important/favorite state, and the latest 500 card usage records. Up to 50 vocabulary items are ordered by guardian-important, favorite, frequent, recent, system core, then custom vocabulary. `currentSituation` is optional and limited to 500 characters.

`POST /api/v1/me/aac-users/{userId}/ai/recommendations` accepts:

```json
{"currentSituation":"점심 시간"}
```

The successful `data` contract contains one field only:

```json
{"sentence":"물 주세요."}
```

`AacSentenceRecommendationProvider` is the extension point for a future LLM. The full `PersonalizedAacContext` is embedded in the provider prompt. With no provider configured, the API returns 503. Empty, multiline, bulleted, numbered, or oversized provider output is rejected with 502. No real LLM is connected in this change.

## Device interaction events

- `POST /api/v1/device/card-usage`: existing device-token endpoint; `SPEAK` records the device, optional card, display snapshot, actual spoken text, and time. With a card and no `spokenText`, it uses the card TTS fallback.
- `POST /api/v1/device/stt-events`: records recognized text, outcome, optional confidence, device/user, and time. It does not accept or store audio.
- `POST /api/v1/device/expression-events`: reuses `SensorEvent` with `type=EXPRESSION`, emotion in `label`, and confidence in `numericValue`. It does not accept or store face images or video.

## Event retention

Automatic deletion is disabled for an event type until its duration is configured. Independent Spring duration properties are:

```text
app.retention.card-usage
app.retention.stt
app.retention.sensor
app.retention.location
```

Equivalent environment variables use the `APP_RETENTION_*` names documented in `.env.example`. Cleanup runs daily at 04:00 by default; the schedule can be changed with `app.retention.cleanup-cron`. No product retention duration is hardcoded.
