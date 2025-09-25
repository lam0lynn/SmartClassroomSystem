<template>
  <main>
    <h1 class="welcome-message">欢迎来到教室预约网站</h1>
    <div class="carousel-container">
      <div class="carousel-wrapper">
        <img :src="images[current]" class="carousel-img" alt="轮播图" />
        <button class="carousel-btn left" @click="prev">&#8592;</button>
        <button class="carousel-btn right" @click="next">&#8594;</button>
        <div class="carousel-dots">
          <span v-for="(img, idx) in images" :key="idx" :class="['dot', { active: idx === current }]" @click="go(idx)"></span>
        </div>
      </div>
    </div>
  </main>
</template>

<script setup>
import { ref } from 'vue';

// 你可以将下面的图片地址替换为自己的图片
const images = [
  '/image/huiyi.png',
  '/image/jiaoshi1.jpg',
  '/image/shiyanshi.jpg',
];
const current = ref(0);
let timer = null;

function next() {
  current.value = (current.value + 1) % images.length;
}

function prev() {
  current.value = (current.value - 1 + images.length) % images.length;
}

function go(idx) {
  current.value = idx;
}

function startAuto() {
  stopAuto();
  timer = setInterval(next, 4000);
}

function stopAuto() {
  if (timer) clearInterval(timer);
  timer = null;
}

startAuto();
</script>

<style scoped>
.welcome-message {
  text-align: center;
  margin-top: 50px;
  font-size: 25px;
  color: #333;
}

.carousel-container {
  width: 900px;
  margin: 50px 250px;
  border-radius: 18px;
  overflow: hidden;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.08);
  background: #fff;
}

.carousel-wrapper {
  position: relative;
  width: 100%;
  height: 450px; /* Increased height for larger images */
  display: flex;
  align-items: center;
  justify-content: center;
}

.carousel-img {
  width: 100%;
  height: 450px; /* Increased height for larger images */
  object-fit: cover;
  border-radius: 18px;
  transition: opacity 0.4s;
}

.carousel-btn {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  background: rgba(0, 0, 0, 0.18);
  color: #fff;
  border: none;
  border-radius: 50%;
  width: 48px;
  height: 48px;
  font-size: 28px;
  cursor: pointer;
  z-index: 2;
  transition: background 0.2s;
}

.carousel-btn.left {
  left: 24px;
}

.carousel-btn.right {
  right: 24px;
}

.carousel-btn:hover {
  background: #409eff;
}

.carousel-dots {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 24px;
  display: flex;
  justify-content: center;
  gap: 16px;
}

.dot {
  width: 14px;
  height: 14px;
  border-radius: 50%;
  background: #e0e0e0;
  cursor: pointer;
  transition: background 0.2s;
}

.dot.active {
  background: #409eff;
}
</style>



