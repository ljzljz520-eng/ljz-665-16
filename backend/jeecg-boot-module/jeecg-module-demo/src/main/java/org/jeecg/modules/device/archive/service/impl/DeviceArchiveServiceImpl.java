package org.jeecg.modules.device.archive.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.device.archive.entity.DeviceArchive;
import org.jeecg.modules.device.archive.mapper.DeviceArchiveMapper;
import org.jeecg.modules.device.archive.service.IDeviceArchiveService;
import org.jeecg.modules.device.archive.util.DeviceImportCache;
import org.jeecg.modules.device.archive.util.DeviceImportSessionHolder;
import org.jeecg.modules.device.archive.vo.DeviceArchiveImportVo;
import org.jeecg.modules.device.archive.vo.DeviceImportCheckResult;
import org.jeecg.modules.device.archive.vo.DeviceImportErrorVo;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @Description: 设备档案
 * @Author: jeecg-boot
 * @Date: 2026-10-02
 * @Version: V1.0
 */
@Slf4j
@Service
public class DeviceArchiveServiceImpl extends ServiceImpl<DeviceArchiveMapper, DeviceArchive> implements IDeviceArchiveService {

    /** 失败预览最多返回条数 */
    private static final int ERROR_PREVIEW_LIMIT = 10;

    @Autowired
    private DeviceImportCache deviceImportCache;

    @Override
    public DeviceImportCheckResult checkImport(MultipartFile file, String fileName, String username) {
        List<DeviceArchiveImportVo> rows;
        try {
            ImportParams params = new ImportParams();
            params.setTitleRows(0);
            params.setHeadRows(1);
            params.setNeedSave(false);
            rows = ExcelImportUtil.importExcel(file.getInputStream(), DeviceArchiveImportVo.class, params);
        } catch (Exception e) {
            log.error("设备档案导入文件解析失败：{}", e.getMessage(), e);
            throw new RuntimeException("文件解析失败，请使用标准导入模板（.xls/.xlsx）：" + e.getMessage());
        }

        DeviceImportCheckResult result = new DeviceImportCheckResult();
        result.setFileName(fileName);
        if (rows == null || rows.isEmpty()) {
            result.setTotalCount(0);
            result.setValidCount(0);
            result.setErrorCount(0);
            result.setErrorPreview(Collections.emptyList());
            return result;
        }
        result.setTotalCount(rows.size());

        // 1. 库中已存在的设备编号
        List<String> codes = rows.stream()
                .map(DeviceArchiveImportVo::getDeviceCode)
                .filter(oConvertUtils::isNotEmpty)
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());
        Set<String> dbCodes = new HashSet<>();
        if (!codes.isEmpty()) {
            QueryWrapper<DeviceArchive> queryWrapper = new QueryWrapper<>();
            queryWrapper.in("device_code", codes);
            List<DeviceArchive> dbList = this.list(queryWrapper);
            dbCodes = dbList.stream().map(DeviceArchive::getDeviceCode).collect(Collectors.toSet());
        }

        List<DeviceArchive> validList = new ArrayList<>();
        List<DeviceImportErrorVo> errorList = new ArrayList<>();
        // 文件内已出现过的编号（用于检查文件内重复）
        Set<String> seenCodes = new HashSet<>();
        int totalCount = 0;

        for (int i = 0; i < rows.size(); i++) {
            DeviceArchiveImportVo vo = rows.get(i);
            // Excel中数据行的实际行号（第1行为表头）
            int rowNum = i + 2;
            String code = trim(vo.getDeviceCode());
            String name = trim(vo.getDeviceName());
            String type = trim(vo.getDeviceType());
            String department = trim(vo.getDepartment());
            String location = trim(vo.getLocation());
            String owner = trim(vo.getOwner());

            // 全部字段为空的行视为空行，直接跳过
            if (oConvertUtils.isEmpty(code) && oConvertUtils.isEmpty(name) && oConvertUtils.isEmpty(type)
                    && oConvertUtils.isEmpty(department) && oConvertUtils.isEmpty(location) && oConvertUtils.isEmpty(owner)) {
                continue;
            }
            totalCount++;

            List<String> reasons = new ArrayList<>(2);
            if (oConvertUtils.isEmpty(code)) {
                reasons.add("设备编号为必填项");
            }
            if (oConvertUtils.isEmpty(name)) {
                reasons.add("设备名称为必填项");
            }
            if (oConvertUtils.isNotEmpty(code)) {
                if (dbCodes.contains(code)) {
                    reasons.add("设备编号与库中已有数据重复");
                } else if (!seenCodes.add(code)) {
                    reasons.add("设备编号在文件内重复");
                }
            }

            if (!reasons.isEmpty()) {
                errorList.add(buildError(rowNum, vo, code, name, String.join("；", reasons)));
                continue;
            }

            DeviceArchive entity = new DeviceArchive();
            BeanUtils.copyProperties(vo, entity);
            entity.setDeviceCode(code);
            entity.setDeviceName(name);
            entity.setDeviceType(type);
            entity.setDepartment(department);
            entity.setLocation(location);
            entity.setOwner(owner);
            validList.add(entity);
        }

        String sessionId = UUID.randomUUID().toString().replace("-", "");
        DeviceImportSessionHolder holder = new DeviceImportSessionHolder();
        holder.setSessionId(sessionId);
        holder.setFileName(fileName);
        holder.setValidList(validList);
        holder.setErrorList(errorList);
        holder.setCreateTime(System.currentTimeMillis());
        holder.setUsername(username);
        deviceImportCache.put(holder);

        result.setSessionId(sessionId);
        result.setTotalCount(totalCount);
        result.setValidCount(validList.size());
        result.setErrorCount(errorList.size());
        result.setErrorPreview(errorList.size() > ERROR_PREVIEW_LIMIT
                ? new ArrayList<>(errorList.subList(0, ERROR_PREVIEW_LIMIT))
                : errorList);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int confirmImport(String sessionId, String username) {
        DeviceImportSessionHolder holder = deviceImportCache.get(sessionId);
        if (holder == null) {
            throw new RuntimeException("导入会话不存在或已过期（30分钟内有效），请重新上传文件");
        }
        if (username != null && !username.equals(holder.getUsername())) {
            throw new RuntimeException("不能确认他人上传的导入数据");
        }
        List<DeviceArchive> validList = holder.getValidList();
        int count = 0;
        if (validList != null && !validList.isEmpty()) {
            // 确认前再次校验编号，防止校验后并发新增导致重复
            List<String> codes = validList.stream().map(DeviceArchive::getDeviceCode).collect(Collectors.toList());
            QueryWrapper<DeviceArchive> queryWrapper = new QueryWrapper<>();
            queryWrapper.in("device_code", codes);
            long dup = this.count(queryWrapper);
            if (dup > 0) {
                throw new RuntimeException("检测到设备编号已存在，为避免重复入库，请重新上传校验");
            }
            this.saveBatch(validList);
            count = validList.size();
        }
        // 入库后保留失败行，允许继续下载；有效数据清除
        holder.setValidList(Collections.emptyList());
        return count;
    }

    @Override
    public List<DeviceImportErrorVo> getErrorList(String sessionId) {
        DeviceImportSessionHolder holder = deviceImportCache.get(sessionId);
        if (holder == null) {
            throw new RuntimeException("导入会话不存在或已过期（30分钟内有效），请重新上传文件");
        }
        return holder.getErrorList() == null ? Collections.emptyList() : holder.getErrorList();
    }

    private DeviceImportErrorVo buildError(int rowNum, DeviceArchiveImportVo vo, String code, String name, String reason) {
        DeviceImportErrorVo error = new DeviceImportErrorVo();
        error.setRowNum(rowNum);
        error.setDeviceCode(code);
        error.setDeviceName(name);
        error.setDeviceType(trim(vo.getDeviceType()));
        error.setDepartment(trim(vo.getDepartment()));
        error.setLocation(trim(vo.getLocation()));
        error.setOwner(trim(vo.getOwner()));
        error.setErrorReason(reason);
        return error;
    }

    private String trim(String str) {
        return str == null ? null : str.trim();
    }
}
