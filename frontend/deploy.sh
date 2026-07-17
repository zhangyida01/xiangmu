#!/bin/bash
# 前端自动部署脚本 - 适配CentOS 7.9

set -e

echo "=================================="
echo "交付项目管理系统 - 前端部署"
echo "=================================="

# 检查是否安装Node.js
if ! command -v node &> /dev/null; then
    echo "❌ 未安装Node.js，正在安装 Node.js 16.x..."
    curl -fsSL https://rpm.nodesource.com/setup_16.x | sudo bash -
    sudo yum install -y nodejs
fi

echo "✅ Node.js版本: $(node -v)"
echo "✅ npm版本: $(npm -v)"

# 安装依赖
echo ""
echo "📦 安装依赖..."
npm install

# 构建项目
echo ""
echo "🔨 构建项目..."
npm run build

# 创建部署目录
DEPLOY_DIR="/opt/delivery-frontend"
echo ""
echo "📁 创建部署目录: $DEPLOY_DIR"
sudo mkdir -p $DEPLOY_DIR

# 备份旧版本
if [ -d "$DEPLOY_DIR/dist" ]; then
    echo "💾 备份旧版本..."
    sudo mv $DEPLOY_DIR/dist $DEPLOY_DIR/dist.backup.$(date +%Y%m%d%H%M%S)
fi

# 复制新版本
echo "📋 复制新版本..."
sudo cp -r dist $DEPLOY_DIR/

# 安装Nginx
if ! command -v nginx &> /dev/null; then
    echo ""
    echo "📦 安装Nginx..."
    sudo yum install -y nginx
fi

# 配置Nginx
echo ""
echo "⚙️  配置Nginx..."
sudo cp nginx.conf /etc/nginx/conf.d/delivery.conf

# 测试Nginx配置
echo "🧪 测试Nginx配置..."
sudo nginx -t

# 重启Nginx
echo "🔄 重启Nginx..."
sudo systemctl restart nginx
sudo systemctl enable nginx

# 开放端口
echo "🔓 配置防火墙..."
sudo firewall-cmd --permanent --add-port=80/tcp 2>/dev/null || true
sudo firewall-cmd --reload 2>/dev/null || true

echo ""
echo "=================================="
echo "✅ 部署完成！"
echo "=================================="
echo ""
echo "访问地址: http://120.27.247.61"
echo "默认账号: admin"
echo "默认密码: admin123"
echo ""
echo "查看Nginx状态: systemctl status nginx"
echo "查看Nginx日志: tail -f /var/log/nginx/delivery-error.log"
echo ""
