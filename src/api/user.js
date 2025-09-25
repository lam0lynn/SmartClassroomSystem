import axios from '@/api';

// 1. 新增用户
export function addUser(data) {
  return axios.post('/users/add', data);
}

// 2. 更新用户
export function updateUser(id, data) {
  return axios.put(`/users/${id}`, data);
}

// 3. 删除用户
export function deleteUser(id) {
  return axios.delete(`/users/${id}`);
}

// 4. 按角色查询用户
export function getUsersByRole(role) {
  return axios.get(`/users/role/${role}`);
}

// 5. 按学院查询用户
export function getUsersByCollege(college) {
  return axios.get(`/users/college/${college}`);
}

// 6. 按用户名查询用户
export function getUserByName(username) {
  return axios.get(`/users/name/${username}`);
}

// 7. 按ID查询用户
export function getUserById(id) {
  return axios.get(`/users/${id}`);
}

// 8. 动态条件查询用户
export function searchUsers(data) {
  return axios.post('/users/search', data);
}

// 9. 用户登录
export function loginUser(data) {
  return axios.post('/users/login', data);
}

// 10. 用户注册
export function registerUser(data) {
  return axios.post('/users/register', data);
}

// 用户登出
export function logoutUser() {
  return axios.post('/users/logout');
}

// 测试用 hello 接口
export function getHello() {
  return axios.get('/users/test');
}

// 获取所有用户
export function getAllUsers() {
  return axios.get('/users');
}

export function changePassword({ userId, oldPassword, newPassword }) {
  return axios.put('/users/change-password', { userId, oldPassword, newPassword });
} 
