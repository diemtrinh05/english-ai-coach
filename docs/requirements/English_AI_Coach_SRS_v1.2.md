# Software Requirements Specification (SRS)
# English AI Coach
**Status:** APPROVED BASELINE  

**Phiên bản:** 1.2  
**Trạng thái:** APPROVED BASELINE — Chốt phạm vi V1
**Ngày:** 2026-08-31  
**Loại hệ thống:** Ứng dụng học từ vựng tiếng Anh tích hợp AI cá nhân hóa  
**Database:** PostgreSQL  
**Backend:** Java Spring Boot  
**Mobile V1:** Android Java  
**Mobile tương lai:** Flutter  
**Admin Web:** React + TypeScript + Vite  
**Backend API:** Java Spring Boot

---

# 1. Tổng quan

## 1.1. Tên đề tài

**Xây dựng ứng dụng học từ vựng tiếng Anh tích hợp AI cá nhân hóa lộ trình học.**

Tên sản phẩm đề xuất:

> **English AI Coach**

---

## 1.2. Mô tả

English AI Coach là hệ thống hỗ trợ người học tiếng Anh tập trung vào **học và ghi nhớ từ vựng**, trong đó hệ thống phân tích dữ liệu học tập của từng người dùng để tạo ra lộ trình và kế hoạch học cá nhân hóa.

Phiên bản đầu tiên tập trung vào:

- Vocabulary learning.
- Flashcard.
- Quiz.
- Spaced Repetition.
- Weak Word Detection.
- Forgetting Risk.
- Vocabulary Recommendation.
- Difficulty Adjustment.
- Personalized Daily Plan.
- Progress Tracking.
- Gamification cơ bản.

AI nâng cao như Machine Learning và AI Tutor được đưa vào roadmap sau khi hệ thống đã thu thập đủ dữ liệu.

---

# 2. Mục tiêu dự án

## 2.1. Mục tiêu chính

Xây dựng một hệ thống học từ vựng có khả năng:

1. Xác định trình độ từ vựng ban đầu của người học.
2. Cho phép người dùng thiết lập mục tiêu và thời gian học.
3. Cung cấp nội dung từ vựng phù hợp với trình độ.
4. Theo dõi quá trình học của từng từ.
5. Xác định từ người dùng yếu hoặc có nguy cơ quên.
6. Tự động tính lịch ôn tập bằng Spaced Repetition.
7. Tạo kế hoạch học hàng ngày cá nhân hóa.
8. Điều chỉnh khối lượng học theo khả năng thực tế.
9. Theo dõi tiến bộ của người dùng.
10. Tạo nền tảng dữ liệu để phát triển Machine Learning trong tương lai.

---

## 2.2. Mục tiêu kỹ thuật

Hệ thống được xây dựng theo mô hình:

```text
Android Java
      │
      │ REST API / HTTPS
      ▼
Spring Boot Backend
      │
      ├── Authentication
      ├── Learning
      ├── Personalization
      ├── Quiz
      ├── Gamification
      └── AI Integration
      │
      ▼
PostgreSQL
```

Admin Web sử dụng cùng Backend:

```text
Admin Web
    │
    ▼
Spring Boot Backend
    │
    ▼
PostgreSQL
```

---

# 3. Phạm vi dự án

## 3.1. Phạm vi V1 — MVP

V1 bắt buộc tập trung vào **Vocabulary + AI Personalization**.

### Account

- Đăng ký.
- Đăng nhập.
- Đăng xuất.
- JWT authentication.
- Access Token + Refresh Token.
- Token expiry và revoke.
- Profile.
- Role USER / ADMIN.

### Onboarding

- Chọn mục tiêu học.
- Chọn thời gian học mỗi ngày.
- Kiểm tra trình độ từ vựng đầu vào.
- Xác định CEFR ban đầu.

### Vocabulary

- Danh sách từ.
- Chi tiết từ.
- Nghĩa tiếng Việt.
- Nghĩa tiếng Anh.
- IPA.
- Từ loại.
- CEFR.
- Topic.
- Audio phát âm.
- Ví dụ câu.
- Flashcard.

### Learning

- Học từ mới.
- Ôn từ cũ.
- Mini quiz.
- Ghi nhận kết quả từng lần trả lời.
- Theo dõi thời gian phản hồi.

### Personalization

- Adaptive Testing.
- Spaced Repetition.
- Weak Word Detection.
- Forgetting Risk heuristic.
- Vocabulary Recommendation.
- Topic Recommendation.
- Difficulty Adjustment.
- New/Review Ratio.
- Personalized Daily Plan.

### Progress

- Accuracy.
- Words learned.
- Words mastered.
- Learning time.
- Review progress.
- Goal progress.

### Gamification

- XP.
- Level.
- Streak.
- Badge.
- Leaderboard cơ bản.

### Notification

- Review reminder.
- Daily learning reminder.
- Streak reminder.

### Admin

- Quản lý user.
- Quản lý vocabulary.
- Quản lý topic.
- Quản lý CEFR.
- Quản lý quiz.
- Xem thống kê học tập.
- Quản lý AI-generated content.
- Xem AI usage/cost.
- Audit log.

---

# 4. Ngoài phạm vi V1

Các chức năng sau **không thuộc MVP**:

- Speaking.
- Writing.
- Listening.
- Reading.
- AI Tutor hội thoại.
- Machine Learning Forgetting Prediction.
- Collaborative Filtering.
- ML Recommendation.
- AI notification timing.
- AI pronunciation scoring.
- Speech recognition.
- Essay correction.
- Conversation voice bot.

Các chức năng này thuộc roadmap V2/V3.

---

# 5. Đối tượng sử dụng

## 5.1. Người học — USER

Người dùng chính của hệ thống.

Có thể:

- Quản lý tài khoản.
- Làm bài đánh giá đầu vào.
- Học từ.
- Ôn từ.
- Làm quiz.
- Xem tiến độ.
- Nhận kế hoạch học cá nhân hóa.
- Nhận notification.
- Tích XP và badge.

---

## 5.2. Quản trị viên — ADMIN

Có thể:

- Quản lý user.
- Quản lý vocabulary.
- Quản lý topic.
- Quản lý quiz.
- Duyệt nội dung AI.
- Xem thống kê.
- Xem AI usage.
- Xem audit log.

---

# 6. Kiến trúc hệ thống

```text
                    ┌─────────────────┐
                    │  Android Java   │
                    └────────┬────────┘
                             │
                             │ REST API
                             │
                    ┌────────▼────────┐
                    │                 │
                    │  Spring Boot    │
                    │    Backend      │
                    │                 │
                    └───────┬─────────┘
                            │
              ┌─────────────┼──────────────┐
              │             │              │
              ▼             ▼              ▼
        PostgreSQL       AI Service     Storage
                                      Audio / Image
              ▲
              │
        ┌─────┴──────┐
        │            │
        │ Admin Web  │
        │            │
        └────────────┘
```

---

# 7. Functional Requirements

# 8. Authentication

## FR-AUTH-01 — Đăng ký

Hệ thống phải cho phép người dùng tạo tài khoản bằng email và password.

### Input

```text
email
password
full_name
```

### Output

```text
user account
JWT / authentication result
```

### Rule

- Email không được trùng.
- Password phải được hash.
- Account mới có role USER.
- Account mới có status ACTIVE.

---

## FR-AUTH-02 — Đăng nhập

Người dùng có thể đăng nhập bằng:

```text
LOCAL
GOOGLE
```

Hệ thống trả JWT access token.

---

## FR-AUTH-03 — JWT Authentication

Các API yêu cầu đăng nhập phải xác thực JWT.

JWT chứa tối thiểu:

```text
user_id
role
```

---

## FR-AUTH-04 — Role Authorization

Hệ thống phải phân quyền:

```text
USER
ADMIN
```

USER không được truy cập Admin API.

## FR-AUTH-05 — Access Token & Refresh Token

Hệ thống phải sử dụng:

```text
Short-lived Access Token
+
Long-lived Refresh Token
```

### Access Token

Access Token dùng để xác thực các API.

Thời gian sống mục tiêu:

```text
15–30 phút
```

Giá trị cụ thể phải được cấu hình bằng application configuration.

JWT payload tối thiểu:

```text
user_id
role
iat
exp
```

### Refresh Token

Refresh Token dùng để cấp Access Token mới khi Access Token hết hạn.

Refresh Token phải có:

```text
expiration
revocation
```

Refresh Token nên được lưu dưới dạng hash trong database, không lưu plaintext.

Có thể gắn Refresh Token với thiết bị/phiên đăng nhập.

Khi logout:

```text
Refresh Token → revoked
```

Refresh Token đã hết hạn hoặc đã revoke không được sử dụng để cấp Access Token mới.

---

# 9. Profile

## FR-PROFILE-01

Người dùng có thể xem profile.

Thông tin:

```text
full_name
avatar
CEFR
daily_learning_minutes
timezone
goals
```

---

## FR-PROFILE-02

Người dùng có thể cập nhật:

```text
full_name
avatar
daily_learning_minutes
timezone
```

---

# 10. Onboarding

## FR-ONBOARD-01 — Chọn mục tiêu

Người dùng chọn một hoặc nhiều mục tiêu:

```text
GENERAL_ENGLISH
TRAVEL
BUSINESS
TOEIC
IELTS
COMMUNICATION
ACADEMIC
```

Có thể xác định một mục tiêu chính.

---

## FR-ONBOARD-02 — Chọn thời gian học

Người dùng chọn số phút học mỗi ngày.

Ví dụ:

```text
10
15
20
30
45
60
```

---

## FR-ONBOARD-03 — Kiểm tra đầu vào

Hệ thống cung cấp bài kiểm tra từ vựng để ước lượng CEFR.

V1 sử dụng:

```text
Rule-based Adaptive Testing
```

Không cần Machine Learning.

---

## FR-ONBOARD-04 — Adaptive Testing

Nếu người dùng trả lời đúng:

```text
difficulty ↑
```

Nếu trả lời sai:

```text
difficulty ↓
```

Hệ thống kết thúc khi đạt điều kiện xác định trình độ.

---

# 11. Vocabulary

## FR-VOC-01 — Xem danh sách từ

User có thể xem danh sách từ theo:

```text
topic
CEFR
search keyword
```

Hỗ trợ:

```text
pagination
sorting
filtering
```

---

## FR-VOC-02 — Xem chi tiết từ

Thông tin:

```text
word
IPA
meaning_vi
meaning_en
part_of_speech
CEFR
topic
audio
example
```

---

## FR-VOC-03 — Flashcard

Flashcard hiển thị:

```text
Word
→
Meaning
→
Example
→
Audio
```

---

## FR-VOC-04 — TTS

Hệ thống cung cấp audio phát âm.

Audio có thể được tạo bằng TTS và lưu/cache ở Object Storage.

Database chỉ lưu:

```text
audio_url
```

---

# 12. Learning

## FR-LEARN-01 — Daily Learning Plan

Khi người dùng mở trang học hôm nay, hệ thống trả kế hoạch:

```text
New Words
Review Words
Quiz
Estimated Time
```

---

## FR-LEARN-02 — Học từ mới

User học các từ mới theo Daily Plan.

Sau mỗi từ, hệ thống có thể yêu cầu một hoạt động kiểm tra.

---

## FR-LEARN-03 — Ôn từ

Hệ thống lấy những từ có:

```text
next_review_at <= current_time
```

để đưa vào danh sách ôn.

---

## FR-LEARN-04 — Ghi nhận Attempt

Mỗi lần người dùng trả lời, hệ thống lưu:

```text
vocabulary
is_correct
response_time
answer_quality
attempted_at
```

---

## FR-LEARN-05 — Cập nhật SRS

Sau attempt, SRS Engine cập nhật:

```text
ease_factor
interval_days
repetitions
next_review_at
status
```

---

# 13. Spaced Repetition

## FR-SRS-01

Hệ thống phải tính thời điểm ôn tiếp theo cho từng từ.

V1 sử dụng:

```text
SM-2 mở rộng
```

hoặc một biến thể SRS tương đương.

---

## FR-SRS-02

Kết quả tốt:

```text
interval tăng
```

Kết quả kém:

```text
interval giảm
```

---

## FR-SRS-03

Mỗi user có trạng thái SRS riêng cho từng vocabulary.

Quan hệ:

```text
USER
  +
VOCABULARY
  ↓
USER_VOCABULARY_PROGRESS
```

---

# 14. Weak Word Detection

## FR-AI-01

Hệ thống phải xác định các từ người dùng đang yếu.

V1 sử dụng heuristic dựa trên:

```text
incorrect_count
correct_count
accuracy
response_time
answer_quality
recent_attempts
```

---

## FR-AI-02

Một từ có thể được xem là yếu nếu:

```text
accuracy thấp
OR
sai nhiều lần
OR
response time cao
OR
answer quality thấp
```

Các threshold được cấu hình ở Backend.

---

# 15. Forgetting Risk

## FR-AI-03

Hệ thống phải ước lượng nguy cơ quên từ.

V1 sử dụng heuristic.

Các yếu tố:

```text
days_since_last_review
interval_days
historical_accuracy
incorrect_count
last_quality
repetitions
```

Output:

```text
LOW
MEDIUM
HIGH
```

hoặc score:

```text
0.0 → 1.0
```

---

# 16. Vocabulary Recommendation

## FR-AI-04

Hệ thống đề xuất từ tiếp theo dựa trên:

```text
user CEFR
goals
topics
learning history
weak words
review schedule
```

V1:

```text
Rule-based
```

---

# 17. Topic Recommendation

## FR-AI-05

Hệ thống đề xuất topic dựa trên:

```text
user goal
current CEFR
completed topics
learning history
```

V1 sử dụng rule-based.

---

# 18. Difficulty Adjustment

## FR-AI-06

Hệ thống tự điều chỉnh lượng từ mới/ngày.

Nếu:

```text
completion rate cao
accuracy cao
```

→ tăng workload.

Nếu:

```text
completion rate thấp
accuracy thấp
```

→ giảm workload.

---

# 19. New/Review Ratio

## FR-AI-07

Hệ thống cân bằng:

```text
New Words
+
Review Words
```

Mục tiêu là tránh việc người dùng học quá nhiều từ mới nhưng không ôn đủ từ cũ.

---

# 20. Personalized Daily Plan

## FR-AI-08

Daily Plan được tạo dựa trên:

```text
CEFR
Goals
Daily learning minutes
SRS due words
Weak words
Forgetting Risk
Learning history
Completion rate
Accuracy
New/Review ratio
```

Output:

```text
new_words_target
review_words_target
quiz_target
estimated_minutes
```

---

# 21. Quiz

## FR-QUIZ-01

Hệ thống hỗ trợ:

```text
MULTIPLE_CHOICE
FILL_BLANK
MATCHING
```

---

## FR-QUIZ-02

Quiz có thể được gắn:

```text
topic
CEFR
vocabulary
```

---

## FR-QUIZ-03

Hệ thống lưu:

```text
quiz attempt
score
correct answers
user answers
response time
```

---

# 22. Progress

## FR-PROGRESS-01

Dashboard hiển thị:

```text
Words Learned
Words Mastered
Accuracy
Learning Time
Review Progress
Goal Progress
Current Streak
XP
```

---

## FR-PROGRESS-02

Hệ thống hiển thị các từ yếu.

---

## FR-PROGRESS-03

Hệ thống hiển thị xu hướng tiến bộ theo thời gian.

---

# 23. Gamification

## FR-GAME-01 — XP

User nhận XP khi:

```text
complete lesson
correct answer
complete daily plan
maintain streak
```

---

## FR-GAME-02 — Level

Level dựa trên tổng XP.

Ví dụ:

```text
Level 1
Level 2
Level 3
...
```

---

## FR-GAME-03 — Streak

Hệ thống theo dõi:

```text
current_streak
longest_streak
```

---

## FR-GAME-04 — Badge

Ví dụ:

```text
First Lesson
7 Day Streak
100 Words
500 Words
Perfect Quiz
```

---

## FR-GAME-05 — Leaderboard

Hiển thị ranking dựa trên XP.

---

# 24. Notification

## FR-NOTI-01

Hệ thống gửi notification khi:

```text
đến lịch ôn
đến giờ học
sắp mất streak
có Daily Plan mới
```

V1 sử dụng lịch cố định hoặc lịch dựa trên SRS.

V2 mới nghiên cứu ML tối ưu thời điểm gửi.

---

# 25. Admin Functional Requirements

# 26. User Management

## FR-ADMIN-USER-01

Admin có thể:

```text
view users
search users
filter users
lock user
unlock user
```

---

# 27. Vocabulary Management

## FR-ADMIN-VOC-01

Admin có thể:

```text
create vocabulary
edit vocabulary
deactivate vocabulary
search vocabulary
filter CEFR
filter topic
```

---

# 28. Topic Management

Admin có thể:

```text
create topic
edit topic
deactivate topic
create parent/child topic
```

---

# 29. Quiz Management

Admin có thể:

```text
create quiz
edit quiz
add questions
remove questions
publish/unpublish quiz
```

---

# 30. AI Content Management

Admin có thể xem:

```text
PENDING_REVIEW
APPROVED
REJECTED
```

Admin có thể:

```text
approve
reject
```

AI-generated content.

---

# 31. AI Usage

Admin có thể xem:

```text
request count
token usage
estimated cost
provider
model
feature
date
```

---

# 32. Audit Log

Hệ thống ghi nhận hành động Admin:

```text
CREATE
UPDATE
DELETE/DEACTIVATE
APPROVE
REJECT
LOCK_USER
UNLOCK_USER
```

---

# 33. AI Requirements

## 33.1. AI V1

AI/personalization V1 gồm:

```text
Adaptive Testing
Spaced Repetition
Weak Word Detection
Forgetting Risk
Recommendation
Difficulty Adjustment
Daily Plan
```

Phần lớn là:

```text
Rule-based
Heuristic
Algorithm
```

Không cần train model.

---

## 33.2. AI Content V1+

Sau khi Core ổn định:

```text
AI Example
AI Explanation
AI Quiz
AI Story
AI Mnemonic
AI Personalized Exercise
```

Sử dụng LLM API.

---

## 33.3. AI Content Cache

Nội dung LLM phải được cache.

Luồng:

```text
Request
 ↓
Check Cache
 ↓
Exists?
 ├── YES → Return cached content
 │
 └── NO → Call LLM
            ↓
        Save Content
            ↓
        Admin Review
```

---

## 33.4. AI Content Review

Nội dung AI sinh ra có trạng thái:

```text
PENDING_REVIEW
APPROVED
REJECTED
```

User chỉ được xem nội dung:

```text
APPROVED
```

---

# 34. Business Rules

## BR-01

Một email chỉ được đăng ký một tài khoản.

---

## BR-02

User mặc định có role:

```text
USER
```

---

## BR-03

Chỉ ADMIN mới truy cập Admin API.

---

## BR-04

Một user chỉ có một `user_profiles`.

---

## BR-05

Một user có thể có nhiều goals nhưng tối đa một goal chính.

---

## BR-06

Một vocabulary có thể thuộc nhiều topics.

---

## BR-07

Một vocabulary không được trùng theo:

```text
word
+
part_of_speech
+
cefr_level
```

---

## BR-08

Một user chỉ có một record:

```text
user_vocabulary_progress
```

cho mỗi vocabulary.

Constraint:

```text
UNIQUE(user_id, vocabulary_id)
```

---

## BR-09

SRS phải cập nhật sau attempt hợp lệ.

---

## BR-10

Daily Plan có tối đa một record cho mỗi user mỗi ngày.

```text
UNIQUE(user_id, plan_date)
```

---

## BR-11

AI-generated content chỉ được publish khi:

```text
status = APPROVED
```

---

## BR-12

Vocabulary/Topic/Quiz có thể bị deactivate thay vì xóa cứng.

## BR-13

Access Token phải có thời gian hết hạn.

---

## BR-14

Refresh Token phải có thời gian hết hạn và có thể bị revoke.

---

## BR-15

Refresh Token đã revoke hoặc hết hạn không được dùng để cấp Access Token mới.

---

## BR-16

Một user có thể có nhiều goals nhưng tối đa một goal có:

```text
is_primary = true
```

Database phải enforce bằng partial unique index:

```sql
CREATE UNIQUE INDEX uq_user_goals_primary
ON user_goals(user_id)
WHERE is_primary = true;
```

---

## BR-17

Hệ thống phải áp dụng brute-force protection cho login theo cấu hình.

---

## BR-18

AI generation phải kiểm tra budget trước khi gọi LLM provider.

---

## BR-19

Khi vượt AI budget, AI generation không thiết yếu phải bị giới hạn hoặc tạm dừng; nội dung đã cache/approved vẫn có thể sử dụng.

---

# 35. Non-Functional Requirements

# 36. Security

## NFR-SEC-01

Password phải được hash bằng thuật toán an toàn, ví dụ:

```text
BCrypt
```

---

## NFR-SEC-02

API authentication sử dụng JWT.

---

## NFR-SEC-03

Authorization dựa trên role.

---

## NFR-SEC-04

Không trả:

```text
password_hash
```

trong API response.

---

## NFR-SEC-05

API sử dụng HTTPS trong production.

## NFR-SEC-06 — Brute-force Login Protection

Hệ thống phải giới hạn số lần đăng nhập thất bại liên tiếp.

Baseline V1:

```text
5 lần đăng nhập sai liên tiếp
        ↓
Tạm khóa đăng nhập 5 phút
        ↓
Cho phép thử lại
```

Yêu cầu:

- Không khóa vĩnh viễn tài khoản chỉ vì đăng nhập sai.
- Reset bộ đếm khi đăng nhập thành công.
- Threshold và thời gian khóa phải cấu hình được.
- Không tiết lộ thông tin giúp attacker xác định email có tồn tại hay không.

---

# 37. Performance

## NFR-PERF-01

Các API thông thường phải có response time phù hợp với mobile app.

Mục tiêu ban đầu:

```text
p95 < 500ms
```

không tính các request phụ thuộc dịch vụ AI bên ngoài.

---

## NFR-PERF-02

Danh sách phải hỗ trợ pagination.

---

## NFR-PERF-03

Query SRS phải sử dụng index:

```text
(user_id, next_review_at)
```

---

## NFR-PERF-04

AI content nên được cache để giảm số lần gọi LLM.

## NFR-AI-COST-01 — AI Budget & Usage Guard

Hệ thống phải có cơ chế kiểm soát chi phí AI.

Phải hỗ trợ cấu hình:

```text
daily request limit
daily token limit
daily estimated cost limit
```

Luồng:

```text
AI Request
    ↓
Budget Check
    ↓
Exceeded?
 ├── NO  → Continue
 └── YES → Block non-critical AI generation
```

Khi vượt ngưỡng, các AI feature không thiết yếu phải được giới hạn hoặc tạm dừng.

Nội dung đã cache/approved vẫn được phục vụ bình thường.

Hệ thống phải ghi nhận sự kiện vượt budget để Admin theo dõi.

Giá trị budget cụ thể không cố định trong SRS; cấu hình theo môi trường:

```text
development
staging
production
```

---

# 38. Scalability

## NFR-SCALE-01

Backend phải stateless ở mức application layer để có thể scale horizontally.

---

## NFR-SCALE-02

Database sử dụng UUID.

---

## NFR-SCALE-03

File audio/image không lưu trực tiếp trong PostgreSQL.

Database chỉ lưu URL.

---

# 39. Maintainability

## NFR-MAINT-01

Backend áp dụng layer:

```text
Controller
Service
Repository
Entity
DTO
```

---

## NFR-MAINT-02

Database migration sử dụng Flyway.

---

## NFR-MAINT-03

API có version:

```text
/api/v1/...
```

---

# 40. Compatibility

Mobile V1:

```text
Android Java
```

Mobile V2:

```text
Flutter
```

Cả hai sử dụng cùng REST API.

---

# 41. Data Requirements

Database chính:

```text
PostgreSQL
```

Các nhóm dữ liệu:

```text
Account
Vocabulary
Learning
Quiz
Gamification
Notification
AI
Admin
```

Database schema chi tiết được định nghĩa trong:

```text
English_AI_Coach_Database_Schema_v1.6.md
```

Do bổ sung FR-AUTH-05, database cần có bảng:

```text
refresh_tokens
```

Tối thiểu gồm:

```text
id
user_id
token_hash
expires_at
revoked_at
created_at
device_info
```

và index phù hợp cho `user_id`, `expires_at` và token identifier/hash.


---

# 42. Learning Data Requirements

Hệ thống phải lưu dữ liệu đủ để phân tích hành vi:

```text
is_correct
response_time_ms
answer_quality
attempted_at
correct_count
incorrect_count
ease_factor
interval_days
repetitions
```

Mục đích:

```text
V1
Rule-based Personalization

↓

V2
Machine Learning
```

---

# 43. ML Future Requirements

Không train ML ngay từ đầu.

Sau khi thu thập đủ dữ liệu:

```text
SESSION_ATTEMPTS
       ↓
Data Cleaning
       ↓
Feature Engineering
       ↓
Dataset
       ↓
ML Model
```

Có thể triển khai:

```text
Forgetting Prediction
Recommendation
Difficulty Prediction
Error Classification
Notification Timing
Progress Prediction
```

---

# 44. AI Content Future Requirements

Roadmap:

```text
V1
Core + Rule-based Personalization

V1.5
LLM Content Generation

V2
ML Personalization

V3
AI Tutor + Speaking + Writing
```

---

# 45. User Flow chính

## 45.1. First-time User

```text
Open App
   ↓
Register/Login
   ↓
Profile Setup
   ↓
Select Goals
   ↓
Select Daily Learning Time
   ↓
Initial Vocabulary Test
   ↓
Determine CEFR
   ↓
Generate Initial Daily Plan
   ↓
Home
```

---

## 45.2. Daily Learning

```text
Open App
   ↓
Home
   ↓
Today's Plan
   ↓
Review Due Words
   ↓
New Words
   ↓
Flashcard
   ↓
Quiz
   ↓
Record Attempts
   ↓
Update SRS
   ↓
Update Progress
   ↓
Update XP/Streak
   ↓
Complete Plan
```

---

# 46. Personalized Learning Flow

```text
User Data
    │
    ├── CEFR
    ├── Goals
    ├── Daily Time
    ├── Learning History
    ├── Accuracy
    ├── Response Time
    ├── Weak Words
    └── SRS State
            │
            ▼
    Personalization Engine
            │
      ┌─────┼─────┐
      ▼     ▼     ▼
     SRS  Weak  Risk
      │     │     │
      └─────┼─────┘
            ▼
   Recommendation
            │
            ▼
       Daily Plan
```

---

# 47. Use Case Summary

## USER

```text
UC-01 Register
UC-02 Login
UC-03 Manage Profile
UC-04 Select Goal
UC-05 Take Placement Test
UC-06 Browse Vocabulary
UC-07 View Vocabulary
UC-08 Study Flashcard
UC-09 Review Vocabulary
UC-10 Take Quiz
UC-11 View Daily Plan
UC-12 View Progress
UC-13 View Weak Words
UC-14 Receive Notification
UC-15 Earn XP
UC-16 Maintain Streak
UC-17 View Badges
UC-18 View Leaderboard
```

## ADMIN

```text
UC-19 Manage Users
UC-20 Manage Vocabulary
UC-21 Manage Topics
UC-22 Manage CEFR
UC-23 Manage Quiz
UC-24 Review AI Content
UC-25 View AI Usage
UC-26 View Learning Statistics
UC-27 View Audit Logs
```

---

# 48. Traceability giữa Requirement và Module

| Requirement Group | Backend Module | Database |
|---|---|---|
| Authentication | Auth | users |
| Profile | User | user_profiles |
| Goals | Onboarding | goals, user_goals |
| CEFR | Assessment | cefr_levels, user_level_assessments |
| Vocabulary | Vocabulary | vocabulary, topics |
| Learning | Learning | learning_sessions, session_attempts |
| SRS | Personalization | user_vocabulary_progress |
| Daily Plan | Personalization | daily_plans |
| Quiz | Quiz | quizzes, quiz_questions, quiz_attempts |
| Progress | Analytics | learning/session data |
| Gamification | Gamification | streaks, xp_logs, badges |
| Notification | Notification | notifications |
| AI Content | AI | ai_generated_content |
| AI Usage | AI | ai_requests, ai_usage_daily |
| Admin | Admin | admin_audit_logs |

---

# 49. API Design Principles

API sẽ được thiết kế sau khi SRS được chốt.

Nguyên tắc:

```text
REST
/api/v1/...
JSON
JWT
Pagination
Filtering
Validation
Standard Error Response
Role-based Authorization
```

API không expose trực tiếp implementation của AI.

Ví dụ:

```text
GET /api/v1/learning/today
```

Backend tự quyết định:

```text
SRS
+
Weak Word Detection
+
Recommendation
+
Difficulty Adjustment
```

Client chỉ nhận:

```text
Today's Learning Plan
```

Điều này cho phép thay thế:

```text
Rule-based
      ↓
ML
      ↓
ML + LLM
```

mà không cần thay đổi API của Mobile App.

---

# 50. Technology Baseline

## Backend

```text
Java
Spring Boot
Spring Web
Spring Security
Spring Data JPA
Hibernate
Bean Validation
Flyway
JWT
OpenAPI / Swagger
```

---

## Database

```text
PostgreSQL
```

---

## Mobile V1

```text
Android
Java
REST API
JWT
```

---

## Mobile V2

```text
Flutter
Dart
REST API
```

---

## Admin

```text
React + TypeScript + Vite
Web UI via Spring Boot REST API
```

---

## AI

V1:

```text
Rule-based
Heuristic
SRS Algorithm
```

AI Content:

```text
LLM API
```

V2:

```text
Machine Learning
```

---

# 51. MVP Acceptance Criteria

MVP được xem là hoàn thành khi:

### Account

```text
[ ] User có thể register.
[ ] User có thể login.
[ ] JWT hoạt động.
[ ] Role USER/ADMIN hoạt động.
```

### Onboarding

```text
[ ] User chọn goal.
[ ] User chọn thời gian học.
[ ] User làm placement test.
[ ] Hệ thống xác định CEFR.
```

### Vocabulary

```text
[ ] Admin tạo vocabulary.
[ ] User xem vocabulary.
[ ] Flashcard hoạt động.
[ ] Audio hoạt động.
[ ] Example hoạt động.
```

### Learning

```text
[ ] User học từ.
[ ] User ôn từ.
[ ] Attempt được lưu.
[ ] SRS cập nhật.
[ ] Next review được tính.
```

### Personalization

```text
[ ] Weak words được phát hiện.
[ ] Forgetting Risk được tính.
[ ] Vocabulary recommendation hoạt động.
[ ] Difficulty adjustment hoạt động.
[ ] Daily Plan được tạo.
```

### Quiz

```text
[ ] User làm quiz.
[ ] Score được tính.
[ ] Answers được lưu.
```

### Progress

```text
[ ] Accuracy hiển thị.
[ ] Words learned hiển thị.
[ ] Words mastered hiển thị.
[ ] Learning time hiển thị.
```

### Gamification

```text
[ ] XP hoạt động.
[ ] Level hoạt động.
[ ] Streak hoạt động.
[ ] Badge hoạt động.
```

### Admin

```text
[ ] User management hoạt động.
[ ] Vocabulary management hoạt động.
[ ] Topic management hoạt động.
[ ] Quiz management hoạt động.
[ ] AI content review hoạt động.
[ ] AI usage statistics hoạt động.
```


### Security & AI Operations

```text
[ ] Access Token có expiry.
[ ] Refresh Token có expiry.
[ ] Refresh Token có thể revoke.
[ ] Logout revoke Refresh Token.
[ ] Brute-force login protection hoạt động.
[ ] AI budget guard hoạt động.
[ ] AI request/token/cost được ghi nhận.
[ ] AI generation bị giới hạn khi vượt budget.
```

---

# 52. Definition of Done

Một chức năng chỉ được xem là hoàn thành khi:

```text
Requirement
    ↓
Database
    ↓
Entity
    ↓
Repository
    ↓
Service
    ↓
Controller/API
    ↓
Validation
    ↓
Error Handling
    ↓
API Test
    ↓
Mobile/Admin Integration
```

và có:

```text
[ ] Happy path
[ ] Error path
[ ] Authentication
[ ] Authorization
[ ] Validation
[ ] Logging
```

---

# 53. Roadmap chính thức

## Phase 0 — Analysis & Design

```text
✓ Project Definition
✓ Scope
✓ Functional Requirements
✓ Non-functional Requirements
✓ AI Scope
✓ SRS
✓ Database Schema
```

## Phase 1 — Backend Foundation

```text
Spring Boot
PostgreSQL
Flyway
JPA
Security
JWT
Access Token + Refresh Token
Brute-force Protection
Exception Handling
Validation
Swagger
```

## Phase 2 — Core Backend

```text
Authentication
User
Onboarding
Vocabulary
Topics
CEFR
Learning
Quiz
Progress
```

## Phase 3 — Personalization V1

```text
SRS
Weak Word Detection
Forgetting Risk
Recommendation
Difficulty Adjustment
Daily Plan
```

## Phase 4 — Android Java

```text
Authentication
Onboarding
Home
Vocabulary
Flashcard
Learning
Quiz
Progress
Gamification
Notification
```

## Phase 5 — Admin

```text
Dashboard
User Management
Vocabulary Management
Topic Management
Quiz Management
AI Content
AI Usage
Audit
```

## Phase 6 — AI Content

```text
AI Example
AI Explanation
AI Quiz
AI Story
AI Mnemonic
Personalized Exercise
```

## Phase 7 — ML

```text
Data Collection
Feature Engineering
Dataset
Model Training
Forgetting Prediction
Recommendation
Difficulty Prediction
```

## Phase 8 — Flutter

```text
Flutter App
       ↓
Reuse Spring Boot API
       ↓
Reuse PostgreSQL
```

## Phase 9 — Advanced Learning

```text
Speaking
Writing
Listening
Reading
AI Tutor
Voice AI
```

---

# 54. Các quyết định đã chốt

## Decision 01

Phạm vi V1 tập trung vào:

> **Vocabulary + AI Personalization**

---

## Decision 02

AI V1 không cần train model.

```text
Rule-based
+
Heuristic
+
Algorithm
```

---

## Decision 03

LLM chỉ dùng cho AI Content ở giai đoạn phù hợp.

---

## Decision 04

Mobile V1:

```text
Android Java
```

Mobile tương lai:

```text
Flutter
```

---

## Decision 05

Backend:

```text
Java Spring Boot
```

---

## Decision 06

Database:

```text
PostgreSQL
```

---

## Decision 07

Database migration:

```text
Flyway
```

---

## Decision 08

API version:

```text
/api/v1/
```

---

## Decision 09

Authentication:

```text
JWT
LOCAL
GOOGLE
```

---

## Decision 10

AI Content phải có cache và trạng thái review:

```text
PENDING_REVIEW
APPROVED
REJECTED
```

## Decision 11

Authentication sử dụng:

```text
Short-lived Access Token
+
Refresh Token
```

Refresh Token được lưu dạng hash và có expiry/revoke.

---

## Decision 12

`user_goals` phải enforce tối đa một primary goal/user bằng:

```sql
CREATE UNIQUE INDEX uq_user_goals_primary
ON user_goals(user_id)
WHERE is_primary = true;
```

---

## Decision 13

Login có brute-force protection với baseline:

```text
5 failed attempts
→ 5-minute temporary lock
```

Các giá trị có thể cấu hình.

---

## Decision 14

AI có budget guard theo:

```text
request
token
estimated cost
```

và có thể chặn AI generation không thiết yếu khi vượt ngưỡng.

---

# 55. Tài liệu liên quan

```text
English_AI_Coach_Database_Schema_v1.6.md
```

Tài liệu Database Schema phải được đối chiếu với SRS trước khi triển khai API và JPA.

---

# 56. Ghi chú cập nhật từ SRS v1.0 → v1.1

Các thay đổi chính:

```text
1. Enforce tối đa một primary goal/user ở Database.
2. Bổ sung Access Token expiry.
3. Bổ sung Refresh Token.
4. Bổ sung Refresh Token expiry và revoke.
5. Bổ sung lưu Refresh Token dạng hash.
6. Bổ sung brute-force login protection.
7. Bổ sung AI budget/cost guard.
8. Bổ sung acceptance criteria cho security và AI cost control.
```

SRS v1.2 là baseline để thiết kế API Specification và cập nhật Database Schema tương ứng.

---

# 57. Bước phát triển tiếp theo

Sau khi SRS v1.2 được chốt:

```text
SRS v1.2
      ↓
API Specification v1.4
      ↓
Review API ↔ Database
      ↓
PostgreSQL + Flyway
      ↓
JPA Entity
      ↓
Spring Boot Backend
      ↓
API Testing
      ↓
Android Java
```

**SRS này là baseline yêu cầu cho V1. Những chức năng ngoài phạm vi V1 chỉ được triển khai sau khi MVP đạt Acceptance Criteria.**

---

# Reconciled V1 Normative Requirements (BR-001..BR-024)

This section is normative for SRS v1.2 and supersedes any earlier illustrative wording that conflicts with it.

## Adaptive Assessment

- Starts at A1; CEFR order is A1→A2→B1→B2→C1→C2.
- 4 questions/block. `>=3` correct promotes one level; `<=1` demotes one level; exactly `2` holds. A1/C2 clamp at the boundary.
- Minimum 20 questions, maximum 50. Complete after at least 20 questions **and** two consecutive HOLD blocks at the same level, or at 50 questions. Final CEFR is current difficulty.
- Placement question V1 is vocabulary-meaning MCQ with 4 options. Questions never repeat within one assessment and are persisted before being sent. Each CEFR requires at least 30 usable active vocabulary records; otherwise return `ASSESSMENT_CONTENT_UNAVAILABLE`.
- Assessment session and answers are persisted and retry-safe. Start and answer mutations require `eventId`.

## SRS learning state

```text
NEW: no accepted attempt. First accepted attempt → LEARNING.
LEARNING: q<3 stays LEARNING; q>=3 and new repetitions>=2 → REVIEWING.
REVIEWING: q<3 → LEARNING; q>=3 stays REVIEWING unless mastered.
MASTERED entry: q>=4 AND repetitions>=5 AND interval>=30 days.
MASTERED: q>=3 stays MASTERED; q<3 → LEARNING.
```

`wordsLearned = count(status in LEARNING, REVIEWING, MASTERED)` and `wordsMastered = count(status=MASTERED)`.

## Personalization

V1 is deterministic/rule-based. Canonical algorithms and constants are defined in AI Personalization v1.3: `weakness-rule-v1`, `forgetting-risk-v1`, deterministic recommendation ranking, workload guard and new/review/quiz allocation. Weighted recommendation formulas without frozen weights are not normative V1.

Daily workload raw adjustment defaults: HIGH performance `+10%`, LOW `-20%`, otherwise `0%`, always bounded by `+20%/-30%` versus previous target.

## Daily Plan

Daily Plan is a persisted snapshot keyed by `(user_id, plan_date)` in the user's timezone. Ordered items are persisted in `daily_plan_items`. Once created, targets/items do not regenerate mid-day. `REVIEW`/`NEW` items reference one vocabulary; `QUIZ` is one aggregate item with `vocabularyId = null` and `targetCount = quiz questions`.

Completing all targets and finalizing the plan awards Daily Plan XP once. Finalizing before all targets marks `PARTIAL` and awards no Daily Plan completion XP.

## Gamification

Canonical XP values:

```text
Correct learning attempt = 5 XP
Correct quiz answer = 5 XP
Learning session completed = 10 XP
Daily Plan completed = 50 XP
Streak maintained on a new qualifying local day = 10 XP
```

Level is derived: `level = floor(totalXp / 500) + 1`; `nextLevelXp = level * 500`. Seeded badges: `FIRST_LESSON`, `STREAK_7`, `WORDS_100`, `WORDS_500`, `PERFECT_QUIZ` (completed quiz, at least 5 questions, all correct).

Streak day boundary uses `user_profiles.timezone`; assessment activity does not qualify for streak.

## Notification

V1 includes FCM push. Backend stores installation/token in `user_devices` and user preferences in `notification_preferences`. Defaults: preferred study time 19:00 local; Daily Plan reminder 07:00 local; streak reminder 21:00 local. Review reminder is sent only when due reviews exist. Scheduled reminder type/user/local-date must be deduplicated.

## Idempotency and concurrency

Body `eventId` is the sole V1 idempotency key for logical mutation operations; clients do not send an idempotency HTTP header. Retention is 30 days. Same event/user/logical request replays; key reuse with a different logical request returns `409 IDEMPOTENCY_KEY_REUSE`. Optimistic locking conflicts use `409 CONCURRENT_UPDATE`.

## AI content

Reusable AI content is generated synchronously, persisted as `PENDING_REVIEW`, and requires Admin approval before publish. Personalized real-time exercise is synchronous, validated/safety-filtered, and does not require per-result Admin review. Client-facing `202/jobId` is not part of V1. Admin AI CEFR Suggestion is Future/V2; Admin selects CEFR manually in V1.
