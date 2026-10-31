import { useState } from 'react'
import { authService } from '../services/authService'
import type { User } from '../types/domain'

export function useAuth() {
  const [user, setUser] = useState<User | null>(null)

  function completeLogin(nextUser: User) {
    setUser(nextUser)
  }

  function logout() {
    authService.logout()
    setUser(null)
  }

  return { user, completeLogin, logout }
}
