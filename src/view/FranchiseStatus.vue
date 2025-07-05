<template>
  <div class="franchise-status">
    <div class="header">
      <h1>申请状态查询</h1>
      <p>查看您的加盟申请进度</p>
    </div>

    <div class="search-container">
      <div class="search-form">
        <div class="form-group">
          <label>手机号</label>
          <input v-model="searchPhone" type="tel" placeholder="请输入申请时的手机号" @keyup.enter="searchApplication">
        </div>
        <button @click="searchApplication" class="btn-search" :disabled="searching">{{ searching ? '查询中...' : '查询申请' }}</button>
      </div>
    </div>

    <div v-if="applications.length > 0" class="applications-list">
      <div v-for="application in applications" :key="application.id" class="application-card">
        <div class="card-header">
          <h3>{{ application.proposedStoreName }}</h3>
          <span :class="['status-badge', getStatusClass(application.status)]">{{ getStatusText(application.status) }}</span>
        </div>
        
        <div class="card-content">
          <div class="info-row">
            <span class="label">申请人：</span>
            <span class="value">{{ application.applicantName }}</span>
          </div>
          <div class="info-row">
            <span class="label">拟开店地址：</span>
            <span class="value">{{ application.proposedStoreAddress }}</span>
          </div>
          <div class="info-row">
            <span class="label">投资金额：</span>
            <span class="value">{{ application.investmentAmount }}万元</span>
          </div>
          <div class="info-row">
            <span class="label">申请时间：</span>
            <span class="value">{{ formatDate(application.createdAt) }}</span>
          </div>
          <div v-if="application.reviewedAt" class="info-row">
            <span class="label">审核时间：</span>
            <span class="value">{{ formatDate(application.reviewedAt) }}</span>
          </div>
          <div v-if="application.reviewComments" class="info-row">
            <span class="label">审核意见：</span>
            <span class="value">{{ application.reviewComments }}</span>
          </div>
        </div>

        <div class="card-actions">
          <button @click="viewDetails(application)" class="btn-details">查看详情</button>
          <button v-if="application.status === '待审核'" @click="editApplication(application)" class="btn-edit">修改申请</button>
        </div>
      </div>
    </div>

    <div v-else-if="searched && applications.length === 0" class="no-results">
      <div class="no-results-content">
        <i class="icon-search"></i>
        <h3>未找到申请记录</h3>
        <p>请检查手机号是否正确，或者您还没有提交过申请</p>
        <button @click="goToApplication" class="btn-apply">立即申请</button>
      </div>
    </div>

    <div v-if="!searched" class="welcome-message">
      <div class="welcome-content">
        <i class="icon-franchise"></i>
        <h3>欢迎查询申请状态</h3>
        <p>请输入您申请时使用的手机号来查询申请进度</p>
      </div>
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
                <span class="label">经验描述：</span>
                <span class="value">{{ selectedApplication.experienceDescription }}</span>
              </div>
            </div>
            
            <div v-if="selectedApplication.applicationReason" class="detail-section">
              <h4>申请理由</h4>
              <div class="detail-item">
                <span class="value">{{ selectedApplication.applicationReason }}</span>
              </div>
            </div>
            
            <!-- 通知记录 -->
            <div class="detail-section">
              <h4>通知记录</h4>
              <div v-if="loadingNotifications" class="loading-text">
                正在加载通知记录...
              </div>
              <div v-else-if="notifications.length === 0" class="no-notifications">
                暂无通知记录
              </div>
              <div v-else class="notifications-list">
                <div v-for="notification in notifications" :key="notification.id" class="notification-item">
                  <div class="notification-header">
                    <span class="notification-type" :class="`type-${notification.notificationType}`">
                      {{ getNotificationTypeText(notification.notificationType) }}
                    </span>
                    <span class="notification-time">
                      {{ formatDateTime(notification.sentAt) }}
                    </span>
                  </div>
                  <div class="notification-content">
                    {{ notification.content }}
                  </div>
                  <div class="notification-footer">
                    <span class="sent-by">发送人：{{ notification.sentBy }}</span>
                    <span class="notification-status" :class="`status-${notification.status}`">
                      {{ getNotificationStatusText(notification.status) }}
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { franchiseApplicationAPI, franchiseUtils } from '@/api/franchise'

export default {
  name: 'FranchiseStatus',
  data() {
    return {
      searchPhone: '',
      searching: false,
      searched: false,
      applications: [],
      showDetails: false,
      selectedApplication: null,
      notifications: [],
      loadingNotifications: false
    }
  },
  methods: {
    async searchApplication() {
      if (!this.searchPhone.trim()) {
        this.$message.error('请输入手机号')
        return
      }
      
      if (!franchiseUtils.validatePhone(this.searchPhone)) {
        this.$message.error('请输入正确的手机号')
        return
      }

      this.searching = true
      try {
        const response = await franchiseApplicationAPI.searchByPhone(this.searchPhone)
        if (response.data.success) {
          this.applications = response.data.data || []
          this.searched = true
        } else {
          this.$message.error(response.data.message || '查询失败')
        }
      } catch (error) {
        console.error('查询申请失败:', error)
        this.$message.error('查询失败，请检查网络连接后重试')
      } finally {
        this.searching = false
      }
    },
    async viewDetails(application) {
      this.selectedApplication = application
      this.showDetails = true
      await this.loadNotifications(application.id)
    },
    closeDetails() {
      this.showDetails = false
      this.selectedApplication = null
      this.notifications = []
    },
    async loadNotifications(applicationId) {
      this.loadingNotifications = true
      try {
        const response = await franchiseApplicationAPI.getApplicationNotifications(applicationId)
        if (response.data.success) {
          this.notifications = response.data.data || []
        } else {
          console.error('加载通知记录失败:', response.data.message)
          this.notifications = []
        }
      } catch (error) {
        console.error('加载通知记录失败:', error)
        this.notifications = []
      } finally {
        this.loadingNotifications = false
      }
    },
    editApplication(application) {
      this.$router.push(`/franchise-edit/${application.id}`)
    },
    goToApplication() {
      this.$router.push('/franchise-apply')
    },
    getStatusClass(status) {
      return franchiseUtils.getStatusClass(status, 'application')
    },
    getStatusText(status) {
      return franchiseUtils.getStatusText(status, 'application')
    },
    formatDate(dateString) {
      return franchiseUtils.formatDate(dateString, 'YYYY-MM-DD HH:mm')
    },
    getNotificationTypeText(type) {
      const typeMap = {
        'APPROVAL': '审核通知',
        'REJECTION': '拒绝通知',
        'INTERVIEW': '面试通知',
        'DOCUMENT': '资料通知',
        'GENERAL': '一般通知'
      }
      return typeMap[type] || type
    },
    getNotificationStatusText(status) {
      const statusMap = {
        'SENT': '已发送',
        'DELIVERED': '已送达',
        'READ': '已读',
        'FAILED': '发送失败'
      }
      return statusMap[status] || status
    },
    formatDateTime(dateTime) {
      if (!dateTime) return ''
      const date = new Date(dateTime)
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    }
  }
}
</script>

<style scoped>
.franchise-status {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
  background: #f5f5f5;
  min-height: 100vh;
}

.header {
  text-align: center;
  margin-bottom: 30px;
  background: white;
  padding: 30px;
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

.header h1 {
  color: #d4af37;
  margin-bottom: 10px;
  font-size: 28px;
}

.header p {
  color: #666;
  font-size: 16px;
}

.search-container {
  background: white;
  padding: 30px;
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  margin-bottom: 30px;
}

.search-form {
  display: flex;
  gap: 20px;
  align-items: end;
}

.form-group {
  flex: 1;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #333;
  font-weight: 500;
}

.form-group input {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
}

.btn-search {
  padding: 12px 30px;
  background: #d4af37;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  white-space: nowrap;
}

.btn-search:hover:not(:disabled) {
  background: #b8941f;
}

.btn-search:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.applications-list {
  display: grid;
  gap: 20px;
}

.application-card {
  background: white;
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  overflow: hidden;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background: #f8f9fa;
  border-bottom: 1px solid #eee;
}

.card-header h3 {
  margin: 0;
  color: #333;
  font-size: 18px;
}

.status-badge {
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge.pending {
  background: #fff3cd;
  color: #856404;
}

.status-badge.reviewing {
  background: #cce5ff;
  color: #004085;
}

.status-badge.approved {
  background: #d4edda;
  color: #155724;
}

.status-badge.rejected {
  background: #f8d7da;
  color: #721c24;
}

.status-badge.returned {
  background: #fff3cd;
  color: #856404;
}

.status-badge.opened {
  background: #d1ecf1;
  color: #0c5460;
}

.card-content {
  padding: 20px;
}

.info-row {
  display: flex;
  margin-bottom: 12px;
}

.info-row .label {
  width: 120px;
  color: #666;
  font-weight: 500;
}

.info-row .value {
  color: #333;
  flex: 1;
}

.card-actions {
  padding: 20px;
  border-top: 1px solid #eee;
  display: flex;
  gap: 10px;
}

.btn-details,
.btn-edit {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.btn-details {
  background: #007bff;
  color: white;
}

.btn-details:hover {
  background: #0056b3;
}

.btn-edit {
  background: #28a745;
  color: white;
}

.btn-edit:hover {
  background: #1e7e34;
}

.no-results,
.welcome-message {
  background: white;
  padding: 60px 30px;
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  text-align: center;
}

.no-results-content,
.welcome-content {
  max-width: 400px;
  margin: 0 auto;
}

.icon-search,
.icon-franchise {
  font-size: 48px;
  color: #d4af37;
  margin-bottom: 20px;
}

.icon-search::before {
  content: '🔍';
}

.icon-franchise::before {
  content: '🏪';
}

.no-results h3,
.welcome-content h3 {
  color: #333;
  margin-bottom: 15px;
}

.no-results p,
.welcome-content p {
  color: #666;
  margin-bottom: 25px;
}

.btn-apply {
  padding: 12px 30px;
  background: #d4af37;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 16px;
}

.btn-apply:hover {
  background: #b8941f;
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

/* 通知记录样式 */
.notifications-section {
  margin-top: 10px;
}

.loading-text, .no-notifications {
  color: #999;
  font-style: italic;
  text-align: center;
  padding: 20px;
}

.notifications-list {
  max-height: 300px;
  overflow-y: auto;
}

.notification-item {
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 10px;
  background: #f9f9f9;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.notification-type {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: bold;
  color: white;
}

.notification-type.type-APPROVAL {
  background-color: #52c41a;
}

.notification-type.type-REJECTION {
  background-color: #ff4d4f;
}

.notification-type.type-INTERVIEW {
  background-color: #1890ff;
}

.notification-type.type-DOCUMENT {
  background-color: #fa8c16;
}

.notification-type.type-GENERAL {
  background-color: #722ed1;
}

.notification-time {
  color: #666;
  font-size: 12px;
}

.notification-content {
  color: #333;
  line-height: 1.5;
  margin-bottom: 8px;
  padding: 8px;
  background: white;
  border-radius: 4px;
}

.notification-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
}

.sent-by {
  color: #666;
}

.notification-status {
  padding: 2px 6px;
  border-radius: 3px;
  font-weight: bold;
}

.notification-status.status-SENT {
  background-color: #e6f7ff;
  color: #1890ff;
}

.notification-status.status-DELIVERED {
  background-color: #f6ffed;
  color: #52c41a;
}

.notification-status.status-read {
  background-color: #f6ffed;
  color: #52c41a;
}

.notification-status.status-FAILED {
  background-color: #fff2f0;
  color: #ff4d4f;
}

@media (max-width: 768px) {
  .franchise-status {
    padding: 10px;
  }
  
  .search-form {
    flex-direction: column;
    gap: 15px;
  }
  
  .btn-search {
    width: 100%;
  }
  
  .card-actions {
    flex-direction: column;
  }
  
  .btn-details,
  .btn-edit {
    width: 100%;
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