<template>
  <div class="admin-layout">
    <!-- 左侧导航栏 -->
    <div class="sidebar">
      <div class="logo">
        <span class="logo-text" @click="goToDashboard">医生推荐系统</span>
      </div>
      <el-menu
        class="nav-menu"
        :router="true"
        :default-active="activeMenu"
      >
        <el-menu-item index="/admin/dashboard">
          <el-icon><Monitor /></el-icon>
          <span>满意度分析</span>
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
        <el-menu-item index="/admin/expertise">
          <el-icon><Star /></el-icon>
          <span>专长管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/symptoms">
          <el-icon><List /></el-icon>
          <span>症状管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/reviews">
          <el-icon><ChatDotRound /></el-icon>
          <span>评价管理</span>
        </el-menu-item>
      </el-menu>
    </div>

    <!-- 右侧内容区域 -->
    <div class="content-container">
      <!-- 顶部用户信息栏 -->
      <div class="header">
        <div class="user-info">
          <span class="role-tag">欢迎【{{ userRole }}】登录</span>
          <el-dropdown>
            <span class="user-dropdown">
              <el-avatar :size="28" :src="userAvatar">
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
      </div>

      <!-- 主要内容区域 -->
      <div class="main-content">
        <router-view></router-view>
      </div>

      <!-- 页脚 -->
      <div class="footer">
        <p>© 2025 医生推荐系统. All rights reserved.</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'
import {
  Monitor,
  User,
  UserFilled,
  OfficeBuilding,
  ArrowDown,
  SwitchButton,
  Star,
  ChatDotRound,
  List
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const username = computed(() => userStore.userInfo?.username || '未登录')
const userAvatar = computed(() => userStore.userInfo?.avatar || '')
const userRole = computed(() => userStore.role === 'admin' ? '管理员' : '用户')
const activeMenu = computed(() => router.currentRoute.value.path)

const goToDashboard = () => {
  router.push('/admin/dashboard')
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
    await userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
  } catch (error) {
    // 用户取消退出或发生错误时不做任何操作
  }
}
</script>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
  background-color: #f0f5ff;
}

.sidebar {
  width: 200px;
  background-color: #fff;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  z-index: 1000;
}

.logo {
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid #e8eaec;
  background-color: #fff;
}

.logo-text {
  font-size: 16px;
  font-weight: 600;
  color: #1890ff;
  cursor: pointer;
  transition: color 0.3s;
}

.logo-text:hover {
  color: #40a9ff;
}

.nav-menu {
  border-right: none;
  font-size: 13px;
}

.nav-menu :deep(.el-menu-item) {
  height: 40px;
  line-height: 40px;
  margin: 4px 0;
}

.nav-menu :deep(.el-menu-item.is-active) {
  background-color: #e6f7ff;
  color: #1890ff;
  border-right: 3px solid #1890ff;
}

.content-container {
  flex: 1;
  margin-left: 200px;
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.header {
  height: 50px;
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 0 20px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-dropdown {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 0 12px;
  height: 32px;
  border-radius: 16px;
  transition: background-color 0.3s;
  font-size: 13px;
}

.user-dropdown:hover {
  background-color: #f0f5ff;
}

.username {
  margin: 0 8px;
  color: #666;
}

.role-tag {
  color: #409EFF;
  font-size: 13px;
}

.main-content {
  flex: 1;
  padding: 15px;
  background-color: #f0f5ff;
  min-height: calc(100vh - 100px);
}

.footer {
  text-align: center;
  padding: 12px 0;
  background-color: #fff;
  color: #666;
  font-size: 12px;
  border-top: 1px solid #e8eaec;
}

/* 过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

:deep(.el-menu-item) [class^="el-icon"] {
  font-size: 16px;
  margin-right: 8px;
}

:deep(.el-dropdown-menu__item) {
  font-size: 13px;
  display: flex;
  align-items: center;
}

:deep(.el-dropdown-menu__item .el-icon) {
  margin-right: 8px;
}
</style>