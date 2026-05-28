<template>
  <div class="p-2">
    <el-card shadow="hover">
      <template #header>
        <el-row :gutter="10">
          <el-col :span="1.5">
            <el-button type="primary" plain icon="Plus" @click="handleAdd">新增</el-button>
          </el-col>
          <right-toolbar :show-search="false" @query-table="getList" />
        </el-row>
      </template>

      <el-table v-loading="loading" border :data="categoryList">
        <el-table-column label="分类名称" prop="name" min-width="160" />
        <el-table-column label="图标地址" prop="iconUrl" min-width="220" show-overflow-tooltip />
        <el-table-column label="排序" prop="sortNo" width="90" align="center" />
        <el-table-column label="启用" width="100" align="center">
          <template #default="{ row }">
            <el-switch v-model="row.enabled" :active-value="true" :inactive-value="false" @change="handleStatusChange(row)" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right" align="center">
          <template #default="{ row }">
            <el-tooltip content="编辑" placement="top">
              <el-button link type="primary" icon="Edit" @click="handleEdit(row)" />
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialog.visible" :title="dialog.title" width="520px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入分类名称" maxlength="30" />
        </el-form-item>
        <el-form-item label="图标地址" prop="iconUrl">
          <el-input v-model="form.iconUrl" placeholder="请输入图标地址" />
        </el-form-item>
        <el-form-item label="排序" prop="sortNo">
          <el-input-number v-model="form.sortNo" :min="0" controls-position="right" />
        </el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="form.enabled" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确定</el-button>
          <el-button @click="dialog.visible = false">取消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts" name="ServiceCategory">
import { onMounted, reactive, ref } from 'vue';
import type { FormInstance, FormRules } from 'element-plus';
import { ElMessage } from 'element-plus';
import { addCategory, listCategory, updateCategory, updateCategoryStatus } from '@/api/staff/category';

const loading = ref(false);
const categoryList = ref<any[]>([]);
const formRef = ref<FormInstance>();
const dialog = reactive({ visible: false, title: '' });
const form = reactive<any>({
  id: undefined,
  name: '',
  iconUrl: '',
  sortNo: 0,
  enabled: true
});
const rules: FormRules = {
  name: [{ required: true, message: '分类名称不能为空', trigger: 'blur' }]
};

const reset = () => {
  form.id = undefined;
  form.name = '';
  form.iconUrl = '';
  form.sortNo = 0;
  form.enabled = true;
};

const getList = async () => {
  loading.value = true;
  try {
    const res: any = await listCategory();
    categoryList.value = res.rows || [];
  } finally {
    loading.value = false;
  }
};

const handleAdd = () => {
  reset();
  dialog.title = '新增服务分类';
  dialog.visible = true;
};

const handleEdit = (row: any) => {
  Object.assign(form, row, { enabled: row.enabled === true || row.enabled === 1 });
  dialog.title = '编辑服务分类';
  dialog.visible = true;
};

const handleStatusChange = async (row: any) => {
  await updateCategoryStatus(row.id, row.enabled === true || row.enabled === 1);
  ElMessage.success('状态已更新');
  getList();
};

const submitForm = async () => {
  await formRef.value?.validate();
  if (form.id) {
    await updateCategory(form.id, form);
  } else {
    await addCategory(form);
  }
  ElMessage.success('保存成功');
  dialog.visible = false;
  getList();
};

onMounted(getList);
</script>
