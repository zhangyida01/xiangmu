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