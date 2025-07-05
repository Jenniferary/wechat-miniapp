<template>
  <div class="page">
    <!-- 左侧导航 -->
    <nav class="sidebar">
      <h2>📊 门店管理</h2>
      <ul>
        <li @click="$router.push('/branch-dashboard')"><strong>店铺概况</strong></li>
        <li @click="$router.push('/branch-orders')">订单管理</li>
        <li @click="$router.push('/branch-employee')">员工入职审批</li> 
        <li @click="$router.push('/branch-leaving')">员工请假审批</li> 
        <li @click="$router.push('/branch-overtime-approval')">员工加班审批</li> 
        <li @click="$router.push('/branch-leavingworking-review')">员工离职审批</li>
        <li @click="$router.push('/branch-employee')">员工打卡情况总览</li> 
        <li @click="$router.push('/performance-review')">绩效考核</li>
        <li @click="$router.push('/branch-feedback')">客户反馈</li>
        <li @click="$router.push('/branch-employee-management')">员工管理</li>
        <li @click="logout" class="logout">退出系统</li>
      </ul>
    </nav>

    <main class="content">
      <!-- 1. 实时概览卡片 -->
      <section class="cards">
        <div class="card">
          <h4>员工总数</h4>
          <p>{{ dashboard.employeeCount }}</p>
        </div>
        <div class="card">
          <h4>本月总工时</h4>
          <p>{{ dashboard.totalHours }} h</p>
        </div>
        <div class="card">
          <h4>人均工时</h4>
          <p>{{ dashboard.avgHours.toFixed(1) }} h</p>
        </div>
      </section>

      <!-- 2. 员工信息表 -->
      <section class="employees">
        <div class="section-header">
          <h3>👥 本店员工信息</h3>
          <div class="action-buttons">
            <button @click="downloadTemplate" class="template-btn">📋 下载模板</button>
            <input type="file" ref="importFile" @change="handleFileImport" accept=".csv,.xlsx" style="display: none" />
            <button @click="$refs.importFile.click()" class="import-btn">📁 批量导入</button>
            <button @click="exportEmployees" class="export-btn">💾 导出员工信息</button>
          </div>
        </div>
        <table class="employee-table">
          <thead>
            <tr>
              <th>姓名</th>
              <th>用户名</th>
              <th>手机号</th>
              <th>邮箱</th>
              <th>职位</th>
              <th>门店ID</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="emp in employees" :key="emp.id">
              <td>{{ emp.name }}</td>
              <td>{{ emp.username }}</td>
              <td>{{ emp.phone }}</td>
              <td>{{ emp.email }}</td>
              <td>{{ emp.role }}</td>
              <td>{{ emp.branchId }}</td>
            </tr>
          </tbody>
        </table>
      </section>

      <!-- 3. 历史数据分析控制器 -->
      <section class="history-controls">
        <h3>📈 历史数据分析</h3>
        <div class="controls-row">
          <div class="control-group">
            <label>时间粒度：</label>
            <select v-model="historyGranularity">
              <option value="week">按周</option>
              <option value="month">按月</option>
              <option value="quarter">按季度</option>
              <option value="year">按年</option>
            </select>
          </div>
          <div class="control-group">
            <label>开始日期：</label>
            <input type="date" v-model="historyFromDate" />
          </div>
          <div class="control-group">
            <label>结束日期：</label>
            <input type="date" v-model="historyToDate" />
          </div>
          <button @click="fetchHistoryData" class="fetch-btn">查询历史数据</button>
          <button @click="exportHistoryData" class="export-btn">导出历史数据</button>
        </div>
      </section>

      <!-- 4. 历史数据图表 -->
      <section class="history-charts" v-if="historyData.length">
        <div class="chart-container">
          <h4>出勤率趋势</h4>
          <canvas ref="historyTrendChart"></canvas>
        </div>
        <div class="chart-container">
          <h4>考勤数据对比</h4>
          <canvas ref="historyComparisonChart"></canvas>
        </div>
        <div class="chart-container">
          <h4>工时分布</h4>
          <canvas ref="historyWorkHoursChart"></canvas>
        </div>
      </section>

      <!-- 5. 月份选择器 -->
      <section class="month-selector">
        <h3>📅 考勤统计</h3>
        <div class="selector-controls">
          <label>选择月份：</label>
          <input type="month" v-model="selectedMonth" @change="fetchAttendanceStats" />
          <button @click="exportAttendanceCsv" class="export-btn">导出考勤数据</button>
        </div>
      </section>

      <!-- 6. 考勤统计表格（增加工时统计列） -->
      <section class="attendance-stats" v-if="attendanceData.length">
        <h4>{{ selectedMonth }} 员工考勤统计</h4>
        <table class="attendance-table">
          <thead>
            <tr>
              <th>员工姓名</th>
              <th>应上班天数</th>
              <th>实际出勤天数</th>
              <th>请假天数</th>
              <th>旷工天数</th>
              <th>加班次数</th>
              <th>工时统计(小时)</th>
              <th>出勤率</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="record in attendanceData" :key="record.employeeId">
              <td>{{ record.employeeName }}</td>
              <td>{{ record.shouldWorkDays }}</td>
              <td>{{ record.actualWorkDays }}</td>
              <td>{{ record.leaveDays }}</td>
              <td>{{ record.absentDays }}</td>
              <td>{{ record.overtimeCount }}</td>
              <td><strong>{{ record.totalWorkHours }}</strong></td>
              <td>
                <span :class="['attendance-rate', getAttendanceRateClass(record.attendanceRate)]">
                  {{ record.attendanceRate }}%
                </span>
              </td>
            </tr>
          </tbody>
        </table>
      </section>

      <!-- 7. 考勤图表 -->
      <section class="charts" v-if="attendanceData.length">
        <div class="chart-container">
          <h4>出勤率分布</h4>
          <canvas ref="attendanceChart"></canvas>
        </div>
        <div class="chart-container">
          <h4>考勤统计对比</h4>
          <canvas ref="comparisonChart"></canvas>
        </div>
        <div class="chart-container">
          <h4>工时统计</h4>
          <canvas ref="workHoursChart"></canvas>
        </div>
      </section>

      <p v-else-if="selectedMonth" class="empty">该月份暂无考勤数据</p>
    </main>
  </div>
</template>

<script>
import { Chart, registerables } from 'chart.js'
Chart.register(...registerables)

export default {
  name: 'BranchEmployeeManagement',
  data () {
    return {
      managerId: Number(localStorage.getItem('managerId')),
      dashboard: { employeeCount: 0, totalHours: 0, avgHours: 0 },
      employees: [],
      selectedMonth: this.getCurrentMonth(),
      attendanceData: [],
      currentManager: null,
      attendanceChart: null,
      comparisonChart: null,
      workHoursChart: null,
      
      // 历史数据分析相关
      historyGranularity: 'month',
      historyFromDate: this.getDefaultFromDate(),
      historyToDate: this.getDefaultToDate(),
      historyData: [],
      historyTrendChart: null,
      historyComparisonChart: null,
      historyWorkHoursChart: null
    }
  },
  created () {
    if (!this.managerId) {
      alert('登录已过期，请重新登录');
      this.$router.push('/login');
      return;
    }
    this.fetchDashboard();
    this.loadCurrentManager();
  },
  mounted() {
    this.fetchAttendanceStats();
  },
  methods: {
    getCurrentMonth() {
      const now = new Date();
      const year = now.getFullYear();
      const month = String(now.getMonth() + 1).padStart(2, '0');
      return `${year}-${month}`;
    },
    getDefaultFromDate() {
      const now = new Date();
      const sixMonthsAgo = new Date(now.getFullYear(), now.getMonth() - 6, 1);
      return sixMonthsAgo.toISOString().split('T')[0];
    },
    getDefaultToDate() {
      const now = new Date();
      return now.toISOString().split('T')[0];
    },
    async fetchDashboard () {
      try {
        const res = await fetch(`/api/branch-managers/${this.managerId}/dashboard`)
        const json = await res.json()
        if (json.status === 'success') {
          this.dashboard = json.data
        }
      } catch (error) {
        console.error('获取概览数据失败:', error)
      }
    },
    async loadCurrentManager() {
      try {
        const res = await fetch(`/api/branch-managers/${this.managerId}`);
        const json = await res.json();
        if (json.status === "success") {
          this.currentManager = json.data;
          this.loadEmployees();
        } else {
          alert("加载店长信息失败");
        }
      } catch (err) {
        alert("请求错误：" + err.message);
      }
    },
    async loadEmployees() {
      if (!this.currentManager) return;
      const url = `/api/employees?branchId=${this.currentManager.branchId}`;
      try {
        const res = await fetch(url);
        const json = await res.json();
        if (json.status === "success") {
          this.employees = json.data;
        } else {
          alert("获取员工信息失败：" + (json.message || ""));
        }
      } catch (e) {
        alert("请求失败：" + e.message);
      }
    },
    async fetchAttendanceStats() {
      if (!this.selectedMonth) return;
      
      try {
        const res = await fetch(`/api/branch-managers/${this.managerId}/attendance-stats?month=${this.selectedMonth}`);
        const json = await res.json();
        if (json.status === 'success') {
          this.attendanceData = json.data;
          this.$nextTick(() => {
            this.renderCharts();
          });
        } else {
          alert('获取考勤统计失败：' + (json.message || ''));
        }
      } catch (error) {
        console.error('获取考勤统计失败:', error);
        alert('获取考勤统计失败');
      }
    },
    async fetchHistoryData() {
      if (!this.historyFromDate || !this.historyToDate) {
        alert('请选择开始和结束日期');
        return;
      }
      
      try {
        const url = `/api/branch-managers/${this.managerId}/attendance-history?granularity=${this.historyGranularity}&from=${this.historyFromDate}&to=${this.historyToDate}`;
        const res = await fetch(url);
        const json = await res.json();
        if (json.status === 'success') {
          this.historyData = json.data;
          this.$nextTick(() => {
            this.renderHistoryCharts();
          });
        } else {
          alert('获取历史数据失败：' + (json.message || ''));
        }
      } catch (error) {
        console.error('获取历史数据失败:', error);
        alert('获取历史数据失败');
      }
    },
    getAttendanceRateClass(rate) {
      if (rate >= 95) return 'excellent';
      if (rate >= 85) return 'good';
      if (rate >= 75) return 'normal';
      return 'poor';
    },
    renderCharts() {
      this.renderAttendanceChart();
      this.renderComparisonChart();
      this.renderWorkHoursChart();
    },
    renderAttendanceChart() {
      if (this.attendanceChart) {
        this.attendanceChart.destroy();
      }
      
      const ctx = this.$refs.attendanceChart.getContext('2d');
      const rateRanges = { '95-100%': 0, '85-94%': 0, '75-84%': 0, '0-74%': 0 };
      
      this.attendanceData.forEach(record => {
        const rate = record.attendanceRate;
        if (rate >= 95) rateRanges['95-100%']++;
        else if (rate >= 85) rateRanges['85-94%']++;
        else if (rate >= 75) rateRanges['75-84%']++;
        else rateRanges['0-74%']++;
      });
      
      this.attendanceChart = new Chart(ctx, {
        type: 'pie',
        data: {
          labels: Object.keys(rateRanges),
          datasets: [{
            data: Object.values(rateRanges),
            backgroundColor: ['#27ae60', '#3498db', '#f39c12', '#e74c3c']
          }]
        },
        options: {
          responsive: true,
          plugins: {
            legend: {
              position: 'bottom'
            }
          }
        }
      });
    },
    renderComparisonChart() {
      if (this.comparisonChart) {
        this.comparisonChart.destroy();
      }
      
      const ctx = this.$refs.comparisonChart.getContext('2d');
      
      this.comparisonChart = new Chart(ctx, {
        type: 'bar',
        data: {
          labels: this.attendanceData.map(d => d.employeeName),
          datasets: [
            {
              label: '实际出勤天数',
              data: this.attendanceData.map(d => d.actualWorkDays),
              backgroundColor: '#3498db'
            },
            {
              label: '请假天数',
              data: this.attendanceData.map(d => d.leaveDays),
              backgroundColor: '#f39c12'
            },
            {
              label: '旷工天数',
              data: this.attendanceData.map(d => d.absentDays),
              backgroundColor: '#e74c3c'
            },
            {
              label: '加班次数',
              data: this.attendanceData.map(d => d.overtimeCount),
              backgroundColor: '#27ae60'
            }
          ]
        },
        options: {
          responsive: true,
          scales: {
            y: {
              beginAtZero: true
            }
          }
        }
      });
    },
    renderWorkHoursChart() {
      if (this.workHoursChart) {
        this.workHoursChart.destroy();
      }
      
      const ctx = this.$refs.workHoursChart.getContext('2d');
      
      this.workHoursChart = new Chart(ctx, {
        type: 'bar',
        data: {
          labels: this.attendanceData.map(d => d.employeeName),
          datasets: [{
            label: '总工时(小时)',
            data: this.attendanceData.map(d => d.totalWorkHours),
            backgroundColor: '#9b59b6'
          }]
        },
        options: {
          responsive: true,
          scales: {
            y: {
              beginAtZero: true
            }
          }
        }
      });
    },
    renderHistoryCharts() {
      this.renderHistoryTrendChart();
      this.renderHistoryComparisonChart();
      this.renderHistoryWorkHoursChart();
    },
    renderHistoryTrendChart() {
      if (this.historyTrendChart) {
        this.historyTrendChart.destroy();
      }
      
      const ctx = this.$refs.historyTrendChart.getContext('2d');
      
      this.historyTrendChart = new Chart(ctx, {
        type: 'line',
        data: {
          labels: this.historyData.map(d => d.label),
          datasets: [{
            label: '出勤率(%)',
            data: this.historyData.map(d => d.attendanceRate),
            borderColor: '#3498db',
            backgroundColor: 'rgba(52, 152, 219, 0.1)',
            tension: 0.3
          }]
        },
        options: {
          responsive: true,
          scales: {
            y: {
              beginAtZero: true,
              max: 100
            }
          }
        }
      });
    },
    renderHistoryComparisonChart() {
      if (this.historyComparisonChart) {
        this.historyComparisonChart.destroy();
      }
      
      const ctx = this.$refs.historyComparisonChart.getContext('2d');
      
      this.historyComparisonChart = new Chart(ctx, {
        type: 'bar',
        data: {
          labels: this.historyData.map(d => d.label),
          datasets: [
            {
              label: '总出勤次数',
              data: this.historyData.map(d => d.totalAttendance),
              backgroundColor: '#3498db'
            },
            {
              label: '总加班次数',
              data: this.historyData.map(d => d.totalOvertime),
              backgroundColor: '#27ae60'
            },
            {
              label: '总请假次数',
              data: this.historyData.map(d => d.totalLeave),
              backgroundColor: '#f39c12'
            }
          ]
        },
        options: {
          responsive: true,
          scales: {
            y: {
              beginAtZero: true
            }
          }
        }
      });
    },
    renderHistoryWorkHoursChart() {
      if (this.historyWorkHoursChart) {
        this.historyWorkHoursChart.destroy();
      }
      
      const ctx = this.$refs.historyWorkHoursChart.getContext('2d');
      
      this.historyWorkHoursChart = new Chart(ctx, {
        type: 'bar',
        data: {
          labels: this.historyData.map(d => d.label),
          datasets: [{
            label: '总工时(小时)',
            data: this.historyData.map(d => d.totalWorkHours),
            backgroundColor: '#9b59b6'
          }]
        },
        options: {
          responsive: true,
          scales: {
            y: {
              beginAtZero: true
            }
          }
        }
      });
    },
    downloadTemplate() {
      const template = `姓名,用户名,手机号,邮箱,职位,门店ID,入职日期
张三,zhangsan,13800138001,zhangsan@example.com,厨师,1,2025-01-01
李四,lisi,13800138002,lisi@example.com,服务员,1,2025-01-01
王五,wangwu,13800138003,wangwu@example.com,收银员,1,2025-01-01
赵六,zhaoliu,13800138004,zhaoliu@example.com,HR,1,2025-01-01`;

      const blob = new Blob(['\ufeff' + template], { type: 'text/csv;charset=utf-8' });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = '员工导入模板.csv';
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
    },
    async exportEmployees() {
      const url = `/api/branch-managers/${this.managerId}/employees/export`;
      window.open(url, '_blank');
    },
    async exportAttendanceCsv() {
      if (!this.selectedMonth) {
        alert('请先选择月份');
        return;
      }
      
      const url = `/api/branch-managers/${this.managerId}/attendance-stats/export?month=${this.selectedMonth}`;
      window.open(url, '_blank');
    },
    async exportHistoryData() {
      if (!this.historyFromDate || !this.historyToDate) {
        alert('请先查询历史数据');
        return;
      }
      
      const url = `/api/branch-managers/${this.managerId}/attendance-history/export?granularity=${this.historyGranularity}&from=${this.historyFromDate}&to=${this.historyToDate}`;
      window.open(url, '_blank');
    },
    async handleFileImport(event) {
      const file = event.target.files[0];
      if (!file) return;
      
      // 检查文件类型
      const fileName = file.name.toLowerCase();
      if (!fileName.endsWith('.csv') && !fileName.endsWith('.xlsx')) {
        alert('请选择CSV或Excel文件');
        return;
      }
      
      const formData = new FormData();
      formData.append('file', file);
      
      try {
        const res = await fetch(`/api/branch-managers/${this.managerId}/employees/import`, {
          method: 'POST',
          body: formData
        });
        const json = await res.json();
        
        if (json.status === 'success') {
          let message = `导入完成！成功：${json.data.successCount}条，失败：${json.data.failCount}条`;
          
          if (json.data.errors.length > 0) {
            message += '\n\n错误详情：\n' + json.data.errors.slice(0, 5).join('\n');
            if (json.data.errors.length > 5) {
              message += '\n...(还有' + (json.data.errors.length - 5) + '个错误)';
            }
          }
          
          alert(message);
          
          // 如果有成功导入的数据，重新加载员工列表
          if (json.data.successCount > 0) {
            await this.loadEmployees();
          }
        } else {
          alert('导入失败：' + json.message);
        }
      } catch (error) {
        alert('导入失败：' + error.message);
      }
      
      // 清空文件选择
      event.target.value = '';
    },
    logout() {
      localStorage.removeItem('managerId');
      this.$router.push('/login');
    }
  },
  beforeUnmount() {
    // 销毁所有图表
    const charts = [
      'attendanceChart', 'comparisonChart', 'workHoursChart',
      'historyTrendChart', 'historyComparisonChart', 'historyWorkHoursChart'
    ];
    
    charts.forEach(chartName => {
      if (this[chartName]) {
        this[chartName].destroy();
      }
    });
  }
}
</script>

<style scoped>
.page { display: flex; height: 100vh; font-family: "Segoe UI", Tahoma, Geneva, Verdana, sans-serif; }
.sidebar { width: 240px; background:#1d3557; color:#fff; padding:24px 18px; }
.sidebar h2 { margin-bottom: 30px; font-size: 22px; border-bottom: 2px solid #fff; padding-bottom: 10px; }
.sidebar ul { list-style: none; padding-left: 0; margin: 0; flex: 1; }
.sidebar li { padding: 10px 0; font-size: 15px; cursor: pointer; transition: color 0.3s ease; }
.sidebar li:hover { color: #ffd166; }
.sidebar li.active { font-weight:bold; color:#ffd166; }
.logout { color: #ffb3b3; transition: color 0.3s ease; }
.logout:hover { color: #ffffff; font-weight: bold; }

.content { flex:1; overflow-y:auto; padding:30px; background:#f8f9fa; }

/* 概览卡片 */
.cards { display:flex; gap:20px; margin-bottom:30px; }
.card { flex:1; background:#fff; border-radius:12px; padding:20px; box-shadow:0 2px 8px rgba(0,0,0,.06); text-align:center; transition: transform 0.2s ease; }
.card:hover { transform: translateY(-2px); }
.card h4 { font-size:16px; color:#888; margin-bottom:10px; }
.card p { font-size:28px; font-weight:700; color:#1d3557; }

/* 节标题和按钮 */
.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.section-header h3 { margin: 0; color: #1d3557; }
.action-buttons { display: flex; gap: 10px; }

/* 按钮样式 */
.template-btn, .import-btn, .export-btn, .fetch-btn { 
  padding: 8px 16px; 
  border: none; 
  border-radius: 6px; 
  cursor: pointer; 
  font-size: 14px; 
  transition: all 0.3s ease; 
}
.template-btn { background: #f39c12; color: white; }
.template-btn:hover { background: #e67e22; }
.import-btn { background: #3498db; color: white; }
.import-btn:hover { background: #2980b9; }
.export-btn { background: #27ae60; color: white; }
.export-btn:hover { background: #219a52; }
.fetch-btn { background: #9b59b6; color: white; }
.fetch-btn:hover { background: #8e44ad; }

/* 员工表格 */
.employees { margin-bottom:30px; }
.employee-table { width:100%; border-collapse:collapse; background:#fff; border-radius:12px; overflow:hidden; box-shadow:0 2px 8px rgba(0,0,0,.06); }
.employee-table th, .employee-table td { padding:12px; border-bottom:1px solid #eee; text-align:center; }
.employee-table th { background:#1d3557; color:#fff; font-weight: 600; }
.employee-table tr:hover { background-color: #f8f9fa; }

/* 历史数据控制器 */
.history-controls { margin-bottom: 30px; }
.history-controls h3 { margin-bottom: 20px; color: #1d3557; }
.controls-row { display: flex; align-items: center; gap: 20px; flex-wrap: wrap; }
.control-group { display: flex; align-items: center; gap: 8px; }
.control-group label { font-weight: 600; color: #555; white-space: nowrap; }
.control-group select, .control-group input[type="date"] { 
  padding: 6px 10px; 
  border: 2px solid #ddd; 
  border-radius: 4px; 
  font-size: 14px; 
}

/* 月份选择器 */
.month-selector { margin-bottom: 30px; }
.month-selector h3 { margin-bottom: 20px; color: #1d3557; }
.selector-controls { display: flex; align-items: center; gap: 15px; }
.selector-controls label { font-weight: 600; color: #555; }
.selector-controls input[type="month"] { padding: 8px 12px; border: 2px solid #ddd; border-radius: 6px; font-size: 14px; }

/* 考勤统计表格 */
.attendance-stats { margin-bottom: 30px; }
.attendance-stats h4 { margin-bottom: 20px; color: #1d3557; }
.attendance-table { width:100%; border-collapse:collapse; background:#fff; border-radius:12px; overflow:hidden; box-shadow:0 2px 8px rgba(0,0,0,.06); }
.attendance-table th, .attendance-table td { padding:12px; border-bottom:1px solid #eee; text-align:center; }
.attendance-table th { background:#1d3557; color:#fff; font-weight: 600; }
.attendance-table tr:hover { background-color: #f8f9fa; }

/* 出勤率样式 */
.attendance-rate { padding: 4px 8px; border-radius: 4px; font-weight: 600; color: white; }
.attendance-rate.excellent { background: #27ae60; }
.attendance-rate.good { background: #3498db; }
.attendance-rate.normal { background: #f39c12; }
.attendance-rate.poor { background: #e74c3c; }

/* 图表容器 */
.charts, .history-charts { display: grid; grid-template-columns: repeat(auto-fit, minmax(400px, 1fr)); gap: 30px; margin-bottom: 30px; }
.chart-container { background: #fff; border-radius: 12px; padding: 20px; box-shadow: 0 2px 8px rgba(0,0,0,.06); }
.chart-container h4 { margin-bottom: 15px; color: #1d3557; text-align: center; }
.chart-container canvas { max-height: 300px; }

.empty { font-size: 16px; color: #888; text-align: center; margin-top: 40px; }
</style>