<template>
  <div class="register-page">
    <div class="register-card">
      <div class="register-header">
        <Icon icon="mdi:factory" width="36" color="#2e7d32" />
        <h2>注册账号</h2>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="0">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" prefix-icon="User" size="large" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" prefix-icon="Lock" size="large" show-password />
        </el-form-item>
        <el-form-item prop="nickname">
          <el-input v-model="form.nickname" placeholder="请输入昵称" size="large" />
        </el-form-item>
        <el-form-item prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" size="large" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" style="width:100%" :loading="loading" @click="handleRegister">
            注 册
          </el-button>
        </el-form-item>
      </el-form>
      <div class="register-footer">
        已有账号？<router-link to="/login">去登录</router-link>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Icon } from '@iconify/vue'
import request from '../api'

export default {
  name: 'RegisterPage',
  components: { Icon },
  setup() {
    const router = useRouter()
    const formRef = ref(null)
    const loading = ref(false)
    const form = reactive({ username: '', password: '', nickname: '', phone: '' })
    const rules = {
      username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
      password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
    }

    const handleRegister = async () => {
      const valid = await formRef.value.validate().catch(() => false)
      if (!valid) return
      loading.value = true
      try {
        const res = await request.post('/api/user/register', form)
        if (res.code === 200) {
          ElMessage.success('注册成功，请登录')
          router.push('/login')
        } else {
          ElMessage.error(res.message)
        }
      } catch (e) {
        ElMessage.error('注册失败，请检查网络')
      } finally {
        loading.value = false
      }
    }

    return { formRef, form, rules, loading, handleRegister }
  }
}
</script>

<style scoped>
.register-page {
  min-height: 100vh; display: flex; align-items: center; justify-content: center;
  background: url('../assets/image/bg.png') no-repeat center center;
  background-size: cover;
}
.register-card {
  width: 400px; background: rgba(255,255,255,0.92); border-radius: 12px; padding: 40px;
  box-shadow: 0 4px 24px rgba(0,0,0,0.12); backdrop-filter: blur(8px);
}
.register-header { text-align: center; margin-bottom: 32px; }
.register-header h2 { margin: 12px 0 0; color: var(--text-primary); font-size: 22px; }
.register-footer { text-align: center; color: var(--text-muted); font-size: 14px; }
</style>
