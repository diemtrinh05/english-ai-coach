# Backend Technical Specification v1.3 — English AI Coach

**Project:** English AI Coach  
**Document:** Backend Technical Specification  
**Version:** 1.3  
**Status:** APPROVED BASELINE  
**Date:** 2026-08-31  

**Purpose:** Đây là tài liệu kỹ thuật chi tiết để triển khai Spring Boot Backend từ Database Schema, System Architecture, AI Personalization, API Specification và OpenAPI đã chốt.

**Source baseline:**
- `English_AI_Coach_SRS_v1.2.md`
- `English_AI_Coach_Database_Schema_v1.6.md`
- `English_AI_Coach_System_Architecture_v1.3.md`
- `English_AI_Coach_AI_Personalization_Specification_v1.3.md`
- `English_AI_Coach_API_Specification_v1.4.md`
- `English_AI_Coach_OpenAPI_Swagger_v1_4.md`
- `English_AI_Coach_Technical_Specification_v1.2.md`

---

# 1. Backend Mission

Backend là authoritative business layer của hệ thống.

```text
Mobile App
      │
Admin Web
      │
      ▼
Spring Boot Backend
      │
      ├── Business Rules
      ├── Security
      ├── SRS
      ├── Personalization
      ├── AI orchestration
      ├── Progress
      ├── Gamification
      └── Admin workflow
      │
      ▼
PostgreSQL
```

Client không được tự quyết định các business rules quan trọng.

---

# 2. Official Backend Stack

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
springdoc-openapi

PostgreSQL

JUnit 5
Mockito
Testcontainers
```

Optional infrastructure:

```text
Redis
Object Storage
FCM
LLM Provider
TTS Provider
```

System architecture đã xác định modular monolith + layered architecture, PostgreSQL, JPA/Hibernate, Flyway và REST/JSON `/api/v1/`. 

---

# 3. Architecture Style

V1 dùng:

```text
Modular Monolith
+
Layered Architecture
```

Không dùng microservices ở V1.

Lý do:

```text
đơn giản hơn
transaction rõ hơn
debug dễ hơn
triển khai phù hợp đồ án
đủ cho MVP
```

Module vẫn phải độc lập về trách nhiệm để có thể tách sau này.

---

# 4. Backend Module Map

```text
com.example.englishaicoach

├── auth
├── user
├── onboarding
├── assessment
├── vocabulary
├── learning
├── personalization
├── quiz
├── progress
├── gamification
├── notification
├── ai
├── admin
├── audit
└── common
```

Vai trò chính:

```text
auth
→ authentication/token/brute-force

user
→ account/profile

onboarding
→ goals/daily time

assessment
→ placement test/CEFR

vocabulary
→ vocabulary/topic/example

learning
→ sessions/attempts

personalization
→ SRS/weakness/forgetting/recommendation/daily plan

quiz
→ quiz attempts/answers

progress
→ learner analytics

gamification
→ XP/streak/badges

notification
→ reminder delivery

ai
→ LLM/TTS/content/budget/usage

admin
→ admin operations

audit
→ admin audit trail
```

---

# 5. Package Structure

```text
src/main/java/com/example/englishaicoach/

├── auth/
│   ├── controller/
│   ├── dto/
│   ├── entity/
│   ├── repository/
│   ├── service/
│   ├── security/
│   └── mapper/
│
├── user/
│   ├── controller/
│   ├── dto/
│   ├── entity/
│   ├── repository/
│   ├── service/
│   └── mapper/
│
├── onboarding/
├── assessment/
├── vocabulary/
├── learning/
├── personalization/
├── quiz/
├── progress/
├── gamification/
├── notification/
├── ai/
├── admin/
├── audit/
│
├── common/
│   ├── exception/
│   ├── response/
│   ├── validation/
│   ├── security/
│   ├── idempotency/
│   ├── clock/
│   ├── logging/
│   └── util/
│
└── config/
```

---

# 6. Layer Responsibilities

```text
Controller
    ↓
Application Service
    ↓
Domain Service
    ↓
Repository
    ↓
Database
```

Infrastructure adapters:

```text
LLM Provider
TTS Provider
Object Storage
FCM
Redis
```

must be behind interfaces/abstractions.

---

# 7. Controller Layer

Controller responsibilities:

```text
Receive HTTP request
Bind DTO
Validate input
Check authentication
Check authorization
Call service
Map result
Return HTTP response
```

Controller must not contain:

```text
SRS formulas
recommendation logic
transaction orchestration
LLM prompt logic
database business rules
```

---

# 8. DTO Layer

Do not expose JPA entities directly.

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

This avoids leaking:

```text
internal columns
lazy associations
security fields
database structure
```

---

# 9. Entity Layer

Entities map to PostgreSQL tables.

Core entities:

```text
User
RefreshToken
UserProfile
Goal
UserGoal
CefrLevel
UserLevelAssessment

Topic
Vocabulary
VocabularyTopic
VocabularyExample

UserVocabularyProgress
LearningSession
SessionAttempt
DailyPlan

Quiz
QuizQuestion
QuizAttempt
QuizAttemptAnswer

Streak
XpLog
Badge
UserBadge

Notification

AiGeneratedContent
AiRequest
AiUsageDaily

AdminAuditLog
IdempotencyKey
```

---

# 10. Entity Mapping Rules

Use:

```java
@Entity
@Table
@Id
@GeneratedValue
```

For UUID IDs:

```java
@Id
@GeneratedValue
private UUID id;
```

Associations should default to lazy loading unless a specific use case requires otherwise.

Avoid large bidirectional object graphs.

---

# 11. UUID Strategy

All public/core IDs:

```text
UUID
```

Client-generated event IDs:

```text
UUID
```

UUID does not mean offline-first is supported.

---

# 12. Repository Layer

Use Spring Data JPA.

Example:

```java
public interface VocabularyRepository
        extends JpaRepository<Vocabulary, UUID> {
}
```

Custom query methods may be used for:

```text
due reviews
weak words
progress
admin filters
statistics
```

Large queries should use projections where practical.

---

# 13. Service Naming

Use-case oriented services:

```text
AuthService
UserService
AssessmentService
VocabularyService
LearningSessionService
LearningAttemptService
SrsService
PersonalizationService
DailyPlanService
QuizService
ProgressService
GamificationService
NotificationService
AiContentService
AiGenerationService
AiBudgetService
AdminUserService
AdminVocabularyService
AiReviewService
AuditService
IdempotencyService
```

---

# 14. Transaction Policy

`@Transactional` belongs primarily at service/use-case boundaries.

Examples:

```java
@Transactional
public LearningAttemptResponse submitAttempt(...)
```

Do not put transaction annotations indiscriminately on every repository method.

---

# 15. Learning Attempt Transaction

Primary mutation:

```text
POST /api/v1/learning/attempts
```

Transaction:

```text
BEGIN
 ↓
Authenticate user
 ↓
Validate session ownership
 ↓
Validate vocabulary
 ↓
Validate answerQuality
 ↓
Check idempotency
 ↓
Load progress + version
 ↓
Calculate SRS
 ↓
Update user_vocabulary_progress
 ↓
Insert session_attempt
 ↓
Update session aggregates if required
 ↓
Update streak
 ↓
Create XP log
 ↓
Save idempotency response
 ↓
COMMIT
```

Any critical failure:

```text
ROLLBACK
```

---

# 16. Answer Quality Source of Truth

Client sends:

```text
answerQuality: 0..5
```

Backend derives:

```text
answerQuality >= 3
→ isCorrect = true

answerQuality < 3
→ isCorrect = false
```

Do not accept client-provided:

```text
isCorrect
```

as a second source of truth.

---

# 17. Learning Attempt Request

Canonical example:

```json
{
  "sessionId": "uuid",
  "vocabularyId": "uuid",
  "attemptType": "WORD_RECALL",
  "responseTimeMs": 2500,
  "answerQuality": 4,
  "eventId": "uuid"
}
```

Validation:

```text
sessionId required
vocabularyId required
attemptType required
responseTimeMs >= 0
answerQuality ∈ [0,5]
eventId required
```

---

# 18. Idempotency Architecture

Idempotency is a shared application capability.

Table:

```text
idempotency_keys
```

Fields:

```text
event_id
user_id
endpoint
request_hash
response_snapshot
response_status
created_at
```

The system architecture explicitly defines `eventId` as a logical operation identifier rather than an HTTP-request identifier. 

---

# 19. Idempotency Applicability

Mandatory V1:

```text
POST /api/v1/learning/attempts
POST /api/v1/quiz-attempts/{attemptId}/answers
POST /api/v1/quiz-attempts/{attemptId}/complete
```

The backend stores the exact logical endpoint with the key. 

---

# 20. Event ID Rules

`eventId` means:

```text
one logical action
```

Example:

```text
User taps Hard
→ eventId = A

Network timeout
→ retry with eventId = A
```

Never:

```text
retry
→ create eventId = B
```

---

# 21. Request Hash

Backend computes:

```text
SHA-256(canonical request payload)
```

Store:

```text
request_hash
```

Rules:

```text
same eventId
+
same endpoint
+
same request hash
→ replay response
```

Otherwise:

```text
same eventId
+
different endpoint/hash
→ HTTP 409
```

This matches the current architecture contract. 

---

# 22. Idempotency Processing

```text
Request
 ↓
IdempotencyService
 ↓
Lookup eventId
```

### Not found

```text
claim key
 ↓
execute business logic
 ↓
save response snapshot
 ↓
commit
```

### Found + same request

```text
return stored response
```

### Found + different request

```text
409 IDEMPOTENCY_KEY_REUSE
```

---

# 23. Idempotency Concurrency — PostgreSQL Canonical

Do **not** use duplicate INSERT → catch `DataIntegrityViolationException` → continue querying in the same transaction. PostgreSQL may mark the transaction rollback-only.

Canonical claim:

```sql
INSERT INTO idempotency_keys(event_id, user_id, endpoint, request_hash, created_at)
VALUES (:eventId, :userId, :endpoint, :requestHash, now())
ON CONFLICT (event_id) DO NOTHING;
```

Transaction flow:

```text
claim with ON CONFLICT DO NOTHING
  inserted → process business mutation → store response_snapshot/status → commit
  not inserted → load existing row → compare user + endpoint + request hash
      same → replay stored response
      different → 409 IDEMPOTENCY_KEY_REUSE
```

A concurrent duplicate may block until the winner commits/rolls back; this is expected. If the business mutation fails, the idempotency claim rolls back in the same transaction.

Request hash is SHA-256 of canonical method + route template + path + query + body after excluding `eventId`; `user_id` is compared separately. Retention: 30 days.

Mandatory integration tests: same-event replay, different-request reuse, two simultaneous identical events with one mutation/no 500, business rollback removes claim, and no duplicate XP/AI cost/notification side effects.

---

# 24. Idempotency Failure

If business transaction fails:

```text
business changes rollback
successful replay snapshot must not remain
```

Then retry using the same event ID may execute again. This matches the architecture's rollback behavior. 

---

# 25. Idempotency Cleanup

Configuration:

```text
IDEMPOTENCY_KEY_RETENTION_DAYS = 30
```

Scheduled cleanup:

```text
delete keys older than retention
```

Use:

```text
@Scheduled
```

or a job scheduler.

---

# 26. Optimistic Locking

Entities:

```text
UserVocabularyProgress
Streak
```

contain:

```text
version BIGINT
```

JPA:

```java
@Version
private Long version;
```

The current database/architecture design explicitly uses optimistic locking on these two states. 

---

# 27. Optimistic Locking Conflict

If update fails because version changed:

```text
OptimisticLockException
```

Map to:

```text
HTTP 409
```

Business handling:

```text
reload and retry
or
reject current mutation
```

depending on operation semantics.

Never silently overwrite newer state.

---

# 28. SRS Service

Central class:

```text
SrsService
```

Responsibilities:

```text
calculate quality adjustment
calculate new ease factor
calculate interval
calculate repetitions
calculate nextReviewAt
derive learning status
```

No SRS calculation in:

```text
Controller
Android
Admin
```

---

# 29. SRS Input

```text
answerQuality
oldEaseFactor
oldInterval
oldRepetitions
responseTimeMs
```

Output:

```text
newEaseFactor
newInterval
newRepetitions
nextReviewAt
newStatus
```

---

# 30. SRS Constraints

```text
minimum interval = 1 day
maximum interval = 180 days
answerQuality = 0..5
```

Response-time extension must remain within the configured limits from the AI Personalization specification.

The personalization acceptance criteria require concrete SRS calculations, interval bounds, workload guards, and concurrency handling. 

---

# 31. SRS Status

Recommended state machine:

```text
NEW
 ↓
LEARNING
 ↓
REVIEWING
 ↓
MASTERED
```

Failed review can move the item backward according to the finalized SRS algorithm.

---

# 32. Learning Session

Use:

```text
LearningSessionService
```

Start:

```text
POST /api/v1/learning/sessions
```

Store:

```text
user
sessionType
startedAt
```

Complete:

```text
endedAt
wordsStudiedCount
accuracyPercent
```

Aggregate fields are derived by backend.

---

# 33. Session Attempt History

`session_attempts` is immutable historical learning data.

It represents:

```text
what happened
```

not:

```text
current SRS state
```

The database design explicitly separates `user_vocabulary_progress` as current state and `session_attempts` as historical learning data. 

---

# 34. User Vocabulary Progress

This is the current per-user/per-word state.

Important:

```text
unique(user_id, vocabulary_id)
```

Contains:

```text
status
ease_factor
interval_days
repetitions
next_review_at
last_reviewed_at
correct_count
incorrect_count
avg_response_time_ms
version
```

---

# 35. Due Review Query

Primary query:

```sql
SELECT ...
FROM user_vocabulary_progress
WHERE user_id = :userId
  AND next_review_at <= :now
ORDER BY next_review_at ASC;
```

Important index:

```text
(user_id, next_review_at)
```

---

# 36. Daily Plan Service

Class:

```text
DailyPlanService
```

Inputs:

```text
user
timezone
dailyLearningMinutes
goals
CEFR
review backlog
weakness
recent performance
```

Outputs:

```text
newWordsTarget
reviewWordsTarget
quizTarget
estimatedMinutes
items
status
```

---

# 37. Daily Plan Architecture

```text
DailyPlanService
      ↓
Review Selector
      ↓
New Word Selector
      ↓
Quiz Selector
      ↓
Workload Guard
      ↓
DailyPlan
```

Daily Plan is personalization output.

---

# 38. Workload Guard

Configuration:

```text
MAX_DAILY_WORKLOAD_INCREASE_PERCENT = 20%
MAX_DAILY_WORKLOAD_DECREASE_PERCENT = 30%
```

Example:

```text
baseline = 10
max increase = 20%

max target = 12
```

No example/test case should violate the 20% production guard.

---

# 39. Review Priority

Review selection should consider:

```text
due date
forgetting risk
weakness
recent failures
```

Priority ordering example:

```text
high forgetting risk
+
recent failure
→ higher priority
```

---

# 40. Weak Word Service

Class:

```text
WeakWordService
```

V1 heuristic inputs:

```text
correctCount
incorrectCount
responseTimeMs
recent attempts
```

Output:

```text
weaknessScore
priority
reasonCode
```

No ML dependency in V1.

---

# 41. Forgetting Risk Service

Class:

```text
ForgettingRiskService
```

Inputs:

```text
time since review
interval
review history
recent failures
```

Output:

```text
riskScore
priority
```

This is a heuristic in V1.

---

# 42. Recommendation Service

Class:

```text
RecommendationService
```

V1 signals:

```text
primary goal
other goals
CEFR
topics
weak words
review state
recent performance
```

Recommendation should be:

```text
content-based
rule-based
deterministic
```

V2 may introduce ML.

---

# 43. Difficulty Adjustment

Class:

```text
DifficultyAdjustmentService
```

Rules:

```text
high completion + high accuracy
→ gradually harder

low completion or low accuracy
→ gradually easier
```

Must remain inside:

```text
workload increase guard
workload decrease guard
```

---

# 44. Personalized Exercise Service

Class:

```text
PersonalizedExerciseService
```

Flow:

```text
User
 ↓
load weak words
 ↓
load goal/CEFR
 ↓
select context
 ↓
AiBudgetGuard
 ↓
LLM
 ↓
Schema validation
 ↓
Content validation
 ↓
Safety filter
 ↓
Response
```

The architecture explicitly separates reusable AI content from personalized content and does not require manual review for each personalized result. 

---

# 45. Reusable AI Content

Types:

```text
EXAMPLE
EXPLANATION
MNEMONIC
STORY
REUSABLE QUIZ
```

Flow:

```text
Request
 ↓
Budget Guard
 ↓
LLM
 ↓
Schema Validation
 ↓
Content Validation
 ↓
PENDING_REVIEW
 ↓
Admin
 ├── APPROVED
 └── REJECTED
```

Approved content can be cached/reused.

---

# 46. AI Review Persistence

Reject request:

```json
{
  "reason": "Incorrect example"
}
```

Backend:

```text
ai_generated_content.status = REJECTED
ai_generated_content.review_note = reason
ai_generated_content.reviewed_by = adminId
ai_generated_content.reviewed_at = now
```

Audit:

```text
admin_audit_logs.action = REJECT
admin_audit_logs.details.reason = reason
```

Database v1.6 explicitly keeps `review_note` on AI content and still preserves the audit reason. 

---

# 47. Personalized Content

Type:

```text
PERSONALIZED_EXERCISE
```

Flow:

```text
Personalization
 ↓
Budget Guard
 ↓
LLM
 ↓
Validation
 ↓
Safety
 ↓
User
```

No per-result manual Admin Review.

The architecture defines this as ephemeral/personalized content. 

---

# 48. AI Provider Abstraction

Interfaces:

```java
public interface LlmProvider {
    AiGenerationResult generate(AiGenerationRequest request);
}
```

Implementations:

```text
OpenAiProvider
GeminiProvider
AzureProvider
...
```

Do not put vendor SDK calls in domain services.

---

# 49. AI Prompt Builder

```text
PromptTemplate
PromptBuilder
PromptVersion
```

Examples:

```text
example_generation_v1
cefr_classification_v1
personalized_exercise_v1
```

Store:

```text
prompt_version
```

for reproducibility.

---

# 50. AI Budget Guard

Configuration:

```text
AI_DAILY_REQUEST_LIMIT
AI_DAILY_TOKEN_LIMIT
AI_DAILY_COST_LIMIT
```

Flow:

```text
AI Request
 ↓
AiBudgetGuard
 ↓
current usage
 ↓
within budget?
```

YES:

```text
LLM
 ↓
record usage
```

NO:

```text
BLOCKED_BY_BUDGET
```

Approved cached content should still be served where appropriate. 

---

# 51. AI Usage Logging

For each request:

```text
userId
feature
provider
model
requestTokens
responseTokens
totalTokens
estimatedCost
status
errorMessage
createdAt
```

Statuses:

```text
SUCCESS
FAILED
BLOCKED_BY_BUDGET
```

---

# 52. AI Usage Aggregation

Daily aggregate:

```text
usageDate
provider
model
feature
requestCount
totalTokens
estimatedCost
blockedRequestCount
```

Unique:

```text
usageDate + provider + model + feature
```

---

# 53. AI Response Validation

Validation pipeline:

```text
LLM output
 ↓
JSON/schema validation
 ↓
domain validation
 ↓
safety filter
```

Failures:

```text
limited retry
or
fallback
```

Never return unchecked raw output.

---

# 54. AI Fallback

If AI is unavailable:

```text
Personalized Exercise
→ standard weak-word practice
```

Reusable content:

```text
use approved cached content
or
return controlled unavailable state
```

Core learning must continue where possible. The architecture explicitly states that core learning should not become unusable because AI is down. 

---

# 55. TTS Service

Interface:

```java
public interface TtsProvider {
    String generateAudio(...);
}
```

Flow:

```text
Vocabulary
 ↓
TTS
 ↓
Object Storage
 ↓
audio_url
```

Prefer pre-generation/cache for stable vocabulary.

---

# 56. Object Storage

Used for:

```text
audio
images
generated media
```

Abstraction:

```text
ObjectStorageService
```

Provider can be:

```text
S3-compatible
MinIO
Cloud Object Storage
```

Do not store large binary media directly in PostgreSQL.

---

# 57. Redis

Redis is optional in MVP.

Potential uses:

```text
cache
rate limiting
distributed locks where justified
temporary data
```

Redis is not source of truth.

If Redis is unavailable:

```text
fallback to database/local computation
```

where safe. The architecture defines Redis as optional cache/rate-limiting infrastructure. 

---

# 58. Authentication

Spring Security.

Flows:

```text
Register
Login
Refresh
Logout
Google OAuth
```

JWT:

```text
Access Token
Refresh Token
```

JWT claims:

```text
sub
role
iat
exp
```

---

# 59. Refresh Token Persistence

Table:

```text
refresh_tokens
```

Store:

```text
token_hash
expires_at
revoked_at
last_used_at
device_info
```

Never store plaintext refresh tokens.

---

# 60. Refresh Token Rotation

Current baseline:

```text
rotation = future improvement
```

V1 minimum:

```text
expiry
revocation
logout revoke
```

If rotation is later implemented:

```text
refresh old token
→ revoke old record
→ issue new refresh token
```

---

# 61. Password Security

Use:

```text
BCrypt or Argon2
```

Never store plaintext.

---

# 62. Auth Provider Rules

```text
LOCAL
→ password_hash required

GOOGLE
→ provider_user_id required
→ password_hash may be null
```

Do not infer provider from password-null state alone.

---

# 63. Brute-Force Protection

Baseline:

```text
5 failed attempts
→ temporary lock
→ 5 minutes
```

Fields:

```text
failed_login_attempts
locked_until
```

The current database stores these fields for this purpose. 

---

# 64. Authorization

Roles:

```text
USER
ADMIN
```

Rules:

```text
/api/v1/admin/**
→ ADMIN

/api/v1/users/me/**
→ authenticated owner

learning/progress
→ authenticated owner
```

Always check resource ownership.

---

# 65. Security Boundary

Never expose:

```text
JWT secret
Refresh token plaintext
Password hash
LLM API key
OAuth client secret
Database credentials
```

---

# 66. Validation

Use Bean Validation.

Examples:

```java
@NotBlank
@NotNull
@Email
@Size
@Min
@Max
```

Learning:

```text
answerQuality 0..5
responseTimeMs >= 0
eventId UUID
```

Admin:

```text
reject reason required
```

---

# 67. Global Exception Handling

Implement:

```java
@RestControllerAdvice
```

Exceptions:

```text
ValidationException
ResourceNotFoundException
ForbiddenException
AuthenticationException
ConflictException
IdempotencyKeyReuseException
OptimisticLockingFailure
AiBudgetExceededException
ExternalServiceException
```

---

# 68. Error Response

Canonical:

```json
{
  "timestamp": "2026-08-30T09:00:00Z",
  "status": 409,
  "code": "IDEMPOTENCY_KEY_REUSE",
  "message": "The eventId was already used for a different logical request.",
  "path": "/api/v1/learning/attempts",
  "traceId": "uuid",
  "details": []
}
```

---

# 69. HTTP Status Mapping

```text
200 OK
201 Created
204 No Content

400 Bad Request
401 Unauthorized
403 Forbidden
404 Not Found
409 Conflict
422 Unprocessable Entity
429 Too Many Requests
500 Internal Server Error
503 Service Unavailable
```

---

# 70. API Versioning

Base:

```text
/api/v1
```

Breaking changes:

```text
v2
```

Non-breaking additions remain in v1 after compatibility review.

---

# 71. Pagination

Paginated response:

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

Non-paginated reference data:

```json
[
  {
    "id": "uuid",
    "name": "Travel"
  }
]
```

Do not mix patterns arbitrarily.

---

# 72. Statistics Service

Use:

```text
StatisticsService
```

Queries:

```text
active users
learning sessions
words studied
accuracy
learning minutes
AI cost
```

Use read-optimized queries/projections rather than loading full entity graphs.

---

# 73. Gamification Service

XP:

```text
XpLog
```

Streak:

```text
Streak
```

Badges:

```text
Badge
UserBadge
```

Service:

```text
GamificationService
```

XP and streak should be derived from authoritative learning events.

---

# 74. Notification Service

Class:

```text
NotificationService
```

Responsibilities:

```text
create reminder
schedule
send
retry
record status
```

Provider:

```text
FCM
```

V1 timing:

```text
SRS due
+
configured/default study schedule
```

---

# 75. Background Jobs

Recommended jobs:

```text
Daily Plan pre-generation
Idempotency key cleanup
AI usage aggregation
Notification sending
Expired token/content cleanup
Optional vocabulary/TTS pre-generation
```

Use Spring Scheduler for MVP.

If jobs become distributed later, migrate to a queue/scheduler.

---

# 76. Daily Plan Pre-generation

Optional optimization:

```text
nightly / early morning
 ↓
generate tomorrow's plan
 ↓
save daily_plans
```

Still regenerate on demand when:

```text
plan missing
user profile changes
major learning state changed
```

---

# 77. Database Migration

Flyway.

Naming:

```text
V1__create_users.sql
...
V30__add_ai_content_review_note.sql
V31__add_idempotency_keys.sql
```

Never edit a migration already applied to a shared environment.

---

# 78. Current Schema Additions

Current baseline requires:

```text
ai_generated_content.review_note
idempotency_keys
user_vocabulary_progress.version
streaks.version
```

Database v1.6 explicitly includes the AI review note and idempotency table design. 

---

# 79. Important Database Indexes

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

idempotency_keys(user_id, created_at)
idempotency_keys(endpoint, created_at)
```

---

# 80. Database Deletion Policy

Prefer soft/deactivate behavior for content referenced by history:

```text
Vocabulary
Topic
AI Content
```

Do not hard delete data required for:

```text
learning history
audit
analytics
```

---

# 81. Ownership and Access

Every user-owned query must include:

```text
user_id = authenticatedUserId
```

Never trust user-provided `userId` for `/me` data.

---

# 82. Admin Data Access

Admin can manage:

```text
users
vocabulary
topics
quizzes
AI reusable content
statistics
audit logs
AI usage
```

Admin should not arbitrarily mutate:

```text
user_vocabulary_progress
session_attempts
quiz attempt history
```

without an explicit support/admin requirement.

---

# 83. Audit Service

Class:

```text
AuditService
```

API:

```java
auditService.record(
    adminId,
    action,
    targetTable,
    targetId,
    details
);
```

Actions:

```text
CREATE
UPDATE
DEACTIVATE
APPROVE
REJECT
LOCK_USER
UNLOCK_USER
```

---

# 84. AI Reject Audit

When reject succeeds:

```text
ai_generated_content
→ status/review_note/reviewer/time

admin_audit_logs
→ REJECT + details.reason
```

These must be part of the same logical admin operation.

---

# 85. Content Lifecycle

Vocabulary:

```text
ACTIVE
INACTIVE
```

AI reusable content:

```text
PENDING_REVIEW
APPROVED
REJECTED
```

Quiz:

```text
DRAFT
PUBLISHED
```

Use explicit state transitions.

---

# 86. State Transition Validation

Examples:

```text
DRAFT → PUBLISHED
valid

PUBLISHED → DRAFT
only through explicit unpublish rule

PENDING_REVIEW → APPROVED
valid

PENDING_REVIEW → REJECTED
valid

REJECTED → APPROVED
optional re-review flow
```

Invalid transitions return:

```text
409 or 400
```

according to API contract.

---

# 87. Caching Strategy

Cache suitable reference/read data:

```text
CEFR levels
Goals
Topics
Approved reusable AI content
selected vocabulary metadata
```

Do not cache authoritative mutations as truth.

---

# 88. Cache Invalidation

When content changes:

```text
Vocabulary updated
→ invalidate affected vocabulary cache

AI content approved/rejected
→ invalidate related AI content cache
```

If Redis unavailable:

```text
serve from PostgreSQL
```

where practical.

---

# 89. Query Optimization

Avoid:

```text
N+1
huge entity graphs
unbounded queries
SELECT * on admin analytics
```

Use:

```text
projections
pagination
fetch joins carefully
batch operations
```

---

# 90. Concurrency Rules

Protected state:

```text
UserVocabularyProgress
Streak
IdempotencyKey
```

Strategies:

```text
Optimistic locking
Primary key conflict
Request idempotency
```

Do not use database pessimistic locks by default.

---

# 91. Time Handling

Store:

```text
TIMESTAMPTZ
```

Use:

```text
UTC internally
```

Convert to user timezone for:

```text
Daily Plan date
Notifications
display
```

Use `Clock` injection for tests.

---

# 92. Timezone

User profile:

```text
timezone
```

Examples:

```text
Asia/Ho_Chi_Minh
Asia/Tokyo
America/New_York
```

Never infer user timezone only from server timezone.

---

# 93. Daily Plan Date Semantics

Daily plan is evaluated using:

```text
user timezone
```

not server local time.

Concept:

```text
today(userTimezone)
```

---

# 94. AI Content Expiration

Personalized content may use:

```text
expires_at
```

Cleanup only when no longer required for audit/debug semantics.

Reusable approved content generally does not expire unless configured.

---

# 95. External Service Resilience

LLM/TTS/FCM/Object Storage:

```text
timeout
limited retry
controlled failure
logging
metrics
```

Do not blindly retry non-idempotent external actions.

---

# 96. AI Retry

AI generation:

```text
try
 ↓
schema validation
 ↓
invalid
 ↓
limited retry
 ↓
fallback
```

Never infinite retry.

---

# 97. Logging Policy

Log:

```text
requestId
traceId
userId when safe
module
operation
duration
result
error code
```

Never log:

```text
password
JWT
refresh token
API keys
full sensitive payloads
```

---

# 98. Observability

Spring Boot Actuator:

```text
health
readiness
liveness
metrics
```

Important metrics:

```text
HTTP latency
HTTP error rate
DB latency
SRS processing time
Daily Plan generation time
AI request count
AI error count
AI blocked count
AI cost
```

---

# 99. Traceability

Propagate:

```text
traceId
requestId
```

across:

```text
Client
 ↓
Spring Boot
 ↓
AI Provider
 ↓
Database
```

---

# 100. Security Headers

Use standard Spring Security protections.

Production:

```text
HTTPS
CORS restricted
secure cookies if applicable
content security policy where applicable
```

Admin frontend origin must be explicitly allowed.

---

# 101. CORS

Android does not require browser CORS.

Admin Web:

```text
allow known origin(s)
```

Do not use:

```text
*
```

for production authenticated admin APIs.

---

# 102. Rate Limiting

Recommended:

```text
login
AI generation
personalized exercise
admin AI generation
```

Limits configured per environment.

Redis may be used later for distributed rate limiting.

---

# 103. API Ownership

Learner:

```text
/api/v1/auth/**
/api/v1/users/**
/api/v1/goals/**
/api/v1/cefr-levels/**
/api/v1/vocabulary/**
/api/v1/learning/**
/api/v1/quizzes/**
/api/v1/progress/**
/api/v1/gamification/**
/api/v1/notifications/**
```

Admin:

```text
/api/v1/admin/**
```

---

# 104. API ↔ Domain Boundary

API exposes:

```text
business capabilities
```

Examples:

```text
GET /api/v1/learning/today
POST /api/v1/learning/attempts
GET /api/v1/progress
```

Do not expose internal algorithms such as:

```text
POST /api/v1/srs/calculate
POST /api/v1/ml/predict-forgetting
POST /api/v1/ai/generate-daily-plan
```

unless a real client need exists.

This separation is explicitly required by the architecture. 

---

# 105. Recommended Controllers

```text
AuthController
UserController
ProfileController
GoalController
AssessmentController
VocabularyController
LearningController
ProgressController
QuizController
GamificationController
NotificationController

AdminUserController
AdminVocabularyController
AdminTopicController
AdminQuizController
AdminAiContentController
AdminStatisticsController
AdminAiUsageController
AdminAuditController
```

---

# 106. Recommended Services

```text
AuthService
TokenService
BruteForceService

UserService
ProfileService
GoalService
AssessmentService

VocabularyService
TopicService
ExampleService

LearningService
LearningAttemptService
LearningSessionService

SrsService
WeakWordService
ForgettingRiskService
RecommendationService
DifficultyAdjustmentService
DailyPlanService
PersonalizationService

QuizService
QuizAttemptService

ProgressService
GamificationService
NotificationService

AiContentService
AiGenerationService
AiBudgetGuard
AiUsageService
AiValidationService
AiSafetyService

AdminService
AuditService
IdempotencyService
```

---

# 107. Recommended Repositories

```text
UserRepository
RefreshTokenRepository
UserProfileRepository
GoalRepository
UserGoalRepository
CefrLevelRepository
UserLevelAssessmentRepository

TopicRepository
VocabularyRepository
VocabularyTopicRepository
VocabularyExampleRepository

UserVocabularyProgressRepository
LearningSessionRepository
SessionAttemptRepository
DailyPlanRepository

QuizRepository
QuizQuestionRepository
QuizAttemptRepository
QuizAttemptAnswerRepository

StreakRepository
XpLogRepository
BadgeRepository
UserBadgeRepository

NotificationRepository

AiGeneratedContentRepository
AiRequestRepository
AiUsageDailyRepository

AdminAuditLogRepository
IdempotencyKeyRepository
```

---

# 108. Learning Attempt Implementation Skeleton

```java
@Transactional
public LearningAttemptResponse submitAttempt(
        UUID userId,
        LearningAttemptRequest request) {

    return idempotencyService.execute(
        userId,
        "/api/v1/learning/attempts",
        request.getEventId(),
        request,
        () -> {
            validateRequest(request);

            LearningSession session =
                sessionService.getOwnedSession(
                    userId,
                    request.getSessionId());

            Vocabulary vocabulary =
                vocabularyService.getActive(
                    request.getVocabularyId());

            UserVocabularyProgress progress =
                progressRepository
                    .findByUserIdAndVocabularyId(
                        userId,
                        vocabulary.getId())
                    .orElseGet(...);

            SrsResult srs =
                srsService.calculate(
                    progress,
                    request.getAnswerQuality(),
                    request.getResponseTimeMs());

            progress.apply(srs);

            sessionAttemptService.record(
                userId,
                session,
                vocabulary,
                request,
                srs);

            gamificationService.applyLearningEvent(...);
            return buildResponse(...);
        }
    );
}
```

Implementation details may differ, but business order must remain transactionally consistent.

---

# 109. Idempotency Service Skeleton

```java
public <T> T execute(
        UUID userId,
        String endpoint,
        UUID eventId,
        Object request,
        Supplier<T> operation) {

    String requestHash =
        requestHashService.sha256Canonical(request);

    Optional<IdempotencyKey> existing =
        repository.findById(eventId);

    if (existing.isPresent()) {
        validateReuse(existing.get(),
                      userId,
                      endpoint,
                      requestHash);

        return replay(existing.get());
    }

    claim(eventId, userId, endpoint, requestHash);

    T response = operation.get();

    storeResponse(
        eventId,
        response,
        httpStatusOf(response));

    return response;
}
```

Actual implementation must account for concurrent claims and transaction behavior.

---

# 110. SRS Service Skeleton

```java
public SrsResult calculate(
        UserVocabularyProgress progress,
        int answerQuality,
        long responseTimeMs) {

    validateQuality(answerQuality);

    if (answerQuality < 3) {
        return calculateFailure(...);
    }

    return calculateSuccess(...);
}
```

The exact formula must follow the separately approved AI Personalization specification and its tests. The backend must have one implementation only.

---

# 111. Daily Plan Service Skeleton

```java
public DailyPlan generate(User user, LocalDate date) {

    ReviewSet reviewSet =
        reviewSelector.select(user);

    NewWordSet newWords =
        newWordSelector.select(user);

    QuizSet quizzes =
        quizSelector.select(user);

    Workload workload =
        workloadGuard.balance(
            user.getDailyLearningMinutes(),
            reviewSet,
            newWords,
            quizzes);

    return dailyPlanRepository.save(
        DailyPlan.create(
            user,
            date,
            workload));
}
```

---

# 112. AI Content Review Service Skeleton

Approve:

```text
load content
→ verify PENDING_REVIEW
→ set APPROVED
→ reviewed_by
→ reviewed_at
→ review_note unchanged/null according to policy
→ audit
→ commit
```

Reject:

```text
load content
→ verify PENDING_REVIEW
→ validate reason
→ set REJECTED
→ review_note = reason
→ reviewed_by
→ reviewed_at
→ audit.details.reason = reason
→ commit
```

---

# 113. Admin Security Boundary

Admin endpoints must use:

```text
@PreAuthorize("hasRole('ADMIN')")
```

or equivalent route-level configuration.

Do not rely only on hidden UI buttons.

---

# 114. Test Package Structure

```text
src/test/java/com/example/englishaicoach/

├── auth/
├── user/
├── assessment/
├── vocabulary/
├── learning/
├── personalization/
├── quiz/
├── progress/
├── gamification/
├── notification/
├── ai/
├── admin/
├── audit/
└── common/
```

---

# 115. Unit Tests

Highest priority:

```text
SrsService
DailyPlanService
WorkloadGuard
WeakWordService
ForgettingRiskService
RecommendationService
DifficultyAdjustmentService
IdempotencyService
AiBudgetGuard
```

---

# 116. SRS Test Matrix

Test:

```text
q=0
q=1
q=2
q=3
q=4
q=5
```

Also:

```text
new card
first success
repeated success
failure after review
min interval
max interval
response time boundaries
```

---

# 117. Idempotency Test Matrix

### Test 1

```text
event A
request X
→ process once
```

### Test 2

```text
event A
request X
retry
→ no second mutation
→ same response
```

### Test 3

```text
event A
request Y
→ 409
```

### Test 4

```text
event A
endpoint B
→ 409
```

### Test 5

```text
same event
two concurrent requests
→ one logical execution
```

---

# 118. Optimistic Lock Test

```text
version = 5

request A → update → version 6
request B using version 5
→ 409
```

No silent overwrite.

---

# 119. Authentication Tests

```text
register
duplicate email
login success
wrong password
5 failures
temporary lock
lock expiry
refresh success
refresh expired
refresh revoked
logout
admin authorization
```

---

# 120. AI Tests

```text
valid response
malformed JSON
missing field
unsafe content
budget exceeded
provider timeout
provider 5xx
duplicate generation key
```

---

# 121. Integration Testing

Use:

```text
@SpringBootTest
Testcontainers
PostgreSQL
```

Test real:

```text
transaction
Flyway schema
JPA
optimistic locking
idempotency
API status
security
```

---

# 122. API Contract Testing

Check:

```text
OpenAPI
DTO
Controller
Status codes
Request validation
Response shape
Pagination
Errors
```

must remain synchronized.

---

# 123. Repository Testing

Use PostgreSQL Testcontainer for queries involving:

```text
JSONB
UUID
timestamps
indexes
constraints
optimistic locking
```

Do not assume H2 behaves exactly like PostgreSQL.

---

# 124. Performance Tests

Baseline targets:

```text
normal GET p50 < 300ms
normal GET p95 < 800ms
```

excluding:

```text
external AI generation
cold infrastructure
```

Load test high-volume:

```text
review query
learning attempt
daily plan
admin lists
```

---

# 125. Security Testing

Test:

```text
unauthorized endpoint
user accesses another user's data
USER accesses ADMIN endpoint
expired JWT
revoked refresh token
brute force
invalid UUID
malicious input
```

---

# 126. Dependency Failure Tests

LLM unavailable:

```text
fallback
```

Redis unavailable:

```text
DB fallback
```

Object storage unavailable:

```text
metadata works
media fails gracefully
```

Architecture explicitly requires graceful degradation. 

---

# 127. Configuration

Use:

```text
application.yml
application-local.yml
application-test.yml
application-prod.yml
```

Configuration groups:

```text
database
jwt
security
learning
srs
ai
notification
storage
redis
logging
```

---

# 128. Environment Variables

Examples:

```text
DB_URL
DB_USERNAME
DB_PASSWORD

JWT_SECRET

GOOGLE_CLIENT_ID
GOOGLE_CLIENT_SECRET

LLM_API_KEY
TTS_API_KEY

REDIS_URL

OBJECT_STORAGE_ENDPOINT
OBJECT_STORAGE_ACCESS_KEY
OBJECT_STORAGE_SECRET_KEY
```

Never commit secrets.

---

# 129. Feature Flags

Recommended:

```text
ai.exampleGeneration.enabled
ai.cefrSuggestion.enabled
ai.personalizedExercise.enabled
notification.enabled
```

Allows controlled rollout.

---

# 130. Build Configuration

Recommended Maven or Gradle.

Suggested Maven structure:

```text
pom.xml
src/main/java
src/main/resources
src/test/java
src/test/resources
```

Dependency versions should be centrally managed.

---

# 131. CI Pipeline

```text
Checkout
 ↓
Build
 ↓
Static checks
 ↓
Unit tests
 ↓
Integration tests
 ↓
OpenAPI validation
 ↓
Package
```

PR must pass before merge.

---

# 132. Deployment

Recommended V1:

```text
Docker
Docker Compose
```

Services:

```text
backend
postgres
redis (optional)
admin-web
```

External:

```text
LLM provider
TTS
FCM
Object Storage
```

---

# 133. Production Deployment

```text
Internet
   ↓
Reverse Proxy
   ↓
Spring Boot
   ↓
PostgreSQL
```

Admin Web may be served:

```text
same domain
or
separate frontend origin
```

with strict CORS.

---

# 134. Health Checks

Endpoints:

```text
/actuator/health
```

Readiness checks:

```text
database
essential dependencies
```

AI provider should not necessarily make the whole application unhealthy if core learning remains operational.

---

# 135. Backup

PostgreSQL:

```text
daily backup
```

For demo:

```text
manual pg_dump
```

Document restore procedure.

---

# 136. Data Retention

Idempotency:

```text
30 days
```

AI logs:

```text
project-defined retention
```

Audit:

```text
longer retention
```

Learning history:

```text
retain for personalization/analytics
```

---

# 137. Developer Workflow

For each feature:

```text
Read SRS
 ↓
Read API
 ↓
Read database mapping
 ↓
Define DTO
 ↓
Entity/repository
 ↓
Service
 ↓
Controller
 ↓
Tests
 ↓
OpenAPI
```

---

# 138. Coding Rules

Use:

```text
constructor injection
```

Avoid:

```text
field injection
```

Prefer:

```text
immutable DTOs where practical
small services
explicit names
no magic constants
```

---

# 139. Business Rule Location

Rules belong in backend services.

Examples:

```text
SRS
→ SrsService

Daily workload
→ WorkloadGuard

Weak words
→ WeakWordService

AI budget
→ AiBudgetGuard

Gamification
→ GamificationService
```

---

# 140. No Business Logic in Controllers

Bad:

```java
if (answerQuality >= 3) {
   ...
}
```

inside controller.

Good:

```text
Controller
→ LearningAttemptService
→ SrsService
```

---

# 141. No Business Logic in Client

Android/Flutter should not independently calculate:

```text
SRS
weakness
forgetting risk
recommendation
daily workload
XP
streak
```

Client renders backend output.

---

# 142. Backend-Authoritative Time

Server determines:

```text
now
nextReviewAt
daily plan date
notification timing
```

using user timezone where appropriate.

---

# 143. Backend-Authoritative Aggregates

Server determines:

```text
accuracy
learned count
mastered count
streak
XP
quiz score
```

Client may display local optimistic UI but final state comes from server.

---

# 144. API Response Mapping

Typical flow:

```text
Entity
 ↓
Domain Result
 ↓
Response DTO
 ↓
JSON
```

Never serialize Hibernate proxy graphs.

---

# 145. Mapping Strategy

Can use:

```text
manual mapper
or
MapStruct
```

Recommendation:

```text
MapStruct
```

for larger number of DTO mappings.

---

# 146. Query Projections

Admin statistics should prefer:

```text
interface projections
record DTO projections
native/JPQL aggregate queries
```

rather than loading thousands of entities.

---

# 147. Transaction Event Pattern

For non-critical side effects:

```text
learning transaction commits
 ↓
publish internal event
 ↓
notification/analytics side effect
```

But:

```text
SRS state
session attempt
XP/streak
```

must remain consistent with the core learning transaction according to business requirements.

---

# 148. Domain Events

Potential internal events:

```text
LearningAttemptRecorded
ReviewCompleted
QuizCompleted
AiContentApproved
AiContentRejected
UserLocked
```

Use these only where they improve decoupling.

---

# 149. Notification Event

Example:

```text
Review due
 ↓
NotificationScheduler
 ↓
NotificationService
 ↓
FCM
```

Do not send FCM directly from controller.

---

# 150. AI Content Generation Job

Reusable content can be generated asynchronously:

```text
Admin request
 ↓
create generation task
 ↓
AI generation
 ↓
validation
 ↓
PENDING_REVIEW
```

For MVP, synchronous generation is acceptable where latency is acceptable, but generated reusable content should be persisted/cached.

---

# 151. Concurrency on AI Content

If two admins request same content:

```text
generation_key
```

should be used to avoid unnecessary duplicate generation.

Use database uniqueness/lookup before generation.

---

# 152. Generation Key

Example concept:

```text
EXAMPLE|vocabularyId|promptVersion|language
```

Hash if needed.

This supports:

```text
deduplication
cache reuse
```

---

# 153. Admin Reject Consistency

Reject operation must atomically update:

```text
AI content
+
review note
+
reviewer
+
review time
+
audit log
```

Partial state is not acceptable.

---

# 154. AI Usage Consistency

When AI request succeeds/fails/blocks:

```text
ai_requests
```

must be written.

Daily aggregate:

```text
ai_usage_daily
```

must be updated consistently or through a reliable aggregation strategy.

---

# 155. AI Budget Race Condition

Two simultaneous AI requests can both see remaining budget.

Guard must be designed to prevent significant overspend.

For simple MVP:

```text
transaction / atomic counter
```

or:

```text
Redis atomic operation
```

may be used.

Do not rely only on:

```text
read usage
→ compare
→ write usage
```

without considering concurrency.

---

# 156. Personalized Exercise Cost Control

Before LLM call:

```text
AiBudgetGuard.check()
```

If blocked:

```text
fallback to standard practice
```

not:

```text
retry forever
```

---

# 157. API Request Correlation

Every request should have:

```text
traceId
requestId
```

where practical.

Store no sensitive data in those IDs.

---

# 158. Admin Audit Correlation

Audit should include enough detail to connect to:

```text
traceId
```

if the implementation includes it in `details`.

---

# 159. Database Connection Pool

Use HikariCP through Spring Boot.

Configure:

```text
maximumPoolSize
minimumIdle
connectionTimeout
idleTimeout
maxLifetime
```

Tune after measurement.

---

# 160. JPA Fetch Strategy

Default:

```text
LAZY for associations
```

Avoid:

```text
EAGER everywhere
```

Fetch data explicitly per use case.

---

# 161. N+1 Prevention

For review list:

```text
projection
or
fetch join
```

Avoid:

```text
for each progress
→ query vocabulary
→ query topic
```

---

# 162. Database Constraint Enforcement

Use both:

```text
application validation
+
database constraints
```

Examples:

```text
unique user+vocabulary progress
unique user+goal
unique primary goal partial index
unique eventId
unique provider OAuth identity
```

Database constraints protect against bugs from future code paths.

---

# 163. Test Data Strategy

Use deterministic fixtures:

```text
UserFixture
VocabularyFixture
ReviewFixture
QuizFixture
AiContentFixture
```

Do not rely on production-like random data for unit tests.

---

# 164. Test Clock

Use:

```java
Clock
```

Inject into:

```text
SrsService
DailyPlanService
NotificationService
TokenService
```

This makes date/time tests deterministic.

---

# 165. Local Development

Recommended:

```text
Docker Compose
├── postgres
├── redis optional
└── minio optional
```

Spring Boot can run:

```text
from IDE
```

against local services.

---

# 166. Backend Local Start

```text
1. Start PostgreSQL
2. Run Flyway
3. Start Spring Boot
4. Open Swagger UI
5. Verify health
6. Run integration tests
```

---

# 167. Swagger Runtime

Expose:

```text
Swagger UI
OpenAPI JSON/YAML
```

only as appropriate for environment.

Production may restrict Swagger to internal/admin access.

---

# 168. Security Review Checklist

```text
[ ] Password hashing
[ ] JWT expiry
[ ] Refresh token revocation
[ ] Brute-force protection
[ ] RBAC
[ ] Ownership checks
[ ] CORS
[ ] HTTPS
[ ] Rate limiting
[ ] Secret management
[ ] Input validation
[ ] Audit logging
[ ] No secret logging
```

---

# 169. Learning Integrity Checklist

```text
[ ] answerQuality is source of truth
[ ] isCorrect derived on server
[ ] eventId checked
[ ] duplicate retry does not reapply SRS
[ ] optimistic locking enabled
[ ] transaction atomic
[ ] progress state updated
[ ] historical attempt written
[ ] XP/streak consistent
```

---

# 170. AI Integrity Checklist

```text
[ ] budget checked before billable request
[ ] request logged
[ ] response validated
[ ] safety checked
[ ] reusable content reviewed
[ ] rejection note persisted
[ ] rejection audited
[ ] personalized content skips manual review
[ ] fallback exists
```

---

# 171. Admin Integrity Checklist

```text
[ ] ADMIN role required
[ ] destructive actions confirmed
[ ] AI rejection reason required
[ ] audit record written
[ ] statistics use read-optimized queries
[ ] pagination used
```

---

# 172. Backend Definition of Done

A backend feature is Done when:

```text
[ ] SRS rule satisfied
[ ] Database mapping correct
[ ] DTO created
[ ] Validation implemented
[ ] Service implemented
[ ] Repository implemented
[ ] Transaction boundary reviewed
[ ] Security reviewed
[ ] Error handling implemented
[ ] Tests pass
[ ] OpenAPI updated
[ ] Logging/metrics considered
[ ] Migration included if needed
```

---

# 173. Critical End-to-End Test

## Learning Attempt

```text
Login
 ↓
Start session
 ↓
Get Daily Plan
 ↓
Start review
 ↓
Flashcard
 ↓
Submit answerQuality=4
 ↓
eventId=A
 ↓
SRS update
 ↓
attempt inserted
 ↓
XP/streak
 ↓
response
```

Retry:

```text
same eventId=A
 ↓
replay
 ↓
no duplicate state change
```

---

# 174. Critical Admin AI Test

```text
Admin login
 ↓
AI Content
 ↓
Reject
 ↓
reason
 ↓
status=REJECTED
 ↓
review_note saved
 ↓
audit saved
 ↓
detail displays review note
```

---

# 175. Critical Personalization Test

```text
User reviews words
 ↓
attempt history grows
 ↓
weak words change
 ↓
forgetting risk changes
 ↓
Daily Plan changes
 ↓
Personalized Exercise targets weak words
```

The Personalization specification requires the end-to-end path from algorithm specification through unit/boundary/integration/concurrency/database transaction tests and client integration. 

---

# 176. Technical Risks

## Risk 1

```text
SRS implementation differs from specification
```

Mitigation:

```text
one SrsService
unit tests
boundary tests
```

## Risk 2

```text
duplicate mobile attempts
```

Mitigation:

```text
idempotency_keys
eventId
requestHash
```

## Risk 3

```text
concurrent progress updates
```

Mitigation:

```text
@Version
409
```

## Risk 4

```text
AI cost overrun
```

Mitigation:

```text
Budget Guard
usage aggregation
limits
```

## Risk 5

```text
AI content quality
```

Mitigation:

```text
validation
safety
admin review
```

---

# 177. Implementation Priority

## P0

```text
Project foundation
Database/Flyway
Security
Auth
User/Profile
Vocabulary
Assessment
Learning Session
Learning Attempt
Idempotency
Optimistic Locking
SRS
Review
```

## P1

```text
Daily Plan
Weak Words
Forgetting Risk
Recommendation
Progress
Quiz
Gamification
Notification
```

## P2

```text
AI reusable content
Personalized Exercise
AI Usage
Advanced analytics
```

AI can be introduced after core learning integrity is stable.

---

# 178. First Sprint

```text
1. Create Spring Boot project
2. Configure PostgreSQL
3. Configure Flyway
4. Create baseline migrations
5. Add JPA
6. Add global exception handling
7. Add security dependencies
8. Add Actuator
9. Add Swagger
10. Create CI skeleton
```

---

# 179. Second Sprint

```text
1. User entity
2. User Profile
3. Refresh Token
4. Auth service
5. JWT filter
6. Login
7. Register
8. Refresh
9. Logout
10. Brute-force protection
```

---

# 180. Third Sprint

```text
1. Goals
2. CEFR
3. Vocabulary
4. Topics
5. Assessment
```

---

# 181. Fourth Sprint — Learning Core

```text
1. Learning Session
2. Idempotency
3. User Vocabulary Progress
4. Session Attempt
5. SRS
6. Optimistic Locking
7. Review
```

This is the most important backend sprint.

---

# 182. Fifth Sprint — Personalization

```text
1. Weak Words
2. Forgetting Risk
3. Daily Plan
4. Workload Guard
5. Recommendation
6. Difficulty Adjustment
```

---

# 183. Sixth Sprint

```text
1. Quiz
2. Progress
3. Gamification
4. Notification
```

---

# 184. Seventh Sprint

```text
1. AI provider abstraction
2. Budget Guard
3. AI usage
4. Reusable content
5. AI review
6. Personalized Exercise
```

---

# 185. Final Backend Architecture

```text
                    REST API
                       │
             ┌─────────┴─────────┐
             │                   │
        Mobile Client        Admin Client
             │                   │
             └─────────┬─────────┘
                       ▼
              Spring Boot Backend
                       │
      ┌────────────────┼────────────────┐
      │                │                │
      ▼                ▼                ▼
    Core          Personalization       AI
      │                │                │
      │                │                ├── Budget
      │                │                ├── LLM
      │                │                ├── Validation
      │                │                └── Safety
      │                │
      │                ├── SRS
      │                ├── Weakness
      │                ├── Forgetting
      │                ├── Recommendation
      │                └── Daily Plan
      │
      └──────────────┬───────────────┐
                     ▼               ▼
                PostgreSQL        External Services
                                  ├── TTS
                                  ├── FCM
                                  └── Object Storage
```

---

# 186. Final Data Integrity Model

```text
eventId
   ↓
idempotency_keys
   ↓
ONE logical operation
   ↓
transaction
   ├── SRS
   ├── session_attempt
   ├── progress
   ├── XP/streak
   └── response
```

---

# 187. Final AI Content Model

```text
REUSABLE
  ↓
LLM
  ↓
Validate
  ↓
Admin Review
  ↓
Approve
  ↓
Cache
  ↓
Users

PERSONALIZED
  ↓
Personalization
  ↓
LLM
  ↓
Validate
  ↓
Safety
  ↓
User
```

---

# 188. Final Technical Baseline

```text
Backend
Java + Spring Boot

Architecture
Modular Monolith

Database
PostgreSQL

ORM
Spring Data JPA / Hibernate

Migration
Flyway

Security
Spring Security + JWT

API
REST / JSON /api/v1

Concurrency
Optimistic Locking

Idempotency
idempotency_keys + eventId + requestHash

Personalization
Rule + Heuristic + Extended SM-2

AI
Provider abstraction + Budget Guard

Storage
PostgreSQL + Object Storage

Cache
Redis optional

Observability
Actuator + structured logging

Testing
JUnit + Mockito + Testcontainers
```

---

# 189. Final Implementation Gate

Before writing business code:

```text
[ ] Database Schema v1.6 reviewed
[ ] System Architecture v1.3 reviewed
[ ] API Specification v1.4 reviewed
[ ] OpenAPI v1.4 reviewed
[ ] Idempotency contract reviewed
[ ] SRS formula implemented/testable
[ ] Optimistic locking reflected in entities
[ ] AI review note reflected in entity/API
[ ] Security boundaries defined
[ ] Transaction boundaries defined
[ ] Error model defined
[ ] Test strategy defined
```

---

# 190. Final Next Step

```text
Backend Technical Specification
          ↓
Create Spring Boot repository
          ↓
PostgreSQL + Flyway
          ↓
Spring Security
          ↓
Auth
          ↓
User/Profile
          ↓
Vocabulary
          ↓
Assessment
          ↓
Learning Core
          ↓
SRS + Personalization
          ↓
AI
```

**Tài liệu này đánh dấu việc chuyển từ “system design” sang “backend implementation”.**


---

# 191. Final Idempotency Concurrency Decision

This section is normative and uses the same canonical PostgreSQL rule as Section 23.

```text
Do not rely on:
findById(eventId)
→ insert
```

Use:

```sql
INSERT INTO idempotency_keys(event_id, user_id, endpoint, request_hash, created_at)
VALUES (:eventId, :userId, :endpoint, :requestHash, now())
ON CONFLICT (event_id) DO NOTHING;
```

Then follow one unambiguous flow:

```text
claim inserted
→ execute the business mutation
→ persist the successful response snapshot/status
→ commit the mutation and idempotency result atomically

claim not inserted
→ load the existing eventId
→ compare user_id separately + endpoint identity + SHA-256 canonical request_hash
→ same logical request: replay the stored response
→ different logical request: HTTP 409 IDEMPOTENCY_KEY_REUSE
```

Do not use unique-constraint exceptions, including `DataIntegrityViolationException`, as normal duplicate-claim control flow. Do not catch a duplicate-key exception and continue work in the same failed PostgreSQL transaction. If the business mutation fails, the claim rolls back in the same transaction. Expected duplicate races must not escape as HTTP 500.

---

# 192. Final Backend Baseline

```text
Database Schema       v1.6
System Architecture   v1.3
AI Personalization    v1.3
API Specification     v1.4
OpenAPI / Swagger     v1.4
Technical Specification v1.2
Backend Technical Specification v1.3
```

---

# Reconciled Backend Domain Baseline v1.3

## Assessment

Implement `assessment-block-v1` exactly as AI Personalization v1.3/API v1.4. Persist aggregate state plus `assessment_items`. Start/answer require `eventId`; GET next question only reads persisted state.

## SRS

Implement state transitions NEW→LEARNING→REVIEWING→MASTERED using mastered thresholds repetitions>=5, interval>=30d, entry quality>=4; REVIEWING/MASTERED regress to LEARNING on q<3. `answerQuality` is required, backend derives correctness, DB enforces it.

## Personalization/Daily Plan

Implement canonical weakness/risk/ranking/workload constants from AI v1.3. Daily Plan is persisted snapshot with `daily_plan_items`; no recalculation on repeated GET.

## Gamification

XP: learning correct 5, quiz correct 5, session complete 10, Daily Plan complete 50, new qualifying streak day 10. Level = floor(totalXp/500)+1. Awarding must be retry-safe/idempotent.

## Notification

Persist `user_devices` and `notification_preferences`. Never log push tokens. Schedule local 07:00 Daily Plan, preferred-study-time review, local 21:00 streak; dedupe reminder type/user/local date.

## AI

Admin reusable generation is synchronous 201/PENDING_REVIEW; personalized exercise is synchronous 200. Event retry must not call the LLM or charge budget twice. Admin AI CEFR Suggestion is not V1.
