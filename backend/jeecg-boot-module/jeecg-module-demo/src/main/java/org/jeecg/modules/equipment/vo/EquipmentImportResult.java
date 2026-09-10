package org.jeecg.modules.equipment.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 设备档案导入校验结果
 *
 * @Author: jeecg-boot
 */
@Data
public class EquipmentImportResult implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 校验批次令牌（确认入库时使用） */
    private String token;

    /** 文件总行数（不含表头） */
    private int totalCount;

    /** 校验通过行数 */
    private int successCount;

    /** 校验失败行数 */
    private int errorCount;

    /** 失败行明细（前端预览前若干条，全量可下载） */
    private List<EquipmentImportError> errorRows;

    public EquipmentImportResult() {
    }

    public EquipmentImportResult(String token, int totalCount, int successCount, int errorCount, List<EquipmentImportError> errorRows) {
        this.token = token;
        this.totalCount = totalCount;
        this.successCount = successCount;
        this.errorCount = errorCount;
        this.errorRows = errorRows;
    }
}
