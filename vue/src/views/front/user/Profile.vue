<template>
  <div class="profile-page">
    <div class="page-card">
      <h3>个人信息</h3>
      <el-form :model="form" label-width="80px" style="max-width:500px;margin-top:20px">
        <el-form-item label="头像">
          <el-upload class="avatar-uploader" action="/api/file/upload" name="file"
                     :show-file-list="false" :on-success="handleAvatarSuccess"
                     :headers="{ token: token }">
            <img v-if="form.avatar" :src="form.avatar" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Icon icon="mdi:plus" /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="用户名">
          <el-input :value="form.username" disabled />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="form.nickname" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSave">保存修改</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="page-card" style="margin-top:20px">
      <h3>修改密码</h3>
      <el-form :model="pwdForm" label-width="80px" style="max-width:500px;margin-top:20px">
        <el-form-item label="旧密码">
          <el-input v-model="pwdForm.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="pwdForm.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleChangePwd">修改密码</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script>
import { reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Icon } from '@iconify/vue'
import request from '../../../api'
import { useUserStore } from '../../../stores/user'

export default {
  name: 'ProfilePage',
  components: { Icon },
  setup() {
    const token = localStorage.getItem('token')
    const userStore = useUserStore()
    const form = reactive({ username: '', nickname: '', phone: '', avatar: '' })
    const pwdForm = reactive({ oldPassword: '', newPassword: '' })

    const loadInfo = async () => {
      const res = await request.get('/api/user/info')
      if (res.code === 200) {
        Object.assign(form, res.data)
        // 同步更新 store 和 localStorage
        userStore.user = { ...userStore.user, ...res.data }
        localStorage.setItem('user', JSON.stringify(userStore.user))
      }
    }

    const handleAvatarSuccess = (res) => {
      if (res.code === 200) {
        form.avatar = res.data
      }
    }

    const handleSave = async () => {
      const res = await request.post('/api/user/update', {
        nickname: form.nickname, phone: form.phone, avatar: form.avatar
      })
      if (res.code === 200) {
        ElMessage.success('保存成功')
        // 重新加载用户信息，同步到导航栏
        await loadInfo()
      } else {
        ElMessage.error(res.message)
      }
    }

    const handleChangePwd = async () => {
      const res = await request.post('/api/user/changePassword', pwdForm)
      if (res.code === 200) {
        ElMessage.success('密码修改成功')
        pwdForm.oldPassword = ''
        pwdForm.newPassword = ''
      } else {
        ElMessage.error(res.message)
      }
    }

    onMounted(loadInfo)
    return { token, form, pwdForm, handleAvatarSuccess, handleSave, handleChangePwd }
  }
}
</script>

<style scoped>
h3 { font-size: 16px; color: var(--text-primary); }
.avatar-uploader { display: inline-block; }
.avatar { width: 80px; height: 80px; border-radius: 50%; object-fit: cover; }
.avatar-uploader-icon {
  width: 80px; height: 80px; border: 1px dashed var(--border-color);
  border-radius: 50%; display: flex; align-items: center; justify-content: center;
  font-size: 24px; color: var(--text-muted);
}
</style>
