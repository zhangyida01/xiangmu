import request from '../utils/request'

export function getDocumentList(params) {
  return request({
    url: '/project-document/list',
    method: 'get',
    params
  })
}

export function uploadDocument(data) {
  return request({
    url: '/project-document/upload',
    method: 'post',
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    data
  })
}

export function downloadDocument(id) {
  return request({
    url: `/project-document/download/${id}`,
    method: 'get',
    responseType: 'blob'
  })
}

export function deleteDocument(id) {
  return request({
    url: `/project-document/${id}`,
    method: 'delete'
  })
}

export function getDocumentDetail(id) {
  return request({
    url: `/project-document/${id}`,
    method: 'get'
  })
}