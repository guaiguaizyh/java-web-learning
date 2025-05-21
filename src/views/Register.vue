<template>
  <div class="register-container">
    <div class="register-background">
      <div class="register-content">
        <el-card class="register-card">
          <template #header>
            <div class="card-header">
              <h2>医生推荐系统</h2>
            </div>
          </template>
          
          <el-form
            ref="registerFormRef"
            :model="registerForm"
            :rules="registerRules"
            label-width="80px"
            class="register-form"
          >
            <el-form-item label="用户名" prop="username">
              <el-input 
                v-model="registerForm.username" 
                placeholder="请输入用户名"
                prefix-icon="User"
              />
            </el-form-item>
            
            <el-form-item label="密码" prop="password">
              <el-input
                v-model="registerForm.password"
                type="password"
                placeholder="请输入密码"
                show-password
                prefix-icon="Lock"
              />
            </el-form-item>

            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input
                v-model="registerForm.confirmPassword"
                type="password"
                placeholder="请再次输入密码"
                show-password
                prefix-icon="Lock"
              />
            </el-form-item>

            <el-form-item label="年龄" prop="age">
              <el-input-number
                v-model="registerForm.age"
                :min="0"
                :max="100"
                placeholder="请输入年龄"
                :controls="false"
                style="width: 100%"
              />
            </el-form-item>
         
            <el-form-item label="性别" prop="gender">
              <el-radio-group v-model="registerForm.gender">
                <el-radio label="男">男</el-radio>
                <el-radio label="女">女</el-radio>
              </el-radio-group>
            </el-form-item>

            <el-form-item label="手机号" prop="phone">
              <el-input 
                v-model="registerForm.phone" 
                placeholder="请输入手机号"
                prefix-icon="Phone"
              />
            </el-form-item>
            
            <el-form-item label="症状描述">
              <el-input
                v-model="registerForm.medicalRecord"
                type="textarea"
                :rows="3"
                placeholder="请输入症状描述（选填）"
              />
            </el-form-item>
            
            <el-form-item>
              <div class="register-buttons">
                <el-button 
                  type="primary" 
                  @click="handleRegister" 
                  :loading="loading"
                  class="register-submit-button"
                >
                  {{ loading ? '注册中...' : '注册' }}
                </el-button>
                <el-button 
                  @click="goToLogin"
                  class="back-button"
                >
                  返回登录
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
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Lock, Phone } from '@element-plus/icons-vue'
import { register, checkUsername } from '@/api/auth'

const router = useRouter()
const registerFormRef = ref(null)
const loading = ref(false)

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  age: 18,
  gender: '男',
  phone: '',
  medicalRecord: ''
})

const validateUsername = async (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入用户名'))
    return
  }
  
  if (value.length < 2 || value.length > 20) {
    callback(new Error('用户名长度必须在2-20个字符之间'))
    return
  }
  
  try {
    const response = await checkUsername(value)
    if (response && response.code === 200) {
      if (response.data) {
        callback(new Error('用户名已存在'))
      } else {
        callback()
      }
    } else {
      callback(new Error('检查用户名失败，请稍后重试'))
    }
  } catch (error) {
    console.error('检查用户名错误:', error)
    callback(new Error('检查用户名失败，请稍后重试'))
  }
}

const validatePass = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入密码'))
  } else {
    if (registerForm.confirmPassword !== '') {
      registerFormRef.value?.validateField('confirmPassword')
    }
    callback()
  }
}

const validatePass2 = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入密码不一致!'))
  } else {
    callback()
  }
}

const validatePhone = (rule, value, callback) => {
  const phoneReg = /^1[3-9]\d{9}$/
  if (!value) {
    callback(new Error('请输入手机号'))
  } else if (!phoneReg.test(value)) {
    callback(new Error('请输入正确的手机号'))
  } else {
    callback()
  }
}

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { validator: validateUsername, trigger: 'blur' }
  ],
  password: [
    { required: true, validator: validatePass, trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度必须在6-20个字符之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validatePass2, trigger: 'blur' }
  ],
  age: [
    { required: true, message: '请输入年龄', trigger: 'blur' },
    { type: 'number', min: 0, max: 100, message: '年龄必须在0-100之间', trigger: 'blur' }
  ],
  gender: [
    { required: true, message: '请选择性别', trigger: 'change' }
  ],
  phone: [
    { required: true, validator: validatePhone, trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  medicalRecord: [
    // 不再是必填项
  ]
}

const handleRegister = async () => {
  if (!registerFormRef.value) return
  
  try {
    await registerFormRef.value.validate()
    loading.value = true
    
    const response = await register({
      username: registerForm.username,
      password: registerForm.password,
      age: registerForm.age,
      gender: registerForm.gender,
      phone: registerForm.phone,
      medicalRecord: registerForm.medicalRecord
    })
    
    console.log('注册响应:', response)
    
    if (response && response.code === 200) {
      ElMessage.success(response.message || '注册成功')
      router.push('/login')
    } else {
      // 注册失败
      ElMessage.error(response?.message || '注册失败，请检查输入信息')
    }
  } catch (error) {
    console.error('注册错误:', error)
    if (error.response?.status === 400) {
      // 参数错误或用户名已存在
      ElMessage.error(error.response?.data?.message || '注册失败，请检查输入信息')
    } else if (error.message === 'Network Error') {
      // 网络错误
      ElMessage.error('网络连接失败，请检查网络后重试')
    } else {
      // 其他错误
      ElMessage.error('注册失败，请稍后重试')
    }
  } finally {
    loading.value = false
  }
}

const goToLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
.register-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
  background-image: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.register-background {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  background-image: url('@/assets/login-bg.jpg');
  background-size: cover;
  background-position: center;
}

.register-content {
  width: 100%;
  max-width: 500px;
  padding: 20px;
}

.register-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  background-color: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  width: 100%;
}

.card-header {
  text-align: center;
  padding: 10px 0;
}

.card-header h2 {
  margin: 0;
  color: #303133;
  font-size: 24px;
}

.register-form {
  margin-top: 20px;
  padding: 0 20px;
}

.register-buttons {
  display: flex;
  justify-content: center;
  gap: 30px;
  margin-top: 30px;
}

.register-submit-button {
  width: 120px;
  height: 40px;
  font-size: 16px;
}

.back-button {
  width: 120px;
  height: 40px;
  font-size: 16px;
  border: 1px solid #409eff;
  color: #409eff;
}

.back-button:hover {
  background-color: #ecf5ff;
  color: #409eff;
}

:deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px #dcdfe6 inset;
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #409eff inset;
}

:deep(.el-form-item__label) {
  font-weight: 500;
}

:deep(.el-input-number) {
  width: 100%;
}

:deep(.el-input-number .el-input__wrapper) {
  padding: 0 15px;
  text-align: center;
}

:deep(.el-input-number .el-input__inner) {
  text-align: center;
  font-size: 16px;
}
</style> 