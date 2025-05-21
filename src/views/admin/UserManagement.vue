<template>
  <div class="user-management">
    <div class="header">
      <div class="header-left">
        <h2>用户管理</h2>
        <el-tag v-if="selectedRows.length === 0" type="info" effect="plain">
          选择用户以进行批量操作
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
        <el-button type="primary" @click="showAddDialog">
          <el-icon><Plus /></el-icon>添加用户
        </el-button>
      </div>
    </div>

    <!-- 搜索区域 -->
    <div class="search-area">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="用户名称">
          <el-input
            v-model="searchForm.username"
            placeholder="搜索用户名称"
            clearable
            style="width: 140px"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="手机号码">
          <el-input
            v-model="searchForm.phone"
            placeholder="搜索手机号"
            clearable
            style="width: 140px"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Phone /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="性别">
          <el-select
            v-model="searchForm.gender"
            placeholder="选择性别"
            clearable
            style="width: 90px"
            @change="handleSearch"
          >
            <el-option label="男" value="男" />
            <el-option label="女" value="女" />
          </el-select>
        </el-form-item>
        <el-form-item >
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>搜索
          </el-button>
          <el-button @click="resetSearch">
            <el-icon><Refresh /></el-icon>重置
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 用户列表 -->
    <div class="table-container">
    <el-table
      v-loading="loading"
      :data="users"
      row-key="id"
      border
      style="width: 100%"
      @selection-change="handleSelectionChange"
    >
        <el-table-column type="selection" width="45" />
        <el-table-column label="用户名称" min-width="120">
        <template #default="{ row }">
          <div class="user-info">
              <el-avatar :size="28" :src="row.avatarUrl">
              <el-icon><User /></el-icon>
            </el-avatar>
              <span>{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="gender" label="性别" width="60" align="center" />
        <el-table-column prop="age" label="年龄" width="60" align="center" />
        <el-table-column prop="phone" label="手机号码" width="120" align="center" />
        <el-table-column prop="medicalRecord" label="病历描述" min-width="120" show-overflow-tooltip />
        <el-table-column label="评价数量" width="100" align="center">
          <template #default="{ row }">
            <span>{{ row.reviewCount || 0 }}条</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
            <div class="button-row">
              <el-button type="primary" size="small" @click="showEditDialog(row)">编辑</el-button>
              <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
              <el-button type="info" size="small" @click="goToReviews(row)">评价</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>
    </div>

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

    <!-- 用户评价对话框 -->
    <el-dialog
      v-model="reviewDialogVisible"
      :title="`${currentUser?.name || '用户'}的评价记录`"
      width="800px"
      destroy-on-close
    >
      <div class="review-dialog-content">
        <!-- 评分统计 -->
        <div class="rating-summary">
          <div class="average-rating">
            <span class="label">平均评分</span>
            <el-rate
              v-model="currentUser.averageRating"
              disabled
              show-score
              text-color="#ff9900"
              score-template="{value}"
            />
          </div>
          <div class="total-reviews">
            总评价数：{{ currentUser.reviewCount || 0 }}
          </div>
        </div>

        <!-- 评价列表 -->
        <el-table
          v-loading="reviewsLoading"
          :data="userReviews"
          border
          style="width: 100%; margin-top: 20px;"
        >
          <el-table-column label="医生" prop="doctorName" min-width="120" />
          <el-table-column label="科室" prop="departmentName" min-width="120" />
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
          <el-table-column label="评价内容" prop="content" min-width="250" show-overflow-tooltip />
          <el-table-column label="评价时间" width="180">
            <template #default="{ row }">
              {{ new Date(row.createdAt).toLocaleString() }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80" fixed="right">
            <template #default="{ row }">
              <el-button
                type="danger"
                size="small"
                @click="handleDeleteReview(row)"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 评价分页 -->
        <div class="review-pagination">
          <el-pagination
            v-model:current-page="reviewCurrentPage"
            v-model:page-size="reviewPageSize"
            :total="reviewTotal"
            :page-sizes="[5, 10, 20]"
            layout="total, sizes, prev, pager, next"
            @size-change="handleReviewSizeChange"
            @current-change="handleReviewPageChange"
          />
        </div>
      </div>
    </el-dialog>

    <!-- 添加/编辑用户对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '添加用户' : '编辑用户'"
      width="600px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="userForm"
        :rules="rules"
        label-width="100px"
        status-icon
        class="user-form"
      >
        <el-form-item label="用户名称" prop="name">
          <el-input 
            v-model="userForm.name" 
            maxlength="50"
            placeholder="请输入用户名称"
            clearable
          />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="userForm.gender">
            <el-radio label="男">男</el-radio>
            <el-radio label="女">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="年龄" prop="age">
          <el-input-number 
            v-model="userForm.age" 
            :min="0" 
            :max="200"
            placeholder="请输入年龄"
            style="width: 100%"
            size="large"
            :controls="false"
          />
        </el-form-item>
        <el-form-item 
          label="手机号码" 
          prop="phone"
          :error="phoneError"
        >
          <el-input 
            v-model="userForm.phone" 
            maxlength="11"
            placeholder="请输入手机号码"
            clearable
            @blur="validatePhone"
          />
        </el-form-item>
        <el-form-item label="病历描述" prop="medicalRecord">
          <el-input
            v-model="userForm.medicalRecord"
            type="textarea"
            :rows="4"
            maxlength="500"
            show-word-limit
            placeholder="请输入病历描述（选填）"
            resize="none"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          确定
        </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Delete, User, Phone, Refresh } from '@element-plus/icons-vue'
import request from '@/utils/axios'
import { useRouter } from 'vue-router'
import { getReviewsList, deleteReview } from '@/api/reviews'

// 数据
const users = ref([])
const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const dialogType = ref('add')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const selectedRows = ref([])

const searchForm = ref({
  username: '',
  phone: '',
  gender: '',
  minAge: null,
  maxAge: null
})

const userForm = ref({
  name: '',
  gender: '男',
  age: 0,
  phone: '',
  medicalRecord: ''
})

const formRef = ref(null)

const phoneError = ref('')

const router = useRouter()

// 评价相关数据
const reviewDialogVisible = ref(false)
const reviewsLoading = ref(false)
const userReviews = ref([])
const currentUser = ref({})
const reviewCurrentPage = ref(1)
const reviewPageSize = ref(10)
const reviewTotal = ref(0)

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入用户名称', trigger: 'blur' },
    { max: 50, message: '用户名称不能超过50个字符', trigger: 'blur' }
  ],
  gender: [
    { required: true, message: '请选择性别', trigger: 'change' }
  ],
  age: [
    { required: true, message: '请输入年龄', trigger: 'blur' },
    { type: 'number', min: 0, max: 150, message: '年龄必须在0-150岁之间', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号码', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ]
}

// 获取用户列表
const fetchUsers = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      username: searchForm.value.username || undefined,
      phone: searchForm.value.phone || undefined,
      gender: searchForm.value.gender || undefined
    }
    
    // 移除所有undefined的参数
    Object.keys(params).forEach(key => {
      if (params[key] === undefined) {
        delete params[key]
      }
    })
    
    const response = await request.get('/users', { params })
    
    if (response.code === 200) {
      // 获取每个用户的评价数量
      const userIds = response.data.records.map(user => user.userId)
      const reviewStatsPromises = userIds.map(userId =>
        getReviewsList({
            userId: userId,
            page: 1,
            size: 1
        }).catch(() => ({ code: 500, data: { total: 0 } }))
      )
      
      const reviewStatsResponses = await Promise.all(reviewStatsPromises)
      const reviewCountMap = new Map()
      
      reviewStatsResponses.forEach((statsRes, index) => {
        if (statsRes.code === 200) {
          reviewCountMap.set(userIds[index], statsRes.data.total || 0)
        }
      })

      users.value = response.data.records.map(user => ({
        id: user.userId,
        name: user.username,
        gender: user.gender,
        age: user.age,
        phone: user.phone,
        medicalRecord: user.medicalRecord,
        avatarUrl: user.avatarUrl,
        reviewCount: reviewCountMap.get(user.userId) || 0
      }))
      total.value = response.data.total
    }
  } catch (error) {
    console.error('获取用户列表失败:', error)
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

// 选择变化处理
const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

// 批量删除
const handleBatchDelete = async () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请选择要删除的用户')
    return
  }

  try {
    // 检查所有选中的用户是否有评论
    let totalReviewCount = 0
    const usersWithReviews = []
    
    for (const row of selectedRows.value) {
      try {
        const response = await getReviewsList({
          userId: row.id,
          page: 1,
          size: 1
        })
        if (response.code === 200 && response.data && response.data.total > 0) {
          totalReviewCount += response.data.total
          usersWithReviews.push(row.name)
        }
      } catch (error) {
        console.error(`检查用户 ${row.name} 的评论失败:`, error)
        // 如果获取评论失败，继续流程
      }
    }
    
    // 根据有无评论显示不同的确认对话框
    if (usersWithReviews.length > 0) {
      const userNames = usersWithReviews.length <= 3 
        ? usersWithReviews.join('、')
        : `${usersWithReviews.slice(0, 3).join('、')}等${usersWithReviews.length}位用户`;
      
      await ElMessageBox.confirm(
        `选中的用户中，${userNames} 共有 ${totalReviewCount} 条评价记录，删除用户将同时删除所有相关评价，是否继续？`,
        '警告',
        {
          type: 'warning',
          confirmButtonText: '确定删除',
          cancelButtonText: '取消'
        }
      )
    } else {
      await ElMessageBox.confirm(
        `确认删除选中的 ${selectedRows.value.length} 个用户吗？此操作不可恢复！`,
        '警告',
        {
          type: 'warning',
          confirmButtonText: '确定',
          cancelButtonText: '取消'
        }
      )
    }
    
    const deletePromises = selectedRows.value.map(row => 
      request.delete(`/users/${row.id}`)
    )
    
    await Promise.all(deletePromises)
    ElMessage.success('批量删除成功')
    
    if (users.value.length === selectedRows.value.length && currentPage.value > 1) {
      currentPage.value--
    }
    fetchUsers()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败')
    }
  }
}

// 搜索和重置
const handleSearch = () => {
  if (searchForm.value.minAge && searchForm.value.maxAge && 
      searchForm.value.minAge > searchForm.value.maxAge) {
    ElMessage.warning('最小年龄不能大于最大年龄')
    return
  }
  currentPage.value = 1
  fetchUsers()
}

const resetSearch = () => {
  searchForm.value = {
    username: '',
    phone: '',
    gender: '',
    minAge: null,
    maxAge: null
  }
  handleSearch()
}

// 分页处理
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchUsers()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchUsers()
}

// 获取用户评价列表
const fetchUserReviews = async (userId) => {
  reviewsLoading.value = true
  try {
    const response = await getReviewsList({
        userId: userId,
        page: reviewCurrentPage.value,
        size: reviewPageSize.value
    })
    
    if (response.code === 200 && response.data) {
      userReviews.value = (response.data.records || []).map(review => {
        // 确保我们有正确的评论ID
        const reviewId = review.reviewId || review.id;
        if (!reviewId) {
          console.error('评论缺少ID:', review);
        }
        return {
          id: reviewId,
          doctorName: review.doctorName || '未知医生',
          departmentName: review.departmentName || '未知科室',
          rating: review.rating || 0,
          content: review.comment || review.content || '',
          createdAt: review.createdAt
        };
      });
      
      reviewTotal.value = response.data.total || 0;

      // 更新当前用户的评分统计
      if (userReviews.value.length > 0) {
        const totalRating = userReviews.value.reduce((sum, review) => sum + review.rating, 0);
        currentUser.value = {
          ...currentUser.value,
          averageRating: +(totalRating / userReviews.value.length).toFixed(1),
          reviewCount: reviewTotal.value
        };
      } else {
        currentUser.value = {
          ...currentUser.value,
          averageRating: 0,
          reviewCount: 0
        };
      }
    }
  } catch (error) {
    console.error('获取用户评价失败:', error);
    ElMessage.error('获取用户评价失败');
    userReviews.value = [];
    reviewTotal.value = 0;
  } finally {
    reviewsLoading.value = false;
  }
}

// 修改查看评价的方法
const showUserReviews = (row) => {
  console.log('Showing reviews for user:', row)  // 添加日志
  currentUser.value = {
    id: row.id,
    name: row.name,
    reviewCount: row.reviewCount || 0
  }
  reviewDialogVisible.value = true
  reviewCurrentPage.value = 1
  reviewPageSize.value = 10
  fetchUserReviews(row.id)  // 确保传递正确的用户ID
}

// 修改评价分页变化的处理方法
const handleReviewPageChange = (val) => {
  reviewCurrentPage.value = val
  if (currentUser.value && currentUser.value.id) {
  fetchUserReviews(currentUser.value.id)
}
}

const handleReviewSizeChange = (val) => {
  reviewPageSize.value = val
  if (currentUser.value && currentUser.value.id) {
    fetchUserReviews(currentUser.value.id)
  }
}

// 修改删除评价的方法
const handleDeleteReview = async (row) => {
  if (!row || !row.id) {
    console.error('评论数据缺少ID:', row);
    ElMessage.error('无效的评价ID');
    return;
  }

  try {
    await ElMessageBox.confirm('确认删除该评价吗？此操作不可恢复！', '警告', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    });
    
    console.log('正在删除评论，ID:', row.id);
    const response = await deleteReview(row.id);
    if (response.code === 200) {
      ElMessage.success('删除成功');
      // 重新获取评价列表
      if (currentUser.value && currentUser.value.id) {
        fetchUserReviews(currentUser.value.id);
      }
      // 重新获取用户列表以更新评分统计
      fetchUsers();
    } else {
      throw new Error(response.message || '删除失败');
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error);
      ElMessage.error(error.message || '删除失败');
    }
  }
}

// 添加/编辑用户
const showAddDialog = () => {
  dialogType.value = 'add'
  userForm.value = {
    name: '',
    gender: '男',
    age: 0,
    phone: '',
    medicalRecord: ''
  }
  phoneError.value = ''
  dialogVisible.value = true
}

const showEditDialog = (row) => {
  dialogType.value = 'edit'
  userForm.value = {
    id: row.id,
    name: row.name,
    gender: row.gender,
    age: row.age,
    phone: row.phone,
    medicalRecord: row.medicalRecord
  }
  dialogVisible.value = true
}

const validatePhone = () => {
  if (!userForm.value.phone) {
    phoneError.value = '请输入手机号码'
    return false
  }
  if (!/^1[3-9]\d{9}$/.test(userForm.value.phone)) {
    phoneError.value = '请输入正确的手机号码'
    return false
  }
  phoneError.value = ''
  return true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    if (!validatePhone()) {
      return
    }
      submitting.value = true
    
        const isAdd = dialogType.value === 'add'
    const submitData = {
      username: userForm.value.name,
      age: userForm.value.age,
      gender: userForm.value.gender,
      phone: userForm.value.phone,
      medicalRecord: userForm.value.medicalRecord
    }

    if (!isAdd) {
      submitData.id = userForm.value.id
    }
    
        const response = await request({
          method: isAdd ? 'post' : 'put',
          url: isAdd ? '/users' : `/users/${userForm.value.id}`,
      data: submitData
    })
    
    // 检查响应状态
    if (response.code === 200 || response.code === 201) {
      ElMessage({
        type: 'success',
        message: isAdd ? '新增用户成功' : '更新用户成功',
        duration: 2000
      })
          dialogVisible.value = false
      fetchUsers() // 刷新用户列表
    } else {
      ElMessage({
        type: 'error',
        message: response.message || (isAdd ? '新增用户失败' : '更新用户失败'),
        duration: 2000
      })
        }
      } catch (error) {
    if (error.errors) {
      // 表单验证失败
      console.error('表单验证失败:', error.errors)
      ElMessage({
        type: 'error',
        message: '请完善必填项信息',
        duration: 2000
      })
    } else {
      // API请求失败
        console.error(isAdd ? '添加用户失败:' : '更新用户失败:', error)
      ElMessage({
        type: 'error',
        message: error.response?.data?.message || (isAdd ? '新增用户失败' : '更新用户失败'),
        duration: 2000
      })
    }
      } finally {
        submitting.value = false
      }
}

// 删除用户
const handleDelete = async (row) => {
  try {
    // 检查用户是否有评论
    let hasReviews = false
    let reviewCount = 0
    try {
      const response = await getReviewsList({
        userId: row.id,
        page: 1,
        size: 1
      })
      if (response.code === 200 && response.data) {
        reviewCount = response.data.total || 0
        hasReviews = reviewCount > 0
      }
    } catch (error) {
      console.error('获取用户评论失败:', error)
      // 如果获取评论失败，我们假设没有评论，继续流程
    }
    
    // 根据是否有评论显示不同的确认对话框
    if (hasReviews) {
      await ElMessageBox.confirm(
        `用户 "${row.name}" 有 ${reviewCount} 条评价记录，删除用户将同时删除所有相关评价，是否继续？`,
        '警告',
        {
          type: 'warning',
          confirmButtonText: '确定删除',
          cancelButtonText: '取消'
        }
      )
    } else {
      await ElMessageBox.confirm(
        '确认删除该用户吗？此操作不可恢复！',
        '警告',
        {
          type: 'warning',
          confirmButtonText: '确定',
          cancelButtonText: '取消'
        }
      )
    }
    
    const response = await request.delete(`/users/${row.id}`)
    if (response.code === 200) {
      ElMessage.success('删除成功')
      if (users.value.length === 1 && currentPage.value > 1) {
        currentPage.value--
      }
      fetchUsers()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 修改原有的 goToReviews 方法
const goToReviews = (row) => {
  showUserReviews(row)
}

onMounted(() => {
  fetchUsers()
})
</script>

<style scoped>
.user-management {
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

  h2 {
    margin: 0;
  }
}

.header-buttons {
  display: flex;
  gap: 8px;
}

.search-area {
  margin-bottom: 12px;
  background-color: #f5f7fa;
  padding: 12px;
  border-radius: 4px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-start;
  gap: 10px;
  width: 100%;
}

.age-range {
  margin-right: 0;
  margin-left: 0;
}

.age-inputs {
  width: 200px;
}

.age-input {
  width: 1000px;
}

.age-separator {
  margin: 0 8px;
  color: #606266;
  font-weight: bold;
}

.search-buttons {
  margin-right: 0;
  margin-left: auto;
}

:deep(.el-form-item) {
  margin-bottom: 8px;
  margin-right: 8px;
}

:deep(.el-input-number .el-input__wrapper) {
  padding: 0 8px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-name {
  font-weight: 500;
  color: #303133;
}

.user-phone {
  font-size: 13px;
  color: #909399;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.button-container {
  display: flex;
  justify-content: center;
}

.button-row {
  display: flex;
  gap: 4px;
}

.button-row .el-button {
  margin: 0;
}

.button-row .el-button + .el-button {
  margin-left: 0;
}

.doctor-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.no-doctor {
  color: #909399;
  font-size: 13px;
}

.expertise-info {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.expertise-tag {
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.no-expertise {
  color: #909399;
  font-size: 13px;
}

.user-form {
  padding: 20px 40px;
}

:deep(.el-form-item) {
  margin-bottom: 22px;
}

:deep(.el-input),
:deep(.el-input-number) {
  width: 100%;
}

:deep(.el-form-item__label) {
  font-weight: 500;
}

:deep(.el-form-item__error) {
  padding-top: 4px;
  font-size: 13px;
}

.dialog-footer {
  padding: 10px 20px;
  text-align: right;
}

:deep(.el-dialog__body) {
  padding: 0;
}

:deep(.el-dialog__header) {
  margin-right: 0;
  padding: 20px;
  border-bottom: 1px solid #dcdfe6;
}

:deep(.el-dialog__footer) {
  border-top: 1px solid #dcdfe6;
  padding: 0;
}

:deep(.el-textarea__inner) {
  font-family: inherit;
}

.rating-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.rating-score {
  display: flex;
  align-items: center;
  justify-content: center;
}

.no-rating {
  color: #909399;
  font-size: 13px;
}

.review-count {
  font-size: 12px;
  color: #909399;
}

:deep(.el-rate) {
  display: inline-flex;
  justify-content: center;
  line-height: 1;
}

:deep(.el-rate__text) {
  font-size: 13px;
  margin-left: 4px;
}

.button-container {
  display: flex;
  justify-content: center;
}

.button-row {
  display: flex;
  gap: 4px;
}

:deep(.el-button--small) {
  padding: 4px 8px;
  min-width: 46px;
  font-size: 12px;
}

:deep(.el-button--small .el-icon) {
  font-size: 12px;
}

:deep(.el-table) {
  width: 100% !important;
  font-size: 13px;
}

:deep(.el-table__body) {
  width: 100% !important;
}

:deep(.el-table__header) {
  width: 100% !important;
}

.table-container {
  overflow-x: auto;
  margin: 0 -12px;
  padding: 0 12px;
}

:deep(.el-input),
:deep(.el-select) {
  width: 140px !important;
}

:deep(.el-input-number) {
  width: 70px !important;
}

:deep(.el-input-number.el-input-number--large) {
  width: 100% !important;
}

:deep(.el-input-number.el-input-number--large .el-input__wrapper) {
  height: 40px;
  padding: 0 15px;
}

:deep(.el-input-number--large .el-input__inner) {
  text-align: center;
  font-size: 16px;
}

.review-dialog-content {
  padding: 20px;
}

.rating-summary {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.average-rating {
  display: flex;
  align-items: center;
  gap: 12px;
}

.average-rating .label {
  font-size: 14px;
  color: #606266;
}

.total-reviews {
  font-size: 14px;
  color: #606266;
}

.review-pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style> 