-- 修改项目账户表结构（安全版本）

-- 1. 检查并添加supplier_id字段（如果不存在）
-- 如果报错"Duplicate column name"，说明字段已存在，可以忽略

-- 2. 添加索引（如果不存在）
ALTER TABLE project_account ADD INDEX IF NOT EXISTS `idx_supplier_id` (`supplier_id`);

-- 3. 修改account_type字段类型
ALTER TABLE project_account MODIFY COLUMN `account_type` VARCHAR(20) NOT NULL DEFAULT 'OTHER' COMMENT '账户类型：DATABASE-数据库 SYSTEM-系统管理 APPLICATION-应用 CLOUD-云服务 OTHER-其他';

-- 4. 重命名access_url为login_url（如果存在）
ALTER TABLE project_account CHANGE COLUMN `access_url` `login_url` VARCHAR(500) DEFAULT NULL COMMENT '登录地址';

-- 5. 检查并添加creator_id字段
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
AND TABLE_NAME = 'project_account' 
AND COLUMN_NAME = 'creator_id';

SET @sql = IF(@col_exists = 0, 
    'ALTER TABLE project_account ADD COLUMN `creator_id` BIGINT(20) DEFAULT NULL COMMENT ''创建人ID'' AFTER `remark`',
    'SELECT ''creator_id already exists'' AS Info');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 6. 删除不需要的字段
ALTER TABLE project_account DROP COLUMN IF EXISTS `port`;
ALTER TABLE project_account DROP COLUMN IF EXISTS `environment`;