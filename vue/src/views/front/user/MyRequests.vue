<template>
  <div class="my-requests-page">
    <div class="page-card">
      <div class="page-header">
        <h3>物料申领</h3>
        <el-button type="primary" @click="openDialog">
          <Icon icon="mdi:plus" width="16" style="margin-right:4px" />提交申领
        </el-button>
      </div>

      <el-table :data="tableData" stripe>
        <el-table-column prop="materialName" label="物料名称" min-width="120" />
        <el-table-column prop="materialCode" label="物料编码" width="120" />
        <el-table-column prop="quantity" label="申领数量" width="90" />
        <el-table-column prop="reason" label="申领原因" min-width="160" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reply" label="审批回复" min-width="140" show-overflow-tooltip />
        <el-table-column label="申领时间" width="160">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination background layout="total, prev, pager, next"
                       :total="total" :page-size="pageSize" v-model:current-page="pageNum" @current-change="loadData" />
      </div>
    </div>

    <el-dialog v-model="dialogVisible" title="提交物料申领" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="选择物料" prop="materialId">
          <el-select v-model="form.materialId" placeholder="请选择物料" filterable style="width:100%">
            <el-option v-for="m in materialList" :key="m.id" :label="`${m.name}（${m.code}）`" :value="m.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="申领数量" prop="quantity">
          <el-input-number v-model="form.quantity" :min="1" style="width:100%" />
        </el-form-item>
        <el-form-item label="申领原因" prop="reason">
          <el-input v-model="form.reason" type="textarea" :rows="3" placeholder="请说明申领原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Icon } from '@iconify/vue'
import request from '../../../api'

export default {
  name: 'MyRequests',
  components: { Icon },
  setup() {
    const tableData = ref([])
    const total = ref(0)
    const pageNum = ref(1)
    const pageSize = 10
    const dialogVisible = ref(false)
    const formRef = ref(null)
    const materialList = ref([])
    const form = reactive({ materialId: null, quantity: 1, reason: '' })
    const rules = {
      materialId: [{ required: true, message: '请选择物料', trigger: 'change' }],
      quantity: [{ required: true, message: '请输入数量', trigger: 'blur' }],
      reason: [{ required: true, message: '请输入原因', trigger: 'blur' }]
    }

    const statusLabel = (v) => ({ 0: '待审批', 1: '已批准', 2: '已拒绝' }[v] || '')
    const statusType = (v) => ({ 0: 'warning', 1: 'success', 2: 'danger' }[v] || '')
    const formatTime = (val) => {
      if (!val) return ''
      const d = new Date(val)
      const pad = (n) => String(n).padStart(2, '0')
      return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
    }

    const loadData = async () => {
      const res = await request.get('/api/materialRequest/list', { params: { pageNum: pageNum.value, pageSize } })
      if (res.code === 200) { tableData.value = res.data.list; total.value = res.data.total }
    }

    const openDialog = async () => {
      const res = await request.get('/api/material/list', { params: { pageNum: 1, pageSize: 100 } })
      if (res.code === 200) materialList.value = res.data.list
      Object.assign(form, { materialId: null, quantity: 1, reason: '' })
      dialogVisible.value = true
    }

    const handleSubmit = async () => {
      await formRef.value.validate()
      const res = await request.post('/api/materialRequest/save', form)
      if (res.code === 200) { ElMessage.success('申领已提交'); dialogVisible.value = false; loadData() }
      else ElMessage.error(res.message)
    }

    onMounted(loadData)
    return { tableData, total, pageNum, pageSize, dialogVisible, formRef, materialList, form, rules, statusLabel, statusType, formatTime, loadData, openDialog, handleSubmit }
  }
}
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h3 { font-size: 16px; color: var(--text-primary); }
</style>
