<template>
  <div class="account-container">
    <el-card>
      <div class="toolbar">
        <el-select v-model="currentProjectId" placeholder="选择项目" style="width: 300px" filterable @change="loadAccounts">
          <el-option v-for="project in projectOptions" :key="project.id" :label="project.projectName" :value="project.id" />
        </el-select>
        <el-button type="primary" :icon="Plus" @click="handleAdd" :disabled="!currentProjectId">添加账户</el-button>
      </div>

      <el-table :data="accountList" style="width: 100%; margin-top: 20px" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="accountName" label="账户名称" min-width="150" />
        <el-table-column prop="supplierId" label="供应商" width="150">
          <template #default="{ row }">
            <el-button v-if="row.supplierId" type="text" @click="handleViewSupplier(row.supplierId)">
              {{ getSupplierName(row.supplierId) }}
            </el-button>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="accountType" label="账户类型" width="120">
          <template #default="{ row }">
            <el-tag :type="accountTypeColors[row.accountType]">{{ accountTypes[row.accountType] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="password" label="密码" width="150">
          <template #default="{ row }">
            <span v-if="!showPasswordMap[row.id]">******</span>
            <span v-else>{{ row.password }}</span>
            <el-button type="text" :icon="showPasswordMap[row.id] ? Hide : View" @click="togglePassword(row.id)" />
          </template>
        </el-table-column>
        <el-table-column prop="loginUrl" label="登录地址" min-width="200" show-overflow-tooltip />
        <el-table-column prop="creatorName" label="创建人" width="100" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link :icon="Edit" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link :icon="Delete" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadAccounts"
        @current-change="loadAccounts"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" @close="resetForm">
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px">
        <el-form-item label="账户名称" prop="accountName">
          <el-input v-model="formData.accountName" placeholder="请输入账户名称" />
        </el-form-item>
        <el-form-item label="供应商" prop="supplierId">
          <el-select v-model="formData.supplierId" placeholder="请选择供应商" filterable clearable style="width: 100%">
            <el-option v-for="supplier in supplierOptions" :key="supplier.id" :label="supplier.supplierName" :value="supplier.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="账户类型" prop="accountType">
          <el-select v-model="formData.accountType" placeholder="请选择账户类型" style="width: 100%">
            <el-option label="数据库" value="DATABASE" />
            <el-option label="系统管理" value="SYSTEM" />
            <el-option label="应用" value="APPLICATION" />
            <el-option label="云服务" value="CLOUD" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="用户名" prop="username">
          <el-input v-model="formData.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="formData.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="登录地址" prop="loginUrl">
          <el-input v-model="formData.loginUrl" placeholder="请输入登录地址" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="formData.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="contactDialogVisible" title="供应商联系人" width="800px">
      <el-table :data="currentContacts" style="width: 100%">
        <el-table-column prop="contactName" label="姓名" width="120" />
        <el-table-column prop="contactRole" label="职位" width="150" />
        <el-table-column prop="contactPhone" label="电话" width="150" />
        <el-table-column prop="contactEmail" label="邮箱" min-width="180" />
        <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
      </el-table>
      <template #footer>
        <el-button type="primary" @click="contactDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, View, Hide } from '@element-plus/icons-vue'
import request from '@/utils/request'

const loading = ref(false)
const accountList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const currentProjectId = ref(null)
const projectOptions = ref([])
const supplierOptions = ref([])

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)
const formData = reactive({
  id: null,
  projectId: null,
  accountName: '',
  supplierId: null,
  accountType: '',
  username: '',
  password: '',
  loginUrl: '',
  remark: ''
})

const rules = {
  accountName: [{ required: true, message: '请输入账户名称', trigger: 'blur' }],
  accountType: [{ required: true, message: '请选择账户类型', trigger: 'change' }],
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const accountTypes = {
  DATABASE: '数据库',
  SYSTEM: '系统管理',
  APPLICATION: '应用',
  CLOUD: '云服务',
  OTHER: '其他'
}

const accountTypeColors = {
  DATABASE: 'success',
  SYSTEM: 'warning',
  APPLICATION: 'primary',
  CLOUD: 'info',
  OTHER: ''
}

const showPasswordMap = ref({})

const contactDialogVisible = ref(false)
const currentContacts = ref([])

const togglePassword = (id) => {
  showPasswordMap.value[id] = !showPasswordMap.value[id]
}

const loadProjects = async () => {
  try {
    const res = await request.get('/project/list', {
      params: { current: 1, size: 1000 }
    })
    if (res.code === 200) {
      projectOptions.value = res.data.records || []
    }
  } catch (error) {
    ElMessage.error('加载项目列表失败')
  }
}

const loadSuppliers = async () => {
  try {
    const res = await request.get('/supplier/list', {
      params: { current: 1, size: 1000 }
    })
    if (res.code === 200) {
      supplierOptions.value = res.data.records || []
    }
  } catch (error) {
    ElMessage.error('加载供应商列表失败')
  }
}

const loadAccounts = async () => {
  if (!currentProjectId.value) {
    accountList.value = []
    total.value = 0
    return
  }
  
  loading.value = true
  try {
    const res = await request.get('/project-account/list', {
      params: {
        projectId: currentProjectId.value,
        current: currentPage.value,
        size: pageSize.value
      }
    })
    if (res.code === 200) {
      accountList.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    ElMessage.error('加载账户列表失败')
  } finally {
    loading.value = false
  }
}

const getSupplierName = (supplierId) => {
  const supplier = supplierOptions.value.find(s => s.id === supplierId)
  return supplier ? supplier.supplierName : '-'
}

const handleViewSupplier = async (supplierId) => {
  try {
    const res = await request.get(`/supplier-contact/supplier/${supplierId}`)
    if (res.code === 200) {
      currentContacts.value = res.data || []
      if (currentContacts.value.length === 0) {
        ElMessage.info('该供应商暂无联系人')
        return
      }
      contactDialogVisible.value = true
    }
  } catch (error) {
    ElMessage.error('加载供应商联系人失败')
  }
}

const handleAdd = () => {
  dialogTitle.value = '添加账户'
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑账户'
  Object.assign(formData, row)
  dialogVisible.value = true
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确认删除该账户吗?', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await request.delete(`/project-account/${id}`)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadAccounts()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    try {
      formData.projectId = currentProjectId.value
      
      const res = formData.id
        ? await request.put('/project-account', formData)
        : await request.post('/project-account', formData)
      
      if (res.code === 200) {
        ElMessage.success(formData.id ? '修改成功' : '添加成功')
        dialogVisible.value = false
        loadAccounts()
      }
    } catch (error) {
      ElMessage.error(formData.id ? '修改失败' : '添加失败')
    }
  })
}

const resetForm = () => {
  Object.assign(formData, {
    id: null,
    projectId: null,
    accountName: '',
    supplierId: null,
    accountType: '',
    username: '',
    password: '',
    loginUrl: '',
    remark: ''
  })
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

onMounted(() => {
  loadProjects()
  loadSuppliers()
})
</script>

<style scoped>
.account-container {
  padding: 20px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
</style>