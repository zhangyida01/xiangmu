package com.pdm.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_doc_category")
public class DocCategory extends BaseEntity {

    private Long parentId;
    private String code;

    @NotBlank(message = "分类名称不能为空")
    private String name;

    private Integer sortNo;
    private Boolean enabled;
}