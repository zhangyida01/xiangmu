<template>
  <div class="page">
    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="项目名称 / 编号 / 合同号 / 标签" clearable style="width: 260px"
                @keyup.enter="search" />
      <el-select v-model="query.status" placeholder="状态" clearable style="width: 130px">
        <el-option v-for="d in dict.items('PROJECT_STATUS')" :key="d.itemCode" :label="d.itemName" :value="d.itemCode" />
      </el-select>
      <el-select v-model="query.projectType" placeholder="类型" clearable style="width: 150px">
        <el-option v-for="d in dict.items('PROJECT_TYPE')" :key="d.itemCode" :label="d.itemName" :value="d.itemCode" />
      </el-select>
      <el-select v-model="query.customerId" placeholder="客户" clearable filterable style="width: 180px">
        <el-option v-for="c in customers" :key="c.id" :label="c.name" :value="c.id" />
      </el-select>
      <el-select v-model="query.archived" placeholder="归档" clearable style="width: 110px">
        <el-option label="未归档" :value="false" />
        <el-option label="已归档" :value="true" />
      </el-select>
      <el-button type="primary" :icon="Search" @click="search">查询</el-button>
      <el-button :icon="Refresh" @click="reset">重置</el-button>
      <div class="spacer"></div>
      <el-button v-if="user.canEdit" type="primary" :icon="Plus" @click="openForm()">新建项目</el-button>
    </div>

    <el-table :data="rows" v-loading="loading" border stripe size="small">
      <el-table-column prop="projectNo" label="项目编号" width="130" fixed />
      <el-table-column prop="name" label="项目名称" min-width="200" show-overflow-tooltip fixed>
        <template #default="{ row }">
          <el-link type="primary" @click="$router.push(`/projects/${row.id}`)">{{ row.name }}</el-link>
          <el-tag v-if="row.archived" size="small" type="info" style="margin-left:6px">已归档</el-tag>
          <el-tag v-if="row.locked" size="small" type="warning" style="margin-left:4px">锁定</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="customerName" label="客户" width="160" show-overflow-tooltip />
      <el-table-column label="类型" width="120">
        <template #default="{ row }">{{ dict.label('PROJECT_TYPE', row.projectType) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag size="small" :type="statusType(row.status)">{{ dict.label('PROJECT_STATUS', row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="健康度" width="80" align="center">
        <template #default="{ row }">
          <span :class="healthClass(row.health)">●</span>
        </template>
      </el-table-column>
      <el-table-column prop="manager" label="项目经理" width="100" />
      <el-table-column label="进度" width="120">
        <template #default="{ row }"><el-progress :percentage="row.progress || 0" :stroke-width="10" /></template>
      </el-table-column>
      <el-table-column label="文档" width="80" align="center">
        <template #default="{ row }">
          <span :class="(row.docRate || 0) < 100 ? 'warn-text' : 'ok-text'">{{ row.docRate || 0 }}%</span>
        </template>
      </el-table-column>
      <el-table-column prop="planAcceptDate" label="计划验收" width="110" sortable />
      <el-table-column label="操作" width="230" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="$router.push(`/projects/${row.id}`)">详情</el-button>
          <el-button v-if="user.canEdit" link type="primary" size="small" @click="openForm(row)">编辑</el-button>
          <el-button v-if="user.canEdit" link type="warning" size="small" @click="openCopy(row)">复制</el-button>
          <el-button v-if="user.canEdit" link size="small" @click="toggleArchive(row)">
            {{ row.archived ? '取消归档' : '归档' }}
          </el-button>
          <el-button v-if="user.can('project:delete')" link type="danger" size="small" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination v-model:current-page="query.page" v-model:page-size="query.size" :total="total"
                     :page-sizes="[10, 20, 50, 100]" layout="total, sizes, prev, pager, next, jumper"
                     @size-change="load" @current-change="load" />
    </div>

    <el-dialog v-model="formVisible" :title="form.id ? '编辑项目' : '新建项目'" width="820px" top="6vh">
      <el-form :model="form" label-width="100px" :rules="rules" ref="formRef">
        <el-row :gutter="12">
          <el-col :span="12"><el-form-item label="项目编号" prop="projectNo"><el-input v-model="form.projectNo" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="项目名称" prop="name"><el-input v-model="form.name" /></el-form-item></el-col>
          <el-col :span="12">
            <el-form-item label="客户">
              <el-select v-model="form.customerId" filterable clearable style="width:100%">
                <el-option v-for="c in customers" :key="c.id" :label="c.name" :value="c.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="项目类型">
              <el-select v-model="form.projectType" style="width:100%">
                <el-option v-for="d in dict.items('PROJECT_TYPE')" :key="d.itemCode" :label="d.itemName" :value="d.itemCode" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-select v-model="form.status" style="width:100%">
                <el-option v-for="d in dict.items('PROJECT_STATUS')" :key="d.itemCode" :label="d.itemName" :value="d.itemCode" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="健康度">
              <el-radio-group v-model="form.health">
                <el-radio-button value="GREEN">正常</el-radio-button>
                <el-radio-button value="YELLOW">风险</el-radio-button>
                <el-radio-button value="RED">告急</el-radio-button>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="8"><el-form-item label="项目经理"><el-input v-model="form.manager" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="销售负责人"><el-input v-model="form.sales" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="交付负责人"><el-input v-model="form.deliveryOwner" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="合同编号"><el-input v-model="form.contractNo" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="合同金额"><el-input-number v-model="form.amount" :min="0" :precision="2" style="width:100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="进度%"><el-input-number v-model="form.progress" :min="0" :max="100" style="width:100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="立项日期"><el-date-picker v-model="form.setupDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="启动日期"><el-date-picker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="计划上线"><el-date-picker v-model="form.planOnlineDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="实际上线"><el-date-picker v-model="form.actualOnlineDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="计划验收"><el-date-picker v-model="form.planAcceptDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="实际验收"><el-date-picker v-model="form.actualAcceptDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="质保到期"><el-date-picker v-model="form.warrantyEndDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="标签"><el-input v-model="form.tags" placeholder="多个标签用逗号分隔" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="项目简介"><el-input v-model="form.summary" type="textarea" :rows="3" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item></el-col>
        </el-row>
        <el-form-item v-if="!form.id" label="交付清单">
          <el-switch v-model="withChecklist" active-text="按项目类型自动生成交付文档清单" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="copyVisible" title="复制项目" width="420px">
      <el-form label-width="100px">
        <el-form-item label="源项目"><span>{{ copySource.name }}</span></el-form-item>
        <el-form-item label="新项目编号"><el-input v-model="copyForm.projectNo" /></el-form-item>
        <el-form-item label="新项目名称"><el-input v-model="copyForm.name" /></el-form-item>
        <el-form-item label="复制环境"><el-switch v-model="copyForm.copyEnv" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="copyVisible = false">取消</el-button>
        <el-button type="primary" @click="doCopy">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onActivated, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, Search } from '@element-plus/icons-vue'
import { customerApi, projectApi } from '../api'
import { useDictStore } from '../store/dict'
import { useUserStore } from '../store/user'

const router = useRouter()
const dict = useDictStore()
const user = useUserStore()

const loading = ref(false)
const saving = ref(false)
const rows = ref([])
const total = ref(0)
const customers = ref([])
const query = reactive({ page: 1, size: 20, keyword: '', status: '', projectType: '', customerId: null, archived: null })

const formVisible = ref(false)
const formRef = ref()
const withChecklist = ref(true)
const emptyForm = () => ({
  id: null, projectNo: '', name: '', customerId: null, projectType: 'STANDARD', status: 'PRESALE',
  health: 'GREEN', manager: '', sales: '', deliveryOwner: '', contractNo: '', amount: null, tags: '',
  summary: '', remark: '', progress: 0, setupDate: null, startDate: null, planOnlineDate: null,
  actualOnlineDate: null, planAcceptDate: null, actualAcceptDate: null, warrantyEndDate: null
})
const form = reactive(emptyForm())
const rules = {
  projectNo: [{ required: true, message: '请输入项目编号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入项目名称', trigger: 'blur' }]
}

const copyVisible = ref(false)
const copySource = ref({})
const copyForm = reactive({ projectNo: '', name: '', copyEnv: true })

onMounted(async () => {
  await dict.load()
  loadCustomers()
  load()
})
onActivated(() => {
  loadCustomers()
  load()
})

async function loadCustomers() {
  customers.value = (await customerApi.all()).data || []
}

async function load() {
  loading.value = true
  try {
    const res = await projectApi.page({ ...query })
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

function reset() {
  Object.assign(query, { page: 1, keyword: '', status: '', projectType: '', customerId: null, archived: null })
  load()
}

function openForm(row) {
  Object.assign(form, emptyForm())
  if (row) {
    Object.keys(form).forEach((k) => { if (row[k] !== undefined) form[k] = row[k] })
  }
  formVisible.value = true
}

async function save() {
  await formRef.value.validate().catch(() => Promise.reject())
  saving.value = true
  try {
    if (form.id) {
      await projectApi.update(form.id, form)
      ElMessage.success('已保存')
    } else {
      await projectApi.create(form, withChecklist.value)
      ElMessage.success('已创建')
    }
    formVisible.value = false
    load()
  } catch (e) {
    // 错误提示由拦截器处理
  } finally {
    saving.value = false
  }
}

function openCopy(row) {
  copySource.value = row
  copyForm.projectNo = row.projectNo + '-COPY'
  copyForm.name = row.name + '（复制）'
  copyForm.copyEnv = true
  copyVisible.value = true
}

async function doCopy() {
  await projectApi.copy(copySource.value.id, { ...copyForm })
  ElMessage.success('已复制')
  copyVisible.value = false
  load()
}

async function toggleArchive(row) {
  await projectApi.archive(row.id, !row.archived)
  ElMessage.success(row.archived ? '已取消归档' : '已归档')
  load()
}

async function remove(row) {
  await ElMessageBox.confirm(`确认删除项目「${row.name}」？该项目下的成员、环境、清单会一并删除，文档与凭据记录保留。`, '危险操作', { type: 'warning' })
  await projectApi.remove(row.id)
  ElMessage.success('已删除')
  load()
}

function statusType(s) {
  return { PRESALE: 'info', SETUP: '', DOING: 'warning', TO_ACCEPT: 'warning', ACCEPTED: 'success', OM: 'success', ARCHIVED: 'info', TERMINATED: 'danger' }[s] || ''
}

function healthClass(h) {
  return { GREEN: 'ok-text', YELLOW: 'warn-text', RED: 'danger-text' }[h] || 'muted'
}
</script>
