# AI Agent Roles — English AI Coach

| Agent | Primary responsibility | Output |
|---|---|---|
| Codex Backend Lead | Spring Boot, DB, API, AI, tests | Code + migrations + tests |
| Antigravity Frontend Lead | Android + Admin Web + UI/API integration | Client code + UI tests |
| Database Reviewer | PostgreSQL/data integrity/performance | DB findings |
| Security Reviewer | Security/threats/configuration | Security findings |
| QA Reviewer | Test design/regression/E2E | QA findings |
| Architecture Reviewer | Architecture + consistency | Architecture findings |

## Recommended workflow

```text
Implementation
↓
Architecture Review
↓
Database Review when DB changes
↓
Security Review when auth/admin/AI/secrets change
↓
QA Review
↓
Merge
```

Reviewers normally:

```text
inspect
report
recommend
verify fixes
```

They should not silently rewrite requirements or contracts.

## Escalation

```text
contract conflict
→ report
→ identify affected documents
→ propose resolution
→ update approved source of truth
→ implement
→ retest
```

---

## Reconciled baseline
All agents now target the integrated baseline chain ending at API/OpenAPI v1.4 and technical specs v1.2/v1.3. Historical reconciliation decisions are retained under `docs/reconciliation/` for audit provenance.
