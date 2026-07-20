<template>
  <el-container class="layout-container">
    <el-aside width="200px">
      <div class="logo">
        <h3>项目管理系统</h3>
      </div>
      <el-menu :default-active="activeMenu" router>
        <el-menu-item index="/dashboard">
          <el-icon><DataLine /></el-icon>
          <span>首页</span>
        </el-menu-item>
        <el-menu-item index="/user">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/customer">
          <el-icon><OfficeBuilding /></el-icon>
          <span>客户管理</span>
        </el-menu-item>
        <el-menu-item index="/supplier">
          <el-icon><ShoppingCart /></el-icon>
          <span>供应商管理</span>
        </el-menu-item>
        <el-menu-item index="/project">
          <el-icon><Folder /></el-icon>
          <span>项目管理</span>
        </el-menu-item>
        <el-menu-item index="/project-account">
          <el-icon><Key /></el-icon>
          <span>项目账户</span>
        </el-menu-item>
        <el-menu-item index="/ticket">
          <el-icon><Tickets /></el-icon>
          <span>工单管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header>
        <div class="header-content">
          <h3>{{ route.meta.title }}</h3>
          <div class="user-info">
            <span>{{ userStore.userInfo.realName || userStore.userInfo.username || '未知用户' }}</span>
            <el-button type="danger" size="small" @click="handleLogout">退出</el-button>
          </div>
        </div>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '../store/user'
import { ElMessageBox } from 'element-plus'

const route = useRoute()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    userStore.logout()
  })
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.el-aside {
  background: #304156;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #2b3947;
}

.logo h3 {
  color: #fff;
  margin: 0;
  font-size: 16px;
}

.el-menu {
  border-right: none;
  background: #304156;
}

:deep(.el-menu-item) {
  color: #bfcbd9;
}

:deep(.el-menu-item:hover),
:deep(.el-menu-item.is-active) {
  background: #263445 !important;
  color: #409eff;
}

.el-header {
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  display: flex;
  align-items: center;
  padding: 0 20px;
}

.header-content {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-content h3 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-info span {
  color: #606266;
}

.el-main {
  background: #f0f2f5;
  padding: 20px;
}
</style>