import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { LayoutDashboard, Building2, Film, ClipboardList, Tag, User } from 'lucide-vue-next'
import MainLayout from '@/layouts/MainLayout.vue'
import AdminLayout from '@/layouts/AdminLayout.vue'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: MainLayout,
    children: [
      {
        path: '',
        name: 'home',
        component: () => import('@/pages/HomePage.vue')
      },
      {
        path: 'movies',               // ← Thêm dòng này
        name: 'movies',
        component: () => import('@/pages/MoviesPage.vue')
      },
    ],
  },

  {
    path: '/admin',
    component: AdminLayout,
    children: [

      // ── Analytics ──────────────────────────────────────────────────────────
      {
        path: 'analystics',
        name: 'admin-analystics',
        meta: {
          sidebar: { label: 'Analystics', icon: LayoutDashboard },
        },
        children: [
          {
            path: 'dashboard',
            name: 'admin-analystics-dashboard',
            component: () => import('@/pages/admin/DashboardPage.vue'),
            meta: { sidebar: { label: 'Dashboard' } },
          },
          {
            path: 'reports',
            name: 'admin-analystics-reports',
            component: () => import('@/pages/admin/ReportsPage.vue'),
            meta: { sidebar: { label: 'Reports' } },
          },
        ],
      },

      // ── Cinemas ────────────────────────────────────────────────────────────
      {
      path: 'cinemas',
      meta: { sidebar: { label: 'Cinemas', icon: Building2 } },
      children: [
        {
          path: '',
          name: 'admin-cinemas',
          component: () => import('@/pages/admin/CinemasPage.vue'),
          meta: { sidebar: { label: 'All Cinemas' } },
        },
        {
          path: ':cinemaId/rooms',
          name: 'admin-cinema-rooms',
          component: () => import('@/pages/admin/RoomsPage.vue'),
          // ← KHÔNG có children ở đây nữa
        },
        {
          path: ':cinemaId/rooms/:roomId/layout',
          name: 'admin-cinema-room-layout',
          component: () => import('@/pages/admin/RoomLayoutPage.vue'),
        },
      ],
    },

      // ── Contents ───────────────────────────────────────────────────────────
      {
        path: 'contents',
        meta: {
          sidebar: { label: 'Contents', icon: Film },
        },
        children: [
          {
            path: 'movies',
            name: 'admin-contents-movies',
            component: () => import('@/pages/admin/MoviesPage.vue'),
            meta: { sidebar: { label: 'Movies' } },
          },
          {
            path: 'genres',
            name: 'admin-contents-genres',
            component: () => import('@/pages/admin/GenresPage.vue'),
            meta: { sidebar: { label: 'Genres' } },
          },
        ],
      },

      // ── Operations ─────────────────────────────────────────────────────────
      {
        path: 'operations',
        meta: {
          sidebar: { label: 'Operations', icon: ClipboardList },
        },
        children: [
          {
            path: 'showtimes',
            name: 'admin-operations-showtimes',
            component: () => import('@/pages/admin/ShowtimesPage.vue'),
            meta: { sidebar: { label: 'Showtimes' } },
          },
          {
            path: 'bookings',
            name: 'admin-operations-bookings',
            component: () => import('@/pages/admin/BookingsPage.vue'),
            meta: { sidebar: { label: 'Bookings' } },
          },
          {
            path: 'refunds',
            name: 'admin-operations-refunds',
            component: () => import('@/pages/admin/RefundsPage.vue'),
            meta: { sidebar: { label: 'Refunds' } },
          },
        ],
      },

      // ── Promotions ─────────────────────────────────────────────────────────
      {
        path: 'promotions',
        meta: {
          sidebar: { label: 'Promotions', icon: Tag },
        },
        children: [
          {
            path: 'coupons',
            name: 'admin-promotions-coupons',
            component: () => import('@/pages/admin/CouponsPage.vue'),
            meta: { sidebar: { label: 'Coupons' } },
          },
          {
            path: 'loyalty/tiers',
            name: 'admin-loyalty-tiers',
            component: () => import('@/pages/admin/MembershipTiersPage.vue'),
            meta: { 
              sidebar: { label: 'Loyalty' }   
            }
          },
          {
            path: 'loyalty/tiers/:tierId/rules',
            name: 'admin-loyalty-tier-rules',
            component: () => import('@/pages/admin/EarningRulesPage.vue'),
            meta: { hidden: true }            
          },
          {
            path: 'combos',
            name: 'admin-promotions-combos',
            component: () => import('@/pages/admin/CombosPage.vue'),
            meta: { sidebar: { label: 'Combos' } },
          },
        ],
      },

      // ── Users ──────────────────────────────────────────────────────────────
      {
        path: 'users',
        name: 'admin-users',
        component: () => import('@/pages/admin/UsersPage.vue'),
        meta: {
          sidebar: { label: 'Users', icon: User },
        },
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition;
    } else {
      return { top: 0, left: 0 };
    }
  },
})

export default router