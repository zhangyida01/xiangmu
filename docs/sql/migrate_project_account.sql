-- 修改项目账户表结构
-- 添加supplier_id字段
ALTER TABLE project_account ADD COLUMN `supplier_id` BIGINT(20) DEFAULT NULL COMMENT '供应商ID' AFTER `project_id`;
ALTER TABLE project_account ADD INDEX `idx_supplier_id` (`supplier_id`);

-- 修改account_type字段类型
ALTER TABLE project_account MODIFY COLUMN `account_type` VARCHAR(20) NOT NULL DEFAULT 'OTHER' COMMENT '账户类型：DATABASE-数据库 SYSTEM-系统管理 APPLICATION-应用 CLOUD-云服务 OTHER-其他';

-- 添加login_url字段（如果access_url存在则重命名）
ALTER TABLE project_account CHANGE COLUMN `access_url` `login_url` VARCHAR(500) DEFAULT NULL COMMENT '登录地址';

-- 添加creator_id字段
ALTER TABLE project_account ADD COLUMN `creator_id` BIGINT(20) DEFAULT NULL COMMENT '创建人ID' AFTER `remark`;

-- 删除不需要的字段（如果存在）
ALTER TABLE project_account DROP COLUMN IF EXISTS `port`;
ALTER TABLE project_account DROP COLUMN IF EXISTS `environment`;