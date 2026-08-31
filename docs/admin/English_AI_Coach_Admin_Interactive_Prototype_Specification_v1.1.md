# Admin Interactive Prototype Specification v1.1 — English AI Coach

**Project:** English AI Coach  
**Document:** Admin Interactive Prototype Specification  
**Version:** 1.1  
**Status:** APPROVED BASELINE  
**Date:** 2026-08-31  

**Based on:**
- `English_AI_Coach_SRS_v1.2.md`
- `English_AI_Coach_Database_Schema_v1.6.md`
- `English_AI_Coach_System_Architecture_v1.3.md`
- `English_AI_Coach_AI_Personalization_Specification_v1.3.md`
- `English_AI_Coach_API_Specification_v1.4.md`
- `English_AI_Coach_OpenAPI_Swagger_v1_4.md`
- `English_AI_Coach_Admin_Web_UI_UX_Specification_v1.2.md`
- `English_AI_Coach_Admin_Design_System_Wireframe_v1.0.md`
- `English_AI_Coach_Admin_High_Fidelity_UI_Design_v1.1.md`

---

# 1. Purpose

Interactive Prototype mô phỏng cách administrator sử dụng Admin Web trước khi triển khai thật.

Prototype phải thể hiện:

```text
Admin Login
↓
Dashboard
↓
Users
↓
Vocabulary
↓
Topics
↓
Quizzes
↓
AI Content Review
↓
Learning Statistics
↓
AI Usage
↓
Audit Logs
```

Mục tiêu:

```text
Admin UX
   ↓
High-Fidelity UI
   ↓
Interactive Prototype
   ↓
Admin Usability Review
   ↓
Spring Boot Admin Implementation
```

---

# 2. Prototype Goals

Prototype phải trả lời được:

```text
1. Admin có tìm được chức năng cần dùng không?
2. Admin có hiểu trạng thái hệ thống không?
3. Admin có quản lý vocabulary dễ không?
4. Admin có review AI content dễ không?
5. Admin có hiểu AI usage/cost không?
6. Admin có truy vết được hành động không?
```

---

# 3. Prototype Platform

Recommended:

```text
Figma
```

Prototype dùng:

```text
Mock Data
Prototype Variables
Static JSON where useful
Clickable Components
```

Không cần trong prototype:

```text
Real Spring Boot backend
Real PostgreSQL
Real authentication provider
Real LLM API
Real analytics pipeline
```

---

# 4. Admin Prototype Scope

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
User Detail
Vocabulary Editor
Topics
Quizzes
Quiz Question Editor
AI Usage
Audit Logs
AI Content Detail
```

## P2

```text
Advanced Analytics
Bulk Operations
Role Management
Advanced Moderation
System Settings
```

---

# 5. Global Interaction Model

## Navigation

```text
Click Sidebar Item
→ Navigate to module
```

## Table

```text
Hover row
→ Highlight

Action menu
→ View / Edit / Lock / etc.
```

## Form

```text
Input
→ validation
→ Save
→ loading
→ success/error
```

## Modal

```text
Open
→ Focus first control
→ Confirm / Cancel
```

---

# 6. Global Prototype Variables

Suggested variables:

```text
isAuthenticated
currentAdmin

selectedUser
selectedVocabulary
selectedTopic
selectedQuiz
selectedAiContent

userStatus
vocabularyStatus
quizStatus
aiContentStatus

reviewReason

aiBudgetPercentage
isAiBudgetExceeded

selectedDateRange
selectedFilter
```

---

# 7. Mock Admin Account

```text
name: Admin User
email: admin@example.com
role: ADMIN
status: ACTIVE
```

Prototype credentials are mock-only.

---

# 8. Admin Login Flow

```text
Admin Login
 ↓
Enter email/password
 ↓
Login
 ↓
Dashboard
```

Interactions:

```text
[Login]
→ Dashboard
```

Validation:

```text
Empty email
→ Email is required

Invalid email
→ Enter a valid email

Empty password
→ Password is required
```

---

# 9. Admin Login Wire Interaction

```text
Email
[ admin@example.com ]

Password
[ *************** ]

[ Login ]
```

Success:

```text
→ Dashboard
```

Error:

```text
Invalid credentials.

[Try Again]
```

---

# 10. Dashboard Flow

```text
Login
 ↓
Dashboard
```

Dashboard navigation:

```text
Users
Vocabulary
Topics
Quizzes
AI Content
Statistics
AI Usage
Audit Logs
```

---

# 11. Dashboard Interactive Areas

Metric cards:

```text
Users
→ Users

Active Users
→ Users filtered ACTIVE

Vocabulary
→ Vocabulary

AI Review
→ AI Content / Pending Review

AI Cost
→ AI Usage
```

Charts:

```text
Learning Activity
→ Statistics

AI Usage
→ AI Usage
```

Alerts:

```text
AI Review pending
→ AI Content Pending Review

Budget warning
→ AI Usage
```

---

# 12. Dashboard Prototype Data

```text
Total Users
12,450

Active Users
1,284

Vocabulary
3,240

Pending AI Review
28

Learning Sessions
8,420

AI Cost Today
$18.42

Budget
82%
```

---

# 13. Flow A — User Management

```text
Dashboard
 ↓
Users
 ↓
Search / Filter
 ↓
User Row
 ├── View
 └── Lock
```

---

# 14. Users Search Interaction

```text
[Search users...]
```

Prototype behavior:

```text
Type "nguyen"
→ filtered rows

Clear
→ all rows
```

---

# 15. Users Filter Interaction

```text
[Role ▼]
[Status ▼]
```

Example:

```text
Status = LOCKED
→ show locked users
```

Clear:

```text
Clear filters
→ reset list
```

---

# 16. Users Table Interaction

Columns:

```text
Name
Email
CEFR
Status
Action
```

Row action:

```text
⋮
 ├── View
 └── Lock/Unlock
```

---

# 17. User Detail Flow

```text
Users
 ↓
View
 ↓
User Detail
```

Actions:

```text
Back
Lock
```

Information:

```text
Profile
CEFR
Goal
Daily Time
Learned
Mastered
Accuracy
Streak
Recent Activity
```

---

# 18. Lock User Flow

```text
Users
 ↓
Action
 ↓
Lock User
 ↓
Confirmation Modal
 ↓
Confirm
 ↓
Loading
 ↓
Success
 ↓
User status = LOCKED
```

Modal:

```text
Lock this user?

The user will not be able to access the app.

[Cancel] [Lock User]
```

Success:

```text
User locked successfully.
```

---

# 19. Unlock User Flow

```text
LOCKED
 ↓
⋮
 ↓
Unlock
 ↓
Confirm
 ↓
ACTIVE
```

---

# 20. Flow B — Vocabulary Management

```text
Dashboard
 ↓
Vocabulary
 ↓
Search / Filter
 ↓
Create / Edit
```

---

# 21. Vocabulary Search

```text
[Search vocabulary...]
```

Mock:

```text
abandon
negotiate
purchase
achieve
```

---

# 22. Vocabulary Filter

```text
[CEFR ▼]
[Topic ▼]
[POS ▼]
```

Example:

```text
CEFR = B1
Topic = Business
```

→ filtered table.

---

# 23. Add Vocabulary Flow

```text
Vocabulary
 ↓
+ Add Vocabulary
 ↓
Vocabulary Editor
 ↓
Fill fields
 ↓
Optional AI CEFR Suggestion (FUTURE/V2)
 ↓
Save
 ↓
Success
 ↓
Vocabulary list
```

---

# 24. Vocabulary Editor Interaction

Fields:

```text
Word
IPA
Meaning Vietnamese
Meaning English
Part of Speech
CEFR
Topics
Audio URL
Image URL
```

Validation:

```text
Word required
IPA optional
Meaning required
Part of Speech required
CEFR required
```

---

# 25. AI CEFR Suggestion (FUTURE/V2) Flow ⭐

```text
Vocabulary Editor
 ↓
[Suggest CEFR (FUTURE/V2) with AI]
 ↓
Loading
 ↓
AI Suggestion
 ↓
Accept
or
Edit Manually
```

Suggestion:

```text
Suggested:
B2

Reason:
Common intermediate business vocabulary.
```

---

# 26. AI Suggestion Interaction

Accept:

```text
[Accept]
→ CEFR = B2
→ suggestion closes
```

Edit:

```text
[Edit]
→ close suggestion
→ manual CEFR select
```

Failure:

```text
AI suggestion unavailable.

[Try Again]
```

---

# 27. Duplicate Vocabulary Flow

When duplicate candidate exists:

```text
Save
 ↓
Duplicate detected
 ↓
Modal
```

```text
This vocabulary already exists.

word + part of speech + CEFR

[View Existing] [Cancel]
```

---

# 28. Edit Vocabulary Flow

```text
Vocabulary
 ↓
Edit
 ↓
Load data
 ↓
Edit
 ↓
Save
 ↓
Success
```

Success:

```text
Vocabulary updated successfully.
```

---

# 29. Flow C — Topic Management

```text
Dashboard
 ↓
Topics
 ↓
Topic Tree
```

Actions:

```text
Add Topic
Edit
Deactivate
Add Child
```

---

# 30. Topic Tree Interaction

```text
Business
 ├── Finance
 │   ├── Banking
 │   └── Investment
 └── Marketing
```

Click:

```text
Business
→ expand/collapse
```

Add Child:

```text
Business
→ Add Child
→ Topic Editor
```

---

# 31. Flow D — Quiz Management

```text
Dashboard
 ↓
Quizzes
 ↓
Search / Filter
 ↓
Quiz Editor
```

Actions:

```text
View
Edit
Publish
Unpublish
```

---

# 32. Quiz Create Flow

```text
+ Create Quiz
 ↓
Quiz Editor
 ↓
Basic Information
 ↓
Add Questions
 ↓
Save Draft
 ↓
Publish
```

---

# 33. Question Editor Flow

```text
Quiz Editor
 ↓
+ Add Question
 ↓
Question Editor
 ↓
Enter question
 ↓
Add options
 ↓
Set correct answer
 ↓
Save
```

Prototype supports:

```text
Multiple Choice
Fill Blank
Matching
```

---

# 34. Publish Quiz Flow

```text
Publish
 ↓
Validation
 ↓
Confirmation
 ↓
Published
```

Validation:

```text
Title
Questions
Valid answers
Correct answer
Active vocabulary references
```

Confirmation:

```text
Publish this quiz?

[Cancel] [Publish]
```

---

# 35. Flow E — AI Content ⭐

```text
Dashboard
 ↓
AI Content
 ↓
Pending Review
 ↓
AI Content Detail
```

Tabs:

```text
Pending Review
Approved
Rejected
```

---

# 36. AI Content List Interaction

Filters:

```text
[Type ▼]
[Scope ▼]
[Date ▼]
```

Actions:

```text
Review
```

---

# 37. AI Content Detail Flow

```text
AI Content List
 ↓
Review
 ↓
AI Content Detail
```

Display:

```text
Type
Scope
Vocabulary
Generated Content
Model
Status
Review Note
Reviewed By
Reviewed At
Created At
```

---

# 38. AI Approve Flow

```text
AI Content Detail
 ↓
Approve
 ↓
Confirmation
 ↓
Approve
 ↓
Loading
 ↓
Success
 ↓
Status = APPROVED
```

Confirmation:

```text
Approve this AI content?

[Cancel] [Approve]
```

---

# 39. AI Reject Flow ⭐

```text
AI Content Detail
 ↓
Reject
 ↓
Reject Modal
 ↓
Enter reason
 ↓
Reject
 ↓
Loading
 ↓
Success
 ↓
Status = REJECTED
```

Reject reason:

```text
Required
```

---

# 40. Reject Reason Persistence

Request:

```json
{
  "reason": "Incorrect example"
}
```

Prototype visualization:

```text
reason
 ├──→ AI Content Review Note
 └──→ Audit Detail
```

The actual backend mapping is:

```text
reason
→ ai_generated_content.review_note
+
admin_audit_logs.details.reason
```

---

# 41. Rejected Content Detail

After rejection:

```text
Status
REJECTED

Review Note
Incorrect example

Reviewed By
Admin User

Reviewed At
2026-08-29 15:30
```

This is a key prototype scenario.

---

# 42. Re-open Rejected Content

```text
AI Content
 ↓
Rejected
 ↓
Open Detail
```

Admin can inspect:

```text
Generated content
Review Note
Reviewer
Review time
Audit
```

---

# 43. Personalized Content Boundary

Do not simulate:

```text
Personalized Exercise
→ Pending Review
→ Admin reviews every result
```

Instead:

```text
Personalized Exercise
→ Validation/Safety
→ User
```

Admin Web only reviews reusable AI content.

---

# 44. Flow F — Learning Statistics

```text
Dashboard
 ↓
Statistics
 ↓
Date Filter
 ↓
Charts / Metrics
```

---

# 45. Statistics Interaction

Date:

```text
[From] [To]
```

Change:

```text
Update
→ loading
→ new metrics
```

Charts:

```text
Active Users
Learning Sessions
Words Studied
Accuracy
```

---

# 46. Flow G — AI Usage

```text
Dashboard
 ↓
AI Usage
 ↓
Date/Provider/Model/Feature filters
 ↓
Usage detail
```

Metrics:

```text
Requests
Tokens
Estimated Cost
Blocked Requests
```

---

# 47. AI Usage Filter Interaction

```text
[Provider ▼]
[Model ▼]
[Feature ▼]
[Date]
```

Example:

```text
Feature = Personalized Exercise
```

→ show only usage for that feature.

---

# 48. AI Budget Interaction ⭐

Normal:

```text
82%

NORMAL
```

Warning:

```text
Near threshold
```

Exceeded:

```text
Budget exceeded
```

Prototype behavior:

```text
Toggle simulated budget state

NORMAL
→ WARNING
→ EXCEEDED
```

---

# 49. AI Budget Exceeded State

```text
⚠ AI Daily Budget Exceeded

AI content generation is temporarily disabled.
```

Do not imply that all core learning stops.

Core learning may continue without AI generation if supported by product flow.

---

# 50. Flow H — Audit Logs

```text
Dashboard
 ↓
Audit Logs
 ↓
Search / Filter
 ↓
Audit row
 ↓
Detail
```

---

# 51. Audit Filters

```text
[Admin ▼]
[Action ▼]
[Target ▼]
[Date]
```

Example:

```text
Action = REJECT
```

→ only reject events.

---

# 52. Audit Detail Interaction

```text
Click row
 ↓
Drawer / Detail
```

Display:

```text
Timestamp
Admin
Action
Target
Details
```

Example:

```json
{
  "reason": "Incorrect example"
}
```

---

# 53. Cross-Module Prototype Flow

Most important end-to-end Admin flow:

```text
Dashboard
 ↓
Vocabulary
 ↓
Add Vocabulary
 ↓
Suggest CEFR (FUTURE/V2) with AI
 ↓
Accept
 ↓
Save
 ↓
AI Content
 ↓
Review Example
 ↓
Reject
 ↓
Enter Reason
 ↓
Confirm
 ↓
AI Content Detail
 ↓
See Review Note
 ↓
Audit Logs
 ↓
Open REJECT event
 ↓
See same reason
```

This demonstrates:

```text
CRUD
+
AI assistance
+
Moderation
+
Persistence
+
Auditability
```

---

# 54. Dashboard-to-Module Prototype Flow

```text
Dashboard
 ├── Users → Users
 ├── Vocabulary → Vocabulary
 ├── AI Review → AI Content/Pending
 ├── AI Cost → AI Usage
 └── Learning Activity → Statistics
```

---

# 55. Prototype State Model

Every list:

```text
DEFAULT
LOADING
SUCCESS
EMPTY
ERROR
```

Every mutation:

```text
IDLE
SUBMITTING
SUCCESS
FAILURE
```

Every confirmation:

```text
OPEN
CANCEL
CONFIRM
```

---

# 56. Loading Prototype

Table:

```text
Header skeleton
Skeleton rows
Pagination skeleton
```

Form:

```text
Inputs remain visible
Save button → Loading
```

---

# 57. Empty Prototype

Users:

```text
No users found.

Try changing your search or filters.
```

AI:

```text
No pending AI content.
```

Audit:

```text
No audit events found.
```

---

# 58. Error Prototype

```text
Unable to load data.

[Retry]
```

Save:

```text
Could not save changes.

[Try Again]
```

---

# 59. Permission Prototype

```text
403

You don't have permission
to access this page.

[Back]
```

---

# 60. Session Expired Prototype

```text
Your session has expired.

[Sign In]
```

---

# 61. Responsive Prototype

Desktop:

```text
Sidebar expanded
Full table
Multi-column dashboard
```

Tablet/smaller viewport:

```text
Sidebar collapses
Filters wrap
Tables scroll horizontally
```

Prototype should include at least:

```text
1440px
1024px
```

---

# 62. Keyboard Prototype

Test:

```text
Tab
Shift+Tab
Enter
Escape
Arrow keys where appropriate
```

Modal:

```text
Tab trap
Esc closes
Enter confirms where appropriate
```

---

# 63. Prototype Navigation Map

```text
Admin Login
    ↓
Dashboard
    │
    ├── Users
    │    └── User Detail
    │
    ├── Vocabulary
    │    └── Vocabulary Editor
    │         └── AI CEFR Suggestion (FUTURE/V2)
    │
    ├── Topics
    │    └── Topic Editor
    │
    ├── Quizzes
    │    ├── Quiz Editor
    │    └── Question Editor
    │
    ├── AI Content
    │    └── AI Content Detail
    │         ├── Approve
    │         └── Reject
    │
    ├── Statistics
    │
    ├── AI Usage
    │    └── Budget State
    │
    └── Audit Logs
         └── Audit Detail
```

---

# 64. Prototype Variables — Example States

## AI Content

```text
pending
approved
rejected
```

## User

```text
active
locked
```

## Quiz

```text
draft
published
```

## Budget

```text
normal
warning
exceeded
```

---

# 65. Prototype Data

## Users

```text
Nguyen A
a@example.com
A2
ACTIVE

Tran B
b@example.com
B1
LOCKED
```

## Vocabulary

```text
abandon
B1
verb
Daily Life

negotiate
B2
verb
Business
```

## AI content

```text
EXAMPLE / abandon / PENDING
MNEMONIC / negotiate / PENDING
```

---

# 66. Prototype Demo Script

Recommended 7–10 minute demo:

```text
1. Admin Login
2. Dashboard
3. Open Vocabulary
4. Add "negotiate"
5. AI suggests B2
6. Accept suggestion
7. Save
8. Open AI Content
9. Open generated example
10. Reject
11. Enter "Incorrect example"
12. Confirm
13. Open AI Content Detail
14. Show Review Note
15. Open Audit Logs
16. Find REJECT event
17. Show reason
18. Open AI Usage
19. Show cost/budget
20. Open Statistics
```

---

# 67. Admin AI Story for Presentation

```text
Admin
 ↓
Adds vocabulary
 ↓
AI assists CEFR classification
 ↓
AI generates reusable content
 ↓
Admin reviews
 ↓
Approve/Reject
 ↓
Reason is stored
 ↓
Audit trail
 ↓
System analytics
```

This provides a clear narrative for the AI portion of the Admin Web.

---

# 68. Prototype Evaluation Questions

```text
[ ] Can admin find Vocabulary quickly?
[ ] Can admin find pending AI content quickly?
[ ] Is Reject action obvious?
[ ] Is reject reason clearly required?
[ ] Does Review Note appear after rejection?
[ ] Can admin find the audit event?
[ ] Can admin understand AI cost?
[ ] Can admin interpret budget warning?
[ ] Can admin distinguish learning statistics from AI usage?
[ ] Are table/search/filter patterns consistent?
```

---

# 69. Usability Test Scenarios

## Scenario A — Vocabulary

> “Hãy thêm một từ mới và nhờ AI gợi ý CEFR.”

Success:

```text
Vocabulary → Add → Suggest AI → Accept → Save
```

## Scenario B — AI Review

> “Hãy tìm một AI example đang chờ duyệt và từ chối nó vì nội dung không chính xác.”

Success:

```text
AI Content → Pending → Review → Reject → Reason
```

## Scenario C — Audit

> “Hãy kiểm tra xem ai đã reject nội dung vừa rồi và lý do là gì.”

Success:

```text
Audit Logs → REJECT → Detail
```

## Scenario D — AI Cost

> “Hãy kiểm tra hôm nay AI đã dùng bao nhiêu request và chi phí bao nhiêu.”

Success:

```text
AI Usage → metrics
```

---

# 70. Prototype Acceptance Criteria

```text
[ ] Admin can login.
[ ] Dashboard navigation works.
[ ] Users flow works.
[ ] User detail works.
[ ] Lock/unlock flow works.
[ ] Vocabulary CRUD flow works.
[ ] AI CEFR suggestion flow works.
[ ] Topic flow works.
[ ] Quiz creation/edit flow works.
[ ] Quiz publish flow works.
[ ] AI Content review flow works.
[ ] Reject requires reason.
[ ] Review Note is visible after rejection.
[ ] Audit contains the reject reason.
[ ] Statistics flow works.
[ ] AI Usage flow works.
[ ] Budget warning/exceeded states work.
[ ] Loading state works.
[ ] Empty state works.
[ ] Error state works.
[ ] Permission state works.
[ ] Session-expired state works.
[ ] Responsive states work.
[ ] Keyboard interaction is testable.
```

---

# 71. Developer Handoff

Each prototype screen should annotate:

```text
Screen
Route
Primary API
Secondary API
Loading State
Empty State
Error State
Mutation
Validation
Permission
```

Example:

```text
AI Content Detail

Route:
/admin/ai-content/{contentId}

Load:
GET /admin/ai-content/{contentId}

Approve:
POST /admin/ai-content/{contentId}/approve

Reject:
POST /admin/ai-content/{contentId}/reject

Reject request:
{ "reason": "..." }

Response:
reviewNote
```

---

# 72. API Binding Summary

| Prototype Flow | API |
|---|---|
| Login | `POST /auth/login` |
| Users | `/admin/users` |
| User Detail | `/admin/users/{userId}` |
| Lock | `/admin/users/{userId}/lock` |
| Vocabulary | `/admin/vocabulary` |
| Vocabulary Editor | `/admin/vocabulary/{vocabularyId}` |
| AI CEFR Suggestion (FUTURE/V2) | FUTURE/V2 — no V1 route |
| Topics | `/admin/topics` |
| Quizzes | `/admin/quizzes` |
| Questions | `/admin/quizzes/{quizId}/questions` |
| AI Content | `/admin/ai-content` |
| AI Generate | `/admin/ai-content/generate` |
| Approve | `/admin/ai-content/{contentId}/approve` |
| Reject | `/admin/ai-content/{contentId}/reject` |
| Statistics | `/admin/statistics/learning` |
| AI Usage | `/admin/statistics/ai-usage` |
| Audit | `/admin/audit-logs` |

---

# 73. Prototype Security Boundaries

Never prototype:

```text
Password hash
JWT token display
Refresh token display
LLM API key
Database credentials
```

Admin can view:

```text
AI model name
AI usage metadata
Audit details
```

where permitted by the product design.

---

# 74. Final Prototype Information Architecture

```text
                    DASHBOARD
                        │
          ┌─────────────┼─────────────┐
          ▼             ▼             ▼
       USERS         CONTENT          AI
          │             │             │
       Details      Vocabulary     Review
       Lock         Topics         Usage
                    Quizzes        Budget
                         │
                         ▼
                    STATISTICS
                         │
                         ▼
                       AUDIT
```

---

# 75. Final Admin Prototype Story

```text
MANAGE
   ↓
CREATE
   ↓
AI ASSIST
   ↓
REVIEW
   ↓
APPROVE / REJECT
   ↓
MONITOR
   ↓
AUDIT
```

---

# 76. Final Project Sequence

```text
Admin UI/UX Specification
        ↓
Admin Design System + Wireframe
        ↓
Admin High-Fidelity
        ↓
Admin Interactive Prototype
        ↓
Admin Usability Test
        ↓
Freeze Admin UI
        ↓
Spring Boot Admin API
        ↓
Admin Web Implementation
```

---

# 77. Final Implementation Readiness Criteria

Admin Web is ready for implementation when:

```text
[ ] P0 screens reviewed
[ ] Main admin journey works
[ ] AI content review works
[ ] Reject reason/review note works
[ ] Audit trail is understandable
[ ] AI usage/cost is understandable
[ ] Loading/empty/error states defined
[ ] Permission/session states defined
[ ] Responsive behavior reviewed
[ ] Keyboard accessibility reviewed
[ ] API mapping checked
```

---

# 78. Final Decision

Admin Interactive Prototype V1 is considered complete when the following end-to-end flow is clickable:

```text
Admin Login
 ↓
Dashboard
 ↓
Vocabulary
 ↓
Add Vocabulary
 ↓
AI CEFR Suggestion (FUTURE/V2)
 ↓
Save
 ↓
AI Content
 ↓
Review
 ↓
Reject + Reason
 ↓
Review Note
 ↓
Audit Log
 ↓
AI Usage / Statistics
```

This flow is the recommended core prototype for the project defense/demo.

---

# Reconciled V1 UI Binding

V1 Admin uses manual CEFR selection. Any retained AI CEFR suggestion screen/control in this document is a **FUTURE/V2 prototype only** and must not be enabled in V1 routing or actions. Admin AI generation is synchronous 201/PENDING_REVIEW and AI usage uses `/api/v1/admin/statistics/ai-usage`. All visible V1 copy is Vietnamese and centralized by the Admin technical resource policy.
