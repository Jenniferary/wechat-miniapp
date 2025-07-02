<template>
    <div class="resume-page">
      <div class="sidebar">
        <h2>💁‍♀️ 前台管理系统</h2>
        <ul class="menu-list">
        <li
        :class="{ active: activeSection === 'profile' }"
        @click="selectSection('profile', '/counter-dashboard')"
        >
        个人档案
        </li>

        <li
        :class="{ active: activeSection === 'dinein' }"
        @click="selectSection('dinein', '/counter-dinein-order')"
        >
        管理堂食订单
        </li>

        <li
        :class="{ active: activeSection === 'tables' }"
        @click="selectSection('tables', '/manage-tables')"
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
        @click="selectSubsection('assign', '/delivery-assign')"
        style="padding-left: 15px; cursor: pointer;"
        >
        分配外卖员
        </li>
        <li
        v-if="activeSection === 'delivery'"
        :class="{ active: activeSubsection === 'add' }"
        @click="selectSubsection('add', '/delivery-add')"
        style="padding-left: 15px; cursor: pointer;"
        >
        添加外卖员
    </li>

  <li
    v-if="activeSection === 'delivery'"
    :class="{ active: activeSubsection === 'view' }"
    @click="selectSubsection('view', '/delivery-view')"
    style="padding-left: 15px; cursor: pointer;"
  >
    查看外卖订单
  </li>

  <li
    :class="{ active: activeSection === 'attendance' }"
    @click="selectSection('attendance', '/counter-attendance')"
  >
    考勤打卡
  </li>

  <li
    :class="{ active: activeSection === 'leave' }"
    @click="selectSection('leave', '/counter-leave')"
  >
    请假申请
  </li>

  <li
    :class="{ active: activeSection === 'leaveProgress' }"
    @click="selectSection('leaveProgress', '/counter-leave-progress')"
  >
    我的请假记录
  </li>
</ul>
  
        <div class="logout" @click="logout">退出系统</div>
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
    name: "CounterAttendance",
    data() {
      return {
        counterId: null,
        branchId: null,
        branchName: "",
        branchLatLng: null,
        map: null,
        circle: null,
        marker: null,
        checkingIn: false,
        message: "",
        amapLoaded: false,
        activeSection: "",
        activeSubsection: null,
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
  
      if (this.counterId) {
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
        selectSection(section, routePath) {
    this.activeSection = section;
    this.activeSubsection = null;
    if (routePath) {
      this.$router.push(routePath);
    }
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

  selectSubsection(subsection, routePath) {
    this.activeSection = 'delivery';
    this.activeSubsection = subsection;
    if (routePath) {
      this.$router.push(routePath);
    }
  },
      async initData() {
        this.counterId = localStorage.getItem("counterId");
        if (!this.counterId) {
          alert("未登录，请先登录");
          this.$router.push("/login");
          return;
        }
  
        try {
          const res = await fetch(`/api/counter/${this.counterId}`);
          const json = await res.json();
          if (json.status === "success") {
            this.branchId = json.data.branchId;
            await this.loadBranchInfo();
            this.hireDate = new Date(json.data.hireDate);
          } else {
            alert("获取前台信息失败：" + (json.message || ""));
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
          const res = await fetch(`/api/attendance/history/${this.counterId}?employeeType=counter`);
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
  
            const counterId = localStorage.getItem("counterId");
            if (!counterId) {
              this.message = "未登录，无法打卡";
              this.checkingIn = false;
              return;
            }
  
            const checkInData = {
              employeeId: parseInt(counterId),
              employeeType: "counter",
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
        localStorage.removeItem("counterId");
        this.$router.push("/login");
      },
    },
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
  .calendar-header button {
    background-color: #1d3557;
    color: white;
    border: none;
    border-radius: 4px;
    padding: 5px 12px;
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
  }
  .calendar-weekday {
    font-weight: 600;
    text-align: center;
    padding: 6px 0;
    background-color: #f4f4f4;
    border-radius: 4px;
    user-select: none;
  }
  .calendar-day {
    border-radius: 4px;
    height: 48px;
    padding: 2px 5px;
    text-align: center;
    position: relative;
    user-select: none;
    font-size: 14px;
    line-height: 1;
    color: #555;
    background: #fafafa;
  }
  .calendar-day.no-current-month {
    color: #aaa;
    background: #f9f9f9;
  }
  .day-number {
    display: block;
    font-weight: 600;
  }
  .check-icon {
    position: absolute;
    right: 6px;
    top: 6px;
    font-size: 18px;
    user-select: none;
  }
  .check-icon.checked {
    color: green;
  }
  .check-icon.unchecked {
    color: red;
  }
  </style>
  