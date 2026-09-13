# PROJECT_RULES.md — English AI Coach

**Project:** English AI Coach  
**Document:** Project Rules for AI Coding Agents  
**Version:** 1.2  
**Status:** APPROVED BASELINE  
**Date:** 2026-08-31  

---

# 1. Purpose

This file defines the rules that **Codex, Antigravity, and any other AI coding agent** must follow when working on English AI Coach.

The project contains multiple specifications covering:

```text
Requirements
Architecture
Database
AI Personalization
API
OpenAPI
Mobile UI/UX
Mobile Design
Mobile Prototype
Admin UI/UX
Admin Design
Admin Prototype
Technical Specifications
```

The purpose is to prevent:

```text
contradictory implementations
invented business rules
API/DB mismatch
client/backend drift
silent scope expansion
```

---

# 2. Core Principle

> **Documentation is the source of truth. Code must implement the approved specifications, not invent a new product.**

Before implementing a feature:

```text
Read relevant documentation
        ↓
Cross-check dependencies
        ↓
Identify contradictions/gaps
        ↓
Explain proposed resolution
        ↓
Implement only after the contract is clear
```

Never skip the cross-check for security-, data-, API-, SRS-, concurrency-, or AI-related changes.

---


---

# Baseline Integration Rule

The BR-001..BR-024 reconciliation decisions dated 2026-08-31 are integrated into the approved baseline documents. `docs/reconciliation/English_AI_Coach_Baseline_Reconciliation_v1.0.md` is retained as **ARCHIVED — INTEGRATED** history and must not override newer approved baseline documents.

Canonical dependency chain:

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
→ Flutter Technical Specification v1.1 (contract-only future client)
→ Admin Web Technical Specification v1.1
```

If two approved baseline documents appear to conflict, **do not silently reconcile**. Follow the dependency chain, stop the affected implementation area, and raise a contradiction report.

---

# 3. Language Policy

## 3.1. AI Agent Response Language

All AI agents working on this project, including **Codex, Antigravity, and review agents**, must respond to the project owner in **Vietnamese**.

This applies to:

```text
plans
explanations
implementation reports
review reports
test reports
bug reports
summaries
error explanations
```

Technical identifiers must remain exactly as defined by the project.

Examples:

```text
CONCURRENT_UPDATE
IDEMPOTENCY_KEY_REUSE
eventId
user_vocabulary_progress
/api/v1/learning/attempts
LearningAttemptService
```

Do not translate or rename technical contract identifiers.

---

## 3.2. Code Comment Language

Comments that explain implementation logic must be written in **Vietnamese**.

Example:

```java
// Kiểm tra eventId trước khi xử lý để tránh submit cùng một thao tác nhiều lần.
```

Keep code identifiers in their official technical form:

```text
class names
method names
variable names
package names
API paths
JSON fields
database/table/column names
enum values
error codes
```

Do not rename technical identifiers merely to satisfy the Vietnamese-comment rule.

---

## 3.3. User Interface Language

All **learner-facing UI** and **Admin Web UI** must display **Vietnamese by default**.

Examples:

```text
Home → Trang chủ
Learn → Học
Review → Ôn tập
Progress → Tiến độ
Profile → Cá nhân

Users → Người dùng
Vocabulary → Từ vựng
AI Content → Nội dung AI
Statistics → Thống kê
Audit Logs → Nhật ký hoạt động
```

The English learning material itself may remain English where appropriate.

Example:

```text
Word:
negotiate

Meaning:
đàm phán

Example:
We need to negotiate the contract.
```

---

## 3.4. Error Message Language

Official API/system error codes must remain unchanged.

Examples:

```text
CONCURRENT_UPDATE
IDEMPOTENCY_KEY_REUSE
```

User-facing messages should be Vietnamese.

Example:

```text
CONCURRENT_UPDATE
→ Dữ liệu đã thay đổi trên máy chủ. Vui lòng tải lại và thử lại.
```

Never replace an official error code with a translated identifier.

---

## 3.5. Localization Rule

V1 is a **single-locale Vietnamese (`vi-VN`)** product. There is no locale switcher, Accept-Language negotiation, or runtime language switching in V1.

User-facing text must still be centralized:

```text
Android V1 → res/values/strings.xml
Admin Web V1 → typed Vietnamese messages/resource module
Flutter V2 → Flutter localization mechanism when V2 is implemented
```

Do not duplicate the same user-facing message throughout business logic.


## 3.6. Documentation Language

Documentation may contain English technical terminology, code, identifiers, API examples, SQL, and diagrams.

New explanatory prose created by agents should be Vietnamese unless the document's established format requires otherwise.

Do not translate technical identifiers, API contracts, database identifiers, enum values, or error codes.


# 4. Official Documentation Hierarchy

## 4.1. Product Requirements

```text
docs/requirements/
```

Primary source for:

```text
scope
functional requirements
business requirements
non-functional requirements
acceptance criteria
```

Official file:

```text
English_AI_Coach_SRS_v1.2.md
```

---

## 4.2. System Architecture

```text
docs/architecture/
```

Primary source for:

```text
component boundaries
deployment architecture
system-level decisions
integration boundaries
security architecture
resilience principles
```

Official file:

```text
English_AI_Coach_System_Architecture_v1.3.md
```

---

## 4.3. Database

```text
docs/database/
```

Primary source for:

```text
tables
columns
constraints
indexes
relationships
migration expectations
```

Official file:

```text
English_AI_Coach_Database_Schema_v1.6.md
```

Database constraints are part of data integrity and must not be silently removed.

---

## 4.4. AI Personalization

```text
docs/ai/
```

Primary source for:

```text
SRS
forgetting risk
weak word detection
recommendation
difficulty adjustment
daily plan
AI personalization boundaries
```

Official file:

```text
English_AI_Coach_AI_Personalization_Specification_v1.3.md
```

---

## 4.5. API

```text
docs/api/
```

Primary source for:

```text
endpoint paths
HTTP methods
request fields
response fields
status codes
error codes
pagination conventions
authentication requirements
```

Official files:

```text
English_AI_Coach_API_Specification_v1.4.md
English_AI_Coach_OpenAPI_Swagger_v1_4.md
```

When API Specification and OpenAPI appear inconsistent, **do not guess**. Report the mismatch before implementation.

---

## 4.6. Client UX/UI

```text
docs/mobile/
docs/flutter/
docs/admin/
```

These control:

```text
screen structure
navigation
visual behavior
interaction states
user-facing copy
component behavior
```

Technical implementation may differ by framework, but product semantics must remain consistent.

---

## 4.7. Technical Specifications

```text
docs/technical/
```

These define:

```text
implementation patterns
project structure
security implementation
testing strategy
deployment
framework-specific technical decisions
```

Current official files:

```text
English_AI_Coach_Technical_Specification_v1.2.md
English_AI_Coach_Backend_Technical_Specification_v1.3.md
```

---

# 5. Version Rule

Always use the **highest approved version** of a document in the repository.

Do not implement from:

```text
old draft
duplicate file
obsolete version
```

unless the task explicitly asks for historical comparison.

Example:

```text
Database v1.6
→ use v1.6

API v1.4
→ use v1.4
```

Do not mix an old version from one document with a newer dependent document without reporting it.

---

# 6. Before Coding

Every AI agent must first inspect:

```text
docs/PROJECT_RULES.md
```

then identify the relevant documents.

For a feature, create a dependency map such as:

```text
Learning Attempt
├── SRS
├── Database
├── API
├── OpenAPI
├── Backend Technical Spec
└── Android Technical Spec
```

---

# 7. No Silent Invention

Do not invent:

```text
database columns
API endpoints
request fields
response fields
error codes
business rules
SRS formulas
AI algorithms
security behavior
product screens
```

when the documentation does not support them.

If something is missing:

```text
STOP
→ report gap
→ propose options
→ wait for decision when the change affects contract/scope
```

For trivial implementation details that do not alter product semantics, use standard engineering judgment and document the choice in code/PR.

---

# 8. No Silent Reconciliation

If two documents conflict:

```text
DO NOT choose one silently.
```

Example:

```text
API says:
CONCURRENT_UPDATE

Client document says:
OPTIMISTIC_LOCK_CONFLICT
```

Correct action:

```text
report mismatch
identify authoritative contract
update dependent document/code
```

---

# 9. API Contract Rule

The API contract is shared by:

```text
Android Java V1
Flutter V2
Admin Web
```

The backend owns implementation.

API contract:

```text
OpenAPI
+
API Specification
```

must remain synchronized with:

```text
Controller DTOs
client DTOs/types
tests
```

---

# 10. Error Code Rule

Error codes are **contract identifiers**, not descriptive labels.

Examples:

```text
CONCURRENT_UPDATE
IDEMPOTENCY_KEY_REUSE
```

Do not replace them with synonyms.

Client code must match the exact server contract.

---

# 11. Idempotency Rules

`eventId` represents:

```text
one logical client operation
```

not:

```text
one HTTP request
```

If a request times out:

```text
retry
→ use the SAME eventId
```

Never generate a new event ID merely to bypass an error.

Official behavior:

```text
same eventId
+
same endpoint
+
same request hash
→ replay stored response

same eventId
+
different endpoint/request hash/user
→ 409 IDEMPOTENCY_KEY_REUSE
```

Storage:

```text
idempotency_keys
```

---

# 12. Optimistic Locking Rules

Protected state includes:

```text
user_vocabulary_progress
streaks
```

Use:

```text
version BIGINT
@Version
```

Expected API conflict code:

```text
CONCURRENT_UPDATE
```

Client/backend must not silently overwrite newer server state.

---

# 13. Learning Attempt Rules

The request source of truth is:

```text
answerQuality
```

Client does not send:

```text
isCorrect
```

Backend derives:

```text
answerQuality >= 3 → isCorrect = true
answerQuality < 3  → isCorrect = false
```

Learning attempt processing must protect:

```text
SRS
session_attempt
progress
XP
streak
```

against duplicate processing.

---

# 14. Backend Authority Rule

Backend is authoritative for:

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
```

Clients may display or locally animate results, but must not create a competing business algorithm.

---

# 15. Database Rule

Use:

```text
PostgreSQL
+
Flyway
+
JPA/Hibernate
```

Database schema changes require:

```text
migration
documentation synchronization
tests
```

Never manually modify a production/shared schema outside the approved migration process.

---

# 16. Migration Rule

Once a Flyway migration is applied to a shared environment:

```text
DO NOT EDIT IT
```

Create a new migration.

Example:

```text
V30__add_ai_content_review_note.sql
V31__add_idempotency_keys.sql
```

---

# 17. Transaction Rule

Critical business mutations must have explicit transaction boundaries.

Learning attempt:

```text
BEGIN
 ↓
idempotency
 ↓
load state/version
 ↓
SRS
 ↓
persist attempt
 ↓
progress
 ↓
XP/streak
 ↓
response snapshot
 ↓
COMMIT
```

Failure:

```text
ROLLBACK
```

---

# 18. Idempotency Race Rule

The following is **not sufficient**:

```text
findById(eventId)
→ not found
→ insert
```

Race-safe mechanism:

```text
INSERT ... ON CONFLICT (event_id) DO NOTHING
 ↓
claim inserted?
 ├─ yes → execute business mutation
 │        → persist successful response snapshot
 │        → commit atomically
 └─ no  → load existing eventId
          → compare user_id separately + endpoint + SHA-256 canonical request_hash
          → same logical request: replay stored response
          → different logical request: 409 IDEMPOTENCY_KEY_REUSE
```

Do not use unique-constraint exceptions as normal duplicate-claim control flow. Do not catch a duplicate-key exception and continue work in the same failed PostgreSQL transaction. A business failure rolls back the claim together with the mutation; expected duplicate races must not escape as HTTP 500.

Do not allow concurrent duplicate requests to become accidental HTTP 500 errors.

---

# 19. AI Rules

AI must be accessed through backend abstractions.

Correct:

```text
Client
 ↓
Spring Boot
 ↓
AI provider abstraction
 ↓
LLM/TTS
```

Incorrect:

```text
Android → LLM directly
Flutter → LLM directly
Admin Web → LLM directly
```

Never ship:

```text
LLM API key
TTS API key
database credentials
JWT signing secret
```

inside client applications.

---

# 20. Reusable AI Content

Reusable content:

```text
EXAMPLE
EXPLANATION
MNEMONIC
STORY
reusable quiz content
```

Flow:

```text
Generate
 ↓
Validate
 ↓
PENDING_REVIEW
 ↓
Admin Review
 ├── APPROVED
 └── REJECTED
```

Reject:

```text
reason
→ ai_generated_content.review_note
→ admin_audit_logs.details.reason
```

---

# 21. Personalized AI Content

Personalized/ephemeral exercise:

```text
Personalization
 ↓
LLM
 ↓
Validation
 ↓
Safety Filter
 ↓
User
```

Do not put every personalized exercise into manual Admin Review.

---

# 22. AI Budget Rule

Every billable AI operation must pass through:

```text
AiBudgetGuard
```

Check:

```text
daily requests
daily tokens
daily cost
feature limits
```

If blocked:

```text
controlled failure
+
usage/audit logging where applicable
+
fallback where defined
```

Do not bypass budget checks from the client.

---

# 23. Security Rule

Security must be enforced on the backend.

Frontend route protection is not authorization.

Backend must enforce:

```text
JWT authentication
role authorization
resource ownership
input validation
rate limiting where configured
```

---

# 24. Authentication Rule

Official authentication:

```text
JWT access token
+
refresh token
```

Minimum:

```text
expiry
revocation
secure storage
```

Refresh-token rotation is future enhancement unless explicitly enabled.

---

# 25. Role Rule

Roles:

```text
USER
ADMIN
```

Admin endpoints:

```text
/api/v1/admin/**
```

require:

```text
ADMIN
```

---

# 26. Client Boundary Rule

Clients are:

```text
presentation + client orchestration
```

Clients are not:

```text
business authority
database authority
AI provider
```

---

# 27. Android Rules

Android Java V1:

```text
Fragment
 ↓
ViewModel
 ↓
UseCase
 ↓
Repository
 ↓
Remote Data Source
 ↓
Retrofit/HTTP
```

Do not:

```text
Fragment → Retrofit
Fragment → DAO
ViewModel → database directly
```

---

# 28. Android Offline Rule

V1 is:

```text
ONLINE-FIRST
```

Offline allows:

```text
read cached vocabulary/content where available
```

Offline does not allow:

```text
learning attempt submission
quiz submission
offline SRS
automatic sync
conflict merge
```

---

# 29. Android Event ID Rule

For mutation retry:

```text
logical action
→ one eventId
```

Timeout:

```text
retry
→ same eventId
```

New logical action:

```text
new eventId
```

---

# 30. Flutter Rules

Flutter V2 must reuse:

```text
same backend API
same OpenAPI contract
same business semantics
same authentication model
same design semantics
```

Flutter must not create a parallel SRS/recommendation/AI engine.

---

# 31. Admin Web Rules

Admin Web:

```text
React + TypeScript
```

uses:

```text
Spring Boot API
```

Do not:

```text
access PostgreSQL directly
call LLM directly
reimplement SRS
```

Sensitive mutations must respect:

```text
authorization
confirmation
audit
```

---

# 32. UI Consistency Rule

Mobile and Admin may use different visual systems.

Shared:

```text
product semantics
status meaning
accessibility principles
brand identity
API behavior
```

Do not force mobile navigation onto Admin Web.

---

# 33. Loading / Empty / Error Rule

Network-driven UIs should define:

```text
Initial
Loading
Success
Empty
Error
Offline where applicable
```

Mutations:

```text
Idle
Submitting
Success
Failure
```

---

# 34. Retry Rule

Safe reads may retry transient failures.

Mutations must not be blindly retried.

For idempotent logical learning actions:

```text
retry
→ same eventId
```

---

# 35. No Duplicate Business Logic

Never duplicate between clients/backend:

```text
SRS formula
forgetting risk
weak-word formula
daily workload
XP
streak
recommendation
AI budget
```

There must be one authoritative implementation.

---

# 36. Testing Rule

Critical features require tests.

Minimum:

```text
Unit
Integration
API contract
Concurrency
Idempotency
```

Important domains:

```text
Auth
Learning Attempt
SRS
Daily Plan
AI Budget
AI Review
Admin Authorization
```

---

# 37. Concurrency Testing

For:

```text
user_vocabulary_progress
streaks
idempotency_keys
```

test concurrent requests.

Expected:

```text
one logical state transition
```

not:

```text
silent overwrite
duplicate side effect
HTTP 500 from expected duplicate race
```

---

# 38. AI Testing

Test:

```text
valid response
malformed response
missing fields
unsafe response
provider timeout
provider 5xx
budget exceeded
duplicate generation
```

---

# 39. API Contract Testing

Whenever API changes:

```text
Update API Specification
Update OpenAPI
Update backend DTO/controller
Update client types
Update tests
```

Do not change only one side.

---

# 40. UI/API Mapping Rule

When implementing a screen:

```text
Screen
 ↓
API endpoint
 ↓
UseCase
 ↓
Repository
```

must be traceable.

---

# 41. File/Folder Rule

Official documentation structure:

```text
docs/
├── PROJECT_RULES.md
├── requirements/
├── architecture/
├── database/
├── ai/
├── api/
├── mobile/
├── flutter/
├── admin/
└── technical/
```

---

# 42. Agent Ownership

Recommended responsibility:

```text
Codex
→ Backend
→ Database
→ API
→ AI
→ tests
→ integration contracts

Antigravity
→ Android
→ Admin Web
→ UI implementation

Flutter
→ later V2 client
```

Agents may inspect the full repository but should modify primarily within their assigned responsibility unless explicitly instructed otherwise.

---

# 43. Cross-Agent Contract

When Codex changes:

```text
API
DB
business behavior
error code
```

dependent client code/documentation must be reviewed.

When a client agent finds a backend contract problem:

```text
report
→ do not invent a client workaround that contradicts the contract
```

---

# 44. Branch Rule

For tasks started after the `GOV-009` effective point, work directly on `main`;
no task branch is required by default. Section 59.2 defines the mandatory
PLAN and safety gates.

The following branch model is LEGACY / GRANDFATHERED guidance only:

```text
main
develop
feature/*
```

Examples:

```text
feature/auth
feature/vocabulary
feature/learning-attempt
feature/srs
feature/ai
feature/admin
```

---

# 45. Commit Rule

Every task-scoped commit uses a type and Task ID:

```text
<type>(<TASK-ID>): <description>
```

For a normal post-`GOV-009` direct-main task, one final commit is created only
after status `DONE`. Legacy/grandfathered branch commits retain their original
workflow. `CI-FND-001` is the explicit two-commit exception in Section 59.2.

---

# 46. No Scope Creep

Current V1 excludes:

```text
Speaking
Writing
Listening
Reading
AI Tutor
Advanced ML recommender
AI notification optimization
Full offline-first sync
Cross-device conflict merge
Microservices
```

Do not add these without an explicit product decision.

---

# 47. Feature Flag Rule

Future/optional features may use feature flags.

Never use feature flags to bypass:

```text
authorization
security
data integrity
```

---

# 48. Dependency Change Rule

If a change affects multiple documents, update the dependency chain.

Example:

```text
DB change
→ Database
→ Backend
→ API if exposed
→ OpenAPI
→ client types
→ tests
```

Example:

```text
error code change
→ API
→ OpenAPI
→ Android
→ Flutter
→ Admin
→ tests
```

---

# 49. Review Rule

Before marking a feature complete:

```text
Requirements
Database
API
OpenAPI
Backend
Client
Tests
```

must be reviewed for the feature.

---

# 50. Change Impact Template

For non-trivial changes:

```text
Change:
Why:
Affected documents:
Affected API:
Affected database:
Affected clients:
Migration:
Tests:
Backward compatibility:
```

---

# 51. Contradiction Report Template

```text
CONTRADICTION

Document A:
...

Document B:
...

Conflict:
...

Impact:
...

Recommended resolution:
...

Files that must be updated:
...
```

---

# 52. Implementation Planning Rule

For a new feature, first identify:

```text
Files to create
Files to modify
APIs used
Database entities/tables
Business services
Tests
```

Then implement.

For large/high-risk features:

```text
plan
→ review
→ code
```

---

# 53. Code Generation Rule

Generated code must:

```text
compile
follow project structure
follow naming conventions
include validation
handle errors
include tests
```

Do not create fake-complete placeholder code unless explicitly marked.

---

# 54. Secret Rule

Never commit:

```text
.env with secrets
API keys
JWT secrets
OAuth secrets
database passwords
private certificates
```

Use environment variables or secret management.

---

# 55. Logging Rule

Never log:

```text
password
JWT
refresh token
API key
database credentials
sensitive private payload
```

---

# 56. Production Readiness Rule

Before release:

```text
HTTPS
secure secrets
debug logs disabled
database migrations verified
backup strategy
health checks
critical tests
```

---

# 57. Performance Rule

Avoid:

```text
N+1 queries
unbounded list APIs
loading entire datasets
main-thread network/database work
client-side aggregation of huge datasets
```

Use:

```text
pagination
projections
caching where appropriate
background/asynchronous processing
```

---

# 58. Documentation Update Rule

When implementation reveals a real specification gap:

```text
do not hide the deviation in code
```

Instead:

```text
identify gap
→ update relevant document
→ update dependent documents
→ implement final contract
```

---

# 59. Acceptance Before Completion

A feature is not "done" because the code compiles.

It is done when:

```text
requirements satisfied
+
contract satisfied
+
security satisfied
+
tests pass
+
UI state handled
+
documentation synchronized
```

## 59.1. Pre-CI Bootstrap Gate

Trước khi `CI-FND-001` effective theo Section 59.2, chỉ các task tiên quyết sau được phép dùng CI status `PRE_CI_BOOTSTRAP_NA` thay cho `CI PASS`:

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

`PRE_CI_BOOTSTRAP_NA` chỉ hợp lệ khi:

```text
all dependencies are DONE
acceptance criteria are satisfied
required local/unit/integration/task tests PASS as applicable
required reviewers PASS
baseline_audit PASS
applicable build/static/git/diff validations PASS
unresolved reviewer findings = NONE
the applicable repository or grandfathered PR evidence records the eligible Task ID, reason, and local validation evidence
no existing CI check is failing
failed-check waiver = NOT USED
```

CI check đang fail không bao giờ được waive bằng `PRE_CI_BOOTSTRAP_NA`. `NOT_APPLICABLE` không được dùng thay cho ngoại lệ bootstrap này.

Task grandfathered tiếp tục dùng PR-level evidence theo workflow lịch sử. Với task direct-main bắt đầu sau effective point của `GOV-009`, repository-level evidence thay PR-level evidence và phải ghi Task ID, dependency/acceptance/tests/reviewer/finding/audit/build-static-git state, xác nhận không có known failing CI check, `failed-check waiver = NOT USED`, và lý do `CI-FND-001` chưa effective. Danh sách eligible task ở trên không thay đổi.

`CI-FND-001` không được dùng `PRE_CI_BOOTSTRAP_NA` hoặc `OWNER_AUTHORIZED_GOVERNANCE_TRANSITION`. Task này là ngoại lệ hai commit: sau PLAN/IMPLEMENT/TEST/REVIEW vẫn giữ `IN_PROGRESS`; bootstrap implementation commit được push lên `main`; actual CI phải `PASS`; sau đó mới chuyển `DONE` và tạo closure commit nhỏ.

Ngay sau khi `CI-FND-001` `DONE` và effective trên `main`, ngoại lệ tự động hết hiệu lực. Legacy/grandfathered tasks tiếp tục yêu cầu actual CI PASS trước `DONE` theo workflow gốc. Với direct-main task mới, các pre-publish gates quyết định `DONE`; actual remote CI chạy sau push như repository-health gate, không phải pre-`DONE` task gate. Gate thoát M1 luôn yêu cầu `CI PASS` thực tế.

## 59.2. GOV-009 Simplified Main-Branch Task Workflow

### Governance precedence and effective point

`PROJECT_RULES.md` remains the highest-priority project workflow truth. Historical rules and evidence remain historical; `GOV-009` supersedes branch/PR requirements only for tasks started after its effective point. `GOV-008` remains valid except that new direct-main PRE_CI tasks use repository-level evidence as defined in Section 59.1. Immutable baseline-tag rules and all product/API/database contracts remain unchanged.

`GOV-009` is a one-time transition explicitly authorized by the repository owner and intentionally implemented directly on `main`. The new workflow is not effective while its changes are merely uncommitted.

The only closure mode allowed for this transition is `OWNER_AUTHORIZED_GOVERNANCE_TRANSITION`. It applies only to `GOV-009`; it is neither `PRE_CI_BOOTSTRAP_NA` nor `CI PASS`; it does not change the PRE_CI eligible task list; it cannot be reused by another task; it expires permanently when the GOV-009 commit is pushed to `main`; and it cannot waive an existing failing CI check. `GOV-009` may transition `IN_PROGRESS → DONE` only when all of the following pass:

```text
repository-owner authorization = CONFIRMED
Architecture Reviewer = PASS
QA Reviewer = PASS
unresolved findings = NONE
acceptance/governance consistency validation = PASS
baseline_audit = PASS
py_compile = PASS
git diff --check = PASS
scope/secret/generated-file checks = PASS
baseline tag integrity = PASS
existing failing CI check = NONE
```

Its official effective point then requires all of:

```text
Architecture Reviewer PASS
QA Reviewer PASS
unresolved findings = NONE
GOV-009 status = DONE
GOV-009 commit successfully pushed to main
```

Until that push succeeds, no normal product task may start under the new workflow. Installed Codex skill migration is pending and must occur in a separate phase after this effective point.

### New default lifecycle and workflow

For tasks started after the effective point:

```text
TODO → IN_PROGRESS → DONE

PLAN → IMPLEMENT → TEST → REVIEW → DONE
     → ONE FINAL COMMIT ON MAIN → PUSH MAIN
```

`BLOCKED` may be used from `IN_PROGRESS` only for an actual blocker. `READY` and `IN_REVIEW` remain supported historical/legacy states but are not required for new tasks. Only one direct-main task may be active at a time; a parked grandfathered task on an old branch does not count.

### Authoritative direct-main admission and CI-mode state machine

Every task started after the GOV-009 effective point must pass admission before entering PLAN or transitioning `TODO → IN_PROGRESS`. Common admission always requires: current branch = `main`; clean worktree; local `main == origin/main`; no Git operation in progress; no other post-GOV-009 direct-main task active; task = `TODO`; dependencies = `DONE`; repository health is not `BLOCKED`; and canonical sources, acceptance, scope/out-of-scope, and reviewer mapping are identified. Merely identifying a CI mode is insufficient—the mode must be valid for that exact task.

| Time / task class | CI mode | Admission |
|---|---|---|
| Before `CI-FND-001` effective: explicitly PRE_CI-eligible task | `PRE_CI_BOOTSTRAP_NA` | Allowed only when every common and PRE_CI admission gate is satisfied and the Task ID is positively present in the approved eligible list. |
| Before `CI-FND-001` effective: `CI-FND-001` | `ACTUAL_CI_BOOTSTRAP` | Allowed under its documented special bootstrap flow. |
| Before `CI-FND-001` effective: non-eligible ordinary task | `INVALID_BEFORE_CI` | `ADMISSION = BLOCKED`; wait until `CI-FND-001` is effective. `NOT_APPLICABLE` and PRE_CI inference are prohibited. |
| `GOV-009` transition only | `OWNER_AUTHORIZED_GOVERNANCE_TRANSITION` | One-time transition closure mode only; not reusable as later-task admission. |
| After `CI-FND-001` effective: ordinary new task | `ACTUAL_CI_REPOSITORY_HEALTH` | Allowed only when every common gate passes and repository health = `HEALTHY`. |

After `CI-FND-001` is effective, repository/main CI health has exactly these workflow states:

```text
HEALTHY    = latest required remote CI for current origin/main PASS
CI_PENDING = required remote CI for current origin/main exists/is expected but is incomplete
BLOCKED    = required remote CI for current origin/main FAIL, or another explicit published-main failure
```

`CI_PENDING` and repository health `BLOCKED` both prohibit PLAN and `TODO → IN_PROGRESS`. A repository-health `BLOCKED` state is distinct from task lifecycle `BLOCKED`, which remains available only for an `IN_PROGRESS` task with an actual task-level blocker. A repository failure never rewrites an already-`DONE` task to `BLOCKED`.

After every normal direct-main push once `CI-FND-001` is effective, repository health immediately becomes `CI_PENDING`. Remote CI PASS changes it to `HEALTHY`; remote CI FAIL changes it to `BLOCKED`. Another task cannot start while the latest `origin/main` CI is pending or failing.

### Published-main recovery state machine

`PUBLISHED_MAIN_RECOVERY` is a separate emergency operational state machine, not an executable backlog task and not normal PLAN admission. It exists only after required remote CI fails for an already-published direct-main commit. The originating task remains `DONE`; its previously satisfied pre-publish gates and reviewer history are preserved. A recovery incident must distinguish all three dimensions:

```text
TASK STATUS                    = DONE
REPOSITORY HEALTH              = BLOCKED
PUBLISHED_MAIN_RECOVERY STATUS = OPEN
```

Recovery states are `OPEN`, `VALIDATING`, `CI_PENDING`, `CLOSED`, with optional `BLOCKED` only when recovery itself cannot proceed. These states must never be written into the normal task lifecycle.

Normal PLAN admission remains prohibited while repository health is `BLOCKED` or `CI_PENDING`. The only exception is that `PUBLISHED_MAIN_RECOVERY` may begin when `BLOCKED` was caused by required remote CI failure on a published direct-main commit. This is recovery of the existing published incident, not admission of a new task; no unrelated task or `TODO → IN_PROGRESS` transition is allowed while recovery is open. Neither `OWNER_AUTHORIZED_GOVERNANCE_TRANSITION` nor `PRE_CI_BOOTSTRAP_NA` is a recovery mechanism.

Every recovery incident is traceable to:

```text
originating Task ID
failing origin/main commit SHA
failing CI run/check identity when available
failure summary
strategy = FIX_FORWARD | REVERT
recovery commit SHA after publication
affected TEST gates
affected reviewer gates
unresolved recovery findings
final remote CI result
```

The recovery flow is authoritative:

1. Required CI failure sets repository health `BLOCKED`; record the incident and set `PUBLISHED_MAIN_RECOVERY = OPEN`.
2. Select exactly `FIX_FORWARD` or `REVERT`; modify only what is necessary to recover the failure. Unrelated refactors, later-task work, feature expansion, V2/V3 scope and opportunistic cleanup are prohibited.
3. Set recovery `VALIDATING`; rerun every affected TEST gate, including at minimum the failed CI-equivalent check, applicable Maven/build/tests and integration/runtime validation, `baseline_audit`, applicable py_compile, `git diff --check`, secret/generated-file audit, scope audit and baseline-tag integrity.
4. Rerun only independent reviewers whose prior domain gate can be affected by the recovery diff. Database recovery requires DBR/QAR and AR when architecture is affected; security recovery requires SR/QAR and AR when architecture is affected; a build-only typo with no architecture/database/security impact may require only QAR when governance mapping permits. All required affected reviewer gates must PASS and unresolved recovery findings must be `NONE`; historical reviewer PASS/FAIL evidence remains intact.
5. Create a recovery commit directly on `main`, using the originating Task ID: `fix(<TASK-ID>): recover published main after CI failure` or `revert(<TASK-ID>): revert failing published change`. Push `main` fast-forward only.
6. Successful recovery push sets repository health and recovery status to `CI_PENDING`. Required remote CI runs for the recovery commit.
7. PASS sets repository health `HEALTHY` and recovery `CLOSED`; only then may normal PLAN admission resume. FAIL sets repository health `BLOCKED` and recovery `OPEN`; continue the same originating Task ID and incident chronology, without admitting unrelated work.

The recovery commit is a narrow exception to `ONE TASK = ONE FINAL COMMIT`, allowed only after an actual published-main CI failure. The only commit-count exceptions are the `CI-FND-001` bootstrap flow and this published-main recovery flow. Recovery must never bypass normal pre-publish completion gates.

`FIX_FORWARD` is preferred when the defect is small and safely recoverable. `REVERT` is also permitted, but never replace it with reset, rebase, force-push or history rewrite. A revert preserves the failing commit as Git evidence and references both the originating Task ID and failing commit. If the revert removes the capability delivered by the original task, record that acceptance is no longer present and the explicit planning consequence; any future replacement task may be created and admitted only after repository health returns `HEALTHY`.

During the `CI-FND-001` bootstrap, a bootstrap CI failure keeps `CI-FND-001` `IN_PROGRESS`; repository health becomes `BLOCKED`, and additional minimal bootstrap/fix commits may be made within that same task until actual CI PASS. This does not reopen a `DONE` task and does not use general `PUBLISHED_MAIN_RECOVERY`. After `CI-FND-001` becomes effective, any later published-main CI failure—including failure of its closure commit—uses `PUBLISHED_MAIN_RECOVERY`; `PRE_CI_BOOTSTRAP_NA` remains permanently expired.

`CI-FND-001` becomes effective only after its bootstrap implementation flow has produced mandatory actual remote CI PASS, the task has transitioned `IN_PROGRESS → DONE`, and its closure commit has been pushed successfully to `origin/main`. At that instant `PRE_CI_BOOTSTRAP_NA` expires permanently and can never reactivate. If CI for the closure commit/latest `origin/main` remains incomplete, PRE_CI stays expired and repository health = `CI_PENDING`; if it fails, PRE_CI stays expired and repository health = `BLOCKED` until fix-forward/revert remediation reaches remote CI PASS.

PLAN requires all applicable common admission conditions and the exact valid CI mode above. It performs `TODO → IN_PROGRESS`; it creates no branch and performs no commit/push.

IMPLEMENT changes only the current task, adds/updates applicable tests and documentation, preserves canonical contracts, keeps `IN_PROGRESS`, and performs no commit/push. TEST runs applicable build/unit/integration/runtime, PostgreSQL/Flyway, API/OpenAPI, baseline audit, py_compile, Git/diff/whitespace, secret/generated-output, scope-creep and tag-integrity gates. A failure returns to remediation and TEST.

REVIEW is independent and impact-based. Reviewers remain strictly read-only and inspect tracked plus untracked current-task files in the uncommitted `main` worktree; they must not require a branch or PR. Every FAIL/finding/remediation/re-review remains in chronological evidence. The task stays `IN_PROGRESS` until all required reviewers PASS and unresolved findings = `NONE`.

FINALIZE synchronizes evidence, CI mode and milestone progress, preserves historical findings/snapshots, then performs `IN_PROGRESS → DONE`. Finalization does not commit or push. Normal policy is `ONE TASK = ONE FINAL COMMIT`.

### Direct-main commit safety

A normal task may be committed directly to `main` only when task = `DONE`, dependency and current-task scope gates PASS, applicable TEST gates PASS, all required reviewers PASS, unresolved findings = `NONE`, baseline audit and `git diff --check` PASS, secret/generated-file audit PASS, and baseline tag integrity PASS. Then commit on `main` and push `origin main`.

Never force-push `main`, rebase published `main`, use `reset --hard` or destructive clean as normal remediation, rewrite published history, mutate baseline tags, commit unresolved findings/known failing tests, or combine unrelated tasks. A defective published commit must be fixed forward or reverted.

### Grandfathered tasks

Tasks and PRs started before the effective point retain their original workflow. `BE-FND-003` / PR #5 is explicitly grandfathered: its existing OPEN PR and branch remain valid; lifecycle/status and AR/SR/QAR evidence remain unchanged; actual CI remains required; `PRE_CI_BOOTSTRAP_NA` remains not eligible; and it must not be cherry-picked to `main` solely because `GOV-009` exists. It completes later through the legacy branch/PR workflow. Completed historical task/PR evidence is not rewritten.

### Post-CI direct-main behavior

After `CI-FND-001` becomes effective, normal tasks follow PLAN → IMPLEMENT → TEST → REVIEW → DONE → COMMIT → PUSH → actual remote CI repository-health verification. Because direct-main has no pre-merge remote execution point, remote CI occurs after push and is not a pre-`DONE` task gate.

If remote CI PASSes, repository/main health = `HEALTHY` and no task lifecycle change is required. If remote CI FAILs, repository/main health = `BLOCKED`; the already-`DONE` task is not rewritten to `BLOCKED`; no new task may start; the failure may never be waived; fix-forward or revert must begin immediately; affected TEST/reviewer gates must rerun; remediation must be pushed; and repository/main remains `BLOCKED` until remote CI PASSes. Never force-push or rewrite published history.

---

# 60. Final Agent Checklist

Before coding:

```text
[ ] Read PROJECT_RULES.md
[ ] Identify current document versions
[ ] Read SRS
[ ] Read relevant architecture
[ ] Read relevant DB/API docs
[ ] Check dependencies
[ ] Check contradictions
[ ] Plan implementation
```

During coding:

```text
[ ] No invented API
[ ] No invented DB field
[ ] No duplicated business logic
[ ] Security preserved
[ ] Transactions preserved
[ ] Idempotency preserved
[ ] Error codes exact
[ ] Tests added
```

Before completion:

```text
[ ] Build passes
[ ] Tests pass
[ ] Contract verified
[ ] Migration verified
[ ] Client/backend synchronized
[ ] Documentation updated
[ ] No secrets committed
```

---

# 61. Official V1 Contract Constants

Use these exact values where applicable:

```text
API:
/api/v1

Roles:
USER
ADMIN

Auth:
LOCAL
GOOGLE

AI reusable content:
PENDING_REVIEW
APPROVED
REJECTED

Learning status:
NEW
LEARNING
REVIEWING
MASTERED

Important 409 codes:
IDEMPOTENCY_KEY_REUSE
CONCURRENT_UPDATE

Learning correctness:
answerQuality >= 3 → isCorrect = true
answerQuality < 3  → isCorrect = false

Idempotency:
eventId = one logical operation

Workload:
MAX_DAILY_WORKLOAD_INCREASE_PERCENT = 20%

Idempotency retention:
30 days
```

---

# 62. Current Official Document Baseline

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

UX/design baseline:

```text
Mobile UI/UX                v1.1
Mobile Design/Wireframe     v1.2 FULL
Mobile High-Fidelity        v1.1
Mobile Interactive Prototype v1.1

Admin UI/UX                 v1.2
Admin Design/Wireframe      v1.0
Admin High-Fidelity         v1.1
Admin Interactive Prototype v1.1
```

If the repository contains a newer approved version, use the newer version.

---

# 63. Final Agent Behavior

Desired behavior:

```text
READ
 ↓
UNDERSTAND
 ↓
CROSS-CHECK
 ↓
REPORT CONTRADICTIONS
 ↓
PLAN
 ↓
IMPLEMENT
 ↓
TEST
 ↓
VERIFY CONTRACT
 ↓
UPDATE DOCS IF NEEDED
```

Not:

```text
READ ONE FILE
 ↓
ASSUME
 ↓
CODE
 ↓
CREATE NEW RULES
```

---

# 64. Final Principle

> **Preserve product consistency before optimizing implementation convenience.**

When implementation convenience conflicts with:

```text
requirements
API contract
database integrity
security
data consistency
```

the documented contract wins unless the project owner explicitly approves a change.

---

# Document Update Preservation Rule

When updating an existing project document:

```text
Preserve all existing approved content.
Do not summarize unrelated sections.
Do not remove sections merely to make the document shorter.
Do not rewrite unrelated sections.
Only make the requested changes and necessary dependent updates.
```

A new revision must be a **superset-preserving update** unless the project owner explicitly approves removal or restructuring.

---

# Reconciled Locked V1 Decisions

The following are hard rules for implementation:

```text
Admin Web = React + TypeScript + Vite; Spring Boot is API only.
V1 locale = vi-VN; resource centralization required; no runtime locale switching.
Assessment = deterministic block-based CEFR algorithm (`assessment-block-v1`).
SRS states = NEW / LEARNING / REVIEWING / MASTERED using `sm2-ext-v1`.
Personalization V1 = deterministic heuristics; no ML model training/inference.
Daily Plan = persisted daily snapshot with `daily_plan_items`; no mid-day re-ranking.
Notification V1 = FCM + device token + notification preferences.
Idempotency V1 = body `eventId` only; no idempotency HTTP header.
PostgreSQL idempotency claim = INSERT ... ON CONFLICT DO NOTHING.
Client-facing AI generation V1 = synchronous; no 202/jobId contract.
Admin AI CEFR Suggestion = Future/V2, not active V1.
answer_quality is required and backend derives is_correct; DB enforces consistency.
```

Canonical error codes remain `CONCURRENT_UPDATE` and `IDEMPOTENCY_KEY_REUSE`.
