<template>
  <div class="leave-approval-page">
    <div class="sidebar">
      <h2>📌 店长请假审批</h2>
      <ul>
        <li @click="$router.push('/branch-manager-profile')">个人档案</li>
        <li @click="$router.push('/branch-employee')">员工入职审批</li>
        <li @click="$router.push('/branch-dashboard')">返回主页</li>
        <li @click="logout" class="logout">退出系统</li>
      </ul>
    </div>

    <div class="form-section">
      <h3>待审批请假申请</h3>

      <table v-if="leaveRequests.length > 0">
        <thead>
          <tr>
            <th>员工ID</th>
            <th>职位</th>
            <th>请假起始</th>
            <th>请假结束</th>
            <th>原因</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="req in leaveRequests" :key="req.id">
            <td>{{ req.employeeId }}</td>
            <td>{{ req.employeeType }}</td>
            <td>{{ formatDate(req.startDate) }}</td>
            <td>{{ formatDate(req.endDate) }}</td>
            <td>{{ req.reason }}</td>
            <td><span :class="['status', statusClass(req.status)]">{{ req.status }}</span></td>
            <td>
              <button
                v-if="req.status === 'HR审批通过待店长审批'"
                @click="handleDecision(req.id, 'approve')"
                class="btn-approve"
              >通过</button>
              <button
                v-if="req.status === 'HR审批通过待店长审批'"
                @click="handleDecision(req.id, 'reject')"
                class="btn-reject"
              >驳回</button>
            </td>
          </tr>
        </tbody>
      </table>

      <p v-else>暂无待审批的请假申请</p>
    </div>
  </div>
</template>

<script>
export default {
  name: "BranchLeaving",
  data() {
    return {
      leaveRequests: [], // 请假申请列表
      managerInfo: null, // 店长信息
    };
  },
  created() {
    this.loadManagerInfo(); // 页面加载时获取店长信息
  },
  methods: {
    // 加载店长信息
    async loadManagerInfo() {
      try {
        const managerId = localStorage.getItem("managerId");
        if (!managerId) {
          alert("未登录");
          this.$router.push("/login");
          return;
        }
        const res = await fetch(`/api/branch-managers/${managerId}`);
        const json = await res.json();
        if (json.status !== "success") throw new Error("获取店长信息失败");
        this.managerInfo = json.data;
        console.log("Manager Info:", this.managerInfo); // 调试：确保店长信息已加载
        this.fetchLeaveRequests(); // 获取待审批的请假申请
      } catch (err) {
        alert("加载店长信息失败：" + err.message);
      }
    },
    // 获取待审批的请假申请
    async fetchLeaveRequests() {
      try {
        const url = `/api/leave/by-branch?branchId=${this.managerInfo.branchId}&role=manager&managerId=${this.managerInfo.id}&managerType=branch_manager`;
        console.log("Request URL:", url); // 打印请求 URL 以调试
        const res = await fetch(url);
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        const json = await res.json();
        console.log("Fetched Leave Requests:", json); // 打印返回的请假申请数据
        this.leaveRequests = json;
      } catch (err) {
        alert("加载请假申请失败：" + err.message);
      }
    },
    // 审批操作（通过或驳回）
    async handleDecision(id, decision) {
      try {
        const res = await fetch(`/api/leave/manager-approve/${id}?decision=${decision}`, {
          method: "PUT",
        });
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        const json = await res.json();
        if (json.status === "success") {
          alert(`已${decision === "approve" ? "通过" : "驳回"}该请假申请`);
          this.fetchLeaveRequests(); // 重新加载待审批申请
        } else {
          alert("操作失败：" + (json.message || ""));
        }
      } catch (err) {
        alert("请求错误：" + err.message);
      }
    },
    // 格式化日期为本地日期
    formatDate(dateStr) {
      if (!dateStr) return "";
      return new Date(dateStr).toLocaleDateString();
    },
    // 根据状态为每个请假申请设置不同的样式
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
    // 退出登录
    logout() {
      localStorage.clear();
      this.$router.push("/login");
    },
  },
};
</script>

<style scoped>
.leave-approval-page {
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
.btn-approve {
  background-color: #27ae60;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
  margin-right: 8px;
}
.btn-approve:hover {
  background-color: #219150;
}
.btn-reject {
  background-color: #c0392b;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
}
.btn-reject:hover {
  background-color: #992d22;
}
</style>
