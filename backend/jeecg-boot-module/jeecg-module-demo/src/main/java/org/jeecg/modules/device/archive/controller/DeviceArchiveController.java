package org.jeecg.modules.device.archive.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.device.archive.entity.DeviceArchive;
import org.jeecg.modules.device.archive.service.IDeviceArchiveService;
import org.jeecg.modules.device.archive.vo.DeviceArchiveImportVo;
import org.jeecg.modules.device.archive.vo.DeviceImportCheckResult;
import org.jeecg.modules.device.archive.vo.DeviceImportErrorVo;
import org.jeecgframework.poi.excel.ExcelExportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.enmus.ExcelType;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @Description: 设备档案
 * @Author: jeecg-boot
 * @Date: 2026-10-02
 * @Version: V1.0
 */
@Slf4j
@Tag(name = "设备档案")
@RestController
@RequestMapping("/device/archive")
public class DeviceArchiveController extends JeecgController<DeviceArchive, IDeviceArchiveService> {

    @Autowired
    private IDeviceArchiveService deviceArchiveService;

    /**
     * 分页列表查询
     */
    @Operation(summary = "设备档案-分页列表")
    @GetMapping("/list")
    public Result<?> list(DeviceArchive deviceArchive,
                          @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                          @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                          HttpServletRequest req) {
        // 设备名称单独走模糊查询，先取出并置空，避免生成器追加等值条件
        String deviceName = deviceArchive.getDeviceName();
        deviceArchive.setDeviceName(null);
        QueryWrapper<DeviceArchive> queryWrapper = QueryGenerator.initQueryWrapper(deviceArchive, req.getParameterMap());
        if (oConvertUtils.isNotEmpty(deviceName)) {
            queryWrapper.like("device_name", deviceName.trim().replace("*", ""));
        }
        queryWrapper.orderByDesc("create_time");
        Page<DeviceArchive> page = new Page<>(pageNo, pageSize);
        IPage<DeviceArchive> pageList = deviceArchiveService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * 添加
     */
    @AutoLog(value = "设备档案-新增")
    @Operation(summary = "设备档案-新增")
    @PostMapping("/add")
    public Result<?> add(@RequestBody DeviceArchive deviceArchive) {
        if (oConvertUtils.isEmpty(deviceArchive.getDeviceCode()) || oConvertUtils.isEmpty(deviceArchive.getDeviceName())) {
            return Result.error("设备编号和设备名称为必填项");
        }
        if (deviceArchiveService.count(new QueryWrapper<DeviceArchive>()
                .eq("device_code", deviceArchive.getDeviceCode().trim())) > 0) {
            return Result.error("设备编号已存在：" + deviceArchive.getDeviceCode());
        }
        deviceArchiveService.save(deviceArchive);
        return Result.OK("添加成功！");
    }

    /**
     * 编辑
     */
    @AutoLog(value = "设备档案-编辑", operateType = CommonConstant.OPERATE_TYPE_3)
    @Operation(summary = "设备档案-编辑")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<?> edit(@RequestBody DeviceArchive deviceArchive) {
        long dup = deviceArchiveService.count(new QueryWrapper<DeviceArchive>()
                .eq("device_code", deviceArchive.getDeviceCode())
                .ne("id", deviceArchive.getId()));
        if (dup > 0) {
            return Result.error("设备编号已存在：" + deviceArchive.getDeviceCode());
        }
        deviceArchiveService.updateById(deviceArchive);
        return Result.OK("编辑成功!");
    }

    /**
     * 通过id删除
     */
    @AutoLog(value = "设备档案-删除")
    @Operation(summary = "设备档案-删除")
    @DeleteMapping("/delete")
    public Result<?> delete(@RequestParam(name = "id") String id) {
        deviceArchiveService.removeById(id);
        return Result.OK("删除成功!");
    }

    /**
     * 批量删除
     */
    @Operation(summary = "设备档案-批量删除")
    @DeleteMapping("/deleteBatch")
    public Result<?> deleteBatch(@RequestParam(name = "ids") String ids) {
        deviceArchiveService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功！");
    }

    /**
     * 通过id查询
     */
    @Operation(summary = "设备档案-通过ID查询")
    @GetMapping("/queryById")
    public Result<?> queryById(@RequestParam(name = "id") String id) {
        return Result.OK(deviceArchiveService.getById(id));
    }

    /**
     * 导出 Excel：按当前搜索条件导出（查询参数原样传入，查询逻辑与 list 一致）
     */
    @Operation(summary = "设备档案-按搜索条件导出")
    @RequestMapping(value = "/exportXls", method = {RequestMethod.GET, RequestMethod.POST})
    public ModelAndView exportXls(HttpServletRequest request, DeviceArchive deviceArchive) {
        // Step.1 组装查询条件（名称走模糊，与 list 一致）
        String deviceName = deviceArchive.getDeviceName();
        deviceArchive.setDeviceName(null);
        QueryWrapper<DeviceArchive> queryWrapper = QueryGenerator.initQueryWrapper(deviceArchive, request.getParameterMap());
        if (oConvertUtils.isNotEmpty(deviceName)) {
            queryWrapper.like("device_name", deviceName.trim().replace("*", ""));
        }
        // 勾选行时只导出选中的数据
        String selections = request.getParameter("selections");
        if (oConvertUtils.isNotEmpty(selections)) {
            queryWrapper.in("id", Arrays.asList(selections.split(",")));
        }
        // Step.2 获取导出数据
        List<DeviceArchive> exportList = deviceArchiveService.list(queryWrapper);
        // Step.3 AutoPoi 导出 Excel
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        ModelAndView mv = new ModelAndView(new JeecgEntityExcelView());
        mv.addObject(NormalExcelConstants.FILE_NAME, "设备档案");
        mv.addObject(NormalExcelConstants.CLASS, DeviceArchive.class);
        ExportParams exportParams = new ExportParams("设备档案报表", "导出人:" + (sysUser == null ? "" : sysUser.getRealname()), "设备档案", ExcelType.XSSF);
        mv.addObject(NormalExcelConstants.PARAMS, exportParams);
        mv.addObject(NormalExcelConstants.DATA_LIST, exportList);
        String exportFields = request.getParameter(NormalExcelConstants.EXPORT_FIELDS);
        if (oConvertUtils.isNotEmpty(exportFields)) {
            mv.addObject(NormalExcelConstants.EXPORT_FIELDS, exportFields);
        }
        return mv;
    }

    /**
     * 下载导入模板
     */
    @Operation(summary = "设备档案-下载导入模板")
    @GetMapping("/importTemplate")
    public ModelAndView importTemplate() {
        ModelAndView mv = new ModelAndView(new JeecgEntityExcelView());
        mv.addObject(NormalExcelConstants.FILE_NAME, "设备档案导入模板");
        mv.addObject(NormalExcelConstants.CLASS, DeviceArchiveImportVo.class);
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        ExportParams exportParams = new ExportParams(
                "导入规则：\n1. 设备编号、设备名称为必填项，设备编号不可与库中数据或文件内其他行重复；\n2. 部门、类型、位置、责任人请按文本填写，请勿修改表头。",
                "导出人:" + (user == null ? "" : user.getRealname()),
                "设备档案",
                ExcelType.XSSF);
        mv.addObject(NormalExcelConstants.PARAMS, exportParams);
        mv.addObject(NormalExcelConstants.DATA_LIST, new ArrayList<DeviceArchiveImportVo>());
        return mv;
    }

    /**
     * 导入第一步：上传文件并校验（必填项 + 重复编号），暂存校验通过的数据，等待确认入库
     */
    @AutoLog(value = "设备档案-导入校验")
    @Operation(summary = "设备档案-上传校验")
    @PostMapping("/importCheck")
    public Result<?> importCheck(HttpServletRequest request) {
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
        if (fileMap.isEmpty()) {
            return Result.error("未获取到上传文件，请重新选择");
        }
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        String username = user == null ? null : user.getUsername();
        try {
            for (Map.Entry<String, MultipartFile> entry : fileMap.entrySet()) {
                MultipartFile file = entry.getValue();
                String originalName = file.getOriginalFilename();
                DeviceImportCheckResult result = deviceArchiveService.checkImport(file, originalName, username);
                return Result.OK(result);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Result.error(e.getMessage());
        }
        return Result.error("文件导入校验失败！");
    }

    /**
     * 导入第二步：确认入库
     */
    @AutoLog(value = "设备档案-确认入库")
    @Operation(summary = "设备档案-确认入库")
    @PostMapping("/importConfirm")
    public Result<?> importConfirm(@RequestParam(name = "sessionId") String sessionId) {
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        String username = user == null ? null : user.getUsername();
        try {
            int count = deviceArchiveService.confirmImport(sessionId, username);
            return Result.OK("确认入库成功，共入库 " + count + " 条设备档案！");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 下载失败行文件（包含失败原因列）
     */
    @Operation(summary = "设备档案-下载失败行")
    @GetMapping("/importError")
    public void importError(@RequestParam(name = "sessionId") String sessionId, HttpServletResponse response) {
        List<DeviceImportErrorVo> errorList = deviceArchiveService.getErrorList(sessionId);
        if (errorList == null) {
            errorList = new ArrayList<>();
        }
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        String fileName = URLEncoder.encode("设备档案导入失败行.xlsx", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment;filename*=utf-8''" + fileName);
        ExportParams params = new ExportParams("设备档案导入失败行", "系统", "失败数据", ExcelType.XSSF);
        org.apache.poi.ss.usermodel.Workbook workbook = ExcelExportUtil.exportExcel(params, DeviceImportErrorVo.class, errorList);
        try {
            workbook.write(response.getOutputStream());
        } catch (Exception e) {
            log.error("失败行文件导出失败：{}", e.getMessage(), e);
            throw new RuntimeException("失败行文件导出失败：" + e.getMessage());
        } finally {
            try {
                workbook.close();
            } catch (Exception ignored) {
            }
        }
    }
}
