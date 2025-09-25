<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue';

const user = ref(JSON.parse(sessionStorage.getItem('user') || '{}'));

const navs = computed(() => {
  if (user.value.role === 'admin') {
    return [
      { to: '/', label: '首页' },
      { to: '/users', label: '用户管理' },
      { to: '/reservation', label: '预约管理' },
      { to: '/classroom', label: '教室管理' },
      { to: '/review', label: '评价管理' },
      { to: '/seat-list', label: '座位管理' },
      { to: '/seat-reservation-list', label: '座位预约' },
      { to: '/profile', label: '个人中心' }
    ];
  } else {
    return [
      { to: '/', label: '首页' },
      { to: '/rece-classroom', label: '教室查询' },
      { to: '/my-reservation', label: '我的预约' },
      { to: '/my-review', label: '我的评论' },
      { to: '/my-seat-reservation', label: '座位预约' },
      { to: '/profile', label: '个人中心' }
    ];
  }
});

function refreshUser() {
  user.value = JSON.parse(sessionStorage.getItem('user') || '{}');
}

onMounted(() => {
  window.addEventListener('user-login', refreshUser);
});
onUnmounted(() => {
  window.removeEventListener('user-login', refreshUser);
});
</script>

<template>
  <nav class="top-nav">
    <router-link v-for="item in navs" :key="item.to" :to="item.to">{{ item.label }}</router-link>
  </nav>
</template>

<style scoped>
.top-nav {
  position: fixed;
  width: 100vw;
  min-width: 0;
  margin: 0;
  padding: 0 24px;
  background: rgba(255,255,255,0.96);
  border-bottom: 1px solid #e5e6eb;
  display: flex;
  justify-content: center; /* 改成水平居中 */
  align-items: center;
  gap: 0;
  min-height: 48px;
  top: 0;
  left: 0;
  z-index: 1000;
}
.top-nav a {
  color: #222;
  font-weight: 400;
  text-decoration: none;
  font-size: 15px;
  padding: 0 18px;
  height: 48px;
  display: flex;
  align-items: center;
  border-bottom: 2.5px solid transparent;
  border-radius: 0;
  transition: color 0.18s, border 0.18s, background 0.18s;
}
.top-nav a.router-link-exact-active {
  color: #409eff;
  border-bottom: 2.5px solid #409eff;
  background: none;
}
.top-nav a:hover {
  color: #1867c0;
  background: #f5f7fa;
}
@media (max-width: 700px) {
  .top-nav {
    flex-wrap: wrap;
    padding: 0 4px;
    min-height: 40px;
  }
  .top-nav a {
    font-size: 13px;
    padding: 0 8px;
    height: 40px;
  }
}
</style> 