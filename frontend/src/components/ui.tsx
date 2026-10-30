import { useId, useState } from 'react'
import type {
  ButtonHTMLAttributes,
  InputHTMLAttributes,
  ReactNode,
  SelectHTMLAttributes,
} from 'react'
import { Icon } from './Icon'

export function Brand({ light = false }: { light?: boolean }) {
  return (
    <div className={`brand ${light ? 'brand-light' : ''}`}>
      <span className="brand-mark">
        <Icon name="bank" size={26} />
      </span>
      <span>
        Bank of CLI<span className="brand-caption">PERSONAL BANKING</span>
      </span>
    </div>
  )
}

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary' | 'text'
  loading?: boolean
  children: ReactNode
}

export function Button({
  variant = 'primary',
  loading = false,
  children,
  className = '',
  disabled,
  ...props
}: ButtonProps) {
  return (
    <button
      className={`button button-${variant} ${className}`}
      disabled={disabled || loading}
      aria-busy={loading}
      {...props}
    >
      {loading && <span className="spinner" aria-hidden="true" />}
      {children}
    </button>
  )
}

interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  label: string
  error?: string
  hint?: string
  leading?: ReactNode
}

export function Input({
  label,
  error,
  hint,
  leading,
  id: providedId,
  type = 'text',
  className = '',
  ...props
}: InputProps) {
  const generatedId = useId()
  const id = providedId || generatedId
  const [revealed, setRevealed] = useState(false)
  const isPassword = type === 'password'
  return (
    <div className={`field ${className}`}>
      <label htmlFor={id}>
        {label}
        {props.required && <span className="sr-only"> (required)</span>}
      </label>
      <div
        className={`input-wrap ${error ? 'input-invalid' : ''} ${leading ? 'input-leading' : ''}`}
      >
        {leading && <span className="input-prefix">{leading}</span>}
        <input
          {...props}
          id={id}
          type={isPassword && revealed ? 'text' : type}
          aria-invalid={!!error}
          aria-describedby={error || hint ? `${id}-message` : undefined}
        />
        {isPassword && (
          <button
            className="password-toggle"
            type="button"
            aria-label={revealed ? `Hide ${label}` : `Show ${label}`}
            onClick={() => setRevealed(!revealed)}
          >
            <Icon name={revealed ? 'eyeOff' : 'eye'} />
          </button>
        )}
      </div>
      {error ? (
        <p className="field-error" id={`${id}-message`}>
          {error}
        </p>
      ) : (
        hint && (
          <p className="field-hint" id={`${id}-message`}>
            {hint}
          </p>
        )
      )}
    </div>
  )
}

interface SelectProps extends SelectHTMLAttributes<HTMLSelectElement> {
  label: string
  error?: string
  children: ReactNode
}

export function Select({
  label,
  error,
  children,
  id: providedId,
  ...props
}: SelectProps) {
  const generatedId = useId()
  const id = providedId || generatedId
  return (
    <div className="field">
      <label htmlFor={id}>{label}</label>
      <select
        {...props}
        id={id}
        aria-invalid={!!error}
        aria-describedby={error ? `${id}-message` : undefined}
      >
        {children}
      </select>
      {error && (
        <p id={`${id}-message`} className="field-error">
          {error}
        </p>
      )}
    </div>
  )
}

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

export function EmptyState({
  title,
  children,
}: {
  title: string
  children: ReactNode
}) {
  return (
    <div className="empty-state">
      <span className="empty-icon">
        <Icon name="history" size={28} />
      </span>
      <h3>{title}</h3>
      <p>{children}</p>
    </div>
  )
}

export function DashboardSkeleton() {
  return (
    <div
      className="dashboard-skeleton"
      role="status"
      aria-label="Loading your accounts"
    >
      <span className="sr-only">Loading your accounts…</span>
      <div className="skeleton skeleton-heading" />
      <div className="skeleton-grid">
        {[1, 2, 3].map((key) => (
          <div className="skeleton skeleton-card" key={key} />
        ))}
      </div>
      <div className="skeleton skeleton-table" />
    </div>
  )
}
