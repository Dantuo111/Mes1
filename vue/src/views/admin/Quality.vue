<template>
  <div class="quality-page">
    <div class="page-card">
      <div class="page-header">
        <h3>质量检验管理</h3>
        <el-button type="primary" @click="openDialog(null)">
          <Icon icon="mdi:clipboard-check" width="16" style="margin-right:4px" />新增检验记录
        </el-button>
      </div>

      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-date-picker v-model="query.startDate" type="date" placeholder="开始日期" value-format="YYYY-MM-DD" style="width:160px" />
        <el-date-picker v-model="query.endDate" type="date" placeholder="结束日期" value-format="YYYY-MM-DD" style="width:160px" />
        <el-select v-model="query.workOrderId" placeholder="选择工单" clearable filterable style="width:200px">
          <el-option v-for="wo in workOrderOptions" :key="wo.id" :label="wo.productName" :value="wo.id" />
        </el-select>
        <el-input-number v-model="query.minPassRate" :min="0" :max="100" :precision="0" placeholder="最低合格率" controls-position="right" style="width:140px" />
        <el-input-number v-model="query.maxPassRate" :min="0" :max="100" :precision="0" placeholder="最高合格率" controls-position="right" style="width:140px" />
        <el-button type="primary" @click="handleSearch">
          <Icon icon="mdi:magnify" width="16" style="margin-right:4px" />搜索
        </el-button>
      </div>

      <!-- 检验记录列表表格 -->
      <el-table :data="tableData" stripe>
        <el-table-column prop="workOrderProductName" label="工单产品" min-width="120" />
        <el-table-column label="检验日期" min-width="110">
          <template #default="{ row }">
            {{ formatDate(row.inspectionDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="inspector" label="检验人员" min-width="100" />
        <el-table-column prop="inspectionQuantity" label="检验数量" width="100" />
        <el-table-column prop="qualifiedQuantity" label="合格数量" width="100" />
        <el-table-column prop="unqualifiedQuantity" label="不合格数量" width="110" />
        <el-table-column label="合格率" width="100">
          <template #default="{ row }">
            <span :style="{ color: getPassRateColor(row.passRate), fontWeight: 'bold' }">
              {{ row.passRate != null ? row.passRate.toFixed(2) + '%' : '-' }}
            </span>
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

    <!-- 质量统计图表区域 -->
    <div class="page-card">
      <h4 style="font-size:15px;color:var(--text-primary);margin-bottom:16px">质量统计</h4>
      <div class="chart-row">
        <div class="chart-card">
          <h5>合格率趋势</h5>
          <div ref="lineChartRef" style="height:300px"></div>
        </div>
        <div class="chart-card">
          <h5>不合格原因分布</h5>
          <div ref="pieChartRef" style="height:300px"></div>
        </div>
      </div>
    </div>

    <!-- 新增/编辑检验记录弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑检验记录' : '新增检验记录'" width="550px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="关联工单" prop="workOrderId">
          <el-select v-model="form.workOrderId" placeholder="请选择工单" filterable style="width:100%">
            <el-option v-for="wo in workOrderOptions" :key="wo.id" :label="wo.productName" :value="wo.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="检验日期" prop="inspectionDate">
          <el-date-picker v-model="form.inspectionDate" type="date" placeholder="请选择检验日期" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item label="检验人员" prop="inspector">
          <el-input v-model="form.inspector" placeholder="请输入检验人员" />
        </el-form-item>
        <el-form-item label="检验数量" prop="inspectionQuantity">
          <el-input-number v-model="form.inspectionQuantity" :min="1" style="width:100%" />
        </el-form-item>
        <el-form-item label="合格数量" prop="qualifiedQuantity">
          <el-input-number v-model="form.qualifiedQuantity" :min="0" style="width:100%" />
        </el-form-item>
        <el-form-item label="不合格数量" prop="unqualifiedQuantity">
          <el-input-number v-model="form.unqualifiedQuantity" :min="0" style="width:100%" />
        </el-form-item>
        <el-form-item label="不合格原因" prop="unqualifiedReason">
          <el-input v-model="form.unqualifiedReason" type="textarea" :rows="3" placeholder="请输入不合格原因" />
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
import { ref, reactive, onMounted, nextTick, onBeforeUnmount } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Icon } from '@iconify/vue'
import * as echarts from 'echarts'
import request from '../../api'

export default {
  name: 'QualityPage',
  components: { Icon },
  setup() {
    const query = reactive({
      startDate: '',
      endDate: '',
      workOrderId: null,
      minPassRate: undefined,
      maxPassRate: undefined
    })
    const tableData = ref([])
    const total = ref(0)
    const pageNum = ref(1)
    const pageSize = 10

    // 工单下拉选项
    const workOrderOptions = ref([])

    // 弹窗
    const dialogVisible = ref(false)
    const isEdit = ref(false)
    const formRef = ref(null)
    const form = reactive({
      id: null,
      workOrderId: null,
      inspectionDate: '',
      inspector: '',
      inspectionQuantity: 1,
      qualifiedQuantity: 0,
      unqualifiedQuantity: 0,
      unqualifiedReason: ''
    })

    // 数量一致性校验
    const validateQuantity = (rule, value, callback) => {
      if (form.qualifiedQuantity != null && form.unqualifiedQuantity != null && form.inspectionQuantity != null) {
        if (form.qualifiedQuantity + form.unqualifiedQuantity !== form.inspectionQuantity) {
          callback(new Error('合格数量 + 不合格数量 必须等于检验数量'))
        } else {
          callback()
        }
      } else {
        callback()
      }
    }

    const rules = {
      workOrderId: [{ required: true, message: '请选择关联工单', trigger: 'change' }],
      inspectionDate: [{ required: true, message: '请选择检验日期', trigger: 'change' }],
      inspector: [{ required: true, message: '请输入检验人员', trigger: 'blur' }],
      inspectionQuantity: [
        { required: true, message: '请输入检验数量', trigger: 'blur' },
        { validator: validateQuantity, trigger: 'blur' }
      ],
      qualifiedQuantity: [
        { required: true, message: '请输入合格数量', trigger: 'blur' },
        { validator: validateQuantity, trigger: 'blur' }
      ],
      unqualifiedQuantity: [
        { required: true, message: '请输入不合格数量', trigger: 'blur' },
        { validator: validateQuantity, trigger: 'blur' }
      ]
    }

    // ECharts refs
    const lineChartRef = ref(null)
    const pieChartRef = ref(null)
    let lineChart = null
    let pieChart = null

    // 格式化日期
    const formatDate = (val) => {
      if (!val) return ''
      const d = new Date(val)
      const pad = (n) => String(n).padStart(2, '0')
      return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
    }

    // 合格率颜色
    const getPassRateColor = (rate) => {
      if (rate == null) return '#909399'
      if (rate >= 90) return '#2e7d32'
      if (rate >= 70) return '#ff8f00'
      return '#c62828'
    }

    // 加载工单下拉选项
    const loadWorkOrders = async () => {
      const res = await request.get('/api/workOrder/list', {
        params: { pageNum: 1, pageSize: 100 }
      })
      if (res.code === 200) {
        workOrderOptions.value = res.data.list || []
      }
    }

    // 加载检验记录列表
    const loadData = async () => {
      const res = await request.get('/api/quality/list', {
        params: {
          workOrderId: query.workOrderId || undefined,
          startDate: query.startDate || undefined,
          endDate: query.endDate || undefined,
          minPassRate: query.minPassRate != null ? query.minPassRate : undefined,
          maxPassRate: query.maxPassRate != null ? query.maxPassRate : undefined,
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

    // 加载质量统计图表
    const loadStats = async () => {
      const res = await request.get('/api/quality/stats')
      if (res.code === 200) {
        await nextTick()
        renderLineChart(res.data.passRateTrend || [])
        renderPieChart(res.data.unqualifiedReasonStats || [])
      }
    }

    const renderLineChart = (trendData) => {
      if (!lineChartRef.value) return
      if (lineChart) {
        lineChart.dispose()
      }
      lineChart = echarts.init(lineChartRef.value)
      const months = trendData.map(item => item.month)
      const rates = trendData.map(item => item.avgPassRate)
      // 计算Y轴最小值，让数据差异更明显
      const minRate = rates.length > 0 ? Math.floor(Math.min(...rates) / 5) * 5 - 5 : 0
      const yMin = Math.max(0, minRate)
      lineChart.setOption({
        tooltip: { trigger: 'axis', formatter: '{b}<br/>平均合格率：{c}%' },
        grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
        xAxis: { type: 'category', data: months, boundaryGap: false },
        yAxis: { type: 'value', name: '合格率(%)', min: yMin, max: 100 },
        series: [{
          name: '平均合格率',
          type: 'line',
          smooth: true,
          data: rates,
          itemStyle: { color: '#4caf50' },
          areaStyle: { color: 'rgba(76, 175, 80, 0.15)' }
        }]
      })
    }

    const renderPieChart = (reasonData) => {
      if (!pieChartRef.value) return
      if (pieChart) {
        pieChart.dispose()
      }
      pieChart = echarts.init(pieChartRef.value)
      const data = reasonData.map(item => ({
        name: item.reason || '未知原因',
        value: item.count
      }))
      pieChart.setOption({
        tooltip: { trigger: 'item', formatter: '{b}：{c}次 ({d}%)' },
        color: ['#ef5350', '#ff7043', '#ffa726', '#ffca28', '#66bb6a', '#42a5f5', '#ab47bc'],
        series: [{
          type: 'pie',
          radius: ['40%', '70%'],
          data: data,
          label: { formatter: '{b}\n{d}%' }
        }]
      })
    }

    // 窗口resize时重绘图表
    const handleResize = () => {
      if (lineChart) lineChart.resize()
      if (pieChart) pieChart.resize()
    }

    // 弹窗操作
    const openDialog = (row) => {
      if (row) {
        isEdit.value = true
        Object.assign(form, {
          id: row.id,
          workOrderId: row.workOrderId,
          inspectionDate: formatDate(row.inspectionDate),
          inspector: row.inspector || '',
          inspectionQuantity: row.inspectionQuantity,
          qualifiedQuantity: row.qualifiedQuantity,
          unqualifiedQuantity: row.unqualifiedQuantity,
          unqualifiedReason: row.unqualifiedReason || ''
        })
      } else {
        isEdit.value = false
        Object.assign(form, {
          id: null,
          workOrderId: null,
          inspectionDate: '',
          inspector: '',
          inspectionQuantity: 1,
          qualifiedQuantity: 0,
          unqualifiedQuantity: 0,
          unqualifiedReason: ''
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
        // 前端二次校验数量一致性
        if (form.qualifiedQuantity + form.unqualifiedQuantity !== form.inspectionQuantity) {
          ElMessage.error('合格数量 + 不合格数量 必须等于检验数量')
          return
        }
        const data = { ...form }
        const res = await request.post('/api/quality/save', data)
        if (res.code === 200) {
          ElMessage.success(isEdit.value ? '编辑成功' : '新增成功')
          dialogVisible.value = false
          loadData()
          loadStats()
        } else {
          ElMessage.error(res.message)
        }
      })
    }

    const handleDelete = (row) => {
      ElMessageBox.confirm('确定要删除该检验记录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        const res = await request.delete(`/api/quality/delete/${row.id}`)
        if (res.code === 200) {
          ElMessage.success('删除成功')
          loadData()
          loadStats()
        } else {
          ElMessage.error(res.message)
        }
      }).catch(() => {})
    }

    onMounted(async () => {
      await loadWorkOrders()
      await loadData()
      await loadStats()
      window.addEventListener('resize', handleResize)
    })

    onBeforeUnmount(() => {
      window.removeEventListener('resize', handleResize)
      if (lineChart) {
        lineChart.dispose()
        lineChart = null
      }
      if (pieChart) {
        pieChart.dispose()
        pieChart = null
      }
    })

    return {
      query, tableData, total, pageNum, pageSize,
      workOrderOptions,
      dialogVisible, isEdit, formRef, form, rules,
      lineChartRef, pieChartRef,
      formatDate, getPassRateColor,
      loadData, handleSearch,
      openDialog, resetForm, handleSave, handleDelete
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
.chart-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}
.chart-card h5 {
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 8px;
}
</style>
