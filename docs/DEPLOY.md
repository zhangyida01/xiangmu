# 部署说明

## 前置准备

### 1. 数据库准备
在你的 MySQL 服务器（172.168.2.40）上执行初始化脚本：

\\\ash
# 登录 MySQL
mysql -h 172.168.2.40 -u root -p

# 执行初始化脚本
source docs/sql/init.sql

# 或者直接导入
mysql -h 172.168.2.40 -u root -p < docs/sql/init.sql
\\\

### 2. 修改数据库配置
编辑 \src/main/resources/application.yml\，修改数据库密码：

\\\yaml
spring:
  datasource:
    password: 你的实际密码
\\\

### 3. 本地编译打包
在 Windows 开发机上执行：

\\\ash
# 确保已安装 Maven
mvn -version

# 清理并打包
mvn clean package -DskipTests

# 打包后的文件位置
# target/delivery-management.jar
\\\

## CentOS 7.9 部署步骤

### 方式一：自动部署（推荐）

\\\ash
# 1. 上传项目到服务器
scp -r delivery-management/ root@服务器IP:/tmp/

# 2. SSH 登录服务器
ssh root@服务器IP

# 3. 进入项目目录
cd /tmp/delivery-management

# 4. 给脚本执行权限
chmod +x deploy.sh

# 5. 执行部署
./deploy.sh
\\\

### 方式二：手动部署

\\\ash
# 1. 创建目录
mkdir -p /opt/delivery-management
mkdir -p /var/log/delivery-management

# 2. 上传 jar 包
scp target/delivery-management.jar root@服务器IP:/opt/delivery-management/

# 3. 创建 systemd 服务文件
cat > /etc/systemd/system/delivery-management.service <<EOF
[Unit]
Description=Delivery Project Management System
After=network.target

[Service]
Type=simple
User=root
WorkingDirectory=/opt/delivery-management
ExecStart=/usr/bin/java -Xms512m -Xmx1024m -jar /opt/delivery-management/delivery-management.jar
StandardOutput=append:/var/log/delivery-management/app.log
StandardError=append:/var/log/delivery-management/error.log
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
EOF

# 4. 重载 systemd
systemctl daemon-reload

# 5. 启动服务
systemctl start delivery-management

# 6. 设置开机自启
systemctl enable delivery-management

# 7. 查看状态
systemctl status delivery-management
\\\

## 验证部署

### 1. 检查服务状态
\\\ash
systemctl status delivery-management
\\\

### 2. 查看日志
\\\ash
# 实时查看应用日志
tail -f /var/log/delivery-management/app.log

# 查看错误日志
tail -f /var/log/delivery-management/error.log

# 查看 systemd 日志
journalctl -u delivery-management -f
\\\

### 3. 测试接口
\\\ash
# 健康检查
curl http://localhost:8080/api/health

# 预期返回
{"code":200,"message":"操作成功","data":"系统运行正常"}
\\\

### 4. 开放防火墙端口
\\\ash
# 查看防火墙状态
firewall-cmd --state

# 开放 8080 端口
firewall-cmd --permanent --add-port=8080/tcp

# 重载防火墙
firewall-cmd --reload

# 查看已开放端口
firewall-cmd --list-ports
\\\

## 常用运维命令

\\\ash
# 启动服务
systemctl start delivery-management

# 停止服务
systemctl stop delivery-management

# 重启服务
systemctl restart delivery-management

# 查看状态
systemctl status delivery-management

# 查看日志（最后50行）
journalctl -u delivery-management -n 50

# 实时查看日志
journalctl -u delivery-management -f
\\\

## 故障排查

### 问题1：服务无法启动
\\\ash
# 查看详细日志
journalctl -u delivery-management -n 100

# 检查 Java 是否安装
java -version

# 检查 jar 包是否存在
ls -lh /opt/delivery-management/delivery-management.jar
\\\

### 问题2：无法连接数据库
- 检查数据库地址和端口是否正确
- 确认数据库用户名和密码
- 测试网络连通性：\ping 172.168.2.40\
- 检查 MySQL 是否允许远程连接

### 问题3：端口被占用
\\\ash
# 查看端口占用
netstat -tunlp | grep 8080

# 或使用 lsof
lsof -i:8080
\\\

## 更新部署

\\\ash
# 1. 停止服务
systemctl stop delivery-management

# 2. 备份旧版本
cp /opt/delivery-management/delivery-management.jar /opt/delivery-management/delivery-management.jar.bak

# 3. 上传新版本
scp target/delivery-management.jar root@服务器IP:/opt/delivery-management/

# 4. 启动服务
systemctl start delivery-management

# 5. 查看状态
systemctl status delivery-management
\\\

## 性能优化建议

### JVM 参数调优
编辑 \/etc/systemd/system/delivery-management.service\：

\\\ini
ExecStart=/usr/bin/java -Xms1024m -Xmx2048m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -jar /opt/delivery-management/delivery-management.jar
\\\

重载配置：
\\\ash
systemctl daemon-reload
systemctl restart delivery-management
\\\

## 安全建议

1. **修改默认密码**：首次登录后立即修改 admin 账户密码
2. **数据库安全**：使用独立的数据库账户，不要使用 root
3. **防火墙配置**：仅开放必要端口
4. **HTTPS**：生产环境建议配置 Nginx 反向代理并启用 HTTPS
5. **定期备份**：定期备份数据库和配置文件
