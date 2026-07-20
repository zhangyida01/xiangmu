import request from '../utils/request'

// 供应商列表
export function getSupplierList(params) {
  return request({
    url: '/supplier/list',
    method: 'get',
    params
  })
}

// 获取所有供应商
export function getAllSuppliers() {
  return request({
    url: '/supplier/all',
    method: 'get'
  })
}

// 添加供应商
export function addSupplier(data) {
  return request({
    url: '/supplier/add',
    method: 'post',
    data
  })
}

// 更新供应商
export function updateSupplier(data) {
  return request({
    url: '/supplier/update',
    method: 'put',
    data
  })
}

// 删除供应商
export function deleteSupplier(id) {
  return request({
    url: `/supplier/delete/${id}`,
    method: 'delete'
  })
}

// 获取供应商联系人列表
export function getSupplierContacts(supplierId) {
  return request({
    url: `/supplier/contact/list/${supplierId}`,
    method: 'get'
  })
}

// 添加联系人
export function addSupplierContact(data) {
  return request({
    url: '/supplier/contact/add',
    method: 'post',
    data
  })
}

// 更新联系人
export function updateSupplierContact(data) {
  return request({
    url: '/supplier/contact/update',
    method: 'put',
    data
  })
}

// 删除联系人
export function deleteSupplierContact(id) {
  return request({
    url: `/supplier/contact/delete/${id}`,
    method: 'delete'
  })
}
