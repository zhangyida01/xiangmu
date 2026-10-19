<template>
  <div class="page">
    <el-alert type="info" :closable="false" show-icon class="card-gap"
              title="密码以 AES-256-GCM 加密存储，列表默认掩码显示；查看明文与复制均会写入审计日志。" />
    <CredentialPanel />
  </div>
</template>

<script setup>
import CredentialPanel from '../components/CredentialPanel.vue'
</script>