package com.pdm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pdm.common.Audit;
import com.pdm.common.LoginUser;
import com.pdm.common.PageResult;
import com.pdm.common.UserContext;
import com.pdm.common.WebUtils;
import com.pdm.crypto.MaskUtil;
import com.pdm.entity.SysAuditLog;
import com.pdm.mapper.SysAuditLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

/** 审计日志：只写不改不删，敏感值一律脱敏 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private static final int DETAIL_MAX = 1800;

    private final SysAuditLogMapper auditLogMapper;
    private final ObjectMapper objectMapper;

    public void record(String module, String action, String targetType, Long targetId, String targetName,
                       Long projectId, String detail, boolean success, String errMsg) {
        try {
            SysAuditLog e = new SysAuditLog();
            LoginUser u = UserContext.get();
            e.setUserId(u == null ? null : u.getId());
            e.setUsername(u == null ? "anonymous" : u.getUsername());
            e.setModule(module);
            e.setAction(action);
            e.setTargetType(targetType);
            e.setTargetId(targetId);
            e.setTargetName(MaskUtil.truncate(targetName, 250));
            e.setProjectId(projectId);
            e.setDetail(MaskUtil.truncate(MaskUtil.maskJson(detail), DETAIL_MAX));
            e.setSuccess(success);
            e.setErrMsg(MaskUtil.truncate(errMsg, 480));
            HttpServletRequest req = currentRequest();
            e.setIp(WebUtils.clientIp(req));
            e.setUserAgent(WebUtils.userAgent(req));
            auditLogMapper.insert(e);
        } catch (Exception ex) {
            log.error("写审计日志失败 module={} action={} : {}", module, action, ex.getMessage());
        }
    }

    public void recordByAnnotation(Audit audit, Object[] args, boolean success, String errMsg, long costMs) {
        String detail = audit.recordArgs() ? safeSerialize(args) : audit.remark();
        SysAuditLog e = new SysAuditLog();
        LoginUser u = UserContext.get();
        try {
            e.setUserId(u == null ? null : u.getId());
            e.setUsername(u == null ? "anonymous" : u.getUsername());
            e.setModule(audit.module());
            e.setAction(audit.action());
            e.setTargetType(audit.targetType());
            e.setDetail(MaskUtil.truncate(MaskUtil.maskJson(detail), DETAIL_MAX));
            e.setSuccess(success);
            e.setErrMsg(MaskUtil.truncate(errMsg, 480));
            e.setCostMs((int) Math.min(costMs, Integer.MAX_VALUE));
            HttpServletRequest req = currentRequest();
            e.setIp(WebUtils.clientIp(req));
            e.setUserAgent(WebUtils.userAgent(req));
            auditLogMapper.insert(e);
        } catch (Exception ex) {
            log.error("写审计日志失败 module={} action={} : {}", audit.module(), audit.action(), ex.getMessage());
        }
    }

    public PageResult<SysAuditLog> page(long page, long size, String module, String action, Long userId,
                                        Long projectId, Boolean success, String keyword,
                                        String startTime, String endTime) {
        LambdaQueryWrapper<SysAuditLog> w = new LambdaQueryWrapper<>();
        w.eq(module != null && !module.isBlank(), SysAuditLog::getModule, module)
                .eq(action != null && !action.isBlank(), SysAuditLog::getAction, action)
                .eq(userId != null, SysAuditLog::getUserId, userId)
                .eq(projectId != null, SysAuditLog::getProjectId, projectId)
                .eq(success != null, SysAuditLog::getSuccess, success)
                .ge(startTime != null && !startTime.isBlank(), SysAuditLog::getCreateTime, startTime)
                .le(endTime != null && !endTime.isBlank(), SysAuditLog::getCreateTime, endTime + " 23:59:59")
                .and(keyword != null && !keyword.isBlank(), q -> q
                        .like(SysAuditLog::getUsername, keyword).or()
                        .like(SysAuditLog::getTargetName, keyword).or()
                        .like(SysAuditLog::getIp, keyword))
                .orderByDesc(SysAuditLog::getId);
        return PageResult.of(auditLogMapper.selectPage(new Page<>(page, size), w));
    }

    private HttpServletRequest currentRequest() {
        var attrs = RequestContextHolder.getRequestAttributes();
        return attrs instanceof ServletRequestAttributes sra ? sra.getRequest() : null;
    }

    private String safeSerialize(Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }
        List<Object> keep = new ArrayList<>();
        for (Object a : args) {
            if (a == null || a instanceof MultipartFile || a instanceof HttpServletRequest
                    || a instanceof jakarta.servlet.http.HttpServletResponse) {
                continue;
            }
            keep.add(a);
        }
        if (keep.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(keep);
        } catch (Exception e) {
            return keep.toString();
        }
    }
}