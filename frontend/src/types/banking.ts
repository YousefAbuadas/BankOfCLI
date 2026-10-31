import type { Account, Transaction, User } from './domain'

export interface DashboardResponse {
  user: User
  accounts: Account[]
  transactions: Transaction[]
}

export interface TransactionResponse {
  accounts: Account[]
  transactions: Transaction[]
}
