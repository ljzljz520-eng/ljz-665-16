package org.jeecg.modules.device.archive.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 设备档案导入校验结果（校验通过的数据暂存在服务端，等待确认入库）
 *
 * @author jeecg-boot
 */
@Data
public class DeviceImportCheckResult implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 暂存会话ID，确认入库时回传 */
    private String sessionId;

    /** 文件名 */
    private String fileName;

    /** 总行数（不含表头） */
    private Integer totalCount;

    /** 校验通过、待入库行数 */
    private Integer validCount;

    /** 失败行数 */
    private Integer errorCount;

    /** 失败行预览（最多返回前10条） */
    private List<DeviceImportErrorVo> errorPreview;
}
