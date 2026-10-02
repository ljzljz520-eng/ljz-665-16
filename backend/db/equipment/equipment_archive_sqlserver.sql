-- 设备档案表（SQL Server，幂等）
-- 对应实体：org.jeecg.modules.equipment.entity.EquipmentArchive
IF NOT EXISTS (SELECT 1 FROM sys.objects WHERE object_id = OBJECT_ID(N'[equipment_archive]') AND type = N'U')
BEGIN
    CREATE TABLE [equipment_archive] (
        [id] NVARCHAR(36) NOT NULL,
        [equip_no] NVARCHAR(64) NOT NULL UNIQUE,
        [equip_name] NVARCHAR(128) NOT NULL,
        [equip_type] NVARCHAR(64) NOT NULL,
        [department] NVARCHAR(128) NOT NULL,
        [location] NVARCHAR(255) NOT NULL,
        [owner] NVARCHAR(64) NOT NULL,
        [remark] NVARCHAR(500) NULL,
        [create_by] NVARCHAR(50) NULL,
        [create_time] DATETIME NULL,
        [update_by] NVARCHAR(50) NULL,
        [update_time] DATETIME NULL,
        CONSTRAINT [pk_equipment_archive] PRIMARY KEY ([id])
    );
END;
