import type { MutationViewState, QueryViewState } from '../types/api'

interface QueryStateInput<TData> {
  data: TData | undefined
  isError: boolean
  isPending: boolean
}

interface MutationStateInput {
  isError: boolean
  isIdle: boolean
  isPending: boolean
  isSuccess: boolean
}

export function resolveQueryState<TData>(
  query: QueryStateInput<TData>,
  isEmpty: (data: TData) => boolean,
): QueryViewState {
  if (query.isPending) return 'loading'
  if (query.isError) return 'error'
  if (query.data === undefined) return 'initial'
  return isEmpty(query.data) ? 'empty' : 'success'
}

export function resolveMutationState(mutation: MutationStateInput): MutationViewState {
  if (mutation.isPending) return 'submitting'
  if (mutation.isError) return 'failure'
  if (mutation.isSuccess) return 'success'
  return 'idle'
}
