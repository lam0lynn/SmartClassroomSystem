import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
    },

    {
      path: '/login',
      name: 'login',
      component: () => import('../views/user/UserLogin.vue'),
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('../views/user/UserRegister.vue'),
    },
    {
      path: '/users',
      name: 'user-list',
      component: () => import('../views/user/UserList.vue'),
    },
    {
      path: '/profile',
      name: 'profile',
      component: () => import('../views/user/UserProfile.vue'),
    },
    {
      path: '/reservation',
      name: 'reservation-list',
      component: () => import('../views/reservation/ReservationList.vue'),
    },
    {
      path: '/classroom',
      name: 'classroom-list',
      component: () => import('../views/classroom/ClassroomList.vue'),
    },
    {
      path: '/review',
      name: 'review-list',
      component: () => import('../views/review/ReviewList.vue'),
    },
    {
      path: '/hello',
      name: 'Hello',
      component: () => import('../components/HelloWorld.vue')
    },
    {
      path: '/my-reservation',
      name: 'my-reservation',
      component: () => import('../views/reservation/MyReservation.vue'),
    },
    {
      path: '/my-review',
      name: 'my-review',
      component: () => import('../views/review/MyReview.vue'),
    },
    {
      path: '/rece-classroom',
      name: 'rece-classroom',
      component: () => import('../views/classroom/ReceClassroomList.vue'),
    },
    {
      path: '/seat-list',
      name: 'seat-list',
      component: () => import('../views/seat/SeatList.vue'),
    },
    {
      path: '/seat-reservation-list',
      name: 'seat-reservation-list',
      component: () => import('../views/seatReservation/SeatReservationList.vue'),
    },
    {
      path: '/my-seat-reservation',
      name: 'my-seat-reservation',
      component: () => import('../views/seatReservation/MySeatReservation.vue'),
    },
    
    
  ],
})

// 路由守卫：未登录只能访问首页、登录、注册
router.beforeEach((to, from, next) => {
  const user = JSON.parse(sessionStorage.getItem('user') || 'null');
  const publicPages = ['/', '/login', '/register'];
  if (publicPages.includes(to.path)) {
    next();
  } else {
    if (user && user.id) {
      next();
    } else {
      next('/login');
    }
  }
});

export default router
