package com.pdm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pdm.entity.SysAuditLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysAuditLogMapper extends BaseMapper<SysAuditLog> {
}