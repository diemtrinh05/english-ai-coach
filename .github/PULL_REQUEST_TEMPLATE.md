# Pull Request

## Task

- Backlog ID:
- Milestone:
- Owner:
- Task status:
- Dependency status:

## CI Gate

CI status (select exactly one):

- [ ] `PASS`
- [ ] `PRE_CI_BOOTSTRAP_NA`
- [ ] `NOT_APPLICABLE`

If `PRE_CI_BOOTSTRAP_NA` is selected:

- Eligible Task ID:
- Reason:
- Local validation evidence:
- [ ] Confirmed that no existing CI check is failing.

`PRE_CI_BOOTSTRAP_NA` is valid only for the explicit eligible task list and conditions in the active governance documents, and only before `CI-FND-001` is `DONE` and merged into `main`. A failing CI check can never be waived. `CI-FND-001` cannot use `PRE_CI_BOOTSTRAP_NA` for its final gate.

If `NOT_APPLICABLE` is selected, explain why the CI gate genuinely does not apply. `NOT_APPLICABLE` must not be used as a substitute for `PRE_CI_BOOTSTRAP_NA`.

Details:

## Summary

Describe what this PR changes and why.

## Scope

- [ ] Changes are limited to the referenced backlog task.
- [ ] No unapproved V1/V2 scope expansion.
- [ ] No business rule was invented outside the approved baseline.

## Change Impact

### Documentation

- [ ] No documentation impact
- [ ] Documentation updated

Details:

### API / OpenAPI

- [ ] No API contract impact
- [ ] API Specification updated
- [ ] OpenAPI updated
- [ ] Breaking change

Details:

### Database / Flyway

- [ ] No database impact (mutually exclusive with every database-impact option below)
- [ ] Schema change
- [ ] New Flyway migration
- [ ] Data migration
- [ ] Other database-impacting change

If any database-impact option is selected, Database Reviewer approval is required and cannot be replaced by an explanation.

Corresponding Flyway migration (required for every production schema change):

Schema-impacting PR without a Flyway migration — explicit exception reason (Database Reviewer approval required):

Ordinary production schema changes must not be applied manually.

Database validation evidence (required for every database-impacting PR):

- [ ] Migration validation, where applicable
- [ ] Testcontainers / integration validation, where applicable
- [ ] Schema validation, where applicable
- [ ] Rollback / forward-migration evidence, where applicable
- [ ] Relevant database tests, where applicable

Documentation-only PRs must provide evidence relevant to their actual scope and do not need irrelevant test types.

Details:

### Backend

- [ ] No backend impact
- [ ] Backend changed

### Android

- [ ] No Android impact
- [ ] Android changed

### Admin Web

- [ ] No Admin Web impact
- [ ] Admin Web changed

### Security

- [ ] No security-sensitive change (mutually exclusive with every security-sensitive impact option below)
- [ ] Authentication / authorization
- [ ] Secrets / credentials
- [ ] Idempotency / concurrency
- [ ] AI / external provider
- [ ] Push token / personal data

If any security-sensitive impact option is selected, Security Reviewer approval is required and cannot be replaced by the generic omitted-reviewer explanation.

## Tests

- [ ] Unit tests
- [ ] Integration tests
- [ ] Contract tests
- [ ] UI tests where applicable
- [ ] `python tools/baseline_audit.py`
- [ ] `git diff --check`

Evidence:

```text
Paste relevant validation/test results here.
```

## Required Reviewers

- [ ] Architecture Reviewer
- [ ] Database Reviewer — mandatory when any Database / Flyway impact is declared.
- [ ] Security Reviewer — mandatory when any security-sensitive impact is declared.
- [ ] QA Reviewer

Database Reviewer is mandatory when any Database / Flyway impact is declared.

Security Reviewer is mandatory when any security-sensitive impact is declared.

Explain omitted reviewers where applicable. This generic omitted-reviewer rule does not permit Database Reviewer omission when any Database / Flyway impact is declared, or Security Reviewer omission when any security-sensitive impact is declared.

## Backward Compatibility

Describe compatibility impact, or write `None`.

## Risks / Known Limitations

List unresolved risks or blockers.

## Definition of Done

- [ ] Acceptance criteria satisfied
- [ ] Required tests pass
- [ ] Baseline audit passes
- [ ] No secrets committed
- [ ] No unrelated changes
- [ ] Required reviewer findings resolved
- [ ] CI gate state and evidence recorded
