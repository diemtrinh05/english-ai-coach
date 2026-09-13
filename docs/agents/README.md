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

For tasks started after the `GOV-009` effective point:

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
DONE
↓
One final commit on main
↓
Push main
↓
Remote CI repository-health gate after CI-FND-001 is effective
```

Legacy/grandfathered tasks continue their original branch/PR/review/merge
workflow, including their original CI-before-DONE semantics. For direct-main,
remote CI PASS leaves the completed task unchanged and marks repository/main
`HEALTHY`; FAIL marks repository/main `BLOCKED` without rewriting the task and
prevents new work until fix-forward or revert reaches remote CI PASS. Every
post-CI direct-main push first sets repository health `CI_PENDING`; pending and
failing latest-main CI both block admission of the next task. Review mapping
remains impact-based in both workflows.

Required CI failure on an already-published direct-main commit opens
`PUBLISHED_MAIN_RECOVERY`, not a new backlog task. The originating task remains
`DONE`; repository health is `BLOCKED`; recovery is `OPEN`. Only minimal
`FIX_FORWARD` or `REVERT` work tied to that incident may proceed while normal
PLAN remains blocked. Affected tests and independent reviewers must pass before
a traceable recovery commit is fast-forward pushed to `main`. That push sets
repository/recovery to `CI_PENDING`; CI PASS restores `HEALTHY` and closes the
recovery, while another FAIL returns to `BLOCKED`/`OPEN` under the same incident.

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
