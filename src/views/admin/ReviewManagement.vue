<template>
  <div class="review-management">
    <div class="header">
      <div class="header-left">
        <h2 class="title">评价管理</h2>
        <el-tag v-if="selectedRows.length === 0" type="info" effect="plain">
          选择评价以进行批量操作
        </el-tag>
      </div>
      <div class="header-buttons">
        <el-button 
          type="danger" 
          @click="handleBatchDelete" 
          :disabled="selectedRows.length === 0"
        >
          <el-icon><Delete /></el-icon>批量删除
        </el-button>
      </div>
    </div>

    <!-- 搜索表单 -->
    <div class="filter-form">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="用户名">
          <el-input
            v-model="searchForm.userName"
            placeholder="请输入用户名"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="医生">
          <el-select
            v-model="searchForm.doctorId"
            placeholder="请选择医生"
            clearable
            style="width: 200px"
          >
            <el-option
              v-for="doctor in doctors"
              :key="doctor.doctorId"
              :label="doctor.name"
              :value="doctor.doctorId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="科室">
          <el-select
            v-model="searchForm.departmentId"
            placeholder="请选择科室"
            clearable
            style="width: 200px"
          >
            <el-option
              v-for="dept in departments"
              :key="dept.id"
              :label="dept.name"
              :value="dept.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="评分">
          <div class="rating-range" style="display: flex; align-items: center;">
            <el-select
              v-model="searchForm.minRating"
              placeholder="最低评分"
              clearable
              style="width: 120px"
            >
              <el-option
                v-for="rating in [1,2,3,4,5]"
                :key="rating"
                :label="rating + '星'"
                :value="rating"
              />
            </el-select>
            <span style="margin: 0 8px;">至</span>
            <el-select
              v-model="searchForm.maxRating"
              placeholder="最高评分"
              clearable
              style="width: 120px"
            >
              <el-option
                v-for="rating in [1,2,3,4,5]"
                :key="rating"
                :label="rating + '星'"
                :value="rating"
              />
            </el-select>
          </div>
          <el-button type="primary" @click="handleSearch" style="margin-left: 10px;">
            <el-icon><Search /></el-icon> 搜索
          </el-button>
          <el-button @click="resetSearch">
            <el-icon><Refresh /></el-icon> 重置
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 评价列表 -->
    <div class="table-container">
      <el-table
        v-loading="loading"
        :data="reviewList"
        border
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column label="用户名称" width="150">
          <template #default="{ row }">
            <div class="user-info">
              <el-avatar :size="32" :src="row.userAvatar">
                <el-icon><User /></el-icon>
              </el-avatar>
              <span>
                {{ row.userName }}
                <el-tag v-if="row.isAnonymous" size="small" type="info" effect="plain" style="margin-left: 5px;">匿名</el-tag>
              </span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="医生" prop="doctorName" width="120" />
        <el-table-column label="科室" prop="departmentName" width="120" />
        <el-table-column label="评分" width="180">
          <template #default="{ row }">
            <el-rate
              v-model="row.rating"
              disabled
              show-score
              text-color="#ff9900"
            />
          </template>
        </el-table-column>
        <el-table-column prop="comment" label="评价内容" min-width="300" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="评价时间" width="180">
          <template #default="{ row }">
            {{ new Date(row.createdAt).toLocaleString() }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button
              type="danger"
              size="small"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, User, Delete } from '@element-plus/icons-vue'
import request from '@/utils/axios'
import { getDepartments } from '@/api/departments'
import { getDoctors } from '@/api/doctors'
import { useRoute } from 'vue-router'
import { getReviewsList, deleteReview, batchDeleteReviews } from '@/api/reviews'

const route = useRoute()

const doctors = ref([])
const departments = ref([])
const loading = ref(false)
const reviewList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const selectedRows = ref([])

// 用户缓存
const userCache = ref(new Map())

const searchForm = ref({
  userName: '',
  doctorId: '',
  departmentId: '',
  minRating: '',
  maxRating: ''
})

// 获取用户信息
const fetchUserInfo = async (userId) => {
  if (!userId) return null
  
  // 如果缓存中已有该用户信息，直接返回
  if (userCache.value.has(userId)) {
    return userCache.value.get(userId)
  }

  try {
    const response = await request.get(`/users/${userId}`)
    if (response.code === 200) {
      const userInfo = response.data
      // 将用户信息存入缓存
      userCache.value.set(userId, userInfo)
      return userInfo
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
  }
  return null
}

// 选择变化处理
const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

// 批量删除
const handleBatchDelete = async () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请选择要删除的评价')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确认删除选中的 ${selectedRows.value.length} 条评价吗？此操作不可恢复！`,
      '警告',
      {
        type: 'warning',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }
    )
    
    const ids = selectedRows.value.map(row => row.id || row.reviewId).filter(id => id != null)
    if (ids.length === 0) {
      ElMessage.error('未找到有效的评价ID')
      return
    }

    const response = await batchDeleteReviews(ids)
    if (response.code === 200) {
      ElMessage.success('批量删除成功')
      if (reviewList.value.length === selectedRows.value.length && currentPage.value > 1) {
        currentPage.value--
      }
      fetchReviews()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败: ' + (error.response?.data?.message || error.message || '未知错误'))
    }
  }
}

// 获取医生列表
const fetchDoctors = async () => {
  try {
    const response = await getDoctors({
      page: 1,
      size: 1000  // 获取足够多的医生数据用于下拉选择
    })
    if (response.code === 200) {
      doctors.value = response.data.records || []
    }
  } catch (error) {
    console.error('获取医生列表失败:', error)
    ElMessage.error('获取医生列表失败')
  }
}

// 获取科室列表
const fetchDepartments = async () => {
  try {
    const response = await getDepartments({
      page: 1,
      size: 1000
    })
    
    if (response.code === 200) {
      // 按照医生管理页面的方式处理科室数据
      console.log('科室API原始响应:', response.data);
      departments.value = response.data.records.map(dept => ({
        id: dept.departmentId,
        name: dept.departmentName
      }));
      console.log('处理后的科室数据:', departments.value);
    }
  } catch (error) {
    console.error('获取科室列表失败:', error)
    ElMessage.error('获取科室列表失败')
  }
}

// 获取评价列表
const fetchReviews = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      doctorName: searchForm.value.doctorId ? doctors.value.find(d => d.doctorId === searchForm.value.doctorId)?.name : undefined,
      userName: searchForm.value.userName || undefined,
      departmentId: searchForm.value.departmentId || undefined,
      minRating: searchForm.value.minRating || undefined,
      maxRating: searchForm.value.maxRating || undefined,
      content: searchForm.value.comment || undefined
    }

    // 移除所有undefined的参数
    Object.keys(params).forEach(key => {
      if (params[key] === undefined) {
        delete params[key]
      }
    })

    const response = await getReviewsList(params)
    if (response.code === 200) {
      const records = response.data.records

      // 获取所有医生ID
      const doctorIds = [...new Set(records.map(review => review.doctorId))]
      
      // 获取医生和科室信息
      const doctorPromises = doctorIds.map(async (doctorId) => {
        try {
          const doctorResponse = await request.get(`/doctors/${doctorId}`)
          if (doctorResponse.code === 200) {
            const doctor = doctorResponse.data
            // 获取科室信息
            if (doctor.departmentId) {
              const deptResponse = await request.get(`/departments/${doctor.departmentId}`)
              if (deptResponse.code === 200) {
                doctor.departmentName = deptResponse.data.name || deptResponse.data.departmentName
              }
            }
            return {
              doctorId,
              ...doctor
            }
          }
        } catch (error) {
          console.error(`获取医生或科室信息失败 (ID: ${doctorId}):`, error)
        }
        return null
      })

      // 获取所有用户ID
      const userIds = [...new Set(records.map(review => review.userId))]
      
      // 获取用户信息
      const userPromises = userIds.map(async (userId) => {
        try {
          const userResponse = await request.get(`/users/${userId}`)
          if (userResponse.code === 200) {
            return {
              userId,
              userName: userResponse.data.username,
              avatarUrl: userResponse.data.avatarUrl
            }
          }
        } catch (error) {
          console.error(`获取用户信息失败 (ID: ${userId}):`, error)
        }
        return null
      })

      // 并行获取医生和用户信息
      const [doctors, users] = await Promise.all([
        Promise.all(doctorPromises),
        Promise.all(userPromises)
      ])

      const doctorMap = new Map(doctors.filter(d => d).map(d => [d.doctorId, d]))
      const userMap = new Map(users.filter(u => u).map(u => [u.userId, u]))
      
      // 处理评价数据
      reviewList.value = records.map(review => {
        const doctor = doctorMap.get(review.doctorId)
        const user = userMap.get(review.userId)
        return {
          id: review.reviewId || review.id,
          ...review,
          userName: review.isAnonymous ? '匿名用户' : (user?.userName || '未知用户'),
          userAvatar: user?.avatarUrl,
          doctorName: doctor?.name || '未知医生',
          doctorAvatar: doctor?.avatarUrl,
          departmentName: doctor?.departmentName || '未知科室',
          rating: review.rating,
          content: review.comment || review.content || '',
          createdAt: review.createdAt,
          isAnonymous: review.isAnonymous
        }
      })
      
      total.value = response.data.total
      console.log('处理后的评价列表:', reviewList.value)
    }
  } catch (error) {
    console.error('获取评价列表失败:', error)
    ElMessage.error('获取评价列表失败')
  } finally {
    loading.value = false
  }
}

// 监听搜索表单变化
watch(
  () => searchForm.value,
  () => {
    currentPage.value = 1
    fetchReviews()
  },
  { deep: true }
)

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchReviews()
}

// 重置搜索
const resetSearch = () => {
  searchForm.value = {
    userName: '',
    doctorId: '',
    departmentId: '',
    minRating: '',
    maxRating: ''
  }
  handleSearch()
}

// 分页处理
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchReviews()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchReviews()
}

// 删除评价
const handleDelete = async (row) => {
  if (!row || (!row.id && !row.reviewId)) {
    ElMessage.error('无效的评价ID');
    return;
  }

  try {
    await ElMessageBox.confirm('确认删除该评价吗？此操作不可恢复！', '警告', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    
    const reviewId = row.id || row.reviewId;
    const response = await deleteReview(reviewId);
    if (response.code === 200) {
      ElMessage.success('删除成功');
      if (reviewList.value.length === 1 && currentPage.value > 1) {
        currentPage.value--;
      }
      fetchReviews();
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error);
      ElMessage.error(typeof error === 'string' ? error : '删除失败');
    }
  }
}

onMounted(async () => {
  // 首先获取医生和科室数据
  await Promise.all([
    fetchDoctors(),
    fetchDepartments()
  ]);
  
  // 然后获取评价数据
  await fetchReviews();
})
</script>

<style scoped>
.review-management {
  padding: 12px;
  min-width: 1000px;
  max-width: 1400px;
  margin: 0 auto;
  background-color: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-buttons {
  display: flex;
  gap: 8px;
}

.title {
  font-size: 20px;
  font-weight: bold;
  color: #303133;
  margin: 0;
}

.filter-form {
  margin-bottom: 10px;
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.table-container {
  margin-top: 20px;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.table-title {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.admin-tag {
  margin-left: 4px;
  font-size: 12px;
  height: 20px;
  line-height: 18px;
}

.anonymous-tag {
  margin-left: 4px;
  font-size: 12px;
  height: 20px;
  line-height: 18px;
}

:deep(.el-table) {
  .el-table__header th {
    background-color: #f5f7fa;
    color: #606266;
    font-weight: 600;
  }
  
  .el-table__row {
    td {
      padding: 8px 0;
    }
  }
}

:deep(.el-button) {
  padding: 8px 16px;
  
  .el-icon {
    margin-right: 4px;
  }
}

:deep(.el-form-item) {
  margin-bottom: 0;
  margin-right: 16px;
}

:deep(.el-select) {
  width: 200px;
}

.tooltip-icon {
  margin-left: 8px;
  font-size: 16px;
  color: #909399;
  cursor: help;
}

/* 添加评价表单样式 */
:deep(.el-textarea__inner) {
  min-height: 120px !important;
  transition: border-color 0.3s;
}

:deep(.el-textarea__inner:hover) {
  border-color: #409eff;
}

:deep(.el-textarea__inner:focus) {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

/* 预览区域样式 */
.comment-preview {
  margin-top: 10px;
  padding: 10px;
  background-color: #f8f9fa;
  border-radius: 4px;
  border: 1px dashed #dcdfe6;
  font-size: 13px;
  color: #606266;
  max-height: 100px;
  overflow-y: auto;
}

.comment-preview-title {
  font-weight: bold;
  margin-bottom: 5px;
  color: #409eff;
}

.form-note {
  margin: 10px 0;
  padding: 10px;
  background-color: #f0f9eb;
  border-radius: 4px;
  font-size: 12px;
  color: #67c23a;
}
</style> 