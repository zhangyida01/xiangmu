<template>
  <div class="page">
    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="操作人 / 对象 / IP" clearable style="width:200px" @keyup.enter="search" />
      <el-select v-model="query.module" placeholder="模块" clearable style="width:140px">
        <el-option v-for="m in modules" :key="m" :label="m" :value="m" />
      </el-select>
      <el-select v-model="query.action" placeholder="动作" clearable style="width:170px">
        <el-option v-for="a in actions" :key="a" :label="a" :value="a" />
      </el-select>
      <el-select v-model="query.success" placeholder="结果" clearable style="width:110px">
        <el-option label="成功" :value="true" /><el-option label="失败" :value="false" />
      </el-select>
      <el-date-picker v-model="range" type="daterange" value-format="YYYY-MM-DD" start-placeholder="开始"
                      end-placeholder="结束" style="width:250px" />
      <el-button type="primary" :icon="Search" @click="search">查询</el-button>
      <el-button :icon="Refresh" @click="reset">重置</el-button>
    </div>

    <el-table :data="rows" v-loading="loading" border stripe size="small">
      <el-table-column prop="createTime" label="时间" width="160" />
      <el-table-column prop="username" label="操作人" width="100" />
      <el-table-column prop="module" label="模块" width="110" />
      <el-table-column prop="action" label="动作" width="150" />
      <el-table-column prop="targetType" label="对象类型" width="130" />
      <el-table-column prop="targetName" label="对象" min-width="150" show-overflow-tooltip />
      <el-table-column prop="projectId" label="项目ID" width="80" />
      <el-table-column prop="detail" label="详情" min-width="220" show-overflow-tooltip />
      <el-table-column prop="ip" label="IP" width="130" />
      <el-table-column label="结果" width="80" align="center">
        <template #default="{ row }">
          <el-tag size="small" :type="row.success ? 'success' : 'danger'">{{ row.success ? '成功' : '失败' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="errMsg" label="错误" min-width="140" show-overflow-tooltip />
    </el-table>

    <div class="pager">
      <el-pagination v-model:current-page="query.page" v-model:page-size="query.size" :total="total"
                     :page-sizes="[20, 50, 100]" layout="total, sizes, prev, pager, next, jumper"
                     @size-change="load" @current-change="load" />
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref, watch } from 'vue'
import { Refresh, Search } from '@element-plus/icons-vue'
import { auditApi } from '../api'

const modules = ['AUTH', 'PROJECT', 'DOC', 'CREDENTIAL', 'CUSTOMER', 'SYSTEM']
const actions = ['LOGIN', 'LOGOUT', 'VIEW_SECRET', 'COPY', 'CREATE', 'UPDATE', 'DELETE', 'DOWNLOAD', 'UPLOAD', 'ROTATE', 'CHANGE_STATUS', 'ARCHIVE', 'LOCK']

const loading = ref(false)
const rows = ref([])
const total = ref(0)
const range = ref([])
const query = reactive({ page: 1, size: 20, keyword: '', module: '', action: '', success: null })

watch(range, (v) => {
  query.startTime = v?.[0] || ''
  query.endTime = v?.[1] || ''
})

onMounted(load)

async function load() {
  loading.value = true
  try {
    const res = await auditApi.page({ ...query, startTime: range.value?.[0], endTime: range.value?.[1] })
    rows.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

function search() { query.page = 1; load() }
function reset() {
  Object.assign(query, { page: 1, keyword: '', module: '', action: '', success: null })
  range.value = []
  load()
}
</script>