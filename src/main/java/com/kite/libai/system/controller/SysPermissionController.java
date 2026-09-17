package com.kite.libai.system.controller;

import com.kite.libai.common.core.domain.R;
import com.kite.libai.security.annotation.RequiresPermissions;
import com.kite.libai.system.domain.vo.UserPermissionVO;
import com.kite.libai.system.service.ISysPermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 权限查询。
 *
 * <p>接入登录鉴权后,这里应改为读取当前登录用户,不再由前端传入 userId。
 *
 * @author kite
 */
@RestController
@RequestMapping(path = "/api/system/permission", produces = MediaType.APPLICATION_JSON_VALUE)
public class SysPermissionController {

    private final ISysPermissionService sysPermissionService;

    @Autowired
    public SysPermissionController(ISysPermissionService sysPermissionService) {
        this.sysPermissionService = sysPermissionService;
    }

    /**
     * 查询指定用户的角色、权限标识与菜单树。
     */
    @GetMapping("/{userId}")
    @RequiresPermissions("system:user:query")
    public R<UserPermissionVO> userPermission(@PathVariable Long userId) {
        return R.ok(sysPermissionService.getUserPermission(userId));
    }
}
