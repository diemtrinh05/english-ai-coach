import { useMutation, type UseMutationResult } from '@tanstack/react-query'
import type { ApiRequestFailure } from './useApiQuery'

interface ApiMutationOptions<TData, TVariables> {
  mutationFn: (variables: TVariables) => Promise<TData>
}

export function useApiMutation<TData, TVariables>(
  options: ApiMutationOptions<TData, TVariables>,
): UseMutationResult<TData, ApiRequestFailure, TVariables> {
  return useMutation<TData, ApiRequestFailure, TVariables>({
    mutationFn: options.mutationFn,
  })
}
