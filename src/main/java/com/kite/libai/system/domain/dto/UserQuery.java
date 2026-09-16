package com.kite.libai.system.domain.dto;

import com.kite.libai.common.core.page.PageQuery;

/**
 * 用户分页查询条件。
 *
 * @author kite
 */
public class UserQuery extends PageQuery {

    private static final long serialVersionUID = 1L;

    /** 登录账号,模糊匹配 */
    private String userName;

    /** 用户昵称,模糊匹配 */
    private String nickName;

    /** 手机号码,模糊匹配 */
    private String phonenumber;

    /** 账号状态:0-正常 1-停用 */
    private Integer status;

    /**
     * 部门ID。
     *
     * <p>按部门筛选时会连带查出该部门所有子孙部门的用户,
     * 符合"点击树节点看整棵子树"的使用习惯。
     */
    private Long deptId;

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

    public String getPhonenumber() {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }
}
