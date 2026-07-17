<template>
  <div class="project-container">
    <el-card>
      <div class="toolbar">
        <el-input v-model="queryParams.keyword" placeholder="搜索项目名称/编号" style="width: 300px" clearable @keyup.enter="loadProjects" />
        <el-select v-model="queryParams.status" placeholder="状态" style="width: 150px" clearable @change="loadProjects">
          <el-option label="立项" :value="0" />
          <el-option label="进行中" :value="1" />
          <el-option label="已验收" :value="2" />
          <el-option label="已关闭" :value="3" />
        </el-select>
        <el-button type="primary" :icon="Search" @click="loadProjects">搜索</el-button>
        <el-button type="primary" :icon="Plus" @click="handleAdd">创建项目</el-button>
      </div>

      <el-table :data="projectList" style="width: 100%; margin-top: 20px" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="projectCode" label="项目编号" width="150" />
        <el-table-column prop="projectName" label="项目名称" min-width="200" />
        <el-table-column prop="contractNo" label="合同编号" width="150" />
        <el-table-column prop="contractAmount" label="合同金额" width="120">
          <template #default="{ row }">
            {{ row.contractAmount ? '¥' + row.contractAmount.toLocaleString() : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="startDate" label="开始日期" width="120" />
        <el-table-column prop="endDate" label="结束日期" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTypes[row.status]">{{ statusTexts[row.status] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="180">
          <template #default="{ row }">
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
        @size-change="loadProjects"
        @current-change="loadProjects"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="项目编号" prop="projectCode">
          <el-input v-model="form.projectCode" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="项目名称" prop="projectName">
          <el-input v-model="form.projectName" />
        </el-form-item>
        <el-form-item label="客户ID" prop="customerId">
          <el-input-number v-model="form.customerId" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="合同编号">
          <el-input v-model="form.contractNo" />
        </el-form-item>
        <el-form-item label="合同金额">
          <el-input-number v-model="form.contractAmount" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="开始日期">
          <el-date-picker v-model="form.startDate" type="date" placeholder="选择日期" format="YYYY-MM-DD" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker v-model="form.endDate" type="date" placeholder="选择日期" format="YYYY-MM-DD" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="项目经理">
          <el-input-number v-model="form.projectManagerId" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width: 100%">
            <el-option label="立项" :value="0" />
            <el-option label="进行中" :value="1" />
            <el-option label="已验收" :value="2" />
            <el-option label="已关闭" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="项目描述">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getProjectList, addProject, updateProject, deleteProject } from '../api/project'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Edit, Delete } from '@element-plus/icons-vue'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const projectList = ref([])
const total = ref(0)
const formRef = ref()

const statusTexts = ['立项', '进行中', '已验收', '已关闭']
const statusTypes = ['info', 'warning', 'success', 'danger']

const queryParams = reactive({
  page: 1,
  size: 10,
  keyword: '',
  status: null
})

const form = reactive({
  id: null,
  projectCode: '',
  projectName: '',
  customerId: 1,
  contractNo: '',
  contractAmount: 0,
  startDate: '',
  endDate: '',
  projectManagerId: 1,
  status: 0,
  description: ''
})

const rules = {
  projectCode: [{ required: true, message: '请输入项目编号', trigger: 'blur' }],
  projectName: [{ required: true, message: '请输入项目名称', trigger: 'blur' }],
  customerId: [{ required: true, message: '请输入客户ID', trigger: 'blur' }]
}

const loadProjects = async () => {
  loading.value = true
  try {
    const res = await getProjectList(queryParams)
    projectList.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '创建项目'
  Object.assign(form, {
    id: null,
    projectCode: 'PRJ' + Date.now(),
    projectName: '',
    customerId: 1,
    contractNo: '',
    contractAmount: 0,
    startDate: '',
    endDate: '',
    projectManagerId: 1,
    status: 0,
    description: ''
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑项目'
  Object.assign(form, { ...row })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (isEdit.value) {
          await updateProject(form)
          ElMessage.success('更新成功')
        } else {
          await addProject(form)
          ElMessage.success('创建成功')
        }
        dialogVisible.value = false
        loadProjects()
      } catch (error) {
        console.error(error)
      } finally {
        submitLoading.value = false
      }
    }
  })
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该项目吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteProject(row.id)
      ElMessage.success('删除成功')
      loadProjects()
    } catch (error) {
      console.error(error)
    }
  })
}

onMounted(() => {
  loadProjects()
})
</script>

<style scoped>
.toolbar {
  display: flex;
  gap: 12px;
  align-items: center;
}
</style>
