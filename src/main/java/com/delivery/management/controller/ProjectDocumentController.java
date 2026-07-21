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
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.time.LocalDateTime;
import java.util.UUID;


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
}
        
        if (StringUtils.hasText(docType)) {
            wrapper.eq("doc_type", docType);
        
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
}
        
        wrapper.orderByDesc("upload_time");
        
        Page<ProjectDocument> result = projectDocumentService.page(page, wrapper);
        
        // 填充上传人姓名和项目名称
        result.getRecords().forEach(doc -> {
            if (doc.getUploadBy() != null) {
                User user = userService.getById(doc.getUploadBy());
                if (user != null) {
                    doc.setUploaderName(user.getRealName() != null ? user.getRealName() : user.getUsername());
                
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
}
            
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
}
            if (doc.getProjectId() != null) {
                Project project = projectService.getById(doc.getProjectId());
                if (project != null) {
                    doc.setProjectName(project.getProjectName());
                
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
}
            
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
}
        
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
});
        
        return Result.success(result);
    
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
}

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
}