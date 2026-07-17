# 数据库设计说明

## 数据库概览

- **数据库名称**: delivery_management
- **字符集**: utf8mb4
- **排序规则**: utf8mb4_general_ci
- **表数量**: 14张核心表
- **设计原则**: 简单实用、易于维护

## 表结构设计

### 1. 系统管理模块

#### 1.1 用户表 (sys_user)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| username | VARCHAR(50) | 用户名（唯一） |
| password | VARCHAR(100) | 密码（BCrypt加密） |
| real_name | VARCHAR(50) | 真实姓名 |
| email | VARCHAR(100) | 邮箱 |
| phone | VARCHAR(20) | 手机号 |
| dept_id | BIGINT | 部门ID（外键） |
| status | TINYINT | 状态：0-禁用 1-启用 |
| avatar | VARCHAR(255) | 头像路径 |

#### 1.2 部门表 (sys_department)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| dept_name | VARCHAR(50) | 部门名称 |
| parent_id | BIGINT | 父部门ID（支持树形结构） |
| dept_code | VARCHAR(50) | 部门编码 |
| sort | INT | 排序 |
| leader | VARCHAR(50) | 负责人 |
| phone | VARCHAR(20) | 联系电话 |
| status | TINYINT | 状态：0-禁用 1-启用 |

#### 1.3 角色表 (sys_role)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| role_code | VARCHAR(50) | 角色编码（唯一） |
| role_name | VARCHAR(50) | 角色名称 |
| description | VARCHAR(200) | 描述 |
| status | TINYINT | 状态：0-禁用 1-启用 |

**预置角色：**
- ADMIN - 管理员（全部权限）
- PM - 项目经理（项目管理权限）
- MEMBER - 项目成员（基本权限）

#### 1.4 用户角色关联表 (sys_user_role)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| user_id | BIGINT | 用户ID |
| role_id | BIGINT | 角色ID |

**联合唯一索引**: (user_id, role_id)

### 2. 客户管理模块

#### 2.1 客户表 (customer)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| customer_name | VARCHAR(100) | 客户名称 |
| customer_code | VARCHAR(50) | 客户编码 |
| contact_person | VARCHAR(50) | 联系人 |
| contact_phone | VARCHAR(20) | 联系电话 |
| contact_email | VARCHAR(100) | 联系邮箱 |
| address | VARCHAR(255) | 地址 |
| status | TINYINT | 状态：0-停用 1-启用 |

### 3. 项目管理模块

#### 3.1 项目表 (delivery_project)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| project_code | VARCHAR(50) | 项目编号（唯一） |
| project_name | VARCHAR(100) | 项目名称 |
| customer_id | BIGINT | 客户ID（外键） |
| contract_no | VARCHAR(50) | 合同编号 |
| contract_amount | DECIMAL(15,2) | 合同金额 |
| start_date | DATE | 开始日期 |
| end_date | DATE | 结束日期 |
| project_manager_id | BIGINT | 项目经理ID（外键） |
| status | TINYINT | 状态：0-立项 1-进行中 2-已验收 3-已关闭 |
| description | TEXT | 项目描述 |

**项目生命周期：**
`
立项(0) → 进行中(1) → 已验收(2) → 已关闭(3)
`

#### 3.2 项目文档表 (project_document)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| project_id | BIGINT | 项目ID（外键） |
| doc_name | VARCHAR(200) | 文档名称 |
| doc_type | VARCHAR(50) | 文档类型 |
| file_path | VARCHAR(500) | 文件路径 |
| file_size | BIGINT | 文件大小（字节） |
| version | VARCHAR(20) | 版本号 |
| status | TINYINT | 状态：0-草稿 1-正式 |
| description | TEXT | 描述 |

#### 3.3 里程碑表 (project_milestone)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| project_id | BIGINT | 项目ID（外键） |
| milestone_name | VARCHAR(100) | 里程碑名称 |
| plan_date | DATE | 计划日期 |
| actual_date | DATE | 实际日期 |
| status | TINYINT | 状态：0-未开始 1-进行中 2-已完成 |
| description | TEXT | 描述 |

#### 3.4 任务表/WBS (project_task)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| project_id | BIGINT | 项目ID（外键） |
| parent_id | BIGINT | 父任务ID（支持WBS分解） |
| task_name | VARCHAR(200) | 任务名称 |
| assignee_id | BIGINT | 负责人ID（外键） |
| start_date | DATE | 开始日期 |
| end_date | DATE | 结束日期 |
| progress | INT | 进度（0-100） |
| status | TINYINT | 状态：0-未开始 1-进行中 2-已完成 |
| priority | TINYINT | 优先级：1-高 2-中 3-低 |
| description | TEXT | 描述 |

**支持多级WBS分解：**
`
项目 (parent_id=0)
├── 阶段1 (parent_id=0)
│   ├── 任务1.1 (parent_id=阶段1_id)
│   └── 任务1.2 (parent_id=阶段1_id)
└── 阶段2 (parent_id=0)
`

### 4. 问题与变更管理

#### 4.1 问题表 (project_issue)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| project_id | BIGINT | 项目ID（外键） |
| issue_code | VARCHAR(50) | 问题编号 |
| title | VARCHAR(200) | 问题标题 |
| description | TEXT | 问题描述 |
| issue_type | TINYINT | 类型：1-问题 2-风险 |
| priority | TINYINT | 优先级：1-高 2-中 3-低 |
| status | TINYINT | 状态：0-待处理 1-处理中 2-已解决 3-已关闭 |
| reporter_id | BIGINT | 提报人ID（外键） |
| assignee_id | BIGINT | 处理人ID（外键） |
| resolve_time | DATETIME | 解决时间 |

**问题处理流程：**
`
待处理(0) → 处理中(1) → 已解决(2) → 已关闭(3)
`

#### 4.2 变更申请表 (project_change)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| project_id | BIGINT | 项目ID（外键） |
| change_code | VARCHAR(50) | 变更编号 |
| title | VARCHAR(200) | 变更标题 |
| description | TEXT | 变更描述 |
| change_type | TINYINT | 类型：1-范围 2-时间 3-成本 |
| status | TINYINT | 状态：0-待审批 1-已批准 2-已拒绝 |
| applicant_id | BIGINT | 申请人ID（外键） |
| approver_id | BIGINT | 审批人ID（外键） |
| approve_time | DATETIME | 审批时间 |

### 5. 验收管理

#### 5.1 验收单表 (project_acceptance)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| project_id | BIGINT | 项目ID（外键） |
| acceptance_code | VARCHAR(50) | 验收单编号 |
| title | VARCHAR(200) | 验收标题 |
| acceptance_date | DATE | 验收日期 |
| customer_sign | VARCHAR(50) | 客户签字人 |
| status | TINYINT | 状态：0-待验收 1-已通过 2-未通过 |
| result | TEXT | 验收结果 |

### 6. 工时管理

#### 6.1 工时记录表 (project_worklog)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| project_id | BIGINT | 项目ID（外键） |
| user_id | BIGINT | 用户ID（外键） |
| work_date | DATE | 工作日期 |
| work_hours | DECIMAL(5,2) | 工时（小时） |
| task_id | BIGINT | 任务ID（外键，可选） |
| description | TEXT | 工作内容 |

### 7. 售后管理

#### 7.1 工单表 (service_ticket)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| ticket_code | VARCHAR(50) | 工单编号（唯一） |
| project_id | BIGINT | 项目ID（外键） |
| title | VARCHAR(200) | 工单标题 |
| description | TEXT | 问题描述 |
| priority | TINYINT | 优先级：1-高 2-中 3-低 |
| status | TINYINT | 状态：0-待处理 1-处理中 2-已解决 3-已关闭 |
| reporter_id | BIGINT | 提交人ID（外键） |
| assignee_id | BIGINT | 处理人ID（外键） |
| sla_deadline | DATETIME | SLA截止时间 |
| resolve_time | DATETIME | 解决时间 |
| satisfaction | TINYINT | 满意度：1-5分 |

**SLA规则建议：**
- 高优先级：4小时内响应
- 中优先级：8小时内响应
- 低优先级：24小时内响应

## 公共字段说明

所有表都包含以下公共字段（继承自 BaseEntity）：

| 字段 | 类型 | 说明 |
|------|------|------|
| create_time | DATETIME | 创建时间（自动填充） |
| create_by | BIGINT | 创建人ID（自动填充） |
| update_time | DATETIME | 更新时间（自动更新） |
| update_by | BIGINT | 更新人ID（自动更新） |
| deleted | TINYINT | 删除标识：0-未删除 1-已删除（逻辑删除） |

## 表关系说明

### 核心关系

`
sys_user (用户)
  ├── 1:N → project_worklog (工时记录)
  ├── 1:N → project_task (负责的任务)
  ├── 1:N → project_issue (提报/处理的问题)
  └── N:M → sys_role (角色) [通过 sys_user_role]

sys_department (部门)
  └── 1:N → sys_user (用户)

customer (客户)
  └── 1:N → delivery_project (项目)

delivery_project (项目)
  ├── 1:N → project_document (文档)
  ├── 1:N → project_milestone (里程碑)
  ├── 1:N → project_task (任务)
  ├── 1:N → project_issue (问题)
  ├── 1:N → project_change (变更)
  ├── 1:N → project_acceptance (验收)
  ├── 1:N → project_worklog (工时)
  └── 1:N → service_ticket (工单)
`

## 索引设计

### 主键索引
所有表的 id 字段自动创建主键索引。

### 唯一索引
- sys_user.username
- sys_role.role_code
- delivery_project.project_code
- service_ticket.ticket_code
- sys_user_role(user_id, role_id) 联合唯一索引

### 外键索引建议
为提高查询性能，建议为以下字段创建索引：
- sys_user.dept_id
- delivery_project.customer_id
- delivery_project.project_manager_id
- project_*.project_id (所有关联项目的表)
- project_task.assignee_id
- project_worklog.user_id

## 数据初始化

系统自动初始化以下数据：

### 默认管理员
- 用户名: admin
- 密码: admin123 (BCrypt加密后存储)
- 角色: ADMIN

### 预置角色
1. ADMIN - 管理员
2. PM - 项目经理
3. MEMBER - 项目成员

## 扩展性设计

### 支持的扩展
1. **多租户**: 可在所有表添加 	enant_id 字段
2. **审计日志**: 公共字段已包含创建人、更新人信息
3. **软删除**: 所有表支持逻辑删除（deleted字段）
4. **版本控制**: 文档表已包含version字段

### 性能优化建议
1. 为高频查询字段添加索引
2. 大文本字段（description）考虑分表存储
3. 工时记录表按月份分区
4. 定期归档已关闭项目的历史数据

## 数据字典

完整的数据字典请参考 init.sql 中的注释。

---

**注意事项：**
- 所有时间字段使用 Asia/Shanghai 时区
- 金额字段使用 DECIMAL(15,2) 精确存储
- 状态字段统一使用 TINYINT 枚举
- 外键约束在应用层维护，数据库层不强制
