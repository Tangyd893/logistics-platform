-- ============================================
-- 综合物流管理系统 - 数据库初始化脚本
-- 数据库: PostgreSQL 16
-- ============================================

-- 创建数据库（需超级用户执行）
-- CREATE DATABASE logistics;
-- CREATE USER logistics_user WITH PASSWORD 'logistics_pass';
-- GRANT ALL PRIVILEGES ON DATABASE logistics TO logistics_user;

-- 连接 logistics 数据库后执行以下内容

-- -------------------------------------------
-- 1. 系统管理模块（sys_）
-- -------------------------------------------

-- 部门表
CREATE TABLE IF NOT EXISTS sys_dept (
    id BIGSERIAL PRIMARY KEY,
    parent_id BIGINT REFERENCES sys_dept(id),
    name VARCHAR(50) NOT NULL,
    sort_order INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64),
    updated_by VARCHAR(64),
    deleted BOOLEAN DEFAULT FALSE
);

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(100),
    avatar VARCHAR(255),
    dept_id BIGINT REFERENCES sys_dept(id),
    warehouse_id BIGINT,
    role_code VARCHAR(50) NOT NULL DEFAULT 'USER',
    status SMALLINT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64),
    updated_by VARCHAR(64),
    deleted BOOLEAN DEFAULT FALSE
);

-- 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    status SMALLINT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64),
    updated_by VARCHAR(64),
    deleted BOOLEAN DEFAULT FALSE
);

-- 菜单表
CREATE TABLE IF NOT EXISTS sys_menu (
    id BIGSERIAL PRIMARY KEY,
    parent_id BIGINT REFERENCES sys_menu(id),
    name VARCHAR(50) NOT NULL,
    path VARCHAR(200),
    component VARCHAR(200),
    icon VARCHAR(50),
    sort_order INT DEFAULT 0,
    type SMALLINT DEFAULT 1 COMMENT '1:菜单 2:按钮',
    perms VARCHAR(100),
    status SMALLINT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64),
    updated_by VARCHAR(64),
    deleted BOOLEAN DEFAULT FALSE
);

-- -------------------------------------------
-- 2. 仓库管理模块（wh_）
-- -------------------------------------------

-- 仓库表
CREATE TABLE IF NOT EXISTS wh_warehouse (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    address VARCHAR(255),
    area_sqm DECIMAL(10, 2),
    status SMALLINT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64),
    updated_by VARCHAR(64),
    deleted BOOLEAN DEFAULT FALSE
);

-- 库位表
CREATE TABLE IF NOT EXISTS wh_location (
    id BIGSERIAL PRIMARY KEY,
    warehouse_id BIGINT NOT NULL REFERENCES wh_warehouse(id),
    zone VARCHAR(20),
    code VARCHAR(50) NOT NULL UNIQUE,
    type SMALLINT DEFAULT 1 COMMENT '1:地面 2:货架 3:冷冻',
    status SMALLINT DEFAULT 1 COMMENT '1:空闲 2:占用 3:冻结',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64),
    updated_by VARCHAR(64),
    deleted BOOLEAN DEFAULT FALSE
);

-- 库存表
CREATE TABLE IF NOT EXISTS wh_inventory (
    id BIGSERIAL PRIMARY KEY,
    warehouse_id BIGINT NOT NULL REFERENCES wh_warehouse(id),
    location_id BIGINT REFERENCES wh_location(id),
    sku_id BIGINT NOT NULL,
    sku_name VARCHAR(200) NOT NULL,
    quantity INT NOT NULL DEFAULT 0,
    unit VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64),
    updated_by VARCHAR(64),
    deleted BOOLEAN DEFAULT FALSE,
    UNIQUE(warehouse_id, location_id, sku_id)
);

-- 入库单
CREATE TABLE IF NOT EXISTS wh_inbound (
    id BIGSERIAL PRIMARY KEY,
    inbound_no VARCHAR(50) NOT NULL UNIQUE,
    warehouse_id BIGINT NOT NULL REFERENCES wh_warehouse(id),
    source_type SMALLINT DEFAULT 1 COMMENT '1:采购 2:退货 3:调拨',
    supplier VARCHAR(200),
    total_quantity INT DEFAULT 0,
    status SMALLINT DEFAULT 1 COMMENT '1:待审核 2:已入库',
    remark VARCHAR(500),
    operator_id BIGINT REFERENCES sys_user(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64),
    updated_by VARCHAR(64),
    deleted BOOLEAN DEFAULT FALSE
);

-- 入库明细
CREATE TABLE IF NOT EXISTS wh_inbound_detail (
    id BIGSERIAL PRIMARY KEY,
    inbound_id BIGINT NOT NULL REFERENCES wh_inbound(id),
    sku_id BIGINT NOT NULL,
    sku_name VARCHAR(200) NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(12, 2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 出库单
CREATE TABLE IF NOT EXISTS wh_outbound (
    id BIGSERIAL PRIMARY KEY,
    outbound_no VARCHAR(50) NOT NULL UNIQUE,
    warehouse_id BIGINT NOT NULL REFERENCES wh_warehouse(id),
    order_id BIGINT,
    total_quantity INT DEFAULT 0,
    status SMALLINT DEFAULT 1 COMMENT '1:待出库 2:已出库',
    operator_id BIGINT REFERENCES sys_user(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64),
    updated_by VARCHAR(64),
    deleted BOOLEAN DEFAULT FALSE
);

-- 出库明细
CREATE TABLE IF NOT EXISTS wh_outbound_detail (
    id BIGSERIAL PRIMARY KEY,
    outbound_id BIGINT NOT NULL REFERENCES wh_outbound(id),
    sku_id BIGINT NOT NULL,
    sku_name VARCHAR(200) NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(12, 2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- -------------------------------------------
-- 3. 订单管理模块（o_）
-- -------------------------------------------

-- 订单表
CREATE TABLE IF NOT EXISTS o_order (
    id BIGSERIAL PRIMARY KEY,
    order_no VARCHAR(50) NOT NULL UNIQUE,
    customer_id BIGINT,
    sender_name VARCHAR(100) NOT NULL,
    sender_phone VARCHAR(20) NOT NULL,
    sender_address VARCHAR(255) NOT NULL,
    receiver_name VARCHAR(100) NOT NULL,
    receiver_phone VARCHAR(20) NOT NULL,
    receiver_address VARCHAR(255) NOT NULL,
    total_amount DECIMAL(12, 2) DEFAULT 0,
    weight_kg DECIMAL(10, 2),
    volume_cbm DECIMAL(10, 4),
    status SMALLINT DEFAULT 10,
    remark VARCHAR(500),
    created_by BIGINT REFERENCES sys_user(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64),
    updated_by VARCHAR(64),
    deleted BOOLEAN DEFAULT FALSE
);

-- 订单明细
CREATE TABLE IF NOT EXISTS o_order_item (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES o_order(id),
    sku_name VARCHAR(200),
    sku_code VARCHAR(50),
    quantity INT NOT NULL,
    weight_kg DECIMAL(10, 2),
    volume_cbm DECIMAL(10, 4),
    unit_price DECIMAL(10, 2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 订单状态日志
CREATE TABLE IF NOT EXISTS o_order_status_log (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES o_order(id),
    status SMALLINT NOT NULL,
    operate_by BIGINT REFERENCES sys_user(id),
    operate_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    remark VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- -------------------------------------------
-- 4. 运输管理模块（t_）
-- -------------------------------------------

-- 司机表
CREATE TABLE IF NOT EXISTS t_driver (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    license_no VARCHAR(50),
    id_card VARCHAR(20),
    status SMALLINT DEFAULT 1 COMMENT '1:空闲 2:配送中',
    warehouse_id BIGINT REFERENCES wh_warehouse(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64),
    updated_by VARCHAR(64),
    deleted BOOLEAN DEFAULT FALSE
);

-- 车辆表
CREATE TABLE IF NOT EXISTS t_vehicle (
    id BIGSERIAL PRIMARY KEY,
    plate_no VARCHAR(20) NOT NULL UNIQUE,
    type VARCHAR(50),
    capacity_kg DECIMAL(10, 2),
    capacity_cbm DECIMAL(10, 4),
    status SMALLINT DEFAULT 1 COMMENT '1:空闲 2:配送中 3:维修',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64),
    updated_by VARCHAR(64),
    deleted BOOLEAN DEFAULT FALSE
);

-- 运单表
CREATE TABLE IF NOT EXISTS t_waybill (
    id BIGSERIAL PRIMARY KEY,
    waybill_no VARCHAR(50) NOT NULL UNIQUE,
    order_id BIGINT REFERENCES o_order(id),
    warehouse_id BIGINT REFERENCES wh_warehouse(id),
    driver_id BIGINT REFERENCES t_driver(id),
    vehicle_id BIGINT REFERENCES t_vehicle(id),
    plan_pickup_time TIMESTAMP,
    plan_delivery_time TIMESTAMP,
    actual_pickup_time TIMESTAMP,
    actual_delivery_time TIMESTAMP,
    status SMALLINT DEFAULT 1 COMMENT '1:待提货 2:配送中 3:已送达 4:拒收',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64),
    updated_by VARCHAR(64),
    deleted BOOLEAN DEFAULT FALSE
);

-- 配送轨迹表
CREATE TABLE IF NOT EXISTS t_tracking (
    id BIGSERIAL PRIMARY KEY,
    waybill_id BIGINT NOT NULL REFERENCES t_waybill(id),
    status SMALLINT NOT NULL,
    location VARCHAR(255),
    latitude DECIMAL(10, 6),
    longitude DECIMAL(10, 6),
    description VARCHAR(255),
    operate_by BIGINT REFERENCES sys_user(id),
    operate_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- -------------------------------------------
-- 5. 索引
-- -------------------------------------------

CREATE INDEX IF NOT EXISTS idx_user_username ON sys_user(username);
CREATE INDEX IF NOT EXISTS idx_user_dept ON sys_user(dept_id);
CREATE INDEX IF NOT EXISTS idx_menu_parent ON sys_menu(parent_id);
CREATE INDEX IF NOT EXISTS idx_dept_parent ON sys_dept(parent_id);
CREATE INDEX IF NOT EXISTS idx_location_warehouse ON wh_location(warehouse_id);
CREATE INDEX IF NOT EXISTS idx_inventory_sku ON wh_inventory(sku_id);
CREATE INDEX IF NOT EXISTS idx_inventory_location ON wh_inventory(location_id);
CREATE INDEX IF NOT EXISTS idx_order_customer ON o_order(customer_id);
CREATE INDEX IF NOT EXISTS idx_order_status ON o_order(status);
CREATE INDEX IF NOT EXISTS idx_order_no ON o_order(order_no);
CREATE INDEX IF NOT EXISTS idx_waybill_order ON t_waybill(order_id);
CREATE INDEX IF NOT EXISTS idx_waybill_driver ON t_waybill(driver_id);
CREATE INDEX IF NOT EXISTS idx_tracking_waybill ON t_tracking(waybill_id);

-- -------------------------------------------
-- 6. 初始数据
-- -------------------------------------------

-- 初始化部门
INSERT INTO sys_dept (name, sort_order) VALUES
    ('总经理室', 1),
    ('仓储部', 2),
    ('运输部', 3),
    ('客服部', 4),
    ('财务部', 5);

-- 初始化角色
INSERT INTO sys_role (name, code, description) VALUES
    ('系统管理员', 'ADMIN', '系统全部权限'),
    ('仓库管理员', 'WAREHOUSE_ADMIN', '仓库管理权限'),
    ('仓库操作员', 'WAREHOUSE_OPERATOR', '仓库操作权限'),
    ('调度员', 'DISPATCHER', '运输调度权限'),
    ('司机', 'DRIVER', '配送权限'),
    ('客户', 'CUSTOMER', '客户权限');

-- 初始化管理员用户 (密码: admin123，BCrypt加密)
-- 密码 hash: $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi
INSERT INTO sys_user (username, password, display_name, phone, email, dept_id, role_code, status) VALUES
    ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', '13800138000', 'admin@logistics.com', 1, 'ADMIN', 1),
    ('warehouse_mgr', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '仓库经理', '13800138001', 'warehouse@logistics.com', 2, 'WAREHOUSE_ADMIN', 1),
    ('warehouse_op', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '仓库操作员', '13800138002', 'warehouse_op@logistics.com', 2, 'WAREHOUSE_OPERATOR', 1),
    ('dispatcher', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '调度员', '13800138003', 'dispatcher@logistics.com', 3, 'DISPATCHER', 1);

-- 初始化仓库
INSERT INTO wh_warehouse (name, address, area_sqm, status) VALUES
    ('北京仓库', '北京市大兴区物流园A座', 5000.00, 1),
    ('上海仓库', '上海市浦东新区物流园B座', 4500.00, 1);

-- 初始化库位
INSERT INTO wh_location (warehouse_id, zone, code, type, status) VALUES
    (1, 'A', 'A-01-001', 2, 1),
    (1, 'A', 'A-01-002', 2, 1),
    (1, 'B', 'B-01-001', 1, 1),
    (2, 'A', 'A-01-001', 2, 1),
    (2, 'B', 'B-01-001', 1, 1);

-- 初始化司机
INSERT INTO t_driver (name, phone, license_no, warehouse_id, status) VALUES
    ('张师傅', '13900139001', '110101198001011234', 1, 1),
    ('李师傅', '13900139002', '110101198002021235', 1, 1),
    ('王师傅', '13900139003', '310101198003031236', 2, 1);

-- 初始化车辆
INSERT INTO t_vehicle (plate_no, type, capacity_kg, capacity_cbm, status) VALUES
    ('京A12345', '厢式货车', 5000.00, 20.00, 1),
    ('京B67890', '厢式货车', 3000.00, 15.00, 1),
    ('沪A11111', '厢式货车', 5000.00, 20.00, 1);
