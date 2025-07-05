<template>
    <div class="resume-page">
      <div class="sidebar">
        <h2>📌 入职审批</h2>
        <ul>
          <li @click="$router.push('/hr-dashboard')"><strong>入职待审批列表</strong></li>
          <li @click="$router.push('/hr-profile')">个人档案</li> 
          <li @click="$router.push('/hr-employee')">员工档案</li>
          <li @click="$router.push('/hr-attendance')">考勤打卡</li>
          <li @click="$router.push('/hr-leave')">请假申请</li>
          <li @click="$router.push('/hr-leave-progress')">我的请假记录</li>
          <li @click="$router.push('/hr-leave-review')">请假待审批</li>
          <li @click="$router.push('/hr-overtime-working')">加班申请</li>
          <li @click="$router.push('/hr-overtime-progress')">我的加班记录</li>
          <li @click="$router.push('/hr-overtime-approval')">加班待审批</li>
          <li @click="$router.push('/hr-overtime-approval-history')">加班审批记录</li>
          <li @click="$router.push('/hr-leaving-working')">离职申请</li>
          <li @click="$router.push('/hr-leaving-status')">查看我的离职进度</li>
          <li @click="$router.push('/hr-leavingworking-review')">离职待审批</li>
          <li @click="$router.push('/hr-salary')">工资管理</li>
          <li @click="logout" class="logout">退出系统</li>
        </ul>
      </div>
  
      <div class="form-section">
        <h3>待审批入职申请</h3>
  
        <table v-if="requests.length">
          <thead>
            <tr>
              <th>姓名</th>
              <th>性别</th>
              <th>职位</th>
              <th>门店</th>
              <th>申请时间</th>
              <th>状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="req in requests" :key="req.requestId">
              <td>{{ req.name }}</td>
              <td>{{ req.gender }}</td>
              <td>{{ req.position }}</td>
              <td>{{ getBranchName(req.appliedBranchId) }}</td>
              <td>{{ formatDate(req.createdAt) }}</td>
              <td>
                <span :class="['status', statusClass(req.status)]">{{ translateStatus(req.status) }}</span>
              </td>
              <td>
                <button v-if="req.status === '已提交待审批'" @click="approve(req.requestId)" class="btn-approve">通过</button>
                <button v-if="req.status === '已提交待审批'" @click="reject(req.requestId)" class="btn-reject">驳回</button>
              </td>
            </tr>
          </tbody>
        </table>
  
        <p v-else>暂无待审批的入职申请</p>
      </div>
    </div>
  </template>
  
  <script>
  export default {
    name: "HrApproval",
    data() {
      return {
        requests: [],
        branches: [],
      };
    },
    created() {
      this.fetchBranches();
      this.fetchPendingRequests();
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
      async fetchPendingRequests() {
      try {
      const hrId = localStorage.getItem("hrId");
      if (!hrId) throw new Error("未登录");
      const hrRes = await fetch(`/api/hr/${hrId}`);
      const hrJson = await hrRes.json();
      if (hrJson.status !== "success") throw new Error("获取HR信息失败");
      const branchId = hrJson.data.branchId;
      // 请求当前门店待审批申请
      const res = await fetch(`/api/onboarding/pending-by-branch?branchId=${branchId}`);
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      const json = await res.json();
      this.requests = json;
    } catch (err) {
      alert("加载待审批申请失败：" + err.message);
    }
   },
      translateStatus(status) {
        switch (status) {
          case "已提交待审批": return "已提交待审批";
          case "HR审批通过待店长审批": return "HR审批通过待店长审批";
          case "店长审批通过已正式入职": return "店长审批通过已正式入职";
          case "已驳回": return "已驳回";
          default: return "未知";
        }
      },
      statusClass(status) {
        switch (status) {
          case "已提交待审批": return "pending";
          case "HR审批通过待店长审批": return "in-review";
          case "店长审批通过已正式入职": return "approved";
          case "已驳回": return "rejected";
          default: return "unknown";
        }
      },
      formatDate(str) {
        if (!str) return "";
        return new Date(str).toLocaleDateString();
      },
      getBranchName(branchId) {
        const branch = this.branches.find(b => b.id === branchId);
        return branch ? branch.name : branchId;
      },
      async approve(requestId) {
        try {
          const res = await fetch(`/api/onboarding/${requestId}/status?status=HR审批通过待店长审批`, {
            method: "PUT",
          });
          if (!res.ok) throw new Error(`HTTP ${res.status}`);
          alert("初审通过，状态已更新为审批中，转交给店长处理");
          this.fetchPendingRequests();
        } catch (err) {
          alert("更新状态失败：" + err.message);
        }
      },
      async reject(requestId) {
        try {
          const res = await fetch(`/api/onboarding/${requestId}/status?status=已驳回`, {
            method: "PUT",
          });
          if (!res.ok) throw new Error(`HTTP ${res.status}`);
          alert("已驳回该申请");
          this.fetchPendingRequests();
        } catch (err) {
          alert("更新状态失败：" + err.message);
        }
      },
      logout() {
        localStorage.clear();
        this.$router.push("/login");
      }
    },
  };
  </script>
  
  <style scoped>
  /* 跟你给的样式保持一致，只略微加了按钮颜色 */
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
  
  .btn-approve {
    background-color: #27ae60;
    color: white;
    border: none;
    padding: 6px 12px;
    border-radius: 4px;
    cursor: pointer;
    margin-right: 8px;
  }
  
  .btn-approve:hover {
    background-color: #219150;
  }
  
  .btn-reject {
    background-color: #c0392b;
    color: white;
    border: none;
    padding: 6px 12px;
    border-radius: 4px;
    cursor: pointer;
  }
  
  .btn-reject:hover {
    background-color: #992d22;
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
  