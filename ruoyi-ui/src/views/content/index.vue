<template>
  <div class="p-2">
    <el-card shadow="hover" class="mb-[10px]">
      <el-form ref="queryFormRef" :model="queryParams" :inline="true">
        <el-form-item label="内容类型" prop="contentType">
          <el-select v-model="queryParams.contentType" placeholder="全部类型" clearable style="width: 180px">
            <el-option v-for="item in contentTypes" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
          <el-button type="primary" plain icon="Plus" @click="openAdd">新增</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="hover">
      <el-table v-loading="loading" border :data="contentList">
        <el-table-column label="类型" prop="contentType" width="130">
          <template #default="{ row }">{{ typeText(row.contentType) }}</template>
        </el-table-column>
        <el-table-column label="标题" prop="title" min-width="180" />
        <el-table-column label="图片" prop="imageUrl" width="120" align="center">
          <template #default="{ row }">
            <el-image
              v-if="row.imageUrl"
              class="table-image"
              :src="row.imageUrl"
              :preview-src-list="[row.imageUrl]"
              preview-teleported
              fit="cover"
            />
            <span v-else class="text-gray-400">未上传</span>
          </template>
        </el-table-column>
        <el-table-column label="内容" prop="content" min-width="260" show-overflow-tooltip />
        <el-table-column label="排序" prop="sortNo" width="90" align="center" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch :model-value="!!row.enabled" @change="(value) => handleStatus(row, value)" />
          </template>
        </el-table-column>
        <el-table-column label="更新时间" prop="updatedAt" width="170" />
        <el-table-column label="操作" width="110" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" icon="Edit" @click="openEdit(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination
        v-if="total > 0"
        v-model:total="total"
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />
    </el-card>

    <el-dialog v-model="dialog.visible" :title="dialog.title" width="720px" append-to-body>
      <el-form ref="contentFormRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="内容类型" prop="contentType">
          <el-select v-model="form.contentType" placeholder="请选择内容类型" style="width: 100%">
            <el-option v-for="item in contentTypes" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" maxlength="128" show-word-limit />
        </el-form-item>
        <el-form-item label="图片">
          <div class="image-upload-row">
            <el-upload
              class="content-image-uploader"
              :action="uploadUrl"
              :headers="uploadHeaders"
              :show-file-list="false"
              :on-success="handleImageSuccess"
              :before-upload="beforeImageUpload"
              accept=".png,.jpg,.jpeg,.webp"
            >
              <img v-if="form.imageUrl" :src="form.imageUrl" class="uploaded-image" />
              <el-icon v-else class="image-uploader-icon"><Plus /></el-icon>
            </el-upload>
            <el-button v-if="form.imageUrl" plain type="danger" @click="form.imageUrl = ''">移除图片</el-button>
          </div>
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="form.content" type="textarea" :rows="8" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortNo" :min="0" controls-position="right" />
        </el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="form.enabled" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts" name="ContentManage">
import { onMounted, reactive, ref } from 'vue';
import type { FormInstance, FormRules } from 'element-plus';
import { ElMessage } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';
import { addContent, listContent, updateContent, updateContentStatus } from '@/api/content/content';
import { globalHeaders } from '@/utils/request';

const contentTypes = [
  { label: '首页轮播', value: 'BANNER' },
  { label: '签约提示', value: 'SIGN_TIP' },
  { label: '常见问题', value: 'FAQ' },
  { label: '用户协议', value: 'AGREEMENT' },
  { label: '隐私政策', value: 'PRIVACY' },
  { label: '关于我们', value: 'ABOUT' }
];

const loading = ref(false);
const total = ref(0);
const contentList = ref<any[]>([]);
const queryFormRef = ref<FormInstance>();
const contentFormRef = ref<FormInstance>();
const currentId = ref<number | string>();
const dialog = reactive({ visible: false, title: '新增内容' });
const queryParams = reactive<any>({
  pageNum: 1,
  pageSize: 10,
  contentType: undefined
});
const form = reactive<any>({
  contentType: 'BANNER',
  title: '',
  imageUrl: '',
  content: '',
  sortNo: 0,
  enabled: true
});
const uploadUrl = `${import.meta.env.VITE_APP_BASE_API}/resource/oss/upload`;
const uploadHeaders = globalHeaders();
const rules: FormRules = {
  contentType: [{ required: true, message: '请选择内容类型', trigger: 'change' }],
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }]
};

const typeText = (type: string) => contentTypes.find((item) => item.value === type)?.label || type || '-';

const getList = async () => {
  loading.value = true;
  try {
    const res: any = await listContent(queryParams);
    contentList.value = res.rows || [];
    total.value = res.total || 0;
  } finally {
    loading.value = false;
  }
};

const handleQuery = () => {
  queryParams.pageNum = 1;
  getList();
};

const resetQuery = () => {
  queryFormRef.value?.resetFields();
  handleQuery();
};

const resetForm = () => {
  currentId.value = undefined;
  Object.assign(form, {
    contentType: queryParams.contentType || 'BANNER',
    title: '',
    imageUrl: '',
    content: '',
    sortNo: 0,
    enabled: true
  });
};

const openAdd = () => {
  resetForm();
  dialog.title = '新增内容';
  dialog.visible = true;
};

const openEdit = (row: any) => {
  currentId.value = row.id;
  Object.assign(form, {
    contentType: row.contentType,
    title: row.title || '',
    imageUrl: row.imageUrl || '',
    content: row.content || '',
    sortNo: row.sortNo || 0,
    enabled: !!row.enabled
  });
  dialog.title = '编辑内容';
  dialog.visible = true;
};

const submitForm = async () => {
  await contentFormRef.value?.validate();
  if (currentId.value) {
    await updateContent(currentId.value, form);
    ElMessage.success('内容已更新');
  } else {
    await addContent(form);
    ElMessage.success('内容已新增');
  }
  dialog.visible = false;
  getList();
};

const beforeImageUpload = (rawFile: File) => {
  const allowTypes = ['image/png', 'image/jpeg', 'image/webp'];
  if (!allowTypes.includes(rawFile.type)) {
    ElMessage.error('请上传 PNG、JPG、JPEG 或 WebP 图片');
    return false;
  }
  if (rawFile.size / 1024 / 1024 > 5) {
    ElMessage.error('图片大小不能超过 5MB');
    return false;
  }
  return true;
};

const handleImageSuccess = (res: any) => {
  if (res.code !== 200) {
    ElMessage.error(res.msg || '上传失败');
    return;
  }
  form.imageUrl = res.data.url;
  ElMessage.success('图片已上传');
};

const handleStatus = async (row: any, value: string | number | boolean) => {
  await updateContentStatus(row.id, !!value);
  ElMessage.success('状态已更新');
  getList();
};

onMounted(getList);
</script>

<style scoped>
.table-image {
  width: 64px;
  height: 44px;
  border-radius: 6px;
}

.image-upload-row {
  display: flex;
  align-items: flex-end;
  gap: 12px;
}

.content-image-uploader :deep(.el-upload) {
  width: 148px;
  height: 104px;
  overflow: hidden;
  border: 1px dashed var(--el-border-color);
  border-radius: 8px;
  cursor: pointer;
}

.content-image-uploader :deep(.el-upload:hover) {
  border-color: var(--el-color-primary);
}

.uploaded-image {
  width: 148px;
  height: 104px;
  object-fit: cover;
}

.image-uploader-icon {
  width: 148px;
  height: 104px;
  color: #8c939d;
  font-size: 28px;
}
</style>
