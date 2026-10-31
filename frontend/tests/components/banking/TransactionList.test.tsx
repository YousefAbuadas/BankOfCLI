import { render, screen, within } from '@testing-library/react'
import { TransactionList } from '../../../src/components/banking/TransactionList'
import type { Account, Transaction } from '../../../src/types/domain'

const accounts: Account[] = [
  {
    id: 'checking-1',
    userId: 'user-1',
    type: 'CHECKING',
    name: 'Everyday Checking',
    lastFour: '4321',
    balanceCents: 25000,
    currency: 'USD',
  },
]

const transactions: Transaction[] = [
  {
    id: 'transaction-1',
    accountId: 'checking-1',
    type: 'DEPOSIT',
    direction: 'CREDIT',
    amountCents: 12550,
    description: 'Payroll deposit',
    createdAt: '2026-06-15T12:00:00.000Z',
    timestamp: '2026-06-15T12:00:00.000Z',
  },
]

describe('TransactionList', () => {
  it('renders transaction details in an accessible table', () => {
    // Arrange

    // Act
    render(<TransactionList transactions={transactions} accounts={accounts} />)

    // Assert
    const row = screen.getByRole('row', { name: /Payroll deposit/ })
    expect(within(row).getByText('Checking')).toBeInTheDocument()
    expect(within(row).getByText('•• 4321')).toBeInTheDocument()
    expect(within(row).getByText('+$125.50')).toBeInTheDocument()
    expect(within(row).getByText('Completed')).toBeInTheDocument()
    expect(screen.getByRole('table', { name: 'Account transaction activity' })).toBeInTheDocument()
  })

  it('shows the empty state when there are no transactions', () => {
    // Arrange

    // Act
    render(<TransactionList transactions={[]} accounts={accounts} />)

    // Assert
    expect(screen.getByRole('heading', { name: 'No transactions yet' })).toBeInTheDocument()
    expect(screen.getByText(/deposits, withdrawals, and transfers/)).toBeInTheDocument()
    expect(screen.queryByRole('table')).not.toBeInTheDocument()
  })
})
