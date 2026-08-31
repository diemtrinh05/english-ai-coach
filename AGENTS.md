# AGENTS.md — English AI Coach

**Purpose:** Global instructions for all AI coding/review agents.

## Read first

Before any work, read:

```text
docs/PROJECT_RULES.md
```

Then read the current approved documents relevant to the task.

## Language Policy

All AI-agent responses and reports must be written in **Vietnamese**.

Explanatory comments in code must be written in **Vietnamese**.

All learner-facing App UI and Admin Web UI must be **Vietnamese by default**.

Technical identifiers remain unchanged:

```text
class/method/variable/package names
API paths
JSON fields
database/table/column names
enum values
error codes
```

Example:

```java
// Lưu eventId để retry cùng một logical operation mà không tạo mutation trùng.
```

Do not translate:

```text
CONCURRENT_UPDATE
IDEMPOTENCY_KEY_REUSE
eventId
/api/v1/...
```

Use localization/resource mechanisms for user-facing strings.

## Source of truth

Documentation is the source of truth. Do not silently invent or change:

```text
requirements
business rules
database schema
API paths/fields
error codes
SRS formulas
AI lifecycle
security rules
project scope
```

If something is missing or contradictory:

```text
STOP
→ identify the gap
→ report impact
→ propose a resolution
→ wait for approval when the change affects contract/scope
```

## Implementation Planning

Before starting any implementation task, every Agent must read:

1. `docs/PROJECT_RULES.md`
2. `docs/planning/IMPLEMENTATION_PLAN.md`
3. `docs/planning/MASTER_BACKLOG.md`
4. the relevant approved baseline specification(s)
5. the applicable role-specific Agent instructions

Every implementation change must reference a valid Master Backlog Task ID.

Agents must:

- implement only the assigned task and its explicit dependencies;
- respect milestone and dependency ordering;
- not invent business rules, API behavior, database behavior, or client behavior outside the approved baseline;
- report blockers instead of silently expanding scope;
- satisfy the task acceptance criteria and required reviewer gates before marking a task `DONE`;
- run the required validation/test commands defined by the task before completion.

## Current baseline

```text
SRS                         v1.2
Database Schema             v1.6
System Architecture         v1.3
AI Personalization          v1.3
API Specification           v1.4
OpenAPI / Swagger           v1.4
Technical Specification     v1.2
Backend Technical Spec      v1.3
Android Java Technical Spec v1.1
Flutter Technical Spec      v1.1
Admin Web Technical Spec    v1.1
```

If the repository contains newer approved versions, use the newer versions.

## Critical constants

Use exact contract values:

```text
CONCURRENT_UPDATE
IDEMPOTENCY_KEY_REUSE

answerQuality >= 3 → isCorrect = true
answerQuality < 3  → isCorrect = false

eventId = one logical operation

MAX_DAILY_WORKLOAD_INCREASE_PERCENT = 20%
```

## Backend authority

Backend owns:

```text
SRS
nextReviewAt
forgetting risk
weakness
recommendation
daily workload
accuracy
quiz score
XP
streak
CEFR result
AI budget
```

Clients must not implement competing business algorithms.

## Idempotency

```text
same eventId + same endpoint + same request
→ replay stored response

same eventId + different endpoint/request/user
→ 409 IDEMPOTENCY_KEY_REUSE
```

For concurrent duplicate INSERT, application must handle the database duplicate-key conflict, reload the record, compare identity/hash, then replay or return 409. It must not become an accidental 500.

## Optimistic locking

Protected state:

```text
user_vocabulary_progress
streaks
```

Uses `version BIGINT` / JPA `@Version`.

Conflict code:

```text
CONCURRENT_UPDATE
```

## V1 offline boundary

V1 is online-first.

Offline may show read-only cached content where available.

Do not add:

```text
offline learning queue
offline SRS
automatic learning sync
cross-device conflict merge
```

without an explicit scope change.

## Security

Never commit or expose:

```text
passwords
JWT signing secrets
refresh tokens
LLM API keys
TTS keys
database credentials
OAuth private secrets
```

Backend enforces authorization. Frontend route hiding is not security.

## Change impact

For non-trivial changes report:

```text
Change
Why
Affected documents
Affected API
Affected database
Affected clients
Migration
Tests
Backward compatibility
```

## Standard workflow

```text
READ
↓
UNDERSTAND
↓
CROSS-CHECK
↓
PLAN
↓
IMPLEMENT / REVIEW
↓
TEST
↓
VERIFY CONTRACT
↓
UPDATE DOCS IF NEEDED
```

## Document Preservation Rule

When updating an existing document:

```text
preserve all approved existing content
do not shorten unrelated sections
do not summarize away technical details
make only requested/necessary dependent changes
```

The updated document should retain the original level of detail unless the project owner explicitly requests a reduction.

---

## Reconciled baseline execution rules

Read approved source documents in dependency order. The historical reconciliation pack under `docs/reconciliation/` documents why the changes were made, but the integrated baseline documents are authoritative.

Never invent or override these V1 rules:

```text
assessment-block-v1
sm2-ext-v1
weakness-rule-v1
forgetting-risk-v1
daily-plan-v1
gamification-v1
body eventId only
Daily Plan persisted snapshot
FCM notification contract
synchronous client-facing AI generation
```

When an implementation changes a cross-layer contract, update all downstream baseline documents and OpenAPI in the same change.
