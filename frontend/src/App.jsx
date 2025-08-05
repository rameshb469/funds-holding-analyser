import { Routes, Route } from 'react-router-dom'
import StockView from './pages/StockView'
import Dashboard from './pages/Dashboard'
import NotFound from './pages/NotFound'
import Reports from './pages/Reports'
import DownloadUrlReport from './pages/DownloadUrlReport'

function App() {
  return (
    <Routes>
      <Route path="/">
        <Route index element={<Dashboard />} />
        <Route path="/StockView" element={<StockView />} />
        <Route path='/reports/configuration' element={<Reports />} />
        <Route path='/reports/download-urls' element={<DownloadUrlReport/>} />
        <Route path="*" element={<NotFound />} />
      </Route>
    </Routes>
  )
}

export default App
