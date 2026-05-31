<template>
  <div class="p-2">
    <el-card shadow="never">
      <el-table v-loading="loading" border :data="rows">
        <el-table-column label="商品" prop="productTitle" min-width="190" show-overflow-tooltip />
        <el-table-column label="团长用户ID" prop="leaderUserId" width="120" />
        <el-table-column label="人数" width="100">
          <template #default="{ row }">{{ row.joinedCount }}/{{ row.groupSize }}</template>
        </el-table-column>
        <el-table-column label="过期时间" prop="expireAt" min-width="170" />
        <el-table-column label="状态" width="140">
          <template #default="{ row }">
            <el-select v-model="row.status" size="small" @change="changeStatus(row)">
              <el-option label="拼团中" value="GROUPING" />
              <el-option label="拼团成功" value="SUCCESS" />
              <el-option label="拼团失败" value="FAILED" />
              <el-option label="已取消" value="CANCELED" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createdAt" min-width="170" />
      </el-table>

      <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus';
import { listGroupTeam, updateGroupTeamStatus } from '@/api/group/group';

const loading = ref(false);
const rows = ref<any[]>([]);
const total = ref(0);
const queryParams = reactive({ pageNum: 1, pageSize: 10 });

const getList = async () => {
  loading.value = true;
  try {
    const res = await listGroupTeam(queryParams);
    rows.value = res.rows || [];
    total.value = res.total || 0;
  } finally {
    loading.value = false;
  }
};

const changeStatus = async (row: any) => {
  await updateGroupTeamStatus(row.id, { status: row.status });
  ElMessage.success('拼团状态已更新');
  getList();
};

onMounted(getList);
</script>
