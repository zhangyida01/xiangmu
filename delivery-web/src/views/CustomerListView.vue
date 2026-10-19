<template>
  <div class="page">
    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="客户名称 / 简称 / 联系人" clearable style="width:240px" @keyup.enter="search" />
      <el-button type="primary" :icon="Search" @click="search">查询</el-button>
      <div class="spacer"></div>
      <el-button v-if="user.canEdit" type="primary" :icon="Plus" @click="openForm()">新增客户</el-button>
    </div>

    <el-table :data="rows" v-loading="loading" border stripe size="small">
      <el-table-column prop="name" label="客户名称" min-width="220" show-overflow-tooltip />
      <el-table-column prop="shortName" label="简称" width="120" />
      <el-table-column prop="industry" label="行业" width="120" />
      <el-table-column prop="contactName" label="联系人" width="110" />
      <el-table-column prop="contactPhone" label="电话" width="140" />
      <el-table-column prop="address" label="地址" min-width="180" show-overflow-tooltip />
      <el-table-column prop="createTime" label="创建时间" width="160" />
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button v-if="user.canEdit" link type="primary" size="small" @click="openForm(row)">编辑</el-button>
          <el-button v-if="user.isAdmin" link type="danger" size="small" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination v-model:current-page="query.page" v-model:page-size="query.size" :total="total"
                     :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next"
                     @size-change="load" @current-change="load" />
    </div>

    <el-dialog v-model="visible" :title="form.id ? '编辑客户' : '新增客户'" width="520px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="客户名称" required><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="简称"><el-input v-model="form.shortName" /></el-form-item>
        <el-form-item label="行业"><el-input v-model="form.industry" /></el-form-item>
        <el-form-item label="联系人"><el-input v-model="form.contactName" /></el-form-item>
        <el-form-item label="电话"><el-input v-model="form.contactPhone" /></el-form-item>
        <el-form-item label="地址"><el-input v-model="form.address" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { customerApi } from '../api'
import { useUserStore } from '../store/user'

const user = useUserStore()
const loading = ref(false)
const rows = ref([])
const total = ref(0)
const query = reactive({ page: 1, size: 20, keyword: '' })
const visible = ref(false)
const form = reactive({ id: null, name: '', shortName: '', industry: '', contactName: '', contactPhone: '', address: '', remark: '' })

onMounted(load)

async function load() {
  loading.value = true
  try {
    const res = await customerApi.page({ ...query })
    rows.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

function search() { query.page = 1; load() }

function openForm(row) {
  Object.assign(form, row ? { ...row } : { id: null, name: '', shortName: '', industry: '', contactName: '', contactPhone: '', address: '', remark: '' })
  visible.value = true
}

async function save() {
  if (!form.name) { ElMessage.warning('请填写客户名称'); return }
  if (form.id) await customerApi.update(form.id, form)
  else await customerApi.create(form)
  ElMessage.success('已保存')
  visible.value = false
  load()
}

async function remove(row) {
  await ElMessageBox.confirm(`确认删除客户「${row.name}」？`, '危险操作', { type: 'warning' })
  await customerApi.remove(row.id)
  ElMessage.success('已删除')
  load()
}
</script>