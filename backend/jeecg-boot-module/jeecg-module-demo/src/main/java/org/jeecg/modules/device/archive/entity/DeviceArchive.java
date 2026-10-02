package org.jeecg.modules.device.archive.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;
import org.jeecgframework.poi.excel.annotation.Excel;

import java.io.Serializable;

/**
 * @Description: 设备档案
 * @Author: jeecg-boot
 * @Date: 2026-10-02
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "设备档案")
@TableName("device_archive")
public class DeviceArchive extends JeecgEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 设备编号（唯一） */
    @Excel(name = "设备编号", width = 20, orderNum = "1")
    @Schema(description = "设备编号")
    private java.lang.String deviceCode;

    /** 设备名称 */
    @Excel(name = "设备名称", width = 25, orderNum = "2")
    @Schema(description = "设备名称")
    private java.lang.String deviceName;

    /** 设备类型 */
    @Excel(name = "设备类型", width = 15, orderNum = "3")
    @Schema(description = "设备类型")
    private java.lang.String deviceType;

    /** 所属部门 */
    @Excel(name = "部门", width = 20, orderNum = "4")
    @Schema(description = "部门")
    private java.lang.String department;

    /** 安装位置 */
    @Excel(name = "位置", width = 25, orderNum = "5")
    @Schema(description = "位置")
    private java.lang.String location;

    /** 责任人 */
    @Excel(name = "责任人", width = 15, orderNum = "6")
    @Schema(description = "责任人")
    private java.lang.String owner;

    /** 租户ID */
    @Schema(description = "租户ID")
    private java.lang.Integer tenantId;
}
