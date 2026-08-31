# Android Java Technical Specification v1.1 — English AI Coach

**Project:** English AI Coach  
**Document:** Android Java Technical Specification  
**Version:** 1.1  
**Status:** APPROVED BASELINE  
**Date:** 2026-08-31  

**Purpose:** Tài liệu này chuyển toàn bộ Mobile UI/UX, API, Database, Backend Technical Specification và System Architecture thành đặc tả kỹ thuật chi tiết để triển khai **Android Java V1**.

**Target:** Android smartphone, portrait-first.

**Official V1 connectivity model:** **Online-first**. Offline-first synchronization is not part of V1.

---

# 1. Source of Truth

Android implementation phải bám theo:

```text
SRS
 ↓
API Contract
 ↓
OpenAPI
 ↓
Backend business rules
 ↓
Mobile UI/UX
 ↓
Design System
 ↓
High-Fidelity
 ↓
Interactive Prototype
```

Backend là authoritative business layer.

Client không phải source of truth cho:

```text
SRS
Forgetting Risk
Weakness Score
Recommendation
Daily Workload
XP
Streak
Quiz Score
```

---

# 2. Android V1 Goals

App phải hỗ trợ:

```text
Authentication
Onboarding
Placement Assessment
Today's Plan
New Words
Review
Flashcard
Learning Attempt
SRS Feedback
Personalized Exercise
Quiz
Progress
Weak Words
Gamification
Profile
Notifications
```

---

# 3. Android V1 Non-Goals

Không triển khai trong V1:

```text
Full offline-first learning
Offline attempt queue
Automatic learning-data synchronization
Cross-device conflict merge
Speaking
Writing
Listening
Reading
AI Tutor
Advanced ML client inference
Direct DB access
```

---

# 4. Recommended Android Architecture

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
Retrofit / OkHttp
 ↓
Spring Boot API
```

Optional local cache:

```text
Repository
 ↓
Local Read Cache
```

nhưng server vẫn là source of truth.

---

# 5. Architecture Style

Recommended:

```text
MVVM
+
feature-oriented packaging
+
domain/use-case layer
+
repository abstraction
```

Không cần full enterprise Clean Architecture nếu làm tăng boilerplate không cần thiết, nhưng boundaries phải rõ.

---

# 6. Project Structure

```text
app/
├── src/
│   ├── main/
│   │   ├── java/com/example/englishaicoach/
│   │   │
│   │   ├── core/
│   │   │   ├── api/
│   │   │   ├── auth/
│   │   │   ├── navigation/
│   │   │   ├── network/
│   │   │   ├── storage/
│   │   │   ├── ui/
│   │   │   ├── util/
│   │   │   └── validation/
│   │   │
│   │   ├── data/
│   │   │   ├── api/
│   │   │   ├── dto/
│   │   │   ├── local/
│   │   │   ├── mapper/
│   │   │   ├── repository/
│   │   │   └── remote/
│   │   │
│   │   ├── domain/
│   │   │   ├── model/
│   │   │   ├── repository/
│   │   │   └── usecase/
│   │   │
│   │   └── feature/
│   │       ├── auth/
│   │       ├── onboarding/
│   │       ├── assessment/
│   │       ├── home/
│   │       ├── learn/
│   │       ├── review/
│   │       ├── vocabulary/
│   │       ├── personalized/
│   │       ├── quiz/
│   │       ├── progress/
│   │       ├── gamification/
│   │       ├── notifications/
│   │       └── profile/
│   │
│   ├── res/
│   │   ├── layout/
│   │   ├── navigation/
│   │   ├── values/
│   │   ├── drawable/
│   │   └── mipmap/
│   │
│   └── test/
└── build.gradle(.kts)
```

---

# 7. Feature Package Structure

Each feature follows:

```text
feature/
└── home/
    ├── HomeFragment.java
    ├── HomeViewModel.java
    ├── HomeUiState.java
    ├── HomeAction.java
    └── HomeViewModelFactory.java
```

For larger feature:

```text
feature/
└── review/
    ├── ReviewFragment.java
    ├── ReviewViewModel.java
    ├── ReviewUiState.java
    ├── ReviewAdapter.java
    ├── ReviewAction.java
    └── ReviewNavigator.java
```

Do not create unnecessary classes for trivial screens.

---

# 8. Android UI Strategy

Recommended:

```text
Activity
+
Fragment
+
XML Layout
+
ViewBinding
+
RecyclerView
```

Use Android Material components consistently.

Avoid:

```text
programmatically constructing every screen
```

unless dynamic content genuinely requires it.

---

# 9. Main Activity

Recommended:

```text
MainActivity
```

Responsibilities:

```text
host navigation
global auth/session state
global app shell
bottom navigation where applicable
```

Do not place business logic in MainActivity.

---

# 10. Fragment Responsibilities

Fragment:

```text
bind views
observe UI state
dispatch user actions
trigger navigation
```

Fragment must not:

```text
calculate SRS
call Retrofit directly
update database directly
calculate recommendation
manage JWT refresh logic
```

---

# 11. ViewModel Responsibilities

ViewModel:

```text
receive UI actions
call UseCase
hold UI state
handle loading/error/success
survive configuration changes
```

ViewModel does not directly access:

```text
Retrofit implementation
Room DAO
SharedPreferences
```

Use repository/use cases.

---

# 12. UseCase Layer

Examples:

```text
LoginUseCase
RegisterUseCase
GetCurrentUserUseCase
CompleteOnboardingUseCase
SubmitAssessmentAnswerUseCase
GetTodayPlanUseCase
StartLearningSessionUseCase
SubmitLearningAttemptUseCase
GetDueReviewsUseCase
GetProgressUseCase
GetWeakWordsUseCase
GeneratePersonalizedExerciseUseCase
SubmitQuizAnswerUseCase
CompleteQuizUseCase
LogoutUseCase
```

Use cases coordinate application actions.

---

# 13. Repository Layer

Repositories hide data sources.

Examples:

```text
AuthRepository
UserRepository
AssessmentRepository
VocabularyRepository
LearningRepository
ProgressRepository
QuizRepository
GamificationRepository
NotificationRepository
```

Example:

```java
public interface LearningRepository {

    Result<DailyPlan> getTodayPlan();

    Result<LearningSession> startSession(SessionType type);

    Result<LearningAttemptResult> submitAttempt(
        LearningAttemptRequest request
    );
}
```

---

# 14. Remote Data Source

```text
AuthRemoteDataSource
LearningRemoteDataSource
VocabularyRemoteDataSource
QuizRemoteDataSource
ProgressRemoteDataSource
AdminRemoteDataSource
```

Mobile V1 does not need Admin data access.

---

# 15. Domain Models

Do not expose DTO classes throughout UI.

Domain models:

```text
User
UserProfile
Goal
CefrLevel
VocabularyItem
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
Badge
Streak
Notification
```

---

# 16. DTOs

API-specific DTOs:

```text
LoginRequestDto
LoginResponseDto
RefreshTokenRequestDto
UserResponseDto
TodayPlanResponseDto
LearningAttemptRequestDto
LearningAttemptResponseDto
ReviewItemDto
ProgressResponseDto
WeakWordDto
QuizQuestionDto
QuizResultDto
```

DTOs map to domain models.

---

# 17. Mapper

```text
LoginResponseMapper
TodayPlanMapper
VocabularyMapper
LearningAttemptMapper
ProgressMapper
QuizMapper
```

Keep API-specific field names out of presentation where possible.

---

# 18. Networking Stack

Recommended:

```text
Retrofit
OkHttp
JSON converter
```

Responsibilities:

```text
HTTP
serialization
headers
timeouts
auth interceptor
logging in debug
```

---

# 19. API Base URL

Environment-based:

```text
DEV
STAGING
PROD
```

Do not hard-code production URL inside Fragment/ViewModel.

---

# 20. HTTP Interceptor

Authorization:

```text
Authorization: Bearer <access-token>
```

Request ID:

```text
X-Request-Id
```

where supported.

Never log:

```text
Authorization header
refresh token
password
API key
```

---

# 21. Token Management

Components:

```text
TokenManager
AuthInterceptor
Authenticator / RefreshHandler
```

Responsibilities:

```text
read access token
attach access token
detect 401
refresh when allowed
persist new access token
logout when refresh fails
```

---

# 22. Token Storage

Use secure Android credential storage.

Required:

```text
access token
refresh token
```

Do not store tokens in plain text logs or ordinary unprotected files.

---

# 23. Refresh Flow

```text
API request
 ↓
401
 ↓
Refresh access token
 ↓
retry original request
 ↓
success
```

If refresh fails:

```text
clear credentials
 ↓
navigate Login
```

Do not retry endlessly.

---

# 24. Refresh Concurrency

Multiple API calls may receive 401 simultaneously.

Use a synchronized refresh mechanism:

```text
Request A → 401
Request B → 401

        ↓
single refresh operation
        ↓
new access token
        ↓
retry A/B
```

Do not send multiple refresh calls simultaneously for one session.

---

# 25. Refresh Token Rotation

Current backend contract:

```text
expiry
revocation
```

Refresh token rotation is a future security enhancement unless the backend enables it.

Client must be designed so rotation can be added without changing screen-level code.

---

# 26. Auth State

Global:

```text
UNKNOWN
AUTHENTICATED
UNAUTHENTICATED
SESSION_EXPIRED
```

Splash decides:

```text
Auth state
+
Onboarding state
```

---

# 27. Navigation Architecture

Recommended destinations:

```text
AuthGraph
OnboardingGraph
MainGraph
LearningGraph
QuizGraph
ProfileGraph
```

Conceptual:

```text
Splash
 ↓
Auth
 ├── Login
 └── Register
      ↓
Onboarding
      ↓
Main
```

---

# 28. Main Navigation

```text
Home
Learn
Review
Progress
Profile
```

Bottom navigation only exists in MainGraph.

---

# 29. Back Navigation Rules

Normal screens:

```text
Back → previous screen
```

Flashcard:

```text
Back on back state
→ front state
```

Learning session:

```text
Back
→ confirmation if progress may be lost
```

Avoid accidental session termination.

---

# 30. Deep Links

Future-compatible routes:

```text
app://englishaicoach/review
app://englishaicoach/vocabulary/{id}
app://englishaicoach/progress
```

Not required for MVP unless notification links need them.

---

# 31. Global UI State

Every network screen:

```text
INITIAL
LOADING
SUCCESS
EMPTY
ERROR
OFFLINE
```

Mutating action:

```text
IDLE
SUBMITTING
SUCCESS
FAILURE
```

---

# 32. UiState Recommendation

Example:

```java
public sealed-like state representation can be implemented
using Java classes/interfaces when appropriate.
```

Practical Java approach:

```text
HomeUiState
    Loading
    Success
    Empty
    Error
    Offline
```

Do not rely on scattered booleans such as:

```text
isLoading
isError
isEmpty
isOffline
```

which can create impossible combinations.

---

# 33. Error Model

Mobile maps backend error:

```text
status
code
message
```

into:

```text
UiError
```

Example:

```text
UiError.Network
UiError.Unauthorized
UiError.Forbidden
UiError.NotFound
UiError.Conflict
UiError.RateLimited
UiError.Server
UiError.Unknown
```

---

# 34. Error Handling Rules

```text
400
→ validation error

401
→ refresh/login

403
→ permission message

404
→ content unavailable

409
→ refresh current resource / conflict flow

429
→ rate-limit/budget message

500
→ generic retryable error

503
→ service unavailable

network timeout/offline
→ Offline State
```

---

# 35. Snackbar vs Full Error

Snackbar:

```text
minor transient update
```

Full error state:

```text
page cannot load
```

Dialog:

```text
critical confirmation
```

Do not show technical exception messages.

---

# 36. Connectivity Detection

Use Android connectivity APIs to detect:

```text
network available
```

But connectivity detection does not guarantee server reachability.

Actual request failure remains authoritative for request-level availability.

---

# 37. Official Offline Behavior

V1:

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
offline SRS
offline quiz submit
auto-sync
```

---

# 38. Offline Mutation Guard

Before a mutation:

```text
submit learning attempt
submit quiz answer
complete quiz
```

If device is offline:

```text
block
→ show Offline State
```

Still keep backend as final authority.

---

# 39. Read Cache

Optional.

Suitable data:

```text
Goals
CEFR levels
Topics
Vocabulary metadata
Approved reusable content
```

Do not use cache as authority for:

```text
SRS
progress
XP
streak
attempt history
```

---

# 40. Local Storage Options

Recommended:

```text
DataStore / secure storage
```

for preferences/session metadata.

Optional:

```text
Room
```

for read-only cache.

Room is not used as an offline synchronization database in V1.

---

# 41. User Preferences

Examples:

```text
selected language
theme if added
cached onboarding completion flag
last selected app preferences
```

Server-owned fields such as goals/daily minutes should sync through API.

---

# 42. UUID Generation

Android generates:

```text
eventId = UUID.randomUUID()
```

for a new logical mutation.

Use same event ID for retry.

---

# 43. Event ID Lifecycle

```text
User selects Hard
 ↓
create eventId A
 ↓
send request
 ↓
timeout
 ↓
retry with eventId A
```

Do not generate event B for the same logical action.

---

# 44. Idempotency Client Helper

Recommended:

```text
EventIdFactory
PendingOperation
```

Example abstraction:

```java
UUID createEventId();
```

For retryable mutation:

```text
logical operation object
→ owns eventId
→ owns request payload
→ can retry
```

---

# 45. Learning Attempt Client Flow

```text
Flashcard
 ↓
quality selected
 ↓
create eventId
 ↓
build request
 ↓
submit
 ↓
success
    → show SRS feedback

timeout
    → retry same eventId

409 idempotency reuse
    → treat as conflict/error
```

---

# 46. Do Not Generate `isCorrect`

The client sends:

```text
answerQuality
```

The backend derives:

```text
isCorrect
```

Client must not calculate/submit:

```text
isCorrect
```

---

# 47. Learning Session Flow

```text
Home / Review / Learn
 ↓
Start session
 ↓
receive sessionId
 ↓
load content
 ↓
attempt(s)
 ↓
complete session
 ↓
result
```

---

# 48. Session ID

Server creates:

```text
sessionId
```

Client keeps it for the logical session.

Do not generate a random replacement session ID on every navigation.

---

# 49. Flashcard Screen Flow

```text
Front
 ↓
Reveal
 ↓
Back
 ↓
Quality
 ↓
Submit
 ↓
SRS Feedback
 ↓
Next Card
```

---

# 50. Flashcard State

```text
FRONT
BACK
SUBMITTING
FEEDBACK
COMPLETED
ERROR
OFFLINE
```

The UI should not depend on multiple independent booleans.

---

# 51. Flashcard Data

Required:

```text
vocabularyId
word
phoneticIpa
meaningVi
meaningEn
partOfSpeech
cefr
topic
example
translation
audioUrl
```

---

# 52. Audio

Use Android media APIs.

Flow:

```text
Tap audio
 ↓
check cached audio
 ↓
play
```

If audio unavailable:

```text
show subtle error
do not block text learning
```

---

# 53. Audio Caching

Audio can be cached because:

```text
audioUrl
```

is stable content.

Cache does not imply offline learning.

---

# 54. Image Loading

Use an image loading library if adopted by project.

Rules:

```text
placeholder
error image
memory/cache
```

Do not block the entire screen if image fails.

---

# 55. Daily Plan Home

API:

```text
GET /api/v1/learning/today
```

Display:

```text
new words
review
quiz
estimated minutes
progress
```

---

# 56. Home ViewModel

Responsibilities:

```text
loadTodayPlan()
loadProgressSummary()
loadHomeRecommendations()
handleContinueLearning()
```

Do not independently calculate:

```text
daily target
review priority
recommendation
```

---

# 57. Continue Learning Logic

```text
Today Plan exists
        ↓
has current/incomplete item?
   ┌────┴────┐
  YES       NO
   │         │
 Resume    Start first
```

If no plan:

```text
request/create plan as supported by API
```

---

# 58. Home Secondary Actions

```text
Start Review
→ Review

Learn New Words
→ New Words activity

Practice Now
→ Personalized Exercise

Progress
→ Progress
```

---

# 59. Daily Plan vs Review

Home:

```text
18 planned today
```

Review tab:

```text
32 currently due
```

Do not reuse labels that make these counts appear identical.

---

# 60. Review Screen

API:

```text
GET /api/v1/learning/reviews
```

Features:

```text
filters
pagination if API supports it
priority indicators
start review
```

---

# 61. Review Filters

```text
All
High Risk
Weak
```

Filter changes:

```text
update ViewModel state
request/derive list based on server response
```

Do not duplicate complex priority logic on client.

---

# 62. Review Priority Display

Show:

```text
High priority
Weak
Accuracy
Missed recently
```

Reason text comes from backend where available.

---

# 63. New Words Activity

New words must use:

```text
dailyPlan.newWordsTarget
```

not an arbitrary client-defined count.

---

# 64. Personalized Exercise

Entry:

```text
Home
Weak Words
Progress
```

Flow:

```text
tap Practice
 ↓
API request
 ↓
loading
 ↓
generated exercise
 ↓
question
 ↓
answer
 ↓
feedback
```

---

# 65. Personalized Exercise API

Recommended:

```text
POST /api/v1/learning/personalized-exercise
```

Client sends only necessary context allowed by API.

Do not send internal:

```text
weaknessScore
forgettingRisk
SRS internals
```

unless API explicitly requires them.

---

# 66. Personalized Exercise UI State

```text
REQUESTING
READY
ANSWERED
COMPLETED
ERROR
OFFLINE
```

---

# 67. Personalized Exercise AI Failure

If AI is unavailable:

```text
Personalized practice isn't available right now.

[Try Standard Practice]
```

Client may route to:

```text
standard weak-word practice
```

if endpoint exists.

---

# 68. Quiz Architecture

Quiz consists of:

```text
Quiz
QuizQuestion
QuizAttempt
QuizAttemptAnswer
```

Client state:

```text
quizId
attemptId
questionIndex
selectedAnswer
score/result
```

---

# 69. Quiz Start

```text
Select quiz
 ↓
start attempt
 ↓
attemptId
 ↓
load questions
```

---

# 70. Quiz Answer

Request includes:

```text
questionId
userAnswer
responseTimeMs
eventId
```

Server determines:

```text
isCorrect
```

Client must not trust a locally calculated correctness value.

---

# 71. Quiz Answer Idempotency

A logical answer submission:

```text
question Q
attempt A
eventId E
```

Retry:

```text
same E
```

not a new event ID.

---

# 72. Quiz Completion

Request:

```json
{
  "eventId": "uuid"
}
```

Retry with same event ID.

---

# 73. Quiz Result

Display:

```text
score
correct count
total
weak words where available
```

Server is authoritative for score.

---

# 74. Assessment Architecture

Placement uses:

```text
Assessment
Assessment question
answer
result
```

Client sends selected answer.

Backend decides scoring/difficulty.

---

# 75. Assessment Adaptivity

Prototype/UX may display:

```text
next question adjusts difficulty
```

Android does not implement an independent adaptive algorithm.

Backend remains authoritative.

---

# 76. Assessment State

```text
INTRO
LOADING
QUESTION
SUBMITTING
RESULT
ERROR
```

---

# 77. Onboarding

Flow:

```text
Welcome
 ↓
Goals
 ↓
Daily Time
 ↓
Placement
 ↓
Assessment Result
 ↓
Home
```

---

# 78. Goals Selection

Client tracks:

```text
selectedGoals
primaryGoal
```

Validation:

```text
≥1 goal
at most 1 primary goal
```

Server validates again.

---

# 79. Daily Time Selection

Allowed values come from product config/API contract.

Example:

```text
5
10
20
30
45
60
```

Send selected value to backend.

---

# 80. Placement Result

Display:

```text
CEFR
score
correct count
short recommendation
```

Do not calculate CEFR on client.

---

# 81. Progress

Endpoint group:

```text
GET /api/v1/progress/*
```

Display:

```text
words learned
mastered
accuracy
learning time
weak words
history
```

---

# 82. Progress ViewModel

```text
loadSummary()
loadTrend()
loadWeakWords()
loadHistory()
```

Use separate loading states when sections load independently.

---

# 83. Weak Words

Endpoint:

```text
GET /api/v1/progress/weak-words
```

Display:

```text
word
accuracy
priority
reason
practice action
```

---

# 84. Weak Word Practice

```text
Tap Practice
 ↓
Flashcard or personalized exercise
```

Keep navigation consistent.

---

# 85. Gamification

Display:

```text
XP
Level
Streak
Badges
```

Do not calculate:

```text
XP
streak
badge eligibility
```

on client.

---

# 86. Profile

Sections:

```text
Account
Goals
Learning Settings
Notifications
Logout
```

---

# 87. Profile Data

Server:

```text
name
email
role
CEFR
timezone
dailyLearningMinutes
goals
```

Client renders.

---

# 88. Goal Settings

Flow:

```text
Load
 ↓
Edit
 ↓
Validate
 ↓
Save
 ↓
Success
```

If primary goal changed:

```text
server re-personalizes future plan
```

Client does not recalculate recommendation.

---

# 89. Learning Settings

Fields:

```text
dailyLearningMinutes
timezone
```

If preferred study time is added later:

```text
requires API/schema support
```

Do not create client-only fake persistence.

---

# 90. Notifications

Use:

```text
Firebase Cloud Messaging
```

Client responsibilities:

```text
register token
receive notification
handle tap
navigate
```

Backend responsibilities:

```text
schedule
send
retry
track
```

---

# 91. FCM Token Registration

Flow:

```text
App authenticated
 ↓
get FCM token
 ↓
POST token to backend
```

If token refreshes:

```text
re-register
```

---

# 92. Notification Deep Link

Examples:

```text
Review notification
→ Review

Personalized practice notification
→ Personalized Exercise
```

If deep link content is unavailable:

```text
Home
```

---

# 93. Authentication UI

Screens:

```text
Login
Register
Forgot Password
```

---

# 94. Login Validation

Client:

```text
email format
required
password required
```

Server:

```text
final authentication authority
```

---

# 95. Register Validation

Client:

```text
name
email
password
confirm password
```

Server validates everything again.

---

# 96. Brute-Force UI

When backend returns temporary lock:

```text
Too many login attempts.

Please try again later.
```

Do not expose internal lock implementation details.

---

# 97. Session Expired UI

```text
Your session has expired.

[Sign In]
```

If safe:

```text
save current non-sensitive screen state
```

but do not persist passwords or tokens unsafely.

---

# 98. Splash

Responsibilities:

```text
initialize app
restore auth state
check onboarding state
navigate
```

No heavy network business logic beyond necessary session/bootstrap checks.

---

# 99. Onboarding Completion Flag

May cache a local hint:

```text
onboardingCompleted = true
```

but server remains authoritative if the API exposes onboarding state.

---

# 100. Home Bootstrap

Recommended:

```text
parallel or coordinated requests
```

for:

```text
Today's Plan
Progress Summary
User Profile
```

Avoid serial requests when independent.

---

# 101. Request Cancellation

ViewModel should avoid updating detached screens.

Use lifecycle-aware collection/observation.

---

# 102. Pagination

Where endpoint is paginated:

```text
page
size
totalElements
hasNext
```

UI:

```text
load initial page
load next page
```

No client-side fake pagination for server-paginated endpoints.

---

# 103. Pagination State

```text
INITIAL
LOADING_FIRST
LOADED
LOADING_NEXT
END_REACHED
ERROR_FIRST
ERROR_NEXT
```

Retry next page without discarding loaded content.

---

# 104. Search

Vocabulary search:

```text
debounced input
cancel previous request
show loading
empty state
```

Do not call API on every keystroke without debouncing.

---

# 105. Search Debounce

Recommended:

```text
~250–400ms
```

Exact value can be tuned through usability/performance testing.

---

# 106. RecyclerView

Use for:

```text
review list
vocabulary list
weak words
quiz options where dynamic
history
notifications
```

Use stable item IDs where useful.

---

# 107. Adapter Rules

Adapter should:

```text
bind UI
emit click callback
```

Adapter must not:

```text
call repository
call Retrofit
calculate business rules
```

---

# 108. Reusable UI Components

Required:

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

# 109. Design Token Usage

Centralize:

```text
colors.xml
dimens.xml
styles/themes
drawables
strings.xml
```

Do not scatter hard-coded:

```text
colors
padding
text sizes
corner radii
```

through layouts.

---

# 110. String Resources

All user-visible text goes through:

```text
res/values/strings.xml
```

Supports later localization.

Do not hard-code learner-facing strings in Java.

---

# 111. Accessibility Labels

For icon buttons:

```text
contentDescription
```

Examples:

```text
Listen to pronunciation
Open profile
Retry
Go back
```

---

# 112. Touch Targets

Minimum intended target:

```text
44–48dp
```

Do not create tiny quality-selector buttons.

---

# 113. Dynamic Text

Layouts must tolerate:

```text
long Vietnamese translations
long English words
IPA
```

Avoid fixed-width text that truncates critical vocabulary.

---

# 114. Font / Typography

Typography should match design system.

Important:

```text
word
IPA
meaning
example
```

Word should be dominant on flashcards.

---

# 115. Localization Readiness

At minimum support:

```text
English UI
Vietnamese content
```

Structure strings for future localization.

Do not concatenate sentences in Java.

Bad:

```java
"Missed " + count + " times"
```

Prefer resource/plural formatting.

---

# 116. Theme

V1 default:

```text
Light
```

Structure code so dark theme can be added later without rewriting features.

---

# 117. Animation

Flashcard:

```text
250–350ms
```

Navigation:

```text
150–250ms
```

Progress:

```text
300–500ms
```

Animations should not block interaction.

---

# 118. UI Performance

Avoid:

```text
nested heavy layouts
unnecessary redraws
large images without resizing
full-screen spinners for small updates
```

Prefer:

```text
skeletons
incremental rendering
image caching
pagination
```

---

# 119. Configuration Change

ViewModel retains:

```text
screen state
```

for:

```text
rotation/configuration
```

Do not store complex screen state only in Activity fields.

---

# 120. Process Death

For critical state:

```text
learning session ID
current question index if necessary
```

may be recreated, but V1 does not require full offline session restoration.

Server state remains authoritative.

---

# 121. Learning Session Interruption

If app leaves foreground:

```text
pause UI timer
```

Do not assume server session remains active forever.

On resume:

```text
refresh state if needed
```

---

# 122. Response Time Measurement

For learning attempt:

```text
responseTimeMs
```

should be measured on client from the relevant interaction start point.

However:

```text
SRS/business interpretation
```

remains backend-owned.

---

# 123. Response Timer

Do not let background time inflate response time unintentionally.

Example:

```text
Question displayed
→ timer starts

App backgrounded
→ timer pause if product semantics require

Return
→ timer resumes
```

The exact semantics must match the backend contract.

---

# 124. Learning Attempt Request Builder

Use one builder/factory to ensure:

```text
sessionId
vocabularyId
attemptType
responseTimeMs
answerQuality
eventId
```

are always populated correctly.

---

# 125. Event ID Storage for Retry

If a network timeout happens before response is known:

```text
retain logical operation + eventId
```

long enough to retry safely.

Do not lose eventId between a transient retry inside the same operation.

A permanent app restart/offline queue is outside V1 scope.

---

# 126. Idempotency 409 Handling

If backend returns:

```text
IDEMPOTENCY_KEY_REUSE
```

client should:

```text
stop retrying with a different payload
refresh state
show controlled conflict message
```

Do not automatically generate a new event ID to bypass the conflict.

---

# 127. Optimistic Locking Conflict

If backend returns:

```text
409
code = CONCURRENT_UPDATE
```

client should:

```text
refresh latest resource
```

and allow a new logical action.

Do not silently overwrite.

The literal error `code` the client must match on is `CONCURRENT_UPDATE` (see API Specification v1.4, § 15) — "Optimistic Locking Conflict" is only the conceptual name of this scenario, not the string returned by the backend.

---

# 128. Conflict vs Idempotency

These are different:

```text
409 IDEMPOTENCY_KEY_REUSE
→ same eventId reused incorrectly

409 CONCURRENT_UPDATE
→ server state changed (optimistic lock conflict)
```

Map them to different UI behaviors.

---

# 129. API Retry Policy

Retry automatically only safe cases:

```text
GET
temporary network failure
selected 503
```

For mutation:

```text
retry only using same eventId
```

Do not blindly retry every POST.

---

# 130. Timeout Policy

Set finite:

```text
connect timeout
read timeout
write timeout
```

External AI responses may need longer timeout than standard APIs.

Android should show progress for long-running AI generation.

---

# 131. AI Loading UX

Example:

```text
Creating practice for you...
```

Avoid:

```text
AI inference
LLM processing
```

in learner UI.

---

# 132. AI Result Caching

Client may cache already received personalized result only for immediate navigation/session UX.

Do not treat it as authoritative persisted learning content.

---

# 133. AI Privacy

Do not store unnecessary:

```text
raw prompts
private user learning history
server AI keys
```

in client storage.

---

# 134. Analytics

Optional client analytics can track:

```text
screen view
button tap
session started
session completed
```

Never send sensitive data unnecessarily.

Product analytics should not replace backend learning history.

---

# 135. Crash Reporting

Recommended:

```text
Crash reporting tool
```

if permitted by project.

Do not include:

```text
JWT
refresh token
password
private API payload
```

---

# 136. Logging

Debug only:

```text
request URL without secrets
status code
duration
```

Production:

```text
minimal
```

Never log:

```text
Authorization
refresh token
password
API keys
```

---

# 137. Network Security

Production:

```text
HTTPS only
```

Do not disable TLS certificate validation.

Avoid insecure cleartext production traffic.

---

# 138. Certificate Pinning

Not required for MVP unless infrastructure/security policy demands it.

Code should not prevent adding it later.

---

# 139. Permissions

Only request permissions necessary for V1.

Typical:

```text
POST_NOTIFICATIONS
```

where required by Android version.

No microphone permission because Speaking is not V1.

No storage permission if modern media/cache APIs avoid it.

---

# 140. Notification Permission

Ask at an appropriate moment:

```text
after onboarding
or
when notifications value is understood
```

Do not ask immediately at first launch without context.

---

# 141. App Lifecycle

Handle:

```text
foreground
background
resume
process recreation
```

Learning UI should refresh state if server-sensitive data may have changed.

---

# 142. Background Work

V1 only needs background work for:

```text
FCM delivery is server-driven
small local maintenance tasks
```

Do not implement offline sync.

---

# 143. WorkManager

May be used for:

```text
non-learning local maintenance
token/device registration retry where appropriate
```

Not for:

```text
offline learning synchronization
SRS reconciliation
```

---

# 144. Room Use Case

Optional Room is only for:

```text
read cache
```

Example:

```text
VocabularyEntity
TopicEntity
```

Not for:

```text
pending session_attempts
offline SRS state
sync conflict queue
```

---

# 145. Local Cache Invalidation

When server content changes:

```text
fetch fresh
replace cache
```

Use TTL/versioning as needed.

---

# 146. Local Cache Failure

If cache cannot be opened:

```text
do not crash app
```

Fallback:

```text
network
```

where available.

---

# 147. App Startup Performance

Target:

```text
fast splash
defer non-critical initialization
```

Do not load:

```text
entire vocabulary database
```

at startup.

---

# 148. API Pre-fetching

Useful:

```text
preload next flashcard vocabulary
```

within the current online session.

Do not prefetch huge datasets.

---

# 149. Learning Session Prefetch

Possible:

```text
current item
+
next 1–2 items
```

This improves perceived latency.

Server remains authoritative for eligibility.

---

# 150. Navigation During Prefetch

Prefetch failure must not break current item.

```text
current loaded
next failed
→ continue current
→ retry next
```

---

# 151. Home Prefetch

After Home loads:

```text
preload first Daily Plan learning item
```

only if useful.

---

# 152. Memory Management

Avoid retaining:

```text
large image bitmaps
entire quiz banks
entire vocabulary lists
```

Use pagination and cache limits.

---

# 153. Network Data Model

All API responses should be parsed into DTOs.

Malformed response:

```text
ApiParseError
→ controlled failure
```

Do not crash due to nullable/missing optional fields.

---

# 154. Nullable Fields

Expect nullable backend fields for:

```text
audioUrl
imageUrl
translation
reviewNote in admin-only API
```

Mobile learner models should not assume optional content exists.

---

# 155. Date/Time Parsing

Backend timestamps use:

```text
ISO-8601
```

Parse with a centralized utility.

Store/handle:

```text
Instant / OffsetDateTime equivalent
```

and convert for display.

---

# 156. Timezone

User profile contains:

```text
timezone
```

Client should send/receive user timezone where API requires it.

Example:

```text
Asia/Ho_Chi_Minh
```

Do not assume:

```text
server timezone
```

equals device timezone.

---

# 157. Local Date Display

Daily Plan:

```text
today
```

is based on user timezone/server semantics.

Do not calculate "today's plan" using device local date independently when server response already defines it.

---

# 158. Quiz Timing

Use server/backend semantics for:

```text
attempt
completion
score
```

Client timer is UX aid.

---

# 159. Progress Refresh

After learning mutation:

```text
use returned server result
```

Optionally:

```text
refresh Home/Progress
```

Do not manually increment all metrics and assume correctness.

---

# 160. UI Optimistic Updates

Allowed for:

```text
small non-critical visual effects
```

Be cautious for:

```text
SRS
XP
streak
quiz score
```

Those should use server response.

---

# 161. Home Progress Update

After completed learning:

```text
refresh relevant server data
```

rather than:

```text
client += 1
```

as final state.

---

# 162. Error Recovery Matrix

| Scenario | UI |
|---|---|
| Initial network failure | Error + Retry |
| Offline before mutation | Offline state |
| Timeout mutation | Retry same eventId |
| 401 | Refresh/session |
| 409 idempotency | Conflict + refresh |
| 409 optimistic lock | Refresh state |
| 429 AI | Controlled retry/fallback |
| 503 | Retry |
| Parse failure | Generic error |
| Missing optional media | Continue text flow |

---

# 163. Screen-to-API Mapping

| Screen | Primary API |
|---|---|
| Splash | Auth/session/bootstrap |
| Login | `POST /auth/login` |
| Register | `POST /auth/register` |
| Forgot Password | configured auth endpoint |
| Goals | `GET /goals`, `PUT /users/me/goals` |
| Daily Time | `PUT /users/me/profile` |
| Placement | `/assessments/*` |
| Home | `GET /learning/today`, progress |
| Learn | `/learning/*`, `/vocabulary`, `/quizzes` |
| Review | `GET /learning/reviews` |
| Vocabulary Search | `GET /vocabulary` |
| Flashcard | vocabulary + `POST /learning/attempts` |
| Word Detail | vocabulary/progress APIs |
| Personalized Exercise | `POST /learning/personalized-exercise` |
| Quiz | quiz/quiz-attempt APIs |
| Learning Result | session/result APIs |
| Progress | `/progress/*` |
| Weak Words | `GET /progress/weak-words` |
| Gamification | `/gamification/*` |
| Profile | `/users/me/*` |
| Notifications | `/notifications` |

---

# 164. Screen-to-UseCase Mapping

| Screen | Use Cases |
|---|---|
| Login | `LoginUseCase` |
| Register | `RegisterUseCase` |
| Goals | `UpdateGoalsUseCase` |
| Placement | `SubmitAssessmentAnswerUseCase` |
| Home | `GetTodayPlanUseCase`, `GetProgressSummaryUseCase` |
| Review | `GetDueReviewsUseCase` |
| Flashcard | `GetVocabularyUseCase`, `SubmitLearningAttemptUseCase` |
| Personalized | `GeneratePersonalizedExerciseUseCase` |
| Quiz | `StartQuizAttemptUseCase`, `SubmitQuizAnswerUseCase`, `CompleteQuizUseCase` |
| Progress | `GetProgressUseCase`, `GetWeakWordsUseCase` |
| Profile | `GetProfileUseCase`, `UpdateProfileUseCase` |

---

# 165. UI-to-Backend Ownership

```text
UI
→ presentation

ViewModel
→ screen state

UseCase
→ app action

Repository
→ data abstraction

Backend
→ business truth
```

---

# 166. Authentication Module

Classes:

```text
LoginFragment
RegisterFragment
ForgotPasswordFragment
AuthViewModel
LoginUseCase
RegisterUseCase
AuthRepository
AuthApi
TokenManager
```

---

# 167. Home Module

Classes:

```text
HomeFragment
HomeViewModel
HomeUiState
HomeAdapter/components if needed
GetTodayPlanUseCase
GetProgressSummaryUseCase
LearningRepository
ProgressRepository
```

---

# 168. Review Module

```text
ReviewFragment
ReviewViewModel
ReviewUiState
ReviewAdapter
GetDueReviewsUseCase
StartLearningSessionUseCase
```

---

# 169. Flashcard Module

```text
FlashcardFragment
FlashcardViewModel
FlashcardUiState
SubmitLearningAttemptUseCase
VocabularyRepository
LearningRepository
AudioPlayer
```

---

# 170. Personalized Module

```text
PersonalizedExerciseFragment
PersonalizedExerciseViewModel
PersonalizedExerciseUiState
GeneratePersonalizedExerciseUseCase
SubmitPersonalizedAnswerUseCase
```

---

# 171. Quiz Module

```text
QuizListFragment
QuizFragment
QuizResultFragment

QuizViewModel
QuizListViewModel

StartQuizAttemptUseCase
SubmitQuizAnswerUseCase
CompleteQuizUseCase
```

---

# 172. Progress Module

```text
ProgressFragment
WeakWordsFragment
LearningHistoryFragment

ProgressViewModel
WeakWordsViewModel

GetProgressUseCase
GetWeakWordsUseCase
GetLearningHistoryUseCase
```

---

# 173. Profile Module

```text
ProfileFragment
GoalsFragment
LearningSettingsFragment

ProfileViewModel
GoalsViewModel
LearningSettingsViewModel
```

---

# 174. Navigation Graph

Conceptual:

```text
nav_graph.xml

splash
 ↓
auth
 ↓
onboarding
 ↓
main
 ├── home
 ├── learn
 ├── review
 ├── progress
 └── profile
```

Nested learning screens:

```text
review
 ↓
flashcard

learn
 ↓
personalized
or
quiz
```

---

# 175. UI State Example — Home

```text
HomeUiState
├── Loading
├── Content
│   ├── user
│   ├── dailyPlan
│   ├── progress
│   ├── reviewSummary
│   ├── newWordsSummary
│   └── personalizedPractice
├── Empty
├── Error
└── Offline
```

---

# 176. UI State Example — Flashcard

```text
FlashcardUiState
├── Loading
├── Front
├── Back
├── Submitting
├── Feedback
├── Completed
├── Error
└── Offline
```

---

# 177. UI State Example — Quiz

```text
QuizUiState
├── Loading
├── Question
├── AnswerSubmitting
├── AnswerFeedback
├── Completing
├── Result
├── Error
└── Offline
```

---

# 178. UI State Example — Personalized Exercise

```text
PersonalizedExerciseUiState
├── Generating
├── Ready
├── AnswerSubmitting
├── Feedback
├── Completed
├── Fallback
├── Error
└── Offline
```

---

# 179. Form Validation Utility

Centralize simple validations:

```text
EmailValidator
PasswordValidator
RequiredFieldValidator
GoalSelectionValidator
```

Domain rules remain backend-owned.

---

# 180. Input Validation Strategy

```text
Client validation
→ immediate UX

Server validation
→ authoritative
```

Never rely only on client validation.

---

# 181. API Client Architecture

```text
Retrofit Service
       ↓
RemoteDataSource
       ↓
Repository
       ↓
UseCase
       ↓
ViewModel
       ↓
Fragment
```

---

# 182. HTTP Client Configuration

```text
connect timeout
read timeout
write timeout
interceptors
auth handler
```

Debug logging only.

---

# 183. API Error Parsing

Backend error:

```json
{
  "status": 409,
  "code": "IDEMPOTENCY_KEY_REUSE",
  "message": "The eventId was already used for a different logical request."
}
```

Map to:

```text
ApiError
```

then:

```text
UiError.Conflict
```

---

# 184. Repository Result Type

Recommended:

```text
Success<T>
Failure<Ui-independent error>
```

Do not pass Android `Context` or UI classes into repository layer.

---

# 185. Coroutines

Because Android V1 is Java, use whichever asynchronous mechanism the project standardizes on.

Recommended architectural requirement:

```text
asynchronous API calls
+
lifecycle-safe observation
```

The specific Java-compatible library choice can be standardized during project setup.

Do not block the main thread.

---

# 186. Main Thread Rule

Never perform directly on UI thread:

```text
network
database
large JSON parsing
large file operations
```

---

# 187. Database Rule

If Room is used:

```text
DAO
→ repository
```

Never:

```text
Fragment
→ DAO
```

---

# 188. Dependency Injection

Recommended:

```text
Hilt/Dagger
```

or a lightweight project-approved DI approach.

DI should provide:

```text
Retrofit
OkHttp
Repositories
UseCases
ViewModels
TokenManager
```

---

# 189. Dependency Graph

```text
Application
 ↓
NetworkModule
 ↓
Api
 ↓
RemoteDataSource
 ↓
Repository
 ↓
UseCase
 ↓
ViewModel
```

---

# 190. Build Types

```text
debug
release
```

Optional:

```text
staging
```

Configurable:

```text
base URL
logging
feature flags
analytics
```

---

# 191. Secure Configuration

Do not embed:

```text
LLM API key
server DB password
private secrets
```

Mobile should never call LLM provider directly.

---

# 192. AI Architecture on Mobile

Mobile:

```text
POST /api/v1/learning/personalized-exercise
```

Backend:

```text
Budget Guard
→ LLM
→ validation
→ safety
```

Mobile does not contain:

```text
LLM API key
prompt business logic
budget calculation
```

---

# 193. Admin Separation

Mobile app must never expose:

```text
/admin/**
```

administrative functionality.

Admin Web has its own client.

---

# 194. Feature Flags on Mobile

Optional:

```text
personalizedExerciseEnabled
quizEnabled
notificationsEnabled
```

Use backend/remote configuration where appropriate.

Do not use feature flags to bypass security.

---

# 195. Crash-Safe Navigation

Avoid:

```text
navigate twice
after rapid taps
```

Disable or guard actions during:

```text
SUBMITTING
```

---

# 196. Double-Tap Protection

Buttons that trigger mutations:

```text
Login
Submit Attempt
Submit Quiz Answer
Complete Quiz
Save Settings
```

must prevent duplicate UI dispatch during submitting.

Idempotency still protects backend retries.

---

# 197. Flashcard Quality Selector

Six levels:

```text
Forgot
Hard
Difficult
Okay
Easy
Very Easy
```

Internal:

```text
0–5
```

Do not send:

```text
isCorrect
```

---

# 198. Quality Selector UI

Two rows preferred:

```text
[Forgot] [Hard] [Difficult]

[Okay] [Easy] [Very Easy]
```

All targets remain comfortably tappable.

---

# 199. SRS Feedback

Use server result:

```text
Next review
in 17 days
```

Client does not recompute interval.

---

# 200. Daily Plan Completion

When all planned actions complete:

```text
Learning Result
```

Use server result for:

```text
accuracy
words
time
```

---

# 201. Progress Refresh Trigger

After meaningful mutation:

```text
learning attempt
quiz completion
session completion
```

refresh:

```text
Home
Progress
Review count
```

where needed.

---

# 202. Notification Refresh

On app resume:

```text
refresh notification-related state
```

if required by API.

---

# 203. Accessibility Testing

Test:

```text
TalkBack
font scaling
touch targets
contrast
focus order
```

---

# 204. Automated UI Tests

P0:

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
state changes
error states
offline state
```

---

# 205. Android Unit Tests

High-priority:

```text
ViewModel
UseCase
Validators
EventId behavior
Repository mapping
```

---

# 206. Learning Attempt Unit Tests

```text
quality 0
quality 1
quality 2
quality 3
quality 4
quality 5
```

Client only checks allowed range; server owns correctness/SRS.

---

# 207. Idempotency Client Tests

Test:

```text
create eventId
retry keeps same ID
new logical attempt gets new ID
```

Example:

```text
Attempt A
→ eventId X

Retry A
→ eventId X

Attempt B
→ eventId Y
```

---

# 208. Auth Tests

```text
login success
login failure
refresh
refresh failure
logout
session expired
```

---

# 209. Offline Tests

```text
open cached vocabulary
turn off network
try learning mutation
→ blocked

restore network
retry
→ success
```

---

# 210. API Contract Tests

Verify client DTOs against:

```text
OpenAPI v1.4
```

Important:

```text
field names
nullable fields
status codes
pagination
error schema
eventId
```

---

# 211. Regression Tests

After backend API change:

```text
run client contract tests
run critical UI tests
```

---

# 212. Performance Tests

Measure:

```text
startup
Home load
Review load
Flashcard transition
Quiz response
AI generation wait
```

---

# 213. Memory Tests

Check:

```text
long Review list
Vocabulary images
Quiz session
repeated navigation
```

for memory leaks.

---

# 214. Security Tests

Verify:

```text
no token logs
no password logs
secure token storage
no API secrets
no direct LLM calls
```

---

# 215. Android Manifest Baseline

Declare only necessary permissions.

Expected:

```text
INTERNET
POST_NOTIFICATIONS where required
```

No microphone for V1.

---

# 216. Network Security Configuration

Production:

```text
HTTPS
```

Debug may use local development configuration as necessary.

Do not ship insecure network configuration in release.

---

# 217. App Package Naming

Recommended:

```text
com.example.englishaicoach
```

Use real organization/package naming before release.

---

# 218. App Resource Organization

```text
res/
├── drawable/
├── drawable-night/
├── layout/
├── menu/
├── navigation/
├── values/
├── values-night/
└── xml/
```

Keep resources feature-neutral where they are shared.

---

# 219. Naming Conventions

Java:

```text
PascalCase classes
camelCase fields/methods
```

Layouts:

```text
fragment_home.xml
fragment_review.xml
item_vocabulary.xml
dialog_reject.xml
```

Resources:

```text
ic_volume
bg_card
shape_primary_button
```

---

# 220. Java Code Style

Prefer:

```text
final where useful
small methods
constructor injection
explicit null handling
```

Avoid:

```text
god classes
static mutable state
global Context
```

---

# 221. Global State

Avoid global mutable singleton data.

Allowed shared services:

```text
TokenManager
SessionManager
```

with controlled lifecycle.

---

# 222. Context Handling

Do not store Activity context in long-lived objects.

Prefer:

```text
Application context
```

only when necessary.

---

# 223. Resource Leak Prevention

Release:

```text
audio player
listeners
callbacks
```

according to lifecycle.

---

# 224. Audio Lifecycle

When leaving Flashcard:

```text
stop audio
release resources
```

Do not continue playback unexpectedly.

---

# 225. Dialog Rules

Use dialogs for:

```text
exit session
logout
critical confirmation
```

Do not use dialogs for every validation message.

---

# 226. Toast/Snackbar Rules

Success:

```text
saved successfully
```

Transient:

```text
network restored
```

Persistent errors need:

```text
full state + retry
```

---

# 227. Analytics Event Naming

If analytics is added:

```text
auth_login_success
assessment_completed
daily_plan_started
learning_attempt_submitted
learning_session_completed
personalized_exercise_started
quiz_completed
```

Do not include:

```text
raw email
token
password
private content
```

unless explicitly needed and privacy-reviewed.

---

# 228. Release Logging

Release build:

```text
debug logs disabled
network body logs disabled
```

---

# 229. App Update Compatibility

API changes:

```text
additive preferred
breaking changes require coordinated versioning
```

Client should tolerate additional unknown JSON fields.

---

# 230. API Backward Compatibility

Jackson/API response may add:

```text
new optional fields
```

without breaking old clients.

Android parsing should ignore unknown fields.

---

# 231. Configuration Separation

```text
local
staging
production
```

Use build config for:

```text
BASE_URL
debug logging
feature flags
```

Do not use source-code conditionals scattered across features.

---

# 232. Development Workflow

```text
Design
 ↓
Create UI
 ↓
Create UiState
 ↓
Create ViewModel
 ↓
Create UseCase
 ↓
Create Repository
 ↓
Create API
 ↓
Test
 ↓
Integrate
```

---

# 233. Feature Development Template

For every feature:

```text
1. Read API contract
2. Define DTO
3. Define domain model
4. Define repository method
5. Define use case
6. Define UiState
7. Build screen
8. Add loading/error/offline
9. Add tests
10. Integrate
```

---

# 234. First Android Sprint

```text
App shell
Navigation
Theme
Shared components
Networking
Token storage
Auth
```

---

# 235. Second Android Sprint

```text
Onboarding
Goals
Daily Time
Assessment
Assessment Result
```

---

# 236. Third Android Sprint

```text
Home
Learn
Review
Flashcard
Learning Attempt
SRS feedback
```

---

# 237. Fourth Android Sprint

```text
Progress
Weak Words
Learning History
Gamification
Profile
```

---

# 238. Fifth Android Sprint

```text
Quiz
Personalized Exercise
Notifications
```

---

# 239. Android MVP Priority

## P0

```text
Login
Register
Onboarding
Assessment
Home
Review
Flashcard
Learning Result
Progress
Profile
```

## P1

```text
Quiz
Vocabulary Search
Weak Words
Gamification
Notifications
Personalized Exercise
```

## P2

```text
Advanced AI
Speaking
Writing
Listening
Reading
```

This follows the existing mobile design priority while adapting implementation order around dependencies. The original UX baseline identifies Login/Register/Onboarding/Assessment/Home/Flashcard/Review/Learning Result/Progress/Profile as P0. 

---

# 240. Backend Integration Order

Android should integrate in this order:

```text
Auth API
 ↓
User/Profile
 ↓
Vocabulary
 ↓
Assessment
 ↓
Learning Session
 ↓
Learning Attempt
 ↓
Review
 ↓
Daily Plan
 ↓
Progress
 ↓
Quiz
 ↓
AI
```

---

# 241. Dependency Reasoning

Example:

```text
Flashcard
depends on Vocabulary + Learning

SRS feedback
depends on Learning Attempt response

Daily Plan
depends on Personalization backend

Personalized Exercise
depends on learning history/weak words
```

Therefore do not build AI-only screens first.

---

# 242. API Mocking Strategy

Before backend is fully ready:

```text
MockApi
```

can return:

```text
static JSON
```

This allows UI implementation before backend completion.

Do not let mock logic become production architecture.

---

# 243. Mock Data

Example:

```text
User:
Alex
A2
Travel

Daily Plan:
7 new
18 review
5 quiz

Review:
32 due
4 high priority

Progress:
320 learned
140 mastered
84.5%
```

---

# 244. Feature Integration Gate

A feature is integration-ready when:

```text
API contract stable
DTO mapping works
loading state works
error state works
offline state works
mutation retry semantics defined
```

---

# 245. Backend Contract Verification

Before each release:

```text
OpenAPI generated
↓
compare client assumptions
↓
integration tests
```

---

# 246. Critical End-to-End Flow

```text
Login
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
AnswerQuality=4
 ↓
eventId=A
 ↓
Backend SRS
 ↓
Next Review
 ↓
Progress
```

Retry:

```text
same eventId=A
→ one logical attempt
```

---

# 247. Critical AI Flow

```text
Home
 ↓
Personalized Practice
 ↓
POST /api/v1/learning/personalized-exercise
 ↓
Loading
 ↓
Question
 ↓
Answer
 ↓
Feedback
 ↓
Result
```

If AI fails:

```text
Fallback
```

---

# 248. Critical Offline Flow

```text
Online
 ↓
open Review
 ↓
network lost
 ↓
tap Start Review
 ↓
Offline State
 ↓
Retry
 ↓
network restored
 ↓
continue
```

No offline submission.

---

# 249. Critical Concurrency Flow

```text
User action
 ↓
eventId A
 ↓
duplicate HTTP due timeout
 ↓
same A
 ↓
backend replay
 ↓
UI receives one logical result
```

Client responsibility is only to preserve event ID for the same logical operation.

---

# 250. Critical UI Freeze

Before release candidate:

```text
[ ] Home hierarchy matches design
[ ] Daily Plan vs Review clear
[ ] Flashcard quality selector usable
[ ] Personalized Exercise recognizable
[ ] Offline state correct
[ ] Loading states match design
[ ] Accessibility checks pass
```

---

# 251. Technical Acceptance Criteria

```text
[ ] No network call on main thread
[ ] JWT attached automatically
[ ] Refresh handles concurrent 401s
[ ] Tokens stored securely
[ ] ViewModels survive configuration change
[ ] Repository hides data source details
[ ] DTOs not exposed to UI
[ ] SRS not calculated on client
[ ] isCorrect not submitted by client
[ ] eventId generated per logical mutation
[ ] retries reuse eventId
[ ] duplicate-submit UI guarded
[ ] 409 conflict handled
[ ] offline mutation blocked
[ ] cache is read-only
[ ] FCM token handled
[ ] user timezone supported
[ ] loading/error/empty/offline states implemented
```

---

# 252. Code Review Checklist

```text
Architecture boundary correct?
Business logic in ViewModel?
Repository used?
Main thread safe?
Null handling?
Error handling?
No secrets?
No duplicate event IDs?
UI state complete?
Accessibility?
```

---

# 253. Pull Request Checklist

```text
[ ] Feature follows package structure
[ ] UI matches High-Fidelity
[ ] API contract matches OpenAPI
[ ] Tests added
[ ] No debug secrets
[ ] No business rules duplicated
[ ] Offline behavior respected
[ ] Idempotency considered for mutations
```

---

# 254. Release Checklist

```text
[ ] Release build works
[ ] Production URL correct
[ ] Debug logging disabled
[ ] HTTPS enabled
[ ] Notifications configured
[ ] Crash reporting configured if used
[ ] Auth flow tested
[ ] Learning flow tested
[ ] SRS result tested
[ ] Idempotency retry tested
[ ] Offline behavior tested
[ ] AI fallback tested
```

---

# 255. Final Android Architecture

```text
                    Android Java V1
                           │
                    Presentation
                           │
                     ViewModel
                           │
                       UseCase
                           │
                     Repository
                           │
                ┌──────────┴──────────┐
                │                     │
             Remote                Local
             API                  Read Cache
                │                     │
                ▼                     ▼
          Spring Boot API       Optional Room
                │
                ▼
             PostgreSQL
```

---

# 256. Final Learning Mutation Architecture

```text
User
 ↓
Flashcard
 ↓
QualitySelector
 ↓
eventId
 ↓
SubmitLearningAttemptUseCase
 ↓
LearningRepository
 ↓
Retrofit
 ↓
Spring Boot
 ↓
Idempotency
 ↓
SRS
 ↓
Progress
 ↓
XP/Streak
 ↓
Response
 ↓
Android Feedback
```

---

# 257. Final AI Architecture

```text
Android
 ↓
PersonalizedExercise API
 ↓
Spring Boot
 ↓
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
Response
 ↓
Android
```

---

# 258. Final Offline Architecture

```text
                    V1
                     │
              ┌──────┴──────┐
              │             │
            ONLINE        OFFLINE
              │             │
              ▼             ▼
         full learning   read cache only
              │             │
              ▼             ▼
          mutations       no mutations
```

---

# 259. Final Security Architecture

```text
Secure Storage
      ↓
Access Token
      ↓
Auth Interceptor
      ↓
HTTPS
      ↓
Spring Security
```

Refresh:

```text
401
 ↓
serialized refresh
 ↓
new access token
 ↓
retry
```

---

# 260. Final Dependency Rules

```text
presentation
→ domain

domain
→ repository interfaces

data
→ domain
→ external libraries

core
→ shared infrastructure

presentation
≠ direct Retrofit
presentation
≠ direct Room
presentation
≠ direct token storage
```

---

# 261. Final Non-Negotiable Rules

```text
1. Backend owns business truth.
2. Client never calculates SRS.
3. Client never sends isCorrect.
4. Client retries mutations only with the same eventId.
5. UI never accesses Retrofit directly.
6. UI never accesses database directly.
7. Offline V1 never queues learning mutations.
8. Mobile never calls LLM providers directly.
9. Secrets never ship in app.
10. 409 conflicts are handled explicitly.
```

---

# 262. Final Development Roadmap

```text
Technical Specification
       ↓
Android Project Setup
       ↓
Core Infrastructure
       ↓
Auth
       ↓
Onboarding
       ↓
Assessment
       ↓
Home
       ↓
Learning
       ↓
Review
       ↓
SRS feedback
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
       ↓
Release Candidate
```

---

# 263. Definition of Done — Android Feature

A feature is complete when:

```text
[ ] UI implemented
[ ] Navigation implemented
[ ] ViewModel implemented
[ ] UseCase implemented
[ ] Repository integrated
[ ] API DTO mapped
[ ] Loading state
[ ] Empty state
[ ] Error state
[ ] Offline state where relevant
[ ] Validation
[ ] Accessibility
[ ] Unit tests
[ ] UI tests where appropriate
[ ] No business logic duplication
```

---

# 264. Definition of Done — Learning Attempt

```text
[ ] answerQuality 0–5 validated
[ ] eventId generated once
[ ] request sent
[ ] retry reuses eventId
[ ] 409 idempotency handled
[ ] 409 concurrency handled
[ ] server result rendered
[ ] SRS feedback shown
[ ] progress refreshed
```

---

# 265. Definition of Done — Auth

```text
[ ] Login
[ ] Register
[ ] Refresh
[ ] Logout
[ ] secure token storage
[ ] concurrent 401 handled
[ ] session expiry handled
[ ] brute-force UI response
```

---

# 266. Definition of Done — AI

```text
[ ] endpoint integrated
[ ] loading state
[ ] fallback
[ ] rate-limit handling
[ ] no client LLM secret
[ ] no prompt business logic in client
```

---

# 267. Final Documentation Baseline

```text
Database Schema             v1.6
System Architecture         v1.3
AI Personalization          v1.3
API Specification           v1.4
OpenAPI / Swagger           v1.4
Technical Specification     v1.2
Backend Technical Spec      v1.3

Mobile UI/UX                v1.1
Design System/Wireframe     v1.2 FULL
High-Fidelity               v1.1
Interactive Prototype       v1.1

Admin UI/UX                 v1.2
Admin Design System         v1.0
Admin High-Fidelity         v1.1
Admin Prototype             v1.1

Android Java Technical Spec v1.1
```

---

# 268. Final Technical Freeze Gate

Android implementation may begin after:

```text
[ ] Backend API contract v1.4 accessible
[ ] OpenAPI v1.4 validated
[ ] Database v1.6 migrated
[ ] Auth contract tested
[ ] Learning Attempt contract tested
[ ] Idempotency contract tested
[ ] Optimistic Locking tested
[ ] SRS unit tests pass
[ ] Home API stable
[ ] Review API stable
[ ] Progress API stable
[ ] AI endpoint stable enough for integration
```

---

# 269. Final Principle

The Android app is a **client of the learning platform**, not a second learning engine.

Correct:

```text
Android
→ asks
→ displays
→ collects input
→ sends
→ renders server result
```

Incorrect:

```text
Android
→ calculates SRS
→ predicts forgetting
→ decides recommendation
→ invents score
→ writes local truth
```

---

# 270. Final Android V1 Implementation Target

```text
                        ENGLISH AI COACH

                              USER
                               │
                               ▼
                        Android Java V1
                               │
                 ┌─────────────┴─────────────┐
                 │                           │
              ONLINE                     OFFLINE
                 │                           │
                 ▼                           ▼
           Full Learning               Read-only Cache
                 │
                 ▼
          Spring Boot API
                 │
        ┌────────┼─────────┐
        ▼        ▼         ▼
      Auth     Learning    AI
        │        │         │
        │        ├── SRS   ├── Budget
        │        ├── Plan  ├── LLM
        │        └── Prog  └── Safety
        │
        ▼
     PostgreSQL
```

---

# 271. Immediate Implementation Tasks

```text
1. Create Android project
2. Configure Gradle/build variants
3. Configure Material theme
4. Create navigation
5. Create core network module
6. Create Retrofit API layer
7. Create TokenManager
8. Create auth interceptor
9. Create secure storage
10. Create shared UiState/Error model
11. Build Login
12. Build Register
13. Build Onboarding
14. Build Assessment
15. Build Home
16. Build Review
17. Build Flashcard
18. Implement eventId/idempotency retry
19. Integrate SRS response
20. Build Progress
```

---

# 272. Final Transition

```text
Android Java Technical Specification v1.1
                  ↓
           Create Android Repo
                  ↓
             App Foundation
                  ↓
                Auth
                  ↓
            Core Learning
                  ↓
                 SRS
                  ↓
            Personalization
                  ↓
                  AI
                  ↓
                Testing
                  ↓
               Release V1
```

**This document is the implementation baseline for Android Java V1.**

---

# Reconciled Android V1 Contract

- Single locale `vi-VN`; all user-facing strings live in `res/values/strings.xml`. No runtime locale switcher.
- Generate/persist a new `eventId` per logical mutation and reuse it on retry. Do not send an idempotency header. Required for assessment start/answer, learning session start/complete, learning attempt, Daily Plan complete, quiz start/answer/complete, and personalized exercise.
- `DailyPlanItem.vocabularyId` is nullable; QUIZ is an aggregate item with target/completed counts. The backend plan snapshot/order is authoritative.
- Personalized exercise route is `POST /api/v1/learning/personalized-exercise`; response is synchronous V1.
- Maintain a persistent per-installation UUID. On FCM token acquisition/refresh call `PUT /api/v1/devices/{installationId}/push-token`; deactivate with DELETE on logout when appropriate. Use GET/PUT `/api/v1/notification-preferences`.
- Client never computes CEFR assessment outcome, SRS, weakness, forgetting risk, recommendation, XP, level, streak, badges, or quiz correctness.
