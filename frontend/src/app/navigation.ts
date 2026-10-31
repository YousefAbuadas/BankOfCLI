import type { IconName } from '../components/ui/Icon'

export type View = 'overview' | 'transactions' | 'activity'

export const navigation: { view: View; label: string; icon: IconName }[] = [
  { view: 'overview', label: 'Overview', icon: 'grid' },
  { view: 'transactions', label: 'Transactions', icon: 'transfer' },
  { view: 'activity', label: 'Activity', icon: 'history' },
]
