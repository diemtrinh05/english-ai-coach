# Technical Specification v1.2 — English AI Coach

**Project:** English AI Coach  
**Document:** Technical Specification  
**Version:** 1.2  
**Status:** APPROVED BASELINE  
**Date:** 2026-08-31  

**Source documents:**
- `English_AI_Coach_SRS_v1.2.md`
- `English_AI_Coach_Database_Schema_v1.6.md`
- `English_AI_Coach_System_Architecture_v1.3.md`
- `English_AI_Coach_AI_Personalization_Specification_v1.3.md`
- `English_AI_Coach_API_Specification_v1.4.md`
- `English_AI_Coach_OpenAPI_Swagger_v1_4.md`
- `English_AI_Coach_App_UI_UX_Specification_v1.1.md`
- `English_AI_Coach_Design_System_Wireframe_v1.2_FULL.md`
- `English_AI_Coach_High_Fidelity_UI_Design_v1.1.md`
- `English_AI_Coach_Interactive_Prototype_Specification_v1.1.md`
- `English_AI_Coach_Admin_Web_UI_UX_Specification_v1.2.md`
- `English_AI_Coach_Admin_Design_System_Wireframe_v1.0.md`
- `English_AI_Coach_Admin_High_Fidelity_UI_Design_v1.1.md`
- `English_AI_Coach_Admin_Interactive_Prototype_Specification_v1.1.md`

---

# 1. Purpose

Tài liệu này chuyển các tài liệu requirements, architecture, API, AI và UI thành một baseline kỹ thuật để bắt đầu implementation.

Technical Specification chốt:

```text
Technology Stack
Project Structure
Module Boundaries
Backend Architecture
Mobile Architecture
Admin Architecture
Database Access
Security
Authentication
Authorization
Transaction Rules
Concurrency
Idempotency
SRS Processing
Personalization
AI Integration
Caching
Validation
Error Handling
Observability
Testing
Build
Deployment
Environment
Configuration
Development Sequence
Definition of Done
```

Mục tiêu:

```text
Specification
    ↓
Technical Design
    ↓
Implementation
    ↓
Integration Test
    ↓
System Test
    ↓
Release
```

---

# 2. Official Technology Stack

## 2.1. Mobile App V1

```text
Language:
Java

Platform:
Android

Architecture:
MVVM + Clean-ish layered architecture

Networking:
Retrofit + OkHttp

Serialization:
JSON

UI:
Android Views / Material components

State:
ViewModel + LiveData/StateFlow-equivalent project choice

Local storage:
Lightweight read-only cache for V1 where useful

Authentication:
JWT access token + refresh token

Push notification:
Firebase Cloud Messaging where enabled
```

The product contract must remain framework-independent for Flutter V2.

---

## 2.2. Mobile App V2

```text
Flutter
Dart

Reuse:
REST API
OpenAPI contract
Design tokens
UX flows
Business semantics
```

Do not duplicate backend business rules inside Flutter.

---

## 2.3. Backend

```text
Java
Spring Boot

Spring Web
Spring Security
Spring Data JPA
Hibernate
Bean Validation
Flyway
Actuator

Database:
PostgreSQL
```

Recommended supporting libraries:

```text
springdoc-openapi
JWT library
Testcontainers
JUnit 5
Mockito
```

---

## 2.4. Admin Web

Recommended baseline:

```text
Frontend:
React + TypeScript

Build:
Vite

UI:
Material UI or equivalent component library

HTTP:
Axios/fetch client

State:
Server state + local UI state

Charts:
A chart library compatible with React
```

Backend:

```text
Same Spring Boot backend
```

Admin Web is not a separate backend service in V1.

---

## 2.5. Infrastructure

Development:

```text
Docker Compose
```

Services:

```text
PostgreSQL
Backend
Optional Admin Web development server
```

Production/demo server may run the same services directly on a VM/server.

---

# 3. High-Level Technical Architecture

```text
                    ┌───────────────────────┐
                    │      Android App      │
                    │       Java V1         │
                    └───────────┬───────────┘
                                │ HTTPS / JSON
                                ▼
                    ┌───────────────────────┐
                    │   Spring Boot API     │
                    │                       │
                    │ Auth                  │
                    │ User                  │
                    │ Learning              │
                    │ SRS                   │
                    │ Personalization       │
                    │ Quiz                  │
                    │ Progress              │
                    │ Gamification          │
                    │ AI                    │
                    │ Admin                 │
                    └───────┬───────┬───────┘
                            │       │
                            │       └───────────────┐
                            ▼                       ▼
                  ┌────────────────┐      ┌────────────────┐
                  │  PostgreSQL    │      │ External AI   │
                  │                │      │ / TTS Provider │
                  └────────────────┘      └────────────────┘
                            ▲
                            │
                    ┌───────┴────────┐
                    │   Admin Web    │
                    │ React + TS     │
                    └────────────────┘
```

---

# 4. Architecture Style

Backend uses:

```text
Layered Modular Monolith
```

not microservices for V1.

Rationale:

```text
Lower deployment complexity
Simpler transaction boundaries
Easier debugging
Suitable for đồ án
Enough scalability for V1
```

Internal modules remain separated so they can be split later if needed.

---

# 5. Backend Module Boundaries

```text
auth
user
onboarding
assessment
vocabulary
learning
srs
personalization
quiz
progress
gamification
notification
ai
admin
audit
```

Dependency direction:

```text
Controller
   ↓
Application Service
   ↓
Domain
   ↓
Repository
   ↓
Infrastructure
```

Avoid:

```text
Controller → Repository direct
Controller → external AI direct
UI → database
```

---

# 6. Backend Package Structure

Recommended:

```text
com.example.englishaicoach
│
├── auth
│   ├── controller
│   ├── dto
│   ├── service
│   ├── security
│   └── mapper
│
├── user
│   ├── controller
│   ├── dto
│   ├── entity
│   ├── repository
│   ├── service
│   └── mapper
│
├── onboarding
├── assessment
├── vocabulary
├── learning
├── srs
├── personalization
├── quiz
├── progress
├── gamification
├── notification
├── ai
├── admin
├── audit
│
├── common
│   ├── exception
│   ├── response
│   ├── validation
│   ├── security
│   ├── idempotency
│   ├── clock
│   └── util
│
└── config
```

---

# 7. Controller Rules

Controllers:

```text
Receive request
Validate DTO
Authorize
Call application service
Return response
```

Controllers must not contain:

```text
SRS formulas
Recommendation algorithm
Database transaction logic
LLM prompt construction
Complex business rules
```

---

# 8. Service Rules

Services own business use cases.

Examples:

```text
CreateUserService
CompleteAssessmentService
GetTodayPlanService
SubmitLearningAttemptService
GeneratePersonalizedExerciseService
ApproveAiContentService
RejectAiContentService
```

Transactions belong at service/use-case boundaries.

---

# 9. DTO Rules

Never expose JPA entity directly over REST.

Use:

```text
Request DTO
Response DTO
Mapper
```

Example:

```text
LearningAttemptRequest
LearningAttemptResponse
```

---

# 10. Repository Rules

Use Spring Data JPA.

Repositories should:

```text
query
persist
paginate
lock where required
```

Repositories should not:

```text
calculate XP
derive SRS
call LLM
send push notification
```

---

# 11. Database

Official database:

```text
PostgreSQL
```

Versioned using:

```text
Flyway
```

Schema baseline:

```text
Database Schema v1.6
```

---

# 12. Database ID Strategy

Primary keys:

```text
UUID
```

Use application/server-generated UUIDs.

Rationale:

```text
distributed-safe
non-sequential public IDs
future multi-device compatibility
```

UUID does not imply offline-first support.

---

# 13. Database Transaction Boundary

Important transaction:

```text
POST /learning/attempts
```

Recommended transaction:

```text
BEGIN
 ↓
Load progress
 ↓
Validate version
 ↓
Calculate SRS
 ↓
Update user_vocabulary_progress
 ↓
Insert session_attempts
 ↓
Update session aggregates where applicable
 ↓
Update streak/XP if applicable
 ↓
COMMIT
```

If any critical step fails:

```text
ROLLBACK
```

Avoid partially written learning attempts.

---

# 14. Optimistic Locking

Entities requiring concurrency protection:

```text
UserVocabularyProgress
Streak
```

Add:

```text
version BIGINT
```

to both.

JPA:

```java
@Version
private Long version;
```

Behavior:

```text
Request A reads version 5
Request B reads version 5

Request A writes → version 6
Request B writes → conflict

→ HTTP 409
```

Client behavior:

```text
409
→ refresh latest state
→ inform/retry safely
```

---

# 15. Idempotency

Use a shared idempotency layer backed by:

```text
idempotency_keys
```

Do not store `event_id` in `session_attempts` for V1.

Applicable mutation endpoints:

```text
POST /learning/attempts
POST /quiz-attempts/{attemptId}/answers
POST /quiz-attempts/{attemptId}/complete
```

---

## 15.1. Event ID

Client sends:

```text
eventId = UUID
```

The same UUID represents one logical action.

Retry rule:

```text
timeout
→ resend same eventId
```

Never create a new event ID for a retry of the same logical operation.

---

## 15.2. Request Hash

Server computes:

```text
SHA-256(canonical request payload)
```

and stores:

```text
request_hash
```

This prevents the same event ID from being reused with different data.

---

## 15.3. Idempotency Table

```text
event_id
user_id
endpoint
request_hash
response_snapshot
response_status
created_at
```

Database:

```text
PRIMARY KEY(event_id)
```

Indexes:

```text
(user_id, created_at)
(endpoint, created_at)
```

---

## 15.4. Processing Rules

### First request

```text
eventId not found
→ insert key
→ execute business logic
→ store response
→ commit
```

### Duplicate same request

```text
eventId found
+
same endpoint
+
same hash
→ return stored response
```

### Key reuse

```text
eventId found
+
different endpoint/hash
→ HTTP 409
code = IDEMPOTENCY_KEY_REUSE
```

---

## 15.5. Response Snapshot

For successful logical operations:

```text
response body
+
HTTP status
```

may be stored in `response_snapshot` and `response_status`.

This allows safe replay without executing the business transaction again.

---

## 15.6. Transaction Boundary

The idempotency record and business mutation must be designed so a successful first operation cannot appear committed without a replayable result.

Recommended:

```text
BEGIN
 ↓
Claim eventId
 ↓
Execute business operation
 ↓
Store response
 ↓
COMMIT
```

Rollback:

```text
business failure
→ rollback
→ retry may execute again
```

---

## 15.7. Cleanup

```text
IDEMPOTENCY_KEY_RETENTION_DAYS = 30
```

A scheduled cleanup job removes expired keys.

---

## 15.8. Quiz idempotency

Quiz answers and completion also use idempotency because double-submit can otherwise:

```text
create duplicate answer records
change score unexpectedly
trigger duplicate progress effects
```

---

## 15.9. Auth refresh exception

`POST /auth/refresh` is not part of this event-id idempotency mechanism.

Refresh behavior is governed by:

```text
refresh token expiry
refresh token revocation
future token rotation
```

# 16. Answer Quality Source of Truth

Client sends:

```text
answerQuality
```

only.

Backend derives:

```text
answerQuality >= 3
→ isCorrect = true

answerQuality < 3
→ isCorrect = false
```

Client does not send:

```text
isCorrect
```

---

# 17. Learning Attempt Processing

Use case:

```text
SubmitLearningAttempt
```

Algorithm:

```text
1. Authenticate user
2. Validate session ownership
3. Validate vocabulary existence
4. Validate answerQuality 0–5
5. Check eventId idempotency
6. Load user_vocabulary_progress
7. Calculate SRS result
8. Persist progress
9. Persist session_attempt
10. Update relevant aggregates
11. Update streak/XP where applicable
12. Commit
13. Return derived result
```

---

# 18. SRS Integration

SRS logic must be centralized in:

```text
SrsService
```

or:

```text
SrsCalculator
```

Never duplicate formula in:

```text
Controller
Mobile
Admin
```

Input:

```text
answerQuality
easeFactor
interval
repetitions
```

Output:

```text
newEaseFactor
newInterval
newRepetitions
status
nextReviewAt
```

---

# 19. SRS Time Source

Backend must be authoritative for:

```text
now
nextReviewAt
```

Do not trust mobile device time for SRS calculations.

Use injectable clock:

```text
Clock
```

for testability.

---

# 20. Daily Plan

Use case:

```text
GetTodayPlan
```

Inputs:

```text
user
current date/time
timezone
daily learning minutes
progress
review queue
goals
CEFR
```

Output:

```text
newWordsTarget
reviewWordsTarget
quizTarget
estimatedMinutes
status
items
```

---

# 21. Daily Plan Rules

Core principle:

```text
Workload must respect user time budget.
```

Important configuration:

```text
BASELINE_DAILY_WORKLOAD
MAX_DAILY_WORKLOAD_INCREASE_PERCENT = 20%
MIN_DAILY_WORKLOAD
MAX_DAILY_WORKLOAD
```

Example:

```text
baseline = 10
max increase = 20%

maximum target = 12
```

High-performance examples must not exceed this configured incremental limit unless explicitly marked as an unbounded/ideal benchmark.

---

# 22. Recommendation Engine

V1:

```text
Rule-based
```

Signals:

```text
User goals
CEFR
Topic
Weak words
Review state
Recent performance
```

Example:

```text
Primary goal = Travel
+
CEFR = A2
+
Weak words = airport-related
→ recommend travel airport vocabulary
```

---

# 23. Weak Word Detection

V1 heuristic based on:

```text
incorrect count
correct count
response time
recent performance
```

Output can be:

```text
weaknessScore
priority
reason
```

Do not expose raw formula on learner UI.

---

# 24. Forgetting Risk V1

V1 uses heuristic approximation.

Inputs:

```text
time since last review
review history
incorrect count
interval
recent performance
```

Output:

```text
forgettingRisk
```

Used to prioritize review.

It is not presented as medically/scientifically guaranteed prediction.

---

# 25. Adaptive Difficulty

V1 uses rules:

```text
high completion + high accuracy
→ gradually increase workload/difficulty

low completion
or
low accuracy
→ reduce workload/difficulty
```

Workload increase remains bounded.

---

# 26. Personalized Exercise

Flow:

```text
POST /learning/personalized-exercise
        ↓
load user profile
        ↓
load weak words
        ↓
select context
        ↓
apply AI Budget Guard
        ↓
LLM
        ↓
validation
        ↓
safety filter
        ↓
response
```

---

# 27. AI Architecture

AI module contains:

```text
AiProvider
AiPromptBuilder
AiBudgetGuard
AiResponseValidator
AiSafetyFilter
AiContentService
AiUsageService
```

Interface:

```text
AiProvider.generate(...)
```

Allows provider replacement.

Do not couple business logic directly to vendor SDK.

---

# 28. AI Content Categories

## Reusable

```text
EXAMPLE
EXPLANATION
MNEMONIC
STORY
QUIZ
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
 ↓
APPROVED / REJECTED
 ↓
Cache
```

## Personalized / Ephemeral

```text
PERSONALIZED_EXERCISE
```

Flow:

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

No manual admin review per generated exercise.

---

# 29. AI Content Persistence

Table:

```text
ai_generated_content
```

Important fields:

```text
content_scope
content_type
generation_key
generated_content
model_used
status
reviewed_by
reviewed_at
review_note
expires_at
```

`generation_key` supports reuse/deduplication.

---

# 30. AI Review Note

Reject request:

```json
{
  "reason": "Incorrect example"
}
```

Persistence:

```text
reason
 ├──→ ai_generated_content.review_note
 └──→ admin_audit_logs.details.reason
```

Response:

```text
reviewNote
```

This keeps domain state and historical audit information separate.

---

# 31. AI Budget Guard

Every billable AI operation passes through:

```text
AiBudgetGuard
```

Checks:

```text
daily request limit
daily token limit
daily estimated cost
feature limit
provider limit
```

When exceeded:

```text
block AI operation
return controlled application error
log usage event
```

Core learning should remain available where possible.

---

# 32. AI Usage Logging

For each AI request record:

```text
date
provider
model
feature
request count
input tokens where available
output tokens where available
total tokens
estimated cost
status
```

Never store secrets.

---

# 33. Prompt Management

Prompts should be versioned.

Example:

```text
example_generation_v1
cefr_classification_v1
personalized_exercise_v1
```

Store:

```text
prompt_version
```

with generated reusable content where applicable.

---

# 34. AI Response Validation

Validate:

```text
required fields
length
allowed question types
JSON structure
vocabulary references
answer options
safety constraints
```

Invalid result:

```text
reject
retry once where safe
fallback
```

Do not blindly return raw model output.

---

# 35. TTS

TTS is external service integration.

Architecture:

```text
TtsService
   ↓
TtsProvider
```

Flow:

```text
Vocabulary
 ↓
Generate audio
 ↓
Cache audio
 ↓
Store audio_url
 ↓
Mobile fetches audio
```

Prefer pre-generation/caching for stable vocabulary.

---

# 36. Authentication

Authentication model:

```text
JWT Access Token
+
Refresh Token
```

Access token:

```text
short-lived
```

Refresh token:

```text
longer-lived
server-revocable
```

V1 includes:

```text
expiry
revocation
```

Refresh token rotation is a future security enhancement unless explicitly implemented.

---

# 37. JWT Claims

Minimum:

```text
sub = user ID
role = USER / ADMIN
iat
exp
```

Do not place sensitive data in JWT.

---

# 38. Password Security

Local password must be stored as:

```text
strong one-way password hash
```

Never store plaintext.

Recommended:

```text
BCrypt or Argon2
```

depending on selected Spring Security configuration.

---

# 39. Auth Provider

User account supports:

```text
LOCAL
GOOGLE
```

Rules:

```text
LOCAL
→ password_hash required

GOOGLE
→ password_hash may be null
```

Do not infer provider only from null password hash.

---

# 40. Authorization

Roles:

```text
USER
ADMIN
```

Use method/route-level authorization.

Examples:

```text
/admin/**
→ ADMIN only
```

User endpoints:

```text
/users/me/**
→ authenticated user
```

Admin cannot access learner data without explicit authority rules where sensitive operations are involved.

---

# 41. Brute-Force Protection

Login protection:

```text
failed-attempt tracking
temporary lock/rate limit
```

Baseline policy:

```text
5 failed login attempts
→ temporary lock / cooldown ≈ 5 minutes
```

Exact values remain configuration.

---

# 42. API Security

All non-public endpoints require:

```text
HTTPS
JWT authentication
role/ownership checks
input validation
rate limiting where appropriate
```

AI endpoints have stricter rate/budget controls.

---

# 43. Input Validation

Use Bean Validation.

Examples:

```java
@NotBlank
@Email
@Size
@Min
@Max
@NotNull
```

Validate:

```text
answerQuality 0–5
responseTimeMs >= 0
pagination bounds
IDs are valid UUIDs
```

---

# 44. Error Model

Standard API error:

```json
{
  "code": "LEARNING_ATTEMPT_CONFLICT",
  "message": "The learning state has changed. Please retry.",
  "timestamp": "2026-08-30T09:00:00Z",
  "path": "/api/v1/learning/attempts",
  "traceId": "uuid"
}
```

Do not expose:

```text
stack trace
SQL
internal class name
vendor error payload
```

---

# 45. HTTP Status Rules

```text
200 → successful read/update
201 → resource created
204 → success without body
400 → validation/business input error
401 → unauthenticated
403 → forbidden
404 → not found
409 → conflict/concurrency/idempotency
422 → optional semantic validation
429 → rate limit/budget
500 → unexpected server error
503 → dependency/service unavailable
```

---

# 46. Pagination

For paginated lists:

```json
{
  "content": [],
  "page": 0,
  "size": 20,
  "totalElements": 100,
  "totalPages": 5,
  "hasNext": true
}
```

Non-paginated reference lists:

```json
[
  {
    "id": "uuid",
    "name": "Travel"
  }
]
```

Do not mix both conventions arbitrarily.

---

# 47. API Versioning

Base path:

```text
/api/v1
```

Changes:

```text
breaking change
→ v2
```

Non-breaking:

```text
additive response field
new optional parameter
```

can remain v1 subject to compatibility review.

---

# 48. API Documentation

Source of truth:

```text OpenAPI / Swagger
```

Generated runtime UI:

```text Swagger UI
```

OpenAPI changes must be reviewed alongside:

```text API Specification
Database
Backend DTO
Frontend client
```

---

# 49. Mobile Architecture

Recommended:

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
Retrofit
```

---

# 50. Android Package Structure

```text
app/src/main/java/.../

core/
├── network/
├── navigation/
├── ui/
├── storage/
└── util/

data/
├── api/
├── dto/
├── mapper/
├── repository/
└── model/

domain/
├── model/
├── repository/
└── usecase/

presentation/
├── auth/
├── onboarding/
├── assessment/
├── home/
├── learn/
├── review/
├── vocabulary/
├── quiz/
├── personalized/
├── progress/
├── gamification/
├── notifications/
└── profile/
```

---

# 51. Android Screen Architecture

Example:

```text
HomeFragment
      ↓
HomeViewModel
      ↓
GetTodayPlanUseCase
      ↓
LearningRepository
      ↓
LearningApi
      ↓
GET /api/v1/learning/today
```

---

# 52. Android State Model

Each screen:

```text
INITIAL
LOADING
SUCCESS
EMPTY
ERROR
OFFLINE
```

Mutation:

```text
IDLE
SUBMITTING
SUCCESS
FAILURE
```

---

# 53. Android Network Client

Use:

```text
Retrofit
OkHttp
```

Interceptors:

```text
Authorization
Logging only in debug
Request ID / trace ID where useful
```

Do not log tokens in production.

---

# 54. Token Storage

Access/refresh credentials must use secure platform storage.

Do not store tokens in:

```text
plain SharedPreferences
logs
database without protection
```

Prefer Android secure storage mechanism.

---

# 55. Android Connectivity

V1:

```text
Online-first
```

Offline:

```text
read-only cached content if available
block mutations
```

Do not implement:

```text
offline attempt queue
background sync
conflict merge
```

---

# 56. Read-Only Cache

Optional cached entities:

```text
Vocabulary
Topic metadata
Approved reusable content
```

Cache should be treated as:

```text
stale-readable
not authoritative
```

Server is authoritative for learning state.

---

# 57. Admin Web Architecture

Recommended:

```text
React
 ↓
Page
 ↓
Feature component
 ↓
API client
 ↓
Spring Boot
```

Suggested structure:

```text
src/
├── app/
├── routes/
├── components/
├── features/
│   ├── auth/
│   ├── dashboard/
│   ├── users/
│   ├── vocabulary/
│   ├── topics/
│   ├── quizzes/
│   ├── ai-content/
│   ├── statistics/
│   ├── ai-usage/
│   └── audit/
├── services/
├── hooks/
├── types/
└── utils/
```

---

# 58. Admin API Client

Centralize API access:

```text
apiClient
```

Feature services:

```text
userService
vocabularyService
topicService
quizService
aiContentService
statisticsService
auditService
```

Do not call raw fetch/axios from every component.

---

# 59. Admin Table Pattern

All list screens follow:

```text
Page Header
 ↓
Search / Filter
 ↓
Data Table
 ↓
Pagination
```

This pattern applies to:

```text
Users
Vocabulary
Quizzes
AI Content
Audit Logs
```

---

# 60. Admin AI Review

Reject flow:

```text
Open AI Content
 ↓
Reject
 ↓
Enter reason
 ↓
POST /admin/ai-content/{id}/reject
 ↓
Backend:
status = REJECTED
review_note = reason
audit.details.reason = reason
 ↓
Refresh detail
```

---

# 61. Notification Architecture

Provider:

```text
Firebase Cloud Messaging
```

Backend:

```text
NotificationService
```

Responsibilities:

```text
schedule
send
track status
retry
```

V1 timing:

```text
SRS due + configured/default schedule
```

AI timing optimization is future.

---

# 62. Session Management

Learning session:

```text
Start
 ↓
Attempt(s)
 ↓
End
```

Backend owns:

```text
session started_at
ended_at
words count
accuracy
```

Do not trust client-calculated aggregate as authoritative.

---

# 63. Progress Aggregation

Historical source:

```text
session_attempts
```

Current per-user-per-word state:

```text
user_vocabulary_progress
```

Aggregated progress may be:

```text
calculated by query
or
maintained carefully as derived fields
```

Do not create multiple conflicting sources of truth.

---

# 64. Gamification

XP:

```text
xp_logs
```

Streak:

```text
streaks
```

Badges:

```text
badges
user_badges
```

Business rules should live in:

```text
GamificationService
```

---

# 65. Audit Logging

Audit sensitive admin actions:

```text
LOCK_USER
UNLOCK_USER
CREATE_VOCABULARY
UPDATE_VOCABULARY
PUBLISH_QUIZ
UNPUBLISH_QUIZ
APPROVE_AI
REJECT_AI
```

Audit record:

```text
admin_id
action
target_table
target_id
details
created_at
```

---

# 66. Logging

Application logs should include:

```text
timestamp
level
service/module
traceId
requestId
message
```

Never log:

```text
password
JWT
refresh token
API key
full private user data
```

---

# 67. Observability

Minimum:

```text
Spring Boot Actuator
health
readiness
liveness
metrics
```

Track:

```text
API latency
error rate
database latency
AI requests
AI failures
AI cost
```

---

# 68. Traceability

Where possible propagate:

```text
traceId
requestId
```

across:

```text
Mobile/Admin
→ API
→ Service
→ AI provider
```

This simplifies debugging.

---

# 69. Configuration

Use environment-specific configuration:

```text
application.yml
application-local.yml
application-test.yml
application-prod.yml
```

Secrets from environment/secret manager.

Do not commit:

```text
JWT secret
database password
AI API key
OAuth client secret
```

---

# 70. Core Configuration

Example:

```text
app.jwt.access-token-expiration
app.jwt.refresh-token-expiration

app.learning.max-daily-workload-increase-percent
app.learning.default-daily-minutes

app.ai.daily-request-limit
app.ai.daily-token-limit
app.ai.daily-cost-limit

app.notification.default-time
```

---

# 71. Flyway Migration Strategy

Migrations are immutable after release.

Naming:

```text
V1__baseline.sql
V2__...
...
V30__add_ai_content_review_note.sql
```

Current schema baseline includes:

```text
ai_generated_content.review_note
```

If upgrading from v1.3:

```sql
ALTER TABLE ai_generated_content
ADD COLUMN review_note TEXT NULL;
```

---

# 72. Test Strategy

Testing levels:

```text
Unit Test
Integration Test
Repository Test
Controller Test
Contract Test
End-to-End Test
UI Test
```

---

# 73. Unit Test Priority

Highest priority:

```text
SRS
Daily Plan
Weak Word Detection
Forgetting Risk
Difficulty Adjustment
Gamification rules
AI Budget Guard
Validation
```

---

# 74. SRS Test Cases

At minimum:

```text
q = 0
q = 1
q = 2
q = 3
q = 4
q = 5
```

Boundary cases:

```text
new word
failed review
first successful review
repeated review
high interval
minimum interval
```

Verify:

```text
easeFactor
interval
repetitions
status
nextReviewAt
```

---

# 75. Concurrency Tests

Scenario:

```text
Two requests update same user_vocabulary_progress
```

Expected:

```text
one succeeds
one receives 409
```

Do not silently overwrite.

---

# 76. Idempotency Tests

Send same:

```text
eventId
```

twice.

Expected:

```text
one logical attempt
one SRS update
one XP effect
one streak effect
```

---

# 77. AI Tests

Test:

```text
valid response
malformed JSON
missing field
unsafe response
overlong response
budget exceeded
provider timeout
provider 5xx
```

Expected:

```text
controlled fallback
no corrupted DB record
appropriate status
usage logging
```

---

# 78. Admin Tests

Test:

```text
Approve AI content
Reject AI content
Reject without reason
Lock user
Publish quiz
```

Reject acceptance:

```text
reason required
review_note persisted
audit details persisted
```

---

# 79. API Contract Tests

Ensure OpenAPI matches:

```text
Controller DTO
status codes
request validation
response shape
pagination
error schema
```

Breaking mismatch fails CI.

---

# 80. Mobile UI Tests

P0 screens:

```text
Login
Onboarding
Home
Review
Flashcard
Personalized Exercise
Progress
Profile
```

Test:

```text
navigation
loading
error
offline
button states
input validation
```

---

# 81. Admin UI Tests

P0:

```text
Login
Dashboard
Users
Vocabulary
AI Content
Statistics
```

Test:

```text
search
filter
pagination
form validation
confirmation
AI review
audit navigation
```

---

# 82. Build Profiles

Development:

```text
local
```

Test:

```text
test
```

Production/demo:

```text
prod
```

Mobile build variants:

```text
debug
release
```

Do not embed production secrets in debug builds.

---

# 83. CI Pipeline

Recommended:

```text
Checkout
 ↓
Compile
 ↓
Static analysis
 ↓
Unit tests
 ↓
Integration tests
 ↓
API contract validation
 ↓
Build artifacts
```

On pull request:

```text
must pass
```

---

# 84. CD / Deployment

V1 can use:

```text
Docker Compose
```

or:

```text
Docker containers on a VM
```

Services:

```text
backend
postgres
admin-web
```

Reverse proxy:

```text
Nginx or equivalent
```

HTTPS required outside local development.

---

# 85. Environment Architecture

```text
Developer
   ↓
Local Docker/PostgreSQL
   ↓
Test Environment
   ↓
Demo/Staging
   ↓
Production (future)
```

---

# 86. Backup

PostgreSQL:

```text
scheduled backup
```

At minimum:

```text
database dump
retention policy
restore verification
```

For đồ án/demo, document the backup process even if infrastructure is minimal.

---

# 87. Data Retention

Keep:

```text
learning history
audit logs
AI usage logs
AI reusable content
```

for defined project retention period.

Do not keep:

```text
unnecessary sensitive data
```

---

# 88. Privacy / Data Minimization

Store only what is needed for:

```text
authentication
learning personalization
progress
administration
audit
```

Avoid collecting unnecessary personal information.

---

# 89. API Ownership Rules

Learner endpoints:

```text
/user
/learning
/vocabulary
/progress
/quiz
```

Admin endpoints:

```text
/admin/**
```

Admin UI should never bypass the public API contract and connect directly to PostgreSQL.

---

# 90. Cross-Feature Dependency Rules

```text
Auth
→ User

User
→ Onboarding / Assessment

Assessment
→ CEFR

CEFR + Goals
→ Vocabulary Recommendation

Learning Attempts
→ SRS

Learning History
→ Weak Word Detection

Weak Words
→ Personalized Exercise

Progress
→ Daily Plan adaptation

AI Content
→ Admin Review
```

---

# 91. Source of Truth Rules

```text
Server
→ authoritative user/profile state

PostgreSQL
→ authoritative persisted state

user_vocabulary_progress
→ current per-user-word SRS state

session_attempts
→ historical attempts

daily_plans
→ generated daily workload

ai_generated_content
→ reusable AI content lifecycle
```

---

# 92. What Must Not Be Duplicated

Do not independently calculate on client:

```text
SRS
forgrtting risk
recommendation score
daily workload
XP
streak
```

Client only renders server results.

---

# 93. Cache Rules

Safe to cache:

```text
reference data
vocabulary metadata
approved reusable content
```

Do not treat cached data as authority for:

```text
SRS
progress
XP
streak
attempt history
```

---

# 94. Performance Baseline

Target for normal API:

```text
p50 < 300ms
p95 < 800ms
```

excluding external AI generation and cold infrastructure.

AI endpoints:

```text
show loading
timeouts
fallback
```

---

# 95. Database Performance

Important indexes:

```text
user_vocabulary_progress(user_id, next_review_at)
session_attempts(session_id, attempted_at)
session_attempts(vocabulary_id, attempted_at)
vocabulary(word)
vocabulary(cefr_level_id)
vocabulary_topics(topic_id)
quiz_questions(quiz_id)
quiz_attempt_answers(quiz_attempt_id)
xp_logs(user_id)
notifications(user_id, status)
ai_generated_content(status)
ai_generated_content(generation_key)
admin_audit_logs(admin_id, created_at)
```

PostgreSQL does not automatically create indexes for every foreign key.

---

# 96. Query Rules

Prefer:

```text
pagination
projection
join fetch carefully
batch loading
```

Avoid:

```text
N+1 queries
SELECT * for large admin tables
unbounded list APIs
```

---

# 97. Transaction Isolation

Default PostgreSQL isolation:

```text
READ COMMITTED
```

Use stronger locking only where necessary.

Optimistic locking is the primary strategy for:

```text
user_vocabulary_progress
streaks
```

---

# 98. External Dependency Timeout

External services must have:

```text
connect timeout
read timeout
retry policy
circuit/failure handling where useful
```

Do not retry unsafe operations blindly.

AI generation can use:

```text
limited retry
then fallback
```

---

# 99. Rate Limiting

Recommended:

```text
login
AI generation
personalized exercise
admin generation endpoints
```

Rate limits are configuration-driven.

---

# 100. Admin Bulk Operations

Not required for V1.

Future:

```text
bulk approve
bulk reject
bulk vocabulary import
bulk publish
```

Do not build until core workflow is stable.

---

# 101. Feature Flags

Optional but recommended for AI:

```text
ai.personalizedExercise.enabled
ai.exampleGeneration.enabled
ai.cefrSuggestion.enabled
```

Allows safe shutdown without redeployment.

---

# 102. AI Fallback Strategy

Example:

```text
AI unavailable
      ↓
Personalized Exercise
      ↓
Use standard weak-word quiz
```

The user should continue learning where possible.

---

# 103. Deployment Security

Non-local:

```text
HTTPS
secure cookies where used
CORS restricted
strong secrets
database not publicly exposed
admin route protected
```

---

# 104. CORS

Allow only known frontend origins.

Examples:

```text
Android does not require browser CORS
Admin Web origin is explicitly allowed
```

Do not use:

```text
Access-Control-Allow-Origin: *
```

for authenticated production admin APIs.

---

# 105. Database Connection Pool

Use bounded PostgreSQL connection pool.

Tune according to deployment resources.

Do not open one database connection per request manually.

---

# 106. API Client Retry Rules

Client may retry:

```text
transient GET
certain 503/timeout requests
```

Be cautious with:

```text
POST mutations
```

unless protected with:

```text
eventId/idempotency
```

---

# 107. Mobile Error UX Mapping

```text
401
→ refresh token / login

403
→ permission message

404
→ not found

409
→ refresh state

429
→ wait / retry later

503
→ service unavailable

Network
→ Offline State
```

---

# 108. Admin Error UX Mapping

```text
400
→ inline validation

401
→ session expired

403
→ permission page

404
→ resource unavailable

409
→ refresh current record

429
→ rate limit/budget

500/503
→ retry state
```

---

# 109. Logging by Environment

Development:

```text
DEBUG allowed
```

Production:

```text
INFO/WARN/ERROR
```

Sensitive payload logging disabled.

---

# 110. Architecture Decision: Modular Monolith vs Microservices

Official V1:

```text
Modular Monolith
```

Do not split:

```text
AI service
Learning service
Admin service
Auth service
```

into separate deployables in V1 unless a clear operational requirement appears.

---

# 111. Future Extraction Boundaries

Potential future services:

```text
AI Service
Notification Service
Analytics Service
```

Current code should keep interfaces around external dependencies so extraction remains possible.

---

# 112. Development Sequence

## Phase 1 — Backend Foundation

```text
Spring Boot
 ↓
Project structure
 ↓
PostgreSQL
 ↓
Flyway
 ↓
JPA
 ↓
Exception handling
 ↓
Security
```

## Phase 2 — Core Domain

```text
User
 ↓
Vocabulary
 ↓
Onboarding
 ↓
Assessment
```

## Phase 3 — Learning

```text
Sessions
 ↓
Attempts
 ↓
SRS
 ↓
Review
```

## Phase 4 — Personalization

```text
Daily Plan
 ↓
Weak Words
 ↓
Forgetting Risk
 ↓
Recommendation
```

## Phase 5 — Product

```text
Quiz
 ↓
Progress
 ↓
Gamification
 ↓
Notification
```

## Phase 6 — AI

```text
Reusable AI Content
 ↓
Admin Review
 ↓
Personalized Exercise
 ↓
AI Usage/Budget
```

## Phase 7 — Clients

```text
Android App
 ↓
Admin Web
```

Integration can happen incrementally after each backend slice is stable.

---

# 113. Recommended Implementation Order Within Android

```text
1. App shell/navigation
2. Auth
3. Onboarding
4. Assessment
5. Home
6. Learn
7. Review
8. Flashcard
9. Learning Result
10. Progress
11. Weak Words
12. Personalized Exercise
13. Quiz
14. Gamification
15. Profile
16. Notifications
```

---

# 114. Recommended Implementation Order Within Admin Web

```text
1. Admin shell
2. Login
3. Dashboard
4. Users
5. Vocabulary
6. AI Content
7. Statistics
8. Topics
9. Quizzes
10. AI Usage
11. Audit Logs
```

---

# 115. Developer Workflow

```text
Feature
 ↓
Read SRS/API
 ↓
Define DTO
 ↓
Implement service
 ↓
Repository
 ↓
Controller
 ↓
Unit test
 ↓
Integration test
 ↓
OpenAPI
 ↓
Mobile/Admin integration
 ↓
UI test
```

---

# 116. Branching Strategy

Simple Git workflow:

```text
main
develop
feature/*
```

Rules:

```text
main
→ stable

develop
→ integration

feature/*
→ one feature/change
```

---

# 117. Commit Convention

Recommended:

```text
feat:
fix:
refactor:
test:
docs:
chore:
```

Examples:

```text
feat: implement learning attempt endpoint
fix: prevent duplicate learning attempt
test: add SRS boundary cases
docs: update OpenAPI review note
```

---

# 118. Code Quality

Backend:

```text
clean naming
small services
limited method complexity
constructor injection
final where appropriate
no magic constants
```

Mobile:

```text
ViewModel not overloaded
reusable components
clear state handling
no business logic in Fragment
```

Admin:

```text
feature-oriented components
typed API responses
reusable table/form patterns
```

---

# 119. Definition of Done — Backend

A feature is done when:

```text
[ ] Requirements satisfied
[ ] DTO defined
[ ] Validation implemented
[ ] Service implemented
[ ] Repository implemented
[ ] Transaction defined
[ ] Security checked
[ ] Error handling implemented
[ ] Tests passed
[ ] OpenAPI updated
[ ] Migration updated if needed
[ ] Logs/metrics considered
```

---

# 120. Definition of Done — Android

```text
[ ] Screen implemented
[ ] Navigation works
[ ] ViewModel state defined
[ ] API integrated
[ ] Loading state
[ ] Error state
[ ] Empty state
[ ] Offline state where relevant
[ ] Accessibility checked
[ ] UI matches design
[ ] Test completed
```

---

# 121. Definition of Done — Admin Web

```text
[ ] Page implemented
[ ] Search/filter works
[ ] Pagination works where required
[ ] Form validation works
[ ] Loading state
[ ] Empty state
[ ] Error state
[ ] Confirmation for destructive action
[ ] API integrated
[ ] Responsive behavior
[ ] Keyboard accessibility
[ ] Audit behavior verified where required
```

---

# 122. Critical Acceptance Tests

## Learning

```text
User logs in
→ starts Daily Plan
→ studies word
→ submits quality 0–5
→ backend derives isCorrect
→ SRS updates
→ attempt recorded
→ next review returned
```

## Personalization

```text
User accumulates weak words
→ Daily Plan adapts
→ Personalized Exercise targets weak words
```

## AI Review

```text
Admin creates/receives reusable AI content
→ Pending Review
→ Reject with reason
→ review_note saved
→ audit reason saved
```

---

# 123. Cross-System Consistency Rules

Any change to one of:

```text
Database
API
SRS
AI
UI
```

must trigger review of dependent specifications.

Examples:

```text
DB change
→ API/OpenAPI review

API change
→ Android/Admin review

SRS change
→ attempt API + tests review

AI content lifecycle change
→ Admin UI + DB + API review
```

---

# 124. Current Version Baseline

```text
SRS
v1.1

Database
v1.4

System Architecture
v1.1

AI Personalization
v1.1

API Specification
v1.2

OpenAPI
v1.2

Mobile UI/UX
v1.1

Design System + Wireframe
v1.2 FULL

Mobile High-Fidelity
v1.1

Mobile Prototype
v1.1

Admin UI/UX
v1.1

Admin Design System
v1.0

Admin High-Fidelity
v1.0

Admin Prototype
v1.0
```

---

# 125. Final Technical Architecture

```text
                         USERS
                           │
              ┌────────────┴────────────┐
              │                         │
        Android App                Admin Web
          Java V1                 React + TS
              │                         │
              └────────────┬────────────┘
                           │
                         HTTPS
                           │
                           ▼
                  Spring Boot API
                           │
        ┌──────────────────┼──────────────────┐
        │                  │                  │
        ▼                  ▼                  ▼
   Core Domain        Personalization        AI
        │                  │                  │
        └──────────────────┼──────────────────┘
                           │
                           ▼
                      PostgreSQL
                           │
                           ▼
                    Flyway Migrations

External:
TTS Provider
LLM Provider
FCM
```

---

# 126. Final Product/Data Flow

```text
USER
 ↓
AUTH
 ↓
PROFILE + GOALS + CEFR
 ↓
PERSONALIZATION
 ↓
DAILY PLAN
 ↓
LEARNING
 ↓
ATTEMPT
 ↓
SRS + PROGRESS + GAMIFICATION
 ↓
WEAK WORDS
 ↓
PERSONALIZED EXERCISE
 ↓
NEXT PLAN
```

---

# 127. Final Admin/Data Flow

```text
ADMIN
 ↓
VOCABULARY
 ↓
AI CEFR ASSISTANCE
 ↓
AI CONTENT GENERATION
 ↓
PENDING REVIEW
 ↓
APPROVE / REJECT
 ↓
REVIEW NOTE + AUDIT
 ↓
PUBLISHED / AVAILABLE CONTENT
```

---

# 128. Final V1 Technical Boundary

## Included

```text
Android Java App
Spring Boot Backend
PostgreSQL
Admin Web
JWT Auth
Refresh Token
SRS
Personalization
AI Content
AI Personalized Exercise
Progress
Gamification
Notifications
Audit
AI Usage/Budget
```

## Not required in V1

```text
Microservices
Offline-first synchronization
Conflict merge protocol
Speaking
Writing
Listening
Reading
AI Tutor
Advanced ML recommender
AI notification optimization
```

---

# 129. Recommended Immediate Tasks

Implementation should start in this order:

```text
1. Create repository structure
2. Create Spring Boot backend
3. Create PostgreSQL + Docker Compose
4. Add Flyway migrations
5. Implement entities/repositories
6. Implement Spring Security
7. Implement Auth APIs
8. Implement User/Profile
9. Implement Vocabulary
10. Implement Assessment
11. Implement Learning Session
12. Implement Learning Attempt
13. Implement SRS
14. Implement Review
15. Implement Daily Plan
16. Implement Progress/Weak Words
17. Implement AI module
18. Implement Android integration
19. Implement Admin Web
20. End-to-end testing
```

---

# 130. Final Technical Freeze

Before coding:

```text
[ ] Technology stack accepted
[ ] Modular monolith accepted
[ ] Package structure accepted
[ ] Database v1.6 accepted
[ ] API v1.4 accepted
[ ] OpenAPI v1.4 accepted
[ ] JWT/refresh strategy accepted
[ ] Optimistic locking accepted
[ ] Idempotency accepted
[ ] SRS ownership accepted
[ ] AI architecture accepted
[ ] AI review workflow accepted
[ ] Online-first behavior accepted
[ ] Mobile architecture accepted
[ ] Admin architecture accepted
[ ] Testing strategy accepted
```

---

# 131. Final Implementation Principle

The implementation must follow:

```text
Database
   ↕
Backend Domain
   ↕
API Contract
   ↕
Client UI
```

not:

```text
UI decides business logic
Client calculates SRS
Admin bypasses API
AI bypasses budget guard
```

The backend is the authoritative business layer.

---

# 132. Final Project Development Path

```text
                   SPECIFICATION
                        │
                        ▼
               TECHNICAL DESIGN
                        │
          ┌─────────────┴─────────────┐
          ▼                           ▼
       BACKEND                     CLIENTS
          │                       /       \
          ▼                      /         \
      PostgreSQL            Android      Admin Web
          │
          ▼
   SRS / Personalization
          │
          ▼
          AI
          │
          ▼
       TESTING
          │
          ▼
        DEMO
          │
          ▼
       RELEASE V1
```

---

# 133. Final Definition of Technical Ready

Project is **Technical Ready** when:

```text
Architecture is frozen
+
Database is frozen enough to migrate
+
OpenAPI is validated
+
Business-critical algorithms are implemented/specifiable
+
Security boundaries are defined
+
Concurrency/idempotency are defined
+
AI integration boundary is defined
+
Client architecture is defined
+
Admin architecture is defined
+
Testing strategy is defined
```

At that point, documentation work should stop expanding unless implementation reveals a real requirement gap.

---

# 134. Current Next Step

```text
Technical Specification v1.2
            ↓
Create Git Repository
            ↓
Create Spring Boot Backend
            ↓
Create PostgreSQL + Flyway
            ↓
Implement Authentication
            ↓
Implement Core Learning
```

**This marks the transition from system-design phase to implementation phase.**


---

# 135. Idempotency Implementation Reference

Recommended components:

```text
IdempotencyService
IdempotencyRepository
RequestHashService
IdempotencyCleanupJob
```

Conceptual service:

```java
IdempotencyResult execute(
    UUID userId,
    String endpoint,
    UUID eventId,
    String requestHash,
    Supplier<ResponseSnapshot> operation
)
```

Rules:

```text
FOUND + SAME HASH
→ replay

FOUND + DIFFERENT HASH/ENDPOINT
→ 409

NOT FOUND
→ execute + persist
```

The implementation must remain transaction-safe.

---

# 136. Final Version Baseline

```text
Database
v1.5

API
v1.3

OpenAPI
v1.3

Technical Specification
v1.1
```

---

# Reconciled Technical Rules v1.2

Implementation must expose typed configuration classes with defaults matching the approved baseline: `AssessmentProperties`, `SrsProperties`, `PersonalizationProperties`, `GamificationProperties`, `NotificationProperties`, `IdempotencyProperties`.

Backend-owned services: `AssessmentService`, `SrsService`, `WeaknessService`, `ForgettingRiskService`, `RecommendationService`, `DailyPlanService`, `GamificationService`, `NotificationService`, `IdempotencyService`, `AiContentService`. Controllers and clients must not duplicate these formulas.

Use algorithm identifiers in logs/analytics where useful: `assessment-block-v1`, `sm2-ext-v1`, `weakness-rule-v1`, `forgetting-risk-v1`, `daily-plan-v1`, `gamification-v1`.

Daily Plan is persisted snapshot state, notifications use FCM + user device/preferences, and AI generation is synchronous at the V1 client contract.
