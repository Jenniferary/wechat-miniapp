<template>
  <div class="status-page">
    <div class="sidebar">
      <h2>💁‍♀️ 前台管理系统</h2>
      <ul class="menu-list">
        <li :class="{ active: activeSection === 'profile' }" @click="selectSection('profile')"><strong>个人档案</strong></li>
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
        <li @click="selectSection('leaving')">申请离职</li>
        <li @click="selectSection('leaving-status')">查看离职申请进度</li>
        <li @click="selectSection('salary')">工资管理</li>
        <li @click="selectSection('attendance')">考勤打卡</li>
        <li @click="selectSection('leave')">请假申请</li>
        <li @click="selectSection('leave-progress')">我的请假记录</li>
      </ul>
      <div class="logout" @click="logout">退出系统</div>
    </div>

    <div class="form-section">
      <h3>我的加班记录</h3>
      <table v-if="overtimeRequests.length > 0">
        <thead>
          <tr>
            <th>加班日期</th>
            <th>加班原因</th>
            <th>当前状态</th>
            <th>提交时间</th>
            <th>更新时间</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="req in overtimeRequests" :key="req.request_id">
            <td>{{ formatDate(req.date) }}</td>
            <td>{{ req.reason }}</td>
            <td><span :class="['status', statusClass(req.status)]">{{ req.status }}</span></td>
            <td>{{ formatDate(req.createdAt) }}</td>
            <td>{{ formatDate(req.updatedAt) }}</td>
          </tr>
        </tbody>
      </table>
      <p v-else>没有找到您的加班记录。</p>
    </div>
  </div>
</template>

<script>
export default {
  name: "CounterOvertimeProgress",
  data() {
    return {
      counterInfo: null,
      overtimeRequests: [],
      activeSection: "overtime-progress",
      activeSubsection: null,
    };
  },
  created() {
    this.syncActiveByRoute(this.$route.path);
    this.loadCounterInfo();
  },
  watch: {
    '$route.path'(newPath) {
      this.syncActiveByRoute(newPath);
    },
  },
  methods: {
    syncActiveByRoute(path) {
      if (path.includes('overtime-working')) {
        this.activeSection = 'overtime';
      } else if (path.includes('overtime-progress')) {
        this.activeSection = 'overtime-progress';
      } else if (path.includes('dashboard')) {
        this.activeSection = 'profile';
      } else if (path.includes('attendance')) {
        this.activeSection = 'attendance';
      } else if (path.includes('leave-progress')) {
        this.activeSection = 'leave-progress';
      } else if (path.includes('leave')) {
        this.activeSection = 'leave';
      } else if (path.includes('leaving-status')) {
        this.activeSection = 'leaving-status';
      } else if (path.includes('leaving-working')) {
        this.activeSection = 'leaving';
      } else if (path.includes('salary')) {
        this.activeSection = 'salary';
      } else if (path.startsWith('/delivery-')) {
        this.activeSection = 'delivery';
        if (path.includes('assign')) this.activeSubsection = 'assign';
        else if (path.includes('add')) this.activeSubsection = 'add';
        else if (path.includes('view')) this.activeSubsection = 'view';
      } else {
        this.activeSection = null;
        this.activeSubsection = null;
      }
    },
    selectSection(section) {
      this.activeSection = section;
      this.activeSubsection = null;
      const routes = {
        profile: "/counter-dashboard",
        dinein: "/counter-dinein-order",
        tables: "/manage-tables",
        delivery: "/delivery-assign",
        overtime: "/counter-overtime-working",
        'overtime-progress': "/counter-overtime-progress",
        leaving: "/counter-leaving-working",
        'leaving-status': "/counter-leaving-status",
        salary: "/counter-salary",
        attendance: "/counter-attendance",
        leave: "/counter-leave",
        'leave-progress': "/counter-leave-progress",
      };
      if (routes[section]) {
        this.$router.push(routes[section]);
      }
    },
    toggleSection(section) {
      if (this.activeSection === section) {
        this.activeSection = null;
        this.activeSubsection = null;
      } else {
        this.activeSection = section;
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
      if (subRoutes[subsection]) {
        this.$router.push(subRoutes[subsection]);
      }
    },
    async loadCounterInfo() {
      try {
        const counterId = localStorage.getItem("counterId");
        if (!counterId) {
          alert("未登录");
          this.$router.push("/login");
          return;
        }
        const res = await fetch(`/api/counter/${counterId}`);
        const json = await res.json();
        if (json.status === "success") {
          this.counterInfo = json.data;
          this.fetchOvertimeRequests();
        } else {
          alert(json.message || "加载失败");
        }
      } catch (err) {
        alert("请求错误：" + err.message);
      }
    },
    async fetchOvertimeRequests() {
      try {
        const res = await fetch(`/api/overtime/by-employee?employee_id=${this.counterInfo.id}&employee_type=counter`);
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
    formatDate(dateStr) {
      return dateStr ? new Date(dateStr).toLocaleDateString() : "无效日期";
    },
    statusClass(status) {
      switch (status) {
        case "待HR审批": return "pending";
        case "HR审批通过待店长审批": return "in-review";
        case "审批成功": return "approved";
        case "已驳回": return "rejected";
        default: return "unknown";
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
/* 样式保持不变，统一与其他页面风格一致 */
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
  height: 100vh;
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
  overflow-y: auto;
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
.status.pending { background-color: #f39c12; }
.status.in-review { background-color: #2980b9; }
.status.approved { background-color: #27ae60; }
.status.rejected { background-color: #c0392b; }
.status.unknown { background-color: #7f8c8d; }
</style>
