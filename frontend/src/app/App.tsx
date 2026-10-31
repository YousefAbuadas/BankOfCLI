import { useState } from 'react'
import { DashboardSkeleton } from '../components/banking/DashboardSkeleton'
import { Button } from '../components/ui/Button'
import { Icon } from '../components/ui/Icon'
import { Toast } from '../components/ui/Toast'
import { useAuth } from '../hooks/useAuth'
import { useDashboard } from '../hooks/useDashboard'
import { useToast } from '../hooks/useToast'
import { BankLayout } from '../layouts/BankLayout'
import { ActivityPage } from '../pages/activity/ActivityPage'
import { AuthPage } from '../pages/auth/AuthPage'
import { OverviewPage } from '../pages/overview/OverviewPage'
import { TransactionPage } from '../pages/transactions/TransactionPage'
import type { TransactionResponse } from '../types/banking'
import type { TransactionType } from '../types/domain'
import { navigation } from './navigation'
import type { View } from './navigation'

function App() {
  const { user, completeLogin, logout: signOut } = useAuth()
  const { dashboard, loadError, retry, reset, applyTransaction } =
    useDashboard(user)
  const { toast, notify, dismiss } = useToast()
  const [view, setView] = useState<View>('overview')
  const [transactionType, setTransactionType] =
    useState<TransactionType>('DEPOSIT')
  const [selectedAccountId, setSelectedAccountId] = useState<
    string | undefined
  >()

  function navigate(nextView: View) {
    setView(nextView)
    setSelectedAccountId(undefined)
    window.scrollTo({ top: 0, behavior: 'instant' })
  }

  function startTransaction(type: TransactionType, accountId?: string) {
    setTransactionType(type)
    setSelectedAccountId(accountId)
    setView('transactions')
    window.scrollTo({ top: 0, behavior: 'instant' })
  }

  function openActivity(accountId?: string) {
    setSelectedAccountId(accountId)
    setView('activity')
    window.scrollTo({ top: 0, behavior: 'instant' })
  }

  function completeTransaction(response: TransactionResponse, message: string) {
    applyTransaction(response)
    notify(message)
  }

  function logout() {
    signOut()
    reset()
    dismiss()
    navigate('overview')
  }

  return (
    <>
      {!user ? (
        <AuthPage onLogin={completeLogin} notify={notify} />
      ) : (
        <BankLayout
          user={user}
          accounts={dashboard?.accounts ?? []}
          title={navigation.find((item) => item.view === view)!.label}
          navigation={navigation.map((item) => ({
            label: item.label,
            icon: item.icon,
            active: item.view === view,
            onClick: () => navigate(item.view),
          }))}
          onActivity={openActivity}
          onLogout={logout}
        >
          {loadError ? (
            <div className="load-error" role="alert">
              <Icon name="info" size={28} />
              <h1>We couldn't load your accounts.</h1>
              <p>{loadError}</p>
              <Button onClick={retry}>Try again</Button>
            </div>
          ) : !dashboard ? (
            <DashboardSkeleton />
          ) : view === 'overview' ? (
            <OverviewPage
              dashboard={dashboard}
              onTransaction={startTransaction}
              onActivity={openActivity}
            />
          ) : view === 'transactions' ? (
            <TransactionPage
              key={`${transactionType}-${selectedAccountId || 'all'}`}
              accounts={dashboard.accounts}
              initialType={transactionType}
              initialAccountId={selectedAccountId}
              onSuccess={completeTransaction}
            />
          ) : (
            <ActivityPage
              key={selectedAccountId || 'all'}
              dashboard={dashboard}
              initialAccountId={selectedAccountId}
            />
          )}
        </BankLayout>
      )}
      <Toast toast={toast} onDismiss={dismiss} />
    </>
  )
}

export default App
