import { parseAmount } from '../../src/utils/money'

describe('parseAmount', () => {
  it('converts dollar amounts to integer cents', () => {
    // Arrange
    const input = ' 42.5 '

    // Act
    const result = parseAmount(input)

    // Assert
    expect(result).toBe(4250)
  })

  it('rejects values that are not positive amounts with at most two decimals', () => {
    // Arrange
    const invalidAmounts = ['0', '0.00', '.50', '-1.00', '1.234', 'one']

    // Act
    const results = invalidAmounts.map(parseAmount)

    // Assert
    expect(results).toEqual(invalidAmounts.map(() => null))
  })
})
