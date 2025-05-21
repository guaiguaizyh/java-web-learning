<template>
  <div class="expertise-management">
    <div class="header">
      <div class="header-left">
        <h2 class="title">专长管理</h2>
        <el-tag v-if="selectedRows.length > 0" type="info">
          已选择 {{ selectedRows.length }} 项
        </el-tag>
        <el-tag v-else type="info" effect="plain">
          选择专长以进行批量操作
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
        <el-button type="primary" @click="showAddDialog">
          <el-icon><Plus /></el-icon>
          添加专长
        </el-button>
      </div>
    </div>

    <!-- 搜索区域 -->
    <div class="search-area">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="专长名称" style="margin-right: 10px;">
          <el-input
            v-model="searchForm.expertiseName"
            placeholder="请输入专长名称"
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
              v-for="dept in departmentOptions"
              :key="dept.id"
              :label="dept.name"
              :value="dept.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="排序字段" style="margin-right: 10px;">
          <el-select
            v-model="searchForm.sortField"
            placeholder="排序方式"
            style="width: 150px;"
            @change="handleSearch"
          >
            <el-option
              v-for="option in sortOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item>
          <div class="search-buttons">
            <el-button 
              :type="searchForm.sortOrder === 'asc' ? 'primary' : 'default'"
              @click="toggleSortOrder"
              size="small"
            >
              <el-icon><Sort /></el-icon>
              {{ searchForm.sortOrder === 'asc' ? '升序' : '降序' }}
            </el-button>
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

    <!-- 专长列表 -->
    <el-table
      v-loading="loading"
      :data="filteredExpertiseList"
      border
      style="width: 100%"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column prop="expertiseId" label="ID" width="80" v-if="false" />
      <el-table-column prop="expertiseName" label="专长名称" min-width="100" />
      <el-table-column label="所属科室" width="150">
        <template #default="{ row }">
          {{ getDepartmentName(row.departmentId) }}
        </template>
      </el-table-column>
      <el-table-column prop="doctorCount" label="医生数量" width="100" align="center">
        <template #default="{ row }">
          {{ row.doctorCount || 0 }}
        </template>
      </el-table-column>
      <el-table-column prop="symptomCount" label="症状数量" width="100" align="center">
        <template #default="{ row }">
          {{ row.symptomCount || 0 }}
        </template>
      </el-table-column>
      <el-table-column label="关联症状" width="500">
        <template #default="{ row }">
          <div class="symptom-list" v-if="row.symptoms && row.symptoms.length > 0">
            <el-scrollbar height="100px">
              <div 
                v-for="symptom in row.symptoms" 
                :key="symptom.symptomId || symptom.symptom_id" 
                class="symptom-item"
              >
                <span class="symptom-name">{{ symptom.keyword || symptom.symptom_name }}</span>
                <span class="symptom-score">{{ 
                  typeof symptom.relevanceScore === 'number' ? 
                    (symptom.relevanceScore * 100).toFixed(0) + '%' : 
                    (typeof symptom.relevance_score === 'number' ? 
                      (symptom.relevance_score * 100).toFixed(0) + '%' : '未设置') 
                }}</span>
              </div>
            </el-scrollbar>
          </div>
          <span v-else class="no-data">暂无关联症状</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <div class="button-group">
            <div class="button-row">
              <el-button
                type="primary"
                size="small"
                @click="handleEdit(row)"
              >
                编辑
              </el-button>
              <el-button
                type="danger"
                size="small"
                @click="handleDelete(row)"
              >
                删除
              </el-button>
            </div>
            <div class="button-row">
              <el-button
                type="success"
                size="small"
                @click="handleSymptomRelation(row)"
              >
                症状
              </el-button>
              <el-button
                type="warning"
                size="small"
                @click="handleDoctorRelation(row)"
              >
                医生
              </el-button>
            </div>
          </div>
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

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '添加专长' : '编辑专长'"
      width="500px"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="专长名称" prop="name">
          <el-input 
            v-model="form.name" 
            placeholder="请输入专长名称"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="所属科室" prop="departmentId">
          <el-tooltip
            v-if="form.doctorCount > 0"
            content="该专长已关联医生，无法修改所属科室"
            placement="top"
          >
            <div style="width: 100%">
          <el-select 
            v-model="form.departmentId" 
                placeholder="请选择所属科室"
            style="width: 100%"
                disabled
              >
                <el-option
                  v-for="dept in departments"
                  :key="dept.id"
                  :label="dept.name"
                  :value="dept.id"
                />
              </el-select>
            </div>
          </el-tooltip>
          <el-select
            v-else
            v-model="form.departmentId"
            placeholder="请选择所属科室"
            style="width: 100%"
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
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 症状关联对话框 -->
    <el-dialog
      v-model="symptomDialogVisible"
      title="症状关联管理"
      width="800px"
    >
      <div v-loading="relationLoading">
        <div class="relation-header">
          <h3>专长: {{ selectedExpertise ? selectedExpertise.expertiseName : '' }}</h3>
          <div class="search-box">
            <el-button
              type="primary"
              size="small"
              @click="toggleRelatedSymptoms"
            >
              {{ showOnlyRelatedSymptoms ? '显示全部症状' : '仅显示已关联' }}
            </el-button>
            <el-input
              v-model="symptomSearchKeyword"
              placeholder="搜索症状关键词"
              clearable
              style="width: 180px; margin-left: 10px;"
              @input="filterSymptoms"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-tooltip
              content="只能关联同一科室的症状"
              placement="top"
            >
              <el-select
                v-model="symptomSearchDepartment"
                placeholder="按科室筛选"
                clearable
                style="width: 180px; margin-left: 10px;"
                @change="filterSymptoms"
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

        <el-table
          :data="filteredAndDisplayedSymptoms"
          border
          max-height="400px"
          @selection-change="handleSymptomSelectionChange"
        >
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column prop="keyword" label="症状关键词" min-width="120">
            <template #default="{ row }">
              <div class="symptom-name-with-tag">
                <span>{{ row.keyword }}</span>
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
                  @click="startAddSymptomRelation(row)"
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
                    @click="saveSymptomRelation(row)"
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
                @click="removeSymptomRelation(row)"
              >
                删除关联
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="symptomDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveSymptomRelations">保存</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 医生关联对话框 -->
    <el-dialog
      v-model="doctorDialogVisible"
      title="医生关联管理"
      width="800px"
    >
      <div v-loading="relationLoading">
        <div class="relation-header">
          <h3>专长: {{ selectedExpertise ? selectedExpertise.expertiseName : '' }}</h3>
          <div class="search-box">
            <el-button
              type="primary"
              size="small"
              @click="toggleRelatedDoctors"
            >
              {{ showOnlyRelated ? '显示全部医生' : '仅显示已关联' }}
            </el-button>
            <el-input
              v-model="doctorSearchKeyword"
              placeholder="搜索医生姓名"
              clearable
              style="width: 180px; margin-left: 10px;"
              @input="filterDoctors"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-select
              v-model="doctorSearchDepartment"
              placeholder="按科室筛选"
              clearable
              style="width: 180px; margin-left: 10px;"
              @change="filterDoctors"
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

        <el-table
          :data="filteredAndDisplayedDoctors"
          border
          max-height="400px"
          @selection-change="handleDoctorSelectionChange"
        >
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column prop="name" label="医生姓名" min-width="120">
            <template #default="{ row }">
              <div class="doctor-name-with-tag">
                <span>{{ row.name }}</span>
                <el-tag v-if="row.masteryLevel > 0" size="small" type="success" effect="plain">已关联</el-tag>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="所属科室" min-width="120">
            <template #default="{ row }">
              {{ row.departmentName || getDepartmentName(row.departmentId) || '未设置' }}
            </template>
          </el-table-column>
          <el-table-column prop="positionsName" label="职称" min-width="100" />
          <el-table-column label="专长掌握度" width="260">
            <template #default="{ row }">
              <template v-if="row.masteryLevel === 0 && !row.isEditing">
                <el-button
                  type="primary"
                  size="small"
                  class="relation-btn add-relation"
                  @click="startAddRelation(row)"
                >
                  添加关联
                </el-button>
              </template>
              <template v-else-if="row.isEditing">
                <div class="editing-container">
              <el-slider
                v-model="row.masteryLevel"
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
                    @click="saveRelation(row)"
                  >
                    保存
                  </el-button>
                </div>
              </template>
              <template v-else>
                <el-slider
                  v-model="row.masteryLevel"
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
                v-if="row.masteryLevel > 0"
                type="danger"
                size="small"
                class="relation-btn remove-relation"
                @click="removeDoctorRelation(row)"
              >
                删除关联
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="doctorDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveDoctorRelations">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Delete, Refresh, Sort, CircleClose } from '@element-plus/icons-vue'
import { 
  getExpertiseList, 
  addExpertise, 
  updateExpertise, 
  deleteExpertise, 
  batchDeleteExpertise,
  getExpertiseSymptoms,
  updateExpertiseSymptoms,
  getExpertiseDoctors,
  updateExpertiseDoctors
} from '@/api/expertise'
import request from '@/utils/axios'
import { getSymptomList } from '@/api/symptom'
import { getDoctors as getDoctorList } from '@/api/doctors'
import { useRouter } from 'vue-router'

const loading = ref(false)
const expertiseList = ref([])
const departments = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const selectedRows = ref([])
const submitting = ref(false)

const dialogVisible = ref(false)
const dialogType = ref('add')
const formRef = ref(null)
const form = ref({
  id: '',
  name: '',
  departmentId: '',
  doctorCount: 0
})

// 搜索表单数据
const searchForm = ref({
  expertiseName: '',
  departmentId: '',
  sortField: '',
  sortOrder: 'asc'
})

// 排序选项
const sortOptions = [
  { label: '默认排序', value: '' },
  { label: '按症状数量排序', value: 'SYMPTOM_COUNT' },
  { label: '按医生数量排序', value: 'DOCTOR_COUNT' }
]

// 科室选项
const departmentOptions = ref([])

const rules = {
  name: [
    { required: true, message: '请输入专长名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  departmentId: [
    { required: true, message: '请选择所属科室', trigger: 'change' }
  ]
}

// 关联管理相关的响应式变量
const relationLoading = ref(false)
const selectedExpertise = ref(null)

// 症状关联相关
const symptomDialogVisible = ref(false)
const symptomList = ref([])
const symptomSearchKeyword = ref('')
const symptomSearchDepartment = ref('')
const selectedSymptoms = ref([])
const showOnlyRelatedSymptoms = ref(false)

// 医生关联相关
const doctorDialogVisible = ref(false)
const doctorList = ref([])
const doctorSearchKeyword = ref('')
const doctorSearchDepartment = ref('')
const selectedDoctors = ref([])
const showOnlyRelated = ref(false)

// 专长关联医生数量的缓存
const expertiseDoctorCountsCache = ref({})

// 添加 filteredExpertiseList 计算属性
const filteredExpertiseList = computed(() => {
  let result = expertiseList.value;
  
  // 按专长名称筛选
  if (searchForm.value.expertiseName) {
    const keyword = searchForm.value.expertiseName.toLowerCase();
    result = result.filter(item => 
      item.expertiseName.toLowerCase().includes(keyword)
    );
  }
  
  // 按科室筛选
  if (searchForm.value.departmentId) {
    result = result.filter(item => 
      item.departmentId === searchForm.value.departmentId
    );
  }
  
  // 按排序字段排序
  if (searchForm.value.sortField) {
    result = [...result].sort((a, b) => {
      const valueA = a[searchForm.value.sortField] || 0;
      const valueB = b[searchForm.value.sortField] || 0;
      return searchForm.value.sortOrder === 'asc' ? valueA - valueB : valueB - valueA;
    });
  }
  
  return result;
});

// 计算属性：过滤后的症状列表
const filteredSymptomList = computed(() => {
  let result = symptomList.value
  
  if (symptomSearchDepartment.value) {
    result = result.filter(item => item.departmentId === symptomSearchDepartment.value)
  }
  
  if (symptomSearchKeyword.value) {
    const keyword = symptomSearchKeyword.value.toLowerCase()
    result = result.filter(item => 
      item.keyword.toLowerCase().includes(keyword)
    )
  }
  
  return result
})

// 计算属性：过滤后的医生列表
const filteredDoctorList = computed(() => {
  let result = doctorList.value
  
  if (doctorSearchDepartment.value) {
    result = result.filter(item => item.departmentId === doctorSearchDepartment.value)
  }
  
  if (doctorSearchKeyword.value) {
    const keyword = doctorSearchKeyword.value.toLowerCase()
    result = result.filter(item => 
      item.name.toLowerCase().includes(keyword)
    )
  }
  
  return result
})

// 计算属性：过滤后的已关联医生列表
const filteredAndDisplayedDoctors = computed(() => {
  let result = doctorList.value;
  
  // 应用科室筛选
  if (doctorSearchDepartment.value) {
    result = result.filter(item => item.departmentId === doctorSearchDepartment.value);
  }
  
  // 应用关键词搜索
  if (doctorSearchKeyword.value) {
    const keyword = doctorSearchKeyword.value.toLowerCase();
    result = result.filter(item => 
      (item.name && item.name.toLowerCase().includes(keyword))
    );
  }
  
  // 根据显示设置过滤
  if (showOnlyRelated.value) {
    result = result.filter(item => item.masteryLevel > 0);
  }
  
  return result;
})

// 计算属性：过滤后的已关联症状列表
const filteredAndDisplayedSymptoms = computed(() => {
  let result = symptomList.value;
  
  // 应用科室筛选
  if (symptomSearchDepartment.value) {
    result = result.filter(item => item.departmentId === symptomSearchDepartment.value);
  }
  
  // 应用关键词搜索
  if (symptomSearchKeyword.value) {
    const keyword = symptomSearchKeyword.value.toLowerCase();
    result = result.filter(item => 
      (item.keyword && item.keyword.toLowerCase().includes(keyword))
    );
  }
  
  // 根据显示设置过滤
  if (showOnlyRelatedSymptoms.value) {
    result = result.filter(item => item.relevanceScore > 0);
  }
  
  return result;
})

// 格式化工具提示
const formatTooltip = (value) => {
  return `${(value * 100).toFixed(0)}%`;
};

// 解析专长详情字符串，提取熟练度值
const parseProficiencyFromDetails = (details) => {
  if (!details) return 0.5;
  const match = details.match(/\((\d+)%\)/);
  if (match && match[1]) {
    return Number(match[1]) / 100;
  }
  return 0.5;
};

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
      // 同时更新departmentOptions，用于搜索框
      departmentOptions.value = [...departments.value]
    }
  } catch (error) {
    console.error('获取科室列表失败:', error)
    ElMessage.error('获取科室列表失败')
  }
}

// 获取专长列表
const fetchExpertiseList = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value
    }

    // 添加搜索参数
    if (searchForm.value.expertiseName) {
      params.expertiseName = searchForm.value.expertiseName
    }
    if (searchForm.value.departmentId) {
      params.departmentId = searchForm.value.departmentId
    }

    // 添加排序参数
    if (searchForm.value.sortField) {
      params.sortField = searchForm.value.sortField
      params.sortOrder = searchForm.value.sortOrder
    }

    // 获取专长列表
    const response = await request.get('/expertises', { params })
    if (response.code === 200) {
      console.log('原始专长列表数据:', response.data.records)
      
      // 获取每个专长的医生数量和关联症状
      const expertisePromises = response.data.records.map(async (expertise) => {
        try {
          // 获取医生数量
          const doctorResponse = await getExpertiseDoctors(expertise.expertiseId)
          let updatedExpertise = { ...expertise }
          
          if (doctorResponse.code === 200) {
            const doctors = Array.isArray(doctorResponse.data) ? doctorResponse.data : 
                          (doctorResponse.data.records || [])
            updatedExpertise.doctorCount = doctors.length
          }
          
          // 获取关联症状
          const symptomsResponse = await getExpertiseSymptoms(expertise.expertiseId)
          console.log(`专长ID=${expertise.expertiseId}的关联症状响应:`, symptomsResponse)
          if (symptomsResponse.code === 200 && symptomsResponse.data) {
            const symptoms = Array.isArray(symptomsResponse.data) ? symptomsResponse.data : 
                           (symptomsResponse.data.records || [])
            
            console.log(`专长ID=${expertise.expertiseId}的关联症状列表:`, symptoms)
            
            // 处理症状数据，转换字段名，支持snake_case和camelCase两种风格
            const processedSymptoms = symptoms.map(symptom => {
              // 打印每个症状对象，调试字段名
              console.log('症状对象:', symptom)
            return {
                symptomId: symptom.symptomId || symptom.symptom_id,
                keyword: symptom.keyword || symptom.symptom_name,
                relevanceScore: symptom.relevanceScore !== undefined ? symptom.relevanceScore : 
                                (symptom.relevance_score !== undefined ? symptom.relevance_score : 0),
                departmentId: symptom.departmentId || symptom.department_id,
                departmentName: symptom.departmentName || symptom.department_name,
                // 保留原始字段
                symptom_id: symptom.symptom_id,
                symptom_name: symptom.symptom_name,
                relevance_score: symptom.relevance_score,
                department_id: symptom.department_id,
                department_name: symptom.department_name
              }
            })
            
            // 按相关度排序
            const sortedSymptoms = [...processedSymptoms].sort((a, b) => {
              const scoreA = a.relevanceScore !== undefined ? a.relevanceScore : 
                            (a.relevance_score !== undefined ? a.relevance_score : 0)
              const scoreB = b.relevanceScore !== undefined ? b.relevanceScore : 
                            (b.relevance_score !== undefined ? b.relevance_score : 0)
              return scoreB - scoreA
            })
            
            updatedExpertise.symptomCount = symptoms.length
            updatedExpertise.symptomNames = sortedSymptoms.slice(0, 5)
              .map(s => s.keyword || s.symptom_name)
              .join(', ')
            updatedExpertise.symptoms = sortedSymptoms // 保存完整的症状数据
            
            console.log(`处理后的专长${expertise.expertiseId}关联症状:`, updatedExpertise.symptoms)
          }
          
          return updatedExpertise
        } catch (error) {
          console.error(`获取专长${expertise.expertiseId}的关联数据失败:`, error)
          return expertise
        }
      })

      // 等待所有请求完成
      expertiseList.value = await Promise.all(expertisePromises)
      
      console.log('处理后的专长列表数据:', expertiseList.value)
      total.value = response.data.total
    }
  } catch (error) {
    console.error('获取专长列表失败:', error)
    ElMessage.error('获取专长列表失败')
  } finally {
    loading.value = false
  }
}

// 显示添加对话框
const showAddDialog = async () => {
  dialogType.value = 'add'
  // 确保科室列表已加载
  if (departments.value.length === 0) {
    await fetchDepartments()
  }
  form.value = {
    id: '',
    name: '',
    departmentId: '',
    doctorCount: 0
  }
  dialogVisible.value = true
}

// 显示编辑对话框
const handleEdit = async (row) => {
  dialogType.value = 'edit'
  // 确保科室列表已加载
  if (departments.value.length === 0) {
    await fetchDepartments()
  }
  
  form.value = { 
    id: row.expertiseId,
    name: row.expertiseName,
    departmentId: row.departmentId,
    doctorCount: row.doctorCount || 0  // 添加医生数量信息
  }
  
  dialogVisible.value = true
}

// 处理提交
const handleSubmit = async () => {
  if (!formRef.value) return;
  
  try {
    const valid = await formRef.value.validate();
    if (!valid) return;

    submitting.value = true;
    
    const data = {
      expertiseName: form.value.name.trim()
    };

    // 只有在没有关联医生时才允许修改科室
    if (!form.value.doctorCount || form.value.doctorCount === 0) {
      data.departmentId = form.value.departmentId;
    }

    let response;
    if (dialogType.value === 'add') {
      response = await addExpertise(data);
        } else {
      response = await updateExpertise(form.value.id, data);
    }
    
    if (response.code === 200) {
      ElMessage.success(dialogType.value === 'add' ? '添加成功' : '更新成功');
      dialogVisible.value = false;
      fetchExpertiseList();
    }
      } catch (error) {
    console.error(dialogType.value === 'add' ? '添加专长失败:' : '更新专长失败:', error);
    ElMessage.error(error.response?.data?.message || (dialogType.value === 'add' ? '添加失败' : '更新失败'));
  } finally {
    submitting.value = false;
  }
};

// 单个删除处理
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除该专长吗？删除后将同时解除与医生和症状的关联关系。',
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    );

    const response = await deleteExpertise(row.expertiseId);
    
    if (response.code === 200) {
      ElMessage.success('删除成功');
      // 如果当前页只有一条数据，且不是第一页，则跳转到上一页
      if (expertiseList.value.length === 1 && currentPage.value > 1) {
        currentPage.value--;
      }
      fetchExpertiseList();
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error);
      ElMessage.error(error.response?.data?.message || '删除失败');
    }
  }
};

// 批量删除
const handleBatchDelete = async () => {
  if (selectedRows.value.length === 0) return;
  
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedRows.value.length} 个专长吗？删除后将同时解除与医生和症状的关联关系。`,
      '批量删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    );

    // 获取要删除的专长ID列表
    const ids = selectedRows.value.map(row => row.expertiseId);
    console.log('批量删除的专长IDs:', ids);
    
    // 显示加载中
    loading.value = true;
    
    // 逐个删除专长
    let successCount = 0;
    let failedExpertises = [];
    
    for (const expertise of selectedRows.value) {
      try {
        const response = await deleteExpertise(expertise.expertiseId);
        if (response.code === 200) {
          successCount++;
        } else {
          failedExpertises.push(expertise.expertiseName);
        }
      } catch (error) {
        console.error(`删除专长 ${expertise.expertiseName} 失败:`, error);
        failedExpertises.push(expertise.expertiseName);
      }
    }

    // 显示删除结果
    if (successCount === selectedRows.value.length) {
      ElMessage.success(`成功删除 ${successCount} 个专长`);
    } else if (successCount > 0) {
      ElMessage.warning(`部分删除成功：${successCount} 个成功，${failedExpertises.length} 个失败\n失败的专长：${failedExpertises.join('、')}`);
    } else {
      ElMessage.error(`删除失败：${failedExpertises.join('、')}`);
    }

    selectedRows.value = []; // 清空选择
    
    // 如果当前页删除完了，且不是第一页，则跳转到上一页
    if (expertiseList.value.length === ids.length && currentPage.value > 1) {
      currentPage.value--;
    }
    await fetchExpertiseList();
    
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error);
      ElMessage.error(error.response?.data?.message || '批量删除失败');
    }
  } finally {
    loading.value = false;
  }
};

// 分页处理
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchExpertiseList()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchExpertiseList()
}

// 搜索处理
const handleSearch = () => {
  currentPage.value = 1
  fetchExpertiseList()
}

// 重置搜索
const resetSearch = () => {
  searchForm.value = {
    expertiseName: '',
    departmentId: '',
    sortField: '',
    sortOrder: 'asc'
  }
  handleSearch()
}

// 切换排序方向
const toggleSortOrder = () => {
  searchForm.value.sortOrder = searchForm.value.sortOrder === 'asc' ? 'desc' : 'asc'
  handleSearch()
}

// 处理表格选择
const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

// 处理症状关联
const handleSymptomRelation = async (row) => {
  selectedExpertise.value = row
  symptomDialogVisible.value = true
  relationLoading.value = true
  showOnlyRelatedSymptoms.value = true
  
  try {
    // 获取所有症状，设置较大的size以确保获取所有数据
    const response = await getSymptomList({ 
      page: 1,
      size: 10000,
      departmentId: selectedExpertise.value.departmentId // 直接按科室ID筛选
    })
    if (response.code === 200) {
      console.log('所有症状列表:', response.data.records)
      console.log('当前专长科室ID:', selectedExpertise.value.departmentId)
      
      // 处理症状列表
      symptomList.value = response.data.records.map(symptom => ({
        ...symptom,
        symptomId: symptom.symptomId || symptom.symptom_id,  // 统一ID字段
        keyword: symptom.keyword || symptom.symptom_name,     // 统一名称字段
        relevanceScore: 0,
        isEditing: false
      }))

      console.log('处理后的症状列表:', symptomList.value)

      if (symptomList.value.length === 0) {
        ElMessage.warning('当前科室下没有可关联的症状')
        symptomDialogVisible.value = false
        return
      }
      
      // 获取已关联的症状
      const relResponse = await getExpertiseSymptoms(row.expertiseId)
      console.log('专长症状关联响应:', relResponse)
      if (relResponse.code === 200 && relResponse.data) {
        // 确保 relResponse.data 是数组
        const relatedSymptoms = Array.isArray(relResponse.data) ? relResponse.data : 
                              (relResponse.data.records || [])
        
        console.log('已关联的症状:', relatedSymptoms)
        
        // 更新已关联症状的相关度
        relatedSymptoms.forEach(relatedSymptom => {
          // 获取symptomId，兼容两种命名风格
          const symptomId = relatedSymptom.symptomId || relatedSymptom.symptom_id
          // 获取相关度，兼容两种命名风格
          const relevanceScore = relatedSymptom.relevanceScore !== undefined ? 
                                relatedSymptom.relevanceScore : 
                                (relatedSymptom.relevance_score !== undefined ? 
                                 relatedSymptom.relevance_score : 0)
          
          console.log('更新症状相关度:', { symptomId, relevanceScore })
          
          const symptom = symptomList.value.find(s => (s.symptomId || s.symptom_id) === symptomId)
          if (symptom) {
            symptom.relevanceScore = relevanceScore
            console.log('更新后的症状:', symptom)
          }
        })
        
        // 打印更新后的症状列表中相关度大于0的症状
        console.log('更新后的关联症状:', symptomList.value.filter(s => s.relevanceScore > 0))
        
        if (relatedSymptoms.length > 0) {
          ElMessage.info(`专长 "${row.expertiseName}" 已关联 ${relatedSymptoms.length} 个症状`)
        } else {
          ElMessage.info(`专长 "${row.expertiseName}" 尚未关联任何症状`)
        }
      }
    }
  } catch (error) {
    console.error('获取症状列表失败:', error)
    ElMessage.error('获取症状列表失败')
  } finally {
    relationLoading.value = false
  }
}

// 处理医生关联
const handleDoctorRelation = async (row) => {
  selectedExpertise.value = row;
  doctorDialogVisible.value = true;
  relationLoading.value = true;
  showOnlyRelated.value = true;
  selectedDoctors.value = [];
  
  try {
    // 获取所有医生
    const response = await getDoctorList({ size: 1000 });
    console.log('获取到的医生列表:', response);
    
    if (response.code === 200) {
      // 初始化医生列表，添加 isEditing 字段
      doctorList.value = response.data.records.map(doctor => ({
        ...doctor,
        doctorId: Number(doctor.doctorId),
        departmentId: Number(doctor.departmentId),
        departmentName: doctor.departmentName || getDepartmentName(Number(doctor.departmentId)),
        masteryLevel: 0,
        isEditing: false,
        isRelated: false,
        positionsName: doctor.positionsName || doctor.title || '未设置'
      }));
      
      // 获取已关联的医生
      const relResponse = await getExpertiseDoctors(row.expertiseId);
      console.log('专长医生关联响应:', relResponse);
      
      if (relResponse.code === 200 && relResponse.data) {
        const relatedDoctors = Array.isArray(relResponse.data) ? relResponse.data : 
                            (relResponse.data.records ? relResponse.data.records : []);
        
        if (relatedDoctors.length > 0) {
        relatedDoctors.forEach(relatedDoctor => {
            const doctor = doctorList.value.find(d => Number(d.doctorId) === Number(relatedDoctor.doctorId));
          if (doctor) {
              let proficiencyValue = null;
              
              // 首先尝试从 expertiseDetails 中解析熟练度
              if (relatedDoctor.expertiseDetails) {
                proficiencyValue = parseProficiencyFromDetails(relatedDoctor.expertiseDetails);
              }
              // 如果没有 expertiseDetails 或解析失败，尝试其他字段
              else if (relatedDoctor.proficiency !== undefined) {
                proficiencyValue = relatedDoctor.proficiency;
              } 
              else if (relatedDoctor.doctorExpertises && relatedDoctor.doctorExpertises.length > 0) {
                const expertise = relatedDoctor.doctorExpertises.find(e => e.expertiseId === row.expertiseId);
                if (expertise && expertise.proficiency !== undefined) {
                  proficiencyValue = expertise.proficiency;
                }
              }
              else if (relatedDoctor.expertiseProficiencyMap) {
                const expertiseIdStr = String(row.expertiseId);
                if (relatedDoctor.expertiseProficiencyMap[expertiseIdStr] !== undefined) {
                  proficiencyValue = relatedDoctor.expertiseProficiencyMap[expertiseIdStr];
                }
              }
              
              doctor.masteryLevel = proficiencyValue !== null && proficiencyValue !== undefined
                ? Number(proficiencyValue)
                : 0.5;
              doctor.isRelated = true;
              selectedDoctors.value.push(doctor);
            }
          });
          
          expertiseDoctorCountsCache.value[row.expertiseId] = relatedDoctors.length;
          ElMessage.info(`专长 "${row.expertiseName}" 已关联 ${relatedDoctors.length} 个医生`);
        } else {
          ElMessage.info(`专长 "${row.expertiseName}" 尚未关联任何医生`);
        }
      }
    }
  } catch (error) {
    console.error('获取医生列表失败:', error);
    ElMessage.error('获取医生列表失败: ' + (error.response?.data?.message || error.message || '未知错误'));
  } finally {
    relationLoading.value = false;
  }
};

// 处理症状选择变化
const handleSymptomSelectionChange = (rows) => {
  selectedSymptoms.value = rows
}

// 处理医生选择变化
const handleDoctorSelectionChange = (rows) => {
  selectedDoctors.value = rows
}

// 保存症状关联
const saveSymptomRelations = async () => {
  relationLoading.value = true
  try {
    // 获取相关度大于0的症状
    const relevantSymptoms = symptomList.value.filter(symptom => symptom.relevanceScore > 0)
    
    // 准备提交数据
    const submitData = {
      symptomIds: relevantSymptoms.map(s => s.symptomId),
      relevanceScores: relevantSymptoms.map(s => s.relevanceScore)
    }
    
    console.log('提交症状关联数据:', JSON.stringify(submitData), '专长ID:', selectedExpertise.value.expertiseId)
    
    // 提交更新
    await updateExpertiseSymptoms(selectedExpertise.value.expertiseId, submitData)
    ElMessage.success(`成功保存 ${relevantSymptoms.length} 个症状的关联`)
    symptomDialogVisible.value = false
    
    // 刷新列表，确保症状数量正确显示
    await fetchExpertiseList()
  } catch (error) {
    console.error('保存症状关联失败:', error)
    ElMessage.error('保存症状关联失败: ' + (error.response?.data?.message || error.message || '未知错误'))
  } finally {
    relationLoading.value = false
  }
}

// 保存医生关联
const saveDoctorRelations = async () => {
  relationLoading.value = true;
  try {
    // 获取掌握度大于0的医生
    const relevantDoctors = doctorList.value.filter(doctor => doctor.masteryLevel > 0);
    
    if (relevantDoctors.length === 0) {
      ElMessage.warning('没有选择任何医生或设置掌握度');
      return;
    }

    // 验证医生数据
    const invalidDoctors = relevantDoctors.filter(d => !d.doctorId);
    if (invalidDoctors.length > 0) {
      console.error('发现无效的医生数据:', invalidDoctors);
      ElMessage.error('存在无效的医生数据，请刷新页面重试');
      return;
    }

    // 验证医生是否属于同一科室
    const expertiseDepartmentId = selectedExpertise.value.departmentId;
    const wrongDepartmentDoctors = relevantDoctors.filter(d => d.departmentId !== expertiseDepartmentId);
    if (wrongDepartmentDoctors.length > 0) {
      const doctorNames = wrongDepartmentDoctors.map(d => d.name).join('、');
      ElMessage.error(`医生 ${doctorNames} 与专长不属于同一科室，无法添加关联`);
      return;
    }
    
    // 准备提交数据
    const doctors = relevantDoctors.map(d => ({
      doctorId: Number(d.doctorId),
      expertiseId: Number(selectedExpertise.value.expertiseId),
      proficiency: Number(Math.max(0, Math.min(1, d.masteryLevel))),
      expertiseDetails: `${selectedExpertise.value.expertiseName}(${Math.round(Math.max(0, Math.min(1, d.masteryLevel)) * 100)}%)`
    }));
    
    console.log('提交医生关联数据:', JSON.stringify(doctors), '专长ID:', selectedExpertise.value.expertiseId);
    
    // 提交更新
    await updateExpertiseDoctors(selectedExpertise.value.expertiseId, doctors);
    
    // 更新缓存
    expertiseDoctorCountsCache.value[selectedExpertise.value.expertiseId] = doctors.length;
    
    ElMessage.success(`成功保存 ${doctors.length} 个医生的专长关联`);
    doctorDialogVisible.value = false;
    
    // 刷新列表
    await fetchExpertiseList();
  } catch (error) {
    console.error('保存医生关联失败:', error);
    if (error.response?.data?.message?.includes('医生')) {
      // 如果错误消息中包含医生ID，尝试将ID替换为医生名字
      let errorMessage = error.response.data.message;
      doctorList.value.forEach(doctor => {
        const idPattern = new RegExp(doctor.doctorId.toString(), 'g');
        errorMessage = errorMessage.replace(idPattern, doctor.name);
      });
      ElMessage.error(errorMessage);
    } else if (error.response?.data?.message?.includes('foreign key constraint fails')) {
      const invalidDoctors = relevantDoctors.filter(d => {
        return !doctorList.value.some(listDoctor => listDoctor.doctorId === d.doctorId);
      });
      const doctorNames = invalidDoctors.map(d => d.name).join('、');
      ElMessage.error(`保存失败：医生 ${doctorNames || '数据'} 不存在或已被删除，请刷新页面重试`);
    } else {
      ElMessage.error('保存医生关联失败: ' + (error.response?.data?.message || error.message || '未知错误'));
    }
  } finally {
    relationLoading.value = false;
  }
};

// 处理症状过滤
const filterSymptoms = () => {
  // 使用计算属性自动过滤
  console.log('过滤症状列表:', {
    总症状数: symptomList.value.length,
    已关联症状数: symptomList.value.filter(s => s.relevanceScore > 0).length,
    搜索关键词: symptomSearchKeyword.value,
    选择科室: symptomSearchDepartment.value,
    仅显示已关联: showOnlyRelatedSymptoms.value
  });
}

// 处理医生过滤
const filterDoctors = () => {
  // 使用计算属性自动过滤
  console.log('过滤医生列表:', {
    总医生数: doctorList.value.length,
    已关联医生数: doctorList.value.filter(d => d.masteryLevel > 0).length,
    搜索关键词: doctorSearchKeyword.value,
    选择科室: doctorSearchDepartment.value,
    仅显示已关联: showOnlyRelated.value
  });
}

// 开始添加关联
const startAddRelation = (row) => {
  row.isEditing = true;
  row.masteryLevel = 0.5; // 设置默认掌握度为 50%
};

// 保存医生关联
const saveRelation = async (row) => {
  try {
    // 验证熟练度值是否在有效范围内
    const validatedMasteryLevel = Math.max(0, Math.min(1, row.masteryLevel));
    if (validatedMasteryLevel !== row.masteryLevel) {
      ElMessage.warning('熟练度已自动调整到0-1之间');
      row.masteryLevel = validatedMasteryLevel;
    }

    // 获取所有已关联的医生（包括当前正在添加的医生）
    const doctors = doctorList.value
      .filter(d => d.masteryLevel > 0 || d.doctorId === row.doctorId)
      .map(d => ({
        doctorId: d.doctorId,
        expertiseId: selectedExpertise.value.expertiseId,
        proficiency: d.doctorId === row.doctorId 
          ? Number(validatedMasteryLevel) 
          : Number(Math.max(0, Math.min(1, d.masteryLevel))),
        expertiseDetails: `${selectedExpertise.value.expertiseName}(${Math.round(validatedMasteryLevel * 100)}%)`
      }));

    console.log('Sending doctors data:', doctors);

    // 提交更新
    await updateExpertiseDoctors(selectedExpertise.value.expertiseId, doctors);
    
    // 更新本地状态
    row.isEditing = false;
    row.isRelated = true;
    
    // 更新缓存中的医生数量
    const relatedDoctors = doctorList.value.filter(d => d.masteryLevel > 0);
    expertiseDoctorCountsCache.value[selectedExpertise.value.expertiseId] = relatedDoctors.length;
    
    ElMessage.success('添加关联成功');
    
    // 刷新列表
    await fetchExpertiseList();
  } catch (error) {
    console.error('添加关联失败:', error);
    ElMessage.error('添加关联失败: ' + (error.response?.data?.message || error.message || '未知错误'));
  }
};

// 删除医生关联
const removeDoctorRelation = async (row) => {
  try {
    // 获取所有已关联的医生，排除要删除的医生
    const doctors = doctorList.value
      .filter(d => d.masteryLevel > 0 && d.doctorId !== row.doctorId)
      .map(d => ({
        doctorId: d.doctorId,
        expertiseId: selectedExpertise.value.expertiseId,
        proficiency: Number(Math.max(0, Math.min(1, d.masteryLevel)))
      }));

    // 提交更新
    await updateExpertiseDoctors(selectedExpertise.value.expertiseId, doctors);
    
    // 更新本地状态
    row.masteryLevel = 0;
    row.isRelated = false;
    
    // 更新缓存中的医生数量
    expertiseDoctorCountsCache.value[selectedExpertise.value.expertiseId] = doctors.length;
    
    ElMessage.success('删除关联成功');
    
    // 刷新列表
    await fetchExpertiseList();
  } catch (error) {
    console.error('删除关联失败:', error);
    ElMessage.error('删除关联失败: ' + (error.response?.data?.message || error.message || '未知错误'));
  }
};

// 切换医生显示
const toggleRelatedDoctors = () => {
  showOnlyRelated.value = !showOnlyRelated.value;
  
  if (showOnlyRelated.value) {
    const relatedCount = doctorList.value.filter(d => d.masteryLevel > 0).length;
    ElMessage.info(`显示 ${relatedCount} 个已关联医生`);
  } else {
    ElMessage.info('显示全部医生');
  }
  
  filterDoctors();
}

// 开始添加症状关联
const startAddSymptomRelation = (row) => {
  row.isEditing = true;
  row.relevanceScore = 0.5; // 设置默认相关度为 50%
};

// 保存单个症状关联
const saveSymptomRelation = async (row) => {
  try {
    // 验证相关度值是否在有效范围内
    const validatedRelevanceScore = Math.max(0, Math.min(1, row.relevanceScore));
    if (validatedRelevanceScore !== row.relevanceScore) {
      ElMessage.warning('相关度已自动调整到0-1之间');
      row.relevanceScore = validatedRelevanceScore;
    }

    // 获取所有具有相关度大于0的症状（包括当前正在添加的症状）
    const relevantSymptoms = symptomList.value.filter(s => s.relevanceScore > 0);
    
    // 准备提交数据
    const submitData = {
      symptomIds: relevantSymptoms.map(s => s.symptomId),
      relevanceScores: relevantSymptoms.map(s => s.relevanceScore)
    }
    
    // 提交更新
    await updateExpertiseSymptoms(selectedExpertise.value.expertiseId, submitData);
    
    // 更新本地状态
    row.isEditing = false;
    
    ElMessage.success('添加关联成功');
  } catch (error) {
    console.error('添加关联失败:', error);
    ElMessage.error('添加关联失败: ' + (error.response?.data?.message || error.message || '未知错误'));
  }
};

// 删除症状关联
const removeSymptomRelation = async (row) => {
  try {
    // 获取所有已关联的症状，排除要删除的症状
    const relevantSymptoms = symptomList.value.filter(s => {
      // 确保使用正确的 ID 进行比较
      const currentId = s.symptomId || s.symptom_id;
      const rowId = row.symptomId || row.symptom_id;
      return s.relevanceScore > 0 && currentId !== rowId;
    });
    
    // 准备提交数据
    const submitData = {
      symptomIds: relevantSymptoms.map(s => s.symptomId || s.symptom_id),
      relevanceScores: relevantSymptoms.map(s => s.relevanceScore)
    }
    
    console.log('删除症状关联提交数据:', submitData);
    
    // 提交更新
    await updateExpertiseSymptoms(selectedExpertise.value.expertiseId, submitData);
    
    // 更新本地状态
    row.relevanceScore = 0;
    
    ElMessage.success('删除关联成功');
  } catch (error) {
    console.error('删除关联失败:', error);
    ElMessage.error('删除关联失败: ' + (error.response?.data?.message || error.message || '未知错误'));
  }
};

// 切换症状显示
const toggleRelatedSymptoms = () => {
  showOnlyRelatedSymptoms.value = !showOnlyRelatedSymptoms.value;
  
  if (showOnlyRelatedSymptoms.value) {
    const relatedCount = symptomList.value.filter(s => s.relevanceScore > 0).length;
    ElMessage.info(`显示 ${relatedCount} 个已关联症状`);
  } else {
    ElMessage.info('显示全部症状');
  }
  
  filterSymptoms();
}

const router = useRouter()

onMounted(async () => {
  await fetchDepartments()
  await fetchExpertiseList()
})

// 初始加载数据，包括医生数量
const loadInitialData = async () => {
  try {
    await fetchExpertiseList() // 先获取专长列表
    
    // 获取所有专长的医生数量并初始化缓存
    const expertiseIds = expertiseList.value.map(expertise => expertise.expertiseId)
    
    // 为每个专长获取医生数量
    await Promise.all(expertiseIds.map(async (expertiseId) => {
      try {
        const response = await getExpertiseDoctors(expertiseId)
        if (response.code === 200 && response.data) {
          const doctorData = Array.isArray(response.data) 
            ? response.data 
            : (response.data.records || [])
          
          // 更新缓存
          expertiseDoctorCountsCache.value[expertiseId] = doctorData.length
        }
      } catch (error) {
        console.error(`获取专长${expertiseId}的医生数量失败:`, error)
      }
    }))
    
    // 更新专长列表中的医生数量
    expertiseList.value = expertiseList.value.map(item => ({
      ...item,
      doctorCount: expertiseDoctorCountsCache.value[item.expertiseId] || 0
    }))
    
    console.log('更新后的专长列表数据(含医生数量):', expertiseList.value)
  } catch (error) {
    console.error('初始化数据失败:', error)
    ElMessage.error('加载数据失败')
  }
}

// 获取科室名称的方法
const getDepartmentName = (departmentId) => {
  const department = departments.value.find(dept => dept.id === departmentId)
  return department ? department.name : departmentId
}
</script>

<style scoped>
.expertise-management {
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
  margin-bottom: 24px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;

  .title {
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

.operation-buttons {
  display: flex;
  gap: 8px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
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

:deep(.el-table) {
  .el-table__header th {
    background-color: #f5f7fa;
    color: #606266;
    font-weight: 500;
    height: 44px;
    padding: 8px 0;
  }
  
  .el-table__body td {
    padding: 8px 0;
  }

  .el-table__cell {
    .cell {
      padding: 0 12px;
    }
  }
}

.relation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  
  h3 {
    font-size: 16px;
    font-weight: 500;
    color: #303133;
    margin: 0;
  }
  
  .search-box {
    display: flex;
    gap: 12px;
  }
}

:deep(.el-slider__button) {
  border-color: #409eff;
  background-color: #409eff;
}

:deep(.el-slider__bar) {
  background-color: #409eff;
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

:deep(.el-button--small) {
  padding: 4px 12px;
  font-size: 12px;
  height: 24px;
  line-height: 1;
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

.add-relation-btn {
  background-color: #409eff;
  color: #fff;
  border-color: #409eff;
  width: 100px;
  text-align: center;
}

.doctor-name-with-tag {
  display: flex;
  align-items: center;
  gap: 8px;
}

.doctor-name-with-tag :deep(.el-tag) {
  margin: 0;
  height: 20px;
  padding: 0 6px;
  font-size: 12px;
}

.relation-tag {
  margin-left: 4px;
}

.actions-cell {
  display: flex;
  justify-content: center;
  align-items: center;
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

.relation-btn {
  min-width: 80px;
}

.symptom-list {
  width: 100%;
  height: 100px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background-color: #fff;
}

.symptom-item {
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

.symptom-name {
  color: #606266;
  font-size: 14px;
  flex: 1;
  margin-right: 12px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.symptom-score {
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
</style>