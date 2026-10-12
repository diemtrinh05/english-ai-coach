import { useAdminAuth } from './useAdminAuth'
import { AdminBootstrapShell } from '../../components/layout/AdminBootstrapShell'

export function AdminSessionShell() {
  const { session } = useAdminAuth()
  return <AdminBootstrapShell onLogout={() => { void session.logout() }} />
}
