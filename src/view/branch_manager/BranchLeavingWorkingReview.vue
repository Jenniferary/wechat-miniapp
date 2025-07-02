<template>
  <div class="status-page">
    <div class="sidebar">
      <h2>📌 离职申请审批</h2>
      <ul>
        <li @click="$router.push('/branch-dashboard')">返回主页</li>
        <li><strong>离职申请审批</strong></li>
        <li @click="logout" class="logout">退出系统</li>
      </ul>
    </div>

    <div class="form-section">
      <h3>待审批离职申请</h3>

      <table v-if="leaveRequests.length > 0">
        <thead>
          <tr>
            <th>申请ID</th>
            <th>员工姓名</th>
            <th>离职原因</th>
            <th>提交时间</th>
            <th>当前状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="req in leaveRequests" :key="req.id">
            <td>{{ req.id }}</td>
            <td>{{ req.name }}</td>
            <td>{{ req.reason }}</td>
            <td>{{ formatDate(req.createdAt) }}</td>
            <td>
              <span :class="['status', statusClass(req.status)]">
                {{ req.status }}
              </span>
            </td>
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

      <p v-else>暂无待审批的离职申请</p>
    </div>
  </div>
</template>

<script>
export default {
  name: "BranchLeavingWorkingReview",
  data() {
    return {
      leaveRequests: [],  // 离职申请列表
      branchManager: null, // 店长信息
    };
  },
  created() {
    this.loadBranchManagerInfo();
  },
  methods: {
    // 加载店长信息
    async loadBranchManagerInfo() {
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
        this.branchManager = json.data;
        console.log("Branch Manager Info:", this.branchManager); // 调试：确保店长信息已加载
        this.fetchLeaveRequests(); // 获取待审批的离职申请
      } catch (err) {
        alert("加载店长信息失败：" + err.message);
      }
    },

    // 获取待审批的离职申请
    async fetchLeaveRequests() {
      try {
        const managerId = localStorage.getItem("managerId");
        if (!managerId) {
          alert("未登录");
          this.$router.push("/login");
          return;
        }

        const res = await fetch(`/api/leaving-working/by-manager?managerId=${managerId}`);
        const json = await res.json();
        console.log("Response from /api/leaving-working/by-manager:", json); // 调试：打印返回的 JSON 数据
        if (json.status !== "success") throw new Error("获取待审批离职申请失败");

        this.leaveRequests = json.data || []; // 更新离职申请列表
        console.log("Leave Requests:", this.leaveRequests); // 调试：确保待审批离职申请已加载
      } catch (err) {
        alert("加载离职申请失败：" + err.message);
      }
    },

    // 处理审批操作
    async handleDecision(id, decision) {
      try {
        const res = await fetch(`/api/leaving-working/${id}/${decision}`, {
          method: "PUT",
        });
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        const json = await res.json();
        if (json.status === "success") {
          alert(`已${decision === "approve" ? "通过" : "驳回"}该离职申请`);
          this.fetchLeaveRequests(); // 刷新列表
        } else {
          alert("操作失败：" + (json.message || ""));
        }
      } catch (err) {
        alert("请求错误：" + err.message);
      }
    },

    // 格式化日期
    formatDate(dateStr) {
      if (!dateStr) return "";
      return new Date(dateStr).toLocaleDateString();
    },

    // 根据离职申请的状态设置样式
    statusClass(status) {
      switch (status) {
        case "待HR审批":
          return "pending";  // 使用黄色表示待审批
        case "HR审批通过待店长审批":
          return "in-review"; // 使用蓝色表示待店长审批
        case "审批成功":
          return "approved";  // 使用绿色表示审批通过
        case "已驳回":
          return "rejected";  // 使用红色表示驳回
        default:
          return "unknown";  // 未知状态
      }
    },

    // 退出系统
    logout() {
      localStorage.clear();
      this.$router.push("/login");
    },
  },
};
</script>

<style scoped>
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

  table,
  th,
  td {
    font-size: 14px;
    padding: 10px;
  }
}
</style>
