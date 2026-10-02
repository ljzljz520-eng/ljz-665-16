-- ============================================================
-- 设备档案模块初始化（追加到 jeecgboot-mysql-5.7.sql 末尾）
-- 该文件同时兼容：
--   1) MySQL 直接执行；
--   2) db/sqlserver/convert_mysql_to_sqlserver.py 转换为 SQL Server 执行。
-- 注意：不要使用 CREATE TABLE IF NOT EXISTS / NOW()（转换器仅支持标准建表与普通 INSERT）
-- ============================================================

-- 设备档案表（列级 UNIQUE 约束，MySQL/SQL Server 均兼容）
CREATE TABLE `equipment_archive` (
  `id` varchar(36) NOT NULL COMMENT '主键',
  `equip_no` varchar(64) NOT NULL UNIQUE COMMENT '设备编号',
  `equip_name` varchar(128) NOT NULL COMMENT '设备名称',
  `equip_type` varchar(64) NOT NULL COMMENT '设备类型',
  `department` varchar(128) NOT NULL COMMENT '所属部门',
  `location` varchar(255) NOT NULL COMMENT '存放位置',
  `owner` varchar(64) NOT NULL COMMENT '责任人',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备档案表';

-- 一级菜单：设备管理（仅填关键列，其余取数据库默认值；单行长度控制在转换器安全范围内）
INSERT INTO `sys_permission` (`id`, `parent_id`, `name`, `url`, `is_route`, `menu_type`, `perms_type`, `sort_no`, `icon`, `is_leaf`, `keep_alive`, `hidden`, `del_flag`, `rule_flag`, `status`, `internal_or_external`) VALUES ('1900000000000000001', NULL, '设备管理', '/equipment', 1, 0, '1', 5.00, 'ant-design:hdd-outlined', 0, 0, 0, 0, 0, '1', 0);

-- 子菜单：设备档案
INSERT INTO `sys_permission` (`id`, `parent_id`, `name`, `url`, `component`, `is_route`, `component_name`, `menu_type`, `perms_type`, `sort_no`, `icon`, `is_leaf`, `keep_alive`, `hidden`, `del_flag`, `rule_flag`, `status`, `internal_or_external`) VALUES ('1900000000000000002', '1900000000000000001', '设备档案', '/equipment/archive', 'equipment/archive/index', 1, 'equipment_archive', 1, '1', 1.00, 'ant-design:profile-outlined', 1, 0, 0, 0, 0, '1', 0);

-- 授权给管理员角色
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`) VALUES ('1900000000000000011', 'f6817f48af4fb3af11b9e8bf182f618b', '1900000000000000001');
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`) VALUES ('1900000000000000012', 'f6817f48af4fb3af11b9e8bf182f618b', '1900000000000000002');
