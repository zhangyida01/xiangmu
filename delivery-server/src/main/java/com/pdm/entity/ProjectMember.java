package com.pdm.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_project_member")
public class ProjectMember extends BaseEntity {

    @NotNull(message = "项目不能为空")
    private Long projectId;

    private String memberType;

    @NotBlank(message = "姓名不能为空")
    private String name;

    private String role;
    private String phone;
    private String email;
    private String wechat;
    private Boolean isMain;
    private String remark;
}