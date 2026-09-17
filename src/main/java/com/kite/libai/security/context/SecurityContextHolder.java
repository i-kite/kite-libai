package com.kite.libai.security.context;

import com.kite.libai.common.constant.SysConstants;
import com.kite.libai.common.enums.ResultCode;
import com.kite.libai.common.exception.ServiceException;
import com.kite.libai.security.domain.LoginUser;

/**
 * 当前登录用户上下文。
 *
 * <p>由 {@code AuthInterceptor} 在 preHandle 中写入、afterCompletion 中清理。
 * 使用 ThreadLocal 保存,因此:
 * <ul>
 *   <li>异步线程、{@code @Async}、线程池中取不到值,需要显式传递</li>
 *   <li>拦截器必须保证清理,否则线程复用会导致上下文串号</li>
 * </ul>
 *
 * @author kite
 */
public final class SecurityContextHolder {

    private static final ThreadLocal<LoginUser> CONTEXT = new ThreadLocal<>();

    private SecurityContextHolder() {
    }

    public static void set(LoginUser loginUser) {
        CONTEXT.set(loginUser);
    }

    /**
     * 获取当前登录用户,未登录时返回 null。
     */
    public static LoginUser get() {
        return CONTEXT.get();
    }

    /**
     * 获取当前登录用户,未登录时抛出 401。
     */
    public static LoginUser requireLoginUser() {
        LoginUser loginUser = CONTEXT.get();
        if (loginUser == null) {
            throw new ServiceException(ResultCode.UNAUTHORIZED, "未登录或登录状态已过期");
        }
        return loginUser;
    }

    public static Long getUserIdOrNull() {
        LoginUser loginUser = CONTEXT.get();
        return loginUser == null ? null : loginUser.getUserId();
    }

    /**
     * 获取当前操作人账号,供审计字段填充使用。
     *
     * <p>无登录上下文时(例如定时任务、初始化脚本)回退为系统账号,
     * 保证审计字段始终有值。
     */
    public static String getOperator() {
        LoginUser loginUser = CONTEXT.get();
        return (loginUser == null || loginUser.getUserName() == null)
                ? SysConstants.DEFAULT_OPERATOR
                : loginUser.getUserName();
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
