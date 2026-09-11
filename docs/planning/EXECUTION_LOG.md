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
| 2026-09-10 | QA-FND-001 | OPEN | `DB-QA-FND-001-001` — Severity: MEDIUM; Blocking: YES; Original Status: OPEN. `PostgreSqlHarnessIntegrationTests` asserted exactly one applied Flyway migration, so the harness would reject a valid append-only V2+ history. | Replaced the total-count assertion with canonical V1 presence/script/`SUCCESS` verification and a no-maximum check that every currently resolved/applied migration is `SUCCESS`; finding remains OPEN pending focused Database Reviewer re-review. |
| 2026-09-10 | QA-FND-001 | RESOLVED | `DB-QA-FND-001-001` — historical metadata preserved as Severity: MEDIUM; Blocking: YES; Original Status: OPEN. | Focused Database re-review PASS confirmed the append-only-compatible Flyway assertion remediation; final finding Status: RESOLVED; new findings: NONE. Historical Database Reviewer FAIL remains preserved. |
| 2026-09-11 | BE-FND-005 | OPEN | `QA-BE-FND-005-001` — Severity: HIGH; Blocking: YES; Original Status: OPEN. Missing required parameters, request parameter/path-variable type mismatches, unsupported HTTP methods, and unsupported media types could fall through to `500 INTERNAL_ERROR`. | Added focused canonical Spring MVC exception mappings and real MockMvc dispatcher regression coverage. Remediation is complete, but the finding remains OPEN pending independent focused QA re-review. |
| 2026-09-11 | BE-FND-005 | RESOLVED | `QA-BE-FND-005-001` — historical metadata preserved as Severity: HIGH; Blocking: YES; Original Status: OPEN. | Independent focused QA re-review PASS confirmed all four Spring MVC request-failure mappings and regression coverage; final finding Status: RESOLVED; new findings: NONE. Historical QA Reviewer FAIL remains preserved. |

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
| 2026-09-10 | QA-FND-001 | Database Reviewer | FAIL | `DB-QA-FND-001-001` — Severity: MEDIUM; Blocking: YES; Original Status: OPEN. The harness asserted total applied Flyway migration count equals one and was not append-only V2+ compatible. Focused remediation and Database Reviewer re-review required; finding remains OPEN. |
| 2026-09-10 | QA-FND-001 | QA Reviewer | PASS | Independent QA review verified the `DB-QA-FND-001-001` remediation and 12/12 tests PASS; findings: NONE; QAR=PASS. At this review point the Database finding remained OPEN pending focused Database Reviewer re-review. |
| 2026-09-10 | QA-FND-001 | Database Reviewer | PASS | Focused Database re-review confirmed `DB-QA-FND-001-001` RESOLVED; append-only Flyway compatibility preserved; product code and Flyway migration unchanged; new findings: NONE; DBR=PASS. |
| 2026-09-11 | BE-FND-005 | QA Reviewer | FAIL | `QA-BE-FND-005-001` — Severity: HIGH; Blocking: YES; Original Status: OPEN. Several Spring MVC request failures fell through to the generic `500 INTERNAL_ERROR` handler. Focused remediation and independent QA re-review are required; finding remains OPEN. |
| 2026-09-11 | BE-FND-005 | Architecture Reviewer | PASS | Independent architecture review found no findings; common exception boundary, dependency direction, canonical envelope, scope and later-task boundaries are compliant; recommendation APPROVE. |
| 2026-09-11 | BE-FND-005 | QA Reviewer | PASS | Focused QA re-review confirmed `QA-BE-FND-005-001` RESOLVED; 10 focused MockMvc tests and 22 full backend tests PASS; new findings/regressions: NONE; recommendation APPROVE. Historical QA FAIL remains preserved. |

#### Milestone status

| Milestone                                | Execution complete | Total | Execution progress | DoD status  |
| ---------------------------------------- | -----------------: | ----: | -----------------: | ----------- |
| M0 — Execution Governance                |                  7 |     7 |               100% | PASS        |
| M1 — Foundation Ready                    |                  9 |    29 |              31.0% | IN_PROGRESS |
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

### BE-FND-004 — JPA base conventions: UUID, audit timestamps, enum, @Version

- Status: DONE
- Status history: TODO → IN_PROGRESS → DONE
- Branch/worktree mode: `GOV009_DIRECT_MAIN` — uncommitted `main` worktree
- HEAD at admission: `db574d34f15f8d66f9b8733ac18ac1f6a4ecdc10`
- `origin/main` at admission: `db574d34f15f8d66f9b8733ac18ac1f6a4ecdc10`
- Dependencies: `DB-FND-002` DONE; `BE-FND-002` DONE
- Priority: P0
- Required reviewers: Database Reviewer, Architecture Reviewer, QA Reviewer
- Acceptance: Entity mapping theo schema; UUID/timezone conventions thống nhất; `@Version` cho `user_vocabulary_progress` và `streaks`.
- Required tests: Theo global DoD + acceptance
- Source documents checked: Database Schema v1.6; System Architecture v1.3; Technical Specification v1.2; Backend Technical Specification v1.3
- Started at: 2026-09-09
- Closed at: 2026-09-09
- CI admission mode: `PRE_CI_BOOTSTRAP_NA`
- PRE_CI eligible Task ID: `BE-FND-004`
- CI status reason: `CI-FND-001` chưa effective và `BE-FND-004` nằm tường minh trong eligible prerequisite list
- Remote admission evidence: sau `git fetch`, `main == origin/main`; GitHub commit có 0 check runs và 0 status contexts
- Existing failing CI check: NONE
- Failed CI check waiver: NOT USED
- Unresolved blockers: NONE
- Contract changes: None — triển khai persistence conventions theo approved baseline

Implementation plan:

```text
Add only the Spring Data JPA dependency required by this task. Establish reusable UUID
and audit timestamp mapping conventions, using TIMESTAMPTZ-compatible Java time types
and string-backed enum mapping. Add schema-aligned mappings for the two explicitly
optimistic-locked states, user_vocabulary_progress and streaks, with @Version.

Add focused convention/mapping tests and validate the mappings against the existing
Flyway baseline where the local PostgreSQL runtime is available. Do not add repositories,
controllers, APIs, exception mapping, business algorithms, seed data, indexes, auth,
idempotency service, clock abstraction, or mappings owned by later backlog tasks.
```

Implementation evidence:

```text
Added spring-boot-starter-data-jpa through Spring Boot dependency management.
Added UuidEntity with application-generated GenerationType.UUID identifiers.
Added AuditableEntity with Instant-backed created_at/updated_at mappings for TIMESTAMPTZ.
Added LearningStatus with the canonical NEW/LEARNING/REVIEWING/MASTERED values and
EnumType.STRING mapping on UserVocabularyProgress.status.
Added schema-aligned UserVocabularyProgress and Streak mappings for every column in
the two optimistic-lock tables; both version fields use jakarta.persistence.Version.
Added focused reflection-based convention/mapping tests and README guidance.
No existing Flyway migration was modified; no repository, API or business service added.
```

Validation evidence:

```text
.\backend\mvnw.cmd --no-transfer-progress -f backend\pom.xml clean verify
→ BUILD SUCCESS
→ Tests run: 9, Failures: 0, Errors: 0, Skipped: 0
→ JpaEntityConventionTests: 5 PASS

PostgreSQL runtime validation on 127.0.0.1:55432 / PostgreSQL 18.6
→ pg_isready: accepting connections
→ Flyway validated V1 and reported schema up to date
→ Hibernate 7.4.5.Final initialized the persistence unit
→ spring.jpa.hibernate.ddl-auto=validate: PASS
→ validation runtime left available for independent reviewers

Maven dependency tree
→ spring-boot-starter-data-jpa 4.1.1
→ spring-data-jpa 4.1.1
→ hibernate-core 7.4.5.Final

python tools/baseline_audit.py
→ BASELINE AUDIT: PASS

python -m py_compile tools/baseline_audit.py
→ PASS

git diff --check
→ PASS

Scope audit, untracked whitespace audit, secret audit, generated-file audit,
Flyway immutability and baseline-tag integrity
→ PASS

Full tracked diff and every untracked current-task file
→ INSPECTED
```

Change impact:

```text
Change: Add BE-FND-004 JPA base conventions and the two optimistic-lock mappings.
Why: Satisfy the approved JPA/UUID/timestamp/enum/@Version foundation acceptance.
Affected documents: README.md plus backlog/execution lifecycle evidence.
Affected API/OpenAPI: None.
Affected database/Flyway: No schema change; mappings validate against existing V1.
Affected security: None.
Affected business rules: None.
Affected clients: None.
Migration: None.
Backward compatibility: Preserved.
```

TEST stop state:

```text
Task status: IN_PROGRESS
Implementation and applicable TEST gates: PASS
Self-review/reviewer PASS claimed: NO
Unresolved implementation blockers: NONE
Required independent reviewers next: Database Reviewer, Architecture Reviewer,
QA Reviewer
Commit/push/merge/tag mutation: NONE
```

Independent reviewer evidence:

```text
Database Reviewer task: Review BE-FND-004 database
Task ID: 01a08624-a453-7de0-9357-8e1b742bd88e
Result: PASS
Findings: NONE
Recommendation: APPROVE

Architecture Reviewer task: Rà soát kiến trúc BE-FND-004
Task ID: 01a0862b-ec5a-7191-a3ab-91adbd69ef4b
Result: PASS
Findings: NONE
Recommendation: APPROVE

QA Reviewer task: Review BE-FND-004 QA
Task ID: 01a0863c-25fe-72c0-94bc-bf1921446833
Result: PASS
Findings: NONE
Recommendation: APPROVE

Final reviewer gates: DBR=PASS; AR=PASS; QAR=PASS
Unresolved reviewer findings: NONE
```

Final closure evidence:

```text
Dependencies: DB-FND-002=DONE; BE-FND-002=DONE
Acceptance criteria: PASS
Required tests: PASS — Maven clean verify, 9 tests, 0 failures/errors/skips
PostgreSQL/Flyway/Hibernate schema validation: PASS
Required reviewers: DBR=PASS; AR=PASS; QAR=PASS
Unresolved findings: NONE
CI status: PRE_CI_BOOTSTRAP_NA
PRE_CI eligible Task ID: BE-FND-004
CI status reason: CI-FND-001 chưa effective và BE-FND-004 thuộc eligible list
Repository-level PRE_CI evidence: SATISFIED
Existing failing CI check: NONE — origin/main có 0 check runs và 0 status contexts
Failed CI check waiver: NOT USED
Baseline audit, build/static/git/diff, scope, secret, generated-file,
Flyway immutability và baseline-tag integrity: PASS
Contract drift: NONE
Unresolved blockers: NONE
Final closure gate: PASS
Task status: DONE
M1 execution progress: 7 / 29 (24.1%); M1 vẫn IN_PROGRESS
Commit/push/merge/tag mutation: NONE
```

### QA-FND-001 — Testcontainers PostgreSQL integration harness

- Status: DONE
- Status history: TODO → IN_PROGRESS → DONE
- Branch/worktree mode: `GOV009_DIRECT_MAIN` — uncommitted `main` worktree
- HEAD at admission: `aa3e9704a1239733959aec24408df3b4d957d929`
- `origin/main` at admission: `aa3e9704a1239733959aec24408df3b4d957d929`
- Dependencies: `DB-FND-002` DONE; `BE-FND-004` DONE
- Priority: P0
- Required reviewers: Database Reviewer, QA Reviewer
- Acceptance: Integration tests chạy trên PostgreSQL thật; Flyway tự chạy; không dùng H2 thay thế cho constraint/JSONB/timestamp behavior.
- Required tests: Theo global DoD + acceptance
- Source documents checked: Database Schema v1.6; System Architecture v1.3; Technical Specification v1.2; Backend Technical Specification v1.3
- Started at: 2026-09-10
- Closed at: 2026-09-10
- CI admission mode: `PRE_CI_BOOTSTRAP_NA`
- PRE_CI eligible Task ID: `QA-FND-001`
- CI status reason: `CI-FND-001` chưa effective và `QA-FND-001` nằm tường minh trong eligible prerequisite list
- Remote admission evidence: sau `git fetch`, `main == origin/main`; GitHub commit có 0 check runs và 0 status contexts
- Existing failing CI check: NONE
- Failed CI check waiver: NOT USED
- Contract changes: None — task chỉ tạo integration-test harness cho approved PostgreSQL/Flyway/JPA baseline
- Historical environment blocker: Docker Desktop 4.88.1 ban đầu không khởi động được do stale runtime reparse-point `sailor-ingest.sock`; runtime đã được khôi phục bên ngoài implementation session và focused retest đã PASS
- Unresolved blockers: NONE
- Self-review status: PROHIBITED — implementation session không được tự thỏa QAR gate

Implementation plan:

```text
Add only the Spring Boot Testcontainers integration and PostgreSQL Testcontainers
module needed by QA-FND-001. Provide reusable Spring test configuration and a base
class that starts an isolated PostgreSQL container through a service connection, lets
Flyway migrate from zero, and enables Hibernate schema validation.

Add a focused integration suite proving the runtime is PostgreSQL, Flyway creates the
34 canonical tables, and native JSONB, TIMESTAMPTZ and CHECK-constraint behavior is
exercised. Do not add H2, repositories, product API, schema migration, business logic,
seed data, indexes, profiles, security, idempotency service or later-task behavior.
```

Implementation evidence:

```text
Added test-scoped spring-boot-testcontainers and testcontainers-postgresql dependencies;
Spring Boot dependency management resolves Testcontainers 2.0.5.
Added PostgreSqlTestContainerConfiguration with the exact local PostgreSQL image
postgres:16.15-alpine3.24 and isolated test database credentials.
Added reusable PostgreSqlIntegrationTestSupport for Spring Boot integration tests.
Added PostgreSqlHarnessIntegrationTests covering real PostgreSQL identity, automatic
Flyway V1 application, exactly 34 canonical tables, Hibernate ddl-auto=validate,
native JSONB extraction, equivalent TIMESTAMPTZ instants, and the canonical
idempotency response_status CHECK constraint.
Updated README.md with Docker prerequisite, execution command and explicit no-H2 rule.
No product code, Flyway migration, API/OpenAPI, database schema or client was changed.
```

TEST evidence and blocker:

```text
.\backend\mvnw.cmd --no-transfer-progress -f backend\pom.xml -DskipTests test-compile
→ BUILD SUCCESS; all 7 test source files compile.

.\backend\mvnw.cmd --no-transfer-progress -f backend\pom.xml clean verify
→ BUILD FAILURE caused only by unavailable Docker runtime.
→ Existing 9 non-container tests PASS, 0 failures/errors/skips.
→ PostgreSqlHarnessIntegrationTests: 3 errors before ApplicationContext startup.
→ Root cause: Testcontainers 2.0.5 could not find a valid Docker environment.

Docker Desktop startup remediation attempt
→ Docker Desktop 4.88.1 backend log identifies a stale 0-byte ReparsePoint at
  C:\Users\Hi\AppData\Local\Docker\run\sailor-ingest.sock.
→ Backend crashes while trying to remove that socket; Docker API never becomes ready.
→ Exact stale socket and parent were verified read-only.
→ Host policy rejected removal of the out-of-workspace runtime socket; no Docker
  volume, repository file or unrelated user data was removed.

Focused non-container test command
→ BUILD SUCCESS; 9 tests, 0 failures, 0 errors, 0 skipped.

Maven dependency tree
→ spring-boot-testcontainers 4.1.1, testcontainers-postgresql 2.0.5.
→ H2 dependency: NONE.

python tools/baseline_audit.py
→ BASELINE AUDIT: PASS.

python -m py_compile tools/baseline_audit.py
→ PASS.

git diff --check
→ PASS.

Scope, untracked-whitespace, secret, generated-file, Flyway immutability and
baseline-tag integrity audits
→ PASS.
→ Changed scope contains only README, backend test dependencies, QA-FND-001 test
  harness/tests, and lifecycle/evidence planning updates.
→ Product main-code diff: NONE; Flyway diff: NONE; tracked target files: NONE.
→ Secret-pattern hits: NONE; container username/password are isolated test-only
  fixture values and are not production/shared credentials.
→ Baseline tag objects and peeled commits remain unchanged.
```

TEST stop state:

```text
Task status: IN_PROGRESS
Compile and non-container regression tests: PASS
Required PostgreSQL Testcontainers runtime acceptance: BLOCKED by Docker host state
Implementation-side product-code blocker: NONE
Self-review/reviewer PASS claimed: NO
Required independent reviewers after runtime TEST PASS: Database Reviewer, QA Reviewer
SELF_REVIEW_CONFLICT: independent QAR is mandatory because Owner and reviewer are QAR
Commit/push/merge/tag mutation: NONE
```

Focused runtime retest after Docker recovery — 2026-09-10:

```text
Docker runtime
→ Docker Desktop engine 29.7.2 available through local Npipe socket.

.\backend\mvnw.cmd --no-transfer-progress -f backend\pom.xml clean verify
→ BUILD SUCCESS.
→ Tests run: 12, Failures: 0, Errors: 0, Skipped: 0.
→ PostgreSqlHarnessIntegrationTests: 3 PASS.
→ Testcontainers 2.0.5 started postgres:16.15-alpine3.24.
→ Flyway detected an empty public schema, validated and applied V1 successfully.
→ PostgreSQL product/version, exactly 34 canonical tables, Hibernate schema
  validation, JSONB, TIMESTAMPTZ and CHECK-constraint behavior: PASS.

Repeat clean verify from a new JVM/container
→ BUILD SUCCESS.
→ Tests run: 12, Failures: 0, Errors: 0, Skipped: 0.
→ New isolated JDBC port and fresh empty schema observed; Flyway reapplied V1 once.
→ Testcontainers present before repeat: NONE.
→ Testcontainers present after cleanup: NONE.
→ Repeatability and isolation: PASS.

python tools/baseline_audit.py
→ BASELINE AUDIT: PASS.

python -m py_compile tools/baseline_audit.py
→ PASS.

git diff --check and untracked whitespace audit
→ PASS.

Dependency/scope/secret/generated/Flyway/tag audits
→ H2 dependency: NONE.
→ Product main-code diff: NONE; Flyway diff: NONE; tracked target files: NONE.
→ Secret-pattern hits: NONE; test-only isolated fixture credential classification PASS.
→ Baseline tag objects and peeled commits: UNCHANGED.
→ Full tracked diff and every untracked current-task file: INSPECTED.
```

Current TEST stop state:

```text
Task status: IN_PROGRESS
Acceptance and applicable TEST gates: PASS
Unresolved implementation/test blockers: NONE
Self-review/reviewer PASS claimed: NO
Required independent reviewers next: Database Reviewer, QA Reviewer
SELF_REVIEW_CONFLICT: independent QAR is mandatory because Owner and reviewer are QAR
PRE_CI status: PRE_CI_BOOTSTRAP_NA eligible; final evidence still requires reviewer PASS
Commit/push/merge/tag mutation: NONE
```

Database Reviewer FAIL and focused remediation — 2026-09-10:

```text
Historical Database Reviewer result: FAIL
Finding ID: DB-QA-FND-001-001
Severity: MEDIUM
Blocking: YES
Original Status: OPEN
Current finding status: OPEN — only Database Reviewer may mark RESOLVED

Finding:
PostgreSqlHarnessIntegrationTests asserted exactly one applied Flyway migration,
which would reject a valid append-only Flyway history after V2+ is introduced.

Focused remediation:
Removed the total migration-count assertion and any implicit future maximum.
The harness now requires canonical V1__create_schema_baseline.sql to be present
and SUCCESS, and requires every currently resolved/applied migration to be SUCCESS.
Fresh-database migration, canonical 34-table validation, PostgreSQL identity,
JSONB, TIMESTAMPTZ, CHECK-constraint, repeatability and isolation coverage remain.
V1__create_schema_baseline.sql: UNCHANGED.
Product code: UNCHANGED.
DB-FND-003 / DB-FND-004 implementation: NOT ADDED.
Task status: IN_PROGRESS.
Finding remains OPEN pending focused Database Reviewer re-review.
```

Focused remediation validation — 2026-09-10:

```text
.\backend\mvnw.cmd --no-transfer-progress -f backend\pom.xml clean verify
→ BUILD SUCCESS.
→ Tests run: 12, Failures: 0, Errors: 0, Skipped: 0.
→ PostgreSqlHarnessIntegrationTests: 3 PASS.
→ Testcontainers started postgres:16.15-alpine3.24 on isolated port 52668.
→ Flyway detected an empty public schema, validated canonical V1 and applied it
  successfully; Hibernate schema validation and 34-table assertion PASS.

Repeat clean verify from a new JVM/container
→ BUILD SUCCESS.
→ Tests run: 12, Failures: 0, Errors: 0, Skipped: 0.
→ New isolated PostgreSQL port 51376 and fresh empty schema observed.
→ Flyway reapplied canonical V1 successfully; repeatability/isolation PASS.
→ Testcontainers present before repeat: NONE.
→ Testcontainers present after cleanup: NONE.

Append-only migration assertion
→ Canonical V1 is selected by version and exact script name and must be SUCCESS.
→ Every currently resolved/applied migration must be SUCCESS.
→ No assertion of total migration count and no future maximum are present.

python tools\baseline_audit.py
→ BASELINE AUDIT: PASS.

python -m py_compile tools\baseline_audit.py
→ PASS.

git diff --check and untracked whitespace audit
→ PASS; untracked whitespace hits: 0.

Scope/secret/generated/Flyway/tag audits
→ Product main-code diff: NONE; Flyway diff: NONE.
→ V1__create_schema_baseline.sql hash remains
  90cea2345563c50176015637050ca73479e72d03.
→ DB-FND-003 / DB-FND-004 markers in diff: NONE; H2 dependency: NONE.
→ Secret scan findings are non-secret documentation terms and an explicit README
  placeholder; isolated Testcontainers credentials remain test-only fixtures.
→ Generated-file hits: NONE.
→ Baseline tag objects and peeled commits: UNCHANGED.
→ Full tracked diff and every untracked current-task file: INSPECTED.

Remediation stop state
→ QA-FND-001: IN_PROGRESS.
→ DB-QA-FND-001-001: OPEN; not marked RESOLVED.
→ Historical Database Reviewer FAIL and original metadata: PRESERVED.
→ Next step: focused Database Reviewer re-review.
→ Commit/push/merge/tag mutation: NONE.
```

Independent reviewer evidence synchronization — 2026-09-10:

```text
Chronology preserved:
1. Initial Database Review: FAIL.
   DB-QA-FND-001-001; Severity MEDIUM; Blocking YES; Original Status OPEN.
2. Remediation removed the hard-coded applied-migration count of 1, preserved
   append-only compatibility, and changed neither product code nor Flyway migration.
3. Independent QA Review: PASS; findings NONE; QAR=PASS.
   QA verified the remediation and 12/12 tests PASS. At that review point,
   DB-QA-FND-001-001 remained OPEN pending Database Reviewer resolution.
4. Focused Database Re-review: PASS.
   DB-QA-FND-001-001 final Status RESOLVED; new findings NONE; DBR=PASS.

Final reviewer state:
DBR = PASS
QAR = PASS
DB-QA-FND-001-001 final status = RESOLVED
Unresolved findings = NONE

Evidence classification: synchronization of independently produced reviewer results;
this QA implementation session did not perform or claim a self-review.
QA-FND-001 status: IN_PROGRESS.
PRE_CI_BOOTSTRAP_NA: pending finalization.
Commit/push/merge/tag mutation: NONE.
```

Final closure evidence — 2026-09-10:

```text
Mode: GOV009_DIRECT_MAIN
Task / Owner / priority: QA-FND-001 / QAR / P0
Dependencies: DB-FND-002=DONE; BE-FND-004=DONE
Acceptance criteria: PASS

Required tests:
.\backend\mvnw.cmd --no-transfer-progress -f backend\pom.xml clean verify
→ BUILD SUCCESS; 12 tests, 0 failures, 0 errors, 0 skipped.
→ PostgreSqlHarnessIntegrationTests: 3 PASS on PostgreSQL 16.15.
→ Fresh Flyway V1 migration, Hibernate validation, JSONB, TIMESTAMPTZ,
  CHECK constraint and canonical 34-table verification: PASS.
→ Prior independent repeat-run/isolation validation: PASS.

Required independent reviewers:
Database Reviewer = PASS after focused re-review.
QA Reviewer = PASS; independently verified remediation and 12/12 tests.
DB-QA-FND-001-001 final Status = RESOLVED.
Historical Database Reviewer FAIL, Severity MEDIUM, Blocking YES and Original
Status OPEN remain preserved.
Unresolved findings = NONE.
Self-review used = NO.

CI status: PRE_CI_BOOTSTRAP_NA
PRE_CI eligible Task ID: QA-FND-001
CI status reason: CI-FND-001 chưa effective và QA-FND-001 thuộc exact eligible list.
Repository-level PRE_CI evidence: SATISFIED.
Existing failing CI check: NONE observed for origin/main
aa3e9704a1239733959aec24408df3b4d957d929; check page contains no check run.
Failed-check waiver: NOT USED.

Final validation:
python tools\baseline_audit.py → PASS.
python -m py_compile tools\baseline_audit.py → PASS.
git diff --check → PASS.
Untracked whitespace, scope, secret, generated-file, H2 dependency,
Flyway immutability and baseline-tag integrity audits → PASS.
Product main-code diff: NONE; Flyway diff: NONE.
V1__create_schema_baseline.sql hash remains
90cea2345563c50176015637050ca73479e72d03.
Full tracked diff and every untracked current-task file: INSPECTED.

Contract impact: no API/OpenAPI, database schema, migration, product behavior,
security or client contract change; backward compatibility preserved.
Final closure gate: PASS.
Task status: DONE.
M1 execution progress: 8 / 29 (27.6%); M1 remains IN_PROGRESS.
Commit/push/merge/tag mutation: NONE.
```

### BE-FND-005 — Common error envelope + HTTP status mapping

- Status: DONE
- Status history: TODO → IN_PROGRESS → DONE
- Branch/worktree mode: `GOV009_DIRECT_MAIN` — uncommitted `main` worktree
- HEAD at admission: `07889d0a682cd8d0b7d784486f2776a2be4d0b6d`
- `origin/main` at admission: `07889d0a682cd8d0b7d784486f2776a2be4d0b6d`
- Dependencies: `BE-FND-002` DONE
- Priority: P0
- Required reviewers: Architecture Reviewer, QA Reviewer
- Acceptance: Global exception handler trả shape canonical; hỗ trợ `CONCURRENT_UPDATE`, `IDEMPOTENCY_KEY_REUSE` và validation codes đúng API.
- Required tests: Theo global DoD + acceptance
- Source documents checked: SRS v1.2; System Architecture v1.3; API Specification v1.4; OpenAPI v1.4; Technical Specification v1.2; Backend Technical Specification v1.3
- Started at: 2026-09-11
- CI admission mode: `PRE_CI_BOOTSTRAP_NA`
- PRE_CI eligible Task ID: `BE-FND-005`
- CI status reason: `CI-FND-001` chưa effective và `BE-FND-005` nằm tường minh trong eligible prerequisite list
- Remote admission evidence: `main == origin/main`; GitHub commit có 0 check runs và 0 status contexts
- Existing failing CI check: NONE
- Failed CI check waiver: NOT USED
- Unresolved blockers: NONE

Implementation plan:

```text
Implement the API/OpenAPI canonical error response fields timestamp, status, code,
message, path and details. Add a reusable typed API exception boundary plus a
RestControllerAdvice that maps validation/input errors, explicit application errors,
the exact CONCURRENT_UPDATE and IDEMPOTENCY_KEY_REUSE codes, and unexpected failures
without exposing internal details.

Add only the Bean Validation runtime needed to exercise validation error mapping and
focused MockMvc tests for response shape, HTTP status/code pairs, details, malformed
input and safe 500 behavior. Do not add business controllers, domain validation rules,
pagination/mappers, correlation/trace ID, persistence conflict translation,
idempotency service, authentication/security or API/OpenAPI contract changes.
```

Implementation evidence:

```text
Added spring-boot-starter-validation through Spring Boot dependency management.
Added ApiErrorResponse with the API/OpenAPI v1.4 fields timestamp, status, code,
message, path and details; details is always a non-null immutable list.
Added ApiErrorCodes and ApiException as the reusable common error boundary.
Added ConcurrentUpdateException and IdempotencyKeyReuseException with exact
HTTP 409/code pairs for later business services to raise.
Added GlobalExceptionHandler using RestControllerAdvice for request-body validation,
method validation, malformed JSON, framework HTTP errors, explicit application errors
and safe unexpected-error responses.
Validation details expose only field/message and never rejected values, credentials,
stack traces, SQL, class names or vendor payloads.
Added focused MockMvc coverage and README usage/ownership guidance.
No product controller, repository, migration, API/OpenAPI or client file changed.
```

Validation evidence:

```text
Focused GlobalExceptionHandlerTests
→ PASS; 10 tests, 0 failures, 0 errors, 0 skipped.
→ Canonical envelope, 400 VALIDATION_ERROR, malformed JSON, generic application
  status/code, exact 409 CONCURRENT_UPDATE and IDEMPOTENCY_KEY_REUSE, and safe
  500 INTERNAL_ERROR behavior verified.
→ Real Spring MVC dispatch also verifies missing required parameters and invalid
  parameter types as 400 VALIDATION_ERROR, unsupported methods as
  405 VALIDATION_ERROR, and unsupported media types as 415 VALIDATION_ERROR.

.\backend\mvnw.cmd --no-transfer-progress -f backend\pom.xml clean verify
→ BUILD SUCCESS.
→ Tests run: 22, Failures: 0, Errors: 0, Skipped: 0.
→ PostgreSqlHarnessIntegrationTests: 3 PASS on PostgreSQL 16.15 Testcontainers.
→ Fresh Flyway V1 migration, Hibernate schema validation, canonical 34 tables,
  JSONB, TIMESTAMPTZ and CHECK-constraint regression coverage: PASS.

Development-test chronology
→ Initial focused compile exposed a Spring Framework 7 exception hierarchy mismatch;
  remediated by status-based handling without product-contract change.
→ A malformed-JSON mapping gap and one standalone-MockMvc-only missing-route
  assertion were identified and corrected/removed before the final PASS suite.
→ No known failing test remains.

python tools/baseline_audit.py
→ BASELINE AUDIT: PASS.

python -m py_compile tools/baseline_audit.py
→ PASS.

git diff --check and git diff --cached --check
→ PASS.

Scope, untracked-whitespace, secret, generated-file, Flyway immutability and
baseline-tag integrity audits
→ PASS.
→ Untracked whitespace findings: 0; tracked backend/target files: 0;
  secret/private-key pattern findings: 0; unmerged paths: 0.
→ Product database/Flyway diff: NONE.
→ Baseline tag objects and peeled commits: UNCHANGED.
→ Full tracked diff and every untracked current-task file: INSPECTED.
```

Change impact:

```text
Change: Add the common REST error envelope and HTTP/code mapping foundation.
Why: Satisfy BE-FND-005 using the approved API/OpenAPI v1.4 contract.
Affected documents: README.md plus backlog/execution lifecycle evidence.
Affected API/OpenAPI: Runtime implementation only; contract files unchanged.
Affected database/Flyway: None.
Affected security: Unexpected/internal details are not exposed; no auth behavior changed.
Affected business rules: None.
Affected clients: None; existing error contract is now implemented by the backend.
Migration: None.
Backward compatibility: Preserved.
```

TEST stop state:

```text
Task status: IN_PROGRESS
Implementation and applicable TEST gates: PASS
Unresolved implementation/test blockers: NONE
Self-review/reviewer PASS claimed: NO
Independent reviewer gates: Architecture Reviewer PASS; QA Reviewer PASS after
historical FAIL and focused re-review
Unresolved findings: NONE
Required next step: finalization
PRE_CI status: PRE_CI_BOOTSTRAP_NA eligible; reviewer evidence PASS
Commit/push/merge/tag mutation: NONE
```

QA Reviewer FAIL and focused remediation chronology:

```text
Reviewer: QA Reviewer
Review result: FAIL (historical; preserved)
Finding ID: QA-BE-FND-005-001
Severity: HIGH
Blocking: YES
Original Status: OPEN
Current Status: RESOLVED — confirmed by independent focused QA re-review

Finding:
MissingServletRequestParameterException, MethodArgumentTypeMismatchException,
HttpRequestMethodNotSupportedException and HttpMediaTypeNotSupportedException could
bypass the existing Spring ErrorResponseException mapping and fall through to the
generic 500 INTERNAL_ERROR response.

Focused remediation:
Added explicit Spring MVC exception handlers that all use the canonical envelope.
Missing required request parameters and request parameter/path-variable type
mismatches return HTTP 400 + VALIDATION_ERROR. Unsupported HTTP methods return
HTTP 405 + VALIDATION_ERROR. Unsupported media types return HTTP 415 +
VALIDATION_ERROR. The existing API/OpenAPI v1.4 public code set and the BE-FND-005
fallback mapping define VALIDATION_ERROR for these otherwise-unspecialized 4xx
request failures, so no new public error code or contract change was introduced.
The genuine unexpected-error fallback remains HTTP 500 + INTERNAL_ERROR.

Regression evidence:
GlobalExceptionHandlerTests exercises real MockMvc request dispatch for missing
required parameters, invalid parameter types, unsupported HTTP methods and
unsupported media types, while retaining body-validation, malformed-JSON,
CONCURRENT_UPDATE, IDEMPOTENCY_KEY_REUSE, canonical-envelope and safe-500 coverage.
Focused suite: 10 tests PASS.
Full Maven clean verify: 22 tests PASS, including PostgreSQL/Testcontainers tests.

Finding disposition:
RESOLVED by independent focused QA re-review. Historical Severity HIGH,
Blocking YES, Original Status OPEN and QA Reviewer FAIL remain preserved.

Independent reviewer evidence:
Architecture Reviewer task: 01a0862b-ec5a-7191-a3ab-91adbd69ef4b
Architecture result: PASS; findings: NONE; recommendation: APPROVE.
QA Reviewer task: 01a0863c-25fe-72c0-94bc-bf1921446833
Focused QA re-review result: PASS.
QA-BE-FND-005-001 final Status: RESOLVED.
New QA findings/regressions: NONE; recommendation: APPROVE.
Unresolved findings: NONE.

Task status: IN_PROGRESS
Commit/push/merge/tag mutation: NONE
```

Finalization evidence — 2026-09-11:

```text
Workflow mode: GOV009_DIRECT_MAIN
Lifecycle transition: IN_PROGRESS → DONE
Dependency BE-FND-002: DONE
Acceptance: PASS
Required TEST gates: PASS
Final Maven clean verify: BUILD SUCCESS; 22 tests PASS; 0 failures/errors/skips
PostgreSQL 16.15 Testcontainers + fresh Flyway V1 + Hibernate validation: PASS
Architecture Reviewer: PASS; findings NONE; recommendation APPROVE
QA Reviewer chronology: historical FAIL preserved; focused re-review PASS
QA-BE-FND-005-001: final Status RESOLVED by independent QA Reviewer
New QA findings/regressions: NONE
Unresolved findings: NONE
CI mode: PRE_CI_BOOTSTRAP_NA
PRE_CI eligible Task ID: BE-FND-005
Existing failing CI check: NONE
Remote origin/main SHA 07889d0a682cd8d0b7d784486f2776a2be4d0b6d:
0 check runs; 0 status contexts; failing checks/statuses 0
Failed-check waiver: NOT USED
baseline_audit, py_compile, git diff checks, scope, untracked whitespace,
secret/private-key, generated-file, Flyway immutability and baseline-tag
integrity: PASS
API/OpenAPI/database/Flyway/client contract changes: NONE
M1 execution progress: 9 / 29 (31.0%); M1 remains IN_PROGRESS
Commit/push/merge/tag mutation: NONE
```
