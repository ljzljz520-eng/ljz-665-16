package org.jeecg.modules.device.archive.util;

import lombok.Data;
import org.jeecg.modules.device.archive.entity.DeviceArchive;
import org.jeecg.modules.device.archive.vo.DeviceImportErrorVo;

import java.util.List;

/**
 * 导入暂存会话：校验通过的数据保存在内存中，等待用户确认入库。
 *
 * @author jeecg-boot
 */
@Data
public class DeviceImportSessionHolder {

    /** 会话过期时间（毫秒），默认30分钟 */
    public static final long EXPIRE_MILLIS = 30 * 60 * 1000L;

    /** 会话ID */
    private String sessionId;

    /** 原始文件名 */
    private String fileName;

    /** 校验通过、待入库的数据 */
    private List<DeviceArchive> validList;

    /** 全部失败行（用于下载失败文件） */
    private List<DeviceImportErrorVo> errorList;

    /** 创建时间戳 */
    private long createTime;

    /** 操作人（用于隔离不同用户的暂存数据） */
    private String username;

    public boolean isExpired() {
        return System.currentTimeMillis() - createTime > EXPIRE_MILLIS;
    }
}
