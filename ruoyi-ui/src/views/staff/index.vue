<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb-[10px]">
        <el-card shadow="hover">
          <el-form ref="queryFormRef" :model="queryParams" :inline="true">
            <el-form-item label="分类" prop="categoryId">
              <el-select v-model="queryParams.categoryId" placeholder="全部分类" clearable style="width: 180px">
                <el-option v-for="item in categoryOptions" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="状态" prop="status">
              <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 150px">
                <el-option label="草稿" value="DRAFT" />
                <el-option label="已上架" value="ONLINE" />
                <el-option label="已下架" value="OFFLINE" />
              </el-select>
            </el-form-item>
            <el-form-item label="关键词" prop="keyword">
              <el-input v-model="queryParams.keyword" placeholder="姓名、城市、区域" clearable @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
              <el-button icon="Refresh" @click="resetQuery">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </div>
    </transition>

    <el-card shadow="hover">
      <template #header>
        <el-row :gutter="10">
          <el-col :span="1.5">
            <el-button type="primary" plain icon="Plus" @click="handleAdd">新增</el-button>
          </el-col>
          <right-toolbar v-model:show-search="showSearch" @query-table="getList" />
        </el-row>
      </template>

      <el-table v-loading="loading" border :data="staffList">
        <el-table-column label="姓名" prop="name" min-width="120" />
        <el-table-column label="分类" prop="categoryName" min-width="120" />
        <el-table-column label="城市" prop="city" width="100" />
        <el-table-column label="区域" prop="district" width="100" />
        <el-table-column label="年龄" prop="age" width="80" align="center" />
        <el-table-column label="经验" width="90" align="center">
          <template #default="{ row }">{{ row.experienceYears || 0 }} 年</template>
        </el-table-column>
        <el-table-column label="薪资" min-width="140">
          <template #default="{ row }">{{ row.salaryMin || '-' }} - {{ row.salaryMax || '-' }} / {{ row.salaryUnit || '月' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="推荐" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.recommended ? 'success' : 'info'">{{ row.recommended ? '是' : '否' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right" align="center">
          <template #default="{ row }">
            <el-tooltip content="编辑" placement="top">
              <el-button link type="primary" icon="Edit" @click="handleEdit(row)" />
            </el-tooltip>
            <el-tooltip :content="row.status === 'ONLINE' ? '下架' : '上架'" placement="top">
              <el-button link type="primary" :icon="row.status === 'ONLINE' ? 'Close' : 'Check'" @click="toggleOnline(row)" />
            </el-tooltip>
            <el-tooltip :content="row.recommended ? '取消推荐' : '设为推荐'" placement="top">
              <el-button link type="primary" icon="Star" @click="toggleRecommend(row)" />
            </el-tooltip>
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

    <el-dialog v-model="dialog.visible" :title="dialog.title" width="900px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="基础资料" name="base">
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="服务分类" prop="categoryId">
                  <el-select v-model="form.categoryId" placeholder="请选择服务分类" style="width: 100%">
                    <el-option v-for="item in categoryOptions" :key="item.id" :label="item.name" :value="item.id" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="姓名" prop="name">
                  <el-input v-model="form.name" maxlength="30" placeholder="请输入姓名" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="头像">
                  <div class="upload-inline">
                    <el-upload
                      :action="uploadUrl"
                      :headers="uploadHeaders"
                      :show-file-list="false"
                      :before-upload="beforeImageUpload"
                      :on-success="handleAvatarUploadSuccess"
                      :on-error="handleUploadError"
                    >
                      <el-button icon="Upload">上传头像</el-button>
                    </el-upload>
                    <el-image v-if="form.avatarUrl" class="avatar-preview" :src="form.avatarUrl" fit="cover" :preview-src-list="[form.avatarUrl]" />
                  </div>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="头像地址">
                  <el-input v-model="form.avatarUrl" placeholder="上传后自动填入，也可手动输入 URL" />
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item label="性别">
                  <el-select v-model="form.gender" style="width: 100%">
                    <el-option label="女" value="FEMALE" />
                    <el-option label="男" value="MALE" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item label="年龄">
                  <el-input-number v-model="form.age" :min="18" :max="70" controls-position="right" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="城市">
                  <el-input v-model="form.city" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="区域">
                  <el-input v-model="form.district" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="学历">
                  <el-input v-model="form.education" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="经验年限">
                  <el-input-number v-model="form.experienceYears" :min="0" controls-position="right" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="最低薪资">
                  <el-input-number v-model="form.salaryMin" :min="0" controls-position="right" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="最高薪资">
                  <el-input-number v-model="form.salaryMax" :min="0" controls-position="right" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="薪资单位">
                  <el-input v-model="form.salaryUnit" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="状态">
                  <el-select v-model="form.status" style="width: 100%">
                    <el-option label="草稿" value="DRAFT" />
                    <el-option label="已上架" value="ONLINE" />
                    <el-option label="已下架" value="OFFLINE" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="推荐">
                  <el-switch v-model="form.recommended" />
                </el-form-item>
              </el-col>
              <el-col :span="24">
                <el-form-item label="标签">
                  <el-input v-model="tagsText" placeholder="多个标签用逗号分隔" />
                </el-form-item>
              </el-col>
              <el-col :span="24">
                <el-form-item label="服务说明">
                  <el-input v-model="form.serviceDesc" type="textarea" :rows="3" maxlength="300" show-word-limit />
                </el-form-item>
              </el-col>
            </el-row>
          </el-tab-pane>

          <el-tab-pane label="证书" name="certificates">
            <div v-for="(item, index) in form.certificates" :key="index" class="repeat-row certificate-row">
              <el-input v-model="item.certificateName" placeholder="证书名称" />
              <el-input v-model="item.fileUrl" placeholder="文件地址" />
              <el-upload
                :action="uploadUrl"
                :headers="uploadHeaders"
                :show-file-list="false"
                :before-upload="beforeFileUpload"
                :on-success="(res) => handleCertificateUploadSuccess(index, res)"
                :on-error="handleUploadError"
              >
                <el-button icon="Upload">上传</el-button>
              </el-upload>
              <el-input-number v-model="item.sortNo" :min="0" controls-position="right" />
              <el-link v-if="item.fileUrl" :href="item.fileUrl" target="_blank" type="primary">预览</el-link>
              <el-button icon="Delete" @click="form.certificates.splice(index, 1)" />
            </div>
            <el-button icon="Plus" @click="form.certificates.push({ certificateName: '', fileUrl: '', sortNo: 0 })">新增证书</el-button>
          </el-tab-pane>

          <el-tab-pane label="照片" name="photos">
            <div v-for="(item, index) in form.photos" :key="index" class="repeat-row photo-row">
              <el-input v-model="item.photoUrl" placeholder="照片地址" />
              <el-upload
                :action="uploadUrl"
                :headers="uploadHeaders"
                :show-file-list="false"
                :before-upload="beforeImageUpload"
                :on-success="(res) => handlePhotoUploadSuccess(index, res)"
                :on-error="handleUploadError"
              >
                <el-button icon="Upload">上传</el-button>
              </el-upload>
              <el-input-number v-model="item.sortNo" :min="0" controls-position="right" />
              <el-image v-if="item.photoUrl" class="photo-preview" :src="item.photoUrl" fit="cover" :preview-src-list="[item.photoUrl]" />
              <el-button icon="Delete" @click="form.photos.splice(index, 1)" />
            </div>
            <el-button icon="Plus" @click="form.photos.push({ photoUrl: '', sortNo: 0 })">新增照片</el-button>
          </el-tab-pane>

          <el-tab-pane label="工作经历" name="experiences">
            <div v-for="(item, index) in form.experiences" :key="index" class="repeat-row experience-row">
              <el-date-picker v-model="item.startDate" value-format="YYYY-MM-DD" type="date" placeholder="开始日期" />
              <el-date-picker v-model="item.endDate" value-format="YYYY-MM-DD" type="date" placeholder="结束日期" />
              <el-input v-model="item.description" placeholder="经历描述" />
              <el-button icon="Delete" @click="form.experiences.splice(index, 1)" />
            </div>
            <el-button icon="Plus" @click="form.experiences.push({ startDate: '', endDate: '', description: '' })">新增经历</el-button>
          </el-tab-pane>
        </el-tabs>
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

<script setup lang="ts" name="ServiceStaff">
import { computed, getCurrentInstance, onMounted, reactive, ref } from 'vue';
import type { ComponentInternalInstance } from 'vue';
import type { FormInstance, FormRules } from 'element-plus';
import { ElMessage } from 'element-plus';
import { listCategory } from '@/api/staff/category';
import { addStaff, getStaff, listStaff, updateStaff, updateStaffStatus } from '@/api/staff/staff';
import { globalHeaders } from '@/utils/request';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const loading = ref(false);
const showSearch = ref(true);
const staffList = ref<any[]>([]);
const categoryOptions = ref<any[]>([]);
const total = ref(0);
const activeTab = ref('base');
const formRef = ref<FormInstance>();
const queryFormRef = ref<FormInstance>();
const dialog = reactive({ visible: false, title: '' });
const queryParams = reactive<any>({
  pageNum: 1,
  pageSize: 10,
  categoryId: undefined,
  status: undefined,
  keyword: ''
});
const tagsText = ref('');
const form = reactive<any>({});
const rules: FormRules = {
  categoryId: [{ required: true, message: '服务分类不能为空', trigger: 'change' }],
  name: [{ required: true, message: '姓名不能为空', trigger: 'blur' }]
};

const uploadUrl = `${import.meta.env.VITE_APP_BASE_API}/api/file/upload`;
const uploadHeaders = computed(() => globalHeaders());

const defaultForm = () => ({
  id: undefined,
  categoryId: undefined,
  name: '',
  avatarUrl: '',
  gender: 'FEMALE',
  age: 30,
  city: '',
  district: '',
  education: '',
  experienceYears: 0,
  salaryMin: 0,
  salaryMax: 0,
  salaryUnit: '月',
  serviceDesc: '',
  status: 'DRAFT',
  recommended: false,
  sortNo: 0,
  certificates: [],
  photos: [],
  experiences: []
});

const resetFormData = () => {
  Object.assign(form, defaultForm());
  tagsText.value = '';
  activeTab.value = 'base';
};

const statusText = (status: string) => {
  const map: Record<string, string> = {
    DRAFT: '草稿',
    ONLINE: '已上架',
    OFFLINE: '已下架'
  };
  return map[status] || status || '-';
};

const statusTagType = (status: string) => {
  if (status === 'ONLINE') return 'success';
  if (status === 'OFFLINE') return 'info';
  return 'warning';
};

const beforeImageUpload = (file: File) => {
  if (!file.type.startsWith('image/')) {
    ElMessage.error('请上传图片文件');
    return false;
  }
  return beforeFileUpload(file);
};

const beforeFileUpload = (file: File) => {
  if (file.name.includes(',')) {
    ElMessage.error('文件名不能包含英文逗号');
    return false;
  }
  if (file.size / 1024 / 1024 > 10) {
    ElMessage.error('文件大小不能超过 10MB');
    return false;
  }
  return true;
};

const readUploadUrl = (res: any) => {
  if (res?.code !== 200 || !res?.data?.url) {
    ElMessage.error(res?.msg || '上传失败');
    return '';
  }
  return res.data.url;
};

const handleAvatarUploadSuccess = (res: any) => {
  const url = readUploadUrl(res);
  if (url) {
    form.avatarUrl = url;
    ElMessage.success('头像上传成功');
  }
};

const handleCertificateUploadSuccess = (index: number, res: any) => {
  const url = readUploadUrl(res);
  if (url) {
    form.certificates[index].fileUrl = url;
    if (!form.certificates[index].certificateName) {
      form.certificates[index].certificateName = res.data.fileName || '证书文件';
    }
    ElMessage.success('证书上传成功');
  }
};

const handlePhotoUploadSuccess = (index: number, res: any) => {
  const url = readUploadUrl(res);
  if (url) {
    form.photos[index].photoUrl = url;
    ElMessage.success('照片上传成功');
  }
};

const handleUploadError = () => {
  ElMessage.error('上传失败，请稍后重试');
};

const getCategories = async () => {
  const res: any = await listCategory();
  categoryOptions.value = res.rows || [];
};

const getList = async () => {
  loading.value = true;
  try {
    const res: any = await listStaff(queryParams);
    staffList.value = res.rows || [];
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

const handleAdd = () => {
  resetFormData();
  dialog.title = '新增服务人员';
  dialog.visible = true;
};

const handleEdit = async (row: any) => {
  resetFormData();
  const res: any = await getStaff(row.id);
  const data = res.data || {};
  Object.assign(form, defaultForm(), data, {
    recommended: data.recommended === true || data.recommended === 1,
    certificates: data.certificates || [],
    photos: data.photos || [],
    experiences: data.experiences || []
  });
  tagsText.value = (data.tags || []).map((item: any) => item.tagName).join(',');
  dialog.title = '编辑服务人员';
  dialog.visible = true;
};

const buildPayload = () => ({
  ...form,
  tags: tagsText.value
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean),
  certificates: form.certificates.filter((item: any) => item.certificateName || item.fileUrl),
  photos: form.photos.filter((item: any) => item.photoUrl),
  experiences: form.experiences.filter((item: any) => item.description)
});

const submitForm = async () => {
  await formRef.value?.validate();
  const payload = buildPayload();
  if (form.id) {
    await updateStaff(form.id, payload);
  } else {
    await addStaff(payload);
  }
  ElMessage.success('保存成功');
  dialog.visible = false;
  getList();
};

const toggleOnline = async (row: any) => {
  const nextStatus = row.status === 'ONLINE' ? 'OFFLINE' : 'ONLINE';
  await updateStaffStatus(row.id, { status: nextStatus, recommended: row.recommended === true || row.recommended === 1 });
  ElMessage.success(nextStatus === 'ONLINE' ? '已上架' : '已下架');
  getList();
};

const toggleRecommend = async (row: any) => {
  await updateStaffStatus(row.id, { status: row.status || 'DRAFT', recommended: !(row.recommended === true || row.recommended === 1) });
  ElMessage.success('推荐状态已更新');
  getList();
};

onMounted(() => {
  getCategories();
  getList();
});
</script>

<style scoped>
.upload-inline {
  display: flex;
  align-items: center;
  gap: 12px;
}

.avatar-preview {
  width: 52px;
  height: 52px;
  border-radius: 4px;
}

.photo-preview {
  width: 52px;
  height: 52px;
  border-radius: 4px;
}

.repeat-row {
  display: grid;
  gap: 8px;
  align-items: center;
  margin-bottom: 10px;
}

.certificate-row {
  grid-template-columns: minmax(130px, 1fr) minmax(220px, 2fr) auto 120px auto auto;
}

.photo-row {
  grid-template-columns: minmax(260px, 2fr) auto 120px auto auto;
}

.experience-row {
  grid-template-columns: 160px 160px minmax(240px, 1fr) auto;
}
</style>
