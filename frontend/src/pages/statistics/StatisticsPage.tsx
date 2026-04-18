import { PageHeader, Card } from '@/components/Table'

export default function StatisticsPage() {
  return (
    <div>
      <PageHeader title="数据统计" subtitle="业务数据分析与可视化" />
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))', gap: '1rem' }}>
        <Card style={{ padding: '1.5rem' }}>
          <div style={{ color: '#64748b', fontSize: '0.875rem', marginBottom: '0.5rem' }}>本周订单量</div>
          <div style={{ fontSize: '2rem', fontWeight: 'bold', color: '#1e293b' }}>--</div>
          <div style={{ color: '#94a3b8', fontSize: '0.8rem', marginTop: '0.5rem' }}>暂无数据</div>
        </Card>
        <Card style={{ padding: '1.5rem' }}>
          <div style={{ color: '#64748b', fontSize: '0.875rem', marginBottom: '0.5rem' }}>本月营收</div>
          <div style={{ fontSize: '2rem', fontWeight: 'bold', color: '#1e293b' }}>--</div>
          <div style={{ color: '#94a3b8', fontSize: '0.8rem', marginTop: '0.5rem' }}>暂无数据</div>
        </Card>
        <Card style={{ padding: '1.5rem' }}>
          <div style={{ color: '#64748b', fontSize: '0.875rem', marginBottom: '0.5rem' }}>仓库利用率</div>
          <div style={{ fontSize: '2rem', fontWeight: 'bold', color: '#1e293b' }}>--</div>
          <div style={{ color: '#94a3b8', fontSize: '0.8rem', marginTop: '0.5rem' }}>暂无数据</div>
        </Card>
        <Card style={{ padding: '1.5rem' }}>
          <div style={{ color: '#64748b', fontSize: '0.875rem', marginBottom: '0.5rem' }}>配送准时率</div>
          <div style={{ fontSize: '2rem', fontWeight: 'bold', color: '#1e293b' }}>--</div>
          <div style={{ color: '#94a3b8', fontSize: '0.8rem', marginTop: '0.5rem' }}>暂无数据</div>
        </Card>
      </div>
    </div>
  )
}
