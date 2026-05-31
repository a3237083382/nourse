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
          <el-input v-model="queryParams.keyword" placeholder="订单号/商品/用户" clearable style="width: 220px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="getList">查询</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" border :data="rows">
        <el-table-column label="订单号" prop="orderNo" min-width="165" />
        <el-table-column label="商品" prop="productTitle" min-width="190" show-overflow-tooltip />
        <el-table-column label="用户" prop="userNickname" min-width="110" />
        <el-table-column label="方式" width="90">
          <template #default="{ row }">{{ row.buyType === 'GROUP' ? '拼团' : '单买' }}</template>
        </el-table-column>
        <el-table-column label="金额" prop="amount" width="100" />
        <el-table-column label="拼团状态" prop="teamStatus" width="110" />
        <el-table-column label="状态" width="150">
          <template #default="{ row }">
            <el-select v-model="row.status" size="small" @change="changeStatus(row)">
              <el-option v-for="item in statuses" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="90" fixed="right">
          <template #default="{ row }"><el-button link type="primary" icon="View" @click="openDetail(row)">详情</el-button></template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>

    <el-dialog v-model="detailDialog.visible" title="团购订单详情" width="700px" append-to-body>
      <el-descriptions v-if="detail" :column="2" border>
        <el-descriptions-item label="订单号">{{ detail.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ statusText(detail.status) }}</el-descriptions-item>
        <el-descriptions-item label="商品">{{ detail.productTitle }}</el-descriptions-item>
        <el-descriptions-item label="用户">{{ detail.userNickname }}</el-descriptions-item>
        <el-descriptions-item label="购买方式">{{ detail.buyType === 'GROUP' ? '拼团' : '单买' }}</el-descriptions-item>
        <el-descriptions-item label="金额">{{ detail.amount }}</el-descriptions-item>
        <el-descriptions-item label="拼团进度" :span="2">{{ detail.joinedCount || '-' }}/{{ detail.groupSize || '-' }} {{ detail.teamStatus || '' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus';
import { getGroupOrder, listGroupOrder, updateGroupOrderStatus } from '@/api/group/group';

const statuses = [
  { label: '待分享', value: 'WAIT_SHARE' },
  { label: '待使用', value: 'WAIT_USE' },
  { label: '已使用', value: 'USED' },
  { label: '到期', value: 'EXPIRED' },
  { label: '售后', value: 'AFTER_SALE' }
];
const loading = ref(false);
const rows = ref<any[]>([]);
const total = ref(0);
const detail = ref<any>();
const detailDialog = reactive({ visible: false });
const queryParams = reactive({ pageNum: 1, pageSize: 10, status: undefined, keyword: '' });

const statusText = (status: string) => statuses.find((item) => item.value === status)?.label || status || '-';

const getList = async () => {
  loading.value = true;
  try {
    const res = await listGroupOrder(queryParams);
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

const changeStatus = async (row: any) => {
  await updateGroupOrderStatus(row.id, { status: row.status });
  ElMessage.success('团购订单状态已更新');
  getList();
};

const openDetail = async (row: any) => {
  const res = await getGroupOrder(row.id);
  detail.value = res.data;
  detailDialog.visible = true;
};

onMounted(getList);
</script>
