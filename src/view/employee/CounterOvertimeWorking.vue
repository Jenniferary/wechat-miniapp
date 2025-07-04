<template>
  <div class="resume-page">
    <div class="sidebar">
      <h2>💼 加班申请</h2>
      <ul>
        <li @click="$router.push('/counter-dashboard')">个人档案</li>
        <li @click="$router.push('/counter-attendance')">考勤打卡</li>
        <li><strong>加班申请</strong></li>
        <li @click="$router.push('/counter-overtime-progress')">我的加班记录</li>
        <li @click="logout" class="logout">退出系统</li>
      </ul>
    </div>

    <div class="form-section">
      <h3>提交加班申请</h3>
      <form @submit.prevent="submitOvertime">
        <div class="form-row">
          <label>选择加班日期：</label>
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
  name: "CounterOvertimeWorking",
  data() {
    return {
      form: {
        date: "",  // 单个加班日期
        reason: "", // 加班原因
      },
      counterInfo: null,
      minDate: new Date().toISOString().split("T")[0], // 设置最小日期为今天
      maxDate: new Date(2025, 11, 31).toISOString().split("T")[0], // 设置最大日期为2025年12月31日
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
    // 校验选择的日期是否为周六或周日
    validateWeekend() {
      const selectedDate = this.form.date;
      const date = new Date(selectedDate);
      const day = date.getDay(); // 获取星期几，0为周日，6为周六

      if (day !== 0 && day !== 6) {
        alert("只能选择周六或周日！");
        this.form.date = ""; // 清除日期
      }
    },
    async submitOvertime() {
      if (!this.counterInfo) {
        alert("用户信息未加载");
        return;
      }
      // 校验加班日期是否有效
      if (!this.form.date) {
        alert("请选择一个有效的加班日期！");
        return;
      }

      try {
        const payload = {
          employeeId: this.counterInfo.id,
          employeeType: "counter",
          branchId: this.counterInfo.branchId,
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
          this.form = { date: "", reason: "" }; // 清空表单
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
/* 与原先的样式基本相同，适用于加班申请页面 */
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
  border-bottom: 2px solid white;
  padding-bottom: 10px;
}
.sidebar ul {
  list-style: none;
  padding: 0;
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
