<template>
  <div class="login-wrap">
    <el-card class="login-card" shadow="always">
      <h2 class="title">项目交付管理后台</h2>
      <p class="subtitle">项目台账 · 交付文档 · 凭据保险箱</p>
      <el-form :model="form" @keyup.enter="submit">
        <el-form-item>
          <el-input v-model="form.username" placeholder="账号" size="large" :prefix-icon="User" clearable />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" type="password" placeholder="密码" size="large"
                    :prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item>
          <div style="display: flex; gap: 8px; width: 100%">
            <el-input v-model="form.captcha" placeholder="验证码" size="large" :prefix-icon="Picture" />
            <div class="captcha" @click="loadCaptcha" title="点击刷新" v-html="captchaSvg"></div>
          </div>
        </el-form-item>
        <el-button type="primary" size="large" style="width: 100%" :loading="loading" @click="submit">登 录</el-button>
      </el-form>
    </el-card>

    <el-dialog v-model="pwdVisible" title="首次登录请修改密码" width="420px" :close-on-click-modal="false"
               :show-close="false">
      <el-form label-width="90px">
        <el-form-item label="原密码"><el-input v-model="pwd.oldPassword" type="password" show-password /></el-form-item>
        <el-form-item label="新密码"><el-input v-model="pwd.newPassword" type="password" show-password /></el-form-item>
        <el-form-item label="确认密码"><el-input v-model="pwd.confirm" type="password" show-password /></el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitPwd">确定并进入系统</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Lock, Picture, User } from '@element-plus/icons-vue'
import { useUserStore } from '../store/user'
import { authApi } from '../api'

const router = useRouter()
const route = useRoute()
const user = useUserStore()

const form = reactive({ username: '', password: '', captcha: '', captchaId: '' })
const captchaSvg = ref('')
const loading = ref(false)
const pwdVisible = ref(false)
const pwd = reactive({ oldPassword: '', newPassword: '', confirm: '' })

onMounted(loadCaptcha)

async function loadCaptcha() {
  const res = await authApi.captcha()
  form.captchaId = res.data.captchaId
  captchaSvg.value = res.data.svg
  form.captcha = ''
}

async function submit() {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入账号和密码')
    return
  }
  loading.value = true
  try {
    const data = await user.login({ ...form })
    if (data.user.mustChangePwd) {
      pwd.oldPassword = form.password
      pwd.newPassword = ''
      pwd.confirm = ''
      pwdVisible.value = true
      return
    }
    goto()
  } catch (e) {
    loadCaptcha()
  } finally {
    loading.value = false
  }
}

async function submitPwd() {
  if (pwd.newPassword !== pwd.confirm) {
    ElMessage.error('两次输入的新密码不一致')
    return
  }
  await authApi.changePassword({ oldPassword: pwd.oldPassword, newPassword: pwd.newPassword })
  ElMessage.success('密码修改成功')
  pwdVisible.value = false
  await user.loadMe()
  goto()
}

function goto() {
  router.replace(route.query.redirect || '/dashboard')
}
</script>

<style scoped>
.login-wrap {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1f2d3d 0%, #3a5068 100%);
}
.login-card { width: 380px; padding: 8px 12px 20px; }
.title { text-align: center; margin: 8px 0 4px; font-size: 20px; }
.subtitle { text-align: center; color: #909399; margin: 0 0 20px; font-size: 12px; }
.captcha {
  width: 120px;
  height: 40px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  cursor: pointer;
  overflow: hidden;
  flex: none;
}
.captcha :deep(svg) { display: block; }
</style>