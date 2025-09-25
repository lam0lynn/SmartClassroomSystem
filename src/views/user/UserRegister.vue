<template>
  <div class="register-container">
    <h2>注册</h2>
    <form @submit.prevent="handleRegister">
      <div>
        <label>用户名：</label>
        <input v-model="form.username" required />
      </div>
      <div>
        <label>密码：</label>
        <input type="password" v-model="form.password" required />
      </div>
      <div>
        <label>确认密码：</label>
        <input type="password" v-model="form.confirmPassword" required />
      </div>
      <div>
        <label>真实姓名：</label>
        <input v-model="form.realName" required />
      </div>
      <button type="submit">注册</button>
    </form>
    <div v-if="msg" class="success">{{ msg }}</div>
    <div v-if="error" class="error">{{ error }}</div>
    <router-link to="/login">已有账号？去登录</router-link>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { registerUser } from "@/api/user";
import { useRouter } from "vue-router";

const form = ref({
  username: "",
  password: "",
  confirmPassword: "",
  realName: "",
});
const msg = ref("");
const error = ref("");
const success = ref(false);
const router = useRouter();

const handleRegister = async () => {
  error.value = "";
  msg.value = "";
  if (
    !form.value.username ||
    !form.value.password ||
    !form.value.confirmPassword ||
    !form.value.realName
  ) {
    error.value = "请填写完整信息";
    return;
  }
  if (form.value.password !== form.value.confirmPassword) {
    error.value = "两次密码不一致";
    return;
  }
  try {
    const { confirmPassword, ...submitForm } = form.value;
    const res = await registerUser(submitForm);
    if (
      res.data &&
      (res.data.success ||
        (typeof res.data === "string" && res.data.startsWith("注册成功")))
    ) {
      msg.value = "注册成功，正在跳转登录页...";
      setTimeout(() => router.push("/login"), 1500);
    } else {
      error.value = res.data.message || res.data || "注册失败";
    }
  } catch (e) {
    error.value = e.response?.data?.message || "注册失败";
  }
};
</script>

<style scoped>
.register-container {
  width: 100%;
  max-width: 400px;
  padding: 40px;
  border-radius: 12px;
  background-color: #f9f9f9;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.06);
  font-family: "Segoe UI", sans-serif;

  /* 居中关键：使用 fixed 定位 + transform */
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 1000;
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

.success,
.error {
  text-align: center;
  margin-top: 12px;
  font-size: 14px;
}

.success {
  color: #2e7d32;
}

.error {
  color: #c62828;
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