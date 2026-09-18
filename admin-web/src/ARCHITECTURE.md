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
