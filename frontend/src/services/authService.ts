import type {
  AuthResponse,
  AuthService,
  LoginRequest,
  RegistrationRequest,
  User,
} from '../models/banking'
import {
  apiRequest,
  clearAuthToken,
  saveAuthToken,
} from './apiClient'

interface BackendAuthResponse {
  userId: string
  firstName: string
  lastName: string
  email: string
  token: string
}

function mapUser(response: BackendAuthResponse): User {

  return {
    id: String(response.userId),
    userId: response.email,
    firstName: response.firstName,
    lastName: response.lastName,
  }
}

function mapAuthResponse(
  response: BackendAuthResponse,
): AuthResponse {
  if (!response.token) {
    throw new Error('The server did not return an authentication token.')
  }

  saveAuthToken(response.token)

  const user = mapUser(response)
  localStorage.setItem('bankCurrentUser', JSON.stringify(user))

  return { user }
}

export const authService: AuthService = {
  async login({ email, password }: LoginRequest) {
    const response = await apiRequest<BackendAuthResponse>(
      '/auth',
      {
        method: 'POST',
        body: JSON.stringify({ email, password }),
      },
    )

    return mapAuthResponse(response)
  },

  async register({ firstName, lastName, email, password }: RegistrationRequest) {
    const response = await apiRequest<BackendAuthResponse>(
      '/auth/register',
      {
        method: 'POST',
        body: JSON.stringify({ firstName, lastName, email, password }),
      },
    )

    // Registration creates the account but does not automatically
    // open a frontend session.
    return { user: mapUser(response) }
  },

  logout() {
    clearAuthToken()
    localStorage.removeItem('bankCurrentUser')
  },
}
