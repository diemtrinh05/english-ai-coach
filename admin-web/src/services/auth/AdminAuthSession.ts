import type { AuthSessionAdapter, AuthSessionGeneration } from './AuthSessionAdapter'
import type { AuthService, AuthUser, TokenPair } from './authService'
import { ApiSessionChangedError } from '../api/apiError'

export interface AdminAuthView {
  user: AuthUser | null
  phase: 'anonymous' | 'authenticating' | 'authenticated' | 'logging-out' | 'forbidden'
  notice: 'none' | 'expired' | 'logout-unconfirmed'
}

export class AdminAuthSession implements AuthSessionAdapter {
  private generation = 0
  private pair: TokenPair | null = null
  private view: AdminAuthView = { user: null, phase: 'anonymous', notice: 'none' }
  private listeners = new Set<() => void>()
  private refreshes = new Map<AuthSessionGeneration, Promise<void>>()

  private service: AuthService
  private clearPrivateCache: () => void
  constructor(service: AuthService, clearPrivateCache: () => void) {
    this.service = service
    this.clearPrivateCache = clearPrivateCache
  }

  getView = (): AdminAuthView => this.view
  subscribe = (listener: () => void): (() => void) => { this.listeners.add(listener); return () => this.listeners.delete(listener) }
  getSessionSnapshot = () => ({ accessToken: this.pair?.accessToken ?? null, sessionGeneration: this.generation })
  private publish(view: AdminAuthView) { this.view = view; this.listeners.forEach((listener) => listener()) }
  private assertCurrent(expected: AuthSessionGeneration) { if (this.generation !== expected) throw new ApiSessionChangedError() }

  async login(email: string, password: string): Promise<void> {
    if (this.view.phase === 'authenticating' || this.view.phase === 'logging-out') return
    const expected = ++this.generation
    this.pair = null
    this.clearPrivateCache()
    this.publish({ user: null, phase: 'authenticating', notice: 'none' })
    try {
      const response = await this.service.login(email, password)
      this.assertCurrent(expected)
      this.pair = response
      this.publish({ user: response.user, phase: 'authenticated', notice: 'none' })
    } catch (error) {
      this.assertCurrent(expected)
      this.publish({ user: null, phase: 'anonymous', notice: 'none' })
      throw error
    }
  }

  refreshAccessToken = (expected: AuthSessionGeneration): Promise<void> => {
    this.assertCurrent(expected)
    const existing = this.refreshes.get(expected)
    if (existing) return existing
    if (!this.pair || this.view.phase === 'logging-out') return Promise.reject(new ApiSessionChangedError())
    const refreshToken = this.pair.refreshToken
    const pending = (async () => {
      const pair = await this.service.refresh(refreshToken)
      this.assertCurrent(expected)
      // Lưu nguyên tử cặp mới trước khi xác minh role hiện tại; không retry rotation.
      this.pair = pair
      const user = await this.service.currentUser(pair.accessToken)
      this.assertCurrent(expected)
      this.publish({ ...this.view, user })
    })().finally(() => { if (this.refreshes.get(expected) === pending) this.refreshes.delete(expected) })
    this.refreshes.set(expected, pending)
    return pending
  }

  clearSession = (expected: AuthSessionGeneration): void => {
    if (this.generation !== expected) return
    ++this.generation
    this.pair = null
    this.clearPrivateCache()
    this.publish({ user: null, phase: 'anonymous', notice: 'expired' })
  }

  clearSessionAfterAuthFailure = (expected: AuthSessionGeneration): void => {
    // Logout đang giữ quyền kết thúc phiên sau một lần thử revoke cặp hiện tại.
    if (this.generation === expected && this.view.phase === 'logging-out') return
    this.clearSession(expected)
  }

  denyPermission = (expected: AuthSessionGeneration): void => {
    if (this.generation !== expected) return
    // Permission callback cũ không được thay ownership của logout đang chạy.
    if (this.view.phase === 'logging-out') return
    this.clearPrivateCache()
    this.publish({ ...this.view, phase: 'forbidden' })
  }

  async logout(): Promise<void> {
    if (this.view.phase === 'logging-out') return
    const expected = this.generation
    const pending = this.refreshes.get(expected)
    this.publish({ ...this.view, phase: 'logging-out' })
    let confirmed = false
    try {
      // Refresh lỗi không đồng nghĩa cặp hiện tại đã được thu hồi trên server.
      try { await pending } catch { /* Tiếp tục thử logout đúng cặp còn lại. */ }
      this.assertCurrent(expected)
      if (this.pair) { await this.service.logout(this.pair); confirmed = true }
    } catch { /* Không suy diễn revoke khi server chưa xác nhận. */ }
    finally {
      if (this.generation === expected) {
        ++this.generation
        this.pair = null
        this.clearPrivateCache()
        this.publish({ user: null, phase: 'anonymous', notice: confirmed ? 'none' : 'logout-unconfirmed' })
      }
    }
  }
}
