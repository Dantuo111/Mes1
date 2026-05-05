<template>
  <div class="front-layout">
    <header class="front-header">
      <div class="header-inner">
        <div class="logo" @click="$router.push('/')">
          <Icon icon="mdi:factory" width="28" />
          <span>企业生产执行系统</span>
        </div>
        <nav class="nav-links">
          <router-link to="/" class="nav-item">首页</router-link>
          <router-link to="/my-orders" class="nav-item">我的工单</router-link>
          <router-link to="/plans" class="nav-item">生产计划</router-link>
          <router-link to="/materials" class="nav-item">物料查询</router-link>
          <router-link to="/quality" class="nav-item">质量检验</router-link>
          <router-link to="/my-stats" class="nav-item">工作统计</router-link>
          <router-link to="/my-requests" class="nav-item">物料申领</router-link>
          <router-link to="/my-feedback" class="nav-item">质量反馈</router-link>
          <router-link to="/profile" class="nav-item">个人信息</router-link>
        </nav>
        <div class="user-area">
          <img v-if="user.avatar" :src="user.avatar" class="nav-avatar" @click="$router.push('/profile')" />
          <Icon v-else icon="mdi:account-circle" width="32" color="#2e7d32" class="nav-avatar-icon" @click="$router.push('/profile')" />
          <span class="username">{{ user.nickname || user.username }}</span>
          <el-button type="primary" size="small" @click="handleLogout" plain>退出</el-button>
        </div>
      </div>
    </header>
    <main class="front-main">
      <router-view />
    </main>
  </div>
</template>

<script>
import { Icon } from '@iconify/vue'
import { useUserStore } from '../stores/user'
import { useRouter } from 'vue-router'
import { computed } from 'vue'

export default {
  name: 'FrontLayout',
  components: { Icon },
  setup() {
    const userStore = useUserStore()
    const router = useRouter()
    const user = computed(() => userStore.user)

    const handleLogout = () => {
      userStore.logout()
      router.push('/login')
    }

    return { user, handleLogout }
  }
}
</script>

<style scoped>
.front-layout { min-height: 100vh; display: flex; flex-direction: column; }
.front-header {
  background: #fff;
  box-shadow: 0 1px 4px rgba(0,0,0,0.08);
  position: sticky; top: 0; z-index: 100;
}
.header-inner {
  max-width: 1200px; margin: 0 auto;
  display: flex; align-items: center; justify-content: space-between;
  padding: 0 20px; height: 60px;
}
.logo {
  display: flex; align-items: center; gap: 8px;
  font-size: 18px; font-weight: 600; color: var(--primary-color);
  cursor: pointer;
}
.nav-links { display: flex; gap: 20px; overflow-x: auto; }
.nav-item {
  color: var(--text-secondary); font-size: 15px; padding: 4px 0;
  border-bottom: 2px solid transparent; transition: all 0.2s;
}
.nav-item:hover, .nav-item.router-link-exact-active {
  color: var(--primary-color); border-bottom-color: var(--primary-color);
}
.user-area { display: flex; align-items: center; gap: 10px; }
.nav-avatar { width: 32px; height: 32px; border-radius: 50%; object-fit: cover; cursor: pointer; border: 2px solid #e8f5e9; }
.nav-avatar-icon { cursor: pointer; }
.username { color: var(--text-secondary); font-size: 14px; white-space: nowrap; }
.front-main { flex: 1; max-width: 1200px; margin: 0 auto; width: 100%; padding: 20px; }
</style>
