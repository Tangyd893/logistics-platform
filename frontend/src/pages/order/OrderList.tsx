import { useEffect, useState } from 'react'
import { PageHeader, Card, Table, Badge } from '@/components/Table'
import api, { ResultDTO, PageDTO } from '@/lib/api'

interface Order {
  id: number
  orderNo: string
  senderName: string
  senderPhone: string
  receiverName: string
  receiverPhone: string
  status: number
  totalFee: number
  createdAt: string
}

const statusMap: Record<number, { label: string; variant: 'success' | 'warning' | 'danger' | 'default' }> = {
  0: { label: '待支付', variant: 'warning' },
  1: { label: '已支付', variant: 'default' },
  2: { label: '已接单', variant: 'default' },
  3: { label: '拣货中', variant: 'default' },
  4: { label: '已出库', variant: 'success' },
  5: { label: '已完成', variant: 'success' },
  9: { label: '已取消', variant: 'danger' },
}

export default function OrderList() {
  const [data, setData] = useState<Order[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    api.get<ResultDTO<PageDTO<Order>>>('/order/orders').then((res) => {
      if (res.data.code === 200) setData(res.data.data.records)
    }).catch(console.error).finally(() => setLoading(false))
  }, [])

  const columns = [
    { key: 'id', label: 'ID', width: '60px' },
    { key: 'orderNo', label: '订单号', width: '150px' },
    { key: 'senderName', label: '发货人' },
    { key: 'senderPhone', label: '发货电话', width: '120px' },
    { key: 'receiverName', label: '收货人' },
    { key: 'receiverPhone', label: '收货电话', width: '120px' },
    { key: 'totalFee', label: '费用', width: '100px', render: (row: Order) => `¥${(row.totalFee / 100).toFixed(2)}` },
    { key: 'status', label: '状态', width: '90px', render: (row: Order) => <Badge variant={statusMap[row.status]?.variant}>{statusMap[row.status]?.label || row.status}</Badge> },
    { key: 'createdAt', label: '下单时间', width: '160px' },
  ]

  return (
    <div>
      <PageHeader title="订单管理" subtitle="管理物流订单" />
      <Card>
        <Table columns={columns} data={data} loading={loading} />
      </Card>
    </div>
  )
}
