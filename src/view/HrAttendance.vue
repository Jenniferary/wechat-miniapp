<template>
    <div class="attendance-page">
      <div class="sidebar">
        <h2>⏰ 考勤打卡</h2>
        <ul>
          <li @click="$router.push('/hr-dashboard')">入职待审批列表</li>
          <li @click="$router.push('/hr-profile')">个人档案</li>
          <li @click="$router.push('/hr-employee')">员工档案</li>
          <li><strong>考勤打卡</strong></li>
          <li @click="$router.push('/hr-leave')">请假申请</li>
          <li @click="$router.push('/hr-leave-progress')">我的请假记录</li>
          <li @click="$router.push('/hr-leave-review')">请假待审批</li>
          <li @click="$router.push('/hr-leavingworking-review')">离职待审批</li>
          <li @click="logout" class="logout">退出系统</li>
        </ul>
      </div>
  
      <div class="map-section">
        <h3>{{ branchName }} - 门店定位</h3>
        <div id="map" ref="mapContainer"></div>
        <button @click="startCheckIn" :disabled="checkingIn" class="checkin-btn">
          {{ checkingIn ? "打卡中..." : "开始打卡" }}
        </button>
        <p v-if="message" class="message">{{ message }}</p>
  
        <div v-if="hireDate" class="history-section">
          <h3>入职日期：{{ formatDateDisplay(hireDate) }}</h3>
          <h3>打卡记录</h3>
          <div class="calendar">
            <div class="calendar-header">
              <button @click="prevMonth">上一月</button>
              <span>{{ currentYear }}年{{ currentMonth + 1 }}月</span>
              <button @click="nextMonth">下一月</button>
            </div>
            <div class="calendar-grid">
              <div class="calendar-weekday" v-for="wd in weekDays" :key="wd">{{ wd }}</div>
              <div
                v-for="day in calendarDays"
                :key="day.date.toISOString()"
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
    name: "HrAttendance",
    data() {
      return {
        branchId: null,
        branchName: "",
        branchLatLng: null,
        map: null,
        circle: null,
        marker: null,
        checkingIn: false,
        message: "",
        amapLoaded: false,
  
        hrId: null,
        hireDate: null,
        attendanceRecords: [],
  
        currentYear: 0,
        currentMonth: 0,
        weekDays: ["日", "一", "二", "三", "四", "五", "六"],
      };
    },
  
    async mounted() {
      await this.initData();
      await this.loadAmapScript();
      this.initMap();
  
      const now = new Date();
      this.currentYear = now.getFullYear();
      this.currentMonth = now.getMonth();
  
      if (this.hrId) {
        await this.loadHireDateAndAttendance();
      }
    },
  
    computed: {
        calendarDays() {
  const days = [];
  const firstDayOfMonth = new Date(this.currentYear, this.currentMonth, 1);
  const lastDayOfMonth = new Date(this.currentYear, this.currentMonth + 1, 0);
  const startWeekDay = firstDayOfMonth.getDay();
  const prevMonthLastDay = new Date(this.currentYear, this.currentMonth, 0).getDate();

  const hireDateStr = this.hireDate ? this.formatDate(this.hireDate) : null;
  const todayStr = this.formatDate(new Date());

  // 上月末尾填充
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

  // 本月日期，判断是否入职前，是否未来日期
  for (let d = 1; d <= lastDayOfMonth.getDate(); d++) {
    const dateObj = new Date(this.currentYear, this.currentMonth, d);
    const dateStr = this.formatDate(dateObj);
    const beforeHire = hireDateStr && dateStr < hireDateStr;
    const isFuture = dateStr > todayStr;

    // 只有入职日及以前的日期且不未来才显示打卡状态
    const showStatus = !beforeHire && !isFuture;

    const checked =
      showStatus &&
      this.attendanceRecords.some((rec) => rec.checkInTime.startsWith(dateStr));

    days.push({
      day: d,
      currentMonth: true,
      date: dateObj,
      checkedIn: checked,
      beforeHire: beforeHire,
      isFuture: isFuture,
    });
  }

  // 下月头部填充，保证42格
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
}
    },
  
    methods: {
      async initData() {
        this.hrId = localStorage.getItem("hrId");
        if (!this.hrId) {
          alert("未登录，请先登录");
          this.$router.push("/hr-login");
          return;
        }
  
        try {
          const res = await fetch(`/api/hr/${this.hrId}`);
          const json = await res.json();
          if (json.status === "success") {
            this.branchId = json.data.branchId;
            await this.loadBranchInfo();
          } else {
            alert("获取HR信息失败：" + (json.message || ""));
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
          const res = await fetch(`/api/attendance/history/${this.hrId}?employeeType=hr`);
          const json = await res.json();
          if (json.status === "success") {
            this.hireDate = new Date(json.data.hireDate);
            this.attendanceRecords = json.data.records; // 例: [{checkInTime: "2025-06-27T07:45:00"}]
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
  
      formatDate(date) {
        const yyyy = date.getFullYear();
        const MM = String(date.getMonth() + 1).padStart(2, "0");
        const dd = String(date.getDate()).padStart(2, "0");
        return `${yyyy}-${MM}-${dd}`;
      },
  
      formatDateDisplay(date) {
        return date.toLocaleDateString();
      },
  
      isWithinCheckInTime() {
        const now = new Date();
        const utc = now.getTime() + now.getTimezoneOffset() * 60000;
        const beijingDate = new Date(utc + 8 * 3600000);
        const h = beijingDate.getHours();
        const m = beijingDate.getMinutes();
        return (
          (h === 7 && m >= 40) ||
          (h === 8 && m === 0) ||
          (h === 15 && m >= 40) ||
          (h === 16 && m === 0)
        );
      },
  
      async startCheckIn() {
        if (!navigator.geolocation) {
          this.message = "浏览器不支持定位功能";
          return;
        }
  
        if (!this.isWithinCheckInTime()) {
          this.message = "❌ 当前时间不在允许打卡时间段（07:40-08:00 或 15:40-16:00）";
          return;
        }
  
        this.checkingIn = true;
        this.message = "正在获取当前位置...";
  
        navigator.geolocation.getCurrentPosition(
          async (pos) => {
            if (!this.amapLoaded || !window.AMap) {
              this.message = "高德地图SDK未加载，无法显示当前位置";
              this.checkingIn = false;
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
              this.message = `❌ 打卡失败，您距离门店太远（${distance.toFixed(1)} 米），请靠近门店范围内打卡`;
              this.checkingIn = false;
              return;
            }
  
            const hrId = localStorage.getItem("hrId");
            if (!hrId) {
              this.message = "未登录，无法打卡";
              this.checkingIn = false;
              return;
            }
  
            const checkInData = {
              employeeId: parseInt(hrId),
              employeeType: "hr",
              branchId: this.branchId,
              checkInTime: this.getBeijingTimeString(),
              latitude: pos.coords.latitude,
              longitude: pos.coords.longitude,
              status: "normal",
            };
  
            try {
              const res = await fetch("/api/attendance/checkin", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(checkInData),
              });
  
              const json = await res.json();
              if (json.status === "success") {
                this.message = `✅ 打卡成功，您距离门店 ${distance.toFixed(1)} 米`;
                await this.loadHireDateAndAttendance();
              } else {
                this.message = `❌ 打卡失败：${json.message || "未知错误"}`;
              }
            } catch (err) {
              this.message = "❌ 打卡请求失败：" + err.message;
            }
  
            this.checkingIn = false;
          },
          (err) => {
            this.message = "定位失败：" + err.message;
            this.checkingIn = false;
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
  
      logout() {
        localStorage.removeItem("hrId");
        localStorage.removeItem("hrUsername");
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
    width: 240px;
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
  .checkin-btn {
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
  .checkin-btn:disabled {
    background-color: #777;
    cursor: not-allowed;
  }
  .checkin-btn:hover:not(:disabled) {
    background-color: #457b9d;
  }
  .message {
    color: #555;
    font-size: 16px;
    margin-top: 10px;
    min-height: 24px;
    text-align: center;
    max-width: 700px;
  }
  .history-section {
    margin-top: 30px;
    width: 100%;
    max-width: 700px;
  }
  .calendar {
    border: 1px solid #ccc;
    border-radius: 6px;
    padding: 10px;
  }
  .calendar-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 10px;
  }
  .calendar-grid {
    display: grid;
    grid-template-columns: repeat(7, 1fr);
    gap: 6px;
  }
  .calendar-weekday {
    font-weight: bold;
    text-align: center;
    color: #555;
  }
  .calendar-day {
    background: #f9f9f9;
    padding: 10px 6px;
    border-radius: 4px;
    text-align: center;
    user-select: none;
    position: relative;
    font-weight: 600;
    font-size: 16px;
  }
  .no-current-month {
    color: #bbb;
    background: transparent !important;
  }
  .day-number {
    display: block;
    margin-bottom: 4px;
  }
  .check-icon {
    font-weight: bold;
    font-size: 20px;
    user-select: none;
  }
  .check-icon.checked {
    color: green;
  }
  .check-icon.unchecked {
    color: red;
  }
  </style>
  