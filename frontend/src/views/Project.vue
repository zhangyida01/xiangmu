<template>
  <div class="project-container">
    <el-card>
      <div class="toolbar">
        <el-input v-model="queryParams.keyword" placeholder="鎼滅储椤圭洰鍚嶇О/缂栧彿" style="width: 300px" clearable @keyup.enter="loadProjects" />
        <el-select v-model="queryParams.status" placeholder="鐘舵€? style="width: 150px" clearable @change="loadProjects">
          <el-option label="绔嬮」" :value="0" />
          <el-option label="杩涜涓? :value="1" />
          <el-option label="宸查獙鏀? :value="2" />
          <el-option label="宸插叧闂? :value="3" />
        </el-select>
        <el-button type="primary" :icon="Search" @click="loadProjects">鎼滅储</el-button>
        <el-button type="primary" :icon="Plus" @click="handleAdd">鍒涘缓椤圭洰</el-button>
      </div>

      <el-table :data="projectList" style="width: 100%; margin-top: 20px" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="projectCode" label="椤圭洰缂栧彿" width="150" />
        <el-table-column prop="projectName" label="椤圭洰鍚嶇О" min-width="200" />
        <el-table-column prop="contractNo" label="鍚堝悓缂栧彿" width="150" />
        <el-table-column prop="contractAmount" label="鍚堝悓閲戦" width="120">
          <template #default="{ row }">
            {{ row.contractAmount ? '楼' + row.contractAmount.toLocaleString() : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="projectManagerId" label="椤圭洰缁忕悊" width="120">
          <template #default="{ row }">
            {{ getUserName(row.projectManagerId) }}
          </template>
        </el-table-column>
        <el-table-column prop="startDate" label="寮€濮嬫棩鏈? width="120" />
        <el-table-column prop="endDate" label="缁撴潫鏃ユ湡" width="120" />
        <el-table-column prop="status" label="鐘舵€? width="100">
          <template #default="{ row }">
            <el-tag :type="statusTypes[row.status]">{{ statusTexts[row.status] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="鎿嶄綔" fixed="right" width="180">
          <template #default="{ row }">
            <el-button type="primary" size="small" :icon="Edit" @click="handleEdit(row)">缂栬緫</el-button>
            <el-button type="danger" size="small" :icon="Delete" @click="handleDelete(row)">鍒犻櫎</el-button>
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
        <el-form-item label="椤圭洰缂栧彿" prop="projectCode">
          <el-input v-model="form.projectCode" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="椤圭洰鍚嶇О" prop="projectName">
          <el-input v-model="form.projectName" />
        </el-form-item>
        <el-form-item label="瀹㈡埛ID" prop="customerId">
          <el-input-number v-model="form.customerId" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="鍚堝悓缂栧彿">
          <el-input v-model="form.contractNo" />
        </el-form-item>
        <el-form-item label="鍚堝悓閲戦">
          <el-input-number v-model="form.contractAmount" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="寮€濮嬫棩鏈?>
          <el-date-picker v-model="form.startDate" type="date" placeholder="閫夋嫨鏃ユ湡" format="YYYY-MM-DD" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="缁撴潫鏃ユ湡">
          <el-date-picker v-model="form.endDate" type="date" placeholder="閫夋嫨鏃ユ湡" format="YYYY-MM-DD" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="椤圭洰缁忕悊" prop="projectManagerId">
          <el-select v-model="form.projectManagerId" placeholder="閫夋嫨椤圭洰缁忕悊" style="width: 100%" filterable>
            <el-option v-for="user in userOptions" :key="user.id" :label="user.realName || user.username" :value="user.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="鐘舵€?>
          <el-select v-model="form.status" style="width: 100%">
            <el-option label="绔嬮」" :value="0" />
            <el-option label="杩涜涓? :value="1" />
            <el-option label="宸查獙鏀? :value="2" />
            <el-option label="宸插叧闂? :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="椤圭洰鎻忚堪">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">鍙栨秷</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">纭畾</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getAllCustomers } from '../api/customer'
import { getProjectList, addProject, updateProject, deleteProject } from '../api/project'
import { getUserList } from '../api/user'
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
const userOptions = ref([])
const customerOptions = ref([])
const customerMap = ref({})
const userMap = ref({})

const statusTexts = ['绔嬮」', '杩涜涓?, '宸查獙鏀?, '宸插叧闂?]
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
  projectManagerId: null,
  status: 0,
  description: ''
})

const rules = {
  projectCode: [{ required: true, message: '璇疯緭鍏ラ」鐩紪鍙?, trigger: 'blur' }],
  projectName: [{ required: true, message: '璇疯緭鍏ラ」鐩悕绉?, trigger: 'blur' }],
  customerId: [{ required: true, message: '璇疯緭鍏ュ鎴稩D', trigger: 'blur' }],
  projectManagerId: [{ required: true, message: '璇烽€夋嫨椤圭洰缁忕悊', trigger: 'change' }]
}

const getUserName = (userId) => {
  return userMap.value[userId] || `鐢ㄦ埛${userId}`
}



const loadCustomers = async () => {
  try {
    const res = await getAllCustomers()
    customerOptions.value = res.data
    customerMap.value = {}
    res.data.forEach(customer => {
      customerMap.value[customer.id] = customer.companyName
    })
  } catch (error) {
    console.error('加载客户列表失败', error)
  }
}
const loadUsers = async () => {
  try {
    const res = await getUserList({ page: 1, size: 1000 })
    userOptions.value = res.data.records
    userMap.value = {}
    res.data.records.forEach(user => {
      userMap.value[user.id] = user.realName || user.username
    })
  } catch (error) {
    console.error('鍔犺浇鐢ㄦ埛鍒楄〃澶辫触', error)
  }
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
  dialogTitle.value = '鍒涘缓椤圭洰'
  Object.assign(form, {
    id: null,
    projectCode: 'PRJ' + Date.now(),
    projectName: '',
    customerId: 1,
    contractNo: '',
    contractAmount: 0,
    startDate: '',
    endDate: '',
    projectManagerId: null,
    status: 0,
    description: ''
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  dialogTitle.value = '缂栬緫椤圭洰'
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
          ElMessage.success('鏇存柊鎴愬姛')
        } else {
          await addProject(form)
          ElMessage.success('鍒涘缓鎴愬姛')
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
  ElMessageBox.confirm('纭畾瑕佸垹闄よ椤圭洰鍚楋紵', '鎻愮ず', {
    confirmButtonText: '纭畾',
    cancelButtonText: '鍙栨秷',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteProject(row.id)
      ElMessage.success('鍒犻櫎鎴愬姛')
      loadProjects()
    } catch (error) {
      console.error(error)
    }
  })
}

onMounted(() => {
  loadCustomers()
  loadUsers()
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
