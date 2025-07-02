<template>
    <div class="resume-page">
      <div class="sidebar">
        <h2>👨‍🍳 请假管理</h2>
        <ul>
          <li @click="$router.push('/chef-dashboard')">个人档案</li>
          <li @click="$router.push('/chef-attendance')">考勤打卡</li>
          <li @click="$router.push('/chef-leave')">请假申请</li>
          <li><strong>我的请假记录</strong></li>
          <li @click="logout" class="logout">退出系统</li>
        </ul>
      </div>
  
      <div class="form-section">
        <h3>我的请假流程</h3>
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
  name: "ChefLeaveProcess",
  data() {
    return {
      chefInfo: null,
      leaveRecords: [],
    };
  },
  created() {
    this.loadChefInfo();
  },
  methods: {
    async loadChefInfo() {
      const chefId = localStorage.getItem("chefId");
      if (!chefId) {
        alert("未登录");
        this.$router.push("/login");
        return;
      }
      try {
        const resUser = await fetch(`/api/chef/${chefId}`);
        const jsonUser = await resUser.json();
        if (jsonUser.status === "success") {
          this.chefInfo = jsonUser.data;
          this.loadLeaveRecords(chefId);
        } else {
          alert(jsonUser.message || "加载用户信息失败");
        }
      } catch (err) {
        alert("请求错误：" + err.message);
      }
    },
    async loadLeaveRecords(chefId) {
      try {
        const res = await fetch(`/api/leave/history/${chefId}?employeeType=chef`);
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
      if (!dateStr) return "-";
      const d = new Date(dateStr);
      return d.toLocaleDateString();
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
    logout() {
      localStorage.removeItem("chefId");
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
    display: flex;
    flex-direction: column;
    box-sizing: border-box;
  }
  .sidebar h2 {
    margin-bottom: 30px;
    font-size: 22px;
    border-bottom: 2px solid white;
    padding-bottom: 10px;
  }
  .sidebar ul {
    list-style: none;
    padding: 0;
    margin: 0;
    flex: 1;
  }
  .sidebar li {
    padding: 10px 0;
    font-size: 15px;
    cursor: pointer;
  }
  .sidebar li strong {
    font-weight: bold;
  }
  .logout {
    color: #ffb3b3;
    transition: color 0.3s ease;
  }
  .logout:hover {
    color: white;
    font-weight: bold;
  }
  .form-section {
    width: calc(100vw - 240px);
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
  