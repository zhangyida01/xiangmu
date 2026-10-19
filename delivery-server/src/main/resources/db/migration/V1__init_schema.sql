-- 项目交付管理后台 初始化表结构
-- 由 docs/02-数据库设计.md 生成，Flyway 自动执行

-- 用户表
CREATE TABLE `sys_user` (
  `id`               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username`         VARCHAR(50)  NOT NULL COMMENT '登录账号',
  `password`         VARCHAR(100) NOT NULL COMMENT 'BCrypt 哈希',
  `real_name`        VARCHAR(50)  NOT NULL DEFAULT '' COMMENT '姓名',
  `role`             VARCHAR(20)  NOT NULL DEFAULT 'USER' COMMENT 'ADMIN/USER/VIEWER',
  `can_view_secret`  TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '可查看凭据明文',
  `can_export`       TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '可导出凭据/文档',
  `can_download`     TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '可下载附件',
  `can_delete_project` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '可删除项目',
  `need_recheck`     TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '查看明文前是否需再次输入密码',
  `status`           TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '1启用 0禁用',
  `must_change_pwd`  TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '下次登录强制改密',
  `pwd_update_time`  DATETIME     NULL COMMENT '密码最后修改时间',
  `login_fail_count` INT          NOT NULL DEFAULT 0 COMMENT '连续登录失败次数',
  `lock_until`       DATETIME     NULL COMMENT '锁定截止时间',
  `last_login_time`  DATETIME     NULL COMMENT '最后登录时间',
  `last_login_ip`    VARCHAR(64)  NULL COMMENT '最后登录IP',
  `remark`           VARCHAR(255) NULL COMMENT '备注',
  `create_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`          TINYINT(1)   NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户';

-- 字典项（项目类型、项目状态、凭据类型、文档分类扩展等）
CREATE TABLE `sys_dict_item` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `dict_type`   VARCHAR(50)  NOT NULL COMMENT '字典类型，如 PROJECT_TYPE',
  `item_code`   VARCHAR(50)  NOT NULL COMMENT '编码',
  `item_name`   VARCHAR(100) NOT NULL COMMENT '显示名',
  `sort_no`     INT          NOT NULL DEFAULT 0,
  `enabled`     TINYINT(1)   NOT NULL DEFAULT 1,
  `remark`      VARCHAR(255) NULL,
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`     TINYINT(1)   NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_type_code` (`dict_type`, `item_code`),
  KEY `idx_type_sort` (`dict_type`, `sort_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='字典项';

-- 系统参数
CREATE TABLE `sys_config` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `cfg_key`     VARCHAR(100) NOT NULL COMMENT '参数键',
  `cfg_value`   TEXT         NULL COMMENT '参数值',
  `cfg_group`   VARCHAR(50)  NOT NULL DEFAULT 'COMMON' COMMENT '分组',
  `value_type`  VARCHAR(20)  NOT NULL DEFAULT 'STRING' COMMENT 'STRING/NUMBER/BOOLEAN/JSON',
  `encrypted`   TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '值是否加密存储',
  `remark`      VARCHAR(255) NULL,
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_cfg_key` (`cfg_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='系统参数';

-- 审计日志（只增不改不删，无 deleted / update_time）
CREATE TABLE `sys_audit_log` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id`     BIGINT UNSIGNED NULL COMMENT '操作人ID，未登录为空',
  `username`    VARCHAR(50)  NULL COMMENT '操作人账号快照',
  `module`      VARCHAR(50)  NOT NULL COMMENT '模块：AUTH/PROJECT/DOC/CREDENTIAL/SYSTEM',
  `action`      VARCHAR(50)  NOT NULL COMMENT '动作：LOGIN/CREATE/UPDATE/DELETE/VIEW_SECRET/COPY/DOWNLOAD/EXPORT',
  `target_type` VARCHAR(50)  NULL COMMENT '对象类型',
  `target_id`   BIGINT UNSIGNED NULL COMMENT '对象ID',
  `target_name` VARCHAR(255) NULL COMMENT '对象名称快照',
  `project_id`  BIGINT UNSIGNED NULL COMMENT '所属项目，便于按项目查流水',
  `detail`      TEXT         NULL COMMENT '参数摘要（敏感值已脱敏）',
  `ip`          VARCHAR(64)  NULL,
  `user_agent`  VARCHAR(500) NULL,
  `success`     TINYINT(1)   NOT NULL DEFAULT 1,
  `err_msg`     VARCHAR(500) NULL,
  `cost_ms`     INT          NULL COMMENT '耗时毫秒',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_time` (`create_time`),
  KEY `idx_user_time` (`user_id`, `create_time`),
  KEY `idx_project` (`project_id`),
  KEY `idx_module_action` (`module`, `action`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='审计日志';

-- 客户（甲方）
CREATE TABLE `biz_customer` (
  `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `name`          VARCHAR(200) NOT NULL COMMENT '客户名称',
  `short_name`    VARCHAR(100) NULL COMMENT '简称',
  `industry`      VARCHAR(50)  NULL COMMENT '行业',
  `contact_name`  VARCHAR(50)  NULL COMMENT '主联系人',
  `contact_phone` VARCHAR(50)  NULL,
  `address`       VARCHAR(255) NULL,
  `remark`        VARCHAR(500) NULL,
  `create_time`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`       TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='客户';

-- 项目主表
CREATE TABLE `biz_project` (
  `id`                 BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `project_no`         VARCHAR(50)  NOT NULL COMMENT '项目编号，唯一',
  `name`               VARCHAR(200) NOT NULL COMMENT '项目名称',
  `customer_id`        BIGINT UNSIGNED NULL COMMENT '客户ID',
  `project_type`       VARCHAR(50)  NOT NULL DEFAULT 'STANDARD' COMMENT '字典 PROJECT_TYPE',
  `status`             VARCHAR(50)  NOT NULL DEFAULT 'PRESALE' COMMENT '字典 PROJECT_STATUS',
  `health`             VARCHAR(20)  NOT NULL DEFAULT 'GREEN' COMMENT 'GREEN/YELLOW/RED',
  `manager`            VARCHAR(50)  NULL COMMENT '项目经理',
  `sales`              VARCHAR(50)  NULL COMMENT '销售负责人',
  `delivery_owner`     VARCHAR(50)  NULL COMMENT '交付负责人',
  `contract_no`        VARCHAR(100) NULL COMMENT '主合同号（冗余便于搜索）',
  `amount`             DECIMAL(14,2) NULL COMMENT '合同金额',
  `tags`               VARCHAR(255) NULL COMMENT '标签，逗号分隔',
  `summary`            TEXT         NULL COMMENT '项目简介',
  `setup_date`         DATE NULL COMMENT '立项日期',
  `start_date`         DATE NULL COMMENT '启动日期',
  `plan_online_date`   DATE NULL COMMENT '计划上线',
  `actual_online_date` DATE NULL COMMENT '实际上线',
  `plan_accept_date`   DATE NULL COMMENT '计划验收',
  `actual_accept_date` DATE NULL COMMENT '实际验收',
  `archive_date`       DATE NULL COMMENT '归档日期',
  `warranty_end_date`  DATE NULL COMMENT '质保到期',
  `progress`           TINYINT NOT NULL DEFAULT 0 COMMENT '整体进度0-100',
  `doc_rate`           TINYINT NOT NULL DEFAULT 0 COMMENT '文档完成率0-100（冗余）',
  `archived`           TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否归档',
  `locked`             TINYINT(1) NOT NULL DEFAULT 0 COMMENT '只读锁定',
  `remark`             VARCHAR(1000) NULL,
  `create_time`        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time`        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`            TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_project_no` (`project_no`),
  KEY `idx_status` (`status`),
  KEY `idx_customer` (`customer_id`),
  KEY `idx_name` (`name`),
  KEY `idx_accept_date` (`plan_accept_date`),
  KEY `idx_manager` (`manager`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='项目';

-- 项目相关人（甲方联系人 / 乙方交付成员，自由文本，不绑定登录账号）
CREATE TABLE `biz_project_member` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `project_id`  BIGINT UNSIGNED NOT NULL,
  `member_type` VARCHAR(20) NOT NULL DEFAULT 'VENDOR' COMMENT 'CUSTOMER甲方/VENDOR乙方/THIRD第三方',
  `name`        VARCHAR(50) NOT NULL,
  `role`        VARCHAR(50) NULL COMMENT '如 信息科主管 / 实施工程师',
  `phone`       VARCHAR(50) NULL,
  `email`       VARCHAR(100) NULL,
  `wechat`      VARCHAR(100) NULL,
  `is_main`     TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否主联系人',
  `remark`      VARCHAR(500) NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`     TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_project` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='项目相关人';

-- 交付环境与服务器
CREATE TABLE `biz_project_env` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `project_id`  BIGINT UNSIGNED NOT NULL,
  `env_name`    VARCHAR(100) NOT NULL COMMENT '环境名称，如 生产环境',
  `env_type`    VARCHAR(20)  NOT NULL DEFAULT 'PROD' COMMENT 'PROD/TEST/UAT/DEV',
  `access_url`  VARCHAR(255) NULL COMMENT '业务访问地址',
  `cloud_vendor` VARCHAR(50) NULL COMMENT '阿里云/华为云/私有云/本地机房',
  `region`      VARCHAR(100) NULL COMMENT '地域或机房',
  `hostname`    VARCHAR(100) NULL,
  `ip`          VARCHAR(64)  NULL COMMENT '内网IP',
  `public_ip`   VARCHAR(64)  NULL,
  `os`          VARCHAR(100) NULL COMMENT '操作系统与版本',
  `cpu`         VARCHAR(50)  NULL,
  `memory_gb`   INT NULL,
  `disk_gb`     INT NULL,
  `deploy_type` VARCHAR(50)  NULL COMMENT '部署方式：Docker/裸机/K8s/云主机',
  `remark`      VARCHAR(1000) NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`     TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_project_env` (`project_id`, `env_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='交付环境';

-- 部署产品与 License
CREATE TABLE `biz_project_product` (
  `id`             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `project_id`     BIGINT UNSIGNED NOT NULL,
  `product_name`   VARCHAR(100) NOT NULL COMMENT '产品名称，如 MySQL/中间件/自研平台',
  `version`        VARCHAR(50)  NULL,
  `license_no`     VARCHAR(255) NULL,
  `license_type`   VARCHAR(50)  NULL COMMENT '永久/订阅/试用',
  `license_expire` DATE NULL COMMENT 'License到期日',
  `remark`         VARCHAR(500) NULL,
  `create_time`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`        TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_project` (`project_id`),
  KEY `idx_expire` (`license_expire`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='产品与License';

-- 合同与回款
CREATE TABLE `biz_contract` (
  `id`             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `project_id`     BIGINT UNSIGNED NOT NULL,
  `contract_no`    VARCHAR(100) NOT NULL,
  `contract_name`  VARCHAR(200) NULL,
  `amount`         DECIMAL(14,2) NULL,
  `sign_date`      DATE NULL,
  `party_a`        VARCHAR(200) NULL,
  `party_b`        VARCHAR(200) NULL,
  `payment_plan`   TEXT NULL COMMENT '付款节点JSON：[{name,rate,amount,plan_date,status}]',
  `received_amount` DECIMAL(14,2) NULL DEFAULT 0,
  `invoice_status` VARCHAR(50) NULL COMMENT '开票情况',
  `remark`         VARCHAR(1000) NULL,
  `create_time`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`        TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_project` (`project_id`),
  KEY `idx_contract_no` (`contract_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='合同与回款';

-- 里程碑
CREATE TABLE `biz_milestone` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `project_id`  BIGINT UNSIGNED NOT NULL,
  `name`        VARCHAR(100) NOT NULL COMMENT '阶段名称，如 需求确认/上线/初验/终验',
  `plan_date`   DATE NULL,
  `actual_date` DATE NULL,
  `owner`       VARCHAR(50) NULL,
  `status`      VARCHAR(20) NOT NULL DEFAULT 'NOT_START' COMMENT 'NOT_START/DOING/DONE/DELAY',
  `weight`      INT NOT NULL DEFAULT 0 COMMENT '进度权重，用于计算整体进度',
  `deliverable` VARCHAR(255) NULL COMMENT '交付物',
  `delay_reason` VARCHAR(500) NULL,
  `sort_no`     INT NOT NULL DEFAULT 0,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`     TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_project` (`project_id`),
  KEY `idx_plan_date` (`plan_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='里程碑';

-- 交付任务
CREATE TABLE `biz_task` (
  `id`           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `project_id`   BIGINT UNSIGNED NOT NULL,
  `milestone_id` BIGINT UNSIGNED NULL,
  `title`        VARCHAR(200) NOT NULL,
  `owner`        VARCHAR(50) NULL,
  `plan_start`   DATE NULL,
  `plan_end`     DATE NULL,
  `actual_end`   DATE NULL,
  `progress`     TINYINT NOT NULL DEFAULT 0,
  `status`       VARCHAR(20) NOT NULL DEFAULT 'TODO' COMMENT 'TODO/DOING/DONE/BLOCKED',
  `priority`     VARCHAR(20) NOT NULL DEFAULT 'MEDIUM' COMMENT 'HIGH/MEDIUM/LOW',
  `remark`       VARCHAR(1000) NULL,
  `create_time`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`      TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_project` (`project_id`),
  KEY `idx_owner_status` (`owner`, `status`),
  KEY `idx_plan_end` (`plan_end`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='交付任务';

-- 交付清单模板（按项目类型）
CREATE TABLE `biz_checklist_template` (
  `id`           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `project_type` VARCHAR(50) NOT NULL COMMENT '项目类型编码，* 表示通用',
  `item_name`    VARCHAR(200) NOT NULL COMMENT '应交文档/事项名称',
  `required`     TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否必填',
  `sort_no`      INT NOT NULL DEFAULT 0,
  `enabled`      TINYINT(1) NOT NULL DEFAULT 1,
  `remark`       VARCHAR(500) NULL,
  `create_time`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`      TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_type_sort` (`project_type`, `sort_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='交付清单模板';

-- 项目交付清单项（由模板实例化）
CREATE TABLE `biz_checklist_item` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `project_id`  BIGINT UNSIGNED NOT NULL,
  `template_id` BIGINT UNSIGNED NULL COMMENT '来源模板，可为空表示手工添加',
  `item_name`   VARCHAR(200) NOT NULL,
  `required`    TINYINT(1) NOT NULL DEFAULT 1,
  `status`      VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/SUBMITTED/CONFIRMED/NA',
  `document_id` BIGINT UNSIGNED NULL COMMENT '关联文档',
  `sort_no`     INT NOT NULL DEFAULT 0,
  `remark`      VARCHAR(500) NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`     TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_project_status` (`project_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='项目交付清单项';

-- 文档分类
CREATE TABLE `biz_doc_category` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `parent_id`   BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '0为顶级',
  `code`        VARCHAR(50)  NULL COMMENT '分类编码',
  `name`        VARCHAR(100) NOT NULL COMMENT '分类名称，如 部署手册',
  `sort_no`     INT NOT NULL DEFAULT 0,
  `enabled`     TINYINT(1) NOT NULL DEFAULT 1,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`     TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_parent_sort` (`parent_id`, `sort_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='文档分类';

-- 文档主表（一个逻辑文档，含多个版本）
CREATE TABLE `biz_document` (
  `id`                 BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `project_id`         BIGINT UNSIGNED NOT NULL,
  `category_id`        BIGINT UNSIGNED NULL,
  `checklist_item_id`  BIGINT UNSIGNED NULL COMMENT '关联的清单项',
  `name`               VARCHAR(255) NOT NULL COMMENT '文档名称（不含扩展名）',
  `tags`               VARCHAR(255) NULL COMMENT '标签，逗号分隔',
  `secret_level`       VARCHAR(20) NOT NULL DEFAULT 'INTERNAL' COMMENT 'PUBLIC/INTERNAL/SECRET',
  `latest_version_id`  BIGINT UNSIGNED NULL COMMENT '当前版本',
  `latest_version_no`  VARCHAR(20) NULL COMMENT '冗余版本号，便于列表展示',
  `uploader_id`        BIGINT UNSIGNED NULL,
  `uploader_name`      VARCHAR(50) NULL,
  `download_count`     INT NOT NULL DEFAULT 0,
  `view_count`         INT NOT NULL DEFAULT 0,
  `remark`             VARCHAR(1000) NULL,
  `create_time`        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time`        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`            TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_project_cat` (`project_id`, `category_id`),
  KEY `idx_name` (`name`),
  KEY `idx_project_name` (`project_id`, `name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='交付文档';

-- 文档版本（实际文件）
CREATE TABLE `biz_document_version` (
  `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `document_id`   BIGINT UNSIGNED NOT NULL,
  `version_no`    VARCHAR(20) NOT NULL COMMENT 'V1.0/V1.1，或递增整数',
  `file_name`     VARCHAR(255) NOT NULL COMMENT '原始文件名',
  `file_path`     VARCHAR(500) NOT NULL COMMENT '相对 uploads 的路径',
  `file_ext`      VARCHAR(20)  NULL,
  `file_size`     BIGINT NOT NULL DEFAULT 0 COMMENT '字节',
  `md5`           VARCHAR(64)  NULL COMMENT '文件MD5，用于秒传与去重',
  `description`   VARCHAR(500) NULL COMMENT '版本说明',
  `uploader_id`   BIGINT UNSIGNED NULL,
  `uploader_name` VARCHAR(50) NULL,
  `create_time`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted`       TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_doc_version` (`document_id`, `version_no`),
  KEY `idx_document` (`document_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='文档版本';

-- 凭据主表
CREATE TABLE `biz_credential` (
  `id`              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `project_id`      BIGINT UNSIGNED NOT NULL,
  `platform_name`   VARCHAR(150) NOT NULL COMMENT '平台/系统名称，如 云平台控制台、MySQL生产库',
  `cred_type`       VARCHAR(50)  NOT NULL DEFAULT 'WEB' COMMENT '字典 CRED_TYPE：WEB/DB/SSH/KEY/TOKEN/CERT/DOMAIN/SMS/PAY/LICENSE/GIT/BASTION/OTHER',
  `env_type`        VARCHAR(20)  NOT NULL DEFAULT 'PROD' COMMENT 'PROD/TEST/UAT/DEV',
  `url`             VARCHAR(500) NULL COMMENT '访问地址',
  `host`            VARCHAR(150) NULL COMMENT '主机名或IP',
  `port`            INT NULL,
  `username`        VARCHAR(150) NULL COMMENT '登录账号（明文存储，界面默认掩码展示）',
  `secret_type`     VARCHAR(20)  NOT NULL DEFAULT 'PASSWORD' COMMENT 'PASSWORD/KEY/TOKEN/CERT/NONE',
  `secret_cipher`   TEXT NULL COMMENT 'AES-256-GCM 加密后的 Base64（含 IV 与 tag）',
  `secret_hint`     VARCHAR(50) NULL COMMENT '非敏感提示，如 长度12/含特殊字符',
  `attachment_path` VARCHAR(500) NULL COMMENT '证书/私钥/License 文件相对路径（文件本身也加密存放）',
  `attachment_name` VARCHAR(255) NULL,
  `owner`           VARCHAR(50) NULL COMMENT '负责人',
  `expire_date`     DATE NULL COMMENT '证书/域名/License 到期日',
  `rotate_days`     INT NULL COMMENT '密码轮换周期（天），空表示不轮换',
  `last_rotate_date` DATE NULL COMMENT '上次改密日期',
  `next_rotate_date` DATE NULL COMMENT '下次应改密日期（由 last_rotate_date + rotate_days 计算）',
  `strength`        VARCHAR(20) NULL COMMENT 'WEAK/MEDIUM/STRONG，保存时自动评估',
  `favorite`        TINYINT(1) NOT NULL DEFAULT 0 COMMENT '收藏置顶',
  `need_change`     TINYINT(1) NOT NULL DEFAULT 0 COMMENT '待改密标记（如人员离职后）',
  `remark`          VARCHAR(1000) NULL,
  `create_time`     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time`     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`         TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_project` (`project_id`),
  KEY `idx_project_type` (`project_id`, `cred_type`),
  KEY `idx_platform` (`platform_name`),
  KEY `idx_expire` (`expire_date`),
  KEY `idx_next_rotate` (`next_rotate_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='账号凭据';

-- 凭据变更历史（新旧值加密保存，便于回溯）
CREATE TABLE `biz_credential_history` (
  `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `credential_id` BIGINT UNSIGNED NOT NULL,
  `change_type`   VARCHAR(20) NOT NULL COMMENT 'CREATE/UPDATE/ROTATE/DELETE/IMPORT',
  `field_name`    VARCHAR(50) NULL COMMENT '变更字段，如 secret_cipher/username/url',
  `old_cipher`    TEXT NULL COMMENT '旧值（敏感字段加密存储）',
  `new_cipher`    TEXT NULL COMMENT '新值（敏感字段加密存储）',
  `operator_id`   BIGINT UNSIGNED NULL,
  `operator_name` VARCHAR(50) NULL,
  `remark`        VARCHAR(500) NULL,
  `create_time`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_cred_time` (`credential_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='凭据变更历史';

-- 凭据访问记录（谁看过/复制过哪条凭据）
CREATE TABLE `biz_credential_access` (
  `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `credential_id` BIGINT UNSIGNED NOT NULL,
  `project_id`    BIGINT UNSIGNED NOT NULL,
  `user_id`       BIGINT UNSIGNED NULL,
  `username`      VARCHAR(50) NULL,
  `action`        VARCHAR(20) NOT NULL COMMENT 'VIEW/COPY/EXPORT/ATTACHMENT',
  `ip`            VARCHAR(64) NULL,
  `user_agent`    VARCHAR(500) NULL,
  `recheck_used`  TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否使用了二次密码确认',
  `success`       TINYINT(1) NOT NULL DEFAULT 1,
  `create_time`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_cred_time` (`credential_id`, `create_time`),
  KEY `idx_user_time` (`user_id`, `create_time`),
  KEY `idx_project` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='凭据访问记录';

CREATE OR REPLACE VIEW `v_expire_alert` AS
SELECT 'CREDENTIAL' AS src_type, c.id AS src_id, c.project_id,
       CONCAT(p.project_no, ' ', p.name) AS project_name,
       CONCAT(c.platform_name, '（', c.cred_type, '）') AS item_name,
       c.expire_date AS expire_date,
       DATEDIFF(c.expire_date, CURDATE()) AS days_left
FROM biz_credential c
JOIN biz_project p ON p.id = c.project_id AND p.deleted = 0
WHERE c.deleted = 0 AND c.expire_date IS NOT NULL
UNION ALL
SELECT 'ROTATE', c.id, c.project_id,
       CONCAT(p.project_no, ' ', p.name),
       CONCAT(c.platform_name, ' 密码轮换'),
       c.next_rotate_date, DATEDIFF(c.next_rotate_date, CURDATE())
FROM biz_credential c
JOIN biz_project p ON p.id = c.project_id AND p.deleted = 0
WHERE c.deleted = 0 AND c.next_rotate_date IS NOT NULL
UNION ALL
SELECT 'LICENSE', pr.id, pr.project_id,
       CONCAT(p.project_no, ' ', p.name),
       CONCAT(pr.product_name, ' License'),
       pr.license_expire, DATEDIFF(pr.license_expire, CURDATE())
FROM biz_project_product pr
JOIN biz_project p ON p.id = pr.project_id AND p.deleted = 0
WHERE pr.deleted = 0 AND pr.license_expire IS NOT NULL
UNION ALL
SELECT 'WARRANTY', p.id, p.id,
       CONCAT(p.project_no, ' ', p.name),
       '质保到期',
       p.warranty_end_date, DATEDIFF(p.warranty_end_date, CURDATE())
FROM biz_project p
WHERE p.deleted = 0 AND p.warranty_end_date IS NOT NULL AND p.archived = 0
UNION ALL
SELECT 'MILESTONE', m.id, m.project_id,
       CONCAT(p.project_no, ' ', p.name),
       CONCAT('里程碑：', m.name),
       m.plan_date, DATEDIFF(m.plan_date, CURDATE())
FROM biz_milestone m
JOIN biz_project p ON p.id = m.project_id AND p.deleted = 0
WHERE m.deleted = 0 AND m.status <> 'DONE' AND m.plan_date IS NOT NULL;
