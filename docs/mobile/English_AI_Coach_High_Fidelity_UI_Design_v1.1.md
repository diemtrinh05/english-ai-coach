# High-Fidelity UI Design Specification v1.1 — English AI Coach

**Project:** English AI Coach  
**Document:** High-Fidelity UI Design Specification  
**Version:** 1.1  
**Status:** APPROVED BASELINE  
**Date:** 2026-08-29

---

# 1. Final Visual Direction

```text
Clean
Friendly
Modern
Calm
Focused
Motivating
Trustworthy
```

Base theme:

```text
Light
Neutral background
White surfaces
Friendly blue/indigo primary
Green success
Amber warning
Red error
Blue info
```

---

# 2. Home Final Hierarchy

Home is a decision center, not a dashboard full of competing actions.

Order:

```text
Greeting
 ↓
Today's Plan
 ↓
Continue Learning  ← PRIMARY
 ↓
Review Summary
 ↓
New Words Summary
 ↓
Personalized Practice
 ↓
Streak
```

---

# 3. Home High-Fidelity Specification

Header:

```text
Good evening, Alex 👋                      Avatar
```

Today's Plan card:

```text
Today's learning

16 / 20 min
████████░░ 80%

[ Continue Learning ]
```

Review card:

```text
⚠ Review

18 planned today
4 high priority

[ Start Review ]
```

New word card:

```text
✦ New words

7 planned today
Recommended for your Travel goal

[ Learn New Words ]
```

Personalized Practice:

```text
✦ Personalized Practice

Based on your weak words

[ Practice Now ]
```

Streak:

```text
🔥 12 day streak
```

---

# 4. Home CTA Rules

Only:

```text
Continue Learning
```

is visually primary.

Secondary actions:

```text
Start Review
Learn New Words
Practice Now
```

They use secondary/card actions.

---

# 5. Daily Plan Visual Semantics

The Daily Plan card represents the current personalized workload.

Example:

```text
New     7
Review 18
Quiz    5
```

Optional:

```text
Estimated 20 min
```

Do not display raw algorithm data.

---

# 6. Review Visual Semantics

Review tab displays due workload.

Example:

```text
32 words currently due
```

Daily Plan may select only:

```text
18 for today's workload
```

Use different copy to prevent confusion.

---

# 7. Flashcard Final Design

Front:

```text
┌─────────────────────────────────┐
│ <                       4 / 18  │
│                                 │
│                                 │
│             abandon             │
│         /əˈbændən/              │
│                                 │
│              🔊                 │
│                                 │
│       [ Show meaning ]          │
│                                 │
└─────────────────────────────────┘
```

Back:

```text
┌─────────────────────────────────┐
│ abandon                         │
│ /əˈbændən/                  🔊   │
│                                 │
│ to leave completely             │
│ từ bỏ                           │
│                                 │
│ He abandoned the project.       │
│ Anh ấy đã từ bỏ dự án.          │
│                                 │
│ How difficult was this?         │
│                                 │
│ [Forgot] [Hard] [Difficult]     │
│ [Okay]  [Easy] [Very Easy]      │
└─────────────────────────────────┘
```

---

# 8. Answer Quality Visual Design

Six equal-weight choices are acceptable initially:

```text
Forgot
Hard
Difficult
Okay
Easy
Very Easy
```

Use:

```text
2-row layout
```

rather than tiny horizontal controls.

If usability test shows confusion, simplify copy while preserving the internal 0–5 mapping.

---

# 9. SRS Feedback UI

Success:

```text
Nice work!

Next review
in 17 days
```

Difficult:

```text
Let's review this one soon.

Next review
tomorrow
```

Never phrase scheduling as a memory guarantee.

---

# 10. Review High-Fidelity

```text
Review

32 words currently due

[All] [High Risk] [Weak]

┌───────────────────────────────┐
│ ⚠ High priority               │
│ negotiate                     │
│ Accuracy 42%                  │
│ Missed 3 times recently       │
│ [Practice]                    │
└───────────────────────────────┘
```

---

# 11. Personalized Exercise High-Fidelity

```text
✦ Practice for you

Based on your weak words

He decided to _____ the project.

[A. abandon]
[B. purchase]
[C. improve]
[D. organize]
```

Feedback:

Correct:

```text
✓ Correct
Nice work!
[Continue]
```

Incorrect:

```text
Not quite
Let's review this word once more.
[Continue]
```

---

# 12. Progress High-Fidelity

First viewport:

```text
320 Words Learned
140 Mastered

Accuracy
84.5%
█████████░

Learning Time
21h
```

Secondary:

```text
Accuracy trend
Weak Words
Learning History
```

---

# 13. Weak Words High-Fidelity

```text
negotiate
Accuracy 42%

High weakness

You've missed it 3 times recently.

[Practice]
```

Reason is more important than raw score alone.

---

# 14. Navigation High-Fidelity

Bottom navigation:

```text
Home
Learn
Review
Progress
Profile
```

Selected:

```text
Icon
Label
Soft primary container
```

Unselected:

```text
Muted icon
Muted label
```

---

# 15. Loading

Home:

```text
Header skeleton
Progress skeleton
Card skeleton
Card skeleton
```

Avoid blank white screens.

---

# 16. Empty

Review:

```text
🎉

You're all caught up!

No words need review right now.
```

---

# 17. Error

```text
Couldn't load today's learning plan.

[Retry]
```

---

# 18. Offline — Official V1

```text
No internet connection

Please reconnect to continue learning.

[Retry]
```

Do not show:

```text
Syncing...
Waiting to sync...
Your progress will sync later...
```

because V1 does not implement offline learning synchronization.

---

# 19. Connectivity Visual Rules

Online:

```text
Normal learning UI
```

Offline while attempting learning:

```text
Block mutation
Show Offline State
```

Read-only cached vocabulary may remain accessible.

---

# 20. AI Visual Rules

Use:

```text
✦
Recommended for you
Personalized
Based on your practice
High priority
```

Do not use:

```text
LLM
ML confidence
Model score
Inference
Prompt
```

on learner screens.

---

# 21. Motion

Button press:

```text
100–150ms
```

Flashcard flip:

```text
250–350ms
```

Progress:

```text
300–500ms
```

Navigation:

```text
150–250ms
```

Motion remains subtle.

---

# 22. Accessibility

```text
Touch target ≥ 44–48dp
Dynamic text where possible
High contrast
Screen-reader labels
Content descriptions
Focus order
Correctness not represented only by color
IPA supported
```

---

# 23. Responsive

```text
<360dp
→ compact spacing

360–480dp
→ standard

>480dp
→ centered content
```

Max content width on larger devices:

```text
560–640dp
```

---

# 24. Final High-Fidelity P0

```text
Home
Review
Flashcard
Personalized Exercise
Progress
Onboarding
Placement Test
Login
Profile
```

---

# 25. High-Fidelity Acceptance Criteria

```text
[ ] Home has one visually dominant CTA.
[ ] Home communicates today's workload.
[ ] Review count clearly differs from Daily Plan count.
[ ] New Words quantity comes from Daily Plan.
[ ] Flashcard is uncluttered.
[ ] Answer Quality can be selected without ambiguity.
[ ] SRS feedback is understandable.
[ ] Personalized Exercise visibly communicates why it exists.
[ ] Progress is scannable.
[ ] Offline UI does not promise sync.
[ ] Error states provide recovery.
[ ] Accessibility requirements are reflected.
```

---

# 26. Final Handoff

```text
High-Fidelity UI
 ↓
Interactive Prototype
 ↓
Usability Test
 ↓
Freeze V1
 ↓
Android Java implementation
```

---

# Reconciled V1 Contract Binding

UI behavior must bind to Android Technical Specification v1.1 and API/OpenAPI v1.4. Daily Plan is a stable persisted snapshot (including aggregate QUIZ item); assessment/SRS/personalization/gamification are backend authoritative; V1 is online-first; notification uses FCM/device preferences; all learner-facing copy is Vietnamese.


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
