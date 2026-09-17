package com.kite.libai;

import com.kite.libai.common.constant.SecurityConstants;
import com.kite.libai.security.token.JwtTokenProvider;
import com.kite.libai.security.token.TokenType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/**
 * 数据库集成测试基类。
 *
 * <p>说明:
 * <ul>
 *   <li>数据源为 H2 内存库(MySQL 兼容模式),建表与初始化数据直接复用生产脚本
 *       {@code db/mysql/schema.sql} 与 {@code db/mysql/data.sql},配置见
 *       {@code src/test/resources/application.yml}</li>
 *   <li>类上的 {@code @Transactional} 使每个测试方法结束后自动回滚,
 *       测试之间互不污染,因此可以放心增删改初始化数据</li>
 *   <li>所有子类共享同一份注解组合,Spring 测试上下文只会创建一次并被缓存复用</li>
 * </ul>
 *
 * @author kite
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public abstract class AbstractIntegrationTest {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    /**
     * 构造指定用户的 Authorization 请求头值。
     *
     * <p>业务接口已由 AuthInterceptor 拦截并校验权限,MockMvc 请求必须带上访问令牌。
     * 这里直接签发令牌而不走登录接口,是为了让被测目标保持是控制器本身,
     * 不因登录流程的问题连带影响一批用例。
     *
     * @param userId   用户ID
     * @param userName 登录账号
     */
    protected String bearerToken(Long userId, String userName) {
        return SecurityConstants.TOKEN_PREFIX
                + jwtTokenProvider.issue(userId, userName, TokenType.ACCESS).getToken();
    }

    /**
     * 超级管理员(admin,拥有全部权限)的 Authorization 头值。
     */
    protected String adminToken() {
        return bearerToken(1L, "admin");
    }

    /**
     * 普通用户 kite 的 Authorization 头值,仅有 system:user:list / system:user:query 权限。
     */
    protected String normalUserToken() {
        return bearerToken(2L, "kite");
    }
}
