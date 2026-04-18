import { useEffect, useState } from 'react'
import { PageHeader, Card, Table, Badge } from '@/components/Table'
import api, { ResultDTO } from '@/lib/api'

interface Vehicle {
  id: number
  plateNo: string
  type: string
  capacityKg: number
  capacityCbm: number
  status: number
  createdAt: string
}

const statusMap: Record<number, { label: string; variant: 'success' | 'warning' | 'danger' }> = {
  1: { label: '空闲', variant: 'success' },
  2: { label: '运输中', variant: 'warning' },
  0: { label: '维修中', variant: 'danger' },
}

export default function VehiclePage() {
  const [data, setData] = useState<Vehicle[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    api.get<ResultDTO<Vehicle[]>>('/transport/vehicles').then((res) => {
      if (res.data.code === 200) setData(res.data.data)
    }).catch(console.error).finally(() => setLoading(false))
  }, [])

  const columns = [
    { key: 'id', label: 'ID', width: '50px' },
    { key: 'plateNo', label: '车牌号', width: '100px', render: (row: Vehicle) => <span style={{ fontWeight: 600, fontFamily: 'monospace' }}>{row.plateNo}</span> },
    { key: 'type', label: '车型', width: '100px' },
    { key: 'capacityKg', label: '载重(kg)', width: '100px', render: (row: Vehicle) => row.capacityKg.toFixed(0) },
    { key: 'capacityCbm', label: '容积(m³)', width: '100px', render: (row: Vehicle) => row.capacityCbm.toFixed(1) },
    { key: 'status', label: '状态', width: '80px', render: (row: Vehicle) => <Badge variant={statusMap[row.status]?.variant}>{statusMap[row.status]?.label || '未知'}</Badge> },
    { key: 'createdAt', label: '创建时间', width: '150px', render: (row: Vehicle) => row.createdAt?.slice(0, 16).replace('T', ' ') },
  ]

  return (
    <div>
      <PageHeader title="车辆管理" subtitle="管理运输车辆信息" />
      <Card>
        <Table columns={columns} data={data} loading={loading} emptyText="暂无车辆数据" />
      </Card>
    </div>
  )
}
