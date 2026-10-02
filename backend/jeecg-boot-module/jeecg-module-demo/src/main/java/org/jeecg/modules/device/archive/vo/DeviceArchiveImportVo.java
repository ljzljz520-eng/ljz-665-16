package org.jeecg.modules.device.archive.vo;

import lombok.Data;
import org.jeecgframework.poi.excel.annotation.Excel;

import java.io.Serializable;

/**
 * 设备档案导入模型（与导入模板列一致）
 *
 * @author jeecg-boot
 */
@Data
public class DeviceArchiveImportVo implements Serializable {
    private static final long serialVersionUID = 1L;

    @Excel(name = "设备编号", width = 20, orderNum = "1")
    private String deviceCode;

    @Excel(name = "设备名称", width = 25, orderNum = "2")
    private String deviceName;

    @Excel(name = "设备类型", width = 15, orderNum = "3")
    private String deviceType;

    @Excel(name = "部门", width = 20, orderNum = "4")
    private String department;

    @Excel(name = "位置", width = 25, orderNum = "5")
    private String location;

    @Excel(name = "责任人", width = 15, orderNum = "6")
    private String owner;
}
