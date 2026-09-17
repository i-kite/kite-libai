package com.kite.libai.security.token;

import com.kite.libai.common.constant.SecurityConstants;
import com.kite.libai.common.enums.ResultCode;
import com.kite.libai.common.exception.ServiceException;
import com.kite.libai.config.SecurityProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.DecodingException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * JWT 签发与解析。
 *
 * <p>只负责令牌本身的加解密与类型校验,不涉及业务逻辑与存储。
 *
 * @author kite
 */
@Component
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    private final SecretKey secretKey;

    private final String issuer;

    private final Duration accessTokenTtl;

    private final Duration refreshTokenTtl;

    public JwtTokenProvider(SecurityProperties properties) {
        SecurityProperties.Jwt jwt = properties.getJwt();
        this.secretKey = resolveSecretKey(jwt.getSecret());
        this.issuer = jwt.getIssuer();
        this.accessTokenTtl = Duration.ofMinutes(jwt.getAccessTokenExpireMinutes());
        this.refreshTokenTtl = Duration.ofDays(jwt.getRefreshTokenExpireDays());
    }

    /**
     * 解析密钥。
     *
     * <p>支持两种形式:Base64 编码的密钥,或长度足够的普通字符串。
     * 未配置时随机生成,避免仓库里出现硬编码密钥这种等同于公开密钥的做法。
     */
    private SecretKey resolveSecretKey(String secret) {
        if (!StringUtils.hasText(secret)) {
            byte[] random = new byte[SecurityConstants.MIN_SECRET_BYTES];
            new SecureRandom().nextBytes(random);
            log.warn("未配置 kite.security.jwt.secret,已随机生成签名密钥。"
                    + "后果:应用重启后所有令牌立即失效,且多实例部署时令牌互不通用。"
                    + "生产环境请通过环境变量 JWT_SECRET 注入固定密钥(不少于 {} 字节)。",
                    SecurityConstants.MIN_SECRET_BYTES);
            return Keys.hmacShaKeyFor(random);
        }

        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secret);
        } catch (DecodingException | IllegalArgumentException ex) {
            // 不是合法 Base64,按普通字符串处理。
            // 注意 jjwt 抛的是 DecodingException,它继承 RuntimeException 而非
            // IllegalArgumentException,只 catch 后者会漏。
            keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        }
        if (keyBytes.length < SecurityConstants.MIN_SECRET_BYTES) {
            throw new IllegalStateException("kite.security.jwt.secret 长度不足,HS256 要求密钥不少于 "
                    + SecurityConstants.MIN_SECRET_BYTES + " 字节,当前为 " + keyBytes.length + " 字节");
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 签发令牌。
     *
     * @param userId    用户ID
     * @param userName  登录账号
     * @param tokenType 令牌类型
     * @return 令牌载荷与令牌串
     */
    public IssuedToken issue(Long userId, String userName, TokenType tokenType) {
        Instant now = Instant.now();
        Duration ttl = tokenType == TokenType.ACCESS ? accessTokenTtl : refreshTokenTtl;
        Instant expiresAt = now.plus(ttl);
        String tokenId = UUID.randomUUID().toString().replace("-", "");

        String token = Jwts.builder()
                .id(tokenId)
                .issuer(issuer)
                .subject(userName)
                .claim(SecurityConstants.CLAIM_USER_ID, userId)
                .claim(SecurityConstants.CLAIM_TOKEN_TYPE, tokenType.getValue())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(secretKey)
                .compact();

        return new IssuedToken(token, new TokenPayload(tokenId, userId, userName, tokenType, expiresAt), ttl);
    }

    /**
     * 解析并校验令牌,同时校验其类型是否符合预期。
     *
     * @param token    令牌串
     * @param expected 期望的令牌类型
     * @throws ServiceException 令牌无效、过期或类型不符时抛出,统一为 401
     */
    public TokenPayload parse(String token, TokenType expected) {
        Claims claims;
        try {
            claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .requireIssuer(issuer)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException ex) {
            throw new ServiceException(ResultCode.UNAUTHORIZED,
                    expected == TokenType.ACCESS ? "访问令牌已过期,请使用刷新令牌重新获取" : "刷新令牌已过期,请重新登录");
        } catch (JwtException | IllegalArgumentException ex) {
            throw new ServiceException(ResultCode.UNAUTHORIZED, "令牌无效");
        }

        TokenType actual = TokenType.of(claims.get(SecurityConstants.CLAIM_TOKEN_TYPE, String.class));
        if (actual != expected) {
            // 用 refreshToken 直接访问业务接口,或用 accessToken 调刷新接口,都会落到这里
            throw new ServiceException(ResultCode.UNAUTHORIZED, "令牌类型不正确");
        }

        Number userId = claims.get(SecurityConstants.CLAIM_USER_ID, Number.class);
        if (userId == null) {
            throw new ServiceException(ResultCode.UNAUTHORIZED, "令牌缺少用户标识");
        }
        return new TokenPayload(claims.getId(), userId.longValue(), claims.getSubject(),
                actual, claims.getExpiration().toInstant());
    }

    public Duration getAccessTokenTtl() {
        return accessTokenTtl;
    }

    public Duration getRefreshTokenTtl() {
        return refreshTokenTtl;
    }

    /**
     * 签发结果。
     */
    public static class IssuedToken {

        private final String token;

        private final TokenPayload payload;

        private final Duration ttl;

        public IssuedToken(String token, TokenPayload payload, Duration ttl) {
            this.token = token;
            this.payload = payload;
            this.ttl = ttl;
        }

        public String getToken() {
            return token;
        }

        public TokenPayload getPayload() {
            return payload;
        }

        public Duration getTtl() {
            return ttl;
        }
    }
}
