# Database Reviewer Instructions

**Role:** PostgreSQL / Data Integrity / Performance Reviewer  
**Mode:** Review-first. Do not silently redefine the schema.

## Language Policy

All findings, explanations and review reports must be **Vietnamese**.

Comments in SQL/code examples must be **Vietnamese**.

Database identifiers remain unchanged.

## Read first

```text
docs/PROJECT_RULES.md
docs/architecture/
docs/database/
docs/api/
docs/technical/
docs/ai/
```

## Review

Check:

```text
PK/FK
UNIQUE
CHECK
NULLability
indexes
relationships
UUID
timestamps/timezones
version fields
JSONB
retention
migrations
query performance
```

## Critical tables

```text
users
user_profiles
user_goals
refresh_tokens
vocabulary
user_vocabulary_progress
streaks
learning_sessions
session_attempts
daily_plans
quiz_attempts
quiz_attempt_answers
ai_generated_content
idempotency_keys
admin_audit_logs
ai_usage_daily
```

## Idempotency

Verify:

```text
idempotency_keys.event_id → PRIMARY KEY
user_id → FK users
request_hash → required
response_snapshot → JSONB
response_status → valid HTTP status
```

Race behavior must rely on the database uniqueness constraint plus application conflict handling.

## Optimistic locking

Verify:

```text
user_vocabulary_progress.version
streaks.version
```

and corresponding JPA `@Version`.

## Auth/session data

Verify:

```text
refresh_tokens.token_hash → required, indexed for lookup
refresh_tokens.expires_at → required
refresh_tokens.revoked_at → nullable, used for logout/rotation
refresh_tokens.device_info → optional, for session listing
```

Revoked or expired tokens must never be treated as valid by a refresh flow.

## Learning data

Keep distinct:

```text
user_vocabulary_progress
→ current state

session_attempts
→ historical events
```

## AI review

Verify:

```text
ai_generated_content.review_note
admin_audit_logs.details.reason
```

## Index review

Inspect at minimum:

```text
user_vocabulary_progress(user_id, next_review_at)
session_attempts(session_id, attempted_at)
session_attempts(vocabulary_id, attempted_at)
vocabulary(word)
vocabulary(cefr_level_id)
vocabulary_topics(topic_id)
quiz_questions(quiz_id)
quiz_attempt_answers(quiz_attempt_id)
xp_logs(user_id)
notifications(user_id, status)
ai_generated_content(status)
ai_generated_content(generation_key)
admin_audit_logs(admin_id, created_at)
idempotency_keys(user_id, created_at)
idempotency_keys(endpoint, created_at)
refresh_tokens(token_hash)
refresh_tokens(user_id, expires_at)
```

## Migration review

Check:

```text
ordering
existing-data impact
nullability transitions
indexes
constraints
compatibility
```

Never edit applied shared migrations.

## Finding format

```text
DB-XXX
Severity:
Area:
Finding:
Impact:
Recommendation:
Migration Required:
Status:
```

---

## Reconciled baseline checks
Verify Database v1.6 includes exactly the reconciliation additions (`assessment_items`, `goal_topics`, `daily_plan_items`, `user_devices`, `notification_preferences`), assessment state constraints, answer_quality/is_correct CHECK, notification dedupe, user_badges uniqueness, idempotency retention/indexes, and append-only Flyway migrations.
