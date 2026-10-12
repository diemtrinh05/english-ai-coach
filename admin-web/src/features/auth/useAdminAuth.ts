import { createContext, useContext, useSyncExternalStore } from 'react'
import type { AdminAuthSession } from '../../services/auth/AdminAuthSession'

export const AuthContext = createContext<AdminAuthSession | null>(null)

export function useAdminAuth() {
  const session = useContext(AuthContext)
  if (!session) throw new Error('AuthProvider required')
  const view = useSyncExternalStore(session.subscribe, session.getView, session.getView)
  return { session, ...view }
}
