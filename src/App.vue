<script setup>
import { RouterView } from 'vue-router'
import TopNav from './components/TopNav.vue'
</script>

<template>
  <TopNav />
  <RouterView />
</template>

<style>
html, body, #app {
  height: 100%;
  margin: 0;
  padding: 0;
  background: #f5f6fa;
}
body {
  min-height: 100vh;
  margin: 0;
  padding: 0;
}
#app {
  min-height: 100vh;
  margin: 0;
  padding: 0;
}
</style>
