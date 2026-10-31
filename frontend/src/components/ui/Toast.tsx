import { Icon } from './Icon'
import './Toast.css'

export interface ToastMessage {
  id: number
  message: string
  kind: 'success' | 'error'
}

export function Toast({
  toast,
  onDismiss,
}: {
  toast: ToastMessage | null
  onDismiss: () => void
}) {
  return (
    <div className="toast-region" aria-live="polite" aria-atomic="true">
      {toast && (
        <div className={`toast toast-${toast.kind}`}>
          <span className="toast-icon">
            <Icon name={toast.kind === 'success' ? 'check' : 'info'} />
          </span>
          <p>{toast.message}</p>
          <button
            className="icon-button"
            onClick={onDismiss}
            aria-label="Dismiss notification"
          >
            <Icon name="close" size={18} />
          </button>
        </div>
      )}
    </div>
  )
}
