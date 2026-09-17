package com.kite.libai.security.token;

import java.time.Instant;

/**
 * 令牌解析结果。
 *
 * @author kite
 */
public class TokenPayload {

    /** JWT 唯一标识(jti),refreshToken 靠它与数据库记录对应 */
    private final String tokenId;

    private final Long userId;

    private final String userName;

    private final TokenType tokenType;

    private final Instant expiresAt;

    public TokenPayload(String tokenId, Long userId, String userName, TokenType tokenType, Instant expiresAt) {
        this.tokenId = tokenId;
        this.userId = userId;
        this.userName = userName;
        this.tokenType = tokenType;
        this.expiresAt = expiresAt;
    }

    public String getTokenId() {
        return tokenId;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public TokenType getTokenType() {
        return tokenType;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }
}
