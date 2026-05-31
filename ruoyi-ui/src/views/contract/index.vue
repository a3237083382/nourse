<template>
  <div class="p-2">
    <el-card shadow="never">
      <el-form :model="queryParams" inline>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 150px">
            <el-option label="已签署" value="SIGNED" />
            <el-option label="已终止" value="TERMINATED" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="queryParams.keyword" placeholder="编号/标题/用户/阿姨" clearable style="width: 220px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="getList">查询</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
          <el-button type="success" icon="Plus" @click="openForm()">新增合同</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" border :data="rows">
        <el-table-column label="合同编号" prop="contractNo" min-width="150" />
        <el-table-column label="合同标题" prop="title" min-width="180" show-overflow-tooltip />
        <el-table-column label="用户" prop="userNickname" min-width="120" />
        <el-table-column label="服务人员" prop="staffName" min-width="120" />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="row.status === 'SIGNED' ? 'success' : 'info'">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createdAt" min-width="170" />
        <el-table-column label="操作" width="170" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" icon="View" @click="openDetail(row)">详情</el-button>
            <el-button link type="primary" icon="Edit" @click="openForm(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>

    <el-dialog v-model="formDialog.visible" :title="form.id ? '编辑合同' : '新增合同'" width="680px" append-to-body>
      <el-form :model="form" label-width="110px">
        <el-form-item label="用户ID"><el-input-number v-model="form.userId" :min="1" /></el-form-item>
        <el-form-item label="服务人员ID"><el-input-number v-model="form.staffId" :min="1" /></el-form-item>
        <el-form-item label="需求ID"><el-input-number v-model="form.demandId" :min="1" /></el-form-item>
        <el-form-item label="服务订单ID"><el-input-number v-model="form.serviceOrderId" :min="1" /></el-form-item>
        <el-form-item label="合同编号"><el-input v-model="form.contractNo" /></el-form-item>
        <el-form-item label="合同标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="合同文件">
          <div class="file-row">
            <el-input v-model="form.fileUrl" placeholder="PDF/图片地址或本地文件说明" />
            <el-upload :action="uploadUrl" :headers="uploadHeaders" :show-file-list="false" :on-success="handleFileSuccess" accept=".pdf,.png,.jpg,.jpeg">
              <el-button type="primary">上传</el-button>
            </el-upload>
          </div>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status">
            <el-option label="已签署" value="SIGNED" />
            <el-option label="已终止" value="TERMINATED" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailDialog.visible" title="合同详情" width="700px" append-to-body>
      <el-descriptions v-if="detail" :column="2" border>
        <el-descriptions-item label="合同编号">{{ detail.contractNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ statusText(detail.status) }}</el-descriptions-item>
        <el-descriptions-item label="用户">{{ detail.userNickname || detail.userId }}</el-descriptions-item>
        <el-descriptions-item label="服务人员">{{ detail.staffName || detail.staffId }}</el-descriptions-item>
        <el-descriptions-item label="合同文件" :span="2">{{ detail.fileUrl }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus';
import { addContract, getContract, listContract, updateContract } from '@/api/contract/contract';
import { globalHeaders } from '@/utils/request';

const loading = ref(false);
const rows = ref<any[]>([]);
const total = ref(0);
const detail = ref<any>();
const queryParams = reactive({ pageNum: 1, pageSize: 10, status: undefined, keyword: '' });
const formDialog = reactive({ visible: false });
const detailDialog = reactive({ visible: false });
const form = reactive<any>({ id: undefined, userId: undefined, staffId: undefined, demandId: undefined, serviceOrderId: undefined, contractNo: '', title: '', fileUrl: '', status: 'SIGNED' });
const uploadUrl = `${import.meta.env.VITE_APP_BASE_API}/resource/oss/upload`;
const uploadHeaders = globalHeaders();

const statusText = (status: string) => ({ SIGNED: '已签署', TERMINATED: '已终止' })[status] || status || '-';

const getList = async () => {
  loading.value = true;
  try {
    const res = await listContract(queryParams);
    rows.value = res.rows || [];
    total.value = res.total || 0;
  } finally {
    loading.value = false;
  }
};

const resetQuery = () => {
  Object.assign(queryParams, { pageNum: 1, pageSize: 10, status: undefined, keyword: '' });
  getList();
};

const openForm = async (row?: any) => {
  Object.assign(form, { id: undefined, userId: undefined, staffId: undefined, demandId: undefined, serviceOrderId: undefined, contractNo: '', title: '', fileUrl: '', status: 'SIGNED' });
  if (row?.id) {
    const res = await getContract(row.id);
    Object.assign(form, res.data);
  }
  formDialog.visible = true;
};

const submitForm = async () => {
  if (form.id) {
    await updateContract(form.id, form);
  } else {
    await addContract(form);
  }
  ElMessage.success('合同已保存');
  formDialog.visible = false;
  getList();
};

const handleFileSuccess = (res: any) => {
  if (res.code !== 200) {
    ElMessage.error(res.msg || '上传失败');
    return;
  }
  form.fileUrl = res.data.url;
  ElMessage.success('合同文件已上传');
};

const openDetail = async (row: any) => {
  const res = await getContract(row.id);
  detail.value = res.data;
  detailDialog.visible = true;
};

onMounted(getList);
</script>

<style scoped>
.file-row {
  display: grid;
  grid-template-columns: 1fr auto;
  width: 100%;
  gap: 10px;
}
</style>
