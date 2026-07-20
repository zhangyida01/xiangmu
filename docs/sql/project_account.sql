-- 项目账户表
CREATE TABLE IF NOT EXISTS `project_account` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `project_id` BIGINT(20) NOT NULL COMMENT '项目ID',
  `supplier_id` BIGINT(20) DEFAULT NULL COMMENT '供应商ID',
  `account_name` VARCHAR(100) NOT NULL COMMENT '账户名称',
  `account_type` VARCHAR(20) NOT NULL DEFAULT 'OTHER' COMMENT '账户类型：DATABASE-数据库 SYSTEM-系统管理 APPLICATION-应用 CLOUD-云服务 OTHER-其他',
  `username` VARCHAR(100) NOT NULL COMMENT '用户名/账号',
  `password` VARCHAR(500) NOT NULL COMMENT '密码（明文存储）',
  `login_url` VARCHAR(500) DEFAULT NULL COMMENT '登录地址',
  `remark` TEXT COMMENT '备注说明',
  `creator_id` BIGINT(20) DEFAULT NULL COMMENT '创建人ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_project_id` (`project_id`),
  KEY `idx_supplier_id` (`supplier_id`),
  KEY `idx_account_type` (`account_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目账户表';