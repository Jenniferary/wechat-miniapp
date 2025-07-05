<template>
  <div class="resume-page">
    <!-- 左侧菜单 -->
    <div class="sidebar">
      <h2>📌离职审批</h2>
      <ul>
        <li @click="$router.push('/br-leavingworking-Progress')">审批待处理列表</li>

        <li @click="$router.push('/branch-dashboard')">返回主页</li>
        <li @click="logout" class="logout">退出系统</li>
      </ul>
    </div>

    <!-- 右侧内容 -->
    <div class="form-section">
      <h3>待店长审批离职申请</h3>

      <table v-if="leaveWorkingRequests.length > 0">
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
          <tr v-for="req in leaveWorkingRequests" :key="req.id">
            <td>{{ req.id }}</td>
            <td>{{ req.name }}</td> <!-- 显示员工姓名 -->
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
  name: "BrLeavingWorkingReview",
  data() {
    return {
      leaveWorkingRequests: [],  // 离职申请列表
      managerInfo: null,  // 店长信息
    };
  },
  created() {
    this.loadManagerInfo();
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
        this.fetchLeaveWorkingRequests(); // 获取待审批离职申请
      } catch (err) {
        alert("加载店长信息失败：" + err.message);
      }
    },

   async fetchLeaveWorkingRequests() {
  try {
    const managerId = this.managerInfo.id;  // 获取店长ID
    const url = `/api/leaving-working/by-manager?managerId=${managerId}`;  // 根据店长ID查询
    const res = await fetch(url);
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    
    const json = await res.json();
    console.log("返回的离职申请数据:", json);  // 打印返回数据

    if (json.status === "success") {
      this.leaveWorkingRequests = json.data;  // 更新离职申请列表
      console.log("更新的离职申请列表:", this.leaveWorkingRequests);  // 打印更新后的数据
    } else {
      alert("加载离职申请失败：" + (json.message || ""));
    }
  } catch (err) {
    alert("加载离职申请失败：" + err.message);
  }
},


    // 审批离职申请
    async handleDecision(id, decision) {
      try {
        const res = await fetch(`/api/leaving-working/${id}/manager-approve`, {
          method: "PUT",
        });
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        const json = await res.json();
        if (json.status === "success") {
          alert(`已${decision === "approve" ? "通过" : "驳回"}该离职申请`);
          this.fetchLeaveWorkingRequests(); // 刷新列表
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

    // 状态的样式
    statusClass(status) {
      switch (status) {
        case "已提交待HR审批":
          return "pending";  // 使用黄色表示待审批
        case "HR审批通过待店长审批":
          return "in-review"; // 使用蓝色表示待店长审批
        case "审批成功":
          return "approved";  // 使用绿色表示审批通过
        case "已驳回":
          return "rejected";  // 使用红色表示驳回
        case "已离职":
          return "resigned";  // 使用灰色表示已离职
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
/* 页面的主容器 */
.resume-page {
  display: flex;
  width: 100vw;
  height: 100vh;
  font-family: "Segoe UI", Tahoma, Geneva, Verdana, sans-serif;
}

/* 左侧菜单样式 */
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
  color: white;
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

/* 右侧内容区域样式 */
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

/* 表格样式 */
table {
  width: 100%;
  border-collapse: collapse;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
}

th, td {
  padding: 12px 16px;
  text-align: center;
  border: 1px solid #ddd;
}

th {
  background-color: #007bff;
  color: white;
}

tr:hover {
  background-color: #f1f7ff;
}

/* 状态标签样式 */
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

.status.resigned {
  background-color: #7f8c8d;
}

/* 按钮样式 */
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
