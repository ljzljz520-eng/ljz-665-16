import { BasicColumn, FormSchema } from '/@/components/Table';

export const columns: BasicColumn[] = [
  {
    title: '设备编号',
    dataIndex: 'deviceCode',
    width: 160,
    resizable: true,
  },
  {
    title: '设备名称',
    dataIndex: 'deviceName',
    width: 200,
    resizable: true,
  },
  {
    title: '设备类型',
    dataIndex: 'deviceType',
    width: 130,
    resizable: true,
  },
  {
    title: '部门',
    dataIndex: 'department',
    width: 150,
    resizable: true,
  },
  {
    title: '位置',
    dataIndex: 'location',
    width: 200,
    resizable: true,
  },
  {
    title: '责任人',
    dataIndex: 'owner',
    width: 120,
    resizable: true,
  },
  {
    title: '创建人',
    dataIndex: 'createBy',
    width: 120,
    resizable: true,
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    width: 160,
    resizable: true,
  },
];

/** 列表查询条件（导出时复用同样的条件参数） */
export const searchFormSchema: FormSchema[] = [
  {
    field: 'deviceCode',
    label: '设备编号',
    component: 'Input',
    componentProps: { placeholder: '请输入设备编号', trim: true },
    colProps: { span: 6 },
  },
  {
    field: 'deviceName',
    label: '设备名称',
    component: 'Input',
    componentProps: { placeholder: '请输入设备名称', trim: true },
    colProps: { span: 6 },
  },
  {
    field: 'deviceType',
    label: '设备类型',
    component: 'Input',
    componentProps: { placeholder: '请输入设备类型', trim: true },
    colProps: { span: 6 },
  },
  {
    field: 'department',
    label: '部门',
    component: 'Input',
    componentProps: { placeholder: '请输入部门', trim: true },
    colProps: { span: 6 },
  },
  {
    field: 'location',
    label: '位置',
    component: 'Input',
    componentProps: { placeholder: '请输入位置', trim: true },
    colProps: { span: 6 },
  },
  {
    field: 'owner',
    label: '责任人',
    component: 'Input',
    componentProps: { placeholder: '请输入责任人', trim: true },
    colProps: { span: 6 },
  },
];

export const formSchema: FormSchema[] = [
  {
    field: 'id',
    label: 'id',
    component: 'Input',
    show: false,
  },
  {
    field: 'deviceCode',
    label: '设备编号',
    component: 'Input',
    required: true,
    componentProps: { placeholder: '请输入设备编号（唯一）', trim: true, maxlength: 50 },
  },
  {
    field: 'deviceName',
    label: '设备名称',
    component: 'Input',
    required: true,
    componentProps: { placeholder: '请输入设备名称', trim: true, maxlength: 100 },
  },
  {
    field: 'deviceType',
    label: '设备类型',
    component: 'Input',
    componentProps: { placeholder: '请输入设备类型', trim: true, maxlength: 50 },
  },
  {
    field: 'department',
    label: '部门',
    component: 'Input',
    componentProps: { placeholder: '请输入部门', trim: true, maxlength: 100 },
  },
  {
    field: 'location',
    label: '位置',
    component: 'Input',
    componentProps: { placeholder: '请输入安装位置', trim: true, maxlength: 200 },
  },
  {
    field: 'owner',
    label: '责任人',
    component: 'Input',
    componentProps: { placeholder: '请输入责任人', trim: true, maxlength: 50 },
  },
];
