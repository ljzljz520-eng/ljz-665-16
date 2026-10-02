<template>
  <BasicModal v-bind="$attrs" @register="registerModal" :title="title" @ok="handleSubmit" width="40%">
    <BasicForm @register="registerForm" :disabled="isDisabled" />
  </BasicModal>
</template>
<script lang="ts" setup>
  import { ref, computed, unref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { formSchema } from './deviceArchive.data';
  import { saveOrUpdateDevice, getDeviceById } from './deviceArchive.api';

  const emit = defineEmits(['register', 'success']);
  const isUpdate = ref(true);

  const props = defineProps({
    isDisabled: {
      type: Boolean,
      default: false,
    },
  });

  const [registerForm, { resetFields, setFieldsValue, validate }] = useForm({
    schemas: formSchema,
    showActionButtonGroup: false,
  });

  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    await resetFields();
    setModalProps({ confirmLoading: false, showOkBtn: !props.isDisabled });
    isUpdate.value = !!data?.isUpdate;
    if (unref(isUpdate)) {
      const record = await getDeviceById({ id: data.record.id });
      await setFieldsValue({ ...record });
    }
  });

  const title = computed(() => {
    if (props.isDisabled) {
      return '详情';
    }
    return unref(isUpdate) ? '编辑设备档案' : '新增设备档案';
  });

  async function handleSubmit() {
    try {
      const values = await validate();
      setModalProps({ confirmLoading: true });
      await saveOrUpdateDevice(values, isUpdate.value);
      closeModal();
      emit('success', values);
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>
