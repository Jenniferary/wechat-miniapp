<template>
    <div class="employee-page">
      <nav class="sidebar">
        <h2>📋 员工档案</h2>
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
      </nav>
  
      <main class="content">
        <h3>👥 当前筛选：{{ selectedRoleLabel }}</h3>
  
        <div class="role-selector">
          <button
            v-for="r in roles"
            :key="r.value"
            :class="{ active: selectedRole === r.value }"
            @click="selectRole(r.value)"
          >
            {{ r.label }}
          </button>
        </div>
  
        <div class="branch-filter">
        <label class="switch">
        <input type="checkbox" v-model="onlyMyBranch" @change="loadEmployees" />
            <span class="slider"></span>
        </label>
        <span>仅查看本门店</span>
        </div>
  
        <table class="employee-table">
          <thead>
            <tr>
              <th>姓名</th>
              <th>用户名</th>
              <th>手机号</th>
              <th>邮箱</th>
              <th>职位</th>
              <th>门店ID</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="emp in employees" :key="emp.id">
              <td>{{ emp.name }}</td>
              <td>{{ emp.username }}</td>
              <td>{{ emp.phone }}</td>
              <td>{{ emp.email }}</td>
              <td>{{ emp.role }}</td>
              <td>{{ emp.branchId }}</td>
            </tr>
          </tbody>
        </table>
      </main>
    </div>
  </template>
  
  <script>
  export default {
    name: "HrEmployee",
    data() {
      return {
        roles: [
          { label: "全部", value: "" },
          { label: "服务员", value: "waiter" },
          { label: "厨师", value: "chef" },
          { label: "收银员", value: "counter" },
          { label: "HR", value: "hr" },
        ],
        selectedRole: "",
        employees: [],
        onlyMyBranch: false,
        currentHr: null,
      };
    },
    computed: {
      selectedRoleLabel() {
        const found = this.roles.find((r) => r.value === this.selectedRole);
        return found ? found.label : "全部";
      },
    },
    created() {
      this.loadCurrentHr();
    },
    methods: {
      async loadCurrentHr() {
        const hrId = localStorage.getItem("hrId");
        if (!hrId) {
          alert("未登录");
          this.$router.push("/login");
          return;
        }
        try {
          const res = await fetch(`/api/hr/${hrId}`);
          const json = await res.json();
          if (json.status === "success") {
            this.currentHr = json.data;
            this.loadEmployees();
          } else {
            alert("加载HR信息失败");
          }
        } catch (err) {
          alert("请求错误：" + err.message);
        }
      },
      selectRole(role) {
        this.selectedRole = role;
        this.loadEmployees();
      },
      async loadEmployees() {
        let url = `/api/employees`;
        const query = [];
  
        if (this.selectedRole) query.push(`role=${this.selectedRole}`);
        if (this.onlyMyBranch && this.currentHr) {
          query.push(`branchId=${this.currentHr.branchId}`);
        }
  
        if (query.length > 0) {
          url += `?${query.join("&")}`;
        }
  
        try {
          const res = await fetch(url);
          const json = await res.json();
          if (json.status === "success") {
            this.employees = json.data;
          } else {
            alert("获取员工信息失败：" + (json.message || ""));
          }
        } catch (e) {
          alert("请求失败：" + e.message);
        }
      },
      logout() {
        localStorage.removeItem("hrId");
        localStorage.removeItem("hrUsername");
        this.$router.push("/login");
      },
    },
  };
  </script>
  
  <style scoped>
  .employee-page {
    display: flex;
    width: 100vw;
    height: 100vh;
    overflow: hidden;
    font-family: "Segoe UI", Tahoma, Geneva, Verdana, sans-serif;
    background: #f2f4f8;
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
  
  .content {
    width: calc(100vw - 240px);
    height: 100vh;
    overflow-y: auto;
    padding: 40px;
    background-color: #f8f9fa;
    box-sizing: border-box;
  }
  .content h3 {
    font-size: 22px;
    color: #333;
    margin-bottom: 20px;
  }
  
  .role-selector {
    margin-bottom: 12px;
  }
  .role-selector button {
    margin-right: 10px;
    padding: 8px 18px;
    font-size: 14px;
    border: 1px solid #ccc;
    border-radius: 20px;
    background-color: white;
    color: #333;
    cursor: pointer;
    transition: all 0.2s ease;
  }
  .role-selector button:hover {
    background-color: #e6eefc;
  }
  .role-selector button.active {
    background-color: #1d3557;
    color: white;
    border-color: transparent;
  }
  .branch-filter {
  margin-bottom: 20px;
  font-size: 14px;
  color: #444;
  display: flex;      
  align-items: center; 
  gap: 8px;            
}


/* 开关容器 */
.switch {
  position: relative;
  display: inline-block;
  width: 42px;
  height: 24px;
  margin-top: 2px;
}

/* 隐藏真实checkbox */
.switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

/* 滑动背景 */
.slider {
  position: absolute;
  cursor: pointer;
  top: 0; left: 0; right: 0; bottom: 0;
  background-color: #ccc;
  border-radius: 24px;
  transition: 0.4s;
}

.slider:before {
  position: absolute;
  content: "";
  height: 18px;
  width: 18px;
  left: 3px;
  bottom: 3px;
  background-color: white;
  border-radius: 50%;
  transition: 0.4s;
}

.switch input:checked + .slider {
  background-color: #1d3557;
}

.switch input:checked + .slider:before {
  transform: translateX(18px);
}
  .employee-table {
    width: 100%;
    border-collapse: separate;
    border-spacing: 0 8px;
    background: transparent;
  }
  .employee-table thead th {
    background-color: #0077b6;
    color: white;
    padding: 12px;
    text-align: center;
    font-weight: bold;
    border-radius: 6px 6px 0 0;
  }
  .employee-table tbody tr {
    background-color: white;
    box-shadow: 0 2px 6px rgba(0, 0, 0, 0.04);
    transition: transform 0.1s;
  }
  .employee-table tbody tr:hover {
    transform: scale(1.005);
  }
  .employee-table td {
    padding: 12px;
    text-align: center;
    font-size: 14px;
    color: #333;
  }
  </style>
  