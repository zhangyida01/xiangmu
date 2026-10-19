<template>
  <div class="page">
    <el-row :gutter="12" class="card-gap">
      <el-col :span="4" v-for="c in cards" :key="c.label">
        <el-card shadow="hover" body-style="padding:16px">
          <div class="stat-value" :style="{ color: c.color }">{{ c.value }}</div>
          <div class="stat-label">{{ c.label }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="12">
      <el-col :span="14">
        <el-card shadow="never" class="card-gap">
          <template #header>
            <div style="display:flex;justify-content:space-between;align-items:center">
              <span>最近项目</span>
              <el-link type="primary" @click="$router.push('/projects')">全部项目</el-link>
            </div>
          </template>
          <el-table :data="stats.recentProjects || []" size="small" @row-click="openProject" style="cursor:pointer">
            <el-table-column prop="projectNo" label="编号" width="130" />
            <el-table-column prop="name" label="项目名称" min-width="180" show-overflow-tooltip />
            <el-table-column prop="customerName" label="客户" width="150" show-overflow-tooltip />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag size="small">{{ dict.label('PROJECT_STATUS', row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="文档完成率" width="110">
              <template #default="{ row }">{{ row.docRate || 0 }}%</template>
            </el-table-column>
            <el-table-column prop="planAcceptDate" label="计划验收" width="110" />
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="10">
        <el-card shadow="never" class="card-gap">
          <template #header>
            <div style="display:flex;justify-content:space-between;align-items:center">
              <span>临期预警（{{ alertDays }} 天内）</span>
              <el-link type="primary" @click="$router.push('/alerts')">查看全部</el-link>
            </div>
          </template>
          <el-table :data="alerts" size="small" max-height="330">
            <el-table-column prop="projectName" label="项目" min-width="140" show-overflow-tooltip />
            <el-table-column prop="itemName" label="事项" min-width="140" show-overflow-tooltip />
            <el-table-column label="剩余" width="90">
              <template #default="{ row }">
                <span :class="row.daysLeft < 0 ? 'danger-text' : row.daysLeft <= 7 ? 'warn-text' : ''">
                  {{ row.daysLeft < 0 ? '已超期' : row.daysLeft + ' 天' }}
                </span>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!alerts.length" description="暂无临期事项" :image-size="60" />
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never">
      <template #header>项目状态分布</template>
      <el-space wrap>
        <el-tag v-for="s in stats.statusGroup || []" :key="s.status" size="large" class="card-gap">
          {{ dict.label('PROJECT_STATUS', s.status) }}：{{ s.cnt }}
        </el-tag>
        <span v-if="!(stats.statusGroup || []).length" class="muted">暂无数据</span>
      </el-space>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onActivated, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { dashboardApi } from '../api'
import { useDictStore } from '../store/dict'

const router = useRouter()
const dict = useDictStore()
const stats = ref({})
const alerts = ref([])
const alertDays = ref(30)

const cards = computed(() => [
  { label: '项目总数', value: stats.value.projectCount ?? 0, color: '#409eff' },
  { label: '进行中', value: stats.value.activeProjectCount ?? 0, color: '#67c23a' },
  { label: '30天内待验收', value: stats.value.acceptingIn30Days ?? 0, color: '#e6a23c' },
  { label: '临期预警', value: stats.value.alertCount ?? 0, color: '#f56c6c' },
  { label: '交付文档', value: stats.value.documentCount ?? 0, color: '#909399' },
  { label: '凭据条目', value: stats.value.credentialCount ?? 0, color: '#9254de' }
])

onMounted(load)
onActivated(load)

async function load() {
  const [s, a] = await Promise.all([dashboardApi.stats(), dashboardApi.expiring(alertDays.value)])
  stats.value = s.data || {}
  alerts.value = (a.data || []).slice(0, 12)
}

function openProject(row) {
  if (row?.id) router.push(`/projects/${row.id}`)
}
</script>

<style scoped>
.stat-value { font-size: 26px; font-weight: 600; line-height: 1.2; }
.stat-label { color: #909399; font-size: 12px; margin-top: 4px; }
</style>