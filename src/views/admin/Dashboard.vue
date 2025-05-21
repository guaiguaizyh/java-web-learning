<template>
  <div class="dashboard">
    <!-- 数据概览卡片 -->
    <!-- 这里的el-row和el-col全部删除 -->
    <!-- 满意度分析区域 -->
    <el-row :gutter="24" class="satisfaction-section">
      <el-col :span="24">
        <el-card shadow="hover" class="satisfaction-card">
          <template #header>
            <div class="section-header">
              <span class="section-title">满意度分析</span>
              <el-icon><DataAnalysis /></el-icon>
            </div>
          </template>
          <div class="satisfaction-content">
            <!-- 总体满意度 -->
            <el-row :gutter="20" class="satisfaction-stats">
              <el-col :span="6">
                <el-card shadow="hover">
                  <template #header>
                    <div class="card-header">
                      <span>总评价数</span>
                    </div>
                  </template>
                  <div class="card-content">
                    <span class="number">{{ globalStats.totalReviews || 0 }}</span>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card shadow="hover">
                  <template #header>
                    <div class="card-header">
                      <span>平均评分</span>
                    </div>
                  </template>
                  <div class="card-content">
                    <span class="number">{{ (globalStats.averageRating || 0).toFixed(1) }}</span>
                    <el-rate
                      v-model="globalStats.averageRating"
                      disabled
                      show-score
                      text-color="#ff9900"
                      score-template=""
                    />
                  </div>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card shadow="hover">
                  <template #header>
                    <div class="card-header">
                      <span>好评率</span>
                    </div>
                  </template>
                  <div class="card-content">
                    <span class="number">{{ (globalStats.satisfactionRate || 0).toFixed(1) }}%</span>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card shadow="hover">
                  <template #header>
                    <div class="card-header">
                      <span>高评分数量</span>
                    </div>
                  </template>
                  <div class="card-content">
                    <span class="number">{{ globalStats.highRatingCount || 0 }}</span>
                </div>
                </el-card>
              </el-col>
            </el-row>

            <!-- 评分最高的医生 -->
            <el-row :gutter="20" class="top-doctors">
              <el-col :span="24">
                <div class="detail-card">
                  <div class="detail-header">
                    <span class="detail-title">评分最高的医生</span>
                  </div>
                  <div class="detail-content">
                    <el-table :data="topDoctors" style="width: 100%" border stripe>
                      <el-table-column prop="doctorName" label="医生姓名" />
                      <el-table-column prop="department" label="所属科室" />
                      <el-table-column prop="reviewCount" label="评价数量" />
                      <el-table-column prop="averageRating" label="平均评分">
                        <template #default="scope">
                          {{ scope.row.averageRating.toFixed(1) }}
                        </template>
                      </el-table-column>
                      <el-table-column label="评分">
                        <template #default="scope">
                          <el-rate
                            v-model="scope.row.averageRating"
                            disabled
                            show-score
                            text-color="#ff9900"
                            score-template=""
                          />
                        </template>
                      </el-table-column>
                      <el-table-column prop="satisfactionRate" label="满意度">
                        <template #default="scope">
                          <el-progress 
                            :percentage="scope.row.satisfactionRate" 
                            :color="getSatisfactionColor(scope.row.satisfactionRate)"
                          />
                        </template>
                      </el-table-column>
                    </el-table>
                  </div>
                </div>
              </el-col>
            </el-row>

            <!-- 满意度趋势 -->
            <el-row :gutter="20" class="satisfaction-trend">
              <el-col :span="24">
                <div class="detail-card">
              <div class="detail-header">
                <span class="detail-title">满意度趋势</span>
              </div>
                  <div class="detail-content">
                    <div ref="trendChartRef" class="trend-chart"></div>
              </div>
            </div>
              </el-col>
            </el-row>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { 
  User, 
  UserFilled, 
  OfficeBuilding, 
  ChatDotRound,
  Check,
  Warning,
  QuestionFilled,
  DataAnalysis
} from '@element-plus/icons-vue'
import request from '@/utils/axios'
import { ElMessage, ElLoading } from 'element-plus'
import { 
  getTopDoctors, 
  getSatisfactionStats, 
  getRatingDistribution, 
  getRatingTrend 
} from '@/api/satisfaction'
import * as echarts from 'echarts'

const router = useRouter()

// 路由跳转
const goToRoute = (path) => {
  router.push(path)
}

// 统计数据
const statistics = ref({
  userCount: 0,
  doctorCount: 0,
  departmentCount: 0,
  reviewCount: 0
})

// 满意度数据默认值
const defaultSatisfactionData = [
  {
    label: '非常满意',
    value: 0,
    icon: 'Check',
    color: '#67C23A'
  },
  {
    label: '满意',
    value: 0,
    icon: 'Check',
    color: '#409EFF'
  },
  {
    label: '一般',
    value: 0,
    icon: 'QuestionFilled',
    color: '#E6A23C'
  },
  {
    label: '不满意',
    value: 0,
    icon: 'Warning',
    color: '#F56C6C'
  }
]

// 满意度数据
const satisfactionData = ref([...defaultSatisfactionData])
// 科室满意度数据
const departmentSatisfaction = ref([])
// 医生满意度数据
const doctorSatisfaction = ref([])
// 满意度趋势数据
const satisfactionTrend = ref([])
// 评分分布数据
const ratingDistribution = ref([])
// 顶级医生数据
const topDoctors = ref([])
// 全局统计数据
const globalStats = ref({
  totalReviews: 0,
  averageRating: 0,
  highRatingCount: 0,
  satisfactionRate: 0
})

// 图表实例
const trendChartRef = ref(null)
let trendChart = null

// 获取满意度颜色
const getSatisfactionColor = (percentage) => {
  if (percentage >= 90) return '#67C23A'
  if (percentage >= 80) return '#409EFF'
  if (percentage >= 70) return '#E6A23C'
  return '#F56C6C'
}

// 格式化日期函数
const formatDate = (date) => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 获取五年前的日期
const getFiveYearsAgoDate = () => {
  const now = new Date()
  const fiveYearsAgo = new Date(now)
  fiveYearsAgo.setFullYear(now.getFullYear() - 5)
  return fiveYearsAgo
}

// 获取顶级医生数据
const fetchTopDoctors = async () => {
  try {
    const res = await getTopDoctors({ limit: 5 })
    if (res.code === 200) {
      topDoctors.value = res.data.map(doctor => ({
        doctorId: doctor.doctor_id,
        doctorName: doctor.doctor_name,
        department: doctor.department_name,
        reviewCount: doctor.reviewCount,
        averageRating: doctor.averageRating,
        satisfactionRate: doctor.satisfactionRate
      }))
    }
  } catch (error) {
    console.error('获取顶级医生数据失败:', error)
  }
}

// 获取全局统计数据
const fetchGlobalStats = async () => {
  try {
    const now = new Date()
    const fiveYearsAgo = getFiveYearsAgoDate()
    
    const params = {
      startDate: formatDate(fiveYearsAgo),
      endDate: formatDate(now)
    }
    
    const res = await getSatisfactionStats(params)
    if (res.code === 200) {
      globalStats.value = res.data
    }
  } catch (error) {
    console.error('获取全局统计数据失败:', error)
  }
}

// 获取评分分布数据
const fetchRatingDistribution = async () => {
  try {
    const res = await getRatingDistribution()
    if (res.code === 200) {
      ratingDistribution.value = res.data
      
      // 更新满意度数据
      const distribution = {}
      res.data.forEach(item => {
        distribution[item.rating] = item.percentage
      })
      
    satisfactionData.value = [
      {
        label: '非常满意',
          value: distribution[5] || 0,
        icon: 'Check',
        color: '#67C23A'
      },
      {
        label: '满意',
          value: distribution[4] || 0,
        icon: 'Check',
        color: '#409EFF'
      },
      {
        label: '一般',
          value: distribution[3] || 0,
        icon: 'QuestionFilled',
        color: '#E6A23C'
      },
      {
        label: '不满意',
          value: (distribution[2] || 0) + (distribution[1] || 0),
        icon: 'Warning',
        color: '#F56C6C'
      }
    ]
    }
  } catch (error) {
    console.error('获取评分分布数据失败:', error)
  }
}

// 获取评分趋势数据
const fetchRatingTrend = async () => {
  try {
    const now = new Date()
    const fiveYearsAgo = getFiveYearsAgoDate()
    
    const params = {
      startDate: formatDate(fiveYearsAgo),
      endDate: formatDate(now),
      periodType: 'month'
    }
    
    const res = await getRatingTrend(params)
    if (res.code === 200) {
      satisfactionTrend.value = res.data.map(item => ({
        date: item.date,
        reviewCount: item.reviewCount,
        averageRating: item.averageRating,
        satisfactionRate: item.satisfactionRate
      }))
      
      // 渲染趋势图表
      nextTick(() => {
        renderTrendChart()
      })
    }
  } catch (error) {
    console.error('获取评分趋势数据失败:', error)
  }
}

// 渲染趋势图表
const renderTrendChart = () => {
  if (!trendChartRef.value) return
  
  // 如果已有图表实例，先销毁
  if (trendChart) {
    trendChart.dispose()
  }
  
  // 创建新图表
  trendChart = echarts.init(trendChartRef.value)
  
  // 准备数据
  const dates = satisfactionTrend.value.map(item => item.date)
  const satisfactionRates = satisfactionTrend.value.map(item => item.satisfactionRate)
  const averageRatings = satisfactionTrend.value.map(item => item.averageRating)
  const reviewCounts = satisfactionTrend.value.map(item => item.reviewCount)
  
  // 图表配置
  const option = {
    title: {
      text: '5年满意度趋势分析',
      left: 'center'
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross',
        crossStyle: {
          color: '#999'
        }
      }
    },
    legend: {
      data: ['满意度(%)', '平均评分', '评价数量'],
      top: 30
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true,
      top: 80
    },
    dataZoom: [
      {
        type: 'slider',
        show: true,
        start: 50,
        end: 100,
        bottom: 10
      },
      {
        type: 'inside',
        start: 50,
        end: 100
      }
    ],
    xAxis: [
      {
        type: 'category',
        data: dates,
        axisPointer: {
          type: 'shadow'
        },
        axisLabel: {
          rotate: 45,
          interval: 'auto'
        }
      }
    ],
    yAxis: [
      {
        type: 'value',
        name: '满意度/评分',
        min: 0,
        max: 100,
        interval: 20,
        axisLabel: {
          formatter: '{value}'
        }
      },
      {
        type: 'value',
        name: '评价数量',
        axisLabel: {
          formatter: '{value}'
        }
      }
    ],
    series: [
      {
        name: '满意度(%)',
        type: 'line',
        smooth: true,
        data: satisfactionRates,
        itemStyle: {
          color: '#409EFF'
        },
        markPoint: {
          data: [
            { type: 'max', name: '最高值' },
            { type: 'min', name: '最低值' }
          ]
        },
        markLine: {
          data: [
            { type: 'average', name: '平均值' }
          ]
        }
      },
      {
        name: '平均评分',
        type: 'line',
        smooth: true,
        data: averageRatings.map(rating => rating * 20), // 转换为百分比方便展示
        itemStyle: {
          color: '#67C23A'
        },
        markLine: {
          data: [
            { type: 'average', name: '平均值' }
          ]
        }
      },
      {
        name: '评价数量',
        type: 'bar',
        yAxisIndex: 1,
        data: reviewCounts,
        itemStyle: {
          color: '#E6A23C'
        }
      }
    ]
  }
  
  // 设置图表配置
  trendChart.setOption(option)
  
  // 窗口大小变化时自动调整图表大小
  window.addEventListener('resize', () => {
    trendChart && trendChart.resize()
  })
}

// 页面卸载时清理图表实例
const onUnmounted = () => {
  if (trendChart) {
    trendChart.dispose()
    trendChart = null
  }
  window.removeEventListener('resize', () => {
    trendChart && trendChart.resize()
  })
}

// 获取数据统计
const fetchStatistics = async () => {
  const loading = ElLoading.service({
    lock: true,
    text: '加载中...',
    background: 'rgba(0, 0, 0, 0.7)'
  })

  try {
    const res = await request({
      url: '/admin/statistics',
      method: 'get'
    })
    
    if (res.code === 200) {
      statistics.value = res.data
    } else {
      ElMessage.error(res.msg || '获取统计数据失败')
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
    ElMessage.error('获取统计数据失败')
  } finally {
    loading.close()
  }
}

// 初始化数据
const initData = async () => {
  await fetchGlobalStats()
  await fetchRatingDistribution()
  await fetchRatingTrend()
  await fetchTopDoctors()
}

// 页面加载时获取数据
onMounted(() => {
  initData()
})
</script>

<style scoped>
.dashboard {
  padding: 24px;
  min-width: 1000px;
  max-width: 1400px;
  margin: 0 auto;
  background-color: #f5f7fa;
}

.data-overview {
  margin-bottom: 24px;
}

.data-card {
  height: 100%;
  transition: all 0.3s ease;
  border-radius: 8px;
  border: none;
  background: linear-gradient(to bottom right, #ffffff, #f8f9fa);
}

.data-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #606266;
  border-bottom: none;
  padding: 16px 20px;
}

.header-icon {
  font-size: 24px;
  color: #409EFF;
}

.card-content {
  text-align: center;
  padding: 24px 20px;
}

.number {
  font-size: 36px;
  font-weight: 600;
  color: #303133;
  margin-right: 8px;
  background: linear-gradient(45deg, #409EFF, #36D1DC);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.label {
  font-size: 14px;
  color: #909399;
}

.satisfaction-section {
  margin-top: 24px;
}

.satisfaction-card {
  border-radius: 8px;
  border: none;
  background: #ffffff;
}

.satisfaction-content {
  padding: 24px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.satisfaction-stats {
  margin-bottom: 20px;
}

.rating-distribution,
.satisfaction-trend,
.top-doctors {
  margin-top: 20px;
}

.detail-card {
  background-color: #f5f7fa;
  border-radius: 4px;
  padding: 15px;
}

.detail-header {
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 10px;
  margin-bottom: 15px;
}

.detail-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.satisfaction-overview {
  display: flex;
  justify-content: space-around;
  margin-bottom: 20px;
}

.satisfaction-item {
  display: flex;
  align-items: center;
  background-color: #fff;
  border-radius: 4px;
  padding: 15px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.satisfaction-icon {
  margin-right: 10px;
}

.satisfaction-value {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}

.satisfaction-label {
  font-size: 14px;
  color: #606266;
}

.trend-chart {
  height: 400px;
  width: 100%;
}
</style> 