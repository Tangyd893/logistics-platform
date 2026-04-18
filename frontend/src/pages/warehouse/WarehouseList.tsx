import { useEffect, useState } from 'react'
import { PageHeader, Card, Table, Badge } from '@/components/Table'
import api, { ResultDTO, PageDTO } from '@/lib/api'

interface Warehouse {
  id: number
  code: string
  name: string
  address: string
  manager: string | null
  phone: string | null
  totalCapacity: number
  usedCapacity: number
  availableCapacity: number
  status: number
  statusName: string
  remark: string | null
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
    { key: 'id', label: 'ID', width: '50px' },
    { key: 'code', label: '仓库编码', width: '110px' },
    { key: 'name', label: '仓库名称' },
    { key: 'address', label: '地址' },
    { key: 'manager', label: '负责人', width: '90px' },
    { key: 'phone', label: '联系电话', width: '120px' },
    { key: 'usedCapacity', label: '已用容量', width: '100px', render: (row: Warehouse) => `${row.usedCapacity} / ${row.totalCapacity}` },
    { key: 'status', label: '状态', width: '70px', render: (row: Warehouse) => <Badge variant={statusMap[row.status]?.variant}>{row.statusName}</Badge> },
  ]

  return (
    <div>
      <PageHeader title="仓库管理" subtitle="管理仓库信息" />
      <Card>
        <Table columns={columns} data={data} loading={loading} emptyText="暂无仓库数据" />
      </Card>
    </div>
  )
}
