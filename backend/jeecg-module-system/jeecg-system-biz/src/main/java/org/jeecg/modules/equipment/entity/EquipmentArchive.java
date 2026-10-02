package org.jeecg.modules.equipment.entity;

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
 * @Author: equipment
 * @Date: 2026-10-02
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "设备档案")
@TableName("equipment_archive")
public class EquipmentArchive extends JeecgEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**设备编号（唯一）*/
    @Excel(name = "设备编号", width = 20)
    @Schema(description = "设备编号")
    private String equipNo;

    /**设备名称*/
    @Excel(name = "名称", width = 25)
    @Schema(description = "设备名称")
    private String equipName;

    /**设备类型*/
    @Excel(name = "类型", width = 15)
    @Schema(description = "设备类型")
    private String equipType;

    /**所属部门*/
    @Excel(name = "部门", width = 20)
    @Schema(description = "所属部门")
    private String department;

    /**存放位置*/
    @Excel(name = "位置", width = 25)
    @Schema(description = "存放位置")
    private String location;

    /**责任人*/
    @Excel(name = "责任人", width = 15)
    @Schema(description = "责任人")
    private String owner;

    /**备注*/
    @Schema(description = "备注")
    private String remark;
}
