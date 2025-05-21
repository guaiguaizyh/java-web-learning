<template>
  <div class="home-container">
    <!-- 医生推荐系统介绍 -->
    <el-card class="intro-card">
      <div class="intro-content">
        <div class="intro-left">
          <h2>智能医生推荐系统</h2>
          <p>基于AI技术，为您精准匹配最适合的医生</p>
          <div class="intro-features">
            <div class="feature-item">
              <el-icon><Check /></el-icon>
              <span>智能症状分析</span>
            </div>
            <div class="feature-item">
              <el-icon><Check /></el-icon>
              <span>精准医生匹配</span>
            </div>
            <div class="feature-item">
              <el-icon><Check /></el-icon>
              <span>实时在线咨询</span>
            </div>
          </div>
          <el-button type="primary" size="large" class="get-started-btn" @click="router.push('/ai-chat')">
            开始智能推荐
          <el-icon><ArrowRight /></el-icon>
        </el-button>
        </div>
        <div class="intro-right">
          <img src="@/assets/doctor-illustration.svg" alt="医生插图" class="intro-image" />
        </div>
      </div>
    </el-card>

    <!-- 快速访问卡片 -->
    <el-row :gutter="20" class="main-content">
      <el-col :span="8">
        <el-card class="feature-card">
          <div class="feature-content" @click="router.push('/profile')">
            <el-icon class="feature-icon"><User /></el-icon>
            <h3>个人资料</h3>
            <p>查看和编辑您的个人信息</p>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="feature-card">
          <div class="feature-content" @click="router.push('/doctors')">
            <el-icon class="feature-icon"><UserFilled /></el-icon>
            <h3>查看医生</h3>
            <p>浏览全部医生列表</p>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="feature-card">
          <div class="feature-content" @click="router.push('/ai-chat')">
            <el-icon class="feature-icon"><ChatDotRound /></el-icon>
            <h3>AI智能推荐</h3>
            <p>根据症状智能推荐医生</p>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 推荐医生轮播图 -->
    <el-card class="doctors-card">
      <template #header>
        <div class="card-header">
          <h3>推荐医生</h3>
          <el-button type="primary" @click="router.push('/doctors')">查看全部医生</el-button>
        </div>
      </template>
      
      <!-- 有推荐医生时显示轮播图 -->
      <el-carousel v-if="recommendedDoctors.length > 0" :interval="4000" height="400px" indicator-position="outside" arrow="always" type="card">
        <el-carousel-item v-for="doctor in recommendedDoctors" :key="doctor.id">
          <div class="doctor-card">
            <div class="doctor-header">
              <el-avatar :size="120" :src="doctor.avatar">
                <el-icon><User /></el-icon>
              </el-avatar>
            </div>
            <h3>{{ doctor.name }}</h3>
            <p class="title">{{ doctor.title }}</p>
            <p class="department">{{ doctor.department }}</p>
            <div class="doctor-stats">
              <div class="rating-info">
                <el-rate
                  v-model="doctor.averageRating"
                  disabled
                  :max="5"
                />
                <span class="rating-score">{{ formatRating(doctor.averageRating) }}</span>
                <span class="review-count" v-if="doctor.ratingCount">({{ doctor.ratingCount }}条评价)</span>
              </div>
            </div>
            <div class="doctor-expertises">
              <template v-if="doctor.expertiseList && doctor.expertiseList.length">
                <el-tag 
                  v-for="(expertise, i) in doctor.expertiseList" 
                  :key="i" 
                  size="small" 
                  class="expertise-tag"
                  type="info"
                >
                  {{ expertise.name }}
                </el-tag>
              </template>
              <template v-else-if="doctor.expertiseListStr">
                <el-tooltip
                  class="box-item"
                  effect="dark"
                  :content="doctor.expertiseListStr"
                  placement="top-start"
                  :hide-after="0"
                >
                  <div class="expertise-text">
                    {{ formatExpertiseList(doctor.expertiseListStr) }}
                  </div>
                </el-tooltip>
              </template>
              <span v-else class="no-expertise">暂无专长</span>
            </div>
            <el-button type="primary" size="large" class="detail-button" @click="showDoctorDetail(doctor)">
              查看详情
            </el-button>
          </div>
        </el-carousel-item>
      </el-carousel>
      
      <!-- 没有推荐医生时显示提示 -->
      <div v-else class="empty-doctors">
        <el-empty description="暂无推荐医生" :image-size="200">
          <template #description>
            <p>登录后获取个性化医生推荐</p>
          </template>
          <el-button v-if="!userStore.isLoggedIn" type="primary" @click="router.push('/login')">
            立即登录
          </el-button>
          <el-button v-else type="primary" @click="router.push('/doctor-recommend')">
            前往AI推荐页面
          </el-button>
        </el-empty>
      </div>
    </el-card>

    <!-- 健康动态 -->
    <el-card class="health-news-card">
      <template #header>
        <div class="card-header">
          <h3>健康资讯</h3>
        </div>
      </template>
      <el-timeline>
        <el-timeline-item
          v-for="(activity, index) in healthActivities"
          :key="index"
          :timestamp="activity.timestamp"
          :type="activity.type"
        >
          {{ activity.content }}
        </el-timeline-item>
      </el-timeline>
    </el-card>

    <!-- AI 客服对话框 -->
    <el-dialog
      v-model="aiChatVisible"
      title="AI 客服"
      width="60%"
      :close-on-click-modal="false"
    >
      <div class="ai-chat-container">
        <div class="chat-messages" ref="chatMessages">
          <div v-for="(message, index) in chatMessages" :key="index" 
               :class="['message', message.type === 'user' ? 'user' : 'assistant']">
            <div class="message-content">
              {{ message.content }}
            </div>
            <div v-if="message.timestamp" class="message-timestamp">
              {{ new Date(message.timestamp).toLocaleTimeString() }}
            </div>
          </div>
        </div>
        <div class="chat-input">
          <el-input
            v-model="chatInput"
            placeholder="请输入您的问题"
            @keyup.enter="sendMessage"
          >
            <template #append>
              <el-button @click="sendMessage">发送</el-button>
            </template>
          </el-input>
        </div>
      </div>
    </el-dialog>

    <!-- 医生详情对话框 -->
    <el-dialog
      v-model="doctorDetailVisible"
      :title="selectedDoctor ? selectedDoctor.name + ' 医生详情' : '医生详情'"
      width="60%"
    >
      <div v-if="selectedDoctor" class="doctor-details">
        <el-row :gutter="20">
          <el-col :span="8">
            <div class="doctor-avatar-large">
              <el-avatar :size="150" :src="selectedDoctor.avatar">
                <el-icon><UserFilled /></el-icon>
              </el-avatar>
            </div>
            <div class="doctor-quick-stats">
              <div class="stat-item">
                <span class="stat-value">{{ formatRating(selectedDoctor.rating) }}</span>
                <span class="stat-label">评分</span>
              </div>
              </div>
            </el-col>
          <el-col :span="16">
            <el-descriptions :column="1" border>
              <el-descriptions-item label="姓名">{{ selectedDoctor.name }}</el-descriptions-item>
              <el-descriptions-item label="职称">{{ selectedDoctor.title }}</el-descriptions-item>
              <el-descriptions-item label="科室">{{ selectedDoctor.department }}</el-descriptions-item>
              <el-descriptions-item label="专长">
                <div class="expertise-tags">
                  <template v-if="selectedDoctor.expertiseList && selectedDoctor.expertiseList.length">
                    <el-tag 
                      v-for="(expertise, index) in selectedDoctor.expertiseList" 
                      :key="index"
                      size="small"
                      class="expertise-tag"
                    >
                      {{ expertise.name }}
                    </el-tag>
                  </template>
                  <span v-else class="no-expertise">暂无专长</span>
              </div>
              </el-descriptions-item>
            </el-descriptions>
            </el-col>
          </el-row>
        
        <!-- 患者评价 -->
        <div class="doctor-reviews">
          <div class="reviews-header">
            <h3>患者评价</h3>
            <el-button type="primary" size="small" @click="handleReviewClick">我要评价</el-button>
          </div>
          
          <div v-if="showReviewForm" class="review-form">
            <el-form :model="reviewForm" label-width="80px">
              <el-form-item label="评分">
                <el-rate v-model="reviewForm.rating" show-score></el-rate>
              </el-form-item>
              <el-form-item label="评价内容">
                <el-input
                  v-model="reviewForm.content"
                  type="textarea"
                  :rows="4"
                  placeholder="请分享您的就医体验"
                ></el-input>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="submitReview">提交评价</el-button>
                <el-button @click="showReviewForm = false">取消</el-button>
              </el-form-item>
            </el-form>
          </div>
          
          <div v-if="doctorReviews.length > 0" class="reviews-list">
            <div v-for="(review, index) in doctorReviews" :key="index" class="review-item">
              <div class="review-header">
                <el-avatar :size="40">{{ review.username ? review.username.substring(0, 1) : 'U' }}</el-avatar>
                <div class="review-user-info">
                  <span class="review-username">{{ review.username || '匿名用户' }}</span>
                  <el-rate v-model="review.rating" disabled></el-rate>
                </div>
                <span class="review-date">{{ review.createdAt }}</span>
              </div>
              <div class="review-content">
                {{ review.content || '该用户未留下评价内容' }}
              </div>
            </div>
            
            <!-- 评价分页 -->
            <div class="review-pagination" v-if="reviewTotal > reviewPageSize">
              <el-pagination
                v-model:current-page="reviewCurrentPage"
                v-model:page-size="reviewPageSize"
                :page-sizes="[5, 10, 20]"
                :total="reviewTotal"
                layout="total, sizes, prev, pager, next"
                @size-change="handleReviewSizeChange"
                @current-change="handleReviewPageChange"
                size="small"
              />
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
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'
import { 
  User, 
  UserFilled, 
  ArrowRight, 
  ChatDotRound,
  Calendar,
  Star,
  Check 
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getSymptomList } from '@/api/symptom'
import { getDoctors } from '@/api/doctors'
import { getNaturalLanguageRecommendations } from '@/api/ai'
import request from '@/utils/axios'
import { getDoctorReviews } from '@/api/reviews'

const router = useRouter()
const userStore = useUserStore()
const route = useRoute()

// 当前日期
const currentDate = computed(() => {
  const now = new Date()
  return now.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long'
  })
})

// 推荐医生数据
const recommendedDoctors = ref([])

// 获取推荐医生
const fetchRecommendedDoctors = async () => {
  try {
    loading.value = true;
    
    const description = userStore.userInfo.medicalRecord;
    const userId = userStore.userInfo.userId;
    const limit = 5;
    
    const response = await getNaturalLanguageRecommendations(description, userId, limit);
    
    if (response && response.code === 200 && Array.isArray(response.data)) {
      recommendedDoctors.value = response.data.map(doctor => {
        let expertiseList = [];
        if (doctor.expertiseList && Array.isArray(doctor.expertiseList)) {
          expertiseList = doctor.expertiseList;
        } else if (typeof doctor.expertiseList === 'string' && doctor.expertiseList) {
          expertiseList = doctor.expertiseList.split(',').map(name => ({ name: name.trim() }));
        }
        return {
          id: doctor.doctorId || doctor.id,
          name: doctor.name,
          avatar: doctor.avatarUrl || doctor.avatar,
          department: doctor.departmentName || doctor.department,
          title: doctor.positionsName || doctor.title,
          expertiseList: expertiseList,
          expertiseListStr: doctor.expertiseListStr,
          age: doctor.age || '-',
          averageRating: doctor.averageRating || 0,
          ratingCount: doctor.ratingCount || 0,
          gender: doctor.gender
        };
      });
    } else {
      recommendedDoctors.value = [];
    }
  } catch (error) {
    console.error('获取推荐医生失败:', error);
    recommendedDoctors.value = [];
    ElMessage.error('获取推荐医生失败，请稍后重试');
  } finally {
    loading.value = false;
  }
}

// 健康动态数据
const healthActivities = ref([
  {
    content: '最新研究：AI辅助诊断在早期肺癌筛查中的应用取得重大突破',
    timestamp: '2025-03-15',
    type: 'success'
  },
  {
    content: '新型基因疗法在治疗遗传性心脏病方面获得突破性进展',
    timestamp: '2025-03-10',
    type: 'primary'
  },
  {
    content: '智能医疗设备在家庭健康监测中的应用指南',
    timestamp: '2025-03-05',
    type: 'info'
  },
  {
    content: '全球首个量子计算辅助药物研发平台在我院启用',
    timestamp: '2025-02-28',
    type: 'success'
  },
  {
    content: '个性化精准营养：基于基因组学的饮食建议',
    timestamp: '2025-02-25',
    type: 'warning'
  },
  {
    content: '远程医疗新规：互联网诊疗服务标准更新',
    timestamp: '2025-02-20',
    type: 'info'
  },
  {
    content: '新发现：肠道菌群与免疫系统相互作用机制研究突破',
    timestamp: '2025-02-15',
    type: 'primary'
  },
  {
    content: '5G智慧医院建设完成，就医体验全面提升',
    timestamp: '2025-02-10',
    type: 'success'
  }
])

// AI 客服相关
const aiChatVisible = ref(false)
const chatInput = ref('')
const chatMessages = ref([
  { 
    type: 'assistant',
    content: '您好！我是AI医疗助手。请描述您的症状，我会为您推荐合适的科室。',
    timestamp: new Date().toISOString()
  }
])

const showAIChat = () => {
  aiChatVisible.value = true
  // 滚动到底部
  nextTick(() => {
    const chatMessagesEl = document.querySelector('.chat-messages')
    if (chatMessagesEl) {
      chatMessagesEl.scrollTop = chatMessagesEl.scrollHeight
    }
  })
}

const sendMessage = async () => {
  if (!chatInput.value.trim()) return
  
  const userMessage = {
    type: 'user',
    content: chatInput.value.trim(),
    timestamp: new Date().toISOString()
  }
  
  // 添加用户消息
  chatMessages.value.push(userMessage)
  
  // 清空输入并保存用户输入
  const userInput = chatInput.value
  chatInput.value = ''
  
  try {
    // 调用AI对话API
    const response = await request.post('/ai/chat', {
      message: userInput,
      history: [] // 可选的聊天历史
    })
    
    console.log('AI响应:', response)
    
    if (response?.code === 200 && response?.data) {
      // 从响应中提取消息内容
      const aiMessage = response.data.message
      
      // 添加AI回复消息
  chatMessages.value.push({
        type: 'assistant',
        content: aiMessage.content,
        timestamp: new Date().toISOString()
      })

      // 如果有推荐的医生，显示医生信息
      if (response.data.doctors && response.data.doctors.length > 0) {
        const doctorInfo = response.data.doctors.map(doc => 
          `推荐医生: ${doc.name} - ${doc.departmentName}`
        ).join('\n')
        
        chatMessages.value.push({
          type: 'assistant',
          content: doctorInfo,
          timestamp: new Date().toISOString()
        })
      }

      // 如果有科室推荐，显示科室信息
      if (response.data.department) {
    chatMessages.value.push({
          type: 'assistant',
          content: `建议就诊科室: ${response.data.department}`,
          timestamp: new Date().toISOString()
        })
      }
    } else {
      throw new Error('AI响应格式异常')
    }
  } catch (error) {
    console.error('AI对话失败:', error)
    chatMessages.value.push({
      type: 'assistant',
      content: '抱歉，系统出现了一些问题，请稍后再试。',
      timestamp: new Date().toISOString()
    })
  }
  
  // 滚动到底部
  nextTick(() => {
    const chatMessagesEl = document.querySelector('.chat-messages')
    if (chatMessagesEl) {
      chatMessagesEl.scrollTop = chatMessagesEl.scrollHeight
    }
  })
}

// 医生详情相关
const doctorDetailVisible = ref(false)
const selectedDoctor = ref(null)
const doctorReviews = ref([])
const reviewTotal = ref(0)
const reviewCurrentPage = ref(1)
const reviewPageSize = ref(5)
const showReviewForm = ref(false)
const reviewForm = ref({
  rating: 5,
  content: ''
})

const showDoctorDetail = async (doctor) => {
  selectedDoctor.value = doctor
  doctorDetailVisible.value = true
  
  // 重置评价相关状态
  reviewCurrentPage.value = 1
  showReviewForm.value = false
  
  // 获取医生评价
  await fetchDoctorReviews(doctor.id)
}

const formatRating = (rating) => {
  if (!rating || isNaN(rating)) return '0.00'
  return Number(rating).toFixed(2)
}

// 格式化专长文本，限制长度并添加省略号
const formatExpertiseList = (expertiseStr) => {
  if (!expertiseStr) return '暂无专长'
  const maxLength = 50
  return expertiseStr.length > maxLength 
    ? expertiseStr.substring(0, maxLength) + '...' 
    : expertiseStr
}

// 处理评价点击事件
const handleReviewClick = () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后再评价')
    router.push('/login')
    return
  }
  showReviewForm.value = true
}

// 提交评价
const submitReview = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后再评价')
    router.push('/login')
    return
  }

  // 检查 token 是否存在
  const token = localStorage.getItem('token')
  if (!token) {
    ElMessage.error('登录已过期，请重新登录')
    userStore.logout()
    router.push('/login')
    return
  }

  if (!reviewForm.value.content.trim()) {
    ElMessage.warning('请填写评价内容')
    return
  }
  
  try {
    // 准备评价数据
    const reviewData = {
      doctorId: selectedDoctor.value.id,
      rating: reviewForm.value.rating,
      comment: reviewForm.value.content,
      isAnonymous: false
    }
    
    console.log('提交评价数据:', reviewData)
    console.log('当前token:', token)
    
    // 使用request实例发送请求
    const response = await request.post('/reviews', reviewData, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    console.log('评价提交响应:', response)
    
    if (response && response.code === 200) {
      ElMessage.success('评价提交成功')
      
      // 重置表单
      reviewForm.value = {
        rating: 5,
        content: ''
      }
      showReviewForm.value = false
      
      // 刷新评价列表
      reviewCurrentPage.value = 1
      await fetchDoctorReviews(selectedDoctor.value.id)
    } else {
      throw new Error(response?.msg || '评价提交失败')
    }
  } catch (error) {
    console.error('提交评价失败:', error)
    if (error.response?.status === 401) {
      ElMessage.error('登录已过期，请重新登录')
      userStore.logout()
      router.push('/login')
    } else {
      ElMessage.error(error.message || '提交评价失败，请稍后重试')
    }
  }
}

// 获取医生评价
const fetchDoctorReviews = async (doctorId) => {
  try {
    const response = await getDoctorReviews(doctorId, reviewCurrentPage.value, reviewPageSize.value)
    if (response.code === 200) {
      doctorReviews.value = response.data.records
      reviewTotal.value = response.data.total
    }
  } catch (error) {
    console.error('获取医生评价失败:', error)
    ElMessage.error('获取医生评价失败')
  }
}

// 处理评价分页变化
const handleReviewPageChange = (page) => {
  reviewCurrentPage.value = page
  fetchDoctorReviews(selectedDoctor.value.id)
}

// 处理评价每页数量变化
const handleReviewSizeChange = (size) => {
  reviewPageSize.value = size
  reviewCurrentPage.value = 1
  fetchDoctorReviews(selectedDoctor.value.id)
}

const fetchDoctorDetails = async (doctorId) => {
  try {
    const response = await request.get(`/doctors/${doctorId}`)
    if (response.code === 200) {
      // 处理专长数据
      let expertiseList = []
      if (response.data.expertiseList && Array.isArray(response.data.expertiseList)) {
        expertiseList = response.data.expertiseList.map(expertise => ({
          name: expertise.expertiseName || expertise.name
        }))
      } else if (typeof response.data.expertiseList === 'string' && response.data.expertiseList) {
        expertiseList = response.data.expertiseList.split(',').map(name => ({
          name: name.trim()
        }))
      }
      
      selectedDoctor.value = {
        ...response.data,
        expertiseList: expertiseList
      }
    }
  } catch (error) {
    console.error('获取医生详情失败:', error)
    ElMessage.error('获取医生详情失败')
  }
}

// 添加 loading 状态
const loading = ref(false)

onMounted(() => {
  fetchRecommendedDoctors()
  // 从路由参数获取医生ID
  const doctorId = route.params.id
  if (doctorId) {
    fetchDoctorDetails(doctorId)
  }
})
</script>

<style scoped>
.home-container {
  padding: 20px;
  width: 100%;
  min-height: 100vh;
  background-color: #f0f2f5;
  box-sizing: border-box;
}

/* 医生推荐系统介绍卡片样式 */
.intro-card {
  margin-bottom: 20px;
  background: linear-gradient(135deg, #ffffff 0%, #f6f9fc 100%);
}

.intro-content {
  display: flex;
  align-items: center;
  padding: 40px;
  gap: 40px;
}

.intro-left {
  flex: 1;
}

.intro-left h2 {
  font-size: 32px;
  color: #303133;
  margin-bottom: 15px;
  font-weight: 600;
}

.intro-left p {
  font-size: 18px;
  color: #606266;
  margin-bottom: 30px;
  line-height: 1.6;
}

.intro-features {
  margin-bottom: 30px;
}

.feature-item {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
  font-size: 16px;
  color: #606266;
}

.feature-item .el-icon {
  color: #67C23A;
  margin-right: 10px;
  font-size: 20px;
}

.get-started-btn {
  padding: 15px 30px;
  font-size: 18px;
}

.intro-right {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
}

.intro-image {
  max-width: 100%;
  height: auto;
  border-radius: 8px;
}

.main-content {
  margin-bottom: 20px;
}

.feature-card {
  height: 100%;
  cursor: pointer;
  transition: all 0.3s;
}

.feature-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.feature-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
}

.feature-icon {
  font-size: 40px;
  color: #409EFF;
  margin-bottom: 10px;
}

.feature-content h3 {
  margin: 10px 0;
  font-size: 18px;
  color: #303133;
}

.feature-content p {
  margin: 0;
  color: #606266;
  text-align: center;
}

.symptoms-card, .doctors-card, .health-news-card {
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

.common-symptoms {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.symptom-category h4 {
  margin: 0 0 10px 0;
  font-size: 16px;
  color: #303133;
}

.symptom-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.symptom-tag {
  cursor: pointer;
}

.doctor-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  height: 100%;
  padding: 30px;
  box-sizing: border-box;
  background: linear-gradient(to bottom, #ffffff, #f9fbff);
  border-radius: 12px;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.05);
  transition: transform 0.3s, box-shadow 0.3s;
}

.doctor-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 25px rgba(0, 0, 0, 0.1);
}

.doctor-header {
  position: relative;
  margin-bottom: 15px;
  width: 100%;
  display: flex;
  justify-content: center;
}

.match-score-badge {
  position: absolute;
  top: 0;
  right: 20%;
  background: linear-gradient(135deg, #2d8cf0, #0072ff);
  color: white;
  border-radius: 50%;
  width: 60px;
  height: 60px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 20px;
  box-shadow: 0 4px 10px rgba(0, 114, 255, 0.3);
  animation: pulse 2s infinite;
}

.match-label {
  font-size: 12px;
  font-weight: normal;
  margin-top: 2px;
}

@keyframes pulse {
  0% {
    box-shadow: 0 0 0 0 rgba(0, 114, 255, 0.5);
  }
  70% {
    box-shadow: 0 0 0 10px rgba(0, 114, 255, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(0, 114, 255, 0);
  }
}

.doctor-card h3 {
  margin: 15px 0 5px;
  font-size: 24px;
  color: #303133;
  font-weight: 600;
}

.doctor-card .department,
.doctor-card .title {
  margin: 5px 0;
  color: #606266;
}

.doctor-card .title {
  color: #409EFF;
  font-weight: bold;
}

.doctor-expertises {
  margin: 15px 0;
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 8px;
}

.expertise-tag {
  margin: 0;
  padding: 2px 10px;
  border-radius: 12px;
}

.expertise-text {
  color: #606266;
  background-color: #f4f4f5;
  display: inline-block;
  padding: 2px 10px;
  border-radius: 12px;
  margin: 2px;
  line-height: 1.4;
  white-space: normal;
  word-break: break-all;
}

.doctor-card .description {
  margin: 10px 0;
  text-align: center;
  color: #606266;
}

.doctor-stats {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  width: 100%;
  margin: 15px 0;
  padding: 10px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.rating-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 5px;
}

.rating-info :deep(.el-rate) {
  display: flex;
  align-items: center;
  height: 24px;
}

.rating-info :deep(.el-rate__text) {
  color: #ff9900;
  font-size: 16px;
  font-weight: bold;
}

.review-count {
  font-size: 14px;
  color: #909399;
}

.recommend-reason {
  max-width: 100%;
  text-align: center;
  margin: 15px 0;
  padding: 15px;
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  max-height: 120px;
  overflow-y: auto;
  background-color: #f8f8f8;
  border-radius: 8px;
  border-left: 4px solid #409EFF;
  position: relative;
}

.recommend-reason h4 {
  margin: 0 0 8px 0;
  color: #409EFF;
  font-size: 16px;
}

.recommend-reason p {
  margin: 0;
  text-align: left;
}

.detail-button {
  margin-top: 20px;
  transition: all 0.3s;
  width: 80%;
}

.empty-doctors {
  padding: 60px 20px;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 300px;
}

.empty-doctors .el-empty__description p {
  color: #909399;
  margin-bottom: 20px;
  font-size: 16px;
}

.doctor-detail {
  padding: 20px 0;
}

.doctor-detail h3 {
  margin: 0 0 10px;
  font-size: 24px;
  color: #303133;
}

.doctor-detail .department,
.doctor-detail .title {
  margin: 5px 0;
  font-size: 16px;
}

.doctor-detail .description {
  margin: 15px 0;
  color: #606266;
}

.doctor-reviews {
  margin-top: 30px;
  width: 100%;
}

.reviews-list {
  max-height: 400px;
  width: 100%;
  overflow-y: auto;
  padding: 20px;
  border: 1px solid #EBEEF5;
  border-radius: 4px;
  margin-top: 20px;
}

.review-item {
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #EBEEF5;
  width: 100%;
}

.review-item:last-child {
  margin-bottom: 0;
  border-bottom: none;
}

.review-header {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  gap: 10px;
}

.review-user-info {
  flex: 1;
  margin-left: 10px;
}

.review-username {
  display: block;
  font-weight: bold;
  margin-bottom: 5px;
  color: #303133;
}

.review-date {
  color: #909399;
  font-size: 12px;
}

.review-content {
  color: #606266;
  line-height: 1.6;
  padding: 10px 0;
}

.reviews-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #EBEEF5;
}

.reviews-header h3 {
  margin: 0;
  font-size: 16px;
  color: #303133;
}

.review-form {
  background-color: #f5f7fa;
  padding: 20px;
  border-radius: 4px;
  margin-bottom: 20px;
}

.review-pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.no-reviews {
  text-align: center;
  padding: 40px 0;
  color: #909399;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.doctor-actions {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  gap: 20px;
}

.ai-chat-container {
  display: flex;
  flex-direction: column;
  height: 600px;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  background-color: #ffffff;
  border-radius: 8px;
  margin-bottom: 16px;
}

.message {
  max-width: 80%;
  padding: 12px 16px;
  border-radius: 12px;
  margin-bottom: 12px;
  white-space: pre-wrap;
  word-wrap: break-word;
  overflow-wrap: break-word;
  position: relative;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.message.user {
  align-self: flex-end;
  background-color: #409EFF;
  color: white;
  margin-left: 20%;
}

.message.assistant {
  align-self: flex-start;
  background-color: #F2F6FC;
  color: #303133;
  margin-right: 20%;
}

.message-content {
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 4px;
}

.message-timestamp {
  font-size: 12px;
  color: #909399;
  text-align: right;
}

.chat-input {
  padding: 16px 20px;
  background-color: #ffffff;
  border-top: 1px solid #EBEEF5;
  border-radius: 0 0 8px 8px;
}

.chat-input :deep(.el-input-group__append) {
  background-color: #409EFF;
  border-color: #409EFF;
  color: white;
}

.chat-input :deep(.el-input__inner) {
  border-right: none;
}
</style>
