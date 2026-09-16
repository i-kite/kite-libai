package com.kite.libai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 密码编码配置。
 *
 * <p>仅引入 spring-security-crypto,不引入 spring-boot-starter-security,
 * 因此不会触发默认的登录鉴权自动配置,只借用其 BCrypt 实现。
 *
 * @author kite
 */
@Configuration
public class PasswordConfig {

    /** BCrypt 强度,10 是安全性与耗时的常用折中 */
    private static final int BCRYPT_STRENGTH = 10;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(BCRYPT_STRENGTH);
    }
}
