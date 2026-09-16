-- ----------------------------------------------------------------------------
-- 权限管理系统 初始化数据
--
-- 初始账号:
--   admin / admin123  超级管理员,拥有全部权限
--   kite  / kite123   普通角色,仅有用户查询权限
-- 密码为 BCrypt(strength=10) 密文,请在生产环境首次登录后立即修改。
--
-- 执行方式:
--   mysql --default-character-set=utf8mb4 -uroot -p kite_libai < data.sql
-- SET NAMES 用于兜底,防止客户端字符集为 latin1 时中文被双重编码存成乱码。
-- ----------------------------------------------------------------------------

SET NAMES utf8mb4;

-- ----------------------------
-- 部门数据
-- ----------------------------
INSERT INTO sys_dept (dept_id, parent_id, ancestors, dept_name, order_num, leader, phone, status, del_flag, create_by, create_time)
VALUES (1, 0, '0', '总公司', 1, '管理员', '15888888888', 0, 0, 'system', now()),
       (2, 1, '0,1', '研发部门', 1, '张三', '15888888888', 0, 0, 'system', now()),
       (3, 1, '0,1', '市场部门', 2, '李四', '15888888888', 0, 0, 'system', now()),
       (4, 2, '0,1,2', '前端组', 1, '王五', '15888888888', 0, 0, 'system', now()),
       (5, 2, '0,1,2', '后端组', 2, '赵六', '15888888888', 0, 0, 'system', now());

-- ----------------------------
-- 菜单数据:目录(M) -> 菜单(C) -> 按钮(F)
-- ----------------------------
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, menu_type, visible, status, perms, icon, del_flag, create_by, create_time)
VALUES (1, '系统管理', 0, 1, 'system', NULL, 1, 'M', 0, 0, NULL, 'system', 0, 'system', now()),
       (100, '用户管理', 1, 1, 'user', 'system/user/index', 1, 'C', 0, 0, 'system:user:list', 'user', 0, 'system', now()),
       (101, '角色管理', 1, 2, 'role', 'system/role/index', 1, 'C', 0, 0, 'system:role:list', 'peoples', 0, 'system', now()),
       (102, '菜单管理', 1, 3, 'menu', 'system/menu/index', 1, 'C', 0, 0, 'system:menu:list', 'tree-table', 0, 'system', now()),
       (103, '部门管理', 1, 4, 'dept', 'system/dept/index', 1, 'C', 0, 0, 'system:dept:list', 'tree', 0, 'system', now()),
       -- 用户管理按钮
       (1000, '用户查询', 100, 1, '', NULL, 1, 'F', 0, 0, 'system:user:query', '#', 0, 'system', now()),
       (1001, '用户新增', 100, 2, '', NULL, 1, 'F', 0, 0, 'system:user:add', '#', 0, 'system', now()),
       (1002, '用户修改', 100, 3, '', NULL, 1, 'F', 0, 0, 'system:user:edit', '#', 0, 'system', now()),
       (1003, '用户删除', 100, 4, '', NULL, 1, 'F', 0, 0, 'system:user:remove', '#', 0, 'system', now()),
       (1004, '重置密码', 100, 5, '', NULL, 1, 'F', 0, 0, 'system:user:resetPwd', '#', 0, 'system', now()),
       -- 角色管理按钮
       (1005, '角色查询', 101, 1, '', NULL, 1, 'F', 0, 0, 'system:role:query', '#', 0, 'system', now()),
       (1006, '角色新增', 101, 2, '', NULL, 1, 'F', 0, 0, 'system:role:add', '#', 0, 'system', now()),
       (1007, '角色修改', 101, 3, '', NULL, 1, 'F', 0, 0, 'system:role:edit', '#', 0, 'system', now()),
       (1008, '角色删除', 101, 4, '', NULL, 1, 'F', 0, 0, 'system:role:remove', '#', 0, 'system', now()),
       -- 菜单管理按钮
       (1009, '菜单查询', 102, 1, '', NULL, 1, 'F', 0, 0, 'system:menu:query', '#', 0, 'system', now()),
       (1010, '菜单新增', 102, 2, '', NULL, 1, 'F', 0, 0, 'system:menu:add', '#', 0, 'system', now()),
       (1011, '菜单修改', 102, 3, '', NULL, 1, 'F', 0, 0, 'system:menu:edit', '#', 0, 'system', now()),
       (1012, '菜单删除', 102, 4, '', NULL, 1, 'F', 0, 0, 'system:menu:remove', '#', 0, 'system', now()),
       -- 部门管理按钮
       (1013, '部门查询', 103, 1, '', NULL, 1, 'F', 0, 0, 'system:dept:query', '#', 0, 'system', now()),
       (1014, '部门新增', 103, 2, '', NULL, 1, 'F', 0, 0, 'system:dept:add', '#', 0, 'system', now()),
       (1015, '部门修改', 103, 3, '', NULL, 1, 'F', 0, 0, 'system:dept:edit', '#', 0, 'system', now()),
       (1016, '部门删除', 103, 4, '', NULL, 1, 'F', 0, 0, 'system:dept:remove', '#', 0, 'system', now());

-- ----------------------------
-- 角色数据
-- ----------------------------
INSERT INTO sys_role (role_id, role_name, role_key, role_sort, data_scope, status, del_flag, create_by, create_time, remark)
VALUES (1, '超级管理员', 'admin', 1, '1', 0, 0, 'system', now(), '拥有系统全部权限'),
       (2, '普通角色', 'common', 2, '2', 0, 0, 'system', now(), '按菜单授权的普通角色');

-- ----------------------------
-- 用户数据(密码均为 BCrypt 密文)
-- admin 明文 admin123 / kite 明文 kite123
-- ----------------------------
INSERT INTO sys_user (user_id, dept_id, user_name, nick_name, email, phonenumber, sex, password, status, del_flag, create_by, create_time, remark)
VALUES (1, 2, 'admin', '系统管理员', 'admin@kite.com', '15888888888', '0',
        '$2a$10$IBl7owiaHePaJuN9FpSbNefQ7NzUVv9ZUtc88t06CRsOo3CGpknYS', 0, 0, 'system', now(), '超级管理员账号'),
       (2, 4, 'kite', '开发者', 'kite@kite.com', '15666666666', '0',
        '$2a$10$r1SeUU4sqLUBkmyd8EDlnu.at72MS2qnuNHsmcuIkPQFO1ZVw/aJq', 0, 0, 'system', now(), '普通开发账号');

-- ----------------------------
-- 用户角色关联
-- ----------------------------
INSERT INTO sys_user_role (user_id, role_id)
VALUES (1, 1),
       (2, 2);

-- ----------------------------
-- 角色菜单关联:普通角色只授予"系统管理 -> 用户管理 -> 用户查询"
-- 超级管理员不落关联数据,由代码按 role_key = admin 直接放行全部权限
-- ----------------------------
INSERT INTO sys_role_menu (role_id, menu_id)
VALUES (2, 1),
       (2, 100),
       (2, 1000);
