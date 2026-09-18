import { describe, expect, it, vi } from 'vitest'
import type { AuthSessionAdapter } from '../auth/AuthSessionAdapter'

function jsonResponse(status: number, body: unknown): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: { 'Content-Type': 'application/json' },
  })
}

function canonicalError(status: number, code: string): Response {
  return jsonResponse(status, {
    timestamp: '2026-09-17T00:00:00Z',
    status,
    code,
    message: 'Request failed',
    path: '/api/v1/admin/users',
    details: [],
  })
}

function createAuthSessionAdapter(overrides: Partial<AuthSessionAdapter> = {}): AuthSessionAdapter {
  return {
    getSessionSnapshot: () => ({ accessToken: null, sessionGeneration: 'anonymous-1' }),
    refreshAccessToken: vi.fn().mockResolvedValue(undefined),
    clearSession: vi.fn(),
    ...overrides,
  }
}

function createDeferred<T>() {
  let resolve: ((value: T) => void) | undefined
  const promise = new Promise<T>((promiseResolve) => {
    resolve = promiseResolve
  })

  return {
    promise,
    resolve: (value: T) => resolve?.(value),
  }
}

async function loadPublicApiClientComposition() {
  vi.resetModules()
  return import('./apiClient')
}

describe('public API client composition', () => {
  it('không export singleton ẩn danh và fail closed trước khi cấu hình auth', async () => {
    const apiModule = await loadPublicApiClientComposition()

    expect(Object.prototype.hasOwnProperty.call(apiModule, 'apiClient')).toBe(false)
    expect(() => apiModule.getApiClient()).toThrow(/AuthSessionAdapter/)
  })

  it('yêu cầu adapter hợp lệ và từ chối cấu hình lại runtime client', async () => {
    const apiModule = await loadPublicApiClientComposition()
    const auth = createAuthSessionAdapter()

    expect(() => apiModule.configureApiClient(undefined as unknown as AuthSessionAdapter)).toThrow(
      /AuthSessionAdapter/,
    )

    apiModule.configureApiClient(auth, {
      fetchImplementation: vi.fn<typeof fetch>().mockResolvedValue(jsonResponse(200, { ok: true })),
    })

    expect(() => apiModule.configureApiClient(auth)).toThrow(/already been configured/)
  })

  it('gắn Bearer token qua đúng public runtime client', async () => {
    const apiModule = await loadPublicApiClientComposition()
    const fetchImplementation = vi.fn<typeof fetch>().mockResolvedValue(jsonResponse(200, { ok: true }))
    apiModule.configureApiClient(
      createAuthSessionAdapter({
        getSessionSnapshot: () => ({ accessToken: 'access-token', sessionGeneration: 'G1' }),
      }),
      { fetchImplementation },
    )

    await expect(apiModule.getApiClient().request('/admin/users')).resolves.toEqual({ ok: true })

    const [, init] = fetchImplementation.mock.calls[0] ?? []
    expect(new Headers(init?.headers).get('Authorization')).toBe('Bearer access-token')
  })

  it('serialize concurrent 401, chờ một refresh và retry mỗi request đúng một lần', async () => {
    const apiModule = await loadPublicApiClientComposition()
    let session = { accessToken: 'expired-token', sessionGeneration: 'G1' }
    let releaseRefresh: ((token: string) => void) | undefined
    const refreshResult = new Promise<string>((resolve) => {
      releaseRefresh = resolve
    })
    const refreshAccessToken = vi.fn(async () => {
      const token = await refreshResult
      session = { ...session, accessToken: token }
    })
    const clearSession = vi.fn()
    const fetchImplementation = vi.fn<typeof fetch>(async (_input, init) => {
      const authorization = new Headers(init?.headers).get('Authorization')
      return authorization === 'Bearer refreshed-token'
        ? jsonResponse(200, { ok: true })
        : canonicalError(401, 'UNAUTHORIZED')
    })
    apiModule.configureApiClient(
      createAuthSessionAdapter({
        getSessionSnapshot: () => ({ ...session }),
        refreshAccessToken,
        clearSession,
      }),
      { fetchImplementation },
    )
    const client = apiModule.getApiClient()

    const requests = Promise.all([
      client.request<{ ok: boolean }>('/admin/users'),
      client.request<{ ok: boolean }>('/admin/vocabulary'),
    ])

    await vi.waitFor(() => expect(refreshAccessToken).toHaveBeenCalledTimes(1))
    expect(fetchImplementation).toHaveBeenCalledTimes(2)
    releaseRefresh?.('refreshed-token')

    await expect(requests).resolves.toEqual([{ ok: true }, { ok: true }])
    expect(refreshAccessToken).toHaveBeenCalledTimes(1)
    expect(refreshAccessToken).toHaveBeenCalledWith('G1')
    expect(fetchImplementation).toHaveBeenCalledTimes(4)
    expect(clearSession).not.toHaveBeenCalled()
    const retryHeaders = fetchImplementation.mock.calls.slice(2).map(([, init]) => new Headers(init?.headers))
    expect(retryHeaders.every((headers) => headers.get('Authorization') === 'Bearer refreshed-token')).toBe(true)
  })

  it('không refresh lần hai khi 401 cũ đến sau khi token đã được thay mới', async () => {
    const apiModule = await loadPublicApiClientComposition()
    const firstOriginalResponse = createDeferred<Response>()
    const delayedOriginalResponse = createDeferred<Response>()
    let session = { accessToken: 'old-token', sessionGeneration: 'G1' }
    const refreshAccessToken = vi.fn(async () => {
      session = { ...session, accessToken: 'refreshed-1' }
    })
    const retryAuthorizations: string[] = []
    const fetchImplementation = vi.fn<typeof fetch>(async (input, init) => {
      const path = String(input)
      const authorization = new Headers(init?.headers).get('Authorization') ?? ''

      if (authorization === 'Bearer old-token') {
        return path.endsWith('/admin/users')
          ? firstOriginalResponse.promise
          : delayedOriginalResponse.promise
      }

      retryAuthorizations.push(authorization)
      return jsonResponse(200, { ok: true })
    })
    apiModule.configureApiClient(
      createAuthSessionAdapter({
        getSessionSnapshot: () => ({ ...session }),
        refreshAccessToken,
      }),
      { fetchImplementation },
    )
    const client = apiModule.getApiClient()

    const firstRequest = client.request<{ ok: boolean }>('/admin/users')
    const delayedRequest = client.request<{ ok: boolean }>('/admin/vocabulary')
    await vi.waitFor(() => expect(fetchImplementation).toHaveBeenCalledTimes(2))

    firstOriginalResponse.resolve(canonicalError(401, 'UNAUTHORIZED'))
    await expect(firstRequest).resolves.toEqual({ ok: true })
    expect(refreshAccessToken).toHaveBeenCalledTimes(1)
    expect(refreshAccessToken).toHaveBeenCalledWith('G1')
    expect(session.accessToken).toBe('refreshed-1')
    expect(session.sessionGeneration).toBe('G1')

    delayedOriginalResponse.resolve(canonicalError(401, 'UNAUTHORIZED'))
    await expect(delayedRequest).resolves.toEqual({ ok: true })

    expect(refreshAccessToken).toHaveBeenCalledTimes(1)
    expect(fetchImplementation).toHaveBeenCalledTimes(4)
    expect(retryAuthorizations).toEqual(['Bearer refreshed-1', 'Bearer refreshed-1'])
  })

  it('không replay request cũ bằng token của session thay thế', async () => {
    const apiModule = await loadPublicApiClientComposition()
    const oldResponse = createDeferred<Response>()
    let session = { accessToken: 'token-A', sessionGeneration: 'G1' }
    const refreshAccessToken = vi.fn()
    const clearSession = vi.fn()
    const attachedTokens: string[] = []
    const fetchImplementation = vi.fn<typeof fetch>(async (_input, init) => {
      attachedTokens.push(new Headers(init?.headers).get('Authorization') ?? '')
      return oldResponse.promise
    })
    apiModule.configureApiClient(
      createAuthSessionAdapter({
        getSessionSnapshot: () => ({ ...session }),
        refreshAccessToken,
        clearSession,
      }),
      { fetchImplementation },
    )

    const oldRequest = apiModule.getApiClient().request('/admin/users', {
      method: 'POST',
      body: { status: 'LOCKED' },
    })
    await vi.waitFor(() => expect(fetchImplementation).toHaveBeenCalledTimes(1))
    session = { accessToken: 'token-B', sessionGeneration: 'G2' }
    oldResponse.resolve(canonicalError(401, 'UNAUTHORIZED'))

    await expect(oldRequest).rejects.toMatchObject({ name: 'ApiSessionChangedError' })
    expect(fetchImplementation).toHaveBeenCalledTimes(1)
    expect(attachedTokens).toEqual(['Bearer token-A'])
    expect(attachedTokens).not.toContain('Bearer token-B')
    expect(refreshAccessToken).not.toHaveBeenCalled()
    expect(clearSession).not.toHaveBeenCalled()
  })

  it('không hồi sinh session đã logout khi 401 cũ đến muộn', async () => {
    const apiModule = await loadPublicApiClientComposition()
    const oldResponse = createDeferred<Response>()
    let session: { accessToken: string | null; sessionGeneration: string } = {
      accessToken: 'token-A',
      sessionGeneration: 'G1',
    }
    const refreshAccessToken = vi.fn()
    const clearSession = vi.fn()
    const fetchImplementation = vi.fn<typeof fetch>().mockImplementation(async () => oldResponse.promise)
    apiModule.configureApiClient(
      createAuthSessionAdapter({
        getSessionSnapshot: () => ({ ...session }),
        refreshAccessToken,
        clearSession,
      }),
      { fetchImplementation },
    )

    const oldRequest = apiModule.getApiClient().request('/admin/users')
    await vi.waitFor(() => expect(fetchImplementation).toHaveBeenCalledTimes(1))
    session = { accessToken: null, sessionGeneration: 'G2' }
    oldResponse.resolve(canonicalError(401, 'UNAUTHORIZED'))

    await expect(oldRequest).rejects.toMatchObject({ name: 'ApiSessionChangedError' })
    expect(fetchImplementation).toHaveBeenCalledTimes(1)
    expect(refreshAccessToken).not.toHaveBeenCalled()
    expect(clearSession).not.toHaveBeenCalled()
  })

  it('không retry hoặc clear session mới khi session đổi trong lúc refresh thành công', async () => {
    const apiModule = await loadPublicApiClientComposition()
    const refreshBarrier = createDeferred<void>()
    let session = { accessToken: 'token-A', sessionGeneration: 'G1' }
    const refreshAccessToken = vi.fn(async (expectedGeneration: string | number) => {
      await refreshBarrier.promise
      if (session.sessionGeneration === expectedGeneration) {
        session = { ...session, accessToken: 'refreshed-A' }
      }
    })
    const clearSession = vi.fn()
    const fetchImplementation = vi.fn<typeof fetch>().mockResolvedValue(canonicalError(401, 'UNAUTHORIZED'))
    apiModule.configureApiClient(
      createAuthSessionAdapter({
        getSessionSnapshot: () => ({ ...session }),
        refreshAccessToken,
        clearSession,
      }),
      { fetchImplementation },
    )

    const oldRequest = apiModule.getApiClient().request('/admin/users')
    await vi.waitFor(() => expect(refreshAccessToken).toHaveBeenCalledWith('G1'))
    session = { accessToken: 'token-B', sessionGeneration: 'G2' }
    refreshBarrier.resolve()

    await expect(oldRequest).rejects.toMatchObject({ name: 'ApiSessionChangedError' })
    expect(session).toEqual({ accessToken: 'token-B', sessionGeneration: 'G2' })
    expect(fetchImplementation).toHaveBeenCalledTimes(1)
    expect(clearSession).not.toHaveBeenCalled()
  })

  it('không clear session mới khi refresh của session cũ thất bại muộn', async () => {
    const apiModule = await loadPublicApiClientComposition()
    const refreshBarrier = createDeferred<void>()
    let session = { accessToken: 'token-A', sessionGeneration: 'G1' }
    const refreshAccessToken = vi.fn(async () => {
      await refreshBarrier.promise
      throw new Error('refresh failed')
    })
    const clearSession = vi.fn()
    const fetchImplementation = vi.fn<typeof fetch>().mockResolvedValue(canonicalError(401, 'UNAUTHORIZED'))
    apiModule.configureApiClient(
      createAuthSessionAdapter({
        getSessionSnapshot: () => ({ ...session }),
        refreshAccessToken,
        clearSession,
      }),
      { fetchImplementation },
    )

    const oldRequest = apiModule.getApiClient().request('/admin/users')
    await vi.waitFor(() => expect(refreshAccessToken).toHaveBeenCalledWith('G1'))
    session = { accessToken: 'token-B', sessionGeneration: 'G2' }
    refreshBarrier.resolve()

    await expect(oldRequest).rejects.toMatchObject({ name: 'ApiSessionChangedError' })
    expect(session).toEqual({ accessToken: 'token-B', sessionGeneration: 'G2' })
    expect(fetchImplementation).toHaveBeenCalledTimes(1)
    expect(clearSession).not.toHaveBeenCalled()
  })

  it('không cho session mới join refresh coordinator của session cũ', async () => {
    const apiModule = await loadPublicApiClientComposition()
    const oldRefreshBarrier = createDeferred<void>()
    let session = { accessToken: 'token-A', sessionGeneration: 'G1' }
    const refreshAccessToken = vi.fn(async (expectedGeneration: string | number) => {
      if (expectedGeneration === 'G1') {
        await oldRefreshBarrier.promise
        return
      }

      if (session.sessionGeneration === expectedGeneration) {
        session = { ...session, accessToken: 'refreshed-B' }
      }
    })
    const clearSession = vi.fn()
    const fetchImplementation = vi.fn<typeof fetch>(async (_input, init) => {
      const authorization = new Headers(init?.headers).get('Authorization')
      return authorization === 'Bearer refreshed-B'
        ? jsonResponse(200, { ok: true })
        : canonicalError(401, 'UNAUTHORIZED')
    })
    apiModule.configureApiClient(
      createAuthSessionAdapter({
        getSessionSnapshot: () => ({ ...session }),
        refreshAccessToken,
        clearSession,
      }),
      { fetchImplementation },
    )
    const client = apiModule.getApiClient()

    const oldRequest = client.request('/admin/users')
    await vi.waitFor(() => expect(refreshAccessToken).toHaveBeenCalledWith('G1'))
    session = { accessToken: 'token-B', sessionGeneration: 'G2' }

    await expect(client.request('/admin/vocabulary')).resolves.toEqual({ ok: true })
    expect(refreshAccessToken).toHaveBeenCalledWith('G2')
    expect(refreshAccessToken).toHaveBeenCalledTimes(2)

    oldRefreshBarrier.resolve()
    await expect(oldRequest).rejects.toMatchObject({ name: 'ApiSessionChangedError' })
    expect(session).toEqual({ accessToken: 'refreshed-B', sessionGeneration: 'G2' })
    expect(clearSession).not.toHaveBeenCalled()
  })

  it('xóa session và dừng sạch khi refresh thất bại', async () => {
    const apiModule = await loadPublicApiClientComposition()
    const refreshAccessToken = vi.fn().mockRejectedValue(new Error('refresh failed'))
    const clearSession = vi.fn()
    const fetchImplementation = vi.fn<typeof fetch>().mockResolvedValue(canonicalError(401, 'UNAUTHORIZED'))
    apiModule.configureApiClient(
      createAuthSessionAdapter({
        getSessionSnapshot: () => ({ accessToken: 'expired-token', sessionGeneration: 'G1' }),
        refreshAccessToken,
        clearSession,
      }),
      { fetchImplementation },
    )

    await expect(apiModule.getApiClient().request('/admin/users')).rejects.toMatchObject({
      status: 401,
      code: 'UNAUTHORIZED',
    })
    expect(fetchImplementation).toHaveBeenCalledTimes(1)
    expect(refreshAccessToken).toHaveBeenCalledTimes(1)
    expect(clearSession).toHaveBeenCalledTimes(1)
    expect(clearSession).toHaveBeenCalledWith('G1')
  })

  it('xóa session và không mở refresh cycle thứ hai khi retry vẫn trả 401', async () => {
    const apiModule = await loadPublicApiClientComposition()
    const clearSession = vi.fn()
    const fetchImplementation = vi.fn<typeof fetch>().mockResolvedValue(canonicalError(401, 'UNAUTHORIZED'))
    let session = { accessToken: 'expired-token', sessionGeneration: 'G1' }
    const refreshAccessToken = vi.fn(async () => {
      session = { ...session, accessToken: 'refreshed-token' }
    })
    apiModule.configureApiClient(
      createAuthSessionAdapter({
        getSessionSnapshot: () => ({ ...session }),
        refreshAccessToken,
        clearSession,
      }),
      { fetchImplementation },
    )

    await expect(apiModule.getApiClient().request('/admin/users')).rejects.toMatchObject({
      status: 401,
      code: 'UNAUTHORIZED',
    })
    expect(fetchImplementation).toHaveBeenCalledTimes(2)
    expect(refreshAccessToken).toHaveBeenCalledTimes(1)
    expect(clearSession).toHaveBeenCalledTimes(1)
    expect(clearSession).toHaveBeenCalledWith('G1')
  })

  it('giữ nguyên 403 và không kích hoạt refresh', async () => {
    const apiModule = await loadPublicApiClientComposition()
    const refreshAccessToken = vi.fn()
    const clearSession = vi.fn()
    apiModule.configureApiClient(
      createAuthSessionAdapter({ refreshAccessToken, clearSession }),
      { fetchImplementation: vi.fn<typeof fetch>().mockResolvedValue(canonicalError(403, 'FORBIDDEN')) },
    )

    await expect(apiModule.getApiClient().request('/admin/users')).rejects.toMatchObject({
      status: 403,
      code: 'FORBIDDEN',
    })
    expect(refreshAccessToken).not.toHaveBeenCalled()
    expect(clearSession).not.toHaveBeenCalled()
  })
})
