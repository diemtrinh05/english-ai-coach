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
| 2026-09-11 | BE-FND-007 | OPEN | `QA-BE-FND-007-001` — Severity: MEDIUM; Blocking: YES; Original Status: OPEN. `PaginatedResponse.from(Page.empty())` truyền `size=0` vào constructor canonical và ném `IllegalArgumentException`. | Normalized only the Spring Page conversion boundary, centralized pagination constants and added empty/unpaged/Pageable/mapper regressions; finding remains OPEN pending independent focused QA re-review. |
| 2026-09-11 | BE-FND-007 | RESOLVED | `QA-BE-FND-007-001` — historical metadata preserved as Severity: MEDIUM; Blocking: YES; Original Status: OPEN. | Independent focused QA re-review PASS confirmed the `Page.empty()` conversion remediation, shared `PaginationConvention` constants and focused regression coverage; final finding Status: RESOLVED; new findings: NONE. Historical QA Reviewer FAIL remains preserved. |
| 2026-09-13 | CI-FND-001 | OPEN | `DB-CI-FND-001-001` — Severity: MEDIUM; Blocking: YES; Original Status: OPEN. Unit gate excluded named integration classes instead of the complete `*IntegrationTests` convention, so future integration tests could also run in Unit. | Replaced the named exclusion with the canonical `!*IntegrationTests` selector, retained the dedicated OpenAPI exclusion and added pattern-based audit regressions. Finding remains OPEN pending independent focused Database Reviewer re-review. |
| 2026-09-13 | CI-FND-001 | OPEN | `SEC-CI-FND-001-001` — Severity: MEDIUM; Blocking: YES; Original Status: OPEN. The audit checked only top-level permissions, so job-level permission escalation could pass. | Prohibited job-level permissions across every job, enforced the exact canonical job set and added privileged-job regression coverage. Finding remains OPEN pending independent focused Security Reviewer re-review. |
| 2026-09-13 | CI-FND-001 | OPEN | `SEC-CI-FND-001-002` — Severity: LOW; Blocking: NO; Original Status: OPEN. Checkout persisted Git credentials although later steps perform no authenticated Git operations. | Set and audited `persist-credentials: false`, with missing/true negative regressions. Recommendation remains OPEN pending independent focused Security Reviewer re-review. |
| 2026-09-13 | CI-FND-001 | RESOLVED | `DB-CI-FND-001-001` — historical Database Reviewer FAIL preserved; Severity: MEDIUM; Blocking: YES; Original Status: OPEN. | Supplied independent focused Database Reviewer re-review PASS confirmed complete `*IntegrationTests` selector separation, workflow-audit protection, named-class/missing-wildcard negative regressions, Unit 31/31, PostgreSQL integration 3/3 and fresh PostgreSQL/Flyway/Hibernate validation; final finding Status: RESOLVED; new Database findings: NONE. |
| 2026-09-13 | CI-FND-001 | RESOLVED | `SEC-CI-FND-001-001` — historical Security Reviewer FAIL preserved; Severity: MEDIUM; Blocking: YES; Original Status: OPEN. | Supplied independent focused Security Reviewer re-review PASS confirmed top-level `contents: read`, all-job permission auditing, job-level override rejection, exact job-set guard and rejection of `contents: write`, `id-token: write`, `write-all` and an extra privileged job; final finding Status: RESOLVED. |
| 2026-09-13 | CI-FND-001 | RESOLVED | `SEC-CI-FND-001-002` — historical Security Reviewer FAIL preserved; Severity: LOW; Blocking: NO; Original Status: OPEN. | Supplied independent focused Security Reviewer re-review PASS confirmed checkout `persist-credentials: false`, audit enforcement and missing/true negative regressions; no authenticated Git command is required after checkout; final finding Status: RESOLVED. |
| 2026-09-13 | DB-FND-003 | OPEN | `QA-DB-FND-003-001` — Severity: MEDIUM; Blocking: YES; Original Status: OPEN. Partial-index behavioral coverage did not prove all three canonical notification types or the allowed-side boundaries for notification dedupe, primary goals and assessment in-progress uniqueness. | Strengthened PostgreSQL integration coverage without changing the already-canonical V2 predicate; finding remained OPEN pending independent focused QA re-review. |
| 2026-09-13 | DB-FND-003 | RESOLVED | `QA-DB-FND-003-001` — historical QA Reviewer FAIL preserved; Severity: MEDIUM; Blocking: YES; Original Status: OPEN. | Independent focused QA re-review PASS confirmed complete behavioral and allowed-side partial-index coverage; final finding Status: RESOLVED; new QA findings: NONE. |
| 2026-09-15 | BE-FND-008 | OPEN | `QA-BE-FND-008-001` — Severity: MEDIUM; Blocking: YES; Original Status: OPEN. The concurrent duplicate test could pass through sequential initial lookup/replay without proving the PostgreSQL `ON CONFLICT` / `inserted=false` path. | Added deterministic winner/loser transaction coordination and PostgreSQL lock observation; remediation completed while the finding remained OPEN pending independent focused QA re-review. |
| 2026-09-15 | BE-FND-008 | RESOLVED | `QA-BE-FND-008-001` — historical QA Reviewer FAIL preserved; Severity: MEDIUM; Blocking: YES; Original Status: OPEN. | Supplied independent focused QA re-review PASS confirmed the loser reaches the real PostgreSQL insert, waits on the winner lock, receives `inserted=false`, replays the committed response and performs exactly one mutation; final finding Status: RESOLVED; new QA findings: NONE. |

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
| 2026-09-11 | BE-FND-007 | QA Reviewer | FAIL | `QA-BE-FND-007-001` — Severity: MEDIUM; Blocking: YES; Original Status: OPEN. `Page.empty()` produced invalid canonical pagination metadata and caused `PaginatedResponse.from(...)` to throw; focused remediation and independent QA re-review are required; finding remains OPEN. |
| 2026-09-11 | BE-FND-007 | QA Reviewer | PASS | Independent focused QA re-review confirmed `QA-BE-FND-007-001` RESOLVED; remediation and regression coverage for `Page.empty()`, `Page.empty(Pageable)`, constructor invariants and mapper behavior are sufficient; new findings: NONE; QAR=PASS. Historical QA FAIL and Original Status OPEN remain preserved. |
| 2026-09-11 | BE-FND-007 | Architecture Reviewer | PASS | Independent Architecture re-review found no architecture findings or regressions after remediation; architecture boundaries, dependency direction, contract consistency and scope remain compliant; AR=PASS. This result does not independently resolve the QA finding and does not override the completed QA re-review that resolved it. |
| 2026-09-13 | CI-FND-001 | Architecture Reviewer | PASS | Historical independent Architecture Reviewer PASS preserved as supplied for the CI-FND-001 review chronology; this remediation session did not perform or recreate Architecture review. |
| 2026-09-13 | CI-FND-001 | Database Reviewer | FAIL | `DB-CI-FND-001-001` — Severity: MEDIUM; Blocking: YES; Original Status: OPEN. Unit excluded only named integration classes, allowing a future `*IntegrationTests` class to run in both Unit and PostgreSQL integration gates. Focused remediation and independent Database Reviewer re-review required; finding remains OPEN. |
| 2026-09-13 | CI-FND-001 | Security Reviewer | FAIL | `SEC-CI-FND-001-001` — MEDIUM, Blocking YES, Original Status OPEN; job-level permission escalation was not audited. `SEC-CI-FND-001-002` — LOW, Blocking NO, Original Status OPEN; checkout persisted credentials unnecessarily. Focused remediation completed; both findings remain OPEN pending independent Security Reviewer re-review. |
| 2026-09-13 | CI-FND-001 | Architecture Reviewer | PASS | Final independent Architecture re-review (supplied evidence): canonical Baseline → Build/static → Unit → PostgreSQL integration → OpenAPI → Package order preserved; complete Unit/integration selectors, least-privilege permissions, fail-closed job policy and `persist-credentials: false` verified; no product/API/database/client architecture changes; findings: NONE; AR=PASS. |
| 2026-09-13 | CI-FND-001 | Database Reviewer | PASS | Independent focused Database re-review (supplied evidence): `DB-CI-FND-001-001` RESOLVED; complete `*IntegrationTests` separation and audit regressions verified; Unit 31/31, PostgreSQL integration 3/3 and fresh PostgreSQL/Flyway/Hibernate validation PASS; new findings: NONE; DBR=PASS. Historical Database FAIL and Original Status OPEN remain preserved. |
| 2026-09-13 | CI-FND-001 | Security Reviewer | PASS | Independent focused Security re-review (supplied evidence): `SEC-CI-FND-001-001` RESOLVED and `SEC-CI-FND-001-002` RESOLVED; all-job permission enforcement, privileged-case negatives, exact job-set guard and checkout credential hardening verified; new findings: NONE; SR=PASS. Historical Security FAIL and both Original Status OPEN values remain preserved. |
| 2026-09-13 | CI-FND-001 | QA Reviewer | PASS | Independent QA review (supplied evidence): workflow audit PASS; audit regressions 11/11; Build/static PASS; Unit 31/31; PostgreSQL integration 3/3; OpenAPI 11/11; Maven clean verify 45/45; Package PASS; canonical order and fail-closed security invariants verified; PRE_CI not used and actual remote CI not claimed; findings: NONE; QAR=PASS. |
| 2026-09-13 | DB-FND-003 | Database Reviewer | PASS | Independent initial Database review verified the append-only V1 → V2 migration, canonical indexes/partial predicates, due-review EXPLAIN, optimistic-lock invariants and PostgreSQL 16.15 execution; Database findings: NONE; DBR=PASS. |
| 2026-09-13 | DB-FND-003 | QA Reviewer | FAIL | `QA-DB-FND-003-001` — Severity: MEDIUM; Blocking: YES; Original Status: OPEN. Behavioral coverage was incomplete for `REVIEW_REMINDER`, `DAILY_PLAN`, `STREAK` and allowed-side partial-index boundaries; focused remediation and independent QA re-review required. |
| 2026-09-13 | DB-FND-003 | QA Reviewer | PASS | Independent focused QA re-review confirmed `QA-DB-FND-003-001` RESOLVED; focused 10/10, full 59/59 and fresh Flyway V1 → V2 PostgreSQL validation PASS; new findings/regressions: NONE; QAR=PASS. Historical QA FAIL and Original Status OPEN remain preserved. |
| 2026-09-13 | DB-FND-003 | Database Reviewer | PASS | Independent post-remediation Database review reconfirmed the complete canonical predicates and allowed-side boundaries, append-only migration integrity, PostgreSQL 16.15 validation and no Database findings; DBR=PASS. This review did not resolve or claim the QA finding independently. |
| 2026-09-15 | DB-FND-004 | Database Reviewer | PASS | Independent Database review verified append-only V3, canonical 6 CEFR/7 goals/5 badges, natural-key upsert repeatability, fresh PostgreSQL V1 → V2 → V3 migration and Hibernate validation; Database findings: NONE; DBR=PASS. |
| 2026-09-15 | DB-FND-004 | QA Reviewer | PASS | Independent QA review verified all acceptance criteria, focused PostgreSQL 15/15, full Maven 64/64, fresh Flyway V1 → V2 → V3, audit gates and scope boundaries; QA findings: NONE; QAR=PASS. |
| 2026-09-15 | BE-FND-008 | Database Reviewer | PASS | Supplied independent Database review verified the canonical `idempotency_keys` schema and indexes, `INSERT ... ON CONFLICT (event_id) DO NOTHING`, atomic claim/mutation/snapshot rollback semantics, strict retention cutoff and deterministic real PostgreSQL conflict path; Database findings: NONE; DBR=PASS. |
| 2026-09-15 | BE-FND-008 | Security Reviewer | PASS | Supplied independent Security re-review verified user/endpoint/request-hash replay binding, cross-user reuse rejection, canonical hash scope, atomic duplicate-race behavior, absence of sensitive logging and no API/Flyway/client/security regression; Security findings: NONE; SR=PASS. |
| 2026-09-15 | BE-FND-008 | Architecture Reviewer | PASS | Supplied independent Architecture re-review verified the test-only remediation, unchanged production idempotency architecture, package-private repository visibility, no production synchronization hook, deterministic PostgreSQL conflict flow and no layer/dependency/contract regression; Architecture findings: NONE; AR=PASS. This reviewer did not resolve the QA finding. |
| 2026-09-15 | BE-FND-008 | QA Reviewer | FAIL | Historical independent QA review: `QA-BE-FND-008-001` — Severity: MEDIUM; Blocking: YES; Original Status: OPEN. The concurrent duplicate test was nondeterministic and could false-pass without exercising the PostgreSQL conflict path. |
| 2026-09-15 | BE-FND-008 | QA Reviewer | PASS | Supplied independent focused QA re-review confirmed `QA-BE-FND-008-001` RESOLVED; the loser deterministically reaches the real PostgreSQL insert/lock path, receives `inserted=false`, replays the winner response, commits exactly one record and one mutation, and does not return 500; new QA findings: NONE; QAR=PASS. Historical QAR FAIL and Original Status OPEN remain preserved. |

#### Milestone status

| Milestone                                | Execution complete | Total | Execution progress | DoD status  |
| ---------------------------------------- | -----------------: | ----: | -----------------: | ----------- |
| M0 — Execution Governance                |                  7 |     7 |               100% | PASS        |
| M1 — Foundation Ready                    |                 16 |    29 |              55.2% | IN_PROGRESS |
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

### BE-FND-003 — Thiết lập application profiles và typed configuration

- Status: IN_REVIEW
- Status history: TODO → READY → IN_PROGRESS → IN_REVIEW → BLOCKED → IN_REVIEW
- Branch: `feat/BE-FND-003-application-profiles-config`
- Baseline provenance: `baseline-v1-implementation-ready-r1` (`34362780eb7ffeb9391ade95220cf895a4592f70`)
- Dependencies: `BE-FND-001` DONE
- Priority: P0
- Required reviewers: Architecture Reviewer, Security Reviewer, QA Reviewer
- Acceptance: `application.yml/local/test/prod`; typed properties cho assessment/SRS/personalization/gamification/notification/idempotency; default constants khớp baseline.
- Required tests: Theo global DoD + acceptance
- Source documents checked: SRS v1.2; System Architecture v1.3; AI Personalization v1.3; Technical Specification v1.2; Backend Technical Specification v1.3
- Blockers: actual CI PASS pending; BE-FND-003 is not eligible for `PRE_CI_BOOTSTRAP_NA`
- Contract changes: None
- Started at: 2026-09-02
- Ready for review: 2026-09-02
- Current reviewer state: AR=PASS; SR=PASS; QAR=PASS

Historical QA finding:

```text
Reviewer: QA Reviewer
Result: FAIL
Finding ID: QA-BE-FND-003-001
Severity: HIGH
Blocking: YES
Finding: maximum-decrease-percent represented the canonical maximum workload decrease
as signed -30 instead of the approved positive magnitude 30.
Required action: change only maximum-decrease-percent to 30 and update its focused test;
preserve low-change-percent = -20 because it is a signed raw change.
Finding status: RESOLVED
```

Focused remediation for `QA-BE-FND-003-001`:

```text
Changed only app.personalization.workload.maximum-decrease-percent from -30 to 30
so the maximum decrease is represented as the approved positive magnitude.
Updated only the corresponding maximumDecreasePercent test expectation from -30 to 30.
Preserved app.personalization.workload.low-change-percent = -20 and its test expectation
because lowChangePercent is the signed raw change applied in the low-performance branch.
Focused ConfigurationPropertiesTests: PASS — 4 tests, 0 failures, 0 errors, 0 skipped.
No other configuration value, typed field, behavior, dependency, or contract changed.
QA-BE-FND-003-001 current status is RESOLVED; its original QA result FAIL remains
preserved in the historical finding above.
```

Historical QA re-review finding:

```text
Finding ID: QA-BE-FND-003-002
Reviewer: QA Reviewer
Result: FAIL
Severity: MEDIUM
Blocking: YES
Finding: historical metadata for QA-BE-FND-003-001 did not preserve its original
Severity: HIGH, Blocking: YES, and current status RESOLVED.
Required action: restore that metadata exactly without changing product code,
configuration values, tests, reviewer results, or unrelated planning history.
Remediation: restored Severity: HIGH, added Blocking: YES, and restored Finding status:
RESOLVED for QA-BE-FND-003-001 while preserving its historical Result: FAIL.
Finding status: RESOLVED
Governance validation: baseline_audit PASS; py_compile PASS; git diff --check PASS;
git status, diff stat, and full execution-log diff inspected.
```

Independent reviewer results:

```text
Reviewer: Architecture Reviewer
Result: PASS
Findings: none

Reviewer: Security Reviewer
Result: PASS
Findings: none

Reviewer: QA Reviewer
Final re-review result: PASS
QA-BE-FND-003-001: RESOLVED
QA-BE-FND-003-002: RESOLVED
Historical chain preserved: initial QA FAIL → QA-BE-FND-003-001 → remediation
→ QA re-review FAIL → QA-BE-FND-003-002 → remediation → final QA re-review PASS.
Final reviewer gates: AR=PASS; SR=PASS; QAR=PASS
Unresolved reviewer findings: NONE
```

CI gate state:

```text
CI status: PENDING — actual CI PASS required before DONE
PRE_CI_BOOTSTRAP_NA eligibility: NO — BE-FND-003 is not in the exact eligible task list
PRE_CI_BOOTSTRAP_NA usage: NOT USED
Closure blocker: actual CI PASS evidence is still pending
Task status: IN_REVIEW
```

Implementation plan:

```text
Add application.yml plus local/test/prod profile resources without credentials.
Add immutable typed configuration records for Assessment, SRS, Personalization,
Gamification, Notification, and Idempotency under app.* prefixes.
Bind canonical Reconciled V1 constants and verify them through Spring context tests.
Do not implement algorithms, schedulers, persistence, providers, security controls,
Clock abstraction, API endpoints, or secrets/env conventions owned by later tasks.
```

Non-contract decisions:

```text
Use immutable Java records with @ConfigurationProperties and centralized
@ConfigurationPropertiesScan. Prefixes follow the task domains:
app.assessment, app.srs, app.personalization, app.gamification,
app.notification, app.idempotency.
Profile-specific files only activate their named profile; all canonical defaults live
in application.yml so local/test/prod inherit one source and cannot drift.
```

Implementation evidence:

```text
Added application.yml as the single source for approved baseline defaults and added
application-local.yml, application-test.yml, and application-prod.yml with explicit
profile activation only.
Added immutable typed configuration records for assessment, SRS, personalization,
gamification, notification, and idempotency under their documented app.* prefixes.
Enabled centralized typed-property discovery through @ConfigurationPropertiesScan.
Added ConfigurationPropertiesTests to load the test profile, verify all four profile
resources, reject embedded sensitive property names, and assert every configured
algorithm identifier, threshold, weight, duration, allocation, notification time,
XP value, and idempotency retention value against the approved baseline.
Added one narrow .gitignore exception for the repository-safe
backend/src/main/resources/application-local.yml required by acceptance; the broader
machine-local Spring configuration ignore rules remain in force.
No dependency or pom.xml change was required.
```

Implementation-side validation evidence:

```text
.\backend\mvnw.cmd -f backend\pom.xml clean verify
→ BUILD SUCCESS
→ Tests run: 7, Failures: 0, Errors: 0, Skipped: 0
→ Spring context smoke tests: PASS
→ Typed configuration/profile tests: PASS (4 tests)
→ Modular package structure tests: PASS (2 tests)
→ Executable Spring Boot JAR packaging: PASS

Profile resource safety check
→ application.yml/local/test/prod contain no password, secret, API/access/private key,
  client secret, or token property

python tools/baseline_audit.py
→ BASELINE AUDIT: PASS

python -m py_compile tools/baseline_audit.py
→ PASS

git diff --check
→ PASS

git status --short --untracked-files=all, git diff --stat, and git diff
→ INSPECTED

Scope and provenance checks
→ PASS; no tracked target output, secret, dependency, contract, database/Flyway,
  security behavior, later-task implementation, branch, HEAD, or baseline tag mutation
```

Acceptance state:

```text
application.yml/local/test/prod: SATISFIED
Typed assessment properties: SATISFIED
Typed SRS properties: SATISFIED
Typed personalization properties: SATISFIED
Typed gamification properties: SATISFIED
Typed notification properties: SATISFIED
Typed idempotency properties: SATISFIED
Configured default constants match approved baseline: SATISFIED by focused binding tests
Dependency BE-FND-001: DONE
Implementation-side blockers: NONE
Reviewer gates: AR=PASS; SR=PASS; QAR=PASS
Task status: IN_REVIEW pending actual CI PASS
```

Change impact:

```text
Change: Add Spring application profiles, canonical typed configuration, and focused tests.
Why: Satisfy the approved BE-FND-003 acceptance criteria.
Affected documents: MASTER_BACKLOG.md and EXECUTION_LOG.md lifecycle/evidence only.
Affected API/OpenAPI: None.
Affected database/Flyway: None.
Affected security behavior: None; no credential or secret convention implemented.
Affected business behavior: None; algorithms remain unimplemented and only approved
configuration values are exposed as typed inputs for later tasks.
Affected clients: None.
Migration: None.
Tests: Build/context smoke, profile loading, typed binding/defaults, and secret-name scan.
Backward compatibility: Preserved; no existing contract or dependency changed.
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

### BE-FND-007 — Validation + pagination + mapper conventions

- Status: IN_PROGRESS
- Status history: TODO → IN_PROGRESS
- Branch/worktree mode: `GOV009_DIRECT_MAIN` — uncommitted `main` worktree
- HEAD/origin/main at admission: `cb8b044eef24abfa1275d4555eeb9b9d575e98a2`
- Dependencies: `BE-FND-002` DONE; `BE-FND-005` DONE
- Priority: P0
- Required reviewers: Architecture Reviewer, QA Reviewer
- Acceptance: DTO không expose entity trực tiếp; validation canonical; pagination response thống nhất; client không gửi `isCorrect`.
- Required tests: Theo global DoD + acceptance
- CI admission mode: `PRE_CI_BOOTSTRAP_NA`
- PRE_CI eligible Task ID: `BE-FND-007`
- Remote admission evidence: 0 check runs; 0 status contexts; failing checks/statuses 0
- Existing failing CI check: NONE
- Failed-check waiver: NOT USED

Implementation plan:

```text
Thêm response phân trang dùng chung đúng API/OpenAPI v1.4 và request phân trang có
validation với mặc định page=0, size=20, page >= 0 và size trong khoảng 1..100.
Thiết lập mapper DTO stateless để entity được chuyển thành response DTO thay vì bị
expose trực tiếp. Thêm SubmitLearningAttemptRequest canonical với các trường bắt buộc
và Bean Validation, chủ động không có isCorrect vì backend suy ra từ answerQuality.

Thêm test tập trung cho shape/defaults/bounds của pagination, mapper, ranh giới field
của DTO và validation answerQuality/responseTime. Không triển khai controller,
service, repository, SRS derivation, idempotency, optimistic-lock translation,
correlation ID, database/Flyway, client hoặc hành vi thuộc task tương lai.
```

Implementation and TEST evidence — 2026-09-11:

```text
Implemented:
- PaginatedResponse<T> giữ đúng sáu field canonical, tạo immutable content và hỗ trợ
  chuyển trực tiếp từ Spring Data Page<T>.
- PaginationRequest áp dụng page=0, size=20 và Bean Validation cho page >= 0,
  size trong khoảng 1..100.
- EntityResponseMapper<E,R> thiết lập đường chuyển entity → response DTO cho list/page.
- SubmitLearningAttemptRequest dùng đúng sáu field OpenAPI v1.4, AttemptType canonical,
  required/range validation tiếng Việt và từ chối unknown property như isCorrect.
- ApiConventionTests dùng MockMvc cho serialization/deserialization và canonical error
  envelope; đồng thời kiểm tra mapper boundary và pagination validation.

Test chronology:
- Lệnh mvn đầu tiên không chạy test vì mvn không có trên PATH; chuyển sang Maven Wrapper
  đã pin của repository.
- Focused run đầu tiên: 6 PASS, 1 FAIL do test fixture kỳ vọng 204 trong khi test-only
  void controller trả 200; sửa kỳ vọng fixture, không đổi production contract.
- Focused rerun: 7/7 PASS.
- Final Maven clean verify sau bổ sung required-field coverage: BUILD SUCCESS;
  30 tests PASS, 0 failures, 0 errors, 0 skipped.
- ApiConventionTests final: 8/8 PASS.
- PostgreSQL 16.15 Testcontainers, fresh Flyway V1 và Hibernate schema validation: PASS.
- Existing GlobalExceptionHandlerTests: 10/10 PASS.
- baseline_audit, py_compile, git diff --check, untracked whitespace, scope,
  secret/private-key, generated-file và baseline-tag integrity: PASS.

Scope/contract impact:
- API runtime foundation: thêm DTO/validation/pagination/mapper conventions đúng baseline.
- API/OpenAPI documents: không đổi; implementation tuân theo contract hiện hành.
- Database/Flyway/client/correlation ID/security/business algorithm: không đổi.
- Dependency mới: NONE.

Stop state:
- Task status: IN_PROGRESS.
- Independent reviewer PASS claimed: NO.
- Required independent reviewers next: Architecture Reviewer, QA Reviewer.
- Unresolved implementation blockers: NONE.
- Commit/push/merge/tag mutation: NONE.
```

Independent QA Reviewer evidence — 2026-09-11:

```text
Reviewer: QA Reviewer
Result: FAIL
Finding ID: QA-BE-FND-007-001
Severity: MEDIUM
Blocking: YES
Original Status: OPEN
Current Status: OPEN

Finding:
PaginatedResponse.from(Page<T>) truyền Page.getSize() trực tiếp vào constructor.
Page.empty().getSize() trả 0, xung đột với invariant canonical size >= 1 và làm
conversion của trang rỗng không có Pageable ném IllegalArgumentException.

Required remediation:
Chỉ normalize tại Spring Page conversion boundary; Page.empty() dùng page=0,size=20;
Page.empty(Pageable) giữ metadata Pageable; non-empty Page không đổi; mapper phải xử lý
được cả ba trường hợp. Không nới constructor invariant và không nhân đôi magic value 20.

Remediation owner: CBL
Resolution authority: independent QA Reviewer only
Finding remains OPEN pending focused QA re-review: YES
```

Remediation and TEST evidence for `QA-BE-FND-007-001` — 2026-09-11:

```text
Historical QA Reviewer result: FAIL (preserved)
Finding ID: QA-BE-FND-007-001
Severity: MEDIUM (preserved)
Blocking: YES (preserved)
Original Status: OPEN (preserved)
Current Status: OPEN

Remediation:
- Thêm PaginationConvention làm nguồn chung cho page/size defaults và size bounds;
  không nhân đôi magic value 20 và không tạo dependency response → validation.
- PaginatedResponse constructor vẫn từ chối size=0.
- PaginatedResponse.from(Page<T>) chỉ normalize trường hợp unpaged-empty thành
  page=0, size=20, totalPages=0; content/totalElements rỗng và hasNext=false.
- Page.empty(PageRequest.of(2,50)) giữ page=2,size=50 và zero totals.
- Non-empty Page tiếp tục giữ nguyên metadata Spring Data.
- EntityResponseMapper.toResponsePage(...) xử lý non-empty, unpaged-empty và
  explicit-pageable-empty; mapper không được invoke khi không có entity.

Regression chronology:
- Focused run đầu tiên sau remediation: 9 PASS, 2 FAIL; test phát hiện Spring Data
  Page.empty().getTotalPages() trả 1. Boundary normalization được bổ sung cho đúng
  canonical totalPages=0.
- Focused ApiConventionTests cuối: 12/12 PASS.
- Maven clean verify cuối: BUILD SUCCESS; 34 tests PASS; 0 failures/errors/skips.
- PostgreSQL 16.15 Testcontainers, fresh Flyway V1 và Hibernate validation: PASS.
- baseline_audit, py_compile, git diff --check, scope, secret/private-key,
  generated-file, untracked whitespace và baseline-tag integrity: PASS.

Scope:
- API/OpenAPI documents, production endpoints, database/Flyway/client code,
  BE-FND-006 và answerQuality/isCorrect contract: không đổi.
- Dependency mới: NONE.

Stop state:
- BE-FND-007: IN_PROGRESS.
- QA-BE-FND-007-001: OPEN pending independent focused QA re-review.
- Finding marked RESOLVED by CBL: NO.
- Reviewer PASS claimed by CBL: NO.
- Commit/push/merge/tag mutation: NONE.
```

TEST rerun evidence for `BE-FND-007` — 2026-09-11:

```text
Command boundary: test
Focused ApiConventionTests: BUILD SUCCESS; 12 tests PASS; 0 failures/errors/skips
Full Maven clean verify: BUILD SUCCESS; 34 tests PASS; 0 failures/errors/skips
PostgreSQL 16.15 Testcontainers + fresh Flyway V1 + Hibernate validation: PASS
Acceptance regression coverage: PASS for pagination defaults/bounds, canonical page
shape, non-empty mapper, Page.empty(), Page.empty(PageRequest.of(2,50)), empty mapper,
constructor size invariant, DTO boundary, validation envelope and isCorrect rejection
QA-BE-FND-007-001 historical QA FAIL and Original Status OPEN: PRESERVED
QA-BE-FND-007-001 Current Status: OPEN pending independent focused QA re-review
BE-FND-007 status: IN_PROGRESS
Reviewer PASS claimed: NO
Commit/push/merge/tag mutation: NONE
```

Finalization attempt for `BE-FND-007` — 2026-09-11:

```text
Command boundary: finalize
Result: FINALIZATION_BLOCKED
Lifecycle transition: NONE; BE-FND-007 remains IN_PROGRESS
Required Architecture Reviewer PASS: NOT PRESENT
Historical QA Reviewer result: FAIL (preserved)
QA-BE-FND-007-001 Severity MEDIUM / Blocking YES / Original Status OPEN: PRESERVED
QA-BE-FND-007-001 Current Status: OPEN
Required focused QA re-review PASS and reviewer-owned RESOLVED disposition: NOT PRESENT
Unresolved findings: QA-BE-FND-007-001
Remote origin/main cb8b044eef24abfa1275d4555eeb9b9d575e98a2:
0 check runs; 0 status contexts; failing checks/statuses 0
Existing failing CI check: NONE
TEST gates: PASS from the immediately preceding unchanged-code test run
Self-review/reviewer PASS fabricated: NO
Commit/push/merge/tag mutation: NONE
Required next action: independent QA Reviewer focused re-review; Architecture Reviewer
PASS must also be present before finalization can be retried.
```

Independent reviewer evidence synchronization for `BE-FND-007` — 2026-09-11:

```text
Synchronization scope: completed independent reviewer reports only
Lifecycle transition: NONE; BE-FND-007 remains IN_PROGRESS

Chronology preserved:
1. Initial independent QA Review: FAIL
   - Finding: QA-BE-FND-007-001
   - Severity: MEDIUM
   - Blocking: YES
   - Original Status: OPEN
2. Remediation:
   - Fixed Page.empty() conversion.
   - Added shared PaginationConvention constants.
   - Added regression tests for Page.empty(), Page.empty(Pageable), constructor
     invariants and mapper behavior.
3. Independent focused QA re-review: PASS
   - QA-BE-FND-007-001 Final Status: RESOLVED
   - New findings: NONE
   - QAR: PASS
4. Independent Architecture re-review: PASS
   - Architecture findings: NONE
   - AR: PASS

Evidence precedence clarification:
- Historical QA FAIL and Original Status OPEN remain immutable and preserved above.
- The Architecture Reviewer statement that Architecture review does not itself resolve
  QA-BE-FND-007-001 remains true for Architecture Reviewer authority.
- The completed independent focused QA re-review is the authoritative resolution event
  for QA-BE-FND-007-001 and is not overridden by the Architecture Reviewer statement.

Final reviewer state:
- AR = PASS
- QAR = PASS
- QA-BE-FND-007-001 = RESOLVED
- Unresolved findings = NONE

Evidence sync boundaries:
- Product code/tests/API/OpenAPI/Flyway/clients/baseline tags: NOT MODIFIED
- Self-review or fabricated reviewer PASS: NO
- Commit/push/merge/tag mutation: NONE
- BE-FND-007 status: IN_PROGRESS
```

Finalization evidence for `BE-FND-007` — 2026-09-11:

```text
Command boundary: finalize
Workflow mode: GOV009_DIRECT_MAIN
Lifecycle transition: IN_PROGRESS → DONE
Dependency BE-FND-002: DONE
Dependency BE-FND-005: DONE
Acceptance: PASS

Independent reviewer gates:
- Architecture Reviewer: PASS; architecture findings NONE; AR=PASS
- QA Reviewer chronology: historical FAIL preserved; focused QA re-review PASS
- QA-BE-FND-007-001: Severity MEDIUM / Blocking YES / Original Status OPEN preserved
- QA-BE-FND-007-001 Final Status: RESOLVED by independent focused QA re-review
- New findings: NONE
- Unresolved findings: NONE

Required TEST gates:
- Initial finalization rerun: BUILD FAILURE caused only by unavailable Docker daemon;
  31 non-container tests passed and 3 PostgreSQL Testcontainers tests had environment
  setup errors; no assertion failure. This failed attempt is preserved in chronology.
- Docker Desktop availability restored; no repository file was changed for recovery.
- Final Maven clean verify rerun: BUILD SUCCESS; 34 tests PASS;
  0 failures/errors/skips.
- ApiConventionTests: 12/12 PASS.
- PostgreSQL 16.15 Testcontainers + fresh Flyway V1 + Hibernate validation: PASS.

CI / PRE_CI evidence:
- CI mode: PRE_CI_BOOTSTRAP_NA
- PRE_CI eligible Task ID: BE-FND-007
- CI-FND-001 effective: NO; task status TODO
- HEAD/origin/main: cb8b044eef24abfa1275d4555eeb9b9d575e98a2
- Repository workflow directory: NONE
- Recorded remote evidence for unchanged origin/main: 0 check runs; 0 status contexts;
  failing checks/statuses 0
- Existing failing CI check: NONE
- Failed-check waiver: NOT USED

Final guards:
- baseline_audit: PASS
- py_compile: PASS
- git diff --check and cached diff check: PASS
- Scope and untracked-whitespace checks: PASS; whitespace findings 0
- Secret/private-key/credential scan: PASS; findings 0
- Generated-file guard: PASS; backend/target is ignored and not tracked
- API/OpenAPI/database/Flyway/client diff: NONE
- Baseline-tag integrity: PASS; tag objects and peeled commits unchanged
- Git operation in progress: NONE

Milestone state:
- M1 execution progress: 10 / 29 (34.5%)
- M1 status: IN_PROGRESS

Stop state:
- BE-FND-007: DONE
- Self-review or fabricated reviewer PASS: NO
- Commit/push/merge/tag mutation: NONE
- Required next step: one final task-scoped commit on main, then push main via the
  English AI Coach Git workflow.
```

### QA-FND-002 — OpenAPI/runtime contract-test harness

- Status: IN_PROGRESS
- Status history: TODO → IN_PROGRESS
- Branch/worktree mode: `GOV009_DIRECT_MAIN` — uncommitted `main` worktree
- HEAD/origin/main at admission: `a62740e490aa5321a79b6be87fa05c23bb0e3643`
- Dependencies: `BE-FND-007` DONE
- Priority: P0
- Required reviewers: Architecture Reviewer, QA Reviewer
- Acceptance: OpenAPI v1.4 parse/validate trong CI; DTO/controller/status/error contract có test khung.
- Required tests: Theo global DoD + acceptance
- Started at: 2026-09-12
- CI admission mode: `PRE_CI_BOOTSTRAP_NA`
- PRE_CI eligible Task ID: `QA-FND-002`
- CI status reason: `CI-FND-001` chưa effective và `QA-FND-002` nằm trong exact eligible list
- Remote admission evidence: `main == origin/main`; public GitHub check page cho current SHA không hiển thị check run
- Existing failing CI check: NONE observed
- Failed CI check waiver: NOT USED
- Self-review status: PROHIBITED — implementation session không được tự thỏa QAR gate

Implementation plan:

```text
Add a test-scoped OpenAPI 3.1 parser and a reusable contract-test support utility that
extracts the canonical YAML block from the approved OpenAPI v1.4 Markdown source.
Fail Maven tests when parsing/validation reports messages or the canonical inventory,
operation IDs, internal references, request/response/status contracts drift.

Add focused tests connecting the existing BE-FND-007 runtime foundation to OpenAPI:
SubmitLearningAttemptRequest properties/required fields/bounds, controller operation
metadata and response status set, and ApiErrorResponse shape. Preserve existing product
code and approved API/OpenAPI; do not implement future controllers or business behavior.
```

PLAN admission evidence:

```text
Mode: GOV009_DIRECT_MAIN
Branch: main
Worktree at admission: CLEAN
main == origin/main: YES
Git operation in progress: NONE
Other direct-main task active: NONE
Task status before PLAN: TODO
Owner: QAR
Dependency BE-FND-007: DONE
Canonical sources, acceptance, scope/out-of-scope and reviewer mapping: IDENTIFIED
CI-FND-001 effective: NO
CI mode for exact task: PRE_CI_BOOTSTRAP_NA
Existing failing CI check: NONE observed
baseline_audit, py_compile, git diff --check and baseline-tag integrity: PASS
Lifecycle transition: TODO → IN_PROGRESS
Commit/push/merge/tag mutation: NONE
```

IMPLEMENT evidence:

```text
Added test-scoped io.swagger.parser.v3:swagger-parser:2.1.46.
Added OpenApiContractTestSupport to extract and parse the first YAML block directly
from docs/api/English_AI_Coach_OpenAPI_Swagger_v1_4.md, locate operations/schemas,
compare Java record properties and inspect response status contracts.
Added OpenApiContractHarnessTests covering:
- OpenAPI 3.1.0 parse/validation and canonical info version 1.4.0;
- canonical inventory 72 paths / 76 operations and unique non-null operationId;
- SubmitLearningAttemptRequest properties, required fields, bounds and absence of
  client-owned isCorrect;
- POST /learning/attempts operationId, request schema and 200/400/409/429 statuses;
- ApiErrorResponse/ErrorResponse shape and reusable error responses.
README documents how the Maven harness consumes the canonical Markdown source.
Product code, canonical API/OpenAPI, Flyway migrations and clients: UNCHANGED.
```

TEST chronology and evidence:

```text
1. Targeted OpenApiContractHarnessTests: FAIL — 4 tests run, 1 failure.
   Cause: the initial assertion read only legacy Schema.getType(), while Swagger
   Parser represents the OpenAPI 3.1 integer type through Schema.getTypes().
   Contract/product defect: NO.
2. Remediation: added an OpenAPI 3.1-aware schema-type assertion with a legacy
   fallback; no product or canonical-contract change.
3. Targeted OpenApiContractHarnessTests rerun: PASS — 4/4, failures 0, errors 0,
   skipped 0.
4. Maven clean verify: PASS — 38/38, failures 0, errors 0, skipped 0.
5. PostgreSqlHarnessIntegrationTests: PASS — 3/3 against PostgreSQL
   16.15-alpine3.24 through Testcontainers 2.0.5.
6. Fresh-database Flyway: PASS — empty PostgreSQL schema migrated successfully to V1.
7. Hibernate schema validation and package/JAR build: PASS.
8. Repeatability/isolation: PASS — the four OpenAPI tests passed in both the corrected
   targeted JVM and the subsequent clean full-suite JVM; Testcontainers exited with no
   PostgreSQL/Ryuk test container remaining.
9. Dependency resolution: PASS — swagger-parser:2.1.46 resolved with test scope.
```

Governance and scope audits:

```text
baseline_audit: PASS
python -m py_compile tools/baseline_audit.py: PASS
git diff --check: PASS
Changed/untracked whitespace audit: PASS; no newly introduced whitespace error
Secret/private-key/credential diff scan: PASS; findings 0
Generated-file guard: PASS; backend/target and tools/__pycache__ are ignored/untracked
Product-code diff: NONE
API/OpenAPI diff: NONE
Database/Flyway diff: NONE
Client diff: NONE
Baseline-tag integrity: PASS; tag objects unchanged
HEAD == main == origin/main: a62740e490aa5321a79b6be87fa05c23bb0e3643
Git operation in progress: NONE
```

Stop state after EXECUTE:

```text
QA-FND-002: IN_PROGRESS
Acceptance implementation/test evidence: READY FOR INDEPENDENT REVIEW
Required Architecture Reviewer: PENDING
Required independent QA Reviewer: PENDING
QAR ownership/reviewer mapping: SELF_REVIEW_CONFLICT — implementation session did not
self-review and did not record QAR PASS
Unresolved reviewer findings: NONE RECORDED; reviewer gates have not run
PRE_CI_BOOTSTRAP_NA: pending finalization after independent reviewer gates
Commit/push/merge/tag mutation: NONE
Required next step: independent Architecture Review and independent QA Review
```

Independent Architecture Review chronology:

```text
Initial Architecture Review result: FAIL
Finding: ARCH-QA-FND-002-001
Severity: MEDIUM
Blocking: YES
Original Status: OPEN
Finding summary: the initial harness validated the canonical OpenAPI document, DTO,
operation responses and error schemas, but did not compare an actual Spring MVC
Controller/Handler boundary with the OpenAPI operation. The OpenAPI-only learning
attempt assertions could not detect controller path/method/request/status drift.
Finding status before remediation: OPEN
```

Focused remediation for ARCH-QA-FND-002-001:

```text
Added a reusable test-only runtime contract boundary based on
RequestMappingHandlerMapping, RequestMappingInfo and HandlerMethod.
The validator independently extracts the registered runtime path, HTTP method,
@RequestBody Java type and explicit @ResponseStatus, then compares them with the
canonical OpenAPI server base path, operation path/method, request schema reference
and success response.
Added an isolated test-only canonical @RestController fixture for
POST /api/v1/learning/attempts accepting SubmitLearningAttemptRequest and returning
runtime HTTP 200. No production controller was added.
Added an isolated deliberately drifted PUT fixture and a negative test proving the
reusable validator rejects its HTTP-method mismatch against canonical POST.
Preserved the existing DTO bounds, 400/409/429, ApiErrorResponse and reusable error
response assertions.
Swagger Parser remains version 2.1.46 and test-scope only; canonical OpenAPI Markdown
remains the sole OpenAPI source.
ARCH-QA-FND-002-001 final status: OPEN — only an independent Architecture Reviewer may
set RESOLVED after focused re-review.
```

Focused remediation TEST evidence:

```text
1. OpenApiContractHarnessTests: PASS — 6/6, failures 0, errors 0, skipped 0.
2. Runtime Spring MVC handler boundary test run independently: PASS — 1/1.
   RequestMappingHandlerMapping registered POST /api/v1/learning/attempts;
   HandlerMethod accepted SubmitLearningAttemptRequest; @ResponseStatus declared 200;
   MockMvc invoked the registered handler with canonical JSON and observed HTTP 200.
3. Negative controller-drift test run independently: PASS — 1/1.
   The isolated test context registered PUT /api/v1/learning/attempts; the reusable
   validator raised AssertionFailedError because canonical OpenAPI requires POST.
4. Maven clean verify: PASS — 40/40, failures 0, errors 0, skipped 0.
5. PostgreSqlHarnessIntegrationTests: PASS — 3/3 on PostgreSQL 16.15 through
   Testcontainers 2.0.5.
6. Fresh Flyway V1 migration: PASS — empty schema detected, canonical V1 validated and
   applied successfully.
7. Hibernate schema validation and package/JAR build: PASS.
8. The initial targeted-test FAIL and its earlier remediation chronology remain
   unchanged above.
```

Focused remediation stop state:

```text
QA-FND-002: IN_PROGRESS
Architecture Review historical result: FAIL
ARCH-QA-FND-002-001: OPEN
Architecture Reviewer focused re-review: PENDING
Independent QA Reviewer: PENDING
PRE_CI_BOOTSTRAP_NA: pending finalization
Commit/push/merge/tag mutation: NONE
```

Independent QA Review chronology:

```text
Historical QA Review result: FAIL
Finding: QA-QA-FND-002-001
Severity: MEDIUM
Blocking: YES
Original Status: OPEN
Finding summary: the runtime boundary did not compare HandlerMethod response-body Java
type with the canonical success response schema, and operation/reusable error responses
were not proven to resolve through their reference chains to ErrorResponse. The test-only
controller returned void despite canonical 200 application/json requiring
LearningAttemptResponse, allowing a false-green result.
Finding status before remediation: OPEN
```

Focused remediation for QA-QA-FND-002-001:

```text
Extended OpenApiContractTestSupport to unwrap the current ResponseEntity<T> handler
return form using HandlerMethod Java reflection and compare T with the canonical success
response application/json schema resolved from OpenAPI.
Changed only the test fixture to return ResponseEntity<LearningAttemptResponse> and
added test-only LearningAttemptResponse, SrsResult and VocabularyProgressResponse records
whose property sets are checked against the canonical OpenAPI schemas.
Added reusable operation/reusable-response resolvers that follow local
#/components/responses/* and #/components/schemas/* chains with missing-ref and cycle
guards, then require the terminal schema identity.
Canonical POST /learning/attempts now proves 400 -> ValidationError -> ErrorResponse,
409 -> ErrorResponse and 429 -> RateLimited -> ErrorResponse. ValidationError and
RateLimited components are also verified directly.
Added isolated negative fixtures/metadata proving detection of request DTO drift,
response DTO drift and an error response that resolves to LearningAttemptResponse rather
than ErrorResponse. Preserved the isolated HTTP-method drift test from
ARCH-QA-FND-002-001 remediation.
Product code, canonical API/OpenAPI, database/Flyway and clients remain unchanged.
Swagger Parser remains 2.1.46 with test scope only.
ARCH-QA-FND-002-001 status: OPEN
QA-QA-FND-002-001 status: OPEN
Only the corresponding independent reviewer may set either finding RESOLVED.
```

QA finding remediation TEST chronology:

```text
1. First focused OpenApiContractHarnessTests attempt: FAIL at test compilation.
   Cause: a schema-name loop variable in the new test-only resolver was captured by
   assertion-message lambdas without being effectively final. Product/contract defect: NO.
2. Minimal remediation: introduced an immutable per-iteration schemaName variable;
   product and canonical contract remained unchanged.
3. Focused OpenApiContractHarnessTests rerun: PASS — 9/9, failures 0, errors 0, skipped 0.
4. Positive runtime controller response contract test: PASS — 1/1.
5. Negative method/request/response/error-reference drift tests: PASS — 4/4.
6. Positive learning-attempt DTO/status/error-reference-chain test: PASS — 1/1.
7. Maven clean verify: PASS — 43/43, failures 0, errors 0, skipped 0.
8. PostgreSqlHarnessIntegrationTests: PASS — 3/3 on PostgreSQL 16.15 through
   Testcontainers 2.0.5.
9. Fresh Flyway V1 and Hibernate schema validation: PASS; package/JAR build: PASS.
10. The original Swagger Parser representation FAIL/remediation chronology and historical
   Architecture FAIL/OPEN chronology above remain unchanged.
```

QA finding remediation governance audits:

```text
baseline_audit: PASS
python -m py_compile tools/baseline_audit.py: PASS
git diff --check: PASS
Untracked whitespace audit: PASS; findings 0
Secret/private-key/credential diff scan: PASS; findings 0
Generated-file guard: PASS; backend/target and tools/__pycache__ are ignored/untracked
Product-code diff: NONE
API/OpenAPI diff: NONE
Database/Flyway diff: NONE
Client diff: NONE
Active PostgreSQL/Testcontainers/Ryuk test containers after suite: NONE
Swagger Parser dependency scope: 2.1.46 test only
Baseline-tag integrity: PASS; tag objects and peeled commits unchanged
HEAD == main == origin/main: a62740e490aa5321a79b6be87fa05c23bb0e3643
Git operation in progress: NONE
```

Stop state before independent re-review:

```text
QA-FND-002: IN_PROGRESS
Historical Architecture Review: FAIL
ARCH-QA-FND-002-001: OPEN
Historical QA Review: FAIL
QA-QA-FND-002-001: OPEN
Architecture Reviewer focused re-review: PENDING
Independent QA Reviewer focused re-review: PENDING
SELF_REVIEW_CONFLICT: QA task implementer did not self-review or record QAR PASS
PRE_CI_BOOTSTRAP_NA: pending finalization
Commit/push/merge/tag mutation: NONE
```

Additional focused remediation for QA-QA-FND-002-001 — reusable error set:

```text
The finding remained OPEN after the prior remediation because only ValidationError and
RateLimited reusable components were individually dereferenced. Unauthorized, Forbidden,
NotFound and Conflict were protected only by exact component-name membership and could
still drift to another valid schema without failing the suite.
Derived the complete canonical reusable error response set directly from OpenAPI v1.4:
ValidationError, Unauthorized, Forbidden, NotFound, Conflict and RateLimited.
Added an explicit canonical set plus a table-driven loop that invokes
assertReusableResponseResolvesToSchema for every member. Exact set equality makes removal
or unreviewed addition fail; per-member dereferencing makes schema drift fail.
Added a dedicated isolated negative regression using the same reusable validator:
Unauthorized intentionally resolves to LearningAttemptResponse and is rejected instead
of silently passing as ErrorResponse.
Existing operation-level 400/409/429 resolution and all controller/request/response/error
positive and negative coverage remain unchanged.
ARCH-QA-FND-002-001 status: OPEN
QA-QA-FND-002-001 status: OPEN
No reviewer result or historical evidence was rewritten.
```

Additional remediation focused TEST evidence:

```text
1. OpenApiContractHarnessTests: PASS — 11/11, failures 0, errors 0, skipped 0.
2. Positive all-reusable-error-response resolution test: PASS — 1/1.
3. Negative reusable-response drift test: PASS — 1/1.
4. Existing negative method/request/response/operation-error tests plus new reusable
   error drift test: PASS — 5/5.
5. Maven clean verify: PASS — 45/45, failures 0, errors 0, skipped 0.
6. PostgreSqlHarnessIntegrationTests: PASS — 3/3; fresh Flyway V1 from empty schema,
   Hibernate schema validation and package/JAR build: PASS.
7. Earlier Swagger Parser representation failure, Architecture review FAIL/OPEN, QA review
   FAIL/OPEN and all prior remediation attempts remain preserved above.
```

Additional remediation governance audits and stop state:

```text
baseline_audit: PASS
python -m py_compile tools/baseline_audit.py: PASS
git diff --check and untracked whitespace audit: PASS
Secret/private-key/credential diff scan: PASS; findings 0
Generated-file guard: PASS; generated outputs are ignored and untracked
Product/API/OpenAPI/database/Flyway/client diff: NONE
Active PostgreSQL/Testcontainers/Ryuk test containers after suite: NONE
Swagger Parser dependency: 2.1.46, test scope only
Baseline-tag integrity: PASS; tag objects and peeled commits unchanged
HEAD == main == origin/main: a62740e490aa5321a79b6be87fa05c23bb0e3643
QA-FND-002: IN_PROGRESS
Historical Architecture Review: FAIL; ARCH-QA-FND-002-001: OPEN
Historical QA Review: FAIL; QA-QA-FND-002-001: OPEN
Focused Architecture and independent QA re-reviews: PENDING
PRE_CI_BOOTSTRAP_NA: pending finalization
Commit/push/merge/tag mutation: NONE
```

Independent reviewer evidence synchronization — QA-FND-002:

```text
Evidence source: chỉ các báo cáo độc lập đã hoàn tất trong các reviewer task;
implementation session không tự review và không tạo reviewer result.

Chronology preserved:
1. Initial Architecture Review: FAIL
   Finding: ARCH-QA-FND-002-001
   Severity: MEDIUM
   Blocking: YES
   Original Status: OPEN
   Historical Architecture finding/status remains immutable in the preceding record.
2. Remediation chronology above remains unchanged: runtime Controller ↔ OpenAPI
   boundary, response DTO/schema boundary, operation/reusable ErrorResponse resolution,
   complete canonical reusable-response set and isolated drift regressions were added
   in test scope only; product code and canonical API/OpenAPI remain unchanged.
3. Independent QA Re-review report: PASS
   QAR = PASS
   Finding: QA-QA-FND-002-001
   Status: RESOLVED
   Historical QAR FAIL, MEDIUM/YES, Original Status OPEN preserved.
   QA independently verified the remediation and 11/11 OpenApiContractHarnessTests,
   4/4 focused response/error remediation tests and 45/45 full Maven tests PASS.
   At that review point the Architecture finding was still recorded OPEN/pending
   Architecture re-review; QA did not claim an Architecture result.
4. Focused independent Architecture Re-review report: PASS
   AR = PASS
   Finding: ARCH-QA-FND-002-001
   Status: RESOLVED
   Historical Architecture FAIL, MEDIUM/YES, Original Status OPEN preserved.
   Architecture independently verified the runtime HandlerMethod boundary, response
   type comparison, isolated method-drift regression and 11/11, 45/45 validation
   evidence. At that review point the QA finding was still recorded OPEN; Architecture
   did not claim a QAR result.

Synchronized final reviewer state:
AR = PASS
QAR = PASS
ARCH-QA-FND-002-001 = RESOLVED
QA-QA-FND-002-001 = RESOLVED
Unresolved reviewer findings = NONE

Task/lifecycle state remains unchanged:
QA-FND-002: IN_PROGRESS
PRE_CI_BOOTSTRAP_NA: pending finalization
No reviewer result, historical FAIL, original OPEN status, or prior remediation
chronology was rewritten. This entry records independent evidence only.
Commit/push/merge/tag mutation: NONE
```

Finalization evidence for `QA-FND-002` — 2026-09-12:

```text
Command boundary: finalize
Workflow mode: GOV009_DIRECT_MAIN
Lifecycle transition: IN_PROGRESS → DONE

Task / ownership / dependency:
- Task: QA-FND-002 — OpenAPI/runtime contract-test harness
- Owner: QAR
- Dependency BE-FND-007: DONE
- Priority: P0
- Acceptance: PASS — OpenAPI v1.4 parse/validate và DTO/controller/status/error
  contract harness đã được kiểm chứng.

Independent reviewer gates:
- Architecture Reviewer: PASS; AR = PASS; Architecture findings mới = NONE
- QA Reviewer: PASS; QAR = PASS; findings mới = NONE
- ARCH-QA-FND-002-001: RESOLVED bởi focused independent Architecture Re-review
- QA-QA-FND-002-001: RESOLVED bởi focused independent QA Re-review
- Historical Architecture/QA FAIL, MEDIUM, Blocking YES, Original Status OPEN:
  preserved unchanged in the preceding chronology.
- Unresolved findings: NONE
- Self-review or fabricated reviewer PASS: NO

TEST / validation gates:
- Focused OpenApiContractHarnessTests: PASS — 11/11
- Maven clean verify: PASS — 45/45; 0 failures, errors or skips
- PostgreSQL 16.15 Testcontainers: PASS — 3/3
- Fresh Flyway V1 from empty schema: PASS
- Hibernate schema validation and package/JAR build: PASS
- Repeatability/isolation and prior drift/error regression coverage: PASS
- baseline_audit.py: PASS
- python -m py_compile tools/baseline_audit.py: PASS
- git diff --check and cached diff check: PASS
- Untracked whitespace audit: PASS; findings 0
- Secret/private-key/credential scan: PASS; findings 0
- Generated-file guard: PASS; generated outputs are not tracked
- Product/API/OpenAPI/database/Flyway/client scope guard: PASS; no out-of-scope
  product or canonical contract changes
- Baseline-tag integrity: PASS; baseline refs unchanged
- Git operation in progress: NONE

CI / PRE_CI evidence:
- CI admission mode: PRE_CI_BOOTSTRAP_NA
- PRE_CI eligible Task ID: QA-FND-002
- CI-FND-001 effective: NO
- Failed-check waiver: NOT USED
- PRE_CI_BOOTSTRAP_NA: accepted as the valid CI mode for this eligible task;
  CI-FND-001 bootstrap remains not effective globally.

Milestone state:
- M1 execution progress: 11 / 29 (37.9%)
- M1 status: IN_PROGRESS

Final state:
QA-FND-002: DONE
No commit, push, merge, branch, rebase, reset or tag mutation performed.
Required next step: one final task-scoped commit on main, then push main via the
English AI Coach Git workflow.
```

## CI-FND-001 — PLAN / admission — 2026-09-13

```text
Command boundary: execute
Workflow mode: GOV009_DIRECT_MAIN
CI mode: ACTUAL_CI_BOOTSTRAP
Lifecycle transition: TODO → IN_PROGRESS

Admission evidence:
- Branch: main
- Worktree before PLAN: clean
- HEAD == main == origin/main: b88f0a3bbd4d6af5d3b7c51ef0c4548c687ef1e9
- Git operation in progress: NONE
- Other active direct-main task: NONE
- Dependencies: QA-FND-001 DONE; QA-FND-002 DONE; GOV-004 DONE
- Repository health blocker: NONE; CI-FND-001 is not yet effective
- PRE_CI_BOOTSTRAP_NA: NOT USED
- OWNER_AUTHORIZED_GOVERNANCE_TRANSITION: NOT USED

Canonical sources checked:
- docs/PROJECT_RULES.md
- docs/planning/IMPLEMENTATION_PLAN.md
- docs/planning/MASTER_BACKLOG.md
- docs/technical/English_AI_Coach_Technical_Specification_v1.2.md, Section 83
- docs/technical/English_AI_Coach_Backend_Technical_Specification_v1.3.md, Section 131
- docs/agents/CODEX_BACKEND_LEAD.md
- Required reviewer instructions: AR, DBR, SR, QAR

PLAN:
1. Add one mandatory GitHub Actions workflow for push to main, pull requests and
   manual dispatch, with read-only repository contents permission.
2. Preserve the exact gate order: baseline audit → build/static checks → unit tests
   → PostgreSQL/Testcontainers integration tests → OpenAPI contract tests → package.
3. Use Java 21, the Maven Wrapper and the existing Python audit dependency; pin
   first-party actions to immutable commit SHAs.
4. Add a narrow repository audit that rejects trigger, permission, action-pin,
   runtime or gate-order drift in the workflow.
5. Run focused workflow checks, every pipeline-equivalent stage, Maven clean verify,
   PostgreSQL/Testcontainers, baseline/py_compile/diff/scope/secret/generated-file/
   whitespace/tag audits. Keep the task IN_PROGRESS and stop before review.

Scope:
- In scope: CI workflow, CI workflow audit, CI-FND-001 lifecycle/test evidence.
- Out of scope: product behavior, API/OpenAPI, database/Flyway, clients, secrets,
  reviewer decisions, commit/push, baseline-tag mutation and CI effective-state closure.
- Required reviewers after TEST: AR, DBR, SR, QAR.
```

## CI-FND-001 — IMPLEMENT / TEST — 2026-09-13

```text
Implementation:
- Added .github/workflows/ci.yml as the single Required CI workflow.
- Triggers: push to main, pull_request and workflow_dispatch.
- Workflow permissions: contents: read only; no secret or write permission added.
- Runtime: ubuntu-latest, Python 3.13, Temurin Java 21, Maven dependency cache.
- First-party actions are pinned to immutable 40-character commit SHAs:
  actions/checkout v7.0.1, actions/setup-python v7.0.0,
  actions/setup-java v6.0.1. The tag-to-commit mappings were verified against
  the official GitHub repositories on 2026-09-13.
- Canonical execution order is explicit in one fail-fast sequential job:
  baseline audit → build/static checks → unit tests → PostgreSQL integration tests
  → OpenAPI contract tests → package.
- The Maven Wrapper is invoked through sh because backend/mvnw is intentionally
  tracked without the executable bit; no wrapper or index metadata was mutated.
- Added tools/ci_workflow_audit.py to guard triggers, read-only permissions,
  immutable action pins, runtime versions, exact gate order and required commands.
- Added tools/test_ci_workflow_audit.py with one positive and two negative
  regressions for the live workflow, an unpinned action and gate-order drift.
- Product code, API/OpenAPI, database/Flyway and clients: unchanged.

Focused TEST evidence:
1. ci_workflow_audit.py: PASS.
2. CiWorkflowAuditTests: PASS — 3/3.
3. py_compile for baseline/workflow audit and workflow audit tests: PASS.
4. CI build/static Maven command: PASS — 32 production and 11 test sources
   compiled with release 21.
5. CI unit-test selector: PASS — 31/31; failures 0, errors 0, skipped 0.
6. CI PostgreSQL integration selector: PASS — 3/3 on PostgreSQL 16.15 via
   Testcontainers 2.0.5; fresh Flyway V1 and Hibernate validation PASS.
7. CI OpenAPI selector: PASS — 11/11; failures 0, errors 0, skipped 0.
8. CI package command: PASS — executable Spring Boot JAR produced.
9. Maven clean verify after the final implementation: PASS — 45/45; failures 0,
   errors 0, skipped 0; package PASS.

Governance / repository audits:
- baseline_audit: PASS.
- git diff --check: PASS.
- Untracked whitespace audit: PASS; findings 0.
- Scope audit: PASS; only CI workflow/audit/tests and CI-FND-001 planning evidence.
- Secret/private-key/credential diff scan: PASS; findings 0.
- Generated-file audit: PASS; backend/target and tools/__pycache__ remain ignored
  and untracked.
- Baseline-tag integrity: PASS; local and origin tag objects/peeled commits match.
- Active PostgreSQL/Testcontainers/Ryuk containers after tests: NONE.
- HEAD == main == origin/main: b88f0a3bbd4d6af5d3b7c51ef0c4548c687ef1e9.
- Git operation in progress: NONE.

Stop state before independent review and bootstrap publication:
- CI-FND-001: IN_PROGRESS.
- CI mode: ACTUAL_CI_BOOTSTRAP.
- Architecture Reviewer: PENDING.
- Database Reviewer: PENDING.
- Security Reviewer: PENDING.
- Independent QA Reviewer: PENDING.
- Reviewer findings for CI-FND-001: not yet assessed; no PASS fabricated.
- Actual remote CI: NOT RUN because workflow changes are intentionally uncommitted.
- CI-FND-001 effective: NO.
- PRE_CI_BOOTSTRAP_NA: NOT USED.
- OWNER_AUTHORIZED_GOVERNANCE_TRANSITION: NOT USED.
- Commit/push/merge/tag mutation: NONE.
- Required next boundary: independent REVIEW while task remains IN_PROGRESS;
  bootstrap commit/push and actual remote CI belong to the later Git workflow.
```

## CI-FND-001 — remediation DB-CI-FND-001-001 — 2026-09-13

```text
Command boundary: remediate
Workflow mode: GOV009_DIRECT_MAIN
CI mode: ACTUAL_CI_BOOTSTRAP
Task status: IN_PROGRESS (unchanged)

Historical independent reviewer chronology preserved:
1. Architecture Reviewer: PASS — historical result preserved; not recreated by the
   implementation session.
2. Database Reviewer: FAIL.
   Finding: DB-CI-FND-001-001
   Severity: MEDIUM
   Blocking: YES
   Original Status: OPEN
3. The Database finding remains OPEN. Only an independent focused Database Reviewer
   re-review may mark it RESOLVED.

Focused remediation:
- Unit selector changed from named-class exclusions to the complete convention:
  -Dtest='!*IntegrationTests,!OpenApiContractHarnessTests'
- PostgreSQL integration selector remains:
  -Dtest='*IntegrationTests'
- OpenApiContractHarnessTests remains excluded from Unit and selected only by the
  dedicated OpenAPI contract gate.
- Canonical gate order remains unchanged: Baseline audit → Build/static → Unit
  → PostgreSQL integration → OpenAPI contract → Package.
- No continue-on-error, waiver or failure suppression was introduced.
- ci_workflow_audit now requires the complete wildcard exclusion for Unit and the
  complete wildcard inclusion for PostgreSQL integration; named-class regression is
  rejected with UNIT_SELECTOR_DRIFT.
- Audit tests now include an isolated LearningTransactionIntegrationTests name to
  prove that enumerating known/future class names is not accepted. No production or
  integration test class was added for this governance-only fixture.

Remediation TEST evidence:
1. python tools/ci_workflow_audit.py: PASS.
2. python -m unittest tools.test_ci_workflow_audit: PASS — 5/5.
   - canonical complete wildcard selector: PASS
   - named PostgreSqlHarnessIntegrationTests + LearningTransactionIntegrationTests
     exclusions: correctly rejected
   - missing *IntegrationTests exclusion: correctly rejected
   - existing action-pin and gate-order negative regressions: PASS
3. py_compile for ci_workflow_audit.py, test_ci_workflow_audit.py and
   baseline_audit.py: PASS.
4. Unit selector independently: PASS — 31/31; executed no *IntegrationTests and did
   not execute OpenApiContractHarnessTests.
5. PostgreSQL integration selector independently: PASS — 3/3; executed
   PostgreSqlHarnessIntegrationTests through *IntegrationTests on PostgreSQL 16.15,
   Testcontainers 2.0.5, fresh Flyway V1 and Hibernate validation.
6. OpenAPI selector independently: PASS — 11/11; executed
   OpenApiContractHarnessTests outside Unit.
7. Maven clean verify: PASS — 45/45; failures 0, errors 0, skipped 0.
8. Separate package command: PASS — executable Spring Boot JAR produced.
9. baseline_audit: PASS.
10. git diff --check and untracked whitespace audit: PASS.
11. Scope audit: PASS; product/API/OpenAPI/database/Flyway/client files unchanged.
12. Secret/private-key/credential scan: PASS; findings 0.
13. Generated-file audit: PASS; generated outputs remain ignored and untracked.
14. Baseline-tag integrity: PASS; local and origin tag refs match.
15. Active PostgreSQL/Testcontainers/Ryuk containers after tests: NONE.

Explicit gate attribution confirmation:
- Unit gate DOES NOT execute any *IntegrationTests.
- PostgreSQL integration gate DOES execute *IntegrationTests.
- OpenApiContractHarnessTests remains outside Unit and runs in the OpenAPI gate.

Stop state:
- CI-FND-001: IN_PROGRESS.
- DB-CI-FND-001-001: OPEN.
- Historical Architecture Reviewer PASS: PRESERVED.
- Historical Database Reviewer FAIL: PRESERVED.
- Focused Database Reviewer re-review: REQUIRED.
- Actual remote CI PASS: NOT CLAIMED; no remote CI run occurred.
- CI-FND-001 effective: NO.
- PRE_CI_BOOTSTRAP_NA: NOT USED.
- Commit/push/merge/tag mutation: NONE.
```

## CI-FND-001 — remediation SEC-CI-FND-001-001 / SEC-CI-FND-001-002 — 2026-09-13

```text
Command boundary: remediate
Workflow mode: GOV009_DIRECT_MAIN
CI mode: ACTUAL_CI_BOOTSTRAP
Task status: IN_PROGRESS (unchanged)

Historical reviewer chronology preserved in order:
1. Architecture Reviewer: PASS — historical result preserved; not recreated here.
2. Database Reviewer: FAIL.
   DB-CI-FND-001-001: MEDIUM; Blocking YES; Original Status OPEN; remains OPEN
   pending independent focused Database Reviewer re-review.
3. Security Reviewer: FAIL.
   SEC-CI-FND-001-001: MEDIUM; Blocking YES; Original Status OPEN.
   SEC-CI-FND-001-002: LOW; Blocking NO; Original Status OPEN.
4. Both Security findings remain OPEN. Only an independent Security Reviewer may
   change their final reviewer status.

Focused security remediation:
- Preserved top-level permissions exactly as contents: read.
- Chose the simpler fail-closed job policy: job-level permissions are prohibited.
- ci_workflow_audit iterates every job and emits JOB_PERMISSION_OVERRIDE for any
  job-level permissions value, including mappings, write-all and unknown permissions.
- The audit additionally enforces the exact canonical job set {required}; an extra
  privileged job produces both JOB_PERMISSION_OVERRIDE and JOB_SET_DRIFT.
- actions/checkout now sets persist-credentials: false; the audit requires the
  parsed value to be exactly false.
- Existing trigger, gate-order, immutable action SHA, Java/Python runtime,
  unit/integration separation, OpenAPI and package audits remain enabled.
- DB-CI-FND-001-001 remediation remains unchanged: Unit excludes all
  *IntegrationTests and the PostgreSQL gate selects all *IntegrationTests.
- No pull_request_target, repository/user secret, write permission,
  continue-on-error, shell command derived from a GitHub expression or authenticated
  Git operation after checkout was introduced.

Explicit negative security audit evidence — all correctly rejected:
A. jobs.required.permissions.contents = write → JOB_PERMISSION_OVERRIDE.
B. jobs.required.permissions.id-token = write → JOB_PERMISSION_OVERRIDE.
C. jobs.required.permissions = write-all → JOB_PERMISSION_OVERRIDE.
D. Extra privileged job with security-events: write → JOB_PERMISSION_OVERRIDE and
   JOB_SET_DRIFT.
E. Missing checkout persist-credentials: false → CHECKOUT_CREDENTIAL_PERSISTENCE.
F. checkout persist-credentials: true → CHECKOUT_CREDENTIAL_PERSISTENCE.
Positive canonical workflow, contents: read, no job override and
persist-credentials: false → PASS.

Remediation TEST evidence:
1. python tools/ci_workflow_audit.py: PASS.
2. python -m unittest tools.test_ci_workflow_audit: PASS — 11/11, including all
   six required security negative cases, canonical positive coverage and every
   pre-existing selector/action-pin/gate-order regression.
3. python -m py_compile tools/ci_workflow_audit.py
   tools/test_ci_workflow_audit.py: PASS.
4. Unit CI selector: PASS — 31/31; no *IntegrationTests or
   OpenApiContractHarnessTests executed.
5. PostgreSQL integration selector: PASS — 3/3 on PostgreSQL 16.15 through
   Testcontainers 2.0.5; *IntegrationTests executed.
6. OpenAPI selector: PASS — 11/11; OpenApiContractHarnessTests executed only in
   its dedicated gate.
7. Maven clean verify: PASS — 45/45; failures 0, errors 0, skipped 0.
8. Separate package command: PASS — executable Spring Boot JAR produced.

Stop state before independent focused Security Reviewer re-review:
- CI-FND-001: IN_PROGRESS.
- ACTUAL_CI_BOOTSTRAP: preserved.
- Historical AR PASS: PRESERVED.
- Historical DBR FAIL and DB-CI-FND-001-001 OPEN chronology: PRESERVED.
- Historical SR FAIL: PRESERVED.
- SEC-CI-FND-001-001: OPEN.
- SEC-CI-FND-001-002: OPEN.
- Actual remote CI PASS: NOT CLAIMED; workflow remains uncommitted/unpushed.
- PRE_CI_BOOTSTRAP_NA: NOT USED.
- Commit/push/merge/tag mutation: NONE.
```

Final governance audit addendum for the Security remediation:

```text
- baseline_audit: PASS.
- git diff --check: PASS.
- Untracked whitespace audit: PASS; findings 0.
- Scope audit: PASS; only CI workflow/audit/tests and CI-FND-001 evidence changed.
- Secret/private-key audit: PASS; findings 0.
- Generated-file audit: PASS; backend/target and tools/__pycache__ remain ignored
  and untracked.
- Baseline-tag integrity: PASS; local and origin tag objects/peeled commits match.
- Security workflow invariant scan: PASS; no pull_request_target,
  continue-on-error or shell command built from GitHub expression input.
- Authenticated/post-checkout Git commands: NONE.
- Active PostgreSQL/Testcontainers/Ryuk containers after tests: NONE.
- HEAD == main == origin/main: b88f0a3bbd4d6af5d3b7c51ef0c4548c687ef1e9.
- Git operation in progress: NONE.
- Actual remote CI result: NOT OBSERVED / NOT CLAIMED.
```

## CI-FND-001 — independent reviewer evidence synchronization — 2026-09-13

```text
Command boundary: synchronize independent reviewer evidence
Evidence source: completed independent AR, DBR, SR and QAR reports supplied by
the task owner in the synchronization request. This entry is evidence
synchronization only; it is not self-review and does not change task lifecycle.

Historical chronology preserved:
- Database Reviewer FAIL
  → DB-CI-FND-001-001 OPEN (MEDIUM; Blocking YES; Original Status OPEN)
  → selector/audit remediation
  → focused Database Reviewer PASS
  → DB-CI-FND-001-001 RESOLVED.
- Security Reviewer FAIL
  → SEC-CI-FND-001-001 OPEN (MEDIUM; Blocking YES; Original Status OPEN)
  → SEC-CI-FND-001-002 OPEN (LOW; Blocking NO; Original Status OPEN)
  → permission/checkout remediation
  → focused Security Reviewer PASS for 001
  → SEC-CI-FND-001-001 RESOLVED
  → focused Security Reviewer PASS for 002
  → SEC-CI-FND-001-002 RESOLVED.

Final independent reviewer state from supplied reports:
- Architecture Reviewer: PASS; findings NONE.
- Database Reviewer: PASS; DB-CI-FND-001-001 RESOLVED; new findings NONE.
- Security Reviewer: PASS; SEC-CI-FND-001-001 RESOLVED;
  SEC-CI-FND-001-002 RESOLVED; new findings NONE.
- QA Reviewer: PASS; findings NONE.
- AR=PASS; DBR=PASS; SR=PASS; QAR=PASS.
- Unresolved findings: NONE.

Synchronized evidence highlights:
- Canonical gate order remains Baseline → Build/static → Unit → PostgreSQL
  integration → OpenAPI → Package.
- Unit excludes the complete `*IntegrationTests` pattern and PostgreSQL
  integration selects the complete `*IntegrationTests` pattern.
- Top-level permissions are exactly `contents: read`; job-level permission
  overrides are prohibited and the exact job set is guarded.
- `actions/checkout` uses `persist-credentials: false` and the audit rejects
  missing/true values.
- QA evidence records workflow audit PASS, audit regressions 11/11, Build/static
  PASS, Unit 31/31, PostgreSQL integration 3/3, OpenAPI 11/11, Maven clean
  verify 45/45 and Package PASS.
- No product/API/database/client architecture changes were reported.

Required unchanged task/governance state:
- CI-FND-001: IN_PROGRESS.
- CI mode: ACTUAL_CI_BOOTSTRAP.
- Actual remote CI: NOT RUN; no actual CI PASS claimed.
- CI-FND-001 effective: NO.
- PRE_CI_BOOTSTRAP_NA: NOT USED.
- Commit/push/merge/tag mutation: NONE.
```

## CI-FND-001 — FINALIZE after bootstrap CI PASS — 2026-09-13

```text
Command boundary: finalize
Workflow mode: GOV009_DIRECT_MAIN
CI mode: ACTUAL_CI_BOOTSTRAP

Task and dependency state:
- CI-FND-001 owner/priority: CBL / P0.
- Dependencies: QA-FND-001 DONE; QA-FND-002 DONE; GOV-004 DONE.
- Required reviewers: AR, DBR, SR, QAR.
- Final reviewer gates: AR=PASS; DBR=PASS; SR=PASS; QAR=PASS.
- Unresolved findings: NONE.
- Historical DBR/SR FAIL and original OPEN finding chronology: PRESERVED.

Bootstrap publication evidence:
- Bootstrap commit: 0a2501fb65c0777bd51fa6a64f16b7de3b01dd6a.
- Commit subject: chore(CI-FND-001): bootstrap required CI pipeline.
- Branch/ref: main == origin/main at the bootstrap commit before closure changes.
- GitHub Actions workflow: Required CI (.github/workflows/ci.yml).
- Run ID: 34745300419.
- Event: push.
- Run status/conclusion: completed / success.
- Run URL: https://github.com/diemtrinh05/english-ai-coach/actions/runs/34745300419
- Required CI job: completed / success.
- Successful required steps: Checkout; Setup Python; Install audit dependencies;
  Baseline audit; Setup Java; Build and static checks; Unit tests; PostgreSQL
  integration tests; OpenAPI contract tests; Package.
- Actual remote CI: PASS — observed directly from the GitHub Actions API for the
  exact bootstrap SHA; no waiver or PRE_CI substitution used.

Acceptance and closure gates:
- Canonical pipeline order: PASS.
- Workflow/audit regression evidence: PASS.
- Local TEST evidence: PASS — Unit 31/31; PostgreSQL integration 3/3; OpenAPI
  11/11; Maven clean verify 45/45; Package PASS.
- baseline_audit/build/static/diff/scope/secret/generated-file gates: PASS.
- Baseline-tag integrity: PASS; no tag mutation.
- Product/API/OpenAPI/database/Flyway/client contract change: NONE.
- CI-FND-001 transition: IN_PROGRESS → DONE.
- M1 execution progress: 12 / 29 (41.4%); milestone remains IN_PROGRESS.

Finalization validation after lifecycle/evidence update:
- python tools/ci_workflow_audit.py: PASS.
- python -m unittest -v tools.test_ci_workflow_audit: PASS — 11/11.
- python tools/baseline_audit.py: PASS.
- python -m py_compile tools/baseline_audit.py tools/ci_workflow_audit.py
  tools/test_ci_workflow_audit.py: PASS.
- git diff --check: PASS.
- Scope audit: PASS — only docs/planning/MASTER_BACKLOG.md and
  docs/planning/EXECUTION_LOG.md changed for closure.
- Secret/private-key audit: PASS; findings 0.
- Generated-file audit: PASS; findings 0.
- Baseline-tag integrity: PASS; local/origin baseline tag refs unchanged.

Post-finalize state before Git closure publication:
- CI-FND-001: DONE in the uncommitted closure worktree.
- CI-FND-001 effective: NO — closure commit has not been pushed to origin/main.
- PRE_CI_BOOTSTRAP_NA: NOT USED.
- PRE_CI expiry: NOT YET; it expires irreversibly only after successful closure
  commit push to origin/main.
- Commit/push/merge/tag mutation by Backend Task finalize: NONE.
- Required next boundary: Git workflow creates the small CI-FND-001 closure
  commit and pushes origin/main. At successful push, CI-FND-001 becomes effective
  and PRE_CI expires permanently; the closure/latest-main CI then determines
  repository health as CI_PENDING → HEALTHY or BLOCKED.
```

## BE-FND-003 — FINALIZE after legacy PR CI PASS — 2026-09-13

```text
Command boundary: finalize
Workflow mode: LEGACY_GRANDFATHERED
CI mode: LEGACY_ACTUAL_CI

Task and dependency state:
- BE-FND-003 owner/priority: CBL / P0.
- Dependency: BE-FND-001 DONE.
- Required reviewers: AR, SR, QAR.
- Final reviewer gates from preserved independent evidence: AR=PASS; SR=PASS;
  QAR=PASS.
- QA-BE-FND-003-001: RESOLVED.
- QA-BE-FND-003-002: RESOLVED.
- Unresolved findings: NONE.
- Historical QA FAIL, finding, remediation and re-review chronology: PRESERVED.

Legacy branch and Pull Request evidence:
- Branch: feat/BE-FND-003-application-profiles-config.
- Pull Request: #5; state OPEN; draft false; base main.
- PR head SHA: cb79d1ebf99744a620e085d9939b776b85b3e67a.
- Local HEAD and origin branch matched the PR head before closure changes.
- PR mergeability at finalization: mergeable true; mergeable state clean.
- The semantic origin/main synchronization merge is complete; no Git operation
  remains in progress.

Remediation and local TEST evidence on the exact PR head:
- ConfigurationPropertiesTests uses an isolated ApplicationContextRunner with
  ConfigDataApplicationContextInitializer and a minimal @TestConfiguration /
  @EnableConfigurationProperties boundary for the six BE-FND-003 property
  records; datasource, Flyway and Hibernate/JPA are not bootstrapped.
- Focused ConfigurationPropertiesTests: PASS — 4/4.
- Maven clean verify: PASS — 49/49; failures 0; errors 0; skipped 0.
- PostgreSQL integration selector: PASS — 3/3 on PostgreSQL/Testcontainers;
  production datasource behavior and integration coverage remain unchanged.
- OpenAPI contract selector: PASS — 11/11.
- CI workflow audit: PASS; audit regression tests: PASS — 11/11.
- baseline_audit, py_compile, git diff, conflict-marker, scope,
  secret/private-key, generated-file and baseline-tag integrity gates: PASS.
- Production/API/OpenAPI/database/Flyway/client changes in the isolation
  remediation: NONE.

Actual CI evidence:
- GitHub Actions workflow: Required CI.
- Run ID: 34747195022.
- Event: pull_request.
- Run status/conclusion: completed / success.
- Run URL: https://github.com/diemtrinh05/english-ai-coach/actions/runs/34747195022
- Required CI check: completed / success for exact PR head SHA
  cb79d1ebf99744a620e085d9939b776b85b3e67a.
- Actual remote CI: PASS; no waiver or PRE_CI substitution used.

Acceptance and closure state:
- BE-FND-003 acceptance semantics and canonical profile/property values:
  PRESERVED and satisfied.
- CI-FND-001: DONE and effective on origin/main; PRE_CI is permanently expired.
- PRE_CI_BOOTSTRAP_NA: NOT ELIGIBLE and NOT USED.
- BE-FND-003 transition: IN_REVIEW → DONE.
- M1 execution progress: 13 / 29 (44.8%); milestone remains IN_PROGRESS.
- PR #5 remains OPEN under the preserved legacy workflow.

Finalization validation after lifecycle/evidence update:
- python tools/ci_workflow_audit.py: PASS.
- python -m unittest -v tools.test_ci_workflow_audit: PASS — 11/11.
- python tools/baseline_audit.py: PASS.
- python -m py_compile tools/baseline_audit.py tools/ci_workflow_audit.py
  tools/test_ci_workflow_audit.py: PASS.
- git diff --check and conflict-marker scan: PASS.
- Scope audit: PASS — only docs/planning/MASTER_BACKLOG.md and
  docs/planning/EXECUTION_LOG.md changed for closure.
- Secret/private-key audit: PASS; findings 0.
- Generated-file audit: PASS; findings 0.
- Baseline-tag integrity: PASS; both local baseline tags match origin.

- Commit/push/merge/tag mutation by Backend Task finalize: NONE.
- Required next boundary: Git workflow records this lifecycle/evidence closure
  in the existing legacy branch and pushes it, then completes PR #5 according
  to the preserved legacy merge workflow.
```

## DB-FND-003 — PLAN / admission — 2026-09-13

```text
Command boundary: execute (PLAN → IMPLEMENT → TEST)
Workflow mode: GOV009_DIRECT_MAIN
CI mode: ACTUAL_CI_REPOSITORY_HEALTH

Admission evidence:
- Branch: main.
- HEAD == main == origin/main == remote refs/heads/main:
  b7872194f8cfcf526ba322d284773ccc65a1e87d.
- Worktree at admission: clean.
- Git operation in progress: NONE.
- Other active direct-main task: NONE.
- DB-FND-003 owner/priority/status: CBL / P0 / TODO.
- Dependency: DB-FND-002 DONE.
- Required reviewers: DBR, QAR.
- CI-FND-001: DONE and effective; PRE_CI permanently expired.
- Latest origin/main Required CI run 34747987612: completed / success.
- Repository health: HEALTHY.

Canonical sources and decisions:
- Database Schema v1.6: canonical tables, indexes, partial uniqueness,
  idempotency indexes and optimistic-lock columns/constraints.
- Backend Technical Specification v1.3: due-review query/index, important
  database indexes, append-only Flyway and PostgreSQL Testcontainers coverage.
- System Architecture v1.3: PostgreSQL/Flyway persistence and optimistic
  locking on user_vocabulary_progress and streaks.
- Technical Specification v1.2: PostgreSQL/Flyway/testing conventions.
- PROJECT_RULES.md, IMPLEMENTATION_PLAN.md and MASTER_BACKLOG.md: direct-main
  lifecycle, acceptance, scope and validation gates.

Planned scope:
- Add the next append-only Flyway migration for canonical secondary and partial
  indexes; do not edit the applied V1 migration.
- Add PostgreSQL integration coverage for index definitions, partial uniqueness,
  optimistic-lock target invariants and EXPLAIN sanity of the due-review query.
- Preserve all production table/column/API/client contracts.

Explicitly out of scope:
- Seed data (DB-FND-004), JPA/application behavior, API/OpenAPI, clients,
  unrelated schema changes and later feature queries.
- No dependency addition, commit, push, merge or baseline-tag mutation.

PLAN transition: DB-FND-003 TODO → IN_PROGRESS.
```

## DB-FND-003 — IMPLEMENT / TEST — 2026-09-13

```text
Command boundary: execute — stop after TEST
Workflow mode: GOV009_DIRECT_MAIN
CI mode: ACTUAL_CI_REPOSITORY_HEALTH

Implementation:
- Added append-only Flyway migration V2__create_canonical_indexes.sql; V1 remains
  unchanged.
- Added canonical secondary indexes for authentication, account/assessment,
  vocabulary/learning, quiz/gamification, notification, AI/audit and
  idempotency lookup/retention paths.
- Added canonical partial unique indexes for provider identity, primary goal,
  one in-progress assessment, notification dedupe and reusable AI generation
  keys.
- Preserved the existing version BIGINT NOT NULL DEFAULT 0 and business-key
  uniqueness invariants for user_vocabulary_progress and streaks; no invented
  (id, version) index was added because the canonical contract does not define
  one and the primary-key lookup already supports optimistic updates.
- Added DatabaseIndexIntegrationTests using real PostgreSQL/Testcontainers.

Acceptance evidence:
- Due review: composite (user_id, next_review_at) index exists and EXPLAIN with
  sequential scan disabled selects that exact index.
- Assessment in-progress: partial unique index metadata and duplicate behavior
  PASS.
- Notification dedupe: duplicate DAILY_PLAN/user/local-date is rejected while
  SYSTEM duplicates remain allowed.
- Primary goal: a second primary goal for one user is rejected.
- Optimistic-lock targets: both canonical version columns remain NOT NULL with
  default 0; their canonical uniqueness indexes remain present.
- Idempotency: both (user_id, created_at) and (endpoint, created_at) indexes
  exist.
- Fresh schema: Flyway validates/applies V1 then V2 on PostgreSQL 16.15 and
  Hibernate schema validation PASS.

Final TEST evidence:
- Focused DatabaseIndexIntegrationTests: PASS — 7/7.
- Maven clean verify: PASS — 56/56; failures 0; errors 0; skipped 0.
- CI Unit selector: PASS — 35/35.
- CI PostgreSQL integration selector: PASS — 10/10.
- CI OpenAPI selector: PASS — 11/11.
- Package: PASS — executable Spring Boot JAR produced.
- python tools/ci_workflow_audit.py: PASS.
- python -m unittest -v tools.test_ci_workflow_audit: PASS — 11/11.
- python tools/baseline_audit.py: PASS.
- python -m py_compile tools/baseline_audit.py tools/ci_workflow_audit.py
  tools/test_ci_workflow_audit.py: PASS.
- git diff --check: PASS.
- Untracked whitespace and conflict-marker audits: PASS; findings 0.
- Scope audit: PASS — only DB-FND-003 lifecycle/evidence, V2 migration and its
  PostgreSQL integration test changed.
- Secret/private-key audit: PASS; findings 0.
- Generated-file audit: PASS; findings 0.
- Baseline-tag integrity: PASS; both local baseline tags match origin.
- Active PostgreSQL/Testcontainers/Ryuk containers after tests: NONE.

Contract impact:
- Database: append-only index migration only; no table/column/FK/CHECK/default
  or existing migration changed.
- API/OpenAPI/clients: NONE.
- Dependencies: NONE.
- Backward compatibility: additive indexes and stricter canonical partial
  uniqueness already required by Database Schema v1.6.

Stop state:
- DB-FND-003 remains IN_PROGRESS.
- Independent reviewer PASS is not claimed.
- Unresolved reviewer findings: not yet assessed.
- Required next reviewers: Database Reviewer, then QA Reviewer.
- Actual remote CI for this uncommitted task diff: NOT RUN / NOT CLAIMED.
- Commit/push/merge/tag mutation: NONE.
```

## DB-FND-003 — remediation QA-DB-FND-003-001 — 2026-09-13

```text
Command boundary: remediate QA-DB-FND-003-001
Workflow mode: GOV009_DIRECT_MAIN
Task state: DB-FND-003 IN_PROGRESS

Historical independent QA Reviewer evidence preserved:
- QAR result: FAIL.
- Finding ID: QA-DB-FND-003-001.
- Severity: MEDIUM.
- Blocking: YES.
- Original Status: OPEN.
- Current finding status: OPEN; only independent QAR focused re-review may
  resolve it.
- DBR final PASS: not yet recorded.

Finding:
- Partial-index behavioral coverage did not prove all canonical notification
  types or the allowed-side boundaries for notification dedupe, primary goals
  and assessment in-progress uniqueness.

Migration inspection:
- uq_notifications_user_type_local_date in V2 already has the exact canonical
  predicate: local_notification_date IS NOT NULL and type IN
  (REVIEW_REMINDER, DAILY_PLAN, STREAK).
- Production migration change for this remediation: NONE.
- No V3 migration was created; V1 remains unchanged and V2 remains the next
  unpublished append-only migration.

Focused remediation:
- Parameterized duplicate-rejection coverage now executes independently for
  REVIEW_REMINDER, DAILY_PLAN and STREAK with the same user/type/local date.
- Allowed-side notification coverage proves duplicate SYSTEM rows with the same
  user/date are accepted and duplicate rows with NULL local_notification_date
  are accepted for all three canonical reminder types.
- Metadata coverage additionally verifies the PostgreSQL predicate contains all
  three canonical type values.
- Primary-goal coverage now first inserts multiple is_primary=false goals for
  one user, then proves only the second is_primary=true row is rejected.
- Assessment coverage now inserts COMPLETED and CANCELLED rows for one user,
  then proves only the second IN_PROGRESS row is rejected.

Remediation TEST evidence:
- Focused DatabaseIndexIntegrationTests: PASS — 10/10.
- Maven clean verify: PASS — 59/59; failures 0; errors 0; skipped 0.
- CI Unit selector: PASS — 35/35.
- CI PostgreSQL integration selector: PASS — 13/13.
- CI OpenAPI selector: PASS — 11/11.
- Package: PASS.
- Fresh PostgreSQL 16.15 migration: V1 → V2 validated/applied successfully;
  Hibernate schema validation PASS.

Stop state before independent focused QA re-review:
- DB-FND-003: IN_PROGRESS.
- QA-DB-FND-003-001: OPEN.
- QAR: historical FAIL preserved; remediation does not self-approve PASS.
- DBR final PASS: not recorded.
- Actual remote CI for uncommitted task diff: NOT RUN / NOT CLAIMED.
- Commit/push/merge/tag mutation: NONE.
```

## DB-FND-003 — independent reviewer evidence synchronization — 2026-09-13

```text
Command boundary: synchronize independent reviewer evidence
Workflow mode: GOV009_DIRECT_MAIN
Evidence source: completed read-only reports from the independent Database
Reviewer and QA Reviewer Codex tasks. This operation records reviewer outcomes
only; it is not self-review and does not finalize the task.

Historical chronology preserved:
- Initial Database Reviewer review: PASS; findings NONE.
- QA Reviewer review: FAIL.
  → QA-DB-FND-003-001 OPEN
    (Severity MEDIUM; Blocking YES; Original Status OPEN).
  → focused test-only remediation; production V2 predicate unchanged.
  → independent focused QA re-review: PASS.
  → QA-DB-FND-003-001 RESOLVED; new QA findings NONE.
- Independent post-remediation Database Reviewer review: PASS; Database
  findings NONE. The DBR report did not resolve or claim the QA finding.

Synchronized independent reviewer state:
- DBR: PASS.
- QAR: PASS.
- QA-DB-FND-003-001: RESOLVED by independent focused QA re-review.
- Unresolved findings: NONE.

Reviewer evidence highlights:
- All three canonical notification types (`REVIEW_REMINDER`, `DAILY_PLAN`,
  `STREAK`) have metadata and duplicate-rejection coverage.
- Allowed-side coverage proves duplicate `SYSTEM` notifications and canonical
  notification rows with NULL local_notification_date are accepted.
- Multiple non-primary goals and non-IN_PROGRESS assessments are accepted;
  duplicate primary goals and duplicate IN_PROGRESS assessments are rejected.
- Focused DatabaseIndexIntegrationTests: PASS — 10/10.
- Maven clean verify: PASS — 59/59.
- Fresh Flyway V1 → V2 and Hibernate validation on PostgreSQL 16.15: PASS.
- Migration remains append-only; V1 integrity preserved; no production change
  was introduced by the QA remediation.

Required unchanged stop state:
- DB-FND-003: IN_PROGRESS.
- Branch: main; uncommitted direct-main worktree.
- Actual remote CI for this uncommitted diff: NOT RUN / NOT CLAIMED.
- Commit/push/merge/baseline-tag mutation: NONE.
- Finalization: NOT PERFORMED.
```

## DB-FND-003 — FINALIZE — 2026-09-13

```text
Command boundary: finalize
Workflow mode: GOV009_DIRECT_MAIN
CI mode: ACTUAL_CI_REPOSITORY_HEALTH

Finalization gates:
- Task/owner/priority before transition: DB-FND-003 / CBL / P0 / IN_PROGRESS.
- Dependency DB-FND-002: DONE.
- Required reviewers: DBR, QAR.
- Independent Database Reviewer: PASS; Database findings NONE.
- Independent QA Reviewer: PASS.
- QA-DB-FND-003-001: RESOLVED by independent focused QA re-review.
- Historical QAR FAIL, Severity MEDIUM, Blocking YES and Original Status OPEN
  chronology: PRESERVED.
- Unresolved findings: NONE.
- Acceptance: PASS — canonical due-review, assessment, notification, primary
  goal, optimistic-lock and idempotency indexes are present; due-review EXPLAIN
  selects the expected composite index.

Fresh final validation on the post-review worktree:
- Maven clean verify: PASS — 59/59; failures 0; errors 0; skipped 0.
- DatabaseIndexIntegrationTests within the full run: PASS — 10/10.
- PostgreSQL/Testcontainers 16.15: PASS.
- Flyway fresh migration: successfully validated and applied V1 → V2.
- Hibernate schema validation: PASS.
- OpenApiContractHarnessTests: PASS — 11/11.
- Package/repackaged executable JAR: PASS.
- python tools/ci_workflow_audit.py: PASS.
- python -m unittest -v tools.test_ci_workflow_audit: PASS — 11/11.
- python tools/baseline_audit.py: PASS.
- python -m py_compile applicable audit files: PASS.
- git diff --check: PASS.
- Conflict-marker, untracked-whitespace, secret/private-key, generated-file,
  scope and baseline-tag integrity audits: PASS; findings 0.
- Active PostgreSQL/Testcontainers/Ryuk containers after validation: NONE.

Lifecycle and milestone transition:
- DB-FND-003: IN_PROGRESS → DONE.
- M1 execution progress: 14/29 (48.3%); milestone remains IN_PROGRESS.

Contract/scope impact:
- Database: additive append-only V2 index migration; V1 unchanged.
- API/OpenAPI/clients/security/business rules: NONE.
- QA remediation changed test coverage only; canonical production predicate was
  already correct and remains unchanged.
- Unrelated/later-task/V2-V3 scope: NONE.

Git/publication stop state:
- Branch: main; HEAD and origin/main remain
  b7872194f8cfcf526ba322d284773ccc65a1e87d.
- Actual remote CI for this uncommitted task diff: NOT RUN / NOT CLAIMED;
  direct-main remote CI is the post-push repository-health gate.
- Commit/push/merge/tag mutation: NONE.
- Next step: create the one final task-scoped commit, then fast-forward push
  main through the separate Git workflow.
```

## BE-FND-008 — PLAN — 2026-09-15

```text
Command boundary: execute (PLAN → IMPLEMENT → TEST)
Workflow mode: GOV009_DIRECT_MAIN
CI mode: ACTUAL_CI_REPOSITORY_HEALTH

Admission evidence:
- Branch/worktree: main / clean before PLAN.
- HEAD == main == origin/main == remote refs/heads/main:
  8cdf2d14c3b60aa866e36be2651794ef61fc5bf6.
- Git operation in progress: NONE; other active direct-main task: NONE.
- BE-FND-008 owner/priority/status: CBL / P0 / TODO.
- Dependencies DB-FND-002 and BE-FND-005: DONE.
- CI-FND-001 is DONE/effective; PRE_CI permanently expired.
- Live Required CI run 34921532954 for the exact origin/main SHA:
  completed / success; repository health = HEALTHY.
- Required reviewers: DBR, SR, AR, QAR.

Canonical scope:
- Implement application-layer IdempotencyService backed by PostgreSQL.
- Build SHA-256 over canonical method, route template, path, query and body
  after excluding body eventId; compare user ownership separately.
- Claim event_id with INSERT ... ON CONFLICT DO NOTHING.
- Commit claim, business mutation and stored response status/snapshot in one
  transaction; replay the same logical request and reject reuse with
  IDEMPOTENCY_KEY_REUSE.
- Reuse IdempotencyProperties retention = 30 days and expose the cleanup
  boundary without scheduling a job.
- Add focused unit and real PostgreSQL/Testcontainers integration coverage for
  canonical hashing, replay, reuse, concurrency, rollback and retention.

Explicitly out of scope:
- Production API endpoints and business mutations that consume the service.
- Idempotency HTTP headers, client changes, authentication/authorization,
  scheduler/background-job wiring, database migrations and API/OpenAPI edits.
- Commit, push, merge, PR or baseline-tag mutation.

PLAN transition: BE-FND-008 TODO → IN_PROGRESS.
```

## BE-FND-008 — IMPLEMENT / TEST — 2026-09-15

```text
Command boundary: execute (PLAN → IMPLEMENT → TEST)
Workflow mode: GOV009_DIRECT_MAIN
Task state after TEST: IN_PROGRESS

Implementation:
- Added common.idempotency application capability with IdempotencyService,
  RequestHashService, JDBC repository, immutable request/result/record types
  and package boundary documentation.
- Canonical request hash covers method, routeTemplate, path, sorted query/body
  object keys and normalized UUID text; top-level body eventId is excluded.
- PostgreSQL claim uses INSERT ... ON CONFLICT (event_id) DO NOTHING; no
  unique-violation exception is used as normal control flow.
- Claim, supplied business mutation and response status/JSONB snapshot execute
  under one @Transactional boundary. Same user/logical request replays the
  snapshot; any user/endpoint/hash mismatch throws IDEMPOTENCY_KEY_REUSE.
- Added deleteExpired(referenceTime), deriving its strict cutoff from the
  existing IdempotencyProperties retention=30d. No scheduler was added.
- Kept the datasource-disabled application smoke test isolated by supplying a
  Mockito JdbcTemplate test bean; production datasource behavior is unchanged.

Regression coverage:
- RequestHashServiceTests: canonical key ordering, eventId exclusion, numeric
  normalization, UUID normalization and changes to method/route/path/query/body.
- IdempotencyServiceIntegrationTests on real PostgreSQL: first result storage,
  exact status/body replay, different body/route/user rejection, two concurrent
  identical claims with exactly one mutation/no 500, business rollback removing
  mutation and claim, retry after rollback, and strict 30-day retention cutoff.

Validation chronology:
- Initial focused run: 6/7 PASS, 1 test-setup error because PostgreSQL JDBC
  required an explicit Timestamp for a test fixture Instant; fixture corrected.
- Final focused idempotency coverage: PASS, 8/8 (focused command 9/9 including
  the application smoke test).
- Initial clean verify: 71/72 PASS, 1 context setup error because the existing
  smoke test disables DataSource while the new repository requires JdbcTemplate;
  smoke test received a Mockito JdbcTemplate and remained DB-isolated.
- Final Maven clean verify: PASS, 72/72; failures 0; errors 0; skipped 0.
- CI Unit selector (!*IntegrationTests,!OpenApiContractHarnessTests):
  PASS, 38/38.
- CI PostgreSQL selector (*IntegrationTests): PASS, 23/23.
- CI OpenAPI selector (OpenApiContractHarnessTests): PASS, 11/11.
- Fresh PostgreSQL 16.15/Testcontainers + Flyway V1 → V2 → V3 + Hibernate
  schema validation: PASS.
- Explicit package/repackage: PASS.
- python tools/ci_workflow_audit.py: PASS.
- python -m unittest -v tools.test_ci_workflow_audit: PASS, 11/11.
- python tools/baseline_audit.py: PASS.
- py_compile for applicable audit files: PASS.
- git diff --check: PASS.
- Scope audit: PASS; 12 changed files are limited to BE-FND-008 production,
  tests and lifecycle evidence.
- Conflict-marker audit: PASS; findings 0.
- Secret/private-key audit: PASS; findings 0.
- Generated-file audit: PASS; findings 0.
- Untracked whitespace audit: PASS; findings 0. Existing trailing spaces on
  unchanged MASTER_BACKLOG heading lines are outside this diff; added diff is clean.
- Baseline-tag integrity: PASS; both local tag refs and peeled targets match
  origin exactly.
- Active PostgreSQL/Testcontainers/Ryuk containers after validation: NONE.

Impact:
- API/OpenAPI: NONE; no endpoint/header contract added or changed.
- Database/Flyway: NONE; uses the canonical existing idempotency_keys table.
- Clients: NONE.
- Security: ownership remains an explicit independent reuse comparison; no
  secret or response logging was introduced.
- Backward compatibility: additive internal service capability.

Review/publication stop state:
- BE-FND-008 remains IN_PROGRESS.
- Required independent reviews DBR, SR, AR and QAR have not been invoked by
  this execute command; no reviewer PASS or finding state is claimed.
- Actual remote CI for the uncommitted BE-FND-008 worktree: NOT RUN / NOT
  CLAIMED. Admission used healthy CI for the unchanged origin/main SHA only.
- No commit, push, merge, PR or baseline-tag mutation performed.
- Next lifecycle step: independent DBR, SR, AR and QAR reviews.
```

## DB-FND-004 — PLAN / admission — 2026-09-15

```text
Command boundary: execute (PLAN → IMPLEMENT → TEST)
Workflow mode: GOV009_DIRECT_MAIN
CI mode: ACTUAL_CI_REPOSITORY_HEALTH

Admission evidence:
- Branch: main.
- Worktree before PLAN: clean.
- HEAD == main == origin/main == remote refs/heads/main:
  da1012a262fbb7216e4e4b67a6a74e40d82a60e5.
- Git operation in progress: NONE.
- Other active direct-main task: NONE.
- DB-FND-004 owner/priority/status: CBL / P0 / TODO.
- Dependency DB-FND-002: DONE.
- DB-FND-003 publication is present on origin/main.
- Required reviewers: Database Reviewer, QA Reviewer.
- CI-FND-001: DONE and effective; PRE_CI permanently expired.
- Live Required CI run 34760502799 for exact origin/main SHA da1012a...:
  completed / success.
- Repository health: HEALTHY.

Canonical sources and decisions:
- Database Schema v1.6 defines six CEFR codes A1..C2, seven onboarding goals
  and the reference-data table shapes.
- SRS v1.2 defines the five canonical badges and the PERFECT_QUIZ boundary.
- MASTER_BACKLOG maps the badge evaluator to the exact condition identifiers
  FIRST_LESSON, STREAK_7, WORDS_100, WORDS_500 and PERFECT_QUIZ.
- PROJECT_RULES.md, IMPLEMENTATION_PLAN.md and MASTER_BACKLOG.md govern the
  direct-main lifecycle, acceptance, scope and reviewer gates.

Planned scope:
- Add the next append-only Flyway migration with stable UUIDs and idempotent
  upserts for 6 CEFR levels, 7 canonical goals and 5 canonical badges.
- Add PostgreSQL/Testcontainers tests for exact reference values, ordering,
  badge thresholds, Flyway application and safe repeated seed execution.

Explicitly out of scope:
- Initial topics/vocabulary, goal-topic mappings, user data and content seeds.
- Badge evaluation/award behavior, APIs, OpenAPI, clients and JPA mappings.
- No dependency addition, unrelated migration, commit, push or tag mutation.

PLAN transition: DB-FND-004 TODO → IN_PROGRESS.
```

## DB-FND-004 — IMPLEMENT / TEST — 2026-09-15

```text
Command boundary: execute (PLAN → IMPLEMENT → TEST)
Workflow mode: GOV009_DIRECT_MAIN
Task state after TEST: IN_PROGRESS

Implementation:
- Added append-only Flyway migration V3__seed_reference_data.sql.
- Seeded 6 CEFR levels A1, A2, B1, B2, C1, C2 with stable UUIDs and
  canonical sort order 1..6.
- Seeded 7 active goals: GENERAL_ENGLISH, TRAVEL, BUSINESS, TOEIC, IELTS,
  COMMUNICATION and ACADEMIC.
- Seeded 5 active canonical badges with rule identifiers and thresholds:
  FIRST_LESSON=1, STREAK_7=7, WORDS_100=100, WORDS_500=500,
  PERFECT_QUIZ=5.
- Used natural-key ON CONFLICT upserts so repeated execution is safe and does
  not duplicate reference rows.
- Added ReferenceDataIntegrationTests for migration state, exact canonical
  rows, ordering, badge rules and two repeated seed executions.
- Updated DatabaseIndexIntegrationTests to reuse the seeded A1 reference row;
  production schema, indexes and behavior are unchanged.

Test evidence:
- Focused ReferenceDataIntegrationTests + DatabaseIndexIntegrationTests:
  PASS, 15/15.
- Fresh PostgreSQL 16.15 / Testcontainers migration V1 → V2 → V3:
  PASS; schema reached v3 with all 3 migrations applied successfully.
- Maven clean verify: PASS, 64/64; package/repackage PASS.
- CI Unit selector (!*IntegrationTests,!OpenApiContractHarnessTests):
  PASS, 35/35.
- CI PostgreSQL selector (*IntegrationTests): PASS, 18/18.
- CI OpenAPI selector (OpenApiContractHarnessTests): PASS, 11/11.
- Explicit Maven package with skipped tests: PASS.
- python tools/ci_workflow_audit.py: PASS.
- python -m unittest -v tools.test_ci_workflow_audit: PASS, 11/11.
- python tools/baseline_audit.py: PASS.
- py_compile for baseline/CI audit Python files: PASS.
- git diff --check: PASS.
- Conflict-marker scan: PASS; none found.
- Scope audit: PASS; only DB-FND-004 migration/test and lifecycle evidence
  files changed.
- Tracked and untracked whitespace audit: PASS.
- Secret/private-key audit: PASS; none found.
- Generated-file audit: PASS; none present in Git status.
- Baseline-tag integrity: PASS; both local tags and peeled targets match
  origin exactly.

Impact:
- API/OpenAPI: NONE.
- Database: append-only V3 reference-data rows only; no schema change.
- Clients: NONE.
- Backward compatibility: compatible; deterministic natural-key upserts are
  repeatable and preserve existing migration history.

Review/publication stop state:
- Database Reviewer and QA Reviewer have not been invoked by this execute
  command; no independent reviewer result is claimed.
- Actual remote CI for the uncommitted DB-FND-004 worktree: NOT RUN and not
  claimed. Admission used repository health for the unchanged origin/main SHA.
- Unresolved findings: no task review has started; no finding state changed.
- No commit, push, merge, PR or baseline-tag mutation performed.
- Next lifecycle step: independent Database Reviewer, then QA Reviewer.
```

## DB-FND-004 — independent reviewer evidence synchronization — 2026-09-15

```text
Command boundary: synchronize independent reviewer evidence
Workflow mode: GOV009_DIRECT_MAIN
Evidence source: completed read-only reports from the independent Database
Reviewer and QA Reviewer Codex tasks. This operation records supplied reviewer
outcomes only; it is not self-review and does not finalize the task.

Independent reviewer chronology:
1. Database Reviewer initial review: PASS.
   - Database findings: NONE.
   - Recommendation: APPROVE.
2. QA Reviewer initial review: PASS.
   - QA findings: NONE.
   - Recommendation: APPROVE.

Synchronized reviewer state:
- DBR: PASS.
- QAR: PASS.
- Historical reviewer FAIL/finding chronology for DB-FND-004: NONE.
- Unresolved findings: NONE.

Database Reviewer evidence highlights:
- V3 is append-only; published V1 and V2 remain unchanged.
- Fresh PostgreSQL 16.15 migration applies V1 → V2 → V3 successfully.
- Exact 6 CEFR levels, 7 active goals and 5 canonical badge rules are present.
- Natural-key upserts are repeatable, preserve an existing PK on conflict and
  do not create duplicate reference rows.
- Focused tests 15/15, Maven clean verify 64/64 and PostgreSQL selector 18/18
  PASS; Hibernate schema validation PASS.

QA Reviewer evidence highlights:
- Acceptance for CEFR order, goals, badge identifiers/thresholds and repeated
  seed execution: PASS; unverified acceptance: NONE.
- Focused PostgreSQL tests: 15/15 PASS.
- Maven clean verify: 64/64 PASS; package/repackage PASS.
- Fresh Flyway V1 → V2 → V3 and Hibernate validation: PASS.
- baseline_audit, ci_workflow_audit, CI audit tests 11/11, py_compile,
  git diff --check, conflict/secret/tag guards: PASS.
- No API/OpenAPI/client/JPA/later-task scope change or regression found.

Required unchanged stop state:
- DB-FND-004: IN_PROGRESS.
- Branch: main; uncommitted direct-main worktree.
- CI mode: ACTUAL_CI_REPOSITORY_HEALTH.
- Admission Required CI run 34760502799 for unchanged origin/main SHA
  da1012a262fbb7216e4e4b67a6a74e40d82a60e5: completed/success.
- Actual remote CI for the uncommitted DB-FND-004 diff: NOT RUN / NOT CLAIMED.
- No commit, push, merge, PR or baseline-tag mutation performed.
- Task is eligible for a separate finalize command; this evidence-sync command
  does not change lifecycle status.
```

## DB-FND-004 — FINALIZE — 2026-09-15

```text
Command boundary: finalize
Workflow mode: GOV009_DIRECT_MAIN
CI mode: ACTUAL_CI_REPOSITORY_HEALTH

Finalization gates:
- Task/owner/priority before transition: DB-FND-004 / CBL / P0 / IN_PROGRESS.
- Dependency DB-FND-002: DONE.
- Required reviewers: DBR, QAR.
- Independent Database Reviewer: PASS; Database findings NONE.
- Independent QA Reviewer: PASS; QA findings NONE.
- Historical reviewer FAIL/finding chronology for DB-FND-004: NONE.
- Unresolved findings: NONE.
- Acceptance: PASS — 6 ordered CEFR levels, 7 active canonical goals and 5
  canonical badge rules are seeded by repeatable natural-key upserts.

Fresh final validation on the post-review worktree:
- Maven clean verify: PASS — 64/64; failures 0; errors 0; skipped 0.
- PostgreSQL/Testcontainers 16.15: PASS.
- Flyway fresh migration: successfully validated and applied V1 → V2 → V3.
- ReferenceDataIntegrationTests: PASS — 5/5 within the full run.
- DatabaseIndexIntegrationTests: PASS — 10/10 within the full run.
- OpenApiContractHarnessTests: PASS — 11/11.
- Package/repackaged executable JAR: PASS.
- python tools/ci_workflow_audit.py: PASS.
- python -m unittest -v tools.test_ci_workflow_audit: PASS — 11/11.
- python tools/baseline_audit.py: PASS.
- python -m py_compile applicable audit files: PASS.
- git diff --check: PASS.
- Conflict-marker, untracked-whitespace, secret/private-key, generated-file,
  scope and baseline-tag integrity audits: PASS; findings 0.
- Active PostgreSQL/Testcontainers/Ryuk containers after validation: NONE.

Lifecycle and milestone transition:
- DB-FND-004: IN_PROGRESS → DONE.
- M1 execution progress: 15/29 (51.7%); milestone remains IN_PROGRESS.

Contract/scope impact:
- Database: additive append-only V3 reference-data migration; published V1
  and V2 unchanged; no schema shape change.
- API/OpenAPI/clients/security/business behavior: NONE.
- Backward compatibility: compatible; natural-key upserts preserve existing
  primary keys and do not duplicate reference rows.
- Unrelated/later-task/V2 scope: NONE.

Git/publication stop state:
- Branch: main; HEAD and origin/main remain
  da1012a262fbb7216e4e4b67a6a74e40d82a60e5.
- Admission Required CI run 34760502799 for that exact origin/main SHA remains
  completed/success; repository health before publication is HEALTHY.
- Actual remote CI for this uncommitted task diff: NOT RUN / NOT CLAIMED;
  direct-main remote CI is the post-push repository-health gate.
- Commit/push/merge/tag mutation: NONE.
- Next step: create the one final task-scoped commit, then fast-forward push
  main through the separate Git workflow.
```

## BE-FND-008 — QA finding / remediation — 2026-09-15

```text
Command boundary: remediate QA-BE-FND-008-001
Workflow mode: GOV009_DIRECT_MAIN
Task state: BE-FND-008 remains IN_PROGRESS

Historical independent QA Reviewer evidence (preserved):
- QAR result: FAIL.
- Finding: QA-BE-FND-008-001.
- Severity: MEDIUM.
- Blocking: YES.
- Original status: OPEN.
- Defect: concurrent duplicate test could pass through initial lookup/replay
  without deterministically exercising PostgreSQL ON CONFLICT inserted=false.

Focused remediation:
- Moved IdempotencyServiceIntegrationTests into the common.idempotency test
  package so a test-only MockitoSpyBean can observe the package-private real
  repository without widening production visibility.
- Replaced the timing-sensitive start latch with an explicit winner/loser
  transaction sequence. The winner performs the real claim and is held after
  INSERT while its transaction remains uncommitted.
- The loser starts only after WINNER_CLAIMED. Reaching the repository spy proves
  its initial lookup completed without seeing the uncommitted row.
- Before releasing the winner, the test queries PostgreSQL pg_stat_activity and
  requires the loser's real INSERT into idempotency_keys to be active with
  wait_event_type='Lock'. This proves the loser reached and blocked on the real
  ON CONFLICT path; no PostgreSQL behavior is mocked.
- After winner commit, the spy records real claim results winner=true and
  loser=false. The loser replays status=200 and body={accepted,1}; both futures
  complete normally, exactly one mutation is committed, and no 500 occurs.
- The deterministic concurrency case uses @RepeatedTest(10); no Thread.sleep
  timing assumption or production synchronization hook was added.

Validation:
- Focused deterministic concurrency repetitions: PASS, 10/10.
- Complete IdempotencyServiceIntegrationTests: PASS, 14/14.
- RequestHashServiceTests: PASS, 3/3.
- Maven clean verify: PASS, 81/81; failures 0; errors 0; skipped 0.
- PostgreSQL integration selector (*IntegrationTests): PASS, 32/32.
- OpenAPI contract selector: PASS, 11/11.
- Package/repackage: PASS.
- Fresh PostgreSQL 16.15/Testcontainers, Flyway V1 → V2 → V3 and Hibernate
  schema validation: PASS.
- python tools/ci_workflow_audit.py: PASS.
- python -m unittest -v tools.test_ci_workflow_audit: PASS, 11/11.
- python tools/baseline_audit.py: PASS.
- py_compile for applicable audit files: PASS.

Scope/contract:
- Production implementation, database/Flyway, API/OpenAPI, clients and baseline
  tags: UNCHANGED by this remediation.
- Remediation code change is test-only plus this historical evidence.
- Existing hash/replay/reuse/rollback/retention assertions remain enabled and
  PASS.

Required stop state:
- QA-BE-FND-008-001 remains OPEN; this implementation task does not mark it
  RESOLVED.
- QAR remains historical FAIL pending independent focused QA re-review.
- No reviewer PASS is claimed.
- Actual remote CI for the uncommitted worktree: NOT RUN / NOT CLAIMED.
- No commit, push, merge, PR or baseline-tag mutation performed.
```

## BE-FND-008 — independent reviewer evidence synchronization — 2026-09-15

```text
Command boundary: synchronize independent reviewer evidence
Workflow mode: GOV009_DIRECT_MAIN
Evidence source: completed independent Database, Security, Architecture and
focused QA re-review reports supplied by the project owner. This operation
records supplied reviewer outcomes only; it is not self-review and does not
finalize the task.

Historical chronology preserved:
1. Independent Database review: PASS; Database findings NONE.
2. Independent Security re-review: PASS; Security findings NONE.
3. Independent Architecture re-review: PASS; Architecture findings NONE.
   - The Architecture Reviewer did not resolve the QA finding.
4. Initial independent QA review: FAIL.
   - QA-BE-FND-008-001 OPEN.
   - Severity MEDIUM; Blocking YES; Original Status OPEN.
5. Focused deterministic concurrency remediation completed while the finding
   remained OPEN.
6. Independent focused QA re-review: PASS.
   - QA-BE-FND-008-001 RESOLVED by the QA Reviewer.
   - Required action remaining: NONE; new QA findings: NONE.

Synchronized independent reviewer state:
- DBR: PASS.
- SR: PASS.
- AR: PASS.
- QAR: PASS.
- QA-BE-FND-008-001: RESOLVED by independent focused QA re-review.
- Unresolved findings: NONE.

Database Reviewer evidence highlights:
- Canonical PostgreSQL `idempotency_keys` schema, mappings and indexes remain
  unchanged; no migration change was introduced.
- Claim uses `INSERT ... ON CONFLICT (event_id) DO NOTHING` without exception
  control flow.
- Claim, business mutation and response snapshot remain atomic; business
  failure rolls back claim and mutation.
- The deterministic test proves the loser waits on the real PostgreSQL lock,
  receives `inserted=false` and replays the winner response.
- Strict `created_at < cutoff` cleanup semantics remain preserved.

Security Reviewer evidence highlights:
- Replay independently matches `userId`, endpoint and `request_hash`; cross-user
  reuse returns `IDEMPOTENCY_KEY_REUSE` without exposing the stored response.
- Canonical request hashing covers method, routeTemplate, path, query and body
  while excluding top-level `eventId`.
- No tokens, credentials, PII or response snapshots were added to logs; secret
  and private-key scan reported zero findings.
- The duplicate race commits one business mutation and returns no 500 or
  response leak.

Architecture Reviewer evidence highlights:
- QA remediation is test-only; production idempotency and transaction
  architecture remain unchanged.
- `IdempotencyRepository` remains package-private and no production test hook
  or widened visibility was introduced.
- Winner/loser sequencing proves the real lookup → insert/lock → commit →
  `inserted=false` → replay flow without a dependency or layer violation.
- No API/OpenAPI/database/Flyway/client or later-task scope change occurred.

Focused QA re-review resolution evidence:
- Winner inserts the real claim and keeps its transaction uncommitted.
- Loser completes the initial invisible-row lookup, reaches the real
  `INSERT INTO idempotency_keys` and is observed waiting on a PostgreSQL lock.
- Winner commit releases the loser; verified claim results are winner=true and
  loser=false.
- Both requests return the same replay response; exactly one business mutation
  and one idempotency record are committed; no application/HTTP 500 occurs.
- Deterministic concurrency repetitions: PASS — 10/10.

Supplied reviewer validation evidence:
- IdempotencyServiceIntegrationTests: PASS — 14/14.
- Focused idempotency/smoke/hash coverage: PASS — 18/18.
- Maven clean verify: PASS — 81/81; failures 0; errors 0; skipped 0.
- PostgreSQL integration selector: PASS — 32/32.
- PostgreSQL 16.15 and Flyway V1 → V2 → V3: PASS.
- Package/repackage and OpenAPI contract selector: PASS.
- ci_workflow_audit and its regression tests: PASS — 11/11.
- baseline_audit, py_compile and git diff checks: PASS.

Required unchanged stop state:
- BE-FND-008: IN_PROGRESS.
- Branch: main; uncommitted direct-main worktree.
- Actual remote CI for this uncommitted diff: NOT RUN / NOT CLAIMED.
- Finalization: NOT PERFORMED.
- Commit/push/merge/PR/baseline-tag mutation: NONE.
```

## BE-FND-008 — FINALIZE — 2026-09-15

```text
Command boundary: finalize
Workflow mode: GOV009_DIRECT_MAIN
CI mode: ACTUAL_CI_REPOSITORY_HEALTH

Finalization gates:
- Task/owner/priority before transition: BE-FND-008 / CBL / P0 /
  IN_PROGRESS.
- Dependencies DB-FND-002 and BE-FND-005: DONE.
- Required reviewers: DBR, SR, AR, QAR.
- Independent Database Reviewer: PASS; Database findings NONE.
- Independent Security Reviewer: PASS; Security findings NONE.
- Independent Architecture Reviewer: PASS; Architecture findings NONE.
- Independent focused QA re-review: PASS; QA findings NONE.
- QA-BE-FND-008-001: RESOLVED by the independent focused QA Reviewer.
- Historical QAR FAIL, Severity MEDIUM, Blocking YES and Original Status OPEN
  chronology: PRESERVED.
- Unresolved findings: NONE.
- Acceptance: PASS — body eventId-only contract, SHA-256 canonical request
  hash, PostgreSQL ON CONFLICT claim, replay/reuse semantics, atomic mutation
  and response persistence, strict 30-day retention and deterministic
  concurrent duplicate behavior.

Fresh final validation on the post-review worktree:
- Maven clean verify: PASS — 81/81; failures 0; errors 0; skipped 0.
- Unit selector (!*IntegrationTests,!OpenApiContractHarnessTests): PASS —
  38/38.
- PostgreSQL integration selector (*IntegrationTests): PASS — 32/32.
- OpenAPI contract selector: PASS — 11/11.
- Package/repackage: PASS.
- PostgreSQL 16.15/Testcontainers, Flyway V1 → V2 → V3 and Hibernate
  validation: PASS.
- python tools/ci_workflow_audit.py: PASS.
- python -m unittest -v tools.test_ci_workflow_audit: PASS — 11/11.
- python tools/baseline_audit.py: PASS.
- python -m py_compile applicable audit files: PASS.
- git diff --check: PASS.
- Conflict-marker, untracked-whitespace, secret/private-key, generated-file,
  scope and baseline-tag integrity audits: PASS; findings 0.
- Active PostgreSQL/Testcontainers/Ryuk containers after validation: NONE.

Lifecycle and milestone transition:
- BE-FND-008: IN_PROGRESS → DONE.
- M1 execution progress: 16/29 (55.2%); milestone remains IN_PROGRESS.

Contract/scope impact:
- API/OpenAPI: NONE.
- Database/Flyway: NONE; canonical existing `idempotency_keys` schema reused.
- Clients: NONE.
- Security: ownership, endpoint and request-hash binding preserved; no
  sensitive logging introduced.
- Backward compatibility: additive internal service capability; replay,
  reuse-conflict, rollback and retention behavior remain canonical.
- Unrelated/later-task/V2 scope: NONE.

Git/publication stop state:
- Branch: main; HEAD and origin/main remain
  8cdf2d14c3b60aa866e36be2651794ef61fc5bf6.
- Actual remote CI for this uncommitted task diff: NOT RUN / NOT CLAIMED;
  repository-health admission evidence applies only to unchanged origin/main.
- Commit/push/merge/PR/baseline-tag mutation: NONE.
- Next step: create the one final task-scoped commit, then fast-forward push
  main through the separate Git workflow.
```
