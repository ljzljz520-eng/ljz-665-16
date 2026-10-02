-- 设备档案菜单（SQL Server）
-- 一级菜单：设备管理；子菜单：设备档案（前端组件 equipment/archive/index）
-- 管理员角色ID：f6817f48af4fb3af11b9e8bf182f618b

IF NOT EXISTS (SELECT 1 FROM sys_permission WHERE id = '1900000000000000001')
BEGIN
    INSERT INTO sys_permission
    (id, parent_id, name, url, component, is_route, menu_type, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, del_flag, rule_flag, status, internal_or_external)
    VALUES
    ('1900000000000000001', NULL, '设备管理', '/equipment', NULL, 1, 0, '1', 5.00, 0, 'ant-design:hdd-outlined', 0, 0, 0, 0, '设备管理', 'admin', GETDATE(), 0, 0, '1', 0);
END;

IF NOT EXISTS (SELECT 1 FROM sys_permission WHERE id = '1900000000000000002')
BEGIN
    INSERT INTO sys_permission
    (id, parent_id, name, url, component, is_route, component_name, menu_type, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, del_flag, rule_flag, status, internal_or_external)
    VALUES
    ('1900000000000000002', '1900000000000000001', '设备档案', '/equipment/archive', 'equipment/archive/index', 1, 'equipment_archive', 1, '1', 1.00, 0, 'ant-design:profile-outlined', 1, 0, 0, 0, '设备档案', 'admin', GETDATE(), 0, 0, '1', 0);
END;

IF NOT EXISTS (SELECT 1 FROM sys_role_permission WHERE role_id = 'f6817f48af4fb3af11b9e8bf182f618b' AND permission_id = '1900000000000000001')
    INSERT INTO sys_role_permission (id, role_id, permission_id, operate_date) VALUES ('1900000000000000011', 'f6817f48af4fb3af11b9e8bf182f618b', '1900000000000000001', GETDATE());

IF NOT EXISTS (SELECT 1 FROM sys_role_permission WHERE role_id = 'f6817f48af4fb3af11b9e8bf182f618b' AND permission_id = '1900000000000000002')
    INSERT INTO sys_role_permission (id, role_id, permission_id, operate_date) VALUES ('1900000000000000012', 'f6817f48af4fb3af11b9e8bf182f618b', '1900000000000000002', GETDATE());
