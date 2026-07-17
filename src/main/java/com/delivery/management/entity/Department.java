package com.delivery.management.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.delivery.management.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_department")
public class Department extends BaseEntity {
    
    private String deptName;
    private Long parentId;
    private String deptCode;
    private Integer sort;
    private String leader;
    private String phone;
    private Integer status;
}
