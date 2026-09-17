package com.kite.libai.system.domain.dto;

import java.io.Serializable;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 登录入参。
 *
 * @author kite
 */
public class LoginBody implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "登录账号不能为空")
    @Size(max = 30, message = "登录账号长度不能超过30个字符")
    private String userName;

    @NotBlank(message = "密码不能为空")
    @Size(max = 100, message = "密码长度不能超过100个字符")
    private String password;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
