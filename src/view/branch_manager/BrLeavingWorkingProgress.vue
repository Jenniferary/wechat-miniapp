<template>
  <div class="resume-page">
    <!-- 左侧菜单 -->
    <div class="sidebar">
      <h2>📌 离职申请审批进度</h2>
      <ul>
        <li @click="$router.push('/branch-leavingworking-review')">待审批离职申请</li>
        <li @click="$router.push('/branch-dashboard')">返回主页</li>
        <li @click="logout" class="logout">退出系统</li>
      </ul>
    </div>

    <!-- 右侧内容 -->
    <div class="form-section">
      <h3>离职申请审批进度</h3>

      <table v-if="progressData.length > 0">
        <thead>
          <tr>
            <th>申请ID</th>
            <th>员工姓名</th>
            <th>离职原因</th>
            <th>提交时间</th>
            <th>当前状态</th>
            <th>审批进度</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="req in progressData" :key="req.id">
            <td>{{ req.id }}</td>
            <td>{{ req.name }}</td>
            <td>{{ req.reason }}</td>
            <td>{{ formatDate(req.created_at) }}</td>
            <td>
              <span :class="['status', statusClass(req.status)]">
                {{ req.status }}
              </span>
            </td>
            <td>
              <div class="progress-bar-container">
                <div :style="getProgressBarStyle(req.progress)" class="progress-bar"></div>
                <span>{{ req.progress }}%</span>
              </div>
            </td>
          </tr>
        </tbody>
      </table>

      <p v-else>暂无审批进度数据</p>
    </div>
  </div>
</template>

<script>
export default {
  name: "BrLeavingWorkingProgress",
  data() {
    return {
      progressData: [],  // 离职申请的进度数据
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
        this.fetchProgressData(); // 获取审批进度数据
      } catch (err) {
        alert("加载店长信息失败：" + err.message);
      }
    },

    // 获取审批进度数据
    async fetchProgressData() {
      try {
        const managerId = this.managerInfo.id;  // 获取店长ID
        const url = `/api/leaving-working/progress-by-manager?managerId=${managerId}`;  // 根据店长ID查询进度
        const res = await fetch(url);
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        const json = await res.json();
        this.progressData = json;  // 更新审批进度数据
        console.log("Returned data:", json);
      } catch (err) {
        alert("加载审批进度失败：" + err.message);
      }
    },

formatDate(dateStr) {
  if (!dateStr) return "";
  const date = new Date(dateStr);  // 转换为日期对象
  if (isNaN(date)) {
    console.error("Invalid date:", dateStr);  // 如果日期无效，输出错误
    return "Invalid date";
  }
  return date.toLocaleDateString("zh-Hans-CN", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit"
  });  // 格式化为本地日期格式
},



    // 状态样式
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

    // 获取进度条的样式
    getProgressBarStyle(progress) {
      return {
        width: `${progress}%`,
        backgroundColor: progress === 100 ? "#27ae60" : "#f39c12",  // 完成时为绿色，进行中为橙色
      };
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
/* 样式保持一致 */
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

.progress-bar-container {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 200px;
  height: 10px;
  background-color: #e1e1e1;
  border-radius: 8px;
}

.progress-bar {
  height: 100%;
  border-radius: 8px;
}

</style>
