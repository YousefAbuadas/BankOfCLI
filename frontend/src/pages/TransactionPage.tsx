import { useState } from 'react'
import type { FormEvent } from 'react'
import type {
  Account,
  TransactionResponse,
  TransactionType,
} from '../models/banking'
import { bankService } from '../services/bankService'
import { Button, Input, Select } from '../components/ui'
import { Icon } from '../components/Icon'
import { handleTabNavigation } from '../utils/tabs'
import { formatMoney, parseAmount } from '../utils/format'

const actions: {
  type: TransactionType
  label: string
  icon: 'deposit' | 'withdraw' | 'transfer'
}[] = [
  { type: 'DEPOSIT', label: 'Deposit', icon: 'deposit' },
  { type: 'WITHDRAWAL', label: 'Withdraw', icon: 'withdraw' },
  { type: 'TRANSFER', label: 'Transfer', icon: 'transfer' },
]

interface TransactionPageProps {
  accounts: Account[]
  initialType: TransactionType
  initialAccountId?: string
  onSuccess: (response: TransactionResponse, message: string) => void
}

export function TransactionPage({
  accounts,
  initialType,
  initialAccountId,
  onSuccess,
}: TransactionPageProps) {
  const [type, setType] = useState<TransactionType>(initialType)
  const [accountId, setAccountId] = useState(
    initialAccountId || accounts[0]?.id || '',
  )
  const [destinationId, setDestinationId] = useState(
    accounts.find(
      (account) => account.id !== (initialAccountId || accounts[0]?.id),
    )?.id || '',
  )
  const [destinationMode, setDestinationMode] = useState<'own' | 'external'>(
    'own',
  )
  const [externalDestinationId, setExternalDestinationId] = useState('')
  const [amount, setAmount] = useState('')
  const [note, setNote] = useState('')
  const [errors, setErrors] = useState<Record<string, string>>({})
  const [serverError, setServerError] = useState('')
  const [busy, setBusy] = useState(false)
  const source = accounts.find((account) => account.id === accountId)
  const destination = accounts.find((account) => account.id === destinationId)
  const selectedDestinationId =
    destinationMode === 'own' ? destinationId : externalDestinationId.trim()
  const cents = parseAmount(amount)
  const action = actions.find((candidate) => candidate.type === type)!
  const remaining = source
    ? source.balanceCents + (type === 'DEPOSIT' ? 1 : -1) * (cents || 0)
    : 0

  function clearErrors() {
    setErrors({})
    setServerError('')
  }

  function changeAccount(id: string) {
    setAccountId(id)
    if (destinationId === id)
      setDestinationId(accounts.find((account) => account.id !== id)?.id || '')
    clearErrors()
  }

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const nextErrors: Record<string, string> = {}
    if (!source) nextErrors.accountId = 'Choose an account.'
    if (!amount.trim()) nextErrors.amount = 'Enter an amount.'
    else if (cents === null)
      nextErrors.amount =
        'Enter a positive amount with no more than two decimal places.'
    else if (type !== 'DEPOSIT' && source && cents > source.balanceCents)
      nextErrors.amount = `Available balance: ${formatMoney(source.balanceCents)}. Enter a smaller amount.`
    else if (
      type === 'DEPOSIT' &&
      source &&
      !Number.isSafeInteger(source.balanceCents + cents)
    )
      nextErrors.amount = 'Enter a smaller amount.'
    if (
      type === 'TRANSFER' &&
      (!selectedDestinationId || selectedDestinationId === source?.id)
    )
      nextErrors.destinationId = 'Choose a different destination account.'
    setErrors(nextErrors)
    setServerError('')
    if (Object.keys(nextErrors).length || cents === null) return
    setBusy(true)
    try {
      const response = await bankService.submitTransaction({
        type,
        accountId,
        amountCents: cents,
        ...(type === 'TRANSFER'
          ? { destinationAccountId: selectedDestinationId }
          : {}),
        ...(note.trim() ? { note: note.trim() } : {}),
      })
      const message =
        type === 'TRANSFER'
          ? `${formatMoney(cents)} transferred to ${destination?.name || `account ${selectedDestinationId}`}.`
          : type === 'DEPOSIT'
            ? `${formatMoney(cents)} deposited into ${source!.name}.`
            : `${formatMoney(cents)} withdrawn from ${source!.name}.`
      onSuccess(response, message)
      setAmount('')
      setNote('')
    } catch (error) {
      setServerError(
        error instanceof Error
          ? error.message
          : 'Unable to complete this transaction. Please try again.',
      )
    } finally {
      setBusy(false)
    }
  }

  return (
    <>
      <div className="page-heading">
        <div>
          <span className="eyebrow">MAKE YOUR NEXT MOVE</span>
          <h1>Transaction center</h1>
          <p>Deposit, withdraw, or move money between your accounts.</p>
        </div>
      </div>
      <div className="transaction-layout">
        <section className="panel transaction-form-panel">
          <div
            className="transaction-tabs"
            role="tablist"
            aria-label="Transaction type"
            onKeyDown={handleTabNavigation}
          >
            {actions.map((item) => (
              <button
                type="button"
                role="tab"
                id={`tab-${item.type}`}
                tabIndex={type === item.type ? 0 : -1}
                aria-controls="transaction-form"
                aria-selected={type === item.type}
                key={item.type}
                disabled={busy}
                onClick={() => {
                  setType(item.type)
                  clearErrors()
                }}
              >
                <Icon name={item.icon} size={19} />
                {item.label}
              </button>
            ))}
          </div>
          <div
            className="transaction-form-content"
            id="transaction-form"
            role="tabpanel"
            aria-labelledby={`tab-${type}`}
          >
            <h2>
              {type === 'TRANSFER'
                ? 'Transfer funds'
                : type === 'DEPOSIT'
                  ? 'Deposit money'
                  : 'Withdraw money'}
            </h2>
            <p className="transaction-description">
              {type === 'TRANSFER'
                ? 'Choose one of your accounts or enter a destination account ID.'
                : type === 'DEPOSIT'
                  ? 'Select an account and the amount to add.'
                  : 'Select an account and the amount to withdraw.'}
            </p>
            <form onSubmit={submit} noValidate>
              <fieldset disabled={busy}>
                <Select
                  label={type === 'TRANSFER' ? 'From account' : 'Account'}
                  value={accountId}
                  onChange={(event) => changeAccount(event.target.value)}
                  error={errors.accountId}
                  required
                >
                  <option value="" disabled>
                    Choose an account
                  </option>
                  {accounts.map((account) => (
                    <option key={account.id} value={account.id}>
                      {account.name} · •• {account.lastFour}
                    </option>
                  ))}
                </Select>
                {source && (
                  <p className="available-balance">
                    Available balance{' '}
                    <strong>{formatMoney(source.balanceCents)}</strong>
                  </p>
                )}
                {type === 'TRANSFER' && (
                  <>
                    <div
                      className="destination-mode"
                      role="group"
                      aria-label="Destination account type"
                    >
                      <button
                        type="button"
                        aria-pressed={destinationMode === 'own'}
                        onClick={() => {
                          setDestinationMode('own')
                          clearErrors()
                        }}
                      >
                        My accounts
                      </button>
                      <button
                        type="button"
                        aria-pressed={destinationMode === 'external'}
                        onClick={() => {
                          setDestinationMode('external')
                          clearErrors()
                        }}
                      >
                        Account ID
                      </button>
                    </div>
                    {destinationMode === 'own' ? (
                      <Select
                        label="To account"
                        value={destinationId}
                        onChange={(event) => {
                          setDestinationId(event.target.value)
                          clearErrors()
                        }}
                        error={errors.destinationId}
                        required
                      >
                        <option value="" disabled>
                          Choose an account
                        </option>
                        {accounts
                          .filter((account) => account.id !== accountId)
                          .map((account) => (
                            <option key={account.id} value={account.id}>
                              {account.name} · •• {account.lastFour}
                            </option>
                          ))}
                      </Select>
                    ) : (
                      <Input
                        label="Destination account ID"
                        name="destinationAccountId"
                        placeholder="Enter account ID"
                        value={externalDestinationId}
                        onChange={(event) => {
                          setExternalDestinationId(event.target.value)
                          clearErrors()
                        }}
                        error={errors.destinationId}
                        hint="For an account at Bank of CLI."
                        autoComplete="off"
                        maxLength={80}
                        required
                      />
                    )}
                  </>
                )}
                <Input
                  label="Amount"
                  name="amount"
                  inputMode="decimal"
                  placeholder="0.00"
                  value={amount}
                  onChange={(event) => {
                    setAmount(event.target.value)
                    clearErrors()
                  }}
                  leading="$"
                  error={errors.amount}
                  required
                  maxLength={16}
                />
                <div className="amount-presets" aria-label="Suggested amounts">
                  {[50, 100, 250, 500].map((value) => (
                    <button
                      type="button"
                      key={value}
                      onClick={() => {
                        setAmount(value.toFixed(2))
                        clearErrors()
                      }}
                    >
                      ${value}
                    </button>
                  ))}
                </div>
                <Input
                  label="Note (optional)"
                  name="note"
                  placeholder="What's this transaction for?"
                  value={note}
                  onChange={(event) => setNote(event.target.value)}
                  maxLength={80}
                  hint="Up to 80 characters."
                />
              </fieldset>
              {serverError && (
                <p className="form-alert" role="alert">
                  <Icon name="info" size={18} />
                  {serverError}
                </p>
              )}
              <Button
                type="submit"
                className="full-width transaction-submit"
                loading={busy}
              >
                {busy
                  ? 'Processing…'
                  : `${action.label} ${type === 'TRANSFER' ? 'funds' : 'money'}`}
              </Button>
            </form>
          </div>
        </section>
        <aside className="transaction-summary">
          <div className="summary-heading">
            <span className="summary-icon">
              <Icon name={action.icon} size={25} />
            </span>
            <span className="eyebrow">TRANSACTION SUMMARY</span>
          </div>
          <div className="summary-amount">{formatMoney(cents || 0)}</div>
          <p className="summary-action">{action.label} amount</p>
          <dl className="summary-details">
            <div>
              <dt>{type === 'TRANSFER' ? 'From' : 'Account'}</dt>
              <dd>
                {source?.name || 'Choose an account'}
                <span>{source ? `•••• ${source.lastFour}` : ''}</span>
              </dd>
            </div>
            {type === 'TRANSFER' && (
              <div>
                <dt>To</dt>
                <dd>
                  {destinationMode === 'own'
                    ? destination?.name || 'Choose an account'
                    : externalDestinationId.trim() || 'Enter an account ID'}
                  <span>
                    {destinationMode === 'own'
                      ? destination
                        ? `•••• ${destination.lastFour}`
                        : ''
                      : externalDestinationId.trim()
                        ? 'Account ID'
                        : ''}
                  </span>
                </dd>
              </div>
            )}
            <div className="summary-balance">
              <dt>
                Balance after{' '}
                {type === 'TRANSFER'
                  ? 'transfer'
                  : type === 'DEPOSIT'
                    ? 'deposit'
                    : 'withdrawal'}
              </dt>
              <dd className={remaining < 0 ? 'negative-balance' : ''}>
                {formatMoney(remaining)}
              </dd>
            </div>
          </dl>
          <div className="summary-note">
            <Icon name="info" size={18} />
            <p>
              Your balance and activity update after the transaction is
              completed.
            </p>
          </div>
          <span className="summary-preview-label">
            Preview with sample data
          </span>
        </aside>
      </div>
    </>
  )
}
