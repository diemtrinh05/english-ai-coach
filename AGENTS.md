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

### Task statuses and workflow precedence

Use only these task statuses:

```text
TODO
READY
IN_PROGRESS
BLOCKED
IN_REVIEW
DONE
```

For tasks started after the `GOV-009` effective point, the normal lifecycle is:

```text
TODO
↓
IN_PROGRESS
↓
DONE
```

`BLOCKED` may be used from `IN_PROGRESS` only when an actual blocker exists. `READY` and `IN_REVIEW` remain supported historical/legacy states and are not required for new direct-main tasks.

A task must not be marked `DONE` until its acceptance criteria, required validation, and reviewer gates are satisfied.

`docs/PROJECT_RULES.md` is the highest-priority workflow truth. `GOV-009` supersedes branch/PR requirements only for tasks started after its effective point. Historical rules/evidence remain immutable, and grandfathered tasks continue their original workflow.

### GOV-009 effective point and direct-main workflow

The new workflow becomes effective only after `GOV-009` has Architecture Reviewer PASS, QA Reviewer PASS, unresolved findings = `NONE`, status `DONE`, and its commit is successfully pushed to `main`. Uncommitted GOV-009 changes are not the effective point.

`OWNER_AUTHORIZED_GOVERNANCE_TRANSITION` is the one-time closure mode for `GOV-009` only. It is not `PRE_CI_BOOTSTRAP_NA` or `CI PASS`, does not modify the PRE_CI eligible list, cannot be reused, expires permanently after the GOV-009 push, and cannot waive a failing CI check. `GOV-009` may move `IN_PROGRESS → DONE` only with confirmed owner authorization, AR PASS, QAR PASS, unresolved findings = `NONE`, acceptance/governance consistency PASS, baseline audit PASS, py_compile PASS, `git diff --check` PASS, scope/secret/generated-file checks PASS, baseline-tag integrity PASS, and existing failing CI check = `NONE`.

For tasks started after that point:

```text
PLAN → IMPLEMENT → TEST → REVIEW → DONE → ONE FINAL COMMIT ON MAIN → PUSH MAIN
```

- No task branch or Pull Request is required by default.
- Only one direct-main task may be active at a time; a parked grandfathered task does not count.
- PLAN admission requires `main`, clean worktree, `main == origin/main`, no Git operation in progress, no other active direct-main task, task `TODO`, dependencies `DONE`, repository health not `BLOCKED`, documented sources/acceptance/scope/reviewers, and a CI mode valid for that exact task before `TODO → IN_PROGRESS`.
- IMPLEMENT and TEST keep the task `IN_PROGRESS`; do not commit or push.
- Reviewers are strictly read-only and review tracked plus untracked current-task changes in the uncommitted `main` worktree. They must not require a task branch or PR.
- Reviewer FAIL and remediation chronology must be preserved. Finalization requires all applicable tests/reviewers PASS and unresolved findings = `NONE`, then transitions `IN_PROGRESS → DONE` without committing.
- A normal task uses one final task-scoped commit after `DONE`; do not create separate implementation and closure commits.

CI-mode admission is authoritative as follows:

- Before `CI-FND-001` is effective, only a positively listed PRE_CI-eligible task with all PRE_CI admission gates (`PRE_CI_BOOTSTRAP_NA`), or `CI-FND-001` under `ACTUAL_CI_BOOTSTRAP`, may enter PLAN. Every non-eligible ordinary task uses `INVALID_BEFORE_CI` and admission is `BLOCKED`; `NOT_APPLICABLE` cannot substitute. `OWNER_AUTHORIZED_GOVERNANCE_TRANSITION` belongs only to GOV-009 and is not a later-task admission mode.
- After `CI-FND-001` is effective, ordinary tasks use `ACTUAL_CI_REPOSITORY_HEALTH` and admission requires repository health `HEALTHY` for the latest `origin/main`.
- Repository health states are `HEALTHY` (latest required CI PASS), `CI_PENDING` (required CI expected/incomplete), and `BLOCKED` (required CI FAIL or explicit published-main failure). `CI_PENDING` and repository `BLOCKED` both prohibit PLAN/`TODO → IN_PROGRESS`. This repository state is separate from task lifecycle `BLOCKED`.
- Every normal direct-main push after CI is effective immediately sets repository health `CI_PENDING`; only remote PASS restores `HEALTHY`, while FAIL sets `BLOCKED`. Do not start another task while CI is pending or failing.

When required remote CI fails for an already-published direct-main commit, use `PUBLISHED_MAIN_RECOVERY`. This is an emergency operational state machine, not a backlog task and not PLAN admission. The originating task remains `DONE`; distinguish `TASK STATUS`, `REPOSITORY HEALTH`, and `PUBLISHED_MAIN_RECOVERY STATUS`. Recovery states are `OPEN`, `VALIDATING`, `CI_PENDING`, `CLOSED`, with optional recovery `BLOCKED`.

Repository health `BLOCKED` continues to prohibit every normal task, but permits this one recovery exception when the blocker is a published-main CI failure. No unrelated task may start. Recovery must record the originating Task ID, failing `origin/main` SHA, failing check/run when available, failure summary, `FIX_FORWARD` or `REVERT`, affected tests/reviewers, recovery commit SHA, findings and final CI result. `OWNER_AUTHORIZED_GOVERNANCE_TRANSITION` and `PRE_CI_BOOTSTRAP_NA` are not recovery modes.

Before publishing recovery, affected TEST gates and affected independent reviewer gates must PASS and unresolved recovery findings must be `NONE`. Use only `fix(<TASK-ID>): recover published main after CI failure` or `revert(<TASK-ID>): revert failing published change`, with the originating Task ID, then fast-forward push `main`. The push sets both repository health and recovery to `CI_PENDING`; CI PASS sets `HEALTHY`/`CLOSED`, while another FAIL sets `BLOCKED`/`OPEN` and continues the same incident. This recovery commit and the `CI-FND-001` bootstrap are the only exceptions to `ONE TASK = ONE FINAL COMMIT`; never reset, rebase, force-push or rewrite published history.

During `CI-FND-001` bootstrap failure, keep that task `IN_PROGRESS` and remediate within it until actual CI PASS. General `PUBLISHED_MAIN_RECOVERY` applies only after `CI-FND-001` is effective, including a later failure of its closure commit; PRE_CI remains permanently expired.

### Pre-CI bootstrap gate

Trước khi `CI-FND-001` effective theo rule bên dưới, chỉ các task tiên quyết sau được phép ghi CI status là `PRE_CI_BOOTSTRAP_NA` thay cho `CI PASS`:

```text
GOV-008
BE-FND-001
BE-FND-002
DB-FND-001
DB-FND-002
BE-FND-004
BE-FND-005
BE-FND-007
QA-FND-001
QA-FND-002
```

`PRE_CI_BOOTSTRAP_NA` chỉ hợp lệ khi tất cả dependency đã `DONE`; acceptance criteria đã thỏa mãn; local/unit/integration/task tests bắt buộc đã `PASS` khi áp dụng; reviewer bắt buộc đã `PASS`; unresolved findings = `NONE`; `baseline_audit` đã `PASS`; build/static/git/diff validations áp dụng đã `PASS`; và không có CI check hiện hữu nào đang fail.

Grandfathered tasks preserve PR-level evidence. For new direct-main tasks, repository-level evidence replaces PR-level evidence and records the eligible Task ID, reason, dependency/acceptance/test/reviewer/finding/audit/build-static-git state, no known failing check, and `failed-check waiver = NOT USED`. The eligible task list is unchanged.

CI check đang fail không bao giờ được waive bằng `PRE_CI_BOOTSTRAP_NA`. `NOT_APPLICABLE` không được dùng thay cho ngoại lệ bootstrap này.

`CI-FND-001` không được dùng `PRE_CI_BOOTSTRAP_NA` hoặc `OWNER_AUTHORIZED_GOVERNANCE_TRANSITION`. It is the explicit two-commit exception: after PLAN/IMPLEMENT/TEST/REVIEW it remains `IN_PROGRESS`; a bootstrap implementation commit is pushed to `main`; actual CI must PASS; only then may it become `DONE`, followed by a small closure commit pushed to `origin/main`.

`CI-FND-001` becomes effective only after mandatory actual remote CI PASS on the bootstrap flow, `IN_PROGRESS → DONE`, and successful push of its closure commit to `origin/main`. At that instant PRE_CI expires permanently. CI for the closure/latest-main commit may still set repository health `CI_PENDING` or `BLOCKED`, but PRE_CI never reactivates and no new task may start until health is `HEALTHY`.

Ngay sau khi `CI-FND-001` `DONE` và effective trên `main`, `PRE_CI_BOOTSTRAP_NA` tự động hết hiệu lực. Legacy/grandfathered tasks retain their original CI-before-`DONE` semantics. For new direct-main tasks, pre-publish local/test/reviewer gates determine `DONE`; remote CI after push is a repository-health gate. PASS means repository/main = `HEALTHY` without a task lifecycle change. FAIL means repository/main = `BLOCKED`, but the already-`DONE` task is not rewritten; no new task may start until fix-forward or revert is pushed, affected tests/reviews rerun, and remote CI PASSes. A failed CI check is never waived. Gate thoát M1 luôn yêu cầu `CI PASS` thực tế.

### Branch naming (legacy/grandfathered)

When a legacy/grandfathered task uses a single-task branch, use:

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

### Pull Request traceability (legacy/grandfathered or explicitly required)

Every Pull Request must identify:

- the applicable Backlog Task ID(s);
- milestone;
- task status;
- dependency status;
- required reviewers;
- validation/test evidence.

PRs must not silently combine unrelated backlog tasks.

### Direct-main commit safety

A post-GOV-009 task may be committed to `main` only when task status is `DONE`, dependencies and scope are valid, applicable tests and required reviewers PASS, unresolved findings = `NONE`, baseline audit and `git diff --check` PASS, secret/generated-file audits PASS, and baseline tags are unchanged. Never force-push, rebase published `main`, use destructive cleanup/reset, rewrite history, mutate baseline tags, commit known failures/findings, or combine unrelated tasks. Repair published defects by fix-forward or `git revert`.

`BE-FND-003` and PR #5 are explicitly grandfathered: the existing OPEN PR, branch, lifecycle/status, and AR/SR/QAR evidence remain unchanged; actual CI remains required; `PRE_CI_BOOTSTRAP_NA` remains not eligible; do not cherry-pick it to `main` solely because `GOV-009` exists.

Installed Codex skill migration is pending and must occur only in a separate phase after `GOV-009` is effective.

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
