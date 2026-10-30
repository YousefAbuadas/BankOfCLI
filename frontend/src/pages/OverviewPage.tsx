import type { DashboardResponse, TransactionType } from '../models/banking'
import { Icon } from '../components/Icon'
import { Button } from '../components/ui'
import { TransactionList } from '../components/TransactionList'
import { formatMoney } from '../utils/format'

interface OverviewProps {
  dashboard: DashboardResponse
  onTransaction: (type: TransactionType, accountId?: string) => void
  onActivity: (accountId?: string) => void
}

export function OverviewPage({
  dashboard,
  onTransaction,
  onActivity,
}: OverviewProps) {
  const totalBalance = dashboard.accounts.reduce(
    (total, account) => total + account.balanceCents,
    0,
  )
  return (
    <>
      <div className="page-heading">
        <div>
          <span className="eyebrow">YOUR BANKING, IN ONE PLACE</span>
          <h1>Account overview</h1>
          <p>Welcome back, {dashboard.user.firstName}.</p>
        </div>
        <Button onClick={() => onTransaction('TRANSFER')}>
          <Icon name="transfer" size={18} />
          Move money
        </Button>
      </div>
      <section className="balances-grid" aria-label="Your account balances">
        <div className="total-balance-card">
          <div className="balance-card-top">
            <span>Total available balance</span>
            <Icon name="wallet" size={21} />
          </div>
          <p className="balance-value">{formatMoney(totalBalance)}</p>
          <div className="total-balance-footer">
            <span>Across {dashboard.accounts.length} accounts</span>
            <span className="currency-label">USD</span>
          </div>
          <div className="balance-decoration" aria-hidden="true" />
        </div>
        {dashboard.accounts.map((account) => (
          <button
            type="button"
            className="account-card"
            key={account.id}
            onClick={() => onActivity(account.id)}
            aria-label={`View ${account.name} activity`}
          >
            <div className="balance-card-top">
              <span className="account-icon">
                <Icon
                  name={account.type === 'SAVINGS' ? 'savings' : 'wallet'}
                  size={21}
                />
              </span>
              <span className="account-number">•••• {account.lastFour}</span>
            </div>
            <p className="account-name">{account.name}</p>
            <p className="account-balance">
              {formatMoney(account.balanceCents)}
            </p>
            <div className="account-card-footer">
              <span>Available balance</span>
              <Icon name="chevron" size={16} />
            </div>
          </button>
        ))}
      </section>
      <section className="quick-actions" aria-label="Quick actions">
        <button onClick={() => onTransaction('DEPOSIT')}>
          <span className="quick-action-icon">
            <Icon name="deposit" size={23} />
          </span>
          <span>
            <strong>Deposit money</strong>
            <span>Add to your balance</span>
          </span>
          <Icon name="chevron" size={16} />
        </button>
        <button onClick={() => onTransaction('WITHDRAWAL')}>
          <span className="quick-action-icon">
            <Icon name="withdraw" size={23} />
          </span>
          <span>
            <strong>Withdraw money</strong>
            <span>Take out what you need</span>
          </span>
          <Icon name="chevron" size={16} />
        </button>
        <button onClick={() => onTransaction('TRANSFER')}>
          <span className="quick-action-icon">
            <Icon name="transfer" size={23} />
          </span>
          <span>
            <strong>Transfer funds</strong>
            <span>Move between accounts</span>
          </span>
          <Icon name="chevron" size={16} />
        </button>
      </section>
      <section className="panel activity-panel">
        <div className="panel-heading">
          <div>
            <h2>Recent activity</h2>
            <p>Your latest account transactions.</p>
          </div>
          <Button variant="text" onClick={() => onActivity()}>
            View all activity
          </Button>
        </div>
        <TransactionList
          transactions={dashboard.transactions.slice(0, 5)}
          accounts={dashboard.accounts}
        />
      </section>
      <div className="overview-footer">
        <span>
          <Icon name="lock" size={16} />
          Your accounts, together in one place.
        </span>
        <span>All balances shown in USD</span>
      </div>
    </>
  )
}
