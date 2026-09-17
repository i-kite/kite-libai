package com.kite.libai.security.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记接口需要登录,但不校验具体权限。
 *
 * <p>可标注在类上(整个控制器生效)或方法上(方法优先)。
 * 未标注本注解也未标注 {@link RequiresPermissions} 的接口视为公开接口。
 *
 * @author kite
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface RequiresLogin {
}
