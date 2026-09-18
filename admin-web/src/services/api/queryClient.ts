import { QueryClient } from '@tanstack/react-query'
import { isRetryableApiFailure } from './apiError'

export function createAdminQueryClient(): QueryClient {
  return new QueryClient({
    defaultOptions: {
      queries: {
        retry: (failureCount, error) => failureCount < 1 && isRetryableApiFailure(error),
        refetchOnWindowFocus: false,
        staleTime: 30_000,
      },
      mutations: {
        retry: false,
      },
    },
  })
}

export const adminQueryClient = createAdminQueryClient()
