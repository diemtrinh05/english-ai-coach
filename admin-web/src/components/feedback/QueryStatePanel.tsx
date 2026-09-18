import { viMessages } from '../../constants/messages.vi'
import { ApiError, ApiSessionChangedError } from '../../services/api/apiError'
import type { QueryViewState } from '../../types/api'

interface QueryStatePanelProps {
  state: Exclude<QueryViewState, 'initial' | 'success'>
  error?: unknown
  onRetry?: () => void
}

function getErrorMessage(error: unknown): string {
  if (error instanceof ApiSessionChangedError) return viMessages.feedback.sessionChanged
  if (!(error instanceof ApiError)) return viMessages.feedback.error
  if (error.status === 401) return viMessages.feedback.unauthorized
  if (error.status === 403) return viMessages.feedback.forbidden
  if (error.code === 'CONCURRENT_UPDATE') return viMessages.feedback.concurrentUpdate
  if (error.code === 'IDEMPOTENCY_KEY_REUSE') return viMessages.feedback.idempotencyKeyReuse
  return viMessages.feedback.error
}

export function QueryStatePanel({ state, error, onRetry }: QueryStatePanelProps) {
  if (state === 'loading') {
    return <p role="status">{viMessages.feedback.loading}</p>
  }

  if (state === 'empty') {
    return <p>{viMessages.feedback.empty}</p>
  }

  return (
    <div role="alert">
      <p>{getErrorMessage(error)}</p>
      {onRetry ? <button onClick={onRetry}>{viMessages.feedback.retry}</button> : null}
    </div>
  )
}
