package com.kite.libai.system.service;

import com.kite.libai.security.domain.LoginUser;
import com.kite.libai.system.domain.dto.LoginBody;
import com.kite.libai.system.domain.vo.TokenPairVO;

/**
 * 认证 Service:登录、刷新、登出,以及把访问令牌还原成登录用户。
 *
 * @author kite
 */
public interface ISysAuthService {

    /**
     * 账号密码登录,成功后签发访问令牌与刷新令牌。
     *
     * @param loginBody 登录入参
     * @param loginIp   登录来源IP,记录到用户表
     */
    TokenPairVO login(LoginBody loginBody, String loginIp);

    /**
     * 用刷新令牌换取新的双令牌(刷新令牌轮换)。
     *
     * @param refreshToken 刷新令牌
     */
    TokenPairVO refresh(String refreshToken);

    /**
     * 登出,撤销该用户全部刷新令牌。
     *
     * <p>已签发的访问令牌因无状态而无法立即作废,最长在其剩余有效期内仍可使用,
     * 这是短有效期访问令牌方案的固有取舍。
     *
     * @param userId 用户ID
     */
    void logout(Long userId);

    /**
     * 校验访问令牌并加载当前登录用户(含角色与权限)。
     *
     * @param accessToken 访问令牌
     */
    LoginUser loadLoginUser(String accessToken);
}
