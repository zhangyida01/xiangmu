<template>
  <div class="page" v-loading="loading">
    <el-card shadow="never" class="card-gap">
      <div class="head">
        <div>
          <div style="font-size:18px;font-weight:600">
            {{ project.name }}
            <el-tag size="small" style="margin-left:8px">{{ project.projectNo }}</el-tag>
            <el-tag size="small" type="info" style="margin-left:4px">{{ dict.label('PROJECT_STATUS', project.status) }}</el-tag>
            <span :class="healthClass" style="margin-left:6px">●</span>
            <el-tag v-if="project.archived" size="small" type="info" style="margin-left:6px">已归档</el-tag>
            <el-tag v-if="project.locked" size="small" type="warning" style="margin-left:4px">已锁定</el-tag>
          </div>
          <div class="muted" style="margin-top:6px">
            客户：{{ overview.customer?.name || '-' }} ｜ 项目经理：{{ project.manager || '-' }} ｜
            计划验收：{{ project.planAcceptDate || '-' }} ｜ 进度：{{ project.progress || 0 }}%
          </div>
        </div>
        <div>
          <el-button size="small" @click="$router.push('/projects')">返回列表</el-button>
          <el-button v-if="user.canEdit" size="small" type="primary" @click="openEdit">编辑</el-button>
          <el-button v-if="user.canEdit" size="small" @click="openStatus">变更状态</el-button>
          <el-button v-if="user.isAdmin" size="small" @click="toggleLock">{{ project.locked ? '解锁' : '锁定' }}</el-button>
        </div>
      </div>
    </el-card>

    <el-tabs v-model="tab" type="border-card">
      <el-tab-pane label="概览" name="overview">
        <el-row :gutter="12">
          <el-col :span="6" v-for="s in summaryCards" :key="s.label">
            <el-card shadow="hover" body-style="padding:14px">
              <div style="font-size:22px;font-weight:600" :style="{color:s.color}">{{ s.value }}</div>
              <div class="muted" style="font-size:12px">{{ s.label }}</div>
            </el-card>
          </el-col>
        </el-row>
        <el-descriptions :column="3" border style="margin-top:12px" title="基础信息">
          <el-descriptions-item label="项目编号">{{ project.projectNo }}</el-descriptions-item>
          <el-descriptions-item label="项目类型">{{ dict.label('PROJECT_TYPE', project.projectType) }}</el-descriptions-item>
          <el-descriptions-item label="健康度">{{ healthText }}</el-descriptions-item>
          <el-descriptions-item label="销售负责人">{{ project.sales || '-' }}</el-descriptions-item>
          <el-descriptions-item label="交付负责人">{{ project.deliveryOwner || '-' }}</el-descriptions-item>
          <el-descriptions-item label="合同编号">{{ project.contractNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="合同金额">{{ project.amount ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="立项日期">{{ project.setupDate || '-' }}</el-descriptions-item>
          <el-descriptions-item label="启动日期">{{ project.startDate || '-' }}</el-descriptions-item>
          <el-descriptions-item label="计划上线">{{ project.planOnlineDate || '-' }}</el-descriptions-item>
          <el-descriptions-item label="实际上线">{{ project.actualOnlineDate || '-' }}</el-descriptions-item>
          <el-descriptions-item label="计划验收">{{ project.planAcceptDate || '-' }}</el-descriptions-item>
          <el-descriptions-item label="实际验收">{{ project.actualAcceptDate || '-' }}</el-descriptions-item>
          <el-descriptions-item label="质保到期">{{ project.warrantyEndDate || '-' }}</el-descriptions-item>
          <el-descriptions-item label="归档日期">{{ project.archiveDate || '-' }}</el-descriptions-item>
          <el-descriptions-item label="标签" :span="3">{{ project.tags || '-' }}</el-descriptions-item>
          <el-descriptions-item label="简介" :span="3">{{ project.summary || '-' }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="3">{{ project.remark || '-' }}</el-descriptions-item>
        </el-descriptions>
      </el-tab-pane>

      <el-tab-pane :label="`相关人(${members.length})`" name="members">
        <div class="toolbar">
          <el-button v-if="user.canEdit" type="primary" size="small" :icon="Plus" @click="openMember()">新增</el-button>
        </div>
        <el-table :data="members" border size="small">
          <el-table-column label="类型" width="90">
            <template #default="{ row }">{{ { CUSTOMER: '甲方', VENDOR: '乙方', THIRD: '第三方' }[row.memberType] || row.memberType }}</template>
          </el-table-column>
          <el-table-column prop="name" label="姓名" width="110" />
          <el-table-column prop="role" label="角色/职务" width="150" />
          <el-table-column prop="phone" label="电话" width="140" />
          <el-table-column prop="email" label="邮箱" min-width="160" show-overflow-tooltip />
          <el-table-column prop="wechat" label="微信" width="120" />
          <el-table-column label="主联系人" width="80" align="center">
            <template #default="{ row }"><el-tag v-if="row.isMain" size="small" type="success">是</el-tag></template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" min-width="140" show-overflow-tooltip />
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button v-if="user.canEdit" link type="primary" size="small" @click="openMember(row)">编辑</el-button>
              <el-button v-if="user.canEdit" link type="danger" size="small" @click="delMember(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane :label="`交付环境(${envs.length})`" name="envs">
        <div class="toolbar">
          <el-button v-if="user.canEdit" type="primary" size="small" :icon="Plus" @click="openEnv()">新增环境</el-button>
        </div>
        <el-table :data="envs" border size="small">
          <el-table-column prop="envName" label="环境" width="110" />
          <el-table-column prop="envType" label="类型" width="70" align="center" />
          <el-table-column prop="hostname" label="主机名" width="130" show-overflow-tooltip />
          <el-table-column prop="ip" label="内网IP" width="130" />
          <el-table-column prop="publicIp" label="公网IP" width="130" />
          <el-table-column prop="os" label="操作系统" width="150" show-overflow-tooltip />
          <el-table-column label="配置" width="130">
            <template #default="{ row }">{{ row.cpu || '-' }} / {{ row.memoryGb || '-' }}G / {{ row.diskGb || '-' }}G</template>
          </el-table-column>
          <el-table-column prop="cloudVendor" label="云/机房" width="110" />
          <el-table-column prop="deployType" label="部署方式" width="100" />
          <el-table-column label="访问地址" min-width="180" show-overflow-tooltip>
            <template #default="{ row }">
              <el-link v-if="row.accessUrl" type="primary" :href="row.accessUrl" target="_blank">{{ row.accessUrl }}</el-link>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button v-if="user.canEdit" link type="primary" size="small" @click="openEnv(row)">编辑</el-button>
              <el-button v-if="user.canEdit" link type="danger" size="small" @click="delEnv(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane :label="`产品License(${products.length})`" name="products">
        <div class="toolbar">
          <el-button v-if="user.canEdit" type="primary" size="small" :icon="Plus" @click="openProduct()">新增</el-button>
        </div>
        <el-table :data="products" border size="small">
          <el-table-column prop="productName" label="产品" min-width="160" />
          <el-table-column prop="version" label="版本" width="120" />
          <el-table-column prop="licenseNo" label="License号" min-width="180" show-overflow-tooltip />
          <el-table-column prop="licenseType" label="授权类型" width="100" />
          <el-table-column label="到期日" width="120">
            <template #default="{ row }">
              <span :class="expiredClass(row.licenseExpire)">{{ row.licenseExpire || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" min-width="140" show-overflow-tooltip />
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button v-if="user.canEdit" link type="primary" size="small" @click="openProduct(row)">编辑</el-button>
              <el-button v-if="user.canEdit" link type="danger" size="small" @click="delProduct(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane :label="`交付清单(${checklist.done}/${checklist.total})`" name="checklist">
        <div class="toolbar">
          <el-progress :percentage="Number(checklist.rate) || 0" style="width:220px" />
          <span v-if="checklist.missingRequired" class="danger-text">必填缺项 {{ checklist.missingRequired }} 项</span>
          <div class="spacer"></div>
          <el-button v-if="user.canEdit" size="small" @click="generate">按模板生成</el-button>
          <el-button v-if="user.canEdit" size="small" type="primary" :icon="Plus" @click="openItem()">新增清单项</el-button>
        </div>
        <el-table :data="checklist.items || []" border size="small">
          <el-table-column prop="itemName" label="应交文档 / 事项" min-width="220" />
          <el-table-column label="必填" width="70" align="center">
            <template #default="{ row }"><el-tag v-if="row.required" size="small" type="danger">必填</el-tag><span v-else class="muted">-</span></template>
          </el-table-column>
          <el-table-column label="状态" width="150">
            <template #default="{ row }">
              <el-select v-model="row.status" size="small" :disabled="!user.canEdit" @change="saveItem(row)">
                <el-option label="待提交" value="PENDING" /><el-option label="已提交" value="SUBMITTED" />
                <el-option label="客户已确认" value="CONFIRMED" /><el-option label="不适用" value="NA" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button v-if="user.canEdit" link type="primary" size="small" @click="openItem(row)">编辑</el-button>
              <el-button v-if="user.canEdit" link type="danger" size="small" @click="delItem(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="交付文档" name="docs" lazy>
        <DocumentPanel :project-id="id" />
      </el-tab-pane>

      <el-tab-pane label="账号凭据" name="creds" lazy>
        <CredentialPanel :project-id="id" />
      </el-tab-pane>

      <el-tab-pane label="操作日志" name="logs" lazy>
        <el-table :data="logs" border size="small" v-loading="logLoading">
          <el-table-column prop="createTime" label="时间" width="160" />
          <el-table-column prop="username" label="操作人" width="100" />
          <el-table-column prop="module" label="模块" width="110" />
          <el-table-column prop="action" label="动作" width="140" />
          <el-table-column prop="targetName" label="对象" min-width="150" show-overflow-tooltip />
          <el-table-column prop="detail" label="详情" min-width="200" show-overflow-tooltip />
          <el-table-column prop="ip" label="IP" width="130" />
        </el-table>
        <div class="pager">
          <el-pagination v-model:current-page="logQuery.page" :page-size="20" :total="logTotal"
                         layout="total, prev, pager, next" @current-change="loadLogs" />
        </div>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="editVisible" title="编辑项目" width="760px" top="6vh">
      <el-form :model="editForm" label-width="100px">
        <el-row :gutter="12">
          <el-col :span="12"><el-form-item label="项目编号"><el-input v-model="editForm.projectNo" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="项目名称"><el-input v-model="editForm.name" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="项目经理"><el-input v-model="editForm.manager" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="销售负责人"><el-input v-model="editForm.sales" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="交付负责人"><el-input v-model="editForm.deliveryOwner" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="合同编号"><el-input v-model="editForm.contractNo" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="合同金额"><el-input-number v-model="editForm.amount" :min="0" :precision="2" style="width:100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="进度%"><el-input-number v-model="editForm.progress" :min="0" :max="100" style="width:100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="立项日期"><el-date-picker v-model="editForm.setupDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="启动日期"><el-date-picker v-model="editForm.startDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="计划上线"><el-date-picker v-model="editForm.planOnlineDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="计划验收"><el-date-picker v-model="editForm.planAcceptDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="实际验收"><el-date-picker v-model="editForm.actualAcceptDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="质保到期"><el-date-picker v-model="editForm.warrantyEndDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="标签"><el-input v-model="editForm.tags" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="简介"><el-input v-model="editForm.summary" type="textarea" :rows="3" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="备注"><el-input v-model="editForm.remark" type="textarea" :rows="2" /></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEdit">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="statusVisible" title="变更项目状态" width="420px">
      <el-form label-width="80px">
        <el-form-item label="新状态">
          <el-select v-model="statusForm.status" style="width:100%">
            <el-option v-for="d in dict.items('PROJECT_STATUS')" :key="d.itemCode" :label="d.itemName" :value="d.itemCode" />
          </el-select>
        </el-form-item>
        <el-form-item label="变更原因"><el-input v-model="statusForm.reason" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="statusVisible = false">取消</el-button>
        <el-button type="primary" @click="submitStatus">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="memberVisible" title="项目相关人" width="520px">
      <el-form :model="memberForm" label-width="90px">
        <el-form-item label="人员类型">
          <el-radio-group v-model="memberForm.memberType">
            <el-radio-button value="CUSTOMER">甲方</el-radio-button>
            <el-radio-button value="VENDOR">乙方</el-radio-button>
            <el-radio-button value="THIRD">第三方</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="姓名" required><el-input v-model="memberForm.name" /></el-form-item>
        <el-form-item label="角色/职务"><el-input v-model="memberForm.role" /></el-form-item>
        <el-form-item label="电话"><el-input v-model="memberForm.phone" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="memberForm.email" /></el-form-item>
        <el-form-item label="微信"><el-input v-model="memberForm.wechat" /></el-form-item>
        <el-form-item label="主联系人"><el-switch v-model="memberForm.isMain" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="memberForm.remark" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="memberVisible = false">取消</el-button>
        <el-button type="primary" @click="submitMember">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="envVisible" title="交付环境" width="640px">
      <el-form :model="envForm" label-width="90px">
        <el-row :gutter="12">
          <el-col :span="12"><el-form-item label="环境名称" required><el-input v-model="envForm.envName" placeholder="如 生产环境" /></el-form-item></el-col>
          <el-col :span="12">
            <el-form-item label="环境类型">
              <el-select v-model="envForm.envType" style="width:100%">
                <el-option label="生产 PROD" value="PROD" /><el-option label="测试 TEST" value="TEST" />
                <el-option label="UAT" value="UAT" /><el-option label="开发 DEV" value="DEV" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12"><el-form-item label="主机名"><el-input v-model="envForm.hostname" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="内网IP"><el-input v-model="envForm.ip" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="公网IP"><el-input v-model="envForm.publicIp" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="操作系统"><el-input v-model="envForm.os" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="CPU"><el-input v-model="envForm.cpu" placeholder="如 8核" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="内存G"><el-input-number v-model="envForm.memoryGb" :min="0" style="width:100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="磁盘G"><el-input-number v-model="envForm.diskGb" :min="0" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="云/机房"><el-input v-model="envForm.cloudVendor" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="部署方式"><el-input v-model="envForm.deployType" placeholder="Docker/裸机/K8s" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="访问地址"><el-input v-model="envForm.accessUrl" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="备注"><el-input v-model="envForm.remark" type="textarea" :rows="2" /></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="envVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEnv">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="productVisible" title="产品与 License" width="520px">
      <el-form :model="productForm" label-width="100px">
        <el-form-item label="产品名称" required><el-input v-model="productForm.productName" /></el-form-item>
        <el-form-item label="版本"><el-input v-model="productForm.version" /></el-form-item>
        <el-form-item label="License号"><el-input v-model="productForm.licenseNo" /></el-form-item>
        <el-form-item label="授权类型"><el-input v-model="productForm.licenseType" placeholder="永久/订阅/试用" /></el-form-item>
        <el-form-item label="到期日"><el-date-picker v-model="productForm.licenseExpire" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="productForm.remark" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="productVisible = false">取消</el-button>
        <el-button type="primary" @click="submitProduct">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="itemVisible" title="交付清单项" width="460px">
      <el-form :model="itemForm" label-width="90px">
        <el-form-item label="名称" required><el-input v-model="itemForm.itemName" /></el-form-item>
        <el-form-item label="必填"><el-switch v-model="itemForm.required" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="itemForm.status" style="width:100%">
            <el-option label="待提交" value="PENDING" /><el-option label="已提交" value="SUBMITTED" />
            <el-option label="客户已确认" value="CONFIRMED" /><el-option label="不适用" value="NA" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="itemForm.remark" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="itemVisible = false">取消</el-button>
        <el-button type="primary" @click="submitItem">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { auditApi, projectApi } from '../api'
import { useDictStore } from '../store/dict'
import { useUserStore } from '../store/user'
import CredentialPanel from '../components/CredentialPanel.vue'
import DocumentPanel from '../components/DocumentPanel.vue'

const route = useRoute()
const dict = useDictStore()
const user = useUserStore()

const id = computed(() => Number(route.params.id))
const tab = ref('overview')
const loading = ref(false)
const project = ref({})
const overview = ref({})
const members = ref([])
const envs = ref([])
const products = ref([])
const checklist = ref({ total: 0, done: 0, rate: 0, missingRequired: 0, items: [] })
const logs = ref([])
const logTotal = ref(0)
const logLoading = ref(false)
const logQuery = reactive({ page: 1, size: 20 })

const summaryCards = computed(() => [
  { label: '交付文档', value: overview.value.docCount ?? 0, color: '#409eff' },
  { label: '账号凭据', value: overview.value.credentialCount ?? 0, color: '#9254de' },
  { label: '文档完成率', value: (overview.value.docRate ?? 0) + '%', color: (overview.value.docRate ?? 0) >= 100 ? '#67c23a' : '#e6a23c' },
  { label: '必填缺项', value: overview.value.missingRequired ?? 0, color: (overview.value.missingRequired ?? 0) > 0 ? '#f56c6c' : '#67c23a' }
])

const healthClass = computed(() => ({ GREEN: 'ok-text', YELLOW: 'warn-text', RED: 'danger-text' }[project.value.health] || 'muted'))
const healthText = computed(() => ({ GREEN: '正常', YELLOW: '有风险', RED: '告急' }[project.value.health] || '-'))

onMounted(async () => {
  await dict.load()
  await loadAll()
})

watch(id, loadAll)
watch(tab, (t) => { if (t === 'logs' && !logs.value.length) loadLogs() })

async function loadAll() {
  loading.value = true
  try {
    const [p, o, m, e, pr, c] = await Promise.all([
      projectApi.get(id.value),
      projectApi.overview(id.value),
      projectApi.members(id.value),
      projectApi.envs(id.value),
      projectApi.products(id.value),
      projectApi.checklist(id.value)
    ])
    project.value = p.data || {}
    overview.value = o.data || {}
    members.value = m.data || []
    envs.value = e.data || []
    products.value = pr.data || []
    checklist.value = c.data || { total: 0, done: 0, rate: 0, missingRequired: 0, items: [] }
  } finally {
    loading.value = false
  }
}

async function loadLogs() {
  logLoading.value = true
  try {
    const res = await auditApi.page({ page: logQuery.page, size: 20, projectId: id.value })
    logs.value = res.data.records || []
    logTotal.value = res.data.total || 0
  } catch (e) {
    logs.value = []
  } finally {
    logLoading.value = false
  }
}

function expiredClass(date) {
  if (!date) return 'muted'
  const days = (new Date(date) - Date.now()) / 86400000
  return days < 0 ? 'danger-text' : days <= 30 ? 'warn-text' : ''
}

const editVisible = ref(false)
const editForm = reactive({})
function openEdit() {
  Object.assign(editForm, JSON.parse(JSON.stringify(project.value)))
  editVisible.value = true
}
async function submitEdit() {
  await projectApi.update(id.value, editForm)
  ElMessage.success('已保存')
  editVisible.value = false
  loadAll()
}

const statusVisible = ref(false)
const statusForm = reactive({ status: '', reason: '' })
function openStatus() {
  statusForm.status = project.value.status
  statusForm.reason = ''
  statusVisible.value = true
}
async function submitStatus() {
  await projectApi.changeStatus(id.value, { ...statusForm })
  ElMessage.success('状态已变更')
  statusVisible.value = false
  loadAll()
}

async function toggleLock() {
  await projectApi.lock(id.value, !project.value.locked)
  ElMessage.success(project.value.locked ? '已解锁' : '已锁定，项目转为只读')
  loadAll()
}

const memberVisible = ref(false)
const memberForm = reactive({})
function openMember(row) {
  Object.assign(memberForm, row ? { ...row } : { id: null, memberType: 'CUSTOMER', name: '', role: '', phone: '', email: '', wechat: '', isMain: false, remark: '' })
  memberVisible.value = true
}
async function submitMember() {
  if (!memberForm.name) { ElMessage.warning('请填写姓名'); return }
  if (memberForm.id) await projectApi.updateMember(id.value, memberForm.id, memberForm)
  else await projectApi.addMember(id.value, memberForm)
  ElMessage.success('已保存')
  memberVisible.value = false
  loadAll()
}
async function delMember(row) {
  await ElMessageBox.confirm(`确认删除「${row.name}」？`, '提示', { type: 'warning' })
  await projectApi.removeMember(id.value, row.id)
  loadAll()
}

const envVisible = ref(false)
const envForm = reactive({})
function openEnv(row) {
  Object.assign(envForm, row ? { ...row } : { id: null, envName: '', envType: 'PROD', hostname: '', ip: '', publicIp: '', os: '', cpu: '', memoryGb: null, diskGb: null, cloudVendor: '', deployType: '', accessUrl: '', remark: '' })
  envVisible.value = true
}
async function submitEnv() {
  if (!envForm.envName) { ElMessage.warning('请填写环境名称'); return }
  if (envForm.id) await projectApi.updateEnv(id.value, envForm.id, envForm)
  else await projectApi.addEnv(id.value, envForm)
  ElMessage.success('已保存')
  envVisible.value = false
  loadAll()
}
async function delEnv(row) {
  await ElMessageBox.confirm(`确认删除环境「${row.envName}」？`, '提示', { type: 'warning' })
  await projectApi.removeEnv(id.value, row.id)
  loadAll()
}

const productVisible = ref(false)
const productForm = reactive({})
function openProduct(row) {
  Object.assign(productForm, row ? { ...row } : { id: null, productName: '', version: '', licenseNo: '', licenseType: '', licenseExpire: null, remark: '' })
  productVisible.value = true
}
async function submitProduct() {
  if (!productForm.productName) { ElMessage.warning('请填写产品名称'); return }
  if (productForm.id) await projectApi.updateProduct(id.value, productForm.id, productForm)
  else await projectApi.addProduct(id.value, productForm)
  ElMessage.success('已保存')
  productVisible.value = false
  loadAll()
}
async function delProduct(row) {
  await ElMessageBox.confirm(`确认删除「${row.productName}」？`, '提示', { type: 'warning' })
  await projectApi.removeProduct(id.value, row.id)
  loadAll()
}

async function generate() {
  const n = await projectApi.generateChecklist(id.value, project.value.projectType)
  ElMessage.success(`已生成 ${n.data} 项`)
  loadAll()
}

const itemVisible = ref(false)
const itemForm = reactive({})
function openItem(row) {
  Object.assign(itemForm, row ? { ...row } : { id: null, itemName: '', required: true, status: 'PENDING', remark: '' })
  itemVisible.value = true
}
async function submitItem() {
  if (!itemForm.itemName) { ElMessage.warning('请填写名称'); return }
  if (itemForm.id) await projectApi.updateChecklistItem(itemForm.id, itemForm)
  else await projectApi.addChecklistItem(id.value, itemForm)
  ElMessage.success('已保存')
  itemVisible.value = false
  loadAll()
}
async function saveItem(row) {
  await projectApi.updateChecklistItem(row.id, row)
  const c = await projectApi.checklist(id.value)
  checklist.value = c.data
}
async function delItem(row) {
  await ElMessageBox.confirm(`确认删除「${row.itemName}」？`, '提示', { type: 'warning' })
  await projectApi.removeChecklistItem(row.id)
  loadAll()
}
</script>

<style scoped>
.head { display: flex; justify-content: space-between; align-items: flex-start; gap: 12px; }
</style>