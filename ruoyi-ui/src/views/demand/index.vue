<template>
  <div class="p-2">
    <el-card shadow="hover" class="mb-[10px]">
      <el-form ref="queryFormRef" :model="queryParams" :inline="true">
        <el-form-item label="服务分类" prop="categoryId">
          <el-select v-model="queryParams.categoryId" placeholder="全部分类" clearable style="width: 170px">
            <el-option v-for="item in categoryOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="审核状态" prop="auditStatus">
          <el-select v-model="queryParams.auditStatus" placeholder="全部" clearable style="width: 150px">
            <el-option label="审核中" value="PENDING" />
            <el-option label="已通过" value="APPROVED" />
            <el-option label="已拒绝" value="REJECTED" />
            <el-option label="已取消" value="CANCELED" />
          </el-select>
        </el-form-item>
        <el-form-item label="跟进状态" prop="followStatus">
          <el-select v-model="queryParams.followStatus" placeholder="全部" clearable style="width: 150px">
            <el-option label="待跟进" value="TO_FOLLOW" />
            <el-option label="已联系" value="CONTACTED" />
            <el-option label="已匹配" value="MATCHED" />
            <el-option label="已签约" value="SIGNED" />
            <el-option label="已关闭" value="CLOSED" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词" prop="keyword">
          <el-input v-model="queryParams.keyword" placeholder="标题、联系人、电话" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="hover">
      <el-table v-loading="loading" border :data="demandList">
        <el-table-column label="标题" prop="title" min-width="180" />
        <el-table-column label="服务分类" prop="categoryName" width="120" />
        <el-table-column label="联系人" prop="contactName" width="110" />
        <el-table-column label="电话" prop="contactPhone" width="130" />
        <el-table-column label="区域" min-width="150">
          <template #default="{ row }">{{ row.city || '-' }} {{ row.district || '' }}</template>
        </el-table-column>
        <el-table-column label="审核" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="auditTagType(row.auditStatus)">{{ auditText(row.auditStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="跟进" width="100" align="center">
          <template #default="{ row }">
            <el-tag>{{ followText(row.followStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="提交时间" prop="createdAt" width="170" />
        <el-table-column label="操作" width="240" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" icon="View" @click="openDetail(row)">详情</el-button>
            <el-button v-if="row.auditStatus === 'PENDING'" link type="success" icon="Check" @click="handleApprove(row)">通过</el-button>
            <el-button v-if="row.auditStatus === 'PENDING'" link type="danger" icon="Close" @click="openReject(row)">拒绝</el-button>
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

    <el-dialog v-model="detailDialog.visible" title="需求详情" width="920px" append-to-body>
      <el-descriptions v-if="detail" :column="2" border>
        <el-descriptions-item label="标题">{{ detail.title }}</el-descriptions-item>
        <el-descriptions-item label="服务分类">{{ detail.categoryName }}</el-descriptions-item>
        <el-descriptions-item label="联系人">{{ detail.contact_name || detail.contactName }}</el-descriptions-item>
        <el-descriptions-item label="电话">{{ detail.contact_phone || detail.contactPhone }}</el-descriptions-item>
        <el-descriptions-item label="区域">{{ detail.city }} {{ detail.district }}</el-descriptions-item>
        <el-descriptions-item label="地址">{{ detail.address }}</el-descriptions-item>
        <el-descriptions-item label="审核状态">{{ auditText(detail.audit_status || detail.auditStatus) }}</el-descriptions-item>
        <el-descriptions-item label="跟进状态">
          <el-select v-model="followForm.followStatus" style="width: 160px" @change="handleFollowChange">
            <el-option label="待跟进" value="TO_FOLLOW" />
            <el-option label="已联系" value="CONTACTED" />
            <el-option label="已匹配" value="MATCHED" />
            <el-option label="已签约" value="SIGNED" />
            <el-option label="已关闭" value="CLOSED" />
          </el-select>
        </el-descriptions-item>
        <el-descriptions-item label="薪资待遇">{{ detail.expected_salary || detail.expectedSalary || '-' }}</el-descriptions-item>
        <el-descriptions-item label="是否住家">{{ detail.live_in || detail.liveIn ? '是' : '否' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detail.remark || '-' }}</el-descriptions-item>
      </el-descriptions>

      <div class="recommend-box">
        <div class="recommend-title">
          <span>推荐阿姨</span>
          <el-button v-if="detail && (detail.audit_status || detail.auditStatus) === 'APPROVED'" type="primary" plain icon="Plus" @click="openRecommend">
            添加推荐
          </el-button>
        </div>
        <el-table border :data="recommendations">
          <el-table-column label="阿姨" prop="staffName" min-width="120" />
          <el-table-column label="服务类型" prop="categoryName" width="120" />
          <el-table-column label="城市" prop="city" width="100" />
          <el-table-column label="推荐理由" prop="reason" min-width="180" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">{{ recommendText(row.status) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="90" align="center">
            <template #default="{ row }">
              <el-button link type="danger" icon="Delete" @click="handleDeleteRecommendation(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>

    <el-dialog v-model="rejectDialog.visible" title="审核拒绝" width="520px" append-to-body>
      <el-input v-model="rejectForm.reason" type="textarea" :rows="4" placeholder="请输入拒绝原因，用户会在消息中看到" />
      <template #footer>
        <el-button @click="rejectDialog.visible = false">取消</el-button>
        <el-button type="danger" @click="handleReject">确认拒绝</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="recommendDialog.visible" title="添加推荐阿姨" width="620px" append-to-body>
      <el-form :model="recommendForm" label-width="90px">
        <el-form-item label="服务人员">
          <el-select v-model="recommendForm.staffId" filterable placeholder="请选择已上架服务人员" style="width: 100%">
            <el-option
              v-for="item in staffOptions"
              :key="item.id"
              :label="`${item.name} / ${item.categoryName || '-'} / ${item.city || '-'}`"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="推荐理由">
          <el-input v-model="recommendForm.reason" type="textarea" :rows="3" maxlength="255" show-word-limit />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="recommendForm.sortNo" :min="0" controls-position="right" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="recommendDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="handleAddRecommendation">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts" name="DemandManage">
import { onMounted, reactive, ref } from 'vue';
import type { FormInstance } from 'element-plus';
import { ElMessage, ElMessageBox } from 'element-plus';
import { listCategory } from '@/api/staff/category';
import { listStaff } from '@/api/staff/staff';
import {
  addDemandRecommendation,
  approveDemand,
  deleteDemandRecommendation,
  getDemand,
  listDemand,
  rejectDemand,
  updateDemandFollowStatus
} from '@/api/demand/demand';

const loading = ref(false);
const queryFormRef = ref<FormInstance>();
const categoryOptions = ref<any[]>([]);
const staffOptions = ref<any[]>([]);
const demandList = ref<any[]>([]);
const recommendations = ref<any[]>([]);
const detail = ref<any>();
const total = ref(0);
const queryParams = reactive<any>({
  pageNum: 1,
  pageSize: 10,
  categoryId: undefined,
  auditStatus: undefined,
  followStatus: undefined,
  keyword: ''
});
const detailDialog = reactive({ visible: false });
const rejectDialog = reactive({ visible: false });
const recommendDialog = reactive({ visible: false });
const currentDemandId = ref<number | string>();
const rejectForm = reactive({ reason: '' });
const followForm = reactive({ followStatus: 'TO_FOLLOW' });
const recommendForm = reactive<any>({ staffId: undefined, reason: '', sortNo: 0 });

const auditText = (status: string) => ({ PENDING: '审核中', APPROVED: '已通过', REJECTED: '已拒绝', CANCELED: '已取消' })[status] || status || '-';
const followText = (status: string) => ({ TO_FOLLOW: '待跟进', CONTACTED: '已联系', MATCHED: '已匹配', SIGNED: '已签约', CLOSED: '已关闭' })[status] || status || '-';
const recommendText = (status: string) => ({ RECOMMENDED: '已推荐', VIEWED: '用户已查看', INTERVIEWED: '已预约面试', IGNORED: '已忽略' })[status] || status || '-';
const auditTagType = (status: string) => {
  if (status === 'APPROVED') return 'success';
  if (status === 'REJECTED' || status === 'CANCELED') return 'info';
  return 'warning';
};

const getCategories = async () => {
  const res: any = await listCategory();
  categoryOptions.value = res.rows || [];
};

const getStaffOptions = async () => {
  const res: any = await listStaff({ pageNum: 1, pageSize: 100, status: 'ONLINE' });
  staffOptions.value = res.rows || [];
};

const getList = async () => {
  loading.value = true;
  try {
    const res: any = await listDemand(queryParams);
    demandList.value = res.rows || [];
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

const openDetail = async (row: any) => {
  currentDemandId.value = row.id;
  const res: any = await getDemand(row.id);
  detail.value = res.data;
  recommendations.value = res.data?.recommendations || [];
  followForm.followStatus = detail.value.follow_status || detail.value.followStatus || 'TO_FOLLOW';
  detailDialog.visible = true;
};

const refreshDetail = async () => {
  if (!currentDemandId.value) return;
  const res: any = await getDemand(currentDemandId.value);
  detail.value = res.data;
  recommendations.value = res.data?.recommendations || [];
  followForm.followStatus = detail.value.follow_status || detail.value.followStatus || 'TO_FOLLOW';
};

const handleApprove = async (row: any) => {
  await ElMessageBox.confirm('确认审核通过该需求？', '审核确认', { type: 'warning' });
  await approveDemand(row.id);
  ElMessage.success('已审核通过');
  getList();
};

const openReject = (row: any) => {
  currentDemandId.value = row.id;
  rejectForm.reason = '';
  rejectDialog.visible = true;
};

const handleReject = async () => {
  if (!currentDemandId.value) return;
  await rejectDemand(currentDemandId.value, { reason: rejectForm.reason });
  ElMessage.success('已拒绝');
  rejectDialog.visible = false;
  getList();
};

const handleFollowChange = async () => {
  if (!currentDemandId.value) return;
  await updateDemandFollowStatus(currentDemandId.value, { followStatus: followForm.followStatus });
  ElMessage.success('跟进状态已更新');
  getList();
  refreshDetail();
};

const openRecommend = async () => {
  await getStaffOptions();
  Object.assign(recommendForm, { staffId: undefined, reason: '', sortNo: 0 });
  recommendDialog.visible = true;
};

const handleAddRecommendation = async () => {
  if (!currentDemandId.value || !recommendForm.staffId) {
    ElMessage.warning('请选择服务人员');
    return;
  }
  await addDemandRecommendation(currentDemandId.value, recommendForm);
  ElMessage.success('推荐已添加');
  recommendDialog.visible = false;
  getList();
  refreshDetail();
};

const handleDeleteRecommendation = async (row: any) => {
  await ElMessageBox.confirm('确认删除该推荐？', '删除确认', { type: 'warning' });
  await deleteDemandRecommendation(row.id);
  ElMessage.success('已删除');
  refreshDetail();
};

onMounted(() => {
  getCategories();
  getList();
});
</script>

<style scoped>
.recommend-box {
  margin-top: 20px;
}

.recommend-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  font-weight: 600;
}
</style>
