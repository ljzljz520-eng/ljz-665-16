package org.jeecg.modules.equipment.vo;

import lombok.Data;
import lombok.experimental.Accessors;
import org.jeecgframework.poi.excel.annotation.Excel;

import java.io.Serializable;

/**
 * @Description: 设备档案导入VO（列顺序即为导入模板列顺序）
 * @Author: equipment
 * @Date: 2026-10-02
 */
@Data
@Accessors(chain = true)
public class EquipmentImportVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Excel(name = "设备编号", width = 20)
    private String equipNo;

    @Excel(name = "名称", width = 25)
    private String equipName;

    @Excel(name = "类型", width = 15)
    private String equipType;

    @Excel(name = "部门", width = 20)
    private String department;

    @Excel(name = "位置", width = 25)
    private String location;

    @Excel(name = "责任人", width = 15)
    private String owner;
}
