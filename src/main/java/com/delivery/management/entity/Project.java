package com.delivery.management.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.delivery.management.common.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("delivery_project")
public class Project extends BaseEntity {
    
    private String projectCode;
    private String projectName;
    private Long customerId;
    private String contractNo;
    private BigDecimal contractAmount;
    
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date startDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date endDate;
    
    private Long projectManagerId;
    private Integer status;
    private String description;
}
