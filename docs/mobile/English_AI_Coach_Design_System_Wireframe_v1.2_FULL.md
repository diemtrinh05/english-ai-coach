# Design System + Wireframe v1.2 — English AI Coach

**Project:** English AI Coach  
**Document:** Design System + Wireframe  
**Version:** 1.2  
**Status:** APPROVED BASELINE  
**Date:** 2026-08-29

**Revision policy:** This version is a **full revision** of v1.1. It preserves the complete design-system, wireframe, implementation, mapping, accessibility, state, handoff, and acceptance content from v1.1 while incorporating the UX review decisions made after the initial interactive prototype review.

**Based on:**
- `English_AI_Coach_SRS_v1.2.md`
- `English_AI_Coach_Database_Schema_v1.6.md`
- `English_AI_Coach_System_Architecture_v1.3.md`
- `English_AI_Coach_AI_Personalization_Specification_v1.3.md`
- `English_AI_Coach_API_Specification_v1.4.md`
- `English_AI_Coach_OpenAPI_Swagger_v1_4.md`
- `English_AI_Coach_App_UI_UX_Specification_v1.1.md`
- `English_AI_Coach_High_Fidelity_UI_Design_v1.1.md`
- `English_AI_Coach_Interactive_Prototype_Specification_v1.1.md`

---

# 1. Purpose

Tài liệu này chốt toàn bộ:

```text
Design Tokens
+
Visual Language
+
Components
+
Component States
+
Layout Rules
+
Navigation
+
Screen Inventory
+
Full Screen Wireframes
+
Interaction Rules
+
UI States
+
Accessibility
+
API Mapping
+
User Story Mapping
+
Android Java Guidance
+
Flutter Compatibility
+
Design Handoff
+
UX Acceptance Criteria
```

Mục tiêu:

```text
Design System
      ↓
Wireframe
      ↓
High-Fidelity UI
      ↓
Interactive Prototype
      ↓
Usability Test
      ↓
Android Java V1
      ↓
Flutter V2
```

Design System phải độc lập framework để có thể tái sử dụng giữa Android Java V1 và Flutter V2.

---

# 2. Product Design Direction

## 2.1. Product personality

```text
Friendly
Modern
Calm
Focused
Motivating
Trustworthy
AI-assisted
```

## 2.2. Product positioning

English AI Coach là:

> Một personal vocabulary coach giúp người học biết hôm nay nên học gì, ôn gì và luyện gì dựa trên trình độ, mục tiêu và lịch sử học tập.

Không phải:

```text
Dictionary
Generic quiz app
Enterprise dashboard
AI chatbot
Game-heavy app
```

---

# 3. V1 Scope

## 3.1. Core experience

```text
Authentication
Onboarding
Placement Test
Today's Personalized Plan
New Words
Review
Flashcard
Quiz
Progress
Weak Words
Gamification
Profile
Notifications
```

## 3.2. AI-enhanced V1

```text
Personalized Exercise
AI-generated reusable learning content
AI-assisted reusable examples/explanations
```

## 3.3. Future

```text
Speaking
Writing
Listening
Reading
AI Tutor
Advanced ML
Offline-first synchronization
```

---

# 4. Critical UX Decisions

## 4.1. Home has one Primary CTA

Home primary CTA:

```text
Continue Learning
```

Secondary shortcuts:

```text
Start Review
Learn New Words
Practice Now
```

Không để các shortcut cạnh tranh trực quan với Primary CTA.

---

## 4.2. Daily Plan vs Review

### Today's Plan

Trả lời:

> “Hôm nay tôi nên làm gì?”

Ví dụ:

```text
7 new words
18 reviews
5 quiz questions
20 minutes
```

### Review

Trả lời:

> “Hiện tại tôi có những từ nào đang đến hạn ôn?”

Ví dụ:

```text
32 words currently due
```

Do đó:

```text
Daily Plan
= Personalized daily recommendation

Review
= Full currently-due review queue
```

---

## 4.3. New Words follows Daily Plan

Khi user bấm:

```text
Learn New Words
```

app phải lấy số lượng từ theo target của Daily Plan.

Ví dụ:

```text
Daily Plan
New Words = 7

Learn New Words
→ 7 selected words
```

Không tự ý biến shortcut thành một lesson không giới hạn.

---

## 4.4. Personalized Exercise

`Personalized Exercise` là:

```text
AI-enhanced V1
```

Core scheduling engine vẫn là:

```text
Daily Plan
SRS
Review
Weak Word Detection
Progress
```

Personalized Exercise được đưa lên Home vì đây là cách quan trọng để người dùng nhìn thấy giá trị AI.

---

## 4.5. Placement Test

V1 decision:

```text
Placement Test required
before first personalized Home plan
```

Flow:

```text
Register
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
```

---

## 4.6. Answer Quality

UI hiện dùng:

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
0
1
2
3
4
5
```

Backend rule:

```text
answerQuality >= 3
→ isCorrect = true
```

Client không submit `isCorrect`.

Usability test phải đặc biệt kiểm tra việc phân biệt:

```text
Hard
Difficult
```

Nếu người dùng nhầm, có thể đổi wording ở bản visual cuối nhưng không đổi semantic 0–5 của backend.

---

# 5. Online-First MVP

## 5.1. Official connectivity model

```text
V1 = Online-first
```

Full offline-first không thuộc MVP.

Không triển khai:

```text
Offline learning
Offline review submission
Offline quiz submission
Offline attempt queue
Automatic learning-data synchronization
Background sync
Conflict merge
```

---

## 5.2. Read-only cache

Có thể cache:

```text
Previously loaded vocabulary
Topic metadata
Previously loaded approved content
```

Nhưng:

```text
No learning state mutation
```

---

## 5.3. Official Offline State

```text
┌─────────────────────────────────┐
│                                 │
│         No internet             │
│                                 │
│ Please reconnect to continue    │
│ learning.                       │
│                                 │
│            [Retry]              │
│                                 │
└─────────────────────────────────┘
```

Không được hiển thị:

```text
Syncing...
Waiting to sync...
Your progress will sync later...
```

---

# 6. Design Principles

## DS-01 — Learning First

Ưu tiên:

```text
What should I do?
How much?
Start
```

## DS-02 — One Primary CTA

Một màn hình chỉ có một hành động visual-primary.

## DS-03 — AI Should Feel Native

Dùng:

```text
Recommended for you
Based on your recent practice
High priority
Personalized practice
```

Không biến mọi component thành “AI”.

## DS-04 — Progressive Disclosure

Ví dụ Flashcard:

```text
Word
 ↓
Reveal
 ↓
Meaning
 ↓
Example
 ↓
Quality
```

## DS-05 — Consistency

Cùng action phải có cùng visual language và behavior.

## DS-06 — No Offline Promise

UI phải phản ánh capability thật.

---

# 7. Design Tokens

## 7.1. Color Roles

```text
color.primary
color.onPrimary

color.primaryContainer
color.onPrimaryContainer

color.background
color.onBackground

color.surface
color.onSurface

color.surfaceVariant
color.onSurfaceVariant

color.success
color.onSuccess

color.warning
color.onWarning

color.error
color.onError

color.info
color.onInfo

color.divider
color.disabled
color.overlay
```

HEX cuối cùng được khóa trong High-Fidelity design source.

---

## 7.2. Semantic Usage

| Role | Usage |
|---|---|
| Primary | Main CTA, selected navigation |
| Primary Container | Selected/recommended surfaces |
| Surface | Cards, inputs |
| Surface Variant | Secondary sections |
| Success | Correct/completed |
| Warning | High priority / attention |
| Error | Validation/service error |
| Info | Explanation/hint |
| Disabled | Disabled interaction |

Không dùng color là signal duy nhất.

---

# 8. Typography

Font phải hỗ trợ:

```text
English
Vietnamese
IPA
Numbers
```

Hierarchy:

```text
Display
Headline
Title
Body
Label
Caption
```

Baseline:

```text
Display → 32–36sp
Headline → 24–28sp
Title → 18–22sp
Body → 14–16sp
Label → 12–14sp
Caption → 11–12sp
```

Font weights:

```text
Regular = 400
Medium = 500
Semibold = 600
Bold = 700
```

Không sử dụng quá nhiều weights trong cùng screen.

---

# 9. Spacing

8pt grid:

```text
4
8
12
16
20
24
32
40
48
64
```

Screen horizontal padding:

```text
16–20dp
```

---

# 10. Radius

```text
8dp
12dp
16dp
20dp
24dp
pill
```

---

# 11. Elevation

```text
0 → flat
1–2 → standard cards
4 → floating
8 → dialog/modal
```

Shadow phải nhẹ.

---

# 12. Iconography

Preferred size:

```text
20–24dp
```

Primary action icon:

```text
24dp
```

Decorative icon:

```text
32–48dp
```

Common icons:

```text
Home
Book
Review
Progress
Profile
Search
Filter
Volume
Arrow
Check
Close
Fire
Star
Clock
Warning
Sparkle
Wifi-off
```

Offline icon chỉ biểu thị connectivity state.

---

# 13. Layout Grid

```text
|<--------------- Screen ---------------->|
     16–20dp    Content    16–20dp
```

Không đặt primary CTA sát edge.

---

# 14. Safe Area

Respect:

```text
Status bar
Notch
Gesture area
Navigation bar
```

---

# 15. Core Components

## 15.1. Primary Button

```text
┌─────────────────────────────┐
│       Start Review          │
└─────────────────────────────┘
```

Height:

```text
48–52dp
```

Radius:

```text
12–16dp
```

States:

```text
Default
Pressed
Loading
Disabled
```

---

## 15.2. Secondary Button

```text
┌─────────────────────────────┐
│       View Progress         │
└─────────────────────────────┘
```

---

## 15.3. Text Button

```text
See all
Skip
View details
```

---

## 15.4. Icon Button

```text
🔊
🔍
←
⋮
```

Must have accessibility label.

---

## 15.5. Card

```text
┌─────────────────────────────────┐
│ Title                           │
│ Supporting text                 │
│                                 │
│ Metadata                 →      │
└─────────────────────────────────┘
```

---

## 15.6. Recommendation Card

```text
┌─────────────────────────────────┐
│ ✦ Recommended for you           │
│                                 │
│ 7 new words                     │
│ Based on your Travel goal       │
│                                 │
│ [ Start ]                       │
└─────────────────────────────────┘
```

---

## 15.7. Priority Review Card

```text
┌─────────────────────────────────┐
│ ⚠ High priority review          │
│                                 │
│ 18 words planned today          │
│ 4 high priority                 │
│                                 │
│ [ Start Review ]                │
└─────────────────────────────────┘
```

---

## 15.8. Vocabulary Card

```text
┌─────────────────────────────────┐
│ abandon                     🔊   │
│ /əˈbændən/                     │
│ to leave completely             │
│ B1      Travel                  │
└─────────────────────────────────┘
```

---

## 15.9. Stat Card

```text
┌────────────────┐
│ 320            │
│ Words learned  │
└────────────────┘
```

---

## 15.10. Progress Bar

```text
████████░░ 80%
```

---

## 15.11. Chips

```text
[A2] [Travel] [Review]
```

Types:

```text
TopicChip
CefrChip
FilterChip
StatusChip
```

---

## 15.12. Answer Button

```text
┌─────────────────────────────┐
│ A. To leave                 │
└─────────────────────────────┘
```

States:

```text
Default
Selected
Correct
Incorrect
Disabled
```

---

## 15.13. Empty State

```text
       [Icon]

Nothing here yet

Supporting explanation

      [Primary CTA]
```

---

## 15.14. Error State

```text
       ⚠

Something went wrong.

Please try again.

       [Retry]
```

---

## 15.15. Offline State

```text
       Wifi-off

No internet connection

Please reconnect to
continue learning.

[Retry]
```

---

## 15.16. Skeleton

Use for:

```text
Home
Progress
Vocabulary
Quiz
```

---

# 16. Component States

Reusable components must define:

```text
Default
Pressed
Selected
Disabled
Loading
Success
Error
Empty
```

Network-driven screens additionally:

```text
Offline
```

---

# 17. Accessibility

```text
Touch target ≥ 44–48dp
Dynamic text where possible
Screen-reader labels
Content descriptions
High contrast
Logical focus order
Color not sole indicator
Readable labels
Correct IPA rendering
```

---

# 18. Main Navigation

Bottom navigation:

```text
┌────────┬────────┬────────┬──────────┬─────────┐
│ Home   │ Learn  │ Review │ Progress │ Profile │
└────────┴────────┴────────┴──────────┴─────────┘
```

Roles:

```text
Home
= today's decision center

Learn
= learning entry/library

Review
= due queue

Progress
= analytics

Profile
= account/settings
```

Vocabulary Search remains secondary.

---

# 19. Full Screen Inventory

```text
AUTH
01 Splash
02 Login
03 Register
04 Forgot Password

ONBOARDING
05 Welcome
06 Goal Selection
07 Daily Time
08 Placement Intro
09 Placement Question
10 Assessment Result

MAIN
11 Home
12 Learn
13 Review
14 Vocabulary Search
15 Progress
16 Profile

LEARNING
17 Flashcard Front
18 Flashcard Back
19 Word Detail
20 Personalized Exercise
21 Quiz
22 Learning Result

PROGRESS
23 Weak Words
24 Learning History
25 Gamification

PROFILE
26 Goal Settings
27 Learning Settings
```

---

# 20. Splash Wireframe

```text
┌─────────────────────────────────┐
│                                 │
│            [LOGO]               │
│                                 │
│       English AI Coach          │
│                                 │
│          ● ● ●                  │
│                                 │
└─────────────────────────────────┘
```

Flow:

```text
Splash
 ↓
Auth Check
 ↓
Onboarding Check
 ↓
Home / Login / Onboarding
```

---

# 21. Login Wireframe

```text
┌─────────────────────────────────┐
│ <                               │
│                                 │
│ Welcome back                    │
│ Continue learning               │
│                                 │
│ { Email                         │
│                                 │
│ { Password                👁    │
│                                 │
│ [        Log In             ]   │
│                                 │
│        or                       │
│                                 │
│ [    Continue with Google   ]   │
│                                 │
│ Forgot password?                │
│                                 │
│ Don't have an account? Sign up  │
└─────────────────────────────────┘
```

Interactions:

```text
Log In → Home
Google → mock Google flow
Forgot → Forgot Password
Sign Up → Register
```

Validation:

```text
empty email → error
invalid email → error
empty password → error
```

---

# 22. Register Wireframe

```text
┌─────────────────────────────────┐
│ <                               │
│                                 │
│ Create account                  │
│                                 │
│ { Full name                    │
│ { Email                        │
│ { Password               👁    │
│ { Confirm password       👁    │
│                                 │
│ [      Create Account       ]   │
│                                 │
│       or                        │
│                                 │
│ [   Continue with Google    ]   │
│                                 │
│ Already have an account? Login  │
└─────────────────────────────────┘
```

Flow:

```text
Create Account
→ Welcome
```

---

# 23. Forgot Password Wireframe

```text
┌─────────────────────────────────┐
│ <                               │
│                                 │
│ Reset password                  │
│                                 │
│ Enter your email                │
│                                 │
│ { Email                         │
│                                 │
│ [ Send Reset Link ]             │
│                                 │
│ Back to Login                   │
└─────────────────────────────────┘
```

Success:

```text
Reset link sent.
```

---

# 24. Onboarding Welcome

```text
┌─────────────────────────────────┐
│                                 │
│          [ILLUSTRATION]         │
│                                 │
│     Learn English your way      │
│                                 │
│ We'll build a learning plan     │
│ based on your level, goals,     │
│ and progress.                   │
│                                 │
│ [        Get Started        ]   │
└─────────────────────────────────┘
```

---

# 25. Goal Selection

```text
┌─────────────────────────────────┐
│ <                         1 / 3 │
│                                 │
│ What do you want to improve?    │
│                                 │
│ ┌────────────┐ ┌────────────┐   │
│ │ ✈ Travel   │ │ 💼 Business│   │
│ └────────────┘ └────────────┘   │
│                                 │
│ ┌────────────┐ ┌────────────┐   │
│ │ IELTS      │ │ TOEIC      │   │
│ └────────────┘ └────────────┘   │
│                                 │
│ ┌────────────┐ ┌────────────┐   │
│ │ General    │ │ Academic   │   │
│ └────────────┘ └────────────┘   │
│                                 │
│ Choose a primary goal           │
│                                 │
│ [        Continue           ]   │
└─────────────────────────────────┘
```

Rules:

```text
At least 1 goal
At most 1 primary goal
```

---

# 26. Daily Time

```text
┌─────────────────────────────────┐
│ <                         2 / 3 │
│                                 │
│ How much time can you study?    │
│                                 │
│ ┌──────┐ ┌──────┐ ┌──────┐      │
│ │ 5min │ │10min │ │20min │      │
│ └──────┘ └──────┘ └──────┘      │
│                                 │
│ ┌──────┐ ┌──────┐ ┌──────┐      │
│ │30min │ │45min │ │60min │      │
│ └──────┘ └──────┘ └──────┘      │
│                                 │
│ Suggested: 20 min/day           │
│                                 │
│ [        Continue           ]   │
└─────────────────────────────────┘
```

Mock default:

```text
20 min
```

---

# 27. Placement Intro

```text
┌─────────────────────────────────┐
│ <                               │
│                                 │
│ Find your vocabulary level      │
│                                 │
│ [ILLUSTRATION]                  │
│                                 │
│ This adaptive test adjusts      │
│ difficulty as you answer.       │
│                                 │
│ About 5–10 minutes              │
│ No penalty                      │
│                                 │
│ [        Start Test         ]   │
└─────────────────────────────────┘
```

---

# 28. Placement Question

```text
┌─────────────────────────────────┐
│ <                    12 / ~25   │
│                                 │
│ What does "abandon" mean?       │
│                                 │
│ ┌─────────────────────────────┐ │
│ │ A. To leave                 │ │
│ └─────────────────────────────┘ │
│                                 │
│ ┌─────────────────────────────┐ │
│ │ B. To improve               │ │
│ └─────────────────────────────┘ │
│                                 │
│ ┌─────────────────────────────┐ │
│ │ C. To purchase              │ │
│ └─────────────────────────────┘ │
│                                 │
│ ┌─────────────────────────────┐ │
│ │ D. To organize              │ │
│ └─────────────────────────────┘ │
│                                 │
│ ████████████░░░░░ 48%           │
└─────────────────────────────────┘
```

Behavior:

```text
Correct
→ slightly harder next question

Incorrect
→ slightly easier next question
```

Prototype simulation only.

---

# 29. Assessment Result

```text
┌─────────────────────────────────┐
│                                 │
│ Your vocabulary level           │
│                                 │
│              A2                 │
│          Elementary             │
│                                 │
│ ┌────────┐ ┌────────┐           │
│ │ 72.5%  │ │ 18/25  │           │
│ │ Score  │ │ Correct │           │
│ └────────┘ └────────┘           │
│                                 │
│ We'll start with personalized   │
│ A2 vocabulary.                  │
│                                 │
│ [ Start My Learning Plan ]      │
└─────────────────────────────────┘
```

---

# 30. Home Wireframe — Final

```text
┌─────────────────────────────────┐
│ Good evening, Alex 👋      ◉    │
│                                 │
│ Today's learning                │
│                                 │
│ 16 / 20 min                     │
│ ████████░░ 80%                  │
│                                 │
│ [     Continue Learning      ]  │
│                                 │
│ ┌─────────────────────────────┐ │
│ │ Review                      │ │
│ │ 18 planned today            │ │
│ │ 4 high priority             │ │
│ │ [Start Review]              │ │
│ └─────────────────────────────┘ │
│                                 │
│ ┌─────────────────────────────┐ │
│ │ ✦ New words                 │ │
│ │ 7 planned today             │ │
│ │ Recommended for your goal   │ │
│ │ [Learn New Words]           │ │
│ └─────────────────────────────┘ │
│                                 │
│ ┌─────────────────────────────┐ │
│ │ ✦ Personalized Practice     │ │
│ │ Based on your weak words    │ │
│ │ [Practice Now]              │ │
│ └─────────────────────────────┘ │
│                                 │
│ 🔥 12 day streak               │
│                                 │
├────────┬────────┬────────┬──────┤
│ Home   │ Learn  │ Review │ ...  │
└────────┴────────┴────────┴──────┘
```

Final hierarchy:

```text
1. Today's Plan
2. Continue Learning  ← Primary
3. Review
4. New Words
5. Personalized Practice
6. Streak
```

---

# 31. Learn Wireframe

```text
┌─────────────────────────────────┐
│ Learn                           │
│                                 │
│ Continue                        │
│                                 │
│ ┌─────────────────────────────┐ │
│ │ Today's plan                │ │
│ │ 7 new • 18 review • 5 quiz │ │
│ │ [Continue]                  │ │
│ └─────────────────────────────┘ │
│                                 │
│ New Words                       │
│ Quiz                            │
│                                 │
│ Recommended Topics              │
│ [Travel] [Business] [Daily]     │
│                                 │
│ Vocabulary Search               │
│                                 │
├────────┬────────┬────────┬──────┤
│ Home   │ Learn  │ Review │ ...  │
└────────┴────────┴────────┴──────┘
```

Learn is not another Home.

---

# 32. Review Wireframe — Final

```text
┌─────────────────────────────────┐
│ Review                          │
│                                 │
│ 32 words currently due          │
│                                 │
│ [All] [High Risk] [Weak]        │
│                                 │
│ ┌─────────────────────────────┐ │
│ │ ⚠ High priority             │ │
│ │ abandon                     │ │
│ │ Accuracy: 42%               │ │
│ │ Missed 3 times recently     │ │
│ │ [Practice]                  │ │
│ └─────────────────────────────┘ │
│                                 │
│ ┌─────────────────────────────┐ │
│ │ purchase                    │ │
│ │ Accuracy: 55%               │ │
│ │ [Practice]                  │ │
│ └─────────────────────────────┘ │
│                                 │
│ [        Start Review       ]   │
└─────────────────────────────────┘
```

Daily Plan may show:

```text
18 selected for today
```

while Review shows:

```text
32 currently due
```

---

# 33. Review Empty State

```text
┌─────────────────────────────────┐
│                                 │
│              🎉                 │
│                                 │
│      You're all caught up!      │
│                                 │
│ No words need review right now. │
│                                 │
│ [ Explore Vocabulary ]          │
└─────────────────────────────────┘
```

---

# 34. Vocabulary Search Wireframe

```text
┌─────────────────────────────────┐
│ < Vocabulary                    │
│                                 │
│ { Search vocabulary... } 🔍     │
│                                 │
│ [All CEFR] [Topic] [POS]       │
│                                 │
│ ┌─────────────────────────────┐ │
│ │ abandon                 🔊   │ │
│ │ /əˈbændən/                 │ │
│ │ to leave completely         │ │
│ │ B1 • Daily Life             │ │
│ └─────────────────────────────┘ │
│                                 │
│ ┌─────────────────────────────┐ │
│ │ negotiate               🔊   │ │
│ │ /nɪˈɡəʊʃieɪt/              │ │
│ │ discuss to reach agreement  │ │
│ │ B2 • Business               │ │
│ └─────────────────────────────┘ │
└─────────────────────────────────┘
```

---

# 35. Flashcard Front Wireframe

```text
┌─────────────────────────────────┐
│ <                       4 / 18  │
│                                 │
│                                 │
│             abandon             │
│         /əˈbændən/              │
│                                 │
│               🔊                │
│                                 │
│       [     Show meaning    ]   │
│                                 │
└─────────────────────────────────┘
```

---

# 36. Flashcard Back Wireframe

```text
┌─────────────────────────────────┐
│ <                       4 / 18  │
│                                 │
│             abandon             │
│         /əˈbændən/              │
│                                 │
│         to leave completely     │
│              từ bỏ              │
│                                 │
│ He abandoned the project.       │
│ Anh ấy đã từ bỏ dự án.          │
│                                 │
│               🔊                │
│                                 │
│ How difficult was this?         │
│                                 │
│ [Forgot] [Hard] [Difficult]     │
│ [Okay]  [Easy] [Very Easy]      │
└─────────────────────────────────┘
```

---

# 37. Flashcard Interaction

```text
Front
 ↓
Show Meaning
 ↓
Back
 ↓
Select Quality
 ↓
Submit
 ↓
SRS Feedback
 ↓
Next Card
```

Không yêu cầu:

```text
Correct / Incorrect
```

---

# 38. Word Detail Wireframe

```text
┌─────────────────────────────────┐
│ <                        ⋮      │
│                                 │
│ abandon                         │
│ /əˈbændən/                  🔊  │
│                                 │
│ to leave completely             │
│ từ bỏ                           │
│                                 │
│ [B1] [Daily Life]               │
│                                 │
│ Example                         │
│ He abandoned the project.       │
│ Anh ấy đã từ bỏ dự án.          │
│                                 │
│ My progress                     │
│ Accuracy       67%              │
│ Repetitions    3                │
│ Next review    Sep 14           │
│                                 │
│ [       Practice       ]        │
└─────────────────────────────────┘
```

---

# 39. Personalized Exercise Wireframe

```text
┌─────────────────────────────────┐
│ <                    1 / 5      │
│                                 │
│ ✦ Practice for you              │
│ Based on your weak words        │
│                                 │
│ He decided to _____ the project.│
│                                 │
│ ┌─────────────────────────────┐ │
│ │ A. abandon                  │ │
│ └─────────────────────────────┘ │
│                                 │
│ ┌─────────────────────────────┐ │
│ │ B. purchase                 │ │
│ └─────────────────────────────┘ │
│                                 │
│ ┌─────────────────────────────┐ │
│ │ C. improve                  │ │
│ └─────────────────────────────┘ │
│                                 │
│ ┌─────────────────────────────┐ │
│ │ D. organize                 │ │
│ └─────────────────────────────┘ │
└─────────────────────────────────┘
```

---

# 40. Personalized Exercise Feedback

Correct:

```text
┌─────────────────────────────────┐
│ ✓ Correct                       │
│                                 │
│ Nice work!                      │
│                                 │
│ [Continue]                      │
└─────────────────────────────────┘
```

Incorrect:

```text
┌─────────────────────────────────┐
│ Not quite                      │
│                                 │
│ Let's review this word once     │
│ more.                           │
│                                 │
│ [Continue]                      │
└─────────────────────────────────┘
```

Optional reason:

```text
You missed this word twice recently.
```

---

# 41. Personalized Exercise Completion

```text
┌─────────────────────────────────┐
│                                 │
│ Personalized Practice Complete  │
│                                 │
│ 5 questions                     │
│ 4 correct                       │
│                                 │
│ [ Review Weak Words ]           │
│ [ Continue Learning ]           │
└─────────────────────────────────┘
```

---

# 42. Quiz Wireframe

```text
┌─────────────────────────────────┐
│ Quiz                    4 / 10  │
│                                 │
│ Which word means "từ bỏ"?      │
│                                 │
│ [ A. abandon ]                  │
│                                 │
│ [ B. achieve ]                  │
│                                 │
│ [ C. purchase ]                 │
│                                 │
│ [ D. negotiate ]                │
│                                 │
│ ████████░░░░ 40%                │
└─────────────────────────────────┘
```

Correct answer not exposed before submission.

---

# 43. Learning Result Wireframe

```text
┌─────────────────────────────────┐
│                                 │
│          Great job! 🎉          │
│                                 │
│ Accuracy                        │
│           86%                   │
│                                 │
│ ┌──────────┐ ┌──────────┐       │
│ │ 15       │ │ 18 min   │       │
│ │ practiced│ │ time     │       │
│ └──────────┘ └──────────┘       │
│                                 │
│ Improved        7               │
│ Needs practice  3               │
│                                 │
│ [ Review Weak Words ]           │
│ [ Continue Learning ]            │
└─────────────────────────────────┘
```

---

# 44. Progress Wireframe

```text
┌─────────────────────────────────┐
│ Progress                        │
│                                 │
│ ┌────────┐ ┌────────┐           │
│ │ 320    │ │ 140    │           │
│ │ Learned│ │Mastered│           │
│ └────────┘ └────────┘           │
│                                 │
│ Accuracy                        │
│ 84.5%                           │
│ █████████░ 84.5%               │
│                                 │
│ Learning time                   │
│ 21h                             │
│                                 │
│ Accuracy trend                  │
│        ╱╲                       │
│   ╱───╯  ╲──╮                   │
│ ───────────────                 │
│                                 │
│ [ View Weak Words ]             │
└─────────────────────────────────┘
```

---

# 45. Weak Words Wireframe

```text
┌─────────────────────────────────┐
│ < Weak Words                    │
│                                 │
│ 8 words need attention          │
│                                 │
│ ┌─────────────────────────────┐ │
│ │ negotiate                   │ │
│ │ Accuracy: 42%               │ │
│ │ ● High weakness             │ │
│ │                             │ │
│ │ You've missed it 3 times.   │ │
│ │ [       Practice        ]   │ │
│ └─────────────────────────────┘ │
│                                 │
│ ┌─────────────────────────────┐ │
│ │ purchase                    │ │
│ │ Accuracy: 55%               │ │
│ │ ● Medium weakness           │ │
│ │ [       Practice        ]   │ │
│ └─────────────────────────────┘ │
└─────────────────────────────────┘
```

---

# 46. Learning History Wireframe

```text
┌─────────────────────────────────┐
│ < Learning History              │
│                                 │
│ [Day] [Week] [Month]            │
│                                 │
│ Today                           │
│ ─────────────────────────────   │
│ 20 min                          │
│ 25 words                        │
│ 86% accuracy                    │
│                                 │
│ Yesterday                       │
│ ─────────────────────────────   │
│ 18 min                          │
│ 20 words                        │
│ 81% accuracy                    │
└─────────────────────────────────┘
```

---

# 47. Gamification Wireframe

```text
┌─────────────────────────────────┐
│ Achievements                    │
│                                 │
│ 🔥 12 day streak                │
│                                 │
│ 1,250 XP                        │
│ Level 5                         │
│ ███████░░░ 83%                 │
│                                 │
│ Badges                          │
│                                 │
│ ┌───────┐ ┌───────┐ ┌───────┐   │
│ │ 7 Day │ │ 100   │ │Perfect│   │
│ │  🔥   │ │ Words │ │ Quiz  │   │
│ └───────┘ └───────┘ └───────┘   │
└─────────────────────────────────┘
```

---

# 48. Profile Wireframe

```text
┌─────────────────────────────────┐
│ Profile                         │
│                                 │
│             ◉                   │
│         Nguyen Van A            │
│      user@example.com           │
│                                 │
│ Level                           │
│ A2                              │
│                                 │
│ Goal                            │
│ Travel                          │
│                                 │
│ Daily learning                  │
│ 20 minutes                      │
│                                 │
│ ─────────────────────────────   │
│ Goals                           │
│ Learning Settings               │
│ Notifications                   │
│ Account                         │
│                                 │
│ [          Logout           ]   │
└─────────────────────────────────┘
```

---

# 49. Goal Settings Wireframe

```text
┌─────────────────────────────────┐
│ < Goals                         │
│                                 │
│ Primary goal                    │
│ [✓ Travel]                      │
│                                 │
│ Other goals                     │
│ [ ] Business                    │
│ [✓] Communication              │
│ [ ] IELTS                       │
│                                 │
│ [          Save             ]   │
└─────────────────────────────────┘
```

---

# 50. Learning Settings Wireframe

```text
┌─────────────────────────────────┐
│ < Learning Settings             │
│                                 │
│ Daily learning time             │
│ [ 20 min                    ]   │
│                                 │
│ Timezone                        │
│ [ Asia/Ho_Chi_Minh          ]   │
│                                 │
│ Preferred study time            │
│ [ 20:00                     ]   │
│                                 │
│ [          Save             ]   │
└─────────────────────────────────┘
```

Preferred study time remains optional until supported consistently by the backend/schema.

---

# 51. Notification UX

V1:

```text
SRS-based reminder
+
configured/default study time
```

Future:

```text
AI notification timing
```

Notification examples:

```text
Time for a quick review
You have 12 words waiting.

Keep your streak alive 🔥
```

---

# 52. Loading States

## Home

```text
Header Skeleton
Progress Skeleton
Card Skeleton
Card Skeleton
```

## Vocabulary

```text
Search Skeleton
List Skeleton
```

## Progress

```text
Metric Skeleton
Chart Skeleton
```

Do not show a blank screen.

---

# 53. Empty States

## No reviews

```text
You're all caught up 🎉
```

## No weak words

```text
Great work!

You currently have no high-priority weak words.
```

## No learning history

```text
Start your first lesson to see your progress here.
```

---

# 54. Error States

Generic:

```text
Something went wrong.

Please try again.

[Retry]
```

Specific:

```text
Couldn't load today's learning plan.

[Retry]
```

---

# 55. Offline States

Official:

```text
No internet connection

Please reconnect to continue learning.

[Retry]
```

Read-only cache:

```text
Vocabulary can still be viewed if previously loaded.
```

No mutation:

```text
Learning attempts
Quiz submissions
SRS updates
```

---

# 56. AI UX

Preferred labels:

```text
✦ Recommended for you
Personalized for your practice
Based on your weak words
High priority
```

Avoid:

```text
LLM
ML score
Model confidence
Prompt
Inference
```

on learner screens.

---

# 57. AI Content UX Boundary

## Reusable content

```text
AI generation
 ↓
Validation
 ↓
Admin Review
 ↓
Approved
 ↓
User
```

## Personalized ephemeral content

```text
Personalization
 ↓
AI generation
 ↓
Validation
 ↓
Safety filter
 ↓
User
```

No per-result Admin Review.

---

# 58. User Trust

Recommendation explanation:

```text
Why this word?
```

Example:

```text
You missed this word 3 times recently.
```

This is user-facing explanation, not technical algorithm output.

---

# 59. Home Interaction Model

```text
Continue Learning
→ current Daily Plan item

Review card
→ Review

New Words card
→ Daily Plan new-word activity

Personalized Practice
→ AI Exercise

Progress
→ Progress

Avatar
→ Profile
```

---

# 60. Daily Plan Interaction Model

```text
GET /learning/today
        ↓
Show targets
        ↓
GET /learning/today/items
        ↓
Continue Learning
        ↓
Learning Activity
        ↓
Attempts
        ↓
Progress updated
        ↓
Complete plan
```

---

# 61. Review Interaction Model

```text
GET /learning/reviews
        ↓
View due queue
        ↓
Filter
        ↓
Start Review
        ↓
Flashcard
        ↓
Answer Quality
        ↓
SRS
```

---

# 62. Flashcard Interaction Model

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
SRS result
 ↓
Next
```

---

# 63. Personalized Exercise Interaction Model

```text
Home
 ↓
Practice Now
 ↓
POST /learning/personalized-exercise
 ↓
Question
 ↓
Answer
 ↓
Feedback
 ↓
Next
 ↓
Result
```

---

# 64. Network Boundary Interaction

```text
Action requires server?
        │
       YES
        │
   Internet available?
       /       \
     YES       NO
      │         │
      ▼         ▼
 Continue    Offline State
```

---

# 65. Screen-to-API Mapping

| Screen | API |
|---|---|
| Splash | Auth/profile check |
| Login | `POST /auth/login` |
| Register | `POST /auth/register` |
| Google Login | `POST /auth/google` |
| Forgot Password | Future auth endpoint if added |
| Goals | `GET /goals`, `PUT /users/me/goals` |
| Daily Time | `PUT /users/me/profile` |
| Placement Intro | none |
| Placement Question | `/assessments/*` |
| Assessment Result | `GET /assessments/{id}` |
| Home | `GET /learning/today`, `GET /progress` |
| Learn | `/learning/today`, `/vocabulary`, `/quizzes` |
| Review | `GET /learning/reviews` |
| Vocabulary Search | `GET /vocabulary` |
| Flashcard | `GET /vocabulary/{id}`, `POST /learning/attempts` |
| Word Detail | `GET /vocabulary/{id}`, `GET /vocabulary/{id}/progress` |
| Personalized Exercise | `POST /learning/personalized-exercise` |
| Quiz | `/quizzes/*`, `/quiz-attempts/*` |
| Learning Result | session/quiz result APIs |
| Progress | `/progress/*` |
| Weak Words | `GET /progress/weak-words` |
| Gamification | `/gamification/*` |
| Profile | `/users/me`, `/users/me/profile` |
| Goal Settings | `GET /users/me/goals`, `PUT /users/me/goals` |
| Learning Settings | `PUT /users/me/profile` |
| Notifications | `GET /notifications` |

---

# 66. Screen-to-User-Story Mapping

## Onboarding

```text
US-ONBOARD-01
Create account

US-ONBOARD-02
Choose goals

US-ONBOARD-03
Choose study time

US-ONBOARD-04
Complete placement assessment
```

## Learning

```text
US-LEARN-01
Learn new words

US-LEARN-02
Review due words

US-LEARN-03
Complete quiz

US-LEARN-04
Practice weak words
```

## Progress

```text
US-PROGRESS-01
View progress

US-PROGRESS-02
View weak words

US-PROGRESS-03
View streak/XP
```

---

# 67. Android Java V1 Architecture

Recommended:

```text
MVVM
```

Flow:

```text
Activity / Fragment
       ↓
ViewModel
       ↓
UseCase
       ↓
Repository
       ↓
Retrofit API
```

Example:

```text
HomeFragment
     ↓
HomeViewModel
     ↓
GetTodayPlanUseCase
     ↓
LearningRepository
     ↓
Retrofit
```

---

# 68. Android UI Package Structure

```text
ui/
├── auth/
├── onboarding/
├── home/
├── learn/
├── review/
├── vocabulary/
├── quiz/
├── personalized/
├── progress/
├── gamification/
├── notifications/
└── profile/

core/
├── navigation/
├── network/
├── storage/
├── ui/
└── utils/
```

`storage/` may support read-only cache, but must not be treated as an offline learning synchronization layer in V1.

---

# 69. Android Component Mapping

```text
PrimaryButton
→ reusable Button component

SelectionCard
→ onboarding goals/time

ProgressCard
→ Home/Progress

ReviewCard
→ Home/Review

VocabularyCard
→ Vocabulary/Search/Weak Words

QualitySelector
→ Flashcard

OfflineState
→ network-required learning screens
```

---

# 70. Flutter V2 Compatibility

Framework-independent:

```text
Navigation model
Design tokens
Screen states
User flows
Component behavior
Validation rules
API contract
```

Android-specific details must not leak into product UX.

---

# 71. UI State Model

Each network-driven screen supports:

```text
INITIAL
LOADING
SUCCESS
EMPTY
ERROR
RETRYING
OFFLINE
```

V1 offline:

```text
read-only where cache exists
mutation blocked
```

---

# 72. Form Validation

Client-side validation:

```text
Email
Password
Daily learning minutes
Goal selection
```

Backend remains authoritative.

---

# 73. API Error Mapping

```text
401
→ session expired
→ refresh/login

403
→ permission error

404
→ content unavailable

409
→ refresh/retry current state

429
→ rate limit / AI budget

503
→ service unavailable
```

Network unavailable:

```text
→ Offline State
→ Retry
```

Never show:

```text
SQL exception
Java stack trace
Raw server error
```

---

# 74. Performance UX

Regular APIs:

```text
Show skeleton quickly
Avoid blank screens
Avoid full-screen spinner for minor updates
```

AI:

```text
Generating your practice...
```

AI failure:

```text
Personalized practice isn't available right now.

You can continue with standard practice.

[Try Standard Practice]
```

---

# 75. Motion Rules

Button press:

```text
100–150ms
```

Flashcard flip:

```text
250–350ms
```

Progress update:

```text
300–500ms
```

Navigation:

```text
150–250ms
```

Motion must remain subtle.

---

# 76. High-Fidelity Priorities

First:

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

Then:

```text
Quiz
Vocabulary Search
Word Detail
Learning Result
Gamification
Notifications
Settings
```

---

# 77. Visual Hierarchy

## Learning screen

```text
WORD / QUESTION
      ↓
MEANING / OPTIONS
      ↓
PRIMARY ACTION
      ↓
SECONDARY INFORMATION
```

## Home

```text
TODAY'S PLAN
      ↓
CONTINUE LEARNING
      ↓
REVIEW
      ↓
NEW WORDS
      ↓
PERSONALIZED PRACTICE
      ↓
STREAK
```

---

# 78. Anti-Patterns to Avoid

```text
[ ] Many competing primary CTAs
[ ] Home becoming a second Learn tab
[ ] Review count confused with Daily Plan count
[ ] New Words ignoring Daily Plan target
[ ] Too many charts
[ ] AI badge everywhere
[ ] Red/green as only signal
[ ] Technical AI language
[ ] Offline sync promise
[ ] Offline attempt queue
[ ] Hard-coded phone layout
[ ] Heavy shadows/gradients
[ ] Overloaded Flashcard
```

---

# 79. Design Handoff Checklist

```text
[ ] Design tokens finalized
[ ] Typography finalized
[ ] Spacing finalized
[ ] Components finalized
[ ] Component states finalized
[ ] Navigation finalized
[ ] Full P0 wireframes complete
[ ] P1 wireframes complete
[ ] Loading designed
[ ] Empty designed
[ ] Error designed
[ ] Offline designed
[ ] Offline promise removed
[ ] Home CTA hierarchy finalized
[ ] Daily Plan vs Review finalized
[ ] New Words target tied to Daily Plan
[ ] Answer Quality usability-tested
[ ] API mapping checked
[ ] Android structure ready
[ ] Flutter compatibility checked
```

---

# 80. MVP Screen Priority

## P0

```text
Login
Register
Onboarding Welcome
Goal Selection
Daily Time
Placement Test
Assessment Result
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
Personalized Exercise
Gamification
Notifications
Goal Settings
Learning Settings
Word Detail
```

## P2

```text
Speaking
Writing
Listening
Reading
AI Tutor
Advanced ML insights
Offline-first
```

---

# 81. UX Acceptance Criteria

```text
[ ] User understands what to do today within seconds.
[ ] Home has one clearly dominant CTA.
[ ] Daily Plan and Review have different meanings.
[ ] New Words uses Daily Plan target.
[ ] User can start today's plan from Home.
[ ] Review exposes currently due review queue.
[ ] Flashcard flow is intuitive.
[ ] Answer Quality is understandable.
[ ] Client never submits isCorrect.
[ ] SRS feedback is understandable.
[ ] Personalized Exercise is clearly recognizable.
[ ] Progress is scannable.
[ ] Weak Word reason is understandable.
[ ] AI feels supportive rather than dominant.
[ ] Error recovery is clear.
[ ] Offline state is clear.
[ ] Offline state does not promise automatic sync.
[ ] No offline learning mutation is simulated.
[ ] Android and Flutter can reuse the same UX model.
```

---

# 82. Usability Test Priorities

Test these three areas first:

## Test A — Home hierarchy

Question:

> Người dùng có biết nút chính “Continue Learning” phải bấm trước không?

Success:

```text
User chooses primary CTA without guidance.
```

## Test B — Daily Plan vs Review

Question:

> Người dùng có hiểu sự khác nhau giữa “18 planned today” và “32 currently due” không?

Success:

```text
User can explain the distinction.
```

## Test C — Answer Quality

Question:

> Người dùng có hiểu khác nhau giữa Hard và Difficult không?

Success:

```text
User selects intended quality consistently.
```

---

# 83. Final UX Architecture

```text
                         HOME
                           │
                     TODAY'S PLAN
                           │
                 CONTINUE LEARNING
                           │
          ┌────────────────┼────────────────┐
          ▼                ▼                ▼
       REVIEW           NEW WORDS       PRACTICE
          │                │                │
          └────────────────┼────────────────┘
                           ▼
                       LEARNING
                           │
                           ▼
                        RESULT
                           │
              ┌────────────┴────────────┐
              ▼                         ▼
         WEAK WORDS                 PROGRESS
              │
              ▼
       PERSONALIZATION
              │
              ▼
        NEXT DAILY PLAN
```

---

# 84. Final Product Experience

```text
ASSESS
   ↓
PERSONALIZE
   ↓
PLAN
   ↓
LEARN
   ↓
REVIEW
   ↓
MEASURE
   ↓
ADAPT
   ↓
RECOMMEND
   ↓
LEARN AGAIN
```

Core UX message:

> “Ứng dụng hiểu mình đang yếu ở đâu và mỗi ngày cho mình một kế hoạch học vừa sức.”

---

# 85. Final Official Connectivity Model

```text
                 ENGLISH AI COACH V1
                         │
              ┌──────────┴──────────┐
              │                     │
            ONLINE               OFFLINE
              │                     │
              ▼                     ▼
        Full learning         Cached read-only
        functionality        content where available
              │                     │
              ▼                     ▼
        Submit attempts       No state mutation
        SRS update            No sync queue
        Quiz submit           Show Retry
        AI exercise
```

---

# 86. Final Design Decision

English AI Coach V1:

```text
Online-first
+
Personalized Learning
+
Daily Plan
+
SRS
+
AI-assisted Content
+
Progress
+
Gamification
```

Not V1:

```text
Offline-first
Offline synchronization
Conflict merge
Speaking
Writing
Listening
Reading
AI Tutor
Advanced ML
```

---

# 87. Final Handoff to Android

```text
Design System v1.2
       ↓
Wireframe v1.2
       ↓
High-Fidelity UI v1.1
       ↓
Interactive Prototype v1.1
       ↓
Usability Test
       ↓
Freeze V1 UI
       ↓
Android Java V1
```

---

# 88. Final Project Documentation Chain

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
App UI/UX Specification v1.1
        ↓
Design System + Wireframe v1.2
        ↓
High-Fidelity UI v1.1
        ↓
Interactive Prototype v1.1
        ↓
NEXT:
Usability Test
        ↓
Freeze V1 UI
        ↓
Android Java V1
```

---

# Reconciled V1 Contract Binding

UI behavior must bind to Android Technical Specification v1.1 and API/OpenAPI v1.4. Daily Plan is a stable persisted snapshot (including aggregate QUIZ item); assessment/SRS/personalization/gamification are backend authoritative; V1 is online-first; notification uses FCM/device preferences; all learner-facing copy is Vietnamese.
