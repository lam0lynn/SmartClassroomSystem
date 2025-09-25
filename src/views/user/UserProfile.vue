<template>
  <div class="profile-card" v-if="user">
    <div class="profile-header">
      <div class="avatar-box">
        <img :src="form.avatar ? getImgUrl(form.avatar) : defaultAvatar" class="avatar" @click="avatarEdit = true" />
        <div class="avatar-tip">点击修改头像</div>
      </div>
      <div class="profile-name">{{ form.real_name || form.username }}</div>
    </div>
    <div class="profile-list">
      <div
        class="profile-item"
        v-for="item in filteredInfoList"
        :key="item.key"
        @click="item.editable && openEdit(item.key)"
      >
        <span class="item-label">{{ item.label }}</span>
        <span class="item-value">
          <template v-if="item.isAvatar">
            <img :src="form.avatar ? getImgUrl(form.avatar) : defaultAvatar" class="avatar-mini" />
          </template>
          <template v-else>
            {{ item.format ? item.format(form[item.key]) : (form[item.key] || '未填写') }}
          </template>
        </span>
        <span v-if="item.editable" class="item-arrow">&gt;</span>
      </div>
      <!-- 修改密码入口，放在信息项最后一行 -->
      <div class="profile-item" @click="showPwd = true">
        <span class="item-label">修改密码</span>
        <span class="item-value">********</span>
        <span class="item-arrow">&gt;</span>
      </div>
    </div>
    <div class="profile-actions">
      <button @click="handleLogout">退出登录</button>
    </div>
    <!-- 编辑弹窗 -->
    <div v-if="editKey" class="modal">
      <div class="modal-content">
        <h3>编辑{{ filteredInfoList.find(i => i.key === editKey)?.label }}</h3>
        <input v-model="form[editKey]" :placeholder="filteredInfoList.find(i => i.key === editKey)?.label" />
        <div class="modal-actions">
          <button @click="saveEdit">保存</button>
          <button @click="editKey = ''">取消</button>
        </div>
      </div>
    </div>
    <!-- 头像上传弹窗 -->
    <div v-if="avatarEdit" class="modal">
      <div class="modal-content">
        <h3>修改头像</h3>
        <input type="file" @change="handleAvatarChange" accept="image/*" />
        <div class="modal-actions">
          <button @click="avatarEdit = false">关闭</button>
        </div>
      </div>
    </div>
    <!-- 修改密码弹窗 -->
    <div v-if="showPwd" class="modal">
      <div class="modal-content">
        <h3>修改密码</h3>
        <input type="password" v-model="pwdForm.oldPwd" placeholder="原密码" />
        <input type="password" v-model="pwdForm.newPwd" placeholder="新密码" />
        <input type="password" v-model="pwdForm.confirmPwd" placeholder="确认新密码" />
        <div class="modal-actions">
          <button @click="handleChangePwd">提交</button>
          <button @click="showPwd = false">取消</button>
        </div>
        <div v-if="pwdMsg" class="msg success-msg">{{ pwdMsg }}</div>
        <div v-if="pwdError" class="msg error-msg">{{ pwdError }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { logoutUser, updateUser, changePassword } from '@/api/user';

const router = useRouter();
const user = ref(null);
const form = ref({});
const defaultAvatar = 'https://img1.imgtp.com/2023/07/21/0Qv6Qw7A.png';
const editKey = ref('');
const avatarEdit = ref(false);
const showPwd = ref(false);
const pwdForm = ref({ oldPwd: '', newPwd: '', confirmPwd: '' });
const pwdMsg = ref('');
const pwdError = ref('');

// 字段映射，顺序与数据库一致，部分字段根据角色显示
const infoList = [
  { key: 'username', label: '用户名', editable: false },
  { key: 'realName', label: '真实姓名', editable: true },
  { key: 'email', label: '邮箱', editable: true },
  { key: 'phone', label: '手机号', editable: true, format: v => v ? v.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2') : '' },
  { key: 'role', label: '角色', editable: false, format: v => v === 'student' ? '学生' : v === 'teacher' ? '教师' : v },
  { key: 'college', label: '学院', editable: true },
  // 学生专属
  { key: 'major', label: '专业', editable: true, show: u => u.role === 'student' },
  // 教师专属
  { key: 'title', label: '职称', editable: true, show: u => u.role === 'teacher' },
];

const filteredInfoList = computed(() => {
  return infoList.filter(item => !item.show || item.show(form.value));
});

onMounted(() => {
  const u = sessionStorage.getItem('user');
  if (u) {
    user.value = JSON.parse(u);
    form.value = { ...user.value };
  } else router.push('/login');
});

function getImgUrl(url) {
  if (!url) return defaultAvatar;
  if (url.startsWith('http')) return url;
  return url.startsWith('/') ? url : '/' + url;
}

const openEdit = (key) => {
  if (key === 'avatar') { avatarEdit.value = true; return; }
  editKey.value = key;
};
const saveEdit = async () => {
  try {
    await updateUser(form.value.id, { ...form.value });
    sessionStorage.setItem('user', JSON.stringify(form.value));
    editKey.value = '';
  } catch (e) {
    alert('保存失败');
  }
};
const handleAvatarChange = async (e) => {
  const file = e.target.files[0];
  if (!file) return;
  const formData = new FormData();
  formData.append('file', file);
  const res = await fetch('/img/upload', {
    method: 'POST',
    body: formData
  });
  const data = await res.json();
  form.value.avatar = data.url;
  await updateUser(form.value.id, { ...form.value });
  sessionStorage.setItem('user', JSON.stringify(form.value));
  avatarEdit.value = false;
};
const handleLogout = async () => {
  await logoutUser();
  sessionStorage.removeItem('user');
  router.push('/login');
};
const handleChangePwd = async () => {
  pwdError.value = '';
  pwdMsg.value = '';
  if (!pwdForm.value.oldPwd || !pwdForm.value.newPwd || !pwdForm.value.confirmPwd) {
    pwdError.value = '请填写完整信息';
    return;
  }
  if (pwdForm.value.newPwd !== pwdForm.value.confirmPwd) {
    pwdError.value = '两次新密码不一致';
    pwdForm.value.newPwd = '';
    pwdForm.value.confirmPwd = '';
    return;
  }
  if (pwdForm.value.oldPwd === pwdForm.value.newPwd) {
    pwdError.value = '新密码不能与原密码相同';
    pwdForm.value.newPwd = '';
    pwdForm.value.confirmPwd = '';
    return;
  }
  try {
    const res = await changePassword({
      userId: user.value.id,
      oldPassword: pwdForm.value.oldPwd,
      newPassword: pwdForm.value.newPwd
    });
    if (res.data.success) {
      pwdMsg.value = '密码修改成功，请重新登录';
      setTimeout(() => handleLogout(), 1200);
    } else {
      pwdError.value = res.data.message || '修改失败';
      pwdForm.value.newPwd = '';
      pwdForm.value.confirmPwd = '';
    }
  } catch (e) {
    pwdError.value = e.response?.data?.message || '修改失败';
    pwdForm.value.newPwd = '';
    pwdForm.value.confirmPwd = '';
  }
};
</script>

<style scoped>
.profile-card {
  width: 900px;
  margin: 100px 300px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 4px 24px rgba(0,0,0,0.08);
}
.profile-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 32px 0 18px 0;
  background: #f8fafc;
}
.avatar-box {
  display: flex;
  flex-direction: column;
  align-items: center;
}
.avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  object-fit: cover;
  border: 2.5px solid #e0e0e0;
  box-shadow: 0 2px 8px rgba(64,158,255,0.13);
  margin-bottom: 8px;
  cursor: pointer;
}
.avatar-tip {
  color: #888;
  font-size: 13px;
  margin-bottom: 6px;
}
.profile-name {
  font-size: 20px;
  font-weight: 600;
  color: #222;
  margin-top: 2px;
}
.profile-list {
  background: #fafbfc;
}
.profile-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 22px;
  height: 52px;
  border-bottom: 1px solid #f0f0f0;
  font-size: 16px;
  cursor: pointer;
  transition: background 0.18s;
}
.profile-item:last-child {
  border-bottom: none;
}
.profile-item:hover {
  background: #f3f6fa;
}
.item-label {
  color: #888;
  min-width: 70px;
}
.item-value {
  flex: 1;
  text-align: right;
  color: #222;
  margin-right: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.item-arrow {
  color: #bdbdbd;
  font-size: 18px;
}
.profile-actions {
  text-align: center;
  padding: 24px 0 18px 0;
}
.profile-actions button {
  background: #409eff;
  color: #fff;
  border: none;
  border-radius: 6px;
  padding: 8px 32px;
  font-size: 15px;
  cursor: pointer;
  transition: background 0.2s;
}
.profile-actions button:hover {
  background: #1867c0;
}
.modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.18);
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
  margin-bottom: 24px;
}
.modal-content input {
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
.modal-content input:focus {
  border: 1.5px solid #409eff;
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
.success-msg {
  color: #2e7d32;
  font-weight: bold;
  text-align: center;
  margin-top: 12px;
}
.error-msg {
  color: #c62828;
  font-weight: bold;
  text-align: center;
  margin-top: 12px;
}
.avatar-mini {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
  border: 1.5px solid #e0e0e0;
  margin-left: 8px;
  vertical-align: middle;
}
</style> 