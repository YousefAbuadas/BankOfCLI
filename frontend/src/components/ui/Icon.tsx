export type IconName =
  | 'bank'
  | 'grid'
  | 'wallet'
  | 'history'
  | 'deposit'
  | 'withdraw'
  | 'transfer'
  | 'logout'
  | 'chevron'
  | 'check'
  | 'close'
  | 'eye'
  | 'eyeOff'
  | 'lock'
  | 'savings'
  | 'menu'
  | 'search'
  | 'info'
  | 'arrowUpRight'

const paths: Record<IconName, React.ReactNode> = {
  bank: (
    <>
      <path d="m3 9 9-5 9 5M4 10h16M6 10v8m6-8v8m6-8v8M3 21h18M4 18h16" />
    </>
  ),
  grid: (
    <>
      <rect x="3" y="3" width="7" height="7" rx="1.5" />
      <rect x="14" y="3" width="7" height="7" rx="1.5" />
      <rect x="3" y="14" width="7" height="7" rx="1.5" />
      <rect x="14" y="14" width="7" height="7" rx="1.5" />
    </>
  ),
  wallet: (
    <>
      <path d="M20 8V6a2 2 0 0 0-2-2H5a3 3 0 0 0 0 6h15v10H5a3 3 0 0 1-3-3V7" />
      <path d="M20 12h-5v4h5" />
      <path d="M17 14h.01" />
    </>
  ),
  history: (
    <>
      <path d="M3 11a9 9 0 1 1 2 7M3 4v7h7" />
      <path d="M12 7v5l3 2" />
    </>
  ),
  deposit: (
    <>
      <path d="M12 3v11m-4-4 4 4 4-4M5 15v5h14v-5" />
    </>
  ),
  withdraw: (
    <>
      <path d="M12 14V3m-4 4 4-4 4 4M5 15v5h14v-5" />
    </>
  ),
  transfer: (
    <>
      <path d="M3 7h17m-4-4 4 4-4 4M21 17H4m4-4-4 4 4 4" />
    </>
  ),
  logout: (
    <>
      <path d="M10 4H4v16h6m-1-8h12m-4-4 4 4-4 4" />
    </>
  ),
  chevron: <path d="m9 5 7 7-7 7" />,
  check: <path d="m5 12 4 4L19 6" />,
  close: <path d="m6 6 12 12M6 18 18 6" />,
  eye: (
    <>
      <path d="M2 12s4-7 10-7 10 7 10 7-4 7-10 7-10-7-10-7Z" />
      <circle cx="12" cy="12" r="3" />
    </>
  ),
  eyeOff: (
    <>
      <path d="m3 3 18 18M10 5c7-2 12 7 12 7a22 22 0 0 1-4 5M7 7a21 21 0 0 0-5 5s4 7 10 7a12 12 0 0 0 4-1" />
    </>
  ),
  lock: (
    <>
      <rect x="5" y="10" width="14" height="11" rx="2" />
      <path d="M8 10V7a4 4 0 0 1 8 0v3M12 14v3" />
    </>
  ),
  savings: (
    <>
      <path d="M5 9a6 6 0 0 1 6-4h4l3-2v5l3 2v6l-3 1-1 4h-3l-1-3H9l-1 3H5l-1-5-2-2V9h3Z" />
      <path d="M10 8h4m3 3h.01" />
    </>
  ),
  menu: <path d="M4 6h16M4 12h16M4 18h16" />,
  search: (
    <>
      <circle cx="10.5" cy="10.5" r="6.5" />
      <path d="m16 16 5 5" />
    </>
  ),
  info: (
    <>
      <circle cx="12" cy="12" r="9" />
      <path d="M12 11v6m0-10h.01" />
    </>
  ),
  arrowUpRight: <path d="M6 18 18 6M6 6h12v12" />,
}

export function Icon({ name, size = 20 }: { name: IconName; size?: number }) {
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.7"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      {paths[name]}
    </svg>
  )
}
