<template>
  <div class="status-page">
    <div class="sidebar">
      <h2>💁‍♀️ 前台管理系统</h2>
      <ul class="menu-list">
        <li :class="{ active: activeSection === 'profile' }" @click="selectSection('profile')">个人档案</li>
        <li @click="selectSection('dinein')">管理堂食订单</li>
        <li @click="selectSection('tables')">管理餐桌</li>

        <li>
          <strong
            @click="toggleSection('delivery')"
            :class="{ active: activeSection === 'delivery' }"
            style="margin-top: 20px; cursor: pointer; color: #fff; font-weight: bold;"
          >外卖管理</strong>
        </li>
        <li v-if="activeSection === 'delivery'" :class="{ active: activeSubsection === 'assign' }" @click="selectSubsection('assign')" style="padding-left: 15px;">分配外卖员</li>
        <li v-if="activeSection === 'delivery'" :class="{ active: activeSubsection === 'add' }" @click="selectSubsection('add')" style="padding-left: 15px;">添加外卖员</li>
        <li v-if="activeSection === 'delivery'" :class="{ active: activeSubsection === 'view' }" @click="selectSubsection('view')" style="padding-left: 15px;">查看外卖订单</li>

        <li :class="{ active: activeSection === 'overtime' }" @click="selectSection('overtime')">申请加班</li>
        <li :class="{ active: activeSection === 'overtime-progress' }" @click="selectSection('overtime-progress')">我的加班记录</li>
        <li :class="{ active: activeSection === 'leaving' }" @click="selectSection('leaving')">离职申请</li>
        <li :class="{ active: activeSection === 'leaving-status' }" @click="selectSection('leaving-status')"><strong>查看离职进度</strong></li>
        <li :class="{ active: activeSection === 'salary' }" @click="selectSection('salary')">工资管理</li>
        <li :class="{ active: activeSection === 'attendance' }" @click="selectSection('attendance')">考勤打卡</li>
        <li :class="{ active: activeSection === 'leave' }" @click="selectSection('leave')">请假申请</li>
        <li :class="{ active: activeSection === 'leave-progress' }" @click="selectSection('leave-progress')">我的请假记录</li>
      </ul>
      <div class="logout" @click="logout">退出系统</div>
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
              <button v-if="request.status === '审批成功'" @click="confirmLeave(request.employeeId)" class="btn-approve">
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
  name: "CounterLeavingWorkStatus",
  data() {
    return {
      leaveRequests: [],
      activeSection: "leaving-status",
      activeSubsection: null,
    };
  },
  created() {
    this.syncActiveByRoute(this.$route.path);
    this.loadLeaveRequests();
  },
  watch: {
    '$route.path'(newPath) {
      this.syncActiveByRoute(newPath);
    },
  },
  methods: {
    syncActiveByRoute(path) {
      if (path.includes('dashboard')) this.activeSection = 'profile';
      else if (path.includes('dinein')) this.activeSection = 'dinein';
      else if (path.includes('manage-tables')) this.activeSection = 'tables';
      else if (path.includes('overtime-working')) this.activeSection = 'overtime';
      else if (path.includes('overtime-progress')) this.activeSection = 'overtime-progress';
      else if (path.includes('leave-progress')) this.activeSection = 'leave-progress';
      else if (path.includes('leave')) this.activeSection = 'leave';
      else if (path.includes('attendance')) this.activeSection = 'attendance';
      else if (path.includes('salary')) this.activeSection = 'salary';
      else if (path.includes('leaving-working')) this.activeSection = 'leaving';
      else if (path.includes('leaving-status')) this.activeSection = 'leaving-status';
      else if (path.startsWith('/delivery-')) {
        this.activeSection = 'delivery';
        if (path.includes('assign')) this.activeSubsection = 'assign';
        else if (path.includes('add')) this.activeSubsection = 'add';
        else if (path.includes('view')) this.activeSubsection = 'view';
      }
    },
    selectSection(section) {
      this.activeSection = section;
      this.activeSubsection = null;
      const routes = {
        profile: "/counter-dashboard",
        dinein: "/counter-dinein-order",
        tables: "/manage-tables",
        overtime: "/counter-overtime-working",
        'overtime-progress': "/counter-overtime-progress",
        leaving: "/counter-leaving-working",
        'leaving-status': "/counter-leaving-status",
        salary: "/counter-salary",
        attendance: "/counter-attendance",
        leave: "/counter-leave",
        'leave-progress': "/counter-leave-progress",
      };
      if (routes[section]) this.$router.push(routes[section]);
    },
    toggleSection(section) {
      this.activeSection = this.activeSection === section ? null : section;
      if (this.activeSection === section) {
        this.activeSubsection = "assign";
        this.$router.push("/delivery-assign");
      }
    },
    selectSubsection(subsection) {
      this.activeSubsection = subsection;
      const subRoutes = {
        assign: "/delivery-assign",
        add: "/delivery-add",
        view: "/delivery-view",
      };
      if (subRoutes[subsection]) this.$router.push(subRoutes[subsection]);
    },
    async loadLeaveRequests() {
      try {
        const counterId = localStorage.getItem("counterId");
        if (!counterId) {
          alert("未登录");
          this.$router.push("/login");
          return;
        }

        const res = await fetch(`/api/leaving-working/counter-by-applicant?applicantId=${counterId}`);
        const data = await res.json();

        if (data.status === "success" && data.data.length > 0) {
          this.leaveRequests = data.data;
        } else {
          this.leaveRequests = [];
        }
      } catch (err) {
        alert("加载离职申请状态失败：" + err.message);
      }
    },
    formatDate(dateStr) {
      const date = new Date(dateStr);
      return date.toLocaleDateString("zh-CN", {
        year: "numeric",
        month: "2-digit",
        day: "2-digit",
      });
    },
    statusClass(status) {
      switch (status) {
        case "已提交待HR审批": return "pending";
        case "HR审批通过待店长审批": return "in-review";
        case "审批成功": return "approved";
        case "已驳回": return "rejected";
        case "已离职": return "resigned";
        default: return "unknown";
      }
    },
    async confirmLeave(counterId) {
      const counterIdParsed = parseInt(localStorage.getItem("counterId"), 10);
      if (!counterIdParsed) {
        alert("未登录或无效的员工ID");
        this.$router.push("/login");
        return;
      }

      if (confirm("确认离职该员工吗？")) {
        try {
          const res = await fetch(`/api/leaving-working/counters/delete-by-id/${counterId}`, {
            method: "DELETE",
          });
          const json = await res.json();
          if (json.status === "success") {
            alert("员工离职确认成功");
            setTimeout(() => {
              this.logout();
            }, 5000);
            this.loadLeaveRequests();
          } else {
            alert("离职确认失败：" + (json.message || ""));
          }
        } catch (err) {
          alert("删除操作失败：" + err.message);
        }
      }
    },
    logout() {
      localStorage.removeItem("counterId");
      this.$router.push("/login");
    },
  },
};
</script>

<style scoped>
/* 复用样式统一风格 */
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
.menu-list {
  flex: 1;
  list-style: none;
  padding: 0;
  margin: 0;
}
.menu-list li {
  padding: 10px 0;
  font-size: 15px;
  cursor: pointer;
  color: #ccc;
}
.menu-list li.active {
  color: #00b4d8;
  font-weight: bold;
}
.menu-list strong.active {
  color: #00b4d8;
}
.logout {
  color: #ffb3b3;
  margin-top: 20px;
  cursor: pointer;
}
.logout:hover {
  color: #fff;
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
  background-color: #c0392b;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
}
button:hover {
  background-color: #e74c3c;
}
</style>
