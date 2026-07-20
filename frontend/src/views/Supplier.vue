<template>
  <div class="supplier-container">
    <el-card>
      <div class="toolbar">
        <el-input v-model="queryParams.keyword" placeholder="搜索供应商名称/编号" style="width: 300px" clearable @keyup.enter="loadSuppliers" />
        <el-select v-model="queryParams.supplierType" placeholder="供应商类型" style="width: 150px" clearable @change="loadSuppliers">
          <el-option label="软件供应商" :value="1" />
          <el-option label="硬件供应商" :value="2" />
          <el-option label="集成供应商" :value="3" />
          <el-option label="其他" :value="4" />
        </el-select>
        <el-button type="primary" :icon="Search" @click="loadSuppliers">搜索</el-button>
        <el-button type="primary" :icon="Plus" @click="handleAdd">添加供应商</el-button>
      </div>

      <el-table :data="supplierList" style="width: 100%; margin-top: 20px" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="supplierCode" label="供应商编号" width="150" />
        <el-table-column prop="supplierName" label="供应商名称" min-width="200" />
        <el-table-column prop="supplierType" label="类型" width="120">
          <template #default="{ row }">
            <el-tag :type="supplierTypeColors[row.supplierType]">{{ supplierTypes[row.supplierType] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="businessScope" label="业务范围" min-width="200" show-overflow-tooltip />
        <el-table-column prop="address" label="地址" min-width="180" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="280">
          <template #default="{ row }">
            <el-button type="success" size="small" :icon="User" @click="handleContacts(row)">联系人</el-button>
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
        @size-change="loadSuppliers"
        @current-change="loadSuppliers"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="供应商编号" prop="supplierCode">
              <el-input v-model="form.supplierCode" :disabled="isEdit" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="供应商名称" prop="supplierName">
              <el-input v-model="form.supplierName" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="供应商类型" prop="supplierType">
              <el-select v-model="form.supplierType" style="width: 100%">
                <el-option label="软件供应商" :value="1" />
                <el-option label="硬件供应商" :value="2" />
                <el-option label="集成供应商" :value="3" />
                <el-option label="其他" :value="4" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-radio-group v-model="form.status">
                <el-radio :label="1">启用</el-radio>
                <el-radio :label="0">禁用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="业务范围">
          <el-input v-model="form.businessScope" placeholder="例如：云计算服务、CDN、数据库" />
        </el-form-item>

        <el-form-item label="公司地址">
          <el-input v-model="form.address" />
        </el-form-item>

        <el-form-item label="公司网站">
          <el-input v-model="form.website" placeholder="https://www.example.com" />
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

    <el-dialog v-model="contactDialogVisible" :title="`${currentSupplier.supplierName} - 联系人管理`" width="900px">
      <div style="margin-bottom: 16px">
        <el-button type="primary" :icon="Plus" size="small" @click="handleAddContact">添加联系人</el-button>
      </div>
      
      <el-table :data="contactList" v-loading="contactLoading">
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
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEditContact(row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDeleteContact(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <el-dialog v-model="contactFormVisible" :title="contactFormTitle" width="600px">
      <el-form :model="contactForm" :rules="contactRules" ref="contactFormRef" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="姓名" prop="contactName">
              <el-input v-model="contactForm.contactName" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="职位">
              <el-input v-model="contactForm.position" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="电话" prop="phone">
              <el-input v-model="contactForm.phone" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱">
              <el-input v-model="contactForm.email" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="微信">
              <el-input v-model="contactForm.wechat" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="QQ">
              <el-input v-model="contactForm.qq" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="主联系人">
          <el-switch v-model="contactForm.isPrimary" :active-value="1" :inactive-value="0" />
        </el-form-item>

        <el-form-item label="备注">
          <el-input v-model="contactForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="contactFormVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitContact" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { 
  getSupplierList, addSupplier, updateSupplier, deleteSupplier,
  getSupplierContacts, addSupplierContact, updateSupplierContact, deleteSupplierContact
} from '../api/supplier'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Edit, Delete, User } from '@element-plus/icons-vue'

const loading = ref(false)
const contactLoading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const contactDialogVisible = ref(false)
const contactFormVisible = ref(false)
const dialogTitle = ref('')
const contactFormTitle = ref('')
const isEdit = ref(false)
const isContactEdit = ref(false)
const supplierList = ref([])
const contactList = ref([])
const total = ref(0)
const formRef = ref()
const contactFormRef = ref()
const currentSupplier = ref({})

const supplierTypes = {
  1: '软件供应商',
  2: '硬件供应商',
  3: '集成供应商',
  4: '其他'
}

const supplierTypeColors = {
  1: 'primary',
  2: 'success',
  3: 'warning',
  4: 'info'
}

const queryParams = reactive({
  page: 1,
  size: 10,
  keyword: '',
  supplierType: null
})

const form = reactive({
  id: null,
  supplierCode: '',
  supplierName: '',
  supplierType: 1,
  businessScope: '',
  address: '',
  website: '',
  status: 1,
  remark: ''
})

const contactForm = reactive({
  id: null,
  supplierId: null,
  contactName: '',
  position: '',
  phone: '',
  email: '',
  wechat: '',
  qq: '',
  isPrimary: 0,
  remark: ''
})

const rules = {
  supplierCode: [{ required: true, message: '请输入供应商编号', trigger: 'blur' }],
  supplierName: [{ required: true, message: '请输入供应商名称', trigger: 'blur' }],
  supplierType: [{ required: true, message: '请选择供应商类型', trigger: 'change' }]
}

const contactRules = {
  contactName: [{ required: true, message: '请输入联系人姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }]
}

const loadSuppliers = async () => {
  loading.value = true
  try {
    const res = await getSupplierList(queryParams)
    supplierList.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '添加供应商'
  Object.assign(form, {
    id: null,
    supplierCode: 'SUP' + new Date().getTime(),
    supplierName: '',
    supplierType: 1,
    businessScope: '',
    address: '',
    website: '',
    status: 1,
    remark: ''
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑供应商'
  Object.assign(form, { ...row })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (isEdit.value) {
          await updateSupplier(form)
          ElMessage.success('更新成功')
        } else {
          await addSupplier(form)
          ElMessage.success('添加成功')
        }
        dialogVisible.value = false
        loadSuppliers()
      } catch (error) {
        console.error(error)
      } finally {
        submitLoading.value = false
      }
    }
  })
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该供应商吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteSupplier(row.id)
      ElMessage.success('删除成功')
      loadSuppliers()
    } catch (error) {
      console.error(error)
    }
  })
}

const handleContacts = async (row) => {
  currentSupplier.value = row
  contactDialogVisible.value = true
  loadContacts(row.id)
}

const loadContacts = async (supplierId) => {
  contactLoading.value = true
  try {
    const res = await getSupplierContacts(supplierId)
    contactList.value = res.data
  } catch (error) {
    console.error(error)
  } finally {
    contactLoading.value = false
  }
}

const handleAddContact = () => {
  isContactEdit.value = false
  contactFormTitle.value = '添加联系人'
  Object.assign(contactForm, {
    id: null,
    supplierId: currentSupplier.value.id,
    contactName: '',
    position: '',
    phone: '',
    email: '',
    wechat: '',
    qq: '',
    isPrimary: 0,
    remark: ''
  })
  contactFormVisible.value = true
}

const handleEditContact = (row) => {
  isContactEdit.value = true
  contactFormTitle.value = '编辑联系人'
  Object.assign(contactForm, { ...row })
  contactFormVisible.value = true
}

const handleSubmitContact = async () => {
  await contactFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (isContactEdit.value) {
          await updateSupplierContact(contactForm)
          ElMessage.success('更新成功')
        } else {
          await addSupplierContact(contactForm)
          ElMessage.success('添加成功')
        }
        contactFormVisible.value = false
        loadContacts(currentSupplier.value.id)
      } catch (error) {
        console.error(error)
      } finally {
        submitLoading.value = false
      }
    }
  })
}

const handleDeleteContact = (row) => {
  ElMessageBox.confirm('确定要删除该联系人吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteSupplierContact(row.id)
      ElMessage.success('删除成功')
      loadContacts(currentSupplier.value.id)
    } catch (error) {
      console.error(error)
    }
  })
}

loadSuppliers()
</script>

<style scoped>
.toolbar {
  display: flex;
  gap: 12px;
  align-items: center;
}
</style>