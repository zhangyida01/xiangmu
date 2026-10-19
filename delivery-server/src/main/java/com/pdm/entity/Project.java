package com.pdm.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_project")
public class Project extends BaseEntity {

    @NotBlank(message = "项目编号不能为空")
    private String projectNo;

    @NotBlank(message = "项目名称不能为空")
    private String name;

    private Long customerId;
    private String projectType;
    private String status;
    private String health;
    private String manager;
    private String sales;
    private String deliveryOwner;
    private String contractNo;
    private BigDecimal amount;
    private String tags;
    private String summary;

    private LocalDate setupDate;
    private LocalDate startDate;
    private LocalDate planOnlineDate;
    private LocalDate actualOnlineDate;
    private LocalDate planAcceptDate;
    private LocalDate actualAcceptDate;
    private LocalDate archiveDate;
    private LocalDate warrantyEndDate;

    private Integer progress;
    private Integer docRate;
    private Boolean archived;
    private Boolean locked;
    private String remark;
}