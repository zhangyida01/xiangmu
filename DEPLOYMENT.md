# 🚀 完整部署指南 - 前后端分离架构

## 系统架构

```
┌─────────────┐         ┌──────────────┐         ┌──────────────┐
│   浏览器    │  HTTP   │    Nginx     │  HTTP   │   后端API    │
│             │ ──────> │   (80端口)   │ ──────> │  (9080端口)  │
│  Vue前端    │         │ 反向代理     │         │  Spring Boot │
└─────────────┘         └──────────────┘         └──────────────┘
                              │
                              │ 静态文件
                              ▼
                        ┌──────────────┐
                        │  /opt/dist/  │
                        │   前端文件   │
                        └──────────────┘
```

## 部署前准备

### 服务器信息
- **服务器IP**: 120.27.247.61
- **操作系统**: CentOS 7.9
- **Java版本**: 1.8+
- **MySQL版本**: 5.7+
- **Node.js版本**: 16.x

### 端口规划
- **80**: Nginx (前端入口)
- **9080**: Spring Boot (后端API)
- **3306**: MySQL

## 一、后端部署

### 1. 更新后端代码

```bash
# SSH登录服务器
ssh root@120.27.247.61

# 进入项目目录
cd /root/xiangmu/java

# 拉取最新代码
git pull origin master

# 修改数据库密码
vi src/main/resources/application.yml
# 修改第9行: password: 你的实际密码
```

### 2. 编译打包

```bash
# 清理并打包
mvn clean package -DskipTests

# 验证jar包
ls -lh target/delivery-management.jar
```

### 3. 启动后端服务

```bash
# 停止旧服务
systemctl stop delivery-management

# 启动新服务
systemctl start delivery-management

# 查看状态
systemctl status delivery-management

# 查看日志
tail -f /var/log/delivery-management/app.log
```

### 4. 验证后端

```bash
# 测试健康检查
curl http://localhost:9080/api/health

# 测试登录接口
curl -X POST http://localhost:9080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

## 二、前端部署

### 方式1：自动部署脚本（推荐）

```bash
# 上传前端代码到服务器
cd /root/xiangmu/java
git pull origin master

# 进入前端目录
cd frontend

# 赋予执行权限
chmod +x deploy.sh

# 执行部署脚本
./deploy.sh
```

脚本会自动完成：
1. 安装Node.js和npm
2. 安装前端依赖
3. 构建生产版本
4. 安装并配置Nginx
5. 部署静态文件
6. 配置防火墙

### 方式2：手动部署

#### 步骤1：安装Node.js

```bash
# 安装Node.js 18.x
curl -fsSL https://rpm.nodesource.com/setup_16.x | sudo bash -
sudo yum install -y nodejs

# 验证安装
node -v
npm -v
```

#### 步骤2：构建前端

```bash
cd /root/xiangmu/java/frontend

# 安装依赖
npm install

# 构建生产版本
npm run build

# 验证构建结果
ls -lh dist/
```

#### 步骤3：部署到Nginx

```bash
# 创建部署目录
sudo mkdir -p /opt/delivery-frontend

# 复制构建文件
sudo cp -r dist /opt/delivery-frontend/

# 安装Nginx
sudo yum install -y nginx

# 复制Nginx配置
sudo cp nginx.conf /etc/nginx/conf.d/delivery.conf

# 测试配置
sudo nginx -t

# 启动Nginx
sudo systemctl start nginx
sudo systemctl enable nginx
```

#### 步骤4：配置防火墙

```bash
# 开放80端口
sudo firewall-cmd --permanent --add-port=80/tcp
sudo firewall-cmd --reload

# 查看开放的端口
sudo firewall-cmd --list-ports
```

## 三、验证部署

### 1. 检查服务状态

```bash
# 后端服务
systemctl status delivery-management

# Nginx服务
systemctl status nginx

# 端口监听
netstat -tunlp | grep -E '9080|80'
```

### 2. 浏览器访问

打开浏览器访问：**http://120.27.247.61**

**默认账号：**
- 用户名：admin
- 密码：admin123

### 3. 功能测试

- ✅ 用户登录
- ✅ 查看首页统计
- ✅ 用户管理（增删改查）
- ✅ 项目管理（增删改查）
- ✅ 工单管理（增删改查）

## 四、故障排查

### 前端访问404

**原因**: Nginx未正确配置或静态文件路径错误

**解决**:
```bash
# 检查Nginx配置
sudo nginx -t

# 检查静态文件
ls -la /opt/delivery-frontend/dist/

# 查看Nginx错误日志
sudo tail -f /var/log/nginx/delivery-error.log

# 重启Nginx
sudo systemctl restart nginx
```

### API接口403/401

**原因**: 后端服务未启动或Token过期

**解决**:
```bash
# 检查后端服务
systemctl status delivery-management

# 查看后端日志
tail -100 /var/log/delivery-management/app.log

# 重启后端
systemctl restart delivery-management
```

### 跨域问题

**原因**: Nginx反向代理配置错误

**解决**: 检查 `/etc/nginx/conf.d/delivery.conf` 中的 `proxy_pass` 配置

### 页面刷新404

**原因**: Vue Router使用history模式，Nginx未配置 `try_files`

**解决**: 确保Nginx配置包含：
```nginx
location / {
    try_files $uri $uri/ /index.html;
}
```

## 五、日常维护

### 重启服务

```bash
# 重启后端
systemctl restart delivery-management

# 重启Nginx
systemctl restart nginx

# 重启MySQL
systemctl restart mysqld
```

### 查看日志

```bash
# 后端日志
tail -f /var/log/delivery-management/app.log

# Nginx访问日志
tail -f /var/log/nginx/delivery-access.log

# Nginx错误日志
tail -f /var/log/nginx/delivery-error.log
```

### 更新代码

```bash
# 拉取最新代码
cd /root/xiangmu/java
git pull origin master

# 重新部署后端
mvn clean package -DskipTests
systemctl restart delivery-management

# 重新部署前端
cd frontend
npm install
npm run build
sudo cp -r dist /opt/delivery-frontend/
sudo systemctl restart nginx
```

## 六、安全加固（可选）

### 1. 配置HTTPS

```bash
# 安装certbot
sudo yum install -y certbot python-certbot-nginx

# 申请证书
sudo certbot --nginx -d yourdomain.com

# 自动续期
sudo certbot renew --dry-run
```

### 2. 修改默认密码

登录系统后立即修改管理员密码。

### 3. 配置数据库权限

```sql
-- 创建专用数据库用户
CREATE USER 'delivery_user'@'localhost' IDENTIFIED BY 'strong_password';
GRANT ALL PRIVILEGES ON delivery_management.* TO 'delivery_user'@'localhost';
FLUSH PRIVILEGES;
```

### 4. 限制API访问速率

在Nginx配置中添加：
```nginx
limit_req_zone $binary_remote_addr zone=api_limit:10m rate=10r/s;

location /api/ {
    limit_req zone=api_limit burst=20;
    # ... 其他配置
}
```

## 七、性能优化

### 1. 前端优化

- ✅ Nginx开启gzip压缩
- ✅ 静态资源缓存（7天）
- ✅ 懒加载路由
- ✅ 图片懒加载

### 2. 后端优化

```yaml
# application.yml
server:
  tomcat:
    max-threads: 200
    min-spare-threads: 10
    
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
```

### 3. 数据库优化

```sql
-- 添加索引
CREATE INDEX idx_project_status ON delivery_project(status);
CREATE INDEX idx_ticket_status ON service_ticket(status);
CREATE INDEX idx_user_username ON sys_user(username);
```

## 八、备份策略

### 数据库备份

```bash
# 创建备份脚本
cat > /root/backup_db.sh << 'EOF'
#!/bin/bash
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/root/backup"
mkdir -p $BACKUP_DIR
mysqldump -h 172.168.2.40 -u root -p密码 delivery_management > $BACKUP_DIR/db_$DATE.sql
find $BACKUP_DIR -name "db_*.sql" -mtime +7 -delete
EOF

chmod +x /root/backup_db.sh

# 添加定时任务（每天凌晨2点）
crontab -e
0 2 * * * /root/backup_db.sh
```

## 九、监控告警（可选）

### 使用Prometheus + Grafana

1. 后端添加Actuator依赖
2. 配置Prometheus抓取
3. Grafana展示监控数据

---

## 📞 技术支持

如有问题，查看日志或联系开发团队。

**系统版本**: v1.1.0  
**更新时间**: 2026-07-17
