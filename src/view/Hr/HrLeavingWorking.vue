<template>
  <div class="hr-leaving-page">
    <div class="sidebar">
      <h2>📌 HR 离职申请</h2>
      <ul>
        <li @click="$router.push('/hr-leaving-status')">查看我的离职进度</li>
        <li @click="$router.push('/hr-dashboard')">返回主页</li>
        <li><strong>申请离职</strong></li>
        <li @click="logout" class="logout">退出系统</li>
      </ul>
    </div>

    <div class="form-section">
      <h3>申请自己的离职</h3>

      <form @submit.prevent="submitLeaveRequest">
        <div class="form-row">
          <label for="reason">离职原因：</label>
          <textarea v-model="form.reason" required rows="4"></textarea>
        </div>

        <button type="submit">提交离职申请</button>
      </form>
    </div>
  </div>
</template>

<script>
export default {
  name: "HrLeavingWorking",
  data() {
    return {
      form: {
        reason: "",  // 离职原因
      },
      hrInfo: null,  // 用于存储HR信息
    };
  },
  created() {
    this.loadHrInfo();
  },
  methods: {
    // 加载当前HR的详细信息
    async loadHrInfo() {
      const hrId = localStorage.getItem("hrId");  // 使用 hrId 来获取HR信息
      if (!hrId) {
        alert("未登录");
        this.$router.push("/login");
        return;
      }

      try {
        const res = await fetch(`/api/hr/${hrId}`);  // 使用 hrId 请求HR信息
        const json = await res.json();
        if (json.status === "success") {
          this.hrInfo = json.data;
        } else {
          alert(json.message || "加载HR信息失败");
        }
      } catch (err) {
        alert("请求错误：" + err.message);
      }
    },

    // 提交离职申请
    async submitLeaveRequest() {
      if (!this.hrInfo) {
        alert("HR信息未加载");
        return;
      }

      try {
        const payload = {
          employeeId: this.hrInfo.id,  // 使用 hrInfo.id 提交离职申请
          employeeType: "hr",  // 员工类型是 "hr"
          branchId: this.hrInfo.branchId,  // 获取HR的branchId
          reason: this.form.reason,  // 获取离职原因
          name: this.hrInfo.name,  // HR姓名
        };

        const res = await fetch("/api/leaving-working/hr-apply", {
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

    // 退出系统
    logout() {
      localStorage.removeItem("hrId");  // 移除 hrId 以退出
      this.$router.push("/login");
    },
  },
};
</script>

<style scoped>
/* 样式保持一致 */
.hr-leaving-page {
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
  color: white;
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

@media (max-width: 768px) {
  .hr-leaving-page {
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
