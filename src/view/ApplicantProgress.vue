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
            </tr>
          </tbody>
        </table>
  
        <p v-else>暂无申请记录</p>
      </div>
    </div>
  </template>
  
  <script>
  export default {
    name: "ApplicantProgress",
    data() {
      return {
        requests: [],
        branches: [], // 门店列表，需提前加载
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

.status.rejected {
  background-color: #c0392b;
}

.status.unknown {
  background-color: #7f8c8d;
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
  