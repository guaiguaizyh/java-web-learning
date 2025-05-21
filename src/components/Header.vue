<template>
  <el-header class="header">
    <div class="logo">
      <router-link to="/">医生推荐系统</router-link>
    </div>
    <el-menu
      mode="horizontal"
      :router="true"
      :default-active="activeIndex"
    >
      <el-menu-item index="/home">首页</el-menu-item>
      <el-menu-item index="/doctors">查看医生</el-menu-item>
      <el-menu-item index="/ai-chat">
        <span class="ai-menu-item">
          <el-badge value="AI" type="primary">
            AI助手
          </el-badge>
        </span>
      </el-menu-item>
      <el-menu-item index="/profile">个人中心</el-menu-item>
    </el-menu>
    <div class="user-info">
      <el-button 
        type="primary" 
        class="ai-btn"
        @click="router.push('/ai-chat')"
      >
        <el-icon><ChatDotRound /></el-icon>
        AI智能客服
      </el-button>
      <el-dropdown trigger="click">
        <span class="user-dropdown">
          <el-avatar :size="32" :src="userStore.userInfo?.avatar">
            {{ userStore.userInfo?.username?.charAt(0)?.toUpperCase() }}
          </el-avatar>
          <span class="username">{{ userStore.userInfo?.username }}</span>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item @click="router.push('/profile')">
              <el-icon><User /></el-icon>个人资料
            </el-dropdown-item>
            <el-dropdown-item @click="handleLogout">
              <el-icon><SwitchButton /></el-icon>退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </el-header>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'
import { User, SwitchButton, ChatDotRound } from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const activeIndex = computed(() => route.path)

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    userStore.logout()
    router.push('/login')
  }).catch(() => {})
}
</script>

<style scoped>
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.logo {
  font-size: 24px;
  font-weight: bold;
}

.logo a {
  color: #409eff;
  text-decoration: none;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.ai-btn {
  font-weight: 500;
  animation: pulse 2s infinite;
  padding: 8px 16px;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 5px;
}

.ai-btn .el-icon {
  font-size: 16px;
}

@keyframes pulse {
  0% {
    box-shadow: 0 0 0 0 rgba(64, 158, 255, 0.4);
  }
  70% {
    box-shadow: 0 0 0 10px rgba(64, 158, 255, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(64, 158, 255, 0);
  }
}

.ai-menu-item {
  display: flex;
  align-items: center;
}

.user-dropdown {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 0 12px;
}

.username {
  margin-left: 8px;
  font-size: 16px;
  color: #606266;
}

:deep(.el-menu) {
  border-bottom: none;
}

:deep(.el-menu--horizontal > .el-menu-item) {
  font-size: 16px;
  border-bottom: 2px solid transparent;
}

:deep(.el-menu--horizontal > .el-menu-item.is-active) {
  border-bottom: 2px solid #409eff;
}

:deep(.el-dropdown-menu__item) {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}
</style> 