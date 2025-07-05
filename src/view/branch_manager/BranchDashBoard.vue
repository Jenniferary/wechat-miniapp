<template>
  <div class="dashboard-page">
    <div class="sidebar">
      <h2>📌店长管理</h2>
      <ul>
        <li @click="$router.push('/branch-dashboard')" class="active">
          <strong>店铺概况</strong>
        </li>
        <li @click="$router.push('/data-analytics')">营业数据分析</li>
        <li @click="$router.push('/branch-employee')">员工入职审批</li>
        <li @click="$router.push('/branch-leaving')">员工请假审批</li>
        <li @click="$router.push('/branch-overtime-approval')">员工加班审批</li>
        <li @click="$router.push('/branch-leavingworking-review')">员工离职审批</li>
        <li @click="$router.push('/performance-review')">绩效考核</li>
        <li @click="$router.push('/branch-employee-management')">员工管理</li>
        <li @click="logout" class="logout">退出系统</li>
      </ul>
    </div>

    <div class="form-section">
      <!-- 加载状态 -->
      <div v-if="loading" class="loading">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>

      <!-- 分店信息显示 -->
      <div class="branch-info" v-if="currentBranch && !loading">
        <div class="info-card branch-card">
          <div class="branch-icon">🏪</div>
          <div class="branch-details">
            <h4>{{ currentBranch.branch_name }}</h4>
            <p>当前管理分店</p>
          </div>
        </div>
      </div>

      <!-- 无权限提示 -->
      <div v-if="!currentBranch && !loading" class="no-permission">
        <div class="no-permission-card">
          <div class="no-permission-icon">⚠️</div>
          <h4>暂无权限</h4>
          <p>您尚未被分配到任何分店，请联系系统管理员</p>
        </div>
      </div>

      <!-- 实时数据卡片 -->
      <div v-if="currentBranch && !loading" class="store-info">
        <div class="info-card">
          <div class="stats-icon">🍽️</div>
          <div class="stats-info">
            <h4>{{ realtimeStats.dineInOrders || 0 }}</h4>
            <p>今日堂食订单</p>
          </div>
        </div>

        <div class="info-card">
          <div class="stats-icon">🚚</div>
          <div class="stats-info">
            <h4>{{ realtimeStats.takeawayOrders || 0 }}</h4>
            <p>今日外卖订单</p>
          </div>
        </div>

        <div class="info-card">
          <div class="stats-icon">🪑</div>
          <div class="stats-info">
            <h4>{{ formatTableUsage() }}</h4>
            <p>餐桌使用率</p>
          </div>
        </div>

        <div class="info-card">
          <div class="stats-icon">💰</div>
          <div class="stats-info">
            <h4>{{ formatRevenue() }}</h4>
            <p>今日总营收</p>
          </div>
        </div>
      </div>

      <!-- 消息提示 -->
      <div v-if="message" :class="['message', success ? 'success' : 'error']">
        {{ message }}
      </div>
    </div>
  </div>
</template>

<script>
import axios from "axios";

export default {
  name: "BranchDashBoard",
  data() {
    return {
      // 当前分店信息
      currentBranch: null,
      managerId: null,
      loading: true,
      
      // 实时统计数据
      realtimeStats: {
        dineInOrders: 0,
        takeawayOrders: 0,
        occupiedTables: 0,
        totalTables: 0,
        todayRevenue: 0
      },
      
      // 定时器
      realtimeTimer: null,
      
      // 消息提示
      message: "",
      success: false
    };
  },

  async mounted() {
    // 检查登录状态
    if (!this.checkLoginStatus()) {
      this.$router.push('/branch-login');
      return;
    }
    
    await this.loadCurrentBranch();
    if (this.currentBranch) {
      await this.fetchRealtimeStats();
      this.startRealtimeUpdate();
    }
    this.loading = false;
  },

  beforeUnmount() {
    // 清理定时器
    if (this.realtimeTimer) {
      clearInterval(this.realtimeTimer);
    }
  },

  methods: {
    // 检查登录状态
    checkLoginStatus() {
      const isLoggedIn = localStorage.getItem('isLoggedIn');
      const managerId = localStorage.getItem('managerId');
      const branchId = localStorage.getItem('branchId');
      
      if (!isLoggedIn || !managerId || !branchId) {
        return false;
      }
      
      this.managerId = parseInt(managerId);
      return true;
    },

    // 加载当前用户的分店信息
    async loadCurrentBranch() {
      try {
        const response = await axios.get("/api/analytics/branches", {
          params: { managerId: this.managerId }
        });
        
        if (response.data.success && response.data.data.length > 0) {
          this.currentBranch = response.data.data[0];
        } else {
          this.currentBranch = null;
        }
      } catch (error) {
        console.error("获取分店信息失败:", error);
        this.showMessage("获取分店信息失败", false);
        this.currentBranch = null;
      }
    },

    // 获取实时统计数据
    async fetchRealtimeStats() {
      if (!this.currentBranch) return;
      
      try {
        const params = {
          managerId: this.managerId,
          branchId: this.currentBranch.branch_id
        };
        
        console.log("发送请求参数:", params); // 调试日志
        console.log("当前分店信息:", this.currentBranch); // 查看分店信息
        
        const response = await axios.get("/api/analytics/realtime", { params });
        
        console.log("API返回数据:", response.data); // 调试日志
        console.log("原始营收数据:", response.data.data?.todayRevenue);
        console.log("餐桌数据:", {
          occupied: response.data.data?.occupiedTables,
          total: response.data.data?.totalTables
        });
        
        // 验证数据逻辑
        if (response.data.data?.todayRevenue === "0.00" && 
            (response.data.data?.dineInOrders > 0 || response.data.data?.takeawayOrders > 0)) {
          console.warn("⚠️ 有订单但营收为0，可能是数据查询问题！");
        }
        
        if (response.data.success) {
          this.realtimeStats = {
            dineInOrders: response.data.data.dineInOrders || 0,
            takeawayOrders: response.data.data.takeawayOrders || 0,
            occupiedTables: response.data.data.occupiedTables || 0,
            totalTables: response.data.data.totalTables || 0,
            todayRevenue: response.data.data.todayRevenue || 0
          };
          
          console.log("处理后的统计数据:", this.realtimeStats); // 调试日志
        } else {
          console.error("API返回失败:", response.data.message);
          this.showMessage(response.data.message || "获取数据失败", false);
        }
      } catch (error) {
        console.error("获取实时数据失败:", error);
        console.error("错误详情:", error.response?.data); // 更详细的错误信息
        if (error.response && error.response.data && error.response.data.message) {
          this.showMessage(error.response.data.message, false);
        }
      }
    },

    // 格式化餐桌使用率显示
    formatTableUsage() {
      const occupied = this.realtimeStats.occupiedTables || 0;
      const total = this.realtimeStats.totalTables || 0;
      
      if (total === 0) {
        return "暂无餐桌";
      }
      
      return `${occupied}/${total}`;
    },

    // 格式化营收显示
    formatRevenue() {
      const revenue = this.realtimeStats.todayRevenue || 0;
      
      // 确保是数字格式
      const numRevenue = parseFloat(revenue);
      
      if (isNaN(numRevenue)) {
        return "￥0.00";
      }
      
      return `￥${numRevenue.toFixed(2)}`;
    },

    // 开始实时更新
    startRealtimeUpdate() {
      this.realtimeTimer = setInterval(() => {
        this.fetchRealtimeStats();
      }, 30000); // 每30秒更新一次
    },

    // 显示消息
    showMessage(msg, isSuccess) {
      this.message = msg;
      this.success = isSuccess;
      setTimeout(() => {
        this.message = "";
      }, 3000);
    },

    // 登出
    async logout() {
      try {
        localStorage.clear();
        this.$router.push('/branch-login');
      } catch (error) {
        console.error('登出失败:', error);
        localStorage.clear();
        this.$router.push('/branch-login');
      }
    }
  },
};
</script>

<style scoped>
.dashboard-page {
  display: flex;
  width: 100vw;
  height: 100vh;
  font-family: "Segoe UI", Tahoma, Geneva, Verdana, sans-serif;
}

.sidebar {
  width: 240px;
  background: #1d3557;
  color: white;
  padding: 30px 20px;
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
}

.sidebar h2 {
  margin-bottom: 30px;
  font-size: 22px;
  border-bottom: 2px solid #fff;
  padding-bottom: 10px;
}

.sidebar ul {
  list-style: none;
  padding-left: 0;
  margin: 0;
  flex: 1;
}

.sidebar li {
  padding: 10px 0;
  font-size: 15px;
  cursor: pointer;
  transition: color 0.3s ease;
}

.sidebar li:hover {
  color: #3498db;
}

.sidebar li.active {
  color: #3498db;
  font-weight: bold;
}

.logout {
  color: #ffb3b3;
  transition: color 0.3s ease;
}

.logout:hover {
  color: #ffffff;
  font-weight: bold;
}

.form-section {
  width: calc(100vw - 240px);
  background: white;
  padding: 40px 60px;
  box-sizing: border-box;
  overflow-y: auto;
}

/* 分店信息 */
.branch-info {
  margin-bottom: 30px;
}

.branch-card {
  display: flex;
  align-items: center;
  background: #007bff !important;
  color: white;
}

.branch-icon {
  font-size: 40px;
  margin-right: 15px;
}

.branch-details h4 {
  font-size: 20px;
  color: white;
  margin: 0 0 5px 0;
}

.branch-details p {
  margin: 0;
  color: rgba(255, 255, 255, 0.8);
  font-size: 14px;
}

/* 无权限提示 */
.no-permission {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 300px;
}

.no-permission-card {
  background: #f8d7da;
  color: #721c24;
  padding: 40px;
  border-radius: 10px;
  text-align: center;
  border: 1px solid #f5c6cb;
  max-width: 400px;
}

.no-permission-icon {
  font-size: 48px;
  margin-bottom: 20px;
}

.store-info {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  margin-bottom: 30px;
}

.info-card {
  width: calc(25% - 15px);
  background: #f1f1f1;
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 0 8px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
}

.stats-icon {
  font-size: 30px;
  margin-right: 15px;
}

.stats-info h4 {
  font-size: 24px;
  color: #333;
  margin: 0 0 5px 0;
}

.stats-info p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

/* 消息提示 */
.message {
  margin-top: 20px;
  padding: 12px;
  border-radius: 6px;
  text-align: center;
  font-weight: 600;
}

.success {
  background-color: #d4edda;
  color: #155724;
  border: 1px solid #c3e6cb;
}

.error {
  background-color: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6cb;
}

/* 加载状态 */
.loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 200px;
  color: #666;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #007bff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 15px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .dashboard-page {
    flex-direction: column;
  }
  
  .sidebar {
    width: 100%;
    padding: 20px;
  }
  
  .form-section {
    width: 100%;
    padding: 20px 30px;
  }
  
  .store-info {
    flex-direction: column;
  }
  
  .info-card {
    width: 100%;
  }
}
</style>