# 🔧 数据库连接问题排查指南

## 错误现象

```
Communications link failure
Failed to obtain JDBC Connection
```

## 可能的原因和解决方案

### 1️⃣ 数据库未启动

**检查：**
```bash
# 如果MySQL在本地
systemctl status mysqld

# 如果MySQL在远程（172.168.2.40）
ssh root@172.168.2.40 "systemctl status mysqld"
```

**解决：**
```bash
systemctl start mysqld
systemctl enable mysqld
```

---

### 2️⃣ 数据库密码错误

**检查配置文件：**
```bash
cat src/main/resources/application.yml | grep -A 3 datasource
```

**修改密码：**
```bash
vi src/main/resources/application.yml
# 修改第9行：password: 你的实际密码
```

**重新编译部署：**
```bash
mvn clean package -DskipTests
systemctl restart delivery-management
```

---

### 3️⃣ 数据库未创建或表未初始化

**手动测试连接：**
```bash
mysql -h 172.168.2.40 -u root -p
```

**执行初始化SQL：**
```sql
-- 如果数据库不存在，执行：
mysql -h 172.168.2.40 -u root -p < docs/sql/init.sql

-- 或者手动执行：
USE delivery_management;
SHOW TABLES;
```

**如果数据库不存在：**
```sql
CREATE DATABASE delivery_management DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

---

### 4️⃣ 网络不通或防火墙阻止

**测试网络连通性：**
```bash
# Ping测试
ping 172.168.2.40

# 端口测试
telnet 172.168.2.40 3306
# 或者
nc -zv 172.168.2.40 3306
```

**如果网络不通，检查：**
```bash
# 1. 检查MySQL服务器防火墙
# 在172.168.2.40上执行：
firewall-cmd --permanent --add-port=3306/tcp
firewall-cmd --reload

# 2. 检查应用服务器防火墙
firewall-cmd --list-all
```

---

### 5️⃣ MySQL远程访问未授权

**MySQL需要允许远程连接：**

在MySQL服务器（172.168.2.40）上执行：

```sql
-- 1. 登录MySQL
mysql -u root -p

-- 2. 检查用户授权
SELECT host, user FROM mysql.user WHERE user='root';

-- 3. 如果只有localhost，需要授权远程访问
GRANT ALL PRIVILEGES ON *.* TO 'root'@'120.27.247.61' IDENTIFIED BY '你的密码';
-- 或者允许所有IP（不太安全）
GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' IDENTIFIED BY '你的密码';

-- 4. 刷新权限
FLUSH PRIVILEGES;
```

**修改MySQL配置允许远程连接：**
```bash
# 编辑MySQL配置文件
vi /etc/my.cnf

# 注释掉或修改这一行（如果存在）：
# bind-address = 127.0.0.1

# 重启MySQL
systemctl restart mysqld
```

---

### 6️⃣ 如果MySQL在本地（127.0.0.1或localhost）

**修改配置文件：**
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/delivery_management?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false
    username: root
    password: 你的密码
```

---

## 🔍 完整排查步骤

### 步骤1：确认数据库位置

```bash
# MySQL在本地还是远程？
# 本地：localhost 或 127.0.0.1
# 远程：172.168.2.40

# 检查MySQL进程
ps aux | grep mysqld
```

### 步骤2：测试手动连接

```bash
# 测试连接（按实际情况修改）
mysql -h 172.168.2.40 -u root -p

# 输入密码后，如果成功，执行：
SHOW DATABASES;
USE delivery_management;
SHOW TABLES;
SELECT * FROM sys_user;
```

### 步骤3：检查配置文件

```bash
cd /root/xiangmu/java
cat src/main/resources/application.yml | grep -A 5 datasource
```

**正确的配置示例：**
```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://172.168.2.40:3306/delivery_management?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false
    username: root
    password: 你的实际密码
```

### 步骤4：初始化数据库（如果未初始化）

```bash
# 执行初始化脚本
mysql -h 172.168.2.40 -u root -p < docs/sql/init.sql

# 验证
mysql -h 172.168.2.40 -u root -p -e "USE delivery_management; SHOW TABLES;"
```

### 步骤5：重新部署

```bash
# 修改配置后重新编译
mvn clean package -DskipTests

# 重启服务
systemctl restart delivery-management

# 查看日志
tail -f /var/log/delivery-management/app.log
```

### 步骤6：验证连接

```bash
# 测试登录接口
curl -X POST http://localhost:9080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

---

## 🎯 快速解决方案（最常见）

### 方案A：数据库密码错误

```bash
# 1. 修改密码
vi src/main/resources/application.yml

# 2. 重新编译
mvn clean package -DskipTests

# 3. 重启
systemctl restart delivery-management
```

### 方案B：数据库未初始化

```bash
# 初始化数据库
mysql -h 172.168.2.40 -u root -p < docs/sql/init.sql
```

### 方案C：MySQL未授权远程访问

```sql
-- 在MySQL服务器上执行
mysql -u root -p
GRANT ALL PRIVILEGES ON *.* TO 'root'@'120.27.247.61' IDENTIFIED BY '你的密码';
FLUSH PRIVILEGES;
```

---

## 📝 检查清单

- [ ] MySQL服务已启动
- [ ] 数据库密码正确
- [ ] 数据库已创建（delivery_management）
- [ ] 表已初始化（14张表）
- [ ] 网络连通（可以ping通）
- [ ] 端口开放（3306）
- [ ] MySQL允许远程连接
- [ ] 配置文件正确
- [ ] 应用已重启

---

## 💡 获取更多信息

如果以上方案都不行，提供以下信息：

```bash
# 1. 查看应用日志
tail -100 /var/log/delivery-management/app.log

# 2. 查看MySQL位置
ps aux | grep mysqld

# 3. 查看配置
cat src/main/resources/application.yml | grep -A 5 datasource

# 4. 测试连接
mysql -h 172.168.2.40 -u root -p -e "SELECT 1"
```

把这些信息发给我，我能更准确地帮你解决！
