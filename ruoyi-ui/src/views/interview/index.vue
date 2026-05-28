<template>
  <div class="p-2">
    <el-card shadow="never">
      <el-form :model="queryParams" inline>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 150px">
            <el-option label="待处理" value="PENDING" />
            <el-option label="已联系" value="CONTACTED" />
            <el-option label="已安排" value="ARRANGED" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已取消" value="CANCELED" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="queryParams.keyword" placeholder="联系人/电话/阿姨/需求" clearable style="width: 220px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="getList">查询</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" border :data="interviewList">
        <el-table-column label="联系人" prop="contactName" min-width="110" />
        <el-table-column label="联系电话" prop="contactPhone" min-width="130" />
        <el-table-column label="服务人员" prop="staffName" min-width="120" />
        <el-table-column label="来源需求" prop="demandTitle" min-width="160" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="提交时间" prop="createdAt" min-width="170" />
        <el-table-column label="操作" width="110" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" icon="View" @click="openDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination
        v-show="total > 0"
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        :total="total"
        @pagination="getList"
      />
    </el-card>

    <el-dialog v-model="detailDialog.visible" title="预约详情" width="720px" append-to-body>
      <el-descriptions v-if="detail" :column="2" border>
        <el-descriptions-item label="联系人">{{ detail.contactName }}</el-descriptions-item>
        <el-descriptions-item label="电话">{{ detail.contactPhone }}</el-descriptions-item>
        <el-descriptions-item label="服务人员">{{ detail.staffName }}</el-descriptions-item>
        <el-descriptions-item label="服务类型">{{ detail.categoryName }}</el-descriptions-item>
        <el-descriptions-item label="来源需求">{{ detail.demandTitle || '-' }}</el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ detail.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="状态" :span="2">
          <el-select v-model="statusForm.status" style="width: 180px" @change="handleStatusChange">
            <el-option label="待处理" value="PENDING" />
            <el-option label="已联系" value="CONTACTED" />
            <el-option label="已安排" value="ARRANGED" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已取消" value="CANCELED" />
          </el-select>
        </el-descriptions-item>
      </el-descriptions>

      <div class="note-box">
        <div class="note-title">后台备注</div>
        <el-input v-model="noteForm.adminNote" type="textarea" :rows="4" maxlength="500" show-word-limit />
      </div>

      <template #footer>
        <el-button @click="detailDialog.visible = false">关闭</el-button>
        <el-button type="primary" @click="handleNoteSave">保存备注</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus';
import { getInterview, listInterview, updateInterviewNote, updateInterviewStatus } from '@/api/interview/interview';

const loading = ref(false);
const total = ref(0);
const interviewList = ref<any[]>([]);
const detail = ref<any>();
const currentId = ref<number | string>();
const detailDialog = reactive({ visible: false });
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  status: undefined,
  keyword: ''
});
const statusForm = reactive({ status: 'PENDING' });
const noteForm = reactive({ adminNote: '' });

const statusText = (status: string) => ({
  PENDING: '待处理',
  CONTACTED: '已联系',
  ARRANGED: '已安排',
  COMPLETED: '已完成',
  CANCELED: '已取消'
})[status] || status || '-';

const statusTag = (status: string) => {
  if (status === 'COMPLETED') return 'success';
  if (status === 'CANCELED') return 'info';
  if (status === 'ARRANGED') return 'warning';
  return 'primary';
};

const getList = async () => {
  loading.value = true;
  try {
    const res = await listInterview(queryParams);
    interviewList.value = res.rows || [];
    total.value = res.total || 0;
  } finally {
    loading.value = false;
  }
};

const resetQuery = () => {
  Object.assign(queryParams, { pageNum: 1, pageSize: 10, status: undefined, keyword: '' });
  getList();
};

const refreshDetail = async () => {
  if (!currentId.value) return;
  const res = await getInterview(currentId.value);
  detail.value = res.data;
  statusForm.status = detail.value.status || 'PENDING';
  noteForm.adminNote = detail.value.adminNote || '';
};

const openDetail = async (row: any) => {
  currentId.value = row.id;
  await refreshDetail();
  detailDialog.visible = true;
};

const handleStatusChange = async () => {
  if (!currentId.value) return;
  await updateInterviewStatus(currentId.value, { status: statusForm.status });
  ElMessage.success('预约状态已更新');
  await refreshDetail();
  getList();
};

const handleNoteSave = async () => {
  if (!currentId.value) return;
  await updateInterviewNote(currentId.value, { adminNote: noteForm.adminNote });
  ElMessage.success('后台备注已保存');
  await refreshDetail();
};

onMounted(getList);
</script>

<style scoped>
.note-box {
  margin-top: 20px;
}

.note-title {
  margin-bottom: 10px;
  color: #303133;
  font-weight: 600;
}
</style>
