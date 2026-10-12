import type { PropsWithChildren } from 'react'
import { QueryClientProvider } from '@tanstack/react-query'
import { BrowserRouter } from 'react-router-dom'
import { adminQueryClient } from '../../services/api/queryClient'
import { AuthProvider } from '../../features/auth/AuthProvider'
import { adminAuthSession } from '../../services/auth/authComposition'

export function AppProviders({ children }: PropsWithChildren) {
  return (
    <QueryClientProvider client={adminQueryClient}>
      <AuthProvider session={adminAuthSession}><BrowserRouter>{children}</BrowserRouter></AuthProvider>
    </QueryClientProvider>
  )
}
