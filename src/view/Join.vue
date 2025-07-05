<template>
  <div class="resume-page">
    <div class="sidebar">
      <h2>📌 入职申请</h2>
      <ul>
        <li @click="$router.push('/join-us')">填写/修改信息</li>
        <li @click="$router.push('/applicant-progress')">查看进度</li>
        <li @click="logout" class="logout">退出系统</li>
      </ul>
    </div>

    <div class="form-section">
      <h3>填写基本信息</h3>
      <form @submit.prevent="submitApplication">
        <div class="form-row">
          <label>姓名：</label>
          <input v-model="form.name" required />
          <label>性别：</label>
          <select v-model="form.gender" required>
            <option disabled value="">请选择</option>
            <option>男</option>
            <option>女</option>
          </select>
        </div>

        <div class="form-row">
          <label>出生日期：</label>
          <input v-model="form.dateOfBirth" type="date" required />
          <label>联系电话：</label>
          <input v-model="form.phone" />
        </div>

        <div class="form-row">
          <label>邮箱：</label>
          <input v-model="form.email" />
          <label>申请职位：</label>
          <select v-model="form.position" required>
            <option disabled value="">请选择</option>
            <option>服务员</option>
            <option>前台</option>
            <option>后厨</option>
            <option>HR</option>
          </select>
        </div>

        <div class="form-row">
          <label>门店：</label>
          <select v-model.number="form.appliedBranchId" required>
            <option disabled value="">请选择门店</option>
            <option v-for="branch in branches" :key="branch.id" :value="branch.id">
              {{ branch.name }}
            </option>
          </select>
        </div>

        <div class="form-row single">
          <label>简历/经历：</label>
          <textarea
            v-model="form.resume"
            placeholder="请输入您的工作经历或简历内容"
          ></textarea>
        </div>

        <div class="form-actions">
          <button type="submit">提交申请</button>
        </div>
      </form>
    </div>
  </div>
</template>

<script>
export default {
  name: "JoinForm",
  data() {
    return {
      form: {
        name: "",
        gender: "",
        dateOfBirth: "",
        phone: "",
        email: "",
        position: "",
        appliedBranchId: null,
        resume: "",
      },
      branches: [],
    };
  },
  created() {
    this.fetchBranches();
  },
  methods: {
    async fetchBranches() {
      try {
        const res = await fetch("/api/restaurant/all-names");
        const json = await res.json();
        if (json.status === "success") {
          this.branches = json.data;
        } else {
          console.error("获取门店列表失败", json);
        }
      } catch (error) {
        console.error("请求门店列表异常", error);
      }
    },
    async submitApplication() {
  try {
    const applicantId = localStorage.getItem("applicantId");
    if (!applicantId) {
      alert("请先登录");
      this.$router.push("/applicant-login");
      return;
    }

    const payload = {
      ...this.form,
      applicantId: Number(applicantId), // 关键，确保是数字
    };

    const response = await fetch("/api/onboarding", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
    });

    const resultText = await response.text();
    alert(resultText);
  } catch (error) {
    alert("提交失败：" + error.message);
  }
},
    logout() {
      localStorage.removeItem("applicantUsername");
      localStorage.removeItem("applicantId");
      this.$router.push("/applicant-login");
    },
  },
};
</script>

<style scoped>
html, body {
  margin: 0;
  padding: 0;
  height: 100%;
  width: 100%;
  background: none;
}

.resume-page {
  display: flex;
  height: 100vh;
  width: 100vw;
  font-family: "Segoe UI", sans-serif;
  background: none;
}

/* 左侧导航栏 */
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

/* 右侧表单区域 */
.form-section {
  flex: 1;
  background: white;
  padding: 40px 80px;
  box-sizing: border-box;
  height: 100vh;
  overflow-y: auto;
  border-radius: 0;
}

.form-section h3 {
  font-size: 24px;
  margin-bottom: 30px;
  border-left: 4px solid #007bff;
  padding-left: 12px;
  color: #333;
}

form {
  max-width: 960px;
}

.form-row {
  display: grid;
  grid-template-columns: 320px 1fr 120px 1fr;
  gap: 12px 20px;
  align-items: center;
  margin-bottom: 20px;
}

.form-row.single {
  grid-template-columns: 320px 1fr;
}

label {
  font-weight: bold;
  text-align: right;
  color: #333;
}

input,
select,
textarea {
  padding: 10px 12px;
  font-size: 15px;
  border: 1px solid #ccc;
  border-radius: 6px;
  width: 100%;
  box-sizing: border-box;
}

textarea {
  height: 100px;
  resize: vertical;
}

.form-actions {
  text-align: left;
  margin-top: 30px;
}

button {
  background-color: #007bff;
  border: none;
  color: white;
  font-size: 16px;
  padding: 10px 24px;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.3s ease;
  margin-left: 550px; /* 和 label 对齐 */
}

button:hover {
  background-color: #0056b3;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .resume-page {
    flex-direction: column;
  }

  .form-section {
    padding: 30px 20px;
    height: auto;
  }

  .sidebar {
    width: 100%;
    text-align: center;
  }

  .form-row {
    grid-template-columns: 1fr;
  }

  label {
    text-align: left;
  }

  button {
    margin-left: 0;
    width: 100%;
  }
}

</style>
