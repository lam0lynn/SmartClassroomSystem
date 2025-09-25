<template>
  <div class="login-container">
    <h2>登录</h2>
    <form @submit.prevent="handleLogin">
      <div>
        <label>用户名：</label>
        <input v-model="form.username" required />
      </div>
      <div>
        <label>密码：</label>
        <input type="password" v-model="form.password" required />
      </div>
      <button type="submit">登录</button>
    </form>
    <div v-if="error" class="error">{{ error }}</div>
    <router-link to="/register">没有账号？去注册</router-link>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { loginUser } from '@/api/user';

const router = useRouter();
const form = ref({ username: '', password: '' });
const error = ref('');

const handleLogin = async () => {
  error.value = '';
  try {
    const res = await loginUser({
      username: form.value.username,
      password: form.value.password
    });
    if (res.data.success) {
      sessionStorage.setItem('user', JSON.stringify(res.data.user));
      // 通知全局"已登录"
      window.dispatchEvent(new Event('user-login'));
      if (res.data.user.role === 'admin') {
        router.push('/users');
      } else {
        router.push('/profile');
      }
    } else {
      error.value = res.data.message || '登录失败';
    }
  } catch (e) {
    error.value = e.response?.data?.message || '登录失败';
  }
};
</script>

<style scoped>
.login-container {
  width: 100%;
  max-width: 400px;
  padding: 40px;
  border-radius: 12px;
  background-color: #f9f9f9;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.06);
  font-family: 'Segoe UI', sans-serif;

  /* 居中关键：使用 fixed 定位 + transform */
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);

  /* 可选：z-index 提升层级，避免被遮挡 */
  z-index: 1000;
}
.error {
  color: red;
  margin-top: 10px;
}
.login-container {
  max-width: 400px;
  width: 100%;
  margin: 80px auto;
  padding: 40px;
  border-radius: 12px;
  background-color: #f9f9f9;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.06);
  font-family: 'Segoe UI', sans-serif;
}

.router-link  {
  color: blue;
}

h2 {
  text-align: center;
  margin-bottom: 24px;
  color: #333;
}

form > div {
  margin-bottom: 20px;
}

label {
  display: block;
  margin-bottom: 6px;
  font-weight: 500;
  color: #555;
}

input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #ccc;
  border-radius: 6px;
  font-size: 14px;
  transition: border-color 0.3s ease;
  box-sizing: border-box;
}

input:focus {
  border-color: #409eff;
  outline: none;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

button {
  width: 100%;
  padding: 12px;
  background-color: #409eff;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

button:hover {
  background-color: #337ecc;
}

.error {
  color: #e64a3b;
  font-size: 14px;
  margin-top: 12px;
  text-align: center;
}

.router-link {
  display: block;
  text-align: center;
  margin-top: 16px;
  color: #409eff;
  text-decoration: none;
  font-size: 14px;
}

.router-link:hover {
  text-decoration: underline;
}
</style> 