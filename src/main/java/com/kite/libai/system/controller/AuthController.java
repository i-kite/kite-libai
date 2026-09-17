package com.kite.libai.system.controller;

import com.kite.libai.common.core.domain.R;
import com.kite.libai.security.annotation.RequiresLogin;
import com.kite.libai.security.context.SecurityContextHolder;
import com.kite.libai.security.domain.LoginUser;
import com.kite.libai.system.domain.dto.LoginBody;
import com.kite.libai.system.domain.dto.RefreshBody;
import com.kite.libai.system.domain.vo.TokenPairVO;
import com.kite.libai.system.domain.vo.UserPermissionVO;
import com.kite.libai.system.service.ISysAuthService;
import com.kite.libai.system.service.ISysPermissionService;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证:登录、刷新令牌、登出、获取当前登录用户信息。
 *
 * <p>登录与刷新接口不加权限注解,属于公开接口;其余接口要求已登录。
 *
 * @author kite
 */
@RestController
@RequestMapping(path = "/api/auth", produces = MediaType.APPLICATION_JSON_VALUE)
public class AuthController {

    private final ISysAuthService sysAuthService;

    private final ISysPermissionService sysPermissionService;

    @Autowired
    public AuthController(ISysAuthService sysAuthService, ISysPermissionService sysPermissionService) {
        this.sysAuthService = sysAuthService;
        this.sysPermissionService = sysPermissionService;
    }

    /**
     * 账号密码登录,返回 accessToken 与 refreshToken。
     */
    @PostMapping("/login")
    public R<TokenPairVO> login(@RequestBody @Valid LoginBody loginBody, HttpServletRequest request) {
        return R.ok("登录成功", sysAuthService.login(loginBody, resolveClientIp(request)));
    }

    /**
     * 用 refreshToken 换取新的双令牌。
     *
     * <p>采用轮换策略:每次刷新都会签发新的 refreshToken 并撤销旧的,
     * 因此客户端必须用返回值覆盖本地保存的两个令牌。
     */
    @PostMapping("/refresh")
    public R<TokenPairVO> refresh(@RequestBody @Valid RefreshBody refreshBody) {
        return R.ok("刷新成功", sysAuthService.refresh(refreshBody.getRefreshToken()));
    }

    /**
     * 登出,撤销当前用户的全部 refreshToken。
     */
    @PostMapping("/logout")
    @RequiresLogin
    public R<Void> logout() {
        sysAuthService.logout(SecurityContextHolder.requireLoginUser().getUserId());
        return R.ok("登出成功", null);
    }

    /**
     * 当前登录用户的角色、权限标识与菜单树,供前端渲染菜单与按钮。
     */
    @GetMapping("/info")
    @RequiresLogin
    public R<UserPermissionVO> info() {
        LoginUser loginUser = SecurityContextHolder.requireLoginUser();
        return R.ok(sysPermissionService.getUserPermission(loginUser.getUserId()));
    }

    /**
     * 取客户端IP。
     *
     * <p>注意:X-Forwarded-For 可被客户端伪造,只有在可信反向代理会重写该头的部署下才可靠。
     * 这里仅用于登录日志展示,不参与任何安全判断。
     */
    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwarded)) {
            int comma = forwarded.indexOf(',');
            return comma > 0 ? forwarded.substring(0, comma).trim() : forwarded.trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        return StringUtils.hasText(realIp) ? realIp.trim() : request.getRemoteAddr();
    }
}
