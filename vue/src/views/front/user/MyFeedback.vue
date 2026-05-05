<template>
  <div class="my-feedback-page">
    <div class="page-card">
      <div class="page-header">
        <h3>质量反馈</h3>
        <el-button type="primary" @click="openDialog">
          <Icon icon="mdi:plus" width="16" style="margin-right:4px" />提交反馈
        </el-button>
      </div>

      <el-table :data="tableData" stripe>
        <el-table-column prop="title" label="问题标题" min-width="140" />
        <el-table-column prop="workOrderProductName" label="关联工单" width="120" />
        <el-table-column label="严重程度" width="100">
          <template #default="{ row }">
            <el-tag :type="severityType(row.severity)" size="small">{{ severityLabel(row.severity) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reply" label="处理回复" min-width="160" show-overflow-tooltip />
        <el-table-column label="提交时间" width="160">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="80">
          <template #default="{ row }">
            <el-button type="info" size="small" @click="showDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination background layout="total, prev, pager, next"
                       :total="total" :page-size="pageSize" v-model:current-page="pageNum" @current-change="loadData" />
      </div>
    </div>

    <!-- 提交反馈弹窗 -->
    <el-dialog v-model="dialogVisible" title="提交质量反馈" width="550px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="关联工单" prop="workOrderId">
          <el-select v-model="form.workOrderId" placeholder="请选择工单" filterable clearable style="width:100%">
            <el-option v-for="wo in orderList" :key="wo.id" :label="wo.productName" :value="wo.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="问题标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入问题标题" />
        </el-form-item>
        <el-form-item label="严重程度" prop="severity">
          <el-select v-model="form.severity" style="width:100%">
            <el-option label="低" :value="0" />
            <el-option label="中" :value="1" />
            <el-option label="高" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="问题描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="请详细描述质量问题" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">提交</el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="反馈详情" width="550px">
      <el-descriptions :column="2" border v-if="detailData">
        <el-descriptions-item label="问题标题" :span="2">{{ detailData.title }}</el-descriptions-item>
        <el-descriptions-item label="关联工单">{{ detailData.workOrderProductName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="严重程度">
          <el-tag :type="severityType(detailData.severity)" size="small">{{ severityLabel(detailData.severity) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="当前状态">
          <el-tag :type="statusType(detailData.status)" size="small">{{ statusLabel(detailData.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ formatTime(detailData.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="问题描述" :span="2">{{ detailData.description || '-' }}</el-descriptions-item>
        <el-descriptions-item label="处理回复" :span="2">{{ detailData.reply || '暂无回复' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer><el-button @click="detailVisible = false">关闭</el-button></template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Icon } from '@iconify/vue'
import request from '../../../api'

export default {
  name: 'MyFeedback',
  components: { Icon },
  setup() {
    const tableData = ref([])
    const total = ref(0)
    const pageNum = ref(1)
    const pageSize = 10
    const dialogVisible = ref(false)
    const detailVisible = ref(false)
    const detailData = ref(null)
    const formRef = ref(null)
    const orderList = ref([])
    const form = reactive({ workOrderId: null, title: '', severity: 1, description: '' })
    const rules = {
      title: [{ required: true, message: '请输入问题标题', trigger: 'blur' }],
      severity: [{ required: true, message: '请选择严重程度', trigger: 'change' }],
      description: [{ required: true, message: '请描述问题', trigger: 'blur' }]
    }

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
      const res = await request.get('/api/qualityFeedback/list', { params: { pageNum: pageNum.value, pageSize } })
      if (res.code === 200) { tableData.value = res.data.list; total.value = res.data.total }
    }

    const openDialog = async () => {
      const res = await request.get('/api/workOrder/list', { params: { pageNum: 1, pageSize: 100 } })
      if (res.code === 200) orderList.value = res.data.list
      Object.assign(form, { workOrderId: null, title: '', severity: 1, description: '' })
      dialogVisible.value = true
    }

    const handleSubmit = async () => {
      await formRef.value.validate()
      const res = await request.post('/api/qualityFeedback/save', form)
      if (res.code === 200) { ElMessage.success('反馈已提交'); dialogVisible.value = false; loadData() }
      else ElMessage.error(res.message)
    }

    const showDetail = (row) => { detailData.value = row; detailVisible.value = true }

    onMounted(loadData)
    return { tableData, total, pageNum, pageSize, dialogVisible, detailVisible, detailData, formRef, orderList, form, rules,
             severityLabel, severityType, statusLabel, statusType, formatTime, loadData, openDialog, handleSubmit, showDetail }
  }
}
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h3 { font-size: 16px; color: var(--text-primary); }
</style>
