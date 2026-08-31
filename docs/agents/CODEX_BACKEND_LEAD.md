# Codex Backend Lead Instructions

**Role:** Backend Lead / Primary Implementation Agent

## Language Policy

All responses and reports to the project owner must be **Vietnamese**.

Explanatory comments in Java/SQL/configuration code must be **Vietnamese**.

Technical identifiers remain unchanged.

```text
class names
method names
variable names
packages
API paths
JSON fields
DB identifiers
enum values
error codes
```

User-facing messages produced by backend endpoints should follow the Vietnamese UI language policy where applicable; technical codes remain unchanged.

## Mission

Own:

```text
Spring Boot
PostgreSQL
Flyway
JPA/Hibernate
Spring Security
REST API
SRS
Personalization
AI orchestration
Admin APIs
Backend tests
```

## Read first

```text
docs/PROJECT_RULES.md
docs/requirements/
docs/architecture/
docs/database/
docs/ai/
docs/api/
docs/technical/
```

For client-facing contract changes also inspect:

```text
docs/mobile/
docs/flutter/
docs/admin/
```

## Backend architecture

Use:

```text
Controller
↓
Application Service
↓
Domain/Business Service
↓
Repository
↓
PostgreSQL
```

External providers must be behind interfaces:

```text
LlmProvider
TtsProvider
ObjectStorageService
NotificationProvider
```

## Do not

```text
Controller → Repository direct for business logic
Controller → LLM direct
Entity → REST response directly
Client → database
Client → LLM provider
```

Use DTOs, services, repositories and mappers.

## Transactions

Critical learning mutations must be transactional.

Learning attempt:

```text
BEGIN
↓
idempotency claim
↓
load state/version
↓
SRS
↓
persist attempt
↓
update progress
↓
XP/streak
↓
store response snapshot
↓
COMMIT
```

Failure:

```text
ROLLBACK
```

## Idempotency

Supported operations include:

```text
POST /api/v1/learning/attempts
POST /api/v1/quiz-attempts/{attemptId}/answers
POST /api/v1/quiz-attempts/{attemptId}/complete
```

Store in:

```text
idempotency_keys
```

Concurrency mechanism:

```text
attempt INSERT
↓
duplicate-key conflict?
↓
catch/inspect the idempotency constraint violation
↓
reload eventId
↓
compare user + endpoint + request_hash
↓
replay or 409
```

Do not let expected duplicate-key races escape as HTTP 500.

## Error codes

Use exactly:

```text
CONCURRENT_UPDATE
IDEMPOTENCY_KEY_REUSE
```

## Optimistic locking

```java
@Version
private Long version;
```

For stale updates:

```text
HTTP 409
code = CONCURRENT_UPDATE
```

Never silently overwrite newer state.

## Learning attempt

Client sends:

```text
answerQuality
```

not `isCorrect`.

Backend derives:

```text
answerQuality >= 3 → true
answerQuality < 3  → false
```

## SRS

Centralize in:

```text
SrsService
```

Test:

```text
0,1,2,3,4,5
```

and all interval/status boundaries.

## AI

Reusable content:

```text
Generate
→ Validate
→ PENDING_REVIEW
→ APPROVED / REJECTED
```

Personalized content:

```text
Personalization
→ Budget Guard
→ LLM
→ Validate
→ Safety
→ User
```

Reject persistence:

```text
ai_generated_content.review_note
admin_audit_logs.details.reason
```

## Database

Use:

```text
PostgreSQL
Flyway
```

Never edit an already applied shared migration.

## API changes

When changing API, synchronize:

```text
API Specification
OpenAPI
DTO
Controller
tests
clients
```

## Security

Implement:

```text
JWT
refresh token
RBAC
resource ownership
brute-force protection
CORS
rate limiting where configured
```

## Done checklist

```text
[ ] docs checked
[ ] no invented contract
[ ] transaction reviewed
[ ] security reviewed
[ ] tests pass
[ ] migration valid
[ ] OpenAPI synchronized
[ ] exact error codes
[ ] no secrets
```

---

## Reconciled implementation authority
Implement only approved algorithms/contracts from SRS v1.2 → DB v1.6 → Architecture v1.3 → AI v1.3 → API/OpenAPI v1.4 → Technical/Backend v1.2/v1.3. Use typed property classes for canonical constants. Do not reintroduce idempotency headers, async AI jobs, dynamic Daily Plan reranking, or V1 AI CEFR suggestion.
