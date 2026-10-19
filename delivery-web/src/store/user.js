import { defineStore } from 'pinia'
import { authApi } from '../api'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('pdm_token') || '',
    info: null,
    permissions: []
  }),
  getters: {
    logged: (s) => !!s.token,
    role: (s) => s.info?.role || '',
    isAdmin: (s) => s.info?.role === 'ADMIN',
    canEdit: (s) => s.info?.role === 'ADMIN' || s.info?.role === 'USER'
  },
  actions: {
    can(perm) {
      return this.isAdmin || this.permissions.includes(perm)
    },
    async login(payload) {
      const res = await authApi.login(payload)
      this.token = res.data.token
      this.info = res.data.user
      this.permissions = res.data.user.permissions || []
      localStorage.setItem('pdm_token', this.token)
      return res.data
    },
    async loadMe() {
      const res = await authApi.me()
      this.info = res.data
      this.permissions = res.data.permissions || []
      return res.data
    },
    async logout() {
      try {
        await authApi.logout()
      } catch (e) {
        // 忽略登出接口异常
      }
      this.reset()
    },
    reset() {
      this.token = ''
      this.info = null
      this.permissions = []
      localStorage.removeItem('pdm_token')
    }
  }
})