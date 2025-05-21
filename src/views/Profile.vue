<template>
  <div class="profile-container">
    <el-row :gutter="20">
      <el-col :span="16">
        <el-card class="profile-card">
          <template #header>
            <div class="card-header">
              <h3>个人资料</h3>
              <div class="header-actions">
                <el-button type="primary" @click="handleEdit" v-if="!isEditing">编辑资料</el-button>
                <template v-else>
                  <el-button @click="cancelEdit">取消</el-button>
                  <el-button type="primary" @click="saveProfile" :loading="loading">保存</el-button>
                </template>
              </div>
            </div>
          </template>
          
          <el-form
            ref="formRef"
            :model="profileForm"
            :rules="formRules"
            :disabled="!isEditing"
            label-width="100px"
          >
            <el-form-item label="用户名" prop="username">
              <el-input v-model="profileForm.username" disabled />
            </el-form-item>
            
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="profileForm.phone" maxlength="11" />
            </el-form-item>
            
            <el-form-item label="年龄" prop="age">
              <el-input-number
                v-model="profileForm.age"
                :min="0"
                :max="150"
              />
            </el-form-item>
            
            <el-form-item label="性别" prop="gender">
              <el-radio-group v-model="profileForm.gender">
                <el-radio label="男">男</el-radio>
                <el-radio label="女">女</el-radio>
              </el-radio-group>
            </el-form-item>
            
            <el-form-item label="病历记录" prop="medicalRecord">
              <el-input
                v-model="profileForm.medicalRecord"
                type="textarea"
                :rows="4"
                placeholder="请输入您的病历记录"
              />
            </el-form-item>

            <el-form-item label="创建时间">
              <div>{{ formatDate(profileForm.createTime) }}</div>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card class="actions-card">
          <template #header>
            <h3>账号操作</h3>
          </template>
          
          <div class="action-buttons">
            <el-button type="primary" @click="handleChangePassword" class="action-button">
              修改密码
            </el-button>
            <el-button type="danger" @click="handleDeleteAccount" class="action-button">
              注销账号
            </el-button>
          </div>
        </el-card>

        <!-- 我的评价卡片 -->
        <el-card class="reviews-card">
          <template #header>
            <div class="card-header">
              <h3>我的评价</h3>
              <el-button type="primary" text @click="showUserReviews">
                查看全部
              </el-button>
            </div>
          </template>
          
          <div class="reviews-preview" v-loading="reviewsLoading">
            <el-empty v-if="!reviewsLoading && (!recentReviews || recentReviews.length === 0)" description="暂无评价" />
            <div v-else class="review-list">
              <div v-for="review in recentReviews" :key="review.id" class="review-item">
                <div class="review-header">
                  <span class="doctor-name">{{ review.doctorName }}</span>
                  <el-rate v-model="review.rating" disabled show-score />
                </div>
                <div class="review-content">{{ review.comment }}</div>
                <div class="review-footer">
                  <span class="review-time">{{ formatDate(review.createdAt) }}</span>
                  <el-button type="danger" size="small" text @click="handleDeleteReview(review)">
                    删除
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 修改密码对话框 -->
    <el-dialog
      v-model="passwordDialogVisible"
      title="修改密码"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="passwordFormRef"
        :model="passwordForm"
        :rules="passwordFormRules"
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
      </el-form>
      
      <div class="password-tips">
        <h4>密码要求：</h4>
        <ul>
          <li>长度在6-20个字符之间</li>
          <li>不能与当前密码相同</li>
        </ul>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="passwordDialogVisible = false">取消</el-button>
          <el-button @click="resetPasswordForm">重置</el-button>
          <el-button type="primary" @click="submitPasswordChange" :loading="passwordLoading">
            确认修改
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 用户评价对话框 -->
    <UserReviews v-model="userReviewsVisible" />
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from '@/utils/axios'
import { useUserStore } from '@/store/user'
import { getReviewsList, deleteReview } from '@/api/reviews'
import UserReviews from '@/components/UserReviews.vue'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)
const isEditing = ref(false)

// 添加密码修改对话框相关数据
const passwordDialogVisible = ref(false)
const passwordFormRef = ref(null)
const passwordLoading = ref(false)

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

const passwordFormRules = {
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

// 修改密码
const submitPasswordChange = async () => {
  if (!passwordFormRef.value) return

  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      passwordLoading.value = true
      try {
        console.log('正在发送修改密码请求...')
        const response = await axios.post('/users/change-password', {
          oldPassword: passwordForm.oldPassword,
          newPassword: passwordForm.newPassword
        })
        console.log('收到响应:', response)

        if (response.code === 200) {
          ElMessage.success(response.message || '密码修改成功，请重新登录')
          passwordDialogVisible.value = false
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
        passwordLoading.value = false
      }
    }
  })
}

// 重置修改密码表单
const resetPasswordForm = () => {
  if (passwordFormRef.value) {
    passwordFormRef.value.resetFields()
  }
}

const profileForm = ref({
  username: '',
  phone: '',
  age: 0,
  gender: '男',
  medicalRecord: '',
  createTime: '',
  updateTime: ''
})

const formRules = {
  phone: [
    { required: true, message: '手机号不能为空', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号（11位）', trigger: 'blur' }
  ],
  age: [
    { required: true, message: '年龄不能为空', trigger: 'blur' },
    { type: 'number', min: 0, max: 150, message: '年龄必须在0-150岁之间', trigger: 'blur' }
  ],
  gender: [
    { required: true, message: '请选择您的性别', trigger: 'change' }
  ]
}

const fetchProfile = async () => {
  try {
    const response = await axios.get('/profile/me')
    if (response.code === 200) {
      const userData = response.data
      profileForm.value = {
        username: userData.username,
        phone: userData.phone,
        age: userData.age,
        gender: userData.gender,
        medicalRecord: userData.medicalRecord,
        createTime: userData.createTime,
        updateTime: userData.updateTime
      }
    }
  } catch (error) {
    console.error('获取个人信息失败:', error)
    ElMessage.error('获取个人信息失败，请刷新页面重试')
  }
}

const handleEdit = () => {
  isEditing.value = true
}

const cancelEdit = () => {
  isEditing.value = false
  fetchProfile() // 重新获取数据，放弃修改
}

const saveProfile = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    loading.value = true
    
    const updateData = {
      phone: profileForm.value.phone,
      age: profileForm.value.age,
      gender: profileForm.value.gender,
      medicalRecord: profileForm.value.medicalRecord
    }

    const response = await axios.put('/profile/me', updateData)
    
    if (response.code === 200) {
      // 更新store中的用户信息，保留原有信息
      const updatedUserInfo = {
        ...userStore.userInfo,  // 保留现有信息
        ...updateData          // 更新修改的字段
      }
      userStore.updateUserInfo(updatedUserInfo)
      
      ElMessage.success('个人信息已成功更新')
      isEditing.value = false
      await fetchProfile()  // 重新获取完整的个人信息
    } else {
      throw new Error(response.message || '更新失败')
    }
  } catch (error) {
    console.error('更新个人信息失败:', error)
    ElMessage.error(error.response?.message || error.message || '更新个人信息失败，请稍后再试')
  } finally {
    loading.value = false
  }
}

const handleDeleteAccount = () => {
  ElMessageBox.confirm(
    '您确定要注销账号吗？注销后所有个人数据将被永久删除且无法恢复。',
    '注销账号确认',
    {
      confirmButtonText: '确认注销',
      cancelButtonText: '我再想想',
      type: 'warning',
      closeOnClickModal: false
    }
  ).then(async () => {
    try {
      const response = await axios.delete('/profile/me')
      if (response.code === 200) {
        ElMessage.success(response.message || '您的账号已成功注销，感谢您的使用')
        userStore.logout()
        router.push('/login')
      }
    } catch (error) {
      console.error('注销账号失败:', error)
      ElMessage.error(error.response?.message || '注销账号失败，请联系管理员')
    }
  }).catch(() => {
    // 用户取消操作，不做任何处理
  })
}

const handleChangePassword = () => {
  passwordDialogVisible.value = true
}

const formatDate = (date) => {
  if (!date) return '暂无'
  return new Date(date).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

// 评价相关数据
const userReviewsVisible = ref(false)
const reviewsLoading = ref(false)
const recentReviews = ref([])

// 获取最近的评价
const fetchRecentReviews = async () => {
  reviewsLoading.value = true
  try {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    const userId = userInfo.userId || userInfo.id

    if (!userId) {
      throw new Error('未找到用户信息')
    }

    const response = await getReviewsList({
      userId: userId,
      page: 1,
      size: 3
    })

    if (response.code === 200) {
      const records = response.data.records

      // 获取所有医生ID
      const doctorIds = [...new Set(records.map(review => review.doctorId))]
      
      // 获取医生和科室信息
      const doctorPromises = doctorIds.map(async (doctorId) => {
        try {
          const doctorResponse = await axios.get(`/doctors/${doctorId}`)
          if (doctorResponse.code === 200) {
            const doctor = doctorResponse.data
            return {
              doctorId,
              ...doctor
            }
          }
        } catch (error) {
          console.error(`获取医生信息失败 (ID: ${doctorId}):`, error)
        }
        return null
      })

      // 并行获取医生信息
      const doctors = await Promise.all(doctorPromises)
      const doctorMap = new Map(doctors.filter(d => d).map(d => [d.doctorId, d]))
      
      // 处理评价数据
      recentReviews.value = records.map(review => ({
        id: review.reviewId || review.id,
        ...review,
        doctorName: doctorMap.get(review.doctorId)?.name || '未知医生',
        rating: review.rating,
        comment: review.comment || review.content || '',
        createdAt: review.createdAt
      }))
    }
  } catch (error) {
    console.error('获取用户评价失败:', error)
    ElMessage.error('获取用户评价失败')
    recentReviews.value = []
  } finally {
    reviewsLoading.value = false
  }
}

// 删除评价
const handleDeleteReview = async (review) => {
  try {
    await ElMessageBox.confirm('确认删除该评价吗？此操作不可恢复！', '警告', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    
    const response = await deleteReview(review.id)
    if (response.code === 200) {
      ElMessage.success('删除成功')
      // 重新获取评价列表
      fetchRecentReviews()
    } else {
      throw new Error(response.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error(error.message || '删除失败')
    }
  }
}

const showUserReviews = () => {
  userReviewsVisible.value = true
}

onMounted(() => {
  fetchProfile()
  fetchRecentReviews()
})
</script>

<style scoped>
.profile-container {
  padding: 20px;
}

.profile-card,
.actions-card {
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

.header-actions {
  display: flex;
  gap: 10px;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.action-button {
  width: 100%;
  margin: 0;
}

:deep(.el-form-item__content) {
  flex-wrap: nowrap;
}

.reviews-card {
  margin-top: 20px;
}

.reviews-preview {
  min-height: 200px;
}

.review-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.review-item {
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 12px;
}

.review-item:last-child {
  border-bottom: none;
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.doctor-name {
  font-weight: 500;
  color: #303133;
}

.review-content {
  color: #606266;
  font-size: 14px;
  margin-bottom: 8px;
  line-height: 1.4;
}

.review-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #909399;
}

:deep(.el-rate) {
  display: inline-flex;
  height: 20px;
}

:deep(.el-rate__item) {
  margin-right: 4px;
}

:deep(.el-button--text.el-button--small) {
  padding: 0;
}
</style> 