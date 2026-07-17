# ⚡ 快速部署指南 - CentOS 7.9

> 针对你的实际环境：MySQL 5.7 + Node.js 16.x + Java 1.8

## 📋 部署前检查

在服务器上运行环境检查：

```bash
# 检查所有环境
echo "Java: $(java -version 2>&1 | head -1)"
echo "Maven: $(mvn -v | head -1)"
echo "MySQL: $(mysql --version)"
echo "Node.js: $(node -v 2>/dev/null || echo '未安装')"
echo "Nginx: $(nginx -v 2>&1 || echo '未安装')"
```

---

## 🚀 一键部署（推荐）

### 步骤1：拉取代码

```bash
cd /root/xiangmu/java
git pull origin master
```

### 步骤2：部署后端

```bash
# 修改数据库密码
vi src/main/resources/application.yml
# 第9行改为你的实际密码

# 编译打包
mvn clean package -DskipTests

# 重启服务
systemctl restart delivery-management

# 验证后端
curl http://localhost:9080/api/health
```

### 步骤3：部署前端（自动化）

```bash
# 进入前端目录
cd frontend

# 赋予执行权限
chmod +x deploy.sh

# 一键部署
./deploy.sh
```

**脚本会自动：**
- ✅ 安装Node.js 16.x
- ✅ 安装npm依赖
- ✅ 构建生产版本
- ✅ 安装并配置Nginx
- ✅ 部署静态文件
- ✅ 配置防火墙

---

## 🎯 部署完成验证

### 1. 检查服务状态

```bash
# 后端服务
systemctl status delivery-management

# Nginx
systemctl status nginx

# 端口监听
netstat -tunlp | grep -E ':80|:9080'
```

### 2. 浏览器访问

打开浏览器：**http://120.27.247.61**

**默认账号：**
- 用户名：`admin`
- 密码：`admin123`

### 3. 功能测试

- ✅ 登录系统
- ✅ 查看首页统计
- ✅ 用户管理
- ✅ 项目管理
- ✅ 工单管理

---

## 🔧 手动部署（备选方案）

如果自动脚本失败，可以手动执行：

### 安装Node.js 16.x

```bash
curl -fsSL https://rpm.nodesource.com/setup_16.x | sudo bash -
sudo yum install -y nodejs
node -v  # 验证版本
```

### 构建前端

```bash
cd /root/xiangmu/java/frontend
npm install
npm run build
```

### 部署到Nginx

```bash
# 创建目录
sudo mkdir -p /opt/delivery-frontend

# 复制文件
sudo cp -r dist /opt/delivery-frontend/

# 安装Nginx
sudo yum install -y nginx

# 复制配置
sudo cp nginx.conf /etc/nginx/conf.d/delivery.conf

# 测试配置
sudo nginx -t

# 启动Nginx
sudo systemctl start nginx
sudo systemctl enable nginx

# 开放端口
sudo firewall-cmd --permanent --add-port=80/tcp
sudo firewall-cmd --reload
```

---

## ⚠️ 常见问题

### Q1: 后端启动失败？

```bash
# 查看日志
tail -100 /var/log/delivery-management/app.log

# 常见原因：
# 1. 数据库连接失败 -> 检查密码配置
# 2. 端口被占用 -> netstat -tunlp | grep 9080
# 3. Java版本不对 -> java -version
```

### Q2: 前端访问404？

```bash
# 检查Nginx配置
sudo nginx -t

# 检查静态文件
ls -la /opt/delivery-frontend/dist/

# 查看Nginx日志
sudo tail -f /var/log/nginx/delivery-error.log

# 重启Nginx
sudo systemctl restart nginx
```

### Q3: Node.js安装失败？

```bash
# 清理缓存
sudo yum clean all

# 手动下载安装
wget https://nodejs.org/dist/v16.20.2/node-v16.20.2-linux-x64.tar.xz
tar xf node-v16.20.2-linux-x64.tar.xz
sudo mv node-v16.20.2-linux-x64 /usr/local/nodejs
echo 'export PATH=/usr/local/nodejs/bin:$PATH' >> ~/.bashrc
source ~/.bashrc
```

### Q4: MySQL连接失败？

```bash
# 测试连接
mysql -h 172.168.2.40 -u root -p

# 检查防火墙
sudo firewall-cmd --list-all

# 检查MySQL状态
systemctl status mysqld
```

---

## 📊 部署架构图

```
                    Internet
                       ↓
                   [80端口]
                       ↓
                  ┌─────────┐
                  │  Nginx  │
                  │ 反向代理 │
                  └─────────┘
                 /           \
        静态文件 /             \ API代理
               ↓               ↓
    ┌─────────────────┐   [9080端口]
    │ /opt/dist/      │   ┌──────────────┐
    │ Vue前端文件     │   │ Spring Boot  │
    └─────────────────┘   └──────────────┘
                               ↓
                          [172.168.2.40:3306]
                          ┌──────────────┐
                          │ MySQL 5.7    │
                          └──────────────┘
```

---

## 🔄 更新部署

代码更新后重新部署：

```bash
cd /root/xiangmu/java

# 拉取代码
git pull

# 后端
mvn clean package -DskipTests
systemctl restart delivery-management

# 前端
cd frontend
npm install
npm run build
sudo cp -r dist /opt/delivery-frontend/
sudo systemctl restart nginx
```

---

## 📞 支持

部署遇到问题？

1. 查看日志：
   - 后端：`/var/log/delivery-management/app.log`
   - Nginx：`/var/log/nginx/delivery-error.log`

2. 环境检查：参考 `ENVIRONMENT.md`

3. 完整文档：参考 `DEPLOYMENT.md`

---

## ✅ 部署完成清单

- [ ] 后端服务启动成功
- [ ] 健康检查接口正常
- [ ] Nginx服务运行正常
- [ ] 浏览器可以访问
- [ ] 登录功能正常
- [ ] 所有页面可以访问
- [ ] API接口调用成功

**全部完成后，系统即可正常使用！** 🎉

---

**版本**: v1.1.0  
**环境**: CentOS 7.9 + MySQL 5.7 + Node.js 16.x  
**更新**: 2026-07-17
