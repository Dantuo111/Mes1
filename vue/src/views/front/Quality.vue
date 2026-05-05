<template>
  <div class="front-quality-page">
    <div class="page-card">
      <h3>质量检验记录</h3>
      <div class="search-bar" style="margin-top:16px">
        <el-date-picker v-model="query.startDate" type="date" placeholder="开始日期" value-format="YYYY-MM-DD" style="width:150px" />
        <el-date-picker v-model="query.endDate" type="date" placeholder="结束日期" value-format="YYYY-MM-DD" style="width:150px" />
        <el-button type="primary" @click="handleSearch">搜索</el-button>
      </div>

      <el-table :data="tableData" stripe>
        <el-table-column prop="workOrderProductName" label="工单产品" min-width="120" />
        <el-table-column label="检验日期" width="120">
          <template #default="{ row }">{{ formatDate(row.inspectionDate) }}</template>
        </el-table-column>
        <el-table-column prop="inspector" label="检验人员" width="100" />
        <el-table-column prop="inspectionQuantity" label="检验数量" width="90" />
        <el-table-column prop="qualifiedQuantity" label="合格数" width="80" />
        <el-table-column prop="unqualifiedQuantity" label="不合格数" width="90" />
        <el-table-column label="合格率" width="100">
          <template #default="{ row }">
            <span :style="{ color: getColor(row.passRate), fontWeight: 'bold' }">
              {{ row.passRate != null ? row.passRate.toFixed(2) + '%' : '-' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="unqualifiedReason" label="不合格原因" min-width="140" show-overflow-tooltip />
      </el-table>

      <el-empty v-if="tableData.length === 0" description="暂无检验记录" />

      <div class="pagination-wrap">
        <el-pagination background layout="total, prev, pager, next"
                       :total="total" :page-size="pageSize" v-model:current-page="pageNum" @current-change="loadData" />
      </div>
    </div>

    <!-- 质量趋势图 -->
    <div class="page-card" style="margin-top:16px">
      <h3>质量趋势</h3>
      <div ref="chartRef" style="height:280px;margin-top:12px"></div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, onMounted, nextTick, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'
import request from '../../api'

export default {
  name: 'FrontQuality',
  setup() {
    const query = reactive({ startDate: '', endDate: '' })
    const tableData = ref([])
    const total = ref(0)
    const pageNum = ref(1)
    const pageSize = 10
    const chartRef = ref(null)
    let chart = null

    const formatDate = (val) => {
      if (!val) return ''
      const d = new Date(val)
      const pad = (n) => String(n).padStart(2, '0')
      return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
    }

    const getColor = (rate) => {
      if (rate == null) return '#909399'
      if (rate >= 90) return '#2e7d32'
      if (rate >= 70) return '#ff8f00'
      return '#c62828'
    }

    const loadData = async () => {
      const res = await request.get('/api/quality/list', {
        params: { startDate: query.startDate || undefined, endDate: query.endDate || undefined, pageNum: pageNum.value, pageSize }
      })
      if (res.code === 200) { tableData.value = res.data.list; total.value = res.data.total }
    }

    const loadChart = async () => {
      const res = await request.get('/api/quality/stats')
      if (res.code === 200 && res.data.passRateTrend) {
        await nextTick()
        if (chart) chart.dispose()
        chart = echarts.init(chartRef.value)
        const trend = res.data.passRateTrend
        const rates = trend.map(i => i.avgPassRate)
        const minRate = rates.length > 0 ? Math.floor(Math.min(...rates) / 5) * 5 - 5 : 0
        const yMin = Math.max(0, minRate)
        chart.setOption({
          tooltip: { trigger: 'axis', formatter: '{b}<br/>平均合格率：{c}%' },
          grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
          xAxis: { type: 'category', data: trend.map(i => i.month), boundaryGap: false },
          yAxis: { type: 'value', name: '合格率(%)', min: yMin, max: 100 },
          series: [{ type: 'line', smooth: true, data: rates, itemStyle: { color: '#4caf50' }, areaStyle: { color: 'rgba(76,175,80,0.15)' } }]
        })
      }
    }

    const handleSearch = () => { pageNum.value = 1; loadData() }
    const handleResize = () => { if (chart) chart.resize() }

    onMounted(() => { loadData(); loadChart(); window.addEventListener('resize', handleResize) })
    onBeforeUnmount(() => { window.removeEventListener('resize', handleResize); if (chart) { chart.dispose(); chart = null } })

    return { query, tableData, total, pageNum, pageSize, chartRef, formatDate, getColor, loadData, handleSearch }
  }
}
</script>

<style scoped>
h3 { font-size: 16px; color: var(--text-primary); }
</style>
