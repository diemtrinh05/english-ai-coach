import { useQuery, type QueryKey, type UseQueryResult } from '@tanstack/react-query'
import type {
  ApiError,
  ApiNetworkError,
  ApiSessionChangedError,
} from '../services/api/apiError'

export type ApiRequestFailure = ApiError | ApiNetworkError | ApiSessionChangedError

interface ApiQueryOptions<TData> {
  queryKey: QueryKey
  queryFn: () => Promise<TData>
  enabled?: boolean
}

export function useApiQuery<TData>(options: ApiQueryOptions<TData>): UseQueryResult<TData, ApiRequestFailure> {
  return useQuery<TData, ApiRequestFailure>({
    queryKey: options.queryKey,
    queryFn: options.queryFn,
    enabled: options.enabled,
  })
}
