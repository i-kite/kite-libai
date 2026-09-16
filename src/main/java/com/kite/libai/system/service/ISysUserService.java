package com.kite.libai.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kite.libai.common.core.domain.PageResult;
import com.kite.libai.system.domain.SysUser;
import com.kite.libai.system.domain.dto.ResetPwdForm;
import com.kite.libai.system.domain.dto.UserForm;
import com.kite.libai.system.domain.dto.UserQuery;
import java.util.List;

/**
 * 用户管理 Service。
 *
 * @author kite
 */
public interface ISysUserService extends IService<SysUser> {

    /**
     * 用户分页列表,按部门筛选时包含其所有子孙部门的用户。
     *
     * @param query 查询条件
     */
    PageResult<SysUser> selectUserPage(UserQuery query);

    /**
     * 按ID查询用户详情,回填部门名称与已分配角色ID。
     *
     * @param userId 用户ID
     */
    SysUser selectUserById(Long userId);

    /**
     * 按登录账号查询用户,供后续接入登录鉴权时使用。
     *
     * @param userName 登录账号
     * @return 未找到时返回 null
     */
    SysUser selectUserByUserName(String userName);

    /**
     * 新增用户,密码以 BCrypt 加密存储,并按 roleIds 分配角色。
     *
     * @param form 用户表单
     * @return 新用户ID
     */
    Long insertUser(UserForm form);

    /**
     * 修改用户。登录账号不可变更;密码留空表示不修改。
     *
     * @param form 用户表单
     */
    void updateUser(UserForm form);

    /**
     * 批量删除用户。超级管理员账号不可删除,同时清理用户角色关联。
     *
     * @param userIds 用户ID集合
     */
    void deleteUsers(List<Long> userIds);

    /**
     * 重置密码。
     *
     * @param form 重置密码表单
     */
    void resetPassword(ResetPwdForm form);

    /**
     * 修改账号状态。超级管理员账号不可停用,避免把自己锁在系统外。
     *
     * @param userId 用户ID
     * @param status 目标状态:0-正常 1-停用
     */
    void updateUserStatus(Long userId, Integer status);

    /**
     * 判断用户是否为超级管理员。
     *
     * @param userId 用户ID
     */
    boolean isSuperAdmin(Long userId);
}
