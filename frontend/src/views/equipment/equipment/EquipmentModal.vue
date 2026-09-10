<template>
  <BasicModal v-bind="$attrs" @register="registerModal" :title="title" @ok="handleSubmit" width="50%">
    <BasicForm @register="registerForm" :disabled="isDisabled" />
  </BasicModal>
</template>
<script lang="ts" setup>
  import { ref, computed, unref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { formSchema } from './equipment.data';
  import { saveOrUpdateEquipment, getEquipmentById } from './equipment.api';

  const emit = defineEmits(['register', 'success']);
  const isUpdate = ref(true);

  const props = defineProps({
    // 是否禁用（详情查看）
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
      const record = await getEquipmentById({ id: data.record.id });
      await setFieldsValue({ ...record });
    } else {
      // 新增时确保id为空
      await setFieldsValue({ id: '' });
    }
  });

  const title = computed(() => {
    if (props.isDisabled) {
      return '设备详情';
    }
    return !unref(isUpdate) ? '新增设备' : '编辑设备';
  });

  async function handleSubmit() {
    try {
      const values = await validate();
      setModalProps({ confirmLoading: true });
      await saveOrUpdateEquipment(values, isUpdate.value);
      closeModal();
      emit('success', values);
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>
