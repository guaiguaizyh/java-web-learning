<template>
  <el-dialog
    v-model="dialogVisible"
    title="我的评价"
    width="800px"
    destroy-on-close
  >
    <div class="user-reviews-container">
      <div class="reviews-list" v-loading="loading">
        <el-empty v-if="!loading && (!reviews || reviews.length === 0)" description="暂无评价" />
        
        <el-table v-else :data="reviews" border style="width: 100%">
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
          <el-table-column label="评价时间" width="180">
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
        <div class="pagination" v-if="total > pageSize">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[5, 10, 20]"
            :total="total"
            layout="total, sizes, prev, pager, next"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getReviewsList, deleteReview } from '@/api/reviews'
import request from '@/utils/axios'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue'])

const dialogVisible = ref(false)
const loading = ref(false)
const reviews = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 监听对话框可见性
watch(() => props.modelValue, (newVal) => {
  dialogVisible.value = newVal
  if (newVal) {
    fetchUserReviews()
  }
})

// 监听对话框关闭
watch(() => dialogVisible.value, (newVal) => {
  emit('update:modelValue', newVal)
})

// 获取用户评价列表
const fetchUserReviews = async () => {
  loading.value = true
  try {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    const userId = userInfo.userId || userInfo.id

    if (!userId) {
      throw new Error('未找到用户信息')
    }

    const response = await getReviewsList({
      userId: userId,
      page: currentPage.value,
      size: pageSize.value
    })

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

      // 并行获取医生信息
      const doctors = await Promise.all(doctorPromises)
      const doctorMap = new Map(doctors.filter(d => d).map(d => [d.doctorId, d]))
      
      // 处理评价数据
      reviews.value = records.map(review => ({
        id: review.reviewId || review.id,
        ...review,
        doctorName: doctorMap.get(review.doctorId)?.name || '未知医生',
        departmentName: doctorMap.get(review.doctorId)?.departmentName || '未知科室',
        rating: review.rating,
        comment: review.comment || review.content || '',
        createdAt: review.createdAt
      }))
      
      total.value = response.data.total
    }
  } catch (error) {
    console.error('获取用户评价失败:', error)
    ElMessage.error('获取用户评价失败')
    reviews.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 删除评价
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确认删除该评价吗？此操作不可恢复！', '警告', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    
    const response = await deleteReview(row.id)
    if (response.code === 200) {
      ElMessage.success('删除成功')
      // 重新获取评价列表
      fetchUserReviews()
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

// 处理分页大小变化
const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
  fetchUserReviews()
}

// 处理页码变化
const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchUserReviews()
}
</script>

<style scoped>
.user-reviews-container {
  padding: 20px 0;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

:deep(.el-table) {
  margin-top: 20px;
}

:deep(.el-table__empty-text) {
  line-height: 60px;
}

:deep(.el-button--small) {
  padding: 6px 12px;
  font-size: 12px;
}
</style> 