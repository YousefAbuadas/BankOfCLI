import './DashboardSkeleton.css'

export function DashboardSkeleton() {
  return (
    <div
      className="dashboard-skeleton"
      role="status"
      aria-label="Loading your accounts"
    >
      <span className="sr-only">Loading your accounts…</span>
      <div className="skeleton skeleton-heading" />
      <div className="skeleton-grid">
        {[1, 2, 3].map((key) => (
          <div className="skeleton skeleton-card" key={key} />
        ))}
      </div>
      <div className="skeleton skeleton-table" />
    </div>
  )
}
