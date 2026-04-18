# 综合物流管理系统

> 基于 Spring Boot 3 + React 18 的中小型物流企业综合管理平台

**技术栈**：Spring Boot 3 · Java 21 · PostgreSQL 16 · Redis 7 · RocketMQ 5.x · React 18 + Vite · TypeScript

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

### 步骤一：启动基础设施（Docker）

```bash
cd docker
docker compose up -d postgres redis minio rocketmq
```

### 步骤二：初始化数据库

```bash
# 创建数据库和用户
docker exec -i logistics_postgres psql -U postgres \
  -c "CREATE DATABASE logistics; CREATE USER logistics_user WITH PASSWORD 'logistics_pass'; GRANT ALL PRIVILEGES ON DATABASE logistics TO logistics_user;"

# 初始化表结构
docker exec -i logistics_postgres psql -U logistics_user -d logistics \
  < backend/sql/init.sql
```

### 步骤三：启动后端

```bash
cd backend
mvn clean package -DskipTests -q
java -Xms768m -Xmx768m -Xss512k -jar target/logistics-backend-1.0.0.jar &
```

### 步骤四：构建并启动前端

```bash
cd frontend
npm install
VITE_API_BASE_URL=http://localhost:8080 npx vite build
npx serve -l 3000 -s dist &
```

**访问**：`http://localhost:3000` → 登录 `admin / admin123`

---

## 项目结构

```
logistics-platform/
├── backend/
│   ├── src/main/java/com/logistics/
│   │   ├── common/        # 公共模块（Entity/DTO/Exception）
│   │   ├── system/        # 系统管理
│   │   ├── warehouse/     # 仓储管理
│   │   ├── order/        # 订单管理
│   │   ├── transport/    # 运输管理
│   │   └── statistics/  # 统计模块
│   ├── sql/init.sql      # 数据库初始化
│   └── pom.xml
│
├── frontend/
│   ├── src/
│   │   ├── api/         # Axios 接口封装
│   │   ├── components/  # 公共组件
│   │   ├── pages/       # 页面
│   │   ├── stores/      # Zustand 状态
│   │   └── router/      # React Router
│   └── package.json
│
├── docker/
│   └── docker-compose.yml
│
├── docs/                # 项目文档（14份）
└── README.md
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

完整文档见 `docs/` 目录。
