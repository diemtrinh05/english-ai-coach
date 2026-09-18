export interface ErrorResponse {
  timestamp: string
  status: number
  code: string
  message: string
  path: string
  details?: readonly Record<string, unknown>[]
}

export interface PaginatedResponse<T> {
  content: readonly T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  hasNext: boolean
}

export type QueryViewState = 'initial' | 'loading' | 'success' | 'empty' | 'error'

export type MutationViewState = 'idle' | 'submitting' | 'success' | 'failure'
