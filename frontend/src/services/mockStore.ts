import seed from '../data/mock-bank.json'
import type { Account, ApiError, Transaction, User } from '../models/banking'

interface MockSeed {
  user: User
  credentials: { userId: string; pin: string }
  accounts: Account[]
  transactions: Transaction[]
}

// Seed data lives in JSON, never in a visual component. This store is demo-only.
const initial = seed as MockSeed

export const mockStore = {
  users: [structuredClone(initial.user)],
  credentials: new Map([[initial.user.id, initial.credentials.pin]]),
  accounts: structuredClone(initial.accounts),
  transactions: structuredClone(initial.transactions),
  currentUserId: null as string | null,
}

export const demoCredentials = { ...initial.credentials }

export class ServiceError extends Error implements ApiError {
  code: string

  constructor(code: string, message: string) {
    super(message)
    this.name = 'ServiceError'
    this.code = code
  }
}

export const simulateLatency = () =>
  new Promise<void>((resolve) => setTimeout(resolve, 650))

export function currentUser(): User {
  const user = mockStore.users.find(
    (candidate) => candidate.id === mockStore.currentUserId,
  )
  if (!user)
    throw new ServiceError('UNAUTHENTICATED', 'Please sign in to continue.')
  return user
}
