import axios from '@/api';

// 1. 新增评价
export function addReview(data) {
  return axios.post('/reviews/add', data);
}

// 2. 更新评价
export function updateReview(id, data) {
  return axios.put(`/reviews/${id}`, data);
}

// 3. 删除评价
export function deleteReview(id) {
  return axios.delete(`/reviews/${id}`);
}

// 4. 动态条件查询评价
export function searchReviews(data) {
  return axios.post('/reviews/search', data);
}

// 5. 根据评分查找教室ID列表
export function getClassroomIdsByRating(rating) {
  return axios.get(`/reviews/classrooms/rating/${rating}`);
}

// 6. 查询所有评价
export function getAllReviews() {
  return axios.get('/reviews');
} 

//7. 根据教室ID查询评价
export function getAllClassroomReviews(classroomId) {
  return axios.get(`/reviews/classroom/${classroomId}`);
}