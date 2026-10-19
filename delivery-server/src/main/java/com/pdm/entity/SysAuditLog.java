package com.pdm.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_audit_log")
public class SysAuditLog extends CreateTimeEntity {

    private Long userId;
    private String username;
    private String module;
    private String action;
    private String targetType;
    private Long targetId;
    private String targetName;
    private Long projectId;
    private String detail;
    private String ip;
    private String userAgent;
    private Boolean success;
    private String errMsg;
    private Integer costMs;
}