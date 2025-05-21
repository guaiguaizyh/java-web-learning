import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'

// 预导入组件
const Profile = () => import('../views/Profile.vue')
const Home = () => import('../views/Home.vue')
const Login = () => import('../views/Login.vue')
const Register = () => import('../views/Register.vue')
const Doctors = () => import('../views/Doctors.vue')
const DoctorRecommend = () => import('../views/DoctorRecommend.vue')
const AIChat = () => import('../views/AIChat.vue')
const AdminDashboard = () => import('../views/admin/Dashboard.vue')
const UserManagement = () => import('../views/admin/UserManagement.vue')
const DoctorManagement = () => import('../views/admin/DoctorManagement.vue')
const DepartmentManagement = () => import('../views/admin/DepartmentManagement.vue')

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/login'
    },
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/Login.vue'),
      meta: {
        title: '登录',
        requiresAuth: false
      }
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('@/views/Register.vue'),
      meta: {
        title: '注册',
        requiresAuth: false
      }
    },
    {
      path: '/',
      component: () => import('@/layouts/MainLayout.vue'),
      children: [
        {
          path: 'home',
          name: 'Home',
          component: () => import('@/views/Home.vue'),
          meta: {
            title: '首页',
            requiresAuth: true
          }
        },
        {
          path: 'profile',
          name: 'Profile',
          component: () => import('@/views/Profile.vue'),
          meta: {
            title: '个人信息',
            requiresAuth: true
          }
        },
        {
          path: 'change-password',
          name: 'ChangePassword',
          component: () => import('@/views/ChangePassword.vue'),
          meta: {
            title: '修改密码',
            requiresAuth: true
          }
        },
        {
          path: 'doctors',
          name: 'Doctors',
          component: () => import('@/views/Doctors.vue'),
          meta: {
            title: '医生列表',
            requiresAuth: true
          }
        },
        {
          path: 'recommend',
          name: 'DoctorRecommend',
          component: () => import('@/views/DoctorRecommend.vue'),
          meta: {
            title: '医生推荐',
            requiresAuth: true
          }
        },
        {
          path: 'ai-chat',
          name: 'AIChat',
          component: () => import('@/views/AIChat.vue'),
          meta: {
            title: 'AI 医疗咨询',
            requiresAuth: true
          }
        }
      ]
    },
    {
      path: '/admin',
      component: () => import('@/views/admin/AdminLayout.vue'),
      meta: {
        requiresAuth: true,
        requiresAdmin: true
      },
      children: [
        {
          path: '',
          redirect: '/admin/dashboard'
        },
        {
          path: 'dashboard',
          name: 'Dashboard',
          component: () => import('@/views/admin/Dashboard.vue'),
          meta: {
            title: '管理控制台',
            requiresAuth: true,
            requiresAdmin: true
          }
        },
        {
          path: 'users',
          name: 'UserManagement',
          component: () => import('@/views/admin/UserManagement.vue'),
          meta: {
            title: '用户管理',
            requiresAuth: true,
            requiresAdmin: true
          }
        },
        {
          path: 'doctors',
          name: 'DoctorManagement',
          component: () => import('@/views/admin/DoctorManagement.vue'),
          meta: {
            title: '医生管理',
            requiresAuth: true,
            requiresAdmin: true
          }
        },
        {
          path: 'departments',
          name: 'DepartmentManagement',
          component: () => import('@/views/admin/DepartmentManagement.vue'),
          meta: {
            title: '科室管理',
            requiresAuth: true,
            requiresAdmin: true
          }
        },
        {
          path: 'expertise',
          name: 'ExpertiseManagement',
          component: () => import('@/views/admin/ExpertiseManagement.vue'),
          meta: { 
            title: '专长管理',
            icon: 'Star',
            requiresAuth: true,
            requiresAdmin: true
          }
        },
        {
          path: 'reviews',
          name: 'ReviewManagement',
          component: () => import('@/views/admin/ReviewManagement.vue'),
          meta: {
            title: '评价管理',
            icon: 'Star',
            requiresAuth: true,
            requiresAdmin: true
          }
        },
        {
          path: 'symptoms',
          name: 'SymptomManagement',
          component: () => import('@/views/admin/SymptomManagement.vue'),
          meta: {
            title: '症状管理',
            icon: 'List',
            requiresAuth: true,
            requiresAdmin: true
          }
        }
      ]
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'NotFound',
      component: () => import('@/views/NotFound.vue'),
      meta: {
        title: '404',
        requiresAuth: false
      }
    }
  ]
})

// 全局前置守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  const isLoggedIn = userStore.isLoggedIn
  const role = userStore.role

  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - 医生推荐系统` : '医生推荐系统'

  // 处理登录和注册页面
  if (to.path === '/login' || to.path === '/register') {
    if (isLoggedIn) {
      // 如果已登录，重定向到对应的首页
      next(role === 'admin' ? '/admin/dashboard' : '/home')
      return
    }
    // 未登录时可以访问登录和注册页
    next()
    return
  }

  // 检查是否需要登录
  if (to.meta.requiresAuth && !isLoggedIn) {
    ElMessage({
      message: '请先登录',
      type: 'warning',
      duration: 2000
    })
    next({
      path: '/login',
      query: { redirect: to.fullPath }
    })
    return
  }

  // 基于角色判断是否显示管理页面
  // 注意：此处只是用于UI导航，不作为权限控制
  // 后端已移除权限控制，所有接口只要有JWT就可以访问
  if (to.path.startsWith('/admin') && role !== 'admin') {
    ElMessage({
      message: '此页面仅对管理员显示',
      type: 'info',
      duration: 2000
    })
    next('/home')
    return
  }
  
  next()
})
export default router 