<template>
  <div class="status-page">
    <div class="sidebar">
      <h2>👨‍🍳 我的加班记录</h2>
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
      <h3>我的加班记录</h3>

      <div v-if="overtimeRequests.length === 0" class="no-records">
        <p>您还没有提交任何加班申请记录。</p>
      </div>

      <div v-else>
        <table>
          <thead>
            <tr>
              <th>加班日期</th>
              <th>加班原因</th>
              <th>当前状态</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="request in overtimeRequests" :key="request.id">
              <td>{{ request.date }}</td>
              <td>{{ request.reason }}</td>
              <td>
                <span :class="['status', statusClass(request.status)]">
                  {{ request.status }}
                </span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "ChefOvertimeProgress",
  data() {
    return {
      overtimeRequests: [], // 存储加班申请记录
      chefInfo: null,
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
        const res = await fetch(`/api/chef/${chefId}`);
        const json = await res.json();
        if (json.status === "success") {
          this.chefInfo = json.data;
          this.loadOvertimeRequests();
        } else {
          alert(json.message || "加载失败");
        }
      } catch (err) {
        alert("请求错误：" + err.message);
      }
    },

    async loadOvertimeRequests() {
      if (!this.chefInfo) return;
      try {
        const res = await fetch(`/api/overtime/by-employee?employee_id=${this.chefInfo.id}&employee_type=chef`);
        const json = await res.json();

        if (json.status === "success") {
          this.overtimeRequests = json.data.records;
        } else {
          alert(json.message || "加载加班记录失败");
        }
      } catch (err) {
        alert("请求错误：" + err.message);
      }
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
      localStorage.removeItem("chefId");
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
