<template>
  <div class="overtime-apply-page">
    <div class="sidebar">
      <h2>📌 加班申请</h2>
      <ul>
        <li @click="$router.push('/waiter-dashboard')">个人档案</li>
          <li @click="$router.push('/waiter-attendance')">考勤打卡</li>
          <li @click="$router.push('/waiter-leave')">请假申请</li>
          <li @click="$router.push('/waiter-leave-progress')">我的请假记录</li>
          <li @click="$router.push('/waiter-leave-working')">离职申请</li>
          <li @click="$router.push('/waiter-leaving-status')">查看离职申请进度</li>
          <li @click="$router.push('/waiter-overtime-working')">加班申请</li>
          <li @click="$router.push('/waiter-overtime-progress')">我的加班记录</li>
          <li @click="$router.push('/waiter-salary')">工资管理</li>
          <li @click="logout" class="logout">退出系统</li>
      </ul>
    </div>

    <div class="form-section">
      <h3>提交加班申请</h3>
      <form @submit.prevent="submitOvertime">
        <div class="form-row">
          <label>加班日期：</label>
          <input type="date" v-model="form.date" :min="minDate" :max="maxDate" @change="validateWeekend" required />
        </div>
        <div class="form-row">
          <label>加班原因：</label>
          <textarea v-model="form.reason" required rows="4"></textarea>
        </div>
        <button type="submit">提交申请</button>
      </form>
    </div>
  </div>
</template>

<script>
export default {
  name: "WaiterOvertimeApply",
  data() {
    return {
      form: {
        date: "",
        reason: "",
      },
      waiterInfo: null,
      minDate: new Date().toISOString().split("T")[0],
      maxDate: new Date(2025, 11, 31).toISOString().split("T")[0],
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
          this.waiterInfo = json.data;
        } else {
          alert(json.message || "加载失败");
        }
      } catch (err) {
        alert("请求错误：" + err.message);
      }
    },
    validateWeekend() {
      const selectedDate = this.form.date;
      const date = new Date(selectedDate);
      const day = date.getDay(); 

      if (day !== 0 && day !== 6) {
        alert("只能选择周六或周日！");
        this.form.date = ""; // 清除日期
      }
    },
    async submitOvertime() {
      if (!this.waiterInfo) {
        alert("用户信息未加载");
        return;
      }

      if (!this.form.date) {
        alert("请选择一个有效的加班日期！");
        return;
      }

      try {
        const payload = {
          employeeId: this.waiterInfo.id,
          employeeType: "waiter",
          branchId: this.waiterInfo.branchId,
          date: this.form.date,
          reason: this.form.reason,
        };
        const res = await fetch("/api/overtime/apply", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(payload),
        });

        const json = await res.json();
        if (json.status === "success") {
          alert("加班申请提交成功！");
          this.form = { date: "", reason: "" };
        } else {
          alert(json.message || "提交失败");
        }
      } catch (err) {
        alert("请求错误：" + err.message);
      }
    },
    logout() {
      localStorage.removeItem("waiterId");
      this.$router.push("/login");
    },
  },
};
</script>

<style scoped>
.overtime-apply-page {
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
  .overtime-apply-page {
    flex-direction: column;
  }

  .sidebar {
    width: 100%;
    text-align: center;
  }

  .form-section {
    width: 100%;
    padding: 20px;
  }
}
</style>
