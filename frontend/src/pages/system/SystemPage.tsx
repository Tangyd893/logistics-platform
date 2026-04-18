import { useEffect, useState } from 'react'
import { PageHeader, Card, Table, Badge } from '@/components/Table'
import api, { ResultDTO, PageDTO } from '@/lib/api'

interface SysUser {
  id: number
  username: string
  displayName: string
  phone: string
  deptName: string
  status: number
  createdAt: string
}

const statusMap: Record<number, { label: string; variant: 'success' | 'danger' }> = {
  1: { label: '启用', variant: 'success' },
  0: { label: '禁用', variant: 'danger' },
}

export default function SystemPage() {
  const [data, setData] = useState<SysUser[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    api.get<ResultDTO<PageDTO<SysUser>>>('/system/users').then((res) => {
      if (res.data.code === 200) setData(res.data.data.records)
    }).catch(console.error).finally(() => setLoading(false))
  }, [])

  const columns = [
    { key: 'id', label: 'ID', width: '60px' },
    { key: 'username', label: '用户名', width: '120px' },
    { key: 'displayName', label: '显示名称' },
    { key: 'phone', label: '手机号', width: '130px' },
    { key: 'deptName', label: '部门', width: '120px' },
    { key: 'status', label: '状态', width: '80px', render: (row: SysUser) => <Badge variant={statusMap[row.status]?.variant}>{statusMap[row.status]?.label}</Badge> },
    { key: 'createdAt', label: '创建时间', width: '160px' },
  ]

  return (
    <div>
      <PageHeader title="系统管理" subtitle="用户、角色、权限管理" />
      <Card>
        <Table columns={columns} data={data} loading={loading} />
      </Card>
    </div>
  )
}
