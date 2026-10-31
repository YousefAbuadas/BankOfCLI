import type { ReactNode } from 'react'
import { Brand } from '../components/branding/Brand'
import { Icon } from '../components/ui/Icon'
import type { IconName } from '../components/ui/Icon'
import type { Account, User } from '../types/domain'
import './BankLayout.css'

interface BankLayoutProps {
  user: User
  accounts: Account[]
  title: string
  navigation: {
    label: string
    icon: IconName
    active: boolean
    onClick: () => void
  }[]
  onActivity: (accountId: string) => void
  onLogout: () => void
  children: ReactNode
}

export function BankLayout({
  user,
  accounts,
  title,
  navigation,
  onActivity,
  onLogout,
  children,
}: BankLayoutProps) {
  return (
    <div className="app-shell">
      <a href="#main-content" className="skip-link">
        Skip to content
      </a>
      <aside className="sidebar">
        <Brand light />
        <div className="sidebar-section-label">WORKSPACE</div>
        <nav aria-label="Main navigation">
          {navigation.map((item) => (
            <button
              key={item.label}
              onClick={item.onClick}
              className={`nav-item ${item.active ? 'nav-active' : ''}`}
              aria-current={item.active ? 'page' : undefined}
            >
              <Icon name={item.icon} size={21} />
              <span>{item.label}</span>
              {item.active && <span className="nav-active-marker" />}
            </button>
          ))}
        </nav>
        <div className="sidebar-accounts">
          <div className="sidebar-section-label">YOUR ACCOUNTS</div>
          {accounts.map((account) => (
            <button
              className="sidebar-account"
              key={account.id}
              onClick={() => onActivity(account.id)}
            >
              <Icon
                name={account.type === 'SAVINGS' ? 'savings' : 'wallet'}
                size={18}
              />
              <span>{account.type === 'SAVINGS' ? 'Savings' : 'Checking'}</span>
              <span className="sidebar-account-digits">{account.lastFour}</span>
            </button>
          ))}
        </div>
        <div className="sidebar-bottom">
          <button className="logout-button" onClick={onLogout}>
            <Icon name="logout" size={19} />
            Sign out
          </button>
        </div>
      </aside>
      <div className="workspace">
        <header className="workspace-header">
          <span className="header-breadcrumb">
            <span className="breadcrumb-root">Personal banking</span>
            <span className="breadcrumb-divider">/</span>
            <strong>{title}</strong>
          </span>
          <div className="header-user">
            <div className="header-user-text">
              <strong>
                {user.firstName} {user.lastName}
              </strong>
              <span>Personal account</span>
            </div>
            <span className="avatar">
              {user.firstName.charAt(0)}
              {user.lastName.charAt(0)}
            </span>
          </div>
          <button
            className="mobile-signout icon-button"
            onClick={onLogout}
            aria-label="Sign out"
          >
            <Icon name="logout" />
          </button>
        </header>
        <main className="main-content" id="main-content">
          {children}
        </main>
      </div>
    </div>
  )
}
