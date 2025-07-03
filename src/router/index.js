import { createRouter, createWebHistory } from 'vue-router'
import LoginPage from '../view/LoginPage.vue'    // 登录页面
import RegisterPage from '../view/RegisterPage.vue'  // 注册页面
import MenuPage from '../view/MenuPage.vue'  
import PackagePage from '@/view/PackagePage.vue'
import AfternoonTeaPage from '@/view/AfternoonTeaPage.vue'
import ReservationPage from '@/view/ReservationPage.vue' 
import MyReservationPage from '@/view/MyReservationPage.vue' 
import CheckoutPage from '@/view/CheckoutPage.vue'
import myAccount from '@/view/my-account.vue'
import MyOrder from '@/view/MyOrder.vue'
import CouponPage from '@/view/CouponPage.vue'
import AdminPage from '@/view/AdminPage.vue'
import OrderReviewPage from '@/view/OrderReviewPage.vue'
import FrontendManagement from '@/view/FrontendManagement.vue'
import CheckOrders from '@/view/CheckOrders.vue'
import CouponDistribute from '@/view/CouponDistribute.vue'
import PointExchangePage from '@/view/PointExchangePage.vue'
import TakeawayOrder  from '@/view/TakeawayOrder.vue'
import DeliveryOrders from '@/view/DeliveryOrders.vue'
import ManageTable from '@/view/ManageTable.vue'
import ManageDelivery from '@/view/ManageDelivery.vue'
import AssignDelivery from  '@/view/AssignDelivery.vue'
import AddDeliveryPerson from  '@/view/AddDeliveryPerson.vue'
import ViewDeliveryOrders from '@/view/ViewDeliveryOrders.vue'
import AddManagerPage from '@/view/AddManager.vue'
import LookManagerVue from '@/view/LookManager.vue'
import JoinVue from '@/view/Join.vue'
import ApplicantLoginVue from '@/view/ApplicantLogin.vue'
import ApplicantRegisterVue from '@/view/ApplicantRegister.vue'
import ApplicantProgressVue from '@/view/ApplicantProgress.vue'
import HRDashboardVue from '@/view/HRDashboard.vue'
import HrProfileVue from '@/view/HrProfile.vue'
import HrEmployeeVue from '@/view/HrEmployee.vue'
import HrAttendanceVue from '@/view/HrAttendance.vue'
import HrLeaveApplyVue from '@/view/HrLeaveApply.vue'
import HrLeaveProgressVue from '@/view/HrLeaveProgress.vue'
import HrLeaveReviewVue from '@/view/HrLeaveReview.vue' 
import DataAnalytics from '@/view/DataAnalytics.vue'
import AllLogin from '@/view/branch_manager/All_login.vue'
import BranchManagerLoginVue from '@/view/branch_manager/BranchManagerLogin.vue'
import BranchDashBoardVue from '@/view/branch_manager/BranchDashBoard.vue'
import BranchEmployeeVue from '@/view/branch_manager/BranchEmployee.vue'
import PerformanceReviewVue from '@/view/branch_manager/PerformanceReview.vue'
import ChefDashboardVue from '@/view/ChefDashboard.vue'
import ChefAttendanceVue from '@/view/ChefAttendance.vue'
import ChefLeaveApplyVue from '@/view/ChefLeaveApply.vue'
import ChefLeaveWorkingVue from '@/view/employee/ChefLeaveWorking.vue'
import EmployeeLeavingStatusVue from '@/view/employee/EmployeeLeavingStatus.vue'
import HrLeavingWorkingReviewVue from '@/view/Hr/HrLeavingWorkingReview.vue'
import BranchLeavingWorkingReviewVue from '@/view/branch_manager/BranchLeavingWorkingReview.vue'
import CounterDashboardVue from '@/view/CounterDashboard.vue'
import CounterDineinOrderVue from '@/view/CounterDineinOrder.vue'
import CounterAttendanceVue from '@/view/CounterAttendance.vue'
import CounterLeaveApplyVue from '@/view/CounterLeaveApply.vue'
import CounterLeaveProgressVue from '@/view/CounterLeaveProgress.vue'


const routes = [
  // 默认路由重定向到登录页面
  { path: '/', redirect: '/all-login' },
  
  // 注册页面路由
  { path: '/register', component: RegisterPage },
  
  // 登录页面路由
  { path: '/login', component: LoginPage },
  //菜单页面
  { path: '/menu', component: MenuPage},
  //套餐页面
  { path: '/package', component: PackagePage},
  //下午茶自助
  { path: '/buffet', component: AfternoonTeaPage},
  //订单页面
  { path: '/checkout',component: CheckoutPage },
  //个人页面
  {path: '/my-account', component:myAccount},
  //订桌页面
  {path: '/ordertable', component:ReservationPage},
  //我已经订的桌页面
  {path: '/my_ordertable', component:MyReservationPage },
  //订单页面
  {path: '/orders', component:MyOrder },
  //优惠券页面
  {path: '/coupon', component:CouponPage},
  //管理员页面
  {path: '/admin', component:AdminPage},
  { path: '/dishes',   component: AdminPage },
  //评价页面
  {path: '/reviews', component:OrderReviewPage},
  //前台页面
  {path: '/counter', component:FrontendManagement},
  //前台查看订单
  {path: '/check-orders', component:CheckOrders},
  //分配优惠券
  {path: '/distribute-coupons', component:CouponDistribute},
  { path: '/exchange', component: PointExchangePage },
  // 提交外送订单
  { path: '/delivery-checkout', component: TakeawayOrder },
  // 我的外卖订单
  { path: '/takeaway-order', component: DeliveryOrders },
  //前台管理外卖员
  {path: '/manage-delivery', component:ManageDelivery},
  //前台分配外卖员
  {path: '/delivery-assign', component:AssignDelivery},
  //前台添加外卖员
  {path: '/delivery-add', component:AddDeliveryPerson},
  //前台查看外卖订单
  {path: '/delivery-view', component:ViewDeliveryOrders},
  //添加管理员
  {path: '/add-manager',component: AddManagerPage},
  {path: '/manage-tables',component: ManageTable},
  //查看管理员
  {path: '/look-manager',componet: LookManagerVue},
  //入职申请
  { path: '/join-us', component: JoinVue },
  //申请人注册
  { path: '/applicant-register', component: ApplicantRegisterVue },
  //申请人登录
  { path: '/applicant-login', component: ApplicantLoginVue },
  //查看申请进度
  { path: '/applicant-progress', component: ApplicantProgressVue },
  //HR处理界面
  { path: '/hr-dashboard', component: HRDashboardVue },
  //HR个人信息界面
  { path: '/hr-profile', component: HrProfileVue},
  //HR个人信息界面
  { path: '/hr-employee', component: HrEmployeeVue},
  //HR打卡界面
  { path: '/hr-attendance', component: HrAttendanceVue},
  //HR请假界面
  { path: '/hr-leave', component: HrLeaveApplyVue},
  //HR请假查看界面
  { path: '/hr-leave-progress', component: HrLeaveProgressVue},
  //HR批准请假界面
  { path: '/hr-leave-review', component: HrLeaveReviewVue }, 

  {path: '/data-analytics', component: DataAnalytics},
  //总登录界面
  {path: '/all-login', component:AllLogin },
  //店长登录界面
  {path: '/branch-login', component:BranchManagerLoginVue },
  //店长操作界面
  {path: '/branch-dashboard', component:BranchDashBoardVue},
  //员工管理界面
  {path:'/branch-employee',component:BranchEmployeeVue},
  //chef个人信息界面
  { path: '/chef-dashboard', component: ChefDashboardVue},
  //chef打卡界面
  { path: '/chef-attendance', component: ChefAttendanceVue},
  //chef请假界面
  { path: '/chef-leave', component: ChefLeaveApplyVue},
  //chef申请离职
  { path: '/chef-leaving-working', component: ChefLeaveWorkingVue},
  //HR离职审批
  {path: '/hr-leavingworking-review', component: HrLeavingWorkingReviewVue},
  //店长离职审批
  {path: '/branch-leavingworking-review', component: BranchLeavingWorkingReviewVue},
  //店长绩效考核
  {path: '/performance-review', component: PerformanceReviewVue},
  //chef查看离职申请状态
  {path: '/employ-leaving-Status', component: EmployeeLeavingStatusVue},
  //counter个人信息界面
  { path: '/counter-dashboard', component: CounterDashboardVue},
  //counter个人信息界面
  { path: '/counter-dinein-order', component: CounterDineinOrderVue},
  //counter管理餐桌
  {path: '/manage-tables', component:ManageTable},
  //counter打卡界面
  { path: '/counter-attendance', component: CounterAttendanceVue},
  //counter请假界面
  { path: '/counter-leave', component: CounterLeaveApplyVue},
  //counter请假查看界面
  { path: '/counter-leave-progress', component: CounterLeaveProgressVue},

]

export default createRouter({
  history: createWebHistory(),
  routes,
})