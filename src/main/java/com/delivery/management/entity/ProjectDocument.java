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