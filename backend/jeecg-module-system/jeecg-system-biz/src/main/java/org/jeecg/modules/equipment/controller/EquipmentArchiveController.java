package org.jeecg.modules.equipment.controller;

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
import org.jeecg.config.JeecgBaseConfig;
import org.jeecg.modules.equipment.entity.EquipmentArchive;
import org.jeecg.modules.equipment.service.IEquipmentArchiveService;
import org.jeecg.modules.equipment.vo.EquipmentImportVO;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.enmus.ExcelType;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import jakarta.annotation.Resource;
import java.util.Arrays;
import java.util.Map;

/**
 * @Description: 设备档案
 * @Author: equipment
 * @Date: 2026-10-02
 * @Version: V1.0
 */
@Slf4j
@Tag(name = "设备档案")
@RestController
@RequestMapping("/equipment/archive")
public class EquipmentArchiveController extends JeecgController<EquipmentArchive, IEquipmentArchiveService> {

    @Autowired
    private IEquipmentArchiveService equipmentArchiveService;

    @Resource
    private JeecgBaseConfig jeecgBaseConfig;

    /**
     * 分页列表查询
     *
     * @param equipmentArchive
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @Operation(summary = "设备档案-分页列表查询")
    @GetMapping(value = "/list")
    public Result<?> list(EquipmentArchive equipmentArchive,
                          @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                          @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                          HttpServletRequest req) {
        QueryWrapper<EquipmentArchive> queryWrapper = QueryGenerator.initQueryWrapper(equipmentArchive, req.getParameterMap());
        queryWrapper.orderByDesc("create_time");
        Page<EquipmentArchive> page = new Page<>(pageNo, pageSize);
        IPage<EquipmentArchive> pageList = equipmentArchiveService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * 添加
     *
     * @param equipmentArchive
     * @return
     */
    @AutoLog(value = "设备档案-添加")
    @Operation(summary = "设备档案-添加")
    @PostMapping(value = "/add")
    public Result<?> add(@RequestBody EquipmentArchive equipmentArchive) {
        // 新增前校验编号唯一
        long count = equipmentArchiveService.count(new QueryWrapper<EquipmentArchive>()
                .eq("equip_no", equipmentArchive.getEquipNo()));
        if (count > 0) {
            return Result.error("设备编号【" + equipmentArchive.getEquipNo() + "】已存在，请勿重复添加。");
        }
        equipmentArchiveService.save(equipmentArchive);
        return Result.OK("添加成功！");
    }

    /**
     * 编辑
     *
     * @param equipmentArchive
     * @return
     */
    @AutoLog(value = "设备档案-编辑", operateType = CommonConstant.OPERATE_TYPE_3)
    @Operation(summary = "设备档案-编辑")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<?> edit(@RequestBody EquipmentArchive equipmentArchive) {
        // 编辑时校验编号唯一（排除自身）
        long count = equipmentArchiveService.count(new QueryWrapper<EquipmentArchive>()
                .eq("equip_no", equipmentArchive.getEquipNo())
                .ne("id", equipmentArchive.getId()));
        if (count > 0) {
            return Result.error("设备编号【" + equipmentArchive.getEquipNo() + "】已存在，请修改。");
        }
        equipmentArchiveService.updateById(equipmentArchive);
        return Result.OK("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "设备档案-删除")
    @Operation(summary = "设备档案-通过id删除")
    @DeleteMapping(value = "/delete")
    public Result<?> delete(@RequestParam(name = "id") String id) {
        equipmentArchiveService.removeById(id);
        return Result.OK("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "设备档案-批量删除")
    @Operation(summary = "设备档案-批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public Result<?> deleteBatch(@RequestParam(name = "ids") String ids) {
        this.equipmentArchiveService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功!");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @Operation(summary = "设备档案-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name = "id") String id) {
        EquipmentArchive equipmentArchive = equipmentArchiveService.getById(id);
        if (equipmentArchive == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(equipmentArchive);
    }

    /**
     * 下载导入模板（含设备编号、名称、类型、部门、位置、责任人）
     *
     * @return
     */
    @Operation(summary = "设备档案-下载导入模板")
    @RequestMapping(value = "/importTemplate")
    public ModelAndView importTemplate() {
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        ModelAndView mv = new ModelAndView(new JeecgEntityExcelView());
        mv.addObject(NormalExcelConstants.FILE_NAME, "设备档案导入模板");
        mv.addObject(NormalExcelConstants.CLASS, EquipmentImportVO.class);
        // 模板与导入解析保持一致：2行标题 + 1行表头
        ExportParams exportParams = new ExportParams("设备档案导入模板", "导出人:" + (sysUser == null ? "系统" : sysUser.getRealname()),
                "设备档案", ExcelType.XSSF);
        exportParams.setImageBasePath(jeecgBaseConfig.getPath().getUpload());
        mv.addObject(NormalExcelConstants.PARAMS, exportParams);
        // 空数据模板，仅输出标题与表头
        mv.addObject(NormalExcelConstants.DATA_LIST, java.util.Collections.emptyList());
        return mv;
    }

    /**
     * 导入第一步：上传文件校验（校验必填项、编号重复，不写库，返回确认凭证）
     *
     * @param request
     * @param response
     * @return
     */
    @AutoLog(value = "设备档案-导入校验")
    @Operation(summary = "设备档案-导入校验")
    @PostMapping(value = "/importCheck")
    public Result<?> importCheck(HttpServletRequest request, HttpServletResponse response) {
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        Map<String, org.springframework.web.multipart.MultipartFile> fileMap = multipartRequest.getFileMap();
        if (fileMap.isEmpty()) {
            return Result.error("未检测到上传文件，请重新选择文件。");
        }
        org.springframework.web.multipart.MultipartFile file = fileMap.values().iterator().next();
        return equipmentArchiveService.importCheck(file);
    }

    /**
     * 导入第二步：确认入库
     *
     * @param request
     * @return
     */
    @AutoLog(value = "设备档案-确认入库")
    @Operation(summary = "设备档案-确认入库")
    @PostMapping(value = "/importConfirm")
    public Result<?> importConfirm(HttpServletRequest request) {
        return equipmentArchiveService.importConfirm(request);
    }

    /**
     * 导出excel：按当前搜索条件导出（selections 不为空时导出勾选行）
     *
     * @param request
     * @param equipmentArchive
     */
    @Operation(summary = "设备档案-导出")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, EquipmentArchive equipmentArchive) {
        return super.exportXls(request, equipmentArchive, EquipmentArchive.class, "设备档案");
    }
}
