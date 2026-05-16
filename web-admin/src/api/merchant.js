import request from '../utils/request'

/**
 * 商家管理 API
 */

// 获取商家列表
export function getMerchantList(params) {
  return request({
    url: '/admin/merchants',
    method: 'get',
    params
  })
}

// 审核商家
export function approveMerchant(merchantId, status, reason) {
  return request({
    url: `/admin/approve-merchant/${merchantId}`,
    method: 'post',
    params: { status, reason }
  })
}

// 更新商家状态
export function updateMerchantStatus(merchantId, status) {
  return request({
    url: `/admin/merchants/${merchantId}/status`,
    method: 'put',
    params: { status }
  })
}

// 获取待审核商家列表
export function getPendingMerchants(params) {
  return request({
    url: '/admin/pending-merchants',
    method: 'get',
    params
  })
}
