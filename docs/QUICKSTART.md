# 快速入门指南

## 项目概述

这是一个轻量级的**交付项目管理系统**，专为5人以内的小团队设计。系统涵盖从项目立项到售后服务的完整流程。

## 技术栈

- **后端**: Spring Boot 2.7.18 + MyBatis-Plus 3.5.5
- **数据库**: MySQL 8.0（172.168.2.40）
- **认证**: Spring Security + JWT
- **部署**: CentOS 7.9

## 核心功能

1. ✅ 组织与用户管理
2. ✅ 角色与权限管理
3. ✅ 安全与访问控制
4. ✅ 交付项目台账与立项
5. ✅ 交付物与文档管理
6. ✅ 交付计划与范围管理（WBS、里程碑、甘特图）
7. ✅ 问题与变更管理
8. ✅ 客户协同与验收确认
9. ✅ 资源与工时管理
10. ✅ 进度与绩效看板
11. ✅ 项目售后管理（工单、SLA）

## 5分钟快速启动

### 第一步：数据库初始化

\\\ash
# 登录你的MySQL服务器
mysql -h 172.168.2.40 -u root -p

# 执行初始化脚本（会自动创建数据库和表）
source docs/sql/init.sql
\\\

### 第二步：修改配置

编辑 \src/main/resources/application.yml\，修改第9行的数据库密码：

\\\yaml
password: 你的实际密码  # 修改这里
\\\

### 第三步：编译打包

\\\ash
# Windows环境下执行
mvn clean package -DskipTests
\\\

### 第四步：部署到CentOS

\\\ash
# 1. 上传整个项目到服务器
scp -r . root@你的服务器IP:/tmp/delivery-management

# 2. SSH登录服务器
ssh root@你的服务器IP

# 3. 执行自动部署脚本
cd /tmp/delivery-management
chmod +x deploy.sh
./deploy.sh
\\\

### 第五步：验证

\\\ash
# 检查服务状态
systemctl status delivery-management

# 测试接口
curl http://localhost:8080/api/health

# 预期返回
{"code":200,"message":"操作成功","data":"系统运行正常"}
\\\

## 默认账号

- 👤 用户名: \dmin\
- 🔐 密码: \dmin123\

**⚠️ 首次登录后请立即修改密码！**

## 项目结构

\\\
delivery-management/
├── docs/                           # 📚 文档
│   ├── sql/init.sql                #    数据库初始化脚本
│   ├── API.md                      #    API接口文档
│   └── DEPLOY.md                   #    详细部署文档
├── src/main/
│   ├── java/com/delivery/management/
│   │   ├── common/                 # 🔧 公共类
│   │   ├── config/                 # ⚙️  配置类
│   │   ├── controller/             # 🎮 控制器（待实现）
│   │   ├── entity/                 # 📦 实体类（已完成）
│   │   ├── mapper/                 # 🗄️  数据访问层
│   │   └── service/                # 💼 业务逻辑层（待实现）
│   └── resources/
│       └── application.yml         # ⚙️  配置文件
├── deploy.sh                       # 🚀 自动部署脚本
├── pom.xml                         # 📦 Maven配置
└── README.md                       # 📖 项目说明
\\\

## 开发进度

### ✅ 已完成
- [x] 项目骨架搭建
- [x] 数据库设计（11个模块，20+张表）
- [x] 实体类定义
- [x] Mapper接口
- [x] Spring Security配置
- [x] 统一响应格式
- [x] 自动部署脚本
- [x] 完整文档

### 🚧 待开发（后续工作）
- [ ] 用户认证与JWT实现
- [ ] Service业务逻辑层
- [ ] Controller接口实现
- [ ] 文件上传下载
- [ ] 邮件通知功能
- [ ] 前端界面开发

## 数据库表说明

| 表名 | 说明 | 模块 |
|------|------|------|
| sys_user | 用户表 | 用户管理 |
| sys_department | 部门表 | 组织管理 |
| sys_role | 角色表 | 权限管理 |
| sys_user_role | 用户角色关联 | 权限管理 |
| customer | 客户表 | 客户管理 |
| delivery_project | 项目表 | 项目管理 |
| project_document | 文档表 | 文档管理 |
| project_milestone | 里程碑表 | 计划管理 |
| project_task | 任务表（WBS） | 任务管理 |
| project_issue | 问题表 | 问题管理 |
| project_change | 变更表 | 变更管理 |
| project_acceptance | 验收表 | 验收管理 |
| project_worklog | 工时记录表 | 工时管理 |
| service_ticket | 工单表 | 售后管理 |

## 常用命令

### 服务管理
\\\ash
# 启动服务
systemctl start delivery-management

# 停止服务
systemctl stop delivery-management

# 重启服务
systemctl restart delivery-management

# 查看状态
systemctl status delivery-management

# 查看日志
tail -f /var/log/delivery-management/app.log
\\\

### Maven命令
\\\ash
# 编译
mvn compile

# 打包
mvn package

# 清理
mvn clean

# 跳过测试打包
mvn package -DskipTests
\\\

## 配置说明

### 端口配置
默认端口：**8080**

修改端口：编辑 \pplication.yml\ 的 \server.port\

### 数据库配置
\\\yaml
spring:
  datasource:
    url: jdbc:mysql://172.168.2.40:3306/delivery_management
    username: root
    password: 修改为你的密码
\\\

### JWT配置
\\\yaml
jwt:
  secret: delivery-management-secret-key-2024  # 生产环境请修改
  expiration: 86400000  # 24小时（毫秒）
\\\

## 下一步做什么？

### 如果你是开发者：
1. 📖 阅读 \docs/API.md\ 了解接口设计
2. 💻 实现 Service 和 Controller 层
3. 🧪 编写单元测试
4. 🎨 开发前端界面（Vue/React）

### 如果你是运维人员：
1. 📖 阅读 \docs/DEPLOY.md\ 了解详细部署
2. 🔒 配置防火墙和安全策略
3. 🔄 设置数据库定期备份
4. 📊 配置监控和告警

### 如果你是项目经理：
1. 🎯 根据实际需求调整功能模块
2. 👥 分配开发任务
3. ⏱️  制定开发计划
4. ✅ 组织需求评审

## 常见问题

### Q1: 如何修改管理员密码？
A: 初始密码是 \dmin123\，登录后通过用户管理功能修改。密码使用 BCrypt 加密。

### Q2: 数据库连接失败怎么办？
A: 检查以下几点：
- 数据库地址是否正确（172.168.2.40）
- 用户名密码是否正确
- 网络是否连通（ping 172.168.2.40）
- MySQL是否允许远程连接

### Q3: 如何查看详细日志？
A: \	ail -f /var/log/delivery-management/app.log\

### Q4: 如何重新部署？
A: 
\\\ash
systemctl stop delivery-management
# 上传新的jar包
systemctl start delivery-management
\\\

### Q5: 支持多少用户？
A: 当前设计适合5人以内的小团队。如需支持更多用户，建议优化数据库索引和增加服务器配置。

## 技术支持

如有问题，请查看：
- 📖 \README.md\ - 项目说明
- 📖 \docs/API.md\ - API文档
- 📖 \docs/DEPLOY.md\ - 部署文档

## 许可证

本项目仅供学习和内部使用。

---

**祝你使用愉快！** 🎉
