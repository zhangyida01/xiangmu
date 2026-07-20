-- 安全的迁移脚本 - 逐条执行

-- 1. 修改account_type字段类型（重要！）
ALTER TABLE project_account MODIFY COLUMN `account_type` VARCHAR(20) NOT NULL DEFAULT 'OTHER' COMMENT '账户类型';

-- 2. 检查login_url字段是否存在，不存在则重命名access_url
-- 如果报错说access_url不存在，说明已经是login_url了，可以忽略
ALTER TABLE project_account CHANGE COLUMN `access_url` `login_url` VARCHAR(500) DEFAULT NULL COMMENT '登录地址';

-- 3. 添加creator_id字段（如果不存在）
-- 如果报错说字段已存在，可以忽略
ALTER TABLE project_account ADD COLUMN `creator_id` BIGINT(20) DEFAULT NULL COMMENT '创建人ID' AFTER `remark`;

-- 4. 删除不需要的字段（如果存在）
ALTER TABLE project_account DROP COLUMN IF EXISTS `port`;
ALTER TABLE project_account DROP COLUMN IF EXISTS `environment`;

-- 5. 添加索引（如果不存在）
-- MySQL 5.7不支持IF NOT EXISTS，如果报错说索引已存在，可以忽略
ALTER TABLE project_account ADD INDEX `idx_supplier_id` (`supplier_id`);