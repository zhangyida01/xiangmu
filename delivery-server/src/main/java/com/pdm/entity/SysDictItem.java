package com.pdm.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dict_item")
public class SysDictItem extends BaseEntity {

    private String dictType;
    private String itemCode;
    private String itemName;
    private Integer sortNo;
    private Boolean enabled;
    private String remark;
}