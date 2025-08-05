import { Outlet, Link } from 'react-router-dom'

const Layout = () => {
  return (
    <div>
      <nav style={{ padding: 10, background: '#eee' }}>
        <Link to="/">Dashboard</Link> | <Link to="/reports">Reports</Link>
      </nav>
      <main style={{ padding: 20 }}>
        <Outlet />
      </main>
    </div>
  )
}

export default Layout
