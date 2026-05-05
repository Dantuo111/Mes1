<template>
  <div class="work-orders-page">
    <div class="page-card">
      <div class="page-header">
        <h3>工单管理</h3>
        <el-button type="primary" @click="openDialog(null)">
          <Icon icon="mdi:clipboard-plus" width="16" style="margin-right:4px" />新增工单
        </el-button>
      </div>

      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-select v-model="query.status" placeholder="工单状态" clearable style="width:150px">
          <el-option label="待派发" :value="0" />
          <el-option label="待接收" :value="1" />
          <el-option label="待开工" :value="2" />
          <el-option label="执行中" :value="3" />
          <el-option label="已完工" :value="4" />
          <el-option label="已关闭" :value="5" />
        </el-select>
        <el-button type="primary" @click="handleSearch">
          <Icon icon="mdi:magnify" width="16" style="margin-right:4px" />搜索
        </el-button>
      </div>

      <!-- 工单列表表格 -->
      <el-table :data="tableData" stripe>
        <el-table-column prop="productName" label="产品名称" min-width="140" />
        <el-table-column prop="quantity" label="生产数量" width="100" />
        <el-table-column prop="assigneeName" label="负责人" width="100" />
        <el-table-column prop="processRequirement" label="工序要求" min-width="160" show-overflow-tooltip />
        <el-table-column label="交付日期" width="120">
          <template #default="{ row }">
            {{ formatDate(row.deliveryDate) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-tooltip content="编辑" placement="top">
              <el-button type="primary" size="small" circle @click="openDialog(row)">
                <Icon icon="mdi:pencil" width="14" />
              </el-button>
            </el-tooltip>
            <el-tooltip v-if="row.status === 0" content="派发" placement="top">
              <el-button type="success" size="small" circle @click="handleDispatch(row)">
                <Icon icon="mdi:send" width="14" />
              </el-button>
            </el-tooltip>
            <el-tooltip v-if="row.status === 4" content="关闭" placement="top">
              <el-button type="warning" size="small" circle @click="handleClose(row)">
                <Icon icon="mdi:lock" width="14" />
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

    <!-- 新增/编辑工单弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑工单' : '新增工单'" width="550px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="生产计划" prop="planId">
          <el-select v-model="form.planId" placeholder="请选择生产计划" style="width:100%" @change="handlePlanChange">
            <el-option v-for="plan in planList" :key="plan.id" :label="plan.productName" :value="plan.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="负责人" prop="assigneeId">
          <el-select v-model="form.assigneeId" placeholder="请选择负责人" style="width:100%">
            <el-option v-for="user in userList" :key="user.id" :label="user.nickname || user.username" :value="user.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="产品名称" prop="productName">
          <el-input v-model="form.productName" placeholder="请输入产品名称" />
        </el-form-item>
        <el-form-item label="生产数量" prop="quantity">
          <el-input-number v-model="form.quantity" :min="1" placeholder="请输入生产数量" style="width:100%" />
        </el-form-item>
        <el-form-item label="工序要求" prop="processRequirement">
          <el-input v-model="form.processRequirement" type="textarea" :rows="3" placeholder="请输入工序要求" />
        </el-form-item>
        <el-form-item label="交付日期" prop="deliveryDate">
          <el-date-picker v-model="form.deliveryDate" type="date" placeholder="请选择交付日期" style="width:100%" />
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
  name: 'WorkOrdersPage',
  components: { Icon },
  setup() {
    const query = reactive({ status: null })
    const tableData = ref([])
    const total = ref(0)
    const pageNum = ref(1)
    const pageSize = 10

    const dialogVisible = ref(false)
    const isEdit = ref(false)
    const formRef = ref(null)
    const form = reactive({
      id: null,
      planId: null,
      productName: '',
      quantity: 1,
      assigneeId: null,
      processRequirement: '',
      deliveryDate: null
    })

    const planList = ref([])
    const userList = ref([])

    const rules = {
      planId: [{ required: true, message: '请选择生产计划', trigger: 'change' }],
      assigneeId: [{ required: true, message: '请选择负责人', trigger: 'change' }],
      productName: [{ required: true, message: '请输入产品名称', trigger: 'blur' }],
      quantity: [{ required: true, message: '请输入生产数量', trigger: 'blur' }],
      deliveryDate: [{ required: true, message: '请选择交付日期', trigger: 'change' }]
    }

    // 状态标签
    const statusLabel = (val) => {
      const map = { 0: '待派发', 1: '待接收', 2: '待开工', 3: '执行中', 4: '已完工', 5: '已关闭' }
      return map[val] ?? '未知'
    }
    const statusTagType = (val) => {
      const map = { 0: 'info', 1: 'warning', 2: '', 3: '', 4: 'success', 5: 'info' }
      return map[val] ?? 'info'
    }

    // 格式化日期
    const formatDate = (val) => {
      if (!val) return ''
      const d = new Date(val)
      const pad = (n) => String(n).padStart(2, '0')
      return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
    }

    const loadData = async () => {
      const res = await request.get('/api/workOrder/list', {
        params: {
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

    // 加载生产计划列表（用于下拉选择）
    const loadPlanList = async () => {
      const res = await request.get('/api/productionPlan/list', {
        params: { pageNum: 1, pageSize: 100 }
      })
      if (res.code === 200) {
        planList.value = res.data.list
      }
    }

    // 加载员工列表（用于负责人下拉选择）
    const loadUserList = async () => {
      const res = await request.get('/api/user/list', {
        params: { role: 0, pageNum: 1, pageSize: 100 }
      })
      if (res.code === 200) {
        userList.value = res.data.list
      }
    }

    // 选择生产计划后自动填充产品名称
    const handlePlanChange = (planId) => {
      const plan = planList.value.find(p => p.id === planId)
      if (plan) {
        form.productName = plan.productName
      }
    }

    const openDialog = async (row) => {
      // 加载下拉数据
      await Promise.all([loadPlanList(), loadUserList()])

      if (row) {
        isEdit.value = true
        Object.assign(form, {
          id: row.id,
          planId: row.planId,
          productName: row.productName,
          quantity: row.quantity,
          assigneeId: row.assigneeId,
          processRequirement: row.processRequirement || '',
          deliveryDate: row.deliveryDate ? new Date(row.deliveryDate) : null
        })
      } else {
        isEdit.value = false
        Object.assign(form, {
          id: null,
          planId: null,
          productName: '',
          quantity: 1,
          assigneeId: null,
          processRequirement: '',
          deliveryDate: null
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
        const res = await request.post('/api/workOrder/save', data)
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
      ElMessageBox.confirm(`确定要删除工单「${row.productName}」吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        const res = await request.delete(`/api/workOrder/delete/${row.id}`)
        if (res.code === 200) {
          ElMessage.success('删除成功')
          loadData()
        } else {
          ElMessage.error(res.message)
        }
      }).catch(() => {})
    }

    const handleDispatch = (row) => {
      ElMessageBox.confirm(`确定要派发工单「${row.productName}」吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        const res = await request.post(`/api/workOrder/dispatch/${row.id}`)
        if (res.code === 200) {
          ElMessage.success('派发成功')
          loadData()
        } else {
          ElMessage.error(res.message)
        }
      }).catch(() => {})
    }

    const handleClose = (row) => {
      ElMessageBox.confirm(`确定要关闭工单「${row.productName}」吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        const res = await request.post(`/api/workOrder/close/${row.id}`)
        if (res.code === 200) {
          ElMessage.success('关闭成功')
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
      planList, userList,
      statusLabel, statusTagType, formatDate,
      loadData, handleSearch, handlePlanChange,
      openDialog, resetForm, handleSave,
      handleDelete, handleDispatch, handleClose
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
