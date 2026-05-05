<template>
  <div class="production-plans-page">
    <div class="page-card">
      <div class="page-header">
        <h3>生产计划管理</h3>
        <el-button type="primary" @click="openDialog(null)">
          <Icon icon="mdi:clipboard-plus" width="16" style="margin-right:4px" />新增计划
        </el-button>
      </div>

      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-input v-model="query.keyword" placeholder="搜索产品名称" clearable style="width:220px" />
        <el-select v-model="query.status" placeholder="状态" clearable style="width:140px">
          <el-option label="待执行" :value="0" />
          <el-option label="执行中" :value="1" />
          <el-option label="已完成" :value="2" />
        </el-select>
        <el-button type="primary" @click="handleSearch">
          <Icon icon="mdi:magnify" width="16" style="margin-right:4px" />搜索
        </el-button>
      </div>

      <!-- 计划列表表格 -->
      <el-table :data="tableData" stripe>
        <el-table-column prop="productName" label="产品名称" min-width="140" />
        <el-table-column prop="plannedQuantity" label="计划数量" width="100" />
        <el-table-column label="开始时间" min-width="160">
          <template #default="{ row }">
            {{ formatTime(row.startTime) }}
          </template>
        </el-table-column>
        <el-table-column label="结束时间" min-width="160">
          <template #default="{ row }">
            {{ formatTime(row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column label="优先级" width="90">
          <template #default="{ row }">
            <el-tag :type="priorityTagType(row.priority)">{{ priorityLabel(row.priority) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-tooltip content="编辑" placement="top">
              <el-button type="primary" size="small" circle @click="openDialog(row)">
                <Icon icon="mdi:pencil" width="14" />
              </el-button>
            </el-tooltip>
            <el-tooltip content="删除" placement="top">
              <el-button type="danger" size="small" circle @click="handleDelete(row)">
                <Icon icon="mdi:delete" width="14" />
              </el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrap">
        <el-pagination background layout="total, prev, pager, next"
                       :total="total" :page-size="pageSize" v-model:current-page="pageNum" @current-change="loadData" />
      </div>
    </div>

    <!-- 新增/编辑计划弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑生产计划' : '新增生产计划'" width="550px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="产品名称" prop="productName">
          <el-input v-model="form.productName" placeholder="请输入产品名称" />
        </el-form-item>
        <el-form-item label="计划数量" prop="plannedQuantity">
          <el-input-number v-model="form.plannedQuantity" :min="1" placeholder="请输入计划数量" style="width:100%" />
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker v-model="form.startTime" type="datetime" placeholder="请选择开始时间" style="width:100%" />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker v-model="form.endTime" type="datetime" placeholder="请选择结束时间" style="width:100%" />
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-select v-model="form.priority" placeholder="请选择优先级" style="width:100%">
            <el-option label="低" :value="0" />
            <el-option label="中" :value="1" />
            <el-option label="高" :value="2" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Icon } from '@iconify/vue'
import request from '../../api'

export default {
  name: 'ProductionPlansPage',
  components: { Icon },
  setup() {
    const query = reactive({ keyword: '', status: null })
    const tableData = ref([])
    const total = ref(0)
    const pageNum = ref(1)
    const pageSize = 10

    const dialogVisible = ref(false)
    const isEdit = ref(false)
    const formRef = ref(null)
    const form = reactive({
      id: null,
      productName: '',
      plannedQuantity: 1,
      startTime: null,
      endTime: null,
      priority: 1
    })

    const rules = {
      productName: [{ required: true, message: '请输入产品名称', trigger: 'blur' }],
      plannedQuantity: [{ required: true, message: '请输入计划数量', trigger: 'blur' }],
      startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
      endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
      priority: [{ required: true, message: '请选择优先级', trigger: 'change' }]
    }

    // 优先级标签
    const priorityLabel = (val) => {
      const map = { 0: '低', 1: '中', 2: '高' }
      return map[val] ?? '未知'
    }
    const priorityTagType = (val) => {
      const map = { 0: 'info', 1: 'warning', 2: 'danger' }
      return map[val] ?? 'info'
    }

    // 状态标签
    const statusLabel = (val) => {
      const map = { 0: '待执行', 1: '执行中', 2: '已完成' }
      return map[val] ?? '未知'
    }
    const statusTagType = (val) => {
      const map = { 0: 'info', 1: 'warning', 2: 'success' }
      return map[val] ?? 'info'
    }

    // 格式化时间
    const formatTime = (val) => {
      if (!val) return ''
      const d = new Date(val)
      const pad = (n) => String(n).padStart(2, '0')
      return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
    }

    const loadData = async () => {
      const res = await request.get('/api/productionPlan/list', {
        params: {
          keyword: query.keyword || undefined,
          status: query.status !== null ? query.status : undefined,
          pageNum: pageNum.value,
          pageSize
        }
      })
      if (res.code === 200) {
        tableData.value = res.data.list
        total.value = res.data.total
      }
    }

    const handleSearch = () => {
      pageNum.value = 1
      loadData()
    }

    const openDialog = (row) => {
      if (row) {
        isEdit.value = true
        Object.assign(form, {
          id: row.id,
          productName: row.productName,
          plannedQuantity: row.plannedQuantity,
          startTime: row.startTime ? new Date(row.startTime) : null,
          endTime: row.endTime ? new Date(row.endTime) : null,
          priority: row.priority
        })
      } else {
        isEdit.value = false
        Object.assign(form, {
          id: null,
          productName: '',
          plannedQuantity: 1,
          startTime: null,
          endTime: null,
          priority: 1
        })
      }
      dialogVisible.value = true
    }

    const resetForm = () => {
      if (formRef.value) {
        formRef.value.resetFields()
      }
    }

    const handleSave = async () => {
      if (!formRef.value) return
      await formRef.value.validate(async (valid) => {
        if (!valid) return
        const data = { ...form }
        const res = await request.post('/api/productionPlan/save', data)
        if (res.code === 200) {
          ElMessage.success(isEdit.value ? '编辑成功' : '新增成功')
          dialogVisible.value = false
          loadData()
        } else {
          ElMessage.error(res.message)
        }
      })
    }

    const handleDelete = (row) => {
      ElMessageBox.confirm(`确定要删除生产计划「${row.productName}」吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        const res = await request.delete(`/api/productionPlan/delete/${row.id}`)
        if (res.code === 200) {
          ElMessage.success('删除成功')
          loadData()
        } else {
          ElMessage.error(res.message)
        }
      }).catch(() => {})
    }

    onMounted(loadData)

    return {
      query, tableData, total, pageNum, pageSize,
      dialogVisible, isEdit, formRef, form, rules,
      priorityLabel, priorityTagType, statusLabel, statusTagType, formatTime,
      loadData, handleSearch, openDialog, resetForm,
      handleSave, handleDelete
    }
  }
}
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.page-header h3 {
  font-size: 16px;
  color: var(--text-primary);
}
</style>
