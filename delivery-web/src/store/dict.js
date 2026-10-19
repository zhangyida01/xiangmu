import { defineStore } from 'pinia'
import { dictApi } from '../api'

const TYPES = ['PROJECT_TYPE', 'PROJECT_STATUS', 'CRED_TYPE', 'SECRET_LEVEL']

export const useDictStore = defineStore('dict', {
  state: () => ({ map: {}, loaded: false }),
  actions: {
    async load() {
      if (this.loaded) return this.map
      const res = await dictApi.items(TYPES)
      this.map = res.data || {}
      this.loaded = true
      return this.map
    },
    items(type) {
      return this.map[type] || []
    },
    label(type, code) {
      const hit = this.items(type).find((i) => i.itemCode === code)
      return hit ? hit.itemName : code || '-'
    }
  }
})