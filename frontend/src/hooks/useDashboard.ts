import { useEffect, useState } from 'react'
import { bankService } from '../services/bankService'
import type { DashboardResponse, TransactionResponse } from '../types/banking'
import type { User } from '../types/domain'

export function useDashboard(user: User | null) {
  const [dashboard, setDashboard] = useState<DashboardResponse | null>(null)
  const [loadError, setLoadError] = useState('')
  const [retryCount, setRetryCount] = useState(0)

  useEffect(() => {
    if (!user) return
    let active = true

    bankService
      .getDashboard()
      .then((data) => {
        if (active) {
          setDashboard(data)
          setLoadError('')
        }
      })
      .catch((error: unknown) => {
        if (active)
          setLoadError(
            error instanceof Error
              ? error.message
              : 'Unable to load your accounts.',
          )
      })

    return () => {
      active = false
    }
  }, [user, retryCount])

  function retry() {
    setLoadError('')
    setRetryCount((count) => count + 1)
  }

  function reset() {
    setDashboard(null)
    setLoadError('')
  }

  function applyTransaction(response: TransactionResponse) {
    setDashboard((previous) =>
      previous
        ? {
            ...previous,
            accounts: response.accounts,
            transactions: [...response.transactions, ...previous.transactions],
          }
        : previous,
    )
  }

  return { dashboard, loadError, retry, reset, applyTransaction }
}
