# AI Personalization Specification v1.3 — English AI Coach

**Project:** English AI Coach  
**Document:** AI Personalization Specification  
**Version:** 1.3  
**Status:** APPROVED BASELINE  
**Date:** 2026-08-31  

**Related documents:**
- `English_AI_Coach_SRS_v1.2.md`
- `English_AI_Coach_Database_Schema_v1.6.md`
- `English_AI_Coach_System_Architecture_v1.3.md`

---

# 1. Purpose

Tài liệu này đặc tả **AI Personalization Engine** của English AI Coach.

Mục tiêu là chốt rõ:

- Dữ liệu đầu vào.
- User Learning Profile.
- Adaptive Testing.
- Extended SM-2.
- Weak Word Detection.
- Forgetting Risk.
- Vocabulary Recommendation.
- Topic Recommendation.
- Difficulty Adjustment.
- New/Review balancing.
- Personalized Daily Plan.
- Workload Guard.
- Explainability.
- Algorithm Versioning.
- V1 Rule-based/Heuristic.
- V2 Machine Learning.
- Boundary giữa Personalization và LLM.
- Evaluation và Acceptance Criteria.

Tài liệu này **không định nghĩa REST API cụ thể**. API được thiết kế sau khi Personalization Specification được chốt.

---

# 2. Scope

## 2.1. V1 — Core Personalization

V1 không train Machine Learning model.

Sử dụng:

```text
Deterministic Algorithm
+
Rule-based
+
Heuristic
```

Các thành phần:

```text
Adaptive Testing
Extended SM-2
Weak Word Detection
Forgetting Risk Estimation
Vocabulary Recommendation
Topic Recommendation
Difficulty Adjustment
New/Review Ratio
Personalized Daily Plan
Workload Guard
```

---

## 2.2. V1+ — AI Content

LLM được sử dụng cho các chức năng hỗ trợ nội dung:

```text
AI Example
AI Explanation
AI Story
AI Mnemonic
AI Quiz
AI Personalized Exercise
```

LLM không trực tiếp quyết định:

```text
next_review_at
SRS interval
daily workload
```

Các quyết định đó thuộc Personalization Engine.

---

## 2.3. V2 — Machine Learning

Sau khi thu thập đủ historical learning data:

```text
Forgetting Prediction
ML Recommendation
Difficulty Prediction
Error Classification
Notification Timing
Progress Prediction
```

---

# 3. Design Principles

## DP-01 — Personalization là Core Domain

Personalization Engine là domain logic cốt lõi.

```text
Mobile
   ↓
Backend
   ↓
Personalization Engine
```

---

## DP-02 — Không phụ thuộc LLM

V1 phải hoạt động đầy đủ mà không cần LLM.

```text
Personalization
    ↓
Rule / Algorithm
```

LLM là enhancement layer.

---

## DP-03 — Historical data phải được giữ

```text
Current State
    ↓
user_vocabulary_progress

Historical Events
    ↓
session_attempts
quiz_attempt_answers
learning_sessions
```

Không được chỉ lưu kết quả cuối cùng mà làm mất lịch sử.

---

## DP-04 — Deterministic trước, ML sau

```text
V1
Rule-based

V2
Rule + ML
```

V1 phải dễ giải thích và tái hiện.

---

## DP-05 — Recommendation phải có reason

Mỗi recommendation nên có nội bộ:

```text
item_id
score
reason_code
reason_details
algorithm_version
```

Ví dụ:

```text
abandon
score = 0.91
reason_code = HIGH_FORGETTING_RISK
algorithm_version = recommendation-v1
```

---

## DP-06 — Workload phải được giới hạn

Personalization không được tăng/giảm workload quá mạnh chỉ vì một ngày có kết quả bất thường.

Mọi raw target phải đi qua:

```text
Workload Guard
```

---

# 4. User Learning Profile

Personalization Engine tổng hợp:

```text
User Profile
+
Goals
+
CEFR
+
Daily Learning Time
+
Vocabulary Knowledge
+
SRS State
+
Learning History
+
Quiz History
+
Weak Words
+
Forgetting Risk
+
Topic History
+
Completion Behavior
```

Conceptual model:

```text
USER LEARNING PROFILE
│
├── CEFR
├── GOALS
├── DAILY TIME
├── VOCABULARY KNOWLEDGE
├── SRS STATE
├── WEAK WORDS
├── FORGETTING RISK
├── TOPIC PREFERENCES
├── LEARNING BEHAVIOR
└── PROGRESS
```

---

# 5. Input Data

## 5.1. Static Inputs

```text
current_cefr_level
goals
daily_learning_minutes
timezone
```

Nguồn:

```text
user_profiles
user_goals
cefr_levels
```

---

## 5.2. Vocabulary Metadata

```text
word
part_of_speech
cefr_level
topics
```

Nguồn:

```text
vocabulary
vocabulary_topics
```

---

## 5.3. Historical Learning Events

```text
attempted_at
is_correct
response_time_ms
answer_quality
attempt_type
```

Nguồn:

```text
session_attempts
```

---

## 5.4. Current Learning State

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
last_quality
version
```

Nguồn:

```text
user_vocabulary_progress
```

---

## 5.5. Quiz Data

```text
score
is_correct
response_time_ms
answered_at
```

Nguồn:

```text
quiz_attempts
quiz_attempt_answers
```

---

# 6. Adaptive Testing

## 6.1. Objective

Ước lượng trình độ từ vựng ban đầu của user.

Output:

```text
A1
A2
B1
B2
C1
C2
```

---

## 6.2. V1 Strategy

Sử dụng rule-based adaptive difficulty.

```text
Current Difficulty
       ↓
User Answer
       ↓
Correct?
  ┌────┴─────┐
 YES         NO
  ↓           ↓
Increase    Decrease
difficulty  difficulty
```

---

## 6.3. Difficulty Representation

Có thể map câu hỏi với:

```text
A1
A2
B1
B2
C1
C2
```

hoặc:

```text
EASY
MEDIUM
HARD
```

Nếu dùng CEFR, câu hỏi phải có `cefr_level_id`.

---

## 6.4. Stopping Rule

Test kết thúc khi:

```text
minimum_questions reached
AND
ability estimate is sufficiently stable
```

hoặc đạt maximum questions.

Baseline đề xuất:

```text
minimum_questions = 20
maximum_questions = 50
```

Giá trị configurable.

---

## 6.5. Output

```text
assessment_level
score
correct_answers
total_questions
```

Lưu:

```text
user_level_assessments
```

---

# 7. Extended SM-2 v1

## 7.1. Objective

Xác định thời điểm user nên ôn lại vocabulary.

> **Extended SM-2 v1** dùng SM-2 làm baseline và thêm một điều chỉnh nhẹ theo response time, đồng thời áp interval bounds.

---

## 7.2. Inputs

```text
q = answer_quality
EF_old = current ease_factor
I_old = current interval_days
R_old = current repetitions
t = response_time_ms
```

---

## 7.3. Answer Quality

MVP sử dụng:

```text
0 = Complete blackout
1 = Incorrect, very difficult
2 = Incorrect, remembered after strong hint
3 = Correct with significant difficulty
4 = Correct with hesitation
5 = Correct and easy/immediate
```

---

## 7.4. Ease Factor Formula

SM-2 baseline:

```text
EF_new =
max(
    1.30,
    EF_old + 0.1
    - (5 - q) × (0.08 + (5 - q) × 0.02)
)
```

---

## 7.5. Initial State

Vocabulary chưa từng được học:

```text
status = NEW
repetitions = 0
interval_days = 0
ease_factor = 2.50
next_review_at = NULL
```

Baseline:

```text
INITIAL_EASE_FACTOR = 2.50
MIN_EASE_FACTOR = 1.30
```

---

## 7.6. Failed Review

Nếu:

```text
q < 3
```

thì:

```text
repetitions_new = 0
interval_new = 1 day
next_review_at = now + 1 day
```

`ease_factor` vẫn được cập nhật bằng công thức ở trên nhưng không được thấp hơn:

```text
1.30
```

---

## 7.7. Successful Review

Nếu:

```text
q >= 3
```

thì:

```text
repetitions_new = repetitions_old + 1
```

Base interval:

```text
if repetitions_new == 1:
    base_interval = 1

else if repetitions_new == 2:
    base_interval = 6

else:
    base_interval =
        round(I_old × EF_new)
```

---

## 7.8. Response Time Extension

Response time chỉ là signal phụ.

Reference time:

```text
T_ref = 3000 ms
```

Time factor:

```text
time_factor =
clamp(
    T_ref / max(t, 1000),
    0.85,
    1.10
)
```

Sau đó:

```text
interval_new =
round(base_interval × time_factor)
```

Response time không được phép làm interval thay đổi quá mạnh.

---

## 7.9. Interval Bounds

```text
MIN_INTERVAL = 1 day
MAX_INTERVAL = 180 days
```

Cuối cùng:

```text
interval_new =
clamp(
    interval_new,
    1,
    180
)
```

---

## 7.10. Next Review

```text
next_review_at =
now + interval_new days
```

---

## 7.11. Complete Formula

### q < 3

```text
EF_new =
max(
    1.30,
    EF_old + 0.1
    - (5 - q) × (0.08 + (5 - q) × 0.02)
)

R_new = 0
I_new = 1
```

### q >= 3

```text
EF_new =
max(
    1.30,
    EF_old + 0.1
    - (5 - q) × (0.08 + (5 - q) × 0.02)
)

R_new = R_old + 1
```

Then:

```text
if R_new == 1:
    base_interval = 1

else if R_new == 2:
    base_interval = 6

else:
    base_interval = round(I_old × EF_new)
```

Response time:

```text
time_factor =
clamp(
    3000 / max(response_time_ms, 1000),
    0.85,
    1.10
)
```

Final:

```text
I_new =
clamp(
    round(base_interval × time_factor),
    1,
    180
)
```

---

## 7.12. Worked Example

Input:

```text
EF_old = 2.50
I_old = 6
R_old = 2
q = 4
response_time_ms = 2500
```

Ease Factor:

```text
EF_new = 2.50
```

Repetitions:

```text
R_new = 3
```

Base interval:

```text
6 × 2.50 = 15
```

Time factor:

```text
3000 / 2500 = 1.20

clamp(1.20, 0.85, 1.10)
= 1.10
```

Final:

```text
round(15 × 1.10)
= 17 days
```

Result:

```text
interval_days = 17
next_review_at = now + 17 days
```

---

# 8. Weak Word Detection

## 8.1. Objective

Xác định vocabulary mà user đang gặp khó khăn.

---

## 8.2. Features

```text
recent_accuracy
overall_accuracy
incorrect_count
correct_count
response_time
answer_quality
recent_failures
```

---

## 8.3. Weakness Score

Conceptual:

```text
weakness_score =
    w1 × error_rate
  + w2 × normalized_response_time
  + w3 × low_quality_rate
  + w4 × recent_failure_rate
```

Trong đó:

```text
w1 + w2 + w3 + w4 = 1
```

Legacy weighted form is non-normative for V1. V1 uses the deterministic rules in the Reconciled V1 Algorithm Baseline below.

---

## 8.4. V1 Baseline Rule

```text
IF attempts >= 3
AND accuracy < 60%
THEN weak = true
```

Có thể bổ sung:

```text
OR recent consecutive failures >= 2
```

---

## 8.5. Output

```text
weakness_score
weak
reason_codes
```

Reason codes:

```text
LOW_ACCURACY
RECENT_FAILURES
SLOW_RECALL
LOW_ANSWER_QUALITY
```

---

# 9. Forgetting Risk Estimation

## 9.1. Objective

Ước lượng nguy cơ user không nhớ vocabulary ở lần review tiếp theo.

V1 dùng heuristic.

Thuật ngữ chính thức:

> **Forgetting Risk Estimation**

Không gọi là ML prediction.

---

## 9.2. Inputs

```text
days_since_last_review
interval_days
historical_accuracy
incorrect_count
last_quality
repetitions
response_time
```

---

## 9.3. Conceptual Formula

```text
forgetting_risk =
    f(
        recency,
        performance,
        difficulty,
        repetition,
        response_time
    )
```

Output:

```text
risk_score ∈ [0, 1]
risk_level ∈ {LOW, MEDIUM, HIGH}
```

---

## 9.4. Baseline Rules

Ví dụ:

```text
HIGH:
accuracy < 60%
AND days_since_last_review >= interval_days

MEDIUM:
accuracy 60–80%
OR moderately overdue

LOW:
accuracy > 80%
AND not overdue
```

Threshold phải configurable.

---

# 10. Vocabulary Recommendation

## 10.1. Objective

Chọn vocabulary phù hợp nhất cho user học tiếp.

---

## 10.2. Candidate Pool

Chọn từ từ:

```text
active vocabulary
+
target CEFR
+
goal-relevant topics
+
not mastered
+
not already overexposed
```

Loại bỏ:

```text
inactive vocabulary
out-of-scope CEFR
duplicate candidates
mandatory-new candidates already mastered
```

Review items được xử lý trước New Words.

---

## 10.3. V1 Score

```text
recommendation_score =  # FUTURE/illustrative only; NOT normative V1
    w1 × goal_relevance
  + w2 × topic_relevance
  + w3 × cefr_fit
  + w4 × weakness_need
  + w5 × forgetting_risk
  + w6 × novelty
```

V1 dùng deterministic/content-based scoring.

---

## 10.4. Priority

```text
1. Due Review
2. High Forgetting Risk
3. Weak Words
4. Goal-relevant New Words
5. Topic-relevant New Words
6. General New Words
```

---

# 11. Topic Recommendation

## 11.1. Objective

Chọn topic nên ưu tiên tiếp theo.

---

## 11.2. V1 Inputs

```text
primary goal
current CEFR
topic completion
recent topic accuracy
recent study frequency
```

---

## 11.3. V1 Rules

Ví dụ:

```text
Goal = BUSINESS
CEFR = B1
```

ưu tiên:

```text
Business
Finance
Communication
Office
```

Topic phải tương thích với vocabulary availability.

---

## 11.4. Topic Diversity

Không ưu tiên cùng một topic quá lâu.

Ví dụ:

```text
Day 1 → Business
Day 2 → Finance
Day 3 → Communication
Day 4 → Business
```

Mục tiêu:

```text
avoid single-topic saturation
```

---

# 12. Difficulty Adjustment

## 12.1. Objective

Điều chỉnh workload theo khả năng tiếp thu.

---

## 12.2. Signals

```text
completion_rate
accuracy
study_time
recent_failures
abandonment_rate
```

---

## 12.3. Raw Workload Rule

Ví dụ:

```text
IF completion_rate >= 90%
AND accuracy >= 85%
    → increase raw target

IF completion_rate < 70%
OR accuracy < 60%
    → decrease raw target

ELSE
    → maintain raw target
```

Raw target không được dùng trực tiếp.

---

# 13. Workload Guard

## 13.1. Objective

Giới hạn tốc độ tăng/giảm workload.

Workload được điều chỉnh dựa trên **target gần nhất của user**, không dựa vào một baseline cứng.

Input:

```text
previous_target
raw_target
MAX_DAILY_WORKLOAD_INCREASE_PERCENT
MAX_DAILY_WORKLOAD_DECREASE_PERCENT
```

---

## 13.2. Formula

```text
upper_bound =
previous_target × (1 + max_increase)

lower_bound =
previous_target × (1 - max_decrease)
```

Final target:

```text
final_target =
clamp(
    raw_target,
    lower_bound,
    upper_bound
)
```

Khi cần số nguyên:

```text
final_target = round(final_target)
```

---

## 13.3. Baseline Configuration

```text
MAX_DAILY_WORKLOAD_INCREASE_PERCENT = 20%
MAX_DAILY_WORKLOAD_DECREASE_PERCENT = 30%
```

---

## 13.4. Increase Example

```text
previous_target = 10
raw_target = 15
max increase = 20%
```

Upper bound:

```text
10 × 1.20 = 12
```

Final:

```text
final_target = 12
```

Không được tăng:

```text
10 → 15
```

---

## 13.5. Decrease Example

```text
previous_target = 10
raw_target = 5
max decrease = 30%
```

Lower bound:

```text
10 × 0.70 = 7
```

Final:

```text
final_target = 7
```

---

## 13.6. Auditability

Personalization engine nên giữ internally:

```text
previous_target
raw_target
final_target
adjustment_reason
algorithm_version
```

Ví dụ:

```text
previous_target = 10
raw_target = 15
final_target = 12
reason = HIGH_PERFORMANCE
```

---

# 14. New / Review Ratio

## 14.1. Objective

Cân bằng:

```text
New Words
+
Review Words
```

---

## 14.2. Inputs

```text
available_daily_minutes
due_review_count
weak_word_count
high_risk_count
current_accuracy
completion_rate
previous_new_target
previous_review_target
```

---

## 14.3. Baseline Examples

Normal:

```text
10 New
15 Review
```

Review backlog cao:

```text
5 New
25 Review
```

Retention cao:

```text
12 New
12 Review
```

Các giá trị cuối cùng phải chịu Workload Guard và Time Budget.

---

# 15. Time Budget

Daily Plan phải tôn trọng:

```text
daily_learning_minutes
```

Conceptual:

```text
estimated_new_time
+
estimated_review_time
+
estimated_quiz_time
<=
available_daily_minutes
```

Nếu vượt:

```text
Reduce New Words
before reducing mandatory Reviews
```

Priority:

```text
Due Review
>
High-risk Review
>
Weak Words
>
New Words
>
Optional Quiz
```

---

# 16. Personalized Daily Plan

## 16.1. Objective

Tạo learning plan cho một ngày.

---

## 16.2. Inputs

```text
CEFR
Goals
Daily Minutes
SRS Due Words
Weak Words
Forgetting Risk
Recommended Topics
Difficulty
Completion Rate
Accuracy
Previous Workload
```

---

## 16.3. Output

```text
new_words_target
review_words_target
quiz_target
estimated_minutes
```

---

## 16.4. Generation Algorithm

```text
STEP 1
Load user profile

STEP 2
Load previous daily target

STEP 3
Get due-review queue

STEP 4
Calculate weak words

STEP 5
Calculate forgetting risk

STEP 6
Generate new-word candidates

STEP 7
Recommend topics

STEP 8
Rank new-word candidates

STEP 9
Determine raw workload

STEP 10
Apply Workload Guard

STEP 11
Apply New/Review balancing

STEP 12
Apply Time Budget

STEP 13
Build Daily Plan

STEP 14
Persist Daily Plan
```

---

# 17. Daily Plan Priority

```text
                DAILY PLAN
                     │
       ┌─────────────┼─────────────┐
       ▼             ▼             ▼
     REVIEW         NEW           QUIZ
       │             │             │
       ▼             ▼             ▼
    Due/Risk     Ranked Words   Weakness Practice
       │             │             │
       └─────────────┼─────────────┘
                     ▼
               Time Allocation
                     │
                     ▼
              Workload Guard
                     │
                     ▼
                DAILY_PLANS
```

---

# 18. Full End-to-End Personalization Flow

```text
                    USER
                      │
                      ▼
              Learning / Review / Quiz
                      │
                      ▼
              SESSION_ATTEMPTS
                      │
                      ▼
             LEARNING ANALYTICS
                      │
        ┌─────────────┼─────────────┐
        ▼             ▼             ▼
    Weakness      Forgetting     Performance
    Detection        Risk          Analysis
        │             │             │
        └─────────────┼─────────────┘
                      ▼
             Recommendation Engine
                      │
          ┌───────────┴───────────┐
          ▼                       ▼
     Vocabulary                Topic
     Recommendation          Recommendation
          │                       │
          └───────────┬───────────┘
                      ▼
              Difficulty Adjustment
                      │
                      ▼
               Raw Workload
                      │
                      ▼
                Workload Guard
                      │
                      ▼
               New/Review Ratio
                      │
                      ▼
              Time Budget Check
                      │
                      ▼
               DAILY_PLANS
                      │
                      ▼
                     USER
```

---

# 19. Recommendation Explainability

Internal result:

```text
item_id
score
reason_code
reason_details
algorithm_version
```

Examples:

```text
abandon
score = 0.91
reason_code = HIGH_FORGETTING_RISK
```

```text
negotiate
score = 0.84
reason_code = GOAL_RELEVANT
```

V1 có thể dùng reason code + template.

V2 có thể dùng LLM để tạo natural-language explanation.

---

# 20. Personalization Constraints

Engine phải tôn trọng:

```text
user daily learning time
user CEFR
user goals
vocabulary availability
SRS due items
workload limits
content availability
```

Không recommend mandatory new content:

```text
inactive vocabulary
duplicate items
already mastered words
unsupported CEFR items
invalid content
```

---

# 21. Configuration

Thresholds phải được centralized.

Suggested baseline:

```text
SRS_INITIAL_EASE_FACTOR = 2.50
SRS_MIN_EASE_FACTOR = 1.30
SRS_MIN_INTERVAL_DAYS = 1
SRS_MAX_INTERVAL_DAYS = 180

SRS_RESPONSE_TIME_REFERENCE_MS = 3000
SRS_TIME_FACTOR_MIN = 0.85
SRS_TIME_FACTOR_MAX = 1.10

WEAK_WORD_MIN_ATTEMPTS = 3
WEAK_WORD_ACCURACY_THRESHOLD = 0.60
WEAK_WORD_RECENT_FAILURE_THRESHOLD = 2

FORGETTING_HIGH_THRESHOLD = 0.70
FORGETTING_MEDIUM_THRESHOLD = 0.40

HIGH_PERFORMANCE_ACCURACY = 0.85
LOW_PERFORMANCE_ACCURACY = 0.60

HIGH_COMPLETION_RATE = 0.90
LOW_COMPLETION_RATE = 0.70

MAX_DAILY_WORKLOAD_INCREASE_PERCENT = 0.20
MAX_DAILY_WORKLOAD_DECREASE_PERCENT = 0.30

PLACEMENT_TEST_MIN_QUESTIONS = 20
PLACEMENT_TEST_MAX_QUESTIONS = 50
```

Các giá trị là baseline để implementation và testing; có thể hiệu chỉnh sau khi có dữ liệu.

---

# 22. Personalization Service Boundaries

Conceptual services:

```text
SrsService
WeakWordService
ForgettingRiskService
VocabularyRecommendationService
TopicRecommendationService
DifficultyAdjustmentService
DailyPlanService
LearningAnalyticsService
```

Facade:

```text
PersonalizationService
```

dùng để orchestration.

---

# 23. Suggested Domain Objects

```text
UserLearningProfile

ReviewCandidate

SrsCalculationResult

WeakWordResult

ForgettingRiskResult

RecommendationResult

DifficultyAdjustment

WorkloadAdjustment

DailyLearningPlan
```

Ví dụ:

```text
SrsCalculationResult
├── oldEaseFactor
├── newEaseFactor
├── oldInterval
├── newInterval
├── oldRepetitions
├── newRepetitions
├── answerQuality
├── responseTimeMs
├── nextReviewAt
└── algorithmVersion
```

---

# 24. V1 Recommendation Engine

Không sử dụng Collaborative Filtering.

Sử dụng:

```text
Rule-based
+
Content-based Ranking
```

Input:

```text
user profile
goals
CEFR
learning history
vocabulary metadata
```

Output:

```text
ranked vocabulary candidates
```

---

# 25. V2 Recommendation Engine

Sau khi có đủ data:

```text
Rule-based
+
Content-based
+
Collaborative Filtering
+
ML
```

Potential features:

```text
user_id
word_id
topic
CEFR
historical accuracy
response time
review frequency
completion rate
goal relevance
```

Potential targets:

```text
probability user completes/reviews item
```

hoặc:

```text
expected learning benefit
```

---

# 26. V1 Forgetting Risk vs V2 Prediction

## V1

```text
Rule / Heuristic
```

Dựa trên:

```text
recency
accuracy
interval
quality
repetitions
```

Output:

```text
LOW
MEDIUM
HIGH
```

## V2

```text
Features
   ↓
ML Model
   ↓
P(correct next review)
```

---

# 27. Personalization + LLM Boundary

LLM không quyết định trực tiếp:

```text
next_review_at
interval_days
daily target
new/review ratio
```

Personalization Engine quyết định.

LLM có thể:

```text
generate example
generate explanation
generate story
generate mnemonic
generate quiz
generate personalized exercise
generate natural-language feedback
```

---

# 28. Personalized Exercise

Input từ Personalization Engine:

```text
weak words
CEFR
goal
recent mistakes
activity type
```

Flow:

```text
Personalization Engine
      ↓
Select weak vocabulary
      ↓
Select exercise type
      ↓
AI Content Service
      ↓
Budget Guard
      ↓
LLM
      ↓
Schema Validation
      ↓
Safety Filter
      ↓
User
```

Không yêu cầu:

```text
Admin Review
```

cho từng personalized result.

Nếu cần lưu tạm:

```text
ai_generated_content
content_scope = PERSONALIZED
user_id = target user
expires_at = ...
```

---

# 29. Reusable AI Content

Áp dụng cho:

```text
EXAMPLE
EXPLANATION
MNEMONIC
STORY
REUSABLE QUIZ
```

Flow:

```text
AI
 ↓
Validation
 ↓
PENDING_REVIEW
 ↓
Admin Review
 ↓
APPROVED
 ↓
Cache
 ↓
Reuse
```

---

# 30. Learning Event Model

Mỗi learning event cần ít nhất:

```text
user
vocabulary
timestamp
activity
result
response_time
quality
```

Ví dụ:

```text
user_id = U01
word = abandon
activity = WORD_RECALL
correct = false
response_time = 4200
quality = 1
timestamp = ...
```

---

# 31. Idempotency & Duplicate Attempts

Backend phải giảm nguy cơ một logical event bị ghi nhiều lần khi client retry.

V1 sử dụng duy nhất `eventId` do client tạo trong request body cho logical mutation. Không dùng idempotency HTTP header. Backend lưu/replay qua `idempotency_keys` với retention 30 ngày.

Điều này hữu ích khi:

```text
network unstable
mobile retry
user double-submit
```

---

# 32. Concurrency

Đối tượng có khả năng concurrent update:

```text
user_vocabulary_progress
streaks
```

Dùng:

```text
Optimistic Locking
version
@Version
```

Database:

```text
version BIGINT NOT NULL DEFAULT 0
```

Flow:

```text
Read
 ↓
Calculate
 ↓
Update with expected version
 ↓
Conflict?
 ┌──┴─────┐
 NO      YES
 │         │
 ▼         ▼
Commit   Reload / Retry / Reject
```

Không được silently overwrite state mới hơn.

---

# 33. Failure Handling

## Personalization failure

Core learning vẫn hoạt động.

Fallback:

```text
SRS due queue
+
default/static plan
```

## LLM failure

```text
Use cached approved content
```

hoặc:

```text
Return standard exercise
```

Core SRS/progress không phụ thuộc LLM.

---

# 34. Performance

Daily Plan V1 phải tránh gọi LLM.

Ưu tiên:

```text
Rule-based computation
```

LLM generation ưu tiên:

```text
pre-generate
cache
background job
```

khi nội dung có thể tái sử dụng.

---

# 35. Data Access

Personalization cần truy cập:

```text
users
user_profiles
user_goals
cefr_levels
topics
vocabulary
user_vocabulary_progress
learning_sessions
session_attempts
daily_plans
quiz_attempts
quiz_attempt_answers
```

Hai nguồn quan trọng nhất:

```text
user_vocabulary_progress
session_attempts
```

---

# 36. Security

Personalization chỉ được truy cập learning data của chính user.

```text
User A
  ↓
Only User A data
```

Admin chỉ xem aggregated/authorized statistics theo permission.

---

# 37. Privacy

Learning history là behavioral data.

Cần:

```text
authorization
access control
data minimization
pseudonymization for ML datasets where appropriate
```

Không đưa learning history cá nhân của một user sang user khác ngoài các dữ liệu đã được tổng hợp/ẩn danh phù hợp.

---

# 38. Algorithm Versioning

Tất cả algorithm nên có version:

```text
SRS_VERSION = "sm2-ext-v1"
FORGETTING_RISK_VERSION = "heuristic-v1"
WEAK_WORD_VERSION = "rule-v1"
RECOMMENDATION_VERSION = "rule-v1"
DIFFICULTY_VERSION = "rule-v1"
DAILY_PLAN_VERSION = "planner-v1"
```

Khi thay đổi algorithm, có thể biết user đã nhận quyết định từ version nào.

---

# 39. Evaluation Metrics

## SRS

```text
review accuracy
retention rate
overdue rate
```

## Weak Word

```text
precision of flagged weak words
subsequent improvement rate
```

## Recommendation

```text
recommendation acceptance rate
completion rate
next-session engagement
```

## Daily Plan

```text
completion rate
partial completion rate
average learning time
```

## Difficulty

```text
accuracy stability
completion stability
drop-off rate
```

---

# 40. Future ML Evaluation

## Forgetting Prediction

```text
Accuracy
Precision
Recall
F1
ROC-AUC
Calibration
```

Calibration quan trọng vì prediction được dùng để ưu tiên review.

## Recommendation

```text
Precision@K
Recall@K
NDCG@K
Completion Rate
Learning Gain
```

---

# 41. A/B Testing Future

Có thể so sánh:

```text
Group A
Rule-based

Group B
ML-assisted
```

Metrics:

```text
Retention
Completion
Learning Time
Engagement
```

Chỉ triển khai sau khi có đủ data và cơ chế experiment.

---

# 42. Personalization Decision Tree

```text
START
  │
  ▼
Are there due reviews?
  │
 ┌┴────────┐
YES       NO
 │         │
 ▼         ▼
Review    Need new words?
first       │
            ▼
        Generate candidates
            │
            ▼
       Goal/Topic relevance
            │
            ▼
       CEFR compatibility
            │
            ▼
       Weakness / Risk score
            │
            ▼
       Difficulty adjustment
            │
            ▼
       Workload Guard
            │
            ▼
       New/Review balancing
            │
            ▼
       Time Budget
            │
            ▼
        Daily Plan
```

---

# 43. Normal Performance Example

Input:

```text
CEFR = A2
Goal = TRAVEL
Daily time = 20 minutes

Due reviews = 18
Weak words = 6
High-risk words = 4
Completion rate = 65%
Accuracy = 58%

Previous new-word target = 10
Previous review target = 15
```

Raw engine may determine:

```text
New words raw target = 8
Review raw target = 24
```

Time and workload rules are then applied.

Because:

```text
low accuracy
+
low completion
+
high review backlog
```

the final plan should prioritize review.

Possible final plan:

```text
New words = 7
Review words = 18
Quiz = 5
```

with estimated time:

```text
≈ 20 minutes
```

Exact final values depend on the configured time-per-item model.

---

# 44. High-performance Example

Input:

```text
CEFR = B1
Daily time = 30 minutes

Due reviews = 5
Weak words = 2
Completion rate = 95%
Accuracy = 91%

Previous new-word target = 10
```

Raw engine may produce:

```text
raw_new_target = 15
```

But Workload Guard limits increase to 20%:

```text
10 × 1.20 = 12
```

Therefore:

```text
final_new_target = 12
```

Example final plan:

```text
New words = 12
Review words = 8
Quiz = 10
Estimated time ≈ 30 minutes
```

The displayed values are valid only if the configured time-per-item model fits within the 30-minute budget.

---

# 45. Personalization Output

The Daily Plan generator should keep internally:

```text
plan_date
new_words_target
review_words_target
quiz_target
estimated_minutes

previous_new_target
raw_new_target
final_new_target

previous_review_target
raw_review_target
final_review_target

recommendation_version
difficulty_version
srs_version
daily_plan_version
```

Public API may expose only fields required by the client.

---

# 46. Personalization Service Execution

Conceptual:

```text
LearningService
      │
      ▼
Record Attempt
      │
      ▼
SrsService
      │
      ▼
Update Progress
      │
      ▼
LearningAnalyticsService
      │
      ├── Weakness
      ├── Forgetting Risk
      └── Performance
      │
      ▼
RecommendationService
      │
      ▼
DifficultyAdjustmentService
      │
      ▼
WorkloadGuard
      │
      ▼
NewReviewBalancer
      │
      ▼
DailyPlanService
```

---

# 47. Synchronous vs Background

## Synchronous

```text
Record Attempt
SRS Update
Read Today's Plan
Basic weakness/risk update
```

## Background

```text
Daily Plan pre-generation
Reusable AI content generation
AI usage aggregation
ML feature generation
Notification sending
```

---

# 48. Personalization Version Migration

Khi thay đổi algorithm:

```text
v1 Rule
    ↓
v2 Rule + ML
```

Không thay đổi behavior đột ngột cho tất cả user nếu chưa đánh giá.

Có thể dùng:

```text
feature flag
A/B testing
gradual rollout
```

---

# 49. Acceptance Criteria — V1

```text
[ ] Adaptive Testing hoạt động.
[ ] CEFR được lưu sau assessment.
[ ] Extended SM-2 có công thức cụ thể.
[ ] SRS tính new_ease_factor.
[ ] SRS tính new_interval.
[ ] Response time extension hoạt động trong giới hạn.
[ ] Interval bị giới hạn 1–180 ngày.
[ ] Progress được cập nhật sau attempt.
[ ] Weak words được phát hiện.
[ ] Forgetting Risk được tính.
[ ] Vocabulary recommendation hoạt động.
[ ] Topic recommendation hoạt động.
[ ] Difficulty adjustment hoạt động.
[ ] Workload Guard giới hạn tăng tối đa 20%.
[ ] Workload Guard giới hạn giảm tối đa 30%.
[ ] New/Review balancing hoạt động.
[ ] Daily Plan tôn trọng daily learning minutes.
[ ] Daily Plan ưu tiên review backlog.
[ ] Recommendation có reason code nội bộ.
[ ] Algorithm version được xác định.
[ ] Concurrent update được bảo vệ bằng optimistic locking.
[ ] Personalization không phụ thuộc LLM.
[ ] Có fallback khi AI/LLM không khả dụng.
```

---

# 50. Definition of Done

Personalization được xem là hoàn thành khi:

```text
Requirement
   ↓
Algorithm Specification
   ↓
Unit Tests
   ↓
Boundary Tests
   ↓
Integration Tests
   ↓
Concurrency Tests
   ↓
Database Transaction Tests
   ↓
API Integration
   ↓
Mobile Integration
   ↓
Metrics / Logging
```

Các test bắt buộc:

```text
minimum value
maximum value
invalid input
high performance
low performance
empty review queue
large review backlog
workload increase limit
workload decrease limit
concurrent update
```

---

# 51. Final Personalization Architecture

```text
                    PERSONALIZATION ENGINE
                              │
        ┌─────────────────────┼─────────────────────┐
        │                     │                     │
        ▼                     ▼                     ▼
       SRS                ANALYTICS          RECOMMENDATION
        │                     │                     │
        │               ┌─────┼─────┐       ┌──────┴──────┐
        │               ▼     ▼     ▼       ▼             ▼
        │            Weakness Risk Perf   Vocabulary      Topic
        │               │     │     │       │             │
        └───────────────┴─────┴─────┴───────┴─────────────┘
                              │
                              ▼
                     DIFFICULTY ADJUSTMENT
                              │
                              ▼
                       RAW WORKLOAD
                              │
                              ▼
                       WORKLOAD GUARD
                              │
                              ▼
                      NEW/REVIEW BALANCER
                              │
                              ▼
                       TIME BUDGET
                              │
                              ▼
                     DAILY PLAN GENERATOR
                              │
                              ▼
                         DAILY_PLANS
```

---

# 52. Final Design Decision

English AI Coach V1 coi:

> **AI Personalization Engine** là lõi của sản phẩm.

V1:

```text
Rule-based
+
Heuristic
+
Extended SM-2
+
Content-based Recommendation
+
Workload Guard
+
Daily Planning Algorithm
```

LLM:

```text
Example
Explanation
Story
Mnemonic
Quiz
Personalized Exercise
```

Machine Learning:

```text
V2+
Forgetting Prediction
Recommendation
Difficulty Prediction
Error Classification
Notification Timing
Progress Prediction
```

---

# 53. Final Baseline

```text
SRS v1.2
      ↓
Database Schema v1.6
      ↓
System Architecture v1.3
      ↓
AI Personalization Specification v1.3
      ↓
NEXT:
API Specification v1.4
```

API Specification phải phản ánh các business capabilities trong tài liệu này nhưng không expose trực tiếp implementation nội bộ của:

```text
SRS
Weakness Detection
Forgetting Risk
Recommendation
ML
LLM
```


---

# 54. Idempotency for Learning Attempts

`POST /api/v1/learning/attempts` requires a client-generated `eventId`.

Purpose:

```text
prevent duplicate learning event processing
```

Flow:

```text
Android
 ↓
eventId
 ↓
Idempotency Layer
 ↓
SubmitLearningAttempt
 ↓
SRS
 ↓
Progress
```

The same logical attempt must reuse the same event ID when a network timeout/retry occurs.

---

## 54.1. Source of truth

```text
eventId
→ idempotency_keys

session_attempts
→ historical learning record

user_vocabulary_progress
→ current SRS state
```

Do not add `event_id` to `session_attempts` for V1.2.

---

## 54.2. Duplicate handling

```text
same eventId
+
same request
→ replay previous response

same eventId
+
different request
→ 409 IDEMPOTENCY_KEY_REUSE
```

---

## 54.3. Why this matters to personalization

Without idempotency:

```text
one user tap
→ two requests
→ two SRS updates
→ wrong retention/progress data
```

With idempotency:

```text
one logical action
→ one state transition
```

Therefore idempotency is a data-integrity prerequisite for:

```text
SRS
Weak Word Detection
Forgetting Risk
Daily Plan
Recommendation
```

---

# Reconciled V1 Algorithm Baseline

The following algorithms are the normative V1 implementation. Earlier formulas/examples in this document are explanatory only when they conflict with this section.

## Algorithm versions

```text
assessment-block-v1
sm2-ext-v1
weakness-rule-v1
forgetting-risk-v1
daily-plan-v1
gamification-v1
```

## Assessment (`assessment-block-v1`)

Constants: min 20, max 50, block size 4, promote at 3/4+, demote at 1/4 or less, hold at 2/4, two stable HOLD blocks required, start A1. Difficulty moves one CEFR level and clamps at A1/C2. Changing level resets stable blocks. Final CEFR is current level. Score is `round(correct * 100 / total, 2)` for analytics only.

Question V1: 4-option vocabulary meaning MCQ, unique vocabulary per assessment, persisted question/options/order, same-CEFR distractors preferred with nearest-level fallback. At least 30 active usable vocabulary records per CEFR; otherwise `ASSESSMENT_CONTENT_UNAVAILABLE`.

## SRS state (`sm2-ext-v1`)

Keep the existing SM-2 interval/ease formula. State transitions:

```text
NEW first accepted attempt → LEARNING
LEARNING q<3 → LEARNING
LEARNING q>=3 and repetitions_new>=2 → REVIEWING
REVIEWING q<3 → LEARNING
REVIEWING q>=3 → REVIEWING unless mastered
REVIEWING q>=4 and repetitions_new>=5 and interval_new>=30d → MASTERED
MASTERED q>=3 → MASTERED
MASTERED q<3 → LEARNING
```

## Weakness (`weakness-rule-v1`)

Last 10 accepted vocabulary attempts; recent window 5; minimum attempts 3.

```text
error_rate = incorrect / score_window_size
normalized_response_time = clamp((avg_non_null_ms - 2000) / 6000, 0, 1); no timings → 0
low_quality_rate = count(answerQuality <= 3) / score_window_size
recent_failure_rate = incorrect in recent window / recent window size
weakness_score = .40*error_rate + .20*normalized_response_time + .20*low_quality_rate + .20*recent_failure_rate
```

Weak iff `attempts_total >= 3 AND (score_window_accuracy < .60 OR consecutive_failures >= 2)`. Reason codes: `LOW_ACCURACY`, `RECENT_FAILURES`, `SLOW_RECALL` (last-5 avg >=6000ms), `LOW_ANSWER_QUALITY` (last-5 avg quality <3.5).

## Forgetting risk (`forgetting-risk-v1`)

NEW/no last review/non-positive interval ⇒ score 0, LOW. Otherwise:

```text
time_pressure = clamp(elapsed_days / max(interval_days,1),0,1)
error_rate = incorrect / last10
quality_penalty = (5 - coalesce(last_quality,2.5))/5
repetition_penalty = 1 - min(repetitions,5)/5
response_penalty = normalized_response_time
risk_score = .45*time_pressure + .25*error_rate + .15*quality_penalty + .10*repetition_penalty + .05*response_penalty
LOW < .40; MEDIUM .40..<.70; HIGH >= .70
```

Due date remains authoritative for due reviews.

## Deterministic recommendation

Review order: due first → forgetting risk DESC → weakness DESC → next_review_at ASC NULLS LAST → vocabulary_id ASC. Review reason precedence: `DUE_REVIEW`, `HIGH_FORGETTING_RISK`, `WEAK_WORD`.

New-word eligibility: active, no progress/NEW, exact user CEFR; fallback one level below only. `goal_topics` provides relevance. Primary goal weight is full; non-primary selected goal weight is multiplied by .5; take max. Sort goal score DESC → exact CEFR first → topic diversity penalty ASC → vocabulary_id ASC. If at least 5 new words and alternatives exist, no leaf topic exceeds 40%; relax if necessary to fill target.

## Workload and allocation (`daily-plan-v1`)

Performance window 7 days, minimum 3 active days. HIGH = completion>=.90 AND accuracy>=.85; LOW = completion<.70 OR accuracy<.60; otherwise NORMAL. Raw change HIGH +10%, LOW -20%, otherwise 0%, with hard guard +20%/-30%. Initial total is `max(5, round(daily_learning_minutes*1.5))`.

Allocation: high review backlog (due >= max(10, ceil(previous_review_target*1.5))) = 80/15/5 review/new/quiz; high retention = 50/35/15; normal = 60/25/15. Estimated seconds: new=60, review=30, quiz question=45. If over time budget decrement quiz → new → review. Availability transfer order follows the reconciliation baseline and total never exceeds final workload.

Daily Plan is a persisted snapshot; there is no mid-day dynamic reranking.
