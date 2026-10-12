# English AI Coach Admin Web

SPA quản trị V1 dùng React, TypeScript và Vite. Giao diện mặc định là tiếng
Việt (`vi-VN`); mọi visible copy nằm trong typed message catalog tại
`src/constants/messages.vi.ts`.

## Yêu cầu

- Node.js 24 hoặc phiên bản tương thích với package lock hiện hành.
- npm 11 hoặc phiên bản tương thích.

## Lệnh phát triển

```text
npm ci
npm run dev
npm run lint
npm run typecheck
npm run test:run
npm run build
```

Production build được tạo tại `dist/`. Thư mục này và `node_modules/` không
được commit.

## Ranh giới foundation

Task `ADM-FND-001` tạo project/tooling, theme tokens, router tối thiểu,
accessible Admin shell placeholder và test nền tảng. `ADM-FND-002` bổ sung
centralized fetch client, injected auth-session boundary, serialized 401
refresh, canonical 403/409 error preservation, TanStack Query và feedback
state dùng chung. Runtime client fail closed: `configureApiClient` bắt buộc
nhận `AuthSessionAdapter` trước khi feature service có thể gọi
`getApiClient`; không có anonymous singleton hoặc fallback không auth.
Mỗi authenticated attempt giữ cả token đã gửi và `sessionGeneration`. Client
chỉ dùng token mới để retry khi generation không đổi; request thuộc generation
cũ bị hủy nội bộ và không được replay dưới session thay thế.

Complete login/logout, browser token storage, role resolution và route guard
business flow thuộc `ADM-AUTH-001`. Feature endpoint services và CRUD screens
thuộc các task feature; Admin Web không gọi PostgreSQL hoặc LLM trực tiếp.

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