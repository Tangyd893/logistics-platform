import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { useAuthStore } from '@/store/auth'
import Login from '@/pages/Login'
import Layout from '@/pages/layout/Layout'
import Dashboard from '@/pages/Dashboard'
import WarehouseList from '@/pages/warehouse/WarehouseList'
import WarehouseDetail from '@/pages/warehouse/WarehouseDetail'
import OrderList from '@/pages/order/OrderList'
import OrderCreate from '@/pages/order/OrderCreate'
import TransportList from '@/pages/transport/TransportList'
import StatisticsPage from '@/pages/statistics/StatisticsPage'
import SystemPage from '@/pages/system/SystemPage'

function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const token = useAuthStore((s) => s.token)
  if (!token) return <Navigate to="/login" replace />
  return <>{children}</>
}

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route
          path="/"
          element={
            <ProtectedRoute>
              <Layout />
            </ProtectedRoute>
          }
        >
          <Route index element={<Dashboard />} />
          <Route path="warehouse" element={<WarehouseList />} />
          <Route path="warehouse/:tab" element={<WarehouseDetail />} />
          <Route path="order" element={<OrderList />} />
          <Route path="order/new" element={<OrderCreate />} />
          <Route path="transport" element={<TransportList />} />
          <Route path="statistics" element={<StatisticsPage />} />
          <Route path="system" element={<SystemPage />} />
        </Route>
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  )
}
