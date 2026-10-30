import type { Account, Transaction } from '../models/banking'
import { formatDate, formatMoney } from '../utils/format'
import { Icon } from './Icon'
import { EmptyState } from './ui'

export function TransactionList({
  transactions,
  accounts,
  emptyTitle = 'No transactions yet',
  emptyMessage = 'Your deposits, withdrawals, and transfers will appear here.',
}: {
  transactions: Transaction[]
  accounts: Account[]
  emptyTitle?: string
  emptyMessage?: string
}) {
  if (!transactions.length)
    return <EmptyState title={emptyTitle}>{emptyMessage}</EmptyState>
  return (
    <div className="transaction-table-wrap">
      <table className="transaction-table">
        <caption className="sr-only">Account transaction activity</caption>
        <thead>
          <tr>
            <th scope="col">Transaction</th>
            <th scope="col">Account</th>
            <th scope="col">Date</th>
            <th scope="col" className="amount-heading">
              Amount
            </th>
            <th scope="col">Status</th>
          </tr>
        </thead>
        <tbody>
          {transactions.map((transaction) => {
            const account = accounts.find(
              (candidate) => candidate.id === transaction.accountId,
            )
            return (
              <tr key={transaction.id}>
                <td>
                  <div className="transaction-name">
                    <span
                      className={`transaction-icon transaction-icon-${transaction.type.toLowerCase()}`}
                    >
                      <Icon
                        name={
                          transaction.type === 'DEPOSIT'
                            ? 'deposit'
                            : transaction.type === 'WITHDRAWAL'
                              ? 'withdraw'
                              : 'transfer'
                        }
                        size={19}
                      />
                    </span>
                    <div>
                      <strong>{transaction.description}</strong>
                      <span>
                        {transaction.type === 'WITHDRAWAL'
                          ? 'Withdrawal'
                          : transaction.type === 'TRANSFER'
                            ? 'Transfer'
                            : 'Deposit'}
                        <span className="mobile-transaction-account">
                          {' '}
                          ·{' '}
                          {account?.type === 'SAVINGS'
                            ? 'Savings'
                            : 'Checking'}{' '}
                          · {formatDate(transaction.createdAt)}
                        </span>
                      </span>
                    </div>
                  </div>
                </td>
                <td className="account-cell">
                  {account?.type === 'SAVINGS' ? 'Savings' : 'Checking'}
                  <span>•• {account?.lastFour}</span>
                </td>
                <td className="date-cell">
                  {formatDate(transaction.createdAt)}
                </td>
                <td
                  className={`amount-cell ${transaction.direction === 'CREDIT' ? 'amount-credit' : ''}`}
                >
                  {transaction.direction === 'CREDIT' ? '+' : '−'}
                  {formatMoney(transaction.amountCents)}
                </td>
                <td className="status-cell">
                  <span className="status-tag">
                    <Icon name="check" size={12} />
                    Completed
                  </span>
                </td>
              </tr>
            )
          })}
        </tbody>
      </table>
    </div>
  )
}
