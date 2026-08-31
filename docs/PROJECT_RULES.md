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
attempt INSERT
 ↓
duplicate-key conflict?
 ↓
catch/inspect database conflict
 ↓
reload eventId
 ↓
compare user/endpoint/requestHash
 ↓
replay or 409
```

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

Recommended:

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

Use:

```text
feat:
fix:
refactor:
test:
docs:
chore:
```

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
