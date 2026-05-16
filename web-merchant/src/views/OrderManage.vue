<template>
  <div class="order-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>订单列表</span>
        </div>
      </template>
      
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="订单号">
          <el-input v-model="queryForm.orderNo" placeholder="请输入订单号" clearable style="width: 180px;" />
        </el-form-item>
        <el-form-item label="订单状态">
          <el-select v-model="queryForm.status" placeholder="请选择状态" clearable style="width: 130px;">
            <el-option label="全部" :value="null" />
            <el-option label="待支付" :value="0" />
            <el-option label="待接单" :value="1" />
            <el-option label="已接单" :value="2" />
            <el-option label="配送中" :value="3" />
            <el-option label="已完成" :value="4" />
            <el-option label="已取消" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker
            v-model="queryForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 240px;"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadOrders">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
      
      <el-table :data="orders" v-loading="loading" style="width: 100%;">
        <el-table-column label="序号" width="70" type="index" :index="(index) => (pagination.page - 1) * pagination.size + index + 1" />
        <el-table-column prop="id" label="订单ID" width="80">
          <template #default="{ row }">
            <span style="font-family: monospace; font-size: 12px;">{{ row.id }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column label="下单时间" width="160">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="userPhone" label="用户电话" width="120" />
        <el-table-column prop="receiverName" label="收货人" width="100" />
        <el-table-column prop="receiverAddress" label="收货地址" min-width="180" show-overflow-tooltip />
        <el-table-column prop="finalAmount" label="订单金额" width="100">
          <template #default="{ row }">
            <span style="color: #ff8c00; font-weight: 600;">¥{{ row.finalAmount || row.totalAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="deliveryFee" label="配送费" width="80">
          <template #default="{ row }">
            ¥{{ row.deliveryFee }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="订单状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="配送状态" width="120" v-if="showDeliveryCol">
          <template #default="{ row }">
            <div v-if="deliveryStatus[row.id]" class="delivery-status">
              <el-tag :type="getDeliveryTagType(deliveryStatus[row.id].phase)" size="small">
                {{ getDeliveryText(deliveryStatus[row.id]) }}
              </el-tag>
              <span v-if="deliveryStatus[row.id].distanceToUser" class="distance-text">
                {{ deliveryStatus[row.id].distanceToUser }}米
              </span>
            </div>
            <span v-else class="no-delivery">-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">
              查看
            </el-button>
        <el-button
          v-if="row.status === 1"
          type="success"
          link
          @click="handleAccept(row)"
        >
        接单
        </el-button>
        <el-button
          v-if="row.status === 1 || row.status === 0"
          type="danger"
          link
          @click="handleCancel(row)"
        >
          取消
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import request from '../utils/request'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const orders = ref([])
const deliveryStatus = reactive({})
const showDeliveryCol = ref(true)
let pollingTimer = null

const queryForm = reactive({
  orderNo: '',
  status: null,
  dateRange: []
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

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

const getDeliveryTagType = (phase) => {
  const map = {
    'going_to_merchant': 'primary',
    'picking_up': 'warning',
    'delivering': 'success'
  }
  return map[phase] || 'info'
}

const getDeliveryText = (info) => {
  if (!info) return '-'
  if (info.status === 2) return '已送达'
  if (info.phase === 'going_to_merchant') return '前往取餐'
  if (info.phase === 'picking_up') return '已交付'
  if (info.phase === 'delivering') return '配送中'
  return '待交付'
}

const formatTime = (time) => {
  return time ? dayjs(time).format('YYYY-MM-DD HH:mm') : '-'
}

const loadOrders = async () => {
  loading.value = true
  try {
    const params = {
      ...queryForm,
      pageNum: pagination.page,
      pageSize: pagination.size
    }
    if (queryForm.dateRange && queryForm.dateRange.length === 2) {
      params.startDate = queryForm.dateRange[0]
      params.endDate = queryForm.dateRange[1]
    }
    delete params.dateRange
    
    const data = await request({
      url: '/merchant/orders',
      method: 'get',
      params
    })
    orders.value = data.records || data.list || []
    pagination.total = data.total || 0

    const deliveryOrders = orders.value.filter(o => o.status === 2 || o.status === 3)
    if (deliveryOrders.length > 0) {
      loadDeliveryStatuses(deliveryOrders.map(o => o.id))
    }
  } catch (error) {
    console.error('加载订单列表失败:', error)
  } finally {
    loading.value = false
  }
}

const loadDeliveryStatuses = async (orderIds) => {
  try {
    for (const orderId of orderIds) {
      const data = await request({
        url: `/delivery/task/${orderId}/location`,
        method: 'get'
      })
      if (data) {
        deliveryStatus[orderId] = data
      }
    }
  } catch (error) {
    console.error('加载配送状态失败:', error)
  }
}

const startPolling = () => {
  pollingTimer = setInterval(() => {
    const activeOrders = orders.value.filter(o => o.status === 2 || o.status === 3)
    if (activeOrders.length > 0) {
      loadDeliveryStatuses(activeOrders.map(o => o.id))
    }
  }, 5000)
}

const stopPolling = () => {
  if (pollingTimer) {
    clearInterval(pollingTimer)
    pollingTimer = null
  }
}

const handleReset = () => {
  queryForm.orderNo = ''
  queryForm.status = null
  queryForm.dateRange = []
  pagination.page = 1
  loadOrders()
}

// 监听分页变化
watch(() => pagination.page, () => {
  loadOrders()
})

watch(() => pagination.size, () => {
  pagination.page = 1
  loadOrders()
})

const handleView = (row) => {
  router.push(`/orders/${row.id}`)
}

const handleAccept = async (row) => {
  console.log('接单订单:', row)
  try {
    await request({
      url: `/merchant/order/accept/${row.id}`,
      method: 'post'
    })
    ElMessage.success('已接单')
    loadOrders()
  } catch (error) {
    console.error('接单失败:', error)
    ElMessage.error('接单失败: ' + (error.message || '未知错误'))
  }
}

const handleComplete = async (row) => {
  try {
    await request({
      url: `/merchant/orders/${row.id}/complete`,
      method: 'post'
    })
    ElMessage.success('订单已完成')
    loadOrders()
  } catch (error) {
    console.error('操作失败:', error)
  }
}

const handleCancel = (row) => {
  ElMessageBox.confirm('确定要取消该订单吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request({
        url: `/merchant/orders/${row.id}/cancel`,
        method: 'post'
      })
      ElMessage.success('订单已取消')
      loadOrders()
    } catch (error) {
      console.error('取消失败:', error)
    }
  }).catch(() => {})
}

onMounted(() => {
  if (route.query.status) {
    queryForm.status = parseInt(route.query.status)
  }
  loadOrders()
  startPolling()
})

onUnmounted(() => {
  stopPolling()
})
</script>

<style scoped>
.order-manage {
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

.delivery-status {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.distance-text {
  font-size: 10px;
  color: #ff8c00;
}

.no-delivery {
  color: #999;
}
</style>
