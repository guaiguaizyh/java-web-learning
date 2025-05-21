import request from '@/utils/axios'

// 获取医生列表
export const getDoctors = (params) => {
  return request({
    url: '/doctors',
    method: 'get',
    params
  })
}

// 获取科室的医生列表
export const getDepartmentDoctors = (departmentId, params) => {
  if (!departmentId) {
    return Promise.reject(new Error('科室ID不能为空'))
  }
  return request({
    url: '/doctors',
    method: 'get',
    params: {
      ...params,
      departmentId
    }
  })
}

// 获取医生详情
export const getDoctorById = (id) => {
  return request({
    url: `/doctors/${id}`,
    method: 'get'
  })
}

// 添加医生
export const addDoctor = (data) => {
  return request({
    url: '/doctors',
    method: 'post',
    data
  })
}

// 更新医生信息
export const updateDoctor = (id, data) => {
  return request({
    url: `/doctors/${id}`,
    method: 'put',
    data
  })
}

// 删除医生
export const deleteDoctor = (id) => {
  return request({
    url: `/doctors/${id}`,
    method: 'delete'
  })
}

// 添加医生到科室
export const addDepartmentDoctor = (doctorId, departmentId) => {
  if (!doctorId || !departmentId) {
    return Promise.reject(new Error('医生ID和科室ID不能为空'))
  }
  return request({
    url: `/doctors/${doctorId}`,
    method: 'post',
    data: {
      departmentId
    }
  })
}

// 批量添加医生到科室
export const batchAddDepartmentDoctors = (doctorIds, departmentId) => {
  if (!Array.isArray(doctorIds) || doctorIds.length === 0) {
    return Promise.reject(new Error('请选择要添加的医生'))
  }
  if (!departmentId) {
    return Promise.reject(new Error('科室ID不能为空'))
  }
  return request({
    url: '/doctors/batch',
    method: 'put',
    data: {
      doctors: doctorIds.map(id => ({
        id,
        departmentId
      }))
    }
  })
}

// 取消医生与科室的关联
export const unlinkDepartmentDoctor = (doctorId) => {
  return request({
    url: `/doctors/${doctorId}/department`,
    method: 'put',
    data: {
      departmentId: null
    }
  })
}

// 批量取消医生与科室的关联
export const batchUnlinkDepartmentDoctors = async (doctorIds) => {
  if (!Array.isArray(doctorIds) || doctorIds.length === 0) {
    return Promise.reject(new Error('请选择要解除关联的医生'))
  }
  // 并发调用每个医生的取消关联接口
  await Promise.all(doctorIds.map(id => unlinkDepartmentDoctor(id)))
  return { code: 200, message: '批量取消关联成功' }
}