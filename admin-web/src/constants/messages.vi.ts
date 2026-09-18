export const viMessages = {
  app: {
    name: 'English AI Coach',
    role: 'Quản trị viên',
    documentTitle: 'English AI Coach — Quản trị',
  },
  navigation: {
    label: 'Điều hướng quản trị',
    dashboard: 'Tổng quan',
    users: 'Người dùng',
    vocabulary: 'Từ vựng',
    topics: 'Chủ đề',
    quizzes: 'Bài kiểm tra',
    aiContent: 'Nội dung AI',
    statistics: 'Thống kê học tập',
    aiUsage: 'Mức sử dụng AI',
    auditLogs: 'Nhật ký hoạt động',
  },
  bootstrap: {
    eyebrow: 'Nền tảng quản trị',
    title: 'Admin Web đã sẵn sàng để phát triển',
    description:
      'React, TypeScript và Vite đã được cấu hình theo kiến trúc Admin Web của dự án.',
    statusTitle: 'Trạng thái nền tảng',
    statusDescription:
      'Các tính năng xác thực, kết nối API và nghiệp vụ quản trị sẽ được triển khai trong các task tiếp theo.',
    statusReady: 'Cấu hình nền tảng đã sẵn sàng',
  },
  feedback: {
    loading: 'Đang tải dữ liệu.',
    empty: 'Chưa có dữ liệu.',
    error: 'Không thể tải dữ liệu. Vui lòng thử lại.',
    retry: 'Thử lại',
    unauthorized: 'Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.',
    sessionChanged: 'Phiên làm việc đã thay đổi. Vui lòng thử lại.',
    forbidden: 'Bạn không có quyền thực hiện thao tác này.',
    concurrentUpdate: 'Dữ liệu đã thay đổi trên máy chủ. Vui lòng tải lại và thử lại.',
    idempotencyKeyReuse: 'Thao tác không hợp lệ do mã yêu cầu đã được sử dụng.',
    mutationSubmitting: 'Đang lưu thay đổi.',
    mutationSuccess: 'Đã lưu thay đổi.',
    mutationFailure: 'Không thể lưu thay đổi. Vui lòng thử lại.',
  },
} as const

export type VietnameseMessageCatalog = typeof viMessages
