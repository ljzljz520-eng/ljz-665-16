-- 设备档案表（MySQL，幂等）
-- 对应实体：org.jeecg.modules.equipment.entity.EquipmentArchive
CREATE TABLE IF NOT EXISTS `equipment_archive` (
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
