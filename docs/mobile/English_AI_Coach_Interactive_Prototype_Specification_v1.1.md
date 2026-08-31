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
