import request from '@/utils/axios'

// 获取所有职称信息
export const getAllPositions = () => {
  return request({
    url: '/positions',
    method: 'get'
  })
}

// 前端页面使用的获取职称列表的简化方法
export const getPositions = () => {
  return request({
    url: '/positions',
    method: 'get'
  })
}

// 获取职称分布统计
export const getPositionsDistribution = () => {
  return request({
    url: '/positions/distribution',
    method: 'get'
  })
}

// 获取职称详情
export const getPositionById = (positionsId) => {
  return request({
    url: `/positions/${positionsId}`,
    method: 'get'
  })
}

// 添加职称
export const addPosition = (data) => {
  return request({
    url: '/positions',
    method: 'post',
    data: {
      positionsName: data.positionsName,
      description: data.description,
      sortOrder: data.sortOrder,
      status: data.status
    }
  })
}

// 更新职称
export const updatePosition = (positionsId, data) => {
  return request({
    url: `/positions/${positionsId}`,
    method: 'put',
    data: {
      positionsName: data.positionsName,
      description: data.description,
      sortOrder: data.sortOrder,
      status: data.status
    }
  })
}

// 删除职称
export const deletePosition = (positionsId) => {
  return request({
    url: `/positions/${positionsId}`,
    method: 'delete'
  })
} 