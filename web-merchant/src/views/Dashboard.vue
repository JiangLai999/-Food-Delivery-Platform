<template>
  <div class="merchant-dashboard">
    <!-- 品牌横幅 -->
    <div class="brand-banner">
      <div class="logo-brand">
        <img src="/src/assets/logo.png" alt="秒送餐" class="brand-icon-img" />
        <div class="brand-text">秒送餐商家平台</div>
      </div>
      <div class="brand-content">
        <div class="brand-title">欢迎回来，商家</div>
        <div class="brand-subtitle">全面掌握店铺运营，轻松管理订单</div>
        <div class="brand-tagline">快人一步，美味即达</div>
      </div>
      <div class="brand-decorator">🚀</div>
    </div>
    
    <div class="dashboard-header">
      <div class="header-left">
        <div class="header-title">
          <h2>商家数据看板</h2>
          <p class="header-subtitle">实时掌握店铺经营状况</p>
        </div>
        <el-tag :type="useMockData ? 'warning' : 'success'" @click="toggleMockData" class="mode-tag">
          {{ useMockData ? '🎯 模拟数据' : '✓ 实时数据' }}
        </el-tag>
      </div>
      <div class="header-right">
        <div class="date-selector">
          <el-radio-group v-model="dateRange" size="large" @change="refreshData">
            <el-radio-button :value="7">
              <el-icon><Calendar /></el-icon> 近7天
            </el-radio-button>
            <el-radio-button :value="30">
              <el-icon><Calendar /></el-icon> 近30天
            </el-radio-button>
          </el-radio-group>
        </div>
      </div>
    </div>

    <el-row :gutter="20" class="stat-row">
      <el-col :span="6">
        <div class="stat-card revenue-card">
          <div class="stat-icon-wrapper">
            <el-icon :size="32"><Coin /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-label">今日营收</div>
            <div class="stat-value">¥{{ formatNumber(statistics.todayRevenue) }}</div>
            <div class="stat-trend" :class="statistics.revenueGrowth >= 0 ? 'trend-up' : 'trend-down'">
              <el-icon><TrendCharts v-if="statistics.revenueGrowth >= 0" /><Bottom v-else /></el-icon>
              {{ Math.abs(statistics.revenueGrowth) }}%
            </div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card order-card">
          <div class="stat-icon-wrapper">
            <el-icon :size="32"><Document /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-label">今日订单</div>
            <div class="stat-value">{{ statistics.todayOrders }}</div>
            <div class="stat-trend" :class="statistics.orderGrowth >= 0 ? 'trend-up' : 'trend-down'">
              <el-icon><TrendCharts v-if="statistics.orderGrowth >= 0" /><Bottom v-else /></el-icon>
              {{ Math.abs(statistics.orderGrowth) }}%
            </div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card rating-card">
          <div class="stat-icon-wrapper">
            <el-icon :size="32"><Star /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-label">店铺评分</div>
            <div class="stat-value">{{ statistics.rating.toFixed(1) }}</div>
            <div class="stat-sub">共 {{ statistics.reviewCount }} 条评价</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card sales-card">
          <div class="stat-icon-wrapper">
            <el-icon :size="32"><ShoppingCart /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-label">总销量</div>
            <div class="stat-value">{{ formatNumber(statistics.totalSales) }}</div>
            <div class="stat-sub">本月新增 {{ statistics.newOrders }} 单</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="16">
        <el-card class="chart-card main-chart-card">
          <template #header>
            <div class="card-header">
              <span>营收趋势</span>
              <el-radio-group v-model="chartType" size="small">
                <el-radio-button value="line">折线图</el-radio-button>
                <el-radio-button value="bar">柱状图</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="mainChartRef" style="height: 380px;"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="chart-card todo-card">
          <template #header>
            <div class="card-header">
              <span><el-icon><Bell /></el-icon> 待处理事项</span>
              <el-badge :value="statistics.totalPending" :max="99" class="task-badge" />
            </div>
          </template>
          <div class="todo-list">
            <div class="todo-item pending-orders" @click="router.push('/orders?status=1')">
              <div class="todo-icon">
                <el-icon><Document /></el-icon>
              </div>
              <div class="todo-content">
                <div class="todo-title">待接单</div>
                <div class="todo-count">{{ statistics.pendingOrders }} 单</div>
              </div>
              <el-tag type="warning" size="small" effect="dark">待处理</el-tag>
            </div>
            <div class="todo-item delivering-orders" @click="router.push('/orders?status=3')">
              <div class="todo-icon">
                <el-icon><Van /></el-icon>
              </div>
              <div class="todo-content">
                <div class="todo-title">配送中</div>
                <div class="todo-count">{{ statistics.deliveringOrders }} 单</div>
              </div>
              <el-tag type="primary" size="small" effect="dark">配送中</el-tag>
            </div>
            <div class="todo-item review-orders" @click="router.push('/reviews')">
              <div class="todo-icon">
                <el-icon><ChatDotRound /></el-icon>
              </div>
              <div class="todo-content">
                <div class="todo-title">待回复评价</div>
                <div class="todo-count">{{ statistics.pendingReviews }} 条</div>
              </div>
              <el-tag type="info" size="small" effect="dark">待回复</el-tag>
            </div>
          </div>
          <div class="quick-stats">
            <div class="quick-stat-item">
              <span class="qs-label">今日收入</span>
              <span class="qs-value">¥{{ formatNumber(statistics.todayRevenue) }}</span>
            </div>
            <div class="quick-stat-item">
              <span class="qs-label">今日订单</span>
              <span class="qs-value">{{ statistics.todayOrders }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>餐品售卖分布</span>
              <el-button link type="primary" @click="router.push('/foods')">查看详情</el-button>
            </div>
          </template>
          <div ref="pieChartRef" style="height: 320px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>时段分布</span>
              <el-link type="primary">时段分析</el-link>
            </div>
          </template>
          <div ref="barChartRef" style="height: 320px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="24">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>热门菜品 TOP 10</span>
              <el-button @click="router.push('/foods')" type="primary" link>
                查看全部 <el-icon><ArrowRight /></el-icon>
              </el-button>
            </div>
          </template>
          <el-table :data="topFoods" style="width: 100%" :cell-style="{ padding: '12px 0' }">
            <el-table-column type="index" label="排名" width="70" align="center">
              <template #default="{ $index }">
                <el-tag :type="$index < 3 ? 'success' : 'info'">{{ $index + 1 }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="foodName" label="菜品名称" min-width="150" />
            <el-table-column prop="categoryName" label="分类" width="120" />
            <el-table-column prop="salesVolume" label="销量" width="100" sortable>
              <template #default="{ row }">
                <span class="sale-number">{{ formatNumber(row.salesVolume) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="price" label="价格" width="100">
              <template #default="{ row }">
                <span class="price-tag">¥{{ row.price }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="rating" label="评分" width="100" align="center">
              <template #default="{ row }">
                <el-rate v-model="row.rating" disabled size="small" />
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
                  {{ row.status === 1 ? '✓ 上架' : '○ 下架' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import request from '../utils/request'

const router = useRouter()
const mainChartRef = ref(null)
const pieChartRef = ref(null)
const barChartRef = ref(null)
const useMockData = ref(false)
const dateRange = ref(7)
const chartType = ref('line')

const statistics = ref({
  todayRevenue: 0,
  todayOrders: 0,
  rating: 0,
  reviewCount: 0,
  totalSales: 0,
  newOrders: 0,
  pendingOrders: 0,
  deliveringOrders: 0,
  pendingReviews: 0,
  totalPending: 0,
  revenueGrowth: 0,
  orderGrowth: 0
})

const topFoods = ref([])

const toggleMockData = () => {
  useMockData.value = !useMockData.value
  refreshData()
}

const refreshData = async () => {
  if (useMockData.value) {
    loadMockData()
  } else {
    await loadStatistics()
  }
}

const loadMockData = () => {
  const days = dateRange.value
  const revenueData = Array.from({ length: days }, (_, i) => ({
    date: new Date(Date.now() - (days - 1 - i) * 24 * 60 * 60 * 1000).toLocaleDateString('zh-CN'),
    revenue: Math.floor(Math.random() * 5000) + 2000
  }))
  
  statistics.value = {
    todayRevenue: revenueData[revenueData.length - 1].revenue,
    todayOrders: Math.floor(Math.random() * 100) + 50,
    rating: (Math.random() * 1.5 + 3.5).toFixed(1) * 1,
    reviewCount: Math.floor(Math.random() * 200) + 50,
    totalSales: Math.floor(Math.random() * 10000) + 5000,
    newOrders: Math.floor(Math.random() * 500) + 200,
    pendingOrders: Math.floor(Math.random() * 10),
    deliveringOrders: Math.floor(Math.random() * 20),
    pendingReviews: Math.floor(Math.random() * 5),
    totalPending: Math.floor(Math.random() * 35),
    revenueGrowth: (Math.random() * 30 - 10).toFixed(1),
    orderGrowth: (Math.random() * 30 - 10).toFixed(1)
  }
  
  topFoods.value = Array.from({ length: 10 }, (_, i) => ({
    foodName: ['招牌红烧肉', '宫保鸡丁', '麻婆豆腐', '鱼香肉丝', '水煮鱼', '东坡肉', '回锅肉', '糖醋里脊', '白切鸡', '佛跳墙'][i],
    categoryName: ['川菜', '川菜', '川菜', '川菜', '川菜', '浙菜', '川菜', '鲁菜', '粤菜', '闽菜'][i],
    salesVolume: Math.floor(Math.random() * 500) + 100,
    price: (Math.random() * 50 + 20).toFixed(0),
    rating: (Math.random() * 1 + 4).toFixed(1) * 1,
    status: 1
  }))
  
  nextTick(() => {
    initMainChart(revenueData)
    initPieChart()
    initBarChart()
  })
}

const loadStatistics = async () => {
  try {
    const data = await request({
      url: '/merchant/statistics',
      method: 'get',
      params: { days: dateRange.value }
    })
    statistics.value = {
      todayRevenue: data.todayRevenue || 0,
      todayOrders: data.todayOrders || 0,
      rating: data.rating || 5,
      reviewCount: data.reviewCount || 0,
      totalSales: data.totalSales || 0,
      newOrders: data.newOrders || 0,
      pendingOrders: data.pendingOrders || 0,
      deliveringOrders: data.deliveringOrders || 0,
      pendingReviews: data.pendingReviews || 0,
      totalPending: (data.pendingOrders || 0) + (data.deliveringOrders || 0) + (data.pendingReviews || 0),
      revenueGrowth: data.revenueGrowth || 0,
      orderGrowth: data.orderGrowth || 0
    }
    topFoods.value = data.topFoods || []
    
    await nextTick()
    initMainChart(data.revenueTrend || [])
    // 使用热门餐品数据作为销售分布
    const foodDistribution = {}
    if (data.topFoods && data.topFoods.length > 0) {
      data.topFoods.forEach(food => {
        foodDistribution[food.foodName] = food.salesVolume || 0
      })
    }
    initPieChart(Object.keys(foodDistribution).length > 0 ? foodDistribution : {})
    initBarChart(data.timeDistribution || {})
  } catch (error) {
    console.error('加载统计数据失败:', error)
    useMockData.value = true
    loadMockData()
  }
}

const initMainChart = (trendData) => {
  if (!mainChartRef.value) return
  
  const chart = echarts.init(mainChartRef.value)
  
  const xAxisData = trendData.map(item => item.date)
  const seriesData = trendData.map(item => item.revenue)
  
  const option = {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#eee',
      borderWidth: 1,
      textStyle: { color: '#333' },
      axisPointer: {
        type: 'shadow',
        shadowStyle: { color: 'rgba(255, 140, 0, 0.1)' }
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '5%',
      top: '10%',
      containLabel: true
    },
    legend: {
      data: ['营收'],
      top: 0
    },
    xAxis: {
      type: 'category',
      data: xAxisData,
      boundaryGap: chartType.value === 'bar',
      axisLine: { lineStyle: { color: '#e4e7ed' } },
      axisLabel: { color: '#909399', margin: 15 }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      axisLabel: { color: '#909399' },
      splitLine: {
        lineStyle: {
          color: '#f0f2f5',
          type: 'dashed'
        }
      }
    },
    series: [{
      name: '营收',
      type: chartType.value,
      data: seriesData,
      smooth: true,
      areaStyle: chartType.value === 'line' ? {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(255, 140, 0, 0.3)' },
          { offset: 0.5, color: 'rgba(255, 140, 0, 0.15)' },
          { offset: 1, color: 'rgba(255, 140, 0, 0.05)' }
        ])
      } : null,
      itemStyle: {
        color: '#FF8C00',
        borderRadius: chartType.value === 'bar' ? [4, 4, 0, 0] : 0
      },
      lineStyle: {
        color: '#FF8C00',
        width: 3,
        shadowColor: 'rgba(255, 140, 0, 0.3)',
        shadowBlur: 10
      },
      markPoint: {
        data: [
          { type: 'max', name: '最高' },
          { type: 'min', name: '最低' }
        ],
        itemStyle: { color: '#FF4D4F' }
      },
      markLine: {
        data: [{ type: 'average', name: '平均值' }],
        lineStyle: { color: '#409EFF', type: 'dashed' }
      },
      animationDuration: 1500,
      animationEasing: 'quadraticOut'
    }]
  }
  
  chart.setOption(option)
}

const initPieChart = (distribution) => {
  if (!pieChartRef.value) return
  
  // 餐品售卖分布（按餐品名称）- 使用真实数据
  const dist = distribution && Object.keys(distribution).length > 0 ? distribution : {
    '暂无餐品数据': 1
  }
  
  const chart = echarts.init(pieChartRef.value)
  
  const colors = ['#FF8C00', '#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399', '#409EFF', '#67C23A', '#E6A23C', '#F56C6C']
  
  const option = {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} 单 ({d}%)',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#eee',
      borderWidth: 1
    },
    legend: {
      orient: 'vertical',
      right: 10,
      top: 'center',
      textStyle: { color: '#606266' }
    },
    series: [{
      name: '餐品名称',
      type: 'pie',
      radius: ['30%', '70%'],
      center: ['35%', '50%'],
      avoidLabelOverlap: true,
      itemStyle: {
        borderRadius: 8,
        borderColor: '#fff',
        borderWidth: 2,
        color: (params) => colors[params.dataIndex % colors.length]
      },
      label: {
        show: false
      },
      emphasis: {
        label: {
          show: true,
          fontSize: 14,
          fontWeight: 'bold'
        },
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.2)'
        }
      },
      labelLine: { show: false },
      data: Object.keys(dist).map(key => ({
        name: key,
        value: dist[key]
      }))
    }]
  }
  
  chart.setOption(option)
}

const initBarChart = (timeData) => {
  if (!barChartRef.value) return
  
  // 如果没有数据，使用模拟数据
  const time = Object.keys(timeData).length > 0 ? timeData : {
    '9:00': 12,
    '10:00': 25,
    '11:00': 38,
    '12:00': 52,
    '13:00': 45,
    '14:00': 30,
    '15:00': 28,
    '16:00': 22,
    '17:00': 35,
    '18:00': 41,
    '19:00': 18,
    '20:00': 10
  }
  
  const chart = echarts.init(barChartRef.value)
  
  const option = {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: Object.keys(time).slice(0, 12).map(h => h + ':00'),
      axisLine: { lineStyle: { color: '#e4e7ed' } },
      axisLabel: { color: '#909399' }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      axisLabel: { color: '#909399' },
      splitLine: {
        lineStyle: { color: '#f0f2f5' }
      }
    },
    series: [{
      name: '订单数',
      type: 'bar',
      data: Object.values(time).slice(0, 12),
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#FF8C00' },
          { offset: 1, color: '#FFA033' }
        ]),
        borderRadius: [4, 4, 0, 0]
      },
      emphasis: {
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#FF6600' },
            { offset: 1, color: '#FF8C00' }
          ])
        }
      },
      animationDelay: (idx) => idx * 50
    }]
  }
  
  chart.setOption(option)
}

const formatNumber = (num) => {
  return num >= 10000 ? (num / 10000).toFixed(1) + '万' : num.toLocaleString()
}

watch([chartType, dateRange], () => {
  refreshData()
})

onMounted(() => {
  loadStatistics()
  
  window.addEventListener('resize', () => {
    mainChartRef.value && echarts.getInstanceByDom(mainChartRef.value)?.resize()
    pieChartRef.value && echarts.getInstanceByDom(pieChartRef.value)?.resize()
    barChartRef.value && echarts.getInstanceByDom(barChartRef.value)?.resize()
  })
})
</script>

<style scoped>
.merchant-dashboard {
  min-height: 100%;
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%);
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 20px 24px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.header-title h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  color: #303133;
  background: linear-gradient(135deg, #FF8C00, #FFA500);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.header-subtitle {
  margin: 4px 0 0;
  font-size: 13px;
  color: #909399;
}

.mode-tag {
  cursor: pointer;
  transition: all 0.3s;
}

.mode-tag:hover {
  transform: scale(1.05);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.date-selector :deep(.el-radio-button__inner) {
  display: flex;
  align-items: center;
  gap: 4px;
}

.stat-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  display: flex;
  align-items: flex-start;
  gap: 20px;
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  border: 1px solid #f0f2f5;
}

.stat-card::before {
  content: '';
  position: absolute;
  width: 120px;
  height: 120px;
  border-radius: 50%;
  opacity: 0.08;
  right: -40px;
  bottom: -40px;
  transition: all 0.5s;
}

.stat-card:hover::before {
  transform: scale(1.2);
}

.revenue-card::before { background: #FF8C00; }
.order-card::before { background: #409EFF; }
.rating-card::before { background: #67C23A; }
.sales-card::before { background: #E6A23C; }

.stat-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.15);
}

.stat-icon-wrapper {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 28px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.revenue-card .stat-icon-wrapper { background: linear-gradient(135deg, #FF8C00, #FFA500); }
.order-card .stat-icon-wrapper { background: linear-gradient(135deg, #409EFF, #40A9FF); }
.rating-card .stat-icon-wrapper { background: linear-gradient(135deg, #67C23A, #85CE61); }
.sales-card .stat-icon-wrapper { background: linear-gradient(135deg, #E6A23C, #F0C78A); }

.stat-content {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
  font-weight: 500;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #303133;
  line-height: 1;
  margin-bottom: 8px;
}

.stat-trend {
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  border-radius: 20px;
  width: fit-content;
}

.trend-up { 
  color: #67C23A; 
  background: rgba(103, 194, 58, 0.1);
}
.trend-down { 
  color: #F56C6C; 
  background: rgba(245, 108, 108, 0.1);
}

.stat-sub {
  font-size: 12px;
  color: #C0C4CC;
  padding: 4px 10px;
  background: #f5f7fa;
  border-radius: 12px;
  display: inline-block;
}

.chart-card {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  border-radius: 16px;
  border: none;
  overflow: hidden;
}

.main-chart-card {
  background: white;
}

.todo-card {
  background: white;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  font-size: 16px;
  color: #303133;
}

.card-header span {
  display: flex;
  align-items: center;
  gap: 8px;
}

.task-badge {
  margin-left: 8px;
}

.todo-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 10px 0;
}

.todo-item {
  display: flex;
  align-items: center;
  padding: 16px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid transparent;
  background: #fafafa;
}

.todo-item:hover {
  background: linear-gradient(135deg, #f5f7fa 0%, #fff 100%);
  border-color: #e4e7ed;
  transform: translateX(4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.todo-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 22px;
  margin-right: 16px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.pending-orders .todo-icon { background: linear-gradient(135deg, #FF8C00, #FFA500); }
.delivering-orders .todo-icon { background: linear-gradient(135deg, #409EFF, #67C23A); }
.review-orders .todo-icon { background: linear-gradient(135deg, #E6A23C, #F56C6C); }

.todo-content {
  flex: 1;
}

.todo-title {
  font-size: 14px;
  color: #606266;
  margin-bottom: 4px;
}

.todo-count {
  font-size: 20px;
  font-weight: 700;
  color: #303133;
}

.quick-stats {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px dashed #e4e7ed;
  display: flex;
  gap: 16px;
}

.quick-stat-item {
  flex: 1;
  text-align: center;
  padding: 12px;
  background: linear-gradient(135deg, #f5f7fa 0%, #fff 100%);
  border-radius: 12px;
}

.qs-label {
  display: block;
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.qs-value {
  font-size: 18px;
  font-weight: 700;
  color: #FF8C00;
}

.sale-number {
  font-weight: 600;
  color: #FF8C00;
}

.price-tag {
  color: #F56C6C;
  font-weight: 600;
}

/* 品牌横幅 */
.brand-banner {
  background: linear-gradient(135deg, #2D3748 0%, #1A202C 100%);
  padding: 36px 40px;
  border-radius: 20px;
  margin-bottom: 24px;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
  position: relative;
  overflow: hidden;
  border: 1px solid rgba(255, 140, 0, 0.1);
}

.brand-banner::before {
  content: '';
  position: absolute;
  top: -100px;
  right: -50px;
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, rgba(255, 140, 0, 0.08) 0%, transparent 70%);
  border-radius: 50%;
}

.brand-banner::after {
  content: '';
  position: absolute;
  bottom: -50px;
  left: 100px;
  width: 200px;
  height: 200px;
  background: radial-gradient(circle, rgba(255, 140, 0, 0.05) 0%, transparent 70%);
  border-radius: 50%;
}

.logo-brand {
  display: flex;
  align-items: center;
  gap: 20px;
  z-index: 1;
  position: relative;
}

.brand-icon-img {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(255, 140, 0, 0.4);
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-5px);
  }
}

.brand-text {
  font-size: 26px;
  font-weight: 700;
  color: #FF8C00;
  letter-spacing: 1px;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.brand-content {
  z-index: 1;
  position: relative;
  margin-left: 24px;
  flex: 1;
}

.brand-title {
  font-size: 22px;
  font-weight: 700;
  color: #FFFFFF;
  margin-bottom: 8px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.brand-subtitle {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.75);
  font-weight: 500;
  margin-bottom: 4px;
}

.brand-tagline {
  font-size: 13px;
  color: #FF8C00;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 16px;
  background: rgba(255, 140, 0, 0.1);
  border: 1px solid rgba(255, 140, 0, 0.3);
  border-radius: 20px;
}

.brand-decorator {
  position: absolute;
  top: 20px;
  right: 30px;
  font-size: 48px;
  opacity: 0.3;
  z-index: 1;
}
</style>
