-- 供应商表
CREATE TABLE IF NOT EXISTS `supplier` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `supplier_code` VARCHAR(50) NOT NULL COMMENT '供应商编号',
  `supplier_name` VARCHAR(200) NOT NULL COMMENT '供应商名称',
  `supplier_type` TINYINT(4) NOT NULL DEFAULT 1 COMMENT '供应商类型：1-软件供应商 2-硬件供应商 3-集成供应商 4-其他',
  `business_scope` VARCHAR(500) DEFAULT NULL COMMENT '业务范围',
  `address` VARCHAR(500) DEFAULT NULL COMMENT '公司地址',
  `website` VARCHAR(200) DEFAULT NULL COMMENT '公司网站',
  `status` TINYINT(4) NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
  `remark` TEXT COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_supplier_code` (`supplier_code`),
  KEY `idx_supplier_name` (`supplier_name`),
  KEY `idx_supplier_type` (`supplier_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商表';

-- 供应商联系人表
CREATE TABLE IF NOT EXISTS `supplier_contact` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `supplier_id` BIGINT(20) NOT NULL COMMENT '供应商ID',
  `contact_name` VARCHAR(50) NOT NULL COMMENT '联系人姓名',
  `position` VARCHAR(50) DEFAULT NULL COMMENT '职位',
  `phone` VARCHAR(20) NOT NULL COMMENT '联系电话',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `wechat` VARCHAR(50) DEFAULT NULL COMMENT '微信号',
  `qq` VARCHAR(20) DEFAULT NULL COMMENT 'QQ号',
  `is_primary` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否主联系人：0-否 1-是',
  `remark` TEXT COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_supplier_id` (`supplier_id`),
  KEY `idx_contact_name` (`contact_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商联系人表';

-- 修改项目账户表，添加供应商ID
ALTER TABLE `project_account` ADD COLUMN `supplier_id` BIGINT(20) DEFAULT NULL COMMENT '供应商ID' AFTER `project_id`;
ALTER TABLE `project_account` ADD KEY `idx_supplier_id` (`supplier_id`);

-- 示例数据
INSERT INTO supplier (supplier_code, supplier_name, supplier_type, business_scope, address, status) 
VALUES 
('SUP20260720001', '阿里云计算有限公司', 1, '云计算服务、CDN、数据库', '浙江省杭州市余杭区', 1),
('SUP20260720002', '华为技术有限公司', 2, '服务器、交换机、路由器', '广东省深圳市龙岗区', 1),
('SUP20260720003', '用友网络科技股份有限公司', 1, 'ERP系统、财务软件', '北京市海淀区', 1);

INSERT INTO supplier_contact (supplier_id, contact_name, position, phone, email, wechat, is_primary) 
VALUES 
(1, '张经理', '客户经理', '13800138001', 'zhang@aliyun.com', 'zhang_aliyun', 1),
(1, '李工程师', '技术支持', '13800138002', 'li@aliyun.com', 'li_aliyun', 0),
(2, '王总监', '销售总监', '13900139001', 'wang@huawei.com', 'wang_hw', 1),
(3, '赵经理', '售前顾问', '13700137001', 'zhao@yonyou.com', 'zhao_yy', 1);
