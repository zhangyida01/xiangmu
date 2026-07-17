<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #409eff">
              <el-icon :size="32"><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.userCount }}</div>
              <div class="stat-label">用户总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #67c23a">
              <el-icon :size="32"><Folder /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.projectCount }}</div>
              <div class="stat-label">项目总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #e6a23c">
              <el-icon :size="32"><Tickets /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.ticketCount }}</div>
              <div class="stat-label">工单总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #f56c6c">
              <el-icon :size="32"><Warning /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.pendingTicket }}</div>
              <div class="stat-label">待处理工单</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="welcome-card" style="margin-top: 20px">
      <h2>欢迎使用交付项目管理系统</h2>
      <p>系统功能：</p>
      <ul>
        <li>✅ 用户管理：完整的用户CRUD操作</li>
        <li>✅ 项目管理：项目全生命周期管理</li>
        <li>✅ 工单管理：售后服务工单处理</li>
        <li>✅ JWT认证：安全的用户认证机制</li>
      </ul>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getUserList } from '../api/user'
import { getProjectList } from '../api/project'
import { getTicketList } from '../api/ticket'

const stats = ref({
  userCount: 0,
  projectCount: 0,
  ticketCount: 0,
  pendingTicket: 0
})

const loadStats = async () => {
  try {
    const [userRes, projectRes, ticketRes, pendingRes] = await Promise.all([
      getUserList({ page: 1, size: 1 }),
      getProjectList({ page: 1, size: 1 }),
      getTicketList({ page: 1, size: 1 }),
      getTicketList({ page: 1, size: 1, status: 0 })
    ])
    
    stats.value.userCount = userRes.data.total || 0
    stats.value.projectCount = projectRes.data.total || 0
    stats.value.ticketCount = ticketRes.data.total || 0
    stats.value.pendingTicket = pendingRes.data.total || 0
  } catch (error) {
    console.error('加载统计数据失败', error)
  }
}

onMounted(() => {
  loadStats()
})
</script>

<style scoped>
.dashboard {
  width: 100%;
}

.stat-card {
  cursor: pointer;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 20px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.welcome-card h2 {
  margin: 0 0 16px 0;
  color: #303133;
}

.welcome-card p {
  margin: 12px 0;
  color: #606266;
}

.welcome-card ul {
  margin: 12px 0;
  padding-left: 20px;
}

.welcome-card li {
  margin: 8px 0;
  color: #606266;
  line-height: 1.6;
}
</style>
