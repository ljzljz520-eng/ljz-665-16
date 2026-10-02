package org.jeecg.modules.equipment.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.jeecgframework.poi.excel.annotation.Excel;

import java.io.Serializable;

/**
 * @Description: 设备档案导入失败行（用于生成可下载的错误文件）
 * @Author: equipment
 * @Date: 2026-10-02
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class EquipmentImportErrorVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**Excel 中的行号（不含表头）*/
    @Excel(name = "行号", width = 10)
    private Integer rowNum;

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

    @Excel(name = "失败原因", width = 40)
    private String errorReason;
}
