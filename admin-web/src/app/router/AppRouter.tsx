import { Navigate, Route, Routes } from 'react-router-dom'
import { LoginPage } from '../../features/auth/LoginPage'
import { AdminRouteGuard } from '../../features/auth/AdminRouteGuard'
import { AdminSessionShell } from '../../features/auth/AdminSessionShell'

export function AppRouter() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route element={<AdminRouteGuard />}>
        <Route path="/admin/*" element={<AdminSessionShell />} />
      </Route>
      <Route path="*" element={<Navigate to="/admin" replace />} />
    </Routes>
  )
}
