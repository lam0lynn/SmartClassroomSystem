import axios from '@/api';

// 获取座位列表（按教室）
export function getSeatList(classroomId) {
  return axios.get(`/seats/classroom/${classroomId}`);
}

// 获取座位详情
export function getSeatById(id) {
  return axios.get(`/seats/${id}`);
}

// 查询所有座位
export function getAllSeats() {
  return axios.get('/seats');
}

// 按教室和状态查询座位
export function getSeatsByClassroomAndStatus(classroomId, status) {
  return axios.get(`/seats/classroom/${classroomId}/status/${status}`);
}

// 添加座位
export function addSeat(data) {
  return axios.post('/seats/add', data);
}

// 批量添加座位
export function batchAddSeats(data) {
  return axios.post('/seats/batch-add', data);
}

// 更新座位状态
export function updateSeatStatus(id, status) {
  return axios.put(`/seats/${id}/status/${status}`);
}

// 删除座位
export function deleteSeat(id) {
  return axios.delete(`/seats/${id}`);
}

// 更新座位信息
export function updateSeat(id, data) {
  return axios.put(`/seats/${id}`, data);
}


