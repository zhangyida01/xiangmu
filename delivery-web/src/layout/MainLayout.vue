<template>
  <el-container style="height: 100vh">
    <el-aside width="210px" style="background: #1f2d3d">
      <div class="logo">项目交付管理</div>
      <el-menu :default-active="activeMenu" router background-color="#1f2d3d" text-color="#c0c4cc"
               active-text-color="#ffd04b" style="border-right: 0">
        <template v-for="item in menus" :key="item.path">
          <el-menu-item :index="item.path">
            <el-icon><component :is="item.icon" /></el-icon>
            <span>{{ item.title }}</span>
          </el-menu-item>
        </template>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header style="display: flex; align-items: center; justify-content: space-between; border-bottom: 1px solid #e4e7ed">
        <div style="font-size: 16px; font-weight: 600">{{ currentTitle }}</div>
        <div style="display: flex; align-items: center; gap: 12px">
          <el-tag v-if="user.isAdmin" type="danger" size="small">管理员</el-tag>
          <el-tag v-else size="small">{{ user.role }}</el-tag>
          <el-dropdown @command="onCommand">
            <span style="cursor: pointer">
              {{ user.info?.realName || user.info?.username }}
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="password">修改密码</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main style="background: #f5f7fa; padding: 0">
        <router-view v-slot="{ Component }">
          <keep-alive :max="5">
            <component :is="Component" :key="route.fullPath" />
          </keep-alive>
        </router-view>
      </el-main>
    </el-container>

    <el-dialog v-model="pwdVisible" title="修改密码" width="420px">
      <el-form :model="pwdForm" label-width="90px">
        <el-form-item label="原密码"><el-input v-model="pwdForm.oldPassword" type="password" show-password /></el-form-item>
        <el-form-item label="新密码"><el-input v-model="pwdForm.newPassword" type="password" show-password /></el-form-item>
        <el-form-item label="确认密码"><el-input v-model="pwdForm.confirm" type="password" show-password /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pwdVisible = false">取消</el-button>
        <el-button type="primary" @click="submitPwd">确定</el-button>
      </template>
    </el-dialog>
  </el-container>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '../store/user'
import { useDictStore } from '../store/dict'
import { authApi } from '../api'
import { routes } from '../router'

const route = useRoute()
const router = useRouter()
const user = useUserStore()
const dict = useDictStore()

const pwdVisible = ref(false)
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirm: '' })

const menus = computed(() => {
  const children = routes.find((r) => r.path === '/')?.children || []
  return children
    // 首个子路由是 { path: '', redirect: '/dashboard' }，没有 meta，必须先过滤掉
    .filter((c) => c.meta && c.meta.title && !c.meta.hidden)
    .filter((c) => !c.meta.role || user.isAdmin || user.role === c.meta.role)
    .filter((c) => !c.meta.perm || user.can(c.meta.perm))
    .map((c) => ({ path: '/' + c.path, title: c.meta.title, icon: c.meta.icon || 'Menu' }))
})

const activeMenu = computed(() => {
  if (route.path.startsWith('/projects')) return '/projects'
  return route.path
})

const currentTitle = computed(() => route.meta?.title || '')

onMounted(() => {
  dict.load().catch(() => {})
})

async function onCommand(cmd) {
  if (cmd === 'logout') {
    await ElMessageBox.confirm('确认退出登录？', '提示', { type: 'warning' }).catch(() => 'cancel')
      .then(async (r) => {
        if (r === 'cancel') return
        await user.logout()
        router.push('/login')
      })
    return
  }
  if (cmd === 'password') {
    pwdForm.oldPassword = ''
    pwdForm.newPassword = ''
    pwdForm.confirm = ''
    pwdVisible.value = true
  }
}

async function submitPwd() {
  if (pwdForm.newPassword !== pwdForm.confirm) {
    ElMessage.error('两次输入的新密码不一致')
    return
  }
  await authApi.changePassword({ oldPassword: pwdForm.oldPassword, newPassword: pwdForm.newPassword })
  ElMessage.success('密码已修改，请重新登录')
  pwdVisible.value = false
  await user.logout()
  router.push('/login')
}
</script>

<style scoped>
.logo {
  height: 56px;
  line-height: 56px;
  text-align: center;
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 1px;
  background: #16222e;
}
</style>