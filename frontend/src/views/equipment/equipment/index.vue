<template>
  <div>
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <template #tableTitle>
        <a-button
          v-if="hasPermission('equipment:equipment:add')"
          preIcon="ant-design:plus-outlined"
          type="primary"
          @click="handleAdd"
          >新增</a-button
        >
        <a-button
          v-if="hasPermission('equipment:equipment:import')"
          preIcon="ant-design:import-outlined"
          type="primary"
          @click="handleImport"
          >导入</a-button
        >
        <a-button
          v-if="hasPermission('equipment:equipment:export')"
          preIcon="ant-design:export-outlined"
          type="primary"
          @click="handleExport"
          >导出</a-button
        >
        <a-dropdown v-if="checkedKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="batchHandleDelete">
                <Icon icon="ant-design:delete-outlined"></Icon>
                删除
              </a-menu-item>
            </a-menu>
          </template>
          <a-button>
            批量操作
            <Icon icon="ant-design:down-outlined" />
          </a-button>
        </a-dropdown>
      </template>
      <template #action="{ record }">
        <TableAction :actions="getActions(record)" />
      </template>
    </BasicTable>
    <EquipmentModal @register="registerModal" @success="reload" :isDisabled="isDisabled" />
    <EquipmentImportModal @register="registerImportModal" @success="reload" />
  </div>
</template>
<script lang="ts" setup>
  import { ref } from 'vue';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useMethods } from '/@/hooks/system/useMethods';
  import { usePermission } from '/@/hooks/web/usePermission';
  import EquipmentModal from './EquipmentModal.vue';
  import EquipmentImportModal from './components/EquipmentImportModal.vue';
  import {
    getEquipmentList,
    deleteEquipment,
    batchDeleteEquipment,
    getExportUrl,
  } from './equipment.api';
  import { columns, searchFormSchema } from './equipment.data';
  import { filterObj } from '/@/utils/common/compUtils';

  defineOptions({ name: 'EquipmentList' });

  const { createConfirm } = useMessage();
  const { handleExportXls } = useMethods();
  const { hasPermission } = usePermission();

  const checkedKeys = ref<Array<string | number>>([]);
  const isDisabled = ref(false);
  const [registerModal, { openModal }] = useModal();
  const [registerImportModal, { openModal: openImportModal }] = useModal();

  const [registerTable, { reload, getForm }] = useTable({
    title: '设备档案',
    api: getEquipmentList,
    columns,
    formConfig: {
      schemas: searchFormSchema,
      autoAdvancedCol: 3,
      actionColOptions: {
        style: { textAlign: 'left' },
      },
    },
    striped: true,
    useSearchForm: true,
    showTableSetting: true,
    bordered: true,
    showIndexColumn: false,
    rowKey: 'id',
    actionColumn: {
      width: 180,
      title: '操作',
      dataIndex: 'action',
      slots: { customRender: 'action' },
    },
  });

  /**
   * 选择列配置
   */
  const rowSelection = {
    type: 'checkbox',
    columnWidth: 40,
    selectedRowKeys: checkedKeys,
    onChange: onSelectChange,
  };

  function onSelectChange(keys: (string | number)[]) {
    checkedKeys.value = keys;
  }

  /**
   * 导出：按当前搜索条件生成文件（若有勾选则导出勾选数据）
   */
  function handleExport() {
    const params: any = { ...getForm().getFieldsValue() };
    if (checkedKeys.value && checkedKeys.value.length > 0) {
      params['selections'] = checkedKeys.value.join(',');
    }
    // 去掉空值搜索项
    handleExportXls('设备档案', getExportUrl, filterObj(params));
  }

  function getActions(record) {
    return [
      {
        label: '编辑',
        ifShow: () => hasPermission('equipment:equipment:edit'),
        onClick: handleEdit.bind(null, record),
      },
      {
        label: '详情',
        onClick: handleDetail.bind(null, record),
      },
      {
        label: '删除',
        ifShow: () => hasPermission('equipment:equipment:delete'),
        popConfirm: {
          title: '是否确认删除该设备档案？',
          confirm: handleDelete.bind(null, record),
        },
      },
    ];
  }

  function handleAdd() {
    isDisabled.value = false;
    openModal(true, { isUpdate: false });
  }

  function handleEdit(record) {
    isDisabled.value = false;
    openModal(true, { record, isUpdate: true });
  }

  function handleDetail(record) {
    isDisabled.value = true;
    openModal(true, { record, isUpdate: true });
  }

  function handleImport() {
    openImportModal(true, {});
  }

  async function handleDelete(record) {
    await deleteEquipment({ id: record.id }, reload);
  }

  async function batchHandleDelete() {
    createConfirm({
      iconType: 'warning',
      title: '确认删除',
      content: `是否删除选中的 ${checkedKeys.value.length} 条设备档案？`,
      onOk: async () => {
        await batchDeleteEquipment({ ids: checkedKeys.value }, () => {
          checkedKeys.value = [];
          reload();
        });
      },
    });
  }
</script>
