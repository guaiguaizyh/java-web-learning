<template>
  <div class="symptom-management">
    <div class="page-header">
      <div class="header-left">
        <h2>症状管理</h2>
        <el-tag v-if="selectedRows.length > 0" type="info">
          已选择 {{ selectedRows.length }} 项
        </el-tag>
      </div>
      <div class="header-buttons">
        <el-button
          type="danger"
          :disabled="selectedRows.length === 0"
          @click="handleBatchDelete"
        >
          <el-icon><Delete /></el-icon>
          批量删除
        </el-button>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          添加症状
        </el-button>
      </div>
    </div>

    <!-- 搜索表单 -->
    <div class="search-area">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="症状关键词" style="margin-right: 10px;">
          <el-input
            v-model="searchForm.keyword"
            placeholder="请输入症状关键词"
            clearable
            style="width: 150px;"
            class="search-input"
            @keyup.enter="handleSearch"
            @clear="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="所属科室" style="margin-right: 10px;">
          <el-select
            v-model="searchForm.departmentId"
            placeholder="请选择科室"
            clearable
            style="width: 150px;"
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

        <el-form-item>
          <div class="search-buttons">
            <el-button type="primary" size="small" @click="handleSearch">
              <el-icon><Search /></el-icon>搜索
            </el-button>
            <el-button size="small" @click="resetSearch">
              <el-icon><Refresh /></el-icon>重置
            </el-button>
          </div>
        </el-form-item>
      </el-form>
    </div>

    <!-- 数据表格 -->
    <el-table
      v-loading="loading"
      :data="symptomList"
      border
      style="width: 100%"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" />
      <el-table-column prop="symptomId" label="ID" width="80" />
      <el-table-column prop="keyword" label="症状关键词" width="150" />
      <el-table-column prop="departmentName" label="所属科室" width="150" />
      <el-table-column label="相关专长" min-width="300">
        <template #default="{ row }">
          <div class="expertise-list" v-if="row.relatedExpertises && row.relatedExpertises.length > 0">
            <el-scrollbar height="100px">
              <div 
                v-for="expertise in row.relatedExpertises" 
                :key="expertise.expertiseId || expertise.expertise_id" 
                class="expertise-item"
              >
                <span class="expertise-name">{{ expertise.expertiseName || expertise.expertise_name }}</span>
                <span class="expertise-score">{{ 
                  typeof expertise.relevanceScore === 'number' ? 
                    (expertise.relevanceScore * 100).toFixed(0) + '%' : 
                    (typeof expertise.relevance_score === 'number' ? 
                      (expertise.relevance_score * 100).toFixed(0) + '%' : '未设置') 
                }}</span>
              </div>
            </el-scrollbar>
          </div>
          <span v-else class="no-data">暂无相关专长</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="300" fixed="right">
        <template #default="{ row }">
          <div class="button-group">
            <div class="button-row">
            <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
              <el-button type="success" size="small" @click="handleExpertiseRelation(row)">专长</el-button>
            </div>
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

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '添加症状' : '编辑症状'"
      width="500px"
    >
      <el-form
        ref="symptomFormRef"
        :model="symptomForm"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="症状关键词" prop="keyword">
          <el-input 
            v-model="symptomForm.keyword" 
            placeholder="请输入症状关键词"
            maxlength="10"
            show-word-limit 
          />
        </el-form-item>
        <el-form-item label="所属科室" prop="departmentId">
          <el-select
            v-model="symptomForm.departmentId"
            placeholder="请选择科室"
            style="width: 100%"
            filterable
          >
            <el-option
              v-for="dept in departments"
              :key="dept.id"
              :label="dept.name"
              :value="dept.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 专长关联管理对话框 -->
    <el-dialog
      v-model="relationDialogVisible"
      title="管理专长关联"
      width="750px"
    >
      <div v-loading="relationLoading">
        <div class="relation-header">
          <h3>症状: {{ selectedSymptom ? selectedSymptom.keyword : '' }}</h3>
          <div class="search-box">
            <el-button
              type="primary"
              size="small"
              @click="toggleRelatedExpertises"
            >
              {{ showOnlyRelated ? '显示全部专长' : '仅显示已关联' }}
            </el-button>
            <el-input
              v-model="expertiseSearchKeyword"
              placeholder="搜索专长名称"
              clearable
              style="width: 180px; margin-left: 10px;"
              @input="filterExpertise"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-tooltip
              content="只能关联同一科室的专长"
              placement="top"
            >
              <el-select
                v-model="selectedDepartmentId"
                placeholder="按科室筛选"
                clearable
                style="width: 180px; margin-left: 10px;"
                @change="filterExpertiseByDepartment"
                disabled
              >
                <el-option
                  v-for="dept in departments"
                  :key="dept.id"
                  :label="dept.name"
                  :value="dept.id"
                />
              </el-select>
            </el-tooltip>
          </div>
        </div>

        <el-table :data="filteredAndDisplayedExpertises" border max-height="400px">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column prop="expertiseName" label="专长名称" min-width="120">
            <template #default="{ row }">
              <div class="expertise-name-with-tag">
                <span>{{ row.expertiseName }}</span>
                <el-tag v-if="row.relevanceScore > 0" size="small" type="success" effect="plain">已关联</el-tag>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="所属科室" min-width="120">
            <template #default="{ row }">
              {{ row.departmentName || getDepartmentName(row.departmentId) || '未设置' }}
            </template>
          </el-table-column>
          <el-table-column label="相关度" width="260">
            <template #default="{ row }">
              <template v-if="row.relevanceScore === 0 && !row.isEditing">
                <el-button
                  type="primary"
                  size="small"
                  class="relation-btn add-relation"
                  @click="startAddExpertiseRelation(row)"
                >
                  添加关联
                </el-button>
              </template>
              <template v-else-if="row.isEditing">
                <div class="editing-container">
                  <el-slider
                    v-model="row.relevanceScore"
                    :min="0"
                    :max="1"
                    :step="0.1"
                    :format-tooltip="formatTooltip"
                    :show-tooltip="true"
                    style="margin: 0 10px; width: 140px;"
                  />
                  <el-button
                    type="success"
                    size="small"
                    @click="saveExpertiseRelation(row)"
                  >
                    保存
                  </el-button>
          </div>
              </template>
              <template v-else>
                <el-slider
                  v-model="row.relevanceScore"
                  :min="0"
                  :max="1"
                  :step="0.1"
                  :format-tooltip="formatTooltip"
                  :show-tooltip="true"
                  style="margin: 0 10px;"
                />
              </template>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" align="center">
            <template #default="{ row }">
              <el-button
                v-if="row.relevanceScore > 0"
                type="danger"
                size="small"
                class="relation-btn remove-relation"
                @click="removeExpertiseRelation(row)"
              >
                删除关联
              </el-button>
              </template>
            </el-table-column>
          </el-table>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="relationDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveRelations">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, Connection, Delete, Sort } from '@element-plus/icons-vue'
import { getSymptomList, addSymptom, updateSymptom, deleteSymptom, getSymptomExpertises, batchDeleteSymptoms } from '@/api/symptom'
import { getDepartments } from '@/api/departments'
import { getExpertiseList } from '@/api/expertise'
import { getExpertiseSymptoms, updateExpertiseSymptoms } from '@/api/symptom'

// 数据
const loading = ref(false)
const symptomList = ref([])
const departments = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const dialogVisible = ref(false)
const dialogType = ref('add')
const currentSymptomId = ref(null)
const selectedRows = ref([])
const selectedSymptom = ref(null)

// 专长关联相关
const relationDialogVisible = ref(false)
const relationLoading = ref(false)
const expertiseList = ref([])
const expertiseSearchKeyword = ref('')
const selectedDepartmentId = ref(null)
const selectedExpertiseRows = ref([])
const dialogExpertiseVisible = ref(false)
const selectedExpertises = ref([])
const showOnlyRelated = ref(false)

// 搜索表单数据
const searchForm = ref({
  keyword: '',
  departmentId: ''
})

const symptomForm = reactive({
  keyword: '',
  departmentId: null
})

const symptomFormRef = ref(null)

// 排序选项
const sortOptions = [
  { label: '默认排序', value: '' },
  { label: '按症状ID排序', value: 'symptomId' },
  { label: '按关键词排序', value: 'keyword' },
  { label: '按科室名称排序', value: 'departmentName' },
  { label: '按专长数量排序', value: 'expertiseCount' }
]

// 计算属性
const filteredExpertiseList = computed(() => {
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
  
  return result
})

// 计算属性：过滤后的已关联专长列表
const filteredAndDisplayedExpertises = computed(() => {
  let result = expertiseList.value;
  
  // 应用科室筛选
  if (selectedDepartmentId.value) {
    result = result.filter(item => item.departmentId === selectedDepartmentId.value);
  }
  
  // 应用关键词搜索
  if (expertiseSearchKeyword.value) {
    const keyword = expertiseSearchKeyword.value.toLowerCase();
    result = result.filter(item => 
      (item.expertiseName && item.expertiseName.toLowerCase().includes(keyword))
    );
  }
  
  // 根据显示设置过滤
  if (showOnlyRelated.value) {
    result = result.filter(item => item.relevanceScore > 0);
  }
  
  return result;
})

// 表单验证规则
const rules = {
  keyword: [
    { required: true, message: '请输入症状关键词', trigger: 'blur' },
    { min: 2, max: 10, message: '长度在 2 到 10 个字符', trigger: 'blur' }
  ],
  departmentId: [
    { required: true, message: '请选择所属科室', trigger: 'change' }
  ]
}

// 方法
const fetchSymptoms = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value
    }

    // 添加搜索参数
    if (searchForm.value.keyword) {
      params.keyword = searchForm.value.keyword
    }
    if (searchForm.value.departmentId) {
      params.departmentId = searchForm.value.departmentId
    }

    const response = await getSymptomList(params)
    if (response.code === 200) {
      const symptoms = response.data.records
      
      // 获取每个症状的关联专长
      const symptomsWithExpertises = await Promise.all(symptoms.map(async symptom => {
        try {
          const expertiseResponse = await getSymptomExpertises(symptom.symptomId)
          console.log(`症状ID=${symptom.symptomId}的关联专长响应:`, expertiseResponse)
          
          if (expertiseResponse.code === 200 && expertiseResponse.data) {
            // 获取专长列表并按相关度排序
            let relatedExpertises = Array.isArray(expertiseResponse.data) ? 
              expertiseResponse.data : (expertiseResponse.data.records || [])
            
            console.log(`症状${symptom.symptomId}的关联专长列表:`, relatedExpertises)
            
            // 处理专长数据，转换字段名，同时支持snake_case和camelCase两种命名风格
            relatedExpertises = relatedExpertises.map(expertise => {
              // 调试每个专长对象
              console.log('专长对象:', expertise)
              return {
                expertiseId: expertise.expertiseId || expertise.expertise_id,
                expertiseName: expertise.expertiseName || expertise.expertise_name,
                relevanceScore: expertise.relevanceScore !== undefined ? expertise.relevanceScore : 
                                (expertise.relevance_score !== undefined ? expertise.relevance_score : 0),
                departmentId: expertise.departmentId || expertise.department_id,
                departmentName: expertise.departmentName || expertise.department_name,
                // 保留原始字段
                expertise_id: expertise.expertise_id,
                expertise_name: expertise.expertise_name,
                relevance_score: expertise.relevance_score,
                department_id: expertise.department_id,
                department_name: expertise.department_name
              }
            })
            
            // 按相关度排序
            relatedExpertises = relatedExpertises.sort((a, b) => {
              const scoreA = a.relevanceScore !== undefined ? a.relevanceScore : 
                            (a.relevance_score !== undefined ? a.relevance_score : 0)
              const scoreB = b.relevanceScore !== undefined ? b.relevanceScore : 
                            (b.relevance_score !== undefined ? b.relevance_score : 0)
              return scoreB - scoreA
            })
            
            return {
              ...symptom,
              relatedExpertises: relatedExpertises
            }
          }
          return {
            ...symptom,
            relatedExpertises: []
          }
        } catch (error) {
          console.error(`获取症状${symptom.symptomId}的关联专长失败:`, error)
          return {
            ...symptom,
            relatedExpertises: []
          }
        }
      }))
      
      symptomList.value = symptomsWithExpertises
      total.value = response.data.total
    }
  } catch (error) {
    console.error('获取症状列表失败:', error)
    ElMessage.error('获取症状列表失败')
  } finally {
    loading.value = false
  }
}

const fetchDepartments = async () => {
  try {
    const response = await getDepartments({ size: 100 })
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

const handleSearch = () => {
  currentPage.value = 1
  fetchSymptoms()
}

const resetSearch = () => {
  searchForm.value = {
    keyword: '',
    departmentId: ''
  }
  handleSearch()
}

const handleSizeChange = (val) => {
  pageSize.value = val
  fetchSymptoms()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchSymptoms()
}

const handleAdd = () => {
  dialogType.value = 'add'
  symptomForm.keyword = ''
  symptomForm.departmentId = null
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogType.value = 'edit'
  currentSymptomId.value = row.symptomId
  symptomForm.keyword = row.keyword
  symptomForm.departmentId = row.departmentId
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      '确认删除该症状吗？删除后将同时解除与专长的关联关系！',
      '警告',
      {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
      }
    )
    
    const response = await deleteSymptom(row.symptomId)
    if (response.code === 200) {
      ElMessage.success('删除成功')
      if (symptomList.value.length === 1 && currentPage.value > 1) {
        currentPage.value--
      }
      fetchSymptoms()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  }
}

const submitForm = async () => {
  if (!symptomFormRef.value) return
  
  await symptomFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const submitData = {
          keyword: symptomForm.keyword,
          departmentId: symptomForm.departmentId
        }
        
        let response
        if (dialogType.value === 'add') {
          response = await addSymptom(submitData)
        } else {
          response = await updateSymptom(currentSymptomId.value, submitData)
        }
        
        if (response.code === 200) {
          ElMessage.success(dialogType.value === 'add' ? '添加成功' : '更新成功')
          dialogVisible.value = false
          fetchSymptoms()
        }
      } catch (error) {
        console.error(dialogType.value === 'add' ? '添加失败:' : '更新失败:', error)
        ElMessage.error(error.response?.data?.message || (dialogType.value === 'add' ? '添加失败' : '更新失败'))
      }
    }
  })
}

// 处理表格选择
const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

// 管理专长关联
const handleExpertiseRelation = async (row) => {
  selectedSymptom.value = row
  relationDialogVisible.value = true
  relationLoading.value = true
  showOnlyRelated.value = true
  
  try {
    // 获取所有专长
    const expertiseResponse = await getExpertiseList({ size: 1000 })
    if (expertiseResponse.code === 200) {
      const symptomId = selectedSymptom.value.symptomId
      
      // 只保留同科室的专长
      expertiseList.value = expertiseResponse.data.records
        .filter(expertise => expertise.departmentId === selectedSymptom.value.departmentId)
        .map(expertise => ({
          ...expertise,
          relevanceScore: 0,
          isEditing: false
        }))

      if (expertiseList.value.length === 0) {
        ElMessage.warning('当前科室下没有可关联的专长')
        relationDialogVisible.value = false
        return
      }
      
      // 获取专长关联的症状
      const promises = expertiseList.value.map(async expertise => {
        try {
          const relResponse = await getExpertiseSymptoms(expertise.expertiseId)
          if (relResponse.code === 200 && relResponse.data) {
            // 查找当前症状在专长关联中的相关度
            const relatedSymptom = relResponse.data.find(item => item.symptomId === symptomId)
            if (relatedSymptom) {
              expertise.relevanceScore = relatedSymptom.relevanceScore
            }
          }
          return expertise
        } catch (error) {
          console.error(`获取专长${expertise.expertiseId}关联症状失败:`, error)
          return expertise
        }
      })
      
      await Promise.all(promises)
      
      // 显示关联状态信息
      const relatedCount = expertiseList.value.filter(e => e.relevanceScore > 0).length
      if (relatedCount > 0) {
        ElMessage.info(`症状 "${row.keyword}" 已关联 ${relatedCount} 个专长`);
      } else {
        ElMessage.info(`症状 "${row.keyword}" 尚未关联任何专长`);
      }
    }
  } catch (error) {
    console.error('获取专长列表失败:', error)
    ElMessage.error('获取专长列表失败')
  } finally {
    relationLoading.value = false
  }
}

const filterExpertiseByDepartment = () => {
  // 使用计算属性自动过滤
}

const filterExpertise = () => {
  // 使用计算属性自动过滤
}

const formatTooltip = value => {
  return `${(value * 100).toFixed(0)}%`
}

const saveRelations = async () => {
  relationLoading.value = true
  
  try {
    // 获取相关度大于0的专长
    const relevantExpertises = expertiseList.value.filter(expertise => expertise.relevanceScore > 0)
    
    // 顺序处理每个专长的关联更新，避免并发请求
    for (const expertise of relevantExpertises) {
      try {
        // 先获取该专长已关联的症状
        const relResponse = await getExpertiseSymptoms(expertise.expertiseId)
        let relatedSymptoms = []
        
        if (relResponse.code === 200 && relResponse.data) {
          // 过滤掉当前症状
          relatedSymptoms = relResponse.data.filter(item => item.symptomId !== selectedSymptom.value.symptomId)
        }
        
        // 添加当前症状的新关联
        relatedSymptoms.push({
          symptomId: selectedSymptom.value.symptomId,
          keyword: selectedSymptom.value.keyword,
          departmentId: selectedSymptom.value.departmentId,
          departmentName: selectedSymptom.value.departmentName,
          relevanceScore: expertise.relevanceScore
        })
        
        // 提交更新
        const symptomIds = relatedSymptoms.map(s => s.symptomId)
        const relevanceScores = relatedSymptoms.map(s => s.relevanceScore)
        
        await updateExpertiseSymptoms(expertise.expertiseId, {
          symptomIds,
          relevanceScores
        })
      } catch (error) {
        console.error(`更新专长${expertise.expertiseId}关联症状失败:`, error)
        throw error
      }
    }
    
    ElMessage.success('关联更新成功')
    relationDialogVisible.value = false
    // 刷新症状列表以显示最新的关联状态
    await fetchSymptoms()
  } catch (error) {
    console.error('保存关联失败:', error)
    ElMessage.error('保存关联失败: ' + (error.response?.data?.message || error.message || '未知错误'))
  } finally {
    relationLoading.value = false
  }
}

// 加载症状关联的专长
const loadSymptomExpertises = async (symptomId) => {
  relationLoading.value = true
  try {
    const res = await getSymptomExpertises(symptomId)
    if (res.code === 200 && res.data) {
      // 提取专长ID列表
      const expertiseIds = res.data.map(item => item.expertiseId)
      selectedExpertises.value = expertiseIds
    }
  } catch (error) {
    console.error('获取症状关联专长失败', error)
    ElMessage.error('获取症状关联专长失败')
  } finally {
    relationLoading.value = false
  }
}

// 批量删除
const handleBatchDelete = async () => {
  if (selectedRows.value.length === 0) return
  
  try {
    await ElMessageBox.confirm(
      `确认删除选中的 ${selectedRows.value.length} 个症状吗？删除后将同时解除与专长的关联关系！`,
      '警告',
      {
        type: 'warning',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }
    )
    
    const ids = selectedRows.value.map(row => row.symptomId)
    await batchDeleteSymptoms(ids)
    ElMessage.success('批量删除成功')
    
    if (symptomList.value.length === selectedRows.value.length && currentPage.value > 1) {
      currentPage.value--
    }
    fetchSymptoms()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败')
    }
  }
}

// 切换排序方向
const toggleSortOrder = () => {
  searchForm.sortOrder = searchForm.sortOrder === 'asc' ? 'desc' : 'asc'
  handleSearch()
}

// 开始添加专长关联
const startAddExpertiseRelation = (row) => {
  row.isEditing = true;
  row.relevanceScore = 0.5; // 设置默认相关度为 50%
};

// 保存单个专长关联
const saveExpertiseRelation = async (row) => {
  try {
    // 验证相关度值是否在有效范围内
    const validatedRelevanceScore = Math.max(0, Math.min(1, row.relevanceScore));
    if (validatedRelevanceScore !== row.relevanceScore) {
      ElMessage.warning('相关度已自动调整到0-1之间');
      row.relevanceScore = validatedRelevanceScore;
    }

    // 先获取该专长已关联的症状
    const relResponse = await getExpertiseSymptoms(row.expertiseId)
    let relatedSymptoms = []
    
    if (relResponse.code === 200 && relResponse.data) {
      // 过滤掉当前症状
      relatedSymptoms = relResponse.data.filter(item => item.symptomId !== selectedSymptom.value.symptomId)
    }
    
    // 添加当前症状的新关联
    relatedSymptoms.push({
      symptomId: selectedSymptom.value.symptomId,
      keyword: selectedSymptom.value.keyword,
      departmentId: selectedSymptom.value.departmentId,
      departmentName: selectedSymptom.value.departmentName,
      relevanceScore: validatedRelevanceScore
    })
    
    // 提交更新
    const symptomIds = relatedSymptoms.map(s => s.symptomId)
    const relevanceScores = relatedSymptoms.map(s => s.relevanceScore)
    
    await updateExpertiseSymptoms(row.expertiseId, {
      symptomIds,
      relevanceScores
    })
    
    // 更新本地状态
    row.isEditing = false;
    
    ElMessage.success('添加关联成功');
  } catch (error) {
    console.error('添加关联失败:', error);
    ElMessage.error('添加关联失败: ' + (error.response?.data?.message || error.message || '未知错误'));
  }
};

// 删除专长关联
const removeExpertiseRelation = async (row) => {
  try {
    // 先获取该专长已关联的症状
    const relResponse = await getExpertiseSymptoms(row.expertiseId)
    let relatedSymptoms = []
    
    if (relResponse.code === 200 && relResponse.data) {
      // 过滤掉当前症状
      relatedSymptoms = relResponse.data.filter(item => item.symptomId !== selectedSymptom.value.symptomId)
    }
    
    // 提交更新
    const symptomIds = relatedSymptoms.map(s => s.symptomId)
    const relevanceScores = relatedSymptoms.map(s => s.relevanceScore)
    
    await updateExpertiseSymptoms(row.expertiseId, {
      symptomIds,
      relevanceScores
    })
    
    // 更新本地状态
    row.relevanceScore = 0;
    
    ElMessage.success('删除关联成功');
  } catch (error) {
    console.error('删除关联失败:', error);
    ElMessage.error('删除关联失败: ' + (error.response?.data?.message || error.message || '未知错误'));
  }
};

// 切换专长显示
const toggleRelatedExpertises = () => {
  showOnlyRelated.value = !showOnlyRelated.value;
  
  if (showOnlyRelated.value) {
    const relatedCount = expertiseList.value.filter(e => e.relevanceScore > 0).length;
    ElMessage.info(`显示 ${relatedCount} 个已关联专长`);
  } else {
    ElMessage.info('显示全部专长');
  }
};

// 获取科室名称的方法
const getDepartmentName = (departmentId) => {
  const department = departments.value.find(dept => dept.id === departmentId)
  return department ? department.name : departmentId
}

// 生命周期
onMounted(() => {
  fetchSymptoms()
  fetchDepartments()
})
</script>

<style scoped>
.symptom-management {
  padding: 12px;
  min-width: 1000px;
  max-width: 1400px;
  margin: 0 auto;
  background-color: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;

  .header-left {
    display: flex;
    align-items: center;
    gap: 12px;

    h2 {
      font-size: 20px;
      font-weight: 500;
      color: #1f2f3d;
      margin: 0;
    }
  }
  
  .header-buttons {
    display: flex;
    gap: 8px;
  }
}

.search-area {
  margin-bottom: 20px;
  padding: 20px;
  background-color: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.search-form {
  display: flex;
  flex-wrap: nowrap;
  align-items: center;
  gap: 8px;
}

.search-input :deep(.el-input__wrapper) {
  height: 32px;
  line-height: 32px;
}

.search-input :deep(.el-input__inner) {
  height: 32px;
  line-height: 32px;
}

:deep(.el-form-item) {
  margin-bottom: 0;
  margin-right: 10px;
}

:deep(.el-form-item:last-child) {
  margin-right: 0;
}

:deep(.el-select) {
  width: 150px;
}

.search-buttons {
  display: flex;
  gap: 4px;
  align-items: center;
}

.search-buttons :deep(.el-button) {
  padding: 4px 8px;
  height: 32px;
  font-size: 12px;
}

.search-buttons :deep(.el-icon) {
  margin-right: 4px;
}

.pagination {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
  padding-right: 16px;
}

.relation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  
  h3 {
    font-size: 16px;
    font-weight: normal;
    margin: 0;
    color: #303133;
  }
}

.search-box {
  display: flex;
  gap: 12px;
}

.expertise-search {
  margin-bottom: 16px;
}

.relation-content {
  margin-bottom: 16px;
}

.expertise-list {
  width: 100%;
  height: 100px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background-color: #fff;
}

.expertise-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  border-bottom: 1px solid #ebeef5;
  
  &:last-child {
    border-bottom: none;
  }
  
  &:hover {
    background-color: #f5f7fa;
  }
}

.expertise-name {
  color: #606266;
  font-size: 14px;
  flex: 1;
  margin-right: 12px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.expertise-score {
  color: #409eff;
  font-weight: 500;
  font-size: 14px;
  min-width: 45px;
  text-align: right;
}

.no-data {
  color: #909399;
  font-size: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100px;
}

:deep(.el-scrollbar__wrap) {
  overflow-x: hidden;
}

:deep(.el-table) {
  .cell {
    padding: 8px;
  }
}

.button-group {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.button-row {
  display: flex;
  gap: 4px;
  justify-content: center;
}

.search-box {
  display: flex;
  gap: 12px;
}

.expertise-name-with-tag {
  display: flex;
  align-items: center;
  gap: 8px;
}

.expertise-name-with-tag :deep(.el-tag) {
  margin: 0;
  height: 20px;
  padding: 0 6px;
  font-size: 12px;
}

.relation-btn {
  min-width: 90px;
  border-radius: 4px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.add-relation {
  background-color: #409EFF;
  border-color: #409EFF;
}

.add-relation:hover {
  background-color: #66b1ff;
  border-color: #66b1ff;
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.remove-relation {
  background-color: #F56C6C;
  border-color: #F56C6C;
}

.remove-relation:hover {
  background-color: #f78989;
  border-color: #f78989;
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.editing-container {
  display: flex;
  align-items: center;
  gap: 8px;
}

.editing-container .el-slider {
  flex: 1;
}
</style> 