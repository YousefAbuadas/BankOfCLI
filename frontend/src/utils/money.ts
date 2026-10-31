export function parseAmount(value: string): number | null {
  const trimmed = value.trim()
  if (!/^\d+(\.\d{1,2})?$/.test(trimmed)) return null
  const [dollars, cents = ''] = trimmed.split('.')
  const amount = Number(dollars) * 100 + Number(cents.padEnd(2, '0'))
  return Number.isSafeInteger(amount) && amount > 0 ? amount : null
}
