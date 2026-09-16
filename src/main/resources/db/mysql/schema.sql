-- ----------------------------------------------------------------------------
-- 权限管理系统 建表脚本(MySQL 8.0+ / 9.x)
--
-- 约定:
--   1. 主键统一 bigint 自增,命名为 {表名去前缀}_id
--   2. 所有业务表带 create_by / create_time / update_by / update_time / remark 审计字段
--   3. 主数据表带 del_flag 逻辑删除标记(0-存在 1-已删除),关联表用物理删除
--   4. status 统一 0-正常 1-停用
--   5. 整数类型不写显示宽度。MySQL 8.0.17 起 bigint(20) / int(4) / tinyint(1)
--      这类写法会触发 "Integer display width is deprecated"(错误码 1681)告警,
--      且宽度本身会被丢弃,未来版本将直接移除该语法
--
-- 本脚本同时兼容 H2 的 MySQL 模式(MODE=MySQL),测试用内存库直接复用,
-- 避免维护两套 DDL 导致测试库与生产库结构漂移。
--
-- 执行方式:
--   mysql --default-character-set=utf8mb4 -uroot -p kite_libai < schema.sql
-- 下面的 SET NAMES 是为了在漏加 --default-character-set 时兜底。若客户端字符集
-- 为 latin1,MySQL 会把 UTF-8 字节当 latin1 再转码,导致中文被双重编码存成乱码。
-- 该语句在 H2 的 MySQL 模式下会被当作空操作,不影响测试。
-- ----------------------------------------------------------------------------

SET NAMES utf8mb4;

-- ----------------------------
-- 1、部门表
-- ----------------------------
DROP TABLE IF EXISTS sys_dept;
CREATE TABLE sys_dept
(
    dept_id     bigint       NOT NULL AUTO_INCREMENT COMMENT '部门ID',
    parent_id   bigint       NOT NULL DEFAULT 0 COMMENT '父部门ID',
    ancestors   varchar(500) NOT NULL DEFAULT '' COMMENT '祖级列表,逗号分隔',
    dept_name   varchar(30)  NOT NULL COMMENT '部门名称',
    order_num   int          NOT NULL DEFAULT 0 COMMENT '显示顺序',
    leader      varchar(20)           DEFAULT NULL COMMENT '负责人',
    phone       varchar(11)           DEFAULT NULL COMMENT '联系电话',
    email       varchar(50)           DEFAULT NULL COMMENT '邮箱',
    status      tinyint      NOT NULL DEFAULT 0 COMMENT '部门状态:0-正常 1-停用',
    del_flag    tinyint      NOT NULL DEFAULT 0 COMMENT '删除标记:0-存在 1-已删除',
    create_by   varchar(64)           DEFAULT NULL COMMENT '创建者',
    create_time datetime              DEFAULT NULL COMMENT '创建时间',
    update_by   varchar(64)           DEFAULT NULL COMMENT '更新者',
    update_time datetime              DEFAULT NULL COMMENT '更新时间',
    remark      varchar(500)          DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (dept_id),
    KEY idx_dept_parent_id (parent_id)
) COMMENT '部门表';

-- ----------------------------
-- 2、菜单权限表
-- ----------------------------
DROP TABLE IF EXISTS sys_menu;
CREATE TABLE sys_menu
(
    menu_id     bigint       NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
    menu_name   varchar(50)  NOT NULL COMMENT '菜单名称',
    parent_id   bigint       NOT NULL DEFAULT 0 COMMENT '父菜单ID',
    order_num   int          NOT NULL DEFAULT 0 COMMENT '显示顺序',
    path        varchar(200)          DEFAULT NULL COMMENT '路由地址',
    component   varchar(255)          DEFAULT NULL COMMENT '前端组件路径',
    is_frame    tinyint      NOT NULL DEFAULT 1 COMMENT '是否外链:0-是 1-否',
    menu_type   char(1)      NOT NULL COMMENT '菜单类型:M-目录 C-菜单 F-按钮',
    visible     tinyint      NOT NULL DEFAULT 0 COMMENT '显示状态:0-显示 1-隐藏',
    status      tinyint      NOT NULL DEFAULT 0 COMMENT '菜单状态:0-正常 1-停用',
    perms       varchar(100)          DEFAULT NULL COMMENT '权限标识',
    icon        varchar(100)          DEFAULT NULL COMMENT '菜单图标',
    del_flag    tinyint      NOT NULL DEFAULT 0 COMMENT '删除标记:0-存在 1-已删除',
    create_by   varchar(64)           DEFAULT NULL COMMENT '创建者',
    create_time datetime              DEFAULT NULL COMMENT '创建时间',
    update_by   varchar(64)           DEFAULT NULL COMMENT '更新者',
    update_time datetime              DEFAULT NULL COMMENT '更新时间',
    remark      varchar(500)          DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (menu_id),
    KEY idx_menu_parent_id (parent_id)
) COMMENT '菜单权限表';

-- ----------------------------
-- 3、角色表
-- ----------------------------
DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role
(
    role_id     bigint       NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    role_name   varchar(30)  NOT NULL COMMENT '角色名称',
    role_key    varchar(100) NOT NULL COMMENT '角色权限字符串',
    role_sort   int          NOT NULL DEFAULT 0 COMMENT '显示顺序',
    data_scope  char(1)      NOT NULL DEFAULT '1' COMMENT '数据范围:1-全部 2-自定义 3-本部门 4-本部门及以下 5-仅本人',
    status      tinyint      NOT NULL DEFAULT 0 COMMENT '角色状态:0-正常 1-停用',
    del_flag    tinyint      NOT NULL DEFAULT 0 COMMENT '删除标记:0-存在 1-已删除',
    create_by   varchar(64)           DEFAULT NULL COMMENT '创建者',
    create_time datetime              DEFAULT NULL COMMENT '创建时间',
    update_by   varchar(64)           DEFAULT NULL COMMENT '更新者',
    update_time datetime              DEFAULT NULL COMMENT '更新时间',
    remark      varchar(500)          DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (role_id),
    -- 这里刻意用普通索引而非唯一索引:表启用了逻辑删除,唯一索引会让已删除记录
    -- 继续占用 role_key,导致"删除后无法用同名重建"。唯一性由 Service 层校验
    -- (校验时自动过滤 del_flag = 0),索引只负责加速校验查询。
    KEY idx_role_key (role_key)
) COMMENT '角色表';

-- ----------------------------
-- 4、用户表
-- ----------------------------
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user
(
    user_id     bigint       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    dept_id     bigint                DEFAULT NULL COMMENT '部门ID',
    user_name   varchar(30)  NOT NULL COMMENT '登录账号',
    nick_name   varchar(30)  NOT NULL COMMENT '用户昵称',
    email       varchar(50)           DEFAULT NULL COMMENT '邮箱',
    phonenumber varchar(11)           DEFAULT NULL COMMENT '手机号码',
    sex         char(1)      NOT NULL DEFAULT '2' COMMENT '性别:0-男 1-女 2-未知',
    avatar      varchar(255)          DEFAULT NULL COMMENT '头像地址',
    password    varchar(100) NOT NULL COMMENT '密码(BCrypt 密文)',
    status      tinyint      NOT NULL DEFAULT 0 COMMENT '账号状态:0-正常 1-停用',
    del_flag    tinyint      NOT NULL DEFAULT 0 COMMENT '删除标记:0-存在 1-已删除',
    login_ip    varchar(128)          DEFAULT NULL COMMENT '最后登录IP',
    login_date  datetime              DEFAULT NULL COMMENT '最后登录时间',
    create_by   varchar(64)           DEFAULT NULL COMMENT '创建者',
    create_time datetime              DEFAULT NULL COMMENT '创建时间',
    update_by   varchar(64)           DEFAULT NULL COMMENT '更新者',
    update_time datetime              DEFAULT NULL COMMENT '更新时间',
    remark      varchar(500)          DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (user_id),
    -- 同 sys_role.idx_role_key,逻辑删除与唯一索引不能共存,唯一性由 Service 层保证
    KEY idx_user_name (user_name),
    KEY idx_user_dept_id (dept_id)
) COMMENT '用户表';

-- ----------------------------
-- 5、用户角色关联表
-- ----------------------------
DROP TABLE IF EXISTS sys_user_role;
CREATE TABLE sys_user_role
(
    user_id bigint NOT NULL COMMENT '用户ID',
    role_id bigint NOT NULL COMMENT '角色ID',
    PRIMARY KEY (user_id, role_id),
    KEY idx_ur_role_id (role_id)
) COMMENT '用户角色关联表';

-- ----------------------------
-- 6、角色菜单关联表
-- ----------------------------
DROP TABLE IF EXISTS sys_role_menu;
CREATE TABLE sys_role_menu
(
    role_id bigint NOT NULL COMMENT '角色ID',
    menu_id bigint NOT NULL COMMENT '菜单ID',
    PRIMARY KEY (role_id, menu_id),
    KEY idx_rm_menu_id (menu_id)
) COMMENT '角色菜单关联表';
