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

Task foundation chưa triển khai API service, auth/query behavior hoặc feature
business flow của các task tiếp theo.
