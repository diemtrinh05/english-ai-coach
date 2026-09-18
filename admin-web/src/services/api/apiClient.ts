import type {
  AuthSessionAdapter,
  AuthSessionGeneration,
} from '../auth/AuthSessionAdapter'
import { ApiNetworkError, ApiSessionChangedError, toApiError } from './apiError'

export type ApiRequestBody = Record<string, unknown> | readonly unknown[] | string | number | boolean | null

export interface ApiRequestOptions extends Omit<RequestInit, 'body' | 'headers'> {
  body?: ApiRequestBody
  headers?: HeadersInit
}

export interface ApiClient {
  request: <TResponse>(path: string, options?: ApiRequestOptions) => Promise<TResponse>
}

export interface CreateApiClientOptions {
  auth: AuthSessionAdapter
  baseUrl?: string
  fetchImplementation?: typeof fetch
  timeoutMs?: number
}

export type ConfigureApiClientOptions = Omit<CreateApiClientOptions, 'auth'>

const DEFAULT_API_BASE_URL = '/api/v1'
const DEFAULT_TIMEOUT_MS = 15_000

function trimTrailingSlash(value: string): string {
  return value.endsWith('/') ? value.slice(0, -1) : value
}

function normalizePath(path: string): string {
  return path.startsWith('/') ? path : `/${path}`
}

function hasJsonBody(body: ApiRequestBody | undefined): body is ApiRequestBody {
  return body !== undefined
}

export function createApiClient(options: CreateApiClientOptions): ApiClient {
  const baseUrl = trimTrailingSlash(options.baseUrl ?? import.meta.env.VITE_API_BASE_URL ?? DEFAULT_API_BASE_URL)
  const fetchImplementation = options.fetchImplementation ?? globalThis.fetch
  const auth = options.auth
  const timeoutMs = options.timeoutMs ?? DEFAULT_TIMEOUT_MS
  const refreshBySessionGeneration = new Map<AuthSessionGeneration, Promise<void>>()

  async function refreshAccessToken(sessionGeneration: AuthSessionGeneration): Promise<void> {
    const activeRefresh = refreshBySessionGeneration.get(sessionGeneration)
    if (activeRefresh) {
      return activeRefresh
    }

    const promise = auth.refreshAccessToken(sessionGeneration).finally(() => {
      if (refreshBySessionGeneration.get(sessionGeneration) === promise) {
        refreshBySessionGeneration.delete(sessionGeneration)
      }
    })
    refreshBySessionGeneration.set(sessionGeneration, promise)
    return promise
  }

  async function clearSessionIfCurrent(sessionGeneration: AuthSessionGeneration): Promise<void> {
    if (auth.getSessionSnapshot().sessionGeneration === sessionGeneration) {
      await auth.clearSession(sessionGeneration)
    }
  }

  async function execute(path: string, requestOptions: ApiRequestOptions, accessToken: string | null): Promise<Response> {
    const { body, headers: headerInit, signal, ...fetchOptions } = requestOptions
    const headers = new Headers(headerInit)
    const abortController = new AbortController()
    const abortFromCaller = () => abortController.abort(signal?.reason)
    const timeoutId = setTimeout(() => abortController.abort(), timeoutMs)
    headers.set('Accept', 'application/json')

    if (accessToken) {
      headers.set('Authorization', `Bearer ${accessToken}`)
    }

    if (hasJsonBody(body)) {
      headers.set('Content-Type', 'application/json')
    }

    if (signal?.aborted) {
      abortFromCaller()
    } else {
      signal?.addEventListener('abort', abortFromCaller, { once: true })
    }

    try {
      return await fetchImplementation(`${baseUrl}${normalizePath(path)}`, {
        ...fetchOptions,
        body: hasJsonBody(body) ? JSON.stringify(body) : undefined,
        headers,
        signal: abortController.signal,
      })
    } catch (error) {
      throw new ApiNetworkError(error)
    } finally {
      clearTimeout(timeoutId)
      signal?.removeEventListener('abort', abortFromCaller)
    }
  }

  async function parseSuccess<TResponse>(response: Response): Promise<TResponse> {
    if (response.status === 204) {
      return undefined as TResponse
    }

    const text = await response.text()
    return (text ? JSON.parse(text) : undefined) as TResponse
  }

  return {
    async request<TResponse>(path: string, requestOptions: ApiRequestOptions = {}): Promise<TResponse> {
      const requestSession = auth.getSessionSnapshot()
      let response = await execute(path, requestOptions, requestSession.accessToken)

      if (response.status === 401) {
        const currentSession = auth.getSessionSnapshot()
        let retryToken: string | null = null

        if (currentSession.sessionGeneration !== requestSession.sessionGeneration) {
          throw new ApiSessionChangedError()
        }

        if (currentSession.accessToken !== requestSession.accessToken) {
          retryToken = currentSession.accessToken
        } else {
          try {
            await refreshAccessToken(requestSession.sessionGeneration)
          } catch {
            if (auth.getSessionSnapshot().sessionGeneration !== requestSession.sessionGeneration) {
              throw new ApiSessionChangedError()
            }
            await clearSessionIfCurrent(requestSession.sessionGeneration)
            throw await toApiError(response)
          }

          const refreshedSession = auth.getSessionSnapshot()
          if (refreshedSession.sessionGeneration !== requestSession.sessionGeneration) {
            throw new ApiSessionChangedError()
          }
          retryToken = refreshedSession.accessToken
        }

        if (!retryToken) {
          await clearSessionIfCurrent(requestSession.sessionGeneration)
          throw await toApiError(response)
        }

        response = await execute(path, requestOptions, retryToken)

        if (response.status === 401) {
          if (auth.getSessionSnapshot().sessionGeneration !== requestSession.sessionGeneration) {
            throw new ApiSessionChangedError()
          }
          await clearSessionIfCurrent(requestSession.sessionGeneration)
        }
      }

      if (!response.ok) {
        throw await toApiError(response)
      }

      return parseSuccess<TResponse>(response)
    },
  }
}

let runtimeApiClient: ApiClient | null = null

function assertAuthSessionAdapter(auth: AuthSessionAdapter): void {
  if (
    !auth ||
    typeof auth.getSessionSnapshot !== 'function' ||
    typeof auth.refreshAccessToken !== 'function' ||
    typeof auth.clearSession !== 'function'
  ) {
    throw new Error('AuthSessionAdapter is required to configure the API client')
  }
}

export function configureApiClient(
  auth: AuthSessionAdapter,
  options: ConfigureApiClientOptions = {},
): void {
  assertAuthSessionAdapter(auth)

  if (runtimeApiClient) {
    throw new Error('API client has already been configured')
  }

  runtimeApiClient = createApiClient({ ...options, auth })
}

export function getApiClient(): ApiClient {
  if (!runtimeApiClient) {
    throw new Error('API client must be configured with an AuthSessionAdapter before use')
  }

  return runtimeApiClient
}
