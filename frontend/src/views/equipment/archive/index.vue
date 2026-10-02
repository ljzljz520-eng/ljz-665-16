<template>
  <div class="p-2">
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <template #tableTitle>
        <a-button type="primary" preIcon="ant-design:plus-outlined" @click="handleAdd">新增</a-button>
        <a-button type="primary" preIcon="ant-design:export-outlined" @click="onExportXls">导出</a-button>
        <a-button type="primary" preIcon="ant-design:import-outlined" @click="handleImport">导入</a-button>
        <a-dropdown v-if="selectedRowKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="batchHandleDelete">
                <Icon icon="ant-design:delete-outlined" />
                删除
              </a-menu-item>
            </a-menu>
          </template>
          <a-button type="primary">
            批量操作
            <Icon icon="ant-design:down-outlined" />
          </a-button>
        </a-dropdown>
      </template>
      <template #action="{ record }">
        <TableAction :actions="getActions(record)" />
      </template>
    </BasicTable>
    <EquipmentModal @register="registerModal" @success="reload" />
    <EquipmentImportModal @register="registerImportModal" @success="reload" />
  </div>
</template>
<script lang="ts" setup>
  import { BasicTable, TableAction } from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { useMethods } from '/@/hooks/system/useMethods';
  import { useModal } from '/@/components/Modal';
  import EquipmentModal from './components/EquipmentModal.vue';
  import EquipmentImportModal from './components/EquipmentImportModal.vue';
  import {
    getEquipmentList,
    deleteEquipment,
    batchDeleteEquipment,
    getExportUrl,
  } from './equipment.api';
  import { columns, searchFormSchema } from './equipment.data';
  import { filterObj } from '/@/utils/common/compUtils';

  const [registerModal, { openModal }] = useModal();
  const [registerImportModal, { openModal: openImportModal }] = useModal();
  const { handleExportXls } = useMethods();

  const { tableContext } = useListPage({
    tableProps: {
      title: '设备档案',
      api: getEquipmentList,
      columns,
      formConfig: {
        schemas: searchFormSchema,
        autoAdvancedCol: 3,
      },
      beforeFetch: (params) => {
        // 文本搜索条件统一按全模糊查询（JeecgBoot QueryGenerator 约定：*关键词*）
        ['equipNo', 'equipName', 'equipType', 'department', 'location', 'owner'].forEach((key) => {
          if (params[key] && typeof params[key] === 'string' && !params[key].includes('*')) {
            params[key] = `*${params[key].trim()}*`;
          }
        });
        return params;
      },
      striped: true,
      showIndexColumn: false,
      actionColumn: {
        width: 140,
        title: '操作',
        dataIndex: 'action',
        slots: { customRender: 'action' },
      },
    },
  });
  const [registerTable, { reload, getForm }, { rowSelection, selectedRowKeys }] = tableContext;

  function getActions(record) {
    return [
      {
        label: '编辑',
        onClick: handleEdit.bind(null, record),
      },
      {
        label: '删除',
        popConfirm: {
          title: '是否确认删除该设备档案？',
          confirm: handleDelete.bind(null, record),
        },
      },
    ];
  }

  function handleAdd() {
    openModal(true, { isUpdate: false });
  }

  function handleEdit(record) {
    openModal(true, { record, isUpdate: true });
  }

  async function handleDelete(record) {
    await deleteEquipment({ id: record.id }, reload);
  }

  async function batchHandleDelete() {
    await batchDeleteEquipment({ ids: selectedRowKeys.value }, reload);
  }

  /**
   * 导出：按当前搜索条件导出（勾选了行则导出勾选行）
   */
  async function onExportXls() {
    // 与列表查询一致，获取当前搜索表单中的查询条件；校验异常时中止导出
    const form = getForm();
    let params: Record<string, any> = {};
    try {
      params = form && form.validate ? await form.validate() : {};
    } catch (e) {
      return;
    }
    // 与列表查询保持一致：文本条件全模糊
    ['equipNo', 'equipName', 'equipType', 'department', 'location', 'owner'].forEach((key) => {
      if (params[key] && typeof params[key] === 'string' && !params[key].includes('*')) {
        params[key] = `*${params[key].trim()}*`;
      }
    });
    if (selectedRowKeys.value && selectedRowKeys.value.length > 0) {
      params['selections'] = selectedRowKeys.value.join(',');
    }
    handleExportXls('设备档案', getExportUrl, filterObj(params));
  }

  function handleImport() {
    openImportModal(true);
  }
</script>
