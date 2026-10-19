package com.pdm.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_credential_history")
public class CredentialHistory extends CreateTimeEntity {

    private Long credentialId;
    private String changeType;
    private String fieldName;
    private String oldCipher;
    private String newCipher;
    private Long operatorId;
    private String operatorName;
    private String remark;
}