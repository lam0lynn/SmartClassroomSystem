<template>
  <div class="review-list-container">
    <h2>评价列表</h2>
    <form @submit.prevent="fetchList" class="filter-form">
      <input v-model="filter.userId" placeholder="用户ID" />
      <input v-model="filter.classroomId" placeholder="教室ID" />
      <input v-model="filter.rating" placeholder="评分" />
      <button type="submit">查询</button>
      <button type="button" @click="resetFilter">重置</button>
    </form>
    <button @click="showAdd = true" class="add-button">新增评价</button>
    <table>
      <thead>
        <tr>
          <th>ID</th>
          <th>用户ID</th>
          <th>教室ID</th>
          <th>预约ID</th>
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
        <tr v-for="r in pagedReviews" :key="r.id">
          <td>{{ r.id }}</td>
          <td>{{ userMap[r.userId || r.user_id] || (r.userId || r.user_id) }}</td>
          <td>{{ classroomMap[r.classroomId || r.classroom_id] || (r.classroomId || r.classroom_id) }}</td>
          <td>{{ r.reservationId || r.reservation_id }}</td>
          <td>{{ r.rating }}</td>
          <td>{{ r.content || r.comment }}</td>
          <td>{{ r.equipmentRating || r.equipment_rating }}</td>
          <td>{{ r.cleanlinessRating || r.cleanliness_rating }}</td>
          <td>
            <img v-if="r.images" :src="getImgUrl(r.images)" alt="图片" style="max-width:80px;max-height:60px;" @error="imgError" />
          </td>
          <td>{{ r.status }}</td>
          <td>{{ r.createTime || r.create_time }}</td>
          <td>
            <button @click="editReview(r)" class="action-button edit-button">编辑</button>
            <button @click="handleDelete(r.id)" class="action-button delete-button">删除</button>
          </td>
        </tr>
      </tbody>
    </table>
    <div class="pagination">
      <button :disabled="page === 1" @click="page--" class="pagination-button prev-button">上一页</button>
      <span>第 {{ page }} 页 / 共 {{ totalPages }} 页</span>
      <button :disabled="page === totalPages" @click="page++" class="pagination-button next-button">下一页</button>
    </div>
    <!-- 新增评价弹窗 -->
    <div v-if="showAdd" class="modal">
      <div class="modal-content">
        <h3>新增评价</h3>
        <form @submit.prevent="handleAdd" class="modal-form-2col">
          <div class="form-col">
            <select v-model="addForm.userId" required>
              <option value="">请选择用户</option>
              <option v-for="(name, id) in userMap" :key="id" :value="id">{{ name }}</option>
            </select>
            <select v-model="addForm.classroomId" required>
              <option value="">请选择教室</option>
              <option v-for="(name, id) in classroomMap" :key="id" :value="id">{{ name }}</option>
            </select>
            <input v-model="addForm.reservationId" placeholder="预约ID" />
            <input v-model="addForm.rating" placeholder="评分" required />
            <input v-model="addForm.content" placeholder="评价内容" />
          </div>
          <div class="form-col">
            <input v-model="addForm.equipmentRating" placeholder="设备评分" />
            <input v-model="addForm.cleanlinessRating" placeholder="卫生评分" />
            <input type="file" @change="e => handleImageChange(e, 'add')" accept="image/*" style="margin-bottom: 10px;" />
            <input v-model="addForm.images" placeholder="图片URL" readonly />
            <img v-if="addForm.images" :src="getImgUrl(addForm.images)" style="max-width:100px;display:block;margin-bottom:10px;" @error="imgError" />
            <input v-model="addForm.status" placeholder="状态" />
          </div>
          <div class="modal-actions-2col">
            <button type="submit" class="modal-button submit-button">提交</button>
            <button type="button" @click="showAdd = false" class="modal-button cancel-button">取消</button>
          </div>
        </form>
      </div>
    </div>
    <!-- 编辑评价弹窗 -->
    <div v-if="showEdit" class="modal">
      <div class="modal-content">
        <h3>编辑评价</h3>
        <form @submit.prevent="handleEdit" class="modal-form-2col">
          <div class="form-col">
            <select v-model="editForm.userId" required>
              <option value="">请选择用户</option>
              <option v-for="(name, id) in userMap" :key="id" :value="id">{{ name }}</option>
            </select>
            <select v-model="editForm.classroomId" required>
              <option value="">请选择教室</option>
              <option v-for="(name, id) in classroomMap" :key="id" :value="id">{{ name }}</option>
            </select>
            <input v-model="editForm.reservationId" placeholder="预约ID" />
            <input v-model="editForm.rating" placeholder="评分" required />
            <input v-model="editForm.content" placeholder="评价内容" />
          </div>
          <div class="form-col">
            <input v-model="editForm.equipmentRating" placeholder="设备评分" />
            <input v-model="editForm.cleanlinessRating" placeholder="卫生评分" />
            <input type="file" @change="e => handleImageChange(e, 'edit')" accept="image/*" style="margin-bottom: 10px;" />
            <input v-model="editForm.images" placeholder="图片URL" readonly />
            <img v-if="editForm.images" :src="getImgUrl(editForm.images)" style="max-width:100px;display:block;margin-bottom:10px;" @error="imgError" />
            <input v-model="editForm.status" placeholder="状态" />
          </div>
          <div class="modal-actions-2col">
            <button type="submit" class="modal-button submit-button">保存</button>
            <button type="button" @click="showEdit = false" class="modal-button cancel-button">取消</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
import {
  addReview,
  updateReview,
  deleteReview,
  searchReviews,
  getAllReviews
} from '@/api/review';
import { getAllUsers } from '@/api/user';
import { getAllClassrooms } from '@/api/classroom';

const reviews = ref([]);
const filter = reactive({ userId: '', classroomId: '', rating: '' });
const showAdd = ref(false);
const showEdit = ref(false);
const addForm = reactive({ userId: '', classroomId: '', reservationId: '', rating: '', content: '', equipmentRating: '', cleanlinessRating: '', images: '', status: '' });
const editForm = reactive({ id: '', userId: '', classroomId: '', reservationId: '', rating: '', content: '', equipmentRating: '', cleanlinessRating: '', images: '', status: '' });
const page = ref(1);
const pageSize = ref(5);
const total = ref(0);
const userMap = ref({});
const classroomMap = ref({});

const pagedReviews = computed(() => {
  const start = (page.value - 1) * pageSize.value;
  return reviews.value.slice(start, start + pageSize.value);
});
const totalPages = computed(() => Math.ceil(reviews.value.length / pageSize.value) || 1);

const fetchList = async () => {
  const params = {};
  if (filter.userId) params.userId = filter.userId;
  if (filter.classroomId) params.classroomId = filter.classroomId;
  if (filter.rating) params.rating = filter.rating;
  if (!filter.userId && !filter.classroomId && !filter.rating) {
    const res = await getAllReviews();
    reviews.value = res.data;
  } else {
    const res = await searchReviews(params);
    reviews.value = res.data;
  }
};

const resetFilter = () => {
  filter.userId = '';
  filter.classroomId = '';
  filter.rating = '';
  fetchList();
};

const handleAdd = async () => {
  await addReview({ ...addForm });
  showAdd.value = false;
  Object.keys(addForm).forEach(k => addForm[k] = '');
  fetchList();
};

const editReview = (r) => {
  Object.assign(editForm, r);
  showEdit.value = true;
};

const handleEdit = async () => {
  await updateReview(editForm.id, { ...editForm });
  showEdit.value = false;
  fetchList();
};

const handleDelete = async (id) => {
  if (confirm('确定要删除该评价吗？')) {
    await deleteReview(id);
    fetchList();
  }
};

function getImgUrl(url) {
  if (!url) return '';
  if (url.startsWith('http')) return url;
  return url.startsWith('/') ? url : '/' + url;
}

function imgError(e) {
  e.target.src = 'https://img1.imgtp.com/2023/07/21/0Qv6Qw7A.png'; // 默认图片
}

const handleImageChange = async (e, type) => {
  const file = e.target.files[0];
  if (!file) return;
  const formData = new FormData();
  formData.append('file', file);
  const res = await fetch('/img/upload', {
    method: 'POST',
    body: formData
  });
  const data = await res.json();
  const imgUrl = data.url;
  if (type === 'add') {
    addForm.images = imgUrl;
  } else {
    editForm.images = imgUrl;
  }
};

const fetchUserAndClassroomMaps = async () => {
  const usersRes = await getAllUsers();
  userMap.value = {};
  (usersRes.data || []).forEach(u => {
    userMap.value[u.id] = u.realName || u.username;
  });
  const classroomsRes = await getAllClassrooms();
  classroomMap.value = {};
  (classroomsRes.data || []).forEach(c => {
    classroomMap.value[c.id] = c.name;
  });
};

onMounted(async () => {
  await fetchUserAndClassroomMaps();
  fetchList();
});
</script>

<style scoped>
.review-list-container {
  max-width: 900px;
  margin: 100px 300px;
  padding: 32px 28px;
  border-radius: 16px;
  background: #fff;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.08);
}

h2 {
  text-align: center;
  margin-bottom: 24px;
  color: #333;
}

.filter-form {
  display: flex;
  gap: 16px;
  justify-content: center;
  align-items: center;
  margin-bottom: 24px;
}

.filter-form input {
  padding: 7px 14px;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  outline: none;
  font-size: 15px;
  background: #fafbfc;
  transition: border 0.2s;
}

.filter-form input:focus {
  border: 1.5px solid #409eff;
}

.filter-form button {
  background: #409eff;
  color: #fff;
  border: none;
  border-radius: 6px;
  padding: 7px 18px;
  font-size: 15px;
  cursor: pointer;
  transition: background 0.2s;
}

.filter-form button:hover {
  background: #1867c0;
}

.add-button {
  background: #409eff;
  color: #fff;
  border: none;
  border-radius: 6px;
  padding: 7px 18px;
  font-size: 15px;
  cursor: pointer;
  transition: background 0.2s;
  margin-bottom: 24px;
}

.add-button:hover {
  background: #1867c0;
}

table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 20px;
}

th, td {
  border: 1px solid #eaeaea;
  padding: 8px 12px;
  text-align: left;
}

th {
  background: #f5f7fa;
}

.action-button {
  background: #409eff;
  color: #fff;
  border: none;
  border-radius: 4px;
  padding: 4px 12px;
  cursor: pointer;
  transition: background 0.2s;
}

.edit-button {
  background: #e6a23c;
}

.delete-button {
  background: #ff4d4f;
}

.action-button:hover {
  opacity: 0.8;
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
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.13);
}

.modal h3 {
  text-align: center;
  margin-bottom: 24px;
}

.modal input {
  display: block;
  width: 100%;
  margin-bottom: 18px;
  padding: 8px 12px;
  border-radius: 6px;
  border: 1px solid #e0e0e0;
  font-size: 15px;
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
  margin-right: 12px;
}

.submit-button:hover {
  background: #1867c0;
}

.cancel-button {
  background: #ff4d4f;
}

.cancel-button:hover {
  background: #d9363e;
}

.pagination {
  margin: 20px 0;
  text-align: center;
}
.pagination-button {
  background: #409eff;
  color: #fff;
  border: none;
  border-radius: 4px;
  padding: 4px 12px;
  cursor: pointer;
  transition: background 0.2s;
}
.prev-button:hover,
.next-button:hover {
  opacity: 0.8;
}
.pagination-button:disabled {
  background: #d3d3d3;
  cursor: not-allowed;
}
.modal-form-2col {
  display: flex;
  flex-wrap: wrap;
  gap: 24px;
  margin-top: 12px;
}
.form-col {
  flex: 1 1 0;
  min-width: 220px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.modal-actions-2col {
  width: 100%;
  display: flex;
  justify-content: center;
  gap: 32px;
  margin-top: 18px;
}
.modal-content {
  min-width: 700px;
  max-width: 700px;
}
.modal-form-2col select {
  padding: 8px 12px;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  outline: none;
  font-size: 15px;
  background: #fafbfc;
  margin-bottom: 12px;
  transition: border 0.2s;
}
.modal-form-2col select:focus, .modal-form-2col select:hover {
  border: 1.5px solid #409eff;
  background: #fff;
}
</style>



