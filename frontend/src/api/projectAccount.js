import request from '../utils/request'

// 根据项目ID获取账户列表
export function getAccountsByProject(projectId) {
  return request({
    url: `/project-account/list/${projectId}`,
    method: 'get'
  })
}

// 账户详情
export function getAccountDetail(id) {
  return request({
    url: `/project-account/detail/${id}`,
    method: 'get'
  })
}

// 获取真实密码
export function getAccountPassword(id) {
  return request({
    url: `/project-account/password/${id}`,
    method: 'get'
  })
}

// 添加账户
export function addAccount(data) {
  return request({
    url: '/project-account/add',
    method: 'post',
    data
  })
}

// 更新账户
export function updateAccount(data) {
  return request({
    url: '/project-account/update',
    method: 'put',
    data
  })
}

// 删除账户
export function deleteAccount(id) {
  return request({
    url: `/project-account/delete/${id}`,
    method: 'delete'
  })
}
