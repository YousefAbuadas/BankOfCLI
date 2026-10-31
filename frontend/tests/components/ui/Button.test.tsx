import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { Button } from '../../../src/components/ui/Button'

describe('Button', () => {
  it('disables itself and announces its busy state while loading', () => {
    // Arrange

    // Act
    render(<Button loading>Save changes</Button>)

    // Assert
    const button = screen.getByRole('button', { name: 'Save changes' })
    expect(button).toBeDisabled()
    expect(button).toHaveAttribute('aria-busy', 'true')
  })

  it('forwards a click to the provided handler', async () => {
    // Arrange
    const user = userEvent.setup()
    const onClick = jest.fn()
    render(<Button onClick={onClick}>Continue</Button>)

    // Act
    await user.click(screen.getByRole('button', { name: 'Continue' }))

    // Assert
    expect(onClick).toHaveBeenCalledTimes(1)
  })
})
