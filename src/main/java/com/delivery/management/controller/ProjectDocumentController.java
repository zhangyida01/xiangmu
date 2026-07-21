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
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
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
        }
        
        if (StringUtils.hasText(docType)) {
            wrapper.eq("doc_type", docType);
        }
        
        wrapper.orderByDesc("upload_time");
        
        Page<ProjectDocument> result = projectDocumentService.page(page, wrapper);
        
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

    @PostMapping("/upload")
    public Result<String> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("projectId") Long projectId,
            @RequestParam("docType") String docType,
            @RequestParam("docName") String docName,
            @RequestParam(required = false) String remark,
            HttpServletRequest request) {
        
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return Result.fail("未登录");
        }
        token = token.substring(7);
        Long userId = jwtUtil.getUserIdFromToken(token);
        
        Project project = projectService.getById(projectId);
        if (project == null) {
            return Result.fail("项目不存在");
        }
        if (!project.getProjectManagerId().equals(userId)) {
            return Result.fail("只有项目经理可以上传文档");
        }
        
        if (file.isEmpty()) {
            return Result.fail("文件不能为空");
        }
        
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            return Result.fail("文件名不能为空");
        }
        
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
        
        if (file.getSize() > 50 * 1024 * 1024) {
            return Result.fail("文件大小不能超过50MB");
        }
        
        QueryWrapper<ProjectDocument> wrapper = new QueryWrapper<>();
        wrapper.eq("project_id", projectId)
               .eq("doc_type", docType)
               .eq("doc_name", docName);
        ProjectDocument existDoc = projectDocumentService.getOne(wrapper);
        
        try {
            String basePath = "/data/documents";
            String docTypeFolder = docType.toLowerCase();
            String projectFolder = "project_" + projectId;
            
            File dir = new File(basePath, projectFolder + "/" + docTypeFolder);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
            String uuid = UUID.randomUUID().toString();
            String newFileName = uuid + "_" + originalFilename;
            String filePath = projectFolder + "/" + docTypeFolder + "/" + newFileName;
            
            File destFile = new File(basePath, filePath);
            file.transferTo(destFile);
            
            if (existDoc != null) {
                File oldFile = new File(basePath, existDoc.getFilePath());
                if (oldFile.exists()) {
                    oldFile.delete();
                }
                
                existDoc.setFileName(originalFilename);
                existDoc.setFilePath(filePath);
                existDoc.setFileSize(file.getSize());
                existDoc.setFileExt(fileExt);
                existDoc.setUploadBy(userId);
                existDoc.setUploadTime(LocalDateTime.now());
                existDoc.setRemark(remark);
                projectDocumentService.updateById(existDoc);
            } else {
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

    @GetMapping("/download/{id}")
    public void download(@PathVariable Long id, HttpServletResponse response, HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token == null || !token.startsWith("Bearer ")) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            token = token.substring(7);
            Long userId = jwtUtil.getUserIdFromToken(token);
            
            ProjectDocument doc = projectDocumentService.getById(id);
            if (doc == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            
            Project project = projectService.getById(doc.getProjectId());
            if (project == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            // 下载权限：允许所有登录用户下载
            // TODO: 后续可扩展检查项目成员权限
            
            String basePath = "/data/documents";
            File file = new File(basePath, doc.getFilePath());
            if (!file.exists()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", 
                "attachment; filename=" + URLEncoder.encode(doc.getFileName(), "UTF-8"));
            response.setContentLengthLong(file.length());
            
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

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return Result.fail("未登录");
        }
        token = token.substring(7);
        Long userId = jwtUtil.getUserIdFromToken(token);
        
        ProjectDocument doc = projectDocumentService.getById(id);
        if (doc == null) {
            return Result.fail("文档不存在");
        }
        
        Project project = projectService.getById(doc.getProjectId());
        if (project == null) {
            return Result.fail("项目不存在");
        }
        if (!project.getProjectManagerId().equals(userId)) {
            return Result.fail("只有项目经理可以删除文档");
        }
        
        String basePath = "/data/documents";
        File file = new File(basePath, doc.getFilePath());
        if (file.exists()) {
            file.delete();
        }
        
        boolean success = projectDocumentService.removeById(id);
        
        return success ? Result.success("删除成功") : Result.fail("删除失败");
    }

    @GetMapping("/{id}")
    public Result<ProjectDocument> detail(@PathVariable Long id) {
        ProjectDocument doc = projectDocumentService.getById(id);
        if (doc == null) {
            return Result.fail("文档不存在");
        }
        return Result.success(doc);
    }
}