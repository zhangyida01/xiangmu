<template>
  <div class="page">
    <div class="toolbar">
      <span>提前天数</span>
      <el-input-number v-model="days" :min="1" :max="365" size="small" />
      <el-button type="primary" size="small" @click="load">刷新</el-button>
      <div class="spacer"></div>
      <el-space>
        <el-tag v-for="s in summary" :key="s.srcType" :type="tagType(s.srcType)">
          {{ label(s.srcType) }}：{{ s.cnt }}（最近 {{ s.minDays }} 天）
        </el-tag>
      </el-space>
    </div>

    <el-table :data="rows" v-loading="loading" border stripe size="small">
      <el-table-column label="类别" width="120">
        <template #default="{ row }"><el-tag size="small" :type="tagType(row.srcType)">{{ label(row.srcType) }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="projectName" label="项目" min-width="220" show-overflow-tooltip>
        <template #default="{ row }">
          <el-link v-if="row.projectId" type="primary" @click="$router.push(`/projects/${row.projectId}`)">{{ row.projectName }}</el-link>
          <span v-else>{{ row.projectName }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="itemName" label="事项" min-width="220" show-overflow-tooltip />
      <el-table-column prop="expireDate" label="到期日" width="120" />
      <el-table-column label="剩余" width="120" align="center">
        <template #default="{ row }">
          <span :class="row.daysLeft < 0 ? 'danger-text' : row.daysLeft <= 7 ? 'warn-text' : ''">
            {{ row.daysLeft < 0 ? `已超期 ${-row.daysLeft} 天` : `${row.daysLeft} 天` }}
          </span>
        </template>
      </el-table-column>
    </el-table>
    <el-empty v-if="!loading && !rows.length" description="太好了，没有临期事项" />
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { dashboardApi } from '../api'

const days = ref(30)
const rows = ref([])
const summary = ref([])
const loading = ref(false)

onMounted(load)

async function load() {
  loading.value = true
  try {
    const [a, s] = await Promise.all([dashboardApi.expiring(days.value), dashboardApi.summary(days.value)])
    rows.value = a.data || []
    summary.value = s.data || []
  } finally {
    loading.value = false
  }
}

const LABELS = { CREDENTIAL: '凭据到期', ROTATE: '密码轮换', LICENSE: 'License', WARRANTY: '质保到期', MILESTONE: '里程碑' }
function label(t) { return LABELS[t] || t }
function tagType(t) {
  return { CREDENTIAL: 'danger', ROTATE: 'warning', LICENSE: '', WARRANTY: 'info', MILESTONE: 'success' }[t] || ''
}
</script>