<template>
    <div class="dashboard-page">
      <!-- 左侧菜单（不变） -->
      <div class="sidebar">
        <h2>📌 绩效考核</h2>
        <ul>

          <li @click="$router.push('/branch-dashboard')">返回主页</li>
          <li @click="logout" class="logout">退出系统</li>
        </ul>
      </div>
  
      <!-- 右侧内容 -->
      <div class="form-section">
        <h2>员工绩效考核</h2>
  
        <!-- Tab切换 -->
        <div class="tab-buttons" style="margin-bottom: 20px; display: flex; gap: 12px;">
          <button
            :class="{ active: currentTab === 'edit' }"
            @click="currentTab = 'edit'"
            type="button"
          >考核录入</button>
          <button
            :class="{ active: currentTab === 'history' }"
            @click="loadHistory"
            type="button"
          >历史记录</button>
        </div>
  
        <!-- 考核录入 -->
        <div v-if="currentTab === 'edit'">
          <div class="filter-row" style="display: flex; align-items: center; gap: 20px; margin-bottom: 20px;">
            <label for="roleFilter" style="font-weight: 600;">按岗位筛选：</label>
            <select
              id="roleFilter"
              v-model="roleFilter"
              style="padding: 4px 8px; border-radius: 4px; border: 1px solid #ccc;"
            >
              <option value="">全部</option>
              <option value="服务员">服务员</option>
              <option value="厨师">厨师</option>
              <option value="HR">HR</option>
              <option value="收银员">收银员</option>
            </select>
  
            <button
              class="btn-save"
              @click="submitAll"
              :disabled="saving"
              style="margin-left:auto;"
            >
              {{ saving ? "保存中..." : "保存所有考核" }}
            </button>
          </div>
  
          <table v-if="filteredEmployees.length">
            <thead>
              <tr>
                <th>姓名</th>
                <th>岗位</th>
                <th>联系方式</th>
                <th>考核周期</th>
                <th>准时分</th>
                <th>主观分</th>
                <th>出勤分</th>
                <th>总分</th>
                <th>评语</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="e in filteredEmployees" :key="e.id">
                <td>{{ e.name }}</td>
                <td>{{ e.role }}</td>
                <td>{{ e.phone }}</td>
                <td>
                  <input
                    type="date"
                    v-model="e.reviewPeriodStart"
                    style="width: 120px"
                    @change="fetchAttendanceScore(e)"
                  /> ~
                  <input
                    type="date"
                    v-model="e.reviewPeriodEnd"
                    style="width: 120px"
                    @change="fetchAttendanceScore(e)"
                  />
                </td>
                <td>
                  <input
                    type="number"
                    v-model.number="e.punctualityScore"
                    @input="updateTotal(e)"
                    min="0"
                    max="100"
                  />
                </td>
                <td>
                  <input
                    type="number"
                    v-model.number="e.performanceScore"
                    @input="updateTotal(e)"
                    min="0"
                    max="100"
                  />
                </td>
                <td>{{ e.attendanceScore }}</td>
                <td>{{ e.totalScore }}</td>
                <td>
                  <input
                    type="text"
                    v-model="e.comments"
                    style="width: 140px"
                  />
                </td>
              </tr>
            </tbody>
          </table>
          <p v-else>暂无员工信息</p>
        </div>
  
        <!-- 历史考核记录 -->
        <div v-if="currentTab === 'history'">
          <div class="filter-row" style="display: flex; align-items: center; gap: 20px; margin-bottom: 20px;">
            <label for="historyRoleFilter" style="font-weight: 600;">按岗位筛选：</label>
            <select
              id="historyRoleFilter"
              v-model="historyRoleFilter"
              style="padding: 4px 8px; border-radius: 4px; border: 1px solid #ccc;"
              @change="filterHistory"
            >
              <option value="">全部</option>
              <option>服务员</option>
              <option>厨师</option>
              <option>HR</option>
              <option>收银员</option>
            </select>
          </div>
  
          <table v-if="filteredHistory.length">
            <thead>
              <tr>
                <th>员工ID</th>
                <th>岗位</th>
                <th>考核周期</th>
                <th>准时分</th>
                <th>主观分</th>
                <th>出勤分</th>
                <th>总分</th>
                <th>评语</th>
                <th>考核日期</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="record in filteredHistory" :key="record.id">
                <td>{{ record.employeeId }}</td>
                <td>{{ convertToRoleName(record.employeeType) }}</td>
                <td>{{ record.reviewPeriodStart }} ~ {{ record.reviewPeriodEnd }}</td>
                <td>{{ record.punctualityScore }}</td>
                <td>{{ record.performanceScore }}</td>
                <td>{{ record.attendanceScore }}</td>
                <td>{{ record.totalScore }}</td>
                <td>{{ record.comments }}</td>
                <td>{{ formatDate(record.createdAt) }}</td>
              </tr>
            </tbody>
          </table>
          <p v-else>暂无历史考核记录</p>
        </div>
      </div>
    </div>
  </template>
  
  <script>
  export default {
    name: "PerformanceReviewPage",
    data() {
      return {
        currentTab: "edit", // 当前Tab: 'edit' 或 'history'
        employees: [],
        saving: false,
        roleFilter: "",
  
        // 历史考核相关
        historyRecords: [],
        historyRoleFilter: "",
        filteredHistory: [],
      };
    },
    created() {
      this.fetchEmployees();
    },
    computed: {
      filteredEmployees() {
        if (!this.roleFilter) return this.employees;
        return this.employees.filter((e) => e.role === this.roleFilter);
      },
    },
    methods: {
      async fetchEmployees() {
        try {
          const branchId = localStorage.getItem("branchId") || 1;
          const res = await fetch(`/api/employees?branchId=${branchId}`);
          const json = await res.json();
          if (json.status === "success") {
            this.employees = json.data.map((emp) => ({
              ...emp,
              employeeId: emp.id,
              employeeType: this.convertToType(emp.role),
              reviewPeriodStart: "",
              reviewPeriodEnd: "",
              punctualityScore: 0,
              performanceScore: 0,
              attendanceScore: 0,
              totalScore: 0,
              comments: "",
            }));
          } else {
            alert("加载员工失败：" + json.message);
          }
        } catch (e) {
          alert("网络错误：" + e.message);
        }
      },
      convertToType(role) {
        switch (role) {
          case "服务员":
            return "waiter";
          case "厨师":
            return "chef";
          case "HR":
            return "hr";
          case "收银员":
            return "counter";
          default:
            return "unknown";
        }
      },
      // 英文岗位转中文岗位，历史考核显示用
      convertToRoleName(type) {
        switch (type) {
          case "waiter":
            return "服务员";
          case "chef":
            return "厨师";
          case "hr":
            return "HR";
          case "counter":
            return "收银员";
          default:
            return "未知岗位";
        }
      },
      // 中文岗位转英文岗位，历史考核筛选用
      roleNameToType(roleName) {
        switch (roleName) {
          case "服务员":
            return "waiter";
          case "厨师":
            return "chef";
          case "HR":
            return "hr";
          case "收银员":
            return "counter";
          default:
            return "";
        }
      },
      updateTotal(e) {
        e.totalScore =
          (e.punctualityScore || 0) +
          (e.performanceScore || 0) +
          (e.attendanceScore || 0);
      },
      async fetchAttendanceScore(e) {
        if (!e.reviewPeriodStart || !e.reviewPeriodEnd) {
          e.attendanceScore = 0;
          this.updateTotal(e);
          return;
        }
        try {
          const branchId = localStorage.getItem("branchId") || 1;
          const url = `/api/performance-reviews/calculate-attendance-score?employeeId=${e.employeeId}&employeeType=${e.employeeType}&branchId=${branchId}&startDate=${e.reviewPeriodStart}&endDate=${e.reviewPeriodEnd}`;
          const res = await fetch(url);
          if (!res.ok) throw new Error("网络请求失败");
          const score = await res.json();
          e.attendanceScore = score;
          this.updateTotal(e);
        } catch (error) {
          console.error("获取考勤分失败:", error);
          e.attendanceScore = 0;
          this.updateTotal(e);
        }
      },
      async submitAll() {
        if (this.saving) return;
        this.saving = true;
  
        try {
          for (const e of this.employees) {
            if (!e.reviewPeriodStart || !e.reviewPeriodEnd) continue;
  
            const payload = {
              employeeId: e.employeeId,
              employeeType: e.employeeType,
              branchId: e.branchId,
              reviewPeriodStart: e.reviewPeriodStart,
              reviewPeriodEnd: e.reviewPeriodEnd,
              punctualityScore: e.punctualityScore,
              performanceScore: e.performanceScore,
              comments: e.comments,
            };
  
            const res = await fetch("/api/performance-reviews", {
              method: "POST",
              headers: { "Content-Type": "application/json" },
              body: JSON.stringify(payload),
            });
  
            const saved = await res.json();
            if (saved.attendanceScore !== undefined) {
              e.attendanceScore = saved.attendanceScore;
              this.updateTotal(e);
            }
          }
  
          alert("考核信息已提交！");
        } catch (e) {
          alert("提交失败：" + e.message);
        } finally {
          this.saving = false;
        }
      },
      logout() {
        localStorage.clear();
        this.$router.push("/login");
      },
  
      // -------- 历史考核部分 --------
      async loadHistory() {
        this.currentTab = "history";
        const branchId = localStorage.getItem("branchId") || 1;
        try {
          const res = await fetch(`/api/performance-reviews/history?branchId=${branchId}`);
          if (!res.ok) throw new Error("网络请求失败，状态码：" + res.status);
          const data = await res.json(); // 纯数组
  
          // 直接用employeeId和employeeType，不从员工列表找姓名岗位
          this.historyRecords = data.map((rec) => ({
            ...rec,
            totalScore:
              (rec.punctualityScore || 0) +
              (rec.performanceScore || 0) +
              (rec.attendanceScore || 0),
          }));
  
          this.filteredHistory = this.historyRecords;
          this.historyRoleFilter = "";
        } catch (e) {
          alert("加载历史考核失败：" + e.message);
          this.historyRecords = [];
          this.filteredHistory = [];
        }
      },
  
      filterHistory() {
        if (!this.historyRoleFilter) {
          this.filteredHistory = this.historyRecords;
        } else {
          const type = this.roleNameToType(this.historyRoleFilter);
          this.filteredHistory = this.historyRecords.filter(
            (r) => r.employeeType === type
          );
        }
      },
      formatDate(datetime) {
        if (!datetime) return "";
        const dt = new Date(datetime);
        return dt.toLocaleDateString();
      },
    },
  };
  </script>
  
  <style scoped>
  .dashboard-page {
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
  
  h2 {
    margin-bottom: 24px;
    font-weight: 700;
    color: #333;
    font-size: 1.8rem;
  }
  
  .filter-row {
    margin-bottom: 24px;
  }
  
  .btn-save {
    background: linear-gradient(90deg, #3b82f6, #2563eb);
    border: none;
    color: white;
    padding: 10px 22px;
    border-radius: 6px;
    font-weight: 600;
    cursor: pointer;
    transition: background 0.3s ease;
  }
  .btn-save:disabled {
    background: #a5b4fc;
    cursor: not-allowed;
  }
  .btn-save:hover:not(:disabled) {
    background: linear-gradient(90deg, #2563eb, #1d4ed8);
  }
  
  table {
    width: 100%;
    border-collapse: separate;
    border-spacing: 0 12px;
    font-size: 1rem;
    color: #444;
  }
  
  th {
    background-color: #3b82f6;
    color: white;
    font-weight: 600;
    padding: 12px 16px;
    border-top-left-radius: 10px;
    border-top-right-radius: 10px;
    text-align: center;
    user-select: none;
  }
  
  td {
    background-color: white;
    padding: 12px 16px;
    text-align: center;
    box-shadow: 0 1px 3px rgba(0, 0,  0.1);
  border-bottom: 12px solid transparent;
  border-radius: 8px;
}

tr:hover td {
  background-color: #e0e7ff;
  box-shadow: 0 2px 8px rgba(59,130,246,0.3);
}

input[type="number"],
input[type="text"],
input[type="date"],
select {
  width: 100%;
  max-width: 140px;
  padding: 6px 10px;
  border: 1px solid #cbd5e1;
  border-radius: 6px;
  font-size: 0.95rem;
  transition: border-color 0.3s ease;
}

input[type="number"]:focus,
input[type="text"]:focus,
input[type="date"]:focus,
select:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 5px rgba(59,130,246,0.5);
}

/* Tab按钮样式 */
.tab-buttons button {
  padding: 8px 20px;
  border: none;
  background: #cbd5e1;
  color: #333;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
  transition: background-color 0.3s;
}
.tab-buttons button.active {
  background: #3b82f6;
  color: white;
}
.tab-buttons button:hover:not(.active) {
  background: #a5b4fc;
}
</style>

  