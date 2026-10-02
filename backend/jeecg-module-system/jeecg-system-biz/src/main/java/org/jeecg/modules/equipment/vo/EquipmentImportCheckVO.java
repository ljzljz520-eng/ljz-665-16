package org.jeecg.modules.equipment.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * @Description: 设备档案导入-校验结果（确认入库前预览）
 * @Author: equipment
 * @Date: 2026-10-02
 */
@Data
@Accessors(chain = true)
public class EquipmentImportCheckVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**确认入库凭证（有效期30分钟）*/
    private String checkToken;

    /**总行数*/
    private Integer totalCount;

    /**校验通过行数*/
    private Integer successCount;

    /**校验失败行数*/
    private Integer errorCount;

    /**校验通过的预览数据（最多返回前100行）*/
    private List<EquipmentImportVO> validList;

    /**失败行明细（用于前端展示）*/
    private List<EquipmentImportErrorVO> errorList;

    /**失败行错误文件下载地址（失败行数>0时返回）*/
    private String fileUrl;

    /**失败行错误文件名*/
    private String fileName;
}
