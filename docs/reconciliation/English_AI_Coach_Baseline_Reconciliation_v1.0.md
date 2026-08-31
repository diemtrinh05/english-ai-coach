# English AI Coach — Baseline Reconciliation v1.0

**Project:** English AI Coach  
**Document:** Baseline Reconciliation & Canonical Decision Pack  
**Version:** 1.0  
**Status:** ARCHIVED — INTEGRATED  
**Effective date:** 2026-08-31  
**Purpose:** đóng toàn bộ blocker consistency/code-readiness đã phát hiện trong baseline hiện tại và tạo một contract deterministic đủ để bắt đầu V1 implementation.

---

# 1. Mục tiêu tài liệu

Tài liệu này không thay thế vĩnh viễn SRS/Database/API/Technical Spec. Nó là **reconciliation overlay** dùng để:

1. chốt quyết định canonical cho các điểm baseline đang mâu thuẫn hoặc còn thiếu;
2. ngăn AI Agent tự phát minh business rule;
3. xác định chính xác file nào phải cập nhật và version đích;
4. tạo implementation gate rõ ràng;
5. cho phép implementation dựa trên một bộ quyết định duy nhất trong giai đoạn các baseline document đang được regenerate.

Sau khi tất cả thay đổi trong tài liệu này đã được merge ngược vào baseline docs, tài liệu có thể chuyển sang trạng thái `ARCHIVED — INTEGRATED` và không còn đóng vai trò override.

---

# 2. Phạm vi reconciliation

Reconciliation này xử lý các nhóm sau:

```text
Technology stack / Admin Web
Document version & provenance
Localization
Adaptive Assessment
Extended SM-2 state machine
Weak Word Detection
Forgetting Risk
Vocabulary/Topic Recommendation
Daily workload
New/Review allocation
Daily Plan persistence/completion
Gamification
Notification / FCM
Idempotency
Async AI API
Admin AI CEFR Suggestion
Admin routes / audit actions
Database integrity for answerQuality/isCorrect
Documentation hygiene
Implementation gates
```

Không mở rộng V1 sang:

```text
Speaking
Writing
Listening
Reading course
AI Tutor hội thoại
Offline learning synchronization
ML forgetting prediction
Collaborative filtering
ML recommendation
AI notification timing optimization
```

---

# 3. Quy tắc precedence tạm thời

Sau khi project owner phê duyệt reconciliation này, thứ tự đọc của Agent phải là:

```text
PROJECT_RULES.md
        ↓
Baseline Reconciliation v1.0
        ↓
AGENTS.md / role-specific agent file
        ↓
Relevant approved baseline documents
        ↓
Implementation
```

Reconciliation chỉ override những điểm **được ghi rõ bằng Decision ID** trong tài liệu này.

Các phần baseline không được đề cập vẫn giữ nguyên authority hiện tại.

Nếu reconciliation và baseline cũ mâu thuẫn tại một Decision ID đã được liệt kê:

```text
Reconciliation v1.0 wins temporarily.
```

Không được suy rộng một decision sang phạm vi khác.

---

# 4. Target reconciled baseline

Sau khi integrate reconciliation, baseline version đích là:

| Document | Current | Target |
|---|---:|---:|
| PROJECT_RULES | 1.1 | **1.2** |
| SRS | 1.1 | **1.2** |
| Database Schema | 1.5 | **1.6** |
| System Architecture | 1.2 filename / 1.1 metadata | **1.3** |
| AI Personalization | 1.2 | **1.3** |
| API Specification | 1.3 filename / 1.1 metadata | **1.4** |
| OpenAPI / Swagger | 1.3 | **1.4** |
| Technical Specification | 1.1 | **1.2** |
| Backend Technical Specification | 1.2 | **1.3** |
| Android Java Technical Specification | 1.0 | **1.1** |
| Flutter Technical Specification | 1.0 | **1.1** |
| Admin Web Technical Specification | 1.0 | **1.1** |
| Admin Web UI/UX | 1.1 | **1.2** |
| Admin High-Fidelity | 1.0 | **1.1** |
| Admin Interactive Prototype | 1.0 | **1.1** |

Canonical dependency chain sau reconciliation:

```text
SRS v1.2
  ↓
Database Schema v1.6
  ↓
System Architecture v1.3
  ↓
AI Personalization v1.3
  ↓
API Specification v1.4
  ↓
OpenAPI v1.4
  ↓
Technical Specification v1.2
  ↓
Backend Technical Specification v1.3
  ↓
Android Java Technical Specification v1.1
  ↓
Flutter Technical Specification v1.1
  ↓
Admin Web Technical Specification v1.1
  ↓
UI/UX + High-Fidelity + Prototype bindings
```

Metadata `Version`, filename version và `Related documents` phải khớp chain trên.

---

# 5. Locked invariants — giữ nguyên tuyệt đối

Các rule sau được reaffirm và không được thay đổi trong reconciliation:

```text
answerQuality ∈ [0,5]
answerQuality >= 3 → isCorrect = true
answerQuality < 3  → isCorrect = false
client KHÔNG gửi isCorrect

eventId = one logical operation
idempotency storage = idempotency_keys
idempotency retention = 30 days

optimistic locking error = CONCURRENT_UPDATE
idempotency misuse error = IDEMPOTENCY_KEY_REUSE

review_note lưu lý do reject reusable AI content
review_note đồng thời được ghi trong admin_audit_logs details

MAX_DAILY_WORKLOAD_INCREASE_PERCENT = 20%
MAX_DAILY_WORKLOAD_DECREASE_PERCENT = 30%

5 failed login attempts → lock khoảng 5 phút

V1 = online-first
không có offline learning queue/SRS/sync/merge

Reusable AI content → Admin approval trước publish
Personalized real-time exercise → không cần per-result Admin approval

Backend authoritative cho:
SRS, correctness, CEFR, personalization, XP, streak, quiz score, AI budget
```

---

# 6. Decision Register

| ID | Decision | Status |
|---|---|---|
| BR-001 | Admin Web canonical stack = React + TypeScript + Vite | LOCKED |
| BR-002 | Version/provenance phải đồng bộ theo target chain | LOCKED |
| BR-003 | V1 single-locale Vietnamese, dùng resource centralization; không locale switcher | LOCKED |
| BR-004 | Adaptive Assessment V1 dùng block-based deterministic CEFR algorithm | LOCKED |
| BR-005 | Assessment state được persist, không chỉ lưu final result | LOCKED |
| BR-006 | SRS status transition NEW/LEARNING/REVIEWING/MASTERED được freeze | LOCKED |
| BR-007 | Weakness Score formula và weak classification được freeze | LOCKED |
| BR-008 | Forgetting Risk formula/threshold được freeze | LOCKED |
| BR-009 | V1 recommendation dùng deterministic ranking; bỏ weighted recommendation formula khỏi normative V1 | LOCKED |
| BR-010 | Thêm goal_topics để biểu diễn goal↔topic relevance | LOCKED |
| BR-011 | Daily workload adjustment constants/rules được freeze | LOCKED |
| BR-012 | Daily Plan là persisted snapshot, có daily_plan_items | LOCKED |
| BR-013 | Gamification XP/Level/Badge/Streak semantics được freeze | LOCKED |
| BR-014 | Notification V1 giữ FCM; thêm user_devices + notification_preferences + API | LOCKED |
| BR-015 | eventId là idempotency mechanism duy nhất; bỏ Idempotency-Key header V1 | LOCKED |
| BR-016 | PostgreSQL idempotency claim dùng INSERT ... ON CONFLICT DO NOTHING | LOCKED |
| BR-017 | Client-facing AI generation V1 synchronous; bỏ 202/jobId khỏi V1 contract | LOCKED |
| BR-018 | Admin AI CEFR Suggestion chuyển khỏi V1 | LOCKED |
| BR-019 | Canonical admin AI usage route = /api/v1/admin/statistics/ai-usage | LOCKED |
| BR-020 | Canonical personalized exercise route = /api/v1/learning/personalized-exercise | LOCKED |
| BR-021 | answer_quality NOT NULL và DB CHECK is_correct = (answer_quality >= 3) | LOCKED |
| BR-022 | Admin audit action enum được hợp nhất | LOCKED |
| BR-023 | Daily Plan/session/assessment/AI cost mutations phải có eventId khi retry có thể tạo side effect trùng | LOCKED |
| BR-024 | Xóa AI drafting citation artifacts và duplicate section numbering | LOCKED |

---

# 7. BR-001 — Admin Web technology

Canonical V1 Admin Web:

```text
React
TypeScript
Vite
HTTP API → Spring Boot Backend
```

Không dùng:

```text
Thymeleaf
Spring MVC server-rendered Admin UI
Admin direct database access
```

SRS và Database Schema phải sửa metadata đang ghi `Admin Web: Java Spring Boot` thành:

```text
Admin Web: React + TypeScript + Vite
Backend API: Java Spring Boot
```

---

# 8. BR-003 — Localization policy

V1 chỉ có một locale UI chính thức:

```text
vi-VN
```

Không implement trong V1:

```text
locale switcher
Accept-Language negotiation
multiple locale bundles
runtime language switching
react-i18next requirement
```

Nhưng user-facing string vẫn phải centralized:

```text
Android V1 → res/values/strings.xml
Admin Web V1 → typed Vietnamese messages/resource module
Flutter V2 → Flutter localization mechanism khi Flutter V2 thực sự được implement
```

Admin có thể dùng:

```text
src/shared/messages/vi.ts
```

hoặc cấu trúc tương đương, nhưng không hard-code cùng một user-facing message rải rác trong business logic.

`ANTIGRAVITY_FRONTEND_LEAD.md` phải bỏ câu cấm tuyệt đối localization mechanism và thay bằng single-locale resource policy này.

---

# 9. BR-004/005 — Adaptive Assessment V1 canonical algorithm

## 9.1 Mục tiêu

Placement assessment V1 phải:

```text
deterministic
rule-based
persisted
retry-safe
không dùng ML
```

Output:

```text
A1 | A2 | B1 | B2 | C1 | C2
```

## 9.2 Constants

```text
ASSESSMENT_MIN_QUESTIONS = 20
ASSESSMENT_MAX_QUESTIONS = 50
ASSESSMENT_BLOCK_SIZE = 4
ASSESSMENT_PROMOTE_MIN_CORRECT = 3
ASSESSMENT_DEMOTE_MAX_CORRECT = 1
ASSESSMENT_STABLE_BLOCKS_REQUIRED = 2
ASSESSMENT_START_LEVEL = A1
```

Tất cả constants configurable nhưng default V1 phải đúng giá trị trên.

## 9.3 Difficulty order

```text
A1 = 0
A2 = 1
B1 = 2
B2 = 3
C1 = 4
C2 = 5
```

Không dùng `EASY/MEDIUM/HARD` trong authoritative Assessment V1.

## 9.4 Block evaluation

Mỗi block gồm 4 câu tại `currentDifficulty`.

Sau đủ 4 câu:

```text
correct >= 3
→ PROMOTE one CEFR level

correct <= 1
→ DEMOTE one CEFR level

correct == 2
→ HOLD current CEFR
```

Boundary:

```text
C2 + PROMOTE → HOLD_CEILING at C2
A1 + DEMOTE  → HOLD_FLOOR at A1
```

Khi difficulty thay đổi:

```text
block_questions = 0
block_correct = 0
stable_block_count = 0
```

Khi result là HOLD/HOLD_FLOOR/HOLD_CEILING tại cùng level:

```text
stable_block_count += 1
```

## 9.5 Stopping rule

Assessment hoàn tất khi:

```text
questions_answered >= 20
AND
stable_block_count >= 2
```

hoặc:

```text
questions_answered == 50
```

Final CEFR:

```text
final_cefr = currentDifficulty
```

Score:

```text
score = round(correct_answers * 100.0 / total_questions, 2)
```

Score chỉ dùng analytics/display; CEFR final không được map lại từ percentage bằng một bảng khác.

## 9.6 Question format V1

V1 placement dùng deterministic vocabulary meaning MCQ:

```text
questionText = "Từ '<word>' có nghĩa là gì?"
4 options
1 correct meaning_vi
3 distractor meaning_vi
```

Question selection:

1. target vocabulary phải `is_active = true`;
2. vocabulary CEFR = currentDifficulty;
3. chưa từng xuất hiện trong assessment hiện tại;
4. distractor phải khác target, có meaning khác và ưu tiên cùng CEFR;
5. nếu cùng CEFR không đủ distractor, fallback sang level kề gần nhất;
6. option order được randomize bằng server, nhưng question instance và order phải persist trước khi gửi client.

Deployment/content readiness:

```text
mỗi CEFR phải có tối thiểu 30 active vocabulary records có meaning_vi usable
```

Nếu không đủ content để tạo câu hợp lệ:

```text
HTTP 503
code = ASSESSMENT_CONTENT_UNAVAILABLE
```

## 9.7 Assessment persistence

`user_level_assessments` không còn chỉ là final history. Nó là assessment aggregate.

Canonical columns sau reconciliation:

```text
id UUID PK
user_id UUID FK NOT NULL
assessment_type VARCHAR(30) NOT NULL
status VARCHAR(20) NOT NULL
current_cefr_level_id UUID FK NOT NULL
final_cefr_level_id UUID FK NULL
score DECIMAL(5,2) NULL
questions_answered INTEGER NOT NULL DEFAULT 0
correct_answers INTEGER NOT NULL DEFAULT 0
block_questions INTEGER NOT NULL DEFAULT 0
block_correct INTEGER NOT NULL DEFAULT 0
stable_block_count INTEGER NOT NULL DEFAULT 0
started_at TIMESTAMPTZ NOT NULL
completed_at TIMESTAMPTZ NULL
created_at TIMESTAMPTZ NOT NULL
updated_at TIMESTAMPTZ NOT NULL
```

Status:

```text
IN_PROGRESS
COMPLETED
CANCELLED
```

`assessment_type` giữ:

```text
INITIAL
PERIODIC
MANUAL
```

Chỉ tối đa một `IN_PROGRESS` assessment/user:

```sql
CREATE UNIQUE INDEX uq_user_assessment_in_progress
ON user_level_assessments(user_id)
WHERE status = 'IN_PROGRESS';
```

## 9.8 assessment_items — new table

```text
id UUID PK                    ← public questionId
assessment_id UUID FK NOT NULL
sequence_no INTEGER NOT NULL
vocabulary_id UUID FK NOT NULL
cefr_level_id UUID FK NOT NULL
question_text TEXT NOT NULL
options_json JSONB NOT NULL
correct_answer TEXT NOT NULL
selected_answer TEXT NULL
is_correct BOOLEAN NULL
response_time_ms INTEGER NULL
answered_at TIMESTAMPTZ NULL
created_at TIMESTAMPTZ NOT NULL
```

Constraints:

```text
UNIQUE(assessment_id, sequence_no)
UNIQUE(assessment_id, vocabulary_id)
CHECK(response_time_ms IS NULL OR response_time_ms >= 0)
```

Backend tạo/persist câu đầu trong transaction `POST /assessments`.

Sau mỗi answer hợp lệ:

```text
update state
→ nếu chưa complete: persist next assessment_item
```

`GET /assessments/{assessmentId}/next-question` chỉ đọc unanswered persisted item; không generate non-idempotent question trong GET.

## 9.9 Assessment API idempotency

### Start

```http
POST /api/v1/assessments
```

Request canonical:

```json
{
  "type": "INITIAL",
  "eventId": "uuid"
}
```

Nếu user đã có assessment `IN_PROGRESS` bằng event khác:

```text
409 ASSESSMENT_ALREADY_IN_PROGRESS
```

### Answer

```http
POST /api/v1/assessments/{assessmentId}/answers
```

Request canonical:

```json
{
  "questionId": "uuid",
  "answer": "To leave",
  "responseTimeMs": 2300,
  "eventId": "uuid"
}
```

Resubmit cùng question bằng logical event khác sau khi đã answered:

```text
409 ASSESSMENT_QUESTION_ALREADY_ANSWERED
```

---

# 10. BR-006 — Extended SM-2 state machine

SM-2 formula hiện tại được giữ nguyên.

Canonical progress states:

```text
NEW
LEARNING
REVIEWING
MASTERED
```

Constants:

```text
MASTERED_MIN_REPETITIONS = 5
MASTERED_MIN_INTERVAL_DAYS = 30
MASTERED_ENTRY_MIN_QUALITY = 4
```

State rules:

## NEW

Vocabulary chưa có accepted learning attempt:

```text
status = NEW
repetitions = 0
interval_days = 0
next_review_at = NULL
```

Attempt đầu tiên, bất kể quality:

```text
NEW → LEARNING
```

## LEARNING

Nếu:

```text
q < 3
```

thì giữ:

```text
LEARNING
```

Nếu:

```text
q >= 3
AND repetitions_new >= 2
```

thì:

```text
LEARNING → REVIEWING
```

## REVIEWING

Nếu:

```text
q < 3
```

thì:

```text
REVIEWING → LEARNING
```

Nếu:

```text
q >= 3
```

thì giữ REVIEWING, trừ khi đủ điều kiện mastered.

Entry to MASTERED:

```text
q >= 4
AND repetitions_new >= 5
AND interval_new >= 30 days
```

thì:

```text
REVIEWING → MASTERED
```

## MASTERED

Nếu:

```text
q >= 3
```

thì:

```text
MASTERED → MASTERED
```

Nếu:

```text
q < 3
```

thì:

```text
MASTERED → LEARNING
```

`wordsLearned`:

```text
count(status IN [LEARNING, REVIEWING, MASTERED])
```

`wordsMastered`:

```text
count(status = MASTERED)
```

---

# 11. BR-007 — Weak Word Detection V1

## 11.1 Window

```text
SCORE_WINDOW = last 10 accepted vocabulary attempts
RECENT_WINDOW = last 5 accepted vocabulary attempts
MIN_ATTEMPTS_FOR_WEAK = 3
```

## 11.2 Features

```text
error_rate
normalized_response_time
low_quality_rate
recent_failure_rate
consecutive_failures
```

### error_rate

```text
incorrect / attempts_in_score_window
```

### normalized_response_time

```text
avg_ms = average non-null response_time_ms in SCORE_WINDOW

normalized_response_time =
clamp((avg_ms - 2000) / 6000, 0, 1)
```

Nếu không có response time:

```text
normalized_response_time = 0
```

### low_quality_rate

```text
count(answer_quality <= 3) / attempts_in_score_window
```

### recent_failure_rate

```text
incorrect in RECENT_WINDOW / size(RECENT_WINDOW)
```

## 11.3 Score

```text
weakness_score =
    0.40 * error_rate
  + 0.20 * normalized_response_time
  + 0.20 * low_quality_rate
  + 0.20 * recent_failure_rate
```

Clamp:

```text
[0,1]
```

## 11.4 Weak classification

Normative V1:

```text
weak = attempts_total >= 3
       AND
       (
         score_window_accuracy < 60%
         OR consecutive_failures >= 2
       )
```

`weakness_score` dùng để ranking/explainability, không tự tạo rule thứ ba.

## 11.5 Reason codes

```text
LOW_ACCURACY
→ score_window_accuracy < 60%

RECENT_FAILURES
→ consecutive_failures >= 2

SLOW_RECALL
→ average response time of last 5 non-null attempts >= 6000ms

LOW_ANSWER_QUALITY
→ average answerQuality of last 5 attempts < 3.5
```

---

# 12. BR-008 — Forgetting Risk V1

V1 là heuristic, không gọi là ML prediction.

## 12.1 Eligibility

Nếu:

```text
status = NEW
OR last_reviewed_at IS NULL
OR interval_days <= 0
```

thì:

```text
risk_score = 0
risk_level = LOW
```

## 12.2 Features

Dùng last 10 accepted attempts cho performance components.

```text
elapsed_days = max(0, hours(now - last_reviewed_at) / 24)
time_pressure = clamp(elapsed_days / max(interval_days, 1), 0, 1)
error_rate = incorrect / attempts_in_last_10
quality_penalty = (5 - coalesce(last_quality, 2.5)) / 5
repetition_penalty = 1 - min(repetitions, 5) / 5
response_penalty = normalized_response_time from Weakness rule
```

## 12.3 Formula

```text
risk_score =
    0.45 * time_pressure
  + 0.25 * error_rate
  + 0.15 * quality_penalty
  + 0.10 * repetition_penalty
  + 0.05 * response_penalty
```

Clamp:

```text
[0,1]
```

## 12.4 Risk levels

```text
LOW    : risk_score < 0.40
MEDIUM : 0.40 <= risk_score < 0.70
HIGH   : risk_score >= 0.70
```

`next_review_at <= now` vẫn là điều kiện due review authoritative; risk không thay thế due date.

---

# 13. BR-009/010 — Recommendation & goal_topics

Weighted `recommendation_score = w1...w6` không còn là normative V1 vì weights và feature semantics chưa đủ contract.

V1 dùng deterministic ranking.

## 13.1 Review ranking

Review candidates được sort:

```text
1. due first: next_review_at <= now
2. forgetting risk score DESC
3. weakness score DESC
4. next_review_at ASC NULLS LAST
5. vocabulary_id ASC
```

Reason code chọn reason mạnh nhất theo order:

```text
DUE_REVIEW
HIGH_FORGETTING_RISK
WEAK_WORD
```

## 13.2 New vocabulary eligibility

```text
vocabulary.is_active = true
user progress does not exist OR status = NEW
CEFR = user's current CEFR
```

Nếu candidate ở exact CEFR không đủ:

```text
fallback one CEFR level below
```

Không tự động chọn level trên current CEFR trong V1.

## 13.3 goal_topics — new table

```text
id UUID PK
goal_id UUID FK NOT NULL
topic_id UUID FK NOT NULL
relevance_weight DECIMAL(4,3) NOT NULL
created_at TIMESTAMPTZ NOT NULL
```

Constraints:

```text
UNIQUE(goal_id, topic_id)
CHECK(relevance_weight >= 0 AND relevance_weight <= 1)
```

## 13.4 New vocabulary ranking

For each vocabulary:

```text
primary_goal_score = max(goal_topics.relevance_weight for primary goal)
secondary_goal_score = 0.5 * max(weight for non-primary selected goals)
goal_score = max(primary_goal_score, secondary_goal_score)
```

Sort:

```text
1. goal_score DESC
2. exact CEFR before fallback CEFR
3. topic diversity penalty ASC
4. vocabulary_id ASC
```

Reason:

```text
goal_score > 0        → GOAL_RELEVANT
has active topic      → TOPIC_RELEVANT
otherwise             → GENERAL
```

## 13.5 Topic diversity

Khi plan có ít nhất 5 new words và có đủ alternatives:

```text
không quá 40% new words từ cùng một leaf topic
```

Nếu không đủ candidates để đạt target, diversity cap được relax để không tạo plan thiếu giả tạo.

---

# 14. BR-011 — Difficulty & workload V1

## 14.1 Performance window

```text
PERFORMANCE_WINDOW_DAYS = 7
MIN_ACTIVE_DAYS_FOR_ADJUSTMENT = 3
```

Metrics:

```text
completion_rate = completed plan workload / planned workload
accuracy = correct accepted attempts / accepted attempts
```

Nếu chưa đủ 3 active days:

```text
performance = INSUFFICIENT_HISTORY
```

## 14.2 Performance classification

```text
HIGH_PERFORMANCE:
completion_rate >= 90%
AND accuracy >= 85%

LOW_PERFORMANCE:
completion_rate < 70%
OR accuracy < 60%

NORMAL:
otherwise
```

## 14.3 Raw workload change

```text
HIGH_PERFORMANCE → +10%
LOW_PERFORMANCE  → -20%
NORMAL           → 0%
INSUFFICIENT_HISTORY → 0%
```

Guard vẫn bắt buộc:

```text
MAX_DAILY_WORKLOAD_INCREASE_PERCENT = 20%
MAX_DAILY_WORKLOAD_DECREASE_PERCENT = 30%
```

## 14.4 Workload unit

V1 workload target được đo bằng:

```text
workload_units = new_words_target + review_words_target + quiz_target
```

`quiz_target` là số quiz questions cần hoàn thành, không phải số quiz attempts.

## 14.5 Initial target

Nếu không có plan trước đó:

```text
previous_total_target = max(5, round(daily_learning_minutes * 1.5))
```

Ví dụ:

```text
20 minutes → 30 workload units
```

## 14.6 Raw/guard formula

```text
raw_total = round(previous_total_target * factor)

factor:
HIGH = 1.10
LOW  = 0.80
NORMAL/INSUFFICIENT = 1.00

upper = previous_total_target * 1.20
lower = previous_total_target * 0.70

final_total = round(clamp(raw_total, lower, upper))
```

---

# 15. BR-011/012 — New/Review/Quiz allocation & Time Budget

## 15.1 Estimated time constants

```text
ESTIMATED_NEW_WORD_SECONDS = 60
ESTIMATED_REVIEW_WORD_SECONDS = 30
ESTIMATED_QUIZ_QUESTION_SECONDS = 45
```

## 15.2 Allocation mode

### HIGH_REVIEW_BACKLOG

Condition:

```text
due_review_count >= max(10, ceil(previous_review_target * 1.5))
```

Ratio:

```text
Review 80%
New    15%
Quiz    5%
```

### HIGH_RETENTION

Condition:

```text
performance = HIGH_PERFORMANCE
AND due_review_count <= previous_review_target
```

Ratio:

```text
Review 50%
New    35%
Quiz   15%
```

### NORMAL

Ratio:

```text
Review 60%
New    25%
Quiz   15%
```

## 15.3 Rounding

```text
review_target = floor(final_total * review_ratio)
new_target = floor(final_total * new_ratio)
quiz_target = final_total - review_target - new_target
```

## 15.4 Availability adjustment

1. cap review target bằng due review candidates available;
2. unused review units chuyển sang new;
3. nếu new candidates thiếu, chuyển units sang remaining due review;
4. phần còn lại có thể chuyển sang quiz;
5. không vượt `final_total`.

## 15.5 Time Budget

```text
estimated_seconds =
  new_target * 60
+ review_target * 30
+ quiz_target * 45
```

Budget:

```text
daily_learning_minutes * 60
```

Nếu vượt budget, decrement theo thứ tự:

```text
1. quiz_target
2. new_target
3. review_target
```

cho tới khi fit.

Review chỉ bị giảm ở bước cuối vì user time budget là hard constraint.

Nếu dưới budget, có thể fill thêm trong giới hạn `final_total`, theo order:

```text
remaining due review
→ new word
→ quiz question
```

---

# 16. BR-012 — Daily Plan canonical persistence model

## 16.1 Snapshot semantics

V1 Daily Plan là **snapshot** theo user local date.

Key:

```text
UNIQUE(user_id, plan_date)
```

`plan_date` được tính theo `user_profiles.timezone`.

Plan được tạo:

```text
first GET /api/v1/learning/today of local day
OR scheduled pre-generation before that
```

Sau khi đã tạo:

```text
targets + ranked vocabulary items không regenerate giữa ngày
```

Thay đổi goal/CEFR/settings giữa ngày áp dụng từ plan ngày kế tiếp.

Không có dynamic re-ranking mỗi lần `GET /today/items`.

## 16.2 daily_plan_items — new table

```text
id UUID PK
daily_plan_id UUID FK NOT NULL
item_type VARCHAR(20) NOT NULL
vocabulary_id UUID FK NULL
position INTEGER NOT NULL
reason_code VARCHAR(50) NULL
target_count INTEGER NOT NULL DEFAULT 1
completed_count INTEGER NOT NULL DEFAULT 0
status VARCHAR(20) NOT NULL
completed_at TIMESTAMPTZ NULL
created_at TIMESTAMPTZ NOT NULL
updated_at TIMESTAMPTZ NOT NULL
```

`item_type`:

```text
REVIEW
NEW
QUIZ
```

`status`:

```text
PENDING
IN_PROGRESS
COMPLETED
```

Constraints:

```text
UNIQUE(daily_plan_id, position)
CHECK(target_count > 0)
CHECK(completed_count >= 0 AND completed_count <= target_count)

REVIEW/NEW → vocabulary_id IS NOT NULL AND target_count = 1
QUIZ       → vocabulary_id IS NULL
```

Plan có:

```text
one row per REVIEW vocabulary
one row per NEW vocabulary
at most one aggregate QUIZ row
```

QUIZ row:

```text
target_count = daily_plans.quiz_target
```

## 16.3 Item completion

REVIEW/NEW item:

```text
completed on first accepted learning attempt
for same vocabulary
within the plan's local day
and after plan creation
```

QUIZ item:

```text
completed_count += 1
for each accepted quiz answer during local day
until target_count
```

Incorrect answer vẫn tính là một quiz question đã làm; correctness chỉ ảnh hưởng accuracy/XP.

## 16.4 Plan status

```text
PENDING     → no item progress
IN_PROGRESS → at least one item has progress and plan not finalized
COMPLETED   → finalized with all targets reached
PARTIAL     → finalized before all targets reached
```

`COMPLETED` và `PARTIAL` là terminal trong V1.

## 16.5 API item contract

Canonical `DailyPlanItem`:

```json
{
  "itemId": "uuid",
  "itemType": "REVIEW",
  "vocabularyId": "uuid",
  "priority": 1,
  "reasonCode": "HIGH_FORGETTING_RISK",
  "targetCount": 1,
  "completedCount": 0,
  "status": "PENDING"
}
```

For QUIZ:

```json
{
  "itemId": "uuid",
  "itemType": "QUIZ",
  "vocabularyId": null,
  "priority": 30,
  "reasonCode": "WEAKNESS_PRACTICE",
  "targetCount": 5,
  "completedCount": 2,
  "status": "IN_PROGRESS"
}
```

OpenAPI phải bỏ `vocabularyId` khỏi required fields của `DailyPlanItem`.

## 16.6 Complete Daily Plan

Canonical request:

```http
POST /api/v1/learning/today/complete
```

```json
{
  "eventId": "uuid"
}
```

Behavior:

```text
all targets reached
→ status COMPLETED
→ DAILY_PLAN_COMPLETED XP awarded once

some/no target reached
→ status PARTIAL
→ no Daily Plan completion XP
```

Retry same logical completion dùng same eventId.

---

# 17. BR-013 — Gamification V1

## 17.1 XP constants

```text
XP_CORRECT_LEARNING_ATTEMPT = 5
XP_CORRECT_QUIZ_ANSWER = 5
XP_LEARNING_SESSION_COMPLETED = 10
XP_DAILY_PLAN_COMPLETED = 50
XP_STREAK_MAINTAINED = 10
```

Không award XP cho incorrect answer.

## 17.2 Canonical xp_logs.reason

```text
LEARNING_CORRECT
QUIZ_CORRECT
SESSION_COMPLETED
DAILY_PLAN_COMPLETED
STREAK_MAINTAINED
```

`reference_type`/`reference_id` phải trỏ tới entity gây XP khi có thể.

## 17.3 Session completion XP

Learning session nhận `SESSION_COMPLETED` XP khi:

```text
session has at least 1 accepted session_attempt
AND transition ended_at NULL → non-NULL xảy ra lần đầu
```

Canonical endpoint request:

```http
POST /api/v1/learning/sessions/{sessionId}/complete
```

```json
{
  "eventId": "uuid"
}
```

## 17.4 Streak semantics

Qualifying activity:

```text
accepted session_attempt
OR accepted quiz_attempt_answer
```

Assessment không tính streak.

Local day dựa trên `user_profiles.timezone`.

First qualifying activity trong local day:

```text
if last_active_date IS NULL:
  current_streak = 1

else if local_date == last_active_date:
  no streak change

else if local_date == last_active_date + 1 day:
  current_streak += 1

else:
  current_streak = 1

longest_streak = max(longest_streak, current_streak)
last_active_date = local_date
```

`XP_STREAK_MAINTAINED` award một lần khi `last_active_date` chuyển sang ngày mới.

Update streak vẫn dùng optimistic locking `@Version`; conflict contract là `CONCURRENT_UPDATE`.

## 17.5 Level

Derived, không cần level table:

```text
XP_PER_LEVEL = 500
level = floor(total_xp / 500) + 1
nextLevelXp = level * 500
```

`nextLevelXp` là **absolute total XP threshold** để vào level tiếp theo.

Ví dụ:

```text
totalXp = 720
level = 2
nextLevelXp = 1000
```

## 17.6 Badges

Canonical seeded badges:

| Code/Name | condition_type | condition_value |
|---|---|---:|
| FIRST_LESSON | LEARNING_SESSION_COUNT | 1 |
| STREAK_7 | LONGEST_STREAK | 7 |
| WORDS_100 | WORDS_LEARNED | 100 |
| WORDS_500 | WORDS_LEARNED | 500 |
| PERFECT_QUIZ | PERFECT_QUIZ | NULL |

PERFECT_QUIZ condition:

```text
completed quiz
AND total_questions >= 5
AND correct_answers = total_questions
```

Badge award phải idempotent bằng:

```text
UNIQUE(user_id, badge_id)
```

## 17.7 Leaderboard

XP theo period = `SUM(xp_logs.xp_amount)` trong period theo UTC timestamps, với period boundary tính theo application-defined UTC boundary.

Ordering:

```text
xp DESC
user_id ASC
```

Users có cùng XP có thể có cùng displayed rank bằng SQL `RANK()`; `user_id` chỉ dùng stable ordering trong cùng rank.

---

# 18. BR-014 — Notification V1

Notification giữ trong V1 và dùng Firebase Cloud Messaging cho Android.

Không có AI notification timing.

## 18.1 New table: user_devices

```text
id UUID PK
user_id UUID FK NOT NULL
installation_id UUID NOT NULL
platform VARCHAR(20) NOT NULL
push_token TEXT NOT NULL
is_active BOOLEAN NOT NULL
last_seen_at TIMESTAMPTZ NULL
created_at TIMESTAMPTZ NOT NULL
updated_at TIMESTAMPTZ NOT NULL
```

Constraints:

```text
UNIQUE(user_id, installation_id)
UNIQUE(push_token)
platform V1 = ANDROID
```

## 18.2 New table: notification_preferences

```text
id UUID PK
user_id UUID FK UNIQUE NOT NULL
push_enabled BOOLEAN NOT NULL DEFAULT true
review_reminder_enabled BOOLEAN NOT NULL DEFAULT true
daily_plan_enabled BOOLEAN NOT NULL DEFAULT true
streak_reminder_enabled BOOLEAN NOT NULL DEFAULT true
preferred_study_time TIME NOT NULL DEFAULT '19:00'
created_at TIMESTAMPTZ NOT NULL
updated_at TIMESTAMPTZ NOT NULL
```

Timezone lấy từ:

```text
user_profiles.timezone
```

## 18.3 Device API

Register/update token:

```http
PUT /api/v1/devices/{installationId}/push-token
```

Request:

```json
{
  "platform": "ANDROID",
  "pushToken": "fcm-token"
}
```

Repeated PUT phải idempotent theo resource semantics; không cần `eventId`.

Deactivate on logout/uninstall when possible:

```http
DELETE /api/v1/devices/{installationId}
```

## 18.4 Preference API

```http
GET /api/v1/notification-preferences
PUT /api/v1/notification-preferences
```

PUT request:

```json
{
  "pushEnabled": true,
  "reviewReminderEnabled": true,
  "dailyPlanEnabled": true,
  "streakReminderEnabled": true,
  "preferredStudyTime": "19:00"
}
```

## 18.5 Scheduling V1

Per user local timezone:

```text
DAILY_PLAN
→ 07:00 local
→ if dailyPlanEnabled + pushEnabled
→ create/generate today's plan if needed, then notify once/day

REVIEW_REMINDER
→ preferred_study_time local
→ only when due review count > 0
→ once/day

STREAK
→ 21:00 local
→ only when current_streak > 0 and no qualifying activity today
→ once/day
```

`SYSTEM` notification chỉ do backend/admin operation tạo.

Dedupe notification type/day bằng application logic + database uniqueness recommendation:

```text
(user_id, type, local_notification_date)
```

Để enforce rõ ràng, Database v1.6 nên thêm:

```text
local_notification_date DATE NULL
```

và partial/regular unique index phù hợp cho scheduled user reminders.

## 18.6 Notification delivery status

Giữ:

```text
PENDING
SENT
FAILED
CANCELLED
```

Read/unread tracking chưa bắt buộc V1.

---

# 19. BR-015/016/023 — Idempotency canonical contract

## 19.1 One mechanism only

V1 sử dụng:

```text
eventId in request body
```

Không sử dụng:

```text
Idempotency-Key HTTP header
```

OpenAPI v1.4 phải xóa:

```text
components.parameters.IdempotencyKeyOptional
```

và mọi reference tới parameter này.

## 19.2 Required semantics

```text
same eventId
+ same authenticated user
+ same method/route/path/query/body logical request
→ replay stored status/body

same eventId
+ different user/method/route/path/query/body
→ 409 IDEMPOTENCY_KEY_REUSE
```

Guarantee chỉ trong retention window:

```text
30 days
```

Sau retention, client không được kỳ vọng replay guarantee.

## 19.3 Canonical request hash

Hash input là canonical JSON envelope:

```json
{
  "method": "POST",
  "routeTemplate": "/api/v1/quiz-attempts/{attemptId}/answers",
  "path": {
    "attemptId": "uuid"
  },
  "query": {},
  "body": {
    "questionId": "uuid",
    "userAnswer": "B",
    "responseTimeMs": 3400
  }
}
```

Rules:

```text
exclude eventId from body before hashing
object keys sorted lexicographically
UUID normalized lowercase canonical text
numbers serialized canonically
no insignificant whitespace
SHA-256 → lowercase hex CHAR(64)
```

`user_id` được compare riêng trong idempotency record.

## 19.4 Endpoint identity

Store:

```text
METHOD + route template
```

Ví dụ:

```text
POST /api/v1/learning/attempts
POST /api/v1/quiz-attempts/{attemptId}/answers
```

Path parameter values nằm trong request hash.

## 19.5 PostgreSQL race-safe claim

Không implement:

```text
INSERT
→ catch unique violation
→ continue same transaction
```

Canonical:

```sql
INSERT INTO idempotency_keys (...)
VALUES (...)
ON CONFLICT (event_id) DO NOTHING;
```

Application flow trong transaction:

```text
claim eventId with ON CONFLICT DO NOTHING
        ↓
inserted?
  ┌─────┴─────┐
 YES          NO
  │            │
  ▼            ▼
process      load existing row
business    compare user/endpoint/hash
mutation      │
  │          ├─ same → replay stored response
  │          └─ different → 409 IDEMPOTENCY_KEY_REUSE
  ▼
store response_snapshot + response_status
  ↓
COMMIT same transaction
```

Nếu business mutation fail:

```text
transaction rollback
→ claimed idempotency row rollback
```

Một concurrent duplicate có thể block ở `ON CONFLICT` cho tới transaction đầu commit/rollback; đây là expected behavior.

## 19.6 Required eventId endpoints V1

At minimum:

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

`eventId` là required field trong request body của các endpoint trên.

PUT resource update có natural idempotency không bắt buộc eventId trừ khi spec riêng yêu cầu.

---

# 20. BR-017 — Client-facing AI V1 synchronous-only

Để tránh contract `202 + jobId` không có job-status lifecycle, V1 canonical:

```text
POST /api/v1/admin/ai-content/generate
→ synchronous
→ 201 Created
→ returns generated reusable content record in PENDING_REVIEW

POST /api/v1/learning/personalized-exercise
→ synchronous
→ 200 OK
→ returns generated personalized exercise
```

Không expose trong V1:

```text
202 Accepted for these workflows
jobId
GET /api/v1/jobs/{jobId}
```

Architecture vẫn có thể dùng background jobs cho:

```text
AI usage aggregation
notification scheduling/sending
daily plan pre-generation
idempotency cleanup
```

nhưng không tạo client-facing async contract.

AI timeout/failure:

```text
AI_GENERATION_FAILED
```

AI budget:

```text
AI_BUDGET_EXCEEDED
```

Retry một personalized/admin AI generation logical request phải dùng cùng `eventId` để không phát sinh LLM cost trùng.

---

# 21. BR-018 — Admin AI CEFR Suggestion

`AI CEFR Suggestion` trong Admin vocabulary editor không thuộc approved V1 scope và hiện không có SRS/API contract.

Canonical decision:

```text
Move to V2 / Future backlog.
```

V1 Admin phải:

```text
manual CEFR selection
```

Các section/flow về:

```text
AI CEFR Suggestion
Suggest CEFR
Accept/Edit AI CEFR
```

trong Admin UI/UX, High-Fidelity, Prototype và Technical Spec phải:

```text
remove khỏi V1 active flow
hoặc đánh dấu FUTURE / V2 rõ ràng
```

Không thêm endpoint V1 chỉ để hợp thức hóa UI cũ.

---

# 22. BR-019/020/022 — Route & Admin audit reconciliation

## 22.1 AI usage route

Canonical:

```http
GET /api/v1/admin/statistics/ai-usage
```

Mọi reference cũ:

```text
/api/v1/admin/statistics/ai-usage
/admin/ai-usage
```

phải đổi sang canonical route tùy tài liệu có/không hiển thị base prefix.

## 22.2 Personalized Exercise

Canonical:

```http
POST /api/v1/learning/personalized-exercise
```

Không dùng:

```text
POST /api/v1/learning/personalized-exercise
```

trong Android/Flutter technical docs.

## 22.3 Admin audit actions

Canonical action identifiers:

```text
CREATE
UPDATE
ACTIVATE
DEACTIVATE
APPROVE
REJECT
PUBLISH
UNPUBLISH
LOCK_USER
UNLOCK_USER
```

Không map `ACTIVATE/PUBLISH` âm thầm thành `UPDATE`.

Reject reusable AI content:

```text
ai_generated_content.review_note = reason
admin_audit_logs.action = REJECT
admin_audit_logs.details contains same reason
```

---

# 23. BR-021 — Database enforcement for answerQuality

`session_attempts` V1 canonical:

```text
answer_quality SMALLINT NOT NULL
is_correct BOOLEAN NOT NULL
```

Constraints:

```sql
CHECK(answer_quality BETWEEN 0 AND 5)
CHECK(is_correct = (answer_quality >= 3))
```

Backend vẫn phải derive `is_correct`; DB CHECK là defense-in-depth.

Client không bao giờ gửi `isCorrect`.

---

# 24. Database Schema v1.6 reconciliation summary

Database v1.6 phải thay đổi tối thiểu:

## 24.1 Add tables

```text
assessment_items
goal_topics
daily_plan_items
user_devices
notification_preferences
```

Total table count canonical sau integration là **34**: DB v1.5 thực tế có 29 table definitions khi tính cả `idempotency_keys`, cộng 5 bảng mới. Metadata cũ ghi 28 là stale bookkeeping.

## 24.2 Modify tables

```text
user_level_assessments
session_attempts
notifications
```

### user_level_assessments

Expand theo Section 9.

### session_attempts

```text
answer_quality NOT NULL
CHECK answer_quality 0..5
CHECK is_correct = (answer_quality >= 3)
```

### notifications

Add:

```text
local_notification_date DATE NULL
```

Recommended uniqueness for scheduled reminders:

```sql
CREATE UNIQUE INDEX uq_notifications_user_type_local_date
ON notifications(user_id, type, local_notification_date)
WHERE local_notification_date IS NOT NULL
  AND type IN ('REVIEW_REMINDER','DAILY_PLAN','STREAK');
```

## 24.3 Recommended Flyway order

Existing migration history không được renumber.

Append-only suggestion:

```text
V32__reconcile_assessment_state.sql
V33__create_assessment_items.sql
V34__create_goal_topics.sql
V35__create_daily_plan_items.sql
V36__create_user_devices.sql
V37__create_notification_preferences.sql
V38__extend_notifications_for_dedup.sql
V39__enforce_learning_attempt_quality.sql
V40__seed_gamification_badges.sql
```

Nếu repository hiện có migration >= V32 khi implementation bắt đầu, Agent phải chọn next available version thay vì overwrite.

---

# 25. API Specification / OpenAPI v1.4 reconciliation summary

## 25.1 Remove

```text
Idempotency-Key header component + references
202/jobId client-facing AI contract
Admin AI CEFR Suggestion V1 references
```

## 25.2 Add/change request eventId

Required body `eventId` cho endpoint list ở Section 19.6.

## 25.3 Add notification/device endpoints

```http
PUT    /api/v1/devices/{installationId}/push-token
DELETE /api/v1/devices/{installationId}
GET    /api/v1/notification-preferences
PUT    /api/v1/notification-preferences
```

## 25.4 DailyPlanItem schema

Required:

```text
itemId
itemType
priority
targetCount
completedCount
status
```

`vocabularyId` nullable.

## 25.5 New specific error codes

Add:

```text
ASSESSMENT_ALREADY_IN_PROGRESS
ASSESSMENT_QUESTION_ALREADY_ANSWERED
ASSESSMENT_CONTENT_UNAVAILABLE
```

Existing locked codes giữ nguyên:

```text
CONCURRENT_UPDATE
IDEMPOTENCY_KEY_REUSE
```

## 25.6 Admin AI generation

Canonical:

```http
POST /api/v1/admin/ai-content/generate
→ 201
```

Request includes:

```json
{
  "contentType": "EXAMPLE",
  "vocabularyId": "uuid",
  "eventId": "uuid"
}
```

## 25.7 Personalized exercise

Request includes:

```json
{
  "activityType": "FILL_BLANK",
  "count": 5,
  "eventId": "uuid"
}
```

---

# 26. Technical/Backend implementation rules

Backend v1.3 phải triển khai theo service authority sau:

```text
AssessmentService
SrsService
WeaknessService
ForgettingRiskService
RecommendationService
DailyPlanService
GamificationService
NotificationService
IdempotencyService
AiContentService
```

Không để Controller hoặc client tự implement formulas.

Constants phải nằm trong typed configuration/property classes, ví dụ:

```text
AssessmentProperties
SrsProperties
PersonalizationProperties
GamificationProperties
NotificationProperties
IdempotencyProperties
```

Default config phải match reconciliation.

Algorithm version identifiers:

```text
assessment-block-v1
sm2-ext-v1
weakness-rule-v1
forgetting-risk-v1
daily-plan-v1
gamification-v1
```

Internal logs/analytics có thể lưu version để audit.

---

# 27. Android Java v1.1 reconciliation

Android V1 phải cập nhật:

```text
Assessment start/answer DTO → eventId
Session start/complete DTO → eventId
Daily plan complete DTO → eventId
Personalized exercise DTO → eventId
No Idempotency-Key header
Canonical /api/v1/learning/personalized-exercise
DailyPlanItem vocabularyId nullable
DailyPlanItem supports aggregate QUIZ item
FCM installationId persistent per installation
PUT device push token
Notification preferences API
strings.xml Vietnamese single-locale
```

Client vẫn không tính:

```text
assessment CEFR
SRS
weakness
forgetting risk
recommendation
XP
level
streak
badge eligibility
quiz correctness
```

---

# 28. Flutter v1.1 reconciliation

Flutter là future client nhưng contract model phải align API v1.4:

```text
eventId body semantics
no Idempotency-Key
DailyPlanItem nullable vocabularyId
notification/device contract
new assessment requests
same exact error codes
```

Khi Flutter V2 được implement:

```text
use Flutter localization mechanism
```

Không cần xây Flutter V2 trong V1 milestone.

---

# 29. Admin Web v1.1 reconciliation

Admin stack:

```text
React + TypeScript + Vite
```

V1 changes:

```text
remove/mark Future AI CEFR Suggestion
use manual CEFR selection
canonical AI usage route
AI generation request includes eventId
AI generation synchronous 201
review_note mandatory for REJECT if current API requires reason
Vietnamese messages centralized
```

No direct database access.

---

# 30. PROJECT_RULES v1.2 required patch

Add a section immediately after Core Principle:

```text
Baseline Reconciliation Rule

When an APPROVED file exists under docs/reconciliation/ and is explicitly
listed as the active reconciliation baseline, every Agent must read it after
PROJECT_RULES.md and before feature-specific specifications.

The reconciliation file may override only the conflicts/decision IDs it
explicitly lists. Unrelated baseline content remains authoritative.
```

Update Localization Rule to Section 8 of this document.

Update official baseline list to target versions after regenerated docs are committed.

---

# 31. AGENTS.md required patch

Read order:

```text
docs/PROJECT_RULES.md
docs/reconciliation/English_AI_Coach_Baseline_Reconciliation_v1.0.md
relevant baseline docs
role-specific agent rules
```

Add locked constants introduced here.

Agent must not use stale rule from a dependent doc when reconciliation Decision ID explicitly overrides it.

---

# 32. Documentation hygiene — BR-024

Before declaring baseline clean:

1. remove all literal AI drafting citation artifacts such as:

```text
filecite...
```

2. ensure top-level section numbers are unique;
3. ensure filename version = header version;
4. update all `Related documents` references;
5. replace stale route strings;
6. avoid fragile cross-reference by number when possible; prefer filename + heading name;
7. no document may claim synchronization with an older baseline unless intentionally historical;
8. document `Status` must be one of:

```text
DRAFT
PROPOSED
APPROVED BASELINE
SUPERSEDED
ARCHIVED
```

---

# 33. Patch matrix by file

| File | Required reconciliation |
|---|---|
| `docs/PROJECT_RULES.md` | BR precedence, localization, target baseline |
| `AGENTS.md` | read order + target baseline + reconciliation awareness |
| `docs/requirements/English_AI_Coach_SRS_v1.2.md` | Admin stack; Assessment deterministic acceptance; gamification constants; notification contract intent; bump 1.2 |
| `docs/database/English_AI_Coach_Database_Schema_v1.6.md` | bump 1.6; 5 new tables; assessment expansion; notification dedup; answer quality CHECK; provenance |
| `docs/architecture/English_AI_Coach_System_Architecture_v1.3.md` | bump 1.3 metadata; routes; sync AI boundary; notification/device components; reconciliation provenance |
| `docs/ai/English_AI_Coach_AI_Personalization_Specification_v1.3.md` | bump 1.3; assessment block rule; SRS transitions; weak/risk formulas; deterministic recommendation; workload/allocation |
| `docs/api/English_AI_Coach_API_Specification_v1.4.md` | bump 1.4; eventId coverage; devices/preferences; sync AI; DailyPlanItem; errors; provenance |
| `docs/api/English_AI_Coach_OpenAPI_Swagger_v1_4.md` | bump 1.4; mirror API exactly; remove header idempotency; validate YAML |
| `docs/technical/English_AI_Coach_Technical_Specification_v1.2.md` | bump 1.2; implementation constants; routes; notification; sync AI; remove stale refs |
| `docs/technical/English_AI_Coach_Backend_Technical_Specification_v1.3.md` | bump 1.3; PostgreSQL idempotency strategy; new services/tables; remove filecite artifacts |
| `docs/mobile/...Android...v1_0_fixed.md` | bump 1.1; eventId DTOs; route; DailyPlanItem; FCM registration/preferences |
| `docs/flutter/...v1.0.md` | bump 1.1 contract alignment; localization rule |
| `docs/admin/...Technical...v1.0.md` | bump 1.1; remove V1 CEFR Suggestion; sync AI generate; route; localization |
| Admin UI/High-Fidelity/Prototype | remove/mark Future CEFR Suggestion; canonical routes |
| `docs/agents/ANTIGRAVITY_FRONTEND_LEAD.md` | reconcile localization rule |
| Reviewer agent docs | add checks for BR decisions where relevant |

---

# 34. Reviewer responsibilities after reconciliation

## Architecture Reviewer

Must verify:

```text
React Admin stack
no client-facing async job contract
notification/device flow
Daily Plan snapshot semantics
service boundaries
no V1 scope creep
```

## Database Reviewer

Must verify:

```text
5 new tables
assessment state constraints
daily_plan_items constraints
answer_quality/is_correct CHECK
notification dedup
idempotency retention/indexes
Flyway append-only migration order
```

## Security Reviewer

Must verify:

```text
eventId ownership
request hash includes path/body semantics
ON CONFLICT flow cannot become 500
AI retry does not double-charge budget
push tokens never exposed/logged
Admin authorization
review_note/audit integrity
```

## QA Reviewer

Must verify all acceptance tests in Section 36.

---

# 35. Implementation gates

## Gate A — FOUNDATION_READY

Can start immediately after reconciliation approval:

```text
Spring Boot skeleton
module boundaries
PostgreSQL
Flyway
JPA
Testcontainers
security/JWT foundation
common errors
logging/correlation
OpenAPI tooling
React/Vite Admin shell
Android shell/network/common layer
```

## Gate B — RECONCILIATION_IMPLEMENTATION_READY

May open full V1 implementation when:

```text
[ ] Reconciliation v1.0 approved by owner
[ ] PROJECT_RULES reconciliation precedence merged
[ ] AGENTS read-order patch merged
[ ] DB migration numbers reserved/verified
[ ] API/OpenAPI v1.4 contract patch generated or Agent is explicitly instructed to use reconciliation overlay
```

At this gate, implementation may use this document as temporary authoritative overlay.

## Gate C — BASELINE_CLEAN

Required before release candidate / final merge freeze:

```text
[ ] SRS v1.2 merged
[ ] DB v1.6 merged
[ ] Architecture v1.3 merged
[ ] AI v1.3 merged
[ ] API v1.4 merged
[ ] OpenAPI v1.4 merged and valid
[ ] Technical v1.2 merged
[ ] Backend v1.3 merged
[ ] Android v1.1 merged
[ ] Flutter v1.1 merged or explicitly marked contract-only future
[ ] Admin Web v1.1 merged
[ ] UI/prototype stale V1 flows removed/marked Future
[ ] no filecite artifacts
[ ] no duplicate top-level section numbering
[ ] no stale document-version references
[ ] reviewer passes complete
```

---

# 36. Mandatory reconciliation acceptance tests

## 36.1 answerQuality

```text
q=0,1,2 → isCorrect false
q=3,4,5 → isCorrect true
client payload with isCorrect → validation reject/ignore according to DTO strictness; preferred reject unknown field in contract tests
DB cannot persist q=5 + is_correct=false
```

## 36.2 SRS transition

```text
NEW + first q=5 → LEARNING
LEARNING + second successful repetition → REVIEWING
REVIEWING meeting R>=5/I>=30/q>=4 → MASTERED
MASTERED + q=2 → LEARNING
```

## 36.3 Optimistic locking

Two concurrent progress updates:

```text
one succeeds
one returns 409 CONCURRENT_UPDATE
no silent overwrite
```

## 36.4 Idempotency replay

```text
same eventId + same logical request
→ same status/body
→ one business mutation
→ one XP effect
```

## 36.5 Idempotency reuse

```text
same eventId + different vocabulary/request/path/user
→ 409 IDEMPOTENCY_KEY_REUSE
```

## 36.6 Concurrent duplicate eventId

Two simultaneous requests same event:

```text
no HTTP 500
one business mutation
second replay after claim conflict resolves
```

## 36.7 Assessment

```text
starts A1
4-question block 3/4 → promotes
4-question block 1/4 → demotes
4-question block 2/4 → holds
stops only after min 20 + 2 stable blocks, or at max 50
question never repeats in same assessment
retry answer same eventId does not double-count
```

## 36.8 Weakness

Test formula boundaries and:

```text
3 attempts, accuracy 2/3 → not weak by low accuracy
3 attempts, accuracy 1/3 → weak LOW_ACCURACY
2 consecutive failures → weak RECENT_FAILURES
```

## 36.9 Forgetting Risk

```text
NEW → LOW
high-quality repeated word before due → LOW/MEDIUM according formula
poor history + due → HIGH when score >= .70
```

## 36.10 Workload guard

```text
previous=10, requested raw=15 → final <=12
previous=10, requested raw=5 → final >=7
```

## 36.11 Daily Plan snapshot

```text
GET today twice → same planId + same ordered items
profile change mid-day → same current plan
next local day → new plan
QUIZ item vocabularyId = null allowed
```

## 36.12 Daily Plan XP

```text
all targets + complete → +50 once
retry same eventId → no duplicate
partial complete → 0 daily-plan completion XP
```

## 36.13 Streak

```text
multiple attempts same local day → one streak update/one streak XP
next consecutive day → +1
skip day → reset to 1 on next activity
concurrent activity → no duplicate daily increment
```

## 36.14 Badge

```text
same eligibility evaluated repeatedly → one user_badges row
perfect quiz requires >=5 questions
```

## 36.15 Notification

```text
same user/type/local day reminder cannot duplicate
push disabled → no FCM send
review disabled → no review reminder
FCM token refresh PUT updates same installation
```

## 36.16 AI generation

```text
admin generate → 201 PENDING_REVIEW
personalized exercise → 200
no 202/jobId V1
same eventId retry → no second LLM request/cost
```

## 36.17 AI review

```text
REJECT requires/stores review_note
admin_audit_logs action=REJECT
same reason is present in audit details
```

---

# 37. Backward compatibility policy

V1 chưa release public production contract, vì vậy reconciliation có thể thực hiện contract corrections trước first production release.

Không cần giữ compatibility với stale draft behaviors như:

```text
Idempotency-Key header
AI 202/jobId
Admin AI CEFR Suggestion
DailyPlanItem vocabularyId required for QUIZ
```

Sau khi API v1.4 được declared production baseline, breaking changes phải tuân theo explicit API version/change policy.

Flutter V2 phải implement API v1.4-or-newer approved contract, không implement stale v1.3 behavior.

---

# 38. Definition of Reconciled

Baseline được coi là **semantically reconciled** khi:

```text
all BR-001..BR-024 accepted
PROJECT_RULES references reconciliation
Agents have deterministic rules for every V1 backend-owned algorithm
no P0 issue requires Agent invention
```

Baseline được coi là **document-clean reconciled** khi:

```text
all target document versions are regenerated and cross-referenced correctly
OpenAPI is syntactically valid and contract-parity checked
all stale contradictory text is removed
all reviewer gates pass
```

---

# 39. Final implementation decision

Sau khi owner APPROVE tài liệu này và merge precedence patch vào `PROJECT_RULES.md`/`AGENTS.md`:

> **English AI Coach V1 chuyển từ “Ready with blockers” sang “RECONCILIATION_IMPLEMENTATION_READY”.**

Điều này cho phép Codex Backend Lead và Antigravity Frontend Lead bắt đầu full feature implementation mà không cần tự invent các business rules đã được đóng trong BR-001..BR-024.

Trước Release Candidate, project vẫn phải đạt `BASELINE_CLEAN` bằng cách integrate các decision này vào từng baseline document đích.

---

# 40. Approval block

```text
Document: English AI Coach Baseline Reconciliation v1.0
Decision range: BR-001..BR-024

Owner approval: ____________________
Date: ______________________________

Integration result:
Status → ARCHIVED — INTEGRATED into target baseline documents
```

---

# Integration Record

Integrated into the approved target baseline on 2026-08-31 by explicit project-owner instruction to execute the reconciliation step. This file is retained for decision provenance only; current approved baseline documents are authoritative.


## Integration Erratum — Table Count

Trong quá trình integrate đã phát hiện DB v1.5 có 29 table definitions thực tế (bao gồm `idempotency_keys`) dù metadata cũ ghi 28. Vì BR-005/010/012/014 thêm 5 bảng, Database v1.6 canonical có **34 tables**, không phải 33. Đây là correction bookkeeping; không thay đổi semantic decision BR-001..BR-024.
