import { defHttp } from '/@/utils/http/axios';
import { Modal } from 'ant-design-vue';

enum Api {
  list = '/equipment/archive/list',
  save = '/equipment/archive/add',
  edit = '/equipment/archive/edit',
  get = '/equipment/archive/queryById',
  delete = '/equipment/archive/delete',
  deleteBatch = '/equipment/archive/deleteBatch',
  exportXls = '/equipment/archive/exportXls',
  importTemplate = '/equipment/archive/importTemplate',
  importCheck = '/equipment/archive/importCheck',
  importConfirm = '/equipment/archive/importConfirm',
}

/**
 * 导出地址（按当前搜索条件导出）
 */
export const getExportUrl = Api.exportXls;
/**
 * 导入模板下载地址
 */
export const getImportTemplateUrl = Api.importTemplate;
/**
 * 导入校验地址
 */
export const getImportCheckUrl = Api.importCheck;
/**
 * 确认入库地址
 */
export const getImportConfirmUrl = Api.importConfirm;

/**
 * 查询设备档案列表
 * @param params
 */
export const getEquipmentList = (params) => {
  return defHttp.get({ url: Api.list, params });
};

/**
 * 保存或更新设备档案
 * @param params
 * @param isUpdate
 */
export const saveOrUpdateEquipment = (params, isUpdate) => {
  const url = isUpdate ? Api.edit : Api.save;
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
 * @param params
 * @param handleSuccess
 */
export const deleteEquipment = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.delete, data: params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};

/**
 * 批量删除设备档案
 * @param params
 * @param handleSuccess
 */
export const batchDeleteEquipment = (params, handleSuccess) => {
  Modal.confirm({
    title: '确认删除',
    content: '是否删除选中数据',
    okText: '确认',
    cancelText: '取消',
    onOk: () => {
      return defHttp.delete({ url: Api.deleteBatch, data: params }, { joinParamsToUrl: true }).then(() => {
        handleSuccess();
      });
    },
  });
};

/**
 * 下载导入模板（携带登录 token，以 blob 方式下载）
 */
export const downloadImportTemplate = async () => {
  const response = await defHttp.get(
    { url: Api.importTemplate, responseType: 'blob' },
    { isTransformResponse: false, isReturnNativeResponse: true },
  );
  if (!response || !response.data) {
    return;
  }
  const blob = new Blob([response.data], {
    type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
  });
  const url = window.URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.style.display = 'none';
  link.href = url;
  link.setAttribute('download', '设备档案导入模板.xlsx');
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  window.URL.revokeObjectURL(url);
};
