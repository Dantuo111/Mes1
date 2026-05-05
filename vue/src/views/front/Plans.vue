<template>
  <div class="front-plans-page">
    <div class="page-card">
      <h3>生产计划</h3>
      <div class="search-bar" style="margin-top:16px">
        <el-input v-model="query.keyword" placeholder="搜索产品名称" clearable style="width:200px" />
        <el-select v-model="query.status" placeholder="计划状态" clearable style="width:140px">
          <el-option label="待执行" :value="0" />
          <el-option label="执行中" :value="1" />
          <el-option label="已完成" :value="2" />
        </el-select>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
      </div>

      <div class="plan-grid">
        <div class="plan-card" v-for="plan in tableData" :key="plan.id">
          <div class="plan-card-header">
            <span class="plan-name">{{ plan.productName }}</span>
            <el-tag :type="statusTagType(plan.status)" size="small">{{ statusLabel(plan.status) }}</el-tag>
          </div>
          <div class="plan-card-body">
            <div class="plan-info-row">
              <Icon icon="mdi:package-variant" width="16" />
              <span>计划数量：<strong>{{ plan.plannedQuantity }}</strong></span>
            </div>
            <div class="plan-info-row">
              <Icon icon="mdi:calendar-range" width="16" />
              <span>{{ formatDate(plan.startTime) }} ~ {{ formatDate(plan.endTime) }}</span>
            </div>
            <div class="plan-info-row">
              <Icon icon="mdi:flag" width="16" />
              <span>优先级：</span>
              <el-tag :type="priorityTagType(plan.priority)" size="small">{{ priorityLabel(plan.priority) }}</el-tag>
            </div>
            <div class="plan-actions">
              <el-button type="primary" size="small" plain @click="showDetail(plan)">
                <Icon icon="mdi:eye" width="14" style="margin-right:4px" />计划详情
              </el-button>
              <el-button type="success" size="small" plain @click="showOrders(plan)">
                <Icon icon="mdi:file-document-multiple" width="14" style="margin-right:4px" />查看工单
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <el-empty v-if="tableData.length === 0" description="暂无生产计划" />
      <div class="pagination-wrap">
        <el-pagination background layout="total, prev, pager, next"
                       :total="total" :page-size="pageSize" v-model:current-page="pageNum" @current-change="loadData" />
      </div>
    </div>

    <!-- 计划详情弹窗 -->
    <el-dialog v-model="detailVisible" :title="'计划详情 - ' + detailPlan.productName" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="产品名称">{{ detailPlan.productName }}</el-descriptions-item>
        <el-descriptions-item label="计划数量">{{ detailPlan.plannedQuantity }}</el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ formatDate(detailPlan.startTime) }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ formatDate(detailPlan.endTime) }}</el-descriptions-item>
        <el-descriptions-item label="优先级">
          <el-tag :type="priorityTagType(detailPlan.priority)" size="small">{{ priorityLabel(detailPlan.priority) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusTagType(detailPlan.status)" size="small">{{ statusLabel(detailPlan.status) }}</el-tag>
        </el-descriptions-item>
      </el-descriptions>

      <!-- 生产进度 -->
      <div class="detail-section">
        <h4>生产进度</h4>
        <div class="progress-info">
          <span>工单完成率：{{ detailProgress.completed }} / {{ detailProgress.total }} 个工单</span>
          <el-progress :percentage="detailProgress.percent" :color="detailProgress.percent === 100 ? '#67c23a' : '#2e7d32'" style="margin-top:8px" />
        </div>
        <div class="progress-info" style="margin-top:12px">
          <span>产量完成率：{{ detailProgress.actualQty }} / {{ detailPlan.plannedQuantity }}</span>
          <el-progress :percentage="detailProgress.qtyPercent" :color="detailProgress.qtyPercent >= 100 ? '#67c23a' : '#ff8f00'" style="margin-top:8px" />
        </div>
      </div>

      <!-- 质量汇总 -->
      <div class="detail-section" v-if="detailQuality.length > 0">
        <h4>质量检验汇总</h4>
        <el-table :data="detailQuality" stripe size="small">
          <el-table-column prop="workOrderProductName" label="工单产品" min-width="100" />
          <el-table-column prop="inspector" label="检验人员" width="80" />
          <el-table-column prop="inspectionQuantity" label="检验数" width="70" />
          <el-table-column prop="qualifiedQuantity" label="合格数" width="70" />
          <el-table-column label="合格率" width="80">
            <template #default="{ row }">
              <span :style="{ color: row.passRate >= 90 ? '#2e7d32' : row.passRate >= 70 ? '#ff8f00' : '#c62828', fontWeight:'bold' }">
                {{ row.passRate != null ? row.passRate.toFixed(1) + '%' : '-' }}
              </span>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <template #footer><el-button @click="detailVisible = false">关闭</el-button></template>
    </el-dialog>

    <!-- 关联工单弹窗 -->
    <el-dialog v-model="ordersVisible" :title="'关联工单 - ' + currentPlanName" width="750px">
      <el-table :data="planOrders" stripe size="small">
        <el-table-column prop="productName" label="产品" min-width="100" />
        <el-table-column prop="quantity" label="计划数量" width="80" />
        <el-table-column prop="assigneeName" label="负责人" width="80" />
        <el-table-column prop="processRequirement" label="工序要求" min-width="130" show-overflow-tooltip />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="woStatusType(row.status)" size="small">{{ woStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="实际完成" width="80">
          <template #default="{ row }">{{ row.actualQuantity || '-' }}</template>
        </el-table-column>
        <el-table-column label="交付日期" width="100">
          <template #default="{ row }">{{ formatDate(row.deliveryDate) }}</template>
        </el-table-column>
      </el-table>
      <el-empty v-if="planOrders.length === 0" description="暂无关联工单" :image-size="60" />
      <template #footer><el-button @click="ordersVisible = false">关闭</el-button></template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { Icon } from '@iconify/vue'
import request from '../../api'

export default {
  name: 'FrontPlans',
  components: { Icon },
  setup() {
    const query = reactive({ keyword: '', status: null })
    const tableData = ref([])
    const total = ref(0)
    const pageNum = ref(1)
    const pageSize = 8

    const statusLabel = (v) => ({ 0: '待执行', 1: '执行中', 2: '已完成' }[v] || '')
    const statusTagType = (v) => ({ 0: 'info', 1: 'warning', 2: 'success' }[v] || 'info')
    const priorityLabel = (v) => ({ 0: '低', 1: '中', 2: '高' }[v] || '')
    const priorityTagType = (v) => ({ 0: 'info', 1: 'warning', 2: 'danger' }[v] || 'info')
    const woStatusLabel = (v) => ({ 0: '待派发', 1: '待接收', 2: '待开工', 3: '执行中', 4: '已完工', 5: '已关闭' }[v] || '')
    const woStatusType = (v) => ({ 0: 'info', 1: 'warning', 2: '', 3: '', 4: 'success', 5: 'info' }[v] || '')

    const formatDate = (val) => {
      if (!val) return ''
      const d = new Date(val)
      const pad = (n) => String(n).padStart(2, '0')
      return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
    }

    const loadData = async () => {
      const res = await request.get('/api/productionPlan/list', {
        params: { keyword: query.keyword || undefined, status: query.status, pageNum: pageNum.value, pageSize }
      })
      if (res.code === 200) { tableData.value = res.data.list; total.value = res.data.total }
    }
    const handleSearch = () => { pageNum.value = 1; loadData() }

    // 计划详情
    const detailVisible = ref(false)
    const detailPlan = reactive({ productName: '', plannedQuantity: 0, startTime: null, endTime: null, priority: 0, status: 0 })
    const detailProgress = reactive({ total: 0, completed: 0, percent: 0, actualQty: 0, qtyPercent: 0 })
    const detailQuality = ref([])

    const showDetail = async (plan) => {
      Object.assign(detailPlan, plan)
      // 加载工单计算进度
      const woRes = await request.get('/api/workOrder/list', { params: { pageNum: 1, pageSize: 100 } })
      if (woRes.code === 200) {
        const orders = (woRes.data.list || []).filter(o => o.planId === plan.id)
        detailProgress.total = orders.length
        detailProgress.completed = orders.filter(o => o.status >= 4).length
        detailProgress.percent = orders.length > 0 ? Math.round(detailProgress.completed / orders.length * 100) : 0
        detailProgress.actualQty = orders.reduce((sum, o) => sum + (o.actualQuantity || 0), 0)
        detailProgress.qtyPercent = plan.plannedQuantity > 0 ? Math.min(Math.round(detailProgress.actualQty / plan.plannedQuantity * 100), 100) : 0

        // 加载质量检验
        const orderIds = orders.map(o => o.id)
        const qRes = await request.get('/api/quality/list', { params: { pageNum: 1, pageSize: 100 } })
        if (qRes.code === 200) {
          detailQuality.value = (qRes.data.list || []).filter(q => orderIds.includes(q.workOrderId))
        }
      }
      detailVisible.value = true
    }

    // 关联工单
    const ordersVisible = ref(false)
    const currentPlanName = ref('')
    const planOrders = ref([])

    const showOrders = async (plan) => {
      currentPlanName.value = plan.productName
      const res = await request.get('/api/workOrder/list', { params: { pageNum: 1, pageSize: 100 } })
      if (res.code === 200) { planOrders.value = (res.data.list || []).filter(o => o.planId === plan.id) }
      ordersVisible.value = true
    }

    onMounted(loadData)
    return {
      query, tableData, total, pageNum, pageSize,
      statusLabel, statusTagType, priorityLabel, priorityTagType, woStatusLabel, woStatusType, formatDate,
      loadData, handleSearch,
      detailVisible, detailPlan, detailProgress, detailQuality, showDetail,
      ordersVisible, currentPlanName, planOrders, showOrders
    }
  }
}
</script>

<style scoped>
h3 { font-size: 16px; color: var(--text-primary); }
.plan-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 16px; margin-top: 8px; }
.plan-card {
  background: #fafbfc; border: 1px solid var(--border-color); border-radius: 10px;
  padding: 18px; transition: box-shadow 0.2s;
}
.plan-card:hover { box-shadow: 0 3px 12px rgba(0,0,0,0.08); }
.plan-card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.plan-name { font-size: 15px; font-weight: 600; color: var(--text-primary); }
.plan-card-body { display: flex; flex-direction: column; gap: 8px; }
.plan-info-row { display: flex; align-items: center; gap: 6px; font-size: 13px; color: var(--text-secondary); }
.plan-info-row strong { color: var(--text-primary); }
.plan-actions { display: flex; gap: 8px; margin-top: 10px; }
.detail-section { margin-top: 20px; }
.detail-section h4 { font-size: 14px; color: var(--text-primary); margin-bottom: 10px; padding-bottom: 6px; border-bottom: 1px solid var(--border-color); }
.progress-info { font-size: 13px; color: var(--text-secondary); }
@media (max-width: 768px) { .plan-grid { grid-template-columns: 1fr; } }
</style>
