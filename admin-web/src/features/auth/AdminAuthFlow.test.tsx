import { act, render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import { describe, expect, it, vi } from 'vitest'
import { AdminAuthSession } from '../../services/auth/AdminAuthSession'
import type { AuthService } from '../../services/auth/authService'
import { ApiError } from '../../services/api/apiError'
import { AuthProvider } from './AuthProvider'
import { AppRouter } from '../../app/router/AppRouter'
import { viMessages } from '../../constants/messages.vi'

function fixture(role: 'USER' | 'ADMIN' = 'ADMIN', loginError?: ApiError, path = '/admin/users') {
  const service: AuthService = {
    login: loginError ? vi.fn().mockRejectedValue(loginError) : vi.fn().mockResolvedValue({ accessToken: 'test-access', refreshToken: 'test-refresh', tokenType: 'Bearer', expiresIn: 1800,
      user: { id: '00000000-0000-0000-0000-000000000001', role, status: 'ACTIVE', email: 'a@example.test', fullName: 'Admin' } }),
    refresh: vi.fn(), currentUser: vi.fn(), logout: vi.fn().mockResolvedValue(undefined),
  }
  const session = new AdminAuthSession(service, vi.fn())
  render(<AuthProvider session={session}><MemoryRouter initialEntries={[path]}><AppRouter /></MemoryRouter></AuthProvider>)
  return { service, session }
}

async function submit() {
  const user = userEvent.setup()
  await user.type(screen.getByLabelText(viMessages.auth.email), 'a@example.test')
  await user.type(screen.getByLabelText(viMessages.auth.password), 'test-password')
  await user.click(screen.getByRole('button', { name: viMessages.auth.signIn }))
}

describe('Admin auth accessible routes', () => {
  it('anonymous deep link hiện Login, không private shell', () => {
    fixture()
    expect(screen.getByRole('heading', { name: viMessages.auth.loginTitle })).toBeInTheDocument()
    expect(screen.queryByRole('complementary')).not.toBeInTheDocument()
    expect(screen.getByLabelText(viMessages.auth.password)).toHaveAttribute('type', 'password')
    expect(screen.getByText(viMessages.auth.memoryNotice)).toBeInTheDocument()
  })
  it('validation không gửi request, association errors', async () => {
    const { service } = fixture()
    await userEvent.click(screen.getByRole('button', { name: viMessages.auth.signIn }))
    expect(service.login).not.toHaveBeenCalled()
    expect(screen.getByLabelText(viMessages.auth.email)).toHaveAttribute('aria-describedby', 'email-error')
    expect(screen.getByLabelText(viMessages.auth.password)).toHaveAttribute('aria-invalid', 'true')
  })
  it('ADMIN vào shell tạm và logout trở lại Login', async () => {
    const { service } = fixture('ADMIN', undefined, '/admin/dashboard')
    await submit()
    await screen.findByRole('heading', { name: viMessages.bootstrap.title })
    expect(screen.getByText(viMessages.bootstrap.statusDescription)).toBeInTheDocument()
    await userEvent.click(screen.getByRole('button', { name: viMessages.auth.signOut }))
    await screen.findByRole('heading', { name: viMessages.auth.loginTitle })
    expect(screen.queryByRole('complementary')).not.toBeInTheDocument()
    expect(service.logout).toHaveBeenCalledTimes(1)
  })
  it('USER trả permission403, không protected shell', async () => {
    fixture('USER')
    await submit()
    await screen.findByRole('heading', { name: '403' })
    expect(screen.queryByRole('complementary')).not.toBeInTheDocument()
    expect(screen.getByText(viMessages.auth.permission)).toBeInTheDocument()
  })
  it.each([
    [401, 'AUTH_INVALID_CREDENTIALS', viMessages.auth.invalidCredentials],
    [423, 'AUTH_ACCOUNT_LOCKED', viMessages.auth.locked],
    [429, 'RATE_LIMIT_EXCEEDED', viMessages.auth.rateLimited],
  ])('hiển thị Vietnamese error HTTP%i và xóa password form', async (status, code, message) => {
    fixture('ADMIN', new ApiError({ status: status as number, code: code as string, message: 'Private server diagnostic' }))
    await submit()
    await screen.findByRole('alert')
    expect(screen.getByRole('alert')).toHaveTextContent(message as string)
    expect(screen.getByLabelText(viMessages.auth.password)).toHaveValue('')
    expect(screen.queryByText('Private server diagnostic')).not.toBeInTheDocument()
  })
  it('terminal invalidation clear private UI và expired notice', async () => {
    const { session } = fixture()
    await submit()
    await screen.findByRole('heading', { name: viMessages.bootstrap.title })
    act(() => session.clearSession(session.getSessionSnapshot().sessionGeneration))
    await screen.findByRole('heading', { name: viMessages.auth.loginTitle })
    expect(screen.getByRole('status')).toHaveTextContent(viMessages.feedback.unauthorized)
  })
  it('logout lỗi thông báo chưa xác nhận server revoke', async () => {
    const { service } = fixture()
    vi.mocked(service.logout).mockRejectedValue(new Error('network'))
    await submit()
    await screen.findByRole('heading', { name: viMessages.bootstrap.title })
    await userEvent.click(screen.getByRole('button', { name: viMessages.auth.signOut }))
    await waitFor(() => expect(screen.getByRole('status')).toHaveTextContent(viMessages.auth.logoutUnconfirmed))
  })
})
