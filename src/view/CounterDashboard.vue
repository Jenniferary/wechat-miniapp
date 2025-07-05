<template>
    <div class="resume-page">
      <div class="sidebar">
        <h2>💁‍♀️ 前台管理系统</h2>
        <ul class="menu-list">
          <li
            :class="{ active: activeSection === 'profile' }"
            @click="selectSection('profile')"
          >
            <strong>个人档案</strong>
          </li>
          <li @click="$router.push('/counter-dinein-order')">管理堂食订单</li>
          <li @click="$router.push('/manage-tables')">管理餐桌</li>
          
          <li>
            <strong
              @click="toggleSection('delivery')"
              :class="{ active: activeSection === 'delivery' }"
              style="margin-top: 20px; cursor: pointer; color: #fff; font-weight: bold;"
            >
              外卖管理
            </strong>
          </li>
  
          <li
            v-if="activeSection === 'delivery'"
            :class="{ active: activeSubsection === 'assign' }"
            @click="selectSubsection('assign')"
            style="padding-left: 15px; cursor: pointer;"
          >
            分配外卖员
          </li>
          <li
            v-if="activeSection === 'delivery'"
            :class="{ active: activeSubsection === 'add' }"
            @click="selectSubsection('add')"
            style="padding-left: 15px; cursor: pointer;"
          >
            添加外卖员
          </li>
          <li
            v-if="activeSection === 'delivery'"
            :class="{ active: activeSubsection === 'view' }"
            @click="selectSubsection('view')"
            style="padding-left: 15px; cursor: pointer;"
          >
            查看外卖订单
          </li>
          <li @click="$router.push('/counter-overtime-working')">申请加班</li>
          <li @click="$router.push('/counter-overtime-progress')">我的加班记录</li>
          <li @click="$router.push('/counter-leaving-working')">申请离职</li>
          <li @click="$router.push('/counter-leaving-status')">查看离职申请进度</li>
          <li @click="$router.push('/counter-salary')">工资管理</li>
          <li @click="$router.push('/counter-attendance')">考勤打卡</li>
          <li @click="$router.push('/counter-leave')">请假申请</li>
          <li @click="$router.push('/counter-leave-progress')">我的请假记录</li>
        </ul>
  
        <div class="logout" @click="logout">退出系统</div>
      </div>
  
      <div class="form-section">
        <h3>我的信息</h3>
  
        <form @submit.prevent="saveProfile" v-if="counterInfo">
          <div class="form-row">
            <label>用户名：</label>
            <input type="text" v-model="counterInfo.username" disabled />
          </div>
  
          <div class="form-row">
            <label>姓名：</label>
            <input type="text" v-model="counterInfo.name" disabled />
          </div>
  
          <div class="form-row">
            <label>邮箱：</label>
            <input type="email" v-model="counterInfo.email" />
          </div>
  
          <div class="form-row">
            <label>电话：</label>
            <input type="tel" v-model="counterInfo.phone" />
          </div>
  
          <div class="form-row">
            <label>门店ID：</label>
            <input type="number" v-model="counterInfo.branchId" disabled />
          </div>
  
          <div class="form-row" v-if="counterInfo.hireDate">
            <label>入职日期：</label>
            <input type="text" :value="formatDateDisplay(counterInfo.hireDate)" disabled />
          </div>
  
          <button type="submit">保存修改</button>
        </form>
  
        <p v-else>加载中...</p>
      </div>
    </div>
  </template>
  
  <script>
  export default {
    name: "CounterProfile",
    data() {
      return {
        counterInfo: null,
        activeSection: "profile",
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
        if (path.startsWith('/counter-dashboard')) {
          this.activeSection = 'profile';
          this.activeSubsection = null;
        } else if (path.startsWith('/counter-dinein-order')) {
          this.activeSection = 'dinein';
          this.activeSubsection = null;
        } else if (path.startsWith('/manage-tables')) {
          this.activeSection = 'tables';
          this.activeSubsection = null;
        } else if (path.startsWith('/delivery-')) {
          this.activeSection = 'delivery';
          if (path.includes('assign')) this.activeSubsection = 'assign';
          else if (path.includes('add')) this.activeSubsection = 'add';
          else if (path.includes('view')) this.activeSubsection = 'view';
          else this.activeSubsection = null;
        } else if (path.startsWith('/counter-attendance')) {
          this.activeSection = 'attendance';
          this.activeSubsection = null;
        } else if (path.startsWith('/counter-leave-progress')) {
          this.activeSection = 'leave-progress';
          this.activeSubsection = null;
        } else if (path.startsWith('/counter-leave')) {
          this.activeSection = 'leave';
          this.activeSubsection = null;
        } else {
          this.activeSection = null;
          this.activeSubsection = null;
        }
      },
  
      selectSection(section) {
        this.activeSection = section;
        this.activeSubsection = null;
  
        if (section === 'profile') {
          this.$router.push('/counter-dashboard');
        } else if (section === 'dinein') {
          this.$router.push('/counter-dinein-order');
        } else if (section === 'tables') {
          this.$router.push('/manage-tables');
        } else if (section === 'attendance') {
          this.$router.push('/counter-attendance');
        } else if (section === 'leave') {
          this.$router.push('/counter-leave');
        } else if (section === 'leave-progress') {
          this.$router.push('/counter-leave-progress');
        }
      },
  
      toggleSection(section) {
        if (this.activeSection === section) {
          this.activeSection = null;
          this.activeSubsection = null;
          this.$router.push('/counter-dashboard'); // 关闭外卖管理跳回个人档案页
        } else {
          this.activeSection = section;
          this.activeSubsection = null;
          this.$router.push('/delivery-assign'); // 打开默认跳分配外卖员
        }
      },
  
      selectSubsection(subsection) {
        this.activeSubsection = subsection;
        if (subsection === 'assign') {
          this.$router.push('/delivery-assign');
        } else if (subsection === 'add') {
          this.$router.push('/delivery-add');
        } else if (subsection === 'view') {
          this.$router.push('/delivery-view');
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
            alert(json.message || "加载失败");
          }
        } catch (error) {
          alert("请求错误：" + error.message);
        }
      },
  
      async saveProfile() {
        try {
          const res = await fetch(`/api/counter/${this.counterInfo.id}`, {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(this.counterInfo),
          });
          const json = await res.json();
          if (json.status === "success") {
            alert("保存成功");
          } else {
            alert(json.message || "保存失败");
          }
        } catch (error) {
          alert("请求错误：" + error.message);
        }
      },
  
      logout() {
        localStorage.removeItem("counterId");
        this.$router.push("/login");
      },
  
      formatDateDisplay(dateStr) {
        const d = new Date(dateStr);
        if (!d || isNaN(d.getTime())) return "";
        return d.toLocaleDateString();
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
    box-sizing: border-box;
    display: flex;
    flex-direction: column;
    height: 100vh;
  }
  .sidebar h2 {
    margin-bottom: 30px;
    font-size: 22px;
    border-bottom: 2px solid #fff;
    padding-bottom: 10px;
  }
  .menu-list {
    flex: 1;
    list-style: none;
    padding-left: 0;
    margin: 0;
    overflow-y: auto;
  }
  .menu-list li {
    padding: 10px 0;
    font-size: 15px;
    cursor: pointer;
    color: #ccc;
    user-select: none;
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
    cursor: pointer;
    margin-top: 20px;
    user-select: none;
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
  .form-row input {
    flex: 1;
    padding: 6px 10px;
    font-size: 16px;
    border: 1px solid #ddd;
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
  @media (max-width: 768px) {
    .resume-page {
      flex-direction: column;
    }
    .form-section {
      width: 100vw;
      padding: 30px 20px;
    }
    .sidebar {
      width: 100vw;
      text-align: center;
      height: auto;
      padding-bottom: 20px;
    }
    .menu-list li {
      display: inline-block;
      padding: 10px 15px;
    }
  }
  </style>
  