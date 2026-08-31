# System Architecture v1.3 — English AI Coach

**Project:** English AI Coach  
**Architecture Version:** 1.3  
**Status:** APPROVED BASELINE  
**Date:** 2026-08-31  

**Related documents:**
- `English_AI_Coach_SRS_v1.2.md`
- `English_AI_Coach_Database_Schema_v1.6.md`

---

# 1. Architecture Overview

English AI Coach is a vocabulary-learning platform centered on:

```text
Vocabulary Learning
+
Adaptive Spaced Repetition
+
Learning Analytics
+
Personalized Daily Plan
+
AI Content Generation
```

The first version focuses on vocabulary learning and AI-assisted personalization.

The architecture must support future expansion into:

```text
Speaking
Writing
Listening
Reading
AI Tutor
Advanced Machine Learning
```

without replacing the existing learning core.

---

# 2. Architectural Goals

The architecture must:

1. Separate Mobile, Backend, Database, AI, Storage, and Admin concerns.
2. Allow Android Java and Flutter to use the same backend API.
3. Keep clients isolated from PostgreSQL and AI provider credentials.
4. Keep Personalization logic independent of any specific LLM provider.
5. Support deterministic Rule/Heuristic personalization in V1.
6. Preserve historical learning data for future ML.
7. Provide clear USER/ADMIN security boundaries.
8. Support reusable AI content caching and human review.
9. Support personalized AI content generation without mandatory manual review.
10. Provide AI budget and usage control.
11. Protect concurrent updates to important user-learning state.
12. Remain simple enough to implement as an MVP.
13. Allow future horizontal scaling.

---

# 3. Architectural Style

## 3.1. Modular Monolith

The backend uses:

> **Modular Monolith + Layered Architecture**

All backend modules initially run inside one Spring Boot application.

```text
                    Spring Boot Application
                            │
       ┌────────────────────┼────────────────────┐
       │                    │                    │
       ▼                    ▼                    ▼
   Auth Module        Learning Module       Admin Module
       │                    │                    │
       ▼                    ▼                    ▼
    Service              Service              Service
       │                    │                    │
       ▼                    ▼                    ▼
   Repository            Repository            Repository
       │                    │                    │
       └────────────────────┼────────────────────┘
                            ▼
                       PostgreSQL
```

Microservices are intentionally not required for MVP.

Reasons:

- Lower deployment complexity.
- Easier development.
- Easier debugging.
- Simpler transaction handling.
- Simpler JPA/database integration.
- No service-discovery requirement.
- No distributed transaction complexity.

Microservices can be considered only after real scaling requirements appear.

---

# 4. System Context

```text
                         ┌───────────────────┐
                         │       USER        │
                         └─────────┬─────────┘
                                   │
                                   ▼
                         ┌───────────────────┐
                         │   Android Java    │
                         │     Mobile V1     │
                         └─────────┬─────────┘
                                   │
                                   │ HTTPS / REST / JSON
                                   ▼
                  ┌────────────────────────────────────┐
                  │          SPRING BOOT API           │
                  │          MODULAR MONOLITH          │
                  └────────────────┬───────────────────┘
                                   │
        ┌──────────────────────────┼──────────────────────────┐
        │                          │                          │
        ▼                          ▼                          ▼
┌───────────────┐          ┌───────────────┐          ┌───────────────┐
│  PostgreSQL   │          │   AI Engine   │          │ Object Storage│
│   Database    │          │               │          │ Audio / Image │
└───────────────┘          └───────┬───────┘          └───────────────┘
                                   │
                          ┌────────┴────────┐
                          ▼                 ▼
                       LLM API          ML Engine
                                        (Future)


                         ┌───────────────────┐
                         │      ADMIN        │
                         └─────────┬─────────┘
                                   │
                                   ▼
                         ┌───────────────────┐
                         │    Admin Web      │
                         └─────────┬─────────┘
                                   │
                                   │ HTTPS / REST
                                   ▼
                         Spring Boot Admin API
```

---

# 5. Client Architecture

## 5.1. Mobile

### Phase 1

```text
Android Java
```

### Phase 2

```text
Flutter / Dart
```

Both clients use:

```text
/api/v1/...
```

and do not connect directly to internal infrastructure.

---

## 5.2. Admin Web

Admin Web accesses:

```text
/api/v1/admin/...
```

and requires:

```text
ROLE_ADMIN
```

---

# 6. Core Boundary Principle

The clients must not directly access:

```text
PostgreSQL
Redis
LLM Provider
Private Object Storage APIs
Internal ML Components
```

Correct:

```text
Mobile / Admin
       ↓
Spring Boot
       ↓
Domain Services
       ↓
Database / AI / Storage / Cache
```

Incorrect:

```text
Android
   ├── PostgreSQL
   └── LLM Provider
```

---

# 7. Backend Module Architecture

```text
backend/
│
├── auth/
├── user/
├── onboarding/
├── vocabulary/
├── learning/
├── personalization/
├── quiz/
├── progress/
├── gamification/
├── notification/
├── ai/
└── admin/
```

Cross-cutting packages:

```text
config/
security/
exception/
common/
storage/
```

---

# 8. Backend Module Responsibilities

## 8.1. Auth Module

Responsibilities:

- Registration.
- Login.
- Logout.
- JWT access token.
- Refresh token.
- Token expiry.
- Token revoke.
- Google OAuth.
- Brute-force protection.

Main data:

```text
users
refresh_tokens
```

---

## 8.2. User Module

Responsibilities:

- Profile.
- Account status.
- User preferences.
- Current CEFR.

Main data:

```text
users
user_profiles
```

---

## 8.3. Onboarding Module

Responsibilities:

- Goal selection.
- Daily study time.
- Initial vocabulary test.
- CEFR assessment.

Main data:

```text
goals
user_goals
cefr_levels
user_level_assessments
```

---

## 8.4. Vocabulary Module

Responsibilities:

- Vocabulary listing.
- Search.
- Filtering.
- Topic hierarchy.
- Vocabulary details.
- Examples.
- CEFR.
- Audio URL.
- Image URL.

Main data:

```text
topics
vocabulary
vocabulary_topics
vocabulary_examples
```

---

## 8.5. Learning Module

Responsibilities:

- Start sessions.
- Study new words.
- Review words.
- Record attempts.
- Update progress.

Main data:

```text
learning_sessions
session_attempts
user_vocabulary_progress
```

---

## 8.6. Personalization Module

This is the core domain of the project.

Responsibilities:

- Adaptive Testing.
- Spaced Repetition.
- Weak Word Detection.
- Forgetting Risk.
- Vocabulary Recommendation.
- Topic Recommendation.
- Difficulty Adjustment.
- New/Review balancing.
- Daily Plan generation.

Suggested internal structure:

```text
personalization/
├── srs/
├── weakness/
├── forgetting/
├── recommendation/
├── difficulty/
├── dailyplan/
└── analytics/
```

---

## 8.7. Quiz Module

Responsibilities:

- Quiz retrieval.
- Quiz execution.
- Answer evaluation.
- Quiz history.
- Feeding quiz results into learning analytics.

---

## 8.8. Progress Module

Responsibilities:

- Dashboard.
- Accuracy.
- Retention.
- Words learned.
- Words mastered.
- Learning time.
- Goal progress.
- Progress trends.

---

## 8.9. Gamification Module

Responsibilities:

- XP.
- Level.
- Streak.
- Badge.
- Leaderboard.

---

## 8.10. Notification Module

Responsibilities:

- Review reminders.
- Daily learning reminders.
- Streak reminders.
- Push scheduling.

---

## 8.11. AI Module

Responsibilities:

- LLM integration.
- AI content generation.
- AI content validation.
- AI budget guard.
- AI usage tracking.
- Provider abstraction.

---

## 8.12. Admin Module

Responsibilities:

- User management.
- Vocabulary management.
- Topic management.
- Quiz management.
- AI content review.
- AI usage.
- Audit logs.

---

# 9. Layered Architecture

Each module should follow:

```text
Controller
     ↓
DTO
     ↓
Service
     ↓
Repository
     ↓
Entity
     ↓
PostgreSQL
```

Cross-cutting services:

```text
Security
Validation
Exception Handler
Logging
AI Client
Storage Client
Cache
```

---

# 10. Controller Layer

Responsibilities:

- Receive HTTP request.
- Validate input.
- Extract authenticated user.
- Delegate to Service.
- Return Response DTO.

Controllers must not contain:

```text
SRS calculations
AI prompt construction
complex recommendation rules
direct database business logic
```

---

# 11. Service Layer

Examples:

```text
AuthService
UserService
OnboardingService
VocabularyService
LearningService
SrsService
PersonalizationService
QuizService
ProgressService
GamificationService
NotificationService
AiContentService
AdminService
```

Business rules belong here or in dedicated domain services.

---

# 12. Repository Layer

Examples:

```text
UserRepository
RefreshTokenRepository
VocabularyRepository
UserVocabularyProgressRepository
LearningSessionRepository
SessionAttemptRepository
DailyPlanRepository
QuizRepository
AiGeneratedContentRepository
AiRequestRepository
```

Repositories are responsible for persistence operations, not high-level business decisions.

---

# 13. DTO Boundary

JPA entities must not be exposed directly through public APIs.

Use:

```text
Request DTO
Response DTO
```

Flow:

```text
HTTP Request
     ↓
Request DTO
     ↓
Service
     ↓
Entity
     ↓
Repository
```

Response:

```text
Entity
     ↓
Mapper
     ↓
Response DTO
     ↓
JSON
```

---

# 14. Personalization Architecture

The Personalization Engine must be independent from LLM generation.

```text
                    PERSONALIZATION ENGINE
                              │
             ┌────────────────┼────────────────┐
             │                │                │
             ▼                ▼                ▼
          SRS Engine      Analytics        Recommendation
             │                │                │
             ▼                ▼                ▼
       Review Schedule    Weak Words       Next Vocabulary
       Next Review        Risk Score       Next Topic
             │                │                │
             └────────────────┼────────────────┘
                              ▼
                       Daily Plan Generator
                              │
                              ▼
                         DAILY_PLANS
```

V1:

```text
Rule
+
Heuristic
+
Deterministic Algorithm
```

V2+:

```text
Rule
+
ML
+
Optional LLM Explanation
```

---

# 15. SRS Architecture

```text
User Answer
     │
     ▼
Session Attempt
     │
     ▼
SRS Engine
     │
     ├── answer_quality
     ├── response_time
     ├── previous interval
     ├── repetitions
     └── ease factor
     │
     ▼
User Vocabulary Progress
     │
     ├── next_review_at
     ├── interval_days
     ├── repetitions
     └── ease_factor
```

V1:

> Extended SM-2 or equivalent deterministic SRS algorithm.

Future:

> ML-assisted forgetting prediction.

---

# 16. Learning Analytics Architecture

```text
SESSION_ATTEMPTS
       │
       ▼
Learning Analytics
       │
 ┌─────┼────────┐
 ▼     ▼        ▼
Accuracy  Speed  Retention
 │        │       │
 └────────┼───────┘
          ▼
User Learning Profile
          │
          ▼
Personalization Engine
```

Historical events must remain separate from current learning state.

```text
user_vocabulary_progress
=
CURRENT STATE

session_attempts
=
HISTORICAL EVENTS
```

---

# 17. Daily Plan Architecture

```text
User Profile
     │
User Goals
     │
Learning History
     │
SRS
     │
Weak Words
     │
Forgetting Risk
     │
Available Learning Time
     │
     ▼
Daily Plan Generator
     │
 ┌───┼────────┐
 ▼   ▼        ▼
New Review    Quiz
     │
     └────┬───┘
          ▼
     DAILY_PLANS
          │
          ▼
        USER
```

---

# 18. Quiz Architecture

```text
QUIZ
  │
  ▼
QUIZ_QUESTIONS
  │
  ▼
USER
  │
  ▼
QUIZ_ATTEMPT
  │
  ▼
QUIZ_ATTEMPT_ANSWERS
  │
  ▼
Learning Analytics
  │
  ▼
Personalization Engine
```

Quiz results contribute to the learning profile.

---

# 19. Gamification Architecture

```text
Learning Event
     │
     ├── Complete Daily Plan
     ├── Correct Answer
     ├── Complete Quiz
     └── Maintain Streak
     │
     ▼
Gamification Service
     │
     ├── XP
     ├── Level
     ├── Streak
     └── Badge
```

---

# 20. Notification Architecture

```text
SRS / Daily Plan / Streak
            │
            ▼
      Notification Service
            │
            ▼
      Notification Scheduler
            │
            ▼
        Push Provider
            │
            ▼
        Android App
```

V1:

```text
SRS due time
+
User-selected study time
```

V2:

```text
User activity
     ↓
ML
     ↓
Optimal notification time
```

---

# 21. AI Architecture

AI consists of separate concerns:

```text
                              AI LAYER
                                  │
          ┌───────────────────────┼───────────────────────┐
          │                       │                       │
          ▼                       ▼                       ▼
    Reusable Content        Personalized Content       ML Future
          │                       │                       │
          ▼                       ▼                       ▼
         LLM                     LLM                   ML Models
          │                       │                       │
          ▼                       ▼                       ▼
 Admin Review              Auto Validation          Prediction
 + Cache                   + Direct Delivery        + Analytics
```

The two AI content flows must remain distinct.

---

# 22. Reusable AI Content

Reusable content can be shared across many users.

Examples:

```text
EXAMPLE
EXPLANATION
MNEMONIC
STORY
```

Flow:

```text
Admin/System
      │
      ▼
AI Content Service
      │
      ▼
Budget Guard
      │
      ▼
LLM
      │
      ▼
Automated Validation
      │
      ▼
PENDING_REVIEW
      │
      ▼
Admin Review
      │
 ┌────┴─────┐
 ▼          ▼
APPROVED  REJECTED
 │
 ▼
Cache / Database
 │
 ▼
Many Users
```

Reusable content is stored in:

```text
ai_generated_content
```

---

# 23. Personalized / Ephemeral AI Content

Personalized content is generated for a specific user.

Example:

```text
PERSONALIZED_EXERCISE
```

Flow:

```text
User
  │
  ▼
Personalization Engine
  │
  ▼
Weak Words / Goals / CEFR
  │
  ▼
AI Content Service
  │
  ▼
Budget Guard
  │
  ▼
LLM
  │
  ▼
Automated Validation / Safety Filter
  │
  ▼
User
```

**No manual Admin Review is required for every personalized result.**

Rationale:

- Content is user-specific.
- Content may be generated on demand.
- Content is typically short-lived.
- Manual review for every personalized exercise would prevent real-time personalization.

Personalized results may optionally be stored temporarily for:

```text
debugging
audit
short-lived cache
```

but should not be treated as permanent reusable content by default.

---

# 24. AI Content Scope

The AI content model must distinguish:

```text
REUSABLE
PERSONALIZED
```

Conceptually:

```text
content_scope
```

Examples:

```text
EXAMPLE + REUSABLE
MNEMONIC + REUSABLE
STORY + REUSABLE

PERSONALIZED_EXERCISE + PERSONALIZED
```

---

# 25. AI Validation

## Reusable Content

Validation pipeline:

```text
Generate
 ↓
Schema Validation
 ↓
Content Validation
 ↓
PENDING_REVIEW
 ↓
Admin Review
```

## Personalized Content

Validation pipeline:

```text
Generate
 ↓
Schema Validation
 ↓
Content Validation
 ↓
Safety Filter
 ↓
Deliver
```

The exact validation implementation may evolve.

---

# 26. LLM Provider Abstraction

Do not couple the business logic directly to one provider.

Recommended structure:

```text
AiGenerationService
        │
        ▼
     LlmClient
        │
   ┌────┴─────┐
   ▼          ▼
Provider A  Provider B
```

Conceptual interface:

```java
public interface LlmClient {

    AiGenerationResult generate(
        AiGenerationRequest request
    );
}
```

Provider-specific implementation stays behind the interface.

Benefits:

- Provider switching.
- Easier tests.
- Lower vendor lock-in.
- Centralized error handling.
- Centralized cost tracking.

---

# 27. AI Content Cache

Reusable content should use a cache key.

Example:

```text
EXAMPLE:abandon:B1:v1
```

Flow:

```text
Request
   │
   ▼
Check generation_key
   │
 ┌─┴──────────┐
 ▼            ▼
Exists       Missing
 │            │
 ▼            ▼
Return      LLM
Cache         │
              ▼
           Store
              │
              ▼
         Admin Review
```

Do not call LLM repeatedly for the same reusable content unless regeneration is explicitly requested.

---

# 28. AI Budget Guard

Every LLM request passes through a budget guard.

```text
AI Request
    │
    ▼
Budget Guard
    │
    ├── Daily Request Limit
    ├── Daily Token Limit
    └── Daily Cost Limit
    │
    ▼
Within Budget?
 ┌──┴─────┐
 YES      NO
 │         │
 ▼         ▼
LLM      BLOCK
 │         │
 ▼         ▼
Record   ai_requests
```

Configuration is environment-specific:

```text
development
staging
production
```

Exact values are not hard-coded into architecture.

Already cached/approved content must remain usable when new generation is blocked.

---

# 29. AI Observability

Every AI call should produce an `ai_requests` record containing:

```text
user_id
feature
provider
model
request_tokens
response_tokens
total_tokens
estimated_cost
status
created_at
```

Aggregation:

```text
AI_REQUESTS
      ↓
AI_USAGE_DAILY
```

Admin can inspect:

```text
Requests
Tokens
Estimated Cost
Provider
Model
Feature
Blocked Requests
```

---

# 30. Authentication Architecture

```text
                    AUTHENTICATION
                           │
             ┌─────────────┴─────────────┐
             ▼                           ▼
          LOCAL                       GOOGLE
             │                           │
             └─────────────┬─────────────┘
                           ▼
                  Authentication Service
                           │
                 ┌─────────┴─────────┐
                 ▼                   ▼
            Access Token        Refresh Token
            Short-lived         Long-lived
                 │                   │
                 ▼                   ▼
              Client              Database
                                  (hashed)
```

---

# 31. Access Token

Target:

```text
15–30 minutes
```

Payload minimum:

```text
user_id
role
iat
exp
```

Actual expiry is configurable.

---

# 32. Refresh Token

Refresh tokens are:

```text
Long-lived
Hashed in database
Expiring
Revocable
```

Table:

```text
refresh_tokens
```

Flow:

```text
Client
  │
  ▼
Refresh Token
  │
  ▼
POST /auth/refresh
  │
  ▼
Validate hash
  │
  ├── expired → reject
  ├── revoked → reject
  └── valid → issue new Access Token
```

Logout:

```text
revoked_at = NOW()
```

---

# 33. Brute-force Protection

Baseline:

```text
5 failed login attempts
        ↓
Temporary lock
        ↓
5 minutes
```

Database fields:

```text
users.failed_login_attempts
users.locked_until
```

Flow:

```text
Login
  ↓
Check locked_until
  ↓
Verify credentials
  │
 ┌┴──────────┐
FAIL       SUCCESS
 │            │
 ▼            ▼
Increment    Reset counter
 │            │
 ▼            ▼
Threshold?   Login
 │
 ▼
Lock temporarily
```

Threshold and duration are configurable.

---

# 34. Concurrency Architecture

Concurrent updates may occur on:

```text
user_vocabulary_progress
streaks
```

Use:

> **Optimistic Locking**

with:

```text
version BIGINT NOT NULL DEFAULT 0
```

JPA:

```java
@Version
private Long version;
```

Conceptual update:

```sql
UPDATE user_vocabulary_progress
SET interval_days = ?,
    version = version + 1
WHERE id = ?
  AND version = ?;
```

If the version no longer matches:

```text
OptimisticLockException
```

The service layer must decide whether to:

```text
retry
reload
or reject the request
```

according to operation type.

---

# 35. Transaction Boundaries

## Learning Attempt

The following should be handled consistently:

```text
Record Session Attempt
+
Update User Vocabulary Progress
+
Update Learning Session
```

---

## Quiz Submission

```text
Save Quiz Answers
+
Calculate Score
+
Update Learning Analytics
```

---

## Gamification

XP/Streak updates should be coordinated with the triggering event where practical.

---

# 36. Database Architecture

Primary database:

```text
PostgreSQL
```

Persistence:

```text
Spring Data JPA
Hibernate
```

Migration:

```text
Flyway
```

Flow:

```text
Spring Boot
    ↓
Spring Data JPA
    ↓
Hibernate
    ↓
PostgreSQL
```

Database Schema:

```text
English_AI_Coach_Database_Schema_v1.6.md
```

---

# 37. Database State Separation

```text
Current State
    ↓
user_vocabulary_progress

Historical Events
    ↓
session_attempts
quiz_attempt_answers
learning_sessions

Personalization Output
    ↓
daily_plans

Reusable AI Content
    ↓
ai_generated_content

AI Request Log
    ↓
ai_requests

AI Usage Aggregation
    ↓
ai_usage_daily
```

---

# 38. Object Storage

Audio and images are not stored directly as PostgreSQL binary data.

```text
TTS / Upload
      │
      ▼
Object Storage
      │
      ├── audio
      └── images
      │
      ▼
URL
      │
      ▼
PostgreSQL
```

Development:

```text
MinIO
```

Production can use S3-compatible storage.

---

# 39. Redis

Redis is optional for MVP.

Potential uses:

```text
Redis
├── Cache
├── Rate limiting
├── Brute-force counters
├── Frequently accessed vocabulary
├── Daily plan cache
└── Distributed locks where necessary
```

Important:

> Redis is not the system of record.

PostgreSQL remains the source of truth.

---

# 40. Background Jobs

Tasks suitable for asynchronous/background execution:

```text
AI reusable-content generation
AI usage aggregation
Notification sending
Daily plan pre-generation
Expired refresh-token cleanup
```

MVP can use Spring scheduling.

At larger scale:

```text
Queue / Message Broker
```

can be introduced.

---

# 41. API Architecture

Public API:

```text
/api/v1/...
```

Examples:

```text
/api/v1/auth/...
/api/v1/users/...
/api/v1/vocabulary/...
/api/v1/learning/...
/api/v1/quizzes/...
/api/v1/progress/...
/api/v1/gamification/...
/api/v1/notifications/...
/api/v1/admin/...
```

The client requests business outcomes, not internal algorithm details.

Good:

```text
GET /api/v1/learning/today
```

The backend may internally execute:

```text
SRS
+
Weak Word Detection
+
Forgetting Risk
+
Recommendation
+
Difficulty Adjustment
```

The API remains stable even when the implementation changes.

---

# 42. API ↔ Personalization Boundary

```text
Mobile
   │
   │ GET /api/v1/learning/today
   ▼
Learning Controller
   │
   ▼
Learning Service
   │
   ▼
Personalization Service
   │
   ├── SRS
   ├── Weakness
   ├── Forgetting
   ├── Recommendation
   └── Difficulty
   │
   ▼
Daily Plan
   │
   ▼
Response DTO
```

Mobile does not need to know the internal personalization algorithm.

---

# 43. API ↔ AI Boundary

## Reusable content

```text
Admin/API
   ↓
AiContentService
   ↓
BudgetGuard
   ↓
LlmClient
   ↓
Provider
   ↓
Validation
   ↓
Review
   ↓
Storage
```

## Personalized content

```text
Mobile
   ↓
Learning API
   ↓
Personalization
   ↓
AiContentService
   ↓
BudgetGuard
   ↓
LlmClient
   ↓
Validation
   ↓
User
```

---

# 44. Error Handling

All APIs should use a consistent error structure.

Conceptual response:

```json
{
  "timestamp": "2026-08-28T10:00:00Z",
  "status": 400,
  "code": "VALIDATION_ERROR",
  "message": "Invalid request",
  "path": "/api/v1/..."
}
```

Example error codes:

```text
AUTH_INVALID_CREDENTIALS
AUTH_ACCOUNT_LOCKED
AUTH_REFRESH_TOKEN_INVALID
VOCABULARY_NOT_FOUND
QUIZ_NOT_FOUND
AI_BUDGET_EXCEEDED
```

---

# 45. Security Architecture

Security layers:

```text
HTTPS
  ↓
JWT Authentication
  ↓
Role Authorization
  ↓
Input Validation
  ↓
Service Authorization
  ↓
Database Constraints
  ↓
Audit Logging
```

Never store or log:

```text
password
password_hash
access token
refresh token plaintext
AI API key
OAuth client secret
```

---

# 46. Secrets Management

Do not hard-code secrets.

Configuration:

```text
DB_URL
DB_USERNAME
DB_PASSWORD

JWT_SECRET

AI_API_KEY

GOOGLE_CLIENT_ID
GOOGLE_CLIENT_SECRET
```

Use environment variables or a secrets manager.

---

# 47. Admin Architecture

```text
                     ADMIN WEB
                         │
                         ▼
                    Admin API
                         │
                  JWT + ROLE_ADMIN
                         │
                         ▼
                   Admin Service
                         │
          ┌──────────────┼──────────────┐
          ▼              ▼              ▼
      Users          Content           AI
          │              │              │
          └──────────────┼──────────────┘
                         ▼
                     PostgreSQL
```

Routes:

```text
/api/v1/admin/users
/api/v1/admin/vocabulary
/api/v1/admin/topics
/api/v1/admin/quizzes
/api/v1/admin/ai-content
/api/v1/admin/statistics/ai-usage
/api/v1/admin/audit-logs
```

---

# 48. Production Deployment — MVP

```text
                         Internet
                             │
                             ▼
                           Nginx
                             │
                    ┌────────┴────────┐
                    ▼                 ▼
              Spring Boot API     Admin Web
                    │
          ┌─────────┼───────────┐
          ▼         ▼           ▼
     PostgreSQL   Redis       MinIO
                                │
                                ▼
                           Audio/Image

Spring Boot
     │
     ▼
  LLM API
```

For a small MVP deployment, several components may run on one server.

---

# 49. Future Scalable Deployment

```text
                         Internet
                            │
                            ▼
                     Load Balancer
                            │
                ┌───────────┼───────────┐
                ▼           ▼           ▼
             API #1       API #2      API #N
                │           │           │
                └───────────┼───────────┘
                            │
                      Redis / Cache
                            │
                            ▼
                     PostgreSQL
                            │
                 ┌──────────┴──────────┐
                 ▼                     ▼
          Object Storage          AI Services
                                       │
                              ┌────────┴────────┐
                              ▼                 ▼
                             LLM               ML
```

---

# 50. Reliability & Graceful Degradation

Core learning should continue when AI services are unavailable.

## LLM unavailable

```text
LLM unavailable
      ↓
Use approved/cached content
      ↓
Continue core learning
```

## Redis unavailable

Redis should not cause data loss because PostgreSQL is the source of truth.

## Object Storage unavailable

Vocabulary metadata can still be returned; media playback should fail gracefully.

---

# 51. Core Learning vs AI Enhancement

The system must distinguish:

```text
CORE LEARNING
├── Flashcard
├── Quiz
├── SRS
├── Progress
├── Daily Plan
├── Gamification
└── Notification

AI ENHANCEMENT
├── AI Example
├── AI Explanation
├── AI Story
├── AI Mnemonic
└── Personalized Exercise
```

Core learning must not depend entirely on real-time LLM availability.

---

# 52. Observability

The system should eventually collect:

```text
Application logs
API logs
Authentication logs
AI request logs
Database metrics
Performance metrics
Error metrics
```

Important metrics:

```text
API response time
HTTP error rate
Login failures
AI request count
AI token usage
AI estimated cost
AI blocked requests
SRS processing time
Database query performance
```

---

# 53. Request Correlation

Use a request/correlation ID.

```text
Client
  │
  │ X-Request-Id
  ▼
Spring Boot
  │
  ├── Controller
  ├── Service
  ├── Repository
  └── AI Service
```

This simplifies debugging across layers.

---

# 54. Future Machine Learning Architecture

ML should initially remain separate from synchronous learning requests.

```text
Production Database
       │
       ▼
Learning Data Extraction
       │
       ▼
Feature Engineering
       │
       ▼
Training Dataset
       │
       ▼
ML Training
       │
       ▼
Model Artifact
       │
       ▼
ML Inference
       │
       ▼
Personalization Engine
```

Initial ML target:

```text
Forgetting Prediction
```

Future:

```text
Recommendation
Difficulty Prediction
Notification Timing
Progress Prediction
```

---

# 55. ML Feedback Loop

```text
User
 ↓
Learning
 ↓
SESSION_ATTEMPTS
 ↓
Feature Engineering
 ↓
ML Model
 ↓
Prediction
 ↓
Personalization
 ↓
Recommendation
 ↓
User
```

The database intentionally keeps historical events required for this loop.

---

# 56. Future Learning Modules

Current:

```text
Learning
└── Vocabulary
```

Future:

```text
Learning
├── Vocabulary
├── Speaking
├── Writing
├── Listening
└── Reading
```

Each module can produce:

```text
Learning Event
     ↓
Learning Analytics
     ↓
User Learning Profile
     ↓
Personalization Engine
```

---

# 57. Future Unified Learning Profile

```text
                USER LEARNING PROFILE
                         │
        ┌────────────────┼────────────────┐
        ▼                ▼                ▼
   Vocabulary         Speaking          Writing
        │                │                │
        ▼                ▼                ▼
      Skills           Skills           Skills
        │                │                │
        └────────────────┼────────────────┘
                         ▼
                  AI Personalization
                         │
                         ▼
                Overall Learning Path
```

Vocabulary remains the core foundation in V1.

---

# 58. Concurrency — Detailed Policy

Optimistic locking applies to high-contention mutable state:

```text
user_vocabulary_progress
streaks
```

Database:

```text
version BIGINT NOT NULL DEFAULT 0
```

JPA:

```java
@Version
private Long version;
```

Recommended handling:

```text
Load state
   ↓
Modify
   ↓
Save with expected version
   ↓
Conflict?
 ┌─┴──────┐
 NO      YES
 │         │
 ▼         ▼
Commit   Reload / Retry / Reject
```

The service must avoid silently overwriting a newer state.

---

# 59. Future Offline-first Support

Full offline-first is not an MVP requirement.

However, architecture allows future support:

```text
Mobile Local Storage
        ↓
Offline Learning
        ↓
Sync Queue
        ↓
Backend
        ↓
Conflict Resolution
```

UUIDs and optimistic locking help support synchronization.

---

# 60. Architecture Decisions

## AD-01

Use:

```text
Modular Monolith
```

for MVP.

## AD-02

Use:

```text
Spring Boot
```

as backend.

## AD-03

Use:

```text
PostgreSQL
```

as system of record.

## AD-04

Use:

```text
Spring Data JPA + Hibernate
```

for persistence.

## AD-05

Use:

```text
Flyway
```

for migrations.

## AD-06

Expose:

```text
REST + JSON
/api/v1/
```

## AD-07

Use:

```text
JWT Access Token
+
Refresh Token
```

## AD-08

Use configurable brute-force protection.

## AD-09

Use optimistic locking for:

```text
user_vocabulary_progress
streaks
```

## AD-10

Keep Personalization independent from LLM.

## AD-11

Use Rule/Heuristic/SRS algorithms for V1 personalization.

## AD-12

Collect learning history for future ML.

## AD-13

Use LLM provider abstraction.

## AD-14

Reusable AI content requires:

```text
Automated Validation
+
Admin Review
```

## AD-15

Personalized AI content requires:

```text
Automated Validation / Safety Filter
```

and does not require per-result manual Admin Review.

## AD-16

Use AI content caching.

## AD-17

Use AI budget/request/token/cost guard.

## AD-18

Use Object Storage for media.

## AD-19

Redis is optional for MVP.

## AD-20

Keep core learning functional when AI is unavailable.

---

# 61. Architecture ↔ Database Mapping

| Architecture Component | Main Database Tables |
|---|---|
| Authentication | users, refresh_tokens |
| Brute-force protection | users |
| User | users, user_profiles |
| Onboarding | goals, user_goals, cefr_levels, user_level_assessments |
| Vocabulary | topics, vocabulary, vocabulary_topics, vocabulary_examples |
| Learning | learning_sessions, session_attempts |
| SRS | user_vocabulary_progress |
| Personalization | user_vocabulary_progress, session_attempts, daily_plans |
| Quiz | quizzes, quiz_questions, quiz_attempts, quiz_attempt_answers |
| Gamification | streaks, xp_logs, badges, user_badges |
| Notification | notifications |
| Reusable AI Content | ai_generated_content |
| AI Request Tracking | ai_requests |
| AI Usage | ai_usage_daily |
| Admin | admin_audit_logs |

---

# 62. Architecture ↔ SRS Mapping

| SRS | Architecture |
|---|---|
| Authentication | Auth + Spring Security |
| Access/Refresh Token | Auth |
| Brute-force protection | Auth |
| Profile | User |
| Onboarding | Onboarding |
| Vocabulary | Vocabulary |
| SRS | Personalization |
| Weak Word Detection | Personalization |
| Forgetting Risk | Personalization |
| Recommendation | Personalization |
| Difficulty Adjustment | Personalization |
| Daily Plan | Personalization |
| Quiz | Quiz |
| Progress | Progress + Analytics |
| Gamification | Gamification |
| Notification | Notification |
| AI Content | AI |
| AI Budget | AI Budget Guard |
| AI Review | AI Content Workflow |
| Admin | Admin |
| Future ML | ML Pipeline |
| Future Speaking/Writing | Learning Extensions |

---

# 63. Architecture ↔ API Principles

API must expose business capabilities.

Examples:

```text
GET  /api/v1/learning/today
POST /api/v1/learning/sessions
POST /api/v1/learning/attempts

GET  /api/v1/progress
GET  /api/v1/vocabulary
POST /api/v1/quizzes/{id}/attempts
```

Avoid exposing internal implementation such as:

```text
POST /api/v1/srs/calculate
POST /api/v1/ml/predict-forgetting
POST /api/v1/ai/generate-daily-plan
```

unless there is a real client-facing use case.

Internal engines should stay behind domain services.

---

# 64. Recommended Development Order

```text
1. SRS v1.2
        ↓
2. Database Schema v1.6
        ↓
3. System Architecture v1.3
        ↓
4. API Specification v1.4
        ↓
5. Review API ↔ Database ↔ Architecture
        ↓
6. Spring Boot Foundation
        ↓
7. Flyway Migration
        ↓
8. JPA Entities
        ↓
9. Authentication
        ↓
10. Vocabulary
        ↓
11. Learning
        ↓
12. SRS / Personalization
        ↓
13. Quiz
        ↓
14. Progress
        ↓
15. Gamification
        ↓
16. Notification
        ↓
17. Admin
        ↓
18. AI Content
        ↓
19. ML
        ↓
20. Flutter
        ↓
21. Speaking / Writing / Listening / Reading
```

---

# 65. Architecture Completion Checklist

```text
[ ] System context defined
[ ] Client boundaries defined
[ ] Backend modules defined
[ ] Layer responsibilities defined
[ ] Database boundary defined
[ ] AI boundary defined
[ ] Personalization boundary defined
[ ] Reusable AI workflow defined
[ ] Personalized AI workflow defined
[ ] AI validation defined
[ ] AI budget guard defined
[ ] Authentication defined
[ ] Refresh Token defined
[ ] Brute-force protection defined
[ ] Concurrency strategy defined
[ ] Optimistic Locking reflected in database
[ ] Notification architecture defined
[ ] Storage architecture defined
[ ] Redis role defined
[ ] Background jobs defined
[ ] Security architecture defined
[ ] Deployment architecture defined
[ ] Observability defined
[ ] Future ML architecture defined
[ ] Future learning-module architecture defined
[ ] SRS mapping defined
[ ] Database mapping defined
[ ] API principles defined
```

---

# 66. Baseline Summary

```text
                    ┌───────────────────────────┐
                    │ Android Java / Flutter    │
                    └────────────┬──────────────┘
                                 │
                            HTTPS REST
                                 │
                                 ▼
                  ┌─────────────────────────────┐
                  │       Spring Boot           │
                  │      Modular Monolith       │
                  │                             │
                  │ Auth                        │
                  │ User                        │
                  │ Onboarding                  │
                  │ Vocabulary                  │
                  │ Learning                    │
                  │ Personalization ⭐          │
                  │ Quiz                        │
                  │ Progress                    │
                  │ Gamification                │
                  │ Notification                │
                  │ AI                          │
                  │ Admin                       │
                  └──────────────┬──────────────┘
                                 │
        ┌────────────────────────┼────────────────────────┐
        │                        │                        │
        ▼                        ▼                        ▼
   PostgreSQL                  Redis                Object Storage
        │
        │
        ▼
 Personalization
     Engine
        │
   ┌────┼─────┐
   ▼    ▼     ▼
  SRS  Rules  ML Future
   │           │
   └──────┬────┘
          ▼
      Daily Plan

AI Content:
Reusable → LLM → Validation → Admin Review → Cache → Users
Personalized → Personalization → LLM → Validation/Safety → User
```

---

# 67. Final Architecture Baseline

```text
Mobile V1
    Android Java

Mobile V2
    Flutter

Backend
    Java Spring Boot

Architecture
    Modular Monolith
    Layered Architecture

Database
    PostgreSQL

ORM
    Spring Data JPA / Hibernate

Migration
    Flyway

API
    REST / JSON
    /api/v1/

Authentication
    Spring Security
    JWT Access Token
    Refresh Token

Concurrency
    Optimistic Locking
    @Version
    user_vocabulary_progress
    streaks

Cache
    Redis (optional MVP)

Media Storage
    Object Storage / MinIO / S3-compatible

AI Content
    LLM via provider abstraction

Reusable AI Content
    Validation
    Admin Review
    Cache

Personalized AI Content
    Automated Validation
    Safety Filter
    No per-result manual review

Personalization V1
    Rule / Heuristic / SRS

ML V2+
    Separate ML pipeline/service

AI Operations
    Budget Guard
    Request Logging
    Usage Aggregation

Admin
    Admin Web + Admin API
```

---

# 68. Next Step

After this Architecture Baseline:

```text
SRS v1.2
      ↓
Database Schema v1.6
      ↓
System Architecture v1.3
      ↓
👉 API Specification v1.4
      ↓
API ↔ Database Review
      ↓
Spring Boot Project
```

The next document should define the complete API contract:

```text
HTTP Method
URL
Authentication
Authorization
Path Parameters
Query Parameters
Request Body
Validation
Business Rules
Response Body
Error Response
Pagination
Filtering
Transaction
Database access
Personalization behavior
AI behavior
```


---

# 69. Idempotency Architecture

## 69.1. Responsibility

Idempotency is implemented as an application-layer concern backed by PostgreSQL.

```text
Client
  ↓
API Controller
  ↓
IdempotencyService
  ↓
Business Use Case
  ↓
PostgreSQL
```

The idempotency key is not stored in `session_attempts`.

---

## 69.2. Storage

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

---

## 69.3. Applicable V1 mutation endpoints

Mandatory:

```text
POST /api/v1/learning/attempts
POST /api/v1/quiz-attempts/{attemptId}/answers
POST /api/v1/quiz-attempts/{attemptId}/complete
```

The exact endpoint identifier is stored with the key.

`POST /auth/refresh` is **not** handled through this learning-event idempotency mechanism; it uses refresh-token expiry/revocation semantics. Refresh-token rotation remains a future security enhancement unless explicitly implemented.

---

## 69.4. Logical operation rule

`eventId` represents one logical client action, not one HTTP request.

Example:

```text
User selects "Hard"
→ eventId = A

HTTP request times out
→ retry with eventId = A
```

The retry must keep the same event ID.

---

## 69.5. Request hash

Backend computes:

```text
SHA-256(canonical request payload)
```

before execution.

If:

```text
eventId = A
endpoint = /learning/attempts
requestHash = X
```

already exists:

```text
same endpoint + same hash
→ return stored response
```

If:

```text
same eventId
+
different endpoint
or
different requestHash
```

return:

```text
HTTP 409
code = IDEMPOTENCY_KEY_REUSE
```

---

## 69.6. Atomicity

The first successful logical operation and its idempotency record must be committed atomically.

Recommended pattern:

```text
BEGIN
 ↓
Insert idempotency key
 ↓
If conflict:
   compare endpoint/hash
   return stored response or 409
 ↓
Execute business transaction
 ↓
Store response snapshot/status
 ↓
COMMIT
```

The learning mutation itself must remain in its business transaction.

---

## 69.7. Duplicate response

For duplicate same-request retry:

```text
HTTP status
+
response body
```

must match the previously completed logical operation as closely as practical.

This prevents:

```text
double SRS update
double XP
double streak effect
duplicate quiz answer
```

---

## 69.8. Cleanup

Scheduled cleanup:

```text
delete idempotency_keys older than
IDEMPOTENCY_KEY_RETENTION_DAYS
```

Baseline:

```text
30 days
```

Cleanup must not affect currently active requests.

---

## 69.9. Failure behavior

If the first transaction fails and rolls back:

```text
business changes rollback
idempotency record must not remain as a successful replayable result
```

A retry with the same event ID may then execute again.

---

## 69.10. Architecture diagram

```text
Android / Admin
      │
      │ eventId
      ▼
Idempotency Layer
      │
 ┌────┴───────────────┐
 │ existing?          │
 │                    │
YES                  NO
 │                    │
 ├─ same hash ───────► cached response
 │
 └─ different ───────► 409
                      │
                      ▼
                Business Service
                      │
                      ▼
                 Transaction
                      │
          ┌───────────┼────────────┐
          ▼           ▼            ▼
   business data   idempotency   audit/derived
```

---

# Reconciled V1 Architecture Decisions

This section is normative and supersedes conflicting illustrative text earlier in this document.

## Assessment boundary

`AssessmentService` owns deterministic `assessment-block-v1`, persists `user_level_assessments` and `assessment_items`, and never derives assessment state on the client. GET next-question reads a persisted unanswered item; question creation occurs inside mutation transactions.

## Daily Plan boundary

`DailyPlanService` creates one persisted snapshot per `(user, local date)` and persists ordered `daily_plan_items`. Goals/profile changes do not rerank the current day's plan. Personalization services provide deterministic ranking inputs; the client never recomputes them.

## Notification boundary

Android registers FCM tokens through `PUT /api/v1/devices/{installationId}/push-token`; preferences use `/api/v1/notification-preferences`. `NotificationService` schedules Daily Plan 07:00 local, review at preferred study time (default 19:00), and streak reminder 21:00 local, respecting user timezone/preferences and type/day dedupe.

## AI boundary

V1 client-facing AI generation is synchronous: Admin reusable generation returns 201/PENDING_REVIEW; personalized exercise returns 200. Background workers may still perform notification sending, usage aggregation, Daily Plan pre-generation, and idempotency cleanup. No client-facing job lifecycle exists in V1.

## Idempotency boundary

Only body `eventId` is used for logical mutation idempotency. Claim uses PostgreSQL `INSERT ... ON CONFLICT DO NOTHING`; business mutation and stored response commit atomically.

Canonical routes include:

```text
GET  /api/v1/admin/statistics/ai-usage
POST /api/v1/learning/personalized-exercise
```
