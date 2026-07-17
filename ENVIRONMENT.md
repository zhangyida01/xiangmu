# 🖥️ 服务器环境说明

## 实际部署环境

### 服务器信息
- **服务器IP**: 120.27.247.61
- **操作系统**: CentOS 7.9
- **SSH端口**: 22

### 软件版本
- **Java**: 1.8+
- **Maven**: 3.x
- **MySQL**: 5.7+
- **Node.js**: 16.x
- **npm**: 8.x
- **Nginx**: 1.x

### 数据库信息
- **MySQL地址**: 172.168.2.40:3306
- **数据库名**: delivery_management
- **用户名**: root
- **密码**: (需要配置)

### 端口占用
- **80**: Nginx (前端入口)
- **9080**: Spring Boot (后端API)
- **3306**: MySQL (数据库)

## 环境差异说明

### MySQL 5.7 vs 8.0

**主要区别：**
1. ✅ 认证插件不同（已兼容处理）
2. ✅ 默认字符集（已配置utf8mb4）
3. ✅ SQL模式更严格（已适配）

**已完成的适配：**
- SQL脚本已移除MySQL 8.0特有语法
- 连接字符串已兼容5.7
- 驱动版本已测试兼容

### Node.js 16.x vs 18.x

**主要区别：**
1. ✅ OpenSSL版本不同
2. ✅ 部分npm包兼容性

**已完成的适配：**
- package.json依赖版本已测试
- 部署脚本使用setup_16.x
- Vite配置兼容Node 16

## 版本兼容性矩阵

| 组件 | 最低版本 | 推荐版本 | 已测试版本 |
|------|---------|---------|-----------|
| Java | 1.8 | 1.8 | ✅ 1.8 |
| MySQL | 5.7 | 5.7 | ✅ 5.7 |
| Node.js | 16.0 | 16.20 | ✅ 16.x |
| Maven | 3.6 | 3.8 | ✅ 3.x |
| Nginx | 1.12 | 1.20 | ✅ 1.x |

## 部署注意事项

### 1. MySQL 5.7 配置

确保MySQL配置文件 `/etc/my.cnf` 包含：

```ini
[mysqld]
character-set-server=utf8mb4
collation-server=utf8mb4_unicode_ci
max_connections=200

[client]
default-character-set=utf8mb4
```

### 2. Java 1.8 限制

- ✅ Spring Boot 2.7.18 完全兼容
- ✅ 不支持Java 11+的新特性
- ✅ 使用Lambda表达式和Stream API

### 3. Node.js 16.x 限制

- ✅ Vue 3 完全支持
- ✅ Vite 4.x 兼容
- ⚠️ 某些新包可能需要Node 18+（本项目已测试）

### 4. CentOS 7.9 限制

- ✅ systemd 服务管理
- ✅ firewalld 防火墙
- ⚠️ 部分软件仓库可能需要EPEL

## 快速环境检查

在服务器上运行以下命令检查环境：

```bash
echo "=== 系统信息 ==="
cat /etc/centos-release
uname -r

echo -e "\n=== Java版本 ==="
java -version

echo -e "\n=== Maven版本 ==="
mvn -v

echo -e "\n=== MySQL版本 ==="
mysql --version

echo -e "\n=== Node.js版本 ==="
node -v

echo -e "\n=== npm版本 ==="
npm -v

echo -e "\n=== Nginx版本 ==="
nginx -v

echo -e "\n=== 端口监听 ==="
netstat -tunlp | grep -E ':80|:9080|:3306'

echo -e "\n=== 防火墙状态 ==="
firewall-cmd --list-ports
```

## 环境准备脚本

```bash
#!/bin/bash
# 一键环境检查和准备

echo "检查Java..."
if ! command -v java &> /dev/null; then
    echo "❌ Java未安装，请安装JDK 1.8"
    exit 1
fi

echo "检查Maven..."
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven未安装，请安装Maven 3.x"
    exit 1
fi

echo "检查MySQL..."
if ! command -v mysql &> /dev/null; then
    echo "❌ MySQL客户端未安装"
    exit 1
fi

echo "检查Node.js..."
if ! command -v node &> /dev/null; then
    echo "安装Node.js 16.x..."
    curl -fsSL https://rpm.nodesource.com/setup_16.x | sudo bash -
    sudo yum install -y nodejs
fi

echo "检查Nginx..."
if ! command -v nginx &> /dev/null; then
    echo "安装Nginx..."
    sudo yum install -y nginx
fi

echo "✅ 环境检查完成！"
```

## 升级建议

### 短期（可选）
- 保持当前版本稳定运行
- 监控系统性能和日志

### 中期（6-12个月）
- 考虑升级MySQL到8.0
- 考虑升级Node.js到18.x LTS
- 考虑迁移到Rocky Linux/AlmaLinux

### 长期（1-2年）
- 升级到Java 11或17 LTS
- 升级Spring Boot到3.x
- 容器化部署（Docker/K8s）

## 故障排查

### 常见问题

**Q1: MySQL 5.7连接报错？**
```bash
# 检查MySQL状态
systemctl status mysqld

# 检查MySQL错误日志
tail -f /var/log/mysqld.log

# 测试连接
mysql -h 172.168.2.40 -u root -p
```

**Q2: Node.js版本不对？**
```bash
# 卸载旧版本
sudo yum remove nodejs

# 安装Node.js 16.x
curl -fsSL https://rpm.nodesource.com/setup_16.x | sudo bash -
sudo yum install -y nodejs

# 验证版本
node -v
```

**Q3: Java版本冲突？**
```bash
# 查看当前Java版本
java -version

# 查看所有Java版本
alternatives --display java

# 切换Java版本
sudo alternatives --config java
```

---

**文档版本**: v1.0  
**更新日期**: 2026-07-17  
**适用系统**: CentOS 7.9
