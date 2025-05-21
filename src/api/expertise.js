import request from '@/utils/axios'

// 获取专长列表
export const getExpertiseList = (params) => {
  return request({
    url: '/expertises',
    method: 'get',
    params
  })
}

// 获取专长详情
export const getExpertiseById = (id) => {
  return request({
    url: `/expertises/${id}`,
    method: 'get'
  })
}

// 添加专长
export const addExpertise = (data) => {
  return request({
    url: '/expertises',
    method: 'post',
    data
  })
}

// 更新专长信息
export const updateExpertise = (id, data) => {
  return request({
    url: `/expertises/${id}`,
    method: 'put',
    data
  })
}

// 删除专长
export const deleteExpertise = (id) => {
  return request({
    url: `/expertises/${id}`,
    method: 'delete'
  })
}

// 批量删除专长
export const batchDeleteExpertise = (ids) => {
  return request({
    url: '/expertises/batch',
    method: 'delete',
    headers: {
      'Content-Type': 'application/json'
    },
    data: ids
  })
}

// 获取专长关联的症状
export const getExpertiseSymptoms = (expertiseId) => {
  return request({
    url: `/expertises/${expertiseId}/symptoms`,
    method: 'get'
  })
}

// 更新专长关联的症状
export const updateExpertiseSymptoms = (expertiseId, data) => {
  console.log('更新专长症状关联:', { expertiseId, data });
  return request({
    url: `/expertises/${expertiseId}/symptoms`,
    method: 'put',
    headers: {
      'Content-Type': 'application/json'
    },
    data
  })
}

// 获取专长关联的医生
export const getExpertiseDoctors = (expertiseId) => {
  return request({
    url: `/expertises/${expertiseId}/doctors`,
    method: 'get'
  })
}

// 更新专长关联的医生
export const updateExpertiseDoctors = (expertiseId, doctors) => {
  console.log('API调用数据:', { expertiseId, doctors });
  return request({
    url: `/expertises/${expertiseId}/doctors`,
    method: 'put',
    headers: {
      'Content-Type': 'application/json'
    },
    data: doctors
  })
}

// 获取医生专长列表
export const getDoctorExpertises = (doctorId) => {
  return request({
    url: `/expertises/doctor/${doctorId}`,
    method: 'get'
  })
}

// 更新医生专长
export const updateDoctorExpertises = (doctorId, data) => {
  return request({
    url: `/expertises/doctor/${doctorId}`,
    method: 'put',
    data
  })
}

// 获取科室专长列表
export const getDepartmentExpertises = (departmentId) => {
  return request({
    url: `/expertises/department/${departmentId}`,
    method: 'get'
  })
} 