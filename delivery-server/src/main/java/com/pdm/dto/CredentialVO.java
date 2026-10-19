package com.pdm.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CredentialVO {

    private Long id;
    private Long projectId;
    private String projectName;
    private String platformName;
    private String credType;
    private String envType;
    private String url;
    private String host;
    private Integer port;
    private String username;
    private String usernameMasked;
    private String secretType;
    private boolean hasSecret;
    private String secretMasked;
    private String secretHint;
    private String strength;
    private boolean hasAttachment;
    private String attachmentName;
    private String owner;
    private LocalDate expireDate;
    private Integer rotateDays;
    private LocalDate lastRotateDate;
    private LocalDate nextRotateDate;
    private Integer daysLeft;
    private Boolean favorite;
    private Boolean needChange;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}