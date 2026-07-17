<template>
  <div class="ticket-container">
    <el-card>
      <div class="toolbar">
        <el-select v-model="queryParams.status" placeholder="状态" style="width: 150px" clearable @change="loadTickets">
          <el-option label="待处理" :value="0" />
          <el-option label="处理中" :value="1" />
          <el-option label="已解决" :value="2" />
          <el-option label="已关闭" :value="3" />
        </el-select>
        <el-select v-model="queryParams.priority" placeholder="优先级" style="width: 150px" clearable @change="loadTickets">
          <el-option label="高" :value="1" />
          <el-option label="中" :value="2" />
          <el-option label="低" :value="3" />
        </el-select>
        <el-button type="primary" :icon="Search" @click="loadTickets">搜索</el-button>
        <el-button type="primary" :icon="Plus" @click="handleAdd">创建工单</el-button>
      </div>

      <el-table :data="ticketList" style="width: 100%; margin-top: 20px" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="ticketCode" label="工单编号" width="160" />
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column prop="priority" label="优先级" width="100">
          <template #default="{ row }">
            <el-tag :type="priorityTypes[row.priority]">{{ priorityTexts[row.priority] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTypes[row.status]">{{ statusTexts[row.status] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="slaDeadline" label="SLA截止" width="180" />
        <el-table-column prop="resolveTime" label="解决时间" width="180" />
        <el-table-column prop="satisfaction" label="满意度" width="100">
          <template #default="{ row }">
            <el-rate v-if="row.satisfaction" :model-value="row.satisfaction" disabled />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="240">
          <template #default="{ row }">
            <el-button type="success" size="small" @click="handleResolve(row)" v-if="row.status < 2">处理</el-button>
            <el-button type="primary" size="small" :icon="Edit" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" size="small" :icon="Delete" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="queryParams.page"
        v-model:page-size="queryParams.size"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadTickets"
        @current-change="loadTickets"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="项目ID" prop="projectId">
          <el-input-number v-model="form.projectId" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="问题描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-select v-model="form.priority" style="width: 100%">
            <el-option label="高" :value="1" />
            <el-option label="中" :value="2" />
            <el-option label="低" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="提交人ID">
          <el-input-number v-model="form.reporterId" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="处理人ID">
          <el-input-number v-model="form.assigneeId" :min="1" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="resolveDialogVisible" title="处理工单" width="500px">
      <el-form :model="resolveForm" label-width="100px">
        <el-form-item label="满意度">
          <el-rate v-model="resolveForm.satisfaction" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resolveDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitResolve" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getTicketList, addTicket, updateTicket, deleteTicket, resolveTicket } from '../api/ticket'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Edit, Delete } from '@element-plus/icons-vue'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const resolveDialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const ticketList = ref([])
const total = ref(0)
const formRef = ref()

const priorityTexts = { 1: '高', 2: '中', 3: '低' }
const priorityTypes = { 1: 'danger', 2: 'warning', 3: 'info' }
const statusTexts = { 0: '待处理', 1: '处理中', 2: '已解决', 3: '已关闭' }
const statusTypes = { 0: 'danger', 1: 'warning', 2: 'success', 3: 'info' }

const queryParams = reactive({
  page: 1,
  size: 10,
  status: null,
  priority: null
})

const form = reactive({
  id: null,
  projectId: 1,
  title: '',
  description: '',
  priority: 2,
  reporterId: 1,
  assigneeId: 1
})

const resolveForm = reactive({
  id: null,
  satisfaction: 5
})

const rules = {
  projectId: [{ required: true, message: '请输入项目ID', trigger: 'blur' }],
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  description: [{ required: true, message: '请输入问题描述', trigger: 'blur' }]
}

const loadTickets = async () => {
  loading.value = true
  try {
    const res = await getTicketList(queryParams)
    ticketList.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '创建工单'
  Object.assign(form, {
    id: null,
    projectId: 1,
    title: '',
    description: '',
    priority: 2,
    reporterId: 1,
    assigneeId: 1
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑工单'
  Object.assign(form, { ...row })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (isEdit.value) {
          await updateTicket(form)
          ElMessage.success('更新成功')
        } else {
          await addTicket(form)
          ElMessage.success('创建成功')
        }
        dialogVisible.value = false
        loadTickets()
      } catch (error) {
        console.error(error)
      } finally {
        submitLoading.value = false
      }
    }
  })
}

const handleResolve = (row) => {
  resolveForm.id = row.id
  resolveForm.satisfaction = 5
  resolveDialogVisible.value = true
}

const submitResolve = async () => {
  submitLoading.value = true
  try {
    await resolveTicket(resolveForm)
    ElMessage.success('工单已处理')
    resolveDialogVisible.value = false
    loadTickets()
  } catch (error) {
    console.error(error)
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该工单吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteTicket(row.id)
      ElMessage.success('删除成功')
      loadTickets()
    } catch (error) {
      console.error(error)
    }
  })
}

onMounted(() => {
  loadTickets()
})
</script>

<style scoped>
.toolbar {
  display: flex;
  gap: 12px;
  align-items: center;
}
</style>
