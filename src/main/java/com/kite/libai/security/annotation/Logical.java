package com.kite.libai.security.annotation;

/**
 * 多个权限标识之间的组合逻辑。
 *
 * @author kite
 */
public enum Logical {

    /** 必须同时拥有全部权限 */
    AND,

    /** 拥有其中任意一个即可 */
    OR
}
