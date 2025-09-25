<template>
  <div class="reservation-list-container">
    <h2>预约列表</h2>
    <!-- 筛选条件 -->
    <form @submit.prevent="fetchList" class="filter-form">
      <select v-model="filter.userId">
        <option value="">全部用户</option>
        <option v-for="(name, id) in userMap" :key="id" :value="id">
          {{ name }}
        </option>
      </select>
      <select v-model="filter.classroomId">
        <option value="">全部教室</option>
        <option v-for="(name, id) in classroomMap" :key="id" :value="id">
          {{ name }}
        </option>
      </select>
      <select v-model="filter.status">
        <option value="">全部状态</option>
        <option
          v-for="opt in statusOptions"
          :key="opt.value"
          :value="opt.value"
        >
          {{ opt.label }}
        </option>
      </select>
      <button type="submit">查询</button>
      <button type="button" @click="resetFilter">重置</button>
    </form>
    <button @click="showAdd = true" class="add-button">新增预约</button>
    <!-- 预约表格 -->
    <table>
      <thead>
        <tr>
          <th>ID</th>
          <th>教室ID</th>
          <th>用户ID</th>
          <th>开始时间</th>
          <th>结束时间</th>
          <th>用途</th>
          <th>人数</th>
          <th>状态</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="r in pagedReservations" :key="r.id">
          <td>{{ r.id }}</td>
          <td>{{ classroomMap[r.classroomId] || r.classroomId }}</td>
          <td>{{ userMap[r.userId] || r.userId }}</td>
          <td>{{ r.startTime }}</td>
          <td>{{ r.endTime }}</td>
          <td>{{ r.purpose }}</td>
          <td>{{ r.participants }}</td>
          <td>{{ statusMap[r.status] || r.status }}</td>
          <td>
            <button
              class="action-button edit-button"
              :disabled="r.status !== 'pending'"
              @click="r.status === 'pending' && handleApprove(r)"
            >
              通过
            </button>
            <button
              class="action-button delete-button"
              :disabled="r.status !== 'pending'"
              @click="r.status === 'pending' && handleReject(r)"
            >
              拒绝
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
    <!-- 新增预约弹窗 -->
    <div v-if="showAdd" class="modal">
      <div class="modal-content">
        <h3>新增预约</h3>
        <form @submit.prevent="handleAdd">
          <select v-model="addForm.classroomId" required>
            <option value="">请选择教室</option>
            <option v-for="(name, id) in classroomMap" :key="id" :value="id">
              {{ name }}
            </option>
          </select>
          <select v-model="addForm.userId" required>
            <option value="">请选择用户</option>
            <option v-for="(name, id) in userMap" :key="id" :value="id">
              {{ name }}
            </option>
          </select>
          <input
            v-model="addForm.startTime"
            type="datetime-local"
            placeholder="开始时间"
            required
          />
          <input
            v-model="addForm.endTime"
            type="datetime-local"
            placeholder="结束时间"
            required
          />
          <input v-model="addForm.purpose" placeholder="用途" />
          <input v-model="addForm.participants" placeholder="人数" />
          <select v-model="addForm.status" required>
            <option value="">请选择状态</option>
            <option
              v-for="opt in statusOptions"
              :key="opt.value"
              :value="opt.value"
            >
              {{ opt.label }}
            </option>
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
    <!-- 编辑预约弹窗 -->
    <div v-if="showEdit" class="modal">
      <div class="modal-content">
        <h3>编辑预约</h3>
        <form @submit.prevent="handleEdit">
          <select v-model="editForm.classroomId" required>
            <option value="">请选择教室</option>
            <option v-for="(name, id) in classroomMap" :key="id" :value="id">
              {{ name }}
            </option>
          </select>
          <select v-model="editForm.userId" required>
            <option value="">请选择用户</option>
            <option v-for="(name, id) in userMap" :key="id" :value="id">
              {{ name }}
            </option>
          </select>
          <input
            v-model="editForm.startTime"
            type="datetime-local"
            placeholder="开始时间"
            required
          />
          <input
            v-model="editForm.endTime"
            type="datetime-local"
            placeholder="结束时间"
            required
          />
          <input v-model="editForm.purpose" placeholder="用途" />
          <input v-model="editForm.participants" placeholder="人数" />
          <select v-model="editForm.status" required>
            <option value="">请选择状态</option>
            <option
              v-for="opt in statusOptions"
              :key="opt.value"
              :value="opt.value"
            >
              {{ opt.label }}
            </option>
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
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from "vue";
import axios from "@/api";
import { getAllUsers } from "@/api/user";
import { getAllClassrooms } from "@/api/classroom";

const reservations = ref([]);
const filter = reactive({ userId: "", classroomId: "", status: "" });
const showAdd = ref(false);
const showEdit = ref(false);
const addForm = reactive({
  classroomId: "",
  userId: "",
  startTime: "",
  endTime: "",
  purpose: "",
  participants: "",
  status: "pending",
});
const editForm = reactive({
  id: "",
  classroomId: "",
  userId: "",
  startTime: "",
  endTime: "",
  purpose: "",
  participants: "",
  status: "",
});
const page = ref(1);
const pageSize = ref(10);
const total = ref(0);
const userMap = ref({});
const classroomMap = ref({});

const statusOptions = [
  { value: "pending", label: "待审批" },
  { value: "approved", label: "已通过" },
  { value: "rejected", label: "已拒绝" },
  { value: "canceled", label: "已取消" },
  { value: "completed", label: "已完成" },
];
const statusMap = Object.fromEntries(
  statusOptions.map((opt) => [opt.value, opt.label])
);

const pagedReservations = computed(() => {
  const start = (page.value - 1) * pageSize.value;
  return reservations.value.slice(start, start + pageSize.value);
});
const totalPages = computed(
  () => Math.ceil(reservations.value.length / pageSize.value) || 1
);

const fetchUserAndClassroomMaps = async () => {
  const usersRes = await getAllUsers();
  userMap.value = {};
  (usersRes.data || []).forEach((u) => {
    userMap.value[u.id] = u.realName || u.username;
  });
  const classroomsRes = await getAllClassrooms();
  classroomMap.value = {};
  (classroomsRes.data || []).forEach((c) => {
    classroomMap.value[c.id] = c.name;
  });
};

const fetchList = async () => {
  const params = {};
  if (filter.userId) params.userId = filter.userId;
  if (filter.classroomId) params.classroomId = filter.classroomId;
  if (filter.status) params.status = filter.status;
  try {
    const res = await axios.get("/reservations", { params });
    reservations.value = res.data;
    total.value = reservations.value.length;
    page.value = 1;
  } catch (error) {
    console.error("Failed to fetch reservation list:", error);
  }
};

const resetFilter = () => {
  filter.userId = "";
  filter.classroomId = "";
  filter.status = "";
  fetchList();
};

const handleAdd = async () => {
  try {
    await axios.post("/reservations/add", { ...addForm });
    showAdd.value = false;
    Object.keys(addForm).forEach((k) => (addForm[k] = ""));
    fetchList();
  } catch (error) {
    console.error("Failed to add reservation:", error);
  }
};

const editReservation = (r) => {
  Object.assign(editForm, r);
  showEdit.value = true;
};

const handleEdit = async () => {
  try {
    await axios.put(`/reservations/${editForm.id}`, { ...editForm });
    showEdit.value = false;
    fetchList();
  } catch (error) {
    console.error("Failed to edit reservation:", error);
  }
};

const handleApprove = async (r) => {
  if (r.status !== "pending") return;
  try {
    await axios.put(`/reservations/${r.id}`, { ...r, status: "approved" });
    fetchList();
  } catch (error) {
    console.error("Failed to approve reservation:", error);
  }
};

const handleReject = async (r) => {
  if (r.status !== "pending") return;
  // 可弹窗输入拒绝理由，这里简单实现
  const reason = prompt("请输入拒绝理由：");
  if (reason === null) return;
  try {
    await axios.put(`/reservations/${r.id}`, {
      ...r,
      status: "rejected",
      rejectReason: reason,
    });
    fetchList();
  } catch (error) {
    console.error("Failed to reject reservation:", error);
  }
};

onMounted(async () => {
  await fetchUserAndClassroomMaps();
  fetchList();
});
</script>

<style scoped>
.reservation-list-container {
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

.filter-form {
  display: flex;
  gap: 16px;
  justify-content: center;
  align-items: center;
  margin-bottom: 24px;
}

.filter-form select {
  padding: 7px 14px;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  outline: none;
  font-size: 15px;
  background: #fafbfc;
  transition: border 0.2s;
}

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

.action-button:disabled {
  background: #d3d3d3 !important;
  color: #fff !important;
  cursor: not-allowed !important;
  opacity: 0.7;
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

.modal select {
  display: block;
  width: 100%;
  margin-bottom: 18px;
  padding: 8px 12px;
  border-radius: 6px;
  border: 1px solid #e0e0e0;
  font-size: 15px;
  background: #fafbfc;
  outline: none;
  transition: border 0.2s;
}

.modal select:focus {
  border: 1.5px solid #409eff;
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
</style>



