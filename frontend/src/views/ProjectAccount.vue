<template>
  <div class="account-container">
    <el-card>
      <div class="toolbar">
        <el-select v-model="currentProjectId" placeholder="閫夋嫨椤圭洰" style="width: 300px" filterable @change="loadAccounts">
          <el-option v-for="project in projectOptions" :key="project.id" :label="project.projectName" :value="project.id" />
        </el-select>
        <el-button type="primary" :icon="Plus" @click="handleAdd" :disabled="!currentProjectId">娣诲姞璐︽埛</el-button>
      </div>

      <el-table :data="accountList" style="width: 100%; margin-top: 20px" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="accountName" label="璐︽埛鍚嶇О" min-width="150" />
        <el-table-column prop="supplierId" label="渚涘簲鍟? width="150">
          <template #default="{ row }">
            {{ getSupplierName(row.supplierId) }}
          </template>
        </el-table-column>
        <el-table-column prop="accountType" label="璐︽埛绫诲瀷" width="120">
          <template #default="{ row }">
            <el-tag :type="accountTypeColors[row.accountType]">{{ accountTypes[row.accountType] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="鐢ㄦ埛鍚? width="150" />
        <el-table-column prop="password" label="瀵嗙爜" width="150">
          <template #default="{ row }">
            <span v-if="!showPasswordMap[row.id]">******</span>
            <span v-else>{{ passwordMap[row.id] || row.password }}</span>
            <el-button type="text" size="small" @click="togglePassword(row)" style="margin-left: 8px">
              <el-icon><View v-if="!showPasswordMap[row.id]" /><Hide v-else /></el-icon>
            </el-button>
          </template>
        </el-table-column>
        <el-table-column prop="accessUrl" label="璁块棶鍦板潃" min-width="200" />
        <el-table-column prop="port" label="绔彛" width="80" />
        <el-table-column prop="environment" label="鐜" width="100">
          <template #default="{ row }">
            <el-tag :type="environmentColors[row.environment]">{{ environments[row.environment] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="鍒涘缓鏃堕棿" width="180" />
        <el-table-column label="鎿嶄綔" fixed="right" width="180">
          <template #default="{ row }">
            <el-button type="primary" size="small" :icon="Edit" @click="handleEdit(row)">缂栬緫</el-button>
            <el-button type="danger" size="small" :icon="Delete" @click="handleDelete(row)">鍒犻櫎</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="璐︽埛鍚嶇О" prop="accountName">
          <el-input v-model="form.accountName" placeholder="渚嬪锛氱敓浜ф暟鎹簱" />
        </el-form-item>
        <el-form-item label="渚涘簲鍟?>
          <el-select v-model="form.supplierId" placeholder="閫夋嫨渚涘簲鍟嗭紙閫夊～锛? style="width: 100%" filterable clearable>
            <el-option v-for="supplier in supplierOptions" :key="supplier.id" :label="supplier.supplierName" :value="supplier.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="璐︽埛绫诲瀷" prop="accountType">
          <el-select v-model="form.accountType" style="width: 100%">
            <el-option v-for="(name, value) in accountTypes" :key="value" :label="name" :value="parseInt(value)" />
          </el-select>
        </el-form-item>
        <el-form-item label="鐢ㄦ埛鍚? prop="username">
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item label="瀵嗙爜" prop="password">
          <el-input v-model="form.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="璁块棶鍦板潃">
          <el-input v-model="form.accessUrl" placeholder="渚嬪锛歨ttps://example.com 鎴?192.168.1.100" />
        </el-form-item>
        <el-form-item label="绔彛">
          <el-input-number v-model="form.port" :min="1" :max="65535" style="width: 100%" />
        </el-form-item>
        <el-form-item label="鐜绫诲瀷" prop="environment">
          <el-select v-model="form.environment" style="width: 100%">
            <el-option v-for="(name, value) in environments" :key="value" :label="name" :value="parseInt(value)" />
          </el-select>
        </el-form-item>
        <el-form-item label="澶囨敞">
          <el-input v-model="form.remark" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">鍙栨秷</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">纭畾</el-button>
      </template>
    </el-dialog>
  </div>


    <el-dialog v-model="supplierContactDialogVisible" :title="${currentSupplier.supplierName} - 联系人" width="800px">
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
      <template v-if="supplierContactList.length === 0 && !contactLoading">
        <el-empty description="暂无联系人" />
      </template>
    </el-dialog>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getAllSuppliers, getSupplierContacts } from '../api/supplier'
import { getAccountsByProject, addAccount, updateAccount, deleteAccount, getAccountPassword } from '../api/projectAccount'
import { getProjectList } from '../api/project'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, View, Hide } from '@element-plus/icons-vue'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const accountList = ref([])
const projectOptions = ref([])
const supplierOptions = ref([])
const currentProjectId = ref(null)
const formRef = ref()
const showPasswordMap = ref({})
const passwordMap = ref({})
const supplierMap = ref({})
const supplierContactDialogVisible = ref(false)
const currentSupplier = ref({})
const supplierContactList = ref([])
const contactLoading = ref(false)

const accountTypes = {
  1: '绯荤粺璐︽埛',
  2: '鏁版嵁搴撹处鎴?,
  3: '浜戝钩鍙拌处鎴?,
  4: 'FTP璐︽埛',
  5: '閭璐︽埛',
  6: '鍏朵粬'
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
  1: '寮€鍙?,
  2: '娴嬭瘯',
  3: '棰勫彂甯?,
  4: '鐢熶骇'
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
  accountName: [{ required: true, message: '璇疯緭鍏ヨ处鎴峰悕绉?, trigger: 'blur' }],
  accountType: [{ required: true, message: '璇烽€夋嫨璐︽埛绫诲瀷', trigger: 'change' }],
  username: [{ required: true, message: '璇疯緭鍏ョ敤鎴峰悕', trigger: 'blur' }],
  password: [{ required: true, message: '璇疯緭鍏ュ瘑鐮?, trigger: 'blur' }],
  environment: [{ required: true, message: '璇烽€夋嫨鐜绫诲瀷', trigger: 'change' }]
}

const getSupplierName = (supplierId) => {
  return supplierMap.value[supplierId] || '-'
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
    console.error('鍔犺浇渚涘簲鍟嗗垪琛ㄥけ璐?, error)
  }
}

const loadProjects = async () => {
  try {
    const res = await getProjectList({ page: 1, size: 1000 })
    projectOptions.value = res.data.records
  } catch (error) {
    console.error('鍔犺浇椤圭洰鍒楄〃澶辫触', error)
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
    passwordMap.value = {}
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const togglePassword = async (row) => {
  if (showPasswordMap.value[row.id]) {
    showPasswordMap.value[row.id] = false
  } else {
    showPasswordMap.value[row.id] = true
  }
}

const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '娣诲姞璐︽埛'
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
  dialogTitle.value = '缂栬緫璐︽埛'
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
          ElMessage.success('鏇存柊鎴愬姛')
        } else {
          await addAccount(form)
          ElMessage.success('娣诲姞鎴愬姛')
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
  ElMessageBox.confirm('纭畾瑕佸垹闄よ璐︽埛鍚楋紵', '鎻愮ず', {
    confirmButtonText: '纭畾',
    cancelButtonText: '鍙栨秷',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteAccount(row.id)
      ElMessage.success('鍒犻櫎鎴愬姛')
      loadAccounts()
    } catch (error) {
      console.error(error)
    }
  })
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