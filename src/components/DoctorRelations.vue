<template>
  <div class="doctor-relations">
    <!-- 专长关联管理对话框 -->
    <el-dialog
      v-model="expertiseDialogVisible"
      title="管理医生专长"
      width="750px"
    >
      <div v-loading="relationLoading">
        <div class="relation-header">
          <h3>医生: {{ doctorName }}</h3>
          <div class="relation-actions">
            <el-button type="primary" size="small" @click="showAllExpertises = !showAllExpertises">
              {{ showAllExpertises ? '仅显示已关联' : '显示全部专长' }}
            </el-button>
            <el-select
              v-model="selectedDepartmentId"
              placeholder="按科室筛选专长"
              clearable
              filterable
              style="width: 200px; margin-left: 10px;"
              @change="filterExpertiseByDepartment"
            >
              <el-option
                v-for="dept in departments"
                :key="dept.id"
                :label="dept.name"
                :value="dept.id"
              />
            </el-select>
          </div>
        </div>

        <div class="relation-content">
          <div class="expertise-search">
            <el-input
              v-model="expertiseSearchKeyword"
              placeholder="搜索专长名称"
              clearable
              @input="filterExpertise"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </div>

          <div class="batch-actions" v-if="hasSelectedItems">
            <el-button type="danger" size="small" @click="batchRemoveExpertise">
              批量取消关联选中项
            </el-button>
          </div>

          <el-table 
            :data="filteredAndDisplayedExpertises" 
            border 
            max-height="400px"
            @selection-change="handleTableSelectionChange"
          >
            <el-table-column type="selection" width="50" />
            <el-table-column prop="expertiseName" label="专长名称" min-width="200">
              <template #default="{ row }">
                <div class="expertise-name">
                  <span>{{ row.expertiseName }}</span>
                  <el-tag 
                    v-if="row.selected" 
                    size="small" 
                    type="success" 
                    class="selected-tag"
                  >已关联</el-tag>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="departmentName" label="所属科室" width="120" />
            <el-table-column label="熟练度" width="220">
              <template #default="{ row }">
                <el-slider
                  v-if="row.selected"
                  v-model="row.proficiency"
                  :min="0"
                  :max="1"
                  :step="0.01"
                  :format-tooltip="formatProficiency"
                  show-tooltip="always"
                  style="width: 180px;"
                />
                <div v-else class="actions-cell">
                  <el-button 
                    type="primary" 
                    size="small" 
                    @click="addExpertise(row)"
                  >添加关联</el-button>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-button 
                  v-if="row.selected"
                  type="danger" 
                  size="small" 
                  @click="removeExpertise(row)"
                >取消关联</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="expertiseDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveExpertiseRelations">保存</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 科室关联管理对话框 -->
    <el-dialog
      v-model="departmentDialogVisible"
      title="管理医生科室"
      width="600px"
    >
      <div v-loading="relationLoading">
        <div class="relation-content">
          <el-transfer
            v-model="selectedDepartments"
            :data="allDepartments"
            :titles="['可选科室', '已选科室']"
            :props="{
              key: 'id',
              label: 'name'
            }"
          />
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="departmentDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveDepartmentRelations">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getExpertiseList, getDoctorExpertises, updateDoctorExpertises } from '@/api/expertise'
import request from '@/utils/axios'

const props = defineProps({
  doctorId: {
    type: Number,
    required: true
  },
  doctorName: {
    type: String,
    required: true
  }
})

const emit = defineEmits(['update:modelValue', 'refresh'])

const expertiseDialogVisible = ref(false)
const departmentDialogVisible = ref(false)
const relationLoading = ref(false)
const expertiseSearchKeyword = ref('')
const selectedDepartmentId = ref(null)
const expertiseList = ref([])
const selectedDepartments = ref([])
const allDepartments = ref([])
const departments = ref([])
const showAllExpertises = ref(false)
const selectedTableRows = ref([])

// 计算是否有选中项
const hasSelectedItems = computed(() => selectedTableRows.value.length > 0)

// 根据显示设置和筛选条件过滤专长列表
const filteredAndDisplayedExpertises = computed(() => {
  let result = expertiseList.value
  
  // 按部门筛选
  if (selectedDepartmentId.value) {
    result = result.filter(item => item.departmentId === selectedDepartmentId.value)
  }
  
  // 按关键词搜索
  if (expertiseSearchKeyword.value) {
    const keyword = expertiseSearchKeyword.value.toLowerCase()
    result = result.filter(item => 
      item.expertiseName.toLowerCase().includes(keyword)
    )
  }
  
  // 根据显示设置过滤
  if (!showAllExpertises.value) {
    result = result.filter(item => item.selected)
  }
  
  return result
})

// 格式化熟练度显示
const formatProficiency = (val) => {
  return `${Math.round(val * 100)}%`
}

// 处理表格选择变化
const handleTableSelectionChange = (selection) => {
  selectedTableRows.value = selection
}

// 添加专长关联
const addExpertise = (row) => {
  row.selected = true
  row.proficiency = 0.0 // 默认熟练度为0%
}

// 移除专长关联
const removeExpertise = (row) => {
  row.selected = false
  row.proficiency = 0.0
}

// 批量移除专长关联
const batchRemoveExpertise = async () => {
  if (selectedTableRows.value.length === 0) return
  
  try {
    await ElMessageBox.confirm('确定要取消选中专长的关联吗？', '确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    selectedTableRows.value.forEach(row => {
      row.selected = false
      row.proficiency = 0.0
    })
    
    ElMessage.success('已批量取消关联')
  } catch (error) {
    // 用户取消操作
  }
}

// 方法
const showExpertiseDialog = async () => {
  expertiseDialogVisible.value = true
  relationLoading.value = true
  
  try {
    // 获取科室列表
    await fetchDepartments()
    
    // 获取所有专长
    const expertiseResponse = await getExpertiseList({ size: 1000 })
    
    if (expertiseResponse.code === 200) {
      // 先获取医生已关联的专长（包含熟练度）
      const doctorExpertisesResponse = await getDoctorExpertises(props.doctorId)
      
      if (doctorExpertisesResponse.code === 200) {
        const doctorExpertises = doctorExpertisesResponse.data
        
        // 合并数据，添加选择状态和熟练度
        expertiseList.value = expertiseResponse.data.records.map(expertise => {
          const doctorExpertise = doctorExpertises.find(
            item => item.expertiseId === expertise.expertiseId
          )
          
          return {
            ...expertise,
            selected: !!doctorExpertise,
            proficiency: doctorExpertise ? doctorExpertise.proficiency : 0.0
          }
        })
      }
    }
  } catch (error) {
    console.error('获取专长列表失败:', error)
    ElMessage.error('获取专长列表失败')
  } finally {
    relationLoading.value = false
  }
}

// 获取科室列表
const fetchDepartments = async () => {
  try {
    const response = await request.get('/departments/list', {
      params: {
        page: 1,
        size: 1000
      }
    })
    
    if (response.code === 200) {
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

const showDepartmentDialog = async () => {
  departmentDialogVisible.value = true
  relationLoading.value = true
  
  try {
    // 获取所有科室
    const response = await request({
      url: '/departments',
      method: 'get',
      params: { size: 1000 }
    })
    
    if (response.code === 200) {
      allDepartments.value = response.data.records.map(dept => ({
        id: dept.departmentId,
        name: dept.departmentName
      }))
      
      // 获取医生已关联的科室
      const doctorDepartments = await request({
        url: `/doctors/${props.doctorId}/departments`,
        method: 'get'
      })
      
      if (doctorDepartments.code === 200) {
        selectedDepartments.value = doctorDepartments.data.map(dept => dept.departmentId)
      }
    }
  } catch (error) {
    console.error('获取科室列表失败:', error)
    ElMessage.error('获取科室列表失败')
  } finally {
    relationLoading.value = false
  }
}

const saveExpertiseRelations = async () => {
  relationLoading.value = true
  try {
    // 筛选出选中的专长，构建包含熟练度的数据
    const selectedExpertisesData = expertiseList.value
      .filter(expertise => expertise.selected)
      .map(expertise => ({
        expertiseId: expertise.expertiseId,
        proficiency: expertise.proficiency
      }))
    
    // 使用支持熟练度的API更新医生专长
    const response = await updateDoctorExpertises(props.doctorId, selectedExpertisesData)
    
    if (response.code === 200) {
      ElMessage.success('专长关联更新成功')
      expertiseDialogVisible.value = false
      emit('refresh')
    }
  } catch (error) {
    console.error('更新专长关联失败:', error)
    ElMessage.error('更新专长关联失败')
  } finally {
    relationLoading.value = false
  }
}

const saveDepartmentRelations = async () => {
  relationLoading.value = true
  try {
    const response = await request({
      url: `/doctors/${props.doctorId}/departments`,
      method: 'post',
      data: {
        departmentIds: selectedDepartments.value
      }
    })
    
    if (response.code === 200) {
      ElMessage.success('科室关联更新成功')
      departmentDialogVisible.value = false
      emit('refresh')
    }
  } catch (error) {
    console.error('更新科室关联失败:', error)
    ElMessage.error('更新科室关联失败')
  } finally {
    relationLoading.value = false
  }
}

const filterExpertiseByDepartment = () => {
  // 由computed属性自动处理过滤逻辑
}

const filterExpertise = () => {
  // 由computed属性自动处理过滤逻辑
}

// 导出方法供外部调用
defineExpose({
  showExpertiseDialog,
  showDepartmentDialog
})
</script>

<style scoped>
.relation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.relation-header h3 {
  margin: 0;
  font-size: 16px;
  color: #303133;
}

.relation-actions {
  display: flex;
  align-items: center;
}

.expertise-search {
  margin-bottom: 16px;
}

.batch-actions {
  margin-bottom: 10px;
}

.relation-content {
  padding: 0 20px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.disabled-text {
  color: #909399;
  font-size: 13px;
  font-style: italic;
}

.expertise-name {
  display: flex;
  align-items: center;
}

.selected-tag {
  margin-left: 8px;
}

.actions-cell {
  display: flex;
  justify-content: center;
}
</style> 