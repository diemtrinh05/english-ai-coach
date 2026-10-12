import { useState, type FormEvent } from 'react'
import { Navigate } from 'react-router-dom'
import { viMessages } from '../../constants/messages.vi'
import { useAdminAuth } from './useAdminAuth'
import { useAdminLogin } from './useAdminLogin'

export function LoginPage() {
  const { phase, user, notice } = useAdminAuth()
  const { login, error } = useAdminLogin()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [validation, setValidation] = useState<{ email?: string; password?: string }>({})
  if ((phase === 'authenticated' || phase === 'forbidden') && user) return <Navigate to="/admin" replace />
  const busy = phase === 'authenticating' || phase === 'logging-out'
  function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (busy) return
    const errors = {
      email: !email.trim() ? viMessages.auth.emailRequired : !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email) ? viMessages.auth.emailInvalid : undefined,
      password: !password ? viMessages.auth.passwordRequired : password.length > 100 ? viMessages.auth.passwordInvalid : undefined,
    }
    setValidation(errors)
    if (errors.email || errors.password) return
    const submitted = password
    setPassword('')
    void login(email, submitted)
  }
  return <main className="auth-page">
    <section className="auth-card" aria-labelledby="login-title">
      <p>{viMessages.app.name}</p>
      <h1 id="login-title">{viMessages.auth.loginTitle}</h1>
      <p>{viMessages.auth.memoryNotice}</p>
      {notice !== 'none' && <p role="status">{notice === 'expired' ? viMessages.feedback.unauthorized : viMessages.auth.logoutUnconfirmed}</p>}
      <form onSubmit={submit} noValidate aria-busy={busy}>
        <label htmlFor="admin-email">{viMessages.auth.email}</label>
        <input id="admin-email" type="email" autoComplete="username" value={email} disabled={busy}
          onChange={(event) => setEmail(event.target.value)} aria-invalid={!!validation.email} aria-describedby={validation.email ? 'email-error' : undefined} />
        {validation.email && <p id="email-error" className="auth-error" role="alert">{validation.email}</p>}
        <label htmlFor="admin-password">{viMessages.auth.password}</label>
        <input id="admin-password" type="password" autoComplete="current-password" value={password} disabled={busy}
          onChange={(event) => setPassword(event.target.value)} aria-invalid={!!validation.password} aria-describedby={validation.password ? 'password-error' : undefined} />
        {validation.password && <p id="password-error" className="auth-error" role="alert">{validation.password}</p>}
        {error && <p className="auth-error" role="alert">{error}</p>}
        <button type="submit" disabled={busy}>{busy ? viMessages.auth.signingIn : viMessages.auth.signIn}</button>
      </form>
    </section>
  </main>
}
