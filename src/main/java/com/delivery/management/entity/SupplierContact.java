package com.delivery.management.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("supplier_contact")
public class SupplierContact {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long supplierId;
    
    private String contactName;
    
    private String position;
    
    private String phone;
    
    private String email;
    
    private String wechat;
    
    private String qq;
    
    private Integer isPrimary;
    
    private String remark;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
}
