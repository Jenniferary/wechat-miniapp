<template>
  <div class="status-page">
    <div class="sidebar">
      <h2>📌 查看加班记录</h2>
      <ul>
        <li @click="$router.push('/hr-dashboard')"><strong>入职待审批列表</strong></li>
          <li @click="$router.push('/hr-profile')">个人档案</li> 
          <li @click="$router.push('/hr-employee')">员工档案</li>
          <li @click="$router.push('/hr-attendance')">考勤打卡</li>
          <li @click="$router.push('/hr-leave')">请假申请</li>
          <li @click="$router.push('/hr-leave-progress')">我的请假记录</li>
          <li @click="$router.push('/hr-leave-review')">请假待审批</li>
          <li @click="$router.push('/hr-overtime-working')">加班申请</li>
          <li @click="$router.push('/hr-overtime-progress')">我的加班记录</li>
          <li @click="$router.push('/hr-overtime-approval')">加班待审批</li>
          <li @click="$router.push('/hr-overtime-approval-history')">加班审批记录</li>
          <li @click="$router.push('/hr-leaving-working')">离职申请</li>
          <li @click="$router.push('/hr-leaving-status')">查看我的离职进度</li>
          <li @click="$router.push('/hr-leavingworking-review')">离职待审批</li>
          <li @click="$router.push('/hr-salary')">工资管理</li>
          <li @click="logout" class="logout">退出系统</li>
      </ul>
    </div>

    <div class="form-section">
      <h3>我的加班记录</h3>

      <table v-if="overtimeRequests.length > 0">
        <thead>
          <tr>
            <th>加班日期</th>
            <th>加班原因</th>
            <th>当前状态</th>
            <th>提交时间</th>
            <th>更新时间</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="req in overtimeRequests" :key="req.request_id">
            <td>{{ formatDate(req.date) }}</td>
            <td>{{ req.reason }}</td>
            <td><span :class="['status', statusClass(req.status)]">{{ req.status }}</span></td>
            <td>{{ formatDate(req.createdAt) }}</td>
            <td>{{ formatDate(req.updatedAt) }}</td>
          </tr>
        </tbody>
      </table>

      <p v-else>没有找到您的加班记录。</p>
    </div>
  </div>
</template>

<script>
export default {
  name: "HrOvertimeProgress",
  data() {
    return {
      overtimeRequests: [],  // 存储当前HR的加班记录
      hrInfo: null, // 存储HR的个人信息
    };
  },
  created() {
    this.loadHrInfo(); // 页面加载时获取HR信息
  },
  methods: {
    async loadHrInfo() {
      try {
        const hrId = localStorage.getItem("hrId"); // 获取当前HR ID
        if (!hrId) {
          alert("未登录");
          this.$router.push("/login");
          return;
        }

        const res = await fetch(`/api/hr/${hrId}`);
        const json = await res.json();
        if (json.status === "success") {
          this.hrInfo = json.data;
          this.fetchOvertimeRequests(); // 获取HR的加班记录
        } else {
          alert(json.message || "加载失败");
        }
      } catch (err) {
        alert("请求错误：" + err.message);
      }
    },

    async fetchOvertimeRequests() {
      try {
        const res = await fetch(`/api/overtime/by-employee?employee_id=${this.hrInfo.id}&employee_type=hr`);

        const json = await res.json();
        if (json.status === "success") {
          this.overtimeRequests = json.data.records;  // 加载HR的加班记录
        } else {
          alert(json.message || "加载加班记录失败");
        }
      } catch (err) {
        alert("请求错误：" + err.message);
      }
    },

    formatDate(dateStr) {
      if (!dateStr) return "无效日期";
      return new Date(dateStr).toLocaleDateString();
    },

    statusClass(status) {
      switch (status) {
        case "待HR审批":
          return "pending";
        case "HR审批通过待店长审批":
          return "in-review";
        case "审批成功":
          return "approved";
        case "已驳回":
          return "rejected";
        default:
          return "unknown";
      }
    },

    logout() {
      localStorage.clear();  // 清空本地存储
      this.$router.push("/login");  // 跳转到登录页面
    },
  },
};
</script>

<style scoped>
/* 与原先的样式基本相同，适用于查看加班记录页面 */
.status-page {
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
table {
  width: 100%;
  border-collapse: collapse;
  background: #fff;
  border-radius: 6px;
  overflow: hidden;
  box-shadow: 0 0 12px rgba(0, 0, 0, 0.05);
}
th,
td {
  padding: 14px 24px;
  text-align: center;
  border-bottom: 1px solid #eee;
  font-size: 16px;
  color: #555;
}
th {
  background-color: #007bff;
  color: white;
  font-weight: 600;
}
tr:hover {
  background-color: #f1f7ff;
}
p {
  font-size: 18px;
  margin-top: 30px;
  color: #888;
  text-align: center;
}
.status {
  display: inline-block;
  padding: 6px 14px;
  border-radius: 20px;
  color: white;
  font-weight: 600;
  font-size: 14px;
}
.status.pending {
  background-color: #f39c12;
}
.status.in-review {
  background-color: #2980b9;
}
.status.approved {
  background-color: #27ae60;
}
.status.rejected {
  background-color: #c0392b;
}
.status.unknown {
  background-color: #7f8c8d;
}
</style>
