import request from '@/utils/axios'

// 用户注册
export const register = (data) => {
  return request({
    url: '/register',
    method: 'post',
    data: {
      username: data.username,
      password: data.password,
      age: data.age,
      gender: data.gender,
      phone: data.phone,
      medicalRecord: data.medicalRecord
    }
  })
}

// 用户登录
export const login = (data) => {
  return request({
    url: '/login',
    method: 'post',
    data: {
      username: data.username,
      password: data.password
    }
  })
}

// 检查用户名是否可用
export const checkUsername = (username) => {
  return request({
    url: '/register/check-username',
    method: 'get',
    params: { username }
  })
}

// 获取用户信息
export const getUserInfo = () => {
  return request({
    url: '/users/info',
    method: 'get'
  })
}

// 更新用户信息
export const updateUserInfo = (data) => {
  return request({
    url: '/users/self',
    method: 'put',
    data: {
      age: data.age,
      gender: data.gender, // 性别值使用中文："男"或"女"
      phone: data.phone,
      medicalRecord: data.medicalRecord
    }
  })
}

// 修改密码
export const updatePassword = (data) => {
  return request({
    url: '/users/password',
    method: 'put',
    data: {
      oldPassword: data.oldPassword,
      newPassword: data.newPassword
    }
  })
} 