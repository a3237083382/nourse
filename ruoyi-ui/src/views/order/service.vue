<template>
  <div class="p-2">
    <el-card shadow="never">
      <el-form :model="queryParams" inline>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 150px">
            <el-option v-for="item in statuses" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="queryParams.keyword" placeholder="订单号/用户/阿姨" clearable style="width: 220px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="getList">查询</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
          <el-button type="success" icon="Plus" @click="openForm()">新增订单</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" border :data="rows">
        <el-table-column label="订单号" prop="orderNo" min-width="160" />
        <el-table-column label="用户" prop="userNickname" min-width="110" />
        <el-table-column label="服务人员" prop="staffName" min-width="120" />
        <el-table-column label="类型" prop="categoryName" min-width="100" />
        <el-table-column label="金额" prop="amount" width="100" />
        <el-table-column label="服务日期" min-width="190">
          <template #default="{ row }">{{ row.startDate || '-' }} 至 {{ row.endDate || '-' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="{ row }"><el-tag>{{ statusText(row.status) }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="170" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" icon="View" @click="openDetail(row)">详情</el-button>
            <el-button link type="primary" icon="Edit" @click="openForm(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>

    <el-dialog v-model="formDialog.visible" :title="form.id ? '编辑服务订单' : '新增服务订单'" width="680px" append-to-body>
      <el-form :model="form" label-width="110px">
        <el-form-item label="订单号"><el-input v-model="form.orderNo" placeholder="留空自动生成" /></el-form-item>
        <el-form-item label="用户ID"><el-input-number v-model="form.userId" :min="1" /></el-form-item>
        <el-form-item label="服务人员ID"><el-input-number v-model="form.staffId" :min="1" /></el-form-item>
        <el-form-item label="需求ID"><el-input-number v-model="form.demandId" :min="1" /></el-form-item>
        <el-form-item label="分类ID"><el-input-number v-model="form.categoryId" :min="1" /></el-form-item>
        <el-form-item label="金额"><el-input-number v-model="form.amount" :min="0" :precision="2" /></el-form-item>
        <el-form-item label="开始日期"><el-date-picker v-model="form.startDate" value-format="YYYY-MM-DD" type="date" /></el-form-item>
        <el-form-item label="结束日期"><el-date-picker v-model="form.endDate" value-format="YYYY-MM-DD" type="date" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status"><el-option v-for="item in statuses" :key="item.value" :label="item.label" :value="item.value" /></el-select>
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="form.adminNote" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailDialog.visible" title="服务订单详情" width="700px" append-to-body>
      <el-descriptions v-if="detail" :column="2" border>
        <el-descriptions-item label="订单号">{{ detail.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ statusText(detail.status) }}</el-descriptions-item>
        <el-descriptions-item label="用户">{{ detail.userNickname || detail.userId }}</el-descriptions-item>
        <el-descriptions-item label="服务人员">{{ detail.staffName || detail.staffId }}</el-descriptions-item>
        <el-descriptions-item label="服务日期">{{ detail.startDate || '-' }} 至 {{ detail.endDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="金额">{{ detail.amount }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detail.adminNote || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus';
import { addServiceOrder, getServiceOrder, listServiceOrder, updateServiceOrder } from '@/api/order/serviceOrder';

const statuses = [
  { label: '待开始', value: 'WAIT_START' },
  { label: '服务中', value: 'SERVING' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '已取消', value: 'CANCELED' }
];
const loading = ref(false);
const rows = ref<any[]>([]);
const total = ref(0);
const detail = ref<any>();
const queryParams = reactive({ pageNum: 1, pageSize: 10, status: undefined, keyword: '' });
const formDialog = reactive({ visible: false });
const detailDialog = reactive({ visible: false });
const form = reactive<any>({ id: undefined, orderNo: '', userId: undefined, staffId: undefined, demandId: undefined, categoryId: undefined, amount: 0, startDate: '', endDate: '', status: 'WAIT_START', adminNote: '' });

const statusText = (status: string) => statuses.find((item) => item.value === status)?.label || status || '-';

const getList = async () => {
  loading.value = true;
  try {
    const res = await listServiceOrder(queryParams);
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
  Object.assign(form, { id: undefined, orderNo: '', userId: undefined, staffId: undefined, demandId: undefined, categoryId: undefined, amount: 0, startDate: '', endDate: '', status: 'WAIT_START', adminNote: '' });
  if (row?.id) {
    const res = await getServiceOrder(row.id);
    Object.assign(form, res.data);
  }
  formDialog.visible = true;
};

const submitForm = async () => {
  if (form.id) await updateServiceOrder(form.id, form);
  else await addServiceOrder(form);
  ElMessage.success('服务订单已保存');
  formDialog.visible = false;
  getList();
};

const openDetail = async (row: any) => {
  const res = await getServiceOrder(row.id);
  detail.value = res.data;
  detailDialog.visible = true;
};

onMounted(getList);
</script>
