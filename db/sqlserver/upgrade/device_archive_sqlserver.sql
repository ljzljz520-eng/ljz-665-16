-- ----------------------------
-- 设备档案模块建表脚本（SQL Server，已初始化过的环境可直接执行；
-- 全新环境无需执行，db-init 会自动转换初始化脚本中的 device_archive）
-- ----------------------------
IF OBJECT_ID(N'[device_archive]', N'U') IS NOT NULL DROP TABLE [device_archive];
CREATE TABLE [device_archive] (
  [id] nvarchar(50) NOT NULL,
  [device_code] nvarchar(50) NOT NULL,
  [device_name] nvarchar(100) NOT NULL,
  [device_type] nvarchar(50) NULL,
  [department] nvarchar(100) NULL,
  [location] nvarchar(200) NULL,
  [owner] nvarchar(50) NULL,
  [create_by] nvarchar(32) NULL,
  [create_time] datetime2 NULL,
  [update_by] nvarchar(32) NULL,
  [update_time] datetime2 NULL,
  [tenant_id] int NULL DEFAULT 0,
  PRIMARY KEY ([id])
);
CREATE UNIQUE INDEX [uk_device_archive_code] ON [device_archive] ([device_code]);

-- 菜单：一级“设备管理” + 二级“设备档案”（管理员角色 f6817f48af4fb3af11b9e8bf182f618b 授权）
INSERT INTO [sys_permission] ([id],[parent_id],[name],[url],[component],[is_route],[component_name],[redirect],[menu_type],[perms],[perms_type],[sort_no],[always_show],[icon],[is_leaf],[keep_alive],[hidden],[hide_tab],[description],[create_by],[create_time],[update_by],[update_time],[del_flag],[rule_flag],[status],[internal_or_external])
VALUES ('da1000000000000000000000000001', '', N'设备管理', '/device', 'layouts/RouteView', 1, NULL, NULL, 0, NULL, '1', 5.00, 0, 'ant-design:hdd-outlined', 0, 0, 0, 0, NULL, 'admin', '2026-10-02 10:00:00', NULL, NULL, 0, 0, '1', 0);
INSERT INTO [sys_permission] ([id],[parent_id],[name],[url],[component],[is_route],[component_name],[redirect],[menu_type],[perms],[perms_type],[sort_no],[always_show],[icon],[is_leaf],[keep_alive],[hidden],[hide_tab],[description],[create_by],[create_time],[update_by],[update_time],[del_flag],[rule_flag],[status],[internal_or_external])
VALUES ('da1000000000000000000000000002', 'da1000000000000000000000000001', N'设备档案', '/device/archive', 'device/archive/index', 1, NULL, NULL, 1, NULL, '1', 1.00, 0, 'ant-design:database-outlined', 1, 1, 0, 0, NULL, 'admin', '2026-10-02 10:00:00', NULL, NULL, 0, 0, '1', 0);
INSERT INTO [sys_role_permission] ([id],[role_id],[permission_id],[data_rule_ids],[operate_date],[operate_ip])
VALUES ('da2000000000000000000000000001', 'f6817f48af4fb3af11b9e8bf182f618b', 'da1000000000000000000000000001', NULL, NULL, NULL);
INSERT INTO [sys_role_permission] ([id],[role_id],[permission_id],[data_rule_ids],[operate_date],[operate_ip])
VALUES ('da2000000000000000000000000002', 'f6817f48af4fb3af11b9e8bf182f618b', 'da1000000000000000000000000002', NULL, NULL, NULL);
