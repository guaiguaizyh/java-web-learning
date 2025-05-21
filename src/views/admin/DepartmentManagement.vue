<template>
  <div class="department-management">
    <div class="header">
      <div class="header-left">
        <h2>科室管理</h2>
      </div>
      <div class="header-buttons">
        <el-button type="primary" @click="showAddDialog">
          <el-icon><Plus /></el-icon>添加科室
        </el-button>
      </div>
    </div>

    <!-- 搜索区域 -->
    <div class="search-area">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="科室描述" style="margin-right: 10px; margin-bottom: 0;">
          <el-input
            v-model="searchForm.description"
            placeholder="请输入科室描述"
            clearable
            style="width: 160px; height: 32px;"
            class="search-input"
            @keyup.enter="handleSearch"
            @clear="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="科室名称" style="margin-right: 10px; margin-bottom: 0;">
          <el-input
            v-model="searchForm.departmentName"
            placeholder="请输入科室名称"
            clearable
            style="width: 160px; height: 32px;"
            class="search-input"
            @keyup.enter="handleSearch"
            @clear="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="排序字段" style="margin-right: 10px; margin-bottom: 0;">
          <el-select
            v-model="searchForm.sortField"
            placeholder="排序方式"
            style="width: 120px"
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
        <el-form-item style="margin-bottom: 0;">
          <div class="search-buttons">
            <el-button 
              :type="searchForm.sortOrder === 'asc' ? 'primary' : 'default'"
              @click="toggleSortOrder"
              style="margin-right: 4px; padding: 8px 12px;"
            >
              <el-icon><Sort /></el-icon>
              {{ searchForm.sortOrder === 'asc' ? '升序' : '降序' }}
            </el-button>
            <el-button type="primary" @click="handleSearch" style="margin-right: 4px; padding: 8px 12px;">
              <el-icon><Search /></el-icon>搜索
            </el-button>
            <el-button @click="resetSearch" style="padding: 8px 12px;">
              <el-icon><Refresh /></el-icon>重置
            </el-button>
          </div>
        </el-form-item>
      </el-form>
    </div>

    <!-- 科室列表 -->
    <el-table
      v-loading="loading"
      :data="departmentList"
      row-key="departmentId"
      border
      style="width: 100%"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" />
      <el-table-column prop="name" label="科室名称" min-width="150" />
      <el-table-column prop="description" label="描述" min-width="360" show-overflow-tooltip />
      <el-table-column label="评分" width="300">
        <template #default="{ row }">
          <div class="rating-info">
            <el-rate
              v-model="row.averageRating"
              disabled
              show-score
              text-color="#ff9900"
              score-template="{value}"
            />
            <div class="rating-count">({{ row.reviewCount || 0 }}条评价)</div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="{ row }">
          <div class="button-container">
            <div class="button-row">
              <el-button
                type="primary"
                size="small"
                @click="showEditDialog(row)"
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
                type="info"
                size="small"
                @click="viewDoctors(row)"
              >
                医生
              </el-button>
              <el-button
                type="warning"
                size="small"
                @click="viewExpertise(row)"
              >
                专长
              </el-button>
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

    <!-- 添加/编辑科室对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '添加科室' : '编辑科室'"
      width="500px"
      @close="handleDialogClose"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="departmentForm"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="科室名称" prop="name">
          <el-input 
            v-model="departmentForm.name" 
            maxlength="50" 
            show-word-limit
            @keyup.enter="handleSubmit"
          />
        </el-form-item>
        <el-form-item label="科室描述" prop="description">
          <el-input
            v-model="departmentForm.description"
            type="textarea"
            :rows="4"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 科室医生管理对话框 -->
    <el-dialog
      v-model="doctorDialogVisible"
      :title="`${currentDepartment?.name || ''} - 关联医生`"
      width="900px"
    >
      <div class="doctor-management" v-loading="doctorLoading">
        <div class="doctor-header">
          <div class="doctor-title-area" style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
            <h3>医生列表</h3>
            <el-button 
              type="danger"
              size="small"
              @click="handleBatchUnlinkDoctors"
              :disabled="selectedDoctors.length === 0 || selectedDoctors.some(doctor => doctor.expertiseCount > 0)"
            >
              <el-icon><Link /></el-icon>批量取消关联
            </el-button>
          </div>
        </div>

        <el-table
          :data="departmentDoctors"
          border
          style="width: 100%"
          @selection-change="handleDoctorSelectionChange"
          :row-key="row => row.doctorId"
          :select-on-indeterminate="false"
        >
          <el-table-column 
            type="selection" 
            width="55"
            :selectable="(row) => row.expertiseCount === 0"
          />
          <el-table-column label="医生信息" width="180">
            <template #default="{ row }">
              <div class="doctor-info">
                <el-avatar :size="36" :src="row.avatarUrl">
                  <el-icon><User /></el-icon>
                </el-avatar>
                <div class="info-content">
                  <div class="doctor-name">{{ row.name }}</div>
                  <div v-if="row.expertiseCount > 0" class="expertise-count">
                    <el-tag size="small" type="success">已关联{{ row.expertiseCount }}个专长</el-tag>
                  </div>
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
          <el-table-column label="年龄" width="80">
            <template #default="{ row }">
              <span>{{ row.age }}</span>
            </template>
          </el-table-column>
          <el-table-column label="评分" width="200">
            <template #default="{ row }">
              <div class="rating-info">
                <el-rate
                  v-model="row.averageRating"
                  disabled
                  show-score
                  text-color="#ff9900"
                />
                <div class="rating-count">({{ row.ratingCount || 0 }}条评价)</div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-tooltip
                v-if="row.expertiseCount > 0"
                content="该医生已关联专长，请先解除专长关联"
                placement="top"
              >
              <el-button
                  type="danger"
                  size="small"
                  disabled
                >
                  取消关联
                </el-button>
              </el-tooltip>
              <el-button
                v-else
                type="danger"
                size="small"
                @click="handleUnlinkDoctor(row)"
              >
                取消关联
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination">
          <el-pagination
            v-model:current-page="doctorCurrentPage"
            v-model:page-size="doctorPageSize"
            :total="doctorTotal"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next"
            @size-change="handleDoctorSizeChange"
            @current-change="handleDoctorPageChange"
          />
        </div>
      </div>
    </el-dialog>

    <!-- 医生添加/编辑对话框 -->
    <el-dialog
      v-model="doctorFormDialogVisible"
      :title="doctorDialogType === 'add' ? '添加医生' : '编辑医生'"
      width="580px"
      :close-on-click-modal="false"
      class="doctor-dialog"
    >
      <el-form
        ref="doctorFormRef"
        :model="doctorForm"
        :rules="doctorRules"
        label-width="80px"
        class="doctor-form"
      >
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
          </div>
        </div>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="doctorFormDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitDoctorForm">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 专长列表对话框 -->
    <el-dialog
      v-model="expertiseDialogVisible"
      :title="`${selectedDepartment?.name || ''} - 专长列表`"
      width="800px"
      @close="handleExpertiseDialogClose"
    >
      <div class="dialog-header" style="margin-bottom: 20px;">
        <div class="dialog-title-area" style="display: flex; justify-content: space-between; align-items: center;">
          <h3 style="margin: 0;">专长管理</h3>
          <el-button 
            type="danger"
            size="small"
            @click="handleBatchUnlink(selectedExpertises)"
            :disabled="selectedExpertises.length === 0 || selectedExpertises.some(expertise => expertise.doctorCount > 0)"
          >
            <el-icon><Link /></el-icon>批量取消关联
          </el-button>
        </div>
      </div>

      <div v-loading="loading">
        <el-table
          :data="departmentExpertiseList"
          border
          style="width: 100%"
          @selection-change="handleExpertiseSelectionChange"
          :row-key="row => row.expertiseId"
          :select-on-indeterminate="false"
        >
          <el-table-column 
            type="selection" 
            width="55"
            :selectable="(row) => row.doctorCount === 0"
          />
          <el-table-column label="专长信息" min-width="300">
            <template #default="{ row }">
              <div class="expertise-info">
                <div class="expertise-name">
                  {{ row.expertiseName }}
                  <el-tag 
                    size="small" 
                    :type="row.doctorCount > 0 ? 'success' : 'info'"
                    class="associated-tag"
                  >
                    已关联{{ row.doctorCount || 0 }}名医生
                  </el-tag>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <div class="operation-buttons">
                <el-tooltip
                  v-if="row.doctorCount > 0"
                  content="该专长已关联医生，请先解除医生关联"
                  placement="top"
                >
                <el-button
                    type="warning"
                    size="small"
                    disabled
                  >
                    取消关联
                  </el-button>
                </el-tooltip>
                <el-button
                  v-else
                  type="warning"
                  size="small"
                  @click="handleUnlinkExpertise(row)"
                >
                  取消关联
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Delete,
  Plus,
  Search,
  Sort,
  Refresh,
  Link,
  OfficeBuilding,
  User,
  Star,
  ChatLineRound,
  List,
  Male,
  Female
} from '@element-plus/icons-vue'
import request from '@/utils/axios'
import { 
  getDepartments, 
  getDepartmentById, 
  addDepartment, 
  updateDepartment, 
  deleteDepartment,
  getDepartmentDoctors,
  searchDepartments,
  getDepartmentExpertises
} from '@/api/departments'
import { getPositions } from '@/api/positions'
import { unlinkDepartmentDoctor, batchUnlinkDepartmentDoctors, addDoctor, updateDoctor } from '@/api/doctors'
import { useUserStore } from '@/store/user'

// 数据初始化
const departmentList = ref([])
const departmentOptions = ref([])
const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const dialogType = ref('add')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const selectedRows = ref([])
const selectedDoctors = ref([])
const positionList = ref([])

// 医生管理相关的响应式变量
const doctorDialogVisible = ref(false)
const doctorFormDialogVisible = ref(false)
const doctorDialogType = ref('add')
const currentDepartment = ref(null)
const departmentDoctors = ref([])
const doctorLoading = ref(false)
const doctorCurrentPage = ref(1)
const doctorPageSize = ref(10)
const doctorTotal = ref(0)

const doctorForm = ref({
  name: '',
  gender: '',
  age: null,
  positionsId: '',
  departmentId: ''
})

const formRef = ref(null)
const doctorFormRef = ref(null)

const searchForm = ref({
  departmentName: '',
  description: '',
  sortField: '',
  sortOrder: 'asc'
})

const departmentForm = ref({
  departmentId: '',
  name: '',
  description: ''
})

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '科室名称不能为空', trigger: 'blur' },
    { required: true, message: '科室名称不能为空', trigger: 'change' },
    { min: 2, message: '科室名称不能少于2个字符', trigger: 'blur' },
    { max: 50, message: '科室名称不能超过50个字符', trigger: 'blur' }
  ],
  description: [
    { max: 500, message: '科室描述不能超过500个字符', trigger: 'blur' }
  ]
}

// 医生表单验证规则
const doctorRules = {
  name: [
    { required: true, message: '医生姓名不能为空', trigger: 'blur' },
    { min: 2, message: '医生姓名不能少于2个字符', trigger: 'blur' },
    { max: 50, message: '医生姓名不能超过50个字符', trigger: 'blur' }
  ],
  gender: [
    { required: true, message: '请选择性别', trigger: 'change' }
  ],
  age: [
    { required: true, message: '请输入年龄', trigger: 'blur' },
    { type: 'number', message: '年龄必须为数字', trigger: 'blur' }
  ],
  positionsId: [
    { required: true, message: '请选择职称', trigger: 'change' }
  ]
}

// 修改排序方式下拉选项
const sortOptions = [
  { label: '默认排序', value: '' },
  { label: '按科室ID排序', value: 'departmentId' },
  { label: '按评分排序', value: 'averageRating' }
]

// 添加选择行数据
const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

// 删除科室
const handleDelete = async (row) => {
  const hasDoctors = row.doctorCount > 0
  const hasExpertise = row.expertiseCount > 0
  
  // 如果有关联的医生或专长，直接提示错误
  if (hasDoctors || hasExpertise) {
    let message = []
    if (hasDoctors) {
      message.push(`${row.doctorCount}个医生`)
    }
    if (hasExpertise) {
      message.push(`${row.expertiseCount}个专长`)
    }
    ElMessage.warning(`该科室尚有${message.join('和')}关联，无法删除，请先解除关联`)
    return
  }

  // 没有关联数据时，才显示删除确认框
  try {
    await ElMessageBox.confirm(
      '确定要删除该科室吗？',
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const response = await request.delete(`/departments/${row.departmentId}`)
    if (response.code === 200) {
      ElMessage.success('删除成功')
      fetchDepartments()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除科室失败:', error)
      ElMessage.error(error.response?.data?.message || '删除科室失败')
    }
  }
}

// 批量删除科室
const handleBatchDelete = async () => {
  if (selectedRows.value.length === 0) return
  
  try {
    const totalDoctors = selectedRows.value.reduce((sum, row) => sum + (row.doctorCount || 0), 0)
    const totalExpertise = selectedRows.value.reduce((sum, row) => sum + (row.expertiseCount || 0), 0)
    const hasDoctors = totalDoctors > 0
    const hasExpertise = totalExpertise > 0
    
    if (hasDoctors || hasExpertise) {
      let message = []
      if (hasDoctors) {
        message.push(`${totalDoctors}个医生`)
      }
      if (hasExpertise) {
        message.push(`${totalExpertise}个专长`)
      }
      
      await ElMessageBox.confirm(
        `选中的科室中共有${message.join('和')}，删除科室将同时删除关联的医生和专长，是否继续？`,
        '批量删除确认',
        {
          confirmButtonText: '确定删除',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
    } else {
      // 没有关联数据，直接删除
      await ElMessageBox.confirm(
        `确定要删除选中的${selectedRows.value.length}个科室吗？`,
        '批量删除确认',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
    }
    
    // 执行批量删除
    const results = await Promise.allSettled(
      selectedRows.value.map(async (row) => {
        try {
          const params = hasDoctors || hasExpertise ? { cascade: true } : undefined;
          return await request.delete(`/departments/${row.departmentId}`, { params });
        } catch (error) {
          console.error(`删除科室 ${row.name}(ID:${row.departmentId}) 失败:`, error);
          throw error;
        }
      })
    );
    
    // 处理结果
    const successCount = results.filter(r => r.status === 'fulfilled').length;
    const failCount = results.length - successCount;
    
    if (successCount > 0) {
      if (failCount > 0) {
        ElMessage.warning(`成功删除${successCount}个科室，${failCount}个科室删除失败`);
      } else {
        ElMessage.success('批量删除成功');
      }
      
      if (departmentList.value.length === selectedRows.value.length && currentPage.value > 1) {
        currentPage.value--;
      }
      fetchDepartments();
    } else {
      ElMessage.error('批量删除失败');
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除科室失败:', error);
      ElMessage.error(error.response?.data?.message || '批量删除科室失败');
    }
  }
}

// 获取科室列表
const fetchDepartments = async () => {
  loading.value = true;
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value
    };

    if (searchForm.value.sortField) {
      params.sortField = searchForm.value.sortField;
      params.sortOrder = searchForm.value.sortOrder || 'asc';
    }

    if (searchForm.value.departmentName) {
      params.departmentName = searchForm.value.departmentName;
    }
    if (searchForm.value.description) {
      params.description = searchForm.value.description;
    }
    
    console.log('Fetching departments with params:', params);
    const response = await request.get('/departments/list', { params });
    console.log('Department API Response:', response);
    
    if (response.code === 200) {
      console.log('Raw department records:', response.data.records);
      departmentList.value = response.data.records.map(dept => {
        console.log('Processing department:', dept);
        return {
          departmentId: dept.departmentId,
          name: dept.departmentName,
          description: dept.description,
          averageRating: dept.averageRating || 0,
          reviewCount: dept.reviewCount || 0
        };
      });
      console.log('Processed department list:', departmentList.value);
      total.value = response.data.total;
    }
  } catch (error) {
    console.error('获取科室列表失败:', error);
    ElMessage.error('获取科室列表失败');
  } finally {
    loading.value = false;
  }
};

// 切换排序方向
const toggleSortOrder = () => {
  searchForm.value.sortOrder = searchForm.value.sortOrder === 'asc' ? 'desc' : 'asc'
  handleSearch()
}

// 搜索处理
const handleSearch = () => {
  currentPage.value = 1
  fetchDepartments()
}

// 重置搜索
const resetSearch = () => {
  searchForm.value = {
    departmentName: '',
    description: '',
    sortField: '',
    sortOrder: 'asc'
  }
  handleSearch()
}

// 分页处理
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchDepartments()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchDepartments()
}

// 添加/编辑科室
const showAddDialog = () => {
  dialogType.value = 'add'
  departmentForm.value = {
    departmentId: '',
    name: '',
    description: ''
  }
  dialogVisible.value = true
  // 重置表单验证
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

const showEditDialog = (row) => {
  dialogType.value = 'edit'
  departmentForm.value = {
    departmentId: row.departmentId,
    name: row.name,
    description: row.description
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return;
  
  try {
    const valid = await formRef.value.validate();
    if (!valid) return;

    submitting.value = true;
    const isAdd = dialogType.value === 'add';
    
    if (!departmentForm.value.name || departmentForm.value.name.trim() === '') {
      ElMessage.error('科室名称不能为空');
      return;
    }

    const data = {
        departmentName: departmentForm.value.name.trim(),
        description: departmentForm.value.description?.trim() || ''
    };

    const response = isAdd 
      ? await addDepartment(data)
      : await updateDepartment(departmentForm.value.departmentId, data);
    
    if (response.code === 200) {
      dialogVisible.value = false;
      ElMessage.success(isAdd ? '添加成功' : '更新成功');
      await fetchDepartments();
      if (formRef.value) {
        formRef.value.resetFields();
      }
        }
      } catch (error) {
    console.error(dialogType.value === 'add' ? '添加科室失败:' : '更新科室失败:', error);
    if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message);
    } else {
      ElMessage.error(dialogType.value === 'add' ? '添加科室失败' : '更新科室失败');
    }
      } finally {
    submitting.value = false;
      }
};

// 添加/编辑科室对话框的关闭处理
const handleDialogClose = () => {
  // 重置表单
  if (formRef.value) {
    formRef.value.resetFields()
  }
  // 重置表单数据
  departmentForm.value = {
    departmentId: '',
    name: '',
    description: ''
  }
}

// 查看科室医生
const viewDoctors = async (row) => {
  try {
    console.log('Selected department:', row)
    currentDepartment.value = {
      id: row.departmentId,
      name: row.name
    }
    console.log('Current department:', currentDepartment.value)
    doctorDialogVisible.value = true
    await fetchDepartmentDoctors()
  } catch (error) {
    console.error('Failed to view doctors:', error)
    ElMessage.error('获取医生列表失败')
  }
}

// 获取科室专长列表
const fetchExpertiseList = async (departmentId) => {
  try {
    loading.value = true;
    // 获取科室的专长列表
    const expertiseResponse = await request.get(`/expertises/department/${departmentId}`);
    if (expertiseResponse.code === 200) {
      const expertiseList = expertiseResponse.data;
      console.log('获取到的专长列表:', expertiseList);
      return expertiseList.map(expertise => ({
        ...expertise,
        expertiseId: expertise.expertiseId,
        expertiseName: expertise.expertiseName,
        departmentId: expertise.departmentId,
        doctorCount: expertise.doctorCount || 0,
              isAssociated: expertise.doctorCount > 0
      }));
    } else {
      ElMessage.error(expertiseResponse.message || '获取专长列表失败');
      return [];
    }
  } catch (error) {
    console.error('获取专长列表失败:', error);
    ElMessage.error('获取专长列表失败: ' + (error.response?.data?.message || error.message || '未知错误'));
    return [];
  } finally {
    loading.value = false;
  }
};

// 查看专长
const viewExpertise = async (row) => {
  try {
    loading.value = true;
    const expertiseList = await fetchExpertiseList(row.departmentId);
    console.log('科室专长列表:', expertiseList);
    
    // 显示专长列表对话框
    selectedDepartment.value = row;
    expertiseDialogVisible.value = true;
    departmentExpertiseList.value = expertiseList;
  } catch (error) {
    console.error('获取科室专长列表失败:', error);
    ElMessage.error('获取科室专长列表失败: ' + (error.response?.data?.message || error.message || '未知错误'));
  } finally {
    loading.value = false;
  }
};

// 获取所有科室列表（用于下拉选择）
const fetchAllDepartments = async () => {
  try {
    const response = await request.get('/departments/list', { 
      params: { 
        page: 1,
        size: 1000,
        simple: true // 添加simple参数，表示只需要基础信息
      } 
    });
    if (response.code === 200) {
      departmentOptions.value = response.data.records.map(dept => ({
        id: dept.departmentId,
        name: dept.departmentName
      }));
    }
  } catch (error) {
    console.error('获取科室列表失败:', error);
    ElMessage.error('获取科室列表失败');
  }
};

// 获取科室医生列表
const fetchDepartmentDoctors = async () => {
  if (!currentDepartment.value?.id) {
    console.error('No department selected', currentDepartment.value);
    return;
  }

  try {
    doctorLoading.value = true;
    console.log('Fetching doctors with params:', {
      departmentId: currentDepartment.value.id,
      page: doctorCurrentPage.value,
      size: doctorPageSize.value
    });
    
    const response = await getDepartmentDoctors(
      currentDepartment.value.id,
      {
        page: doctorCurrentPage.value,
        size: doctorPageSize.value
      }
    );
    
    console.log('Doctor API Response:', response);
    
    if (response.code === 200 && response.data) {
      // 获取每个医生的专长数量
      const doctorsWithExpertise = await Promise.all(
        response.data.records.map(async doctor => {
          try {
            const expertiseResponse = await request.get(`/expertises/doctor/${doctor.doctorId}`);
            const expertiseCount = expertiseResponse.code === 200 ? (expertiseResponse.data?.length || 0) : 0;
            return {
              ...doctor,
        id: doctor.doctorId,
        doctorId: doctor.doctorId,
        name: doctor.name,
        gender: doctor.gender,
        age: doctor.age,
        positionsId: doctor.positionsId,
        positionsName: doctor.positionsName,
        departmentId: doctor.departmentId,
        avatarUrl: doctor.avatarUrl,
        averageRating: doctor.averageRating || 0,
              ratingCount: doctor.ratingCount || 0,
              expertiseCount
            };
          } catch (error) {
            console.error(`Failed to fetch expertise count for doctor ${doctor.doctorId}:`, error);
            return {
              ...doctor,
              expertiseCount: 0
            };
          }
        })
      );
      
      departmentDoctors.value = doctorsWithExpertise;
      doctorTotal.value = response.data.total || 0;
    }
  } catch (error) {
    console.error('Failed to fetch department doctors:', error);
    ElMessage.error('获取医生列表失败');
  } finally {
    doctorLoading.value = false;
  }
};

// 获取职称列表
const fetchPositions = async () => {
  try {
    positionsLoading.value = true
    const response = await request.get('/positions')
    console.log('职称数据响应:', response)
    
    if (response.code === 200) {
      positionList.value = response.data.map(item => ({
        id: String(item.positionsId || item.id),
        name: item.positionsName || item.name
      }))
      console.log('处理后的职称列表:', positionList.value)
    }
  } catch (error) {
    console.error('获取职称列表失败:', error)
    ElMessage.error('获取职称列表失败')
  } finally {
    positionsLoading.value = false
  }
}

// 添加一个计算属性来获取当前选中的职称名称
const currentPositionName = computed(() => {
  const position = positionList.value.find(p => p.id === doctorForm.value.positionsId)
  return position ? position.name : ''
})

// 添加医生
const handleAddDoctor = async () => {
  doctorDialogType.value = 'add'
  doctorForm.value = {
    name: '',
    gender: '',
    age: null,
    positionsId: '',
    departmentId: currentDepartment.value?.id || ''  // 设置当前科室ID
  }
  await fetchPositions()
  doctorFormDialogVisible.value = true
}

// 编辑医生
const handleEditDoctor = async (row) => {
  try {
    console.log('开始编辑医生，原始数据:', row)
  doctorDialogType.value = 'edit'
    // 先获取职称列表
    await fetchPositions()
    
    // 设置表单数据
  doctorForm.value = {
      id: row.doctorId || row.id,
    name: row.name,
    gender: row.gender,
    age: row.age,
      positionsId: String(row.positionsId || ''), // 确保转换为字符串
      departmentId: row.departmentId || currentDepartment.value?.id || ''
    }
    
    console.log('设置表单数据:', doctorForm.value)
    console.log('当前职称列表:', positionList.value)
    console.log('当前选中的职称名称:', currentPositionName.value)
    
  doctorFormDialogVisible.value = true
  } catch (error) {
    console.error('编辑医生时出错:', error)
    ElMessage.error('编辑医生失败')
  }
}

// 提交医生表单
const submitDoctorForm = async () => {
  if (!doctorFormRef.value) {
    console.warn('Form reference not found')
    return
  }
  
  try {
    const valid = await doctorFormRef.value.validate()
    if (!valid) {
      return
    }
    
    const formData = {
      name: doctorForm.value.name,
      gender: doctorForm.value.gender,
      age: doctorForm.value.age,
      positionsId: doctorForm.value.positionsId
    }
    
    console.log('提交的表单数据:', formData)

    if (doctorDialogType.value === 'add') {
      try {
        const createResponse = await addDoctor(formData)
        if (createResponse.code === 200 && createResponse.data) {
          const newDoctorId = createResponse.data.doctorId || createResponse.data.id
          await addDepartmentDoctor(newDoctorId, currentDepartment.value.id)
          ElMessage.success('添加医生成功')
          doctorFormDialogVisible.value = false
          await fetchDepartmentDoctors()
        } else {
          throw new Error(createResponse.message || '创建医生失败')
        }
      } catch (error) {
        console.error('添加医生失败:', error)
        ElMessage.error(error.message || '添加医生失败')
        throw error
      }
    } else {
      if (!doctorForm.value.id) {
        ElMessage.error('无效的医生信息')
        return
      }
      const updateResponse = await updateDoctor(doctorForm.value.id, formData)
      if (updateResponse.code === 200) {
        ElMessage.success('更新医生信息成功')
        doctorFormDialogVisible.value = false
        await fetchDepartmentDoctors()
      } else {
        throw new Error(updateResponse.message || '更新医生失败')
      }
    }
  } catch (error) {
    console.error('提交医生表单失败:', error)
    ElMessage.error(error.message || (doctorDialogType.value === 'add' ? '添加医生失败' : '更新医生信息失败'))
  }
}

// 取消医生与科室的关联
const handleUnlinkDoctor = async (doctor) => {
  try {
    await ElMessageBox.confirm(
      `确定要取消医生 ${doctor.name} 与当前科室的关联吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const response = await unlinkDepartmentDoctor(doctor.doctorId)
    if (response.code === 200) {
      ElMessage.success('取消关联成功')
      await fetchDepartmentDoctors() // 刷新医生列表
    } else {
      ElMessage.error(response.message || '取消关联失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消关联失败:', error)
      ElMessage.error('取消关联失败')
    }
  }
}

// 批量解除医生与科室的关联
const handleBatchUnlinkDoctors = async () => {
  if (selectedDoctors.value.length === 0) {
    ElMessage.warning('请选择要解除关联的医生')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定要解除选中的 ${selectedDoctors.value.length} 名医生与当前科室的关联吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const doctorIds = selectedDoctors.value.map(doctor => doctor.id)
    await batchUnlinkDepartmentDoctors(doctorIds)
    ElMessage.success('批量解除关联成功')
    await fetchDepartmentDoctors()
    selectedDoctors.value = []
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量解除关联失败:', error)
      ElMessage.error(`批量解除关联失败：${error.message || '未知错误'}`)
    }
  }
}

// 处理医生选择变更
const handleDoctorSelectionChange = (selection) => {
  selectedDoctors.value = selection
}

// 重置医生管理相关状态
const resetDoctorManagement = () => {
  doctorDialogVisible.value = false
  doctorFormDialogVisible.value = false
  currentDepartment.value = null
  departmentDoctors.value = []
  selectedDoctors.value = []
  doctorCurrentPage.value = 1
  doctorTotal.value = 0
  doctorForm.value = {
    name: '',
    gender: '',
    age: null,
    positionsId: '',
    departmentId: ''
  }
}

// 处理医生对话框关闭
const handleDoctorDialogClose = () => {
  resetDoctorManagement()
}

// 处理医生分页大小变更
const handleDoctorSizeChange = (size) => {
  doctorPageSize.value = size
  doctorCurrentPage.value = 1 // 重置到第一页
  fetchDepartmentDoctors()
}

// 处理医生页码变更
const handleDoctorPageChange = (page) => {
  doctorCurrentPage.value = page
  fetchDepartmentDoctors()
}

// 添加新的响应式变量
const positionsLoading = ref(false)
const departments = ref([])
const sortedPositionOptions = computed(() => {
  return [...positionList.value].sort((a, b) => {
    const aId = Number(a.id)
    const bId = Number(b.id)
    return aId - bId
  })
})

// 添加一个计算属性来获取职称名称
const getPositionName = computed(() => (positionId) => {
  const position = positionList.value.find(p => p.id === positionId)
  return position ? position.name : ''
})

// 在 script setup 部分添加
const expertiseDialogVisible = ref(false);
const selectedDepartment = ref(null);
const departmentExpertiseList = ref([]);

// 关闭专长对话框
const handleExpertiseDialogClose = () => {
  expertiseDialogVisible.value = false;
  selectedDepartment.value = null;
  departmentExpertiseList.value = [];
};

// 添加新的响应式变量
const selectedExpertises = ref([])

// 处理专长选择变更
const handleExpertiseSelectionChange = (selection) => {
  selectedExpertises.value = selection
}

// 查看专长的医生列表
const viewExpertiseDoctors = (expertise) => {
  // TODO: 实现查看专长医生列表的功能
  console.log('查看专长医生列表:', expertise)
}

// 查看专长的症状列表
const viewExpertiseSymptoms = (expertise) => {
  // TODO: 实现查看专长症状列表的功能
  console.log('查看专长症状列表:', expertise)
}

// 添加专长
const handleAddExpertise = () => {
  // TODO: 实现添加专长的功能
  console.log('添加专长')
}

// 编辑专长
const handleEditExpertise = async (expertise) => {
  try {
    const { expertiseId, expertiseName } = expertise;
    const response = await request.put(`/expertises/${expertiseId}/department/${selectedDepartment.value.departmentId}`, {
      name: expertiseName
    });
    
    if (response.code === 200) {
      ElMessage.success('专长更新成功');
      // 刷新专长列表
      await viewExpertise(selectedDepartment.value);
    } else {
      ElMessage.error(response.message || '专长更新失败');
    }
  } catch (error) {
    console.error('编辑专长失败:', error);
    ElMessage.error('编辑专长失败: ' + (error.response?.data?.message || error.message || '未知错误'));
  }
};

// 取消关联专长
const handleUnlinkExpertise = async (expertise) => {
  try {
    await ElMessageBox.confirm(
      `确定要取消关联专长 "${expertise.expertiseName}" 吗？\n取消关联后，该专长将不再属于此科室。`,
      '取消关联确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    );

    // 发送更新请求，将 departmentId 设置为 null
    const response = await request.put(`/expertises/${expertise.expertiseId}`, {
      expertiseName: expertise.expertiseName,
      departmentId: null
    });
    
    if (response.code === 200) {
      ElMessage.success('取消关联成功');
      // 刷新专长列表
      await viewExpertise(selectedDepartment.value);
    } else {
      ElMessage.error(response.message || '取消关联失败');
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消关联专长失败:', error);
      ElMessage.error('取消关联专长失败: ' + (error.response?.data?.message || error.message || '未知错误'));
    }
  }
};

// 批量取消关联
const handleBatchUnlink = async (expertiseList) => {
  if (!expertiseList || expertiseList.length === 0) {
    ElMessage.warning('请选择要取消关联的专长');
    return;
  }

  try {
    // 检查是否有专长关联了医生
    const hasAssociatedDoctors = expertiseList.some(expertise => expertise.doctorCount > 0);
    if (hasAssociatedDoctors) {
      ElMessage.warning('选中的专长中有已关联医生的专长，请先解除医生关联');
      return;
    }

    await ElMessageBox.confirm(
      `确定要取消关联选中的 ${expertiseList.length} 个专长吗？\n取消关联后，这些专长将不再属于此科室。`,
      '批量取消关联确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    );

    // 批量发送更新请求
    const promises = expertiseList.map(expertise => 
      request.put(`/expertises/${expertise.expertiseId}`, {
        expertiseName: expertise.expertiseName,
        departmentId: null
      })
    );
    
    await Promise.all(promises);
      ElMessage.success('批量取消关联成功');
      // 刷新专长列表
      await viewExpertise(selectedDepartment.value);
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量取消关联失败:', error);
      ElMessage.error('批量取消关联失败: ' + (error.response?.data?.message || error.message || '未知错误'));
    }
  }
};

onMounted(async () => {
  await fetchAllDepartments()
  await fetchDepartments()
  await fetchPositions()
})
</script>

<style scoped>
/* .department-container {
  flex: 1;
  flex-grow: 1;
  flex-shrink: 1;
  flex-basis: 0%;
  padding: 12px;
  background-color: #f0f5ff;
  min-height: calc(100vh - 100px);
  overflow-x: hidden;
} */

.department-management {
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
  margin-bottom: 20px;
  padding: 12px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.search-form {
  display: flex;
  flex-wrap: nowrap;
  align-items: center;
}

.search-buttons {
  display: flex;
  gap: 4px;
  white-space: nowrap;
}

.rating-info {
  display: flex;
  align-items: center;
  flex-direction: column;
  justify-content: center;
  gap: 4px;
}

.review-count {
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
  flex-direction: column;
  gap: 4px;
}

.button-row {
  display: flex;
  gap: 4px;
}

.button-row .el-button {
  flex: 1;
  padding: 4px 8px;
  min-width: 0;
}

.button-row .el-button + .el-button {
  margin-left: 0;
}

:deep(.el-rate) {
  display: inline-flex;
  align-items: center;
}

:deep(.el-rate__text) {
  font-size: 13px;
  margin-left: 8px;
}

.doctor-management {
  .doctor-header {
    margin-bottom: 16px;
  }

  .doctor-title {
    font-size: 16px;
    color: #303133;
  }

  .doctor-info {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .info-content {
    display: flex;
    flex-direction: column;
    gap: 4px;
  }

  .doctor-name {
    font-weight: 500;
    color: #303133;
    font-size: 14px;
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
}

:deep(.el-table .cell) {
  padding-left: 8px;
  padding-right: 8px;
}

.search-input :deep(.el-input__wrapper) {
  height: 32px;
  line-height: 32px;
}

.search-input :deep(.el-input__inner) {
  height: 32px;
  line-height: 32px;
}

.doctor-dialog {
  .doctor-form {
    max-width: 500px;
    margin: 0 auto;
  }

  .form-section {
    margin-top: 20px;
    
    .section-title {
      font-size: 16px;
      font-weight: 500;
      color: #303133;
      margin-bottom: 16px;
      padding-bottom: 8px;
      border-bottom: 1px solid #EBEEF5;
    }

    .form-content {
      padding: 0 8px;
    }
  }

  .gender-group {
    .gender-icon {
      margin-right: 4px;
      vertical-align: middle;
    }
  }

  .age-input {
    width: 100%;
  }
}

.expertise-info {
  display: flex;
  align-items: center;
  width: 100%;
  padding: 4px 0;
}

.expertise-name {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #303133;
}

.associated-tag {
  margin-left: 8px;
}

.operation-buttons {
  display: flex;
  justify-content: center;
}

.operation-buttons .el-button {
  padding: 4px 12px;
}

.dialog-title-area {
  padding: 0 20px;
}

.dialog-title-area h3 {
  font-size: 16px;
  color: #303133;
}

.doctor-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.info-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.doctor-name {
  font-weight: 500;
  color: #303133;
  font-size: 14px;
}

.expertise-count {
  margin-top: 2px;
}

.expertise-count .el-tag {
  font-size: 12px;
  height: 20px;
  line-height: 18px;
  padding: 0 6px;
}
</style>