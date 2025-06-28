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
import DataAnalytics from '@/view/DataAnalytics.vue'

const routes = [
  // 默认路由重定向到登录页面
  { path: '/', redirect: '/login' },
  
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
  //前台管理餐桌
  {path: '/manage-tables', component:ManageTable},
  //前台管理外卖员
  {path: '/manage-delivery', component:ManageDelivery},
  //前台分配外卖员
  {path: '/assign-delivery', component:AssignDelivery},
  //前台添加外卖员
  {path: '/add-delivery-person', component:AddDeliveryPerson},
  //前台查看外卖订单
  {path: '/view-delivery-orders', component:ViewDeliveryOrders},
  //添加管理员
  {path: '/add-manager',component: AddManagerPage},
  //查看管理员
  {path: '/look-manager',componet: LookManagerVue},

  {path: '/data-analytics', component: DataAnalytics}
 
]

export default createRouter({
  history: createWebHistory(),
  routes,
})