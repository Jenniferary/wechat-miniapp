<template>
    <div class="resume-page">
      <div class="sidebar">
        <h2>💁‍♀️ 前台管理系统</h2>
        <ul class="menu-list">
          <li :class="{ active: activeSection === 'profile' }" @click="selectSection('profile', '/counter-dashboard')">个人档案</li>
          <li :class="{ active: activeSection === 'dinein' }" @click="selectSection('dinein', '/counter-dinein-order')">管理堂食订单</li>
          <li :class="{ active: activeSection === 'tables' }" @click="selectSection('tables', '/manage-tables')">管理餐桌</li>
          <li><strong @click="toggleSection('delivery')" :class="{ active: activeSection === 'delivery' }">外卖管理</strong></li>
  
          <li v-if="activeSection === 'delivery'" :class="{ active: activeSubsection === 'assign' }" @click="selectSubsection('assign', '/delivery-assign')">分配外卖员</li>
          <li v-if="activeSection === 'delivery'" :class="{ active: activeSubsection === 'add' }" @click="selectSubsection('add', '/delivery-add')">添加外卖员</li>
          <li v-if="activeSection === 'delivery'" :class="{ active: activeSubsection === 'view' }" @click="selectSubsection('view', '/delivery-view')">查看外卖订单</li>
  
          <li :class="{ active: activeSection === 'attendance' }" @click="selectSection('attendance', '/counter-attendance')">考勤打卡</li>
          <li :class="{ active: activeSection === 'leave' }" @click="selectSection('leave', '/counter-leave')">请假申请</li>
          <li :class="{ active: activeSection === 'leaveProgress' }" @click="selectSection('leaveProgress', '/counter-leave-progress')">我的请假记录</li>
        </ul>
        <div class="logout" @click="logout">退出系统</div>
      </div>
  
      <div class="form-section">
        <h3>提交请假申请</h3>
        <form @submit.prevent="submitLeave">
          <div class="form-row">
            <label>开始日期：</label>
            <input type="date" v-model="form.startDate" required />
          </div>
          <div class="form-row">
            <label>结束日期：</label>
            <input type="date" v-model="form.endDate" required />
          </div>
          <div class="form-row">
            <label>请假原因：</label>
            <textarea v-model="form.reason" required rows="4"></textarea>
          </div>
          <button type="submit">提交申请</button>
        </form>
      </div>
    </div>
  </template>
  
  <script>
  export default {
    name: "CounterLeave",
    data() {
      return {
        activeSection: 'leave',
        activeSubsection: '',
        form: {
          startDate: "",
          endDate: "",
          reason: "",
        },
        counterInfo: null,
      };
    },
    created() {
      this.loadCounterInfo();
    },
    methods: {
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
            alert(json.message || "加载失败");
          }
        } catch (err) {
          alert("请求错误：" + err.message);
        }
      },
      async submitLeave() {
        if (!this.counterInfo) {
          alert("用户信息未加载");
          return;
        }
        if (this.form.endDate < this.form.startDate) {
          alert("结束日期不能早于开始日期");
          return;
        }
        try {
          const payload = {
            employeeId: this.counterInfo.id,
            employeeType: "counter",
            branchId: this.counterInfo.branchId,
            startDate: this.form.startDate,
            endDate: this.form.endDate,
            reason: this.form.reason,
          };
          const res = await fetch("/api/leave/apply", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload),
          });
          const json = await res.json();
          if (json.status === "success") {
            alert("请假申请提交成功！");
            this.form = { startDate: "", endDate: "", reason: "" };
          } else {
            alert(json.message || "提交失败");
          }
        } catch (err) {
          alert("请求错误：" + err.message);
        }
      },
      selectSection(section, path) {
        this.activeSection = section;
        this.activeSubsection = '';
        this.$router.push(path);
      },
      toggleSection(section) {
        this.activeSection = this.activeSection === section ? '' : section;
      },
      selectSubsection(subsection, path) {
        this.activeSubsection = subsection;
        this.activeSection = 'delivery';
        this.$router.push(path);
      },
      logout() {
        localStorage.removeItem("counterId");
        this.$router.push("/login");
      },
    },
  };
  </script>
  
  <style scoped>
  /* 可复用你已有样式 */
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
    box-sizing: border-box;
  }
  .sidebar h2 {
    font-size: 22px;
    border-bottom: 2px solid white;
    padding-bottom: 10px;
    margin-bottom: 20px;
  }
  .menu-list {
    list-style: none;
    padding: 0;
  }
  .menu-list li {
    padding: 10px 0;
    cursor: pointer;
  }
  .menu-list li.active {
    color: #00b4d8;
    font-weight: bold;
  }
  .logout {
    color: #ffb3b3;
    margin-top: 20px;
    cursor: pointer;
  }
  .logout:hover {
    color: white;
  }
  .form-section {
    flex: 1;
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
  .form-row input,
  .form-row textarea {
    flex: 1;
    padding: 8px 12px;
    font-size: 16px;
    border: 1px solid #ccc;
    border-radius: 4px;
  }
  button {
    background-color: #007bff;
    border: none;
    color: white;
    padding: 12px 24px;
    font-size: 16px;
    border-radius: 6px;
    cursor: pointer;
  }
  button:hover {
    background-color: #0056b3;
  }
  </style>
  