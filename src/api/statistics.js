import request from '@/utils/axios'

// 获取控制台统计数据
export const getStatistics = () => {
  return request({
    url: '/statistics',
    method: 'get'
  })
} 