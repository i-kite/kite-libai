package com.kite.libai.system.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kite.libai.common.constant.SysConstants;
import com.kite.libai.common.core.domain.PageResult;
import com.kite.libai.common.exception.ServiceException;
import com.kite.libai.system.domain.SysDept;
import com.kite.libai.system.domain.SysRole;
import com.kite.libai.system.domain.SysUser;
import com.kite.libai.system.domain.SysUserRole;
import com.kite.libai.system.domain.dto.ResetPwdForm;
import com.kite.libai.system.domain.dto.UserForm;
import com.kite.libai.system.domain.dto.UserQuery;
import com.kite.libai.system.mapper.SysDeptMapper;
import com.kite.libai.system.mapper.SysRoleMapper;
import com.kite.libai.system.mapper.SysUserMapper;
import com.kite.libai.system.mapper.SysUserRoleMapper;
import com.kite.libai.system.service.ISysUserService;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * 用户管理 Service 实现。
 *
 * @author kite
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

    private final SysUserRoleMapper sysUserRoleMapper;

    private final SysRoleMapper sysRoleMapper;

    private final SysDeptMapper sysDeptMapper;

    private final PasswordEncoder passwordEncoder;

    @Autowired
    public SysUserServiceImpl(SysUserRoleMapper sysUserRoleMapper,
                              SysRoleMapper sysRoleMapper,
                              SysDeptMapper sysDeptMapper,
                              PasswordEncoder passwordEncoder) {
        this.sysUserRoleMapper = sysUserRoleMapper;
        this.sysRoleMapper = sysRoleMapper;
        this.sysDeptMapper = sysDeptMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public PageResult<SysUser> selectUserPage(UserQuery query) {
        IPage<SysUser> page = baseMapper.selectUserPage(query.toPage(), query);
        return PageResult.of(page);
    }

    @Override
    public SysUser selectUserById(Long userId) {
        SysUser user = getById(userId);
        if (user == null) {
            throw ServiceException.notFound("用户不存在或已被删除");
        }
        if (user.getDeptId() != null) {
            SysDept dept = sysDeptMapper.selectById(user.getDeptId());
            user.setDeptName(dept == null ? null : dept.getDeptName());
        }
        user.setRoleIds(selectRoleIdsByUserId(userId));
        return user;
    }

    @Override
    public SysUser selectUserByUserName(String userName) {
        return getOne(Wrappers.<SysUser>lambdaQuery().eq(SysUser::getUserName, userName), false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long insertUser(UserForm form) {
        if (!StringUtils.hasText(form.getPassword())) {
            throw ServiceException.badRequest("新增用户时密码不能为空");
        }
        checkUserNameUnique(form.getUserName(), null);
        checkPhoneUnique(form.getPhonenumber(), null);
        checkEmailUnique(form.getEmail(), null);
        checkDeptAvailable(form.getDeptId());

        SysUser user = new SysUser();
        copyFormToEntity(form, user);
        user.setUserId(null);
        user.setUserName(form.getUserName());
        user.setPassword(passwordEncoder.encode(form.getPassword()));
        if (user.getStatus() == null) {
            user.setStatus(SysConstants.STATUS_NORMAL);
        }
        if (!StringUtils.hasText(user.getSex())) {
            user.setSex("2");
        }
        save(user);

        assignRoles(user.getUserId(), form.getRoleIds());
        return user.getUserId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(UserForm form) {
        if (form.getUserId() == null) {
            throw ServiceException.badRequest("用户ID不能为空");
        }
        selectUserByIdOrThrow(form.getUserId());

        checkPhoneUnique(form.getPhonenumber(), form.getUserId());
        checkEmailUnique(form.getEmail(), form.getUserId());
        checkDeptAvailable(form.getDeptId());

        SysUser user = new SysUser();
        copyFormToEntity(form, user);
        user.setUserId(form.getUserId());
        // 登录账号一经创建不可变更,这里显式置空,防止表单里的值被写入
        user.setUserName(null);
        // 密码留空表示不修改,避免每次编辑用户都被迫重置密码
        if (StringUtils.hasText(form.getPassword())) {
            user.setPassword(passwordEncoder.encode(form.getPassword()));
        }
        updateById(user);

        assignRoles(form.getUserId(), form.getRoleIds());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUsers(List<Long> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            throw ServiceException.badRequest("请选择要删除的用户");
        }
        for (Long userId : userIds) {
            selectUserByIdOrThrow(userId);
            if (isSuperAdmin(userId)) {
                throw ServiceException.badRequest("超级管理员账号不允许删除");
            }
        }
        removeByIds(userIds);
        sysUserRoleMapper.delete(Wrappers.<SysUserRole>lambdaQuery().in(SysUserRole::getUserId, userIds));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(ResetPwdForm form) {
        selectUserByIdOrThrow(form.getUserId());

        SysUser update = new SysUser();
        update.setUserId(form.getUserId());
        update.setPassword(passwordEncoder.encode(form.getPassword()));
        updateById(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserStatus(Long userId, Integer status) {
        if (status == null
                || (!SysConstants.STATUS_NORMAL.equals(status) && !SysConstants.STATUS_DISABLED.equals(status))) {
            throw ServiceException.badRequest("状态取值只能是 0-正常 或 1-停用");
        }
        selectUserByIdOrThrow(userId);
        if (isSuperAdmin(userId) && SysConstants.STATUS_DISABLED.equals(status)) {
            throw ServiceException.badRequest("超级管理员账号不允许停用");
        }

        SysUser update = new SysUser();
        update.setUserId(userId);
        update.setStatus(status);
        updateById(update);
    }

    @Override
    public boolean isSuperAdmin(Long userId) {
        return sysRoleMapper.selectRolesByUserId(userId).stream()
                .map(SysRole::getRoleKey)
                .anyMatch(SysConstants.SUPER_ADMIN_ROLE_KEY::equals);
    }

    /**
     * 重新分配用户角色,覆盖语义与角色授权菜单保持一致。
     *
     * @param roleIds null 表示不变更,空集合表示清空全部角色
     */
    private void assignRoles(Long userId, List<Long> roleIds) {
        if (roleIds == null) {
            return;
        }
        sysUserRoleMapper.delete(Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getUserId, userId));
        if (roleIds.isEmpty()) {
            return;
        }

        List<Long> distinctIds = new ArrayList<>(new LinkedHashSet<>(roleIds));
        long existCount = sysRoleMapper.selectCount(
                Wrappers.<SysRole>lambdaQuery().in(SysRole::getRoleId, distinctIds));
        if (existCount != distinctIds.size()) {
            throw ServiceException.badRequest("分配的角色中存在不存在或已删除的角色");
        }
        for (Long roleId : distinctIds) {
            sysUserRoleMapper.insert(new SysUserRole(userId, roleId));
        }
    }

    private List<Long> selectRoleIdsByUserId(Long userId) {
        return sysUserRoleMapper
                .selectList(Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getUserId, userId))
                .stream()
                .map(SysUserRole::getRoleId)
                .collect(Collectors.toList());
    }

    /**
     * 仅做存在性校验,不做部门名与角色的额外查询,供写操作前置检查使用。
     */
    private SysUser selectUserByIdOrThrow(Long userId) {
        SysUser user = getById(userId);
        if (user == null) {
            throw ServiceException.notFound("用户不存在或已被删除");
        }
        return user;
    }

    private void checkDeptAvailable(Long deptId) {
        if (deptId == null) {
            return;
        }
        SysDept dept = sysDeptMapper.selectById(deptId);
        if (dept == null) {
            throw ServiceException.badRequest("所属部门不存在或已被删除");
        }
        if (SysConstants.STATUS_DISABLED.equals(dept.getStatus())) {
            throw ServiceException.badRequest("所属部门已停用,不允许分配用户");
        }
    }

    private void checkUserNameUnique(String userName, Long excludeUserId) {
        long count = count(Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getUserName, userName)
                .ne(excludeUserId != null, SysUser::getUserId, excludeUserId));
        if (count > 0) {
            throw ServiceException.badRequest("登录账号「" + userName + "」已存在");
        }
    }

    private void checkPhoneUnique(String phonenumber, Long excludeUserId) {
        if (!StringUtils.hasText(phonenumber)) {
            return;
        }
        long count = count(Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getPhonenumber, phonenumber)
                .ne(excludeUserId != null, SysUser::getUserId, excludeUserId));
        if (count > 0) {
            throw ServiceException.badRequest("手机号「" + phonenumber + "」已被其他用户使用");
        }
    }

    private void checkEmailUnique(String email, Long excludeUserId) {
        if (!StringUtils.hasText(email)) {
            return;
        }
        long count = count(Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getEmail, email)
                .ne(excludeUserId != null, SysUser::getUserId, excludeUserId));
        if (count > 0) {
            throw ServiceException.badRequest("邮箱「" + email + "」已被其他用户使用");
        }
    }

    private void copyFormToEntity(UserForm form, SysUser user) {
        user.setDeptId(form.getDeptId());
        user.setNickName(form.getNickName());
        user.setEmail(form.getEmail());
        user.setPhonenumber(form.getPhonenumber());
        user.setSex(form.getSex());
        user.setStatus(form.getStatus());
        user.setRemark(form.getRemark());
    }
}
