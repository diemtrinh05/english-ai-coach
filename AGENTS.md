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

## Task Lifecycle and Git Naming

Every implementation or governance change must be traceable to one or more valid Task IDs from `docs/planning/MASTER_BACKLOG.md`.

### Task statuses

Use only these task statuses:

```text
TODO
READY
IN_PROGRESS
BLOCKED
IN_REVIEW
DONE
```

Normal lifecycle:

```text
TODO
↓
READY
↓
IN_PROGRESS
↓
IN_REVIEW
↓
DONE
```

A task may move to `BLOCKED` from `READY`, `IN_PROGRESS`, or `IN_REVIEW` when an unresolved dependency, contract ambiguity, failed required gate, or external blocker prevents progress.

A task must not be marked `DONE` until its acceptance criteria, required validation, and reviewer gates are satisfied.

### Branch naming

For a single-task branch, use:

```text
<type>/<TASK-ID>-<short-slug>
```

Examples:

```text
feat/BE-FND-001-spring-bootstrap
fix/BE-FND-008-idempotency-race
chore/CI-FND-001-ci-pipeline
docs/GOV-005-task-workflow
test/QA-FND-001-foundation-tests
```

For an explicitly approved milestone batch containing multiple closely related governance tasks, include the covered Task ID range:

```text
chore/GOV-001-GOV-007-m0-governance
```

Do not create generic implementation branches such as:

```text
feature/backend
fix/stuff
development
work
temp
```

### Commit naming

Each task-scoped commit must include its Task ID:

```text
<type>(<TASK-ID>): <description>
```

Examples:

```text
feat(BE-FND-001): bootstrap Spring Boot backend
fix(BE-FND-008): handle concurrent idempotency claims
test(QA-FND-001): add foundation integration tests
docs(GOV-005): define task lifecycle and Git naming
```

A commit covering an approved multi-task governance batch may reference the relevant Task IDs in the commit body, but implementation commits should normally remain task-scoped.

### Pull Request traceability

Every Pull Request must identify:

- the applicable Backlog Task ID(s);
- milestone;
- task status;
- dependency status;
- required reviewers;
- validation/test evidence.

PRs must not silently combine unrelated backlog tasks.

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

For concurrent claims, use PostgreSQL `INSERT ... ON CONFLICT (event_id) DO NOTHING`. If the claim is not inserted, load the existing `eventId`, compare `user_id` separately together with endpoint identity and the SHA-256 canonical `request_hash`, then replay the stored response or return `409 IDEMPOTENCY_KEY_REUSE`. Do not use unique-constraint exceptions as normal duplicate-claim control flow or continue work in a failed PostgreSQL transaction. The business mutation and successful idempotency result must commit atomically; expected duplicate races must not become HTTP 500.

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
