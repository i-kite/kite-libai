package com.kite.libai.system.domain.vo;

import java.io.Serializable;

/**
 * 双令牌签发结果。
 *
 * @author kite
 */
public class TokenPairVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 访问令牌,放入 Authorization 头访问业务接口 */
    private String accessToken;

    /** 刷新令牌,仅用于调用刷新接口换取新的访问令牌 */
    private String refreshToken;

    /** 令牌类型,固定 Bearer */
    private String tokenType;

    /** 访问令牌剩余有效秒数 */
    private long accessTokenExpiresIn;

    /** 刷新令牌剩余有效秒数 */
    private long refreshTokenExpiresIn;

    private Long userId;

    private String userName;

    private String nickName;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public long getAccessTokenExpiresIn() {
        return accessTokenExpiresIn;
    }

    public void setAccessTokenExpiresIn(long accessTokenExpiresIn) {
        this.accessTokenExpiresIn = accessTokenExpiresIn;
    }

    public long getRefreshTokenExpiresIn() {
        return refreshTokenExpiresIn;
    }

    public void setRefreshTokenExpiresIn(long refreshTokenExpiresIn) {
        this.refreshTokenExpiresIn = refreshTokenExpiresIn;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }
}
