package com.kite.libai.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kite.libai.system.domain.SysMenu;
import com.kite.libai.system.domain.dto.MenuForm;
import java.util.List;

/**
 * 菜单权限管理 Service。
 *
 * @author kite
 */
public interface ISysMenuService extends IService<SysMenu> {

    /**
     * 查询菜单平铺列表。
     *
     * @param menuName 菜单名称,模糊匹配,可为空
     * @param status   菜单状态,可为空
     */
    List<SysMenu> selectMenuList(String menuName, Integer status);

    /**
     * 查询菜单树(含按钮),用于菜单管理页面与角色授权勾选。
     *
     * @param menuName 菜单名称,模糊匹配,可为空
     * @param status   菜单状态,可为空
     */
    List<SysMenu> selectMenuTree(String menuName, Integer status);

    /**
     * 按ID查询菜单,不存在时抛出业务异常。
     *
     * @param menuId 菜单ID
     */
    SysMenu selectMenuById(Long menuId);

    /**
     * 新增菜单。
     *
     * @param form 菜单表单
     * @return 新菜单ID
     */
    Long insertMenu(MenuForm form);

    /**
     * 修改菜单。
     *
     * @param form 菜单表单
     */
    void updateMenu(MenuForm form);

    /**
     * 删除菜单。存在下级菜单或已被角色引用时拒绝删除。
     *
     * @param menuId 菜单ID
     */
    void deleteMenu(Long menuId);
}
