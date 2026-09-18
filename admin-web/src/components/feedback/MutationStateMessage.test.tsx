import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { viMessages } from '../../constants/messages.vi'
import { MutationStateMessage } from './MutationStateMessage'

describe('MutationStateMessage', () => {
  it('hiển thị trạng thái đang gửi và thành công', () => {
    const { rerender } = render(<MutationStateMessage state="submitting" />)
    expect(screen.getByRole('status')).toHaveTextContent(viMessages.feedback.mutationSubmitting)

    rerender(<MutationStateMessage state="success" />)
    expect(screen.getByRole('status')).toHaveTextContent(viMessages.feedback.mutationSuccess)
  })

  it('hiển thị lỗi mutation bằng thông báo tập trung', () => {
    render(<MutationStateMessage state="failure" />)
    expect(screen.getByRole('alert')).toHaveTextContent(viMessages.feedback.mutationFailure)
  })
})
