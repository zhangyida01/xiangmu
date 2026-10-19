<template>
  <div class="page">
    <el-tabs v-model="tab" type="border-card">
      <el-tab-pane label="系统参数" name="config">
        <el-alert type="warning" :closable="false" show-icon class="card-gap"
                  title="cred.recheck.enabled 开启后，查看凭据明文需要再输入一次登录密码。" />
        <el-table :data="configs" border size="small" v-loading="loading">
          <el-table-column prop="cfgGroup" label="分组" width="110" />
          <el-table-column prop="cfgKey" label="参数键" width="240" />
          <el-table-column label="参数值" min-width="260">
            <template #default="{ row }">
              <el-switch v-if="row.valueType === 'BOOLEAN'" v-model="row.cfgValue" active-value="true" inactive-value="false" />
              <el-input v-else v-model="row.cfgValue" size="small" />
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="说明" min-width="200" show-overflow-tooltip />
        </el-table>
        <div style="margin-top:12px">
          <el-button type="primary" :loading="saving" @click="saveConfigs">保存全部参数</el-button>
        </div>
      </el-tab-pane>

      <el-tab-pane label="数据字典" name="dict">
        <div class="toolbar">
          <el-select v-model="dictType" style="width:200px" @change="filterDict">
            <el-option v-for="t in dictTypes" :key="t" :label="t" :value="t" />
          </el-select>
          <div class="spacer"></div>
          <el-button type="primary" size="small" :icon="Plus" @click="openDict()">新增字典项</el-button>
        </div>
        <el-table :data="filteredDict" border size="small">
          <el-table-column prop="dictType" label="类型" width="160" />
          <el-table-column prop="itemCode" label="编码" width="160" />
          <el-table-column prop="itemName" label="名称" min-width="160" />
          <el-table-column prop="sortNo" label="排序" width="80" align="center" />
          <el-table-column label="启用" width="80" align="center">
            <template #default="{ row }"><el-tag size="small" :type="row.enabled ? 'success' : 'info'">{{ row.enabled ? '是' : '否' }}</el-tag></template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" min-width="140" show-overflow-tooltip />
          <el-table-column label="操作" width="130" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" size="small" @click="openDict(row)">编辑</el-button>
              <el-button link type="danger" size="small" @click="delDict(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="交付清单模板" name="checklist">
        <div class="toolbar">
          <el-select v-model="tplType" placeholder="按项目类型筛选" clearable style="width:200px">
            <el-option label="* 通用" value="*" />
            <el-option v-for="d in dict.items('PROJECT_TYPE')" :key="d.itemCode" :label="d.itemName" :value="d.itemCode" />
          </el-select>
          <div class="spacer"></div>
          <el-button type="primary" size="small" :icon="Plus" @click="openTpl()">新增模板项</el-button>
        </div>
        <el-table :data="filteredTpl" border size="small">
          <el-table-column prop="projectType" label="项目类型" width="140" />
          <el-table-column prop="itemName" label="应交文档 / 事项" min-width="220" />
          <el-table-column label="必填" width="80" align="center">
            <template #default="{ row }"><el-tag size="small" :type="row.required ? 'danger' : 'info'">{{ row.required ? '必填' : '可选' }}</el-tag></template>
          </el-table-column>
          <el-table-column prop="sortNo" label="排序" width="80" align="center" />
          <el-table-column label="启用" width="80" align="center">
            <template #default="{ row }">{{ row.enabled ? '是' : '否' }}</template>
          </el-table-column>
          <el-table-column label="操作" width="130" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" size="small" @click="openTpl(row)">编辑</el-button>
              <el-button link type="danger" size="small" @click="delTpl(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="系统信息" name="info">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="应用">{{ info.app }}</el-descriptions-item>
          <el-descriptions-item label="版本">{{ info.version }}</el-descriptions-item>
          <el-descriptions-item label="Java 版本">{{ info.javaVersion }}</el-descriptions-item>
          <el-descriptions-item label="附件目录">{{ info.uploadDir }}</el-descriptions-item>
          <el-descriptions-item label="附件占用">{{ formatSize(info.storageUsedBytes) }}</el-descriptions-item>
        </el-descriptions>
        <el-alert style="margin-top:12px" type="info" :closable="false"
                  title="备份提醒：数据库每日备份 + 附件每周备份，主密钥 master.key 必须单独离线保存，不能与数据库备份放在一起。" />
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="dictVisible" :title="dictForm.id ? '编辑字典项' : '新增字典项'" width="460px">
      <el-form :model="dictForm" label-width="80px">
        <el-form-item label="类型">
          <el-select v-model="dictForm.dictType" filterable allow-create style="width:100%">
            <el-option v-for="t in dictTypes" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="编码"><el-input v-model="dictForm.itemCode" /></el-form-item>
        <el-form-item label="名称"><el-input v-model="dictForm.itemName" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="dictForm.sortNo" :min="0" /></el-form-item>
        <el-form-item label="启用"><el-switch v-model="dictForm.enabled" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="dictForm.remark" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dictVisible = false">取消</el-button>
        <el-button type="primary" @click="saveDict">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="tplVisible" :title="tplForm.id ? '编辑模板项' : '新增模板项'" width="460px">
      <el-form :model="tplForm" label-width="90px">
        <el-form-item label="项目类型">
          <el-select v-model="tplForm.projectType" style="width:100%">
            <el-option label="* 通用" value="*" />
            <el-option v-for="d in dict.items('PROJECT_TYPE')" :key="d.itemCode" :label="d.itemName" :value="d.itemCode" />
          </el-select>
        </el-form-item>
        <el-form-item label="名称"><el-input v-model="tplForm.itemName" /></el-form-item>
        <el-form-item label="必填"><el-switch v-model="tplForm.required" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="tplForm.sortNo" :min="0" /></el-form-item>
        <el-form-item label="启用"><el-switch v-model="tplForm.enabled" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="tplVisible = false">取消</el-button>
        <el-button type="primary" @click="saveTpl">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { checklistTemplateApi, configApi, dictApi, systemApi } from '../../api'
import { useDictStore } from '../../store/dict'

const dict = useDictStore()
const tab = ref('config')
const loading = ref(false)
const saving = ref(false)
const configs = ref([])
const dictItems = ref([])
const dictType = ref('PROJECT_TYPE')
const templates = ref([])
const tplType = ref('')
const info = ref({})

const dictTypes = computed(() => [...new Set(dictItems.value.map((i) => i.dictType))])
const filteredDict = computed(() =>
  dictType.value ? dictItems.value.filter((i) => i.dictType === dictType.value) : dictItems.value
)
const filteredTpl = computed(() =>
  tplType.value ? templates.value.filter((t) => t.projectType === tplType.value) : templates.value
)

onMounted(async () => {
  loading.value = true
  try {
    await dict.load()
    const [c, d, t, i] = await Promise.all([
      configApi.list(), dictApi.all(), checklistTemplateApi.list(), systemApi.info()
    ])
    configs.value = c.data || []
    dictItems.value = d.data || []
    templates.value = t.data || []
    info.value = i.data || {}
  } finally {
    loading.value = false
  }
})

async function saveConfigs() {
  saving.value = true
  try {
    const values = {}
    configs.value.forEach((c) => { values[c.cfgKey] = c.cfgValue == null ? '' : String(c.cfgValue) })
    await configApi.save(values)
    ElMessage.success('参数已保存并生效')
  } finally {
    saving.value = false
  }
}

function filterDict() { /* computed 自动过滤 */ }

const dictVisible = ref(false)
const dictForm = reactive({ id: null, dictType: 'PROJECT_TYPE', itemCode: '', itemName: '', sortNo: 0, enabled: true, remark: '' })
function openDict(row) {
  Object.assign(dictForm, row ? { ...row } : { id: null, dictType: dictType.value, itemCode: '', itemName: '', sortNo: 0, enabled: true, remark: '' })
  dictVisible.value = true
}
async function saveDict() {
  if (!dictForm.itemCode || !dictForm.itemName) { ElMessage.warning('请填写编码和名称'); return }
  if (dictForm.id) await dictApi.update(dictForm.id, dictForm)
  else await dictApi.create(dictForm)
  ElMessage.success('已保存')
  dictVisible.value = false
  dictItems.value = (await dictApi.all()).data || []
  dict.loaded = false
  dict.load()
}
async function delDict(row) {
  await ElMessageBox.confirm(`确认删除字典项「${row.itemName}」？`, '提示', { type: 'warning' })
  await dictApi.remove(row.id)
  dictItems.value = (await dictApi.all()).data || []
}

const tplVisible = ref(false)
const tplForm = reactive({ id: null, projectType: 'STANDARD', itemName: '', required: true, sortNo: 0, enabled: true })
function openTpl(row) {
  Object.assign(tplForm, row ? { ...row } : { id: null, projectType: tplType.value || 'STANDARD', itemName: '', required: true, sortNo: 0, enabled: true })
  tplVisible.value = true
}
async function saveTpl() {
  if (!tplForm.itemName) { ElMessage.warning('请填写名称'); return }
  if (tplForm.id) await checklistTemplateApi.update(tplForm.id, tplForm)
  else await checklistTemplateApi.create(tplForm)
  ElMessage.success('已保存')
  tplVisible.value = false
  templates.value = (await checklistTemplateApi.list()).data || []
}
async function delTpl(row) {
  await ElMessageBox.confirm(`确认删除「${row.itemName}」？`, '提示', { type: 'warning' })
  await checklistTemplateApi.remove(row.id)
  templates.value = (await checklistTemplateApi.list()).data || []
}

function formatSize(size) {
  if (!size && size !== 0) return '-'
  if (size < 1024 * 1024) return (size / 1024).toFixed(1) + ' KB'
  if (size < 1024 * 1024 * 1024) return (size / 1024 / 1024).toFixed(1) + ' MB'
  return (size / 1024 / 1024 / 1024).toFixed(2) + ' GB'
}
</script>