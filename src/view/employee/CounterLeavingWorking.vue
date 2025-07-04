<template>
  <div class="resume-page">
    <div class="sidebar">
      <h2>📌 离职申请</h2>
      <ul>
        <li @click="$router.push('/counter-dashboard')">个人档案</li>
        <li @click="$router.push('/counter-leaving-status')">查看离职申请进度</li>
        <li><strong>离职申请</strong></li>
        <li @click="logout" class="logout">退出系统</li>
      </ul>
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
      form: {
        reason: "",
      },
      counterInfo: null, // 用于存储前台员工信息
    };
  },
  created() {
    this.loadCounterInfo();
  },
  methods: {
    async loadCounterInfo() {
      const counterId = localStorage.getItem("counterId");  // 使用 counterId 来获取前台员工信息
      if (!counterId) {
        alert("未登录");
        this.$router.push("/login");
        return;
      }

      try {
        const res = await fetch(`/api/counter/${counterId}`);  // 使用 counterId 请求前台员工信息
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
          employeeId: this.counterInfo.id,  // 使用 employeeInfo.id 提交离职申请
          employeeType: "counter",  // 假设员工类型是 "counter"
          branchId: this.counterInfo.branchId,  // 获取前台员工的 branchId
          reason: this.form.reason,  // 获取离职原因
          name: this.counterInfo.name,  // 获取前台员工的姓名
        };

        const res = await fetch("/api/leaving-working/counter-apply", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(payload),
        });

        const json = await res.json();
        if (json.status === "success") {
          alert("离职申请提交成功！");
          this.form.reason = "";  // 清空表单
        } else {
          alert(json.message || "提交失败");
        }
      } catch (err) {
        alert("请求错误：" + err.message);
      }
    },

    logout() {
      localStorage.removeItem("counterId");  // 移除 counterId 以退出
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

.sidebar li:hover {
  background-color: #ffb3b3;
  color: #fff;
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
