import { describe, expect, it, vi } from 'vitest'
import type { AuthSessionAdapter } from '../auth/AuthSessionAdapter'
import { createApiClient } from './apiClient'
import { ApiError, ApiNetworkError } from './apiError'

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

function createAuthSessionAdapter(): AuthSessionAdapter {
  return {
    getSessionSnapshot: () => ({ accessToken: null, sessionGeneration: 'anonymous-1' }),
    refreshAccessToken: vi.fn().mockResolvedValue(undefined),
    clearSession: vi.fn(),
  }
}

describe('apiClient', () => {
  it('gắn Bearer token và parse JSON response', async () => {
    const fetchImplementation = vi.fn<typeof fetch>().mockResolvedValue(
      jsonResponse(200, { id: 'user-1' }),
    )
    const auth: AuthSessionAdapter = {
      getSessionSnapshot: () => ({ accessToken: 'access-token', sessionGeneration: 'G1' }),
      refreshAccessToken: vi.fn(),
      clearSession: vi.fn(),
    }
    const client = createApiClient({ baseUrl: '/api/v1', fetchImplementation, auth })

    await expect(client.request<{ id: string }>('/admin/users/user-1')).resolves.toEqual({
      id: 'user-1',
    })

    const [, init] = fetchImplementation.mock.calls[0] ?? []
    expect(new Headers(init?.headers).get('Authorization')).toBe('Bearer access-token')
  })

  it('serialize concurrent refresh và retry đúng một lần với token mới', async () => {
    let session = { accessToken: 'expired-token', sessionGeneration: 'G1' }
    let releaseRefresh: ((token: string) => void) | undefined
    const refreshedToken = new Promise<string>((resolve) => {
      releaseRefresh = resolve
    })
    const refreshAccessToken = vi.fn(async () => {
      const token = await refreshedToken
      session = { ...session, accessToken: token }
    })
    const clearSession = vi.fn()
    const fetchImplementation = vi.fn<typeof fetch>(async (_input, init) => {
      const authorization = new Headers(init?.headers).get('Authorization')
      return authorization === 'Bearer refreshed-token'
        ? jsonResponse(200, { ok: true })
        : canonicalError(401, 'UNAUTHORIZED')
    })
    const client = createApiClient({
      baseUrl: '/api/v1',
      fetchImplementation,
      auth: {
        getSessionSnapshot: () => ({ ...session }),
        refreshAccessToken,
        clearSession,
      },
    })

    const requests = Promise.all([
      client.request<{ ok: boolean }>('/admin/users'),
      client.request<{ ok: boolean }>('/admin/vocabulary'),
    ])

    await vi.waitFor(() => expect(refreshAccessToken).toHaveBeenCalledTimes(1))
    releaseRefresh?.('refreshed-token')

    await expect(requests).resolves.toEqual([{ ok: true }, { ok: true }])
    expect(refreshAccessToken).toHaveBeenCalledTimes(1)
    expect(refreshAccessToken).toHaveBeenCalledWith('G1')
    expect(clearSession).not.toHaveBeenCalled()
  })

  it('xóa session và giữ canonical 401 khi refresh thất bại', async () => {
    const clearSession = vi.fn()
    const client = createApiClient({
      fetchImplementation: vi.fn<typeof fetch>().mockResolvedValue(canonicalError(401, 'UNAUTHORIZED')),
      auth: {
        getSessionSnapshot: () => ({ accessToken: 'expired-token', sessionGeneration: 'G1' }),
        refreshAccessToken: vi.fn().mockRejectedValue(new Error('refresh failed')),
        clearSession,
      },
    })

    await expect(client.request('/admin/users')).rejects.toMatchObject({
      status: 401,
      code: 'UNAUTHORIZED',
    })
    expect(clearSession).toHaveBeenCalledTimes(1)
    expect(clearSession).toHaveBeenCalledWith('G1')
  })

  it('không lặp refresh khi request retry vẫn trả 401', async () => {
    const clearSession = vi.fn()
    const fetchImplementation = vi.fn<typeof fetch>().mockResolvedValue(canonicalError(401, 'UNAUTHORIZED'))
    let session = { accessToken: 'expired-token', sessionGeneration: 'G1' }
    const refreshAccessToken = vi.fn(async () => {
      session = { ...session, accessToken: 'refreshed-token' }
    })
    const client = createApiClient({
      fetchImplementation,
      auth: {
        getSessionSnapshot: () => ({ ...session }),
        refreshAccessToken,
        clearSession,
      },
    })

    await expect(client.request('/admin/users')).rejects.toMatchObject({
      status: 401,
      code: 'UNAUTHORIZED',
    })
    expect(fetchImplementation).toHaveBeenCalledTimes(2)
    expect(refreshAccessToken).toHaveBeenCalledTimes(1)
    expect(clearSession).toHaveBeenCalledTimes(1)
    expect(clearSession).toHaveBeenCalledWith('G1')
  })

  it.each([
    [403, 'FORBIDDEN'],
    [409, 'CONCURRENT_UPDATE'],
    [409, 'IDEMPOTENCY_KEY_REUSE'],
  ])('giữ nguyên HTTP %i và error code %s', async (status, code) => {
    const client = createApiClient({
      auth: createAuthSessionAdapter(),
      fetchImplementation: vi.fn<typeof fetch>().mockResolvedValue(canonicalError(status, code)),
    })

    const error = await client.request('/admin/users').catch((caught: unknown) => caught)

    expect(error).toBeInstanceOf(ApiError)
    expect(error).toMatchObject({ status, code })
  })

  it('không retry mutation sau lỗi mạng ở HTTP client', async () => {
    const fetchImplementation = vi.fn<typeof fetch>().mockRejectedValue(new TypeError('offline'))
    const client = createApiClient({ auth: createAuthSessionAdapter(), fetchImplementation })

    await expect(client.request('/admin/users', { method: 'POST', body: { role: 'ADMIN' } })).rejects.toBeInstanceOf(
      ApiNetworkError,
    )
    expect(fetchImplementation).toHaveBeenCalledTimes(1)
  })

  it('serialize JSON nhưng không tự tạo Idempotency-Key', async () => {
    const fetchImplementation = vi.fn<typeof fetch>().mockResolvedValue(jsonResponse(201, { id: 'user-1' }))
    const client = createApiClient({ auth: createAuthSessionAdapter(), fetchImplementation })

    await client.request('/admin/users', { method: 'POST', body: { role: 'ADMIN' } })

    const [, init] = fetchImplementation.mock.calls[0] ?? []
    const headers = new Headers(init?.headers)
    expect(headers.get('Content-Type')).toBe('application/json')
    expect(headers.has('Idempotency-Key')).toBe(false)
    expect(init?.body).toBe(JSON.stringify({ role: 'ADMIN' }))
  })

  it('hủy request vượt quá timeout cấu hình', async () => {
    const fetchImplementation = vi.fn<typeof fetch>((_input, init) => {
      return new Promise<Response>((_resolve, reject) => {
        init?.signal?.addEventListener('abort', () => reject(new DOMException('Aborted', 'AbortError')))
      })
    })
    const client = createApiClient({ auth: createAuthSessionAdapter(), fetchImplementation, timeoutMs: 1 })

    await expect(client.request('/admin/users')).rejects.toBeInstanceOf(ApiNetworkError)
  })

  it('hỗ trợ response 204 không có body', async () => {
    const client = createApiClient({
      auth: createAuthSessionAdapter(),
      fetchImplementation: vi.fn<typeof fetch>().mockResolvedValue(new Response(null, { status: 204 })),
    })

    await expect(client.request<void>('/admin/users/user-1', { method: 'DELETE' })).resolves.toBeUndefined()
  })
})
