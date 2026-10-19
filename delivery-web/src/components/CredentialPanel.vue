<template>
  <div>
    <div class="toolbar">
      <el-select v-if="!projectId" v-model="query.projectId" placeholder="全部项目" clearable filterable style="width: 220px">
        <el-option v-for="p in projects" :key="p.id" :label="`${p.projectNo} ${p.name}`" :value="p.id" />
      </el-select>
      <el-select v-model="query.credType" placeholder="凭据类型" clearable style="width: 150px">
        <el-option v-for="d in dict.items('CRED_TYPE')" :key="d.itemCode" :label="d.itemName" :value="d.itemCode" />
      </el-select>
      <el-input v-model="query.keyword" placeholder="平台 / 账号 / 主机 / 负责人" clearable style="width: 220px" @keyup.enter="search" />
      <el-checkbox v-model="onlyFavorite" label="只看收藏" border size="small" />
      <el-button type="primary" :icon="Search" @click="search">查询</el-button>
      <div class="spacer"></div>
      <el-button v-if="user.canEdit" type="primary" :icon="Plus" @click="openForm()">新增凭据</el-button>
    </div>

    <el-table :data="rows" v-loading="loading" border stripe size="small">
      <el-table-column width="42" align="center">
        <template #default="{ row }">
          <el-icon style="cursor:pointer" :class="row.favorite ? 'warn-text' : 'muted'" @click="toggleFav(row)">
            <StarFilled v-if="row.favorite" /><Star v-else />
          </el-icon>
        </template>
      </el-table-column>
      <el-table-column v-if="!projectId" prop="projectName" label="项目" width="180" show-overflow-tooltip />
      <el-table-column prop="platformName" label="平台 / 系统" min-width="160" show-overflow-tooltip />
      <el-table-column label="类型" width="110">
        <template #default="{ row }">{{ dict.label('CRED_TYPE', row.credType) }}</template>
      </el-table-column>
      <el-table-column prop="envType" label="环境" width="70" align="center" />
      <el-table-column label="地址" min-width="200" show-overflow-tooltip>
        <template #default="{ row }">
          <el-link v-if="row.url" type="primary" :href="row.url" target="_blank" class="mono">{{ row.url }}</el-link>
          <span v-else-if="row.host" class="mono">{{ row.host }}<template v-if="row.port">:{{ row.port }}</template></span>
          <span v-else class="muted">-</span>
        </template>
      </el-table-column>
      <el-table-column label="账号" width="170">
        <template #default="{ row }">
          <span class="mono">{{ revealed[row.id]?.username || row.usernameMasked || '-' }}</span>
          <el-button v-if="row.username" link size="small" :icon="CopyDocument" title="复制账号"
                     @click="copyText(row.username, row, 'account')" />
        </template>
      </el-table-column>
      <el-table-column label="密码 / 密钥" width="200">
        <template #default="{ row }">
          <span class="mono secret-mask">{{ revealed[row.id]?.secret || (row.hasSecret ? '••••••••' : '未设置') }}</span>
          <el-button v-if="row.hasSecret && user.can('secret:view')" link size="small" :icon="View"
                     title="查看明文" @click="reveal(row)" />
          <el-button v-if="row.hasSecret && user.can('secret:view')" link size="small" :icon="CopyDocument"
                     title="复制密码" @click="copySecret(row)" />
        </template>
      </el-table-column>
      <el-table-column prop="owner" label="负责人" width="90" />
      <el-table-column label="到期 / 轮换" width="120" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.needChange" type="danger" size="small">待改密</el-tag>
          <span v-else-if="row.daysLeft !== null && row.daysLeft !== undefined"
                :class="row.daysLeft < 0 ? 'danger-text' : row.daysLeft <= 30 ? 'warn-text' : 'muted'">
            {{ row.daysLeft < 0 ? `超期${-row.daysLeft}天` : `${row.daysLeft}天` }}
          </span>
          <span v-else class="muted">-</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="210" fixed="right">
        <template #default="{ row }">
          <el-button v-if="user.canEdit" link type="primary" size="small" @click="openForm(row)">编辑</el-button>
          <el-button v-if="user.canEdit" link size="small" @click="markRotated(row)">已改密</el-button>
          <el-button link size="small" @click="openLogs(row)">记录</el-button>
          <el-button v-if="user.canEdit" link type="danger" size="small" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination v-model:current-page="query.page" v-model:page-size="query.size" :total="total"
                     :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next"
                     @size-change="load" @current-change="load" />
    </div>

    <el-dialog v-model="formVisible" :title="form.id ? '编辑凭据' : '新增凭据'" width="760px" top="6vh">
      <el-form :model="form" label-width="110px" ref="formRef" :rules="rules">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="所属项目" prop="projectId">
              <el-select v-model="form.projectId" filterable style="width:100%" :disabled="!!projectId">
                <el-option v-for="p in projects" :key="p.id" :label="`${p.projectNo} ${p.name}`" :value="p.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12"><el-form-item label="平台/系统" prop="platformName"><el-input v-model="form.platformName" placeholder="如 云平台控制台 / 生产MySQL" /></el-form-item></el-col>
          <el-col :span="12">
            <el-form-item label="凭据类型">
              <el-select v-model="form.credType" style="width:100%">
                <el-option v-for="d in dict.items('CRED_TYPE')" :key="d.itemCode" :label="d.itemName" :value="d.itemCode" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="环境">
              <el-select v-model="form.envType" style="width:100%">
                <el-option label="生产 PROD" value="PROD" /><el-option label="测试 TEST" value="TEST" />
                <el-option label="UAT" value="UAT" /><el-option label="开发 DEV" value="DEV" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24"><el-form-item label="访问地址"><el-input v-model="form.url" placeholder="https://..." /></el-form-item></el-col>
          <el-col :span="14"><el-form-item label="主机 / IP"><el-input v-model="form.host" /></el-form-item></el-col>
          <el-col :span="10"><el-form-item label="端口"><el-input-number v-model="form.port" :min="1" :max="65535" style="width:100%" /></el-form-item></el-col>
          <el-col :span="14"><el-form-item label="账号"><el-input v-model="form.username" autocomplete="off" /></el-form-item></el-col>
          <el-col :span="10">
            <el-form-item label="密钥类型">
              <el-select v-model="form.secretType" style="width:100%">
                <el-option label="密码 PASSWORD" value="PASSWORD" /><el-option label="密钥 KEY" value="KEY" />
                <el-option label="令牌 TOKEN" value="TOKEN" /><el-option label="证书 CERT" value="CERT" />
                <el-option label="无 NONE" value="NONE" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="18">
            <el-form-item :label="form.id ? '新密码' : '密码 / 密钥'">
              <el-input v-model="form.secret" type="password" show-password autocomplete="new-password"
                        :placeholder="form.id ? '留空表示不修改' : '将加密存储'" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label-width="10px">
              <el-button :icon="MagicStick" @click="genPwd">生成</el-button>
            </el-form-item>
          </el-col>
          <el-col :span="8"><el-form-item label="负责人"><el-input v-model="form.owner" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="到期日"><el-date-picker v-model="form.expireDate" type="date" value-format="YYYY-MM-DD" style="width:100%" placeholder="证书/域名/License" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="轮换周期"><el-input-number v-model="form.rotateDays" :min="0" :max="3650" style="width:100%" placeholder="天" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="上次改密"><el-date-picker v-model="form.lastRotateDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="收藏"><el-switch v-model="form.favorite" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="待改密"><el-switch v-model="form.needChange" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="logVisible" :title="`凭据记录 - ${current.platformName || ''}`" size="620px">
      <el-tabs>
        <el-tab-pane label="访问记录">
          <el-table :data="accessLogs" size="small" border>
            <el-table-column prop="createTime" label="时间" width="160" />
            <el-table-column prop="username" label="操作人" width="100" />
            <el-table-column prop="action" label="动作" width="100" />
            <el-table-column prop="ip" label="IP" width="130" />
            <el-table-column label="结果" width="70">
              <template #default="{ row }"><el-tag :type="row.success ? 'success' : 'danger'" size="small">{{ row.success ? '成功' : '失败' }}</el-tag></template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="变更历史">
          <el-table :data="histories" size="small" border>
            <el-table-column prop="createTime" label="时间" width="160" />
            <el-table-column prop="changeType" label="类型" width="90" />
            <el-table-column prop="fieldName" label="字段" width="120" />
            <el-table-column prop="operatorName" label="操作人" width="100" />
            <el-table-column prop="remark" label="说明" show-overflow-tooltip />
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="附件">
          <div style="margin-bottom:10px">
            <span v-if="current.attachmentName">当前附件：<b>{{ current.attachmentName }}</b>
              <el-button link type="primary" size="small" @click="downloadAttachment">下载</el-button>
              <el-button v-if="user.canEdit" link type="danger" size="small" @click="removeAttachment">删除</el-button>
            </span>
            <span v-else class="muted">暂无附件（证书 / 私钥 / License 文件，加密存储）</span>
          </div>
          <input ref="fileInput" type="file" style="display:none" @change="uploadAttachment" />
          <el-button v-if="user.canEdit" :icon="Upload" @click="$refs.fileInput.click()">上传附件</el-button>
        </el-tab-pane>
      </el-tabs>
    </el-drawer>
  </div>
</template>

<script setup>
import { onActivated, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { CopyDocument, MagicStick, Plus, Search, Star, StarFilled, Upload, View } from '@element-plus/icons-vue'
import { authApi, credentialApi, projectApi } from '../api'
import { useDictStore } from '../store/dict'
import { useUserStore } from '../store/user'

const props = defineProps({ projectId: { type: [Number, String], default: null } })

const dict = useDictStore()
const user = useUserStore()

const loading = ref(false)
const saving = ref(false)
const rows = ref([])
const total = ref(0)
const projects = ref([])
const onlyFavorite = ref(false)
const revealed = reactive({})
const recheckToken = ref('')
const query = reactive({ page: 1, size: 20, projectId: props.projectId ? Number(props.projectId) : null, credType: '', keyword: '' })

const formVisible = ref(false)
const formRef = ref()
const emptyForm = () => ({
  id: null, projectId: props.projectId ? Number(props.projectId) : null, platformName: '', credType: 'WEB',
  envType: 'PROD', url: '', host: '', port: null, username: '', secretType: 'PASSWORD', secret: '',
  owner: '', expireDate: null, rotateDays: 180, lastRotateDate: null, favorite: false, needChange: false, remark: ''
})
const form = reactive(emptyForm())
const rules = {
  projectId: [{ required: true, message: '请选择项目', trigger: 'change' }],
  platformName: [{ required: true, message: '请输入平台/系统名称', trigger: 'blur' }]
}

const logVisible = ref(false)
const current = ref({})
const accessLogs = ref([])
const histories = ref([])
const fileInput = ref()

onMounted(async () => {
  await dict.load()
  loadProjects()
  load()
})

onActivated(loadProjects)

async function loadProjects() {
  projects.value = (await projectApi.all()).data || []
}

watch(() => props.projectId, (v) => {
  query.projectId = v ? Number(v) : null
  load()
})
watch(onlyFavorite, load)

async function load() {
  loading.value = true
  try {
    const res = await credentialApi.page({ ...query, favorite: onlyFavorite.value || null })
    rows.value = res.data.records || []
    total.value = res.data.total || 0
    Object.keys(revealed).forEach((k) => delete revealed[k])
  } finally {
    loading.value = false
  }
}

function search() {
  query.page = 1
  load()
}

function openForm(row) {
  Object.assign(form, emptyForm())
  if (row) {
    Object.keys(form).forEach((k) => { if (row[k] !== undefined && row[k] !== null) form[k] = row[k] })
    form.id = row.id
    form.secret = ''
  }
  formVisible.value = true
}

async function save() {
  await formRef.value.validate().catch(() => Promise.reject())
  saving.value = true
  try {
    if (form.id) {
      await credentialApi.update(form.id, form)
    } else {
      await credentialApi.create(form)
    }
    ElMessage.success('已保存，密码已加密存储')
    formVisible.value = false
    load()
  } catch (e) {
    // 拦截器已提示
  } finally {
    saving.value = false
  }
}

async function genPwd() {
  const res = await credentialApi.generatePassword(16)
  form.secret = res.data.password
}

async function ensureReveal(row) {
  try {
    return await credentialApi.reveal(row.id, recheckToken.value || undefined)
  } catch (e) {
    if (e.code !== 40302) {
      return null
    }
    const { value } = await ElMessageBox.prompt('查看明文需要二次密码校验', '安全校验', {
      inputType: 'password', inputPlaceholder: '请输入登录密码', confirmButtonText: '校验', cancelButtonText: '取消'
    }).catch(() => ({ value: null }))
    if (!value) return null
    const r = await authApi.recheck(value)
    recheckToken.value = r.data.recheckToken
    return credentialApi.reveal(row.id, recheckToken.value)
  }
}

async function reveal(row) {
  const res = await ensureReveal(row)
  if (!res) return
  revealed[row.id] = { username: res.data.username, secret: res.data.secret }
  ElMessage.success(`明文将在 ${res.data.autoHideSec} 秒后自动隐藏`)
  setTimeout(() => { delete revealed[row.id] }, (res.data.autoHideSec || 60) * 1000)
}

async function copySecret(row) {
  const res = await ensureReveal(row)
  if (!res) return
  await copyText(res.data.secret, row, 'secret', res.data.clipboardClearSec)
}

async function copyText(text, row, kind, clearSec = 30) {
  try {
    await navigator.clipboard.writeText(text)
  } catch (e) {
    const ta = document.createElement('textarea')
    ta.value = text
    document.body.appendChild(ta)
    ta.select()
    document.execCommand('copy')
    document.body.removeChild(ta)
  }
  if (kind === 'secret') {
    await credentialApi.copyLog(row.id).catch(() => {})
    ElMessage.success(`密码已复制，${clearSec} 秒后自动清空剪贴板`)
    setTimeout(() => { navigator.clipboard.writeText('').catch(() => {}) }, (clearSec || 30) * 1000)
  } else {
    ElMessage.success('账号已复制')
  }
}

async function toggleFav(row) {
  await credentialApi.favorite(row.id, !row.favorite)
  row.favorite = !row.favorite
}

async function markRotated(row) {
  await ElMessageBox.confirm(`确认将「${row.platformName}」标记为已改密？将按轮换周期重算下次到期日。`, '提示')
  await credentialApi.rotate(row.id)
  ElMessage.success('已更新')
  load()
}

async function remove(row) {
  await ElMessageBox.confirm(`确认删除凭据「${row.platformName}」？访问记录与变更历史会保留。`, '危险操作', { type: 'warning' })
  await credentialApi.remove(row.id)
  ElMessage.success('已删除')
  load()
}

async function openLogs(row) {
  current.value = row
  logVisible.value = true
  const [a, h] = await Promise.all([
    credentialApi.accessLogs(row.id).catch(() => ({ data: [] })),
    credentialApi.history(row.id)
  ])
  accessLogs.value = a.data || []
  histories.value = h.data || []
}

async function uploadAttachment(e) {
  const file = e.target.files?.[0]
  e.target.value = ''
  if (!file) return
  const fd = new FormData()
  fd.append('file', file)
  await credentialApi.uploadAttachment(current.value.id, fd)
  ElMessage.success('附件已加密上传')
  current.value.attachmentName = file.name
  load()
}

function downloadAttachment() {
  window.open(credentialApi.attachmentUrl(current.value.id, localStorage.getItem('pdm_token')), '_blank')
}

async function removeAttachment() {
  await ElMessageBox.confirm('确认删除该附件？', '提示', { type: 'warning' })
  await credentialApi.deleteAttachment(current.value.id)
  current.value.attachmentName = null
  ElMessage.success('已删除')
  load()
}
</script>
