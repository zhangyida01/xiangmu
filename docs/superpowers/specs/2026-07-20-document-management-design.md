# 交付物与文档管理模块 - 设计方案

**日期**: 2026-07-20  
**版本**: 1.0  
**负责人**: 开发团队

---

## 一、需求概述

### 1.1 背景

项目管理系统需要一个集中管理交付文档的模块，用于存储和管理项目相关的功能清单、验收材料、合同文档等交付物。

### 1.2 核心需求

- 按项目组织文档，每个项目下按文档类型分类
- 支持5种文档类型：功能清单、验收材料、合同文档、技术方案、其他文档
- 文件上传支持覆盖，不保留历史版本
- 按项目角色控制访问权限：只有项目相关人员能查看该项目文档
- 操作权限：项目经理可管理文档，项目成员只能查看和下载
- 文档存储在本地文件系统，数据库存储元数据
- 文档上传即生效，无需审批流程
- 提供基础筛选功能：按项目、文档类型、时间排序

### 1.3 用户角色

- **项目经理**: 可上传、替换、删除本项目的文档
- **项目成员**: 可查看、下载参与项目的文档
- **系统管理员**: 可管理所有项目文档（未来扩展）

---

## 二、数据库设计

### 2.1 新建表：project_document

```sql
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

### 2.2 字段说明

| 字段名 | 类型 | 说明 | 备注 |
|--------|------|------|------|
| id | BIGINT | 主键 | 自增 |
| project_id | BIGINT | 项目ID | 关联 delivery_project.id |
| doc_type | VARCHAR(20) | 文档类型 | 枚举值 |
| doc_name | VARCHAR(200) | 文档名称 | 用户自定义名称 |
| file_name | VARCHAR(200) | 原始文件名 | 上传时的文件名 |
| file_path | VARCHAR(500) | 文件存储路径 | 服务器本地路径 |
| file_size | BIGINT | 文件大小 | 单位：字节 |
| file_ext | VARCHAR(20) | 文件扩展名 | 如 pdf, docx, xlsx |
| upload_by | BIGINT | 上传人ID | 关联 user.id |
| upload_time | DATETIME | 上传时间 | 自动填充 |
| remark | TEXT | 备注说明 | 可选 |
| deleted | TINYINT | 逻辑删除 | 0-未删除 1-已删除 |

### 2.3 文档类型枚举

| 枚举值 | 说明 |
|--------|------|
| FUNCTION_LIST | 功能清单（包括软件、硬件功能清单） |
| ACCEPTANCE | 验收材料（包括验收单、验收报告等） |
| CONTRACT | 合同文档 |
| TECHNICAL | 技术方案 |
| OTHER | 其他文档 |

---

## 三、文件存储设计

### 3.1 存储结构

```
/data/documents/
├── project_{projectId}/
│   ├── function_list/
│   │   └── {uuid}_{filename}
│   ├── acceptance/
│   │   └── {uuid}_{filename}
│   ├── contract/
│   │   └── {uuid}_{filename}
│   ├── technical/
│   │   └── {uuid}_{filename}
│   └── other/
│       └── {uuid}_{filename}
```

### 3.2 文件命名规则

- 格式: `{UUID}_{originalFileName}`
- 示例: `a1b2c3d4-e5f6-7890-abcd-ef1234567890_软件功能清单.xlsx`
- 目的: 避免文件名冲突，保留原始文件名便于识别

### 3.3 文件类型限制

**允许的文件类型**:
- 文档: `.pdf`, `.doc`, `.docx`, `.xls`, `.xlsx`, `.ppt`, `.pptx`
- 压缩包: `.zip`, `.rar`, `.7z`
- 图片: `.jpg`, `.jpeg`, `.png` (用于截图等)
- 文本: `.txt`, `.md`

**文件大小限制**: 50MB

### 3.4 覆盖逻辑

当上传文档时：
1. 检查是否存在相同 `project_id` + `doc_type` + `doc_name` 的记录
2. 如果存在，删除旧文件，更新数据库记录
3. 如果不存在，创建新记录

---

## 四、后端API设计

### 4.1 接口列表

| 接口 | 方法 | 说明 | 权限 |
|------|------|------|------|
| /project-document/list | GET | 文档列表（分页+筛选） | 项目成员 |
| /project-document/upload | POST | 上传文档 | 项目经理 |
| /project-document/download/{id} | GET | 下载文档 | 项目成员 |
| /project-document/{id} | DELETE | 删除文档 | 项目经理 |
| /project-document/{id} | GET | 文档详情 | 项目成员 |

### 4.2 接口详细设计

#### 4.2.1 文档列表

**请求**: `GET /project-document/list`

**参数**:
```json
{
  "projectId": 1,           // 可选，项目ID
  "docType": "ACCEPTANCE",  // 可选，文档类型
  "current": 1,             // 当前页
  "size": 10                // 每页数量
}
```

**响应**:
```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "records": [
      {
        "id": 1,
        "projectId": 1,
        "projectName": "XX项目",
        "docType": "ACCEPTANCE",
        "docName": "验收单",
        "fileName": "验收单.pdf",
        "fileSize": 1024000,
        "fileExt": "pdf",
        "uploadBy": 2,
        "uploaderName": "张三",
        "uploadTime": "2026-07-20 14:30:00",
        "remark": "第一次验收"
      }
    ],
    "total": 1,
    "current": 1,
    "size": 10
  }
}
```

#### 4.2.2 上传文档

**请求**: `POST /project-document/upload`

**Content-Type**: `multipart/form-data`

**参数**:
- `file`: 文件
- `projectId`: 项目ID
- `docType`: 文档类型
- `docName`: 文档名称
- `remark`: 备注（可选）

**响应**:
```json
{
  "code": 200,
  "message": "上传成功",
  "data": {
    "id": 1,
    "docName": "验收单",
    "fileSize": 1024000
  }
}
```

#### 4.2.3 下载文档

**请求**: `GET /project-document/download/{id}`

**响应**: 文件流

**Headers**:
```
Content-Type: application/octet-stream
Content-Disposition: attachment; filename="验收单.pdf"
```

#### 4.2.4 删除文档

**请求**: `DELETE /project-document/{id}`

**响应**:
```json
{
  "code": 200,
  "message": "删除成功"
}
```

### 4.3 权限检查逻辑

**项目成员判断**:
```java
// 查询项目信息
Project project = projectService.getById(projectId);

// 检查是否为项目经理
boolean isManager = project.getProjectManagerId().equals(currentUserId);

// TODO: 后续扩展 - 检查是否为项目成员
// 可以新建 project_member 表记录项目参与人员
```

**权限规则**:
- 查看/下载: 项目经理或项目成员
- 上传/删除: 仅项目经理

---

## 五、前端UI设计

### 5.1 页面结构

**菜单位置**: 左侧导航 → 交付文档

**页面布局**:
```
┌────────────────────────────────────────────────────────┐
│  交付文档管理                               [+ 上传文档]  │
├────────────────────────────────────────────────────────┤
│  项目: [全部 ▼]  文档类型: [全部 ▼]  [搜索]            │
├─────┬──────────────┬──────────┬─────────┬──────┬───────┤
│ ID  │ 文档名称      │ 类型     │ 大小    │上传人│ 操作  │
├─────┼──────────────┼──────────┼─────────┼──────┼───────┤
│ 1   │ 软件功能清单  │ 功能清单 │ 2.3 MB │ 张三 │下载 删除│
│ 2   │ 验收单       │ 验收材料 │ 1.5 MB │ 张三 │下载 删除│
│ 3   │ 项目合同     │ 合同文档 │ 3.2 MB │ 李四 │下载     │
└─────┴──────────────┴──────────┴─────────┴──────┴───────┘
│                                          Total: 3    [分页]│
└────────────────────────────────────────────────────────┘
```

### 5.2 上传文档弹窗

```
┌───────────────────────────────────────┐
│  上传文档                        [×]  │
├───────────────────────────────────────┤
│  * 项目:      [选择项目 ▼]           │
│  * 文档类型:   [功能清单 ▼]          │
│  * 文档名称:   [________________]    │
│  * 选择文件:   [选择文件]  未选择     │
│    备注说明:   [________________]    │
│               [________________]    │
│                                      │
│  提示: 支持 PDF、Word、Excel等格式    │
│        最大文件大小: 50MB            │
│                                      │
│            [取消]      [确定上传]    │
└───────────────────────────────────────┘
```

### 5.3 交互逻辑

**筛选功能**:
- 项目下拉框: 显示当前用户作为项目经理或成员的项目
- 文档类型下拉框: 5种类型 + "全部"选项
- 筛选后自动刷新列表

**操作按钮**:
- **下载**: 所有项目成员可见，点击直接下载
- **删除**: 仅项目经理可见，点击需二次确认

**文件大小格式化**:
- < 1KB: 显示字节
- < 1MB: 显示 KB
- >= 1MB: 显示 MB

**上传逻辑**:
1. 项目下拉框只显示当前用户为项目经理的项目
2. 选择文件后显示文件名和大小
3. 校验文件类型和大小
4. 提交前检查是否存在同名文档，提示是否覆盖
5. 上传进度条显示

---

## 六、技术实现要点

### 6.1 后端实现

**技术栈**: Spring Boot + MyBatis-Plus

**关键类**:
- `ProjectDocument.java` - 实体类
- `ProjectDocumentMapper.java` - Mapper接口
- `ProjectDocumentService.java` - Service接口
- `ProjectDocumentServiceImpl.java` - Service实现
- `ProjectDocumentController.java` - Controller

**文件上传处理**:
```java
@PostMapping("/upload")
public Result<String> upload(
    @RequestParam("file") MultipartFile file,
    @RequestParam("projectId") Long projectId,
    @RequestParam("docType") String docType,
    @RequestParam("docName") String docName,
    @RequestParam(required = false) String remark) {
    
    // 1. 权限检查
    // 2. 文件类型校验
    // 3. 文件大小校验
    // 4. 检查是否存在同名文档
    // 5. 生成存储路径
    // 6. 保存文件到本地
    // 7. 保存元数据到数据库
}
```

**文件下载处理**:
```java
@GetMapping("/download/{id}")
public void download(@PathVariable Long id, HttpServletResponse response) {
    // 1. 权限检查
    // 2. 查询文档信息
    // 3. 读取文件
    // 4. 设置响应头
    // 5. 输出文件流
}
```

### 6.2 前端实现

**技术栈**: Vue 3 + Element Plus

**关键组件**:
- `Document.vue` - 文档列表页
- API文件: `src/api/document.js`

**文件上传组件**:
```vue
<el-upload
  :action="uploadUrl"
  :headers="uploadHeaders"
  :on-success="handleUploadSuccess"
  :on-error="handleUploadError"
  :before-upload="beforeUpload"
  :show-file-list="true"
  :limit="1">
  <el-button type="primary">选择文件</el-button>
</el-upload>
```

**文件下载实现**:
```javascript
const handleDownload = async (row) => {
  const res = await request.get(`/project-document/download/${row.id}`, {
    responseType: 'blob'
  })
  const blob = new Blob([res])
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = row.fileName
  link.click()
}
```

---

## 七、非功能需求

### 7.1 性能要求

- 文件上传: 支持最大50MB
- 列表查询: 分页加载，默认10条/页
- 下载速度: 取决于服务器带宽和文件大小

### 7.2 安全要求

**文件类型校验**:
- 前端: 通过 `accept` 属性限制
- 后端: 检查文件扩展名和MIME类型

**路径安全**:
- 禁止文件名包含 `../`、`./` 等路径遍历字符
- 使用UUID前缀避免文件名冲突
- 文件存储路径不可被外部直接访问

**权限校验**:
- 每个接口都需检查用户权限
- 使用JWT token验证用户身份
- 项目经理权限通过查询项目表验证

### 7.3 备份策略

**文件备份**:
- 定期备份 `/data/documents/` 目录
- 建议每日增量备份，每周全量备份

**数据库备份**:
- 定期备份 `project_document` 表
- 与其他业务表一起备份

---

## 八、开发计划

### 8.1 开发阶段

**阶段1: 后端开发（2天）**
- 数据库表创建
- 实体类和Mapper
- Service层实现
- Controller接口开发
- 文件上传下载逻辑
- 权限检查实现

**阶段2: 前端开发（2天）**
- 文档列表页面
- 上传文档弹窗
- 筛选功能实现
- 下载和删除功能
- UI优化和交互完善

**阶段3: 联调测试（1天）**
- 功能测试
- 权限测试
- 异常场景测试
- 性能测试

**总计**: 5个工作日

### 8.2 验收标准

- [ ] 项目经理可以上传文档
- [ ] 项目成员可以查看和下载文档
- [ ] 可以按项目和类型筛选文档
- [ ] 上传同名文档时正确覆盖
- [ ] 删除文档时同时删除物理文件
- [ ] 非项目成员无法查看文档
- [ ] 非项目经理无法上传/删除文档
- [ ] 文件类型和大小限制生效
- [ ] 文件下载正确

---

## 九、后续扩展

### 9.1 近期扩展

- 支持在线预览（PDF、图片等）
- 文档批量下载（打包为ZIP）
- 文档批量上传

### 9.2 远期扩展

- 文档版本管理（保留历史版本）
- 文档审批流程（草稿→审核→发布）
- 项目成员管理表（支持多人协作）
- 文档全文搜索
- 文档访问日志
- 对象存储支持（OSS/S3）

---

## 十、风险和注意事项

### 10.1 风险

1. **磁盘空间不足**: 文档累积可能占用大量磁盘空间
   - 缓解: 定期清理已删除项目的文档，监控磁盘使用率

2. **并发上传冲突**: 多人同时上传同名文档可能冲突
   - 缓解: 使用数据库事务 + 文件锁

3. **大文件上传失败**: 50MB文件上传可能超时
   - 缓解: 调整nginx和Tomcat的超时配置

### 10.2 注意事项

1. 确保 `/data/documents/` 目录有足够权限
2. 配置nginx支持大文件上传（`client_max_body_size`）
3. 配置Spring Boot上传大小限制（`spring.servlet.multipart.max-file-size`）
4. 文档删除采用逻辑删除，物理文件可异步清理
5. 生产环境建议使用对象存储（OSS）代替本地存储

---

## 十一、配置清单

### 11.1 Spring Boot配置

```yaml
spring:
  servlet:
    multipart:
      enabled: true
      max-file-size: 50MB
      max-request-size: 50MB
      
document:
  storage:
    path: /data/documents/
  allowed-extensions:
    - pdf
    - doc
    - docx
    - xls
    - xlsx
    - zip
    - rar
```

### 11.2 Nginx配置

```nginx
client_max_body_size 50M;
client_body_timeout 300s;
```

---

**文档结束**