<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-header">
        <Icon icon="mdi:factory" width="36" color="#2e7d32" />
        <h2>企业生产执行系统</h2>
        <p>中小型制造企业生产管理平台</p>
      </div>

      <!-- 角色切换 -->
      <div class="role-tabs">
        <div class="role-tab" :class="{ active: loginRole === 'employee' }" @click="switchRole('employee')">
          <Icon icon="mdi:account-hard-hat" width="22" />
          <span>员工登录</span>
        </div>
        <div class="role-tab" :class="{ active: loginRole === 'admin' }" @click="switchRole('admin')">
          <Icon icon="mdi:shield-account" width="22" />
          <span>管理员登录</span>
        </div>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="0">
        <el-form-item prop="username">
          <el-input v-model="form.username" :placeholder="loginRole === 'admin' ? '请输入管理员账号' : '请输入员工账号'" prefix-icon="User" size="large" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码"
                    prefix-icon="Lock" size="large" show-password @keyup.enter="handleLogin" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" style="width:100%" :loading="loading" @click="handleLogin">
            {{ loginRole === 'admin' ? '管理员登录' : '员工登录' }}
          </el-button>
        </el-form-item>
      </el-form>
      <div class="login-footer">
        还没有账号？<router-link to="/register">立即注册</router-link>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'
import { Icon } from '@iconify/vue'

export default {
  name: 'LoginPage',
  components: { Icon },
  setup() {
    const router = useRouter()
    const userStore = useUserStore()
    const formRef = ref(null)
    const loading = ref(false)
    const loginRole = ref('employee')
    const form = reactive({ username: '', password: '' })
    const rules = {
      username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
      password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
    }

    const switchRole = (role) => {
      loginRole.value = role
      form.username = ''
      form.password = ''
      if (formRef.value) formRef.value.resetFields()
    }

    const handleLogin = async () => {
      const valid = await formRef.value.validate().catch(() => false)
      if (!valid) return
      loading.value = true
      try {
        const res = await userStore.login(form.username, form.password)
        if (res.code === 200) {
          const user = res.data.user
          // 校验角色是否匹配
          if (loginRole.value === 'admin' && user.role !== 1) {
            ElMessage.error('该账号不是管理员，请切换到员工登录')
            userStore.logout()
            loading.value = false
            return
          }
          if (loginRole.value === 'employee' && user.role !== 0) {
            ElMessage.error('该账号不是员工，请切换到管理员登录')
            userStore.logout()
            loading.value = false
            return
          }
          ElMessage.success('登录成功')
          if (user.role === 1) {
            router.push('/admin')
          } else {
            router.push('/')
          }
        } else {
          ElMessage.error(res.message)
        }
      } catch (e) {
        ElMessage.error('登录失败，请检查网络')
      } finally {
        loading.value = false
      }
    }

    return { formRef, form, rules, loading, loginRole, switchRole, handleLogin }
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh; display: flex; align-items: center; justify-content: center;
  background: url('../assets/image/bg.png') no-repeat center center;
  background-size: cover;
}
.login-card {
  width: 420px; background: rgba(255,255,255,0.92); border-radius: 12px; padding: 36px 40px;
  box-shadow: 0 4px 24px rgba(0,0,0,0.12); backdrop-filter: blur(8px);
}
.login-header { text-align: center; margin-bottom: 24px; }
.login-header h2 { margin: 12px 0 4px; color: var(--text-primary); font-size: 22px; }
.login-header p { color: var(--text-muted); font-size: 13px; }
.role-tabs {
  display: flex; gap: 0; margin-bottom: 24px; border: 1px solid var(--border-color); border-radius: 8px; overflow: hidden;
}
.role-tab {
  flex: 1; display: flex; align-items: center; justify-content: center; gap: 6px;
  padding: 10px 0; cursor: pointer; font-size: 14px; color: var(--text-secondary);
  background: #fafbfc; transition: all 0.2s; user-select: none;
}
.role-tab:first-child { border-right: 1px solid var(--border-color); }
.role-tab.active {
  background: var(--primary-color); color: #fff;
}
.role-tab:hover:not(.active) { background: #f0f7f0; }
.login-footer { text-align: center; color: var(--text-muted); font-size: 14px; }
</style>
