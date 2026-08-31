# QA Reviewer Instructions

**Role:** QA / Test Design / Regression Reviewer  
**Mode:** Test-first. Do not change product behavior just to make tests pass.

## Language Policy

All QA plans, test reports, findings and explanations must be **Vietnamese**.

Explanatory comments in test code must be **Vietnamese**.

Technical identifiers and contract values remain unchanged.

User-facing UI strings are expected to be Vietnamese under the project language policy.

## Read first

```text
docs/PROJECT_RULES.md
docs/requirements/
docs/api/
docs/architecture/
docs/database/
docs/ai/
docs/technical/
```

Also inspect relevant client design/technical docs.

## Learning tests

Test:

```text
answerQuality = 0,1,2,3,4,5
```

Verify:

```text
isCorrect
SRS state
nextReviewAt
progress
```

## Idempotency

Mandatory:

```text
first request
→ success

same request + same eventId
→ replay

same eventId + different payload
→ 409 IDEMPOTENCY_KEY_REUSE

same eventId + different endpoint/user
→ 409

concurrent same eventId
→ one logical execution
```

## Concurrency

```text
same version
→ one succeeds
→ one 409 CONCURRENT_UPDATE
```

## Transactions

Learning attempt must not partially commit critical effects after failure.

## AI

Test:

```text
valid output
malformed JSON
missing fields
unsafe content
provider timeout
provider 5xx
budget exceeded
duplicate generation
```

## AI review

Reject:

```text
reason required
```

Assert:

```text
status = REJECTED
reviewNote = reason
audit.details.reason = reason
```

## UI states

Test:

```text
Initial
Loading
Success
Empty
Error
Offline where applicable
Submitting
```

## Regression triggers

After:

```text
DB change
API change
SRS change
auth change
error-code change
```

run dependent suites.

## Bug format

```text
BUG-XXX
Severity:
Environment:
Preconditions:
Steps:
Expected:
Actual:
Evidence:
Affected component:
Regression:
Status:
```

---

## Reconciled baseline checks
Mandatory coverage includes answerQuality boundaries, SRS transitions/regressions, CONCURRENT_UPDATE, idempotency replay/reuse/concurrent duplicate, assessment block/stopping rules, weakness/risk boundaries, workload guards, Daily Plan snapshot, XP/streak/badge idempotency, FCM preference/dedupe, synchronous AI/no jobId, and reject review_note audit persistence.
