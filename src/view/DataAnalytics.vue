<template>
  <div class="container">
    <!-- Sidebar -->
    <div class="sidebar">
      <h3>营业数据分析</h3>
      <ul>
        <li @click="$router.push('/branch-dashboard')">返回主页</li>
        <li @click="logout" class="logout">退出系统</li>
      </ul>
    </div>

    <!-- Content -->
    <div class="content">


      <!-- 分店信息显示 -->
      <div v-if="currentBranch" class="branch-info">
        <div class="branch-card">
          <div class="branch-icon">🏪</div>
          <div class="branch-details">
            <h3>{{ currentBranch.branch_name }}</h3>
            <p>当前管理分店</p>
          </div>
        </div>
      </div>

      <!-- 无权限提示 -->
      <div v-if="!currentBranch && !loading" class="no-permission">
        <div class="no-permission-card">
          <div class="no-permission-icon">⚠️</div>
          <h3>暂无权限</h3>
          <p>您尚未被分配到任何分店，请联系系统管理员</p>
        </div>
      </div>

      <!-- 主要内容区域 -->
      <div v-if="currentBranch">
        <!-- 时间范围选择 -->
        <div class="time-selector">
          <label>时间范围：</label>
          <select v-model="selectedTimeRange" @change="updateCharts">
            <option value="today">今日</option>
            <option value="week">本周</option>
            <option value="month">本月</option>
            <option value="quarter">本季度</option>
            <option value="year">本年</option>
          </select>
          <button @click="refreshData" class="refresh-btn">🔄 刷新数据</button>
        </div>

        <!-- 图表区域 -->
        <div class="charts-container">
          <div class="chart-card">
            <h3>订单趋势分析</h3>
            <div id="orderTrendChart" style="height: 400px;"></div>
          </div>
          <div class="chart-card">
            <h3>营收统计</h3>
            <div id="revenueChart" style="height: 400px;"></div>
          </div>
          <div class="chart-card">
            <h3>订单类型分布</h3>
            <div id="orderTypeChart" style="height: 400px;"></div>
          </div>
          <div class="chart-card">
            <h3>热门菜品排行</h3>
            <div id="popularDishesChart" style="height: 400px;"></div>
          </div>
        </div>

        <!-- 数据导入导出区域 -->
        <div class="data-management">
          <h3>数据管理</h3>
          
          

          <!-- 数据导出 -->
          <div class="export-section">
            <h4>📤 数据导出</h4>
            <div class="export-options">
              <label>导出类型：</label>
              <select v-model="exportType">
                <option value="orders">订单数据</option>
                <option value="takeaway">外卖数据</option>
                <option value="revenue">营收报表</option>
                <option value="dishes">菜品统计</option>
                <option value="users">用户统计</option>
                <option value="branches">分店信息</option>
              </select>
              <label>导出格式：</label>
              <select v-model="exportFormat">
                <option value="csv">CSV格式</option>
                <option value="xlsx">Excel格式</option>
              </select>
              <span v-if="currentBranch" class="export-scope">
                导出范围：{{ currentBranch.branch_name }}
              </span>
              <button @click="exportData" class="export-btn">导出数据</button>
            </div>
          </div>
        </div>
      </div>

      <!-- 消息提示 -->
      <div v-if="message" :class="{ success: success, error: !success }" class="message">
        {{ message }}
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="loading">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>
    </div> <!-- /.content -->
  </div> <!-- /.container -->
</template>


<script>
import axios from "axios";
import * as echarts from "echarts";

export default {
  name: "DataAnalytics",
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
      
      // 选择的时间范围
      selectedTimeRange: "today",
      
      // 图表实例
      charts: {
        orderTrend: null,
        revenue: null,
        orderType: null,
        popularDishes: null
      },
      
      // 文件导入导出
      selectedFile: null,
      importType: "dishes",
      exportType: "orders",
      exportFormat: "csv",
      
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
    
    // 只有在有分店权限时才初始化图表和数据
    if (this.currentBranch) {
      // 等待一个tick确保DOM已经渲染
      await this.$nextTick();
      this.initCharts();
      await this.initData();
      this.startRealtimeUpdate();
    }
    
    this.loading = false;
  },

  beforeUnmount() {
    // 清理图表实例
    Object.values(this.charts).forEach(chart => {
      if (chart && chart.dispose) {
        try {
          chart.dispose();
        } catch (error) {
          console.warn('图表销毁失败:', error);
        }
      }
    });
    
    // 清理定时器
    if (this.realtimeTimer) {
      clearInterval(this.realtimeTimer);
    }
    
    // 清理resize监听器
    window.removeEventListener('resize', this.handleResize);
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
          this.showMessage("未找到分配的分店信息", false);
        }
      } catch (error) {
        console.error("获取分店信息失败:", error);
        this.showMessage("获取分店信息失败", false);
        this.currentBranch = null;
      }
    },

    // 初始化数据
    async initData() {
      if (!this.currentBranch) return;
      
      try {
        await Promise.all([
          this.fetchRealtimeStats(),
          this.fetchChartData()
        ]);
      } catch (error) {
        console.error("初始化数据失败:", error);
        this.showMessage("数据加载失败", false);
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
        
        const response = await axios.get("/api/analytics/realtime", { params });
        if (response.data.success) {
          this.realtimeStats = response.data.data;
        }
      } catch (error) {
        console.error("获取实时数据失败:", error);
        if (error.response && error.response.data && error.response.data.message) {
          this.showMessage(error.response.data.message, false);
        }
      }
    },

    // 获取图表数据
    async fetchChartData() {
      if (!this.currentBranch) return;
      
      try {
        const params = {
          timeRange: this.selectedTimeRange,
          managerId: this.managerId,
          branchId: this.currentBranch.branch_id
        };
        
        const response = await axios.get("/api/analytics/charts", { params });
        if (response.data.success) {
          this.updateAllCharts(response.data.data);
        }
      } catch (error) {
        console.error("获取图表数据失败:", error);
        if (error.response && error.response.data && error.response.data.message) {
          this.showMessage(error.response.data.message, false);
        }
      }
    },

    // 初始化图表
    initCharts() {
      this.$nextTick(() => {
        // 确保 DOM 元素存在后再初始化图表
        const orderTrendEl = document.getElementById('orderTrendChart');
        const revenueEl = document.getElementById('revenueChart');
        const orderTypeEl = document.getElementById('orderTypeChart');
        const popularDishesEl = document.getElementById('popularDishesChart');

        if (orderTrendEl) {
          this.charts.orderTrend = echarts.init(orderTrendEl);
        }
        if (revenueEl) {
          this.charts.revenue = echarts.init(revenueEl);
        }
        if (orderTypeEl) {
          this.charts.orderType = echarts.init(orderTypeEl);
        }
        if (popularDishesEl) {
          this.charts.popularDishes = echarts.init(popularDishesEl);
        }
        
        // 监听窗口resize事件
        window.addEventListener('resize', this.handleResize);
      });
    },

    // 更新所有图表
    updateAllCharts(data) {
      this.updateOrderTrendChart(data.orderTrend || []);
      this.updateRevenueChart(data.revenue || []);
      this.updateOrderTypeChart(data.orderType || []);
      this.updatePopularDishesChart(data.popularDishes || []);
    },

    // 订单趋势图
    updateOrderTrendChart(data) {
      if (!this.charts.orderTrend || !this.currentBranch) return;
      
      const branchName = this.currentBranch.branch_name;
      const option = {
        title: {
          text: `订单趋势分析 - ${branchName}`,
          left: 'center'
        },
        tooltip: {
          trigger: 'axis'
        },
        legend: {
          data: ['堂食订单', '外卖订单'],
          top: 30
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: data.map(item => item.date)
        },
        yAxis: {
          type: 'value'
        },
        series: [
          {
            name: '堂食订单',
            type: 'line',
            data: data.map(item => item.dineIn),
            smooth: true,
            itemStyle: { color: '#5470c6' }
          },
          {
            name: '外卖订单',
            type: 'line',
            data: data.map(item => item.takeaway),
            smooth: true,
            itemStyle: { color: '#91cc75' }
          }
        ]
      };
      this.charts.orderTrend.setOption(option);
    },

    // 营收统计图
    updateRevenueChart(data) {
      if (!this.charts.revenue || !this.currentBranch) return;
      
      const branchName = this.currentBranch.branch_name;
      const option = {
        title: {
          text: `营收统计 - ${branchName}`,
          left: 'center'
        },
        tooltip: {
          trigger: 'axis',
          formatter: '{b}<br/>{a}: ￥{c}'
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: data.map(item => item.date)
        },
        yAxis: {
          type: 'value',
          axisLabel: {
            formatter: '￥{value}'
          }
        },
        series: [
          {
            name: '营收',
            type: 'bar',
            data: data.map(item => item.revenue),
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: '#83bff6' },
                { offset: 0.5, color: '#188df0' },
                { offset: 1, color: '#188df0' }
              ])
            }
          }
        ]
      };
      this.charts.revenue.setOption(option);
    },

    // 订单类型分布图
    updateOrderTypeChart(data) {
      if (!this.charts.orderType || !this.currentBranch) return;
      
      const branchName = this.currentBranch.branch_name;
      const option = {
        title: {
          text: `订单类型分布 - ${branchName}`,
          left: 'center'
        },
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b}: {c} ({d}%)'
        },
        legend: {
          orient: 'vertical',
          left: 'left',
          top: 'middle'
        },
        series: [
          {
            name: '订单类型',
            type: 'pie',
            radius: '50%',
            center: ['60%', '50%'],
            data: data,
            emphasis: {
              itemStyle: {
                shadowBlur: 10,
                shadowOffsetX: 0,
                shadowColor: 'rgba(0, 0, 0, 0.5)'
              }
            }
          }
        ]
      };
      this.charts.orderType.setOption(option);
    },

    // 热门菜品排行图
    updatePopularDishesChart(data) {
      if (!this.charts.popularDishes || !this.currentBranch) return;
      
      const branchName = this.currentBranch.branch_name;
      const option = {
        title: {
          text: `热门菜品排行 - ${branchName}`,
          left: 'center'
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          }
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'value'
        },
        yAxis: {
          type: 'category',
          data: data.map(item => item.name)
        },
        series: [
          {
            name: '销量',
            type: 'bar',
            data: data.map(item => item.count),
            itemStyle: {
              color: '#fac858'
            }
          }
        ]
      };
      this.charts.popularDishes.setOption(option);
    },

    // 窗口resize处理
    handleResize() {
      Object.values(this.charts).forEach(chart => {
        if (chart && chart.resize) {
          try {
            chart.resize();
          } catch (error) {
            console.warn('图表resize失败:', error);
          }
        }
      });
    },

    // 更新图表数据
    async updateCharts() {
      await this.fetchChartData();
    },

    // 刷新数据
    async refreshData() {
      await this.initData();
      this.showMessage("数据已刷新", true);
    },

    // 开始实时更新
    startRealtimeUpdate() {
      this.realtimeTimer = setInterval(() => {
        this.fetchRealtimeStats();
      }, 30000); // 每30秒更新一次
    },

    // 文件选择
    handleFileSelect(event) {
      this.selectedFile = event.target.files[0];
    },



    // 导出数据
    async exportData() {
      if (!this.currentBranch) {
        this.showMessage("当前无可用分店", false);
        return;
      }

      try {
        const params = {
          type: this.exportType,
          format: this.exportFormat,
          timeRange: this.selectedTimeRange,
          managerId: this.managerId,
          branchId: this.currentBranch.branch_id
        };
        
        const response = await axios.get("/api/data/export", {
          params,
          responseType: 'blob'
        });

        // 创建下载链接
        const url = window.URL.createObjectURL(new Blob([response.data]));
        const link = document.createElement('a');
        link.href = url;
        
        const filename = `${this.exportType}_${this.currentBranch.branch_name}_${new Date().toISOString().split('T')[0]}.${this.exportFormat}`;
        
        link.setAttribute('download', filename);
        document.body.appendChild(link);
        link.click();
        link.remove();
        window.URL.revokeObjectURL(url);

        this.showMessage("数据导出成功！", true);
      } catch (error) {
        console.error("导出失败:", error);
        if (error.response && error.response.data && error.response.data.message) {
          this.showMessage(error.response.data.message, false);
        } else {
          this.showMessage("导出过程中出现错误", false);
        }
      }
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
    logout() {
      localStorage.clear();
      this.$router.push('/branch-login');
    }
  }
};
</script>

<style scoped>
.container {
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

.sidebar h3 {
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

.sidebar ul li {
  padding: 10px 0;
  font-size: 15px;
  cursor: pointer;
  transition: color 0.3s ease;
}

.sidebar ul li:hover {
  color: #3498db;
}

.logout {
  color: #ffb3b3;
  transition: color 0.3s ease;
}

.logout:hover {
  color: #ffffff;
  font-weight: bold;
}

.content {
  width: calc(100vw - 240px);
  background: white;
  padding: 40px 60px;
  box-sizing: border-box;
  overflow-y: auto;
}

.page-title {
  text-align: center;
  font-size: 32px;
  color: #333;
  margin-bottom: 30px;
}

/* 分店信息卡片 */
.branch-info {
  margin-bottom: 30px;
}

.branch-card {
  background: #f1f1f1;
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 0 8px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
}

.branch-icon {
  font-size: 40px;
  margin-right: 15px;
}

.branch-details h3 {
  font-size: 24px;
  color: #333;
  margin: 0 0 5px 0;
}

.branch-details p {
  margin: 0;
  color: #666;
  font-size: 16px;
}

/* 无权限提示 */
.no-permission {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
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

.no-permission-card h3 {
  color: #721c24;
  margin-bottom: 15px;
}

.no-permission-card p {
  color: #721c24;
  line-height: 1.6;
}

/* 时间选择器 */
.time-selector {
  background: #f1f1f1;
  padding: 20px;
  border-radius: 10px;
  margin-bottom: 30px;
  box-shadow: 0 0 8px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  gap: 15px;
}

.time-selector label {
  font-weight: 600;
  font-size: 16px;
  color: #333;
}

.time-selector select {
  padding: 10px 15px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 16px;
  background: white;
}

.refresh-btn {
  background: #007bff;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 16px;
  font-weight: 600;
  transition: background-color 0.3s ease;
}

.refresh-btn:hover {
  background: #0056b3;
}

/* 图表容器 */
.charts-container {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  margin-bottom: 30px;
}

.chart-card {
  width: calc(50% - 10px);
  background: white;
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 0 12px rgba(0, 0, 0, 0.05);
  border: 1px solid #eee;
}

.chart-card h3 {
  margin: 0 0 15px 0;
  color: #333;
  font-size: 20px;
  font-weight: 600;
}

/* 数据管理区域 */
.data-management {
  background: white;
  padding: 30px;
  border-radius: 10px;
  box-shadow: 0 0 12px rgba(0, 0, 0, 0.05);
  border: 1px solid #eee;
  margin-bottom: 30px;
}

.data-management h3 {
  color: #333;
  font-size: 24px;
  margin-bottom: 20px;
  font-weight: 600;
}

.import-section,
.export-section {
  margin-bottom: 20px;
  padding: 20px;
  border: 1px solid #eee;
  border-radius: 10px;
  background: #f8f9fa;
}

.import-section h4,
.export-section h4 {
  color: #333;
  margin-bottom: 15px;
  font-size: 18px;
  font-weight: 600;
}

.file-upload {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 15px;
}

.upload-btn {
  background: #007bff;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
  transition: background-color 0.3s ease;
}

.upload-btn:hover {
  background: #0056b3;
}

.file-name {
  color: #666;
  font-size: 14px;
  font-style: italic;
}

.import-options,
.export-options {
  display: flex;
  align-items: center;
  gap: 15px;
  flex-wrap: wrap;
}

.import-options label,
.export-options label {
  font-weight: 600;
  color: #333;
}

.import-options select,
.export-options select {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  min-width: 120px;
  background: white;
}

.target-branch,
.export-scope {
  background: #007bff;
  color: white;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 600;
}

.import-btn,
.export-btn {
  background: #28a745;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
  transition: background-color 0.3s ease;
}

.import-btn:hover,
.export-btn:hover {
  background: #218838;
}

.import-btn:disabled {
  background: #6c757d;
  cursor: not-allowed;
}

/* 消息提示 */
.message {
  margin-top: 20px;
  padding: 15px;
  border-radius: 6px;
  text-align: center;
  font-weight: 600;
  font-size: 16px;
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
  .container {
    flex-direction: column;
  }
  
  .sidebar {
    width: 100%;
    padding: 20px;
  }
  
  .content {
    width: 100%;
    padding: 20px 30px;
  }
  
  .charts-container {
    flex-direction: column;
  }
  
  .chart-card {
    width: 100%;
  }
  
  .time-selector,
  .import-options,
  .export-options {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .time-selector select,
  .import-options select,
  .export-options select {
    width: 100%;
  }
  
  .branch-card {
    flex-direction: column;
    text-align: center;
  }
  
  .branch-icon {
    margin-right: 0;
    margin-bottom: 10px;
  }
}
</style>