<template>
  <div class="material-requests-page">
    <div class="page-card">
      <h3>物料申领审批</h3>
      <div class="search-bar" style="margin-top:16px">
        <el-select v-model="query.status" placeholder="审批状态" clearable style="width:140px">
          <el-option label="待审批" :value="0" />
          <el-option label="已批准" :value="1" />
          <el-option label="已拒绝" :value="2" />
        </el-select>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
      </div>

      <el-table :data="tableData" stripe>
        <el-table-column prop="userName" label="申领人" width="100" />
        <el-table-column prop="materialName" label="物料名称" min-width="120" />
        <el-table-column prop="materialCode" label="物料编码" width="120" />
        <el-table-column prop="quantity" label="申领数量" width="90" />
        <el-table-column prop="reason" label="申领原因" min-width="160" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <template v-if="row.status === 0">
              <el-button type="success" size="small" @click="handleApprove(row, 1)">批准</el-button>
              <el-button type="danger" size="small" @click="handleApprove(row, 2)">拒绝</el-button>
            </template>
            <span v-else style="color:var(--text-muted);font-size:13px">{{ row.reply || '已处理' }}</span>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination background layout="total, prev, pager, next"
                       :total="total" :page-size="pageSize" v-model:current-page="pageNum" @current-change="loadData" />
      </div>
    </div>

    <!-- 审批弹窗 -->
    <el-dialog v-model="approveVisible" :title="approveStatus === 1 ? '批准申领' : '拒绝申领'" width="450px">
      <p style="margin-bottom:12px;color:#606266">
        申领人：<strong>{{ currentRow.userName }}</strong>，
        物料：<strong>{{ currentRow.materialName }}</strong>，
        数量：<strong>{{ currentRow.quantity }}</strong>
      </p>
      <el-form label-width="80px">
        <el-form-item label="审批回复">
          <el-input v-model="approveReply" type="textarea" :rows="3" :placeholder="approveStatus === 1 ? '批准说明（可选）' : '请说明拒绝原因'" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="approveVisible = false">取消</el-button>
        <el-button :type="approveStatus === 1 ? 'success' : 'danger'" @click="submitApprove">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../api'

export default {
  name: 'MaterialRequestsPage',
  setup() {
    const query = reactive({ status: null })
    const tableData = ref([])
    const total = ref(0)
    const pageNum = ref(1)
    const pageSize = 10
    const approveVisible = ref(false)
    const approveStatus = ref(1)
    const approveReply = ref('')
    const currentRow = ref({})

    const statusLabel = (v) => ({ 0: '待审批', 1: '已批准', 2: '已拒绝' }[v] || '')
    const statusType = (v) => ({ 0: 'warning', 1: 'success', 2: 'danger' }[v] || '')

    const loadData = async () => {
      const res = await request.get('/api/materialRequest/list', {
        params: { status: query.status, pageNum: pageNum.value, pageSize }
      })
      if (res.code === 200) { tableData.value = res.data.list; total.value = res.data.total }
    }

    const handleSearch = () => { pageNum.value = 1; loadData() }

    const handleApprove = (row, status) => {
      currentRow.value = row
      approveStatus.value = status
      approveReply.value = ''
      approveVisible.value = true
    }

    const submitApprove = async () => {
      const res = await request.post('/api/materialRequest/approve', {
        id: currentRow.value.id, status: approveStatus.value, reply: approveReply.value
      })
      if (res.code === 200) {
        ElMessage.success(approveStatus.value === 1 ? '已批准' : '已拒绝')
        approveVisible.value = false
        loadData()
      } else ElMessage.error(res.message)
    }

    onMounted(loadData)
    return { query, tableData, total, pageNum, pageSize, approveVisible, approveStatus, approveReply, currentRow,
             statusLabel, statusType, loadData, handleSearch, handleApprove, submitApprove }
  }
}
</script>

<style scoped>
h3 { font-size: 16px; color: var(--text-primary); }
</style>
