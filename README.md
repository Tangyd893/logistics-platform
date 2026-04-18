# 综合物流管理系统

> 基于 Spring Boot 3 + React 18 的中小型物流企业综合管理平台

**技术栈**：Spring Boot 3 · Java 21 · PostgreSQL 16 · Redis 7 · RocketMQ 5.x · React 18 · Vite · TypeScript · Tailwind CSS

---

## 功能模块

| 模块 | 功能 |
|------|------|
| 🏭 **仓储管理** | 库位管理、库存查询、入库管理、出库管理 |
| 📦 **订单管理** | 订单创建、状态流转、费用计算、物流查询 |
| 🚚 **运输管理** | 运单管理、司机管理、车辆管理、配送跟踪 |
| ⚙️ **系统管理** | 用户管理、角色管理、菜单管理、部门管理 |
| 📊 **数据统计** | 库存仪表盘、订单报表、运输报表 |

---

## 快速启动

### 环境要求

| 工具 | 版本 |
|------|------|
| Java | 21 |
| Maven | 3.9+ |
| Node.js | 18+ |
| Docker | 24+ |

### 步骤一：启动基础设施

```bash
cd docker
docker compose up -d postgres redis minio rocketmq-namesrv rocketmq-broker

# 确认服务状态
docker ps --filter "name=logistics"
```

### 步骤二：启动后端

```bash
cd backend
mvn clean package -DskipTests -q
java -Xms768m -Xmx768m -Xss512k -jar target/logistics-backend-1.0.0.jar &
```

### 步骤三：启动前端

```bash
cd frontend
npm install
npm run dev -- --port 3000
```

**访问**：`http://localhost:3000` → 登录 `admin / admin123`

### 服务端口一览

| 服务 | 端口 | 说明 |
|------|------|------|
| 后端 API | 8080 | Spring Boot + Tomcat |
| 前端 | 3000 | Vite 开发服务器 |
| PostgreSQL | 5432 | Docker |
| Redis | 6379 | Docker |
| MinIO API | 9000 | Docker |
| MinIO Console | 9001 | Docker |
| RocketMQ NameServer | 9876 | Docker |
| RocketMQ Broker | 10811 | Docker remoting 端口 |

> ⚠️ RocketMQ Broker 使用端口 **10811**（非默认 8080，避免与后端冲突）

---

## 项目结构

```
logistics-platform/
├── backend/
│   ├── src/main/java/com/logistics/
│   │   ├── common/        # 公共模块（Entity/DTO/Exception/枚举）
│   │   ├── system/        # 系统管理（用户/角色/菜单/部门）
│   │   ├── warehouse/     # 仓储管理（仓库/库位/库存/入库/出库）
│   │   ├── order/         # 订单管理（订单/订单项/状态流转）
│   │   ├── transport/     # 运输管理（运单/司机/车辆/跟踪）
│   │   └── statistics/    # 统计模块（Dashboard 数据聚合）
│   ├── src/main/resources/
│   │   ├── application.yml     # 主配置
│   │   └── mapper/             # MyBatis XML 映射
│   └── pom.xml
│
├── frontend/
│   ├── src/
│   │   ├── lib/api.ts         # Axios 封装 + 统一拦截器
│   │   ├── store/auth.ts       # Zustand 认证状态
│   │   ├── components/Table.tsx # 通用表格/卡片/Badge 组件
│   │   ├── pages/
│   │   │   ├── Login.tsx           # 登录页
│   │   │   ├── Dashboard.tsx        # 首页仪表盘
│   │   │   ├── layout/Layout.tsx    # 侧边栏布局
│   │   │   ├── warehouse/           # 仓库管理页面
│   │   │   ├── order/              # 订单管理页面
│   │   │   ├── transport/           # 运输管理页面
│   │   │   ├── statistics/          # 数据统计页面
│   │   │   └── system/              # 系统管理页面
│   │   └── App.tsx                  # 路由配置
│   ├── vite.config.ts
│   └── package.json
│
├── docker/
│   ├── docker-compose.yml      # 基础设施编排
│   └── rocketmq_data/          # RocketMQ 数据卷（挂载配置）
│       └── broker/broker.conf  # Broker 配置（listenPort=10811）
│
└── docs/                      # 项目文档（15份）
    ├── 00_项目环境配置信息.md
    ├── 01_软件需求规格说明书.md
    ├── 02_系统概要设计说明书.md
    ├── 03_数据库设计说明书.md
    ├── 04_接口设计说明书.md
    ├── 05_测试计划说明书.md
    ├── 08_人机协作工作流.md
    └── RocketMQ问题记录.md      # ⚠️ RocketMQ 问题与解决方案
```

---

## 文档索引

| 编号 | 文档 |
|------|------|
| 00 | 项目环境配置信息 |
| 01 | 软件需求规格说明书 |
| 02 | 系统概要设计说明书 |
| 03 | 数据库设计说明书 |
| 04 | 接口设计说明书 |
| 05 | 测试计划说明书 |
| 08 | 人机协作工作流 |
| - | **RocketMQ问题记录** ← 必读 |

---

## 技术亮点

- **Spring Boot 3 + Java 21 虚拟线程**：高并发场景下保持简洁的同步编程模型
- **RocketMQ 5.x 异步解耦**：订单状态变更触发运输任务自动创建
- **PostgreSQL JSON 列**：订单商品明细、状态变更日志等半结构化数据
- **Redis 会话缓存**：JWT token 黑名单、热点数据缓存
- **MinIO 对象存储**：签收单据、照片等文件存储
- **React 18 + Zustand**：轻量状态管理，TanStack Query 处理服务端状态

---

## 开发注意事项

### ⚠️ 内存约束（4核8G 虚拟机）

调试前检查残留进程：

```bash
# 检查 Java/Node 残留
ps aux | grep -E 'java|node' | grep -v grep

# 杀掉不需要的进程后开始调试
kill <PID>
```

### ⚠️ RocketMQ Broker 内存

Broker remoting 端口 **10811**（非默认 8080）。详见 `docs/RocketMQ问题记录.md`。

### ⚠️ 数据库迁移

使用 JPA `spring.jpa.hibernate.ddl-auto=update`，开发环境自动同步 schema。生产环境使用 Flyway/Liquibase。
