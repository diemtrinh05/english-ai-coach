# API Specification v1.4 — English AI Coach

**Project:** English AI Coach  
**Version:** 1.4  
**Status:** APPROVED BASELINE  
**Date:** 2026-08-31  

**Related documents**
- `English_AI_Coach_SRS_v1.2.md`
- `English_AI_Coach_Database_Schema_v1.6.md`
- `English_AI_Coach_System_Architecture_v1.3.md`
- `English_AI_Coach_AI_Personalization_Specification_v1.3.md`

---

# 1. Purpose

REST API contract for:

```text
Android Java V1
Flutter V2
Admin Web
```

Backend:

```text
Java Spring Boot
```

Database:

```text
PostgreSQL
```

The API exposes business capabilities and does not expose internal implementations of:

```text
SRS
Weak Word Detection
Forgetting Risk
Recommendation
ML
LLM
```

---

# 2. API Conventions

## 2.1. Base URL

```text
/api/v1
```

## 2.2. Protocol

```text
HTTPS
REST
JSON
UTF-8
```

## 2.3. Authentication

Authenticated APIs use:

```http
Authorization: Bearer <access_token>
```

Access Token:

```text
JWT
Short-lived
Target: 15–30 minutes
```

Refresh Token is handled by:

```text
POST /api/v1/auth/refresh
```

## 2.4. Authorization

Roles:

```text
USER
ADMIN
```

## 2.5. Date/Time

Timestamp:

```text
ISO-8601
```

Example:

```text
2026-08-28T10:30:00Z
```

Date:

```text
2026-08-28
```

---

# 3. Collection Response Convention

API must use one explicit rule for collection responses.

## 3.1. Small non-paginated reference collection

Return a plain JSON array:

```json
[
  {
    "id": "uuid",
    "name": "IELTS"
  },
  {
    "id": "uuid",
    "name": "TRAVEL"
  }
]
```

Typical endpoints:

```text
GET /api/v1/goals
GET /api/v1/cefr-levels
```

Do not wrap these in `content`.

## 3.2. Paginated collection

Use:

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

Typical endpoints:

```text
GET /api/v1/topics
GET /api/v1/vocabulary
GET /api/v1/quizzes
GET /api/v1/progress/weak-words
GET /api/v1/gamification/leaderboard
GET /api/v1/gamification/xp/history
GET /api/v1/notifications
GET /api/v1/admin/users
GET /api/v1/admin/vocabulary
GET /api/v1/admin/ai-content
GET /api/v1/admin/audit-logs
```

Defaults:

```text
page = 0
size = 20
1 <= size <= 100
```

## 3.3. Domain-specific collection/result

A domain response may use its own object when metadata is part of the business result.

Example:

```text
GET /api/v1/learning/reviews
```

Response:

```json
{
  "items": [
    {
      "vocabularyId": "uuid",
      "word": "abandon",
      "status": "REVIEWING",
      "nextReviewAt": "2026-08-28T09:00:00Z",
      "forgettingRisk": "HIGH"
    }
  ],
  "count": 1
}
```

`items/count` is a domain-specific response, not a substitute for pagination.

---

# 4. Success Responses

Responses do not need a generic `data` wrapper.

Example:

```json
{
  "id": "uuid",
  "word": "abandon"
}
```

---

# 5. Standard Error Response

```json
{
  "timestamp": "2026-08-28T10:30:00Z",
  "status": 400,
  "code": "VALIDATION_ERROR",
  "message": "Invalid request",
  "path": "/api/v1/learning/attempts",
  "details": []
}
```

Common codes:

```text
VALIDATION_ERROR
UNAUTHORIZED
FORBIDDEN
NOT_FOUND
CONFLICT
RATE_LIMITED
INTERNAL_ERROR
CONCURRENT_UPDATE

AUTH_INVALID_CREDENTIALS
AUTH_ACCOUNT_LOCKED
AUTH_REFRESH_TOKEN_INVALID
AUTH_REFRESH_TOKEN_EXPIRED

VOCABULARY_NOT_FOUND
TOPIC_NOT_FOUND
QUIZ_NOT_FOUND
QUIZ_ATTEMPT_NOT_FOUND
LEARNING_SESSION_NOT_FOUND

AI_BUDGET_EXCEEDED
AI_GENERATION_FAILED
```

---

# 6. HTTP Status Conventions

| Status | Meaning |
|---|---|
| 200 | Success |
| 201 | Created |
| 202 | Reserved for future client-facing async workflows; unused by V1 AI endpoints |
| 204 | Success with no body |
| 400 | Invalid request |
| 401 | Authentication required/invalid |
| 403 | Insufficient permission |
| 404 | Resource not found |
| 409 | Conflict/concurrent update/idempotency |
| 422 | Semantic validation failure if adopted |
| 423 | Account/resource temporarily locked |
| 429 | Rate limit or AI quota exceeded |
| 500 | Internal error |
| 503 | Temporary service unavailable |

---

# 7. Authentication API

## 7.1. Register

```http
POST /api/v1/auth/register
```

Auth: Public

Request:

```json
{
  "email": "user@example.com",
  "password": "StrongPassword123!",
  "fullName": "Nguyen Van A"
}
```

Validation:

```text
email: required, valid format
password: required, minimum configured length
fullName: required
```

Response 201:

```json
{
  "user": {
    "id": "uuid",
    "email": "user@example.com",
    "fullName": "Nguyen Van A",
    "role": "USER"
  },
  "accessToken": "jwt",
  "expiresIn": 1800,
  "tokenType": "Bearer"
}
```

---

## 7.2. Login

```http
POST /api/v1/auth/login
```

Auth: Public

Request:

```json
{
  "email": "user@example.com",
  "password": "StrongPassword123!"
}
```

Response:

```json
{
  "accessToken": "jwt",
  "expiresIn": 1800,
  "tokenType": "Bearer",
  "user": {
    "id": "uuid",
    "email": "user@example.com",
    "fullName": "Nguyen Van A",
    "role": "USER",
    "status": "ACTIVE"
  }
}
```

Brute-force baseline:

```text
5 failed attempts
→ temporary lock 5 minutes
```

When locked:

```text
HTTP 423
code = AUTH_ACCOUNT_LOCKED
```

Do not reveal whether an email exists.

---

## 7.3. Refresh Access Token

```http
POST /api/v1/auth/refresh
```

Auth: Refresh Token

Request:

```json
{
  "refreshToken": "refresh-token"
}
```

Response:

```json
{
  "accessToken": "new-jwt",
  "expiresIn": 1800,
  "tokenType": "Bearer"
}
```

Rules:

```text
invalid → reject
expired → reject
revoked → reject
valid → issue new Access Token
```

### Refresh Token Rotation

V1:

```text
rotation not required
```

Refresh Token still has:

```text
expiry
revocation
```

Future improvement:

```text
Refresh
  ↓
Revoke old Refresh Token
  ↓
Issue new Refresh Token
+
new Access Token
```

Future reuse detection may revoke the affected token family/session.

---

## 7.4. Logout

```http
POST /api/v1/auth/logout
```

Auth: Authenticated User

Request:

```json
{
  "refreshToken": "refresh-token"
}
```

Response:

```http
204 No Content
```

Refresh session/token is revoked.

---

## 7.5. Google Login

```http
POST /api/v1/auth/google
```

Auth: Public

Request:

```json
{
  "idToken": "google-id-token"
}
```

Backend validates Google ID Token before login/create account.

---

# 8. User API

## 8.1. Current User

```http
GET /api/v1/users/me
```

Response:

```json
{
  "id": "uuid",
  "email": "user@example.com",
  "fullName": "Nguyen Van A",
  "role": "USER",
  "status": "ACTIVE"
}
```

Never expose:

```text
password_hash
refresh token
```

## 8.2. Current Profile

```http
GET /api/v1/users/me/profile
```

Response:

```json
{
  "fullName": "Nguyen Van A",
  "avatarUrl": "...",
  "currentCefrLevel": {
    "code": "A2",
    "name": "A2"
  },
  "dailyLearningMinutes": 20,
  "timezone": "Asia/Ho_Chi_Minh"
}
```

## 8.3. Update Profile

```http
PUT /api/v1/users/me/profile
```

Request:

```json
{
  "fullName": "Nguyen Van A",
  "avatarUrl": "...",
  "dailyLearningMinutes": 30,
  "timezone": "Asia/Ho_Chi_Minh"
}
```

Response: updated profile.

---

# 9. Goal API

## 9.1. List Goals

```http
GET /api/v1/goals
```

Response: plain JSON array.

```json
[
  {
    "id": "uuid",
    "name": "IELTS",
    "description": "..."
  },
  {
    "id": "uuid",
    "name": "TRAVEL",
    "description": "..."
  }
]
```

## 9.2. Get User Goals

```http
GET /api/v1/users/me/goals
```

Response:

```json
{
  "goals": [
    {
      "id": "uuid",
      "name": "IELTS",
      "isPrimary": true
    },
    {
      "id": "uuid",
      "name": "COMMUNICATION",
      "isPrimary": false
    }
  ]
}
```

## 9.3. Set User Goals

```http
PUT /api/v1/users/me/goals
```

Request:

```json
{
  "goalIds": [
    "uuid-1",
    "uuid-2"
  ],
  "primaryGoalId": "uuid-1"
}
```

Rules:

```text
at least one goal
at most one primary goal
all selected goals must be active
primaryGoalId must belong to goalIds
```

---

# 10. CEFR API

## 10.1. List CEFR Levels

```http
GET /api/v1/cefr-levels
```

Response: plain JSON array.

```json
[
  {
    "id": "uuid",
    "code": "A1",
    "name": "A1",
    "sortOrder": 1
  },
  {
    "id": "uuid",
    "code": "A2",
    "name": "A2",
    "sortOrder": 2
  }
]
```

---

# 11. Placement Assessment API

Assessment V1 is deterministic `assessment-block-v1`; state/questions are persisted.

## 11.1. Start Assessment

```http
POST /api/v1/assessments
```

```json
{ "type": "INITIAL", "eventId": "uuid" }
```

Returns 201 with `assessmentId`, `type`, `status=IN_PROGRESS`, `currentDifficulty=A1`. If another logical event tries to start while the user already has an IN_PROGRESS assessment: `409 ASSESSMENT_ALREADY_IN_PROGRESS`.

## 11.2. Get Next Question

```http
GET /api/v1/assessments/{assessmentId}/next-question
```

Reads the already-persisted unanswered question. It does not perform a non-idempotent generation in GET. Response includes `questionId`, `questionText`, four `options`, and `cefrLevel`.

## 11.3. Submit Assessment Answer

```http
POST /api/v1/assessments/{assessmentId}/answers
```

```json
{
  "questionId": "uuid",
  "answer": "To leave",
  "responseTimeMs": 2300,
  "eventId": "uuid"
}
```

Returns `correct`, `nextDifficulty`, `questionsAnswered`, `isCompleted`. A different event attempting to answer an already answered question returns `409 ASSESSMENT_QUESTION_ALREADY_ANSWERED`. Same logical retry with same `eventId` replays. Content shortage returns `503 ASSESSMENT_CONTENT_UNAVAILABLE`.

## 11.4. Get Assessment

```http
GET /api/v1/assessments/{assessmentId}
```

Returns status plus score/counts and final CEFR when completed.

---

# 12. Topic API

## 12.1. List Topics

```http
GET /api/v1/topics
```

Query:

```text
page
size
search
parentTopicId
```

Response: paginated collection.

## 12.2. Topic Detail

```http
GET /api/v1/topics/{topicId}
```

---

# 13. Vocabulary API

## 13.1. List Vocabulary

```http
GET /api/v1/vocabulary
```

Query:

```text
page
size
search
cefr
topicId
partOfSpeech
sort
```

Response: paginated collection.

Example:

```http
GET /api/v1/vocabulary?page=0&size=20&cefr=B1&topicId=uuid&sort=word,asc
```

## 13.2. Vocabulary Detail

```http
GET /api/v1/vocabulary/{vocabularyId}
```

Response:

```json
{
  "id": "uuid",
  "word": "abandon",
  "phoneticIpa": "/əˈbændən/",
  "meaningVi": "từ bỏ",
  "meaningEn": "to leave completely",
  "partOfSpeech": "verb",
  "cefr": "B1",
  "topics": [
    {
      "id": "uuid",
      "name": "Daily Life"
    }
  ],
  "audioUrl": "...",
  "imageUrl": "...",
  "examples": [
    {
      "text": "He abandoned the project.",
      "translation": "Anh ấy đã từ bỏ dự án."
    }
  ]
}
```

## 13.3. Vocabulary Examples

```http
GET /api/v1/vocabulary/{vocabularyId}/examples
```

If small, return plain array:

```json
[
  {
    "text": "He abandoned the project.",
    "translation": "Anh ấy đã từ bỏ dự án."
  }
]
```

---

# 14. Learning Session API

## 14.1. Start Session

```http
POST /api/v1/learning/sessions
```

```json
{ "sessionType": "MIXED", "eventId": "uuid" }
```

Allowed: `NEW_WORDS`, `REVIEW`, `QUIZ`, `MIXED`. Returns 201.

## 14.2. Get Session

```http
GET /api/v1/learning/sessions/{sessionId}
```

## 14.3. Complete Session

```http
POST /api/v1/learning/sessions/{sessionId}/complete
```

```json
{ "eventId": "uuid" }
```

First valid completion of a session with at least one accepted attempt awards `SESSION_COMPLETED` XP once. Retry replays through idempotency.

---

# 15. Personalized Daily Plan

Daily Plan is one persisted snapshot per user local date. GET calls do not rerank an existing plan.

## 15.1. Get Today's Plan

```http
GET /api/v1/learning/today
```

Returns `planId`, `date`, `estimatedMinutes`, `status`, `newWordsTarget`, `reviewWordsTarget`, `quizTarget`.

## 15.2. Get Today's Plan Items

```http
GET /api/v1/learning/today/items
```

Each item returns `itemId`, `itemType`, nullable `vocabularyId`, `priority`, optional `reasonCode`, `targetCount`, `completedCount`, `status`. `REVIEW/NEW` have a vocabulary and targetCount=1; aggregate `QUIZ` has `vocabularyId=null`.

## 15.3. Complete Daily Plan

```http
POST /api/v1/learning/today/complete
```

```json
{ "eventId": "uuid" }
```

All targets reached → `COMPLETED`, +50 XP once. Otherwise → `PARTIAL`, no Daily Plan completion XP. Both are terminal in V1.

---

# 16. Review Queue

## 16.1. Get Due Reviews

```http
GET /api/v1/learning/reviews
```

Query:

```text
limit
topicId
```

Response: domain-specific.

```json
{
  "items": [
    {
      "vocabularyId": "uuid",
      "word": "abandon",
      "status": "REVIEWING",
      "nextReviewAt": "2026-08-28T09:00:00Z",
      "forgettingRisk": "HIGH"
    }
  ],
  "count": 1
}
```

If the queue later needs pagination, convert it to the standard paginated format instead of inventing another pagination shape.

---

# 17. Learning Attempt

## 17.1. Submit Vocabulary Attempt

```http
POST /api/v1/learning/attempts
```

Auth: USER

### Request

```json
{
  "sessionId": "uuid",
  "vocabularyId": "uuid",
  "attemptType": "WORD_RECALL",
  "responseTimeMs": 2500,
  "answerQuality": 4,
  "eventId": "client-generated-uuid"
}
```

### Source of Truth

Client **must not send `isCorrect`**.

Backend derives:

```text
isCorrect = answerQuality >= 3
```

Mapping:

```text
0–2 → incorrect
3–5 → correct
```

The flow is:

```text
answerQuality
    ↓
derive isCorrect
    ↓
session_attempts.is_correct
    ↓
Extended SM-2
```

### Validation

```text
sessionId: required
vocabularyId: required
attemptType: required
responseTimeMs >= 0
answerQuality ∈ [0,5]
eventId: idempotency key
```

### Response

```json
{
  "attemptId": "uuid",
  "isCorrect": true,
  "answerQuality": 4,
  "srs": {
    "oldEaseFactor": 2.5,
    "newEaseFactor": 2.5,
    "oldIntervalDays": 6,
    "newIntervalDays": 17,
    "oldRepetitions": 2,
    "newRepetitions": 3,
    "nextReviewAt": "2026-09-14T10:30:00Z",
    "algorithmVersion": "sm2-ext-v1"
  },
  "progress": {
    "status": "REVIEWING",
    "correctCount": 5,
    "incorrectCount": 1
  }
}
```

### Transaction

Conceptually:

```text
BEGIN
  derive isCorrect
  insert session_attempt
  update user_vocabulary_progress
  update learning_session
  update relevant gamification state
COMMIT
```

Optimistic locking must be respected.

---

# 18. Vocabulary Progress

## 18.1. Get Progress

```http
GET /api/v1/vocabulary/{vocabularyId}/progress
```

Response:

```json
{
  "vocabularyId": "uuid",
  "status": "REVIEWING",
  "easeFactor": 2.5,
  "intervalDays": 17,
  "repetitions": 3,
  "nextReviewAt": "2026-09-14T10:30:00Z",
  "correctCount": 5,
  "incorrectCount": 1
}
```

---

# 19. Progress API

## 19.1. Overview

```http
GET /api/v1/progress
```

Response:

```json
{
  "wordsLearned": 320,
  "wordsMastered": 140,
  "accuracyPercent": 84.5,
  "learningMinutes": 1260,
  "currentStreak": 12,
  "goalProgressPercent": 64.0
}
```

## 19.2. Weak Words

```http
GET /api/v1/progress/weak-words
```

Query:

```text
page
size
```

Response: paginated collection.

```json
{
  "content": [
    {
      "vocabularyId": "uuid",
      "word": "negotiate",
      "weaknessScore": 0.82,
      "reasonCodes": [
        "LOW_ACCURACY",
        "RECENT_FAILURES"
      ]
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1,
  "hasNext": false
}
```

## 19.3. Trend

```http
GET /api/v1/progress/trend
```

Query:

```text
from
to
granularity=DAY|WEEK|MONTH
```

Response:

```json
{
  "points": [
    {
      "date": "2026-08-20",
      "accuracyPercent": 80.2,
      "wordsStudied": 25,
      "learningMinutes": 21
    }
  ]
}
```

---

# 20. Quiz API

## 20.1. List Quizzes

```http
GET /api/v1/quizzes
```

Query:

```text
page
size
topicId
cefr
```

Response: paginated collection.

## 20.2. Quiz Detail

```http
GET /api/v1/quizzes/{quizId}
```

Do not expose `correctAnswer` before submission.

## 20.3. Start Quiz Attempt

```http
POST /api/v1/quizzes/{quizId}/attempts
```

Request:

```json
{ "eventId": "uuid" }
```

Response:

```json
{
  "attemptId": "uuid",
  "quizId": "uuid",
  "startedAt": "..."
}
```

## 20.4. Submit Quiz Answer

```http
POST /api/v1/quiz-attempts/{attemptId}/answers
```

Request:

```json
{
  "questionId": "uuid",
  "userAnswer": "B",
  "responseTimeMs": 3400,
  "eventId": "uuid"
}
```

Backend derives:

```text
isCorrect
```

from stored question/correct-answer data.

Client must not send `isCorrect`.

Response:

```json
{
  "answerId": "uuid",
  "isCorrect": true
}
```

## 20.5. Complete Quiz Attempt

```http
POST /api/v1/quiz-attempts/{attemptId}/complete
```

Request:

```json
{
  "eventId": "uuid"
}
```

The same `eventId` must be reused if the completion request is retried.

Response:

```json
{
  "attemptId": "uuid",
  "score": 80.0,
  "totalQuestions": 10,
  "correctAnswers": 8,
  "completedAt": "..."
}
```

## 20.6. Get Quiz Attempt

```http
GET /api/v1/quiz-attempts/{attemptId}
```

---

# 21. Gamification API

## 21.1. Streak

```http
GET /api/v1/gamification/streak
```

## 21.2. XP

```http
GET /api/v1/gamification/xp
```

## 21.3. XP History

```http
GET /api/v1/gamification/xp/history
```

Query:

```text
page
size
from
to
```

Response: paginated.

## 21.4. Badges

```http
GET /api/v1/gamification/badges
```

Response:

```json
{
  "earned": [
    {
      "id": "uuid",
      "name": "7 Day Streak",
      "earnedAt": "..."
    }
  ],
  "available": [
    {
      "id": "uuid",
      "name": "100 Words"
    }
  ]
}
```

## 21.5. Leaderboard

```http
GET /api/v1/gamification/leaderboard
```

Query:

```text
period=DAILY|WEEKLY|MONTHLY|ALL_TIME
page
size
```

Response: paginated.

---

# 22. Notification & Device API

## 22.1. List Notifications

```http
GET /api/v1/notifications
```

Returns a paginated notification collection. Read/unread tracking is not required by V1.

## 22.2. Register or Refresh Push Token

```http
PUT /api/v1/devices/{installationId}/push-token
```

```json
{ "platform": "ANDROID", "pushToken": "fcm-token" }
```

Natural resource-idempotent PUT; no `eventId`. Token is never returned by ordinary list/profile APIs.

## 22.3. Deactivate Device

```http
DELETE /api/v1/devices/{installationId}
```

Returns 204.

## 22.4. Get Notification Preferences

```http
GET /api/v1/notification-preferences
```

## 22.5. Update Notification Preferences

```http
PUT /api/v1/notification-preferences
```

```json
{
  "pushEnabled": true,
  "reviewReminderEnabled": true,
  "dailyPlanEnabled": true,
  "streakReminderEnabled": true,
  "preferredStudyTime": "19:00"
}
```

---

# 23. Personalized AI Exercise

```http
POST /api/v1/learning/personalized-exercise
```

Auth: USER. Synchronous V1 response: 200.

```json
{
  "activityType": "FILL_BLANK",
  "count": 5,
  "vocabularyIds": ["uuid-1"],
  "eventId": "uuid"
}
```

`vocabularyIds` is optional. Backend performs personalization, AI budget guard, LLM generation, schema validation and safety filtering. No per-result Admin review. Same logical retry uses same eventId and must not cause a second LLM cost. Failures use `AI_GENERATION_FAILED`; budget exhaustion uses `AI_BUDGET_EXCEEDED`.

---

# 24. Reusable AI Content

## 24.1. Admin Generate Content

```http
POST /api/v1/admin/ai-content/generate
```

Auth: ADMIN. V1 is synchronous and returns **201 Created**.

```json
{
  "contentType": "EXAMPLE",
  "vocabularyId": "uuid",
  "topicId": null,
  "eventId": "uuid"
}
```

The validated reusable content record is stored as `PENDING_REVIEW`. There is no client-facing `202/jobId` contract in V1.

---

# 25. Admin AI Content Review

## 25.1. List AI Content

```http
GET /api/v1/admin/ai-content
```

Query:

```text
page
size
status
contentScope
contentType
```

Response: paginated.

## 25.2. Get AI Content

```http
GET /api/v1/admin/ai-content/{contentId}
```

## 25.3. Approve

```http
POST /api/v1/admin/ai-content/{contentId}/approve
```

Response:

```json
{
  "id": "uuid",
  "status": "APPROVED",
  "reviewNote": null,
  "reviewedBy": "admin-user-id",
  "reviewedAt": "..."
}
```

## 25.4. Reject

```http
POST /api/v1/admin/ai-content/{contentId}/reject
```

Request:

```json
{
  "reason": "Incorrect example"
}
```

### Server-side persistence

When the request is successful, backend must:

```text
ai_generated_content.status = REJECTED
ai_generated_content.review_note = reason
ai_generated_content.reviewed_by = current_admin_id
ai_generated_content.reviewed_at = now()

admin_audit_logs.action = REJECT
admin_audit_logs.details.reason = reason
```

The `review_note` belongs to the AI content record. The audit log remains the historical record of the admin action.

### Response

```json
{
  "id": "uuid",
  "contentScope": "REUSABLE",
  "contentType": "EXAMPLE",
  "status": "REJECTED",
  "reviewNote": "Incorrect example",
  "reviewedBy": "admin-user-id",
  "reviewedAt": "..."
}
```

---

# 26. Admin User API

## 26.1. List Users

```http
GET /api/v1/admin/users
```

Query:

```text
page
size
search
status
role
```

Response: paginated.

## 26.2. Get User

```http
GET /api/v1/admin/users/{userId}
```

## 26.3. Lock User

```http
POST /api/v1/admin/users/{userId}/lock
```

## 26.4. Unlock User

```http
POST /api/v1/admin/users/{userId}/unlock
```

Every admin action must create:

```text
admin_audit_logs
```

---

# 27. Admin Vocabulary API

## 27.1. Create

```http
POST /api/v1/admin/vocabulary
```

Request:

```json
{
  "word": "abandon",
  "phoneticIpa": "/əˈbændən/",
  "meaningVi": "từ bỏ",
  "meaningEn": "to leave completely",
  "partOfSpeech": "verb",
  "cefrLevelId": "uuid",
  "topicIds": [
    "uuid"
  ]
}
```

Constraint:

```text
UNIQUE(word, part_of_speech, cefr_level_id)
```

## 27.2. Update

```http
PUT /api/v1/admin/vocabulary/{vocabularyId}
```

## 27.3. Deactivate

```http
POST /api/v1/admin/vocabulary/{vocabularyId}/deactivate
```

## 27.4. Reactivate

```http
POST /api/v1/admin/vocabulary/{vocabularyId}/activate
```

---

# 28. Admin Topic API

## 28.1. Create

```http
POST /api/v1/admin/topics
```

Request:

```json
{
  "name": "Banking",
  "description": "...",
  "parentTopicId": "uuid"
}
```

## 28.2. Update

```http
PUT /api/v1/admin/topics/{topicId}
```

## 28.3. Deactivate

```http
POST /api/v1/admin/topics/{topicId}/deactivate
```

---

# 29. Admin Quiz API

## 29.1. Create Quiz

```http
POST /api/v1/admin/quizzes
```

## 29.2. Update Quiz

```http
PUT /api/v1/admin/quizzes/{quizId}
```

## 29.3. Add Question

```http
POST /api/v1/admin/quizzes/{quizId}/questions
```

Request:

```json
{
  "vocabularyId": "uuid",
  "questionText": "...",
  "questionType": "MULTIPLE_CHOICE",
  "correctAnswer": "A",
  "options": {
    "choices": [
      "A",
      "B",
      "C",
      "D"
    ]
  },
  "sortOrder": 1
}
```

## 29.4. Update Question

```http
PUT /api/v1/admin/quiz-questions/{questionId}
```

## 29.5. Publish/Unpublish

```http
POST /api/v1/admin/quizzes/{quizId}/publish
POST /api/v1/admin/quizzes/{quizId}/unpublish
```

---

# 30. Admin Statistics API

## 30.1. Learning Statistics

```http
GET /api/v1/admin/statistics/learning
```

Query:

```text
from
to
```

Response:

```json
{
  "activeUsers": 1200,
  "totalLearningSessions": 8500,
  "averageAccuracyPercent": 82.4,
  "totalWordsStudied": 125000,
  "totalLearningMinutes": 24000
}
```

## 30.2. AI Usage Statistics

```http
GET /api/v1/admin/statistics/ai-usage
```

Query:

```text
from
to
provider
model
feature
```

Response:

```json
{
  "totalRequests": 5000,
  "totalTokens": 1200000,
  "estimatedCost": 15.42,
  "blockedRequests": 120,
  "byFeature": [
    {
      "feature": "AI_EXAMPLE",
      "requests": 2000,
      "tokens": 400000,
      "estimatedCost": 5.10
    }
  ]
}
```

---

# 31. Admin Audit API

## 31.1. List Audit Logs

```http
GET /api/v1/admin/audit-logs
```

Query:

```text
page
size
adminId
action
targetTable
from
to
```

Response: paginated.

---

# 32. API ↔ Personalization Mapping

| API | Internal capability |
|---|---|
| `GET /learning/today` | Daily Plan Generator |
| `GET /learning/reviews` | SRS Review Queue |
| `POST /learning/attempts` | SRS + Analytics |
| `GET /progress/weak-words` | Weak Word Detector |
| `GET /vocabulary` | Candidate retrieval |
| `POST /learning/personalized-exercise` | Personalization + LLM |
| Assessment APIs | Adaptive Testing |

---

# 33. API ↔ Database Mapping

| API Group | Main Tables |
|---|---|
| Auth | users, refresh_tokens |
| User | users, user_profiles |
| Goals | goals, user_goals |
| Assessment | cefr_levels, user_level_assessments |
| Topic | topics |
| Vocabulary | vocabulary, vocabulary_topics, vocabulary_examples |
| Learning | learning_sessions, session_attempts |
| SRS | user_vocabulary_progress |
| Daily Plan | daily_plans |
| Quiz | quizzes, quiz_questions, quiz_attempts, quiz_attempt_answers |
| Progress | learning + quiz data |
| Gamification | streaks, xp_logs, badges, user_badges |
| Notification | notifications |
| AI Content | ai_generated_content |
| AI Tracking | ai_requests, ai_usage_daily |
| Admin Audit | admin_audit_logs |

---

# 34. Authentication/Authorization Matrix

| API Group | Public | USER | ADMIN |
|---|---:|---:|---:|
| `/auth/*` | ✓ | ✓ | ✓ |
| `/goals` | optional | ✓ | ✓ |
| `/cefr-levels` | optional | ✓ | ✓ |
| `/vocabulary` | optional | ✓ | ✓ |
| `/topics` | optional | ✓ | ✓ |
| `/users/me/*` | | ✓ | ✓ |
| `/learning/*` | | ✓ | |
| `/progress/*` | | ✓ | |
| `/quizzes/*` | | ✓ | |
| `/gamification/*` | | ✓ | |
| `/notifications/*` | | ✓ | |
| `/admin/*` | | | ✓ |

---

# 35. Idempotency

V1 uses **only body `eventId`** for logical mutation idempotency. No idempotency HTTP header is part of the V1 contract. Retention is 30 days.

Required endpoints:

```text
POST /api/v1/assessments
POST /api/v1/assessments/{assessmentId}/answers
POST /api/v1/learning/sessions
POST /api/v1/learning/sessions/{sessionId}/complete
POST /api/v1/learning/attempts
POST /api/v1/learning/today/complete
POST /api/v1/quizzes/{quizId}/attempts
POST /api/v1/quiz-attempts/{attemptId}/answers
POST /api/v1/quiz-attempts/{attemptId}/complete
POST /api/v1/learning/personalized-exercise
POST /api/v1/admin/ai-content/generate
```

Request hash is SHA-256 of canonical `{method, routeTemplate, path, query, body}` after removing `eventId`; UUID text is normalized and JSON keys sorted. Ownership is checked separately.

```text
same eventId + same user + same logical request → replay stored status/body
same eventId + different user/route/path/query/body → 409 IDEMPOTENCY_KEY_REUSE
```

PostgreSQL claim uses `INSERT ... ON CONFLICT (event_id) DO NOTHING`; business mutation and response snapshot commit in the same transaction.

---

# 36. Concurrency

Learning attempts update:

```text
user_vocabulary_progress
```

using Optimistic Locking.

If the conflict cannot be safely retried:

```text
HTTP 409
code = CONCURRENT_UPDATE
```

Example:

```json
{
  "timestamp": "2026-08-28T10:30:00Z",
  "status": 409,
  "code": "CONCURRENT_UPDATE",
  "message": "Learning state was updated by another request.",
  "path": "/api/v1/learning/attempts",
  "details": []
}
```

---

# 37. Pagination

Paginated endpoints accept:

```text
page
size
sort
```

Baseline:

```text
page >= 0
1 <= size <= 100
```

Defaults:

```text
page = 0
size = 20
```

---

# 38. Filtering & Search

Vocabulary:

```text
search
cefr
topicId
partOfSpeech
```

Users:

```text
search
role
status
```

AI Content:

```text
status
contentScope
contentType
```

---

# 39. Sorting

Format:

```text
sort=field,direction
```

Examples:

```text
sort=word,asc
sort=createdAt,desc
```

Only whitelisted fields may be sorted.

---

# 40. Validation

Use:

```text
Jakarta Bean Validation
```

Typical annotations:

```java
@NotBlank
@Email
@Size
@NotNull
@Min
@Max
@PositiveOrZero
```

Business validation remains in Service/domain logic.

---

# 41. Rate Limiting

At minimum protect:

```text
POST /auth/login
POST /auth/refresh
POST /auth/google
POST /admin/ai-content/generate
POST /learning/personalized-exercise
```

Possible implementation:

```text
Spring Boot
+
Redis
```

or reverse proxy.

---

# 42. AI Budget Enforcement

AI endpoints must pass through:

```text
AiContentService
 ↓
BudgetGuard
```

If allowed:

```text
→ LLM
```

If blocked:

```text
HTTP 429
code = AI_BUDGET_EXCEEDED
```

Background blocked requests must be recorded as:

```text
ai_requests.status = BLOCKED_BY_BUDGET
```

Cached approved content remains usable.

---

# 43. AI Content Boundary

## Reusable AI Content

```text
/admin/ai-content/generate
        ↓
LLM
        ↓
Validation
        ↓
PENDING_REVIEW
        ↓
Admin Review
        ↓
APPROVED
```

## Personalized AI Content

```text
/learning/personalized-exercise
        ↓
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

Personalized results do not require per-result manual Admin Review.

---

# 44. Background Work vs Client-Facing Async

V1 does **not** expose a client-facing async job contract for AI generation. No `202 Accepted`, `jobId`, or `/jobs/{jobId}` is required. Background jobs remain internal for notification sending, AI usage aggregation, Daily Plan pre-generation, and idempotency cleanup.

---

# 45. API Security Rules

Never expose:

```text
password_hash
refresh token plaintext
AI provider secret
OAuth client secret
internal stack traces
```

Admin APIs require:

```text
JWT
+
ROLE_ADMIN
```

Clients never access directly:

```text
PostgreSQL
LLM provider
Redis
private storage
```

---

# 46. API Contract Stability

Internal changes must not force mobile changes if the business contract is unchanged.

Examples:

```text
Rule-based
→ ML

SM-2
→ ML-assisted SRS

Provider A
→ Provider B
```

---

# 47. Main User Flow

```text
REGISTER
   ↓
LOGIN
   ↓
GET /users/me
   ↓
GET /goals
   ↓
PUT /users/me/goals
   ↓
GET /cefr-levels
   ↓
POST /assessments
   ↓
Assessment Answers
   ↓
GET /learning/today
   ↓
POST /learning/sessions
   ↓
POST /learning/attempts
   ↓
SRS Update
   ↓
GET /progress
   ↓
GET /progress/weak-words
```

---

# 48. Daily Learning Flow

```text
GET /learning/today
        ↓
Receive targets
        ↓
GET /learning/today/items
        ↓
POST /learning/sessions
        ↓
Study
        ↓
POST /learning/attempts
        ↓
SRS result
        ↓
Continue
        ↓
POST /learning/sessions/{id}/complete
        ↓
POST /learning/today/complete
```

---

# 49. Personalized Exercise Flow

```text
User
 ↓
POST /learning/personalized-exercise
 ↓
Personalization Engine
 ↓
Select weak words
 ↓
Budget Guard
 ↓
LLM
 ↓
Schema Validation
 ↓
Safety Filter
 ↓
Exercise Response
 ↓
User answers
 ↓
POST /learning/attempts
 ↓
Analytics
```

---

# 50. OpenAPI Organization

Swagger/OpenAPI tags:

```text
Auth
Users
Goals
CEFR
Assessments
Topics
Vocabulary
Learning
Personalization
Quiz
Progress
Gamification
Notifications
Admin Users
Admin Vocabulary
Admin Topics
Admin Quiz
Admin AI Content
Admin Statistics
Admin Audit
```

---

# 51. Recommended Controller Structure

```text
AuthController
UserController
GoalController
CefrController
AssessmentController
TopicController
VocabularyController
LearningController
ProgressController
QuizController
GamificationController
NotificationController
PersonalizedExerciseController
AdminUserController
AdminVocabularyController
AdminTopicController
AdminQuizController
AdminAiContentController
AdminStatisticsController
AdminAuditController
```

---

# 52. Recommended DTO Naming

```text
RegisterRequest
LoginRequest
TokenResponse

UpdateProfileRequest
UserProfileResponse

SetUserGoalsRequest
UserGoalResponse

SubmitAssessmentAnswerRequest
AssessmentResultResponse

VocabularyResponse
VocabularyListResponse

CreateLearningSessionRequest
LearningSessionResponse
SubmitLearningAttemptRequest
LearningAttemptResponse

DailyPlanResponse
DailyPlanItemResponse

QuizResponse
SubmitQuizAnswerRequest
QuizAttemptResponse

ProgressOverviewResponse
WeakWordResponse

PersonalizedExerciseRequest
PersonalizedExerciseResponse
```

---

# 53. Testing Strategy

Every endpoint should test:

```text
Happy path
Validation failure
Authentication failure
Authorization failure
Not found
Conflict
Duplicate request
Concurrent request
Rate limit
External AI failure
Database failure where relevant
```

Tools:

```text
Swagger UI
Postman
JUnit
MockMvc
Integration Tests
Testcontainers
```

Testcontainers can provide:

```text
PostgreSQL
Redis
```

---

# 54. API Acceptance Criteria

```text
[ ] All V1 use cases have API coverage.
[ ] All authenticated APIs verify JWT.
[ ] Admin APIs verify ROLE_ADMIN.
[ ] Error response is standardized.
[ ] Small reference lists return JSON arrays.
[ ] Paginated lists return PaginatedResponse.
[ ] Domain-specific responses are explicit.
[ ] Learning Attempt accepts answerQuality only.
[ ] isCorrect is derived by backend.
[ ] Refresh Token expiry/revoke are enforced.
[ ] Refresh Token rotation is documented as a future improvement.
[ ] Pagination is standardized.
[ ] Validation is implemented.
[ ] Idempotency is defined for event-like requests.
[ ] Concurrency is handled for learning state.
[ ] SRS result is returned after a learning attempt.
[ ] Daily Plan hides internal personalization implementation.
[ ] AI budget guard protects AI endpoints.
[ ] Reusable AI content follows Admin Review.
[ ] Personalized AI content uses automated validation/safety.
[ ] No sensitive secret is exposed.
[ ] Swagger/OpenAPI documents public APIs.
[ ] Android and Flutter can use the same contract.
```

---

# 55. Known Schema Alignment Notes

## Notification Read State

Database Schema v1.6 does not persist notification read/unread state (`read_at` / `is_read`). Therefore persisted **mark as read** remains outside V1. Delivery lifecycle (`PENDING`, `SENT`, `FAILED`, `CANCELLED`) is independent from read state.

## Device Push Tokens and Preferences

Device push registration **is part of V1**. Database Schema v1.6 provides `user_devices` and `notification_preferences`.

Canonical endpoints:

```text
PUT    /devices/{installationId}/push-token
DELETE /devices/{installationId}
GET    /notification-preferences
PUT    /notification-preferences
```

Android V1 uses FCM. `installationId` identifies an app installation; token refresh updates the same installation resource. Push tokens must not be returned in ordinary user/admin payloads or written to logs.

## Refresh Token Rotation

Database Schema v1.6 supports refresh-token expiry and revocation. V1 does not require refresh-token rotation; rotation remains a future hardening improvement and must not be assumed by clients.

---

# 56. Final API Surface

```text
AUTH
├── POST /auth/register
├── POST /auth/login
├── POST /auth/refresh
├── POST /auth/logout
└── POST /auth/google

USER
├── GET /users/me
├── GET /users/me/profile
├── PUT /users/me/profile
├── GET /users/me/goals
└── PUT /users/me/goals

REFERENCE
├── GET /goals
├── GET /cefr-levels
└── GET /topics

ASSESSMENT
├── POST /assessments
├── GET /assessments/{id}
├── GET /assessments/{id}/next-question
└── POST /assessments/{id}/answers

VOCABULARY
├── GET /vocabulary
├── GET /vocabulary/{id}
└── GET /vocabulary/{id}/examples

LEARNING
├── POST /learning/sessions
├── GET /learning/sessions/{id}
├── POST /learning/sessions/{id}/complete
├── GET /learning/today
├── GET /learning/today/items
├── POST /learning/today/complete
├── GET /learning/reviews
├── POST /learning/attempts
└── POST /learning/personalized-exercise

PROGRESS
├── GET /progress
├── GET /progress/weak-words
└── GET /progress/trend

QUIZ
├── GET /quizzes
├── GET /quizzes/{id}
├── POST /quizzes/{id}/attempts
├── GET /quiz-attempts/{id}
├── POST /quiz-attempts/{id}/answers
└── POST /quiz-attempts/{id}/complete

GAMIFICATION
├── GET /gamification/streak
├── GET /gamification/xp
├── GET /gamification/xp/history
├── GET /gamification/badges
└── GET /gamification/leaderboard

NOTIFICATIONS / DEVICES
├── GET /notifications
├── PUT /devices/{installationId}/push-token
├── DELETE /devices/{installationId}
├── GET /notification-preferences
└── PUT /notification-preferences

ADMIN
├── /admin/users
├── /admin/vocabulary
├── /admin/topics
├── /admin/quizzes
├── /admin/ai-content
├── /admin/statistics
└── /admin/audit-logs
```

---

# 57. Final Architecture Relationship

```text
SRS v1.2
    ↓
Database Schema v1.6
    ↓
System Architecture v1.3
    ↓
AI Personalization Specification v1.3
    ↓
API Specification v1.4
    ↓
Android Java / Admin Web
    ↓
Spring Boot
    ↓
PostgreSQL + AI/LLM
```

---

# 58. Next Implementation Step

```text
API Specification v1.4
      ↓
OpenAPI YAML
      ↓
Spring Boot Project
      ↓
DTO
      ↓
Controller
      ↓
Service
      ↓
Repository
      ↓
JPA Entity
      ↓
Flyway Migration
      ↓
Integration Test
```

API Specification v1.4 is the contract between Mobile/Admin clients and Spring Boot Backend.


---

# 59. AI Content Review Persistence Rule

The API and database use separate concepts for the review request and the stored record:

```text
Request field
→ reason

Database field
→ review_note

Response field
→ reviewNote
```

For a reusable AI content rejection:

```text
reason
   ↓
ai_generated_content.review_note
   +
admin_audit_logs.details.reason
```

The audit log must still be written even when `review_note` is stored on the content record.

For personalized/ephemeral AI content:

```text
No manual Admin Review
→ validation + safety filter
```


---

# 60. Idempotency Error Contract

For event ID reuse with a different endpoint or payload:

```json
{
  "timestamp": "2026-08-30T09:00:00Z",
  "status": 409,
  "code": "IDEMPOTENCY_KEY_REUSE",
  "message": "The eventId was already used for a different logical request.",
  "path": "/api/v1/learning/attempts",
  "details": []
}
```

For the same logical request repeated:

```text
Return the previously stored response.
```

No second business transaction is created.

---

# Reconciled v1.4 Error Additions

```text
ASSESSMENT_ALREADY_IN_PROGRESS → 409
ASSESSMENT_QUESTION_ALREADY_ANSWERED → 409
ASSESSMENT_CONTENT_UNAVAILABLE → 503
CONCURRENT_UPDATE → 409
IDEMPOTENCY_KEY_REUSE → 409
AI_GENERATION_FAILED → 503 (or mapped provider/service failure)
AI_BUDGET_EXCEEDED → 429
```

`answerQuality` remains the sole client correctness input for learning attempts; client-supplied `isCorrect` is not accepted.
