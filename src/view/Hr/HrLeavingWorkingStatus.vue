<template>
  <div class="status-page">
    <div class="sidebar">
      <h2>📌 查看离职申请进度</h2>
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
      <h3>我的离职申请状态</h3>

      <table v-if="leaveRequests.length">
        <thead>
          <tr>
            <th>申请ID</th>
            <th>离职原因</th>
            <th>当前状态</th>
            <th>提交时间</th>
            <th>更新时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="request in leaveRequests" :key="request.id">
            <td>{{ request.id }}</td>
            <td>{{ request.reason }}</td>
            <td>
              <span :class="['status', statusClass(request.status)]">
                {{ request.status }}
              </span>
            </td>
            <td>{{ formatDate(request.createdAt) }}</td>
            <td>{{ formatDate(request.updatedAt) }}</td>
            <td>
              <button
                v-if="request.status === '审批成功'"
                @click="confirmLeave(request.id)"
                class="btn-approve"
              >
                确认离职
              </button>
            </td>
          </tr>
        </tbody>
      </table>

      <p v-else>没有找到您的离职申请记录。</p>
    </div>
  </div>
</template>

<script>
export default {
  name: "HrLeavingWorkStatus",
  data() {
    return {
      leaveRequests: [],  // 存储当前HR的离职申请
    };
  },
  created() {
    this.loadLeaveRequests();
  },
  methods: {
    // 加载当前HR的离职申请状态
    async loadLeaveRequests() {
      try {
        const hrId = localStorage.getItem("hrId");  // 获取当前HR ID
        if (!hrId) {
          alert("未登录");
          this.$router.push("/login");
          return;
        }

        // 调用后端接口获取该HR的离职申请
        const res = await fetch(`/api/leaving-working/hr-by-applicant?applicantId=${hrId}`);
        if (!res.ok) throw new Error("无法获取离职申请状态");

        const data = await res.json();

        // 如果返回的数据有离职申请记录，赋值给 leaveRequests
        if (data.status === "success" && data.data.length > 0) {
          this.leaveRequests = data.data;
        } else {
          this.leaveRequests = [];  // 如果没有离职申请，设置为空数组
        }
      } catch (err) {
        alert("加载离职申请状态失败：" + err.message);
      }
    },

    // 格式化日期
    formatDate(dateStr) {
      const date = new Date(dateStr);
      return date.toLocaleDateString("zh-CN", {
        year: "numeric",
        month: "2-digit",
        day: "2-digit",
      });
    },

    // 设置状态样式
    statusClass(status) {
      switch (status) {
        case "已提交待HR审批":
          return "pending";
        case "HR审批通过待店长审批":
          return "in-review";
        case "审批成功":
          return "approved";
        case "已驳回":
          return "rejected";
        case "已离职":
          return "resigned";
        default:
          return "unknown";
      }
    },

    // 确认离职
    async confirmLeave(requestId) {
      const hrId = parseInt(localStorage.getItem("hrId"), 10);  // 获取 HR ID
      if (!hrId) {
        alert("未登录或无效的HR ID");
        this.$router.push("/login");
        return;
      }

      if (confirm("确认离职该员工吗？")) {
        try {
          // 发送删除请求，传递 requestId 和 hrId
          const res = await fetch(`/api/leaving-working/hr-confirm-leave/${hrId}/${requestId}`, {
            method: "DELETE",
          });

          const json = await res.json();
          if (json.status === "success") {
            alert("HR离职确认成功");

            // 等待 5 秒后跳转到登录页面
            setTimeout(() => {
              this.logout();  // 调用退出方法
            }, 5000); // 5000 毫秒 = 5 秒

            this.loadLeaveRequests(); // 刷新离职申请状态
          } else {
            alert("离职确认失败：" + (json.message || ""));
          }
        } catch (err) {
          alert("等待一会跳转");
        }
      }
    },

    // 退出系统
    logout() {
      localStorage.removeItem("hrId");
      this.$router.push("/login");
    },
  },
};
</script>

<style scoped>
/* 样式保持一致 */
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
  font-size: 18px;
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
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
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

button {
  background-color: #27ae60;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
}

button:hover {
  background-color: #219150;
}
</style>
