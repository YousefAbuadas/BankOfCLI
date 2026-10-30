import { useState } from 'react'
import type { DashboardResponse } from '../models/banking'
import { Select } from '../components/ui'
import { Icon } from '../components/Icon'
import { TransactionList } from '../components/TransactionList'

export function ActivityPage({
  dashboard,
  initialAccountId,
}: {
  dashboard: DashboardResponse
  initialAccountId?: string
}) {
  const [accountId, setAccountId] = useState(initialAccountId || 'ALL')
  const [type, setType] = useState('ALL')
  const [query, setQuery] = useState('')
  const transactions = dashboard.transactions.filter(
    (transaction) =>
      (accountId === 'ALL' || transaction.accountId === accountId) &&
      (type === 'ALL' || transaction.type === type) &&
      transaction.description
        .toLowerCase()
        .includes(query.trim().toLowerCase()),
  )
  return (
    <>
      <div className="page-heading">
        <div>
          <span className="eyebrow">EVERY MOVE, ACCOUNTED FOR</span>
          <h1>Account activity</h1>
          <p>A closer look at your deposits, withdrawals, and transfers.</p>
        </div>
      </div>
      <section className="panel activity-panel">
        <div className="activity-filters">
          <div className="activity-search">
            <Icon name="search" size={19} />
            <input
              type="search"
              aria-label="Search transactions"
              placeholder="Search transactions"
              value={query}
              onChange={(event) => setQuery(event.target.value)}
            />
          </div>
          <Select
            label="Account"
            value={accountId}
            onChange={(event) => setAccountId(event.target.value)}
          >
            <option value="ALL">All accounts</option>
            {dashboard.accounts.map((account) => (
              <option value={account.id} key={account.id}>
                {account.name}
              </option>
            ))}
          </Select>
          <Select
            label="Type"
            value={type}
            onChange={(event) => setType(event.target.value)}
          >
            <option value="ALL">All transactions</option>
            <option value="DEPOSIT">Deposits</option>
            <option value="WITHDRAWAL">Withdrawals</option>
            <option value="TRANSFER">Transfers</option>
          </Select>
        </div>
        <div className="activity-count" aria-live="polite">
          {transactions.length}{' '}
          {transactions.length === 1 ? 'transaction' : 'transactions'}
        </div>
        <TransactionList
          accounts={dashboard.accounts}
          transactions={transactions}
          emptyTitle={
            dashboard.transactions.length
              ? 'No matching transactions'
              : undefined
          }
          emptyMessage={
            dashboard.transactions.length
              ? 'Try changing your search or filters.'
              : undefined
          }
        />
      </section>
    </>
  )
}
