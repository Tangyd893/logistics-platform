import { useEffect, useState } from 'react'
import { PageHeader, Card, Table, Badge } from '@/components/Table'
import api, { ResultDTO, PageDTO } from '@/lib/api'

interface Warehouse {
  id: number
  name: string
  address: string
  managerName: string
  status: number
  createdAt: string
}

const statusMap: Record<number, { label: string; variant: 'success' | 'warning' | 'danger' }> = {
  1: { label: '启用', variant: 'success' },
  0: { label: '禁用', variant: 'danger' },
}

export default function WarehouseList() {
  const [data, setData] = useState<Warehouse[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    api.get<ResultDTO<PageDTO<Warehouse>>>('/warehouse/warehouses').then((res) => {
      if (res.data.code === 200) setData(res.data.data.records)
    }).catch(console.error).finally(() => setLoading(false))
  }, [])

  const columns = [
    { key: 'id', label: 'ID', width: '60px' },
    { key: 'name', label: '仓库名称' },
    { key: 'address', label: '地址' },
    { key: 'managerName', label: '负责人' },
    { key: 'status', label: '状态', width: '80px', render: (row: Warehouse) => <Badge variant={statusMap[row.status]?.variant}>{statusMap[row.status]?.label}</Badge> },
    { key: 'createdAt', label: '创建时间', width: '160px' },
  ]

  return (
    <div>
      <PageHeader title="仓库管理" subtitle="管理仓库信息" />
      <Card>
        <Table columns={columns} data={data} loading={loading} />
      </Card>
    </div>
  )
}
