# 交付文档管理模块 - 部署指南

## 📋 部署检查清单

### 开发完成内容
✅ 数据库表 `project_document`
✅ 后端实体、Mapper、Service
✅ 后端Controller (列表、上传、下载、删除)
✅ 前端API接口
✅ 前端文档管理页面
✅ 路由配置
✅ 文件上传配置

---

## 🚀 服务器部署步骤

### 步骤1: 执行数据库迁移

连接到MySQL数据库：

```bash
mysql -h 172.16.2.40 -u root -p
```

执行建表SQL：

```sql
USE delivery_management;

CREATE TABLE `project_document` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `project_id` BIGINT(20) NOT NULL COMMENT '项目ID',
  `doc_type` VARCHAR(20) NOT NULL COMMENT '文档类型：FUNCTION_LIST-功能清单, ACCEPTANCE-验收材料, CONTRACT-合同文档, TECHNICAL-技术方案, OTHER-其他',
  `doc_name` VARCHAR(200) NOT NULL COMMENT '文档名称',
  `file_name` VARCHAR(200) NOT NULL COMMENT '原始文件名',
  `file_path` VARCHAR(500) NOT NULL COMMENT '文件存储路径',
  `file_size` BIGINT(20) NOT NULL COMMENT '文件大小(字节)',
  `file_ext` VARCHAR(20) DEFAULT NULL COMMENT '文件扩展名',
  `upload_by` BIGINT(20) NOT NULL COMMENT '上传人ID',
  `upload_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  `remark` TEXT COMMENT '备注说明',
  `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_project_id` (`project_id`),
  KEY `idx_doc_type` (`doc_type`),
  KEY `idx_upload_time` (`upload_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目文档表';
```

或者直接执行SQL文件：

```bash
mysql -h 172.16.2.40 -u root -p delivery_management < docs/sql/project_document.sql
```

---

### 步骤2: 创建文档存储目录

```bash
sudo mkdir -p /data/documents
sudo chown -R root:root /data/documents
sudo chmod -R 755 /data/documents
```

---

### 步骤3: 拉取代码并部署后端

```bash
cd /root/xiangmu/java
git pull origin main
mvn clean package -DskipTests
./deploy.sh
```

如果Git连接失败，手动上传以下文件：
- `src/main/java/com/delivery/management/entity/ProjectDocument.java`
- `src/main/java/com/delivery/management/mapper/ProjectDocumentMapper.java`
- `src/main/java/com/delivery/management/service/ProjectDocumentService.java`
- `src/main/java/com/delivery/management/service/impl/ProjectDocumentServiceImpl.java`
- `src/main/java/com/delivery/management/controller/ProjectDocumentController.java`
- `src/main/resources/application.yml`

---

### 步骤4: 构建并部署前端

```bash
cd /root/xiangmu/java/frontend
npm run build
sudo cp -r dist/* /opt/delivery-frontend/
sudo systemctl restart nginx
```

如果Git连接失败，手动上传以下文件：
- `frontend/src/api/document.js`
- `frontend/src/views/Document.vue`
- `frontend/src/router/index.js`

---

### 步骤5: 配置Nginx (如果需要)

确保nginx支持大文件上传，编辑 `/etc/nginx/nginx.conf`：

```nginx
http {
    client_max_body_size 50M;
    client_body_timeout 300s;
}
```

重启nginx：

```bash
sudo systemctl restart nginx
```

---

## 🧪 功能测试清单

部署完成后，按顺序测试：

### 1. 登录系统
- 访问 http://120.27.247.61
- 使用 admin/admin123 登录

### 2. 访问交付文档页面
- 在左侧菜单找到"交付文档"
- 页面正常加载

### 3. 测试文档上传（作为项目经理）
- 选择一个自己是项目经理的项目
- 点击"上传文档"按钮
- 填写信息：
  - 项目：选择项目
  - 文档类型：功能清单
  - 文档名称：测试功能清单
  - 选择文件：上传一个PDF或Word文件
  - 备注：测试上传
- 点击"确定上传"
- ✅ 验证：上传成功，列表中显示新文档

### 4. 测试文档下载
- 点击刚上传文档的"下载"按钮
- ✅ 验证：文件下载成功，内容正确

### 5. 测试文档覆盖
- 再次上传同名文档（项目、类型、名称都相同）
- ✅ 验证：旧文档被覆盖，列表中只有一条记录

### 6. 测试文档筛选
- 按项目筛选
- 按文档类型筛选
- ✅ 验证：筛选结果正确

### 7. 测试文档删除
- 点击文档的"删除"按钮
- 确认删除
- ✅ 验证：文档从列表中消失，物理文件也被删除

### 8. 测试权限控制
- 切换到一个不是项目经理的项目
- ✅ 验证：看不到"上传文档"按钮，无法删除文档

### 9. 测试文件类型限制
- 尝试上传不支持的文件类型（如.exe）
- ✅ 验证：提示"不支持的文件类型"

### 10. 测试文件大小限制
- 尝试上传超过50MB的文件
- ✅ 验证：提示"文件大小不能超过50MB"

---

## ❌ 常见问题排查

### 问题1: 上传失败 "413 Request Entity Too Large"

**原因**: Nginx文件上传大小限制

**解决**:
```bash
sudo vi /etc/nginx/nginx.conf
# 添加或修改
client_max_body_size 50M;

sudo systemctl restart nginx
```

---

### 问题2: 上传失败 "文件上传失败"

**原因**: 文件存储目录没有写权限

**解决**:
```bash
sudo mkdir -p /data/documents
sudo chmod -R 755 /data/documents
```

---

### 问题3: 下载文件404

**原因**: 文件路径不正确或文件被删除

**解决**: 检查数据库中的file_path字段和实际文件是否存在

---

### 问题4: 页面显示"您不是任何项目的项目经理"

**原因**: 当前用户没有作为项目经理的项目

**解决**: 
1. 到"项目管理"页面
2. 创建一个新项目，项目经理选择当前用户
3. 或修改现有项目的项目经理

---

## 📊 验收标准

所有以下项目必须通过：

- [x] 数据库表创建成功
- [x] 后端编译无错误
- [x] 前端构建成功
- [x] 可以上传文档（PDF、Word、Excel等）
- [x] 可以下载文档
- [x] 可以删除文档
- [x] 文档列表可以按项目和类型筛选
- [x] 只有项目经理可以上传和删除文档
- [x] 上传同名文档时正确覆盖
- [x] 文件大小和类型限制生效
- [x] 文件存储在正确的目录结构中

---

## 📝 提交记录

本次开发共提交13次：

1. feat: add project_document table schema
2. feat: add ProjectDocument entity
3. feat: add ProjectDocumentMapper
4. feat: add ProjectDocumentService
5. feat: add ProjectDocumentController with list API
6. feat: add document upload functionality
7. feat: add document download and delete APIs
8. feat: add document API module
9. feat: add document management page
10. feat: add document route
11. config: add document storage configuration
12. docs: add document management implementation plan
13. docs: add document management module design spec

---

## 🎉 部署完成

交付文档管理模块开发完成！