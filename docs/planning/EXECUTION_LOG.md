# English AI Coach — Implementation Execution Log

## M0 — Governance

### GOV-001 — Baseline Input Verification

- Status: DONE
- Branch: `chore/m0-governance`
- Input commit: `ff6e13f4fe1444879b28d846801d0caa555bf4a7`
- Baseline tag: `baseline-v1-implementation-ready`
- Baseline audit: PASS
- Verified at: 2026-08-31
- Contract changes: None

Evidence:

```text
BASELINE AUDIT: PASS
Canonical baseline files present; OpenAPI parses; BR contract checks passed.
```

### GOV-002 — Implementation Planning Binding

- Status: DONE
- Branch: `chore/m0-governance`
- Implementation Plan: `docs/planning/IMPLEMENTATION_PLAN.md`
- Master Backlog: `docs/planning/MASTER_BACKLOG.md`
- Planning Validation: `docs/planning/PLANNING_VALIDATION_REPORT.md`
- Agent workflow binding: ADDED
- Contract changes: None
- Verified at: 2026-08-31

Evidence:

```text
AGENTS.md now requires implementation tasks to follow
IMPLEMENTATION_PLAN.md and MASTER_BACKLOG.md.
Every implementation change must reference a valid Master Backlog Task ID.
```

### GOV-003 — Canonical Idempotency Guidance

- Status: DONE
- Branch: `chore/m0-governance`
- Updated document: `docs/agents/CODEX_BACKEND_LEAD.md`
- Canonical claim strategy: PostgreSQL `INSERT ... ON CONFLICT DO NOTHING`
- Request idempotency key: body `eventId`
- Idempotency request header: NOT USED
- Duplicate exception control flow: REMOVED
- Contract changes: None
- Verified at: 2026-08-31

Evidence:

```text
CODEX_BACKEND_LEAD.md now requires PostgreSQL
INSERT ... ON CONFLICT DO NOTHING for idempotency claims.

Duplicate-key exceptions are no longer used as the normal
duplicate-request control flow.

Same eventId + same logical request -> replay stored response.
Same eventId + different logical request -> 409 IDEMPOTENCY_KEY_REUSE.
```
