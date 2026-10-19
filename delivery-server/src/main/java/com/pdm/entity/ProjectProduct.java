package com.pdm.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_project_product")
public class ProjectProduct extends BaseEntity {

    @NotNull(message = "项目不能为空")
    private Long projectId;

    @NotBlank(message = "产品名称不能为空")
    private String productName;

    private String version;
    private String licenseNo;
    private String licenseType;
    private LocalDate licenseExpire;
    private String remark;
}