import request from '@/utils/axios'

/**
 * 获取医生评价统计
 * @param {Number} doctorId 医生ID
 * @returns {Promise}
 */
export const getDoctorSatisfaction = (doctorId) => {
  return request({
    url: `/satisfaction/doctor/${doctorId}`,
    method: 'get'
  })
}

/**
 * 获取医生评价趋势
 * @param {Number} doctorId 医生ID
 * @param {Object} params 查询参数 {periodType, startDate, endDate}
 * @returns {Promise}
 */
export const getDoctorSatisfactionTrend = (doctorId, params) => {
  return request({
    url: `/satisfaction/doctor/${doctorId}/trend`,
    method: 'get',
    params
  })
}

/**
 * 获取科室评价统计
 * @param {Number} departmentId 科室ID
 * @returns {Promise}
 */
export const getDepartmentSatisfaction = (departmentId) => {
  return request({
    url: `/satisfaction/department/${departmentId}`,
    method: 'get'
  })
}

/**
 * 获取科室评分分布
 * @param {Number} departmentId 科室ID
 * @returns {Promise}
 */
export const getDepartmentDistribution = (departmentId) => {
  return request({
    url: `/satisfaction/department/${departmentId}/distribution`,
    method: 'get'
  })
}

/**
 * 获取科室评价趋势
 * @param {Number} departmentId 科室ID
 * @param {Object} params 查询参数 {periodType, startDate, endDate}
 * @returns {Promise}
 */
export const getDepartmentSatisfactionTrend = (departmentId, params) => {
  return request({
    url: `/satisfaction/department/${departmentId}/trend`,
    method: 'get',
    params
  })
}

/**
 * 获取科室医生评分排名
 * @param {Number} departmentId 科室ID
 * @returns {Promise}
 */
export const getDepartmentDoctorsRanking = (departmentId) => {
  return request({
    url: `/satisfaction/department/${departmentId}/doctors`,
    method: 'get'
  })
}

/**
 * 获取评分最高的医生
 * @param {Object} params 查询参数 {limit}
 * @returns {Promise}
 */
export const getTopDoctors = (params) => {
  return request({
    url: '/satisfaction/top-doctors',
    method: 'get',
    params
  })
}

/**
 * 获取时间段评价统计
 * @param {Object} params 查询参数 {startDate, endDate}
 * @returns {Promise}
 */
export const getSatisfactionStats = (params) => {
  return request({
    url: '/satisfaction/stats',
    method: 'get',
    params
  })
}

/**
 * 获取全局评分分布
 * @returns {Promise}
 */
export const getRatingDistribution = () => {
  return request({
    url: '/satisfaction/rating/distribution',
    method: 'get'
  })
}

/**
 * 获取评分趋势
 * @param {Object} params 查询参数 {months}
 * @returns {Promise}
 */
export const getRatingTrend = (params) => {
  return request({
    url: '/satisfaction/rating/trend',
    method: 'get',
    params
  })
} 