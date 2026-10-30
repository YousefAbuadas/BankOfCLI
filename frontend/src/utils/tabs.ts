import type { KeyboardEvent } from 'react'

export function handleTabNavigation(event: KeyboardEvent<HTMLDivElement>) {
  if (!['ArrowLeft', 'ArrowRight', 'Home', 'End'].includes(event.key)) return
  const tabs = Array.from(
    event.currentTarget.querySelectorAll<HTMLButtonElement>(
      '[role="tab"]:not(:disabled)',
    ),
  )
  if (!tabs.length) return
  const index = tabs.findIndex((tab) => tab === event.target)
  const nextIndex =
    event.key === 'Home'
      ? 0
      : event.key === 'End'
        ? tabs.length - 1
        : (index + (event.key === 'ArrowRight' ? 1 : -1) + tabs.length) %
          tabs.length
  event.preventDefault()
  tabs[nextIndex].focus()
  tabs[nextIndex].click()
}
