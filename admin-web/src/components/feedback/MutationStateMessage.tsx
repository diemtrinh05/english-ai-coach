import { viMessages } from '../../constants/messages.vi'
import type { MutationViewState } from '../../types/api'

interface MutationStateMessageProps {
  state: Exclude<MutationViewState, 'idle'>
}

export function MutationStateMessage({ state }: MutationStateMessageProps) {
  if (state === 'submitting') {
    return <p role="status">{viMessages.feedback.mutationSubmitting}</p>
  }

  if (state === 'success') {
    return <p role="status">{viMessages.feedback.mutationSuccess}</p>
  }

  return <p role="alert">{viMessages.feedback.mutationFailure}</p>
}
