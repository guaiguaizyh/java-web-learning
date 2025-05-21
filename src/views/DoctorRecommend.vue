<template>
  <div class="recommend-container">
    <el-card class="description-card">
      <template #header>
        <div class="card-header">
          <h3>智能医生推荐</h3>
          <p>使用日常语言描述您的症状，我们的AI将为您推荐适合的医生</p>
        </div>
      </template>
      
      <div class="description-input">
        <el-form :model="nlpForm" label-width="120px">
          <el-form-item label="症状描述">
            <el-input
              v-model="nlpForm.description"
              type="textarea"
              :rows="4"
              placeholder="例如：最近几天感到头晕目眩，站起来时特别明显，有时还会伴随耳鸣..."
              class="description-textarea"
            />
          </el-form-item>
          
          <el-form-item>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-button type="primary" @click="getNLPRecommendation" 
                  :disabled="!nlpForm.description || nlpLoading" 
                  :loading="nlpLoading">
                  <el-icon><SearchIcon /></el-icon> 快速推荐
                </el-button>
              </el-col>
              <el-col :span="12">
                <div class="ai-button-container">
                  <el-button type="success" @click="getAIDirectRecommendation" 
                    :disabled="!nlpForm.description || aiDirectLoading" 
                    :loading="aiDirectLoading">
                    <el-icon><CpuIcon /></el-icon> AI精准推荐
                  </el-button>
                  <div class="recommendation-info">
                    <el-icon><InfoFilled /></el-icon>
                    <span>AI精准推荐使用深度分析，适用于复杂症状描述，但需要更长响应时间</span>
                  </div>
                </div>
              </el-col>
            </el-row>
          </el-form-item>
        </el-form>
      </div>
    </el-card>

    <div v-if="isLoading" class="loading-container">
      <el-skeleton :rows="6" animated />
    </div>

    <el-alert
      v-if="errorMessage"
      :title="errorMessage"
      type="error"
      show-icon
      @close="errorMessage = ''"
      style="margin: 20px 0"
    />

    <el-card v-if="recommendedDoctors.length > 0 && !isLoading" class="doctor-list-card">
      <template #header>
        <div class="card-header">
          <h3>推荐医生</h3>
          <p v-if="recommendReason">{{ recommendReason }}</p>
          <p v-else>根据您的症状，我们为您推荐以下医生</p>
        </div>
      </template>

      <el-row :gutter="20" class="doctor-list">
        <el-col v-for="doctor in recommendedDoctors" :key="doctor.id || doctor.doctorId" :xs="24" :sm="12" :md="8" :lg="6">
          <el-card class="doctor-card" :body-style="{ padding: '0px' }">
            <div class="doctor-avatar">
              <el-avatar :size="100" :src="doctor.avatar || doctor.avatarUrl">
                <el-icon><UserFilled /></el-icon>
              </el-avatar>
            </div>
            <div class="doctor-info">
              <h3 class="doctor-name">{{ doctor.name }}</h3>
              <p class="title">{{ doctor.title || doctor.positionsName }}</p>
              <p class="department">{{ doctor.department || doctor.departmentName }}</p>
              <div class="match-score">
                <el-progress type="dashboard" :percentage="getMatchScorePercentage(doctor)" :color="getColorForScore(getMatchScorePercentage(doctor))">
                  <template #default="{ percentage }">
                    <span class="progress-value">{{ percentage }}%</span>
                    <span class="progress-label">匹配度</span>
                  </template>
                </el-progress>
              </div>
              <div v-if="doctor.recommendReason" class="recommend-reason">
                {{ doctor.recommendReason }}
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </el-card>

    <!-- 医生详情对话框 -->
    <el-dialog
      v-model="doctorDetailVisible"
      :title="selectedDoctor ? selectedDoctor.name + ' 医生详情' : '医生详情'"
      width="60%"
      class="doctor-detail-dialog"
    >
      <div v-if="selectedDoctor" class="doctor-details">
        <el-row :gutter="20">
          <el-col :span="8">
            <div class="doctor-avatar-large">
              <el-avatar :size="150" :src="selectedDoctor.avatar || selectedDoctor.avatarUrl">
                <el-icon><UserFilled /></el-icon>
              </el-avatar>
            </div>
          </el-col>
          <el-col :span="16">
            <el-descriptions :column="1" border>
              <el-descriptions-item label="姓名">{{ selectedDoctor.name }}</el-descriptions-item>
              <el-descriptions-item label="职称">{{ selectedDoctor.title || selectedDoctor.positionsName }}</el-descriptions-item>
              <el-descriptions-item label="科室">{{ selectedDoctor.department || selectedDoctor.departmentName }}</el-descriptions-item>
              <el-descriptions-item label="专长">
                <template v-if="selectedDoctor.expertises && selectedDoctor.expertises.length > 0">
                  <el-tag v-for="expertise in selectedDoctor.expertises" :key="expertise.id" class="expertise-tag">
                    {{ expertise.name }}
                  </el-tag>
                </template>
                <template v-else-if="selectedDoctor.expertiseList && typeof selectedDoctor.expertiseList === 'object' && selectedDoctor.expertiseList.length > 0">
                  <el-tag v-for="expertise in selectedDoctor.expertiseList" :key="expertise.id" class="expertise-tag">
                    {{ expertise.name || expertise.expertiseName }}
                  </el-tag>
                </template>
                <template v-else-if="selectedDoctor.expertiseList && typeof selectedDoctor.expertiseList === 'string' && selectedDoctor.expertiseList.trim() !== ''">
                  <el-tag v-for="(expertise, index) in selectedDoctor.expertiseList.split(',')" :key="index" class="expertise-tag">
                    {{ expertise.trim() }}
                  </el-tag>
                </template>
                <template v-else-if="selectedDoctor.expertiseListStr && selectedDoctor.expertiseListStr.trim() !== ''">
                  <el-tag v-for="(expertise, index) in selectedDoctor.expertiseListStr.split(',')" :key="index" class="expertise-tag">
                    {{ expertise.trim() }}
                  </el-tag>
                </template>
                <span v-else>暂无专长</span>
              </el-descriptions-item>
              <el-descriptions-item v-if="selectedDoctor.recommendReason" label="推荐理由">
                {{ selectedDoctor.recommendReason }}
              </el-descriptions-item>
              <el-descriptions-item label="出诊时间" v-if="selectedDoctor.schedules && selectedDoctor.schedules.length">
                <el-tag
                  v-for="schedule in selectedDoctor.schedules"
                  :key="schedule.day"
                  class="schedule-tag"
                >
                  {{ schedule.day }} {{ schedule.time }}
                </el-tag>
              </el-descriptions-item>
            </el-descriptions>
          </el-col>
        </el-row>

        <!-- 医生评价 -->
        <div class="doctor-reviews">
          <h3>患者评价</h3>
          <div v-if="doctorReviews.length > 0">
            <div v-for="review in doctorReviews" :key="review.id" class="review-item">
              <div class="review-header">
                <el-avatar :size="40">{{ review.username.substring(0, 1) }}</el-avatar>
                <div class="review-user-info">
                  <span class="review-username">{{ review.username }}</span>
                  <el-rate v-model="review.rating" disabled />
                </div>
                <span class="review-date">{{ review.createdAt }}</span>
              </div>
              <div class="review-content">
                {{ review.content }}
              </div>
            </div>
          </div>
          <div v-else class="no-reviews">
            暂无评价
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { UserFilled, Search as SearchIcon, Cpu as CpuIcon, InfoFilled } from '@element-plus/icons-vue'
import { getDoctorById } from '@/api/doctors'
import request from '@/utils/axios'
import { getDoctorReviews, addReview } from '@/api/reviews'
import { useUserStore } from '@/store/user'

const router = useRouter()
const nlpForm = ref({ description: '' })
const recommendedDoctors = ref([])
const doctorDetailVisible = ref(false)
const selectedDoctor = ref(null)
const doctorReviews = ref([])
const isLoading = ref(false)
const nlpLoading = ref(false)
const aiDirectLoading = ref(false)
const errorMessage = ref('')
const recommendReason = ref('')
const userStore = useUserStore()

// 基于自然语言获取推荐
const getNLPRecommendation = async () => {
  if (!nlpForm.value.description) {
    ElMessage.warning('请输入症状描述')
    return
  }

  nlpLoading.value = true
  errorMessage.value = ''
  recommendReason.value = ''

  try {
    const response = await request.post('/recommend/doctors', {
      description: nlpForm.value.description,
      userId: userStore.userInfo.userId,
      limit: 10
    })

    if (response && response.data) {
      recommendedDoctors.value = response.data || []
      
      if (recommendedDoctors.value.length === 0) {
        ElMessage.warning('未找到匹配的医生，请尝试更详细地描述症状')
      }
    }
  } catch (error) {
    console.error('获取推荐失败:', error)
    errorMessage.value = '获取推荐失败，请稍后重试'
  } finally {
    nlpLoading.value = false
  }
}

// 基于AI直接推荐
const getAIDirectRecommendation = async () => {
  if (!nlpForm.value.description) {
    ElMessage.warning('请输入症状描述')
    return
  }

  aiDirectLoading.value = true
  errorMessage.value = ''
  recommendReason.value = ''

  try {
    const response = await request.post('/recommend/doctors', {
      description: nlpForm.value.description,
      userId: userStore.userInfo.userId,
      recommendType: 'AI',
      limit: 5
    })

    if (response && response.data) {
      recommendedDoctors.value = response.data || []
      
      if (response.data.reason) {
        recommendReason.value = 'AI智能分析：' + response.data.reason
      }
      
      if (recommendedDoctors.value.length === 0) {
        ElMessage.warning('未找到匹配的医生，请尝试更详细地描述症状')
      }
    }
  } catch (error) {
    console.error('AI精准推荐失败:', error)
    errorMessage.value = 'AI精准推荐失败，请稍后重试'
  } finally {
    aiDirectLoading.value = false
  }
}

// 查看医生详情
const viewDoctorDetails = async (doctor) => {
  try {
    // 直接使用当前医生数据
    selectedDoctor.value = doctor
    
    // 如果有ID，尝试获取更详细的信息
    const doctorId = doctor.id || doctor.doctorId
    if (doctorId) {
      try {
        const response = await getDoctorById(doctorId)
        if (response && response.data && response.data.data) {
          // 合并详细信息，保留原始推荐信息
          const detailedDoctor = response.data.data
          selectedDoctor.value = {
            ...detailedDoctor,
            matchScore: doctor.matchScore,
            recommendReason: doctor.recommendReason,
            // 保留原始的专长信息
            expertiseListStr: doctor.expertiseListStr || detailedDoctor.expertiseListStr
          }
        }
      } catch (error) {
        console.error('获取医生详情失败:', error)
        // 继续使用当前医生数据
      }

      // 获取医生评价
      try {
        console.log('正在获取医生评价，医生ID:', doctorId)
        const reviewResponse = await getDoctorReviews(doctorId)
        console.log('评价响应:', reviewResponse)
        
        if (reviewResponse && reviewResponse.data && Array.isArray(reviewResponse.data.records)) {
          doctorReviews.value = reviewResponse.data.records.map(review => ({
            id: review.id || review.reviewId,
            username: review.username || '匿名用户',
            rating: review.rating || 5,
            content: review.comment || review.content || '该用户未留下评价内容',
            createdAt: review.createdAt ? new Date(review.createdAt).toLocaleDateString() : '未知日期'
          }))
          console.log('处理后的评价数据:', doctorReviews.value)
        } else {
          console.log('未获取到评价数据或数据格式不正确')
          doctorReviews.value = []
        }
      } catch (error) {
        console.error('获取医生评价失败:', error)
        if (error.response) {
          console.error('错误响应:', error.response)
        }
        doctorReviews.value = []
        ElMessage.error('获取医生评价失败，请稍后重试')
      }
    }

    doctorDetailVisible.value = true
  } catch (error) {
    console.error('获取医生详情失败:', error)
    ElMessage.error('获取医生详情失败，请稍后重试')
  }
}

// 获取匹配度百分比
const getMatchScorePercentage = (doctor) => {
  if (doctor.matchScore !== undefined) {
    // 如果是0到1之间的小数，转换为百分比
    return doctor.matchScore <= 1 ? Math.round(doctor.matchScore * 100) : doctor.matchScore
  }
  return 75; // 默认值
}

// 根据匹配分数获取颜色
const getColorForScore = (score) => {
  if (score >= 80) return '#67C23A'  // 高匹配度 - 绿色
  if (score >= 60) return '#E6A23C'  // 中等匹配度 - 黄色
  return '#F56C6C'  // 低匹配度 - 红色
}

// 修改获取医生评价的方法
const fetchDoctorReviews = async (doctorId) => {
  try {
    const response = await getDoctorReviews(doctorId)
    if (response.code === 200) {
      doctorReviews.value = response.data.records
    }
  } catch (error) {
    console.error('获取医生评价失败:', error)
    ElMessage.error('获取医生评价失败')
  }
}

// 修改提交评价的方法
const submitReview = async () => {
  if (!reviewForm.value.rating) {
    ElMessage.warning('请选择评分')
    return
  }

  try {
    const response = await addReview({
      doctorId: currentDoctor.value.id,
      rating: reviewForm.value.rating,
      comment: reviewForm.value.content
    })

    if (response.code === 200) {
      ElMessage.success('评价提交成功')
      reviewDialogVisible.value = false
      // 重新获取评价列表
      fetchDoctorReviews(currentDoctor.value.id)
    }
  } catch (error) {
    console.error('提交评价失败:', error)
    ElMessage.error('提交评价失败')
  }
}
</script>

<style scoped>
.recommend-container {
  padding: 20px;
}

.description-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  flex-direction: column;
}

.card-header h3 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}

.card-header p {
  margin: 8px 0 0;
  color: #606266;
  font-size: 14px;
}

.description-input {
  padding: 20px;
}

.ai-button-container {
  position: relative;
  display: inline-block;
}

.recommendation-info {
  display: none;
  align-items: center;
  gap: 8px;
  color: #909399;
  font-size: 13px;
  background-color: #f8f8f8;
  padding: 8px;
  border-radius: 4px;
  position: absolute;
  width: 350px;
  z-index: 10;
  left: 100%;
  top: 0;
  margin-left: 10px;
}

.recommendation-info span {
  white-space: normal;
}

.ai-button-container:hover .recommendation-info {
  display: flex;
}

.doctor-list {
  margin-top: 20px;
}

.doctor-card {
  margin-bottom: 20px;
  transition: all 0.3s;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.doctor-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.doctor-avatar {
  padding: 20px;
  text-align: center;
  background-color: #f5f7fa;
}

.doctor-info {
  padding: 20px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.doctor-info h3 {
  margin: 0 0 10px;
  font-size: 18px;
  color: #303133;
  text-align: center;
}

.doctor-info .title {
  color: #409EFF;
  margin: 5px 0;
  text-align: center;
}

.doctor-info .department {
  color: #606266;
  margin: 5px 0;
  text-align: center;
}

.match-score {
  margin: 15px auto;
  text-align: center;
}

.progress-value {
  display: block;
  font-size: 28px;
  font-weight: bold;
}

.progress-label {
  display: block;
  font-size: 14px;
  color: #909399;
}

.recommend-reason {
  margin: 10px 0;
  font-size: 13px;
  color: #606266;
  background-color: #f8f8f8;
  padding: 8px;
  border-radius: 4px;
  border-left: 3px solid #409EFF;
}

.actions {
  margin-top: auto;
  display: flex;
  gap: 10px;
  justify-content: center;
}

.doctor-avatar-large {
  display: flex;
  justify-content: center;
  padding: 20px;
}

.doctor-details {
  padding: 20px 0;
}

.symptom-match {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.symptom-match-item {
  display: flex;
  align-items: center;
}

.symptom-name {
  width: 100px;
  margin-right: 10px;
}

.schedule-tag {
  margin: 0 5px 5px 0;
}

.doctor-reviews {
  margin-top: 20px;
}

.doctor-reviews h3 {
  font-size: 16px;
  margin-bottom: 10px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.review-item {
  margin-bottom: 15px;
  padding-bottom: 15px;
  border-bottom: 1px solid #f0f0f0;
}

.review-header {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.review-user-info {
  margin-left: 10px;
  flex: 1;
}

.review-username {
  display: block;
  font-weight: bold;
  margin-bottom: 5px;
}

.review-date {
  color: #909399;
  font-size: 12px;
}

.review-content {
  color: #606266;
  line-height: 1.5;
}

.no-reviews {
  color: #909399;
  text-align: center;
  padding: 20px;
}

.loading-container {
  padding: 20px;
  background: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

@media (max-width: 768px) {
  .recommend-container {
    padding: 10px;
  }
  
  .el-form-item {
    margin-bottom: 15px;
  }
}

.doctor-detail-dialog :deep(.el-dialog__title) {
  width: 100%;
  text-align: center;
  display: block;
}
</style> 