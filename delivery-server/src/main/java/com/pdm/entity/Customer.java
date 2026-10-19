package com.pdm.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_customer")
public class Customer extends BaseEntity {

    @NotBlank(message = "客户名称不能为空")
    private String name;

    private String shortName;
    private String industry;
    private String contactName;
    private String contactPhone;
    private String address;
    private String remark;
}