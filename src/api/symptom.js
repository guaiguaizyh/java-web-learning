import request from '@/utils/axios'

// 获取症状列表
export const getSymptomList = (params) => {
  return request({
    url: '/symptoms',
    method: 'get',
    params
  })
}

// 获取症状详情
export const getSymptomById = (symptomId) => {
  return request({
    url: `/symptoms/${symptomId}`,
    method: 'get'
  })
}

// 搜索症状
export const searchSymptoms = (keyword) => {
  return request({
    url: '/symptoms/search',
    method: 'get',
    params: { keyword }
  })
}

// 添加症状
export const addSymptom = (data) => {
  return request({
    url: '/symptoms',
    method: 'post',
    data
  })
}

// 更新症状
export const updateSymptom = (symptomId, data) => {
  return request({
    url: `/symptoms/${symptomId}`,
    method: 'put',
    data
  })
}

// 删除症状
export const deleteSymptom = (symptomId) => {
  return request({
    url: `/symptoms/${symptomId}`,
    method: 'delete'
  })
}

// 获取专长关联的症状列表
export const getExpertiseSymptoms = (expertiseId) => {
  return request({
    url: `/expertises/${expertiseId}/symptoms`,
    method: 'get'
  })
}

// 更新专长关联的症状
export const updateExpertiseSymptoms = (expertiseId, data) => {
  return request({
    url: `/expertises/${expertiseId}/symptoms`,
    method: 'put',
    data
  })
}

// 获取症状关联的专长列表
export const getSymptomExpertises = (symptomId) => {
  return request({
    url: `/symptoms/${symptomId}/expertises`,
    method: 'get'
  })
}

// 批量删除症状
export const batchDeleteSymptoms = (ids) => {
  return request({
    url: '/symptoms/batch',
    method: 'delete',
    data: { ids }
  })
} 