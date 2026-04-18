import { useEffect, useState } from 'react'
import { PageHeader, Card, Table, Badge } from '@/components/Table'
import api, { ResultDTO, PageDTO } from '@/lib/api'

interface Waybill {
  id: number
  waybillNo: string
  orderNo: string
  driverName: string | null
  driverPhone: string | null
  vehiclePlate: string | null
  status: number
  statusName: string
  fromAddress: string
  toAddress: string
  createdAt: string
}

const statusMap: Record<number, { label: string; variant: 'success' | 'warning' | 'danger' | 'default' }> = {
  1: { label: '待分配', variant: 'warning' },
  2: { label: '已分配', variant: 'default' },
  3: { label: '运输中', variant: 'warning' },
  4: { label: '已到达', variant: 'success' },
  5: { label: '已签收', variant: 'success' },
  9: { label: '异常', variant: 'danger' },
}

export default function TransportList() {
  const [data, setData] = useState<Waybill[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    api.get<ResultDTO<PageDTO<Waybill>>>('/transport/waybills').then((res) => {
      if (res.data.code === 200) setData(res.data.data.records)
    }).catch(console.error).finally(() => setLoading(false))
  }, [])

  const columns = [
    { key: 'id', label: 'ID', width: '50px' },
    { key: 'waybillNo', label: '运单号', width: '160px' },
    { key: 'orderNo', label: '订单号', width: '160px' },
    { key: 'driverName', label: '司机' },
    { key: 'driverPhone', label: '司机电话', width: '120px' },
    { key: 'vehiclePlate', label: '车牌', width: '100px' },
    { key: 'fromAddress', label: '起点', width: '130px' },
    { key: 'toAddress', label: '终点', width: '130px' },
    { key: 'status', label: '状态', width: '90px', render: (row: Waybill) => <Badge variant={statusMap[row.status]?.variant || 'default'}>{row.statusName}</Badge> },
    { key: 'createdAt', label: '创建时间', width: '160px' },
  ]

  return (
    <div>
      <PageHeader title="运输管理" subtitle="管理运输任务与跟踪" />
      <Card>
        <Table columns={columns} data={data} loading={loading} emptyText="暂无运单数据" />
      </Card>
    </div>
  )
}
