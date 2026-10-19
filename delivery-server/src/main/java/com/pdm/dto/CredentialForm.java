package com.pdm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CredentialForm {

    private Long id;

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

    /** 明文密码/密钥/Token；修改时留空表示不改动 */
    private String secret;

    private String owner;
    private LocalDate expireDate;
    private Integer rotateDays;
    private LocalDate lastRotateDate;
    private Boolean favorite;
    private Boolean needChange;
    private String remark;
}