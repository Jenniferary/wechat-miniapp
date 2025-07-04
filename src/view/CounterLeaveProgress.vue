<template>
  <div class="page">
    <!-- 侧栏 -->
    <aside class="sidebar">
      <h2>💁‍♀️ 前台管理系统</h2>
      <ul>
        <li :class="{ active: activeSection === 'profile' }" @click="selectSection('profile', '/counter-dashboard')">个人档案</li>
        <li :class="{ active: activeSection === 'dinein' }" @click="selectSection('dinein', '/counter-dinein-order')">管理堂食订单</li>
        <li :class="{ active: activeSection === 'tables' }" @click="selectSection('tables', '/manage-tables')">管理餐桌</li>
        <li>
          <strong @click="toggleSection('delivery')" :class="{ active: activeSection === 'delivery' }">外卖管理</strong>
        </li>
        <li v-if="activeSection === 'delivery'" :class="{ active: activeSubsection === 'assign' }" @click="selectSubsection('assign', '/delivery-assign')">分配外卖员</li>
        <li v-if="activeSection === 'delivery'" :class="{ active: activeSubsection === 'add' }" @click="selectSubsection('add', '/delivery-add')">添加外卖员</li>
        <li v-if="activeSection === 'delivery'" :class="{ active: activeSubsection === 'view' }" @click="selectSubsection('view', '/delivery-view')">查看外卖订单</li>
        <li :class="{ active: activeSection === 'attendance' }" @click="selectSection('attendance', '/counter-attendance')">考勤打卡</li>
        <li :class="{ active: activeSection === 'leave' }" @click="selectSection('leave', '/counter-leave')">请假申请</li>
        <li :class="{ active: activeSection === 'leaveProgress' }" @click="selectSection('leaveProgress', '/counter-leave-progress')"><strong>我的请假记录</strong></li>
      </ul>
      <div class="logout" @click="logout">退出系统</div>
    </aside>

    <!-- 主体 -->
    <main class="main">
      <h3>我的请假记录</h3>

      <table v-if="leaveRecords.length" class="leave-table">
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
          <tr v-for="rec in leaveRecords" :key="rec.id">
            <td>{{ fmtDate(rec.startDate) }}</td>
            <td>{{ fmtDate(rec.endDate) }}</td>
            <td class="reason">{{ rec.reason }}</td>
            <td>
              <span :class="['badge', badgeClass(rec.status)]">{{ statusText(rec.status) }}</span>
            </td>
            <td>{{ rec.remark || '—' }}</td>
            <td>
              <button class="btn-detail" @click="showDetail(rec)">查看详情</button>
            </td>
          </tr>
        </tbody>
      </table>

      <p v-else class="empty">暂无请假记录</p>
    </main>

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
            <li><strong>起止：</strong>{{ fmtDate(cur.startDate) }} ～ {{ fmtDate(cur.endDate) }}</li>
            <li><strong>原因：</strong>{{ cur.reason }}</li>
            <li><strong>状态：</strong>{{ statusText(cur.status) }}</li>
            <li><strong>备注：</strong>{{ cur.remark || '—' }}</li>
          </ul>
        </div>
      </div>
    </transition>
  </div>
</template>

<script>
export default {
  name: 'CounterLeaveProgress',
  data() {
    return {
      activeSection: 'leaveProgress',
      activeSubsection: '',
      leaveRecords: [],
      // 详情弹窗相关
      detailVisible: false,
      cur: {},
      flowSteps: [
        { value: '待HR审批', label: '提交<br>待HR' },
        { value: 'HR审批通过待店长审批', label: 'HR通过<br>待店长' },
        { value: '审批成功', label: '审批成功' },
        { value: '已驳回', label: '已驳回' }
      ]
    };
  },
  computed: {
    currIndex() {
      return this.flowSteps.findIndex(s => s.value === this.cur.status);
    }
  },
  created() {
    this.fetchData();
  },
  methods: {
    async fetchData() {
      const id = localStorage.getItem('counterId');
      if (!id) {
        alert('未登录，请先登录');
        this.$router.push('/login');
        return;
      }
      try {
        const res = await fetch(`/api/leave/history/${id}?employeeType=counter`);
        const json = await res.json();
        if (json.status === 'success') {
          this.leaveRecords = json.data.records ?? [];
        } else {
          throw new Error(json.message);
        }
      } catch (e) {
        alert('加载失败：' + e.message);
      }
    },
    fmtDate(s) {
      return s ? new Date(s).toLocaleDateString() : '-';
    },
    statusText(s) {
      switch (s) {
        case '待HR审批':
          return '待HR审批';
        case 'HR审批通过待店长审批':
          return 'HR审批通过，待店长审批';
        case '审批成功':
          return '审批成功';
        case '已驳回':
          return '已驳回';
        default:
          return s;
      }
    },
    badgeClass(s) {
      if (s === '审批成功') return 'approved';
      if (s === '已驳回') return 'rejected';
      return 'pending';
    },
    showDetail(rec) {
      this.cur = rec;
      if (rec.status === '已驳回') {
        this.flowSteps = [
          { value: '待HR审批', label: '提交<br>待HR' },
          { value: 'HR审批通过待店长审批', label: 'HR通过<br>待店长' },
          { value: '已驳回', label: '已驳回', rejected: true }
        ];
      } else {
        this.flowSteps = [
          { value: '待HR审批', label: '提交<br>待HR' },
          { value: 'HR审批通过待店长审批', label: 'HR通过<br>待店长' },
          { value: '审批成功', label: '审批成功' }
        ];
      }
      this.detailVisible = true;
    },
    selectSection(section, path) {
      this.activeSection = section;
      this.activeSubsection = '';
      this.$router.push(path);
    },
    toggleSection(section) {
      this.activeSection = this.activeSection === section ? '' : section;
    },
    selectSubsection(subsection, path) {
      this.activeSubsection = subsection;
      this.activeSection = 'delivery';
      this.$router.push(path);
    },
    logout() {
      localStorage.removeItem('counterId');
      this.$router.push('/login');
    }
  }
};
</script>

<style scoped>
.page {
  display: flex;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  font-family: "Segoe UI", Tahoma, Geneva, Verdana, sans-serif;
  background: #fff;
}

.sidebar {
  width: 240px;
  background: #1d3557;
  color: #fff;
  display: flex;
  flex-direction: column;
  padding: 28px 20px;
}

.sidebar h2 {
  font-size: 22px;
  border-bottom: 2px solid #fff;
  padding-bottom: 10px;
  margin-bottom: 30px;
}

.sidebar ul {
  list-style: none;
  padding: 0;
  margin: 0;
  flex: 1;
}

.sidebar li {
  padding: 10px 0;
  cursor: pointer;
  user-select: none;
}

.sidebar li.active,
.sidebar strong.active {
  color: #00b4d8;
  font-weight: 700;
}

.logout {
  margin-top: auto;
  color: #ffb3b3;
  transition: 0.3s;
  cursor: pointer;
  user-select: none;
}

.logout:hover {
  color: #fff;
  font-weight: 700;
}

.main {
  flex: 1;
  padding: 40px 60px 50px 60px;
  overflow: auto;
  background: #fff;
}

.main h3 {
  font-size: 26px;
  text-align: center;
  margin-bottom: 32px;
}

.leave-table {
  width: 100%;
  border-collapse: collapse;
  background: #fff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  border-radius: 8px;
  overflow: hidden;
}

.leave-table th,
.leave-table td {
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
  text-align: center;
  font-size: 15px;
}

.leave-table th {
  background: #007bff;
  color: #fff;
}

.leave-table .reason {
  max-width: 260px;
  word-break: break-word;
}

.badge {
  padding: 4px 12px;
  border-radius: 16px;
  color: #fff;
  font-size: 12px;
  font-weight: 600;
}

.badge.pending {
  background: #f39c12;
}

.badge.approved {
  background: #27ae60;
}

.badge.rejected {
  background: #c0392b;
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

.empty {
  text-align: center;
  color: #888;
  font-size: 17px;
  margin-top: 40px;
}

/* 弹窗 */
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
