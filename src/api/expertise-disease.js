import request from '@/utils/axios'

// 获取专长病症关联列表
export const getExpertiseDiseaseList = (params) => {
  return request({
    url: '/expertise-diseases',
    method: 'get',
    params
  })
}

// 更新单个专长病症关联
export const updateExpertiseDisease = (data) => {
  return request({
    url: '/expertise-diseases',
    method: 'put',
    data
  })
}

// 批量更新某专长的病症关联
export const updateExpertiseDiseases = (expertiseId, data) => {
  return request({
    url: `/expertise-diseases/expertise/${expertiseId}`,
    method: 'put',
    data
  })
}

// 批量更新某病症的专长关联
export const updateDiseaseExpertises = (diseaseId, data) => {
  return request({
    url: `/expertise-diseases/disease/${diseaseId}`,
    method: 'put',
    data
  })
} 