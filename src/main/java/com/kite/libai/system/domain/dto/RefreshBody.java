package com.kite.libai.system.domain.dto;

import java.io.Serializable;
import javax.validation.constraints.NotBlank;

/**
 * 刷新令牌入参。
 *
 * <p>刷新令牌通过请求体传递而不是 Authorization 头,目的是与访问令牌的传递方式区分开,
 * 避免客户端把刷新令牌顺手带在每个业务请求上,扩大泄露面。
 *
 * @author kite
 */
public class RefreshBody implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "刷新令牌不能为空")
    private String refreshToken;

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
