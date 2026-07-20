package com.delivery.management.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Date;

@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        // 支持Date类型
        this.strictInsertFill(metaObject, "createTime", Date.class, new Date());
        this.strictInsertFill(metaObject, "updateTime", Date.class, new Date());
        
        // 支持LocalDateTime类型
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        
        // 创建人ID（暂时使用固定值）
        this.strictInsertFill(metaObject, "createBy", Long.class, 1L);
        this.strictInsertFill(metaObject, "updateBy", Long.class, 1L);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        // 支持Date类型
        this.strictUpdateFill(metaObject, "updateTime", Date.class, new Date());
        
        // 支持LocalDateTime类型
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        
        // 更新人ID（暂时使用固定值）
        this.strictUpdateFill(metaObject, "updateBy", Long.class, 1L);
    }
}