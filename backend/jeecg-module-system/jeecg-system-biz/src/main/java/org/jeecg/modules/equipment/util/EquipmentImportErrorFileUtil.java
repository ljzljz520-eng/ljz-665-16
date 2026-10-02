package org.jeecg.modules.equipment.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.jeecg.common.util.DateUtils;
import org.jeecg.modules.equipment.vo.EquipmentImportErrorVO;
import org.jeecgframework.poi.excel.ExcelExportUtil;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.enmus.ExcelType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.List;

/**
 * @Description: 设备档案导入失败行文件生成（xlsx，含失败原因列）
 * @Author: equipment
 * @Date: 2026-10-02
 */
@Slf4j
@Component
public class EquipmentImportErrorFileUtil {

    private static String uploadPath;

    @Value("${jeecg.path.upload:}")
    public void setUploadPath(String uploadPath) {
        EquipmentImportErrorFileUtil.uploadPath = uploadPath;
    }

    /**
     * 生成失败行 xlsx 文件
     *
     * @param errorList 失败行（含失败原因）
     * @return 相对 uploadPath 的文件相对路径，如 logs/20261002/equipImportError20261002....xlsx
     */
    public static String saveErrorExcel(List<EquipmentImportErrorVO> errorList) {
        Date d = new Date();
        String saveDir = "logs" + File.separator + DateUtils.yyyyMMdd.get().format(d) + File.separator;
        String saveFullDir = uploadPath + File.separator + saveDir;
        File dirFile = new File(saveFullDir);
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
        String fileName = "equipImportError" + DateUtils.yyyymmddhhmmss.get().format(d) + Math.round(Math.random() * 10000) + ".xlsx";
        String saveFilePath = saveFullDir + fileName;

        // 与导入模板保持一致：2行标题 + 1行表头
        ExportParams exportParams = new ExportParams("设备档案导入失败数据", "导出人:系统", "失败数据", ExcelType.XSSF);
        Workbook workbook = ExcelExportUtil.exportExcel(exportParams, EquipmentImportErrorVO.class, errorList);
        try (FileOutputStream fos = new FileOutputStream(saveFilePath)) {
            workbook.write(fos);
        } catch (Exception e) {
            log.error("设备档案导入失败文件生成异常：" + e.getMessage(), e);
        } finally {
            try {
                workbook.close();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        return saveDir + fileName;
    }
}
