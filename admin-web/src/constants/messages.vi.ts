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
    title: 'Khu vực quản trị',
    description:
      'React, TypeScript và Vite đã được cấu hình theo kiến trúc Admin Web của dự án.',
    statusTitle: 'Trạng thái nền tảng',
    statusDescription:
      'Bạn đã đăng nhập quản trị. Tổng quan và các chức năng quản trị chưa được triển khai.',
    statusReady: 'Cấu hình nền tảng đã sẵn sàng',
  },
  auth: {
    loginTitle: 'Đăng nhập quản trị',
    email: 'Email', password: 'Mật khẩu', signIn: 'Đăng nhập', signOut: 'Đăng xuất',
    signingIn: 'Đang đăng nhập…', waiting: 'Đang xử lý phiên làm việc…',
    emailRequired: 'Vui lòng nhập email.', emailInvalid: 'Vui lòng nhập email hợp lệ.',
    passwordRequired: 'Vui lòng nhập mật khẩu.', passwordInvalid: 'Mật khẩu tối đa 100 ký tự.',
    invalidCredentials: 'Email hoặc mật khẩu không đúng.',
    locked: 'Tài khoản đang bị khóa. Vui lòng thử lại sau hoặc liên hệ quản trị viên.',
    rateLimited: 'Bạn đã gửi quá nhiều yêu cầu. Vui lòng thử lại sau.',
    loginFailure: 'Không thể đăng nhập. Vui lòng kiểm tra kết nối và thử lại.',
    permission: 'Bạn không có quyền truy cập khu vực quản trị.',
    memoryNotice: 'Phiên đăng nhập chỉ được giữ trong tab này. Tải lại hoặc đóng tab cần đăng nhập lại.',
    logoutUnconfirmed: 'Đã đăng xuất trên trình duyệt. Chưa xác nhận thu hồi phiên trên máy chủ.',
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
