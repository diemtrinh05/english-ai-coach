# QA-LEARN-002 — Idempotency và optimistic concurrency

Task M3/P0, Owner QAR, dependency BE-LEARN-002 DONE. Workflow
GOV009_DIRECT_MAIN, CI mode ACTUAL_CI_REPOSITORY_HEALTH. Admission tại
`b517397240c62d5aae8094f5b6e259e151b3f8bb`, Required CI39019001187 PASS,
main sạch bằng origin/live; không task khác active, tags nguyên vẹn. Backlog
TODO→IN_PROGRESS đã readback/assert trước test patch ngày 2026-10-18.

## Nguồn và phạm vi

PROJECT_RULES §§11–18,36–37,59.2; AGENTS; implementation plan/backlog/log;
QA role; SRS/Architecture/DB/API/OpenAPI/Technical/Backend hiện hành. API §35–36
quy định canonical hash của method/route/path/query/body, user so riêng,
claim PostgreSQL ON CONFLICT DO NOTHING và conflict CONCURRENT_UPDATE.
BE-LEARN-002 transaction và BE-GAME-001/002 source/award rules đã tích hợp
trong baseline; không cần sửa hoặc phê duyệt contract mới.

Suite mới:
`backend/src/test/java/com/example/englishaicoach/common/idempotency/LearningConcurrencyContractIntegrationTests.java`.
Tám ca dùng MockMvc + JWT thật, PostgreSQL Testcontainers/Flyway/JPA thật,
Clock cố định/chủ động tăng ngày. Fixture user/profile/session chỉ tạo điều kiện;
attempt/completion đi qua HTTP và transaction product. Không outer test
transaction; cleanup chỉ xóa user và row thuộc fixture của test.

## Ma trận kiểm chứng

| Ca | Điều kiện ép / kiểm chứng |
|---|---|
| Canonical replay | Đổi thứ tự JSON và uppercase UUID, replay snapshot cũ sau attempt mới và completion; sáu nhóm bảng không đổi |
| Reuse | Payload, query, endpoint, path completion và user khác trả409 IDEMPOTENCY_KEY_REUSE; không đổi dữ liệu đã commit |
| Bốn duplicate | Barrier trước real INSERT buộc cả bốn request SELECT miss; chính xác một claim true, ba false; bốn response200/snapshot giống nhau; một attempt/progress/streak và đúng ledger sources |
| Payload cạnh tranh | Hai body cùng event, một200/một409; quality và stored snapshot thuộc winner; loser retry không sửa trạng thái |
| User cạnh tranh | Hai user và owned session hợp lệ cùng event; một200/một409, owner ledger/claim đúng, loser không có side effect |
| Winner rollback | Fault sau real storeResponse đầu tiên; response500 có chủ đích, waiter thực hiện lại claim sau rollback và200; attempt/XP/snapshot bị rollback không còn, chỉ một mutation commit |
| Stale progress | Hai session cùng user/word, barrier sau real load cùng version; một200/một409 CONCURRENT_UPDATE; losing claim/attempt/aggregate rollback; explicit cùng-event retry thành công rồi replay không mutation |
| Stale streak | Hai word/session khác nhau, barrier sau real streak load cùng version trong ngày mới; loser rollback cả progress/attempt/XP đã ghi; retry cùng event không thưởng lại streak ngày đó |

Spy chỉ gọi real method rồi điều phối thời điểm hoặc chèn lỗi cố ý; không giả
kết quả claim, state, SRS, award hoặc optimistic lock. Test ở package idempotency
để quan sát claim package-private mà không mở visibility production. Barrier10s,
SQL statement timeout15s và bounded executor45s/future5s giúp regression fail
rõ thay vì treo vô hạn. Không dùng sleep để giả định overlap. Fixture đã commit
cho phép assert SQL ngoài request thấy toàn bộ kết quả thật.

500 chỉ hợp lệ trong ca fault injection được đặt tên rõ; race duplicate thường
đòi chính xác200, race reuse/concurrency đòi chính xác409 với error code tương ứng.
Không claim live Android/E2E hoặc network provider verification.

## Validation và chronology

Command dùng process-only JAVA_HOME JDK21, MAVEN_OPTS=-Xmx384m:

```text
backend/mvnw.cmd --no-transfer-progress -f backend/pom.xml
  -DargLine="-Xmx512m -XX:ReservedCodeCacheSize=128m"
  -Dspring.test.context.cache.maxSize=4
  -Dtest=LearningConcurrencyContractIntegrationTests test
```

Trong PowerShell, từng đối số `-D...` được đặt trong single quotes. Lần command
đầu chưa quote property contextcache bị tách thành lifecycle phase và BUILD
FAILURE0.674s, chưa chạy test (`TEMP/QA-LEARN-002-focused.log`). Lần sau quote
đúng nhưng testCompile FAIL12.677s vì thiếu import MockMvc; thêm import test-only,
giữ log `TEMP/QA-LEARN-002-focused-final.log`. Không sửa product cho test pass.

Focused runtime:8/8 PASS, zero failure/error/skip, BUILD SUCCESS57.550s,
suite38.58s, exit0; `TEMP/QA-LEARN-002-focused-runtime.log`.
Focused repeat8/8 PASS, zero failure/error/skip, BUILD SUCCESS33.229s,
suite28.11s, exit0 (`TEMP/QA-LEARN-002-repeat.log`). Hai focused runs tạo
fresh context/database, xác minh repeatability và cleanup. Full clean verify411/411 PASS, zero failure/error/skip, BUILD SUCCESS04:36min,
exit0,70freshSurefireXML; suite mới8/8PASS. Log
`C:/Users/Hi/AppData/Local/Temp/QA-LEARN-002-full.log`. Fresh Flyway8 migrations,
JPA validate và JAR/repackage PASS.
Audits baseline/CIworkflow/secret PASS; Python46/46 PASS2.037s, py_compile và
diff/cached-diff PASS. Guards scope/newfile/tags được kiểm lại trước finalization.

## Change impact và boundary

Chỉ test/evidence/planning. API/database/client/runtime/dependency/CI workflow
không đổi; migration không áp dụng, backward compatibility không thay đổi.
Không proxy hoặc global environment mutation. Task giữ IN_PROGRESS sau TEST;
required DBR/SR/QAR độc lập. QAR gate phải do agent/session khác implementation,
không self-review, không tự resolve finding. Chưa finalization/commit/push.


## Independent review và finalization — 2026-10-18

DBR /root/qa_learn002_dbr, SR /root/qa_learn002_sr và QAR
/root/qa_learn002_review đều initial PASS/NONE. QAR khác session implementer
/root/qa_learn002_impl; independent run8/8PASS zero failure/error/skip,
BUILD SUCCESS56.518s, suite23.84s/exit0, log
`C:/Users/Hi/AppData/Local/Temp/QA-LEARN-002-independent-qa.log`.
Không reviewer finding/FAIL; resolved0, unresolvedNONE. Owner full411/411 là
run riêng; không ghi independent focused thành full rerun.

Finalizer đồng bộ external evidence và fresh audits/guards, chuyển exact row
IN_PROGRESS→DONE và check current M3 checkbox, không tự review hoặc resolve.
M3 hiện12/16DONE còn4; exit QA-E2E-001 chưa đạt. Fresh audits baseline/CIworkflow/
secret PASS; Python46/46PASS1.384s; pycompile/diff/cached-diff/new2/scope4/
conflict/generated/tag guardsPASS. Lịch sử hai lỗi command/import giữ ở trên.
Diagnostic finalization đếm nhầm checklist section assert0/0FAIL đã được sửa
selector canonical table và readback12/16PASS; chronology chi tiết trong log.
Source Java giữ nguyên sau tests/reviews; không commit/push/index/tag mutation.
Publication và current-task actual Required CI thuộc Git workflow tiếp theo.
