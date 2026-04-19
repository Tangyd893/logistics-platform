# 综合物流管理系统

> 基于 Spring Boot 3 + React 18 的中小型物流企业综合管理平台

**技术栈**：Spring Boot 3 · Java 21 · PostgreSQL 16 · Redis 7 · RocketMQ 5.x · MinIO · React 18 + Vite · TypeScript · Tailwind CSS · recharts · Leaflet

---

## 功能模块

| 模块 | 功能 |
|------|------|
| 🏭 **仓储管理** | 仓库列表/详情、入库单、出库单、库位管理（含容量使用率）、库存查询 |
| 📦 **订单管理** | 订单创建/列表/详情、状态推进（确认→入库→发货→运输→到达→完成）、状态日志 |
| 🚚 **运输管理** | 运单列表/创建/状态推进（提货→送达→拒收）、司机管理、车辆管理、配送跟踪地图 |
| 📊 **数据统计** | Dashboard 概览、recharts 图表（订单状态分布/仓库容量/运输量） |
| ⚙️ **系统管理** | 用户管理、角色管理、部门管理、菜单管理（4 Tab 切换） |
| ☁️ **文件管理** | MinIO 对象存储，文件上传/删除（签收单、附件等） |

### RocketMQ 事件驱动

- 订单状态 **20（已确认）** → 自动创建入库单（分配默认仓库）
- 订单状态 **40（已发货）** → 自动创建运单（自动分配空闲司机+车辆）

---

## 快速启动

### 环境要求

| 工具 | 版本 |
|------|------|
| Java | 21 |
| Maven | 3.9+ |
| Node.js | 18+ |
| Docker | 24+ |

> ⚠️ 本项目在 4核8G 虚拟机环境下开发。调试前请先检查并杀掉残留 Java/Node 进程，避免内存不足。

### 步骤一：启动基础设施（Docker）

```bash
cd docker
docker compose up -d

# 确认服务状态
docker ps --filter "name=logistics"
# 应显示 6 个容器：postgres / redis / minio / rocketmq-namesrv / rocketmq-broker / minio-init（可选）
```

### 步骤二：编译并启动后端

```bash
cd backend

# 编译
mvn clean package -DskipTests -q

# 启动（后台运行，注意内存约束 768M）
nohup java -Xms768m -Xmx768m -Xss512k -jar target/logistics-backend-1.0.0.jar > /tmp/backend.log 2>&1 &

# 等待约 20 秒后验证
curl http://localhost:8080/api/auth/login -X POST -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
# {"code":200,"data":{"token":"..."}}
```

### 步骤三：安装并启动前端

```bash
cd frontend

# 安装依赖（首次）
npm install

# 启动开发服务器
nohup npx vite --port 3000 > /tmp/vite.log 2>&1 &
```

**访问**：`http://localhost:3000` → 登录 `admin / admin123`

### 服务端口一览

| 服务 | 端口 | 说明 |
|------|------|------|
| 后端 API | 8080 | Spring Boot + Tomcat |
| 前端 | 3000 | Vite 开发服务器 |
| PostgreSQL | 5432 | Docker，账号 `logistics_user`，密码 `logistics_pass` |
| Redis | 6379 | Docker |
| MinIO API | 9000 | Docker，账号密码 `minioadmin/minioadmin`，Bucket: `logistics` |
| MinIO Console | 9001 | 浏览器访问 `http://localhost:9001` |
| RocketMQ NameServer | 9876 | Docker |
| RocketMQ Broker | 10811 | Docker remoting 端口（非默认 8080，避免冲突） |

> ⚠️ RocketMQ Broker remoting 端口 **10811**，broker.conf 在 `docker/rocketmq_data/broker/broker.conf`

### 停止服务

```bash
# 前端
pkill -f "vite.*3000"

# 后端
pkill -f "logistics-backend"

# Docker 基础设施
cd docker && docker compose stop
```

---

## 项目结构

```
logistics-platform/
├── backend/
│   ├── src/main/java/com/logistics/
│   │   ├── common/          # 公共模块（Result/DTO/Exception/PageDTO/RocketMQ事件）
│   │   ├── system/           # 系统管理（用户/角色/部门/菜单 CRUD）
│   │   ├── warehouse/        # 仓储管理（仓库/库位/库存/入库/出库 CRUD）
│   │   ├── order/             # 订单管理（CRUD + 状态推进 + 日志）
│   │   ├── transport/         # 运输管理（运单/司机/车辆 CRUD + 轨迹）
│   │   ├── statistics/        # Dashboard 统计聚合
│   │   └── oss/              # MinIO 文件上传
│   ├── src/main/resources/
│   │   └── application.yml   # 主配置（数据源/RocketMQ/MinIO/Redis）
│   └── pom.xml
│
├── frontend/
│   ├── src/
│   │   ├── lib/api.ts          # Axios 封装 + Token 拦截器
│   │   ├── store/auth.ts       # Zustand 认证状态
│   │   ├── components/         # 通用组件（Table/Card/Badge/PageHeader）
│   │   ├── pages/
│   │   │   ├── Login.tsx / Dashboard.tsx / layout/
│   │   │   ├── warehouse/       # 仓库/库位/库存（6个页面）
│   │   │   ├── order/          # 订单（3个页面，含状态推进）
│   │   │   ├── transport/      # 运输+跟踪（5个页面）
│   │   │   ├── statistics/      # 统计图表
│   │   │   └── system/          # 系统管理（4 Tab）
│   │   └── App.tsx             # 路由配置（15个页面）
│   ├── vite.config.ts          # Vite 配置 + rollup manualChunks
│   └── package.json
│
├── docker/
│   ├── docker-compose.yml      # 基础设施编排
│   └── rocketmq_data/          # RocketMQ 数据卷
│       └── broker/broker.conf  # Broker 配置（listenPort=10811）
│
├── docs/                       # 项目文档（10份）
│   ├── 00_项目环境配置信息.md
│   ├── 01~05_需求/设计/测试文档.md
│   ├── 06_项目完成状态.md        # ⚠️ 当前进展
│   ├── 07_RocketMQ工作流程.md   # 消息队列设计与调试
│   └── RocketMQ问题记录.md     # 4个问题与根因分析
│
└── test-e2e.mjs               # Playwright E2E 测试套件（21 项全通过）
```

---

## 运行测试

```bash
# E2E 测试（需前后端运行）
node test-e2e.mjs
# 输出：21/21 通过，验证所有页面加载、功能按钮、console 错误
```

---

## 项目完成度

| 层级 | 完成度 | 说明 |
|------|--------|------|
| 基础设施 | **100%** | Docker 全家桶 |
| 后端 API | **~92%** | 核心 CRUD 完成，MinIO 文件上传已完成 |
| 前端 | **~92%** | 15 个页面全部完成 |
| RocketMQ 事件驱动 | **100%** | 订单→入库单/运单自动创建 |
| Playwright E2E | **21/21 ✅** | 全量页面+功能验证 |
| 文档 | **~95%** | 接口文档待同步字段（低优先级） |

详见 `docs/06_项目完成状态.md`

---

## 技术亮点

- **Spring Boot 3 + Java 21 虚拟线程**：高并发保持简洁同步模型
- **RocketMQ 5.x 异步解耦**：订单状态变更触发入库单/运单自动创建，全自动业务流
- **PostgreSQL + MyBatis-Plus**：Entity 自动映射，半结构化数据（JSON 列）存储订单商品明细
- **Redis 会话缓存**：JWT token 黑名单、热点数据缓存
- **MinIO 对象存储**：签收单据、附件等文件存储（有效期 7 天 Presigned URL）
- **React 18 + Vite + Tailwind CSS**：路由级分包（vendor-charts 494KB），11 个 Sidebar 菜单项
- **Leaflet 地图**：城市名→坐标映射，运单配送路线可视化（演示模式）
- **recharts 图表**：Dashboard + Statistics 统计图表

---

## 开发注意事项

### ⚠️ 内存约束（4核8G 虚拟机）

调试前检查残留进程：

```bash
# 检查残留
ps aux | grep -E 'java|node' | grep -v grep

# 清理后开始调试
kill -9 <PID>
```

### ⚠️ RocketMQ Broker 内存

`runbroker.sh` 自动按系统 1/4 内存计算堆（8G 主机→~1973M），临时方案限制容器 mem_limit=1536m。详见 `docs/RocketMQ问题记录.md`。

### ⚠️ 数据库迁移

生产环境使用 Flyway/Liquibase。开发环境 MyBatis-Plus 自动同步 schema（`ddl-auto: none` 已配置，使用现有 SQL 脚本）。

### ⚠️ Topic 命名

RocketMQ Topic 名称**不能含冒号**（`:` 是非法字符），使用下划线（`logistics_order_warehouse`）。
