import { viMessages } from '../../constants/messages.vi'

const navigationItems = [
  viMessages.navigation.dashboard,
  viMessages.navigation.users,
  viMessages.navigation.vocabulary,
  viMessages.navigation.topics,
  viMessages.navigation.quizzes,
  viMessages.navigation.aiContent,
  viMessages.navigation.statistics,
  viMessages.navigation.aiUsage,
  viMessages.navigation.auditLogs,
]

export function AdminBootstrapShell() {
  return (
    <div className="admin-shell">
      <header className="admin-shell__topbar">
        <p className="admin-shell__brand">{viMessages.app.name}</p>
        <p className="admin-shell__role">{viMessages.app.role}</p>
      </header>

      <aside className="admin-shell__sidebar" aria-label={viMessages.navigation.label}>
        <p className="admin-shell__nav-title">{viMessages.navigation.label}</p>
        <nav>
          <ul className="admin-shell__nav-list">
            {navigationItems.map((item, index) => (
              <li key={item}>
                <span
                  className={`admin-shell__nav-item${index === 0 ? ' admin-shell__nav-item--active' : ''}`}
                  aria-current={index === 0 ? 'page' : undefined}
                >
                  {item}
                </span>
              </li>
            ))}
          </ul>
        </nav>
      </aside>

      <main className="admin-shell__main">
        <div className="admin-shell__content">
          <p className="admin-shell__eyebrow">{viMessages.bootstrap.eyebrow}</p>
          <h1 className="admin-shell__title">{viMessages.bootstrap.title}</h1>
          <p className="admin-shell__description">{viMessages.bootstrap.description}</p>

          <section className="admin-shell__card" aria-labelledby="bootstrap-status-title">
            <h2 id="bootstrap-status-title">{viMessages.bootstrap.statusTitle}</h2>
            <p className="admin-shell__description">{viMessages.bootstrap.statusDescription}</p>
            <p className="admin-shell__status">{viMessages.bootstrap.statusReady}</p>
          </section>
        </div>
      </main>
    </div>
  )
}
