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
