package com.kite.libai.security.crypto;

import at.favre.lib.crypto.bcrypt.BCrypt;
import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 基于 at.favre.lib:bcrypt 的密码编码器。
 *
 * <p>生成的密文为标准 BCrypt 格式({@code $2a$10$...}),与 Spring Security 的
 * BCryptPasswordEncoder 完全互通,因此初始化脚本中已有的密文无需重新生成。
 *
 * @author kite
 */
@Component
public class BcryptPasswordEncoder implements PasswordEncoder {

    /** BCrypt 代价因子,10 是安全性与登录耗时的常用折中 */
    private static final int COST = 10;

    @Override
    public String encode(String rawPassword) {
        if (rawPassword == null) {
            throw new IllegalArgumentException("明文密码不能为 null");
        }
        return BCrypt.withDefaults().hashToString(COST, rawPassword.toCharArray());
    }

    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || !StringUtils.hasText(encodedPassword)) {
            return false;
        }
        // verifyer 内部使用恒定时间比较
        return BCrypt.verifyer()
                .verify(rawPassword.getBytes(StandardCharsets.UTF_8),
                        encodedPassword.getBytes(StandardCharsets.UTF_8))
                .verified;
    }
}
