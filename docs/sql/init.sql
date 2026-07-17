-- ========================================
-- 交付项目管理系统数据库初始化脚本
-- ========================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS delivery_management DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE delivery_management;

-- ========================================
-- 1. 组织与用户管理
-- ========================================

-- 用户表
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  username VARCHAR(50) NOT NULL COMMENT '用户名',
  password VARCHAR(100) NOT NULL COMMENT '密码',
  real_name VARCHAR(50) COMMENT '真实姓名',
  email VARCHAR(100) COMMENT '邮箱',
  phone VARCHAR(20) COMMENT '手机号',
  dept_id BIGINT COMMENT '部门ID',
  status TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
  avatar VARCHAR(255) COMMENT '头像',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  create_by BIGINT COMMENT '创建人',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  update_by BIGINT COMMENT '更新人',
  deleted TINYINT DEFAULT 0 COMMENT '删除标识：0-未删除 1-已删除',
  PRIMARY KEY (id),
  UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 部门表
DROP TABLE IF EXISTS sys_department;
CREATE TABLE sys_department (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '部门ID',
  dept_name VARCHAR(50) NOT NULL COMMENT '部门名称',
  parent_id BIGINT DEFAULT 0 COMMENT '父部门ID',
  dept_code VARCHAR(50) COMMENT '部门编码',
  sort INT DEFAULT 0 COMMENT '排序',
  leader VARCHAR(50) COMMENT '负责人',
  phone VARCHAR(20) COMMENT '联系电话',
  status TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  create_by BIGINT,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  update_by BIGINT,
  deleted TINYINT DEFAULT 0,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

-- ========================================
-- 2. 角色与权限管理
-- ========================================

-- 角色表
DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  role_code VARCHAR(50) NOT NULL COMMENT '角色编码',
  role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
  description VARCHAR(200) COMMENT '描述',
  status TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  create_by BIGINT,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  update_by BIGINT,
  deleted TINYINT DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 用户角色关联表
DROP TABLE IF EXISTS sys_user_role;
CREATE TABLE sys_user_role (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL COMMENT '用户ID',
  role_id BIGINT NOT NULL COMMENT '角色ID',
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_role (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- ========================================
-- 4. 交付项目台账与立项
-- ========================================

-- 客户表
DROP TABLE IF EXISTS customer;
CREATE TABLE customer (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '客户ID',
  customer_name VARCHAR(100) NOT NULL COMMENT '客户名称',
  customer_code VARCHAR(50) COMMENT '客户编码',
  contact_person VARCHAR(50) COMMENT '联系人',
  contact_phone VARCHAR(20) COMMENT '联系电话',
  contact_email VARCHAR(100) COMMENT '联系邮箱',
  address VARCHAR(255) COMMENT '地址',
  status TINYINT DEFAULT 1 COMMENT '状态：0-停用 1-启用',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  create_by BIGINT,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  update_by BIGINT,
  deleted TINYINT DEFAULT 0,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户表';

-- 项目表
DROP TABLE IF EXISTS delivery_project;
CREATE TABLE delivery_project (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '项目ID',
  project_code VARCHAR(50) NOT NULL COMMENT '项目编号',
  project_name VARCHAR(100) NOT NULL COMMENT '项目名称',
  customer_id BIGINT NOT NULL COMMENT '客户ID',
  contract_no VARCHAR(50) COMMENT '合同编号',
  contract_amount DECIMAL(15,2) COMMENT '合同金额',
  start_date DATE COMMENT '开始日期',
  end_date DATE COMMENT '结束日期',
  project_manager_id BIGINT COMMENT '项目经理ID',
  status TINYINT DEFAULT 0 COMMENT '状态：0-立项 1-进行中 2-已验收 3-已关闭',
  description TEXT COMMENT '项目描述',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  create_by BIGINT,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  update_by BIGINT,
  deleted TINYINT DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_project_code (project_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交付项目表';

-- ========================================
-- 5. 交付物与文档管理
-- ========================================

-- 文档表
DROP TABLE IF EXISTS project_document;
CREATE TABLE project_document (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '文档ID',
  project_id BIGINT NOT NULL COMMENT '项目ID',
  doc_name VARCHAR(200) NOT NULL COMMENT '文档名称',
  doc_type VARCHAR(50) COMMENT '文档类型',
  file_path VARCHAR(500) COMMENT '文件路径',
  file_size BIGINT COMMENT '文件大小（字节）',
  version VARCHAR(20) DEFAULT '1.0' COMMENT '版本号',
  status TINYINT DEFAULT 1 COMMENT '状态：0-草稿 1-正式',
  description TEXT COMMENT '描述',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  create_by BIGINT,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  update_by BIGINT,
  deleted TINYINT DEFAULT 0,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目文档表';

-- ========================================
-- 6. 交付计划与范围管理
-- ========================================

-- 里程碑表
DROP TABLE IF EXISTS project_milestone;
CREATE TABLE project_milestone (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '里程碑ID',
  project_id BIGINT NOT NULL COMMENT '项目ID',
  milestone_name VARCHAR(100) NOT NULL COMMENT '里程碑名称',
  plan_date DATE COMMENT '计划日期',
  actual_date DATE COMMENT '实际日期',
  status TINYINT DEFAULT 0 COMMENT '状态：0-未开始 1-进行中 2-已完成',
  description TEXT COMMENT '描述',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  create_by BIGINT,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  update_by BIGINT,
  deleted TINYINT DEFAULT 0,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目里程碑表';

-- 任务表（WBS）
DROP TABLE IF EXISTS project_task;
CREATE TABLE project_task (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  project_id BIGINT NOT NULL COMMENT '项目ID',
  parent_id BIGINT DEFAULT 0 COMMENT '父任务ID',
  task_name VARCHAR(200) NOT NULL COMMENT '任务名称',
  assignee_id BIGINT COMMENT '负责人ID',
  start_date DATE COMMENT '开始日期',
  end_date DATE COMMENT '结束日期',
  progress INT DEFAULT 0 COMMENT '进度（0-100）',
  status TINYINT DEFAULT 0 COMMENT '状态：0-未开始 1-进行中 2-已完成',
  priority TINYINT DEFAULT 2 COMMENT '优先级：1-高 2-中 3-低',
  description TEXT COMMENT '描述',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  create_by BIGINT,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  update_by BIGINT,
  deleted TINYINT DEFAULT 0,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目任务表';

-- ========================================
-- 7. 问题与变更管理
-- ========================================

-- 问题表
DROP TABLE IF EXISTS project_issue;
CREATE TABLE project_issue (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '问题ID',
  project_id BIGINT NOT NULL COMMENT '项目ID',
  issue_code VARCHAR(50) COMMENT '问题编号',
  title VARCHAR(200) NOT NULL COMMENT '问题标题',
  description TEXT COMMENT '问题描述',
  issue_type TINYINT DEFAULT 1 COMMENT '类型：1-问题 2-风险',
  priority TINYINT DEFAULT 2 COMMENT '优先级：1-高 2-中 3-低',
  status TINYINT DEFAULT 0 COMMENT '状态：0-待处理 1-处理中 2-已解决 3-已关闭',
  reporter_id BIGINT COMMENT '提报人ID',
  assignee_id BIGINT COMMENT '处理人ID',
  resolve_time DATETIME COMMENT '解决时间',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  create_by BIGINT,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  update_by BIGINT,
  deleted TINYINT DEFAULT 0,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目问题表';

-- 变更申请表
DROP TABLE IF EXISTS project_change;
CREATE TABLE project_change (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '变更ID',
  project_id BIGINT NOT NULL COMMENT '项目ID',
  change_code VARCHAR(50) COMMENT '变更编号',
  title VARCHAR(200) NOT NULL COMMENT '变更标题',
  description TEXT COMMENT '变更描述',
  change_type TINYINT DEFAULT 1 COMMENT '类型：1-范围 2-时间 3-成本',
  status TINYINT DEFAULT 0 COMMENT '状态：0-待审批 1-已批准 2-已拒绝',
  applicant_id BIGINT COMMENT '申请人ID',
  approver_id BIGINT COMMENT '审批人ID',
  approve_time DATETIME COMMENT '审批时间',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  create_by BIGINT,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  update_by BIGINT,
  deleted TINYINT DEFAULT 0,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目变更表';

-- ========================================
-- 8. 客户协同与验收确认
-- ========================================

-- 验收单表
DROP TABLE IF EXISTS project_acceptance;
CREATE TABLE project_acceptance (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '验收ID',
  project_id BIGINT NOT NULL COMMENT '项目ID',
  acceptance_code VARCHAR(50) COMMENT '验收单编号',
  title VARCHAR(200) NOT NULL COMMENT '验收标题',
  acceptance_date DATE COMMENT '验收日期',
  customer_sign VARCHAR(50) COMMENT '客户签字人',
  status TINYINT DEFAULT 0 COMMENT '状态：0-待验收 1-已通过 2-未通过',
  result TEXT COMMENT '验收结果',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  create_by BIGINT,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  update_by BIGINT,
  deleted TINYINT DEFAULT 0,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目验收表';

-- ========================================
-- 9. 资源与工时管理
-- ========================================

-- 工时记录表
DROP TABLE IF EXISTS project_worklog;
CREATE TABLE project_worklog (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '工时ID',
  project_id BIGINT NOT NULL COMMENT '项目ID',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  work_date DATE NOT NULL COMMENT '工作日期',
  work_hours DECIMAL(5,2) NOT NULL COMMENT '工时（小时）',
  task_id BIGINT COMMENT '任务ID',
  description TEXT COMMENT '工作内容',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  create_by BIGINT,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  update_by BIGINT,
  deleted TINYINT DEFAULT 0,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工时记录表';

-- ========================================
-- 11. 项目售后管理
-- ========================================

-- 工单表
DROP TABLE IF EXISTS service_ticket;
CREATE TABLE service_ticket (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '工单ID',
  ticket_code VARCHAR(50) NOT NULL COMMENT '工单编号',
  project_id BIGINT NOT NULL COMMENT '项目ID',
  title VARCHAR(200) NOT NULL COMMENT '工单标题',
  description TEXT COMMENT '问题描述',
  priority TINYINT DEFAULT 2 COMMENT '优先级：1-高 2-中 3-低',
  status TINYINT DEFAULT 0 COMMENT '状态：0-待处理 1-处理中 2-已解决 3-已关闭',
  reporter_id BIGINT COMMENT '提交人ID',
  assignee_id BIGINT COMMENT '处理人ID',
  sla_deadline DATETIME COMMENT 'SLA截止时间',
  resolve_time DATETIME COMMENT '解决时间',
  satisfaction TINYINT COMMENT '满意度：1-5分',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  create_by BIGINT,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  update_by BIGINT,
  deleted TINYINT DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_ticket_code (ticket_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工单表';

-- ========================================
-- 初始化数据
-- ========================================

-- 初始化管理员用户（密码：admin123，使用BCrypt加密）
INSERT INTO sys_user (username, password, real_name, status) 
VALUES ('admin', '.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', 1);

-- 初始化角色
INSERT INTO sys_role (role_code, role_name, description) VALUES 
('ADMIN', '管理员', '系统管理员'),
('PM', '项目经理', '项目经理'),
('MEMBER', '项目成员', '普通项目成员');

-- 初始化用户角色关联
INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1);

-- 完成
SELECT '数据库初始化完成！' AS message;
