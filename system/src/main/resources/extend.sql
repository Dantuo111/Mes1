USE mes_db;

-- 物料申领表
DROP TABLE IF EXISTS `material_request`;
CREATE TABLE `material_request` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '申领ID',
    `user_id` BIGINT NOT NULL COMMENT '申领人ID',
    `material_id` BIGINT NOT NULL COMMENT '物料ID',
    `quantity` INT NOT NULL COMMENT '申领数量',
    `reason` VARCHAR(500) COMMENT '申领原因',
    `status` INT DEFAULT 0 COMMENT '状态：0待审批 1已批准 2已拒绝',
    `reply` VARCHAR(500) COMMENT '审批回复',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`),
    FOREIGN KEY (`material_id`) REFERENCES `material`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物料申领表';

-- 质量反馈表
DROP TABLE IF EXISTS `quality_feedback`;
CREATE TABLE `quality_feedback` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '反馈ID',
    `user_id` BIGINT NOT NULL COMMENT '反馈人ID',
    `work_order_id` BIGINT COMMENT '关联工单ID',
    `title` VARCHAR(200) NOT NULL COMMENT '问题标题',
    `description` TEXT COMMENT '问题描述',
    `severity` INT DEFAULT 1 COMMENT '严重程度：0低 1中 2高',
    `status` INT DEFAULT 0 COMMENT '状态：0待处理 1处理中 2已解决 3已关闭',
    `reply` TEXT COMMENT '处理回复',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`),
    FOREIGN KEY (`work_order_id`) REFERENCES `work_order`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='质量反馈表';

-- 测试数据
INSERT INTO `material_request` (`user_id`, `material_id`, `quantity`, `reason`, `status`, `reply`) VALUES
(2, 3, 20, '轴承加工需要补充GCr15钢材', 1, '已批准，请到仓库领取'),
(3, 6, 10, '铜合金棒库存不足，急需补充', 0, NULL),
(4, 8, 3, '喷涂工序需要防锈漆', 2, '当前库存充足，暂不批准'),
(2, 5, 100, '螺栓消耗较快，申请补充', 0, NULL);

INSERT INTO `quality_feedback` (`user_id`, `work_order_id`, `title`, `description`, `severity`, `status`, `reply`) VALUES
(2, 3, '齿轮组件表面粗糙度异常', '本批次齿轮组件在精加工后表面粗糙度Ra值偏高，超出标准0.2μm', 2, 1, '已安排技术人员排查刀具磨损情况'),
(3, 5, '电机外壳铸造气孔问题', '发现约5%的外壳存在微小气孔，可能影响密封性', 1, 0, NULL),
(4, 3, '渗碳层深度不均匀', '部分齿轮渗碳层深度偏差超过0.1mm', 2, 2, '已调整渗碳炉温度参数，问题已解决'),
(2, 1, '轴承内圈尺寸偏差', '少量轴承内圈直径偏差超出公差范围', 0, 3, '已筛选不合格品，偏差在可接受范围内');
