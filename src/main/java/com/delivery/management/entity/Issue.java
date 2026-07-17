package com.delivery.management.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.delivery.management.common.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("project_issue")
public class Issue extends BaseEntity {
    
    private Long projectId;
    private String issueCode;
    private String title;
    private String description;
    private Integer issueType;
    private Integer priority;
    private Integer status;
    private Long reporterId;
    private Long assigneeId;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date resolveTime;
}
