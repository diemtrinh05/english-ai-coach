# Flutter Technical Specification v1.1 — English AI Coach

**Project:** English AI Coach  
**Document:** Flutter Technical Specification  
**Version:** 1.1  
**Status:** APPROVED BASELINE  
**Date:** 2026-08-31  

**Purpose:** Đặc tả kỹ thuật cho Flutter V2 sau Android Java V1. Flutter V2 là client mới của cùng REST API và không tạo backend/business engine riêng.

---

# 1. Scope

Flutter V2 kế thừa:

```text
REST API
OpenAPI contract
Backend business rules
Design tokens
UX flows
Screen semantics
Authentication model
Idempotency contract
```

Flutter V2 không được tạo lại:

```text
SRS engine
Forgetting prediction
Recommendation engine
Daily workload engine
XP/streak rules
AI budget logic
LLM calls
```

---

# 2. V2 Product Goals

Flutter V2 phải tái hiện đầy đủ core learner experience:

```text
Authentication
Onboarding
Assessment
Today's Personalized Plan
New Words
Review
Flashcard
Quiz
Personalized Exercise
Progress
Weak Words
Gamification
Profile
Notifications
```

Future modules:

```text
Speaking
Writing
Listening
Reading
AI Tutor
Advanced ML
```

không thuộc V2 Core hiện tại trừ khi được kích hoạt bằng product scope mới.

---

# 3. Technology Baseline

```text
Flutter
Dart
Material 3 / project-approved design system

Networking:
Dio or standardized HTTP client

Serialization:
json_serializable / freezed-compatible approach

State:
Riverpod / Bloc / project-approved state management

Routing:
go_router or equivalent declarative router

Secure Storage:
flutter_secure_storage

Local read cache:
Hive / Isar / Drift / SQLite as selected
```

Technology choice may be finalized during project bootstrap, but architecture must remain framework-independent at feature/domain level.

---

# 4. Architecture

Recommended:

```text
Presentation
    ↓
Application / Use Case
    ↓
Domain
    ↓
Repository
    ↓
Data
    ↓
Remote API
```

Optional:

```text
Repository
   ├── Remote
   └── Local Read Cache
```

---

# 5. Flutter Project Structure

```text
lib/
├── app/
│   ├── app.dart
│   ├── router/
│   ├── theme/
│   └── bootstrap/
│
├── core/
│   ├── network/
│   ├── auth/
│   ├── storage/
│   ├── errors/
│   ├── time/
│   ├── connectivity/
│   ├── analytics/
│   ├── localization/
│   └── utils/
│
├── data/
│   ├── dto/
│   ├── mapper/
│   ├── remote/
│   ├── local/
│   └── repositories/
│
├── domain/
│   ├── entities/
│   ├── repositories/
│   └── usecases/
│
└── features/
    ├── auth/
    ├── onboarding/
    ├── assessment/
    ├── home/
    ├── learn/
    ├── review/
    ├── vocabulary/
    ├── personalized/
    ├── quiz/
    ├── progress/
    ├── gamification/
    ├── notifications/
    └── profile/
```

---

# 6. Feature Structure

Example:

```text
features/review/
├── presentation/
│   ├── review_page.dart
│   ├── review_controller.dart
│   └── review_state.dart
├── domain/
│   └── get_due_reviews.dart
└── data/
    ├── review_api.dart
    ├── review_dto.dart
    └── review_repository_impl.dart
```

Keep feature boundaries clear.

---

# 7. Application Shell

```text
MaterialApp
    ↓
Router
    ├── Splash
    ├── Auth
    ├── Onboarding
    └── Main
```

Main:

```text
Home
Learn
Review
Progress
Profile
```

---

# 8. State Management

Use one approved state-management solution consistently.

Each feature owns:

```text
State
Controller/Notifier
Events/Commands
```

Do not mix multiple state frameworks without strong reason.

---

# 9. State Model

Every network-driven feature supports:

```text
Initial
Loading
Success
Empty
Error
Offline
```

Mutations:

```text
Idle
Submitting
Success
Failure
```

Prefer sealed classes/unions where supported by the selected tooling.

---

# 10. Navigation

Recommended conceptual routes:

```text
/
 /splash
 /login
 /register
 /onboarding
 /assessment
 /home
 /learn
 /review
 /flashcard/:id
 /personalized
 /quiz/:id
 /progress
 /weak-words
 /profile
 /settings
```

Actual router syntax is implementation-specific.

---

# 11. Deep Linking

Future-compatible:

```text
englishaicoach://review
englishaicoach://vocabulary/{id}
englishaicoach://progress
```

Notification deep links should route through the application router.

---

# 12. Back Navigation

```text
Normal page
→ previous route

Learning session
→ confirmation before abandoning when necessary

Quiz
→ confirmation before losing active attempt if applicable
```

Browser/platform back behavior must be handled consistently on supported platforms.

---

# 13. API Base URL

Configuration:

```text
DEV
STAGING
PROD
```

Use:

```text
--dart-define
environment files
build flavors
```

Do not hard-code production API URL in widgets.

---

# 14. HTTP Client

Recommended:

```text
Dio
```

Responsibilities:

```text
base URL
headers
serialization
timeouts
interceptors
auth
logging in debug
```

---

# 15. Auth Interceptor

Attach:

```http
Authorization: Bearer <access-token>
```

Do not log:

```text
Authorization
refresh token
password
API key
```

---

# 16. Token Manager

Components:

```text
TokenStorage
TokenManager
AuthInterceptor
RefreshCoordinator
```

Responsibilities:

```text
read token
save token
clear token
attach access token
refresh on 401
logout on refresh failure
```

---

# 17. Concurrent 401 Handling

If multiple requests receive 401:

```text
Request A → 401
Request B → 401
Request C → 401

        ↓
single refresh operation
        ↓
new access token
        ↓
retry pending requests
```

Do not issue parallel refresh requests.

---

# 18. Refresh Token Rotation

Current product contract:

```text
expiry
revocation
```

Rotation is future security enhancement.

Client architecture must isolate token behavior so rotation can be introduced later.

---

# 19. Secure Storage

Use:

```text
flutter_secure_storage
```

for:

```text
access token
refresh token
```

Do not use:

```text
plain shared preferences
logs
unencrypted local files
```

for credentials.

---

# 20. Authentication Flow

```text
Splash
 ↓
restore tokens
 ↓
authenticated?
 ├── yes → check onboarding
 └── no → Login
```

After login:

```text
profile/bootstrap
 ↓
onboarding complete?
 ├── yes → Home
 └── no → Onboarding
```

---

# 21. Registration

Client validation:

```text
name required
valid email
password required
confirm password
```

Server remains authoritative.

---

# 22. Login Error Mapping

```text
invalid credentials
→ controlled auth error

temporary lock
→ "Too many login attempts..."

network
→ retry

server
→ generic error
```

Never expose internal authentication details.

---

# 23. Domain Models

Recommended:

```text
User
UserProfile
Goal
CefrLevel
VocabularyItem
VocabularyExample
DailyPlan
LearningSession
LearningAttemptResult
ReviewItem
ProgressSummary
WeakWord
Quiz
QuizQuestion
QuizResult
PersonalizedExercise
Streak
Badge
Notification
```

---

# 24. DTO Strategy

API DTOs remain isolated from widgets.

Example:

```text
LoginRequestDto
LoginResponseDto
TodayPlanDto
LearningAttemptRequestDto
LearningAttemptResponseDto
ReviewItemDto
QuizDto
ProgressDto
```

Map:

```text
DTO
 ↓
Domain Model
 ↓
Presentation State
```

---

# 25. Repository

Example:

```dart
abstract class LearningRepository {
  Future<DailyPlan> getTodayPlan();

  Future<LearningSession> startSession(SessionType type);

  Future<LearningAttemptResult> submitAttempt(
    LearningAttemptRequest request,
  );
}
```

Repository hides remote/cache decisions.

---

# 26. Use Cases

Examples:

```text
Login
Register
GetCurrentUser
CompleteOnboarding
SubmitAssessmentAnswer
GetTodayPlan
StartLearningSession
SubmitLearningAttempt
GetDueReviews
GetProgress
GetWeakWords
GeneratePersonalizedExercise
SubmitQuizAnswer
CompleteQuiz
Logout
```

---

# 27. Home

Primary API:

```text
GET /api/v1/learning/today
```

May also load:

```text
progress summary
profile
review summary
```

Parallelize independent calls where useful.

---

# 28. Home State

```text
HomeState
├── Initial
├── Loading
├── Content
├── Empty
├── Error
└── Offline
```

Content may contain:

```text
user
dailyPlan
progress
reviewSummary
newWords
practiceRecommendation
```

---

# 29. Daily Plan

Flutter renders server-selected:

```text
newWordsTarget
reviewWordsTarget
quizTarget
estimatedMinutes
```

Flutter must not calculate daily workload.

---

# 30. Daily Plan vs Review

Home:

```text
planned today
```

Review:

```text
currently due
```

Keep these concepts visually separate.

---

# 31. Review

API:

```text
GET /api/v1/learning/reviews
```

Display:

```text
word
priority
accuracy
reason
```

No duplicated backend priority algorithm.

---

# 32. Flashcard

State:

```text
Front
Back
Submitting
Feedback
Completed
Error
Offline
```

Flow:

```text
front
 ↓
reveal
 ↓
quality
 ↓
submit
 ↓
server result
 ↓
feedback
 ↓
next
```

---

# 33. Answer Quality

Client sends:

```text
answerQuality = 0..5
```

Client does not send:

```text
isCorrect
```

Backend derives correctness.

---

# 34. Learning Attempt Request

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

---

# 35. Event ID

Create:

```text
UUID
```

once per logical mutation.

Example:

```text
User selects Hard
→ eventId A

timeout
→ retry eventId A
```

Never create event B for the same logical action.

---

# 36. Mutation Helper

Recommended abstraction:

```text
LogicalMutation<T>
├── eventId
├── payload
└── retry()
```

This keeps retry semantics consistent.

---

# 37. Idempotency 409

Backend code:

```text
IDEMPOTENCY_KEY_REUSE
```

Client behavior:

```text
do not generate a replacement eventId automatically
stop retry
refresh relevant state
show controlled conflict
```

---

# 38. Optimistic Lock 409

Backend code:

```text
CONCURRENT_UPDATE
```

Client behavior:

```text
refresh latest state
allow a new logical action
```

Do not silently overwrite.

---

# 39. Important 409 Distinction

```text
409 IDEMPOTENCY_KEY_REUSE
→ same eventId used for different request

409 CONCURRENT_UPDATE
→ server state changed
```

Never collapse both into the same semantic branch.

---

# 40. Offline V1

Official behavior:

```text
ONLINE
→ full functionality

OFFLINE
→ read-only cached content where available
→ no learning mutations
```

No:

```text
offline attempt queue
auto sync
SRS offline
quiz offline submit
cross-device merge
```

---

# 41. Connectivity

Use Flutter connectivity APIs for UX hints.

Do not treat:

```text
Wi-Fi connected
```

as proof that API is reachable.

Network request outcome remains authoritative.

---

# 42. Offline Mutation

Before:

```text
learning attempt
quiz answer
quiz completion
```

if offline:

```text
block
→ Offline State
→ Retry
```

---

# 43. Local Cache

Allowed:

```text
Goals
CEFR
Topics
Vocabulary
approved reusable content
```

Not authoritative for:

```text
SRS
progress
XP
streak
attempt history
quiz score
```

---

# 44. Local Storage

Use secure storage for credentials.

Use local database/cache only for:

```text
read-only content cache
```

Do not implement full offline sync.

---

# 45. Audio

Architecture:

```text
AudioService
 ↓
cache
 ↓
player
```

Source:

```text
audioUrl
```

Failure:

```text
continue text learning
```

---

# 46. Image

Use image caching.

States:

```text
loading
loaded
error
```

Image failure must not break vocabulary learning.

---

# 47. Search

Vocabulary search:

```text
debounce ~250–400ms
cancel previous request
show loading
show empty
```

No API call for every keystroke.

---

# 48. Pagination

Where API supports pagination:

```text
page
size
totalElements
totalPages
hasNext
```

State:

```text
LoadingFirst
Loaded
LoadingNext
EndReached
ErrorFirst
ErrorNext
```

---

# 49. Quiz

Client keeps:

```text
quizId
attemptId
questionIndex
selected answer
```

Server owns:

```text
correctness
score
final result
```

---

# 50. Quiz Answer

Request:

```json
{
  "questionId": "uuid",
  "userAnswer": "A",
  "responseTimeMs": 1200,
  "eventId": "uuid"
}
```

Retry same logical answer with same eventId.

---

# 51. Quiz Completion

Request:

```json
{
  "eventId": "uuid"
}
```

Completion retries use same eventId.

---

# 52. Assessment

Flutter sends:

```text
selected answer
```

Backend decides:

```text
score
adaptive difficulty
CEFR result
```

Flutter does not implement independent placement algorithm.

---

# 53. Personalized Exercise

Flow:

```text
tap practice
 ↓
request
 ↓
loading
 ↓
AI-generated content
 ↓
answer
 ↓
feedback
```

---

# 54. AI Failure

Preferred UX:

```text
Personalized practice is unavailable right now.

[Try Standard Practice]
```

Do not show:

```text
LLM exception
API key
provider failure
```

---

# 55. Progress

Display:

```text
words learned
mastered
accuracy
learning time
weak words
history
```

Server result is authoritative.

---

# 56. Progress Refresh

After:

```text
learning attempt
quiz completion
session completion
```

refresh relevant progress views.

Avoid arbitrary local arithmetic as final state.

---

# 57. Gamification

Display:

```text
XP
Level
Streak
Badges
```

Do not calculate eligibility/client-side XP.

---

# 58. Profile

```text
Profile
Goals
Learning Settings
Notifications
Logout
```

Server owns account state.

---

# 59. Notifications

FCM:

```text
register device token
receive notification
handle tap
route to destination
```

Backend owns:

```text
schedule
send
retry
status
```

---

# 60. Localization

Use Flutter localization system.

At minimum:

```text
English UI
Vietnamese learning content
```

All UI strings must be localizable.

---

# 61. Theme

Reuse framework-independent design tokens:

```text
colors
typography
spacing
radius
elevation
component semantics
```

Do not copy Android XML directly into Flutter.

---

# 62. Component Library

Create reusable components:

```text
PrimaryButton
SecondaryButton
AppCard
VocabularyCard
ReviewCard
RecommendationCard
ProgressCard
StatCard
AnswerOption
QualitySelector
StatusChip
LoadingSkeleton
EmptyState
ErrorState
OfflineState
```

---

# 63. Accessibility

Test:

```text
screen reader
font scaling
contrast
semantic labels
touch target
focus order
```

---

# 64. Animation

Keep consistent with mobile design:

```text
short
purposeful
non-blocking
```

Avoid large decorative animations.

---

# 65. Performance

Avoid:

```text
large widget rebuilds
unbounded lists
large image memory use
heavy work on UI isolate
```

Use:

```text
ListView.builder
cached images
pagination
selective rebuilds
```

---

# 66. Background Work

V1:

```text
minimal
```

Allowed:

```text
FCM handling
small cache maintenance
safe token/device registration retry
```

Not allowed:

```text
offline learning sync
SRS reconciliation
```

---

# 67. App Lifecycle

Handle:

```text
resume
pause
background
process recreation
```

On resume, refresh server-sensitive state when appropriate.

---

# 68. Error Model

Map API errors:

```text
400 → Validation
401 → Authentication
403 → Forbidden
404 → Not Found
409 → Conflict
429 → Rate Limited
500 → Server
503 → Unavailable
Network → Network/Offline
```

---

# 69. Exception Boundary

Do not leak exceptions into widgets.

Use:

```text
Data Exception
 ↓
Domain Failure
 ↓
Presentation Error State
```

---

# 70. Analytics

Optional events:

```text
auth_login_success
assessment_completed
daily_plan_started
learning_attempt_submitted
learning_session_completed
personalized_exercise_started
quiz_completed
```

Do not send sensitive:

```text
password
tokens
raw private learning payloads
```

---

# 71. Logging

Debug:

```text
request path
status
duration
```

Production:

```text
minimal
```

Never log tokens or passwords.

---

# 72. Testing Pyramid

```text
Unit
 ↓
Widget
 ↓
Integration
 ↓
End-to-End
```

---

# 73. Unit Tests

Priority:

```text
ViewModels/Notifiers
UseCases
Validators
Mapper
EventId lifecycle
Error mapping
```

---

# 74. Widget Tests

P0:

```text
Login
Onboarding
Home
Review
Flashcard
Progress
Profile
```

Test:

```text
loading
success
empty
error
offline
button interactions
```

---

# 75. Integration Tests

Test:

```text
auth
token refresh
learning attempt
idempotency retry
quiz attempt
personalized exercise
```

---

# 76. Idempotency Tests

```text
new operation
→ eventId A

retry
→ eventId A

new operation
→ eventId B
```

Expected:

```text
A remains stable
B is different
```

---

# 77. Contract Tests

Verify against:

```text
OpenAPI v1.4
```

Check:

```text
field names
nullable
status codes
pagination
error codes
eventId
reviewNote where applicable
```

---

# 78. Android-to-Flutter Parity Tests

Both clients must produce equivalent UI behavior for:

```text
login
today plan
review
learning attempt
quiz
progress
profile
```

Differences in implementation are allowed; product semantics are not.

---

# 79. Build Flavors

Recommended:

```text
development
staging
production
```

Configure:

```text
base URL
logging
analytics
feature flags
```

---

# 80. CI

```text
format
analyze
unit tests
widget tests
integration tests
build
```

PR must pass required checks.

---

# 81. Release

Before release:

```text
production API
HTTPS
secure storage
debug logging disabled
notifications
crash reporting if used
```

---

# 82. Security

Flutter never contains:

```text
LLM API key
database password
JWT signing secret
OAuth private secret
```

Mobile app never calls LLM provider directly.

---

# 83. Future Flutter Migration Rule

When Android Java V1 and Flutter V2 coexist:

```text
                Spring Boot API
                 /           \
                /             \
        Android Java        Flutter
              V1              V2
```

Both clients must consume the same:

```text
API
business semantics
authentication rules
SRS result
AI contracts
```

---

# 84. Flutter V2 Readiness Checklist

```text
[ ] OpenAPI stable
[ ] API error codes stable
[ ] auth contract stable
[ ] learning attempt contract stable
[ ] eventId/idempotency stable
[ ] CONCURRENT_UPDATE stable
[ ] UI design tokens stable
[ ] screen flows stable
[ ] backend business rules centralized
```

---

# 85. Final Flutter Non-Negotiable Rules

```text
1. Flutter is a client, not a second learning engine.
2. Backend owns SRS.
3. Backend owns correctness.
4. Backend owns progress.
5. Backend owns XP/streak.
6. Flutter sends answerQuality, not isCorrect.
7. Retry uses the same eventId.
8. CONCURRENT_UPDATE and IDEMPOTENCY_KEY_REUSE are distinct.
9. V1/V2 clients do not implement offline sync.
10. Flutter never calls LLM providers directly.
```

---

# 86. Final Implementation Sequence

```text
Flutter project
 ↓
Theme / tokens
 ↓
Router
 ↓
Networking
 ↓
Secure auth
 ↓
Login / Register
 ↓
Onboarding
 ↓
Assessment
 ↓
Home
 ↓
Review
 ↓
Flashcard
 ↓
Learning Attempt + Idempotency
 ↓
Progress
 ↓
Quiz
 ↓
Personalized Exercise
 ↓
Notifications
 ↓
Testing
```

---

# 87. Definition of Done

A Flutter feature is complete when:

```text
[ ] UI implemented
[ ] Navigation implemented
[ ] State model implemented
[ ] UseCase implemented
[ ] Repository integrated
[ ] DTO mapping correct
[ ] API contract checked
[ ] Loading state
[ ] Empty state
[ ] Error state
[ ] Offline state where relevant
[ ] Accessibility
[ ] Tests
```

---

# 88. Final Architecture

```text
Flutter UI
    ↓
State / Controller
    ↓
UseCase
    ↓
Repository
    ↓
Remote API
    ↓
Spring Boot
    ↓
PostgreSQL / AI / external services
```

---

# 89. Final Goal

Flutter V2 should provide the same product:

```text
What to learn today?
What to review?
Why was it recommended?
How am I progressing?
```

while remaining a clean client of the existing learning platform.

---

# Reconciled Contract Alignment (Future V2 Client)

Flutter is not implemented in the V1 milestone, but its future client contract must align API/OpenAPI v1.4+: body `eventId` semantics, no idempotency header, nullable `DailyPlanItem.vocabularyId`, aggregate QUIZ plan item, assessment eventId requests, notification device/preferences endpoints, synchronous V1 AI responses, and exact error codes `CONCURRENT_UPDATE` / `IDEMPOTENCY_KEY_REUSE`.

When Flutter V2 is implemented, use the Flutter localization mechanism; do not copy Android strings or business rules into the client.
