#!/bin/bash

# 交付项目管理系统部署脚本（CentOS 7.9）

# 配置变量
APP_NAME="delivery-management"
APP_VERSION="1.0.0"
APP_JAR=".jar"
DEPLOY_DIR="/opt/"
LOG_DIR="/var/log/"
JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC"

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "================================"
echo -e "交付项目管理系统部署脚本"
echo -e "================================"

# 检查 Java 环境
check_java() {
    echo -e "[1/6] 检查 Java 环境..."
    if ! command -v java &> /dev/null; then
        echo -e "错误: 未找到 Java 环境，请先安装 JDK 1.8+"
        exit 1
    fi
    java_version=java -version 2>&1 | awk -F '"' '/version/ {print }'
    echo -e "Java 版本: "
}

# 创建目录
create_dirs() {
    echo -e "[2/6] 创建应用目录..."
    mkdir -p 
    mkdir -p 
    echo -e "目录创建完成"
}

# 停止旧服务
stop_service() {
    echo -e "[3/6] 停止旧服务..."
    if systemctl is-active --quiet ; then
        systemctl stop 
        echo -e "服务已停止"
    else
        echo -e "服务未运行"
    fi
}

# 复制文件
copy_files() {
    echo -e "[4/6] 复制应用文件..."
    if [ -f "target/" ]; then
        cp target/ /
        echo -e "文件复制完成"
    else
        echo -e "错误: 未找到 ，请先执行 mvn package"
        exit 1
    fi
}

# 创建 systemd 服务
create_service() {
    echo -e "[5/6] 创建 systemd 服务..."
    cat > /etc/systemd/system/.service <<EOF
[Unit]
Description=Delivery Project Management System
After=network.target

[Service]
Type=simple
User=root
WorkingDirectory=
ExecStart=/usr/bin/java  -jar /
StandardOutput=append:/app.log
StandardError=append:/error.log
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
EOF

    systemctl daemon-reload
    echo -e "服务配置完成"
}

# 启动服务
start_service() {
    echo -e "[6/6] 启动服务..."
    systemctl start 
    systemctl enable 
    
    sleep 3
    
    if systemctl is-active --quiet ; then
        echo -e "================================"
        echo -e "部署成功！"
        echo -e "================================"
        echo -e "服务状态: 运行中"
        echo -e "访问地址: http://服务器IP:8080/api/health"
        echo -e "日志目录: "
        echo -e ""
        echo -e "常用命令:"
        echo -e "  查看状态: systemctl status "
        echo -e "  查看日志: tail -f /app.log"
        echo -e "  停止服务: systemctl stop "
        echo -e "  重启服务: systemctl restart "
    else
        echo -e "服务启动失败，请检查日志"
        journalctl -u  -n 50
        exit 1
    fi
}

# 执行部署
check_java
create_dirs
stop_service
copy_files
create_service
start_service
