<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    title="设备档案导入"
    :width="720"
    :showOkBtn="false"
    :showCancelBtn="false"
    :destroyOnClose="true"
    @cancel="handleCancel"
  >
    <div class="equipment-import">
      <!-- 第一步：选择文件 -->
      <a-alert type="info" show-icon style="margin-bottom: 12px">
        <template #message>
          请先
          <a @click="handleDownloadTemplate">下载导入模板</a>
          ，按模板填写后上传；模板包含设备编号、名称、类型、部门、位置、责任人6个必填字段。
        </template>
      </a-alert>

      <a-spin :spinning="uploading" tip="正在校验，请稍候...">
        <a-upload-dragger
          name="file"
          :multiple="false"
          :showUploadList="false"
          :before-upload="beforeUpload"
          :customRequest="handleCustomUpload"
          accept=".xls,.xlsx"
          :disabled="uploading || confirming"
        >
          <p class="ant-upload-drag-icon">
            <Icon icon="ant-design:inbox-outlined" />
          </p>
          <p class="ant-upload-text">点击或将Excel文件拖拽到此处上传</p>
          <p class="ant-upload-hint">支持 .xls / .xlsx；校验通过后可重新上传覆盖</p>
        </a-upload-dragger>
      </a-spin>

      <div v-if="fileName" class="file-name">
        <Icon icon="ant-design:file-excel-outlined" />
        <span style="margin-left: 6px">{{ fileName }}</span>
      </div>

      <!-- 第二步：校验结果 -->
      <div v-if="checkResult" class="check-result">
        <a-divider style="margin: 12px 0" />
        <a-alert
          :type="checkResult.errorCount > 0 ? 'warning' : 'success'"
          show-icon
          style="margin-bottom: 12px"
        >
          <template #message>
            共解析 <b>{{ checkResult.totalCount }}</b> 行，校验通过
            <b style="color: #52c41a">{{ checkResult.successCount }}</b> 行，失败
            <b style="color: #faad14">{{ checkResult.errorCount }}</b> 行。
          </template>
        </a-alert>

        <a-table
          v-if="checkResult.errorRows && checkResult.errorRows.length > 0"
          size="small"
          bordered
          rowKey="rowNum"
          :columns="errorColumns"
          :dataSource="checkResult.errorRows"
          :pagination="false"
          :scroll="{ y: 220 }"
        >
          <template #bodyCell="{ column, record }">
            <a-tooltip v-if="column.dataIndex === 'errorReason'" :title="record.errorReason">
              <span style="color: #fa541c">{{ record.errorReason }}</span>
            </a-tooltip>
          </template>
        </a-table>

        <div v-if="checkResult.errorCount > checkResult.errorRows.length" class="more-tip">
          仅展示前 {{ checkResult.errorRows.length }} 条失败记录，
          <a @click="handleDownloadError">下载全部失败行查看原因</a>
        </div>
        <div v-else-if="checkResult.errorCount > 0" class="more-tip">
          <a @click="handleDownloadError">下载失败行查看原因</a>
        </div>
      </div>
    </div>

    <template #footer>
      <a-button @click="handleCancel" :disabled="confirming">关闭</a-button>
      <template v-if="checkResult">
        <a-button
          v-if="checkResult.errorCount > 0"
          @click="handleDownloadError"
          style="margin-right: 8px"
        >
          <Icon icon="ant-design:download-outlined" /> 下载失败行
        </a-button>
        <a-button
          type="primary"
          @click="handleConfirm"
          :loading="confirming"
          :disabled="checkResult.successCount === 0"
        >
          确认入库（{{ checkResult.successCount }}条）
        </a-button>
      </template>
    </template>
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { downloadFile } from '/@/api/common/api';
  import {
    getTemplateUrl,
    getImportErrorUrl,
    importCheckEquipment,
    importConfirmEquipment,
  } from '../equipment.api';

  const emit = defineEmits(['register', 'success']);
  const { createMessage } = useMessage();

  const uploading = ref(false);
  const confirming = ref(false);
  const fileName = ref('');
  const checkResult = ref<any>(null);

  const errorColumns = [
    { title: '行号', dataIndex: 'rowNum', width: 60, align: 'center' },
    { title: '设备编号', dataIndex: 'equipCode', width: 130, ellipsis: true },
    { title: '名称', dataIndex: 'equipName', width: 140, ellipsis: true },
    { title: '类型', dataIndex: 'equipType', width: 100, ellipsis: true },
    { title: '部门', dataIndex: 'department', width: 120, ellipsis: true },
    { title: '位置', dataIndex: 'location', width: 130, ellipsis: true },
    { title: '责任人', dataIndex: 'owner', width: 90, ellipsis: true },
    { title: '失败原因', dataIndex: 'errorReason', ellipsis: true },
  ];

  const [registerModal, { closeModal }] = useModalInner(() => {
    resetState();
  });

  function resetState() {
    uploading.value = false;
    confirming.value = false;
    fileName.value = '';
    checkResult.value = null;
  }

  function beforeUpload(file: File) {
    const name = file.name.toLowerCase();
    if (!name.endsWith('.xls') && !name.endsWith('.xlsx')) {
      createMessage.warning('仅支持 xls、xlsx 格式的文件');
      return false;
    }
    return true;
  }

  /** 自定义上传：调用导入校验接口（不入库） */
  async function handleCustomUpload(option: any) {
    const file = option.file as File;
    fileName.value = file.name;
    uploading.value = true;
    checkResult.value = null;
    try {
      const res: any = await importCheckEquipment(file);
      if (res && res.success) {
        checkResult.value = res.result;
        createMessage.success('文件校验完成');
      } else {
        createMessage.error(res?.message || '文件校验失败');
      }
    } catch (e: any) {
      createMessage.error(e?.message || '文件上传校验失败');
    } finally {
      uploading.value = false;
    }
  }

  /** 确认入库 */
  async function handleConfirm() {
    if (!checkResult.value?.token) {
      createMessage.warning('校验结果缺失，请重新上传文件');
      return;
    }
    confirming.value = true;
    try {
      const res: any = await importConfirmEquipment(checkResult.value.token);
      if (res && res.success) {
        createMessage.success(res.message || '入库成功');
        emit('success');
        closeModal();
      } else {
        createMessage.error(res?.message || '入库失败');
      }
    } catch (e: any) {
      createMessage.error(e?.message || '入库失败');
    } finally {
      confirming.value = false;
    }
  }

  /** 下载导入模板 */
  function handleDownloadTemplate() {
    downloadFile(getTemplateUrl, '设备档案导入模板.xlsx');
  }

  /** 下载失败行（含失败原因） */
  function handleDownloadError() {
    if (!checkResult.value?.token) {
      createMessage.warning('校验结果已失效，请重新上传文件');
      return;
    }
    downloadFile(getImportErrorUrl, '设备档案导入失败行.xlsx', { token: checkResult.value.token });
  }

  function handleCancel() {
    closeModal();
  }
</script>

<style lang="less" scoped>
  .equipment-import {
    .file-name {
      margin-top: 8px;
      color: #555;
    }
    .check-result {
      .more-tip {
        margin-top: 8px;
        font-size: 13px;
        color: #888;
      }
    }
  }
</style>
