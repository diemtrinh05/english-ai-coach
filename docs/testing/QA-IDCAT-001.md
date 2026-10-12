# QA-IDCAT-001 — Bộ kiểm thử Identity & Catalog

Task M2/P0 do QAR implement; reviewer độc lập bắt buộc SR, DBR, QAR. Tài liệu
này mô tả coverage và lệnh tái hiện, không phát hành verdict reviewer.

## Nguồn và phạm vi

`PROJECT_RULES.md`, `IMPLEMENTATION_PLAN.md` §14/19, `MASTER_BACKLOG.md`
QA-IDCAT-001; SRS v1.2, Database v1.6, Architecture v1.3, API/OpenAPI v1.4,
Technical v1.2, Backend v1.3 và các hợp đồng owner-approved đã tích hợp của
BE-AUTH-003/005/006/007, BE-USER-001, BE-GOAL-001, BE-VOC-001/002/003.

Chỉ thêm kiểm thử xuyên module và tài liệu QA; không đổi sản phẩm, API,
schema, migration, dependency, CI hoặc client. Learning/assessment/Daily Plan
và thuật toán M3 trở đi không thuộc task này.

## Coverage acceptance

| Phạm vi | Kiểm thử xuyên module mới | Coverage chuyên biệt giữ nguyên |
|---|---|---|
| Register/login | Phiên do API cấp dùng được cho hồ sơ/mục tiêu; login giữ identity và dữ liệu đã lưu; duplicate/credentials sai không cấp phiên | RegistrationIntegration/Policy/Rollback/OpenApiContractTests; LoginIntegration/Policy/Rollback/OpenApiContractTests |
| Refresh/logout | Token pair thật qua API; fixed expiry, hash DB, revoke/reuse, retry logout 204, Access JWT còn hiệu lực, không ảnh hưởng phiên khác | RefreshIntegration/Rollback/OpenApiContractTests; LogoutIntegration/OpenApiContractTests — expiry/lock/concurrency/rollback |
| Google | Signed JWT qua verifier thực với fake JWKS; profile/goals và rotation/logout dùng session đã cấp; cùng sub giữ metadata, collision không link, LOCAL login từ chối GOOGLE | GoogleIdentityVerifierTests; GoogleLoginIntegration/Rollback/OpenApiContractTests — signature/claims/config/outage/manual lock/concurrency/rollback |
| Profile | GET chưa có profile không tạo default; dữ liệu đọc lại xuyên login/refresh; payload userId/role/CEFR không đổi owner/quyền; timezone lỗi không ghi; hai PUT profile/goals có commit độc lập | ProfileIntegration/OpenApiContractTests — boundaries/avatar/CEFR/timestamps/concurrency/rollback/unknown user |
| Goals | Lựa chọn gắn principal; optional primary; duplicate/unknown/inactive không ghi; retry explicit goal sau profile đã commit | GoalIntegration/OpenApiContractTests — public active, own inactive, sorted IDs, one primary DB/service, concurrent replace/rollback |
| Catalog | Không cần JWT trước/sau logout; sáu CEFR mỗi mức >=30 từ active qua API, meaningVi không blank; topic/detail/vocabulary/examples; invalid filter/unknown ID | CefrLevelApiIntegrationTests, TopicApiIntegration/TopicOpenApiContractTests, VocabularyApiIntegration/VocabularyOpenApiContract/VocabularyBatchingTests; DemoVocabularySeed/GoalTopicSeedIntegrationTests |
| TTS/audio | GET metadata xuyên auth lifecycle vẫn thành công khi adapter thực disabled theo cấu hình test mặc định | TtsServiceTests + TtsConcurrencyIntegrationTests: bytes/contentType, URL HTTPS, lỗi provider/storage degrade, không ghi URL lỗi, concurrent winner |
| Authz/isolation | Hai user API thật không đổi profile/goals/refresh session của nhau; USER→ADMIN 403; private reads/writes/logout thiếu/sai JWT 401; response ngoài auth không chứa token/hash | JwtAuthenticationIntegrationTests, SecurityFilterChainIntegrationTests, SecurityFoundationTests, RequestCorrelationTests |

Tên rút gọn trong bảng tương ứng class Java dưới `backend/src/test/java`.
Suite mới `auth/IdentityCatalogIntegrationTests.java` bổ sung sáu scenario,
không sao chép toàn bộ boundary case của từng suite chuyên biệt.

## Môi trường và lệnh

JDK21, Docker Engine tương thích Testcontainers, Maven Wrapper hiện có.
PostgreSQL image `postgres:16.15-alpine3.24`; datasource được cung cấp qua
`PostgreSqlIntegrationTestSupport`/`@ServiceConnection`. Context mới chạy Flyway
V1–V7 và Hibernate `ddl-auto=validate`; không dùng H2 hoặc DB chung.

Chạy từ repository root, mỗi thời điểm chỉ một Maven writer:

```powershell
.\backend\mvnw.cmd --no-transfer-progress -f backend/pom.xml clean verify
.\backend\mvnw.cmd --no-transfer-progress -f backend/pom.xml '-Dtest=IdentityCatalogIntegrationTests' test
python -m unittest discover -s tools -p 'test_*.py'
python tools/baseline_audit.py
python tools/ci_workflow_audit.py
python tools/secret_audit.py
git diff --check
```

Lệnh focused chạy lại sau full verify để kiểm tra repeatability; không thay thế
full acceptance/regression. Báo cáo số test lấy từ Surefire XML và exit code/log
thật. Reviewers chạy riêng và ghi rõ full hay subset; không cộng hai lần cùng test.

## Isolation và giới hạn

Clock cố định dùng `GoogleTokenFixture.NOW`; JWT secret/RSA key chỉ sinh trong
runtime test, fake JWKS có public key tương ứng. Google verifier thực vẫn kiểm
tra chữ ký/claims; chỉ I/O JWKS được thay. Không gọi Google thật, không cần
credential thật và không chứng minh cấu hình deployment/live-account.

Không có outer `@Transactional`: từng request qua security/controller/service
commit thật, request kế tiếp đọc kết quả đã commit. Email/subject/fixture goal
dùng UUID; `@AfterEach` chỉ xóa token/goals/profile/user thuộc email đã tạo trong
test và goal UUID do test sở hữu. Không truncate seed/reference data hoặc tắt
constraint. Container do Spring/Testcontainers quản lý; không sửa proxy máy hay
environment toàn cục. Session fixture có `toString()` che token, không in token.

M2 exit gate kiểm tra >=30 vocabulary usable/CEFR qua API trong suite mới và
chi tiết chất lượng/provenance ở `DemoVocabularySeedIntegrationTests`. Đây là
kiểm chứng backend Identity/Catalog; không tuyên bố full UI E2E hoặc provider
TTS/Object Storage thật. Kết quả TEST/reviewer/finalize/publish/CI được ghi theo
thời gian tại `docs/planning/EXECUTION_LOG.md`.
