<template>
  <div class="status-page">
    <div class="sidebar">
      <h2>📌 查看离职申请状态</h2>
      <ul>
        <li @click="$router.push('/chef-dashboard')">返回主页</li>
        <li><strong>查看进度</strong></li>
        <li @click="logout" class="logout">退出系统</li>
      </ul>
    </div>

    <div class="form-section">
      <h3>我的离职申请状态</h3>

      <table v-if="leaveRequest">
        <thead>
          <tr>
            <th>申请ID</th>
            <th>离职原因</th>
            <th>当前状态</th>
            <th>提交时间</th>
            <th>更新时间</th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <td>{{ leaveRequest.id }}</td>
            <td>{{ leaveRequest.reason }}</td>
            <td>
              <span :class="['status', statusClass(leaveRequest.status)]">
                {{ leaveRequest.status }}
              </span>
            </td>
            <td>{{ formatDate(leaveRequest.createdAt) }}</td>
            <td>{{ formatDate(leaveRequest.updatedAt) }}</td>
          </tr>
        </tbody>
      </table>

      <p v-else>没有找到您的离职申请记录。</p>
    </div>
  </div>
</template>

<script>
export default {
  name: "EmployeeStatus",
  data() {
    return {
      leaveRequest: null,
    };
  },
  created() {
    this.loadLeaveRequestStatus();
  },
  methods: {
    async loadLeaveRequestStatus() {
      try {
        const chefId = localStorage.getItem("chefId");  // 使用 chefId 来获取员工信息
        if (!chefId) {
          alert("未登录");
          this.$router.push("/login");
          return;
        }

        // 调用后端接口获取某个员工的所有离职申请
        const res = await fetch(`/api/leaving-working/by-applicant?applicantId=${chefId}`);
        if (!res.ok) throw new Error("无法获取离职申请状态");

        const data = await res.json();

        // 假设员工只有一个离职申请，取第一个记录
        if (data.length > 0) {
          this.leaveRequest = data[0];  // 获取离职申请的第一个记录
        } else {
          this.leaveRequest = null;  // 如果没有离职申请，设置为 null
        }
      } catch (err) {
        alert("加载离职申请状态失败：" + err.message);
      }
    },
    formatDate(dateStr) {
      if (!dateStr) return "";
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
      localStorage.clear();
      this.$router.push("/login");
    },
  },
};
</script>

<style scoped>
html, body, #app {
  margin: 0;
  padding: 0;
  height: 100%;
  width: 100%;
  overflow: hidden;
  background: none;
}

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

th, td {
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

@media (max-width: 768px) {
  .status-page {
    flex-direction: column;
  }

  .form-section {
    width: 100vw;
    padding: 30px 20px;
  }

  .sidebar {
    width: 100vw;
    text-align: center;
  }

  table, th, td {
    font-size: 14px;
    padding: 10px;
  }
}
</style>
