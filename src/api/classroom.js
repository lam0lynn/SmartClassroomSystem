import axios from '@/api';

//查找所有类型
export function getAllClassroomsType() {
  return axios.get(`/classrooms/type`);
}
//查找所有状态
export function getAllClassroomsStatus() {
  return axios.get(`/classrooms/status`);
}
// 按类型查询教室
export function getClassroomsByType(type) {
  return axios.get(`/classrooms/type/${type}`);
}

// 按状态查询教室
export function getClassroomsByStatus(status) {
  return axios.get(`/classrooms/status/${status}`);
}

// 按楼栋查询教室
export function getClassroomsByBuilding(building) {
  return axios.get(`/classrooms/building/${building}`);
}

// 按设备查询教室
export function getClassroomsByEquipment(equipment) {
  return axios.get(`/classrooms/equipment/${equipment}`);
}

// 查询所有教室
export function getAllClassrooms() {
  return axios.post('/classrooms/search', {});
}

// 新增教室
export function addClassroom(data) {
  return axios.post('/classrooms', data);
}

// 编辑教室
export function updateClassroom(id, data) {
  return axios.put(`/classrooms/${id}`, data);
}

// 删除教室
export function deleteClassroom(id) {
  return axios.delete(`/classrooms/${id}`);
} 