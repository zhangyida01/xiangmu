package com.pdm.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_checklist_template")
public class ChecklistTemplate extends BaseEntity {

    @NotBlank(message = "项目类型不能为空")
    private String projectType;

    @NotBlank(message = "清单项名称不能为空")
    private String itemName;

    private Boolean required;
    private Integer sortNo;
    private Boolean enabled;
    private String remark;
}