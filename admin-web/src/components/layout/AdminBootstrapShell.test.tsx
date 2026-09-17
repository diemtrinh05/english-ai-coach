import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { viMessages } from '../../constants/messages.vi'
import { AdminBootstrapShell } from './AdminBootstrapShell'

describe('AdminBootstrapShell', () => {
  it('hiển thị shell quản trị với nội dung tiếng Việt tập trung', () => {
    render(<AdminBootstrapShell />)

    expect(
      screen.getByRole('heading', { name: viMessages.bootstrap.title }),
    ).toBeInTheDocument()
    expect(
      screen.getByRole('complementary', { name: viMessages.navigation.label }),
    ).toBeInTheDocument()
    expect(screen.getByText(viMessages.bootstrap.statusReady)).toBeInTheDocument()
  })

  it('đánh dấu mục Tổng quan là vị trí điều hướng hiện tại', () => {
    render(<AdminBootstrapShell />)

    expect(screen.getByText(viMessages.navigation.dashboard)).toHaveAttribute(
      'aria-current',
      'page',
    )
  })
})
