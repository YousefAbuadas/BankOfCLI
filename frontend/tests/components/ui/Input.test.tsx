import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { Input } from '../../../src/components/ui/Input'

describe('Input', () => {
  it('associates the label and hint with the input', () => {
    // Arrange

    // Act
    render(<Input label="Email" type="email" hint="We will never share it." />)

    // Assert
    const input = screen.getByRole('textbox', { name: 'Email' })
    expect(input).toHaveAttribute('type', 'email')
    expect(input).toHaveAccessibleDescription('We will never share it.')
  })

  it('marks an invalid input and exposes its error message', () => {
    // Arrange

    // Act
    render(<Input label="Email" error="Enter a valid email address." />)

    // Assert
    const input = screen.getByRole('textbox', { name: 'Email' })
    expect(input).toHaveAttribute('aria-invalid', 'true')
    expect(input).toHaveAccessibleDescription('Enter a valid email address.')
  })

  it('lets a user reveal and hide a password', async () => {
    // Arrange
    const user = userEvent.setup()
    render(<Input label="Password" type="password" />)
    const password = screen.getByLabelText('Password')

    // Act
    await user.click(screen.getByRole('button', { name: 'Show Password' }))

    // Assert
    expect(password).toHaveAttribute('type', 'text')

    // Act
    await user.click(screen.getByRole('button', { name: 'Hide Password' }))

    // Assert
    expect(password).toHaveAttribute('type', 'password')
  })
})
