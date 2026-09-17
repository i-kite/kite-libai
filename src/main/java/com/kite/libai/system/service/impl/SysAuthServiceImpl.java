package com.kite.libai.system.service.impl;

import com.kite.libai.common.constant.SecurityConstants;
import com.kite.libai.common.constant.SysConstants;
import com.kite.libai.common.enums.ResultCode;
import com.kite.libai.common.exception.ServiceException;
import com.kite.libai.security.crypto.PasswordEncoder;
import com.kite.libai.security.domain.LoginUser;
import com.kite.libai.security.token.JwtTokenProvider;
import com.kite.libai.security.token.TokenPayload;
import com.kite.libai.security.token.TokenType;
import com.kite.libai.system.domain.SysRefreshToken;
import com.kite.libai.system.domain.SysUser;
import com.kite.libai.system.domain.dto.LoginBody;
import com.kite.libai.system.domain.vo.TokenPairVO;
import com.kite.libai.system.domain.vo.UserPermissionVO;
import com.kite.libai.system.mapper.SysUserMapper;
import com.kite.libai.system.service.ISysAuthService;
import com.kite.libai.system.service.ISysPermissionService;
import com.kite.libai.system.service.ISysUserService;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 认证 Service 实现。
 *
 * @author kite
 */
@Service
public class SysAuthServiceImpl implements ISysAuthService {

    private static final Logger log = LoggerFactory.getLogger(SysAuthServiceImpl.class);

    private final ISysUserService sysUserService;

    private final ISysPermissionService sysPermissionService;

    private final RefreshTokenStore refreshTokenStore;

    private final SysUserMapper sysUserMapper;

    private final PasswordEncoder passwordEncoder;

    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public SysAuthServiceImpl(ISysUserService sysUserService,
                              ISysPermissionService sysPermissionService,
                              RefreshTokenStore refreshTokenStore,
                              SysUserMapper sysUserMapper,
                              PasswordEncoder passwordEncoder,
                              JwtTokenProvider jwtTokenProvider) {
        this.sysUserService = sysUserService;
        this.sysPermissionService = sysPermissionService;
        this.refreshTokenStore = refreshTokenStore;
        this.sysUserMapper = sysUserMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TokenPairVO login(LoginBody loginBody, String loginIp) {
        SysUser user = sysUserService.selectUserByUserName(loginBody.getUserName());

        // 账号不存在与密码错误返回同一提示,避免接口被用来枚举有效账号
        if (user == null || !passwordEncoder.matches(loginBody.getPassword(), user.getPassword())) {
            log.warn("登录失败,账号或密码不正确: userName={}", loginBody.getUserName());
            throw new ServiceException(ResultCode.UNAUTHORIZED, "账号或密码不正确");
        }
        if (SysConstants.STATUS_DISABLED.equals(user.getStatus())) {
            throw new ServiceException(ResultCode.FORBIDDEN, "账号已停用,请联系管理员");
        }

        refreshTokenStore.deleteExpired(user.getUserId());
        recordLogin(user.getUserId(), loginIp);
        return issueTokenPair(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TokenPairVO refresh(String refreshToken) {
        TokenPayload payload = jwtTokenProvider.parse(refreshToken, TokenType.REFRESH);
        SysRefreshToken stored = refreshTokenStore.findByTokenId(payload.getTokenId());

        if (stored == null) {
            // 签名有效但白名单里没有:通常意味着已被登出或清理流程移除
            throw new ServiceException(ResultCode.UNAUTHORIZED, "刷新令牌已失效,请重新登录");
        }
        if (SecurityConstants.TOKEN_REVOKED.equals(stored.getRevoked())) {
            // 已撤销的令牌被再次使用,按令牌泄露处置:连带撤销该用户全部刷新令牌,
            // 迫使攻击者与真实用户都必须重新登录。
            //
            // 必须走独立事务:紧随其后抛出的 ServiceException 会让当前事务回滚,
            // 若撤销与抛异常同属一个事务,撤销结果会被一起回滚掉。
            log.warn("检测到已撤销的刷新令牌被重复使用,撤销该用户全部刷新令牌: userId={}", stored.getUserId());
            refreshTokenStore.revokeAllByUserIdInNewTransaction(stored.getUserId());
            throw new ServiceException(ResultCode.UNAUTHORIZED, "刷新令牌已失效,请重新登录");
        }

        SysUser user = sysUserService.getById(stored.getUserId());
        if (user == null) {
            throw new ServiceException(ResultCode.UNAUTHORIZED, "用户不存在或已被删除");
        }
        if (SysConstants.STATUS_DISABLED.equals(user.getStatus())) {
            throw new ServiceException(ResultCode.FORBIDDEN, "账号已停用,请联系管理员");
        }

        // 轮换:旧刷新令牌立即撤销,再签发新的一对
        refreshTokenStore.revokeById(stored.getId());
        return issueTokenPair(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void logout(Long userId) {
        if (userId != null) {
            refreshTokenStore.revokeAllByUserId(userId);
        }
    }

    @Override
    public LoginUser loadLoginUser(String accessToken) {
        TokenPayload payload = jwtTokenProvider.parse(accessToken, TokenType.ACCESS);

        SysUser user = sysUserService.getById(payload.getUserId());
        if (user == null) {
            throw new ServiceException(ResultCode.UNAUTHORIZED, "用户不存在或已被删除");
        }
        // 每次请求都重新查库校验状态与权限,代价是几次查询,收益是停用账号、
        // 调整授权后立即生效,不必等访问令牌自然过期。后续可在此处接入缓存。
        if (SysConstants.STATUS_DISABLED.equals(user.getStatus())) {
            throw new ServiceException(ResultCode.FORBIDDEN, "账号已停用,请联系管理员");
        }

        UserPermissionVO permission = sysPermissionService.getUserPermission(user.getUserId());

        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(user.getUserId());
        loginUser.setUserName(user.getUserName());
        loginUser.setNickName(user.getNickName());
        loginUser.setDeptId(user.getDeptId());
        loginUser.setRoles(permission.getRoles());
        loginUser.setPermissions(permission.getPermissions());
        return loginUser;
    }

    /**
     * 签发一对令牌,并把刷新令牌的 jti 写入白名单。
     */
    private TokenPairVO issueTokenPair(SysUser user) {
        JwtTokenProvider.IssuedToken access =
                jwtTokenProvider.issue(user.getUserId(), user.getUserName(), TokenType.ACCESS);
        JwtTokenProvider.IssuedToken refresh =
                jwtTokenProvider.issue(user.getUserId(), user.getUserName(), TokenType.REFRESH);

        refreshTokenStore.save(refresh.getPayload().getTokenId(), user.getUserId(),
                LocalDateTime.ofInstant(refresh.getPayload().getExpiresAt(), ZoneId.systemDefault()));

        TokenPairVO vo = new TokenPairVO();
        vo.setAccessToken(access.getToken());
        vo.setRefreshToken(refresh.getToken());
        vo.setTokenType(SecurityConstants.TOKEN_PREFIX.trim());
        vo.setAccessTokenExpiresIn(access.getTtl().getSeconds());
        vo.setRefreshTokenExpiresIn(refresh.getTtl().getSeconds());
        vo.setUserId(user.getUserId());
        vo.setUserName(user.getUserName());
        vo.setNickName(user.getNickName());
        return vo;
    }

    /**
     * 记录最后登录信息。
     *
     * <p>审计字段仍会被 MetaObjectHandler 自动填充(它作用于 MyBatis 层,走 Mapper
     * 也绕不开)。登录时尚未建立登录上下文,填入的操作人为 system,符合预期。
     */
    private void recordLogin(Long userId, String loginIp) {
        SysUser update = new SysUser();
        update.setUserId(userId);
        update.setLoginIp(loginIp);
        update.setLoginDate(LocalDateTime.now());
        sysUserMapper.updateById(update);
    }
}
