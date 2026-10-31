import { useId, useState } from 'react'
import type { InputHTMLAttributes, ReactNode } from 'react'
import { Icon } from './Icon'
import './Field.css'
import './Input.css'

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
