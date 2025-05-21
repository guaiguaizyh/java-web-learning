<template>
  <div class="doctor-management">
    <div class="page-header">

    </div>

    <!-- 搜索表单 -->
    <div class="search-form">
      <el-form :inline="true" :model="searchForm" @submit.prevent="handleSearch">
        <el-form-item label="医生姓名" style="margin-right: 20px;">
          <el-input
            v-model="searchForm.name"
            placeholder="请输入医生姓名"
            clearable
            style="width: 200px;height: 30px;"
            @clear="handleSearch"
          />
        </el-form-item>
        <el-form-item label="性别" style="margin-right: 20px;">
          <el-select
            v-model="searchForm.gender"
            placeholder="请选择性别"
            clearable
            style="width: 150px;height: 28px;"
            @clear="handleSearch"
            @change="handleSearch"
          >
            <el-option label="男" value="男" />
            <el-option label="女" value="女" />
          </el-select>
        </el-form-item>
        <el-form-item label="职称" style="margin-right: 20px;">
          <el-select
            v-model="searchForm.positionsId"
            placeholder="请选择职称"
            clearable
            style="width: 150px;height: 28px;"
            @clear="handleSearch"
            @change="handleSearch"
            :loading="positionsLoading"
          >
            <el-option 
              v-for="p in sortedPositionOptions" 
              :key="p.id" 
              :label="p.name" 
              :value="p.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="所属科室" style="margin-right: 20px;">
          <el-select
            v-model="searchForm.departmentId"
            placeholder="请选择科室"
            clearable
            filterable
            style="width: 150px;height: 28px;"
            @clear="handleSearch"
            @change="handleSearch"
          >
            <el-option
              v-for="dept in departments"
              :key="dept.id"
              :label="dept.name"
              :value="dept.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="排序方式" style="margin-right: 20px;">
          <el-select
            v-model="searchForm.sortField"
            placeholder="排序方式"
            style="width: 150px;height: 28px;"
            @change="handleSearch"
          >
            <el-option label="默认排序" value="doctorId" />
            <el-option label="按评分排序" value="averageRating" />
            <el-option label="按评价数排序" value="ratingCount" />
          </el-select>
        </el-form-item>
        <el-form-item style="margin-right: 0px;">
          <el-button 
            :type="searchForm.sortOrder === 'ASC' ? 'primary' : 'default'"
            @click="toggleSortOrder"
          >
            <el-icon><Sort /></el-icon>
            {{ searchForm.sortOrder === 'ASC' ? '升序' : '降序' }}
          </el-button>
          <el-button type="primary" @click="handleSearch" style="margin-left: 10px;">
            <el-icon><Search /></el-icon>搜索
          </el-button>
          <el-button @click="resetSearch" style="margin-left: 10px;">
            <el-icon><Refresh /></el-icon>重置
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 数据表格 -->
    <el-table
      v-loading="loading"
      :data="doctorList"
      border
      style="width: 100%"
    >
      <el-table-column label="医生信息" width="180">
        <template #default="{ row }">
          <div class="doctor-info">
            <el-avatar :size="40" :src="row.avatarUrl">
              <el-icon><User /></el-icon>
            </el-avatar>
            <div class="info-content">
              <div class="doctor-name">{{ row.name }}</div>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="positionsName" label="职称" width="120">
        <template #default="{ row }">
          <span>{{ row.positionsName || '暂无职称' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="性别" width="80">
        <template #default="{ row }">
          <span>{{ row.gender || '未设置' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="departmentName" label="所属科室" width="120" />
      <el-table-column label="年龄" width="150">
        <template #default="{ row }">
          <div>{{ row.age || '-' }}</div>
        </template>
      </el-table-column>
      <el-table-column prop="averageRating" label="评分" width="220" align="center">
        <template #default="{ row }">
          <div class="rating-info">
            <el-rate
              v-model="row.averageRating"
              disabled
              :max="5"
            />
            <span class="rating-score">{{ formatRating(row.averageRating || 0) }}</span>
            <span class="review-count" v-if="row.ratingCount">({{ row.ratingCount }}条评价)</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="专长" min-width="300">
        <template #default="{ row }">
          <div class="expertise-tags">
            <template v-if="row.expertiseList && row.expertiseList.length">
              <el-tag 
                v-for="(expertise, index) in row.expertiseList" 
                :key="index"
                size="small"
                class="expertise-tag"
                type="info"
              >
                {{ expertise.name }}
              </el-tag>
            </template>
            <template v-else-if="row.expertiseListStr">
              <el-tooltip
                class="box-item"
                effect="dark"
                :content="row.expertiseListStr"
                placement="top-start"
                :hide-after="0"
              >
                <div class="expertise-text">
                  {{ formatExpertiseList(row.expertiseListStr) }}
                </div>
              </el-tooltip>
            </template>
            <span v-else class="no-expertise">暂无专长</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <div class="operation-buttons">
            <el-button
              type="primary"
              size="small"
              @click="viewDetails(row)"
            >
              查看详情
            </el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 医生详情对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="selectedDoctor ? selectedDoctor.name + ' 医生详情' : '医生详情'"
      width="60%"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <div v-if="selectedDoctor" class="doctor-details">
        <el-row :gutter="20">
          <el-col :span="8">
            <div class="doctor-avatar-large">
              <el-avatar :size="150" :src="selectedDoctor.avatarUrl">
                <el-icon><UserFilled /></el-icon>
              </el-avatar>
            </div>
            <div class="doctor-quick-stats">
              <div class="stat-item">
                <span class="stat-value">{{ formatRating(selectedDoctor.averageRating) }}</span>
                <span class="stat-label">评分 ({{ selectedDoctor.ratingCount || 0 }}条评价)</span>
              </div>
            </div>
          </el-col>
          <el-col :span="16">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="姓名" :span="2">{{ selectedDoctor.name }}</el-descriptions-item>
              <el-descriptions-item label="职称" :span="2">{{ selectedDoctor.positionsName }}</el-descriptions-item>
              <el-descriptions-item label="科室" :span="2">{{ selectedDoctor.departmentName }}</el-descriptions-item>
              <el-descriptions-item label="年龄" :span="2">{{ selectedDoctor.age || '-' }}</el-descriptions-item>
              <el-descriptions-item label="专长" :span="2">
                <div class="expertise-tags">
                  <template v-if="selectedDoctor.expertiseList && selectedDoctor.expertiseList.length">
                    <el-tag 
                      v-for="(expertise, index) in selectedDoctor.expertiseList" 
                      :key="index"
                      size="small"
                      class="expertise-tag"
                      type="info"
                    >
                      {{ expertise.name }}
                    </el-tag>
                  </template>
                  <template v-else-if="selectedDoctor.expertiseListStr">
                    <el-tooltip
                      class="box-item"
                      effect="dark"
                      :content="selectedDoctor.expertiseListStr"
                      placement="top-start"
                      :hide-after="0"
                    >
                      <div class="expertise-text">
                        {{ formatExpertiseList(selectedDoctor.expertiseListStr) }}
                      </div>
                    </el-tooltip>
                  </template>
                  <span v-else class="no-expertise">暂无专长</span>
                </div>
              </el-descriptions-item>
            </el-descriptions>
          </el-col>
        </el-row>

        <!-- 医生评价 -->
        <div class="doctor-reviews">
          <div class="reviews-header">
            <h3>患者评价 ({{ reviewTotal || 0 }}条)</h3>
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
              <el-form-item label="匿名评价">
                <el-tooltip
                  content="开启后您的用户名将显示为匿名用户"
                  placement="top"
                  :show-after="300"
                >
                  <el-switch v-model="reviewForm.isAnonymous" />
                </el-tooltip>
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
            
            <!-- 添加分页组件 -->
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
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  User, 
  UserFilled, 
  Search, 
  Refresh,
  Sort
} from '@element-plus/icons-vue'
import request from '@/utils/axios'  // 导入配置好的request实例
import { getDoctors, getDoctorById } from '@/api/doctors'
import { getDepartments } from '@/api/departments'
import { getPositions } from '@/api/positions'
import { getDoctorReviews } from '@/api/reviews'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

// 搜索表单
const searchForm = ref({
  name: '',
  gender: '',
  positionsId: '',
  departmentId: '',
  sortField: 'doctorId',  // 设置默认排序字段为doctorId
  sortOrder: 'ASC'
})

// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 数据获取相关
const departments = ref([])
const positions = ref([])
const doctorList = ref([])
const loading = ref(false)
const positionsLoading = ref(false)

// 医生详情相关
const dialogVisible = ref(false)
const selectedDoctor = ref(null)

// 评价相关
const doctorReviews = ref([])
const showReviewForm = ref(false)
const reviewForm = ref({
  rating: 5,
  content: '',
  isAnonymous: false
})

// 评价分页
const reviewCurrentPage = ref(1)
const reviewPageSize = ref(5)
const reviewTotal = ref(0)

// 计算属性 - 排序后的职称选项
const sortedPositionOptions = computed(() => {
  return [...positions.value].sort((a, b) => a.name.localeCompare(b.name, 'zh-CN'))
})

// 获取科室列表
const fetchDepartments = async () => {
  try {
    const response = await getDepartments({ page: 1, size: 100 })
    if (response && response.code === 200) {
      departments.value = response.data.records.map(dept => ({
        id: dept.departmentId,
        name: dept.departmentName
      }))
    }
  } catch (error) {
    console.error('获取科室列表失败:', error)
    ElMessage.error('获取科室列表失败')
  }
}

// 获取职称列表
const fetchPositions = async () => {
  positionsLoading.value = true
  try {
    const response = await getPositions()
    if (response && response.code === 200) {
      positions.value = response.data.map(pos => ({
        id: pos.positionsId,
        name: pos.positionsName,
        doctorCount: pos.doctorCount || 0
      }))
    }
  } catch (error) {
    console.error('获取职称列表失败:', error)
    ElMessage.error('获取职称列表失败')
  } finally {
    positionsLoading.value = false
  }
}

// 获取医生列表
const fetchDoctors = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      name: searchForm.value.name,
      gender: searchForm.value.gender,
      positionsId: searchForm.value.positionsId,
      departmentId: searchForm.value.departmentId,
      sortField: searchForm.value.sortField,
      sortOrder: searchForm.value.sortOrder,
      includeExpertise: true
    }
    
    console.log('发送查询参数:', params);  // 添加调试日志
    
    const response = await getDoctors(params)
    console.log('获取医生列表响应:', response);  // 添加调试日志
    
    if (response && response.code === 200) {
      doctorList.value = response.data.records.map(doctor => {
        // 处理专长数据
        let expertiseList = []
        if (doctor.expertiseList && Array.isArray(doctor.expertiseList)) {
          expertiseList = doctor.expertiseList
        } else if (typeof doctor.expertiseList === 'string' && doctor.expertiseList) {
          expertiseList = doctor.expertiseList.split(',').map(name => ({ name: name.trim() }))
        }
        return {
          ...doctor,
          expertiseList: expertiseList,
          age: doctor.age || '-'
        }
      })
      total.value = response.data.total || 0
    } else {
      doctorList.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('获取医生列表失败:', error)
    ElMessage.error('获取医生列表失败')
  } finally {
    loading.value = false
  }
}

// 查看医生详情
const viewDetails = async (doctor) => {
  try {
    console.log('Opening doctor details for:', doctor)
    // 先用传入的数据初始化，保留原始的专长数据
    selectedDoctor.value = {
      ...doctor
    }
    
    // 获取更详细的医生信息
    const doctorId = doctor.doctorId
    const response = await getDoctorById(doctorId)
    
    if (response && response.code === 200 && response.data) {
      // 合并详细信息，保留原始数据中的一些信息
      const detailedDoctor = response.data
      console.log('Detailed doctor data:', detailedDoctor)
      
      // 处理专长数据
      let expertiseList = []
      
      // 1. 首先尝试使用详细信息中的expertiseList（如果是数组）
      if (Array.isArray(detailedDoctor.expertiseList)) {
        expertiseList = detailedDoctor.expertiseList.map(expertise => ({
          name: expertise.expertiseName || expertise.name || expertise
        }))
      }
      // 2. 如果详细信息中的expertiseList是字符串
      else if (typeof detailedDoctor.expertiseList === 'string' && detailedDoctor.expertiseList.trim()) {
        expertiseList = detailedDoctor.expertiseList.split(/[,，、]/).map(name => ({
          name: name.trim()
        }))
      }
      // 3. 尝试使用expertiseListStr
      else if (detailedDoctor.expertiseListStr && detailedDoctor.expertiseListStr.trim()) {
        expertiseList = detailedDoctor.expertiseListStr.split(/[,，、]/).map(name => ({
          name: name.trim()
        }))
      }
      // 4. 如果上述都没有，尝试使用原始doctor对象中的专长数据
      else if (doctor.expertiseList && Array.isArray(doctor.expertiseList)) {
        expertiseList = doctor.expertiseList
      }
      else if (doctor.expertiseListStr && doctor.expertiseListStr.trim()) {
        expertiseList = doctor.expertiseListStr.split(/[,，、]/).map(name => ({
          name: name.trim()
        }))
      }
      
      console.log('Processed expertise list:', expertiseList)
      
      selectedDoctor.value = {
        ...detailedDoctor,
        doctorId,
        expertiseList: expertiseList,
        expertiseListStr: detailedDoctor.expertiseListStr || doctor.expertiseListStr,
        averageRating: doctor.averageRating,
        ratingCount: doctor.ratingCount
      }
    }
    
    // 重置评价页码
    reviewCurrentPage.value = 1
    
    // 获取医生评价
    await fetchDoctorReviews(doctorId)
    
    // 打开对话框
    dialogVisible.value = true
  } catch (error) {
    console.error('获取医生详情失败:', error)
    ElMessage.error('获取医生详情失败')
  }
}

// 获取医生评价
const fetchDoctorReviews = async (doctorId, page = 1, size = 5) => {
  const userStore = useUserStore()
  
  try {
    // 修复：直接传递page和size作为独立参数，而不是作为对象
    const response = await getDoctorReviews(doctorId, page, size)
    
    if (response && response.code === 200) {
      const responseData = response.data
      
      // 设置评价总数
      reviewTotal.value = responseData.total || 0
      
      // 处理评价记录
      if (responseData.records && Array.isArray(responseData.records)) {
        doctorReviews.value = responseData.records.map(review => ({
          id: review.reviewId,
          username: review.username || '匿名用户',
          rating: review.rating,
          content: review.comment || '该用户未留下评价内容',
          createdAt: review.createdAt ? review.createdAt.substring(0, 10) : '未知日期'
        }))
      } else {
        doctorReviews.value = []
      }
    } else {
      doctorReviews.value = []
      reviewTotal.value = 0
    }
  } catch (error) {
    console.error('获取医生评价失败:', error)
    if (error.response?.status === 401) {
      ElMessage.error('登录已过期，请重新登录')
      userStore.logout()
      router.push('/login')
    } else {
      ElMessage.error('获取医生评价失败')
    }
    doctorReviews.value = []
    reviewTotal.value = 0
  }
}

// 处理评价分页变化
const handleReviewPageChange = (page) => {
  reviewCurrentPage.value = page
  fetchDoctorReviews(selectedDoctor.value.doctorId, page, reviewPageSize.value)
}

// 处理评价每页数量变化
const handleReviewSizeChange = (size) => {
  reviewPageSize.value = size
  reviewCurrentPage.value = 1 // 重置为第一页
  fetchDoctorReviews(selectedDoctor.value.doctorId, 1, size)
}

// 提交评价
const submitReview = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后再评价');
    return;
  }

  if (!reviewForm.value.content.trim()) {
    ElMessage.warning('请输入评价内容');
    return;
  }

  try {
    // 从 localStorage 获取用户信息
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
    const userId = userInfo.userId || userInfo.id;

    if (!userId) {
      ElMessage.error('获取用户信息失败');
      return;
    }

    const reviewData = {
      doctorId: selectedDoctor.value.doctorId,
      userId: userId,  // 添加用户ID
      rating: reviewForm.value.rating,
      comment: reviewForm.value.content.trim(),
      isAnonymous: reviewForm.value.isAnonymous
    };

    // 获取 token
    const token = localStorage.getItem('token');
    console.log('当前token:', token);
    console.log('提交的评论数据:', reviewData);
    
    if (!token) {
      ElMessage.error('登录已过期，请重新登录');
      userStore.logout();
      router.push('/login');
      return;
    }

    const response = await request({
      url: '/reviews',
      method: 'post',
      data: reviewData,
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    });
    
    if (response.code === 200) {
      ElMessage.success('评价提交成功');
      showReviewForm.value = false;
      // 重置表单
      reviewForm.value = {
        rating: 5,
        content: '',
        isAnonymous: false
      };
      
      // 获取更新后的医生信息
      const doctorResponse = await getDoctorById(selectedDoctor.value.doctorId);
      if (doctorResponse && doctorResponse.code === 200) {
        const updatedDoctor = doctorResponse.data;
        // 更新医生评分信息
        selectedDoctor.value = {
          ...selectedDoctor.value,
          averageRating: updatedDoctor.averageRating,
          ratingCount: updatedDoctor.ratingCount
        };
      }
      
      // 刷新评价列表
      await fetchDoctorReviews(selectedDoctor.value.doctorId);
    } else {
      ElMessage.error(response.message || '评价提交失败');
    }
  } catch (error) {
    console.error('提交评价失败:', error);
    if (error.response?.status === 403) {
      ElMessage.error('您已经评价过这位医生了');
    } else {
      ElMessage.error(error.response?.data?.message || '评价提交失败，请稍后重试');
    }
  }
};

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchDoctors()
}

// 重置搜索
const resetSearch = () => {
  searchForm.value = {
    name: '',
    gender: '',
    positionsId: '',
    departmentId: '',
    sortField: 'doctorId',  // 重置时设置为默认排序字段
    sortOrder: 'ASC'
  }
  currentPage.value = 1
  fetchDoctors()
}

// 分页相关
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchDoctors()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchDoctors()
}

// 格式化专长文本
const formatExpertiseText = (text) => {
  if (!text || text === '暂无专长') return '暂无专长';
  const maxLength = 50;
  return text.length > maxLength ? `${text.substring(0, maxLength)}...` : text;
}

// 格式化评分，保留两位小数
const formatRating = (rating) => {
  if (!rating || isNaN(rating)) return '0.00'
  return Number(rating).toFixed(2)
}

// 格式化专长列表
const formatExpertiseList = (expertiseStr) => {
  if (!expertiseStr) return '暂无专长'
  const maxLength = 50
  return expertiseStr.length > maxLength 
    ? expertiseStr.substring(0, maxLength) + '...' 
    : expertiseStr
}

// 在 script setup 部分添加 handleReviewClick 函数
const handleReviewClick = () => {
  const userStore = useUserStore()
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后再评价')
    router.push('/login')
    return
  }
  showReviewForm.value = true
}

// 修改排序相关的方法
const handleSortChange = (field) => {
  if (searchForm.value.sortField === field) {
    // 如果点击的是同一个字段，切换排序顺序
    searchForm.value.sortOrder = searchForm.value.sortOrder === 'ASC' ? 'DESC' : 'ASC'
  } else {
    // 如果是新的排序字段，设置为升序
    searchForm.value.sortField = field
    searchForm.value.sortOrder = 'ASC'
  }
  handleSearch()
}

// 添加切换排序顺序的方法
const toggleSortOrder = () => {
  searchForm.value.sortOrder = searchForm.value.sortOrder === 'ASC' ? 'DESC' : 'ASC'
  handleSearch()
}

onMounted(() => {
  fetchDepartments()
  fetchPositions()
  fetchDoctors()
})
</script>

<style scoped>
.doctor-management {
  padding: 12px;
  min-width: 1000px;
  max-width: 1800px;
  margin: 0 auto;
  background-color: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);

}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-left {
  display: flex;
  align-items: center;
}

.header-left h2 {
  margin: 0;
  margin-right: 15px;
}

.search-form {
  margin-bottom: 20px;
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.search-buttons {
  margin-bottom: 0;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.doctor-info {
  display: flex;
  align-items: center;
}

.info-content {
  margin-left: 10px;
}

.doctor-name {
  font-weight: bold;
  font-size: 14px;
}

.operation-buttons {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.button-column {
  display: flex;
  gap: 10px;
}

.rating-info {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
  gap: 8px;
}

.rating-score {
  font-size: 14px;
  font-weight: bold;
  color: #ff9900;
}

.review-count {
  font-size: 12px;
  color: #909399;
}

.expertise-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.expertise-tag-group {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.expertise-tag {
  margin: 2px;
}

.no-expertise {
  color: #909399;
  font-style: italic;
}

/* 详情对话框样式 */
.doctor-avatar-large {
  display: flex;
  justify-content: center;
  padding: 20px;
}

.doctor-quick-stats {
  display: flex;
  justify-content: space-around;
  margin-top: 20px;
}

.doctor-details {
  padding: 20px 0;
}

.doctor-reviews {
  margin-top: 20px;
}

.reviews-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #EBEEF5;
}

.reviews-header h3 {
  margin: 0;
  font-size: 16px;
  color: #303133;
}

.review-form {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.review-pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.reviews-list {
  max-height: none;
  overflow-y: visible;
  border: 1px solid #EBEEF5;
  border-radius: 4px;
  margin-top: 10px;
  padding: 10px;
}

.review-item {
  margin-bottom: 15px;
  padding-bottom: 15px;
  border-bottom: 1px solid #EBEEF5;
}

.review-item:last-child {
  margin-bottom: 5px;
  padding-bottom: 5px;
  border-bottom: none;
}

.review-header {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
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
  padding-left: 50px;
  color: #606266;
}

.no-reviews {
  color: #909399;
  font-style: italic;
  text-align: center;
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.doctor-actions {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  gap: 20px;
}

.expertise-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
}

.expertise-tag {
  margin-right: 5px;
  margin-bottom: 5px;
}

.no-data {
  color: #909399;
  font-style: italic;
}

.review-count {
  font-size: 12px;
  color: #909399;
  margin-left: 4px;
}

@media (max-width: 768px) {
  .search-form {
    padding: 15px;
  }
  
  .operation-buttons {
    flex-direction: row;
    flex-wrap: wrap;
    justify-content: center;
  }
  
  .button-column {
    flex-direction: column;
  }
}
</style> 