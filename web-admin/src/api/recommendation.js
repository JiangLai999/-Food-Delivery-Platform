import request from '../utils/request'

// Get top merchants by sales/popularity
export function getTopMerchants(limit = 5) {
  return request.get('/recommendation/top-merchants', { params: { limit } })
}

// Get top foods for a merchant (or globally if merchantId not provided)
export function getTopFoods(merchantId = null, limit = 10) {
  const params = { limit }
  if (merchantId != null) params.merchantId = merchantId
  return request.get('/recommendation/foods', { params })
}

// Get recommendations for a user (optional)
export function getRecommendationsForUser(userId, limit = 5) {
  return request.get('/recommendation/foods', { params: { userId, limit } })
}
