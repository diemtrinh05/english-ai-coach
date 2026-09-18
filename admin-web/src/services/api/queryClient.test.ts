import { describe, expect, it } from 'vitest'
import { ApiError, ApiNetworkError } from './apiError'
import { createAdminQueryClient } from './queryClient'

describe('createAdminQueryClient', () => {
  it('chỉ retry safe query một lần cho transient failure', () => {
    const options = createAdminQueryClient().getDefaultOptions()
    const retry = options.queries?.retry

    expect(typeof retry).toBe('function')
    if (typeof retry !== 'function') return

    expect(retry(0, new ApiNetworkError(new Error('offline')))).toBe(true)
    expect(retry(1, new ApiNetworkError(new Error('offline')))).toBe(false)
    expect(retry(0, new ApiError({ status: 503, message: 'Unavailable' }))).toBe(true)
    expect(retry(0, new ApiError({ status: 403, message: 'Forbidden' }))).toBe(false)
  })

  it('không tự retry mutation', () => {
    expect(createAdminQueryClient().getDefaultOptions().mutations?.retry).toBe(false)
  })
})
