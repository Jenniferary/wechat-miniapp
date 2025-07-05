<template>
  <div class="container">
    <div class="order-form">
      <h2>预定餐桌</h2>
      <div class="table-images">
        <img src="/img/table.jpg" alt="普通餐桌" class="table-image" />
        <img src="/img/boxes.jpg" alt="包厢" class="table-image" />
      </div>

      <form @submit.prevent="submitReservation">
        <label class="table-type-label">请选择餐桌类型：</label>
        <div class="table-type-input">
          <input type="radio" value="normal" v-model="form.tableType" /> 普通桌位
          <input type="radio" value="private" v-model="form.tableType" /> 包厢
        </div>

        <label class="table-size-label" for="numOfPeople">用餐人数：</label>
        <input
          type="number"
          id="numOfPeople"
          v-model="form.numOfPeople"
          class="table-size-input"
          required
        /><br />

        <label class="table-size-label" for="reservationTime">用餐时间：</label>
        <input
          type="datetime-local"
          id="reservationTime"
          v-model="form.reservationTime"
          :min="minDateTime"
          class="table-size-input"
          required
        /><br />

        <button type="submit">确认预定</button>
      </form>

      <!-- 成功或错误消息 -->
      <p v-if="successMessage" class="success-message">{{ successMessage }}</p>
      <p v-if="errorMessage" class="error-message">{{ errorMessage }}</p>

      <!-- 可选跳转按钮 -->
      <router-link v-if="successMessage" to="/menu" class="redirect-button">
        返回菜单
      </router-link>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'ReservationPage',
  data() {
    return {
      form: {
        tableType: 'normal',
        numOfPeople: '',
        reservationTime: '',
        username: localStorage.getItem('username') || ''
      },
      minDateTime: '',
      successMessage: '',
      errorMessage: ''
    }
  },
  mounted() {
    const now = new Date()
    const iso = now.toISOString().slice(0, 16)
    this.minDateTime = iso
  },
  methods: {
    async submitReservation() {
      this.successMessage = ''
      this.errorMessage = ''

      try {
        const res = await axios.post('http://localhost:8080/api/reserve', this.form)
        if (res.data.status === 'success') {
          this.successMessage = `预定成功！您的桌号为：${res.data.tableNumber}`
        } else {
          this.errorMessage = res.data.message
        }
      } catch (err) {
        this.errorMessage = '请求失败，请稍后重试'
      }
    }
  }
}
</script>

<style scoped>
.container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;

  background-size: cover;
  background-attachment: fixed;
  margin: 0;
  padding: 0;
  font-family: Arial, sans-serif;
}

.order-form {
  background-color: rgba(255, 255, 255, 0.8);
  padding: 20px;
  border-radius: 10px;
  text-align: center;
  width: 600px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
}

.table-images {
  display: flex;
  justify-content: center;
  margin-bottom: 20px;
}

.table-image {
  width: 40%;
  margin: 0 10px;
  border: 2px solid #ccc;
  border-radius: 10px;
}

.table-type-label,
.table-size-label {
  font-weight: bold;
  margin-top: 15px;
  display: block;
}

.table-type-input,
.table-size-input {
  margin-bottom: 20px;
  width: 300px;
  padding: 8px;
}

button {
  padding: 10px 20px;
  font-size: 18px;
  background-color: #3498db;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
}

button:hover {
  background-color: #2980b9;
}

.success-message {
  color: green;
  font-size: 18px;
  margin-top: 20px;
}

.error-message {
  color: red;
  font-size: 18px;
  margin-top: 20px;
}

.redirect-button {
  margin-top: 15px;
  display: inline-block;
  background-color: #27ae60;
  padding: 10px 20px;
  color: white;
  border-radius: 5px;
  text-decoration: none;
}
</style>
