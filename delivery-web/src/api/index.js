import http from './request'

export const authApi = {
  captcha: () => http.get('/auth/captcha'),
  login: (data) => http.post('/auth/login', data),
  logout: () => http.post('/auth/logout'),
  me: () => http.get('/auth/me'),
  changePassword: (data) => http.post('/auth/password', data),
  recheck: (password) => http.post('/auth/recheck', { password })
}

export const userApi = {
  page: (params) => http.get('/users', { params }),
  get: (id) => http.get(`/users/${id}`),
  create: (data) => http.post('/users', data),
  update: (id, data) => http.put(`/users/${id}`, data),
  remove: (id) => http.delete(`/users/${id}`),
  resetPassword: (id) => http.put(`/users/${id}/password/reset`),
  unlock: (id) => http.put(`/users/${id}/unlock`)
}

export const dictApi = {
  items: (types) => http.get('/dicts/items', { params: { types: types.join(',') } }),
  all: () => http.get('/dicts/all'),
  create: (data) => http.post('/dicts/items', data),
  update: (id, data) => http.put(`/dicts/items/${id}`, data),
  remove: (id) => http.delete(`/dicts/items/${id}`)
}

export const configApi = {
  list: (group) => http.get('/configs', { params: { group } }),
  save: (values) => http.put('/configs', values)
}

export const systemApi = {
  health: () => http.get('/system/health'),
  info: () => http.get('/system/info')
}

export const customerApi = {
  page: (params) => http.get('/customers', { params }),
  all: () => http.get('/customers/all'),
  create: (data) => http.post('/customers', data),
  update: (id, data) => http.put(`/customers/${id}`, data),
  remove: (id) => http.delete(`/customers/${id}`)
}

export const projectApi = {
  page: (params) => http.get('/projects', { params }),
  all: () => http.get('/projects/all'),
  get: (id) => http.get(`/projects/${id}`),
  overview: (id) => http.get(`/projects/${id}/overview`),
  create: (data, withChecklist = true) => http.post('/projects', data, { params: { withChecklist } }),
  update: (id, data) => http.put(`/projects/${id}`, data),
  remove: (id) => http.delete(`/projects/${id}`),
  changeStatus: (id, data) => http.put(`/projects/${id}/status`, data),
  archive: (id, archived) => http.put(`/projects/${id}/archive`, null, { params: { archived } }),
  lock: (id, locked) => http.put(`/projects/${id}/lock`, null, { params: { locked } }),
  copy: (id, data) => http.post(`/projects/${id}/copy`, data),
  members: (id) => http.get(`/projects/${id}/members`),
  addMember: (id, data) => http.post(`/projects/${id}/members`, data),
  updateMember: (id, mid, data) => http.put(`/projects/${id}/members/${mid}`, data),
  removeMember: (id, mid) => http.delete(`/projects/${id}/members/${mid}`),
  envs: (id) => http.get(`/projects/${id}/envs`),
  addEnv: (id, data) => http.post(`/projects/${id}/envs`, data),
  updateEnv: (id, eid, data) => http.put(`/projects/${id}/envs/${eid}`, data),
  removeEnv: (id, eid) => http.delete(`/projects/${id}/envs/${eid}`),
  products: (id) => http.get(`/projects/${id}/products`),
  addProduct: (id, data) => http.post(`/projects/${id}/products`, data),
  updateProduct: (id, pid, data) => http.put(`/projects/${id}/products/${pid}`, data),
  removeProduct: (id, pid) => http.delete(`/projects/${id}/products/${pid}`),
  checklist: (id) => http.get(`/projects/${id}/checklist`),
  generateChecklist: (id, projectType) =>
    http.post(`/projects/${id}/checklist/generate`, null, { params: { projectType } }),
  addChecklistItem: (id, data) => http.post(`/projects/${id}/checklist`, data),
  updateChecklistItem: (itemId, data) => http.put(`/checklist/${itemId}`, data),
  removeChecklistItem: (itemId) => http.delete(`/checklist/${itemId}`)
}

export const checklistTemplateApi = {
  list: (projectType) => http.get('/checklist-templates', { params: { projectType } }),
  create: (data) => http.post('/checklist-templates', data),
  update: (id, data) => http.put(`/checklist-templates/${id}`, data),
  remove: (id) => http.delete(`/checklist-templates/${id}`)
}

export const docApi = {
  categoryTree: () => http.get('/doc-categories/tree'),
  createCategory: (data) => http.post('/doc-categories', data),
  updateCategory: (id, data) => http.put(`/doc-categories/${id}`, data),
  removeCategory: (id) => http.delete(`/doc-categories/${id}`),
  page: (params) => http.get('/documents', { params }),
  detail: (id) => http.get(`/documents/${id}`),
  create: (data) => http.post('/documents', data),
  update: (id, data) => http.put(`/documents/${id}`, data),
  remove: (id) => http.delete(`/documents/${id}`),
  uploadVersion: (id, formData) => http.post(`/documents/${id}/versions`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  }),
  versions: (id) => http.get(`/documents/${id}/versions`),
  rollback: (id, versionId) => http.put(`/documents/${id}/rollback/${versionId}`),
  removeVersion: (versionId) => http.delete(`/documents/versions/${versionId}`),
  downloadUrl: (versionId, token) => `/api/files/download/${versionId}?token=${encodeURIComponent(token || '')}`,
  previewUrl: (versionId, token) => `/api/files/preview/${versionId}?token=${encodeURIComponent(token || '')}`
}

export const credentialApi = {
  page: (params) => http.get('/credentials', { params }),
  expiring: (days) => http.get('/credentials/expiring', { params: { days } }),
  detail: (id) => http.get(`/credentials/${id}`),
  create: (data) => http.post('/credentials', data),
  update: (id, data) => http.put(`/credentials/${id}`, data),
  remove: (id) => http.delete(`/credentials/${id}`),
  reveal: (id, recheckToken) => http.post(`/credentials/${id}/reveal`, { recheckToken }),
  copyLog: (id) => http.post(`/credentials/${id}/copy-log`),
  rotate: (id, date) => http.post(`/credentials/${id}/rotate`, null, { params: { date } }),
  favorite: (id, favorite) => http.put(`/credentials/${id}/favorite`, null, { params: { favorite } }),
  batchOwner: (data) => http.put('/credentials/batch-owner', data),
  history: (id) => http.get(`/credentials/${id}/history`),
  accessLogs: (id) => http.get(`/credentials/${id}/access-logs`),
  generatePassword: (length) => http.get('/credentials/generate-password', { params: { length } }),
  uploadAttachment: (id, formData) => http.post(`/credentials/${id}/attachment`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  }),
  deleteAttachment: (id) => http.delete(`/credentials/${id}/attachment`),
  attachmentUrl: (id, token) => `/api/credentials/${id}/attachment?token=${encodeURIComponent(token || '')}`
}

export const dashboardApi = {
  expiring: (days) => http.get('/alerts/expiring', { params: { days } }),
  summary: (days) => http.get('/alerts/summary', { params: { days } }),
  stats: () => http.get('/stats/dashboard')
}

export const auditApi = {
  page: (params) => http.get('/audit-logs', { params })
}