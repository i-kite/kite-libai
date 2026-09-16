package com.kite.libai.common.constant;

/**
 * 系统通用常量。
 *
 * @author kite
 */
public final class SysConstants {

    /** 根节点父级 ID */
    public static final Long ROOT_PARENT_ID = 0L;

    /** 状态:正常 */
    public static final Integer STATUS_NORMAL = 0;

    /** 状态:停用 */
    public static final Integer STATUS_DISABLED = 1;

    /** 删除标记:存在 */
    public static final Integer NOT_DELETED = 0;

    /** 删除标记:已删除 */
    public static final Integer DELETED = 1;

    /** 菜单类型:目录 */
    public static final String MENU_TYPE_DIR = "M";

    /** 菜单类型:菜单 */
    public static final String MENU_TYPE_MENU = "C";

    /** 菜单类型:按钮 */
    public static final String MENU_TYPE_BUTTON = "F";

    /** 超级管理员角色标识,拥有全部权限 */
    public static final String SUPER_ADMIN_ROLE_KEY = "admin";

    /** 超级管理员权限通配符 */
    public static final String ALL_PERMISSION = "*:*:*";

    /** 祖级列表分隔符 */
    public static final String ANCESTORS_SEPARATOR = ",";

    /** 审计字段在无登录上下文时使用的默认操作人 */
    public static final String DEFAULT_OPERATOR = "system";

    private SysConstants() {
        throw new IllegalStateException("常量类禁止实例化");
    }
}
