<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    title="导入设备档案"
    :width="800"
    :showOkBtn="false"
    @cancel="handleClose"
    destroyOnClose
  >
    <div class="device-import">
      <!-- 第一步：下载模板、上传文件 -->
      <div class="import-step">
        <div class="step-tip">
          <span>第一步：请先</span>
          <a @click="handleDownloadTemplate">下载导入模板</a>
          <span>，按模板填写后上传，系统将校验必填项（设备编号、名称）与重复编号。</span>
        </div>
        <a-upload
          name="file"
          accept=".xls,.xlsx"
          :maxCount="1"
          :fileList="fileList"
          :beforeUpload="beforeUpload"
          @remove="handleRemove"
        >
          <a-button preIcon="ant-design:upload-outlined" :loading="uploading">
            {{ uploading ? '校验中...' : '选择文件并校验' }}
          </a-button>
        </a-upload>
      </div>

      <!-- 第二步：校验结果 -->
      <div v-if="checkResult" class="import-result">
        <a-divider orientation="left" style="font-size: 14px">校验结果（{{ checkResult.fileName }}）</a-divider>
        <a-alert class="result-alert" :type="checkResult.errorCount > 0 ? 'warning' : 'success'" show-icon>
          <template #message>
            共解析 <b>{{ checkResult.totalCount }}</b> 行，校验通过
            <b style="color: #52c41a">{{ checkResult.validCount }}</b> 行，失败
            <b style="color: #faad14">{{ checkResult.errorCount }}</b> 行。
          </template>
        </a-alert>

        <div v-if="checkResult.errorCount > 0" class="error-area">
          <div class="error-toolbar">
            <span>失败行不会入库，请修正后重新上传；也可下载失败文件查看具体原因：</span>
            <a-button type="link" @click="handleDownloadError">
              <Icon icon="ant-design:download-outlined" />
              下载失败行（{{ checkResult.errorCount }} 行）
            </a-button>
          </div>
          <a-table
            v-if="checkResult.errorPreview && checkResult.errorPreview.length"
            size="small"
            :columns="errorColumns"
            :dataSource="checkResult.errorPreview"
            :pagination="false"
            :scroll="{ y: 220 }"
            rowKey="rowNum"
          />
        </div>

        <div v-if="checkResult.validCount > 0" class="confirm-area">
          <span>校验通过的数据尚未入库，请确认后入库：</span>
          <a-button type="primary" :loading="confirming" @click="handleConfirm">
            <Icon icon="ant-design:check-circle-outlined" />
            确认入库（{{ checkResult.validCount }} 行）
          </a-button>
          <span v-if="checkResult.errorCount > 0" class="confirm-hint">仅入库校验通过的 {{ checkResult.validCount }} 行</span>
        </div>
        <a-empty v-if="checkResult.validCount === 0" description="没有可入库的数据，请修改文件后重新上传" />
      </div>
    </div>
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { defHttp } from '/@/utils/http/axios';
  import { getCheckUrl, getConfirmUrl, getErrorUrl, getTemplateUrl } from './deviceArchive.api';
  import { downloadByData } from '/@/utils/file/download';

  const emit = defineEmits(['register', 'success']);
  const { createMessage } = useMessage();

  const [registerModal, { closeModal }] = useModalInner(() => {
    reset();
  });

  const fileList = ref<any[]>([]);
  const uploading = ref(false);
  const confirming = ref(false);
  const checkResult = ref<any>(null);

  const errorColumns = [
    { title: '行号', dataIndex: 'rowNum', width: 70 },
    { title: '设备编号', dataIndex: 'deviceCode', width: 130 },
    { title: '设备名称', dataIndex: 'deviceName', width: 150 },
    { title: '失败原因', dataIndex: 'errorReason' },
  ];

  function reset() {
    fileList.value = [];
    uploading.value = false;
    confirming.value = false;
    checkResult.value = null;
  }

  function handleClose() {
    closeModal();
  }

  function handleRemove() {
    fileList.value = [];
    checkResult.value = null;
  }

  /** 选择文件后直接上传校验 */
  function beforeUpload(file) {
    const name = file.name || '';
    if (!/\.(xls|xlsx)$/i.test(name)) {
      createMessage.warning('仅支持 .xls / .xlsx 格式文件');
      return false;
    }
    doCheck(file);
    // 阻止 antd 自动上传，列表展示由我们自己控制
    fileList.value = [
      {
        uid: '-1',
        name,
        status: 'done',
      },
    ];
    return false;
  }

  async function doCheck(file) {
    uploading.value = true;
    checkResult.value = null;
    const formData = new FormData();
    formData.append('file', file);
    try {
      const res = await defHttp.post(
        {
          url: getCheckUrl,
          params: formData,
          headers: { 'Content-Type': 'multipart/form-data;boundary=' + new Date().getTime() },
        },
        { isTransformResponse: false }
      );
      if (res && res.success) {
        checkResult.value = res.result;
      } else if (res && res.message) {
        createMessage.error(res.message);
        fileList.value = [];
      }
    } catch (e: any) {
      createMessage.error(e?.message || '文件校验失败，请重试');
      fileList.value = [];
    } finally {
      uploading.value = false;
    }
  }

  /** 确认入库 */
  async function handleConfirm() {
    if (!checkResult.value?.sessionId) {
      createMessage.warning('校验结果已失效，请重新上传文件');
      return;
    }
    confirming.value = true;
    try {
      await defHttp.post({
        url: getConfirmUrl,
        params: { sessionId: checkResult.value.sessionId },
      });
      createMessage.success(`入库成功，共入库 ${checkResult.value.validCount} 条设备档案`);
      emit('success');
      closeModal();
    } finally {
      confirming.value = false;
    }
  }

  /** 下载导入模板（走鉴权请求，避免未登录跳转） */
  async function handleDownloadTemplate() {
    const data = await defHttp.get({ url: getTemplateUrl, responseType: 'blob' }, { isTransformResponse: false });
    downloadByData(data, '设备档案导入模板.xlsx', 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet');
  }

  /** 下载失败行文件（带鉴权，使用 blob 方式） */
  async function handleDownloadError() {
    if (!checkResult.value?.sessionId) {
      createMessage.warning('校验结果已失效，请重新上传文件');
      return;
    }
    const data = await defHttp.get(
      {
        url: getErrorUrl,
        params: { sessionId: checkResult.value.sessionId },
        responseType: 'blob',
      },
      { isTransformResponse: false }
    );
    downloadByData(data, '设备档案导入失败行.xlsx', 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet');
  }
</script>

<style lang="less" scoped>
  .import-step {
    .step-tip {
      margin-bottom: 12px;
      line-height: 24px;
      color: #666;
    }
  }
  .import-result {
    margin-top: 8px;
    .result-alert {
      margin-bottom: 12px;
    }
    .error-area {
      .error-toolbar {
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin-bottom: 8px;
        color: #666;
      }
    }
    .confirm-area {
      margin-top: 16px;
      display: flex;
      align-items: center;
      gap: 12px;
      .confirm-hint {
        color: #999;
      }
    }
  }
</style>
