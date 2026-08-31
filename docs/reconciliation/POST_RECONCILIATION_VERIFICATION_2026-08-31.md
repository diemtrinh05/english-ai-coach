# English AI Coach — Post-Reconciliation Verification

**Date:** 2026-08-31  
**Baseline:** Integrated BR-001..BR-024  
**Result:** PASS  
**Purpose:** Verification record for the reconciled documentation baseline before implementation begins.

## 1. Canonical baseline verified

```text
SRS v1.2
→ Database Schema v1.6
→ System Architecture v1.3
→ AI Personalization v1.3
→ API Specification v1.4
→ OpenAPI v1.4
→ Technical Specification v1.2
→ Backend Technical Specification v1.3
→ Android Java Technical Specification v1.1
→ Flutter Technical Specification v1.1 (future V2 contract client)
→ Admin Web Technical Specification v1.1
```

UI/UX bindings verified against the reconciled contract, including Admin UI/UX v1.2, Admin High-Fidelity v1.1 and Admin Interactive Prototype v1.1.

## 2. BR decision integration

All decisions `BR-001..BR-024` have been integrated into their authoritative layers. The repository no longer relies on the reconciliation overlay for active implementation. The reconciliation document is retained under `docs/reconciliation/` as `ARCHIVED — INTEGRATED` provenance.

Key verified outcomes:

- Admin V1 stack is React + TypeScript + Vite.
- V1 locale policy is Vietnamese `vi-VN` with centralized user-facing resources.
- Adaptive Assessment is deterministic, persisted and retry-safe.
- SRS state transitions are deterministic.
- Weakness, forgetting risk, recommendation and workload policies are deterministic V1 rules.
- Daily Plan is a persisted snapshot with `daily_plan_items`.
- XP/Level/Badge/Streak rules are deterministic.
- FCM device registration and notification preferences are part of V1.
- `eventId` in request body is the sole logical-operation idempotency mechanism.
- PostgreSQL claim semantics use `INSERT ... ON CONFLICT DO NOTHING`.
- V1 client-facing AI generation is synchronous; no AI `202/jobId` contract remains.
- Admin AI CEFR Suggestion is Future/V2 and has no V1 route.
- `answer_quality`/`is_correct` integrity is enforced at DB level.
- Canonical Admin audit action identifiers are aligned.

## 3. Database verification

Database Schema v1.6 contains **34 unique table definitions**.

The previous v1.5 document metadata said 28, but the actual v1.5 content contained 29 tables because `idempotency_keys` was already defined. Reconciliation adds five tables:

```text
assessment_items
goal_topics
daily_plan_items
user_devices
notification_preferences
```

Therefore the correct reconciled count is:

```text
29 + 5 = 34
```

Verified DB invariants include:

```text
answer_quality SMALLINT NOT NULL
CHECK(answer_quality BETWEEN 0 AND 5)
CHECK(is_correct = (answer_quality >= 3))
notification reminder deduplication
idempotency_keys retention/claim design
daily_plan_items persistence
assessment state persistence
```

## 4. API/OpenAPI verification

OpenAPI verification result:

```text
OpenAPI version : 3.1.0
API info version: 1.4.0
Paths           : 72
Operations      : 76
Schemas         : 101
Broken internal $ref: 0
Duplicate operationId: 0
```

API Specification ↔ OpenAPI explicit operation parity:

```text
API Specification operations: 76
OpenAPI operations          : 76
Missing in OpenAPI          : 0
Extra in OpenAPI            : 0
Result                      : PASS
```

Contract checks verified:

- no active `Idempotency-Key` header component/reference;
- required `eventId` is present on all reconciled event mutations;
- no AI `AsyncJobResponse` or AI `202` response;
- device/token and notification-preference endpoints exist;
- `DailyPlanItem.vocabularyId` is nullable/not required for aggregate QUIZ items;
- canonical AI usage and personalized-exercise routes are used.

## 5. Documentation hygiene verification

Active baseline docs verify:

```text
literal AI drafting filecite artifacts: 0
broken canonical .md references: 0
duplicate numbered H1 sections: 0
stale Admin Spring-rendered stack declaration: 0
stale /api/v1/admin/ai-usage route: 0
stale POST /personalized-exercise route: 0
active Idempotency-Key contract: 0
non-canonical active Status metadata: 0
known stale baseline-version fingerprints: 0
```

Filename/version header consistency is checked by `tools/baseline_audit.py` for all primary technical baseline documents.

## 6. Automated audit command

Run from repository root:

```bash
python tools/baseline_audit.py
```

Expected result:

```text
BASELINE AUDIT: PASS
Canonical baseline files present; OpenAPI parses; BR contract checks passed.
```

## 7. Gate decision

**Documentation gate: BASELINE_CLEAN = PASS.**

The reconciled documentation set is ready to commit and may be used as the authoritative input for V1 implementation. This does not bypass the project workflow: implementation changes still require Architecture, Database, Security and QA reviewer passes before merge.
