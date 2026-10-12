import { useRef, useState } from 'react'
import { useAdminAuth } from './useAdminAuth'
import { ApiError, ApiSessionChangedError } from '../../services/api/apiError'
import { viMessages } from '../../constants/messages.vi'

export function useAdminLogin() {
  const { session } = useAdminAuth()
  const pending = useRef(false)
  const [error, setError] = useState<string | null>(null)
  async function login(email: string, password: string) {
    if (pending.current) return
    pending.current = true
    setError(null)
    try { await session.login(email, password) }
    catch (failure) {
      if (failure instanceof ApiSessionChangedError) return
      const code = failure instanceof ApiError ? failure.code : null
      setError(code === 'AUTH_INVALID_CREDENTIALS' ? viMessages.auth.invalidCredentials
        : code === 'AUTH_ACCOUNT_LOCKED' ? viMessages.auth.locked
          : failure instanceof ApiError && failure.status === 429 ? viMessages.auth.rateLimited
            : viMessages.auth.loginFailure)
    } finally { pending.current = false }
  }
  return { login, error }
}
