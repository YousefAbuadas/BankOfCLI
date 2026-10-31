import { useEffect, useState } from 'react'
import type { ToastMessage } from '../components/ui/Toast'

export function useToast() {
  const [toast, setToast] = useState<ToastMessage | null>(null)

  useEffect(() => {
    if (!toast) return
    const timeout = setTimeout(() => setToast(null), 6000)
    return () => clearTimeout(timeout)
  }, [toast])

  function notify(message: string, kind: 'success' | 'error' = 'success') {
    setToast({ id: Date.now(), message, kind })
  }

  function dismiss() {
    setToast(null)
  }

  return { toast, notify, dismiss }
}
