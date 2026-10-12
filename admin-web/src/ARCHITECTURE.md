# Cấu trúc Admin Web

```text
app/          composition root, router, providers, theme
components/   component dùng chung theo layout/table/form/feedback/dialog/chart
features/     auth, dashboard, users, vocabulary, topics, quizzes, AI, statistics, audit
services/     API, auth và browser storage adapter
hooks/        hook dùng chung không thuộc riêng một feature
types/        kiểu dùng chung; API models chỉ thêm khi contract task cho phép
utils/        utility thuần, không chứa business rule do backend sở hữu
constants/    typed Vietnamese messages và constants presentation
```

Luồng phụ thuộc canonical:

```text
Page → Feature Component → Query/Mutation Hook → API Service → HTTP Client
```

`ADM-FND-002` bổ sung nền tảng dùng chung theo đúng luồng trên:

```text
services/api/apiClient.ts       fetch abstraction và canonical error parsing
services/auth/                  callback boundary cho token/session
services/api/queryClient.ts     TanStack Query policy
hooks/                          typed query/mutation hooks
components/feedback/            loading/empty/error presentation
types/api.ts                    shared ErrorResponse/PaginatedResponse
```

Auth adapter chỉ là dependency boundary được inject. Login/logout, token
storage, role resolution và route guard business flow vẫn thuộc
`ADM-AUTH-001`; feature endpoint services và CRUD vẫn thuộc các task feature.
Không có runtime API client ẩn danh: app composition phải gọi
`configureApiClient(AuthSessionAdapter)` đúng một lần, sau đó feature service
mới lấy centralized client qua `getApiClient()`. `getApiClient()` fail closed
trước cấu hình và việc cấu hình lại bị từ chối. Bootstrap hiện chưa cấu hình
client vì concrete auth adapter thuộc `ADM-AUTH-001`.

401 handling gắn với snapshot của từng HTTP attempt gồm access token và
`sessionGeneration`. Refresh trong cùng authenticated session giữ nguyên
generation; logout, login, account switch hoặc session replacement phải đổi
generation. Request chỉ retry bằng token mới khi generation không đổi. Nếu
generation đã đổi, request fail bằng internal `ApiSessionChangedError`, không
refresh, retry hoặc clear session thay thế.

Refresh coordinator lưu một promise theo từng generation. Chỉ request cùng
generation được join promise; cleanup so sánh đúng promise của generation đó
để refresh cũ không xóa state của generation mới.
`refreshAccessToken(expectedGeneration)` và
`clearSession(expectedGeneration)` yêu cầu concrete adapter của ADM-AUTH-001
thực hiện mutation có điều kiện theo generation, nhưng không quy định storage.

### ADM-AUTH-001 — Quyết định owner cho phiên Admin V1 (2026-10-12)

Token pair accessToken/refreshToken chỉ giữ nguyên tử trong bộ nhớ tab; reload
hoặc đóng tab cần đăng nhập lại. Không lưu token trong localStorage,
sessionStorage, IndexedDB hoặc cookie; không bổ sung backend/BFF/cookie contract.
Role USER/ADMIN lấy từ auth response/current user đã xác minh trên backend;
frontend guard là tiện ích UX, backend tiếp tục thực thi quyền ADMIN.

Trong ADM-AUTH-001, ADMIN vào protected shell hiện có tại /admin hoặc
/admin/dashboard, hiển thị rõ Tổng quan/chức năng quản trị chưa triển khai.
Không gọi statistics API hoặc hiển thị metric giả; Dashboard giữ ADM-DASH-001.
USER hoặc HTTP403 hiện màn hình không đủ quyền; không render protected content.
HTTP401 refresh serialized, atomic pair trong cùng session generation;
refresh lỗi/terminal401 clear phiên và private cache, chuyển Login với thông báo.
Request/callback của phiên cũ không được replay hoặc publish dưới phiên thay thế.

Logout explicit chờ refresh đang chạy rồi gửi current pair đúng auth/logout
một lần, không blind retry. Luôn xóa local token/private cache và rời protected
route bằng replace; history cũ không thể render lại dữ liệu private do guard.
Server204 xác nhận revoke đúng token theo BE-AUTH-006. Timeout/offline/server
lỗi chỉ xác nhận đã đăng xuất trên trình duyệt, báo chưa xác nhận thu hồi phiên
trên máy chủ; không claim revoke thành công hoặc rollback local logout.
Các màn hình/flow tương lai và nội dung approved ngoài phạm vi giữ nguyên.
Remediation ADM-AUTH-001: optional clearSessionAfterAuthFailure(expectedGeneration)
phân biệt cleanup tự động sau HTTP401 với explicit clearSession. Khi explicit
logout đang chờ refresh/revoke, private UI đã bị guard và automatic cleanup
đợi logout finally; explicit invalidation vẫn đổi generation ngay. Refresh
settle dù reject vẫn thử revoke đúng current pair còn lại một lần, tuyệt đối
không gửi token của generation thay thế hoặc tự retry refresh/logout.