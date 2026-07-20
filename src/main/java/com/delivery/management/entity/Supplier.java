package com.delivery.management.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("supplier")
public class Supplier {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String supplierCode;
    
    private String supplierName;
    
    private Integer supplierType;
    
    private String businessScope;
    
    private String address;
    
    private String website;
    
    private Integer status;
    
    private String remark;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
}
