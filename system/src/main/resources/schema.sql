-- MES系统数据库脚本
CREATE DATABASE IF NOT EXISTS mes_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE mes_db;

-- 用户表
DROP TABLE IF EXISTS `quality_inspection`;
DROP TABLE IF EXISTS `material_record`;
DROP TABLE IF EXISTS `work_order`;
DROP TABLE IF EXISTS `production_plan`;
DROP TABLE IF EXISTS `material`;
DROP TABLE IF EXISTS `user`;

CREATE TABLE `user` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    `password` VARCHAR(100) NOT NULL COMMENT '密码',
    `nickname` VARCHAR(50) COMMENT '昵称',
    `phone` VARCHAR(20) COMMENT '手机号',
    `avatar` VARCHAR(255) COMMENT '头像',
    `role` INT DEFAULT 0 COMMENT '角色：0普通员工 1管理员',
    `status` INT DEFAULT 1 COMMENT '状态：0禁用 1启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 生产计划表
CREATE TABLE `production_plan` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '计划ID',
    `product_name` VARCHAR(100) NOT NULL COMMENT '产品名称',
    `planned_quantity` INT NOT NULL COMMENT '计划数量',
    `start_time` DATETIME NOT NULL COMMENT '计划开始时间',
    `end_time` DATETIME NOT NULL COMMENT '计划结束时间',
    `priority` INT DEFAULT 1 COMMENT '优先级：0低 1中 2高',
    `status` INT DEFAULT 0 COMMENT '状态：0待执行 1执行中 2已完成',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='生产计划表';

-- 生产工单表
CREATE TABLE `work_order` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '工单ID',
    `plan_id` BIGINT NOT NULL COMMENT '关联生产计划ID',
    `product_name` VARCHAR(100) NOT NULL COMMENT '产品名称',
    `quantity` INT NOT NULL COMMENT '生产数量',
    `assignee_id` BIGINT COMMENT '负责人ID',
    `process_requirement` TEXT COMMENT '工序要求',
    `delivery_date` DATE COMMENT '交付日期',
    `status` INT DEFAULT 0 COMMENT '状态：0待派发 1待接收 2待开工 3执行中 4已完工 5已关闭',
    `actual_quantity` INT COMMENT '实际完成数量',
    `complete_time` DATETIME COMMENT '完工时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (`plan_id`) REFERENCES `production_plan`(`id`),
    FOREIGN KEY (`assignee_id`) REFERENCES `user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='生产工单表';

-- 物料表
CREATE TABLE `material` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '物料ID',
    `code` VARCHAR(50) NOT NULL UNIQUE COMMENT '物料编码',
    `name` VARCHAR(100) NOT NULL COMMENT '物料名称',
    `spec` VARCHAR(100) COMMENT '规格',
    `unit` VARCHAR(20) COMMENT '单位',
    `stock_quantity` INT DEFAULT 0 COMMENT '库存数量',
    `safety_stock` INT DEFAULT 0 COMMENT '安全库存',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物料表';

-- 物料出入库记录表
CREATE TABLE `material_record` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    `material_id` BIGINT NOT NULL COMMENT '物料ID',
    `type` INT NOT NULL COMMENT '类型：0入库 1出库',
    `quantity` INT NOT NULL COMMENT '操作数量',
    `before_quantity` INT NOT NULL COMMENT '操作前库存',
    `after_quantity` INT NOT NULL COMMENT '操作后库存',
    `operate_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (`material_id`) REFERENCES `material`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物料出入库记录表';

-- 质量检验记录表
CREATE TABLE `quality_inspection` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '检验ID',
    `work_order_id` BIGINT NOT NULL COMMENT '关联工单ID',
    `inspection_date` DATE NOT NULL COMMENT '检验日期',
    `inspector` VARCHAR(50) NOT NULL COMMENT '检验人员',
    `inspection_quantity` INT NOT NULL COMMENT '检验数量',
    `qualified_quantity` INT NOT NULL COMMENT '合格数量',
    `unqualified_quantity` INT NOT NULL COMMENT '不合格数量',
    `unqualified_reason` TEXT COMMENT '不合格原因',
    `pass_rate` DECIMAL(5,2) COMMENT '合格率',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (`work_order_id`) REFERENCES `work_order`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='质量检验记录表';

-- 初始数据：管理员账号
INSERT INTO `user` (`username`, `password`, `nickname`, `role`, `status`) VALUES
('admin', '123456', '系统管理员', 1, 1);

-- 初始数据：测试员工
INSERT INTO `user` (`username`, `password`, `nickname`, `phone`, `role`, `status`) VALUES
('zhangsan', '123456', '张三', '13800138001', 0, 1),
('lisi', '123456', '李四', '13800138002', 0, 1),
('wangwu', '123456', '王五', '13800138003', 0, 1);
