import request from '../utils/request'

export function getProjectList(params) {
  return request({
    url: '/project/list',
    method: 'get',
    params
  })
}

export function getProjectDetail(id) {
  return request({
    url: `/project/detail/${id}`,
    method: 'get'
  })
}

export function addProject(data) {
  return request({
    url: '/project',
    method: 'post',
    data
  })
}

export function updateProject(data) {
  return request({
    url: '/project',
    method: 'put',
    data
  })
}

export function deleteProject(id) {
  return request({
    url: `/project/${id}`,
    method: 'delete'
  })
}