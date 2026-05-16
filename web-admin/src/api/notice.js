import request from '../utils/request'

/**
 * 公告管理 API
 */

// 获取公告列表
export function getNoticeList(params) {
  return request({
    url: '/admin/notices',
    method: 'get',
    params
  })
}

// 发布公告
export function publishNotice(data) {
  return request({
    url: '/admin/publish-notice',
    method: 'post',
    data
  })
}

// 更新公告
export function updateNotice(noticeId, data) {
  return request({
    url: `/admin/notices/${noticeId}`,
    method: 'put',
    data
  })
}

// 删除公告
export function deleteNotice(noticeId) {
  return request({
    url: `/admin/notices/${noticeId}`,
    method: 'delete'
  })
}
