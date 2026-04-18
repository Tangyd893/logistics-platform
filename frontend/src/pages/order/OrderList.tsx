import { useEffect, useState } from 'react'
import { PageHeader, Card, Table, Badge } from '@/components/Table'
import api, { ResultDTO, PageDTO } from '@/lib/api'

interface OrderItem {
  id: number
  skuName: string
  skuCode: string
  quantity: number
  unitPrice: number
}

interface Order {
  id: number
  orderNo: string
  customerId: number | null
  customerName: string | null
  senderName: string
  senderPhone: string
  senderAddress: string
  receiverName: string
  receiverPhone: string
  receiverAddress: string
  totalAmount: number
  weightKg: number
  volumeCbm: number
  status: number
  statusName: string
  remark: string | null
  createdBy: string | null
  creatorName: string | null
  createdAt: string
  items: OrderItem[]
  logs: any[]
}

const statusMap: Record<number, { label: string; variant: 'success' | 'warning' | 'danger' | 'default' }> = {
  10: { label: '待确认', variant: 'warning' },
  20: { label: '已确认', variant: 'default' },
  30: { label: '拣货中', variant: 'default' },
  40: { label: '已出库', variant: 'default' },
  50: { label: '运输中', variant: 'warning' },
  60: { label: '已到达', variant: 'success' },
  70: { label: '派送中', variant: 'warning' },
  80: { label: '已完成', variant: 'success' },
  90: { label: '已取消', variant: 'danger' },
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
    { key: 'id', label: 'ID', width: '50px' },
    { key: 'orderNo', label: '订单号', width: '170px' },
    { key: 'senderName', label: '发货人' },
    { key: 'senderPhone', label: '发货电话', width: '115px' },
    { key: 'receiverName', label: '收货人' },
    { key: 'receiverPhone', label: '收货电话', width: '115px' },
    { key: 'totalAmount', label: '金额', width: '90px', render: (row: Order) => `¥${row.totalAmount.toFixed(2)}` },
    { key: 'status', label: '状态', width: '90px', render: (row: Order) => <Badge variant={statusMap[row.status]?.variant || 'default'}>{row.statusName}</Badge> },
    { key: 'createdAt', label: '下单时间', width: '160px' },
  ]

  return (
    <div>
      <PageHeader title="订单管理" subtitle="管理物流订单" />
      <Card>
        <Table columns={columns} data={data} loading={loading} emptyText="暂无订单数据" />
      </Card>
    </div>
  )
}
