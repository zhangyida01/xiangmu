# 交付项目管理系统

## 项目简介
轻量级交付项目管理系统，适用于5人以内的小团队，涵盖项目立项、交付物管理、问题跟踪、验收确认、工时管理、售后服务等核心功能。

## 技术栈
- **后端**: Spring Boot 2.7.18 + MyBatis-Plus + Spring Security + JWT
- **数据库**: MySQL 8.0
- **前端**: 前后端分离（需单独开发）

## 功能模块

### 1. 组织与用户管理
- 用户账号管理
- 部门组织架构
- 用户档案信息

### 2. 角色与权限管理
- 角色定制（管理员/项目经理/成员）
- 功能权限控制
- 数据范围隔离

### 3. 安全与访问控制
- JWT Token 认证
- 密码加密存储
- 会话管理

### 4. 交付项目台账与立项
- 项目创建与编号
- 合同关联管理
- 客户信息维护

### 5. 交付物与文档管理
- 文档库管理
- 版本控制
- 文件上传下载

### 6. 交付计划与范围管理
- WBS 任务分解
- 里程碑管理
- 甘特图数据支持

### 7. 问题与变更管理
- 问题流转跟踪
- 变更审批流程
- 风险管理

### 8. 客户协同与验收确认
- 验收单签署
- 培训记录
- 沟通归档

### 9. 资源与工时管理
- 团队成员指派
- 工时填报
- 资源负载统计

### 10. 进度与绩效看板
- 项目健康度仪表盘
- 里程碑达成率
- 问题分布统计

### 11. 项目售后管理
- 维保项目管理
- 工单提交与流转
- SLA 计时
- 满意度评价

## 项目结构
\\\
delivery-management/
├── docs/                          # 文档目录
│   └── sql/                       # SQL 脚本
│       └── init.sql               # 数据库初始化脚本
├── src/
│   ├── main/
│   │   ├── java/com/delivery/management/
│   │   │   ├── common/            # 公共类
│   │   │   │   ├── BaseEntity.java
│   │   │   │   └── Result.java
│   │   │   ├── config/            # 配置类
│   │   │   │   ├── MyMetaObjectHandler.java
│   │   │   │   └── SecurityConfig.java
│   │   │   ├── controller/        # 控制器
│   │   │   ├── entity/            # 实体类
│   │   │   ├── mapper/            # Mapper 接口
│   │   │   └── service/           # 服务层
│   │   └── resources/
│   │       ├── mapper/            # MyBatis XML
│   │       └── application.yml    # 配置文件
│   └── test/
└── pom.xml
\\\

## 快速开始

### 1. 数据库初始化
在你的 MySQL 服务器（172.168.2.40）上执行：
\\\ash
mysql -h 172.168.2.40 -u root -p < docs/sql/init.sql
\\\

### 2. 修改配置
编辑 \src/main/resources/application.yml\，修改数据库密码：
\\\yaml
spring:
  datasource:
    url: jdbc:mysql://172.168.2.40:3306/delivery_management
    username: root
    password: 你的数据库密码
\\\

### 3. 编译打包
\\\ash
mvn clean package -DskipTests
\\\

### 4. 部署到 CentOS 7.9
\\\ash
# 上传 jar 包到服务器
scp target/delivery-management.jar root@your-server:/opt/

# SSH 登录服务器
ssh root@your-server

# 启动应用
nohup java -jar /opt/delivery-management.jar > /opt/app.log 2>&1 &
\\\

### 5. 使用 systemd 管理服务
创建 \/etc/systemd/system/delivery-management.service\：
\\\ini
[Unit]
Description=Delivery Management System
After=network.target

[Service]
Type=simple
User=root
ExecStart=/usr/bin/java -jar /opt/delivery-management.jar
Restart=on-failure

[Install]
WantedBy=multi-user.target
\\\

启动服务：
\\\ash
systemctl daemon-reload
systemctl start delivery-management
systemctl enable delivery-management
systemctl status delivery-management
\\\

## API 接口

### 认证接口
- POST \/api/auth/login\ - 用户登录
- POST \/api/auth/logout\ - 用户登出

### 用户管理
- GET \/api/user/list\ - 用户列表
- POST \/api/user/add\ - 添加用户
- PUT \/api/user/update\ - 更新用户
- DELETE \/api/user/delete/{id}\ - 删除用户

### 项目管理
- GET \/api/project/list\ - 项目列表
- POST \/api/project/add\ - 创建项目
- PUT \/api/project/update\ - 更新项目
- DELETE \/api/project/delete/{id}\ - 删除项目

## 默认账号
- 用户名: \dmin\
- 密码: \dmin123\

## 注意事项
1. 首次使用请修改默认管理员密码
2. 数据库需支持 utf8mb4 字符集
3. 服务器需安装 JDK 1.8+
4. 建议配置防火墙规则，仅开放必要端口

## 后续开发建议
- 实现 JWT 拦截器
- 完善业务逻辑层（Service）
- 开发前端界面（Vue/React）
- 添加文件上传功能
- 实现工作流引擎
- 集成邮件/短信通知

## 联系方式
如有问题，请联系项目管理员。
