import type { User } from './domain'

export interface LoginRequest {
  email: string
  password: string
}

export interface RegistrationRequest {
  firstName: string
  lastName: string
  email: string
  password: string
}

export interface AuthResponse {
  user: User
}

export interface AuthService {
  login(request: LoginRequest): Promise<AuthResponse>
  register(request: RegistrationRequest): Promise<AuthResponse>
  logout(): void
}
