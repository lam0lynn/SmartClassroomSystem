<template>
  <div class="my-review-container">
    <h2>我的评论</h2>
    <table class="review-table">
      <thead>
        <tr>
          <th>教室</th>
          <th>评分</th>
          <th>内容</th>
          <th>设备评分</th>
          <th>卫生评分</th>
          <th>图片</th>
          <th>状态</th>
          <th>创建时间</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="item in pagedReviews" :key="item.id">
          <td>{{ classroomMap[item.classroomId] || classroomMap[item.classroom_id] || item.classroomId || item.classroom_id || '--' }}</td>
          <td>{{ item.rating }}</td>
          <td>{{ item.content }}</td>
          <td>{{ item.equipmentRating || item.equipment_rating }}</td>
          <td>{{ item.cleanlinessRating || item.cleanliness_rating }}</td>
          <td>
            <img v-if="item.images" :src="getImgUrl(item.images)" alt="图片" style="max-width:60px;max-height:40px;" />
          </td>
          <td>{{ statusMap[item.status] || item.status }}</td>
          <td>{{ item.createTime || item.create_time }}</td>
          <td>
            <button class="delete-btn" @click="confirmDelete(item)">删除</button>
          </td>
        </tr>
      </tbody>
    </table>
    <div class="pagination-bar">
      <button :disabled="page === 1" @click="prevPage">上一页</button>
      <span>第 {{ page }} / {{ totalPages }} 页</span>
      <button :disabled="page === totalPages" @click="nextPage">下一页</button>
    </div>
  </div>
  <div v-if="deleteDialog.show" class="modal">
    <div class="modal-content delete-modal-content">
      <h3>确认删除该评论？</h3>
      <div class="modal-actions delete-modal-actions">
        <button @click="doDelete" class="modal-button submit-button">确定</button>
        <button @click="cancelDelete" class="modal-button cancel-button">取消</button>
      </div>
    </div>
  </div>
  <div v-if="showAddDialog" class="modal">
    <div class="modal-content add-modal-content">
      <h3>添加评论</h3>
      <form class="review-form" @submit.prevent="submitReview">
        <div class="form-row">
          <label>教室：</label>
          <select v-model="form.classroomId" required>
            <option value="" disabled>请选择教室</option>
            <option v-for="(name, id) in classroomMap" :key="id" :value="id">{{ name }}</option>
          </select>
        </div>
        <div class="form-row">
          <label>评分：</label>
          <input type="number" v-model="form.rating" min="1" max="5" required />
        </div>
        <div class="form-row">
          <label>内容：</label>
          <textarea v-model="form.content" required></textarea>
        </div>
        <div class="form-row">
          <label>设备评分：</label>
          <input type="number" v-model="form.equipmentRating" min="1" max="5" required />
        </div>
        <div class="form-row">
          <label>卫生评分：</label>
          <input type="number" v-model="form.cleanlinessRating" min="1" max="5" required />
        </div>
        <div class="form-row">
          <label>图片：</label>
          <input type="file" accept="image/*" @change="handleImgChange" />
          <div v-if="form.images" class="img-preview">
            <img :src="getImgUrl(form.images)" alt="预览" />
          </div>
        </div>
        <div class="form-actions">
          <button type="submit" class="modal-button submit-button">提交</button>
          <button type="button" class="modal-button cancel-button" @click="closeAddDialog">取消</button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import { searchReviews, deleteReview } from '@/api/review';
import { getAllClassrooms } from '@/api/classroom';

const user = JSON.parse(sessionStorage.getItem('user') || '{}');
const reviews = ref([]);
const classroomMap = ref({});
const page = ref(1);
const pageSize = 5;

const pagedReviews = computed(() => {
  const start = (page.value - 1) * pageSize;
  return reviews.value.slice(start, start + pageSize);
});

const totalPages = computed(() => Math.ceil(reviews.value.length / pageSize) || 1);

const statusMap = {
  pending: '待审核',
  approved: '已通过',
  rejected: '已拒绝',
  cancelled: '已取消',
  canceled: '已取消',
  completed: '已完成',
  '待审核': '待审核',
  '已通过': '已通过',
  '已拒绝': '已拒绝',
  '已取消': '已取消',
  '已完成': '已完成',
};

const deleteDialog = ref({ show: false, item: null });
const showAddDialog = ref(false);
const form = ref({
  classroomId: '',
  rating: '',
  content: '',
  equipmentRating: '',
  cleanlinessRating: '',
  images: ''
});

function getImgUrl(url) {
  if (!url) return '';
  if (url.startsWith('http')) return url;
  return url.startsWith('/') ? url : '/' + url;
}

const fetchClassroomMap = async () => {
  const res = await getAllClassrooms();
  classroomMap.value = {};
  (res.data || []).forEach(c => {
    classroomMap.value[c.id] = c.name;
    classroomMap.value[c.classroom_id] = c.name;
  });
};

const fetchList = async () => {
  const res = await searchReviews({ userId: user.id });
  reviews.value = res.data || [];
};

function confirmDelete(item) {
  deleteDialog.value = { show: true, item };
}

async function doDelete() {
  const item = deleteDialog.value.item;
  await deleteReview(item.id);
  deleteDialog.value.show = false;
  fetchList();
}

function cancelDelete() {
  deleteDialog.value.show = false;
}

function prevPage() {
  if (page.value > 1) page.value--;
}

function nextPage() {
  if (page.value < totalPages.value) page.value++;
}

function openAddDialog() {
  form.value = { classroomId: '', rating: '', content: '', equipmentRating: '', cleanlinessRating: '', images: '' };
  showAddDialog.value = true;
}

function closeAddDialog() {
  showAddDialog.value = false;
}

async function handleImgChange(e) {
  const file = e.target.files[0];
  if (!file) return;
  const fd = new FormData();
  fd.append('file', file);
  const res = await fetch('/img/upload', {
    method: 'POST',
    body: fd
  });
  const data = await res.json();
  if (data && data.data) {
    form.value.images = data.data;
  }
}

async function submitReview() {
  // 这里假设有addReview接口
  const payload = {
    userId: user.id,
    classroomId: form.value.classroomId,
    rating: form.value.rating,
    content: form.value.content,
    equipmentRating: form.value.equipmentRating,
    cleanlinessRating: form.value.cleanlinessRating,
    images: form.value.images
  };
  // await addReview(payload); // 需实现addReview接口
  showAddDialog.value = false;
  fetchList();
}

onMounted(() => {
  fetchClassroomMap();
  fetchList();
  window.addEventListener('review-updated', fetchList);
});

watch(page, () => {
  if (page.value < 1) page.value = 1;
  if (page.value > totalPages.value) page.value = totalPages.value;
});
</script>

<style scoped>
.my-review-container {
  width: 900px;
  margin: 100px 300px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.08);
  padding: 32px 32px 18px 32px;
}

h2 {
  text-align: center;
  margin-bottom: 24px;
  color: #222;
}

.review-table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 18px;
  border: 1px solid #e0e0e0;
}

.review-table th, .review-table td {
  padding: 12px 8px;
  border-bottom: 1px solid #f0f0f0;
  text-align: left;
}

.review-table th {
  background-color: #f7f7f7;
  font-weight: bold;
}

.pagination-bar {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 18px;
  margin-top: 32px;
}

.pagination-bar button {
  background: #409eff;
  color: #fff;
  border: none;
  border-radius: 6px;
  padding: 7px 18px;
  font-size: 15px;
  cursor: pointer;
  transition: background 0.2s;
}

.pagination-bar button:disabled {
  background: #e0e0e0;
  color: #aaa;
  cursor: not-allowed;
}

.delete-btn {
  background: #ff4d4f;
  color: #fff;
  border: none;
  border-radius: 6px;
  padding: 6px 14px;
  font-size: 15px;
  cursor: pointer;
  margin-left: 8px;
  transition: background 0.2s;
}

.delete-btn:hover {
  background: #d9363e;
}

.modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: #fff;
  padding: 32px 24px;
  border-radius: 10px;
  min-width: 320px;
  max-width: 400px;
  text-align: center;
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.13);
}

.modal h3 {
  margin-bottom: 24px;
  font-size: 18px;
  color: #222;
}

.modal-actions {
  display: flex;
  justify-content: center;
  gap: 24px;
  margin-top: 12px;
}

.modal-button {
  background: #409eff;
  color: #fff;
  border: none;
  border-radius: 6px;
  padding: 7px 18px;
  font-size: 15px;
  cursor: pointer;
  transition: background 0.2s;
}

.modal-button:hover {
  background: #1867c0;
}

.cancel-button {
  background: #ff4d4f;
}

.cancel-button:hover {
  background: #d9363e;
}

.add-modal-content {
  min-width: 420px;
  max-width: 520px;
}

.review-form {
  display: flex;
  flex-wrap: wrap;
  gap: 18px 24px;
  justify-content: space-between;
}

.form-row {
  width: 48%;
  display: flex;
  flex-direction: column;
  margin-bottom: 8px;
}

.form-row label {
  margin-bottom: 6px;
  color: #333;
  font-size: 15px;
}

.form-row input,
.form-row select,
.form-row textarea {
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  padding: 7px 10px;
  font-size: 15px;
  outline: none;
  transition: border 0.2s;
}

.form-row input:focus,
.form-row select:focus,
.form-row textarea:focus {
  border: 1.5px solid #409eff;
}

.form-row textarea {
  resize: vertical;
  min-height: 38px;
  max-height: 90px;
}

.img-preview {
  margin-top: 6px;
  border: 1px solid #eee;
  border-radius: 6px;
  padding: 4px;
  background: #fafbfc;
  max-width: 120px;
  max-height: 80px;
}

.img-preview img {
  max-width: 100%;
  max-height: 70px;
  border-radius: 4px;
}

.form-actions {
  width: 100%;
  display: flex;
  justify-content: center;
  gap: 32px;
  margin-top: 18px;
}
</style>



