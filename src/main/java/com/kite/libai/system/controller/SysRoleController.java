package com.kite.libai.system.controller;

import com.kite.libai.common.core.domain.PageResult;
import com.kite.libai.common.core.domain.R;
import com.kite.libai.system.domain.SysRole;
import com.kite.libai.system.domain.dto.RoleForm;
import com.kite.libai.system.domain.dto.RoleQuery;
import com.kite.libai.system.service.ISysRoleService;
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
 * 角色管理。
 *
 * @author kite
 */
@RestController
@RequestMapping(path = "/api/system/role", produces = MediaType.APPLICATION_JSON_VALUE)
public class SysRoleController {

    private final ISysRoleService sysRoleService;

    @Autowired
    public SysRoleController(ISysRoleService sysRoleService) {
        this.sysRoleService = sysRoleService;
    }

    /**
     * 角色分页列表。查询条件通过 URL 参数绑定,例如 ?pageNum=1&pageSize=10&roleName=管理
     */
    @GetMapping("/page")
    public R<PageResult<SysRole>> page(RoleQuery query) {
        return R.ok(sysRoleService.selectRolePage(query));
    }

    /**
     * 全部启用角色,供用户分配角色下拉使用。
     */
    @GetMapping("/enabled")
    public R<List<SysRole>> enabled() {
        return R.ok(sysRoleService.selectEnabledRoles());
    }

    /**
     * 角色详情,含已授权菜单ID集合。
     */
    @GetMapping("/{roleId}")
    public R<SysRole> detail(@PathVariable Long roleId) {
        return R.ok(sysRoleService.selectRoleById(roleId));
    }

    /**
     * 新增角色。
     */
    @PostMapping
    public R<Long> add(@RequestBody @Valid RoleForm form) {
        return R.ok("新增成功", sysRoleService.insertRole(form));
    }

    /**
     * 修改角色。
     */
    @PutMapping
    public R<Void> edit(@RequestBody @Valid RoleForm form) {
        sysRoleService.updateRole(form);
        return R.ok("修改成功", null);
    }

    /**
     * 修改角色状态。
     */
    @PutMapping("/{roleId}/status")
    public R<Void> changeStatus(@PathVariable Long roleId, @RequestParam Integer status) {
        sysRoleService.updateRoleStatus(roleId, status);
        return R.ok("状态修改成功", null);
    }

    /**
     * 角色授权菜单,覆盖式提交:请求体为完整的菜单ID列表,空数组表示清空权限。
     */
    @PutMapping("/{roleId}/menus")
    public R<Void> assignMenus(@PathVariable Long roleId, @RequestBody List<Long> menuIds) {
        sysRoleService.assignMenus(roleId, menuIds);
        return R.ok("授权成功", null);
    }

    /**
     * 批量删除角色,路径参数支持逗号分隔,例如 /api/system/role/3,4
     */
    @DeleteMapping("/{roleIds}")
    public R<Void> remove(@PathVariable List<Long> roleIds) {
        sysRoleService.deleteRoles(roleIds);
        return R.ok("删除成功", null);
    }
}
