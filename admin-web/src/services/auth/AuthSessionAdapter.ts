export type AuthSessionGeneration = string | number

export interface AuthSessionSnapshot {
  accessToken: string | null
  sessionGeneration: AuthSessionGeneration
}

export interface AuthSessionAdapter {
  getSessionSnapshot: () => AuthSessionSnapshot
  // Concrete adapter chỉ được ghi token nếu generation này vẫn đang active.
  refreshAccessToken: (expectedSessionGeneration: AuthSessionGeneration) => Promise<void>
  // Concrete adapter chỉ được clear nếu generation này vẫn đang active.
  clearSession: (expectedSessionGeneration: AuthSessionGeneration) => void | Promise<void>
}
