<template>
  <div class="franchise-management">
    <div class="header">
      <h1>加盟申请管理</h1>
      <div class="header-actions">
        <div class="stats">
          <div class="stat-item">
            <span class="stat-number">{{ stats.total }}</span>
            <span class="stat-label">总申请</span>
          </div>
          <div class="stat-item">
            <span class="stat-number">{{ stats['待审核'] || 0 }}</span>
            <span class="stat-label">待审核</span>
          </div>
          <div class="stat-item">
            <span class="stat-number">{{ stats['审核中'] || 0 }}</span>
            <span class="stat-label">审核中</span>
          </div>
          <div class="stat-item">
            <span class="stat-number">{{ stats['审核通过'] || 0 }}</span>
            <span class="stat-label">已通过</span>
          </div>
          <div class="stat-item">
            <span class="stat-number">{{ stats['审核拒绝'] || 0 }}</span>
            <span class="stat-label">已拒绝</span>
          </div>
          <div class="stat-item">
            <span class="stat-number">{{ stats['已退回'] || 0 }}</span>
            <span class="stat-label">已退回</span>
          </div>
        </div>
      </div>
    </div>

    <div class="filters">
      <div class="filter-row">
        <div class="filter-group">
          <label>状态筛选</label>
          <select v-model="filters.status" @change="onFilterChange">
            <option value="">全部状态</option>
            <option value="待审核">待审核</option>
            <option value="审核中">审核中</option>
            <option value="审核通过">已通过</option>
            <option value="审核拒绝">已拒绝</option>
            <option value="已退回">已退回</option>
            <option value="已开业">已开业</option>
          </select>
        </div>
        <div class="filter-group">
          <label>地区筛选</label>
          <select v-model="filters.region" @change="onFilterChange">
            <option value="">全部地区</option>
            <option value="北京">北京</option>
            <option value="上海">上海</option>
            <option value="广州">广州</option>
            <option value="深圳">深圳</option>
            <option value="杭州">杭州</option>
            <option value="成都">成都</option>
          </select>
        </div>
        <div class="filter-group">
          <label>搜索</label>
          <input v-model="filters.search" @keyup.enter="onFilterChange" @input="onFilterChange" placeholder="申请人姓名、电话或地区">
        </div>
        <div class="filter-group">
          <label>投资金额范围</label>
          <div class="amount-range">
            <input v-model.number="filters.minAmount" type="number" placeholder="最小金额" @change="onFilterChange">
            <span>-</span>
            <input v-model.number="filters.maxAmount" type="number" placeholder="最大金额" @change="onFilterChange">
          </div>
        </div>
        <div class="filter-group">
          <label>排序方式</label>
          <select v-model="filters.sortBy" @change="onFilterChange">
            <option value="createdAt">按提交时间</option>
            <option value="applicantName">按申请人姓名</option>
            <option value="investmentAmount">按投资金额</option>
            <option value="status">按状态</option>
          </select>
          <button @click="toggleSortOrder" class="sort-btn">
            {{ filters.sortOrder === 'asc' ? '↑' : '↓' }}
          </button>
        </div>
        <button @click="onFilterChange" class="btn-search">搜索</button>
        <button @click="resetFilters" class="btn-reset">重置</button>
      </div>
    </div>

    <div class="applications-table">
      <div v-if="loading" class="loading">加载中...</div>
      <div v-else-if="applications.length === 0" class="no-data">暂无申请数据</div>
      <table v-else>
        <thead>
          <tr>
            <th>申请人</th>
            <th>手机号</th>
            <th>拟开店名称</th>
            <th>拟开店地址</th>
            <th>投资金额</th>
            <th>申请时间</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="application in applications" :key="application.id">
            <td>{{ application.applicantName }}</td>
            <td>{{ application.applicantPhone }}</td>
            <td>{{ application.proposedStoreName }}</td>
            <td>{{ application.proposedStoreAddress }}</td>
            <td>{{ application.investmentAmount }}万元</td>
            <td>{{ formatDate(application.createdAt) }}</td>
            <td>
              <span :class="['status-badge', getStatusClass(application.status)]">{{ getStatusText(application.status) }}</span>
            </td>
            <td>
              <div class="action-buttons">
                <button @click="viewDetails(application)" class="btn-view">查看详情</button>
                <button v-if="application.status === '待审核'" @click="startReview(application)" class="btn-review">开始审核</button>
                <button v-if="application.status === '审核中'" @click="reviewApplication(application, '审核通过')" class="btn-approve">通过</button>
          <button v-if="application.status === '审核中'" @click="reviewApplication(application, '审核拒绝')" class="btn-reject">拒绝</button>
                <button v-if="application.status === '审核中'" @click="returnApplication(application)" class="btn-return">退回补充</button>
                <button @click="sendNotification(application)" class="btn-notify">发送通知</button>
                <button @click="deleteApplication(application)" class="btn-delete">删除</button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="totalPages > 1" class="pagination">
      <button @click="changePage(currentPage - 1)" :disabled="currentPage === 1" class="btn-page">上一页</button>
      <span class="page-info">第 {{ currentPage }} 页，共 {{ totalPages }} 页</span>
      <button @click="changePage(currentPage + 1)" :disabled="currentPage === totalPages" class="btn-page">下一页</button>
    </div>

    <!-- 详情弹窗 -->
    <div v-if="showDetails" class="modal-overlay" @click="closeDetails">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>申请详情</h3>
          <button @click="closeDetails" class="btn-close">×</button>
        </div>
        <div class="modal-body">
          <div v-if="selectedApplication" class="details-content">
            <div class="detail-section">
              <h4>申请人信息</h4>
              <div class="detail-item">
                <span class="label">姓名：</span>
                <span class="value">{{ selectedApplication.applicantName }}</span>
              </div>
              <div class="detail-item">
                <span class="label">手机号：</span>
                <span class="value">{{ selectedApplication.applicantPhone }}</span>
              </div>
              <div class="detail-item">
                <span class="label">身份证号：</span>
                <span class="value">{{ selectedApplication.applicantIdCard }}</span>
              </div>
              <div v-if="selectedApplication.applicantEmail" class="detail-item">
                <span class="label">邮箱：</span>
                <span class="value">{{ selectedApplication.applicantEmail }}</span>
              </div>
            </div>
            
            <div class="detail-section">
              <h4>店铺信息</h4>
              <div class="detail-item">
                <span class="label">拟开店名称：</span>
                <span class="value">{{ selectedApplication.proposedStoreName }}</span>
              </div>
              <div class="detail-item">
                <span class="label">拟开店地址：</span>
                <span class="value">{{ selectedApplication.proposedStoreAddress }}</span>
              </div>
              <div class="detail-item">
                <span class="label">投资金额：</span>
                <span class="value">{{ selectedApplication.investmentAmount }}万元</span>
              </div>
              <div v-if="selectedApplication.expectedOpeningDate" class="detail-item">
                <span class="label">预计开业时间：</span>
                <span class="value">{{ formatDate(selectedApplication.expectedOpeningDate) }}</span>
              </div>
            </div>
            
            <div v-if="selectedApplication.experienceDescription" class="detail-section">
              <h4>经营经验</h4>
              <div class="detail-item">
                <span class="value">{{ selectedApplication.experienceDescription }}</span>
              </div>
            </div>
            
            <div v-if="selectedApplication.applicationReason" class="detail-section">
              <h4>申请理由</h4>
              <div class="detail-item">
                <span class="value">{{ selectedApplication.applicationReason }}</span>
              </div>
            </div>
            
            <div v-if="selectedApplication.reviewComments" class="detail-section">
              <h4>审核意见</h4>
              <div class="detail-item">
                <span class="value">{{ selectedApplication.reviewComments }}</span>
              </div>
            </div>
          </div>
        </div>
        <div v-if="selectedApplication && selectedApplication.status === '待审核'" class="modal-footer">
          <button @click="reviewApplication(selectedApplication, '审核通过')" class="btn-approve">通过申请</button>
          <button @click="reviewApplication(selectedApplication, '审核拒绝')" class="btn-reject">拒绝申请</button>
        </div>
      </div>
    </div>

    <!-- 审核弹窗 -->
    <div v-if="showReview" class="modal-overlay" @click="closeReview">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>{{ reviewAction === '审核通过' ? '通过申请' : '拒绝申请' }}</h3>
          <button @click="closeReview" class="btn-close">×</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>审核意见</label>
            <textarea v-model="reviewComments" :placeholder="reviewAction === '审核通过' ? '请输入通过理由（可选）' : '请输入拒绝理由'" rows="4"></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button @click="closeReview" class="btn-cancel">取消</button>
          <button @click="confirmReview" class="btn-confirm" :disabled="reviewAction === '审核拒绝' && !reviewComments.trim()">确认</button>
        </div>
      </div>
    </div>

    <!-- 通知发送弹窗 -->
    <div v-if="showNotificationModal" class="modal-overlay" @click="closeNotificationModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>发送通知 - {{ selectedApplication ? selectedApplication.applicantName : '' }}</h3>
          <button @click="closeNotificationModal" class="btn-close">×</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>通知类型</label>
            <select v-model="notificationForm.type" class="form-select">
              <option value="approval">审核通过通知</option>
              <option value="rejection">审核拒绝通知</option>
              <option value="return">补充材料通知</option>
              <option value="custom">自定义通知</option>
            </select>
          </div>
          <div class="form-group">
            <label>通知内容</label>
            <textarea 
              v-model="notificationForm.content" 
              class="form-textarea"
              rows="6"
              placeholder="请输入通知内容..."
            ></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button @click="closeNotificationModal" class="btn-cancel">取消</button>
          <button @click="sendNotificationConfirm" class="btn-confirm">发送通知</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { franchiseApplicationAPI, franchiseUtils } from '@/api/franchise'

export default {
  name: 'FranchiseManagement',
  data() {
    return {
      loading: false,
      applications: [],
      stats: {
        '总计': 0,
        '待审核': 0,
        '审核中': 0,
        '审核通过': 0,
        '审核拒绝': 0,
        '已退回': 0,
        '已开业': 0
      },
      filters: {
        status: '',
        region: '',
        search: '',
        minAmount: null,
        maxAmount: null,
        sortBy: 'createdAt',
        sortOrder: 'desc'
      },
      showNotificationModal: false,
      notificationForm: {
        type: 'custom',
        content: ''
      },
      currentPage: 1,
      pageSize: 10,
      totalPages: 0,
      showDetails: false,
      selectedApplication: null,
      showReview: false,
      reviewAction: '',
      reviewComments: ''
    }
  },
  mounted() {
    this.loadApplications()
    this.loadStats()
  },
  methods: {
    async loadApplications() {
      this.loading = true
      try {
        // 首先获取所有申请数据
        const response = await franchiseApplicationAPI.getAllApplications()
        if (response && response.data && response.data.success) {
          let allApplications = response.data.data || []
          
          // 应用筛选条件
          let filteredApplications = allApplications.filter(app => {
            // 状态筛选
            if (this.filters.status && app.status !== this.filters.status) {
              return false
            }
            
            // 地区筛选
            if (this.filters.region && !app.proposedStoreAddress.includes(this.filters.region)) {
              return false
            }
            
            // 搜索筛选（姓名、电话、地区）
            if (this.filters.search) {
              const searchTerm = this.filters.search.toLowerCase()
              const matchName = app.applicantName && app.applicantName.toLowerCase().includes(searchTerm)
              const matchPhone = app.applicantPhone && app.applicantPhone.includes(searchTerm)
              const matchAddress = app.proposedStoreAddress && app.proposedStoreAddress.toLowerCase().includes(searchTerm)
              if (!matchName && !matchPhone && !matchAddress) {
                return false
              }
            }
            
            // 投资金额范围筛选
            if (this.filters.minAmount !== null && app.investmentAmount < this.filters.minAmount) {
              return false
            }
            if (this.filters.maxAmount !== null && app.investmentAmount > this.filters.maxAmount) {
              return false
            }
            
            return true
          })
          
          // 排序
          filteredApplications.sort((a, b) => {
            let aValue, bValue
            switch (this.filters.sortBy) {
              case 'applicantName':
                aValue = a.applicantName || ''
                bValue = b.applicantName || ''
                break
              case 'investmentAmount':
                aValue = a.investmentAmount || 0
                bValue = b.investmentAmount || 0
                break
              case 'status':
                aValue = a.status || ''
                bValue = b.status || ''
                break
              case 'createdAt':
              default:
                aValue = new Date(a.createdAt || 0)
                bValue = new Date(b.createdAt || 0)
                break
            }
            
            if (this.filters.sortOrder === 'asc') {
              return aValue > bValue ? 1 : -1
            } else {
              return aValue < bValue ? 1 : -1
            }
          })
          
          // 分页
          this.totalPages = Math.ceil(filteredApplications.length / this.pageSize)
          const startIndex = (this.currentPage - 1) * this.pageSize
          const endIndex = startIndex + this.pageSize
          this.applications = filteredApplications.slice(startIndex, endIndex)
          
        } else {
          const errorMessage = response && response.data && response.data.message ? response.data.message : '加载失败'
          if (this.$message && this.$message.error) {
            this.$message.error(errorMessage)
          } else {
            alert(errorMessage)
          }
          this.applications = []
          this.totalPages = 0
        }
      } catch (error) {
        console.error('加载申请列表失败:', error)
        if (this.$message && this.$message.error) {
          this.$message.error('加载失败，请检查网络连接后重试')
        } else {
          alert('加载失败，请检查网络连接后重试')
        }
        this.applications = []
        this.totalPages = 0
      } finally {
        this.loading = false
      }
    },
    async loadStats() {
      try {
        // 获取所有申请数据来计算统计信息
        const response = await franchiseApplicationAPI.getAllApplications()
        if (response && response.data && response.data.success) {
          const allApplications = response.data.data || []
          
          // 计算各状态的数量
          const stats = {
            total: allApplications.length,
            '待审核': 0,
            '审核中': 0,
            '审核通过': 0,
            '审核拒绝': 0,
            '已退回': 0,
            '已开业': 0
          }
          
          allApplications.forEach(app => {
            if (stats[app.status] !== undefined) {
              stats[app.status]++
            }
          })
          
          this.stats = stats
        } else {
          this.stats = {
            total: 0,
            '待审核': 0,
            '审核中': 0,
            '审核通过': 0,
            '审核拒绝': 0,
            '已退回': 0,
            '已开业': 0
          }
        }
      } catch (error) {
        console.error('加载统计数据失败:', error)
        this.stats = {
          total: 0,
          '待审核': 0,
          '审核中': 0,
          '审核通过': 0,
          '审核拒绝': 0,
          '已退回': 0,
          '已开业': 0
        }
      }
    },
    resetFilters() {
      this.filters = {
        status: '',
        region: '',
        search: '',
        minAmount: null,
        maxAmount: null,
        sortBy: 'createdAt',
        sortOrder: 'desc'
      }
      this.currentPage = 1
      this.loadApplications()
    },
    toggleSortOrder() {
      this.filters.sortOrder = this.filters.sortOrder === 'asc' ? 'desc' : 'asc'
      this.loadApplications()
    },
    startReview(application) {
      // 开始审核，将状态改为审核中
      this.updateApplicationStatus(application, '审核中', '已开始审核')
    },
    returnApplication(application) {
      // 退回申请，要求补充材料
      this.updateApplicationStatus(application, '已退回', '申请已退回，请补充材料')
    },
    async updateApplicationStatus(application, status, message) {
      try {
        const response = await franchiseApplicationAPI.updateApplicationStatus(application.id, {
          status: status
        })
        
        if (response.data.success) {
          this.$message.success(message)
          this.loadApplications()
          this.loadStats()
        } else {
          this.$message.error(response.data.message || '操作失败')
        }
      } catch (error) {
        console.error('更新申请状态失败:', error)
        this.$message.error('操作失败，请检查网络连接后重试')
      }
    },
    sendNotification(application) {
      this.selectedApplication = application
      this.notificationForm.content = this.getDefaultNotificationContent(application.status)
      this.showNotificationModal = true
    },
    closeNotificationModal() {
      this.showNotificationModal = false
      this.selectedApplication = null
      this.notificationForm = { type: 'custom', content: '' }
    },
    async sendNotificationConfirm() {
      if (!this.notificationForm.content.trim()) {
        this.$message.error('请输入通知内容')
        return
      }
      
      try {
        const response = await franchiseApplicationAPI.sendNotification(this.selectedApplication.id, {
          type: this.notificationForm.type,
          content: this.notificationForm.content
        })
        
        if (response.data.success) {
          this.$message.success(`通知已发送给 ${this.selectedApplication.applicantName}`)
          this.closeNotificationModal()
        } else {
          this.$message.error(response.data.message || '发送失败')
        }
      } catch (error) {
        console.error('发送通知失败:', error)
        this.$message.error('发送失败，请检查网络连接后重试')
      }
    },
    getDefaultNotificationContent(status) {
      const templates = {
        '待审核': '您的加盟申请已收到，我们将在3个工作日内完成初步审核。',
        '审核中': '您的加盟申请正在审核中，请耐心等待。',
        '审核通过': '恭喜！您的加盟申请已通过审核，我们将安排专员与您联系。',
        '审核拒绝': '很抱歉，您的加盟申请未能通过审核。',
        '已退回': '您的申请材料需要补充，请按要求提交完整资料。',
        '已开业': '恭喜您的加盟店铺已成功开业！'
      }
      return templates[status] || ''
    },
    changePage(page) {
      if (page >= 1 && page <= this.totalPages) {
        this.currentPage = page
        this.loadApplications()
      }
    },
    // 当筛选条件改变时，重置到第一页
    onFilterChange() {
      this.currentPage = 1
      this.loadApplications()
    },
    viewDetails(application) {
      this.selectedApplication = application
      this.showDetails = true
    },
    closeDetails() {
      this.showDetails = false
      this.selectedApplication = null
    },
    reviewApplication(application, action) {
      this.selectedApplication = application
      this.reviewAction = action
      this.reviewComments = ''
      this.showReview = true
    },
    closeReview() {
      this.showReview = false
      this.selectedApplication = null
      this.reviewAction = ''
      this.reviewComments = ''
    },
    async confirmReview() {
      if (this.reviewAction === '审核拒绝' && !this.reviewComments.trim()) {
        this.$message.error('拒绝申请时必须填写拒绝理由')
        return
      }

      try {
        const response = await franchiseApplicationAPI.reviewApplication(this.selectedApplication.id, {
          status: this.reviewAction,
          reviewComments: this.reviewComments
        })
        
        if (response.data.success) {
          this.$message.success(this.reviewAction === '审核通过' ? '申请已通过' : '申请已拒绝')
          this.closeReview()
          this.closeDetails()
          this.loadApplications()
          this.loadStats()
        } else {
          this.$message.error(response.data.message || '操作失败')
        }
      } catch (error) {
        console.error('审核申请失败:', error)
        this.$message.error('操作失败，请检查网络连接后重试')
      }
    },
    async deleteApplication(application) {
      if (!confirm(`确定要删除申请人"${application.applicantName}"的申请吗？`)) {
        return
      }

      try {
        const response = await franchiseApplicationAPI.deleteApplication(application.id)
        if (response.data.success) {
          this.$message.success('删除成功')
          this.loadApplications()
          this.loadStats()
        } else {
          this.$message.error(response.data.message || '删除失败')
        }
      } catch (error) {
        console.error('删除申请失败:', error)
        this.$message.error('删除失败，请检查网络连接后重试')
      }
    },
    getStatusClass(status) {
      return franchiseUtils.getStatusClass(status, 'application')
    },
    getStatusText(status) {
      return franchiseUtils.getStatusText(status, 'application')
    },
    formatDate(dateString) {
      return franchiseUtils.formatDate(dateString, 'YYYY-MM-DD HH:mm')
    }
  }
}
</script>

<style scoped>
.franchise-management {
  padding: 20px;
  background: #f5f5f5;
  min-height: 100vh;
}

.header {
  background: white;
  padding: 30px;
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  margin-bottom: 20px;
}

.header h1 {
  color: #d4af37;
  margin-bottom: 20px;
  font-size: 28px;
}

.stats {
  display: flex;
  gap: 30px;
}

.stat-item {
  text-align: center;
}

.stat-number {
  display: block;
  font-size: 24px;
  font-weight: bold;
  color: #d4af37;
  margin-bottom: 5px;
}

.stat-label {
  color: #666;
  font-size: 14px;
}

.filters {
  background: white;
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  margin-bottom: 20px;
}

.filter-row {
  display: flex;
  gap: 20px;
  align-items: end;
  flex-wrap: wrap;
}

.filter-group {
  flex: 1;
  min-width: 150px;
}

.filter-group label {
  display: block;
  margin-bottom: 8px;
  color: #333;
  font-weight: 500;
}

.filter-group input,
.filter-group select {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
}

.amount-range {
  display: flex;
  align-items: center;
  gap: 10px;
}

.amount-range input {
  flex: 1;
}

.amount-range span {
  color: #666;
}

.btn-search,
.btn-reset {
  padding: 10px 20px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  white-space: nowrap;
}

.btn-search {
  background: #d4af37;
  color: white;
}

.btn-search:hover {
  background: #b8941f;
}

.btn-reset {
  background: #6c757d;
  color: white;
}

.btn-reset:hover {
  background: #545b62;
}

.applications-table {
  background: white;
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  overflow: hidden;
  margin-bottom: 20px;
}

.loading,
.no-data {
  padding: 60px;
  text-align: center;
  color: #666;
  font-size: 16px;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th,
td {
  padding: 15px;
  text-align: left;
  border-bottom: 1px solid #eee;
}

th {
  background: #f8f9fa;
  font-weight: 600;
  color: #333;
}

.status-badge {
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge.pending {
  background: #fff3cd;
  color: #856404;
}

.status-badge.approved {
  background: #d4edda;
  color: #155724;
}

.status-badge.rejected {
  background: #f8d7da;
  color: #721c24;
}

.action-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.action-buttons button {
  padding: 6px 12px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  white-space: nowrap;
}

.btn-view {
  background: #007bff;
  color: white;
}

.btn-view:hover {
  background: #0056b3;
}

.btn-approve {
  background: #28a745;
  color: white;
}

.btn-approve:hover {
  background: #1e7e34;
}

.btn-reject {
  background: #dc3545;
  color: white;
}

.btn-reject:hover {
  background: #c82333;
}

.btn-delete {
  background: #6c757d;
  color: white;
}

.btn-delete:hover {
  background: #545b62;
}

.btn-review {
  background: #17a2b8;
  color: white;
}

.btn-review:hover {
  background: #138496;
}

.btn-return {
  background: #ffc107;
  color: #212529;
}

.btn-return:hover {
  background: #e0a800;
}

.btn-notify {
  background: #6f42c1;
  color: white;
}

.btn-notify:hover {
  background: #5a32a3;
}

.sort-header {
  cursor: pointer;
  user-select: none;
  position: relative;
}

.sort-header:hover {
  background: #e9ecef;
}

.sort-header.active {
  background: #f8f9fa;
}

.sort-icon {
  margin-left: 5px;
  font-size: 12px;
  color: #666;
}

.status-badge.reviewing {
  background: #cce5ff;
  color: #004085;
}

.status-badge.returned {
  background: #fff3cd;
  color: #856404;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  background: white;
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

.btn-page {
  padding: 10px 20px;
  background: #d4af37;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}

.btn-page:hover:not(:disabled) {
  background: #b8941f;
}

.btn-page:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.page-info {
  color: #666;
}

/* 弹窗样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 10px;
  max-width: 600px;
  width: 90%;
  max-height: 80vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #eee;
}

.modal-header h3 {
  margin: 0;
  color: #333;
}

.btn-close {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #999;
}

.btn-close:hover {
  color: #333;
}

.modal-body {
  padding: 20px;
}

.modal-footer {
  padding: 20px;
  border-top: 1px solid #eee;
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

.detail-section {
  margin-bottom: 25px;
}

.detail-section h4 {
  color: #d4af37;
  margin-bottom: 15px;
  font-size: 16px;
  border-bottom: 1px solid #eee;
  padding-bottom: 8px;
}

.detail-item {
  display: flex;
  margin-bottom: 10px;
}

.detail-item .label {
  width: 120px;
  color: #666;
  font-weight: 500;
}

.detail-item .value {
  color: #333;
  flex: 1;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #333;
  font-weight: 500;
}

.form-group textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  resize: vertical;
}

.form-select {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  background: white;
}

.form-textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  resize: vertical;
  font-family: inherit;
}

.btn-cancel,
.btn-confirm {
  padding: 10px 20px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
}

.btn-cancel {
  background: #6c757d;
  color: white;
}

.btn-cancel:hover {
  background: #545b62;
}

.btn-confirm {
  background: #d4af37;
  color: white;
}

.btn-confirm:hover:not(:disabled) {
  background: #b8941f;
}

.btn-confirm:disabled {
  background: #ccc;
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .franchise-management {
    padding: 10px;
  }
  
  .filter-row {
    flex-direction: column;
  }
  
  .filter-group {
    min-width: auto;
  }
  
  .stats {
    flex-wrap: wrap;
    gap: 15px;
  }
  
  .applications-table {
    overflow-x: auto;
  }
  
  table {
    min-width: 800px;
  }
  
  .action-buttons {
    flex-direction: column;
  }
  
  .modal-content {
    width: 95%;
    margin: 10px;
  }
  
  .detail-item {
    flex-direction: column;
  }
  
  .detail-item .label {
    width: auto;
    margin-bottom: 5px;
  }
}
</style>