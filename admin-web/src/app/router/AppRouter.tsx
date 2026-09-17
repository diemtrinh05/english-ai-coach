import { Navigate, Route, Routes } from 'react-router-dom'
import { AdminBootstrapShell } from '../../components/layout/AdminBootstrapShell'

export function AppRouter() {
  return (
    <Routes>
      <Route path="/admin" element={<AdminBootstrapShell />} />
      <Route path="*" element={<Navigate to="/admin" replace />} />
    </Routes>
  )
}
