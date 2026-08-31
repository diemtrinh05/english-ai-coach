# English AI Coach — Implementation Execution Log

## M0 — Governance

### GOV-001 — Baseline Input Verification

- Status: IN_REVIEW
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

- Status: IN_REVIEW
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

- Status: IN_REVIEW
- Status history: IN_REVIEW → BLOCKED → IN_REVIEW
- Branch: `chore/GOV-001-GOV-007-m0-governance`
- Updated document: `docs/agents/CODEX_BACKEND_LEAD.md`
- Canonical claim strategy: PostgreSQL `INSERT ... ON CONFLICT DO NOTHING`
- Request idempotency key: body `eventId`
- Idempotency request header: NOT USED
- Duplicate exception control flow: REMOVED
- Architecture review result: FAIL — `ARCH-M0-001` (historical)
- Remediation: stale active exception-based guidance replaced with the canonical PostgreSQL claim flow
- Re-review required: NO — Architecture re-review PASS on 2026-08-31
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

- Status: IN_REVIEW
- Status history: IN_REVIEW → BLOCKED → IN_REVIEW
- Branch: `chore/GOV-001-GOV-007-m0-governance`
- PR template: `.github/PULL_REQUEST_TEMPLATE.md`
- Task ID required: YES
- Task status required: YES
- Dependency status required: YES
- Contract impact declaration required: YES
- API / DB / Security / Client impact declaration required: YES
- Test evidence required: YES
- Reviewer declaration required: YES
- Architecture review result: FAIL — `ARCH-M0-002` (historical)
- Remediation: required Task status and Dependency status metadata fields added
- Re-review required: NO — Architecture re-review PASS on 2026-08-31
- Contract changes: None
- Verified at: 2026-08-31

Evidence:

```text
Repository now provides a governed Pull Request template requiring
task traceability, impact declaration, validation evidence,
reviewer gates, backward-compatibility assessment, and Definition of Done.
```

### GOV-005 — Task Lifecycle and Git Naming Governance

- Status: IN_REVIEW
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

- Status: IN_REVIEW
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

### GOV-007 — Execution Log and Milestone Reporting Cadence

- Status: IN_REVIEW
- Branch: `chore/GOV-001-GOV-007-m0-governance`
- Execution log: `docs/planning/EXECUTION_LOG.md`
- Decision logging: ENABLED
- Blocker logging: ENABLED
- Review-result logging: ENABLED
- Milestone completion reporting: ENABLED
- Secret logging: PROHIBITED
- Contract changes: None
- Verified at: 2026-08-31

#### Reporting cadence

Update this execution log:

- when a backlog task changes status;
- when a non-contract implementation decision is made;
- when a blocker is discovered, changed, or resolved;
- when a required reviewer returns PASS, FAIL, or findings;
- before and after each milestone gate;
- before merging a milestone or task branch into `main`.

Do not record passwords, API keys, access tokens, refresh tokens,
private keys, production credentials, or other secrets in this log.

#### Task execution record

For each executed task, record:

```text
Task ID:
Status:
Branch:
Commit:
Dependencies:
Validation:
Required reviewers:
Review result:
Blockers:
Non-contract decisions:
Contract changes:
```

#### Decision log

Record only implementation decisions that do not modify the approved baseline contract.

| Date       | Task ID          | Decision                                            | Reason                                                        | Contract impact |
| ---------- | ---------------- | --------------------------------------------------- | ------------------------------------------------------------- | --------------- |
| 2026-08-31 | GOV-001..GOV-007 | Use one governed M0 batch branch with Task ID range | Governance tasks are closely related and explicitly traceable | None            |

If a proposed decision would change an approved contract, do not record it as an implementation decision and do not proceed. Stop and use the approved contract-change process instead.

#### Blocker log

| Date       | Task ID | Status   | Blocker | Resolution |
| ---------- | ------- | -------- | ------- | ---------- |
| 2026-08-31 | GOV-003 | RESOLVED | `ARCH-M0-001` — active documentation contradicted the canonical PostgreSQL idempotency claim with exception-based duplicate handling. | Replaced all identified active stale passages with `INSERT ... ON CONFLICT (event_id) DO NOTHING`, load/compare/replay-or-409 flow; Architecture re-review PASS on 2026-08-31. |
| 2026-08-31 | GOV-004 | RESOLVED | `ARCH-M0-002` — PR template omitted required Task status and Dependency status metadata. | Added both required fields without removing existing governance sections; Architecture re-review PASS on 2026-08-31. |

#### Review result log

| Date       | Task ID(s) | Reviewer              | Result | Findings / evidence |
| ---------- | ---------- | --------------------- | ------ | ------------------- |
| 2026-08-31 | GOV-003 | Architecture Reviewer | FAIL | `ARCH-M0-001` — canonical PostgreSQL idempotency guidance was contradicted by stale exception-based duplicate-key guidance. Remediation was required; subsequently RESOLVED by Architecture re-review PASS on 2026-08-31. |
| 2026-08-31 | GOV-004 | Architecture Reviewer | FAIL | `ARCH-M0-002` — PR template did not collect Task status and Dependency status. Remediation was required; subsequently RESOLVED by Architecture re-review PASS on 2026-08-31. |
| 2026-08-31 | GOV-002, GOV-003, GOV-004, GOV-006 | Architecture Reviewer | PASS | Architecture re-review: `ARCH-M0-001` and `ARCH-M0-002` RESOLVED; no new blocking or non-blocking findings; recommendation APPROVE. |

#### Milestone status

| Milestone                                | Execution complete | Total | Execution progress | DoD status  |
| ---------------------------------------- | -----------------: | ----: | -----------------: | ----------- |
| M0 — Execution Governance                |                  7 |     7 |               100% | IN_REVIEW   |
| M1 — Foundation Ready                    |                  0 |    27 |                 0% | NOT_STARTED |
| M2 — Identity & Catalog                  |                  0 |    21 |                 0% | NOT_STARTED |
| M3 — First Vertical Slice — Learning/SRS |                  0 |    16 |                 0% | NOT_STARTED |
| M4                                       |                  0 |    14 |                 0% | NOT_STARTED |
| M5                                       |                  0 |    18 |                 0% | NOT_STARTED |
| M6                                       |                  0 |     8 |                 0% | NOT_STARTED |
| M7                                       |                  0 |    21 |                 0% | NOT_STARTED |
| M8                                       |                  0 |    25 |                 0% | NOT_STARTED |
| M9                                       |                  0 |    18 |                 0% | NOT_STARTED |

Evidence:

```text
Execution logging now defines a repeatable cadence for task status,
non-contract decisions, blockers, reviewer results, and milestone completion.

Secrets and credentials are explicitly prohibited from execution logs.
```
