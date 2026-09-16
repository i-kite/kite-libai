package com.kite.libai.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kite.libai.common.core.domain.BaseEntity;
import java.util.ArrayList;
import java.util.List;

/**
 * 菜单权限实体,对应表 sys_menu。
 *
 * <p>菜单与权限在本系统中是同一张表:目录(M)与菜单(C)承载前端路由,
 * 按钮(F)承载细粒度操作权限,权限标识统一放在 {@code perms} 字段。
 *
 * @author kite
 */
@TableName("sys_menu")
public class SysMenu extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 菜单ID */
    @TableId(value = "menu_id", type = IdType.AUTO)
    private Long menuId;

    /** 菜单名称 */
    @TableField("menu_name")
    private String menuName;

    /** 父菜单ID,顶级为 0 */
    @TableField("parent_id")
    private Long parentId;

    /** 显示顺序 */
    @TableField("order_num")
    private Integer orderNum;

    /** 路由地址 */
    @TableField("path")
    private String path;

    /** 前端组件路径 */
    @TableField("component")
    private String component;

    /** 是否外链:0-是 1-否 */
    @TableField("is_frame")
    private Integer isFrame;

    /** 菜单类型:M-目录 C-菜单 F-按钮 */
    @TableField("menu_type")
    private String menuType;

    /** 显示状态:0-显示 1-隐藏 */
    @TableField("visible")
    private Integer visible;

    /** 菜单状态:0-正常 1-停用 */
    @TableField("status")
    private Integer status;

    /** 权限标识,例如 system:user:add */
    @TableField("perms")
    private String perms;

    /** 菜单图标 */
    @TableField("icon")
    private String icon;

    /** 子菜单列表,非数据库字段,仅在返回树形结构时填充 */
    @TableField(exist = false)
    private List<SysMenu> children = new ArrayList<>();

    public Long getMenuId() {
        return menuId;
    }

    public void setMenuId(Long menuId) {
        this.menuId = menuId;
    }

    public String getMenuName() {
        return menuName;
    }

    public void setMenuName(String menuName) {
        this.menuName = menuName;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Integer getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(Integer orderNum) {
        this.orderNum = orderNum;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public Integer getIsFrame() {
        return isFrame;
    }

    public void setIsFrame(Integer isFrame) {
        this.isFrame = isFrame;
    }

    public String getMenuType() {
        return menuType;
    }

    public void setMenuType(String menuType) {
        this.menuType = menuType;
    }

    public Integer getVisible() {
        return visible;
    }

    public void setVisible(Integer visible) {
        this.visible = visible;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getPerms() {
        return perms;
    }

    public void setPerms(String perms) {
        this.perms = perms;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public List<SysMenu> getChildren() {
        return children;
    }

    public void setChildren(List<SysMenu> children) {
        this.children = children;
    }
}
