package com.pdm.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注在 Controller 类或方法上。
 * roles 非空时要求当前用户角色命中其一（ADMIN 恒通过）；
 * perms 非空时要求当前用户拥有全部能力（ADMIN 恒通过）。
 * 未标注该注解的接口只要求已登录。
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresPerm {

    String[] roles() default {};

    String[] perms() default {};
}