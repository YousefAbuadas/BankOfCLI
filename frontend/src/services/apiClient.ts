const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

export function getAuthToken(): string | null {
  return localStorage.getItem('bankAuthToken')
}

export function saveAuthToken(token: string): void {
  localStorage.setItem('bankAuthToken', token)
}

export function clearAuthToken(): void {
  localStorage.removeItem('bankAuthToken')
}

export async function apiRequest<T>(
  endpoint: string,
  options: RequestInit = {},
  authenticated = false,
): Promise<T> {
  const token = getAuthToken()

  const response = await fetch(`${API_BASE_URL}${endpoint}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...(authenticated && token
        ? { Authorization: `Bearer ${token}` }
        : {}),
      ...options.headers,
    },
  })

  const responseText = await response.text()

  if (!response.ok) {
    let message = responseText || `Request failed (${response.status})`

    try {
      const parsed = JSON.parse(responseText)
      message = parsed.message || message
    } catch {
      // Keep the original response message.
    }

    throw new Error(message)
  }

  if (!responseText) {
    return undefined as T
  }

  return JSON.parse(responseText) as T
}