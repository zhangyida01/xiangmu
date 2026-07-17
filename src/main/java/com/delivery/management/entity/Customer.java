package com.delivery.management.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.delivery.management.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("customer")
public class Customer extends BaseEntity {
    
    private String customerName;
    private String customerCode;
    private String contactPerson;
    private String contactPhone;
    private String contactEmail;
    private String address;
    private Integer status;
}
