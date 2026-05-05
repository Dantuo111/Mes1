<template>
  <div class="dashboard-page">
    <!-- 统计卡片 -->
    <div class="stat-cards">
      <div class="stat-card" v-for="item in statCards" :key="item.label">
        <Icon :icon="item.icon" width="32" :color="item.color" />
        <div class="stat-info">
          <span class="stat-value">{{ item.value }}</span>
          <span class="stat-label">{{ item.label }}</span>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="chart-row">
      <div class="page-card chart-card">
        <h4>工单状态分布</h4>
        <div ref="pieChartRef" style="height:300px"></div>
      </div>
      <div class="page-card chart-card">
        <h4>生产计划概览</h4>
        <div ref="barChartRef" style="height:300px"></div>
      </div>
    </div>

    <!-- 快捷操作 + 库存预警 -->
    <div class="chart-row" style="margin-top:16px">
      <div class="page-card">
        <h4>快捷操作</h4>
        <div class="quick-actions">
          <el-button type="primary" @click="$router.push('/admin/plans')">
            <Icon icon="mdi:clipboard-plus" width="16" style="margin-right:4px" />新建生产计划
          </el-button>
          <el-button type="success" @click="$router.push('/admin/orders')">
            <Icon icon="mdi:file-document-edit" width="16" style="margin-right:4px" />管理工单
          </el-button>
          <el-button type="warning" @click="$router.push('/admin/materials')">
            <Icon icon="mdi:package-variant-closed" width="16" style="margin-right:4px" />物料管理
          </el-button>
          <el-button @click="$router.push('/admin/quality')">
            <Icon icon="mdi:check-decagram" width="16" style="margin-right:4px" />质量检验
          </el-button>
          <el-button @click="$router.push('/admin/users')">
            <Icon icon="mdi:account-group" width="16" style="margin-right:4px" />用户管理
          </el-button>
        </div>
      </div>
      <div class="page-card">
        <div style="display:flex;justify-content:space-between;align-items:center">
          <h4>库存预警</h4>
          <el-button type="warning" size="small" plain @click="$router.push('/admin/materials')">查看全部</el-button>
        </div>
        <el-table :data="warningList" stripe size="small" style="margin-top:12px" max-height="200">
          <el-table-column prop="code" label="编码" width="100" />
          <el-table-column prop="name" label="名称" min-width="100" />
          <el-table-column label="库存" width="80">
            <template #default="{ row }">
              <span style="color:#E6A23C;font-weight:bold">{{ row.stockQuantity }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="safetyStock" label="安全库存" width="80" />
        </el-table>
        <el-empty v-if="warningList.length === 0" description="暂无预警" :image-size="50" />
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { Icon } from '@iconify/vue'
import * as echarts from 'echarts'
import request from '../../api'

export default {
  name: 'DashboardPage',
  components: { Icon },
  setup() {
    const pieChartRef = ref(null)
    const barChartRef = ref(null)
    const statCards = reactive([
      { label: '用户总数', value: 0, icon: 'mdi:account-group', color: '#2e7d32' },
      { label: '生产计划', value: 0, icon: 'mdi:clipboard-list', color: '#1565c0' },
      { label: '工单总数', value: 0, icon: 'mdi:file-document-edit', color: '#ff8f00' },
      { label: '物料总数', value: 0, icon: 'mdi:package-variant-closed', color: '#c62828' }
    ])

    const loadOverview = async () => {
      const res = await request.get('/api/stats/overview')
      if (res.code === 200) {
        statCards[0].value = res.data.userCount
        statCards[1].value = res.data.planCount
        statCards[2].value = res.data.workOrderCount
        statCards[3].value = res.data.materialCount
      }
    }

    const warningList = ref([])
    const loadWarning = async () => {
      const res = await request.get('/api/material/warning')
      if (res.code === 200) { warningList.value = res.data || [] }
    }

    const loadCharts = async () => {
      // 工单状态分布
      const woRes = await request.get('/api/stats/workOrderStatus')
      if (woRes.code === 200) {
        await nextTick()
        const pie = echarts.init(pieChartRef.value)
        pie.setOption({
          tooltip: { trigger: 'item' },
          color: ['#90a4ae','#ffb74d','#64b5f6','#4caf50','#81c784','#bdbdbd'],
          series: [{
            type: 'pie', radius: ['40%', '70%'],
            data: [
              { value: woRes.data.pendingDispatch, name: '待派发' },
              { value: woRes.data.pendingReceive, name: '待接收' },
              { value: woRes.data.pendingStart, name: '待开工' },
              { value: woRes.data.inProgress, name: '执行中' },
              { value: woRes.data.completed, name: '已完工' },
              { value: woRes.data.closed, name: '已关闭' }
            ]
          }]
        })
      }

      // 生产计划概览
      const planRes = await request.get('/api/stats/planTrend')
      if (planRes.code === 200) {
        await nextTick()
        const bar = echarts.init(barChartRef.value)
        bar.setOption({
          tooltip: {},
          color: ['#ffb74d', '#4caf50', '#81c784'],
          xAxis: { type: 'category', data: ['待执行', '执行中', '已完成'] },
          yAxis: { type: 'value' },
          series: [{
            type: 'bar', barWidth: '40%',
            data: [planRes.data.pending, planRes.data.inProgress, planRes.data.completed]
          }]
        })
      }
    }

    onMounted(async () => {
      await loadOverview()
      await loadWarning()
      await loadCharts()
    })

    return { statCards, pieChartRef, barChartRef, warningList }
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
.chart-card h4 { font-size: 15px; color: var(--text-primary); margin-bottom: 12px; }
.quick-actions { display: flex; flex-wrap: wrap; gap: 10px; margin-top: 12px; }
</style>
