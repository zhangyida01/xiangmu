#!/bin/bash
# 自动部署脚本

echo "===== 项目账户模块部署 ====="

# 1. 进入项目目录
cd /root/xiangmu/java || exit 1

# 2. 拉取最新代码（如果Git可用）
echo "正在拉取最新代码..."
git pull origin main

# 3. 清理编译
echo "正在清理并编译..."
mvn clean package -DskipTests

# 4. 检查编译结果
if [ ! -f target/delivery-management-1.0.0.jar ]; then
    echo "编译失败！"
    exit 1
fi

# 5. 重启服务
echo "正在重启服务..."
./deploy.sh

echo "===== 部署完成 ====="
echo "请访问 http://120.27.247.61 验证功能"