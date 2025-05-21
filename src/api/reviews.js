import request from '@/utils/axios'

/**
 * 获取医生评价列表
 * @param {number} doctorId - 医生ID
 * @param {number} page - 页码（默认1）
 * @param {number} size - 每页大小（默认10）
 */
export function getDoctorReviews(doctorId, page = 1, size = 10) {
  return request({
    url: `/reviews/doctor/${doctorId}?page=${page}&size=${size}`,
    method: 'get',
    headers: {
      'Content-Type': 'application/json'
    }
  })
}

/**
 * 删除评价
 * @param {number} id - 评价ID
 */
export function deleteReview(id) {
  if (!id) {
    return Promise.reject(new Error('评价ID不能为空'));
  }
  return request({
    url: `/reviews/${id}`,
    method: 'delete'
  })
}

/**
 * 管理评价列表
 * @param {Object} params - 查询参数
 * @param {number} [params.userId] - 用户ID
 * @param {number} [params.doctorId] - 医生ID
 * @param {string} [params.doctorName] - 医生姓名（模糊查询）
 * @param {string} [params.userName] - 用户姓名（模糊查询）
 * @param {number} [params.departmentId] - 科室ID
 * @param {number} [params.minRating] - 最低评分（1-5）
 * @param {number} [params.maxRating] - 最高评分（1-5）
 * @param {string} [params.content] - 评论内容关键词
 * @param {number} [params.page=1] - 页码
 * @param {number} [params.size=10] - 每页大小
 */
export function getReviewsList(params) {
  return request({
    url: '/reviews/manage',
    method: 'get',
    params: {
      page: 1,
      size: 10,
      ...params
    }
  })
}

/**
 * 批量删除评价
 * @param {number[]} ids - 评价ID数组
 */
export function batchDeleteReviews(ids) {
  // 使用 Promise.all 并行处理多个删除请求
  const deletePromises = ids.map(id => deleteReview(id));
  return Promise.all(deletePromises)
    .then(() => {
      return {
        code: 200,
        message: '批量删除成功'
      };
    })
    .catch(error => {
      throw error;
    });
}

/**
 * 更新评价
 * @param {number} id - 评价ID
 * @param {Object} data - 评价数据
 */
export function updateReview(id, data) {
  return request({
    url: `/reviews/${id}`,
    method: 'put',
    data
  })
}

/**
 * 获取评价统计信息
 */
export function getReviewsStatistics() {
  return request({
    url: '/reviews/statistics',
    method: 'get'
  })
}

// 获取单个评价
export const getReviewById = (id) => {
  return request({
    url: `/reviews/${id}`,
    method: 'get'
  })
}

// 添加评论
export const addReview = (data) => {
  return request({
    url: '/reviews',
    method: 'post',
    data
  })
} 