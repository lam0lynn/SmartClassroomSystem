import axios from '@/api';

// 获取预约列表（按用户）
export function getReservationList(userId) {
  return axios.get(`/seat-reservations/user/${userId}`);
}

// 获取预约详情
export function getReservationById(id) {
  return axios.get(`/seat-reservations/${id}`);
}

// 按座位ID查询预约
export function getReservationsBySeat(seatId) {
  return axios.get(`/seat-reservations/seat/${seatId}`);
}

// 预约座位（固定 JSON）
export async function reserveSeat(data) {
  return await axios.post('/seat-reservations/reserve', data, {
    headers: { 'Content-Type': 'application/json' }
  });
}

// 取消预约
export function cancelReservation(reservationId) {
  return axios.post(`/seat-reservations/cancel/${reservationId}`);
}

// 签到
export function checkIn(reservationId) {
  return axios.post(`/seat-reservations/checkin/${reservationId}`);
}

// 签退
export function checkOut(reservationId) {
  return axios.post(`/seat-reservations/checkout/${reservationId}`);
}

// 检查座位可用性（固定 JSON）
export async function checkSeatAvailability(data) {
  return await axios.post('/seat-reservations/check-availability', data, {
    headers: { 'Content-Type': 'application/json' }
  });
}

// 更新预约状态
export function updateReservationStatus(id, status) {
  return axios.put(`/seat-reservations/${id}/status/${status}`);
}

// 更新预约信息
export function updateReservation(id, data) {
  return axios.put(`/seat-reservations/${id}`, data);
}

// 删除预约
export function deleteReservation(id) {
  return axios.delete(`/seat-reservations/${id}`);
}

// 查询所有预约
export function getAllReservations() {
  return axios.get('/seat-reservations');
}


