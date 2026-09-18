import { describe, expect, it } from 'vitest'
import { resolveMutationState, resolveQueryState } from './apiState'

describe('API state helpers', () => {
  it('chuẩn hóa query state', () => {
    const isEmpty = (items: readonly string[]) => items.length === 0

    expect(resolveQueryState({ data: undefined, isError: false, isPending: true }, isEmpty)).toBe('loading')
    expect(resolveQueryState({ data: undefined, isError: true, isPending: false }, isEmpty)).toBe('error')
    expect(resolveQueryState({ data: undefined, isError: false, isPending: false }, isEmpty)).toBe('initial')
    expect(resolveQueryState({ data: [], isError: false, isPending: false }, isEmpty)).toBe('empty')
    expect(resolveQueryState({ data: ['item'], isError: false, isPending: false }, isEmpty)).toBe('success')
  })

  it('chuẩn hóa mutation state', () => {
    expect(resolveMutationState({ isError: false, isIdle: true, isPending: false, isSuccess: false })).toBe('idle')
    expect(resolveMutationState({ isError: false, isIdle: false, isPending: true, isSuccess: false })).toBe(
      'submitting',
    )
    expect(resolveMutationState({ isError: false, isIdle: false, isPending: false, isSuccess: true })).toBe('success')
    expect(resolveMutationState({ isError: true, isIdle: false, isPending: false, isSuccess: false })).toBe('failure')
  })
})
