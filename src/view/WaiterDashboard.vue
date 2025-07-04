<template>
    <div class="resume-page">
      <div class="sidebar">
        <h2>👨‍🍳 个人档案</h2>
        <ul>
          <li><strong>个人档案</strong></li>
          <li @click="$router.push('/waiter-attendance')">考勤打卡</li>
          <li @click="$router.push('/waiter-leave')">请假申请</li>
          <li @click="$router.push('/waiter-leave-progress')">我的请假记录</li>
          <li @click="$router.push('/waiter-leave-working')">离职申请</li>
          <li @click="$router.push('/waiter-overtime-working')">加班申请</li>
          <li @click="$router.push('/waiter-salary')">工资管理</li>
          <li @click="logout" class="logout">退出系统</li>
        </ul>
      </div>
  
      <div class="form-section">
        <h3>我的信息</h3>
  
        <form @submit.prevent="saveProfile" v-if="waitersInfo">
          <div class="form-row">
            <label>用户名：</label>
            <input type="text" v-model="waitersInfo.username" disabled />
          </div>
  
          <div class="form-row">
            <label>姓名：</label>
            <input type="text" v-model="waitersInfo.name" disabled />
          </div>
  
          <div class="form-row">
            <label>邮箱：</label>
            <input type="email" v-model="waitersInfo.email" />
          </div>
  
          <div class="form-row">
            <label>电话：</label>
            <input type="tel" v-model="waitersInfo.phone" />
          </div>
  
          <div class="form-row">
            <label>门店ID：</label>
            <input type="number" v-model="waitersInfo.branchId" disabled />
          </div>
  
          <div class="form-row" v-if="waitersInfo.hireDate">
            <label>入职日期：</label>
            <input type="text" :value="formatDateDisplay(waitersInfo.hireDate)" disabled />
          </div>
  
          <button type="submit">保存修改</button>
        </form>
  
        <p v-else>加载中...</p>
      </div>
    </div>
  </template>
  
  <script>
  export default {
    name: "WaiterProfile",
    data() {
      return {
        waitersInfo: null,
      };
    },
    created() {
      this.loadWaiterInfo();
    },
    methods: {
      async loadWaiterInfo() {
        const waiterId = localStorage.getItem("waiterId");
        if (!waiterId) {
          alert("未登录");
          this.$router.push("/login");
          return;
        }
        try {
          const res = await fetch(`/api/waiters/${waiterId}`);
          const json = await res.json();
          if (json.status === "success") {
            this.waitersInfo = json.data;
          } else {
            alert(json.message || "加载失败");
          }
        } catch (error) {
          alert("请求错误：" + error.message);
        }
      },
      async saveProfile() {
        try {
          const res = await fetch(`/api/waiters/${this.waitersInfo.id}`, {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(this.waitersInfo),
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
        localStorage.removeItem("waiterId");
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
    width: calc(100vw - 220px);
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
    }
    .sidebar li {
      display: inline-block;
      padding: 10px 15px;
    }
  }
  </style>
  