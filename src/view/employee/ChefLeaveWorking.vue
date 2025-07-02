<template> 
  <div class="resume-page">
    <div class="sidebar">
      <h2>📌 离职申请</h2>
      <ul>
        <li @click="$router.push('/chef-dashboard')">个人档案</li>
        <li @click="$router.push('/employ-leaving-Status')">查看离职申请进度</li>
        <li @click="$router.push('/employee-attendance')">考勤打卡</li>
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
  name: "EmployeeLeave",
  data() {
    return {
      form: {
        reason: "",
      },
      employeeInfo: null, // 用于存储员工信息
    };
  },
  created() {
    this.loadEmployeeInfo();
  },
  methods: {
    // 加载员工信息，包括姓名（username）
    async loadEmployeeInfo() {
      const chefId = localStorage.getItem("chefId");  // 使用 chefId 来获取员工信息
      if (!chefId) {
        alert("未登录");
        this.$router.push("/login");
        return;
      }
      try {
        const res = await fetch(`/api/chef/${chefId}`);  // 使用 chefId 请求员工信息
        const json = await res.json();
        if (json.status === "success") {
          this.employeeInfo = json.data;
        } else {
          alert(json.message || "加载员工信息失败");
        }
      } catch (err) {
        alert("请求错误：" + err.message);
      }
    },

    // 提交离职申请
    async submitLeaveRequest() {
      if (!this.employeeInfo) {
        alert("员工信息未加载");
        return;
      }
      try {
        const payload = {
          employeeId: this.employeeInfo.id,  // 使用 employeeInfo.id 提交离职申请
          employeeType: "chef",  // 假设员工类型是 "chef"
          branchId: this.employeeInfo.branchId,  // 获取员工的 branchId
          reason: this.form.reason,  // 获取离职原因
          name: this.employeeInfo.username,  // 将员工的 username（即姓名）传递到后端
        };

        const res = await fetch("/api/leaving-working/apply", {
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
      localStorage.removeItem("chefId");  // 移除 chefId 以退出
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
