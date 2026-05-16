import request from '../utils/request'

/**
 * 获取平台统计数据
 */
export function getPlatformStatistics() {
  return request({
    url: '/statistics/platform',
    method: 'get'
  })
}

/**
 * 获取日订单量统计
 */
export function getDailyOrders(days = 7) {
  return request({
    url: '/statistics/daily-orders',
    method: 'get',
    params: { days }
  })
}

/**
 * 获取日销售额统计
 */
export function getDailySales(days = 7) {
  return request({
    url: '/statistics/daily-sales',
    method: 'get',
    params: { days }
  })
}

/**
 * 获取商家活跃度统计
 */
export function getMerchantActivity() {
  return request({
    url: '/statistics/merchant-activity',
    method: 'get'
  })
}

/**
 * 获取订单状态分布
 */
export function getOrderStatusDistribution() {
  return request({
    url: '/statistics/order-status-distribution',
    method: 'get'
  })
}

/**
 * 获取订单与销售额趋势
 */
export function getOrderTrend(days = 7) {
  return request({
    url: '/statistics/platform-trend',
    method: 'get',
    params: { days }
  })
}

/**
 * 获取商家分类分布
 */
export function getMerchantCategory() {
  return request({
    url: '/statistics/merchant-category',
    method: 'get'
  })
}

/**
 * 获取用户增长趋势
 */
export function getUserGrowth(days = 7) {
  return request({
    url: '/statistics/user-growth',
    method: 'get',
    params: { days }
  })
}

/**
 * 获取热销商品TOP10
 */
export function getTopFoods(limit = 10) {
  return request({
    url: '/statistics/top-foods',
    method: 'get',
    params: { limit }
  })
}

/**
 * 获取区域订单分布
 */
export function getRegionDistribution() {
  return request({
    url: '/statistics/region-distribution',
    method: 'get'
  })
}

/**
 * 获取餐品分类销量分布
 */
export function getFoodCategorySales(merchantId = null) {
  return request({
    url: '/statistics/food-category-sales',
    method: 'get',
    params: { merchantId }
  })
}

/**
 * 获取完整平台统计数据
 */
export function getFullPlatformStatistics(days = 7) {
  return request({
    url: '/statistics/full-platform',
    method: 'get',
    params: { days }
  })
}
