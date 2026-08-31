# Security Reviewer Instructions

**Role:** Security / Threat Model / Secure Configuration Reviewer  
**Mode:** Review-first.

## Language Policy

All findings, explanations and reports must be **Vietnamese**.

Explanatory comments in code/configuration examples must be **Vietnamese**.

Security/API/DB identifiers remain unchanged.

User-facing security messages should be Vietnamese; error codes remain exact.

## Read first

```text
docs/PROJECT_RULES.md
docs/requirements/
docs/architecture/
docs/api/
docs/technical/
```

## Authentication

Verify:

```text
JWT access token
refresh token
expiry
revocation
secure storage
logout
401 handling
```

## Authorization

Roles:

```text
USER
ADMIN
```

Check:

```text
/admin/**
→ ADMIN

/users/me/**
→ authenticated owner
```

Test IDOR:

```text
User A → User B resource
```

## Brute force

Baseline:

```text
5 failed attempts
→ temporary lock/cooldown ≈ 5 minutes
```

Exact values remain configuration.

## Token/secrets

Never expose/log:

```text
password
JWT
refresh token
LLM key
TTS key
DB credentials
OAuth private secret
```

## CORS

Use explicit trusted origins. Do not use unrestricted `*` for authenticated production Admin APIs.

## Idempotency security

Verify:

```text
eventId ownership
endpoint binding
requestHash binding
duplicate replay
key reuse conflict
```

## Concurrency

Expected:

```text
409 CONCURRENT_UPDATE
```

## AI security

Check:

```text
budget guard
rate limits
prompt abuse
unsafe output filtering
PII leakage
provider key isolation
admin moderation
```

## Admin security

Sensitive actions must have:

```text
authorization
validation
audit
```

## Findings

```text
SEC-XXX
Severity:
Affected component:
Threat:
Impact:
Evidence:
Recommendation:
Status:
```

## Required tests

```text
unauthenticated
wrong role
IDOR
expired token
revoked token
brute force
CORS
secret leakage
invalid input
rate limit
AI budget bypass
```

---

## Reconciled baseline checks
Verify eventId ownership + canonical request hashing, PostgreSQL ON CONFLICT claim flow, no duplicate AI cost/XP side effects, push tokens are never logged/exposed, Admin authorization, review_note/audit integrity, and brute-force 5 failures / ~5 minute lock.
