<template>
  <div class="home-page">
    <!-- 轮播图 -->
    <el-carousel height="360px" :interval="4000" arrow="hover">
      <el-carousel-item v-for="item in banners" :key="item.id">
        <div class="banner-item" :style="{ backgroundImage: `url(${item.url})` }">
          <div class="banner-overlay">
            <h2>{{ item.title }}</h2>
            <p>{{ item.desc }}</p>
          </div>
        </div>
      </el-carousel-item>
    </el-carousel>

    <!-- 待办提醒 + 公告 -->
    <div class="info-row">
      <div class="page-card todo-card">
        <div class="card-title">
          <Icon icon="mdi:bell-ring" width="20" color="#ff8f00" />
          <h3>待办提醒</h3>
          <el-badge :value="todoOrders.length" :hidden="todoOrders.length === 0" />
        </div>
        <div v-if="todoOrders.length > 0" class="todo-list">
          <div class="todo-item" v-for="order in todoOrders" :key="order.id" @click="$router.push('/my-orders')">
            <div class="todo-left">
              <el-tag :type="order.status === 1 ? 'warning' : order.status === 2 ? '' : 'primary'" size="small">
                {{ order.status === 1 ? '待接收' : order.status === 2 ? '待开工' : '执行中' }}
              </el-tag>
              <span class="todo-name">{{ order.productName }}</span>
            </div>
            <span class="todo-date">交付：{{ formatDate(order.deliveryDate) }}</span>
          </div>
        </div>
        <el-empty v-else description="暂无待办工单" :image-size="60" />
      </div>
      <div class="page-card notice-card">
        <div class="card-title">
          <Icon icon="mdi:bullhorn" width="20" color="#2e7d32" />
          <h3>系统公告</h3>
        </div>
        <div class="notice-list">
          <div class="notice-item" v-for="n in notices" :key="n.id">
            <Icon icon="mdi:circle-small" width="20" color="#4caf50" />
            <div class="notice-content">
              <span class="notice-title">{{ n.title }}</span>
              <span class="notice-time">{{ n.time }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 功能入口 -->
    <div class="section">
      <h3 class="section-title">快捷入口</h3>
      <div class="quick-links">
        <div class="quick-card" @click="$router.push('/my-orders')">
          <Icon icon="mdi:file-document-edit" width="36" color="#2e7d32" />
          <span>我的工单</span>
        </div>
        <div class="quick-card" @click="$router.push('/plans')">
          <Icon icon="mdi:clipboard-list" width="36" color="#1565c0" />
          <span>生产计划</span>
        </div>
        <div class="quick-card" @click="$router.push('/materials')">
          <Icon icon="mdi:package-variant-closed" width="36" color="#e65100" />
          <span>物料查询</span>
        </div>
        <div class="quick-card" @click="$router.push('/quality')">
          <Icon icon="mdi:check-decagram" width="36" color="#6a1b9a" />
          <span>质量检验</span>
        </div>
        <div class="quick-card" @click="$router.push('/my-stats')">
          <Icon icon="mdi:chart-bar" width="36" color="#ff8f00" />
          <span>工作统计</span>
        </div>
        <div class="quick-card" @click="$router.push('/profile')">
          <Icon icon="mdi:account-circle" width="36" color="#00838f" />
          <span>个人信息</span>
        </div>
      </div>
    </div>

    <!-- 页脚 -->
    <footer class="home-footer">
      <div class="footer-inner">
        <p>© 2026 企业生产执行系统 - 中小型制造企业生产管理平台</p>
        <p>技术支持：SpringBoot + Vue3 + Element Plus</p>
      </div>
    </footer>
  </div>
</template>

<script>
import { Icon } from '@iconify/vue'
import { ref, onMounted } from 'vue'
import request from '../../api'

export default {
  name: 'HomePage',
  components: { Icon },
  setup() {
    const banners = [
      { id: 1, url: 'https://picsum.photos/1200/400?random=1', title: '智能生产管理', desc: '全流程数字化管控，提升生产效率' },
      { id: 2, url: 'https://picsum.photos/1200/400?random=2', title: '物料精准管控', desc: '实时库存监控，智能预警提醒' },
      { id: 3, url: 'https://picsum.photos/1200/400?random=3', title: '质量全程追溯', desc: '检验数据可视化，质量问题早发现' }
    ]

    const todoOrders = ref([])
    const notices = ref([
      { id: 1, title: '系统已升级至最新版本，新增物料预警功能', time: '2026-04-28' },
      { id: 2, title: '五一假期期间请提前完成工单交付', time: '2026-04-25' },
      { id: 3, title: '质量检验标准已更新，请查阅最新规范', time: '2026-04-20' },
      { id: 4, title: '新员工培训通知：生产流程操作规范', time: '2026-04-15' }
    ])

    const formatDate = (val) => {
      if (!val) return ''
      const d = new Date(val)
      const pad = (n) => String(n).padStart(2, '0')
      return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
    }

    const loadTodo = async () => {
      const res = await request.get('/api/workOrder/list', { params: { pageNum: 1, pageSize: 100 } })
      if (res.code === 200) {
        todoOrders.value = (res.data.list || []).filter(o => [1, 2, 3].includes(o.status)).slice(0, 5)
      }
    }

    onMounted(loadTodo)
    return { banners, todoOrders, notices, formatDate }
  }
}
</script>

<style scoped>
.home-page { padding-bottom: 0; }
.banner-item {
  height: 100%; background-size: cover; background-position: center;
  position: relative; border-radius: 8px; overflow: hidden;
}
.banner-overlay {
  position: absolute; bottom: 0; left: 0; right: 0;
  background: linear-gradient(transparent, rgba(0,0,0,0.5));
  padding: 40px 32px 24px; color: #fff;
}
.banner-overlay h2 { font-size: 24px; margin-bottom: 6px; }
.banner-overlay p { font-size: 14px; opacity: 0.9; }
.section { margin: 32px 0; }
.section-title { font-size: 18px; color: var(--text-primary); margin-bottom: 16px; }
.info-row { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin-top: 24px; }
.card-title { display: flex; align-items: center; gap: 8px; margin-bottom: 14px; }
.card-title h3 { font-size: 15px; color: var(--text-primary); margin: 0; }
.todo-list { display: flex; flex-direction: column; gap: 10px; }
.todo-item {
  display: flex; justify-content: space-between; align-items: center;
  padding: 10px 12px; background: #fafbfc; border-radius: 6px; cursor: pointer;
  transition: background 0.15s;
}
.todo-item:hover { background: #f0f7f0; }
.todo-left { display: flex; align-items: center; gap: 8px; }
.todo-name { font-size: 14px; color: var(--text-primary); }
.todo-date { font-size: 12px; color: var(--text-muted); }
.notice-list { display: flex; flex-direction: column; gap: 6px; }
.notice-item { display: flex; align-items: flex-start; gap: 2px; }
.notice-content { display: flex; flex-direction: column; }
.notice-title { font-size: 14px; color: var(--text-secondary); }
.notice-time { font-size: 12px; color: var(--text-muted); margin-top: 2px; }
@media (max-width: 768px) { .info-row { grid-template-columns: 1fr; } }
.quick-links { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; }
.quick-card {
  flex: 1; background: #fff; border-radius: 10px; padding: 28px;
  display: flex; flex-direction: column; align-items: center; gap: 12px;
  box-shadow: 0 1px 6px rgba(0,0,0,0.06); cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}
.quick-card:hover { transform: translateY(-3px); box-shadow: 0 4px 16px rgba(0,0,0,0.1); }
.quick-card span { font-size: 15px; color: var(--text-secondary); }
.home-footer {
  background: #fff; margin: 40px -20px 0; padding: 24px 0;
  border-top: 1px solid var(--border-color); text-align: center;
}
.footer-inner p { color: var(--text-muted); font-size: 13px; line-height: 1.8; }
</style>
