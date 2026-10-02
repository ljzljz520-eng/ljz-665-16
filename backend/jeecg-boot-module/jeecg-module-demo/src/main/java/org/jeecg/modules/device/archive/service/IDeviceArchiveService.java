package org.jeecg.modules.device.archive.service;

import org.jeecg.common.system.base.service.JeecgService;
import org.jeecg.modules.device.archive.entity.DeviceArchive;
import org.jeecg.modules.device.archive.vo.DeviceImportCheckResult;
import org.jeecg.modules.device.archive.vo.DeviceImportErrorVo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @Description: 设备档案
 * @Author: jeecg-boot
 * @Date: 2026-10-02
 * @Version: V1.0
 */
public interface IDeviceArchiveService extends JeecgService<DeviceArchive> {

    /**
     * 上传文件并校验（必填项、与库中重复编号、文件内重复编号），暂存校验通过的数据
     *
     * @param file     导入文件
     * @param fileName 文件名
     * @param username 当前登录人
     * @return 校验结果
     */
    DeviceImportCheckResult checkImport(MultipartFile file, String fileName, String username);

    /**
     * 确认入库：将暂存的有效数据批量保存
     *
     * @param sessionId 暂存会话ID
     * @param username  当前登录人
     * @return 实际入库条数
     */
    int confirmImport(String sessionId, String username);

    /**
     * 获取校验失败行（用于下载失败文件）
     *
     * @param sessionId 暂存会话ID
     * @return 失败行列表
     */
    List<DeviceImportErrorVo> getErrorList(String sessionId);
}
