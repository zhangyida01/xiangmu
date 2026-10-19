package com.pdm.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注在 Controller 方法上，由 AuditAspect 自动写入 sys_audit_log。
 * 需要精确记录对象ID与名称时，改为在 Service 中直接调用 AuditService。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Audit {

    String module();

    String action();

    String targetType() default "";

    String remark() default "";

    boolean recordArgs() default true;
}