# QA-LEARN-001 — Kiểm thử tích hợp learning transaction

Ngày: 2026-10-17. Owner: QAR. Task M3/P0, dependencies BE-LEARN-004 và
BE-PROG-001 DONE. Required independent reviewers: AR, DBR, SR, QAR; implementer
không dùng cùng session để thỏa QAR gate.

Phạm vi là backend integration trên PostgreSQL 16/Flyway thật qua MockMvc.
Không sửa product, schema, migration, API/OpenAPI, dependency hoặc clients.
QA-LEARN-002 sở hữu ma trận concurrency/idempotency chuyên sâu; QA-E2E-001 sở hữu
luồng Android end-to-end. Suite này kiểm chứng kết nối giữa các module hiện có.

## Nguồn chuẩn

- PROJECT_RULES §11–18, §59.2: transaction, correctness, idempotency và lifecycle.
- MASTER_BACKLOG, exact row QA-LEARN-001: happy/error/rollback, quality DB check,
  XP/streak effects, due review và session completion.
- SRS v1.2, Database v1.6 §8/10, Architecture v1.3: backend authority, integrity,
  atomic effects và module boundaries.
- AI Personalization v1.3 §7 và approved BE-LEARN-002 additions: SRS/aggregate.
- API/OpenAPI v1.4 và approved BE-GAME-001/002, BE-LEARN-002/003/004,
  BE-PROG-001: XP source identity, local date, response/guards và read-only state.
- Technical v1.2, Backend v1.3: PostgreSQL integration và deterministic Clock.

## Ma trận hiện thực

File: `backend/src/test/java/com/example/englishaicoach/learning/LearningVerticalSliceIntegrationTests.java`.

| Ca | Bằng chứng hành vi |
|---|---|
| committedApiJourneyConnectsProgressDueReviewCompletionAndLedgerSources | Register/profile qua API; start, correct/incorrect attempts, GET progress, due cutoff/order, complete/get/complete mới, XP source; ngày sau REVIEW đẩy đúng một từ khỏi queue. |
| eachQualityReachesDatabaseAndReadApisWithBackendCorrectness | q=0..5 trên sáu từ mới: HTTP correctness, EF, interval, repetition/counters, nextReviewAt, DB quality/correctness và GET progress; completion accuracy50%, correct XP15 + streak10 + completion10. |
| wrongAnswersAcrossLocalMidnightMaintainStreakWithoutCorrectAnswerXp | 23:59:30→00:00:30 Asia/Ho_Chi_Minh vẫn cùng ngày UTC; incorrect đủ streak, source UUID theo local date, không correct XP, completion không sửa streak. |
| rejectedRequestsPreserveBothUsersAndExplicitCorrectionCanContinue | Forged isCorrect400, foreign session404, empty completion400 không mutation; caller khác chỉ thấy NEW/empty due; closed attempt400 giữ nguyên state. |
| missingProfileFailureRollsBackAndSameOperationSucceedsAfterProfileSave | Missing timezone invariant500 rollback mọi learning effects/claim; lưu profile qua API rồi retry đúng request/event thành công. |
| lateAttemptSnapshotFailureRestoresPreviouslyCommittedStateAndRetrySucceeds | Bắt đầu từ progress/XP/streak đã commit; PostgreSQL trigger scoped event làm snapshot UPDATE fail sau next-day effects; so sánh toàn row/timestamp/version/ledger/session/claim, retry cùng event sau bỏ trigger. |
| lateCompletionSnapshotFailurePreservesAcceptedAttemptAndRetryClosesOnce | Incorrect attempt thật đã commit; snapshot completion failure rollback closure/award/claim, giữ attempt/streak; retry rồi replay không award thêm. |
| postgresRejectsOutOfRangeNullAndInconsistentQualityWithoutHttpValidation | Direct SQL q=-1/6/null, q2true/q3false bị PostgreSQL23514/23502; statement riêng không làm failed transaction tiếp tục; valid HTTP q3 sau đó thành công. |

Không outer `@Transactional`: mỗi request commit thật, request sau đọc committed
state. Mutable Clock chỉ trong test configuration, reset trước mỗi test; không
dùng sleep hoặc Clock hệ điều hành. JWT dùng issuer backend để thích ứng khi
Clock test chuyển ngày; account và profile được tạo qua API thật.

Fixture UUID/email riêng; không TRUNCATE hoặc sửa seed vocabulary. Cleanup xóa
chỉ rows của owned user theo thứ tự FK, assert user đã xóa. Trigger fault có tên
riêng, điều kiện exact event UUID, bỏ trong finally; không dùng mock product
để tạo lỗi. Suite chạy tuần tự theo cấu hình Surefire hiện hành. Chỉ ephemeral
Testcontainers database nhận trigger; không migration/schema triển khai.

## Lệnh xác thực

JDK21 qua process-only JAVA_HOME. Process-only MAVEN_OPTS=-Xmx384m;
fork `-DargLine="-Xmx512m -XX:ReservedCodeCacheSize=128m"`, context cache4.
Không đổi POM, instrumentation, cấu hình máy/proxy hoặc skip tests.

```powershell
.\backend\mvnw.cmd --no-transfer-progress -f backend/pom.xml `
  '-DargLine=-Xmx512m -XX:ReservedCodeCacheSize=128m' `
  '-Dspring.test.context.cache.maxSize=4' `
  '-Dtest=LearningVerticalSliceIntegrationTests' test
.\backend\mvnw.cmd --no-transfer-progress -f backend/pom.xml `
  '-DargLine=-Xmx512m -XX:ReservedCodeCacheSize=128m' `
  '-Dspring.test.context.cache.maxSize=4' clean verify
python tools/baseline_audit.py
python tools/ci_workflow_audit.py
python tools/secret_audit.py
python -m unittest discover -s tools -p 'test_*.py'
```

Kết quả thực chạy và chronology được ghi trong EXECUTION_LOG. Tài liệu này không
tự phát hành reviewer PASS hoặc CI PASS. Sau TEST task giữ IN_PROGRESS để review
độc lập; publication thuộc Git workflow sau closure gates.

## Kết quả TEST của implementer

Focused-final8/8 PASS, zero failure/error/skip, BUILD SUCCESS37.033s; full clean
verify403/403 PASS, zero failure/error/skip, BUILD SUCCESS04:11min,69freshXML.
Fullrun chạy lại suite8/8 trên fresh database/context sau focused; cleanup được
assert trong từng ca. Logs TEMP/QA-LEARN-001-focused-final.log và
TEMP/QA-LEARN-001-full.log. Initial focused8FAIL36.455s do thiếu test refresh
expiration config được giữ trong EXECUTION_LOG, sửa test property7d rồi rerun;
không product fix hoặc reviewer finding. Baseline/CIworkflow/secret auditsPASS,
Python46/46PASS0.724s, pycompile/diff/newfile/scope/generated/tagsPASS.

Task còn IN_PROGRESS; bốn reviewer gates cần report độc lập trước finalize.

## Closure evidence độc lập

Task đã DONE sau independent AR/DBR/SR/QAR initial PASS/NONE; không reviewer
finding, unresolvedNONE. QAR /root/qa_learn001_review độc lập với implementer
/root/qa_learn001_impl, tự chạy8/8PASS31.206s; log
TEMP/QA-LEARN-001-qa-independent.log. Finalizer đồng bộ reports do orchestrator
chuyển, không self-review. Full403/403 là owner evidence, independent run chỉ8/8.
Chi tiết closure gates, admission CI, audit và chronology giữ trong EXECUTION_LOG.
Publication và exact-SHA Required CI do Git workflow xác minh sau finalization.
