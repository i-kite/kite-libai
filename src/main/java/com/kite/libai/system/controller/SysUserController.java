package com.kite.libai.system.controller;

import com.kite.libai.common.core.domain.PageResult;
import com.kite.libai.common.core.domain.R;
import com.kite.libai.security.annotation.RequiresPermissions;
import com.kite.libai.system.domain.SysUser;
import com.kite.libai.system.domain.dto.ResetPwdForm;
import com.kite.libai.system.domain.dto.UserForm;
import com.kite.libai.system.domain.dto.UserQuery;
import com.kite.libai.system.service.ISysUserService;
import java.util.List;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户管理。
 *
 * @author kite
 */
@RestController
@RequestMapping(path = "/api/system/user", produces = MediaType.APPLICATION_JSON_VALUE)
public class SysUserController {

    private final ISysUserService sysUserService;

    @Autowired
    public SysUserController(ISysUserService sysUserService) {
        this.sysUserService = sysUserService;
    }

    /**
     * 用户分页列表。按 deptId 筛选时会连带查出其所有子孙部门的用户。
     */
    @GetMapping("/page")
    @RequiresPermissions("system:user:list")
    public R<PageResult<SysUser>> page(UserQuery query) {
        return R.ok(sysUserService.selectUserPage(query));
    }

    /**
     * 用户详情,含部门名称与已分配角色ID集合。
     */
    @GetMapping("/{userId}")
    @RequiresPermissions("system:user:query")
    public R<SysUser> detail(@PathVariable Long userId) {
        return R.ok(sysUserService.selectUserById(userId));
    }

    /**
     * 新增用户。
     */
    @PostMapping
    @RequiresPermissions("system:user:add")
    public R<Long> add(@RequestBody @Valid UserForm form) {
        return R.ok("新增成功", sysUserService.insertUser(form));
    }

    /**
     * 修改用户。登录账号不可变更,密码留空表示不修改。
     */
    @PutMapping
    @RequiresPermissions("system:user:edit")
    public R<Void> edit(@RequestBody @Valid UserForm form) {
        sysUserService.updateUser(form);
        return R.ok("修改成功", null);
    }

    /**
     * 修改账号状态。
     */
    @PutMapping("/{userId}/status")
    @RequiresPermissions("system:user:edit")
    public R<Void> changeStatus(@PathVariable Long userId, @RequestParam Integer status) {
        sysUserService.updateUserStatus(userId, status);
        return R.ok("状态修改成功", null);
    }

    /**
     * 重置密码。
     */
    @PutMapping("/resetPwd")
    @RequiresPermissions("system:user:resetPwd")
    public R<Void> resetPwd(@RequestBody @Valid ResetPwdForm form) {
        sysUserService.resetPassword(form);
        return R.ok("密码重置成功", null);
    }

    /**
     * 批量删除用户,路径参数支持逗号分隔,例如 /api/system/user/3,4
     */
    @DeleteMapping("/{userIds}")
    @RequiresPermissions("system:user:remove")
    public R<Void> remove(@PathVariable List<Long> userIds) {
        sysUserService.deleteUsers(userIds);
        return R.ok("删除成功", null);
    }
}
