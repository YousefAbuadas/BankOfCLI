

import type {
  Account,
  DashboardResponse,
  Transaction,
  TransactionResponse,
  TransactionType,
  User,
} from '../models/banking'
import { apiRequest } from './apiClient'

interface BackendAccount {
  accountId: number | string
  user?: { userId?: number | string }
  currencyType?: string
  accountBalance?: number
  date?: string
  type?: string
  name?: string
}

interface BackendTransaction {
  transactionId: number | string
  account?: { accountId?: number | string }
  type?: string
  amount?: string | number
  date?: string
  description?: string
}

function toCents(value: number | string | undefined): number {
  const amount = Number(value ?? 0)
  return Number.isFinite(amount) ? Math.round(amount * 100) : 0
}

function mapAccount(account: BackendAccount): Account {
  const id = String(account.accountId)
  const balance = Number(account.accountBalance ?? 0)

  return {
    id,
    userId: String(account.user?.userId ?? ''),
    type: account.type?.toUpperCase() === 'SAVINGS'
      ? 'SAVINGS'
      : 'CHECKING',
    name: account.name || `Account ${id}`,
    lastFour: id.slice(-4).padStart(4, '0'),
    balanceCents: Math.round(balance * 100),
    currency: account.currencyType || 'USD',
  }
}

function mapTransaction(
  transaction: BackendTransaction,
): Transaction {
  const rawType = (transaction.type || 'DEPOSIT').toUpperCase()

  let type: TransactionType
  let direction: 'CREDIT' | 'DEBIT'

  if (rawType === 'WITHDRAW' || rawType === 'WITHDRAWAL') {
    type = 'WITHDRAWAL'
    direction = 'DEBIT'
  } else if (rawType === 'TRANSFER_OUT') {
    type = 'TRANSFER_OUT'
    direction = 'DEBIT'
  } else if (rawType === 'TRANSFER_IN') {
    type = 'TRANSFER_IN'
    direction = 'CREDIT'
  } else if (rawType === 'TRANSFER') {
    type = 'TRANSFER'
    direction = 'DEBIT'
  } else {
    type = 'DEPOSIT'
    direction = 'CREDIT'
  }

  const timestamp = transaction.date || new Date().toISOString()

  return {
    id: String(transaction.transactionId),
    accountId: String(transaction.account?.accountId ?? ''),
    type,
    direction,
    amountCents: toCents(transaction.amount),
    description: transaction.description || type.replace('_', ' '),
    createdAt: timestamp,
    timestamp,
    status: 'COMPLETED',
  }
}

async function getAccounts(): Promise<Account[]> {
  const response = await apiRequest<BackendAccount[]>(
    '/user/accounts',
    {},
    true,
  )

  return response.map(mapAccount)
}

async function getTransactions(
  accounts: Account[],
): Promise<Transaction[]> {
  const results = await Promise.all(
    accounts.map(async (account) => {
      const response = await apiRequest<BackendTransaction[]>(
        `/transaction/transactions/${encodeURIComponent(account.id)}`,
        {},
        true,
      )

      return response.map(mapTransaction)
    }),
  )

  return results
    .flat()
    .sort(
      (a, b) =>
        new Date(b.createdAt).getTime() -
        new Date(a.createdAt).getTime(),
    )
}

export const bankService = {
  async getDashboard(): Promise<DashboardResponse> {
    const savedUser = localStorage.getItem('bankCurrentUser')

    if (!savedUser) {
      throw new Error('Your session has expired. Please sign in again.')
    }

    const user = JSON.parse(savedUser) as User
    const accounts = await getAccounts()
    const transactions = await getTransactions(accounts)

    return {
      user,
      accounts,
      transactions,
    }
  },

  async submitTransaction(input: {
    type: TransactionType
    accountId: string
    amountCents: number
    destinationAccountId?: string
    note?: string
  }): Promise<TransactionResponse> {
    const amount = (input.amountCents / 100).toFixed(2)
    const accountId = encodeURIComponent(input.accountId)

    if (input.type === 'DEPOSIT') {
      await apiRequest(
        `/transaction/deposit/${accountId}?amount=${encodeURIComponent(amount)}`,
        { method: 'PUT' },
        true,
      )
    } else if (input.type === 'WITHDRAWAL') {
      await apiRequest(
        `/transaction/withdraw/${accountId}?amount=${encodeURIComponent(amount)}`,
        { method: 'PUT' },
        true,
      )
    } else if (input.type === 'TRANSFER') {
      if (!input.destinationAccountId) {
        throw new Error('Choose a destination account.')
      }

      await apiRequest(
        `/transaction/transfer/${accountId}?destinationAccountId=${encodeURIComponent(input.destinationAccountId)}&amount=${encodeURIComponent(amount)}`,
        { method: 'PUT' },
        true,
      )
    } else {
      throw new Error('Unsupported transaction type.')
    }

    const dashboard = await this.getDashboard()

    return {
      accounts: dashboard.accounts,
      transactions: dashboard.transactions,
    }
  },
}

