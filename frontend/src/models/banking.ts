
export interface User {
  id: string;
  userId?: string | number;
  firstName: string;
  lastName: string;
  email?: string;
}

export type AccountType = "CHECKING" | "SAVINGS";

export interface Account {
  id: string;
  userId: string;
  type: AccountType;
  firstName: string;
  lastName: string;
  lastFour: string;
  balanceCents: number;
  currency: string;
}

export type TransactionType =
  | "DEPOSIT"
  | "WITHDRAWAL"
  | "TRANSFER"
  | "TRANSFER_IN"
  | "TRANSFER_OUT";

export interface Transaction {
  id: string;
  accountId: string;
  type: TransactionType;
  direction: "CREDIT" | "DEBIT";
  amountCents: number;
  description: string;
  createdAt: string;
  timestamp: string;
  status?: string;
  relatedAccountId?: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegistrationRequest {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
}

export interface AuthResponse {
  user: User;
}

export interface AuthService {
  login(request: LoginRequest): Promise<AuthResponse>;
  register(request: RegistrationRequest): Promise<AuthResponse>;
  logout(): void;
}

export interface DashboardResponse {
  user: User;
  accounts: Account[];
  transactions: Transaction[];
}

export interface TransactionResponse {
  accounts: Account[];
  transactions: Transaction[];
}

export interface ApiError {
  message: string;
  status?: number;
}
