import axios from 'axios'

// 设置基础URL
const API_BASE_URL = process.env.VUE_APP_API_BASE_URL || 'http://localhost:8080'

// 创建axios实例
const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
api.interceptors.request.use(
  config => {
    // 可以在这里添加token等认证信息
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
api.interceptors.response.use(
  response => {
    return response
  },
  error => {
    if (error.response) {
      // 处理HTTP错误状态码
      switch (error.response.status) {
        case 401:
          // 未授权，跳转到登录页
          localStorage.removeItem('token')
          window.location.href = '/login'
          break
        case 403:
          console.error('权限不足')
          break
        case 404:
          console.error('请求的资源不存在')
          break
        case 500:
          console.error('服务器内部错误')
          break
        default:
          console.error('请求失败:', error.response.data.message || error.message)
      }
    } else {
      console.error('网络错误:', error.message)
    }
    return Promise.reject(error)
  }
)

// 加盟申请相关API
export const franchiseApplicationAPI = {
  // 提交加盟申请
  submitApplication(data) {
    return api.post('/api/franchise/applications/submit', data)
  },

  // 根据手机号查询申请
  searchByPhone(phone) {
    return api.get(`/api/franchise/applications/phone/${phone}`)
  },

  // 获取所有申请（管理员）
  getAllApplications(params = {}) {
    return api.get('/api/franchise/applications/all', { params })
  },

  // 根据状态获取申请
  getApplicationsByStatus(status, params = {}) {
    return api.get(`/api/franchise/applications/status/${status}`, { params })
  },

  // 根据ID获取申请详情
  getApplicationById(id) {
    return api.get(`/api/franchise/applications/${id}`)
  },

  // 审核申请
  reviewApplication(id, data) {
    return api.put(`/api/franchise/applications/${id}/review`, data)
  },

  // 搜索申请（根据申请人姓名或店铺名称）
  searchApplications(keyword, params = {}) {
    return api.get(`/api/franchise/applications/search?keyword=${keyword}`, { params })
  },

  // 获取申请统计信息
  getApplicationStats() {
    return api.get('/api/franchise/applications/statistics')
  },

  // 更新申请状态
  updateApplicationStatus(id, data) {
    return api.put(`/api/franchise/applications/${id}/status`, data)
  },

  // 发送通知
  sendNotification(id, data) {
    return api.post(`/api/franchise/applications/${id}/notify`, data)
  },
  
  // 获取申请的通知记录
  getApplicationNotifications(id) {
    return api.get(`/api/franchise/applications/${id}/notifications`)
  },
  
  // 根据手机号获取通知记录
  getNotificationsByPhone(phone) {
    return api.get(`/api/franchise/applications/notifications/phone/${phone}`)
  },

  // 删除申请
  deleteApplication(id) {
    return api.delete(`/api/franchise/applications/${id}`)
  },

  // 根据投资金额范围查询
  getApplicationsByAmountRange(minAmount, maxAmount, params = {}) {
    return api.get('/api/franchise/applications/amount-range', {
      params: { minAmount, maxAmount, ...params }
    })
  },

  // 根据经纬度查询附近的申请
  getNearbyApplications(latitude, longitude, radius = 10, params = {}) {
    return api.get('/api/franchise/applications/nearby', {
      params: { latitude, longitude, radius, ...params }
    })
  }
}

// 加盟店铺相关API
export const franchiseStoreAPI = {
  // 创建店铺
  createStore(data) {
    return api.post('/api/franchise/stores/create', data)
  },

  // 获取所有店铺
  getAllStores(params = {}) {
    return api.get('/api/franchise/stores/all', { params })
  },

  // 根据状态获取店铺
  getStoresByStatus(status, params = {}) {
    return api.get(`/api/franchise/stores/status/${status}`, { params })
  },

  // 根据ID获取店铺详情
  getStoreById(id) {
    return api.get(`/api/franchise/stores/${id}`)
  },

  // 更新店铺信息
  updateStore(id, data) {
    return api.put(`/api/franchise/stores/${id}`, data)
  },

  // 根据加盟商手机号查询店铺
  getStoresByFranchiseePhone(phone, params = {}) {
    return api.get(`/api/franchise/stores/franchisee/phone/${phone}`, { params })
  },

  // 搜索店铺（根据店铺名称、地址或加盟商姓名）
  searchStores(keyword, params = {}) {
    return api.get(`/api/franchise/stores/search?keyword=${keyword}`, { params })
  },

  // 获取附近的店铺
  getNearbyStores(latitude, longitude, radius = 10, params = {}) {
    return api.get('/api/franchise/stores/nearby', {
      params: { latitude, longitude, radius, ...params }
    })
  },

  // 获取店铺统计信息
  getStoreStats() {
    return api.get('/api/franchise/stores/statistics')
  },

  // 获取正在营业的店铺
  getOperatingStores(params = {}) {
    return api.get('/api/franchise/stores/operating', { params })
  },

  // 删除店铺
  deleteStore(id) {
    return api.delete(`/api/franchise/stores/${id}`)
  },

  // 根据投资金额范围查询店铺
  getStoresByAmountRange(minAmount, maxAmount, params = {}) {
    return api.get('/api/franchise/stores/amount-range', {
      params: { minAmount, maxAmount, ...params }
    })
  },

  // 根据月特许费范围查询店铺
  getStoresByFeeRange(minFee, maxFee, params = {}) {
    return api.get('/api/franchise/stores/fee-range', {
      params: { minFee, maxFee, ...params }
    })
  },

  // 根据店铺管理员手机号查询
  getStoresByManagerPhone(phone, params = {}) {
    return api.get(`/api/franchise/stores/manager/${phone}`, { params })
  },

  // 根据员工数量范围查询
  getStoresByEmployeeRange(minCount, maxCount, params = {}) {
    return api.get('/api/franchise/stores/employee-range', {
      params: { minCount, maxCount, ...params }
    })
  },

  // 根据座位数范围查询
  getStoresBySeatRange(minSeats, maxSeats, params = {}) {
    return api.get('/api/franchise/stores/seat-range', {
      params: { minSeats, maxSeats, ...params }
    })
  }
}

// 通用工具函数
export const franchiseUtils = {
  // 格式化日期
  formatDate(dateString, format = 'YYYY-MM-DD') {
    if (!dateString) return ''
    const date = new Date(dateString)
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    const hour = String(date.getHours()).padStart(2, '0')
    const minute = String(date.getMinutes()).padStart(2, '0')
    
    switch (format) {
      case 'YYYY-MM-DD':
        return `${year}-${month}-${day}`
      case 'YYYY-MM-DD HH:mm':
        return `${year}-${month}-${day} ${hour}:${minute}`
      case 'MM-DD':
        return `${month}-${day}`
      default:
        return `${year}-${month}-${day}`
    }
  },

  // 验证手机号
  validatePhone(phone) {
    return /^1[3-9]\d{9}$/.test(phone)
  },

  // 验证身份证号
  validateIdCard(idCard) {
    return /^[1-9]\d{5}(18|19|20)\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\d{3}[0-9Xx]$/.test(idCard)
  },

  // 验证邮箱
  validateEmail(email) {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)
  },

  // 获取状态文本
  getStatusText(status, type = 'application') {
    if (type === 'application') {
      const statusMap = {
        '待审核': '待审核',
        '审核中': '审核中',
        '审核通过': '审核通过',
        '审核拒绝': '审核拒绝',
        '已退回': '已退回',
        '已开业': '已开业',
        'PENDING': '待审核',
        'REVIEWING': '审核中',
        'APPROVED': '审核通过',
        'REJECTED': '审核拒绝',
        'RETURNED': '已退回',
        'OPENED': '已开业'
      }
      return statusMap[status] || '未知状态'
    } else if (type === 'store') {
      const statusMap = {
        'OPERATING': '营业中',
        'PREPARING': '筹备中',
        'CLOSED': '已关闭'
      }
      return statusMap[status] || '未知状态'
    }
    return '未知状态'
  },

  // 获取状态样式类
  getStatusClass(status, type = 'application') {
    if (type === 'application') {
      const statusMap = {
        '待审核': 'pending',
        '审核中': 'reviewing',
        '审核通过': 'approved',
        '审核拒绝': 'rejected',
        '已退回': 'returned',
        '已开业': 'opened',
        'PENDING': 'pending',
        'REVIEWING': 'reviewing',
        'APPROVED': 'approved',
        'REJECTED': 'rejected',
        'RETURNED': 'returned',
        'OPENED': 'opened'
      }
      return statusMap[status] || 'pending'
    } else if (type === 'store') {
      const statusMap = {
        'OPERATING': 'operating',
        'PREPARING': 'preparing',
        'CLOSED': 'closed'
      }
      return statusMap[status] || 'preparing'
    }
    return 'default'
  },

  // 计算距离（简单的直线距离计算）
  calculateDistance(lat1, lon1, lat2, lon2) {
    const R = 6371 // 地球半径（公里）
    const dLat = (lat2 - lat1) * Math.PI / 180
    const dLon = (lon2 - lon1) * Math.PI / 180
    const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
              Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
              Math.sin(dLon / 2) * Math.sin(dLon / 2)
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    return R * c // 距离（公里）
  },

  // 格式化金额
  formatAmount(amount, unit = '万元') {
    if (amount === null || amount === undefined) return ''
    return `${amount}${unit}`
  },

  // 生成随机ID
  generateId() {
    return Date.now().toString(36) + Math.random().toString(36).substr(2)
  }
}

export default {
  franchiseApplicationAPI,
  franchiseStoreAPI,
  franchiseUtils
}