# Admin Web Technical Specification v1.1 — English AI Coach

**Project:** English AI Coach  
**Document:** Admin Web Technical Specification  
**Version:** 1.1  
**Status:** APPROVED BASELINE  
**Date:** 2026-08-31  

**Purpose:** Đặc tả kỹ thuật cho Admin Web quản trị English AI Coach, sử dụng cùng Spring Boot Backend với Mobile/Flutter nhưng có frontend riêng.

---

# 1. Scope

Admin Web chịu trách nhiệm:

```text
Authentication
User Management
Vocabulary Management
Topic Management
Quiz Management
AI Content Review
Learning Statistics
AI Usage
Audit Logs
```

Admin Web không truy cập PostgreSQL trực tiếp.

```text
Admin Web
    ↓ HTTPS/JSON
Spring Boot API
    ↓
PostgreSQL
```

---

# 2. Official Technology Stack

```text
React
TypeScript
Vite

UI:
Material UI or equivalent

HTTP:
Axios or fetch abstraction

Server state:
TanStack Query or equivalent

Routing:
React Router

Charts:
project-approved chart library
```

The baseline project technical specification identifies React + TypeScript, Vite, Material UI/equivalent and Axios/fetch as the Admin Web technology direction. 

---

# 3. Architecture

Recommended:

```text
Page
 ↓
Feature Component
 ↓
Query/Mutation Hook
 ↓
API Service
 ↓
HTTP Client
 ↓
Spring Boot
```

Shared:

```text
types
api errors
auth
design tokens
UI components
```

---

# 4. Project Structure

```text
src/
├── app/
│   ├── App.tsx
│   ├── router/
│   ├── providers/
│   └── theme/
│
├── components/
│   ├── layout/
│   ├── table/
│   ├── form/
│   ├── feedback/
│   ├── dialog/
│   └── charts/
│
├── features/
│   ├── auth/
│   ├── dashboard/
│   ├── users/
│   ├── vocabulary/
│   ├── topics/
│   ├── quizzes/
│   ├── ai-content/
│   ├── statistics/
│   ├── ai-usage/
│   └── audit/
│
├── services/
│   ├── api/
│   ├── auth/
│   └── storage/
│
├── hooks/
├── types/
├── utils/
├── constants/
└── main.tsx
```

---

# 5. Application Shell

```text
App
 ├── Auth Guard
 ├── Admin Layout
 │   ├── Sidebar
 │   ├── Topbar
 │   └── Page Outlet
```

Desktop-first.

---

# 6. Route Structure

Recommended:

```text
/login

/admin
/admin/dashboard
/admin/users
/admin/users/:userId
/admin/vocabulary
/admin/vocabulary/new
/admin/vocabulary/:id/edit
/admin/topics
/admin/quizzes
/admin/quizzes/new
/admin/quizzes/:id/edit
/admin/ai-content
/admin/ai-content/:id
/admin/statistics
/admin/ai-usage
/admin/audit-logs
```

---

# 7. Route Guard

```text
Public
→ /login

Authenticated ADMIN
→ /admin/*

Authenticated non-admin
→ 403

Expired session
→ /login
```

Security must be enforced by backend as well; hiding routes in the frontend is not a security mechanism.

---

# 8. Admin Authentication

Flow:

```text
Login
 ↓
POST /api/v1/auth/login
 ↓
receive tokens
 ↓
secure session
 ↓
Admin role check
 ↓
Dashboard
```

---

# 9. Admin Token Handling

Use the same backend JWT contract.

Responsibilities:

```text
attach access token
refresh on 401
serialize concurrent refresh
logout on failure
```

Do not store secrets in source code.

---

# 10. Admin Role

Backend claim:

```text
role = ADMIN
```

Frontend route protection is convenience only.

Backend:

```text
/admin/**
→ ADMIN authorization
```

---

# 11. HTTP Client

Create one centralized client:

```text
apiClient
```

Responsibilities:

```text
base URL
authorization
timeouts
error normalization
refresh
request ID if supported
```

Feature services call this client.

---

# 12. API Service Pattern

Example:

```text
services/api/userService.ts
services/api/vocabularyService.ts
services/api/topicService.ts
services/api/quizService.ts
services/api/aiContentService.ts
services/api/statisticsService.ts
services/api/auditService.ts
```

Do not call raw Axios/fetch directly from page components.

---

# 13. TypeScript API Types

Keep API contracts typed:

```text
UserResponse
VocabularyResponse
TopicResponse
QuizResponse
AiGeneratedContentResponse
LearningStatisticsResponse
AiUsageResponse
AuditLogResponse
ErrorResponse
PaginatedResponse<T>
```

---

# 14. Query/Mutation Strategy

Use server-state library pattern:

```text
Query
→ GET/read

Mutation
→ POST/PUT/PATCH/DELETE
```

After mutation:

```text
invalidate affected query
or
update cache explicitly
```

---

# 15. Standard List Page Pattern

All major list screens:

```text
Page Header
 ↓
Search / Filter
 ↓
Table
 ↓
Pagination
```

Applies to:

```text
Users
Vocabulary
Quizzes
AI Content
Audit Logs
```

---

# 16. Pagination

Server paginated response:

```json
{
  "content": [],
  "page": 0,
  "size": 20,
  "totalElements": 100,
  "totalPages": 5,
  "hasNext": true
}
```

Frontend must not implement fake pagination over unbounded backend data.

---

# 17. Search

Recommended:

```text
debounce 250–400ms
cancel obsolete request
preserve current filters
```

---

# 18. Filter Model

Filters should be represented in URL/query state where useful.

Examples:

```text
/users?status=ACTIVE&page=0
/vocabulary?cefr=B1&topic=Business
/ai-content?status=PENDING_REVIEW
```

This supports:

```text
refresh
deep link
browser navigation
```

---

# 19. Dashboard

Components:

```text
MetricCard
ChartCard
AlertCard
QuickLink
```

Primary metrics:

```text
Total Users
Active Users
Vocabulary
Pending AI Review
Learning Sessions
AI Cost
```

---

# 20. Dashboard Navigation

Clickable metrics:

```text
Users
→ Users

Vocabulary
→ Vocabulary

Pending AI Review
→ AI Content/Pending

AI Cost
→ AI Usage

Learning Activity
→ Statistics
```

---

# 21. User Management

Route:

```text
/admin/users
```

Features:

```text
search
role filter
status filter
pagination
view
lock
unlock
```

---

# 22. User Detail

Display:

```text
name
email
status
role
CEFR
goal
daily learning time
words learned
mastered
accuracy
streak
recent activity
```

Admin should not arbitrarily mutate learning history in V1.

---

# 23. Lock/Unlock

Flow:

```text
click action
 ↓
confirmation
 ↓
mutation
 ↓
invalidate user query
 ↓
toast
```

Lock:

```text
POST /api/v1/admin/users/{userId}/lock
```

Unlock:

```text
POST /api/v1/admin/users/{userId}/unlock
```

---

# 24. Vocabulary Management

Route:

```text
/admin/vocabulary
```

Features:

```text
search
CEFR filter
topic filter
POS filter
pagination
create
edit
deactivate
```

---

# 25. Vocabulary Editor

Fields:

```text
word
phonetic IPA
meaning VI
meaning EN
part of speech
CEFR
topics
audio URL
image URL
```

Client validates required fields.

Backend validates again.

---

# 26. FUTURE/V2 — AI CEFR Suggestion

Flow:

```text
Vocabulary Editor
 ↓
Suggest CEFR (FUTURE/V2)
 ↓
loading
 ↓
AI suggestion
 ↓
Accept or Edit
 ↓
Save
```

AI suggestion is advisory.

Admin remains authoritative.

---

# 27. Topic Management

Route:

```text
/admin/topics
```

Support:

```text
tree
create
edit
add child
deactivate
```

Prevent invalid hierarchy.

---

# 28. Quiz Management

Route:

```text
/admin/quizzes
```

Features:

```text
search
filter
pagination
create
edit
publish
unpublish
```

---

# 29. Quiz Editor

Sections:

```text
Basic Information
Questions
Publishing
```

Question data:

```text
questionText
questionType
options
correctAnswer
vocabularyId
```

Correct answers are admin-only.

---

# 30. Publish Validation

Before publish:

```text
title valid
questions > 0
all questions valid
correct answers configured
referenced content active
```

Invalid publish:

```text
show validation errors
do not publish
```

---

# 31. AI Content Management

Route:

```text
/admin/ai-content
```

Tabs:

```text
Pending Review
Approved
Rejected
```

Filters:

```text
type
scope
status
date
```

---

# 32. AI Content Detail

Display:

```text
content type
content scope
vocabulary/topic
generated content
model
status
review note
reviewed by
reviewed at
created at
```

---

# 33. AI Content Approval

Flow:

```text
Approve
 ↓
confirmation
 ↓
POST /api/v1/admin/ai-content/{contentId}/approve
 ↓
success
 ↓
refresh
```

---

# 34. AI Content Rejection

Flow:

```text
Reject
 ↓
modal
 ↓
reason required
 ↓
POST /api/v1/admin/ai-content/{contentId}/reject
 ↓
status = REJECTED
reviewNote = reason
 ↓
audit record
```

---

# 35. Rejection Persistence

API request:

```json
{
  "reason": "Incorrect example"
}
```

Backend persists:

```text
ai_generated_content.review_note
```

and:

```text
admin_audit_logs.details.reason
```

This preserves both current review state and historical audit evidence.

---

# 36. Personalized AI Content

Do not send personalized ephemeral exercise results through manual review queue.

Flow:

```text
Personalization
 ↓
LLM
 ↓
Validation
 ↓
Safety
 ↓
User
```

Admin Web reviews reusable content only.

---

# 37. Learning Statistics

Route:

```text
/admin/statistics
```

Metrics:

```text
active users
sessions
words studied
accuracy
learning minutes
```

Filters:

```text
date range
```

---

# 38. Statistics Query Strategy

Use:

```text
server aggregation
typed response
```

Frontend displays:

```text
cards
line charts
bar charts
```

Do not fetch raw learning rows just to calculate totals in browser.

---

# 39. AI Usage

Route:

```text
/admin/ai-usage
```

Metrics:

```text
requests
tokens
estimated cost
blocked requests
```

Filters:

```text
date
provider
model
feature
```

---

# 40. AI Budget

Display:

```text
used / daily limit
percentage
status
```

States:

```text
NORMAL
WARNING
EXCEEDED
```

When exceeded:

```text
show alert
```

Do not let frontend bypass budget guard.

---

# 41. Audit Logs

Route:

```text
/admin/audit-logs
```

Columns:

```text
timestamp
admin
action
target
```

Filters:

```text
admin
action
target
date
```

---

# 42. Audit Detail

Display:

```text
timestamp
admin
action
target table
target id
details
```

JSON details may be rendered in a read-only formatted viewer.

---

# 43. Admin State Model

Page states:

```text
Initial
Loading
Success
Empty
Error
```

Mutation:

```text
Idle
Submitting
Success
Failure
```

---

# 44. Loading

Use:

```text
skeleton table rows
metric skeletons
chart skeletons
form loading states
```

Avoid blocking the entire application for a small mutation.

---

# 45. Empty

Examples:

```text
No users found.
No vocabulary found.
No pending AI content.
No audit events found.
```

Include filter reset where useful.

---

# 46. Error

Example:

```text
Unable to load data.

[Retry]
```

Do not show:

```text
SQL error
stack trace
internal Java class
```

---

# 47. 401 Handling

```text
refresh access token
→ retry request

if refresh fails
→ clear session
→ login
```

Serialize concurrent refresh operations.

---

# 48. 403 Handling

```text
403
→ permission page
```

Do not assume frontend route hiding is sufficient.

---

# 49. 409 Handling

Two important codes:

```text
CONCURRENT_UPDATE
IDEMPOTENCY_KEY_REUSE
```

Admin mutations should refresh/reconcile on `CONCURRENT_UPDATE`.

Idempotency is relevant to mutation endpoints that accept event IDs.

---

# 50. React Component Rules

Components should:

```text
render
handle local UI state
invoke hooks
```

Components should not:

```text
contain API implementation
duplicate business rules
calculate analytics from raw data
```

---

# 51. Feature Hook Pattern

Example:

```text
useUsers()
useUser(id)
useLockUser()
useVocabulary()
useCreateVocabulary()
useAiContent()
useApproveAiContent()
useRejectAiContent()
useLearningStatistics()
useAiUsage()
useAuditLogs()
```

---

# 52. Form Management

Use one standardized form solution.

Requirements:

```text
client validation
touched state
error messages
submitting state
server error mapping
```

---

# 53. Table Component

Reusable:

```text
DataTable
```

Capabilities:

```text
columns
rows
loading
empty
row actions
sorting where supported
selection where needed
```

---

# 54. Action Menu

Standard:

```text
⋮
```

Actions may include:

```text
View
Edit
Lock
Unlock
Deactivate
Publish
Reject
```

Danger actions visually distinct.

---

# 55. Confirmation Dialog

Required for:

```text
Lock
Deactivate
Publish
Unpublish
Approve
Reject
```

Reject has additional reason input.

---

# 56. Toast

Success:

```text
Vocabulary saved.
AI content approved.
User locked.
```

Error:

```text
Action could not be completed.
```

Critical failures still need page-level error state.

---

# 57. Theme

Use Admin Design System:

```text
neutral background
white surfaces
professional typography
semantic colors
subtle borders
limited elevation
```

Admin visual language differs from learner app.

---

# 58. Responsive Design

Primary:

```text
1200px+
```

Support:

```text
1024px+
```

Below:

```text
sidebar collapse
table horizontal scroll
filters wrap
```

---

# 59. Accessibility

Required:

```text
keyboard navigation
visible focus
semantic tables
form labels
contrast
non-color status
dialog keyboard handling
```

---

# 60. Security

Never expose:

```text
password hashes
JWT secrets
refresh token plaintext
AI API keys
database credentials
```

Admin Web should never access DB directly.

---

# 61. CORS

Backend allows only approved Admin Web origins.

Production must not use unrestricted:

```text
*
```

for authenticated Admin API.

---

# 62. Browser Storage

Do not store sensitive token material in arbitrary local storage if the chosen authentication architecture provides a safer mechanism.

Storage strategy must be consistent with backend session/token contract.

---

# 63. URL State

Use URL for:

```text
search
filter
pagination
sort
```

where practical.

Benefits:

```text
shareable admin view
browser back/forward
refresh persistence
```

---

# 64. Cache Invalidation

Examples:

```text
approve AI content
→ invalidate AI content list/detail

lock user
→ invalidate user list/detail

edit vocabulary
→ invalidate vocabulary list/detail
```

---

# 65. Optimistic UI

Allowed for:

```text
non-critical cosmetic updates
```

Be cautious for:

```text
lock status
publish
approve/reject
statistics
```

Prefer server-confirmed mutation state.

---

# 66. AI Review Consistency

After reject:

```text
status = REJECTED
reviewNote = submitted reason
reviewedBy = current admin
reviewedAt = server timestamp
```

Frontend refreshes from server rather than inventing these fields.

---

# 67. Audit Consistency

Every sensitive admin mutation should produce the expected audit record.

For reject:

```text
action = REJECT
details.reason = same reason
```

---

# 68. API Contract

Source of truth:

```text
OpenAPI v1.4
```

Frontend generated/manual types must be checked against the OpenAPI contract.

---

# 69. Admin API Map

```text
Users
→ /api/v1/admin/users

User Detail
→ /api/v1/admin/users/{id}

Vocabulary
→ /api/v1/admin/vocabulary

Topics
→ /api/v1/admin/topics

Quizzes
→ /api/v1/admin/quizzes

AI Content
→ /api/v1/admin/ai-content

Learning Statistics
→ /api/v1/admin/statistics/learning

AI Usage
→ /api/v1/admin/statistics/ai-usage

Audit
→ /api/v1/admin/audit-logs
```

---

# 70. Testing Strategy

Levels:

```text
Unit
Component
Integration
E2E
```

---

# 71. Unit Tests

Test:

```text
formatters
validators
error mapping
filter serialization
permission helpers
```

---

# 72. Component Tests

P0:

```text
Login
Dashboard
Users
Vocabulary
AI Content Review
Statistics
```

Test:

```text
render
loading
empty
error
mutation
```

---

# 73. E2E Tests

Critical flow:

```text
Login
 ↓
Dashboard
 ↓
Vocabulary
 ↓
Add word
 ↓
AI CEFR suggestion
 ↓
Save
 ↓
AI Content
 ↓
Reject with reason
 ↓
AI Content Detail shows Review Note
 ↓
Audit Log shows reason
 ↓
AI Usage
```

---

# 74. AI Review E2E Assertions

After rejection:

```text
status = REJECTED
reviewNote = reason
audit action = REJECT
audit details.reason = reason
```

---

# 75. Accessibility Tests

Check:

```text
keyboard
focus
screen reader where practical
contrast
dialogs
forms
tables
```

---

# 76. Performance

Avoid:

```text
loading thousands of rows
client-side aggregation of large datasets
unmemoized expensive table rendering
```

Use:

```text
pagination
server filtering
server aggregation
query caching
```

---

# 77. Security Testing

Test:

```text
user role denied
expired JWT
403
401
CORS
CSRF strategy where relevant
secret leakage
```

---

# 78. Environment

```text
development
staging
production
```

Config:

```text
VITE_API_BASE_URL
feature flags
logging
```

Never put secret backend credentials in frontend environment variables.

---

# 79. Build

```text
npm install
npm run lint
npm run typecheck
npm test
npm run build
```

Exact package manager/scripts can be standardized during bootstrap.

---

# 80. Deployment

Recommended:

```text
build static assets
 ↓
Nginx / CDN / static hosting
 ↓
Spring Boot API
```

Use HTTPS.

---

# 81. SPA Routing

Server must support fallback:

```text
/index.html
```

for client-side routes when using browser history routing.

---

# 82. Logging

Frontend logs:

```text
development only
```

Production:

```text
minimal
```

Never log:

```text
tokens
passwords
secrets
full sensitive API payloads
```

---

# 83. Error Monitoring

Optional:

```text
Sentry or equivalent
```

Sanitize:

```text
auth headers
user private data
```

---

# 84. Admin Design System Reuse

Shared from Admin Design System:

```text
spacing
typography
colors
radius
components
state semantics
```

Do not duplicate component definitions per feature.

---

# 85. Feature Priority

## P0

```text
Login
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
AI Usage
Audit Logs
AI Content Detail
```

## P2

```text
Advanced analytics
Bulk operations
Role management
Advanced moderation
System settings
```

---

# 86. Development Order

```text
1. App shell
2. Routing
3. Theme
4. Auth
5. API client
6. Dashboard
7. Users
8. Vocabulary
9. AI Content
10. Statistics
11. Topics
12. Quizzes
13. AI Usage
14. Audit
15. E2E testing
```

---

# 87. Backend Integration Order

```text
Auth
 ↓
Admin Users
 ↓
Vocabulary
 ↓
AI Content
 ↓
Statistics
 ↓
Topics
 ↓
Quizzes
 ↓
AI Usage
 ↓
Audit
```

---

# 88. Definition of Done

An Admin Web feature is complete when:

```text
[ ] Route implemented
[ ] UI matches High-Fidelity
[ ] API service typed
[ ] Query/mutation integrated
[ ] Validation
[ ] Loading
[ ] Empty
[ ] Error
[ ] Permission
[ ] Confirmation where needed
[ ] Cache invalidation
[ ] Audit behavior
[ ] Accessibility
[ ] Tests
[ ] Responsive behavior
```

---

# 89. Final Security Rules

```text
1. Frontend route protection is not authorization.
2. Backend enforces ADMIN role.
3. No direct DB access.
4. No secret API keys in browser.
5. No password/token logging.
6. Destructive actions require confirmation.
7. AI rejection reason is persisted/audited.
8. 409 conflicts are handled explicitly.
```

---

# 90. Final Admin Architecture

```text
Browser
  │
  ▼
React + TypeScript
  │
  ├── Router
  ├── UI Components
  ├── Feature Hooks
  ├── API Client
  └── Auth Session
  │
  ▼
Spring Boot API
  │
  ├── Admin
  ├── Auth
  ├── AI
  ├── Statistics
  └── Audit
  │
  ▼
PostgreSQL
```

---

# 91. Final Admin AI Flow

```text
Admin
 ↓
Add Vocabulary
 ↓
AI CEFR Suggestion (FUTURE/V2)
 ↓
AI Content Generation
 ↓
PENDING_REVIEW
 ↓
Admin Review
 ├── Approve
 └── Reject + Reason
           ↓
      review_note
           +
        Audit Log
```

---

# 92. Final Project Structure

```text
                     ENGLISH AI COACH
                            │
            ┌───────────────┼────────────────┐
            │               │                │
       Android Java      Flutter V2       Admin Web
           V1               V2             React + TS
            │               │                │
            └───────────────┼────────────────┘
                            │
                     Spring Boot API
                            │
                ┌───────────┴───────────┐
                │                       │
             PostgreSQL          External Services
                                   ├── LLM
                                   ├── TTS
                                   ├── FCM
                                   └── Storage
```

---

# 93. Final Implementation Gate

Before Admin Web coding:

```text
[ ] OpenAPI v1.4 stable
[ ] Admin API endpoints tested
[ ] ADMIN authorization tested
[ ] AI review lifecycle tested
[ ] review_note tested
[ ] audit tested
[ ] statistics queries tested
[ ] AI usage endpoint tested
[ ] High-Fidelity approved
[ ] Prototype reviewed
```

---

# 94. Immediate Tasks

```text
1. Create React + TypeScript + Vite project
2. Configure Admin theme
3. Configure router
4. Configure API client
5. Implement auth/session guard
6. Implement Admin shell
7. Implement Dashboard
8. Implement Users
9. Implement Vocabulary
10. Implement AI Content Review
```

---

# 95. Final Principle

Admin Web is a **management client** of the English AI Coach platform.

Correct:

```text
Admin UI
→ API
→ Backend business service
→ PostgreSQL / external service
```

Incorrect:

```text
Admin UI
→ database directly

Admin UI
→ LLM provider directly

Admin UI
→ reimplement SRS/business logic
```

---

# 96. Final Transition

```text
Admin Web Technical Specification
        ↓
Admin project bootstrap
        ↓
API integration
        ↓
Dashboard
        ↓
Users
        ↓
Vocabulary
        ↓
AI Review
        ↓
Statistics
        ↓
Audit
        ↓
Testing
        ↓
Release
```

**This document is the implementation baseline for the Admin Web.**

---

# Reconciled Admin Web V1 Contract

Canonical stack is React + TypeScript + Vite over Spring Boot REST API; no direct database access and no Thymeleaf Admin UI. V1 is single-locale Vietnamese with user-facing messages centralized in a typed messages/resource module.

Admin vocabulary CEFR is manual in V1. AI CEFR Suggestion is Future/V2 and must not appear as an enabled V1 action.

Canonical AI usage route: `GET /api/v1/admin/statistics/ai-usage`. Reusable AI generation sends body `eventId`, is synchronous, and returns 201 with `PENDING_REVIEW`. Reject requires a reason; backend persists the same reason to `review_note` and audit details.
