<template>
  <div class="my-seat-reservation">
    <h2>座位预约管理</h2>

    <!-- 预约座位部分（始终显示） -->
    <div class="reserve-section">
      <!-- 筛选条件 -->
      <form @submit.prevent="applyFilter" class="filter-form">
        <select
          v-model="filter.classroomId"
          @change="onClassroomChange"
          class="filter-select"
        >
          <option value="">选择教室</option>
          <option
            v-for="classroom in classroomList"
            :key="classroom.id"
            :value="classroom.id"
          >
            {{ classroom.name }}
          </option>
        </select>
        <input type="date" v-model="filter.reserveDate" class="filter-input" />
        <button type="submit" class="filter-button">查询</button>
        <button
          type="button"
          @click="resetFilter"
          class="filter-button reset-button"
        >
          重置
        </button>
      </form>

      <!-- 座位表 -->
      <div v-if="seatList.length" class="seat-section">
        <h4 class="seat-title">座位图</h4>

        <div class="seat-grid" :style="gridStyle">
          <!-- 动态生成网格，包含所有可能的列，空位置显示过道 -->
          <template v-for="row in actualRows" :key="`row-${row}`">
            <template v-for="col in 11" :key="`col-${col}`">
              <div class="grid-cell">
                <!-- 如果有座位数据，显示座位 -->
                <div
                  v-if="getSeatAtPosition(row, col)"
                  :class="[
                    'seat-item',
                    getSeatStatus(getSeatAtPosition(row, col).id),
                    { loading: isReserving },
                  ]"
                  @click="handleSeatClick(row, col)"
                  :title="getSeatTooltip(getSeatAtPosition(row, col))"
                  :style="getSeatItemStyle(getSeatAtPosition(row, col))"
                >
                  <span>{{ getSeatAtPosition(row, col).seatNumber }}</span>
                  <span
                    v-if="
                      getSeatStatus(getSeatAtPosition(row, col).id) ===
                      'reserved'
                    "
                    class="status-text"
                    >（已预约）</span
                  >
                  <span
                    v-else-if="
                      getSeatStatus(getSeatAtPosition(row, col).id) === 'mine'
                    "
                    class="status-text"
                    >（我的预约）</span
                  >
                  <span v-else class="status-text">（可预约）</span>
                  
                  <!-- 加载指示器 -->
                  <div v-if="isReserving" class="loading-indicator">
                    <div class="spinner"></div>
                  </div>
                </div>
                <!-- 如果没有座位数据，显示过道 -->
                <div v-else class="aisle-cell">
                  <span class="aisle-text">过道</span>
                </div>
              </div>
            </template>
          </template>
        </div>
        <div class="seat-legend">
          <span class="legend-item mine">
            <span class="legend-box mine"></span>我的预约
          </span>
          <span class="legend-item aisle">
            <span class="legend-box aisle"></span>过道
          </span>
        </div>
      </div>
    </div>
  </div>

  <!-- 通用弹窗：新建/编辑预约 -->
  <div v-if="dialogVisible" class="dialog-mask" @click.self="closeDialog">
    <div class="dialog">
      <div class="dialog-header">
        <h3 class="dialog-title">{{ dialogMode === 'edit' ? '编辑我的预约' : '提交预约' }}</h3>
        <button class="dialog-close" @click="closeDialog">×</button>
      </div>
      <div class="dialog-body">
        <div class="dialog-field">
          <label class="dialog-label">座位号</label>
          <div class="dialog-value">{{ dialogSeat?.seatNumber }}</div>
        </div>
        <div class="dialog-field">
          <label class="dialog-label">日期与时间</label>
          <input class="dialog-input" type="datetime-local" v-model="dialogDateTime" />
        </div>
      </div>
      <div class="dialog-actions">
        <button class="btn primary" @click="submitDialog">{{ dialogMode === 'edit' ? '保存' : '提交预约' }}</button>
        <button class="btn ghost" @click="closeDialog">取消</button>
        <button v-if="dialogMode === 'edit'" class="btn ghost" @click="doCancelReservation">取消预约</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from "vue";
import { getAllClassrooms } from "@/api/classroom";
import { getAllSeats } from "@/api/seat";
import {
  getReservationList,
  reserveSeat,
  deleteReservation,
  getAllReservations,
} from "@/api/seatreservation";

const classroomList = ref([]);
const seatList = ref([]);
const seatInfoMap = ref({});
const filter = reactive({
  classroomId: "",
  reserveDate: new Date().toISOString().slice(0, 10),
});
const myReservations = ref([]);
const allReservations = ref([]); // 当前教室和日期下所有预约
const isReserving = ref(false); // 预约加载状态

// 通用弹窗状态
const dialogVisible = ref(false);
const dialogMode = ref('create'); // 'create' | 'edit'
const dialogSeat = ref(null);
const dialogDateTime = ref("");

function getClassroomNameBySeat(seatId) {
  const info = seatInfoMap.value[seatId];
  if (!info) return "-";
  const classroom = classroomList.value.find((c) => c.id === info.classroomId);
  return classroom ? classroom.name : "-";
}
function getSeatNumber(seatId) {
  return seatInfoMap.value[seatId]?.seatNumber || "-";
}

async function loadClassrooms() {
  const res = await getAllClassrooms();
  classroomList.value = res.data || [];
}
async function loadSeats() {
  const res = await getAllSeats();
  seatList.value = res.data || [];
  seatInfoMap.value = {};
  seatList.value.forEach((s) => {
    seatInfoMap.value[s.id] = {
      seatNumber: s.seatNumber,
      classroomId: s.classroomId,
    };
  });
}
async function loadMyReservations() {
  const userId = getCurrentUserId();
  if (!userId) return;
  const res = await getReservationList(userId);
  myReservations.value = res.data || [];
}
async function loadAllReservations() {
  // 获取当前教室和日期下所有预约
  if (!filter.classroomId || !filter.reserveDate) {
    allReservations.value = [];
    return;
  }
  // 查询所有预约后前端筛选（如无后端接口支持）
  const res = await getAllReservations();
  // 归一化比较：后端可能返回 datetime（含时分秒），我们仅按同一天比较
  const selectedDate = filter.reserveDate;
  const toDateOnly = (v) => (typeof v === 'string' ? v.slice(0, 10) : '');
  allReservations.value = (res.data || []).filter(r =>
    String(r.classroomId) === String(filter.classroomId) &&
    toDateOnly(r.reserveTime) === selectedDate
  );
}

const filteredSeats = computed(() => {
  if (!filter.classroomId) return [];
  // 过滤当前教室的座位，并按座位号排序
  const seats = seatList.value.filter(
    (s) => String(s.classroomId) === String(filter.classroomId)
  );
  return seats.sort((a, b) => {
    // 先按行字母排序，再按列数字排序
    const rowA = a.seatNumber.charAt(0).toUpperCase();
    const rowB = b.seatNumber.charAt(0).toUpperCase();
    if (rowA !== rowB) {
      return rowA.localeCompare(rowB);
    }
    return parseInt(a.seatNumber.slice(1)) - parseInt(a.seatNumber.slice(1));
  });
});

// 计算实际的行数和列数
const actualRows = computed(() => {
  if (filteredSeats.value.length === 0) return [];
  const rows = new Set();
  filteredSeats.value.forEach((seat) => {
    const rowLetter = seat.seatNumber.charAt(0).toUpperCase();
    rows.add(rowLetter);
  });
  return Array.from(rows).sort();
});

// 动态网格样式
const gridStyle = computed(() => {
  return {
    display: "grid",
    gridTemplateColumns: "repeat(11, 50px)", // 固定11列
    gridTemplateRows: `repeat(${actualRows.value.length}, 45px)`,
    gap: "4px",
    justifyContent: "center",
  };
});

// 获取座位状态
function getSeatStatus(seatId) {
  const toDateOnly = (v) => (typeof v === 'string' ? v.slice(0, 10) : '');
  const selectedDate = filter.reserveDate;
  const userId = getCurrentUserId();
  const mine = myReservations.value.find(r => r.seatId === seatId && toDateOnly(r.reserveTime) === selectedDate && r.userId == userId);
  if (mine) return 'mine';
  const reserved = allReservations.value.find(r => r.seatId === seatId && toDateOnly(r.reserveTime) === selectedDate);
  if (reserved) return 'reserved';
  return '';
}

// 获取座位样式，根据尾数对齐
function getSeatStyle(seat) {
  const seatNumber = seat.seatNumber;

  // 解析座位号，支持 A1, B2 等格式
  const rowLetter = seatNumber.charAt(0).toUpperCase();
  const colNumber = parseInt(seatNumber.slice(1));

  // 计算行索引（A=0, B=1, C=2...）
  const rowIndex = rowLetter.charCodeAt(0) - 65;

  // 计算列索引（1-11）
  const colIndex = colNumber - 1;

  // 设置网格位置
  const borderStyle = {
    gridRow: rowIndex + 1,
    gridColumn: colIndex + 1,
  };

  // 根据列位置决定边框显示
  if (colIndex % 2 === 0) {
    // 偶数列，隐藏右边框
    borderStyle.borderRight = "none";
  } else {
    // 奇数列，隐藏左边框
    borderStyle.borderLeft = "none";
  }

  return borderStyle;
}

// 获取指定位置的座位
function getSeatAtPosition(row, col) {
  const rowLetter = row; // row 现在是实际的字母，如 'A', 'B'
  const seatNumber = `${rowLetter}${col}`;
  return filteredSeats.value.find((seat) => seat.seatNumber === seatNumber);
}

// 获取座位提示信息
function getSeatTooltip(seat) {
  const status = getSeatStatus(seat.id);
  if (status === "reserved") {
    return `座位 ${seat.seatNumber} 已预约`;
  } else if (status === "mine") {
    return `座位 ${seat.seatNumber} 是你的预约`;
  } else {
    return `座位 ${seat.seatNumber} 可预约`;
  }
}

// 获取座位项样式
function getSeatItemStyle(seat) {
  const status = getSeatStatus(seat.id);
  const baseStyle = {
    cursor: status === "reserved" ? "not-allowed" : "pointer",
    opacity: status === "reserved" ? 0.6 : 1,
  };

  // 如果是我的预约，添加特殊样式
  if (status === "mine") {
    baseStyle.boxShadow = "0 0 8px rgba(64, 158, 255, 0.5)";
  }

  return baseStyle;
}

// 显示成功提示
function showSuccessMessage(message) {
  const successDiv = document.createElement("div");
  successDiv.className = "success-message";
  successDiv.textContent = message;
  successDiv.style.cssText = `
    position: fixed;
    top: 20px;
    right: 20px;
    background: #52c41a;
    color: white;
    padding: 12px 20px;
    border-radius: 6px;
    box-shadow: 0 4px 12px rgba(0,0,0,0.15);
    z-index: 9999;
    font-size: 14px;
    animation: slideIn 0.3s ease-out;
  `;
  const style = document.createElement("style");
  style.textContent = `
    @keyframes slideIn {
      from { transform: translateX(100%); opacity: 0; }
      to { transform: translateX(0); opacity: 1; }
    }
    @keyframes slideOut {
      from { transform: translateX(0); opacity: 1; }
      to { transform: translateX(100%); opacity: 0; }
    }
  `;
  document.head.appendChild(style);
  document.body.appendChild(successDiv);
  setTimeout(() => {
    successDiv.style.animation = "slideOut 0.3s ease-in";
    setTimeout(() => {
      if (successDiv.parentNode) {
        successDiv.parentNode.removeChild(successDiv);
      }
    }, 300);
  }, 3000);
}

// 获取当前登录用户ID（从 sessionStorage）
function getCurrentUserId() {
  try {
    const userStr = sessionStorage.getItem("user");
    if (!userStr) return null;
    const user = JSON.parse(userStr);
    return user?.id || user?.userId || null;
  } catch {
    return null;
  }
}

// 点击座位：可预约 -> 打开新建弹窗；我的预约 -> 打开编辑弹窗；已预约 -> 提示
async function handleSeatClick(row, col) {
  const seat = getSeatAtPosition(row, col);
  if (!seat) return;
  const status = getSeatStatus(seat.id);
  const base = filter.reserveDate || new Date().toISOString().slice(0, 10);
  if (status === 'mine') {
    dialogMode.value = 'edit';
    dialogSeat.value = seat;
    const mine = myReservations.value.find(r => r.seatId === seat.id);
    dialogDateTime.value = mine?.reserveTime ? String(mine.reserveTime).replace(' ', 'T').slice(0,16) : `${base}T08:00`;
    dialogVisible.value = true;
    return;
  }
  if (status === 'reserved') {
    alert('该座位已被预约');
    return;
  }
  dialogMode.value = 'create';
  dialogSeat.value = seat;
  dialogDateTime.value = `${base}T08:00`;
  dialogVisible.value = true;
}

function closeDialog() {
  dialogVisible.value = false;
  dialogSeat.value = null;
}

// 将 yyyy-MM-ddTHH:mm 或包含空格的字符串格式化为 yyyy-MM-ddTHH:mm:00
function normalizeDateTime(v) {
  if (!v) return '';
  let s = String(v).trim();
  s = s.replaceAll('/', '-').replace(' ', 'T');
  if (s.length === 16) return s + ':00';
  return s;
}

async function submitDialog() {
  if (!dialogSeat.value) return;
  if (!dialogDateTime.value) {
    alert("请选择预约时间（到分钟）");
    return;
  }
  const userId = getCurrentUserId();
  const dateTimeISO = normalizeDateTime(dialogDateTime.value);
  const seatNo = dialogSeat.value ? dialogSeat.value.seatNumber : '';
  try {
    isReserving.value = true;
    if (dialogMode.value === 'edit') {
      const mine = myReservations.value.find(r => r.seatId === dialogSeat.value.id);
      if (mine && mine.id) await deleteReservation(mine.id);
    }
    const payload = {
      userId: Number(userId),
      seatId: Number(dialogSeat.value.id),
      classroomId: Number(filter.classroomId),
      reserveTime: dateTimeISO,
      user_id: Number(userId),
      seat_id: Number(dialogSeat.value.id),
      classroom_id: Number(filter.classroomId),
      reserve_time: dateTimeISO,
    };
    await reserveSeat(payload);
    await Promise.all([loadMyReservations(), loadAllReservations()]);
    // 先提示，再关闭，避免读取已清空的 dialogSeat 造成报错
    showSuccessMessage(dialogMode.value === 'edit' ? '已保存预约时间' : `预约成功！座位号：${seatNo}`);
    closeDialog();
  } catch (e) {
    console.error('提交失败', e);
    alert(e.response?.data?.message || '提交失败');
  } finally {
    isReserving.value = false;
  }
}

// 从编辑弹窗直接取消预约
async function doCancelReservation() {
  try {
    const seat = dialogSeat.value;
    if (!seat) return;
    const mine = myReservations.value.find(r => r.seatId === seat.id);
    if (!mine) return;
    if (!confirm('确认取消该预约吗？')) return;
    await deleteReservation(mine.id);
    await Promise.all([loadMyReservations(), loadAllReservations()]);
    closeDialog();
    showSuccessMessage('已取消预约');
  } catch (e) {
    console.error('取消预约失败', e);
    alert(e.response?.data?.message || '取消预约失败');
  }
}

function applyFilter() {
  loadAllReservations();
}
function resetFilter() {
  filter.classroomId = "";
  filter.reserveDate = new Date().toISOString().slice(0, 10);
  allReservations.value = [];
}

function onClassroomChange() {
  loadAllReservations();
}

// 统计预约数量
function getReservedCount() {
  const toDateOnly = (v) => (typeof v === 'string' ? v.slice(0, 10) : '');
  return allReservations.value.filter(r => toDateOnly(r.reserveTime) === filter.reserveDate).length;
}

function getAvailableCount() {
  return filteredSeats.value.length - getReservedCount();
}

function getMyReservedCount() {
  const toDateOnly = (v) => (typeof v === 'string' ? v.slice(0, 10) : '');
  return myReservations.value.filter(r => toDateOnly(r.reserveTime) === filter.reserveDate).length;
}

// 显示预约数据
function showReservationData() {
  if (!filter.classroomId || !filter.reserveDate) {
    alert("请先选择教室和日期");
    return;
  }

  const sampleSeat = filteredSeats.value[0];
  if (!sampleSeat) {
    alert("当前教室没有可用座位");
    return;
  }

  const sampleData = {
    userId: userId || "未登录",
    seatId: sampleSeat.id,
    reserveTime: filter.reserveDate,
    classroomId: filter.classroomId,
  };

  console.log("示例预约数据:", sampleData);
  alert(
    `示例预约数据:\n\n${JSON.stringify(
      sampleData,
      null,
      2
    )}\n\n请检查控制台获取详细信息`
  );
}

// 取消预约
async function cancelReservationHandler(reservationId) {
  try {
    await deleteReservation(reservationId);
    await loadMyReservations();
    await loadAllReservations();
    alert("已取消预约");
  } catch (e) {
    alert("取消失败");
  }
}

onMounted(async () => {
  await loadClassrooms();
  await loadSeats();
  await loadMyReservations();
  if (filter.classroomId) await loadAllReservations();
});
</script>

<style scoped>
.my-seat-reservation {
  width: 900px;
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

h3 {
  margin-bottom: 20px;
  color: #333;
  font-size: 18px;
}

/* 筛选表单样式 */
.filter-form {
  display: flex;
  gap: 16px;
  justify-content: center;
  align-items: center;
  margin-bottom: 24px;
}

.filter-select,
.filter-input {
  padding: 8px 14px;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  outline: none;
  font-size: 15px;
  background: #fafbfc;
  transition: border 0.2s;
  min-width: 120px;
}

.filter-select:focus,
.filter-input:focus {
  border: 1.5px solid #409eff;
}

.filter-button {
  background: #409eff;
  color: #fff;
  border: none;
  border-radius: 6px;
  padding: 8px 18px;
  font-size: 15px;
  cursor: pointer;
  transition: background 0.2s;
}

.filter-button:hover {
  background: #1867c0;
}

.reset-button {
  background: #ff4d4f;
}

.reset-button:hover {
  background: #d9363e;
}

/* 座位列表样式 */
.seat-section {
  margin-top: 20px;
  text-align: center;
}

.seat-title {
  font-size: 18px;
  color: #333;
  margin-bottom: 15px;
  font-weight: 500;
}

.seat-grid {
  margin: 24px 0;
  max-width: 600px;
  margin-left: auto;
  margin-right: auto;
  padding: 20px;
  background: #fafafa;
  border-radius: 12px;
  border: 1px solid #e8e8e8;
  min-height: 200px;
}

.seat-item {
  width: 50px;
  height: 45px;
  border-radius: 6px;
  background: #f5f7fa;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border: 1px solid #e0e0e0;
  font-size: 12px;
  transition: all 0.2s;
  text-align: center;
  line-height: 1.2;
  position: relative;
  box-sizing: border-box;
}

.grid-cell {
  width: 50px;
  height: 45px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.empty-cell {
  width: 50px;
  height: 45px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: none;
}

.aisle-cell {
  width: 50px;
  height: 45px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border: 2px dashed #dee2e6;
  border-radius: 6px;
  font-weight: 500;
  color: #6c757d;
  position: relative;
  overflow: hidden;
}

.aisle-cell::before {
  content: "";
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 25px;
  height: 2px;
  background: #adb5bd;
  border-radius: 1px;
}

.aisle-text {
  font-size: 10px;
  color: #6c757d;
  z-index: 1;
  position: relative;
  font-weight: 500;
}

.seat-item.mine {
  background: #409eff;
  color: #fff;
  border: 1px solid #1867c0;
}

.seat-item.reserved {
  background: #e0e0e0;
  color: #aaa;
  cursor: not-allowed;
  border: 1px solid #d0d0d0;
}

.seat-item:hover:not(.reserved):not(.mine) {
  border: 1px solid #409eff;
  background: #e6f7ff;
}

/* 加载状态样式 */
.seat-item.loading {
  opacity: 0.7;
  cursor: wait;
  pointer-events: none;
}

.seat-item.loading:hover {
  border: 1px solid #e0e0e0;
  background: #f5f7fa;
}

.status-text {
  font-size: 10px;
  margin-top: 2px;
  opacity: 0.8;
}

.no-seat,
.no-reservation {
  text-align: center;
  color: #888;
  margin: 32px 0;
  font-size: 16px;
}

/* 座位图标题和图例样式 */
.seat-legend {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 15px;
  font-size: 14px;
  color: #555;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 5px;
}

.legend-item.mine {
  color: #409eff;
}

.legend-item.aisle {
  color: #6c757d;
}

.legend-box {
  display: inline-block;
  width: 12px;
  height: 12px;
  border-radius: 3px;
  margin-right: 5px;
}

.legend-box.mine {
  background-color: #409eff;
}

.legend-box.aisle {
  background-color: #6c757d;
}

/* 加载指示器样式 */
.loading-indicator {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 20px;
  height: 20px;
  border: 3px solid #f3f3f3;
  border-top: 3px solid #3498db;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% {
    transform: translate(-50%, -50%) rotate(0deg);
  }
  100% {
    transform: translate(-50%, -50%) rotate(360deg);
  }
}

/* 预约弹窗样式 */
.dialog-mask {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
}
.dialog {
  width: 420px;
  background: #ffffff;
  border-radius: 14px;
  padding: 0 0 14px 0;
  box-shadow: 0 20px 60px rgba(0,0,0,.18);
  overflow: hidden;
}
.dialog-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  background: linear-gradient(90deg, #ecf5ff 0%, #f7fbff 100%);
  border-bottom: 1px solid #e6f0fb;
}
.dialog-title {
  margin: 0;
  font-size: 16px;
  color: #1f2937;
}
.dialog-close {
  width: 28px;
  height: 28px;
  border: none;
  border-radius: 6px;
  background: transparent;
  color: #64748b;
  font-size: 18px;
  cursor: pointer;
}
.dialog-close:hover { background: #eef2f7; }

.dialog-body { padding: 14px 16px 0 16px; }
.dialog-field { margin-bottom: 14px; }
.dialog-label { display: block; font-size: 13px; color: #6b7280; margin-bottom: 6px; }
.dialog-value { padding: 10px 12px; background: #f9fafb; border: 1px solid #e5e7eb; border-radius: 8px; color: #374151; }
.dialog-input {
  width: 100%;
  box-sizing: border-box;
  padding: 10px 12px;
  border: 1px solid #dbe2ea;
  border-radius: 8px;
  background: #ffffff;
  font-size: 14px;
  outline: none;
  transition: border-color .2s, box-shadow .2s;
}
.dialog-input:focus {
  border-color: #409eff;
  box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.15);
}

.dialog-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  padding: 10px 16px 0 16px;
}
.btn {
  border: none;
  border-radius: 8px;
  padding: 8px 16px;
  font-size: 14px;
  cursor: pointer;
  transition: all .2s;
}
.btn.primary {
  background: #409eff;
  color: #fff;
}
.btn.primary:hover { background: #2f80ed; }
.btn.ghost {
  background: #eef2f7;
  color: #475569;
}
.btn.ghost:hover { background: #e5eaf1; }
</style>