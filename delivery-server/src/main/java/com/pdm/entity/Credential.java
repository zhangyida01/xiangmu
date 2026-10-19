package com.pdm.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_credential")
public class Credential extends BaseEntity {

    @NotNull(message = "所属项目不能为空")
    private Long projectId;

    @NotBlank(message = "平台/系统名称不能为空")
    private String platformName;

    private String credType;
    private String envType;
    private String url;
    private String host;
    private Integer port;
    private String username;
    private String secretType;

    /** AES-256-GCM 密文，任何接口都不会返回该字段 */
    @JsonIgnore
    private String secretCipher;

    private String secretHint;
    private String attachmentPath;
    private String attachmentName;
    private String owner;
    private LocalDate expireDate;
    private Integer rotateDays;
    private LocalDate lastRotateDate;
    private LocalDate nextRotateDate;
    private String strength;
    private Boolean favorite;
    private Boolean needChange;
    private String remark;
}