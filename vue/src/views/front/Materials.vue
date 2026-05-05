<template>
  <div class="front-materials-page">
    <div class="page-card">
      <h3>物料库存查询</h3>
      <div class="search-bar" style="margin-top:16px">
        <el-input v-model="query.keyword" placeholder="搜索物料编码/名称" clearable style="width:220px" />
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button type="warning" @click="showWarning = !showWarning">
          <Icon icon="mdi:alert" width="16" style="margin-right:4px" />
          {{ showWarning ? '显示全部' : '库存预警' }}
        </el-button>
      </div>

      <el-table :data="displayData" stripe :row-class-name="rowClassName">
        <el-table-column prop="code" label="物料编码" min-width="120" />
        <el-table-column prop="name" label="物料名称" min-width="120" />
        <el-table-column prop="spec" label="规格" min-width="100" />
        <el-table-column prop="unit" label="单位" width="80" />
        <el-table-column label="库存数量" width="120">
          <template #default="{ row }">
            <span :style="{ color: row.stockQuantity < row.safetyStock ? '#E6A23C' : '', fontWeight: row.stockQuantity < row.safetyStock ? 'bold' : '' }">
              {{ row.stockQuantity }}
              <Icon v-if="row.stockQuantity < row.safetyStock" icon="mdi:alert-circle" width="14" style="color:#E6A23C;vertical-align:middle" />
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="safetyStock" label="安全库存" width="100" />
        <el-table-column label="库存状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.stockQuantity >= row.safetyStock ? 'success' : 'danger'" size="small">
              {{ row.stockQuantity >= row.safetyStock ? '正常' : '预警' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="displayData.length === 0" description="暂无物料数据" />

      <div class="pagination-wrap" v-if="!showWarning">
        <el-pagination background layout="total, prev, pager, next"
                       :total="total" :page-size="pageSize" v-model:current-page="pageNum" @current-change="loadData" />
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted } from 'vue'
import { Icon } from '@iconify/vue'
import request from '../../api'

export default {
  name: 'FrontMaterials',
  components: { Icon },
  setup() {
    const query = reactive({ keyword: '' })
    const tableData = ref([])
    const warningData = ref([])
    const total = ref(0)
    const pageNum = ref(1)
    const pageSize = 10
    const showWarning = ref(false)

    const displayData = computed(() => showWarning.value ? warningData.value : tableData.value)

    const rowClassName = ({ row }) => row.stockQuantity < row.safetyStock ? 'warning-row' : ''

    const loadData = async () => {
      const res = await request.get('/api/material/list', {
        params: { keyword: query.keyword || undefined, pageNum: pageNum.value, pageSize }
      })
      if (res.code === 200) { tableData.value = res.data.list; total.value = res.data.total }
    }

    const loadWarning = async () => {
      const res = await request.get('/api/material/warning')
      if (res.code === 200) { warningData.value = res.data }
    }

    const handleSearch = () => { pageNum.value = 1; loadData() }

    onMounted(() => { loadData(); loadWarning() })
    return { query, tableData, warningData, total, pageNum, pageSize, showWarning, displayData, rowClassName, loadData, handleSearch }
  }
}
</script>

<style scoped>
h3 { font-size: 16px; color: var(--text-primary); }
</style>
<style>
.front-materials-page .el-table .warning-row { background-color: #fdf6ec !important; }
</style>
