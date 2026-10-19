package com.pdm.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_credential_access")
public class CredentialAccess extends CreateTimeEntity {

    private Long credentialId;
    private Long projectId;
    private Long userId;
    private String username;
    private String action;
    private String ip;
    private String userAgent;
    private Boolean recheckUsed;
    private Boolean success;
}