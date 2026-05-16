<template>
  <div class="delivery-tracking" v-if="deliveryInfo">
    <el-card class="tracking-card">
      <template #header>
        <div class="card-header">
          <span>配送追踪</span>
          <el-tag :type="getStatusTagType(deliveryInfo.status)" size="small">
            {{ getStatusText(deliveryInfo.status) }}
          </el-tag>
        </div>
      </template>

      <div class="tracking-map" v-if="deliveryInfo">
        <div class="map-container">
          <img 
            v-if="mapUrl" 
            :src="mapUrl" 
            alt="配送地图" 
            class="delivery-map"
            @error="mapLoadFailed = true"
          />
          <div v-else-if="mapLoadFailed" class="map-placeholder">
            <el-icon :size="48"><Location /></el-icon>
            <span>地图加载失败</span>
          </div>
          <div v-else class="map-loading">
            <el-icon class="is-loading" :size="32"><Loading /></el-icon>
            <span>加载地图中...</span>
          </div>
          
          <div class="map-legend">
            <div class="legend-item">
              <span class="legend-dot merchant"></span>
              <span>商家</span>
            </div>
            <div class="legend-item">
              <span class="legend-dot rider"></span>
              <span>骑手</span>
            </div>
            <div class="legend-item">
              <span class="legend-dot user"></span>
              <span>用户</span>
            </div>
          </div>
        </div>
      </div>

      <div class="tracking-content">
        <el-steps :active="currentStep" finish-status="success" align-center>
          <el-step title="待接单" :description="formatTime(deliveryInfo.createTime)" />
          <el-step title="已接单" description="商家准备中" />
          <el-step title="已交付" description="骑手已取餐" />
          <el-step title="配送中" :description="getDeliveryDescription()" />
          <el-step title="已送达" :description="deliveryInfo.status === 2 ? formatTime(deliveryInfo.updateTime) : ''" />
        </el-steps>

        <div class="rider-info" v-if="riderLocation">
          <el-divider content-position="left">骑手信息</el-divider>
          <div class="rider-detail">
            <el-avatar :size="48" :icon="User" />
            <div class="rider-text">
              <div class="rider-name">
                {{ riderLocation.riderName || '骑手' }}
              </div>
              <div class="rider-status">
                <el-tag :type="getPhaseTagType(riderLocation.phase)" size="small">
                  {{ riderLocation.description || getPhaseText(riderLocation.phase) }}
                </el-tag>
              </div>
            </div>
            <div class="rider-distance" v-if="riderLocation.distanceToUser && riderLocation.phase === 'delivering'">
              <el-statistic :value="riderLocation.distanceToUser" suffix="米" />
              <span class="distance-label">距用户</span>
            </div>
          </div>
        </div>

        <div class="time-estimate" v-if="deliveryInfo.estimatedTime !== null">
          <el-alert
            :title="`预计送达时间: 约 ${deliveryInfo.estimatedTime} 分钟`"
            type="info"
            :closable="false"
            show-icon
          />
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { User, Location, Loading } from '@element-plus/icons-vue'
import request from '../utils/request'

const props = defineProps({
  orderId: {
    type: Number,
    required: true
  }
})

const deliveryInfo = ref(null)
const riderLocation = ref(null)
const mapUrl = ref('')
const mapLoadFailed = ref(false)
let pollingTimer = null
let wsConnection = null

const currentStep = computed(() => {
  if (!deliveryInfo.value) return 0
  const status = deliveryInfo.value.status
  if (status === 0) return 0
  if (status === 1) return 1
  if (status === 2) {
    if (!riderLocation.value) return 2
    const phase = riderLocation.value.phase
    if (phase === 'going_to_merchant' || phase === 'picking_up') return 2
    if (phase === 'delivering') return 3
  }
  if (status === 2) return 4
  return 0
})

const getStatusText = (status) => {
  const map = {
    0: '待取餐',
    1: '配送中',
    2: '已送达'
  }
  return map[status] || '未知'
}

const getStatusTagType = (status) => {
  const map = {
    0: 'info',
    1: 'warning',
    2: 'success'
  }
  return map[status] || 'info'
}

const getPhaseText = (phase) => {
  const map = {
    'going_to_merchant': '前往商家',
    'picking_up': '正在取餐',
    'delivering': '配送中'
  }
  return map[phase] || '配送中'
}

const getPhaseTagType = (phase) => {
  const map = {
    'going_to_merchant': 'primary',
    'picking_up': 'warning',
    'delivering': 'success'
  }
  return map[phase] || 'info'
}

const getPhaseDescription = (phase) => {
  if (!riderLocation.value) return ''
  if (riderLocation.value.phase === phase) {
    return riderLocation.value.description || getPhaseText(phase)
  }
  return ''
}

const getDeliveryDescription = () => {
  if (!riderLocation.value) return ''
  if (riderLocation.value.phase === 'delivering' && riderLocation.value.distanceToUser) {
    return `距离用户 ${riderLocation.value.distanceToUser} 米`
  }
  return riderLocation.value.description || ''
}

const formatTime = (time) => {
  if (!time) return '-'
  const date = new Date(time)
  return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

const AMAP_KEY = 'f3fee95358618293dd1b023340fca63f'

const loadDeliveryInfo = async () => {
  try {
    const data = await request({
      url: `/delivery/task/${props.orderId}`,
      method: 'get'
    })
    deliveryInfo.value = data
    console.log('配送信息:', data)
    
    // 无论状态如何，都尝试加载骑手位置和地图
    await loadRiderLocation()
  } catch (error) {
    console.error('获取配送信息失败:', error)
  }
}

const loadMapImage = async () => {
  if (!deliveryInfo.value) return

  const task = deliveryInfo.value
  
  // 验证商家和用户坐标
  if (!task.pickupLongitude || !task.pickupLatitude || !task.deliveryLongitude || !task.deliveryLatitude) {
    console.warn('商家或用户坐标无效:', task)
    mapLoadFailed.value = true
    return
  }

  // 如果有骑手位置就使用，否则使用商家位置作为中心
  let centerLng = task.pickupLongitude
  let centerLat = task.pickupLatitude
  
  if (riderLocation.value && riderLocation.value.longitude && riderLocation.value.latitude) {
    centerLng = riderLocation.value.longitude
    centerLat = riderLocation.value.latitude
  }

  // 标记样式 - 商家蓝色, 骑手橙色, 用户绿色
  const markers = [
    `p1,${task.pickupLongitude},${task.pickupLatitude},商家`,
    `p2,${centerLng},${centerLat},骑手`,
    `p3,${task.deliveryLongitude},${task.deliveryLatitude},用户`
  ].join('|')

  const zoom = 14
  // 绘制配送路线
  const path = `${task.pickupLongitude},${task.pickupLatitude};${task.deliveryLongitude},${task.deliveryLatitude}`
  
  console.log('加载地图，markers:', markers, 'path:', path)
  
  // 使用高德静态地图API
  mapUrl.value = `https://restapi.amap.com/v3/staticmap?location=${centerLng},${centerLat}&zoom=${zoom}&size=600*300&markers=${markers}&path=${path}&key=${AMAP_KEY}`
  
  console.log('地图URL:', mapUrl.value)
}

const loadRiderLocation = async () => {
  try {
    const data = await request({
      url: `/delivery/task/${props.orderId}/location`,
      method: 'get'
    })
    riderLocation.value = data
    console.log('骑手位置:', data)
    
    // 无论deliveryInfo.status是什么，都尝试加载地图
    await loadMapImage()
  } catch (error) {
    console.error('获取骑手位置失败:', error)
  }
}

const startPolling = () => {
  pollingTimer = setInterval(() => {
    loadRiderLocation()
  }, 5000)
}

const stopPolling = () => {
  if (pollingTimer) {
    clearInterval(pollingTimer)
    pollingTimer = null
  }
}

onMounted(async () => {
  await loadDeliveryInfo()
  if (deliveryInfo.value && deliveryInfo.value.status === 1) {
    await loadRiderLocation()
    startPolling()
  }
})

onUnmounted(() => {
  stopPolling()
})
</script>

<style scoped>
.delivery-tracking {
  margin-top: 20px;
}

.tracking-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
}

.tracking-card :deep(.el-card__header) {
  background: rgba(255, 255, 255, 0.1);
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
}

.tracking-card :deep(.el-card__body) {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 0 0 8px 8px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: white;
}

.tracking-content {
  padding: 10px 0;
}

.rider-info {
  margin-top: 20px;
}

.rider-detail {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
  margin-top: 12px;
}

.rider-text {
  flex: 1;
}

.rider-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.rider-status {
  margin-top: 4px;
}

.rider-distance {
  text-align: center;
  padding: 8px 16px;
  background: #fff7e6;
  border-radius: 8px;
  border: 1px solid #ffd591;
}

.rider-distance :deep(.el-statistic__content) {
  color: #ff8c00;
  font-size: 24px;
  font-weight: 600;
}

.distance-label {
  font-size: 12px;
  color: #999;
}

.time-estimate {
  margin-top: 20px;
}

.tracking-map {
  margin-bottom: 20px;
}

.map-container {
  position: relative;
  border-radius: 8px;
  overflow: hidden;
  background: #f5f7fa;
}

.delivery-map {
  width: 100%;
  height: 200px;
  object-fit: cover;
}

.map-placeholder,
.map-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: #909399;
  gap: 10px;
}

.map-legend {
  display: flex;
  justify-content: center;
  gap: 20px;
  padding: 10px;
  background: rgba(255, 255, 255, 0.9);
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #606266;
}

.legend-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.legend-dot.merchant {
  background: #409eff;
}

.legend-dot.rider {
  background: #e6a23c;
}

.legend-dot.user {
  background: #67c23a;
}
</style>
