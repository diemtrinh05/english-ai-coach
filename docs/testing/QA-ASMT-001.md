# QA-ASMT-001 — Ma trận kiểm thử assessment

**Task:** QA-ASMT-001 · M4/P0 · Owner QAR

**Baseline kiểm chứng:** `assessment-block-v1` và các clarifications BE-ASMT-001/003/004.

**Scope:** test và evidence; không đổi production code, API/OpenAPI, schema hoặc client.

## Nguồn và oracle

SRS v1.2 và AI Personalization v1.3 khóa A1 start, block4, promote>=3,
demote<=1, hold2, min20/max50, stable2 và clamp A1/C2. Các quyết định owner
đã tích hợp xác định raw persisted options, guard precedence, atomic answer/next,
score HALF_UP và profile existing-only. Database v1.6 §6.2/6.3 khóa aggregate/items;
API/OpenAPI v1.4 và Backend v1.3 khóa transaction, snapshot và replay.

Test QA không gọi calculator/selector production để tính expected outcome của
integration test. Correct/wrong answer được chọn từ options snapshot của câu đã
persist; expected CEFR, counters, stable, score và stopping point là bảng literal.
Pure matrix gọi calculator như system under test nhưng oracle là bảng literal
6×5 và đủ16 permutations mỗi level; không chép công thức chuyển mức vào test.

## Coverage mới

| Acceptance / rủi ro | Test QA | Oracle / bằng chứng |
|---|---|---|
| Promote/demote/hold không phụ thuộc vị trí đáp án đúng | `AssessmentQaBlockMatrixTests` | 6 CEFR ×16 permutations =96 cases; prefix1–3 chưa đổi level; sau câu4 đúng bảng level/stable và reset counters |
| Mixed path/reset stable/stop | `mixedPromotionDemotionAndHoldsMatchLiteralCommittedTrajectory` | Block correct `[3,3,1,2,3,2,2]`; CEFR `[A2,B1,A2,A2,B1,B1,B1]`; stable `[0,0,0,1,0,1,2]`; hoàn tất28, score57.14, không lặp target |
| Max50 với partial block ở level không phải biên | `maximumFiftyKeepsC1DespiteTwoWrongAnswersInThePartialBlock` | Golden path tới C1 tại48; hai câu sai cuối không DEMOTE hoặc tạo HOLD; final C1/50/score56.00, partial2/0/stable0 |
| >=30 usable từng CEFR, kể cả off-target | `startRejectsTwentyNineUsableRowsAtEveryLevelAndSameEventRecovers` | Sáu cases PostgreSQL; giữ29 active ở level đang xét,503/không claim/không aggregate; restore và retry đúng event tạo một201 rồi replay |
| Active không đồng nghĩa usable | `thirtyActiveRowsAreInsufficientWhenOneOffTargetRowIsBlank` | Reference seed30 C2; blank word/POS/meaning làm còn29 usable, start A1 vẫn503; fixture được restore trong finally |
| Content loss ngay tại block transition | `unavailableNextLevelRollsBackTheFourthAnswerAndRetryPromotesExactlyOnce` | Ba câu đúng, mất một A2 trước câu4;503 giữ nguyên persisted state/items và không claim; restore→same event promote A2 đúng một lần |
| Concurrent terminal answer | `concurrentTerminalAnswersProduceOneCompletionAndNoExtraQuestion` | Same/different event, bốn worker; một transition20, một hoặc bốn200 replay, loser409/không claim; final profile A1,20 distinct items/không unanswered/không XP-streak-SRS |

New suites tổng **110 invocations**:96 pure và14 PostgreSQL cases. PostgreSQL
scenario gọi controller qua MockMvc, không dùng outer test transaction: assertions
đọc dữ liệu đã commit sau response. Integration suite dùng Testcontainers/Flyway/Hibernate validation;
Clock cố định `2026-10-26T08:00:00Z`, JWT secret ngẫu nhiên chỉ trong process.
Các thay đổi catalog chỉ trong isolated test database, được restore trong finally;
không migration, shared database hoặc seed production bị sửa.

## Coverage hiện hữu được giữ và chạy regression

| Acceptance | Existing suite / evidence |
|---|---|
| Min20, floor clamp, ceiling clamp, stable reset, partial50 | `AssessmentBlockCalculatorTests`, `AssessmentAnswerIntegrationTests` |
| Selector determinism/distinct raw meanings/nearest fallback/target exhaustion | `AssessmentQuestionSelectorTests` |
| Snapshot/options persistence/constraints/ownership/row lock | `AssessmentPersistenceIntegrationTests` |
| Start/read/next, replay/reuse, content rollback và concurrent start | `AssessmentApiIntegrationTests` |
| Answer raw validation, current guards, replay/reuse, next/profile rollback, score3.13, all3types và missing profile | `AssessmentAnswerIntegrationTests` |
| Response/auth/errors và int32 request bounds | `AssessmentOpenApiContractTests` |

Các suite trên không bị sửa hoặc bỏ khỏi build. Full clean verify kiểm cả coverage
hiện hữu và QA mới. Task này không claim Android/device assessment E2E hoặc M4 exit.

## Lệnh chạy từ repository root

Yêu cầu JDK21 và Docker đang chạy. Dùng Maven Wrapper hiện có; memory bounds chỉ
áp dụng trong process. Trên PowerShell, có thể đặt JAVA_HOME tới JDK21 đang cài và
MAVEN_OPTS=-Xmx384m trong process hiện tại, không đổi global environment/proxy.

Focused:

```powershell
.\backend\mvnw.cmd --no-transfer-progress -f backend/pom.xml `
  '-DargLine=-Xmx512m -XX:ReservedCodeCacheSize=128m' `
  '-Dspring.test.context.cache.maxSize=4' `
  '-Dtest=AssessmentQaBlockMatrixTests,AssessmentQaScenarioIntegrationTests' test
```

CI-equivalent unit selector:

```powershell
.\backend\mvnw.cmd --no-transfer-progress -f backend/pom.xml `
  '-DargLine=-Xmx512m -XX:ReservedCodeCacheSize=128m' `
  '-Dspring.test.context.cache.maxSize=4' `
  '-Dtest=!*IntegrationTests,!OpenApiContractHarnessTests' test
```

Full regression và package:

```powershell
.\backend\mvnw.cmd --no-transfer-progress -f backend/pom.xml `
  '-DargLine=-Xmx512m -XX:ReservedCodeCacheSize=128m' `
  '-Dspring.test.context.cache.maxSize=4' clean verify
```

Không chạy hai Maven writer cùng lúc. Unit selector phát hiện optional
`AndroidLearningE2eScenario`; thiếu explicit `eac.e2e.serial` phải là NOT RUN,
không được báo thành E2E PASS. QA-ASMT-001 không cần Android serial.

## Evidence và trạng thái

Owner focused ban đầu PASS110, zero failure/error/skip, BUILD48.393s. Các kết quả
owner tiếp theo: exact CI selector PASS320 discovered/319 executed/1 Scenario
NOT RUN, BUILD21.357s; full clean verify PASS635/79XML, zero failure/error/skip,
BUILD5:30. Trong full run, hai QA suites chạy lại trên cùng source freeze: pure96
và PostgreSQL14, tổng110. Không có initial test failure hoặc product-code fix.

Independent review và closure được ghi theo actual command tại
`docs/planning/EXECUTION_LOG.md`; không suy PASS từ tài liệu test design này.
Logs/XML/source hashes lưu ngoài repository dưới
`%TEMP%/QA-ASMT-001-20261026/`. Full XML được archive trước reviewer focused rerun
để không nhầm current `target/surefire-reports` với một full run sạch.

Independent gates bắt buộc: AR, DBR và QAR khác authority/session với QA implementer.
Implementer dừng sau TEST, không tự cấp reviewer PASS, không commit/push.
