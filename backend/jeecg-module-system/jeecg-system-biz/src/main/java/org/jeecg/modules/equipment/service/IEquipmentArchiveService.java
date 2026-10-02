package org.jeecg.modules.equipment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletRequest;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.equipment.entity.EquipmentArchive;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Description: 设备档案
 * @Author: equipment
 * @Date: 2026-10-02
 * @Version: V1.0
 */
public interface IEquipmentArchiveService extends IService<EquipmentArchive> {

    /**
     * 导入第一步：解析并校验（必填项、与库中编号重复、文件内编号重复），校验结果缓存30分钟，不写库
     *
     * @param file 导入文件
     * @return 校验结果（含确认凭证、失败行错误文件地址）
     */
    Result<?> importCheck(MultipartFile file);

    /**
     * 导入第二步：按校验凭证确认入库（入库前会再次校验编号是否已存在）
     *
     * @param request 含 checkToken
     * @return 入库结果
     */
    Result<?> importConfirm(HttpServletRequest request);
}
