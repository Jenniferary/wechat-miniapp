<template>
  <div class="resume-page">
    <div class="sidebar">
      <h2>👨‍🍳 请假管理</h2>
      <ul>
        <li @click="$router.push('/waiter-dashboard')">个人档案</li>
          <li @click="$router.push('/waiter-attendance')">考勤打卡</li>
          <li @click="$router.push('/waiter-leave')">请假申请</li>
          <li @click="$router.push('/waiter-leave-progress')">我的请假记录</li>
          <li @click="$router.push('/waiter-leave-working')">离职申请</li>
          <li @click="$router.push('/waiter-leaving-status')">查看离职申请进度</li>
          <li @click="$router.push('/waiter-overtime-working')">加班申请</li>
          <li @click="$router.push('/waiter-overtime-progress')">我的加班记录</li>
          <li @click="$router.push('/waiter-salary')">工资管理</li>
          <li @click="logout" class="logout">退出系统</li>
      </ul>
    </div>

    <div class="form-section">
      <h3>我的请假流程</h3>
      <table class="leave-table" v-if="leaveRecords.length > 0">
        <thead>
          <tr>
            <th>起始日期</th>
            <th>结束日期</th>
            <th>请假原因</th>
            <th>状态</th>
            <th>审批备注</th>
            <th style="width: 90px;">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="record in leaveRecords" :key="record.id">
            <td>{{ formatDate(record.startDate) }}</td>
            <td>{{ formatDate(record.endDate) }}</td>
            <td>{{ record.reason }}</td>
            <td>
              <span :class="statusClass(record.status)">
                {{ statusText(record.status) }}
              </span>
            </td>
            <td>{{ record.remark || '-' }}</td>
            <td>
              <button class="btn-detail" @click="showDetail(record)">查看详情</button>
            </td>
          </tr>
        </tbody>
      </table>
      <p v-else>暂无请假记录</p>
    </div>

    <!-- 弹窗：流程图 -->
    <transition name="fade">
      <div v-if="detailVisible" class="modal" @click.self="detailVisible = false">
        <div class="modal-box">
          <header>
            <h4>流程详情</h4>
            <button class="close" @click="detailVisible = false">✕</button>
          </header>

          <section class="flow">
            <template v-for="(step, i) in flowSteps" :key="step.value">
              <div
                class="node"
                :class="{
                  done: i < currIndex,
                  active: i === currIndex && !step.rejected,
                  rejected: step.rejected
                }"
              >
                <div class="circle">{{ i + 1 }}</div>
                <span v-html="step.label" />
              </div>
              <div
                v-if="i !== flowSteps.length - 1"
                class="line"
                :class="{ done: i < currIndex }"
              />
            </template>
          </section>

          <ul class="info">
            <li><strong>起止：</strong>{{ formatDate(cur.startDate) }} ～ {{ formatDate(cur.endDate) }}</li>
            <li><strong>原因：</strong>{{ cur.reason }}</li>
            <li><strong>状态：</strong>{{ statusText(cur.status) }}</li>
            <li><strong>备注：</strong>{{ cur.remark || '-' }}</li>
          </ul>
        </div>
      </div>
    </transition>
  </div>
</template>

<script>
export default {
  name: "WaiterLeaveProcess",
  data() {
    return {
      waiterInfo: null,
      leaveRecords: [],
      detailVisible: false,
      cur: {},
      flowSteps: []
    };
  },
  computed: {
    currIndex() {
      return this.flowSteps.findIndex(s => s.value === this.cur.status);
    }
  },
  created() {
    this.loadWaiterInfo();
  },
  methods: {
    async loadWaiterInfo() {
      const waiterId = localStorage.getItem("waiterId");
      if (!waiterId) {
        alert("未登录");
        this.$router.push("/login");
        return;
      }
      try {
        const resUser = await fetch(`/api/waiters/${waiterId}`);
        const jsonUser = await resUser.json();
        if (jsonUser.status === "success") {
          this.waiterInfo = jsonUser.data;
          this.loadLeaveRecords(waiterId);
        } else {
          alert(jsonUser.message || "加载用户信息失败");
        }
      } catch (err) {
        alert("请求错误：" + err.message);
      }
    },
    async loadLeaveRecords(waiterId) {
      try {
        const res = await fetch(`/api/leave/history/${waiterId}?employeeType=waiter`);
        const json = await res.json();
        if (json.status === "success") {
          this.leaveRecords = json.data.records || [];
        } else {
          alert(json.message || "加载请假记录失败");
        }
      } catch (err) {
        alert("请求错误：" + err.message);
      }
    },
    formatDate(dateStr) {
      if (!dateStr) return "-";
      const d = new Date(dateStr);
      return d.toLocaleDateString();
    },
    statusText(status) {
      switch (status) {
        case "待HR审批":
          return "待HR审批";
        case "HR审批通过待店长审批":
          return "HR审批通过，待店长审批";
        case "审批成功":
          return "审批成功";
        case "已驳回":
          return "已驳回";
        default:
          return status;
      }
    },
    statusClass(status) {
      switch (status) {
        case "待HR审批":
        case "HR审批通过待店长审批":
          return "status-pending";
        case "审批成功":
          return "status-approved";
        case "已驳回":
          return "status-rejected";
        default:
          return "";
      }
    },
    showDetail(rec) {
      this.cur = rec;
      if (rec.status === "已驳回") {
        this.flowSteps = [
          { value: "待HR审批", label: "提交<br>待HR" },
          { value: "HR审批通过待店长审批", label: "HR通过<br>待店长" },
          { value: "已驳回", label: "已驳回", rejected: true }
        ];
      } else {
        this.flowSteps = [
          { value: "待HR审批", label: "提交<br>待HR" },
          { value: "HR审批通过待店长审批", label: "HR通过<br>待店长" },
          { value: "审批成功", label: "审批成功" }
        ];
      }
      this.detailVisible = true;
    },
    logout() {
      localStorage.removeItem("waiterId");
      this.$router.push("/login");
    }
  }
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
.leave-table {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0 12px; /* 行间距 */
  font-size: 14px;
  color: #333;
}

.leave-table thead th {
  background: #007bff;
  color: #fff;
  font-weight: 600;
  padding: 12px 15px;
  text-align: center;
  user-select: none;
}

.leave-table tbody tr {
  background: #fff;
  box-shadow: 0 1px 6px rgb(0 0 0 / 0.08);
  border-radius: 10px;
  transition: transform 0.15s ease, box-shadow 0.15s ease;
  cursor: pointer;
}

.leave-table tbody tr:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 20px rgb(0 0 0 / 0.12);
}

.leave-table tbody td {
  padding: 12px 15px;
  text-align: center;
  vertical-align: middle;
  border: none; /* 去掉默认边框 */
}

/* 按钮样式 */
.btn-detail {
  background: #007bff;
  color: white;
  border: none;
  border-radius: 20px;
  padding: 6px 16px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.btn-detail:hover {
  background: #0056b3;
}

/* 状态标签改成带背景色的pill样式 */
.status-pending {
  color: #d97706;
  background: #fff7e6;
  font-weight: 600;
  padding: 4px 10px;
  border-radius: 15px;
  display: inline-block;
  min-width: 80px;
}

.status-approved {
  color: #15803d;
  background: #dcfce7;
  font-weight: 600;
  padding: 4px 10px;
  border-radius: 15px;
  display: inline-block;
  min-width: 80px;
}

.status-rejected {
  color: #b91c1c;
  background: #fee2e2;
  font-weight: 600;
  padding: 4px 10px;
  border-radius: 15px;
  display: inline-block;
  min-width: 80px;
}

.btn-detail {
  background: #2980b9;
  color: #fff;
  border: none;
  border-radius: 4px;
  padding: 6px 10px;
  cursor: pointer;
  font-size: 13px;
}
.btn-detail:hover {
  background: #206fa1;
}

/* 弹窗样式和动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
.modal {
  position: fixed;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.45);
  z-index: 999;
}
.modal-box {
  width: 560px;
  max-width: 92vw;
  background: #fff;
  border-radius: 10px;
  padding: 26px 30px 34px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
}
.modal-box header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}
.modal-box h4 {
  font-size: 20px;
  margin: 0;
}
.close {
  background: transparent;
  border: none;
  font-size: 22px;
  cursor: pointer;
}
.flow {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 28px;
}
.node {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 22%;
  user-select: none;
}
.circle {
  width: 40px;
  height: 40px;
  line-height: 40px;
  border-radius: 50%;
  background: #e0e0e0;
  font-weight: 700;
  text-align: center;
}
.node.done .circle,
.node.active .circle {
  background: #007bff;
  color: #fff;
}
.node.active .circle {
  animation: pulse 1.2s infinite;
}
@keyframes pulse {
  0% {
    box-shadow: 0 0 0 0 rgba(0, 123, 255, 0.6);
  }
  70% {
    box-shadow: 0 0 0 12px rgba(0, 123, 255, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(0, 123, 255, 0);
  }
}
.node span {
  font-size: 13px;
  margin-top: 8px;
  line-height: 1.3;
  text-align: center;
  color: #666;
}
.node.done span,
.node.active span {
  color: #007bff;
}
/* 已驳回样式 */
.node.rejected .circle {
  background: #c0392b !important;
  color: #fff;
  animation: none;
}
.node.rejected span {
  color: #c0392b !important;
}
.line {
  flex: 1;
  height: 4px;
  background: #e0e0e0;
}
.line.done {
  background: #007bff;
}
.info {
  list-style: none;
  padding: 0;
  margin: 0;
  font-size: 14px;
  line-height: 2;
}
</style>
