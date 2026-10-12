import { describe, expect, it, vi } from 'vitest'
import { AdminAuthSession } from './AdminAuthSession'
import { createAuthService, parsePair, parseUser, type AuthService, type LoginResponse, type TokenPair } from './authService'
import { createApiClient } from '../api/apiClient'
import { ApiError, ApiSessionChangedError } from '../api/apiError'

const pair: TokenPair = { accessToken: 'test-access', refreshToken: 'test-refresh', expiresIn: 1800, tokenType: 'Bearer' }
const response: LoginResponse = { ...pair, user: { id: '00000000-0000-0000-0000-000000000001', email: 'admin@example.test', fullName: 'Admin', role: 'ADMIN', status: 'ACTIVE' } }
function deferred<T>() { let resolve!: (value: T) => void; let reject!: (error: Error) => void; const promise = new Promise<T>((yes, no) => { resolve = yes; reject = no }); return { promise, resolve, reject } }
function fixture(overrides: Partial<AuthService> = {}) {
  const service: AuthService = { login: vi.fn().mockResolvedValue(response), refresh: vi.fn().mockResolvedValue({ ...pair, accessToken: 'new-access', refreshToken: 'new-refresh' }), currentUser: vi.fn().mockResolvedValue(response.user), logout: vi.fn().mockResolvedValue(undefined), ...overrides }
  const clear = vi.fn()
  const session = new AdminAuthSession(service, clear)
  return { service, clear, session }
}

describe('Admin auth memory/session contract', () => {
  it('khởi tạo lại mất token, không persist browser storage', async () => {
    const { session } = fixture()
    await session.login('admin@example.test', 'test-password')
    expect(session.getSessionSnapshot().accessToken).toBe(pair.accessToken)
    expect(fixture().session.getSessionSnapshot().accessToken).toBeNull()
    expect(localStorage.length).toBe(0)
    expect(session.getView()).not.toHaveProperty('refreshToken')
  })
  it('login sai giữ anonymous và không cấp role', async () => {
    const { session } = fixture({ login: vi.fn().mockRejectedValue(new ApiError({ status: 401, code: 'AUTH_INVALID_CREDENTIALS', message: 'test' })) })
    await expect(session.login('a@example.test', 'bad')).rejects.toMatchObject({ status: 401 })
    expect(session.getView().user).toBeNull()
  })
  it('serialized refresh và lưu nguyên tử token pair mới', async () => {
    const pending = deferred<TokenPair>()
    const { session, service } = fixture({ refresh: vi.fn().mockReturnValue(pending.promise) })
    await session.login('admin@example.test', 'test-password')
    const expected = session.getSessionSnapshot().sessionGeneration
    const first = session.refreshAccessToken(expected)
    const second = session.refreshAccessToken(expected)
    expect(second).toBe(first)
    pending.resolve({ ...pair, accessToken: 'rotated-access', refreshToken: 'rotated-refresh' })
    await first
    expect(service.refresh).toHaveBeenCalledTimes(1)
    await session.logout()
    expect(service.logout).toHaveBeenCalledWith(expect.objectContaining({ accessToken: 'rotated-access', refreshToken: 'rotated-refresh' }))
  })
  it('logout chờ refresh rồi revoke đúng pair hiện tại, chặn duplicate', async () => {
    const pending = deferred<TokenPair>()
    const { session, service, clear } = fixture({ refresh: vi.fn().mockReturnValue(pending.promise) })
    await session.login('admin@example.test', 'test-password')
    const refresh = session.refreshAccessToken(session.getSessionSnapshot().sessionGeneration)
    const logout = session.logout()
    await session.logout()
    expect(service.logout).not.toHaveBeenCalled()
    pending.resolve({ ...pair, accessToken: 'rotated', refreshToken: 'rotated-refresh' })
    await refresh; await logout
    expect(service.logout).toHaveBeenCalledTimes(1)
    expect(service.logout).toHaveBeenCalledWith(expect.objectContaining({ refreshToken: 'rotated-refresh' }))
    expect(session.getView()).toMatchObject({ phase: 'anonymous', notice: 'none' })
    expect(clear).toHaveBeenCalledTimes(2)
  })
  it('logout timeout clear local nhưng không claim server revoke hoặc retry', async () => {
    const { session, service } = fixture({ logout: vi.fn().mockRejectedValue(new Error('timeout')) })
    await session.login('admin@example.test', 'test-password')
    await session.logout()
    expect(session.getSessionSnapshot().accessToken).toBeNull()
    expect(session.getView().notice).toBe('logout-unconfirmed')
    expect(service.logout).toHaveBeenCalledTimes(1)
  })
  it('refresh reject trước rotation vẫn logout đúng old current pair một lần', async () => {
    const pending = deferred<TokenPair>()
    const { session, service } = fixture({ refresh: vi.fn().mockReturnValue(pending.promise) })
    await session.login('admin@example.test', 'test-password')
    const refresh = session.refreshAccessToken(session.getSessionSnapshot().sessionGeneration)
    const rejected = expect(refresh).rejects.toThrow('network')
    const logout = session.logout()
    pending.reject(new Error('network'))
    await rejected; await logout
    expect(service.logout).toHaveBeenCalledExactlyOnceWith(expect.objectContaining(pair))
    expect(session.getView()).toMatchObject({ phase: 'anonymous', notice: 'none' })
  })
  it('rotation lưu pair mới rồi GETme reject vẫn revoke đúng rotated pair', async () => {
    const current = deferred<typeof response.user>()
    const { session, service } = fixture({ currentUser: vi.fn().mockReturnValue(current.promise) })
    await session.login('admin@example.test', 'test-password')
    const refresh = session.refreshAccessToken(session.getSessionSnapshot().sessionGeneration)
    const rejected = expect(refresh).rejects.toThrow('me failed')
    const logout = session.logout()
    await Promise.resolve()
    current.reject(new Error('me failed'))
    await rejected; await logout
    expect(service.logout).toHaveBeenCalledExactlyOnceWith(expect.objectContaining({ accessToken: 'new-access', refreshToken: 'new-refresh' }))
    expect(session.getView().notice).toBe('none')
  })
  it('generation thay khi chờ refresh không logout hoặc clear B', async () => {
    const pending = deferred<TokenPair>()
    const { session, service } = fixture({ refresh: vi.fn().mockReturnValue(pending.promise) })
    await session.login('admin@example.test', 'test-password')
    const expected = session.getSessionSnapshot().sessionGeneration
    const refresh = session.refreshAccessToken(expected)
    const rejected = expect(refresh).rejects.toBeInstanceOf(ApiSessionChangedError)
    const logout = session.logout()
    session.clearSession(expected)
    await session.login('admin@example.test', 'test-password')
    pending.resolve({ ...pair, accessToken: 'stale-A' })
    await rejected; await logout
    expect(service.logout).not.toHaveBeenCalled()
    expect(session.getView()).toMatchObject({ phase: 'authenticated', user: response.user })
    expect(session.getSessionSnapshot().accessToken).toBe(pair.accessToken)
  })
  it('concrete401 refreshfailure không xóa pair hoặc notice khi logout đang chờ', async () => {
    const pending = deferred<TokenPair>()
    const started = deferred<void>()
    const revocation = deferred<void>()
    const { session, service } = fixture({
      refresh: vi.fn().mockImplementation(() => { started.resolve(); return pending.promise }),
      logout: vi.fn().mockReturnValue(revocation.promise),
    })
    await session.login('admin@example.test', 'test-password')
    const client = createApiClient({ auth: session, fetchImplementation: vi.fn<typeof fetch>().mockResolvedValue(new Response('{}', { status: 401 })) })
    const request = client.request('/users/me')
    const rejected = expect(request).rejects.toMatchObject({ status: 401 })
    await started.promise
    const logout = session.logout()
    pending.reject(new Error('network'))
    await rejected
    expect(session.getView().phase).toBe('logging-out')
    expect(session.getSessionSnapshot().accessToken).toBe(pair.accessToken)
    revocation.resolve()
    await logout
    expect(service.logout).toHaveBeenCalledExactlyOnceWith(expect.objectContaining(pair))
    expect(session.getView()).toMatchObject({ phase: 'anonymous', notice: 'none', user: null })
    expect(session.getSessionSnapshot().accessToken).toBeNull()
  })
  it('late403 khi chờ refresh không mở duplicate logout hoặc private UI', async () => {
    const pending = deferred<TokenPair>()
    const { session, service } = fixture({ refresh: vi.fn().mockReturnValue(pending.promise) })
    await session.login('admin@example.test', 'test-password')
    const expected = session.getSessionSnapshot().sessionGeneration
    const refresh = session.refreshAccessToken(expected)
    const logout = session.logout()
    session.denyPermission(expected)
    await session.logout()
    expect(session.getView().phase).toBe('logging-out')
    expect(service.logout).not.toHaveBeenCalled()
    pending.resolve({ ...pair, accessToken: 'rotated', refreshToken: 'rotated-refresh' })
    await refresh; await logout
    expect(service.logout).toHaveBeenCalledExactlyOnceWith(expect.objectContaining({ refreshToken: 'rotated-refresh' }))
    expect(session.getView()).toMatchObject({ phase: 'anonymous', notice: 'none' })
  })
  it('concrete late403 khi pending revoke không tạo POST thứ hai', async () => {
    const revocation = deferred<void>()
    const { session, service } = fixture({ logout: vi.fn().mockReturnValue(revocation.promise) })
    await session.login('admin@example.test', 'test-password')
    const client = createApiClient({ auth: session, onForbidden: session.denyPermission, fetchImplementation: vi.fn<typeof fetch>().mockResolvedValue(new Response('{}', { status: 403 })) })
    const request = client.request('/admin/users')
    const rejected = expect(request).rejects.toMatchObject({ status: 403 })
    const logout = session.logout()
    await rejected
    await session.logout()
    expect(session.getView().phase).toBe('logging-out')
    expect(service.logout).toHaveBeenCalledTimes(1)
    revocation.resolve()
    await logout
    expect(session.getView()).toMatchObject({ phase: 'anonymous', notice: 'none', user: null })
  })
  it('late403 và automaticfailure giữ currentpair/notice đến logoutfinally', async () => {
    const revocation = deferred<void>()
    const { session, service } = fixture({ logout: vi.fn().mockReturnValue(revocation.promise) })
    await session.login('admin@example.test', 'test-password')
    const expected = session.getSessionSnapshot().sessionGeneration
    const logout = session.logout()
    await Promise.resolve()
    session.denyPermission(expected)
    session.clearSessionAfterAuthFailure(expected)
    await session.logout()
    expect(session.getView()).toMatchObject({ phase: 'logging-out', notice: 'none' })
    expect(session.getSessionSnapshot()).toEqual({ accessToken: pair.accessToken, sessionGeneration: expected })
    expect(service.logout).toHaveBeenCalledTimes(1)
    revocation.reject(new Error('unknown server outcome'))
    await logout
    expect(session.getView()).toMatchObject({ phase: 'anonymous', user: null, notice: 'logout-unconfirmed' })
    expect(session.getSessionSnapshot().accessToken).toBeNull()
  })
  it('late login sau invalidation không restore phiên', async () => {
    const pending = deferred<LoginResponse>()
    const { session } = fixture({ login: vi.fn().mockReturnValue(pending.promise) })
    const login = session.login('admin@example.test', 'test-password')
    session.clearSession(session.getSessionSnapshot().sessionGeneration)
    pending.resolve(response)
    await expect(login).rejects.toBeInstanceOf(ApiSessionChangedError)
    expect(session.getView().user).toBeNull()
  })
  it('refresh A không ghi hoặc GET me dưới session B', async () => {
    const pending = deferred<TokenPair>()
    const { session, service } = fixture({ refresh: vi.fn().mockReturnValue(pending.promise) })
    await session.login('admin@example.test', 'test-password')
    const expected = session.getSessionSnapshot().sessionGeneration
    const refresh = session.refreshAccessToken(expected)
    session.clearSession(expected)
    await session.login('admin@example.test', 'test-password')
    pending.resolve({ ...pair, accessToken: 'stale-A' })
    await expect(refresh).rejects.toBeInstanceOf(ApiSessionChangedError)
    expect(service.currentUser).not.toHaveBeenCalled()
    expect(session.getSessionSnapshot().accessToken).toBe(pair.accessToken)
    session.clearSession(expected)
    expect(session.getView().phase).toBe('authenticated')
  })
  it('refresh cập nhật server role USER, không trust JWT decode', async () => {
    const { session } = fixture({ currentUser: vi.fn().mockResolvedValue({ ...response.user, role: 'USER' }) })
    await session.login('admin@example.test', 'test-password')
    await session.refreshAccessToken(session.getSessionSnapshot().sessionGeneration)
    expect(session.getView().user?.role).toBe('USER')
  })
  it('401 concurrent qua concrete client chỉ rotation một lần', async () => {
    const { session, service } = fixture()
    await session.login('admin@example.test', 'test-password')
    const fetcher = vi.fn<typeof fetch>().mockImplementation(async (_input, init) => {
      const token = new Headers(init?.headers).get('Authorization')
      return token === `Bearer ${pair.accessToken}` ? new Response('{}', { status: 401 }) : Response.json({ accepted: true })
    })
    const client = createApiClient({ auth: session, fetchImplementation: fetcher })
    const results = await Promise.all([client.request('/users/me'), client.request('/users/me')])
    expect(results).toEqual([{ accepted: true }, { accepted: true }])
    expect(service.refresh).toHaveBeenCalledTimes(1)
  })
  it('refresh lỗi clear phiên và cache, không retry request', async () => {
    const { session, service } = fixture({ refresh: vi.fn().mockRejectedValue(new Error('revoked')) })
    await session.login('admin@example.test', 'test-password')
    const fetcher = vi.fn<typeof fetch>().mockResolvedValue(new Response('{}', { status: 401 }))
    const client = createApiClient({ auth: session, fetchImplementation: fetcher })
    await expect(client.request('/users/me')).rejects.toMatchObject({ status: 401 })
    expect(service.refresh).toHaveBeenCalledTimes(1)
    expect(fetcher).toHaveBeenCalledTimes(1)
    expect(session.getView()).toMatchObject({ user: null, notice: 'expired' })
  })
  it('403 chuyển permission và giữ phiên để explicit logout', async () => {
    const { session, service } = fixture()
    await session.login('admin@example.test', 'test-password')
    const client = createApiClient({ auth: session, onForbidden: session.denyPermission, fetchImplementation: vi.fn<typeof fetch>().mockResolvedValue(new Response('{}', { status: 403 })) })
    await expect(client.request('/admin/users')).rejects.toMatchObject({ status: 403 })
    expect(session.getView().phase).toBe('forbidden')
    expect(service.refresh).not.toHaveBeenCalled()
    await session.logout()
    expect(service.logout).toHaveBeenCalledTimes(1)
  })
  it('late 200 A không publish data vào B', async () => {
    const pending = deferred<Response>()
    const { session } = fixture()
    await session.login('admin@example.test', 'test-password')
    const client = createApiClient({ auth: session, fetchImplementation: vi.fn<typeof fetch>().mockReturnValue(pending.promise) })
    const request = client.request('/users/me')
    session.clearSession(session.getSessionSnapshot().sessionGeneration)
    await session.login('admin@example.test', 'test-password')
    pending.resolve(Response.json({ private: 'A' }))
    await expect(request).rejects.toBeInstanceOf(ApiSessionChangedError)
  })
})

describe('Auth HTTP/DTO contract', () => {
  it('login/refresh public không authorization, cookies, redirect/retry', async () => {
    const fetcher = vi.fn<typeof fetch>().mockResolvedValueOnce(Response.json(response)).mockResolvedValueOnce(Response.json(pair))
    const service = createAuthService(fetcher)
    await service.login('admin@example.test', 'test-password'); await service.refresh('test-refresh')
    expect(fetcher).toHaveBeenCalledTimes(2)
    for (const [, init] of fetcher.mock.calls) {
      expect(new Headers(init?.headers).has('Authorization')).toBe(false)
      expect(init).toMatchObject({ method: 'POST', credentials: 'omit', redirect: 'error', cache: 'no-store' })
    }
  })
  it('logout đúng path/Bearer/body và 204', async () => {
    const fetcher = vi.fn<typeof fetch>().mockResolvedValue(new Response(null, { status: 204 }))
    await createAuthService(fetcher).logout(pair)
    const [url, init] = fetcher.mock.calls[0]
    expect(url).toBe('/api/v1/auth/logout')
    expect(new Headers(init?.headers).get('Authorization')).toBe(`Bearer ${pair.accessToken}`)
    expect(JSON.parse(init?.body as string)).toEqual({ refreshToken: pair.refreshToken })
  })
  it.each([201, 204])('login reject success status không phải200 (%i)', async (status) => {
    const fetcher = vi.fn<typeof fetch>().mockResolvedValue(status === 204 ? new Response(null, { status }) : Response.json(response, { status }))
    await expect(createAuthService(fetcher).login('a@example.test', 'p')).rejects.toThrow('Unexpected auth status')
  })
  it.each([{ ...pair, refreshToken: '' }, { ...pair, expiresIn: '1800' }, { ...pair, tokenType: 'Other' }])('reject malformed pair', (value) => { expect(() => parsePair(value)).toThrow() })
  it('reject forged/unknown role và UUID malformed', () => {
    expect(() => parseUser({ ...response.user, role: 'SUPERADMIN' })).toThrow()
    expect(() => parseUser({ ...response.user, id: 'bad' })).toThrow()
  })
})
