# IMPLEMENTATION_PLAN.md — English AI Coach V1

**Document type:** Execution / Implementation Planning  
**Planning state:** PROPOSED  
**Execution readiness:** READY — non-contract planning document  
**Generated from approved baseline:** 2026-08-31  
**Scope:** V1 MVP only  
**Master backlog:** `MASTER_BACKLOG.md`  

> Tài liệu này **không override** SRS/DB/Architecture/API/OpenAPI/Technical Specs. Khi có xung đột, approved baseline luôn thắng. Mọi thay đổi contract phải đi qua Change Impact + cập nhật toàn bộ downstream docs/OpenAPI/client trong cùng change.

## 1. Mục tiêu

Chuyển English AI Coach từ trạng thái **documentation baseline clean** sang **implementation có thể giao trực tiếp cho AI Agents**, với:

- thứ tự triển khai theo dependency thay vì theo cảm tính;
- first vertical slice để kiểm chứng kiến trúc sớm;
- task ID duy nhất, owner/reviewer/acceptance/test rõ ràng;
- 100% API/OpenAPI operations và database tables có task owner;
- không cho client duplicate backend-owned algorithms;
- không mở scope V2.

## 2. Source of truth

1. SRS v1.2
2. Database Schema v1.6
3. System Architecture v1.3
4. AI Personalization v1.3
5. API Specification v1.4
6. OpenAPI v1.4
7. Technical Specification v1.2
8. Backend Technical Specification v1.3
9. Android Java Technical Specification v1.1
10. Flutter Technical Specification v1.1 (contract-only V2)
11. Admin Web Technical Specification v1.1

Ngoài ra mọi Agent phải đọc `docs/PROJECT_RULES.md`, `AGENTS.md` và role-specific agent file trước khi code.

## 3. Non-negotiable V1 execution invariants

```text
answerQuality 0..5; backend derives isCorrect
client must not send isCorrect
eventId in body = sole logical-operation idempotency key
same event + same logical request → replay
same event + different request/user/endpoint → 409 IDEMPOTENCY_KEY_REUSE
PostgreSQL claim → INSERT ... ON CONFLICT DO NOTHING
optimistic lock conflict → 409 CONCURRENT_UPDATE
idempotency retention = 30 days
V1 online-first; no offline learning mutation/sync
Daily Plan = persisted snapshot; no mid-day dynamic reranking
AI client-facing generation = synchronous V1
Reusable AI requires Admin review; personalized result does not
MAX_DAILY_WORKLOAD_INCREASE_PERCENT = 20%
Brute-force: 5 failures → ~5 minute temporary lock
Admin = React + TypeScript + Vite
Android V1 = Java MVVM
Flutter = future V2; V1 only verifies contract compatibility
```

## 4. M0 stop condition

Không mở feature implementation trước khi các task `GOV-001..GOV-006` hoàn tất. Đặc biệt `GOV-003` phải làm cho operational Codex instruction khớp canonical PostgreSQL idempotency (`ON CONFLICT DO NOTHING`). Đây là governance correction, **không thay đổi business contract**.

## 5. Vai trò

| Code | Role | Ownership |
|---|---|---|
| INT | Integration Lead | Điều phối plan/backlog, direct-main hoặc grandfathered branch/PR, baseline/change impact, release integration. |
| CBL | Codex Backend Lead | Spring Boot, PostgreSQL/Flyway/JPA, API, algorithms, providers, backend/ops tests. |
| AFL | Antigravity Frontend Lead | Android Java V1, React Admin, API integration, UI/client tests, Flutter contract-only review. |
| AR | Architecture Reviewer | Module boundaries, architecture, cross-layer contract, scope. |
| DBR | Database Reviewer | Schema/migrations/queries/indexes/transactions/concurrency. |
| SR | Security Reviewer | Auth/authz/secrets/idempotency/provider/device/admin security. |
| QAR | QA Reviewer | Acceptance, regression, contract/E2E/performance/release quality. |

## 6. Trạng thái task

Với task bắt đầu sau effective point của `GOV-009`:

```text
TODO → IN_PROGRESS → DONE
          ↓
       BLOCKED
```

- `IN_PROGRESS`: PLAN gate đã pass; task đang implement, test, review hoặc remediation.
- `BLOCKED`: chỉ dùng khi có blocker thực tế; khi blocker được giải quyết task trở lại `IN_PROGRESS`.
- `DONE`: pre-publish acceptance/tests/reviewer/audit gates đã pass; unresolved findings = `NONE`; legacy CI-before-DONE hoặc applicable direct-main PRE_CI repository evidence đã thỏa; backlog evidence được cập nhật.
- `READY` và `IN_REVIEW` vẫn là status hợp lệ để giữ nguyên lịch sử và vận hành task grandfathered, nhưng không bắt buộc cho task direct-main mới.

## 7. Nguyên tắc direct-main và legacy branch/PR

- Sau effective point của `GOV-009`, task mới làm trực tiếp trên `main`; không cần task branch hoặc PR mặc định.
- Chỉ một direct-main task được active; parked grandfathered task trên branch cũ không tính vào giới hạn này.
- Mặc định một task tạo đúng một final commit sau khi đã `DONE`, rồi push `main`; không gộp task không liên quan.
- Task/PR bắt đầu trước effective point tiếp tục legacy workflow. Mỗi PR legacy phải gắn Task ID và vẫn dùng branch/reviewer/CI/merge gates cũ.
- `BE-FND-003` / PR #5 được grandfather rõ ràng: PR và branch giữ nguyên, status/evidence không đổi, actual CI vẫn bắt buộc, `PRE_CI_BOOTSTRAP_NA` không eligible, và không cherry-pick chỉ vì `GOV-009` tồn tại.
- Mọi API change phải đồng bộ API Spec + OpenAPI + DTO/controller + client model + contract tests trong cùng change.
- Mọi schema change phải Flyway append-only; không sửa migration đã apply/shared.
- P0 cần đúng reviewer gate trong `MASTER_BACKLOG.md` trước khi finalization/commit; legacy PR vẫn cần gate trước merge.

### 7.1. Direct-main admission gate

Trước PLAN / `TODO → IN_PROGRESS`, luôn yêu cầu: current branch `main`; clean worktree; local `main == origin/main`; không Git operation; không post-GOV-009 direct-main task khác active; task `TODO`; dependencies `DONE`; repository không `BLOCKED`; canonical sources/acceptance/scope/out-of-scope/reviewer mapping đã xác định; và CI mode hợp lệ cho chính task. Chỉ nhận diện tên CI mode là chưa đủ.

| State | CI mode | Admission |
|---|---|---|
| Trước CI effective, Task ID explicitly PRE_CI eligible | `PRE_CI_BOOTSTRAP_NA` | ALLOWED khi common + PRE_CI gates thỏa. |
| Trước CI effective, `CI-FND-001` | `ACTUAL_CI_BOOTSTRAP` | ALLOWED theo special flow. |
| Trước CI effective, ordinary task không eligible | `INVALID_BEFORE_CI` | `BLOCKED`; không được dùng `NOT_APPLICABLE` hoặc suy diễn PRE_CI. |
| GOV-009 transition | `OWNER_AUTHORIZED_GOVERNANCE_TRANSITION` | One-time only; không reusable cho later task admission. |
| Sau CI effective, ordinary new task | `ACTUAL_CI_REPOSITORY_HEALTH` | ALLOWED chỉ khi latest `origin/main` health = `HEALTHY`. |

Sau CI effective: `HEALTHY` = latest required remote CI PASS; `CI_PENDING` = required CI exists/is expected nhưng chưa xong; repository health `BLOCKED` = required CI FAIL hoặc explicit published-main failure. `CI_PENDING` và repository health `BLOCKED` đều chặn PLAN. Repository health `BLOCKED` không phải task lifecycle `BLOCKED`.

`PUBLISHED_MAIN_RECOVERY` là emergency operational state machine, không phải executable backlog task và không phải PLAN admission. Khi required remote CI fail trên published direct-main commit: originating task vẫn `DONE`; repository health = `BLOCKED`; recovery = `OPEN`. Đây là admission exception duy nhất trong repository `BLOCKED`, chỉ để recover cùng published incident; unrelated task và `TODO → IN_PROGRESS` vẫn bị cấm.

```text
TASK STATUS                    = DONE
REPOSITORY HEALTH              = BLOCKED
PUBLISHED_MAIN_RECOVERY STATUS = OPEN

OPEN → VALIDATING → CI_PENDING → CLOSED
                   CI FAIL → OPEN
optional recovery state: BLOCKED
```

Incident evidence gồm originating Task ID, failing `origin/main` SHA, failing check/run khi có, failure summary, strategy `FIX_FORWARD | REVERT`, recovery commit SHA, affected TEST/reviewer gates, recovery findings và final CI. Recovery diff phải minimal; chạy failed CI-equivalent check, applicable build/test/integration/runtime, baseline audit, applicable py_compile, diff/secret/generated/scope/tag gates; rerun only affected independent reviewers. Chỉ publish khi affected gates PASS và unresolved recovery findings = `NONE`.

Recovery commit dùng originating Task ID và một trong `fix(<TASK-ID>): recover published main after CI failure` hoặc `revert(<TASK-ID>): revert failing published change`; push `main` fast-forward only. Push đặt repository/recovery = `CI_PENDING`; PASS → `HEALTHY`/`CLOSED`; FAIL → `BLOCKED`/`OPEN`, tiếp tục cùng incident. Originating task không reopen. Revert giữ failing commit trong history và phải ghi planning consequence nếu capability/acceptance bị loại bỏ. Future replacement task chỉ theo normal admission sau `HEALTHY`.

## 8. Milestone execution map

| Milestone | Name | Tasks | Goal | Exit gate |
|---|---|---:|---|---|
| M0 | Execution Governance | 7 | Khóa cách thực thi, backlog, PR/reviewer gate; sửa operational instruction còn lệch canonical. | GOV-001..GOV-007 hoàn tất; baseline audit PASS; tag baseline tồn tại. |
| M1 | Foundation Ready | 29 | Governance/pre-foundation amendments, Backend/DB/CI/Admin/Android shell và cross-cutting infrastructure. | Fresh DB migrate; CI PASS thực tế; idempotency/locking/error/token foundations sẵn; clients build. `PRE_CI_BOOTSTRAP_NA` không thỏa gate này. |
| M2 | Identity & Catalog | 21 | Auth, profile/goals, CEFR, vocabulary/topic foundation và content đủ cho assessment. | Auth lifecycle pass; catalog APIs pass; >=30 usable vocabulary/CEFR. |
| M3 | First Vertical Slice — Learning/SRS | 16 | Kiểm chứng kiến trúc end-to-end bằng Vocabulary→Session→Attempt→SRS→Progress. | QA-E2E-001 PASS, gồm replay/reuse/concurrency. |
| M4 | Assessment & Quiz | 14 | Placement deterministic và quiz engine end-to-end. | Assessment/quiz algorithm + Android flow PASS. |
| M5 | Personalization & Daily Plan | 18 | Weakness, forgetting risk, recommendation, workload, snapshot Daily Plan, progress. | QA-E2E-003 PASS; same-day snapshot stable; Daily Plan XP idempotent. |
| M6 | Gamification Complete | 8 | XP/level/streak/badges/leaderboard hoàn chỉnh. | QA-E2E-004 và gamification suite PASS. |
| M7 | AI & Notifications | 21 | Reusable/personalized AI synchronous + budget + FCM notification. | AI cost/idempotency + notification timezone/dedupe tests PASS. |
| M8 | Client/Admin Completion | 25 | Admin V1 đầy đủ, Android V1 đầy đủ, full learner/admin E2E. | QA-E2E-006 và QA-E2E-007 PASS. |
| M9 | Hardening & Release Candidate | 18 | Security, performance, resilience, ops, reviewer gates và RC. | Architecture/DB/Security/QA final pass; REL-001/REL-002 DONE. |

### 8.1. M1 Governance Amendment / Pre-Foundation

`GOV-008` là task governance/pre-foundation đầu tiên của M1, với owner `INT`, dependency `GOV-007`, priority `P0`, reviewer `AR,QAR` và trạng thái `DONE`. Task này không thuộc M0 và không thay đổi closure lịch sử `GOV-001..GOV-007`.

`GOV-008` phải được merge trước khi PR #2 / `BE-FND-001` được phép dùng `PRE_CI_BOOTSTRAP_NA`. Điều này không thêm `GOV-008` retroactively vào dependency metadata của `BE-FND-001`.

`GOV-009` là one-time governance transition được repository owner ủy quyền triển khai trực tiếp trên `main`, dependency `GOV-008`, priority `P0`, reviewer `AR,QAR`, trạng thái hiện tại `DONE` sau finalization với AR PASS, QAR PASS và unresolved findings = `NONE`. Governance commit và push vẫn `PENDING`; workflow mới chưa effective khi thay đổi còn uncommitted. Workflow chỉ effective khi commit `GOV-009` được push thành công lên `origin/main`; installed skills chỉ được migrate trong phase riêng sau effective point.

Closure mode `OWNER_AUTHORIZED_GOVERNANCE_TRANSITION` chỉ dành cho `GOV-009`. Đây không phải `PRE_CI_BOOTSTRAP_NA` hoặc `CI PASS`, không đổi PRE_CI eligible list, không được reuse, hết hiệu lực vĩnh viễn sau GOV-009 push và không waive failing CI. `IN_PROGRESS → DONE` chỉ hợp lệ khi owner authorization confirmed; AR/QAR PASS; unresolved findings = `NONE`; acceptance/governance consistency, baseline audit, py_compile, diff, scope/secret/generated-file, tag-integrity gates PASS; existing failing CI check = `NONE`.

## 9. Critical path

```text
M0 Governance
  ↓
M1 Foundation / DB / CI
  ↓
M2 Auth + Catalog + Seed Content
  ↓
M3 Learning Attempt + SRS + XP/Streak Core
  ↓
M4 Assessment + Quiz
  ↓
M5 Personalization + Daily Plan + Progress
  ↓
M6 Gamification Complete
  ↓
M7 AI + Notification
  ↓
M8 Android/Admin Complete
  ↓
M9 Hardening + Reviewer Gates + RC
```

Các track có thể chạy song song **chỉ khi dependency trong backlog đã DONE**. Ví dụ Admin/Android shell có thể chạy trong M1, nhưng feature integration không được invent API trước backend contract.

## 10. First Vertical Slice bắt buộc

Slice đầu tiên không phải “CRUD demo”; nó phải kiểm chứng invariant quan trọng nhất:

```text
Vocabulary
  ↓
POST Learning Session (eventId)
  ↓
Flashcard quality 0..5
  ↓
POST Learning Attempt (eventId)
  ↓
backend derive isCorrect
  ↓
SRS + optimistic locking
  ↓
attempt history + user progress
  ↓
XP + streak
  ↓
idempotency response snapshot
  ↓
GET progress / due review
  ↓
Android refresh UI
```

Gate: `QA-E2E-001` phải PASS trước khi mở rộng mạnh sang Personalization/Daily Plan.

## 11. Parallelization strategy

| Track | Có thể bắt đầu | Không được vượt qua trước khi backend sẵn |
|---|---|---|
| Backend/DB | M1 | N/A |
| Android shell | M1 | Feature integration phải chờ API task tương ứng |
| Admin shell | M1 | Feature integration phải chờ admin API task tương ứng |
| Assessment | M4 sau catalog seed | Không chạy nếu content <30/CEFR |
| Personalization | Sau Learning/Quiz history | Không invent local/client formulas |
| AI | Provider/mock có thể prep M1/M7 | Không expose 202/jobId hoặc bypass budget/idempotency |
| Notification | Foundation scheduler M1 | Scheduling business flow chờ profile/timezone, Daily Plan, streak |
| Flutter | Không implement V1 | Chỉ `FLT-CONTRACT-001` ở M9 |

## 12. API coverage gate

Approved OpenAPI có **76 operations / 72 paths**. `MASTER_BACKLOG.md` chứa operation→task coverage matrix. Release candidate yêu cầu:

```text
implemented operations = 76
OpenAPI operations     = 76
missing                = 0
extra                  = 0
duplicate operationId  = 0
broken $ref             = 0
```

## 13. Database coverage gate

DB v1.6 có **34 canonical tables**. Fresh migration phải tạo đủ tất cả; `MASTER_BACKLOG.md` map từng table vào primary implementation task.

Không được:

- tạo bảng ngoài schema mà không có approved change impact;
- bỏ DB CHECK `answer_quality`/`is_correct`;
- bỏ partial uniqueness primary goal / assessment in-progress / notification dedupe;
- dùng H2 để thay PostgreSQL cho integration test behavior.

## 14. Testing model

### 14.1. Per-task

- Unit tests cho pure business rules.
- PostgreSQL Testcontainers cho repository/transaction/constraint.
- Contract tests cho controller/DTO/status/error.
- Client unit/component/UI tests cho mapping/state/retry.

### 14.2. Mandatory critical suites

```text
SRS q=0..5 + status boundaries
Assessment block promotion/demotion/hold/stop
Weakness/risk/recommendation/workload boundaries
Daily Plan snapshot/local-date/complete XP
same event replay / event reuse / concurrent duplicate
optimistic lock CONCURRENT_UPDATE
streak timezone + concurrent activity
AI malformed/unsafe/timeout/budget/duplicate cost
notification timezone/prefs/dedupe/FCM failure
auth refresh rotation/revoke/brute force
USER→ADMIN denial and resource ownership
```

### 14.3. Test clock

Mọi test liên quan local day, next review, streak, notification, retention phải dùng injectable/fixed Clock; không phụ thuộc wall clock của CI.

## 15. CI / validation gate

Mỗi task tối thiểu, khi áp dụng:

```text
baseline_audit
→ compile/build
→ static checks
→ unit tests
→ integration tests
→ OpenAPI validation/contract tests
→ client tests (nếu affected)
→ package
```

Task không được finalization nếu reviewer bắt buộc chưa pass. Legacy/grandfathered PR không được merge nếu reviewer bắt buộc chưa pass. Với direct-main task mới, task-level `DONE` dựa trên pre-publish gates; remote CI sau push là repository-health gate.

### 15.1. PRE_CI_BOOTSTRAP

Trước khi `CI-FND-001` effective theo state machine ở Section 7.1, chỉ đúng các task sau được phép ghi CI status `PRE_CI_BOOTSTRAP_NA` thay cho `CI PASS`:

```text
GOV-008
BE-FND-001
BE-FND-002
DB-FND-001
DB-FND-002
BE-FND-004
BE-FND-005
BE-FND-007
QA-FND-001
QA-FND-002
```

Điều kiện bắt buộc:

```text
all dependencies are DONE
acceptance criteria are satisfied
required local/unit/integration/task tests PASS as applicable
required reviewers PASS
baseline_audit PASS
applicable build/static/git/diff validations PASS
evidence records eligible Task ID + reason + local validation evidence
no existing CI check is failing
failed-check waiver = NOT USED
```

CI check đang fail không bao giờ được waive bằng `PRE_CI_BOOTSTRAP_NA`. `NOT_APPLICABLE` không được dùng thay cho ngoại lệ này.

Với task grandfathered, evidence tiếp tục nằm ở PR theo workflow lịch sử. Với task direct-main mới, PR-level evidence được thay bằng repository-level evidence gồm Task ID, dependency, acceptance, tests, reviewers, unresolved findings, baseline audit, build/static/git checks, xác nhận không có known failing check, `failed-check waiver = NOT USED`, và lý do `CI-FND-001` chưa effective. Danh sách eligible task không thay đổi.

`CI-FND-001` không được dùng `PRE_CI_BOOTSTRAP_NA` hoặc `OWNER_AUTHORIZED_GOVERNANCE_TRANSITION`. Do remote CI chỉ chạy sau khi configuration được push, task này dùng ngoại lệ hai commit: PLAN → IMPLEMENT → TEST → REVIEW → giữ `IN_PROGRESS` → bootstrap commit/push `main` → actual CI chạy. Nếu CI FAIL, task giữ `IN_PROGRESS`, repository health = `BLOCKED`, không task tiếp theo, fix-forward hoặc revert, không waive. Chỉ actual CI PASS mới cho phép `DONE` → closure commit nhỏ → push `origin/main`.

Bootstrap CI fail được remediate trong chính `CI-FND-001` đang `IN_PROGRESS`; additional minimal bootstrap/fix commit được phép tới khi actual CI PASS, không dùng general `PUBLISHED_MAIN_RECOVERY`. Sau CI-FND-001 effective, mọi later published-main CI failure, kể cả closure-commit failure, dùng general recovery; PRE_CI không reactivate.

`CI-FND-001` chỉ effective sau mandatory actual remote CI PASS của bootstrap flow, `IN_PROGRESS → DONE`, và closure commit được push thành công tới `origin/main`. Tại thời điểm đó PRE_CI expired vĩnh viễn. CI của closure/latest main còn chạy đặt repository health = `CI_PENDING`; nếu fail đặt `BLOCKED`; cả hai chặn task mới và không bao giờ reactivate PRE_CI.

Với normal direct-main task sau CI effective: `DONE` → final commit → push `main` → repository health lập tức `CI_PENDING` → actual remote CI. PASS đặt repository/main = `HEALTHY` và không đổi lifecycle. FAIL đặt repository/main = `BLOCKED`, nhưng không viết lại task đã `DONE`; không bắt đầu task mới; phải fix-forward hoặc revert, rerun tests/reviews bị ảnh hưởng và push remediation; không waive hoặc force-push. Gate thoát M1 luôn yêu cầu `CI PASS` thực tế.

`ONE TASK = ONE FINAL COMMIT` là normal rule. Chỉ `CI-FND-001` bootstrap và `PUBLISHED_MAIN_RECOVERY` sau actual published-main CI failure được phép có additional narrowly scoped commit; không có commit-count exception nào khác.

## 16. Security gates

- JWT/refresh token: expiry, revoke, rotation, hashed persistence.
- Brute-force: exact 5/~5min baseline.
- ADMIN authorization enforced backend, không dựa UI.
- Ownership kiểm tra mọi user-owned resource.
- Push token/LLM key/JWT secret/refresh token không log hoặc expose.
- Idempotency `eventId` được bound user + endpoint + canonical request hash.
- AI retry không double-charge.
- CORS/security headers/rate limit cấu hình theo Technical Spec.

## 17. External dependency strategy

Trong local/test phải có deterministic fake/mock cho LLM/TTS/FCM/Object Storage. Integration provider thật chỉ chạy ở environment có secret. Provider outage test phải chứng minh core learning degrade đúng architecture.

## 18. Seed/data readiness

Trước Assessment E2E:

```text
CEFR A1..C2 seeded
>=30 active usable vocabulary / CEFR
goal_topics relevance seeded
canonical badges seeded
demo Admin + User accounts via safe local seed/test fixture only
```

Production seed không được chứa default password hard-coded.

## 19. Definition of Done

### Task DONE

```text
[ ] dependencies DONE
[ ] source docs checked
[ ] code/migration/client change complete
[ ] happy + error + validation paths tested
[ ] auth/authz/ownership tested when applicable
[ ] idempotency/concurrency tested when applicable
[ ] API/OpenAPI/client synchronized when applicable
[ ] no new contract invented
[ ] reviewer gate PASS
[ ] unresolved reviewer findings = NONE
[ ] applicable PRE_CI repository evidence satisfied before CI-FND-001
[ ] baseline_audit/build/static/runtime validation PASS
[ ] git diff --check PASS
[ ] secret/generated-file audit PASS
[ ] baseline tag integrity PASS
[ ] backlog evidence updated
[ ] direct-main safety gates PASS hoặc legacy PR gate PASS
```

Legacy/grandfathered tasks giữ CI-before-`DONE` theo workflow gốc. Với task direct-main bắt đầu sau GOV-009 effective, checklist trên quyết định task-level `DONE`; trước `CI-FND-001`, `PRE_CI_BOOTSTRAP_NA` chỉ áp dụng đúng eligible list/conditions tại Section 15.1. Sau `CI-FND-001` effective, actual remote CI sau push là repository-health gate và không thay đổi lifecycle task đã `DONE`. `CI-FND-001` vẫn dùng bootstrap special case riêng.

### Milestone DONE

- tất cả P0 task của milestone DONE;
- milestone exit gate PASS;
- không có unresolved P0/P1 contradiction;
- `baseline_audit.py` PASS;
- change impact/documentation synced.

## 20. Release Candidate gate

V1 RC chỉ được build sau:

```text
76/76 API operation coverage
34/34 DB table migration coverage
full learner E2E PASS
full admin E2E PASS
idempotency/concurrency suite PASS
security regression PASS
performance baseline PASS or approved documented exception
resilience suite PASS
Architecture Reviewer PASS
Database Reviewer PASS
Security Reviewer PASS
QA Reviewer PASS
baseline_audit PASS
no active V2 scope
```

## 21. Deferred / explicitly out of V1

Không implement trong backlog executable V1:

```text
Flutter application implementation
ML model training/prediction/recommendation
Speaking / Writing / Listening / Reading course
Conversational AI Tutor
offline learning mutation/sync/merge
AI CEFR Suggestion in Admin
client-facing async AI job lifecycle
AI notification timing optimization
```

## 22. Agent execution protocol cho từng task

Áp dụng cho task bắt đầu sau effective point của `GOV-009`:

1. PLAN admission: xác minh toàn bộ common gates tại Section 7.1, repository health cho phép admission, và CI mode hợp lệ cho exact task; chỉ khi đó mới chuyển `TODO → IN_PROGRESS`.
2. IMPLEMENT: chỉ sửa current-task scope, cập nhật tests/docs cần thiết, không commit/push và giữ `IN_PROGRESS`.
3. TEST: chạy gates áp dụng gồm build/unit/integration/runtime, DB/Flyway hoặc API/OpenAPI checks, baseline audit, py_compile, diff/whitespace, secret/generated/scope và baseline-tag audits. Fail thì remediate và test lại.
4. REVIEW: reviewer độc lập, strictly read-only, kiểm tra tracked + untracked diff trên uncommitted `main`; reviewer không được yêu cầu branch/PR. FAIL phải giữ lịch sử, remediation + test lại + focused re-review; task vẫn `IN_PROGRESS`.
5. FINALIZE: khi mọi gate PASS và unresolved findings = `NONE`, đồng bộ evidence/progress và chuyển `IN_PROGRESS → DONE`; chưa commit/push trong bước này.
6. COMMIT/PUSH: mặc định `ONE TASK = ONE FINAL COMMIT` trên `main`, chỉ sau direct-main safety gate; push `origin main`, không force/rebase/reset-hard/clean destructive/history rewrite/tag mutation.

Task grandfathered tiếp tục protocol legacy của nó, gồm `READY`/`IN_REVIEW`, task branch, PR và pre-merge CI nếu đã áp dụng. `CI-FND-001` dùng ngoại lệ hai commit tại Section 15.1.

## 23. First execution queue

Thứ tự khuyến nghị ngay khi bắt đầu:

1. `GOV-001` — Chạy baseline audit trên commit đầu vào
2. `GOV-002` — Đưa IMPLEMENTATION_PLAN và MASTER_BACKLOG vào docs/planning
3. `GOV-003` — Đồng bộ hướng dẫn Codex về PostgreSQL idempotency với canonical spec
4. `GOV-004` — Thiết lập PR template + Change Impact + reviewer gate
5. `GOV-006` — Freeze implementation baseline bằng Git tag
6. `GOV-008` — Clarify pre-CI bootstrap gate
7. `GOV-009` — Simplified Main-Branch Task Workflow
8. `BE-FND-001` — Bootstrap Spring Boot backend project
9. `DB-FND-001` — Dựng PostgreSQL local + Docker Compose
10. `BE-FND-002` — Dựng modular-monolith package/module skeleton
11. `BE-FND-003` — Thiết lập application profiles và typed configuration
12. `DB-FND-002` — Tạo Flyway schema baseline cho 34 bảng
13. `QA-FND-001` — Testcontainers PostgreSQL integration harness
14. `CI-FND-001` — CI pipeline bắt buộc
15. `ADM-FND-001` — Bootstrap React + TypeScript + Vite Admin
16. `AND-FND-001` — Bootstrap Android Java MVVM project

Sau `QA-E2E-001`, dự án đã chứng minh được critical architecture path và có thể tăng parallelism cho M4/M5.

## 24. Planning artifact integrity

- Executable tasks: **177**
- OpenAPI mapped operations: **76/76**
- Database mapped tables: **34/34**
- Task IDs unique: validated
- Dependency references: validated
- Dependency cycles: none
