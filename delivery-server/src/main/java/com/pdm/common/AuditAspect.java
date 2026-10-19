package com.pdm.common;

import com.pdm.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditService auditService;

    @Around("@annotation(audit)")
    public Object around(ProceedingJoinPoint pjp, Audit audit) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            Object result = pjp.proceed();
            auditService.recordByAnnotation(audit, pjp.getArgs(), true, null, System.currentTimeMillis() - start);
            return result;
        } catch (Throwable e) {
            auditService.recordByAnnotation(audit, pjp.getArgs(), false, e.getMessage(), System.currentTimeMillis() - start);
            throw e;
        }
    }
}