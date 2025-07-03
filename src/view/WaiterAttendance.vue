<template>
    <div class="attendance-page">
      <div class="sidebar">
        <h2>⏰ 考勤打卡</h2>
        <ul>
          <li @click="$router.push('/waiter-dashboard')">个人档案</li>
          <li><strong>考勤打卡</strong></li>
          <li @click="$router.push('/waiter-leave')">请假申请</li>
          <li @click="$router.push('/waiter-leave-progress')">我的请假记录</li>
          <li @click="logout" class="logout">退出系统</li>
        </ul>
      </div>
  
      <div class="map-section">
      <h3>{{ branchName }} - 门店定位</h3>
      <div id="map" ref="mapContainer"></div>

      <div class="btn-group">
        <button 
          @click="startCheckIn" 
          :disabled="checkingIn || !canCheckIn" 
          class="checkin-btn"
        >
          {{ checkingIn && checkType === 'in' ? "签到中..." : "开始签到" }}
        </button>

        <button 
          @click="startCheckOut" 
          :disabled="checkingOut || !canCheckOut" 
          class="checkout-btn"
        >
          {{ checkingOut && checkType === 'out' ? "签退中..." : "开始签退" }}
        </button>
      </div>

      <p v-if="message" class="message">{{ message }}</p>

      <div class="history-section">
        <h3>入职日期：{{ formatDateDisplay(hireDate) }}</h3>

        <!-- 新增日历切换按钮 -->
        <div class="calendar-toggle-btns" style="margin-bottom: 20px;">
          <button
            :class="{ active: showCalendar === 'checkin' }"
            @click="showCalendar = 'checkin'"
          >
            查看签到记录
          </button>
          <button
            :class="{ active: showCalendar === 'checkout' }"
            @click="showCalendar = 'checkout'"
          >
            查看签退记录
          </button>
        </div>

        <!-- 签到日历 -->
        <div v-if="showCalendar === 'checkin'" class="calendar">
          <h3>签到记录</h3>
          <div class="calendar-header">
            <button @click="prevMonth">上一月</button>
            <span>{{ currentYear }}年{{ currentMonth + 1 }}月</span>
            <button @click="nextMonth">下一月</button>
          </div>
          <div class="calendar-grid">
            <div class="calendar-weekday" v-for="wd in weekDays" :key="'checkin-wd-'+wd">{{ wd }}</div>
            <div
              v-for="day in calendarCheckInDays"
              :key="'checkin-day-'+day.date.toISOString()"
              :class="['calendar-day', { 'no-current-month': !day.currentMonth }]"
            >
              <template v-if="day.currentMonth">
                <span class="day-number">{{ day.day }}</span>
                <span
                  class="check-icon"
                  v-if="!day.beforeHire && !day.isFuture"
                  :class="day.checkedIn ? 'checked' : 'unchecked'"
                >
                  {{ day.checkedIn ? '✔' : '✘' }}
                </span>
              </template>
              <template v-else>
                <span class="day-number no-current-month">{{ day.day }}</span>
              </template>
            </div>
          </div>
        </div>

        <!-- 签退日历 -->
        <div v-if="showCalendar === 'checkout'" class="calendar" style="margin-top: 40px;">
          <h3>签退记录</h3>
          <div class="calendar-header">
            <button @click="prevMonth">上一月</button>
            <span>{{ currentYear }}年{{ currentMonth + 1 }}月</span>
            <button @click="nextMonth">下一月</button>
          </div>
          <div class="calendar-grid">
            <div class="calendar-weekday" v-for="wd in weekDays" :key="'checkout-wd-'+wd">{{ wd }}</div>
            <div
              v-for="day in calendarCheckOutDays"
              :key="'checkout-day-'+day.date.toISOString()"
              :class="['calendar-day', { 'no-current-month': !day.currentMonth }]"
            >
              <template v-if="day.currentMonth">
                <span class="day-number">{{ day.day }}</span>
                <span
                  class="check-icon"
                  v-if="!day.beforeHire && !day.isFuture"
                  :class="day.checkedIn ? 'checked' : 'unchecked'"
                >
                  {{ day.checkedIn ? '✔' : '✘' }}
                </span>
              </template>
              <template v-else>
                <span class="day-number no-current-month">{{ day.day }}</span>
              </template>
            </div>
          </div>
        </div>

      </div>
    </div>
  </div>
</template>

<script>
/* global AMap */
export default {
  name: "WaiterAttendance",
  data() {
    return {
      waiterId: null,
      branchId: null,
      branchName: "",
      branchLatLng: null,
      map: null,
      circle: null,
      marker: null,
      checkingIn: false,
      checkingOut: false,
      message: "",
      amapLoaded: false,
      checkType: "", // 'in' or 'out'

      hireDate: null,
      attendanceRecords: [],

      currentYear: 0,
      currentMonth: 0,
      weekDays: ["日", "一", "二", "三", "四", "五", "六"],

      showCalendar: "checkin", // 新增控制显示哪个日历
    };
  },

  async mounted() {
    await this.initData();
    await this.loadAmapScript();
    this.initMap();

    const now = new Date();
    this.currentYear = now.getFullYear();
    this.currentMonth = now.getMonth();

    if (this.waiterId) {
      await this.loadHireDateAndAttendance();
    }
  },

  computed: {
    // 计算签到日历天数
    calendarCheckInDays() {
      return this.generateCalendarDays("checkInTime");
    },

    // 计算签退日历天数
    calendarCheckOutDays() {
      return this.generateCalendarDays("checkOutTime");
    },

    // 是否允许签到（7:00-8:00）
    canCheckIn() {
      const now = new Date();
      const h = now.getHours();
      return h >= 7 && h < 8;
    },

    // 是否允许签退（21:00-21:30）
    canCheckOut() {
      const now = new Date();
      const h = now.getHours();
      const m = now.getMinutes();
      return h === 21 && m >= 0 && m <= 30;
    },
  },

  methods: {
    generateCalendarDays(timeField) {
      const days = [];
      const firstDayOfMonth = new Date(this.currentYear, this.currentMonth, 1);
      const lastDayOfMonth = new Date(this.currentYear, this.currentMonth + 1, 0);
      const startWeekDay = firstDayOfMonth.getDay();
      const prevMonthLastDay = new Date(this.currentYear, this.currentMonth, 0).getDate();

      const hireDateStr = this.hireDate ? this.formatDate(this.hireDate) : null;
      const todayStr = this.formatDate(new Date());

      // 上月尾部补齐
      for (let i = startWeekDay - 1; i >= 0; i--) {
        days.push({
          day: prevMonthLastDay - i,
          currentMonth: false,
          date: new Date(this.currentYear, this.currentMonth - 1, prevMonthLastDay - i),
          checkedIn: false,
          beforeHire: true,
          isFuture: false,
        });
      }

      // 本月日期
      for (let d = 1; d <= lastDayOfMonth.getDate(); d++) {
        const dateObj = new Date(this.currentYear, this.currentMonth, d);
        const dateStr = this.formatDate(dateObj);
        const beforeHire = hireDateStr && dateStr < hireDateStr;
        const isFuture = dateStr > todayStr;

        const showStatus = !beforeHire && !isFuture;

        const checked =
          showStatus &&
          this.attendanceRecords.some((rec) => rec[timeField] && rec[timeField].startsWith(dateStr));

        days.push({
          day: d,
          currentMonth: true,
          date: dateObj,
          checkedIn: checked,
          beforeHire,
          isFuture,
        });
      }

      // 下月头部补齐
      const totalCells = 42;
      const nextDaysCount = totalCells - days.length;
      for (let i = 1; i <= nextDaysCount; i++) {
        days.push({
          day: i,
          currentMonth: false,
          date: new Date(this.currentYear, this.currentMonth + 1, i),
          checkedIn: false,
          beforeHire: true,
          isFuture: false,
        });
      }

      return days;
    },

    async initData() {
      this.waiterId = localStorage.getItem("waiterId");
      if (!this.waiterId) {
        alert("未登录，请先登录");
        this.$router.push("/login");
        return;
      }

      try {
        const res = await fetch(`/api/waiter/${this.waiterId}`);
        const json = await res.json();
        if (json.status === "success") {
          this.branchId = json.data.branchId;
          await this.loadBranchInfo();
          this.hireDate = new Date(json.data.hireDate);
        } else {
          alert("获取厨师信息失败：" + (json.message || ""));
        }
      } catch (e) {
        alert("请求异常：" + e.message);
      }
    },

    async loadBranchInfo() {
      try {
        const res = await fetch(`/api/restaurant/branches/${this.branchId}`);
        const json = await res.json();
        if (json.status === "success") {
          this.branchName = json.data.name;
          this.branchLatLng = {
            lat: Number(json.data.latitude),
            lng: Number(json.data.longitude),
          };
        } else {
          alert("获取门店信息失败：" + (json.message || ""));
        }
      } catch (e) {
        alert("请求异常：" + e.message);
      }
    },

    async loadHireDateAndAttendance() {
      try {
        const res = await fetch(`/api/attendance/history/${this.waiterId}?employeeType=waiter`);
        const json = await res.json();
        if (json.status === "success") {
          this.attendanceRecords = json.data.records || [];
        } else {
          alert("获取历史打卡失败：" + (json.message || ""));
        }
      } catch (e) {
        alert("请求异常：" + e.message);
      }
    },

    loadAmapScript() {
      return new Promise((resolve, reject) => {
        if (window.AMap) {
          this.amapLoaded = true;
          resolve();
          return;
        }
        const script = document.createElement("script");
        script.src =
          "https://webapi.amap.com/maps?v=2.0&key=458bca7b69c738e295644538a1ed4faf&callback=amapInitCallback";
        script.async = true;
        window.amapInitCallback = () => {
          this.amapLoaded = true;
          resolve();
        };
        script.onerror = () => reject(new Error("高德地图SDK加载失败"));
        document.head.appendChild(script);
      });
    },

    initMap() {
      if (!this.amapLoaded) {
        this.message = "高德地图SDK未加载，无法初始化地图";
        return;
      }
      if (!this.branchLatLng) {
        this.message = "门店经纬度信息缺失，无法加载地图";
        return;
      }
      this.map = new AMap.Map(this.$refs.mapContainer, {
        center: [this.branchLatLng.lng, this.branchLatLng.lat],
        zoom: 16,
      });

      this.circle = new AMap.Circle({
        center: [this.branchLatLng.lng, this.branchLatLng.lat],
        radius: 800,
        strokeColor: "#1E90FF",
        strokeWeight: 2,
        fillColor: "#ADD8E6",
        fillOpacity: 0.3,
      });
      this.circle.setMap(this.map);
    },

    getBeijingTimeString() {
      const now = new Date();
      const utc = now.getTime() + now.getTimezoneOffset() * 60000;
      const beijingDate = new Date(utc + 8 * 3600000);
      const yyyy = beijingDate.getFullYear();
      const MM = String(beijingDate.getMonth() + 1).padStart(2, "0");
      const dd = String(beijingDate.getDate()).padStart(2, "0");
      const hh = String(beijingDate.getHours()).padStart(2, "0");
      const mm = String(beijingDate.getMinutes()).padStart(2, "0");
      const ss = String(beijingDate.getSeconds()).padStart(2, "0");
      return `${yyyy}-${MM}-${dd}T${hh}:${mm}:${ss}`;
    },

    async startCheckIn() {
  if (!navigator.geolocation) {
    this.message = "浏览器不支持定位功能";
    return;
  }

  if (!this.canCheckIn) {
    this.message = "❌ 当前时间不在允许签到时间段（07:00-08:00）";
    return;
  }

  this.checkingIn = true;
  this.checkType = "in";
  this.message = "正在获取当前位置...";

  navigator.geolocation.getCurrentPosition(
    async (pos) => {
      if (!this.amapLoaded || !window.AMap) {
        this.message = "高德地图SDK未加载，无法显示当前位置";
        this.checkingIn = false;
        this.checkType = "";
        return;
      }

      const userLngLat = [pos.coords.longitude, pos.coords.latitude];

      if (this.marker) {
        this.marker.setMap(null);
      }
      this.marker = new AMap.Marker({
        position: userLngLat,
        map: this.map,
        title: "您的位置",
      });

      this.map.panTo(userLngLat);

      const distance = this.getDistance(
        pos.coords.latitude,
        pos.coords.longitude,
        this.branchLatLng.lat,
        this.branchLatLng.lng
      );

      if (distance > 800) {
        this.message = `❌ 签到失败，您距离门店太远（${distance.toFixed(1)} 米），请靠近门店范围内签到`;
        this.checkingIn = false;
        this.checkType = "";
        return;
      }

      const waiterId = localStorage.getItem("waiterId");
      if (!waiterId) {
        this.message = "未登录，无法签到";
        this.checkingIn = false;
        this.checkType = "";
        return;
      }

      const checkInData = {
        employeeId: parseInt(waiterId),
        employeeType: "waiter",
        branchId: this.branchId,
        checkInTime: this.getBeijingTimeString(),
        checkInLat: pos.coords.latitude,
        checkInLng: pos.coords.longitude,
        checkInStatus: "normal",
      };

      try {
        const res = await fetch("/api/attendance/checkin", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(checkInData),
        });

        const json = await res.json();
        if (json.status === "success") {
          this.message = `✅ 签到成功，您距离门店 ${distance.toFixed(1)} 米`;
          await this.loadHireDateAndAttendance();
        } else {
          this.message = `❌ 签到失败：${json.message || "未知错误"}`;
        }
      } catch (err) {
        this.message = "❌ 签到请求失败：" + err.message;
      }

      this.checkingIn = false;
      this.checkType = "";
    },
    (err) => {
      this.message = "定位失败：" + err.message;
      this.checkingIn = false;
      this.checkType = "";
    },
    { enableHighAccuracy: true, timeout: 10000, maximumAge: 0 }
  );
},

async startCheckOut() {
  if (!navigator.geolocation) {
    this.message = "浏览器不支持定位功能";
    return;
  }

  if (!this.canCheckOut) {
    this.message = "❌ 当前时间不在允许签退时间段（21:00-21:30）";
    return;
  }

  this.checkingOut = true;
  this.checkType = "out";
  this.message = "正在获取当前位置...";

  navigator.geolocation.getCurrentPosition(
    async (pos) => {
      if (!this.amapLoaded || !window.AMap) {
        this.message = "高德地图SDK未加载，无法显示当前位置";
        this.checkingOut = false;
        this.checkType = "";
        return;
      }

      const userLngLat = [pos.coords.longitude, pos.coords.latitude];

      if (this.marker) {
        this.marker.setMap(null);
      }
      this.marker = new AMap.Marker({
        position: userLngLat,
        map: this.map,
        title: "您的位置",
      });

      this.map.panTo(userLngLat);

      const distance = this.getDistance(
        pos.coords.latitude,
        pos.coords.longitude,
        this.branchLatLng.lat,
        this.branchLatLng.lng
      );

      if (distance > 800) {
        this.message = `❌ 签退失败，您距离门店太远（${distance.toFixed(1)} 米），请靠近门店范围内签退`;
        this.checkingOut = false;
        this.checkType = "";
        return;
      }

      const waiterId = localStorage.getItem("waiterId");
      if (!waiterId) {
        this.message = "未登录，无法签退";
        this.checkingOut = false;
        this.checkType = "";
        return;
      }

      const checkOutData = {
        employeeId: parseInt(waiterId),
        employeeType: "waiter",
        branchId: this.branchId,
        checkOutTime: this.getBeijingTimeString(),
        checkOutLat: pos.coords.latitude,
        checkOutLng: pos.coords.longitude,
        checkOutStatus: "normal",
      };

      try {
        const res = await fetch("/api/attendance/checkout", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(checkOutData),
        });

        const json = await res.json();
        if (json.status === "success") {
          this.message = `✅ 签退成功，您距离门店 ${distance.toFixed(1)} 米`;
          await this.loadHireDateAndAttendance();
        } else {
          this.message = `❌ 签退失败：${json.message || "未知错误"}`;
        }
      } catch (err) {
        this.message = "❌ 签退请求失败：" + err.message;
      }

      this.checkingOut = false;
      this.checkType = "";
    },
    (err) => {
      this.message = "定位失败：" + err.message;
      this.checkingOut = false;
      this.checkType = "";
    },
    { enableHighAccuracy: true, timeout: 10000, maximumAge: 0 }
  );
},


    getDistance(lat1, lng1, lat2, lng2) {
      function toRad(d) {
        return (d * Math.PI) / 180;
      }
      const R = 6371000;
      const dLat = toRad(lat2 - lat1);
      const dLng = toRad(lng2 - lng1);
      const a =
        Math.sin(dLat / 2) * Math.sin(dLat / 2) +
        Math.cos(toRad(lat1)) *
          Math.cos(toRad(lat2)) *
          Math.sin(dLng / 2) *
          Math.sin(dLng / 2);
      const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
      return R * c;
    },

    prevMonth() {
      if (this.currentMonth === 0) {
        this.currentMonth = 11;
        this.currentYear--;
      } else {
        this.currentMonth--;
      }
    },

    nextMonth() {
      if (this.currentMonth === 11) {
        this.currentMonth = 0;
        this.currentYear++;
      } else {
        this.currentMonth++;
      }
    },

    formatDate(date) {
      const yyyy = date.getFullYear();
      const MM = String(date.getMonth() + 1).padStart(2, "0");
      const dd = String(date.getDate()).padStart(2, "0");
      return `${yyyy}-${MM}-${dd}`;
    },

    formatDateDisplay(date) {
      if (!date) return "";
      return date.toLocaleDateString();
    },

    logout() {
      localStorage.removeItem("waiterId");
      this.$router.push("/login");
    },
  },
};
</script>

<style scoped>
.attendance-page {
  display: flex;
  height: 100vh;
  width: 100vw;
  font-family: "Segoe UI", Tahoma, Geneva, Verdana, sans-serif;
  background: #f0f2f5;
}
.sidebar {
  width: 220px;
  background: #1d3557;
  color: #fff;
  padding: 30px 20px;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
}
.sidebar h2 {
  font-size: 24px;
  margin-bottom: 30px;
  border-bottom: 2px solid #fff;
  padding-bottom: 10px;
}
.sidebar ul {
  list-style: none;
  padding: 0;
  margin: 0;
  flex-grow: 1;
}
.sidebar li {
  padding: 12px 0;
  font-size: 16px;
  cursor: pointer;
  user-select: none;
}
.sidebar li:hover {
  background-color: #457b9d;
}
.sidebar .logout {
  margin-top: auto;
  color: #ffb3b3;
  transition: color 0.3s ease;
}
.sidebar .logout:hover {
  color: #fff;
  font-weight: bold;
}
.map-section {
  flex: 1;
  padding: 40px;
  background: white;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  align-items: center;
  overflow: auto;
}
.map-section h3 {
  margin-bottom: 20px;
  color: #333;
  font-weight: 600;
}
#map {
  width: 100%;
  max-width: 1100px;
  height: 600px;
  border-radius: 8px;
  box-shadow: 0 0 15px rgb(0 0 0 / 0.1);
  margin-bottom: 20px;
  flex-shrink: 0;
}
.btn-group {
  display: flex;
  gap: 20px;
  margin-bottom: 10px;
}
.checkin-btn,
.checkout-btn {
  background-color: #1d3557;
  border: none;
  color: white;
  font-size: 18px;
  padding: 12px 30px;
  border-radius: 6px;
  cursor: pointer;
  user-select: none;
  transition: background-color 0.3s ease;
}
.checkin-btn:disabled,
.checkout-btn:disabled {
  background-color: #777;
  cursor: not-allowed;
}
.checkin-btn:hover:not(:disabled),
.checkout-btn:hover:not(:disabled) {
  background-color: #457b9d;
}

.message {
  margin-top: 10px;
  color: #d9534f;
  font-weight: 600;
  min-height: 24px;
}

.history-section {
  margin-top: 30px;
  width: 100%;
  max-width: 1100px;
}

.calendar-toggle-btns button {
  padding: 8px 18px;
  margin-right: 15px;
  border: none;
  border-radius: 5px;
  background-color: #ccc;
  cursor: pointer;
  font-size: 16px;
  transition: background-color 0.3s ease;
  user-select: none;
}
.calendar-toggle-btns button.active,
.calendar-toggle-btns button:hover {
  background-color: #1d3557;
  color: white;
}

.calendar {
  user-select: none;
}

.calendar-header {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 10px;
  gap: 20px;
}
.calendar-header button {
  background-color: #1d3557;
  border: none;
  color: white;
  padding: 6px 14px;
  font-size: 14px;
  border-radius: 5px;
  cursor: pointer;
  user-select: none;
  transition: background-color 0.3s ease;
}
.calendar-header button:hover {
  background-color: #457b9d;
}

.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 6px;
  background-color: #f5f7fa;
  padding: 10px;
  border-radius: 8px;
}

.calendar-weekday {
  text-align: center;
  font-weight: 600;
  color: #666;
  padding: 8px 0;
}

.calendar-day {
  background: white;
  height: 40px;
  line-height: 40px;
  text-align: center;
  border-radius: 5px;
  position: relative;
  color: #333;
  font-weight: 500;
  user-select: none;
  box-shadow: 0 0 3px rgb(0 0 0 / 0.05);
}

.calendar-day.no-current-month {
  color: #bbb;
  background: #eee;
}

.day-number {
  display: inline-block;
  width: 100%;
}

.check-icon {
  position: absolute;
  right: 6px;
  top: 6px;
  font-size: 16px;
  font-weight: 700;
  user-select: none;
}
.check-icon.checked {
  color: green;
}
.check-icon.unchecked {
  color: red;
}
</style>
