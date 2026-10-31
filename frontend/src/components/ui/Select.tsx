import { useId } from 'react'
import type { SelectHTMLAttributes, ReactNode } from 'react'
import './Field.css'
import './Select.css'

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
