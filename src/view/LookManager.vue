<template>
  <div class="container">
    <!-- Sidebar -->
    <div class="sidebar">
      <h3>前台管理系统</h3>
      <ul>
        <li><router-link to="/counter">🏠 回到管理主页</router-link></li>
        <li><router-link to="/manage-delivery">🛵 外卖员管理</router-link></li>
        <li><router-link to="/add-manager">👤➕ 添加管理员</router-link></li>
        <li><router-link to="/check-orders">💵 查看订单及结账</router-link></li>
        <li><router-link to="/distribute-coupons">🎟️ 分配优惠券</router-link></li>
        <li><router-link to="/manage-tables">🪑 餐桌预定</router-link></li>
      </ul>
    </div>

    <!-- Content -->
    <div class="content">
      <h2 class="page-title">🧐 管理员列表</h2>
      <div class="table-card">
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>账号</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="manager in managers" :key="manager.id">
              <td>{{ manager.id }}</td>
              <td>{{ manager.username }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import axios from 'axios'

export default {
  name: 'CheckManager',
  setup() {
    const managers = ref([])

    const fetchManagers = async () => {
      try {
        const response = await axios.get('http://localhost:8080/api/managers')
        const data = response.data

        if (data.status === 'success') {
          const list = []
          for (const key in data) {
            if (key.startsWith('data')) {
              list.push(data[key])
            }
          }
          managers.value = list
        } else {
          console.error('获取失败：', data.message)
        }
      } catch (error) {
        console.error('请求出错：', error)
      }
    }

    onMounted(fetchManagers)

    return {
      managers
    }
  }
}
</script>

<style scoped>
.container {
  display: flex;
  max-width: 1200px;
  margin: 40px auto;
  padding: 40px;
  background-color: #fff;
  border-radius: 10px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
}

.sidebar {
  background-color: rgba(255, 255, 255, 0.9);
  padding: 20px;
  width: 200px;
  border-radius: 10px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.05);
}

.sidebar h3 {
  margin-bottom: 10px;
  font-size: 20px;
  color: #2980b9;
  border-bottom: 2px solid #2980b9;
  padding-bottom: 10px;
}

.sidebar ul {
  list-style-type: none;
  padding: 0;
  margin: 0;
}

.sidebar ul li {
  margin-bottom: 10px;
}

.sidebar ul li a {
  text-decoration: none;
  color: #333;
  font-size: 16px;
  transition: all 0.3s ease;
}

.sidebar ul li a:hover {
  color: #2980b9;
  font-weight: bold;
}

.content {
  flex-grow: 1;
  padding: 2rem;
  width: 760px;
  background-color: #fafafa;
  margin-top: 50px;
}

.page-title {
  text-align: center;
  font-size: 1.8rem;
  color: #2980b9;
  margin-bottom: 1.5rem;
}

.table-card {
  max-width: 600px;
  margin: 0 auto;
  padding: 1.5rem;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 0 10px rgba(204, 204, 204, 0.5);
}

table {
  width: 100%;
  border-collapse: collapse;
  font-size: 16px;
}

th,
td {
  padding: 0.75rem;
  border-bottom: 1px solid #ddd;
  text-align: center;
}

th {
  background-color: #f5f5f5;
  font-weight: bold;
  color: #333;
}
</style>
