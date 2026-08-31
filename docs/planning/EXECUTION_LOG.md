# English AI Coach — Implementation Execution Log

## M0 — Governance

### GOV-001 — Baseline Input Verification

- Status: DONE
- Branch: `chore/GOV-001-GOV-007-m0-governance`
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
- Branch: `chore/GOV-001-GOV-007-m0-governance`
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
- Branch: `chore/GOV-001-GOV-007-m0-governance`
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

### GOV-004 — Pull Request Governance Template

- Status: DONE
- Branch: `chore/GOV-001-GOV-007-m0-governance`
- PR template: `.github/PULL_REQUEST_TEMPLATE.md`
- Task ID required: YES
- Contract impact declaration required: YES
- API / DB / Security / Client impact declaration required: YES
- Test evidence required: YES
- Reviewer declaration required: YES
- Contract changes: None
- Verified at: 2026-08-31

Evidence:

```text
Repository now provides a governed Pull Request template requiring
task traceability, impact declaration, validation evidence,
reviewer gates, backward-compatibility assessment, and Definition of Done.
```

### GOV-005 — Task Lifecycle and Git Naming Governance

- Status: DONE
- Branch: `chore/GOV-001-GOV-007-m0-governance`
- Allowed task statuses: `TODO`, `READY`, `IN_PROGRESS`, `BLOCKED`, `IN_REVIEW`, `DONE`
- Task ID in implementation branch naming: REQUIRED
- Task ID in task-scoped commits: REQUIRED
- Task ID in Pull Requests: REQUIRED
- Generic implementation branches: PROHIBITED
- Contract changes: None
- Verified at: 2026-08-31

Evidence:

```text
AGENTS.md now defines the canonical task lifecycle and Git traceability rules.

Task-scoped implementation branches, commits, and Pull Requests must
reference valid Master Backlog Task IDs.

The M0 governance branch was normalized to:
chore/GOV-001-GOV-007-m0-governance
```

### GOV-006 — Implementation Baseline Freeze Verification

- Status: DONE
- Branch: `chore/GOV-001-GOV-007-m0-governance`
- Baseline tag: `baseline-v1-implementation-ready`
- Baseline commit: `ff6e13f4fe1444879b28d846801d0caa555bf4a7`
- Tag type: Annotated
- Baseline audit at tagged commit: PASS
- Remote tag: VERIFIED
- Tag moved or rewritten: NO
- Contract changes: None
- Verified at: 2026-08-31

Evidence:

```text
Local tag dereferences to:
ff6e13f4fe1444879b28d846801d0caa555bf4a7

Remote annotated tag is present on origin and dereferences to the same
implementation-ready baseline commit.

The existing baseline tag was not moved, rewritten, or force-pushed.
```
