package com.kite.libai.security.token;

/**
 * 令牌类型。
 *
 * <p>双令牌方案的关键在于类型隔离:两种令牌都是 JWT,若不加类型标记,
 * 长有效期的 refreshToken 就能直接当作 accessToken 访问业务接口,
 * 短有效期的设计意义随之失效。
 *
 * @author kite
 */
public enum TokenType {

    /** 访问令牌:有效期短,用于访问业务接口 */
    ACCESS("access"),

    /** 刷新令牌:有效期长,只能用于换取新的访问令牌 */
    REFRESH("refresh");

    private final String value;

    TokenType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static TokenType of(String value) {
        for (TokenType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        return null;
    }
}
