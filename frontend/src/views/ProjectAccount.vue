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
            <el-button type="text" size="small" @click="togglePassword(row)" style="margin-left: 8px">
              <el-icon><View v-if="!showPasswordMap[row.id]" /><Hide v-else /></el-icon>
            </el-button>
          </template>
        </el-table-column>
        <el-table-column prop="accessUrl" label="访问地址" min-width="200" />
        <el-table-column prop="port" label="端口" width="80" />
        <el-table-column prop="environment" label="环境" width="100">
          <template #default="{ row }">
            <el-tag :type="environmentColors[row.environment]">{{ environments[row.environment] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" fixed="right" width="180">
          <template #default="{ row }">
            <el-button type="primary" size="small" :icon="Edit" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" size="small" :icon="Delete" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="账户名称" prop="accountName">
          <el-input v-model="form.accountName" placeholder="例如：生产数据库" />
        </el-form-item>
        <el-form-item label="供应商">
          <el-select v-model="form.supplierId" placeholder="选择供应商（选填）" style="width: 100%" filterable clearable>
            <el-option v-for="supplier in supplierOptions" :key="supplier.id" :label="supplier.supplierName" :value="supplier.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="账户类型" prop="accountType">
          <el-select v-model="form.accountType" style="width: 100%">
            <el-option v-for="(name, value) in accountTypes" :key="value" :label="name" :value="parseInt(value)" />
          </el-select>
        </el-form-item>
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="访问地址">
          <el-input v-model="form.accessUrl" placeholder="例如：https://example.com 或 192.168.1.100" />
        </el-form-item>
        <el-form-item label="端口">
          <el-input-number v-model="form.port" :min="1" :max="65535" style="width: 100%" />
        </el-form-item>
        <el-form-item label="环境类型" prop="environment">
          <el-select v-model="form.environment" style="width: 100%">
            <el-option v-for="(name, value) in environments" :key="value" :label="name" :value="parseInt(value)" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="supplierContactDialogVisible" :title="currentSupplier.supplierName + ' - 联系人'" width="800px">
      <el-table :data="supplierContactList" v-loading="contactLoading">
        <el-table-column prop="contactName" label="姓名" width="120" />
        <el-table-column prop="position" label="职位" width="120" />
        <el-table-column prop="phone" label="电话" width="140" />
        <el-table-column prop="email" label="邮箱" width="180" />
        <el-table-column prop="wechat" label="微信" width="120" />
        <el-table-column prop="qq" label="QQ" width="120" />
        <el-table-column prop="isPrimary" label="主联系人" width="100">
          <template #default="{ row }">
            <el-tag :type="row.isPrimary === 1 ? 'success' : 'info'" size="small">
              {{ row.isPrimary === 1 ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="supplierContactList.length === 0 && !contactLoading" description="暂无联系人" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getAllSuppliers, getSupplierContacts } from '../api/supplier'
import { getAccountsByProject, addAccount, updateAccount, deleteAccount } from '../api/projectAccount'
import { getProjectList } from '../api/project'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, View, Hide } from '@element-plus/icons-vue'

const loading = ref(false)
const contactLoading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const supplierContactDialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const accountList = ref([])
const projectOptions = ref([])
const supplierOptions = ref([])
const supplierContactList = ref([])
const currentProjectId = ref(null)
const currentSupplier = ref({})
const formRef = ref()
const showPasswordMap = ref({})
const supplierMap = ref({})

const accountTypes = {
  1: '系统账户',
  2: '数据库账户',
  3: '云平台账户',
  4: 'FTP账户',
  5: '邮箱账户',
  6: '其他'
}

const accountTypeColors = {
  1: 'primary',
  2: 'success',
  3: 'warning',
  4: 'info',
  5: 'danger',
  6: ''
}

const environments = {
  1: '开发',
  2: '测试',
  3: '预发布',
  4: '生产'
}

const environmentColors = {
  1: 'info',
  2: 'warning',
  3: 'warning',
  4: 'danger'
}

const form = reactive({
  id: null,
  projectId: null,
  supplierId: null,
  accountName: '',
  accountType: 1,
  username: '',
  password: '',
  accessUrl: '',
  port: null,
  environment: 1,
  remark: ''
})

const rules = {
  accountName: [{ required: true, message: '请输入账户名称', trigger: 'blur' }],
  accountType: [{ required: true, message: '请选择账户类型', trigger: 'change' }],
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  environment: [{ required: true, message: '请选择环境类型', trigger: 'change' }]
}

const getSupplierName = (supplierId) => {
  return supplierMap.value[supplierId] || '-'
}

const handleViewSupplier = async (supplierId) => {
  const supplier = supplierOptions.value.find(s => s.id === supplierId)
  if (!supplier) {
    ElMessage.warning('供应商不存在')
    return
  }
  
  currentSupplier.value = supplier
  supplierContactDialogVisible.value = true
  
  contactLoading.value = true
  try {
    const res = await getSupplierContacts(supplierId)
    supplierContactList.value = res.data
  } catch (error) {
    console.error('加载联系人失败', error)
  } finally {
    contactLoading.value = false
  }
}

const loadSuppliers = async () => {
  try {
    const res = await getAllSuppliers()
    supplierOptions.value = res.data
    supplierMap.value = {}
    res.data.forEach(supplier => {
      supplierMap.value[supplier.id] = supplier.supplierName
    })
  } catch (error) {
    console.error('加载供应商列表失败', error)
  }
}

const loadProjects = async () => {
  try {
    const res = await getProjectList({ page: 1, size: 1000 })
    projectOptions.value = res.data.records
  } catch (error) {
    console.error('加载项目列表失败', error)
  }
}

const loadAccounts = async () => {
  if (!currentProjectId.value) {
    accountList.value = []
    return
  }
  
  loading.value = true
  try {
    const res = await getAccountsByProject(currentProjectId.value)
    accountList.value = res.data
    showPasswordMap.value = {}
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const togglePassword = (row) => {
  showPasswordMap.value[row.id] = !showPasswordMap.value[row.id]
}

const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '添加账户'
  Object.assign(form, {
    id: null,
    projectId: currentProjectId.value,
    supplierId: null,
    accountName: '',
    accountType: 1,
    username: '',
    password: '',
    accessUrl: '',
    port: null,
    environment: 1,
    remark: ''
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑账户'
  Object.assign(form, { ...row })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (isEdit.value) {
          await updateAccount(form)
          ElMessage.success('更新成功')
        } else {
          await addAccount(form)
          ElMessage.success('添加成功')
        }
        dialogVisible.value = false
        loadAccounts()
      } catch (error) {
        console.error(error)
      } finally {
        submitLoading.value = false
      }
    }
  })
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该账户吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteAccount(row.id)
      ElMessage.success('删除成功')
      loadAccounts()
    } catch (error) {
      console.error(error)
    }
  })
}

onMounted(() => {
  loadSuppliers()
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