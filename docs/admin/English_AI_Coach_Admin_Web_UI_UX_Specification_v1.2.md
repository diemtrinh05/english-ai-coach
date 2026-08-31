# Admin Web UI/UX Specification v1.2 — English AI Coach

**Project:** English AI Coach  
**Document:** Admin Web UI/UX Specification  
**Version:** 1.2  
**Status:** APPROVED BASELINE  
**Date:** 2026-08-31  

**Based on:**
- `English_AI_Coach_SRS_v1.2.md`
- `English_AI_Coach_Database_Schema_v1.6.md`
- `English_AI_Coach_System_Architecture_v1.3.md`
- `English_AI_Coach_AI_Personalization_Specification_v1.3.md`
- `English_AI_Coach_API_Specification_v1.4.md`
- `English_AI_Coach_OpenAPI_Swagger_v1_4.md`
- `English_AI_Coach_App_UI_UX_Specification_v1.1.md`
- `English_AI_Coach_Design_System_Wireframe_v1.2_FULL.md`
- `English_AI_Coach_High_Fidelity_UI_Design_v1.1.md`
- `English_AI_Coach_Interactive_Prototype_Specification_v1.1.md`

---

# 1. Purpose

Tài liệu này đặc tả UX/UI cho **Admin Web** của English AI Coach.

Admin Web phục vụ:

```text
System Administration
Content Management
AI Content Review
Learning Analytics
AI Usage Monitoring
Audit
```

Admin Web không phải giao diện học tập.

```text
Mobile App
→ USER learns

Admin Web
→ ADMIN manages
```

Mục tiêu:

```text
Requirements
   ↓
Admin UX
   ↓
Admin UI
   ↓
Admin Prototype
   ↓
Spring Boot Admin API Integration
```

---

# 2. Admin Product Goals

Admin Web phải giúp admin:

```text
1. Quản lý user
2. Quản lý vocabulary
3. Quản lý topic
4. Quản lý quiz
5. Kiểm duyệt reusable AI content
6. Theo dõi learning statistics
7. Theo dõi AI usage/cost
8. Theo dõi audit logs
```

---

# 3. Admin UX Principles

## 3.1. Information First

Admin thường xử lý:

```text
Table
Filter
Search
Status
Action
Statistics
```

Không dùng visual design quá decorative.

---

## 3.2. Management First

Mỗi màn hình phải giúp admin:

```text
Find
Understand
Act
```

Ví dụ:

```text
Search vocabulary
→ inspect
→ edit
```

---

## 3.3. Safe Actions

Các hành động nguy hiểm:

```text
Lock User
Delete/Deactivate
Reject AI Content
Publish Quiz
```

phải có:

```text
confirmation
```

---

## 3.4. Auditability

Các hành động quan trọng của admin phải có audit trail.

```text
Who
What
Target
When
```

---

## 3.5. Consistency

Các module:

```text
Users
Vocabulary
Topics
Quizzes
AI Content
Statistics
Audit
```

phải dùng cùng:

```text
Search
Filter
Pagination
Table
Status
Action Menu
```

---

# 4. Admin Layout

Desktop-first:

```text
┌─────────────────────────────────────────────────────┐
│ Top Bar                                             │
├────────────────┬────────────────────────────────────┤
│                │                                    │
│ Sidebar        │ Main Content                       │
│                │                                    │
│ Dashboard      │                                    │
│ Users          │                                    │
│ Vocabulary     │                                    │
│ Topics         │                                    │
│ Quizzes        │                                    │
│ AI Content     │                                    │
│ Statistics     │                                    │
│ AI Usage       │                                    │
│ Audit Logs     │                                    │
│                │                                    │
└────────────────┴────────────────────────────────────┘
```

---

# 5. Sidebar Navigation

```text
┌──────────────────────┐
│ English AI Coach     │
│ Admin                │
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

Selected item:

```text
soft primary background
left/side indicator
```

---

# 6. Top Bar

```text
┌─────────────────────────────────────────────────────┐
│ Page Title                            Admin ▼       │
└─────────────────────────────────────────────────────┘
```

Optional:

```text
notifications
profile
logout
```

---

# 7. Admin Design Tokens

## 7.1. Color Roles

```text
primary
surface
background
border
text-primary
text-secondary

success
warning
error
info
```

Admin can use more neutral surfaces than Mobile App.

---

## 7.2. Typography

Recommended:

```text
Page title → 24–28px
Section title → 18–20px
Body → 14–16px
Table → 13–14px
Caption → 12px
```

---

# 8. Spacing

Desktop:

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

Content padding:

```text
24–32px
```

---

# 9. Tables

Standard table:

```text
┌────────────┬─────────────┬────────┬──────────┬────────┐
│ Name       │ Email       │ CEFR   │ Status   │ Action │
├────────────┼─────────────┼────────┼──────────┼────────┤
│ Nguyen A   │ a@gmail.com │ A2     │ ACTIVE   │ ⋮      │
│ Tran B     │ b@gmail.com │ B1     │ LOCKED   │ ⋮      │
└────────────┴─────────────┴────────┴──────────┴────────┘
```

Features:

```text
sorting
pagination
row action
bulk selection where useful
```

---

# 10. Search

```text
[ Search users...                         🔍 ]
```

Search should support:

```text
debounced input
clear
loading
empty
```

---

# 11. Filters

Use horizontal filter bar:

```text
[Role ▼] [Status ▼] [CEFR ▼] [Topic ▼]
```

Reset:

```text
Clear filters
```

---

# 12. Pagination

Standard:

```text
Rows per page: 20

< 1 2 3 4 ... 12 >
```

Pagination should correspond to API response:

```text
content
page
size
totalElements
totalPages
hasNext
```

---

# 13. Status Badge

Examples:

```text
ACTIVE
LOCKED

DRAFT
PUBLISHED

PENDING_REVIEW
APPROVED
REJECTED
```

Use:

```text
text
+
semantic icon/color
```

Color must not be the only signal.

---

# 14. Dashboard

## Goal

Admin must understand system health in seconds.

Recommended cards:

```text
Total Users
Active Users
Vocabulary
Pending AI Review
Learning Sessions
AI Cost Today
```

Example:

```text
┌──────────────┐ ┌──────────────┐ ┌──────────────┐
│ Users        │ │ Active       │ │ Vocabulary   │
│ 12,450       │ │ 1,284        │ │ 3,240        │
└──────────────┘ └──────────────┘ └──────────────┘

┌──────────────┐ ┌──────────────┐ ┌──────────────┐
│ AI Review    │ │ Sessions     │ │ AI Cost      │
│ 28 pending   │ │ 8,420        │ │ $18.42       │
└──────────────┘ └──────────────┘ └──────────────┘
```

---

# 15. Dashboard Charts

Recommended:

```text
Active Users Trend
Learning Sessions
Accuracy Trend
AI Cost Trend
```

Avoid:

```text
10+ charts
```

---

# 16. Dashboard Alerts

Example:

```text
⚠ 28 AI content items awaiting review

⚠ AI daily budget at 82%
```

Click:

```text
Review
→ corresponding module
```

---

# 17. Dashboard Wireframe

```text
┌──────────────────────────────────────────────────────────┐
│ Dashboard                                                │
│                                                          │
│ ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐             │
│ │ Users  │ │ Active │ │ Words  │ │ AI Rev │             │
│ │ 12,450 │ │ 1,284  │ │ 3,240  │ │   28   │             │
│ └────────┘ └────────┘ └────────┘ └────────┘             │
│                                                          │
│ ┌──────────────────────────────┐ ┌─────────────────────┐ │
│ │ Learning Activity            │ │ AI Usage            │ │
│ │                              │ │                     │ │
│ │        ╱╲     ╱╲            │ │ Cost: $18.42        │ │
│ │    ╱───╯ ╲───╯ ╲──          │ │ 82% budget          │ │
│ │                              │ │                     │ │
│ └──────────────────────────────┘ └─────────────────────┘ │
│                                                          │
│ Alerts                                                   │
│ ⚠ 28 AI items pending review                             │
│ ⚠ Daily AI budget at 82%                                 │
└──────────────────────────────────────────────────────────┘
```

---

# 18. User Management

Route:

```text
/admin/users
```

Functions:

```text
Search
Filter
Pagination
View
Lock
Unlock
```

---

# 19. User List Wireframe

```text
┌──────────────────────────────────────────────────────────┐
│ Users                                      [+ optional] │
│                                                          │
│ [Search users...] [Role ▼] [Status ▼]                  │
│                                                          │
│ ┌─────────┬─────────────────┬─────┬─────────┬────────┐  │
│ │ Name    │ Email           │CEFR │ Status  │ Action │  │
│ ├─────────┼─────────────────┼─────┼─────────┼────────┤  │
│ │ Nguyen  │ a@example.com   │ A2  │ ACTIVE  │ ⋮      │  │
│ │ Tran    │ b@example.com   │ B1  │ LOCKED  │ ⋮      │  │
│ └─────────┴─────────────────┴─────┴─────────┴────────┘  │
│                                                          │
│                    < 1 2 3 ... >                         │
└──────────────────────────────────────────────────────────┘
```

---

# 20. User Detail

```text
┌──────────────────────────────────────────────────────────┐
│ ← User Detail                                            │
│                                                          │
│ Nguyen A                         ACTIVE                  │
│ a@example.com                    USER                    │
│                                                          │
│ CEFR                         A2                          │
│ Primary Goal                Travel                      │
│ Daily Time                  20 min                      │
│                                                          │
│ ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐             │
│ │ Learned│ │Mastered│ │Accuracy│ │Streak  │             │
│ │ 320    │ │ 140    │ │ 84.5%  │ │ 12     │             │
│ └────────┘ └────────┘ └────────┘ └────────┘             │
│                                                          │
│ Recent Activity                                          │
│ ...                                                      │
│                                                          │
│ [Lock User]                                              │
└──────────────────────────────────────────────────────────┘
```

---

# 21. Lock User Confirmation

```text
Lock this user?

The user will not be able to access the app.

[Cancel] [Lock User]
```

---

# 22. Vocabulary Management

Route:

```text
/admin/vocabulary
```

Functions:

```text
Search
Filter
Pagination
Create
Edit
Activate
Deactivate
```

---

# 23. Vocabulary List

```text
┌────────────┬────────┬─────┬──────────────┬────────┐
│ Word       │ POS    │CEFR │ Topic        │ Action │
├────────────┼────────┼─────┼──────────────┼────────┤
│ abandon    │ verb   │ B1  │ Daily Life   │ ⋮      │
│ negotiate  │ verb   │ B2  │ Business     │ ⋮      │
└────────────┴────────┴─────┴──────────────┴────────┘
```

---

# 24. Vocabulary Editor

Fields:

```text
Word
Phonetic IPA
Meaning Vietnamese
Meaning English
Part of Speech
CEFR
Topics
Audio URL
Image URL
```

Actions:

```text
Save
Cancel
```

---

# 25. AI CEFR Suggestion (FUTURE/V2)

Button:

```text
[Suggest CEFR (FUTURE/V2) with AI]
```

Result:

```text
AI Suggested CEFR
B2

Reason
Common intermediate business vocabulary.

[Accept Suggestion]
[Edit Manually]
```

The final admin action remains authoritative.

---

# 26. Duplicate Vocabulary Validation

If duplicate candidate exists:

```text
This vocabulary already exists.

word + part of speech + CEFR
```

Actions:

```text
View existing
Cancel
```

---

# 27. Topic Management

Route:

```text
/admin/topics
```

Support:

```text
List
Create
Edit
Activate
Deactivate
Hierarchy
```

Wireframe:

```text
Topics

[+ Add Topic]

Business
├── Finance
│   ├── Banking
│   └── Investment
└── Marketing

Travel
├── Airport
└── Hotel
```

---

# 28. Topic Editor

```text
Name
Description
Icon URL
Parent Topic
```

Validation:

```text
Name required
Parent cannot equal itself
```

---

# 29. Quiz Management

Route:

```text
/admin/quizzes
```

List:

```text
Title
Topic
CEFR
Status
Updated
Action
```

Actions:

```text
Edit
View
Publish
Unpublish
```

---

# 30. Quiz Editor

Sections:

```text
Basic Information
Questions
Publishing
```

---

# 31. Quiz Question Editor

```text
Question:
What does "abandon" mean?

Type:
Multiple Choice

A. To leave
B. To buy
C. To improve
D. To organize

Correct answer:
A

[Save]
```

Admin can see the correct answer.

Learner must not receive the correct answer before submission.

---

# 32. Publish Quiz

Validation before publish:

```text
Title present
At least one question
Every question valid
Correct answers set
All referenced vocabulary active
```

Confirmation:

```text
Publish this quiz?

[Cancel] [Publish]
```

---

# 33. AI Content Management ⭐

Route:

```text
/admin/ai-content
```

Main states:

```text
PENDING_REVIEW
APPROVED
REJECTED
```

Filters:

```text
Content Type
Status
Scope
Date
```

---

# 34. AI Content List

```text
┌────────────┬──────────────┬──────────────┬─────────┐
│ Type       │ Vocabulary   │ Status       │ Action  │
├────────────┼──────────────┼──────────────┼─────────┤
│ EXAMPLE    │ abandon      │ PENDING      │ Review  │
│ MNEMONIC   │ negotiate    │ PENDING      │ Review  │
└────────────┴──────────────┴──────────────┴─────────┘
```

---

# 35. AI Content Detail

```text
AI Generated Content

Type:
EXAMPLE

Vocabulary:
abandon

Generated content:
"He abandoned the project..."

Model:
[model name]

Status:
PENDING_REVIEW

Review Note:
—

Reviewed By:
—

Reviewed At:
—

Created:
...

[Approve]
[Reject]
```

Do not expose raw prompt to normal learner accounts.

Admin may inspect prompt metadata if the implementation permits it.

---

# 36. AI Content Review

Approve:

```text
Approve this content?

[Cancel] [Approve]
```

Reject:

```text
Reject reason
[________________________]

[Cancel] [Reject]
```

Persistence:

```text
reason
 ↓
ai_generated_content.review_note
 +
admin_audit_logs.details.reason
```

After rejection, AI Content Detail displays:

```text
Status: REJECTED
Review Note: <reason>
Reviewed By: <admin>
Reviewed At: <timestamp>
```

---

# 37. AI Content Rule

Reusable:

```text
Example
Explanation
Mnemonic
Story
Reusable Quiz Content
```

Flow:

```text
LLM
 ↓
Validation
 ↓
PENDING_REVIEW
 ↓
Admin Review
 ↓
APPROVED
 ↓
User
```

Personalized ephemeral:

```text
Personalized Exercise
```

Flow:

```text
Personalization
 ↓
LLM
 ↓
Validation
 ↓
Safety Filter
 ↓
User
```

Personalized exercises are not manually reviewed one by one.

---

# 38. Learning Statistics

Route:

```text
/admin/statistics/learning
```

Filters:

```text
From
To
```

Metrics:

```text
Active Users
Learning Sessions
Words Studied
Average Accuracy
Learning Minutes
```

---

# 39. Learning Statistics Wireframe

```text
┌──────────────────────────────────────────────────────────┐
│ Learning Statistics                                      │
│ [From] [To]                                              │
│                                                          │
│ ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐             │
│ │ Active │ │Sessions│ │ Words  │ │Accuracy│             │
│ │ 1,284  │ │ 8,420  │ │124,560 │ │ 82.4%  │             │
│ └────────┘ └────────┘ └────────┘ └────────┘             │
│                                                          │
│ Active Users Trend                                       │
│       ╱╲       ╱╲                                       │
│  ╱───╯  ╲─────╯  ╲                                     │
│                                                          │
│ Accuracy Trend                                           │
│ ─────╱╲────╲────                                      │
└──────────────────────────────────────────────────────────┘
```

---

# 40. AI Usage / Cost

Route:

```text
/admin/statistics/ai-usage
```

Metrics:

```text
Requests
Tokens
Estimated Cost
Blocked Requests
```

Breakdown:

```text
Feature
Provider
Model
Date
```

---

# 41. AI Usage Wireframe

```text
┌──────────────────────────────────────────────────────────┐
│ AI Usage                                                 │
│ [From] [To] [Provider ▼] [Model ▼]                     │
│                                                          │
│ ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐             │
│ │Requests│ │ Tokens │ │ Cost   │ │Blocked │             │
│ │ 12,420 │ │ 3.8M   │ │ $18.42 │ │  143   │             │
│ └────────┘ └────────┘ └────────┘ └────────┘             │
│                                                          │
│ Cost by Feature                                          │
│ Example Generation                  $4.10               │
│ CEFR Suggestion                      $1.20               │
│ Personalized Exercise               $9.80               │
│ Explanation                         $3.32                │
└──────────────────────────────────────────────────────────┘
```

---

# 42. AI Budget Alert

Normal:

```text
AI Daily Budget

$8.20 / $10.00

████████░░ 82%

Status: NORMAL
```

Warning:

```text
⚠ AI daily budget near limit
```

Exceeded:

```text
⚠ AI daily budget exceeded

AI content generation has been temporarily disabled.
```

---

# 43. Audit Logs

Route:

```text
/admin/audit-logs
```

Columns:

```text
Timestamp
Admin
Action
Target Table
Target ID
```

Filters:

```text
Admin
Action
Target
Date
```

---

# 44. Audit Log Wireframe

```text
┌───────────────┬────────┬────────────────┬────────────┐
│ Timestamp     │ Admin  │ Action         │ Target     │
├───────────────┼────────┼────────────────┼────────────┤
│ 10:20         │ Admin1 │ APPROVE_AI     │ example123 │
│ 10:15         │ Admin1 │ LOCK_USER      │ user123    │
│ 09:50         │ Admin2 │ UPDATE_WORD    │ abandon    │
└───────────────┴────────┴────────────────┴────────────┘
```

---

# 45. Admin Confirmation Rules

Require confirmation for:

```text
Lock user
Unlock user if applicable
Deactivate vocabulary
Deactivate topic
Publish quiz
Unpublish quiz
Approve AI content
Reject AI content
```

Use destructive styling only where necessary.

---

# 46. Admin Loading States

Table:

```text
Header
Skeleton rows
Pagination placeholder
```

Dashboard:

```text
Metric skeleton
Chart skeleton
```

---

# 47. Admin Empty States

Users:

```text
No users found.
Try changing your search or filters.
```

Vocabulary:

```text
No vocabulary found.
[Add Vocabulary]
```

AI:

```text
No pending AI content.
```

---

# 48. Admin Error States

```text
Unable to load data.

[Retry]
```

Save failure:

```text
Changes could not be saved.

[Try Again]
```

---

# 49. Admin Permission States

Unauthorized:

```text
403

You don't have permission to access this page.
```

Session expired:

```text
Your session has expired.

[Sign In]
```

---

# 50. Admin Responsive Strategy

Desktop-first.

Primary:

```text
≥ 1200px
```

Support:

```text
1024px+
```

For small screens:

```text
Sidebar collapses
Tables can horizontally scroll
Actions remain accessible
```

Admin Web does not need to reproduce mobile App layout.

---

# 51. Admin Accessibility

```text
Keyboard navigation
Visible focus
Readable contrast
Semantic table headers
ARIA labels where applicable
Non-color status indicators
Confirm dialogs accessible
```

---

# 52. Admin Security UX

Do not expose:

```text
JWT
Refresh Token
Password Hash
AI API Key
Database credentials
```

Sensitive actions should use confirmation.

---

# 53. Admin API Mapping

| Admin Screen | API |
|---|---|
| Dashboard | `/admin/statistics/learning`, `/admin/statistics/ai-usage` |
| Users | `/admin/users` |
| User Detail | `/admin/users/{userId}` |
| Lock User | `/admin/users/{userId}/lock` |
| Unlock User | `/admin/users/{userId}/unlock` |
| Vocabulary | `/admin/vocabulary` |
| Vocabulary Edit | `/admin/vocabulary/{vocabularyId}` |
| Topics | `/admin/topics` |
| Quizzes | `/admin/quizzes` |
| Quiz Questions | `/admin/quizzes/{quizId}/questions` |
| AI Content | `/admin/ai-content` |
| Generate AI Content | `/admin/ai-content/generate` |
| Approve AI | `/admin/ai-content/{contentId}/approve` |
| Reject AI | `/admin/ai-content/{contentId}/reject` |
| Learning Stats | `/admin/statistics/learning` |
| AI Usage | `/admin/statistics/ai-usage` |
| Audit | `/admin/audit-logs` |

---

# 54. Admin Information Architecture

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
│   └── AI Content Review
│
├── Statistics
│
├── AI Usage
│
└── Audit Logs
```

---

# 55. Admin Prototype Happy Path

Recommended demo:

```text
Admin Login
 ↓
Dashboard
 ↓
Vocabulary
 ↓
Add vocabulary
 ↓
Suggest CEFR (FUTURE/V2) with AI
 ↓
Save
 ↓
AI Content
 ↓
Review generated example
 ↓
Approve
 ↓
Statistics
 ↓
AI Usage
 ↓
Audit Logs
```

This demonstrates:

```text
Content Management
+
AI Assistance
+
Content Review
+
Analytics
+
Auditability
```

---

# 56. Admin AI Demo Story

```text
Admin adds vocabulary
        ↓
AI suggests CEFR
        ↓
Admin approves/edits CEFR
        ↓
AI generates reusable example
        ↓
Validation
        ↓
Pending Review
        ↓
Admin reviews
        ↓
Approve
        ↓
Content becomes available
```

This is the cleanest AI-admin demonstration.

---

# 57. Admin UX Acceptance Criteria

```text
[ ] Admin can login.
[ ] Admin sees Dashboard.
[ ] Admin can navigate every major module.
[ ] Users support search/filter/pagination.
[ ] Admin can view user details.
[ ] Admin can lock/unlock users.
[ ] Vocabulary supports CRUD/activate/deactivate.
[ ] Vocabulary supports AI CEFR suggestion.
[ ] Topics support hierarchy.
[ ] Quizzes support questions and publish workflow.
[ ] Correct answers are visible only to admin interfaces.
[ ] AI reusable content supports Pending Review.
[ ] Admin can approve/reject reusable AI content.
[ ] Personalized Exercise is not placed into manual review queue.
[ ] Learning Statistics are readable.
[ ] AI Usage shows requests/tokens/cost.
[ ] AI budget warning is visible.
[ ] Audit Logs are searchable/filterable.
[ ] Destructive actions require confirmation.
[ ] Error/empty/loading states exist.
[ ] Admin screens are keyboard accessible.
```

---

# 58. Admin Design Priority

## P0

```text
Admin Login
Dashboard
Users
Vocabulary
AI Content Review
Learning Statistics
```

## P1

```text
Topics
Quizzes
AI Usage
Audit Logs
User Detail
Vocabulary Editor
Quiz Question Editor
```

## P2

```text
Advanced analytics
Bulk content operations
Advanced moderation
Role management
System configuration
```

---

# 59. Admin vs Mobile Design Boundary

Shared:

```text
Brand
Semantic colors
Typography family
Icon style
Status meaning
Accessibility principles
```

Different:

```text
Mobile
→ learning-first
→ card-first
→ minimal information

Admin
→ table-first
→ filter-first
→ information-dense
→ action-oriented
```

Do not force one layout system onto both products.

---

# 60. Final Admin User Journey

```text
LOGIN
  ↓
DASHBOARD
  ↓
┌───────────────┬───────────────┬───────────────┐
│ USERS         │ CONTENT       │ AI            │
│               │               │               │
│ Manage        │ Vocabulary    │ AI Review     │
│ users         │ Topics        │ AI Usage      │
│               │ Quizzes       │ AI Budget     │
└───────────────┴───────────────┴───────────────┘
  ↓
STATISTICS
  ↓
AUDIT
```

---

# 61. Final Admin Product Story

Admin Web phải thể hiện:

```text
Manage Users
      ↓
Manage Learning Content
      ↓
Use AI to assist content operations
      ↓
Review reusable AI content
      ↓
Monitor learning outcomes
      ↓
Monitor AI cost
      ↓
Audit system actions
```

---

# 62. Development Sequence

```text
Admin UX Specification
        ↓
Admin Design System
        ↓
Admin Wireframe
        ↓
Admin High-Fidelity UI
        ↓
Admin Interactive Prototype
        ↓
Spring Boot Admin API
        ↓
Admin Web implementation
```

---

# 63. Final Project UI Architecture

```text
                    ENGLISH AI COACH
                           │
              ┌────────────┴────────────┐
              │                         │
         MOBILE APP                 ADMIN WEB
              │                         │
          USER UX                   ADMIN UX
              │                         │
       Learn / Review             Manage / Monitor
       Personalize                Review AI
       Progress                   Statistics
                                  Audit
```

---

# 64. Final Documentation Chain

```text
SRS
 ↓
Database Schema
 ↓
System Architecture
 ↓
AI Personalization
 ↓
API Specification
 ↓
OpenAPI / Swagger
 ↓
Mobile UI/UX
 ↓
Mobile Design System
 ↓
Mobile High-Fidelity
 ↓
Mobile Interactive Prototype
 ↓
Admin Web UI/UX
 ↓
NEXT:
Admin Design System + Wireframe
 ↓
Admin High-Fidelity UI
 ↓
Admin Interactive Prototype
 ↓
Implementation
```

---

# 65. Final Recommendation

Thiết kế Admin Web nên ưu tiên 5 màn hình:

```text
1. Dashboard
2. Users
3. Vocabulary
4. AI Content Review
5. Statistics / AI Usage
```

Đây là nhóm màn hình vừa có giá trị thực tế vừa thể hiện rõ nhất:

```text
Backend
Database
AI
Content Management
Analytics
Security
```


---

# 66. AI Review Note — Final V1.1 Rule

`Reject reason` is a first-class review field.

## UI

Admin enters:

```text
Reject reason
```

## API

Request:

```json
{
  "reason": "Incorrect example"
}
```

## Database

```text
ai_generated_content.review_note
```

## Audit

```text
admin_audit_logs.details.reason
```

## Detail screen

Rejected content must show:

```text
Status
REJECTED

Review Note
Incorrect example

Reviewed By
Admin

Reviewed At
2026-08-29 15:30
```

This prevents a rejected AI record from losing the explanation for the rejection.

---

# 67. Admin AI Content Acceptance Criteria — Updated

```text
[ ] Admin can enter a reject reason.
[ ] Reject reason is required.
[ ] Backend persists the reason as ai_generated_content.review_note.
[ ] Backend writes the same reason to admin_audit_logs.details.reason.
[ ] AI Content Detail displays Review Note.
[ ] Approved content may show Review Note as empty/null.
[ ] Reject action updates reviewed_by and reviewed_at.
[ ] Reject action is auditable.
```

---

# Reconciled V1 UI Binding

V1 Admin uses manual CEFR selection. Any retained AI CEFR suggestion screen/control in this document is a **FUTURE/V2 prototype only** and must not be enabled in V1 routing or actions. Admin AI generation is synchronous 201/PENDING_REVIEW and AI usage uses `/api/v1/admin/statistics/ai-usage`. All visible V1 copy is Vietnamese and centralized by the Admin technical resource policy.
