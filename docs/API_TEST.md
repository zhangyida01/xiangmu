# API 接口测试指南

## 基础信息

- **基础URL**: `http://120.27.247.61:9080/api`
- **认证方式**: JWT Token（请求头 `Authorization: Bearer {token}`）

## 1. 用户认证

### 1.1 用户登录

```bash
curl -X POST http://120.27.247.61:9080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsInVzZXJuYW1lIjoiYWRtaW4i...",
    "userInfo": {
      "id": 1,
      "username": "admin",
      "realName": "系统管理员",
      "email": null
    }
  }
}
```

**重要**: 保存返回的 token，后续所有接口都需要使用！

### 1.2 用户登出

```bash
curl -X POST http://120.27.247.61:9080/api/auth/logout \
  -H "Authorization: Bearer {你的token}"
```

## 2. 用户管理

### 2.1 获取用户列表

```bash
curl -X GET "http://120.27.247.61:9080/api/user/list?page=1&size=10" \
  -H "Authorization: Bearer {你的token}"
```

**参数说明：**
- `page`: 页码（默认1）
- `size`: 每页数量（默认10）
- `keyword`: 搜索关键词（可选）

### 2.2 获取用户详情

```bash
curl -X GET http://120.27.247.61:9080/api/user/detail/1 \
  -H "Authorization: Bearer {你的token}"
```

### 2.3 添加用户

```bash
curl -X POST http://120.27.247.61:9080/api/user/add \
  -H "Authorization: Bearer {你的token}" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "zhangsan",
    "password": "123456",
    "realName": "张三",
    "email": "zhangsan@example.com",
    "phone": "13800138001",
    "status": 1
  }'
```

### 2.4 更新用户

```bash
curl -X PUT http://120.27.247.61:9080/api/user/update \
  -H "Authorization: Bearer {你的token}" \
  -H "Content-Type: application/json" \
  -d '{
    "id": 2,
    "realName": "张三三",
    "email": "zhangsan@example.com",
    "phone": "13800138002"
  }'
```

### 2.5 删除用户

```bash
curl -X DELETE http://120.27.247.61:9080/api/user/delete/2 \
  -H "Authorization: Bearer {你的token}"
```

## 3. 项目管理

### 3.1 获取项目列表

```bash
curl -X GET "http://120.27.247.61:9080/api/project/list?page=1&size=10" \
  -H "Authorization: Bearer {你的token}"
```

**参数说明：**
- `page`: 页码
- `size`: 每页数量
- `status`: 项目状态（0-立项 1-进行中 2-已验收 3-已关闭）
- `keyword`: 搜索关键词

### 3.2 创建项目

```bash
curl -X POST http://120.27.247.61:9080/api/project/add \
  -H "Authorization: Bearer {你的token}" \
  -H "Content-Type: application/json" \
  -d '{
    "projectCode": "PRJ20240101001",
    "projectName": "XX系统交付项目",
    "customerId": 1,
    "contractNo": "HT20240101",
    "contractAmount": 1000000.00,
    "startDate": "2024-01-01",
    "endDate": "2024-12-31",
    "projectManagerId": 1,
    "status": 0,
    "description": "项目描述"
  }'
```

### 3.3 更新项目

```bash
curl -X PUT http://120.27.247.61:9080/api/project/update \
  -H "Authorization: Bearer {你的token}" \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "status": 1,
    "description": "项目已启动"
  }'
```

### 3.4 删除项目

```bash
curl -X DELETE http://120.27.247.61:9080/api/project/delete/1 \
  -H "Authorization: Bearer {你的token}"
```

## 4. 工单管理

### 4.1 获取工单列表

```bash
curl -X GET "http://120.27.247.61:9080/api/ticket/list?page=1&size=10" \
  -H "Authorization: Bearer {你的token}"
```

**参数说明：**
- `projectId`: 项目ID
- `status`: 工单状态（0-待处理 1-处理中 2-已解决 3-已关闭）
- `priority`: 优先级（1-高 2-中 3-低）

### 4.2 创建工单

```bash
curl -X POST http://120.27.247.61:9080/api/ticket/add \
  -H "Authorization: Bearer {你的token}" \
  -H "Content-Type: application/json" \
  -d '{
    "projectId": 1,
    "title": "系统无法访问",
    "description": "客户反馈系统登录页面无法打开",
    "priority": 1,
    "reporterId": 1,
    "assigneeId": 1
  }'
```

**注意**: 工单编号会自动生成，SLA截止时间根据优先级自动计算：
- 高优先级：4小时
- 中优先级：8小时
- 低优先级：24小时

### 4.3 处理工单

```bash
curl -X PUT http://120.27.247.61:9080/api/ticket/resolve \
  -H "Authorization: Bearer {你的token}" \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "satisfaction": 5
  }'
```

### 4.4 更新工单

```bash
curl -X PUT http://120.27.247.61:9080/api/ticket/update \
  -H "Authorization: Bearer {你的token}" \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "status": 1,
    "assigneeId": 2
  }'
```

## 完整测试流程示例

```bash
# 1. 登录获取token
TOKEN=$(curl -s -X POST http://120.27.247.61:9080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' \
  | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

echo "Token: $TOKEN"

# 2. 获取用户列表
curl -X GET "http://120.27.247.61:9080/api/user/list?page=1&size=10" \
  -H "Authorization: Bearer $TOKEN"

# 3. 创建项目
curl -X POST http://120.27.247.61:9080/api/project/add \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "projectCode": "PRJ20240101001",
    "projectName": "测试项目",
    "customerId": 1,
    "startDate": "2024-01-01",
    "endDate": "2024-12-31",
    "status": 0
  }'

# 4. 创建工单
curl -X POST http://120.27.247.61:9080/api/ticket/add \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "projectId": 1,
    "title": "测试工单",
    "description": "这是一个测试工单",
    "priority": 2
  }'

# 5. 查看工单列表
curl -X GET "http://120.27.247.61:9080/api/ticket/list?page=1&size=10" \
  -H "Authorization: Bearer $TOKEN"
```

## 使用Postman测试

### 1. 导入环境变量

创建环境变量：
- `base_url`: `http://120.27.247.61:9080/api`
- `token`: 登录后获取的token

### 2. 设置请求头

在所有需要认证的接口中添加：
```
Authorization: Bearer {{token}}
Content-Type: application/json
```

### 3. 登录后自动保存Token

在登录接口的 `Tests` 标签中添加：
```javascript
pm.test("登录成功", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.code).to.eql(200);
    pm.environment.set("token", jsonData.data.token);
});
```

## 常见问题

### Q1: 返回401未授权？
A: 检查以下几点：
1. Token是否正确（Bearer 后面有空格）
2. Token是否已过期（24小时有效期）
3. 重新登录获取新token

### Q2: 返回400参数错误？
A: 检查请求体JSON格式是否正确，必填字段是否都提供

### Q3: 返回500服务器错误？
A: 查看服务器日志：`tail -f /var/log/delivery-management/app.log`

---

**测试愉快！** 🎉
