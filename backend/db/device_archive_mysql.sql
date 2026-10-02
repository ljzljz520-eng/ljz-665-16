-- ----------------------------
-- 设备档案模块建表脚本（MySQL，已初始化过 jeecgboot-mysql-5.7.sql 的环境可直接执行；
-- 全新环境无需执行，device_archive 已包含在 jeecgboot-mysql-5.7.sql 初始化脚本中）
-- ----------------------------
DROP TABLE IF EXISTS `device_archive`;
CREATE TABLE `device_archive`  (
  `id` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '主键ID',
  `device_code` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '设备编号',
  `device_name` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '设备名称',
  `device_type` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '设备类型',
  `department` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '部门',
  `location` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '位置',
  `owner` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '责任人',
  `create_by` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
  `tenant_id` int(10) NULL DEFAULT 0 COMMENT '租户ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_device_archive_code` (`device_code`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC COMMENT = '设备档案';

-- 菜单：一级“设备管理” + 二级“设备档案”（管理员角色 f6817f48af4fb3af11b9e8bf182f618b 授权）
INSERT INTO `sys_permission` VALUES ('da1000000000000000000000000001', '', '设备管理', '/device', 'layouts/RouteView', 1, NULL, NULL, 0, NULL, '1', 5.00, 0, 'ant-design:hdd-outlined', 0, 0, 0, 0, NULL, 'admin', '2026-10-02 10:00:00', NULL, NULL, 0, 0, '1', 0);
INSERT INTO `sys_permission` VALUES ('da1000000000000000000000000002', 'da1000000000000000000000000001', '设备档案', '/device/archive', 'device/archive/index', 1, NULL, NULL, 1, NULL, '1', 1.00, 0, 'ant-design:database-outlined', 1, 1, 0, 0, NULL, 'admin', '2026-10-02 10:00:00', NULL, NULL, 0, 0, '1', 0);
INSERT INTO `sys_role_permission` VALUES ('da2000000000000000000000000001', 'f6817f48af4fb3af11b9e8bf182f618b', 'da1000000000000000000000000001', NULL, NULL, NULL);
INSERT INTO `sys_role_permission` VALUES ('da2000000000000000000000000002', 'f6817f48af4fb3af11b9e8bf182f618b', 'da1000000000000000000000000002', NULL, NULL, NULL);
