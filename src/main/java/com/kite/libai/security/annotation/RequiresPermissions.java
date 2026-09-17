package com.kite.libai.security.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记接口所需的权限标识。
 *
 * <p>由 {@code AuthInterceptor} 在请求进入控制器前校验,隐含要求已登录。
 * 超级管理员(角色标识 admin)跳过校验。
 *
 * <pre>
 * &#64;RequiresPermissions("system:user:add")
 * &#64;RequiresPermissions(value = {"system:user:edit", "system:user:add"}, logical = Logical.OR)
 * </pre>
 *
 * @author kite
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface RequiresPermissions {

    /** 所需权限标识 */
    String[] value();

    /** 多个权限之间的组合逻辑,默认必须全部满足 */
    Logical logical() default Logical.AND;
}
