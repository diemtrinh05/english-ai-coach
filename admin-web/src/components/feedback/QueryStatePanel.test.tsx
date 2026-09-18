import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import { viMessages } from '../../constants/messages.vi'
import { ApiError, ApiSessionChangedError } from '../../services/api/apiError'
import { QueryStatePanel } from './QueryStatePanel'

describe('QueryStatePanel', () => {
  it('hiển thị loading và empty bằng message tập trung', () => {
    const { rerender } = render(<QueryStatePanel state="loading" />)
    expect(screen.getByRole('status')).toHaveTextContent(viMessages.feedback.loading)

    rerender(<QueryStatePanel state="empty" />)
    expect(screen.getByText(viMessages.feedback.empty)).toBeInTheDocument()
  })

  it('ánh xạ 403 và hỗ trợ retry', async () => {
    const onRetry = vi.fn()
    render(
      <QueryStatePanel
        state="error"
        error={new ApiError({ status: 403, code: 'FORBIDDEN', message: 'Forbidden' })}
        onRetry={onRetry}
      />,
    )

    expect(screen.getByRole('alert')).toHaveTextContent(viMessages.feedback.forbidden)
    await userEvent.click(screen.getByRole('button', { name: viMessages.feedback.retry }))
    expect(onRetry).toHaveBeenCalledTimes(1)
  })

  it('hiển thị lỗi đổi session mà không lộ generation hoặc token', () => {
    render(<QueryStatePanel state="error" error={new ApiSessionChangedError()} />)

    expect(screen.getByRole('alert')).toHaveTextContent(viMessages.feedback.sessionChanged)
    expect(screen.getByRole('alert')).not.toHaveTextContent('G1')
    expect(screen.getByRole('alert')).not.toHaveTextContent('token-A')
  })

  it.each([
    ['CONCURRENT_UPDATE', viMessages.feedback.concurrentUpdate],
    ['IDEMPOTENCY_KEY_REUSE', viMessages.feedback.idempotencyKeyReuse],
  ])('giữ semantics cho 409 %s', (code, message) => {
    render(<QueryStatePanel state="error" error={new ApiError({ status: 409, code, message: 'Conflict' })} />)
    expect(screen.getByRole('alert')).toHaveTextContent(message)
  })
})
