import axios from 'axios';

const instance = axios.create({
  baseURL: '/api', // 统一通过 Vite 代理
  timeout: 5000
});

export default instance; 