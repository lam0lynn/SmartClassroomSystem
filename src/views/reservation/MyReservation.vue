<template>
  <div class="my-reservation-card">
    <h2>我的预约</h2>
    <div class="filter-bar">
      <!-- 预留筛选区，可按教室/状态/时间筛选 -->
    </div>
    <table class="reservation-table">
      <thead>
        <tr>
          <th>教室</th>
          <th>预约时间</th>
          <th>用途</th>
          <th>状态</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="item in pagedReservations" :key="item.id">
          <td>{{ classroomMap[item.classroomId] || classroomMap[item.classroom_id] || item.classroomId || item.classroom_id || '--' }}</td>
          <td>{{ (item.startTime || item.start_time || '--') + ' ~ ' + (item.endTime || item.end_time || '--') }}</td>
          <td>{{ item.purpose }}</td>
          <td>
            <span :class="'status-tag status-' + (item.status || item.status)">
              {{ statusMap[item.status] || statusMap[item.status] || item.status || '--' }}
            </span>
          </td>
          <td>
            <button
              v-if="item.status === '待审核' || item.status === 'pending'"
              @click="confirmCancel(item)"
              :disabled="item.cancelLoading"
            >
              取消预约
            </button>
            <button v-else disabled>不可操作</button>
            <button
              v-if="(item.status === '已通过' || item.status === 'approved') && !item.reviewed"
              class="comment-btn"
              @click="openComment(item)"
            >评论</button>
          </td>
        </tr>
      </tbody>
    </table>
    <!-- 分页 -->
    <div class="pagination-bar">
      <button :disabled="page === 1" @click="page--">上一页</button>
      <span>第 {{ page }} / {{ totalPages }} 页</span>
      <button :disabled="page === totalPages" @click="page++">下一页</button>
    </div>
    <!-- 取消确认弹窗 -->
    <div v-if="cancelDialog.show" class="modal">
      <div class="modal-content">
        <h3>确认取消预约？</h3>
        <p>确定要取消对教室"{{ cancelDialog.item?.classroom_id }}"的预约吗？</p>
        <div class="modal-actions">
          <button @click="doCancel">确定</button>
          <button @click="cancelDialog.show = false">关闭</button>
        </div>
      </div>
    </div>
    <!-- 评论弹窗 -->
    <div v-if="commentDialog.show" class="modal">
      <div class="modal-content modal-content-2col">
        <h3>评价教室</h3>
        <form @submit.prevent="submitComment" class="modal-form-2col">
          <div class="form-col">
            <input v-model="commentForm.rating" type="number" min="1" max="5" required placeholder="评分（1-5分）" />
            <textarea v-model="commentForm.content" required placeholder="请输入评价内容" style="resize:vertical;min-height:48px;max-height:120px;"></textarea>
            <input v-model="commentForm.equipmentRating" type="number" min="1" max="5" placeholder="设备评分（1-5分）" />
          </div>
          <div class="form-col">
            <input v-model="commentForm.cleanlinessRating" type="number" min="1" max="5" placeholder="卫生评分（1-5分）" />
            <div class="upload-area">
              <input
                ref="fileInput"
                type="file"
                accept="image/*"
                @change="handleCommentImgChange"
                style="display:none"
              />
              <button type="button" class="upload-btn" @click="triggerFileInput">选择图片</button>
              <span class="file-name" v-if="commentForm.images">{{ getFileName(commentForm.images) }}</span>
            </div>
            <div v-if="commentForm.images" class="img-preview">
              <img :src="getImgUrl(commentForm.images)" alt="预览" />
            </div>
          </div>
          <div class="modal-actions-2col">
            <button type="submit" class="modal-button submit-button" :disabled="commentLoading">提交</button>
            <button type="button" class="modal-button cancel-button" @click="closeComment">取消</button>
          </div>
          <div v-if="commentError" class="error-msg">{{ commentError }}</div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive, computed, watch } from 'vue';
import { getMyReservations, cancelReservation } from '@/api/reservation';
import { addReview } from '@/api/review';
import { getAllClassrooms } from '@/api/classroom';

const reservationList = ref([]);
const classroomMap = ref({});
const statusMap = {
  pending: '待审核',
  approved: '已通过',
  rejected: '已拒绝',
  cancelled: '已取消',
  canceled: '已取消',
  completed: '已完成',
  // 兼容中文
  '待审核': '待审核',
  '已通过': '已通过',
  '已拒绝': '已拒绝',
  '已取消': '已取消',
  '已完成': '已完成',
};
const cancelDialog = ref({ show: false, item: null });
const commentDialog = ref({ show: false, item: null });
const commentForm = reactive({ rating: '', content: '', images: '' });
const commentLoading = ref(false);
const commentError = ref('');
const page = ref(1);
const pageSize = 5;
const pagedReservations = computed(() => {
  const start = (page.value - 1) * pageSize;
  return reservationList.value.slice(start, start + pageSize);
});
const totalPages = computed(() => Math.ceil(reservationList.value.length / pageSize) || 1);

watch(page, () => {
  if (page.value < 1) page.value = 1;
  if (page.value > totalPages.value) page.value = totalPages.value;
});

const user = JSON.parse(sessionStorage.getItem('user') || '{}');

const fetchList = async () => {
  const res = await getMyReservations({ userId: user.id });
  reservationList.value = res.data || [];
};

const fetchClassroomMap = async () => {
  const res = await getAllClassrooms();
  classroomMap.value = {};
  (res.data || []).forEach(c => {
    classroomMap.value[c.id] = c.name;
    classroomMap.value[c.classroom_id] = c.name; // 兼容下划线
  });
};

const confirmCancel = (item) => {
  cancelDialog.value = { show: true, item };
};
const doCancel = async () => {
  const item = cancelDialog.value.item;
  item.cancelLoading = true;
  await cancelReservation(item.id);
  item.status = '已取消';
  item.cancelLoading = false;
  cancelDialog.value.show = false;
};

function openComment(item) {
  commentDialog.value = { show: true, item };
  commentForm.rating = '';
  commentForm.content = '';
  commentForm.images = '';
  commentError.value = '';
}
function closeComment() {
  commentDialog.value = { show: false, item: null };
  commentError.value = '';
}
function getImgUrl(url) {
  if (!url) return '';
  if (url.startsWith('http')) return url;
  return url.startsWith('/') ? url : '/' + url;
}

const fileInput = ref(null);

function triggerFileInput() {
  fileInput.value && fileInput.value.click();
}

function getFileName(url) {
  if (!url) return '';
  return url.split('/').pop();
}

async function handleCommentImgChange(e) {
  const file = e.target.files[0];
  if (!file) return;
  const fd = new FormData();
  fd.append('file', file);
  const res = await fetch('/img/upload', {
    method: 'POST',
    body: fd
  });
  const data = await res.json();
  console.log('图片上传返回', data);
  if (data && (data.data || data.url)) {
    commentForm.images = data.data || data.url;
  } else {
    commentForm.images = '';
  }
}

async function submitComment() {
  if (!commentDialog.value.item) return;
  commentLoading.value = true;
  commentError.value = '';
  try {
    const user = JSON.parse(sessionStorage.getItem('user') || '{}');
    const data = {
      userId: user.id,
      classroomId: commentDialog.value.item.classroom_id || commentDialog.value.item.classroomId,
      reservationId: commentDialog.value.item.id,
      rating: commentForm.rating,
      content: commentForm.content,
      equipmentRating: commentForm.equipmentRating,
      cleanlinessRating: commentForm.cleanlinessRating,
      images: commentForm.images,
      status: 1,
    };
    console.log('提交数据', data);
    await addReview(data);
    commentDialog.value.item.reviewed = true;
    const idx = reservationList.value.findIndex(r => r.id === commentDialog.value.item.id);
    if (idx !== -1) reservationList.value[idx].reviewed = true;
    commentDialog.value.show = false;
    window.dispatchEvent(new Event('review-updated'));
    alert('评论成功！');
  } catch (e) {
    commentError.value = e?.response?.data?.message || '评论失败';
  } finally {
    commentLoading.value = false;
  }
}

onMounted(() => {
  fetchClassroomMap();
  fetchList();
});
</script>

<style scoped>
.my-reservation-card {
  width: 900px;
  margin: 100px 300px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 4px 24px rgba(0,0,0,0.08);
  padding: 32px 32px 18px 32px;
}
h2 {
  text-align: center;
  margin-bottom: 24px;
  color: #222;
}
.filter-bar {
  margin-bottom: 18px;
}
.reservation-table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 18px;
}
.reservation-table th, .reservation-table td {
  padding: 12px 8px;
  border-bottom: 1px solid #f0f0f0;
  text-align: center;
}
.status-tag {
  display: inline-block;
  padding: 2px 12px;
  border-radius: 12px;
  font-size: 14px;
  color: #fff;
}
.status-tag.status-pending, .status-tag.status-待审核 { background: #ff9800; }
.status-tag.status-approved, .status-tag.status-已通过 { background: #4caf50; }
.status-tag.status-rejected, .status-tag.status-已拒绝 { background: #f44336; }
.status-tag.status-cancelled, .status-tag.status-canceled, .status-tag.status-已取消 { background: #bdbdbd; }
.status-tag.status-completed, .status-tag.status-已完成 { background: #2196f3; }
button {
  background: #409eff;
  color: #fff;
  border: none;
  border-radius: 6px;
  padding: 6px 18px;
  font-size: 15px;
  cursor: pointer;
  transition: background 0.2s;
}
button:disabled {
  background: #e0e0e0;
  color: #aaa;
  cursor: not-allowed;
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
.modal {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.18);
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
  box-shadow: 0 2px 16px rgba(0,0,0,0.13);
}
.modal-content h3 {
  text-align: center;
  margin-bottom: 18px;
}
.modal-actions {
  text-align: right;
  margin-top: 12px;
}
.modal-actions button {
  background: #409eff;
  color: #fff;
  border: none;
  border-radius: 6px;
  padding: 7px 18px;
  font-size: 15px;
  cursor: pointer;
  margin-right: 12px;
  transition: background 0.2s;
}
.modal-actions button:last-child {
  background: #ff4d4f;
}
.comment-btn {
  background: #ff9800;
  margin-left: 8px;
}
.comment-btn:disabled {
  background: #e0e0e0;
  color: #aaa;
  cursor: not-allowed;
}
.form-row {
  display: flex;
  align-items: flex-start;
  margin-bottom: 18px;
  gap: 10px;
}
.form-row label {
  width: 60px;
  color: #333;
  font-size: 15px;
  margin-right: 8px;
  flex-shrink: 0;
  line-height: 32px;
}
.form-row input[type="file"] {
  border: none;
  background: #f5f7fa;
  padding: 6px 0;
  font-size: 15px;
  border-radius: 6px;
  transition: background 0.2s;
  cursor: pointer;
}
.form-row input[type="file"]:hover {
  background: #e6f0fa;
}
.error-msg {
  color: #ff4d4f;
  margin-top: 10px;
  text-align: center;
}
.upload-area {
  display: flex;
  align-items: center;
  gap: 12px;
}
.upload-btn {
  background: #409eff;
  color: #fff;
  border: none;
  border-radius: 6px;
  padding: 7px 18px;
  font-size: 15px;
  cursor: pointer;
  transition: background 0.2s;
  outline: none;
}
.upload-btn:hover {
  background: #1867c0;
}
.file-name {
  color: #888;
  font-size: 14px;
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.img-preview {
  margin-top: 10px;
  border: 1.5px solid #dbeafe;
  border-radius: 8px;
  padding: 6px;
  background: #f0f6ff;
  max-width: 140px;
  max-height: 90px;
  box-shadow: 0 2px 8px rgba(64,158,255,0.08);
  transition: box-shadow 0.2s;
  display: flex;
  align-items: center;
  justify-content: flex-start;
}
.img-preview img {
  max-width: 120px;
  max-height: 70px;
  border-radius: 6px;
  transition: transform 0.2s;
  display: block;
}
.img-preview img:hover {
  transform: scale(1.12);
  box-shadow: 0 4px 16px rgba(64,158,255,0.18);
}
.form-row input[type="number"],
.form-row textarea {
  width: 100%;
  border: 1.5px solid #e0e7ef;
  border-radius: 6px;
  padding: 8px 12px;
  font-size: 15px;
  color: #222;
  background: #f8fafc;
  transition: border 0.2s, box-shadow 0.2s;
  outline: none;
  box-sizing: border-box;
  margin-top: 2px;
}

.form-row input[type="number"]:focus,
.form-row textarea:focus {
  border: 1.5px solid #409eff;
  background: #fff;
  box-shadow: 0 2px 8px rgba(64,158,255,0.08);
}

.form-row textarea {
  resize: vertical;
  min-height: 48px;
  max-height: 120px;
}

.form-row input[type="number"]::-webkit-inner-spin-button,
.form-row input[type="number"]::-webkit-outer-spin-button {
  -webkit-appearance: none;
  margin: 0;
}
.form-row input[type="number"] {
  appearance: textfield;
}
.modal-content-2col {
  min-width: 600px;
  max-width: 700px;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(64,158,255,0.10);
  padding: 32px 32px 18px 32px;
}
.modal-form-2col {
  display: flex;
  flex-wrap: wrap;
  gap: 32px 24px;
  margin-top: 12px;
}
.form-col {
  flex: 1 1 0;
  min-width: 240px;
  display: flex;
  flex-direction: column;
  gap: 18px;
}
.form-col input[type="number"],
.form-col input[type='text'],
.form-col textarea {
  width: 100%;
  border: 1.5px solid #e0e7ef;
  border-radius: 6px;
  padding: 9px 14px;
  font-size: 15px;
  color: #222;
  background: #f8fafc;
  transition: border 0.2s, box-shadow 0.2s;
  outline: none;
  box-sizing: border-box;
}

.form-col input[type="number"]:focus,
.form-col input[type='text']:focus,
.form-col textarea:focus {
  border: 1.5px solid #409eff;
  background: #fff;
  box-shadow: 0 2px 8px rgba(64,158,255,0.08);
}

.form-col textarea {
  resize: vertical;
  min-height: 48px;
  max-height: 120px;
}

.upload-area {
  display: flex;
  align-items: center;
  gap: 12px;
}

.upload-btn {
  background: #409eff;
  color: #fff;
  border: none;
  border-radius: 6px;
  padding: 7px 18px;
  font-size: 15px;
  cursor: pointer;
  transition: background 0.2s;
  outline: none;
}
.upload-btn:hover {
  background: #1867c0;
}
.file-name {
  color: #888;
  font-size: 14px;
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.img-preview {
  margin-top: 8px;
  border: 1.5px solid #dbeafe;
  border-radius: 8px;
  padding: 6px;
  background: #f0f6ff;
  max-width: 140px;
  max-height: 90px;
  box-shadow: 0 2px 8px rgba(64,158,255,0.08);
  display: flex;
  align-items: center;
  justify-content: flex-start;
}
.img-preview img {
  max-width: 120px;
  max-height: 70px;
  border-radius: 6px;
  transition: transform 0.2s;
  display: block;
}
.img-preview img:hover {
  transform: scale(1.12);
  box-shadow: 0 4px 16px rgba(64,158,255,0.18);
}

.modal-actions-2col {
  width: 100%;
  display: flex;
  justify-content: center;
  gap: 32px;
  margin-top: 18px;
}
.modal-button {
  background: #409eff;
  color: #fff;
  border: none;
  border-radius: 6px;
  padding: 7px 22px;
  font-size: 16px;
  cursor: pointer;
  transition: background 0.2s;
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

.error-msg {
  color: #ff4d4f;
  margin-top: 10px;
  text-align: center;
  font-size: 15px;
}
</style> 