<template>
  <div class="admin-layout">
    <aside class="sidebar" :class="{ collapsed: isCollapsed }">
      <div class="sidebar-logo">
        <Icon icon="mdi:factory" width="24" />
        <span v-if="!isCollapsed">MES管理后台</span>
      </div>
      <el-menu :default-active="activeMenu" :collapse="isCollapsed" router
               background-color="#fff" text-color="#606266" active-text-color="#2e7d32">
        <el-menu-item index="/admin">
          <Icon icon="mdi:view-dashboard" width="20" />
          <template #title>数据统计</template>
        </el-menu-item>
        <el-menu-item index="/admin/users">
          <Icon icon="mdi:account-group" width="20" />
          <template #title>用户管理</template>
        </el-menu-item>
        <el-menu-item index="/admin/plans">
          <Icon icon="mdi:clipboard-list" width="20" />
          <template #title>生产计划</template>
        </el-menu-item>
        <el-menu-item index="/admin/orders">
          <Icon icon="mdi:file-document-edit" width="20" />
          <template #title>工单管理</template>
        </el-menu-item>
        <el-menu-item index="/admin/materials">
          <Icon icon="mdi:package-variant-closed" width="20" />
          <template #title>物料管理</template>
        </el-menu-item>
        <el-menu-item index="/admin/quality">
          <Icon icon="mdi:check-decagram" width="20" />
          <template #title>质量检验</template>
        </el-menu-item>
        <el-menu-item index="/admin/material-requests">
          <Icon icon="mdi:hand-extended" width="20" />
          <template #title>物料申领审批</template>
        </el-menu-item>
        <el-menu-item index="/admin/quality-feedback">
          <Icon icon="mdi:message-alert" width="20" />
          <template #title>质量反馈处理</template>
        </el-menu-item>
      </el-menu>
    </aside>
    <div class="main-area">
      <header class="admin-header">
        <div class="header-left">
          <Icon :icon="isCollapsed ? 'mdi:menu-open' : 'mdi:menu'" width="22"
                @click="isCollapsed = !isCollapsed" style="cursor:pointer" />
        </div>
        <div class="header-right">
          <img v-if="user.avatar" :src="user.avatar" class="admin-avatar" />
          <Icon v-else icon="mdi:account-circle" width="32" color="#2e7d32" />
          <span class="admin-name">{{ user.nickname || '管理员' }}</span>
          <el-button type="primary" size="small" @click="handleLogout" plain>退出</el-button>
        </div>
      </header>
      <main class="admin-content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script>
import { Icon } from '@iconify/vue'
import { useUserStore } from '../stores/user'
import { useRouter, useRoute } from 'vue-router'
import { ref, computed } from 'vue'

export default {
  name: 'AdminLayout',
  components: { Icon },
  setup() {
    const userStore = useUserStore()
    const router = useRouter()
    const route = useRoute()
    const isCollapsed = ref(false)
    const user = computed(() => userStore.user)
    const activeMenu = computed(() => route.path)

    const handleLogout = () => {
      userStore.logout()
      router.push('/login')
    }

    return { user, isCollapsed, activeMenu, handleLogout }
  }
}
</script>

<style scoped>
.admin-layout { display: flex; min-height: 100vh; }
.sidebar {
  width: 220px; background: #fff; border-right: 1px solid var(--border-color);
  transition: width 0.25s; display: flex; flex-direction: column;
}
.sidebar.collapsed { width: 64px; }
.sidebar-logo {
  height: 60px; display: flex; align-items: center; justify-content: center;
  gap: 8px; font-size: 16px; font-weight: 600; color: var(--primary-color);
  border-bottom: 1px solid var(--border-color); white-space: nowrap; overflow: hidden;
}
.el-menu { border-right: none; }
.el-menu-item { display: flex; align-items: center; gap: 8px; }
.main-area { flex: 1; display: flex; flex-direction: column; overflow: hidden; }
.admin-header {
  height: 60px; background: #fff; border-bottom: 1px solid var(--border-color);
  display: flex; align-items: center; justify-content: space-between; padding: 0 20px;
}
.header-right { display: flex; align-items: center; gap: 10px; }
.admin-avatar { width: 32px; height: 32px; border-radius: 50%; object-fit: cover; border: 2px solid #e8f5e9; }
.admin-name { color: var(--text-secondary); font-size: 14px; }
.admin-content { flex: 1; padding: 20px; overflow-y: auto; }
</style>
