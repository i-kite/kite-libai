package com.kite.libai.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kite.libai.system.domain.SysMenu;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 菜单权限 Mapper。
 *
 * @author kite
 */
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    /**
     * 查询指定用户拥有的权限标识集合。
     *
     * <p>链路为 用户 -> 用户角色 -> 角色菜单 -> 菜单,只统计启用状态的角色与菜单。
     *
     * @param userId 用户ID
     */
    List<String> selectPermsByUserId(@Param("userId") Long userId);

    /**
     * 查询指定用户有权访问的菜单(仅目录与菜单,不含按钮),用于构建前端路由。
     *
     * @param userId 用户ID
     */
    List<SysMenu> selectMenusByUserId(@Param("userId") Long userId);

    /**
     * 查询指定角色已授权的菜单ID集合。
     *
     * @param roleId 角色ID
     */
    List<Long> selectMenuIdsByRoleId(@Param("roleId") Long roleId);
}
