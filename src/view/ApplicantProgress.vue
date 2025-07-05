<template>
  <div class="resume-page">
    <div class="sidebar">
      <h2>📌 入职申请</h2>
      <ul>
        <li @click="$router.push('/join-us')">填写/修改信息</li>
        <li><strong>查看进度</strong></li>
        <li @click="logout" class="logout">退出系统</li>
      </ul>
    </div>

    <div class="form-section">
      <h3>我的申请进度</h3>

      <table v-if="requests.length">
        <thead>
          <tr>
            <th>职位</th>
            <th>门店</th>
            <th>申请时间</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in requests" :key="item.requestId">
            <td>{{ item.position }}</td>
            <td>{{ getBranchName(item.appliedBranchId) }}</td>
            <td>{{ formatDate(item.createdAt) || "无" }}</td>
            <td>
              <span :class="['status', statusClass(item.status)]">
                {{ translateStatus(item.status) }}
              </span>
            </td>
            <td>
              <button
                v-if="item.status === '店长审批通过已正式入职'"
                @click="openConfirmModal(item)"
              >
                确认入职
              </button>
              <span v-else>—</span>
            </td>
          </tr>
        </tbody>
      </table>

      <p v-else>暂无申请记录</p>
    </div>

    <!-- 确认入职弹窗 -->
    <div v-if="showConfirmModal" class="modal-overlay" @click.self="closeConfirmModal">
      <div class="modal-content">
        <h3>确认入职 - {{ selectedRequest.position }}</h3>
        <form @submit.prevent="confirmOnboarding">
          <label>
            用户名：
            <input v-model="confirmForm.username" required />
          </label>
          <label>
            密码：
            <input type="password" v-model="confirmForm.password" required />
          </label>
          <div class="buttons">
            <button type="submit">提交</button>
            <button type="button" @click="closeConfirmModal">取消</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "ApplicantProgress",
  data() {
    return {
      requests: [],
      branches: [],
      showConfirmModal: false,
      selectedRequest: null,
      confirmForm: {
        username: "",
        password: ""
      },
    };
  },
  created() {
    this.fetchBranches();
    this.fetchProgress();
  },
  methods: {
    async fetchBranches() {
      try {
        const res = await fetch("/api/restaurant/all-names");
        const json = await res.json();
        if (json.status === "success") {
          this.branches = json.data;
        }
      } catch (err) {
        console.error("加载门店列表失败", err);
      }
    },
    async fetchProgress() {
      const applicantId = localStorage.getItem("applicantId");
      if (!applicantId) {
        alert("未登录，请重新登录");
        return this.$router.push("/applicant-login");
      }

      try {
        const res = await fetch(`/api/onboarding/by-applicant?applicantId=${applicantId}`);
        if (!res.ok) throw new Error("网络响应错误");
        const json = await res.json();
        this.requests = json;
      } catch (err) {
        alert("加载申请记录失败：" + err.message);
      }
    },
    translateStatus(status) {
      switch (status) {
        case "已提交待审批":
          return "已提交待审批";
        case "HR审批通过待店长审批":
          return "HR审批通过待店长审批";
        case "店长审批通过已正式入职":
          return "店长审批通过已正式入职";
        case "员工已确认入职":
          return "员工已确认入职";
        case "已驳回":
          return "已驳回";
        default:
          return "未知";
      }
    },
    statusClass(status) {
      switch (status) {
        case "已提交待审批":
          return "pending";
        case "HR审批通过待店长审批":
          return "in-review";
        case "店长审批通过已正式入职":
          return "approved";
        case "员工已确认入职":
          return "confirmed";
        case "已驳回":
          return "rejected";
        default:
          return "unknown";
      }
    },
    formatDate(str) {
      if (!str) return "";
      return new Date(str).toLocaleDateString();
    },
    logout() {
      localStorage.removeItem("applicantUsername");
      localStorage.removeItem("applicantId");
      this.$router.push("/applicant-login");
    },
    getBranchName(branchId) {
      const branch = this.branches.find((b) => b.id === branchId);
      return branch ? branch.name : branchId;
    },
    openConfirmModal(request) {
      this.selectedRequest = request;
      this.confirmForm.username = "";
      this.confirmForm.password = "";
      this.showConfirmModal = true;
    },
    closeConfirmModal() {
      this.showConfirmModal = false;
    },
    async confirmOnboarding() {
      if (!this.confirmForm.username || !this.confirmForm.password) {
        alert("用户名和密码不能为空");
        return;
      }
      try {
        const res = await fetch(`/api/onboarding/${this.selectedRequest.requestId}/confirm`, {
          method: "POST",
          headers: {
            "Content-Type": "application/json"
          },
          body: JSON.stringify({
            username: this.confirmForm.username,
            password: this.confirmForm.password
          })
        });
        const json = await res.json();
        if (json.status === "success") {
          alert("确认入职成功");
          this.showConfirmModal = false;
          this.fetchProgress();
        } else {
          alert("确认入职失败: " + json.message);
        }
      } catch (err) {
        alert("请求失败: " + err.message);
      }
    }
  },
};
</script>

<style scoped>
html, body, #app {
  margin: 0;
  padding: 0;
  height: 100%;
  width: 100%;
  overflow: hidden;
  background: none;
}

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

table {
  width: 100%;
  border-collapse: collapse;
  background: #fff;
  border-radius: 6px;
  overflow: hidden;
  box-shadow: 0 0 12px rgba(0, 0, 0, 0.05);
}

th, td {
  padding: 14px 24px;
  text-align: center;
  border-bottom: 1px solid #eee;
  font-size: 16px;
  color: #555;
}

th {
  background-color: #007bff;
  color: white;
  font-weight: 600;
}

tr:hover {
  background-color: #f1f7ff;
}

p {
  font-size: 18px;
  margin-top: 30px;
  color: #888;
  text-align: center;
}

.status {
  display: inline-block;
  padding: 6px 14px;
  border-radius: 20px;
  color: white;
  font-weight: 600;
  font-size: 14px;
}

.status.pending {
  background-color: #f39c12;
}

.status.in-review {
  background-color: #2980b9;
}

.status.approved {
  background-color: #27ae60;
}

.status.confirmed {
  background-color: #16a085;
}

.status.rejected {
  background-color: #c0392b;
}

.status.unknown {
  background-color: #7f8c8d;
}

/* 弹窗样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0,0,0,0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 999;
}

.modal-content {
  background: white;
  padding: 30px;
  border-radius: 6px;
  width: 320px;
  box-sizing: border-box;
}

.modal-content form label {
  display: block;
  margin-bottom: 15px;
  font-weight: 600;
}

.modal-content input {
  width: 100%;
  padding: 6px 8px;
  box-sizing: border-box;
  margin-top: 6px;
  border: 1px solid #ccc;
  border-radius: 4px;
}

.modal-content .buttons {
  margin-top: 20px;
  text-align: right;
}

.modal-content button {
  padding: 6px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.modal-content button[type="submit"] {
  background-color: #007bff;
  color: white;
  margin-right: 10px;
}

.modal-content button[type="button"] {
  background-color: #ccc;
  color: #333;
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

  table, th, td {
    font-size: 14px;
    padding: 10px;
  }
}
</style>
