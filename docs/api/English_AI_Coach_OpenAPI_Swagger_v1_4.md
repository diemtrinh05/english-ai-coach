# OpenAPI / Swagger YAML v1.4 — English AI Coach

**Project:** English AI Coach  
**OpenAPI:** 3.1.0  
**Version:** 1.4  
**Status:** APPROVED BASELINE  
**Date:** 2026-08-31  

**Source specifications**
- `English_AI_Coach_SRS_v1.2.md`
- `English_AI_Coach_Database_Schema_v1.6.md`
- `English_AI_Coach_System_Architecture_v1.3.md`
- `English_AI_Coach_AI_Personalization_Specification_v1.3.md`
- `English_AI_Coach_API_Specification_v1.4.md`

## 1. Contract decisions reflected

```text
- answerQuality is the single correctness input.
- Backend derives isCorrect = answerQuality >= 3.
- Refresh Token expiry/revocation are V1.
- Refresh Token rotation is future improvement.
- Small reference lists return plain arrays.
- Paginated lists use content/page/size/totalElements/totalPages/hasNext.
- learning/reviews is a domain-specific response in V1.
- Reusable AI content requires Admin Review.
- Personalized AI content uses automated validation/safety filtering.
- AI endpoints are protected by rate limiting/budget guard.
- Optimistic locking conflicts use HTTP 409.
- Rejected reusable AI content persists `review_note` and exposes it as `reviewNote`.
- V1 idempotency uses body `eventId` only; no idempotency HTTP header.
- Client-facing AI generation is synchronous in V1.
- Daily Plan items are persisted snapshots; QUIZ items may have null vocabularyId.
- Android FCM device-token and notification-preference endpoints are part of V1.
```

## 2. OpenAPI YAML

```yaml
openapi: 3.1.0
info:
  title: English AI Coach API
  version: 1.4.0
  description: REST API contract for Android Java V1, Flutter V2, and Admin Web.
servers:
- url: https://api.example.com/api/v1
  description: Production
- url: http://localhost:8080/api/v1
  description: Local
security:
- bearerAuth: []
tags:
- name: Auth
- name: Users
- name: Goals
- name: CEFR
- name: Assessments
- name: Topics
- name: Vocabulary
- name: Learning
- name: Progress
- name: Quiz
- name: Gamification
- name: Devices
- name: Notifications
- name: Admin Users
- name: Admin Vocabulary
- name: Admin Topics
- name: Admin Quiz
- name: Admin AI Content
- name: Admin Statistics
- name: Admin Audit
paths:
  /auth/register:
    post:
      tags:
      - Auth
      security: []
      summary: Register a local account
      operationId: register
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/RegisterRequest'
      responses:
        '201':
          description: Created
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/AuthResponse'
        '400':
          $ref: '#/components/responses/ValidationError'
        '409':
          $ref: '#/components/responses/Conflict'
  /auth/login:
    post:
      tags:
      - Auth
      security: []
      summary: Login with email and password
      operationId: login
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/LoginRequest'
      responses:
        '200':
          description: Success
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/AuthResponse'
        '401':
          $ref: '#/components/responses/Unauthorized'
        '423':
          description: Account temporarily locked
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
        '429':
          $ref: '#/components/responses/RateLimited'
  /auth/refresh:
    post:
      tags:
      - Auth
      security: []
      summary: Refresh access token
      description: V1 supports refresh-token expiry and revocation. Rotation is a future improvement.
      operationId: refreshAccessToken
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/RefreshTokenRequest'
      responses:
        '200':
          description: New access token
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/TokenResponse'
        '401':
          $ref: '#/components/responses/Unauthorized'
        '429':
          $ref: '#/components/responses/RateLimited'
  /auth/logout:
    post:
      tags:
      - Auth
      summary: Revoke refresh token
      operationId: logout
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/RefreshTokenRequest'
      responses:
        '204':
          description: No Content
        '401':
          $ref: '#/components/responses/Unauthorized'
  /auth/google:
    post:
      tags:
      - Auth
      security: []
      summary: Login with Google ID token
      operationId: googleLogin
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/GoogleLoginRequest'
      responses:
        '200':
          description: Success
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/AuthResponse'
        '401':
          $ref: '#/components/responses/Unauthorized'
  /users/me:
    get:
      tags:
      - Users
      summary: Get current user
      operationId: getCurrentUser
      responses:
        '200':
          description: Current user
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/UserResponse'
        '401':
          $ref: '#/components/responses/Unauthorized'
  /users/me/profile:
    get:
      tags:
      - Users
      summary: Get current profile
      operationId: getCurrentProfile
      responses:
        '200':
          description: Profile
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/UserProfileResponse'
    put:
      tags:
      - Users
      summary: Update current profile
      operationId: updateCurrentProfile
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/UpdateProfileRequest'
      responses:
        '200':
          description: Updated profile
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/UserProfileResponse'
        '400':
          $ref: '#/components/responses/ValidationError'
  /goals:
    get:
      tags:
      - Goals
      security: []
      summary: List active goals
      operationId: listGoals
      responses:
        '200':
          description: Plain JSON array
          content:
            application/json:
              schema:
                type: array
                items:
                  $ref: '#/components/schemas/GoalResponse'
  /users/me/goals:
    get:
      tags:
      - Goals
      summary: Get user goals
      operationId: getMyGoals
      responses:
        '200':
          description: User goals
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/UserGoalsResponse'
    put:
      tags:
      - Goals
      summary: Replace user goals
      operationId: setMyGoals
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/SetUserGoalsRequest'
      responses:
        '200':
          description: Updated goals
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/UserGoalsResponse'
        '400':
          $ref: '#/components/responses/ValidationError'
        '404':
          $ref: '#/components/responses/NotFound'
  /cefr-levels:
    get:
      tags:
      - CEFR
      security: []
      summary: List CEFR levels
      operationId: listCefrLevels
      responses:
        '200':
          description: Plain JSON array
          content:
            application/json:
              schema:
                type: array
                items:
                  $ref: '#/components/schemas/CefrLevelResponse'
  /assessments:
    post:
      tags:
      - Assessments
      summary: Start assessment
      operationId: startAssessment
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/StartAssessmentRequest'
      responses:
        '201':
          description: Assessment started
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/AssessmentResponse'
        '409':
          description: Assessment conflict (including already in progress / already answered) or idempotency reuse
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
  /assessments/{assessmentId}:
    get:
      tags:
      - Assessments
      summary: Get assessment
      operationId: getAssessment
      parameters:
      - $ref: '#/components/parameters/AssessmentId'
      responses:
        '200':
          description: Assessment result
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/AssessmentResultResponse'
        '404':
          $ref: '#/components/responses/NotFound'
  /assessments/{assessmentId}/next-question:
    get:
      tags:
      - Assessments
      summary: Get next adaptive question
      operationId: getNextAssessmentQuestion
      parameters:
      - $ref: '#/components/parameters/AssessmentId'
      responses:
        '200':
          description: Question
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/AssessmentQuestionResponse'
        '404':
          $ref: '#/components/responses/NotFound'
        '503':
          description: ASSESSMENT_CONTENT_UNAVAILABLE
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
  /assessments/{assessmentId}/answers:
    post:
      tags:
      - Assessments
      summary: Submit assessment answer
      operationId: submitAssessmentAnswer
      parameters:
      - $ref: '#/components/parameters/AssessmentId'
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/SubmitAssessmentAnswerRequest'
      responses:
        '200':
          description: Answer evaluated
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/AssessmentAnswerResponse'
        '400':
          $ref: '#/components/responses/ValidationError'
        '409':
          description: Assessment conflict (including already in progress / already answered) or idempotency reuse
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
  /topics:
    get:
      tags:
      - Topics
      summary: List topics
      operationId: listTopics
      parameters:
      - $ref: '#/components/parameters/Page'
      - $ref: '#/components/parameters/Size'
      - name: search
        in: query
        schema:
          type: string
      - name: parentTopicId
        in: query
        schema:
          type: string
          format: uuid
      responses:
        '200':
          description: Paginated topics
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/PageTopicResponse'
  /topics/{topicId}:
    get:
      tags:
      - Topics
      summary: Get topic
      operationId: getTopic
      parameters:
      - $ref: '#/components/parameters/TopicId'
      responses:
        '200':
          description: Topic
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/TopicResponse'
        '404':
          $ref: '#/components/responses/NotFound'
  /vocabulary:
    get:
      tags:
      - Vocabulary
      summary: Search vocabulary
      operationId: listVocabulary
      parameters:
      - $ref: '#/components/parameters/Page'
      - $ref: '#/components/parameters/Size'
      - name: search
        in: query
        schema:
          type: string
      - name: cefr
        in: query
        schema:
          $ref: '#/components/schemas/CefrCode'
      - name: topicId
        in: query
        schema:
          type: string
          format: uuid
      - name: partOfSpeech
        in: query
        schema:
          type: string
      - $ref: '#/components/parameters/Sort'
      responses:
        '200':
          description: Paginated vocabulary
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/PageVocabularyResponse'
  /vocabulary/{vocabularyId}:
    get:
      tags:
      - Vocabulary
      summary: Get vocabulary
      operationId: getVocabulary
      parameters:
      - $ref: '#/components/parameters/VocabularyId'
      responses:
        '200':
          description: Vocabulary detail
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/VocabularyResponse'
        '404':
          $ref: '#/components/responses/NotFound'
  /vocabulary/{vocabularyId}/examples:
    get:
      tags:
      - Vocabulary
      summary: Get examples
      operationId: getVocabularyExamples
      parameters:
      - $ref: '#/components/parameters/VocabularyId'
      responses:
        '200':
          description: Examples array
          content:
            application/json:
              schema:
                type: array
                items:
                  $ref: '#/components/schemas/VocabularyExampleResponse'
  /learning/sessions:
    post:
      tags:
      - Learning
      summary: Start learning session
      operationId: startLearningSession
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/StartLearningSessionRequest'
      responses:
        '201':
          description: Session started
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/LearningSessionResponse'
        '409':
          description: IDEMPOTENCY_KEY_REUSE or CONCURRENT_UPDATE where applicable
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
  /learning/sessions/{sessionId}:
    get:
      tags:
      - Learning
      summary: Get learning session
      operationId: getLearningSession
      parameters:
      - $ref: '#/components/parameters/SessionId'
      responses:
        '200':
          description: Session
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/LearningSessionResponse'
        '404':
          $ref: '#/components/responses/NotFound'
  /learning/sessions/{sessionId}/complete:
    post:
      tags:
      - Learning
      summary: Complete learning session
      operationId: completeLearningSession
      parameters:
      - $ref: '#/components/parameters/SessionId'
      responses:
        '200':
          description: Session summary
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/LearningSessionSummaryResponse'
        '409':
          $ref: '#/components/responses/Conflict'
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/EventIdRequest'
  /learning/today:
    get:
      tags:
      - Learning
      summary: Get today's personalized plan
      operationId: getTodayPlan
      responses:
        '200':
          description: Daily plan
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/DailyPlanResponse'
  /learning/today/items:
    get:
      tags:
      - Learning
      summary: Get today's plan items
      operationId: getTodayPlanItems
      responses:
        '200':
          description: Daily plan items
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/DailyPlanItemsResponse'
  /learning/today/complete:
    post:
      tags:
      - Learning
      summary: Complete today's plan
      operationId: completeTodayPlan
      responses:
        '200':
          description: Completed
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/CompleteDailyPlanResponse'
        '409':
          description: IDEMPOTENCY_KEY_REUSE or CONCURRENT_UPDATE where applicable
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/EventIdRequest'
  /learning/reviews:
    get:
      tags:
      - Learning
      summary: Get due reviews
      description: Domain-specific response in V1; not generic pagination.
      operationId: getDueReviews
      parameters:
      - name: limit
        in: query
        schema:
          type: integer
          minimum: 1
          maximum: 100
          default: 50
      - name: topicId
        in: query
        schema:
          type: string
          format: uuid
      responses:
        '200':
          description: Review queue
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ReviewQueueResponse'
  /learning/attempts:
    post:
      tags:
      - Learning
      summary: Submit vocabulary learning attempt
      description: '`answerQuality` is the single correctness input.

        Backend derives `isCorrect = answerQuality >= 3`.

        Client must not send `isCorrect`.

        '
      operationId: submitLearningAttempt
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/SubmitLearningAttemptRequest'
      responses:
        '200':
          description: Attempt processed
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/LearningAttemptResponse'
        '400':
          $ref: '#/components/responses/ValidationError'
        '409':
          description: Duplicate event or concurrent update
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
        '429':
          $ref: '#/components/responses/RateLimited'
  /vocabulary/{vocabularyId}/progress:
    get:
      tags:
      - Progress
      summary: Get vocabulary progress
      operationId: getVocabularyProgress
      parameters:
      - $ref: '#/components/parameters/VocabularyId'
      responses:
        '200':
          description: Progress
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/VocabularyProgressResponse'
  /progress:
    get:
      tags:
      - Progress
      summary: Get progress overview
      operationId: getProgressOverview
      responses:
        '200':
          description: Overview
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ProgressOverviewResponse'
  /progress/weak-words:
    get:
      tags:
      - Progress
      summary: Get weak words
      operationId: getWeakWords
      parameters:
      - $ref: '#/components/parameters/Page'
      - $ref: '#/components/parameters/Size'
      responses:
        '200':
          description: Paginated weak words
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/PageWeakWordResponse'
  /progress/trend:
    get:
      tags:
      - Progress
      summary: Get progress trend
      operationId: getProgressTrend
      parameters:
      - name: from
        in: query
        required: true
        schema:
          type: string
          format: date
      - name: to
        in: query
        required: true
        schema:
          type: string
          format: date
      - name: granularity
        in: query
        schema:
          type: string
          enum:
          - DAY
          - WEEK
          - MONTH
          default: DAY
      responses:
        '200':
          description: Trend
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ProgressTrendResponse'
  /quizzes:
    get:
      tags:
      - Quiz
      summary: List quizzes
      operationId: listQuizzes
      parameters:
      - $ref: '#/components/parameters/Page'
      - $ref: '#/components/parameters/Size'
      - name: topicId
        in: query
        schema:
          type: string
          format: uuid
      - name: cefr
        in: query
        schema:
          $ref: '#/components/schemas/CefrCode'
      responses:
        '200':
          description: Paginated quizzes
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/PageQuizSummaryResponse'
  /quizzes/{quizId}:
    get:
      tags:
      - Quiz
      summary: Get quiz
      operationId: getQuiz
      parameters:
      - $ref: '#/components/parameters/QuizId'
      responses:
        '200':
          description: Quiz without correct answers
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/QuizResponse'
  /quizzes/{quizId}/attempts:
    post:
      tags:
      - Quiz
      summary: Start quiz attempt
      operationId: startQuizAttempt
      parameters:
      - $ref: '#/components/parameters/QuizId'
      responses:
        '201':
          description: Started
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/QuizAttemptResponse'
        '409':
          description: IDEMPOTENCY_KEY_REUSE or CONCURRENT_UPDATE where applicable
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/StartQuizAttemptRequest'
  /quiz-attempts/{attemptId}:
    get:
      tags:
      - Quiz
      summary: Get quiz attempt
      operationId: getQuizAttempt
      parameters:
      - $ref: '#/components/parameters/QuizAttemptId'
      responses:
        '200':
          description: Attempt
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/QuizAttemptDetailResponse'
  /quiz-attempts/{attemptId}/answers:
    post:
      tags:
      - Quiz
      summary: Submit quiz answer
      operationId: submitQuizAnswer
      parameters:
      - $ref: '#/components/parameters/QuizAttemptId'
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/SubmitQuizAnswerRequest'
      responses:
        '200':
          description: Answer
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/QuizAnswerResponse'
        '409':
          $ref: '#/components/responses/Conflict'
  /quiz-attempts/{attemptId}/complete:
    post:
      tags:
      - Quiz
      summary: Complete quiz
      operationId: completeQuizAttempt
      parameters:
      - $ref: '#/components/parameters/QuizAttemptId'
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/CompleteQuizAttemptRequest'
      responses:
        '200':
          description: Completion
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/QuizCompletionResponse'
        '409':
          $ref: '#/components/responses/Conflict'
  /gamification/streak:
    get:
      tags:
      - Gamification
      summary: Get streak
      operationId: getStreak
      responses:
        '200':
          description: Streak
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/StreakResponse'
  /gamification/xp:
    get:
      tags:
      - Gamification
      summary: Get XP summary
      operationId: getXp
      responses:
        '200':
          description: XP
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/XpSummaryResponse'
  /gamification/xp/history:
    get:
      tags:
      - Gamification
      summary: Get XP history
      operationId: getXpHistory
      parameters:
      - $ref: '#/components/parameters/Page'
      - $ref: '#/components/parameters/Size'
      - name: from
        in: query
        schema:
          type: string
          format: date-time
      - name: to
        in: query
        schema:
          type: string
          format: date-time
      responses:
        '200':
          description: Paginated XP history
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/PageXpLogResponse'
  /gamification/badges:
    get:
      tags:
      - Gamification
      summary: Get badges
      operationId: getBadges
      responses:
        '200':
          description: Badge summary
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/BadgeSummaryResponse'
  /gamification/leaderboard:
    get:
      tags:
      - Gamification
      summary: Get leaderboard
      operationId: getLeaderboard
      parameters:
      - name: period
        in: query
        required: true
        schema:
          type: string
          enum:
          - DAILY
          - WEEKLY
          - MONTHLY
          - ALL_TIME
      - $ref: '#/components/parameters/Page'
      - $ref: '#/components/parameters/Size'
      responses:
        '200':
          description: Paginated leaderboard
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/PageLeaderboardEntryResponse'
  /notifications:
    get:
      tags:
      - Notifications
      summary: List notifications
      operationId: listNotifications
      parameters:
      - $ref: '#/components/parameters/Page'
      - $ref: '#/components/parameters/Size'
      - name: status
        in: query
        schema:
          $ref: '#/components/schemas/NotificationStatus'
      responses:
        '200':
          description: Paginated notifications
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/PageNotificationResponse'
  /learning/personalized-exercise:
    post:
      tags:
      - Learning
      summary: Generate personalized AI exercise
      description: Personalized content uses validation/safety filtering and does not require per-result manual Admin Review.
      operationId: generatePersonalizedExercise
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/PersonalizedExerciseRequest'
      responses:
        '200':
          description: Exercise generated
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/PersonalizedExerciseResponse'
        '429':
          description: Rate limit or AI budget exceeded
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
        '503':
          description: AI provider unavailable
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
        '409':
          description: IDEMPOTENCY_KEY_REUSE or CONCURRENT_UPDATE where applicable
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
  /admin/users:
    get:
      tags:
      - Admin Users
      summary: List users
      operationId: adminListUsers
      parameters:
      - $ref: '#/components/parameters/Page'
      - $ref: '#/components/parameters/Size'
      - name: search
        in: query
        schema:
          type: string
      - name: role
        in: query
        schema:
          $ref: '#/components/schemas/UserRole'
      - name: status
        in: query
        schema:
          $ref: '#/components/schemas/UserStatus'
      responses:
        '200':
          description: Paginated users
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/PageAdminUserResponse'
        '403':
          $ref: '#/components/responses/Forbidden'
  /admin/users/{userId}:
    get:
      tags:
      - Admin Users
      summary: Get user
      operationId: adminGetUser
      parameters:
      - $ref: '#/components/parameters/UserId'
      responses:
        '200':
          description: User
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/AdminUserDetailResponse'
        '403':
          $ref: '#/components/responses/Forbidden'
        '404':
          $ref: '#/components/responses/NotFound'
  /admin/users/{userId}/lock:
    post:
      tags:
      - Admin Users
      summary: Lock user
      operationId: adminLockUser
      parameters:
      - $ref: '#/components/parameters/UserId'
      responses:
        '204':
          description: Locked
        '403':
          $ref: '#/components/responses/Forbidden'
  /admin/users/{userId}/unlock:
    post:
      tags:
      - Admin Users
      summary: Unlock user
      operationId: adminUnlockUser
      parameters:
      - $ref: '#/components/parameters/UserId'
      responses:
        '204':
          description: Unlocked
        '403':
          $ref: '#/components/responses/Forbidden'
  /admin/vocabulary:
    get:
      tags:
      - Admin Vocabulary
      summary: List vocabulary
      operationId: adminListVocabulary
      parameters:
      - $ref: '#/components/parameters/Page'
      - $ref: '#/components/parameters/Size'
      - name: search
        in: query
        schema:
          type: string
      - name: cefr
        in: query
        schema:
          $ref: '#/components/schemas/CefrCode'
      - name: topicId
        in: query
        schema:
          type: string
          format: uuid
      responses:
        '200':
          description: Paginated vocabulary
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/PageVocabularyResponse'
    post:
      tags:
      - Admin Vocabulary
      summary: Create vocabulary
      operationId: adminCreateVocabulary
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/CreateVocabularyRequest'
      responses:
        '201':
          description: Created
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/VocabularyResponse'
        '409':
          $ref: '#/components/responses/Conflict'
  /admin/vocabulary/{vocabularyId}:
    put:
      tags:
      - Admin Vocabulary
      summary: Update vocabulary
      operationId: adminUpdateVocabulary
      parameters:
      - $ref: '#/components/parameters/VocabularyId'
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/UpdateVocabularyRequest'
      responses:
        '200':
          description: Updated
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/VocabularyResponse'
        '409':
          $ref: '#/components/responses/Conflict'
  /admin/vocabulary/{vocabularyId}/deactivate:
    post:
      tags:
      - Admin Vocabulary
      summary: Deactivate vocabulary
      operationId: adminDeactivateVocabulary
      parameters:
      - $ref: '#/components/parameters/VocabularyId'
      responses:
        '204':
          description: Deactivated
  /admin/vocabulary/{vocabularyId}/activate:
    post:
      tags:
      - Admin Vocabulary
      summary: Reactivate vocabulary
      operationId: adminActivateVocabulary
      parameters:
      - $ref: '#/components/parameters/VocabularyId'
      responses:
        '204':
          description: Activated
  /admin/topics:
    post:
      tags:
      - Admin Topics
      summary: Create topic
      operationId: adminCreateTopic
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/CreateTopicRequest'
      responses:
        '201':
          description: Created
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/TopicResponse'
  /admin/topics/{topicId}:
    put:
      tags:
      - Admin Topics
      summary: Update topic
      operationId: adminUpdateTopic
      parameters:
      - $ref: '#/components/parameters/TopicId'
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/UpdateTopicRequest'
      responses:
        '200':
          description: Updated
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/TopicResponse'
  /admin/topics/{topicId}/deactivate:
    post:
      tags:
      - Admin Topics
      summary: Deactivate topic
      operationId: adminDeactivateTopic
      parameters:
      - $ref: '#/components/parameters/TopicId'
      responses:
        '204':
          description: Deactivated
  /admin/quizzes:
    post:
      tags:
      - Admin Quiz
      summary: Create quiz
      operationId: adminCreateQuiz
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/CreateQuizRequest'
      responses:
        '201':
          description: Created
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/QuizResponse'
  /admin/quizzes/{quizId}:
    put:
      tags:
      - Admin Quiz
      summary: Update quiz
      operationId: adminUpdateQuiz
      parameters:
      - $ref: '#/components/parameters/QuizId'
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/UpdateQuizRequest'
      responses:
        '200':
          description: Updated
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/QuizResponse'
  /admin/quizzes/{quizId}/questions:
    post:
      tags:
      - Admin Quiz
      summary: Add quiz question
      operationId: adminAddQuizQuestion
      parameters:
      - $ref: '#/components/parameters/QuizId'
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/CreateQuizQuestionRequest'
      responses:
        '201':
          description: Created
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/QuizQuestionAdminResponse'
  /admin/quiz-questions/{questionId}:
    put:
      tags:
      - Admin Quiz
      summary: Update quiz question
      operationId: adminUpdateQuizQuestion
      parameters:
      - $ref: '#/components/parameters/QuizQuestionId'
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/UpdateQuizQuestionRequest'
      responses:
        '200':
          description: Updated
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/QuizQuestionAdminResponse'
  /admin/quizzes/{quizId}/publish:
    post:
      tags:
      - Admin Quiz
      summary: Publish quiz
      operationId: adminPublishQuiz
      parameters:
      - $ref: '#/components/parameters/QuizId'
      responses:
        '204':
          description: Published
  /admin/quizzes/{quizId}/unpublish:
    post:
      tags:
      - Admin Quiz
      summary: Unpublish quiz
      operationId: adminUnpublishQuiz
      parameters:
      - $ref: '#/components/parameters/QuizId'
      responses:
        '204':
          description: Unpublished
  /admin/ai-content/generate:
    post:
      tags:
      - Admin AI Content
      summary: Generate reusable AI content
      description: Reusable content is validated, then stored as PENDING_REVIEW for Admin Review.
      operationId: adminGenerateAiContent
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/GenerateAiContentRequest'
      responses:
        '429':
          $ref: '#/components/responses/RateLimited'
        '503':
          description: AI provider unavailable
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
        '201':
          description: Created reusable content in PENDING_REVIEW
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/AiGeneratedContentResponse'
        '409':
          description: IDEMPOTENCY_KEY_REUSE or CONCURRENT_UPDATE where applicable
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
  /admin/ai-content:
    get:
      tags:
      - Admin AI Content
      summary: List AI content
      operationId: adminListAiContent
      parameters:
      - $ref: '#/components/parameters/Page'
      - $ref: '#/components/parameters/Size'
      - name: status
        in: query
        schema:
          $ref: '#/components/schemas/AiContentStatus'
      - name: contentScope
        in: query
        schema:
          $ref: '#/components/schemas/AiContentScope'
      - name: contentType
        in: query
        schema:
          $ref: '#/components/schemas/AiContentType'
      responses:
        '200':
          description: Paginated AI content
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/PageAiGeneratedContentResponse'
  /admin/ai-content/{contentId}:
    get:
      tags:
      - Admin AI Content
      summary: Get AI content
      operationId: adminGetAiContent
      parameters:
      - $ref: '#/components/parameters/AiContentId'
      responses:
        '200':
          description: AI content
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/AiGeneratedContentResponse'
  /admin/ai-content/{contentId}/approve:
    post:
      tags:
      - Admin AI Content
      summary: Approve reusable AI content
      operationId: adminApproveAiContent
      parameters:
      - $ref: '#/components/parameters/AiContentId'
      responses:
        '200':
          description: Approved
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/AiGeneratedContentResponse'
        '409':
          $ref: '#/components/responses/Conflict'
  /admin/ai-content/{contentId}/reject:
    post:
      tags:
      - Admin AI Content
      summary: Reject reusable AI content
      operationId: adminRejectAiContent
      parameters:
      - $ref: '#/components/parameters/AiContentId'
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/RejectAiContentRequest'
      responses:
        '200':
          description: Rejected
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/AiGeneratedContentResponse'
  /admin/statistics/learning:
    get:
      tags:
      - Admin Statistics
      summary: Get learning statistics
      operationId: adminLearningStatistics
      parameters:
      - $ref: '#/components/parameters/FromDate'
      - $ref: '#/components/parameters/ToDate'
      responses:
        '200':
          description: Statistics
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/AdminLearningStatisticsResponse'
  /admin/statistics/ai-usage:
    get:
      tags:
      - Admin Statistics
      summary: Get AI usage statistics
      operationId: adminAiUsageStatistics
      parameters:
      - $ref: '#/components/parameters/FromDate'
      - $ref: '#/components/parameters/ToDate'
      - name: provider
        in: query
        schema:
          type: string
      - name: model
        in: query
        schema:
          type: string
      - name: feature
        in: query
        schema:
          type: string
      responses:
        '200':
          description: AI usage statistics
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/AdminAiUsageStatisticsResponse'
  /admin/audit-logs:
    get:
      tags:
      - Admin Audit
      summary: List admin audit logs
      operationId: adminListAuditLogs
      parameters:
      - $ref: '#/components/parameters/Page'
      - $ref: '#/components/parameters/Size'
      - name: adminId
        in: query
        schema:
          type: string
          format: uuid
      - name: action
        in: query
        schema:
          type: string
      - name: targetTable
        in: query
        schema:
          type: string
      - name: from
        in: query
        schema:
          type: string
          format: date-time
      - name: to
        in: query
        schema:
          type: string
          format: date-time
      responses:
        '200':
          description: Paginated audit logs
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/PageAuditLogResponse'
  /devices/{installationId}/push-token:
    put:
      tags:
      - Devices
      summary: Register or refresh Android FCM push token
      operationId: upsertPushToken
      parameters:
      - name: installationId
        in: path
        required: true
        schema:
          type: string
          format: uuid
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/PushTokenRequest'
      responses:
        '204':
          description: Token registered/updated
        '400':
          $ref: '#/components/responses/ValidationError'
  /devices/{installationId}:
    delete:
      tags:
      - Devices
      summary: Deactivate a device installation
      operationId: deactivateDevice
      parameters:
      - name: installationId
        in: path
        required: true
        schema:
          type: string
          format: uuid
      responses:
        '204':
          description: Device deactivated
        '404':
          $ref: '#/components/responses/NotFound'
  /notification-preferences:
    get:
      tags:
      - Notifications
      summary: Get notification preferences
      operationId: getNotificationPreferences
      responses:
        '200':
          description: Preferences
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/NotificationPreferencesResponse'
    put:
      tags:
      - Notifications
      summary: Update notification preferences
      operationId: updateNotificationPreferences
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/UpdateNotificationPreferencesRequest'
      responses:
        '200':
          description: Updated preferences
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/NotificationPreferencesResponse'
        '400':
          $ref: '#/components/responses/ValidationError'
components:
  securitySchemes:
    bearerAuth:
      type: http
      scheme: bearer
      bearerFormat: JWT
  parameters:
    Page:
      name: page
      in: query
      schema:
        type: integer
        minimum: 0
        default: 0
    Size:
      name: size
      in: query
      schema:
        type: integer
        minimum: 1
        maximum: 100
        default: 20
    Sort:
      name: sort
      in: query
      schema:
        type: string
        example: word,asc
    UserId:
      name: userId
      in: path
      required: true
      schema:
        type: string
        format: uuid
    AssessmentId:
      name: assessmentId
      in: path
      required: true
      schema:
        type: string
        format: uuid
    TopicId:
      name: topicId
      in: path
      required: true
      schema:
        type: string
        format: uuid
    VocabularyId:
      name: vocabularyId
      in: path
      required: true
      schema:
        type: string
        format: uuid
    SessionId:
      name: sessionId
      in: path
      required: true
      schema:
        type: string
        format: uuid
    QuizId:
      name: quizId
      in: path
      required: true
      schema:
        type: string
        format: uuid
    QuizAttemptId:
      name: attemptId
      in: path
      required: true
      schema:
        type: string
        format: uuid
    QuizQuestionId:
      name: questionId
      in: path
      required: true
      schema:
        type: string
        format: uuid
    AiContentId:
      name: contentId
      in: path
      required: true
      schema:
        type: string
        format: uuid
    FromDate:
      name: from
      in: query
      required: true
      schema:
        type: string
        format: date
    ToDate:
      name: to
      in: query
      required: true
      schema:
        type: string
        format: date
  responses:
    ValidationError:
      description: Validation error
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/ErrorResponse'
    Unauthorized:
      description: Authentication required/invalid
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/ErrorResponse'
    Forbidden:
      description: Insufficient permission
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/ErrorResponse'
    NotFound:
      description: Resource not found
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/ErrorResponse'
    Conflict:
      description: Conflict
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/ErrorResponse'
    RateLimited:
      description: Rate limit / AI budget exceeded
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/ErrorResponse'
  schemas:
    ErrorResponse:
      type: object
      required:
      - timestamp
      - status
      - code
      - message
      - path
      properties:
        timestamp:
          type: string
          format: date-time
        status:
          type: integer
        code:
          type: string
        message:
          type: string
        path:
          type: string
        details:
          type: array
          items:
            type: object
            additionalProperties: true
    RegisterRequest:
      type: object
      required:
      - email
      - password
      - fullName
      properties:
        email:
          type: string
          format: email
          maxLength: 255
        password:
          type: string
          minLength: 8
          maxLength: 100
          writeOnly: true
        fullName:
          type: string
          minLength: 1
          maxLength: 100
    LoginRequest:
      type: object
      required:
      - email
      - password
      properties:
        email:
          type: string
          format: email
        password:
          type: string
          minLength: 1
          maxLength: 100
          writeOnly: true
    GoogleLoginRequest:
      type: object
      required:
      - idToken
      properties:
        idToken:
          type: string
          writeOnly: true
    RefreshTokenRequest:
      type: object
      required:
      - refreshToken
      properties:
        refreshToken:
          type: string
          writeOnly: true
    TokenResponse:
      type: object
      required:
      - accessToken
      - expiresIn
      - tokenType
      properties:
        accessToken:
          type: string
        expiresIn:
          type: integer
          example: 1800
        tokenType:
          type: string
          example: Bearer
    UserRole:
      type: string
      enum:
      - USER
      - ADMIN
    UserStatus:
      type: string
      enum:
      - ACTIVE
      - LOCKED
    AuthUserSummary:
      type: object
      required:
      - id
      - email
      - fullName
      - role
      - status
      properties:
        id:
          type: string
          format: uuid
        email:
          type: string
          format: email
        fullName:
          type: string
        role:
          $ref: '#/components/schemas/UserRole'
        status:
          $ref: '#/components/schemas/UserStatus'
    AuthResponse:
      allOf:
      - $ref: '#/components/schemas/TokenResponse'
      - type: object
        required:
        - user
        properties:
          user:
            $ref: '#/components/schemas/AuthUserSummary'
    UserResponse:
      $ref: '#/components/schemas/AuthUserSummary'
    UpdateProfileRequest:
      type: object
      required:
      - fullName
      - dailyLearningMinutes
      - timezone
      properties:
        fullName:
          type: string
          maxLength: 100
        avatarUrl:
          type: string
          nullable: true
        dailyLearningMinutes:
          type: integer
          minimum: 5
          maximum: 180
        timezone:
          type: string
          maxLength: 50
    UserProfileResponse:
      type: object
      required:
      - fullName
      - dailyLearningMinutes
      - timezone
      properties:
        fullName:
          type: string
        avatarUrl:
          type: string
          nullable: true
        currentCefrLevel:
          $ref: '#/components/schemas/CefrLevelResponse'
        dailyLearningMinutes:
          type: integer
        timezone:
          type: string
    GoalResponse:
      type: object
      required:
      - id
      - name
      properties:
        id:
          type: string
          format: uuid
        name:
          type: string
        description:
          type: string
          nullable: true
    UserGoalResponse:
      type: object
      required:
      - id
      - name
      - isPrimary
      properties:
        id:
          type: string
          format: uuid
        name:
          type: string
        isPrimary:
          type: boolean
    UserGoalsResponse:
      type: object
      required:
      - goals
      properties:
        goals:
          type: array
          items:
            $ref: '#/components/schemas/UserGoalResponse'
    SetUserGoalsRequest:
      type: object
      required:
      - goalIds
      - primaryGoalId
      properties:
        goalIds:
          type: array
          minItems: 1
          items:
            type: string
            format: uuid
        primaryGoalId:
          type: string
          format: uuid
    CefrCode:
      type: string
      enum:
      - A1
      - A2
      - B1
      - B2
      - C1
      - C2
    CefrLevelResponse:
      type: object
      required:
      - id
      - code
      - name
      - sortOrder
      properties:
        id:
          type: string
          format: uuid
        code:
          $ref: '#/components/schemas/CefrCode'
        name:
          type: string
        description:
          type: string
          nullable: true
        sortOrder:
          type: integer
    StartAssessmentRequest:
      type: object
      required:
      - type
      - eventId
      properties:
        type:
          type: string
          enum:
          - INITIAL
          - PERIODIC
          - MANUAL
        eventId:
          type: string
          format: uuid
          description: Client-generated idempotency key for one logical operation.
      additionalProperties: false
    AssessmentResponse:
      type: object
      required:
      - assessmentId
      - type
      - status
      - currentDifficulty
      properties:
        assessmentId:
          type: string
          format: uuid
        type:
          type: string
          enum:
          - INITIAL
          - PERIODIC
          - MANUAL
        status:
          type: string
          enum:
          - IN_PROGRESS
          - COMPLETED
          - CANCELLED
        currentDifficulty:
          $ref: '#/components/schemas/CefrCode'
    AssessmentQuestionResponse:
      type: object
      required:
      - questionId
      - questionText
      - options
      - cefrLevel
      properties:
        questionId:
          type: string
          format: uuid
        questionText:
          type: string
        options:
          type: array
          items:
            type: string
        cefrLevel:
          $ref: '#/components/schemas/CefrCode'
    SubmitAssessmentAnswerRequest:
      type: object
      required:
      - questionId
      - answer
      - responseTimeMs
      - eventId
      properties:
        questionId:
          type: string
          format: uuid
        answer:
          type: string
        responseTimeMs:
          type: integer
          minimum: 0
        eventId:
          type: string
          format: uuid
          description: Client-generated idempotency key for one logical operation.
      additionalProperties: false
    AssessmentAnswerResponse:
      type: object
      required:
      - correct
      - questionsAnswered
      - isCompleted
      properties:
        correct:
          type: boolean
        nextDifficulty:
          $ref: '#/components/schemas/CefrCode'
        questionsAnswered:
          type: integer
          minimum: 0
        isCompleted:
          type: boolean
    AssessmentResultResponse:
      type: object
      required:
      - assessmentId
      - status
      - score
      - totalQuestions
      - correctAnswers
      properties:
        assessmentId:
          type: string
          format: uuid
        status:
          type: string
          enum:
          - IN_PROGRESS
          - COMPLETED
          - CANCELLED
        score:
          type: number
          format: double
        totalQuestions:
          type: integer
        correctAnswers:
          type: integer
        cefrLevel:
          $ref: '#/components/schemas/CefrLevelResponse'
        createdAt:
          type: string
          format: date-time
    TopicResponse:
      type: object
      required:
      - id
      - name
      - isActive
      properties:
        id:
          type: string
          format: uuid
        name:
          type: string
        description:
          type: string
          nullable: true
        iconUrl:
          type: string
          nullable: true
        parentTopicId:
          type: string
          format: uuid
          nullable: true
        isActive:
          type: boolean
    VocabularyExampleResponse:
      type: object
      required:
      - id
      - exampleText
      - source
      properties:
        id:
          type: string
          format: uuid
        exampleText:
          type: string
        translationText:
          type: string
          nullable: true
        source:
          type: string
          enum:
          - MANUAL
          - AI_GENERATED
    VocabularyResponse:
      type: object
      required:
      - id
      - word
      - cefr
      properties:
        id:
          type: string
          format: uuid
        word:
          type: string
        phoneticIpa:
          type: string
          nullable: true
        meaningVi:
          type: string
          nullable: true
        meaningEn:
          type: string
          nullable: true
        partOfSpeech:
          type: string
          nullable: true
        cefr:
          $ref: '#/components/schemas/CefrCode'
        topics:
          type: array
          items:
            $ref: '#/components/schemas/TopicResponse'
        audioUrl:
          type: string
          nullable: true
        imageUrl:
          type: string
          nullable: true
        examples:
          type: array
          items:
            $ref: '#/components/schemas/VocabularyExampleResponse'
    StartLearningSessionRequest:
      type: object
      required:
      - sessionType
      - eventId
      properties:
        sessionType:
          type: string
          enum:
          - NEW_WORDS
          - REVIEW
          - QUIZ
          - MIXED
        eventId:
          type: string
          format: uuid
          description: Client-generated idempotency key for one logical operation.
      additionalProperties: false
    LearningSessionResponse:
      type: object
      required:
      - sessionId
      - sessionType
      - startedAt
      properties:
        sessionId:
          type: string
          format: uuid
        sessionType:
          type: string
          enum:
          - NEW_WORDS
          - REVIEW
          - QUIZ
          - MIXED
        startedAt:
          type: string
          format: date-time
        endedAt:
          type: string
          format: date-time
          nullable: true
        wordsStudiedCount:
          type: integer
        accuracyPercent:
          type: number
          format: double
          nullable: true
    LearningSessionSummaryResponse:
      $ref: '#/components/schemas/LearningSessionResponse'
    DailyPlanResponse:
      type: object
      required:
      - planId
      - date
      - estimatedMinutes
      - status
      - newWordsTarget
      - reviewWordsTarget
      - quizTarget
      properties:
        planId:
          type: string
          format: uuid
        date:
          type: string
          format: date
        estimatedMinutes:
          type: integer
        status:
          type: string
          enum:
          - PENDING
          - IN_PROGRESS
          - COMPLETED
          - PARTIAL
        newWordsTarget:
          type: integer
        reviewWordsTarget:
          type: integer
        quizTarget:
          type: integer
    DailyPlanItem:
      type: object
      required:
      - itemId
      - itemType
      - priority
      - targetCount
      - completedCount
      - status
      properties:
        itemId:
          type: string
          format: uuid
        itemType:
          type: string
          enum:
          - REVIEW
          - NEW
          - QUIZ
        vocabularyId:
          type:
          - string
          - 'null'
          format: uuid
          description: Required for REVIEW/NEW; null for aggregate QUIZ item.
        priority:
          type: integer
          minimum: 1
        reasonCode:
          type:
          - string
          - 'null'
        targetCount:
          type: integer
          minimum: 1
        completedCount:
          type: integer
          minimum: 0
        status:
          type: string
          enum:
          - PENDING
          - IN_PROGRESS
          - COMPLETED
    DailyPlanItemsResponse:
      type: object
      required:
      - planId
      - items
      properties:
        planId:
          type: string
          format: uuid
        items:
          type: array
          items:
            $ref: '#/components/schemas/DailyPlanItem'
    CompleteDailyPlanResponse:
      type: object
      required:
      - planId
      - status
      - xpEarned
      properties:
        planId:
          type: string
          format: uuid
        status:
          type: string
          enum:
          - COMPLETED
          - PARTIAL
        xpEarned:
          type: integer
    ReviewQueueItem:
      type: object
      required:
      - vocabularyId
      - word
      - status
      - nextReviewAt
      - forgettingRisk
      properties:
        vocabularyId:
          type: string
          format: uuid
        word:
          type: string
        status:
          type: string
          enum:
          - NEW
          - LEARNING
          - REVIEWING
          - MASTERED
        nextReviewAt:
          type: string
          format: date-time
        forgettingRisk:
          type: string
          enum:
          - LOW
          - MEDIUM
          - HIGH
    ReviewQueueResponse:
      type: object
      required:
      - items
      - count
      properties:
        items:
          type: array
          items:
            $ref: '#/components/schemas/ReviewQueueItem'
        count:
          type: integer
    SubmitLearningAttemptRequest:
      type: object
      required:
      - sessionId
      - vocabularyId
      - attemptType
      - responseTimeMs
      - answerQuality
      - eventId
      additionalProperties: false
      properties:
        sessionId:
          type: string
          format: uuid
        vocabularyId:
          type: string
          format: uuid
        attemptType:
          type: string
          enum:
          - FLASHCARD
          - WORD_RECALL
          - WORD_MEANING
          - MULTIPLE_CHOICE
          - FILL_BLANK
          - MATCHING
        responseTimeMs:
          type: integer
          minimum: 0
        answerQuality:
          type: integer
          minimum: 0
          maximum: 5
        eventId:
          type: string
          format: uuid
          description: Client-generated idempotency key for one logical operation.
    SrsResult:
      type: object
      required:
      - oldEaseFactor
      - newEaseFactor
      - oldIntervalDays
      - newIntervalDays
      - oldRepetitions
      - newRepetitions
      - nextReviewAt
      - algorithmVersion
      properties:
        oldEaseFactor:
          type: number
          format: double
        newEaseFactor:
          type: number
          format: double
        oldIntervalDays:
          type: integer
        newIntervalDays:
          type: integer
        oldRepetitions:
          type: integer
        newRepetitions:
          type: integer
        nextReviewAt:
          type: string
          format: date-time
        algorithmVersion:
          type: string
          example: sm2-ext-v1
    VocabularyProgressResponse:
      type: object
      required:
      - vocabularyId
      - status
      - easeFactor
      - intervalDays
      - repetitions
      - correctCount
      - incorrectCount
      properties:
        vocabularyId:
          type: string
          format: uuid
        status:
          type: string
          enum:
          - NEW
          - LEARNING
          - REVIEWING
          - MASTERED
        easeFactor:
          type: number
          format: double
        intervalDays:
          type: integer
        repetitions:
          type: integer
        nextReviewAt:
          type: string
          format: date-time
          nullable: true
        correctCount:
          type: integer
        incorrectCount:
          type: integer
    LearningAttemptResponse:
      type: object
      required:
      - attemptId
      - isCorrect
      - answerQuality
      - srs
      - progress
      properties:
        attemptId:
          type: string
          format: uuid
        isCorrect:
          type: boolean
          readOnly: true
        answerQuality:
          type: integer
          minimum: 0
          maximum: 5
        srs:
          $ref: '#/components/schemas/SrsResult'
        progress:
          $ref: '#/components/schemas/VocabularyProgressResponse'
    ProgressOverviewResponse:
      type: object
      properties:
        wordsLearned:
          type: integer
        wordsMastered:
          type: integer
        accuracyPercent:
          type: number
          format: double
        learningMinutes:
          type: integer
        currentStreak:
          type: integer
        goalProgressPercent:
          type: number
          format: double
    WeakWordResponse:
      type: object
      required:
      - vocabularyId
      - word
      - weaknessScore
      - reasonCodes
      properties:
        vocabularyId:
          type: string
          format: uuid
        word:
          type: string
        weaknessScore:
          type: number
          format: double
          minimum: 0
          maximum: 1
        reasonCodes:
          type: array
          items:
            type: string
            enum:
            - LOW_ACCURACY
            - RECENT_FAILURES
            - SLOW_RECALL
            - LOW_ANSWER_QUALITY
    ProgressTrendPoint:
      type: object
      properties:
        date:
          type: string
          format: date
        accuracyPercent:
          type: number
          format: double
        wordsStudied:
          type: integer
        learningMinutes:
          type: integer
    ProgressTrendResponse:
      type: object
      required:
      - points
      properties:
        points:
          type: array
          items:
            $ref: '#/components/schemas/ProgressTrendPoint'
    QuizSummaryResponse:
      type: object
      required:
      - id
      - title
      properties:
        id:
          type: string
          format: uuid
        title:
          type: string
        description:
          type: string
          nullable: true
        cefr:
          $ref: '#/components/schemas/CefrCode'
        topic:
          $ref: '#/components/schemas/TopicResponse'
    QuizQuestionResponse:
      type: object
      required:
      - id
      - questionText
      - questionType
      - options
      properties:
        id:
          type: string
          format: uuid
        questionText:
          type: string
        questionType:
          type: string
          enum:
          - MULTIPLE_CHOICE
          - FILL_BLANK
          - MATCHING
        options:
          type: array
          items:
            type: string
    QuizResponse:
      allOf:
      - $ref: '#/components/schemas/QuizSummaryResponse'
      - type: object
        properties:
          questions:
            type: array
            items:
              $ref: '#/components/schemas/QuizQuestionResponse'
    CompleteQuizAttemptRequest:
      type: object
      required:
      - eventId
      additionalProperties: false
      properties:
        eventId:
          type: string
          format: uuid
          description: Client-generated idempotency key for one logical operation.
    QuizAttemptResponse:
      type: object
      required:
      - attemptId
      - quizId
      - startedAt
      properties:
        attemptId:
          type: string
          format: uuid
        quizId:
          type: string
          format: uuid
        startedAt:
          type: string
          format: date-time
    QuizAttemptDetailResponse:
      type: object
      required:
      - attemptId
      - quizId
      - startedAt
      properties:
        attemptId:
          type: string
          format: uuid
        quizId:
          type: string
          format: uuid
        score:
          type: number
          format: double
          nullable: true
        totalQuestions:
          type: integer
          nullable: true
        correctAnswers:
          type: integer
          nullable: true
        startedAt:
          type: string
          format: date-time
        completedAt:
          type: string
          format: date-time
          nullable: true
    SubmitQuizAnswerRequest:
      type: object
      required:
      - questionId
      - userAnswer
      - responseTimeMs
      - eventId
      additionalProperties: false
      properties:
        questionId:
          type: string
          format: uuid
        userAnswer:
          type: string
        responseTimeMs:
          type: integer
          minimum: 0
        eventId:
          type: string
          format: uuid
          description: Client-generated idempotency key for one logical operation.
    QuizAnswerResponse:
      type: object
      required:
      - answerId
      - isCorrect
      properties:
        answerId:
          type: string
          format: uuid
        isCorrect:
          type: boolean
          readOnly: true
    QuizCompletionResponse:
      type: object
      required:
      - attemptId
      - score
      - totalQuestions
      - correctAnswers
      - completedAt
      properties:
        attemptId:
          type: string
          format: uuid
        score:
          type: number
          format: double
        totalQuestions:
          type: integer
        correctAnswers:
          type: integer
        completedAt:
          type: string
          format: date-time
    StreakResponse:
      type: object
      properties:
        currentStreak:
          type: integer
        longestStreak:
          type: integer
        lastActiveDate:
          type: string
          format: date
          nullable: true
    XpSummaryResponse:
      type: object
      properties:
        totalXp:
          type: integer
        level:
          type: integer
        nextLevelXp:
          type: integer
    XpLogResponse:
      type: object
      properties:
        id:
          type: string
          format: uuid
        xpAmount:
          type: integer
        reason:
          type: string
        createdAt:
          type: string
          format: date-time
    BadgeResponse:
      type: object
      properties:
        id:
          type: string
          format: uuid
        name:
          type: string
        description:
          type: string
          nullable: true
        earnedAt:
          type: string
          format: date-time
          nullable: true
    BadgeSummaryResponse:
      type: object
      properties:
        earned:
          type: array
          items:
            $ref: '#/components/schemas/BadgeResponse'
        available:
          type: array
          items:
            $ref: '#/components/schemas/BadgeResponse'
    LeaderboardEntryResponse:
      type: object
      properties:
        rank:
          type: integer
        userId:
          type: string
          format: uuid
        displayName:
          type: string
        xp:
          type: integer
    NotificationStatus:
      type: string
      enum:
      - PENDING
      - SENT
      - FAILED
      - CANCELLED
    NotificationResponse:
      type: object
      properties:
        id:
          type: string
          format: uuid
        type:
          type: string
          enum:
          - REVIEW_REMINDER
          - DAILY_PLAN
          - STREAK
          - SYSTEM
        title:
          type: string
        message:
          type: string
        scheduledAt:
          type: string
          format: date-time
          nullable: true
        sentAt:
          type: string
          format: date-time
          nullable: true
        status:
          $ref: '#/components/schemas/NotificationStatus'
        createdAt:
          type: string
          format: date-time
    PersonalizedExerciseRequest:
      type: object
      required:
      - activityType
      - count
      - eventId
      properties:
        activityType:
          type: string
          enum:
          - MULTIPLE_CHOICE
          - FILL_BLANK
          - MATCHING
          - WORD_RECALL
        count:
          type: integer
          minimum: 1
          maximum: 20
        vocabularyIds:
          type: array
          items:
            type: string
            format: uuid
        eventId:
          type: string
          format: uuid
          description: Client-generated idempotency key for one logical operation.
      additionalProperties: false
    PersonalizedExerciseItem:
      type: object
      required:
      - question
      - vocabularyId
      properties:
        question:
          type: string
        vocabularyId:
          type: string
          format: uuid
        options:
          type: array
          items:
            type: string
          nullable: true
    PersonalizedExerciseResponse:
      type: object
      required:
      - exerciseId
      - activityType
      - items
      - generatedBy
      properties:
        exerciseId:
          type: string
          format: uuid
        activityType:
          type: string
        items:
          type: array
          items:
            $ref: '#/components/schemas/PersonalizedExerciseItem'
        generatedBy:
          type: string
          enum:
          - LLM
        expiresAt:
          type: string
          format: date-time
          nullable: true
    CreateVocabularyRequest:
      type: object
      required:
      - word
      - cefrLevelId
      - topicIds
      properties:
        word:
          type: string
          maxLength: 150
        phoneticIpa:
          type: string
          nullable: true
        meaningVi:
          type: string
          nullable: true
        meaningEn:
          type: string
          nullable: true
        partOfSpeech:
          type: string
          maxLength: 50
          nullable: true
        cefrLevelId:
          type: string
          format: uuid
        topicIds:
          type: array
          minItems: 1
          items:
            type: string
            format: uuid
        audioUrl:
          type: string
          nullable: true
        imageUrl:
          type: string
          nullable: true
    UpdateVocabularyRequest:
      allOf:
      - $ref: '#/components/schemas/CreateVocabularyRequest'
    CreateTopicRequest:
      type: object
      required:
      - name
      properties:
        name:
          type: string
          maxLength: 100
        description:
          type: string
          nullable: true
        iconUrl:
          type: string
          nullable: true
        parentTopicId:
          type: string
          format: uuid
          nullable: true
    UpdateTopicRequest:
      allOf:
      - $ref: '#/components/schemas/CreateTopicRequest'
    CreateQuizRequest:
      type: object
      required:
      - title
      properties:
        title:
          type: string
          maxLength: 255
        description:
          type: string
          nullable: true
        topicId:
          type: string
          format: uuid
          nullable: true
        cefrLevelId:
          type: string
          format: uuid
          nullable: true
    UpdateQuizRequest:
      allOf:
      - $ref: '#/components/schemas/CreateQuizRequest'
    CreateQuizQuestionRequest:
      type: object
      required:
      - questionText
      - questionType
      - correctAnswer
      - sortOrder
      properties:
        vocabularyId:
          type: string
          format: uuid
          nullable: true
        questionText:
          type: string
        questionType:
          type: string
          enum:
          - MULTIPLE_CHOICE
          - FILL_BLANK
          - MATCHING
        correctAnswer:
          type: string
        options:
          type: object
          additionalProperties: true
        sortOrder:
          type: integer
          minimum: 1
    UpdateQuizQuestionRequest:
      allOf:
      - $ref: '#/components/schemas/CreateQuizQuestionRequest'
    QuizQuestionAdminResponse:
      allOf:
      - $ref: '#/components/schemas/QuizQuestionResponse'
      - type: object
        properties:
          correctAnswer:
            type: string
          options:
            type: object
            additionalProperties: true
    GenerateAiContentRequest:
      type: object
      required:
      - contentType
      - vocabularyId
      - eventId
      properties:
        contentType:
          type: string
          enum:
          - EXAMPLE
          - EXPLANATION
          - MNEMONIC
          - STORY
          - QUIZ
        vocabularyId:
          type: string
          format: uuid
        topicId:
          type: string
          format: uuid
          nullable: true
        eventId:
          type: string
          format: uuid
          description: Client-generated idempotency key for one logical operation.
      additionalProperties: false
    AiContentScope:
      type: string
      enum:
      - REUSABLE
      - PERSONALIZED
    AiContentType:
      type: string
      enum:
      - EXAMPLE
      - EXPLANATION
      - MNEMONIC
      - STORY
      - QUIZ
      - PERSONALIZED_EXERCISE
    AiContentStatus:
      type: string
      enum:
      - PENDING_REVIEW
      - APPROVED
      - REJECTED
      - GENERATED
      - VALIDATED
      - FAILED
    AiGeneratedContentResponse:
      type: object
      required:
      - id
      - contentScope
      - contentType
      - generatedContent
      - modelUsed
      - status
      - createdAt
      properties:
        id:
          type: string
          format: uuid
        contentScope:
          $ref: '#/components/schemas/AiContentScope'
        contentType:
          $ref: '#/components/schemas/AiContentType'
        userId:
          type: string
          format: uuid
          nullable: true
        vocabularyId:
          type: string
          format: uuid
          nullable: true
        topicId:
          type: string
          format: uuid
          nullable: true
        generatedContent:
          type: object
          additionalProperties: true
        modelUsed:
          type: string
        status:
          $ref: '#/components/schemas/AiContentStatus'
        reviewNote:
          type: string
          nullable: true
          description: Review note/rejection reason stored on the AI content record
        reviewedBy:
          type: string
          format: uuid
          nullable: true
        reviewedAt:
          type: string
          format: date-time
          nullable: true
        expiresAt:
          type: string
          format: date-time
          nullable: true
        createdAt:
          type: string
          format: date-time
    RejectAiContentRequest:
      type: object
      required:
      - reason
      properties:
        reason:
          type: string
          minLength: 1
          maxLength: 500
          description: Stored as ai_generated_content.review_note and also copied to admin_audit_logs.details.reason
    AdminUserResponse:
      allOf:
      - $ref: '#/components/schemas/AuthUserSummary'
      - type: object
        properties:
          createdAt:
            type: string
            format: date-time
            nullable: true
          lastLoginAt:
            type: string
            format: date-time
            nullable: true
    AdminUserDetailResponse:
      allOf:
      - $ref: '#/components/schemas/AdminUserResponse'
      - type: object
        properties:
          failedLoginAttempts:
            type: integer
          lockedUntil:
            type: string
            format: date-time
            nullable: true
    AdminLearningStatisticsResponse:
      type: object
      properties:
        activeUsers:
          type: integer
        totalLearningSessions:
          type: integer
        averageAccuracyPercent:
          type: number
          format: double
        totalWordsStudied:
          type: integer
        totalLearningMinutes:
          type: integer
    AiUsageFeatureStat:
      type: object
      properties:
        feature:
          type: string
        requests:
          type: integer
        tokens:
          type: integer
          format: int64
        estimatedCost:
          type: number
          format: double
    AdminAiUsageStatisticsResponse:
      type: object
      properties:
        totalRequests:
          type: integer
        totalTokens:
          type: integer
          format: int64
        estimatedCost:
          type: number
          format: double
        blockedRequests:
          type: integer
        byFeature:
          type: array
          items:
            $ref: '#/components/schemas/AiUsageFeatureStat'
    AuditLogResponse:
      type: object
      properties:
        id:
          type: string
          format: uuid
        adminId:
          type: string
          format: uuid
        action:
          type: string
        targetTable:
          type: string
          nullable: true
        targetId:
          type: string
          format: uuid
          nullable: true
        details:
          type: object
          additionalProperties: true
        createdAt:
          type: string
          format: date-time
    PageTopicResponse:
      type: object
      properties:
        content:
          type: array
          items:
            $ref: '#/components/schemas/TopicResponse'
        page:
          type: integer
        size:
          type: integer
        totalElements:
          type: integer
          format: int64
        totalPages:
          type: integer
        hasNext:
          type: boolean
    PageVocabularyResponse:
      type: object
      properties:
        content:
          type: array
          items:
            $ref: '#/components/schemas/VocabularyResponse'
        page:
          type: integer
        size:
          type: integer
        totalElements:
          type: integer
          format: int64
        totalPages:
          type: integer
        hasNext:
          type: boolean
    PageQuizSummaryResponse:
      type: object
      properties:
        content:
          type: array
          items:
            $ref: '#/components/schemas/QuizSummaryResponse'
        page:
          type: integer
        size:
          type: integer
        totalElements:
          type: integer
          format: int64
        totalPages:
          type: integer
        hasNext:
          type: boolean
    PageWeakWordResponse:
      type: object
      properties:
        content:
          type: array
          items:
            $ref: '#/components/schemas/WeakWordResponse'
        page:
          type: integer
        size:
          type: integer
        totalElements:
          type: integer
          format: int64
        totalPages:
          type: integer
        hasNext:
          type: boolean
    PageXpLogResponse:
      type: object
      properties:
        content:
          type: array
          items:
            $ref: '#/components/schemas/XpLogResponse'
        page:
          type: integer
        size:
          type: integer
        totalElements:
          type: integer
          format: int64
        totalPages:
          type: integer
        hasNext:
          type: boolean
    PageLeaderboardEntryResponse:
      type: object
      properties:
        content:
          type: array
          items:
            $ref: '#/components/schemas/LeaderboardEntryResponse'
        page:
          type: integer
        size:
          type: integer
        totalElements:
          type: integer
          format: int64
        totalPages:
          type: integer
        hasNext:
          type: boolean
    PageNotificationResponse:
      type: object
      properties:
        content:
          type: array
          items:
            $ref: '#/components/schemas/NotificationResponse'
        page:
          type: integer
        size:
          type: integer
        totalElements:
          type: integer
          format: int64
        totalPages:
          type: integer
        hasNext:
          type: boolean
    PageAiGeneratedContentResponse:
      type: object
      properties:
        content:
          type: array
          items:
            $ref: '#/components/schemas/AiGeneratedContentResponse'
        page:
          type: integer
        size:
          type: integer
        totalElements:
          type: integer
          format: int64
        totalPages:
          type: integer
        hasNext:
          type: boolean
    PageAdminUserResponse:
      type: object
      properties:
        content:
          type: array
          items:
            $ref: '#/components/schemas/AdminUserResponse'
        page:
          type: integer
        size:
          type: integer
        totalElements:
          type: integer
          format: int64
        totalPages:
          type: integer
        hasNext:
          type: boolean
    PageAuditLogResponse:
      type: object
      properties:
        content:
          type: array
          items:
            $ref: '#/components/schemas/AuditLogResponse'
        page:
          type: integer
        size:
          type: integer
        totalElements:
          type: integer
          format: int64
        totalPages:
          type: integer
        hasNext:
          type: boolean
    EventIdRequest:
      type: object
      required:
      - eventId
      additionalProperties: false
      properties:
        eventId:
          type: string
          format: uuid
          description: Client-generated idempotency key for one logical operation.
    StartQuizAttemptRequest:
      type: object
      required:
      - eventId
      additionalProperties: false
      properties:
        eventId:
          type: string
          format: uuid
          description: Client-generated idempotency key for one logical operation.
    PushTokenRequest:
      type: object
      required:
      - platform
      - pushToken
      additionalProperties: false
      properties:
        platform:
          type: string
          enum:
          - ANDROID
        pushToken:
          type: string
          minLength: 1
          maxLength: 4096
    NotificationPreferencesResponse:
      type: object
      required:
      - pushEnabled
      - reviewReminderEnabled
      - dailyPlanEnabled
      - streakReminderEnabled
      - preferredStudyTime
      properties:
        pushEnabled:
          type: boolean
        reviewReminderEnabled:
          type: boolean
        dailyPlanEnabled:
          type: boolean
        streakReminderEnabled:
          type: boolean
        preferredStudyTime:
          type: string
          pattern: ^(?:[01]\d|2[0-3]):[0-5]\d$
    UpdateNotificationPreferencesRequest:
      allOf:
      - $ref: '#/components/schemas/NotificationPreferencesResponse'
```

## 3. Important implementation notes

### Learning attempt

Request contains only the answer-quality signal:

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

Client must not send `isCorrect`.

Backend derives:

```text
0–2 → incorrect
3–5 → correct
```

The response may return `isCorrect` because it is a derived result useful to the client.

### Collection conventions

```text
GET /goals
GET /cefr-levels
→ [...]

GET /topics
GET /vocabulary
GET /quizzes
GET /progress/weak-words
GET /admin/users
...
→ {content, page, size, totalElements, totalPages, hasNext}

GET /learning/reviews
→ {items, count}
```

### Refresh Token

V1:

```text
expiry + revoke
```

Future:

```text
rotation + reuse detection
```

### Validation

Validate the YAML with Swagger Editor, Redocly CLI, or another OpenAPI 3.1 validator before code generation.

## 4. Spring Boot mapping

Recommended controllers:

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

Recommended stack:

```text
Spring Boot
Spring Security
Spring Data JPA
Hibernate
Flyway
springdoc-openapi
PostgreSQL
```

## 5. Next step

```text
OpenAPI YAML
    ↓
Validate specification
    ↓
Swagger UI
    ↓
Create Spring Boot DTOs
    ↓
Controllers
    ↓
Services
    ↓
Repositories / JPA
    ↓
Integration Tests
```


## 4. AI Review persistence

For:

```http
POST /admin/ai-content/{contentId}/reject
```

the request:

```json
{
  "reason": "Incorrect example"
}
```

maps to:

```text
reason
→ ai_generated_content.review_note
→ admin_audit_logs.details.reason
```

The response exposes:

```text
reviewNote
```

so the Admin Web can render the rejection note directly on AI Content Detail.


## 5. Idempotency Contract

```text
eventId
→ one logical mutation

Same eventId + same endpoint + same payload
→ replay stored response

Same eventId + different endpoint/payload
→ 409 IDEMPOTENCY_KEY_REUSE
```

Applicable endpoints:

```text
POST /learning/attempts
POST /quiz-attempts/{attemptId}/answers
POST /quiz-attempts/{attemptId}/complete
```
