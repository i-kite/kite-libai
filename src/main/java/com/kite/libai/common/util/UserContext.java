package com.kite.libai.common.util;

import com.kite.libai.common.constant.SysConstants;

/**
 * 当前操作人上下文。
 *
 * <p><b>注意:</b>本项目尚未接入登录鉴权,这里只是一个 ThreadLocal 占位实现,
 * 供审计字段自动填充使用。接入 JWT / Spring Security 后,应改为从安全上下文中读取
 * 当前登录用户,并在请求拦截器中调用 {@link #set(String)} 与 {@link #clear()}。
 *
 * @author kite
 */
public final class UserContext {

    private static final ThreadLocal<String> CURRENT_USER = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(String username) {
        CURRENT_USER.set(username);
    }

    /**
     * 获取当前操作人,未设置时返回默认值,保证审计字段始终有值。
     */
    public static String getUsername() {
        String username = CURRENT_USER.get();
        return (username == null || username.trim().isEmpty())
                ? SysConstants.DEFAULT_OPERATOR
                : username;
    }

    public static void clear() {
        CURRENT_USER.remove();
    }
}
