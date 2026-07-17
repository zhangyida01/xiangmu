# 交付项目管理系统 - 前端

基于 Vue 3 + Element Plus + Vite 开发的现代化管理后台。

## 技术栈

- **框架**: Vue 3 (Composition API)
- **UI组件**: Element Plus
- **构建工具**: Vite
- **状态管理**: Pinia
- **路由**: Vue Router
- **HTTP客户端**: Axios

## 功能页面

- ✅ 登录页面（JWT认证）
- ✅ 首页看板（统计数据）
- ✅ 用户管理（增删改查）
- ✅ 项目管理（增删改查）
- ✅ 工单管理（增删改查+处理）

## 本地开发

### 1. 安装依赖

```bash
cd frontend
npm install
```

### 2. 启动开发服务器

```bash
npm run dev
```

访问：http://localhost:3000

**默认账号：**
- 用户名：admin
- 密码：admin123

### 3. 构建生产版本

```bash
npm run build
```

生成的文件在 `dist/` 目录。

## CentOS 7.9 部署

### 方式1：Nginx + 反向代理（推荐）

#### 1. 安装Node.js和npm

```bash
# 安装Node.js 18.x
curl -fsSL https://rpm.nodesource.com/setup_18.x | sudo bash -
sudo yum install -y nodejs

# 验证安装
node -v
npm -v
```

#### 2. 上传前端代码

```bash
# 在本地打包
cd frontend
npm install
npm run build

# 上传到服务器
scp -r dist/ root@120.27.247.61:/opt/delivery-frontend/
```

#### 3. 安装Nginx

```bash
sudo yum install -y nginx
```

#### 4. 配置Nginx

创建配置文件 `/etc/nginx/conf.d/delivery.conf`：

```nginx
server {
    listen 80;
    server_name 120.27.247.61;
    
    # 前端静态文件
    location / {
        root /opt/delivery-frontend/dist;
        try_files $uri $uri/ /index.html;
        index index.html;
    }
    
    # 后端API代理
    location /api/ {
        proxy_pass http://localhost:9080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

#### 5. 启动Nginx

```bash
# 测试配置
sudo nginx -t

# 启动Nginx
sudo systemctl start nginx
sudo systemctl enable nginx

# 开放80端口
sudo firewall-cmd --permanent --add-port=80/tcp
sudo firewall-cmd --reload
```

#### 6. 访问系统

浏览器访问：http://120.27.247.61

### 方式2：直接使用Node.js serve

```bash
# 安装serve
npm install -g serve

# 启动服务
serve -s dist -l 3000
```

## 配置说明

### API地址配置

开发环境（`vite.config.js`）：
```javascript
server: {
  port: 3000,
  proxy: {
    '/api': {
      target: 'http://120.27.247.61:9080',
      changeOrigin: true
    }
  }
}
```

生产环境：通过Nginx反向代理配置

### 路由模式

使用 `createWebHistory` 模式，需要服务器配置 `try_files`。

## 项目结构

```
frontend/
├── public/               # 静态资源
├── src/
│   ├── api/             # API接口
│   │   ├── auth.js      # 认证接口
│   │   ├── user.js      # 用户接口
│   │   ├── project.js   # 项目接口
│   │   └── ticket.js    # 工单接口
│   ├── components/      # 公共组件
│   ├── router/          # 路由配置
│   ├── store/           # 状态管理
│   ├── utils/           # 工具类
│   │   └── request.js   # Axios封装
│   ├── views/           # 页面组件
│   │   ├── Login.vue    # 登录页
│   │   ├── Layout.vue   # 主布局
│   │   ├── Dashboard.vue # 首页
│   │   ├── User.vue     # 用户管理
│   │   ├── Project.vue  # 项目管理
│   │   └── Ticket.vue   # 工单管理
│   ├── App.vue          # 根组件
│   └── main.js          # 入口文件
├── index.html           # HTML模板
├── package.json         # 依赖配置
├── vite.config.js       # Vite配置
└── README.md           # 说明文档
```

## 功能特性

### 认证机制
- JWT Token认证
- 自动Token刷新
- 路由守卫保护

### 用户体验
- 统一的响应处理
- 友好的错误提示
- Loading状态管理
- 表单验证

### 数据管理
- 分页查询
- 条件筛选
- 批量操作
- 实时刷新

## 常见问题

### Q1: 登录后接口返回401？
A: Token可能已过期，重新登录即可。

### Q2: API请求跨域？
A: 
- 开发环境：已配置Vite代理
- 生产环境：通过Nginx反向代理解决

### Q3: 页面刷新404？
A: Nginx需要配置 `try_files $uri $uri/ /index.html`

### Q4: 如何修改后端API地址？
A: 
- 开发环境：修改 `vite.config.js` 中的 proxy.target
- 生产环境：修改Nginx配置中的 proxy_pass

## 更新日志

### v1.0.0 (2026-07-17)
- ✅ 初始版本发布
- ✅ 完成核心功能开发
- ✅ 支持前后端分离部署

---

**开发团队**: 交付项目管理系统
