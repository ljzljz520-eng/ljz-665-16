package org.jeecg.modules.device.archive.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jeecgframework.poi.excel.annotation.Excel;

import java.io.Serializable;

/**
 * 设备档案导入失败行（用于生成失败原因文件）
 *
 * @author jeecg-boot
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DeviceImportErrorVo implements Serializable {
    private static final long serialVersionUID = 1L;

    @Excel(name = "行号", width = 10, orderNum = "0")
    private Integer rowNum;

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

    @Excel(name = "失败原因", width = 40, orderNum = "7")
    private String errorReason;
}
