package com.delivery.management.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.delivery.management.common.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("project_task")
public class Task extends BaseEntity {
    
    private Long projectId;
    private Long parentId;
    private String taskName;
    private Long assigneeId;
    
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date startDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date endDate;
    
    private Integer progress;
    private Integer status;
    private Integer priority;
    private String description;
}
