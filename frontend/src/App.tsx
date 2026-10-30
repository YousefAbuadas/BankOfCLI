import { useEffect, useState } from 'react'
import type {
  DashboardResponse,
  TransactionResponse,
  TransactionType,
  User,
} from './models/banking'
import { authService } from './services/authService'
import { bankService } from './services/bankService'
import { Brand, Button, DashboardSkeleton, Toast } from './components/ui'
import type { ToastMessage } from './components/ui'
import { Icon } from './components/Icon'
import type { IconName } from './components/Icon'
import { AuthPage } from './pages/AuthPage'
import { OverviewPage } from './pages/OverviewPage'
import { TransactionPage } from './pages/TransactionPage'
import { ActivityPage } from './pages/ActivityPage'
import './App.css'

type View = 'overview' | 'transactions' | 'activity'
const navigation: { view: View; label: string; icon: IconName }[] = [
  { view: 'overview', label: 'Overview', icon: 'grid' },
  { view: 'transactions', label: 'Transactions', icon: 'transfer' },
  { view: 'activity', label: 'Activity', icon: 'history' },
]

function App() {
  const [user, setUser] = useState<User | null>(null)
  const [dashboard, setDashboard] = useState<DashboardResponse | null>(null)
  const [view, setView] = useState<View>('overview')
  const [transactionType, setTransactionType] =
    useState<TransactionType>('DEPOSIT')
  const [selectedAccountId, setSelectedAccountId] = useState<
    string | undefined
  >()
  const [toast, setToast] = useState<ToastMessage | null>(null)
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

  useEffect(() => {
    if (!toast) return
    const timeout = setTimeout(() => setToast(null), 6000)
    return () => clearTimeout(timeout)
  }, [toast])

  function notify(message: string, kind: 'success' | 'error' = 'success') {
    setToast({ id: Date.now(), message, kind })
  }

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
    setDashboard((previous) =>
      previous
        ? {
            ...previous,
            accounts: response.accounts,
            transactions: [...response.transactions, ...previous.transactions],
          }
        : previous,
    )
    notify(message)
  }

  function logout() {
    authService.logout()
    setUser(null)
    setDashboard(null)
    setLoadError('')
    setToast(null)
    navigate('overview')
  }

  return (
    <>
      {!user ? (
        <AuthPage onLogin={setUser} notify={notify} />
      ) : (
        <div className="app-shell">
          <a href="#main-content" className="skip-link">
            Skip to content
          </a>
          <aside className="sidebar">
            <Brand light />
            <div className="sidebar-section-label">WORKSPACE</div>
            <nav aria-label="Main navigation">
              {navigation.map((item) => (
                <button
                  key={item.view}
                  onClick={() => navigate(item.view)}
                  className={`nav-item ${view === item.view ? 'nav-active' : ''}`}
                  aria-current={view === item.view ? 'page' : undefined}
                >
                  <Icon name={item.icon} size={21} />
                  <span>{item.label}</span>
                  {view === item.view && <span className="nav-active-marker" />}
                </button>
              ))}
            </nav>
            <div className="sidebar-accounts">
              <div className="sidebar-section-label">YOUR ACCOUNTS</div>
              {dashboard?.accounts.map((account) => (
                <button
                  className="sidebar-account"
                  key={account.id}
                  onClick={() => openActivity(account.id)}
                >
                  <Icon
                    name={account.type === 'SAVINGS' ? 'savings' : 'wallet'}
                    size={18}
                  />
                  <span>
                    {account.type === 'SAVINGS' ? 'Savings' : 'Checking'}
                  </span>
                  <span className="sidebar-account-digits">
                    {account.lastFour}
                  </span>
                </button>
              ))}
            </div>
            <div className="sidebar-bottom">
              <button className="logout-button" onClick={logout}>
                <Icon name="logout" size={19} />
                Sign out
              </button>

            </div>
          </aside>
          <div className="workspace">
            <header className="workspace-header">
              <span className="header-breadcrumb">
                <span className="breadcrumb-root">Personal banking</span>
                <span className="breadcrumb-divider">/</span>
                <strong>
                  {view === 'overview'
                    ? 'Overview'
                    : view === 'transactions'
                      ? 'Transactions'
                      : 'Activity'}
                </strong>
              </span>
              <div className="header-user">
                <div className="header-user-text">
                  <strong>
                    {user.firstName} {user.lastName}
                  </strong>
                  <span>Personal account</span>
                </div>
                <span className="avatar">
                  {user.firstName.charAt(0)}
                  {user.lastName.charAt(0)}
                </span>
              </div>
              <button
                className="mobile-signout icon-button"
                onClick={logout}
                aria-label="Sign out"
              >
                <Icon name="logout" />
              </button>
            </header>
            <main className="main-content" id="main-content">
              {loadError ? (
                <div className="load-error" role="alert">
                  <Icon name="info" size={28} />
                  <h1>We couldn't load your accounts.</h1>
                  <p>{loadError}</p>
                  <Button
                    onClick={() => {
                      setLoadError('')
                      setRetryCount((count) => count + 1)
                    }}
                  >
                    Try again
                  </Button>
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
            </main>
          </div>
        </div>
      )}
      <Toast toast={toast} onDismiss={() => setToast(null)} />
    </>
  )
}

export default App
