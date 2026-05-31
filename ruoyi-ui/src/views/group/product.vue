<template>
  <div class="p-2">
    <el-card shadow="never">
      <el-form :model="queryParams" inline>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 140px">
            <el-option label="上架" value="ONLINE" />
            <el-option label="下架" value="OFFLINE" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="queryParams.keyword" placeholder="商品名称" clearable style="width: 220px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="getList">查询</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
          <el-button type="success" icon="Plus" @click="openForm()">新增商品</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" border :data="rows">
        <el-table-column label="商品名称" prop="title" min-width="180" show-overflow-tooltip />
        <el-table-column label="单买价" prop="singlePrice" width="100" />
        <el-table-column label="拼团价" prop="groupPrice" width="100" />
        <el-table-column label="人数" prop="groupSize" width="80" />
        <el-table-column label="已售" prop="soldCount" width="80" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }"><el-tag :type="row.status === 'ONLINE' ? 'success' : 'info'">{{ row.status === 'ONLINE' ? '上架' : '下架' }}</el-tag></template>
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

    <el-dialog v-model="formDialog.visible" :title="form.id ? '编辑团购商品' : '新增团购商品'" width="760px" append-to-body>
      <el-form :model="form" label-width="100px">
        <el-form-item label="商品名称"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="封面地址"><el-input v-model="form.coverUrl" /></el-form-item>
        <el-form-item label="原价"><el-input-number v-model="form.originalPrice" :min="0" :precision="2" /></el-form-item>
        <el-form-item label="单买价"><el-input-number v-model="form.singlePrice" :min="0" :precision="2" /></el-form-item>
        <el-form-item label="拼团价"><el-input-number v-model="form.groupPrice" :min="0" :precision="2" /></el-form-item>
        <el-form-item label="成团人数"><el-input-number v-model="form.groupSize" :min="2" /></el-form-item>
        <el-form-item label="有效天数"><el-input-number v-model="form.validDays" :min="1" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status">
            <el-option label="上架" value="ONLINE" />
            <el-option label="下架" value="OFFLINE" />
          </el-select>
        </el-form-item>
        <el-form-item label="购买须知"><el-input v-model="form.notice" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="服务保障"><el-input v-model="form.guarantee" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="商品介绍"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailDialog.visible" title="团购商品详情" width="700px" append-to-body>
      <el-descriptions v-if="detail" :column="2" border>
        <el-descriptions-item label="商品名称">{{ detail.title }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ detail.status === 'ONLINE' ? '上架' : '下架' }}</el-descriptions-item>
        <el-descriptions-item label="单买价">{{ detail.singlePrice }}</el-descriptions-item>
        <el-descriptions-item label="拼团价">{{ detail.groupPrice }}</el-descriptions-item>
        <el-descriptions-item label="购买须知" :span="2">{{ detail.notice || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus';
import { addGroupProduct, getGroupProduct, listGroupProduct, updateGroupProduct } from '@/api/group/group';

const loading = ref(false);
const rows = ref<any[]>([]);
const total = ref(0);
const detail = ref<any>();
const queryParams = reactive({ pageNum: 1, pageSize: 10, status: undefined, keyword: '' });
const formDialog = reactive({ visible: false });
const detailDialog = reactive({ visible: false });
const form = reactive<any>({ id: undefined, title: '', coverUrl: '', originalPrice: 0, singlePrice: 0, groupPrice: 0, groupSize: 2, validDays: 30, notice: '', guarantee: '', description: '', status: 'ONLINE' });

const getList = async () => {
  loading.value = true;
  try {
    const res = await listGroupProduct(queryParams);
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
  Object.assign(form, { id: undefined, title: '', coverUrl: '', originalPrice: 0, singlePrice: 0, groupPrice: 0, groupSize: 2, validDays: 30, notice: '', guarantee: '', description: '', status: 'ONLINE' });
  if (row?.id) {
    const res = await getGroupProduct(row.id);
    Object.assign(form, res.data);
  }
  formDialog.visible = true;
};

const submitForm = async () => {
  if (form.id) await updateGroupProduct(form.id, form);
  else await addGroupProduct(form);
  ElMessage.success('团购商品已保存');
  formDialog.visible = false;
  getList();
};

const openDetail = async (row: any) => {
  const res = await getGroupProduct(row.id);
  detail.value = res.data;
  detailDialog.visible = true;
};

onMounted(getList);
</script>
