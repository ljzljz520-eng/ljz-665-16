package org.jeecg.modules.equipment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description: 设备档案
 * @Author: jeecg-boot
 * @Date: 2026-09-10
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "设备档案")
@TableName("equipment")
public class Equipment implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键")
    private String id;

    /** 设备编号（唯一） */
    @Schema(description = "设备编号")
    @Excel(name = "设备编号", width = 20)
    private String equipCode;

    /** 设备名称 */
    @Schema(description = "设备名称")
    @Excel(name = "名称", width = 25)
    private String equipName;

    /** 设备类型 */
    @Schema(description = "设备类型")
    @Excel(name = "类型", width = 15)
    private String equipType;

    /** 所属部门 */
    @Schema(description = "所属部门")
    @Excel(name = "部门", width = 20)
    private String department;

    /** 存放位置 */
    @Schema(description = "存放位置")
    @Excel(name = "位置", width = 25)
    private String location;

    /** 责任人 */
    @Schema(description = "责任人")
    @Excel(name = "责任人", width = 15)
    private String owner;

    /** 创建人 */
    @Schema(description = "创建人")
    private String createBy;

    /** 创建时间 */
    @Schema(description = "创建时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 更新人 */
    @Schema(description = "更新人")
    private String updateBy;

    /** 更新时间 */
    @Schema(description = "更新时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
}
