<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    title="设备档案导入"
    :width="900"
    :footer="null"
    :destroyOnClose="true"
    @cancel="handleClose"
  >
    <div class="equipment-import">
      <!-- 步骤一：选择文件 -->
      <a-alert type="info" show-icon style="margin-bottom: 16px">
        <template #message>
          <span>请先</span>
          <a @click="downloadTemplate">下载导入模板</a>
          <span>，按模板填写设备编号、名称、类型、部门、位置、责任人后上传；系统先校验必填项和重复编号，确认后才会入库。</span>
        </template>
      </a-alert>
      <a-upload :maxCount="1" :before-upload="beforeUpload" :file-list="fileList" accept=".xls,.xlsx" @remove="handleRemove">
        <a-button preIcon="ant-design:upload-outlined" type="primary" :loading="checking">选择文件并校验</a-button>
      </a-upload>

      <!-- 步骤二：校验结果 -->
      <div v-if="checkResult" class="check-result" style="margin-top: 16px">
        <a-descriptions :column="3" bordered size="small">
          <a-descriptions-item label="总行数">{{ checkResult.totalCount }}</a-descriptions-item>
          <a-descriptions-item label="校验通过">
            <span style="color: #52c41a">{{ checkResult.successCount }}</span>
          </a-descriptions-item>
          <a-descriptions-item label="校验失败">
            <span :style="{ color: checkResult.errorCount > 0 ? '#f5222d' : undefined }">{{ checkResult.errorCount }}</span>
          </a-descriptions-item>
        </a-descriptions>

        <!-- 失败行 -->
        <template v-if="checkResult.errorCount > 0">
          <a-alert type="error" show-icon style="margin: 12px 0">
            <template #message>
              <span>有 {{ checkResult.errorCount }} 行校验未通过，不会入库，可</span>
              <a :href="errorFileHref" :download="checkResult.fileName">下载失败行文件</a>
              <span>查看具体原因。</span>
            </template>
          </a-alert>
          <a-table
            size="small"
            bordered
            rowKey="rowNum"
            :columns="errorColumns"
            :data-source="checkResult.errorList"
            :pagination="{ pageSize: 5, size: 'small' }"
            :scroll="{ y: 200 }"
          />
        </template>

        <!-- 通过行预览 -->
        <template v-if="checkResult.successCount > 0">
          <div style="margin: 12px 0 8px; font-weight: 500">
            校验通过数据预览（{{ checkResult.successCount }} 条{{ checkResult.successCount > 100 ? '，仅展示前100条' : '' }}）
          </div>
          <a-table
            size="small"
            bordered
            rowKey="equipNo"
            :columns="validColumns"
            :data-source="checkResult.validList"
            :pagination="{ pageSize: 5, size: 'small' }"
            :scroll="{ y: 200 }"
          />
        </template>

        <div style="margin-top: 16px; text-align: right">
          <a-button style="margin-right: 8px" @click="handleClose">取消</a-button>
          <a-button
            type="primary"
            :loading="confirming"
            :disabled="checkResult.successCount === 0"
            @click="handleConfirm"
          >
            确认入库（{{ checkResult.successCount }} 条）
          </a-button>
        </div>
      </div>
    </div>
  </BasicModal>
</template>
<script lang="ts" setup>
  import { ref, computed, h } from 'vue';
  import type { UploadProps } from 'ant-design-vue';
  import { Modal } from 'ant-design-vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useGlobSetting } from '/@/hooks/setting';
  import { defHttp } from '/@/utils/http/axios';
  import { downloadImportTemplate, getImportCheckUrl, getImportConfirmUrl } from '../equipment.api';

  const glob = useGlobSetting();
  const { createMessage } = useMessage();
  const emit = defineEmits(['register', 'success']);

  const fileList = ref<any[]>([]);
  const checking = ref(false);
  const confirming = ref(false);
  const checkResult = ref<any>(null);

  const [registerModal, { closeModal }] = useModalInner(() => {
    fileList.value = [];
    checkResult.value = null;
    checking.value = false;
    confirming.value = false;
  });

  const errorColumns = [
    { title: '行号', dataIndex: 'rowNum', width: 70, align: 'center' },
    { title: '设备编号', dataIndex: 'equipNo', width: 130 },
    { title: '名称', dataIndex: 'equipName', width: 150 },
    { title: '类型', dataIndex: 'equipType', width: 100 },
    { title: '部门', dataIndex: 'department', width: 120 },
    { title: '位置', dataIndex: 'location', width: 140 },
    { title: '责任人', dataIndex: 'owner', width: 90 },
    { title: '失败原因', dataIndex: 'errorReason', width: 200 },
  ];
  const validColumns = [
    { title: '设备编号', dataIndex: 'equipNo', width: 130 },
    { title: '名称', dataIndex: 'equipName', width: 160 },
    { title: '类型', dataIndex: 'equipType', width: 110 },
    { title: '部门', dataIndex: 'department', width: 130 },
    { title: '位置', dataIndex: 'location', width: 150 },
    { title: '责任人', dataIndex: 'owner', width: 100 },
  ];

  const errorFileHref = computed(() => {
    if (!checkResult.value?.fileUrl) {
      return '';
    }
    return glob.uploadUrl + checkResult.value.fileUrl;
  });

  /**
   * 下载导入模板（由后端生成，保证列与解析规则一致）
   */
  function downloadTemplate() {
    downloadImportTemplate();
  }

  const beforeUpload: UploadProps['beforeUpload'] = async (file) => {
    fileList.value = [
      {
        uid: String(Date.now()),
        name: file.name,
        status: 'done',
        originFileObj: file,
      },
    ];
    await doCheck(file);
    // 阻止 antd 自动上传，由自定义流程完成
    return false;
  };

  function handleRemove() {
    fileList.value = [];
    checkResult.value = null;
  }

  /**
   * 第一步：上传文件校验（不写库）
   */
  async function doCheck(file: File) {
    checking.value = true;
    checkResult.value = null;
    await defHttp.uploadFile(
      { url: getImportCheckUrl },
      { file },
      {
        isReturnResponse: true,
      },
    ).then((res: any) => {
      // res 为后端完整 Result：{ success, code, message, result }
      if (!res || res.success === false) {
        fileList.value = [];
        createMessage.error(res?.message || '文件校验失败，请检查文件后重试');
        return;
      }
      const data = res.result;
      checkResult.value = data;
      if (data.errorCount > 0) {
        createMessage.warning(`校验完成：通过 ${data.successCount} 条，失败 ${data.errorCount} 条`);
      } else {
        createMessage.success(`校验完成，全部 ${data.successCount} 条数据可入库`);
      }
    }).catch((e: any) => {
      fileList.value = [];
      createMessage.error(e?.message || '文件校验失败，请检查文件后重试');
    }).finally(() => {
      checking.value = false;
    });
  }

  /**
   * 第二步：确认入库（部分行失败时后端返回 code=201，结果中含失败文件下载地址）
   */
  async function handleConfirm() {
    if (!checkResult.value?.checkToken) {
      createMessage.error('校验凭证缺失，请重新上传文件');
      return;
    }
    confirming.value = true;
    try {
      const res: any = await defHttp.post(
        {
          url: getImportConfirmUrl,
          params: { checkToken: checkResult.value.checkToken },
        },
        { joinParamsToUrl: true, isTransformResponse: false },
      );
      if (res.code === 201) {
        // 部分失败：弹窗提示并提供失败文件下载
        const href = res.result?.fileUrl ? glob.uploadUrl + res.result.fileUrl : '';
        Modal.warning({
          title: res.message || '导入完成，但有失败行',
          centered: false,
          width: 520,
          content: () =>
            h('div', [
              h('span', res.result?.msg || ''),
              h('br'),
              href
                ? h('span', [
                    h('span', '失败行可'),
                    h('a', { href, download: res.result?.fileName }, ' 点击下载 '),
                    h('span', '查看具体原因。'),
                  ])
                : null,
            ]),
          onOk: () => {
            closeModal();
            emit('success');
          },
        });
      } else if (res.success === false) {
        createMessage.error(res.message || '入库失败');
      } else {
        createMessage.success(typeof res.result === 'string' ? res.result : res.message || '入库成功');
        closeModal();
        emit('success');
      }
    } catch (e: any) {
      createMessage.error(e?.message || '入库失败，请稍后重试');
    } finally {
      confirming.value = false;
    }
  }

  function handleClose() {
    closeModal();
  }
</script>
<style lang="less" scoped>
  .equipment-import {
    min-height: 200px;
  }
</style>
