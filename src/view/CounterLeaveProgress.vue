<template>
    <div class="resume-page">
      <div class="sidebar">
        <h2>💁‍♀️ 前台管理系统</h2>
        <ul class="menu-list">
          <li :class="{ active: activeSection === 'profile' }" @click="selectSection('profile', '/counter-dashboard')">个人档案</li>
          <li :class="{ active: activeSection === 'dinein' }" @click="selectSection('dinein', '/counter-dinein-order')">管理堂食订单</li>
          <li :class="{ active: activeSection === 'tables' }" @click="selectSection('tables', '/manage-tables')">管理餐桌</li>
          <li>
            <strong @click="toggleSection('delivery')" :class="{ active: activeSection === 'delivery' }">外卖管理</strong>
          </li>
          <li v-if="activeSection === 'delivery'" :class="{ active: activeSubsection === 'assign' }" @click="selectSubsection('assign', '/delivery-assign')">分配外卖员</li>
          <li v-if="activeSection === 'delivery'" :class="{ active: activeSubsection === 'add' }" @click="selectSubsection('add', '/delivery-add')">添加外卖员</li>
          <li v-if="activeSection === 'delivery'" :class="{ active: activeSubsection === 'view' }" @click="selectSubsection('view', '/delivery-view')">查看外卖订单</li>
          <li :class="{ active: activeSection === 'attendance' }" @click="selectSection('attendance', '/counter-attendance')">考勤打卡</li>
          <li :class="{ active: activeSection === 'leave' }" @click="selectSection('leave', '/counter-leave')">请假申请</li>
          <li :class="{ active: activeSection === 'leaveProgress' }" @click="selectSection('leaveProgress', '/counter-leave-progress')">我的请假记录</li>
        </ul>
        <div class="logout" @click="logout">退出系统</div>
      </div>
  
      <div class="form-section">
        <h3>我的请假记录</h3>
        <table class="leave-table" v-if="leaveRecords.length > 0">
          <thead>
            <tr>
              <th>起始日期</th>
              <th>结束日期</th>
              <th>请假原因</th>
              <th>状态</th>
              <th>审批备注</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="record in leaveRecords" :key="record.id">
              <td>{{ formatDate(record.startDate) }}</td>
              <td>{{ formatDate(record.endDate) }}</td>
              <td>{{ record.reason }}</td>
              <td>
                <span :class="statusClass(record.status)">
                  {{ statusText(record.status) }}
                </span>
              </td>
              <td>{{ record.remark || '-' }}</td>
            </tr>
          </tbody>
        </table>
        <p v-else>暂无请假记录</p>
      </div>
    </div>
  </template>
  
  <script>
  export default {
    name: "CounterLeaveProgress",
    data() {
      return {
        activeSection: 'leaveProgress',
        activeSubsection: '',
        counterInfo: null,
        leaveRecords: [],
      };
    },
    created() {
      this.loadCounterInfo();
    },
    methods: {
      async loadCounterInfo() {
        const counterId = localStorage.getItem("counterId");
        if (!counterId) {
          alert("未登录");
          this.$router.push("/login");
          return;
        }
        try {
          const res = await fetch(`/api/counter/${counterId}`);
          const json = await res.json();
          if (json.status === "success") {
            this.counterInfo = json.data;
            this.loadLeaveRecords(counterId);
          } else {
            alert(json.message || "加载失败");
          }
        } catch (err) {
          alert("请求错误：" + err.message);
        }
      },
      async loadLeaveRecords(counterId) {
        try {
          const res = await fetch(`/api/leave/history/${counterId}?employeeType=counter`);
          const json = await res.json();
          if (json.status === "success") {
            this.leaveRecords = json.data.records || [];
          } else {
            alert(json.message || "加载请假记录失败");
          }
        } catch (err) {
          alert("请求错误：" + err.message);
        }
      },
      formatDate(dateStr) {
        return dateStr ? new Date(dateStr).toLocaleDateString() : "-";
      },
      statusText(status) {
        switch (status) {
          case "待HR审批":
            return "待HR审批";
          case "HR审批通过待店长审批":
            return "HR审批通过，待店长审批";
          case "审批成功":
            return "审批成功";
          case "已驳回":
            return "已驳回";
          default:
            return status;
        }
      },
      statusClass(status) {
        switch (status) {
          case "待HR审批":
          case "HR审批通过待店长审批":
            return "status-pending";
          case "审批成功":
            return "status-approved";
          case "已驳回":
            return "status-rejected";
          default:
            return "";
        }
      },
      selectSection(section, path) {
        this.activeSection = section;
        this.activeSubsection = '';
        this.$router.push(path);
      },
      toggleSection(section) {
        this.activeSection = this.activeSection === section ? '' : section;
      },
      selectSubsection(subsection, path) {
        this.activeSubsection = subsection;
        this.activeSection = 'delivery';
        this.$router.push(path);
      },
      logout() {
        localStorage.removeItem("counterId");
        this.$router.push("/login");
      },
    },
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
    box-sizing: border-box;
  }
  .sidebar h2 {
    font-size: 22px;
    border-bottom: 2px solid white;
    padding-bottom: 10px;
    margin-bottom: 20px;
  }
  .menu-list {
    list-style: none;
    padding: 0;
  }
  .menu-list li {
    padding: 10px 0;
    cursor: pointer;
  }
  .menu-list li.active {
    color: #ffd166;
    font-weight: bold;
  }
  .logout {
    color: #ffb3b3;
    margin-top: 20px;
    cursor: pointer;
  }
  .logout:hover {
    color: white;
  }
  .form-section {
    flex: 1;
    background: white;
    padding: 40px 60px;
    box-sizing: border-box;
    overflow-y: auto;
  }
  .form-section h3 {
    font-size: 24px;
    margin-bottom: 30px;
    border-left: 6px solid #007bff;
    padding-left: 14px;
    color: #333;
  }
  .leave-table {
    width: 100%;
    border-collapse: collapse;
  }
  .leave-table th,
  .leave-table td {
    border: 1px solid #ccc;
    padding: 10px 8px;
    text-align: center;
    font-size: 14px;
  }
  .status-pending {
    color: orange;
    font-weight: 600;
  }
  .status-approved {
    color: green;
    font-weight: 600;
  }
  .status-rejected {
    color: red;
    font-weight: 600;
  }
  @media (max-width: 768px) {
    .resume-page {
      flex-direction: column;
    }
    .sidebar {
      width: 100vw;
      text-align: center;
    }
    .form-section {
      width: 100vw;
      padding: 20px;
    }
  }
  </style>
  