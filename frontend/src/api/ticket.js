import request from '../utils/request'

// 工单列表
export function getTicketList(params) {
  return request({
    url: '/ticket/list',
    method: 'get',
    params
  })
}

// 工单详情
export function getTicketDetail(id) {
  return request({
    url: `/ticket/detail/${id}`,
    method: 'get'
  })
}

// 创建工单
export function addTicket(data) {
  return request({
    url: '/ticket/add',
    method: 'post',
    data
  })
}

// 更新工单
export function updateTicket(data) {
  return request({
    url: '/ticket/update',
    method: 'put',
    data
  })
}

// 处理工单
export function resolveTicket(data) {
  return request({
    url: '/ticket/resolve',
    method: 'put',
    data
  })
}

// 删除工单
export function deleteTicket(id) {
  return request({
    url: `/ticket/delete/${id}`,
    method: 'delete'
  })
}
