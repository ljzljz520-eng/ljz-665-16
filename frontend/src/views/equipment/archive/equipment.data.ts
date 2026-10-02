import { BasicColumn, FormSchema } from '/@/components/Table';

/**
 * 列表列
 */
export const columns: BasicColumn[] = [
  {
    title: '设备编号',
    dataIndex: 'equipNo',
    width: 160,
    resizable: true,
  },
  {
    title: '名称',
    dataIndex: 'equipName',
    width: 200,
    resizable: true,
  },
  {
    title: '类型',
    dataIndex: 'equipType',
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
    title: '备注',
    dataIndex: 'remark',
    width: 180,
    resizable: true,
  },
];

/**
 * 查询条件
 */
export const searchFormSchema: FormSchema[] = [
  {
    field: 'equipNo',
    label: '设备编号',
    component: 'Input',
    componentProps: { placeholder: '请输入设备编号' },
    colProps: { span: 8 },
  },
  {
    field: 'equipName',
    label: '名称',
    component: 'Input',
    componentProps: { placeholder: '请输入设备名称' },
    colProps: { span: 8 },
  },
  {
    field: 'equipType',
    label: '类型',
    component: 'Input',
    componentProps: { placeholder: '请输入设备类型' },
    colProps: { span: 8 },
  },
  {
    field: 'department',
    label: '部门',
    component: 'Input',
    componentProps: { placeholder: '请输入部门' },
    colProps: { span: 8 },
  },
  {
    field: 'location',
    label: '位置',
    component: 'Input',
    componentProps: { placeholder: '请输入位置' },
    colProps: { span: 8 },
  },
  {
    field: 'owner',
    label: '责任人',
    component: 'Input',
    componentProps: { placeholder: '请输入责任人' },
    colProps: { span: 8 },
  },
];

/**
 * 新增/编辑表单
 */
export const formSchema: FormSchema[] = [
  {
    field: 'id',
    label: '',
    component: 'Input',
    show: false,
  },
  {
    field: 'equipNo',
    label: '设备编号',
    component: 'Input',
    required: true,
    componentProps: { placeholder: '请输入设备编号', maxlength: 64 },
  },
  {
    field: 'equipName',
    label: '名称',
    component: 'Input',
    required: true,
    componentProps: { placeholder: '请输入设备名称', maxlength: 128 },
  },
  {
    field: 'equipType',
    label: '类型',
    component: 'Input',
    required: true,
    componentProps: { placeholder: '请输入设备类型', maxlength: 64 },
  },
  {
    field: 'department',
    label: '部门',
    component: 'Input',
    required: true,
    componentProps: { placeholder: '请输入所属部门', maxlength: 128 },
  },
  {
    field: 'location',
    label: '位置',
    component: 'Input',
    required: true,
    componentProps: { placeholder: '请输入存放位置', maxlength: 255 },
  },
  {
    field: 'owner',
    label: '责任人',
    component: 'Input',
    required: true,
    componentProps: { placeholder: '请输入责任人', maxlength: 64 },
  },
  {
    field: 'remark',
    label: '备注',
    component: 'InputTextArea',
    componentProps: { placeholder: '请输入备注', rows: 3, maxlength: 500 },
  },
];
