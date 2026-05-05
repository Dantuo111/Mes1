<template>
  <div class="my-orders-page">
    <div class="page-card">
      <h3>我的工单</h3>
      <div class="search-bar" style="margin-top:16px">
        <el-select v-model="query.status" placeholder="工单状态" clearable style="width:150px">
          <el-option label="待接收" :value="1" />
          <el-option label="待开工" :value="2" />
          <el-option label="执行中" :value="3" />
          <el-option label="已完工" :value="4" />
          <el-option label="已关闭" :value="5" />
        </el-select>
        <el-button type="primary" @click="loadData">搜索</el-button>
      </div>

      <el-table :data="tableData" stripe>
        <el-table-column prop="productName" label="产品名称" />
        <el-table-column prop="quantity" label="生产数量" width="100" />
        <el-table-column prop="processRequirement" label="工序要求" show-overflow-tooltip />
        <el-table-column prop="deliveryDate" label="交付日期" width="120" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260">
          <template #default="{ row }">
            <el-button type="info" size="small" @click="showDetail(row)">详情</el-button>
            <el-button v-if="row.status === 1" type="primary" size="small" @click="handleAction('receive', row.id)">接收</el-button>
            <el-button v-if="row.status === 2" type="primary" size="small" @click="handleAction('start', row.id)">开始</el-button>
            <el-button v-if="row.status === 3" type="warning" size="small" @click="showComplete(row)">完工上报</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap">
        <el-pagination background layout="total, prev, pager, next"
                       :total="total" :page-size="pageSize" v-model:current-page="pageNum" @current-change="loadData" />
      </div>
    </div>

    <!-- 完工上报弹窗 -->
    <el-dialog v-model="completeVisible" title="完工上报" width="400px">
      <el-form label-width="100px">
        <el-form-item label="实际完成数量">
          <el-input-number v-model="actualQuantity" :min="0" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="completeVisible = false">取消</el-button>
        <el-button type="primary" @click="handleComplete">确认上报</el-button>
      </template>
    </el-dialog>

    <!-- 工单详情弹窗 -->
    <el-dialog v-model="detailVisible" title="工单详情" width="550px">
      <el-descriptions :column="2" border v-if="detailData">
        <el-descriptions-item label="产品名称" :span="2">{{ detailData.productName }}</el-descriptions-item>
        <el-descriptions-item label="生产数量">{{ detailData.quantity }}</el-descriptions-item>
        <el-descriptions-item label="实际完成">{{ detailData.actualQuantity || '-' }}</el-descriptions-item>
        <el-descriptions-item label="负责人">{{ detailData.assigneeName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="当前状态">
          <el-tag :type="statusType(detailData.status)">{{ statusText(detailData.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="工序要求" :span="2">{{ detailData.processRequirement || '-' }}</el-descriptions-item>
        <el-descriptions-item label="交付日期">{{ detailData.deliveryDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="完工时间">{{ detailData.completeTime || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../../api'

export default {
  name: 'MyWorkOrders',
  setup() {
    const query = reactive({ status: null })
    const tableData = ref([])
    const total = ref(0)
    const pageNum = ref(1)
    const pageSize = 10
    const completeVisible = ref(false)
    const currentOrderId = ref(null)
    const actualQuantity = ref(0)
    const detailVisible = ref(false)
    const detailData = ref(null)

    const statusMap = { 0: '待派发', 1: '待接收', 2: '待开工', 3: '执行中', 4: '已完工', 5: '已关闭' }
    const statusTypeMap = { 0: 'info', 1: 'warning', 2: '', 3: '', 4: 'success', 5: 'info' }
    const statusText = (s) => statusMap[s] || ''
    const statusType = (s) => statusTypeMap[s] || ''

    const loadData = async () => {
      const res = await request.get('/api/workOrder/list', {
        params: { status: query.status, pageNum: pageNum.value, pageSize }
      })
      if (res.code === 200) {
        tableData.value = res.data.list
        total.value = res.data.total
      }
    }

    const handleAction = async (action, id) => {
      const res = await request.post(`/api/workOrder/${action}/${id}`)
      if (res.code === 200) {
        ElMessage.success('操作成功')
        loadData()
      } else {
        ElMessage.error(res.message)
      }
    }

    const showComplete = (row) => {
      currentOrderId.value = row.id
      actualQuantity.value = row.quantity
      completeVisible.value = true
    }

    const showDetail = async (row) => {
      const res = await request.get(`/api/workOrder/detail/${row.id}`)
      if (res.code === 200) {
        detailData.value = res.data
        detailVisible.value = true
      }
    }

    const handleComplete = async () => {
      const res = await request.post(`/api/workOrder/complete/${currentOrderId.value}`, {
        actualQuantity: actualQuantity.value
      })
      if (res.code === 200) {
        ElMessage.success('完工上报成功')
        completeVisible.value = false
        loadData()
      } else {
        ElMessage.error(res.message)
      }
    }

    onMounted(loadData)
    return { query, tableData, total, pageNum, pageSize, completeVisible, actualQuantity,
             detailVisible, detailData,
             statusText, statusType, loadData, handleAction, showComplete, showDetail, handleComplete }
  }
}
</script>

<style scoped>
h3 { font-size: 16px; color: var(--text-primary); }
</style>
