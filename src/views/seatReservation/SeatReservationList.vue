<template>
  <div class="user-list-container">
    <h2>座位预约查询</h2>
    <!-- 筛选条件 -->
    <form @submit.prevent="applyFilter" class="filter-form">
      <input 
        v-model="filter.userName" 
        type="text" 
        placeholder="输入用户名搜索" 
        @input="onUserNameInput"
      />
      
      <select v-model="filter.classroomId" @change="onClassroomChange">
        <option value="">选择教室</option>
        <option v-for="classroom in classroomList" :key="classroom.id" :value="classroom.id">
          {{ classroom.name }}
        </option>
      </select>
      
      <select v-model="filter.status">
        <option value="">全部状态</option>
        <option value="pending">待审批</option>
        <option value="approved">已通过</option>
        <option value="rejected">已拒绝</option>
        <option value="canceled">已取消</option>
        <option value="completed">已完成</option>
      </select>

      <!-- 预约日期（按天匹配） -->
      <input 
        v-model="filter.reserveDate" 
        type="date" 
        placeholder="预约日期"
      />

      <button type="submit">查询</button>
      <button type="button" @click="resetFilter">重置</button>
      <button type="button" @click="exportCsv">导出表</button>
    </form>

    <!-- 表格 -->
    <table>
      <thead>
        <tr>
          <th>ID</th>
          <th>用户</th>
          <th>教室</th>
          <th>座位号</th>
          <th>预约时间</th>
          <th>状态</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="r in pagedList" :key="r.id">
          <td>{{ r.id }}</td>
          <td>{{ getUserName(r.userId) || r.userId }}</td>
          <td>{{ getClassroomNameBySeat(r.seatId) || '-' }}</td>
          <td>{{ getSeatNumber(r.seatId) || '-' }}</td>
          <td>{{ r.reserveTime }}</td>
          <td>{{ r.status }}</td>
        </tr>
      </tbody>
    </table>

    <!-- 分页 -->
    <div class="pagination">
      <button :disabled="page === 1" @click="page--" class="pagination-button prev-button">上一页</button>
      <span>第 {{ page }} 页 / 共 {{ totalPages }} 页</span>
      <button :disabled="page === totalPages" @click="page++" class="pagination-button next-button">下一页</button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { getAllReservations } from '@/api/seatreservation';
import { getAllUsers } from '@/api/user';
import { getAllClassrooms } from '@/api/classroom';
import { getAllSeats } from '@/api/seat';

const list = ref([]);
const error = ref('');
const loading = ref(false);
const filter = reactive({ 
  userName: '', 
  status: '',
  userId: '', // 内部存储选中的用户ID
  classroomId: '', // 内部存储选中的教室ID
  reserveDate: '' // yyyy-MM-dd
});
const page = ref(1);
const pageSize = ref(10);

// 名称映射
const userMap = ref({}); // id -> name
const classroomMap = ref({}); // id -> name
const seatInfoMap = ref({}); // seatId -> { seatNumber, classroomId }
const seatList = ref([]);

// 下拉选择相关
const classroomList = ref([]);

// 名称工具
function getUserName(userId) { return userMap.value?.[userId]; }
function getClassroomName(classroomId) { return classroomMap.value?.[classroomId]; }
function getClassroomNameBySeat(seatId) { const info = seatInfoMap.value?.[seatId]; return info ? getClassroomName(info.classroomId) : undefined; }
function getSeatNumber(seatId) { return seatInfoMap.value?.[seatId]?.seatNumber; }

// 用户名输入处理
function onUserNameInput() {
  // 用户名输入时的处理逻辑，用于模糊查询
}

// 教室选择处理
function onClassroomChange() {
  // 教室ID变化时的处理逻辑
}

// 工具：取日期部分
function dateOnly(v){
  return typeof v === 'string' ? v.replaceAll('/','-').slice(0,10) : '';
}

async function loadMaps() {
  const [usersRes, classroomsRes, seatsRes] = await Promise.all([
    getAllUsers(),
    getAllClassrooms(),
    getAllSeats(),
  ]);
  userMap.value = {};
  (usersRes.data || []).forEach(u => { userMap.value[u.id] = u.realName || u.username; });
  classroomMap.value = {};
  classroomList.value = classroomsRes.data || [];
  (classroomsRes.data || []).forEach(c => { classroomMap.value[c.id] = c.name; });
  seatInfoMap.value = {};
  seatList.value = seatsRes.data || [];
  seatList.value.forEach(s => { seatInfoMap.value[s.id] = { seatNumber: s.seatNumber, classroomId: s.classroomId }; });
}

async function loadAllReservations() {
  loading.value = true;
  try {
    const { data } = await getAllReservations();
    list.value = data || [];
    page.value = 1;
  } catch (e) {
    console.error('加载所有预约失败:', e);
    list.value = [];
  } finally {
    loading.value = false;
  }
}

const filteredList = computed(() => {
  let d = list.value;
  if (filter.userName) {
    d = d.filter(i => {
      const userName = getUserName(i.userId);
      return userName && userName.toLowerCase().includes(filter.userName.toLowerCase());
    });
  }
  if (filter.classroomId) d = d.filter(i => {
    const seatInfo = seatInfoMap.value[i.seatId];
    return seatInfo && String(seatInfo.classroomId) === String(filter.classroomId);
  });
  if (filter.status) d = d.filter(i => String(i.status) === String(filter.status));

  // 按日期匹配
  if (filter.reserveDate) {
    d = d.filter(i => dateOnly(i.reserveTime) === filter.reserveDate);
  }
  return d;
});

const totalPages = computed(() => Math.ceil(filteredList.value.length / pageSize.value) || 1);
const pagedList = computed(() => {
  const start = (page.value - 1) * pageSize.value;
  return filteredList.value.slice(start, start + pageSize.value);
});

function applyFilter() { page.value = 1; }
function resetFilter() { 
  filter.userName = ''; 
  filter.status = ''; 
  filter.classroomId = ''; 
  filter.reserveDate = '';
  page.value = 1; 
}

// 导出为 CSV，Excel 可直接打开
function exportCsv() {
  const rows = [
    ['ID', '用户', '教室', '座位号', '预约时间', '状态'],
    ...filteredList.value.map(i => [
      i.id,
      getUserName(i.userId) || i.userId,
      getClassroomNameBySeat(i.seatId) || '-',
      getSeatNumber(i.seatId) || '-',
      i.reserveTime || '',
      i.status || ''
    ])
  ];
  const escape = (v) => {
    const s = String(v ?? '');
    // 若包含逗号、引号或换行，则用双引号包裹并转义内部引号
    if (/[",\n]/.test(s)) {
      return '"' + s.replace(/"/g, '""') + '"';
    }
    return s;
  };
  const csv = rows.map(r => r.map(escape).join(',')).join('\r\n');
  const blob = new Blob(["\uFEFF" + csv], { type: 'text/csv;charset=utf-8;' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  const date = new Date();
  const ymd = `${date.getFullYear()}-${String(date.getMonth()+1).padStart(2,'0')}-${String(date.getDate()).padStart(2,'0')}`;
  a.href = url;
  a.download = `座位预约_${ymd}.csv`;
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  URL.revokeObjectURL(url);
}

onMounted(async () => { await loadMaps(); await loadAllReservations(); });
</script>

<style scoped>
.user-list-container {
  width: 900px;
  margin: 100px 300px ;
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

.filter-form input, .filter-form select {
  padding: 7px 14px;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  outline: none;
  font-size: 15px;
  background: #fafbfc;
  transition: border 0.2s;
}

.filter-form input:focus, .filter-form select:focus {
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
</style>


