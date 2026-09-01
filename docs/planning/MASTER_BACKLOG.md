# MASTER_BACKLOG.md — English AI Coach V1

**Planning state:** PROPOSED  
**Execution readiness:** READY  
**Authority:** Planning artifact only; approved baseline remains source of truth  
**Companion:** `IMPLEMENTATION_PLAN.md`  
**Executable task count:** 176

## 1. Conventions

### Status

`TODO | READY | IN_PROGRESS | BLOCKED | IN_REVIEW | DONE`

### Priority

- **P0:** required for milestone/release gate or core invariant.
- **P1:** required for V1 quality/completeness but can follow critical path.
- **P2:** compatibility/non-critical cleanup inside V1 cycle.

### Owner

- `INT` — Integration Lead
- `CBL` — Codex Backend Lead
- `AFL` — Antigravity Frontend Lead
- `AR` — Architecture Reviewer
- `DBR` — Database Reviewer
- `SR` — Security Reviewer
- `QAR` — QA Reviewer

### Reviewer codes

`AR` Architecture Reviewer · `DBR` Database Reviewer · `SR` Security Reviewer · `QAR` QA Reviewer

## 2. Milestone summary

| Milestone | Tasks | P0 | P1 | P2 |
|---|---:|---:|---:|---:|
| M0 | 7 | 5 | 2 | 0 |
| M1 | 28 | 22 | 6 | 0 |
| M2 | 21 | 18 | 3 | 0 |
| M3 | 16 | 16 | 0 | 0 |
| M4 | 14 | 14 | 0 | 0 |
| M5 | 18 | 17 | 1 | 0 |
| M6 | 8 | 5 | 3 | 0 |
| M7 | 21 | 16 | 5 | 0 |
| M8 | 25 | 18 | 7 | 0 |
| M9 | 18 | 11 | 6 | 1 |

## 3. Executable backlog

# M0 — Execution Governance

## Governance

| ID | Task | Owner | Depends on | Pri | Review | Acceptance / evidence | Required tests | Status |
|---|---|---|---|---|---|---|---|---|
| `GOV-001` | Chạy baseline audit trên commit đầu vào | INT | — | P0 | QAR | `python tools/baseline_audit.py` PASS; ghi commit SHA đầu vào trong execution log. | Theo global DoD + acceptance. | DONE |
| `GOV-002` | Đưa IMPLEMENTATION_PLAN và MASTER_BACKLOG vào docs/planning | INT | `GOV-001` | P0 | AR,QAR | Hai tài liệu được commit, cross-link từ AGENTS/README nếu phù hợp; không thay đổi contract. | Theo global DoD + acceptance. | DONE |
| `GOV-003` | Đồng bộ hướng dẫn Codex về PostgreSQL idempotency với canonical spec | CBL | `GOV-001` | P0 | AR,DBR,SR,QAR | `CODEX_BACKEND_LEAD.md` không còn khuyến nghị catch unique violation rồi tiếp tục transaction; dùng `INSERT ... ON CONFLICT DO NOTHING` theo Backend v1.3/PROJECT_RULES. | Theo global DoD + acceptance. | DONE |
| `GOV-004` | Thiết lập PR template + Change Impact + reviewer gate | INT | `GOV-002` | P0 | AR,DBR,SR,QAR | PR template bắt buộc nêu docs/API/DB/client/tests/backward compatibility và reviewer cần thiết. | Theo global DoD + acceptance. | DONE |
| `GOV-005` | Thiết lập branch/task naming và trạng thái backlog | INT | `GOV-002` | P1 | QAR | Task ID xuất hiện trong branch/PR/commit; trạng thái dùng TODO/READY/IN_PROGRESS/BLOCKED/IN_REVIEW/DONE. | Theo global DoD + acceptance. | DONE |
| `GOV-006` | Freeze implementation baseline bằng Git tag | INT | `GOV-003`<br>`GOV-004` | P0 | AR,QAR | Tag `baseline-v1-implementation-ready` (hoặc convention repo) trỏ đúng commit baseline audit PASS. | Theo global DoD + acceptance. | DONE |
| `GOV-007` | Tạo execution log và cadence báo cáo milestone | INT | `GOV-005` | P1 | QAR | Có log quyết định non-contract, blocker, review result và % completion theo milestone; không ghi secret. | Theo global DoD + acceptance. | DONE |

# M1 — Foundation Ready

## Governance Amendment / Pre-Foundation

| ID | Task | Owner | Depends on | Pri | Review | Acceptance / evidence | Required tests | Status |
|---|---|---|---|---|---|---|---|---|
| `GOV-008` | Clarify pre-CI bootstrap gate | INT | `GOV-007` | P0 | AR,QAR | `PRE_CI_BOOTSTRAP_NA` được định nghĩa hẹp; eligible prerequisite tasks được liệt kê tường minh; CI đang fail không bao giờ được waive; `CI-FND-001` phải pass CI thực tế; ngoại lệ hết hiệu lực sau khi `CI-FND-001` `DONE` và merge; gate thoát M1 vẫn yêu cầu `CI PASS` thực tế. | Baseline audit + governance consistency checks. | DONE |

## Foundation

| ID | Task | Owner | Depends on | Pri | Review | Acceptance / evidence | Required tests | Status |
|---|---|---|---|---|---|---|---|---|
| `BE-FND-001` | Bootstrap Spring Boot backend project | CBL | `GOV-006` | P0 | AR,QAR | Project build được; Java/Spring Boot theo Technical Spec; build tool được ghi rõ trong README; không thêm dependency ngoài nhu cầu baseline. | Build smoke test. | DONE |
| `BE-FND-002` | Dựng modular-monolith package/module skeleton | CBL | `BE-FND-001` | P0 | AR,QAR | Có module/package cho auth,user,onboarding,vocabulary,learning,personalization,quiz,progress,gamification,notification,ai,admin,audit,common; dependency direction đúng Architecture v1.3. | Theo global DoD + acceptance. | TODO |
| `BE-FND-003` | Thiết lập application profiles và typed configuration | CBL | `BE-FND-001` | P0 | AR,SR,QAR | `application.yml/local/test/prod`; typed properties cho assessment/SRS/personalization/gamification/notification/idempotency; default constants khớp baseline. | Theo global DoD + acceptance. | TODO |
| `DB-FND-001` | Dựng PostgreSQL local + Docker Compose | CBL | `BE-FND-001` | P0 | DBR,QAR | Local PostgreSQL start/stop reproducible; credential lấy từ env; healthcheck hoạt động. | Theo global DoD + acceptance. | TODO |
| `DB-FND-002` | Tạo Flyway schema baseline cho 34 bảng | CBL | `DB-FND-001` | P0 | DBR,AR,QAR | Fresh DB migrate từ zero tạo đúng 34 bảng theo DB v1.6; migration append-only; FK/unique/check/not-null đầy đủ. | Theo global DoD + acceptance. | TODO |
| `DB-FND-003` | Tạo indexes/constraints/partial indexes canonical | CBL | `DB-FND-002` | P0 | DBR,QAR | Có indexes cho due review, assessment in-progress, notification dedupe, primary goal, optimistic-lock targets, idempotency; EXPLAIN sanity cho query trọng yếu. | Theo global DoD + acceptance. | TODO |
| `DB-FND-004` | Seed reference data baseline | CBL | `DB-FND-002` | P0 | DBR,QAR | Seed CEFR A1..C2, goals cần thiết, 5 badges canonical; seed idempotent/repeatable theo strategy repo. | Theo global DoD + acceptance. | TODO |
| `BE-FND-004` | JPA base conventions: UUID, audit timestamps, enum, @Version | CBL | `DB-FND-002`<br>`BE-FND-002` | P0 | DBR,AR,QAR | Entity mapping theo schema; UUID/timezone conventions thống nhất; `@Version` cho `user_vocabulary_progress` và `streaks`. | Theo global DoD + acceptance. | TODO |
| `BE-FND-005` | Common error envelope + HTTP status mapping | CBL | `BE-FND-002` | P0 | AR,QAR | Global exception handler trả shape canonical; hỗ trợ `CONCURRENT_UPDATE`, `IDEMPOTENCY_KEY_REUSE` và validation codes đúng API. | Theo global DoD + acceptance. | TODO |
| `BE-FND-006` | Correlation ID + structured logging | CBL | `BE-FND-005` | P1 | SR,QAR | Mỗi request có correlation ID; log không lộ token/secret/FCM token; admin audit có thể liên kết correlation. | Theo global DoD + acceptance. | TODO |
| `BE-FND-007` | Validation + pagination + mapper conventions | CBL | `BE-FND-002`<br>`BE-FND-005` | P0 | AR,QAR | DTO không expose entity trực tiếp; validation canonical; pagination response thống nhất; client không gửi `isCorrect`. | Theo global DoD + acceptance. | TODO |
| `BE-FND-008` | IdempotencyService canonical | CBL | `DB-FND-002`<br>`BE-FND-005` | P0 | DBR,SR,AR,QAR | Body `eventId` only; SHA-256 canonical hash; claim `ON CONFLICT DO NOTHING`; replay/reuse semantics; retention 30 ngày. | Theo global DoD + acceptance. | TODO |
| `BE-FND-009` | Optimistic-lock conflict mapping | CBL | `BE-FND-004`<br>`BE-FND-005` | P0 | DBR,QAR | Stale `@Version` trả HTTP 409 + `CONCURRENT_UPDATE`; không silent overwrite. | Theo global DoD + acceptance. | TODO |
| `BE-FND-010` | Clock/timezone abstraction | CBL | `BE-FND-003` | P0 | AR,QAR | Business time lấy từ injectable Clock; local-day dùng `user_profiles.timezone`; test có fixed clock. | Theo global DoD + acceptance. | TODO |
| `BE-FND-011` | External provider interfaces | CBL | `BE-FND-002` | P1 | AR,SR,QAR | Có interfaces `LlmProvider`, `TtsProvider`, `ObjectStorageService`, `NotificationProvider`; domain không phụ thuộc SDK provider. | Theo global DoD + acceptance. | TODO |
| `BE-FND-012` | Scheduler/background-job foundation | CBL | `BE-FND-003`<br>`BE-FND-010` | P1 | AR,QAR | Có scheduler framework cho notification, daily-plan pregen, idempotency cleanup, AI aggregation; không tạo client-facing async AI job. | Theo global DoD + acceptance. | TODO |
| `BE-FND-013` | Actuator health/readiness | CBL | `DB-FND-001` | P1 | AR,SR,QAR | Health endpoint kiểm tra DB; AI provider failure không làm core app unhealthy theo spec. | Theo global DoD + acceptance. | TODO |
| `QA-FND-001` | Testcontainers PostgreSQL integration harness | QAR | `DB-FND-002`<br>`BE-FND-004` | P0 | DBR,QAR | Integration tests chạy trên PostgreSQL thật; Flyway tự chạy; không dùng H2 thay thế cho constraint/JSONB/timestamp behavior. | Theo global DoD + acceptance. | TODO |
| `QA-FND-002` | OpenAPI/runtime contract-test harness | QAR | `BE-FND-007` | P0 | AR,QAR | OpenAPI v1.4 parse/validate trong CI; DTO/controller/status/error contract có test khung. | Theo global DoD + acceptance. | TODO |
| `CI-FND-001` | CI pipeline bắt buộc | CBL | `QA-FND-001`<br>`QA-FND-002`<br>`GOV-004` | P0 | AR,DBR,SR,QAR | Pipeline: baseline audit → build/static checks → unit → integration → OpenAPI → package; fail thì không merge. | Theo global DoD + acceptance. | TODO |
| `SEC-FND-001` | Secrets/env/config security baseline | CBL | `BE-FND-003` | P0 | SR,QAR | Không secret trong repo/log; `.env` mẫu không chứa giá trị thật; prod config dùng env/secret manager. | Theo global DoD + acceptance. | TODO |
| `SEC-FND-002` | CORS, security headers, rate-limit foundation | CBL | `BE-FND-005`<br>`SEC-FND-001` | P1 | SR,AR,QAR | CORS allowlist cấu hình; security headers hợp lý; rate-limit hook/config có test cho auth/admin/AI nếu spec yêu cầu. | Theo global DoD + acceptance. | TODO |
| `ADM-FND-001` | Bootstrap React + TypeScript + Vite Admin | AFL | `GOV-006` | P0 | AR,QAR | SPA build được; structure theo Admin Tech v1.1; không Thymeleaf; Vietnamese message module central. | Theo global DoD + acceptance. | TODO |
| `ADM-FND-002` | Admin API client/query/mutation/error foundation | AFL | `ADM-FND-001` | P0 | SR,QAR | Typed models, HTTP client, auth interceptor, 401/403/409 handling, query/mutation state, loading/empty/error pattern. | Theo global DoD + acceptance. | TODO |
| `AND-FND-001` | Bootstrap Android Java MVVM project | AFL | `GOV-006` | P0 | AR,QAR | Project build/install được; package/feature structure đúng Android v1.1; strings.xml Vietnamese single-locale. | Theo global DoD + acceptance. | TODO |
| `AND-FND-002` | Android HTTP/token/error/eventId foundation | AFL | `AND-FND-001` | P0 | SR,QAR | Retrofit/OkHttp hoặc approved equivalent; secure token storage; serialized refresh; canonical error parser; eventId helper giữ ID khi retry. | Theo global DoD + acceptance. | TODO |
| `AND-FND-003` | Android navigation/design/connectivity baseline | AFL | `AND-FND-001` | P1 | AR,QAR | Navigation shell, design tokens/resources, UiState, connectivity detection; offline mutation bị chặn, read-only cache boundary rõ. | Theo global DoD + acceptance. | TODO |

# M2 — Identity & Catalog

## Identity

| ID | Task | Owner | Depends on | Pri | Review | Acceptance / evidence | Required tests | Status |
|---|---|---|---|---|---|---|---|---|
| `BE-AUTH-001` | User/refresh-token repositories + password hashing | CBL | `BE-FND-004`<br>`SEC-FND-001` | P0 | DBR,SR,QAR | users/refresh_tokens mappings + repository; password hash secure; token stored hash only. | Theo global DoD + acceptance. | TODO |
| `BE-AUTH-002` | POST /auth/register | CBL | `BE-AUTH-001`<br>`BE-FND-007` | P0 | SR,QAR | Register validates email/password, duplicate handling deterministic, role USER default, response đúng OpenAPI. | Theo global DoD + acceptance. | TODO |
| `BE-AUTH-003` | POST /auth/login + brute-force protection | CBL | `BE-AUTH-001`<br>`BE-FND-010` | P0 | SR,QAR | 5 failed attempts → lock ~5 phút; success resets counters per baseline; locked account response đúng API. | Theo global DoD + acceptance. | TODO |
| `BE-AUTH-004` | JWT access token issuing/verification | CBL | `BE-AUTH-003`<br>`SEC-FND-001` | P0 | SR,QAR | Short-lived access token, subject/role claims canonical, expiry enforced. | Theo global DoD + acceptance. | TODO |
| `BE-AUTH-005` | POST /auth/refresh + rotation | CBL | `BE-AUTH-004` | P0 | SR,DBR,QAR | Refresh expiry/revoke/hash/rotation hoạt động; reuse/revoked token bị từ chối. | Theo global DoD + acceptance. | TODO |
| `BE-AUTH-006` | POST /auth/logout revoke refresh token | CBL | `BE-AUTH-005` | P0 | SR,QAR | Logout revoke token/session theo contract; retry an toàn. | Theo global DoD + acceptance. | TODO |
| `BE-AUTH-007` | POST /auth/google | CBL | `BE-AUTH-004`<br>`BE-FND-011` | P1 | SR,QAR | Google identity được verify server-side; account link/create không trust client profile. | Theo global DoD + acceptance. | TODO |
| `BE-USER-001` | Current user/profile APIs | CBL | `BE-AUTH-004`<br>`BE-FND-004` | P0 | SR,QAR | `GET /users/me`, `GET/PUT /users/me/profile`; ownership implicit current user; timezone/daily_learning_minutes validate. | Theo global DoD + acceptance. | TODO |

## Onboarding

| ID | Task | Owner | Depends on | Pri | Review | Acceptance / evidence | Required tests | Status |
|---|---|---|---|---|---|---|---|---|
| `BE-GOAL-001` | Goals + user goals APIs | CBL | `BE-USER-001`<br>`DB-FND-004` | P0 | DBR,QAR | `GET /goals`, `GET/PUT /users/me/goals`; tối đa một primary goal enforced DB + service. | Theo global DoD + acceptance. | TODO |
| `BE-CEFR-001` | GET /cefr-levels | CBL | `DB-FND-004` | P0 | QAR | Trả A1..C2 canonical, stable order. | Theo global DoD + acceptance. | TODO |

## Catalog

| ID | Task | Owner | Depends on | Pri | Review | Acceptance / evidence | Required tests | Status |
|---|---|---|---|---|---|---|---|---|
| `DB-CONTENT-001` | Seed/import vocabulary content đủ placement | CBL | `DB-FND-002`<br>`BE-CEFR-001` | P0 | DBR,QAR | Mỗi CEFR có >=30 active vocabulary với `meaning_vi` usable; dữ liệu demo/import có provenance; không duplicate invalid. | Theo global DoD + acceptance. | TODO |
| `DB-CONTENT-002` | Seed goal_topics relevance | CBL | `DB-FND-004`<br>`DB-CONTENT-001` | P0 | DBR,QAR | `goal_topics` có mapping/relevance 0..1 đủ để recommendation V1 không rơi toàn bộ về GENERAL. | Theo global DoD + acceptance. | TODO |
| `BE-VOC-001` | Topic read APIs | CBL | `DB-CONTENT-001`<br>`BE-FND-007` | P0 | QAR | `GET /topics`, `GET /topics/{topicId}` hỗ trợ active data và contract. | Theo global DoD + acceptance. | TODO |
| `BE-VOC-002` | Vocabulary list/detail/examples APIs | CBL | `DB-CONTENT-001`<br>`BE-FND-007` | P0 | DBR,QAR | `GET /vocabulary`, `/{id}`, `/{id}/examples`; filter/search/pagination không N+1; inactive content policy đúng spec. | Theo global DoD + acceptance. | TODO |
| `BE-VOC-003` | TTS/audio integration boundary | CBL | `BE-VOC-002`<br>`BE-FND-011` | P1 | AR,SR,QAR | Vocabulary audio/TTS theo technical contract; provider failure degrade gracefully; không block metadata core. | Theo global DoD + acceptance. | TODO |

## Identity/Catalog

| ID | Task | Owner | Depends on | Pri | Review | Acceptance / evidence | Required tests | Status |
|---|---|---|---|---|---|---|---|---|
| `QA-IDCAT-001` | Auth/profile/catalog integration suite | QAR | `BE-AUTH-007`<br>`BE-USER-001`<br>`BE-GOAL-001`<br>`BE-VOC-003` | P0 | SR,DBR,QAR | Register/login/refresh/logout/google/profile/goals/catalog happy+error+authz; PostgreSQL integration. | Theo global DoD + acceptance. | TODO |

## Android Identity

| ID | Task | Owner | Depends on | Pri | Review | Acceptance / evidence | Required tests | Status |
|---|---|---|---|---|---|---|---|---|
| `AND-AUTH-001` | Splash/auth/token lifecycle | AFL | `AND-FND-002`<br>`BE-AUTH-006` | P0 | SR,QAR | Splash restore session; 401 refresh serialized; expired/revoked → login; no token logs. | Theo global DoD + acceptance. | TODO |
| `AND-AUTH-002` | Login/Register UI | AFL | `AND-AUTH-001`<br>`BE-AUTH-003` | P0 | QAR | Validation, brute-force locked UI, loading/error; Vietnamese copy. | Theo global DoD + acceptance. | TODO |

## Android Onboarding

| ID | Task | Owner | Depends on | Pri | Review | Acceptance / evidence | Required tests | Status |
|---|---|---|---|---|---|---|---|---|
| `AND-ONB-001` | Profile goals + daily time selection | AFL | `AND-AUTH-002`<br>`BE-GOAL-001`<br>`BE-USER-001` | P0 | QAR | Save profile/goals/time through API; no client CEFR logic. | Theo global DoD + acceptance. | TODO |

## Android Catalog

| ID | Task | Owner | Depends on | Pri | Review | Acceptance / evidence | Required tests | Status |
|---|---|---|---|---|---|---|---|---|
| `AND-VOC-001` | Vocabulary browse/search/detail | AFL | `AND-FND-003`<br>`BE-VOC-002` | P1 | QAR | List/detail/examples states, pagination/search, offline read-only cache where available. | Theo global DoD + acceptance. | TODO |

## Admin Identity

| ID | Task | Owner | Depends on | Pri | Review | Acceptance / evidence | Required tests | Status |
|---|---|---|---|---|---|---|---|---|
| `ADM-AUTH-001` | Admin auth + route guard | AFL | `ADM-FND-002`<br>`BE-AUTH-004` | P0 | SR,QAR | ADMIN role required; USER cannot enter protected route; 401/403 behavior đúng spec. | Theo global DoD + acceptance. | TODO |

# M3 — First Vertical Slice — Learning/SRS

## Learning Core

| ID | Task | Owner | Depends on | Pri | Review | Acceptance / evidence | Required tests | Status |
|---|---|---|---|---|---|---|---|---|
| `BE-SRS-001` | SRS typed properties + pure calculation model | CBL | `BE-FND-003`<br>`BE-FND-010` | P0 | AR,QAR | Defaults match `sm2-ext-v1`; pure calculation input/output testable; interval clamp/ease-factor canonical. | Theo global DoD + acceptance. | TODO |
| `BE-SRS-002` | SrsService state transitions | CBL | `BE-SRS-001` | P0 | AR,QAR | NEW/LEARNING/REVIEWING/MASTERED transitions exact; q=0..5; backend derives `isCorrect`. | Theo global DoD + acceptance. | TODO |
| `QA-SRS-001` | SRS unit/boundary matrix | QAR | `BE-SRS-002` | P0 | QAR | Cover q0..5, first attempt, repetition, failure regression, min/max interval, response-time extension, mastered boundaries. | Theo global DoD + acceptance. | TODO |
| `BE-LEARN-001` | Learning session start/get | CBL | `BE-FND-008`<br>`BE-VOC-002`<br>`BE-USER-001` | P0 | DBR,SR,QAR | `POST /learning/sessions` body eventId + replay; `GET /learning/sessions/{id}` ownership enforced. | Theo global DoD + acceptance. | TODO |
| `BE-LEARN-002` | Learning attempt transaction | CBL | `BE-LEARN-001`<br>`BE-SRS-002`<br>`BE-GAME-002`<br>`BE-FND-008`<br>`BE-FND-009` | P0 | AR,DBR,SR,QAR | Atomic claim→load version→derive correctness→SRS→attempt→progress→XP/streak→snapshot→commit; DB check blocks inconsistent correctness. | Theo global DoD + acceptance. | TODO |
| `BE-LEARN-003` | Learning session completion | CBL | `BE-LEARN-002` | P0 | DBR,QAR | `POST /learning/sessions/{id}/complete` eventId; requires >=1 accepted attempt; +10 XP once. | Theo global DoD + acceptance. | TODO |
| `BE-LEARN-004` | Due review query/API | CBL | `BE-LEARN-002`<br>`DB-FND-003` | P0 | DBR,QAR | `GET /learning/reviews`; due date authoritative; stable priority/order; performant query. | Theo global DoD + acceptance. | TODO |
| `QA-LEARN-001` | Learning transaction integration tests | QAR | `BE-LEARN-004`<br>`BE-PROG-001` | P0 | AR,DBR,SR,QAR | Happy/error/rollback, answerQuality DB check, XP/streak effects, due review, session complete. | Theo global DoD + acceptance. | TODO |
| `QA-LEARN-002` | Idempotency + optimistic concurrency tests | QAR | `BE-LEARN-002` | P0 | DBR,SR,QAR | Same event replay one mutation; reuse → `IDEMPOTENCY_KEY_REUSE`; concurrent duplicate no 500; stale progress → `CONCURRENT_UPDATE`. | Theo global DoD + acceptance. | TODO |

## Gamification Core

| ID | Task | Owner | Depends on | Pri | Review | Acceptance / evidence | Required tests | Status |
|---|---|---|---|---|---|---|---|---|
| `BE-GAME-001` | XP ledger core service | CBL | `BE-FND-004` | P0 | DBR,QAR | xp_logs reason canonical; award exactly once per source event; total XP derived correctly. | Theo global DoD + acceptance. | TODO |
| `BE-GAME-002` | Streak mutation core | CBL | `BE-GAME-001`<br>`BE-FND-010`<br>`BE-FND-009` | P0 | DBR,QAR | Timezone day boundary exact; one qualifying increment/day; 10 XP maintained once; assessment excluded. | Theo global DoD + acceptance. | TODO |

## Progress

| ID | Task | Owner | Depends on | Pri | Review | Acceptance / evidence | Required tests | Status |
|---|---|---|---|---|---|---|---|---|
| `BE-PROG-001` | Vocabulary progress API | CBL | `BE-LEARN-002` | P0 | QAR | `GET /vocabulary/{id}/progress`; exact status/repetitions/interval/nextReview fields; ownership. | Theo global DoD + acceptance. | TODO |

## Android Learning

| ID | Task | Owner | Depends on | Pri | Review | Acceptance / evidence | Required tests | Status |
|---|---|---|---|---|---|---|---|---|
| `AND-LEARN-001` | Learning session + Flashcard state flow | AFL | `AND-VOC-001`<br>`BE-LEARN-001` | P0 | QAR | Start session once/eventId, render flashcard, timer lifecycle, interruption/process handling per Android spec. | Theo global DoD + acceptance. | TODO |
| `AND-LEARN-002` | Submit answerQuality attempt + retry | AFL | `AND-LEARN-001`<br>`BE-LEARN-002` | P0 | QAR | Send answerQuality/eventId; never isCorrect; retry preserves eventId; 409 codes handled distinctly. | Theo global DoD + acceptance. | TODO |
| `AND-LEARN-003` | Review list + learning completion | AFL | `AND-LEARN-002`<br>`BE-LEARN-004`<br>`BE-LEARN-003` | P0 | QAR | Due reviews displayed; complete session; progress refresh after accepted attempt. | Theo global DoD + acceptance. | TODO |

## Vertical Slice

| ID | Task | Owner | Depends on | Pri | Review | Acceptance / evidence | Required tests | Status |
|---|---|---|---|---|---|---|---|---|
| `QA-E2E-001` | E2E Vocabulary → Session → Attempt → SRS → Progress | QAR | `AND-LEARN-003`<br>`QA-LEARN-002` | P0 | AR,DBR,SR,QAR | Critical first vertical slice passes on real backend/PostgreSQL + Android client; retry/concurrency included. | Theo global DoD + acceptance. | TODO |

# M4 — Assessment & Quiz

## Assessment

| ID | Task | Owner | Depends on | Pri | Review | Acceptance / evidence | Required tests | Status |
|---|---|---|---|---|---|---|---|---|
| `BE-ASMT-001` | AssessmentProperties + deterministic question selector | CBL | `DB-CONTENT-001`<br>`BE-FND-010` | P0 | AR,QAR | `assessment-block-v1`: A1 start, 4/block, 3 promote, 1 demote, 2 hold, 20/50 limits, stable blocks=2. | Theo global DoD + acceptance. | TODO |
| `BE-ASMT-002` | Assessment aggregate/repositories | CBL | `BE-FND-004`<br>`BE-ASMT-001` | P0 | DBR,QAR | `user_level_assessments` + `assessment_items`; one IN_PROGRESS/user; question/options/order persisted before response. | Theo global DoD + acceptance. | TODO |
| `BE-ASMT-003` | Start/get/next assessment APIs | CBL | `BE-ASMT-002`<br>`BE-FND-008` | P0 | DBR,SR,QAR | `POST /assessments`, `GET /{id}`, `GET /{id}/next-question`; start uses eventId; next GET only reads persisted unanswered item. | Theo global DoD + acceptance. | TODO |
| `BE-ASMT-004` | Submit assessment answer | CBL | `BE-ASMT-003` | P0 | AR,DBR,SR,QAR | Answer eventId; score/state update atomic; no duplicate question; final CEFR=current difficulty; errors canonical. | Theo global DoD + acceptance. | TODO |
| `QA-ASMT-001` | Assessment algorithm + integration tests | QAR | `BE-ASMT-004` | P0 | AR,DBR,QAR | Promote/demote/hold/boundary/stop/content unavailable/idempotency/concurrent answer; >=30 content guard. | Theo global DoD + acceptance. | TODO |

## Android Assessment

| ID | Task | Owner | Depends on | Pri | Review | Acceptance / evidence | Required tests | Status |
|---|---|---|---|---|---|---|---|---|
| `AND-ASMT-001` | Placement test UI + persisted question flow | AFL | `AND-ONB-001`<br>`BE-ASMT-004` | P0 | QAR | Question/options render from server; submit eventId; no client adaptive algorithm; resume IN_PROGRESS. | Theo global DoD + acceptance. | TODO |
| `AND-ASMT-002` | Placement completion/result routing | AFL | `AND-ASMT-001` | P0 | QAR | Show server CEFR, complete onboarding, route Home; error/content-unavailable states. | Theo global DoD + acceptance. | TODO |

## Quiz

| ID | Task | Owner | Depends on | Pri | Review | Acceptance / evidence | Required tests | Status |
|---|---|---|---|---|---|---|---|---|
| `BE-QUIZ-001` | Quiz list/detail read APIs | CBL | `BE-VOC-002` | P0 | QAR | `GET /quizzes`, `GET /quizzes/{id}` active/published rules, stable question DTO. | Theo global DoD + acceptance. | TODO |
| `BE-QUIZ-002` | Start/get quiz attempt | CBL | `BE-QUIZ-001`<br>`BE-FND-008` | P0 | DBR,SR,QAR | `POST /quizzes/{id}/attempts` eventId; `GET /quiz-attempts/{id}` ownership and persisted attempt. | Theo global DoD + acceptance. | TODO |
| `BE-QUIZ-003` | Submit quiz answer | CBL | `BE-QUIZ-002`<br>`BE-GAME-001` | P0 | DBR,SR,QAR | eventId; backend correctness; answer persisted; correct +5 XP once; retry no duplicate. | Theo global DoD + acceptance. | TODO |
| `BE-QUIZ-004` | Complete quiz + deterministic score | CBL | `BE-QUIZ-003` | P0 | DBR,QAR | `POST /quiz-attempts/{id}/complete` eventId; score/rounding exact; completion terminal; perfect-quiz eligibility hook. | Theo global DoD + acceptance. | TODO |
| `QA-QUIZ-001` | Quiz unit/integration/idempotency tests | QAR | `BE-QUIZ-004` | P0 | DBR,SR,QAR | Start/answer/complete/get; incorrect/correct; retry; score; XP; authz. | Theo global DoD + acceptance. | TODO |

## Android Quiz

| ID | Task | Owner | Depends on | Pri | Review | Acceptance / evidence | Required tests | Status |
|---|---|---|---|---|---|---|---|---|
| `AND-QUIZ-001` | Quiz list/start/answer/result flow | AFL | `BE-QUIZ-004`<br>`AND-FND-003` | P0 | QAR | All quiz mutations preserve eventId; result from backend; no client correctness/score logic. | Theo global DoD + acceptance. | TODO |

## Vertical Slice

| ID | Task | Owner | Depends on | Pri | Review | Acceptance / evidence | Required tests | Status |
|---|---|---|---|---|---|---|---|---|
| `QA-E2E-002` | E2E onboarding placement + quiz | QAR | `AND-ASMT-002`<br>`AND-QUIZ-001`<br>`QA-ASMT-001`<br>`QA-QUIZ-001` | P0 | AR,DBR,SR,QAR | First-time user flow through CEFR placement and a quiz works end-to-end. | Theo global DoD + acceptance. | TODO |

# M5 — Personalization & Daily Plan

## Personalization

| ID | Task | Owner | Depends on | Pri | Review | Acceptance / evidence | Required tests | Status |
|---|---|---|---|---|---|---|---|---|
| `BE-PERS-001` | WeaknessService `weakness-rule-v1` | CBL | `BE-LEARN-002`<br>`BE-QUIZ-003` | P0 | AR,QAR | Last10/last5 windows, weights/thresholds/reason codes exact; insufficient history deterministic. | Theo global DoD + acceptance. | TODO |
| `BE-PERS-002` | ForgettingRiskService `forgetting-risk-v1` | CBL | `BE-PERS-001`<br>`BE-SRS-002` | P0 | AR,QAR | Formula weights .45/.25/.15/.10/.05; LOW/MEDIUM/HIGH thresholds; due date remains authoritative. | Theo global DoD + acceptance. | TODO |
| `BE-PERS-003` | RecommendationService deterministic ranking | CBL | `BE-PERS-002`<br>`DB-CONTENT-002` | P0 | AR,DBR,QAR | Review/new ranking/tie-break/reason codes exact; exact CEFR then one level below; no level above; topic diversity cap 40%. | Theo global DoD + acceptance. | TODO |
| `BE-PERS-004` | Performance classification + workload guard | CBL | `BE-PERS-003` | P0 | AR,QAR | 7-day window; min 3 active days; HIGH +10%, LOW -20%, bounded +20/-30; initial units/min exact. | Theo global DoD + acceptance. | TODO |
| `BE-PERS-005` | New/Review/Quiz allocation + time budget | CBL | `BE-PERS-004` | P0 | AR,QAR | Ratios high backlog/high retention/normal, rounding, availability transfer and 60/30/45 sec budget exact. | Theo global DoD + acceptance. | TODO |
| `QA-PERS-001` | Algorithm unit matrix | QAR | `BE-PERS-005` | P0 | AR,QAR | Weak/risk/recommendation/workload/allocation boundary values + deterministic tie-break. | Theo global DoD + acceptance. | TODO |

## Daily Plan

| ID | Task | Owner | Depends on | Pri | Review | Acceptance / evidence | Required tests | Status |
|---|---|---|---|---|---|---|---|---|
| `BE-PERS-006` | DailyPlanService snapshot generation | CBL | `BE-PERS-005`<br>`BE-FND-010` | P0 | AR,DBR,QAR | UNIQUE(user,local date); ordered persisted `daily_plan_items`; no midday rerank; changes apply next day. | Theo global DoD + acceptance. | TODO |
| `BE-PERS-007` | Daily-plan progress hooks | CBL | `BE-PERS-006`<br>`BE-LEARN-002`<br>`BE-QUIZ-003` | P0 | DBR,QAR | REVIEW/NEW complete on first accepted learning attempt after plan creation; QUIZ count increments per accepted quiz answer. | Theo global DoD + acceptance. | TODO |
| `BE-PERS-008` | Today plan/read item APIs | CBL | `BE-PERS-006` | P0 | QAR | `GET /learning/today`, `/today/items`; same day returns same planId/order; QUIZ vocabularyId nullable. | Theo global DoD + acceptance. | TODO |
| `BE-PERS-009` | Complete Daily Plan mutation | CBL | `BE-PERS-007`<br>`BE-GAME-001`<br>`BE-FND-008` | P0 | DBR,SR,QAR | `POST /learning/today/complete` eventId; all targets → COMPLETED +50 once; otherwise PARTIAL +0; terminal. | Theo global DoD + acceptance. | TODO |
| `QA-PERS-002` | Daily Plan integration/concurrency/timezone suite | QAR | `BE-PERS-009` | P0 | AR,DBR,SR,QAR | Snapshot stable, local-day rollover, partial/completed, XP once, concurrent GET/create, retry completion. | Theo global DoD + acceptance. | TODO |

## Progress

| ID | Task | Owner | Depends on | Pri | Review | Acceptance / evidence | Required tests | Status |
|---|---|---|---|---|---|---|---|---|
| `BE-PROG-002` | Progress overview API | CBL | `BE-PERS-001`<br>`BE-LEARN-002` | P0 | QAR | `GET /progress`; accuracy/wordsLearned/wordsMastered/learningTime semantics canonical. | Theo global DoD + acceptance. | TODO |
| `BE-PROG-003` | Weak words API | CBL | `BE-PERS-001` | P0 | QAR | `GET /progress/weak-words`; ordering/reason/score consistent WeaknessService. | Theo global DoD + acceptance. | TODO |
| `BE-PROG-004` | Progress trend API | CBL | `BE-PROG-002` | P1 | DBR,QAR | `GET /progress/trend`; period/date semantics documented; performant aggregation. | Theo global DoD + acceptance. | TODO |

## Android Daily Plan

| ID | Task | Owner | Depends on | Pri | Review | Acceptance / evidence | Required tests | Status |
|---|---|---|---|---|---|---|---|---|
| `AND-HOME-001` | Home bootstrap + Today's Plan | AFL | `BE-PERS-008`<br>`AND-LEARN-003` | P0 | QAR | Home shows backend targets/items/progress; Continue Learning uses server snapshot; no client ranking. | Theo global DoD + acceptance. | TODO |
| `AND-HOME-002` | Daily plan execution/completion UX | AFL | `AND-HOME-001`<br>`BE-PERS-009` | P0 | QAR | Navigate REVIEW/NEW/QUIZ items, update progress after accepted responses, complete eventId once. | Theo global DoD + acceptance. | TODO |

## Android Progress

| ID | Task | Owner | Depends on | Pri | Review | Acceptance / evidence | Required tests | Status |
|---|---|---|---|---|---|---|---|---|
| `AND-PROG-001` | Progress overview/trend/weak words | AFL | `BE-PROG-004`<br>`BE-PROG-003` | P0 | QAR | Render server aggregates, weak reasons, loading/empty/error; no local metric formula. | Theo global DoD + acceptance. | TODO |

## Vertical Slice

| ID | Task | Owner | Depends on | Pri | Review | Acceptance / evidence | Required tests | Status |
|---|---|---|---|---|---|---|---|---|
| `QA-E2E-003` | E2E personalized Daily Plan journey | QAR | `AND-HOME-002`<br>`AND-PROG-001`<br>`QA-PERS-002` | P0 | AR,DBR,SR,QAR | Daily plan create→learn/review/quiz→progress→complete; same-day snapshot and XP retry verified. | Theo global DoD + acceptance. | TODO |

# M6 — Gamification Complete

## Gamification

| ID | Task | Owner | Depends on | Pri | Review | Acceptance / evidence | Required tests | Status |
|---|---|---|---|---|---|---|---|---|
| `BE-GAME-003` | Badge evaluator + seeded badge rules | CBL | `BE-GAME-001`<br>`BE-QUIZ-004`<br>`BE-LEARN-003` | P0 | DBR,QAR | FIRST_LESSON/STREAK_7/WORDS_100/WORDS_500/PERFECT_QUIZ; `UNIQUE(user_id,badge_id)` prevents duplicate. | Theo global DoD + acceptance. | TODO |
| `BE-GAME-004` | XP/level/history APIs | CBL | `BE-GAME-001` | P0 | QAR | `GET /gamification/xp`, `/xp/history`; level=floor(total/500)+1; nextLevelXp absolute threshold. | Theo global DoD + acceptance. | TODO |
| `BE-GAME-005` | Streak API | CBL | `BE-GAME-002` | P0 | QAR | `GET /gamification/streak` exposes current/longest/date consistently. | Theo global DoD + acceptance. | TODO |
| `BE-GAME-006` | Badges API | CBL | `BE-GAME-003` | P0 | QAR | `GET /gamification/badges` seeded/earned state correct. | Theo global DoD + acceptance. | TODO |
| `BE-GAME-007` | Leaderboard API | CBL | `BE-GAME-001`<br>`DB-FND-003` | P1 | DBR,QAR | `GET /gamification/leaderboard`; period SUM xp_logs; rank semantics stable; user_id tie order. | Theo global DoD + acceptance. | TODO |
| `QA-GAME-001` | Gamification integration/concurrency tests | QAR | `BE-GAME-007` | P0 | DBR,QAR | XP once, streak timezone/concurrent activity, badge idempotency, perfect quiz >=5, level boundaries, leaderboard ties. | Theo global DoD + acceptance. | TODO |

## Android Gamification

| ID | Task | Owner | Depends on | Pri | Review | Acceptance / evidence | Required tests | Status |
|---|---|---|---|---|---|---|---|---|
| `AND-GAME-001` | XP/level/streak/badge/leaderboard UI | AFL | `BE-GAME-007`<br>`AND-HOME-001` | P1 | QAR | Client displays server values only; refresh after qualifying actions; accessible states. | Theo global DoD + acceptance. | TODO |

## Vertical Slice

| ID | Task | Owner | Depends on | Pri | Review | Acceptance / evidence | Required tests | Status |
|---|---|---|---|---|---|---|---|---|
| `QA-E2E-004` | E2E multi-day streak/gamification scenario | QAR | `AND-GAME-001`<br>`QA-GAME-001` | P1 | DBR,QAR | Fixed-clock scenario verifies same-day no duplicate, next day +1, skipped day reset, badges/XP. | Theo global DoD + acceptance. | TODO |

# M7 — AI & Notifications

## AI Content

| ID | Task | Owner | Depends on | Pri | Review | Acceptance / evidence | Required tests | Status |
|---|---|---|---|---|---|---|---|---|
| `BE-AI-001` | LLM provider implementation + provider abstraction | CBL | `BE-FND-011`<br>`SEC-FND-001` | P0 | AR,SR,QAR | Provider behind `LlmProvider`; timeout/error mapping; API key secret; provider swappable. | Theo global DoD + acceptance. | TODO |
| `BE-AI-002` | Prompt builder + structured response validator + safety | CBL | `BE-AI-001` | P0 | AR,SR,QAR | Prompts per content type; JSON/schema validation; unsafe/malformed rejected/fallback per spec; prompt injection boundary considered. | Theo global DoD + acceptance. | TODO |
| `BE-AI-003` | AI budget guard + ai_requests usage logging | CBL | `BE-AI-001`<br>`BE-FND-008` | P0 | DBR,SR,QAR | Request/token/estimated cost logged; budget checked atomically enough to avoid overspend race; duplicate eventId không double-charge. | Theo global DoD + acceptance. | TODO |
| `BE-AI-004` | Reusable AI generation synchronous | CBL | `BE-AI-002`<br>`BE-AI-003`<br>`BE-VOC-002` | P0 | AR,DBR,SR,QAR | `POST /admin/ai-content/generate` eventId → 201 PENDING_REVIEW; no 202/jobId; generated record persisted. | Theo global DoD + acceptance. | TODO |
| `BE-AI-005` | AI content cache/generation-key behavior | CBL | `BE-AI-004` | P1 | DBR,QAR | Equivalent reusable request can reuse valid approved/cache content per spec; no duplicate cost/content race. | Theo global DoD + acceptance. | TODO |
| `BE-AI-006` | Admin AI list/detail APIs | CBL | `BE-AI-004` | P0 | SR,QAR | `GET /admin/ai-content`, `/{contentId}` pagination/filter/ownership/admin-only. | Theo global DoD + acceptance. | TODO |
| `BE-AI-007` | Approve reusable AI content | CBL | `BE-AI-006` | P0 | DBR,SR,QAR | `POST .../approve` valid state transition; audit action APPROVE; publish availability per lifecycle. | Theo global DoD + acceptance. | TODO |
| `BE-AI-008` | Reject reusable AI content + review_note audit | CBL | `BE-AI-006` | P0 | DBR,SR,QAR | `POST .../reject`; review_note required/stored; `admin_audit_logs.details` contains same reason; action REJECT. | Theo global DoD + acceptance. | TODO |
| `BE-AI-009` | Personalized real-time exercise | CBL | `BE-AI-002`<br>`BE-AI-003`<br>`BE-PERS-003` | P0 | AR,SR,QAR | `POST /learning/personalized-exercise` eventId → 200; context personalized; validation/safety; no per-result admin review; retry no second LLM cost. | Theo global DoD + acceptance. | TODO |
| `BE-AI-010` | AI usage daily aggregation | CBL | `BE-AI-003`<br>`BE-FND-012` | P1 | DBR,QAR | `ai_usage_daily` aggregation retry-safe; totals reconcile with requests. | Theo global DoD + acceptance. | TODO |
| `QA-AI-001` | AI unit/integration/failure/budget tests | QAR | `BE-AI-010`<br>`BE-AI-009` | P0 | AR,SR,QAR | Valid/malformed/unsafe/timeout/5xx/budget exceeded/retry/idempotency/cost; no client-facing async job. | Theo global DoD + acceptance. | TODO |

## Notifications

| ID | Task | Owner | Depends on | Pri | Review | Acceptance / evidence | Required tests | Status |
|---|---|---|---|---|---|---|---|---|
| `BE-NOTI-001` | Device registration/deactivation APIs | CBL | `BE-FND-007`<br>`BE-AUTH-004` | P0 | DBR,SR,QAR | `PUT /devices/{installationId}/push-token`, `DELETE /devices/{installationId}`; natural idempotency; token ownership; token not logged. | Theo global DoD + acceptance. | TODO |
| `BE-NOTI-002` | Notification preferences APIs | CBL | `BE-USER-001`<br>`BE-FND-010` | P0 | DBR,QAR | `GET/PUT /notification-preferences`; default 19:00; timezone from profile; validation. | Theo global DoD + acceptance. | TODO |
| `BE-NOTI-003` | FCM NotificationProvider | CBL | `BE-FND-011`<br>`SEC-FND-001` | P0 | SR,QAR | FCM send abstraction; invalid token handling/deactivation; secrets protected. | Theo global DoD + acceptance. | TODO |
| `BE-NOTI-004` | Notification scheduling + dedupe | CBL | `BE-NOTI-002`<br>`BE-NOTI-003`<br>`BE-FND-012`<br>`BE-PERS-006`<br>`BE-GAME-002` | P0 | AR,DBR,QAR | Daily plan 07:00, review preferred time, streak 21:00 local; type/user/local-date dedupe; prefs respected. | Theo global DoD + acceptance. | TODO |
| `BE-NOTI-005` | Notification persistence/list/delivery statuses | CBL | `BE-NOTI-004` | P0 | DBR,QAR | `GET /notifications`; PENDING/SENT/FAILED/CANCELLED lifecycle; retry policy does not duplicate notification rows. | Theo global DoD + acceptance. | TODO |
| `QA-NOTI-001` | Notification timezone/dedupe/provider tests | QAR | `BE-NOTI-005` | P0 | DBR,SR,QAR | Push disabled, review disabled, due=0, token refresh, same day dedupe, fixed timezone/day boundaries, FCM failure. | Theo global DoD + acceptance. | TODO |

## Android AI

| ID | Task | Owner | Depends on | Pri | Review | Acceptance / evidence | Required tests | Status |
|---|---|---|---|---|---|---|---|---|
| `AND-AI-001` | Personalized exercise UI | AFL | `BE-AI-009`<br>`AND-HOME-001` | P1 | QAR | AI loading/failure/retry preserves eventId; no admin-review assumption; no local AI logic. | Theo global DoD + acceptance. | TODO |

## Android Notifications

| ID | Task | Owner | Depends on | Pri | Review | Acceptance / evidence | Required tests | Status |
|---|---|---|---|---|---|---|---|---|
| `AND-NOTI-001` | FCM token + permission + device lifecycle | AFL | `BE-NOTI-003`<br>`BE-NOTI-001`<br>`AND-AUTH-001` | P0 | SR,QAR | installationId persistent; token refresh PUT; logout deactivates when appropriate; Android notification permission handled. | Theo global DoD + acceptance. | TODO |
| `AND-NOTI-002` | Notification preferences/list/deeplink | AFL | `BE-NOTI-005`<br>`BE-NOTI-002` | P1 | QAR | Preferences save, list screen/states, deep-link to relevant Home/Review; no duplicate local scheduler. | Theo global DoD + acceptance. | TODO |

## Vertical Slice

| ID | Task | Owner | Depends on | Pri | Review | Acceptance / evidence | Required tests | Status |
|---|---|---|---|---|---|---|---|---|
| `QA-E2E-005` | E2E personalized AI + notification | QAR | `AND-AI-001`<br>`AND-NOTI-002`<br>`QA-AI-001`<br>`QA-NOTI-001` | P1 | AR,SR,QAR | Personalized AI retry no double cost; notification delivery scenario works with fixed clock/test provider. | Theo global DoD + acceptance. | TODO |

# M8 — Client/Admin Completion

## Admin Backend

| ID | Task | Owner | Depends on | Pri | Review | Acceptance / evidence | Required tests | Status |
|---|---|---|---|---|---|---|---|---|
| `BE-ADMIN-001` | Admin user list/detail | CBL | `BE-AUTH-004`<br>`BE-USER-001` | P0 | SR,QAR | `GET /admin/users`, `/{userId}` admin-only, pagination/filter, no sensitive token/hash exposure. | Theo global DoD + acceptance. | TODO |
| `BE-ADMIN-002` | Manual lock/unlock user | CBL | `BE-ADMIN-001`<br>`BE-AUTH-003` | P0 | DBR,SR,QAR | `POST .../lock`, `/unlock`; semantics không nhầm với temporary brute-force lock; audit LOCK_USER/UNLOCK_USER. | Theo global DoD + acceptance. | TODO |
| `BE-ADMIN-003` | Admin vocabulary CRUD/state APIs | CBL | `BE-VOC-002` | P0 | DBR,SR,QAR | List/create/update/activate/deactivate đúng OpenAPI; duplicate/content validation; audit actions canonical. | Theo global DoD + acceptance. | TODO |
| `BE-ADMIN-004` | Admin topic mutation APIs | CBL | `BE-VOC-001` | P0 | DBR,SR,QAR | Create/update/deactivate; referential safety; audit. | Theo global DoD + acceptance. | TODO |
| `BE-ADMIN-005` | Admin quiz management APIs | CBL | `BE-QUIZ-004` | P0 | DBR,SR,QAR | Create/update/add/update question/publish/unpublish; publish validation; audit. | Theo global DoD + acceptance. | TODO |
| `BE-ADMIN-006` | Learning statistics API | CBL | `BE-PROG-004` | P1 | DBR,SR,QAR | `GET /admin/statistics/learning`; aggregate/date filters/performance sane. | Theo global DoD + acceptance. | TODO |
| `BE-ADMIN-007` | AI usage statistics API | CBL | `BE-AI-010` | P0 | DBR,SR,QAR | `GET /admin/statistics/ai-usage` canonical route; budget/cost metrics reconcile. | Theo global DoD + acceptance. | TODO |
| `BE-ADMIN-008` | Audit log service + list API | CBL | `BE-ADMIN-002`<br>`BE-ADMIN-003`<br>`BE-ADMIN-004`<br>`BE-ADMIN-005`<br>`BE-AI-008` | P0 | DBR,SR,QAR | `GET /admin/audit-logs`; action vocabulary canonical; actor/entity/details/correlation persisted for state mutations. | Theo global DoD + acceptance. | TODO |

## Admin Web

| ID | Task | Owner | Depends on | Pri | Review | Acceptance / evidence | Required tests | Status |
|---|---|---|---|---|---|---|---|---|
| `ADM-DASH-001` | Dashboard shell + statistics cards | AFL | `ADM-AUTH-001`<br>`BE-ADMIN-006`<br>`BE-ADMIN-007` | P1 | QAR | Dashboard matches Admin UI/UX, loading/error/empty; server data only. | Theo global DoD + acceptance. | TODO |
| `ADM-USER-001` | User list/detail/lock-unlock | AFL | `BE-ADMIN-002`<br>`ADM-FND-002` | P0 | SR,QAR | Pagination/search/detail/confirm lock-unlock; 403/409; audit-aware UX. | Theo global DoD + acceptance. | TODO |
| `ADM-VOC-001` | Vocabulary management UI | AFL | `BE-ADMIN-003` | P0 | QAR | List/create/edit/activate/deactivate; manual CEFR selection; AI CEFR Suggestion không active V1. | Theo global DoD + acceptance. | TODO |
| `ADM-TOP-001` | Topic management UI | AFL | `BE-ADMIN-004` | P0 | QAR | Create/edit/deactivate with confirmation and error states. | Theo global DoD + acceptance. | TODO |
| `ADM-QUIZ-001` | Quiz + question editor/publish UI | AFL | `BE-ADMIN-005` | P0 | QAR | Editor validation, publish/unpublish confirmations, error/status handling. | Theo global DoD + acceptance. | TODO |
| `ADM-AI-001` | AI content generate/list/detail/review UI | AFL | `BE-AI-008`<br>`BE-AI-004` | P0 | SR,QAR | Synchronous 201 generate with eventId; PENDING_REVIEW list/detail; approve/reject; reject note required. | Theo global DoD + acceptance. | TODO |
| `ADM-STAT-001` | Learning stats + AI usage pages | AFL | `BE-ADMIN-006`<br>`BE-ADMIN-007` | P1 | QAR | Canonical `/admin/statistics/ai-usage`; budget alerts; charts/tables accessible. | Theo global DoD + acceptance. | TODO |
| `ADM-AUDIT-001` | Audit log page/detail | AFL | `BE-ADMIN-008` | P1 | SR,QAR | List/filter/detail; reason/correlation shown when available; no secret data. | Theo global DoD + acceptance. | TODO |

## Admin

| ID | Task | Owner | Depends on | Pri | Review | Acceptance / evidence | Required tests | Status |
|---|---|---|---|---|---|---|---|---|
| `QA-ADMIN-001` | Admin RBAC/API/audit integration suite | QAR | `BE-ADMIN-008` | P0 | DBR,SR,QAR | USER cannot admin; admin CRUD/review transitions/audit actions/statistics routes exact. | Theo global DoD + acceptance. | TODO |
| `QA-ADMIN-002` | Admin Web component/E2E suite | QAR | `ADM-AUDIT-001`<br>`ADM-AI-001`<br>`ADM-QUIZ-001`<br>`ADM-VOC-001`<br>`ADM-USER-001` | P1 | SR,QAR | Critical happy/error/permission flows; AI reject reason persistence verified via backend. | Theo global DoD + acceptance. | TODO |

## Android Completion

| ID | Task | Owner | Depends on | Pri | Review | Acceptance / evidence | Required tests | Status |
|---|---|---|---|---|---|---|---|---|
| `AND-PROFILE-001` | Profile/goals/learning/notification settings UI | AFL | `AND-HOME-001`<br>`BE-USER-001`<br>`BE-GOAL-001`<br>`BE-NOTI-002` | P1 | QAR | Profile/timezone/goals/preferences edit and refresh; server authoritative. | Theo global DoD + acceptance. | TODO |
| `AND-UX-001` | Global loading/empty/error/offline UX | AFL | `AND-PROFILE-001` | P0 | QAR | Network-driven screens implement canonical states; offline mutations blocked; retry preserves eventId. | Theo global DoD + acceptance. | TODO |
| `AND-UX-002` | Accessibility + lifecycle/process-death hardening | AFL | `AND-UX-001` | P1 | QAR | Touch targets/content descriptions/text scaling; timers/audio/request cancellation; state restoration avoids duplicate mutation. | Theo global DoD + acceptance. | TODO |
| `AND-SEC-001` | Mobile security/logging/network config | AFL | `AND-UX-002` | P0 | SR,QAR | Secure token storage, network security config, no PII/token logs, release logging disabled as required. | Theo global DoD + acceptance. | TODO |
| `AND-TEST-001` | Android unit/UI/contract regression suite | QAR | `AND-SEC-001`<br>`AND-GAME-001`<br>`AND-AI-001`<br>`AND-NOTI-002`<br>`AND-PROG-001` | P0 | SR,QAR | Auth/eventId/learning/quiz/assessment/home/progress/notification/AI UI tests; API DTO contract nullable/error codes. | Theo global DoD + acceptance. | TODO |

## End-to-End

| ID | Task | Owner | Depends on | Pri | Review | Acceptance / evidence | Required tests | Status |
|---|---|---|---|---|---|---|---|---|
| `QA-E2E-006` | Full learner MVP journey | QAR | `AND-TEST-001`<br>`QA-E2E-003`<br>`QA-E2E-004`<br>`QA-E2E-005` | P0 | AR,DBR,SR,QAR | Register→onboard→assessment→Daily Plan→flashcard/review→quiz→progress→gamification→AI→notification completes on integrated environment. | Theo global DoD + acceptance. | TODO |
| `QA-E2E-007` | Full admin MVP journey | QAR | `QA-ADMIN-002` | P0 | AR,DBR,SR,QAR | Admin auth→manage vocab/topic/quiz→generate/review AI→stats→audit works end-to-end. | Theo global DoD + acceptance. | TODO |

# M9 — Hardening & Release Candidate

## Hardening

| ID | Task | Owner | Depends on | Pri | Review | Acceptance / evidence | Required tests | Status |
|---|---|---|---|---|---|---|---|---|
| `QA-RC-001` | Fresh-database migration/seed verification | QAR | `DB-FND-004`<br>`BE-ADMIN-008` | P0 | DBR,QAR | Blank PostgreSQL → all migrations → 34 tables + seeds; rollback/restore procedure tested where applicable. | Theo global DoD + acceptance. | TODO |
| `QA-RC-002` | API/OpenAPI coverage all 76 operations | QAR | `QA-E2E-006`<br>`QA-E2E-007` | P0 | AR,QAR | 76/76 operations have controller/contract tests or intentional coverage record; no extra/missing path/method. | Theo global DoD + acceptance. | TODO |
| `QA-RC-003` | Full idempotency/concurrency suite | QAR | `QA-RC-002` | P0 | DBR,SR,QAR | Required eventId endpoints replay/reuse/concurrent duplicate; learning/streak optimistic conflicts; no accidental 500. | Theo global DoD + acceptance. | TODO |
| `QA-RC-004` | Security penetration-style regression | QAR | `QA-RC-002` | P0 | SR,QAR | Authz/ownership/JWT expiry/refresh revoke/brute force/CORS/input abuse/admin/AI/device token privacy. | Theo global DoD + acceptance. | TODO |
| `QA-RC-005` | Performance/load test | QAR | `QA-RC-002` | P1 | AR,DBR,QAR | Normal GET p50<300ms, p95<800ms excluding external AI/cold infra; load review/attempt/daily plan/admin list; bottlenecks documented. | Theo global DoD + acceptance. | TODO |
| `QA-RC-006` | Dependency failure/resilience suite | QAR | `BE-AI-010`<br>`BE-NOTI-005`<br>`BE-VOC-003` | P1 | AR,SR,QAR | LLM/FCM/object storage/Redis(if enabled) failures degrade per architecture; core learning remains usable where required. | Theo global DoD + acceptance. | TODO |

## Operations

| ID | Task | Owner | Depends on | Pri | Review | Acceptance / evidence | Required tests | Status |
|---|---|---|---|---|---|---|---|---|
| `OPS-RC-001` | Integrated Docker Compose/deployment package | CBL | `QA-RC-001`<br>`ADM-AUDIT-001` | P1 | AR,SR,QAR | backend/postgres/admin-web (+ optional redis) start reproducibly; external provider config documented. | Theo global DoD + acceptance. | TODO |
| `OPS-RC-002` | Health/observability/alert baseline | CBL | `BE-FND-013`<br>`BE-FND-006` | P1 | AR,SR,QAR | Health/readiness, structured logs, correlation, key error/AI cost metrics; secrets redacted. | Theo global DoD + acceptance. | TODO |
| `OPS-RC-003` | Backup/restore + retention jobs | CBL | `OPS-RC-001`<br>`BE-FND-012` | P1 | DBR,SR,QAR | PostgreSQL backup/restore documented/tested; idempotency >30d cleanup; token/content retention per baseline. | Theo global DoD + acceptance. | TODO |

## Documentation

| ID | Task | Owner | Depends on | Pri | Review | Acceptance / evidence | Required tests | Status |
|---|---|---|---|---|---|---|---|---|
| `DOC-RC-001` | Implementation README/runbook cập nhật | INT | `OPS-RC-001`<br>`AND-TEST-001`<br>`QA-ADMIN-002` | P1 | AR,QAR | Setup/run/test/env/migration/provider/mock instructions đủ cho dev mới; no stale baseline version. | Theo global DoD + acceptance. | TODO |
| `DOC-RC-002` | Final contract/doc impact sync | INT | `QA-RC-002`<br>`DOC-RC-001` | P0 | AR,DBR,SR,QAR | Mọi implementation deviation đã được approved và docs/OpenAPI/client cập nhật cùng change; baseline audit PASS. | Theo global DoD + acceptance. | TODO |

## Review Gate

| ID | Task | Owner | Depends on | Pri | Review | Acceptance / evidence | Required tests | Status |
|---|---|---|---|---|---|---|---|---|
| `REV-ARCH-001` | Architecture Reviewer final pass | AR | `DOC-RC-002`<br>`QA-RC-005`<br>`QA-RC-006` | P0 | AR | Module boundaries, sync AI, Daily Plan snapshot, provider abstraction, scope V1 pass; no critical open finding. | Theo global DoD + acceptance. | TODO |
| `REV-DB-001` | Database Reviewer final pass | DBR | `DOC-RC-002`<br>`QA-RC-003`<br>`QA-RC-001` | P0 | DBR | Schema/migrations/indexes/constraints/idempotency/locks/queries pass; no critical open finding. | Theo global DoD + acceptance. | TODO |
| `REV-SEC-001` | Security Reviewer final pass | SR | `DOC-RC-002`<br>`QA-RC-004`<br>`QA-RC-006` | P0 | SR | Auth/authz/secrets/idempotency/AI/push/admin audit pass; no critical open finding. | Theo global DoD + acceptance. | TODO |
| `REV-QA-001` | QA Reviewer final pass | QAR | `REV-ARCH-001`<br>`REV-DB-001`<br>`REV-SEC-001`<br>`QA-E2E-006`<br>`QA-E2E-007` | P0 | QAR | MVP acceptance criteria + regression + API contract pass; no P0/P1 release blocker. | Theo global DoD + acceptance. | TODO |

## Release

| ID | Task | Owner | Depends on | Pri | Review | Acceptance / evidence | Required tests | Status |
|---|---|---|---|---|---|---|---|---|
| `REL-001` | Build V1 Release Candidate | INT | `REV-QA-001`<br>`OPS-RC-003` | P0 | AR,DBR,SR,QAR | Versioned RC artifacts produced; release notes list known limitations; V2 features disabled/not present. | Theo global DoD + acceptance. | TODO |
| `REL-002` | Tag release candidate + freeze | INT | `REL-001` | P0 | QAR | RC tag created; only approved blocker fixes allowed; baseline audit and CI PASS on tagged commit. | Theo global DoD + acceptance. | TODO |

## Compatibility

| ID | Task | Owner | Depends on | Pri | Review | Acceptance / evidence | Required tests | Status |
|---|---|---|---|---|---|---|---|---|
| `FLT-CONTRACT-001` | Flutter V2 contract compatibility review only | AFL | `QA-RC-002`<br>`DOC-RC-002` | P2 | AR,QAR | Không build Flutter V1; verify Flutter Technical v1.1 models can represent API v1.4: eventId body, nullable DailyPlanItem.vocabularyId, errors, notifications, sync AI. | Theo global DoD + acceptance. | TODO |

# 4. OpenAPI operation → primary task coverage

Coverage requirement: **76/76 operations mapped**.

| Tag | Method | Path | operationId | Primary task |
|---|---|---|---|---|
| Auth | `POST` | `/auth/register` | `register` | `BE-AUTH-002` |
| Auth | `POST` | `/auth/login` | `login` | `BE-AUTH-003` |
| Auth | `POST` | `/auth/refresh` | `refreshAccessToken` | `BE-AUTH-005` |
| Auth | `POST` | `/auth/logout` | `logout` | `BE-AUTH-006` |
| Auth | `POST` | `/auth/google` | `googleLogin` | `BE-AUTH-007` |
| Users | `GET` | `/users/me` | `getCurrentUser` | `BE-USER-001` |
| Users | `GET` | `/users/me/profile` | `getCurrentProfile` | `BE-USER-001` |
| Users | `PUT` | `/users/me/profile` | `updateCurrentProfile` | `BE-USER-001` |
| Goals | `GET` | `/goals` | `listGoals` | `BE-GOAL-001` |
| Goals | `GET` | `/users/me/goals` | `getMyGoals` | `BE-GOAL-001` |
| Goals | `PUT` | `/users/me/goals` | `setMyGoals` | `BE-GOAL-001` |
| CEFR | `GET` | `/cefr-levels` | `listCefrLevels` | `BE-CEFR-001` |
| Assessments | `POST` | `/assessments` | `startAssessment` | `BE-ASMT-003` |
| Assessments | `GET` | `/assessments/{assessmentId}` | `getAssessment` | `BE-ASMT-003` |
| Assessments | `GET` | `/assessments/{assessmentId}/next-question` | `getNextAssessmentQuestion` | `BE-ASMT-003` |
| Assessments | `POST` | `/assessments/{assessmentId}/answers` | `submitAssessmentAnswer` | `BE-ASMT-004` |
| Topics | `GET` | `/topics` | `listTopics` | `BE-VOC-001` |
| Topics | `GET` | `/topics/{topicId}` | `getTopic` | `BE-VOC-001` |
| Vocabulary | `GET` | `/vocabulary` | `listVocabulary` | `BE-VOC-002` |
| Vocabulary | `GET` | `/vocabulary/{vocabularyId}` | `getVocabulary` | `BE-VOC-002` |
| Vocabulary | `GET` | `/vocabulary/{vocabularyId}/examples` | `getVocabularyExamples` | `BE-VOC-002` |
| Learning | `POST` | `/learning/sessions` | `startLearningSession` | `BE-LEARN-001` |
| Learning | `GET` | `/learning/sessions/{sessionId}` | `getLearningSession` | `BE-LEARN-001` |
| Learning | `POST` | `/learning/sessions/{sessionId}/complete` | `completeLearningSession` | `BE-LEARN-003` |
| Learning | `GET` | `/learning/today` | `getTodayPlan` | `BE-PERS-008` |
| Learning | `GET` | `/learning/today/items` | `getTodayPlanItems` | `BE-PERS-008` |
| Learning | `POST` | `/learning/today/complete` | `completeTodayPlan` | `BE-PERS-009` |
| Learning | `GET` | `/learning/reviews` | `getDueReviews` | `BE-LEARN-004` |
| Learning | `POST` | `/learning/attempts` | `submitLearningAttempt` | `BE-LEARN-002` |
| Progress | `GET` | `/vocabulary/{vocabularyId}/progress` | `getVocabularyProgress` | `BE-PROG-001` |
| Progress | `GET` | `/progress` | `getProgressOverview` | `BE-PROG-002` |
| Progress | `GET` | `/progress/weak-words` | `getWeakWords` | `BE-PROG-003` |
| Progress | `GET` | `/progress/trend` | `getProgressTrend` | `BE-PROG-004` |
| Quiz | `GET` | `/quizzes` | `listQuizzes` | `BE-QUIZ-001` |
| Quiz | `GET` | `/quizzes/{quizId}` | `getQuiz` | `BE-QUIZ-001` |
| Quiz | `POST` | `/quizzes/{quizId}/attempts` | `startQuizAttempt` | `BE-QUIZ-002` |
| Quiz | `GET` | `/quiz-attempts/{attemptId}` | `getQuizAttempt` | `BE-QUIZ-002` |
| Quiz | `POST` | `/quiz-attempts/{attemptId}/answers` | `submitQuizAnswer` | `BE-QUIZ-003` |
| Quiz | `POST` | `/quiz-attempts/{attemptId}/complete` | `completeQuizAttempt` | `BE-QUIZ-004` |
| Gamification | `GET` | `/gamification/streak` | `getStreak` | `BE-GAME-005` |
| Gamification | `GET` | `/gamification/xp` | `getXp` | `BE-GAME-004` |
| Gamification | `GET` | `/gamification/xp/history` | `getXpHistory` | `BE-GAME-004` |
| Gamification | `GET` | `/gamification/badges` | `getBadges` | `BE-GAME-006` |
| Gamification | `GET` | `/gamification/leaderboard` | `getLeaderboard` | `BE-GAME-007` |
| Notifications | `GET` | `/notifications` | `listNotifications` | `BE-NOTI-005` |
| Learning | `POST` | `/learning/personalized-exercise` | `generatePersonalizedExercise` | `BE-AI-009` |
| Admin Users | `GET` | `/admin/users` | `adminListUsers` | `BE-ADMIN-001` |
| Admin Users | `GET` | `/admin/users/{userId}` | `adminGetUser` | `BE-ADMIN-001` |
| Admin Users | `POST` | `/admin/users/{userId}/lock` | `adminLockUser` | `BE-ADMIN-002` |
| Admin Users | `POST` | `/admin/users/{userId}/unlock` | `adminUnlockUser` | `BE-ADMIN-002` |
| Admin Vocabulary | `GET` | `/admin/vocabulary` | `adminListVocabulary` | `BE-ADMIN-003` |
| Admin Vocabulary | `POST` | `/admin/vocabulary` | `adminCreateVocabulary` | `BE-ADMIN-003` |
| Admin Vocabulary | `PUT` | `/admin/vocabulary/{vocabularyId}` | `adminUpdateVocabulary` | `BE-ADMIN-003` |
| Admin Vocabulary | `POST` | `/admin/vocabulary/{vocabularyId}/deactivate` | `adminDeactivateVocabulary` | `BE-ADMIN-003` |
| Admin Vocabulary | `POST` | `/admin/vocabulary/{vocabularyId}/activate` | `adminActivateVocabulary` | `BE-ADMIN-003` |
| Admin Topics | `POST` | `/admin/topics` | `adminCreateTopic` | `BE-ADMIN-004` |
| Admin Topics | `PUT` | `/admin/topics/{topicId}` | `adminUpdateTopic` | `BE-ADMIN-004` |
| Admin Topics | `POST` | `/admin/topics/{topicId}/deactivate` | `adminDeactivateTopic` | `BE-ADMIN-004` |
| Admin Quiz | `POST` | `/admin/quizzes` | `adminCreateQuiz` | `BE-ADMIN-005` |
| Admin Quiz | `PUT` | `/admin/quizzes/{quizId}` | `adminUpdateQuiz` | `BE-ADMIN-005` |
| Admin Quiz | `POST` | `/admin/quizzes/{quizId}/questions` | `adminAddQuizQuestion` | `BE-ADMIN-005` |
| Admin Quiz | `PUT` | `/admin/quiz-questions/{questionId}` | `adminUpdateQuizQuestion` | `BE-ADMIN-005` |
| Admin Quiz | `POST` | `/admin/quizzes/{quizId}/publish` | `adminPublishQuiz` | `BE-ADMIN-005` |
| Admin Quiz | `POST` | `/admin/quizzes/{quizId}/unpublish` | `adminUnpublishQuiz` | `BE-ADMIN-005` |
| Admin AI Content | `POST` | `/admin/ai-content/generate` | `adminGenerateAiContent` | `BE-AI-004` |
| Admin AI Content | `GET` | `/admin/ai-content` | `adminListAiContent` | `BE-AI-006` |
| Admin AI Content | `GET` | `/admin/ai-content/{contentId}` | `adminGetAiContent` | `BE-AI-006` |
| Admin AI Content | `POST` | `/admin/ai-content/{contentId}/approve` | `adminApproveAiContent` | `BE-AI-007` |
| Admin AI Content | `POST` | `/admin/ai-content/{contentId}/reject` | `adminRejectAiContent` | `BE-AI-008` |
| Admin Statistics | `GET` | `/admin/statistics/learning` | `adminLearningStatistics` | `BE-ADMIN-006` |
| Admin Statistics | `GET` | `/admin/statistics/ai-usage` | `adminAiUsageStatistics` | `BE-ADMIN-007` |
| Admin Audit | `GET` | `/admin/audit-logs` | `adminListAuditLogs` | `BE-ADMIN-008` |
| Devices | `PUT` | `/devices/{installationId}/push-token` | `upsertPushToken` | `BE-NOTI-001` |
| Devices | `DELETE` | `/devices/{installationId}` | `deactivateDevice` | `BE-NOTI-001` |
| Notifications | `GET` | `/notification-preferences` | `getNotificationPreferences` | `BE-NOTI-002` |
| Notifications | `PUT` | `/notification-preferences` | `updateNotificationPreferences` | `BE-NOTI-002` |

# 5. Database table → primary task coverage

Coverage requirement: **34/34 canonical tables mapped**.

| Table | Primary task |
|---|---|
| `users` | `BE-AUTH-001` |
| `refresh_tokens` | `BE-AUTH-001` |
| `user_profiles` | `BE-USER-001` |
| `goals` | `BE-GOAL-001` |
| `user_goals` | `BE-GOAL-001` |
| `goal_topics` | `DB-CONTENT-002` |
| `cefr_levels` | `DB-FND-004` |
| `user_level_assessments` | `BE-ASMT-002` |
| `assessment_items` | `BE-ASMT-002` |
| `topics` | `BE-VOC-001` |
| `vocabulary` | `BE-VOC-002` |
| `vocabulary_topics` | `BE-VOC-002` |
| `vocabulary_examples` | `BE-VOC-002` |
| `user_vocabulary_progress` | `BE-LEARN-002` |
| `learning_sessions` | `BE-LEARN-001` |
| `session_attempts` | `BE-LEARN-002` |
| `daily_plans` | `BE-PERS-006` |
| `daily_plan_items` | `BE-PERS-006` |
| `quizzes` | `BE-QUIZ-001` |
| `quiz_questions` | `BE-QUIZ-001` |
| `quiz_attempts` | `BE-QUIZ-002` |
| `quiz_attempt_answers` | `BE-QUIZ-003` |
| `streaks` | `BE-GAME-002` |
| `xp_logs` | `BE-GAME-001` |
| `badges` | `DB-FND-004` |
| `user_badges` | `BE-GAME-003` |
| `notifications` | `BE-NOTI-005` |
| `user_devices` | `BE-NOTI-001` |
| `notification_preferences` | `BE-NOTI-002` |
| `ai_generated_content` | `BE-AI-004` |
| `ai_requests` | `BE-AI-003` |
| `ai_usage_daily` | `BE-AI-010` |
| `admin_audit_logs` | `BE-ADMIN-008` |
| `idempotency_keys` | `BE-FND-008` |

# 6. Global Definition of Done

Mỗi executable task chỉ chuyển `DONE` khi:

```text
[ ] dependency DONE
[ ] source-of-truth docs checked
[ ] implementation satisfies acceptance evidence
[ ] unit/integration/client tests as applicable
[ ] auth/authz/validation/error paths checked
[ ] idempotency/concurrency checked where mutation/state is affected
[ ] API/OpenAPI synced where API is affected
[ ] Flyway/schema synced where DB is affected
[ ] Android/Admin model/UI synced where client is affected
[ ] no V2 scope introduced
[ ] required reviewer(s) PASS
[ ] CI PASS
[ ] baseline_audit PASS
```

Ngoại lệ bootstrap có thời hạn cho checklist trên:

- Trước khi `CI-FND-001` ở trạng thái `DONE` và đã được merge vào `main`, chỉ đúng các task `GOV-008`, `BE-FND-001`, `BE-FND-002`, `DB-FND-001`, `DB-FND-002`, `BE-FND-004`, `BE-FND-005`, `BE-FND-007`, `QA-FND-001`, `QA-FND-002` được phép dùng CI status `PRE_CI_BOOTSTRAP_NA` thay cho `CI PASS`.
- `PRE_CI_BOOTSTRAP_NA` chỉ hợp lệ khi tất cả dependency đã `DONE`; acceptance criteria đã thỏa mãn; local/unit/integration/task tests bắt buộc đã `PASS` khi áp dụng; reviewer bắt buộc đã `PASS`; `baseline_audit` đã `PASS`; build/static/git/diff validations áp dụng đã `PASS`; PR ghi rõ eligible Task ID, lý do và bằng chứng local; và không có CI check hiện hữu nào đang fail.
- CI check đang fail không bao giờ được waive bằng `PRE_CI_BOOTSTRAP_NA`. `NOT_APPLICABLE` không được dùng thay cho ngoại lệ bootstrap này.
- `CI-FND-001` không được dùng `PRE_CI_BOOTSTRAP_NA` cho final gate. Pipeline thực tế phải `PASS` trên PR `CI-FND-001` trước khi task chuyển `DONE`.
- Ngay sau khi `CI-FND-001` `DONE` và được merge vào `main`, `PRE_CI_BOOTSTRAP_NA` tự động hết hiệu lực; Global Definition of Done trở lại yêu cầu `CI PASS` thực tế cho mọi executable task tiếp theo trước `DONE`/merge.
- Gate thoát M1 luôn yêu cầu `CI PASS` thực tế.

# 7. Milestone gate checklist

## M0 — Execution Governance

**Exit:** GOV-001..GOV-007 hoàn tất; baseline audit PASS; tag baseline tồn tại.

P0 tasks:

```text
[ ] GOV-001
[ ] GOV-002
[ ] GOV-003
[ ] GOV-004
[ ] GOV-006
```

## M1 — Foundation Ready

**Exit:** Fresh DB migrate; CI PASS thực tế; idempotency/locking/error/token foundations sẵn; clients build. `PRE_CI_BOOTSTRAP_NA` không thỏa gate thoát M1.

P0 tasks:

```text
[ ] GOV-008
[ ] BE-FND-001
[ ] BE-FND-002
[ ] BE-FND-003
[ ] DB-FND-001
[ ] DB-FND-002
[ ] DB-FND-003
[ ] DB-FND-004
[ ] BE-FND-004
[ ] BE-FND-005
[ ] BE-FND-007
[ ] BE-FND-008
[ ] BE-FND-009
[ ] BE-FND-010
[ ] QA-FND-001
[ ] QA-FND-002
[ ] CI-FND-001
[ ] SEC-FND-001
[ ] ADM-FND-001
[ ] ADM-FND-002
[ ] AND-FND-001
[ ] AND-FND-002
```

## M2 — Identity & Catalog

**Exit:** Auth lifecycle pass; catalog APIs pass; >=30 usable vocabulary/CEFR.

P0 tasks:

```text
[ ] BE-AUTH-001
[ ] BE-AUTH-002
[ ] BE-AUTH-003
[ ] BE-AUTH-004
[ ] BE-AUTH-005
[ ] BE-AUTH-006
[ ] BE-USER-001
[ ] BE-GOAL-001
[ ] BE-CEFR-001
[ ] DB-CONTENT-001
[ ] DB-CONTENT-002
[ ] BE-VOC-001
[ ] BE-VOC-002
[ ] QA-IDCAT-001
[ ] AND-AUTH-001
[ ] AND-AUTH-002
[ ] AND-ONB-001
[ ] ADM-AUTH-001
```

## M3 — First Vertical Slice — Learning/SRS

**Exit:** QA-E2E-001 PASS, gồm replay/reuse/concurrency.

P0 tasks:

```text
[ ] BE-SRS-001
[ ] BE-SRS-002
[ ] QA-SRS-001
[ ] BE-GAME-001
[ ] BE-GAME-002
[ ] BE-LEARN-001
[ ] BE-LEARN-002
[ ] BE-LEARN-003
[ ] BE-LEARN-004
[ ] BE-PROG-001
[ ] QA-LEARN-001
[ ] QA-LEARN-002
[ ] AND-LEARN-001
[ ] AND-LEARN-002
[ ] AND-LEARN-003
[ ] QA-E2E-001
```

## M4 — Assessment & Quiz

**Exit:** Assessment/quiz algorithm + Android flow PASS.

P0 tasks:

```text
[ ] BE-ASMT-001
[ ] BE-ASMT-002
[ ] BE-ASMT-003
[ ] BE-ASMT-004
[ ] QA-ASMT-001
[ ] AND-ASMT-001
[ ] AND-ASMT-002
[ ] BE-QUIZ-001
[ ] BE-QUIZ-002
[ ] BE-QUIZ-003
[ ] BE-QUIZ-004
[ ] QA-QUIZ-001
[ ] AND-QUIZ-001
[ ] QA-E2E-002
```

## M5 — Personalization & Daily Plan

**Exit:** QA-E2E-003 PASS; same-day snapshot stable; Daily Plan XP idempotent.

P0 tasks:

```text
[ ] BE-PERS-001
[ ] BE-PERS-002
[ ] BE-PERS-003
[ ] BE-PERS-004
[ ] BE-PERS-005
[ ] BE-PERS-006
[ ] BE-PERS-007
[ ] BE-PERS-008
[ ] BE-PERS-009
[ ] BE-PROG-002
[ ] BE-PROG-003
[ ] QA-PERS-001
[ ] QA-PERS-002
[ ] AND-HOME-001
[ ] AND-HOME-002
[ ] AND-PROG-001
[ ] QA-E2E-003
```

## M6 — Gamification Complete

**Exit:** QA-E2E-004 và gamification suite PASS.

P0 tasks:

```text
[ ] BE-GAME-003
[ ] BE-GAME-004
[ ] BE-GAME-005
[ ] BE-GAME-006
[ ] QA-GAME-001
```

## M7 — AI & Notifications

**Exit:** AI cost/idempotency + notification timezone/dedupe tests PASS.

P0 tasks:

```text
[ ] BE-AI-001
[ ] BE-AI-002
[ ] BE-AI-003
[ ] BE-AI-004
[ ] BE-AI-006
[ ] BE-AI-007
[ ] BE-AI-008
[ ] BE-AI-009
[ ] QA-AI-001
[ ] BE-NOTI-001
[ ] BE-NOTI-002
[ ] BE-NOTI-003
[ ] BE-NOTI-004
[ ] BE-NOTI-005
[ ] QA-NOTI-001
[ ] AND-NOTI-001
```

## M8 — Client/Admin Completion

**Exit:** QA-E2E-006 và QA-E2E-007 PASS.

P0 tasks:

```text
[ ] BE-ADMIN-001
[ ] BE-ADMIN-002
[ ] BE-ADMIN-003
[ ] BE-ADMIN-004
[ ] BE-ADMIN-005
[ ] BE-ADMIN-007
[ ] BE-ADMIN-008
[ ] ADM-USER-001
[ ] ADM-VOC-001
[ ] ADM-TOP-001
[ ] ADM-QUIZ-001
[ ] ADM-AI-001
[ ] QA-ADMIN-001
[ ] AND-UX-001
[ ] AND-SEC-001
[ ] AND-TEST-001
[ ] QA-E2E-006
[ ] QA-E2E-007
```

## M9 — Hardening & Release Candidate

**Exit:** Architecture/DB/Security/QA final pass; REL-001/REL-002 DONE.

P0 tasks:

```text
[ ] QA-RC-001
[ ] QA-RC-002
[ ] QA-RC-003
[ ] QA-RC-004
[ ] DOC-RC-002
[ ] REV-ARCH-001
[ ] REV-DB-001
[ ] REV-SEC-001
[ ] REV-QA-001
[ ] REL-001
[ ] REL-002
```

# 8. Deferred backlog — NOT executable in V1

| ID | Item | Horizon | Rule |
|---|---|---|---|
| `V2-FLT-001` | Flutter application implementation | V2 | Reuse approved API contract; no V1 implementation. |
| `V2-ML-001` | ML forgetting prediction | V2 | Train/evaluate model only after V1 data readiness. |
| `V2-ML-002` | ML recommendation/difficulty | V2 | Replace/augment deterministic V1 only via explicit algorithm-version migration. |
| `V2-LRN-001` | Speaking module | Future | Outside V1. |
| `V2-LRN-002` | Writing module | Future | Outside V1. |
| `V2-LRN-003` | Listening module | Future | Outside V1. |
| `V2-LRN-004` | Reading course | Future | Outside V1. |
| `V2-AI-001` | Conversational AI Tutor | Future | Outside V1. |
| `V2-OFF-001` | Offline learning sync/conflict merge | Future | V1 remains online-first. |
| `V2-AI-002` | Admin AI CEFR Suggestion | Future | V1 manual CEFR selection. |
| `V2-AI-003` | Client-facing async AI job lifecycle | Future | V1 AI endpoints synchronous. |
| `V2-NOTI-001` | AI notification timing optimization | Future | V1 uses fixed deterministic schedules/preferences. |

# 9. Backlog integrity

- Unique executable task IDs: **176**
- Missing dependency references: **0**
- Dependency cycles: **0**
- OpenAPI operation mapping: **76/76**
- Database table mapping: **34/34**
