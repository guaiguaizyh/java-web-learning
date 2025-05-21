<template>
  <div class="doctor-container">
  <div class="doctor-management">
      <div class="header">
      <div class="header-left">
        <h2>医生管理</h2>
        <el-tag v-if="selectedRows.length === 0" type="info" effect="plain">
          选择医生以进行批量操作
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
        <el-button 
          type="warning" 
          @click="handleManagePositions"
        >
          <el-icon><EditPen /></el-icon>编辑职称
        </el-button>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>添加医生
        </el-button>
      </div>
    </div>

    <!-- 搜索区域 -->
    <div class="search-area">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <!-- 输入框区域 -->
          <el-form-item label="医生姓名" style="margin-right: 2px;">
          <el-input
            v-model="searchForm.name"
            placeholder="请输入医生姓名"
            clearable
            style="width: 180px"
            @keyup.enter="handleSearch"
            @clear="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-form-item>
          <el-form-item label="职称名称" style="margin-right: 2px;">
          <el-select
            v-model="searchForm.positionsId"
            placeholder="请选择职称"
            clearable
            style="width: 120px"
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
          <el-form-item label="性别" style="margin-right: 2px;">
          <el-select
            v-model="searchForm.gender"
            placeholder="选择性别"
            clearable
            style="width: 100px"
            @change="handleSearch"
          >
            <el-option label="男" value="男" />
            <el-option label="女" value="女" />
          </el-select>
        </el-form-item>
          <el-form-item label="科室名称" style="margin-right: 2px;">
          <el-select
            v-model="searchForm.departmentId"
            placeholder="请选择科室"
            clearable
            style="width: 120px"
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
        
        <!-- 排序控制区域 -->
          <el-form-item label="排序方式" style="margin-right: 2px;">
          <el-select
              v-model="searchForm.sortField"
            placeholder="排序方式"
            style="width: 120px"
            @change="handleSearch"
          >
            <el-option label="默认排序" value="" />
              <el-option label="按评分排序" value="averageRating" />
              <el-option label="按评价数排序" value="ratingCount" />
          </el-select>
        </el-form-item>
        
          <el-form-item style="margin-right: 2px;">
          <el-button 
            :type="searchForm.sortOrder === 'ASC' ? 'primary' : 'default'"
            @click="toggleSortOrder"
          >
            <el-icon><Sort /></el-icon>
            {{ searchForm.sortOrder === 'ASC' ? '升序' : '降序' }}
          </el-button>
        </el-form-item>

        <!-- 操作按钮区域 -->
          <el-form-item style="margin-right: 2px;">
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>搜索
          </el-button>
          </el-form-item>

          <el-form-item>
          <el-button @click="resetSearch">
            <el-icon><Refresh /></el-icon>重置
          </el-button>
        </el-form-item>

      </el-form>
    </div>

    <!-- 数据表格 -->
      <div class="table-container">
    <el-table
      v-loading="loading"
      :data="doctorList"
      border
      style="width: 100%"
      @selection-change="handleSelectionChange"
    >
          <el-table-column type="selection" width="55" fixed="left" />
          <el-table-column label="医生信息" width="180" fixed="left">
        <template #default="{ row }">
          <div class="doctor-info">
                <el-avatar :size="36" :src="row.avatarUrl">
              <el-icon><User /></el-icon>
            </el-avatar>
            <div class="info-content">
              <div class="doctor-name">{{ row.name }}</div>
            </div>
          </div>
        </template>
      </el-table-column>
          <el-table-column prop="positionsName" label="职称" width="100">
        <template #default="{ row }">
          <span>{{ row.positionsName || '暂无职称' }}</span>
        </template>
      </el-table-column>
          <el-table-column label="性别" width="60">
        <template #default="{ row }">
          <span>{{ row.gender || '未设置' }}</span>
        </template>
      </el-table-column>
          <el-table-column prop="departmentName" label="所属科室" width="100" />
          <el-table-column label="年龄" width="100">
        <template #default="{ row }">
              <div>{{ row.age }}</div>
        </template>
      </el-table-column>
          <el-table-column label="专长" min-width="200">
            <template #default="{ row }">
              <div class="expertise-tags">
                <template v-if="row.expertiseList && row.expertiseList.length">
                  <el-tag 
                    v-for="(expertise, index) in row.expertiseList" 
                    :key="index"
                    size="small"
                    class="expertise-tag"
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
                <span v-else>暂无专长</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="评分" width="180">
        <template #default="{ row }">
          <div class="rating-info">
            <el-rate
              v-model="row.averageRating"
              disabled
              show-score
              text-color="#ff9900"
            />
                <div class="rating-count">({{ row.ratingCount }}条评价)</div>
          </div>
        </template>
      </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <div class="operation-buttons">
            <div class="button-column">
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
            <div class="button-column">
              <el-button
                type="success"
                size="small"
                @click="handleManageExpertise(row)"
              >
                专长关联
              </el-button>
              <el-button
                type="info"
                size="small"
                @click="handleManageComments(row)"
              >
                评论管理
              </el-button>
            </div>
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
    </div>

    <!-- 职称管理对话框 -->
    <el-dialog
      v-model="positionsDialogVisible"
      title="职称管理"
      width="600px"
    >
      <div class="positions-dialog-content">
        <div class="positions-header">
          <h3>职称列表</h3>
          <el-button type="primary" @click="showAddPositionDialog">
            <el-icon><Plus /></el-icon>添加职称
          </el-button>
        </div>
        
        <el-table 
          :data="sortedPositions" 
          border 
          style="width: 100%; margin-top: 16px;"
          v-loading="positionsLoading"
        >
          <el-table-column prop="name" label="职称名称" min-width="180" />
          <el-table-column prop="doctorCount" label="关联医生数" width="120">
            <template #default="{ row }">
              <el-tag type="info" v-if="row.doctorCount > 0">{{ row.doctorCount }}人</el-tag>
              <span v-else>0人</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180" fixed="right">
            <template #default="{ row }">
              <div class="position-operations">
                <el-button
                  type="primary"
                  size="small"
                  @click="showEditPositionDialog(row)"
                >
                  编辑
                </el-button>
                <el-button
                  type="danger"
                  size="small"
                  @click="handleDeletePosition(row)"
                >
                  删除
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="positionsDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 添加职称对话框 -->
    <el-dialog
      v-model="addPositionDialogVisible"
      title="添加职称"
      width="400px"
    >
      <el-form 
        :model="positionForm" 
        ref="positionFormRef"
        :rules="positionRules"
        label-width="80px"
      >
        <el-form-item label="职称名称" prop="name">
          <el-input 
            v-model="positionForm.name" 
            placeholder="请输入职称名称"
            autofocus
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="addPositionDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitPositionForm">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 编辑职称对话框 -->
    <el-dialog
      v-model="editPositionDialogVisible"
      title="编辑职称"
      width="400px"
    >
      <el-form 
        :model="positionForm" 
        ref="positionFormRef"
        :rules="positionRules"
        label-width="80px"
      >
        <el-form-item label="职称名称" prop="name">
          <el-input 
            v-model="positionForm.name" 
            placeholder="请输入职称名称"
            autofocus
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editPositionDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitPositionForm">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '添加医生' : '编辑医生'"
      width="580px"
      :close-on-click-modal="false"
      class="doctor-dialog"
    >
      <el-form
        ref="doctorFormRef"
        :model="doctorForm"
        :rules="rules"
        label-width="80px"
        class="doctor-form"
      >
        <div class="form-section">
          <h3 class="section-title">基本信息</h3>
          <div class="form-content">
            <el-form-item label="姓名" prop="name">
              <el-input 
                v-model="doctorForm.name" 
                placeholder="请输入医生姓名"
                clearable
              />
            </el-form-item>
            <el-form-item label="性别" prop="gender">
              <el-radio-group v-model="doctorForm.gender" class="gender-group">
                <el-radio label="男">
                  <el-icon class="gender-icon"><Male /></el-icon>男
                </el-radio>
                <el-radio label="女">
                  <el-icon class="gender-icon"><Female /></el-icon>女
                </el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="年龄" prop="age">
              <el-input-number 
                v-model="doctorForm.age" 
                :min="18" 
                :max="100" 
                placeholder="请输入年龄"
                class="age-input"
                controls-position="right"
              />
            </el-form-item>
          </div>
        </div>

        <div class="form-section">
          <h3 class="section-title">职务信息</h3>
          <div class="form-content">
            <el-form-item label="职称" prop="positionsId">
              <el-select
                v-model="doctorForm.positionsId"
                placeholder="请选择职称"
                style="width: 100%"
                :loading="positionsLoading"
                clearable
              >
                <el-option
                  v-for="p in sortedPositionOptions"
                  :key="p.id"
                  :label="p.name"
                  :value="p.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="科室" prop="departmentId">
              <el-select
                v-model="doctorForm.departmentId"
                placeholder="请选择科室"
                style="width: 100%"
                clearable
              >
                <el-option
                  v-for="dept in departments"
                  :key="dept.id"
                  :label="dept.name"
                  :value="dept.id"
                />
              </el-select>
            </el-form-item>
          </div>
        </div>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 专长关联管理对话框 -->
    <el-dialog
      v-model="expertiseDialogVisible"
      title="管理医生专长"
      width="750px"
    >
      <div v-loading="relationLoading">
        <div class="relation-header">
          <h3>医生: {{ selectedDoctor ? selectedDoctor.name : '' }}</h3>
          <el-select
            v-model="selectedDepartmentId"
            placeholder="按科室筛选专长"
            clearable
            filterable
            style="width: 200px;"
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

          <el-table :data="filteredExpertiseList" border max-height="400px">
            <el-table-column type="selection" width="55" />
            <el-table-column prop="expertiseId" label="ID" width="80" />
            <el-table-column prop="expertiseName" label="专长名称" min-width="150" />
            <el-table-column prop="departmentName" label="所属科室" width="150" />
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

    <!-- 添加关联管理组件 -->
    <doctor-relations
      ref="doctorRelationsRef"
      :doctor-id="currentDoctorId"
      :doctor-name="currentDoctorName"
      @refresh="fetchDoctors"
    />

    <!-- 评论管理对话框 -->
    <el-dialog
      v-model="reviewDialogVisible"
      title="评论管理"
      width="900px"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <div class="review-management" v-loading="reviewLoading">
        <div class="review-header">
          <h3>医生: {{ currentDoctorName }}</h3>
        </div>

        <el-table :data="reviewList" border style="width: 100%; margin-top: 16px;" row-class-name="review-row" :row-class-name="getReviewRowClass">
          <el-table-column prop="username" label="用户名称" width="120">
            <template #default="{ row }">
              <div class="user-info">
                {{ row.username || '匿名用户' }}
                <el-tag v-if="row.isAnonymous" size="small" type="info" effect="plain" class="anonymous-tag">匿名</el-tag>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="评分" width="180">
            <template #default="{ row }">
              <div class="rating-display">
                <el-rate
                  v-model="row.rating"
                  disabled
                  show-score
                  text-color="#ff9900"
                />
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="comment" label="评论内容" min-width="300">
            <template #default="{ row }">
              <el-tooltip
                :content="row.comment"
                placement="top"
                :show-after="300"
                :enterable="false"
                :max-width="400"
              >
                <div class="comment-content">{{ row.comment }}</div>
              </el-tooltip>
            </template>
          </el-table-column>
          <el-table-column label="评论时间" width="180">
            <template #default="{ row }">
              {{ formatDateTime(row.createdAt) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button
                type="danger"
                size="small"
                @click="handleDeleteReview(row)"
              >
                删除评论
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination" style="margin-top: 16px;">
          <el-pagination
            v-model:current-page="reviewPagination.currentPage"
            v-model:page-size="reviewPagination.pageSize"
            :total="reviewPagination.total"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next"
            @size-change="handleReviewSizeChange"
            @current-change="handleReviewPageChange"
          />
        </div>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="reviewDialogVisible = false">关闭</el-button>
          <el-button type="primary" @click="fetchDoctorReviews(currentDoctorId)" :loading="reviewLoading">刷新数据</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive, computed, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, User, Star, OfficeBuilding, Delete, EditPen, Sort, Male, Female } from '@element-plus/icons-vue'
import request from '@/utils/axios'
import DoctorRelations from '@/components/DoctorRelations.vue'
import { getDoctorExpertises, updateDoctorExpertises } from '@/api/expertise'
import { getDoctorReviews, deleteReview } from '@/api/reviews'

// 数据
const router = useRouter()
const loading = ref(false)
const positionsLoading = ref(false)
const doctorList = ref([])
const departments = ref([])
const positions = ref([])
const expertiseList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const dialogVisible = ref(false)
const expertiseDialogVisible = ref(false)
const departmentDialogVisible = ref(false)
const relationLoading = ref(false)
const dialogType = ref('add')
const currentDoctorId = ref(null)
const currentDoctorName = ref('')
const selectedExpertises = ref([])
const selectedRows = ref([])
const selectedDoctor = ref(null)
const expertiseSearchKeyword = ref('')
const selectedDepartmentId = ref(null)
const selectedDepartments = ref([])
const allDepartments = ref([])
const positionsDialogVisible = ref(false)
const newPositionName = ref('')
const addPositionDialogVisible = ref(false)
const editPositionDialogVisible = ref(false)
const positionForm = reactive({
  id: '',
  name: ''
})
const positionFormRef = ref(null)
const positionRules = {
  name: [
    { required: true, message: '请输入职称名称', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ]
}

// 表单数据
const searchForm = reactive({
  name: '',
  departmentId: '',
  positionsId: '',
  gender: '',
  sortField: '',
  sortOrder: 'ASC'
})

const doctorForm = reactive({
  name: '',
  gender: '男',
  age: 30,
  positionsId: '',
  departmentId: '',
  avatarUrl: ''
})

const doctorFormRef = ref(null)

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入医生姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  gender: [
    { required: true, message: '请选择性别', trigger: 'change' }
  ],
  age: [
    { required: true, message: '请输入年龄', trigger: 'blur' },
    { type: 'number', min: 18, max: 100, message: '年龄必须在18-100岁之间', trigger: 'blur' }
  ],
  positionsId: [
    { required: true, message: '请选择职称', trigger: 'change' }
  ],
  departmentId: [
    { required: true, message: '请选择所属科室', trigger: 'change' }
  ]
}

// 医生关联相关
const doctorDialogVisible = ref(false)
const doctorSearchKeyword = ref('')
const doctorSearchDepartment = ref('')
const selectedDoctors = ref([])

// 评价管理相关
const reviewDialogVisible = ref(false)
const reviewLoading = ref(false)
const reviewList = ref([])
const averageRating = ref(0) // 添加averageRating响应式变量
const reviewPagination = ref({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 在评价管理相关部分下方添加
const addReviewDialogVisible = ref(false)
const addReviewFormRef = ref(null)
const addReviewForm = ref({
  rating: 5,
  content: '',
  isAnonymous: false
})


// 方法
const fetchDoctors = async () => {
  loading.value = true
  try {
    // 准备查询参数
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      name: searchForm.name || undefined,
      departmentId: searchForm.departmentId || undefined,
      positionsId: searchForm.positionsId || undefined,
      gender: searchForm.gender || undefined,
      sortField: searchForm.sortField || 'doctorId', // 当未选择排序字段时使用doctorId
      sortOrder: searchForm.sortOrder || 'ASC',
      includeExpertise: true // 确保包含专长信息
    }
    
    // 移除所有undefined的参数
    Object.keys(params).forEach(key => {
      if (params[key] === undefined) {
        delete params[key];
      }
    });
    
    console.log('查询参数:', params)
    
    const response = await request.get('/doctors', { params })
    if (response.code === 200) {
      console.log('获取医生列表成功:', response.data)
      
      doctorList.value = response.data.records.map(doctor => {
        // 处理专长数据
        let expertiseArray = [];
        if (doctor.expertiseList && Array.isArray(doctor.expertiseList)) {
          expertiseArray = doctor.expertiseList;
        } else if (doctor.expertiseListStr) {
          // 尝试从字符串解析专长列表
          const expertises = doctor.expertiseListStr.split(/[,，、]/).map(item => item.trim());
          // 去重
          const uniqueExpertises = [...new Set(expertises)];
          expertiseArray = uniqueExpertises.map(name => ({ name }));
        }
        
        return {
          id: doctor.doctorId,
          name: doctor.name,
          gender: doctor.gender,
          age: doctor.age,
          positionsName: doctor.positionsName,
          positionsId: doctor.positionsId,
          departmentId: doctor.departmentId,
          departmentName: doctor.departmentName,
          avatarUrl: doctor.avatarUrl,
          averageRating: doctor.averageRating || 0,
          ratingCount: doctor.ratingCount || 0,
          expertiseListStr: doctor.expertiseListStr || '',
          expertiseList: expertiseArray
        }
      });
      total.value = response.data.total
    }
  } catch (error) {
    console.error('获取医生列表失败:', error)
    ElMessage.error('获取医生列表失败')
  } finally {
    loading.value = false
  }
}

const fetchDepartments = async () => {
  try {
    // 尝试使用departments/list接口
    const response = await request({
      url: '/departments/list',
      method: 'get',
      params: {
        page: 1,
        size: 1000
      },
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
    
    if (response.code === 200) {
      departments.value = response.data.records.map(dept => ({
        id: dept.departmentId,
        name: dept.departmentName
      }))
      console.log('获取到的科室列表:', departments.value)
    } else {
      console.error('获取科室列表失败:', response)
      ElMessage.error(response.message || '获取科室列表失败')
    }
  } catch (error) {
    console.error('获取科室列表失败:', error)
    console.error('错误详情:', error.response?.data || error.message)
    if (error.response?.status === 403) {
      ElMessage.error('获取科室列表失败：没有访问权限, 请确认您已登录')
      // 直接调用应急方法
      const success = await fetchDepartmentsEmergency();
      if (success) {
        console.log('通过应急方法成功获取科室数据');
        return; // 成功获取数据，提前返回
      }
    }
    ElMessage.error('获取科室列表失败，请刷新重试或检查网络连接')
  }
}

const fetchPositions = async () => {
  positionsLoading.value = true;
  try {
    const response = await request.get('/positions')
    if (response.code === 200) {
      console.log('获取职称列表:', response.data);
      positions.value = response.data.map(p => ({
        id: p.positionsId,
        name: p.positionsName
      }))
      console.log('处理后的职称列表:', positions.value);
    }
  } catch (error) {
    console.error('获取职称列表失败:', error)
    ElMessage.error('获取职称列表失败')
  } finally {
    positionsLoading.value = false;
  }
}

const handleSearch = () => {
  currentPage.value = 1
  console.log('搜索条件:', searchForm)
  fetchDoctors()
}

const resetSearch = () => {
  // 完全重置所有字段，包括排序
  searchForm.name = ''
  searchForm.departmentId = ''
  searchForm.positionsId = ''
  searchForm.gender = ''
  searchForm.sortField = ''
  searchForm.sortOrder = 'ASC'
  
  handleSearch()
}

const handleSizeChange = (val) => {
  pageSize.value = val
  fetchDoctors()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchDoctors()
}

const handleAdd = async () => {
  dialogType.value = 'add'
  
  // 确保职称和科室数据已加载
  const loadDataPromises = [];
  
  if (positions.value.length === 0) {
    loadDataPromises.push(fetchPositions());
  }
  
  if (departments.value.length === 0) {
    loadDataPromises.push(fetchDepartments());
  }
  
  if (loadDataPromises.length > 0) {
    try {
      await Promise.all(loadDataPromises);
    } catch (error) {
      console.error('加载表单相关数据失败:', error);
      // 即使加载失败也继续，使用已有数据
    }
  }
  
  doctorForm.name = ''
  doctorForm.gender = '男'
  doctorForm.age = 18
  doctorForm.positionsId = ''
  doctorForm.departmentId = ''
  
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  dialogType.value = 'edit'
  currentDoctorId.value = row.id
  currentDoctorName.value = row.name
  
  if (positions.value.length === 0) {
    await fetchPositions()
  }
  
  console.log('编辑医生:', row);
  
  let positionsId = row.positionsId;
  
  if (!positionsId && row.positionsName) {
    const position = getPositionByName(row.positionsName);
    if (position) {
      positionsId = position.id;
      console.log('通过名称找到职称ID:', positionsId);
    } else {
      console.warn('未找到对应职称:', row.positionsName);
      console.log('可用职称列表:', positions.value);
    }
  }
  
  if (positionsId && !getPositionById(positionsId)) {
    console.warn('职称ID无效:', positionsId);
    positionsId = '';
  }
  
  Object.assign(doctorForm, {
    name: row.name,
    gender: row.gender,
    age: row.age,
    positionsId: positionsId || '',
    departmentId: row.departmentId
  })
  
  console.log('编辑表单数据:', doctorForm);
  
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    // 先检查医生是否有专长关联
    const expertiseResponse = await request.get(`/expertises/doctor/${row.id}`)
    // 如果有专长关联，直接阻止删除并提示用户先取消关联
    if (expertiseResponse.code === 200 && expertiseResponse.data && expertiseResponse.data.length > 0) {
      ElMessage.error(`删除失败: 医生 "${row.name}" 有专长关联，请先取消所有专长关联后再删除`)
      return
    }
    
    // 检查医生是否有评论
    let hasReviews = false
    let reviewCount = 0
    try {
      // 尝试获取医生评论数量
      const reviewResponse = await request.get(`/reviews/doctor/${row.id}`, {
        params: { page: 1, size: 1 }
      })
      if (reviewResponse.code === 200 && reviewResponse.data) {
        reviewCount = reviewResponse.data.total || 0
        hasReviews = reviewCount > 0
      }
    } catch (error) {
      console.error('获取医生评论失败:', error)
      // 如果获取评论失败，我们假设没有评论，继续流程
    }
    
    // 如果有评论，显示带有评论信息的确认对话框
    if (hasReviews) {
      await ElMessageBox.confirm(
        `医生 "${row.name}" 有 ${reviewCount} 条评论记录，删除医生将同时删除所有相关评论，是否继续？`,
        '警告',
        {
          type: 'warning',
          confirmButtonText: '确定删除',
          cancelButtonText: '取消'
        }
      )
    } else {
      // 没有评论时的普通确认对话框
    await ElMessageBox.confirm('确认删除该医生吗？此操作不可恢复！', '警告', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    }
    
    try {
      // 执行删除操作
    const response = await request.delete(`/doctors/${row.id}`)
    if (response.code === 200) {
      ElMessage.success('删除成功')
      if (doctorList.value.length === 1 && currentPage.value > 1) {
        currentPage.value--
      }
      fetchDoctors()
    }
  } catch (error) {
      console.error('删除失败:', error)
      if (error.response?.data?.message) {
        ElMessage.error(`删除失败: ${error.response.data.message}`)
      } else {
      ElMessage.error('删除失败')
      }
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除操作被取消:', error)
    }
  }
}

const submitForm = async () => {
  if (!doctorFormRef.value) return
  
  await doctorFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        let response
        const submitData = {
          name: doctorForm.name,
          gender: doctorForm.gender,
          age: doctorForm.age,
          positionsId: doctorForm.positionsId,
          departmentId: doctorForm.departmentId
        }
        
        console.log('提交的医生数据:', submitData)
        
        if (dialogType.value === 'add') {
          response = await request.post('/doctors', submitData)
        } else {
          response = await request.put(`/doctors/${currentDoctorId.value}`, submitData)
        }
        
        if (response.code === 200) {
          ElMessage.success(dialogType.value === 'add' ? '添加成功' : '更新成功')
          dialogVisible.value = false
          fetchDoctors()
        }
      } catch (error) {
        console.error(dialogType.value === 'add' ? '添加失败:' : '更新失败:', error)
        ElMessage.error(error.response?.data?.message || (dialogType.value === 'add' ? '添加失败' : '更新失败'))
      }
    }
  })
}

const handleExpertise = async (row) => {
  currentDoctorId.value = row.id
  try {
    const res = await getDoctorExpertises(row.id)
    if (res.code === 200) {
      // 适配返回数据结构
      selectedExpertises.value = res.data.map(item => item.expertiseId)
      expertiseDialogVisible.value = true
    }
  } catch (error) {
    ElMessage.error('获取专长列表失败')
  }
}

const submitExpertises = async () => {
  try {
    // 构建包含熟练度的数据结构
    const expertiseData = selectedExpertises.value.map(id => ({
      expertiseId: id,
      proficiency: 0.5  // 使用默认熟练度
    }))
    
    const res = await updateDoctorExpertises(currentDoctorId.value, expertiseData)
    if (res.code === 200) {
      ElMessage.success('更新专长成功')
      expertiseDialogVisible.value = false
      fetchDoctors()
    }
  } catch (error) {
    ElMessage.error('更新专长失败')
  }
}

const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

const handleManageExpertise = (row) => {
  currentDoctorId.value = row.id
  currentDoctorName.value = row.name
  doctorRelationsRef.value.showExpertiseDialog()
}

const handleManageDepartment = () => {
  if (selectedRows.value.length !== 1) {
    ElMessage.warning('请选择一个医生进行科室关联管理')
    return
  }
  doctorRelationsRef.value.showDepartmentDialog()
}

const saveExpertiseRelations = async () => {
  relationLoading.value = true
  try {
    // 构建包含熟练度的数据结构
    const expertiseData = selectedExpertises.value.map(id => ({
      expertiseId: id,
      proficiency: 0.5  // 使用默认熟练度
    }))
    
    const response = await updateDoctorExpertises(selectedDoctor.value.id, expertiseData)
    
    if (response.code === 200) {
      ElMessage.success('专长关联更新成功')
      expertiseDialogVisible.value = false
      fetchDoctors()
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
      url: `/doctors/${selectedDoctor.value.id}/departments`,
      method: 'post',
      data: {
        departmentIds: selectedDepartments.value
      }
    })
    
    if (response.code === 200) {
      ElMessage.success('科室关联更新成功')
      departmentDialogVisible.value = false
      fetchDoctors()
    }
  } catch (error) {
    console.error('更新科室关联失败:', error)
    ElMessage.error('更新科室关联失败')
  } finally {
    relationLoading.value = false
  }
}

const filterExpertiseByDepartment = () => {
}

const filterExpertise = () => {
}

const getPositionById = (id) => {
  if (!id) return null;
  return positions.value.find(p => p.id == id);
};

const getPositionByName = (name) => {
  if (!name) return null;
  return positions.value.find(p => p.name === name);
};

const sortedPositions = computed(() => {
  return [...positions.value].sort((a, b) => a.id - b.id);
});

const sortedPositionOptions = computed(() => {
  return [...positions.value].sort((a, b) => a.id - b.id);
});

// 计算属性：当前用户是否已登录
const isUserLoggedIn = computed(() => {
  return !!localStorage.getItem('token');
});

onMounted(async () => {
  try {
    console.log('开始加载基础数据...');
    // 先分开获取各类数据，避免一个失败影响全部
    try {
      await fetchDepartments();
      console.log('科室数据加载完成:', departments.value);
    } catch (deptError) {
      console.error('科室数据加载失败:', deptError);
      // 如果正常方法失败，尝试应急方法
      if (departments.value.length === 0) {
        try {
          const success = await fetchDepartmentsEmergency();
          if (success) {
            console.log('通过应急方法成功获取科室数据');
          } else {
            console.error('无法获取科室数据，所有方法都已尝试');
          }
        } catch (emergencyError) {
          console.error('应急获取科室数据失败:', emergencyError);
        }
      }
    }
    
    try {
      await fetchPositions();
    console.log('职称数据加载完成:', positions.value);
    } catch (posError) {
      console.error('职称数据加载失败:', posError);
    }
    
    // 最后获取医生数据
    await fetchDoctors();
  } catch (error) {
    console.error('初始化数据失败:', error);
    ElMessage.error('加载数据失败，请刷新重试');
  }
});

const doctorRelationsRef = ref(null)

const handleBatchDelete = async () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请选择要删除的医生')
    return
  }

  try {
    // 检查所有选中的医生是否有专长关联
    const doctorsWithExpertise = []
    
    for (const row of selectedRows.value) {
      try {
        const expertiseResponse = await request.get(`/expertises/doctor/${row.id}`)
        if (expertiseResponse.code === 200 && expertiseResponse.data && expertiseResponse.data.length > 0) {
          doctorsWithExpertise.push(row.name)
        }
      } catch (error) {
        console.error(`检查医生 ${row.name} 的专长关联失败:`, error)
      }
    }
    
    // 如果有医生有专长关联，直接阻止删除并提示用户
    if (doctorsWithExpertise.length > 0) {
      const doctorNames = doctorsWithExpertise.join('、')
      ElMessage.error(`删除失败: 医生 "${doctorNames}" 有专长关联，请先取消所有专长关联后再删除`)
      return
    }
    
    // 检查所有选中的医生是否有评论
    let totalReviewCount = 0
    const doctorsWithReviews = []
    
    for (const row of selectedRows.value) {
      try {
        const reviewResponse = await request.get(`/reviews/doctor/${row.id}`, {
          params: { page: 1, size: 1 }
        })
        if (reviewResponse.code === 200 && reviewResponse.data && reviewResponse.data.total > 0) {
          totalReviewCount += reviewResponse.data.total
          doctorsWithReviews.push(row.name)
        }
      } catch (error) {
        console.error(`检查医生 ${row.name} 的评论失败:`, error)
        // 如果获取评论失败，继续流程
      }
    }
    
    // 根据有无评论显示不同的确认对话框
    if (doctorsWithReviews.length > 0) {
      const doctorNames = doctorsWithReviews.length <= 3 
        ? doctorsWithReviews.join('、')
        : `${doctorsWithReviews.slice(0, 3).join('、')}等${doctorsWithReviews.length}位医生`;
      
      await ElMessageBox.confirm(
        `选中的医生中，${doctorNames} 共有 ${totalReviewCount} 条评论记录，删除医生将同时删除所有相关评论，是否继续？`,
        '警告',
        {
          type: 'warning',
          confirmButtonText: '确定删除',
          cancelButtonText: '取消'
        }
      )
    } else {
      // 只有当所有医生都没有专长关联和评论时，显示普通确认对话框
    await ElMessageBox.confirm(
      `确认删除选中的 ${selectedRows.value.length} 位医生吗？此操作不可恢复！`,
      '警告',
      {
        type: 'warning',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }
    )
    }
    
    // 执行批量删除
    const results = await Promise.allSettled(
      selectedRows.value.map(async (row) => {
        try {
          return await request.delete(`/doctors/${row.id}`)
        } catch (error) {
          console.error(`删除医生 ${row.name}(ID:${row.id}) 失败:`, error)
          throw error
        }
      })
    )
    
    // 处理结果
    const successCount = results.filter(r => r.status === 'fulfilled').length
    const failCount = results.length - successCount
    
    if (successCount > 0) {
      if (failCount > 0) {
        ElMessage.warning(`成功删除${successCount}位医生，${failCount}位医生删除失败`)
      } else {
    ElMessage.success('批量删除成功')
      }
    
    if (doctorList.value.length === selectedRows.value.length && currentPage.value > 1) {
      currentPage.value--
    }
    fetchDoctors()
    } else {
      ElMessage.error('批量删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败')
    }
  }
}

const handleManageComments = (row) => {
  // 检查用户登录状态
  const token = localStorage.getItem('token');
  console.log('当前Token:', token);
  
  if (!token) {
    console.log('未检测到登录状态，尝试继续打开评论管理对话框');
    // 只给出警告，但仍然允许打开对话框以便调试
    ElMessage.warning('您当前未登录，部分功能可能受限');
  }
  
  currentDoctorId.value = row.id;
  currentDoctorName.value = row.name;
  console.log('准备打开评论管理对话框，医生ID:', row.id, '医生名称:', row.name);
  
  // 设置对话框可见
  reviewDialogVisible.value = true;
  
  // 重置筛选条件
  reviewPagination.value.currentPage = 1;
  
  // 尝试获取评价列表，如果已登录的话
  if (token) {
    fetchDoctorReviews(currentDoctorId.value);  // 传入当前医生的 ID
  } else {
    // 如果未登录，则至少清空列表，显示空表格
    reviewList.value = [];
    reviewPagination.value.total = 0;
    reviewLoading.value = false;
  }
}

const handleManagePositions = async () => {
  positionsDialogVisible.value = true
  
  const token = localStorage.getItem('token')
  if (!token) {
    ElMessage.error('请先登录再进行操作')
    return
  }
  
  console.log('当前用户Token:', token)
  
  await fetchPositionsWithCount()
}

const fetchPositionsWithCount = async () => {
  positionsLoading.value = true
  try {
    console.log('开始获取职称分布统计...')
    const response = await request({
      url: '/positions/distribution',
      method: 'get',
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
    console.log('获取职称分布统计响应:', response)
    
    if (response.code === 200) {
      positions.value = response.data.map(position => ({
        id: position.positionsId,
        name: position.positionsName,
        doctorCount: position.doctorCount
      }))
      console.log('职称列表处理完成:', positions.value)
    } else {
      console.error('获取职称列表响应错误:', response)
      ElMessage.error(response.message || '获取职称列表失败')
    }
  } catch (error) {
    console.error('获取职称列表异常:', error)
    if (error.response) {
      console.error('错误响应数据:', error.response.data)
      console.error('错误状态码:', error.response.status)
      
      if (error.response.status === 401 || error.response.status === 403) {
        ElMessage.error('没有操作权限，请检查登录状态')
      } else {
        ElMessage.error(error.response.data?.message || '获取职称列表失败')
      }
    } else if (error.request) {
      console.error('未收到响应:', error.request)
      ElMessage.error('服务器未响应，请检查网络连接')
    } else {
      console.error('请求配置错误:', error.message)
      ElMessage.error('请求配置错误: ' + error.message)
    }
  } finally {
    positionsLoading.value = false
  }
}

const showAddPositionDialog = () => {
  positionForm.id = ''
  positionForm.name = ''
  addPositionDialogVisible.value = true
  nextTick(() => {
    if (positionFormRef.value) {
      positionFormRef.value.resetFields()
    }
  })
}

const showEditPositionDialog = (position) => {
  positionForm.id = position.id
  positionForm.name = position.name
  editPositionDialogVisible.value = true
  nextTick(() => {
    if (positionFormRef.value) {
      positionFormRef.value.clearValidate()
    }
  })
}

const submitPositionForm = async () => {
  if (!positionFormRef.value) return
  
  await positionFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        let response
        const isAdd = !positionForm.id
        
        if (isAdd) {
          response = await request.post('/positions', {
            positionsId: null,
            positionsName: positionForm.name.trim(),
            description: '',
            sortOrder: positions.value.length + 1,
            status: 1
          })
          
          if (response.code === 200) {
            ElMessage.success('添加职称成功')
            addPositionDialogVisible.value = false
          }
        } else {
          response = await request.put(`/positions/${positionForm.id}`, {
            positionsName: positionForm.name.trim(),
            status: 1
          })
          
          if (response.code === 200) {
            ElMessage.success('编辑职称成功')
            editPositionDialogVisible.value = false
          }
        }
        
        await fetchPositionsWithCount()
      } catch (error) {
        console.error(positionForm.id ? '编辑职称失败:' : '添加职称失败:', error)
        ElMessage.error(error.response?.data?.message || (positionForm.id ? '编辑职称失败' : '添加职称失败'))
      }
    }
  })
}

const handleDeletePosition = async (position) => {
  let confirmMessage = `确认删除职称"${position.name}"吗？`;
  
  if (position.doctorCount > 0) {
    confirmMessage = `该职称已关联${position.doctorCount}名医生，删除后这些医生的职称将被清空。确认删除吗？`;
  }
  
  try {
    await ElMessageBox.confirm(confirmMessage, '警告', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    
    const response = await request.delete(`/positions/${position.id}`)
    if (response.code === 200) {
      ElMessage.success('删除职称成功')
      await fetchPositionsWithCount()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除职称失败:', error)
      ElMessage.error(error.response?.data?.message || '删除职称失败')
    }
  }
}

const toggleSortOrder = () => {
  searchForm.sortOrder = searchForm.sortOrder === 'ASC' ? 'DESC' : 'ASC'
  handleSearch()
}

// 添加新的应急函数，尝试使用各种可能的方法获取科室数据
const fetchDepartmentsEmergency = async () => {
  console.log('正在尝试应急方法获取科室数据...');
  const apiEndpoints = [
    '/departments',
    '/departments/list',
    '/departments/all',
    '/admin/departments',
    '/api/departments'
  ];
  
  for (const endpoint of apiEndpoints) {
    try {
      console.log(`尝试从 ${endpoint} 获取科室数据...`);
      const response = await request.get(endpoint, {
        params: { page: 1, size: 1000 }
      });
      
      if (response && response.code === 200 && response.data) {
        let records = [];
        
        // 处理不同的响应结构
        if (response.data.records) {
          records = response.data.records;
        } else if (Array.isArray(response.data)) {
          records = response.data;
        }
        
        if (records.length > 0) {
          departments.value = records.map(dept => ({
            id: dept.departmentId || dept.id,
            name: dept.departmentName || dept.name
          }));
          
          console.log(`成功从 ${endpoint} 获取${departments.value.length}条科室数据`);
          return true;
        }
      }
    } catch (error) {
      console.error(`从 ${endpoint} 获取科室数据失败:`, error.message);
    }
  }
  
  console.log('所有应急方法都失败了');
  return false;
}

const formatExpertiseList = (expertiseStr) => {
  if (!expertiseStr) return '暂无专长';
  
  // 如果是长字符串，可能包含重复内容，尝试提取不同的专长
  const expertises = expertiseStr.split(/[,，、]/).map(item => item.trim());
  // 去重
  const uniqueExpertises = [...new Set(expertises)];
  
  // 如果字符串太长，只显示部分
  if (uniqueExpertises.join(', ').length > 50) {
    return uniqueExpertises.slice(0, 3).join(', ') + '...';
  }
  
  return uniqueExpertises.join(', ');
}

// 获取医生评价列表
const fetchDoctorReviews = async (doctorId) => {
  reviewLoading.value = true
  try {
    const response = await getDoctorReviews(doctorId, reviewPagination.value.currentPage, reviewPagination.value.pageSize)
    if (response.code === 200) {
      reviewList.value = response.data.records
      reviewPagination.value.total = response.data.total
    }
  } catch (error) {
    console.error('获取医生评价失败:', error)
    ElMessage.error('获取医生评价失败')
    reviewList.value = []
    reviewPagination.value.total = 0
  } finally {
    reviewLoading.value = false
  }
}

// 处理评价分页大小变更
const handleReviewSizeChange = (size) => {
  reviewPagination.value.pageSize = size
  reviewPagination.value.currentPage = 1
  fetchDoctorReviews(currentDoctorId.value)
}

// 处理评价页码变更
const handleReviewPageChange = (page) => {
  reviewPagination.value.currentPage = page
  fetchDoctorReviews(currentDoctorId.value)
}

// 删除评价
const handleDeleteReview = async (row) => {
  try {
    await ElMessageBox.confirm('确认删除该评价吗？此操作不可恢复！', '警告', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })

    const response = await deleteReview(row.reviewId || row.id)
    if (response.code === 200) {
      ElMessage.success('删除成功')
      fetchDoctorReviews(currentDoctorId.value)
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
      }
  }
}

const testDialogVisibility = () => {
  // 实现测试对话框可见性的逻辑
  console.log('测试对话框可见性');
};

const refreshReviews = () => {
  // 实现强制刷新评论列表的逻辑
  console.log('强制刷新评论列表');
  fetchDoctorReviews();
};

const getReviewRowClass = (row) => {
  if (row.row.isAdmin) {
    return 'admin-review-row';
  }
  return '';
};

// 在 script setup 部分添加日期格式化函数
const formatDateTime = (dateStr) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  }).replace(/\//g, '-');
};

// 格式化评分，保留一位小数
const formatRating = (rating) => {
  if (!rating || isNaN(rating)) return '0.0'
  return Number(rating).toFixed(1)
}
</script>

<style scoped>
.doctor-container {
  padding: 12px;
  min-width: 1000px;
  max-width: 1400px;
  margin: 0 auto;
  background-color: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

/* 确保对话框在最顶层显示 */
:deep(.el-dialog) {
  margin-top: 5vh !important;
  display: flex;
  flex-direction: column;
  max-height: 90vh;
  margin: 0 auto;
}

:deep(.el-dialog__body) {
  overflow: auto;
  flex: 1;
}

:deep(.el-dialog__headerbtn) {
  z-index: 2001;
}

.review-management {
  min-height: 200px;
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

.header-left h2 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}

.header-buttons {
  display: flex;
  gap: 8px;
}

.search-area {
  margin-bottom: 16px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}

.table-container {
  width: 100%;
  overflow-x: auto;
}

.doctor-info {
  display: flex;
  align-items: center;
}

.info-content {
  margin-left: 10px;
}

.doctor-name {
  font-weight: 500;
  color: #303133;
  font-size: 13px;
}

.doctor-title {
  font-size: 12px;
  color: #909399;
}

.rating-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.rating-count {
  font-size: 12px;
  color: #909399;
}

.expertise-text {
  white-space: normal;
  word-break: break-all;
  line-height: 1.5;
  max-height: 3em;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.expertise-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
  max-height: 54px;
  overflow: hidden;
}

.expertise-tag {
  margin-right: 5px;
  margin-bottom: 5px;
}

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
  padding-right: 16px;
}

.operation-buttons {
  display: flex;
  gap: 8px;
}

.button-column {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.button-column .el-button {
  margin-left: 0;
  width: 100%;
}

.positions-dialog-content {
  padding: 20px;
}

.positions-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.positions-header h3 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}

:deep(.el-dialog__body) {
  padding: 0;
}

:deep(.positions-dialog-content .el-table) {
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

.position-operations {
  display: flex;
  gap: 8px;
  justify-content: center;
}

/* 医生表单对话框样式 */
.doctor-dialog {
  :deep(.el-dialog__body) {
    padding: 20px 30px;
  }
}

.doctor-form {
  .form-section {
    background-color: #f8f9fa;
    border-radius: 8px;
    padding: 16px;
    margin-bottom: 20px;

    &:last-child {
      margin-bottom: 0;
    }
  }

  .section-title {
    font-size: 16px;
    font-weight: 500;
    color: #303133;
    margin: 0 0 16px 0;
    padding-left: 8px;
    border-left: 3px solid #409eff;
  }

  .form-content {
    background-color: #fff;
    padding: 16px;
    border-radius: 4px;
    box-shadow: 0 2px 6px rgba(0, 0, 0, 0.04);
  }

  :deep(.el-form-item) {
    margin-bottom: 18px;

    &:last-child {
      margin-bottom: 0;
    }
  }

  :deep(.el-form-item__label) {
    font-weight: 500;
    color: #606266;
  }

  .gender-group {
    display: flex;
    gap: 24px;
  }

  .gender-icon {
    margin-right: 4px;
    vertical-align: middle;
  }

  :deep(.el-radio) {
    display: flex;
    align-items: center;
    height: 32px;
    margin-right: 0;
    padding: 0 15px;
    border: 1px solid #dcdfe6;
    border-radius: 4px;
    transition: all 0.3s;

    &.is-checked {
      border-color: #409eff;
      background-color: #ecf5ff;
    }

    &:hover {
      border-color: #409eff;
    }
  }

  .age-input {
    width: 160px;

    :deep(.el-input-number__decrease),
    :deep(.el-input-number__increase) {
      border-color: #dcdfe6;
      background-color: #f5f7fa;

      &:hover {
        color: #409eff;
      }
    }
  }
}

.dialog-footer {
  padding-top: 10px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.debug-info {
  padding: 10px;
  background-color: #f0f9eb;
  border-radius: 4px;
}

/* 优化评分星星的显示 */
:deep(.el-rate) {
  display: flex;
  justify-content: flex-start;
  min-width: 150px;
}

:deep(.el-rate__icon) {
  font-size: 16px;
  margin-right: 4px;
}

:deep(.el-rate__text) {
  margin-left: 8px;
  font-size: 14px;
  color: #ff9900;
}

.review-filters-container {
  margin-bottom: 16px;
}

.review-filters {
  display: flex;
  flex-wrap: nowrap;
  align-items: center;
  gap: 10px;
  background-color: #f5f7fa;
  padding: 12px;
  border-radius: 4px;
}

.filter-group {
  display: flex;
  align-items: center;
}

.separator {
  margin: 0 5px;
  color: #909399;
}

.filter-buttons {
  display: flex;
  gap: 8px;
  margin-left: auto;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 5px;
}

.anonymous-tag {
  font-size: 10px;
  padding: 0 4px;
  height: 16px;
  line-height: 14px;
}

.admin-tag {
  font-size: 10px;
  padding: 0 4px;
  height: 16px;
  line-height: 14px;
  background-color: #67c23a;
  color: white;
}

.admin-review-row {
  background-color: rgba(103, 194, 58, 0.1);
}

.admin-review-row td {
  border-bottom-color: #e1f3d8 !important;
}

.comment-content-cell {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100%;
  cursor: pointer;
}

.rating-display {
  display: flex;
  align-items: center;
}

.comment-content {
  white-space: normal;
  word-break: break-word;
  line-height: 1.5;
  max-height: 3em;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

:deep(.el-rate) {
  display: inline-flex;
  align-items: center;
}

:deep(.el-rate__icon) {
  margin-right: 2px;
  font-size: 14px;
}

:deep(.el-rate__text) {
  font-size: 14px;
  margin-left: 8px;
}
</style> 