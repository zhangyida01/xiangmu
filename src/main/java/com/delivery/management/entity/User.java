package com.delivery.management.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.delivery.management.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class User extends BaseEntity {
    
    private String username;
    private String password;
    private String realName;
    private String email;
    private String phone;
    private Long deptId;
    private Integer status;
    private String avatar;
}
