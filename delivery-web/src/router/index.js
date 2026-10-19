import { createRouter, createWebHashHistory } from 'vue-router'
import { useUserStore } from '../store/user'

const routes = [
  {
    path: '/login',
    name: 'login',
    component: () => import('../views/LoginView.vue'),
    meta: { public: true, title: '登录' }
  },
  {
    path: '/',
    component: () => import('../layout/MainLayout.vue'),
    children: [
      { path: '', redirect: '/dashboard' },
      { path: 'dashboard', name: 'dashboard', component: () => import('../views/DashboardView.vue'), meta: { title: '工作台', icon: 'HomeFilled' } },
      { path: 'projects', name: 'projects', component: () => import('../views/ProjectListView.vue'), meta: { title: '项目台账', icon: 'Folder' } },
      { path: 'projects/:id', name: 'project-detail', component: () => import('../views/ProjectDetailView.vue'), meta: { title: '项目详情', hidden: true } },
      { path: 'credentials', name: 'credentials', component: () => import('../views/CredentialListView.vue'), meta: { title: '凭据保险箱', icon: 'Key' } },
      { path: 'documents', name: 'documents', component: () => import('../views/DocumentListView.vue'), meta: { title: '交付文档', icon: 'Document' } },
      { path: 'alerts', name: 'alerts', component: () => import('../views/AlertListView.vue'), meta: { title: '临期预警', icon: 'AlarmClock' } },
      { path: 'customers', name: 'customers', component: () => import('../views/CustomerListView.vue'), meta: { title: '客户档案', icon: 'OfficeBuilding' } },
      { path: 'audit', name: 'audit', component: () => import('../views/AuditLogView.vue'), meta: { title: '审计日志', icon: 'Tickets', perm: 'audit:view' } },
      { path: 'system/users', name: 'users', component: () => import('../views/system/UserView.vue'), meta: { title: '用户管理', icon: 'User', role: 'ADMIN' } },
      { path: 'system/settings', name: 'settings', component: () => import('../views/system/SettingsView.vue'), meta: { title: '字典与参数', icon: 'Setting', role: 'ADMIN' } }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/dashboard' }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

router.beforeEach(async (to) => {
  const store = useUserStore()
  if (to.meta.public) {
    return true
  }
  if (!store.logged) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  if (!store.info) {
    try {
      await store.loadMe()
    } catch (e) {
      store.reset()
      return { path: '/login' }
    }
  }
  if (to.meta.role && store.role !== to.meta.role && !store.isAdmin) {
    return { path: '/dashboard' }
  }
  if (to.meta.perm && !store.can(to.meta.perm)) {
    return { path: '/dashboard' }
  }
  return true
})

export default router
export { routes }