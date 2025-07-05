<template>
  <div class="resume-page">
    <!-- 侧边栏 -->
    <div class="sidebar">
      <h2>💁‍♀️ 前台管理系统</h2>
      <ul class="menu-list">
        <li
          :class="{ active: activeSection === 'profile' }"
          @click="selectSection('profile'); $router.push('/counter-dashboard')"
        >
          <strong>个人档案</strong>
        </li>
        <li
          :class="{ active: activeSection === 'dinein' }"
          @click="selectSection('dinein'); $router.push('/counter-dinein-order')"
        >
          管理堂食订单
        </li>
        <li
          :class="{ active: activeSection === 'tables' }"
          @click="selectSection('tables'); $router.push('/manage-tables')"
        >
          管理餐桌
        </li>

        <li>
          <strong
            @click="toggleSection('delivery')"
            :class="{ active: activeSection === 'delivery' }"
            style="margin-top: 20px; cursor: pointer; color: #fff; font-weight: bold;"
          >
            外卖管理
          </strong>
        </li>

        <li
          v-if="activeSection === 'delivery'"
          :class="{ active: activeSubsection === 'assign' }"
          @click="selectSubsection('assign'); $router.push('/delivery-assign')"
          style="padding-left: 15px; cursor: pointer;"
        >
          分配外卖员
        </li>
        <li
          v-if="activeSection === 'delivery'"
          :class="{ active: activeSubsection === 'add' }"
          @click="selectSubsection('add'); $router.push('/delivery-add')"
          style="padding-left: 15px; cursor: pointer;"
        >
          添加外卖员
        </li>
        <li
          v-if="activeSection === 'delivery'"
          :class="{ active: activeSubsection === 'view' }"
          @click="selectSubsection('view'); $router.push('/delivery-view')"
          style="padding-left: 15px; cursor: pointer;"
        >
          查看外卖订单
        </li>

        <li @click="$router.push('/counter-overtime-working')">申请加班</li>
          <li @click="$router.push('/counter-overtime-progress')">我的加班记录</li>
          <li @click="$router.push('/counter-leaving-working')">申请离职</li>
          <li @click="$router.push('/counter-leaving-status')">查看离职申请进度</li>
          <li @click="$router.push('/counter-salary')">工资管理</li>
          <li @click="$router.push('/counter-attendance')">考勤打卡</li>
          <li @click="$router.push('/counter-leave')">请假申请</li>
          <li @click="$router.push('/counter-leave-progress')">我的请假记录</li>
      </ul>

      <div class="logout" @click="logout">退出系统</div>
    </div>

    <!-- 主体内容 -->
    <div class="form-section">
      <h3>管理餐桌</h3>

      <div class="button-group">
        <button @click="showDistribute = true" :class="{ active: showDistribute }">分配餐桌</button>
        <button @click="showDistribute = false" :class="{ active: !showDistribute }">释放餐桌</button>
      </div>

      <!-- 分配餐桌表单 -->
      <div v-if="showDistribute" class="form-card">
        <div class="form-group">
          <label for="userId">用户 ID</label>
          <input v-model="userId" type="number" placeholder="请输入用户编号" required />
        </div>

        <div class="form-group">
          <label>选择桌型</label>
          <div class="radio-group">
            <label><input v-model="tableType" type="radio" value="normal" /> 普通桌位</label>
            <label><input v-model="tableType" type="radio" value="private" /> 包厢</label>
          </div>
        </div>

        <div class="form-group">
          <label for="numOfPeople">用餐人数</label>
          <input v-model="numOfPeople" type="number" placeholder="例如：4" required />
        </div>

        <div class="form-group">
          <label for="reservationTime">用餐时间</label>
          <input v-model="reservationTime" type="datetime-local" :min="minTime" required />
        </div>

        <button @click="submitReservation">确认分配</button>

        <div v-if="successMessage" class="success-message">{{ successMessage }}</div>
        <div v-if="errorMessage" class="error-message">{{ errorMessage }}</div>
      </div>

      <!-- 释放餐桌表单 -->
      <div v-else class="form-card">
        <div class="form-group">
          <label for="userId">用户 ID</label>
          <input v-model="userId" type="number" required />
        </div>

        <button @click="releaseTable">确认释放</button>

        <div v-if="successMessage" class="success-message">{{ successMessage }}</div>

        <div v-if="releasedTables.length" class="released-tables">
          <div>已释放的桌位号：</div>
          <ul>
            <li v-for="table in releasedTables" :key="table">{{ table }}</li>
          </ul>
        </div>

        <div v-if="errorMessage" class="error-message">{{ errorMessage }}</div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: "CounterManageTables",
  data() {
    return {
      activeSection: 'tables',
      activeSubsection: null,

      showDistribute: true,
      userId: '',
      tableType: 'normal',
      numOfPeople: '',
      reservationTime: '',
      minTime: '',
      successMessage: '',
      errorMessage: '',
      releasedTables: [],
    };
  },
  created() {
    const now = new Date();
    const offset = -now.getTimezoneOffset();
    const localISOTime = new Date(now.getTime() + offset * 60000).toISOString().slice(0, 16);
    this.minTime = localISOTime;
  },
  methods: {
    selectSection(section) {
      this.activeSection = section;
      this.activeSubsection = null;
    },
    toggleSection(section) {
      if (this.activeSection === section) {
        this.activeSection = null;
        this.activeSubsection = null;
      } else {
        this.activeSection = section;
        this.activeSubsection = null;
      }
    },
    selectSubsection(subsection) {
      this.activeSection = 'delivery';
      this.activeSubsection = subsection;
    },
    logout() {
      localStorage.removeItem("counterId");
      this.$router.push("/login");
    },
    async submitReservation() {
      this.successMessage = '';
      this.errorMessage = '';
      if (!this.userId || !this.numOfPeople || !this.reservationTime) {
        this.errorMessage = '请填写所有必填项';
        return;
      }
      try {
        const res = await axios.post('http://localhost:8080/api/table/distribute', {
          userId: this.userId,
          tableType: this.tableType,
          numOfPeople: this.numOfPeople,
          reservationTime: this.reservationTime,
        });
        if (res.data.success) {
          this.successMessage = `餐桌分配成功！分配给用户ID：${this.userId}，桌号为：${res.data.tableNumber}`;
          setTimeout(() => {
            this.$router.push('/manage-tables');
          }, 1000);
        } else {
          this.errorMessage = res.data.message || '分配失败';
        }
      } catch (err) {
        this.errorMessage = '请求失败：' + err.message;
      }
    },
    async releaseTable() {
      this.successMessage = '';
      this.errorMessage = '';
      this.releasedTables = [];
      if (!this.userId) {
        this.errorMessage = '请输入用户ID';
        return;
      }
      try {
        const response = await axios.post('http://localhost:8080/api/table/relieve', {
          userId: this.userId,
        });
        if (response.data.success) {
          this.successMessage = `餐桌释放成功！释放用户ID：${this.userId}`;
          this.releasedTables = response.data.releasedTables || [];
          setTimeout(() => {
            this.$router.push('/manage-tables');
          }, 100);
        } else {
          this.errorMessage = response.data.message || '释放失败';
        }
      } catch (err) {
        this.errorMessage = '请求失败：' + err.message;
      }
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
    box-sizing: border-box;
    display: flex;
    flex-direction: column;
    height: 100vh;
  }
  .sidebar h2 {
    margin-bottom: 30px;
    font-size: 22px;
    border-bottom: 2px solid #fff;
    padding-bottom: 10px;
  }
  .menu-list {
    flex: 1;
    list-style: none;
    padding-left: 0;
    margin: 0;
    overflow-y: auto;
  }
  .menu-list li {
    padding: 10px 0;
    font-size: 15px;
    cursor: pointer;
    color: #ccc;
    user-select: none;
  }
  .menu-list li.active {
    color: #00b4d8;
    font-weight: bold;
  }
  .menu-list strong.active {
    color: #00b4d8;
  }

.logout {
  color: #ffb3b3;
  transition: color 0.3s ease;
  margin-top: auto;
}

.logout:hover {
  color: #ffffff;
  font-weight: bold;
}

.form-section {
  flex: 1;
  background: #ffffff;
  padding: 50px 60px;
  box-sizing: border-box;
  overflow-y: auto;
  border-radius: 0 20px 20px 0;
  box-shadow: -8px 0 30px rgba(0, 0, 0, 0.08);
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  color: #2c3e50;
}

.form-section h3 {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 36px;
  border-left: 6px solid #007bff;
  padding-left: 18px;
  color: #34495e;
  letter-spacing: 0.05em;
}

/* 按钮组 */
.button-group {
  display: flex;
  gap: 20px;
  margin-bottom: 36px;
}

.button-group button {
  flex: 1;
  padding: 16px 0;
  background: linear-gradient(135deg, #4a90e2, #357ABD);
  color: white;
  font-size: 18px;
  font-weight: 700;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  box-shadow: 0 6px 12px rgba(53, 122, 189, 0.5);
  transition: background 0.3s ease, box-shadow 0.3s ease;
  user-select: none;
}
.button-group button.active,
.button-group button:hover {
  background: linear-gradient(135deg, #2a5ea8, #1f4a7a);
  box-shadow: 0 8px 18px rgba(31, 74, 122, 0.8);
}

/* 表单卡片 */
.form-card {
  max-width: 620px;
  background: #f9fbff;
  padding: 36px 48px;
  border-radius: 20px;
  box-shadow: 0 16px 40px rgba(50, 79, 133, 0.12);
  margin: 0 auto;
  transition: box-shadow 0.3s ease;
}
.form-card:hover {
  box-shadow: 0 20px 60px rgba(50, 79, 133, 0.18);
}

/* 表单组 */
.form-group {
  margin-bottom: 28px;
}

.form-group label {
  display: block;
  font-weight: 700;
  margin-bottom: 12px;
  color: #34495e;
  font-size: 16px;
  letter-spacing: 0.03em;
}

.form-group input[type="number"],
.form-group input[type="datetime-local"] {
  width: 100%;
  padding: 14px 18px;
  font-size: 16px;
  border: 2px solid #d1d8e0;
  border-radius: 12px;
  box-shadow: inset 0 2px 6px #e3ebf8;
  outline-offset: 0;
  transition: border-color 0.3s ease, box-shadow 0.3s ease;
  font-weight: 500;
  color: #2c3e50;
}

.form-group input[type="number"]:focus,
.form-group input[type="datetime-local"]:focus {
  border-color: #4a90e2;
  box-shadow: 0 0 10px #4a90e2;
}

/* 单选按钮组 */
.radio-group {
  display: flex;
  gap: 36px;
  font-weight: 600;
  color: #34495e;
  user-select: none;
}
.radio-group label {
  cursor: pointer;
  font-size: 16px;
}
.radio-group input[type="radio"] {
  margin-right: 10px;
  cursor: pointer;
  accent-color: #357abd;
}

/* 确认按钮 */
button {
  background: linear-gradient(135deg, #357ABD, #4a90e2);
  border: none;
  color: white;
  padding: 16px 0;
  width: 100%;
  font-size: 18px;
  font-weight: 700;
  border-radius: 16px;
  cursor: pointer;
  box-shadow: 0 10px 22px rgba(53, 122, 189, 0.5);
  transition: background 0.3s ease, box-shadow 0.3s ease;
  user-select: none;
  margin-top: 10px;
}
button:hover {
  background: linear-gradient(135deg, #1f4a7a, #2a5ea8);
  box-shadow: 0 14px 30px rgba(31, 74, 122, 0.75);
}

/* 成功和错误提示 */
.success-message {
  background-color: #27ae60;
  color: white;
  padding: 16px;
  border-radius: 16px;
  margin-top: 28px;
  text-align: center;
  font-weight: 700;
  box-shadow: 0 6px 20px rgba(39, 174, 96, 0.7);
  user-select: none;
}

.error-message {
  background-color: #e74c3c;
  color: white;
  padding: 16px;
  border-radius: 16px;
  margin-top: 28px;
  text-align: center;
  font-weight: 700;
  box-shadow: 0 6px 20px rgba(231, 76, 60, 0.7);
  user-select: none;
}

/* 释放桌位列表 */
.released-tables {
  margin-top: 28px;
  color: #34495e;
  font-weight: 600;
  font-size: 18px;
  user-select: none;
}
.released-tables ul {
  padding-left: 20px;
  list-style-type: disc;
  margin-top: 10px;
}
</style>
