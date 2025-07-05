<template>
  <div class="franchise-store-management">
    <div class="header">
      <h1>加盟店铺管理</h1>
      <div class="header-actions">
        <div class="stats">
          <div class="stat-item">
            <span class="stat-number">{{ stats.total }}</span>
            <span class="stat-label">总店铺</span>
          </div>
          <div class="stat-item">
            <span class="stat-number">{{ stats.operating }}</span>
            <span class="stat-label">营业中</span>
          </div>
          <div class="stat-item">
            <span class="stat-number">{{ stats.preparing }}</span>
            <span class="stat-label">筹备中</span>
          </div>
          <div class="stat-item">
            <span class="stat-number">{{ stats.closed }}</span>
            <span class="stat-label">已关闭</span>
          </div>
        </div>
        <button @click="showCreateStore = true" class="btn-create">新建店铺</button>
      </div>
    </div>

    <div class="filters">
      <div class="filter-row">
        <div class="filter-group">
          <label>状态筛选</label>
          <select v-model="filters.status" @change="loadStores">
            <option value="">全部状态</option>
            <option value="OPERATING">营业中</option>
            <option value="PREPARING">筹备中</option>
            <option value="CLOSED">已关闭</option>
          </select>
        </div>
        <div class="filter-group">
          <label>搜索</label>
          <input v-model="filters.search" @keyup.enter="loadStores" placeholder="店铺名称、地址或加盟商姓名">
        </div>
        <div class="filter-group">
          <label>投资金额范围</label>
          <div class="amount-range">
            <input v-model.number="filters.minAmount" type="number" placeholder="最小金额" @change="loadStores">
            <span>-</span>
            <input v-model.number="filters.maxAmount" type="number" placeholder="最大金额" @change="loadStores">
          </div>
        </div>
        <button @click="loadStores" class="btn-search">搜索</button>
        <button @click="resetFilters" class="btn-reset">重置</button>
      </div>
    </div>

    <div class="stores-table">
      <div v-if="loading" class="loading">加载中...</div>
      <div v-else-if="stores.length === 0" class="no-data">暂无店铺数据</div>
      <table v-else>
        <thead>
          <tr>
            <th>店铺名称</th>
            <th>加盟商</th>
            <th>地址</th>
            <th>投资金额</th>
            <th>月特许费</th>
            <th>开业时间</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="store in stores" :key="store.id">
            <td>{{ store.storeName }}</td>
            <td>{{ store.franchiseeName }}</td>
            <td>{{ store.storeAddress }}</td>
            <td>{{ store.investmentAmount }}万元</td>
            <td>{{ store.monthlyFranchiseFee }}元</td>
            <td>{{ formatDate(store.openingDate) }}</td>
            <td>
              <span :class="['status-badge', getStatusClass(store.status)]">{{ getStatusText(store.status) }}</span>
            </td>
            <td>
              <div class="action-buttons">
                <button @click="viewDetails(store)" class="btn-view">查看</button>
                <button @click="editStore(store)" class="btn-edit">编辑</button>
                <button @click="deleteStore(store)" class="btn-delete">删除</button>
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
          <h3>店铺详情</h3>
          <button @click="closeDetails" class="btn-close">×</button>
        </div>
        <div class="modal-body">
          <div v-if="selectedStore" class="details-content">
            <div class="detail-section">
              <h4>基本信息</h4>
              <div class="detail-item">
                <span class="label">店铺名称：</span>
                <span class="value">{{ selectedStore.storeName }}</span>
              </div>
              <div class="detail-item">
                <span class="label">店铺地址：</span>
                <span class="value">{{ selectedStore.storeAddress }}</span>
              </div>
              <div class="detail-item">
                <span class="label">营业状态：</span>
                <span :class="['value', 'status-' + selectedStore.status.toLowerCase()]">{{ getStatusText(selectedStore.status) }}</span>
              </div>
            </div>
            
            <div class="detail-section">
              <h4>加盟商信息</h4>
              <div class="detail-item">
                <span class="label">加盟商姓名：</span>
                <span class="value">{{ selectedStore.franchiseeName }}</span>
              </div>
              <div class="detail-item">
                <span class="label">联系电话：</span>
                <span class="value">{{ selectedStore.franchiseePhone }}</span>
              </div>
              <div class="detail-item">
                <span class="label">身份证号：</span>
                <span class="value">{{ selectedStore.franchiseeIdCard }}</span>
              </div>
            </div>
            
            <div class="detail-section">
              <h4>财务信息</h4>
              <div class="detail-item">
                <span class="label">投资金额：</span>
                <span class="value">{{ selectedStore.investmentAmount }}万元</span>
              </div>
              <div class="detail-item">
                <span class="label">加盟费：</span>
                <span class="value">{{ selectedStore.franchiseFee }}万元</span>
              </div>
              <div class="detail-item">
                <span class="label">月特许费：</span>
                <span class="value">{{ selectedStore.monthlyFranchiseFee }}元</span>
              </div>
            </div>
            
            <div class="detail-section">
              <h4>运营信息</h4>
              <div class="detail-item">
                <span class="label">开业时间：</span>
                <span class="value">{{ formatDate(selectedStore.openingDate) }}</span>
              </div>
              <div class="detail-item">
                <span class="label">合同开始：</span>
                <span class="value">{{ formatDate(selectedStore.contractStartDate) }}</span>
              </div>
              <div class="detail-item">
                <span class="label">合同结束：</span>
                <span class="value">{{ formatDate(selectedStore.contractEndDate) }}</span>
              </div>
              <div class="detail-item">
                <span class="label">员工数量：</span>
                <span class="value">{{ selectedStore.employeeCount }}人</span>
              </div>
              <div class="detail-item">
                <span class="label">座位数量：</span>
                <span class="value">{{ selectedStore.seatCount }}个</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 创建/编辑店铺弹窗 -->
    <div v-if="showCreateStore || showEditStore" class="modal-overlay" @click="closeStoreForm">
      <div class="modal-content large" @click.stop>
        <div class="modal-header">
          <h3>{{ showCreateStore ? '新建店铺' : '编辑店铺' }}</h3>
          <button @click="closeStoreForm" class="btn-close">×</button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="saveStore">
            <div class="form-section">
              <h4>基本信息</h4>
              <div class="form-row">
                <div class="form-group">
                  <label>店铺名称 *</label>
                  <input v-model="storeForm.storeName" type="text" required placeholder="请输入店铺名称">
                </div>
                <div class="form-group">
                  <label>店铺地址 *</label>
                  <input v-model="storeForm.storeAddress" type="text" required placeholder="请输入详细地址">
                </div>
              </div>
              <div class="form-row">
                <div class="form-group">
                  <label>营业状态</label>
                  <select v-model="storeForm.status">
                    <option value="PREPARING">筹备中</option>
                    <option value="OPERATING">营业中</option>
                    <option value="CLOSED">已关闭</option>
                  </select>
                </div>
                <div class="form-group">
                  <label>开业时间</label>
                  <input v-model="storeForm.openingDate" type="date">
                </div>
              </div>
            </div>

            <div class="form-section">
              <h4>加盟商信息</h4>
              <div class="form-row">
                <div class="form-group">
                  <label>加盟商姓名 *</label>
                  <input v-model="storeForm.franchiseeName" type="text" required placeholder="请输入加盟商姓名">
                </div>
                <div class="form-group">
                  <label>联系电话 *</label>
                  <input v-model="storeForm.franchiseePhone" type="tel" required placeholder="请输入联系电话">
                </div>
              </div>
              <div class="form-row">
                <div class="form-group">
                  <label>身份证号 *</label>
                  <input v-model="storeForm.franchiseeIdCard" type="text" required placeholder="请输入身份证号">
                </div>
                <div class="form-group">
                  <label>店铺管理员电话</label>
                  <input v-model="storeForm.storeManagerPhone" type="tel" placeholder="请输入店铺管理员电话">
                </div>
              </div>
            </div>

            <div class="form-section">
              <h4>财务信息</h4>
              <div class="form-row">
                <div class="form-group">
                  <label>投资金额 (万元) *</label>
                  <input v-model.number="storeForm.investmentAmount" type="number" required placeholder="请输入投资金额" min="0" step="0.1">
                </div>
                <div class="form-group">
                  <label>加盟费 (万元) *</label>
                  <input v-model.number="storeForm.franchiseFee" type="number" required placeholder="请输入加盟费" min="0" step="0.1">
                </div>
              </div>
              <div class="form-row">
                <div class="form-group">
                  <label>月特许费 (元) *</label>
                  <input v-model.number="storeForm.monthlyFranchiseFee" type="number" required placeholder="请输入月特许费" min="0">
                </div>
              </div>
            </div>

            <div class="form-section">
              <h4>合同信息</h4>
              <div class="form-row">
                <div class="form-group">
                  <label>合同开始日期</label>
                  <input v-model="storeForm.contractStartDate" type="date">
                </div>
                <div class="form-group">
                  <label>合同结束日期</label>
                  <input v-model="storeForm.contractEndDate" type="date">
                </div>
              </div>
            </div>

            <div class="form-section">
              <h4>运营信息</h4>
              <div class="form-row">
                <div class="form-group">
                  <label>员工数量</label>
                  <input v-model.number="storeForm.employeeCount" type="number" placeholder="请输入员工数量" min="0">
                </div>
                <div class="form-group">
                  <label>座位数量</label>
                  <input v-model.number="storeForm.seatCount" type="number" placeholder="请输入座位数量" min="0">
                </div>
              </div>
            </div>

            <div class="form-actions">
              <button type="button" @click="closeStoreForm" class="btn-cancel">取消</button>
              <button type="submit" class="btn-save" :disabled="saving">{{ saving ? '保存中...' : '保存' }}</button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { franchiseStoreAPI, franchiseUtils } from '@/api/franchise'

export default {
  name: 'FranchiseStoreManagement',
  data() {
    return {
      loading: false,
      saving: false,
      stores: [],
      stats: {
        total: 0,
        operating: 0,
        preparing: 0,
        closed: 0
      },
      filters: {
        status: '',
        search: '',
        minAmount: null,
        maxAmount: null
      },
      currentPage: 1,
      pageSize: 10,
      totalPages: 0,
      showDetails: false,
      selectedStore: null,
      showCreateStore: false,
      showEditStore: false,
      storeForm: this.getEmptyStoreForm()
    }
  },
  mounted() {
    this.loadStores()
    this.loadStats()
  },
  methods: {
    getEmptyStoreForm() {
      return {
        storeName: '',
        storeAddress: '',
        status: 'PREPARING',
        openingDate: '',
        franchiseeName: '',
        franchiseePhone: '',
        franchiseeIdCard: '',
        storeManagerPhone: '',
        investmentAmount: null,
        franchiseFee: null,
        monthlyFranchiseFee: null,
        contractStartDate: '',
        contractEndDate: '',
        employeeCount: null,
        seatCount: null
      }
    },
    async loadStores() {
      this.loading = true
      try {
        const params = {
          page: this.currentPage,
          size: this.pageSize,
          ...this.filters
        }
        
        const response = await franchiseStoreAPI.getAllStores(params)
        if (response.data.success) {
          this.stores = response.data.data.content || []
          this.totalPages = response.data.data.totalPages || 0
        } else {
          this.$message.error(response.data.message || '加载失败')
        }
      } catch (error) {
        console.error('加载店铺列表失败:', error)
        this.$message.error('加载失败，请检查网络连接后重试')
      } finally {
        this.loading = false
      }
    },
    async loadStats() {
      try {
        const response = await franchiseStoreAPI.getStoreStats()
        if (response.data.success) {
          this.stats = response.data.data
        }
      } catch (error) {
        console.error('加载统计数据失败:', error)
      }
    },
    resetFilters() {
      this.filters = {
        status: '',
        search: '',
        minAmount: null,
        maxAmount: null
      }
      this.currentPage = 1
      this.loadStores()
    },
    changePage(page) {
      if (page >= 1 && page <= this.totalPages) {
        this.currentPage = page
        this.loadStores()
      }
    },
    viewDetails(store) {
      this.selectedStore = store
      this.showDetails = true
    },
    closeDetails() {
      this.showDetails = false
      this.selectedStore = null
    },
    editStore(store) {
      this.storeForm = { ...store }
      this.showEditStore = true
    },
    closeStoreForm() {
      this.showCreateStore = false
      this.showEditStore = false
      this.storeForm = this.getEmptyStoreForm()
    },
    async saveStore() {
      if (!this.validateStoreForm()) {
        return
      }

      this.saving = true
      try {
        let response
        if (this.showCreateStore) {
          response = await franchiseStoreAPI.createStore(this.storeForm)
        } else {
          response = await franchiseStoreAPI.updateStore(this.storeForm.id, this.storeForm)
        }
        
        if (response.data.success) {
          this.$message.success(this.showCreateStore ? '店铺创建成功' : '店铺更新成功')
          this.closeStoreForm()
          this.loadStores()
          this.loadStats()
        } else {
          this.$message.error(response.data.message || '保存失败')
        }
      } catch (error) {
        console.error('保存店铺失败:', error)
        this.$message.error('保存失败，请检查网络连接后重试')
      } finally {
        this.saving = false
      }
    },
    validateStoreForm() {
      if (!this.storeForm.storeName.trim()) {
        this.$message.error('请输入店铺名称')
        return false
      }
      if (!this.storeForm.storeAddress.trim()) {
        this.$message.error('请输入店铺地址')
        return false
      }
      if (!this.storeForm.franchiseeName.trim()) {
        this.$message.error('请输入加盟商姓名')
        return false
      }
      if (!this.storeForm.franchiseePhone.trim()) {
        this.$message.error('请输入联系电话')
        return false
      }
      if (!franchiseUtils.validatePhone(this.storeForm.franchiseePhone)) {
        this.$message.error('请输入正确的手机号')
        return false
      }
      if (!this.storeForm.franchiseeIdCard.trim()) {
        this.$message.error('请输入身份证号')
        return false
      }
      if (!this.storeForm.investmentAmount || this.storeForm.investmentAmount <= 0) {
        this.$message.error('请输入有效的投资金额')
        return false
      }
      if (!this.storeForm.franchiseFee || this.storeForm.franchiseFee <= 0) {
        this.$message.error('请输入有效的加盟费')
        return false
      }
      if (!this.storeForm.monthlyFranchiseFee || this.storeForm.monthlyFranchiseFee <= 0) {
        this.$message.error('请输入有效的月特许费')
        return false
      }
      return true
    },
    async deleteStore(store) {
      if (!confirm(`确定要删除店铺"${store.storeName}"吗？`)) {
        return
      }

      try {
        const response = await franchiseStoreAPI.deleteStore(store.id)
        if (response.data.success) {
          this.$message.success('删除成功')
          this.loadStores()
          this.loadStats()
        } else {
          this.$message.error(response.data.message || '删除失败')
        }
      } catch (error) {
        console.error('删除店铺失败:', error)
        this.$message.error('删除失败，请检查网络连接后重试')
      }
    },
    getStatusClass(status) {
      return franchiseUtils.getStatusClass(status, 'store')
    },
    getStatusText(status) {
      return franchiseUtils.getStatusText(status, 'store')
    },
    formatDate(dateString) {
      return franchiseUtils.formatDate(dateString, 'YYYY-MM-DD')
    }
  }
}
</script>

<style scoped>
.franchise-store-management {
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
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header h1 {
  color: #d4af37;
  margin: 0;
  font-size: 28px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 30px;
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

.btn-create {
  padding: 12px 24px;
  background: #28a745;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 16px;
  white-space: nowrap;
}

.btn-create:hover {
  background: #1e7e34;
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

.stores-table {
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

.status-badge.operating {
  background: #d4edda;
  color: #155724;
}

.status-badge.preparing {
  background: #fff3cd;
  color: #856404;
}

.status-badge.closed {
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

.btn-edit {
  background: #28a745;
  color: white;
}

.btn-edit:hover {
  background: #1e7e34;
}

.btn-delete {
  background: #dc3545;
  color: white;
}

.btn-delete:hover {
  background: #c82333;
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

.modal-content.large {
  max-width: 800px;
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

.detail-item .value.status-operating {
  color: #155724;
}

.detail-item .value.status-preparing {
  color: #856404;
}

.detail-item .value.status-closed {
  color: #721c24;
}

.form-section {
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eee;
}

.form-section:last-of-type {
  border-bottom: none;
}

.form-section h4 {
  color: #d4af37;
  margin-bottom: 20px;
  font-size: 16px;
}

.form-row {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
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

.form-group input,
.form-group select {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
}

.form-actions {
  display: flex;
  gap: 15px;
  justify-content: flex-end;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.btn-cancel,
.btn-save {
  padding: 12px 30px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 16px;
}

.btn-cancel {
  background: #6c757d;
  color: white;
}

.btn-cancel:hover {
  background: #545b62;
}

.btn-save {
  background: #d4af37;
  color: white;
}

.btn-save:hover:not(:disabled) {
  background: #b8941f;
}

.btn-save:disabled {
  background: #ccc;
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .franchise-store-management {
    padding: 10px;
  }
  
  .header {
    flex-direction: column;
    gap: 20px;
  }
  
  .header-actions {
    flex-direction: column;
    gap: 15px;
  }
  
  .stats {
    flex-wrap: wrap;
    gap: 15px;
  }
  
  .filter-row {
    flex-direction: column;
  }
  
  .filter-group {
    min-width: auto;
  }
  
  .stores-table {
    overflow-x: auto;
  }
  
  table {
    min-width: 1000px;
  }
  
  .action-buttons {
    flex-direction: column;
  }
  
  .modal-content {
    width: 95%;
    margin: 10px;
  }
  
  .form-row {
    flex-direction: column;
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