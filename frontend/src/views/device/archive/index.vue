<template>
  <div class="p-2">
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <template #tableTitle>
        <a-button preIcon="ant-design:plus-outlined" type="primary" @click="handleAdd">新增</a-button>
        <a-button preIcon="ant-design:import-outlined" type="primary" @click="handleImport">导入</a-button>
        <a-button preIcon="ant-design:export-outlined" type="primary" @click="handleExport">导出</a-button>
        <a-dropdown v-if="checkedKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="batchHandleDelete">
                <Icon icon="ant-design:delete-outlined"></Icon>
                批量删除
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
    <DeviceModal @register="registerModal" @success="reload" />
    <DeviceImportModal @register="registerImportModal" @success="reload" />
  </div>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  import { Modal } from 'ant-design-vue';
  import DeviceModal from './DeviceModal.vue';
  import DeviceImportModal from './DeviceImportModal.vue';
  import { useMethods } from '/@/hooks/system/useMethods';
  import {
    getDeviceList,
    deleteDevice,
    batchDeleteDevice,
    getExportUrl,
  } from './deviceArchive.api';
  import { columns, searchFormSchema } from './deviceArchive.data';

  const checkedKeys = ref<Array<string | number>>([]);
  const [registerModal, { openModal }] = useModal();
  const [registerImportModal, { openModal: openImportModal }] = useModal();
  const { handleExportXls } = useMethods();

  const [registerTable, { reload, getForm }] = useTable({
    title: '设备档案',
    api: getDeviceList,
    columns,
    formConfig: {
      schemas: searchFormSchema,
      autoAdvancedCol: 3,
    },
    striped: true,
    useSearchForm: true,
    showTableSetting: true,
    bordered: true,
    rowKey: 'id',
    actionColumn: {
      width: 160,
      title: '操作',
      dataIndex: 'action',
      slots: { customRender: 'action' },
    },
  });

  /** 行选择配置 */
  const rowSelection = {
    type: 'checkbox',
    columnWidth: 40,
    selectedRowKeys: checkedKeys,
    onChange: onSelectChange,
  };

  function onSelectChange(selectedRowKeys: (string | number)[]) {
    checkedKeys.value = selectedRowKeys;
  }

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

  function handleImport() {
    openImportModal(true);
  }

  async function handleDelete(record) {
    await deleteDevice({ id: record.id }, reload);
  }

  function batchHandleDelete() {
    Modal.confirm({
      title: '确认删除',
      content: `是否删除选中的 ${checkedKeys.value.length} 条设备档案？`,
      okText: '确认',
      cancelText: '取消',
      onOk: async () => {
        await batchDeleteDevice({ ids: checkedKeys.value.join(',') }, () => {
          checkedKeys.value = [];
          reload();
        });
      },
    });
  }

  /**
   * 导出：按当前搜索条件导出
   * 1. 优先导出列表勾选行；2. 未勾选时携带当前查询表单的全部条件
   */
  async function handleExport() {
    let params: any = {};
    if (checkedKeys.value.length > 0) {
      params.selections = checkedKeys.value.join(',');
    } else {
      const formData = getForm().getFieldsValue();
      // 剔除空值，避免空条件干扰
      Object.keys(formData || {}).forEach((key) => {
        const val = formData[key];
        if (val !== undefined && val !== null && val !== '') {
          params[key] = val;
        }
      });
    }
    await handleExportXls('设备档案', getExportUrl, params);
  }
</script>
