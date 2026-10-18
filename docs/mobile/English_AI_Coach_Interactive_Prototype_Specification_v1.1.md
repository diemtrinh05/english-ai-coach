# Interactive Prototype Specification v1.1 — English AI Coach

**Project:** English AI Coach  
**Document:** Interactive Prototype Specification  
**Version:** 1.1  
**Status:** APPROVED BASELINE  
**Date:** 2026-08-29

---

# 1. Prototype Goal

Prototype phải chứng minh được:

```text
Auth
Onboarding
Assessment
Personalized Daily Plan
Learning
Review
SRS feedback
Personalized Exercise
Progress
Error recovery
Offline behavior
```

---

# 2. Prototype Platform

Recommended:

```text
Figma
```

Prototype uses:

```text
mock data
static JSON
prototype variables
```

Không cần:

```text
real backend
real PostgreSQL
real LLM
real FCM
real offline sync
```

---

# 3. Main User Journey

```text
Login
 ↓
Onboarding
 ↓
Goals
 ↓
Daily Time
 ↓
Placement Test
 ↓
Assessment Result
 ↓
Home
 ↓
Continue Learning
 ↓
Learning
 ↓
Result
 ↓
Personalized Practice
 ↓
Progress
 ↓
Weak Words
 ↓
Home
```

---

# 4. Home Prototype Rules

Home primary action:

```text
Continue Learning
```

Tap behavior:

```text
If current Daily Plan item exists:
→ resume it

Otherwise:
→ start first Daily Plan item
```

---

# 5. Home Prototype

Mock:

```text
16 / 20 min
80%

7 new
18 review
5 quiz
```

Interactions:

```text
Continue Learning
→ current Daily Plan item

Review
→ Review Tab

Learn New Words
→ New Word Flashcard

Personalized Practice
→ AI Exercise

Progress
→ Progress
```

---

# 6. Daily Plan Prototype

Mock plan:

```text
New Words: 7
Review: 18
Quiz: 5
Estimated: 20 min
```

The prototype must demonstrate that:

```text
New Words = 7
Review = 18
```

are today's personalized targets.

---

# 7. Review Prototype

Important distinction:

```text
Daily Plan
→ 18 selected reviews

Review Tab
→ 32 currently due reviews
```

User can:

```text
Filter
Open word
Start review
```

---

# 8. Learn Tab Prototype

Learn is not another Home.

Structure:

```text
Continue
New Words
Quiz
Topics
Vocabulary Search
```

Home remains the daily decision center.

---

# 9. Flashcard Prototype

State 1:

```text
Word
IPA
Audio
Show Meaning
```

Tap:

```text
Show Meaning
→ Back State
```

State 2:

```text
Meaning
Translation
Example
Quality selector
```

---

# 10. Answer Quality Prototype

Tap:

```text
Forgot
Hard
Difficult
Okay
Easy
Very Easy
```

Then:

```text
Selected
 ↓
Mock SRS feedback
 ↓
Next card
```

No `isCorrect` input.

---

# 11. Mock SRS Feedback

Example:

```text
Nice work!

Next review
in 17 days
```

Alternative:

```text
Let's review this one soon.

Next review
tomorrow
```

Prototype only simulates feedback.

---

# 12. Personalized Exercise Prototype

Home:

```text
Personalized Practice
→ AI Exercise
```

AI Exercise:

```text
✦ Practice for you
Based on your weak words
```

Questions:

```text
1 / 5
He decided to _____ the project.

A. abandon
B. purchase
C. improve
D. organize
```

---

# 13. Personalized Exercise Success

Correct:

```text
✓ Correct

Nice work!

[Continue]
```

Incorrect:

```text
Not quite.

Let's review this word once more.

[Continue]
```

Optional:

```text
You missed this word twice recently.
```

---

# 14. Personalized Exercise Completion

```text
Personalized Practice Complete

5 questions
4 correct

[Review Weak Words]
[Continue Learning]
```

---

# 15. Quiz Prototype

```text
Quiz
4 / 10

Which word means "từ bỏ"?

[A. abandon]
[B. achieve]
[C. purchase]
[D. negotiate]
```

Flow:

```text
Tap option
 ↓
Selected
 ↓
Next
 ↓
Result
```

---

# 16. Progress Prototype

```text
320 Words Learned
140 Mastered
84.5% Accuracy
21h Learning Time
```

Tap:

```text
View Weak Words
→ Weak Words
```

---

# 17. Weak Words Prototype

```text
negotiate
Accuracy 42%
High weakness
Missed 3 times recently

[Practice]
```

Tap:

```text
Practice
→ Flashcard
```

---

# 18. Placement Test Prototype

Flow:

```text
Intro
 ↓
Question
 ↓
Select answer
 ↓
Next
 ↓
Result
```

Prototype simulates adaptive behavior:

```text
correct → slightly harder
incorrect → slightly easier
```

This is prototype simulation only.

---

# 19. Onboarding Validation

Goals:

```text
0 selected
→ Continue disabled
```

At least 1:

```text
Continue enabled
```

Primary goal:

```text
maximum 1
```

Daily time:

```text
5 / 10 / 20 / 30 / 45 / 60 min
```

---

# 20. Login Validation

```text
Empty email
→ Email is required

Invalid email
→ Enter a valid email

Empty password
→ Password is required
```

---

# 21. Network Error Prototype

When an API-like action fails:

```text
Couldn't load this content.

[Retry]
```

Retry:

```text
Retry
→ Loading
→ Success
```

---

# 22. Offline Prototype — Official V1

Scenario:

```text
User opens Review
 ↓
Network becomes unavailable
 ↓
Tap Start Review
 ↓
Offline State
```

Display:

```text
No internet connection

Please reconnect to continue learning.

[Retry]
```

Do not simulate:

```text
offline attempt
local queue
background sync
```

---

# 23. Offline Read-Only Scenario

Optional prototype:

```text
Open previously cached vocabulary
 ↓
View word
 ↓
Audio if cached
```

But:

```text
Practice
→ Offline State
```

because learning mutation requires network.

---

# 24. Prototype State Variables

```text
isLoggedIn
onboardingCompleted

selectedGoals
primaryGoal
dailyMinutes

assessmentQuestionIndex
assessmentDifficulty

currentPlanItem
currentVocabularyIndex
selectedAnswerQuality

quizQuestionIndex
quizScore

isOnline
```

---

# 25. Mock Data

```text
User
Alex
A2
Travel
20 min/day

Daily Plan
7 new
18 review
5 quiz

Review Queue
32 due
4 high priority

Progress
320 learned
140 mastered
84.5% accuracy
21h
```

---

# 26. Prototype Navigation Map

Phạm vi tạm thời owner duyệt cho `AND-AUTH-001`: Splash restore session bằng
authenticated `GET /users/me`; hợp lệ → existing Main shell, không phiên hoặc
invalid session → Login destination boundary. Chưa triển khai Login/Register
form (`AND-AUTH-002`) hoặc quyết định onboarding (`AND-ONB`/`AND-ASMT`). Sơ đồ
đích bên dưới vẫn là journey đầy đủ; Main shell tạm không khẳng định completion,
không tự tạo completion flag hoặc suy diễn từ profile/goals/CEFR.

```text
Splash
 ↓
Login
 ├── Register
 ├── Google Login
 └── Forgot Password
 ↓
Onboarding
 ├── Welcome
 ├── Goals
 ├── Daily Time
 ├── Placement
 └── Result
       ↓
      Home
       ├── Continue Learning → Current Plan Item
       ├── Review → Review
       ├── New Words → Flashcard
       ├── Personalized Practice → AI Exercise
       ├── Progress → Progress
       └── Profile → Profile
```

---

# 27. Prototype Happy Path

```text
1. Login
2. Select Travel
3. Select 20 min
4. Complete placement
5. Receive A2
6. Open Home
7. Tap Continue Learning
8. Complete review/new-word item
9. Select Hard
10. See SRS feedback
11. Return to Home
12. Tap Personalized Practice
13. Answer 5 questions
14. View result
15. Open Progress
16. Open Weak Words
17. Practice a weak word
18. Return Home
```

---

# 28. Prototype AI Demo Story

```text
User profile
    ↓
Assessment
    ↓
Personalization
    ↓
Today's Plan
    ↓
Learning behavior
    ↓
Weak Word Detection
    ↓
Personalized Exercise
    ↓
Progress
    ↓
Next recommendation
```

This is the primary demo narrative.

---

# 29. Usability Test Scenarios

## Scenario A

> “Bạn mới đăng ký. Hãy chọn mục tiêu Travel, chọn 20 phút/ngày và hoàn thành bài kiểm tra.”

Success:

```text
User can complete without guidance.
```

## Scenario B

> “Bạn đang có 18 lượt ôn trong kế hoạch hôm nay. Hãy bắt đầu học.”

Success:

```text
User understands Continue Learning.
```

## Scenario C

> “Hãy tìm bài luyện tập được cá nhân hóa.”

Success:

```text
User can identify Personalized Practice.
```

## Scenario D

> “Hãy tìm những từ bạn đang yếu.”

Success:

```text
Home → Progress → Weak Words
```

## Scenario E

> “Tắt mạng và thử bắt đầu Review.”

Success:

```text
User understands learning requires reconnecting.
```

---

# 30. Usability Evaluation Questions

```text
[ ] User knows what to do next.
[ ] User understands Daily Plan.
[ ] User understands Review.
[ ] User understands Personalized Practice.
[ ] User understands Flashcard.
[ ] User understands Answer Quality.
[ ] User understands SRS feedback.
[ ] User can find Weak Words.
[ ] User can recover from error.
[ ] User understands offline limitation.
```

---

# 31. Prototype Acceptance Criteria

```text
[ ] Main happy path works.
[ ] Bottom navigation works.
[ ] Home has one primary CTA.
[ ] Daily Plan and Review have distinct flows.
[ ] New Words follows Daily Plan target.
[ ] Flashcard reveal works.
[ ] Answer Quality works.
[ ] SRS feedback is visible.
[ ] Personalized Exercise works.
[ ] Quiz works.
[ ] Progress works.
[ ] Weak Words works.
[ ] Profile works.
[ ] Loading state works.
[ ] Empty state works.
[ ] Error state works.
[ ] Offline state works.
[ ] No offline sync is simulated.
[ ] AI failure has fallback.
```

---

# 32. Final Prototype Freeze Criteria

Prototype is ready for development when:

```text
Happy path reviewed
+
P0 screens reviewed
+
Daily Plan semantics accepted
+
Review semantics accepted
+
Answer Quality tested
+
Offline behavior accepted
+
AI flow understood
```

Then:

```text
Freeze V1 UI
 ↓
Android Java implementation
```

---

# Reconciled V1 Contract Binding

UI behavior must bind to Android Technical Specification v1.1 and API/OpenAPI v1.4. Daily Plan is a stable persisted snapshot (including aggregate QUIZ item); assessment/SRS/personalization/gamification are backend authoritative; V1 is online-first; notification uses FCM/device preferences; all learner-facing copy is Vietnamese.

### Phạm vi Android AND-AUTH-002

Login/Register email/password dùng API thật; Google/Forgot Password chưa nằm
trong task và không dùng production mock. Auth success lưu atomic token pair và
nối existing GET /users/me restore gate, dùng interim Main shell đã duyệt tại
AND-AUTH-001. Flow Welcome/onboarding đầy đủ giữ nguyên làm đích cho AND-ONB/ASMT,
không inference completion. Validation inline, loading, lỗi credentials/lock/
network/rate-limit, explicit resubmit và offline block có Vietnamese resource copy.


## AND-ONB-001 — Thiết lập hồ sơ, mục tiêu và thời gian (owner-approved 2026-10-11)

Splash/auth success giữ Main shell tạm. Main có entry “Thiết lập hồ sơ và mục
tiêu”; luồng Hồ sơ → Mục tiêu → Thời gian học → Xác nhận đã lưu → Main shell.
Không completion flag, suy diễn onboarding từ profile/goals/CEFR hoặc gọi
Placement. Placement/completion thuộc AND-ASMT; Main tạm không biểu thị
personalized Home plan hoặc onboarding hoàn tất.

GET /users/me, GET /users/me/profile, GET /goals và GET /users/me/goals dùng shared
session/API. Profile404 là chưa có profile; không tự ghi/default thời gian/múi
giờ. Prefill server/fullName, người dùng nhập exact IANA timezone ID có xác nhận;
backend TZDB authoritative (400 nếu sai), không trim/normalize hoặc giả định
Android TZDB đồng nhất backend. Profile chưa có thì daily/timezone chưa chọn.
Giữ avatarUrl hiện tại trong PUT; không avatar editor hoặc client CEFR logic.

Chọn ít nhất một active goal, primary optional/null và tối đa một. Goal inactive
đã lưu hiển thị rõ, người dùng phải bỏ/chọn active thay thế trước save, không tự
bỏ lựa chọn trên server. Daily chips5/10/20/30/45/60 theo wireframe, custom integer
5–180 theo API, bảo toàn giá trị server đã lưu.

Save explicit PUT /users/me/profile trước, PUT /users/me/goals sau. Hai operation
không transaction chung: goals lỗi sau profile thành công báo đã lưu một phần,
giữ draft; retry explicit chỉ goals nếu profile draft đã xác nhận lưu không đổi.
Profile đổi thì lưu lại profile trước goals; không rollback/blind transport retry
hoặc thêm eventId. Response sai/timeout không giả định rollback hoặc thành công.
Offline chặn save, loading chặn duplicate. Draft chỉ ViewModel bộ nhớ cho
rotation; không persistence private draft/completion. Session invalidation hủy
stage, xóa draft/back stack; late A không publish/request dưới B. Resource tiếng
Việt, Loading/Empty/Error/Offline/Partial/Success, form scrollable/touch≥48dp.
API/schema/migration và backend CEFR/recommendation/Daily Plan giữ nguyên.

Tên goal canonical có display label Việt từ strings.xml; giữ nguyên name/ID
trong DTO/API. Goal name mới chưa có resource hiển thị nội dung server, không
tự tạo code hoặc đổi lựa chọn.


## AND-LEARN-001 — Client learning boundary (owner duyệt 2026-10-18)

Điểm vào “Học từ này” ở Vocabulary Detail mở một thẻ vocabulary đã chọn và
`sessionType=MIXED`. Không suy diễn từ NEW theo progress hoặc xếp hạng queue.
Start explicit tạo một body `eventId`; timeout/retry cùng logical operation giữ
nguyên eventId và payload. Không blind POST. GET /users/me xác thực USER và
stable auth session identity trước từng start/get; server session có ID được
GET kiểm tra open trước khi tải detail/examples. API và database không đổi.

Task này triển khai Front → Reveal → Back và HTTPS audio URL hiện có; audio
thiếu không chặn reveal và không gọi TTS generation. Quality/submit/feedback/
next thuộc AND-LEARN-002; due review/completion thuộc AND-LEARN-003. Không tạo
attempt, summary giả, XP hoặc SRS trên client.

Timer dùng monotonic clock, bắt đầu khi card đã tải, Fragment RESUMED, online
và session đã xác nhận open; pause background/offline/rotation gap/revalidation.
Reveal hoặc Back về Front không reset; rotation giữ elapsed và face. Sau process
recreation về Front/timer0. Hardware Back và toolbar Back cùng semantics:
BACK→FRONT; FRONT/loading/error/pending hỏi xác nhận; cancel giữ logical state,
exit chỉ clear local/cancel request, không completion hoặc XP.

OS SavedState chỉ giữ eventId/payload (`MIXED`), vocabularyId, stable auth
TokenSession.sessionId và server sessionId nếu có, không token/content/queue.
Refresh giữ stable auth identity; đổi account/logout làm mất hiệu lực state cũ.
Restore phải qua auth gate GET /users/me và khớp owner; chưa có server ID chỉ
explicit retry cùng event/body, không autoPOST/replacementevent. Có server ID
chỉ GET open rồi reload content; closed/missing báo không khả dụng và cho exit.
SavedState chỉ phục hồi cùng Android task khi OS recreation; không cold-start
session restoration, offline mutation queue hoặc automatic sync. Không gửi
private request/state của phiên A dưới phiên B.


## AND-LEARN-002 — Submit flashcard và retry (owner duyệt 2026-10-18)

Mở rộng luồng một từ MIXED của AND-LEARN-001: ở mặt sau có sáu lựa chọn
0 Quên, 1 Rất khó, 2 Khó, 3 Tạm được, 4 Dễ, 5 Rất dễ. Chạm một mức tạo đúng
một logical attempt `FLASHCARD`; request chỉ có sessionId/vocabularyId/attemptType/
responseTimeMs/answerQuality/eventId, không isCorrect/version/header idempotency.
Freeze elapsed monotonic, quality và toàn bộ payload/eventId tại lựa chọn; khóa
chọn khi submitting/pending. Nếu elapsed vượt Integer.MAX_VALUE thì báo lỗi,
không wrap/clamp hoặc gửi. Không tính correctness/SRS/XP trên client.

Success hiển thị isCorrect/quality và interval/nextReviewAt từ response server;
không hứa bảo đảm trí nhớ. Một thẻ đã accepted không tạo attempt khác. Nút
Trở về từ vựng chỉ đóng local; không Next/complete/summary/XP giả. Progress refresh
sau accepted và review/completion vẫn thuộc AND-LEARN-003.

Rotation giữ operation; OS SavedState giữ event/payload số và IDs, stable auth
session UUID, accepted/conflict marker; không token/content hoặc offline queue.
Không tự POST khi resume/online/recreation. Pending chỉ explicit retry exact
payload sau GET /users/me và owner khớp; không GET session-open/progress guard
trước POST replay, kể cả server session đã closed hoặc vocabulary inactive.
Accepted marker phục hồi trạng thái đã ghi nhận và chỉ thoát khi không còn
feedback trong bộ nhớ; không giả feedback hoặc POST để khôi phục. Đổi account/
logout vô hiệu state và callback phiên cũ. Back với pending/submitting/feedback
hỏi xác nhận: cancel giữ state; confirm clear local/cancel, cảnh báo server có
thể đã ghi nhận, không hoàn tất phiên. Khi chưa có attempt giữ BACK→FRONT cũ.

CONCURRENT_UPDATE không tự retry mutation: explicit Tải lại chạy GET progress
và GET session, cả hai PASS và session open mới cho explicit chọn quality như
logical action mới với eventId mới/timer mới. Refresh lỗi giữ khóa; session
closed/missing không tạo action. IDEMPOTENCY_KEY_REUSE refresh dữ liệu và báo
conflict terminal, không retry/change event để bypass. Những read này chỉ phục
vụ conflict recovery, không triển khai accepted-progress-refresh của AND-LEARN-003.
Timeout/network/429/5xx chỉ explicit retry cùng payload/event; lỗi definitive
khác báo controlled error và không tự gửi lại. API/backend/schema không đổi.
