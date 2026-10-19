<template>
  <div>
    <div class="toolbar">
      <el-select v-if="!projectId" v-model="query.projectId" placeholder="全部项目" clearable filterable style="width: 220px">
        <el-option v-for="p in projects" :key="p.id" :label="`${p.projectNo} ${p.name}`" :value="p.id" />
      </el-select>
      <el-tree-select v-model="query.categoryId" :data="categories" clearable check-strictly
                      :props="{ label: 'name', value: 'id', children: 'children' }"
                      placeholder="文档分类" style="width: 180px" />
      <el-input v-model="query.keyword" placeholder="文档名 / 标签 / 说明" clearable style="width: 220px" @keyup.enter="search" />
      <el-button type="primary" :icon="Search" @click="search">查询</el-button>
      <div class="spacer"></div>
      <el-button v-if="user.canEdit" type="primary" :icon="Upload" @click="openUpload()">上传文档</el-button>
    </div>

    <el-table :data="rows" v-loading="loading" border stripe size="small">
      <el-table-column v-if="!projectId" prop="projectId" label="项目" width="90" />
      <el-table-column prop="name" label="文档名称" min-width="220" show-overflow-tooltip />
      <el-table-column label="分类" width="120">
        <template #default="{ row }">{{ categoryName(row.categoryId) }}</template>
      </el-table-column>
      <el-table-column prop="latestVersionNo" label="当前版本" width="90" align="center" />
      <el-table-column label="密级" width="80" align="center">
        <template #default="{ row }">
          <el-tag size="small" :type="row.secretLevel === 'SECRET' ? 'danger' : row.secretLevel === 'PUBLIC' ? 'info' : ''">
            {{ dict.label('SECRET_LEVEL', row.secretLevel) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="uploaderName" label="上传人" width="90" />
      <el-table-column prop="downloadCount" label="下载" width="60" align="center" />
      <el-table-column prop="updateTime" label="更新时间" width="160" />
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.latestVersionId" link type="primary" size="small" @click="preview(row)">预览</el-button>
          <el-button v-if="row.latestVersionId && user.can('file:download')" link type="primary" size="small"
                     @click="download(row.latestVersionId)">下载</el-button>
          <el-button link size="small" @click="openVersions(row)">版本</el-button>
          <el-button v-if="user.canEdit" link type="primary" size="small" @click="openUpload(row)">上传新版</el-button>
          <el-button v-if="user.canEdit" link size="small" @click="openEdit(row)">编辑</el-button>
          <el-button v-if="user.canEdit" link type="danger" size="small" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination v-model:current-page="query.page" v-model:page-size="query.size" :total="total"
                     :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next"
                     @size-change="load" @current-change="load" />
    </div>

    <el-dialog v-model="uploadVisible" :title="uploadForm.id ? `上传新版本 - ${uploadForm.name}` : '上传交付文档'" width="560px">
      <el-form :model="uploadForm" label-width="90px">
        <template v-if="!uploadForm.id">
          <el-form-item label="所属项目" required>
            <el-select v-model="uploadForm.projectId" filterable style="width:100%" :disabled="!!projectId">
              <el-option v-for="p in projects" :key="p.id" :label="`${p.projectNo} ${p.name}`" :value="p.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="文档名称" required><el-input v-model="uploadForm.name" /></el-form-item>
          <el-form-item label="分类">
            <el-tree-select v-model="uploadForm.categoryId" :data="categories" clearable check-strictly
                            :props="{ label: 'name', value: 'id', children: 'children' }" style="width:100%" />
          </el-form-item>
          <el-form-item label="密级">
            <el-radio-group v-model="uploadForm.secretLevel">
              <el-radio-button v-for="d in dict.items('SECRET_LEVEL')" :key="d.itemCode" :value="d.itemCode">{{ d.itemName }}</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="标签"><el-input v-model="uploadForm.tags" placeholder="逗号分隔" /></el-form-item>
          <el-form-item label="说明"><el-input v-model="uploadForm.remark" type="textarea" :rows="2" /></el-form-item>
        </template>
        <el-form-item label="版本说明"><el-input v-model="uploadForm.description" placeholder="本次版本的变更说明" /></el-form-item>
        <el-form-item label="版本号"><el-input v-model="uploadForm.versionNo" placeholder="留空自动生成" /></el-form-item>
        <el-form-item label="选择文件" required>
          <input ref="fileInput" type="file" @change="onFileChange" />
          <div v-if="pickedFile" class="muted" style="margin-top:4px">
            {{ pickedFile.name }}（{{ formatSize(pickedFile.size) }}）
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="uploadVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitUpload">上传</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="editVisible" title="编辑文档信息" width="480px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="名称"><el-input v-model="editForm.name" /></el-form-item>
        <el-form-item label="分类">
          <el-tree-select v-model="editForm.categoryId" :data="categories" clearable check-strictly
                          :props="{ label: 'name', value: 'id', children: 'children' }" style="width:100%" />
        </el-form-item>
        <el-form-item label="密级">
          <el-select v-model="editForm.secretLevel" style="width:100%">
            <el-option v-for="d in dict.items('SECRET_LEVEL')" :key="d.itemCode" :label="d.itemName" :value="d.itemCode" />
          </el-select>
        </el-form-item>
        <el-form-item label="标签"><el-input v-model="editForm.tags" /></el-form-item>
        <el-form-item label="说明"><el-input v-model="editForm.remark" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEdit">保存</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="versionVisible" :title="`版本历史 - ${current.name || ''}`" size="640px">
      <el-table :data="versions" size="small" border>
        <el-table-column prop="versionNo" label="版本" width="80" />
        <el-table-column prop="fileName" label="文件名" min-width="180" show-overflow-tooltip />
        <el-table-column label="大小" width="90">
          <template #default="{ row }">{{ formatSize(row.fileSize) }}</template>
        </el-table-column>
        <el-table-column prop="uploaderName" label="上传人" width="90" />
        <el-table-column prop="createTime" label="上传时间" width="160" />
        <el-table-column prop="description" label="说明" min-width="120" show-overflow-tooltip />
        <el-table-column label="操作" width="170" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="download(row.id)">下载</el-button>
            <el-button v-if="user.canEdit && row.id !== current.latestVersionId" link type="warning" size="small"
                       @click="rollback(row)">回滚</el-button>
            <el-button v-if="user.isAdmin && row.id !== current.latestVersionId" link type="danger" size="small"
                       @click="removeVersion(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-drawer>
  </div>
</template>

<script setup>
import { onActivated, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Upload } from '@element-plus/icons-vue'
import { docApi, projectApi } from '../api'
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
const categories = ref([])
const query = reactive({ page: 1, size: 20, projectId: props.projectId ? Number(props.projectId) : null, categoryId: null, keyword: '' })

const uploadVisible = ref(false)
const uploadForm = reactive({ id: null, projectId: null, name: '', categoryId: null, secretLevel: 'INTERNAL', tags: '', remark: '', description: '', versionNo: '' })
const fileInput = ref()
const pickedFile = ref(null)

const editVisible = ref(false)
const editForm = reactive({ id: null, name: '', categoryId: null, secretLevel: 'INTERNAL', tags: '', remark: '' })

const versionVisible = ref(false)
const versions = ref([])
const current = ref({})

onMounted(async () => {
  await dict.load()
  loadOptions()
  load()
})

onActivated(loadOptions)

async function loadOptions() {
  const [p, c] = await Promise.all([projectApi.all(), docApi.categoryTree()])
  projects.value = p.data || []
  categories.value = c.data || []
}

watch(() => props.projectId, (v) => {
  query.projectId = v ? Number(v) : null
  load()
})

async function load() {
  loading.value = true
  try {
    const res = await docApi.page({ ...query })
    rows.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

function search() {
  query.page = 1
  load()
}

function categoryName(id) {
  const walk = (nodes) => {
    for (const n of nodes || []) {
      if (n.id === id) return n.name
      const hit = walk(n.children)
      if (hit) return hit
    }
    return ''
  }
  return walk(categories.value) || '-'
}

function formatSize(size) {
  if (!size) return '-'
  if (size < 1024) return size + ' B'
  if (size < 1024 * 1024) return (size / 1024).toFixed(1) + ' KB'
  return (size / 1024 / 1024).toFixed(1) + ' MB'
}

function openUpload(row) {
  pickedFile.value = null
  Object.assign(uploadForm, {
    id: row?.id || null,
    projectId: row?.projectId || (props.projectId ? Number(props.projectId) : null),
    name: row?.name || '',
    categoryId: row?.categoryId || null,
    secretLevel: row?.secretLevel || 'INTERNAL',
    tags: row?.tags || '',
    remark: row?.remark || '',
    description: '',
    versionNo: ''
  })
  uploadVisible.value = true
}

function onFileChange(e) {
  pickedFile.value = e.target.files?.[0] || null
  if (pickedFile.value && !uploadForm.name && !uploadForm.id) {
    uploadForm.name = pickedFile.value.name.replace(/\.[^.]+$/, '')
  }
}

async function submitUpload() {
  if (!pickedFile.value) {
    ElMessage.warning('请选择文件')
    return
  }
  saving.value = true
  try {
    let docId = uploadForm.id
    if (!docId) {
      if (!uploadForm.projectId) {
        ElMessage.warning('请选择所属项目')
        return
      }
      if (!uploadForm.name) {
        ElMessage.warning('请填写文档名称')
        return
      }
      const created = await docApi.create({
        projectId: uploadForm.projectId, name: uploadForm.name, categoryId: uploadForm.categoryId,
        secretLevel: uploadForm.secretLevel, tags: uploadForm.tags, remark: uploadForm.remark
      })
      docId = created.data
    }
    const fd = new FormData()
    fd.append('file', pickedFile.value)
    if (uploadForm.description) fd.append('description', uploadForm.description)
    if (uploadForm.versionNo) fd.append('versionNo', uploadForm.versionNo)
    await docApi.uploadVersion(docId, fd)
    ElMessage.success('上传成功')
    uploadVisible.value = false
    load()
  } catch (e) {
    // 拦截器已提示
  } finally {
    saving.value = false
  }
}

function openEdit(row) {
  Object.assign(editForm, {
    id: row.id, name: row.name, categoryId: row.categoryId,
    secretLevel: row.secretLevel, tags: row.tags, remark: row.remark
  })
  editVisible.value = true
}

async function submitEdit() {
  await docApi.update(editForm.id, { ...editForm, projectId: props.projectId ? Number(props.projectId) : undefined })
  ElMessage.success('已保存')
  editVisible.value = false
  load()
}

async function remove(row) {
  await ElMessageBox.confirm(`确认删除文档「${row.name}」及其全部版本记录？物理文件会保留在磁盘上等待清理。`, '危险操作', { type: 'warning' })
  await docApi.remove(row.id)
  ElMessage.success('已删除')
  load()
}

async function openVersions(row) {
  current.value = row
  const res = await docApi.detail(row.id)
  versions.value = res.data.versions || []
  versionVisible.value = true
}

async function rollback(v) {
  await ElMessageBox.confirm(`确认回滚到 ${v.versionNo}？系统会以该文件生成一个新版本，历史不会丢失。`, '提示')
  await docApi.rollback(current.value.id, v.id)
  ElMessage.success('已回滚')
  versionVisible.value = false
  load()
}

async function removeVersion(v) {
  await ElMessageBox.confirm(`确认删除版本 ${v.versionNo}？`, '危险操作', { type: 'warning' })
  await docApi.removeVersion(v.id)
  ElMessage.success('已删除')
  openVersions(current.value)
}

function download(versionId) {
  window.open(docApi.downloadUrl(versionId, localStorage.getItem('pdm_token')), '_blank')
}

function preview(row) {
  window.open(docApi.previewUrl(row.latestVersionId, localStorage.getItem('pdm_token')), '_blank')
}
</script>
