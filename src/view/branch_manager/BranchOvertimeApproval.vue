<template>
  <div class="overtime-approval-page">
    <div class="sidebar">
      <h2>📌 店长加班审批</h2>
      <ul>
         <li @click="$router.push('/branch-overtime-approval')">审批待处理列表</li>
        <li @click="$router.push('/branch-dashboard')">返回主页</li>
        <li @click="logout" class="logout">退出系统</li>
      </ul>
    </div>

    <div class="form-section">
      <h3>待审批加班申请</h3>

      <table v-if="overtimeRequests.length > 0">
        <thead>
          <tr>
            <th>申请ID</th>
            <th>员工ID</th>
            <th>职位</th>
            <th>加班日期</th>
            <th>加班原因</th>
            <th>当前状态</th>
            <th>提交时间</th>
            <th>更新时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="req in overtimeRequests" :key="req.id">
            <td>{{ req.id }}</td>
            <td>{{ req.employeeId }}</td>
            <td>{{ req.employeeType }}</td>
            <td>{{ formatDate(req.date) }}</td>
            <td>{{ req.reason }}</td>
            <td><span :class="['status', statusClass(req.status)]">{{ req.status }}</span></td>
            <td>{{ formatDate(req.createdAt) }}</td>
            <td>{{ formatDate(req.updatedAt) }}</td>
            <td>
              <button
                v-if="req.status === 'HR审批通过待店长审批'"
                @click="handleDecision(req.id, 'approve')"
                class="btn-approve"
              >
                通过
              </button>
              <button
                v-if="req.status === 'HR审批通过待店长审批'"
                @click="handleDecision(req.id, 'reject')"
                class="btn-reject"
              >
                驳回
              </button>
            </td>
          </tr>
        </tbody>
      </table>

      <p v-else>暂无待审批的加班申请</p>
    </div>
  </div>
</template>

<script>
export default {
  name: "BranchOvertimeApproval",
  data() {
    return {
      overtimeRequests: [], // 加班申请列表
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
        this.fetchOvertimeRequests(); // 获取待审批的加班申请
      } catch (err) {
        alert("加载店长信息失败：" + err.message);
      }
    },
    async fetchOvertimeRequests() {
  try {
    const res = await fetch(`/api/overtime/by-branch?branchId=${this.managerInfo.branchId}&role=manager`);
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    
    const json = await res.json();
    console.log("Fetched Overtime Requests:", json); // 打印返回的 JSON 数据

    // 直接将返回的数组赋值给 overtimeRequests
    if (Array.isArray(json)) {
      this.overtimeRequests = json;
    } else {
      alert("没有找到加班记录！");
    }
  } catch (err) {
    alert("加载加班申请失败：" + err.message);
  }
},

  async handleDecision(requestId, decision) {
  try {
    // 检查 requestId 是否存在
    console.log("Request ID:", requestId); // 打印请求ID，确保它是有效的数字

    const res = await fetch(`/api/overtime/manager-approve/${requestId}?decision=${decision}`, {
      method: "PUT",
    });

    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    const json = await res.json();
    if (json.status === "success") {
      alert(`已${decision === "approve" ? "通过" : "驳回"}该加班申请`);
      this.fetchOvertimeRequests(); // 重新加载待审批申请
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
    // 根据状态为每个加班申请设置不同的样式
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
.overtime-approval-page {
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

.sidebar li:hover {
  background-color: #ffb3b3;
  color: #fff;
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

h3 {
  font-size: 24px;
  color: #333;
  margin-bottom: 30px;
  border-bottom: 2px solid #007bff;
  padding-bottom: 10px;
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

button {
  background-color: #27ae60;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
  margin-right: 8px;
}

button:hover {
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
