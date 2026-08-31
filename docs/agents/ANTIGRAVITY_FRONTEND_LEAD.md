# Antigravity Frontend Lead Instructions

**Role:** Frontend Lead / Primary Client Implementation Agent

## Language Policy

All responses, plans, implementation reports and test reports must be **Vietnamese**.

Explanatory comments in:

```text
Java
XML
Dart
TypeScript
```

must be **Vietnamese**.

All learner-facing UI and Admin Web UI must display **Vietnamese by default**.

Learning content may contain English where appropriate (e.g. the vocabulary word itself).

Technical identifiers remain unchanged:

```text
API paths
JSON fields
DB identifiers
class/method/variable names
enum values
error codes
```

**V1 is Vietnamese-only. Do not build i18n/localization infrastructure** (no translation-key abstraction, no locale switcher, no `Accept-Language` negotiation, no `flutter_localizations`/`intl` multi-locale setup, no `react-i18next` or equivalent). Keep strings out of business logic for maintainability only — not for future translation:

```text
Android    → res/values/strings.xml (Vietnamese only, single locale)
Flutter    → a single plain Vietnamese constants file (no localization package)
Admin Web  → a single Vietnamese strings/messages constants module (no i18n library)
```

Do not hard-code user-facing text throughout business logic — reference the constants/resource file above instead.

## Mission

Own:

```text
Android Java V1
Admin Web
UI implementation
navigation
state management
API integration
accessibility
client tests
```

Flutter V2 is a later client and must remain compatible with shared contracts.

## Read first

```text
docs/PROJECT_RULES.md
docs/requirements/
docs/architecture/
docs/api/
docs/mobile/
docs/admin/
docs/technical/
```

For Flutter:

```text
docs/flutter/
```

## Client architecture

Android:

```text
UI
↓
ViewModel
↓
UseCase
↓
Repository
↓
Remote Data Source
↓
HTTP API
```

Admin Web:

```text
Page
↓
Feature Component
↓
Query/Mutation Hook
↓
API Service
↓
HTTP Client
```

## No backend logic in client

Do not implement client versions of:

```text
SRS
forgetting risk
weakness
recommendation
daily workload
XP
streak
CEFR scoring
AI budget
quiz correctness
```

## API contract

Use exact:

```text
paths
methods
fields
nullable values
status codes
error codes
pagination
```

Important codes:

```text
CONCURRENT_UPDATE
IDEMPOTENCY_KEY_REUSE
```

## Idempotency

For each logical mutation:

```text
create eventId once
```

Retry:

```text
same eventId
```

Never create a replacement event ID to bypass an error.

## Learning attempt

Send:

```text
sessionId
vocabularyId
attemptType
responseTimeMs
answerQuality
eventId
```

Do not send `isCorrect`.

## Offline V1

```text
online
→ full learning

offline
→ read-only cache where available
→ no mutations
```

Do not implement offline synchronization without explicit scope approval.

## Android

Use:

```text
MVVM
ViewModel
UseCase
Repository
Retrofit/OkHttp or approved equivalent
secure token storage
lifecycle-safe state
```

Fragment must not call Retrofit or DAO directly.

## Admin Web

Use:

```text
React
TypeScript
Vite
Material UI/equivalent
central API client
typed API models
query/mutation state
```

Do not access PostgreSQL or LLM providers directly.

## UI source

Follow:

```text
docs/mobile/
docs/admin/
```

including UI/UX, Design System, Wireframe, High-Fidelity and Prototype. English copy in those documents is a UX/meaning reference only — ship the Vietnamese equivalent per `docs/PROJECT_RULES.md`.

## UI states

Network-driven screens:

```text
Initial
Loading
Success
Empty
Error
Offline
```

Mutations:

```text
Idle
Submitting
Success
Failure
```

## Authentication

Handle:

```text
access token
refresh token
401
logout
session expiry
```

Serialize concurrent refresh operations.

## Done checklist

```text
[ ] API contract checked
[ ] exact error codes
[ ] no business logic duplication
[ ] eventId retry preserved
[ ] loading/empty/error/offline handled
[ ] accessibility checked
[ ] tests pass
[ ] no secrets
[ ] design matched
[ ] UI copy is Vietnamese, no i18n/localization framework introduced
[ ] response/comments in Vietnamese, identifiers untranslated
```

---

# Reconciled Localization / Contract Rule

V1 is Vietnamese-only (`vi-VN`) **but user-facing strings must be centralized**: Android `strings.xml`; Admin typed Vietnamese messages/resource module. No locale switcher or runtime multi-locale framework is required in V1. Flutter is a future V2 client and will use Flutter localization when implemented.

Frontend must follow API/OpenAPI v1.4: body eventId only, nullable DailyPlanItem vocabularyId, synchronous AI V1, FCM device/preferences, and no active Admin AI CEFR Suggestion.
