# Admin High-Fidelity UI Design Specification v1.1 — English AI Coach

**Project:** English AI Coach  
**Document:** Admin High-Fidelity UI Design Specification  
**Version:** 1.1  
**Status:** APPROVED BASELINE  
**Date:** 2026-08-31  

**Based on:**
- `English_AI_Coach_Admin_Web_UI_UX_Specification_v1.2.md`
- `English_AI_Coach_Admin_Design_System_Wireframe_v1.0.md`
- `English_AI_Coach_Database_Schema_v1.6.md`
- `English_AI_Coach_API_Specification_v1.4.md`
- `English_AI_Coach_OpenAPI_Swagger_v1_4.md`

---

# 1. Purpose

Tài liệu này chốt visual specification cho Admin Web ở mức đủ chi tiết để tạo giao diện thật trước implementation Spring Boot Admin Web.

High-Fidelity phải chốt:

```text
Shell
Sidebar
Top Bar
Typography
Color
Tables
Forms
Buttons
Badges
Cards
Charts
Modals
Drawers
AI Review
Audit
Responsive
Accessibility
States
```

---

# 2. Visual Direction

Admin Web:

```text
Professional
Minimal
Information-dense
Trustworthy
Controlled
```

Khác Mobile App:

```text
Mobile
→ learning-first
→ friendly
→ card-oriented

Admin
→ management-first
→ table-oriented
→ dense
→ operational
```

---

# 3. Theme

Default:

```text
Light theme
Neutral background
White surface
Primary blue/indigo family
Success green
Warning amber
Error red
Info blue
```

Avoid:

```text
strong gradients
heavy shadows
game-like colors
excessive illustrations
```

---

# 4. App Shell

Recommended desktop:

```text
Sidebar = 240–264px

Top bar = 56–64px

Content padding = 24–32px
```

Main content should not exceed a comfortable reading width for dense tables.

---

# 5. Sidebar High-Fidelity

Structure:

```text
┌──────────────────────────┐
│ English AI Coach         │
│ ADMIN                    │
│                          │
│  Dashboard               │
│                          │
│  Users                   │
│  Vocabulary              │
│  Topics                  │
│  Quizzes                 │
│                          │
│  ✦ AI Content            │
│  Statistics              │
│  AI Usage                │
│  Audit Logs              │
│                          │
│  Settings                │
└──────────────────────────┘
```

Selected item:

```text
primary-tinted background
bold/medium label
icon emphasized
```

---

# 6. Top Bar High-Fidelity

```text
┌─────────────────────────────────────────────────────────┐
│ Dashboard                              Admin ▼          │
└─────────────────────────────────────────────────────────┘
```

Right side:

```text
Notifications
Avatar
Admin menu
```

---

# 7. Page Header

Every page:

```text
Page Title
Supporting description

[Primary Action]
```

Example:

```text
Vocabulary

Manage vocabulary, CEFR and topics.

[+ Add Vocabulary]
```

---

# 8. Card High-Fidelity

Surface:

```text
white
1px border
subtle radius
minimal elevation
```

Content:

```text
Title
Value
Supporting metadata
```

---

# 9. Metric Card

```text
┌──────────────────────────┐
│ Active Users             │
│                          │
│ 1,284                    │
│ +8.4% this month         │
└──────────────────────────┘
```

Number:

```text
28–32px
semibold/bold
```

Label:

```text
13–14px
secondary
```

---

# 10. Dashboard High-Fidelity

```text
Dashboard

┌────────────┐ ┌────────────┐ ┌────────────┐ ┌────────────┐
│ Users      │ │ Active     │ │ Vocabulary │ │ AI Review  │
│ 12,450     │ │ 1,284      │ │ 3,240      │ │ 28         │
└────────────┘ └────────────┘ └────────────┘ └────────────┘

┌──────────────────────────────────────┐ ┌────────────────────┐
│ Learning Activity                    │ │ AI Usage           │
│                                      │ │                    │
│       ╭──╮        ╭──╮              │ │ 12,420 requests    │
│    ╭──╯  ╰────╭───╯  ╰──            │ │ 3.8M tokens        │
│                                      │ │ $18.42 cost        │
└──────────────────────────────────────┘ └────────────────────┘

⚠ 28 AI content items awaiting review
⚠ AI daily budget at 82%
```

---

# 11. Table High-Fidelity

Header:

```text
font-weight: 600
small text
subtle surface difference
```

Rows:

```text
48–56px
```

Hover:

```text
subtle surface highlight
```

Actions:

```text
⋮
```

---

# 12. User Management High-Fidelity

```text
Users

[Search users...]
[Role ▼] [Status ▼]

┌─────────────┬──────────────────┬──────┬─────────┬─────────┐
│ Name        │ Email            │ CEFR │ Status  │ Actions │
├─────────────┼──────────────────┼──────┼─────────┼─────────┤
│ Nguyen A    │ a@example.com    │ A2   │ ●ACTIVE │ ⋮       │
│ Tran B      │ b@example.com    │ B1   │ ●LOCKED │ ⋮       │
└─────────────┴──────────────────┴──────┴─────────┴─────────┘
```

Status badge:

```text
ACTIVE
LOCKED
```

---

# 13. User Detail High-Fidelity

```text
← User Detail

Nguyen A                            ● ACTIVE
a@example.com                       USER

┌─────────┬─────────┬─────────┬─────────┐
│ Learned │ Mastered│Accuracy │ Streak  │
│ 320     │ 140     │ 84.5%   │ 12      │
└─────────┴─────────┴─────────┴─────────┘

CEFR       A2
Goal       Travel
Daily Time 20 min

Recent Activity
────────────────────────────────────
...
```

Lock action should be destructive and visually separated.

---

# 14. Vocabulary Management High-Fidelity

```text
Vocabulary                          [+ Add Vocabulary]

[Search vocabulary...] [CEFR ▼] [Topic ▼] [POS ▼]

┌────────────┬──────────┬──────┬────────────┬────────┐
│ Word       │ POS      │ CEFR │ Topic      │ Action │
├────────────┼──────────┼──────┼────────────┼────────┤
│ abandon    │ verb     │ B1   │ Daily Life │ Edit ⋮ │
│ negotiate  │ verb     │ B2   │ Business   │ Edit ⋮ │
└────────────┴──────────┴──────┴────────────┴────────┘
```

Primary action:

```text
Add Vocabulary
```

---

# 15. Vocabulary Editor High-Fidelity

Two-column desktop form where space allows:

```text
┌───────────────────────────┐ ┌───────────────────────────┐
│ Word                      │ │ CEFR                      │
│ [ abandon              ]  │ │ [ B1 ▼ ]                 │
│                           │ │                           │
│ IPA                       │ │ Part of Speech            │
│ [ /əˈbændən/           ]  │ │ [ Verb ▼ ]               │
│                           │ │                           │
│ Meaning Vietnamese        │ │ Topics                    │
│ [ từ bỏ               ]  │ │ [Daily Life]              │
│                           │ │                           │
│ Meaning English           │ │                           │
│ [ to leave completely ]  │ │                           │
└───────────────────────────┘ └───────────────────────────┘

Audio URL
[____________________________________________]

Image URL
[____________________________________________]

[Cancel]                             [Save Vocabulary]
```

---

# 16. AI CEFR Suggestion (FUTURE/V2) High-Fidelity

```text
CEFR
[B1 ▼]

✦ AI Suggestion

Suggested:
B2

Confidence/reasoning should not expose raw model internals.

Reason:
Common intermediate business vocabulary.

[Accept] [Edit]
```

Use friendly language rather than:

```text
LLM inference score
```

---

# 17. Topic Management High-Fidelity

Use tree view:

```text
Business
  Finance
    Banking
    Investment
  Marketing

Travel
  Airport
  Hotel
```

Action:

```text
⋮
```

for:

```text
Edit
Deactivate
Add Child
```

---

# 18. Quiz Management High-Fidelity

Table:

```text
Quizzes                         [+ Create Quiz]

Title             Topic       CEFR    Status
Travel A2         Travel      A2      PUBLISHED
Business B1       Business    B1      DRAFT
```

Status is visible without opening detail.

---

# 19. Quiz Editor High-Fidelity

```text
Quiz Editor

Basic Information
────────────────────────────────────
Title
[ Travel Vocabulary A2 ]

Description
[_______________________________]

Topic [Travel ▼]
CEFR  [A2 ▼]

Questions
────────────────────────────────────
1. What does abandon mean?
   [Edit]

2. ...
   [Edit]

[+ Add Question]

[Save Draft]                         [Publish]
```

---

# 20. Quiz Question High-Fidelity

```text
Question text
┌──────────────────────────────────────┐
│ What does "abandon" mean?            │
└──────────────────────────────────────┘

Type
[Multiple Choice ▼]

A [To leave........................]
B [To improve......................]
C [To purchase.....................]
D [To organize.....................]

Correct Answer
[A ▼]

[Cancel]                                  [Save]
```

---

# 21. AI Content List High-Fidelity

```text
AI Content                        [+ Generate Content]

[Pending Review] [Approved] [Rejected]

[Type ▼] [Scope ▼] [Date ▼]

┌────────────┬──────────────┬───────────────┬──────────┐
│ Type       │ Vocabulary   │ Status        │ Action   │
├────────────┼──────────────┼───────────────┼──────────┤
│ EXAMPLE    │ abandon      │ ● PENDING     │ Review   │
│ MNEMONIC   │ negotiate    │ ● PENDING     │ Review   │
└────────────┴──────────────┴───────────────┴──────────┘
```

---

# 22. AI Content Detail High-Fidelity ⭐

This screen must make review decision fast.

```text
AI Generated Content

EXAMPLE
REUSABLE

Vocabulary
abandon

────────────────────────────────────────
Generated Content

“He abandoned the project after the
first month.”

────────────────────────────────────────
Metadata

Model         model-name
Created       2026-08-29
Status        ● REJECTED

Review Note
Incorrect example

Reviewed By   Admin
Reviewed At   15:30
────────────────────────────────────────

[Approve]                     [Reject]
```

For rejected content:

```text
Review Note
```

must be visible directly on this screen.

---

# 23. AI Reject Flow High-Fidelity

Click:

```text
Reject
```

opens modal:

```text
┌──────────────────────────────────────┐
│ Reject AI Content                    │
│                                      │
│ Reason *                             │
│ ┌──────────────────────────────────┐ │
│ │ Incorrect example                │ │
│ └──────────────────────────────────┘ │
│                                      │
│ [Cancel]                 [Reject]    │
└──────────────────────────────────────┘
```

After success:

```text
Status → REJECTED
Review Note → Incorrect example
Reviewed By → Admin
Reviewed At → timestamp
```

---

# 24. AI Content Approval

Approval should use a smaller confirmation:

```text
Approve this AI content?

[Cancel] [Approve]
```

After approval:

```text
Toast:
AI content approved.
```

---

# 25. Learning Statistics High-Fidelity

```text
Learning Statistics

[From] [To]

┌────────────┐ ┌────────────┐ ┌────────────┐ ┌────────────┐
│ Active     │ │ Sessions   │ │ Words      │ │ Accuracy   │
│ 1,284      │ │ 8,420      │ │ 124,560    │ │ 82.4%      │
└────────────┘ └────────────┘ └────────────┘ └────────────┘

Active Users Trend
────────────────────────────────────────
          ╭──╮
      ╭───╯  ╰────╮
  ╭───╯            ╰──
────────────────────────────────────────

Accuracy Trend
────────────────────────────────────────
```

---

# 26. AI Usage High-Fidelity

```text
AI Usage

[From] [To] [Provider ▼] [Model ▼]

Requests       12,420
Tokens         3.8M
Cost           $18.42
Blocked        143
```

Feature table:

```text
Feature                    Requests    Cost
Example Generation         4,200       $4.10
CEFR Suggestion             1,100       $1.20
Personalized Exercise       5,900       $9.80
Explanation                 1,220       $3.32
```

---

# 27. AI Budget High-Fidelity

Normal:

```text
AI Daily Budget

$8.20 / $10.00
████████░░ 82%

NORMAL
```

Warning:

```text
⚠ Approaching daily budget
```

Exceeded:

```text
⚠ Budget exceeded

AI content generation is temporarily disabled.
```

---

# 28. Audit Logs High-Fidelity

```text
Audit Logs

[Admin ▼] [Action ▼] [Target ▼] [Date]

┌──────────────────┬────────┬─────────────┬──────────────────┐
│ Timestamp        │ Admin  │ Action      │ Target           │
├──────────────────┼────────┼─────────────┼──────────────────┤
│ Aug 29, 10:20    │ Admin1 │ REJECT      │ ai_content/123   │
│ Aug 29, 10:15    │ Admin1 │ LOCK_USER   │ user/456         │
│ Aug 29, 09:50    │ Admin2 │ UPDATE_WORD │ vocabulary/789   │
└──────────────────┴────────┴─────────────┴──────────────────┘
```

---

# 29. Audit Detail

```text
Audit Event

Timestamp
2026-08-29 10:20

Admin
Admin1

Action
REJECT

Target
ai_generated_content / uuid

Details
{
  "reason": "Incorrect example"
}
```

Use monospace only for JSON-like details.

---

# 30. Loading High-Fidelity

Table:

```text
████████
████████████
█████████
██████████████
```

Use skeleton rows matching real table geometry.

Dashboard:

```text
Metric skeleton
Chart skeleton
```

---

# 31. Empty High-Fidelity

```text
No users found.

Try changing your filters.
```

AI:

```text
No pending AI content.

You're all caught up.
```

---

# 32. Error High-Fidelity

```text
⚠ Unable to load data.

The server could not complete the request.

[Retry]
```

Do not expose stack traces.

---

# 33. Permission High-Fidelity

```text
403

You don't have permission
to access this page.

[Back]
```

---

# 34. Session Expired High-Fidelity

```text
Your session has expired.

[Sign In]
```

---

# 35. Responsive High-Fidelity

At:

```text
≥1200px
```

use:

```text
full sidebar
multi-column dashboard
full table
```

At:

```text
1024–1199px
```

use:

```text
slightly tighter content
collapsible filters if needed
```

Below:

```text
1024px
```

allow:

```text
sidebar collapse
horizontal table scrolling
wrapped filters
```

---

# 36. Accessibility High-Fidelity

Required:

```text
visible keyboard focus
semantic headings
table headers
form labels
error associations
keyboard modal handling
sufficient contrast
status not color-only
```

---

# 37. Interaction Feedback

Success:

```text
Vocabulary saved successfully.
```

Approve:

```text
AI content approved.
```

Reject:

```text
AI content rejected.
```

Lock:

```text
User locked successfully.
```

Failure:

```text
Could not complete the action.
[Retry]
```

---

# 38. Destructive Action Visual Rules

Destructive controls:

```text
Lock
Deactivate
Reject
```

must:

```text
use danger semantic
avoid huge visual emphasis
require confirmation
```

---

# 39. High-Fidelity Admin Component Library

```text
AppShell
Sidebar
TopBar
PageHeader
SearchBar
FilterBar
DataTable
Pagination
StatusBadge
MetricCard
ChartCard
AlertCard
PrimaryButton
SecondaryButton
DangerButton
ActionMenu
TextField
Select
DatePicker
Modal
Drawer
Toast
Tabs
TreeView
FormSection
```

---

# 40. Admin UI State Matrix

| Component/Page | Default | Loading | Empty | Error | Disabled |
|---|---|---|---|---|---|
| Dashboard | ✓ | ✓ | rare | ✓ | — |
| Table | ✓ | ✓ | ✓ | ✓ | — |
| Form | ✓ | submit | — | ✓ | ✓ |
| AI Review | ✓ | ✓ | ✓ | ✓ | ✓ |
| Modal | ✓ | submit | — | ✓ | ✓ |
| Chart | ✓ | ✓ | ✓ | ✓ | — |

---

# 41. Admin API Binding

```text
Users
→ GET /admin/users
→ GET /admin/users/{id}
→ POST /admin/users/{id}/lock
→ POST /admin/users/{id}/unlock

Vocabulary
→ GET /admin/vocabulary
→ POST /admin/vocabulary
→ PUT /admin/vocabulary/{id}

Topics
→ POST/PUT /admin/topics

Quizzes
→ POST/PUT /admin/quizzes
→ POST /admin/quizzes/{id}/questions

AI
→ GET /admin/ai-content
→ POST /admin/ai-content/generate
→ POST /admin/ai-content/{id}/approve
→ POST /admin/ai-content/{id}/reject

Statistics
→ GET /admin/statistics/learning
→ GET /admin/statistics/ai-usage

Audit
→ GET /admin/audit-logs
```

---

# 42. AI Review Data Binding

The AI Content Detail displays:

```text
contentType
contentScope
vocabulary
generatedContent
modelUsed
status
reviewNote
reviewedBy
reviewedAt
createdAt
```

For reject:

```text
request.reason
→ response.reviewNote
→ DB.review_note
→ audit.details.reason
```

---

# 43. Admin Prototype Priority

The first prototype-ready High-Fidelity screens:

```text
1. Dashboard
2. Users
3. Vocabulary
4. AI Content Review
5. AI Content Detail
6. Learning Statistics
7. AI Usage
8. Audit Logs
```

---

# 44. Admin UX Quality Bar

A good screen should let admin:

```text
Identify the important information
within seconds.
```

A good management flow should:

```text
search
→ inspect
→ act
→ receive confirmation
```

---

# 45. Final Admin Demo Flow

```text
Admin Login
 ↓
Dashboard
 ↓
Vocabulary
 ↓
Add Word
 ↓
AI CEFR Suggestion (FUTURE/V2)
 ↓
Save
 ↓
AI Content
 ↓
Review
 ↓
Approve/Reject
 ↓
Statistics
 ↓
AI Usage
 ↓
Audit Logs
```

---

# 46. Final High-Fidelity Acceptance Criteria

```text
[ ] Sidebar is clear.
[ ] Page hierarchy is clear.
[ ] Primary actions are visible.
[ ] Tables are readable.
[ ] Search/filter/pagination are consistent.
[ ] Forms are visually structured.
[ ] Status is understandable without color alone.
[ ] AI review decision is fast.
[ ] Review Note is visible on rejected content.
[ ] AI cost is visible.
[ ] Budget warning is visible.
[ ] Audit action is traceable.
[ ] Error recovery is obvious.
[ ] Keyboard navigation works.
[ ] Responsive desktop behavior works.
[ ] Visual language differs appropriately from learner app.
```

---

# 47. Final Handoff

```text
Admin Design System + Wireframe
          ↓
Admin High-Fidelity
          ↓
Admin Interactive Prototype
          ↓
Admin UX Review
          ↓
Freeze Admin UI
          ↓
Spring Boot Admin Implementation
```

---

# Reconciled V1 UI Binding

V1 Admin uses manual CEFR selection. Any retained AI CEFR suggestion screen/control in this document is a **FUTURE/V2 prototype only** and must not be enabled in V1 routing or actions. Admin AI generation is synchronous 201/PENDING_REVIEW and AI usage uses `/api/v1/admin/statistics/ai-usage`. All visible V1 copy is Vietnamese and centralized by the Admin technical resource policy.
