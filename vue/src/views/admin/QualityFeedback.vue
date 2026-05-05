<template>
  <div class="quality-feedback-page">
    <div class="page-card">
      <h3>质量反馈处理</h3>
      <div class="search-bar" style="margin-top:16px">
        <el-select v-model="query.status" placeholder="处理状态" clearable style="width:140px">
          <el-option label="待处理" :value="0" />
          <el-option label="处理中" :value="1" />
          <el-option label="已解决" :value="2" />
          <el-option label="已关闭" :value="3" />
        </el-select>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
      </div>

      <el-table :data="tableData" stripe>
        <el-table-column prop="userName" label="反馈人" width="90" />
        <el-table-column prop="title" label="问题标题" min-width="140" />
        <el-table-column prop="workOrderProductName" label="关联工单" width="120" />
        <el-table-column label="严重程度" width="90">
          <template #default="{ row }">
            <el-tag :type="severityType(row.severity)" size="small">{{ severityLabel(row.severity) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="提交时间" width="150">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="openHandle(row)">处理</el-button>
            <el-button type="info" size="small" @click="showDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination background layout="total, prev, pager, next"
                       :total="total" :page-size="pageSize" v-model:current-page="pageNum" @current-change="loadData" />
      </div>
    </div>

    <!-- 处理弹窗 -->
    <el-dialog v-model="handleVisible" title="处理质量反馈" width="500px">
      <el-descriptions :column="1" border style="margin-bottom:16px">
        <el-descriptions-item label="问题标题">{{ currentRow.title }}</el-descriptions-item>
        <el-descriptions-item label="问题描述">{{ currentRow.description }}</el-descriptions-item>
      </el-descriptions>
      <el-form label-width="80px">
        <el-form-item label="处理状态">
          <el-select v-model="handleForm.status" style="width:100%">
            <el-option label="处理中" :value="1" />
            <el-option label="已解决" :value="2" />
            <el-option label="已关闭" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理回复">
          <el-input v-model="handleForm.reply" type="textarea" :rows="3" placeholder="请输入处理回复" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleVisible = false">取消</el-button>
        <el-button type="primary" @click="submitHandle">确认</el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="反馈详情" width="550px">
      <el-descriptions :column="2" border v-if="detailData">
        <el-descriptions-item label="反馈人">{{ detailData.userName }}</el-descriptions-item>
        <el-descriptions-item label="关联工单">{{ detailData.workOrderProductName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="问题标题" :span="2">{{ detailData.title }}</el-descriptions-item>
        <el-descriptions-item label="严重程度">
          <el-tag :type="severityType(detailData.severity)" size="small">{{ severityLabel(detailData.severity) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="当前状态">
          <el-tag :type="statusType(detailData.status)" size="small">{{ statusLabel(detailData.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="问题描述" :span="2">{{ detailData.description || '-' }}</el-descriptions-item>
        <el-descriptions-item label="处理回复" :span="2">{{ detailData.reply || '暂无回复' }}</el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ formatTime(detailData.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ formatTime(detailData.updateTime) }}</el-descriptions-item>
      </el-descriptions>
      <template #footer><el-button @click="detailVisible = false">关闭</el-button></template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../api'

export default {
  name: 'QualityFeedbackPage',
  setup() {
    const query = reactive({ status: null })
    const tableData = ref([])
    const total = ref(0)
    const pageNum = ref(1)
    const pageSize = 10
    const handleVisible = ref(false)
    const detailVisible = ref(false)
    const detailData = ref(null)
    const currentRow = ref({})
    const handleForm = reactive({ status: 1, reply: '' })

    const severityLabel = (v) => ({ 0: '低', 1: '中', 2: '高' }[v] || '')
    const severityType = (v) => ({ 0: 'info', 1: 'warning', 2: 'danger' }[v] || '')
    const statusLabel = (v) => ({ 0: '待处理', 1: '处理中', 2: '已解决', 3: '已关闭' }[v] || '')
    const statusType = (v) => ({ 0: 'warning', 1: '', 2: 'success', 3: 'info' }[v] || '')
    const formatTime = (val) => {
      if (!val) return ''
      const d = new Date(val)
      const pad = (n) => String(n).padStart(2, '0')
      return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
    }

    const loadData = async () => {
      const res = await request.get('/api/qualityFeedback/list', {
        params: { status: query.status, pageNum: pageNum.value, pageSize }
      })
      if (res.code === 200) { tableData.value = res.data.list; total.value = res.data.total }
    }

    const handleSearch = () => { pageNum.value = 1; loadData() }

    const openHandle = (row) => {
      currentRow.value = row
      handleForm.status = row.status === 0 ? 1 : row.status
      handleForm.reply = row.reply || ''
      handleVisible.value = true
    }

    const submitHandle = async () => {
      const res = await request.post('/api/qualityFeedback/handle', {
        id: currentRow.value.id, status: handleForm.status, reply: handleForm.reply
      })
      if (res.code === 200) { ElMessage.success('处理成功'); handleVisible.value = false; loadData() }
      else ElMessage.error(res.message)
    }

    const showDetail = (row) => { detailData.value = row; detailVisible.value = true }

    onMounted(loadData)
    return { query, tableData, total, pageNum, pageSize, handleVisible, detailVisible, detailData, currentRow, handleForm,
             severityLabel, severityType, statusLabel, statusType, formatTime, loadData, handleSearch, openHandle, submitHandle, showDetail }
  }
}
</script>

<style scoped>
h3 { font-size: 16px; color: var(--text-primary); }
</style>
