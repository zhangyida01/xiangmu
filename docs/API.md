# API 接口文档

## 基础信息

- 基础路径: \http://服务器IP:8080/api\
- 数据格式: JSON
- 字符编码: UTF-8
- 认证方式: JWT Token

## 统一响应格式

\\\json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
\\\

**响应码说明：**
- 200：成功
- 400：请求参数错误
- 401：未授权
- 403：无权限
- 404：资源不存在
- 500：服务器错误

## 1. 系统管理

### 1.1 健康检查

\\\
GET /health
\\\

**响应示例：**
\\\json
{
  "code": 200,
  "message": "操作成功",
  "data": "系统运行正常"
}
\\\

### 1.2 用户登录

\\\
POST /auth/login
\\\

**请求参数：**
\\\json
{
  "username": "admin",
  "password": "admin123"
}
\\\

**响应示例：**
\\\json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userInfo": {
      "id": 1,
      "username": "admin",
      "realName": "系统管理员"
    }
  }
}
\\\

### 1.3 用户登出

\\\
POST /auth/logout
\\\

**请求头：**
\\\
Authorization: Bearer {token}
\\\

## 2. 用户管理

### 2.1 用户列表

\\\
GET /user/list?page=1&size=10&keyword=
\\\

**请求参数：**
- page: 页码（默认1）
- size: 每页数量（默认10）
- keyword: 搜索关键词（可选）

**响应示例：**
\\\json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 5,
    "records": [
      {
        "id": 1,
        "username": "admin",
        "realName": "系统管理员",
        "email": "admin@example.com",
        "phone": "13800138000",
        "deptId": 1,
        "status": 1,
        "createTime": "2024-01-01 10:00:00"
      }
    ]
  }
}
\\\

### 2.2 添加用户

\\\
POST /user/add
\\\

**请求参数：**
\\\json
{
  "username": "zhangsan",
  "password": "123456",
  "realName": "张三",
  "email": "zhangsan@example.com",
  "phone": "13800138001",
  "deptId": 1,
  "status": 1
}
\\\

### 2.3 更新用户

\\\
PUT /user/update
\\\

**请求参数：**
\\\json
{
  "id": 2,
  "realName": "张三三",
  "email": "zhangsan@example.com",
  "phone": "13800138002",
  "status": 1
}
\\\

### 2.4 删除用户

\\\
DELETE /user/delete/{id}
\\\

## 3. 部门管理

### 3.1 部门列表

\\\
GET /department/list
\\\

**响应示例：**
\\\json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "deptName": "技术部",
      "parentId": 0,
      "deptCode": "TECH",
      "leader": "张经理",
      "phone": "13800138000",
      "status": 1
    }
  ]
}
\\\

### 3.2 添加部门

\\\
POST /department/add
\\\

**请求参数：**
\\\json
{
  "deptName": "研发部",
  "parentId": 1,
  "deptCode": "DEV",
  "leader": "李经理",
  "phone": "13800138001",
  "sort": 1,
  "status": 1
}
\\\

## 4. 角色管理

### 4.1 角色列表

\\\
GET /role/list
\\\

**响应示例：**
\\\json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "roleCode": "ADMIN",
      "roleName": "管理员",
      "description": "系统管理员",
      "status": 1
    }
  ]
}
\\\

## 5. 客户管理

### 5.1 客户列表

\\\
GET /customer/list?page=1&size=10
\\\

**响应示例：**
\\\json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 10,
    "records": [
      {
        "id": 1,
        "customerName": "XX科技有限公司",
        "customerCode": "C001",
        "contactPerson": "王总",
        "contactPhone": "13900139000",
        "contactEmail": "wang@example.com",
        "address": "北京市朝阳区",
        "status": 1
      }
    ]
  }
}
\\\

### 5.2 添加客户

\\\
POST /customer/add
\\\

**请求参数：**
\\\json
{
  "customerName": "XX科技有限公司",
  "customerCode": "C001",
  "contactPerson": "王总",
  "contactPhone": "13900139000",
  "contactEmail": "wang@example.com",
  "address": "北京市朝阳区",
  "status": 1
}
\\\

## 6. 项目管理

### 6.1 项目列表

\\\
GET /project/list?page=1&size=10&status=
\\\

**请求参数：**
- page: 页码
- size: 每页数量
- status: 项目状态（0-立项 1-进行中 2-已验收 3-已关闭）

**响应示例：**
\\\json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 20,
    "records": [
      {
        "id": 1,
        "projectCode": "PRJ20240101001",
        "projectName": "XX系统交付项目",
        "customerId": 1,
        "contractNo": "HT20240101",
        "contractAmount": 1000000.00,
        "startDate": "2024-01-01",
        "endDate": "2024-12-31",
        "projectManagerId": 2,
        "status": 1,
        "description": "项目描述"
      }
    ]
  }
}
\\\

### 6.2 创建项目

\\\
POST /project/add
\\\

**请求参数：**
\\\json
{
  "projectCode": "PRJ20240101001",
  "projectName": "XX系统交付项目",
  "customerId": 1,
  "contractNo": "HT20240101",
  "contractAmount": 1000000.00,
  "startDate": "2024-01-01",
  "endDate": "2024-12-31",
  "projectManagerId": 2,
  "description": "项目描述"
}
\\\

### 6.3 更新项目

\\\
PUT /project/update
\\\

### 6.4 删除项目

\\\
DELETE /project/delete/{id}
\\\

### 6.5 项目详情

\\\
GET /project/detail/{id}
\\\

## 7. 任务管理

### 7.1 任务列表

\\\
GET /task/list?projectId=1
\\\

**响应示例：**
\\\json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "projectId": 1,
      "parentId": 0,
      "taskName": "需求分析",
      "assigneeId": 3,
      "startDate": "2024-01-01",
      "endDate": "2024-01-15",
      "progress": 80,
      "status": 1,
      "priority": 1
    }
  ]
}
\\\

### 7.2 创建任务

\\\
POST /task/add
\\\

**请求参数：**
\\\json
{
  "projectId": 1,
  "parentId": 0,
  "taskName": "需求分析",
  "assigneeId": 3,
  "startDate": "2024-01-01",
  "endDate": "2024-01-15",
  "priority": 1,
  "description": "任务描述"
}
\\\

### 7.3 更新任务进度

\\\
PUT /task/updateProgress
\\\

**请求参数：**
\\\json
{
  "id": 1,
  "progress": 80,
  "status": 1
}
\\\

## 8. 里程碑管理

### 8.1 里程碑列表

\\\
GET /milestone/list?projectId=1
\\\

### 8.2 创建里程碑

\\\
POST /milestone/add
\\\

**请求参数：**
\\\json
{
  "projectId": 1,
  "milestoneName": "需求评审完成",
  "planDate": "2024-01-15",
  "description": "描述"
}
\\\

## 9. 问题管理

### 9.1 问题列表

\\\
GET /issue/list?projectId=1&status=
\\\

**响应示例：**
\\\json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "projectId": 1,
      "issueCode": "ISSUE001",
      "title": "登录功能异常",
      "issueType": 1,
      "priority": 1,
      "status": 0,
      "reporterId": 3,
      "assigneeId": 4
    }
  ]
}
\\\

### 9.2 创建问题

\\\
POST /issue/add
\\\

**请求参数：**
\\\json
{
  "projectId": 1,
  "title": "登录功能异常",
  "description": "详细描述",
  "issueType": 1,
  "priority": 1,
  "reporterId": 3,
  "assigneeId": 4
}
\\\

### 9.3 更新问题状态

\\\
PUT /issue/updateStatus
\\\

**请求参数：**
\\\json
{
  "id": 1,
  "status": 2
}
\\\

## 10. 工时管理

### 10.1 工时记录列表

\\\
GET /worklog/list?projectId=1&userId=&startDate=&endDate=
\\\

### 10.2 填报工时

\\\
POST /worklog/add
\\\

**请求参数：**
\\\json
{
  "projectId": 1,
  "userId": 3,
  "workDate": "2024-01-10",
  "workHours": 8.0,
  "taskId": 1,
  "description": "完成需求文档编写"
}
\\\

## 11. 工单管理

### 11.1 工单列表

\\\
GET /ticket/list?projectId=1&status=
\\\

**响应示例：**
\\\json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "ticketCode": "TK20240101001",
      "projectId": 1,
      "title": "系统无法访问",
      "priority": 1,
      "status": 0,
      "reporterId": 5,
      "assigneeId": 3,
      "slaDeadline": "2024-01-10 18:00:00"
    }
  ]
}
\\\

### 11.2 创建工单

\\\
POST /ticket/add
\\\

**请求参数：**
\\\json
{
  "projectId": 1,
  "title": "系统无法访问",
  "description": "详细描述",
  "priority": 1,
  "reporterId": 5,
  "assigneeId": 3
}
\\\

### 11.3 工单处理

\\\
PUT /ticket/resolve
\\\

**请求参数：**
\\\json
{
  "id": 1,
  "status": 2,
  "satisfaction": 5
}
\\\

## 12. 统计看板

### 12.1 项目统计

\\\
GET /dashboard/projectStats
\\\

**响应示例：**
\\\json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "totalProjects": 20,
    "ongoingProjects": 8,
    "completedProjects": 10,
    "closedProjects": 2
  }
}
\\\

### 12.2 问题统计

\\\
GET /dashboard/issueStats?projectId=1
\\\

### 12.3 工时统计

\\\
GET /dashboard/worklogStats?projectId=1&startDate=&endDate=
\\\

## 错误码说明

| 错误码 | 说明 |
|-------|------|
| 200 | 操作成功 |
| 400 | 请求参数错误 |
| 401 | 未登录或 Token 失效 |
| 403 | 无权限访问 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

## 注意事项

1. 除登录接口外，所有接口都需要在请求头中携带 Token
2. Token 有效期为 24 小时
3. 日期格式统一为：\yyyy-MM-dd\ 或 \yyyy-MM-dd HH:mm:ss\
4. 分页查询默认返回第 1 页，每页 10 条
5. 删除操作为逻辑删除，不会真正删除数据
