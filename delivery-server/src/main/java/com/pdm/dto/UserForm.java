package com.pdm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UserForm {

    private Long id;

    @NotBlank(message = "账号不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9_.-]{3,30}$", message = "账号为3-30位字母数字或 _.-")
    private String username;

    private String realName;

    /** 新建时必填；修改时留空表示不改密码 */
    private String password;

    @Pattern(regexp = "^(ADMIN|USER|VIEWER)$", message = "角色只能是 ADMIN/USER/VIEWER")
    private String role;

    private Boolean canViewSecret;
    private Boolean canExport;
    private Boolean canDownload;
    private Boolean canDeleteProject;
    private Boolean needRecheck;
    private Integer status;
    private String remark;
}