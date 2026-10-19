package com.pdm.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_project_env")
public class ProjectEnv extends BaseEntity {

    @NotNull(message = "项目不能为空")
    private Long projectId;

    @NotBlank(message = "环境名称不能为空")
    private String envName;

    private String envType;
    private String accessUrl;
    private String cloudVendor;
    private String region;
    private String hostname;
    private String ip;
    private String publicIp;
    private String os;
    private String cpu;
    private Integer memoryGb;
    private Integer diskGb;
    private String deployType;
    private String remark;
}