<template>
  <div class="order-detail">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>订单详情</span>
          <el-button @click="router.back()">返回</el-button>
        </div>
      </template>
      
      <div v-if="order" class="detail-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号">
            {{ order.orderNo }}
          </el-descriptions-item>
          <el-descriptions-item label="订单状态">
            <el-tag :type="getStatusType(order.status)">
              {{ getStatusText(order.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="下单时间">
            {{ formatTime(order.createTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="支付状态">
            <el-tag :type="order.payStatus === 1 ? 'success' : 'warning'">
              {{ order.payStatus === 1 ? '已支付' : '未支付' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="用户ID">
            {{ order.userId }}
          </el-descriptions-item>
          <el-descriptions-item label="用户电话">
            {{ order.userPhone }}
          </el-descriptions-item>
          <el-descriptions-item label="收货人" :span="1">
            {{ order.receiverName }}
          </el-descriptions-item>
          <el-descriptions-item label="收货电话" :span="1">
            {{ order.receiverPhone }}
          </el-descriptions-item>
          <el-descriptions-item label="收货地址" :span="2">
            {{ order.receiverAddress }}
          </el-descriptions-item>
          <el-descriptions-item label="订单备注" :span="2">
            {{ order.remark || '无' }}
          </el-descriptions-item>
        </el-descriptions>
        
        <el-divider content-position="left">订单商品</el-divider>
        
        <el-table :data="order.items" style="width: 100%;">
          <el-table-column label="商品图片" width="80">
            <template #default="{ row }">
              <el-image
                :src="getImageUrl(row.foodImage)"
                fit="cover"
                style="width: 50px; height: 50px; border-radius: 4px;"
              />
            </template>
          </el-table-column>
          <el-table-column prop="foodName" label="商品名称" />
          <el-table-column prop="price" label="单价" width="100">
            <template #default="{ row }">
              ¥{{ row.price }}
            </template>
          </el-table-column>
          <el-table-column prop="quantity" label="数量" width="80" />
          <el-table-column label="小计" width="100">
            <template #default="{ row }">
              <span style="color: #ff8c00; font-weight: 600;">
                ¥{{ (row.price * row.quantity).toFixed(2) }}
              </span>
            </template>
          </el-table-column>
        </el-table>
        
        <div class="amount-summary">
          <div class="summary-item">
            <span>商品总金额：</span>
            <span>¥{{ (order.finalAmount || order.totalAmount) - (order.deliveryFee || 0) - (order.packFee || 0) + (order.couponDiscount || 0) }}</span>
          </div>
          <div class="summary-item">
            <span>配送费：</span>
            <span>¥{{ order.deliveryFee }}</span>
          </div>
          <div class="summary-item">
            <span>包装费：</span>
            <span>¥{{ order.packFee }}</span>
          </div>
          <div class="summary-item" v-if="order.couponDiscount && order.couponDiscount > 0" style="color: #67C23A;">
            <span>优惠券优惠：</span>
            <span>-¥{{ order.couponDiscount }}</span>
          </div>
          <div class="summary-item total">
            <span>实付金额：</span>
            <span>¥{{ order.finalAmount || order.totalAmount }}</span>
          </div>
        </div>

        <DeliveryTracking v-if="order.status === 2 || order.status === 3" :order-id="order.id" />
        
        <div class="action-buttons">
          <el-button 
            v-if="order.status === 1" 
            type="success" 
            size="large" 
            @click="handleAccept"
          >
            接单
          </el-button>
          <el-button 
            v-if="order.status === 2" 
            type="warning" 
            size="large" 
            @click="handleComplete"
          >
            完成配送
          </el-button>
          <el-button 
            v-if="order.status === 1 || order.status === 0" 
            type="danger" 
            size="large" 
            @click="handleCancel"
          >
            取消订单
          </el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import request from '../utils/request'
import DeliveryTracking from '../components/DeliveryTracking.vue'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const order = ref(null)

const defaultFoodImage = 'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIxMDAiIGhlaWdodD0iMTAwIj48cmVjdCB3aWR0aD0iMTAwIiBoZWlnaHQ9IjEwMCIgZmlsbD0iI2YzZjRmNiIvPjx0ZXh0IHg9IjUwIiB5PSI1MCIgdGV4dC1hbmNob3I9Im1pZGRsZSIgZmlsbD0iI2EwYTBhMCI+5rOV5ZOBPC90ZXh0Pjwvc3ZnPg=='

const getImageUrl = (path) => {
  if (!path) return defaultFoodImage
  if (path.startsWith('http')) return path
  const baseUrl = import.meta.env.VITE_IMAGE_BASE_URL || ''
  return `${baseUrl}${path}`
}

const statusMap = {
  0: { text: '待支付', type: 'warning' },
  1: { text: '待接单', type: 'info' },
  2: { text: '已接单', type: 'primary' },
  3: { text: '配送中', type: 'warning' },
  4: { text: '已完成', type: 'success' },
  5: { text: '已取消', type: 'danger' }
}

const getStatusText = (status) => statusMap[status]?.text || '未知'
const getStatusType = (status) => statusMap[status]?.type || 'info'

const formatTime = (time) => {
  return time ? dayjs(time).format('YYYY-MM-DD HH:mm:ss') : '-'
}

const loadOrderDetail = async () => {
  loading.value = true
  try {
    const data = await request({
      url: `/merchant/orders/${route.params.id}`,
      method: 'get'
    })
    order.value = data
  } catch (error) {
    console.error('加载订单详情失败:', error)
    ElMessage.error('加载失败')
    router.back()
  } finally {
    loading.value = false
  }
}

const handleAccept = async () => {
  try {
    await request({
      url: `/merchant/order/accept/${route.params.id}`,
      method: 'post'
    })
    ElMessage.success('已接单')
    loadOrderDetail()
  } catch (error) {
    console.error('接单失败:', error)
  }
}

const handleComplete = async () => {
  try {
    await request({
      url: `/merchant/orders/${route.params.id}/complete`,
      method: 'post'
    })
    ElMessage.success('订单已完成')
    loadOrderDetail()
  } catch (error) {
    console.error('操作失败:', error)
  }
}

const handleCancel = () => {
  ElMessageBox.confirm('确定要取消该订单吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request({
        url: `/merchant/orders/${route.params.id}/cancel`,
        method: 'post'
      })
      ElMessage.success('订单已取消')
      loadOrderDetail()
    } catch (error) {
      console.error('取消失败:', error)
    }
  }).catch(() => {})
}

onMounted(() => {
  loadOrderDetail()
})
</script>

<style scoped>
.order-detail {
  min-height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.detail-content {
  padding: 20px 0;
}

.el-divider--horizontal {
  margin: 30px 0;
}

.amount-summary {
  margin-top: 20px;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
  font-size: 14px;
}

.summary-item.total {
  font-size: 18px;
  font-weight: 600;
  color: #ff8c00;
  padding-top: 10px;
  border-top: 1px solid #e4e7ed;
  margin-top: 10px;
}

.action-buttons {
  margin-top: 30px;
  display: flex;
  justify-content: center;
  gap: 20px;
}
</style>
