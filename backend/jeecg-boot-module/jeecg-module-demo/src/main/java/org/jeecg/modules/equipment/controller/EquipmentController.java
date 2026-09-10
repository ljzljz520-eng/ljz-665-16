package org.jeecg.modules.equipment.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.query.QueryRuleEnum;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.equipment.entity.Equipment;
import org.jeecg.modules.equipment.service.IEquipmentService;
import org.jeecg.modules.equipment.vo.EquipmentImportError;
import org.jeecg.modules.equipment.vo.EquipmentImportResult;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.entity.enmus.ExcelType;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import java.util.*;

/**
 * @Description: 设备档案
 * @Author: jeecg-boot
 * @Date: 2026-09-10
 * @Version: V1.0
 */
@Slf4j
@Tag(name = "设备档案")
@RestController
@RequestMapping("/equipment/equipment")
public class EquipmentController extends JeecgController<Equipment, IEquipmentService> {

    @Autowired
    private IEquipmentService equipmentService;

    /** 导入暂存项：token -> 校验通过的数据（30分钟有效） */
    private static final class CacheEntry<T> {
        final T data;
        final long createTime;

        CacheEntry(T data) {
            this.data = data;
            this.createTime = System.currentTimeMillis();
        }
    }

    /** 导入暂存区：token -> 校验通过的数据 */
    private static final Map<String, CacheEntry<List<Equipment>>> IMPORT_CACHE = new LinkedHashMap<>();
    /** 导入失败明细：token -> 失败行 */
    private static final Map<String, CacheEntry<List<EquipmentImportError>>> ERROR_CACHE = new LinkedHashMap<>();
    /** 暂存有效期：30分钟 */
    private static final long EXPIRE_MILLIS = 30 * 60 * 1000L;
    /** 预览失败行最大条数 */
    private static final int ERROR_PREVIEW_SIZE = 10;

    /**
     * 分页列表查询
     */
    @Operation(summary = "设备档案-分页列表查询")
    @GetMapping(value = "/list")
    public Result<?> queryPageList(Equipment equipment,
                                   @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                   @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                   HttpServletRequest req) {
        QueryWrapper<Equipment> queryWrapper = QueryGenerator.initQueryWrapper(equipment, req.getParameterMap(), likeRuleMap());
        queryWrapper.orderByDesc("create_time");
        Page<Equipment> page = new Page<>(pageNo, pageSize);
        IPage<Equipment> pageList = equipmentService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * 添加
     */
    @AutoLog(value = "设备档案-添加")
    @Operation(summary = "设备档案-添加")
    @PostMapping(value = "/add")
    public Result<?> add(@RequestBody Equipment equipment) {
        // 编号唯一性校验
        long count = equipmentService.count(new QueryWrapper<Equipment>().eq("equip_code", equipment.getEquipCode()));
        if (count > 0) {
            return Result.error("设备编号[" + equipment.getEquipCode() + "]已存在，请勿重复添加");
        }
        equipmentService.save(equipment);
        return Result.OK("添加成功！");
    }

    /**
     * 编辑
     */
    @AutoLog(value = "设备档案-编辑", operateType = CommonConstant.OPERATE_TYPE_3)
    @Operation(summary = "设备档案-编辑")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<?> edit(@RequestBody Equipment equipment) {
        // 编号唯一性校验（排除自身）
        long count = equipmentService.count(new QueryWrapper<Equipment>()
                .eq("equip_code", equipment.getEquipCode())
                .ne("id", equipment.getId()));
        if (count > 0) {
            return Result.error("设备编号[" + equipment.getEquipCode() + "]已存在，请修改");
        }
        equipmentService.updateById(equipment);
        return Result.OK("编辑成功!");
    }

    /**
     * 通过id删除
     */
    @AutoLog(value = "设备档案-删除")
    @Operation(summary = "设备档案-通过id删除")
    @DeleteMapping(value = "/delete")
    public Result<?> delete(@RequestParam(name = "id") String id) {
        equipmentService.removeById(id);
        return Result.OK("删除成功!");
    }

    /**
     * 批量删除
     */
    @AutoLog(value = "设备档案-批量删除")
    @Operation(summary = "设备档案-批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public Result<?> deleteBatch(@RequestParam(name = "ids") String ids) {
        this.equipmentService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功!");
    }

    /**
     * 通过id查询
     */
    @Operation(summary = "设备档案-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name = "id") String id) {
        Equipment equipment = equipmentService.getById(id);
        if (equipment == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(equipment);
    }

    /**
     * 下载导入模板
     * 模板列：设备编号、名称、类型、部门、位置、责任人（均为必填）
     */
    @Operation(summary = "设备档案-下载导入模板")
    @GetMapping(value = "/downloadTemplate")
    public ModelAndView downloadTemplate() {
        ModelAndView mv = new ModelAndView(new JeecgEntityExcelView());
        // 空数据列表，仅生成标题行与表头行（设备编号、名称、类型、部门、位置、责任人）
        List<Equipment> list = new ArrayList<>();
        ExportParams params = new ExportParams("设备档案导入模板", "提示：以下字段均为必填，设备编号不能与库中或文件内重复", "设备档案", ExcelType.XSSF);
        mv.addObject(NormalExcelConstants.FILE_NAME, "设备档案导入模板");
        mv.addObject(NormalExcelConstants.CLASS, Equipment.class);
        mv.addObject(NormalExcelConstants.PARAMS, params);
        mv.addObject(NormalExcelConstants.DATA_LIST, list);
        return mv;
    }

    /**
     * 导入第一步：上传文件并校验（必填项 + 编号重复），不入库
     */
    @AutoLog(value = "设备档案-导入校验")
    @Operation(summary = "设备档案-上传校验")
    @PostMapping(value = "/importCheck")
    public Result<?> importCheck(HttpServletRequest request) {
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
        if (fileMap == null || fileMap.isEmpty()) {
            return Result.error("未检测到上传文件，请重新选择！");
        }
        MultipartFile file = fileMap.values().iterator().next();
        String fileName = file.getOriginalFilename();
        if (fileName == null || !(fileName.toLowerCase().endsWith(".xls") || fileName.toLowerCase().endsWith(".xlsx"))) {
            return Result.error("仅支持xls、xlsx格式的文件！");
        }
        ImportParams params = new ImportParams();
        // 与jeecg标准模板一致：前2行标题，第3行表头
        params.setTitleRows(2);
        params.setHeadRows(1);
        params.setNeedSave(false);
        List<Equipment> rows;
        try {
            rows = ExcelImportUtil.importExcel(file.getInputStream(), Equipment.class, params);
        } catch (Exception e) {
            log.error("设备档案导入解析失败", e);
            return Result.error("Excel解析失败：" + e.getMessage());
        }
        if (rows == null || rows.isEmpty()) {
            return Result.error("文件中没有可导入的数据行！");
        }

        // 库中已存在的全部编号
        List<Equipment> dbList = equipmentService.list();
        Set<String> dbCodeSet = new HashSet<>();
        for (Equipment e : dbList) {
            if (e.getEquipCode() != null) {
                dbCodeSet.add(e.getEquipCode().trim());
            }
        }

        List<EquipmentImportError> errorRows = new ArrayList<>();
        List<Equipment> validRows = new ArrayList<>();
        Set<String> fileCodeSet = new HashSet<>();
        // Excel真实行号：2行标题 + 1行表头，数据从第4行开始
        int rowNum = 3;
        for (Equipment row : rows) {
            rowNum++;
            List<String> reasons = new ArrayList<>();
            String code = trim(row.getEquipCode());
            String name = trim(row.getEquipName());
            String type = trim(row.getEquipType());
            String department = trim(row.getDepartment());
            String location = trim(row.getLocation());
            String owner = trim(row.getOwner());

            // 必填项校验
            if (code == null) {
                reasons.add("设备编号不能为空");
            }
            if (name == null) {
                reasons.add("名称不能为空");
            }
            if (type == null) {
                reasons.add("类型不能为空");
            }
            if (department == null) {
                reasons.add("部门不能为空");
            }
            if (location == null) {
                reasons.add("位置不能为空");
            }
            if (owner == null) {
                reasons.add("责任人不能为空");
            }
            // 重复编号校验：与库中重复、文件内重复
            if (code != null) {
                if (dbCodeSet.contains(code)) {
                    reasons.add("设备编号与库中已有数据重复");
                }
                if (!fileCodeSet.add(code)) {
                    reasons.add("设备编号在导入文件内重复");
                }
            }

            if (reasons.isEmpty()) {
                row.setEquipCode(code).setEquipName(name).setEquipType(type).setDepartment(department).setLocation(location).setOwner(owner);
                row.setId(null);
                validRows.add(row);
            } else {
                EquipmentImportError err = new EquipmentImportError();
                err.setRowNum(rowNum);
                err.setEquipCode(code);
                err.setEquipName(name);
                err.setEquipType(type);
                err.setDepartment(department);
                err.setLocation(location);
                err.setOwner(owner);
                err.setErrorReason(String.join("；", reasons));
                errorRows.add(err);
            }
        }

        cleanExpiredCache();
        String token = UUID.randomUUID().toString().replace("-", "");
        IMPORT_CACHE.put(token, new CacheEntry<>(validRows));
        if (!errorRows.isEmpty()) {
            ERROR_CACHE.put(token, new CacheEntry<>(errorRows));
        }

        List<EquipmentImportError> preview = errorRows.size() > ERROR_PREVIEW_SIZE
                ? new ArrayList<>(errorRows.subList(0, ERROR_PREVIEW_SIZE)) : errorRows;
        EquipmentImportResult result = new EquipmentImportResult(token, rows.size(), validRows.size(), errorRows.size(), preview);
        return Result.OK(result);
    }

    /**
     * 导入第二步：确认入库（校验通过的数据落库）
     */
    @AutoLog(value = "设备档案-确认入库")
    @Operation(summary = "设备档案-确认入库")
    @PostMapping(value = "/importConfirm")
    @Transactional(rollbackFor = Exception.class)
    public Result<?> importConfirm(@RequestParam(name = "token") String token) {
        CacheEntry<List<Equipment>> cache = IMPORT_CACHE.get(token);
        if (cache == null) {
            return Result.error("校验结果已过期，请重新上传文件校验！");
        }
        List<Equipment> validRows = cache.data;
        if (validRows == null || validRows.isEmpty()) {
            IMPORT_CACHE.remove(token);
            return Result.error("没有可入库的数据！");
        }
        // 二次兜底校验，防止确认期间编号被占用
        List<Equipment> toSave = new ArrayList<>();
        Set<String> codeSet = new HashSet<>();
        int skipCount = 0;
        for (Equipment e : validRows) {
            if (codeSet.add(e.getEquipCode())
                    && equipmentService.count(new QueryWrapper<Equipment>().eq("equip_code", e.getEquipCode())) == 0) {
                toSave.add(e);
            } else {
                skipCount++;
            }
        }
        if (!toSave.isEmpty()) {
            equipmentService.saveBatch(toSave);
        }
        IMPORT_CACHE.remove(token);
        if (skipCount > 0) {
            return Result.OK("入库完成：成功 " + toSave.size() + " 条，" + skipCount + " 条因编号被占用已跳过。");
        }
        return Result.OK("入库成功，共入库 " + toSave.size() + " 条设备档案！");
    }

    /**
     * 下载导入失败行（含失败原因）
     */
    @Operation(summary = "设备档案-下载失败行")
    @GetMapping(value = "/exportImportError")
    public ModelAndView exportImportError(@RequestParam(name = "token") String token) {
        CacheEntry<List<EquipmentImportError>> cache = ERROR_CACHE.get(token);
        List<EquipmentImportError> errorRows = cache == null ? new ArrayList<>() : cache.data;
        ModelAndView mv = new ModelAndView(new JeecgEntityExcelView());
        ExportParams params = new ExportParams("设备档案导入失败行", "失败原因", "失败明细", ExcelType.XSSF);
        mv.addObject(NormalExcelConstants.FILE_NAME, "设备档案导入失败行");
        mv.addObject(NormalExcelConstants.CLASS, EquipmentImportError.class);
        mv.addObject(NormalExcelConstants.PARAMS, params);
        mv.addObject(NormalExcelConstants.DATA_LIST, errorRows);
        return mv;
    }

    /**
     * 按当前搜索条件导出excel
     */
    @Operation(summary = "设备档案-导出")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, Equipment equipment) {
        QueryWrapper<Equipment> queryWrapper = QueryGenerator.initQueryWrapper(equipment, request.getParameterMap(), likeRuleMap());
        // 支持勾选导出
        String selections = request.getParameter("selections");
        if (selections != null && !selections.trim().isEmpty()) {
            queryWrapper.in("id", Arrays.asList(selections.split(",")));
        }
        queryWrapper.orderByDesc("create_time");
        List<Equipment> pageList = equipmentService.list(queryWrapper);
        ModelAndView mv = new ModelAndView(new JeecgEntityExcelView());
        mv.addObject(NormalExcelConstants.FILE_NAME, "设备档案");
        mv.addObject(NormalExcelConstants.CLASS, Equipment.class);
        String exportUser = "system";
        try {
            LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            if (sysUser != null && sysUser.getRealname() != null) {
                exportUser = sysUser.getRealname();
            }
        } catch (Exception ignored) {
        }
        mv.addObject(NormalExcelConstants.PARAMS,
                new ExportParams("设备档案报表", "导出人:" + exportUser, "设备档案", ExcelType.XSSF));
        mv.addObject(NormalExcelConstants.DATA_LIST, pageList);
        return mv;
    }

    private static String trim(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    /**
     * 列表/导出搜索时，文本字段统一按全模糊匹配
     */
    private static Map<String, QueryRuleEnum> likeRuleMap() {
        Map<String, QueryRuleEnum> ruleMap = new HashMap<>();
        ruleMap.put("equipCode", QueryRuleEnum.LIKE);
        ruleMap.put("equipName", QueryRuleEnum.LIKE);
        ruleMap.put("equipType", QueryRuleEnum.LIKE);
        ruleMap.put("department", QueryRuleEnum.LIKE);
        ruleMap.put("location", QueryRuleEnum.LIKE);
        ruleMap.put("owner", QueryRuleEnum.LIKE);
        return ruleMap;
    }

    /** 清理过期暂存数据 */
    private static synchronized void cleanExpiredCache() {
        long now = System.currentTimeMillis();
        IMPORT_CACHE.entrySet().removeIf(en -> now - en.getValue().createTime > EXPIRE_MILLIS);
        ERROR_CACHE.entrySet().removeIf(en -> now - en.getValue().createTime > EXPIRE_MILLIS);
    }
}
