<template>
  <div class="my-stats-page">
    <!-- 统计卡片 -->
    <div class="stat-cards">
      <div class="stat-card">
        <Icon icon="mdi:file-document-multiple" width="32" color="#2e7d32" />
        <div class="stat-info">
          <span class="stat-value">{{ stats.total }}</span>
          <span class="stat-label">工单总数</span>
        </div>
      </div>
      <div class="stat-card">
        <Icon icon="mdi:progress-clock" width="32" color="#ff8f00" />
        <div class="stat-info">
          <span class="stat-value">{{ stats.inProgress }}</span>
          <span class="stat-label">进行中</span>
        </div>
      </div>
      <div class="stat-card">
        <Icon icon="mdi:check-circle" width="32" color="#4caf50" />
        <div class="stat-info">
          <span class="stat-value">{{ stats.completed }}</span>
          <span class="stat-label">已完工</span>
        </div>
      </div>
      <div class="stat-card">
        <Icon icon="mdi:lock-check" width="32" color="#90a4ae" />
        <div class="stat-info">
          <span class="stat-value">{{ stats.closed }}</span>
          <span class="stat-label">已关闭</span>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="chart-row">
      <div class="page-card">
        <h4>我的工单状态分布</h4>
        <div ref="pieRef" style="height:300px"></div>
      </div>
      <div class="page-card">
        <h4>近期工单列表</h4>
        <el-table :data="recentOrders" stripe size="small" style="margin-top:12px">
          <el-table-column prop="productName" label="产品" min-width="100" />
          <el-table-column prop="quantity" label="数量" width="70" />
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="statusTagType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="交付日期" width="110">
            <template #default="{ row }">{{ formatDate(row.deliveryDate) }}</template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, onMounted, nextTick, onBeforeUnmount } from 'vue'
import { Icon } from '@iconify/vue'
import * as echarts from 'echarts'
import request from '../../../api'

export default {
  name: 'MyStats',
  components: { Icon },
  setup() {
    const stats = reactive({ total: 0, inProgress: 0, completed: 0, closed: 0 })
    const recentOrders = ref([])
    const pieRef = ref(null)
    let pieChart = null

    const statusLabel = (v) => ({ 0: '待派发', 1: '待接收', 2: '待开工', 3: '执行中', 4: '已完工', 5: '已关闭' }[v] || '')
    const statusTagType = (v) => ({ 0: 'info', 1: 'warning', 2: '', 3: '', 4: 'success', 5: 'info' }[v] || '')

    const formatDate = (val) => {
      if (!val) return ''
      const d = new Date(val)
      const pad = (n) => String(n).padStart(2, '0')
      return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
    }

    const loadData = async () => {
      const res = await request.get('/api/workOrder/list', { params: { pageNum: 1, pageSize: 100 } })
      if (res.code === 200) {
        const list = res.data.list || []
        stats.total = list.length
        stats.inProgress = list.filter(o => o.status === 3).length
        stats.completed = list.filter(o => o.status === 4).length
        stats.closed = list.filter(o => o.status === 5).length

        recentOrders.value = list.slice(0, 10)

        // 绘制饼图
        const statusCounts = [0, 0, 0, 0, 0, 0]
        list.forEach(o => { if (o.status >= 0 && o.status <= 5) statusCounts[o.status]++ })

        await nextTick()
        if (pieChart) pieChart.dispose()
        pieChart = echarts.init(pieRef.value)
        pieChart.setOption({
          tooltip: { trigger: 'item' },
          color: ['#90a4ae', '#ffb74d', '#64b5f6', '#4caf50', '#81c784', '#bdbdbd'],
          series: [{
            type: 'pie', radius: ['40%', '70%'],
            data: [
              { value: statusCounts[0], name: '待派发' },
              { value: statusCounts[1], name: '待接收' },
              { value: statusCounts[2], name: '待开工' },
              { value: statusCounts[3], name: '执行中' },
              { value: statusCounts[4], name: '已完工' },
              { value: statusCounts[5], name: '已关闭' }
            ].filter(d => d.value > 0)
          }]
        })
      }
    }

    const handleResize = () => { if (pieChart) pieChart.resize() }

    onMounted(() => { loadData(); window.addEventListener('resize', handleResize) })
    onBeforeUnmount(() => { window.removeEventListener('resize', handleResize); if (pieChart) { pieChart.dispose(); pieChart = null } })

    return { stats, recentOrders, pieRef, statusLabel, statusTagType, formatDate }
  }
}
</script>

<style scoped>
.stat-cards { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 20px; }
.stat-card {
  background: #fff; border-radius: 10px; padding: 20px;
  display: flex; align-items: center; gap: 16px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
}
.stat-info { display: flex; flex-direction: column; }
.stat-value { font-size: 24px; font-weight: 600; color: var(--text-primary); }
.stat-label { font-size: 13px; color: var(--text-muted); margin-top: 2px; }
.chart-row { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
h4 { font-size: 15px; color: var(--text-primary); margin-bottom: 8px; }
@media (max-width: 1024px) { .stat-cards { grid-template-columns: repeat(2, 1fr); } .chart-row { grid-template-columns: 1fr; } }
@media (max-width: 768px) { .stat-cards { grid-template-columns: 1fr; } }
</style>
