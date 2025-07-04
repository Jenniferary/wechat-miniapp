<template>
    <div class="resume-page">
      <div class="sidebar">
        <h2>📌 请假申请</h2>
        <ul>
          <li @click="$router.push('/hr-dashboard')">入职待审批列表</li>
          <li @click="$router.push('/hr-profile')">个人档案</li>
          <li @click="$router.push('/hr-employee')">员工档案</li>
          <li @click="$router.push('/hr-attendance')">考勤打卡</li>
          <li><strong>请假申请</strong></li>
          <li @click="$router.push('/hr-leave-progress')">我的请假记录</li>
          <li @click="$router.push('/hr-leave-review')">请假待审批</li>
          <li @click="$router.push('/hr-leaving-working')">离职申请</li>
          <li @click="$router.push('/hr-leavingworking-review')">离职待审批</li>
          <li @click="logout" class="logout">退出系统</li>
        </ul>
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
    name: "HrLeaveApply",
    data() {
      return {
        form: {
          startDate: '',
          endDate: '',
          reason: '',
        },
        hrInfo: null,
      };
    },
    created() {
      this.loadHrInfo();
    },
    methods: {
      async loadHrInfo() {
        const hrId = localStorage.getItem("hrId");
        if (!hrId) {
          alert("未登录");
          return this.$router.push("/login");
        }
        const res = await fetch(`/api/hr/${hrId}`);
        const json = await res.json();
        if (json.status === "success") {
          this.hrInfo = json.data;
        } else {
          alert("加载失败");
        }
      },
      async submitLeave() {
        if (!this.hrInfo) return;
        try {
          const payload = {
            employeeId: this.hrInfo.id,
            employeeType: 'hr',
            branchId: this.hrInfo.branchId,
            startDate: this.form.startDate,
            endDate: this.form.endDate,
            reason: this.form.reason
          };
          const res = await fetch("/api/leave/apply", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload),
          });
          const json = await res.json();
          if (json.status === "success") {
            alert("请假申请提交成功！");
            this.form = { startDate: '', endDate: '', reason: '' };
          } else {
            alert(json.message || "提交失败");
          }
        } catch (err) {
          alert("请求错误：" + err.message);
        }
      },
      logout() {
        localStorage.clear();
        this.$router.push("/login");
      }
    }
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
  }
  .sidebar h2 {
    margin-bottom: 30px;
    font-size: 22px;
    border-bottom: 2px solid white;
    padding-bottom: 10px;
  }
  .sidebar ul {
    list-style: none;
    padding: 0;
    margin: 0;
  }
  .sidebar li {
    padding: 10px 0;
    font-size: 15px;
    cursor: pointer;
  }
  .logout {
    color: #ffb3b3;
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
  textarea {
    resize: vertical;
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
    .sidebar {
      width: 100vw;
      text-align: center;
    }
    .form-section {
      width: 100vw;
      padding: 20px;
    }
  }
  </style>
  