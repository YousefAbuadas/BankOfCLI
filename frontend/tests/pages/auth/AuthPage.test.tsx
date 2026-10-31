import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { AuthPage } from '../../../src/pages/auth/AuthPage'
import { authService } from '../../../src/services/authService'
import type { User } from '../../../src/types/domain'

jest.mock('../../../src/services/authService', () => ({
  authService: {
    login: jest.fn(),
    register: jest.fn(),
    logout: jest.fn(),
  },
}))

const login = jest.mocked(authService.login)
const register = jest.mocked(authService.register)

const signedInUser: User = {
  id: 'user-1',
  firstName: 'Avery',
  lastName: 'Morgan',
}

function renderAuthPage() {
  const onLogin = jest.fn()
  const notify = jest.fn()

  render(<AuthPage onLogin={onLogin} notify={notify} />)

  return { onLogin, notify }
}

describe('AuthPage', () => {
  it('shows validation errors and skips login when required fields are empty', async () => {
    // Arrange
    const user = userEvent.setup()
    renderAuthPage()

    // Act
    await user.click(screen.getByRole('button', { name: 'Sign in' }))

    // Assert
    expect(await screen.findByText('Enter your email address.')).toBeInTheDocument()
    expect(screen.getByText('Enter your password.')).toBeInTheDocument()
    expect(login).not.toHaveBeenCalled()
  })

  it('submits valid credentials and returns the signed-in user', async () => {
    // Arrange
    const userEventDriver = userEvent.setup()
    const { onLogin } = renderAuthPage()
    login.mockResolvedValue({ user: signedInUser })

    // Act
    await userEventDriver.type(screen.getByRole('textbox', { name: /Email/ }), 'avery@example.com')
    await userEventDriver.type(screen.getByLabelText(/^Password/), 'correct-horse')
    await userEventDriver.click(screen.getByRole('button', { name: 'Sign in' }))

    // Assert
    await waitFor(() => expect(onLogin).toHaveBeenCalledWith(signedInUser))
    expect(login).toHaveBeenCalledWith({ email: 'avery@example.com', password: 'correct-horse' })
  })

  it('prevents registration when the password confirmation does not match', async () => {
    // Arrange
    const user = userEvent.setup()
    renderAuthPage()
    await user.click(screen.getByRole('tab', { name: 'Create account' }))
    await user.type(screen.getByRole('textbox', { name: /First name/ }), 'Avery')
    await user.type(screen.getByRole('textbox', { name: /Last name/ }), 'Morgan')
    await user.type(screen.getByRole('textbox', { name: /Email/ }), 'avery@example.com')
    await user.type(screen.getByLabelText(/^Password/), 'correct-horse')
    await user.type(screen.getByLabelText(/^Confirm password/), 'different-password')

    // Act
    await user.click(screen.getByRole('button', { name: 'Create account' }))

    // Assert
    expect(await screen.findByText('Your passwords do not match.')).toBeInTheDocument()
    expect(register).not.toHaveBeenCalled()
  })

  it('registers valid account details, then returns to sign in and notifies the user', async () => {
    // Arrange
    const user = userEvent.setup()
    const { notify } = renderAuthPage()
    register.mockResolvedValue({ user: signedInUser })
    await user.click(screen.getByRole('tab', { name: 'Create account' }))
    await user.type(screen.getByRole('textbox', { name: /First name/ }), 'Avery')
    await user.type(screen.getByRole('textbox', { name: /Last name/ }), 'Morgan')
    await user.type(screen.getByRole('textbox', { name: /Email/ }), 'avery@example.com')
    await user.type(screen.getByLabelText(/^Password/), 'correct-horse')
    await user.type(screen.getByLabelText(/^Confirm password/), 'correct-horse')

    // Act
    await user.click(screen.getByRole('button', { name: 'Create account' }))

    // Assert
    await waitFor(() => expect(register).toHaveBeenCalledWith({
      firstName: 'Avery',
      lastName: 'Morgan',
      email: 'avery@example.com',
      password: 'correct-horse',
    }))
    expect(await screen.findByRole('heading', { name: 'Welcome back.' })).toBeInTheDocument()
    expect(notify).toHaveBeenCalledWith('Account created. Sign in with your email and password.')
  })
})
