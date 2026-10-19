package com.pdm.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {

    private String username;

    @JsonIgnore
    private String password;

    private String realName;

    private String role;

    private Boolean canViewSecret;

    private Boolean canExport;

    private Boolean canDownload;

    private Boolean canDeleteProject;

    private Boolean needRecheck;

    private Integer status;

    private Boolean mustChangePwd;

    private LocalDateTime pwdUpdateTime;

    private Integer loginFailCount;

    private LocalDateTime lockUntil;

    private LocalDateTime lastLoginTime;

    private String lastLoginIp;

    private String remark;
}