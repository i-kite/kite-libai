package com.kite.libai.system.controller;

import com.kite.libai.common.core.domain.R;
import com.kite.libai.security.annotation.RequiresPermissions;
import com.kite.libai.system.domain.SysMenu;
import com.kite.libai.system.domain.dto.MenuForm;
import com.kite.libai.system.service.ISysMenuService;
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
 * 菜单权限管理。
 *
 * @author kite
 */
@RestController
@RequestMapping(path = "/api/system/menu", produces = MediaType.APPLICATION_JSON_VALUE)
public class SysMenuController {

    private final ISysMenuService sysMenuService;

    @Autowired
    public SysMenuController(ISysMenuService sysMenuService) {
        this.sysMenuService = sysMenuService;
    }

    /**
     * 菜单列表(平铺)。
     */
    @GetMapping("/list")
    @RequiresPermissions("system:menu:list")
    public R<List<SysMenu>> list(@RequestParam(required = false) String menuName,
                                 @RequestParam(required = false) Integer status) {
        return R.ok(sysMenuService.selectMenuList(menuName, status));
    }

    /**
     * 菜单树,含按钮节点,可直接用于角色授权勾选。
     */
    @GetMapping("/tree")
    @RequiresPermissions("system:menu:list")
    public R<List<SysMenu>> tree(@RequestParam(required = false) String menuName,
                                 @RequestParam(required = false) Integer status) {
        return R.ok(sysMenuService.selectMenuTree(menuName, status));
    }

    /**
     * 菜单详情。
     */
    @GetMapping("/{menuId}")
    @RequiresPermissions("system:menu:query")
    public R<SysMenu> detail(@PathVariable Long menuId) {
        return R.ok(sysMenuService.selectMenuById(menuId));
    }

    /**
     * 新增菜单。
     */
    @PostMapping
    @RequiresPermissions("system:menu:add")
    public R<Long> add(@RequestBody @Valid MenuForm form) {
        return R.ok("新增成功", sysMenuService.insertMenu(form));
    }

    /**
     * 修改菜单。
     */
    @PutMapping
    @RequiresPermissions("system:menu:edit")
    public R<Void> edit(@RequestBody @Valid MenuForm form) {
        sysMenuService.updateMenu(form);
        return R.ok("修改成功", null);
    }

    /**
     * 删除菜单。
     */
    @DeleteMapping("/{menuId}")
    @RequiresPermissions("system:menu:remove")
    public R<Void> remove(@PathVariable Long menuId) {
        sysMenuService.deleteMenu(menuId);
        return R.ok("删除成功", null);
    }
}
