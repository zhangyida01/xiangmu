package com.pdm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_document_version")
public class DocumentVersion extends CreateTimeEntity {

    private Long documentId;
    private String versionNo;
    private String fileName;
    private String filePath;
    private String fileExt;
    private Long fileSize;
    private String md5;
    private String description;
    private Long uploaderId;
    private String uploaderName;

    @TableLogic
    @TableField(select = false)
    private Integer deleted;
}