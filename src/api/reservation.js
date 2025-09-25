import axios from '@/api';

// 新增预约
export function addReservation(data) {
  return axios.post('/reservations', data);
}

// 编辑预约
export function updateReservation(id, data) {
  return axios.put(`/reservations/${id}`, data);
}

// 删除预约
export function deleteReservation(id) {
  return axios.delete(`/reservations/${id}`);
}

// 按状态查询预约
export function getReservationsByStatus(status) {
  return axios.get(`/reservations/status/${status}`);
}

// 按教室ID查询预约
export function getReservationsByClassroom(classroomId) {
  return axios.get(`/reservations/classroom/${classroomId}`);
}

// 按用户ID查询预约
export function getReservationsByUser(userId) {
  return axios.get(`/reservations/user/${userId}`);
}

// 查询所有预约
export function getAllReservations() {
  return axios.get('/reservations');
}

// 按用户ID查询预约（别名，便于前端统一调用）
export function getMyReservations({ userId }) {
  return getReservationsByUser(userId);
}

// 取消预约（更新状态为已取消）
export function cancelReservation(id) {
  return updateReservation(id, { status: '已取消' });
} 