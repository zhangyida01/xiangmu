import request from '../utils/request'

// 客户列表
export function getCustomerList(params) {
  return request({
    url: '/customer/list',
    method: 'get',
    params
  })
}

// 客户详情
export function getCustomerDetail(id) {
  return request({
    url: `/customer/detail/${id}`,
    method: 'get'
  })
}

// 添加客户
export function addCustomer(data) {
  return request({
    url: '/customer/add',
    method: 'post',
    data
  })
}

// 更新客户
export function updateCustomer(data) {
  return request({
    url: '/customer/update',
    method: 'put',
    data
  })
}

// 删除客户
export function deleteCustomer(id) {
  return request({
    url: `/customer/delete/${id}`,
    method: 'delete'
  })
}

// 获取所有客户（下拉选择用）
export function getAllCustomers() {
  return request({
    url: '/customer/all',
    method: 'get'
  })
}
