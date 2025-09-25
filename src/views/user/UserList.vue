<template>
  <div class="user-list-container">
    <h2>用户列表</h2>
    <!-- 筛选条件 -->
    <form @submit.prevent="fetchUsers" class="filter-form">
      <input v-model="searchForm.username" placeholder="用户名" />
      <input v-model="searchForm.role" placeholder="角色" />
      <input v-model="searchForm.college" placeholder="学院" />
      <button type="submit">查询</button>
      <button type="button" @click="resetSearch">重置</button>
    </form>
    <button @click="showAdd = true" class="add-button">新增用户</button>
    <!-- 用户表格 -->
    <table>
      <thead>
        <tr>
          <th>ID</th>
          <th>用户名</th>
          <th>角色</th>
          <th>学院</th>
          <th>手机号</th>
          <th>邮箱</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="u in pagedUsers" :key="u.id">
          <td>{{ u.id }}</td>
          <td>{{ u.username }}</td>
          <td>{{ u.role }}</td>
          <td>{{ u.college }}</td>
          <td>{{ u.phone }}</td>
          <td>{{ u.email }}</td>
          <td>
            <button @click="editUser(u)" class="action-button edit-button">编辑</button>
            <button @click="handleDelete(u.id)" class="action-button delete-button">删除</button>
          </td>
        </tr>
      </tbody>
    </table>
    <!-- 分页 -->
    <div class="pagination">
      <button :disabled="page === 1" @click="page--" class="pagination-button prev-button">上一页</button>
      <span>第 {{ page }} 页 / 共 {{ totalPages }} 页</span>
      <button :disabled="page === totalPages" @click="page++" class="pagination-button next-button">下一页</button>
    </div>
    <!-- 新增用户弹窗 -->
    <div v-if="showAdd" class="modal">
      <div class="modal-content">
        <h3>新增用户</h3>
        <form @submit.prevent="handleAdd">
          <input v-model="addForm.username" placeholder="用户名" required />
          <input v-model="addForm.password" type="password" placeholder="密码" required />
          <input v-model="addForm.realName" placeholder="真实姓名" required />
          <input v-model="addForm.role" placeholder="角色" required />
          <input v-model="addForm.college" placeholder="学院" required />
          <input v-model="addForm.phone" placeholder="手机号" />
          <input v-model="addForm.email" placeholder="邮箱" />
          <button type="submit" class="modal-button submit-button">提交</button>
          <button type="button" @click="showAdd = false" class="modal-button cancel-button">取消</button>
        </form>
      </div>
    </div>
    <!-- 编辑用户弹窗 -->
    <div v-if="showEdit" class="modal">
      <div class="modal-content">
        <h3>编辑用户</h3>
        <form @submit.prevent="handleEdit">
          <input v-model="editForm.username" placeholder="用户名" required />
          <input v-model="editForm.realName" placeholder="真实姓名" required />
          <input v-model="editForm.role" placeholder="角色" required />
          <input v-model="editForm.college" placeholder="学院" required />
          <input v-model="editForm.phone" placeholder="手机号" />
          <input v-model="editForm.email" placeholder="邮箱" />
          <button type="submit" class="modal-button submit-button">保存</button>
          <button type="button" @click="showEdit = false" class="modal-button cancel-button">取消</button>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from "vue";
import {
  searchUsers,
  addUser,
  updateUser,
  deleteUser,
  getUsersByRole,
  getUsersByCollege,
} from "@/api/user";

const users = ref([]);
const searchForm = reactive({ username: "", role: "", college: "" });
const page = ref(1);
const pageSize = ref(10);
const total = ref(0);

const showAdd = ref(false);
const showEdit = ref(false);
const addForm = reactive({
  username: "",
  password: "",
  realName: "",
  role: "",
  college: "",
  phone: "",
  email: "",
});
const editForm = reactive({
  id: "",
  username: "",
  realName: "",
  role: "",
  college: "",
  phone: "",
  email: "",
});

const fetchUsers = async () => {
  // 只填角色且其他为空
  if (searchForm.role && !searchForm.college && !searchForm.username) {
    const res = await getUsersByRole(searchForm.role);
    users.value = res.data;
    total.value = users.value.length;
    page.value = 1;
    return;
  }
  // 只填学院且其他为空
  if (searchForm.college && !searchForm.role && !searchForm.username) {
    const res = await getUsersByCollege(searchForm.college);
    users.value = res.data;
    total.value = users.value.length;
    page.value = 1;
    return;
  }
  // 其他情况（只填用户名，或填了多个条件）都用动态条件查询
  const res = await searchUsers({ ...searchForm });
  users.value = res.data.list || res.data || [];
  total.value = users.value.length;
  page.value = 1;
};

const pagedUsers = computed(() => {
  const start = (page.value - 1) * pageSize.value;
  return users.value.slice(start, start + pageSize.value);
});
const totalPages = computed(() => Math.ceil(total.value / pageSize.value) || 1);

const resetSearch = () => {
  searchForm.username = "";
  searchForm.role = "";
  searchForm.college = "";
  fetchUsers();
};

const handleAdd = async () => {
  await addUser({ ...addForm });
  showAdd.value = false;
  Object.keys(addForm).forEach((k) => (addForm[k] = ""));
  fetchUsers();
};

const editUser = (u) => {
  Object.assign(editForm, u);
  showEdit.value = true;
};

const handleEdit = async () => {
  await updateUser(editForm.id, { ...editForm });
  showEdit.value = false;
  fetchUsers();
};

const handleDelete = async (id) => {
  if (confirm("确定要删除该用户吗？")) {
    await deleteUser(id);
    fetchUsers();
  }
};

onMounted(fetchUsers);
watch([page], fetchUsers);
</script>

<style scoped>
.user-list-container {
  max-width: 900px;
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



