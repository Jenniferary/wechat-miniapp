<template> 
  <div class="status-page">
    <div class="sidebar">
      <h2>📌 查看离职申请状态</h2>
      <ul>
        <li @click="$router.push('/chef-dashboard')">个人档案</li>
        <li @click="$router.push('/chef-attendance')">考勤打卡</li>
          <li @click="$router.push('/chef-leave')">请假申请</li>
          <li @click="$router.push('/chef-leave-progress')">我的请假记录</li>
          <li @click="$router.push('/chef-overtime-working')">加班申请</li>
          <li @click="$router.push('/chef-overtime-progress')">我的加班记录</li>
          <li @click="$router.push('/chef-leaving-working')">离职申请</li>
          <li @click="$router.push('/employ-leaving-Status')">查看离职申请进度</li>
          <li @click="$router.push('/chef-salary')">工资管理</li>
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
            <th>操作</th>
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
            <td>
              <!-- 仅当状态为“审批成功”时，显示“确认离职”按钮 -->
              <button
                v-if="leaveRequest.status === '审批成功'"
                @click="confirmLeave(leaveRequest.id)"
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
  name: "EmployeeStatus",
  data() {
    return {
      leaveRequest: null,  // 存储当前员工的离职申请
    };
  },
  created() {
    this.loadLeaveRequestStatus();
  },
  methods: {
    // 加载当前员工的离职申请状态
    async loadLeaveRequestStatus() {
      try {
        const chefId = localStorage.getItem("chefId");  // 获取当前员工ID
        if (!chefId) {
          alert("未登录");
          this.$router.push("/login");
          return;
        }

        // 调用后端接口获取该员工的离职申请
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
    console.log("原始日期字符串:", dateStr);  // 打印原始日期字符串
    if (!dateStr) return "无效日期";  // 防止无效日期传入
    console.log("原始日期字符串:", dateStr);  // 打印原始日期字符串
    // 直接尝试解析ISO 8601格式的日期
    const date = new Date(dateStr);
    console.log("解析的日期:", new Date(dateStr));  // 查看解析后的日期对象

    // 检查日期是否有效
    if (isNaN(date.getTime())) {
      console.error("无效日期:", dateStr);  // 打印无效日期以便调试
      return "无效日期";
    }
    
    // 使用 toLocaleDateString 来格式化日期
    return date.toLocaleDateString("zh-CN", {
      year: "numeric",
      month: "2-digit",
      day: "2-digit",
    });
  },

    // 状态样式
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
        case "已离职":
          return "resigned";
        default:
          return "unknown";
      }
    },

    // 确认离职操作
    async confirmLeave(id) {
      if (confirm("确认离职该员工吗？")) {
        try {
          const res = await fetch(`/api/leaving-working/chef/${id}/confirm-leave`, {
            method: "PUT",
          });

          const json = await res.json();
          if (json.status === "success") {
            alert("员工离职确认成功");

            // 等待 5 秒后跳转到登录页面
            setTimeout(() => {
              this.logout();  // 调用退出方法
            }, 5000); // 5000 毫秒 = 5 秒

            this.loadLeaveRequestStatus(); // 刷新离职申请状态
          } else {
            alert("离职确认失败：" + (json.message || ""));
          }
        } catch (err) {
          alert("请求错误：" + err.message);
        }
      }
    },

    // 退出系统
    logout() {
      localStorage.clear();  // 清空本地存储
      this.$router.push("/login");  // 跳转到登录页面
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
    font-size: 20px;
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
