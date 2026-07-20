# 交付物与文档管理模块 - 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现按项目组织的文档管理系统，支持文档上传、下载、删除和权限控制

**Architecture:** 后端使用Spring Boot + MyBatis-Plus，文件存储在本地文件系统；前端使用Vue3 + Element Plus，支持文件上传和列表展示；权限通过项目经理身份验证

**Tech Stack:** Spring Boot 2.x, MyBatis-Plus, Vue 3, Element Plus, MySQL

---

## 文件结构

### 后端文件 (新建)
- `src/main/java/com/delivery/management/entity/ProjectDocument.java` - 文档实体类
- `src/main/java/com/delivery/management/mapper/ProjectDocumentMapper.java` - Mapper接口
- `src/main/java/com/delivery/management/service/ProjectDocumentService.java` - Service接口
- `src/main/java/com/delivery/management/service/impl/ProjectDocumentServiceImpl.java` - Service实现
- `src/main/java/com/delivery/management/controller/ProjectDocumentController.java` - Controller
- `docs/sql/project_document.sql` - 建表SQL

### 前端文件 (新建)
- `frontend/src/views/Document.vue` - 文档管理页面
- `frontend/src/api/document.js` - 文档API接口

### 前端文件 (修改)
- `frontend/src/router/index.js` - 添加路由

---

## Task 1: 创建数据库表

**Files:**
- Create: `docs/sql/project_document.sql`

- [ ] **Step 1: 创建建表SQL文件**

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

- [ ] **Step 2: 验证SQL语法**

在数据库中执行上述SQL，确认表创建成功

- [ ] **Step 3: 提交**

```bash
git add docs/sql/project_document.sql
git commit -m "feat: add project_document table schema"
```

---

## Task 2: 创建实体类

**Files:**
- Create: `src/main/java/com/delivery/management/entity/ProjectDocument.java`

- [ ] **Step 1: 创建ProjectDocument实体类**

```java
package com.delivery.management.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("project_document")
public class ProjectDocument {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long projectId;
    
    private String docType;
    
    private String docName;
    
    private String fileName;
    
    private String filePath;
    
    private Long fileSize;
    
    private String fileExt;
    
    private Long uploadBy;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime uploadTime;
    
    private String remark;
    
    @TableLogic
    private Integer deleted;
    
    // 临时字段，不映射到数据库
    @TableField(exist = false)
    private String uploaderName;
    
    @TableField(exist = false)
    private String projectName;
}
```

- [ ] **Step 2: 验证编译**

```bash
cd /root/xiangmu/java
mvn compile
```

预期输出：BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git add src/main/java/com/delivery/management/entity/ProjectDocument.java
git commit -m "feat: add ProjectDocument entity"
```

---

## Task 3: 创建Mapper接口

**Files:**
- Create: `src/main/java/com/delivery/management/mapper/ProjectDocumentMapper.java`

- [ ] **Step 1: 创建Mapper接口**

```java
package com.delivery.management.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.delivery.management.entity.ProjectDocument;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProjectDocumentMapper extends BaseMapper<ProjectDocument> {
}
```

- [ ] **Step 2: 验证编译**

```bash
mvn compile
```

预期输出：BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git add src/main/java/com/delivery/management/mapper/ProjectDocumentMapper.java
git commit -m "feat: add ProjectDocumentMapper"
```

---

## Task 4: 创建Service接口和实现

**Files:**
- Create: `src/main/java/com/delivery/management/service/ProjectDocumentService.java`
- Create: `src/main/java/com/delivery/management/service/impl/ProjectDocumentServiceImpl.java`

- [ ] **Step 1: 创建Service接口**

```java
package com.delivery.management.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.delivery.management.entity.ProjectDocument;

public interface ProjectDocumentService extends IService<ProjectDocument> {
}
```

- [ ] **Step 2: 创建Service实现类**

```java
package com.delivery.management.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.delivery.management.entity.ProjectDocument;
import com.delivery.management.mapper.ProjectDocumentMapper;
import com.delivery.management.service.ProjectDocumentService;
import org.springframework.stereotype.Service;

@Service
public class ProjectDocumentServiceImpl extends ServiceImpl<ProjectDocumentMapper, ProjectDocument> 
        implements ProjectDocumentService {
}
```

- [ ] **Step 3: 验证编译**

```bash
mvn compile
```

预期输出：BUILD SUCCESS

- [ ] **Step 4: 提交**

```bash
git add src/main/java/com/delivery/management/service/ProjectDocumentService.java
git add src/main/java/com/delivery/management/service/impl/ProjectDocumentServiceImpl.java
git commit -m "feat: add ProjectDocumentService"
```

---
## Task 5: 创建Controller - 文档列表查询

**Files:**
- Create: `src/main/java/com/delivery/management/controller/ProjectDocumentController.java`

- [ ] **Step 1: 创建Controller基础结构和列表接口**

```java
package com.delivery.management.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.delivery.management.common.Result;
import com.delivery.management.entity.Project;
import com.delivery.management.entity.ProjectDocument;
import com.delivery.management.entity.User;
import com.delivery.management.service.ProjectDocumentService;
import com.delivery.management.service.ProjectService;
import com.delivery.management.service.UserService;
import com.delivery.management.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/project-document")
public class ProjectDocumentController {

    @Autowired
    private ProjectDocumentService projectDocumentService;
    
    @Autowired
    private ProjectService projectService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/list")
    public Result<Page<ProjectDocument>> list(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) String docType,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {
        
        Page<ProjectDocument> page = new Page<>(current, size);
        QueryWrapper<ProjectDocument> wrapper = new QueryWrapper<>();
        
        if (projectId != null) {
            wrapper.eq("project_id", projectId);
        }
        
        if (StringUtils.hasText(docType)) {
            wrapper.eq("doc_type", docType);
        }
        
        wrapper.orderByDesc("upload_time");
        
        Page<ProjectDocument> result = projectDocumentService.page(page, wrapper);
        
        // 填充上传人姓名和项目名称
        result.getRecords().forEach(doc -> {
            if (doc.getUploadBy() != null) {
                User user = userService.getById(doc.getUploadBy());
                if (user != null) {
                    doc.setUploaderName(user.getRealName() != null ? user.getRealName() : user.getUsername());
                }
            }
            if (doc.getProjectId() != null) {
                Project project = projectService.getById(doc.getProjectId());
                if (project != null) {
                    doc.setProjectName(project.getProjectName());
                }
            }
        });
        
        return Result.success(result);
    }
}
```

- [ ] **Step 2: 验证编译**

```bash
mvn compile
```

预期输出：BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git add src/main/java/com/delivery/management/controller/ProjectDocumentController.java
git commit -m "feat: add ProjectDocumentController with list API"
```

---

## Task 6: Controller - 文件上传功能

**Files:**
- Modify: `src/main/java/com/delivery/management/controller/ProjectDocumentController.java`

- [ ] **Step 1: 在Controller中添加上传接口**

在ProjectDocumentController类中添加以下方法：

```java
    @PostMapping("/upload")
    public Result<String> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("projectId") Long projectId,
            @RequestParam("docType") String docType,
            @RequestParam("docName") String docName,
            @RequestParam(required = false) String remark,
            HttpServletRequest request) {
        
        // 1. 获取当前用户ID
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return Result.fail("未登录");
        }
        token = token.substring(7);
        Long userId = jwtUtil.getUserIdFromToken(token);
        
        // 2. 权限检查：必须是项目经理
        Project project = projectService.getById(projectId);
        if (project == null) {
            return Result.fail("项目不存在");
        }
        if (!project.getProjectManagerId().equals(userId)) {
            return Result.fail("只有项目经理可以上传文档");
        }
        
        // 3. 文件校验
        if (file.isEmpty()) {
            return Result.fail("文件不能为空");
        }
        
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            return Result.fail("文件名不能为空");
        }
        
        // 文件类型校验
        String fileExt = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        String[] allowedExts = {"pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "zip", "rar", "jpg", "jpeg", "png", "txt"};
        boolean isAllowed = false;
        for (String ext : allowedExts) {
            if (ext.equals(fileExt)) {
                isAllowed = true;
                break;
            }
        }
        if (!isAllowed) {
            return Result.fail("不支持的文件类型");
        }
        
        // 文件大小校验（50MB）
        if (file.getSize() > 50 * 1024 * 1024) {
            return Result.fail("文件大小不能超过50MB");
        }
        
        // 4. 检查是否存在同名文档（覆盖逻辑）
        QueryWrapper<ProjectDocument> wrapper = new QueryWrapper<>();
        wrapper.eq("project_id", projectId)
               .eq("doc_type", docType)
               .eq("doc_name", docName);
        ProjectDocument existDoc = projectDocumentService.getOne(wrapper);
        
        try {
            // 5. 生成存储路径
            String basePath = "/data/documents";
            String docTypeFolder = docType.toLowerCase();
            String projectFolder = "project_" + projectId;
            
            // 创建目录
            File dir = new File(basePath, projectFolder + "/" + docTypeFolder);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
            // 生成文件名：UUID_originalFileName
            String uuid = UUID.randomUUID().toString();
            String newFileName = uuid + "_" + originalFilename;
            String filePath = projectFolder + "/" + docTypeFolder + "/" + newFileName;
            
            // 6. 保存文件
            File destFile = new File(basePath, filePath);
            file.transferTo(destFile);
            
            // 7. 如果存在同名文档，删除旧文件并更新记录
            if (existDoc != null) {
                // 删除旧文件
                File oldFile = new File(basePath, existDoc.getFilePath());
                if (oldFile.exists()) {
                    oldFile.delete();
                }
                
                // 更新记录
                existDoc.setFileName(originalFilename);
                existDoc.setFilePath(filePath);
                existDoc.setFileSize(file.getSize());
                existDoc.setFileExt(fileExt);
                existDoc.setUploadBy(userId);
                existDoc.setUploadTime(LocalDateTime.now());
                existDoc.setRemark(remark);
                projectDocumentService.updateById(existDoc);
            } else {
                // 创建新记录
                ProjectDocument doc = new ProjectDocument();
                doc.setProjectId(projectId);
                doc.setDocType(docType);
                doc.setDocName(docName);
                doc.setFileName(originalFilename);
                doc.setFilePath(filePath);
                doc.setFileSize(file.getSize());
                doc.setFileExt(fileExt);
                doc.setUploadBy(userId);
                doc.setRemark(remark);
                projectDocumentService.save(doc);
            }
            
            return Result.success("上传成功");
            
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("上传失败：" + e.getMessage());
        }
    }
```

- [ ] **Step 2: 添加必要的import语句**

在文件顶部添加：

```java
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.time.LocalDateTime;
import java.util.UUID;
```

- [ ] **Step 3: 验证编译**

```bash
mvn compile
```

预期输出：BUILD SUCCESS

- [ ] **Step 4: 提交**

```bash
git add src/main/java/com/delivery/management/controller/ProjectDocumentController.java
git commit -m "feat: add document upload functionality"
```

---

## Task 7: Controller - 文件下载和删除功能

**Files:**
- Modify: `src/main/java/com/delivery/management/controller/ProjectDocumentController.java`

- [ ] **Step 1: 添加下载接口**

在ProjectDocumentController类中添加：

```java
    @GetMapping("/download/{id}")
    public void download(@PathVariable Long id, HttpServletResponse response, HttpServletRequest request) {
        try {
            // 1. 获取当前用户ID
            String token = request.getHeader("Authorization");
            if (token == null || !token.startsWith("Bearer ")) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            token = token.substring(7);
            Long userId = jwtUtil.getUserIdFromToken(token);
            
            // 2. 查询文档信息
            ProjectDocument doc = projectDocumentService.getById(id);
            if (doc == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            
            // 3. 权限检查：必须是项目成员或项目经理
            Project project = projectService.getById(doc.getProjectId());
            if (project == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            // TODO: 后续可扩展检查项目成员
            if (!project.getProjectManagerId().equals(userId)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            
            // 4. 读取文件
            String basePath = "/data/documents";
            File file = new File(basePath, doc.getFilePath());
            if (!file.exists()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            
            // 5. 设置响应头
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", 
                "attachment; filename=" + URLEncoder.encode(doc.getFileName(), "UTF-8"));
            response.setContentLengthLong(file.length());
            
            // 6. 输出文件流
            try (FileInputStream fis = new FileInputStream(file);
                 OutputStream os = response.getOutputStream()) {
                byte[] buffer = new byte[1024];
                int len;
                while ((len = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, len);
                }
                os.flush();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
```

- [ ] **Step 2: 添加删除接口**

```java
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id, HttpServletRequest request) {
        // 1. 获取当前用户ID
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return Result.fail("未登录");
        }
        token = token.substring(7);
        Long userId = jwtUtil.getUserIdFromToken(token);
        
        // 2. 查询文档信息
        ProjectDocument doc = projectDocumentService.getById(id);
        if (doc == null) {
            return Result.fail("文档不存在");
        }
        
        // 3. 权限检查：必须是项目经理
        Project project = projectService.getById(doc.getProjectId());
        if (project == null) {
            return Result.fail("项目不存在");
        }
        if (!project.getProjectManagerId().equals(userId)) {
            return Result.fail("只有项目经理可以删除文档");
        }
        
        // 4. 删除物理文件
        String basePath = "/data/documents";
        File file = new File(basePath, doc.getFilePath());
        if (file.exists()) {
            file.delete();
        }
        
        // 5. 删除数据库记录（逻辑删除）
        boolean success = projectDocumentService.removeById(id);
        
        return success ? Result.success("删除成功") : Result.fail("删除失败");
    }
```

- [ ] **Step 3: 添加详情接口**

```java
    @GetMapping("/{id}")
    public Result<ProjectDocument> detail(@PathVariable Long id) {
        ProjectDocument doc = projectDocumentService.getById(id);
        if (doc == null) {
            return Result.fail("文档不存在");
        }
        return Result.success(doc);
    }
```

- [ ] **Step 4: 添加必要的import**

```java
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
```

- [ ] **Step 5: 验证编译**

```bash
mvn compile
```

预期输出：BUILD SUCCESS

- [ ] **Step 6: 提交**

```bash
git add src/main/java/com/delivery/management/controller/ProjectDocumentController.java
git commit -m "feat: add document download and delete APIs"
```

---
## Task 8: 创建前端API文件

**Files:**
- Create: `frontend/src/api/document.js`

- [ ] **Step 1: 创建API文件**

```javascript
import request from '../utils/request'

export function getDocumentList(params) {
  return request({
    url: '/project-document/list',
    method: 'get',
    params
  })
}

export function uploadDocument(data) {
  return request({
    url: '/project-document/upload',
    method: 'post',
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    data
  })
}

export function downloadDocument(id) {
  return request({
    url: `/project-document/download/${id}`,
    method: 'get',
    responseType: 'blob'
  })
}

export function deleteDocument(id) {
  return request({
    url: `/project-document/${id}`,
    method: 'delete'
  })
}

export function getDocumentDetail(id) {
  return request({
    url: `/project-document/${id}`,
    method: 'get'
  })
}
```

- [ ] **Step 2: 提交**

```bash
git add frontend/src/api/document.js
git commit -m "feat: add document API module"
```

---

## Task 9: 创建文档管理页面

**Files:**
- Create: `frontend/src/views/Document.vue`

- [ ] **Step 1: 创建Document.vue文件**

```vue
<template>
  <div class="document-container">
    <el-card>
      <div class="toolbar">
        <el-select v-model="queryParams.projectId" placeholder="选择项目" style="width: 300px" filterable clearable @change="loadDocuments">
          <el-option v-for="project in projectOptions" :key="project.id" :label="project.projectName" :value="project.id" />
        </el-select>
        <el-select v-model="queryParams.docType" placeholder="文档类型" style="width: 150px" clearable @change="loadDocuments">
          <el-option label="功能清单" value="FUNCTION_LIST" />
          <el-option label="验收材料" value="ACCEPTANCE" />
          <el-option label="合同文档" value="CONTRACT" />
          <el-option label="技术方案" value="TECHNICAL" />
          <el-option label="其他" value="OTHER" />
        </el-select>
        <el-button type="primary" :icon="Search" @click="loadDocuments">搜索</el-button>
        <el-button type="primary" :icon="Plus" @click="handleUpload" :disabled="!canUpload">上传文档</el-button>
      </div>

      <el-table :data="documentList" style="width: 100%; margin-top: 20px" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="docName" label="文档名称" min-width="200" />
        <el-table-column prop="projectName" label="所属项目" width="180" />
        <el-table-column prop="docType" label="文档类型" width="120">
          <template #default="{ row }">
            <el-tag :type="docTypeColors[row.docType]">{{ docTypes[row.docType] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="fileName" label="文件名" min-width="180" show-overflow-tooltip />
        <el-table-column prop="fileSize" label="文件大小" width="100">
          <template #default="{ row }">
            {{ formatFileSize(row.fileSize) }}
          </template>
        </el-table-column>
        <el-table-column prop="uploaderName" label="上传人" width="100" />
        <el-table-column prop="uploadTime" label="上传时间" width="180" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link :icon="Download" @click="handleDownload(row)">下载</el-button>
            <el-button v-if="canDelete(row)" type="danger" link :icon="Delete" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="queryParams.current"
        v-model:page-size="queryParams.size"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadDocuments"
        @current-change="loadDocuments"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" title="上传文档" width="600px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="项目" prop="projectId">
          <el-select v-model="form.projectId" placeholder="选择项目" style="width: 100%" filterable>
            <el-option v-for="project in managerProjects" :key="project.id" :label="project.projectName" :value="project.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="文档类型" prop="docType">
          <el-select v-model="form.docType" placeholder="选择类型" style="width: 100%">
            <el-option label="功能清单" value="FUNCTION_LIST" />
            <el-option label="验收材料" value="ACCEPTANCE" />
            <el-option label="合同文档" value="CONTRACT" />
            <el-option label="技术方案" value="TECHNICAL" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="文档名称" prop="docName">
          <el-input v-model="form.docName" placeholder="请输入文档名称" />
        </el-form-item>
        <el-form-item label="选择文件" prop="file">
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :limit="1"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
            :file-list="fileList"
            accept=".pdf,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.zip,.rar,.jpg,.jpeg,.png,.txt">
            <el-button type="primary">选择文件</el-button>
            <template #tip>
              <div class="el-upload__tip">
                支持 PDF、Word、Excel、PPT、压缩包、图片等格式，最大50MB
              </div>
            </template>
          </el-upload>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定上传</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Download, Delete } from '@element-plus/icons-vue'
import { getDocumentList, uploadDocument, downloadDocument, deleteDocument } from '../api/document'
import request from '../utils/request'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const documentList = ref([])
const total = ref(0)
const formRef = ref()
const uploadRef = ref()
const fileList = ref([])

const projectOptions = ref([])
const managerProjects = ref([])
const currentUserId = ref(null)

const docTypes = {
  'FUNCTION_LIST': '功能清单',
  'ACCEPTANCE': '验收材料',
  'CONTRACT': '合同文档',
  'TECHNICAL': '技术方案',
  'OTHER': '其他'
}

const docTypeColors = {
  'FUNCTION_LIST': 'primary',
  'ACCEPTANCE': 'success',
  'CONTRACT': 'warning',
  'TECHNICAL': 'info',
  'OTHER': ''
}

const queryParams = reactive({
  projectId: null,
  docType: '',
  current: 1,
  size: 10
})

const form = reactive({
  projectId: null,
  docType: '',
  docName: '',
  file: null,
  remark: ''
})

const rules = {
  projectId: [{ required: true, message: '请选择项目', trigger: 'change' }],
  docType: [{ required: true, message: '请选择文档类型', trigger: 'change' }],
  docName: [{ required: true, message: '请输入文档名称', trigger: 'blur' }],
  file: [{ required: true, message: '请选择文件', trigger: 'change' }]
}

const canUpload = computed(() => {
  return managerProjects.value.length > 0
})

const canDelete = (row) => {
  // 只有项目经理可以删除
  const project = projectOptions.value.find(p => p.id === row.projectId)
  return project && project.projectManagerId === currentUserId.value
}

const formatFileSize = (bytes) => {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(2) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(2) + ' MB'
}

const loadProjects = async () => {
  try {
    const res = await request.get('/project/list', {
      params: { current: 1, size: 1000 }
    })
    if (res.code === 200) {
      projectOptions.value = res.data.records || []
      
      // 筛选当前用户为项目经理的项目
      managerProjects.value = projectOptions.value.filter(
        p => p.projectManagerId === currentUserId.value
      )
    }
  } catch (error) {
    ElMessage.error('加载项目列表失败')
  }
}

const loadCurrentUser = async () => {
  try {
    const res = await request.get('/user/info')
    if (res.code === 200) {
      currentUserId.value = res.data.id
    }
  } catch (error) {
    console.error('获取用户信息失败')
  }
}

const loadDocuments = async () => {
  loading.value = true
  try {
    const res = await getDocumentList(queryParams)
    if (res.code === 200) {
      documentList.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    ElMessage.error('加载文档列表失败')
  } finally {
    loading.value = false
  }
}

const handleUpload = () => {
  if (managerProjects.value.length === 0) {
    ElMessage.warning('您不是任何项目的项目经理，无法上传文档')
    return
  }
  dialogVisible.value = true
}

const handleFileChange = (file) => {
  form.file = file.raw
  fileList.value = [file]
}

const handleFileRemove = () => {
  form.file = null
  fileList.value = []
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    if (!form.file) {
      ElMessage.error('请选择文件')
      return
    }
    
    submitLoading.value = true
    try {
      const formData = new FormData()
      formData.append('file', form.file)
      formData.append('projectId', form.projectId)
      formData.append('docType', form.docType)
      formData.append('docName', form.docName)
      if (form.remark) {
        formData.append('remark', form.remark)
      }
      
      const res = await uploadDocument(formData)
      if (res.code === 200) {
        ElMessage.success('上传成功')
        dialogVisible.value = false
        loadDocuments()
      }
    } catch (error) {
      ElMessage.error('上传失败')
    } finally {
      submitLoading.value = false
    }
  })
}

const handleDownload = async (row) => {
  try {
    const res = await downloadDocument(row.id)
    const blob = new Blob([res])
    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    link.download = row.fileName
    link.click()
    URL.revokeObjectURL(link.href)
  } catch (error) {
    ElMessage.error('下载失败')
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确认删除该文档吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await deleteDocument(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadDocuments()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const resetForm = () => {
  Object.assign(form, {
    projectId: null,
    docType: '',
    docName: '',
    file: null,
    remark: ''
  })
  fileList.value = []
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

onMounted(async () => {
  await loadCurrentUser()
  await loadProjects()
  loadDocuments()
})
</script>

<style scoped>
.document-container {
  padding: 20px;
}

.toolbar {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}
</style>
```

- [ ] **Step 2: 提交**

```bash
git add frontend/src/views/Document.vue
git commit -m "feat: add document management page"
```

---

## Task 10: 添加路由配置

**Files:**
- Modify: `frontend/src/router/index.js`

- [ ] **Step 1: 在路由文件中添加文档管理路由**

找到路由配置数组，添加以下路由项：

```javascript
{
  path: '/document',
  name: 'Document',
  component: () => import('../views/Document.vue'),
  meta: { requiresAuth: true, title: '交付文档' }
}
```

- [ ] **Step 2: 验证前端编译**

```bash
cd frontend
npm run build
```

预期输出：Build completed

- [ ] **Step 3: 提交**

```bash
git add frontend/src/router/index.js
git commit -m "feat: add document route"
```

---

## Task 11: 配置文件和部署准备

**Files:**
- Modify: `src/main/resources/application.yml`

- [ ] **Step 1: 添加文档存储配置**

在application.yml中添加：

```yaml
# 文档存储配置
document:
  storage:
    path: /data/documents

# 文件上传配置
spring:
  servlet:
    multipart:
      enabled: true
      max-file-size: 50MB
      max-request-size: 50MB
```

- [ ] **Step 2: 创建存储目录**

```bash
sudo mkdir -p /data/documents
sudo chown -R $USER:$USER /data/documents
sudo chmod -R 755 /data/documents
```

- [ ] **Step 3: 提交**

```bash
git add src/main/resources/application.yml
git commit -m "config: add document storage configuration"
```

---

## Task 12: 集成测试

- [ ] **Step 1: 在数据库中执行建表SQL**

```bash
mysql -h 172.168.2.40 -u your_user -p your_database < docs/sql/project_document.sql
```

- [ ] **Step 2: 重新构建后端**

```bash
cd /root/xiangmu/java
mvn clean package -DskipTests
```

预期输出：BUILD SUCCESS

- [ ] **Step 3: 重启后端服务**

```bash
./deploy.sh
```

- [ ] **Step 4: 构建前端**

```bash
cd frontend
npm run build
```

- [ ] **Step 5: 部署前端**

```bash
sudo cp -r dist/* /opt/delivery-frontend/
sudo systemctl restart nginx
```

- [ ] **Step 6: 功能测试**

测试清单：
1. 访问 http://120.27.247.61，登录系统
2. 进入"交付文档"页面
3. 选择一个自己作为项目经理的项目
4. 上传一个测试文档（PDF或Word）
5. 验证文档列表显示正确
6. 下载刚上传的文档，验证内容正确
7. 上传同名文档，验证覆盖逻辑
8. 删除文档，验证删除成功
9. 切换到其他项目，验证权限控制

- [ ] **Step 7: 最终提交**

```bash
git add .
git commit -m "feat: complete document management module"
git push origin main
```

---

## 验收标准

- [ ] 数据库表创建成功
- [ ] 后端API全部可用
- [ ] 前端页面显示正常
- [ ] 可以上传文档（PDF、Word、Excel等）
- [ ] 可以下载文档
- [ ] 可以删除文档
- [ ] 文档列表可以按项目和类型筛选
- [ ] 只有项目经理可以上传和删除文档
- [ ] 上传同名文档时正确覆盖
- [ ] 文件大小和类型限制生效

---

## 后续优化建议

1. 添加在线预览功能（PDF、图片）
2. 支持批量下载（打包为ZIP）
3. 添加项目成员表，精细化权限控制
4. 添加文档访问日志
5. 考虑使用OSS代替本地存储

---

**计划完成时间**: 预计5个工作日
