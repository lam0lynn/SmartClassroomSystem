<template>
  <div class="rece-classroom-list-container">
    <h2>教室列表</h2>
    <div class="filter-bar">
      <label>类型：</label>
      <select v-model="filterType" @change="fetchClassrooms">
        <option value="">全部</option>
        <option v-for="t in typeOptions" :key="t" :value="t">{{ t }}</option>
      </select>
      <label style="margin-left:18px;">状态：</label>
      <select v-model="filterStatus" @change="fetchClassrooms">
        <option value="">全部</option>
        <option v-for="s in statusOptions" :key="s" :value="s">{{ statusMap[s] || s }}</option>
      </select>
    </div>
    <div class="classroom-list">
      <div
        v-for="c in pagedClassrooms"
        :key="c.id"
        class="classroom-card"
        @click="openReviewDialog(c)"
        style="cursor:pointer"
      >
        <img v-if="c.imageUrl" :src="getImgUrl(c.imageUrl)" alt="图片" class="classroom-img" />
        <div class="classroom-info">
          <div class="classroom-title">
            {{ c.name }}
            <span class="classroom-status" :class="'status-' + c.status">{{ statusMap[c.status] || c.status }}</span>
          </div>
          <!-- <div>楼栋：{{ c.building }} 楼层：{{ c.floor }} 容量：{{ c.capacity }}</div>
          <div>
            类型：{{ c.type }}
            设备：{{ c.equipment }}
          </div>
          <div>描述：{{ c.description }}</div> -->
        </div> 
        <button
          class="reserve-btn"
          @click.stop="openReserve(c)"
          :disabled="c.status !== 'available' && c.status !== '空闲'"
        >预约</button>
      </div>
    </div>
    <div class="pagination-bar">
      <button :disabled="page === 1" @click="page--">上一页</button>
      <span>第 {{ page }} / {{ totalPages }} 页</span>
      <button :disabled="page === totalPages" @click="page++">下一页</button>
    </div>
    <!-- 预约弹窗 -->
    <div v-if="reserveDialog.show" class="modal">
      <div class="modal-content">
        <h3>预约教室：{{ reserveDialog.classroom?.name }}</h3>
        <form @submit.prevent="submitReserve">
          <div class="form-row">
            <label>开始时间：</label>
            <input v-model="reserveForm.start_time" type="datetime-local" required />
          </div>
          <div class="form-row">
            <label>结束时间：</label>
            <input v-model="reserveForm.end_time" type="datetime-local" required />
          </div>
          <div class="form-row">
            <label>用途：</label>
            <input v-model="reserveForm.purpose" placeholder="请输入用途" required />
          </div>
          <div class="form-row">
            <label>人数：</label>
            <input v-model="reserveForm.participants" type="number" min="1" required />
          </div>
          <div class="modal-actions">
            <button type="submit" class="modal-button submit-button" :disabled="reserveLoading">提交</button>
            <button type="button" class="modal-button cancel-button" @click="closeReserve">取消</button>
          </div>
        </form>
        <div v-if="reserveError" class="error-msg">{{ reserveError }}</div>
      </div>
    </div>

    <!-- 教室评论弹窗 -->
    <div v-if="reviewDialogVisible" class="modal">
      <div class="modal-content review-modal-content">
        <div class="review-header">
          <h3>教室详情与评价</h3>
          <button class="modal-close-btn" @click="reviewDialogVisible = false">×</button>
        </div>
        <div v-if="selectedClassroom" class="review-classroom-info">
          <img v-if="selectedClassroom.imageUrl" :src="getImgUrl(selectedClassroom.imageUrl)" class="review-classroom-img" />
          <div class="review-classroom-meta">
            <div><b>{{ selectedClassroom.name }}</b></div>
            <div>楼栋：{{ selectedClassroom.building }} 楼层：{{ selectedClassroom.floor }}</div>
            <div>容量：{{ selectedClassroom.capacity }} 类型：{{ selectedClassroom.type }}</div>
            <div>描述：{{ selectedClassroom.description }}</div>
          </div>
        </div>
        <div class="review-list-section">
          <h4>教室评价</h4>
          <div v-if="reviewLoading" style="text-align:center;color:#888;">加载中...</div>
          <div v-else-if="reviewList.length === 0" style="text-align:center;color:#888;">暂无评价</div>
          <div v-else>
            <div v-for="r in reviewList" :key="r.id" class="review-item">
              <div class="review-meta">
                <img v-if="r.avatar" :src="getImgUrl(r.avatar)" class="review-avatar" />
                <span class="review-user">{{ r.realName || r.username || '匿名' }}</span>
                <span class="review-rating">评分: {{ r.rating }} / 5</span>
                <span class="review-time">{{ r.createTime?.slice(0,16).replace('T',' ') }}</span>
              </div>
              <div class="review-content">{{ r.content }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue';
import { getAllClassrooms, getAllClassroomsType, getAllClassroomsStatus } from '@/api/classroom';
import { getAllClassroomReviews } from '@/api/review';
import axios from '@/api';

const classrooms = ref([]);
const reserveDialog = reactive({ show: false, classroom: null });
const reserveForm = reactive({ start_time: '', end_time: '', purpose: '', participants: '' });
const reserveLoading = ref(false);
const reserveError = ref('');

// 动态类型选项（字符串数组）
const typeOptions = ref([]);
// 动态状态选项（字符串数组）
const statusOptions = ref([]);
// 动态状态映射
const statusMap = ref({});

const filterType = ref('');
const filterStatus = ref('');
const page = ref(1);
const pageSize = 3;

const pagedClassrooms = computed(() => {
  let filtered = classrooms.value;
  if (filterType.value) filtered = filtered.filter(c => c.type === filterType.value);
  if (filterStatus.value) filtered = filtered.filter(c => c.status === filterStatus.value);
  totalPages.value = Math.max(1, Math.ceil(filtered.length / pageSize));
  const start = (page.value - 1) * pageSize;
  return filtered.slice(start, start + pageSize);
});
const totalPages = ref(1);

watch([filterType, classrooms], () => {
  page.value = 1;
}, { immediate: true });

watch(page, () => {
  // 保证页码不越界
  if (page.value < 1) page.value = 1;
  if (page.value > totalPages.value) page.value = totalPages.value;
});

function getImgUrl(url) {
  if (!url) return '';
  if (url.startsWith('http')) return url;
  return url.startsWith('/') ? url : '/' + url;
}

const fetchClassrooms = async () => {
  const res = await getAllClassrooms();
  classrooms.value = res.data || [];
};

// 获取类型选项
const fetchTypeOptions = async () => {
  const res = await getAllClassroomsType();
  typeOptions.value = res.data || [];
};
// 获取状态选项和映射
const fetchStatusOptions = async () => {
  const res = await getAllClassroomsStatus();
  // 支持返回 ["available","maintenance"] 或 [{value,label}]
  if (Array.isArray(res.data)) {
    if (typeof res.data[0] === 'object') {
      statusOptions.value = res.data.map(i => i.value);
      statusMap.value = Object.fromEntries(res.data.map(i => [i.value, i.label]));
    } else {
      statusOptions.value = res.data;
      // 默认映射
      statusMap.value = {};
      res.data.forEach(s => {
        if (s === 'available') statusMap.value[s] = '空闲';
        else if (s === 'maintenance') statusMap.value[s] = '维修';
        else if (s === 'closed') statusMap.value[s] = '关闭';
        else statusMap.value[s] = s;
      });
    }
  }
};

function openReserve(classroom) {
  // 仅允许英文 available 状态预约
  // 兼容中英文，优先判断英文
  if (
    classroom.status !== 'available' &&
    classroom.status !== '空闲'
  ) return;
  reserveDialog.show = true;
  reserveDialog.classroom = classroom;
  reserveForm.start_time = '';
  reserveForm.end_time = '';
  reserveForm.purpose = '';
  reserveForm.participants = '';
  reserveError.value = '';
}

function closeReserve() {
  reserveDialog.show = false;
  reserveDialog.classroom = null;
  reserveError.value = '';
}

async function submitReserve() {
  if (!reserveDialog.classroom) return;
  reserveLoading.value = true;
  reserveError.value = '';
  try {
    // 获取当前登录用户ID
    const user = JSON.parse(sessionStorage.getItem('user') || '{}');
    if (!user.id) {
      reserveError.value = '请先登录';
      reserveLoading.value = false;
      return;
    }
    const data = {
      classroomId: reserveDialog.classroom.id,
      userId: user.id,
      startTime: reserveForm.start_time,
      endTime: reserveForm.end_time,
      purpose: reserveForm.purpose,
      participants: reserveForm.participants,
      status: 'pending',
    };
    await axios.post('/reservations/add', data);
    reserveDialog.show = false;
    // 触发"我的预约"页刷新（可用事件总线/全局状态/或页面监听）
    window.dispatchEvent(new Event('reservation-updated'));
    // 可选：提示成功
    alert('预约成功！请等待审核。');
  } catch (e) {
    reserveError.value = e?.response?.data?.message || '预约失败';
  } finally {
    reserveLoading.value = false;
  }
}

// 评论弹窗相关
const reviewDialogVisible = ref(false);
const reviewList = ref([]);
const reviewLoading = ref(false);
const selectedClassroom = ref(null);

async function fetchReviews(classroomId) {
  reviewLoading.value = true;
  reviewList.value = [];
  try {
    const res = await getAllClassroomReviews(classroomId);
    // 调试输出
    console.log('评论接口返回:', res.data);
    reviewList.value = (res.data || []).map(r => ({
      ...r,
      avatar: r.avatar || '',
      username: r.username || '',
      realName: r.realName || '',
      createTime: r.createTime || r.create_time || '',
      rating: r.rating || 0,
      content: r.content || ''
    }));
  } catch (e) {
    console.error('获取评论失败', e);
    reviewList.value = [];
  } finally {
    reviewLoading.value = false;
  }
}

function openReviewDialog(classroom) {
  selectedClassroom.value = classroom;
  reviewDialogVisible.value = true;
  fetchReviews(classroom.id);
}

onMounted(async () => {
  await fetchTypeOptions();
  await fetchStatusOptions();
  await fetchClassrooms();
});
</script>

<style scoped>
.rece-classroom-list-container {
  width: 1100px;
  margin: 100px 200px;
  padding: 32px 24px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 4px 24px rgba(0,0,0,0.08);
}
.classroom-list {
  display: flex;
  flex-wrap: wrap;
  gap: 28px;
  justify-content: flex-start;
}
.classroom-card {
  width: 320px;
  background: #f8fafd;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  padding: 18px 18px 16px 18px;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  position: relative;
}
.classroom-img {
  width: 100%;
  height: 120px;
  object-fit: cover;
  border-radius: 8px;
  margin-bottom: 12px;
}
.classroom-info {
  flex: 1;
  width: 100%;
  margin-bottom: 10px;
}
.classroom-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 4px;
}
.classroom-status {
  font-size: 13px;
  margin-left: 8px;
  padding: 2px 8px;
  border-radius: 8px;
  background: #e6f7ff;
  color: #1890ff;
}
.status-maintenance {
  background: #fff1f0;
  color: #ff4d4f;
}
.status-closed {
  background: #f0f0f0;
  color: #aaa;
}
.status-available {
  background: #f6ffed;
  color: #52c41a;
}
.reserve-btn {
  align-self: flex-end;
  background: #409eff;
  color: #fff;
  border: none;
  border-radius: 6px;
  padding: 7px 18px;
  font-size: 15px;
  cursor: pointer;
  transition: background 0.2s;
  margin-top: 8px;
}
.reserve-btn:hover {
  background: #1867c0;
}
.reserve-btn:disabled {
  background: #e0e0e0;
  color: #aaa;
  cursor: not-allowed;
  box-shadow: none;
}
.modal {
  position: fixed;
  left: 0; top: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.18);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}
.modal-content {
  background: #fff;
  border-radius: 12px;
  padding: 32px 36px 24px 36px;
  min-width: 340px;
  box-shadow: 0 4px 24px rgba(0,0,0,0.12);
}
.review-modal-content {
  min-width: 600px;
  max-width: 800px;
  padding: 24px 32px 24px 32px;
  position: relative;
}
.form-row {
  display: flex;
  align-items: center;
  margin-bottom: 18px;
}
.form-row label {
  width: 80px;
  color: #333;
  font-size: 15px;
  margin-right: 8px;
}
.form-row input {
  flex: 1;
  padding: 7px 12px;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  font-size: 15px;
  background: #fafbfc;
  outline: none;
  transition: border 0.2s;
}
.form-row input:focus {
  border: 1.5px solid #409eff;
}
.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 18px;
  margin-top: 18px;
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
}
.filter-bar {
  display: flex;
  align-items: center;
  margin-bottom: 24px;
  gap: 12px;
  background: #f6f8fa;
  padding: 14px 22px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  width: fit-content;
  margin-left: auto;
  margin-right: 0;
}
.filter-bar label {
  font-size: 15px;
  color: #333;
  margin-right: 6px;
  font-weight: 500;
}
.filter-bar select {
  padding: 7px 18px;
  border: 1.5px solid #e0e0e0;
  border-radius: 8px;
  outline: none;
  font-size: 15px;
  background: #fff;
  box-shadow: 0 1px 4px rgba(64,158,255,0.06);
  transition: border 0.2s, box-shadow 0.2s;
  cursor: pointer;
}
.filter-bar select:focus, .filter-bar select:hover {
  border: 1.5px solid #409eff;
  box-shadow: 0 2px 8px rgba(64,158,255,0.12);
}
.pagination-bar {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 18px;
  margin: 32px 0 0 0;
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
.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.modal-close-btn {
  background: none;
  border: none;
  font-size: 26px;
  color: #888;
  cursor: pointer;
  border-radius: 50%;
  width: 32px;
  height: 32px;
  line-height: 32px;
  text-align: center;
  transition: background 0.2s;
}
.modal-close-btn:hover {
  background: #f0f0f0;
}
.review-classroom-info {
  display: flex;
  gap: 18px;
  margin-bottom: 18px;
}
.review-classroom-img {
  width: 120px;
  height: 90px;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid #eee;
}
.review-classroom-meta {
  flex: 1;
  font-size: 15px;
  color: #444;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.review-list-section {
  margin-top: 10px;
}
.review-item {
  border-bottom: 1px solid #f0f0f0;
  padding: 12px 0 10px 0;
}
.review-meta {
  font-size: 13px;
  color: #888;
  display: flex;
  gap: 10px;
  align-items: center;
  margin-bottom: 4px;
}
.review-user {
  font-weight: 500;
  color: #333;
}
.review-content {
  font-size: 15px;
  color: #333;
  margin-bottom: 6px;
}
.review-images {
  display: flex;
  gap: 8px;
  margin-bottom: 4px;
}
.review-img {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 6px;
  border: 1px solid #eee;
}
.review-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
  margin-right: 8px;
  border: 1px solid #eee;
  background: #f5f5f5;
  vertical-align: middle;
}
</style>