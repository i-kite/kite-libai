package com.kite.libai.security.crypto;

/**
 * 密码编码器。
 *
 * <p>自定义接口而非复用 Spring Security 的同名接口,目的是让项目完全不依赖
 * Spring Security,同时保留将来替换算法(如 Argon2)的余地。
 *
 * @author kite
 */
public interface PasswordEncoder {

    /**
     * 将明文密码编码为密文。
     *
     * @param rawPassword 明文密码
     * @return 密文
     */
    String encode(String rawPassword);

    /**
     * 校验明文密码与密文是否匹配。
     *
     * <p>实现必须使用恒定时间比较,避免通过响应耗时推测密码。
     *
     * @param rawPassword     明文密码
     * @param encodedPassword 数据库中存储的密文
     */
    boolean matches(String rawPassword, String encodedPassword);
}
