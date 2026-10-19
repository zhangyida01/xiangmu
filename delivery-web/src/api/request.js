import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'

const http = axios.create({ baseURL: '/api', timeout: 120000 })

let redirecting = false

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('pdm_token')
  if (token) {
    config.headers.Authorization = 'Bearer ' + token
  }
  return config
})

http.interceptors.response.use(
  (response) => {
    const data = response.data
    if (data instanceof Blob || data instanceof ArrayBuffer || data.code === undefined) {
      return data
    }
    if (data.code === 0) {
      return data
    }
    if (data.code === 40101) {
      handleUnauthorized(data.msg)
      return Promise.reject(bizError(data.code, data.msg || '登录已失效'))
    }
    // 40302 需要二次密码校验，交由调用方处理，不弹全局提示
    if (data.code !== 40302) {
      ElMessage.error(data.msg || '操作失败')
    }
    return Promise.reject(bizError(data.code, data.msg || '操作失败'))
  },
  (error) => {
    const status = error.response?.status
    if (status === 401) {
      handleUnauthorized('登录已失效')
    } else {
      ElMessage.error(error.response?.data?.msg || error.message || '网络异常')
    }
    return Promise.reject(error)
  }
)

function bizError(code, msg) {
  const e = new Error(msg)
  e.code = code
  return e
}

function handleUnauthorized(msg) {
  if (redirecting) return
  redirecting = true
  localStorage.removeItem('pdm_token')
  ElMessageBox.alert(msg || '登录已失效，请重新登录', '提示', { type: 'warning' })
    .catch(() => {})
    .finally(() => {
      redirecting = false
      if (!location.hash.startsWith('#/login')) {
        location.hash = '#/login'
        location.reload()
      }
    })
}

export default http