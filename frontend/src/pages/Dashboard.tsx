import { useEffect, useState } from 'react'
import api, { ResultDTO } from '@/lib/api'

interface DashboardStats {
  warehouseCount: number
  orderCount: number
  orderPendingCount: number
  transportTaskCount: number
  transportPendingCount: number
  todayInbound: number
  todayOutbound: number
}

const cards = [
  { key: 'warehouseCount' as const, label: '仓库数量', icon: '🏭', color: '#3b82f6', bg: '#eff6ff' },
  { key: 'orderCount' as const, label: '订单总数', icon: '📦', color: '#10b981', bg: '#ecfdf5' },
  { key: 'orderPendingCount' as const, label: '待处理订单', icon: '⏳', color: '#f59e0b', bg: '#fffbeb' },
  { key: 'transportTaskCount' as const, label: '运输任务', icon: '🚚', color: '#6366f1', bg: '#eef2ff' },
  { key: 'transportPendingCount' as const, label: '运输中', icon: '📍', color: '#ef4444', bg: '#fef2f2' },
  { key: 'todayInbound' as const, label: '今日入库', icon: '📥', color: '#14b8a6', bg: '#f0fdfa' },
]

export default function Dashboard() {
  const [stats, setStats] = useState<DashboardStats | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    api.get<ResultDTO<DashboardStats>>('/statistics/dashboard').then((res) => {
      if (res.data.code === 200) setStats(res.data.data)
    }).catch(() => {}).finally(() => setLoading(false))
  }, [])

  if (loading) {
    return <div style={{ color: '#64748b', padding: '2rem' }}>加载中...</div>
  }

  return (
    <div>
      <h1 style={{ fontSize: '1.25rem', fontWeight: 'bold', color: '#1e293b', marginBottom: '1.5rem' }}>首页概览</h1>

      {/* 统计卡片 */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))', gap: '1rem', marginBottom: '2rem' }}>
        {cards.map((card) => (
          <div key={card.key} style={{ backgroundColor: card.bg, borderRadius: '0.75rem', padding: '1.25rem' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
              <div>
                <div style={{ fontSize: '0.75rem', color: '#64748b', marginBottom: '0.5rem' }}>{card.label}</div>
                <div style={{ fontSize: '1.75rem', fontWeight: 'bold', color: card.color }}>
                  {stats?.[card.key] ?? '-'}
                </div>
              </div>
              <div style={{ fontSize: '1.75rem' }}>{card.icon}</div>
            </div>
          </div>
        ))}
      </div>

      {/* 快捷操作 */}
      <div style={{ backgroundColor: 'white', borderRadius: '0.75rem', padding: '1.5rem', boxShadow: '0 1px 3px rgba(0,0,0,0.1)' }}>
        <h2 style={{ fontSize: '1rem', fontWeight: '600', color: '#1e293b', marginBottom: '1rem' }}>快捷操作</h2>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(180px, 1fr))', gap: '0.75rem' }}>
          {[
            { label: '新建入库单', icon: '📥', path: '/warehouse/inbound/new' },
            { label: '新建出库单', icon: '📤', path: '/warehouse/outbound/new' },
            { label: '创建订单', icon: '➕', path: '/order/new' },
            { label: '创建运单', icon: '🚚', path: '/transport/new' },
            { label: '库位管理', icon: '📍', path: '/warehouse/location' },
            { label: '库存查询', icon: '🔍', path: '/warehouse/inventory' },
          ].map((item) => (
            <div key={item.label} style={{ padding: '0.75rem', backgroundColor: '#f8fafc', borderRadius: '0.5rem', display: 'flex', alignItems: 'center', gap: '0.5rem', cursor: 'pointer', transition: 'background-color 0.15s' }}
              onMouseOver={(e) => e.currentTarget.style.backgroundColor = '#f1f5f9'}
              onMouseOut={(e) => e.currentTarget.style.backgroundColor = '#f8fafc'}
            >
              <span style={{ fontSize: '1.25rem' }}>{item.icon}</span>
              <span style={{ fontSize: '0.875rem', color: '#475569' }}>{item.label}</span>
            </div>
          ))}
        </div>
      </div>
    </div>
  )
}
