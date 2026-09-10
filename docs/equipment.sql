-- ----------------------------
-- 设备档案表
-- ----------------------------
DROP TABLE IF EXISTS `equipment`;
CREATE TABLE `equipment` (
  `id` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '主键',
  `equip_code` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '设备编号',
  `equip_name` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '设备名称',
  `equip_type` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '设备类型',
  `department` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '所属部门',
  `location` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '存放位置',
  `owner` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '责任人',
  `create_by` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建日期',
  `update_by` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新日期',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uniq_equip_code`(`equip_code`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '设备档案' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- 菜单及按钮权限（顶级菜单“设备管理”->“设备档案”）
-- 注意：如已存在相同id的记录请自行调整id
-- ----------------------------
INSERT INTO `sys_permission`(`id`, `parent_id`, `name`, `url`, `component`, `is_route`, `component_name`, `redirect`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `description`, `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `rule_flag`, `status`, `internal_or_external`)
VALUES ('equip_root_000000000000000001', NULL, '设备管理', '/equipment', 'layouts/default/index', 1, NULL, '/equipment/equipment', 0, NULL, '1', 10.00, 0, 'ant-design:hdd-outlined', 0, 0, 0, 0, '设备管理', 'admin', NOW(), NULL, NULL, 0, 0, '1', 0);

INSERT INTO `sys_permission`(`id`, `parent_id`, `name`, `url`, `component`, `is_route`, `component_name`, `redirect`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `description`, `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `rule_flag`, `status`, `internal_or_external`)
VALUES ('equip_menu_000000000000000001', 'equip_root_000000000000000001', '设备档案', '/equipment/equipment', 'equipment/equipment/index', 1, 'EquipmentList', NULL, 1, NULL, '1', 1.00, 0, 'ant-design:database-outlined', 1, 1, 0, 0, '设备档案', 'admin', NOW(), NULL, NULL, 0, 0, '1', 0);

INSERT INTO `sys_permission`(`id`, `parent_id`, `name`, `url`, `component`, `is_route`, `component_name`, `redirect`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `description`, `create_by`, `create_time`, `update_by`, `update_time`, `del_flag`, `rule_flag`, `status`, `internal_or_external`)
VALUES
('equip_btn_0000000000000000001', 'equip_menu_000000000000000001', '新增', NULL, NULL, 1, NULL, NULL, 2, 'equipment:equipment:add', '1', 1.00, 0, NULL, 1, 0, 0, NULL, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0),
('equip_btn_0000000000000000002', 'equip_menu_000000000000000001', '编辑', NULL, NULL, 1, NULL, NULL, 2, 'equipment:equipment:edit', '1', 2.00, 0, NULL, 1, 0, 0, NULL, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0),
('equip_btn_0000000000000000003', 'equip_menu_000000000000000001', '删除', NULL, NULL, 1, NULL, NULL, 2, 'equipment:equipment:delete', '1', 3.00, 0, NULL, 1, 0, 0, NULL, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0),
('equip_btn_0000000000000000004', 'equip_menu_000000000000000001', '导入', NULL, NULL, 1, NULL, NULL, 2, 'equipment:equipment:import', '1', 4.00, 0, NULL, 1, 0, 0, NULL, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0),
('equip_btn_0000000000000000005', 'equip_menu_000000000000000001', '导出', NULL, NULL, 1, NULL, NULL, 2, 'equipment:equipment:export', '1', 5.00, 0, NULL, 1, 0, 0, NULL, NULL, 'admin', NOW(), NULL, NULL, 0, 0, '1', 0);

-- ----------------------------
-- 授权：管理员角色(admin)拥有上述全部菜单/按钮权限
-- ----------------------------
INSERT INTO `sys_role_permission`(`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`)
SELECT rp.id, 'f6817f48af4fb3af11b9e8bf182f618b', rp.pid, NULL, NOW(), NULL
FROM (
  SELECT 'equip_rp_0000000000000000001' AS id, 'equip_root_000000000000000001' AS pid
  UNION ALL SELECT 'equip_rp_0000000000000000002', 'equip_menu_000000000000000001'
  UNION ALL SELECT 'equip_rp_0000000000000000003', 'equip_btn_0000000000000000001'
  UNION ALL SELECT 'equip_rp_0000000000000000004', 'equip_btn_0000000000000000002'
  UNION ALL SELECT 'equip_rp_0000000000000000005', 'equip_btn_0000000000000000003'
  UNION ALL SELECT 'equip_rp_0000000000000000006', 'equip_btn_0000000000000000004'
  UNION ALL SELECT 'equip_rp_0000000000000000007', 'equip_btn_0000000000000000005'
) rp;
