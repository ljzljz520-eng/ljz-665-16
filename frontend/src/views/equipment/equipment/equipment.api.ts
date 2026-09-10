import { defHttp } from '/@/utils/http/axios';

export enum Api {
  list = '/equipment/equipment/list',
  save = '/equipment/equipment/add',
  edit = '/equipment/equipment/edit',
  get = '/equipment/equipment/queryById',
  delete = '/equipment/equipment/delete',
  deleteBatch = '/equipment/equipment/deleteBatch',
  exportXls = '/equipment/equipment/exportXls',
  downloadTemplate = '/equipment/equipment/downloadTemplate',
  importCheck = '/equipment/equipment/importCheck',
  importConfirm = '/equipment/equipment/importConfirm',
  exportImportError = '/equipment/equipment/exportImportError',
}

/**
 * 导出地址（列表页导出按钮使用，随当前搜索条件）
 */
export const getExportUrl = Api.exportXls;

/**
 * 导入模板下载地址
 */
export const getTemplateUrl = Api.downloadTemplate;

/**
 * 导入失败行下载地址
 */
export const getImportErrorUrl = Api.exportImportError;

/**
 * 查询设备档案列表
 * @param params
 */
export const getEquipmentList = (params) => {
  return defHttp.get({ url: Api.list, params });
};

/**
 * 新增/编辑设备档案
 * @param params
 * @param isUpdate
 */
export const saveOrUpdateEquipment = (params, isUpdate) => {
  let url = isUpdate ? Api.edit : Api.save;
  return defHttp.post({ url, params });
};

/**
 * 查询设备档案详情
 * @param params
 */
export const getEquipmentById = (params) => {
  return defHttp.get({ url: Api.get, params });
};

/**
 * 删除设备档案
 */
export const deleteEquipment = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.delete, data: params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};

/**
 * 批量删除设备档案
 */
export const batchDeleteEquipment = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.deleteBatch, data: params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};

/**
 * 导入第一步：上传校验（不入库）
 * @param file
 */
export const importCheckEquipment = (file) => {
  return defHttp.uploadFile({ url: Api.importCheck }, { file }, { isReturnResponse: true });
};

/**
 * 导入第二步：确认入库
 * @param token 校验批次令牌
 */
export const importConfirmEquipment = (token) => {
  return defHttp.post({ url: Api.importConfirm, data: { token } }, { joinParamsToUrl: true });
};
