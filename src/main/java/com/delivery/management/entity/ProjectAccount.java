package com.delivery.management.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("project_account")
public class ProjectAccount {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long projectId;
    
    private Long supplierId;
    
    private String accountName;
    
    private String accountType;
    
    private String username;
    
    private String password;
    
    private String loginUrl;
    
    private String remark;
    
    private Long creatorId;
    
    @TableField(value = "creator_id", exist = false)
    private String creatorName;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
}