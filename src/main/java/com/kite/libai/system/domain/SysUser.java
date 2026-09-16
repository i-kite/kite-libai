package com.kite.libai.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.kite.libai.common.core.domain.BaseEntity;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户实体,对应表 sys_user。
 *
 * @author kite
 */
@TableName("sys_user")
public class SysUser extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 用户ID */
    @TableId(value = "user_id", type = IdType.AUTO)
    private Long userId;

    /** 所属部门ID */
    @TableField("dept_id")
    private Long deptId;

    /** 登录账号 */
    @TableField("user_name")
    private String userName;

    /** 用户昵称 */
    @TableField("nick_name")
    private String nickName;

    /** 邮箱 */
    @TableField("email")
    private String email;

    /** 手机号码 */
    @TableField("phonenumber")
    private String phonenumber;

    /** 性别:0-男 1-女 2-未知 */
    @TableField("sex")
    private String sex;

    /** 头像地址 */
    @TableField("avatar")
    private String avatar;

    /**
     * 密码(BCrypt 密文)。
     *
     * <p>标注 {@link JsonIgnore} 确保密文永不出现在任何接口响应中;
     * 新增/修改用户的明文密码通过 {@code UserForm} 传入,不经过本字段反序列化。
     */
    @JsonIgnore
    @TableField("password")
    private String password;

    /** 账号状态:0-正常 1-停用 */
    @TableField("status")
    private Integer status;

    /** 最后登录IP */
    @TableField("login_ip")
    private String loginIp;

    /** 最后登录时间 */
    @TableField("login_date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime loginDate;

    /** 所属部门名称,非数据库字段,列表查询时由关联查询填充 */
    @TableField(exist = false)
    private String deptName;

    /** 用户拥有的角色ID集合,非数据库字段,查询详情时填充 */
    @TableField(exist = false)
    private List<Long> roleIds;

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

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
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

    public String getLoginIp() {
        return loginIp;
    }

    public void setLoginIp(String loginIp) {
        this.loginIp = loginIp;
    }

    public LocalDateTime getLoginDate() {
        return loginDate;
    }

    public void setLoginDate(LocalDateTime loginDate) {
        this.loginDate = loginDate;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public List<Long> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(List<Long> roleIds) {
        this.roleIds = roleIds;
    }
}
