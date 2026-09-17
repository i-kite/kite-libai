package com.kite.libai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

/**
 * 认证鉴权配置,对应 application.yml 中的 {@code kite.security}。
 *
 * @author kite
 */
@ConfigurationProperties(prefix = "kite.security")
@ConstructorBinding
public class SecurityProperties {

    private final Jwt jwt;

    public SecurityProperties(Jwt jwt) {
        this.jwt = jwt == null ? new Jwt(null, null, null, null) : jwt;
    }

    public Jwt getJwt() {
        return jwt;
    }

    /**
     * JWT 相关配置。
     */
    public static class Jwt {

        /** 默认访问令牌有效期(分钟) */
        private static final long DEFAULT_ACCESS_MINUTES = 30L;

        /** 默认刷新令牌有效期(天) */
        private static final long DEFAULT_REFRESH_DAYS = 7L;

        private static final String DEFAULT_ISSUER = "kite-libai";

        /**
         * 签名密钥,建议通过环境变量 JWT_SECRET 注入,长度不少于 32 字节。
         *
         * <p>刻意不提供硬编码默认值:仓库里写死密钥等同于公开密钥。留空时应用会在
         * 启动时随机生成一个,代价是重启后旧令牌全部失效、且多实例之间令牌不通用。
         */
        private final String secret;

        private final Long accessTokenExpireMinutes;

        private final Long refreshTokenExpireDays;

        private final String issuer;

        public Jwt(String secret, Long accessTokenExpireMinutes, Long refreshTokenExpireDays, String issuer) {
            this.secret = secret;
            this.accessTokenExpireMinutes = accessTokenExpireMinutes == null
                    ? DEFAULT_ACCESS_MINUTES : accessTokenExpireMinutes;
            this.refreshTokenExpireDays = refreshTokenExpireDays == null
                    ? DEFAULT_REFRESH_DAYS : refreshTokenExpireDays;
            this.issuer = (issuer == null || issuer.trim().isEmpty()) ? DEFAULT_ISSUER : issuer;
        }

        public String getSecret() {
            return secret;
        }

        public Long getAccessTokenExpireMinutes() {
            return accessTokenExpireMinutes;
        }

        public Long getRefreshTokenExpireDays() {
            return refreshTokenExpireDays;
        }

        public String getIssuer() {
            return issuer;
        }
    }
}
