import request from '../utils/request'

// 项目列表
export function getProjectList(params) {
  return request({
    url: '/project/list',
    method: 'get',
    params
  })
}

// 项目详情
export function getProjectDetail(id) {
  return request({
    url: `/project/detail/${id}`,
    method: 'get'
  })
}

// 创建项目
export function addProject(data) {
  return request({
    url: '/project/add',
    method: 'post',
    data
  })
}

// 更新项目
export function updateProject(data) {
  return request({
    url: '/project/update',
    method: 'put',
    data
  })
}

// 删除项目
export function deleteProject(id) {
  return request({
    url: `/project/delete/${id}`,
    method: 'delete'
  })
}
