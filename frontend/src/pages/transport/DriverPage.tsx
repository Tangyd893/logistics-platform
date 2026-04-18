import { useEffect, useState } from 'react'
import { PageHeader, Card, Table, Badge } from '@/components/Table'
import api, { ResultDTO } from '@/lib/api'

interface Driver {
  id: number
  name: string
  phone: string
  licenseNo: string
  status: number
  warehouseId: number
  warehouseName: string | null
  createdAt: string
}

const statusMap: Record<number, { label: string; variant: 'success' | 'warning' | 'danger' }> = {
  1: { label: '空闲', variant: 'success' },
  2: { label: '运输中', variant: 'warning' },
  0: { label: '禁用', variant: 'danger' },
}

export default function DriverPage() {
  const [data, setData] = useState<Driver[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    api.get<ResultDTO<Driver[]>>('/transport/drivers').then((res) => {
      if (res.data.code === 200) setData(res.data.data)
    }).catch(console.error).finally(() => setLoading(false))
  }, [])

  const columns = [
    { key: 'id', label: 'ID', width: '50px' },
    { key: 'name', label: '姓名', width: '100px' },
    { key: 'phone', label: '联系电话', width: '130px' },
    { key: 'licenseNo', label: '驾驶证号', width: '180px' },
    { key: 'warehouseName', label: '所属仓库', width: '130px', render: (row: Driver) => row.warehouseName || '-' },
    { key: 'status', label: '状态', width: '80px', render: (row: Driver) => <Badge variant={statusMap[row.status]?.variant}>{statusMap[row.status]?.label || '未知'}</Badge> },
    { key: 'createdAt', label: '创建时间', width: '150px', render: (row: Driver) => row.createdAt?.slice(0, 16).replace('T', ' ') },
  ]

  return (
    <div>
      <PageHeader title="司机管理" subtitle="管理运输司机信息" />
      <Card>
        <Table columns={columns} data={data} loading={loading} emptyText="暂无司机数据" />
      </Card>
    </div>
  )
}
