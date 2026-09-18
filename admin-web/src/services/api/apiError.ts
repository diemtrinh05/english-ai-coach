import type { ErrorResponse } from '../../types/api'

export class ApiError extends Error {
  readonly status: number
  readonly code: string | null
  readonly path: string | null
  readonly details: readonly Record<string, unknown>[]

  constructor(response: Partial<ErrorResponse> & Pick<ErrorResponse, 'status' | 'message'>) {
    super(response.message)
    this.name = 'ApiError'
    this.status = response.status
    this.code = response.code ?? null
    this.path = response.path ?? null
    this.details = response.details ?? []
  }
}

export class ApiNetworkError extends Error {
  readonly cause: unknown

  constructor(cause: unknown) {
    super('Network request failed')
    this.name = 'ApiNetworkError'
    this.cause = cause
  }
}

export class ApiSessionChangedError extends Error {
  constructor() {
    super('Authenticated session changed while request was in flight')
    this.name = 'ApiSessionChangedError'
  }
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null && !Array.isArray(value)
}

function parseDetails(value: unknown): readonly Record<string, unknown>[] {
  return Array.isArray(value) ? value.filter(isRecord) : []
}

export async function toApiError(response: Response): Promise<ApiError> {
  let payload: unknown

  try {
    payload = await response.json()
  } catch {
    payload = null
  }

  const body = isRecord(payload) ? payload : {}
  const message = typeof body.message === 'string' ? body.message : response.statusText

  return new ApiError({
    status: response.status,
    message: message || `HTTP ${response.status}`,
    code: typeof body.code === 'string' ? body.code : undefined,
    path: typeof body.path === 'string' ? body.path : undefined,
    timestamp: typeof body.timestamp === 'string' ? body.timestamp : undefined,
    details: parseDetails(body.details),
  })
}

export function isRetryableApiFailure(error: unknown): boolean {
  return error instanceof ApiNetworkError || (error instanceof ApiError && error.status >= 500)
}
