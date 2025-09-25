<template>
  <div class="classroom-list-container">
    <h2>教室管理</h2>
    <form @submit.prevent="fetchList" class="filter-form">
      <select v-model="filter.building">
        <option value="">全部楼栋</option>
        <option v-for="b in buildingOptions" :key="b" :value="b">{{ b }}</option>
      </select>
      <select v-model="filter.type">
        <option value="">全部类型</option>
        <option v-for="t in typeOptions" :key="t.value" :value="t.value">{{ t.label }}</option>
      </select>
      <select v-model="filter.status">
        <option value="">全部状态</option>
        <option v-for="s in statusOptions" :key="s.value" :value="s.value">{{ s.label }}</option>
      </select>
      <button type="submit">查询</button>
      <button type="button" @click="resetFilter">重置</button>
      <button type="button" @click="showAdd = true" class="add-button">新增教室</button>
    </form>
    <table>
      <thead>
        <tr>
          <th>编号</th>
          <th class="name-col">名称</th>
          <th class="building-col">楼栋</th>
          <th>楼层</th>
          <th>容量</th>
          <th>类型</th>
          <th>状态</th>
          <th>设备</th>
          <th class="desc-col">描述</th>
          <th class="img-col">图片</th>
          <th class="admin-col">管理员</th>
          <th class="ops-col">操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="c in pagedClassrooms" :key="c.id">
          <td>{{ c.code }}</td>
          <td class="name-col">{{ c.name }}</td>
          <td class="building-col">{{ c.building }}</td>
          <td>{{ c.floor }}</td>
          <td>{{ c.capacity }}</td>
          <td>{{ typeOptions.find(t => t.value === c.type)?.label || c.type }}</td>
          <td>{{ statusOptions.find(s => s.value === c.status)?.label || c.status }}</td>
          <td>{{ c.equipment }}</td>
          <td class="desc-col">{{ c.description }}</td>
          <td class="img-col">
            <img v-if="c.imageUrl" :src="getImgUrl(c.imageUrl)" alt="图片" style="max-width:60px;max-height:40px;" />
          </td>
          <td class="admin-col">{{ adminMap[c.adminId] || c.adminId }}</td>
          <td class="ops-col">
            <button @click="editClassroom(c)" class="action-button edit-button">编辑</button>
            <button @click="deleteClassroom(c.id)" class="action-button delete-button">删除</button>
          </td>
        </tr>
      </tbody>
    </table>
    <div class="pagination">
      <button :disabled="page === 1" @click="page--" class="pagination-button prev-button">上一页</button>
      <span>第 {{ page }} 页 / 共 {{ totalPages }} 页</span>
      <button :disabled="page === totalPages" @click="page++" class="pagination-button next-button">下一页</button>
    </div>
    <!-- 新增/编辑弹窗 -->
    <div v-if="showAdd || editForm" class="modal">
      <div class="modal-content">
        <h3>{{ editForm ? '编辑教室' : '新增教室' }}</h3>
        <form @submit.prevent="editForm ? updateClassroom() : addClassroom()" class="modal-form-2col">
          <div class="form-col">
            <input v-model="form.code" placeholder="编号" required />
            <input v-model="form.name" placeholder="教室名" required />
            <input v-model="form.building" placeholder="楼栋" required />
            <input v-model="form.floor" placeholder="楼层" type="number" required />
            <input v-model="form.capacity" placeholder="容量" type="number" required />
            <select v-model="form.type" required>
              <option value="">请选择类型</option>
              <option v-for="t in typeOptions" :key="t.value" :value="t.value">{{ t.label }}</option>
            </select>
            <select v-model="form.status" required>
              <option value="">请选择状态</option>
              <option v-for="s in statusOptions" :key="s.value" :value="s.value">{{ s.label }}</option>
            </select>
          </div>
          <div class="form-col">
            <input v-model="form.equipment" placeholder="设备" />
            <input v-model="form.description" placeholder="描述" />
            <input v-model="form.imageUrl" placeholder="图片URL" />
            <input type="file" @change="handleImageChange" accept="image/*" style="margin-bottom: 10px;" />
            <img v-if="form.imageUrl" :src="getImgUrl(form.imageUrl)" alt="图片预览" style="max-width:120px;max-height:80px;display:block;margin-bottom:10px;" />
            <select v-model="form.adminId">
              <option value="">请选择管理员</option>
              <option v-for="(name, id) in adminMap" :key="id" :value="id">{{ name }}</option>
            </select>
          </div>
          <div class="modal-actions-2col">
            <button type="submit" class="modal-button submit-button">提交</button>
            <button type="button" @click="closeModal" class="modal-button cancel-button">取消</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import axios from '@/api';
import { getAllUsers } from '@/api/user';

const classrooms = ref([]);
const filter = reactive({ building: '', type: '', status: '' });
const showAdd = ref(false);
const editForm = ref(null);
const form = reactive({ code: '', name: '', building: '', floor: '', capacity: '', type: '', status: '', equipment: '', description: '', imageUrl: '', adminId: '' });
const adminMap = ref({});
const buildingOptions = ref([]);
const typeOptions = [
  { value: 'lab', label: '实验室' },
  { value: 'classroom', label: '教室' },
  { value: 'meeting_room', label: '会议室' },
  { value: 'multimedia', label: '多媒体' },
  { value: 'normal', label: '普通' },
  { value: 'other', label: '其他' }
];
const statusOptions = [
  { value: 'maintenance', label: '维修' },
  { value: 'closed', label: '关闭' },
  { value: 'available', label: '空闲' }
];
const page = ref(1);
const pageSize = ref(10);
const total = ref(0);
const pagedClassrooms = computed(() => {
  const start = (page.value - 1) * pageSize.value;
  return classrooms.value.slice(start, start + pageSize.value);
});
const totalPages = computed(() => Math.ceil(total.value / pageSize.value) || 1);

function getImgUrl(url) {
  if (!url) return '';
  if (url.startsWith('http')) return url;
  return url.startsWith('/') ? url : '/' + url;
}

const fetchList = async () => {
  // 动态条件查询
  const params = { ...filter };
  const res = await axios.post('/classrooms/search', params);
  classrooms.value = res.data;
  total.value = classrooms.value.length;
  page.value = 1;
  // 自动收集楼栋、类型选项
  buildingOptions.value = [...new Set(res.data.map(c => c.building).filter(Boolean))];
};

const fetchAdmins = async () => {
  const res = await getAllUsers();
  adminMap.value = {};
  (res.data || []).forEach(u => {
    adminMap.value[u.id] = u.realName || u.username;
  });
};

onMounted(async () => {
  await fetchAdmins();
  fetchList();
});

const addClassroom = async () => {
  await axios.post('/classrooms/add', { ...form });
  showAdd.value = false;
  closeModal();
  fetchList();
};

const editClassroom = (c) => {
  editForm.value = c;
  Object.assign(form, c);
};

const updateClassroom = async () => {
  await axios.put(`/classrooms/${editForm.value.id}`, { ...form });
  editForm.value = null;
  closeModal();
  fetchList();
};

const deleteClassroom = async (id) => {
  if (confirm('确定要删除该教室吗？')) {
    await axios.delete(`/classrooms/${id}`);
    fetchList();
  }
};

const closeModal = () => {
  showAdd.value = false;
  editForm.value = null;
  Object.keys(form).forEach(k => form[k] = '');
};
const resetFilter = () => {
  filter.building = '';
  filter.type = '';
  filter.status = '';
  fetchList();
};

// 假设有图片上传相关方法，统一为/img/upload接口
const handleImageChange = async (e) => {
  const file = e.target.files[0];
  if (!file) return;
  const formData = new FormData();
  formData.append('file', file);
  const res = await fetch('/img/upload', {
    method: 'POST',
    body: formData
  });
  const data = await res.json();
  form.imageUrl = data.url;
};
</script>

<style scoped>
.classroom-list-container {
  max-width: 1600px;
  width: 100%;
  min-width: 1100px;
  margin: 100px ;
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
  margin-bottom: 56px;
  position: relative;
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

.add-button {
  background: #409eff;
  color: #fff;
  border: none;
  border-radius: 6px;
  padding: 7px 18px;
  font-size: 15px;
  cursor: pointer;
  transition: background 0.2s;
  margin-bottom: 0;
  position: absolute;
  left: 0;
  bottom: -48px;
  min-width: 110px;
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
  writing-mode: initial;
  text-orientation: initial;
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
  min-width: 700px;
  max-width: 700px;
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.13);
}

.modal h3 {
  text-align: center;
  margin-bottom: 24px;
}

.modal input, .modal select {
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

.modal input:focus, .modal select:focus {
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

th.name-col, td.name-col {
  min-width: 120px;
}
th.building-col, td.building-col {
  min-width: 100px;
}
th.desc-col, td.desc-col {
  min-width: 160px;
}
th.img-col, td.img-col {
  min-width: 90px;
}
th.admin-col, td.admin-col {
  min-width: 90px;
}
th.ops-col, td.ops-col {
  min-width: 120px;
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
</style> 