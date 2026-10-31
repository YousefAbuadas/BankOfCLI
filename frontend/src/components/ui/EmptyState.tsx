import type { ReactNode } from 'react'
import { Icon } from './Icon'
import './EmptyState.css'

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
