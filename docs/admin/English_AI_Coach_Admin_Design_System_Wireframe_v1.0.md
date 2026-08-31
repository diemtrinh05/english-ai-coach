# Admin Design System + Wireframe v1.0 — English AI Coach

**Project:** English AI Coach  
**Document:** Admin Design System + Wireframe  
**Version:** 1.0  
**Status:** APPROVED BASELINE  
**Date:** 2026-08-29

**Based on:**
- `English_AI_Coach_SRS_v1.2.md`
- `English_AI_Coach_Database_Schema_v1.6.md`
- `English_AI_Coach_System_Architecture_v1.3.md`
- `English_AI_Coach_AI_Personalization_Specification_v1.3.md`
- `English_AI_Coach_API_Specification_v1.4.md`
- `English_AI_Coach_OpenAPI_Swagger_v1_4.md`
- `English_AI_Coach_Admin_Web_UI_UX_Specification_v1.2.md`

---

# 1. Purpose

Tài liệu này chuyển Admin Web UI/UX Specification thành:

```text
Admin Design Tokens
+
Component System
+
Layout Rules
+
Navigation
+
Full Wireframe Set
+
Interaction States
+
Developer Handoff
```

Admin Web là giao diện desktop-first dành cho administrator.

```text
Mobile App
→ USER learns

Admin Web
→ ADMIN manages / reviews / monitors
```

Admin Web không dùng cùng navigation model với Mobile App.

---

# 2. Admin Product Design Direction

## 2.1. Personality

```text
Professional
Clear
Efficient
Trustworthy
Information-dense
Controlled
```

## 2.2. Visual goal

Admin phải có khả năng:

```text
Find
→ Understand
→ Act
```

trong số ít thao tác.

---

# 3. Design Principles

## ADS-01 — Information First

Ưu tiên:

```text
Tables
Filters
Status
Actions
Metrics
```

---

## ADS-02 — Consistent Management

Các module phải có pattern nhất quán:

```text
Page Header
Search / Filters
Primary Action
Table / Content
Pagination
Row Actions
```

---

## ADS-03 — Safe Actions

Các hành động có hậu quả phải xác nhận:

```text
Lock
Unlock
Deactivate
Publish
Unpublish
Approve
Reject
```

---

## ADS-04 — Auditability

Admin action quan trọng phải có:

```text
Who
What
Target
When
```

---

## ADS-05 — AI Is Reviewable

AI reusable content phải hiển thị trạng thái rõ:

```text
Pending Review
Approved
Rejected
```

---

# 4. App Shell

Desktop:

```text
┌──────────────────────────────────────────────────────────────┐
│ Top Bar                                                      │
├────────────────────┬─────────────────────────────────────────┤
│                    │                                         │
│ Sidebar            │ Main Content                            │
│                    │                                         │
│ Dashboard          │                                         │
│ Users              │                                         │
│ Vocabulary         │                                         │
│ Topics             │                                         │
│ Quizzes            │                                         │
│ AI Content         │                                         │
│ Statistics         │                                         │
│ AI Usage           │                                         │
│ Audit Logs         │                                         │
│                    │                                         │
└────────────────────┴─────────────────────────────────────────┘
```

---

# 5. Desktop Grid

Recommended:

```text
Sidebar:
240–264px

Main content:
flex

Content padding:
24–32px

Max-width:
1440px where useful
```

---

# 6. Top Bar

```text
┌──────────────────────────────────────────────────────────────┐
│ Page Title                                      Admin ▼      │
└──────────────────────────────────────────────────────────────┘
```

Optional:

```text
Notifications
Help
Profile
Logout
```

Top bar remains visually quiet.

---

# 7. Sidebar

```text
┌──────────────────────┐
│ English AI Coach     │
│ Administration       │
│                      │
│ 🏠 Dashboard         │
│                      │
│ 👥 Users             │
│ 📚 Vocabulary        │
│ 🏷 Topics             │
│ 📝 Quizzes           │
│                      │
│ ✦ AI Content         │
│ 📊 Statistics        │
│ 💰 AI Usage          │
│ 🧾 Audit Logs        │
│                      │
│ ⚙ Settings           │
└──────────────────────┘
```

Selected:

```text
soft primary surface
+
clear selected indicator
```

---

# 8. Admin Color Tokens

```text
color.primary
color.primaryContainer

color.background
color.surface
color.surfaceVariant

color.textPrimary
color.textSecondary
color.textMuted

color.border
color.divider

color.success
color.warning
color.error
color.info

color.disabled
color.overlay
```

---

# 9. Semantic Color Usage

| Role | Usage |
|---|---|
| Primary | Primary actions, selected nav |
| Success | Approved, Active, Published |
| Warning | Pending, Budget warning |
| Error | Failed, Rejected, validation |
| Info | Contextual information |
| Muted | Secondary metadata |

Color must not be the only status indicator.

---

# 10. Typography

```text
Page title
24–28px / Semibold

Section title
18–20px / Semibold

Body
14–16px

Table
13–14px

Caption
12px
```

Use one primary font family throughout.

---

# 11. Spacing

```text
8
12
16
20
24
32
40
48
```

Page:

```text
24–32px
```

Card:

```text
16–24px
```

---

# 12. Radius

```text
6px → small controls
8px → inputs / badges
10–12px → cards
12px → buttons
```

Admin should generally use smaller radius than the learner Mobile App to support a more professional visual language.

---

# 13. Elevation

Prefer:

```text
border
+
surface contrast
```

over heavy shadows.

Suggested:

```text
0 → page
1 → cards
2 → dropdown
4 → modal
```

---

# 14. Buttons

## Primary

```text
[+ Add Vocabulary]
```

Use for:

```text
Create
Save
Publish
Approve
```

## Secondary

```text
[Cancel]
[View]
[Edit]
```

## Destructive

```text
[Reject]
[Lock User]
[Deactivate]
```

Must use confirmation where required.

---

# 15. Inputs

```text
Label
┌─────────────────────────────┐
│ Input                       │
└─────────────────────────────┘
Helper text
```

States:

```text
Default
Focused
Filled
Error
Disabled
Loading
```

---

# 16. Search

```text
┌───────────────────────────────────────┐
│ 🔍 Search...                          │
└───────────────────────────────────────┘
```

States:

```text
Default
Typing
Loading
No result
Error
```

---

# 17. Filter

```text
[Role ▼]
[Status ▼]
[CEFR ▼]
[Topic ▼]
```

Reset:

```text
Clear filters
```

---

# 18. Table

Header:

```text
┌────────────┬─────────────┬────────┬──────────┬──────────┐
│ Name       │ Email       │ CEFR   │ Status   │ Action   │
├────────────┼─────────────┼────────┼──────────┼──────────┤
│ Nguyen A   │ a@x.com     │ A2     │ ACTIVE   │ ⋮        │
│ Tran B     │ b@x.com     │ B1     │ LOCKED   │ ⋮        │
└────────────┴─────────────┴────────┴──────────┴──────────┘
```

Table requirements:

```text
sticky header when useful
row hover
row action
sortable headers where supported
```

---

# 19. Pagination

```text
Rows per page: 20

< 1 2 3 4 ... 12 >
```

Maps to:

```text
content
page
size
totalElements
totalPages
hasNext
```

---

# 20. Status Badge

```text
ACTIVE
LOCKED
DRAFT
PUBLISHED
PENDING_REVIEW
APPROVED
REJECTED
```

Example:

```text
● ACTIVE
● PENDING_REVIEW
● REJECTED
```

Use icon/text with semantic color.

---

# 21. Metric Card

```text
┌──────────────────────┐
│ Active Users         │
│                      │
│ 1,284                │
│ ↑ 8.4%              │
└──────────────────────┘
```

---

# 22. Alert Card

```text
┌─────────────────────────────────────┐
│ ⚠ AI Budget                         │
│ 82% of today's budget used          │
│ [View AI Usage]                     │
└─────────────────────────────────────┘
```

---

# 23. Modal

Structure:

```text
Title
Description

Content

[Cancel] [Primary]
```

Use for:

```text
confirmation
quick form
review action
```

---

# 24. Drawer

Use for:

```text
details
filters
secondary inspection
```

---

# 25. Toast / Snackbar

Success:

```text
Vocabulary saved successfully.
```

Error:

```text
Could not save changes.
```

Never rely on toast alone for critical errors.

---

# 26. Tabs

AI Content:

```text
[Pending Review] [Approved] [Rejected]
```

Statistics:

```text
[Learning] [AI Usage]
```

---

# 27. Chart Rules

Use simple charts:

```text
Line
Bar
Area where appropriate
```

Avoid:

```text
3D charts
too many colors
decorative charts
```

---

# 28. Dashboard Wireframe

```text
┌──────────────────────────────────────────────────────────────┐
│ Dashboard                                                    │
│                                                              │
│ ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐ ┌────────────┐ │
│ │ Users  │ │ Active │ │ Words  │ │ AI Rev │ │ AI Cost    │ │
│ │ 12,450 │ │ 1,284  │ │ 3,240  │ │   28   │ │ $18.42     │ │
│ └────────┘ └────────┘ └────────┘ └────────┘ └────────────┘ │
│                                                              │
│ ┌────────────────────────────────┐ ┌────────────────────────┐│
│ │ Learning Activity              │ │ AI Usage               ││
│ │                                │ │                        ││
│ │        ╱╲      ╱╲              │ │ Requests   12,420      ││
│ │    ╱───╯ ╲────╯  ╲──           │ │ Tokens     3.8M        ││
│ │                                │ │ Cost       $18.42      ││
│ └────────────────────────────────┘ └────────────────────────┘│
│                                                              │
│ Alerts                                                       │
│ ⚠ 28 AI content items awaiting review                        │
│ ⚠ AI budget at 82%                                           │
└──────────────────────────────────────────────────────────────┘
```

---

# 29. User Management Wireframe

```text
┌──────────────────────────────────────────────────────────────┐
│ Users                                                        │
│                                                              │
│ [Search users...]   [Role ▼] [Status ▼]                    │
│                                                              │
│ ┌─────────┬─────────────────┬──────┬─────────┬────────────┐ │
│ │ Name    │ Email           │ CEFR │ Status  │ Actions    │ │
│ ├─────────┼─────────────────┼──────┼─────────┼────────────┤ │
│ │ Nguyen  │ a@example.com   │ A2   │ ACTIVE  │ View ⋮     │ │
│ │ Tran    │ b@example.com   │ B1   │ LOCKED  │ View ⋮     │ │
│ └─────────┴─────────────────┴──────┴─────────┴────────────┘ │
│                                                              │
│                     < 1 2 3 ... >                            │
└──────────────────────────────────────────────────────────────┘
```

---

# 30. User Detail Wireframe

```text
┌──────────────────────────────────────────────────────────────┐
│ ← User Detail                                                │
│                                                              │
│ Nguyen A                                      ACTIVE         │
│ a@example.com                                 USER           │
│                                                              │
│ CEFR        A2       Goal      Travel                       │
│ Daily Time  20 min   Streak    12                           │
│                                                              │
│ ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐             │
│ │ Learned │ │Mastered │ │Accuracy │ │ XP      │             │
│ │ 320     │ │ 140     │ │ 84.5%   │ │ 1,250   │             │
│ └─────────┘ └─────────┘ └─────────┘ └─────────┘             │
│                                                              │
│ Recent Activity                                               │
│ ...                                                          │
│                                                              │
│ [Lock User]                                                  │
└──────────────────────────────────────────────────────────────┘
```

---

# 31. Lock User Modal

```text
Lock this user?

The user will not be able to access the app.

[Cancel] [Lock User]
```

---

# 32. Vocabulary Management Wireframe

```text
┌──────────────────────────────────────────────────────────────┐
│ Vocabulary                                  [+ Add Vocabulary]│
│                                                              │
│ [Search...] [CEFR ▼] [Topic ▼] [POS ▼]                     │
│                                                              │
│ ┌────────────┬────────┬─────┬──────────────┬──────────────┐ │
│ │ Word       │ POS    │CEFR │ Topic        │ Action       │ │
│ ├────────────┼────────┼─────┼──────────────┼──────────────┤ │
│ │ abandon    │ verb   │ B1  │ Daily Life   │ Edit ⋮       │ │
│ │ negotiate  │ verb   │ B2  │ Business     │ Edit ⋮       │ │
│ └────────────┴────────┴─────┴──────────────┴──────────────┘ │
│                                                              │
│                    < 1 2 3 ... >                             │
└──────────────────────────────────────────────────────────────┘
```

---

# 33. Vocabulary Editor Wireframe

```text
┌──────────────────────────────────────────────────────────────┐
│ ← Add Vocabulary                                             │
│                                                              │
│ Word                                                         │
│ [ abandon                       ]                             │
│                                                              │
│ Phonetic IPA                                                 │
│ [ /əˈbændən/                  ]                             │
│                                                              │
│ Meaning Vietnamese                                            │
│ [ từ bỏ                         ]                             │
│                                                              │
│ Meaning English                                               │
│ [ to leave completely         ]                             │
│                                                              │
│ Part of Speech                                                │
│ [ Verb ▼ ]                                                    │
│                                                              │
│ CEFR                                                         │
│ [ B1 ▼ ]                        [Suggest CEFR (FUTURE/V2) with AI]       │
│                                                              │
│ Topics                                                       │
│ [Daily Life] [+ Add]                                         │
│                                                              │
│ Audio URL                                                    │
│ [___________________________]                                │
│                                                              │
│ Image URL                                                    │
│ [___________________________]                                │
│                                                              │
│ [Cancel]                                  [Save]             │
└──────────────────────────────────────────────────────────────┘
```

---

# 34. FUTURE/V2 — AI CEFR Suggestion Result

```text
┌──────────────────────────────────────┐
│ ✦ AI CEFR Suggestion (FUTURE/V2)     │
│                                      │
│ Suggested level                      │
│ B2                                   │
│                                      │
│ Reason                               │
│ Common intermediate business         │
│ vocabulary.                          │
│                                      │
│ [Edit Manually] [Accept Suggestion] │
└──────────────────────────────────────┘
```

---

# 35. Topic Management Wireframe

```text
┌──────────────────────────────────────────────────────────────┐
│ Topics                                      [+ Add Topic]   │
│                                                              │
│ ┌──────────────────────────────────────────────────────────┐ │
│ │ Business                                                 │ │
│ │ ├── Finance                                              │ │
│ │ │   ├── Banking                                          │ │
│ │ │   └── Investment                                       │ │
│ │ └── Marketing                                            │ │
│ │                                                          │ │
│ │ Travel                                                   │ │
│ │ ├── Airport                                              │ │
│ │ └── Hotel                                                │ │
│ └──────────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────────┘
```

---

# 36. Topic Editor

```text
┌──────────────────────────────────────────────────────────────┐
│ Topic                                                       │
│                                                              │
│ Name                                                         │
│ [ Business Finance              ]                           │
│                                                              │
│ Description                                                  │
│ [______________________________]                            │
│                                                              │
│ Parent Topic                                                 │
│ [ Business ▼ ]                                               │
│                                                              │
│ Icon URL                                                     │
│ [______________________________]                            │
│                                                              │
│ [Cancel]                                  [Save]             │
└──────────────────────────────────────────────────────────────┘
```

---

# 37. Quiz Management

```text
┌──────────────────────────────────────────────────────────────┐
│ Quizzes                                      [+ Create Quiz]│
│                                                              │
│ [Search...] [Topic ▼] [CEFR ▼] [Status ▼]                  │
│                                                              │
│ ┌───────────────┬────────────┬──────┬─────────────┬────────┐│
│ │ Title         │ Topic      │ CEFR │ Status      │ Action ││
│ ├───────────────┼────────────┼──────┼─────────────┼────────┤│
│ │ Travel A2     │ Travel     │ A2   │ PUBLISHED   │ ⋮      ││
│ │ Business B1   │ Business   │ B1   │ DRAFT       │ ⋮      ││
│ └───────────────┴────────────┴──────┴─────────────┴────────┘│
└──────────────────────────────────────────────────────────────┘
```

---

# 38. Quiz Editor

```text
┌──────────────────────────────────────────────────────────────┐
│ ← Quiz Editor                                                │
│                                                              │
│ Title                                                        │
│ [ Travel Vocabulary A2            ]                         │
│                                                              │
│ Description                                                  │
│ [______________________________]                            │
│                                                              │
│ Topic                        CEFR                            │
│ [ Travel ▼ ]                 [ A2 ▼ ]                       │
│                                                              │
│ Questions                                                    │
│ 1. What does "abandon" mean?                                │
│    [Edit]                                                    │
│                                                              │
│ 2. ...                                                       │
│    [Edit]                                                    │
│                                                              │
│ [+ Add Question]                                             │
│                                                              │
│ [Save Draft]                            [Publish]            │
└──────────────────────────────────────────────────────────────┘
```

---

# 39. Quiz Question Editor

```text
┌──────────────────────────────────────────────────────────────┐
│ Question 1                                                   │
│                                                              │
│ Question text                                                │
│ [ What does "abandon" mean? ]                               │
│                                                              │
│ Type                                                         │
│ [Multiple Choice ▼]                                          │
│                                                              │
│ Options                                                      │
│ A. [To leave                    ]                            │
│ B. [To improve                 ]                             │
│ C. [To purchase               ]                             │
│ D. [To organize               ]                             │
│                                                              │
│ Correct answer                                               │
│ [ A ▼ ]                                                       │
│                                                              │
│ [Cancel]                                  [Save]             │
└──────────────────────────────────────────────────────────────┘
```

---

# 40. AI Content Management

```text
┌──────────────────────────────────────────────────────────────┐
│ AI Content                             [+ Generate Content]  │
│                                                              │
│ [Pending Review] [Approved] [Rejected]                      │
│                                                              │
│ [Type ▼] [Scope ▼] [Date ▼]                                │
│                                                              │
│ ┌────────────┬──────────────┬───────────────┬──────────────┐│
│ │ Type       │ Vocabulary   │ Status        │ Action       ││
│ ├────────────┼──────────────┼───────────────┼──────────────┤│
│ │ EXAMPLE    │ abandon      │ PENDING       │ Review       ││
│ │ MNEMONIC   │ negotiate    │ PENDING       │ Review       ││
│ └────────────┴──────────────┴───────────────┴──────────────┘│
└──────────────────────────────────────────────────────────────┘
```

---

# 41. AI Content Detail

```text
┌──────────────────────────────────────────────────────────────┐
│ ← AI Generated Content                                       │
│                                                              │
│ Type              EXAMPLE                                   │
│ Vocabulary        abandon                                   │
│ Scope             REUSABLE                                  │
│                                                              │
│ Generated Content                                            │
│ ┌──────────────────────────────────────────────────────────┐ │
│ │ He abandoned the project after the first month.          │ │
│ └──────────────────────────────────────────────────────────┘ │
│                                                              │
│ Model             model-name                                │
│ Status            REJECTED                                  │
│                                                              │
│ Review Note                                                │
│ Incorrect example                                             │
│                                                              │
│ Reviewed By       Admin                                     │
│ Reviewed At       2026-08-29 15:30                         │
│                                                              │
│ [Approve] [Reject]                                           │
└──────────────────────────────────────────────────────────────┘
```

For `APPROVED` content:

```text
Review Note
may be empty/null
```

---

# 42. AI Reject Modal

```text
┌──────────────────────────────────────┐
│ Reject AI Content                    │
│                                      │
│ Reject reason                         │
│ ┌──────────────────────────────────┐ │
│ │                                  │ │
│ └──────────────────────────────────┘ │
│                                      │
│ This reason will be stored as the    │
│ review note and recorded in audit.   │
│                                      │
│ [Cancel]             [Reject]        │
└──────────────────────────────────────┘
```

Persistence:

```text
reason
 ├──→ ai_generated_content.review_note
 └──→ admin_audit_logs.details.reason
```

---

# 43. Learning Statistics

```text
┌──────────────────────────────────────────────────────────────┐
│ Learning Statistics                                         │
│                                                              │
│ [From] [To]                                                 │
│                                                              │
│ ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐                │
│ │ Active │ │Sessions│ │ Words  │ │Accuracy│                │
│ │ 1,284  │ │ 8,420  │ │124,560 │ │ 82.4%  │                │
│ └────────┘ └────────┘ └────────┘ └────────┘                │
│                                                              │
│ Active Users Trend                                           │
│      ╱╲      ╱╲                                            │
│  ╱───╯ ╲────╯  ╲──                                        │
│                                                              │
│ Accuracy Trend                                               │
│ ───╱╲────╲──────                                           │
└──────────────────────────────────────────────────────────────┘
```

---

# 44. AI Usage

```text
┌──────────────────────────────────────────────────────────────┐
│ AI Usage                                                     │
│                                                              │
│ [From] [To] [Provider ▼] [Model ▼] [Feature ▼]             │
│                                                              │
│ ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐                │
│ │Requests│ │ Tokens │ │ Cost   │ │Blocked │                │
│ │ 12,420 │ │ 3.8M   │ │ $18.42 │ │  143   │                │
│ └────────┘ └────────┘ └────────┘ └────────┘                │
│                                                              │
│ Cost by Feature                                              │
│ Example Generation                $4.10                     │
│ CEFR Suggestion                    $1.20                     │
│ Personalized Exercise             $9.80                     │
│ Explanation                       $3.32                      │
└──────────────────────────────────────────────────────────────┘
```

---

# 45. AI Budget Alert

Normal:

```text
┌─────────────────────────────────────┐
│ AI Daily Budget                     │
│                                     │
│ $8.20 / $10.00                      │
│ ████████░░ 82%                      │
│                                     │
│ Status: NORMAL                      │
└─────────────────────────────────────┘
```

Near limit:

```text
⚠ AI daily budget near limit
```

Exceeded:

```text
⚠ AI daily budget exceeded

AI content generation has been temporarily disabled.
```

---

# 46. Audit Logs

```text
┌──────────────────────────────────────────────────────────────┐
│ Audit Logs                                                   │
│                                                              │
│ [Admin ▼] [Action ▼] [Target ▼] [From] [To]                │
│                                                              │
│ ┌───────────────┬────────┬──────────────┬─────────────────┐│
│ │ Timestamp     │ Admin  │ Action       │ Target          ││
│ ├───────────────┼────────┼──────────────┼─────────────────┤│
│ │ 10:20         │ Admin1 │ APPROVE_AI   │ example123      ││
│ │ 10:15         │ Admin1 │ LOCK_USER    │ user123         ││
│ │ 09:50         │ Admin2 │ UPDATE_WORD  │ abandon         ││
│ └───────────────┴────────┴──────────────┴─────────────────┘│
└──────────────────────────────────────────────────────────────┘
```

---

# 47. Audit Detail / Inspection

```text
┌──────────────────────────────────────────┐
│ Audit Event                               │
│                                          │
│ Timestamp       2026-08-29 10:20         │
│ Admin           Admin1                   │
│ Action          REJECT                    │
│ Target Table    ai_generated_content     │
│ Target ID       uuid                      │
│                                          │
│ Details                                   │
│ {                                        │
│   "reason": "Incorrect example"          │
│ }                                        │
└──────────────────────────────────────────┘
```

---

# 48. Generic Loading

Table:

```text
Header
Skeleton Row
Skeleton Row
Skeleton Row
Skeleton Row
```

Dashboard:

```text
Metric Skeleton
Chart Skeleton
```

---

# 49. Generic Empty

```text
No data found.

Try changing the filters.
```

Module-specific:

```text
No pending AI content.
```

---

# 50. Generic Error

```text
Unable to load data.

[Retry]
```

Save error:

```text
Changes could not be saved.

[Try Again]
```

---

# 51. Permission Error

```text
403

You don't have permission
to access this page.

[Back]
```

---

# 52. Session Expired

```text
Your session has expired.

[Sign In]
```

---

# 53. Responsive Rules

Primary:

```text
Desktop ≥ 1200px
```

Supported:

```text
1024px+
```

At smaller width:

```text
Sidebar collapses
Tables may horizontal-scroll
Filters wrap
Action menu remains accessible
```

No requirement to replicate Mobile App design.

---

# 54. Accessibility

```text
Keyboard navigation
Visible focus
Semantic table headers
ARIA labels where applicable
Readable contrast
Non-color status indicators
Modal keyboard handling
```

---

# 55. Admin → API Mapping

| UI | Endpoint |
|---|---|
| Dashboard Learning | `/admin/statistics/learning` |
| Dashboard AI | `/admin/statistics/ai-usage` |
| Users | `/admin/users` |
| User Detail | `/admin/users/{userId}` |
| Lock | `/admin/users/{userId}/lock` |
| Unlock | `/admin/users/{userId}/unlock` |
| Vocabulary | `/admin/vocabulary` |
| Vocabulary Editor | `/admin/vocabulary/{vocabularyId}` |
| Topics | `/admin/topics` |
| Quizzes | `/admin/quizzes` |
| Quiz Questions | `/admin/quizzes/{quizId}/questions` |
| AI Content List | `/admin/ai-content` |
| AI Generate | `/admin/ai-content/generate` |
| AI Approve | `/admin/ai-content/{contentId}/approve` |
| AI Reject | `/admin/ai-content/{contentId}/reject` |
| Learning Stats | `/admin/statistics/learning` |
| AI Usage | `/admin/statistics/ai-usage` |
| Audit | `/admin/audit-logs` |

---

# 56. Admin Component Map

```text
AppShell
├── Sidebar
├── TopBar
└── MainContent

Data Components
├── DataTable
├── SearchBar
├── FilterBar
├── Pagination
├── StatusBadge
├── MetricCard
└── ChartCard

Action Components
├── PrimaryButton
├── SecondaryButton
├── DangerButton
├── ActionMenu
├── ConfirmDialog
└── Toast

Content Components
├── VocabularyForm
├── TopicTree
├── QuizEditor
├── AiContentReview
├── StatCard
└── AuditDetail
```

---

# 57. Admin State Model

Every data-driven page:

```text
INITIAL
LOADING
SUCCESS
EMPTY
ERROR
```

Actions may additionally use:

```text
SUBMITTING
SUCCESS
FAILURE
```

---

# 58. Admin Interaction Rules

## Table row

```text
Hover
→ highlight

Action menu
→ View / Edit / Lock / Deactivate
```

## Modal

```text
Open
→ focus first control

Confirm
→ submitting
→ success/error
```

## Save

```text
Save
→ disable duplicate submission
→ loading
→ result
```

---

# 59. Destructive Action Rules

```text
Lock
→ confirmation

Deactivate
→ confirmation

Reject
→ reason required

Publish
→ confirmation
```

---

# 60. AI Review Workflow UI

```text
AI Content
     ↓
PENDING_REVIEW
     ↓
Open Detail
     ↓
Inspect content
     ↓
     ┌───────────────┐
     │               │
   APPROVE         REJECT
     │               │
     ▼               ▼
 APPROVED       Reason required
                     ↓
                 REJECTED
                     ↓
             Review Note stored
```

---

# 61. Admin Demo Happy Path

```text
Admin Login
 ↓
Dashboard
 ↓
Vocabulary
 ↓
Add Vocabulary
 ↓
Suggest CEFR (FUTURE/V2) with AI
 ↓
Accept Suggestion
 ↓
Save
 ↓
AI Content
 ↓
Review Example
 ↓
Approve / Reject
 ↓
Statistics
 ↓
AI Usage
 ↓
Audit Logs
```

---

# 62. Admin UX Acceptance Criteria

```text
[ ] Sidebar navigation works.
[ ] Dashboard loads key metrics.
[ ] Users support search/filter/pagination.
[ ] User detail is readable.
[ ] Lock/unlock requires safe action handling.
[ ] Vocabulary supports create/edit/status.
[ ] AI CEFR suggestion is understandable.
[ ] Topics support hierarchy.
[ ] Quizzes support questions and publishing.
[ ] AI reusable content has review states.
[ ] Reject reason is required.
[ ] Review note is visible on AI Content Detail.
[ ] Review note is persisted in database.
[ ] Reject reason is recorded in audit.
[ ] Learning statistics are readable.
[ ] AI cost is visible.
[ ] AI budget warnings are visible.
[ ] Audit logs are searchable/filterable.
[ ] Loading/empty/error states exist.
[ ] Permission/session states exist.
[ ] Keyboard accessibility is supported.
[ ] Admin UI is visually distinct from learner UI.
```

---

# 63. Design Handoff Checklist

```text
[ ] Sidebar finalized
[ ] Top Bar finalized
[ ] Page grid finalized
[ ] Table component finalized
[ ] Search/filter finalized
[ ] Pagination finalized
[ ] Status badges finalized
[ ] Buttons finalized
[ ] Dialogs finalized
[ ] Forms finalized
[ ] Dashboard cards finalized
[ ] Charts finalized
[ ] AI Review component finalized
[ ] Audit component finalized
[ ] Loading states finalized
[ ] Empty states finalized
[ ] Error states finalized
[ ] Permission states finalized
[ ] Responsive rules finalized
[ ] Accessibility reviewed
[ ] API mapping reviewed
```

---

# 64. P0 Admin Screens

```text
1. Admin Login
2. Dashboard
3. Users
4. Vocabulary
5. AI Content Review
6. Learning Statistics
```

---

# 65. P1 Admin Screens

```text
7. User Detail
8. Vocabulary Editor
9. Topics
10. Quizzes
11. Quiz Question Editor
12. AI Usage
13. Audit Logs
14. AI Content Detail
```

---

# 66. P2 Admin Screens

```text
Advanced Analytics
Bulk Operations
Role Management
Advanced Moderation
System Configuration
```

---

# 67. Final Admin Information Architecture

```text
ADMIN
│
├── Dashboard
│
├── Users
│   └── User Detail
│
├── Vocabulary
│   └── Vocabulary Editor
│
├── Topics
│   └── Topic Editor
│
├── Quizzes
│   ├── Quiz Editor
│   └── Question Editor
│
├── AI Content
│   └── AI Content Detail
│
├── Statistics
│
├── AI Usage
│
└── Audit Logs
```

---

# 68. Final Admin Product Experience

```text
LOGIN
  ↓
DASHBOARD
  ↓
MANAGE
  ├── Users
  ├── Vocabulary
  ├── Topics
  └── Quizzes
  ↓
AI OPERATIONS
  ├── Generate
  ├── Review
  └── Monitor Cost
  ↓
ANALYTICS
  ↓
AUDIT
```

---

# 69. Final Admin Design Philosophy

Admin Web should feel:

```text
Controlled
Predictable
Efficient
Auditable
Professional
```

The goal is not visual entertainment.

The goal is:

> **Give administrators the fastest safe path from information to action.**

---

# 70. Final Design Sequence

```text
Admin UI/UX Specification
        ↓
Admin Design System + Wireframe
        ↓
Admin High-Fidelity UI
        ↓
Admin Interactive Prototype
        ↓
UX Review
        ↓
Freeze Admin UI
        ↓
Spring Boot Admin API
        ↓
Admin Web Implementation
```

---

# 71. Project UI Architecture

```text
                   ENGLISH AI COACH
                          │
             ┌────────────┴────────────┐
             │                         │
        MOBILE APP                 ADMIN WEB
             │                         │
        USER EXPERIENCE          ADMIN EXPERIENCE
             │                         │
       Learn / Review             Manage
       Personalize                Review AI
       Progress                   Monitor
                                  Audit
```

---

# Reconciled V1 Binding

This design-system/wireframe document binds to Admin Web Technical Specification v1.1 and Admin UI/UX v1.2. Any AI CEFR suggestion affordance is Future/V2 only; V1 uses manual CEFR selection.
