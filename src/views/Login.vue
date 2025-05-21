<template>
  <div class="login-container">
    <div class="login-background">
      <div class="login-content">
        <el-card class="login-card">
          <template #header>
            <div class="card-header">
              <h2>医生推荐系统</h2>
            </div>
          </template>
          
          <el-form
            ref="loginFormRef"
            :model="loginForm"
            :rules="loginRules"
            label-width="80px"
            class="login-form"
          >
            <el-form-item label="用户名" prop="username">
              <el-input 
                v-model="loginForm.username" 
                placeholder="请输入用户名"
              >
                <template #prefix>
                  <el-icon><User /></el-icon>
                </template>
              </el-input>
            </el-form-item>
            
            <el-form-item label="密码" prop="password">
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="请输入密码"
                show-password
                @keyup.enter="handleLogin"
              >
                <template #prefix>
                  <el-icon><Lock /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item label="角色" prop="role">
              <el-radio-group v-model="loginForm.role">
                <el-radio label="user">用户</el-radio>
                <el-radio label="admin">管理员</el-radio>
              </el-radio-group>
            </el-form-item>
            
            <el-form-item>
              <div class="login-buttons">
                <el-button 
                  type="primary" 
                  @click="handleLogin" 
                  :loading="loading"
                  class="login-button"
                >
                  {{ loading ? '登录中...' : '登录' }}
                </el-button>
                <el-button 
                  @click="handleRegister"
                  class="register-button"
                >
                  注册
                </el-button>
              </div>
            </el-form-item>
          </el-form>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { login } from '@/api/auth'
import { useUserStore } from '@/store/user'

const router = useRouter()
const route = useRoute()
const loginFormRef = ref(null)
const loading = ref(false)
const userStore = useUserStore()

const loginForm = reactive({
  username: '',
  password: '',
  role: 'user'  // 默认选择用户角色
})

const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度必须在2-20个字符之间', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度必须在6-20个字符之间', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  try {
    await loginFormRef.value.validate()
    loading.value = true
    
    const response = await login({
      username: loginForm.username.trim(),
      password: loginForm.password,
      role: loginForm.role  // 添加角色信息到登录请求
    })

    if (response.code === 200 && response.data) {
      const { token, user, role } = response.data
      
      // 验证返回的角色是否匹配
      if (role !== loginForm.role) {
        throw new Error(`当前账号不是${loginForm.role === 'admin' ? '管理员' : '用户'}账号`)
      }

      // 更新 store
      await userStore.loginAction({ token, user, role })
      ElMessage.success('登录成功')
      
      // 获取重定向地址或根据角色跳转
      const redirect = route.query.redirect
      if (redirect && !redirect.startsWith('/login')) {
        await router.replace(redirect)
      } else {
        // 根据角色跳转到不同的首页
        await router.replace(role === 'admin' ? '/admin/dashboard' : '/home')
      }
    } else {
      throw new Error(response.message || '登录失败')
    }
  } catch (error) {
    console.error('登录错误:', error)
    if (error.response?.status === 401) {
      ElMessage.error('用户名或密码错误')
    } else if (error.response?.status === 403) {
      ElMessage.error('权限不足，无法访问')
    } else if (error.response?.status === 400) {
      ElMessage.error(error.message || '请求参数错误，请检查输入')
    } else if (error.message === 'Network Error') {
      ElMessage.error('网络连接失败，请检查网络后重试')
    } else {
      ElMessage.error(error.message || '登录失败，请稍后重试')
    }
  } finally {
    loading.value = false
  }
}

const handleRegister = () => {
  if (loginForm.role === 'admin') {
    ElMessageBox.alert('管理员账号不能通过注册创建', '提示', {
      confirmButtonText: '确定',
      type: 'warning',
      callback: () => {
        loginForm.role = 'user'
      }
    })
  } else {
    router.push('/register')
  }
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
  background-image: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.login-background {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
}

.login-content {
  width: 100%;
  max-width: 420px;
  padding: 20px;
}

.login-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.card-header {
  text-align: center;
  margin-bottom: 10px;
}

.card-header h2 {
  margin: 0;
  font-size: 24px;
  color: #303133;
}

.login-form {
  padding: 20px 0;
}

.login-buttons {
  display: flex;
  justify-content: flex-start;
  margin-left: -30px;
  gap: 20px;
  width: 100%;
}

.login-button,
.register-button {
  width: 120px;
}

:deep(.el-input__wrapper) {
  padding-left: 0;
}

:deep(.el-input__prefix) {
  width: 30px;
  justify-content: center;
  color: #909399;
}

:deep(.el-form-item__label) {
  font-weight: 500;
}

:deep(.el-radio-group) {
  display: flex;
  gap: 30px;
}
</style> 