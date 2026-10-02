package org.jeecg.modules.equipment.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.util.UUIDGenerator;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.equipment.entity.EquipmentArchive;
import org.jeecg.modules.equipment.mapper.EquipmentArchiveMapper;
import org.jeecg.modules.equipment.service.IEquipmentArchiveService;
import org.jeecg.modules.equipment.util.EquipmentImportErrorFileUtil;
import org.jeecg.modules.equipment.vo.EquipmentImportCheckVO;
import org.jeecg.modules.equipment.vo.EquipmentImportErrorVO;
import org.jeecg.modules.equipment.vo.EquipmentImportVO;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Description: 设备档案
 * @Author: equipment
 * @Date: 2026-10-02
 * @Version: V1.0
 */
@Slf4j
@Service
public class EquipmentArchiveServiceImpl extends ServiceImpl<EquipmentArchiveMapper, EquipmentArchive> implements IEquipmentArchiveService {

    /**校验结果在 Redis 中的缓存 key 前缀*/
    private static final String IMPORT_CHECK_CACHE_PREFIX = "equipment:import:check:";
    /**完整通过列表缓存 key 后缀*/
    private static final String IMPORT_FULL_LIST_SUFFIX = ":full";
    /**校验结果缓存有效期：30分钟*/
    private static final long IMPORT_CHECK_CACHE_EXPIRE_SECONDS = 30 * 60;
    /**前端预览最多返回的通过行数*/
    private static final int PREVIEW_LIMIT = 100;

    @Autowired
    @Qualifier("redisTemplate")
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public Result<?> importCheck(MultipartFile file) {
        // Step.1 解析 Excel（模板为 2 行标题 + 1 行表头）
        List<EquipmentImportVO> rows;
        try {
            ImportParams params = new ImportParams();
            params.setTitleRows(2);
            params.setHeadRows(1);
            rows = ExcelImportUtil.importExcel(file.getInputStream(), EquipmentImportVO.class, params);
        } catch (Exception e) {
            log.error("设备档案导入文件解析失败：" + e.getMessage(), e);
            return Result.error("文件解析失败，请使用最新导入模板上传：" + e.getMessage());
        }
        if (CollectionUtils.isEmpty(rows)) {
            return Result.error("导入文件中没有可导入的数据行，请检查文件内容。");
        }

        // Step.2 收集文件内出现过的所有编号，一次性查询库中已存在的编号
        Set<String> allEquipNos = new HashSet<>();
        for (EquipmentImportVO row : rows) {
            if (row != null && oConvertUtils.isNotEmpty(row.getEquipNo())) {
                allEquipNos.add(row.getEquipNo().trim());
            }
        }
        Set<String> existDbNos = queryExistEquipNos(allEquipNos);

        // Step.3 逐行校验：必填项 + 文件内编号重复 + 与库中编号重复
        List<EquipmentImportVO> validList = new ArrayList<>();
        List<EquipmentImportErrorVO> errorList = new ArrayList<>();
        // 文件内已出现过的编号（首次出现保留，后续重复行判失败）
        Set<String> seenNos = new HashSet<>();
        for (int i = 0; i < rows.size(); i++) {
            EquipmentImportVO row = rows.get(i);
            int rowNum = i + 1;
            trimRow(row);
            List<String> reasons = new ArrayList<>();
            String equipNo = row == null ? null : row.getEquipNo();
            if (row == null) {
                reasons.add("空行或数据格式不正确");
            } else {
                // 必填项校验：设备编号、名称、类型、部门、位置、责任人
                if (oConvertUtils.isEmpty(row.getEquipNo())) {
                    reasons.add("设备编号为空");
                }
                if (oConvertUtils.isEmpty(row.getEquipName())) {
                    reasons.add("名称为空");
                }
                if (oConvertUtils.isEmpty(row.getEquipType())) {
                    reasons.add("类型为空");
                }
                if (oConvertUtils.isEmpty(row.getDepartment())) {
                    reasons.add("部门为空");
                }
                if (oConvertUtils.isEmpty(row.getLocation())) {
                    reasons.add("位置为空");
                }
                if (oConvertUtils.isEmpty(row.getOwner())) {
                    reasons.add("责任人为空");
                }
            }
            // 重复编号校验（编号非空时才校验）
            if (oConvertUtils.isNotEmpty(equipNo)) {
                if (seenNos.contains(equipNo)) {
                    reasons.add("设备编号在文件内重复");
                }
                if (existDbNos.contains(equipNo)) {
                    reasons.add("设备编号在系统中已存在");
                }
            }

            if (reasons.isEmpty()) {
                seenNos.add(equipNo);
                validList.add(row);
            } else {
                errorList.add(buildErrorVO(rowNum, row, String.join("；", reasons)));
            }
        }

        // Step.4 组装校验结果并缓存（JSON 字符串，30分钟过期），等待用户确认入库
        String checkToken = UUIDGenerator.generate();
        List<EquipmentImportVO> previewList = validList.size() > PREVIEW_LIMIT ? new ArrayList<>(validList.subList(0, PREVIEW_LIMIT)) : validList;
        EquipmentImportCheckVO checkVO = new EquipmentImportCheckVO()
                .setCheckToken(checkToken)
                .setTotalCount(rows.size())
                .setSuccessCount(validList.size())
                .setErrorCount(errorList.size())
                .setValidList(previewList);
        if (CollectionUtils.isNotEmpty(errorList)) {
            // 失败行生成 xlsx 文件（含原始数据 + 失败原因），供下载查看
            String relativePath = EquipmentImportErrorFileUtil.saveErrorExcel(errorList);
            checkVO.setFileUrl("/sys/common/static/" + relativePath.replace(File.separator, "/"));
            checkVO.setFileName(relativePath.substring(relativePath.lastIndexOf(File.separator) + 1));
        }
        checkVO.setErrorList(errorList);
        redisTemplate.opsForValue().set(IMPORT_CHECK_CACHE_PREFIX + checkToken, JSON.toJSONString(checkVO),
                IMPORT_CHECK_CACHE_EXPIRE_SECONDS, TimeUnit.SECONDS);
        // 完整通过列表单独缓存（预览仅返回前100行，确认入库使用完整数据）
        redisTemplate.opsForValue().set(IMPORT_CHECK_CACHE_PREFIX + checkToken + IMPORT_FULL_LIST_SUFFIX, JSON.toJSONString(validList),
                IMPORT_CHECK_CACHE_EXPIRE_SECONDS, TimeUnit.SECONDS);
        return Result.ok(checkVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> importConfirm(HttpServletRequest request) {
        String checkToken = request.getParameter("checkToken");
        if (oConvertUtils.isEmpty(checkToken)) {
            return Result.error("缺少校验凭证，请重新上传文件进行校验。");
        }
        String cacheKey = IMPORT_CHECK_CACHE_PREFIX + checkToken;
        Object cacheObj = redisTemplate.opsForValue().get(cacheKey);
        if (cacheObj == null) {
            return Result.error("校验结果不存在或已过期（有效期30分钟），请重新上传文件校验。");
        }
        EquipmentImportCheckVO checkVO;
        List<EquipmentImportVO> fullValidList;
        try {
            checkVO = JSON.parseObject(String.valueOf(cacheObj), EquipmentImportCheckVO.class);
            Object fullObj = redisTemplate.opsForValue().get(cacheKey + IMPORT_FULL_LIST_SUFFIX);
            if (fullObj != null) {
                fullValidList = JSON.parseObject(String.valueOf(fullObj), new TypeReference<List<EquipmentImportVO>>() {});
            } else {
                fullValidList = checkVO.getValidList();
            }
        } catch (Exception e) {
            log.error("设备档案导入校验结果读取失败：" + e.getMessage(), e);
            return Result.error("校验结果读取失败，请重新上传文件校验。");
        }
        if (CollectionUtils.isEmpty(fullValidList)) {
            removeCache(cacheKey);
            return Result.error("没有可入库的数据。");
        }

        // 确认入库前再次校验：防止校验通过后编号被其他入口新增
        Set<String> nos = fullValidList.stream().map(EquipmentImportVO::getEquipNo).collect(Collectors.toSet());
        Set<String> existDbNos = queryExistEquipNos(nos);
        List<EquipmentArchive> toSave = new ArrayList<>();
        List<EquipmentImportErrorVO> errorList = new ArrayList<>();
        Set<String> currentSeen = new HashSet<>();
        for (int i = 0; i < fullValidList.size(); i++) {
            EquipmentImportVO vo = fullValidList.get(i);
            List<String> reasons = new ArrayList<>();
            if (currentSeen.contains(vo.getEquipNo())) {
                reasons.add("设备编号在文件内重复");
            }
            if (existDbNos.contains(vo.getEquipNo())) {
                reasons.add("设备编号在系统中已存在");
            }
            if (reasons.isEmpty()) {
                currentSeen.add(vo.getEquipNo());
                EquipmentArchive entity = new EquipmentArchive();
                BeanUtils.copyProperties(vo, entity);
                toSave.add(entity);
            } else {
                errorList.add(buildErrorVO(i + 1, vo, String.join("；", reasons)));
            }
        }

        int savedCount = 0;
        if (CollectionUtils.isNotEmpty(toSave)) {
            this.saveBatch(toSave);
            savedCount = toSave.size();
        }
        // 凭证使用一次即失效
        removeCache(cacheKey);

        if (CollectionUtils.isEmpty(errorList)) {
            return Result.ok("设备档案导入成功，共入库 " + savedCount + " 条。");
        }
        // 确认阶段仍有失败行（并发场景），生成失败文件并返回 201
        String relativePath = EquipmentImportErrorFileUtil.saveErrorExcel(errorList);
        Map<String, Object> result = new LinkedHashMap<>(6);
        result.put("totalCount", fullValidList.size());
        result.put("successCount", savedCount);
        result.put("errorCount", errorList.size());
        result.put("msg", "共 " + fullValidList.size() + " 条，已入库 " + savedCount + " 条，失败 " + errorList.size() + " 条。");
        result.put("fileUrl", "/sys/common/static/" + relativePath.replace(File.separator, "/"));
        result.put("fileName", relativePath.substring(relativePath.lastIndexOf(File.separator) + 1));
        Result<?> res = Result.ok(result);
        res.setCode(201);
        res.setMessage("导入完成，但有失败行。");
        return res;
    }

    /**
     * 查询库中已存在的设备编号集合
     */
    private Set<String> queryExistEquipNos(Set<String> equipNos) {
        if (CollectionUtils.isEmpty(equipNos)) {
            return new HashSet<>();
        }
        QueryWrapper<EquipmentArchive> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("equip_no", equipNos);
        List<EquipmentArchive> existList = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(existList)) {
            return new HashSet<>();
        }
        return existList.stream().map(EquipmentArchive::getEquipNo).collect(Collectors.toSet());
    }

    /**
     * 删除导入校验缓存
     */
    private void removeCache(String cacheKey) {
        redisTemplate.delete(cacheKey);
        redisTemplate.delete(cacheKey + IMPORT_FULL_LIST_SUFFIX);
    }

    /**
     * 构建失败行对象
     */
    private EquipmentImportErrorVO buildErrorVO(Integer rowNum, EquipmentImportVO row, String reason) {
        EquipmentImportErrorVO vo = new EquipmentImportErrorVO();
        vo.setRowNum(rowNum);
        if (row != null) {
            vo.setEquipNo(row.getEquipNo());
            vo.setEquipName(row.getEquipName());
            vo.setEquipType(row.getEquipType());
            vo.setDepartment(row.getDepartment());
            vo.setLocation(row.getLocation());
            vo.setOwner(row.getOwner());
        }
        vo.setErrorReason(reason);
        return vo;
    }

    /**
     * 去除各字段首尾空格
     */
    private void trimRow(EquipmentImportVO row) {
        if (row == null) {
            return;
        }
        row.setEquipNo(trim(row.getEquipNo()));
        row.setEquipName(trim(row.getEquipName()));
        row.setEquipType(trim(row.getEquipType()));
        row.setDepartment(trim(row.getDepartment()));
        row.setLocation(trim(row.getLocation()));
        row.setOwner(trim(row.getOwner()));
    }

    private String trim(String str) {
        return str == null ? null : str.trim();
    }
}
