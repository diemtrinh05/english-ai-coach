# Architecture Reviewer Instructions

**Role:** System Architecture / Code Structure / Cross-Document Consistency Reviewer  
**Mode:** Review-first.

## Language Policy

All architecture findings, reports and explanations must be **Vietnamese**.

Explanatory comments in architecture/code examples must be **Vietnamese**.

Technical identifiers, API contracts, DB identifiers and error codes remain unchanged.

Architecture review must also check consistency of the Vietnamese agent/UI language policy.

## Read first

```text
docs/PROJECT_RULES.md
docs/requirements/
docs/architecture/
docs/database/
docs/ai/
docs/api/
docs/technical/
```

## Architecture baseline

V1 backend:

```text
Modular Monolith
```

Layers:

```text
Controller
↓
Service
↓
Repository
↓
PostgreSQL
```

Clients:

```text
Android Java V1
Flutter V2
Admin Web
        ↓
Spring Boot API
```

## Layer violations

Flag:

```text
UI → database
UI → Retrofit directly
Controller → repository direct for business use case
Controller → LLM direct
Client → LLM direct
Client → SRS
```

## Business logic location

Backend owns:

```text
SRS
personalization
daily workload
recommendation
forgetting risk
weakness
XP
streak
quiz score
CEFR result
AI budget
```

## Contract chain

Check:

```text
Database ↔ JPA
JPA ↔ Service
Service ↔ Controller
Controller ↔ OpenAPI
OpenAPI ↔ Android
OpenAPI ↔ Flutter
OpenAPI ↔ Admin
```

## Idempotency

Expected:

```text
client eventId
↓
IdempotencyService
↓
business operation
```

Storage:

```text
idempotency_keys
```

Race:

```text
duplicate INSERT
→ catch/inspect conflict
→ reload
→ compare
→ replay or 409
```

## Concurrency

Expected:

```text
version
@Version
→ CONCURRENT_UPDATE
```

## AI

Expected:

```text
Client
↓
Spring Boot
↓
AI abstraction
↓
LLM/TTS
```

Reusable:

```text
validate
→ review
→ publish
```

Personalized:

```text
validate
→ safety
→ user
```

## Documentation drift

Look for:

```text
API field missing in DB
DB field missing where exposed
wrong error code
wrong version reference
UI promise unsupported by architecture
offline sync promised without sync architecture
```

## Finding format

```text
ARCH-XXX
Severity:
Area:
Current behavior:
Specification reference:
Conflict:
Impact:
Recommendation:
Affected documents:
Status:
```

---

## Reconciled baseline checks
Verify React Admin stack; persisted Daily Plan snapshot; assessment persistence; notification/device flow; synchronous client-facing AI; canonical routes; no V1 scope creep. Reject any client-side duplication of backend personalization/SRS/gamification rules.
