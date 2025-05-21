<template>
  <div class="main-layout">
    <!-- 顶部导航栏 -->
    <el-header class="header">
      <div class="logo">
        <span class="logo-text" @click="goHome">医生推荐系统</span>
      </div>
      <div class="nav-menu">
        <el-menu
          mode="horizontal"
          :router="true"
          :default-active="activeMenu"
          background-color="#ffffff"
          text-color="#303133"
          active-text-color="#409EFF"
        >
          <template v-if="isAdmin">
            <el-menu-item index="/admin/dashboard">
              <el-icon><Setting /></el-icon>
              <span>管理仪表板</span>
            </el-menu-item>
            <el-menu-item index="/admin/users">
              <el-icon><User /></el-icon>
              <span>用户管理</span>
            </el-menu-item>
            <el-menu-item index="/admin/doctors">
              <el-icon><UserFilled /></el-icon>
              <span>医生管理</span>
            </el-menu-item>
            <el-menu-item index="/admin/departments">
              <el-icon><OfficeBuilding /></el-icon>
              <span>科室管理</span>
            </el-menu-item>
          </template>
          <template v-else>
            <el-menu-item index="/home">
              <el-icon><HomeFilled /></el-icon>
              <span>首页</span>
            </el-menu-item>
            <el-menu-item index="/profile">
              <el-icon><UserFilled /></el-icon>
              <span>个人中心</span>
            </el-menu-item>
            <el-menu-item index="/doctors">
              <el-icon><List /></el-icon>
              <span>医生列表</span>
            </el-menu-item>
            <el-menu-item index="/ai-chat">
              <el-icon><ChatDotRound /></el-icon>
              <span>AI智能客服</span>
            </el-menu-item>
            <el-menu-item index="/recommend">
              <el-icon><Opportunity /></el-icon>
              <span>症状推荐</span>
            </el-menu-item>
          </template>
        </el-menu>
      </div>
      <div class="user-info">
        <el-dropdown>
          <span class="user-dropdown">
            <el-avatar :size="32" :src="userAvatar">
              <el-icon><UserFilled /></el-icon>
            </el-avatar>
            <span class="username">{{ username }}</span>
            <el-icon class="el-icon--right"><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="handleLogout">
                <el-icon><SwitchButton /></el-icon>
                <span>退出登录</span>
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>

    <!-- 主要内容区域 -->
    <el-main class="main-content">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </el-main>

    <!-- 页脚 -->
    <el-footer class="footer">
      <p>© 2025 医生推荐系统. All rights reserved.</p>
    </el-footer>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'
import {
  HomeFilled,
  User,
  UserFilled,
  List,
  ArrowDown,
  Setting,
  SwitchButton,
  OfficeBuilding,
  Opportunity,
  ChatDotRound
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

// 计算属性
const isAdmin = computed(() => userStore.role === 'admin')
const username = computed(() => userStore.userInfo?.username || '未登录')
const userAvatar = computed(() => userStore.userInfo?.avatar || '')
const activeMenu = computed(() => router.currentRoute.value.path)

// 方法
const goHome = () => {
  router.push(isAdmin.value ? '/admin/dashboard' : '/home')
}

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要退出登录吗？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 执行退出登录
    await userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
  } catch (error) {
    // 用户取消退出或发生错误时不做任何操作
  }
}
</script>

<style scoped>
.main-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  background-color: #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  padding: 0 20px;
  height: 60px;
  position: relative;
  z-index: 1000;
}

.logo {
  display: flex;
  align-items: center;
  margin-right: 40px;
}

.logo-text {
  font-size: 20px;
  font-weight: bold;
  color: #409EFF;
  cursor: pointer;
  transition: color 0.3s;
}

.logo-text:hover {
  color: #66b1ff;
}

.nav-menu {
  flex: 1;
}

.user-info {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 15px;
}

.user-dropdown {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 0 12px;
  height: 40px;
  border-radius: 20px;
  transition: background-color 0.3s;
}

.user-dropdown:hover {
  background-color: #f5f7fa;
}

.username {
  margin: 0 8px;
  font-size: 14px;
}

.main-content {
  flex: 1;
  padding: 20px;
  background-color: #f5f7fa;
}

.footer {
  text-align: center;
  padding: 20px;
  background-color: #fff;
  border-top: 1px solid #e6e6e6;
  color: #909399;
}

:deep(.el-menu--horizontal) {
  border-bottom: none;
}

:deep(.el-menu-item) {
  height: 60px;
  line-height: 60px;
  display: flex;
  align-items: center;
}

:deep(.el-menu-item .el-icon) {
  margin-right: 4px;
  font-size: 18px;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

:deep(.el-dropdown-menu__item) {
  display: flex;
  align-items: center;
  gap: 8px;
}

:deep(.el-dropdown-menu__item .el-icon) {
  margin-right: 4px;
  font-size: 16px;
}
</style>