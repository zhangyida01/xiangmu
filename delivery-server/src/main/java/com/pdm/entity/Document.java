package com.pdm.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_document")
public class Document extends BaseEntity {

    @NotNull(message = "项目不能为空")
    private Long projectId;

    private Long categoryId;
    private Long checklistItemId;

    @NotBlank(message = "文档名称不能为空")
    private String name;

    private String tags;
    private String secretLevel;
    private Long latestVersionId;
    private String latestVersionNo;
    private Long uploaderId;
    private String uploaderName;
    private Integer downloadCount;
    private Integer viewCount;
    private String remark;
}