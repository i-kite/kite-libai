package com.kite.libai.common.constant;

/**
 * 认证鉴权相关常量。
 *
 * @author kite
 */
public final class SecurityConstants {

    /** 存放令牌的请求头名称 */
    public static final String TOKEN_HEADER = "Authorization";

    /** 令牌前缀 */
    public static final String TOKEN_PREFIX = "Bearer ";

    /** JWT 中标识令牌类型的自定义声明名 */
    public static final String CLAIM_TOKEN_TYPE = "typ";

    /** JWT 中存放用户ID的自定义声明名 */
    public static final String CLAIM_USER_ID = "uid";

    /** HS256 要求密钥长度不低于 256 位(32 字节) */
    public static final int MIN_SECRET_BYTES = 32;

    /** 刷新令牌未撤销 */
    public static final Integer TOKEN_ACTIVE = 0;

    /** 刷新令牌已撤销 */
    public static final Integer TOKEN_REVOKED = 1;

    private SecurityConstants() {
        throw new IllegalStateException("常量类禁止实例化");
    }
}
