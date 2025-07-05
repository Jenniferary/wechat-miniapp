<template>
    <div class="resume-page">
      <!-- 左侧导航栏，改为HR菜单 -->
      <div class="sidebar">
        <h2>🧑‍💼 工资信息</h2>
        <ul>
          <li @click="$router.push('/hr-dashboard')">入职待审批列表</li>
          <li @click="$router.push('/hr-profile')">个人档案</li> 
          <li @click="$router.push('/hr-employee')">员工档案</li>
          <li @click="$router.push('/hr-attendance')">考勤打卡</li>
          <li @click="$router.push('/hr-leave')">请假申请</li>
          <li @click="$router.push('/hr-leave-progress')">我的请假记录</li>
          <li @click="$router.push('/hr-leave-review')">请假待审批</li>
          <li @click="$router.push('/hr-overtime-working')">加班申请</li>
          <li @click="$router.push('/hr-overtime-approval')">加班待审批</li>
          <li @click="$router.push('/hr-overtime-approval-history')">加班审批记录</li>
          <li @click="$router.push('/hr-leaving-working')">离职申请</li>
          <li @click="$router.push('/hr-leavingworking-review')">离职待审批</li>
          <li><strong>工资信息</strong></li>
          <li @click="logout" class="logout">退出系统</li>
        </ul>
      </div>
  
      <!-- 主体内容 -->
      <div class="form-section">
        <h3>我的工资</h3>
  
        <!-- 工具栏 -->
        <div class="form-row">
          <label>月份：</label>
          <input type="month" v-model="month" />
          <button class="btn-generate" @click="generateSalary">生成工资</button>
        </div>
  
        <!-- 工资表 -->
        <table class="salary-table" v-if="salaries.length">
          <thead>
            <tr>
              <th>工资月份</th>
              <th>基础工资</th>
              <th>绩效奖金</th>
              <th>加班补贴</th>
              <th>请假扣款</th>
              <th>实发工资</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="s in salaries" :key="s.salaryId">
              <td>{{ s.salaryMonth }}</td>
              <td>{{ fmt(s.baseSalary) }}</td>
              <td>{{ fmt(s.performanceBonus) }}</td>
              <td>{{ fmt(s.overtimeBonus) }}</td>
              <td>{{ fmt(s.leaveDeduction) }}</td>
              <td class="net">{{ fmt(s.netSalary) }}</td>
              <td>
                <button class="btn-detail" @click="showDetail(s.salaryMonth)">查看详情</button>
              </td>
            </tr>
          </tbody>
        </table>
  
        <p v-else class="empty-text">暂无工资记录</p>
      </div>
  
      <!-- 详情弹窗 -->
      <transition name="fade">
        <div v-if="detailVisible" class="modal-overlay" @click.self="detailVisible = false">
          <div class="modal-content">
            <h4>工资详情 - {{ detailMonth }}</h4>
            <ul>
              <li><span>应上班天数（工作日）:</span> {{ detail.workDaysInMonth }}</li>
              <li><span>实际出勤天数:</span> {{ detail.attendanceDays }}</li>
              <li><span>请假天数:</span> {{ detail.leaveDays }}</li>
              <li><span>旷工天数:</span> {{ detail.absentDays }}</li>
              <li><span>加班次数:</span> {{ detail.overtimeCount }}</li>
              <li><span>绩效得分:</span> {{ detail.totalScore }}</li>
            </ul>
            <button class="btn-close" @click="detailVisible = false">关闭</button>
          </div>
        </div>
      </transition>
    </div>
  </template>
  
  <script>
  export default {
    name: "HrSalary",
    data() {
      return {
        hrId: localStorage.getItem("hrId"),
        month: new Date().toISOString().slice(0, 7),
        salaries: [],
        detailVisible: false,
        detailMonth: '',
        detail: {},
      };
    },
    created() {
      this.fetchSalaryList();
    },
    methods: {
        async fetchSalaryList() {
  try {
    const employeeId = this.hrId;
    const employeeType = 'hr';
    const res = await fetch(`/api/salaries/list?employeeId=${employeeId}&employeeType=${employeeType}`);
    const json = await res.json();
    if (json.status === "success") {
      this.salaries = json.data;  // 这里后端已经过滤，不用再filter了
    } else {
      alert(json.message || "获取工资失败");
    }
  } catch (e) {
    alert("请求错误：" + e.message);
  }
},

      async generateSalary() {
        try {
          const res = await fetch(
            `/api/salaries/generate?employeeId=${this.hrId}&employeeType=hr&branchId=1&month=${this.month}`,
            { method: "POST" }
          );
          const json = await res.json();
          if (json.status === "success") {
            alert("生成成功");
            this.fetchSalaryList();
          } else {
            alert(json.message || "生成失败");
          }
        } catch (e) {
          alert("请求错误：" + e.message);
        }
      },
      fmt(val) {
        return parseFloat(val).toFixed(2);
      },
      logout() {
        localStorage.removeItem("hrId");
        this.$router.push("/login");
      },
      async showDetail(month) {
        this.detailVisible = true;
        this.detailMonth = month;
        try {
          const res = await fetch(
            `/api/salaries/detail?employeeId=${this.hrId}&employeeType=hr&month=${month}`
          );
          const json = await res.json();
          if (json.status === "success") {
            this.detail = json.data;
          } else {
            alert(json.message || "获取明细失败");
            this.detail = {};
            this.detailVisible = false;
          }
        } catch (e) {
          alert("请求错误：" + e.message);
          this.detail = {};
          this.detailVisible = false;
        }
      }
    }
  };
  </script>
  
  <style scoped>
  .resume-page {
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
    width: calc(100vw - 220px);
    background: white;
    padding: 40px 60px;
    box-sizing: border-box;
    overflow-y: auto;
  }
  .form-section h3 {
    font-size: 30px;
    margin-bottom: 35px;
    font-weight: 700;
    color: #34495e;
    border-left: 8px solid #4c91e3;
    padding-left: 18px;
    letter-spacing: 0.5px;
  }
  
  /* 工具栏 */
  .form-row {
    margin-bottom: 30px;
    display: flex;
    align-items: center;
    gap: 18px;
  }
  .form-row label {
    width: 120px;
    font-weight: 700;
    color: #34495e;
    font-size: 17px;
  }
  .form-row input[type="month"] {
    padding: 10px 15px;
    font-size: 16px;
    border: 2px solid #4c91e3;
    border-radius: 8px;
    outline-offset: 2px;
    transition: border-color 0.3s ease;
    width: 180px;
    background: white;
  }
  .form-row input[type="month"]:focus {
    border-color: #3a70d8;
  }
  
  /* 按钮样式 */
  button {
    cursor: pointer;
    border: none;
    border-radius: 20px;
    font-weight: 700;
    transition: all 0.3s ease;
    box-shadow: 0 6px 10px rgba(76, 145, 227, 0.3);
  }
  .btn-generate {
    background: #4c91e3;
    color: white;
    padding: 12px 32px;
    font-size: 16px;
    box-shadow: 0 6px 12px rgba(76, 145, 227, 0.6);
  }
  .btn-generate:hover {
    background: #3a70d8;
    box-shadow: 0 8px 15px rgba(58, 112, 216, 0.9);
  }
  .btn-detail {
    background: #81c784;
    color: #1b5e20;
    padding: 8px 20px;
    font-size: 14px;
    box-shadow: 0 4px 8px rgba(129, 199, 132, 0.5);
    margin-bottom: 10px;
  }
  .btn-detail:hover {
    background: #66bb6a;
    box-shadow: 0 6px 12px rgba(102, 187, 106, 0.7);
  }
  .btn-close {
    margin-top: 20px;
    width: 100%;
    background: #4d7bcf;
    color: white;
    padding: 10px 0;
    font-size: 16px;
    box-shadow: 0 6px 12px rgba(138, 161, 231, 0.8);
  }
  .btn-close:hover {
    background: #658eed;
    box-shadow: 0 8px 15px rgb(105, 142, 236);
  }
  
  /* 表格 */
  .salary-table {
    width: 100%;
    border-collapse: separate;
    border-spacing: 0 12px;
    margin-top: 20px;
    font-size: 15px;
    color: #34495e;
    box-shadow: 0 2px 15px rgb(0 0 0 / 0.07);
    border-radius: 12px;
    overflow: hidden;
    background: white;
  }
  .salary-table thead tr {
    background: #4c91e3;
    color: white;
    font-weight: 700;
  }
  .salary-table thead th {
    padding: 14px 18px;
    text-align: center;
    letter-spacing: 0.05em;
  }
  .salary-table tbody tr {
    background: #f0f7ff;
    transition: background-color 0.3s ease;
    border-radius: 12px;
    cursor: pointer;
  }
  .salary-table tbody tr:hover {
    background: #d9e8ff;
  }
  .salary-table tbody td {
    padding: 14px 18px;
    text-align: center;
  }
  .salary-table td.net {
    font-weight: 900;
    color: #2e7d32;
  }
  
  /* 空数据文本 */
  .empty-text {
    color: #95a5a6;
    font-size: 16px;
    margin-top: 30px;
    text-align: center;
  }
  
  /* 弹窗样式 */
  .modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    width: 100vw;
    height: 100vh;
    background: rgba(15, 23, 42, 0.85);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 9999;
  }
  .modal-content {
    background: white;
    padding: 30px 40px;
    border-radius: 20px;
    width: 360px;
    box-shadow: 0 12px 30px rgba(76, 145, 227, 0.4);
    text-align: center;
  }
  .modal-content h4 {
    font-weight: 800;
    color: #34495e;
    font-size: 22px;
    margin-bottom: 25px;
    letter-spacing: 0.05em;
  }
  .modal-content ul {
    list-style: none;
    padding-left: 0;
    text-align: left;
    font-size: 16px;
    color: #555;
    line-height: 2.2em;
    letter-spacing: 0.03em;
  }
  .modal-content ul li span {
    font-weight: 700;
    color: #34495e;
  }
  
  /* 动画效果 */
  .fade-enter-active, .fade-leave-active {
    transition: opacity 0.3s ease;
  }
  .fade-enter-from, .fade-leave-to {
    opacity: 0;
  }
  
  @media (max-width: 768px) {
    .resume-page {
      flex-direction: row;
      overflow-x: auto;
    }
    .sidebar {
      width: 260px;
      height: 100vh;
      box-shadow: 2px 0 12px rgba(0,0,0,0.3);
    }
    .form-section {
      width: calc(100vw - 260px);
      padding: 30px 25px;
      border-radius: 0 30px 30px 0;
      box-shadow: inset 0 0 30px #c0d4f9;
      overflow-x: auto;
    }
  }
  </style>
  