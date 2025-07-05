<template>
  <div class="headquarters-dashboard">
    <!-- 顶部导航栏 -->
    <header class="dashboard-header">
      <div class="header-left">
        <h1>🏢 食尚阁总部管理系统</h1>
        <span class="department-badge">{{ userDepartment }}</span>
      </div>
      <div class="header-right">
        <span class="welcome-text">欢迎，{{ username }}</span>
        <button @click="logout" class="logout-btn">退出登录</button>
      </div>
    </header>

    <!-- 主要内容区域 -->
    <main class="dashboard-main">
      <!-- 统计卡片 -->
      <section class="stats-section">
        <div class="stats-grid">
          <div class="stat-card">
            <div class="stat-icon">🏪</div>
            <div class="stat-content">
              <h3>门店总数</h3>
              <p class="stat-number">{{ stats.totalStores }}</p>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon">👥</div>
            <div class="stat-content">
              <h3>员工总数</h3>
              <p class="stat-number">{{ stats.totalEmployees }}</p>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon">💰</div>
            <div class="stat-content">
              <h3>月度营收</h3>
              <p class="stat-number">¥{{ stats.monthlyRevenue.toLocaleString() }}</p>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon">📈</div>
            <div class="stat-content">
              <h3>增长率</h3>
              <p class="stat-number">{{ stats.growthRate }}%</p>
            </div>
          </div>
        </div>
      </section>

      <!-- 功能模块 -->
      <section class="modules-section">
        <h2>管理模块</h2>
        <div class="modules-grid">
          <div class="module-card" @click="navigateTo('/store-management')">
            <div class="module-icon">🏪</div>
            <h3>门店管理</h3>
            <p>管理全国门店信息、运营状态</p>
          </div>
          <div class="module-card" @click="navigateTo('/employee-management')">
            <div class="module-icon">👥</div>
            <h3>员工管理</h3>
            <p>员工信息、薪资、考勤管理</p>
          </div>
          <div class="module-card" @click="navigateTo('/financial-management')">
            <div class="module-icon">💰</div>
            <h3>财务管理</h3>
            <p>财务报表、成本控制、预算管理</p>
          </div>
          <div class="module-card" @click="navigateTo('/data-analytics')">
            <div class="module-icon">📊</div>
            <h3>数据分析</h3>
            <p>销售数据、客户分析、市场趋势</p>
          </div>
          <div class="module-card" @click="navigateTo('/supply-chain')">
            <div class="module-icon">🚚</div>
            <h3>供应链管理</h3>
            <p>供应商管理、库存控制、采购</p>
          </div>
          <div class="module-card" @click="navigateTo('/franchise-management')">
            <div class="module-icon">🤝</div>
            <h3>加盟管理</h3>
            <p>加盟申请、合同管理、支持服务</p>
          </div>
          <div class="module-card" @click="navigateTo('/marketing-management')">
            <div class="module-icon">📢</div>
            <h3>营销管理</h3>
            <p>促销活动、品牌推广、客户关系</p>
          </div>
          <div class="module-card" @click="navigateTo('/system-settings')">
            <div class="module-icon">⚙️</div>
            <h3>系统设置</h3>
            <p>系统配置、权限管理、安全设置</p>
          </div>
        </div>
      </section>

      <!-- 最近活动 -->
      <section class="activity-section">
        <h2>最近活动</h2>
        <div class="activity-list">
          <div class="activity-item" v-for="activity in recentActivities" :key="activity.id">
            <div class="activity-icon">{{ activity.icon }}</div>
            <div class="activity-content">
              <h4>{{ activity.title }}</h4>
              <p>{{ activity.description }}</p>
              <span class="activity-time">{{ activity.time }}</span>
            </div>
          </div>
        </div>
      </section>
    </main>
  </div>
</template>

<script>
export default {
  name: 'HeadquartersDashboard',
  data() {
    return {
      username: '',
      userDepartment: '',
      stats: {
        totalStores: 156,
        totalEmployees: 2340,
        monthlyRevenue: 12580000,
        growthRate: 15.6
      },
      recentActivities: [
        {
          id: 1,
          icon: '🏪',
          title: '新门店开业',
          description: '上海浦东新区分店正式开业',
          time: '2小时前'
        },
        {
          id: 2,
          icon: '📊',
          title: '月度报表生成',
          description: '11月份财务报表已生成完成',
          time: '4小时前'
        },
        {
          id: 3,
          icon: '👥',
          title: '员工培训完成',
          description: '第三季度员工培训计划完成',
          time: '1天前'
        },
        {
          id: 4,
          icon: '🤝',
          title: '加盟申请审批',
          description: '3个新的加盟申请等待审批',
          time: '2天前'
        }
      ]
    }
  },
  mounted() {
    this.loadUserInfo();
  },
  methods: {
    loadUserInfo() {
      this.username = localStorage.getItem('username') || '总部用户';
      this.userDepartment = localStorage.getItem('department') || '未知部门';
    },
    navigateTo(path) {
      this.$router.push(path);
    },
    logout() {
      localStorage.removeItem('userRole');
      localStorage.removeItem('username');
      localStorage.removeItem('department');
      this.$router.push('/all-login');
    }
  }
}
</script>

<style scoped>
.headquarters-dashboard {
  min-height: 100vh;
  background-color: #f5f7fa;
  font-family: 'Arial', sans-serif;
}

.dashboard-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 20px 30px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.header-left h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
}

.department-badge {
  background: rgba(255, 255, 255, 0.2);
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  margin-left: 15px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.welcome-text {
  font-size: 16px;
}

.logout-btn {
  background: rgba(255, 255, 255, 0.2);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.3);
  padding: 8px 16px;
  border-radius: 20px;
  cursor: pointer;
  transition: background 0.3s ease;
}

.logout-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}

.dashboard-main {
  padding: 30px;
  max-width: 1400px;
  margin: 0 auto;
}

.stats-section {
  margin-bottom: 40px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
}

.stat-card {
  background: white;
  padding: 25px;
  border-radius: 15px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  gap: 20px;
  transition: transform 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-5px);
}

.stat-icon {
  font-size: 40px;
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
}

.stat-content h3 {
  margin: 0 0 10px 0;
  color: #666;
  font-size: 14px;
  font-weight: 500;
}

.stat-number {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
  color: #333;
}

.modules-section {
  margin-bottom: 40px;
}

.modules-section h2 {
  color: #333;
  margin-bottom: 25px;
  font-size: 24px;
}

.modules-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
}

.module-card {
  background: white;
  padding: 25px;
  border-radius: 15px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: all 0.3s ease;
  text-align: center;
}

.module-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.module-icon {
  font-size: 48px;
  margin-bottom: 15px;
}

.module-card h3 {
  color: #333;
  margin: 0 0 10px 0;
  font-size: 18px;
}

.module-card p {
  color: #666;
  margin: 0;
  font-size: 14px;
  line-height: 1.5;
}

.activity-section h2 {
  color: #333;
  margin-bottom: 25px;
  font-size: 24px;
}

.activity-list {
  background: white;
  border-radius: 15px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.activity-item {
  display: flex;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #f0f0f0;
  gap: 15px;
}

.activity-item:last-child {
  border-bottom: none;
}

.activity-icon {
  font-size: 24px;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8f9fa;
  border-radius: 50%;
}

.activity-content h4 {
  margin: 0 0 5px 0;
  color: #333;
  font-size: 16px;
}

.activity-content p {
  margin: 0 0 5px 0;
  color: #666;
  font-size: 14px;
}

.activity-time {
  color: #999;
  font-size: 12px;
}

@media (max-width: 768px) {
  .dashboard-header {
    flex-direction: column;
    gap: 15px;
    text-align: center;
  }
  
  .dashboard-main {
    padding: 20px 15px;
  }
  
  .stats-grid,
  .modules-grid {
    grid-template-columns: 1fr;
  }
}
</style>