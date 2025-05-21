<template>
  <div class="change-password-container">
    <el-card class="password-card">
      <template #header>
        <div class="card-header">
          <h3>修改密码</h3>
          <el-button @click="router.back()">返回</el-button>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="passwordForm"
        :rules="formRules"
        label-width="120px"
        class="password-form"
      >
        <el-form-item label="当前密码" prop="oldPassword">
          <el-input
            v-model="passwordForm.oldPassword"
            type="password"
            placeholder="请输入当前密码"
            show-password
          />
        </el-form-item>

        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            placeholder="请输入新密码"
            show-password
          />
        </el-form-item>

        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            @click="handleSubmit"
          >
            确认修改
          </el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>

      <div class="password-tips">
        <h4>密码要求：</h4>
        <ul>
          <li>长度在6-20个字符之间</li>
          <li>不能与当前密码相同</li>
        </ul>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from '@/utils/axios'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 密码验证规则
const validatePass = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入密码'))
  } else if (value.length < 6 || value.length > 20) {
    callback(new Error('密码长度必须在6-20个字符之间'))
  } else {
    callback()
  }
}

// 确认密码验证
const validateConfirmPass = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const formRules = {
  oldPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度必须在6-20个字符之间', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, trigger: 'blur', validator: validatePass }
  ],
  confirmPassword: [
    { required: true, trigger: 'blur', validator: validateConfirmPass }
  ]
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        console.log('正在发送修改密码请求...')
        const response = await axios.post('/users/change-password', {
          oldPassword: passwordForm.oldPassword,
          newPassword: passwordForm.newPassword
        })
        console.log('收到响应:', response)

        if (response.code === 200) {
          ElMessage.success(response.message || '密码修改成功，请重新登录')
          userStore.logout()
          router.push('/login')
        }
      } catch (error) {
        console.error('修改密码失败:', error)
        // 处理 403 权限不足的情况
        if (error.response?.status === 403) {
          ElMessage.error('权限不足，请确认是否已登录')
          router.push('/login')
          return
        }
        // 处理错误情况
        if (error.response?.data?.code === 500 && error.response?.data?.message === "旧密码不正确") {
          ElMessage.error('旧密码不正确')
        } else {
          ElMessage.error(
            error.response?.data?.message || 
            error.response?.data?.msg || 
            '修改密码失败，请稍后重试'
          )
        }
      } finally {
        loading.value = false
      }
    }
  })
}

const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
}
</script>

<style scoped>
.change-password-container {
  padding: 20px;
  max-width: 600px;
  margin: 0 auto;
}

.password-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}

.password-form {
  margin-top: 20px;
}

.password-tips {
  margin-top: 30px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.password-tips h4 {
  margin: 0 0 10px;
  color: #606266;
}

.password-tips ul {
  margin: 0;
  padding-left: 20px;
  color: #909399;
}

.password-tips li {
  margin-bottom: 5px;
}

:deep(.el-form-item__content) {
  flex-wrap: nowrap;
}
</style> 