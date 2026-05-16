<template>
  <div class="review-audit">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>评价审核管理</span>
        </div>
      </template>
      
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="审核状态">
          <el-select v-model="queryForm.status" placeholder="请选择状态" clearable style="width: 140px;">
            <el-option label="全部" :value="undefined" />
            <el-option label="待审核" :value="0" />
            <el-option label="已通过" :value="1" />
            <el-option label="已拒绝" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="评价内容">
          <el-input v-model="queryForm.content" placeholder="请输入评价内容" clearable />
        </el-form-item>
        <el-form-item label="评分">
          <el-select v-model="queryForm.rating" placeholder="请选择评分" clearable style="width: 120px;">
            <el-option label="5星" :value="5" />
            <el-option label="4星" :value="4" />
            <el-option label="3星" :value="3" />
            <el-option label="2星" :value="2" />
            <el-option label="1星" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadReviews">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
      
      <el-table :data="reviews" v-loading="loading" style="width: 100%;">
        <el-table-column label="序号" width="80" type="index" :index="(index) => (pagination.page - 1) * pagination.size + index + 1" />
        <el-table-column prop="id" label="评价ID" width="80" />
        <el-table-column prop="userId" label="用户ID" width="100" />
        <el-table-column prop="merchantId" label="商家ID" width="100" />
        <el-table-column prop="orderId" label="订单ID" width="100" />
        <el-table-column prop="orderNo" label="订单号" width="150">
          <template #default="{ row }">
            <span v-if="row.orderNo" style="font-family: monospace; font-size: 12px;">{{ row.orderNo }}</span>
            <span v-else style="color: #909399;">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="rating" label="评分" width="150">
          <template #default="{ row }">
            <el-rate v-model="row.rating" disabled show-score />
          </template>
        </el-table-column>
        <el-table-column prop="content" label="评价内容" min-width="200" show-overflow-tooltip />
        <el-table-column prop="replyContent" label="商家回复" min-width="150">
          <template #default="{ row }">
            <span v-if="row.replyContent" style="color: #67c23a;">{{ row.replyContent }}</span>
            <span v-else style="color: #909399;">未回复</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 0" type="warning">待审核</el-tag>
            <el-tag v-else-if="row.status === 1" type="success">已通过</el-tag>
            <el-tag v-else-if="row.status === 2" type="danger">已拒绝</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="评价时间" width="160">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 0" type="success" link @click="handleApprove(row)">
              通过
            </el-button>
            <el-button v-if="row.status === 0" type="danger" link @click="handleReject(row)">
              拒绝
            </el-button>
            <el-button type="primary" link @click="handleView(row)">
              查看
            </el-button>
            <el-button type="danger" link @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        style="margin-top: 20px; display: flex; justify-content: flex-end;"
      />
    </el-card>
    
    <el-dialog v-model="detailVisible" title="评价详情" width="600px">
      <el-descriptions v-if="currentReview" :column="2" border>
        <el-descriptions-item label="评价ID">{{ currentReview.id }}</el-descriptions-item>
        <el-descriptions-item label="用户ID">{{ currentReview.userId }}</el-descriptions-item>
        <el-descriptions-item label="商家ID">{{ currentReview.merchantId }}</el-descriptions-item>
        <el-descriptions-item label="订单ID">{{ currentReview.orderId }}</el-descriptions-item>
        <el-descriptions-item label="订单号">{{ currentReview.orderNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="评分">
          <el-rate v-model="currentReview.rating" disabled show-score />
        </el-descriptions-item>
        <el-descriptions-item label="评价时间">
          {{ formatTime(currentReview.createTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="评价内容" :span="2">
          {{ currentReview.content }}
        </el-descriptions-item>
        <el-descriptions-item label="商家回复" :span="2">
          {{ currentReview.replyContent || '未回复' }}
        </el-descriptions-item>
        <el-descriptions-item label="审核状态" :span="2">
          <el-tag v-if="currentReview.status === 0" type="warning">待审核</el-tag>
          <el-tag v-else-if="currentReview.status === 1" type="success">已通过</el-tag>
          <el-tag v-else-if="currentReview.status === 2" type="danger">已拒绝</el-tag>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button v-if="currentReview.status === 0" type="success" @click="handleApprove(currentReview)">通过</el-button>
        <el-button v-if="currentReview.status === 0" type="danger" @click="handleReject(currentReview)">拒绝</el-button>
        <el-button type="danger" @click="handleDelete(currentReview)">删除评价</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import request from '../utils/request'

const loading = ref(false)
const reviews = ref([])
const detailVisible = ref(false)
const currentReview = ref(null)

const queryForm = reactive({
  status: null,
  content: '',
  rating: null
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const formatTime = (time) => {
  return time ? dayjs(time).format('YYYY-MM-DD HH:mm') : '-'
}

const loadReviews = async () => {
  loading.value = true
  try {
    const res = await request({
      url: '/admin/reviews',
      method: 'get',
      params: {
        status: queryForm.status,
        rating: queryForm.rating,
        pageNum: pagination.page,
        pageSize: pagination.size
      }
    })
    console.log('评价列表响应:', res)
    const pageData = res.data || res
    console.log('分页数据:', pageData)
    reviews.value = pageData.records || pageData.list || []
    console.log('评价数据:', reviews.value)
    pagination.total = pageData.total || 0
  } catch (error) {
    console.error('加载评价列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  queryForm.status = null
  queryForm.content = ''
  queryForm.rating = null
  pagination.page = 1
  loadReviews()
}

// 监听分页变化
watch(() => pagination.page, () => {
  loadReviews()
})

watch(() => pagination.size, () => {
  pagination.page = 1
  loadReviews()
})

const handleView = (row) => {
  currentReview.value = row
  detailVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该评价吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request({
        url: `/admin/reviews/${row.id}`,
        method: 'delete'
      })
      ElMessage.success('删除成功')
      loadReviews()
      detailVisible.value = false
    } catch (error) {
      console.error('删除失败:', error)
    }
  }).catch(() => {})
}

const handleApprove = (row) => {
  ElMessageBox.confirm('确定要通过该评价吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request({
        url: `/admin/review/${row.id}/audit`,
        method: 'post',
        params: { status: 1 }
      })
      ElMessage.success('审核通过')
      loadReviews()
    } catch (error) {
      console.error('审核失败:', error)
    }
  }).catch(() => {})
}

const handleReject = (row) => {
  ElMessageBox.confirm('确定要拒绝该评价吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request({
        url: `/admin/review/${row.id}/audit`,
        method: 'post',
        params: { status: 2 }
      })
      ElMessage.success('已拒绝')
      loadReviews()
    } catch (error) {
      console.error('审核失败:', error)
    }
  }).catch(() => {})
}

onMounted(() => {
  loadReviews()
})
</script>

<style scoped>
.review-audit {
  min-height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 20px;
}

:deep(.el-card) {
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  border: none;
}

:deep(.el-button--primary) {
  background: linear-gradient(135deg, #ff8c00 0%, #ffa033 100%);
  border: none;
}

:deep(.el-rate) {
  --el-rate-active-color: #ff9900;
}
</style>
