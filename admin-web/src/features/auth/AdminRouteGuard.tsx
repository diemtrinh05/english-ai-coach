import { Navigate, Outlet } from 'react-router-dom'
import { viMessages } from '../../constants/messages.vi'
import { useAdminAuth } from './useAdminAuth'

export function AdminRouteGuard() {
  const { session, phase, user } = useAdminAuth()
  if (phase === 'authenticating' || phase === 'logging-out') {
    return <main className="auth-page"><p role="status">{viMessages.auth.waiting}</p></main>
  }
  if (!user) return <Navigate to="/login" replace />
  if (phase === 'forbidden' || user.role !== 'ADMIN' || user.status !== 'ACTIVE') {
    return <main className="auth-page"><section className="auth-card" aria-labelledby="permission-title">
      <h1 id="permission-title">403</h1>
      <p>{viMessages.auth.permission}</p>
      <button onClick={() => { void session.logout() }}>{viMessages.auth.signOut}</button>
    </section></main>
  }
  return <Outlet />
}
