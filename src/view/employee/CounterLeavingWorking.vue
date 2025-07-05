<template>
  <div class="resume-page">
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
        <li :class="{ active: activeSection === 'leaving' }" @click="selectSection('leaving')"><strong>离职申请</strong></li>
        <li :class="{ active: activeSection === 'leaving-status' }" @click="selectSection('leaving-status')">查看离职申请进度</li>
        <li :class="{ active: activeSection === 'salary' }" @click="selectSection('salary')">工资管理</li>
        <li :class="{ active: activeSection === 'attendance' }" @click="selectSection('attendance')">考勤打卡</li>
        <li :class="{ active: activeSection === 'leave' }" @click="selectSection('leave')">请假申请</li>
        <li :class="{ active: activeSection === 'leave-progress' }" @click="selectSection('leave-progress')">我的请假记录</li>
      </ul>
      <div class="logout" @click="logout">退出系统</div>
    </div>

    <div class="form-section">
      <h3>提交离职申请</h3>
      <form @submit.prevent="submitLeaveRequest">
        <div class="form-row">
          <label>离职原因：</label>
          <textarea v-model="form.reason" required rows="4"></textarea>
        </div>
        <button type="submit">提交离职申请</button>
      </form>
    </div>
  </div>
</template>

<script>
export default {
  name: "CounterLeavingWorking",
  data() {
    return {
      form: { reason: "" },
      counterInfo: null,
      activeSection: "leaving",
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
      const counterId = localStorage.getItem("counterId");
      if (!counterId) {
        alert("未登录");
        this.$router.push("/login");
        return;
      }

      try {
        const res = await fetch(`/api/counter/${counterId}`);
        const json = await res.json();
        if (json.status === "success") {
          this.counterInfo = json.data;
        } else {
          alert(json.message || "加载员工信息失败");
        }
      } catch (err) {
        alert("请求错误：" + err.message);
      }
    },
    async submitLeaveRequest() {
      if (!this.counterInfo) {
        alert("前台员工信息未加载");
        return;
      }

      try {
        const payload = {
          employeeId: this.counterInfo.id,
          employeeType: "counter",
          branchId: this.counterInfo.branchId,
          reason: this.form.reason,
          name: this.counterInfo.name,
        };

        const res = await fetch("/api/leaving-working/counter-apply", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(payload),
        });

        const json = await res.json();
        if (json.status === "success") {
          alert("离职申请提交成功！");
          this.form.reason = "";
        } else {
          alert(json.message || "提交失败");
        }
      } catch (err) {
        alert("请求错误：" + err.message);
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

h3 {
  font-size: 24px;
  color: #333;
  margin-bottom: 30px;
  border-bottom: 2px solid #007bff;
  padding-bottom: 10px;
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

.form-row {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
}

.form-row label {
  width: 100px;
  font-weight: 600;
  color: #555;
}

.form-row textarea {
  flex: 1;
  padding: 8px 12px;
  font-size: 16px;
  border: 1px solid #ccc;
  border-radius: 4px;
}
</style>
