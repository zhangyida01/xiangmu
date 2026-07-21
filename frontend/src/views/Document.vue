<template>
  <div class="document-container">
    <el-card>
      <div class="toolbar">
        <el-select v-model="queryParams.projectId" placeholder="选择项目" style="width: 300px" filterable clearable @change="loadDocuments">
          <el-option v-for="project in projectOptions" :key="project.id" :label="project.projectName" :value="project.id" />
        </el-select>
        <el-select v-model="queryParams.docType" placeholder="文档类型" style="width: 150px" clearable @change="loadDocuments">
          <el-option label="功能清单" value="FUNCTION_LIST" />
          <el-option label="验收材料" value="ACCEPTANCE" />
          <el-option label="合同文档" value="CONTRACT" />
          <el-option label="技术方案" value="TECHNICAL" />
          <el-option label="其他" value="OTHER" />
        </el-select>
        <el-button type="primary" :icon="Search" @click="loadDocuments">搜索</el-button>
        <el-button type="primary" :icon="Plus" @click="handleUpload" :disabled="!canUpload">上传文档</el-button>
      </div>

      <el-table :data="documentList" style="width: 100%; margin-top: 20px" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="docName" label="文档名称" min-width="200" />
        <el-table-column prop="projectName" label="所属项目" width="180" />
        <el-table-column prop="docType" label="文档类型" width="120">
          <template #default="{ row }">
            <el-tag :type="docTypeColors[row.docType]">{{ docTypes[row.docType] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="fileName" label="文件名" min-width="180" show-overflow-tooltip />
        <el-table-column prop="fileSize" label="文件大小" width="100">
          <template #default="{ row }">
            {{ formatFileSize(row.fileSize) }}
          </template>
        </el-table-column>
        <el-table-column prop="uploaderName" label="上传人" width="100" />
        <el-table-column prop="uploadTime" label="上传时间" width="180" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link :icon="Download" @click="handleDownload(row)">下载</el-button>
            <el-button v-if="canDelete(row)" type="danger" link :icon="Delete" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="queryParams.current"
        v-model:page-size="queryParams.size"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadDocuments"
        @current-change="loadDocuments"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>
    <el-dialog v-model="dialogVisible" title="上传文档" width="600px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="项目" prop="projectId">
          <el-select v-model="form.projectId" placeholder="选择项目" style="width: 100%" filterable>
            <el-option v-for="project in managerProjects" :key="project.id" :label="project.projectName" :value="project.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="文档类型" prop="docType">
          <el-select v-model="form.docType" placeholder="选择类型" style="width: 100%">
            <el-option label="功能清单" value="FUNCTION_LIST" />
            <el-option label="验收材料" value="ACCEPTANCE" />
            <el-option label="合同文档" value="CONTRACT" />
            <el-option label="技术方案" value="TECHNICAL" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="文档名称" prop="docName">
          <el-input v-model="form.docName" placeholder="请输入文档名称" />
        </el-form-item>
        <el-form-item label="选择文件" prop="file">
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :limit="1"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
            :file-list="fileList"
            accept=".pdf,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.zip,.rar,.jpg,.jpeg,.png,.txt">
            <el-button type="primary">选择文件</el-button>
            <template #tip>
              <div class="el-upload__tip">
                支持 PDF、Word、Excel、PPT、压缩包、图片等格式，最大50MB
              </div>
            </template>
          </el-upload>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定上传</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Download, Delete } from '@element-plus/icons-vue'
import { getDocumentList, uploadDocument, downloadDocument, deleteDocument } from '../api/document'
import request from '../utils/request'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const documentList = ref([])
const total = ref(0)
const formRef = ref()
const uploadRef = ref()
const fileList = ref([])

const projectOptions = ref([])
const managerProjects = ref([])
const currentUserId = ref(null)

const docTypes = {
  'FUNCTION_LIST': '功能清单',
  'ACCEPTANCE': '验收材料',
  'CONTRACT': '合同文档',
  'TECHNICAL': '技术方案',
  'OTHER': '其他'
}

const docTypeColors = {
  'FUNCTION_LIST': 'primary',
  'ACCEPTANCE': 'success',
  'CONTRACT': 'warning',
  'TECHNICAL': 'info',
  'OTHER': ''
}

const queryParams = reactive({
  projectId: null,
  docType: '',
  current: 1,
  size: 10
})

const form = reactive({
  projectId: null,
  docType: '',
  docName: '',
  file: null,
  remark: ''
})

const rules = {
  projectId: [{ required: true, message: '请选择项目', trigger: 'change' }],
  docType: [{ required: true, message: '请选择文档类型', trigger: 'change' }],
  docName: [{ required: true, message: '请输入文档名称', trigger: 'blur' }],
  file: [{ required: true, message: '请选择文件', trigger: 'change' }]
}

const canUpload = computed(() => {
  return managerProjects.value.length > 0
})

const canDelete = (row) => {
  const project = projectOptions.value.find(p => p.id === row.projectId)
  return project && project.projectManagerId === currentUserId.value
}

const formatFileSize = (bytes) => {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(2) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(2) + ' MB'
}
const loadProjects = async () => {
  try {
    const res = await request.get('/project/list', {
      params: { current: 1, size: 1000 }
    })
    if (res.code === 200) {
      projectOptions.value = res.data.records || []
      managerProjects.value = projectOptions.value.filter(
        p => p.projectManagerId === currentUserId.value
      )
    }
  } catch (error) {
    ElMessage.error('加载项目列表失败')
  }
}

const loadCurrentUser = async () => {
  try {
    const res = await request.get('/user/info')
    if (res.code === 200) {
      currentUserId.value = res.data.id
    }
  } catch (error) {
    console.error('获取用户信息失败')
  }
}

const loadDocuments = async () => {
  loading.value = true
  try {
    const res = await getDocumentList(queryParams)
    if (res.code === 200) {
      documentList.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    ElMessage.error('加载文档列表失败')
  } finally {
    loading.value = false
  }
}

const handleUpload = () => {
  if (managerProjects.value.length === 0) {
    ElMessage.warning('您不是任何项目的项目经理，无法上传文档')
    return
  }
  dialogVisible.value = true
}

const handleFileChange = (file) => {
  form.file = file.raw
  fileList.value = [file]
}

const handleFileRemove = () => {
  form.file = null
  fileList.value = []
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    if (!form.file) {
      ElMessage.error('请选择文件')
      return
    }
    
    submitLoading.value = true
    try {
      const formData = new FormData()
      formData.append('file', form.file)
      formData.append('projectId', form.projectId)
      formData.append('docType', form.docType)
      formData.append('docName', form.docName)
      if (form.remark) {
        formData.append('remark', form.remark)
      }
      
      const res = await uploadDocument(formData)
      if (res.code === 200) {
        ElMessage.success('上传成功')
        dialogVisible.value = false
        loadDocuments()
      }
    } catch (error) {
      ElMessage.error('上传失败')
    } finally {
      submitLoading.value = false
    }
  })
}

const handleDownload = async (row) => {
  try {
    const token = localStorage.getItem('token')
    const response = await fetch(`/api/project-document/download/${row.id}`, {
      headers: { 'Authorization': `Bearer ${token}` }
    })
    
    if (!response.ok) {
      throw new Error('下载失败')
    }
    
    const blob = await response.blob()
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = row.fileName
    a.click()
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('下载成功')
  } catch (error) {
    console.error('下载错误:', error)
    ElMessage.error('下载失败')
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确认删除该文档吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await deleteDocument(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadDocuments()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const resetForm = () => {
  Object.assign(form, {
    projectId: null,
    docType: '',
    docName: '',
    file: null,
    remark: ''
  })
  fileList.value = []
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

onMounted(async () => {
  await loadCurrentUser()
  await loadProjects()
  loadDocuments()
})
</script>

<style scoped>
.document-container {
  padding: 20px;
}

.toolbar {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}
</style>