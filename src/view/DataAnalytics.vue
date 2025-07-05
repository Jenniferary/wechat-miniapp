<template>
  <div class="container">
    <!-- Sidebar -->
    <div class="sidebar">
      <h3>前台管理系统</h3>
      <ul>
        <li><router-link to="/check-orders">（1）管理堂食订单</router-link></li>
        <li><router-link to="/distribute-coupons">（2）管理优惠券</router-link></li>
        <li><router-link to="/manage-delivery">（3）管理外卖订单</router-link></li>
        <li><router-link to="/manage-tables">（4）管理餐桌</router-link></li>
        <li><router-link to="/dishes">（5）管理菜品</router-link></li>
        <li><router-link to="/data-analytics" class="active">（7）数据分析</router-link></li>
        <li><router-link to="/counter">（6）回到管理主页</router-link></li>
      </ul>
    </div>

    <!-- Content -->
    <div class="content">
      <h2 class="page-title">数据分析仪表板</h2>

      <!-- 实时数据卡片 -->
      <div class="stats-cards">
        <div class="stats-card">
          <div class="stats-icon">🍽️</div>
          <div class="stats-info">
            <h3>{{ realtimeStats.dineInOrders }}</h3>
            <p>今日堂食订单</p>
          </div>
        </div>
        <div class="stats-card">
          <div class="stats-icon">🚚</div>
          <div class="stats-info">
            <h3>{{ realtimeStats.takeawayOrders }}</h3>
            <p>今日外卖订单</p>
          </div>
        </div>
        <div class="stats-card">
          <div class="stats-icon">🪑</div>
          <div class="stats-info">
            <h3>{{ realtimeStats.occupiedTables }}/{{ realtimeStats.totalTables }}</h3>
            <p>餐桌使用率</p>
          </div>
        </div>
        <div class="stats-card">
          <div class="stats-icon">💰</div>
          <div class="stats-info">
            <h3>￥{{ realtimeStats.todayRevenue }}</h3>
            <p>今日总营收</p>
          </div>
        </div>
      </div>

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
        <!-- 订单趋势图 -->
        <div class="chart-card">
          <h3>订单趋势分析</h3>
          <div id="orderTrendChart" style="height: 400px;"></div>
        </div>

        <!-- 营收统计图 -->
        <div class="chart-card">
          <h3>营收统计</h3>
          <div id="revenueChart" style="height: 400px;"></div>
        </div>

        <!-- 订单类型分布 -->
        <div class="chart-card">
          <h3>订单类型分布</h3>
          <div id="orderTypeChart" style="height: 400px;"></div>
        </div>

        <!-- 热门菜品排行 -->
        <div class="chart-card">
          <h3>热门菜品排行</h3>
          <div id="popularDishesChart" style="height: 400px;"></div>
        </div>
      </div>

      <!-- 数据导入导出区域 -->
      <div class="data-management">
        <h3>数据管理</h3>
        
        <!-- 数据导入 -->
        <div class="import-section">
          <h4>📥 数据导入</h4>
          <div class="file-upload">
            <input
              type="file"
              ref="fileInput"
              @change="handleFileSelect"
              accept=".csv,.xlsx,.xls"
              style="display: none;"
            >
            <button @click="$refs.fileInput.click()" class="upload-btn">
              选择文件
            </button>
            <span v-if="selectedFile" class="file-name">{{ selectedFile.name }}</span>
          </div>
          <div class="import-options">
            <label>导入类型：</label>
            <select v-model="importType">
              <option value="dishes">菜品信息</option>
              <option value="users">用户信息</option>
              <option value="coupons">优惠券信息</option>
            </select>
            <button @click="importData" :disabled="!selectedFile" class="import-btn">
              导入数据
            </button>
          </div>
        </div>

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
            </select>
            <label>导出格式：</label>
            <select v-model="exportFormat">
              <option value="csv">CSV格式</option>
              <option value="xlsx">Excel格式</option>
            </select>
            <button @click="exportData" class="export-btn">
              导出数据
            </button>
          </div>
        </div>
      </div>

      <!-- 消息提示 -->
      <div v-if="message" :class="{'success': success, 'error': !success}" class="message">
        {{ message }}
      </div>
    </div>
  </div>
</template>

<script>
import axios from "axios";
import * as echarts from "echarts";

export default {
  name: "DataAnalytics",
  data() {
    return {
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
    await this.initData();
    this.initCharts();
    this.startRealtimeUpdate();
  },

  beforeUnmount() {
    // 清理图表实例
    Object.values(this.charts).forEach(chart => {
      if (chart) chart.dispose();
    });
    
    // 清理定时器
    if (this.realtimeTimer) {
      clearInterval(this.realtimeTimer);
    }
  },

  methods: {
    // 初始化数据
    async initData() {
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
      try {
        const response = await axios.get("/api/analytics/realtime");
        if (response.data.success) {
          this.realtimeStats = response.data.data;
        }
      } catch (error) {
        console.error("获取实时数据失败:", error);
      }
    },

    // 获取图表数据
    async fetchChartData() {
      try {
        const response = await axios.get(
          `/api/analytics/charts?timeRange=${this.selectedTimeRange}`
        );
        if (response.data.success) {
          this.updateAllCharts(response.data.data);
        }
      } catch (error) {
        console.error("获取图表数据失败:", error);
      }
    },

    // 初始化图表
    initCharts() {
      this.charts.orderTrend = echarts.init(document.getElementById('orderTrendChart'));
      this.charts.revenue = echarts.init(document.getElementById('revenueChart'));
      this.charts.orderType = echarts.init(document.getElementById('orderTypeChart'));
      this.charts.popularDishes = echarts.init(document.getElementById('popularDishesChart'));
      
      // 监听窗口resize事件
      window.addEventListener('resize', this.handleResize);
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
      const option = {
        title: {
          text: '订单趋势分析',
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
      const option = {
        title: {
          text: '营收统计',
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
      const option = {
        title: {
          text: '订单类型分布',
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
      const option = {
        title: {
          text: '热门菜品排行',
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
        if (chart) chart.resize();
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

    // 导入数据
    async importData() {
      if (!this.selectedFile) {
        this.showMessage("请选择要导入的文件", false);
        return;
      }

      const formData = new FormData();
      formData.append('file', this.selectedFile);
      formData.append('type', this.importType);

      try {
        const response = await axios.post("/api/data/import", formData, {
          headers: {
            'Content-Type': 'multipart/form-data'
          }
        });

        if (response.data.success) {
          this.showMessage(`${this.importType}数据导入成功！`, true);
          this.selectedFile = null;
          this.$refs.fileInput.value = '';
        } else {
          this.showMessage(response.data.message || "导入失败", false);
        }
      } catch (error) {
        console.error("导入失败:", error);
        this.showMessage("导入过程中出现错误", false);
      }
    },

    // 导出数据
    async exportData() {
      try {
        const response = await axios.get("/api/data/export", {
          params: {
            type: this.exportType,
            format: this.exportFormat,
            timeRange: this.selectedTimeRange
          },
          responseType: 'blob'
        });

        // 创建下载链接
        const url = window.URL.createObjectURL(new Blob([response.data]));
        const link = document.createElement('a');
        link.href = url;
        link.setAttribute('download', `${this.exportType}_${new Date().toISOString().split('T')[0]}.${this.exportFormat}`);
        document.body.appendChild(link);
        link.click();
        link.remove();
        window.URL.revokeObjectURL(url);

        this.showMessage("数据导出成功！", true);
      } catch (error) {
        console.error("导出失败:", error);
        this.showMessage("导出过程中出现错误", false);
      }
    },

    // 显示消息
    showMessage(msg, isSuccess) {
      this.message = msg;
      this.success = isSuccess;
      setTimeout(() => {
        this.message = "";
      }, 3000);
    }
  }
};
</script>

<style scoped>
.container {
  display: flex;
  max-width: 1400px;
  margin: 40px auto;
  padding: 40px;
  background-color: #fff;
  border-radius: 10px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
}

.sidebar {
  background-color: rgba(255, 255, 255, 0.8);
  padding: 20px;
  width: 200px;
  border-radius: 10px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
}

.sidebar h3 {
  margin-bottom: 10px;
  font-size: 20px;
  color: #2980b9;
  border-bottom: 2px solid #2980b9;
  padding-bottom: 10px;
}

.sidebar ul {
  list-style-type: none;
  padding: 0;
  margin: 0;
}

.sidebar ul li {
  margin-bottom: 10px;
}

.sidebar ul li a {
  text-decoration: none;
  color: #333;
  font-size: 16px;
  transition: color 0.3s ease;
}

.sidebar ul li a:hover,
.sidebar ul li a.active {
  color: #2980b9;
  font-weight: bold;
}

.content {
  flex-grow: 1;
  padding: 2rem;
  background-color: #fafafa;
  margin-left: 20px;
  border-radius: 10px;
}

.page-title {
  text-align: center;
  font-size: 2rem;
  color: #2980b9;
  margin-bottom: 2rem;
}

/* 实时数据卡片 */
.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 2rem;
}

.stats-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 20px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.stats-icon {
  font-size: 2.5rem;
  margin-right: 15px;
}

.stats-info h3 {
  font-size: 1.8rem;
  margin: 0 0 5px 0;
}

.stats-info p {
  margin: 0;
  opacity: 0.9;
}

/* 时间选择器 */
.time-selector {
  background: white;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 2rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  gap: 15px;
}

.time-selector label {
  font-weight: bold;
}

.time-selector select {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
}

.refresh-btn {
  background: #28a745;
  color: white;
  border: none;
  padding: 8px 15px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
}

.refresh-btn:hover {
  background: #218838;
}

/* 图表容器 */
.charts-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(500px, 1fr));
  gap: 20px;
  margin-bottom: 2rem;
}

.chart-card {
  background: white;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.chart-card h3 {
  margin: 0 0 15px 0;
  color: #333;
  font-size: 1.2rem;
}

/* 数据管理区域 */
.data-management {
  background: white;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  margin-bottom: 2rem;
}

.data-management h3 {
  color: #2980b9;
  margin-bottom: 20px;
}

.import-section,
.export-section {
  margin-bottom: 20px;
  padding: 15px;
  border: 1px solid #eee;
  border-radius: 8px;
}

.import-section h4,
.export-section h4 {
  color: #333;
  margin-bottom: 15px;
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
  padding: 8px 15px;
  border-radius: 6px;
  cursor: pointer;
}

.upload-btn:hover {
  background: #0056b3;
}

.file-name {
  color: #666;
  font-size: 14px;
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
  font-weight: bold;
}

.import-options select,
.export-options select {
  padding: 6px 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.import-btn,
.export-btn {
  background: #28a745;
  color: white;
  border: none;
  padding: 8px 15px;
  border-radius: 6px;
  cursor: pointer;
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
  margin-top: 1rem;
  padding: 12px;
  border-radius: 6px;
  text-align: center;
  font-weight: bold;
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

/* 响应式设计 */
@media (max-width: 768px) {
  .container {
    flex-direction: column;
    padding: 20px;
  }
  
  .sidebar {
    width: 100%;
    margin-bottom: 20px;
  }
  
  .content {
    margin-left: 0;
  }
  
  .charts-container {
    grid-template-columns: 1fr;
  }
  
  .stats-cards {
    grid-template-columns: 1fr;
  }
}
</style>