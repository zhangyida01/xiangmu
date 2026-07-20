-- 项目账户表
CREATE TABLE IF NOT EXISTS `project_account` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `project_id` BIGINT(20) NOT NULL COMMENT '项目ID',
  `account_name` VARCHAR(100) NOT NULL COMMENT '账户名称',
  `account_type` TINYINT(4) NOT NULL DEFAULT 1 COMMENT '账户类型：1-系统账户 2-数据库账户 3-云平台账户 4-FTP账户 5-邮箱账户 6-其他',
  `username` VARCHAR(100) NOT NULL COMMENT '用户名/账号',
  `password` VARCHAR(500) NOT NULL COMMENT '密码（加密存储）',
  `access_url` VARCHAR(500) DEFAULT NULL COMMENT '访问地址/URL',
  `port` INT(11) DEFAULT NULL COMMENT '端口',
  `environment` TINYINT(4) DEFAULT 1 COMMENT '环境类型：1-开发 2-测试 3-预发布 4-生产',
  `remark` TEXT COMMENT '备注说明',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_project_id` (`project_id`),
  KEY `idx_account_type` (`account_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目账户表';

-- 示例数据
INSERT INTO project_account (project_id, account_name, account_type, username, password, access_url, port, environment, remark) 
VALUES (1, '生产数据库', 2, 'db_user', 'encrypted_password', '192.168.1.100', 3306, 4, 'MySQL生产数据库');
