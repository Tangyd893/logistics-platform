import { useEffect, useState } from 'react'
import { PageHeader, Card, Table, Badge } from '@/components/Table'
import api, { ResultDTO, PageDTO } from '@/lib/api'

interface TransportTask {
  id: number
  waybillNo: string
  orderNo: string
  driverName: string
  vehiclePlate: string
  status: number
  fromAddress: string
  toAddress: string
  createdAt: string
}

const statusMap: Record<number, { label: string; variant: 'success' | 'warning' | 'danger' | 'default' }> = {
  0: { label: '待分配', variant: 'warning' },
  1: { label: '已分配', variant: 'default' },
  2: { label: '运输中', variant: 'warning' },
  3: { label: '已到达', variant: 'success' },
  4: { label: '已签收', variant: 'success' },
  9: { label: '异常', variant: 'danger' },
}

export default function TransportList() {
  const [data, setData] = useState<TransportTask[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    api.get<ResultDTO<PageDTO<TransportTask>>>('/transport/tasks').then((res) => {
      if (res.data.code === 200) setData(res.data.data.records)
    }).catch(console.error).finally(() => setLoading(false))
  }, [])

  const columns = [
    { key: 'id', label: 'ID', width: '60px' },
    { key: 'waybillNo', label: '运单号', width: '150px' },
    { key: 'orderNo', label: '订单号', width: '150px' },
    { key: 'driverName', label: '司机' },
    { key: 'vehiclePlate', label: '车牌', width: '100px' },
    { key: 'fromAddress', label: '起点', width: '150px' },
    { key: 'toAddress', label: '终点', width: '150px' },
    { key: 'status', label: '状态', width: '90px', render: (row: TransportTask) => <Badge variant={statusMap[row.status]?.variant}>{statusMap[row.status]?.label || row.status}</Badge> },
    { key: 'createdAt', label: '创建时间', width: '160px' },
  ]

  return (
    <div>
      <PageHeader title="运输管理" subtitle="管理运输任务与跟踪" />
      <Card>
        <Table columns={columns} data={data} loading={loading} />
      </Card>
    </div>
  )
}
