<template>
  <div class="page">
    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="账号 / 姓名" clearable style="width:200px" @keyup.enter="load" />
      <el-select v-model="query.role" placeholder="角色" clearable style="width:140px">
        <el-option label="管理员 ADMIN" value="ADMIN" /><el-option label="普通用户 USER" value="USER" />
        <el-option label="只读 VIEWER" value="VIEWER" />
      </el-select>
      <el-button type="primary" :icon="Search" @click="load">查询</el-button>
      <div class="spacer"></div>
      <el-button type="primary" :icon="Plus" @click="openForm()">新增用户</el-button>
    </div>

    <el-table :data="rows" v-loading="loading" border stripe size="small">
      <el-table-column prop="username" label="账号" width="130" />
      <el-table-column prop="realName" label="姓名" width="110" />
      <el-table-column label="角色" width="110">
        <template #default="{ row }"><el-tag size="small" :type="row.role === 'ADMIN' ? 'danger' : row.role === 'VIEWER' ? 'info' : ''">{{ row.role }}</el-tag></template>
      </el-table-column>
      <el-table-column label="凭据明文" width="90" align="center">
        <template #default="{ row }"><el-tag size="small" :type="row.canViewSecret ? 'success' : 'info'">{{ row.canViewSecret ? '可看' : '不可' }}</el-tag></template>
      </el-table-column>
      <el-table-column label="导出" width="70" align="center">
        <template #default="{ row }">{{ row.canExport ? '是' : '否' }}</template>
      </el-table-column>
      <el-table-column label="下载" width="70" align="center">
        <template #default="{ row }">{{ row.canDownload ? '是' : '否' }}</template>
      </el-table-column>
      <el-table-column label="二次校验" width="90" align="center">
        <template #default="{ row }">{{ row.needRecheck ? '开启' : '关闭' }}</template>
      </el-table-column>
      <el-table-column label="状态" width="80" align="center">
        <template #default="{ row }"><el-tag size="small" :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="lastLoginTime" label="最后登录" width="160" />
      <el-table-column prop="lastLoginIp" label="登录IP" width="130" />
      <el-table-column label="操作" width="230" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="openForm(row)">编辑</el-button>
          <el-button link size="small" @click="resetPwd(row)">重置密码</el-button>
          <el-button v-if="row.lockUntil" link type="warning" size="small" @click="unlock(row)">解锁</el-button>
          <el-button link type="danger" size="small" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination v-model:current-page="query.page" v-model:page-size="query.size" :total="total"
                     layout="total, prev, pager, next" @current-change="load" />
    </div>

    <el-dialog v-model="visible" :title="form.id ? '编辑用户' : '新增用户'" width="560px">
      <el-form :model="form" label-width="110px">
        <el-form-item label="账号" required><el-input v-model="form.username" :disabled="!!form.id" /></el-form-item>
        <el-form-item label="姓名"><el-input v-model="form.realName" /></el-form-item>
        <el-form-item :label="form.id ? '重设密码' : '初始密码'">
          <el-input v-model="form.password" type="password" show-password :placeholder="form.id ? '留空表示不修改' : '至少8位，含字母和数字'" />
        </el-form-item>
        <el-form-item label="角色">
          <el-radio-group v-model="form.role">
            <el-radio-button value="ADMIN">管理员</el-radio-button>
            <el-radio-button value="USER">普通用户</el-radio-button>
            <el-radio-button value="VIEWER">只读</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <template v-if="form.role !== 'ADMIN' && form.role !== 'VIEWER'">
          <el-form-item label="凭据明文"><el-switch v-model="form.canViewSecret" /></el-form-item>
          <el-form-item label="凭据导出"><el-switch v-model="form.canExport" /></el-form-item>
          <el-form-item label="附件下载"><el-switch v-model="form.canDownload" /></el-form-item>
          <el-form-item label="删除项目"><el-switch v-model="form.canDeleteProject" /></el-form-item>
          <el-form-item label="看密文需二次校验"><el-switch v-model="form.needRecheck" /></el-form-item>
        </template>
        <el-form-item label="启用"><el-switch v-model="form.status" :active-value="1" :inactive-value="0" /></el-form-item>
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
import { userApi } from '../../api'

const loading = ref(false)
const rows = ref([])
const total = ref(0)
const query = reactive({ page: 1, size: 20, keyword: '', role: '' })
const visible = ref(false)
const empty = () => ({
  id: null, username: '', realName: '', password: '', role: 'USER', canViewSecret: false,
  canExport: false, canDownload: true, canDeleteProject: false, needRecheck: false, status: 1, remark: ''
})
const form = reactive(empty())

onMounted(load)

async function load() {
  loading.value = true
  try {
    const res = await userApi.page({ ...query })
    rows.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

function openForm(row) {
  Object.assign(form, empty())
  if (row) Object.keys(form).forEach((k) => { if (row[k] !== undefined && row[k] !== null) form[k] = row[k] })
  form.password = ''
  visible.value = true
}

async function save() {
  if (!form.username) { ElMessage.warning('请填写账号'); return }
  if (!form.id && !form.password) { ElMessage.warning('请设置初始密码'); return }
  if (form.id) await userApi.update(form.id, form)
  else await userApi.create(form)
  ElMessage.success('已保存')
  visible.value = false
  load()
}

async function resetPwd(row) {
  await ElMessageBox.confirm(`确认重置「${row.username}」的密码？`, '提示', { type: 'warning' })
  const res = await userApi.resetPassword(row.id)
  await ElMessageBox.alert(`新密码：${res.data.password}`, '请通过安全渠道告知用户', { type: 'success' })
}

async function unlock(row) {
  await userApi.unlock(row.id)
  ElMessage.success('已解锁')
  load()
}

async function remove(row) {
  await ElMessageBox.confirm(`确认删除用户「${row.username}」？`, '危险操作', { type: 'warning' })
  await userApi.remove(row.id)
  ElMessage.success('已删除')
  load()
}
</script>