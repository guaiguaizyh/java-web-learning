import request from '@/utils/axios'

// 获取科室列表
export const getDepartments = (params) => {
  return request({
    url: '/departments/list',
    method: 'get',
    params: {
      page: params?.page || 1,
      size: params?.size || 10,
      ...params
    }
  })
}

// 获取科室详情
export const getDepartmentById = (id) => {
  return request({
    url: `/departments/${id}`,
    method: 'get'
  })
}

// 添加科室
export const addDepartment = (data) => {
  return request({
    url: '/departments',
    method: 'post',
    data
  })
}

// 更新科室信息
export const updateDepartment = (id, data) => {
  return request({
    url: `/departments/${id}`,
    method: 'put',
    data
  })
}

// 删除科室
export const deleteDepartment = (id) => {
  return request({
    url: `/departments/${id}`,
    method: 'delete'
  })
}

// 获取科室医生列表
export const getDepartmentDoctors = (departmentId, params) => {
  return request({
    url: `/departments/${departmentId}/doctors`,
    method: 'get',
    params: {
      page: params?.page || 1,
      size: params?.size || 10,
      ...params
    }
  })
}

// 高级查询科室列表
export const searchDepartments = (params) => {
  return request({
    url: '/departments/search',
    method: 'get',
    params: {
      page: params?.page || 1,
      size: params?.size || 10,
      ...params
    }
  })
}

/**
 * 获取科室专长列表
 * @param {number|string} departmentId - 科室ID
 * @returns {Promise} - 返回API请求Promise
 */
export const getDepartmentExpertises = (departmentId) => {
  if (!departmentId) {
    return Promise.reject(new Error('科室ID不能为空'))
  }
  return request({
    url: `/expertises/department/${departmentId}`,
    method: 'get'
  })
} 