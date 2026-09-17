package com.kite.libai.security.interceptor;

import com.kite.libai.common.constant.SecurityConstants;
import com.kite.libai.common.enums.ResultCode;
import com.kite.libai.common.exception.ServiceException;
import com.kite.libai.security.annotation.Logical;
import com.kite.libai.security.annotation.RequiresLogin;
import com.kite.libai.security.annotation.RequiresPermissions;
import com.kite.libai.security.context.SecurityContextHolder;
import com.kite.libai.security.domain.LoginUser;
import com.kite.libai.system.service.ISysAuthService;
import java.util.Arrays;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 认证鉴权拦截器。
 *
 * <p>判定规则(方法上的注解优先于类上的注解):
 * <ol>
 *   <li>既没有 {@link RequiresLogin} 也没有 {@link RequiresPermissions} 的接口视为公开接口,
 *       直接放行。采用"默认公开、显式加固"而不是"默认拦截 + 白名单",是为了让每个接口的
 *       访问要求都写在接口自己身上,不必去另一个配置文件里对照白名单</li>
 *   <li>只有 {@link RequiresLogin}:校验访问令牌,不校验权限</li>
 *   <li>有 {@link RequiresPermissions}:校验访问令牌,再校验权限标识</li>
 * </ol>
 *
 * <p>校验失败时直接抛出 {@link ServiceException}。preHandle 抛出的异常会经过
 * Spring 的 HandlerExceptionResolver,因此仍由全局异常处理器统一转换为 R 结构,
 * 不需要在这里手工拼 JSON 响应。
 *
 * @author kite
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(AuthInterceptor.class);

    /**
     * 用 ObjectProvider 延迟获取,打断 拦截器 -> AuthService -> ... 的循环依赖风险。
     */
    private final ObjectProvider<ISysAuthService> authServiceProvider;

    public AuthInterceptor(ObjectProvider<ISysAuthService> authServiceProvider) {
        this.authServiceProvider = authServiceProvider;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod)) {
            // 静态资源、错误页等非控制器处理器
            return true;
        }
        HandlerMethod handlerMethod = (HandlerMethod) handler;

        RequiresPermissions requiresPermissions = resolvePermissions(handlerMethod);
        boolean requiresLogin = requiresPermissions != null || resolveLogin(handlerMethod);
        if (!requiresLogin) {
            return true;
        }

        LoginUser loginUser = authServiceProvider.getObject().loadLoginUser(resolveAccessToken(request));
        SecurityContextHolder.set(loginUser);

        if (requiresPermissions != null) {
            checkPermissions(loginUser, requiresPermissions, request);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        // 必须清理:Tomcat 线程会被复用,残留的上下文会让下一个请求拿到上一个用户
        SecurityContextHolder.clear();
    }

    private RequiresPermissions resolvePermissions(HandlerMethod handlerMethod) {
        RequiresPermissions annotation = handlerMethod.getMethodAnnotation(RequiresPermissions.class);
        return annotation != null
                ? annotation
                : handlerMethod.getBeanType().getAnnotation(RequiresPermissions.class);
    }

    private boolean resolveLogin(HandlerMethod handlerMethod) {
        return handlerMethod.getMethodAnnotation(RequiresLogin.class) != null
                || handlerMethod.getBeanType().getAnnotation(RequiresLogin.class) != null;
    }

    /**
     * 从 Authorization 头取出访问令牌。
     */
    private String resolveAccessToken(HttpServletRequest request) {
        String header = request.getHeader(SecurityConstants.TOKEN_HEADER);
        if (!StringUtils.hasText(header)) {
            throw new ServiceException(ResultCode.UNAUTHORIZED, "未登录,请先登录");
        }
        if (!header.startsWith(SecurityConstants.TOKEN_PREFIX)) {
            throw new ServiceException(ResultCode.UNAUTHORIZED, "令牌格式不正确,应为 Bearer <token>");
        }
        String token = header.substring(SecurityConstants.TOKEN_PREFIX.length()).trim();
        if (!StringUtils.hasText(token)) {
            throw new ServiceException(ResultCode.UNAUTHORIZED, "令牌不能为空");
        }
        return token;
    }

    private void checkPermissions(LoginUser loginUser, RequiresPermissions annotation, HttpServletRequest request) {
        String[] required = annotation.value();
        if (required.length == 0) {
            return;
        }

        boolean passed = annotation.logical() == Logical.AND
                ? Arrays.stream(required).allMatch(loginUser::hasPermission)
                : Arrays.stream(required).anyMatch(loginUser::hasPermission);

        if (!passed) {
            log.warn("权限不足: userId={} userName={} uri={} 需要权限={} 逻辑={}",
                    loginUser.getUserId(), loginUser.getUserName(), request.getRequestURI(),
                    Arrays.toString(required), annotation.logical());
            throw new ServiceException(ResultCode.FORBIDDEN, "没有操作权限");
        }
    }
}
