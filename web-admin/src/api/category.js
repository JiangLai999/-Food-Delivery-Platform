import request from '../utils/request'

/**
 * 分类管理 API
 */

// 获取分类列表
export function getCategoryList(params) {
  return request({
    url: '/admin/categories/ops/all',
    method: 'get',
    params
  })
}

// 添加分类
export function addCategory(data) {
  return request({
    url: '/admin/categories/ops/add',
    method: 'post',
    data
  })
}

// 更新分类
export function updateCategory(categoryId, data) {
  return request({
    url: `/admin/categories/ops/update/${categoryId}`,
    method: 'put',
    data
  })
}

// 删除分类
export function deleteCategory(categoryId) {
  return request({
    url: `/admin/categories/ops/delete/${categoryId}`,
    method: 'delete'
  })
}
