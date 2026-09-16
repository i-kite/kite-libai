package com.kite.libai.system.domain.dto;

import java.io.Serializable;
import java.util.List;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 用户新增/修改入参。
 *
 * @author kite
 */
public class UserForm implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户ID,新增时为空,修改时必填 */
    private Long userId;

    /** 所属部门ID */
    private Long deptId;

    /**
     * 登录账号。
     *
     * <p>修改时忽略本字段,账号一经创建不允许变更,避免历史审计数据对不上。
     */
    @NotBlank(message = "登录账号不能为空")
    @Size(min = 2, max = 30, message = "登录账号长度必须在2到30个字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "登录账号只能包含字母、数字、下划线和中划线")
    private String userName;

    @NotBlank(message = "用户昵称不能为空")
    @Size(max = 30, message = "用户昵称长度不能超过30个字符")
    private String nickName;

    @Email(message = "邮箱格式不正确")
    @Size(max = 50, message = "邮箱长度不能超过50个字符")
    private String email;

    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phonenumber;

    /** 性别:0-男 1-女 2-未知 */
    @Pattern(regexp = "^$|^[012]$", message = "性别取值只能是 0-男、1-女、2-未知")
    private String sex;

    /**
     * 明文密码,仅新增时必填,修改时留空表示不改密码。
     *
     * <p>服务端负责 BCrypt 加密,数据库不存明文。
     */
    @Size(min = 6, max = 20, message = "密码长度必须在6到20个字符之间")
    private String password;

    /** 账号状态:0-正常 1-停用 */
    private Integer status;

    /** 分配给该用户的角色ID集合 */
    private List<Long> roleIds;

    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhonenumber() {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public List<Long> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(List<Long> roleIds) {
        this.roleIds = roleIds;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
