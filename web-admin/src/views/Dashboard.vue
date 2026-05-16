<template>
  <div class="admin-dashboard">
    <!-- 品牌横幅 -->
    <div class="brand-banner">
      <div class="logo-brand">
        <img src="/src/assets/logo.png" alt="秒送餐" class="brand-icon-img" />
        <div class="brand-text">秒送餐管理后台</div>
      </div>
      <div class="brand-content">
        <div class="brand-title">欢迎回来，管理员</div>
        <div class="brand-subtitle">全面掌控平台运营，数据驱动决策</div>
        <div class="brand-tagline">快人一步，美味即达 · 专业的管理平台</div>
      </div>
      <div class="brand-decorator">💼</div>
    </div>

    <div class="dashboard-header">
      <div class="header-left">
        <div class="header-title">
          <h2><el-icon><DataAnalysis /></el-icon> 数据概览</h2>
          <p class="header-subtitle">实时掌握平台运营状况</p>
        </div>
        <el-tag :type="useMockData ? 'warning' : 'success'" @click="useMockData = !useMockData; refreshData()" class="mode-tag">
          {{ useMockData ? '🎯 模拟数据' : '✓ 实时数据' }}
        </el-tag>
      </div>
      <div class="header-right">
        <div class="date-selector">
          <el-radio-group v-model="chartPeriod" size="large" @change="refreshData">
            <el-radio-button :value="7"><el-icon><Calendar /></el-icon> 近7天</el-radio-button>
            <el-radio-button :value="30"><el-icon><Calendar /></el-icon> 近30天</el-radio-button>
          </el-radio-group>
        </div>
        <el-button type="primary" :icon="Refresh" @click="refreshData" :loading="loading">
          刷新数据
        </el-button>
      </div>
    </div>

    <el-row :gutter="24" class="stat-row">
      <el-col :xs="12" :sm="12" :md="6">
        <div class="stat-card order-card">
          <div class="stat-bg-icon"><el-icon><ShoppingCart /></el-icon></div>
          <div class="stat-content">
            <div class="stat-icon-wrapper"><el-icon :size="24"><ShoppingCart /></el-icon></div>
            <div class="stat-info">
              <div class="stat-label">今日订单</div>
              <div class="stat-value">{{ formatNumber(statistics.todayOrderCount) }}</div>
              <div class="stat-trend" :class="orderTrend >= 0 ? 'up' : 'down'">
                <el-icon><TrendCharts v-if="orderTrend >= 0" /><Bottom v-else /></el-icon>
                较昨日 {{ Math.abs(orderTrend) }}%
              </div>
            </div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6">
        <div class="stat-card sales-card">
          <div class="stat-bg-icon"><el-icon><Money /></el-icon></div>
          <div class="stat-content">
            <div class="stat-icon-wrapper"><el-icon :size="24"><Money /></el-icon></div>
            <div class="stat-info">
              <div class="stat-label">今日销售额</div>
              <div class="stat-value">¥{{ formatNumber(statistics.todaySales) }}</div>
              <div class="stat-trend" :class="salesTrend >= 0 ? 'up' : 'down'">
                <el-icon><TrendCharts v-if="salesTrend >= 0" /><Bottom v-else /></el-icon>
                较昨日 {{ Math.abs(salesTrend) }}%
              </div>
            </div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6">
        <div class="stat-card user-card">
          <div class="stat-bg-icon"><el-icon><User /></el-icon></div>
          <div class="stat-content">
            <div class="stat-icon-wrapper"><el-icon :size="24"><User /></el-icon></div>
            <div class="stat-info">
              <div class="stat-label">用户总数</div>
              <div class="stat-value">{{ formatNumber(statistics.totalUserCount) }}</div>
              <div class="stat-trend up">
                <el-icon><TrendCharts /></el-icon>增长 {{ userGrowthRate }}%
              </div>
            </div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6">
        <div class="stat-card merchant-card">
          <div class="stat-bg-icon"><el-icon><Shop /></el-icon></div>
          <div class="stat-content">
            <div class="stat-icon-wrapper"><el-icon :size="24"><Shop /></el-icon></div>
            <div class="stat-info">
              <div class="stat-label">活跃商家</div>
              <div class="stat-value">{{ formatNumber(statistics.activeMerchantCount) }}</div>
              <div class="stat-trend up">
                <el-icon><TrendCharts /></el-icon>增长 {{ merchantGrowthRate }}%
              </div>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="24" style="margin-top: 24px;">
      <el-col :xs="24" :lg="16">
        <el-card class="chart-card main-chart">
          <template #header>
            <div class="chart-header">
              <span class="chart-title"><el-icon><TrendCharts /></el-icon> 订单与销售额趋势</span>
              <div class="chart-actions">
                <el-radio-group v-model="chartPeriod" size="small" @change="refreshData">
                  <el-radio-button :value="7">7天</el-radio-button>
                  <el-radio-button :value="30">30天</el-radio-button>
                </el-radio-group>
              </div>
            </div>
          </template>
          <div ref="trendChartRef" style="height: 380px"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="8">
        <el-card class="chart-card">
          <template #header>
            <div class="chart-header">
              <span class="chart-title"><el-icon><PieChart /></el-icon> 订单状态分布</span>
            </div>
          </template>
          <div ref="orderPieRef" style="height: 380px"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="24" style="margin-top: 24px;">
      <el-col :xs="24" :sm="24" :md="12">
        <el-card class="chart-card">
          <template #header>
            <div class="chart-header">
              <span class="chart-title"><el-icon><PieChart /></el-icon> 商家分类占比</span>
            </div>
          </template>
          <div ref="categoryPieRef" style="height: 320px"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="24" :md="12">
        <el-card class="chart-card">
          <template #header>
            <div class="chart-header">
              <span class="chart-title"><el-icon><TrendCharts /></el-icon> 用户增长趋势</span>
            </div>
          </template>
          <div ref="userChartRef" style="height: 320px"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="24" style="margin-top: 24px;">
      <el-col :xs="24" :sm="24" :md="12">
        <el-card class="chart-card">
          <template #header>
            <div class="chart-header">
              <span class="chart-title"><el-icon><Top /></el-icon> 热销商品TOP10</span>
            </div>
          </template>
          <div ref="productBarRef" style="height: 320px"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="24" :md="12">
        <el-card class="chart-card">
          <template #header>
            <div class="chart-header">
              <span class="chart-title"><el-icon><Location /></el-icon> 区域订单分布</span>
            </div>
          </template>
          <div ref="regionBarRef" style="height: 320px"></div>
        </el-card>
      </el-col>
    </el-row>

    <FoodImageManager v-if="showFoodImageManager" @close="showFoodImageManager = false" />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import * as echarts from 'echarts'
import { getPlatformStatistics, getOrderTrend, getOrderStatusDistribution, getMerchantCategory, getUserGrowth, getTopFoods, getRegionDistribution } from '../api/statistics'
import { ShoppingCart, Money, User, Shop, Refresh, Top, Bottom, TrendCharts, DataAnalysis, Calendar, PieChart, Location } from '@element-plus/icons-vue'
import FoodImageManager from './FoodImageManager.vue'

const chartPeriod = ref(7)
const useMockData = ref(false)
const loading = ref(false)
const showFoodImageManager = ref(false)
const updateTime = ref(new Date().toLocaleString('zh-CN'))

const statistics = ref({
  todayOrderCount: 0,
  todaySales: 0,
  totalUserCount: 0,
  activeMerchantCount: 0
})

const orderTrend = ref(12.5)
const salesTrend = ref(8.3)
const userGrowthRate = ref(15.2)
const merchantGrowthRate = ref(5.8)

const trendChartRef = ref(null)
const orderPieRef = ref(null)
const categoryPieRef = ref(null)
const userChartRef = ref(null)
const productBarRef = ref(null)
const regionBarRef = ref(null)

let charts = {}
let refreshInterval = null

const formatNumber = (num) => {
  if (!num && num !== 0) return '0'
  return new Intl.NumberFormat('zh-CN').format(num)
}

const refreshData = async () => {
  loading.value = true
  try {
    updateTime.value = new Date().toLocaleString('zh-CN')
    
    if (useMockData.value) {
      loadMockData()
    } else {
      await loadRealData()
    }
    await renderAllCharts()
  } catch (error) {
    console.error('加载数据失败:', error)
    loadMockData()
    await renderAllCharts()
  } finally {
    loading.value = false
  }
}

const loadMockData = () => {
  statistics.value = {
    todayOrderCount: Math.floor(Math.random() * 500) + 200,
    todaySales: Math.floor(Math.random() * 50000) + 20000,
    totalUserCount: Math.floor(Math.random() * 50000) + 10000,
    activeMerchantCount: Math.floor(Math.random() * 500) + 200
  }
  orderTrend.value = Math.floor(Math.random() * 30) - 10
  salesTrend.value = Math.floor(Math.random() * 40) - 15
  userGrowthRate.value = 12 + Math.floor(Math.random() * 10)
  merchantGrowthRate.value = 3 + Math.floor(Math.random() * 8)
}

const loadRealData = async () => {
  try {
    const res = await getPlatformStatistics()
    if (res) {
      statistics.value = {
        todayOrderCount: res.todayOrderCount || 0,
        todaySales: res.todaySales || 0,
        totalUserCount: res.totalUserCount || 0,
        activeMerchantCount: res.activeMerchantCount || 0
      }
    }
  } catch (e) {
    console.error('获取统计数据失败:', e)
  }
}

const renderAllCharts = async () => {
  const dates = getDates(chartPeriod.value)
  
  let orderData = []
  let salesData = []
  let orderStatusData = {}
  let categoryData = []
  let userData = []
  let userDates = []
  let productData = []
  let regionData = []

  if (useMockData.value) {
    orderData = Array.from({ length: chartPeriod.value }, () => Math.floor(Math.random() * 500) + 100)
    salesData = Array.from({ length: chartPeriod.value }, () => Math.floor(Math.random() * 50000) + 10000)
    orderStatusData = { '已完成': 335, '配送中': 310, '待发货': 234, '待付款': 135, '已取消': 48 }
    categoryData = [
      { name: '中餐', value: 35 }, { name: '西餐', value: 20 }, { name: '快餐', value: 25 },
      { name: '饮品', value: 10 }, { name: '甜品', value: 5 }, { name: '日韩料理', value: 5 }
    ]
    userData = Array.from({ length: chartPeriod.value }, (_, i) => 10000 + i * 50 + Math.floor(Math.random() * 100))
    productData = [
      { name: '宫保鸡丁饭', value: 1200 }, { name: '麻辣烫', value: 980 }, { name: '汉堡套餐', value: 850 },
      { name: '披萨', value: 720 }, { name: '寿司拼盘', value: 650 }, { name: '牛肉面', value: 580 },
      { name: '蛋糕', value: 520 }, { name: '奶茶', value: 480 }, { name: '炸鸡', value: 420 }, { name: '炒饭', value: 380 }
    ]
    regionData = [
      { name: '朝阳区', value: 2850 }, { name: '海淀区', value: 2420 }, { name: '西城区', value: 1860 },
      { name: '东城区', value: 1650 }, { name: '丰台区', value: 1280 }, { name: '石景山区', value: 920 },
      { name: '通州区', value: 780 }, { name: '大兴区', value: 540 }
    ]
  } else {
    try {
      const [trendRes, statusRes, categoryRes, userRes, productRes, regionRes] = await Promise.all([
        getOrderTrend(chartPeriod.value),
        getOrderStatusDistribution(),
        getMerchantCategory(),
        getUserGrowth(chartPeriod.value),
        getTopFoods(10),
        getRegionDistribution()
      ])
      
      if (trendRes) {
        orderData = trendRes.orderCounts || []
        salesData = trendRes.salesAmounts || []
      }
      if (statusRes) {
        orderStatusData = statusRes
      }
      if (categoryRes) {
        categoryData = categoryRes
      }
      if (userRes) {
        userData = userRes.yaxisData || userRes.yAxisData || userRes.values || []
        userDates = userRes.xaxisData || userRes.xAxisData || []
      }
      if (productRes) {
        productData = (productRes || []).map(item => ({ name: item.foodName, value: item.salesVolume }))
      }
      if (regionRes) {
        regionData = regionRes
      }
    } catch (e) {
      console.error('获取图表数据失败:', e)
      loadMockData()
      orderData = Array.from({ length: chartPeriod.value }, () => Math.floor(Math.random() * 500) + 100)
      salesData = Array.from({ length: chartPeriod.value }, () => Math.floor(Math.random() * 50000) + 10000)
      userData = Array.from({ length: chartPeriod.value }, (_, i) => 10000 + i * 50 + Math.floor(Math.random() * 100))
    }
  }
  
  renderTrendChart(dates, orderData, salesData)
  renderOrderPieChart(orderStatusData)
  renderCategoryPieChart(categoryData)
  renderUserChart(userDates.length > 0 ? userDates : dates, userData)
  renderProductBarChart(productData)
  renderRegionBarChart(regionData)
}

const getDates = (days) => {
  const arr = []
  for (let i = days - 1; i >= 0; i--) {
    const d = new Date()
    d.setDate(d.getDate() - i)
    arr.push(`${d.getMonth() + 1}/${d.getDate()}`)
  }
  return arr
}

const renderTrendChart = (dates, orderData, salesData) => {
  if (!trendChartRef.value) return
  
  // 销毁旧实例
  if (charts.trend) {
    charts.trend.dispose()
  }
  
  const chart = echarts.init(trendChartRef.value)
  charts.trend = chart
  
  chart.setOption({
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255,255,255,0.95)',
      borderColor: '#E4E7ED',
      borderWidth: 1,
      textStyle: { color: '#303133' },
      axisPointer: { type: 'cross', crossStyle: { color: '#409EFF' } }
    },
    legend: {
      data: ['订单量', '销售额'],
      textStyle: { color: '#606266' },
      top: 5
    },
    grid: { left: '3%', right: '4%', bottom: '3%', top: '15%', containLabel: true },
    xAxis: {
      type: 'category',
      data: dates,
      axisLine: { lineStyle: { color: '#DCDFE6' } },
      axisLabel: { color: '#606266' }
    },
    yAxis: [
      {
        type: 'value',
        name: '订单',
        position: 'left',
        axisLine: { show: false },
        axisLabel: { color: '#606266' },
        splitLine: { lineStyle: { color: '#F0F2F5', type: 'dashed' } }
      },
      {
        type: 'value',
        name: '销售额(元)',
        position: 'right',
        axisLine: { show: false },
        axisLabel: { color: '#606266', formatter: (val) => `¥${val / 10000}万` },
        splitLine: { show: false }
      }
    ],
    series: [
      {
        name: '订单量',
        type: 'line',
        data: orderData,
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        lineStyle: { color: '#409EFF', width: 3 },
        itemStyle: { color: '#409EFF' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0.05)' }
          ])
        },
        markPoint: {
          data: [
            { type: 'max', name: '最高', itemStyle: { color: '#409EFF' } },
            { type: 'min', name: '最低', itemStyle: { color: '#409EFF' } }
          ]
        }
      },
      {
        name: '销售额',
        type: 'line',
        yAxisIndex: 1,
        data: salesData,
        smooth: true,
        symbol: 'diamond',
        symbolSize: 10,
        lineStyle: { color: '#67C23A', width: 3 },
        itemStyle: { color: '#67C23A' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(103, 194, 58, 0.3)' },
            { offset: 1, color: 'rgba(103, 194, 58, 0.05)' }
          ])
        },
        markPoint: {
          data: [
            { type: 'max', name: '最高', itemStyle: { color: '#67C23A' } },
            { type: 'min', name: '最低', itemStyle: { color: '#67C23A' } }
          ]
        }
      }
    ]
  })
}

const renderOrderPieChart = (data) => {
  if (!orderPieRef.value) return
  
  // 销毁旧实例
  if (charts.orderPie) {
    charts.orderPie.dispose()
  }
  
  const chart = echarts.init(orderPieRef.value)
  charts.orderPie = chart
  
  const statusColors = { '已完成': '#67C23A', '配送中': '#409EFF', '待发货': '#E6A23C', '待付款': '#F56C6C', '已取消': '#909399' }
  const chartData = Object.entries(data).map(([name, value]) => ({
    value,
    name,
    itemStyle: { color: statusColors[name] || '#409EFF' }
  }))
  
  chart.setOption({
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)',
      backgroundColor: 'rgba(255,255,255,0.95)',
      borderColor: '#E4E7ED',
      textStyle: { color: '#303133' }
    },
    legend: {
      orient: 'vertical',
      right: '5%',
      top: 'center',
      textStyle: { color: '#606266' }
    },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['35%', '50%'],
      avoidLabelOverlap: true,
      itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
      label: { show: true, formatter: '{d}%', color: '#606266' },
      emphasis: {
        label: { show: true, fontSize: 14, fontWeight: 'bold' },
        itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0, 0, 0, 0.5)' }
      },
      data: chartData
    }]
  })
}

const renderCategoryPieChart = (data) => {
  if (!categoryPieRef.value) return
  
  // 销毁旧实例
  if (charts.categoryPie) {
    charts.categoryPie.dispose()
  }
  
  const chart = echarts.init(categoryPieRef.value)
  charts.categoryPie = chart
  
  const colors = ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399', '#B37FEB', '#36cfc9', '#ff85c0']
  const chartData = data.map((item, i) => ({
    ...item,
    itemStyle: { color: colors[i % colors.length] }
  }))
  
  chart.setOption({
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)',
      backgroundColor: 'rgba(255,255,255,0.95)',
      borderColor: '#E4E7ED'
    },
    legend: {
      orient: 'horizontal',
      bottom: '5%',
      textStyle: { color: '#606266' }
    },
    series: [{
      type: 'pie',
      radius: '65%',
      center: ['50%', '45%'],
      roseType: 'radius',
      itemStyle: { borderRadius: 5, borderColor: '#fff', borderWidth: 2 },
      label: { formatter: '{b}\n{d}%', color: '#606266' },
      data: chartData
    }]
  })
}

const renderUserChart = (dates, userData) => {
  if (!userChartRef.value) return
  
  // 销毁旧实例
  if (charts.user) {
    charts.user.dispose()
  }
  
  const chart = echarts.init(userChartRef.value)
  charts.user = chart
  
  chart.setOption({
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255,255,255,0.95)',
      borderColor: '#E4E7ED'
    },
    grid: { left: '3%', right: '4%', bottom: '3%', top: '10%', containLabel: true },
    xAxis: {
      type: 'category',
      data: dates,
      axisLine: { lineStyle: { color: '#DCDFE6' } },
      axisLabel: { color: '#606266' }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      axisLabel: { color: '#606266', formatter: (val) => `${val / 10000}万` },
      splitLine: { lineStyle: { color: '#F0F2F5', type: 'dashed' } }
    },
    series: [{
      type: 'bar',
      data: userData,
      smooth: true,
      barWidth: '60%',
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#36cfc9' },
          { offset: 1, color: '#13c2c2' }
        ]),
        borderRadius: [4, 4, 0, 0]
      },
      emphasis: {
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#36cfc9' },
            { offset: 1, color: '#13c2c2' }
          ])
        }
      }
    }]
  })
}

const renderProductBarChart = (data) => {
  if (!productBarRef.value) return
  
  // 销毁旧实例
  if (charts.product) {
    charts.product.dispose()
  }
  
  const chart = echarts.init(productBarRef.value)
  charts.product = chart
  
  const sortedData = [...data].sort((a, b) => b.value - a.value)
  const products = sortedData.map(item => item.name)
  const sales = sortedData.map(item => item.value)
  
  chart.setOption({
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      backgroundColor: 'rgba(255,255,255,0.95)',
      borderColor: '#E4E7ED'
    },
    grid: { left: '3%', right: '8%', bottom: '3%', top: '5%', containLabel: true },
    xAxis: {
      type: 'value',
      axisLine: { show: false },
      axisLabel: { color: '#606266' },
      splitLine: { lineStyle: { color: '#F0F2F5', type: 'dashed' } }
    },
    yAxis: {
      type: 'category',
      data: products,
      axisLine: { lineStyle: { color: '#DCDFE6' } },
      axisLabel: { color: '#606266', fontSize: 12 }
    },
    series: [{
      type: 'bar',
      data: sales,
      barWidth: '60%',
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
          { offset: 0, color: '#B37FEB' },
          { offset: 1, color: '#9254de' }
        ]),
        borderRadius: [0, 4, 4, 0]
      },
      label: {
        show: true,
        position: 'right',
        formatter: '{c}',
        color: '#606266',
        fontSize: 11
      }
    }]
  })
}

const renderRegionBarChart = (data) => {
  if (!regionBarRef.value) return
  
  // 销毁旧实例
  if (charts.region) {
    charts.region.dispose()
  }
  
  const chart = echarts.init(regionBarRef.value)
  charts.region = chart
  
  const regions = data.map(item => item.name)
  const orders = data.map(item => item.value)
  
  chart.setOption({
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      backgroundColor: 'rgba(255,255,255,0.95)',
      borderColor: '#E4E7ED'
    },
    grid: { left: '3%', right: '4%', bottom: '3%', top: '10%', containLabel: true },
    xAxis: {
      type: 'category',
      data: regions,
      axisLine: { lineStyle: { color: '#DCDFE6' } },
      axisLabel: { color: '#606266', rotate: 30, fontSize: 11 }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      axisLabel: { color: '#606266' },
      splitLine: { lineStyle: { color: '#F0F2F5', type: 'dashed' } }
    },
    series: [{
      type: 'bar',
      data: orders,
      barWidth: '50%',
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#ff85c0' },
          { offset: 1, color: '#eb2f96' }
        ]),
        borderRadius: [4, 4, 0, 0]
      },
      markPoint: {
        data: [
          { type: 'max', name: '最高', itemStyle: { color: '#eb2f96' } }
        ]
      }
    }]
  })
}

const handleResize = () => {
  Object.values(charts).forEach(chart => chart && chart.resize())
}

watch(chartPeriod, () => {
  refreshData()
})

onMounted(() => {
  refreshData()
  refreshInterval = setInterval(refreshData, 60000)
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  Object.values(charts).forEach(chart => chart && chart.dispose())
  if (refreshInterval) clearInterval(refreshInterval)
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.admin-dashboard {
  padding: 24px;
  min-height: 100vh;
  position: relative;
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
  border: 1px solid rgba(245, 158, 11, 0.1);
}

.brand-banner::before {
  content: '';
  position: absolute;
  top: -100px;
  right: -50px;
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, rgba(245, 158, 11, 0.08) 0%, transparent 70%);
  border-radius: 50%;
}

.brand-banner::after {
  content: '';
  position: absolute;
  bottom: -50px;
  left: 100px;
  width: 200px;
  height: 200px;
  background: radial-gradient(circle, rgba(245, 158, 11, 0.05) 0%, transparent 70%);
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
  box-shadow: 0 8px 32px rgba(245, 158, 11, 0.4);
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
  color: #F59E0B;
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
  color: #F59E0B;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 16px;
  background: rgba(245, 158, 11, 0.1);
  border: 1px solid rgba(245, 158, 11, 0.3);
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

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 28px;
  padding: 24px 28px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.08);
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
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-title h2 .el-icon {
  color: #409EFF;
}

.header-subtitle {
  margin: 4px 0 0;
  font-size: 13px;
  color: #909399;
}

.mode-tag {
  cursor: pointer;
  transition: all 0.3s;
  padding: 8px 16px;
  border-radius: 20px;
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

.stat-row {
  margin-bottom: 8px;
}

.stat-card {
  border-radius: 16px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  border: none;
  background: white;
  position: relative;
  overflow: hidden;
  height: 100%;
  min-height: 140px;
}

.stat-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.15);
}

.stat-bg-icon {
  position: absolute;
  right: -10px;
  bottom: -10px;
  font-size: 80px;
  opacity: 0.1;
  color: #409EFF;
}

.order-card .stat-bg-icon { color: #667eea; }
.sales-card .stat-bg-icon { color: #f5576c; }
.user-card .stat-bg-icon { color: #4facfe; }
.merchant-card .stat-bg-icon { color: #43e97b; }

.stat-content {
  display: flex;
  align-items: center;
  gap: 20px;
  height: 100%;
  padding: 20px;
  position: relative;
  z-index: 1;
}

.stat-icon-wrapper {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.order-card .stat-icon-wrapper { background: linear-gradient(135deg, #667eea, #764ba2); }
.sales-card .stat-icon-wrapper { background: linear-gradient(135deg, #f093fb, #f5576c); }
.user-card .stat-icon-wrapper { background: linear-gradient(135deg, #4facfe, #00f2fe); }
.merchant-card .stat-icon-wrapper { background: linear-gradient(135deg, #43e97b, #38f9d7); }

.stat-info {
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
  line-height: 1.2;
  margin-bottom: 8px;
}

.stat-trend {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  padding: 4px 12px;
  border-radius: 20px;
  width: fit-content;
}

.stat-trend.up {
  color: #67C23A;
  background: rgba(103, 194, 58, 0.1);
}

.stat-trend.down {
  color: #F56C6C;
  background: rgba(245, 108, 108, 0.1);
}

.chart-card {
  border-radius: 16px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  border: none;
  background: white;
}

.main-chart {
  min-height: 440px;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 4px 0;
}

.chart-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
}

.chart-title .el-icon {
  color: #409EFF;
}

.chart-actions {
  display: flex;
  gap: 12px;
}
</style>
