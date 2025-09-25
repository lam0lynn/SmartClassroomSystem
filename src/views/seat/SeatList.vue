<template>
  <div class="user-list-container">
    <h2>座位列表</h2>
    <!-- 筛选条件 -->
    <form @submit.prevent="applyFilter" class="filter-form">
      <input
        v-model="filter.classroomKeyword"
        type="text"
        placeholder="搜索教室"
      />
      <select v-model="filter.classroomId">
        <option value="">全部教室</option>
        <option
          v-for="c in filteredClassroomList"
          :key="c.id"
          :value="String(c.id)"
        >
          {{ c.name }}
        </option>
      </select>
      <select v-model="filter.status">
        <option value="">全部状态</option>
        <option value="空闲">空闲</option>
        <option value="已预约">已预约</option>
        <option value="使用中">使用中</option>
      </select>
      <button type="submit">查询</button>
      <button type="button" @click="resetFilter">重置</button>
    </form>
    <div
      style="display: flex; gap: 12px; align-items: center; margin-bottom: 12px"
    >
      <button @click="showAdd = true" class="add-button">新增座位</button>
      <button @click="openSeatMap()" class="add-button">座位图批量添加</button>
      <button @click="openSeatMapEdit()" class="add-button">
        座位图批量编辑
      </button>
    </div>

    <!-- 表格 -->
    <table>
      <thead>
        <tr>
          <th>ID</th>
          <th>教室</th>
          <th>座位号</th>
          <th>状态</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="s in pagedSeats" :key="s.id">
          <td>{{ s.id }}</td>
          <td>{{ getClassroomName(s.classroomId) || "-" }}</td>
          <td>{{ s.seatNumber }}</td>
          <td>{{ s.status }}</td>
          <td>
            <button class="action-button edit-button" @click="startEdit(s)">
              编辑
            </button>
            <button class="action-button delete-button" @click="onDelete(s.id)">
              删除
            </button>
          </td>
        </tr>
      </tbody>
    </table>

    <!-- 分页 -->
    <div class="pagination">
      <button
        :disabled="page === 1"
        @click="page--"
        class="pagination-button prev-button"
      >
        上一页
      </button>
      <span>第 {{ page }} 页 / 共 {{ totalPages }} 页</span>
      <button
        :disabled="page === totalPages"
        @click="page++"
        class="pagination-button next-button"
      >
        下一页
      </button>
    </div>

    <!-- 新增座位弹窗 -->
    <div v-if="showAdd" class="modal">
      <div class="modal-content">
        <h3>新增座位</h3>
        <form @submit.prevent="handleAdd">
          <select v-model="addForm.classroomId" required>
            <option value="" disabled>请选择教室</option>
            <option v-for="c in classroomList" :key="c.id" :value="c.id">
              {{ c.name }}
            </option>
          </select>
          <input v-model="addForm.seatNumber" placeholder="座位号" required />
          <select v-model="addForm.status" required>
            <option value="空闲">空闲</option>
            <option value="已预约">已预约</option>
            <option value="使用中">使用中</option>
          </select>
          <button type="submit" class="modal-button submit-button">提交</button>
          <button
            type="button"
            @click="showAdd = false"
            class="modal-button cancel-button"
          >
            取消
          </button>
        </form>
      </div>
    </div>

    <!-- 编辑座位弹窗 -->
    <div v-if="showEdit" class="modal">
      <div class="modal-content">
        <h3>编辑座位</h3>
        <form @submit.prevent="handleEdit">
          <select v-model="editForm.classroomId" required>
            <option value="" disabled>请选择教室</option>
            <option v-for="c in classroomList" :key="c.id" :value="c.id">
              {{ c.name }}
            </option>
          </select>
          <input v-model="editForm.seatNumber" placeholder="座位号" required />
          <select v-model="editForm.status" required>
            <option value="空闲">空闲</option>
            <option value="已预约">已预约</option>
            <option value="使用中">使用中</option>
          </select>
          <button type="submit" class="modal-button submit-button">保存</button>
          <button
            type="button"
            @click="showEdit = false"
            class="modal-button cancel-button"
          >
            取消
          </button>
        </form>
      </div>
    </div>

    <!-- 座位图批量添加弹窗 -->
    <div v-if="showSeatMap" class="modal">
      <div class="modal-content" style="min-width: 720px">
        <h3>座位图批量添加</h3>
        <form @submit.prevent="submitSeatMap">
          <div class="seatmap-toolbar">
            <select
              v-model="seatMapForm.classroomId"
              @change="reloadExistingSeats"
              required
            >
              <option value="" disabled>请选择教室</option>
              <option v-for="c in classroomList" :key="c.id" :value="c.id">
                {{ c.name }}
              </option>
            </select>
            <input
              type="number"
              min="1"
              v-model.number="seatMapForm.rows"
              placeholder="行数"
            />
            <input
              type="number"
              min="1"
              v-model.number="seatMapForm.cols"
              placeholder="列数"
            />
            <button type="button" class="modal-button" @click="clearSelections">
              清空选择
            </button>
          </div>
          <div class="seatmap-legend">
            <span class="legend item selectable"></span> 可选
            <span class="legend item selected"></span> 已选
            <span class="legend item disabled"></span> 已存在
          </div>
          <div class="seatmap-grid" :style="gridStyle">
            <button
              v-for="cell in gridCells"
              :key="cell.key"
              type="button"
              class="seatmap-cell"
              :class="{
                disabled: existingSeatSet.has(cell.label),
                selected:
                  selectedSeatSet.has(cell.label) &&
                  !existingSeatSet.has(cell.label),
              }"
              @click="toggleCell(cell)"
            >
              {{ cell.label }}
            </button>
          </div>
          <div
            style="
              margin-top: 14px;
              display: flex;
              justify-content: flex-end;
              gap: 12px;
            "
          >
            <button
              type="button"
              class="modal-button cancel-button"
              @click="showSeatMap = false"
            >
              取消
            </button>
            <button
              type="submit"
              class="modal-button submit-button"
              :disabled="!seatMapForm.classroomId || selectedSeatSet.size === 0"
            >
              提交（{{ selectedSeatSet.size }}）
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- 座位图批量编辑弹窗（批量改状态/删除） -->
    <div v-if="showSeatMapEdit" class="modal">
      <div class="modal-content" style="min-width: 720px">
        <h3>座位图批量编辑</h3>
        <form @submit.prevent="submitSeatMapEdit">
          <div class="seatmap-toolbar">
            <select
              v-model="seatMapEditForm.classroomId"
              @change="reloadExistingSeatsEdit"
              required
            >
              <option value="" disabled>请选择教室</option>
              <option v-for="c in classroomList" :key="c.id" :value="c.id">
                {{ c.name }}
              </option>
            </select>
            <input
              type="number"
              min="1"
              v-model.number="seatMapEditForm.rows"
              placeholder="行数"
            />
            <input
              type="number"
              min="1"
              v-model.number="seatMapEditForm.cols"
              placeholder="列数"
            />
            <select v-model="seatMapEditForm.op" class="modal-input">
              <option value="status">批量修改状态</option>
              <option value="delete">批量删除</option>
            </select>
            <select
              v-if="seatMapEditForm.op === 'status'"
              v-model="seatMapEditForm.status"
            >
              <option value="空闲">空闲</option>
              <option value="已预约">已预约</option>
              <option value="使用中">使用中</option>
            </select>
            <button
              type="button"
              class="modal-button"
              @click="clearSelectionsEdit"
            >
              清空选择
            </button>
          </div>
          <div class="seatmap-legend">
            <span class="legend item selected"></span> 选中将{{
              seatMapEditForm.op === "delete"
                ? "删除"
                : '改为"' + seatMapEditForm.status + '"'
            }}
            <span class="legend item disabled"></span> 不存在
          </div>
          <div class="seatmap-grid" :style="gridStyleEdit">
            <button
              v-for="cell in gridCellsEdit"
              :key="cell.key"
              type="button"
              class="seatmap-cell"
              :class="{
                disabled: !cell.exists,
                selected: selectedSeatSetEdit.value?.has(cell.label) && cell.exists,
              }"
              @click="toggleCellEdit(cell)"
            >
              {{ cell.label }}
            </button>
          </div>
          <div
            style="
              margin-top: 14px;
              display: flex;
              justify-content: flex-end;
              gap: 12px;
            "
          >
            <button
              type="button"
              class="modal-button cancel-button"
              @click="showSeatMapEdit = false"
            >
              取消
            </button>
            <button
              type="submit"
              class="modal-button submit-button"
              :disabled="
                !seatMapEditForm.classroomId ||
                !selectedSeatSetEdit.value ||
                selectedSeatSetEdit.value.size === 0
              "
            >
              执行（{{ selectedSeatSetEdit.value?.size || 0 }}）
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from "vue";
import {
  getAllSeats,
  addSeat,
  updateSeat,
  deleteSeat,
  batchAddSeats,
  getSeatList,
  updateSeatStatus,
} from "@/api/seat";
import { getAllClassrooms } from "@/api/classroom";

const seats = ref([]);
const classroomMap = ref({}); // id -> name
const classroomList = ref([]); // {id, name}[]
const filter = reactive({ classroomId: "", classroomKeyword: "", status: "" });
const page = ref(1);
const pageSize = ref(10);

const showAdd = ref(false);
const showEdit = ref(false);
const showSeatMap = ref(false);
const showSeatMapEdit = ref(false);
const addForm = reactive({ classroomId: "", seatNumber: "", status: "空闲" });
const editForm = reactive({
  id: "",
  classroomId: "",
  seatNumber: "",
  status: "空闲",
});

// 座位图表单与状态（添加）
const seatMapForm = reactive({ classroomId: "", rows: 8, cols: 11 });
const selectedSeatSet = ref(new Set()); // 选中的编号（大写）
const existingSeatSet = ref(new Set()); // 已存在编号（大写）

// 座位图表单与状态（编辑）
const seatMapEditForm = reactive({
  classroomId: "",
  rows: 8,
  cols: 11,
  op: "status",
  status: "空闲",
});
const selectedSeatSetEdit = ref(new Set());
const existingSeatSetEdit = ref(new Set());
const numberToSeatIdMap = ref({}); // {LABEL -> id}
const numberToSeatStatusMap = ref({}); // {LABEL -> status}

function toRowLabel(index) {
  let n = index;
  let label = "";
  while (n >= 0) {
    label = String.fromCharCode((n % 26) + 65) + label;
    n = Math.floor(n / 26) - 1;
  }
  return label;
}

const gridCells = computed(() => {
  const cells = [];
  const rows = Math.max(1, Number(seatMapForm.rows) || 1);
  const cols = Math.max(1, Number(seatMapForm.cols) || 1);
  for (let r = 0; r < rows; r++) {
    const rowLabel = toRowLabel(r);
    for (let c = 1; c <= cols; c++) {
      const label = (rowLabel + c).toUpperCase();
      cells.push({ key: rowLabel + "-" + c, label, r, c });
    }
  }
  return cells;
});

const gridStyle = computed(() => ({
  display: "grid",
  gridTemplateColumns: `repeat(${Math.max(
    1,
    Number(seatMapForm.cols) || 1
  )}, 56px)`,
  gap: "8px",
  justifyContent: "center",
}));

function toggleCell(cell) {
  if (existingSeatSet.value.has(cell.label)) return;
  if (selectedSeatSet.value.has(cell.label)) {
    selectedSeatSet.value.delete(cell.label);
  } else {
    selectedSeatSet.value.add(cell.label);
  }
}
function clearSelections() {
  selectedSeatSet.value = new Set();
}

async function reloadExistingSeats() {
  existingSeatSet.value = new Set();
  selectedSeatSet.value = new Set();
  if (!seatMapForm.classroomId) return;
  try {
    const { data } = await getSeatList(seatMapForm.classroomId);
    const set = new Set();
    (data || []).forEach((s) =>
      set.add(String(s.seatNumber || "").toUpperCase())
    );
    existingSeatSet.value = set;
  } catch (e) {
    existingSeatSet.value = new Set();
  }
}

async function submitSeatMap() {
  if (!seatMapForm.classroomId || selectedSeatSet.value.size === 0) return;
  try {
    const seatNumbers = Array.from(selectedSeatSet.value);
    await batchAddSeats({
      classroomId: Number(seatMapForm.classroomId),
      seatNumbers,
    });
    showSeatMap.value = false;
    await fetchAllSeats();
    alert("批量添加成功");
  } catch (e) {
    alert("批量添加失败");
  }
}

function openSeatMap() {
  showSeatMap.value = true;
  if (filter.classroomId) {
    seatMapForm.classroomId = Number(filter.classroomId);
    reloadExistingSeats();
  }
}

// 编辑座位图逻辑
const gridCellsEdit = computed(() => {
  const cells = [];
  const rows = Math.max(1, Number(seatMapEditForm.rows) || 1);
  const cols = Math.max(1, Number(seatMapEditForm.cols) || 1);
  for (let r = 0; r < rows; r++) {
    const rowLabel = toRowLabel(r);
    for (let c = 1; c <= cols; c++) {
      const label = (rowLabel + c).toUpperCase();
      // 标记是否存在
      const exists = existingSeatSetEdit.value?.has(label);
      cells.push({ key: "E-" + rowLabel + "-" + c, label, r, c, exists });
    }
  }
  return cells;
});

const gridStyleEdit = computed(() => ({
  display: "grid",
  gridTemplateColumns: `repeat(${Math.max(
    1,
    Number(seatMapEditForm.cols) || 1
  )}, 56px)`,
  gap: "8px",
  justifyContent: "center",
}));

function toggleCellEdit(cell) {
  if (!existingSeatSetEdit.value?.has(cell.label)) return;
  // 若是删除操作，仅在“已预约”时禁止删除；其余状态允许
  if (seatMapEditForm.op === "delete") {
    const st = numberToSeatStatusMap.value[cell.label];
    if (st === "已预约") {
      alert(`座位 ${cell.label} 当前状态为“已预约”，不可删除`);
      return;
    }
  }
  if (selectedSeatSetEdit.value?.has(cell.label)) {
    selectedSeatSetEdit.value.delete(cell.label);
  } else {
    selectedSeatSetEdit.value.add(cell.label);
  }
}

function clearSelectionsEdit() {
  selectedSeatSetEdit.value = new Set();
}

async function reloadExistingSeatsEdit() {
  existingSeatSetEdit.value = new Set();
  selectedSeatSetEdit.value = new Set();
  numberToSeatIdMap.value = {};
  numberToSeatStatusMap.value = {};
  if (!seatMapEditForm.classroomId) return;
  try {
    const { data } = await getSeatList(seatMapEditForm.classroomId);
    const set = new Set();
    const idMap = {};
    const stMap = {};
    (data || []).forEach((s) => {
      const label = String(s.seatNumber || "").toUpperCase();
      set.add(label);
      idMap[label] = s.id;
      stMap[label] = s.status;
    });
    existingSeatSetEdit.value = set;
    numberToSeatIdMap.value = idMap;
    numberToSeatStatusMap.value = stMap;
  } catch (e) {
    existingSeatSetEdit.value = new Set();
    numberToSeatIdMap.value = {};
    numberToSeatStatusMap.value = {};
  }
}

async function submitSeatMapEdit() {
  if (!seatMapEditForm.classroomId || !selectedSeatSetEdit.value || selectedSeatSetEdit.value.size === 0)
    return;
  try {
    const labels = Array.from(selectedSeatSetEdit.value || []);
    const ids = labels.map((l) => numberToSeatIdMap.value[l]).filter(Boolean);
    const tasks = ids.map((id) =>
      seatMapEditForm.op === "delete"
        ? deleteSeat(id)
        : updateSeatStatus(id, seatMapEditForm.status)
    );
    const results = await Promise.allSettled(tasks);
    const ok = results.filter((r) => r.status === "fulfilled").length;
    const fail = results.length - ok;
    if (fail > 0) {
      console.warn("批量编辑失败详情:", results);
      alert(`批量编辑完成：成功 ${ok}，失败 ${fail}`);
    } else {
      alert(`批量编辑完成：成功 ${ok}`);
    }
    showSeatMapEdit.value = false;
    await fetchAllSeats();
  } catch (e) {
    alert("批量编辑失败");
  }
}

function openSeatMapEdit() {
  showSeatMapEdit.value = true;
  // 确保集合被初始化
  if (!existingSeatSetEdit.value) existingSeatSetEdit.value = new Set();
  if (!selectedSeatSetEdit.value) selectedSeatSetEdit.value = new Set();
  if (filter.classroomId) {
    seatMapEditForm.classroomId = Number(filter.classroomId);
    reloadExistingSeatsEdit();
  }
}

async function fetchClassroomMap() {
  try {
    const res = await getAllClassrooms();
    const map = {};
    const list = res.data || [];
    list.forEach((c) => {
      map[c.id] = c.name;
    });
    classroomMap.value = map;
    classroomList.value = list;
  } catch (e) {
    classroomMap.value = {};
    classroomList.value = [];
  }
}

const fetchAllSeats = async () => {
  try {
    const { data } = await getAllSeats();
    seats.value = data || [];
    page.value = 1;
  } catch (e) {
    seats.value = [];
  }
};

function getClassroomName(id) {
  return classroomMap.value?.[id];
}

const filteredClassroomList = computed(() => {
  const kw = (filter.classroomKeyword || "").toLowerCase();
  if (!kw) return classroomList.value;
  return classroomList.value.filter((c) =>
    (c.name || "").toLowerCase().includes(kw)
  );
});

const filteredSeats = computed(() => {
  let list = seats.value;
  if (filter.classroomId) {
    list = list.filter(
      (s) => String(s.classroomId || "") === String(filter.classroomId)
    );
  }
  if (filter.status) {
    list = list.filter((s) => (s.status || "") === filter.status);
  }
  return list;
});

const totalPages = computed(
  () => Math.ceil(filteredSeats.value.length / pageSize.value) || 1
);
const pagedSeats = computed(() => {
  const start = (page.value - 1) * pageSize.value;
  return filteredSeats.value.slice(start, start + pageSize.value);
});

function applyFilter() {
  page.value = 1;
}
function resetFilter() {
  filter.classroomId = "";
  filter.classroomKeyword = "";
  filter.status = "";
  page.value = 1;
}

async function onDelete(id) {
  if (!confirm("确定删除该座位？")) return;
  try {
    await deleteSeat(id);
    await fetchAllSeats();
    alert("删除成功");
  } catch (e) {
    alert("删除失败");
  }
}

function startEdit(row) {
  editForm.id = row.id;
  editForm.classroomId = row.classroomId;
  editForm.seatNumber = row.seatNumber;
  editForm.status = row.status || "空闲";
  showEdit.value = true;
}

async function handleAdd() {
  try {
    await addSeat({
      classroomId: Number(addForm.classroomId),
      seatNumber: addForm.seatNumber,
      status: addForm.status,
    });
    showAdd.value = false;
    addForm.classroomId = "";
    addForm.seatNumber = "";
    addForm.status = "空闲";
    await fetchAllSeats();
  } catch (e) {
    alert("新增失败");
  }
}

async function handleEdit() {
  try {
    await updateSeat(editForm.id, {
      classroomId: Number(editForm.classroomId),
      seatNumber: editForm.seatNumber,
      status: editForm.status,
    });
    showEdit.value = false;
    await fetchAllSeats();
  } catch (e) {
    alert("保存失败");
  }
}

onMounted(async () => {
  await Promise.all([fetchClassroomMap(), fetchAllSeats()]);
});
</script>

<style scoped>
.user-list-container {
  max-width: 900px;
  margin: 100px 400px;
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
  margin-bottom: 12px;
}

.filter-form input,
.filter-form select {
  padding: 7px 14px;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  outline: none;
  font-size: 15px;
  background: #fafbfc;
  transition: border 0.2s;
}

.filter-form input:focus,
.filter-form select:focus {
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
  text-decoration: none;
  display: inline-block;
}

.add-button:hover {
  background: #1867c0;
}

.add-button + .add-button {
  background: #67c23a;
}

.add-button + .add-button:hover {
  background: #5daf34;
}

table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 12px;
}

th,
td {
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

/* modal */
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
  padding: 24px 24px;
  border-radius: 10px;
  min-width: 320px;
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.13);
}

.modal h3 {
  text-align: center;
  margin-bottom: 16px;
}

.modal input,
.modal select {
  display: block;
  width: 100%;
  margin-bottom: 12px;
  padding: 8px 12px;
  border-radius: 6px;
  border: 1px solid #e0e0e0;
  font-size: 15px;
  background: #fafbfc;
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

/* seatmap */
.seatmap-toolbar {
  display: flex;
  gap: 10px;
  align-items: center;
  justify-content: center;
  margin-bottom: 12px;
}
.seatmap-legend {
  display: flex;
  gap: 14px;
  align-items: center;
  justify-content: center;
  color: #666;
  font-size: 13px;
  margin-bottom: 10px;
}
.seatmap-legend .legend.item {
  display: inline-block;
  width: 14px;
  height: 14px;
  border-radius: 4px;
  border: 1px solid #ddd;
  margin-right: 6px;
}
.seatmap-legend .legend.item.selectable {
  background: #fff;
}
.seatmap-legend .legend.item.selected {
  background: #409eff;
}
.seatmap-legend .legend.item.disabled {
  background: #e0e0e0;
}
.seatmap-grid {
  max-height: 420px;
  overflow: auto;
  padding: 8px;
  background: #fafafa;
  border: 1px solid #eee;
  border-radius: 8px;
}
.seatmap-cell {
  width: 56px;
  height: 36px;
  border: 1px solid #ddd;
  border-radius: 6px;
  background: #fff;
  cursor: pointer;
  font-size: 12px;
  color: #333;
}
.seatmap-cell:hover {
  border-color: #409eff;
}
.seatmap-cell.disabled {
  background: #eaeaea;
  color: #999;
  cursor: not-allowed;
  border-color: #ddd;
}
.seatmap-cell.selected {
  background: #409eff;
  color: #fff;
  border-color: #409eff;
}
</style>


