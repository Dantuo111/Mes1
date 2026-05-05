<template>
  <div class="materials-page">
    <div class="page-card">
      <div class="page-header">
        <h3>物料管理</h3>
        <div>
          <el-button type="warning" @click="openWarningDialog">
            <Icon icon="mdi:alert" width="16" style="margin-right:4px" />库存预警
          </el-button>
          <el-button type="primary" @click="openDialog(null)">
            <Icon icon="mdi:package-variant-plus" width="16" style="margin-right:4px" />新增物料
          </el-button>
        </div>
      </div>

      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-input v-model="query.keyword" placeholder="搜索物料编码/名称" clearable style="width:220px" />
        <el-button type="primary" @click="handleSearch">
          <Icon icon="mdi:magnify" width="16" style="margin-right:4px" />搜索
        </el-button>
      </div>

      <!-- 物料列表表格 -->
      <el-table :data="tableData" stripe :row-class-name="tableRowClassName">
        <el-table-column prop="code" label="物料编码" min-width="120" />
        <el-table-column prop="name" label="物料名称" min-width="120" />
        <el-table-column prop="spec" label="规格" min-width="100" />
        <el-table-column prop="unit" label="单位" width="80" />
        <el-table-column label="库存数量" width="110">
          <template #default="{ row }">
            <span :class="{ 'warning-text': row.stockQuantity < row.safetyStock }">
              {{ row.stockQuantity }}
              <Icon v-if="row.stockQuantity < row.safetyStock" icon="mdi:alert-circle" width="14" style="color:#E6A23C;vertical-align:middle;margin-left:2px" />
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="safetyStock" label="安全库存" width="100" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-tooltip content="编辑" placement="top">
              <el-button type="primary" size="small" circle @click="openDialog(row)">
                <Icon icon="mdi:pencil" width="14" />
              </el-button>
            </el-tooltip>
            <el-tooltip content="入库" placement="top">
              <el-button type="success" size="small" circle @click="openInboundDialog(row)">
                <Icon icon="mdi:package-down" width="14" />
              </el-button>
            </el-tooltip>
            <el-tooltip content="出库" placement="top">
              <el-button type="warning" size="small" circle @click="openOutboundDialog(row)">
                <Icon icon="mdi:package-up" width="14" />
              </el-button>
            </el-tooltip>
            <el-tooltip content="记录" placement="top">
              <el-button type="info" size="small" circle @click="openRecordsDialog(row)">
                <Icon icon="mdi:history" width="14" />
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

    <!-- 新增/编辑物料弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑物料' : '新增物料'" width="500px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="物料编码" prop="code">
          <el-input v-model="form.code" placeholder="请输入物料编码" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="物料名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入物料名称" />
        </el-form-item>
        <el-form-item label="规格" prop="spec">
          <el-input v-model="form.spec" placeholder="请输入规格" />
        </el-form-item>
        <el-form-item label="单位" prop="unit">
          <el-input v-model="form.unit" placeholder="请输入单位（如：个、kg、米）" />
        </el-form-item>
        <el-form-item label="安全库存" prop="safetyStock">
          <el-input-number v-model="form.safetyStock" :min="0" placeholder="请输入安全库存" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">确定</el-button>
      </template>
    </el-dialog>

    <!-- 入库弹窗 -->
    <el-dialog v-model="inboundVisible" title="物料入库" width="400px">
      <p style="margin-bottom:12px;color:#606266">物料：<strong>{{ currentMaterial.name }}</strong>（{{ currentMaterial.code }}）</p>
      <p style="margin-bottom:16px;color:#606266">当前库存：<strong>{{ currentMaterial.stockQuantity }}</strong> {{ currentMaterial.unit }}</p>
      <el-form label-width="80px">
        <el-form-item label="入库数量">
          <el-input-number v-model="inboundQuantity" :min="1" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="inboundVisible = false">取消</el-button>
        <el-button type="primary" @click="handleInbound">确定入库</el-button>
      </template>
    </el-dialog>

    <!-- 出库弹窗 -->
    <el-dialog v-model="outboundVisible" title="物料出库" width="400px">
      <p style="margin-bottom:12px;color:#606266">物料：<strong>{{ currentMaterial.name }}</strong>（{{ currentMaterial.code }}）</p>
      <p style="margin-bottom:16px;color:#606266">当前库存：<strong>{{ currentMaterial.stockQuantity }}</strong> {{ currentMaterial.unit }}</p>
      <el-form label-width="80px">
        <el-form-item label="出库数量">
          <el-input-number v-model="outboundQuantity" :min="1" :max="currentMaterial.stockQuantity" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="outboundVisible = false">取消</el-button>
        <el-button type="primary" @click="handleOutbound">确定出库</el-button>
      </template>
    </el-dialog>

    <!-- 库存预警弹窗 -->
    <el-dialog v-model="warningVisible" title="库存预警列表" width="700px">
      <el-table :data="warningList" stripe>
        <el-table-column prop="code" label="物料编码" min-width="120" />
        <el-table-column prop="name" label="物料名称" min-width="120" />
        <el-table-column prop="spec" label="规格" min-width="100" />
        <el-table-column prop="unit" label="单位" width="80" />
        <el-table-column label="库存数量" width="110">
          <template #default="{ row }">
            <span class="warning-text">{{ row.stockQuantity }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="safetyStock" label="安全库存" width="100" />
      </el-table>
      <template #footer>
        <el-button @click="warningVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 出入库记录弹窗 -->
    <el-dialog v-model="recordsVisible" title="出入库记录" width="700px">
      <p style="margin-bottom:12px;color:#606266">物料：<strong>{{ currentMaterial.name }}</strong>（{{ currentMaterial.code }}）</p>
      <el-table :data="recordsList" stripe>
        <el-table-column label="类型" width="80">
          <template #default="{ row }">
            <el-tag :type="row.type === 0 ? 'success' : 'danger'">{{ row.type === 0 ? '入库' : '出库' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="quantity" label="操作数量" width="100" />
        <el-table-column prop="beforeQuantity" label="操作前库存" width="110" />
        <el-table-column prop="afterQuantity" label="操作后库存" width="110" />
        <el-table-column label="操作时间" min-width="160">
          <template #default="{ row }">
            {{ formatTime(row.operateTime) }}
          </template>
        </el-table-column>
      </el-table>
      <!-- 记录分页 -->
      <div class="pagination-wrap" style="margin-top:12px">
        <el-pagination background layout="total, prev, pager, next"
                       :total="recordsTotal" :page-size="recordsPageSize" v-model:current-page="recordsPageNum" @current-change="loadRecords" />
      </div>
      <template #footer>
        <el-button @click="recordsVisible = false">关闭</el-button>
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
  name: 'MaterialsPage',
  components: { Icon },
  setup() {
    const query = reactive({ keyword: '' })
    const tableData = ref([])
    const total = ref(0)
    const pageNum = ref(1)
    const pageSize = 10

    // 新增/编辑弹窗
    const dialogVisible = ref(false)
    const isEdit = ref(false)
    const formRef = ref(null)
    const form = reactive({
      id: null,
      code: '',
      name: '',
      spec: '',
      unit: '',
      safetyStock: 0
    })

    const rules = {
      code: [{ required: true, message: '请输入物料编码', trigger: 'blur' }],
      name: [{ required: true, message: '请输入物料名称', trigger: 'blur' }]
    }

    // 入库弹窗
    const inboundVisible = ref(false)
    const inboundQuantity = ref(1)

    // 出库弹窗
    const outboundVisible = ref(false)
    const outboundQuantity = ref(1)

    // 当前操作的物料
    const currentMaterial = reactive({ id: null, name: '', code: '', stockQuantity: 0, unit: '' })

    // 库存预警弹窗
    const warningVisible = ref(false)
    const warningList = ref([])

    // 出入库记录弹窗
    const recordsVisible = ref(false)
    const recordsList = ref([])
    const recordsTotal = ref(0)
    const recordsPageNum = ref(1)
    const recordsPageSize = 10

    // 格式化时间
    const formatTime = (val) => {
      if (!val) return ''
      const d = new Date(val)
      const pad = (n) => String(n).padStart(2, '0')
      return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
    }

    // 表格行样式：库存低于安全库存时标记警告
    const tableRowClassName = ({ row }) => {
      if (row.stockQuantity < row.safetyStock) {
        return 'warning-row'
      }
      return ''
    }

    const loadData = async () => {
      const res = await request.get('/api/material/list', {
        params: {
          keyword: query.keyword || undefined,
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
          code: row.code,
          name: row.name,
          spec: row.spec || '',
          unit: row.unit || '',
          safetyStock: row.safetyStock || 0
        })
      } else {
        isEdit.value = false
        Object.assign(form, {
          id: null,
          code: '',
          name: '',
          spec: '',
          unit: '',
          safetyStock: 0
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
        const res = await request.post('/api/material/save', data)
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
      ElMessageBox.confirm(`确定要删除物料「${row.name}」吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        const res = await request.delete(`/api/material/delete/${row.id}`)
        if (res.code === 200) {
          ElMessage.success('删除成功')
          loadData()
        } else {
          ElMessage.error(res.message)
        }
      }).catch(() => {})
    }

    // 入库
    const openInboundDialog = (row) => {
      Object.assign(currentMaterial, { id: row.id, name: row.name, code: row.code, stockQuantity: row.stockQuantity, unit: row.unit || '' })
      inboundQuantity.value = 1
      inboundVisible.value = true
    }

    const handleInbound = async () => {
      const res = await request.post('/api/material/inbound', {
        materialId: currentMaterial.id,
        quantity: inboundQuantity.value
      })
      if (res.code === 200) {
        ElMessage.success('入库成功')
        inboundVisible.value = false
        loadData()
      } else {
        ElMessage.error(res.message)
      }
    }

    // 出库
    const openOutboundDialog = (row) => {
      Object.assign(currentMaterial, { id: row.id, name: row.name, code: row.code, stockQuantity: row.stockQuantity, unit: row.unit || '' })
      outboundQuantity.value = 1
      outboundVisible.value = true
    }

    const handleOutbound = async () => {
      const res = await request.post('/api/material/outbound', {
        materialId: currentMaterial.id,
        quantity: outboundQuantity.value
      })
      if (res.code === 200) {
        ElMessage.success('出库成功')
        outboundVisible.value = false
        loadData()
      } else {
        ElMessage.error(res.message)
      }
    }

    // 库存预警
    const openWarningDialog = async () => {
      const res = await request.get('/api/material/warning')
      if (res.code === 200) {
        warningList.value = res.data
        warningVisible.value = true
      }
    }

    // 出入库记录
    const openRecordsDialog = (row) => {
      Object.assign(currentMaterial, { id: row.id, name: row.name, code: row.code, stockQuantity: row.stockQuantity, unit: row.unit || '' })
      recordsPageNum.value = 1
      recordsVisible.value = true
      loadRecords()
    }

    const loadRecords = async () => {
      const res = await request.get('/api/material/records', {
        params: {
          materialId: currentMaterial.id,
          pageNum: recordsPageNum.value,
          pageSize: recordsPageSize
        }
      })
      if (res.code === 200) {
        recordsList.value = res.data.list
        recordsTotal.value = res.data.total
      }
    }

    onMounted(loadData)

    return {
      query, tableData, total, pageNum, pageSize,
      dialogVisible, isEdit, formRef, form, rules,
      inboundVisible, inboundQuantity,
      outboundVisible, outboundQuantity,
      currentMaterial,
      warningVisible, warningList,
      recordsVisible, recordsList, recordsTotal, recordsPageNum, recordsPageSize,
      formatTime, tableRowClassName,
      loadData, handleSearch, openDialog, resetForm,
      handleSave, handleDelete,
      openInboundDialog, handleInbound,
      openOutboundDialog, handleOutbound,
      openWarningDialog,
      openRecordsDialog, loadRecords
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
.warning-text {
  color: #E6A23C;
  font-weight: bold;
}
</style>

<style>
/* 库存预警行样式（非scoped，因为el-table通过row-class-name注入） */
.materials-page .el-table .warning-row {
  background-color: #fdf6ec !important;
}
.materials-page .el-table .warning-row td {
  color: #E6A23C;
}
</style>
