import { defineStore } from 'pinia'
import { login } from '../api/auth'
import { ElMessage } from 'element-plus'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo')) || null,
    role: localStorage.getItem('userRole') || ''
  }),
  getters: {
    isLoggedIn: (state) => !!state.token,
    isAdmin: (state) => state.role === 'admin',
    username: (state) => state.userInfo?.username || '',
    userId: (state) => state.userInfo?.id,
    authHeader: (state) => state.token ? `Bearer ${state.token}` : '',
    fullName: (state) => {
      const info = state.userInfo || {}
      return `${info.firstName || ''} ${info.lastName || ''}`.trim() || info.username || ''
    }
  },
  actions: {
    _updateStorage(token = null, user = null, role = null) {
      if (token !== null) {
        localStorage.setItem('token', token)
        this.token = token
      }
      if (user !== null) {
        localStorage.setItem('userInfo', JSON.stringify(user))
        this.userInfo = user
      }
      if (role !== null) {
        localStorage.setItem('userRole', role)
        this.role = role
      }
    },
    async loginAction(loginResponse) {
      try {
        const { token, user, role } = loginResponse
        this._updateStorage(token, user, role)
        return loginResponse
      } catch (error) {
        console.error('Store login error:', error)
        this.logout()
        throw error
      }
    },
    updateUserInfo(userData) {
      this.userInfo = {
        ...this.userInfo,
        ...userData
      }
      localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
    },
    logout() {
      this.token = ''
      this.userInfo = null
      this.role = ''
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      localStorage.removeItem('userRole')
    }
  }
}) 