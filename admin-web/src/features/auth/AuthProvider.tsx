import { type PropsWithChildren } from 'react'
import type { AdminAuthSession } from '../../services/auth/AdminAuthSession'
import { AuthContext } from './useAdminAuth'

export function AuthProvider({ session, children }: PropsWithChildren<{ session: AdminAuthSession }>) {
  return <AuthContext.Provider value={session}>{children}</AuthContext.Provider>
}
