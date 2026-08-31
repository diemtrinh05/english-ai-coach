# Review Report Template

**Review ID:**  
**Reviewer:**  
**Date:**  
**Scope:**  
**Commit / Branch:**  

## Language Check

```text
[ ] Review report is Vietnamese
[ ] Explanatory code comments are Vietnamese where present
[ ] User-facing UI text is Vietnamese where required
[ ] Technical identifiers remain unchanged
```

## 1. Documents Reviewed

```text
PROJECT_RULES:
SRS:
Architecture:
Database:
AI:
API:
OpenAPI:
Technical:
Client:
```

## 2. Findings

### FINDING-001

```text
Severity:
Area:
Component:
Specification reference:
Current behavior:
Expected behavior:
Impact:
Recommendation:
Affected files:
Status:
```

## 3. Contract Check

```text
[ ] Requirements ↔ implementation
[ ] Database ↔ entities
[ ] API ↔ OpenAPI
[ ] Backend ↔ clients
[ ] Error codes exact
[ ] Idempotency correct
[ ] Concurrency correct
```

## 4. Security Check

```text
[ ] Authentication
[ ] Authorization
[ ] Ownership
[ ] Secrets
[ ] CORS
[ ] Logging
[ ] Rate limiting
```

## 5. Testing Check

```text
[ ] Unit
[ ] Integration
[ ] Contract
[ ] Concurrency
[ ] Idempotency
[ ] E2E
```

## 6. Decision

```text
PASS
PASS WITH CHANGES
FAIL
BLOCKED
```

## 7. Follow-up

```text
Required fixes:
Affected documents:
Required tests:
Reviewer:
```

---

## Baseline reconciliation checklist

- [ ] BR-001..BR-024 integrated behavior preserved
- [ ] No stale document versions/routes/idempotency header
- [ ] OpenAPI v1.4 parses and matches API v1.4
- [ ] No Agent-invented business constants
- [ ] No V1 scope creep (offline sync / advanced ML / conversational tutor / AI CEFR suggestion)
