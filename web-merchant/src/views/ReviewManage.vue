<template>
  <div class="review-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>评价列表</span>
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
        <el-form-item label="评分">
          <el-select v-model="queryForm.rating" placeholder="请选择评分" clearable style="width: 120px;">
            <el-option label="全部" :value="undefined" />
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
        <el-table-column label="序号" width="70" type="index" :index="(index) => (pagination.page - 1) * pagination.size + index + 1" />
        <el-table-column prop="userAvatar" label="用户" width="80">
          <template #default="{ row }">
            <el-avatar :size="40" :src="row.userAvatar">
              {{ row.userName?.charAt(0) || '用' }}
            </el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="userName" label="用户名" width="100" />
        <el-table-column prop="userId" label="用户ID" width="90">
          <template #default="{ row }">
            <span style="font-family: monospace; font-size: 12px;">{{ row.userId }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="评价内容" min-width="180" show-overflow-tooltip />
        <el-table-column prop="rating" label="整体评分" width="120">
          <template #default="{ row }">
            <el-rate
              v-model="row.rating"
              :colors="['#ff6b00', '#ff8c00', '#ffaa33']"
              disabled
              text-color="#ff8c00"
            />
          </template>
        </el-table-column>
        <el-table-column prop="tasteRating" label="口味" width="90">
          <template #default="{ row }">
            <span v-if="row.tasteRating">{{ row.tasteRating }}★</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="portionRating" label="分量" width="90">
          <template #default="{ row }">
            <span v-if="row.portionRating">{{ row.portionRating }}★</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="orderFoodNames" label="订单菜品" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.orderFoodNames">{{ row.orderFoodNames }}</span>
            <span v-else style="color: #909399;">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="orderId" label="订单ID" width="90">
          <template #default="{ row }">
            <span v-if="row.orderId" style="font-family: monospace; font-size: 12px;">{{ row.orderId }}</span>
            <span v-else style="color: #909399;">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="orderNo" label="订单号" width="150">
          <template #default="{ row }">
            <span v-if="row.orderNo" style="font-family: monospace; font-size: 12px;">{{ row.orderNo }}</span>
            <span v-else style="color: #909399;">-</span>
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
        <el-table-column prop="replyContent" label="商家回复" min-width="150">
          <template #default="{ row }">
            <span v-if="row.replyContent" style="color: #67c23a;">
              {{ row.replyContent }}
            </span>
            <span v-else style="color: #909399;">待回复</span>
          </template>
        </el-table-column>
        <el-table-column label="评价时间" width="160">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="showReviewDetail(row)">详情</el-button>
            <el-button 
              v-if="row.status === 1 && !row.replyContent" 
              type="primary" 
              link 
              @click="handleReply(row)"
            >
              回复
            </el-button>
            <span v-else-if="row.status !== 1" style="color: #909399; font-size: 12px;">
              待审核
            </span>
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
    
    <!-- 评价详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="评价详情" width="700px">
      <div v-if="currentDetail" class="review-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="用户">{{ currentDetail.userName }}</el-descriptions-item>
          <el-descriptions-item label="用户ID">{{ currentDetail.userId }}</el-descriptions-item>
          <el-descriptions-item label="用户手机号">{{ currentDetail.userPhone }}</el-descriptions-item>
          <el-descriptions-item label="评价时间">{{ formatTime(currentDetail.createTime) }}</el-descriptions-item>
        </el-descriptions>
        
        <div style="margin: 20px 0;">
          <h4 style="margin-bottom: 10px;">评分详情</h4>
          <el-row :gutter="20">
            <el-col :span="8">
              <div style="text-align: center; padding: 10px; background: #f5f7fa; border-radius: 8px;">
                <div style="font-size: 12px; color: #909399; margin-bottom: 5px;">整体评分</div>
                <div style="font-size: 24px; font-weight: bold; color: #ff8c00;">{{ currentDetail.rating || '-' }}</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div style="text-align: center; padding: 10px; background: #f5f7fa; border-radius: 8px;">
                <div style="font-size: 12px; color: #909399; margin-bottom: 5px;">口味</div>
                <div style="font-size: 24px; font-weight: bold; color: #ff8c00;">{{ currentDetail.tasteRating || '-' }}</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div style="text-align: center; padding: 10px; background: #f5f7fa; border-radius: 8px;">
                <div style="font-size: 12px; color: #909399; margin-bottom: 5px;">分量</div>
                <div style="font-size: 24px; font-weight: bold; color: #ff8c00;">{{ currentDetail.portionRating || '-' }}</div>
              </div>
            </el-col>
          </el-row>
        </div>
        
        <div style="margin: 20px 0;">
          <h4 style="margin-bottom: 10px;">评价内容</h4>
          <div style="padding: 15px; background: #f5f7fa; border-radius: 8px; line-height: 1.6;">
            {{ currentDetail.content }}
          </div>
        </div>
        
        <div v-if="currentDetail.orderId" style="margin: 20px 0;">
          <h4 style="margin-bottom: 10px;">订单信息</h4>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="订单ID">{{ currentDetail.orderId }}</el-descriptions-item>
            <el-descriptions-item label="订单菜品">{{ currentDetail.orderFoodNames }}</el-descriptions-item>
            <el-descriptions-item label="订单金额">¥{{ currentDetail.orderFinalAmount || '0.00' }}</el-descriptions-item>
            <el-descriptions-item label="下单时间">{{ formatTime(currentDetail.orderCreateTime) }}</el-descriptions-item>
          </el-descriptions>
        </div>
        
        <div v-if="currentDetail.replyContent" style="margin: 20px 0;">
          <h4 style="margin-bottom: 10px;">商家回复</h4>
          <div style="padding: 15px; background: #f0f9ff; border-left: 3px solid #67c23a; border-radius: 4px;">
            {{ currentDetail.replyContent }}
          </div>
          <div style="margin-top: 8px; font-size: 12px; color: #909399;">
            回复时间：{{ formatTime(currentDetail.replyTime) }}
          </div>
        </div>
      </div>
      <template #footer>
        <el-button type="primary" @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
    
    <!-- 回复对话框 -->
    <el-dialog v-model="replyDialogVisible" title="回复评价" width="500px">
      <el-form :model="replyForm" label-width="80px">
        <el-form-item label="评价内容">
          <div class="review-content">{{ currentReview?.content }}</div>
        </el-form-item>
        <el-form-item label="评价详情">
          <el-button link type="primary" @click="showDetail">
            查看完整评价信息
          </el-button>
        </el-form-item>
        <el-form-item label="回复内容" required>
          <el-input
            v-model="replyForm.reply"
            type="textarea"
            :rows="4"
            placeholder="请输入回复内容"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="replyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitReply" :loading="replying">
          提交回复
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import request from '../utils/request'

const loading = ref(false)
const reviews = ref([])
const replyDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const replying = ref(false)
const currentReview = ref(null)
const currentDetail = ref(null)

const queryForm = reactive({
  status: 1,  // 默认只显示已通过的评价
  rating: null
})

const replyForm = reactive({
  reply: ''
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
    const data = await request({
      url: '/merchant/reviews',
      method: 'get',
      params: {
        status: queryForm.status,
        rating: queryForm.rating,
        page: pagination.page,
        size: pagination.size
      }
    })
    console.log('商家评价列表响应:', data)
    reviews.value = data.records || data.list || []
    console.log('商家评价数据:', reviews.value)
    pagination.total = data.total || 0
  } catch (error) {
    console.error('加载评价列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  queryForm.status = 1  // 重置为只显示已通过的评价
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

const showReviewDetail = (row) => {
  currentDetail.value = row
  detailDialogVisible.value = true
}

const showDetail = () => {
  if (currentReview.value) {
    currentDetail.value = currentReview.value
    detailDialogVisible.value = true
  }
}

const handleReply = (row) => {
  currentReview.value = row
  replyForm.reply = ''
  replyDialogVisible.value = true
}

const handleSubmitReply = async () => {
  if (!replyForm.reply.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }
  
  replying.value = true
  try {
    await request({
      url: `/merchant/reviews/${currentReview.value.id}/reply`,
      method: 'post',
      data: { reply: replyForm.reply }
    })
    ElMessage.success('回复成功')
    replyDialogVisible.value = false
    loadReviews()
  } catch (error) {
    console.error('回复失败:', error)
  } finally {
    replying.value = false
  }
}

onMounted(() => {
  loadReviews()
})
</script>

<style scoped>
.review-manage {
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

.review-content {
  padding: 12px;
  background: #f5f7fa;
  border-radius: 4px;
  color: #606266;
}

.review-detail {
  max-height: 500px;
  overflow-y: auto;
}
</style>
