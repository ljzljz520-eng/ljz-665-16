import { defHttp } from '/@/utils/http/axios';

enum Api {
  list = '/device/archive/list',
  save = '/device/archive/add',
  edit = '/device/archive/edit',
  get = '/device/archive/queryById',
  delete = '/device/archive/delete',
  deleteBatch = '/device/archive/deleteBatch',
  exportXls = '/device/archive/exportXls',
  importTemplate = '/device/archive/importTemplate',
  importCheck = '/device/archive/importCheck',
  importConfirm = '/device/archive/importConfirm',
  importError = '/device/archive/importError',
}

/** 导出地址（按当前搜索条件） */
export const getExportUrl = Api.exportXls;
/** 导入模板地址 */
export const getTemplateUrl = Api.importTemplate;
/** 上传校验地址 */
export const getCheckUrl = Api.importCheck;
/** 确认入库地址 */
export const getConfirmUrl = Api.importConfirm;
/** 失败行下载地址 */
export const getErrorUrl = Api.importError;

/** 分页查询设备档案 */
export const getDeviceList = (params) => {
  return defHttp.get({ url: Api.list, params });
};

/** 新增/编辑设备档案 */
export const saveOrUpdateDevice = (params, isUpdate) => {
  const url = isUpdate ? Api.edit : Api.save;
  return defHttp.post({ url, params });
};

/** 查询设备档案详情 */
export const getDeviceById = (params) => {
  return defHttp.get({ url: Api.get, params });
};

/** 删除 */
export const deleteDevice = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.delete, data: params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess && handleSuccess();
  });
};

/** 批量删除 */
export const batchDeleteDevice = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.deleteBatch, data: params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess && handleSuccess();
  });
};
