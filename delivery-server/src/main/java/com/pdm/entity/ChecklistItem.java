package com.pdm.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_checklist_item")
public class ChecklistItem extends BaseEntity {

    private Long projectId;
    private Long templateId;
    private String itemName;
    private Boolean required;
    private String status;
    private Long documentId;
    private Integer sortNo;
    private String remark;
}