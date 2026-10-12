import { ApiNetworkError, toApiError } from '../api/apiError'

export interface AuthUser {
  id: string
  email: string
  fullName: string
  role: 'USER' | 'ADMIN'
  status: 'ACTIVE' | 'LOCKED'
}

export interface TokenPair {
  accessToken: string
  refreshToken: string
  expiresIn: number
  tokenType: 'Bearer'
}

export interface LoginResponse extends TokenPair { user: AuthUser }

function object(value: unknown): Record<string, unknown> {
  if (!value || typeof value !== 'object' || Array.isArray(value)) throw new Error('Invalid auth response')
  return value as Record<string, unknown>
}

export function parseUser(value: unknown): AuthUser {
  const data = object(value)
  if (typeof data.id !== 'string' || !/^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(data.id)
    || typeof data.email !== 'string' || !data.email || typeof data.fullName !== 'string'
    || (data.role !== 'USER' && data.role !== 'ADMIN') || (data.status !== 'ACTIVE' && data.status !== 'LOCKED')) {
    throw new Error('Invalid auth user')
  }
  return { id: data.id, email: data.email, fullName: data.fullName, role: data.role, status: data.status }
}

export function parsePair(value: unknown): TokenPair {
  const data = object(value)
  if (typeof data.accessToken !== 'string' || !data.accessToken.trim()
    || typeof data.refreshToken !== 'string' || !data.refreshToken.trim()
    || data.tokenType !== 'Bearer' || !Number.isSafeInteger(data.expiresIn) || (data.expiresIn as number) <= 0) {
    throw new Error('Invalid auth token pair')
  }
  return { accessToken: data.accessToken, refreshToken: data.refreshToken, tokenType: 'Bearer', expiresIn: data.expiresIn as number }
}

export function createAuthService(fetchImplementation: typeof fetch = globalThis.fetch, baseUrl = import.meta.env.VITE_API_BASE_URL ?? '/api/v1') {
  async function request(path: string, body?: Record<string, unknown>, accessToken?: string, expectedStatus = 200): Promise<unknown> {
    const controller = new AbortController()
    const timeout = setTimeout(() => controller.abort(), 15_000)
    const headers = new Headers({ Accept: 'application/json' })
    if (body) headers.set('Content-Type', 'application/json')
    if (accessToken) headers.set('Authorization', `Bearer ${accessToken}`)
    try {
      const response = await fetchImplementation(`${baseUrl.replace(/\/$/, '')}${path}`, {
        method: body ? 'POST' : 'GET', headers, body: body ? JSON.stringify(body) : undefined,
        signal: controller.signal, credentials: 'omit', redirect: 'error', cache: 'no-store',
      })
      if (!response.ok) throw await toApiError(response)
      if (response.status !== expectedStatus) throw new Error('Unexpected auth status')
      return response.status === 204 ? undefined : await response.json()
    } catch (error) {
      if (error instanceof TypeError || (error instanceof DOMException && error.name === 'AbortError')) throw new ApiNetworkError(error)
      throw error
    } finally { clearTimeout(timeout) }
  }
  return {
    async login(email: string, password: string): Promise<LoginResponse> {
      const data = object(await request('/auth/login', { email, password }))
      return { ...parsePair(data), user: parseUser(data.user) }
    },
    async refresh(refreshToken: string): Promise<TokenPair> { return parsePair(await request('/auth/refresh', { refreshToken })) },
    async currentUser(accessToken: string): Promise<AuthUser> { return parseUser(await request('/users/me', undefined, accessToken)) },
    async logout(pair: TokenPair): Promise<void> { await request('/auth/logout', { refreshToken: pair.refreshToken }, pair.accessToken, 204) },
  }
}

export type AuthService = ReturnType<typeof createAuthService>
