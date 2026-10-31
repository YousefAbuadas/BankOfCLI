export interface User {
  id: string
  userId?: string | number
  firstName: string
  lastName: string
  email?: string
}

export type AccountType = 'CHECKING' | 'SAVINGS'

export interface Account {
  id: string
  userId: string
  type: AccountType
  name: string
  lastFour: string
  balanceCents: number
  currency: string
}

export type TransactionType =
  | 'DEPOSIT'
  | 'WITHDRAWAL'
  | 'TRANSFER'
  | 'TRANSFER_IN'
  | 'TRANSFER_OUT'

export interface Transaction {
  id: string
  accountId: string
  type: TransactionType
  direction: 'CREDIT' | 'DEBIT'
  amountCents: number
  description: string
  createdAt: string
  timestamp: string
  status?: string
  relatedAccountId?: string
}
