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

Task `ADM-FND-001` chỉ tạo project/tooling, theme tokens, router tối thiểu,
accessible Admin shell placeholder và test nền tảng. HTTP client, auth
interceptor, query/mutation, canonical API error mapping và route guard thuộc
các task tiếp theo; foundation này không gọi API, PostgreSQL hoặc LLM.
