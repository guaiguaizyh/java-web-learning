import request from '@/utils/axios'

// 获取用户列表
export const getUserList = (params) => {
  return request({
    url: '/users',
    method: 'get',
    params
  })
}

// 获取用户详情
export const getUserDetail = (userId) => {
  return request({
    url: `/users/${userId}`,
    method: 'get'
  })
}

// 添加用户
export const addUser = (data) => {
  return request({
    url: '/users',
    method: 'post',
    data
  })
}

// 更新用户
export const updateUser = (userId, data) => {
  return request({
    url: `/users/${userId}`,
    method: 'put',
    data
  })
}

// 删除用户
export const deleteUser = (userId) => {
  return request({
    url: `/users/${userId}`,
    method: 'delete'
  })
} 