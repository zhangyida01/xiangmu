package com.pdm.controller;

import com.pdm.common.ApiResult;
import com.pdm.common.PageResult;
import com.pdm.common.Perm;
import com.pdm.common.RequiresPerm;
import com.pdm.entity.SysAuditLog;
import com.pdm.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
@RequiresPerm(perms = {Perm.AUDIT_VIEW})
public class AuditController {

    private final AuditService auditService;

    @GetMapping
    public ApiResult<PageResult<SysAuditLog>> page(@RequestParam(defaultValue = "1") long page,
                                                   @RequestParam(defaultValue = "20") long size,
                                                   @RequestParam(required = false) String module,
                                                   @RequestParam(required = false) String action,
                                                   @RequestParam(required = false) Long userId,
                                                   @RequestParam(required = false) Long projectId,
                                                   @RequestParam(required = false) Boolean success,
                                                   @RequestParam(required = false) String keyword,
                                                   @RequestParam(required = false) String startTime,
                                                   @RequestParam(required = false) String endTime) {
        return ApiResult.ok(auditService.page(page, size, module, action, userId, projectId, success,
                keyword, startTime, endTime));
    }
}