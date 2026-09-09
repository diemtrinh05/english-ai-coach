# English AI Coach — Implementation Execution Log

## M0 — Governance

### GOV-001 — Baseline Input Verification

- Status: DONE
- Status history: IN_REVIEW → DONE
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
- Status history: IN_REVIEW → DONE
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
- Status history: IN_REVIEW → BLOCKED → IN_REVIEW → DONE
- Branch: `chore/GOV-001-GOV-007-m0-governance`
- Updated document: `docs/agents/CODEX_BACKEND_LEAD.md`
- Canonical claim strategy: PostgreSQL `INSERT ... ON CONFLICT DO NOTHING`
- Request idempotency key: body `eventId`
- Idempotency request header: NOT USED
- Duplicate exception control flow: REMOVED
- Architecture review result: FAIL — `ARCH-M0-001` (historical)
- Remediation: stale active exception-based guidance replaced with the canonical PostgreSQL claim flow
- Architecture re-review required: NO — Architecture re-review PASS on 2026-08-31
- Database review result: PASS
- Security review result: PASS
- QA review result: FAIL — `QA-M0-001` (historical)
- QA finding status: `QA-M0-001` RESOLVED by Section 109 remediation on 2026-09-01
- QA remediation: Section 109 now makes the PostgreSQL claim result authoritative; only an inserted claim may execute the business mutation, while a non-inserted claim reloads and validates the existing logical request before replay or HTTP 409 `IDEMPOTENCY_KEY_REUSE`
- Architecture focused re-review required: NO — PASS on 2026-09-01
- Database focused re-review required: NO — PASS on 2026-09-01
- Security focused re-review required: NO — PASS on 2026-09-01
- QA re-review required: NO — QA re-review PASS on 2026-09-01
- QA re-review result: PASS
- Final QA recommendation: APPROVE M0
- Contract changes: None
- Verified at: 2026-09-01

Evidence:

```text
CODEX_BACKEND_LEAD.md now requires PostgreSQL
INSERT ... ON CONFLICT DO NOTHING for idempotency claims.

Duplicate-key exceptions are no longer used as the normal
duplicate-request control flow.

Same eventId + same logical request -> replay stored response.
Same eventId + different logical request -> 409 IDEMPOTENCY_KEY_REUSE.

Section 109 now branches on the boolean result returned by
claimWithOnConflictDoNothing(...). operation.get() is reachable only when
the claim was inserted. The claim-not-inserted branch reloads eventId,
compares user_id separately + endpoint + canonical SHA-256 request_hash,
then replays or returns 409 IDEMPOTENCY_KEY_REUSE.
```

### GOV-004 — Pull Request Governance Template

- Status: DONE
- Status history: IN_REVIEW → BLOCKED → IN_REVIEW → DONE
- Security review status transition: IN_REVIEW → BLOCKED → IN_REVIEW
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
- Architecture re-review required: NO — Architecture re-review PASS on 2026-08-31
- Database review result: FAIL — `DB-M0-001` (historical)
- Database remediation: mandatory Database Reviewer gate, schema-to-Flyway declaration, approved no-migration exception, and database validation evidence added
- Database re-review required: NO — Database re-review PASS on 2026-09-01
- Security review result: FAIL — `SEC-M0-001` (historical)
- Security remediation: mandatory Security Reviewer gate, mutually exclusive no-security-impact declaration, and explicit protection against generic omitted-reviewer bypass added
- Security re-review required: NO — Security re-review PASS on 2026-09-01
- Contract changes: None
- Verified at: 2026-09-01

Evidence:

```text
Repository now provides a governed Pull Request template requiring
task traceability, impact declaration, validation evidence,
reviewer gates, backward-compatibility assessment, and Definition of Done.

Database-impacting PRs now require Database Reviewer approval and relevant
database validation evidence. Every production schema change must identify
its Flyway migration, or document an explicit exception approved by the
Database Reviewer. Database re-review PASS on 2026-09-01.

Security-sensitive PRs now require Security Reviewer approval. The
`No security-sensitive change` declaration is mutually exclusive with every
security-sensitive impact option, and the generic omitted-reviewer explanation
cannot be used to omit Security Reviewer. Historical Security FAIL is preserved;
Security re-review PASS on 2026-09-01.
```

### GOV-005 — Task Lifecycle and Git Naming Governance

- Status: DONE
- Status history: IN_REVIEW → DONE
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
- Status history: IN_REVIEW → DONE
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

- Status: DONE
- Status history: IN_REVIEW → DONE
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
- before merging a milestone or task branch into `main` for a legacy/grandfathered task;
- for a post-`GOV-009` direct-main task, before finalization/commit and after push/remote-CI verification.

Do not record passwords, API keys, access tokens, refresh tokens,
private keys, production credentials, or other secrets in this log.

#### Task execution record

For each executed task, record:

```text
Task ID:
Status:
Branch/worktree mode:
Commit (after finalization, except the CI-FND-001 bootstrap exception):
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
| 2026-09-01 | GOV-004 | RESOLVED | `DB-M0-001` — PR template did not require Database Reviewer approval for declared database impact and did not bind production schema changes to a Flyway migration or an approved exception. | Added the mandatory Database Reviewer gate, mutually exclusive no-impact declaration, schema/Flyway and approved-exception declarations, and relevant database validation evidence. Historical Database FAIL is preserved; Database re-review PASS on 2026-09-01. |
| 2026-09-01 | GOV-004 | RESOLVED | `SEC-M0-001` — PR template declared security-sensitive impacts without making Security Reviewer approval mandatory and allowed the generic omitted-reviewer explanation to bypass Security Reviewer. | Added the mandatory Security Reviewer gate, made `No security-sensitive change` mutually exclusive with all security-impact options, and prohibited Security Reviewer omission through the generic explanation. Historical Security FAIL is preserved; Security re-review PASS on 2026-09-01. |
| 2026-09-01 | GOV-003 | RESOLVED | `QA-M0-001` — Section 109 performed a pre-read and claim, then executed `operation.get()` without checking whether `INSERT ... ON CONFLICT (event_id) DO NOTHING` inserted the claim. | Section 109 was remediated so the claim result is authoritative: only the inserted branch executes the mutation and stores the response atomically; the not-inserted branch reloads, validates user/endpoint/hash, and replays or returns 409. Architecture, Database, and Security focused re-reviews PASS; QA re-review PASS on 2026-09-01. |
| 2026-09-01 | BE-FND-001 | RESOLVED | `QA-BE-FND-001-001` — README documents build commands and artifact location but omits the executable Spring Boot JAR run command. | README now documents unambiguous build/test/run commands from the repository root; implementation-side validation passed; QA re-review PASS confirmed the finding resolved with no regression and recommendation APPROVE. |
| 2026-09-08 | DB-FND-002 | OPEN | `QA-DB-FND-002-001` — QA could not independently reproduce the fresh migration/catalog/repeat-run evidence because the prior temporary PostgreSQL runtime had been removed. Severity: MEDIUM; Blocking: YES. | Focused remediation restored a fresh PostgreSQL runtime on `127.0.0.1:55432`, reran Flyway/catalog/constraint/repeat-run validation successfully, and left the runtime available; independent QA re-review remains pending. |
| 2026-09-08 | DB-FND-002 | RESOLVED | `QA-DB-FND-002-001` — historical metadata preserved as Severity: MEDIUM; Blocking: YES; original Status: OPEN. | Focused QA re-review PASS after independently accessible PostgreSQL runtime evidence; final finding Status: RESOLVED; unresolved findings: NONE. |
| 2026-09-09 | GOV-009 | OPEN | `ARCH-GOV-009-001` — Severity: HIGH; Blocking: YES; Original Status: OPEN. GOV-009 lacked an explicit valid one-time closure mode before its effective-point commit. | Added `OWNER_AUTHORIZED_GOVERNANCE_TRANSITION` and its non-reusable validation gates; finding remains OPEN pending focused Architecture re-review. |
| 2026-09-09 | GOV-009 | OPEN | `ARCH-GOV-009-002` — Severity: HIGH; Blocking: YES; Original Status: OPEN. Active Global DoD still required CI PASS before DONE while direct-main remote CI can run only after push. | Split legacy task CI completion semantics from direct-main pre-publish task DONE and post-push repository-health semantics; finding remains OPEN pending focused Architecture re-review. |
| 2026-09-09 | GOV-009 | OPEN | `ARCH-GOV-009-003` — Severity: MEDIUM; Blocking: YES; Original Status: OPEN. Active `CI-FND-001` acceptance still said CI failure prevents merge. | Replaced active acceptance with the bootstrap commit/push/actual-CI/two-commit state machine; finding remains OPEN pending focused Architecture re-review. |
| 2026-09-09 | GOV-009 | RESOLVED | `ARCH-GOV-009-001` — Severity: HIGH; Blocking: YES; Original Status: OPEN. | Architecture focused re-review #2 confirmed the GOV-009-only `OWNER_AUTHORIZED_GOVERNANCE_TRANSITION` closure gate; Final Status: RESOLVED. |
| 2026-09-09 | GOV-009 | RESOLVED | `ARCH-GOV-009-002` — Severity: HIGH; Blocking: YES; Original Status: OPEN. | Architecture focused re-review #2 confirmed task DONE versus repository-health separation; Final Status: RESOLVED. |
| 2026-09-09 | GOV-009 | RESOLVED | `ARCH-GOV-009-003` — Severity: MEDIUM; Blocking: YES; Original Status: OPEN. | Architecture focused re-review #2 confirmed the CI-FND-001 bootstrap state machine; Final Status: RESOLVED. |
| 2026-09-09 | GOV-009 | OPEN | `ARCH-GOV-009-004` — Severity: HIGH; Blocking: YES; Original Status: OPEN. Direct-main admission did not require an exact valid CI mode or latest-main health. | Added common admission, pre-/post-CI decision table, `INVALID_BEFORE_CI`, `HEALTHY`/`CI_PENDING`/repository `BLOCKED`, and after-push transitions; remains OPEN pending focused Architecture re-review #3. |
| 2026-09-09 | GOV-009 | OPEN | `ARCH-GOV-009-005` — Severity: MEDIUM; Blocking: YES; Original Status: OPEN. Active PRE_CI expiry still used legacy merge wording and did not define irreversible expiry after the CI-FND-001 closure push. | Defined the exact CI-FND-001 effective point and irreversible PRE_CI expiry across active governance; remains OPEN pending focused Architecture re-review #3. |
| 2026-09-09 | GOV-009 | RESOLVED | `ARCH-GOV-009-004` — Severity: HIGH; Blocking: YES; Original Status: OPEN. | Architecture focused re-review #3 confirmed exact direct-main admission and repository-health gating; Final Status: RESOLVED. |
| 2026-09-09 | GOV-009 | RESOLVED | `ARCH-GOV-009-005` — Severity: MEDIUM; Blocking: YES; Original Status: OPEN. | Architecture focused re-review #3 confirmed the irreversible PRE_CI expiry/effective-point model; Final Status: RESOLVED. |
| 2026-09-09 | GOV-009 | OPEN | `ARCH-GOV-009-006` — Severity: HIGH; Blocking: YES; Original Status: OPEN. Published-main CI failure blocked all work but had no explicit recovery admission/state machine, creating a recovery deadlock. | Added the separate `PUBLISHED_MAIN_RECOVERY` operational model, narrow BLOCKED-state exception, traceability, FIX_FORWARD/REVERT flow, affected validation/reviewer gates, recovery commit semantics and CI state transitions; remains OPEN pending focused Architecture re-review #4. |
| 2026-09-09 | GOV-009 | RESOLVED | `ARCH-GOV-009-006` — Severity: HIGH; Blocking: YES; Original Status: OPEN. | Architecture focused re-review #4 confirmed the published-main recovery model; Final Status: RESOLVED; new findings: NONE. |

#### Review result log

| Date       | Task ID(s) | Reviewer              | Result | Findings / evidence |
| ---------- | ---------- | --------------------- | ------ | ------------------- |
| 2026-08-31 | GOV-003 | Architecture Reviewer | FAIL | `ARCH-M0-001` — canonical PostgreSQL idempotency guidance was contradicted by stale exception-based duplicate-key guidance. Remediation was required; subsequently RESOLVED by Architecture re-review PASS on 2026-08-31. |
| 2026-08-31 | GOV-004 | Architecture Reviewer | FAIL | `ARCH-M0-002` — PR template did not collect Task status and Dependency status. Remediation was required; subsequently RESOLVED by Architecture re-review PASS on 2026-08-31. |
| 2026-08-31 | GOV-002, GOV-003, GOV-004, GOV-006 | Architecture Reviewer | PASS | Architecture re-review: `ARCH-M0-001` and `ARCH-M0-002` RESOLVED; no new blocking or non-blocking findings; recommendation APPROVE. |
| 2026-09-01 | GOV-003 | Database Reviewer | PASS | Canonical PostgreSQL idempotency guidance is aligned with Database Schema v1.6 and the approved `INSERT ... ON CONFLICT (event_id) DO NOTHING` claim flow. |
| 2026-09-01 | GOV-004 | Database Reviewer | FAIL | `DB-M0-001` — PR template allowed database impact without mandatory Database Reviewer approval and lacked a required schema-change/Flyway declaration. Remediation was required; subsequently RESOLVED by Database re-review PASS on 2026-09-01. |
| 2026-09-01 | GOV-003, GOV-004 | Database Reviewer | PASS | Database re-review: `DB-M0-001` RESOLVED; no new blocking or non-blocking findings; Database v1.6 unchanged; no Flyway migration required; recommendation APPROVE. |
| 2026-09-01 | GOV-003 | Security Reviewer | PASS | Canonical PostgreSQL idempotency guidance preserves the approved eventId ownership, endpoint binding, canonical request-hash binding, replay, and reuse-conflict controls. |
| 2026-09-01 | GOV-004 | Security Reviewer | FAIL | `SEC-M0-001` — PR template did not require Security Reviewer approval when a security-sensitive impact was declared, and the generic omitted-reviewer explanation could bypass Security Reviewer. Remediation was required; subsequently RESOLVED by Security re-review PASS on 2026-09-01. |
| 2026-09-01 | GOV-003, GOV-004 | Security Reviewer | PASS | Security re-review: `SEC-M0-001` RESOLVED; no new blocking or non-blocking findings; authentication/authorization and V1 security contracts unchanged; recommendation APPROVE. |
| 2026-09-01 | GOV-001 | QA Reviewer | PASS | M0 governance QA review; no QA finding for GOV-001. |
| 2026-09-01 | GOV-002 | QA Reviewer | PASS | M0 governance QA review; no QA finding for GOV-002. |
| 2026-09-01 | GOV-003 | QA Reviewer | FAIL | `QA-M0-001` — Section 109 allowed the claim-losing concurrent request to reach the business mutation. Remediation was required; the finding was subsequently RESOLVED after Section 109 remediation, focused Architecture/Database/Security PASS, and final QA re-review PASS on 2026-09-01. |
| 2026-09-01 | GOV-004 | QA Reviewer | PASS | M0 governance QA review; no QA finding for GOV-004. |
| 2026-09-01 | GOV-005 | QA Reviewer | PASS | M0 governance QA review; no QA finding for GOV-005. |
| 2026-09-01 | GOV-006 | QA Reviewer | PASS | M0 governance QA review; no QA finding for GOV-006. |
| 2026-09-01 | GOV-007 | QA Reviewer | PASS | M0 governance QA review; no QA finding for GOV-007. |
| 2026-09-01 | GOV-003 | Architecture Reviewer | PASS | Focused re-review of `QA-M0-001`: Section 109 is race-safe; claim insertion result is authoritative; atomicity guidance remains consistent; no architecture contract change; recommendation APPROVE. |
| 2026-09-01 | GOV-003 | Database Reviewer | PASS | Focused re-review of `QA-M0-001`: PostgreSQL claim flow and transaction semantics are correct; concurrent loser cannot execute the mutation; Database Schema v1.6 unchanged; no Flyway migration required; recommendation APPROVE. |
| 2026-09-01 | GOV-003 | Security Reviewer | PASS | Focused re-review of `QA-M0-001`: concurrent duplicate mutation is prevented; user_id/endpoint/request_hash binding and replay/reuse-conflict semantics are preserved; no security contract change; recommendation APPROVE. |
| 2026-09-01 | GOV-003 | QA Reviewer | PASS | QA re-review of `QA-M0-001`: Section 109 is race-safe; duplicate side effects are prevented; canonical idempotency semantics are preserved; no API/OpenAPI, Database Schema v1.6, security, or business-rule contract change; recommendation APPROVE M0. |
| 2026-09-01 | BE-FND-001 | QA Reviewer | FAIL | `QA-BE-FND-001-001` (P1 — Blocking) — README omits the executable Spring Boot JAR run command; focused remediation and QA re-review required. |
| 2026-09-01 | BE-FND-001 | QA Reviewer | PASS | Focused re-review: `QA-BE-FND-001-001` RESOLVED; README build/test/run usage is unambiguous; no regression introduced; recommendation APPROVE. |
| 2026-09-02 | BE-FND-002 | Architecture Reviewer | PASS | Independent architecture review completed with no findings. |
| 2026-09-02 | BE-FND-002 | QA Reviewer | PASS | Independent QA review completed with no findings. |
| 2026-09-03 | DB-FND-001 | Database Reviewer | PASS | Independent database review completed with no findings. |
| 2026-09-03 | DB-FND-001 | QA Reviewer | PASS | Independent QA review completed with no findings. |
| 2026-09-08 | DB-FND-002 | Database Reviewer | PASS | Independent database review completed with no findings. |
| 2026-09-08 | DB-FND-002 | Architecture Reviewer | PASS | Independent architecture review completed with no findings. |
| 2026-09-08 | DB-FND-002 | QA Reviewer | FAIL | `QA-DB-FND-002-001` — Severity: MEDIUM; Blocking: YES; Status: OPEN. The prior temporary PostgreSQL runtime was not independently accessible. Focused remediation and QA re-review required. |
| 2026-09-08 | DB-FND-002 | QA Reviewer | PASS | Focused QA re-review PASS; `QA-DB-FND-002-001` final Status: RESOLVED; unresolved findings: NONE. Historical QA FAIL and original OPEN metadata remain preserved. |
| 2026-09-09 | GOV-009 | Architecture Reviewer | FAIL | `ARCH-GOV-009-001` HIGH/Blocking, `ARCH-GOV-009-002` HIGH/Blocking, and `ARCH-GOV-009-003` MEDIUM/Blocking. Original Status for all findings: OPEN. Remediation required; no finding is resolved pending focused Architecture re-review. |
| 2026-09-09 | GOV-009 | Architecture Reviewer | FAIL | Architecture focused re-review #2: `ARCH-GOV-009-001/002/003` Final Status RESOLVED; new `ARCH-GOV-009-004` HIGH/Blocking and `ARCH-GOV-009-005` MEDIUM/Blocking, both Original Status OPEN. Focused remediation and Architecture re-review #3 required. |
| 2026-09-09 | GOV-009 | Architecture Reviewer | FAIL | Architecture focused re-review #3: `ARCH-GOV-009-004/005` Final Status RESOLVED; new `ARCH-GOV-009-006` HIGH/Blocking, Original Status OPEN. Focused remediation and Architecture re-review #4 required. |
| 2026-09-09 | GOV-009 | Architecture Reviewer | PASS | Architecture focused re-review #4: `ARCH-GOV-009-006` Final Status RESOLVED; new findings: NONE; final AR gate PASS. Historical review FAIL results remain preserved. |
| 2026-09-09 | GOV-009 | QA Reviewer | PASS | Independent QA review: findings NONE; final QAR gate PASS. |

#### Milestone status

| Milestone                                | Execution complete | Total | Execution progress | DoD status  |
| ---------------------------------------- | -----------------: | ----: | -----------------: | ----------- |
| M0 — Execution Governance                |                  7 |     7 |               100% | PASS        |
| M1 — Foundation Ready                    |                  6 |    29 |              20.7% | IN_PROGRESS |
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

M0 closure reviewer gates: Architecture PASS, Database PASS, Security PASS,
and final QA re-review PASS. Final QA recommendation: APPROVE M0.
```

## M1 — Foundation Ready

### BE-FND-001 — Bootstrap Spring Boot backend project

- Status: DONE
- Status history: TODO → READY → IN_PROGRESS → IN_REVIEW → BLOCKED → IN_REVIEW → DONE
- Branch: `feat/BE-FND-001-spring-bootstrap`
- Baseline provenance: `baseline-v1-implementation-ready-r1` (`34362780eb7ffeb9391ade95220cf895a4592f70`)
- Dependencies: `GOV-006` DONE
- Priority: P0
- Required reviewers: Architecture Reviewer, QA Reviewer
- Acceptance: Project build được; Java/Spring Boot theo Technical Spec; build tool được ghi rõ trong README; không thêm dependency ngoài nhu cầu baseline.
- Required tests: Build smoke test
- Contract changes: None
- Started at: 2026-09-01
- Ready for review: 2026-09-01
- Reviewer results: Architecture Reviewer PASS; QA Reviewer FAIL (historical) — `QA-BE-FND-001-001`; QA re-review PASS
- Final reviewer gates: AR=PASS; QAR=PASS
- Blockers: None
- Remediation ready for QA re-review: 2026-09-01
- Closed at: 2026-09-01
- Pull Request: #2 — `feat(BE-FND-001): bootstrap Spring Boot backend`
- CI status: `PRE_CI_BOOTSTRAP_NA`
- PRE_CI eligible Task ID: `BE-FND-001`
- `GOV-008` prerequisite: DONE — merged to `main` through PR #3 and merged into this branch
- CI status reason: `CI-FND-001` has not yet been implemented and `BE-FND-001` is an explicitly eligible prerequisite in the approved pre-CI bootstrap chain
- Existing failing CI check: NONE
- Failed CI check waiver: NOT USED — `PRE_CI_BOOTSTRAP_NA` does not waive a failed CI check
- PRE_CI reviewer state: Architecture Reviewer PASS; QA Reviewer PASS
- PRE_CI finding state: `QA-BE-FND-001-001` RESOLVED; regression introduced: NO
- PRE_CI unresolved blockers: NONE

Reviewer evidence:

```text
Reviewer: Architecture Reviewer
Result: PASS
Findings: none
Recommendation: APPROVE
Scope creep: none
Contract impact: none
```

QA reviewer evidence (historical):

```text
Reviewer: QA Reviewer
Result: FAIL
Finding ID: QA-BE-FND-001-001
Severity: P1 — Blocking
Finding: README documents clean verify/build commands and artifact location but
does not document how to run the executable Spring Boot JAR.
Required action: Add the executable JAR run command for the documented working
directory and make build/test/run usage unambiguous.
Finding status: REMEDIATED — QA RE-REVIEW PENDING
QA re-review: REQUIRED
```

Non-contract decisions:

```text
Build tool: Maven + Maven Wrapper
Java baseline: 21 LTS
Spring Boot: 4.1.1
Reason: Technical/Backend Specifications cho phép Maven hoặc Gradle và không khóa
số phiên bản; lựa chọn này dùng stable Spring Boot hiện tại, Java LTS và dependency
management do Spring Boot cung cấp mà không thay đổi API/DB/business contract.
```

Implementation evidence:

```text
Maven Wrapper 3.3.4 khóa Maven 3.9.16.
Spring Boot entry point dùng package com.example.englishaicoach.
Direct runtime dependency: spring-boot-starter-webmvc.
Direct test dependency: spring-boot-starter-webmvc-test.
README ghi rõ build tool, JDK requirement, lệnh smoke build và artifact path.
Không thêm API, DB, Flyway, security, profile/config hoặc business logic của task kế tiếp.
```

Validation evidence:

```text
.\mvnw.cmd clean verify
→ BUILD SUCCESS
→ Tests run: 1, Failures: 0, Errors: 0, Skipped: 0

java -jar target\english-ai-coach-backend-0.0.1-SNAPSHOT.jar --server.port=0
→ Embedded Tomcat started successfully on an ephemeral port

python tools/baseline_audit.py
→ BASELINE AUDIT: PASS

python -m py_compile tools/baseline_audit.py
→ PASS

git diff --check
→ PASS
```

Focused remediation evidence — `QA-BE-FND-001-001`:

```text
README working directory: repository root
Build and smoke test: .\backend\mvnw.cmd -f backend\pom.xml clean verify
Run executable JAR: java -jar backend\target\english-ai-coach-backend-0.0.1-SNAPSHOT.jar
Build result: BUILD SUCCESS
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
Run result: embedded Tomcat started successfully on an ephemeral port
baseline_audit.py: PASS
py_compile: PASS
git diff --check: PASS
QA re-review: REQUIRED — no QA PASS claimed
```

QA re-review and closure evidence:

```text
Reviewer: QA Reviewer
Result: PASS
Finding ID: QA-BE-FND-001-001
Finding status: RESOLVED
Regression introduced: NO
Recommendation: APPROVE
Final reviewer gates: AR=PASS; QAR=PASS
Unresolved blockers: none
API/OpenAPI changes: none
Database/Flyway changes: none
Security changes: none
Business/client contract changes: none
```

### BE-FND-002 — Dựng modular-monolith package/module skeleton

- Status: DONE
- Status history: TODO → READY → IN_PROGRESS → IN_REVIEW → DONE
- Branch: `feat/BE-FND-002-modular-monolith-skeleton`
- Commit: `a84da779bb352f26636a8b96c3c34db8eaec7a38`
- Pull Request: #4 — `feat(BE-FND-002): add modular-monolith package skeleton`
- Pull Request state at evidence sync: OPEN; non-draft; mergeable state CLEAN
- Dependencies: `BE-FND-001` DONE
- Priority: P0
- Required reviewers: Architecture Reviewer, QA Reviewer
- Acceptance: Có module/package cho auth, user, onboarding, vocabulary, learning, personalization, quiz, progress, gamification, notification, ai, admin, audit, common; dependency direction đúng Architecture v1.3.
- Required tests: Theo global DoD + acceptance
- Source documents checked: System Architecture v1.3; Technical Specification v1.2; Backend Technical Specification v1.3
- Unresolved blockers: None
- Contract changes: None
- Started at: 2026-09-02
- Ready for review: 2026-09-02
- Closed at: 2026-09-02
- Reviewer results: Architecture Reviewer PASS — no findings; QA Reviewer PASS — no findings
- Final reviewer gates: AR=PASS; QAR=PASS
- Unresolved reviewer findings: None
- CI status: `PRE_CI_BOOTSTRAP_NA`
- PRE_CI eligible Task ID: `BE-FND-002`
- `GOV-008` prerequisite: DONE — merged to `main` through PR #3
- CI status reason: `CI-FND-001` has not yet been implemented and `BE-FND-002` is an explicitly eligible prerequisite in the approved pre-CI bootstrap chain
- PR-level PRE_CI evidence: SATISFIED — PR #4 records the CI status, eligible Task ID, reason, local validation evidence, reviewer gates, and no-failing-check confirmation
- GitHub PR check runs: 0
- GitHub commit status contexts: 0
- Existing failing CI check: NONE
- Failed CI check waiver: NOT USED — `PRE_CI_BOOTSTRAP_NA` does not waive a failed CI check
- Final closure gate: PASS

Reviewer evidence:

```text
Reviewer: Architecture Reviewer
Result: PASS
Findings: none
```

```text
Reviewer: QA Reviewer
Result: PASS
Findings: none
```

Closure state:

```text
Required reviewer gates: AR=PASS; QAR=PASS.
PR #4 records PRE_CI_BOOTSTRAP_NA, eligible Task ID BE-FND-002, the approved
reason, local validation evidence, and confirmation that no CI check is failing.
GitHub evidence: 0 check runs and 0 commit status contexts at head
a84da779bb352f26636a8b96c3c34db8eaec7a38.
All dependency, acceptance, validation, reviewer, baseline, diff, and PR-level
bootstrap gates are satisfied. BE-FND-002 is closed as DONE.
```

Implementation plan:

```text
Create one package-info.java skeleton for each package named by the acceptance criteria.
Add a focused structural test that verifies all required package skeletons exist,
declare the expected package, and introduce no imports/cross-module dependencies.
Do not add assessment/config/security/storage subpackages, framework dependencies,
API, persistence, configuration, security, provider, or business implementation.
```

Non-contract decision:

```text
Use package-info.java instead of placeholder services/controllers/entities.
Reason: package-info.java makes every package version-controlled and documents its
approved responsibility without inventing later-task implementation. The dependency-
free skeleton is consistent with the Architecture v1.3 layered direction at this stage.
```

Implementation evidence:

```text
Added package-info.java for exactly the 14 acceptance packages:
auth, user, onboarding, vocabulary, learning, personalization, quiz, progress,
gamification, notification, ai, admin, audit, common.
Each skeleton is dependency-free and contains only package responsibility
documentation plus its package declaration.
Added ModulePackageStructureTests to verify every required package skeleton,
its canonical package declaration, and absence of imports in the skeleton.
No dependency was added and pom.xml remains unchanged.
```

Implementation-side validation evidence:

```text
.\backend\mvnw.cmd -f backend\pom.xml clean verify
→ BUILD SUCCESS
→ Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
→ Context smoke test: PASS
→ Module package structure tests: PASS (2 tests)

python tools/baseline_audit.py
→ BASELINE AUDIT: PASS

python -m py_compile tools/baseline_audit.py
→ PASS

git diff --check
→ PASS

git status --short --untracked-files=all, git diff --stat, and git diff
→ INSPECTED
```

Change impact:

```text
Change: Add the BE-FND-002 modular-monolith root package skeleton and structural tests.
Why: Satisfy the approved BE-FND-002 acceptance criteria.
Affected documents: MASTER_BACKLOG.md and EXECUTION_LOG.md lifecycle/evidence only.
Affected API/OpenAPI: None.
Affected database/Flyway: None.
Affected security: None.
Affected clients: None.
Migration: None.
Tests: Build smoke test plus focused module package structure tests.
Backward compatibility: Preserved; no runtime or contract behavior changed.
```

Final acceptance and closure validation:

```text
.\backend\mvnw.cmd -f backend\pom.xml clean verify
→ BUILD SUCCESS
→ Tests run: 3, Failures: 0, Errors: 0, Skipped: 0

Required 14 package/module skeletons: PASS
Package declarations: PASS
Dependency-free skeleton: PASS
Architecture Reviewer: PASS — findings none
QA Reviewer: PASS — findings none
PR #4 PRE_CI_BOOTSTRAP_NA evidence: PASS
Existing failing CI check: NONE
Unresolved blockers: NONE
```

### DB-FND-001 — Dựng PostgreSQL local + Docker Compose

- Status: DONE
- Status history: TODO → READY → IN_PROGRESS → BLOCKED → IN_REVIEW → DONE
- Branch: `chore/DB-FND-001-postgresql-docker-compose`
- Commit: `de8bbb63884b27392f8ceafd08a63647ec28d0e2`
- Pull Request: #6 — `chore(DB-FND-001): add local PostgreSQL Docker Compose`
- Pull Request state at evidence sync: OPEN; non-draft; mergeable state CLEAN
- Baseline provenance: `baseline-v1-implementation-ready-r1` (`34362780eb7ffeb9391ade95220cf895a4592f70`)
- Dependencies: `BE-FND-001` DONE
- Priority: P0
- Required reviewers: Database Reviewer, QA Reviewer
- Acceptance: Local PostgreSQL start/stop reproducible; credential lấy từ env; healthcheck hoạt động.
- Required tests: Theo global DoD + acceptance
- Source documents checked: Database Schema v1.6; System Architecture v1.3; Technical Specification v1.2; Backend Technical Specification v1.3
- Started at: 2026-09-03
- Ready for review: 2026-09-03
- Closed at: 2026-09-03
- Contract changes: None
- Implementation/reviewer blockers: None
- Reviewer results: Database Reviewer PASS — no findings; QA Reviewer PASS — no findings
- Final reviewer gates: DBR=PASS; QAR=PASS
- Unresolved reviewer findings: None
- CI status: `PRE_CI_BOOTSTRAP_NA`
- PRE_CI eligible Task ID: `DB-FND-001`
- `GOV-008` prerequisite: DONE — merged to `main` through PR #3
- CI status reason: `CI-FND-001` has not yet been implemented and `DB-FND-001` is an explicitly eligible prerequisite in the approved pre-CI bootstrap chain
- PR-level PRE_CI evidence: SATISFIED — PR #6 records the CI status, eligible Task ID, reason, local validation evidence, reviewer gates, and no-failing-check confirmation
- GitHub PR check runs: 0
- GitHub commit status contexts: 0
- Existing failing CI check: NONE
- Failed CI check waiver: NOT USED — `PRE_CI_BOOTSTRAP_NA` does not waive a failed CI check
- Unresolved blockers: None
- Final closure gate: PASS
- Historical validation blocker: Docker engine was initially unavailable; resolved by starting the installed Docker Desktop engine. Host port 5432 was unavailable, so the isolated runtime validation used the documented `POSTGRES_PORT=55432` override.

Reviewer evidence:

```text
Reviewer: Database Reviewer
Result: PASS
Findings: none
```

```text
Reviewer: QA Reviewer
Result: PASS
Findings: none
```

Implementation plan:

```text
Add a root Compose definition containing only the local PostgreSQL service and a named
data volume. Require database/user/password through environment interpolation, publish
the port on loopback by default, and use pg_isready for container health.
Add a tracked secret-free .env.example plus reproducible setup/start/health/stop/restart/
down documentation. Do not add Flyway, schema, JPA, backend datasource, Redis, MinIO,
Admin Web, production deployment, backup/restore, or later-task infrastructure.
```

Non-contract decision:

```text
Use Docker Official Image postgres:16.15-alpine3.24. The approved baseline requires
PostgreSQL but does not lock a version; PostgreSQL 16 remains supported and the exact
minor/Alpine tag avoids an unbounded latest/major-only image drift.
```

Implementation evidence:

```text
Added compose.yml with one PostgreSQL service, an exact postgres:16.15-alpine3.24
image tag, a named persistent volume, loopback-only default port binding, required
POSTGRES_DB/POSTGRES_USER/POSTGRES_PASSWORD environment interpolation, and a pg_isready
healthcheck.
Added .env.example with non-secret local placeholders; actual .env remains ignored.
Updated README.md with reproducible setup, config validation, start, health inspection,
stop/restart, down, and explicit destructive volume-reset instructions.
No Flyway migration, schema, backend datasource, dependency, Redis, MinIO, Admin Web,
production deployment, or backup/restore implementation was added.
```

Validation evidence:

```text
docker compose config --quiet without .env
→ EXPECTED FAIL; required POSTGRES_DB, POSTGRES_USER, and POSTGRES_PASSWORD enforced

docker compose --env-file .env.example config --quiet
→ PASS

docker compose --env-file .env.example config --images
→ postgres:16.15-alpine3.24

docker manifest inspect postgres:16.15-alpine3.24
→ PASS; official image tag resolves

Initial runtime attempt
→ ENVIRONMENT BLOCKED; Docker API socket was initially unavailable
→ RESOLVED by starting the installed Docker Desktop engine

Runtime validation project: english-ai-coach-db-fnd-001-validation
Environment override: POSTGRES_PORT=55432 because host port 5432 was unavailable
docker compose up -d --wait postgres
→ PASS; container state healthy
pg_isready
→ PASS; accepting connections
psql SELECT 1
→ PASS
docker compose stop postgres
→ PASS; container state exited
docker compose up -d --wait postgres
→ PASS; container returned to healthy
psql SELECT 1 after restart
→ PASS
docker compose down --volumes --remove-orphans
→ PASS; isolated validation container, network, and named volume removed

.\backend\mvnw.cmd -f backend\pom.xml clean verify
→ BUILD SUCCESS; 3 tests, 0 failures, 0 errors, 0 skipped

Compose static assertions
→ PASS; exact image, loopback binding, required env interpolation, named volume,
  and healthcheck are present in the rendered model

python tools/baseline_audit.py
→ BASELINE AUDIT: PASS

python -m py_compile tools/baseline_audit.py
→ PASS

git diff --check
→ PASS

git status --short --untracked-files=all, git diff --stat, and git diff
→ INSPECTED
```

Acceptance state:

```text
Local PostgreSQL start: SATISFIED
Healthcheck reaches healthy: SATISFIED
pg_isready and SQL connectivity: SATISFIED
Stop and restart reproducibility: SATISFIED
Credentials sourced from env: SATISFIED; missing required variables fail Compose render
Persistent named volume wiring: SATISFIED
Implementation-side blockers: NONE
Reviewer gates: DBR=PASS; QAR=PASS
PR-level PRE_CI_BOOTSTRAP_NA evidence: SATISFIED in PR #6
Existing failing CI check: NONE
Task status: DONE
```

Change impact:

```text
Change: Add local PostgreSQL Docker Compose foundation and usage documentation.
Why: Satisfy DB-FND-001 local database lifecycle/env credential/healthcheck acceptance.
Affected documents: README.md, MASTER_BACKLOG.md, and EXECUTION_LOG.md.
Affected API/OpenAPI: None.
Affected database schema/Flyway: None.
Affected backend runtime/configuration: None.
Affected security: Local credential values remain external in ignored .env; service
binds to 127.0.0.1 by default.
Affected clients: None.
Migration: None.
Backward compatibility: Preserved.
```

### DB-FND-002 — Tạo Flyway schema baseline cho 34 bảng

- Status: DONE
- Status history: TODO → READY → IN_PROGRESS → IN_REVIEW → BLOCKED → IN_REVIEW → DONE
- Branch: `feat/DB-FND-002-flyway-schema-baseline`
- Pull Request: #7 — `feat(DB-FND-002): add Flyway schema baseline`
- Baseline provenance: `baseline-v1-implementation-ready-r1` (`34362780eb7ffeb9391ade95220cf895a4592f70`)
- Dependencies: `DB-FND-001` DONE
- Priority: P0
- Required reviewers: Database Reviewer, Architecture Reviewer, QA Reviewer
- Acceptance: Fresh DB migrate từ zero tạo đúng 34 bảng theo DB v1.6; migration append-only; FK/unique/check/not-null đầy đủ.
- Required tests: Theo global DoD + acceptance
- Source documents checked: Database Schema v1.6; System Architecture v1.3; Technical Specification v1.2; Backend Technical Specification v1.3
- Started at: 2026-09-08
- Ready for review: 2026-09-08
- Closed at: 2026-09-08
- Current reviewer state: DBR=PASS; AR=PASS; QAR=PASS after focused re-review
- Historical reviewer state: QAR=FAIL for `QA-DB-FND-002-001`
- Final reviewer gates: DBR=PASS; AR=PASS; QAR=PASS
- Unresolved reviewer findings: NONE
- CI status: `PRE_CI_BOOTSTRAP_NA`
- PRE_CI eligible Task ID: `DB-FND-002`
- `GOV-008` prerequisite: DONE — merged to `main` through PR #3
- CI status reason: `CI-FND-001` has not yet been implemented and `DB-FND-002` is an explicitly eligible prerequisite in the approved pre-CI bootstrap chain
- PR-level PRE_CI evidence: SATISFIED — PR #7 records the CI status, eligible Task ID, reason, local validation evidence, reviewer gates, and no-failing-check confirmation
- Existing failing CI check: NONE
- Failed CI check waiver: NOT USED — `PRE_CI_BOOTSTRAP_NA` does not waive a failed CI check
- Final repository evidence synchronization: COMPLETE
- Contract changes: None — implementation realizes the approved Database Schema v1.6 baseline
- Unresolved blockers: NONE
- Final closure gate: PASS

Implementation plan:

```text
Add the first repository Flyway migration as a final-state V1 baseline because no
migration has previously existed or been applied from this repository. Create all 34
canonical Database Schema v1.6 tables in FK-safe order with the documented columns,
types, defaults, PK/FK/UNIQUE/CHECK/NOT NULL constraints.

Add only the Spring Boot Flyway starter, PostgreSQL Flyway database module, and
PostgreSQL runtime driver needed to execute the migration. Keep performance/partial
indexes in DB-FND-003, seed data in DB-FND-004, JPA mapping in BE-FND-004, and the
Testcontainers harness in QA-FND-001.
```

Non-contract decisions:

```text
Use V1__create_schema_baseline.sql rather than manufacturing the document's historical
V1..V40 example sequence: the repository has no existing migration history, and
Technical Specification v1.2 explicitly permits V1__baseline.sql for a fresh baseline.

Foreign keys use PostgreSQL default NO ACTION because Database Schema v1.6 leaves user
deletion/cascade policy as a later business decision and prohibits blind cascades.
```

Implementation evidence:

```text
Added backend/src/main/resources/db/migration/V1__create_schema_baseline.sql.
The migration declares exactly 34 canonical tables and includes all documented
declarative PK/FK/UNIQUE/CHECK/NOT NULL constraints, JSONB/TIMESTAMPTZ/UUID types,
optimistic-lock version columns, idempotency response-status guard, answer-quality
correctness invariant, Daily Plan item guards, and approved defaults.

Added spring-boot-starter-flyway, flyway-database-postgresql, and the PostgreSQL runtime
driver through Spring Boot dependency management. The existing context smoke test
disables datasource/Flyway auto-configuration only for that database-independent test.
Added a unit test that requires the migration to declare exactly the canonical 34-table
set without duplicate CREATE TABLE statements.

Updated README.md with migration location, immutable/append-only rule, datasource
environment variables, automatic startup migration, flyway_schema_history, and explicit
later-task boundaries.
```

Validation evidence:

```text
.\backend\mvnw.cmd -f backend\pom.xml clean verify
→ BUILD SUCCESS; 4 tests, 0 failures, 0 errors, 0 skipped

Fresh isolated PostgreSQL 18.6 cluster on 127.0.0.1:55433
→ initialized with trust authentication inside ignored .agent-tmp for validation only
→ pg_isready PASS

java -jar ... --spring.main.web-application-type=none --spring.datasource.*
→ Flyway validated 1 migration
→ migrated empty public schema to V1 successfully

PostgreSQL catalog verification
→ 34 canonical application tables exactly
→ 48 foreign keys
→ 21 UNIQUE constraints
→ 16 CHECK constraints
→ 34 application primary keys; flyway_schema_history adds its own primary key
→ flyway_schema_history version=1, success=true

Second application/Flyway run
→ validation PASS; schema at V1; up to date; no migration necessary

Migration/source column-set audit
→ PASS; all 34 table column sets match Database Schema v1.6 exactly
→ 34 distinct CREATE TABLE statements; 0 INSERT and 0 CREATE INDEX statements

Maven dependency tree
→ PASS; Spring Boot Flyway starter 4.1.1, Flyway PostgreSQL 12.4.0,
  and PostgreSQL driver 42.7.13 resolved through dependency management

docker compose --env-file .env.example config --quiet
→ PASS

python tools/baseline_audit.py
→ BASELINE AUDIT: PASS

python -m py_compile tools/baseline_audit.py
→ PASS

git diff --check and new-file whitespace check
→ PASS

git status --short --untracked-files=all, git diff --stat, and full diff
→ INSPECTED

Validation environment cleanup
→ temporary PostgreSQL cluster stopped and removed
→ Docker Desktop attempt was shut down after its host-environment startup failure
```

Historical independent QA finding and focused remediation:

```text
Finding ID: QA-DB-FND-002-001
Reviewer: QA Reviewer
Historical QA result: FAIL
Severity: MEDIUM
Blocking: YES
Finding classification: Runtime/environment evidence blocker; no product-code defect established
Finding: The prior validation runtime had been removed, so QA could not independently
reproduce the fresh Flyway migration, PostgreSQL catalog/constraint checks, and repeat run.
Finding status: OPEN — focused remediation complete; independent QA re-review pending

Focused remediation (2026-09-08):
→ Attempted to restore the DB-FND-001 Docker Compose PostgreSQL 16.15 runtime first.
→ Docker Desktop could not start its Linux engine because the host retained an
  inaccessible stale sailor-ingest.sock reparse point; repository Compose configuration
  was not the cause.
→ Restored an isolated, fresh PostgreSQL 18.6 validation runtime at
  127.0.0.1:55432, database/user english_ai_coach, with local trust authentication.
→ Runtime data is under ignored .agent-tmp/db-fnd-002-qa-postgres and remains running
  for focused QA re-review.
→ QA access command:
  D:\Database\PostgreSQL\18\bin\psql.exe -h 127.0.0.1 -p 55432
  -U english_ai_coach -d english_ai_coach

Fresh Flyway migration:
→ Empty public schema detected.
→ V1__create_schema_baseline.sql validated and applied successfully.
→ flyway_schema_history contains exactly one successful V1 migration.

PostgreSQL catalog and constraint verification:
→ 34 canonical application tables exactly.
→ 48 FOREIGN KEY, 21 UNIQUE, 16 CHECK, 221 NOT NULL, and 34 PRIMARY KEY constraints.
→ 0 unvalidated constraints.

Repeat-run validation:
→ Flyway validated V1 and reported schema public at V1 and up to date.
→ No migration was re-applied; 34 application tables and one successful history row remain.

Product-code/config/test changes for remediation: NONE
Implementation-side remediation status: COMPLETE
Focused QA re-review result: PASS
Final finding status: RESOLVED
Unresolved findings: NONE
Required next gate: NONE — PR-level PRE_CI_BOOTSTRAP_NA evidence satisfied by PR #7
```

Reviewer evidence:

```text
Reviewer: Database Reviewer
Result: PASS
Findings: none
```

```text
Reviewer: Architecture Reviewer
Result: PASS
Findings: none
```

```text
Reviewer: QA Reviewer
Historical result: FAIL
Finding: QA-DB-FND-002-001
Severity: MEDIUM
Blocking: YES
Original status: OPEN
Focused re-review result: PASS
Final finding status: RESOLVED
Unresolved findings: NONE
```

Acceptance state:

```text
Fresh PostgreSQL migration from zero: SATISFIED
Exactly 34 Database Schema v1.6 tables: SATISFIED
Migration append-only foundation: SATISFIED; first immutable V1 migration established
Documented PK/FK/UNIQUE/CHECK/NOT NULL constraints: SATISFIED
Implementation-side blockers: NONE
Reviewer gates: DBR=PASS; AR=PASS; QAR=PASS
Historical QA FAIL: PRESERVED
QA-DB-FND-002-001: RESOLVED after focused QA re-review PASS
Unresolved reviewer findings: NONE
PR-level PRE_CI_BOOTSTRAP_NA evidence: SATISFIED by PR #7
Final repository evidence synchronization: COMPLETE
Unresolved blockers: NONE
Task status: DONE
```

Change impact:

```text
Change: Add executable Flyway baseline for the approved PostgreSQL schema.
Why: Satisfy DB-FND-002 fresh-database migration and integrity acceptance.
Affected documents: README.md, MASTER_BACKLOG.md, and EXECUTION_LOG.md.
Affected API/OpenAPI: None.
Affected database: Yes — creates the approved 34-table Database Schema v1.6 baseline.
Affected Flyway: Yes — establishes immutable V1 migration history.
Affected security: No contract change; no credential value is committed.
Affected business rules: No change; documented database invariants are enforced.
Affected clients: None.
Migration: V1__create_schema_baseline.sql for an empty database.
Backward compatibility: Preserved; no prior repository Flyway migration exists.
```

### Governance Amendment / Pre-Foundation

#### GOV-008 — Clarify Pre-CI Bootstrap Gate

- Owner approval: APPROVED — project owner explicitly approved `GOV-008` and the `PRE_CI_BOOTSTRAP` rule on 2026-09-01
- Status: DONE
- Status history: TODO → READY → IN_PROGRESS → IN_REVIEW → BLOCKED → IN_REVIEW → DONE
- Branch: `chore/GOV-008-pre-ci-bootstrap-gate`
- Pull Request: #3 — `chore(GOV-008): clarify pre-CI bootstrap gate`
- Dependencies: `GOV-007` DONE
- Milestone placement: M1 — Governance Amendment / Pre-Foundation
- M0 impact: None — historical M0 remains closed at GOV-001..GOV-007, 7/7, 100%, PASS
- Bootstrap deadlock: IDENTIFIED — Global DoD required `CI PASS` for prerequisite tasks needed to build `CI-FND-001`
- Resolution: narrowly scoped `PRE_CI_BOOTSTRAP_NA` CI state introduced
- Eligible Task IDs: `GOV-008`, `BE-FND-001`, `BE-FND-002`, `DB-FND-001`, `DB-FND-002`, `BE-FND-004`, `BE-FND-005`, `BE-FND-007`, `QA-FND-001`, `QA-FND-002`
- CI status: `PRE_CI_BOOTSTRAP_NA`
- PRE_CI eligible Task ID: `GOV-008`
- CI status reason: `GOV-008` is an approved eligible governance prerequisite and the required pipeline does not exist before `CI-FND-001`
- PR-level PRE_CI evidence: PR #3 explicitly records the CI status, eligible Task ID, reason, and local validation evidence
- GitHub PR CI checks: 0 — `CI-FND-001` does not exist yet
- Existing failing CI check: NONE
- Failing existing CI check waiver: PROHIBITED
- `CI-FND-001` final gate: REAL CI PASS REQUIRED
- Expiry: immediately after `CI-FND-001` is DONE and merged into `main`
- Post-expiry Global DoD: ACTUAL CI PASS REQUIRED
- M1 exit gate: ACTUAL CI PASS REQUIRED
- Validation: `python tools/baseline_audit.py` PASS; `python -m py_compile tools/baseline_audit.py` PASS; targeted planning checks PASS; final acceptance checks PASS; `git diff --check` PASS; status/stat/full diff inspected
- Required reviewers: AR,QAR
- Current reviewer state: AR PASS; QAR PASS
- Architecture initial review result: FAIL — `ARCH-GOV-008-001`
- Architecture finding severity: HIGH — Blocking
- Architecture finding root cause: `GOV-008` was incorrectly inserted retroactively into the already closed M0 milestone
- Architecture remediation: moved `GOV-008` to M1 governance/pre-foundation scope; restored all historical M0 representations to GOV-001..GOV-007 / 7 tasks / 7/7 / 100% / PASS
- Architecture re-review result: PASS
- Architecture finding status: `ARCH-GOV-008-001` RESOLVED
- Architecture remediation regression: NO
- Architecture recommendation: APPROVE
- Architecture re-review required: NO — completed with PASS
- QA review result: PASS
- QA review findings: None
- QA recommendation: APPROVE
- All required reviewer gates: PASS
- Unresolved reviewer findings: None
- Blocker status: RESOLVED by focused remediation and Architecture re-review PASS
- Unresolved blockers: None
- Final closure gate: PASS — reviewer gates and PR-level `PRE_CI_BOOTSTRAP_NA` evidence are satisfied
- Merge ordering: `GOV-008` must merge before PR #2 / `BE-FND-001` may use `PRE_CI_BOOTSTRAP_NA`; `BE-FND-001` dependency metadata remains unchanged
- Non-contract decision: approved governance/process clarification only
- Product/technical contract impact: None
- API/OpenAPI impact: None
- Database/Flyway impact: None
- Security/business/client/architecture impact: None
- Baseline tags: Unchanged
- `baseline-v1-implementation-ready-r1` provenance: tag object `e3884521c3d497094961d015b7b32d12a8e55650` remains pointed at commit `34362780eb7ffeb9391ade95220cf895a4592f70`
- Verified at: 2026-09-01

Evidence:

```text
PRE_CI_BOOTSTRAP_NA is limited to the ten explicitly approved prerequisite tasks.
It requires completed dependencies, acceptance, applicable local tests and
validations, required reviewer PASS, baseline_audit PASS, explicit PR evidence,
and confirmation that no existing CI check is failing.

CI-FND-001 cannot use the exception for its final gate. The exception expires
after CI-FND-001 is DONE and merged into main. M1 exit still requires actual CI PASS.

BASELINE AUDIT: PASS.
baseline_audit.py byte-compilation: PASS.
Targeted planning checks: PASS — M0 history, M1 placement, task counts,
dependency graph, PRE_CI eligible list, M1 CI exit gate, BE-FND-001 metadata,
branch state, scope, and baseline-v1-implementation-ready-r1 provenance verified.
git diff --check: PASS.
git status --short, git diff --stat, and full git diff: inspected.
GOV-008 final acceptance checks: PASS — PRE_CI scope/list/invariants, PR template
evidence fields, reviewer gates, historical M0 integrity, and planning integrity.
Architecture initial FAIL and HIGH — Blocking finding ARCH-GOV-008-001 remain
in history. Focused remediation was re-reviewed with PASS; regression: NO;
ARCH-GOV-008-001: RESOLVED; recommendation: APPROVE.
QA Reviewer result: PASS; findings: none; recommendation: APPROVE.
AR and QAR are PASS. Required PR-level PRE_CI_BOOTSTRAP_NA evidence is
satisfied in PR #3: CI status PRE_CI_BOOTSTRAP_NA; eligible Task ID GOV-008;
reason and local validation evidence recorded; GitHub CI checks: 0; existing
failing CI check: NONE. GOV-008 is closed as DONE.
```

#### Current M1 milestone status after GOV-008 closure

| Milestone | Execution complete | Total | Execution progress | DoD status |
| --- | ---: | ---: | ---: | --- |
| M1 — Foundation Ready | 1 | 28 | 3.6% | IN_PROGRESS |

#### GOV-009 — Simplified Main-Branch Task Workflow

- Owner authorization: APPROVED — repository owner explicitly authorized this one-time governance transition on 2026-09-09
- Status: DONE
- Status history: TODO → IN_PROGRESS → DONE
- Execution location: direct uncommitted worktree on current `main`, as an explicit one-time exception
- Dependencies: `GOV-008` DONE
- Milestone placement: M1 — Governance Amendment / Pre-Foundation
- Current executable task total: 177
- Current M1 task total: 29
- M1 execution before GOV-009 closure: 5 / 29 (17.2%)
- Current M1 execution complete after GOV-009 closure: 6 / 29 (20.7%)
- Historical milestone snapshots/totals: PRESERVED — including the GOV-008 closure snapshot of 1 / 28 (3.6%)
- Required reviewers: AR,QAR
- Historical Architecture review #1: FAIL
- Architecture focused re-review #2: FAIL
- Architecture focused re-review #3: FAIL
- Architecture focused re-review #4: PASS
- Architecture Reviewer current gate: PASS
- QA Reviewer: PASS — findings NONE
- Resolved Architecture findings: `ARCH-GOV-009-001`, `ARCH-GOV-009-002`, `ARCH-GOV-009-003`, `ARCH-GOV-009-004`, `ARCH-GOV-009-005`, `ARCH-GOV-009-006` — reviewer-confirmed Final Status RESOLVED; not reopened
- Unresolved reviewer findings: NONE
- DONE: YES
- Closure mode: `OWNER_AUTHORIZED_GOVERNANCE_TRANSITION` — applies only to GOV-009; not `PRE_CI_BOOTSTRAP_NA`; not `CI PASS`; eligible list unchanged; non-reusable; expires permanently after GOV-009 push; cannot waive failing CI
- Transition gate: `OWNER_AUTHORIZED_GOVERNANCE_TRANSITION`
- Applicable Task: `GOV-009` only
- Repository-owner authorization: CONFIRMED
- Reusable: NO
- `PRE_CI_BOOTSTRAP_NA`: NOT USED
- CI PASS substitution: NO
- Failed-check waiver: NOT USED
- Closure gates: PASS — `GOV-008` DONE; owner CONFIRMED; AR PASS; QAR PASS; unresolved findings NONE; governance acceptance PASS; baseline audit PASS; py_compile PASS; `git diff --check` PASS; scope audit PASS; secret/generated-file audit PASS; baseline tag integrity PASS; existing failing CI check NONE
- Existing failing CI check: NONE — current `main` HEAD has 0 check runs and 0 status contexts; no failed-check waiver used
- Effective point: only after AR PASS, QAR PASS, unresolved findings = `NONE`, `GOV-009` finalization to `DONE`, and successful push of the `GOV-009` commit to `main`
- Governance commit: PENDING
- Push to `origin/main`: PENDING
- Simplified workflow effective: NO — uncommitted GOV-009 changes do not activate it
- New default after effective point: PLAN → IMPLEMENT → TEST → REVIEW → DONE → one final commit on `main` → push `main`
- New lifecycle after effective point: TODO → IN_PROGRESS → DONE; `BLOCKED` from `IN_PROGRESS` for an actual blocker; `READY` and `IN_REVIEW` preserved for historical/legacy use
- Active-task limit: one direct-main task; a parked grandfathered task does not count
- Admission before CI effective: only an explicitly PRE_CI-eligible task with all admission gates, or `CI-FND-001` under `ACTUAL_CI_BOOTSTRAP`; non-eligible ordinary task = `INVALID_BEFORE_CI` / admission `BLOCKED`
- Admission after CI effective: CI mode `ACTUAL_CI_REPOSITORY_HEALTH`; latest `origin/main` must be `HEALTHY`; `CI_PENDING` and repository health `BLOCKED` both prohibit PLAN / `TODO → IN_PROGRESS`
- After each post-CI direct-main push: repository health immediately `CI_PENDING`; PASS → `HEALTHY`; FAIL → repository health `BLOCKED`; task lifecycle remains separate
- Published-main recovery: `PUBLISHED_MAIN_RECOVERY` is not a backlog task or PLAN admission; a required CI failure keeps the originating task `DONE`, sets repository health `BLOCKED`, and opens recovery under the same originating Task ID
- Recovery admission exception: only minimal incident-bound `FIX_FORWARD` or `REVERT` work may proceed while repository health is `BLOCKED`; no unrelated task or `TODO → IN_PROGRESS`; `OWNER_AUTHORIZED_GOVERNANCE_TRANSITION` and `PRE_CI_BOOTSTRAP_NA` are prohibited as recovery mechanisms
- Recovery states/evidence: `OPEN → VALIDATING → CI_PENDING → CLOSED` (optional recovery `BLOCKED`), traced by originating Task ID, failing `origin/main` SHA/check, strategy, recovery SHA, affected tests/reviewers/findings and final CI
- Recovery publication: affected TEST/reviewer gates PASS + unresolved recovery findings `NONE` → traceable recovery commit/fast-forward push → repository/recovery `CI_PENDING`; PASS → `HEALTHY`/`CLOSED`; repeated FAIL → `BLOCKED`/`OPEN` under the same incident
- Commit-count rule: `ONE TASK = ONE FINAL COMMIT` remains normal; only CI-FND-001 bootstrap and actual-failure `PUBLISHED_MAIN_RECOVERY` are exceptions
- Grandfathered task: `BE-FND-003` / PR #5 remains on `feat/BE-FND-003-application-profiles-config`; PR remains OPEN; lifecycle/status and AR/SR/QAR evidence unchanged; actual CI required; `PRE_CI_BOOTSTRAP_NA` not eligible; no GOV-009-only cherry-pick
- PRE_CI rule: eligible list unchanged; new direct-main eligible tasks use repository-level evidence, while grandfathered tasks retain PR-level evidence; failing CI is never waived
- PRE_CI expiry: irreversible when CI-FND-001 bootstrap actual CI PASS + task `DONE` + closure commit successfully pushed `origin/main`; closure/latest-main `CI_PENDING` or `BLOCKED` never reactivates PRE_CI
- `CI-FND-001`: cannot use `PRE_CI_BOOTSTRAP_NA` or `OWNER_AUTHORIZED_GOVERNANCE_TRANSITION`; documented two-commit bootstrap/push/actual-CI/closure-push exception
- Post-CI rule: remote CI runs after direct-main push as repository-health gate; PASS leaves task lifecycle unchanged and repository/main `HEALTHY`; FAIL blocks repository/main without rewriting the already-`DONE` task until fix-forward or revert, affected retest/re-review, remediation push and remote CI PASS
- Direct-main safety: task `DONE`, dependency/scope/tests/reviewers/findings/audit/diff/secret/generated/tag gates PASS before commit; no force-push, published-main rebase, destructive reset/clean, history rewrite, tag mutation, known failure, or mixed-task commit
- Skill migration: PENDING — installed Codex skills remain unchanged and are explicitly excluded from this finalization
- Product/technical contract impact: None
- API/OpenAPI impact: None
- Database/Flyway impact: None
- Client behavior impact: None
- Baseline tags: PASS — both immutable tag objects and peeled targets are unchanged (`baseline-v1-implementation-ready` → tag `5ccf0650d81ffbbd3d96eb523d097e0b9b022308`, commit `ff6e13f4fe1444879b28d846801d0caa555bf4a7`; `baseline-v1-implementation-ready-r1` → tag `e3884521c3d497094961d015b7b32d12a8e55650`, commit `34362780eb7ffeb9391ade95220cf895a4592f70`)
- Finalization validation: PASS — `python tools/baseline_audit.py`; `python -m py_compile tools/baseline_audit.py`; `git diff --check`; planning integrity 177 unique tasks / 0 missing dependencies / 0 cycles / M1 29 with 6 DONE (20.7%); governance/reviewer/effective-point consistency searches and full diff inspected; scope/secret/generated-file audits PASS; baseline tags and installed skill unchanged; no existing failing CI check
- Commit/push/branch/tag mutation: NONE

Architecture finding chronology and remediation:

```text
Historical Architecture review #1: FAIL

ARCH-GOV-009-001
Severity: HIGH
Blocking: YES
Original Status: OPEN
Remediation: Added the explicit OWNER_AUTHORIZED_GOVERNANCE_TRANSITION closure
gate for GOV-009 only, including owner, reviewer, finding, validation, secret/
generated-file, baseline-tag and no-failing-CI conditions. It is explicitly
separate from PRE_CI_BOOTSTRAP_NA and CI PASS, is non-reusable, and expires
permanently after the GOV-009 push.
Final Status: RESOLVED — confirmed by Architecture focused re-review #2.

ARCH-GOV-009-002
Severity: HIGH
Blocking: YES
Original Status: OPEN
Remediation: Split Global DoD semantics. Legacy/grandfathered tasks retain
CI-before-DONE. New direct-main tasks reach DONE through pre-publish local/test/
reviewer/applicable PRE_CI gates; post-push remote CI is repository health.
Remote failure blocks repository/main without rewriting the completed task.
Final Status: RESOLVED — confirmed by Architecture focused re-review #2.

ARCH-GOV-009-003
Severity: MEDIUM
Blocking: YES
Original Status: OPEN
Remediation: Replaced the active CI-FND-001 "fail thì không merge" acceptance
with IN_PROGRESS → bootstrap commit/push → actual CI PASS → DONE → closure
commit. CI-FND-001 cannot use PRE_CI_BOOTSTRAP_NA or
OWNER_AUTHORIZED_GOVERNANCE_TRANSITION.
Final Status: RESOLVED — confirmed by Architecture focused re-review #2.

Architecture focused re-review #2: FAIL

ARCH-GOV-009-004
Severity: HIGH
Blocking: YES
Original Status: OPEN
Remediation: Added one mandatory admission gate for every post-GOV-009 task.
Before CI-FND-001 effective, only a positively listed PRE_CI-eligible task with
all PRE_CI admission gates or CI-FND-001 under ACTUAL_CI_BOOTSTRAP may enter
PLAN; ordinary non-eligible tasks use INVALID_BEFORE_CI and are blocked. After
CI effective, ACTUAL_CI_REPOSITORY_HEALTH requires latest origin/main HEALTHY;
CI_PENDING and repository health BLOCKED both prohibit TODO → IN_PROGRESS.
Every direct-main push sets CI_PENDING until PASS or FAIL. Task BLOCKED and
REPOSITORY HEALTH = BLOCKED are explicitly distinct.
Final Status: RESOLVED — confirmed by Architecture focused re-review #3.

ARCH-GOV-009-005
Severity: MEDIUM
Blocking: YES
Original Status: OPEN
Remediation: Replaced active merge-based PRE_CI expiry with the exact
CI-FND-001 effective point: mandatory bootstrap actual CI PASS, task DONE, and
successful closure-commit push to origin/main. PRE_CI then expires permanently;
closure/latest-main CI_PENDING or BLOCKED never reactivates it and blocks new
task admission until repository health returns HEALTHY.
Final Status: RESOLVED — confirmed by Architecture focused re-review #3.

Architecture focused re-review #3: FAIL

ARCH-GOV-009-006
Severity: HIGH
Blocking: YES
Original Status: OPEN
Remediation: Added PUBLISHED_MAIN_RECOVERY as a non-backlog operational state
machine for actual CI failure on an already-published direct-main commit. The
originating task remains DONE while repository health is BLOCKED and recovery
is OPEN. Only incident-bound FIX_FORWARD or REVERT may proceed; unrelated PLAN
and TODO → IN_PROGRESS remain prohibited. Recovery records the originating Task
ID, failing origin/main SHA/check, strategy, affected tests/reviewers, recovery
SHA/findings and final CI. Affected gates must pass before a fast-forward
recovery push sets repository/recovery CI_PENDING. PASS closes recovery and
restores HEALTHY; repeated FAIL returns to BLOCKED/OPEN under the same incident.
The recovery commit and CI-FND-001 bootstrap are the only narrow exceptions to
ONE TASK = ONE FINAL COMMIT; PRE_CI remains permanently expired.
Final Status: RESOLVED — confirmed by Architecture focused re-review #4.
New findings: NONE.

Architecture focused re-review #4: PASS
Final Architecture Reviewer gate: PASS

QA Review: PASS
Findings: NONE
Final QA Reviewer gate: PASS

Final reviewer state:
AR = PASS
QAR = PASS
Unresolved findings = NONE
```
