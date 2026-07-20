-- 客户表
CREATE TABLE IF NOT EXISTS `customer` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `customer_code` VARCHAR(50) NOT NULL COMMENT '客户编号',
  `company_name` VARCHAR(200) NOT NULL COMMENT '公司名称',
  `industry_type` VARCHAR(50) DEFAULT NULL COMMENT '行业类型',
  `contact_person` VARCHAR(50) NOT NULL COMMENT '联系人',
  `contact_phone` VARCHAR(20) NOT NULL COMMENT '联系电话',
  `contact_email` VARCHAR(100) DEFAULT NULL COMMENT '联系邮箱',
  `address` VARCHAR(500) DEFAULT NULL COMMENT '公司地址',
  `website` VARCHAR(200) DEFAULT NULL COMMENT '公司网站',
  `status` TINYINT(4) NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
  `remark` TEXT COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_customer_code` (`customer_code`),
  KEY `idx_company_name` (`company_name`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户表';

-- 示例数据
INSERT INTO customer (customer_code, company_name, industry_type, contact_person, contact_phone, contact_email, address, status) 
VALUES 
('CUS20260720001', '阿里巴巴集团', 'IT互联网', '张三', '13800138000', 'zhangsan@alibaba.com', '浙江省杭州市余杭区', 1),
('CUS20260720002', '腾讯科技', 'IT互联网', '李四', '13900139000', 'lisi@tencent.com', '广东省深圳市南山区', 1);
