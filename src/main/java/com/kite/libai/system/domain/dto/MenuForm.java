package com.kite.libai.system.domain.dto;

import java.io.Serializable;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 菜单(权限)新增/修改入参。
 *
 * @author kite
 */
public class MenuForm implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 菜单ID,新增时为空,修改时必填 */
    private Long menuId;

    @NotBlank(message = "菜单名称不能为空")
    @Size(max = 50, message = "菜单名称长度不能超过50个字符")
    private String menuName;

    @NotNull(message = "上级菜单不能为空")
    private Long parentId;

    @NotNull(message = "显示顺序不能为空")
    private Integer orderNum;

    @Size(max = 200, message = "路由地址长度不能超过200个字符")
    private String path;

    @Size(max = 255, message = "组件路径长度不能超过255个字符")
    private String component;

    /** 是否外链:0-是 1-否 */
    private Integer isFrame;

    @NotBlank(message = "菜单类型不能为空")
    @Pattern(regexp = "^[MCF]$", message = "菜单类型只能是 M-目录、C-菜单、F-按钮")
    private String menuType;

    /** 显示状态:0-显示 1-隐藏 */
    private Integer visible;

    /** 菜单状态:0-正常 1-停用 */
    private Integer status;

    @Size(max = 100, message = "权限标识长度不能超过100个字符")
    private String perms;

    @Size(max = 100, message = "菜单图标长度不能超过100个字符")
    private String icon;

    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;

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

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
